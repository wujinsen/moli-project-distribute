# 知识库 · 内容管道运维 · 前端对接说明（meiling-ui）

> **读者**：meiling-ui 前端（知识库管理员 / 平台 admin 相关页面）。  
> **产品 PRD**：[knowledge-ops-prd.md](../product/knowledge-ops-prd.md)  
> **技术规划**：[kb-ops-roadmap.md](../design/kb-ops-roadmap.md)  
> **HTTP 契约**：[KNOWLEDGE_API.md](KNOWLEDGE_API.md) §4（体检/Sync）、§3.5（LLM）  
> **工作台总览**：[knowledge-workbench-frontend.md](knowledge-workbench-frontend.md)  
> **meiling-ui 落地审计（权威）**：[`meiling-ui/docs/api/knowledge-ops-frontend.md`](../../meiling-ui/docs/api/knowledge-ops-frontend.md) §0 · 与 [`moli-knowledge/TASKS.md`](../../moli-knowledge/TASKS.md) 同步至 **2026-07-13**

---

## 0. 前端落地摘要（2026-07-13）

| 模块 | 状态 | 代码落点（meiling-ui） |
|------|------|------------------------|
| O1–O4 Sync | ✅ | `KbSyncOpsPanel.vue` |
| O5–O8 体检工单 | ✅ | `KbLintIssuesPanel.vue` · `kbLint.ts` |
| O9 Scan 状态条 | ✅ | `KbLintScanStatusBar.vue` |
| T16f Wiki 治理 | ✅ | `KnowledgeWikiGovernView.vue` |
| T19d 平台 LLM | ✅ | `system/kb-llm/index.vue` |
| T20f Ingest 三 Tab | ✅ | `KnowledgeIngestWorkbenchView.vue` |
| KBOPS-9 Dashboard | ✅ | `KnowledgeOpsDashboardView.vue` · `getKbOpsDashboardApi` |

**点验（2026-07-13）**：meiling-ui `npm run kb:prd` **17/17**（含 REG-llm-off merge 探针）。

**剩余（非前端）**：生产开 `kb.sync.schedule-enabled` / `kb.lint.schedule-enabled`、告警 webhook；生产环境注入真实 `KB_LLM_CONFIG_SECRET`。

---

## 1. 开发优先级（给前端排期）

| 优先级 | 模块 | 路由 | 文档 | 后端 | 前端 |
|--------|------|------|------|------|------|
| **P0** | 健康体检 · Sync 增强 | `knowledge/lint/index` | **本文 §3** | ✅ KBOPS-1/2 + O1 + **O9** | ✅ **O1–O4** · **O9** |
| **P1** | **体检工单增强** | `knowledge/lint/index` | **本文 §3.7** | ✅ KBOPS-8/10 | ✅ **O5–O8**（**KBOPS-8f**） |
| **P0** | Wiki 治理全链路 | `knowledge/wiki-govern/index` | [wiki-govern-frontend.md](wiki-govern-frontend.md) | ✅ | ✅ **T16f** |
| **P1** | 平台 LLM 设置 | `system/kb-llm` | [kb-llm-platform-frontend.md](kb-llm-platform-frontend.md) | ✅ T19 | ✅ **T19d** |
| **P1** | Ingest 三 Tab | `knowledge/ingest/index` | [kb-import-entry-frontend.md](kb-import-entry-frontend.md) | ✅ T20a/b/e | ✅ **T20f** |
| **P2** | 运维 Dashboard | `knowledge/ops/dashboard` | **本文 §8** | ✅ KBOPS-9 | ✅ 单请求 + legacy 降级 |

**建议迭代顺序（已完成）**：O1–O4 + O9 → O5–O8 → T16f → T19d → T20f → Dashboard。E2E：`meiling-ui` 仓库 `npm run kb:e2e` + `kb:e2e:extended`。

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
├─ KbLintScanStatusBar（新增） # §3.5 · O9
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

### 3.5 Scan 状态条（O9 · 只读）

> **后端** ✅ `GET /kb/lint/scan/status` · **前端** ✅ `KbLintScanStatusBar.vue`

| ID | 功能 | API | UI |
|----|------|-----|-----|
| **O9** | 定时 scan 状态 | `GET /kb/lint/scan/status?spaceId=` | 信息条：`scheduleEnabled` 徽章 + `lastScanTime` + 可选 `openIssueCount` |

**交互**：

- `scheduleEnabled=true` → 绿色「定时 scan 已开启」+ Tooltip 展示 `scheduleCron`
- `scheduleEnabled=false` → 灰色「定时 scan 未开启（运维 yml）」
- `lastScanTime` 为空 → 「尚未 scan 落库」
- **不提供** Web 开关改 yml；手动 scan 仍用 `POST /kb/lint/scan`
- 选空间 / 手动 scan 成功后刷新 status

```typescript
export type LintScanStatusVo = {
  spaceId?: string
  spaceCode?: string
  scheduleEnabled: boolean
  scheduleCron?: string
  lastScanTime?: string
  openIssueCount?: number
}

export const getLintScanStatus = (spaceId: string) =>
  request.get<LintScanStatusVo>(`${KB}/lint/scan/status`, { params: { spaceId } })
```

### 3.6 权限与错误

| 场景 | 处理 |
|------|------|
| 无 `kb:sync:trigger` | 隐藏触发按钮或 Tooltip「需要 Sync 权限」 |
| KBOPS-2 并发锁 | HTTP 200 + 业务码「同步进行中」→ 禁用按钮 + 轮询 status |
| 脚本失败 KBOPS-1 | 展示 `message` / `stdoutTail`（若有）；勿覆盖为 success |

### 3.7 验收 O1–O4 / O9

- [x] 选空间后加载 status + 最近 10 条 log  
- [x] trigger 成功 → status 刷新、log 新增 success 行  
- [x] trigger 失败（运维配合制造）→ fail 行可见、Toast、「仅显示失败」筛选  
- [x] running 时不能重复 trigger  
- [x] **O9**：展示定时 scan 开关状态 + 最近 scan 时间（只读）

**P0-O4 环境点验**：✅ 2026-07-13 `kb:prd` **P0-O4** 通过（`enterprise-kb` fail 行）。造败步骤见 [`kb-sync-failure-runbook.md` §9](../../ops/kb-sync-failure-runbook.md#9-p0-o4-点验故意制造失败仅显示失败筛选)。

---

## 3.7 P1 · 体检工单增强（O5–O8 · KBOPS-8f） ✅

> **后端** ✅ · **前端** ✅ `KbLintIssuesPanel.vue` + `src/api/knowledge/kbLint.ts`

| ID | 功能 | API | UI |
|----|------|-----|-----|
| **O5** | 类型筛选 | `GET /kb/lint/issues?issueType=` | 下拉；数据源 `GET /kb/lint/issue-types` |
| **O6** | 指派 | `PUT /kb/lint/issue/{id}?assigneeId=` | 行内选择处理人 +「指派给我」 |
| **O7** | 批量状态 | `PUT /kb/lint/issues/batch` | 多选 → 已忽略/已修复（`clearAssignee` 清指派） |
| **O8** | 分页 | `GET /kb/lint/issues?pageNum&pageSize` | `AppPagination`；旧版全量数组仍兼容客户端 slice |

**TypeScript 建议**：

```typescript
export type KbLintIssue = {
  id: string
  spaceId: string
  documentId?: string
  issueType: string
  detail?: string
  status: 0 | 1 | 2
  assigneeId?: string
  priority?: 0 | 1 | 2
  scanTime?: string
}

export type LintIssueTypeVo = {
  code?: string
  label: string
  lintPyKind?: string
  webOnly?: boolean
  lintPyOnly?: boolean
}
```

**分工提示（KBOPS-10）**：列表页角标说明「DB 快照体检」；Sync 前引导用户去 **Wiki 治理** 跑 `lint-space`（文件真值）。

### 3.7.1 DoD（KBOPS-8f · meiling-ui） ✅

- [x] 扫描落库后工单列表支持 `issueType` / `assigneeId` / `status` 筛选  
- [x] `GET /kb/lint/issue-types` 驱动类型下拉  
- [x] 行内改 `assigneeId`（O6）  
- [x] 多选批量改 status / 指派（O7 · `PUT /kb/lint/issues/batch`）  
- [x] 服务端分页或全量兼容 slice（O8）  
- [x] 页头说明：DB 快照 ≠ 磁盘 lint.py；修 wiki 走治理页  

---

## 4. P0 · Wiki 治理（T16f / KBOPS-6） ✅

**完整规格**见下表（meiling-ui `KnowledgeWikiGovernView.vue` 已联调）：

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

## 5. P1 · 平台 LLM 设置（T19d / KBOPS-7） ✅

→ 全文 **[kb-llm-platform-frontend.md](kb-llm-platform-frontend.md)**

| 项 | 值 |
|----|-----|
| 路由 | `system/kb-llm` |
| 权限 | `kb:platform:llm` |
| API | `GET/PUT/POST test` → `/kb/platform/llm-config` |

治理页 AI 修复依赖 LLM 可用；保存后 `GET /kb/wiki-moli/govern/options` 的 `llmAvailable` 应变 true。

---

## 6. P1 · Ingest 三 Tab（T20f） ✅

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

## 8. P2 · 运维 Dashboard（KBOPS-9） ✅ 前后端

**路由**：`knowledge/ops/dashboard` · perm `kb:ops:dashboard` · `KnowledgeOpsDashboardView.vue`

| 区块 | 数据源（2026-07-13 · meiling-ui 已接线） |
|------|------------------------------------------|
| D1 Sync 趋势 | `GET /kb/ops/dashboard` → `syncTrend`（降级 sync/logs 聚合） |
| D2 待处理工单 | `lintSummary.openByType` |
| D3 LLM 可用 | `dashboard.llm`（含 AI-8 `cacheHitRate` / `estimatedCostUsd` / `costTrend`） |
| D4 断链 Top | `lintSummary.topBrokenLinks` |
| D5 检索质量 | `retrievalQuality`（AI-3）· 明细 `GET /kb/ops/eval-trend` · `GET /kb/ops/eval-runs` |

**后端** `GET /kb/ops/dashboard` ✅（**`trendDays`**，默认 7）。  
**AI-3 只读扩展**（权限同 `kb:ops:dashboard`）：

| 路径 | 说明 |
|------|------|
| `GET /kb/ops/eval-trend?strategy=&days=14` | 按日 hit@3/MRR（当日最后一次 run） |
| `GET /kb/ops/eval-runs?strategy=&limit=20` | run 明细含 `report_path` / `gate_pass` / `by_difficulty_json` |

Dashboard 新增字段 `retrievalQuality.strategies[]`：`strategy` · `hit3` · `mrr` · `baselineHit3` · `deltaHit3` · `gatePass`。

**AI-8 LLM 缓存/成本（additive · `dashboard.llm`）**：

| 字段 | 说明 |
|------|------|
| `cacheHitRate` | 窗口内成功调用中 `cache_hit=1` 占比 |
| `estimatedCostUsd` | 窗口内 `estimated_cost_usd` 求和 |
| `failoverCount` | 窗口内 `failover=1` 次数 |
| `estimatedCostSavedUsd` | 缓存命中按 token 粗算的节省成本 |
| `estimatedTokensSaved` | 缓存命中 prompt+completion tokens 粗算合计 |
| `costTrend[]` | 按日：`date` · `estimatedCostUsd` · `cacheHits` · `calls` |

前置 DDL：[`docs/sql/35_kb_llm_call_log_ai8.sql`](../sql/35_kb_llm_call_log_ai8.sql)（在 `18_kb_llm_call_log.sql` 之后）。
**前端** ✅ `getKbOpsDashboardApi`（commit `a7b6fa9`）；缺 `kb_llm_call_log` 时 legacy 降级。  
**详稿**：[p3-optional-backend-handoff.md](p3-optional-backend-handoff.md) §3 · meiling-ui [knowledge-ops-frontend.md](../../meiling-ui/docs/api/knowledge-ops-frontend.md) §8。

---

## 9. 配置项（联调须知）

| 配置 | 默认 | 前端影响 |
|------|------|----------|
| `kb.ingest.commit-auto-sync` | `true` | publish 后可能已 Sync，O1 仍要展示最后批次 |
| `kb.sync.schedule-enabled` | `false` | 定时 Sync 默认关 |
| `kb.lint.schedule-enabled` | `false` | 定时 DB scan 落库默认关；**Web 只读展示**（O9），改 yml/Nacos |
| `kb.sync.space-code` | 单空间 | KBOPS-4 后可能变多空间列表 |
| `kb.llm.call-log-enabled` | `true` | 写 `kb_llm_call_log`；缺表时 dashboard 500 → 前端降级；DDL：`docs/sql/18_kb_llm_call_log.sql` + AI-8 [`35_kb_llm_call_log_ai8.sql`](../sql/35_kb_llm_call_log_ai8.sql) |
| `kb.llm.cache.enabled` | `false` | 语义缓存（Redis）；`true` 时同问二次命中；见 [`kb-llm-platform-settings.md`](../design/kb-llm-platform-settings.md) §12 |
| `kb.llm.router.enabled` | `false` | 多 provider failover；见同上 §12 |

详表见 PRD §6、运维 `wiki同步指南`。

---

## 10. 验收总表（运维前端）

| ID | 模块 | 项 | 优先级 | 状态 |
|----|------|-----|--------|------|
| O1 | Sync | 状态卡片 | P0 | ✅ |
| O2 | Sync | 触发按钮 + 锁 | P0 | ✅ |
| O3 | Sync | 日志列表 | P0 | ✅ |
| O4 | Sync | 失败展示 | P0 | ✅ |
| O9 | 体检 Scan | 定时状态 + 最近 scan 时间（只读） | P0 | ✅ |
| O5 | 体检工单 | 类型筛选 | P1 | ✅ |
| O6 | 体检工单 | 行内指派 | P1 | ✅ |
| O7 | 体检工单 | 批量改状态/指派 | P1 | ✅ |
| O8 | 体检工单 | 分页 | P1 | ✅ |
| W1–W8 | 治理 | 见 wiki-govern §13 | P0 | ✅ |
| T19d | LLM | 见 kb-llm-platform | P1 | ✅ |
| T20f | Ingest | 见 kb-import-entry §10 | P1 | ✅ |
| D1–D4 | Dashboard | §8 四区块 | P2 | ✅ |

---

## 11. 代码落点（meiling-ui 建议）

| 路径 | 职责 |
|------|------|
| `src/api/knowledge/kbSync.ts` | §3.4 Sync API |
| `src/types/knowledge/kbSync.ts` | §3.3 类型 |
| `src/components/knowledge/KbSyncOpsPanel.vue` | §3.2 可复用 Sync 区 |
| `src/components/knowledge/KbLintIssuesPanel.vue` | §3.7 工单表（O5–O8） |
| `src/api/knowledge/kbLint.ts` | issues / batch / assign / **scan/status** |
| `src/views/knowledge/KnowledgeLintView.vue` | 嵌入 KbSyncOpsPanel + KbLintIssuesPanel + O9 |
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
| 2026-07-13 | §8 Dashboard 前后端 ✅；§9 增 `kb_llm_call_log` DDL 说明；P3 对齐 `frontend-gaps` |
| 2026-07-12 | §0 前端落地摘要；§1/§3.7/§8/§10 标 ✅；O7 统一 `PUT /kb/lint/issues/batch`；与 TASKS.md / meiling-ui §0 对齐 |
| 2026-07-12 | §3.5 增 O9 · `GET /kb/lint/scan/status`（定时 scan 只读展示） |
| 2026-07-09 | 初稿：O1–O4 Sync UI、排期、Dashboard 规划、与 T16f/T19d/T20f 交叉引用 |
| 2026-06-28 | 治理细节见 wiki-govern-frontend.md |
