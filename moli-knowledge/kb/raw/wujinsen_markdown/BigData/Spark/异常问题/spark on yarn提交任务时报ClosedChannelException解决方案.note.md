spark on yarn提交任务时报ClosedChanelException解决⽅案

spark2.1出来了，想玩玩就搭了个原⽣的apache集群，但在standalone模式下没有任何问题，基于 apache hadop 2.7.3使⽤spark on yarn⼀直报这个错。(Java 8) 报错⽇志如下：

![image 1](<spark on yarn提交任务时报ClosedChannelException解决方案.note_images/imageFile1.png>)

复制代码

Warning: Master yarn-client is deprecated since 2.0. Please use master "yarn" with specified deploy mode instead.

- 17/01/31 00:12:11 INFO spark.SparkContext: Running Spark version 2.1.0

- 17/01/31 00:12:11 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable

- 17/01/31 00:12:11 INFO spark.SecurityManager: Changing view acls to: root

- 17/01/31 00:12:11 INFO spark.SecurityManager: Changing modify acls to: root

- 17/01/31 00:12:11 INFO spark.SecurityManager: Changing view acls groups to:

- 17/01/31 00:12:11 INFO spark.SecurityManager: Changing modify acls groups to:

- 17/01/31 00:12:11 INFO spark.SecurityManager: SecurityManager: authentication disabled; ui acls disabled; users with view permissions: Set(root); groups with view permissions: Set(); users with modify permissions: Set(root); groups with modify permissions: Set()

- 17/01/31 00:12:11 INFO util.Utils: Successfully started service 'sparkDriver' on port 59775.

- 17/01/31 00:12:11 INFO spark.SparkEnv: Registering MapOutputTracker

- 17/01/31 00:12:11 INFO spark.SparkEnv: Registering BlockManagerMaster

- 17/01/31 00:12:11 INFO storage.BlockManagerMasterEndpoint: Using org.apache.spark.storage.DefaultTopologyMapper for getting topology information

- 17/01/31 00:12:11 INFO storage.BlockManagerMasterEndpoint: BlockManagerMasterEndpoint up

- 17/01/31 00:12:11 INFO storage.DiskBlockManager: Created local directory at /opt/program/spark2.1.0-bin-hadoop2.7/blockmgr-b04fc6c2-501f-4df4-ae13-f6fb0aaa6470

- 17/01/31 00:12:11 INFO memory.MemoryStore: MemoryStore started with capacity 366.3 MB

- 17/01/31 00:12:11 INFO spark.SparkEnv: Registering OutputCommitCoordinator

- 17/01/31 00:12:12 INFO util.log: Logging initialized @2406ms


- 17/01/31 00:12:12 INFO server.Server: jetty-9.2.z-SNAPSHOT


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started o.s.j.s.ServletContextHandler@64712be{/jobs,null,AVAILABLE}


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started o.s.j.s.ServletContextHandler@53499d85{/jobs/json,null,AVAILABLE}


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started o.s.j.s.ServletContextHandler@30ed9c6c{/jobs/job,null,AVAILABLE}


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started o.s.j.s.ServletContextHandler@782a4fff{/jobs/job/json,null,AVAILABLE}


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started o.s.j.s.ServletContextHandler@46c670a6{/stages,null,AVAILABLE}


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started o.s.j.s.ServletContextHandler@59fc684e{/stages/json,null,AVAILABLE}


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started o.s.j.s.ServletContextHandler@5ae81e1{/stages/stage,null,AVAILABLE}


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started o.s.j.s.ServletContextHandler@2fd1731c{/stages/stage/json,null,AVAILABLE}


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started o.s.j.s.ServletContextHandler@5ae76500{/stages/pool,null,AVAILABLE}


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started o.s.j.s.ServletContextHandler@6063d80a{/stages/pool/json,null,AVAILABLE}


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started o.s.j.s.ServletContextHandler@1133ec6e{/storage,null,AVAILABLE}


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started o.s.j.s.ServletContextHandler@355e34c7{/storage/json,null,AVAILABLE}


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started


- o.s.j.s.ServletContextHandler@54709809{/storage/rdd,null,AVAILABLE} 17/01/31 00:12:12 INFO handler.ContextHandler: Started

- o.s.j.s.ServletContextHandler@2a2da905{/storage/rdd/json,null,AVAILABLE} 17/01/31 00:12:12 INFO handler.ContextHandler: Started

- o.s.j.s.ServletContextHandler@24f360b2{/environment,null,AVAILABLE} 17/01/31 00:12:12 INFO handler.ContextHandler: Started

- o.s.j.s.ServletContextHandler@60cf80e7{/environment/json,null,AVAILABLE} 17/01/31 00:12:12 INFO handler.ContextHandler: Started

- o.s.j.s.ServletContextHandler@302fec27{/executors,null,AVAILABLE}


- 17/01/31 00:12:12 INFO handler.ContextHandler: Started
- o.s.j.s.ServletContextHandler@770d0ea6{/executors/json,null,AVAILABLE} 17/01/31 00:12:12 INFO handler.ContextHandler: Started

- o.s.j.s.ServletContextHandler@48c40605{/executors/threadDump,null,AVAILABLE} 17/01/31 00:12:12 INFO handler.ContextHandler: Started

- o.s.j.s.ServletContextHandler@54107f42{/executors/threadDump/json,null,AVAILABLE} 17/01/31 00:12:12 INFO handler.ContextHandler: Started

- o.s.j.s.ServletContextHandler@1b11ef33{/static,null,AVAILABLE} 17/01/31 00:12:12 INFO handler.ContextHandler: Started

- o.s.j.s.ServletContextHandler@476aac9{/,null,AVAILABLE} 17/01/31 00:12:12 INFO handler.ContextHandler: Started

- o.s.j.s.ServletContextHandler@6cea706c{/api,null,AVAILABLE} 17/01/31 00:12:12 INFO handler.ContextHandler: Started

- o.s.j.s.ServletContextHandler@3bd7f8dc{/jobs/job/kill,null,AVAILABLE}

- 17/01/31 00:12:12 INFO handler.ContextHandler: Started

o.s.j.s.ServletContextHandler@2f2bf0e2{/stages/stage/kill,null,AVAILABLE}

- 17/01/31 00:12:12 INFO server.ServerConnector: Started ServerConnector@780ec4a5{HTTP/1.1} {0.0.0.0:4040}

- 17/01/31 00:12:12 INFO server.Server: Started @2508ms

- 17/01/31 00:12:12 INFO util.Utils: Successfully started service 'SparkUI' on port 4040.

- 17/01/31 00:12:12 INFO ui.SparkUI: Bound SparkUI to 0.0.0.0, and started at http://192.168.56.101:4040

- 17/01/31 00:12:12 INFO spark.SparkContext: Added JAR file:/opt/spark/examples/jars/sparkexamples_2.11-2.1.0.jar at spark://192.168.56.101:59775/jars/spark-examples_2.11-2.1.0.jar with timestamp 1485792732176

- 17/01/31 00:12:12 INFO client.RMProxy: Connecting to ResourceManager at node01/192.168.56.101:8032

- 17/01/31 00:12:13 INFO yarn.Client: Requesting a new application from cluster with 3 NodeManagers


- 17/01/31 00:12:13 INFO yarn.Client: Verifying our application has not requested more than the maximum memory capability of the cluster (8192 MB per container)


- 17/01/31 00:12:13 INFO yarn.Client: Will allocate AM container, with 896 MB memory including 384 MB overhead


- 17/01/31 00:12:13 INFO yarn.Client: Setting up container launch context for our AM


- 17/01/31 00:12:13 INFO yarn.Client: Setting up the launch environment for our AM container


- 17/01/31 00:12:13 INFO yarn.Client: Preparing resources for our AM container


- 17/01/31 00:12:13 WARN yarn.Client: Neither spark.yarn.jars nor spark.yarn.archive is set, falling back to uploading libraries under SPARK_HOME. 17/01/31 00:12:15 INFO yarn.Client: Uploading resource file:/opt/program/spark-2.1.0-binhadoop2.7/spark-6cafe3cb-9ed2-4f7f-b44f-a2bb447eaa30/__spark_libs__493282781411356296.zip -> hdfs://node01:9000/user/root/.sparkStaging/application_1485792095366_0003/__spark_libs__49328278141 1356296.zip 17/01/31 00:12:17 INFO yarn.Client: Uploading resource file:/opt/program/spark-2.1.0-binhadoop2.7/spark-6cafe3cb-9ed2-4f7f-b44f-a2bb447eaa30/__spark_conf__2188039824841197723.zip -> hdfs://node01:9000/user/root/.sparkStaging/application_1485792095366_0003/__spark_conf__.zip 17/01/31 00:12:17 INFO spark.SecurityManager: Changing view acls to: root 17/01/31 00:12:17 INFO spark.SecurityManager: Changing modify acls to: root 17/01/31 00:12:17 INFO spark.SecurityManager: Changing view acls groups to: 17/01/31 00:12:17 INFO spark.SecurityManager: Changing modify acls groups to:




- 17/01/31 00:12:17 INFO spark.SecurityManager: SecurityManager: authentication disabled; ui acls disabled; users with view permissions: Set(root); groups with view permissions: Set(); users with modify permissions: Set(root); groups with modify permissions: Set()

- 17/01/31 00:12:17 INFO yarn.Client: Submitting application application_1485792095366_0003 to ResourceManager

- 17/01/31 00:12:18 INFO impl.YarnClientImpl: Submitted application application_1485792095366_0003

17/01/31 00:12:18 INFO cluster.SchedulerExtensionServices: Starting Yarn extension services with app application_1485792095366_0003 and attemptId None 17/01/31 00:12:19 INFO yarn.Client: Application report for application_1485792095366_0003 (state: ACCEPTED)

- 17/01/31 00:12:19 INFO yarn.Client:




client token: N/A diagnostics: N/A ApplicationMaster host: N/A ApplicationMaster RPC port: -1 queue: default start time: 1485792737991 final status: UNDEFINED tracking URL: http://node01:8088/proxy/application_1485792095366_0003/ user: root

- 17/01/31 00:12:20 INFO yarn.Client: Application report for application_1485792095366_0003 (state: ACCEPTED)

- 17/01/31 00:12:21 INFO yarn.Client: Application report for application_1485792095366_0003 (state: ACCEPTED) 17/01/31 00:12:21 INFO cluster.YarnSchedulerBackend$YarnSchedulerEndpoint: ApplicationMaster registered as NettyRpcEndpointRef(null) 17/01/31 00:12:21 INFO cluster.YarnClientSchedulerBackend: Add WebUI Filter. org.apache.hadoop.yarn.server.webproxy.amfilter.AmIpFilter, Map(PROXY_HOSTS -> node01, PROXY_URI_BASES -> http://node01:8088/proxy/application_1485792095366_0003), /proxy/application_1485792095366_0003


- 17/01/31 00:12:21 INFO ui.JettyUtils: Adding filter: org.apache.hadoop.yarn.server.webproxy.amfilter.AmIpFilter

- 17/01/31 00:12:22 INFO yarn.Client: Application report for application_1485792095366_0003 (state: ACCEPTED)

- 17/01/31 00:12:23 INFO yarn.Client: Application report for application_1485792095366_0003 (state: RUNNING) 17/01/31 00:12:23 INFO yarn.Client:


client token: N/A diagnostics: N/A ApplicationMaster host: 192.168.56.101 ApplicationMaster RPC port: 0 queue: default start time: 1485792737991 final status: UNDEFINED tracking URL: http://node01:8088/proxy/application_1485792095366_0003/ user: root

17/01/31 00:12:23 INFO cluster.YarnClientSchedulerBackend: Application application_1485792095366_0003 has started running. 17/01/31 00:12:23 INFO util.Utils: Successfully started service 'org.apache.spark.network.netty.NettyBlockTransferService' on port 38405. 17/01/31 00:12:23 INFO netty.NettyBlockTransferService: Server created on 192.168.56.101:38405 17/01/31 00:12:23 INFO storage.BlockManager: Using org.apache.spark.storage.RandomBlockReplicationPolicy for block replication policy 17/01/31 00:12:23 INFO storage.BlockManagerMaster: Registering BlockManager BlockManagerId(driver, 192.168.56.101, 38405, None) 17/01/31 00:12:23 INFO storage.BlockManagerMasterEndpoint: Registering block manager 192.168.56.101:38405 with 366.3 MB RAM, BlockManagerId(driver, 192.168.56.101, 38405, None) 17/01/31 00:12:23 INFO storage.BlockManagerMaster: Registered BlockManager BlockManagerId(driver, 192.168.56.101, 38405, None)

- 17/01/31 00:12:23 INFO storage.BlockManager: Initialized BlockManager: BlockManagerId(driver, 192.168.56.101, 38405, None)

- 17/01/31 00:12:23 INFO handler.ContextHandler: Started o.s.j.s.ServletContextHandler@643ba1ed{/metrics/json,null,AVAILABLE}

- 17/01/31 00:12:24 INFO cluster.YarnSchedulerBackend$YarnDriverEndpoint: Registered executor NettyRpcEndpointRef(null) (192.168.56.101:43252) with ID 1

17/01/31 00:12:24 INFO storage.BlockManagerMasterEndpoint: Registering block manager node01:53420 with 366.3 MB RAM, BlockManagerId(1, node01, 53420, None) 17/01/31 00:12:25 INFO cluster.YarnSchedulerBackend$YarnDriverEndpoint: Disabling executor 1.

- 17/01/31 00:12:25 INFO scheduler.DAGScheduler: Executor lost: 1 (epoch 0)




17/01/31 00:12:25 ERROR client.TransportClient: Failed to send RPC 8305478367380188725 to /192.168.56.101:43246: java.nio.channels.ClosedChannelException java.nio.channels.ClosedChannelException

at io.netty.channel.AbstractChannel$AbstractUnsafe.write(...)(Unknown Source) 17/01/31 00:12:25 INFO storage.BlockManagerMasterEndpoint: Trying to remove executor 1 from BlockManagerMaster. 17/01/31 00:12:25 INFO storage.BlockManagerMasterEndpoint: Removing block manager BlockManagerId(1, node01, 53420, None) 17/01/31 00:12:25 INFO storage.BlockManagerMaster: Removed 1 successfully in removeExecutor

- 17/01/31 00:12:25 INFO scheduler.DAGScheduler: Shuffle files lost for executor: 1 (epoch 0)

- 17/01/31 00:12:25 WARN cluster.YarnSchedulerBackend$YarnSchedulerEndpoint: Attempted to get executor loss reason for executor id 1 at RPC address 192.168.56.101:43252, but got no response. Marking as slave lost. java.io.IOException: Failed to send RPC 8305478367380188725 to /192.168.56.101:43246: java.nio.channels.ClosedChannelException

at org.apache.spark.network.client.TransportClient$3.operationComplete(TransportClient.java:249) at

org.apache.spark.network.client.TransportClient$3.operationComplete(TransportClient.java:233) at io.netty.util.concurrent.DefaultPromise.notifyListener0(DefaultPromise.java:514) at io.netty.util.concurrent.DefaultPromise.notifyListenersNow(DefaultPromise.java:488) at io.netty.util.concurrent.DefaultPromise.access$000(DefaultPromise.java:34) at io.netty.util.concurrent.DefaultPromise$1.run(DefaultPromise.java:438) at

io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:408) at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:455) at

io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:140)

at io.netty.util.concurrent.DefaultThreadFactory$DefaultRunnableDecorator.run(DefaultThreadFactory.jav a:144)

at java.lang.Thread.run(Thread.java:745) Caused by: java.nio.channels.ClosedChannelException at io.netty.channel.AbstractChannel$AbstractUnsafe.write(...)(Unknown Source)

- 17/01/31 00:12:25 ERROR cluster.YarnScheduler: Lost executor 1 on node01: Slave lost

- 17/01/31 00:12:26 INFO cluster.YarnSchedulerBackend$YarnSchedulerEndpoint: ApplicationMaster registered as NettyRpcEndpointRef(null)


- 17/01/31 00:12:26 INFO cluster.YarnClientSchedulerBackend: Add WebUI Filter. org.apache.hadoop.yarn.server.webproxy.amfilter.AmIpFilter, Map(PROXY_HOSTS -> node01, PROXY_URI_BASES -> http://node01:8088/proxy/application_1485792095366_0003), /proxy/application_1485792095366_0003


- 17/01/31 00:12:26 INFO ui.JettyUtils: Adding filter: org.apache.hadoop.yarn.server.webproxy.amfilter.AmIpFilter 17/01/31 00:12:28 ERROR cluster.YarnClientSchedulerBackend: Yarn application has already exited with state FINISHED! 17/01/31 00:12:28 INFO server.ServerConnector: Stopped ServerConnector@780ec4a5{HTTP/1.1} {0.0.0.0:4040} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped


- o.s.j.s.ServletContextHandler@2f2bf0e2{/stages/stage/kill,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@3bd7f8dc{/jobs/job/kill,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@6cea706c{/api,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@476aac9{/,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@1b11ef33{/static,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped


- o.s.j.s.ServletContextHandler@54107f42{/executors/threadDump/json,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@48c40605{/executors/threadDump,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@770d0ea6{/executors/json,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@302fec27{/executors,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@60cf80e7{/environment/json,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@24f360b2{/environment,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@2a2da905{/storage/rdd/json,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@54709809{/storage/rdd,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@355e34c7{/storage/json,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@1133ec6e{/storage,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@6063d80a{/stages/pool/json,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@5ae76500{/stages/pool,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@2fd1731c{/stages/stage/json,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@5ae81e1{/stages/stage,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@59fc684e{/stages/json,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@46c670a6{/stages,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@782a4fff{/jobs/job/json,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@30ed9c6c{/jobs/job,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@53499d85{/jobs/json,null,UNAVAILABLE} 17/01/31 00:12:28 INFO handler.ContextHandler: Stopped

- o.s.j.s.ServletContextHandler@64712be{/jobs,null,UNAVAILABLE} 17/01/31 00:12:28 INFO ui.SparkUI: Stopped Spark web UI at http://192.168.56.101:4040 17/01/31 00:12:28 ERROR client.TransportClient: Failed to send RPC 6600979308376699964 to /192.168.56.103:56283: java.nio.channels.ClosedChannelException java.nio.channels.ClosedChannelException


at io.netty.channel.AbstractChannel$AbstractUnsafe.write(...)(Unknown Source) 17/01/31 00:12:28 ERROR spark.SparkContext: Error initializing SparkContext. java.lang.IllegalStateException: Spark context stopped while waiting for backend

at

org.apache.spark.scheduler.TaskSchedulerImpl.waitBackendReady(TaskSchedulerImpl.scala:614) at org.apache.spark.scheduler.TaskSchedulerImpl.postStartHook(TaskSchedulerImpl.scala:169) at org.apache.spark.SparkContext.<init>(SparkContext.scala:567) at org.apache.spark.SparkContext$.getOrCreate(SparkContext.scala:2313) at org.apache.spark.sql.SparkSession$Builder$$anonfun$6.apply(SparkSession.scala:868) at org.apache.spark.sql.SparkSession$Builder$$anonfun$6.apply(SparkSession.scala:860) at scala.Option.getOrElse(Option.scala:121) at org.apache.spark.sql.SparkSession$Builder.getOrCreate(SparkSession.scala:860) at org.apache.spark.examples.SparkPi$.main(SparkPi.scala:31) at org.apache.spark.examples.SparkPi.main(SparkPi.scala) at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)

at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) at java.lang.reflect.Method.invoke(Method.java:498) at

org.apache.spark.deploy.SparkSubmit$.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala :738)

at org.apache.spark.deploy.SparkSubmit$.doRunMain$1(SparkSubmit.scala:187) at org.apache.spark.deploy.SparkSubmit$.submit(SparkSubmit.scala:212) at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:126) at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)

17/01/31 00:12:28 ERROR cluster.YarnSchedulerBackend$YarnSchedulerEndpoint: Sending RequestExecutors(0,0,Map()) to AM was unsuccessful java.io.IOException: Failed to send RPC 6600979308376699964 to /192.168.56.103:56283: java.nio.channels.ClosedChannelException

at org.apache.spark.network.client.TransportClient$3.operationComplete(TransportClient.java:249) at

org.apache.spark.network.client.TransportClient$3.operationComplete(TransportClient.java:233) at io.netty.util.concurrent.DefaultPromise.notifyListener0(DefaultPromise.java:514) at io.netty.util.concurrent.DefaultPromise.notifyListenersNow(DefaultPromise.java:488) at io.netty.util.concurrent.DefaultPromise.notifyListeners(DefaultPromise.java:427) at io.netty.util.concurrent.DefaultPromise.tryFailure(DefaultPromise.java:129) at io.netty.channel.AbstractChannel$AbstractUnsafe.safeSetFailure(AbstractChannel.java:852) at io.netty.channel.AbstractChannel$AbstractUnsafe.write(AbstractChannel.java:738) at

io.netty.channel.DefaultChannelPipeline$HeadContext.write(DefaultChannelPipeline.java:1251) at io.netty.channel.AbstractChannelHandlerContext.invokeWrite0(AbstractChannelHandlerContext.java:743) at io.netty.channel.AbstractChannelHandlerContext.invokeWrite(AbstractChannelHandlerContext.java:735) at io.netty.channel.AbstractChannelHandlerContext.access$1900(AbstractChannelHandlerContext.java:36)

at io.netty.channel.AbstractChannelHandlerContext$AbstractWriteTask.write(AbstractChannelHandlerContex t.java:1072)

at io.netty.channel.AbstractChannelHandlerContext$WriteAndFlushTask.write(AbstractChannelHandlerContex t.java:1126)

at io.netty.channel.AbstractChannelHandlerContext$AbstractWriteTask.run(AbstractChannelHandlerContext. java:1061)

at

io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:408) at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:455) at

io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:140)

at io.netty.util.concurrent.DefaultThreadFactory$DefaultRunnableDecorator.run(DefaultThreadFactory.jav a:144)

at java.lang.Thread.run(Thread.java:745) Caused by: java.nio.channels.ClosedChannelException

at io.netty.channel.AbstractChannel$AbstractUnsafe.write(...)(Unknown Source) 17/01/31 00:12:28 INFO cluster.SchedulerExtensionServices: Stopping SchedulerExtensionServices (serviceOption=None,

services=List(), started=false)

17/01/31 00:12:28 INFO spark.SparkContext: SparkContext already stopped. Exception in thread "main" java.lang.IllegalStateException: Spark context stopped while waiting for

backend

at

org.apache.spark.scheduler.TaskSchedulerImpl.waitBackendReady(TaskSchedulerImpl.scala:614) at org.apache.spark.scheduler.TaskSchedulerImpl.postStartHook(TaskSchedulerImpl.scala:169) at org.apache.spark.SparkContext.<init>(SparkContext.scala:567) at org.apache.spark.SparkContext$.getOrCreate(SparkContext.scala:2313) at org.apache.spark.sql.SparkSession$Builder$$anonfun$6.apply(SparkSession.scala:868) at org.apache.spark.sql.SparkSession$Builder$$anonfun$6.apply(SparkSession.scala:860) at scala.Option.getOrElse(Option.scala:121) at org.apache.spark.sql.SparkSession$Builder.getOrCreate(SparkSession.scala:860) at org.apache.spark.examples.SparkPi$.main(SparkPi.scala:31) at org.apache.spark.examples.SparkPi.main(SparkPi.scala) at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) at java.lang.reflect.Method.invoke(Method.java:498) at

org.apache.spark.deploy.SparkSubmit$.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala :738)

at org.apache.spark.deploy.SparkSubmit$.doRunMain$1(SparkSubmit.scala:187) at org.apache.spark.deploy.SparkSubmit$.submit(SparkSubmit.scala:212) at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:126) at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)

17/01/31 00:12:28 ERROR util.Utils: Uncaught exception in thread Yarn application state monitor org.apache.spark.SparkException: Exception thrown in awaitResult

at org.apache.spark.rpc.RpcTimeout$$anonfun$1.applyOrElse(RpcTimeout.scala:77) at org.apache.spark.rpc.RpcTimeout$$anonfun$1.applyOrElse(RpcTimeout.scala:75) at scala.runtime.AbstractPartialFunction.apply(AbstractPartialFunction.scala:36) at

org.apache.spark.rpc.RpcTimeout$$anonfun$addMessageIfTimeout$1.applyOrElse(RpcTimeout.scala:59) at

org.apache.spark.rpc.RpcTimeout$$anonfun$addMessageIfTimeout$1.applyOrElse(RpcTimeout.scala:59) at scala.PartialFunction$OrElse.apply(PartialFunction.scala:167) at org.apache.spark.rpc.RpcTimeout.awaitResult(RpcTimeout.scala:83) at

org.apache.spark.scheduler.cluster.CoarseGrainedSchedulerBackend.requestTotalExecutors(CoarseGraine dSchedulerBackend.scala:512)

at org.apache.spark.scheduler.cluster.YarnSchedulerBackend.stop(YarnSchedulerBackend.scala:93)

at org.apache.spark.scheduler.cluster.YarnClientSchedulerBackend.stop(YarnClientSchedulerBackend.scala :151)

at org.apache.spark.scheduler.TaskSchedulerImpl.stop(TaskSchedulerImpl.scala:467) at org.apache.spark.scheduler.DAGScheduler.stop(DAGScheduler.scala:1588) at org.apache.spark.SparkContext$$anonfun$stop$8.apply$mcV$sp(SparkContext.scala:1826) at org.apache.spark.util.Utils$.tryLogNonFatalError(Utils.scala:1283) at org.apache.spark.SparkContext.stop(SparkContext.scala:1825) at

org.apache.spark.scheduler.cluster.YarnClientSchedulerBackend$MonitorThread.run(YarnClientScheduler Backend.scala:108) Caused by: java.io.IOException: Failed to send RPC 6600979308376699964 to /192.168.56.103:56283: java.nio.channels.ClosedChannelException

at org.apache.spark.network.client.TransportClient$3.operationComplete(TransportClient.java:249) at

org.apache.spark.network.client.TransportClient$3.operationComplete(TransportClient.java:233) at io.netty.util.concurrent.DefaultPromise.notifyListener0(DefaultPromise.java:514) at io.netty.util.concurrent.DefaultPromise.notifyListenersNow(DefaultPromise.java:488)

at io.netty.util.concurrent.DefaultPromise.notifyListeners(DefaultPromise.java:427) at io.netty.util.concurrent.DefaultPromise.tryFailure(DefaultPromise.java:129) at io.netty.channel.AbstractChannel$AbstractUnsafe.safeSetFailure(AbstractChannel.java:852) at io.netty.channel.AbstractChannel$AbstractUnsafe.write(AbstractChannel.java:738) at

io.netty.channel.DefaultChannelPipeline$HeadContext.write(DefaultChannelPipeline.java:1251) at io.netty.channel.AbstractChannelHandlerContext.invokeWrite0(AbstractChannelHandlerContext.java:743) at io.netty.channel.AbstractChannelHandlerContext.invokeWrite(AbstractChannelHandlerContext.java:735) at io.netty.channel.AbstractChannelHandlerContext.access$1900(AbstractChannelHandlerContext.java:36)

at io.netty.channel.AbstractChannelHandlerContext$AbstractWriteTask.write(AbstractChannelHandlerContex t.java:1072)

at io.netty.channel.AbstractChannelHandlerContext$WriteAndFlushTask.write(AbstractChannelHandlerContex t.java:1126)

at io.netty.channel.AbstractChannelHandlerContext$AbstractWriteTask.run(AbstractChannelHandlerContext. java:1061)

at

io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:408) at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:455) at

io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:140)

at io.netty.util.concurrent.DefaultThreadFactory$DefaultRunnableDecorator.run(DefaultThreadFactory.jav a:144)

at java.lang.Thread.run(Thread.java:745) Caused by: java.nio.channels.ClosedChannelException

at io.netty.channel.AbstractChannel$AbstractUnsafe.write(...)(Unknown Source) 17/01/31 00:12:28 INFO spark.MapOutputTrackerMasterEndpoint: MapOutputTrackerMasterEndpoint stopped! 17/01/31 00:12:28 INFO storage.DiskBlockManager: Shutdown hook called 17/01/31 00:12:28 INFO util.ShutdownHookManager: Shutdown hook called 17/01/31 00:12:28 INFO memory.MemoryStore: MemoryStore cleared 17/01/31 00:12:28 INFO storage.BlockManager: BlockManager stopped 17/01/31 00:12:28 INFO util.ShutdownHookManager: Deleting directory /opt/program/spark-2.1.0-binhadoop2.7/spark-6cafe3cb-9ed2-4f7f-b44f-a2bb447eaa30/userFiles-4be7a61f-e6ef-4914-b896-eedb46d78dbc 17/01/31 00:12:28 INFO storage.BlockManagerMaster: BlockManagerMaster stopped 17/01/31 00:12:28 INFO util.ShutdownHookManager: Deleting directory /opt/program/spark-2.1.0-binhadoop2.7/spark-6cafe3cb-9ed2-4f7f-b44f-a2bb447eaa30 17/01/31 00:12:28 INFO scheduler.OutputCommitCoordinator$OutputCommitCoordinatorEndpoint: OutputCommitCoordinator stopped! 17/01/31 00:12:28 INFO spark.SparkContext: Successfully stopped SparkContext

![image 2](<spark on yarn提交任务时报ClosedChannelException解决方案.note_images/imageFile2.png>)

复制代码

解决⽅案： 修改yarn-site.xml，添加下列property

![image 3](<spark on yarn提交任务时报ClosedChannelException解决方案.note_images/imageFile3.png>)

复制代码

<property> <name>yarn.nodemanager.pmem-check-enabled</name> <value>false</value>

</property>

<property> <name>yarn.nodemanager.vmem-check-enabled</name> <value>false</value>

</property>

![image 4](<spark on yarn提交任务时报ClosedChannelException解决方案.note_images/imageFile4.png>)

复制代码

分析： 按照上述配置提供的信息，⽬测是我给节点分配的内存太⼩，yarn直接kil掉了进程，导致 ClosedChanelException

⽂献参考：htp:/stackoverflow.com/questions/3898941/runing-yarn-with-spark-not-workingwith-java-8

实测：不修改yarn-site.xml，换成Java 7可正常执⾏。

