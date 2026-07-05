---
title: Java 集合框架
slug: java-集合框架
type: concept
status: active
tags: [Java, 集合, HashMap, ArrayList]
sources:
- raw/wujinsen_markdown/面试笔试/高级java/面试：HashMap 夺命二十一问！.note.md
related: [hashmap-面试题, java-并发, java-并发面试题]
created: 2026-06-22
updated: 2026-07-05
---

# Java 集合框架

> HashMap 深度 [[java/hashmap-面试题]]；并发集合 [[java/java-并发]]。

## 1. 体系

```
Collection
├── List (ArrayList, LinkedList)
├── Set (HashSet, TreeSet)
└── Queue (PriorityQueue, Deque)

Map
├── HashMap / LinkedHashMap / TreeMap
└── ConcurrentHashMap（并发）
```

## 2. 常用选型

| 需求 | 选型 |
|------|------|
| 随机访问 | ArrayList |
| 头尾插删 | LinkedList / ArrayDeque |
| 去重 | HashSet |
| 有序 | TreeMap / LinkedHashMap |
| 并发 Map | ConcurrentHashMap |

## 3. HashMap 要点（1.8+）

- 数组 + 链表 + **红黑树**（链表≥8 且 table≥64 树化）
- 容量 2 的幂；`(n-1) & hash` 寻址
- **线程不安全** → 并发用 ConcurrentHashMap

## 4. 与业务代码

业务层少直接调优集合；读配置、缓存 Map、MyBatis 结果 List 常见。Stream API 操作集合注意 NPE 与并发修改。

## 相关

[[java/hashmap-面试题]] · [[java/java-并发面试题]]

原文插图 annex：[[java/annex-面试：HashMap-夺命二十一问！]]
