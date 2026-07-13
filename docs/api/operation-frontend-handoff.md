# 运营管理 · 前端开工手册（meiling-ui）

> **更新**：2026-07-13（**S-VO + W7–W10 前端已完工** · 待联合走查）  
> **后端**：commit **`b4ac176a`** · `:8888` 无 API 阻塞  
> **走查**：[operation-w1-w10-walkthrough.md](../test/operation-w1-w10-walkthrough.md) · meiling-ui 交付稿 [`meiling-ui/docs/api/operation-frontend-handoff.md`](../../meiling-ui/docs/api/operation-frontend-handoff.md)  
> **给后端**：[frontend-backend-dependencies.md](frontend-backend-dependencies.md) · [operation-backend-handoff.md](operation-backend-handoff.md)

本地：`http://127.0.0.1:5141` → proxy `8888` · `admin`/`123456`  
**前置**：重启 user-center `:8888`（含 `toVo` 计数、批量 deploy、任务取消等 2026-07-13 改动）

---

## 0. 给前端一句话（2026-07-13）

> 1. **`serverCount` 等已在 `toVo()` 派生** — list / detail / check 一致；**`serverCount === serverIds.length`**；勿为 chips 批量 `GET .../links`。  
> 2. **`POST` 新建 project / component / server** — 响应 `data` 均为 **number（Long id）**（Breaking）。  
> 3. **部署中心** — 多机滚动重启用 **`POST /operation/deploy/batch/task`**；上传仍 **`POST /operation/file/upload`** → `taskId` 轮询。  
> 4. **任务** — 轮询 `GET /operation/task/{id}`；运行中可 **`POST /operation/task/{id}/cancel`**（`status=cancelled`）。  
> 5. **可选** — 列表减轻 N+1：`GET .../project|component/links/batch?ids=1,2,3`（最多 50，**勿**用于 chips 计数）。

---

## 1. 后端已就绪 · 前端已对接（2026-07-13）

| 项 | 后端 | 前端（meiling-ui） |
|----|------|-------------------|
| `POST` project/component/**server** 返回 **id** | ✅ | ✅ W7 |
| **list + detail `*Count`** | ✅ `toVo()` | ✅ S-VO W1–W6 |
| **批量滚动重启** | ✅ batch/task | ✅ W9 |
| **任务取消** | ✅ cancel | ✅ W10 |
| **上传轮询** | ✅ upload→taskId | ✅ W8 |
| **联合走查** | 参与勾选 | 🟡 [走查稿](../test/operation-w1-w10-walkthrough.md) |

---

## 2. 本轮前端任务（S-VO · 关系计数）

### 2.1 契约（list / detail / 探活 一致）

| 页 | 适用 API | VO 字段 |
|----|----------|---------|
| 项目 | `GET .../list`、`GET /{id}` | `serverIds`、`serverCount`、`componentCount` |
| 组件 | 同上 | `serverIds`、`serverCount`、`projectCount` |
| 服务器 | 同上 | `projectCount`、`componentCount` |

**恒等式**：项目/组件 `serverCount === serverIds.length`。

**反例**：

```typescript
// ❌ 为 chips 批量拉 links
chipCount = (await getProjectLinksApi(id)).data.serverIds.length

// ✅
chipCount = row.serverCount ?? row.serverIds?.length ?? 0
```

### 2.2 建议改动

| ID | 文件 | 改动 |
|----|------|------|
| S-VO-1 | `types/operation.ts` | 补全 `*Count`；create 响应类型为 `number` |
| S-VO-2 | 三管理页 | 详情用 `getXxxApi(id)` 的 `serverCount` |
| S-VO-3 | `useOperationLinksEnrich` | 列表 chips 用 list 的 `serverCount`；links 仅弹窗 |
| S-VO-4 | `OperationRelationChips` | 优先 `row.serverCount` |
| S-VO-5 | 保存关联后 | `PUT links` → 刷新 list |

**仍必须用 links**：关联弹窗 `GET/PUT .../links`（单 id）。

---

## 3. 部署中心与异步任务（2026-07-13 新增）

### 3.1 Breaking · 新建服务器返回 id

```typescript
export const addServerApi = (body: OperationServerSave) =>
  request<number>(`${OP}/server`, { method: 'POST', data: body })
```

与 `addProjectApi` / `addComponentApi` 一致；成功后可直接 `PUT .../links` 或带 `serverIds` 的 create body。

### 3.2 上传并发布（不变路径）

| 项 | 说明 |
|----|------|
| **API** | `POST /operation/file/upload`（`multipart/form-data`） |
| **字段** | `file`、`serverId`、`targetPath`、`postAction?`、`postCommand?` |
| **响应** | `data: taskId`（Long） |
| **后续** | `GET /operation/task/{taskId}?logOffset=` 轮询至 `finished=true` |
| **权限** | `operation:file:upload` + `operation:server:list` |
| **本地** | `ops.upload.enabled=true`；目标机 SSH 已配置；路径在 presets / 白名单内 |

**常见报错**（Network `msg`）：

| msg 关键词 | 处理 |
|------------|------|
| 远程上传未启用 | 后端 `ops.upload.enabled=true` 并重启 |
| 路径不在白名单 | 用 `GET /operation/deploy/presets` 的 `pathPresets` |
| 服务器不存在 / SSH | 先配 `PUT .../ssh` 并 `ssh/test` |

### 3.3 批量滚动重启（替代多机扇出）

**`POST /operation/deploy/batch/task`** · JSON · 权限 `operation:deploy:exec` + `list`

```typescript
export type OperationDeployBatchTaskRequest = {
  steps: Array<{
    serviceKey: string      // user-center | gateway | knowledge | order | bi
    action: 'start' | 'stop' | 'restart'
    serverId?: number
    projectId?: number
  }>
  projectId?: number        // 批次级；步骤未传时回填
  stopOnFailure?: boolean   // 默认 true
  intervalSeconds?: number  // 0~300，步骤间隔，默认 0
}

export const createDeployBatchTaskApi = (body: OperationDeployBatchTaskRequest) =>
  request<number>(`${OP}/deploy/batch/task`, { method: 'POST', data: body })
```

- 返回 **单父任务** `taskId`（`taskType=deploy_batch`）
- 日志含 `[BATCH]`；与单任务相同轮询
- 单步仍可用 `POST /operation/deploy/{serviceKey}/{action}/task?serverId=&projectId=`

**DeployCenterView 建议**：多机「滚动重启」勾选列表 → 组装 `steps[]` → 一次 batch → 一个任务面板。

### 3.4 任务轮询与取消

| API | 说明 |
|-----|------|
| `GET /operation/task/{id}?logOffset=` | 增量日志；`finished` 含 success/failed/**cancelled** |
| `GET /operation/task/{id}/poll` | 与上等价别名 |
| **`POST /operation/task/{id}/cancel`** | pending/running 可取消；终态拒绝 |

```typescript
export const cancelOperationTaskApi = (taskId: number) =>
  request<OperationTaskVo>(`${OP}/task/${taskId}/cancel`, { method: 'POST' })
```

**协作式取消**：SSH 执行中不会立刻中断；批量 deploy / 探活在**步骤间隙**退出。UI 取消后**继续 poll** 直至 `finished=true` 且 `status=cancelled`。

**任务面板建议**：`running` 时显示「取消」；`status` 展示 `cancelled` 样式（与 failed 区分）。

### 3.5 批量 links（可选 · 减 N+1）

```typescript
export const getProjectLinksBatchApi = (ids: number[]) =>
  request<{ items: OperationProjectLinksVo[] }>(
    `${OP}/project/links/batch`,
    { params: { ids: ids.join(',') } }
  )

export const getComponentLinksBatchApi = (ids: number[]) =>
  request<{ items: OperationComponentLinksVo[] }>(
    `${OP}/component/links/batch`,
    { params: { ids: ids.join(',') } }
  )
```

- `ids` 逗号分隔，**最多 50**
- **禁止**用 batch links 结果覆盖 list 行的 `serverCount`（仍以 VO 为准）

---

## 4. TypeScript 汇总（`src/api/operation.ts`）

```typescript
const OP = '/operation'

// --- Create（均返回 number id）---
export const addProjectApi = (body: OperationProjectSave) =>
  request<number>(`${OP}/project`, { method: 'POST', data: body })
export const addComponentApi = (body: OperationComponentSave) =>
  request<number>(`${OP}/component`, { method: 'POST', data: body })
export const addServerApi = (body: OperationServerSave) =>
  request<number>(`${OP}/server`, { method: 'POST', data: body })

// --- 单任务启停 ---
export const createDeployTaskApi = (
  serviceKey: string, action: string, serverId?: number, projectId?: number
) => request<number>(`${OP}/deploy/${serviceKey}/${action}/task`, {
  method: 'POST', params: { serverId, projectId }
})

// --- 批量滚动重启 ---
export const createDeployBatchTaskApi = (body: OperationDeployBatchTaskRequest) =>
  request<number>(`${OP}/deploy/batch/task`, { method: 'POST', data: body })

// --- 上传 ---
export const uploadFileApi = (form: FormData) =>
  request<number>(`${OP}/file/upload`, { method: 'POST', data: form })

// --- 任务 ---
export const pollTaskApi = (id: number, logOffset = 0) =>
  request<OperationTaskVo>(`${OP}/task/${id}`, { params: { logOffset } })
export const cancelTaskApi = (id: number) =>
  request<OperationTaskVo>(`${OP}/task/${id}/cancel`, { method: 'POST' })

// --- Links（弹窗 / 可选 batch）---
export const getProjectLinksApi = (id: number) =>
  request<OperationProjectLinksVo>(`${OP}/project/${id}/links`)
export const getProjectLinksBatchApi = (ids: number[]) =>
  request<{ items: OperationProjectLinksVo[] }>(`${OP}/project/links/batch`, {
    params: { ids: ids.join(',') }
  })
```

---

## 5. 浏览器走查

| # | 操作 | 通过标准 |
|---|------|----------|
| W1–W6 | S-VO | 见上版（chips、list vs detail、关联弹窗） |
| **W7** | 新建服务器 | `POST /operation/server` → `data` 为 number |
| **W8** | 上传并发布 | `POST /operation/file/upload` → `taskId`；轮询至 success；日志含 SFTP |
| **W9** | 批量滚动重启 | `POST /deploy/batch/task` → 单 taskId；日志 `[BATCH]` 多步 |
| **W10** | 取消任务 | 运行中点取消 → `status=cancelled`，`finished=true` |

验收：[operation-w1-w10-walkthrough.md](../test/operation-w1-w10-walkthrough.md) · [operation-relations-topology-acceptance.md](../test/operation-relations-topology-acceptance.md) · [operation-deploy-center-acceptance.md](../test/operation-deploy-center-acceptance.md)

---

## 6. 其它模块

| 模块 | 文档 | 阻塞 |
|------|------|------|
| 知识库 | [knowledge-workbench-frontend.md](knowledge-workbench-frontend.md) | 环境配置 |
| SSO | [frontend-backend-dependencies.md §4](frontend-backend-dependencies.md#4-sso--按系统隔离菜单) | 后端 `system_id` |

---

## 7. 转发与开工

### 7.1 转发（联合走查 · 可复制）

```
【运营 · 2026-07-13 前后端对齐】
后端：commit b4ac176a · :8888 install+重启 · ops.upload/deploy.enabled=true
前端：meiling-ui W1–W10 代码已完工

联合走查：monorepo docs/test/operation-w1-w10-walkthrough.md（§5 记录表）
契约：docs/api/operation-frontend-handoff.md · operation-frontend.md §10/§16
跨模块：docs/api/frontend-backend-dependencies.md §7
```

### 7.2 开工提示词（meiling-ui 对话）

```
请读 monorepo docs/api/operation-frontend-handoff.md：
1. §2 S-VO：serverCount，去掉 enrichRowsWithLinks 计数
2. §3 部署中心：upload 轮询、batch/task 多机重启、task cancel 按钮
3. §4 对齐 src/api/operation.ts
4. 跑 §5 W1–W10
后端 :8888 已重启最新 user-center 后再测
```

### 7.3 相关

- 部署 API 字段级：[operation-deploy-api.md](operation-deploy-api.md)  
- HTTP 索引：[user-center-api-map.md](user-center-api-map.md) §4  
- links 设计：[operation-server-links.md](../design/operation-server-links.md) §5
