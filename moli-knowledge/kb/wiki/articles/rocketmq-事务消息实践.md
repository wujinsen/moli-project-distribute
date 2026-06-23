---
title: RocketMQ 事务消息实践
slug: rocketmq-事务消息实践
type: article
status: active
tags: [RocketMQ, 分布式事务, 最终一致]
sources:
  - raw/wujinsen_markdown/架构/分布式事务/基于RocketMQ实现分布式事务 - 完整示例.note.md
  - raw/wujinsen_markdown/架构/消息队列/RocketMQ/Spring Cloud异步场景分布式事务怎样做？试试RocketMQ.note.md
  - raw/wujinsen_markdown/源码分析/RocketMQ/RocketMQ 源码分析 —— 事务消息.note.md
related: [分布式事务, 消息队列, rocketmq-架构与实战, 接口幂等性实践]
created: 2026-06-22
updated: 2026-06-22
---

# RocketMQ 事务消息实践

> 概念 [[分布式事务]]；架构 [[rocketmq-架构与实战]]。

## 1. 半消息流程

1. Producer 发 **半消息**（对消费者不可见）
2. 执行本地事务
3. **Commit** → 消息可见；**Rollback** → 丢弃
4. 若未决，Broker 回查本地事务状态

## 2. 与本地消息表对比

| | 事务消息 | 本地消息表 |
|---|----------|------------|
| 耦合 | 绑 MQ | 任意 MQ |
| 实现 | Broker 支持 | 自建定时扫表 |

## 3. 实现注意

- 回查接口必须**幂等**
- 消费端也要幂等 [[接口幂等性实践]]
- 与 Seata AT 二选一，勿叠床架屋

## 4. 茉莉场景示例（演进）

订单创建成功 → 发 MQ → 扣库存/发券消费者；失败则回滚或补偿。当前 order 模块可先用 DB 状态机 + Redis 队列，再迁 RocketMQ。

## 相关

[[kafka-与-mq选型]] · [[秒杀设计]]
