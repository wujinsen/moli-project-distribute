# moli-order · 订单服务

茉莉微服务 **订单模块**；v1 **仅交付秒杀压测域**（Redis Lua + 异步落库），非完整电商订单中心。

## 模块构成

| 子模块 | 职责 |
|--------|------|
| `moli-order-server` | HTTP API、秒杀 Lua、异步 Consumer |

## 运行时

| 项 | 值 |
|----|----|
| Nacos 服务名 | `order-server` |
| HTTP 端口 | **8087** |
| Dubbo 端口 | **20882**（`version=1.0.0`, `group=moli`） |
| 网关路由 | `/OrderServer/**` → StripPrefix=1 |
| Swagger | `http://localhost:8087/swagger-ui.html`（经网关：`/OrderServer/swagger-ui.html`） |

## 功能域（v1）

| 域 | 路径前缀 | 说明 |
|----|----------|------|
| 秒杀 | `/seckill` | ping、活动查询、下单、metrics、loadtest 初始化 |

## 依赖

- **Redis** — 库存/队列/幂等（**database=1**，与 user-center 一致）
- **MySQL** — `seckill_activity`、`seckill_order`（[`docs/sql/02_seckill_schema.sql`](../docs/sql/02_seckill_schema.sql)）
- **Nacos** — 服务注册
- **user-center** — 压测场景可选 Session（秒杀接口 v1 不强制登录）

## 本地启动

```bash
cd moli-distribute-parent && mvn clean install -DskipTests
cd ../moli-distribute-common && mvn clean install -DskipTests
cd ../moli-user-center && mvn clean install -DskipTests   # Shiro Starter 依赖
cd ../moli-order/moli-order-server
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

压测：`profiles=loadtest` · 详见 [`load-test/README.md`](../load-test/README.md)

## 文档索引

| 类型 | 路径 |
|------|------|
| **v1 范围** | [`docs/product/moli-v1-release-scope.md`](../docs/product/moli-v1-release-scope.md) §3.3 |
| **PRD** | [`docs/product/order-seckill-requirements.md`](../docs/product/order-seckill-requirements.md) |
| **设计** | [`docs/design/order-seckill-design.md`](../docs/design/order-seckill-design.md) |
| **API** | [`docs/api/order-seckill-api.md`](../docs/api/order-seckill-api.md) |
| **手测** | [`docs/test/order-seckill.md`](../docs/test/order-seckill.md) |
| **压测** | [`load-test/README.md`](../load-test/README.md) |
| **架构图** | [`docs/diagrams/moli-seckill-flow.drawio`](../docs/diagrams/moli-seckill-flow.drawio) |
| **网关** | [`docs/api/gateway-routes.md`](../docs/api/gateway-routes.md) |
| **启动** | [`kb/wiki-moli/guides/本地启动指南.md`](../moli-knowledge/kb/wiki-moli/guides/本地启动指南.md) |

## 测试

v1 无模块内 JUnit；验收见 [`docs/test/order-seckill.md`](../docs/test/order-seckill.md) 与 k6 脚本。
