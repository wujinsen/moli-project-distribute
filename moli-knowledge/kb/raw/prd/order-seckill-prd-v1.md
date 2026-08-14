# 订单 · 秒杀压测 · PRD v1（raw 投喂副本）

> **工程索引（权威导航）**：[`docs/product/order-seckill-requirements.md`](../../../docs/product/order-seckill-requirements.md)  
> **概要设计**：[`docs/design/order-seckill-design.md`](../../../docs/design/order-seckill-design.md)  
> **状态**：v1 已交付 · 2026-06-20

---

## 1. 背景

v1 需要一条 **可压测、可演示** 的业务链路，验证网关 → 业务 → Redis → MySQL 全链路，**不是**完整电商订单中心。

## 2. 目标

| 目标 | 指标 |
|------|------|
| 高并发扣库存 | Redis Lua 原子，无超卖 |
| 削峰 | 异步队列落 MySQL |
| 压测 | k6 经 gateway 线性扩展 RPS |

## 3. 功能范围（v1）

### Must

- 活动查询 `GET /seckill/activity/{id}`
- 秒杀下单 `POST /seckill/order`
- 指标 `GET /seckill/metrics`
- loadtest profile 下 `admin/init` 初始化

### Won't（v1）

- 支付、退款、物流
- 购物车、普通订单 CRUD

## 4. 用户故事

| 角色 | 场景 |
|------|------|
| 压测工程师 | k6 打 `/OrderServer/seckill/order` |
| 开发 | 本地 smoke 下单 SUCCESS |
| 访客 | ping 无需登录 |

## 5. 验收

- [order-seckill.md](../../../docs/test/order-seckill.md)
- [load-test/README.md](../../../../load-test/README.md)

## 6. 关联

- [order-seckill-api.md](../../../docs/api/order-seckill-api.md)
- [moli-order/README.md](../../../../moli-order/README.md)
