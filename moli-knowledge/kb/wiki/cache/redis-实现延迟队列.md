---
title: Redis 实现延迟队列
slug: redis-实现延迟队列
type: article
status: active
tags: [Redis, 延迟队列, ZSet]
sources:
- raw/wujinsen_markdown/面试笔试/高级java/缓存更新的套路.note.md
related: [延迟消息与队列, redis-数据结构与使用场景]
created: 2026-06-22
updated: 2026-07-05
---

# Redis 实现延迟队列

> 概念 [[cache/延迟消息与队列]]；ZSet 结构 [[cache/redis-数据结构与使用场景]]。

## 1. ZSet 方案

```
ZADD delay_queue <execute_timestamp> <task_payload>
```

消费者循环：

```text
ZRANGEBYSCORE delay_queue 0 now LIMIT 0 1
→ 取到则 ZREM + 执行业务（Lua 保证原子）
```

## 2. List + 轮询（简单）

`BRPOP` 阻塞队列，生产者 `LPUSH` 时已算好延迟 → 需 sleep 或分层队列，精度差。

## 3. 注意点

| 问题 | 处理 |
|------|------|
| 重复消费 | 业务幂等 [[middleware/接口幂等性实践]] |
| 丢失 | 持久化 AOF [[cache/redis-持久化与高可用]] |
| 集群 | key 单 slot；大流量用 MQ |

## 4. 与秒杀

未支付订单超时释放库存：ZSet 存 `(orderId, payDeadline)`，扫单线程关单并回滚 Redis 库存。

## 相关

[[spring/spring-async与线程池]] · [[middleware/消息队列]]
