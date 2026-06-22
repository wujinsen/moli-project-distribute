- 1.下⾯哪个程序负责 HDFS 数据存储。 a)NameNode b)Jobtracker c)Datanode d)secondaryNameNode e)tasktracker 答案 C datanode
- 2.HDfS 中的 block 默认保存⼏份？ a)3份 b)2 份 c)1 份 d)不确定

- 答案 A 默认 3 份


- 3.下列哪个程序通常与 NameNode 在⼀个节点启动？ a)SecondaryNameNodeb)DataNode c)TaskTracker d)Jobtracker 答案 D
- 4.Hadoop 作者 a)MartinFowler b)Kent Beck c)Doug cutting 答案 C Doug cutting
- 5.HDFS 默认 Block Size a)32MB b)64MB c)128MB

- 答案：B

6.下列哪项通常是集群的最主要瓶颈 a)CPU b)⽹络 c)磁盘 IO d)内存

- 答案：C 磁盘 ⾸先集群的⽬的是为了节省成本，⽤廉价的 pc 机，取代⼩型机及⼤型机。⼩型机和⼤型机有什么特 点？


- 7.关于 SecondaryNameNode 哪项是正确的？ a)它是 NameNode 的热备 b)它对内存没有要求 c)它的⽬的是帮助 NameNode 合并编辑⽇志，减少 NameNode 启动时间 d)SecondaryNameNode应与 NameNode 部署到⼀个节点 答案 C。
- 8.下列哪项可以作为集群的管理？ a)Puppetb)Pdsh c)Cloudera Manager d)Zookeeper 答案 ABD 具体可查看什么是 Zookeeper，Zookeeper 的作⽤是什么，在 Hadoop 及 hbase 中具体作⽤是什么。

- 9.Client 端上传⽂件的时候下列哪项正确


- 1.cpu处理能⼒强
- 2.内存够⼤，所以集群的瓶颈不可能是 a 和 d
- 3.如果是互联⽹有瓶颈，可以让集群搭建内⽹。每次写⼊数据都要通过⽹络（集群是内⽹），然后还要 写⼊ 3 份数据，所以 IO 就会打折扣。


- a)数据经过 NameNode 传递给DataNode
- b)Client端将⽂件切分为 Block，依次上传
- c)Client只上传数据到⼀台 DataNode，然后由 NameNode 负责 Block 复制⼯作


- 答案 B 分析：Client 向 NameNode 发起⽂件写⼊的请求。NameNode 根据⽂件⼤⼩和⽂件块配置情况，返回 给 Client 它所管理部分 DataNode 的信息。Client 将⽂件划分为多个 Block，根据 DataNode 的地址信 息，按顺序写⼊到每⼀个DataNode 块中。具体查看HDFS 体系结构简介及优缺点。


- 10.下列哪个是 Hadoop 运⾏的模式 a)单机版 b)伪分布式 c)分布式 答案 ABC 单机版,伪分布式只是学习⽤的。


- 1. 集群可以运⾏的3个模式？ 单机（本地）模式 伪分布式模式 全分布式模式
- 2. 单机（本地）模式中的注意点？ 在单机模式（standalone）中不会存在守护进程，所有东⻄都运⾏在⼀个JVM上。这⾥同样没有 DFS，使⽤的是本地⽂件系统。单机模式适⽤于开发过程中运⾏MapReduce程序，这也是最少使⽤的 ⼀个模式。
- 3. 伪分布模式中的注意点？ 伪分布式（Pseudo）适⽤于开发和测试环境，在这个模式中，所有守护进程都在同⼀台机器上运⾏。


Hadop

5. 全分布模式⼜有什么注意点？ 全分布模式通常被⽤于⽣产环境，这⾥我们使⽤N台主机组成⼀个Hadop集群，Hadop守护进程运 ⾏在每台主机之上。这⾥会存在Namenode运⾏的主机，Datanode运⾏的主机，以及task tracker运⾏ 的主机。在分布式环境下，主节点和从节点会分开。

- 8. Namenode、Job tracker和task tracker的端⼝号是？ Namenode，70；Job tracker，30；Task tracker，60。
- 9. Hadop的核⼼配置是什么？ Hadop的核⼼配置通过两个xml⽂件来完成：1，hadop-default.xml；2，hadop-site.xml。这些⽂ 件都使⽤xml格式，因此每个xml中都有⼀些属性，包括名称和值，但是当下这些⽂件都已不复存在。
- 10. 那当下⼜该如何配置？ Hadop现在拥有3个配置⽂件：1，core-site.xml；2，hdfs-site.xml；3，mapred-site.xml。这些⽂ 件都保存在conf/⼦⽬录下。


17. “jps”命令的⽤处？ 这个命令可以检查Namenode、Datanode、Task Tracker、 Job Tracker是否正常⼯作。

- 1、hadop运⾏的原理? x
- 2、mapreduce的原理?


x

- 3、HDFS存储的机制? x
- 4、举⼀个简单的例⼦说明mapreduce是怎么来运⾏的 ? x
- 5、⾯试的⼈给你出⼀些问题,让你⽤mapreduce来实现？ ⽐如:现在有10个⽂件夹,每个⽂件夹都有1 0个url.现在让你找出top1 0url。

x

- 6、hadop中Combiner的作⽤? x


