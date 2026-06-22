#### Spark技术内幕：Shufle Map Task运算结果的处理

分类： 2015-01-12 08:02 8684⼈阅读 (10) 收藏

Spark 架构探索 评论 举报

SparkShuffleShuffle Write

⽬录(?)[+]

# Shuffle Map Task运算结果的处理

这个结果的处理，分为两部分，⼀个是在Executor端是如何直接处理Task的结果的；还有就是 Driver端，如果在接到Task运⾏结束的消息时，如何对Shuffle Write的结果进⾏处理，从⽽在调度 下游的Task时，下游的Task可以得到其需要的数据。

## Executor端的处理

##### 在解析BasicShuffle Writer时，我们知道ShuffleMap Task在Executor上运⾏时，最终会调⽤ org.apache.spark.scheduler.ShuffleMapTask的runTask：

[java] view plaincopy

![image 1](<Spark技术内幕：Shuffle Map Task运算结果的处理.note_images/imageFile1.png>)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


override def runTask(context: TaskContext): MapStatus = {

// 反序列化⼴播变量taskBinary得到RDD

val ser = SparkEnv.get.closureSerializer.newInstance()

val (rdd, dep) = ser.deserialize[(RDD[_], ShuffleDependency[_, _, _])](

ByteBuffer.wrap(taskBinary.value),Thread.currentThread.getContextClassLoader)

//省略⼀些⾮核⼼代码

- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.


val manager =SparkEnv.get.shuffleManager //获得Shuffle Manager

//获得Shuffle Writer

writer= manager.getWriter[Any, Any](dep.shuffleHandle, partitionId, context)

//⾸先调⽤rdd .iterator，如果该RDD已经cache了或者checkpoint了，那么直接读取

//结果，否则开始计算计算的结果将调⽤Shuffle Writer写⼊本地⽂件系统

writer.write(rdd.iterator(partition,context).asInstanceOf[Iterator[_ <: Product2[Any, Any]] ])

// 返回数据的元数据信息，包括location和size

returnwriter.stop(success = true).get

那么这个结果最终是如何处理的呢？特别是下游的Task如何获取这些Shuffle的数据呢？还要从 Task是如何开始执⾏开始讲起。在Worker上接收Task执⾏命令的是 org.apache.spark.executor.CoarseGrainedExecutorBackend。它在接收到LaunchTask的命令 后，通过在Driver创建SparkContext时已经创建的org.apache.spark.executor.Executor的实例的 launchTask，启动Task：

[java] view plaincopy

![image 2](<Spark技术内幕：Shuffle Map Task运算结果的处理.note_images/imageFile2.png>)

<table>
  <tr>
    <th>![image 3](<Spark技术内幕：Shuffle Map Task运算结果的处理.note_images/imageFile3.png>)</th>
  </tr>
</table>


def launchTask(

- 2.
- 3.
- 4.
- 5.
- 6.


context:ExecutorBackend, taskId: Long, taskName: String,serializedTask: ByteBuffer) {

val tr = newTaskRunner(context, taskId, taskName, serializedTask)

runningTasks.put(taskId, tr)

threadPool.execute(tr) // 开始在executor中运⾏

}

最终Task的执⾏是在org.apache.spark.executor.Executor.TaskRunner#run。

在Executor运⾏Task时，得到计算结果会存⼊org.apache.spark.scheduler.DirectTaskResult。

[java] view plaincopy

![image 4](<Spark技术内幕：Shuffle Map Task运算结果的处理.note_images/imageFile4.png>)

<table>
  <tr>
    <th>![image 5](<Spark技术内幕：Shuffle Map Task运算结果的处理.note_images/imageFile5.png>)</th>
  </tr>
</table>


//开始执⾏Task，最终得到的是org.apache.spark.scheduler.ShuffleMapTask#runTask

- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


//返回的org.apache.spark.scheduler.MapStatus

val value = task.run(taskId.toInt)

val resultSer = env.serializer.newInstance() //获得序列化⼯具

val valueBytes = resultSer.serialize(value) //序列化结果

//⾸先将结果直接放⼊org.apache.spark.scheduler.DirectTaskResult

val directResult = new DirectTaskResult(valueBytes,accumUpdates, task.metrics.orNull)

val ser = env.closureSerializer.newInstance()

val serializedDirectResult = ser.serialize(directResult)//序列化结果

val resultSize = serializedDirectResult.limit //序列化结果的⼤⼩

在将结果回传到Driver时，会根据结果的⼤⼩有不同的策略：

- 1) 如果结果⼤于1GB，那么直接丢弃这个结果。这个是Spark1.2中新加的策略。可以通过 spark.driver.maxResultSize来进⾏设置。

- 2) 对于“较⼤”的结果，将其以taskid为key存⼊org.apache.spark.storage.BlockManager；如果 结果不⼤，那么直接回传给Driver。那么如何判定这个阈值呢？

这⾥的回传是直接通过akka的消息传递机制。因此这个⼤⼩⾸先不能超过这个机制设置的消息的 最⼤值。这个最⼤值是通过spark.akka.frameSize设置的，单位是MBytes，默认值是10MB。除此 之外，还有200KB的预留空间。因此这个阈值就是conf.getInt("spark.akka.frameSize",10) * 1024

*1024 – 200*1024。

- 3) 其他的直接通过AKKA回传到Driver。


实现源码解析如下：

###### [java] view plaincopy

![image 6](<Spark技术内幕：Shuffle Map Task运算结果的处理.note_images/imageFile6.png>)

<table>
  <tr>
    <th>![image 7](<Spark技术内幕：Shuffle Map Task运算结果的处理.note_images/imageFile7.png>)</th>
  </tr>
</table>


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


val serializedResult = {

if (maxResultSize > 0 &&resultSize > maxResultSize) {

// 如果结果的⼤⼩⼤于1GB，那么直接丢弃，

// 可以在spark.driver.maxResultSize设置

ser.serialize(newIndirectTaskResult[Any](TaskResultBlockId(taskId),

resultSize))

} else if (resultSize >=akkaFrameSize - AkkaUtils.reservedSizeBytes) {

// 如果不能通过AKKA的消息传递，那么放⼊BlockManager

// 等待调⽤者以⽹络的形式来获取。AKKA的消息的默认⼤⼩可以通过

// spark.akka.frameSize来设置，默认10MB。

val blockId =TaskResultBlockId(taskId)

env.blockManager.putBytes(

blockId, serializedDirectResult,StorageLevel.MEMORY_AND_DISK_SER)

ser.serialize(newIndirectTaskResult[Any](blockId, resultSize))

###### } else {

//结果可以直接回传到Driver

serializedDirectResult

}

- 19.
- 20.
- 21.


}

// 通过AKKA向Driver汇报本次Task的已经完成

execBackend.statusUpdate(taskId,TaskState.FINISHED, serializedResult)

⽽execBackend是org.apache.spark.executor.ExecutorBackend的⼀个实例，它实际上是Executor 与Driver通信的接⼝：

[java] view plaincopy

![image 8](<Spark技术内幕：Shuffle Map Task运算结果的处理.note_images/imageFile8.png>)

<table>
  <tr>
    <th>![image 9](<Spark技术内幕：Shuffle Map Task运算结果的处理.note_images/imageFile9.png>)</th>
  </tr>
</table>


- 1.
- 2.


private[spark] trait ExecutorBackend {

def statusUpdate(taskId:Long, state: TaskState, data: ByteBuffer)

3. }

TaskRunner会将Task执⾏的状态汇报给Driver （org.apache.spark.scheduler.cluster.CoarseGrainedSchedulerBackend.DriverActor）。 ⽽ Driver会转给org.apache.spark.scheduler.TaskSchedulerImpl#statusUpdate。

### Driver的处理

TaskRunner将Task的执⾏状态汇报给Driver后，Driver会转给 org.apache.spark.scheduler.TaskSchedulerImpl#statusUpdate。⽽在这⾥不同的状态有不同的处 理：

- 1) 如果类型是TaskState.FINISHED，那么调⽤ org.apache.spark.scheduler.TaskResultGetter#enqueueSuccessfulTask进⾏处理。

- 2) 如果类型是TaskState.FAILED或者TaskState.KILLED或者TaskState.LOST，调⽤ org.apache.spark.scheduler.TaskResultGetter#enqueueFailedTask进⾏处理。对于 TaskState.LOST，还需要将其所在的Executor标记为failed,并且根据更新后的Executor重新调度。


enqueueSuccessfulTask的逻辑也⽐较简单，就是如果是IndirectTaskResult，那么需要通过 blockid来获取结果：sparkEnv.blockManager.getRemoteBytes(blockId)；如果是 DirectTaskResult，那么结果就⽆需远程获取了。然后调⽤

- 1) org.apache.spark.scheduler.TaskSchedulerImpl#handleSuccessfulTask

- 2) org.apache.spark.scheduler.TaskSetManager#handleSuccessfulTask

- 3) org.apache.spark.scheduler.DAGScheduler#taskEnded

- 4) org.apache.spark.scheduler.DAGScheduler#eventProcessActor

- 5) org.apache.spark.scheduler.DAGScheduler#handleTaskCompletion 进⾏处理。核⼼逻辑都在第5个调⽤栈。


如果task是ShuffleMapTask，那么它需要将结果通过某种机制告诉下游的Stage，以便于其可以作 为下游Stage的输⼊。这个机制是怎么实现的？

实际上，对于ShuffleMapTask来说，其结果实际上是org.apache.spark.scheduler.MapStatus；其 序列化后存⼊了DirectTaskResult或者IndirectTaskResult中。⽽ DAGScheduler#handleTaskCompletion通过下⾯的⽅式来获取这个结果：

val status=event.result.asInstanceOf[MapStatus]

##### 通过将这个status注册到org.apache.spark.MapOutputTrackerMaster，就完成了结果处理的漫⻓ 过程：

[java] view plaincopy

![image 10](<Spark技术内幕：Shuffle Map Task运算结果的处理.note_images/imageFile10.png>)

<table>
  <tr>
    <th>![image 11](<Spark技术内幕：Shuffle Map Task运算结果的处理.note_images/imageFile11.png>)</th>
  </tr>
</table>


- 1.
- 2.
- 3.
- 4.


mapOutputTracker.registerMapOutputs(

stage.shuffleDep.get.shuffleId,

stage.outputLocs.map(list=> if (list.isEmpty) null else list.head).toArray,

changeEpoch = true)

⽽registerMapOutputs的处理也很简单，以Shuffle ID为key将MapStatus的列表存⼊带有时间戳的 HashMap：TimeStampedHashMap[Int, Array[MapStatus]]()。如果设置了cleanup的函数，那么这 个HashMap会将超过⼀定时间（TTL，Time to Live）的数据清理掉。

