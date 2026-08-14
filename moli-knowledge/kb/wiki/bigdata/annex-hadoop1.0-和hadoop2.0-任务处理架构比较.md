---
title: hadoop1.0 和hadoop2.0 任务处理架构比较.note（原文插图 annex）
slug: annex-hadoop1.0-和hadoop2.0-任务处理架构比较
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hadoop/hadoop1.0 和hadoop2.0 任务处理架构比较.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

刚刚看到⼀篇⽂章对 hadop1 和 hadop 2 做了⼀个解释 图⽚不错 拿来看看

![image 1](assets/imageFile1.png)

Hadop 1.0

![image 2](assets/imageFile2.png)

从上图中可以清楚的看出原 MapReduce 程序的流程及设计思路： ⾸先⽤户程序 (JobClient) 提交了⼀个 job，job 的信息会发送到 Job Tracker 中，Job Tracker 是 Map-reduce 框架的中⼼，他需要与集群中的机器定时通信 (heartbeat), 需要管理哪些程序应该跑 在哪些机器上，需要管理所有 job 失败、重启等操作。

1.

- 2.
- 3.


TaskTracker 是 Map-reduce 集群中每台机器都有的⼀个部分，他做的事情主要是监视⾃⼰所在机 器的资源情况。 TaskTracker 同时监视当前机器的 tasks 运⾏状况。TaskTracker 需要把这些信息通过 heartbeat 发送给 JobTracker，JobTracker 会搜集这些信息以给新提交的 job 分配运⾏在哪些机器上。上图 虚线箭头就是表示消息的发送 - 接收的过程。

可以看得出原来的 map-reduce 架构是简单明了的，在最初推出的⼏年，也得到了众多的成功案例， 获得业界⼴泛的⽀持和肯定，但随着分布式系统集群的规模和其⼯作负荷的增⻓，原框架的问题逐渐 浮出⽔⾯，主要的问题集中如下：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


JobTracker 是 Map-reduce 的集中处理点，存在单点故障。 JobTracker 完成了太多的任务，造成了过多的资源消耗，当 map-reduce job ⾮常多的时候，会 造成很⼤的内存开销，潜在来说，也增加了 JobTracker fail 的⻛险，这也是业界普遍总结出⽼ Hadop 的 Map-Reduce 只能⽀持 4 0 节点主机的上限。 在 TaskTracker 端，以 map/reduce task 的数⽬作为资源的表示过于简单，没有考虑到 cpu/ 内存 的占⽤情况，如果两个⼤内存消耗的 task 被调度到了⼀块，很容易出现 OM。 在 TaskTracker 端，把资源强制划分为 map task slot 和 reduce task slot, 如果当系统中只有 map task 或者只有 reduce task 的时候，会造成资源的浪费，也就是前⾯提过的集群资源利⽤的问 题。 源代码层⾯分析的时候，会发现代码⾮常的难读，常常因为⼀个 clas 做了太多的事情，代码量达 3 0 多⾏，，造成 clas 的任务不清晰，增加 bug 修复和版本维护的难度。 从操作的⻆度来看，现在的 Hadop MapReduce 框架在有任何重要的或者不重要的变化 ( 例如 bug 修复，性能提升和特性化 ) 时，都会强制进⾏系统级别的升级更新。更糟的是，它不管⽤户 的喜好，强制让分布式集群系统的每⼀个⽤户端同时更新。这些更新会让⽤户为了验证他们之前 的应⽤程序是不是适⽤新的 Hadop 版本⽽浪费⼤量时间。

hadop2.0：

![image 3](assets/imageFile3.png)

从业界使⽤分布式系统的变化趋势和 hadop 框架的⻓远发展来看，MapReduce 的 JobTracker/TaskTracker 机制需要⼤规模的调整 来修复它在可扩展性，内存消耗，线程模型，可靠性和性能上的缺陷。在过去的⼏年中，hadop 开发团队做了⼀些 bug 的修复，但是 最近这些修复的成本越来越⾼，这表明对原框架做出改变的难度越来越⼤。

为从根本上解决旧 MapReduce 框架的性能瓶颈，促进 Hadop 框架的更⻓远发展，从 0.23.0 版本开 始，Hadop 的 MapReduce 框架完全重构，发⽣了根本的变化。新的 Hadop MapReduce 框架命名 为 MapReduceV2 或者叫 Yarn，

重构根本的思想是将 JobTracker 两个主要的功能分离成单独的组件，这两个功能是资源管理和任务调 度 / 监控。新的资源管理器全局管理所有应⽤程序计算资源的分配，每⼀个应⽤的 AplicationMaster 负责相应的调度和协调。⼀个应⽤程序⽆⾮是⼀个单独的传统的 MapReduce 任务或者是⼀个 DAG( 有 向⽆环图 ) 任务。ResourceManager 和每⼀台机器的节点管理服务器能够管理⽤户在那台机器上的进 程并能对计算进⾏组织。 事实上，每⼀个应⽤的 AplicationMaster 是⼀个详细的框架库，它结合从 ResourceManager 获得的 资源和 NodeManager 协同⼯作来运⾏和监控任务。 上图中 ResourceManager ⽀持分层级的应⽤队列，这些队列享有集群⼀定⽐例的资源。从某种意义上 讲它就是⼀个纯粹的调度器，它在执⾏过程中不对应⽤进⾏监控和状态跟踪。同样，它也不能重启因 应⽤失败或者硬件错误⽽运⾏失败的任务。 ResourceManager 是基于应⽤程序对资源的需求进⾏调度的 ; 每⼀个应⽤程序需要不同类型的资源因 此就需要不同的容器。资源包括：内存，CPU，磁盘，⽹络等等。可以看出，这同现 Mapreduce 固定 类型的资源使⽤模型有显著区别，它给集群的使⽤带来负⾯的影响。资源管理器提供⼀个调度策略的 插件，它负责将集群资源分配给多个队列和应⽤程序。调度插件可以基于现有的能⼒调度和公平调度 模型。 上图中 NodeManager 是每⼀台机器框架的代理，是执⾏应⽤程序的容器，监控应⽤程序的资源使⽤ 情况 (CPU，内存，硬盘，⽹络 ) 并且向调度器汇报。

每⼀个应⽤的 AplicationMaster 的职责有：向调度器索要适当的资源容器，运⾏任务，跟踪应⽤程序 的状态和监控它们的进程，处理任务的失败原因。

详细配置参考： htp:/ w.ibm.com/developerworks/cn/opensource/os-cn-hadop-yarn/
