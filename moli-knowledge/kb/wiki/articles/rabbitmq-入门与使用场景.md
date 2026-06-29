---
title: RabbitMQ 入门与使用场景
slug: rabbitmq-入门与使用场景
type: article
status: active
tags: [RabbitMQ, MQ, AMQP]
sources:
 - raw/wujinsen_markdown/架构/消息队列/RabbitMQ/RabbitMQ安装教程.note.md
 - raw/wujinsen_markdown/架构/消息队列/RabbitMQ/rabbitmq私信队列.note.md
related: [消息队列, kafka-与-mq选型, rocketmq-架构与实战]
created: 2026-06-22
updated: 2026-06-22
---

# RabbitMQ 入门与使用场景

> 三 MQ 对比 [[kafka-与-mq选型]]；常见未默认引入 RabbitMQ。

## 1. 核心模型（AMQP）

```
Producer → Exchange → (Binding) → Queue → Consumer
```

| 概念 | 说明 |
|------|------|
| Exchange | direct / fanout / topic / headers |
| Queue | 存储消息 |
| Binding | 路由规则 + routing key |
| ACK | 手动 ack 防丢；nack 重入队 |

## 2. 典型场景

| 场景 | Exchange 类型 |
|------|---------------|
| 点对点任务 | direct |
| 广播通知 | fanout |
| 日志路由 | topic `order.*` |
| 延迟/死信 | DLX + TTL |

## 3. 与 Kafka/RocketMQ

- RabbitMQ：**低延迟、复杂路由**，吞吐量低于 Kafka
- 若上 MQ，业务消息偏 **RocketMQ**（阿里系、事务消息）；日志/大数据偏 Kafka

## 4. 本地试装

管理台默认 `15672`；注意 **内存/磁盘告警** 会 block producer。

## 相关

[[消息队列]] · [[rocketmq-架构与实战]]
