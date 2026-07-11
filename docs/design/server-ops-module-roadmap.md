# 服务器运维模块 · 演进规划（技术端运维）

> 更新：2026-07-11 · 状态：**P0 安全已落地**（SVR-1/2/3）；**P1 可观测已落地**（SVR-4/5/6）；**P2 联动已落地**（SVR-7/8 + 驾驶舱统计）；**P3 部署中心已落地**（SVR-13~20）
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
| `operation_server_info` | 服务器 | `server_name`, `ip`, `inner_ip`, `port`, `environment` |
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
| **SVR-5** | **级联视图**：`GET /operation/server/{id}/topology` 返回该服务器上的项目 + 组件（聚合 N:N + server_id/server_ip 回退） | ✅ 2026-07-09 |
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
| **SVR-21d** | meiling-ui 管理页 `operation/port-matrix/index`（用户方） | ⬜ |
| **SVR-21e** | 迁移 `24_operation_port_matrix.sql` + `moli.sql` 基线合并；`production-checklist` §2 加注 DB 权威 | ✅ |

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

**建议起手**：R0 + R1（低风险、修 `Unknown column` 类问题），再 R3.2（部署状态远程化）。

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
