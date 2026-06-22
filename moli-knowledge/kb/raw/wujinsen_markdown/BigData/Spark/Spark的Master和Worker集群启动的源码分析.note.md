# 基于spark1.3.1的源码进⾏分析 spark master启动源码分析1、在start-master.sh调⽤master的main⽅法，main⽅法调⽤ def main(argStrings: Array[String]) {

SignalLogger.register(log) val conf = new SparkConf val args = new MasterArguments(argStrings, conf) val (actorSystem, _, _, _) = startSystemAndActor(args.host, args.port, args.webUiPort, conf)//启动系统和actor actorSystem.awaitTermination()

}

- 2、调⽤startSystemAndActor启动系统和创建actor def startSystemAndActor(

host: String, port: Int, webUiPort: Int, conf: SparkConf): (ActorSystem, Int, Int, Option[Int]) = {

val securityMgr = new SecurityManager(conf) val (actorSystem, boundPort) = AkkaUtils.createActorSystem(systemName, host, port, conf = conf,

securityManager = securityMgr) val actor = actorSystem.actorOf(

Props(classOf[Master], host, boundPort, webUiPort, securityMgr, conf), actorName) val timeout = AkkaUtils.askTimeout(conf) val portsRequest = actor.ask(BoundPortsRequest)(timeout) val portsResponse = Await.result(portsRequest, timeout).asInstanceOf[BoundPortsResponse] (actorSystem, boundPort, portsResponse.webUIPort, portsResponse.restPort)

- 3、调⽤AkkaUtils.createActorSystem来创建ActorSystem def createActorSystem(

name: String, host: String, port: Int, conf: SparkConf, securityManager: SecurityManager): (ActorSystem, Int) = {

val startService: Int => (ActorSystem, Int) = { actualPort => doCreateActorSystem(name, host, actualPort, conf, securityManager)

} Utils.startServiceOnPort(port, startService, conf, name)

}

- 4、调⽤Utils.startServiceOnPort启动⼀个端⼝上的服务，创建成功后调⽤doCreateActorSystem创建ActorSystem

- 5、ActorSystem创建成功后创建Actor

- 6、调⽤Master的主构造函数，执⾏preStart()


- 1、start-slaves.sh调⽤Worker类的main⽅法 def main(argStrings: Array[String]) {

SignalLogger.register(log) val conf = new SparkConf val args = new WorkerArguments(argStrings, conf) val (actorSystem, _) = startSystemAndActor(args.host, args.port, args.webUiPort, args.cores,

args.memory, args.masters, args.workDir) actorSystem.awaitTermination()

}

- 2、调⽤startSystemAndActor启动系统和创建actor def startSystemAndActor(

host: String, port: Int, webUiPort: Int, cores: Int, memory: Int, masterUrls: Array[String], workDir: String, workerNumber: Option[Int] = None, conf: SparkConf = new SparkConf): (ActorSystem, Int) = {

// The LocalSparkCluster runs multiple local sparkWorkerX actor systems val systemName = "sparkWorker" + workerNumber.map(_.toString).getOrElse("") val actorName = "Worker" val securityMgr = new SecurityManager(conf) val (actorSystem, boundPort) = AkkaUtils.createActorSystem(systemName, host, port,

conf = conf, securityManager = securityMgr) val masterAkkaUrls = masterUrls.map(Master.toAkkaUrl(_, AkkaUtils.protocol(actorSystem))) actorSystem.actorOf(Props(classOf[Worker], host, boundPort, webUiPort, cores, memory,

masterAkkaUrls, systemName, actorName, workDir, conf, securityMgr), name = actorName) (actorSystem, boundPort)

}

- 3、调⽤AkkaUtils的createActorSystem创建ActorSystem def createActorSystem(


name: String, host: String, port: Int, conf: SparkConf, securityManager: SecurityManager): (ActorSystem, Int) = {

val startService: Int => (ActorSystem, Int) = { actualPort => doCreateActorSystem(name, host, actualPort, conf, securityManager)

} Utils.startServiceOnPort(port, startService, conf, name)

}

- 4、创建完ActorSystem后调⽤Worker的主构造函数，执⾏preStart⽅法 override def preStart() {

assert(!registered) logInfo("Starting Spark worker %s:%d with %d cores, %s RAM".format(

host, port, cores, Utils.megabytesToString(memory))) logInfo(s"Running Spark version ${org.apache.spark.SPARK_VERSION}") logInfo("Spark home: " + sparkHome) createWorkDir() context.system.eventStream.subscribe(self, classOf[RemotingLifecycleEvent]) shuffleService.startIfEnabled() webUi = new WorkerWebUI(this, workDir, webUiPort) webUi.bind() registerWithMaster()

metricsSystem.registerSource(workerSource) metricsSystem.start() // Attach the worker metrics servlet handler to the web ui after the metrics system is started. metricsSystem.getServletHandlers.foreach(webUi.attachHandler)

}

- 5、调⽤registerWithMaster⽅法向Master注册启动的worker def registerWithMaster() {


// DisassociatedEvent may be triggered multiple times, so don't attempt registration // if there are outstanding registration attempts scheduled. registrationRetryTimer match {

case None => registered = false tryRegisterAllMasters() connectionAttemptCount = 0 registrationRetryTimer = Some {

context.system.scheduler.schedule(INITIAL_REGISTRATION_RETRY_INTERVAL,

INITIAL_REGISTRATION_RETRY_INTERVAL, self, ReregisterWithMaster) }

case Some(_) => logInfo("Not spawning another attempt to register with the master, since there is an" +

" attempt scheduled already.") }

}

- 6、调⽤tryRegisterAllMasters向Master发送注册的Worker消息 private def tryRegisterAllMasters() {

for (masterAkkaUrl <- masterAkkaUrls) { logInfo("Connecting to master " + masterAkkaUrl + "...") val actor = context.actorSelection(masterAkkaUrl) actor ! RegisterWorker(workerId, host, port, cores, memory, webUi.boundPort, publicAddress)

} }

- 7、Master的receiveWithLogging接收到消息执⾏ case RegisterWorker(id, workerHost, workerPort, cores, memory, workerUiPort, publicAddress) =>

{

logInfo("Registering worker %s:%d with %d cores, %s RAM".format(

workerHost, workerPort, cores, Utils.megabytesToString(memory))) if (state == RecoveryState.STANDBY) {

// ignore, don't send response } else if (idToWorker.contains(id)) {

sender ! RegisterWorkerFailed("Duplicate worker ID") } else {

val worker = new WorkerInfo(id, workerHost, workerPort, cores, memory, sender, workerUiPort, publicAddress)

if (registerWorker(worker)) { persistenceEngine.addWorker(worker) sender ! RegisteredWorker(masterUrl, masterWebUiUrl) schedule() val workerAddress = worker.actor.path.address logWarning("Worker registration failed. Attempted to re-register worker at same " +

"address: " + workerAddress) sender ! RegisterWorkerFailed("Attempted to re-register worker at same address: "

+ workerAddress) }

} }

} else {

- 8、失败向worker返回失败消息，成功则返回Master的相关信息

- 9、返回消息后调⽤schedule，但是因为没有application，所以这时候不会进⾏资源的分配


⾄此整个Spark集群就已经启动完成

