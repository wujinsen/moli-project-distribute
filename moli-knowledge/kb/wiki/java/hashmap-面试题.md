---
title: HashMap 面试题
slug: hashmap-面试题
type: interview
status: active
tags: [面试, HashMap, Java]
sources:
- raw/wujinsen_markdown/面试笔试/高级java/2018年一线互联网公司Java高级面试题总结.note.md
- raw/wujinsen_markdown/面试笔试/高级java/Java高级程序员面试大纲——备战金三银四跳槽季.note.md
- raw/wujinsen_markdown/面试笔试/高级java/缓存更新的套路.note.md
- raw/wujinsen_markdown/面试笔试/高级java/面试：HashMap 夺命二十一问！.note.md
- raw/wujinsen_markdown/面试笔试/高级java/高级java面试.note.md
related: [java-集合框架, java-并发面试题]
created: 2026-06-22
updated: 2026-07-05
---

# HashMap 面试题

## Q1. HashMap 结构？

数组+链表+红黑树；1.8 后链表过长转树。

## Q2. 初始容量 16，负载因子 0.75？

超阈值扩容 2 倍；0.75 时空/时间折中。

## Q3. hash 扰动函数作用？

高 bit 参与下标，减碰撞。

## Q4. 为什么容量是 2 的幂？

位运算取模快；扩容 `(e.hash & oldCap)==0` 分裂链表。

## Q5. put 流程简述？

算 hash → 桶空则放 → 否则链表/树插入 → 超阈值 resize。

## Q6. 线程为什么不安全？

并发 put 丢数据、死循环（1.7）；1.8 改头插为尾插。

## Q7. ConcurrentHashMap 1.8 怎么锁？

CAS + synchronized 锁桶头；粒度细于 1.7 Segment。

## Q8. HashMap vs Hashtable？

Hashtable 全表 synchronized，已过时。

## Q9. equals 与 hashCode 契约？

不等对象可同 hash；相等必须同 hash。

## Q10. LinkedHashMap 用途？

插入/访问顺序；LRU 可重写 `removeEldestEntry`。

## Q11. 用 Stream 遍历 Map 注意？

`ConcurrentHashMap` 弱一致迭代；普通 Map 勿并发改。

## 批次#1312 增补（wujinsen P1）

合并高级 Java/HashMap raw。

<!-- t22-wujinsen-images:raw/wujinsen_markdown/面试笔试/高级java/2018年一线互联网公司Java高级面试题总结.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/面试笔试/高级java/2018年一线互联网公司Java高级面试题总结.note.md` · T22 **B** 档

### 来自：2018年一线互联网公司Java高级面试题总结

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/%E9%AB%98%E7%BA%A7java/2018%E5%B9%B4%E4%B8%80%E7%BA%BF%E4%BA%92%E8%81%94%E7%BD%91%E5%85%AC%E5%8F%B8Java%E9%AB%98%E7%BA%A7%E9%9D%A2%E8%AF%95%E9%A2%98%E6%80%BB%E7%BB%93.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/%E9%AB%98%E7%BA%A7java/2018%E5%B9%B4%E4%B8%80%E7%BA%BF%E4%BA%92%E8%81%94%E7%BD%91%E5%85%AC%E5%8F%B8Java%E9%AB%98%E7%BA%A7%E9%9D%A2%E8%AF%95%E9%A2%98%E6%80%BB%E7%BB%93.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/%E9%AB%98%E7%BA%A7java/2018%E5%B9%B4%E4%B8%80%E7%BA%BF%E4%BA%92%E8%81%94%E7%BD%91%E5%85%AC%E5%8F%B8Java%E9%AB%98%E7%BA%A7%E9%9D%A2%E8%AF%95%E9%A2%98%E6%80%BB%E7%BB%93.note_images/imageFile3.png)

原文插图 annex：[[java/annex-Java高级程序员面试大纲——备战金三银四跳槽季]]

原文插图 annex：[[cache/annex-缓存更新的套路]]

原文插图 annex：[[java/annex-面试：HashMap-夺命二十一问！]]
