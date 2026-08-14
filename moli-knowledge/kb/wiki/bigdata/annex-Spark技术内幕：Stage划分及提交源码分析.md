---
title: Spark技术内幕：Stage划分及提交源码分析.note（原文插图 annex）
slug: annex-Spark技术内幕：Stage划分及提交源码分析
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Stage划分及提交源码分析.note.md
related: [spark-核心概念与实践]
created: 2026-07-05
updated: 2026-07-05
---

# Spark技术内幕：Stage划分及提交源码分析

分类： 2014-10-18 23:05 17316⼈阅读 (25) 收藏

Spark 云计算 评论 举报

sparkstageRDD

⽬录(?)[+]

当触发⼀个RDD的action后，以count为例，调⽤关系如下：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


org.apache.spark.rdd.RDD#count org.apache.spark.SparkContext#runJob org.apache.spark.scheduler.DAGScheduler#runJob org.apache.spark.scheduler.DAGScheduler#submitJob org.apache.spark.scheduler.DAGSchedulerEventProcessActor#receive （JobSubmitted） org.apache.spark.scheduler.DAGScheduler#handleJobSubmitted

其中步骤五的DAGSchedulerEventProcessActor是DAGScheduler 的与外部交互的接 ⼝代理，DAGScheduler在创建时会创建名字为eventProcessActor的actor。这个actor 的作⽤看它的实现就⼀⽬了然了：

[java] view plaincopy

![image 1](assets/imageFile1.png)

- 1.
- 2.
- 3.
- 4.


/**

- * The main event loop of the DAG scheduler.

- */


def receive = {

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


case JobSubmitted(jobId, rdd, func, partitions, allowLocal, callSite, listener, propertie s) =>

dagScheduler.handleJobSubmitted(jobId, rdd, func, partitions, allowLocal, callSite,

listener, properties) // 提交job，来⾃与RDD->SparkContext->DAGScheduler的消息。之所以在这需 要在这⾥中转⼀下，是为了模块功能的⼀致性。

case StageCancelled(stageId) => // 消息源org.apache.spark.ui.jobs.JobProgressTab，在GUI上显 示⼀个SparkContext的Job的执⾏状态。

// ⽤户可以cancel⼀个Stage，会通过SparkContext->DAGScheduler 传递到这⾥。

dagScheduler.handleStageCancellation(stageId)

case JobCancelled(jobId) => // 来⾃于org.apache.spark.scheduler.JobWaiter的消息。取消⼀个 Job

dagScheduler.handleJobCancellation(jobId)

case JobGroupCancelled(groupId) => // 取消整个Job Group

dagScheduler.handleJobGroupCancelled(groupId)

case AllJobsCancelled => //取消所有Job

dagScheduler.doCancelAllJobs()

- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.


case ExecutorAdded(execId, host) => // TaskScheduler得到⼀个Executor被添加的消息。具体来⾃ org.apache.spark.scheduler.TaskSchedulerImpl.resourceOffers

dagScheduler.handleExecutorAdded(execId, host)

case ExecutorLost(execId) => //来⾃TaskScheduler

dagScheduler.handleExecutorLost(execId)

case BeginEvent(task, taskInfo) => // 来⾃TaskScheduler

dagScheduler.handleBeginEvent(task, taskInfo)

case GettingResultEvent(taskInfo) => //处理获得TaskResult信息的消息

dagScheduler.handleGetTaskResult(taskInfo)

case completion @ CompletionEvent(task, reason, _, _, taskInfo, taskMetrics) => //来⾃ TaskScheduler，报告task是完成或者失败

dagScheduler.handleTaskCompletion(completion)

case TaskSetFailed(taskSet, reason) => //来⾃TaskScheduler，要么TaskSet失败次数超过阈值或者由于 Job Cancel。

dagScheduler.handleTaskSetFailed(taskSet, reason)

- 39.
- 40.
- 41.
- 42.


case ResubmitFailedStages => //当⼀个Stage处理失败时，重试。来⾃ org.apache.spark.scheduler.DAGScheduler.handleTaskCompletion

dagScheduler.resubmitFailedStages()

}

总结⼀下org.apache.spark.scheduler.DAGSchedulerEventProcessActor的作⽤：可以 把他理解成DAGScheduler的对外的功能接⼝。它对外隐藏了⾃⼰内部实现的细节，也 更易于理解其逻辑；也降低了维护成本，将DAGScheduler的⽐较复杂功能接⼝化。

## handleJobSubmitted

org.apache.spark.scheduler.DAGScheduler#handleJobSubmitted⾸先会根据RDD创建 finalStage。finalStage，顾名思义，就是最后的那个Stage。然后创建job，最后提交。 提交的job如果满⾜⼀下条件，那么它将以本地模式运⾏：

1）spark.localExecution.enabled设置为true 并且 2）⽤户程序显式指定可以本地运⾏ 并且 3）finalStage的没有⽗Stage 并且 4）仅有⼀个partition

3）和 4）的话主要为了任务可以快速执⾏；如果有多个stage或者多个partition的话， 本地运⾏可能会因为本机的计算资源的问题⽽影响任务的计算速度。 要理解什么是Stage，⾸先要搞明⽩什么是Task。Task是在集群上运⾏的基本单位。⼀ 个Task负责处理RDD的⼀个partition。RDD的多个patition会分别由不同的Task去处 理。当然了这些Task的处理逻辑完全是⼀致的。这⼀组Task就组成了⼀个Stage。有两 种Task：

- 1.
- 2.


org.apache.spark.scheduler.ShuffleMapTask org.apache.spark.scheduler.ResultTask

ShuffleMapTask根据Task的partitioner将计算结果放到不同的bucket中。⽽ResultTask 将计算结果发送回Driver Application。⼀个Job包含了多个Stage，⽽Stage是由⼀组完 全相同的Task组成的。最后的Stage包含了⼀组ResultTask。

在⽤户触发了⼀个action后，⽐如count，collect，SparkContext会通过runJob的函数开 始进⾏任务提交。最后会通过DAG的event processor 传递到DAGScheduler本身的 handleJobSubmitted，它⾸先会划分Stage，提交Stage，提交Task。⾄此，Task就开 始在运⾏在集群上了。 ⼀个Stage的开始就是从外部存储或者shuffle结果中读取数据；⼀个Stage的结束就是 由于发⽣shuffle或者⽣成结果时。

### 创建finalStage handleJobSubmitted 通过调⽤newStage来创建finalStage：

[java] view plaincopy

![image 2](assets/imageFile2.png)

<table>
  <tr>
    <th>![image 3](assets/imageFile3.png)</th>
  </tr>
</table>


finalStage = newStage(finalRDD, partitions.size, None, jobId, callSite)

创建⼀个result stage，或者说finalStage，是通过调⽤ org.apache.spark.scheduler.DAGScheduler#newStage完成的；⽽创建⼀个shuffle stage，需要通过调⽤ org.apache.spark.scheduler.DAGScheduler#newOrUsedStage。

[java] view plaincopy

![image 4](assets/imageFile4.png)

<table>
  <tr>
    <th>![image 5](assets/imageFile5.png)</th>
  </tr>
</table>


private def newStage(

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


rdd: RDD[_],

numTasks: Int,

shuffleDep: Option[ShuffleDependency[_, _, _]],

jobId: Int,

callSite: CallSite)

: Stage =

{

val id = nextStageId.getAndIncrement()

val stage =

new Stage(id, rdd, numTasks, shuffleDep, getParentStages(rdd, jobId), jobId, callSite )

stageIdToStage(id) = stage

updateJobIdStageIdMaps(jobId, stage)

stage

}

对于result 的final stage来说，传⼊的shuffleDep是None。 我们知道，RDD通过org.apache.spark.rdd.RDD#getDependencies可以获得它依赖的 parent RDD。⽽Stage也可能会有parent Stage。看⼀个RDD论⽂的Stage划分吧：

![image 6](assets/imageFile6.png)

⼀个stage的边界，输⼊是外部的存储或者⼀个stage shuffle的结果；输⼊则是Job的结 果（result task对应的stage）或者shuffle的结果。

上图的话stage3的输⼊则是RDD A和RDD F shuffle的结果。⽽A和F由于到B和G需要 shuffle，因此需要划分到不同的stage。

从源码实现的⻆度来看，通过触发action也就是最后⼀个RDD创建final stage（上图的 stage 3），我们注意到new Stage的第五个参数就是该Stage的parent Stage：通过rdd 和job id获取：

[java] view plaincopy

![image 7](assets/imageFile7.png)

<table>
  <tr>
    <th>![image 8](assets/imageFile8.png)</th>
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


// ⽣成rdd的parent Stage。没遇到⼀个ShuffleDependency，就会⽣成⼀个Stage

private def getParentStages(rdd: RDD[_], jobId: Int): List[Stage] = {

val parents = new HashSet[Stage] //存储parent stage

val visited = new HashSet[RDD[_]] //存储已经被访问到得RDD

// We are manually maintaining a stack here to prevent StackOverflowError

// caused by recursively visiting // 存储需要被处理的RDD。Stack中得RDD都需要被处理。

val waitingForVisit = new Stack[RDD[_]]

def visit(r: RDD[_]) {

if (!visited(r)) {

visited += r

// Kind of ugly: need to register RDDs with the cache here since

// we can't do it in its constructor because # of partitions is unknown

for (dep <- r.dependencies) {

dep match {

case shufDep: ShuffleDependency[_, _, _] => // 在ShuffleDependency时需要⽣成新的 stage

parents += getShuffleMapStage(shufDep, jobId)

#### case _ =>

waitingForVisit.push(dep.rdd) //不是ShuffleDependency，那么就属于同⼀个Stage

- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.


}

}

}

}

waitingForVisit.push(rdd) // 输⼊的rdd作为第⼀个需要处理的RDD。然后从该rdd开始，顺序访问其 parent rdd

while (!waitingForVisit.isEmpty) { //只要stack不为空，则⼀直处理。

visit(waitingForVisit.pop()) //每次visit如果遇到了ShuffleDependency，那么就会形成⼀个 Stage，否则这些RDD属于同⼀个Stage

}

parents.toList

}

### ⽣成了finalStage后，就需要提交Stage了。

[java] view plaincopy

![image 9](assets/imageFile9.png)

<table>
  <tr>
    <th>![image 10](assets/imageFile10.png)</th>
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


// 提交Stage，如果有parent Stage没有提交，那么递归提交它。

private def submitStage(stage: Stage) {

val jobId = activeJobForStage(stage)

if (jobId.isDefined) {

logDebug("submitStage(" + stage + ")")

// 如果当前stage不在等待其parent stage的返回，并且 不在运⾏的状态， 并且 没有已经失败（失败会有重试 机制，不会通过这⾥再次提交）

if (!waitingStages(stage) && !runningStages(stage) && !failedStages(stage)) {

val missing = getMissingParentStages(stage).sortBy(_.id)

logDebug("missing: " + missing)

if (missing == Nil) { // 如果所有的parent stage都已经完成，那么提交该stage所包含的task

logInfo("Submitting " + stage + " (" + stage.rdd + "), which has no missing parents ")

submitMissingTasks(stage, jobId.get)

#### } else {

for (parent <- missing) { // 有parent stage为完成，则递归提交它

submitStage(parent)

}

waitingStages += stage

}

- 19.
- 20.
- 21.
- 22.
- 23.


}

#### } else {

abortStage(stage, "No active job for stage " + stage.id)

}

}

DAGScheduler将Stage划分完成后，提交实际上是通过把Stage转换为TaskSet，然后 通过TaskScheduler将计算任务最终提交到集群。其所在的位置如下图所示。

![image 11](assets/imageFile11.png)

接下来，将分析Stage是如何转换为TaskSet，并最终提交到Executor去运⾏的。

BTW，最近⼯作太忙了，基本上到家洗漱完都要10点多。也再没有精⼒去进⾏源码解 析了。幸运的是周末不⽤加班。因此以后的博⽂更新都要集中在周末了。加油。
