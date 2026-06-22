# http://www.cnblogs.com/ggjucheng/archive/20 13/02/20/2917799.html

MapReduce详细流程.vsd 16.5KB

# ⼀ MapReduce概述

Map/Reduce是⼀个⽤于⼤规模数据处理的分布式计算模型，它最初是由Google⼯程师设计并实现的，Google已经 将它完整的MapReduce论⽂公开发布了。其中对它的定义是，Map/Reduce是⼀个编程模型 （programmingmodel），是⼀个⽤于处理和⽣成⼤规模数据集（processing and generating large data sets） 的相关的实现。⽤户定义⼀个map函数来处理⼀个key/value对以⽣成⼀批中间的key/value对，再定义⼀个reduce函 数将所有这些中间的有着相同key的values合并起来。很多现实世界中的任务都可⽤这个模型来表达。

# ⼆ MapReduce⼯作原理

Map-Reduce框架的运作完全基于<key,value>对，即数据的输⼊是⼀批<key,value>对，⽣成的结果也是⼀批

<key,value>对，只是有时候它们的类型不⼀样⽽已。Key和value的类由于需要⽀持被序列化（serialize）操作，所 以它们必须要实现Writable接⼝，⽽且key的类还必须实现WritableComparable接⼝，使得可以让框架对数据集的执 ⾏排序操作。

⼀个Map-Reduce任务的执⾏过程以及数据输⼊输出的类型如下所示：

Map：<k1,v1> ->list<k2,v2>

Reduce：<k2,list<v2>> -><k3,v3>

下⾯通过⼀个的例⼦来详细说明这个过程。

WordCount是Hadoop⾃带的⼀个例⼦，⽬标是统计⽂本⽂件中单词的个数。假设有如下的两个⽂本⽂件来运⾏ WorkCount程序：

Hello World Bye World

Hello Hadoop GoodBye Hadoop

- 1 map数据输⼊


Hadoop针对⽂本⽂件缺省使⽤LineRecordReader类来实现读取，⼀⾏⼀个key/value对，key取偏移量，value为 ⾏内容。

如下是map1的输⼊数据：

<table>
  <tr>
    <th>Key1</th>
    <th>Value1</th>
  </tr>
  <tr>
    <td>0</td>
    <td>Helo World Bye World</td>
  </tr>
</table>


如下是map2的输⼊数据：

<table>
  <tr>
    <th>Key1</th>
    <th>Value1</th>
  </tr>
  <tr>
    <td>0</td>
    <td>Helo Hadop GodBye Hadop</td>
  </tr>
</table>


## 2 map输出/combine输⼊

- 如下是map1的输出结果

<table>
  <tr>
    <th>Key2</th>
    <th>Value2</th>
  </tr>
  <tr>
    <td>Helo</td>
    <td>1</td>
  </tr>
  <tr>
    <td>World</td>
    <td>1</td>
  </tr>
  <tr>
    <td>Bye</td>
    <td>1</td>
  </tr>
  <tr>
    <td>World</td>
    <td>1</td>
  </tr>
</table>


- 如下是map2的输出结果


<table>
  <tr>
    <th>Key2</th>
    <th>Value2</th>
  </tr>
  <tr>
    <td>Helo</td>
    <td>1</td>
  </tr>
  <tr>
    <td>Hadop</td>
    <td>1</td>
  </tr>
  <tr>
    <td>GodBye</td>
    <td>1</td>
  </tr>
  <tr>
    <td>Hadop</td>
    <td>1</td>
  </tr>
</table>


## 3 combine输出

Combiner类实现将相同key的值合并起来，它也是⼀个Reducer的实现。

- 如下是combine1的输出


<table>
  <tr>
    <th>Key2</th>
    <th>Value2</th>
  </tr>
  <tr>
    <td>Helo</td>
    <td>1</td>
  </tr>
  <tr>
    <td>World</td>
    <td>2</td>
  </tr>
  <tr>
    <td>Bye</td>
    <td>1</td>
  </tr>
</table>


- 如下是combine2的输出


<table>
  <tr>
    <th>Key2</th>
    <th>Value2</th>
  </tr>
  <tr>
    <td>Helo</td>
    <td>1</td>
  </tr>
  <tr>
    <td>Hadop</td>
    <td>2</td>
  </tr>
  <tr>
    <td>GodBye</td>
    <td>1</td>
  </tr>
</table>


## 4 reduce输出

Reducer类实现将相同key的值合并起来。

如下是reduce的输出

<table>
  <tr>
    <th>Key2</th>
    <th>Value2</th>
  </tr>
  <tr>
    <td>Helo</td>
    <td>2</td>
  </tr>
  <tr>
    <td>World</td>
    <td>2</td>
  </tr>
  <tr>
    <td>Bye</td>
    <td>1</td>
  </tr>
  <tr>
    <td>Hadop</td>
    <td>2</td>
  </tr>
  <tr>
    <td>GodBye</td>
    <td>1</td>
  </tr>
</table>


![image 1](<MapReduce源码分析总结(转).note_images/imageFile1.png>)

# 三 MapReduce框架结构

- 1 ⻆⾊

- 1.1 JobTracker

JobTracker是⼀个master服务， JobTracker负责调度job的每⼀个⼦任务task运⾏于TaskTracker上，并监控它 们，如果发现有失败的task就重新运⾏它。⼀般情况应该把JobTracker部署在单独的机器上。

- 1.2 TaskTracker

TaskTracker是 运⾏于多个节点上的slaver服务。TaskTracker则负责直接执⾏每⼀个task。TaskTracker都需要 运⾏在HDFS的DataNode上，

- 1.3 JobClient


每⼀个job都会在⽤户端通过JobClient类将应⽤程序以及配置参数打包成jar⽂件存储在HDFS，并把路径提交到 JobTracker，然后由JobTracker创建每⼀个Task（即MapTask和ReduceTask）并将它们分发到各个TaskTracker服 务中去执⾏。

- 2 数据结构


2.1 Mapper和Reducer

运⾏于Hadoop的MapReduce应⽤程序最基本的组成部分包括⼀个Mapper和⼀个Reducer类，以及⼀个创建 JobConf的执⾏程序，在⼀些应⽤中还可以包括⼀个Combiner类，它实际也是Reducer的实现。

#### 2.2 JobInProgress

JobClient提交job后，JobTracker会创建⼀个JobInProgress来跟踪和调度这个job，并把它添加到job队列⾥。 JobInProgress会根据提交的job jar中定义的输⼊数据集（已分解成FileSplit）创建对应的⼀批TaskInProgress⽤于 监控和调度MapTask，同时在创建指定数⽬的TaskInProgress⽤于监控和调度ReduceTask，缺省为1个 ReduceTask。

#### 2.3 TaskInProgress

JobTracker启动任务时通过每⼀个TaskInProgress来launchTask，这时会把Task对象（即MapTask和 ReduceTask）序列化写⼊相应的TaskTracker服务中，TaskTracker收到后会创建对应的TaskInProgress（此 TaskInProgress实现⾮JobTracker中使⽤的TaskInProgress，作⽤类似）⽤于监控和调度该Task。启动具体的Task 进程是通过TaskInProgress管理的TaskRunner对象来运⾏的。TaskRunner会⾃动装载jobjar，并设置好环境变量后 启动⼀个独⽴的java child进程来执⾏Task，即MapTask或者ReduceTask，但它们不⼀定运⾏在同⼀个TaskTracker 中。

#### 2.4 MapTask和ReduceTask

⼀个完整的job会⾃动依次执⾏Mapper、Combiner（在JobConf指定了Combiner时执⾏）和Reducer，其中 Mapper和Combiner是由MapTask调⽤执⾏，Reducer则由ReduceTask调⽤，Combiner实际也是Reducer接⼝类 的实现。Mapper会根据jobjar中定义的输⼊数据集按<key1,value1>对读⼊，处理完成⽣成临时的<key2,value2> 对，如果定义了Combiner，MapTask会在Mapper完成调⽤该Combiner将相同key的值做合并处理，以减少输出结果 集。MapTask的任务全完成即交给ReduceTask进程调⽤Reducer处理，⽣成最终结果<key3,value3>对。这个过程 在下⼀部分再详细介绍。

下图描述了Map/Reduce框架中主要组成和它们之间的关系：

![image 2](<MapReduce源码分析总结(转).note_images/imageFile2.png>)

### 3 流程

⼀道MapRedcue作业是通过JobClient.rubJob(job)向master节点的JobTracker提交的, JobTracker接到

JobClient的请求后把其加⼊作业队列中。JobTracker⼀直在等待JobClient通过RPC提交作业,⽽TaskTracker⼀直通过 RPC向JobTracker发送⼼跳heartbeat询问有没有任务可做，如果有，让其派发任务给它执⾏。如果JobTracker的作业 队列不为空, 则TaskTracker发送的⼼跳将会获得JobTracker给它派发的任务。这是⼀道pull过程。slave节点的

TaskTracker接到任务后在其本地发起Task,执⾏任务。以下是简略示意图：

![image 3](<MapReduce源码分析总结(转).note_images/imageFile3.png>)

下⾯详细介绍⼀下Map/Reduce处理⼀个⼯作的流程。

# 四JobClient

在编写MapReduce程序时通常是上是这样写的:

![image 4](<MapReduce源码分析总结(转).note_images/imageFile4.png>)

Configuration conf = new Configuration();// 读取hadoop配置 Job job = new Job(conf, "作业名称"); // 实例化⼀道作业 job.setMapperClass(Mapper类型); job.setCombinerClass(Combiner类型); job.setReducerClass(Reducer类型); job.setOutputKeyClass(输出Key的类型); job.setOutputValueClass(输出Value的类型); FileInputFormat.addInputPath(job, new Path(输⼊hdfs路径)); FileOutputFormat.setOutputPath(job, newPath(输出hdfs路径)); // 其它初始化配置 JobClient.runJob(job);

![image 5](<MapReduce源码分析总结(转).note_images/imageFile5.png>)

## 1配置Job

JobConf是⽤户描述⼀个job的接⼝。下⾯的信息是MapReduce过程中⼀些较关键的定制信息：

![image 6](<MapReduce源码分析总结(转).note_images/imageFile6.png>)

#### 2 JobClient.runJob()：运⾏Job并分解输⼊数据集

⼀个MapReduce的Job会通过JobClient类根据⽤户在JobConf类中定义的InputFormat实现类来将输⼊的数据集分 解成⼀批⼩的数据集，每⼀个⼩数据集会对应创建⼀个MapTask来处理。JobClient会使⽤缺省的FileInputFormat类 调⽤FileInputFormat.getSplits()⽅法⽣成⼩数据集，如果判断数据⽂件是isSplitable()的话，会将⼤的⽂件分解成⼩ 的FileSplit，当然只是记录⽂件在HDFS⾥的路径及偏移量和Split⼤⼩。这些信息会统⼀打包到jobFile的jar中。

JobClient然后使⽤submitJob(job)⽅法向 master提交作业。submitJob(job)内部是通过 submitJobInternal(job)⽅法完成实质性的作业提交。 submitJobInternal(job)⽅法⾸先会向hadoop分布系统⽂件 系统hdfs依次上传三个⽂件: job.jar, job.split和job.xml。

job.xml: 作业配置，例如Mapper,Combiner, Reducer的类型，输⼊输出格式的类型等。

job.jar: jar包,⾥⾯包含了执⾏此任务需要的各种类，⽐如 Mapper,Reducer等实现。

job.split: ⽂件分块的相关信息，⽐如有数据分多少个块，块的⼤⼩(默认64m)等。

这三个⽂件在hdfs上的路径由hadoop-default.xml⽂件中的mapreduce系统路径mapred.system.dir属性 + jobid决定。mapred.system.dir属性默认是/tmp/hadoop-user_name/mapred/system。写完这三个⽂ 件之 后, 此⽅法会通过RPC调⽤master节点上的JobTracker.submitJob(job)⽅法，此时作业已经提交完成。

## 3提交Job

jobFile的提交过程是通过RPC模块（有单独⼀章来详细介绍）来实现的。⼤致过程是，JobClient类中通过RPC实现 的Proxy接⼝调⽤JobTracker的submitJob()⽅法，⽽JobTracker必须实现JobSubmissionProtocol接⼝。

JobTracker创建job成功后会给JobClient传回⼀个JobStatus对象⽤于记录job的状态信息，如执⾏时间、Map和 Reduce任务完成的⽐例等。JobClient会根据这个JobStatus对象创建⼀个NetworkedJob的RunningJob对象，⽤于 定时从JobTracker获得执⾏过程的统计数据来监控并打印到⽤户的控制台。

与创建Job过程相关的类和⽅法如下图所示

![image 7](<MapReduce源码分析总结(转).note_images/imageFile7.png>)

# 五 JobTracker

上⾯已经提到，job是统⼀由JobTracker来调度的，具体的Task分发给各个TaskTracker节点来执⾏。下⾯来详细 解析执⾏过程，⾸先先从JobTracker收到JobClient的提交请求开始。

- 1JobTracker初始化Job


1.1JobTracker.submitJob() 收到请求

当JobTracker接收到新的job请求（即submitJob()函数被调⽤）后，会创建⼀个JobInProgress对象并通过它来管

理和调度任务。JobInProgress在创建的时候会初始化⼀系列与任务有关的参数，调⽤到FileSystem，把在JobClient 端上传的所有任务⽂件下载到本地的⽂件系统中的临时⽬录⾥。这其中包括上传的*.jar⽂件包、记录配置信息的xml、 记录分割信息的⽂件。

#### 1.2JobTracker.JobInitThread 通知初始化线程

JobTracker 中的监听器类EagerTaskInitializationListener负责任务Task的初始化。JobTracker使⽤ jobAdded(job)加⼊job到EagerTaskInitializationListener中 ⼀ 个专 ⻔ 管 理 需 要 初 始化的队列⾥，即⼀个list成员变量 jobInitQueue⾥。resortInitQueue⽅法根据作业的优先级排序。然后调⽤notifyAll()函数，会唤起⼀个⽤于初始化 job的线程JobInitThread来处理。JobInitThread收到信号后即取出最靠前的job，即优先级别最⾼的job，调⽤ TaskTrackerManager的initJob最终调⽤JobInProgress.initTasks()执⾏真正的初始化⼯作。

#### 1.3JobInProgress.initTasks() 初始化TaskInProgress

任务Task分两种: MapTask 和reduceTask，它们的管理对象都是TaskInProgress 。

⾸先JobInProgress会创建Map的监控对象。在initTasks()函数⾥通过调⽤JobClient的readSplitFile()获得已分解 的输⼊数据的RawSplit列表，然后根据这个列表创建对应数⽬的Map执⾏管理对象TaskInProgress。在这个过程中， 还会记录该RawSplit块对应的所有在HDFS⾥的blocks所在的DataNode节点的host，这个会在RawSplit创建时通过 FileSplit的getLocations()函数获取，该函数会调⽤DistributedFileSystem的getFileCacheHints()获得（这个细节 会在HDFS中讲解）。当然如果是存储在本地⽂件系统中，即使⽤LocalFileSystem时当然只有⼀个location即 “localhost”了。

创建这些TaskInProgress对象完毕后，initTasks()⽅法会通过createCache()⽅法为这些TaskInProgress对象产 ⽣⼀个未执⾏任务的Map缓存nonRunningMapCache。slave端的TaskTracker向master发送⼼跳时，就可以直接从 这个cache中取任务去执⾏。

其次JobInProgress会创建Reduce的监控对象，这个⽐较简单，根据JobConf⾥指定的Reduce数⽬创建，缺省只创 建1个Reduce任务。监控和调度Reduce任务的是TaskInProgress类，不过构造⽅法有所不同，TaskInProgress会根 据不同参数分别创建具体的MapTask或者ReduceTask。同样地，initTasks()也会通过createCache()⽅法产⽣ nonRunningReduceCache成员。

JobInProgress创建完TaskInProgress后，最后构造JobStatus并记录job正在执⾏中，然后再调⽤ JobHistory.JobInfo.logStarted()记录job的执⾏⽇志。到这⾥JobTracker⾥初始化job的过程全部结束。

![image 8](<MapReduce源码分析总结(转).note_images/imageFile8.png>)

#### 2 JobTracker调度Job

hadoop默 认 的 调 度 器 是 FIFO策 略 的 JobQueueTaskScheduler,它 有 两个 成 员变 量 jobQueueJobInProgressListener与上 ⾯ 说 的 eagerTaskInitializationListener。 JobQueueJobInProgressListener是 JobTracker的 另 ⼀ 个 监 听 器 类 ， 它 包 含 了 ⼀ 个 映 射 ， ⽤ 来 管 理 和 调 度 所 有 的 JobInProgress。jobAdded(job)同 时 会 加 ⼊ job到 JobQueueJobInProgressListener中 的 映 射 。

JobQueueTaskScheduler最重要的⽅法是assignTasks，他实现了⼯作调度。具体实现：JobTracker 接到 TaskTracker的heartbeat() 调⽤后，⾸先会检查上⼀个⼼跳响应是否完成，是没要求启动或重启任务，如果⼀切正 常，则会处理⼼跳。⾸先它会检查 TaskTracker 端还可以做多少个 map 和 reduce 任务，将要派发的任务数是否超出 这个数，是否超出集群的任务平均剩余可负载数。如果都没超出，则为此TaskTracker 分配⼀个 MapTask 或 ReduceTask 。产⽣ Map 任务使⽤ JobInProgress 的obtainNewMapTask() ⽅法，实质上最后调⽤了 JobInProgress 的 findNewMapTask() 访问nonRunningMapCache 。

上⾯讲解任务初始化时说过，createCache()⽅法会在⽹络拓扑结构上挂上需要执⾏的TaskInProgress。 findNewMapTask()从近到远⼀层⼀层地寻找，⾸先是同⼀节点，然后在寻找同⼀机柜上的节点，接着寻找相同数据中 ⼼下的节点，直到找了maxLevel层结束。这样的话，在JobTracker给TaskTracker派发任务的时候，可以迅速找到最 近的TaskTracker，让它执⾏任务。

最终⽣成⼀个Task类对象，该对象被封装在⼀个LanuchTaskAction中，发回给TaskTracker，让它去执⾏任务。

产⽣ Reduce 任务过程类似，使⽤JobInProgress.obtainNewReduceTask() ⽅法，实质上最后调⽤了 JobInProgress的 findNewReduceTask() 访问 nonRuningReduceCache。

![image 9](<MapReduce源码分析总结(转).note_images/imageFile9.png>)

# 六 TaskTracker

#### 1TaskTracker加载Task到⼦进程

Task的执⾏实际是由TaskTracker发起的，TaskTracker会定期（缺省为10秒钟，参⻅MRConstants类中定义的 HEARTBEAT_INTERVAL变量）与JobTracker进⾏⼀次通信，报告⾃⼰Task的执⾏状态，接收JobTracker的指令等。 如果发现有⾃⼰需要执⾏的新任务也会在这时启动，即是在TaskTracker调⽤JobTracker的heartbeat()⽅法时进⾏， 此调⽤底层是通过IPC层调⽤Proxy接⼝实现。下⾯⼀⼀简单介绍下每个步骤。

#### 1.1TaskTracker.run() 连接JobTracker

TaskTracker的启动过程会初始化⼀系列参数和服务，然后尝试连接JobTracker（即必须实现 InterTrackerProtocol接⼝），如果连接断开，则会循环尝试连接JobTracker，并重新初始化所有成员和参数。

#### 1.2TaskTracker.offerService() 主循环

如果连接JobTracker服务成功，TaskTracker就会调⽤offerService()函数进⼊主执⾏循环中。这个循环会每隔10 秒与JobTracker通讯⼀次，调⽤transmitHeartBeat()，获得HeartbeatResponse信息。然后调⽤

HeartbeatResponse的getActions()函数获得JobTracker传过来的所有指令即⼀个TaskTrackerAction数组。再遍历 这个数组，如果是⼀个新任务指令即LaunchTaskAction则调⽤调⽤addToTaskQueue加⼊到待执⾏队列，否则加⼊到 tasksToCleanup队列，交给⼀个taskCleanupThread线程来处理，如执⾏KillJobAction或者KillTaskAction等。

#### 1.3TaskTracker.transmitHeartBeat() 获取JobTracker指令

在transmitHeartBeat()函数处理中，TaskTracker会创建⼀个新的TaskTrackerStatus对象记录⽬前任务的执⾏状 况，检查⽬前执⾏的Task数⽬以及本地磁盘的空间使⽤情况等，如果可以接收新的Task则设置heartbeat()的 askForNewTask参数为true。然后通过IPC接⼝调⽤JobTracker的heartbeat()⽅法发送过去，heartbeat()返回值 TaskTrackerAction数组。

#### 1.4 TaskTracker.addToTaskQueue，交给TaskLauncher处理

TaskLauncher是⽤来处理新任务的线程类，包含了⼀个待运⾏任务的队列 tasksToLaunch。 TaskTracker.addToTaskQueue会调⽤TaskTracker的registerTask，创建TaskInProgress对象来调度和监控任务， 并把它加⼊到runningTasks队列中。同时将这个TaskInProgress加到tasksToLaunch中，并notifyAll()唤醒⼀个线程 运⾏，该线程从队列tasksToLaunch取出⼀个待运⾏任务，调⽤TaskTracker的startNewTask运⾏任务。

#### 1.5 TaskTracker.startNewTask() 启动新任务

调⽤localizeJob()真正初始化Task并开始执⾏。

#### 1.6 TaskTracker.localizeJob() 初始化job⽬录等

此函数主要任务是初始化⼯作⽬录workDir，再将job jar包从HDFS复制到本地⽂件系统中，调⽤RunJar.unJar()将 包解压到⼯作⽬录。然后创建⼀个RunningJob并调⽤addTaskToJob()函数将它添加到runningJobs监控队列中。 addTaskToJob⽅法把⼀个任务加⼊到该任务属于的runningJob的tasks列表中。如果该任务属于的runningJob不存 在，先新建，加到runningJobs中。完成后即调⽤launchTaskForJob()开始执⾏Task。

#### 1.7 TaskTracker.launchTaskForJob()执⾏任务

启动Task的⼯作实际是调⽤TaskTracker$TaskInProgress的launchTask()函数来执⾏的。

#### 1.8 TaskTracker$TaskInProgress.launchTask()执⾏任务

执⾏任务前先调⽤localizeTask()更新⼀下jobConf⽂件并写⼊到本地⽬录中。然后通过调⽤Task的 createRunner()⽅法创建TaskRunner对象并调⽤其start()⽅法最后启动Task独⽴的java执⾏⼦进程。

#### 1.9 Task.createRunner()创建启动Runner对象

Task有两个实现版本，即MapTask和ReduceTask，它们分别⽤于创建Map和Reduce任务。MapTask会创建 MapTaskRunner来启动Task⼦进程，⽽ReduceTask则创建ReduceTaskRunner来启动。

#### 1.10 TaskRunner.start()启动⼦进程

TaskRunner负责将⼀个任务放到⼀个进程⾥⾯来执⾏。它会调⽤run()函数来处理，主要的⼯作就是初始化启动 java⼦进程的⼀系列环境变量，包括设定⼯作⽬录workDir，设置CLASSPATH环境变量等。然后装载job jar包。 JvmManager⽤于管理该TaskTracker上所有运⾏的Task⼦进程。每⼀个进程都是由JvmRunner来管理的，它也是位 于单独线程中的。JvmManager的launchJvm⽅法，根据任务是map还是reduce,⽣成对应的JvmRunner并放到对应 JvmManagerForType的进程容器中进⾏管理。JvmManagerForType的reapJvm()

分配⼀个新的JVM进程。如果JvmManagerForType槽满，就寻找idle的进程，如果是同Job的直接放进去，否则杀死这 个进程，⽤⼀个新的进程代替。 如果槽没有满，那么就启动新的⼦进程。⽣成新的进程使⽤spawnNewJvm⽅法。 spawnNewJvm使⽤JvmRunner线程的run⽅法，run⽅法⽤于⽣成⼀个新的进程并运⾏它，具体实现是调⽤ runChild。

#### 2 ⼦进程执⾏MapTask

真实的执⾏载体，是Child，它包含⼀个 main函数，进程执⾏，会将相关参数传进来，它会拆解这些参数，通过 getTask(jvmId)向⽗进程索取任务，并且构造出相关的Task实例，然后使⽤Task的run()启动任务。

2.1 run

⽅法相当简单，配置完系统的TaskReporter后，就根据情况执⾏runJobCleanupTask，runJobSetupTask， runTaskCleanupTask或执⾏Mapper。由于MapReduce现在有两套API，MapTask需要⽀持这两套API，使得 MapTask执⾏Mapper分为runNewMapper和runOldMapper，我们分析runOldMapper。

2.2 runOldMapper

runOldMapper最开始部分是构造Mapper处理的InputSplit，然后就开始创建Mapper的RecordReader，最终得 到map的输⼊。之后构造Mapper的输出，是通过MapOutputCollector进⾏的，也分两种情况，如果没有Reducer， 那么，⽤DirectMapOutputCollector，否则，⽤MapOutputBuffer。

构造完Mapper的输⼊输出，通过构造配置⽂件中配置的MapRunnable，就可以执⾏Mapper了。⽬前系统有两个 MapRunnable：MapRunner和MultithreadedMapRunner。MapRunner是单线程执⾏器，⽐较简单，他会使⽤反射 机制⽣成⽤户定义的Mapper接⼝实现类，作为他的⼀个成员。

2.3 MapRunner的run⽅法

会先创建对应的key，value对象，然后，对InputSplit的每⼀对<key，value>，调⽤⽤户实现的Mapper接⼝实现 类的map⽅法，每处理⼀个数据对，就要使⽤OutputCollector收集每次处理kv对后得到的新的kv对，把他们spill到⽂ 件或者放到内存，以做进⼀步的处理，⽐如排序，combine等。

2.4 OutputCollector

OutputCollector的作⽤是收集每次调⽤map后得到的新的kv对，宁把他们spill到⽂件或者放到内存，以做进⼀步的 处理，⽐如排序，combine等。

MapOutputCollector 有两个⼦类：MapOutputBuffer和 DirectMapOutputCollector。 DirectMapOutputCollector⽤在不需要Reduce阶段的时候。如果Mapper后续有 reduce任务，系统会使⽤MapOutputBuffer做为输出，MapOutputBuffer使⽤了⼀个缓冲区对map的处理结果进⾏缓 存，放在内存中，⼜使⽤⼏个数组对这个缓冲区进⾏管理。

![image 10](<MapReduce源码分析总结(转).note_images/imageFile10.png>)

在适当的时机，缓冲区中的数据会被spill到硬盘中。

![image 11](<MapReduce源码分析总结(转).note_images/imageFile11.png>)

向硬盘中写数据的时机:

- （1）当内存缓冲区不能容下⼀个太⼤的kv对时。spillSingleRecord⽅法。

- （2）内存缓冲区已满时。SpillThread线程。

- （3）Mapper的结果都已经collect了，需要对缓冲区做最后的清理。Flush⽅法。


2.5 spillThread线程：将缓冲区中的数据spill到硬盘中。

- （1）需要spill时调⽤函数sortAndSpill，按照partition和key做排序。默认使⽤的是快速排序QuickSort。

- （2）如果没有combiner，则直接输出记录，否则，调⽤CombinerRunner的combine，先做combin然后输出。


#### 3 ⼦进程执⾏ReduceTask

ReduceTask.run⽅法开始和MapTask类似，包括initialize()初始化，runJobCleanupTask()， runJobSetupTask()，runTaskCleanupTask()。之后进⼊正式的⼯作，主要有这么三个步骤：Copy、Sort、 Reduce。

##### 3.1 Copy

就是从执⾏各个Map任务的服务器那⾥，收罗到map的输出⽂件。拷⻉的任务，是由ReduceTask.ReduceCopier类 来负责。

- 3.1.1 类图:


![image 12](<MapReduce源码分析总结(转).note_images/imageFile12.png>)

- 3.1.2 流程: 使⽤ReduceCopier.fetchOutputs开始


（1）索取任务。使⽤GetMapEventsThread线程。该线程的run⽅法不停的调⽤getMapCompletionEvents⽅ 法，该⽅法⼜使⽤RPC调⽤TaskUmbilicalProtocol协议的getMapCompletionEvents，⽅法使⽤所属的jobID向其⽗ TaskTracker询问此作业个Map任务的完成状况（TaskTracker要向JobTracker询问后再转告给它...）。返回⼀个数组 TaskCompletionEventevents[]。TaskCompletionEvent包含taskid和ip地址之类的信息。 （2）当获取到相关Map

任务执⾏服务器的信息后，有⼀个线程MapOutputCopier开启，做具体的拷⻉⼯作。 它会在⼀个单独的线程内，负责 某个Map任务服务器上⽂件的拷⻉⼯作。MapOutputCopier的run循环调⽤copyOutput，copyOutput⼜调⽤ getMapOutput，使⽤HTTP远程拷⻉。

- （3）getMapOutput远程拷⻉过来的内容（当然也可以是本地了...），作为MapOutput对象存在，它可以在内存

中也可以序列化在磁盘上，这个根据内存使⽤状况来⾃动调节。

- （4） 同时，还有⼀个内存Merger线程InMemFSMergeThread和⼀个⽂件Merger线程LocalFSMerger在同步⼯


作，它们将下载过来的⽂件（可能在内存中，简单的统称为⽂件...），做着归并排序，以此，节约时间，降低输⼊⽂件 的数量，为后续的排序⼯作减 负。InMemFSMergeThread的run循环调⽤doInMemMerge，该⽅法使⽤⼯具类 Merger实现归并，如果需要combine，则combinerRunner.combine。

##### 3.2 Sort

排序⼯作，就相当于上述排序⼯作的⼀个延续。它会在所有的⽂件都拷⻉完毕后进⾏。使⽤⼯具类Merger归并所有 的⽂件。经过这⼀个流程，⼀个合并了所有所需Map任务输出⽂件的新⽂件产⽣了。⽽那些从其他各个服务器⽹罗过来 的 Map任务输出⽂件，全部删除了。

##### 3.3Reduce

Reduce任务的最后⼀个阶段。他会准备好keyClass （"mapred.output.key.class"或"mapred.mapoutput.key.class"）,valueClass("mapred.mapoutput.value.cla ss"或"mapred.output.value.class")和Comparator（“mapred.output.value.groupfn.class”或 “mapred.output.key.comparator.class”）。最后调⽤runOldReducer⽅法。（也是两套API，我们分析 runOldReducer）

3.3.1 runOldReducer

- （1）输出⽅⾯。

它会准备⼀个OutputCollector收集输出，与MapTask不同，这个OutputCollector更为简单，仅仅是打开⼀个 RecordWriter，collect⼀次，write⼀次。最⼤的不同在于，这次传⼊RecordWriter的⽂件系统，基本都是分布式⽂件 系统，或者说是HDFS。

- （2）输⼊⽅⾯，ReduceTask会⽤准备好的KeyClass、ValueClass、KeyComparator等等之类的⾃定义类，构造


出Reducer所需的键类型，和值的迭代类型Iterator（⼀个键到了这⾥⼀般是对应⼀组值）。

###### （3）有了输⼊，有了输出，不断循环调⽤⾃定义的Reducer，最终，Reduce阶段完成。

![image 13](<MapReduce源码分析总结(转).note_images/imageFile13.png>)

