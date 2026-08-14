# KB Ingest 工作台（T15）代码审查报告（2026-06-25）

> 同目录 [`kb-sync-ask-code-review-2026-06-25.md`](kb-sync-ask-code-review-2026-06-25.md) 审的是 CI Sync / Ask 链路，本报告审的是**本次新增的 Ingest 工作台（T15a–e）**，两份互不覆盖。

## 背景

本次交付「Web 批次厚 Ingest 工作台」：raw 选源 → Plan 去重 → 多页 LLM 草稿 → 逐页 diff 审阅 → lint 门禁 → 原子写 wiki（log/edges/index）→ 可选 Sync；外加 enrich patch、断点续跑、批次模板。审查目标：

- 红线是否被后端强制（禁止 raw→DB、无 plan 生成、无 diff commit）
- 事务 / 文件一致性、并发、性能
- 安全（路径穿越、ACL）
- 可维护性

## 审查范围

- `service/impl/KbIngestServiceImpl.java`（核心，~1670 行）
- `controller/KbIngestController.java`
- `service/impl/KbWikiFileServiceImpl.java`（落盘复用）
- `dto/Ingest*.java`、`entity/KbIngest*.java`
- `docs/sql/08_kb_ingest_workbench.sql`、`09_kb_ingest_t15e.sql`
- meiling-ui `KnowledgeIngestWorkbenchView.vue`

## 总体结论

实现完成度高，分层清晰，**产品红线已在后端硬性拦截**，路径穿越与 ACL 防护到位，LLM 输出做了防御式解析。主要风险集中在 **commit 的事务/文件一致性** 与 **generate 的失败语义**，建议在上线前处理 P1。

| 维度 | 评价 |
|------|------|
| 功能完整性 | ✅ 覆盖 T15a–e，与 API 文档 §9 一致 |
| 安全（穿越/ACL） | ✅ 每个入口 `assertEnabled + assertCanRead/Edit`，`normalizeUnder` / `resolveWikiRelFile` 双重 `startsWith` 校验 |
| 事务一致性 | ⚠️ 见 P1-1、P1-2 |
| 失败语义 | ⚠️ 见 P1-3 |
| 性能 | ⚠️ N+1，见 P2-1 |
| 可维护性 | 🟡 魔法字符串偏多，见 P3 |

## 主要发现（按严重级别）

### P1（高）commit：文件写入与 DB 事务边界不一致

**现象**

`commit()` 标注 `@Transactional(rollbackFor = Exception.class)`，但事务内顺序为：

1. 循环 `kbWikiFileService.writePage(...)`（**写磁盘**）
2. `appendEdges` / `appendLog` / `appendIndexSection`（**写磁盘**）
3. `commitMapper.insert(...)`、`jobMapper.updateById(...)`（DB）

文件 I/O 不受 DB 事务管辖。若第 3 步（或其后任一 DB 操作）抛异常，DB 回滚，但 **wiki 文件、log.md、edges.jsonl、index.md 已经落盘且不会撤销** → 产生「文件已改但 job 仍是 reviewing、无 commit 记录」的脏状态，重复 commit 会二次追加 log/edges/index。

**影响**：数据不一致、log/edges/index 重复污染（与产品红线「只写 articles 不更新治理文件」相对，是另一种「治理文件被重复写」）。

**建议**

- 调整顺序：**先做可回滚的 DB 校验/插入，再做文件落盘**；或将文件落盘移出事务、落盘后再单独提交「commit 记录 + 状态」，失败时记录补偿日志。
- edges/log/index 追加做幂等保护（如按 `批次#` 去重，重复 commit 不重复追加）。
- 文档已写明「文件写入非事务，失败需人工核对 `git status`」，但当前**顺序**让 DB 失败也会留下文件，建议至少把 DB 写放在文件写之前。

### P1（高）Sync 子进程在事务内执行

**现象**

`if (sync)` 分支里的 `kbSyncService.trigger(...)` 仍在 `commit()` 的 `@Transactional` 范围内。Sync 通常会拉起外部 `sync_to_db.py` 子进程（数秒~数十秒）。

**影响**：长事务占用 DB 连接 / 潜在锁，子进程期间事务一直挂着；Sync 慢会放大连接池压力。

**建议**：将 Sync 触发移到事务**提交之后**（如拆分「事务方法 commitCore() + 事务外 triggerSync()」，或用 `TransactionSynchronization.afterCommit`）。

### P1（中高）generate 非事务：全量模式先删后生成，中途失败丢草稿

**现象**

`generate(resume=false)` 先 `draftMapper.delete(全部)`，再进入 `create/enrich` 循环逐条调用 LLM 并 `insert`。方法**未加 `@Transactional`**。

**影响**：若循环中途某次 LLM 调用抛错（超时/限流），旧草稿已被删除、仅部分新草稿入库，**用户之前的草稿与人工修改全部丢失**且无法回滚。

**建议**

- 至少对 `generate` 加 `@Transactional`（LLM 在事务内的长耗时同 P1-2，需权衡；可改为「先生成到内存/临时再整体替换」）。
- 或：全量模式也保留旧草稿，生成成功后再替换；失败则旧草稿不动。
- 单页失败建议**隔离**（标记该页 error，继续其余页），与「断点续跑」配合更稳。

## 次要发现

### P2（中）N+1 查询

- `pageJobs`：对每条 job `kbSpaceMapper.selectById(spaceId)`；
- `listTemplates` → `toTemplateVo`：每个模板 `selectById(spaceId)`。

**建议**：先收集 `spaceId` 去重批量查，组 `Map<Long,KbSpace>` 复用。

### P2（中）commit 内重复查询

`commit()` 调 `lint(jobId)`（内部 `selectList(drafts)`），随后又 `selectList(drafts)` 一次。可将 drafts 读一次后传入复用，减少一次全表扫描。

### P2（低中）enrich 静默降级为 create

`genEnrichDraft` 找不到已有页时降级为 create 落在 `articles/`，仅靠 `action` 翻成 `create` 体现，VO 无显式「降级」标记，前端/用户不易察觉。建议在草稿上加一个 `downgraded` 标志或 lint 提示一条 WARN。

### P3（低）可维护性

- **魔法字符串**：`status`(`created/planned/reviewing/committed`)、`action`(`create/enrich`)、`approval`(`draft/approved/rejected`) 散落多处，建议抽 enum / 常量，避免拼写漂移（已出现 `generating` 在 SQL 注释但代码未用）。
- `SimpleDateFormat` 多次 `new`（局部安全但可抽 `DateTimeFormatter` 常量）。
- `candidateSlugs` 与 `candidateBareSlugs` 职责相近，可注释区分（一个供 Planner 召回带 title，一个供互链校验）。
- 前端 `generateDrafts` 同步请求 `timeoutMs: 300_000`，大批次 UX 风险；与「无 SSE 进度」属已知简化，建议产品上限制单批页数（后端已有 `max-pages-per-batch`，前端可提示）。

### P3（低）并发：草稿更新无乐观锁

`updateDraft` 直接覆盖，无 `baselineHash` 乐观锁（对比 T14 wiki 编辑有乐观锁）。多人同时审同一批次时可能互相覆盖。当前为单 editor 批次工作流，风险低，可后续补。

## 安全评估

- ✅ **路径穿越**：`normalizeUnder`（raw）与 `resolveWikiRelFile`/`resolveWikiBase`（wiki）均 `normalize()` 后校验 `startsWith(root)`，并拒绝 `..`/绝对路径/盘符。
- ✅ **ACL**：读路径 viewer、写路径 editor，`accessibleSpaceIds()` 收敛分页/模板列表。
- ✅ **raw 只读**：无任何写 raw 路径的操作。
- ✅ **红线**：无 plan→`generate` 抛错；lint ERROR / 无 approved / 存在 draft 均阻塞 commit；edges 仅一端为本批次新页才追加。
- 🟡 `readSnippet` 对 raw 文本按 `raw-snippet-chars` 截断，`maxTreeNodes` 控制树规模，已防大文件/大目录 OOM。

## 建议落地顺序

1. **P1-1 / P1-2**：调整 commit 内 DB/文件顺序，Sync 移出事务，治理文件追加做幂等 —— 上线前必做。
2. **P1-3**：generate 全量模式改为「成功后替换」或加事务 + 单页失败隔离。
3. **P2-1 / P2-2**：消除 N+1 与重复查询。
4. **P3**：状态/动作枚举化、enrich 降级显式化、并发乐观锁（可排期二期）。

## 备注

- 本报告为静态审查；编译已通过（`mvn compile` ✅、`vue-tsc -b` ✅）。
- 未覆盖运行时联调与 LLM 真实输出质量评估。
- 关联：API [`docs/api/KNOWLEDGE_API.md`](../api/KNOWLEDGE_API.md) §9；产品方案 [[Ingest工作台产品方案]]；任务 `moli-knowledge/TASKS.md` T15。
