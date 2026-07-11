# 知识库 · 内容管道运维 · 前端对接说明（meiling-ui）

> **读者**：meiling-ui 前端（知识库管理员 / 平台 admin 相关页面）。  
> **产品 PRD**：[knowledge-ops-prd.md](../product/knowledge-ops-prd.md)  
> **技术规划**：[kb-ops-roadmap.md](../design/kb-ops-roadmap.md)  
> **HTTP 契约**：[KNOWLEDGE_API.md](KNOWLEDGE_API.md) §4（体检/Sync）、§3.5（LLM）  
> **工作台总览**：[knowledge-workbench-frontend.md](knowledge-workbench-frontend.md)

---

## 1. 开发优先级（给前端排期）

| 优先级 | 模块 | 路由 | 文档 | 后端 | 前端 |
|--------|------|------|------|------|------|
| **P0** | 健康体检 · Sync 增强 | `knowledge/lint/index` | **本文 §3** | ✅ KBOPS-1/2 + O1 字段 | 🔵 **O1–O4** |
| **P0** | Wiki 治理全链路 | `knowledge/wiki-govern/index` | [wiki-govern-frontend.md](wiki-govern-frontend.md) | ✅ | 🔵 **W2/W4/W5/W7** |
| **P1** | 平台 LLM 设置 | `system/kb-llm` | [kb-llm-platform-frontend.md](kb-llm-platform-frontend.md) | ✅ T19 | 🔵 **T19d** |
| **P1** | Ingest 三 Tab | `knowledge/ingest/index` | [kb-import-entry-frontend.md](kb-import-entry-frontend.md) | ✅ T20a/b/e | 🔵 **T20f** |
| **P2** | 运维 Dashboard | `knowledge/ops/dashboard` | **本文 §8** | ✅ KBOPS-9 | 📋 规划 |

**建议迭代顺序**：**O1–O4（Sync 可见）→ W2/W4/W5/W7（治理闭环）→ T19d → T20f Tab1/3 → Dashboard**

**网关前缀**：`{VITE_API_BASE_URL}/KnowledgeServer`

---

## 2. 页面分工（勿混淆）

| 页面 | 数据源 | 用户动作 |
|------|--------|----------|
| **Wiki 治理** | 磁盘 `POST /kb/wiki-moli/lint-space` | 修 **文件**（script/AI/auto） |
| **健康体检** | MySQL `GET /kb/lint` | 看 **DB 快照**；Scan 写工单 |
| **Sync** | `POST /kb/sync/trigger` | wiki → DB；日志 `GET /kb/sync/logs` |

```text
治理修文件 → (可选 syncAfter) → Sync → 健康体检 Scan → 处理 kb_lint_issue
```

Ingest `commit/publish` 默认 **auto-sync**（`kb.ingest.commit-auto-sync=true`）；失败时用户来 **健康体检** 看 Sync 区（O1–O4）。

---

## 3. P0 · 健康体检页 Sync 增强（O1–O4）

> **背景**：KBOPS-1/2/O1 后端 ✅（2026-07-11）。`SyncStatusVo` 含 `running`/`lastStatus`/`lastMessage`；前端 O1–O4 可对接。

### 3.1 建议布局（在现有 lint 页扩展）

```text
KnowledgeLintView.vue
├─ KbSpaceSelector
├─ LintSummaryPanel          # 现有：GET /kb/lint
├─ LintIssueTable            # 现有：GET /kb/lint/issues
├─ KbSyncOpsPanel  （新增）   # §3.2
└─ ScanActions               # 现有：POST /kb/lint/scan
```

### 3.2 KbSyncOpsPanel 行为

| ID | 功能 | API | UI |
|----|------|-----|-----|
| **O1** | 当前 Sync 状态 | `GET /kb/sync/status?spaceId=` | 展示 `running` / `lastBatchNo` / `lastStatus` / `lastMessage` |
| **O2** | 触发 Sync | `POST /kb/sync/trigger?spaceId=&async=true` | 按钮；提交后轮询 O1 `running`；running 时 disabled |
| **O3** | 最近日志 | `GET /kb/sync/logs?spaceId=&pageSize=10` | 表格：batchNo、status、createTime、message 摘要 |
| **O4** | 失败态 | 同上 | `status=fail` 行 **danger** 色 + 展开 message；Toast「Sync 失败，请查看日志」 |

**三空间快捷（可选）**：平台 admin 展示 `enterprise-kb` / `moli-ops-manual` / `jp-fe-ap-exam` Tab 或下拉，避免只 sync 默认空间。

### 3.3 TypeScript（建议 `src/types/knowledge/kbSync.ts`）

```typescript
export type KbSyncStatusVo = {
  running: boolean
  spaceId?: string
  spaceCode?: string
  lastBatchNo?: string
  lastStatus?: 'success' | 'fail' | 'running'
  lastMessage?: string
  lastFinishTime?: string
  failCount?: number
  successCount?: number
}

export type KbSyncLogVo = {
  id: string
  batchNo: string
  spaceId: string
  sourcePath?: string
  action?: string
  status: 'success' | 'fail'
  message?: string
  createTime: string
}

export type SyncTriggerVo = {
  batchNo?: string
  status?: string
  message?: string
  stdoutTail?: string
  nextSteps?: KbWorkflowHintVo[]
}
```

### 3.4 API 模块（建议 `src/api/knowledge/kbSync.ts`）

```typescript
import request from '@/utils/request'

const KB = '/KnowledgeServer/kb'

export const getSyncStatus = (spaceId: string) =>
  request.get<KbSyncStatusVo>(`${KB}/sync/status`, { params: { spaceId } })

export const triggerSync = (spaceId: string) =>
  request.post<SyncTriggerVo>(`${KB}/sync/trigger`, null, { params: { spaceId } })

export const listSyncLogs = (params: { spaceId: string; pageNum?: number; pageSize?: number }) =>
  request.get<{ rows: KbSyncLogVo[]; total: number }>(`${KB}/sync/logs`, { params })
```

### 3.5 权限与错误

| 场景 | 处理 |
|------|------|
| 无 `kb:sync:trigger` | 隐藏触发按钮或 Tooltip「需要 Sync 权限」 |
| KBOPS-2 并发锁 | HTTP 200 + 业务码「同步进行中」→ 禁用按钮 + 轮询 status |
| 脚本失败 KBOPS-1 | 展示 `message` / `stdoutTail`（若有）；勿覆盖为 success |

### 3.6 验收 O1–O4

- [ ] 选空间后加载 status + 最近 10 条 log  
- [ ] trigger 成功 → status 刷新、log 新增 success 行  
- [ ] trigger 失败（运维配合制造）→ fail 行可见、Toast  
- [ ] running 时不能重复 trigger  

---

## 4. P0 · Wiki 治理（T16f / KBOPS-6）

**完整规格不在此重复**，请直接实现：

| 文档 | 内容 |
|------|------|
| [wiki-govern-frontend.md](wiki-govern-frontend.md) | W1–W8、状态机、六个 govern API |
| [knowledge-workbench-frontend.md §10.2](knowledge-workbench-frontend.md) | 与现 MVP（仅 Lint+AI）差距 |

**运维闭环必做（W2/W4/W5/W7）**：

| 按钮 | API | 说明 |
|------|-----|------|
| 脚本修复 | `POST /kb/wiki-moli/govern/script-fix` | `missing_dates` / `slug_mismatch` / `missing_source` |
| 一键修复 | `POST /kb/wiki-moli/govern/auto-fix` | `relintAfter` + 可选 **`syncAfter`** |
| 合并提示 | `POST /kb/wiki-moli/govern/merge-hint` | `dup_slug` 复制 Cursor 指令 |
| 修复后 Sync | `syncAfter: true` 或跳转本页 **O2** | 与 §3 联动 |

---

## 5. P1 · 平台 LLM 设置（T19d / KBOPS-7）

→ 全文 **[kb-llm-platform-frontend.md](kb-llm-platform-frontend.md)**

| 项 | 值 |
|----|-----|
| 路由 | `system/kb-llm` |
| 权限 | `kb:platform:llm` |
| API | `GET/PUT/POST test` → `/kb/platform/llm-config` |

治理页 AI 修复依赖 LLM 可用；保存后 `GET /kb/wiki-moli/govern/options` 的 `llmAvailable` 应变 true。

---

## 6. P1 · Ingest 三 Tab（T20f）

→ 全文 **[kb-import-entry-frontend.md](kb-import-entry-frontend.md)**

| Tab | API |
|-----|-----|
| Tab1 投喂 Raw | `POST /kb/ingest/raw-upload` |
| Tab2 选源入库 | 现有 Ingest |
| Tab3 成品导入 | `POST /kb/wiki/page/import` |

commit/publish 响应 **`nextSteps`** → 渲染 [KbWorkflowNextSteps](knowledge-workbench-frontend.md#32-nextsteps入库--sync-后-cta)（`wiki_govern_lint` / `kb_health_scan`）。

---

## 7. 共享组件建议

| 组件 | 用途 | 复用页 |
|------|------|--------|
| `KbWorkflowNextSteps.vue` | 入库/Sync 后 CTA 按钮组 | Ingest、Sync trigger 响应 |
| `KbSyncOpsPanel.vue` | status + trigger + logs | 健康体检、治理页底部（W7） |
| `KbSpaceSelector` | 空间选择 | 全部 KB 页 |

---

## 8. P2 · 运维 Dashboard（规划 · KBOPS-9）

**路由建议**：`knowledge/ops/dashboard` · perm `kb:ops:dashboard`（SQL 待补）

| 区块 | 数据源 | 说明 |
|------|--------|------|
| Sync 趋势 | `GET /kb/sync/logs` 聚合 | 近 7 日 success/fail 计数 |
| 待处理工单 | `GET /kb/lint/issues?resolved=0` | 按 space / issueType |
| LLM 可用 | `GET /kb/ask/llm-config` | available 灯 |
| （可选）断链 Top N | lint issues `broken_link` | P2 |

后端 Dashboard 专用 API **尚未实现**；P2 前可用现有 logs/issues 接口前端聚合。

---

## 9. 配置项（联调须知）

| 配置 | 默认 | 前端影响 |
|------|------|----------|
| `kb.ingest.commit-auto-sync` | `true` | publish 后可能已 Sync，O1 仍要展示最后批次 |
| `kb.sync.schedule-enabled` | `false` | 定时 Sync 默认关 |
| `kb.sync.space-code` | 单空间 | KBOPS-4 后可能变多空间 |

详表见 PRD §6、运维 `wiki同步指南`。

---

## 10. 验收总表（运维前端）

| ID | 模块 | 项 | 优先级 |
|----|------|-----|--------|
| O1 | Sync | 状态卡片 | P0 |
| O2 | Sync | 触发按钮 + 锁 | P0 |
| O3 | Sync | 日志列表 | P0 |
| O4 | Sync | 失败展示 | P0 |
| W1–W8 | 治理 | 见 wiki-govern §13 | P0 |
| T19d | LLM | 见 kb-llm-platform | P1 |
| T20f | Ingest | 见 kb-import-entry §10 | P1 |
| D1–D4 | Dashboard | §8 四区块 | P2 |

---

## 11. 代码落点（meiling-ui 建议）

| 路径 | 职责 |
|------|------|
| `src/api/knowledge/kbSync.ts` | §3.4 Sync API |
| `src/types/knowledge/kbSync.ts` | §3.3 类型 |
| `src/components/knowledge/KbSyncOpsPanel.vue` | §3.2 可复用 Sync 区 |
| `src/views/knowledge/lint/index.vue` | 嵌入 KbSyncOpsPanel |
| `src/views/knowledge/wiki-govern/` | 见 wiki-govern §14 |
| `src/views/system/kb-llm/index.vue` | T19d |

菜单 SQL 已有：910 治理 · 904 体检 · 12 LLM（`docs/sql/12_kb_platform_llm_menu.sql`）。

---

## 12. 联调环境

1. 启动 gateway + `moli-knowledge-server`  
2. 部署机存在 `kb/tools/sync_to_db.py`、`lint.py`  
3. 测试空间：`900000000000000001` enterprise-kb · `900000000000000003` moli-ops-manual · `900000000000000002` jp-fe-ap-exam  
4. LLM：先完成 T19d 或 yml 配 `kb.llm`  

---

## 13. 相关文件

| 路径 | 说明 |
|------|------|
| `KbSyncController.java` | `/kb/sync/*` |
| `KbInsightController.java` | `/kb/lint*` |
| `KbSyncServiceImpl.java` | trigger + 日志 |
| `kb/tools/sync_to_db.py` | Sync 脚本（KBOPS-1） |
| `docs/test/knowledge-wiki-lint-space.md` | 治理 API 测试 |

---

## 14. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-09 | 初稿：O1–O4 Sync UI、排期、Dashboard 规划、与 T16f/T19d/T20f 交叉引用 |
| 2026-06-28 | 治理细节见 wiki-govern-frontend.md |
