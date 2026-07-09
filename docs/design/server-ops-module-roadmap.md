# 服务器运维模块 · 演进规划（技术端运维）

> 更新：2026-07-09 · 状态：**P0 安全已落地**（SVR-1/2/3）；**P1 可观测已落地**（SVR-4/5/6）；**P2 联动已落地**（SVR-7/8 + 驾驶舱统计）
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

均为 **Mapper 直连 CRUD**（无 Service 层），列表支持按名称 like + environment 过滤 + 分页。

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
| **SVR-6** | 前端「运维管理」页展示状态灯 + 行内探测 + 拓扑弹窗（meiling-ui） | ✅ 2026-07-09 |

### P2 —— 联动与自动化（按需）

| 任务 | 内容 |
|------|------|
| **SVR-7** | 部署信息与 `docs/ops/production-checklist.md` 端口矩阵对齐校验；`GET /operation/audit/port-matrix`；组件列表带 `portMatchStatus` | ✅ 2026-07-09 |
| **SVR-8** | 只读查询 `deploy/linux/moli-service.sh status`；`POST .../{action}` 变更动作需 `ops.deploy.enabled=true` + `operation:deploy:exec` | ✅ 2026-07-09 |
| **SVR-9** | 驾驶舱 ops 页 KPI 接 `GET /operation/stats` 真实台账计数 | ✅ 2026-07-09 |

---

## 6. 表与权限增量（规划）

| 类型 | 增量 |
|------|------|
| 字段 | `operation_server_info` / `operation_component_deploy_info` 增 `status`、`last_check_time`（SVR-4）—— 迁移 `docs/sql/18_operation_health_columns.sql` |
| 权限 | 新增 `operation:secret:view`（SVR-3）；`operation:deploy:exec`（SVR-8 变更动作）；探测/拓扑/审计沿用 `*:list` |
| 配置 | `ops.deploy.enabled`（默认 false）、`OPS_DEPLOY_ROOT`；迁移 `docs/sql/19_operation_deploy_exec.sql` |
| 加密 | 复用 `KB_LLM_CONFIG_SECRET` 思路，建议独立 `OPS_SECRET_KEY`，避免跨域共享密钥 |

---

## 7. 边界（不做）

- **不含知识库运维**（Sync / Lint / wiki / LLM 配置）—— 见 [`kb-ops-roadmap.md`](kb-ops-roadmap.md)。
- 不含业务监控大盘 / APM / 日志中心（平台级可观测性，另行规划，见 `docs/ops/monitoring-and-logs.md`）。
- 不含 CI/CD 编排（Jenkins/Actions 属发布流水线）。

---

## 8. 相关

- 表结构：`docs/sql/USER_CENTER_SCHEMA.md` §2.3、`scripts/moli.sql`
- API 地图：`docs/api/user-center-api-map.md` §4
- 加密参考实现：`kb-llm-platform-settings.md` §3.3、`KbLlmConfigCipher`
- 平台可观测性规划：`docs/ops/monitoring-and-logs.md`、`docs/zh-CN/TECH_STACK.md` §6
