⼀、概述

HDFS集群以Master-Slave模式运⾏，主要有两类节点：⼀个Namenode(即Master)和多个 Datanode(即Slave)。 HDFS Architecture：

![image 1](<HDFS学习(三) – Namenode and Datanode.note_images/imageFile1.png>)

⼆、Namenode

Namenode 管理者⽂件系统的Namespace。它维护着⽂件系统树(filesystem tre)以及⽂件树中所有 的⽂件和⽂件夹的元数据(metadata)。管理这些信息的⽂件有两个，分别是Namespace 镜像⽂件 (Namespace image)和操作⽇志⽂件(edit log)，这些信息被Cache在RAM中，当然，这两个⽂件也会 被持久化存储在本地硬盘。Namenode记录着每个⽂件中各个块所在的数据节点的位置信息，但是他 并不持久化存储这些信息，因为这些信息会在系统启动时从数据节点重建。 Namenode结构图课抽象为如图：

![image 2](<HDFS学习(三) – Namenode and Datanode.note_images/imageFile2.png>)

客户端(client)代表⽤户与namenode和datanode交互来访问整个⽂件系统。客户端提供了⼀些列的⽂ 件系统接⼝，因此我们在编程时，⼏乎⽆须知道datanode和namenode，即可完成我们所需要的功 能。

# 三、Datanode

Datanode是⽂件系统的⼯作节点，他们根据客户端或者是namenode的调度存储和检索数据，并且定 期向namenode发送他们所存储的块(block)的列表。

# 四、Namenode容错机制

没有Namenode，HDFS就不能⼯作。事实上，如果运⾏namenode的机器坏掉的话，系统中的⽂件将 会完全丢失，因为没有其他⽅法能够将位于不同datanode上的⽂件块(blocks)重建⽂件。因此， namenode的容错机制⾮常重要，Hadop提供了两种机制。 第⼀种⽅式是将持久化存储在本地硬盘的⽂件系统元数据备份。Hadop可以通过配置来让Namenode 将他的持久化状态⽂件写到不同的⽂件系统中。这种写操作是同步并且是原⼦化的。⽐较常⻅的配置 是在将持久化状态写到本地硬盘的同时，也写⼊到⼀个远程挂载的⽹络⽂件系统。 第⼆种⽅式是运⾏⼀个辅助的Namenode(Secondary Namenode)。 事实上Secondary Namenode并 不能被⽤作Namenode它的主要作⽤是定期的将Namespace镜像与操作⽇志⽂件(edit log)合并，以防 ⽌操作⽇志⽂件(edit log)变得过⼤。通常，Secondary Namenode 运⾏在⼀个单独的物理机上，因为 合并操作需要占⽤⼤量的CPU时间以及和Namenode相当的内存。辅助Namenode保存着合并后的 Namespace镜像的⼀个备份，万⼀哪天Namenode宕机了，这个备份就可以⽤上了。 但是辅助Namenode总是落后于主Namenode，所以在Namenode宕机时，数据丢失是不可避免的。在 这种情况下，⼀般的，要结合第⼀种⽅式中提到的远程挂载的⽹络⽂件系统(NFS)中的Namenode的元 数据⽂件来使⽤，把NFS中的Namenode元数据⽂件，拷⻉到辅助Namenode，并把辅助Namenode作 为主Namenode来运⾏。 更多有关Hadop HA的介绍，可以参⻅ 3013.2.2⽇更新如下：

HDFS学习(四) – HDFS Federation

# 五、HDFS⽂件读写流程

- (1)写⽂件流程


![image 3](<HDFS学习(三) – Namenode and Datanode.note_images/imageFile3.png>)

HDFS Client端通过调⽤DistributedFileSystem发起创建请求，在NameNode上创建⼀个⽂件，此 时，⽂件并没有任何Block块信息

⽂件创建成功之后，DistributedFileSystem会返回⼀个FSDataOutputStream对象，这个对象负责 写⽂件过程中Namenode和Datanode之间的通信

FSDataOutputStream将Client端要写⼊得快分成若⼲份packet，然后向Namenode询问这些packet 以及其副本所要存储的Datanode的⼀个列表

得到列表后，先将这个Datanode列表组成⼀个管道，接下来，FSDataOutputStream就要将这些 packet写⼊这个管道

FSDataOutputStream先将第⼀个packet写⼊管道中第⼀个Datanode，成功后第⼀个Datanode将数 据在传送到第⼆个Datanode，成功后第⼆个Datanode将数据再写⼊到第三个

直到第三个Datanode数据写⼊完成后，逐层向上返回写⼊成功，直到通知FSDataOutputStream

FSDataOutputStream得到通知后，接着写⼊第⼆个packet，如此循环

当把所有的packet都写⼊后，FSDataOutputStream调⽤close⽅法，关闭与Client端的连接，然后 通知Namenode写⼊完毕

- (2) 读⽂件流程


![image 4](<HDFS学习(三) – Namenode and Datanode.note_images/imageFile4.png>)

HDFS Client端通过调⽤DistributedFileSystem向Namenode发起open请求，获取⽂件有哪些块， 这些块及其副本所在节点的位置，然后返回⼀个FSDataInputStream对象。这个对象负责读⽂件过 程中Namenode和Datanode之间的通信

Client端调⽤FSDataInputStream的读⽅法，与Datanode端建⽴socket连接并获取数据

如果在通讯的时候失效了，就会与该数据块第⼆个副本的位置建⽴连接并获取数据，通信成功，将 数据返回

直到将所有的块都读取成功完毕，FSDataInputStream对象关闭与客户端连接

HDFS系列⽂章： 《 》 《 》 《 》 《 》 This entry was posted in , and taged , , ,

HDFS学习(⼀) – HDFS设计 HDFS学习(⼆) – HDFS Block介绍 HDFS学习(三) – Namenode and Datanode HDFS学习(四) – HDFS Federation

Hadop学习 HDFS篇 DatanodeHadop HAHDFS学习 Nam enode permalink

. Bokmark the .

# Postnavigation

← shel中的cut命令 产品经理也学数据分析（前记） →

2 thoughts on “HDFS学习(三) – Namenode and Datanode”

1.

![image 5](<HDFS学习(三) – Namenode and Datanode.note_images/imageFile5.png>)

michaelsays:

2013 年 8 ⽉ 29 ⽇ at 1 45

你好 ”HDFS Client端通过调⽤DistributedFileSystem “ 这⾥我有⼀些疑问

- 1.HDFS Client，这个Clinet指的是什么
- 2.DistributedFileSystem 这个是指什么
- 3.可以帮忙描述下 HDFS Client端通过调⽤DistributedFileSystem 这到底是怎么样那个⼀个过程


登录以回复

![image 6](<HDFS学习(三) – Namenode and Datanode.note_images/imageFile6.png>)

Fowler Zhangsays:

2013 年 8 ⽉ 29 ⽇ at 15 25

^_^

登录以回复

