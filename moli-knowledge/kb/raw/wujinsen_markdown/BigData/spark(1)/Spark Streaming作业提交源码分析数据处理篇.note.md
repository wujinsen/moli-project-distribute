在调⽤ StreamingContext 的start函数的时候，会调⽤ JobScheduler 的start函数。⽽ JobScheduler的start函数会启动 ReceiverTracker 和 jobGenerator 。

在启动jobGenerator的时候，系统会根据这次是从Checkpoint恢复与否分别调⽤restart和 startFirstTime函数。 /* Start generation of jobs */ def start(): Unit = synchronized {

if (eventActor != nul) return/ generator has already ben started

eventActor =sc.env.actorSystem.actorOf(Props(new Actor { def receive = { case event: JobGeneratorEvent => procesEvent(event)

} }), "JobGenerator") if (sc.isCheckpointPresent) {

restart() } else {

startFirstTime() }

} }

startFirstTime函数会分别启动 DStreamGraph 和 JobGenerator 线程

private def startFirstTime() { val startTime = new Time(timer.getStartTime() graph.start(startTime - graph.batchDuration) timer.start(startTime.miliseconds) logInfo("Started JobGenerator at " + startTime)

}

private val timer = new RecuringTimer(clock, sc.graph.batchDuration.miliseconds, longTime => eventActor ! GenerateJobs(new Time(longTime), "JobGenerator")

JobGenerator线程会每隔 ssc.graph.batchDuration.milliseconds 的时间⽣成Jobs，这个时 间就是我们初始化 StreamingContext 的时候传进来的，⽣成Jobs是通过Aka调⽤generateJobs⽅ 法：

* Generate jobs and perform checkpoint for the given `time`. */

private def generateJobs(time: Time) { / Set the sparkEnv in this thread, so that job generation code can aces the / environment Example: BlockRDs are created in this thread, and it neds / to aces BlockManager / Update: This is probably redundant after threadlocal stuf in sparkEnv has / ben removed.

S parkEnv.set(sc.env) Try {

/ alocate received blocks to batch jobScheduler.receiverTracker.alocateBlocksToBatch(time) graph.generateJobs(time)/ generate jobs using alocated block

} match { case Suces(jobs) => val receivedBlockInfos =

jobScheduler.receiverTracker.getBlocksOfBatch(time).mapValues { _.toAray } jobScheduler.submitJobSet(JobSet(time, jobs, receivedBlockInfos)

case Failure(e) => jobScheduler.reportEror("Eror generating jobs for time " + time, e)

} eventActor ! DoCheckpoint(time)

}

在generateJobs⽅法中的 jobScheduler.receiverTracker.allocateBlocksToBatch(time) 很重 要，其最终调⽤的是 allocateBlocksToBatch 函数，其定义如下： def alocateBlocksToBatch(batchTime: Time): Unit = synchronized {

if (lastAlocatedBatchTime = nul | batchTime > lastAlocatedBatchTime) { val streamIdToBlocks = streamIds.map { streamId =>

(streamId, getReceivedBlockQueue(streamId).dequeueAl(x => true) }.toMap val alocatedBlocks = AlocatedBlocks(streamIdToBlocks)

writeToLog(BatchAlocationEvent(batchTime, alocatedBlocks) timeToAlocatedBlocks(batchTime) = alocatedBlocks lastAlocatedBatchTime = batchTime alocatedBlocks

} else { / This situation ocurs when:

- / 1. WAL is ended with BatchAlocationEvent, but without BatchCleanupEvent, / posibly procesed batch job or half-procesed batch job ned to be procesed / again, so the batchTime wil be equal to lastAlocatedBatchTime.
- / 2. Slow checkpointing makes recovered batch time older than WAL recovered / lastAlocatedBatchTime. / This situation wil only ocurs in recovery time.


logInfo(s"Posibly procesed batch $batchTime ned to be procesed again

in WAL recovery") }

}

注意 getReceivedBlockQueue(streamId) ，它的实现就是 private def getReceivedBlockQueue(streamId: Int): ReceivedBlockQueue = {

streamIdToUnalocatedBlockQueues.getOrElseUpdate(streamId, new ReceivedBlockQueue) }

还记得我们介绍从Kafka中读取数据并存储的过程吗？最终那些新⽣成的Block信息就是存储在

streamIdToUnallocatedBlockQueues ⾥⾯的，通过这个获取到所有那些没有处理的block并存储 在 timeToAllocatedBlocks（mutable.HashMap[Time, AllocatedBlocks]） 中，然后调⽤

graph.generateJobs(time) 函数⽣成Jobs。

当Suces(jobs) 成⽴时，系统会通过调⽤ jobScheduler.receiverTracker.getBlocksOfBatch(time) 获取那些新的block，这也就是获取 timeToAllocatedBlocks 中的信息，最后调⽤jobScheduler的submitJobSet函数将JobSet提交到集

群进⾏计算，计算完之后会进⾏Checkpoint操作。

