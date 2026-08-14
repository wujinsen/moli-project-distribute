---
title: Zookeeper 与协调服务
slug: zookeeper-与协调服务
type: concept
status: active
tags: [Zookeeper, 分布式, CAP, Dubbo]
sources:
- raw/wujinsen_markdown/BigData/Zookeeper/Zookeeper介绍.note.md
- raw/wujinsen_markdown/BigData/Zookeeper/Zookeeper的Quorum机制-谈谈怎样解决脑裂(split-brain).note.md
- raw/wujinsen_markdown/BigData/Zookeeper/apache kafka系列之在zookeeper中存储结构.note.md
- raw/wujinsen_markdown/BigData/Zookeeper/zookeeper原理和实战/leader选举.note.md
- raw/wujinsen_markdown/BigData/Zookeeper/zookeeper原理和实战/zab协议.note.md
- raw/wujinsen_markdown/BigData/Zookeeper/zookeeper安装        .note.md
- raw/wujinsen_markdown/BigData/Zookeeper/改良版taokeeper.note.md
- raw/wujinsen_markdown/BigData/Zookeeper/通过Java代码获取Zookeeper服务器状态.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/zookeeper安装手顺.note.md
- raw/wujinsen_markdown/面试笔试/框架/zookeeper/精尽 Zookeeper 面试题（最新更新时间：2020-09-01.note.md
related: [dubbo-与-nacos, 分布式锁, 分布式理论基础, zookeeper-面试题, nacos-注册与配置]
created: 2026-06-22
updated: 2026-07-05
---

# Zookeeper 与协调服务

> **注册中心用 Nacos**，非 ZK；ZK 仍常见于 Dubbo 老架构、Kafka、分布式锁。面试 [[middleware/zookeeper-面试题]]。

## 1. 是什么

Zookeeper（ZK）是 **CP** 型分布式协调服务：强一致、选举期间可能短暂不可用。

| 能力 | 用途 |
|------|------|
| 临时顺序节点 | 分布式锁、选主 |
| Watcher | 配置变更通知 |
| 树形 ZNode | Dubbo 注册目录、Kafka broker 元数据 |

## 2. 核心概念

- **ZAB 协议**：崩溃恢复 + 消息广播，保证顺序一致
- **Leader 选举**：过半存活才能选主；**脑裂**靠 epoch 防双主
- **会话 Session**：客户端心跳；超时 ephemeral 节点删除 → 故障感知

## 3. 与 Nacos / Dubbo

| | Zookeeper | Nacos（） |
|---|-----------|---------------|
| 一致性 | CP 倾向 | AP 注册 + 配置 |
| Dubbo 注册 | 历史默认 | ✅ 项目用 Nacos |
| 运维 | 奇数节点集群 | 8848 单机/集群 |

见 [[middleware/dubbo-与-nacos]]、[[middleware/nacos-注册与配置]]。

## 4. 与分布式锁

ZK 锁：临时顺序节点 + 监听前驱；**可靠性高、性能低于 Redis**。对比 [[cache/分布式锁]]。

## 相关

[[middleware/zookeeper-面试题]] · [[middleware/分布式理论基础]]

## 批次#1321 增补（wujinsen Phase2 P1 BigData）

合并 BigData/王 ZK 原理 raw。

原文插图 annex：[[middleware/annex-Zookeeper介绍]]

原文插图 annex：[[middleware/annex-精尽-Zookeeper-面试题（最新更新时间：2020-09-01]]
