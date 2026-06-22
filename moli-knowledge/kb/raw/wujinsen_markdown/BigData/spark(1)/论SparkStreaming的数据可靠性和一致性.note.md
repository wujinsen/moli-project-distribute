# 2Driver HA

由于流计算系统是⻓期运⾏、且不断有数据流⼊，因此其Spark守护进程（Driver）的可靠性⾄关 重要，它决定了Streaming程序能否⼀直正确地运⾏下去。

Driver实现HA的解决⽅案就是将元数据持久化，以便重启后的状态恢复。如图⼀所示，Driver持久 化的元数据包括：

Block元数据（图1中的绿⾊箭头）：Receiver从⽹络上接收到的数据，组装成Block后产⽣的Block 元数据；

Checkpoint数据（图1中的橙⾊箭头）：包括配置项、DStream操作、未完成的Batch状态、和⽣成 的RDD数据等；

Driver失败重启后：

![image 1](<论SparkStreaming的数据可靠性和一致性.note_images/imageFile1.png>)

恢复计算（图2中的橙⾊箭头）：使⽤Checkpoint数据重启driver，重新构造上下⽂并重启接收 器。

恢复元数据块（图2中的绿⾊箭头）：恢复Block元数据。

![image 2](<论SparkStreaming的数据可靠性和一致性.note_images/imageFile2.png>)

恢复未完成的作业（图2中的红⾊箭头）：使⽤恢复出来的元数据，再次产⽣RDD和对应的job， 然后提交到Spark集群执⾏。

通过如上的数据备份和恢复机制，Driver实现了故障后重启、依然能恢复Streaming任务⽽不丢失 数据，因此提供了系统级的数据⾼可靠。

可靠的上下游IO系统

流计算主要通过⽹络socket通信来实现与外部IO系统的数据交互。由于⽹络通信的不可靠特点， 发送端与接收端需要通过⼀定的协议来保证数据包的接收确认和失败重发机制。

不是所有的IO系统都⽀持重发，这⾄少需要实现数据流的持久化，同时还要实现⾼吞吐和低时 延。在SparkStreaming官⽅⽀持的data source⾥⾯，能同时满⾜这些要求的只有Kafka，因此在最 近的SparkStreaming release⾥⾯，也是把Kafka当成推荐的外部数据系统。

除了把Kafka当成输⼊数据源（inbound data source）之外，通常也将其作为输出数据源 （outbound data source）。所有的实时系统都通过Kafka这个MQ来做数据的订阅和分发，从⽽实 现流数据⽣产者和消费者的解耦。

⼀个典型的企业⼤数据中⼼数据流向视图如图3所示：

![image 3](<论SparkStreaming的数据可靠性和一致性.note_images/imageFile3.png>)

除了从源头保证数据可重发之外，Kafka更是流数据Exact Once语义的重要保障。Kafka提供了⼀套 低级API，使得client可以访问topic数据流的同时也能访问其元数据。SparkStreaming每个接收的 任务都可以从指定的Kafka topic、partition和offset去获取数据流，各个任务的数据边界很清晰， 任务失败后可以重新去接收这部分数据⽽不会产⽣“重叠的”数据，因⽽保证了流数据“有且仅处理 ⼀次”。

可靠的接收器

在Spark 1.3版本之前，SparkStreaming是通过启动专⽤的Receiver任务来完成从Kafka集群的数据 流拉取。

Receiver任务启动后，会使⽤Kafka的⾼级API来创建topicMessageStreams对象，并逐条读取数据 流缓存，每个batchInerval时刻到来时由JobGenerator提交⽣成⼀个spark计算任务。

由于Receiver任务存在宕机⻛险，因此Spark提供了⼀个⾼级的可靠接收器-ReliableKafkaReceiver 类型来实现可靠的数据收取，它利⽤了Spark 1.2提供的WAL（Write Ahead Log）功能，把接收到 的每⼀批数据持久化到磁盘后，更新topic-partition的offset信息，再去接收下⼀批Kafka数据。万 ⼀Receiver失败，重启后还能从WAL⾥⾯恢复出已接收的数据，从⽽避免了Receiver节点宕机造成 的数据丢失（以下代码删除了细枝末节的逻辑）：

![image 4](<论SparkStreaming的数据可靠性和一致性.note_images/imageFile4.png>)

启⽤WAL后，虽然Receiver的数据可靠性⻛险降低了，但却由于磁盘持久化带来的开销，系统整体 吞吐率会明显下降。因此，最新发布的Spark 1.3版本，SparkStreaming增加了使⽤Direct API的⽅ 式来实现Kafka数据源的访问。

引⼊了Direct API后，SparkStreaming不再启动常驻的Receiver接收任务，⽽是直接分配给每个 Batch及RDD最新的topic partition offset。job启动运⾏后Executor使⽤Kafka的simple consumer API去获取那⼀段offset的数据。

这样做的好处不仅避免了Receiver宕机带来数据可靠性的⻛险，也由于避免使⽤ZooKeeper做offset 跟踪，⽽实现了数据的精确⼀次性（以下代码删除了细枝末节的逻辑）：

![image 5](<论SparkStreaming的数据可靠性和一致性.note_images/imageFile5.png>)

# 预写⽇志 Write Ahead Log

Spark 1.2开始提供预写⽇志能⼒，⽤于Receiver数据及Driver元数据的持久化和故障恢复。WAL之 所以能提供持久化能⼒，是因为它利⽤了可靠的HDFS做数据存储。

SparkStreaming预写⽇志机制的核⼼API包括：

管理WAL⽂件的WriteAheadLogManager

读/写WAL的WriteAheadLogWriter和WriteAheadLogReader

基于WAL的RDD：WriteAheadLogBackedBlockRDD

基于WAL的Partition：WriteAheadLogBackedBlockRDDPartition

以上核⼼API在数据接收和恢复阶段的交互示意图如图4所示。

![image 6](<论SparkStreaming的数据可靠性和一致性.note_images/imageFile6.png>)

## 从WriteAheadLogWriter的源码⾥可以清楚看到，每次写⼊⼀块数据buffer到HDFS后都会调⽤flush ⽅法去强制刷⼊磁盘，然后才去取下⼀块数据。因此receiver接收的数据是可以保证持久化到磁盘 了，因⽽做到较好的数据可靠性。

![image 7](<论SparkStreaming的数据可靠性和一致性.note_images/imageFile7.png>)

结束语

得益于Kafka这类可靠的data source以及⾃身的checkpoint/WAL等机制，SparkStreaming的数据可 靠性得到了很好的保证，数据能保证“⾄少⼀次”（at least once）被处理。但由于其outbound端的 ⼀致性实现还未完善，因此Exact once语义仍然不能端到端保证。SparkStreaming社区已经在跟进 这个特性的实现（SPARK-4122），预计很快将合⼊trunk发布。

