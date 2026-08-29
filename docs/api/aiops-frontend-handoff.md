# AIOps · meiling-ui 联调契约

> 前端：`meiling-ui` · 后端：`moli-aiops` FastAPI `:8099` · 鉴权：user-center Shiro

## 路由与代理

| 环境 | 浏览器前缀 | 实际服务 |
|------|------------|----------|
| dev (Vite) | `/AiOpsServer/*` | `http://127.0.0.1:8099/*`（StripPrefix） |
| prod (nginx) | `/AiOpsServer/*` | upstream `:8099` |
| 网关（可选） | `/AiOpsServer/**` | `application-dev.yml` aiops-route |

环境变量：

- `VITE_AIOPS_BASE_URL` — 默认空，即同源 `/AiOpsServer`
- `VITE_AIOPS_PROXY_TARGET` — Vite dev 代理目标，默认 `http://127.0.0.1:8099`

## 鉴权

**与 user-center 不同**：AIOps 返回 FastAPI 原生 JSON / SSE，**不是** `MoliResult` 信封。

1. 前端所有请求带 `Authorization: <Shiro sessionId>`（与 `http.ts` 相同，无 Bearer 前缀）
2. FastAPI `ShiroAuthMiddleware` 转发到 `GET {AIOPS_AUTH_VALIDATE_URL}`（默认 `http://127.0.0.1:8888/auth/capabilities`）
3. 按路由校验权限码：

| 接口 | 权限 |
|------|------|
| `GET /health` | `operation:aiops:list` |
| `GET /runs*` | `operation:aiops:list` |
| `POST /diagnose` | `operation:aiops:diagnose` |
| `POST /runs/{id}/approve|reject` | `operation:aiops:approve` |

独立演示页 `static/index.html` 可设 `AIOPS_AUTH_DISABLED=true` 跳过入站鉴权。

## 前端页面

| 菜单 | 路由 | 组件 |
|------|------|------|
| 故障诊断 | `/operation/aiops` | `AiopsDiagnosisView.vue` |
| 诊断历史 | `/operation/aiops-runs` | `AiopsRunsView.vue` |

菜单 SQL：`docs/sql/40_operation_aiops_menu.sql`

## API 摘要

详见 `moli-aiops/README.md`。前端封装：`meiling-ui/src/api/aiops.ts`

| Method | Path | 说明 |
|--------|------|------|
| GET | `/health` | 配置 chips |
| POST | `/diagnose` | 启动诊断 |
| GET | `/runs/{id}/stream` | SSE（fetch + ReadableStream） |
| GET | `/runs` | 历史列表 |
| GET | `/runs/{id}` | 详情 + 复盘 + trace |
| POST | `/runs/{id}/approve` | 人工批准 |
| POST | `/runs/{id}/reject` | 否决 |

## 与运维模块联动

| 入口 | 行为 |
|------|------|
| **服务器管理** 行操作「发起诊断」 | 跳转 `/operation/aiops?target={serverName}&title=...` |
| 诊断页目标下拉 | 优先运维 `GET /operation/server/list`，fallback inventory |

权限：行按钮需 `operation:aiops:diagnose`；列表页需 `operation:aiops:list`。

## 本地联调顺序

1. user-center `:8888` 已启动，执行 `40_operation_aiops_menu.sql`，重新登录
2. `moli-aiops`：`AIOPS_AUTH_VALIDATE_URL=http://127.0.0.1:8888/auth/capabilities`
3. `meiling-ui`：`npm run dev` → 运营管理 → 故障诊断
