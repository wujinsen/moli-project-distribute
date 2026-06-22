前提：需要在idea上安装scala插件（略）

Scala的代码⽐Java代码简洁的多，看完这个例⼦，你或许或者⼩岳岳⼀样说道，咿，这么神奇！

- 1、创建maven项⽬（略）
- 2、导⼊pom依赖⽂件
- 3、编写wordcount代码


- 1 <dependency>

- 2 <groupId>org.apache.spark</groupId>

- 3 <artifactId>spark-core_2.11</artifactId>

- 4 <version>1.6.0</version>

- 5 </dependency>


- 1 import org.apache.spark.{SparkConf, SparkContext}

- 2

- 3 object WordCount {

- 4

- 5 def main(args: Array[String]) {

- 6 //1、创建⼀个sparkContext对象，并设置当前driverApp的名字

- 7 new SparkContext(new SparkConf().setAppName("myScalaApp"))

- 8 //2、通过SparkContext加载数据⽂件

- 9 .textFile("E:\\words.txt", 1)

- 10 //3、对该数据⽂件所有的block(hadoop hdfs 概念)⽂件，进⾏读取并按照指定的代码进⾏操作

- 11 .flatMap(line => line.split(" "))

- 12 //4、对步骤3的结果集进⾏处理,对每个单词加1

- 13 .map(word => (word, 1))

- 14 //5、对⽂件中出现的同⼀个单词进⾏累加

- 15 //这⾥输⼊的两个参数都是value，⼀个是累加的总value，⼀个是下⼀个将要被累加的value

- 16 .reduceByKey((x,y) => x+y)

- 17 //6、将最后的结果action成为⼀个list

- 18 .collect()

- 19 //7、打印list，从元组中获取⻆标1的值、⻆标2的值

- 20 .foreach(tuple =>println(tuple._1+" "+tuple._2))

- 21 }

- 22 }


- 4、代码编写完毕之后，在运⾏时，假如参数 -Dspark.master=local
- 5、运⾏效果如下 "D:\Program Files\Java\jdk1.8.0_73\bin\java" …


![image 1](<6、在Idea编辑器上创建maven项目，通过Scala编写Spark WordCount案例.note_images/imageFile1.png>)

com.intelij.rt.execution.aplication.ApMain WordCount Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties

- 16/03/18 12 48 56 INFO SparkContext: Runing Spark version 1.6.0
- 16/03/18 12 48 57 WARN NativeCodeLoader: Unable to load nativehadop library for your platform. using builtin-java clases where aplicable


- 16/03/18 12 48 57 INFO SecurityManager: Changing view acls to: maoxiangyi


- 16/03/18 12 48 57 INFO SecurityManager: Changing modify acls to: maoxiangyi


- 16/03/18 12 48 57 INFO SecurityManager: SecurityManager: authentication disabled; ui acls disabl ed; users with view permisions: Set(maoxiangyi); users with modify permisions: Set(maoxiangyi)
- 16/03/18 12 48 58 INFO Utils: Sucesfuly started service 'sparkDriver' on port 59490.
- 16/03/18 12 48 59 INFO Slf4jLoger: Slf4jLoger started


- 16/03/18 12 48 59 INFO Remoting: Starting remoting sparkDrive


- 16/03/18 12 48 59 INFO Remoting: Remoting started; listening on adreses :[aka.tcp:/ :59503]


rActorSystem@192.168.56.1

- 16/03/18 12 48 59 INFO Utils: Sucesfuly started service 'sparkDriverActorSystem' on port 5950 3.


- 16/03/18 12 48 59 INFO SparkEnv: Registering MapOutputTracker


- 16/03/18 12 48 59 INFO SparkEnv: Registering BlockManagerMaster


- 16/03/18 12 48 59 INFO DiskBlockManager: Created local directory at C:\Users\maoxiangyi\ApDat a\Local\Temp\blockmgr-56901a95-06c1-45fe-95d5-ea4fb939180


- 16/03/18 12 48 59 INFO MemoryStore: MemoryStore started with capacity 2.4 GB


- 16/03/18 12 48 59 INFO SparkEnv: Registering OutputComitCordinator


- 16/03/18 12 48 59 INFO Utils: Sucesfuly started service 'SparkUI' on port 4040.


- 16/03/18 12 48 59 INFO SparkUI: Started SparkUI at


htp:/192.168.56.1 4040

- 16/03/18 12 48 59 INFO Executor: Starting executor ID driver on host localhost


- 16/03/18 12 48 59 INFO Utils: Sucesfuly started service 'org.apache.spark.network.nety.NetyBl ockTransferService' on port 59510.


- 16/03/18 12 48 59 INFO NetyBlockTransferService: Server created on 59510


- 16/03/18 12 48 59 INFO BlockManagerMaster: Trying to register BlockManager


- 16/03/18 12 48 59 INFO BlockManagerMasterEndpoint: Registering block manager localhost:59510 with 2.4 GB RAM, BlockManagerId(driver, localhost, 59510)

- 16/03/18 12 48 59 INFO BlockManagerMaster: Registered BlockManager
- 16/03/18 12 49  0 INFO MemoryStore: Block broadcast_0 stored as values in memory (estimated s ize 107.7 KB, fre 107.7 KB)


- 16/03/18 12 49  0 INFO MemoryStore: Block broadcast_0_piece0 stored as bytes in memory (esti mated size 9.8 KB, fre17.5 KB)


- 16/03/18 12 49  0 INFO BlockManagerInfo: Aded broadcast_0_piece0 in memory on localhost:59 510 (size: 9.8 KB, fre: 2.4 GB)


- 16/03/18 12 49  0 INFO SparkContext: Created broadcast 0 from textFile at WordCount.scala:7


- 16/03/18 12 49 02 WARN : Your hostname, maoxiangyi-PC resolves to a l opback/nonreachable adres: fe80 0 0 0 0 5efe:ac10 20%net13, but we couldn't find any external IP adres!


- 16/03/18 12 49 03 INFO FileInputFormat: Total input paths to proces : 1


- 16/03/18 12 49 03 INFO SparkContext: Starting job: colect at WordCount.scala:1


- 16/03/18 12 49 03 INFO DAGScheduler: Registering RD 3 (map at WordCount.scala:9)


- 16/03/18 12 49 03 INFO DAGScheduler: Got job 0 (colect at WordCount.scala:1) with 1 output part itions


- 16/03/18 12 49 03 INFO DAGScheduler: Final stage: ResultStage 1 (colect at WordCount.scala:1)


- 16/03/18 12 49 03 INFO DAGScheduler: Parents of final stage: List(ShufleMapStage 0)


- 16/03/18 12 49 03 INFO DAGScheduler: Mising parents: List(ShufleMapStage 0)


- 16/03/18 12 49 03 INFO DAGScheduler: Submiting ShufleMapStage 0 (MapPartitionsRD[3] at m ap at WordCount.scala:9), which has no mising parents


- 16/03/18 12 49 03 INFO MemoryStore: Block broadcast_1 stored as values in memory (estimated si ze 3.9 KB, fre 121.4 KB)


- 16/03/18 12 49 03 INFO MemoryStore: Block broadcast_1_piece0 stored as bytes in memory (esti mated size 2.2 KB, fre 123.6 KB)


- 16/03/18 12 49 03 INFO BlockManagerInfo: Aded broadcast_1_piece0 in memory on localhost:59 510 (size: 2.2 KB, fre: 2.4 GB)


- 16/03/18 12 49 03 INFO SparkContext: Created broadcast 1 from broadcast at DAGScheduler.scala :106


- 16/03/18 12 49 03 INFO DAGScheduler: Submiting 1 mising tasks from ShufleMapStage 0 (MapP artitionsRD[3] at map at WordCount.scala:9)


- 16/03/18 12 49 03 INFO TaskSchedulerImpl: Ading task set 0.0 with 1 tasks


- 16/03/18 12 49 03 INFO TaskSetManager: Starting task 0.0 in stage 0.0 (TID 0, localhost, partition 0,PROCES_LOCAL, 2028 bytes)


- 16/03/18 12 49 03 INFO Executor: Runing task 0.0 in stage 0.0 (TID 0) 16/03/18 12 49 03 INFO HadopRD: Input split: file:/E:/words.txt:0+37


16/03/18 12 49 03 INFO deprecation: mapred.tip.id is deprecated. Instead, use mapreduce.task.id 16/03/18 12 49 03 INFO deprecation: mapred.task.id is deprecated. Instead, use mapreduce.task.at tempt.id 16/03/18 12 49 03 INFO deprecation: mapred.task.is.map is deprecated. Instead, use mapreduce.ta sk.ismap 16/03/18 12 49 03 INFO deprecation: mapred.task.partition is deprecated. Instead, use mapreduce. task.partition 16/03/18 12 49 03 INFO deprecation: mapred.job.id is deprecated. Instead, use mapreduce.job.id

- 16/03/18 12 49 03 INFO Executor: Finished task 0.0 in stage 0.0 (TID 0). 253 bytes result sent to d river


- 16/03/18 12 49 03 INFO TaskSetManager: Finished task 0.0 in stage 0.0 (TID 0) in 160 ms on localh ost (1/1)


- 16/03/18 12 49 03 INFO TaskSchedulerImpl: Removed TaskSet 0.0, whose tasks have al completed , from pol 16/03/18 12 49 03 INFO DAGScheduler: ShufleMapStage 0 (map at WordCount.scala:9) finished in


0.175 s 16/03/18 12 49 03 INFO DAGScheduler: l oking for newly runable stages 16/03/18 12 49 03 INFO DAGScheduler: runing: Set() 16/03/18 12 49 03 INFO DAGScheduler: waiting: Set(ResultStage 1) 16/03/18 12 49 03 INFO DAGScheduler: failed: Set() 16/03/18 12 49 03 INFO DAGScheduler: Submiting ResultStage 1 (ShufledRD[4] at reduceByKey

at WordCount.scala:10), which has no mising parents 16/03/18 12 49 03 INFO MemoryStore: Block broadcast_2 stored as values in memory (estimated s ize 2.5 KB, fre 126.0 KB) 16/03/18 12 49 03 INFO MemoryStore: Block broadcast_2_piece0 stored as bytes in memory (esti mated size 1520.0 B, fre 127.5 KB) 16/03/18 12 49 03 INFO BlockManagerInfo: Aded broadcast_2_piece0 in memory on localhost:59 510 (size: 1520.0 B, fre: 2.4 GB) 16/03/18 12 49 03 INFO SparkContext: Created broadcast 2 from broadcast at DAGScheduler.scala :106 16/03/18 12 49 03 INFO DAGScheduler: Submiting 1 mising tasks from ResultStage 1 (ShufledRD D[4] at reduceByKey at WordCount.scala:10)

- 16/03/18 12 49 03 INFO TaskSchedulerImpl: Ading task set 1.0 with 1 tasks


- 16/03/18 12 49 03 INFO TaskSetManager: Starting task 0.0 in stage 1.0 (TID 1, localhost, partition 0, NODE_LOCAL, 1813 bytes)


- 16/03/18 12 49 03 INFO Executor: Runing task 0.0 in stage 1.0 (TID 1) 16/03/18 12 49 03 INFO ShufleBlockFetcherIterator: Geting 1 non-empty blocks out of 1 blocks


16/03/18 12 49 03 INFO ShufleBlockFetcherIterator: Started 0 remote fetches in 21 ms

- 16/03/18 12 49 03 INFO Executor: Finished task 0.0 in stage 1.0 (TID 1). 1390 bytes result sent to dr iver


- 16/03/18 12 49 03 INFO TaskSetManager: Finished task 0.0 in stage 1.0 (TID 1) in 75 ms on localhos t (1/1)


- 16/03/18 12 49 03 INFO TaskSchedulerImpl: Removed TaskSet 1.0, whose tasks have al completed, from pol


16/03/18 12 49 03 INFO DAGScheduler: ResultStage 1 (colect at WordCount.scala:1) finished in 0. 07 s 16/03/18 12 49 03 INFO DAGScheduler: Job 0 finished: colect at WordCount.scala:1, tok 0.3835 01 s am 3 i 3 tom 1 hanmeimei 1 lilei 1 16/03/18 12 49 03 INFO SparkContext: Invoking stop() from shutdown hok 16/03/18 12 49 03 INFO SparkUI: Stoped Spark web UI at 16/03/18 12 49 03 INFO MapOutputTrackerMasterEndpoint: MapOutputTrackerMasterEndpoint sto

htp:/192.168.56.1 4040

ped! 16/03/18 12 49 03 INFO MemoryStore: MemoryStore cleared 16/03/18 12 49 03 INFO BlockManager: BlockManager stoped 16/03/18 12 49 03 INFO BlockManagerMaster: BlockManagerMaster stoped 16/03/18 12 49 03 INFO OutputComitCordinator$OutputComitCordinatorEndpoint: OutputCo

mitCordinator stoped! 16/03/18 12 49 03 INFO SparkContext: Sucesfuly stoped SparkContext 16/03/18 12 49 03 INFO ShutdownHokManager: Shutdown hok caled 16/03/18 12 49 03 INFO ShutdownHokManager: Deleting directory C:\Users\maoxiangyi\ApData\ Local\Temp\spark-c708ad4-410f-415f-9dc2-06c8414e792 16/03/18 12 49 03 INFO RemoteActorRefProvider$RemotingTerminator: Shuting down remote dae mon. 16/03/18 12 49 03 INFO RemoteActorRefProvider$RemotingTerminator: Remote daemon shut dow n; proceding with flushing remote transports.

声明：本系列博⽂是在学习耿嘉安《深⼊理解Spark 核⼼思想与源码分析》、⾼彦杰《Spark⼤数据处理》、张安战 《Spark技术内幕》及互联⽹公开博客资料后，摘抄或者拷⻉相关内容整理⽽成，个别知识点会有⾃⼰的理解并输 出。欢迎转载、使⽤、重新发布，但务必保留相关图书的信息，并且不得⽤于商业⽬的，基于本⽂修改后的作品务必 以相同的声明及许可发布。如有任何疑问，请与我联系。

# 技术讨论群： 138712835（需付费-会定期以发群红包的⽅式，将⼊群⾦额返回到群⾥⾯，本⼈不赚取 ⼀分钱）

