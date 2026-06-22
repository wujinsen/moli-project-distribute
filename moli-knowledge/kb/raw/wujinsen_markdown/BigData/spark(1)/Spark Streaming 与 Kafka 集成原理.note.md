Spark Streaming 与 Kafka 集成接受数据的⽅式有两种：

- 1.
- 2.


Receiver-based Aproach Direct Aproach (No Receivers)

我们会对这两种⽅案做详细的解析，同时对⽐两种⽅案优劣。选型后，我们针对Direct Aproach (No Receivers)模式详细介绍其如何实现Exactly Once Semantics，也就是保证接收到的数据只被处理⼀ 次，不丢，不重。

# Receiver-based Aproach

要描述清楚 Receiver-based Aproach ，我们需要了解其接收流程，分析其内存使⽤，以及相关参数 配置对内存的影响。

- * 数据接收流程 * 启动Spark Streaming(后续缩写为 S)后， S 会选择⼀台Executor 启动ReceiverSupervisor,并且标记 为Active状态。接着按如下步骤处理：

到⽬前为⽌，整个过程不会有太多内存消耗，正常的⼀个线性调⽤。所有复杂的数据结构都隐含 在 BlockGenerator 中。

- * BlockGenerator 存储结构 * BlockGenerator 会复杂些，重要的数据存储结构有四个：


- 1.
- 2.
- 3.


ReceiverSupervisor会启动对应的Receiver(这⾥是KafkaReceiver) KafkaReceiver 会根据配置启动新的线程接受数据，在该线程中调 ⽤ ReceiverSupervisor.store ⽅法填充数据，注意，这⾥是⼀条⼀条填充的。

ReceiverSupervisor 会调⽤ BlockGenerator.addData 进⾏数据填充。

- 1.
- 2.
- 3.
- 4.


维护了⼀个缓存 currentBuffer ，就是⼀个⽆限⻓度的ArayBufer。 currentBuffer 并不会 被复⽤，⽽是每次都会新建，然后把⽼的对象直接封装成Block，BlockGenerator会负责保证

currentBuffer 只有⼀个。 currentBuffer 填充的速度是可以被限制的，以秒为单位，配置参 数为 spark.streaming.receiver.maxRate 。这个是Spark内存控制的第⼀道防线，填充

currentBuffer 是阻塞的，消费Kafka的线程直接做填充。 维护了⼀个 blocksForPushing 队列， size 默认为10个(1.5.1版本)，可通 过 spark.streaming.blockQueueSize 进⾏配置。该队列主要⽤来实现⽣产-消费模式。每个元素 其实是⼀个curentBufer形成的block。 blockIntervalTimer 是⼀个定时器。其实是⼀个⽣产者，负责将 currentBuffer 的数据放到

blocksForPushing 中。通过参数 spark.streaming.blockInterval 设置，默认为20ms。放 的⽅式很简单，直接把curentBufer做为Block的数据源。这就是为什么curentBufer不会被复 ⽤。

blockPushingThread 也是⼀个定时器，负责将Block从 blocksForPushing 取出来,然后交给 BlockManagerBasedBlockHandler.storeBlock ⽅法。10毫秒会取⼀次，不可配置。到这⼀步，

才真的将数据放到了Spark的BlockManager中。

下⾯我们会详细分析每⼀个存储对象对内存的使⽤情况：

- * curentBufer * ⾸先⾃然要说下curentBufer,如果20ms期间你从Kafka接受的数据⾜够⼤，则⾜以把内存承包了。 ⽽且curentBufer使⽤的并不是spark的storage内存，⽽是有限的⽤于运算存储的内存。 默认应该是 heap*0.4。除了把内存搞爆掉了，还有⼀个是GC。导致receiver所在的Executor 极容易挂掉，处理速 度也巨慢。 如果你在SparkUI发现Receiver挂掉了，考虑有没有可能是这个问题。
- * blocksForPushing * blocksForPushing 这个是作为 currentBuffer 和BlockManager之间的中转站。默认存储的数据最


⼤可以达到 10*currentBuffer ⼤⼩。⼀般不打可能，除⾮你 的 spark.streaming.blockInterval 设置的⽐10ms 还⼩，官⽅推荐最⼩也要设置成 50ms，你就不 要搞对抗了。所以这块不⽤太担⼼。

* blockPushingThread * blockPushingThread 负责从 blocksForPushing 获取数据，并且写⼊ BlockManager 。 blockPushingThread 只写他⾃⼰所在的Executor的 blockManager ,也就是每个batch周期的数据都 会被 ⼀个Executor给扛住了。 这是导致内存被撑爆的最⼤⻛险。 建议每个batch周期接受到的数据最 好不要超过接受Executor的内存(Storage)的⼀半。否则在数据量很⼤的情况下，会导致Receiver所在 的Executor直接挂掉。 对应的解决⽅案是使⽤多个Receiver来消费同⼀个topic,使⽤类似下⾯的代码

- 1 val kafkaDStreams = (1 to kafkaDStreamsNum).map { _ => KafkaUtils.createStream(

- 2 ssc,

- 3 zookeeper,

- 4 groupId,

- 5 Map("your topic" -> 1),

if (memoryOnly) StorageLevel.MEMORY_ONLY else StorageLevel.MEMORY_AND_DISK_SER_2)}

- 6

- 7 val unionDStream = ssc.union(kafkaDStreams)

- 8 unionDStream


1 * 动态控制消费速率以及相关论⽂ *

前⾯我们提到， S的消费速度可以设置上限，其实 S也可以根据之前的周期处理情况来⾃动调整下⼀ 个周期处理的数据量。你可以通过将 spark.streaming.backpressure.enabled 设置为true 打开该功 能。算法的论⽂可参考： Soc 2014: ,还是 有⽤的，我现在也都开启着。

Adaptive Stream Procesing using Dynamic Batch Sizing

另外值得提及的是，Spark⾥除了这个 Dynamic ,还有⼀个就是 Dynamic Allocation ,也就是 Executor数量会根据资源使⽤情况，⾃动伸缩。我其实蛮喜欢Spark这个特⾊的。具体的可以查找下相 关设计⽂档。

# Direct Aproach (No Receivers)

个⼈认为，DirectAproach 更符合Spark的思维。我们知道，RD的概念是⼀个不变的，分区的数据 集合。我们将kafka数据源包裹成了⼀个KafkaRD,RD⾥的partition 对应的数据源为kafka的 partition。唯⼀的区别是数据在Kafka⾥⽽不是事先被放到Spark内存⾥。其实包括FileInputStream⾥ 也是把每个⽂件映射成⼀个RD,⽐较好奇，为什么⼀开始会有Receiver-based Aproach，额外添加 了 Receiver 这么⼀个概念。

* DirectKafkaInputDStream * Spark Streaming通过Direct Aproach接收数据的⼊⼝⾃然是 KafkaUtils.createDirectStream 了。 在调⽤该⽅法时，会先创建

- 1 val kc = new KafkaCluster(kafkaParams)

- 2


KafkaCluster 这个类是真实负责和Kafka 交互的类，该类会获取Kafka的partition信息,接着会创建 DirectKafkaInputDStream ,每个 DirectKafkaInputDStream 对应⼀个Topic。 此时会获取每个Topic

的每个Partition的ofset。 如果配置成 smallest 则拿到最早的ofset,否则拿最近的ofset。 每个 DirectKafkaInputDStream 也会持有⼀个KafkaCluster实例。 到了计算周期后，对应的 DirectKafkaInputDStream .compute ⽅法会被调⽤,此时做下⾯⼏个操作：

- 1.
- 2.
- 3.
- 4.


获取对应Kafka Partition的 untilOffset 。这样就确定过了需要获取数据的区间，同时也就知道 了需要计算多少数据了 构建⼀个KafkaRD实例。这⾥我们可以看到，每个计算周期⾥，

DirectKafkaInputDStream 和 KafkaRDD 是⼀⼀对应的 将相关的ofset信息报给InputInfoTracker 返回该RD

* KafkaRD 的组成结构 * KafkaRD 包含 N(N=Kafka的partition数⽬)个 KafkaRDPartition,每个KafkaRDPartition 其实只是包 含⼀些信息，譬如topic,ofset等，真正如果想要拉数据， 是透过KafkaRDIterator 来完成，⼀个

KafkaRDDIterator 对应⼀个 KafkaRDDPartition 。 整个过程都是延时过程，也就是数据其实都在Kafka存着呢，直到有实际的Action被触发，才会有去 kafka主动拉数据。

* 限速 * Direct Aproach (NoReceivers) 的接收⽅式也是可以限制接受数据的量的。你可以通过设置

spark.streaming.kafka.maxRatePerPartition 来完成对应的配置。需要注意的是，这⾥是对每个 Partition进⾏限速。所以你需要事先知道Kafka有多少个分区，才好评估系统的实际吞吐量，从⽽设置 该值。 相应的， spark.streaming.backpressure.enabled 参数在Direct Aproach 中也是继续有效的。

# Direct Aproach VS Receiver-based Aproach

经过上⾯对两种数据接收⽅案的介绍，我们发现， Receiver-based Aproach 存在各种内存折腾，对应的Direct Aproach (No Receivers)则显得⽐较纯 粹简单些，这也给其带来了较多的优势，主要有如下⼏点：

- 1.
- 2.
- 3.
- 4.


因为按需拉数据，所以不存在缓冲区，就不⽤担⼼缓冲区把内存撑爆了。这个在Receiver-based Aproach 就⽐较麻烦，你需要通过 spark.streaming.blockInterval 等参数来调整。 数据默认就被分布到了多个Executor上。Receiver-based Aproach 你需要做特定的处理，才能 让 Receiver分不到多个Executor上。 Receiver-based Aproach 的⽅式，⼀旦你的Batch Procesing 被delay了，或者被delay了很多 个batch,那估计你的Spark Streaming程序离奔溃也就不远了。 Direct Aproach (No Receivers) 则完全不会存在类似问题。就算你delay了很多个batch time,你内存中的数据只有这次处理的。 Direct Aproach (No Receivers) 直接维护了 Kafka ofset,可以保证数据只有被执⾏成功了，才会 被记录下来，透过 checkpoint 机制。如果采⽤Receiver-based Aproach，消费Kafka和数据处 理是被分开的，这样就很不好做容错机制，⽐如系统当掉了。所以你需要开启WAL,但是开启WAL 带来⼀个问题是，数据量很⼤，对HDFS是个很⼤的负担，⽽且也会对实时程序带来⽐较⼤延迟。

我原先以为Direct Aproach 因为只有在计算的时候才拉取数据，可能会⽐Receiver-based Aproach 的⽅式慢，但是经过我⾃⼰的实际测试，总体性能 Direct Aproach会更快些，因为Receiver-based Aproach可能会有较⼤的内存隐患，GC也会影响整体处理速度。

如何保证数据接受的可靠性

S ⾃身可以做到 at least once 语义,具体⽅式是通过CheckPoint机制。

* CheckPoint 机制 * CheckPoint 会涉及到⼀些类，以及他们之间的关系：

DStreamGraph 类负责⽣成任务执⾏图，⽽ JobGenerator 则是任务真实的提交者。任务的数据源则 来源于 DirectKafkaInputDStream ，checkPoint ⼀些相关信息则是由类

DirectKafkaInputDStreamCheckpointData 负责。 好像涉及的类有点多，其实没关系，我们完全可以不⽤关⼼他们。先看看checkpoint都⼲了些啥， checkpoint 其实就序列化了⼀个类⽽已：

- 1 org.apache.spark.streaming.Checkpoint

- 2


看看类成员都有哪些：

- 1 val master = ssc.sc.master

- 2 val framework = ssc.sc.appName

- 3 val jars = ssc.sc.jars

- 4 val graph = ssc.graph

- 5 val checkpointDir = ssc.checkpointDir

val checkpointDuration = ssc.checkpointDurationval pendingTimes = ssc.scheduler.getPendingTimes().toArray

- 6

- 7 val delaySeconds = MetadataCleaner.getDelaySeconds(ssc.conf)

- 8 val sparkConfPairs = ssc.conf.getAll


1 其他的都⽐较容易理解，最重要的是 graph，该类全路径名是：

- 1 org.apache.spark.streaming.DStreamGraph

- 2


⾥⾯有两个核⼼的数据结构是：

- 1 private val inputStreams = new ArrayBuffer[InputDStream[_]]()

- 2 private val outputStreams = new ArrayBuffer[DStream[_]]()


1 inputStreams 对应的就是 DirectKafkaInputDStream 了。

再进⼀步， DirectKafkaInputDStream 有⼀个重要的对象

protected[streaming] override val checkpointData = new DirectKafkaInputDStreamCheckpointData

1

1 checkpointData ⾥则有⼀个data 对象，⾥⾯存储的内容也很简单

1 data.asInstanceOf[mutable.HashMap[Time, Array[OffsetRange.OffsetRangeTuple]]]

1 就是每个batch 的唯⼀标识 time 对象，以及每个KafkaRDD对应的的Kafka偏移信息。

⽽ outputStreams ⾥则是RD,如果你存储的时候做了foreach操作，那么应该就是 ForEachRDD 了， 他被序列化的时候是不包含数据的。 ⽽downtime由checkpoint 时间决定,pending time之类的也会被序列化。 经过上⾯的分析，我们发现：

- 1.
- 2.


checkpoint 是⾮常⾼效的。没有涉及到实际数据的存储。⼀般⼤⼩只有⼏⼗K，因为只存了Kafka 的偏移量等信息。 checkpoint 采⽤的是序列化机制，尤其是DStreamGraph的引⼊，⾥⾯包含了可能如ForeachRD 等，⽽ForeachRD⾥⾯的函数应该也会被序列化。如果采⽤了CheckPoint机制，⽽你的程序包 做了做了变更，恢复后可能会有⼀定的问题。

接着我们看看 JobGenerator 是怎么提交⼀个真实的batch任务的，分析在什么时间做checkpoint 操 作，从⽽保证数据的⾼可⽤：

- 1.
- 2.
- 3.
- 4.
- 5.


产⽣jobs 成功则提交jobs 然后异步执⾏ 失败则会发出⼀个失败的事件 ⽆论成功或者失败，都会发出⼀个 DoCheckpoint 事件。 当任务运⾏完成后，还会再调⽤⼀次 DoCheckpoint 事件。

只要任务运⾏完成后没能顺利执⾏完 DoCheckpoint 前crash,都会导致这次Batch被重新调度。也就说 ⽆论怎样，不存在丢数据的问题，⽽这种稳定性是靠checkpoint 机制以及Kafka的可回溯性来完成的。 那现在会产⽣⼀个问题，假设我们的业务逻辑会对每⼀条数据都处理，则

- 1.
- 2.
- 3.


我们没有处理⼀条数据 我们可能只处理了部分数据 我们处理了全部数据

根据我们上⾯的分析，⽆论如何，这次失败了，都会被重新调度，那么我们可能会重复处理数据，可 能最后失败的那⼀次数据的⼀部分，也可能是全部，但不会更多了。

* 业务需要做事务，保证 Exactly Once 语义 * 这⾥业务场景被区分为两个：

- 1.
- 2.


幂等操作 业务代码需要⾃身添加事物操作

所谓幂等操作就是重复执⾏不会产⽣问题，如果是这种场景下，你不需要额外做任何⼯作。但如果你 的应⽤场景是不允许数据被重复执⾏的，那只能通过业务⾃身的逻辑代码来解决了。 这个 S 倒是也给出了官⽅⽅案：

- 1 dstream.foreachRDD { (rdd, time) =>

- 2 rdd.foreachPartition { partitionIterator =>

- 3 val partitionId = TaskContext.get.partitionId()

- 4 val uniqueId = generateUniqueId(time.milliseconds, partitionId)

- 5 // use this uniqueId to transactionally commit the data in partitionIterator

- 6 }

- 7 }


这代码啥含义呢？ 就是说针对每个partition的数据，产⽣⼀个uniqueId,只有这个partion的所有数据 被完全消费，则算成功，否则算失败，要回滚。下次重复执⾏这个uniqueId 时，如果已经被执⾏成功过 的，则skip掉。

1

这样，就能保证数据 Exactly Once 语义啦。

总结

根据我的实际经验，⽬前Direct Aproach 稳定性个⼈感觉⽐ Receiver-based Aproach 更好些，推 荐使⽤ Direct Aproach ⽅式和Kafka进⾏集成,并且开启响应的checkpoint 功能，保证数据接收的稳 定性，Direct Aproach 模式本身可以保证数据 at least once语义，如果你需要Exactly Once 语义 时，需要保证你的业务是幂等，或者保证了相应的事务。

