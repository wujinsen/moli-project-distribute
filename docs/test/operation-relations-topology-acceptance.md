# 运营管理 · 拓扑与关联关系验收用例（SVR-25a / 26a / 28a / 28b）

> 模块：`moli-user-center-server` + `meiling-ui`（前端 S10/S11/S26 **已落地**，待联合点验）  
> 契约：[`docs/api/operation-frontend.md`](../api/operation-frontend.md) §5.6~5.8  
> 设计：[`server-topology-visualization.md`](../design/server-topology-visualization.md)、[`operation-relations-navigation.md`](../design/operation-relations-navigation.md)  
> 自动化：`mvn -pl moli-user-center-server -Dtest=Operation*Relation*,Operation*Topology*,OperationProjectComponentLink*,OperationRelationsTopologyControllersApiTest test`

## 0. 前置条件

| # | 项 | 期望 |
|---|-----|------|
| P0 | DB 已执行 `docs/sql/29_operation_project_component.sql` | 表 `operation_project_component` 存在 |
| P1 | 已有 SVR-22 N:N 数据 | `operation_server_project` / `operation_server_component` 有样例行 |
| P2 | 角色权限 | `operation:project:list`、`operation:server:list` |
| P3 | user-center 已部署含本次后端 | `GET /operation/topology`、`GET /operation/relations/...` 可访问 |

---

## 1. 项目依赖组件（SVR-26a）

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| PC-1 | 查询依赖 | `GET /operation/project/{id}/component-links` | 200；`{ projectId, componentIds[] }` |
| PC-2 | 保存依赖 | `PUT .../component-links` body `{ componentIds: [301,302] }` | 200；表全量替换为 2 行 |
| PC-3 | 非法组件 | `PUT` 含不存在 componentId | 业务错误「组件不存在」 |
| PC-4 | 删除级联 | 删除组件 301 后查项目 links | `componentIds` 不再含 301 |
| PC-5 | 删除项目级联 | 删除项目后查 `operation_project_component` | 对应行已清理 |

---

## 2. 列表关系计数与反向过滤（SVR-28a）

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| LC-1 | 项目计数 | `GET /operation/project/list` | 每行含 `serverCount`、`componentCount`；与 N:N+主 `server_id` 一致 |
| LC-1b | 项目详情计数 | `GET /operation/project/{id}` | `serverCount === serverIds.length`；与 list 同行 `serverCount` 一致 |
| LC-2 | 组件计数 | `GET /operation/component/list` | 每行含 `serverCount`、`projectCount`；`serverCount === serverIds.length` |
| LC-2b | 组件详情计数 | `GET /operation/component/{id}` | 同上恒等式；与 list 一致 |
| LC-3 | 服务器计数 | `GET /operation/server/list` | 每行含 `projectCount`、`componentCount` |
| LC-3b | 服务器详情计数 | `GET /operation/server/{id}` | 含 `projectCount`、`componentCount` |
| LC-4 | 按服务器筛项目 | `GET /operation/project/list?serverId=201` | 仅返回关联 201 的项目（含 N:N 与主 `server_id`） |
| LC-5 | 按组件筛项目 | `GET /operation/project/list?componentId=301` | 仅返回依赖 MySQL 的项目 |
| LC-6 | 按项目筛组件 | `GET /operation/component/list?projectId=401` | 仅返回被项目 401 依赖的组件 |
| LC-7 | 按项目筛服务器 | `GET /operation/server/list?projectId=401` | 仅返回承载该项目的机器 |
| LC-8 | 按组件筛服务器 | `GET /operation/server/list?componentId=301` | 仅返回部署该组件的机器 |

---

## 3. 统一关系 API（SVR-28b）

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| REL-1 | 项目视角 | `GET /operation/relations/project/401` | `entity` + `servers[]`（含 `primary`）+ `components[]` + `recentTasks`≤5 |
| REL-2 | 服务器视角 | `GET /operation/relations/server/201` | `projects[]` + `components[]` + `recentTasks`；`servers` 为空 |
| REL-3 | 组件视角 | `GET /operation/relations/component/301` | `servers[]` + `projects[]`；`components` 为空 |
| REL-4 | 主服务器标记 | 项目主 `server_id=201` 且 N:N 含 202 | `servers` 中 id=201 的 `primary=true` |
| REL-5 | 最近任务 | 项目有关联 deploy 任务 | `recentTasks` 含 taskType/action/status/createTime |
| REL-6 | 非法类型 | `GET /operation/relations/platform/1` | 参数错误 |
| REL-7 | 不存在实体 | `GET /operation/relations/project/999999` | 404 业务码 / 实体不存在 |

---

## 4. 全局拓扑图（SVR-25a）

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| TOP-1 | 全量节点 | `GET /operation/topology` | `servers`/`projects`/`components` 非空（有种子数据时） |
| TOP-2 | 节点 id 前缀 | 检查任意节点 | 服务器 `s-{id}`、项目 `p-{id}`、组件 `c-{id}` |
| TOP-3 | deploys 边 | 项目主 `server_id=201` | 存在 `{ source:"s-201", target:"p-401", type:"deploys" }` |
| TOP-4 | 边去重 | 主 `server_id` 与 N:N 重复 | 同 server→project 仅 1 条 `deploys` 边 |
| TOP-5 | depends_on 边 | 项目依赖 MySQL | `{ source:"p-401", target:"c-301", type:"depends_on" }` |
| TOP-6 | 端口状态 | 项目/组件节点 | 含 `portMatchStatus`（来自端口矩阵 Provider） |
| TOP-7 | 权限 | 无 `operation:server:list` | 403 |

---

## 5. 前端验收（SVR-25b / 28c / 26b · meiling-ui 已落地）

> 实现对照：meiling-ui [`operation-frontend-handoff.md`](../../meiling-ui/docs/api/operation-frontend-handoff.md) · §16 [`operation-frontend.md`](../../meiling-ui/docs/api/operation-frontend.md)

| ID | 场景 | 期望 |
|----|------|------|
| UI-1 | 三列表「关联」列 chips | 计数与 API 一致；点击打开 `RelationDrawer` |
| UI-2 | RelationDrawer | 调 `GET /operation/relations/...`；Tab 切换；「定位」带 query 过滤 |
| UI-3 | 拓扑页 | `OperationTopologyGraphView` 渲染 `GET /operation/topology`；`?focus=` 深链 |
| UI-4 | 项目依赖弹窗 | `OperationProjectComponentLinksModal` · `PUT .../component-links` 全量保存 |
| UI-5 | 关联保存回归 | 弹窗只选 1 台后 `serverCount=1`；详情 `GET /{id}` 同行 `serverCount=1`；无「幽灵第二台」 |

---

## 6. 单元测试覆盖（CI）

| 类 | 覆盖点 |
|----|--------|
| `OperationProjectComponentLinkServiceImplTest` | getLinks / syncLinks / 非法组件 |
| `OperationRelationQuerySupportTest` | 主 `server_id` 回退、计数、N:N 合并 |
| `OperationRelationServiceImplTest` | 三实体视角、primary 标记、非法类型 |
| `OperationTopologyServiceImplTest` | 节点前缀、deploys 去重、depends_on 边 |
| `OperationServerCascadeSupportTest` | 删项目/组件时清理 `operation_project_component` |
| `OperationRelationsTopologyControllersApiTest` | Controller：`GET /topology`、`GET /relations/*`、`GET/PUT component-links` |
