# 茉莉 API 网关 · PRD v1（raw 投喂副本）

> **工程索引（权威导航）**：[`docs/product/gateway-requirements.md`](../../../docs/product/gateway-requirements.md)  
> **概要设计**：[`docs/design/gateway-design.md`](../../../docs/design/gateway-design.md)  
> **状态**：v1 已交付 · 2026-06-20

---

## 1. 背景

茉莉微服务需要 **单一 HTTP 入口**，避免前端/压测记住多个端口。网关负责路由与负载均衡，**不做业务鉴权**。

## 2. 目标用户

| 角色 | 诉求 |
|------|------|
| 前端 | 只配 `VITE_API_BASE_URL` 一个 origin |
| 压测 | k6 统一打 `:21000` |
| 运维 | 对外只暴露 21000 |

## 3. 功能范围（v1）

### 3.1 四路由（Must）

- `/UserCenter/**` → user-center-server
- `/OrderServer/**` → order-server
- `/AiServer/**` → ai-server
- `/KnowledgeServer/**` → knowledge-server

每条路由 StripPrefix=1，下游收到无前缀路径。

### 3.2 透传（Must）

- 请求头 `Authorization` 原样转发
- 支持 Nacos `lb://` 负载均衡

### 3.3 非目标（v1 Won't）

- 网关签发 token
- API 聚合文档
- 多租户路由

## 4. 验收

- [gateway-smoke.md](../../../docs/test/gateway-smoke.md) 全绿
- [release-smoke-checklist.md](../../../docs/test/release-smoke-checklist.md) 网关段

## 5. 关联文档

- [gateway-routes.md](../../../docs/api/gateway-routes.md)
- [moli-gateway/README.md](../../../../moli-gateway/README.md)
