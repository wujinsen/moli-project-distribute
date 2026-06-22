Spark：⼀个⾼效的分布式计算系统

参考： 在看spark Streaming，我们需要⾸先知道什么是Spark streaming？ Spark streaming: 构建在Spark上处理Stream数据的框架，基本的原理是将Stream数据分成⼩的时间⽚ 断（⼏秒），以类似batch批量处理的⽅式来处理这⼩部分数据。Spark Streaming构建在Spark上，⼀ ⽅⾯是因为Spark的低延迟执⾏引擎（100ms+）可以⽤于实时计算，另⼀⽅⾯相⽐基于Record的其它 处理框架（如Storm），RDD数据集更容易做⾼效的容错处理。此外⼩批量处理的⽅式使得它可以同时 兼容批量和实时数据处理的逻辑和算法。⽅便了⼀些需要历史数据和实时数据联合分析的特定应⽤场 合。

![image 1](<SparkStreaming编程指南.note_images/imageFile1.png>)

Overview Spark Streaming属于Spark的核⼼api，它⽀持⾼吞吐量、⽀持容错的实时流数据处理。 它可以接受来⾃Kafka, Flume, Twiter, ZeroMQ和TCP Socket的数据源，使⽤简单的api函数⽐如 map, reduce, join, window等操作，还可以直接使⽤内置的机器学习算法、图算法包来处理数据。

![image 2](<SparkStreaming编程指南.note_images/imageFile2.png>)

它的⼯作流程像下⾯的图所示⼀样，接受到实时数据后，给数据分批次，然后传给Spark Engine处理 最后⽣成该批次的结果。

![image 3](<SparkStreaming编程指南.note_images/imageFile3.png>)

它⽀持的数据流叫Dstream，直接⽀持Kafka、Flume的数据源。Dstream是⼀种连续的RDs，下⾯是 ⼀个例⼦帮助⼤家理解Dstream。

# A Quick Example

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.


/ 创建StreamingContext，1秒⼀个批次 val sc = new StreamingContext(sparkConf, Seconds(1);

/ 获得⼀个DStream负责连接 监听端⼝:地址 val lines =sc.socketTextStream(serverIP, serverPort);

/ 对每⼀⾏数据执⾏Split操作 val words = lines.flatMap(_.split(" ");

/ 统计word的数量 val pairs = words.map(word => (word, 1); val wordCounts = pairs.reduceByKey(_ + _);

/ 输出结果 wordCounts.print();

sc.start(); / 开始 sc.awaitTermination(); / 计算完毕退出复制代码

具体的代码可以访问这个⻚⾯：

htps:/github.com/apache/incuba. workWordCount.scala

如果已经装好Spark的朋友，我们可以通过下⾯的例⼦试试。

⾸先，启动Netcat，这个⼯具在Unix-like的系统都存在，是个简易的数据服务器。

使⽤下⾯这句命令来启动Netcat：

1.

$ nc -lk 9复制代码

接着启动example

1.

$ ./bin/run-example org.apache.spark.streaming.examples.NetworkWordCount local[2] localhost 9复制代码

在Netcat这端输⼊hello world，看Spark这边的

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.


- # TERMINAL 1: # Runing Netcat

$ nc -lk 9

helo world

.

- # TERMINAL 2: RUNING NetworkWordCount or JavaNetworkWordCount


$ ./bin/run-example org.apache.spark.streaming.examples.NetworkWordCount local[2] localhost 9

.

Time: 13570843 0 ms

-

(helo,1) (world,1) .复制代码

Basics 下⾯这块是如何编写代码的啦，哇咔咔！ ⾸先我们要在SBT或者Maven⼯程添加以下信息：

1.

groupId = org.apache.spark

artifactId = spark-streaming_2.10 version = 0.9.0-incubating复制代码

3.

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


/需要使⽤⼀下数据源的，还要添加相应的依赖 Source Artifact Kafka spark-streaming-kafka_2.10 Flume spark-streaming-flume_2.10 Twiter spark-streaming-twiter_2.10 ZeroMQ spark-streaming-zeromq_2.10 MQT spark-streaming-mqt_2.10复制代码

接着就是实例化

1.

new StreamingContext(master, apName, batchDuration, [sparkHome], [jars])复制代码

这是之前的例⼦对DStream的操作。

![image 4](<SparkStreaming编程指南.note_images/imageFile4.png>)

# Input Sources

除了sockets之外，我们还可以这样创建Dstream

1.

streamingContext.fileStream(dataDirectory)复制代码

这⾥有3个要点：

- （1）dataDirectory下的⽂件格式都是⼀样
- （2）在这个⽬录下创建⽂件都是通过移动或者重命名的⽅式创建的
- （3）⼀旦⽂件进去之后就不能再改变


假设我们要创建⼀个Kafka的Dstream。

- 1.
- 2.


import org.apache.spark.streaming.kafka._ KafkaUtils.createStream(streamingContext, kafkaParams, .)复制代码

如果我们需要⾃定义流的receiver，可以查看 Operations 对于Dstream，我们可以进⾏两种操作，transformations 和 output Transformations

htps:/spark.incubator.apache.o. stom-receivers.html

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.


Transformation Meaning map(func) 对每⼀个元素执⾏func⽅法 flatMap(func) 类似map函数，但是可以map到0+个输出 filter(func) 过滤 repartition(numPartitions) 增加分区，提⾼并⾏度 union(otherStream) 合并两个流 count() 统计元素的个数 reduce(func) 对RDs⾥⾯的元素进⾏聚合操作，2个输⼊参数，1个输出参数 countByValue() 针对类型统计，当⼀个Dstream的元素的类型是K的时候，调⽤它会返 回⼀个新的Dstream，包含<K,Long>键值对，Long是每个K出现的频率。 reduceByKey(func, [numTasks]) 对于⼀个(K, V)类型的Dstream，为每个key，执⾏func函数， 默认是local是2个线程，cluster是8个线程，也可以指定numTasks join(otherStream, [numTasks]) 把(K, V)和(K, W)的Dstream连接成⼀个(K, (V, W)的新Dstream cogroup(otherStream, [numTasks]) 把(K, V)和(K, W)的Dstream连接成⼀个(K, Seq[V], Seq[W]) 的新Dstream transform(func) 转换操作，把原来的RD通过func转换成⼀个新的RD updateStateByKey(func) 针对key使⽤func来更新状态和值，可以将state该为任何值复制代码

UpdateStateByKey Operation 使⽤这个操作，我们是希望保存它状态的信息，然后持续的更新它，使⽤它有两个步骤：

- （1）定义状态，这个状态可以是任意的数据类型
- （2）定义状态更新函数，从前⼀个状态更改新的状态 下⾯展示⼀个例⼦：


1.

def updateFunction(newValues: Seq[Int], runingCount: Option[Int]): Option[Int] = {

val newCount =. / ad the new values with the previous runing count to get the new count

- 3.
- 4.


Some(newCount) }复制代码

它可以⽤在包含(word, 1) 的Dstream当中，⽐如前⾯展示的example

1.

val runingCounts = pairs.updateStateByKey[Int](updateFunction _)复制代码

它会针对⾥⾯的每个word调⽤⼀下更新函数，newValues是最新的值，runingCount是之前的值。

Transform Operation 和transformWith⼀样，可以对⼀个Dstream进⾏RD->RD操作，⽐如我们要对Dstream流⾥的RD 和另外⼀个数据集进⾏join操作，但是Dstream的API没有直接暴露出来，我们就可以使⽤transform⽅ 法来进⾏这个操作，下⾯是例⼦：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


val spamInfoRD = sparkContext.hadopFile(.)/ RD containing spam information

val cleanedDStream = inputDStream.transform(rd => { rd.join(spamInfoRD).filter(.)/ join data stream with spam information to do data cleaning

. })复制代码

另外，我们也可以在⾥⾯使⽤机器学习算法和图算法。 Window Operations

![image 5](<SparkStreaming编程指南.note_images/imageFile5.png>)

、

先举个例⼦吧，⽐如前⾯的word count的例⼦，我们想要每隔10秒计算⼀下最近30秒的单词总数。

我们可以使⽤以下语句：

- 1.
- 2.


/ Reduce last 30 seconds of data, every 10 seconds val windowedWordCounts = pairs.reduceByKeyAndWindow(_ + _, Seconds(30), Seconds(10)

复制代码

这⾥⾯提到了windows的两个参数：

- （1）window length：window的⻓度是30秒，最近30秒的数据
- （2）slice interval：计算的时间间隔


通过这个例⼦，我们⼤概能够窗⼝的意思了，定期计算滑动的数据。

下⾯是window的⼀些操作函数，还是有点⼉理解不了window的概念，Meaning就不翻译了，直接删掉

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


Transformation Meaning window(windowLength, slideInterval) countByWindow(windowLength, slideInterval) reduceByWindow(func, windowLength, slideInterval) reduceByKeyAndWindow(func, windowLength, slideInterval, [numTasks]) reduceByKeyAndWindow(func, invFunc, windowLength, slideInterval, [numTasks]) countByValueAndWindow(windowLength, slideInterval, [numTasks])复制代码

# Output Operations

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


Output Operation Meaning print() 打印到控制台 foreachRD(func) 对Dstream⾥⾯的每个RD执⾏func，保存到外部系统 saveAsObjectFiles(prefix, [sufix]) 保存流的内容为SequenceFile, ⽂件名 : "prefixTIME_IN_MS[.sufix]". saveAsTextFiles(prefix, [sufix]) 保存流的内容为⽂本⽂件, ⽂件名 : "prefixTIME_IN_MS[.sufix]". saveAsHadopFiles(prefix, [sufix]) 保存流的内容为hadop⽂件, ⽂件名 : "prefixTIME_IN_MS[.sufix]".复制代码

Persistence Dstream中的RD也可以调⽤persist()⽅法保存在内存当中，但是基于window和state的操作， reduceByWindow,reduceByKeyAndWindow,updateStateByKey它们就是隐式的保存了，系统已经帮 它⾃动保存了。 从⽹络接收的数据(such as, Kafka, Flume, sockets, etc.)，默认是保存在两个节点来实现容错性，以序 列化的⽅式保存在内存当中。

RDD Checkpointing 状态的操作是基于多个批次的数据的。它包括基于window的操作和updateStateByKey。因为状态的操 作要依赖于上⼀个批次的数据，所以它要根据时间，不断累积元数据。为了清空数据，它⽀持周期性 的检查点，通过把中间结果保存到hdfs上。因为检查操作会导致保存到hdfs上的开销，所以设置这个 时间间隔，要很慎重。对于⼩批次的数据，⽐如⼀秒的，检查操作会⼤⼤降低吞吐量。但是检查的间 隔太⻓，会导致任务变⼤。通常来说，5-10秒的检查间隔时间是⽐较合适的。

- 1.
- 2.


sc.checkpoint(hdfsPath) /设置检查点的保存位置 dstream.checkpoint(checkpointInterval) /设置检查点间隔复制代码

对于必须设置检查点的Dstream，⽐如通过updateStateByKey和reduceByKeyAndWindow创建的 Dstream，默认设置是⾄少10秒。 Performance Tuning 对于调优，可以从两个⽅⾯考虑：

- （1）利⽤集群资源，减少处理每个批次的数据的时间
- （2）给每个批次的数据量的设定⼀个合适的⼤⼩ Level of Parallelism


像⼀些分布式的操作，⽐如reduceByKey和reduceByKeyAndWindow，默认的8个并发线程，可以通 过对应的函数提⾼它的值，或者通过修改参数spark.default.paralelism来提⾼这个默认值。 Task Launching Overheads 通过进⾏的任务太多也不好，⽐如每秒50个，发送任务的负载就会变得很重要，很难实现压秒级的时 延了，当然可以通过压缩来降低批次的⼤⼩。 Setting the Right Batch Size 要使流程序能在集群上稳定的运⾏，要使处理数据的速度跟上数据流⼊的速度。最好的⽅式计算这个 批量的⼤⼩，我们⾸先设置batch size为5-10秒和⼀个很低的数据输⼊速度。确实系统能跟上数据的速 度的时候，我们可以根据经验设置它的⼤⼩，通过查看⽇志看看Total delay的多⻓时间。如果delay的 ⼩于batch的，那么系统可以稳定，如果delay⼀直增加，说明系统的处理速度跟不上数据的输⼊速 度。 24/7 Operation Spark默认不会忘记元数据，⽐如⽣成的RD，处理的stages，但是Spark Streaming是⼀个24/7的程 序，它需要周期性的清理元数据，通过spark.cleaner.tl来设置。⽐如我设置它为60，当超过10分钟 的时候，Spark就会清楚所有元数据，然后持久化RDs。但是这个属性要在SparkContext 创建之前设 置。

但是这个值是和任何的window操作绑定。Spark会要求输⼊数据在过期之后必须持久化到内存当中， 所以必须设置delay的值⾄少和最⼤的window操作⼀致，如果设置⼩了，就会报错。 Monitoring 除了Spark内置的监控能⼒，还可以StreamingListener这个接⼝来获取批处理的时间, 查询时延, 全部 的端到端的试验。 Memory TuningSpark Stream默认的序列化⽅式是 ，⽽不是RD 的 。

StorageLevel.MEMORY_ONLY_SER StorageLevel.MEMORY_ONLY

默认的，所有持久化的RD都会通过被Spark的LRU算法剔除出内存，如果设置了spark.cleaner.tl，就 会周期性的清理，但是这个参数设置要很谨慎。⼀个更好的⽅法是设置spark.streaming.unpersist为 true，这就让Spark来计算哪些RD需要持久化，这样有利于提⾼GC的表现。 推荐使⽤concurent mark-and-swep GC，虽然这样会降低系统的吞吐量，但是这样有助于更稳定的 进⾏批处理。 Fault-tolerance PropertiesFailure of a Worker Node 下⾯有两种失效的⽅式：

- 1.使⽤hdfs上的⽂件，因为hdfs是可靠的⽂件系统，所以不会有任何的数据失效。
- 2.如果数据来源是⽹络，⽐如Kafka和Flume，为了防⽌失效，默认是数据会保存到2个节点上，但是有 ⼀种可能性是接受数据的节点挂了，那么数据可能会丢失，因为它还没来得及把数据复制到另外⼀个 节点。 Failure of the Driver Node


为了⽀持24/7不间断的处理，Spark⽀持驱动节点失效后，重新恢复计算。Spark Streaming会周期性 的写数据到hdfs系统，就是前⾯的检查点的那个⽬录。驱动节点失效之后，StreamingContext可以被 恢复的。

为了让⼀个Spark Streaming程序能够被回复，它需要做以下操作： （1）第⼀次启动的时候，创建 StreamingContext，创建所有的streams，然后调⽤start()⽅法。 （2）恢复后重启的，必须通过检查点的数据重新创建StreamingContext。

下⾯是⼀个实际的例⼦： 通过StreamingContext.getOrCreate来构造StreamingContext，可以实现上⾯所说的。

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.


/ Function to create and setup a new StreamingContext

def functionToCreateContext(): StreamingContext = { val sc = new StreamingContext(.) / new context val lines =sc.socketTextStream(.)/ create DStreams

. sc.checkpoint(checkpointDirectory) / set checkpoint directory sc

}

/ Get StreaminContext from checkpoint data or create a new one val context = StreamingContext.getOrCreate(checkpointDirectory, functionToCreateContext _)

/ Do aditional setup on context that neds to be done, / irespective of whether it is being started or restarted

context. .

/ Start the context context.start() context.awaitTermination()复制代码

在stand-alone的部署模式下⾯，驱动节点失效了，也可以⾃动恢复，让别的驱动节点替代它。这个可 以在本地进⾏测试，在提交的时候采⽤supervise模式，当提交了程序之后，使⽤jps查看进程，看到类 似DriverWraper就杀死它，如果是使⽤YARN模式的话就得使⽤其它⽅式来重新启动了。

这⾥顺便提⼀下向客户端提交程序吧，之前总结的时候把这块给落下了。

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.


./bin/spark-clas org.apache.spark.deploy.Client launch [client-options] \ <cluster-url> <aplication-jar-url> <main-clas> \ [aplication-options]

cluster-url: master的地址. aplication-jar-url: jar包的地址，最好是hdfs上的,带上hdfs： /.否则要所有的节点的⽬录下都有 这个jar的 main-clas: 要发布的程序的main函数所在类. Client Options:

- -memory <count> (驱动程序的内存，单位是MB)
- -cores <count> (为你的驱动程序分配多少个核⼼)
- -supervise (节点失效的时候，是否重新启动应⽤)
- -verbose (打印增量的⽇志输出)复制代码


在未来的版本，会⽀持所有的数据源的可恢复性。

为了更好的理解基于HDFS的驱动节点失效恢复，下⾯⽤⼀个简单的例⼦来说明：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.


Time Number of lines in input file Output without driver failure Output with driver failure 10 10 10 20 20 20 30 30 30 40 40 [DRIVER

FAILS] no output

50 50 no output 60 60 no output 70 70 [DRIVER

RECOVERS] 40, 50, 60, 70

80 80 80 90 90 90

10 10 10复制代码

## 在4的时候出现了错误，40,50,60都没有输出，到70的时候恢复了，恢复之后把之前没输出的⼀下⼦全 部输出。

