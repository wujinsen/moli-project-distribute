# 运营管理 · 后端联调通知（给 meiling-ui 前端）

> **更新**：2026-07-13 · **user-center `:8888` 已联调通过**  
> **前端对接专稿**：[operation-frontend.md](operation-frontend.md) · **meiling-ui 副本**：[`meiling-ui/docs/api/operation-frontend-handoff.md`](../../meiling-ui/docs/api/operation-frontend-handoff.md)  
> **HTTP 索引**：[user-center-api-map.md](user-center-api-map.md) §4

---

## 1. 结论（前端优先看）

| 项 | 状态 |
|----|------|
| **拓扑 / 关系 / component-links API** | ✅ 可联调（SVR-25a/26a/28a/28b） |
| **多服务器 links 同步（L7/L8）** | ✅ `PUT .../links` 同步主表 `server_id` / `server_ip` / `innerIp` |
| **create 带 `serverIds`** | ✅ `POST` body 写 N:N + 主 `serverId` 对齐 `serverIds[0]` |
| **`POST` 返回新建 id** | ✅ **Breaking**：`data` 为 **Long**，不再是 `boolean` |
| **order / bi 远程启停** | ✅ `presets.serviceKeys` 含五服务；`moli-service.sh` 已扩展 |
| **阻塞项** | **无** |

本地 smoke：`admin`/`123456` · Vite `http://127.0.0.1:5141` → proxy `8888` · 2026-07-13 通过。

---

## 2. Breaking · 前端必改

### 2.1 `POST /operation/project` · `POST /operation/component`

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
```

create 成功后可直接 `PUT /operation/project/{id}/links`（若 body 已带 `serverIds` 通常不必再补）。

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

**注意**：`serverCount` / `componentCount` 仅在 **`GET .../list`** 回填；`GET /{id}` 详情无计数字段——chips 请用列表或 relations。

---

## 4. 部署中心

| 项 | 说明 |
|----|------|
| `GET /operation/deploy/presets` | `serviceKeys`: user-center, gateway, knowledge, **order**, **bi** |
| 远程启停 | 目标机需 `deploy/linux/moli-service.sh`（已支持五 key） |
| dev 配置 | `ops.upload/command/deploy.enabled=true`；大文件 **勿经 Gateway** |
| `serverId` | 生产必传；`10109` = 未传 serverId 且 `allow-local=false` |

---

## 5. 前端点验清单（浏览器）

1. 登录 `http://127.0.0.1:5141`（`admin`/`123456`）
2. **项目管理** · 关联弹窗只选 1 台 → 列表 `serverCount=1`，抽屉 servers=1
3. **新建项目** · Network 看 `POST /operation/project` → `data` 为数字 id
4. **RelationDrawer** · `GET /operation/relations/...` 含 deployRunning / recentTasks
5. **拓扑图** · `GET /operation/topology` 可渲染
6. **部署中心** · presets 五服务；选项目 + 多机扇出（DC-2）

验收用例：[operation-relations-topology-acceptance.md](../test/operation-relations-topology-acceptance.md) §5。

---

## 6. 变更记录

| 日期 | 提交范围 | 说明 |
|------|----------|------|
| 2026-07-13 | links 同步修复 | `ab70ed3d` · `68142cbf` |
| 2026-07-13 | create 返回 id + order/bi 脚本 | 本轮 commit |
