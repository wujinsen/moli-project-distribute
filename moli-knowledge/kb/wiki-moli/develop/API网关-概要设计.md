---


title: API 网关 · 概要设计
slug: API网关-概要设计
type: concept
status: active
tags: [设计, 概要设计, 架构]
sources:
  - docs/design/gateway-design.md
related:
  - 网关
  - 服务调用与架构
  - 技术方案与架构索引
created: 2026-07-06
updated: 2026-09-02
---

> **浏览镜像**：工程契约权威仍在 `docs/design/gateway-design.md`；改设计请先改契约再重新运行本脚本或 Tab3 导入。

> 模块：`moli-gateway` · 端口 **21000**  
> 路由契约：`docs/api/gateway-routes.md`  
> 全链路：`docs/zh-CN/ARCHITECTURE.md`

---

## 1. 定位

`moli-gateway` 是茉莉 v1 的 **唯一对外 HTTP 入口**（meiling-ui / curl / k6 均经此访问业务服务）。**网关路由和限流这块架构**：路径路由 / `lb://` 负载见 §3，限流规划见 §6（v1 网关未硬编码规则，预留 Sentinel）。详细路由表见 [[茉莉-网关路由规范]]。

| 做 | 不做 |
|----|------|
| 路径路由、负载均衡（Nacos `lb://`） | 业务鉴权（Shiro 在业务服务） |
| 透传 `Authorization` 等请求头 | 签发 Session |
| 统一端口 21000 | 业务逻辑 |

---

## 2. 部署位置

![部署拓扑](../../../../docs/diagrams/png/moli-deploy-topology.png)

> 可编辑源文件：[moli-deploy-topology.drawio](../../../../docs/diagrams/moli-deploy-topology.drawio)

<details>
<summary>ASCII 备查</summary>

```
Browser / k6
     │
     ▼
moli-gateway :21000
     ├── /UserCenter/**    → user-center-server :8888
     ├── /OrderServer/**   → order-server :8087
     ├── /AiServer/**      → ai-server :1128
     └── /KnowledgeServer/** → knowledge-server :8090
```

</details>

**启动顺序**：业务服务注册 Nacos 后 **再启 gateway**，避免冷启动 503。

---

## 3. 路由设计

![网关路由](../../../../docs/diagrams/png/moli-gateway-routes.png)

> 可编辑源文件：[moli-gateway-routes.drawio](../../../../docs/diagrams/moli-gateway-routes.drawio)

| 配置项 | 值 |
|--------|-----|
| 实现 | Spring Cloud Gateway |
| 发现 | `spring.cloud.gateway.discovery.locator.lower-case-service-id=true` |
| 负载 | `uri: lb://{nacos-service-name}` |
| 前缀剥离 | `StripPrefix=1` |

**StripPrefix 语义**：去掉路径第一段再转发。

- 请求 `GET /UserCenter/login` → 下游 `GET /login`
- 请求 `POST /KnowledgeServer/kb/ask` → 下游 `POST /kb/ask`

完整表见 `docs/api/gateway-routes.md`。

---

## 4. 鉴权与 Session

网关 **不解析** token，只转发：

```
Authorization: <sessionId>
```

下游 user-center / order / knowledge 通过 **Shiro + 共享 Redis Session** 校验。时序见 `docs/diagrams/moli-auth-flow.drawio`。

登录唯一入口：`POST /UserCenter/login`。

---

## 5. Profile

| Profile | 用途 |
|---------|------|
| `dev` | 本地四路由（`application-dev.yml`） |
| `loadtest` | 压测；配合各服务 loadtest profile |

---

## 6. 限流与 Sentinel

- 技术栈预留 **Sentinel**（见 `docs/zh-CN/TECH_STACK.md`）
- v1 gateway **未**硬编码限流规则；压测限流在 k6 / 下游 Redis Lua 层
- 生产建议在网关或 Nginx 加 QPS / IP 限制

---

## 7. CORS

- 开发：meiling-ui dev server 跨域需 gateway 或前端 proxy 配置
- 生产：Nginx 反代同一域名，见 `moli-knowledge/kb/wiki-moli/ops/nginx反向代理与前端部署指南.md`

---

## 8. 优雅停机

`application.yml`：

- `server.shutdown: graceful`
- `spring.lifecycle.timeout-per-shutdown-phase: 30s`

---

## 9. 故障模式

| 现象 | 原因 | 处理 |
|------|------|------|
| 503 Service Unavailable | 下游未注册 Nacos | 先启 user-center 等 |
| 404 | 路径前缀错误 | 检查 `/UserCenter` 大小写 |
| token 失效 | 网关正常；查 Redis / user-center | 见故障排查指南 |
| 秒杀无鉴权暴露 | 设计如此 | 生产加网络隔离 |

---

## 10. 相关

- 模块 README：`moli-gateway/README.md`
- 冒烟：`docs/test/release-smoke-checklist.md` §1
- v1 范围：`docs/product/moli-v1-release-scope.md`