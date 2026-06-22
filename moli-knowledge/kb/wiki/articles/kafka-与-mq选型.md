---
title: Kafka 与 MQ 选型
slug: kafka-与-mq选型
type: article
status: active
tags: [kafka, rocketmq, rabbitmq, 选型]
sources:
  - raw/wujinsen_markdown/面试笔试/高并发架构系列：Kafka、RocketMQ、RabbitMQ的优劣势比较.note.md
  - raw/wujinsen_markdown/面试笔试/kafka/精尽 Kafka 面试题（最新更新时间：2019-12-14）.note.md
related: [消息队列, 秒杀设计, elasticsearch-搜索]
created: 2026-06-22
updated: 2026-06-22
---

# Kafka 与 MQ 选型

> 枢纽 [[消息队列]]；茉莉秒杀 [[秒杀设计]]。

## 1. 吞吐量与时延（量级印象）

| 中间件 | 单机吞吐 | 时延 | 典型场景 |
|--------|----------|------|----------|
| **Kafka** | 百万级 TPS 写 | ms | 日志、大数据、埋点 |
| **RocketMQ** | 十万级 | ms | 电商订单、交易、阿里系 |
| **RabbitMQ** | 万级 | ms | 企业集成、复杂路由 |
| **ActiveMQ** | 万级 | ms | 老系统，维护减少 |

## 2. Kafka

**优点**

- 吞吐极高，分区横向扩展
- 分布式副本，高可用
- Pull 消费，顺序（分区内）
- 日志领域成熟

**缺点**

- 分区过多 load 升高
- 消费失败重试需自研（旧版）
- 功能相对「管道化」，非完整 MQ 全家桶

**适合**：日志采集、监控、大数据管道、与 [[elasticsearch-搜索]] 联动。

## 3. RocketMQ

**优点**

- Java 源码，国内电商验证
- 十万 TPS、万亿堆积不降性能（官方宣称）
- 事务消息、定时消息等 MQ 特性较全

**缺点**

- 多语言客户端少
- 社区相对 Kafka 小

**适合**：**订单、秒杀异步落库**（[[秒杀设计]] 演进首选之一）、金融级消息。

## 4. RabbitMQ

**优点**

- AMQP 标准，路由模型丰富（Exchange）
- 管理界面友好，功能完备

**缺点**

- Erlang 实现，二次开发难
- 吞吐低于 Kafka/RocketMQ

**适合**：复杂路由、中小吞吐、企业集成。

## 5. 选型建议（摘要）

| 需求 | 倾向 |
|------|------|
| 日志/大数据/ELK | **Kafka** |
| 电商订单/秒杀削峰 | **RocketMQ** |
| 复杂路由/中小规模 | **RabbitMQ** |
| 茉莉秒杀从 Redis 队列升级 | **RocketMQ** 或 Kafka（看团队栈） |

## 6. Kafka 为什么快？（面试常问）

- **顺序写磁盘** + Page Cache
- **零拷贝** sendfile
- **批量**压缩与发送
- **分区并行**

## 7. 消息可靠性（通用）

生产端：ack 级别（Kafka `acks=all`）、重试  
消费端：手动 commit、幂等消费、死信队列  
服务端：副本同步、持久化

Redis List 队列（茉莉现状）缺持久化与标准 ACK，宕机可能丢消息。

## 8. 茉莉落地 checklist

若替换 [[秒杀设计]] 中 Redis 队列：

1. 定义 topic + 消费者组
2. 消息体含 activityId、userId、orderId（幂等键）
3. 消费失败 → 重试 N 次 → 死信
4. 监控堆积 lag
5. 压测对比 Redis 队列与 MQ 延迟

入口仍建议 [[sentinel-限流与熔断]]，MQ 只解决**写库削峰**。
