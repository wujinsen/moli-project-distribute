---
title: CAP 定理的含义.note（原文插图 annex）
slug: annex-CAP-定理的含义
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/分布式事务/CAP 定理的含义.note.md
related: [分布式事务]
created: 2026-07-05
updated: 2026-07-05
---

htp:/ w.ruanyifeng.com/blog/2018/07/cap.html

分布式系统（distributed system）正变得越来越重要，⼤型⽹站⼏乎都是分布式的。 分布式系统的最⼤难点，就是各个节点的状态如何同步。CAP 定理是这⽅⾯的基本定理，也是理解分 布式系统的起点。 本⽂介绍该定理。它其实很好懂，⽽且是显⽽易⻅的。下⾯的内容主要参考了 Michael Whitaker 的

⽂ 章

。

# ⼀、分布式系统的三个指标

![image 1](assets/imageFile1.png)

198年，加州⼤学的计算机科学家 Eric Brewer 提出，分布式系统有三个指标。

Consistency Availability Partition tolerance

它们的第⼀个字⺟分别是 C、A、P。 Eric Brewer 说，这三个指标不可能同时做到。这个结论就叫做 CAP 定理。

# ⼆、Partition tolerance

先看 Partition tolerance，中⽂叫做"分区容错"。 ⼤多数分布式系统都分布在多个⼦⽹络。每个⼦⽹络就叫做⼀个区（partition）。分区容错的意思是， 区间通信可能失败。⽐如，⼀台服务器放在中国，另⼀台服务器放在美国，这就是两个区，它们之间 可能⽆法通信。

![image 2](assets/imageFile2.png)

上图中，G1 和 G2 是两台跨区的服务器。G1 向 G2 发送⼀条消息，G2 可能⽆法收到。系统设计的时 候，必须考虑到这种情况。 ⼀般来说，分区容错⽆法避免，因此可以认为 CAP 的 P 总是成⽴。CAP 定理告诉我们，剩下的 C 和 A ⽆法同时做到。

# 三、Consistency

Consistency 中⽂叫做"⼀致性"。意思是，写操作之后的读操作，必须返回该值。举例来说，某条记录 是 v0，⽤户向 G1 发起⼀个写操作，将其改为 v1。

![image 3](assets/imageFile3.png)

接下来，⽤户的读操作就会得到 v1。这就叫⼀致性。

![image 4](assets/imageFile4.png)

问题是，⽤户有可能向 G2 发起读操作，由于 G2 的值没有发⽣变化，因此返回的是 v0。G1 和 G2 读 操作的结果不⼀致，这就不满⾜⼀致性了。

![image 5](assets/imageFile5.png)

为了让 G2 也能变为 v1，就要在 G1 写操作的时候，让 G1 向 G2 发送⼀条消息，要求 G2 也改成 v1。

![image 6](assets/imageFile6.png)

这样的话，⽤户向 G2 发起读操作，也能得到 v1。

![image 7](assets/imageFile7.png)

# 四、Availability

Availability 中⽂叫做"可⽤性"，意思是只要收到⽤户的请求，服务器就必须给出回应。 ⽤户可以选择向 G1 或 G2 发起读操作。不管是哪台服务器，只要收到请求，就必须告诉⽤户，到底是 v0 还是 v1，否则就不满⾜可⽤性。

# 五、Consistency 和 Availability 的⽭盾

⼀致性和可⽤性，为什么不可能同时成⽴？答案很简单，因为可能通信失败（即出现分区容错）。 如果保证 G2 的⼀致性，那么 G1 必须在写操作时，锁定 G2 的读操作和写操作。只有数据同步后，才 能重新开放读写。锁定期间，G2 不能读写，没有可⽤性不。 如果保证 G2 的可⽤性，那么势必不能锁定 G2，所以⼀致性不成⽴。 综上所述，G2 ⽆法同时做到⼀致性和可⽤性。系统设计时只能选择⼀个⽬标。如果追求⼀致性，那么 ⽆法保证所有节点的可⽤性；如果追求所有节点的可⽤性，那就没法做到⼀致性。 [更新 2018.7.17] 读者问，在什么场合，可⽤性⾼于⼀致性？ 举例来说，发布⼀张⽹⻚到 CDN，多个服务器有这张⽹⻚的副本。后来发现⼀个错误，需要更新⽹ ⻚，这时只能每个服务器都更新⼀遍。 ⼀般来说，⽹⻚的更新不是特别强调⼀致性。短时期内，⼀些⽤户拿到⽼版本，另⼀些⽤户拿到新版 本，问题不会特别⼤。当然，所有⼈最终都会看到新版本。所以，这个场合就是可⽤性⾼于⼀致性。 （完）
