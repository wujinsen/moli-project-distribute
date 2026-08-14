# 服务器运维 · 产品 PRD（Operation / S-Ops）

> **状态**：active · 2026-07-13（W1–W10 + P3 前后端对齐）  
> **技术路线图**：[`docs/design/server-ops-module-roadmap.md`](../design/server-ops-module-roadmap.md)  
> **改造方案**：[`docs/design/operation-module-refactor-plan.md`](../design/operation-module-refactor-plan.md)  
> **前端契约**：[`docs/api/operation-frontend.md`](../api/operation-frontend.md) · [`operation-frontend-handoff.md`](../api/operation-frontend-handoff.md)  
> **HTTP 契约**：[`docs/api/operation-deploy-api.md`](../api/operation-deploy-api.md)  
> **边界**：**不含**知识库内容管道（KBOPS → [`knowledge-ops-prd.md`](knowledge-ops-prd.md)）

---

## 1. 背景与定位

面向 **平台运维 / SRE**，在 user-center 内提供：

1. **四台账** — 平台、服务器、项目、组件  
2. **部署中心** — 远程启停、SFTP 上传、命令执行、任务观测  
3. **关系与拓扑** — 项目/组件多机关联、全局拓扑图、统一关系抽屉  
4. **任务历史** — 扁平列表 + **按项目分组**（DC-4）

> **归属**：`moli-user-center` · 菜单 400 段 · 端口 `:8888` `/operation/*`

---

## 2. 用户与场景

| 角色 | 场景 | 期望 |
|------|------|------|
| **运维** | 新机器入库、关联项目 | CRUD + N:N links + 拓扑可见 |
| **运维** | 批量重启某项目下多台机 | 部署中心选项目 → batch task → 任务历史分组查看 |
| **运维** | 上传 jar 并重启 | SFTP upload + 后置 deploy |
| **开发联调** | 看某服务器关联哪些项目 | RelationDrawer / 拓扑 focus 深链 |

---

## 3. 菜单与能力（2026-07-13）

| 菜单 | 路由 | 状态 | 任务 ID |
|------|------|------|---------|
| 平台管理 | `operation/platform/index` | ✅ | SVR-1~3 |
| 服务器管理 | `operation/server/index` | ✅ | SVR-4~12 · SVR-23/24 角色/标签 |
| 项目管理 | `operation/project/index` | ✅ | SVR-22 · SVR-26b 组件依赖 |
| 组件管理 | `operation/component/index` | ✅ | SVR-22 |
| 部署中心 | `operation/deploy/index` | ✅ | SVR-13~20 · DC-2/3 |
| 端口矩阵 | `operation/port-matrix/index` | ✅ | SVR-21 |
| **拓扑图** | `operation/topology/index` | ✅ | SVR-25b · 菜单 407 |
| **任务历史** | `operation/task/history` | ✅ | DC-4 分组视图 |

---

## 4. 功能需求摘要

### P0 — 台账与部署

| ID | 需求 | 验收 |
|----|------|------|
| **S-VO** | 列表/详情 `*Count` 与 N:N 一致 | `serverCount === serverIds.length` |
| **S-ERR-1** | 错误码 10101–10109 Toast | i18n 映射 |
| **W7–W10** | create 返回 id · batch deploy · upload · cancel | 走查 ✅ 2026-07-13 |

### P1 — 拓扑与关系

| ID | 需求 | 验收 |
|----|------|------|
| **SVR-25** | `GET /operation/topology` 全图 | ECharts 拓扑页 |
| **SVR-28** | `GET /operation/relations/{type}/{id}` | RelationDrawer 四台账 + 部署中心 |
| **SVR-26** | 项目-组件 `depends_on` | component-links API + 拓扑边 |

### P2 — 部署中心增强

| ID | 需求 | 验收 |
|----|------|------|
| **DC-2** | 部署中心项目优先 | 选项目 → 过滤服务器 |
| **DC-3** | 追加台账机到任务 | 未关联 server 可选 |
| **DC-4** | 任务历史按项目分组 | `GET /operation/task/groups` + TaskHistoryView |

---

## 5. 权限与安全

| 权限码 | 用途 |
|--------|------|
| `operation:server:list` | 台账列表、拓扑、任务列表/分组 |
| `operation:deploy:exec` | 远程启停、batch task |
| `operation:file:upload` | SFTP 上传 |
| `operation:command:exec` | 远程命令 |
| `operation:ssh:manage` | SSH 凭据与探活 |

**默认关**：`ops.deploy.enabled` / `ops.upload.enabled` / `ops.command.enabled`（生产 Runbook 显式开启）。

---

## 6. 验收

| 类型 | 文档 |
|------|------|
| W1–W10 走查 | [`operation-w1-w10-walkthrough.md`](../test/operation-w1-w10-walkthrough.md) |
| 部署中心 | [`operation-deploy-center-acceptance.md`](../test/operation-deploy-center-acceptance.md) |
| DC-4 分组 | [`operation-task-groups-acceptance.md`](../test/operation-task-groups-acceptance.md) |
| 拓扑/关系 | [`operation-relations-topology-acceptance.md`](../test/operation-relations-topology-acceptance.md) |

---

## 7. 运维剩余（非功能开发）

| 项 | 负责方 |
|----|--------|
| 共享环境 jar 部署（含 `b4ac176a` / `755abd43`） | 运维 |
| 老库菜单 407 SQL | DBA · `28_operation_topology_menu.sql` |
| 生产开启 deploy/upload/command 开关 | 运维 |

---

## 8. 文档地图

| 类型 | 路径 |
|------|------|
| 本 PRD | `docs/product/operation-server-ops-prd.md` |
| 路线图 | `docs/design/server-ops-module-roadmap.md` |
| 拓扑设计 | `docs/design/server-topology-visualization.md` |
| 关系导航 | `docs/design/operation-relations-navigation.md` |
| 多机 links | `docs/design/operation-server-links.md` |
| API | `docs/api/operation-deploy-api.md` |

---

## 9. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-13 | 初稿：W1–W10 + SVR-25/28 + DC-4 产品索引 |
