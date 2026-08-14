---
title: hadoop入门之设置datanode的心跳时间的方法.note（原文插图 annex）
slug: annex-hadoop入门之设置datanode的心跳时间的方法
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hadoop/hadoop入门之设置datanode的心跳时间的方法.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

Linux环境：CentOs6.4 Hadop版本：Hadop-1.1.2 master: 192.168.1.241 NameNode JobTracker DataNode TaskTracker slave:192.168.1.242 DataNode TaskTracker 内容：设置DataNode的⼼跳，当某⼀个节点失去连接之后，在超过设置的时间，看hadop能否正常 ⼯作。 设置时间：

复制代码代码如下: <property> <name>heartbeat.recheck.interval</name> <value>15</value> </property>

第⼀步： 配置hdfs-site.xml

![image 1](assets/imageFile1.png)

第⼆步：重启Hadop

# 第三步：通过⽹⻚浏览两个节点的状态。

![image 2](assets/imageFile2.png)

![image 3](assets/imageFile3.png)

![image 4](assets/imageFile4.png)

![image 5](assets/imageFile5.png)

# hadop两个节点都已正常运⾏。 第三步：杀死主节点的进程，等待15秒。

![image 6](assets/imageFile6.png)

通过kil命令杀死master上的DataNode节点。 第四步：查看节点状态

![image 7](assets/imageFile7.png)

活着的DataNode还有1个，死亡的DataNode⼀个

![image 8](assets/imageFile8.png)

master上的DataNode节点已经标识为Dead

![image 9](assets/imageFile9.png)

只剩下slave节点，其最后连接时间是2秒（Last Contact 2）

![image 10](assets/imageFile10.png)

杀死⼀个节点，两⼀个节点仍能够正常查看⽂件信息。

![image 11](assets/imageFile11.png)

只有slave节点在运⾏。
