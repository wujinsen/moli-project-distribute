---
title: RocketMQ 架构与实战
slug: rocketmq-架构与实战
type: article
status: active
tags: [RocketMQ, MQ, 消息队列]
sources:
 - raw/wujinsen_markdown/架构/消息队列/RocketMQ/rocketmq常用命令.note.md
 - raw/wujinsen_markdown/架构/消息队列/RocketMQ/安装部署/rocketmq4.5.2 - 服务安装， web管理界面安装.note.md
 - raw/wujinsen_markdown/架构/消息队列/RocketMQ/问题解决/Rocketmq之No route info of this topic解决思路.note.md
related: [消息队列, kafka-与-mq选型, rocketmq-事务消息实践, 秒杀设计]
created: 2026-06-22
updated: 2026-06-22
---

# RocketMQ 架构与实战

> 选型 [[middleware/kafka-与-mq选型]]；秒杀当前 Redis 队列。

## 1. 架构组件

| 组件 | 作用 |
|------|------|
| NameServer | 路由注册，无状态 |
| Broker | 存消息，Master-Slave |
| Producer / Consumer | 生产/消费，Consumer Group 负载 |

## 2. 核心概念

- **Topic** / **Tag** / **MessageQueue**（分区）
- 消费模式：集群（竞争）vs 广播
- 顺序消息：同一 MessageQueue 内有序

## 3. 常见运维

```bash
# 发消息 / 查消费进度等见 mqadmin
mqadmin topicList -n localhost:9876
```

## 4. No route info of this topic

- Broker 未注册 Topic、Producer 未连 NameServer、Topic 未创建
- 排查：NameServer 地址、Broker 日志、`autoCreateTopicEnable`

## 相关

[[middleware/rocketmq-事务消息实践]] · [[middleware/消息队列]]
