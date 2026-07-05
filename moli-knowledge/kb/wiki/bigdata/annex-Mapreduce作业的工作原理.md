---
title: Mapreduce作业的工作原理.note（原文插图 annex）
slug: annex-Mapreduce作业的工作原理
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hadoop/Mapreduce作业的工作原理.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

![image 1](assets/imageFile1.png)

JobClient的runJob()⽅法是⽤于新建JobClient实例和调⽤其submitJob()⽅法。提交作业后，runJob （）将每秒轮询作业的进度，如果发现与上⼀个记录不同，便把报告显示到控制台。作业完成后，如 果成功，就显示作业计数器。否则，导致作业失败的错误会被记录到控制台。

- 1、向jobtracker请求⼀个新的作业ID（通过Jobtracker的getNewJobId（））
- 2、检查作业的输出说明。⽐如，如果没有指定输出⽬录或者它已经存在，作业就不会被提交，并有错 误返回给MapReduce程序。
- 3、计算作业的输出划分。如果划分⽆法计算，⽐如因为输⼊路径不存在，作业就不会被提交，并有错 误返回给MapReduce程序。
- 4、将运⾏作业所需要的资源 -包括作业的JAR⽂件、配置⽂件和计算所得的输⼊划分 -复制到⼀个以 作业ID号命名的⽬录中jobtracker的⽂件系统。作业JAR的副本较多（由mapred.submit.replication 属 性控制，默认为10），如此⼀来，在tasktracker运⾏作业任务时，集群能为它们提供许多副本进⾏访 问。（步骤3）
- 5、告诉jobtracker作业准备执⾏（通过调⽤JobTracker的submitJob()⽅法）（步骤4）


- 6、Jobtracker接受到对其submitJob（）⽅法调⽤后，会把此调⽤放⼊⼀个内部的队列中，交由作业 调度器进⾏调度，并对其进⾏初始化。初始化包括创建⼀个代表该正在运⾏的作业的对象，它封装任 务和记录信息，以便跟踪任务的状态和进程（步骤5）
- 7、要创建运⾏任务列表，作业调度器⾸先从共享⽂件系统中获取JobClient已经计算好的输⼊划分信息 （步骤6）然后为每个划分创建⼀个map任务。创建的reduce任务的数量由JobConf的 mapred.reduce.tasks属性决定，它是⽤setNumReduceTasks（）⽅法来设定的，然后调度器便创建 这么多reduce任务来运⾏。任务在此时指定ID号。
- 8、TaskTraker 执⾏⼀个简单的循环，定期发送⼼跳（heartbeat）⽅法调⽤Jobtracker。⼼跳⽅法告 诉jobtracker，tasktracker是否存活，同时也充当两者之间的消息通道。作业⼼跳⽅法调⽤的⼀部分， tasktracker会指明它是否已经准备运⾏新的任务，如果是，jobtracker会为他分配⼀个任务，并使⽤⼼ 跳⽅法的返回值与tasktracker进⾏通信（步骤7）
- 9、现在，tasktracker已经被分配了任务，下⼀步是运⾏任务。⾸先，它本地化作业的JAR⽂件，将它 从共享⽂件系统复制到tasktracker所在的⽂件系统。同时，将应⽤程序所需要的全部⽂件从分布式缓 存复制到本地磁盘。然后，为任务新建⼀个本地⼯作⽬录，并把JAR⽂件中的内容解压到这个⽂件夹 下。第三步，新建⼀个TaskRuner实例来运⾏任务。 TaskRuner启动⼀个新的Java虚拟机（步骤9）来运⾏每个任务（步骤10），使得⽤户第⼀的map和 reduce函数的任何缺陷都不会影响tasktracker（⽐如导致它崩溃或者挂起）。但在不同的任务之间重 ⽤JVM还是可能的。 ⼦进程通过 umbilical 接⼝与⽗进程进⾏通信。它每隔⼏秒便告知⽗进程它的进度，直到任务完成。
