---
title: HDFS源码学习（10）——NameNode与DataNode间的通信.note（原文插图 annex）
slug: annex-HDFS源码学习（10）——NameNode与DataNode间的通信
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hadoop/HDFS源码学习（10）——NameNode与DataNode间的通信.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

NameNode和DataNode间的通信分为四种场景：

- 1.
- 2.
- 3.
- 4.


初始时DataNode注册： 周期性⼼跳检测： 周期性blockreport： 完成⼀个副本的写⼊：

# ⼀、初始时DataNode注册

DataNode在启动时会向NameNode注册，注册时需要提交的信息有DatanodeRegistration表示。结构 如下：

![image 1](assets/imageFile1.png)

主要包括：

- 1.
- 2.
- 3.


name：机器名（主机名+服务端⼝号） infoPort: 状态信息服务端⼝好 ipcPort： 提供ipc服务的端⼝号

此外，该类中的storageID是该datanode在集群中的唯⼀id，在注册时有NameNode分配

注册的主要流程如下：

![image 2](assets/imageFile2.png)

# ⼆、⼼跳检测（heartbeat）

DataNode通过周期性调⽤namenode.sendHeartbeat()来完成⼼跳检测.主要流程如下：

![image 3](assets/imageFile3.png)

# 三、blockReport

DataNode周期性向NameNode发送blockReport，告知⾃⼰最新的block信息：

![image 4](assets/imageFile4.png)

# 四、完成副本写⼊
