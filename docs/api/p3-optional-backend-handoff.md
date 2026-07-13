# P3 可选增强 · 后端 API 需求（给 moli-server / knowledge-server）

> **更新**：2026-07-13  
> **读者**：user-center（`:8888`）、knowledge-server（`:8090`）、meiling-ui 前端  
> **优先级**：**P3 可选** — 不挡运营/KB 主流程；**前端暂缓开发**，待 API 就绪后再接线  
> **前端镜像**：[`meiling-ui/docs/api/p3-optional-backend-handoff.md`](../../meiling-ui/docs/api/p3-optional-backend-handoff.md)

| 任务 ID | 服务 | 前端落点 | 状态 |
|---------|------|----------|------|
| **DC-4** | `8888` | `TaskHistoryView.vue` | ⬜ 待后端 API |
| **KB-LINT-1** | `8090` | `kbLint.ts` · `KbLintIssuesPanel.vue` | ⬜ 待服务端分页 + `unassignedOnly` |
| **KB-LINT-2** | `8090` | 同上 | ⬜ 与 KB-LINT-1 同批交付 |
| **KBOPS-2** | `8090` | `KnowledgeOpsDashboardView.vue` | ⬜ 待 Dashboard 专用 API |

**前端现状**：均有 **客户端兜底**（扁平 task 列表 / Lint 全量 slice / Dashboard 多接口聚合），可继续用；本稿定义「收紧后」契约。

---

## 0. 给后端一句话（可复制）

```
【meiling-ui · P3 可选 API · 2026-07-13】

以下三项前端暂不开发，请后端评估排期后回复接口形态与 ETA：

1) DC-4（8888）任务历史按 project 聚合 — TaskHistoryView 分组视图
2) KB-LINT-1/2（8090）GET /kb/lint/issues 真分页 + unassignedOnly 服务端过滤
3) KBOPS-2（8090）GET /kb/ops/dashboard 运维看板专用聚合 API

详稿：moli-project-distribute/docs/api/p3-optional-backend-handoff.md
前端缺口：meiling-ui/docs/frontend-gaps.md §1.3 · §2.2
```

---

## 1. DC-4 · 任务历史按项目聚合（user-center `:8888`）

### 1.1 背景

- 部署中心已 **项目优先**（DC-2）；工具栏「任务历史」带 `?projectId=` 跳转 `TaskHistoryView`。
- 现网 `GET /operation/task/list` 为**扁平分页**，多机批量后同一项目下多台服务器任务分散，运维需肉眼归并。
- 设计意图：[deploy-center-project-first.md](../../meiling-ui/docs/design/deploy-center-project-first.md) §5「任务历史按 `projectId` 聚合视图」。

### 1.2 前端目标（API 就绪后）

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
- 权限：与 `operation:task:list`（或现 task list 同等权限）一致。

### 1.5 前端接线（API 就绪后）

| 文件 | 改动 |
|------|------|
| `src/api/operation.ts` | `listTaskGroupsApi` |
| `src/views/operation/TaskHistoryView.vue` | 平铺/分组切换 · 手风琴 UI |
| `src/i18n/locales/{zh,en,ja}.ts` | `operation.taskHistory.groupByProject` 等 |

### 1.6 验收

- [ ] 部署中心选项目 → 任务历史带 `projectId` → 分组视图仅 1 组或平铺一致
- [ ] 多项目账号：分组头 `taskCount` 与组内 `list?projectId=` 总数一致
- [ ] 无 `projectId` 任务进入「未关联项目」组
- [ ] 分页：`total` 为组数，翻页不串组

---

## 2. KB-LINT-1 / KB-LINT-2 · Lint 工单真分页（knowledge-server `:8090`）

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

### 2.5 前端接线

- `kbLint.ts`：收到标准分页后**不再** `paginateLintIssuesClientSide`。
- `KbLintIssuesPanel.vue`：`AppPagination` 的 `total` 直接用服务端 `total`。

### 2.6 验收

- [ ] `unassignedOnly=true` + `pageNum=2`：无 assignee 的工单，且页间不重复
- [ ] `total` 与 DB `COUNT(*)` 一致（同筛选条件）
- [ ] 1 万条工单：单页响应 < 合理体积（仅 20 条 records）

---

## 3. KBOPS-2 · 运维 Dashboard 专用 API（knowledge-server `:8090`）

### 3.1 背景

- 路由：`/knowledge/ops/dashboard` · 权限 `kb:ops:dashboard`（SQL：`docs/sql/13_kb_ops_dashboard_menu.sql`）。
- 现网 `KnowledgeOpsDashboardView` **并行 3 请求**：
  1. `GET /kb/ask/llm-config` → D3 LLM 指示灯
  2. `GET /kb/lint/issues?status=0`（**无分页，全量 records**）→ D2 + D4 客户端聚合
  3. `GET /kb/sync/logs?pageNum=1&pageSize=500` → D1 近 7 日趋势客户端聚合

工单/日志量大时：首屏慢、占带宽、聚合不一致。

### 3.2 推荐接口

```
GET /kb/ops/dashboard
```

**Query**：

| 参数 | 默认 | 说明 |
|------|------|------|
| `days` | `7` | Sync 趋势天数 |
| `brokenTopN` | `10` | 断链 Top N |
| `spaceId` | 空 | 可选；单空间看板 |

**Response** `MoliResult<KbOpsDashboardVo>`：

```typescript
type KbOpsDashboardVo = {
  /** D1 · 近 N 日按日聚合（服务端算好，前端只画图） */
  syncTrend: Array<{
    date: string          // yyyy-MM-dd
    success: number
    fail: number
  }>
  /** D2 · 待处理工单 status=0，按 space + issueType */
  pendingIssues: Array<{
    spaceId: string | number
    spaceName?: string
    issueType: string
    count: number
  }>
  /** D4 · 断链 Top N（按 detail 归并） */
  brokenLinkTop: Array<{
    spaceId?: string | number
    spaceName?: string
    detail: string
    count: number
  }>
  /** D3 · LLM（可与 /kb/ask/llm-config 同结构或子集） */
  llm: {
    available: boolean
    configEnabled?: boolean
    apiKeyConfigured?: boolean
    encryptionReady?: boolean
    defaultModel?: string
    message?: string
  }
  /** 可选摘要 */
  meta?: {
    syncLogSampled?: boolean   // 日志是否抽样
    openIssueTotal?: number
    generatedAt?: string
  }
}
```

**权限**：`kb:ops:dashboard`（与菜单一致）。

### 3.3 与现网关系

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

## 4. 建议后端排期

```text
① KB-LINT-1/2（8090）— 改动面小，收益直接（质量 Tab + Dashboard 减负）
② KBOPS-2（8090）— 依赖 Lint 分页或 DB 聚合成熟
③ DC-4（8888）— 运营体验增强，可与 W1–W10 走查并行评估
```

---

## 5. 相关文档

| 文档 | 用途 |
|------|------|
| [frontend-gaps.md](../frontend-gaps.md) | 前端缺口总表 |
| [frontend-backend-dependencies.md](frontend-backend-dependencies.md) | 跨模块依赖 §4 · §7 转发 |
| [knowledge-ops-frontend.md](knowledge-ops-frontend.md) §8 | Dashboard D1–D4 现网聚合 |
| [deploy-center-project-first.md](../../meiling-ui/docs/design/deploy-center-project-first.md) §5 | DC-4 产品背景 |
| [operation-frontend-handoff.md](operation-frontend-handoff.md) | 运营已交付基线 |
| [operation-w1-w10-walkthrough.md](../test/operation-w1-w10-walkthrough.md) | W1–W10 走查 ✅ |

---

## 6. 后端回复模板（请填回）

| 任务 ID | 是否做 | 接口路径 | 预计版本/commit | 备注 |
|---------|--------|----------|-----------------|------|
| DC-4 | ⬜ | | | 方案 A / B |
| KB-LINT-1/2 | ⬜ | `GET /kb/lint/issues` | | |
| KBOPS-2 | ⬜ | `GET /kb/ops/dashboard` | | |
