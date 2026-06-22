作者Michael G. Nol是瑞⼠的⼀位⼯程师和研究员，效⼒于Verisign，是Verisign实验室的⼤规模数据分 析基础设施（基础Hadop）的技术主管。本⽂，Michael详细的演示了如何将Kafka整合到Spark Streaming中。 期间， Michael还提到了将Kafka整合到 Spark Streaming中的⼀些现状，⾮常值得阅 读，虽然有⼀些信息在Spark 1.2版本中已发⽣了⼀些变化，⽐如HA策略：

通过Spark Contributor、Spa rk布道者陈超我们了解到

，在Spark 1.2版本中，Spark Streaming开始⽀持fuly HA模式（选择使⽤）， 通过添加⼀层WAL（Write Ahead Log），每次收到数据后都会存在HDFS上，从⽽避免了以前版本中的 数据丢失情况，但是不可避免的造成了⼀定的开销，需要开发者⾃⾏衡量。

以下为译⽂

作为⼀个实时⼤数据处理⼯具， 近⽇⼀直被⼴泛关注，与 的对⽐也经 常出现。但是依我说，缺少与Kafka整合，任何实时⼤数据处理⼯具都是不完整的，因此我将⼀个示例 Spark Streaming应⽤程序添加到 ，并且示范如何从Kafka读取，以及如何写⼊到 Kafka。在这个过程中，我还使⽤Avro作为数据格式，以及Twiter Bijection进⾏数据序列化。

Spark Sreaming Apache Storm

kafka-storm-starter

在本篇⽂章，我将详细地讲解这个Spark Streaming示例；同时，我还会穿插当下Spark Streaming与 Kafka整合的⼀些焦点话题。免责声明：这是我⾸次试验Spark Streaming，仅作为参考。

当下，这个Spark Streaming示例被上传到GitHub，下载访问： 。项⽬的名称或许 会让你产⽣某些误解，不过，不要在意这些细节：）

kafka-storm-starter

## 什么是Spark Streaming

Spark Streaming

是Apache Spark的⼀个⼦项⽬。Spark是个类似于Apache Hadop的开源批处理平 台，⽽Spark Streaming则是个实时处理⼯具，运⾏在Spark引擎之上。

## Spark Streaming vs. Apache Storm

Spark Streaming与Apache Storm有⼀些相似之处，后者是当下最流⾏的⼤数据处理平台。前不久，雅 ⻁的Boby Evans 和Tom Graves曾发表过⼀个“ ”的演讲，在这个演讲中， 他们对⽐了两个⼤平台，并提供了⼀些选择参考。类似的，Hortonworks的P. Taylor Goetz也分享过名 为 的讲义。

Spark and Storm at Yaho!

Apache Storm and Spark Streaming Compared

这⾥，我也提供了⼀个⾮常简短的对⽐：对⽐Spark Streaming，Storm的产业采⽤更⾼，⽣产环境应⽤ 也更稳定。但是从另⼀⽅⾯来说，对⽐Storm，Spark拥有更清晰、等级更⾼的API，因此Spark使⽤起来 也更加愉快，最起码是在使⽤Scala编写Spark应⽤程序的情况（毫⽆疑问，我更喜欢Spark中的API）。 但是，请别这么直接的相信我的话，多看看上⾯的演讲和讲义。

不管是Spark还是Storm，它们都是Apache的顶级项⽬，当下许多⼤数据平台提供商也已经开始整合这 两个框架（或者其中⼀个）到其商业产品中，⽐如Hortonworks就同时整合了Spark和Storm，⽽ Cloudera也整合了Spark。

附录：Spark中的Machines、cores、executors、tasks和receivers

本⽂的后续部分将讲述许多Spark和Kafka中的paralelism问题，因此，你需要掌握⼀些Spark中的术语 以弄懂这些环节。

⼀个Spark集群必然包含了1个以上的⼯者作节点，⼜称为从主机（为了简化架构，这⾥我们先抛弃开 集群管理者不谈）。 ⼀个⼯作者节点可以运⾏⼀个以上的executor Executor是⼀个⽤于应⽤程序或者⼯作者节点的进程，它们负责处理tasks，并将数据保存到内存或 者磁盘中。每个应⽤程序都有属于⾃⼰的executors，⼀个executor则包含了⼀定数量的cores（也被 称为slots）来运⾏分配给它的任务。 Task是⼀个⼯作单元，它将被传送给executor。也就是说，task将是你应⽤程序的计算内容（或者是 ⼀部分）。SparkContext将把这些tasks发送到executors进⾏执⾏。每个task都会占⽤⽗executor中 的⼀个core（slot）。 Receiver（ ， ）将作为⼀个⻓期运⾏的task跑在⼀个executor上。每个receiver都会负责⼀ 个所谓的input DStream（⽐如从Kafka中读取的⼀个输⼊流），同时每个receiver（ input DStream）占⽤⼀个core/slot。

API ⽂档

input DStream：input DStream是DStream的⼀个类型，它负责将Spark Streaming连接到外部的数 据源，⽤于读取数据。对于每个外部数据源（⽐如Kafka）你都需要配置⼀个input DStream。⼀个 Spark Streaming会通过⼀个input DStream与⼀个外部数据源进⾏连接，任何后续的DStream都会建 ⽴标准的DStreams。

在Spark的执⾏模型，每个应⽤程序都会获得⾃⼰的executors，它们会⽀撑应⽤程序的整个流程，并以 多线程的⽅式运⾏1个以上的tasks，这种隔离途径⾮常类似Storm的执⾏模型。⼀旦引⼊类似YARN或者 Mesos这样的集群管理器，整个架构将会变得异常复杂，因此这⾥将不会引⼊。你可以通过Spark⽂档中 的 了解更多细节。

Cluster Overview

## 整合Kafka到Spark Streaming

概述

简⽽⾔之，Spark是⽀持Kafka的，但是这⾥存在许多不完善的地⽅。

Spark代码库中的 对于我们来说是个⾮常好的起点，但是这⾥仍然存在⼀些开放式问 题。

KafkaWordCount

特别是我想了解如何去做：

从kafaka中并⾏读⼊。在Kafka，⼀个话题（topic）可以有N个分区。理想的情况下，我们希望在多 个分区上并⾏读取。这也是 Kafka spout in Storm 的⼯作。 从⼀个Spark Streaming应⽤程序向Kafka写⼊，同样，我们需要并⾏执⾏。

在完成这些操作时，我同样碰到了Spark Streaming和/或Kafka中⼀些已知的问题，这些问题⼤部分都已 经在Spark mailing list中列出。在下⾯，我将详细总结Kafka集成到Spark的现状以及⼀些常⻅问题。

Kafka中的话题、分区（partitions）和paralelism

详情可以查看我之前的博⽂： 和 。

Apache Kafka 0.8 Training Deck and Tutorial Runing a Multi-Broker Apache Kafka 0.8 Cluster on a Single Node

Kafka将数据存储在话题中，每个话题都包含了⼀些可配置数量的分区。话题的分区数量对于性能来说⾮ 常重要，⽽这个值⼀般是消费者paralelism的最⼤数量：如果⼀个话题拥有N个分区，那么你的应⽤程序 最⼤程度上只能进⾏N个线程的并⾏，最起码在使⽤Kafka内置Scala/Java消费者API时是这样的。

与其说应⽤程序，不如说Kafka术语中的消费者群（consumer group）。消费者群，通过你选择的字符 串识别，它是逻辑消费者应⽤程序集群范围的识别符。同⼀个消费者群中的所有消费者将分担从⼀个指 定Kafka话题中的读取任务，同时，同⼀个消费组中所有消费者从话题中读取的线程数最⼤值即是N（等 同于分区的数量），多余的线程将会闲置。

多个不同的Kafka消费者群可以并⾏的运⾏：毫⽆疑问，对同⼀个Kafka话题，你可以运⾏多个独⽴的逻 辑消费者应⽤程序。这⾥，每个逻辑应⽤程序都会运⾏⾃⼰的消费者线程，使⽤⼀个唯⼀的消费者群 id。⽽每个应⽤程序通常可以使⽤不同的read paralelisms（⻅下⽂）。当在下⽂我描述不同的⽅式配置 read paralelisms时，我指的是如何完成这些逻辑消费者应⽤程序中的⼀个设置。

这⾥有⼀些简单的例⼦

你的应⽤程序使⽤“teran”消费者群id对⼀个名为“zerg.hydra”的kafka话题进⾏读取，这个话题拥有 10个分区。如果你的消费者应⽤程序只配置⼀个线程对这个话题进⾏读取，那么这个线程将从10个分 区中进⾏读取。 同上，但是这次你会配置5个线程，那么每个线程都会从2个分区中进⾏读取。 同上，这次你会配置10个线程，那么每个线程都会负责1个分区的读取。 同上，但是这次你会配置多达14个线程。那么这14个线程中的10个将平分10个分区的读取⼯作，剩下 的4个将会被闲置。

这⾥我们不妨看⼀下现实应⽤中的复杂性⸺Kafka中的再平衡事件。在Kafka中，再平衡是个⽣命周期 事件（lifecycle event），在消费者加⼊或者离开消费者群时都会触发再平衡事件。这⾥我们不会进⾏详 述，更多再平衡详情可参⻅我的 ⼀⽂。

Kafka training deck

你的应⽤程序使⽤消费者群id“teran”，并且从1个线程开始，这个线程将从10个分区中进⾏读取。在运 ⾏时，你逐渐将线程从1个提升到14个。也就是说，在同⼀个消费者群中，paralelism突然发⽣了变化。 毫⽆疑问，这将造成Kafka中的再平衡。⼀旦在平衡结束，你的14个线程中将有10个线程平分10个分区 的读取⼯作，剩余的4个将会被闲置。因此如你想象的⼀样，初始线程以后只会读取⼀个分区中的内容， 将不会再读取其他分区中的数据。

现在，我们终于对话题、分区有了⼀定的理解，⽽分区的数量将作为从Kafka读取时paralelism的上限。 但是对于⼀个应⽤程序来说，这种机制会产⽣⼀个什么样的影响，⽐如⼀个Spark Streaming job或者 Storm topology从Kafka中读取数据作为输⼊。

- 1. Read paralelism： 通常情况下，你期望使⽤N个线程并⾏读取Kafka话题中的N个分区。同时，鉴于 数据的体积，你期望这些线程跨不同的NIC，也就是跨不同的主机。在Storm中，这可以通过 TopologyBuilder#setSpout()设置Kafka spout的paralelism为N来实现。在Spark中，你则需要做更多的 事情，在下⽂我将详述如何实现这⼀点。

- 2. Downstream procesing paralelism： ⼀旦使⽤Kafka，你希望对数据进⾏并⾏处理。鉴于你的⽤ 例，这种等级的paralelism必然与read paralelism有所区别。如果你的⽤例是计算密集型的，举个例 ⼦，对⽐读取线程，你期望拥有更多的处理线程；这可以通过从多个读取线程shufling或者⽹路 “faning out”数据到处理线程实现。因此，你通过增⻓⽹络通信、序列化开销等将访问交付给更多的 cores。在Storm中，你通过 将Kafka spout shufling到下游的bolt中。在Spark中，你 需要通过DStreams上的 转换来实现。


shufle grouping repartition

通常情况下，⼤家都渴望去耦从Kafka的paralelisms读取，并⽴即处理读取来的数据。在下⼀节，我将 详述使⽤ Spark Streaming从Kafka中的读取和写⼊。

## 从Kafka中读取

Spark Streaming中的Read paralelism

类似Kafka，Read paralelism中也有分区的概念。了解Kafka的per-topic话题与 中的分区 没有关联⾮常重要。

RDs in Spark

Spark Streaming中的 （⼜称为Kafka连接器）使⽤了Kafka的 ， 这意味着在Spark中为Kafka设置 read paralelism将拥有两个控制按钮。

KafkaInputDStream ⾼等级消费者API

- 1. Input DStreams的数量。 因为Spark在每个Input DStreams都会运⾏⼀个receiver（=task），这就 意味着使⽤多个input DStreams将跨多个节点并⾏进⾏读取操作，因此，这⾥寄希望于多主机和NICs。

- 2. Input DStreams上的消费者线程数量。 这⾥，相同的receiver（=task）将运⾏多个读取线程。这也 就是说，读取操作在每个core/machine/NIC上将并⾏的进⾏。


在实际情况中，第⼀个选择显然更是⼤家期望的。

为什么会这样？⾸先以及最重要的，从Kafka中读取通常情况下会受到⽹络/NIC限制，也就是说，在同⼀ 个主机上你运⾏多个线程不会增加读的吞吐量。另⼀⽅⾯来讲，虽然不经常，但是有时候从Kafka中读取 也会遭遇CPU瓶颈。其次，如果你选择第⼆个选项，多个读取线程在将数据推送到blocks时会出现锁竞 争（在block⽣产者实例上，BlockGenerator的“+=”⽅法真正使⽤的是“synchronized”⽅式）。

input DStreams建⽴的RDs分区数量：KafkaInputDStream将储存从Kafka中读取的每个信息到 Blocks。从我的理解上，⼀个新的Block由 spark.streaming.blockInterval在毫秒级别建⽴，⽽每个 block都会转换成RD的⼀个分区，最终由DStream建⽴。如果我的这种假设成⽴，那么由 KafkaInputDStream建⽴的RDs分区数量由batchInterval / spark.streaming.blockInterval决定，⽽ batchInterval则是数据流拆分成batches的时间间隔，它可以通过StreamingContext的⼀个构造函数参 数设置。举个例⼦，如果你的批时间价格是2秒（默认情况下），⽽block的时间间隔是20毫秒（默认 情况），那么你的RD将包含10个分区。如果有错误的话，可以提醒我。

选项1：控制input DStreams的数量

下⾯这个例⼦可以从 Spark Streaming Progra ming Guide 中获得：

val ssc:StreamingContext=???// ignore for now val kafkaParams:Map[String,String]=Map("group.id"->"terran",/* ignore rest */)

val numInputDStreams =5 val kafkaDStreams =(1 to numInputDStreams).map {_=>KafkaUtils.createStream(...)}

在这个例⼦中，我们建⽴了5个input DStreams，因此从Kafka中读取的⼯作将分担到5个核⼼上，寄希 望于5个主机/NICs（之所以说是寄希望于，因为我也不确定Spark Streaming task布局策略是否会将 receivers投放到多个主机上）。所有Input Streams都是“teran”消费者群的⼀部分，⽽Kafka将保证 topic的所有数据可以同时对这5个input DSreams可⽤。换句话说，这种“colaborating”input DStreams 设置可以⼯作是基于消费者群的⾏为是由Kafka API提供，通过KafkaInputDStream完成。

在这个例⼦中，我没有提到每个input DSream会建⽴多少个线程。在这⾥，线程的数量可以通过 KafkaUtils.createStream⽅法的参数设置（同时，input topic的数量也可以通过这个⽅法的参数指 定）。在下⼀节中，我们将通过实际操作展示。

但是在开始之前，在这个步骤我先解释⼏个Spark Streaming中常⻅的⼏个问题，其中有些因为当下 Spark中存在的⼀些限制引起，另⼀⽅⾯则是由于当下Kafka input DSreams的⼀些设置造成：

当你使⽤我上⽂介绍的多输⼊流途径，⽽这些消费者都是属于同⼀个消费者群，它们会给消费者指定负 责的分区。这样⼀来则可能导致syncpartitionrebalance的失败，系统中真正⼯作的消费者可能只会有⼏ 个。为了解决这个问题，你可以把再均衡尝试设置的⾮常⾼，从⽽获得它的帮助。然后，你将会碰到另 ⼀个坑⸺如果你的receiver宕机（ OM，亦或是硬件故障），你将停⽌从Kafka接收消息。

Spark⽤户讨论

markmail.org/mesage/…

这⾥，我们需要对“停⽌从Kafka中接收”问题 。当下，当你通过 sc.start()开启你的streams 应⽤程序后，处理会开始并⼀直进⾏，即使是输⼊数据源（⽐如Kafka）变得不可⽤。也就是说，流不能 检测出是否与上游数据源失去链接，因此也不会对丢失做出任何反应，举个例⼦来说也就是重连或者结 束执⾏。类似的，如果你丢失这个数据源的⼀个receiver，那么

做⼀些解释

你的流应⽤程序可能就会⽣成⼀些空的R Ds

。

这是⼀个⾮常糟糕的情况。最简单也是最粗糙的⽅法就是，在与上游数据源断开连接或者⼀个receiver失 败时，重启你的流应⽤程序。但是，这种解决⽅案可能并不会产⽣实际效果，即使你的应⽤程序需要将 Kafka配置选项auto.ofset.reset设置到最⼩⸺因为Spark Streaming中⼀些已知的bug，可能导致你的 流应⽤程序发⽣⼀些你意想不到的问题，在下⽂Spark Streaming中常⻅问题⼀节我们将详细的进⾏介 绍。

选择2：控制每个input DStream上⼩发着线程的数量

在这个例⼦中，我们将建⽴⼀个单⼀的input DStream，它将运⾏3个消费者线程⸺在同⼀个 receiver/task，因此是在同⼀个core/machine/NIC上对Kafka topic “zerg.hydra”进⾏读取。

val ssc:StreamingContext=???// ignore for now val kafkaParams:Map[String,String]=Map("group.id"->"terran",...)

val consumerThreadsPerInputDstream =3 val topics =Map("zerg.hydra"-> consumerThreadsPerInputDstream) val stream =KafkaUtils.createStream(ssc, kafkaParams, topics,...)

KafkaUtils.createStream⽅法被重载，因此这⾥有⼀些不同⽅法的特征。在这⾥，我们会选择Scala派⽣ 以获得最佳的控制。

结合选项1和选项2

下⾯是⼀个更完整的示例，结合了上述两种技术：

val ssc:StreamingContext=??? val kafkaParams:Map[String,String]=Map("group.id"->"terran",...)

val numDStreams =5 val topics =Map("zerg.hydra"->1) val kafkaDStreams =(1 to numDStreams).map{_

=>KafkaUtils.createStream(ssc, kafkaParams, topics,...)}

我们建⽴了5个input DStreams，它们每个都会运⾏⼀个消费者线程。如果“zerg.hydra”topic拥有5个分 区（或者更少），那么这将是进⾏并⾏读取的最佳途径，如果你在意系统最⼤吞吐量的话。

Spark Streaming中的并⾏Downstream处理

在之前的章节中，我们覆盖了从Kafka的并⾏化读取，那么我们就可以在Spark中进⾏并⾏化处理。那么 这⾥，你必须弄清楚Spark本身是如何进⾏并⾏化处理的。类似Kafka，Spark将paralelism设置的与 （RD）分区数量有关， 。在有些⽂档中，分区仍然被称为 “slices”。

通过在每个RD分区上运⾏task进⾏

在任何Spark应⽤程序中，⼀旦某个Spark Streaming应⽤程序接收到输⼊数据，其他处理都与⾮ streaming应⽤程序相同。也就是说，与普通的Spark数据流应⽤程序⼀样，在Spark Streaming应⽤程 序中，你将使⽤相同的⼯具和模式。更多详情可⻅ ⽂档。

Level of Paralelism in Data Procesing

因此，我们同样将获得两个控制⼿段：

- 1. input DStreams的数量 ，也就是说，我们在之前章节中read paralelism的数量作为结果。这是我们 的⽴⾜点，这样⼀来，我们在下⼀个步骤中既可以保持原样，也可以进⾏修改。

- 2. DStream转化的重分配 。这⾥将获得⼀个全新的DStream，其paralelism等级可能增加、减少，或者 保持原样。在DStream中每个返回的RD都有指定的N个分区。DStream由⼀系列的RD组成， DStream.repartition则是通过RD.repartition实现。接下来将对RD中的所有数据做随机的reshufles， 然后建⽴或多或少的分区，并进⾏平衡。同时，数据会在所有⽹络中进⾏shufles。换句话说， DStream.repartition⾮常类似Storm中的shufle grouping。


因此，repartition是从procesing paralelism解耦read paralelism的主要途径。在这⾥，我们可以设置 procesing tasks的数量，也就是说设置处理过程中所有core的数量。间接上，我们同样设置了投⼊ machines/NICs的数量。

⼀个DStream转换相关是 。这个⽅法同样在StreamingContext中，它将从多个DStream中返回⼀ 个统⼀的DStream，它将拥有相同的类型和滑动时间。通常情况下，你更愿意⽤StreamingContext的派 ⽣。⼀个union将返回⼀个由Union RD⽀撑的UnionDStream。Union RD由RDs统⼀后的所有分区组 成，也就是说，如果10个分区都联合了3个RDs，那么你的联合RD实例将包含30个分区。换句话说， union会将多个 DStreams压缩到⼀个 DStreams或者RD中，但是需要注意的是，这⾥的paralelism并 不会发⽣改变。你是否使⽤union依赖于你的⽤例是否需要从所有Kafka分区进⾏“in one place”信息获取 决定，因此这⾥⼤部分都是基于语义需求决定。举个例⼦，当你需要执⾏⼀个不⽤元素上的（全局）计 数。

union

注意： RDs是⽆序的。因此，当你union RDs时，那么结果RD同样不会拥有⼀个很好的序列。如果 你需要在RD中进⾏sort。

你的⽤例将决定需要使⽤的⽅法，以及你需要使⽤哪个。如果你的⽤例是CPU密集型的，你希望对 zerg.hydra topic进⾏5 read paralelism读取。也就是说，每个消费者进程使⽤5个receiver，但是却可 以将procesing paralelism提升到20。

val ssc:StreamingContext=??? val kafkaParams:Map[String,String]=Map("group.id"->"terran",...) val readParallelism =5 val topics =Map("zerg.hydra"->1) val kafkaDStreams =(1 to readParallelism).map{ _

=>KafkaUtils.createStream(ssc, kafkaParams, topics,...)}//> collection of five *input* DStreams = handled by five receivers/tasks val unionDStream = ssc.union(kafkaDStreams)// often unnecessary, just showcasing how to do it//> single DStream val processingParallelism =20 val processingDStream = unionDStream(processingParallelism)//> single DStream but now with 20 partitions

在下⼀节中，我将把所有部分结合到⼀起，并且联合实际数据处理进⾏讲解。

## 写⼊到Kafka

写⼊到Kafka需要从foreachRD输出操作进⾏：

通⽤的输出操作者都包含了⼀个功能（函数），让每个RD都由Stream⽣成。这个函数需要将每个RD 中的数据推送到⼀个外部系统，⽐如将RD保存到⽂件，或者通过⽹络将它写⼊到⼀个数据库。需要注 意的是，这⾥的功能函数将在驱动中执⾏，同时其中通常会伴随RD⾏为，它将会促使流RDs的计算。

注意： 重提“功能函数是在驱动中执⾏”，也就是Kafka⽣产者将从驱动中进⾏，也就是说“功能函数是在 驱动中进⾏评估”。当你使⽤foreachRD从驱动中读取Design Paterns时，实际过程将变得更加清晰。

在这⾥，建议⼤家去阅读Spark⽂档中的 ⼀节，它将详细讲解使 ⽤foreachRD读外部系统中的⼀些常⽤推荐模式，以及经常出现的⼀些陷阱。

Design Paterns for using foreachRD

在我们这个例⼦⾥，我们将按照推荐来重⽤Kafka⽣产者实例，通过⽣产者池跨多个RDs/batches。 我 通过 实现了这样⼀个⼯具，已经上传到 。这个⽣产者池本身通过

Apache Comons Pol GitHub broad cast variable

提供给tasks。

最终结果看起来如下：

val producerPool ={// See the full code on GitHub for details on how the pool is created

val pool = createKafkaProducerPool(kafkaZkCluster.kafka.brokerList, outputTopic.name)

ssc.sparkContext.broadcast(pool)}

stream.map {...}.foreachRDD(rdd =>{ rdd.foreachPartition(partitionOfRecords =>{// Get a producer from the

shared pool val p = producerPool.value.borrowObject() partitionOfRecords.foreach{case tweet:Tweet=>// Convert pojo back

into Avro binary format

val bytes = converter.value.apply(tweet)// Send the bytes to Kafka

p.send(bytes)}// Returning the producer to the pool also shuts it down

producerPool.value.returnObject(p)})})

需要注意的是， Spark Streaming每分钟都会建⽴多个RDs，每个都会包含多个分区，因此你⽆需为 Kafka⽣产者实例建⽴新的Kafka⽣产者，更不⽤说每个Kafka消息。上⾯的步骤将最⼩化Kafka⽣产者实 例的建⽴数量，同时也会最⼩化TCP连接的数量（通常由Kafka集群确定）。你可以使⽤这个池设置来精 确地控制对流应⽤程序可⽤的Kafka⽣产者实例数量。如果存在疑惑，尽量⽤更少的。

完整示例

下⾯的代码是示例Spark Streaming应⽤程序的要旨（所有代码参⻅ 这⾥ ）。这⾥，我做⼀些解释：

并⾏地从Kafka topic中读取Avro-encoded数据。我们使⽤了⼀个最佳的read paralelism，每个 Kafka分区都配置了⼀个单线程 input DStream。 并⾏化Avro-encoded数据到pojos中，然后将他们并⾏写到binary，序列化可以通过

Twiter

Bijection 通过Kafka⽣产者池将结果写回⼀个不同的Kafka topic。

执⾏。

// Set up the input DStream to read from Kafka (in parallel) val kafkaStream ={

val sparkStreamingConsumerGroup ="spark-streaming-consumer-group" val kafkaParams =Map("zookeeper.connect"-

>"zookeeper1:2181","group.id"->"spark-streamingtest","zookeeper.connection.timeout.ms"->"1000")

val inputTopic ="input-topic" val numPartitionsOfInputTopic =5 val streams =(1 to numPartitionsOfInputTopic) map { _

=>KafkaUtils.createStream(ssc, kafkaParams,Map(inputTopic >1),StorageLevel.MEMORY_ONLY_SER).map(_._2)}

val unifiedStream = ssc.union(streams) val sparkProcessingParallelism =1// You'd probably pick a higher

value than 1 in production.

unifiedStream.repartition(sparkProcessingParallelism)}// We use accumulators to track global "counters" across the tasks of our streaming app val numInputMessages = ssc.sparkContext.accumulator(0L,"Kafka messages consumed") val numOutputMessages = ssc.sparkContext.accumulator(0L,"Kafka messages produced")// We use a broadcast variable to share a pool of Kafka producers, which we use to write data from Spark to Kafka. val producerPool ={

val pool = createKafkaProducerPool(kafkaZkCluster.kafka.brokerList, outputTopic.name)

ssc.sparkContext.broadcast(pool)}// We also use a broadcast variable for our Avro Injection (Twitter Bijection) val converter = ssc.sparkContext.broadcast(SpecificAvroCodecs.toBinary[Tweet])// Define the actual data flow of the streaming job kafkaStream.map {case bytes =>

numInputMessages +=1// Convert Avro binary data to pojo converter.value.invert(bytes) match {caseSuccess(tweet)=> tweet

caseFailure(e)=>// ignore if the conversion failed}}.foreachRDD(rdd

=>{ rdd.foreachPartition(partitionOfRecords =>{

val p = producerPool.value.borrowObject() partitionOfRecords.foreach{case tweet:Tweet=>// Convert pojo back

into Avro binary format

val bytes = converter.value.apply(tweet)// Send the bytes to Kafka

p.send(bytes) numOutputMessages +=1}

producerPool.value.returnObject(p)})})// Run the streaming job ssc.start() ssc.awaitTermination()

更多的细节和解释可以在这⾥看所有源代码。

就我⾃⼰⽽⾔，我⾮常喜欢 Spark Streaming代码的简洁和表述。在Boby Evans和 Tom Graves讲话中 没有提到的是，Storm中这个功能的等价代码是⾮常繁琐和低等级的： 中的

kafka-storm-starter Kafka StormSpec

会运⾏⼀个Stormtopology来执⾏相同的计算。同时，规范⽂件本身只有⾮常少的代码，当 然是除下说明语⾔，它们能更好的帮助理解；同时，需要注意的是，在Storm的Java API中，你不能使⽤ 上⽂Spark Streaming 示例中所使⽤的匿名函数，⽐如map和foreach步骤。取⽽代之的是，你必须编写 完整的类来获得相同的功能，你可以查看 。这感觉是将Spark的API转换到Java，在这 ⾥使⽤匿名函数是⾮常痛苦的。

AvroDecoderBolt

最后，我同样也⾮常喜欢 ，它⾮常适合初学者查看，甚⾄还包含了⼀些 。关 于Kafka整合到Spark，上⽂已经基本介绍完成，但是我们仍然需要浏览mailing list和深挖源代码。这 ⾥，我不得不说，维护帮助⽂档的同学做的实在是太棒了。

Spark的说明⽂档 进阶使⽤

## 知晓Spark Streaming中的⼀些已知问题

你可能已经发现在Spark中仍然有⼀些尚未解决的问题，下⾯我描述⼀些我的发现：

Multiple Kafka Receivers a nd Union How to scale more consumer to Kafka stream

⼀⽅⾯，在对Kafka进⾏读写上仍然存在⼀些含糊不清的问题，你可以在类似 和 mailing list的讨论中发现。

另⼀⽅⾯，Spark Streaming中⼀些问题是因为Spark本身的固有问题导致，特别是故障发⽣时的数据丢 失问题。换句话说，这些问题让你不想在⽣产环境中使⽤Spark。

在Spark 1.1版本的驱动中，Spark并不会考虑那些已经接收却因为种种原因没有进⾏处理的元数据（

）。因此，在某些情况下，你的Spark可能会丢失数据。Tathagata Das指出 驱动恢复问题会在Spark的1.2版本中解决，当下已经释放。

点击这⾥查看更多细节

1.1版本中的Kafka连接器是基于Kafka的⾼等级消费者API。这样就会造成⼀个问题，Spark Streaming 不可以依赖其⾃身的KafkaInputDStream将数据从Kafka中重新发送，从⽽⽆法解决下游数据丢失问 题（⽐如Spark服务器发⽣故障）。 有些⼈甚⾄认为这个版本中的Kafka连接器不应该投⼊⽣产环境使⽤，因为它是基于Kafka的⾼等级消 费者API。取⽽代之，Spark应该使⽤简单的消费者API（就像Storm中的Kafka spout），它将允许你 控制便宜和分区分配确定性。 但是当下Spark社区已经在致⼒这些⽅⾯的改善，⽐如Dibyendu Bhatacharya的Kafka连接器。后者 是Apache Storm Kafka spout的⼀个端⼝，它基于Kafka所谓的简单消费者API，它包含了故障发⽣情 景下⼀个更好的重放机制。 即使拥有如此多志愿者的努⼒，Spark团队更愿意⾮特殊情况下的Kafka故障恢复策略，他们的⽬标是 “在所有转换中提供强保证，通⽤的策略”，这⼀点⾮常难以理解。从另⼀个⻆度来说，这是浪费 Kafka本身的故障恢复策略。这⾥确实难以抉择。 这种情况同样也出现在写⼊情况中，很可能会造成数据丢失。

Spark的Kafka消费者参数auto.ofset.reset的使⽤同样与Kafka的策略不同。在Kafka中，将 auto.ofset.reset设置为最⼩是消费者将⾃动的将ofset设置为最⼩ofset，这通常会发⽣在两个情 况：第⼀，在ZoKeper中不存在已有ofsets；第⼆，已存在ofset，但是不在范围内。⽽在Spark 中，它会始终删除所有的ofsets，并从头开始。这样就代表着，当你使⽤auto.ofset.reset = “smalest”重启你的应⽤程序时，你的应⽤程序将完全重新处理你的Kafka应⽤程序。更多详情可以在 下⾯的两个讨论中发现： 1 和 2 。 Spark-1341：⽤于控制Spark Streaming中的数据传输速度。这个能⼒可以⽤于很多情况，当你已经 受Kafka引起问题所烦恼时（⽐如auto.ofset.reset所造成的），然后可能让你的应⽤程序重新处理⼀ 些旧数据。但是鉴于这⾥并没有内置的传输速率控制，这个功能可能会导致你的应⽤程序过载或者内 存不⾜。

在这些故障处理策略和Kafka聚焦的问题之外之外，扩展性和稳定性上的关注同样不可忽视。再⼀次，仔 细观看 以获得更多细节。在Spark使⽤经验上，他们都永远⽐我更丰富。

Boby和Tom的视频

当然，我也有我的 ，在 G1 garbage（在Java 1.7.0u4+中） 上可能也会存在问题。但是，我从来都 没碰到这个问题。

评论

# Spark使⽤技巧和敲⻔

在我实现这个示例的代码时，我做了⼀些重要的笔记。虽然这不是⼀个全⾯的指南，但是在你开始Kafka 整合时可能发挥⼀定的作⽤。它包含了 中的⼀些信息，也有⼀些 是来⾃Spark⽤户的mailing list。

Spark Streaming progra ming guide

通⽤

当你建⽴你的Spark环境时，对Spark使⽤的cores数量配置需要特别投⼊精⼒。你必须为Spark配置 receiver⾜够使⽤的cores（⻅下⽂），当然实际数据处理所需要的cores的数量也要进⾏配置。在 Spark中，每个receiver都负责⼀个input DStream。同时，每个receiver（以及每个input DStream） ocies⼀个core，这样做是服务于每个⽂件流中的读取（详⻅⽂档）。举个例⼦，你的作业需要从两 个input streams中读取数据，但是只访问两个cores，这样⼀来，所有数据都只会被读取⽽不会被处 理。 注意，在⼀个流应⽤程序中，你可以建⽴多个input DStreams来并⾏接收多个数据流。在上⽂从 Kafka并⾏读取⼀节中，我曾演示过这个示例作业。 你可以使⽤ broadcast variables在不同主机上共享标准、只读参数，相关细节⻅下⽂的优化指导。在 示例作业中，我使⽤了broadcast variables共享了两个参数：第⼀，Kafka⽣产者池（作业通过它将 输出写⼊Kafka）；第⼆，encoding/decoding Avro数据的注⼊（从Twiter Bijection中）。

Pasing

functions to Spark 你可以使⽤累加器参数来跟踪流作业上的所有全局“计数器”，这⾥可以对照Hadop作业计数器。在 示例作业中，我使⽤累加器分别计数所有消费的Kafka消息，以及所有对Kafka的写⼊。如果你对累加 器进⾏命名，它们同样可以在Spark UI上展示。 不要忘记import Spark和Spark Streaming环境：

。

// Required to gain access to RDD transformations via implicits.import org.apache.spark.SparkContext._

// Required when working on `PairDStreams` to gain access to e.g. `DStream.reduceByKey`// (versus `DStream.transform(rddBatch => rddBatch.reduceByKey()`) via implicits.//// See also

http://spark.apach e.org/docs/1.1.0/programming-guide.html#working-with-key-valuepairs

import org.apache.spark.streaming.StreamingContext.toPairDStreamFunctions

如果你是 Twiter Algebird的爱好者，你将会喜欢使⽤Count-Min Sketch和Spark中的⼀些特性，代表性 的，你会使⽤reduce或者reduceByWindow这样的操作（⽐如， ）。Spark项⽬包含 了 和 的示例介绍。

DStreams上的转换 Count-Min Sketch HyperLogLog

如果你需要确定Algebird数据结构的内存介绍，⽐如Count-Min Sketch、HyperLogLog或者Bl om Filters，你可以使⽤SparkContext⽇志进⾏查看，更多细节参⻅ 。

Determining Memory Consumption

Kafka整合

我前⽂所述的⼀些增补：

你可能需要修改Spark Streaming中的⼀些Kafka消费者配置。举个例⼦，如果你需要从Kafka中读取 ⼤型消息，你必须添加fetch.mesage.max.bytes消费设置。你可以使⽤KafkaUtils.createStream(…) 将这样定制的Kafka参数给Spark Streaming传送。

测试

⾸先，确定 已经 在⼀个finaly bloc或者测试框架的teardown method中使⽤stop()关闭了 StreamingContext 和/或 SparkContext，因为在同⼀个程序（或者JVM？）中Spark不⽀持并⾏运⾏ 两种环境。 根据我的经验，在使⽤sbt时，你希望在测试中将你的建⽴配置到分⽀JVM中。最起码在kafkastorm-starter中，测试必须并⾏运⾏多个线程，⽐如ZoKeper、Kafka和Spark的内存实例。开始 时，你可以参考 build.sbt 。 同样，如果你使⽤的是Mac OS X，你可能期望关闭JVM上的IPv6⽤以阻⽌DNS相关超时。这个问题 与Spark⽆关，你可以查看 .sbtopts 来获得关闭IPv6的⽅法。

性能调优

确定你理解作业中的运⾏时影响，如果你需要与外部系统通信，⽐如Kafka。在使⽤foreachRD时， 你应该阅读中 中的Design Paterns⼀节。举个例⼦，我的⽤ 例中使⽤Kafka产⽣者池来优化 Spark Streaming到Kafka的写⼊。在这⾥，优化意味着在多个task中 共享同⼀个⽣产者，这个操作可以显著地减少由Kafka集群建⽴的新TCP连接数。

Spark Streaming progra ming guide

使⽤Kryo做序列化，取代默认的Java serialization，详情可以访问 。我的例⼦就使⽤ 了Kryo和注册器，举个例⼦，使⽤Kryo⽣成的Avro-generated Java类（⻅

Tuning Spark KafkaSparkStreamingRe

gistrator 通过将spark.streaming.unpersist设置为true将Spark Streaming 作业设置到明确持续的RDs。这可 以显著地减少Spark在RD上的内存使⽤，同时也可以改善GC⾏为。（点击访问 来源 ） 通过MEMORY_ONLY_SER开始你的储存级别P&S测试（在这⾥，RD被存储到序列化对象，每个分 区⼀个字节）。取代反序列化，这样做更有空间效率，特别是使⽤Kryo这样的⾼速序列化⼯具时，但 是会增加读取上的CPU密集操作。这个优化对 Spark Streaming作业也⾮常有效。对于本地测试来 说，你可能并不想使⽤*_2派⽣（2=复制因⼦）。

）。除此之外，在Storm中类似的问题也可以使⽤Kryo来解决。

总结

完整的Spark Streaming示例代码可以在 查看。这个应⽤包含了Kafka、 Zokeper、Spark，以及上⽂我讲述的示例。

kafka-storm-starter

总体来说，我对我的初次Spark Streaming体验⾮常满意。当然，在Spark/Spark Streaming也存在⼀些 需要特别指明的问题，但是我肯定Spark社区终将解决这些问题。在这个过程中，得到了Spark社区积极 和热情的帮助，同时我也⾮常期待Spark 1.2版本的新特性。

在⼤型⽣产环境中，基于Spark还需要⼀些TLC才能达到Storm能⼒，这种情况我可能将它投⼊⽣产环境 中么？⼤部分情况下应该不会，更准确的说应该是现在不会。那么在当下，我⼜会使⽤Spark Streaming 做什么样的处理？这⾥有两个想法，我认为肯定存在更多：

它可以⾮常快的原型数据流。如果你因为数据流太⼤⽽遭遇扩展性问题，你可以运⾏ Spark Streaming，在⼀些样本数据或者⼀部分数据中。 搭配使⽤Storm和Spark Streaming。举个例⼦，你可以使⽤Storm将原始、⼤规模输⼊数据处理到易 管理等级，然后使⽤Spark Streaming来做下⼀步的分析，因为后者可以开箱即⽤⼤量有趣的算法、 计算指令和⽤例。

感谢Spark社区对⼤数据领域所作出的贡献！

