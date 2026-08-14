---
title: Spark技术内幕：Sort Based Shuffle实现解析.note（原文插图 annex）
slug: annex-Spark技术内幕：Sort-Based-Shuffle实现解析
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Sort Based Shuffle实现解析.note.md
related: [spark-核心概念与实践]
created: 2026-07-05
updated: 2026-07-05
---

Spark技术内幕：Sort Based Shufle实现解析

分 在Spark 1.2.0中，Spark Core的⼀个重要的升级就是将默认的Hash Based Shufle换成了Sort Based Shufle，即spark.shufle.manager 从hash换成了sort，对应的实现类分别是 org.apache.spark.shufle.hash.HashShufleManager和 org.apache.spark.shufle.sort.SortShufleManager。 这个⽅式的选择是在org.apache.spark.SparkEnv完成的： [java]

view plaincopy / Let the user specify short names forshufle managers

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


val shortShufleMgrNames = Map( "hash" ->"org.apache.spark.shufle.hash.HashShufleManager", "sort" ->"org.apache.spark.shufle.sort.SortShufleManager")

val shufleMgrName =conf.get("spark.shufle.manager", "sort") /获得Shufle Manager的 type，sort为默认 val shufleMgrClas =shortShufleMgrNames.getOrElse(shufleMgrName.toLowerCase, shufle MgrName) val shufleManager =instantiateClas[ShufleManager](shufleMgrClas)

那么Sort BasedShufle“取代”Hash BasedShufle作为默认选项的原因是什么？ 正如前⾯提到的，Hashbased shufle的每个maper都需要为每个reducer写⼀个⽂件，供reducer读 取，即需要产⽣M*R个数量的⽂件，如果maper和reducer的数量⽐较⼤，产⽣的⽂件数会⾮常多。 Hash based shufle设计的⽬标之⼀就是避免不需要的排序（Hadop Map Reduce被⼈诟病的地⽅， 很多不需要sort的地⽅的sort导致了不必要的开销）。但是它在处理超⼤规模数据集的时候，产⽣了⼤ 量的DiskIO和内存的消耗，这⽆疑很影响性能。Hash based shufle也在不断的优化中，正如前⾯讲到 的Spark 0.8.1引⼊的file consolidation在⼀定程度上解决了这个问题。为了更好的解决这个问题， Spark 1.1 引⼊了Sort based shufle。⾸先，每个Shufle Map Task不会为每个Reducer⽣成⼀个单独 的⽂件；相反，它会将所有的结果写到⼀个⽂件⾥，同时会⽣成⼀个index⽂件，Reducer可以通过这 个index⽂件取得它需要处理的数据。避免产⽣⼤量的⽂件的直接收益就是节省了内存的使⽤和顺序 Disk IO带来的低延时。节省内存的使⽤可以减少GC的⻛险和频率。⽽减少⽂件的数量可以避免同时写 多个⽂件对系统带来的压⼒。 并且从作者ReynoldXin的⼏乎所有的测试来看，Sortbased shufle在速度和内存使⽤⽅⾯优于 Hashbased shufle：“sort-basedshufle has lower memory usage and sems to outperformhashbased in almost alof our testing.” 性能数据：from：

htps:/isues.apache.org/jira/browse/SPARK-3280

![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

Shufle Map Task会按照key相对应的partition ID进⾏sort，其中属于同⼀个partition的key不会sort。 因为对于不需要sort的操作来说，这个sort是负收益的；要知道之前Spark刚开始使⽤Hash based的 shufle⽽不是sort based就是为了避免Hadop Map Reduce对于所有计算都会sort的性能损耗。对于 那些需要sort的运算，⽐如sortByKey，这个sort在Spark 1.2.0⾥还是由reducer完成的。 如果这个过程内存不够⽤了，那么这些已经sort的内容会被spil到外部存储。然后在结束的时候将这些 不同的⽂件进⾏merge sort。 为了便于下游的Taskfetch到其需要的partition，这⾥会⽣成⼀个index⽂件，去记录不同的partition的 位置信息。当然了org.apache.spark.storage.BlockManager需要也有响应的实现以实现这种新的寻址 ⽅式。

![image 3](assets/imageFile3.png)

核⼼实现的逻辑都在类org.apache.spark.shufle.sort.SortShufleWriter。下⾯简要分析⼀下它的实 现：

- 1） 对于每个partition，创建⼀个scala.Aray存储它所包含的key，value对。每个待处理的key， value对都会插⼊相应的scala.Aray。
- 2） 如果scala.Aray的⼤⼩超过阈值，那么需要将这个in memory的数据spil到外部存储。这个⽂ 件的开始部分会记录这个partition的ID，这个⽂件保存了多少个pair等信息。
- 3） 最后需要将所有spil到外部存储的⽂件进⾏mergesort。同时打开的⽂件不能过多，过多的话 会消耗⼤量的内存，增加 OM或者GC的⻛险；也不能过少，过少的话就会影响性能，增⼤计算的延 时。⼀般的话推荐每次同时打开10 – 10个⽂件。
- 4） 在⽣成最后的数据⽂件时，需要同时⽣成index索引⽂件。正如前⾯提到的，这个索引⽂件将 记录不同partition的range。 当然了，你可能还有个疑问，就是Hash Based Shufle说⽩了就是根据key需要写⼊的 org.apache.spark.HashPartitioner，为每个Reducer写⼊单独的Partition。只不过对于同⼀个Core启 动的Shufle Map Task，如果选择spark.shufle.consolidateFiles的话，第⼆个Shufle Map Task会把 结果apend到上⼀个⽂件中去。那么sort的逻辑是完全可以整合到Hash Based Shufle中去，为什么 ⼜要重新实现⼀种Shufle Writer呢？我认为有以下⼏点：


Shufle机制是所有类似计算模块的核⼼机制之⼀，要进⾏⼤的优化的⻛险⾮常⾼；⽐如⼀个看似简 单的consolidation机制，在0.8.1就引⼊了，但是到1.2.0还是没有作为默认选项。

Hash Based Shufle如果修改为Sort的逻辑，所谓的改进可能会影响原来已经稳定的Spark应⽤。⽐ 如⼀个应⽤在使⽤Hash Based Shufle性能是完全符合预期的，那么迁移到Spark 1.2.0后，只需要 将配置⽂件修改以下就可以完成这个⽆缝的迁移。

作为⼀个通⽤的计算平台，你的测试的case永远cover不了所有的场景。那么，还是留给⽤户去选 择吧。

Sort的机制还处理不断完善的阶段。⽐如很有的优化或者功能的改进会不断的完善。因此，期待 Sort在以后的版本中更加完善吧。
