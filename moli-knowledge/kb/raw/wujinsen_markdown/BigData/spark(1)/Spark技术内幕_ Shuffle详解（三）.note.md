# Spark技术内幕: Shufle详解（三）

前两篇⽂章写了Shufle Read的⼀些实现细节。但是要想彻底理清楚这⾥边的实现逻辑，还是需要更多 篇幅的；本篇开始，将按照Job的执⾏顺序，来讲解Shufle。即，结果数据（ShufleMapTask的结果 和ResultTask的结果）是如何产⽣的；结果是如何处理的；结果是如何读取的。 在Worker上接收Task执⾏命令的是org.apache.spark.executor.CoarseGrainedExecutorBackend。它 在接收到LaunchTask的命令后，通过在Driver创建SparkContext时已经创建的 org.apache.spark.executor.Executor的实例的launchTask，启动Task： [java]

view plaincopy deflaunchTask(

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


context: ExecutorBackend, taskId: Long, taskName: String,serializedTask: ByteBufer) { val tr = new TaskRuner(context, taskId, taskName, serializedTask) runingTasks.put(taskId, tr) threadPol.execute(tr) / 开始在executor中运⾏

}

最终Task的执⾏是在org.apache.spark.executor.Executor.TaskRuner#run。 org.apache.spark.executor.ExecutorBackend是Executor与Driver通信的接⼝，它实际上是⼀个trait：

[java]

view plaincopy private[spark] trait ExecutorBackend {

- 1.
- 2.
- 3.


defstatusUpdate(taskId: Long, state: TaskState, data: ByteBufer) } TaskRuner会将Task执⾏的状态汇报给Driver （org.apache.spark.scheduler.cluster.CoarseGrainedSchedulerBackend.DriverActor）。 ⽽ Driver会转给org.apache.spark.scheduler.TaskSchedulerImpl#statusUpdate。

在Executor运⾏Task时，得到计算结果会存⼊org.apache.spark.scheduler.DirectTaskResult。在将结 果回传到Driver时，会根据结果的⼤⼩有不同的策略：对于“较⼤”的结果，将其以taskid为key存⼊ org.apache.spark.storage.BlockManager；如果结果不⼤，那么直接回传给Driver。那么如何判定这 个阈值呢？ 这⾥的回传是直接通过aka的消息传递机制。因此这个⼤⼩⾸先不能超过这个机制设置的消息的最⼤ 值。这个最⼤值是通过spark.aka.frameSize设置的，单位是Bytes，默认值是10MB。除此之外，还有 20KB的预留空间。因此这个阈值就是conf.getInt("spark.aka.frameSize", 10) * 1024 *1024 – 20KB。 [java]

view plaincopy

- 1.
- 2.


/ directSend = sending directly back to the driver val (serializedResult, directSend) = {

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


if (resultSize >=akaFrameSize - AkaUtils.reservedSizeBytes) { /如果结果太⼤，那么存⼊

BlockManager val blockId = TaskResultBlockId(taskId) env.blockManager.putBytes(

blockId, serializedDirectResult,StorageLevel.MEMORY_AND_DISK_SER) (ser.serialize(new IndirectTaskResult[Any](blockId), false)

} else { / 如果⼤⼩合适，则直接发送结果给Driver

(serializedDirectResult, true) }

} execBackend.statusUpdate(taskId, TaskState.FINISHED, serializedResult)

TaskRuner将Task的执⾏状态汇报给Driver后，Driver会转给 org.apache.spark.scheduler.TaskSchedulerImpl#statusUpdate。⽽在这⾥不同的状态有不同的处 理：

- 1. 如果类型是TaskState.FINISHED，那么调⽤ org.apache.spark.scheduler.TaskResultGeter#enqueueSucesfulTask进⾏处理。
- 2. 如果类型是TaskState.FAILED或者TaskState.KI LED或者TaskState.LOST，调⽤ org.apache.spark.scheduler.TaskResultGeter#enqueueFailedTask进⾏处理。对于 TaskState.LOST，还需要将其所在的Executor标记为failed, 并且根据更新后的Executor重新调度。


enqueueSucesfulTask的逻辑也⽐较简单，就是如果是IndirectTaskResult，那么需要通过blockid来 获取结果：sparkEnv.blockManager.getRemoteBytes(blockId)；如果是DirectTaskResult，那么结果 就⽆需远程获取了。然后调⽤

- 1. org.apache.spark.scheduler.TaskSchedulerImpl#handleSucesfulTask
- 2. org.apache.spark.scheduler.TaskSetManager#handleSucesfulTask
- 3. org.apache.spark.scheduler.DAGScheduler#taskEnded
- 4. org.apache.spark.scheduler.DAGScheduler#eventProcesActor
- 5. org.apache.spark.scheduler.DAGScheduler#handleTaskCompletion 进⾏处理。核⼼逻辑都在第5个调⽤栈。如果task是ResultTask，处理逻辑⽐较简单，停⽌job，更新⼀ 些状态，发送⼀些event即可。 [java]


view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


if (!job.finished(rt.outputId){ job.finished(rt.outputId) =true job.numFinished += 1

/ If the whole job hasfinished, remove it if (job.numFinished =job.numPartitions) { markStageAsFinished(stage)

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
- 20.


cleanupStateForJobAndIndependentStages(job)

listenerBus.post(SparkListenerJobEnd(job.jobId,JobSuceded) }

/ taskSuceded runs someuser code that might throw an exception. / Make sure we areresilient against that.

try {

job.listener.taskSuceded(rt.outputId, event.result) } catch {

case e: Exception => / TODO: Perhaps we wanto mark the stage as failed?

job.listener.jobFailed(new SparkDriverExecutionException(e) }

}

如果task是ShufleMapTask，那么它需要将结果通过某种机制告诉下游的Stage，以便于其可以作为下 游Stage的输⼊。这个机制是怎么实现的？ 实际上，对于ShufleMapTask来说，其结果实际上是org.apache.spark.scheduler.MapStatus；其序 列化后存⼊了DirectTaskResult或者IndirectTaskResult中。⽽DAGScheduler#handleTaskCompletion 通过下⾯的⽅式来获取这个结果： val status =event.result.asInstanceOf[MapStatus] 通过将这个status注册到org.apache.spark.MapOutputTrackerMaster，就实现了 [java]

view plaincopy mapOutputTracker.registerMapOutputs(

- 1.
- 2.
- 3.
- 4.


stage.shufleDep.get.shufleId, stage.outputLocs.map(list=> if (list.isEmpty) nulelse list.head).toAray, changeEpoch = true)

