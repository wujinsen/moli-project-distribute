# P3 可选增强 · 前端开工手册（meiling-ui）

> **更新**：2026-07-13  
> **读者**：**meiling-ui 前端**（主）、user-center `:8888`、knowledge-server `:8090`  
> **优先级**：**P3 可选** — 不挡运营/KB 主流程；**现网客户端兜底可继续用**  
> **前端仓库**：[`meiling-ui/docs/api/p3-optional-backend-handoff.md`](../../meiling-ui/docs/api/p3-optional-backend-handoff.md)

| 任务 ID | 服务 | 前端落点 | 后端 API | 前端动作 |
|---------|------|----------|----------|----------|
| **DC-4** | `8888` | `TaskHistoryView.vue` | ✅ **已交付** | ✅ **已接线** — `listTaskGroupsApi` + 分组视图 |
| **KB-LINT-1/2** | `8090` | `kbLint.ts` · `KbLintIssuesPanel.vue` | ✅ **已交付** | ✅ **已收紧** — 信任服务端分页 |
| **KBOPS-2** | `8090` | `KnowledgeOpsDashboardView.vue` | ✅ **已交付**（KBOPS-9） | ✅ **已接线** — `getKbOpsDashboardApi` + legacy 降级 |

**结论**：P3 三项 **前后端均已交付**（2026-07-13）。KB 点验 **`npm run kb:prd` 17/17**。

---

## 0. 给前端一句话（可复制 · 2026-07-13 已完工）

```
【meiling-ui · P3 · 2026-07-13 ✅ 三项已接线】

① DC-4：TaskHistoryView 平铺/按项目分组 · listTaskGroupsApi
② KBOPS-2：KnowledgeOpsDashboardView · getKbOpsDashboardApi（失败降级 3 请求）
③ KB-LINT：kbLint.ts 服务端分页信任

KB 点验：npm run kb:prd 17/17（含 REG-llm-off merge 探针）

详稿：meiling-ui/docs/api/p3-optional-backend-handoff.md §6
```

---

## 0.2 后端版本与联调前置（2026-07-13）

| 服务 | 端口 | 关键 commit | 重启后验证 |
|------|------|-------------|------------|
| user-center | `8888` | `755abd43`（DC-4 `GET /operation/task/groups`） | `GET /operation/task/groups?pageSize=1` → 200 |
| knowledge-server | `8090` | `38570430`（`KbRepoPathUtil` 统一 kb 路径） | Sync / Lint 脚本可解析；`kb:prd` 已通过 |

```powershell
# monorepo 根目录
mvn -pl moli-user-center/moli-user-center-server,moli-knowledge/moli-knowledge-server -am install -DskipTests -q
# 重启本地 8888 / 8090 后再让前端联调
```

**KB 点验**：meiling-ui `npm run kb:prd` → **16/17**（2026-07-13）；仅 `REG-llm-off` 跳过。

---

## 0.1 开工提示词（贴进 meiling-ui 对话）

```
请读 monorepo docs/api/p3-optional-backend-handoff.md，按 §4 排期实现 P3 可选：

1. DC-4：operation.ts 增加 listTaskGroupsApi；TaskHistoryView 增加「按项目分组」视图
   - 分组头用 taskCount / runningCount / failedCount / latestCreateTime
   - projectId=null 显示「未关联项目」
   - 组内 tasks 不足时用 GET /operation/task/list?projectId= 补全
2. KBOPS-2：KnowledgeOpsDashboardView 改调 GET /kb/ops/dashboard
3. KB-LINT：确认 Lint 工单请求带 pageNum/pageSize（无新 API）

类型见 operation-frontend.md §11.2.1；字段级见 operation-deploy-api.md
```

---

## 1. DC-4 · 任务历史按项目聚合（user-center `:8888`）

### 1.1 背景

- 部署中心已 **项目优先**（DC-2）；工具栏「任务历史」带 `?projectId=` 跳转 `TaskHistoryView`。
- 现网 `GET /operation/task/list` 为**扁平分页**，多机批量后同一项目下多台服务器任务分散，运维需肉眼归并。
- 设计意图：[deploy-center-project-first.md](../design/deploy-center-project-first.md) §5「任务历史按 `projectId` 聚合视图」。

### 1.2 前端目标（可开工）

| 能力 | 说明 |
|------|------|
| 视图切换 | 平铺列表（现网） / **按项目分组** |
| 分组头 | 项目名、任务数、进行中/失败计数、最近时间 |
| 组内 | 展开显示该机群下的任务行（服务器、类型、状态、日志入口） |
| 无 `projectId` | 归入「未关联项目」组（`health_probe`、全站探活等） |

### 1.3 推荐接口（二选一）

#### 方案 A（推荐）· 分组列表

```
GET /operation/task/groups
```

**Query**（与现 `task/list` 对齐，并加分页语义）：

| 参数 | 类型 | 说明 |
|------|------|------|
| `pageNum` | int | 分页：**项目组**页码，默认 1 |
| `pageSize` | int | 每组列表页大小，默认 10 |
| `tasksPerGroup` | int | 可选；每组内嵌任务条数上限，默认 20；超出可 `GET /operation/task/list?projectId=` |
| `taskType` | string | 可选；`deploy` / `upload` / `command` / `health_probe` / `deploy_batch` |
| `projectId` | long | 可选；筛单一项目（部署中心跳入场景） |
| `serverId` | long | 可选 |
| `status` | string | 可选；`pending` / `running` / `success` / `failed` / `cancelled` |

**Response** `MoliResult<PageRes<OperationTaskProjectGroup>>`：

```typescript
type OperationTaskProjectGroup = {
  projectId: number | null      // null → 前端显示「未关联项目」
  projectName: string | null    // 来自 project 表或任务冗余字段
  taskCount: number             // 组内总任务数（可 > tasksPerGroup）
  runningCount: number
  failedCount: number
  successCount: number
  latestCreateTime?: string     // ISO / 与现网 createTime 格式一致
  tasks: OperationTask[]        // 组内任务（按 createTime 降序）
}

type PageRes<T> = {
  total: number                 // 项目组总数
  list: OperationTaskProjectGroup[]
  pageNum?: number
  pageSize?: number
}
```

`OperationTask` 字段与现 `GET /operation/task/list` 行一致：`id` · `taskType` · `serverId` · `projectId` · `serviceKey` · `action` · `targetName` · `status` · `progress` · `message` · `createTime` · `finishTime`。

#### 方案 B · 扩展 flat list

```
GET /operation/task/list?groupBy=project
```

返回 `list` 仍为扁平，额外带 `groups: { projectId, projectName, taskCount, ... }[]` 元数据。  
前端倾向 **方案 A**（组内任务一次带回，减少往返）。

### 1.4 与现网兼容

- **勿 Breaking** `GET /operation/task/list` 扁平语义。
- `deploy_batch` 父任务：建议 `projectId` 有值、`serverId` 可空；子任务若单独落库需可筛入同一 `projectId` 组。
- 权限：`operation:server:list`（与 `GET /operation/task/list` 一致）

**请求示例**：

```http
GET /operation/task/groups?pageNum=1&pageSize=10&tasksPerGroup=20
Authorization: Bearer <token>
```

**响应片段**（`MoliResult<PageRes<OperationTaskProjectGroup>>`）：

```json
{
  "code": 200,
  "data": {
    "total": 3,
    "pageNum": 1,
    "pageSize": 10,
    "list": [
      {
        "projectId": 731708402010423296,
        "projectName": "moli-user-center",
        "taskCount": 12,
        "runningCount": 1,
        "failedCount": 2,
        "successCount": 9,
        "latestCreateTime": "2026-07-13T10:00:00.000+00:00",
        "tasks": [ { "id": 99, "taskType": "deploy", "status": "running", "projectId": 731708402010423296 } ]
      },
      {
        "projectId": null,
        "projectName": null,
        "taskCount": 5,
        "runningCount": 0,
        "failedCount": 0,
        "successCount": 5,
        "latestCreateTime": "2026-07-12T08:00:00.000+00:00",
        "tasks": [ { "id": 1, "taskType": "health_probe", "status": "success", "projectId": null } ]
      }
    ]
  }
}
```

> `runningCount` = `pending` + `running`；`cancelled` 计入 `taskCount` 但不计入 success/failed/running 三计数。

### 1.5 前端接线

| 文件 | 改动 |
|------|------|
| `src/api/operation.ts` | `listTaskGroupsApi`（见下方签名） |
| `src/views/operation/TaskHistoryView.vue` | 平铺/分组切换 · 手风琴 UI · 路由 `?projectId=` 保留 |
| `src/i18n/locales/{zh,en,ja}.ts` | `operation.taskHistory.groupByProject` 等 |

```typescript
export const listTaskGroupsApi = (params?: {
  pageNum?: number
  pageSize?: number
  tasksPerGroup?: number
  taskType?: string
  projectId?: number
  serverId?: number
  status?: string
}) => request<PageRes<OperationTaskProjectGroup>>(`${OP}/task/groups`, { params })
```

### 1.6 验收

手测 + 单测见 [`docs/test/operation-task-groups-acceptance.md`](../test/operation-task-groups-acceptance.md)。

- [ ] 部署中心选项目 → 任务历史带 `projectId` → 分组视图仅 1 组或平铺一致（FE-4）
- [ ] 多项目账号：分组头 `taskCount` 与 `list?projectId=` 总数一致（GR-13）
- [ ] 无 `projectId` 任务进入「未关联项目」组（GR-4）
- [ ] 分页：`total` 为组数，翻页不串组（GR-2 / FE-5）
- [ ] `mvn -Dtest=OperationTaskServiceImplTest,OperationRemoteDeployControllersApiTest test` 通过

---

## 2. KB-LINT-1 / KB-LINT-2 · Lint 工单真分页（knowledge-server `:8090`）

### 2.0 状态（2026-07-13）

| 层 | 状态 |
|----|------|
| **后端** | ✅ `GET /kb/lint/issues` 已支持 MyBatis 分页 + `unassignedOnly` SQL 过滤 |
| **前端** | 🟡 已接 API；`kbLint.ts` 在响应含 **`current` + `size`** 时走服务端分页 |
| **验证** | `npm run kb:prd` → P2-O5 / P2-O5-unassigned / P2-O8 ✅ |

**前端可选收紧**（非阻塞）：健康体检 · 质量 Tab 拉工单时始终带 `pageNum`/`pageSize`；勿依赖裸数组全量 + 客户端 `slice`。

### 2.1 背景

- `GET /kb/lint/issues` 已用于 **健康体检 · 质量 Tab**（O5–O8）与 **运维 Dashboard D2/D4**。
- 前端 `normalizeLintIssuesResponse`（`src/api/knowledge/kbLint.ts`）兼容三种形态：
  1. **全量数组** → 客户端 `slice` 分页（现状，工单量大时慢）
  2. **`{ records, total, current, size }`** → 信任服务端分页
  3. 有 `records` 但无 `current`/`size` → 仍客户端 slice

### 2.2 必须修复（KB-LINT-1）

| 项 | 现网问题 | 期望 |
|----|----------|------|
| **`unassignedOnly=true`** | 可能仍返全量，前端再 filter | **SQL/查询层过滤** `assignee_id IS NULL` |
| **分页** | 返数组或 `total` 不准 | 返标准分页对象，且 `total` = 过滤后总数 |
| **`resolved=0`** | 已有 | 与 `status=0` 语义一致即可 |

**请求示例**：

```http
GET /KnowledgeServer/kb/lint/issues?spaceId=900000000000000001&status=0&unassignedOnly=true&pageNum=1&pageSize=20
```

**响应（必须）**：

```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "records": [ /* KbLintIssue */ ],
    "total": 128,
    "current": 1,
    "size": 20
  }
}
```

> 若沿用 MyBatis-Plus 风格，**同时返回 `current` + `size`**，前端即走服务端分页分支（见 `kbLint.ts` `serverPaginated` 判断）。

**`KbLintIssue` 单条**（与现网对齐，勿删字段）：

`id` · `spaceId` · `documentId` · `issueType` · `detail` · `status`（0/1/2）· `assigneeId` · `scanTime` · `createTime` · `updateTime`

### 2.3 KB-LINT-2 补充

| 项 | 说明 |
|----|------|
| 排序 | 建议默认 `createTime DESC` 或 `scanTime DESC`，全页一致 |
| 筛选组合 | `spaceId` + `status` + `issueType` + `unassignedOnly` + `assigneeId` 任意组合，`total` 均正确 |
| Dashboard | `KnowledgeOpsDashboardView` 现拉 `status=0` **无分页**；工单 >500 时建议后端提供 **聚合接口**（见 §3）或 `pageSize` 上限说明 |

### 2.4 勿 Breaking

- 继续允许短期返 **数组**（老客户端）；但 `current`+`size` 存在时不得再返全量数组。
- `PUT /kb/lint/issue/{id}` · `PUT /kb/lint/issues/batch` 不变。

### 2.5 前端接线（现状 + 可选）

| 文件 | 现状 | 可选收紧 |
|------|------|----------|
| `src/api/knowledge/kbLint.ts` | ✅ `normalizeLintIssuesResponse` 识别 `current`+`size` | 无必改 |
| `KbLintIssuesPanel.vue` | ✅ `AppPagination` 绑定 `total` | 确认请求始终带分页参数 |
| `KnowledgeLintView.vue` | 同上 | 大库场景避免过大 `pageSize` |

### 2.6 验收

- [ ] `unassignedOnly=true` + `pageNum=2`：无 assignee 的工单，且页间不重复
- [ ] `total` 与 DB `COUNT(*)` 一致（同筛选条件）
- [ ] 1 万条工单：单页响应 < 合理体积（仅 20 条 records）

---

## 3. KBOPS-2 · 运维 Dashboard 专用 API（knowledge-server `:8090`）

### 3.0 状态（2026-07-13）

| 层 | 状态 |
|----|------|
| **后端** | ✅ **KBOPS-9** · `GET /kb/ops/dashboard` 已上线（`KbOpsController`） |
| **前端** | ⬜ `KnowledgeOpsDashboardView.vue` 仍 **3 并行请求** + `kbOpsDashboard.ts` 客户端聚合 |
| **权限** | `kb:ops:dashboard`（菜单 SQL：`docs/sql/13_kb_ops_dashboard_menu.sql`） |

**推荐前端改动**：新增 `getKbOpsDashboardApi` → `loadDashboard()` 单请求；保留现有多请求逻辑作降级。

### 3.1 背景（现网 vs 目标）

- 路由：`/knowledge/ops/dashboard` · 权限 `kb:ops:dashboard`（SQL：`docs/sql/13_kb_ops_dashboard_menu.sql`）。
- 现网 `KnowledgeOpsDashboardView` **并行 3 请求**：
  1. `GET /kb/ask/llm-config` → D3 LLM 指示灯
  2. `GET /kb/lint/issues?status=0`（**无分页，全量 records**）→ D2 + D4 客户端聚合
  3. `GET /kb/sync/logs?pageNum=1&pageSize=500` → D1 近 7 日趋势客户端聚合

工单/日志量大时：首屏慢、占带宽、聚合不一致。

### 3.2 现网接口（权威 · 已实现）

```
GET /kb/ops/dashboard
```

**Query**：

| 参数 | 默认 | 说明 |
|------|------|------|
| `spaceId` | 空 | 可选；单空间看板 |
| `trendDays` | `7` | Sync 趋势天数（最大 30） |

> 注意：参数名为 **`trendDays`**（非 `days`）。

**Response** `MoliResult<KbOpsDashboardVo>`（Java 实现字段）：

```typescript
type KbOpsDashboardVo = {
  spaceId?: number | null
  /** D1 · 按日批次 success/fail 计数 */
  syncTrend: Array<{
    date: string           // yyyy-MM-dd
    successBatches: number
    failBatches: number
  }>
  /** D2 · 工单汇总 */
  lintSummary: {
    openCount: number
    ignoredCount: number
    fixedCount: number
    openByType: Record<string, number>   // issueType → count
    topBrokenLinks: string[]             // broken_link detail Top N
  }
  unresolvedRelationCount: number
  /** D3 · LLM 摘要（含近 N 日调用趋势） */
  llm: {
    enabled?: boolean
    available?: boolean
    provider?: string
    model?: string
    successRate?: number
    callTrend?: Array<{ date: string; count: number }>
    // …见 Swagger / KbOpsLlmSummaryVo
  }
  /** wiki↔DB 漂移采样 */
  driftSummary?: { /* KbOpsDriftSummaryVo */ }
}
```

### 3.2.1 前端映射（`KnowledgeOpsDashboardView`）

| 区块 | 现客户端聚合 | 改接 dashboard 字段 |
|------|--------------|---------------------|
| D1 Sync 趋势 | `aggregateSyncTrendByDay(syncLogs)` | `syncTrend` → 映射 `successBatches`/`failBatches` 为图表 |
| D2 待处理工单 | `aggregatePendingIssues(lintIssues)` | `lintSummary.openByType` + `spaces` 显示名 |
| D4 断链 Top | `topBrokenLinkIssues(lintIssues)` | `lintSummary.topBrokenLinks`（或继续 issues 接口） |
| D3 LLM 灯 | `getKbLlmConfigApi` | `llm.available` / `llm.enabled`；可保留 llm-config 降级 |

**建议新增**：

```typescript
// src/api/knowledge/kbOps.ts（新建或并入 knowledge.ts）
export const getKbOpsDashboardApi = (params?: { spaceId?: string; trendDays?: number }) =>
  request<KbOpsDashboardVo>(`${KB_BASE}/ops/dashboard${buildQuery(params)}`)
```

### 3.3 原稿目标形态（参考 · 与现实现略有差异）

| 区块 | 现聚合函数 | 后端替代 |
|------|------------|----------|
| D1 | `aggregateSyncTrendByDay` | `syncTrend` |
| D2 | `aggregatePendingIssues` | `pendingIssues` |
| D4 | `topBrokenLinkIssues` | `brokenLinkTop` |
| D3 | `getKbLlmConfigApi` | `llm` |

实现后前端改为 **单请求**；聚合逻辑可保留作 mock/降级。

### 3.4 验收

- [ ] 看板首屏仅 1 次 `GET /kb/ops/dashboard`（可保留 llm-config 独立请求作降级，但非必须）
- [ ] `syncTrend` 长度 = `days`，success+fail 与同期 `sync/logs` 抽样一致
- [ ] `pendingIssues` 各 `count` 之和 = `openIssueTotal`（若提供）
- [ ] 无权限 → 403；无数据 → 空数组非 null

---

## 4. 建议前端排期（2026-07-13）

```text
~~① DC-4~~ ✅ 2026-07-13
~~② KBOPS-2~~ ✅ 2026-07-13
~~③ KB-LINT~~ ✅ 2026-07-13
```

**运维可选**：8090 补 `kb_llm_call_log` 表 → dashboard 单请求稳定；删 `kb/wiki/_p0o4-fail-test` 样本目录。

---

## 5. 相关文档

| 文档 | 用途 |
|------|------|
| [frontend-gaps.md](../frontend-gaps.md) | 前端缺口总表 |
| [frontend-backend-dependencies.md](frontend-backend-dependencies.md) | 跨模块依赖 §4 · §7 转发 |
| [knowledge-ops-frontend.md](knowledge-ops-frontend.md) §8 | Dashboard D1–D4 现网聚合 |
| [deploy-center-project-first.md](../design/deploy-center-project-first.md) §5 | DC-4 产品背景 |
| [operation-frontend-handoff.md](operation-frontend-handoff.md) | 运营已交付基线 |

---

## 6. 状态登记表（2026-07-13）

| 任务 ID | 后端 | 前端 | 接口路径 | 备注 |
|---------|------|------|----------|------|
| DC-4 | ✅ `755abd43` | ✅ 已接线 | `GET /operation/task/groups` | `TaskHistoryView` 分组 |
| KB-LINT-1/2 | ✅ 已交付 | ✅ 已收紧 | `GET /kb/lint/issues` | `current`+`size`+`unassignedOnly` |
| KBOPS-2 | ✅ 已交付 | ✅ 已接线 | `GET /kb/ops/dashboard` | 单请求 + legacy 降级 |
