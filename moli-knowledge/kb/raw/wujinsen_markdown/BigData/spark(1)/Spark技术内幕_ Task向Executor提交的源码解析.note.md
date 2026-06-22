2014-10-1918 1614829⼈阅读 Spark技术内幕: Task向Executor提交的源码解析 Spar k云计算

分类：

(12)

评论 收藏举报 rdspark任务调度

在上⽂《Spark技术内幕：Stage划分及提交源码分析》中，我们分析了Stage的⽣成和提交。但是 Stage的提交，只是DAGScheduler完成了对DAG的划分，⽣成了⼀个计算拓扑，即需要按照顺序计算 的Stage，Stage中包含了可以以partition为单位并⾏计算的Task。我们并没有分析Stage中得Task是如 何⽣成并且最终提交到Executor中去的。

这就是本⽂的主题。 从org.apache.spark.scheduler.DAGScheduler#submitMisingTasks开始，分析Stage是如 何⽣成TaskSet的。 如果⼀个Stage的所有的parent stage都已经计算完成或者存在于cache中，那么他会调⽤ submitMisingTasks来提交该Stage所包含的Tasks。 org.apache.spark.scheduler.DAGScheduler#submitMisingTasks的计算流程如下：

- 1.
- 2.
- 3.
- 4.
- 5.


⾸先得到RD中需要计算的partition，对于Shufle类型的stage，需要判断stage中是否缓存了该 结果；对于Result类型的Final Stage，则判断计算Job中该partition是否已经计算完成。 序列化task的binary。Executor可以通过⼴播变量得到它。每个task运⾏的时候⾸先会反序列化。 这样在不同的executor上运⾏的task是隔离的，不会相互影响。 为每个需要计算的partition⽣成⼀个task：对于Shufle类型依赖的Stage，⽣成ShufleMapTask类 型的task；对于Result类型的Stage，⽣成⼀个ResultTask类型的task 确保Task是可以被序列化的。因为不同的cluster有不同的taskScheduler，在这⾥判断可以简化逻 辑；保证TaskSet的task都是可以序列化的 通过TaskScheduler提交TaskSet。

TaskSet就是可以做pipeline的⼀组完全相同的task，每个task的处理逻辑完全相同，不同的 是处理数据，每个task负责处理⼀个partition。pipeline，可以称为⼤数据处理的基⽯，只 有数据进⾏pipeline处理，才能将其放到集群中去运⾏。对于⼀个task来说，它从数据源获 得逻辑，然后按照拓扑顺序，顺序执⾏（实际上是调⽤rd的compute）。 TaskSet是⼀个数据结构，存储了这⼀组task：

[java]

view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.


private[spark] clas TaskSet( val tasks: Aray[Task[_], val stageId: Int, val atempt: Int, val priority: Int,

- 6.
- 7.
- 8.
- 9.
- 10.


val properties: Properties) { val id: String = stageId + "." + atempt

overide def toString: String = "TaskSet " + id }

管理调度这个TaskSet的时org.apache.spark.scheduler.TaskSetManager， TaskSetManager会负责task的失败重试；跟踪每个task的执⾏状态；处理locality-aware的 调⽤。 详细的调⽤堆栈如下：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


org.apache.spark.scheduler.TaskSchedulerImpl#submitTasks org.apache.spark.scheduler.SchedulableBuilder#adTaskSetManager org.apache.spark.scheduler.cluster.CoarseGrainedSchedulerBackend#reviveOfers org.apache.spark.scheduler.cluster.CoarseGrainedSchedulerBackend.DriverActor#makeOfers org.apache.spark.scheduler.TaskSchedulerImpl#resourceOfers org.apache.spark.scheduler.cluster.CoarseGrainedSchedulerBackend.DriverActor#launchTasks

# org.apache.spark.executor.CoarseGrainedExecutorBackend.receiveWithLoging#lau nchTask

org.apache.spark.executor.Executor#launchTask

⾸先看⼀下org.apache.spark.executor.Executor#launchTask：

[java]

view plaincopy def launchTask(

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


context: ExecutorBackend, taskId: Long, taskName: String, serializedTask: ByteBufer) { val tr = new TaskRuner(context, taskId, taskName, serializedTask) runingTasks.put(taskId, tr) threadPol.execute(tr) / 开始在executor中运⾏

}

# TaskRuner会从序列化的task中反序列化得到task，这个需要 看 org.apache.spark.executor.Executor.TaskRuner#run 的实现：task.run(taskId.toInt)。 ⽽task.run的实现是：

[java]

view plaincopy final def run(atemptId: Long): T = {

1.

- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


context = new TaskContext(stageId, partitionId, atemptId, runingLocaly = false) context.taskMetrics.hostname = Utils.localHostName() taskThread = Thread.curentThread() if (_kiled) {

kil(interuptThread = false)

} runTask(context)

}

对于原来提到的两种Task，即

- 1.
- 2.


org.apache.spark.scheduler.ShufleMapTask org.apache.spark.scheduler.ResultTask

分别实现了不同的runTask: org.apache.spark.scheduler.ResultTask#runTask即顺序调⽤rd的compute，通过rd的拓 扑顺序依次对partition进⾏计算：

[java]

view plaincopy overide def runTask(context: TaskContext): U = {

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


/ Deserialize the RD and the func using the broadcast variables. val ser = SparkEnv.get.closureSerializer.newInstance() val (rd, func) = ser.deserialize[(RD[T], (TaskContext, Iterator[T]) => U)](

ByteBufer.wrap(taskBinary.value), Thread.curentThread.getContextClasLoader)

metrics = Some(context.taskMetrics) try {

func(context, rd.iterator(partition, context) } finaly {

context.markTaskCompleted() }

}

# ⽽org.apache.spark.scheduler.ShufleMapTask#runTask则是写shufle的结果，

[java]

view plaincopy overide def runTask(context: TaskContext): MapStatus = { / Deserialize the RD using the broadcast variable. val ser = SparkEnv.get.closureSerializer.newInstance()

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
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.


val (rd, dep) = ser.deserialize[(RD[_], ShufleDependency[_, _, _])]( ByteBufer.wrap(taskBinary.value), Thread.curentThread.getContextClasLoader)

/此处的taskBinary即为在org.apache.spark.scheduler.DAGScheduler#submitMisingTasks序 列化的task的⼴播变量取得的

metrics = Some(context.taskMetrics) var writer: ShufleWriter[Any, Any] = nul try {

val manager = SparkEnv.get.shufleManager writer = manager.getWriter[Any, Any](dep.shufleHandle, partitionId, context) writer.write(rd.iterator(partition, context).asInstanceOf[Iterator[_ <: Product2[Any, Any]) /

将rd计算的结果写⼊memory或者disk

return writer.stop(suces = true).get } catch {

case e: Exception => if (writer != nul) { writer.stop(suces = false)

} throw e

} finaly {

context.markTaskCompleted() }

}

这两个task都不要按照拓扑顺序调⽤rd的compute来完成对partition的计算，不同的是 ShufleMapTask需要shufle write，以供child stage读取shufle的结果。 对于这两个task都 ⽤到的taskBinary，即为在 org.apache.spark.scheduler.DAGScheduler#submitMisingTasks序列化的task的⼴播变量 取得的。

通过上述⼏篇博⽂，实际上我们已经粗略的分析了从⽤户定义SparkContext开始，集群是 如果为每个Aplication分配Executor的，回顾⼀下这个序列图：

# 还有就是⽤户触发某个action，集群是如何⽣成DAG，如果将DAG划分为可以成Stage，已 经Stage是如何将这些可以pipeline执⾏的task提交到Executor去执⾏的。当然了，具体细节 还是⾮常值得推敲的。以后的每个周末，都会奉上某个细节的实现。 休息了。明天⼜会开始忙碌的⼀周。

