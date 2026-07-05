---
title: ConcurrentHashMap 原理
slug: concurrenthashmap原理
type: article
status: active
tags: [java, ConcurrentHashMap, 并发集合, CAS, 分段锁]
sources:
- raw/wujinsen_markdown/面试笔试/Java面试题精选/【67期】谈谈ConcurrentHashMap是如何保证线程安全的？.note.md
- raw/wujinsen_markdown/面试笔试/面试小结/面试小结之并发篇.note.md
related: [java-并发, volatile与可见性, synchronized与锁原理, java-并发面试题]
created: 2026-06-22
updated: 2026-07-05
---

# ConcurrentHashMap 原理

> 并发枢纽 [[java/java-并发]]；volatile 在 CHM 中的作用见 [[java/volatile与可见性]]。

## 为什么不用 HashMap + synchronized？

`HashMap` 非线程安全；`Collections.synchronizedMap` 全表一把锁，写多时吞吐差。CHM 目标：**高并发读 + 可控写**。

## JDK 7 vs 8（面试常问）

| 版本 | 结构 | 锁粒度 |
|------|------|--------|
| **7** | Segment[] + HashEntry | Segment 级 ReentrantLock |
| **8** | Node[] + 链表/红黑树 | **桶首节点** synchronized + CAS |

8 取消 Segment，结构更接近 HashMap，复杂度换更低锁粒度。

## get 为什么可以不加锁？

1. `table` 数组引用为 **volatile**，扩容替换数组时可见。
2. 每个 `Node` 的 `val`、`next` 为 **volatile**，读链路看到最新 published 节点。
3. 扩容时用 `ForwardingNode`（hash=-1）引导到 `nextTable`。
4. 红黑树节点 `TreeBin` 读路径有读写锁保护旋转期间一致性。

即：**volatile 保证可见性 + 不变性约束**（不会读到半初始化节点），而非 get 全程 mutex。

## put 概要（8）

1. 空表 CAS 初始化
2. 桶空 → CAS 放首 Node
3. 桶非空 → synchronized 锁**首节点**，在链表/树上插入或更新
4. 链表过长转红黑树；负载因子超阈值则扩容（多线程协助 transfer）

## size 与迭代

- `size()` 在 8 中用 baseCount + CounterCell 求和（类似 LongAdder）。
- 迭代器**弱一致性**：不抛 ConcurrentModificationException，可能看不到刚写入项。

## 使用建议

- 并发读写 Map → **CHM**；只读多可 `Collections.unmodifiableMap` 或 Immutable。
- **复合操作**（if-absent-then-put）用 `putIfAbsent`、`compute` 等原子 API，别 get+put 分开。
- 高冲突 key 仍会在同桶 synchronized，热点 key 需业务层拆分。

## 面试一句话

> CHM 8：CAS 初始化 + 锁桶头 + volatile 域保证无锁 get 可见；扩容 ForwardingNode 协调迁移。

详见 [[java/java-并发面试题]] Q6～Q8。
