---
title: Storm 集群空闲 CPU 飙高问题排查.note（原文插图 annex）
slug: annex-Storm-集群空闲-CPU-飙高问题排查
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/Storm/Storm的坑/Storm 集群空闲 CPU 飙高问题排查.note.md
related: [flink-流批一体入门]
created: 2026-07-05
updated: 2026-07-05
---

最近将公司的在线业务迁移到Storm集群上，上线后遇到低峰期CPU耗费严重的情况。在解决问题的过 程中深⼊了解了storm的内部实现原理，并且解决了⼀个storm0.9-0.10版本⼀直存在的严重bug，⽬前 代码已经合并到了storm新版本中，在这篇⽂章⾥会介绍这个问题出现的场景、分析思路、解决的⽅式 和⼀些个⼈的收获。

# 背景

⾸先简单介绍⼀下Storm，熟悉的同学可以直接跳过这段。 Storm是Twitter开源的⼀个⼤数据处理框架，专注于流式数据的处理。Storm通过创建拓扑结构 （Topology）来转换数据流。和Hadoop的作业（Job）不同，Topology会持续转换数据，除⾮被集群 关闭。 下图是⼀个简单的Storm Topology结构图。

![image 1](assets/imageFile1.png)

可以看出Topology是由不同组件（Component）串/并联形成的有向图。数据元组（Tuple）会在 Component之间通过数据流的形式进⾏有向传递。Component有两种

Spout：Tuple来源节点，持续不断的产⽣Tuple，形成数据流 Bolt：Tuple处理节点，处理收到的Tuple，如果有需要，也可以⽣成新的Tuple传递到其他Bolt

⽬前业界主要在离线或者对实时性要求不⾼业务中使⽤Storm。随着Storm版本的更迭，可靠性和实时 性在逐渐增强，已经有运⾏在线业务的能⼒。因此我们尝试将⼀些实时性要求在百毫秒级的在线业务 迁⼊Storm集群。

现象

- 1.
- 2.


某次⾼峰时，Storm上的⼀个业务拓扑频繁出现消息处理延迟。延时达到了10s甚⾄更⾼。查看⾼ 峰时的物理机指标监控，CPU、内存和IO都有很⼤的余量。判断是随着业务增⻓，服务流量逐渐 增加，某个Bolt之前设置的并⾏度不够，导致消息堆积了。 临时增加该Bolt并⾏度，解决了延迟的问题，但是第⼆天的低峰期，服务突然报警，CPU负载过 ⾼，达到了100%。

排查

⽤Top看了下CPU占⽤，系统调⽤占⽤了70%左右。再⽤ wtool

- 1.


对Storm的⼯作进程进⾏分析，找到了CPU占⽤最⾼的线程

java.lang.Thread.State: TIMED_WAITING (parking) at sun.misc.Unsafe.park(Native Method)

- parking to wait for <0x0000000640a248f8> (a

java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject) at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:215) at

java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchroni zer.java:2163)

at com.lmax.disruptor.BlockingWaitStrategy.waitFor(BlockingWaitStrategy.java:87) at com.lmax.disruptor.ProcessingSequenceBarrier.waitFor(ProcessingSequenceBarrier.java:54) at backtype.storm.utils.DisruptorQueue.consumeBatchWhenAvailable(DisruptorQueue.java:97) at backtype.storm.disruptor$consume_batch_when_available.invoke(disruptor.clj:80) at backtype.storm.daemon.executor$fn__3441$fn__3453$fn__3500.invoke(executor.clj:748) at backtype.storm.util$async_loop$fn__464.invoke(util.clj:463) at clojure.lang.AFn.run(AFn.java:24) at java.lang.Thread.run(Thread.java:745)

我们可以看到这些线程都在信号量上等待。调⽤的来源是 disruptor$consume_batch_when_available。

2.

disruptor是Storm内部消息队列的封装。所以先了解了⼀下Storm内部的消息传输机制。

![image 2](assets/imageFile2.png)

Understanding the Internal Message Buﬀers of Storm

（图⽚来源 ） Storm的⼯作节点称为Worker（其实就是⼀个JVM进程）。不同Worker之间通过Netty（旧版Storm使 ⽤ZeroMQ）进⾏通讯。 每个Worker内部包含⼀组Executor。Strom会为拓扑中的每个Component都分配⼀个Executor。在实 际的数据处理流程中，数据以消息的形式在Executor之间流转。Executor会循环调⽤绑定的 Component的处理⽅法来处理收到的消息。

Executor之间的消息传输使⽤队列作为消息管道。Storm会给每个Executor分配两个队列和两个处理线 程。

⼯作线程：读取接收队列，对消息进⾏处理，如果产⽣新的消息，会写⼊发送队列 发送线程：读取发送队列，将消息发送其他Executor

当Executor的发送线程发送消息时，会判断⽬标Executor是否在同⼀Worker内，如果是，则直接将消 息写⼊⽬标Executor的接收队列，如果不是，则将消息写⼊Worker的传输队列，通过⽹络发送。 Executor⼯作/发送线程读取队列的代码如下，这⾥会循环调⽤consume-batch-when-available读取队 列中的消息，并对消息进⾏处理。

(async-loop (fn []

... (disruptor/consume-batch-when-available receive-queue event-handler)

... ))

3. 我们再来看⼀下consume_batch_when_available这个函数⾥做了什么。

(defn consume-batch-when-available [^DisruptorQueue queue handler] (.consumeBatchWhenAvailable queue handler))

前⾯提到Storm使⽤队列作为消息管道。Storm作为流式⼤数据处理框架，对消息传输的性能很敏感， 因此使⽤了⾼效内存队列Disruptor Queue作为消息队列。

![image 3](assets/imageFile3.png)

Disruptor Queue是LMAX开源的⼀个⽆锁内存队列。内部实现如下。

![image 4](assets/imageFile4.png)

## Disruptor queue Introduction

（图⽚来源 ） Disruptor Queue通过Sequencer来管理队列，Sequencer内部使⽤RingBuﬀer存储消息。RingBuﬀer中 消息的位置使⽤Sequence表示。队列的⽣产消费过程如下

Sequencer使⽤⼀个Cursor来保存写⼊位置。 每个Consumer都会维护⼀个消费位置，并注册到Sequencer。 Consumer通过SequenceBarrier和Sequencer进⾏交互。Consumer每次消费时， SequenceBarrier会⽐较消费位置和Cursor来判断是否有可⽤消息：如果没有， 会按照设定的策 略等待消息 ；如果有，则读取消息，修改消费位置。 Producer在写⼊前会查看所有消费者的消费位置，在有可⽤位置时会写⼊消息，更新Cursor。

查看DisruptorQueue.consumeBatchWhenAvailable实现如下

final long nextSequence = _consumer.get() + 1; final long availableSequence = _barrier.waitFor(nextSequence, 10, TimeUnit.MILLISECONDS); if (availableSequence >= nextSequence) {

consumeBatchToCursor(availableSequence, handler); }

继续查看_barrier.waitFor⽅法

public long waitFor(final long sequence, final long timeout, final TimeUnit units) throws AlertException, InterruptedException {

checkAlert(); return waitStrategy.waitFor(sequence, cursorSequence, dependentSequences, this, timeout,

units); }

Disruptor Queue为消费者提供了若⼲种消息等待策略

BlockingWaitStrategy：阻塞等待，CPU占⽤⼩，但是会切换线程，延迟较⾼ BusySpinWaitStrategy：⾃旋等待，CPU占⽤⾼，但是⽆需切换线程，延迟低 YieldingWaitStrategy：先⾃旋等待，然后使⽤Thread.yield()唤醒其他线程，CPU占⽤和延迟⽐ 较均衡

SleepingWaitStrategy：先⾃旋，然后Thread.yield()，最后调⽤LockSupport.parkNanos(1L)， CPU占⽤和延迟⽐较均衡

Storm的默认等待策略为BlockingWaitStrategy。BlockingWaitStrategy的waitFor函数实现如下

if ((availableSequence = cursor.get()) < sequence) { lock.lock(); try {

++numWaiters; while ((availableSequence = cursor.get()) < sequence) {

barrier.checkAlert(); if (!processorNotifyCondition.await(timeout, sourceUnit)) {

break; }

}

} finally {

--numWaiters; lock.unlock();

} }

BlockingWaitStrategy内部使⽤信号量来阻塞Consumer，当await超时后，Consumer线程会被⾃动唤 醒，继续循环查询可⽤消息。

- 4.
- 5.


⽽DisruptorQueue.consumeBatchWhenAvailable⽅法中可以看到，Storm此处设置超时为10ms。 推测在没有消息或者消息量较少时，Executor在消费队列时会被阻塞，由于超时时间很短，⼯作 线程会频繁超时然后重新阻塞，导致CPU占⽤飙⾼。

尝试将10ms修改成100ms，编译Storm后重新部署集群，使⽤Storm的demo拓扑，将bolt并发度调到 1000，修改spout代码为10s发⼀条消息。经测试CPU占⽤⼤幅减少。 再将100ms改成1s，测试CPU占⽤基本降为零。

但是随着调⾼超时，测试时并没有发现消息处理有延时。继续查看BlockingWaitStrategy代码，发 现Disruptor Queu的Producer在写⼊消息后会唤醒等待的Consumer。

if (0 != numWaiters) {

lock.lock(); try {

processorNotifyCondition.signalAll();

} finally {

lock.unlock(); }

}

这样，Storm的10ms超时就很奇怪了，没有减少消息延时，反⽽增加了系统负载。带着这个疑问查看 代码的上下⽂，发现在构造DisruptorQueue对象时有这么⼀句注释

;; :block strategy requires using a timeout on waitFor (implemented in DisruptorQueue), as sometimes the consumer stays blocked even when there's an item on the queue.

(defnk disruptor-queue [^String queue-name buffer-size :claim-strategy :multi-threaded :wait-strategy :block] (DisruptorQueue. queue-name

((CLAIM-STRATEGY claim-strategy) buffer-size) (mk-wait-strategy wait-strategy)))

Storm使⽤的Disruptor Queue版本为2.10.1。查看Disruptor Queue的change log，发现该版本的 BlockingWaitStrategy有潜在的并发问题，可能导致某条消息在写⼊时没有唤醒等待的消费者。

- 2.10.2 Released (21-Aug-2012)


Bug ﬁx, potential race condition in BlockingWaitStrategy. Bug ﬁx set initial SequenceGroup value to -1 (Issue #27). Deprecate timeout methods that will be removed in version 3.

因此Storm使⽤了短超时，这样在出现并发问题时，没有被唤醒的消费⽅也会很快因为超时重新查询可 ⽤消息，防⽌出现消息延时。 这样如果直接修改超时到1000ms，⼀旦出现并发问题，最坏情况下消息会延迟1000ms。在权衡性能 和延时之后，我们在Storm的配置⽂件中增加配置项来修改超时参数。这样使⽤者可以⾃⼰选择保证低 延时还是低CPU占⽤率。

6.

就BlockingWaitStrategy的潜在并发问题咨询了Disruptor Queue的作者，得知2.10.4版本已经修复 了这个并发问题（

Race condition in 2.10.1 release ）。 将Storm依赖升级到此版本。但是对Disruptor Queue的2.10.1做了并发测试，⽆法复现这个并发问题， 因此也⽆法确定2.10.4是否彻底修复。谨慎起⻅，在升级依赖的同时保留了之前的超时配置项，并将默 认超时调整为1000ms。经测试，在集群空闲时CPU占⽤正常，并且压测也没有出现消息延时。

总结

## [STORM-935] Upda te Disruptor queue version to 2.10.4

- 1.
- 2.


关于集群空闲CPU反⽽飙⾼的问题，已经向Storm社区提交PR并且已被接受

。在线业务流量通常起伏很⼤，如果被这个问题困扰，可 以考虑应⽤此patch。

Storm UI中可以看到很多有⽤的信息，但是缺乏记录，最好对其进⾏⼆次开发（或者直接读取 ZooKeeper中信息），记录每个时间段的数据，⽅便分析集群和拓扑运⾏状况。
