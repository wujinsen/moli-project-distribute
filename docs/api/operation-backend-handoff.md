# 运营管理 · 后端联调通知（给 meiling-ui 前端）

> **更新**：2026-07-13 · **DC-4 `755abd43`** · **W1–W10 走查 ✅** · **前端 W7–W10 已对接**  
> **走查**：[operation-w1-w10-walkthrough.md](../test/operation-w1-w10-walkthrough.md)  
> **前端开工**：[operation-frontend-handoff.md](operation-frontend-handoff.md) · **完整契约**：[operation-frontend.md](operation-frontend.md)  
> **meiling-ui 交付稿**：[`meiling-ui/docs/api/operation-frontend-handoff.md`](../../meiling-ui/docs/api/operation-frontend-handoff.md)

---

## 1. 结论（前端优先看）

| 项 | 状态 |
|----|------|
| **拓扑 / 关系 / component-links API** | ✅ 可联调（SVR-25a/26a/28a/28b） |
| **多服务器 links 同步（L7/L8）** | ✅ `PUT .../links` 同步主表 `server_id` / `server_ip` / `innerIp` |
| **create 带 `serverIds`** | ✅ `POST` body 写 N:N + 主 `serverId` 对齐 `serverIds[0]` |
| **create 返回新建 id** | ✅ **Breaking**：`data` 为 **Long**，不再是 `boolean`（**含 `POST /operation/server`**） |
| **批量滚动重启** | ✅ `POST /operation/deploy/batch/task` |
| **批量 links** | ✅ `GET .../project|component/links/batch?ids=` |
| **任务取消** | ✅ `POST /operation/task/{id}/cancel` |
| **任务历史分组（DC-4）** | ✅ `GET /operation/task/groups` · 见 [p3-optional-backend-handoff.md](p3-optional-backend-handoff.md) §1 |
| **detail `*Count` 与 list 一致** | ✅ **`toVo()` 统一派生** · [operation-frontend-handoff.md](operation-frontend-handoff.md) |
| **order / bi 远程启停** | ✅ `presets.serviceKeys` 含五服务；`moli-service.sh` 已扩展 |
| **阻塞项** | **无**；P3 前后端 ✅；共享环境待 **jar 部署**（代码已 push） |

本地：`admin`/`123456` · Vite `5141` → `8888` · 后端 smoke 2026-07-13 通过 · 见 [走查稿](../test/operation-w1-w10-walkthrough.md)。

---

## 2. Breaking · 前端已对齐（2026-07-13 ✅）

> meiling-ui 已改 `addProjectApi` / `addComponentApi` / `addServerApi` → `request<number | string>`；本节供契约留存与回归。

### 2.1 `POST /operation/project` · `POST /operation/component` · `POST /operation/server`

| 变更前 | 变更后 |
|--------|--------|
| `{ code: 200, data: true }` | `{ code: 200, data: 731708402010423296 }`（新建台账 **id**） |

**meiling-ui 建议**：

```typescript
// src/api/operation.ts
export const addProjectApi = (body: OperationProjectSave) =>
  request<number>(`${OP}/project`, { method: 'POST', data: body })

export const addComponentApi = (body: OperationComponentSave) =>
  request<number>(`${OP}/component`, { method: 'POST', data: body })

export const addServerApi = (body: OperationServerSave) =>
  request<number>(`${OP}/server`, { method: 'POST', data: body })
```

create 成功后可直接 `PUT /operation/project/{id}/links`（若 body 已带 `serverIds` 通常不必再补）。

**批量滚动重启**（替代 N 次单任务扇出）：

```typescript
export const createDeployBatchTaskApi = (body: OperationDeployBatchTaskRequest) =>
  request<number>(`${OP}/deploy/batch/task`, { method: 'POST', data: body })
```

**批量 links**（列表页减轻 N+1，仍勿用于 chips 计数）：

```typescript
export const getProjectLinksBatchApi = (ids: number[]) =>
  request<{ items: OperationProjectLinksVo[] }>(`${OP}/project/links/batch`, { params: { ids: ids.join(',') } })

export const cancelOperationTaskApi = (taskId: number) =>
  request<OperationTaskVo>(`${OP}/task/${taskId}/cancel`, { method: 'POST' })
```

### 2.2 其它接口不变

`PUT` 项目/组件、`PUT .../links` 仍返回 `boolean`。

---

## 3. 关联保存契约（S6-b · 已验收）

| 操作 | 行为 |
|------|------|
| `POST` create + `serverIds` | N:N 写入；主 `serverId` = `serverIds[0]`；`server_ip` 随台账覆盖 |
| `PUT .../links` 全量替换 | 同步主表；**不再**报「serverIp 与 serverId 不一致」 |
| `GET .../links` | 有序 `serverIds`；无关联 `[]` |
| `GET /operation/relations/project/{id}` | N:N 非空时 **仅 N:N 计数**；无幽灵第二台 |
| `GET /operation/relations/server/{id}` | **对称**：项目 N:N 非空时不再用残留主表 `server_id` 计入该机 |
| `GET /operation/audit/reconcile-relations` | 一次性把主表 `server_id` 对齐到 N:N 首台（修历史脏数据） |

**注意**：`serverCount` / `componentCount` 在 **`toVo()` 内与 `serverIds` 同源派生**（list / detail / checkHealth 凡走 `toVo` 均一致）；`serverCount === serverIds.length`。

---

## 4. 部署中心

| 项 | 说明 |
|----|------|
| `GET /operation/deploy/presets` | `serviceKeys`: user-center, gateway, knowledge, **order**, **ai** |
| **上传** | `POST /operation/file/upload` → `taskId`；需 `ops.upload.enabled=true` + SSH |
| **批量重启** | `POST /operation/deploy/batch/task`（`steps[]` / `intervalSeconds`） |
| **任务取消** | `POST /operation/task/{id}/cancel` → `status=cancelled` |
| 远程启停 | 目标机 `deploy/linux/moli-service.sh` |
| dev 配置 | `ops.upload/command/deploy.enabled=true`；大文件 **勿经 Gateway** |
| `serverId` | 生产必传；`10109` = 未传 serverId 且 `allow-local=false` |

前端对接详见 [operation-frontend-handoff.md §3](operation-frontend-handoff.md#3-部署中心与异步任务2026-07-13-新增)。

---

## 5. 前端点验清单（浏览器）

1. 登录 `http://127.0.0.1:5141`（`admin`/`123456`）
2. **项目管理** · 关联弹窗只选 1 台 → 列表 `serverCount=1`，抽屉 servers=1
3. **新建项目** · Network 看 `POST /operation/project` → `data` 为数字 id
4. **RelationDrawer** · `GET /operation/relations/...` 含 deployRunning / recentTasks
5. **拓扑图** · `GET /operation/topology` 可渲染
6. **部署中心** · 上传 `POST /file/upload` → taskId；多机用 `POST /deploy/batch/task`；任务可 cancel

验收用例：[operation-relations-topology-acceptance.md](../test/operation-relations-topology-acceptance.md) §5。

---

## 6. 变更记录

| 日期 | 提交范围 | 说明 |
|------|----------|------|
| 2026-07-13 | links 同步修复 | `ab70ed3d` · `68142cbf` |
| 2026-07-13 | create 返回 id + order/ai 脚本 | 本轮 commit |
| 2026-07-13 | 详情 VO 关系计数 | **`toVo()` 统一派生 `*Count`**；前端 [operation-frontend-handoff.md](operation-frontend-handoff.md) §0 |
| 2026-07-13 | server create 返回 id + 批量 deploy/links | `POST /operation/server` → Long；`POST /deploy/batch/task`；`GET .../links/batch` |
| 2026-07-13 | 任务取消 | `POST /operation/task/{id}/cancel`；`status=cancelled` |
| 2026-07-13 | 反向关联对称 + reconcile | `resolveProjectIdsForServer` 与 N:N 对称；`GET /operation/audit/reconcile-relations` |
| 2026-07-13 | **DC-4** `755abd43` · **push** `origin/ci/kb-sync-multi-space-gate`（`b4ac176a` 含） |
| 2026-07-13 | **W1–W10 走查 ✅** · 对齐 meiling-ui frontend-gaps / P3 handoff |

---

## 7. 剩余后端依赖（给后端同学）

Breaking（create 返回 id）已对齐。运营 / 知识库 / SSO **剩余契约、环境、排期**见：

**[frontend-backend-dependencies.md](frontend-backend-dependencies.md)**（§2 任务矩阵 · §7 转发 · §8 评估与后端回复）

索引入口：[frontend-gaps.md](../frontend-gaps.md)
