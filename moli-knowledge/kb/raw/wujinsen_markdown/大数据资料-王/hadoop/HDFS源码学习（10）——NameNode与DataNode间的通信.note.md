NameNode和DataNode间的通信分为四种场景：

- 1.
- 2.
- 3.
- 4.


初始时DataNode注册： 周期性⼼跳检测： 周期性blockreport： 完成⼀个副本的写⼊：

# ⼀、初始时DataNode注册

DataNode在启动时会向NameNode注册，注册时需要提交的信息有DatanodeRegistration表示。结构 如下：

![image 1](<HDFS源码学习（10）——NameNode与DataNode间的通信.note_images/imageFile1.png>)

主要包括：

- 1.
- 2.
- 3.


name：机器名（主机名+服务端⼝号） infoPort: 状态信息服务端⼝好 ipcPort： 提供ipc服务的端⼝号

此外，该类中的storageID是该datanode在集群中的唯⼀id，在注册时有NameNode分配

注册的主要流程如下：

![image 2](<HDFS源码学习（10）——NameNode与DataNode间的通信.note_images/imageFile2.png>)

# ⼆、⼼跳检测（heartbeat）

DataNode通过周期性调⽤namenode.sendHeartbeat()来完成⼼跳检测.主要流程如下：

![image 3](<HDFS源码学习（10）——NameNode与DataNode间的通信.note_images/imageFile3.png>)

# 三、blockReport

DataNode周期性向NameNode发送blockReport，告知⾃⼰最新的block信息：

![image 4](<HDFS源码学习（10）——NameNode与DataNode间的通信.note_images/imageFile4.png>)

# 四、完成副本写⼊

