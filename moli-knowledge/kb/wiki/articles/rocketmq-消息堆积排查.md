---
title: RocketMQ 消息堆积排查
slug: rocketmq-消息堆积排查
type: article
status: active
tags: [RocketMQ, MQ, 排查]
sources:
 - raw/wujinsen_markdown/
related: [rocketmq-架构与实战, 延迟消息与队列, 故障排查指南]
created: 2026-06-21
updated: 2026-06-21
---

# RocketMQ 消息堆积排查

> 架构 [[rocketmq-架构与实战]]；延迟 [[延迟消息与队列]]；总指南。

## 1. 现象

- 控制台 **Consumer Lag** 持续上升
- 消费 TPS << 生产 TPS
- 业务延迟（秒杀落库、通知）

## 2. 根因矩阵

| 类型 | 检查 |
|------|------|
| 消费慢 | DB 慢、锁竞争、单线程消费 |
| 消费挂 | 异常未 ack、Rebalance 抖动 |
| 生产暴增 | 秒杀/补偿任务未限流 |
| 下游不可用 | 消费里 RPC 超时重试 |

## 3. 处置

1. 扩容 Consumer 实例（同 Group）
2. 提高 `consumeThreadMin/Max`（评估 DB 连接）
3. 批量消费 + 幂等 [[接口幂等性实践]]
4. 临时 **跳过非核心 Topic**，保秒杀主链路

## 4. 预防

- 消费逻辑无长事务；异步写库
- 监控 Lag 告警 [[prometheus-告警规则设计]]
- 死信队列 + 人工补偿

## 相关

[[kafka-与-mq选型]] · [[rocketmq-事务消息实践]]
