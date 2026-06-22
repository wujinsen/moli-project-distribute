问题导读：

- 1.Spark 1.4为什么Master 经常挂掉？

- 2.为什么只有少数 Executor 在运⾏，别的 Executor ⻓时间空闲？

- 3.Spark 如何⽀持多⽤户？


![image 1](<Spark常见问题总结3.note_images/imageFile1.png>)

运维 Master 挂掉了，Standby 的 Master 也访问超时

升级到 Spark 1.4.1 之后，发现运⾏⼀段时间之后 Master 就会挂掉，⽽且 Standby 的 Master 也 ⽆法访问，⼀直处于访问超时的状态，最后把 Master 都杀掉，把 zookeeper ⾥⾯的状态全部删 掉，再启动就好了。通过查看 log，我们发现原来是内存不⾜导致的。

Master 的内存默认配置是 512M 的内存，尝试着把它的内存调⼤到 2G，发现还是挂，在⽇志⾥⾯ 发现每次挂的时候，⼏乎都有同⼀个任务在跑，仔细观察了⼀下这个任务，它居然有 300 多万个 Tasks，在 HDFS 上查看它的 Event log ⽇志，有好⼏个 G 的⼤⼩。看到这⾥，果断的把内存调到 10G。

问题倒是解决了，可为什么会是这样呢？从 log ⾥⾯它⽆法完成主从切换的时候，把它杀死最后 都是卡在 rebuildUI 这个⽅法上⾯了，打开 Master 这个类仔细⼀看，它会去读取每⼀⾏的 event log ⽇志，遇到⼤任务的时候，这是⼀个很恐怖的事情。

再继续查看，rebuildUI ⽅法是在 finishApplication ⽅法⾥⾯调⽤的，每次有任务完成，它都会把 任务的 Event log 读到内存当中来，然后在完成列表⾥⾯就可以点击看到作业的相关信息，这部分 信息原来是保存在 driver 端的。所以需要减少 Master 保存在内存当中的作业的信息，历史任务到 History server 当中查看。

对⽐了 Spark 1.3 的代码，发现 Spark1.3 ⾥也有这个逻辑，为什么 Spark1.3 没有发⽣这个问题 呢？Spark 1.4 在 UI 上增加了很酷的图表，需要消耗多⼀点内存也可以理解，但是为什么会多那 么多呢？和同事交流得知，升级到 Spark 1.4 之后，原来的程序在 Shuffle 的时候经常报错，他们 就通过调⼤ partition 数的⽅法来避免，有的程序⼀直调整到 10 倍于以前的 partition 数才可以正 常运⾏。partition 数太⼤还会引发别的问题，Job UI 的界⾯很卡，甚⾄是打不开，Akka 的线程数 也需要调整，默认值是 4。

spark.akka.threads 10

Worker 时不时也会挂掉或者没有⼼跳，但是出问题的周期要⻓⼀点，发现还是内存的原因，把 Worker 的内存调成 1G，并且减少保存在内存当中的 Executor 和 Driver 的信息，默认都是 1000 个。

spark.worker.ui.retainedExecutors 5

spark.worker.ui.retainedDrivers5

当 SQL 遇到 Hive 的 Bug

Spark 的 jar 包⾥⾯⾃带了⼀个 Hive 版本，在 Spark 1.3 版本上遇到 Hive 的 bug，需要把 bug 修 改好编译之后才替换掉 Spark 的 jar 包内部的 class 类，⾮常的痛苦。⽐如之前遇到⼀个很严重的 问题，在⼀张分区表上新增列，插⼊数据之后，发现新增的列查出来全是 NULL，这是 Hive 0.13 的已知 Bug：

https://issues.apache.org/jira/browse/HIVE-6131

Spark 1.4 ⽀持使⽤外置的 Hive 的 Metastore 的 jar 包，这样可以很⽅便的修改 hive 的代码，⽽ 不去影响 Spark 的代码。配置下⾯的⼀句就可以使⽤外置的 hive 的 jar 包了（注意：这⾥的 jar 包不仅包括 hive 依赖的所有 jar 包，包括 hadoop 的 jar 包。此外还需要把 guava-11.0.2.jar 添加 到 SPARK_CLASSPATH ⾥⾯）。

spark.sql.hive.metastore.jars=/data/spark-1.4.1/hive/*

由于采⽤了直接连接 Hive 的元数据库读取元数据的⽅式，在 Spark1.3 版本上使⽤ HiveContext 偶尔会发⽣以下 Hive 的经典问题：

DELETEME 表不存在，该问题会导致程序退出。 ⻆⾊ admin 已存在，该问题会导致程序退出。 往 Hive 的元数据库的 Version 表插⼊⼀条记录，该问题会导致所有的 Hive 客户端都⽆法使 ⽤！如果该问题发⽣在夜间，会影响所有的 Hive 例⾏任务。

在 Spark1.4 版本，上⾯的问题出现的频率变得很⾼。这些是 Hive 的问题，但是严重影响了 Spark 的使⽤。有两种处理⽅法：

- 1)采⽤连接 Hive 的 Metastore 服务的⽅式读取元数据。

- 2)修改代码，去掉⼀些不必要的操作。


修改 ObjectStore 类 verifySchema 的⽅法，直接返回，禁⽌它检查 Hive 的版本。 修改 HiveMetaStore 类，删除下⾯这段代码。

synchronized (HMSHandler.class) {

createDefaultDB(); # 创建默认数据库

createDefaultRoles(); # 创建默认⻆⾊

addAdminUsers(); # 创建默认⽤户

}

3)修改 MetaStoreDirectSql 类的构造⽅法，删除掉该构造⽅法⾥的 120 ⾏之后关于查询数据库的 代码。

Streaming 程序已失败，进程不退出

⽤户提交到 Yarn 上的 Spark Streaming 程序容易受到别的因素影响⽽导致程序失败，有时候程序 失败之后 driver 进程不退出，这样⽆法通过监控 driver 的进程来重启 Streaming 程序。推荐将 Streaming 程序运⾏在 Standalone 模式的集群之上，使⽤ cluster 部署模式，并启⽤ supervise 功 能。使⽤这种⽅式的好处是 Streaming 程序⾮正常退出之后，Spark 集群会⾃动重启 Streaming 的程序，⽆须⼈为⼲预。

任务调度Executor ⻓时间空闲

经常会碰到⼀种现象：只有少数 Executor 在运⾏，别的 Executor ⻓时间空闲。这种现象⽐较常 ⻅的原因是数据的分区⽐较少，可以使⽤ repartition 来提⾼并⾏度。

另外⼀种原因和数据的本地性有关，请看下⾯的例⼦：

⽤户的任务申请了 100 个 executors，每个 executor 的 cores 为 6，那么最多会有 600 个任务同 时在运⾏，刚开始是 600 个任务在运⾏，接着正在运⾏的任务越来越少，只剩下 78 个任务在运 ⾏，像下图所示：

![image 2](<Spark常见问题总结3.note_images/imageFile2.png>)

这个问题会导致 Spark 基于 yarn 的动态分配功能也⽆法使⽤了，Executor ⻓时间空闲之后会被杀 死，然后报⼀⼤堆让⼈⼼烦的 Error 信息。

先回顾⼀下 Spark 作业提交的流程，如下图所示：

![image 3](<Spark常见问题总结3.note_images/imageFile3.png>)

- 1、⾸先 DAGSchedular 会把作业划分成多个 Stage，划分的依据：是否需要进⾏ shuffle 操作。

- 2、每个 Stage 由很多的 Tasks 组成，Tasks 的数量由这个 Stage 的 partition 数决定。Stage 之间 可能有依赖关系，先提交没有前置依赖的 Stage。把 Stage ⾥的任务包装成⼀个 TaskSet，交给 TaskScheduler 提交。

- 3、把 Task 发送给 Executor，让 Executor 执⾏ Task。


这个问题是出在第⼆步，TaskScheduler 是怎么提交任务的。这块的逻辑主要是在 CoarseGrainedSchedulerBackend 和 TaskSchedulerImpl。

下⾯是 CoarseGrainedSchedulerBackend ⾥⾯的 makeOffer ⽅法的主要逻辑：

CoarseGrainedSchedulerBackend 筛选出来活跃的 Executors，交给 TaskSchedulerImpl。 TaskSchedulerImpl 返回⼀批 Task 描述给 CoarseGrainedSchedulerBackend。 序列化之后的任务的⼤⼩没有超过 spark.akka.frameSize 就向 Executor 发送该任务。

问题是出在第⼆步，根据活跃的 Executors，返回可以执⾏的 Tasks。具体查看 TaskSchedulerImpl 的 resourceOffers ⽅法。

- 1、在内存当中记录传⼊的 Executor 的映射关系，记录是否有新的机器加⼊。

- 2、如果有新的机器加⼊，要对所有的 TaskSetManager 重新计算本地性。

- 3、遍历所有的 TaskSetManager，根据 TaskSetManager 计算得出的任务的本地性来分配任务。


分配任务的优先级：

- 1）同⼀个 Executor

- 2）同⼀个节点

- 3）没有优先节点

- 4）同⼀个机架

- 5）任务节点


如果上⼀个优先级的任务的最后发布时间不满⾜下⾯这个条件，任务将不会被分发出去，导致出 现上⾯的现象。

判断条件是：curTime - lastLaunchTime >= localityWaits(currentLocalityIndex)

这样设计的初衷是好的，希望先让本地性更好的任务先运⾏，但是这⾥没有考虑到 Executor 的空 闲时间以及每个 Task 的空闲时间。跳过了这个限制之后，它还是会按照优先级来分配任务的，所 以不⽤担⼼本地性的问题。

下⾯这⼏个参数在官⽅的配置介绍当中有，但是没介绍清楚，默认都是 3 秒，减⼩这⼏个参数就 可以绕过限制了。

spark.locality.wait.process 1ms # 超过这个时间，可以执⾏ NODE_LOCAL 的任务

spark.locality.wait.node 3ms # 超过这个时间，可以执⾏ RACK_LOCAL 的任务

spark.locality.wait.rack 1s # 超过这个时间，可以执⾏ ANY 的任务

实践测试，问题解决了，并且速度快了 20%以上。

Task 在同⼀棵树上连续吊死

Spark 的任务在失败之后还在同⼀台机器上不断的重试，直⾄超过了设置的重试次数之后。在⽣产 环境当中，因为各种各样的原因，⽐如⽹络原因、磁盘满了等原因会使任务挂掉，在这个时候， 在同⼀台机器上重试⼏乎没有成功的机会，把任务发到别的机器上运⾏是最明智的选择。

Spark 是有任务的⿊名单机制的，但是这个配置在官⽅⽂档⾥⾯并没有写，可以设置下⾯的参数， ⽐如设置成⼀分钟之内不要再把任务发到这个 Executor 上了，单位是毫秒。

spark.scheduler.executorTaskBlacklistTime 60000

安全

如何⽀持多⽤户

Spark 运⾏的时候使⽤的是同⼀个账户，但是在某些场景下需要⽀持多⽤户，⽐如 Spark SQL 的 ThriftServer 服务要⾯向所有⽤户开放，如果都使⽤⼀个账户执⾏任务，数据的安全就⽆法保障 了。尝试过创建多个 SparkContext，但是 Spark ⽬前的实现是不⽀持在同⼀个 JVM ⾥创建多个 SparkContext。

下⾯介绍⼀种基于 Task 的实现⽅式，它需要注意以下两点：

能够模拟某个⽤户操作，并且这个操作是线程安全的，避免影响到别的任务。 执⾏某个任务时，driver 端和 executor 端都要同时使⽤该模拟⽤户执⾏。

Hadoop 本身提供了⼀个线程安全的模拟其他⽤户的⽅法（UserGroupInformation 的 doAs ⽅ 法），具体的实现有三点：

在 driver 端使⽤ UserGroupInformation 的 doAs ⽅法模拟⽤户操作。 给 Task 类添加 user 属性，在 DAGSchedular 创建 Task 的时候把当前模拟⽤户传给 Task 的 user 属性。 在 Task 的 run ⽅法⾥使⽤ UserGroupInformation 的 doAs ⽅法模拟该⽤户的操作。

展望 Spark 1.5 的 Tungsten-sort

升级到 1.4.1 之后，在 Shuffle 的时候⼤任务总是时不时挂掉⼀批任务，运⾏时间⼤⼤延⻓了。除 了增加 partition、在某些场景下⽤ reduceByKey 代替 groupByKey 等⼀些常⻅的⽅法之后，貌似 也没什么好的⽅法，⽽且 partition 数增⼤到⼀定数量之后弊端很⼤。

Databricks 之前发布了⼀个“钨丝计划”，号称要榨⼲ JVM 的性能。现在 Spark 1.5 已经发布了， 它会引⼊⼀种新的 Shuffle ⽅式，不过暂时只在使⽤ Spark-SQL 的时候才默认开启。现在⼀起来 看看新的 Shuffle ⽅式 tungsten-sort 它是怎么实现的。

要查看 Shuffle 的过程可以直接找到 ShuffleMapTask 这个类，它是 Shuffle 的起点。

下图是整个 tungsten-sort 的写⼊每条记录的过程：

![image 4](<Spark常见问题总结3.note_images/imageFile4.png>)

- 1、Record 的 key 和 value 会以⼆进制的格式存储写⼊到 ByteArrayOutputStream 当中，⽤⼆进 制的形式存储的好处是可以减少序列化和反序列化的时间。然后判断当前 Page 是否有⾜够的内 存，如果有⾜够的空间就写⼊到当前 Page（注：Page 是⼀块连续的内存）。写⼊ Page 之后，会 把内存地址 address 和 partitionId 编码成⼀个 8 字节的⻓整形记录在 InMemorySorter 当中。


- 2、当前 Page 内存不够的时候会去申请新的 Page，如果内存不够就要把当前数据 Spill 到磁盘 了。Shuffle 可以利⽤的内存默认是 Executor 内存的 0.2*0.8=0.16，它是由下⾯两个参数来决定 的，如果数据量⽐较⼤，建议增⼤下⾯两个值，减少 Spill 的次数。

spark.shuffle.memoryFraction 0.2

spark.shuffle.safetyFraction 0.8

- 3、Spill 的过程，从 InMemorySorter 反编码出来内存地址，按照 partitionId 的顺序把数据从内存 写⼊到⼀个⽂件当中，不会对同⼀个 partition 的数据做排序。

- 4、Spill 完了内存⾜够就申请新的 Page，内存不够就要报错了！因为内存不够就直接抛异常的做 法是⽆法在⽣产环境运⾏。Bug 产⽣的原因和它为每个任务平均分配内存的机制有关系，在数据 倾斜的场景很容易复现该问题，并且这个异常不应该抛，内存不⾜就继续 Spill。请关注下⾯这个 Bug。

https://issues.apache.org/jira/browse/SPARK-10474

实践的时候发现有两个⽅法可以降低它产⽣的⼏率，增加 partition 数量和减⼩ Page 的⼤⼩。 Page 的⼤⼩通过参数 spark.buffer.pageSize 来设置，单位是 bytes，最⼩是 1MB，最⼤是 64MB。默认的计算公式是：nextPowerOf2(maxMemory / cores / 16) （注：maxMemory 指的是 上⾯提到的 Shuffle 可⽤内存，nextPowerOf2 是 2 的次⽅）。

- 5、所有数据写⼊完毕之后，会把 Spill 产⽣的所有⽂件合并成⼀个数据⽂件，并⽣成⼀个索引⽂ 件，如果 map 数是 M，那⽣成的⽂件个数就是 2M。Shuffle Writer 的⼯作到这⾥就结束了， Shuffle Reader 沿⽤了 Sort-based 的 Reader 来读取 Shuffle 产⽣的数据。合并的过程有个优化 点，它会使⽤ NIO 的 FileChannel 去合并⽂件，不过使⽤条件⽐较苛刻，必须设置以下参数并且 Kernel 内核不能是 2.6.32 版本。


spark.shuffle.compress true

spark.io.compression.codec org.apache.spark.io.LZFCompressionCodec

spark.file.transferTo true

