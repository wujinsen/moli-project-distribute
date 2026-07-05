---
title: GC调优在Spark应用中的实践.note（原文插图 annex）
slug: annex-GC调优在Spark应用中的实践
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/spark(1)/GC调优在Spark应用中的实践.note.md
related: [spark-核心概念与实践]
created: 2026-07-05
updated: 2026-07-05
---

Spark是时下⾮常热⻔的⼤数据计算框架，以其卓越的性能优势、独特的架构、易⽤的⽤户接⼝和丰富 的分析计算库，正在⼯业界获得越来越⼴泛的应⽤。与Hadop、HBase⽣态圈的众多项⽬⼀样， Spark的运⾏离不开JVM的⽀持。由于Spark⽴⾜于内存计算，常常需要在内存中存放⼤量数据，因此 也更依赖JVM的垃圾回收机制（GC）。并且同时，它也⽀持兼容批处理和流式处理，对于程序吞吐量 和延迟都有较⾼要求，因此GC参数的调优在Spark应⽤实践中显得尤为重要。本⽂主要讲述如何针对 Spark应⽤程序配置JVM的垃圾回收器，并从实际案例出发，剖析如何进⾏GC调优，进⼀步提升Spark 应⽤的性能。

问题介绍

随着Spark在⼯业界得到⼴泛使⽤，Spark应⽤稳定性以及性能调优问题不可避免地引起了⽤户的关 注。由于Spark的特⾊在于内存计算，我们在部署Spark集群时，动辄使⽤超过10GB的内存作为Heap 空间，这在传统的Java应⽤中是⽐较少⻅的。在⼴泛的合作过程中，确实有很多⽤户向我们抱怨运⾏ Spark应⽤时GC所带来的各种问题。例如垃圾回收时间久、程序⻓时间⽆响应，甚⾄造成程序崩溃或 者作业失败。对此，我们该怎样调试Spark应⽤的垃圾收集器呢？在本⽂中，我们从应⽤实例出发，结 合具体问题场景，探讨了Spark应⽤的GC调优⽅法。 按照经验来说，当我们配置垃圾收集器时，主要有两种策略⸺Paralel GC和CMS GC。前者注重更⾼ 的吞吐量，⽽后者则注重更低的延迟。两者似乎是⻥和熊掌，不能兼得。在实际应⽤中，我们只能根 据应⽤对性能瓶颈的侧重性，来选取合适的垃圾收集器。例如，当我们运⾏需要有实时响应的场景的 应⽤时，我们⼀般选⽤CMS GC，⽽运⾏⼀些离线分析程序时，则选⽤Paralel GC。那么对于Spark这 种既⽀持流式计算，⼜⽀持传统的批处理运算的计算框架来说，是否存在⼀组通⽤的配置选项呢？ 通常CMS GC是企业⽐较常⽤的GC配置⽅案，并在⻓期实践中取得了⽐较好的效果。例如对于进程中 若存在⼤量寿命较⻓的对象，Paralel GC经常带来较⼤的性能下降。因此，即使是批处理的程序也能 从CMS GC中获益。不过，在从1.6开始的HOTSPOT JVM中，我们发现了⼀个新的GC设置项： Garbage-First GC(G1 GC)。Oracle将其定位为CMS GC的⻓期演进，这让我们重燃了⻥与熊掌兼得的 希望！那么，我们⾸先了解⼀下GC的⼀些相关原理吧。

GC算法原理

在传统JVM内存管理中，我们把Heap空间分为Young/Old两个分区，Young分区⼜包括⼀个Eden和两 个Survivor分区，如图1所示。新产⽣的对象⾸先会被存放在Eden区，⽽每次minor GC发⽣时，JVM⼀ ⽅⾯将Eden分区内存活的对象拷⻉到⼀个空的Survivor分区，另⼀⽅⾯将另⼀个正在被使⽤的Survivor 分区中的存活对象也拷⻉到空的Survivor分区内。在此过程中，JVM始终保持⼀个Survivor分区处于全 空的状态。⼀个对象在两个Survivor之间的拷⻉到⼀定次数后，如果还是存活的，就将其拷⼊Old分 区。当Old分区没有⾜够空间时，GC会停下所有程序线程，进⾏Ful GC，即对Old区中的对象进⾏整 理。这个所有线程都暂停的阶段被称为Stop-The-World(STW)，也是⼤多数GC算法中对性能影响最⼤ 的部分。

![image 1](assets/imageFile1.png)

- 图 1 分年代的Heap结构 ⽽G1 GC则完全改变了这⼀传统思路。它将整个Heap分为若⼲个预先设定的⼩区域块（如图2），每个 区域块内部不再进⾏新旧分区， ⽽是将整个区域块标记为Eden/Survivor/Old。当创建新对象时，它⾸ 先被存放到某⼀个可⽤区块（Region）中。当该区块满了，JVM就会创建新的区块存放对象。当发⽣ minor GC时，JVM将⼀个或⼏个区块中存活的对象拷⻉到⼀个新的区块中，并在空余的空间中选择⼏ 个全新区块作为新的Eden分区。当所有区域中都有存活对象，找不到全空区块时，才发⽣Ful GC。⽽ 在标记存活对象时，G1使⽤RememberSet的概念，将每个分区外指向分区内的引⽤记录在该分区的 RememberSet中，避免了对整个Heap的扫描，使得各个分区的GC更加独⽴。在这样的背景下，我们 可以看出G1 GC⼤⼤提⾼了触发Ful GC时的Heap占⽤率，同时也使得Minor GC的暂停时间更加可控， 对于内存较⼤的环境⾮常友好。这些颠覆性的改变，将给GC性能带来怎样的变化呢？最简单的⽅式， 我们可以将⽼的GC设置直接迁移为G1 GC，然后观察性能变化。
- 图 2 G1 Heap结构示意


![image 2](assets/imageFile2.png)

由于G1取消了对于heap空间不同新旧对象固定分区的概念，所以我们需要在GC配置选项上作相应的调 整，使得应⽤能够合理地运⾏在G1 GC收集器上。⼀般来说，对于原运⾏在Paralel GC上的应⽤，需要 去除的参数包括-Xmn, -X:-UseAdaptiveSizePolicy, -X SurvivorRatio=n等；⽽对于原来使⽤ CMS GC的应⽤，我们需要去掉-Xmn -X InitialSurvivorRatio -X SurvivorRatio -

X InitialTenuringThreshold -X MaxTenuringThreshold等参数。另外在CMS中已经调优过的X ParalelGCThreads -X ConcGCThreads参数最好也移除掉，因为对于CMS来说性能最好的不⼀ 定是对于G1性能最好的选择。我们先统⼀置为默认值，⽅便后期调优。此外，当应⽤开启的线程较多 时，最好使⽤-X:-ResizePLAB来关闭PLAB()的⼤⼩调整，以避免⼤量的线程通信所导致的性能下 降。 关于Hotspot JVM所⽀持的完整的GC参数列表，可以使⽤参数-X:+PrintFlagsFinal打印出来，也可以 参⻅Oracle官⽅的⽂档中对部分参数的解释。

# Spark的内存管理

Spark的核⼼概念是RD，实际运⾏中内存消耗都与RD密切相关。Spark允许⽤户将应⽤中重复使⽤ 的RD数据持久化缓存起来，从⽽避免反复计算的开销，⽽RD的持久化形态之⼀就是将全部或者部 分数据缓存在JVM的Heap中。Spark Executor会将JVM的heap空间⼤致分为两个部分，⼀部分⽤来存 放Spark应⽤中持久化到内存中的RD数据，剩下的部分则⽤来作为JVM运⾏时的堆空间，负责RD转 化等过程中的内存消耗。我们可以通过spark.storage.memoryFraction参数调节这两块内存的⽐例， Spark会控制缓存RD总⼤⼩不超过heap空间体积乘以这个参数所设置的值，⽽这块缓存RD的空间 中没有使⽤的部分也可以为JVM运⾏时所⽤。因此，分析Spark应⽤GC问题时应当分别分析两部分内 存的使⽤情况。 ⽽当我们观察到GC延迟影响效率时，应当先检查Spark应⽤本身是否有效利⽤有限的内存空间。RD 占⽤的内存空间⽐较少的话，程序运⾏的heap空间也会⽐较宽松，GC效率也会相应提⾼；⽽RD如果 占⽤⼤量空间的话，则会带来巨⼤的性能损失。下⾯我们从⼀个⽤户案例展开： 该应⽤是利⽤Spark的组件Bagel来实现的，其本质就是⼀个简单的迭代计算。⽽每次迭代计算依赖于 上⼀次的迭代结果，因此每次迭代结果都会被主动持续化到内存空间中。当运⾏⽤户程序时，我们观 察到随着迭代次数的增加，进程占⽤的内存空间不断快速增⻓，GC问题越来越突出。但是，仔细分析 Bagel实现机制，我们很快发现Bagel将每次迭代产⽣的RD都持久化下来了，⽽没有及时释放掉不再 使⽤的RD，从⽽造成了内存空间不断增⻓，触发了更多GC执⾏。经过简单的修改，我们修复了这个 问题（SPARK-261）。应⽤的内存空间得到了有效的控制后，迭代次数三次以后RD⼤⼩趋于稳定， 缓存空间得到有效控制（如表1所示），GC效率得以⼤⼤提⾼，程序总的运⾏时间缩短了10%~20%。

![image 3](assets/imageFile3.png)

⼩结：当观察到GC频繁或者延时⻓的情况，也可能是Spark进程或者应⽤中内存空间没有有效利⽤。 所以可以尝试检查是否存在RD持久化后未得到及时释放等情况。

# 选择垃圾收集器

在解决了应⽤本身的问题之后，我们就要开始针对Spark应⽤的GC调优了。基于修复了SPARK-261的 Spark版本，我们搭建了⼀个4个节点的集群，给每个Executor分配 8G的Heap，在Spark的 Standalone模式下来进⾏我们的实验。在使⽤默认的Paralel GC运⾏我们的Spark应⽤时，我们发现， 由于Spark应⽤对于内存的开销⽐较⼤，⽽且⼤部分对象并不能在⼀个较短的⽣命周期中被回收， Paralel GC也常常受困于Ful GC，⽽每次Ful GC都给性能带来了较⼤的下降。⽽Paralel GC可以进⾏ 参数调优的空间也⾮常有限，我们只能通过调节⼀些基本参数来提⾼性能，如各年代分区⼤⼩⽐例、 进⼊⽼年代前的拷⻉次数等。⽽且这些调优策略只能推迟Ful GC的到来，如果是⻓期运⾏的应⽤， Paralel GC调优的意义就⾮常有限了。因此，本⽂中不会再对Paralel GC进⾏调优。表2列出了 Paralel GC的运⾏情况，其中CPU利⽤率较低的部分正是发⽣Ful GC的时候。

<table>
  <tr>
    <th>Configuration Options</th>
    <th>-XX:+UseParallelGC -<br><br>XX:+UseParallelOldGC XX:+PrintFlagsFinal XX:+PrintReferenceGC -verbose:gc XX:+PrintGCDetails XX:+PrintGCTimeStamps XX:+PrintAdaptiveSizePolicy -Xms88g<br><br>-Xmx88g<br></th>
  </tr>
  <tr>
    <td>Stage*</td>
    <td>![image 4](assets/imageFile4.png)</td>
  </tr>
  <tr>
    <td>Task*</td>
    <td>![image 5](assets/imageFile5.png)</td>
  </tr>
  <tr>
    <td>CPU*</td>
    <td>![image 6](assets/imageFile6.png)</td>
  </tr>
  <tr>
    <td>Mem*</td>
    <td>![image 7](assets/imageFile7.png)</td>
  </tr>
</table>


## Paralel GC运⾏情况(未调优) ⾄于CMS GC，也没有办法消除这个Spark应⽤中的Ful GC，⽽且CMS的Ful GC的暂停时间远远超过 了Paralel GC，⼤⼤拖累了该应⽤的吞吐量。

接下来，我们就使⽤最基本的G1 GC配置来运⾏我们的应⽤。实验结果发现，G1 GC竟然也出现了不可 忍受的Ful GC（表3的CPU利⽤率图中，可以明显发现Job 3中出现了将近10秒的暂停），超⻓的暂 停时间⼤⼤拖累了整个应⽤的运⾏。如表4所示，虽然总的运⾏时间⽐Paralel GC略⻓，不过G1 GC表 现略好于CMS GC。

<table>
  <tr>
    <th>Configuration Options</th>
    <th>-X:+UseG1GC -X:+PrintFlagsFinal X: rnReferenceGC -verbose:gc X:+PrintGCDetails -X:+PrintGCTimeStamps XPrintAdaptiveSizePolicy XUnlockDi gnosticVOptions X:+G1SumarizeConcMark -Xms8g -</th>
  </tr>
  <tr>
    <td>Stage*</td>
    <td>Xmx8g<br><br>![image 8](assets/imageFile8.png)</td>
  </tr>
  <tr>
    <td>Task*</td>
    <td>![image 9](assets/imageFile9.png)</td>
  </tr>
  <tr>
    <td>CPU*</td>
    <td>![image 10](assets/imageFile10.png)</td>
  </tr>
  <tr>
    <td>Mem*</td>
    <td>![image 11](assets/imageFile11.png)</td>
  </tr>
</table>


表 3 G1 GC运⾏情况(未调优)

![image 12](assets/imageFile12.png)

表 4 三种垃圾收集器对应的程序运⾏时间⽐较（ 8GB heap未调优）

# 根据⽇志进⼀步调优

在让G1 GC跑起来之后，我们下⼀步就是需要根据GC log，来进⼀步进⾏性能调优。⾸先，我们要让 JVM记录⽐较详细的GC⽇志. 对于Spark⽽⾔，我们需要在SPARK_JAVA_OPTS中设置参数使得Spark 保留下我们需要⽤到的⽇志. ⼀般⽽⾔，我们需要设置这样⼀串参数：

-X:+PrintFlagsFinal -X:+PrintReferenceGC -verbose:gc -X:+PrintGCDetails X:+PrintGCTimeStamps -X:+PrintAdaptiveSizePolicy -X:+UnlockDiagnosticVMOptions X:+G1SumarizeConcMark

有了这些参数，我们就可以在SPARK的EXECUTOR⽇志中（默认输出到各worker节点的 $SPARK_HOME/work/$ap_id/$executor_id/stdout中）读到详尽的GC⽇志以及⽣效的GC 参数了。 接下来，我们就可以根据GC⽇志来分析问题，使程序获得更优性能。我们先来了解⼀下G1中⼀次GC 的⽇志结构。

251.354: [G1Ergonomics (Mixed GCs) continue mixed GCs, reason: candidate old regions available, candidate old regions: 363 regions, reclaimable: 9830652576 bytes (10.40 %), threshold: 10.00 %]

- 1

- 2

- 3 [Parallel Time: 145.1 ms, GC Workers: 23]

- 4

- 5 [GC Worker Start (ms): Min: 251176.0, Avg: 251176.4, Max: 251176.7, Diff: 0.7]

- 6

- 7 [Ext Root Scanning (ms): Min: 0.8, Avg: 1.2, Max: 1.7, Diff: 0.9, Sum: 28.1]

- 8

- 9 [Update RS (ms): Min: 0.0, Avg: 0.3, Max: 0.6, Diff: 0.6, Sum: 5.8]

- 10

- 11 [Processed Buffers: Min: 0, Avg: 1.6, Max: 9, Diff: 9, Sum: 37]

- 12

- 13 [Scan RS (ms): Min: 6.0, Avg: 6.2, Max: 6.3, Diff: 0.3, Sum: 143.0]

- 14

- 15 [Object Copy (ms): Min: 136.2, Avg: 136.3, Max: 136.4, Diff: 0.3, Sum: 3133.9]

- 16

- 17 [Termination (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.3]

- 18

- 19 [GC Worker Other (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 1.9]

- 20

[GC Worker Total (ms): Min: 143.7, Avg: 144.0, Max: 144.5, Diff: 0.8, Sum: 3313.0]

- 21

- 22

- 23 [GC Worker End (ms): Min: 251320.4, Avg: 251320.5, Max: 251320.6, Diff: 0.2]

- 24

- 25 [Code Root Fixup: 0.0 ms]

- 26

- 27 [Clear CT: 6.6 ms]

- 28

- 29 [Other: 26.8 ms]

- 30

- 31 [Choose CSet: 0.2 ms]

- 32

- 33 [Ref Proc: 16.6 ms]

- 34

- 35 [Ref Enq: 0.9 ms]

- 36

- 37 [Free CSet: 2.0 ms]


[Eden: 3904.0M(3904.0M)->0.0B(4448.0M) Survivors: 576.0M->32.0M Heap: 63.7G(88.0G)->58.3G(88.0G)]

- 39

- 40

- 41 [Times: user=3.43 sys=0.01, real=0.18 secs]


以G1 GC的⼀次mixed GC为例，从这段⽇志中，我们可以看到G1 GC⽇志的层次是⾮常清晰的。⽇志 列出了这次暂停发⽣的时间、原因，并分级各种线程所消耗的时⻓以及CPU时间的均值和最值。最 后，G1 GC列出了本次暂停的清理结果，以及总共消耗的时间。 ⽽在我们现在的G1 GC运⾏⽇志中，我们明显发现这样⼀段特殊的⽇志：

- 1 (to-space exhausted), 1.0552680 secs]

- 2

- 3 [Parallel Time: 958.8 ms, GC Workers: 23]

- 4

- 5 [GC Worker Start (ms): Min: 759925.0, Avg: 759925.1, Max: 759925.3, Diff: 0.3]

- 6

- 7 [Ext Root Scanning (ms): Min: 1.1, Avg: 1.4, Max: 1.8, Diff: 0.6, Sum: 33.0]

- 8

- 9 [SATB Filtering (ms): Min: 0.0, Avg: 0.0, Max: 0.3, Diff: 0.3, Sum: 0.3]

- 10

- 11 [Update RS (ms): Min: 0.0, Avg: 1.2, Max: 2.1, Diff: 2.1, Sum: 26.9]

- 12

- 13 [Processed Buffers: Min: 0, Avg: 2.8, Max: 11, Diff: 11, Sum: 65]

- 14

- 15 [Scan RS (ms): Min: 1.6, Avg: 2.5, Max: 3.0, Diff: 1.4, Sum: 58.0]

- 16

- 17 [Object Copy (ms): Min: 952.5, Avg: 953.0, Max: 954.3, Diff: 1.7, Sum: 21919.4]

- 18

- 19 [Termination (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 2.2]

- 20

- 21 [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.6]

- 22

[GC Worker Total (ms): Min: 958.1, Avg: 958.3, Max: 958.4, Diff: 0.3, Sum: 22040.4]

- 23

- 24

- 25 [GC Worker End (ms): Min: 760883.4, Avg: 760883.4, Max: 760883.4, Diff: 0.0]

- 26

- 27 [Code Root Fixup: 0.0 ms]

- 28

- 29 [Clear CT: 0.4 ms]

- 30

- 31 [Other: 96.0 ms]

- 32

- 33 [Choose CSet: 0.0 ms]

- 34

- 35 [Ref Proc: 0.4 ms]

- 36

- 37 [Ref Enq: 0.0 ms]

- 38

- 39 [Free CSet: 0.1 ms]


[Eden: 160.0M(3904.0M)->0.0B(4480.0M) Survivors: 576.0M->0.0B Heap: 87.7G(88.0G)->87.7G(88.0G)]

- 41

- 42

- 43 [Times: user=1.69 sys=0.24, real=1.05 secs]

- 44

760.981: [G1Ergonomics (Heap Sizing) attempt heap expansion, reason: allocation request failed, allocation request: 90128 bytes]

- 45

- 46

760.981: [G1Ergonomics (Heap Sizing) expand the heap, requested expansion amount: 33554432 bytes, attempted expansion amount: 33554432 bytes]

- 47

- 48

760.981: [G1Ergonomics (Heap Sizing) did not expand the heap, reason: heap expansion operation failed]

- 49

- 50

- 51 760.981: [Full GC 87G->36G(88G), 67.4381220 secs]


显然最⼤的性能下降是这样的Ful GC导致的，我们可以在⽇志中看到类似To-space Exhausted或者 To-space Overflow这样的输出（取决于不同版本的JVM，输出略有不同）。这是G1 GC收集器在将某 个需要垃圾回收的分区进⾏回收时，⽆法找到⼀个能将其中存活对象拷⻉过去的空闲分区。这种情况 被称为Evacuation Failure，常常会引发Ful GC。⽽且很显然，G1 GC的Ful GC效率相对于Paralel GC 实在是相差太远，我们想要获得⽐Paralel GC更好的表现，⼀定要尽⼒规避Ful GC的出现。对于这种 情况，我们常⻅的处理办法有两种：

- 1.
- 2.


将InitiatingHeapOcupancyPercent参数调低（默认值是45），可以使G1 GC收集器更早开始 Mixed GC；但另⼀⽅⾯，会增加GC发⽣频率。 提⾼ConcGCThreads的值，在Mixed GC阶段投⼊更多的并发线程，争取提⾼每次暂停的效率。 但是此参数会占⽤⼀定的有效⼯作线程资源。

调试这两个参数可以有效降低Ful GC出现的概率。Ful GC被消除之后，最终的性能获得了⼤幅提升。 但是我们发现，仍然有⼀些地⽅GC产⽣了⼤量的暂停时间。⽐如，我们在⽇志中读到很多类似这样的 ⽚断：

280.08: [G1Ergonomics (Concurent Cycles) request concurent cycle initiation, reason: ocupancy higher than threshold, ocupancy: 6234134656 bytes, alocation request: 46137368 bytes, threshold: 4252017625 bytes (45.0 %), source: concurent humongous alocation]

这⾥就是Humongous object，⼀些⽐G1的⼀个分区的⼀半更⼤的对象。对于这些对象，G1会专⻔在 Heap上开出⼀个个Humongous Area来存放，每个分区只放⼀个对象。但是申请这么⼤的空间是⽐较 耗时的，⽽且这些区域也仅当Ful GC时才进⾏处理，所以我们要尽量减少这样的对象产⽣。或者提⾼ G1HeapRegionSize的值减少HumongousArea的创建。不过在内存⽐较⼤的时，JVM默认把这个值设 到了最⼤(32M)，此时我们只能通过分析程序本身找到这些对象并且尽量减少这样的对象产⽣。当然， 相信随着G1 GC的发展，在后期的版本中相信这个最⼤值也会越来越⼤，毕竟G1号称是在1024～2048 个Region时能够获得最佳性能。 接下来，我们可以分析⼀下单次cycle start到Mixed GC为⽌的时间间隔。如果这⼀时间过⻓，可以考 虑进⼀步提升ConcGCThreads，需要注意的是，这会进⼀步占⽤⼀定CPU资源。 对于追求更短暂停时间的在线应⽤，如果观测到较⻓的Mixed GC pause，我们还要把 G1RSetUpdatingPauseTimePercent调低，把G1ConcRefinementThreads调⾼。前⽂提到G1 GC通过 为每个分区维护RememberSet来记录分区外对分区内的引⽤，G1RSetUpdatingPauseTimePercent则 正是在STW阶段为G1收集器指定更新RememberSet的时间占总STW时间的期望⽐例，默认为10。⽽ G1ConcRefinementThreads则是在程序运⾏时维护RememberSet的线程数⽬。通过对这两个值的对应 调整，我们可以把STW阶段的RememberSet更新⼯作压⼒更多地移到Concurent阶段。 另外，对于需要⻓时间运⾏的应⽤，我们不妨加上AlwaysPreTouch参数，这样JVM会在启动时就向OS 申请所有需要使⽤的内存，避免动态申请，也可以提⾼运⾏时性能。但是该参数也会⼤⼤延⻓启动时 间。 最终，经过⼏轮GC参数调试，其结果如下表5所示。较之先前的结果，我们最终还是获得了较满意的 运⾏效率。

<table>
  <tr>
    <th>Configuration Options</th>
    <th>-XX:+UseG1GC -XX:+PrintFlagsFinal -<br><br>XX:+PrintReferenceGC -verbose:gc XX:+PrintGCDetails XX:+PrintGCTimeStamps XX:+PrintAdaptiveSizePolicy XX:+UnlockDiagnosticVMOptions XX:+G1SummarizeConcMark -Xms88g Xmx88g XX:InitiatingHeapOccupancyPercent=35<br><br>-XX:ConcGCThread=20<br></th>
  </tr>
  <tr>
    <td>Stage*</td>
    <td>![image 13](assets/imageFile13.png)</td>
  </tr>
  <tr>
    <td>Task*</td>
    <td>![image 14](assets/imageFile14.png)</td>
  </tr>
  <tr>
    <td>CPU*</td>
    <td>![image 15](assets/imageFile15.png)</td>
  </tr>
  <tr>
    <td>Mem*</td>
    <td>![image 16](assets/imageFile16.png)</td>
  </tr>
</table>


表 5 使⽤G1 GC调优完成后的表现 ⼩结：综合考虑G1 GC是较为推崇的默认Spark GC机制。进⼀步的GC⽇志分析，可以收获更多的GC 优化。经过上⾯的调优过程，我们将该应⽤的运⾏时间缩短到了4.3分钟，相⽐调优之前，我们获得了 1.7倍左右的性能提升，⽽相⽐Paralel GC也获得了1.5倍左右的性能提升。

# 总结

对于⼤量依赖于内存计算的Spark应⽤，GC调优显得尤为重要。在发现GC问题的时候，不要着急调试 GC。⽽是先考虑是否存在Spark进程内存管理的效率问题，例如RD缓存的持久化和释放。⾄于GC参 数的调试，⾸先我们⽐较推荐使⽤G1 GC来运⾏Spark应⽤。相较于传统的垃圾收集器，随着G1的不断 成熟，需要配置的选项会更少，能同时满⾜⾼吞吐量和低延迟的寻求。当然，GC的调优不是绝对的， 不同的应⽤会有不同应⽤的特性，掌握根据GC⽇志进⾏调优的⽅法，才能以不变应万变。最后，也不 能忘了先对程序本身的逻辑和代码编写进⾏考量，例如减少中间变量的创建或者复制，控制⼤对象的 创建，将⻓期存活对象放在Of-heap中等等。
