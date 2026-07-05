---
title: Zookeeper 面试题
slug: zookeeper-面试题
type: interview
status: active
tags: [面试, Zookeeper, 分布式]
sources:
- raw/wujinsen_markdown/BigData/Zookeeper/Zookeeper介绍.note.md
- raw/wujinsen_markdown/BigData/Zookeeper/Zookeeper的Quorum机制-谈谈怎样解决脑裂(split-brain).note.md
- raw/wujinsen_markdown/BigData/Zookeeper/apache kafka系列之在zookeeper中存储结构.note.md
- raw/wujinsen_markdown/BigData/Zookeeper/zookeeper原理和实战/leader选举.note.md
- raw/wujinsen_markdown/BigData/Zookeeper/zookeeper原理和实战/zab协议.note.md
- raw/wujinsen_markdown/BigData/Zookeeper/zookeeper安装        .note.md
- raw/wujinsen_markdown/BigData/Zookeeper/改良版taokeeper.note.md
- raw/wujinsen_markdown/BigData/Zookeeper/通过Java代码获取Zookeeper服务器状态.note.md
- raw/wujinsen_markdown/面试笔试/框架/zookeeper/精尽 Zookeeper 面试题（最新更新时间：2020-09-01.note.md
related: [zookeeper-与协调服务, dubbo-面试题, 分布式锁, 分布式理论面试题]
created: 2026-06-22
updated: 2026-07-05
---

# Zookeeper 面试题

## Q1. ZK 是什么？CAP 哪类？

分布式协调，**CP**：分区时保一致，可能牺牲可用。

## Q2. ZAB 与 Paxos 关系？

ZAB 为 Kafka/ZK 场景优化的一致性协议，选主+广播。

## Q3. 四种节点类型？

持久/临时 × 有序/无序；临时节点 session 断则删。

## Q4. Watcher 特点？

一次性、异步；收到通知后需重新注册。

## Q5. 选举过程简述？

Looking → Following/Leading；过半 ack 新 Leader。

## Q6. 为什么集群奇数节点？

过半机制，3 节点允许挂 1，4 节点也只允许挂 1，浪费。

## Q7. ZK 做 Dubbo 注册中心路径？

`/dubbo/{service}/providers` 等（老版本）；用 Nacos [[middleware/dubbo-与-nacos]]。

## Q8. ZK 锁 vs Redis 锁？

ZK 可靠、性能低；Redis 快、需 Redisson/Lua 防 bug [[cache/redis分布式锁实现]]。

## Q9. 脑裂如何避免？

epoch 递增，旧 Leader 写入被拒绝。
## ZK 面试速记补充

- **ZAB** 协议：崩溃恢复 + 消息广播
- **临时节点**：Session 断开自动删，做服务发现
- **watch**：一次性触发，需重新注册
- **脑裂**：过半写成功原则

见 [[middleware/zookeeper-与协调服务]]。

## 批次#1313 增补（wujinsen P2）

合并精尽 ZK 面试题 raw。
