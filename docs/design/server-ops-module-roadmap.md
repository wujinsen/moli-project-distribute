# 服务器运维模块 · 演进规划（技术端运维）

> 更新：2026-07-13 · 状态：**后端 + 前端主线已闭环**（SVR-1～28、21、22～24）；**2026-07-13 已修** 多服务器双轨同步与 `serverId`/`serverIp` 绑定  
> **meiling-ui 交付**：[`meiling-ui/docs/api/operation-frontend-handoff.md`](../../meiling-ui/docs/api/operation-frontend-handoff.md)
> 归属：`moli-user-center` · `operation_*` 表 · 菜单「运营/运维管理」(id 400)
> 边界：**只管服务器/基础设施资产运维**；知识库内容管道运维见 [`kb-ops-roadmap.md`](kb-ops-roadmap.md)（另一条独立路线，互不重叠）

---

## 1. 背景与定位

本模块**原始设计偏技术端**：给运维/技术人员一个**基础设施资产台账**——记录"哪些平台账号、哪些服务器、哪些组件、部署在哪、什么环境"。

它**不是**知识库运维，也不是业务运营；它面向的是"机器和部署"这一层。

---

## 2. 现状（静态台账 CRUD）

### 2.1 表结构（`scripts/moli.sql`）

| 表 | 用途 | 关键字段 |
|----|------|----------|
| `operation_platform_info` | 运维平台账号 | `platform_name`, `url`, `account`, `password`, `environment` |
| `operation_server_info` | 服务器 | `server_name`, `ip`, `inner_ip`, `port`, `environment`, `server_role`, `tags` |
| `operation_component_deploy_info` | 组件部署 | `component_name`, `server_ip`, `account`, `password`, `deploy_path`, `port`, `version`, `environment` |
| `operation_project_deploy_info` | 项目部署 | `server_id`, `server_ip`, `url`, `project_name`, `deploy_path`, `port`, `environment` |
| `operation_server_project` | 服务器↔项目 N:N | `server_id`, `project_id` |
| `operation_server_component` | 服务器↔组件 N:N | `server_id`, `component_id` |

`environment`：`1 dev / 2 test / 3 pre / 4 pro`。

### 2.2 接口（`moli-user-center` · 前缀 `/operation/*`）

| Controller | 前缀 | 能力 | 权限码 |
|------------|------|------|--------|
| `OperationPlatformController` | `/operation/platform` | list/insert/update/get/remove | `operation:platform:*` |
| `OperationServerController` | `/operation/server` | 同上 | `operation:server:*` |
| `OperationComponentController` | `/operation/component` | 同上 | `operation:component:*` |
| `OperationProjectController` | `/operation/project` | 同上 | `operation:project:*` |

平台 / 服务器 / 组件 / 项目均为 **Service 层**（列表 enrichment、加解密、探活、关联维护）；列表支持按名称 like + environment 过滤 + 分页。

---

## 3. 现状问题

| # | 问题 | 严重度 | 位置 |
|---|------|--------|------|
| S-P1 | **密码明文存库**：`operation_platform_info.password`、`operation_component_deploy_info.password` 明文；`list`/`get` 直接回明文 | 🔴 高 | 四表 + Controller |
| S-P2 | **死台账**：与真实服务器/组件无连接，部署变化不会反映，容易过时失真 | 🟡 中 | 全模块 |
| S-P3 | **敏感变更无审计**：谁改了生产服务器/密码查不到（虽有 `LogAspect`，但需确认 operation 包被拦截且记录） | 🟡 中 | `LogAspect` |
| S-P4 | **无 Service 层**：脱敏、校验、加解密无处挂载 | 🟢 低 | Controller |
| S-P5 | 关联表（server-project / server-component）只有裸 N:N，无级联视图（"某服务器上跑了哪些项目+组件"一次查不出） | 🟢 低 | Mapper |

---

## 4. 目标：静态台账 → "活的"服务器运维

从"记录本"演进为**可观测、可追溯、安全**的技术端运维台：

1. 敏感凭据**加密存储** + 列表脱敏 + 按权限查看明文。
2. 服务器/组件**健康探测**（可达性、端口、版本），台账带"状态灯"。
3. 部署信息与真实拓扑/端口矩阵**对齐校验**。
4. 敏感操作**审计**闭环。

---

## 5. 路线图

### P0 —— 安全（先做）

| 任务 | 内容 | 涉及 |
|------|------|------|
| **SVR-1** | 密码/凭据 **AES 加密入库** + 列表脱敏；Service 层 `OperationSecretCipher` / `OperationSecretSupport` | ✅ 2026-07-09 |
| **SVR-2** | 敏感变更**审计**：`LogAspect` 覆盖 operation；请求参数 password 脱敏 | ✅ 2026-07-09 |
| **SVR-3** | 明文查看**降权**：`GET .../{id}/secret` + `operation:secret:view` | ✅ 2026-07-09 |

### P1 —— 可观测

| 任务 | 内容 |
|------|------|
| **SVR-4** | **健康探测**：对 `operation_server_info` / `operation_component_deploy_info` 做 TCP 端口探活，记录 `last_check_time` / `status`；`POST .../check` 触发探测 | ✅ 2026-07-09 |
| **SVR-5** | **级联视图**：原 `GET /operation/server/{id}/topology` → **已删除**，由 `GET /operation/relations/server/{id}`（SVR-28b）承接 | ✅ 2026-07-09 · 下线 2026-07-12 |
| **SVR-6** | 前端「运维管理」页展示状态灯 + 行内探测 + 拓扑弹窗 + 关联编辑（meiling-ui） | ✅ 2026-07-10 · [operation-frontend.md](../api/operation-frontend.md) §5 |

### P2 —— 联动与自动化（按需）

| 任务 | 内容 |
|------|------|
| **SVR-7** | 部署信息与 `docs/ops/production-checklist.md` 端口矩阵对齐校验；`GET /operation/audit/port-matrix`；组件列表带 `portMatchStatus` | ✅ 2026-07-09 |
| **SVR-8** | 只读查询 `deploy/linux/moli-service.sh status`；`POST .../{action}` 变更动作需 `ops.deploy.enabled=true` + `operation:deploy:exec` | ✅ 2026-07-09 |
| **SVR-9** | 驾驶舱 ops 页 KPI 接 `GET /operation/stats` 真实台账计数 | ✅ 2026-07-09 |

### 架构债收尾（2026-07-10）

| 任务 | 内容 | 状态 |
|------|------|------|
| **SVR-10** | 项目管理引入 `OperationProjectService`；列表/详情返回 `OperationProjectVo`（`portMatchStatus` / `deployRunning`）；保存时 `serverIp` → `serverId` 回填 | ✅ |
| **SVR-11** | N:N 关联 CRUD：`GET/PUT /operation/server/{id}/links` 维护 `operation_server_project` / `operation_server_component` | ✅ |
| **SVR-12** | 定时探活 + 部署状态同步：`OperationHealthProbeScheduler` + `POST /operation/health/probe-all`；配置 `ops.health.*`；迁移 `20_operation_project_deploy_columns.sql` | ✅ |

### P3 —— 远程部署自动化（SVR-13 ~ SVR-16，2026-07-11）

| 任务 | 内容 | 状态 |
|------|------|------|
| **SVR-13** | 服务器 SSH 凭据：JSch、`PUT /operation/server/{id}/ssh`、`POST .../ssh/test`；AES-GCM 只写不读；内网 IP 优先 | ✅ |
| **SVR-14** | 异步任务：`operation_task` 表、`GET /operation/task/{id}` 轮询进度/增量日志、`GET /operation/task/list` | ✅ |
| **SVR-15** | 远程启停：`POST /operation/deploy/{key}/{action}/task?serverId=`；SSH 执行 `moli-service.sh`；脚本自动就位 | ✅ |
| **SVR-16** | 文件发布：`POST /operation/file/upload`；SFTP 进度；路径白名单 + `postAction` 枚举（nginxReload/unzipToDist/restartService） | ✅ |
| **SVR-17** | 前端部署中心 + 服务器 SSH 配置弹窗（meiling-ui `DeployCenterView`） | ✅ |

### P3+ —— 部署中心灵活化（SVR-18 ~ SVR-20，2026-07-11）

| 任务 | 内容 | 状态 |
|------|------|------|
| **SVR-18** | 远程命令：`OperationShellGuard` 高危拦截；`POST /operation/command/exec/task`；`ops.command.enabled`；`operation:command:exec` | ✅ |
| **SVR-19** | 上传灵活化：手输 `targetPath` + 三层路径白名单；`postAction=custom` + `postCommand`；服务器 `upload_allowed_roots` | ✅ |
| **SVR-20** | `GET /operation/deploy/presets`；前端去掉硬编码下拉，改 API 预设 + 自定义命令区 | ✅ |

### P2+ —— 端口矩阵可配置化（SVR-21，设计稿 2026-07-11）

| 任务 | 内容 | 状态 |
|------|------|------|
| **SVR-21a** | 表 `operation_port_matrix` + `operation_port_matrix_alias`；种子 = 现 `OperationPortMatrix.java` | ✅ · [`24_operation_port_matrix.sql`](../sql/24_operation_port_matrix.sql) |
| **SVR-21b** | `OperationPortMatrixProvider` 内存缓存；审计/列表改读 DB；空表回退内置默认 | ✅ |
| **SVR-21c** | CRUD `GET/POST/PUT/DELETE /operation/port-matrix/*`；权限 `operation:port-matrix:*`；菜单 406 | ✅ · [`operation-port-matrix-api.md`](../api/operation-port-matrix-api.md) |
| **SVR-21d** | meiling-ui 管理页 `operation/port-matrix/index`（用户方） | ✅ |
| **SVR-21e** | 迁移 `24_operation_port_matrix.sql` + `moli.sql` 基线合并；`production-checklist` §2 加注 DB 权威 | ✅ |

### P2+ —— 项目/组件多服务器（SVR-22，2026-07-11）

| 任务 | 内容 | 状态 |
|------|------|------|
| **SVR-22a** | 设计：主 `server_id` + N:N 双轨 · [`operation-server-links.md`](operation-server-links.md) | ✅ |
| **SVR-22b** | 项目 CRUD/VO `serverIds`；`GET/PUT /operation/project/{id}/links` | ✅ |
| **SVR-22c** | 组件对称 API + `OperationComponentLinkService` | ✅ |
| **SVR-22d** | meiling-ui 项目/组件列表「关联服务器」多选弹窗（S6-b / S6-b+） | ✅ |

### P2+ —— 服务器角色分类（SVR-23，2026-07-12）

| 任务 | 内容 | 状态 |
|------|------|------|
| **SVR-23a** | `operation_server_info.server_role` 列 + 索引 + 按名称回填 · [`26_operation_server_role.sql`](../sql/26_operation_server_role.sql) | ✅ |
| **SVR-23b** | 后端 VO/DTO/校验 + 列表 `serverRole` 筛选；新建默认 `app` | ✅ |
| **SVR-23c** | meiling-ui 服务器页角色筛选/列/表单 · `ServerRoleSelect` / `ServerRoleBadge` | ✅ |

### P2+ —— 服务器标签（SVR-24，2026-07-12）

| 任务 | 内容 | 状态 |
|------|------|------|
| **SVR-24a** | `operation_server_info.tags` JSON 列 · [`27_operation_server_tags.sql`](../sql/27_operation_server_tags.sql) | ✅ |
| **SVR-24b** | 后端 `tags[]` 校验 + 列表 `tag` 筛选 + `GET /tag-options` | ✅ |
| **SVR-24c** | meiling-ui `ServerTagsInput` / `ServerTagsBadges` + 列表筛选 | ✅ |

### P4 —— 服务器拓扑可视化（SVR-25~27，2026-07-12 设计）

> 设计文档：[`server-topology-visualization.md`](server-topology-visualization.md) · 架构图 `docs/diagrams/moli-operation-topology-graph.drawio`

| 任务 | 内容 | 状态 |
|------|------|------|
| **SVR-25a** | 后端 `GET /operation/topology` 全局聚合 API（三表 + 两 N:N，回退主 `server_id`） | ✅ |
| **SVR-25b** | meiling-ui `OperationTopologyGraphView`（ECharts force/circular + 环境/角色/标签筛选 + `ServerDetailModal` 联动） | ✅ |
| **SVR-25c** | 菜单 SQL `28_operation_topology_menu.sql`（perms 复用 `operation:server:list`） | ✅ · 已合并 `moli.sql` |
| **SVR-25d** | `RelationDrawer` 叠加 `deployRunning` / `portMatchStatus` / `recentTasks` | ✅ |
| **SVR-26a** | `operation_project_component` 表 + `component-links` API + 拓扑 `depends_on` 边 | ✅ |
| **SVR-26b** | 项目页 `OperationProjectComponentLinksModal` + 拓扑 `depends_on` 边 | ✅ |
| **SVR-27a/b** | SSH facts 白名单采集 / 探测历史曲线（远期） | ⬜ |

### P4 —— 关联关系导航与搜索（SVR-28，2026-07-12 设计）

> 设计文档：[`operation-relations-navigation.md`](operation-relations-navigation.md) · 架构图 `docs/diagrams/moli-operation-relations-nav.drawio`
> 列表内关系视角，与 SVR-25 拓扑图共用 `OperationRelationQuerySupport` 读取层

| 任务 | 内容 | 状态 |
|------|------|------|
| **SVR-28a** | 列表 VO 关系计数（GROUP BY 聚合）+ 项目/组件/服务器列表反向过滤参数 | ✅ |
| **SVR-28b** | 统一关系 API `GET /operation/relations/{type}/{id}`（servers/projects/components/recentTasks） | ✅ |
| **SVR-28c** | `RelationDrawer` + `OperationRelationChips` + URL 过滤 chip | ✅ |
| **SVR-28d** | 服务器页 `OperationServerRelationLinksModal`；旧 topology API 已移除 | ✅ |
| **SVR-28e** | `OperationEntityLink` · 部署中心/端口矩阵/任务历史/平台 → 同一抽屉 | ✅ |
| **SVR-28f** | 拓扑页实体搜索 + `?focus=s-{id}` 深链 | ✅ |

建议顺序：26a → 28a → 28b → 28c →（25a/b 并行）→ 28d/e/f

### 5.1 当前待办总览（2026-07-13）

| 类别 | 状态 | 内容 |
|------|------|------|
| **后端 API** | ✅ 主线完成 | 四台账、部署中心、端口矩阵、拓扑/关系读取层、component-links |
| **后端增强** | ⬜ 可选 | `deploy_running` 全量远程化；relations 分实体权限 |
| **后端 2026-07-13** | ✅ | `POST` project/component create 返回 `id`；`moli-service.sh` 扩展 order/ai |
| **前端 meiling-ui** | ✅ **主线 + P3** | W1–W10 · **DC-4** 分组 · 详 [`operation-frontend-handoff.md`](../api/operation-frontend-handoff.md) |
| **共享部署** | ☐ 待运维 | `origin/ci/kb-sync-multi-space-gate` 已 push（`b4ac176a`/`755abd43`）；共享 `:8888` 待 install+重启 |
| **联合验收** | ✅ W1–W10 | [operation-w1-w10-walkthrough.md](../test/operation-w1-w10-walkthrough.md)；拓扑关系验收可选 |
| **本地联调** | ⚠️ 常漏配 | `ops.upload/command/deploy.enabled` 默认 false；大文件勿经 Gateway；SSH + `OPS_SECRET_KEY` |
| **缺陷修复** | ✅ 2026-07-13 | 见 §5.2（关联计数虚高、`serverIp` 与 `serverId` 不一致报错） |

### 5.2 缺陷修复记录（2026-07-13 · SVR-22/28 关联）

| 现象 | 根因 | 修复 |
|------|------|------|
| 关联弹窗只选 1 台，列表/抽屉显示「服务器 2」 | 主表 `server_id` 与 N:N 不同步；计数合并两边 | `PUT .../links` 同步主表 `server_id`；`OperationRelationQuerySupport` N:N 非空时以 N:N 为准 |
| 保存关联报「serverIp 与 serverId 不一致」 | 换服务器后行内仍留旧 `server_ip` | `OperationServerBindingSupport`：**有 `serverId` 时以台账 IP 覆盖**，不再抛错 |
| 同名 `project_name` 多行误解为「一台项目两台机」 | 设计允许 dev/pro 各一条台账 | 以 **`project_id` / `server_id`** 区分，不按 `project_name` 合并计数 |

实现类：`OperationProjectLinkServiceImpl`、`OperationRelationQuerySupport`、`OperationServerBindingSupport`。设计细节：[`operation-server-links.md`](operation-server-links.md) §2.3。

---

## 6. Phase R · 台账 + 部署中心改造（2026-07-11 规划）

SVR-13~20 功能已上线，但 **Schema 漂移、组件缺 `server_id`、定时 `deploy_running` 仍走本机脚本** 等问题需在下一迭代收敛。

| Phase | 内容 | 状态 |
|-------|------|------|
| **R0** | 合并 `moli.sql` 与 21/22 迁移；Runbook 补部署中心三开关 | ✅ 种子密码 NULL · [`deploy/上线流程.md`](../deploy/上线流程.md) §7 |
| **R1** | 组件 `server_id`、N:N 唯一、删除级联 | ✅ |
| **R2** | Request DTO + 部署中心入参校验 + 10101–10109 | ✅ |
| **R3 P1** | `operation_task.project_id` + 部署 task 绑定校验 | ✅ |
| **R3.4** | 拓扑 `server_ip IN (ip, inner_ip)` | ✅ |
| **R4** | `OperationCrudSupport` / 测试补齐 | ✅ |

架构图：[`moli-operation-refactor.drawio`](../diagrams/moli-operation-refactor.drawio)

**建议起手**：R0 + R1（低风险）已完成；后续可选 **R3.2**（`deploy_running` 定时同步全走 SSH，弱化本机脚本依赖）。

### 6.1 仍开放的架构债（非阻塞）

| 项 | 说明 |
|----|------|
| `deploy_running` 本机回退 | `status-sync-mode=local` 时仍读本机 `moli-service.sh`；生产应 `ssh` |
| order/ai 远程启停 | YAML/registry 已登记；目标机 `moli-service.sh` 待扩展 |
| Gateway 大文件上传 | 经网关上传 multipart 易 `Failed to fetch`；dev 宜 vite proxy → `:8888` |
| `OperationHealthSupport` 抽取 | 重构计划中的可选收敛项 |

---

## 7. 表与权限增量（规划）

| 类型 | 增量 |
|------|------|
| 字段 | `operation_server_info` / `operation_component_deploy_info` 增 `status`、`last_check_time`（SVR-4）—— 迁移 `docs/sql/18_operation_health_columns.sql`；`operation_project_deploy_info` 增 `deploy_running`、`last_deploy_check_time`（SVR-12）—— 迁移 `docs/sql/20_operation_project_deploy_columns.sql`；**SVR-13~16** SSH 字段 + `operation_task` 表 —— 迁移 `docs/sql/21_operation_ssh_deploy.sql`；**SVR-19** `upload_allowed_roots` —— `docs/sql/22_operation_command_flex.sql` |
| 权限 | 新增 `operation:secret:view`（SVR-3）；`operation:deploy:exec`（SVR-8）；**`operation:ssh:manage`**（SVR-13）；**`operation:file:upload`**（SVR-16）；**`operation:command:exec`**（SVR-18）；菜单 **部署中心** id=405 |
| 配置 | `ops.deploy.enabled`（默认 false）、`OPS_DEPLOY_ROOT`；迁移 `docs/sql/19_operation_deploy_exec.sql`；`ops.health.probe-enabled` / `ops.health.probe-cron`（默认定时 15 分钟）；**`ops.upload.enabled`**（默认 false）、`ops.upload.allowed-paths`、`ops.upload.allow-any-under`；**`ops.command.enabled`**（默认 false）；**`OPS_SECRET_KEY`**（SSH/凭据加解密） |
| 加密 | 复用 `KB_LLM_CONFIG_SECRET` 思路，建议独立 `OPS_SECRET_KEY`，避免跨域共享密钥 |

---

## 8. 边界（不做）

- **不含知识库运维**（Sync / Lint / wiki / LLM 配置）—— 见 [`kb-ops-roadmap.md`](kb-ops-roadmap.md)。
- 不含业务监控大盘 / APM / 日志中心（平台级可观测性，另行规划，见 `docs/ops/monitoring-and-logs.md`）。
- 不含 CI/CD 编排（Jenkins/Actions 属发布流水线）。

---

## 9. 相关

- **改造方案**：[`operation-module-refactor-plan.md`](operation-module-refactor-plan.md)
- 表结构：`docs/sql/USER_CENTER_SCHEMA.md` §2.3、`scripts/moli.sql`
- API 地图：`docs/api/user-center-api-map.md` §4
- **前端对接**：[`docs/api/operation-frontend.md`](../api/operation-frontend.md)
- 加密参考实现：`kb-llm-platform-settings.md` §3.3、`KbLlmConfigCipher`
- 平台可观测性规划：`docs/ops/monitoring-and-logs.md`、`docs/zh-CN/TECH_STACK.md` §6
