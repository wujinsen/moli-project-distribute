# 茉莉微服务 · v1.0 发布范围说明

> **状态**：首版上线基线 · 更新：2026-06-28  
> **读者**：产品、研发、测试、运维  
> **冲突时**：以本文「交付边界」为准；各模块细节见下表链接。

---

## 1. 版本定位

**茉莉项目微服务 v1.0** 是一套可本地/内网部署的 **Spring Cloud 微服务演示平台**，交付：

- 统一网关 + 用户中心 RBAC + 多系统门户
- 订单秒杀压测链路（Redis Lua + 异步落库）
- 企业知识库（浏览 / 问答 / Ingest / Wiki 治理 / 三空间 Sync）
- BI 模块 **仅占位**（demo 接口，不纳入 v1 业务验收）

**非目标（v1 明确不做）**

- 生产级多租户 SaaS、OAuth2 开放平台
- 完整电商订单/支付/物流
- BI 报表与数据仓库
- 知识库 Meilisearch/向量检索（MySQL ngram 已够用）
- Wiki 治理前端 **T16f 全链路**（后端已就绪，UI 可部分交付，见 §3）

---

## 2. 服务清单与交付状态

| 服务 | Nacos 名 | 端口 | 网关前缀 | v1 交付 | 说明 |
|------|----------|------|----------|---------|------|
| **gateway** | `moli-gateway` | 21000 | — | ✅ | 四路由转发 + StripPrefix |
| **user-center** | `user-center-server` | 8888 | `/UserCenter/**` | ✅ | 登录/RBAC/门户/Dubbo |
| **order** | `order-server` | 8087 | `/OrderServer/**` | ✅ | **秒杀压测域**（非完整订单中心） |
| **knowledge** | `knowledge-server` | 8090 | `/KnowledgeServer/**` | ✅ | 知识库 REST + Ingest + 治理 API |
| **bi** | `bi-server` | 1128 | `/BiServer/**` | 🟡 占位 | 仅 `/demo/test`，不验收 |

---

## 3. 功能范围矩阵

### 3.1 用户中心（P0 · 全量交付）

| 能力 | v1 | 文档 |
|------|-----|------|
| 登录 / 登出 / Session（Redis 共享） | ✅ | [user-center-requirements.md](user-center-requirements.md) |
| RBAC（用户/角色/菜单/部门/动作） | ✅ | [RBAC.md](../zh-CN/RBAC.md) |
| 多系统门户 enter/switch | ✅ | [portal-system-group.md](../design/portal-system-group.md) |
| Dubbo 三接口 | ✅ | [user-center-dubbo.md](../api/user-center-dubbo.md) |
| 审计日志 | ✅ | PRD §3.5 |
| 压测 loadtest profile | ✅ | [load-test/README.md](../../load-test/README.md) |

### 3.2 网关（P0）

| 能力 | v1 | 文档 |
|------|-----|------|
| 四服务路由 + 负载均衡 | ✅ | [gateway-requirements.md](gateway-requirements.md) · [gateway-routes.md](../api/gateway-routes.md) |
| Authorization 头透传 | ✅ | [ARCHITECTURE.md](../zh-CN/ARCHITECTURE.md) |
| Sentinel / 限流 | 🟡 配置预留 | TECH_STACK |

### 3.3 订单 · 秒杀（P0 · 压测演示）

| 能力 | v1 | 文档 |
|------|-----|------|
| Redis Lua 原子扣库存 | ✅ | [order-seckill-design.md](../design/order-seckill-design.md) |
| 异步队列落 MySQL | ✅ | 同上 |
| k6 百万 QPS 压测脚本 | ✅ | [load-test/README.md](../../load-test/README.md) |
| 完整订单生命周期 | ❌ | v2+ |

### 3.4 知识库（P0 · 核心交付）

| 能力 | v1 后端 | v1 前端 | 文档 |
|------|---------|---------|------|
| 浏览 / 问答 / 搜索 | ✅ | ✅ | [KNOWLEDGE_API.md](../api/KNOWLEDGE_API.md) |
| 三空间 Sync | ✅ | ✅ | [wiki同步指南](../../moli-knowledge/kb/wiki-ops/guides/wiki同步指南.md) |
| Ingest 工作台 | ✅ | ⚠️ 部分 | [knowledge-workbench-requirements.md](knowledge-workbench-requirements.md) |
| Wiki 单页编辑 + AI | ✅ | ✅ | KNOWLEDGE_API §8 |
| Wiki 治理（lint/script/AI/auto） | ✅ | ⚠️ T16f 部分 | [wiki-govern-frontend.md](../api/wiki-govern-frontend.md) |
| 平台 LLM 设置 | ✅ | 🔵 T19d | [kb-llm-platform-settings.md](../design/kb-llm-platform-settings.md) |

### 3.5 BI（不纳入 v1 验收）

- 接口：`GET /BiServer/demo/test` → 字符串 `test success`
- 用途：验证网关路由与 Shiro 骨架；**不要求**产品/测试用例

---

## 4. 前端（meiling-ui）v1 菜单预期

> 前端仓库独立；以下为 v1 联调最小集。

| 模块 | 路由（示例） | v1 必验 |
|------|--------------|---------|
| 登录 / 首页 | `/login` | ✅ |
| 系统管理 | user/role/menu… | ✅ |
| 知识库浏览 | `knowledge/browse` | ✅ |
| 知识库问答 | `knowledge/ask` | ✅ |
| Ingest 工作台 | `knowledge/ingest/index` | ✅ Express + Expert |
| Wiki 编辑 | `knowledge/wiki/edit` | ✅ |
| Wiki 治理 | `knowledge/wiki-govern/index` | ⚠️ Lint + AI（script/auto 待补） |
| 健康体检 | `knowledge/lint/index` | ✅ |
| 秒杀压测 | 无专用页 | 经 k6 / curl |

---

## 5. 数据与基础设施

| 组件 | v1 要求 | 说明 |
|------|---------|------|
| MySQL `moli` | ✅ | [`scripts/moli.sql`](../../scripts/moli.sql) + 知识库/秒杀增量 |
| Redis db=1 | ✅ | Session + 秒杀热数据（order/user-center 一致） |
| Nacos `dev` | ✅ | 服务发现 |
| MinIO | 可选 | 知识库附件 |
| LLM API Key | 可选 | 无 Key 时 Ask/Ingest 降级 |

初始化：[`scripts/init-db.ps1`](../../scripts/init-db.ps1) · 发布：[`v1-release-runbook.md`](../ops/v1-release-runbook.md)

---

## 6. v1 验收入口

| 类型 | 文档 |
|------|------|
| **冒烟清单** | [release-smoke-checklist.md](../test/release-smoke-checklist.md) |
| **秒杀手测** | [order-seckill.md](../test/order-seckill.md) |
| **用户中心回归** | [user-center.md](../test/user-center.md) |
| **知识库 Ingest** | [knowledge-ingest-acceptance.md](../test/knowledge-ingest-acceptance.md) |
| **生产检查** | [production-checklist.md](../ops/production-checklist.md) |

---

## 7. 文档地图（v1 相关）

| 类型 | 路径 |
|------|------|
| PRD | 本文 · [user-center-requirements.md](user-center-requirements.md) · [knowledge-workbench-requirements.md](knowledge-workbench-requirements.md) |
| 设计 | [ARCHITECTURE.md](../zh-CN/ARCHITECTURE.md) · [order-seckill-design.md](../design/order-seckill-design.md) · [knowledge-module-overview.md](../design/knowledge-module-overview.md) · [gateway-design.md](../design/gateway-design.md) |
| API | [api/README.md](../api/README.md) |
| 测试 | [test/README.md](../test/README.md) · [knowledge-e2e-regression.md](../test/knowledge-e2e-regression.md) |
| 运维 | [v1-release-runbook.md](../ops/v1-release-runbook.md) · [sql-migration-order.md](../ops/sql-migration-order.md) |

---

## 8. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-06-28 | 首版发布范围初稿 |
