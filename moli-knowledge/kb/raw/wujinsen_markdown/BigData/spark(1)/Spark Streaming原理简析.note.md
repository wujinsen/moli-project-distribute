执⾏流程

数据的接收 StreamingContext实例化的时候，需要传⼊⼀个SparkContext，然后指定要连接的spark matser url， 即连接⼀个spark engine，⽤于获得executor。

实例化之后，⾸先，要指定⼀个接收数据的⽅式，如

val lines =sc.socketTextStream("localhost", 9) 这样从socket接收⽂本数据。这个步骤返回的是⼀个ReceiverInputDStream的实现，内含Receiver， 可接收数据并转化为RD放内存⾥。

ReceiverInputDStream有⼀个需要⼦类实现的⽅法

def getReceiver(): Receiver[T] ⼦类实现这个⽅法，worker节点调⽤后能得到Receiver，使得数据接收的⼯作能分布到worker上。

如果是local跑，由于Receiver接收数据在本地，所以在启动streaming aplication的时候，要注意分配 的core数⽬要⼤于Receiver数⽬，才能腾出cpu做计算任务的调度。 Receiver需要⼦类实现

def onStart() def onStop()

来定义⼀个数据接收器的初始化、接收到数据后如何存、如何在结束的时候释放资源。

Receiver提供了⼀系列store()接⼝，如store(ByteBufer)，store(Iterator)等等。这些store接⼝是实现 好了的，会由worker节点上初始化的ReceiverSupervisor来完成这些存储功能。ReceiverSupervisor还 会对Receiver做监控，如监控是否启动了、是否停⽌了、是否要重启、汇报eror等等。

ReceiverSupervisor的存储接⼝的实现，借助的是BlockManager，数据会以RD的形式被存放，根据 StorageLevel选择不同存放策略。默认是序列化后存内存，放不下的话写磁盘(executor)。被计算出来 的RD中间结果，默认存放策略是序列化后只存内存。

ReceiverSupervisor在做putBlock操作的时候，会⾸先借助BlockManager存好数据，然后往 ReceiverTracker发送⼀个AdBlock的消息。ReceiverTracker内部的ReceivedBlockTracker⽤于维护⼀ 个receiver接收到的所有block信息，即BlockInfo，所以AdBlock会把信息存放在 ReceivedBlockTracker⾥。未来需要计算的时候，ReceiverTracker根据streamId，从 ReceivedBlockTracker取出对应的block列表。

RateLimiter帮助控制Receiver速度，spark.streaming.receiver.maxRate参数。 数据源⽅⾯，普通的数据源为file, socket, aka, RDs。⾼级数据源为Twiter, Kafka, Flume等。开发 者也可以⾃⼰定制数据源。

任务调度

JobScheduler在context⾥初始化。当context start的时候，触发scheduler的start。

scheduler的start触发了ReceiverTracker和JobGenerator的start。这两个类是任务调度的重点。前者 在worker上启动Receiver接收数据，并且暴露接⼝能够根据streamId获得对应的⼀批Block地址。后者 基于数据和时间来⽣成任务描述。

JobScheduler内含⼀个线程池，⽤于调度任务执⾏。spark.streaming.concurentJobs可以控制job并 发度，默认是1，即它只能⼀个⼀个提job。

job来⾃JobGenerator⽣成的JobSet。JobGenerator根据时间，⽣成job并且执⾏cp。

JobGenerator的⽣成job逻辑：

- - 调⽤ReceiverTracker的alocateBlocksToBatch⽅法，为本批数据分配好block，即准备好数据
- - 间接调⽤DStream的generateJob(time)⽅法，制造可执⾏的RD

DStream切分RD和⽣成可执⾏的RD，即getOrCompute(time)：

- - 如果这个时间点的RD已经⽣成好了，那么从内存hashmap⾥拿出来，否则下⼀步
- - 如果时间是批次间隔的整数倍，则下⼀步，否则这个时间点不切
- - 调⽤DStream的⼦类的compute⽅法，得到RD。可能是⼀个RD，也可以是个RD列表
- - 对每个RD,调⽤persist⽅法，制定默认的存储策略。如果时间点合适，同时调⽤RD的checkpoint ⽅法，制定好cp策略
- - 得到这些RD后，调⽤SparkContext.runJob(rd, emptyFunction)。把这整个变成⼀个function，⽣ 成Job类。未来会在executor上触发其runJob


JobGenerator成功⽣成job后，调⽤JobScheduler.submitJobSet(JobSet)，JobScheduler会使⽤线程 池提交JobSet中的所有job。该⽅法调⽤结束后，JobGenerator发送⼀个DoCheckpoint的消息，注意 这⾥的cp是driver端元数据的cp，⽽不是RD本身的cp。如果time合适，会触发cp操作，内部的 CheckpointWriter类会完成write(streamingContext, time)。

JobScheduler提交job的线程⾥，触发了job的run()⽅法，同时，job跑完后，JobScheduler处理 JobCompleted(job)。如果job跑成功了，调⽤JobSet的handleJobCompletion(Job)，做些计时和数数 ⼯作，如果整个JobSet完成了，调⽤JobGenerator的onBatchCompletion(time)⽅法，JobGenerator 接着会做clearMetadata的⼯作，然后JobScheduler打印输出；如果job跑失败了，JobScheduler汇报 eror，最后会在context⾥抛异常。

更多说明

特殊操作

transform：可以与外部RD交互，⽐如做维表的join

updateStateByKey：⽣成StateDStream，⽐如做增量计算。WordCount例⼦

每⼀批都需要与增量RD进⾏⼀次cogroup之后，然后执⾏update function。两个RD做cogroup过程 有些开销：RD[K, V]和RD[K, U]合成RD[K, List[V], List[U]，List[U]⼀般size是1，理解为 oldvalue，即RD[K, batchValueList, Option[oldValue]。然后update function处理完，变成 RD[K, newValue]。 批与批之间严格有序，即增量合并操作，是有序的，批之间没发并发 增量RD的分区数可以开⼤，即这步增量的计算可以调⼤并发 window：batch size，window length, sliding interval三个参数组成的滑窗操作。把多个批次的RD合 并成⼀个UnionRD进⾏计算。

foreachRD: 这个操作是⼀个输出操作，⽐较特殊。

/*

- * Aply a function to each RD in this DStream. This is an output operator, so
- * 'this' DStream wil be registered as an output stream and therefore materialized.
- */ def foreachRD(foreachFunc: (RD[T], Time) => Unit) {


new ForEachDStream(this, context.sparkContext.clean(foreachFunc, false).register() }

DStream.foreachRD()操作使开发者可以直接控制RD的计算逻辑，⽽不是通过DStream映射过去。 所以借助这个⽅法，可以实现MLlib, Spark SQL与Streaming的集合，如：结合Spark SQL、 DataFrame做Wordcount。

Cache

如果是window操作，默认接收的数据都persist在内存⾥。

如果是flume, kafka源头，默认接收的数据replicate成两份存起来。

Checkpoint

与state有关的流计算，计算出来的结果RD，会被cp到HDFS上，原⽂如下：

Data checkpointing - Saving of the generated RDs to reliable storage. This is necesary in some s tateful transformations that combine data acros multiple batches. In such transformations, the ge nerated RDs depends on RDs of previous batches, which causes the length of the dependency chain to kep increasing with time. To avoid such unbounded increase in recovery time (proportion al to dependency chain), intermediate RDs of stateful transformations are periodicaly checkpoint ed to reliable storage (e.g. HDFS) to cut of the dependency chains. cp的时间间隔也可以设定，可以多批做⼀次cp。

cp的操作是同步的。

简单的不带state操作的流任务，可以不开启cp。

driver端的metadata也有cp策略。driver cp的时候是将整个StreamingContext对象写到了可靠存储 ⾥。

