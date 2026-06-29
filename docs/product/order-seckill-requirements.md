# 订单 · 秒杀压测 · 需求说明

> **v1 定位**：[moli-v1-release-scope.md](moli-v1-release-scope.md) §3.3  
> **非完整订单中心** — 仅演示高并发秒杀 + 压测链路  
> 模块：`moli-order` · 更新：2026-06-28

---

## 1. 背景与目标

茉莉 v1 需要一条 **可压测、可演示** 的业务链路，验证：

- 网关 → order 转发
- Redis Lua 原子扣库存
- 异步落 MySQL（削峰）
- k6 百万 QPS 方案（多节点水平扩展）

**目标用户**：架构学习、压测演练、面试/demo 展示。

**非目标**

- 支付、物流、订单状态机、退款
- 购物车、普通下单
- 生产级 MQ / 分库分表

---

## 2. 用户故事

| 角色 | 场景 | 验收 |
|------|------|------|
| 压测工程师 | k6 经 gateway 打 `/seckill/order` | RPS 可线性扩展；409/429 语义正确 |
| 开发 | 本地 smoke 下单 | SUCCESS + DB 有记录 |
| 运维 | loadtest 初始化活动库存 | `admin/init` 仅 loadtest profile |
| 访客 | ping 探测 | 无需登录返回 pong |

---

## 3. 功能需求

### 3.1 活动查询（P0）

- `GET /seckill/activity/{id}` 读 Redis 热数据
- 返回 stock、sold、status
- 活动不存在 → 404

### 3.2 秒杀下单（P0）

- `POST /seckill/order`：body 含 `activityId`、`userId`、可选 `requestId`
- **Lua 原子**：扣库存 + 用户去重 + 入队
- 结果：`SUCCESS` | `SOLD_OUT` | `DUPLICATE` | `INVALID_PARAM` | `ACTIVITY_NOT_FOUND`
- HTTP 业务码：409 售罄、429 重复

### 3.3 异步落库（P0）

- Redis List 队列 → 定时 Consumer 批量 INSERT `seckill_order`
- 配置 `moli.seckill.async-db=true`（默认）

### 3.4 压测辅助（P1）

- `GET /seckill/ping` — 连通性
- `GET /seckill/metrics` — Redis 计数
- `POST /seckill/admin/init` — **仅 loadtest** 重置库存

### 3.5 鉴权（v1 约定）

- 秒杀接口 **不强制登录**（便于纯 RPS 压测）
- 生产环境应在网关层加 IP 限流 / 内网隔离

---

## 4. 数据需求

| 存储 | 内容 |
|------|------|
| MySQL | `seckill_activity`、`seckill_order`（[`02_seckill_schema.sql`](../sql/02_seckill_schema.sql)） |
| Redis db=1 | stock / user / queue / metrics |

默认活动 id=**1**，stock=1_000_000（种子 SQL）。

---

## 5. 非功能需求

| 项 | 要求 |
|----|------|
| 端口 | 8087；网关 `/OrderServer/**` |
| Redis | 与 user-center **同 database** |
| 幂等 | 同一 userId 同一活动仅成功一次 |
| 性能 | 同步路径仅 Redis；MySQL 异步 |

---

## 6. 验收

| 类型 | 文档 |
|------|------|
| 手测 | [order-seckill.md](../test/order-seckill.md) |
| 冒烟 | [release-smoke-checklist.md](../test/release-smoke-checklist.md) §3 |
| 压测 | [load-test/README.md](../../load-test/README.md) |
| API | [order-seckill-api.md](../api/order-seckill-api.md) |
| 设计 | [order-seckill-design.md](../design/order-seckill-design.md) |

---

## 7. 里程碑

| 阶段 | 内容 | v1 |
|------|------|-----|
| M1 | Lua + 同步 API | ✅ |
| M2 | 异步 Consumer + DDL | ✅ |
| M3 | k6 脚本 + loadtest profile | ✅ |
| M4 | 完整订单域 | 📋 v2+ |
