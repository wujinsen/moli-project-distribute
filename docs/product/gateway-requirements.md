# 网关 · 需求说明（v1 工程索引）

> **v1 发布范围**：[moli-v1-release-scope.md](moli-v1-release-scope.md) §3.2  
> **模块**：`moli-gateway` · 服务名 `moli-gateway` · 端口 **21000** · 更新：2026-06-20  
> **raw 原文**：[kb/raw/prd/gateway-prd-v1.md](../../moli-knowledge/kb/raw/prd/gateway-prd-v1.md)

本文是 **工程侧需求导航**（验收口径 + 文档链）。

---

## 1. 产品目标（摘要）

网关是茉莉 v1 的 **唯一对外 HTTP 入口**：

- 浏览器 / meiling-ui / curl / k6 **只访问 :21000**
- 按路径前缀转发至 user-center / order / bi / knowledge
- **透传** `Authorization`；鉴权在下游 Shiro

**v1 不做**：网关层 JWT 签发、WAF、灰度路由、多集群联邦。

---

## 2. 功能清单与验收

### 2.1 路由转发（P0）

| 前缀 | 目标 | 验收 |
|------|------|------|
| `/UserCenter/**` | `lb://user-center-server` | StripPrefix=1；下游收到 `/login` 等 |
| `/OrderServer/**` | `lb://order-server` | 秒杀经网关可下单 |
| `/AiServer/**` | `lb://ai-server` | `/demo/test` 200 |
| `/KnowledgeServer/**` | `lb://knowledge-server` | `/kb/index` 200 |

**契约**：[gateway-routes.md](../api/gateway-routes.md) · **冒烟**：[gateway-smoke.md](../test/gateway-smoke.md)

### 2.2 请求头透传（P0）

| 需求 | 验收 |
|------|------|
| `Authorization` | 登录后 order/bi/knowledge 同 token 可访问 |
| 客户端 IP | 可选 `X-Forwarded-For`（若配置） |

### 2.3 服务发现（P0）

| 需求 | 验收 |
|------|------|
| Nacos 注册 | 四下游在 Nacos 可见时 gateway 路由成功 |
| 冷启动 | 先启业务再启 gateway，避免 503 |

### 2.4 Sentinel / 限流（P1 · 预留）

| 需求 | v1 |
|------|-----|
| Sentinel 依赖 | 🟡 已引入，规则可按环境配置 |
| 网关统一限流 | 可选，非 v1 必验收 |

---

## 3. 非功能

| 项 | 目标 |
|----|------|
| 延迟 | 转发开销可忽略（本地 <5ms） |
| 可用性 | 单实例 demo；生产需多副本 + LB |

---

## 4. 文档链

| 类型 | 链接 |
|------|------|
| 概要设计 | [gateway-design.md](../design/gateway-design.md) |
| 架构 | [ARCHITECTURE.md](../zh-CN/ARCHITECTURE.md) |
| 模块 README | [moli-gateway/README.md](../../moli-gateway/README.md) |
| wiki 服务页 | [kb/wiki-moli/develop/网关.md](../../moli-knowledge/kb/wiki-moli/develop/网关.md) |
| wiki 产品摘要 | [kb/wiki-moli/product/网关产品说明.md](../../moli-knowledge/kb/wiki-moli/product/网关产品说明.md) |

---

## 5. v2+ 展望

- 统一 OpenAPI 聚合 / Swagger 经网关
- 灰度、金丝雀、按租户路由
- 网关层 OAuth2 / API Key（若开放平台）
