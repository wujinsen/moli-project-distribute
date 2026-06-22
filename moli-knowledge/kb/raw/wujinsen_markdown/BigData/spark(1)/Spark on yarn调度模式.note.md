背景

Spark on Yarn分yarn-cluster和yarn-client两种模式。 本⽂通过Cluster模式的TaskScheduler实现⼊⼿，梳理⼀遍spark on yarn的⼤致实现逻辑。

前提我对两种模式以及yarn任务的整体运⾏逻辑不是很清楚。

主体逻辑

cluster模式中，使⽤的TaskScheduler是YarnClusterScheduler。 它继承了默认使⽤的TaskSchedulerImpl类，额外在postStartHok⽅法⾥，唤醒了AplicationMaster 类的设置sparkcontext的⽅法。 AplicationMaster相当于是spark在yarn上的AM，内部的YarnRMClient类，负责向RM注册和注销 AM，以及拿到atemptId。注册AM之后，得到⼀个可以申请/释放资源的YarnAlocationHandler类，从 ⽽可以维护container与executor之间的关系。 下节具体介绍⼏个主要类的实现逻辑。

具体实现

AM

AplicationMaster，通过YarnRMClient来完成⾃⼰的注册和注销。 AM的启动⽅式

- 1 /**

* This object does not provide any special functionality. It exists so that it's easy to tell

- 2

* apart the client-mode AM from the cluster-mode AM when using tools such as ps or jps.

- 3

- 4 */

- 5 object ExecutorLauncher {

- 6

- 7 def main(args: Array[String]) = {

- 8 ApplicationMaster.main(args)

- 9 }

- 10

- 11 }


1 main⾥⾯调⽤AM的run⽅法：

- 1 def main(args: Array[String]) = {

- 2 SignalLogger.register(log)

- 3 val amArgs = new ApplicationMasterArguments(args)

- 4 SparkHadoopUtil.get.runAsSparkUser { () =>

- 5 master = new ApplicationMaster(amArgs, new YarnRMClientImpl(amArgs))

- 6 System.exit(master.run())

- 7 }

- 8 }


如果AM的启动参数⾥有⽤户⾃⼰定义的类，则是Driver模式，即cluster模式。⽤户⾃⼰定义的类⾥⾯带了 spark driver，会在单独⼀个线程⾥启动。这也是cluster模式与client模式的区别，⽤户实现了 driver vs ⽤户只是提交app。

1

run⽅法⾥

- 1. 如果不是Driver模式，执⾏runExecutorLauncher逻辑： 启动后，执⾏registerAM，⾥⾯new了YarnAlocator的实现，调⽤alocateResources， 申请并执⾏ container。同时，启动⼀个reporter线程，每隔⼀段时间调⽤YarnAlocator的alocateResources⽅ 法，或汇报有太多executor fail了。

- 2. 如果是Driver模式，执⾏runDriver逻辑： 也是执⾏registerAM，但是之前需要反射执⾏jar包⾥⽤户定义的driver类。


# YarnAlocator

YarnAlocator负责向yarn申请和释放containers，维护containe、executor相关关系，有⼀个线程池。 申请到container之后，在container⾥执⾏ExecutorRunable。需要⼦类实现的是申请和释放这两个⽅ 法：

- 1 protected def allocateContainers(count: Int, pending: Int): YarnAllocateResponse

- 2 protected def releaseContainer(container: Container): Unit


1 YarnAllocationHandler继承了YarnAllocator。

- 1.
- 2.


alocateContainers⽅法: Yarn api⾥提供ResourceRequest这个类，⾥⾯包含了⼀个ap向RM索要 不同container的信息，包括机器名/机架名，cpu和mem资源数，container数，优先级，locality 是否放松。然后组成AlocateRequest类，代表AM向RM从集群⾥获得resource。调⽤ AplicationMasterProtocal的alocate(AlocateRequest)，由AM*向RM发起资源请求 *。 releaseContainer⽅法: 每次把需要release的container记录下来。在每次alocateContainers调⽤ 的时候， 会往AlocateRequest⾥adAlReleases(releasedContainerList)，在请求资源的时候顺 便把历史资源释放掉。

ExecutorRunable与Yarn的关系：

- 1. 向ContainerManager建⽴连接，让cm来startContainer。
- 2. ContainerLaunchContext包含了yarn的NodeManager启动⼀个container需要的所有信息。 ExecutorRunable会构建这个container申请信息。 可以参考这段启动逻辑：


- 1 def startContainer = {

- 2 logInfo("Setting up ContainerLaunchContext")

- 3

- 4 val ctx = Records.newRecord(classOf[ContainerLaunchContext])

- 5 .asInstanceOf[ContainerLaunchContext]

- 6

- 7 ctx.setContainerId(container.getId())

- 8 ctx.setResource(container.getResource())

- 9 val localResources = prepareLocalResources

- 10 ctx.setLocalResources(localResources)

- 11

- 12 val env = prepareEnvironment

- 13 ctx.setEnvironment(env)

- 14

- 15 ctx.setUser(UserGroupInformation.getCurrentUser().getShortUserName())

- 16

- 17 val credentials = UserGroupInformation.getCurrentUser().getCredentials()

- 18 val dob = new DataOutputBuffer()

- 19 credentials.writeTokenStorageToStream(dob)

- 20 ctx.setContainerTokens(ByteBuffer.wrap(dob.getData()))

- 21

val commands = prepareCommand(masterAddress, slaveId, hostname, executorMemory, executorCores,

- 22

- 23 appAttemptId, localResources)

- 24 logInfo("Setting up executor with commands: " + commands)

- 25 ctx.setCommands(commands)

- 26

ctx.setApplicationACLs(YarnSparkHadoopUtil.getApplicationAclsForYarn(securityMgr ))

- 27

- 28

// If external shuffle service is enabled, register with the Yarn shuffle service already

- 29

// started on the NodeManager and, if authentication is enabled, provide it with our secret

- 30

- 31 // key for fetching shuffle files later

- 32 if (sparkConf.getBoolean("spark.shuffle.service.enabled", false)) {

- 33 val secretString = securityMgr.getSecretKey()

- 34 val secretBytes =

- 35 if (secretString != null) {


// This conversion must match how the YarnShuffleService decodes our secret

- 36

- 37 JavaUtils.stringToBytes(secretString)

- 38 } else {

- 39 // Authentication is not enabled, so just provide dummy metadata

- 40 ByteBuffer.allocate(0)

- 41 }

ctx.setServiceData(Map[String, ByteBuffer]("spark_shuffle" -> secretBytes))

- 42

- 43 }

- 44

- 45 // Send the start request to the ContainerManager

- 46 val startReq = Records.newRecord(classOf[StartContainerRequest])

- 47 .asInstanceOf[StartContainerRequest]

- 48 startReq.setContainerLaunchContext(ctx)

- 49 cm.startContainer(startReq)

- 50 }


值得注意的是setServiceData⽅法，如果在node manager上启动了external shuffle service。 Yarn的AuxiliaryService⽀持在NodeManager上启动辅助服务。spark有⼀个参数 spark.shuffle.service.enabled来设置该服务是否被启⽤，我看的1.2.0版本⾥貌似没有服务的实现 代码。

1

# Executor

此外，从ExecutorRunableUtil的prepareComand⽅法可以得知，ExecutorRunable通过命令⾏启 动了 CoarseGrainedExecutorBackend 进程，与粗粒度的mesos模式和standalone模式⼀致，task最终 落到CoarseGrainedExecutorBackend⾥⾯执⾏。

