# 订单 · 秒杀手测说明

> API 契约：[order-seckill-api.md](../api/order-seckill-api.md)  
> 设计：[order-seckill-design.md](../design/order-seckill-design.md)  
> 压测：[load-test/README.md](../../load-test/README.md)

模块 **无 JUnit 自动化测试**；v1 以本手测 + k6 压测验收。

---

## 1. 前置

| 项 | 说明 |
|----|------|
| DB | `02_seckill_schema.sql` 已执行，活动 id=1 存在 |
| Redis | localhost:16379，password 与 order `application-dev.yml` 一致，**database=1** |
| 服务 | order-server :8087；可选 gateway :21000 |
| Profile | 日常 `dev`；初始化活动用 `loadtest` |

---

## 2. 用例 1 · 连通性

```bash
curl http://127.0.0.1:21000/OrderServer/seckill/ping
```

期望：`code=200`，`data.pong=true`。

---

## 3. 用例 2 · 查询活动

```bash
curl http://127.0.0.1:21000/OrderServer/seckill/activity/1
```

期望：`activityId=1`，`stock` 为 Redis 当前值（首次可能与 DB 种子一致）。

---

## 4. 用例 3 · 成功下单

```bash
curl -X POST http://127.0.0.1:21000/OrderServer/seckill/order \
  -H "Content-Type: application/json" \
  -d '{"activityId":1,"userId":"smoke-u1","requestId":"smoke-req-001"}'
```

期望：

- `code=200`
- `data.status=SUCCESS`
- `data.orderId` 非空
- `data.remainStock` 比下单前减 1

---

## 5. 用例 4 · 重复抢购

同一 `userId` 再次 POST `/order`。

期望：`code=429`，`msg` 含 duplicate。

---

## 6. 用例 5 · 异步落库

等待 1–2 秒后查 MySQL：

```sql
SELECT * FROM seckill_order WHERE user_id = 'smoke-u1' ORDER BY create_time DESC LIMIT 5;
```

期望：至少 1 条成功记录。

---

## 7. 用例 6 · 库存耗尽（可选）

高并发或调小 stock 后连续下单直至：

期望：`code=409`，`status=SOLD_OUT`。

---

## 8. loadtest · 初始化活动

启动 order：`spring.profiles.active=loadtest`

```bash
curl -X POST "http://127.0.0.1:8087/seckill/admin/init?activityId=1&stock=1000000&name=smoke-test"
```

非 loadtest profile 期望 `403`。

---

## 9. metrics

```bash
curl http://127.0.0.1:21000/OrderServer/seckill/metrics
```

压测后查看 success / sold_out / duplicate 计数。

---

## 10. 纳入发布冒烟

本清单用例 1–5 已并入 [release-smoke-checklist.md](release-smoke-checklist.md) §3。

---

## 11. 相关

- 模块 README：[moli-order/README.md](../../moli-order/README.md)
- DDL：[02_seckill_schema.sql](../sql/02_seckill_schema.sql)
