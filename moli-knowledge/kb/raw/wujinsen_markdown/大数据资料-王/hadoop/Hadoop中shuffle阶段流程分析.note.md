宏观上，Hadoop每个作业要经历两个阶段：Map phase和reduce phase。对于Map phase，⼜主要包含四个⼦阶段：从磁盘上读数 据-》执⾏map函数-》combine结果-》将结果写到本地磁盘上；对于reduce phase，同样包含四个⼦阶段：从各个map task上读相 应的数据（shuffle）-》sort-》执⾏reduce函数-》将结果写到HDFS中。

（注：本⽂介绍的shufle阶段⾮常粗略，如果想了解shufle实现细节以及当前主流的优化⽅法，可阅 读我的最新书籍《Hadop技术内幕：深⼊解析MapReduce架构设计与实现原理》（购买说明）第8 章 “Task运⾏过程分析”以及第8.5.2节 “系统优化”） Hadop处理流程中的两个⼦阶段严重降低了其性能。第⼀个是map阶段产⽣的中间结果要写到磁盘 上，这样做的主要⽬的是提⾼系统的可靠性，但代价是降低了系统的性能，实际上，Hadop的改进版

–MapReduce Online去除了这个阶段，⽽采⽤其他更⾼效的⽅式提⾼系统可靠性（⻅参考资料[1]）； 另⼀个是shufle阶段采⽤HTP协议从各个map task上远程拷⻉结果，这种设计思路（远程拷⻉，协议 采⽤htp）同样降低了系统性能。实际上，Baidu公司正试图将该部分代码替换成C+代码来提⾼性能 （⻅参考资料[2]）。 本⽂⾸先着重分析shufle阶段的具体流程，然后分析了其低效的原因，最后给出了可能的改进⽅法。

如图所示，每个reduce task都会有⼀个后台进程GetMapCompletionEvents，它获取heartbeat中（从 JobTracker）传过来的已经完成的task列表，并将与该reduce task对应的数据位置信息保存到 mapLocations中，mapLocations中的数据位置信息经过滤和去重（相同的位置信息因为某种原因，可 能发过来多次）等处理后保存到集合scheduledCopies中，然后由⼏个拷⻉线程（默认为5个）通过 HTP并⾏的拷⻉数据，同时线程InMemFSMergeThread和LocalFSMerger会对拷⻉过来的数据进⾏归 并排序。 主要有两个⽅⾯影响shufle阶段的性能：（1）数据完全是远程拷⻉ （2）采⽤HTP协议进⾏数据传 输。对于第⼀个⽅⾯，如果采⽤某种策略（修改框架），让你reduce task也能有locality就好了；对于 第⼆个⽅⾯，⽤新的更快的数据传输协议替换HTP，也许能更快些, 如UDT协议（⻅参考资料[3]）， 它在MapReduce的另⼀个C+开源实现Sector/Sphere（⻅参考资料[4]）中被使⽤，效果不错！

⸻—-

【参考资料】 htp:/code.gogle.com/p/hop/ htp:/wenku.baidu.com/view/825e73f0912a21614792947.html htp:/udt.sourceforge.net/ htp:/sector.sourceforge.net/

- 【1】
- 【2】
- 【3】
- 【4】 原创⽂章，转载请注明： 转载⾃ 本⽂链接地址: 作者：Dong，作者介绍： 本博客的⽂章集合:


董的博客 htp:/dongxicheng.org/mapreduce/hadop-shufle-phase/

htp:/dongxicheng.org/about/ htp:/dongxicheng.org/recomend/

