内存溢出问题

在Spark中使⽤hql⽅法执⾏hive语句时，由于其在查询过程中调⽤的是Hive的获取元数据信息、SQL 解析，并且使⽤Cglib等进⾏序列化反序列化，中间可能产⽣较多的clas⽂件，导致JVM中的持久代使 ⽤较多，如果配置不当，可能引起类似于如下的 OM问题：

1.

Exception in thread "Thread-2" java.lang.OutOfMemoryEror: PermGen space复制代码

原因是实际使⽤时，如果⽤的是JDK1.6版本，Server模式的持久代默认⼤⼩是64M，Client模式的持久 代默认⼤⼩是32M，⽽Driver端进⾏SQL处理时，其持久代的使⽤可能会达到90M，导致 OM溢出， 任务失败。 解决⽅法就是在Spark的conf⽬录中的spark-defaults.conf⾥，增加对Driver的JVM配置，因为Driver 才负责SQL的解析和元数据获取。配置如下：

1.

spark.driver.extraJavaOptions -X PermSize=128M -X MaxPermSize=256M 复制代码

但是，上述情况是在yarn-cluster模式下出现，yarn-client模式运⾏时倒是正常的，原来在 $SPARK_HOME/bin/spark-clas⽂件中已经设置了持久代⼤⼩：

1.

JAVA_OPTS="-X MaxPermSize=256m $OUR_JAVA_OPTS"复制代码

当以yarn-client模式运⾏时，driver就运⾏在客户端的spark-submit进程中，其JVM参数是取的sparkclas⽂件中的设置，所谓未出现持久代溢出现象。

总结⼀下Spark中各个⻆⾊的JVM参数设置： (1)Driver的JVM参数：

-Xmx，-Xms，如果是yarn-client模式，则默认读取 -env⽂件中的SPARK_DRIVER_MEMORY 值，-Xmx，-Xms值⼀样⼤⼩；如果是yarn-cluster模式，则读取的是spark-default.conf⽂件中的 spark.driver.extraJavaOptions对应的JVM参数值。

spark

PermSize，如果是yarn-client模式，则是默认读取 -clas⽂件中的JAVA_OPTS="-

spark

X MaxPermSize=256m $OUR_JAVA_OPTS"值；如果是yarn-cluster模式，读取的是sparkdefault.conf⽂件中的spark.driver.extraJavaOptions对应的JVM参数值。 GC⽅式，如果是yarn-client模式，默认读取的是spark-clas⽂件中的JAVA_OPTS；如果是yarncluster模式，则读取的是spark-default.conf⽂件中的spark.driver.extraJavaOptions对应的参数值。 以上值最后均可被spark-submit⼯具中的 -driver-java-options参数覆盖。

- (2)Executor的JVM参数：


-Xmx，-Xms，如果是yarn-client模式，则默认读取spark-env⽂件中的 SPARK_EXECUTOR_MEMORY值，-Xmx，-Xms值⼀样⼤⼩；如果是yarn-cluster模式，则读取的是 spark-default.conf⽂件中的spark.executor.extraJavaOptions对应的JVM参数值。 PermSize，两种模式都是读取的是 -default.conf⽂件中的spark.executor.extraJavaOptions对应 的JVM参数值。 GC⽅式，两种模式都是读取的是spark-default.conf⽂件中的spark.executor.extraJavaOptions对应的 JVM参数值。

spark

- (3)Executor数⽬及所占CPU个数 如果是yarn-client模式，Executor数⽬由spark-env中的SPARK_EXECUTOR_INSTANCES指定，每个 实例的数⽬由SPARK_EXECUTOR_CORES指定；如果是yarn-cluster模式，Executor的数⽬由sparksubmit⼯具的 -num-executors参数指定，默认是2个实例，⽽每个Executor使⽤的CPU数⽬由 executor-cores指定，默认为1核。 每个Executor运⾏时的信息可以通过yarn logs命令查看到，类似于如下：


1.

14/08/13 18 12 59 INFO org.apache.spark.Loging$clas.logInfo(Loging.scala:58): Seting up executor with comands: List($JAVA_HOME/bin/java, -server, -X OnOutOfMemoryEror='kil %p', -Xms1024m -Xmx1024m , -X PermSize=256M -X MaxPermSize=256M -verbose:gc -

X:+PrintGCDetails -X:+PrintGCTimeStamps -X:+PrintHeapAtGC Xlogc:/tmp/ _gc.log, -Djava.io.tmpdir=$PWD/tmp, -Dlog4j.configuration=log4j-sparkcontainer.properties, org.apache.spark.executor.CoarseGrainedExecutorBackend, aka.tcp:/spark@sparktest1 41606/user/CoarseGrainedScheduler, 1, sparktest2, 3, 1>, <LOG_DIR>/stdout, 2>, <LOG_DIR>/stder)复制代码

spark

其中，aka.tcp:/spark@sparktest1 41606/user/CoarseGrainedScheduler表示当前的Executor进程 所在节点，后⾯的1表示Executor编号，sparktest2表示AplicationMaster的host，接着的3表示当前 Executor所占⽤的CPU数⽬。

-

序列化异常 在Spark上执⾏hive语句的时候，出现类似于如下的异常：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


org.apache.spark.SparkDriverExecutionException: Execution eror at org.apache.spark.scheduler.DAGScheduler.handleTaskCompletion(DAGScheduler.scala:849)

at org.apache. .scheduler.DAGSchedulerEventProcesActor$anonfun$receive$2.aplyOrEls e(DAGScheduler.scala:1231)

spark

at aka.actor.ActorCel.receiveMesage(ActorCel.scala:498) at aka.actor.ActorCel.invoke(ActorCel.scala:456) at aka.dispatch.Mailbox.procesMailbox(Mailbox.scala:237) at aka.dispatch.Mailbox.run(Mailbox.scala:219) at

aka.dispatch.ForkJoinExecutorConfigurator$AkaForkJoinTask.exec(AbstractDispatcher.scal a:386)

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


at scala.concurent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260) at scala.concurent.forkjoin.ForkJoinPol$WorkQueue.runTask(ForkJoinPol.java:139) at scala.concurent.forkjoin.ForkJoinPol.runWorker(ForkJoinPol.java:1979) at scala.concurent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)

Caused by: java.lang.ClasCastException: scala.colection.mutable.HashSet canot be cast to scala.colection.mutable.BitSet

at

org.apache.spark.sql.execution.BroadcastNestedLopJoin$anonfun$7.aply(joins.scala: 36) at org.apache.spark.rd.RD$anonfun$19.aply(RD.scala:813) at org.apache.spark.rd.RD$anonfun$19.aply(RD.scala:810) at org.apache.spark.scheduler.JobWaiter.taskSuceded(JobWaiter.scala:56) at

org.apache.spark.scheduler.DAGScheduler.handleTaskCompletion(DAGScheduler.scala:845)复 制代码

排查其前后的⽇志，发现⼤都是序列化的东⻄：

- 1.
- 2.


14/08/131 10 01 INFO org.apache.spark.Loging$clas.logInfo(Loging.scala:58): Serialized task 8.0 3 as 20849 bytes in 0 ms 14/08/131 10 01 INFO org.apache.spark.Loging$clas.logInfo(Loging.scala:58): Finished TID 813 in 25 ms on sparktest0 (progres: 3/20)复制代码 spark spark.serializer org.apache.spark.serializer.KryoSerializer复制代码

⽽在 -default.conf中，事先设置了序列化⽅式为Kryo：

1.

根据异常信息，可⻅是HashSet转为BitSet类型转换失败，Kryo把松散的HashSet转换为了紧凑的 BitSet，把序列化⽅式注释掉之后，任务可以正常执⾏。难道Spark的Kryo序列化做的还不到位？此问 题需要进⼀步跟踪。

9 Executor僵死问题

运⾏⼀个Spark任务，发现其运⾏速度远远慢于执⾏同样SQL语句的Hive的执⾏，甚⾄出现了 OM 的错误，最后卡住达⼏⼩时！并且Executor进程在疯狂GC。

截取其⼀Task的 OM异常信息：

![image 1](<Spark on yarn中的内存溢出案例.note_images/imageFile1.png>)

可以看到这是在序列化过程中发⽣的 OM。根据节点信息，找到对应的Executor进程，观察其Jstack 信息：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


Thread 36169: (state = BLOCKED)

- - java.lang.Long.valueOf(long) @bci=27, line=57 (Compiled frame)
- com.esotericsoftware.kryo.serializers.DefaultSerializers$LongSerializer.read(com.esotericsoft ware.kryo.Kryo, com.esotericsoftware.kryo.io.Input, java.lang.Clas) @bci=5, =13 (Compiled frame)


line

- com.esotericsoftware.kryo.serializers.DefaultSerializers$LongSerializer.read(com.esotericsoft ware.kryo.Kryo, com.esotericsoftware.kryo.io.Input, java.lang.Clas) @bci=4, line=103 (Compiled frame)

- - com.esotericsoftware.kryo.Kryo.readClasAndObject(com.esotericsoftware.kryo.io.Input) @bci=158, line=732 (Compiled frame)

- com.esotericsoftware.kryo.serializers.DefaultAraySerializers$ObjectAraySerializer.read(com. esotericsoftware.kryo.Kryo, com.esotericsoftware.kryo.io.Input, java.lang.Clas) @bci=158,line =38 (Compiled frame)
- com.esotericsoftware.kryo.serializers.DefaultAraySerializers$ObjectAraySerializer.read(com. esotericsoftware.kryo.Kryo, com.esotericsoftware.kryo.io.Input, java.lang.Clas) @bci=4, line=293 (Compiled frame)


- - com.esotericsoftware.kryo.Kryo.readObject(com.esotericsoftware.kryo.io.Input, java.lang.Clas, com.esotericsoftware.kryo.Serializer) @bci=136,line =651 (Compiled frame)


- com.esotericsoftware.kryo.serializers.FieldSerializer$ObjectField.read(com.esotericsoftware.kr yo.io.Input, java.lang.Object) @bci=143, line=605 (Compiled frame)


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
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.


- - com.esotericsoftware.kryo.serializers.FieldSerializer.read(com.esotericsoftware.kryo.Kryo, com.esotericsoftware.kryo.io.Input, java.lang.Clas) @bci=4, line=21 (Compiled frame)
- - com.esotericsoftware.kryo.Kryo.readObject(com.esotericsoftware.kryo.io.Input, java.lang.Clas, com.esotericsoftware.kryo.Serializer) @bci=136,line =651 (Compiled frame)

com.esotericsoftware.kryo.serializers.FieldSerializer$ObjectField.read(com.esotericsoftware.kr yo.io.Input, java.lang.Object) @bci=143, line=605 (Compiled frame)

- - com.esotericsoftware.kryo.serializers.FieldSerializer.read(com.esotericsoftware.kryo.Kryo, com.esotericsoftware.kryo.io.Input, java.lang.Clas) @bci=4, line=21 (Compiled frame)
- - com.esotericsoftware.kryo.Kryo.readClasAndObject(com.esotericsoftware.kryo.io.Input) @bci=158,line =732 (Compiled frame)
- - org.apache.spark.serializer.KryoDeserializationStream.readObject(scala.reflect.ClasTag) @bci=8, line=18 (Compiled frame)
- - org.apache.spark.serializer.DeserializationStream$anon$1.getNext() @bci=10, line=125 (Compiled frame)
- - org.apache.spark.util.NextIterator.hasNext() @bci=16, line=71 (Compiled frame)
- - org.apache. .storage.BlockManager$LazyProxyIterator$1.hasNext() @bci=4, =1031 (Compiled frame)

spark line

- - scala.colection.Iterator$anon$13.hasNext() @bci=4, line=371 (Compiled frame)
- - org.apache.spark.util.CompletionIterator.hasNext() @bci=4, line=30 (Compiled frame)
- - org.apache.spark.InteruptibleIterator.hasNext() @bci=2, line=39 (Compiled frame)
- - scala.colection.Iterator$anon$1.hasNext() @bci=4, line=327 (Compiled frame)
- - org.apache. .sql.execution.HashJoin$anonfun$execute$1.aply(scala.colection.Iterator, scala.colection.Iterator) @bci=14, =7 (Compiled frame)

spark

line

- - org.apache.spark. .execution.HashJoin$anonfun$execute$1.aply(java.lang.Object, java.lang.Object) @bci=9, line=71 (Interpreted frame)

sql

- - org.apache.spark.rd.Zi pedPartitionsRD2.compute(org.apache.spark.Partition, org.apache.spark.TaskContext) @bci=48, line=87 (Interpreted frame)
- - org.apache. .rd.RD.computeOrReadCheckpoint(org.apache.spark.Partition, org.apache.spark.TaskContext) @bci=26, =262 (Interpreted frame)复制代码


spark

line

有⼤量的BLOCKED线程，继续观察GC信息，发现⼤量的FUL GC。

分析，在插⼊Hive表的时候，实际上需要写HDFS，在此过程的HashJoin时，伴随着⼤量的Shufle 写操作，JVM的新⽣代不断GC，Eden Space写满了就往Survivor Space写，同时超过⼀定⼤⼩的数据 会直接写到⽼⽣代，当新⽣代写满了之后，也会把⽼的数据搞到⽼⽣代，如果⽼⽣代空间不⾜了，就 触发FUL GC，还是空间不够，那就 OM错误了，此时线程被Blocked，导致整个Executor处理数据 的进程被卡住。

# 当处理⼤数据的时候，如果JVM配置不当就容易引起上述问题。解决的⽅法就是增⼤Executor的使⽤ 内存，合理配置新⽣代和⽼⽣代的⼤⼩，可以将⽼⽣代的空间适当的调⼤点

