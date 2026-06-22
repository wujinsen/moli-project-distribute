- 1、创建maven项⽬（略）
- 2、导⼊pom依赖⽂件
- 3、编写wordcount代码 -注意：我⽤的jdk1.8，jdk1.8提供了函数式编程，代码最后的 forEach(Consumer)就是jdk1.8提供的


<dependency> <groupId>org.apache.spark</groupId> <artifactId>spark-core_2.11</artifactId> <version>1.6.0</version>

</dependency>

import org.apache.spark.SparkConf; import org.apache.spark.api.java.JavaRDD; import org.apache.spark.api.java.JavaSparkContext; import org.apache.spark.api.java.function.FlatMapFunction; import org.apache.spark.api.java.function.Function2; import org.apache.spark.api.java.function.PairFunction; import scala.Tuple2;

import java.util.ArrayList; import java.util.List; import java.util.function.Consumer;

public class WordCount {

public static void main(String[] args) { //初始化配置⽂件 SparkConf sparkConf = new SparkConf().setAppName("javaSparkApp");

- //1、创建核⼼的SparkContext类，SparkContext在Java中的实现是 JavaSparkContext JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

- //2、通过JavaSparkContext加载数据⽂件，使⽤封装好的textFile读取⽂件，并 缓存到内存中。


// 注意这⾥的textFile、cache是⼀个transformation,不进⾏实际的操作。

# JavaRDD textRDD = sparkContext.textFile("E:\\words.txt").cache();

- //3、通过RDD.ﬂatMap的⽅式，读取⽂档中的数据，⼀条⼀条。 textRDD.ﬂatMap(new FlatMapFunction() { //3.1、对读取的句⼦进⾏单词切割，并返回⼀个迭代器Iterable，Iterable是⼀ 个接⼜，ArrayList是其实现类之⼀ public Iterable call(Object o) throws Exception {

String message = (String) o; String[] strArr = message.split(" "); List<String> wordList = new ArrayList<String>();

for (String word : strArr) { wordList.add(word);

} return wordList; }

- //4、将普通RDD转换成JavaPairRDD }).mapToPair(new PairFunction() { public Tuple2 call(Object o) throws Exception { return new Tuple2<String, Integer>((String) o, 1); }

- //5、对相同Key的value，进⾏累加


// 注意：这⾥输⼊的两个参数都是value，⼀个是累加的总value，⼀个是 下⼀个将要被累加的value }).reduceByKey(new Function2() {

public Object call(Object v1, Object v2) throws Exception { return (Integer)v1+(Integer)v2; }

//6、执⾏collect⽅法，将最终的结果封装到List<Tuple>的⽅式，返回给客户 端

//注意：collect⽅法是⼀个action⽅法，也就是说上⾯的所有⽅法都是 transformation，只有到这⾥才会真正开始执⾏。 }).collect().forEach(new Consumer() { public void accept(Object o) {

Tuple2 tuple = (Tuple2) o; System.out.println(tuple._1() + ": " + tuple._2()); }

}); sparkContext.stop(); } }

- 4、代码编写完毕之后，在运⾏时，假如参数 -Dspark.master=local
- 5、运⾏效果如下 "D:\Program Files\Java\jdk1.8.0_73\bin\java" …


![image 1](<5、在Idea编辑器上创建maven项目，通过Java编写Spark WordCount案例.note_images/imageFile1.png>)

com.intelij.rt.execution.aplication.ApMain cn.maoxiangyi.spark.WordCount Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties 16/03/181 50 43 INFO SparkContext: Runing Spark version 1.6.0

16/03/181 50 43 WARN NativeCodeLoader: Unable to load nativehadop library for your platform. using builtin-java clases where aplicable 16/03/181 50  4 INFO SecurityManager: Changing view acls to: maoxiangyi 16/03/181 50  4 INFO SecurityManager: Changing modify acls to: maoxiangyi 16/03/181 50  4 INFO SecurityManager: SecurityManager: authentication disabled; ui acls disable d; users with view permisions: Set(maoxiangyi); users with modify permisions: Set(maoxiangyi) 16/03/181 50  4 INFO Utils: Sucesfuly started service 'sparkDriver' on port 5876. 16/03/181 50 45 INFO Slf4jLoger: Slf4jLoger started 16/03/181 50 45 INFO Remoting: Starting remoting 16/03/181 50 45 INFO Remoting: Remoting started; listening on adreses :[aka.tcp:/

sparkDrive rActorSystem@192.168.56.1

:5879] 16/03/181 50 45 INFO Utils: Sucesfuly started service 'sparkDriverActorSystem' on port 5879

. 16/03/181 50 45 INFO SparkEnv: Registering MapOutputTracker 16/03/181 50 45 INFO SparkEnv: Registering BlockManagerMaster 16/03/181 50 45 INFO DiskBlockManager: Created local directory at C:\Users\maoxiangyi\ApDat a\Local\Temp\blockmgr-4ea15741-c16-4376-94bf-704fb58e6c8 16/03/181 50 45 INFO MemoryStore: MemoryStore started with capacity 2.4 GB 16/03/181 50 45 INFO SparkEnv: Registering OutputComitCordinator 16/03/181 50 45 INFO Utils: Sucesfuly started service 'SparkUI' on port 4040. 16/03/181 50 45 INFO SparkUI: Started SparkUI at 16/03/181 50 45 INFO Executor: Starting executor ID driver on host localhost 16/03/181 50 45 INFO Utils: Sucesfuly started service 'org.apache.spark.network.nety.NetyBl ockTransferService' on port 58786.

htp:/192.168.56.1 4040

- 16/03/181 50 45 INFO NetyBlockTransferService: Server created on 58786

- 16/03/181 50 45 INFO BlockManagerMaster: Trying to register BlockManager

- 16/03/181 50 45 INFO BlockManagerMasterEndpoint: Registering block manager localhost:58786 with 2.4 GB RAM, BlockManagerId(driver, localhost, 58786)

- 16/03/181 50 45 INFO BlockManagerMaster: Registered BlockManager
- 16/03/181 50 46 INFO MemoryStore: Block broadcast_0 stored as values in memory (estimated si ze 107.7 KB, fre 107.7 KB)


- 16/03/181 50 46 INFO MemoryStore: Block broadcast_0_piece0 stored as bytes in memory (esti mated size 9.8 KB, fre17.5 KB)


- 16/03/181 50 46 INFO BlockManagerInfo: Aded broadcast_0_piece0 in memory on localhost:58 786 (size: 9.8 KB, fre: 2.4 GB)


- 16/03/181 50 46 INFO SparkContext: Created broadcast 0 from textFile at WordCount.java:24


- 16/03/181 50 48 WARN : Your hostname, maoxiangyi-PC resolves to a l opback/nonreachable adres: fe80 0 0 0 0 5efe:ac10 20%net13, but we couldn't find any external IP adres!
- 16/03/181 50 49 INFO FileInputFormat: Total input paths to proces : 1 16/03/181 50 49 INFO SparkContext: Starting job: colect at WordCount.java:50 16/03/181 50 49 INFO DAGScheduler: Registering RD 3 (mapToPair at WordCount.java:38) 16/03/181 50 49 INFO DAGScheduler: Got job 0 (colect at WordCount.java:50) with 1 output parti tions 16/03/181 50 49 INFO DAGScheduler: Final stage: ResultStage 1 (colect at WordCount.java:50) 16/03/181 50 49 INFO DAGScheduler: Parents of final stage: List(ShufleMapStage 0) 16/03/181 50 49 INFO DAGScheduler: Mising parents: List(ShufleMapStage 0) 16/03/181 50 49 INFO DAGScheduler: Submiting ShufleMapStage 0 (MapPartitionsRD[3] at m apToPair at WordCount.java:38), which has no mising parents


- 16/03/181 50 49 INFO MemoryStore: Block broadcast_1 stored as values in memory (estimated si ze 4.6 KB, fre 12.1 KB)


- 16/03/181 50 49 INFO MemoryStore: Block broadcast_1_piece0 stored as bytes in memory (estim ated size 2.6 KB, fre 124.7 KB)


- 16/03/181 50 49 INFO BlockManagerInfo: Aded broadcast_1_piece0 in memory on localhost:587 86 (size: 2.6 KB, fre: 2.4 GB)


- 16/03/181 50 49 INFO SparkContext: Created broadcast 1 from broadcast at DAGScheduler.scala: 106 16/03/181 50 49 INFO DAGScheduler: Submiting 1 mising tasks from ShufleMapStage 0 (MapP artitionsRD[3] at mapToPair at WordCount.java:38)


- 16/03/181 50 49 INFO TaskSchedulerImpl: Ading task set 0.0 with 1 tasks


- 16/03/181 50 49 INFO TaskSetManager: Starting task 0.0 in stage 0.0 (TID 0, localhost, partition 0 ,PROCES_LOCAL, 2028 bytes)


- 16/03/181 50 49 INFO Executor: Runing task 0.0 in stage 0.0 (TID 0) 16/03/181 50 49 INFO CacheManager: Partition rd_1_0 not found, computing it 16/03/181 50 49 INFO HadopRD: Input split: file:/E:/words.txt:0+37 16/03/181 50 49 INFO deprecation: mapred.tip.id is deprecated. Instead, use mapreduce.task.id 16/03/181 50 49 INFO deprecation: mapred.task.id is deprecated. Instead, use mapreduce.task.at tempt.id 16/03/181 50 49 INFO deprecation: mapred.task.is.map is deprecated. Instead, use mapreduce.ta sk.ismap 16/03/181 50 49 INFO deprecation: mapred.task.partition is deprecated. Instead, use mapreduce. task.partition 16/03/181 50 49 INFO deprecation: mapred.job.id is deprecated. Instead, use mapreduce.job.id


16/03/181 50 49 INFO MemoryStore: Block rd_1_0 stored as values in memory (estimated size 2 24.0 B, fre 124.9 KB) 16/03/181 50 49 INFO BlockManagerInfo: Aded rd_1_0 in memory on localhost:58786 (size: 2 4.0 B, fre: 2.4 GB)

- 16/03/181 50 49 INFO Executor: Finished task 0.0 in stage 0.0 (TID 0). 2752 bytes result sent to d river

- 16/03/181 50 49 INFO TaskSetManager: Finished task 0.0 in stage 0.0 (TID 0) in 162 ms on localh ost (1/1)


- 16/03/181 50 49 INFO TaskSchedulerImpl: Removed TaskSet 0.0, whose tasks have al completed, from pol


16/03/181 50 49 INFO DAGScheduler: ShufleMapStage 0 (mapToPair at WordCount.java:38) finis hed in 0.180 s 16/03/181 50 49 INFO DAGScheduler: l oking for newly runable stages 16/03/181 50 49 INFO DAGScheduler: runing: Set() 16/03/181 50 49 INFO DAGScheduler: waiting: Set(ResultStage 1) 16/03/181 50 49 INFO DAGScheduler: failed: Set() 16/03/181 50 49 INFO DAGScheduler: Submiting ResultStage 1 (ShufledRD[4] at reduceByKey at WordCount.java: 4), which has no mising parents 16/03/181 50 49 INFO MemoryStore: Block broadcast_2 stored as values in memory (estimated si ze 2.8 KB, fre 127.7 KB) 16/03/181 50 49 INFO MemoryStore: Block broadcast_2_piece0 stored as bytes in memory (esti mated size 1683.0 B, fre 129.3 KB) 16/03/181 50 49 INFO BlockManagerInfo: Aded broadcast_2_piece0 in memory on localhost:58 786 (size: 1683.0 B, fre: 2.4 GB) 16/03/181 50 49 INFO SparkContext: Created broadcast 2 from broadcast at DAGScheduler.scala :106 16/03/181 50 49 INFO DAGScheduler: Submiting 1 mising tasks from ResultStage 1 (ShufledRD D[4] at reduceByKey at WordCount.java: 4) 16/03/181 50 49 INFO TaskSchedulerImpl: Ading task set 1.0 with 1 tasks 16/03/181 50 49 INFO TaskSetManager: Starting task 0.0 in stage 1.0 (TID 1, localhost, partition 0, NODE_LOCAL, 1813 bytes) 16/03/181 50 49 INFO Executor: Runing task 0.0 in stage 1.0 (TID 1) 16/03/181 50 49 INFO ShufleBlockFetcherIterator: Geting 1 non-empty blocks out of 1 blocks 16/03/181 50 49 INFO ShufleBlockFetcherIterator: Started 0 remote fetches in 4 ms

- 16/03/181 50 49 INFO Executor: Finished task 0.0 in stage 1.0 (TID 1). 1390 bytes result sent to dr iver


16/03/181 50 49 INFO DAGScheduler: ResultStage 1 (colect at WordCount.java:50) finished in 0.0 39 s

- 16/03/181 50 49 INFO TaskSetManager: Finished task 0.0 in stage 1.0 (TID 1) in 39 ms on localhos t (1/1)


- 16/03/181 50 49 INFO TaskSchedulerImpl: Removed TaskSet 1.0, whose tasks have al completed, from pol


16/03/181 50 49 INFO DAGScheduler: Job 0 finished: colect at WordCount.java:50, tok 0.3282 6 s am: 3 i: 3 tom: 1 hanmeimei: 1 lilei: 1 16/03/181 50 49 INFO SparkUI: Stoped Spark web UI at 16/03/181 50 49 INFO MapOutputTrackerMasterEndpoint: MapOutputTrackerMasterEndpoint sto

htp:/192.168.56.1 4040

ped! 16/03/181 50 49 INFO MemoryStore: MemoryStore cleared 16/03/181 50 49 INFO BlockManager: BlockManager stoped 16/03/181 50 49 INFO BlockManagerMaster: BlockManagerMaster stoped 16/03/181 50 49 INFO OutputComitCordinator$OutputComitCordinatorEndpoint: OutputCo

mitCordinator stoped! 16/03/181 50 49 INFO SparkContext: Sucesfuly stoped SparkContext 16/03/181 50 49 INFO RemoteActorRefProvider$RemotingTerminator: Shuting down remote dae mon. 16/03/181 50 49 INFO RemoteActorRefProvider$RemotingTerminator: Remote daemon shut down ; proceding with flushing remote transports. 16/03/181 50 49 INFO ShutdownHokManager: Shutdown hok caled 16/03/181 50 49 INFO ShutdownHokManager: Deleting directory C:\Users\maoxiangyi\ApData\ Local\Temp\spark-29734c78-b621-413-a16-f7657f84930f 16/03/181 50 49 INFO RemoteActorRefProvider$RemotingTerminator: Remoting shut down.

声明：本系列博⽂是在学习耿嘉安《深⼊理解Spark 核⼼思想与源码分析》、⾼彦杰《Spark⼤数据处 理》、张安战《Spark技术内幕》及互联⽹公开博客资料后，摘抄或者拷⻉相关内容整理⽽成，个别知 识点会有⾃⼰的理解并输出。欢迎转载、使⽤、重新发布，但务必保留相关图书的信息，并且不得⽤ 于商业⽬的，基于本⽂修改后的作品务必以相同的声明及许可发布。如有任何疑问，请与我联系。

## 技术讨论群： 138712835（需付费-会定期以发群红包的⽅式，将⼊群⾦额返回到群⾥⾯，本⼈不 赚取⼀分钱）

