---
title: Zookeeper 面试题
slug: zookeeper-面试题
type: interview
status: active
tags: [面试, Zookeeper, 分布式]
sources:
 - raw/wujinsen_markdown/面试笔试/框架/zookeeper/精尽 Zookeeper 面试题（最新更新时间：2020-09-01.note.md
related: [zookeeper-与协调服务, dubbo-面试题, 分布式锁, 分布式理论面试题]
created: 2026-06-22
updated: 2026-06-22
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

`/dubbo/{service}/providers` 等（老版本）；用 Nacos [[dubbo-与-nacos]]。

## Q8. ZK 锁 vs Redis 锁？

ZK 可靠、性能低；Redis 快、需 Redisson/Lua 防 bug [[redis分布式锁实现]]。

## Q9. 脑裂如何避免？

epoch 递增，旧 Leader 写入被拒绝。
