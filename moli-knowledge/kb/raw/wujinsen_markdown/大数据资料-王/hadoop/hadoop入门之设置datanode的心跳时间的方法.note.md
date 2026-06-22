Linux环境：CentOs6.4 Hadop版本：Hadop-1.1.2 master: 192.168.1.241 NameNode JobTracker DataNode TaskTracker slave:192.168.1.242 DataNode TaskTracker 内容：设置DataNode的⼼跳，当某⼀个节点失去连接之后，在超过设置的时间，看hadop能否正常 ⼯作。 设置时间：

复制代码代码如下: <property> <name>heartbeat.recheck.interval</name> <value>15</value> </property>

第⼀步： 配置hdfs-site.xml

![image 1](<hadoop入门之设置datanode的心跳时间的方法.note_images/imageFile1.png>)

第⼆步：重启Hadop

# 第三步：通过⽹⻚浏览两个节点的状态。

![image 2](<hadoop入门之设置datanode的心跳时间的方法.note_images/imageFile2.png>)

![image 3](<hadoop入门之设置datanode的心跳时间的方法.note_images/imageFile3.png>)

![image 4](<hadoop入门之设置datanode的心跳时间的方法.note_images/imageFile4.png>)

![image 5](<hadoop入门之设置datanode的心跳时间的方法.note_images/imageFile5.png>)

# hadop两个节点都已正常运⾏。 第三步：杀死主节点的进程，等待15秒。

![image 6](<hadoop入门之设置datanode的心跳时间的方法.note_images/imageFile6.png>)

通过kil命令杀死master上的DataNode节点。 第四步：查看节点状态

![image 7](<hadoop入门之设置datanode的心跳时间的方法.note_images/imageFile7.png>)

活着的DataNode还有1个，死亡的DataNode⼀个

![image 8](<hadoop入门之设置datanode的心跳时间的方法.note_images/imageFile8.png>)

master上的DataNode节点已经标识为Dead

![image 9](<hadoop入门之设置datanode的心跳时间的方法.note_images/imageFile9.png>)

只剩下slave节点，其最后连接时间是2秒（Last Contact 2）

![image 10](<hadoop入门之设置datanode的心跳时间的方法.note_images/imageFile10.png>)

杀死⼀个节点，两⼀个节点仍能够正常查看⽂件信息。

![image 11](<hadoop入门之设置datanode的心跳时间的方法.note_images/imageFile11.png>)

只有slave节点在运⾏。

