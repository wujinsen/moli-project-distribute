## 1.概述

最近有朋友问我Secondary NameNode的作⽤，是不是NameNode的备份？是不是为了防 ⽌NameNode的单点问题？确实，刚接触Hadoop，从字⾯上看，很容易会把Secondary NameNode当作备份节点；其实，这是⼀个误区，我们不能从字⾯来理解，阅读官⽅⽂档，我们 可以知道，其实并不是这么回事，下⾯就来赘述下Secondary NameNode的作⽤。

# 2.Secondary NameNode？

在Hadoop中，有⼀些命名模块不那么尽⼈意，Secondary NameNode就是⼀个典型的例 ⼦之⼀。从它的名字上看，它给⼈的感觉就像是NameNode的备份节点，但实际上却不是。很多 Hadoop的⼊⻔者都很疑惑，Secondary NameNode究竟在其中起什么作⽤，它在HDFS中所扮 演的⻆⾊是什么。下⾯，我就来解释下：

从名字来看，它确实与NameNode有点关系；因此，在深⼊了解Secondary NameNode之 前，我们先来看看NameNode是做什么的。

### 2.1NameNode

NameNode主要是⽤来保存HDFS的元数据信息，⽐如命名空间信息，块信息等等。当它运 ⾏的时候，这些信息是存在内存中的。但是这些信息也可以持久化到磁盘上。如下图所示：

![image 1](<解读Secondary NameNode的功能.note_images/imageFile1.png>)

上图展示来NameNode怎么把元数据保存到磁盘上，这⾥有两个不同的⽂件：

fsimage：它是NameNode启动时对整个⽂件系统的快照。 edits：它是在NameNode启动后，对⽂件系统的改动序列。

只有在NameNode重启时，edits才会合并到fsimage⽂件中，从⽽得到⼀个⽂件系统的最新 快照。但是在⽣产环境集群中的NameNode是很少重启的，这意味者当NameNode运⾏来很⻓ 时间后，edits⽂件会变的很⼤。在这种情况下就会出现下⾯这些问题：

edits⽂件会变的很⼤，如何去管理这个⽂件？ NameNode的重启会花费很⻓的时间，因为有很多改动要合并到fsimage⽂件上。 如果NameNode宕掉了，那我们就丢失了很多改动，因为此时的fsimage⽂件时间戳⽐较 旧。

- 1.
- 2.
- 3.


因此为了克服这个问题，我们需要⼀个易于管理的机制来帮助我们减⼩edits⽂件的⼤⼩和得 到⼀个最新的fsimage⽂件，这样也会减⼩在NameNode上的压⼒。⽽Secondary NameNode 就是为了帮助解决上述问题提出的，它的职责是合并NameNode的edits到fsimage⽂件中。如图 所示：

![image 2](<解读Secondary NameNode的功能.note_images/imageFile2.png>)

上图的⼯作原理，我这⾥也赘述下：

⾸先，它定时到NameNode去获取edits，并更新到fsimage上。 ⼀旦它有新的fsimage⽂件，它将其拷⻉回NameNode上。 NameNode在下次重启时回使⽤这个新的fsimage⽂件，从⽽减少重启的时间。

- 1.
- 2.
- 3.


Secondary NameNode的整个⽬的在HDFS中提供⼀个Checkpoint Node，通过阅读官⽅ ⽂档可以清晰的知道，它只是NameNode的⼀个助⼿节点，这也是它在社区内被认为是 Checkpoint Node的原因。

现在，我们明⽩Secondary NameNode所做的是在⽂件系统这设置⼀个Checkpoint来帮助 NameNode更好的⼯作；它不是取代NameNode，也不是NameNode的备份。

Secondary NameNode的检查点进程启动，是由两个配置参数控制的：

fs.checkpoint.period，指定连续两次检查点的最⼤时间间隔， 默认值是1⼩时。 fs.checkpoint.size定义了edits⽇志⽂件的最⼤值，⼀旦超过这个值会导致强制执⾏检查点 （即使没到检查点的最⼤时间间隔）。默认值是64MB。

如果NameNode上除了最新的检查点以外，所有的其他的历史镜像和edits⽂件都丢失了， NameNode可以引⼊这个最新的检查点。以下操作可以实现这个功能：

在配置参数dfs.name.dir指定的位置建⽴⼀个空⽂件夹； 把检查点⽬录的位置赋值给配置参数fs.checkpoint.dir； 启动NameNode，并加上-importCheckpoint。

NameNode会从fs.checkpoint.dir⽬录读取检查点，并把它保存在dfs.name.dir⽬录下。 如果dfs.name.dir⽬录下有合法的镜像⽂件，NameNode会启动失败。 NameNode会检查 fs.checkpoint.dir⽬录下镜像⽂件的⼀致性，但是不会去改动它。

注：关于NameNode是什么时候将改动写到edit logs中的？这个操作实际上是由 DataNode的写操作触发的，当我们往DataNode写⽂件时，DataNode会跟NameNode通 信，告诉NameNode什么⽂件的第⼏个block放在它那⾥，NameNode这个时候会将这些元 数据信息写到edit logs⽂件中。

下⾯附上官⽅⽂档说明：

![image 3](<解读Secondary NameNode的功能.note_images/imageFile3.png>)

The NameNode stores modifications to the file system as a log appended to a native file system file, edits. When a NameNode starts up, it reads HDFS state from an image file, fsimage, and then applies edits from the edits log file. It then writes new HDFS state to the fsimage and starts normal operation with an empty edits file. Since NameNode merges fsimage and edits files only during start up, the edits log file could get verylarge over time on a busy cluster. Another side effect of a larger edits file is that next restart of NameNode takes longer.

The secondary NameNode merges the fsimage and the edits log files periodically and keeps edits log size within a limit. It is usually run ona different machine than the primary NameNode since its memory requirements are on the same order as the primary NameNode.

The start of the checkpoint process on the secondary NameNode is controlled by two configuration parameters.

- * dfs.namenode.checkpoint.period, set to 1 hour by default, specifies the maximum delay between two consecutive checkpoints, and

- * dfs.namenode.checkpoint.txns, set to 1 million by default, defines the number of uncheckpointed transactions on the NameNode which will force an urgent checkpoint, even if the checkpoint period has not been reached.


The secondary NameNode stores the latest checkpoint in a directory which is structured the same way as the primary NameNode's directory. So that the check pointed image is always ready to be read by the primary NameNode if necessary.

![image 4](<解读Secondary NameNode的功能.note_images/imageFile4.png>)

参考地址：http://hadoop.apache.org/docs/r2.6.0/hadoop-project-dist/hadoophdfs/HdfsUserGuide.html

## 3.总结

这篇⽂章就和⼤家分享到这⾥，若在阅读过程中有什么疑问，可以加群进⾏讨论或发送邮件 给我，我会尽我所能为您解答，与君共勉！

联系⽅式：

邮箱：smartdengjie@gmail.com

QQ群（Hadoop - 董的博客2）：306184597 （已满）

QQ群（Hadoop - 交流社区1）：424769183

温馨提示：请⼤家加群的时候写上加群理由（姓名＋公司/学校），⽅便管理员审核，谢谢！

热爱⽣活，享受编程，与君共勉！

作者：哥不是⼩萝莉 ［ ］

关于我

#### http://www.cnblogs.com/smartloli/

出处：

转载请注明出处，谢谢合作！

