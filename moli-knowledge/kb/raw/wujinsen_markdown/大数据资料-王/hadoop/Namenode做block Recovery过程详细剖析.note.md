hdfs为什么会做block recovery

说HDFS的block recovery，其实就是Namenode认为该block的状态需要发⽣⼀些变化，其原信息和数 据都需要做⼀些相应的调整（或恢复），原信息的调整在namenode上，⽽由于数据本身是存储在 datanode的磁盘上的，所以数据本身的调整其实是由datanode来完成。 那么，为什么Namenode会认为某个block的状态“需要发⽣⼀些改变呢”？这就要从代码中实际触发 block recovery的⼊⼝着⼿。实际上，在namenode中，namenode并不是直接触发block recovery的， 因为要对block做recovery操作，刚才上⾯提过，实际上涉及到两个部分的修改：

- a.
- b.


block原信息的修改 ⸺ 这部分的修改在namenode中直接就可以完成 block数据本身的修改 ⸺ 由于存储block实际上是datanode，所以，namenode就需要“告诉” datanode，让datanode来完成其上该block的recovery操作所以，实际上namenode是要向 datanode“发送block recovery命令”来达到上述第⼆个修改，也就是block数据本身的修改的⽬的。⽽“命令”是如何发

送给datanode的呢？这就涉及到HDFS中namenode和datanode的通信机制。我们都知道在HDFS中，datanode是 会每隔⼏秒钟向namenode定期的发送⼼跳，namenode通过监控每隔datanode的两次⼼跳间隔来判断该datanode 是否还活着，超过⼀定的时间间隔（默认10分30秒），就会认为该datanode已经死掉了。但是很多⼈不知道的是，

datanode每次⼼跳发送到namenode，namenode是会返回给datanode⼀个“命令集(cmds)”的，这个命令集就是 namenode需要datanode执⾏的某些操作，⽐如 将该datanode上的某个block拷⻉到其他datanode上去的DNA_TRANSFER命令 将该datanode上的某个block从物理磁盘上删除的DNA_TRANSFER命令 停⽌该datanode的DNA_SHUTDOWN命令 对某个block进⾏block recovery的DNA_RECOVERBLOCK命令 ….从上可以看出，从heartbeat的返回命令集中，就包括了对某个block进⾏recovery的命令。所以，datanode某个 block进⾏recovery操作的动作，实际上是来⾃namenode的指令。也就是说，namenode认为这个block需要做 recovery了，并且这个block在某⼏个datanode上保存，那么namenode就会在这⼏个datanode的heartbeat发送过来 后，给这⼏个datanode返回指令集，指令集中就包括对这个block进⾏recovery的指令。于是datanode接受到这个指 令后，对block进⾏数据本身的recovery操作。明⽩了datanode是如何会做block的recovery操作后，剩下的问题就还 剩下两个了：

- a.
- b.
- c.
- d.
- e.


- 1.
- 2.


namenode为什么会“认为某个block需要做recovery操作”？ datanode在recovery⼀个block的时候，实际上到底对这个block做了些什么？

namenode为什么会“认为某个block需要做recovery操作”？（什么情况下会做block recovery）

这个问题其实就是namenode对block做recovery的⼊⼝问题。什么情况下namenode会认为某个block 需要进⾏recovery操作呢？从namenode的代码内部实现层层挖下去，就会发现，其实⼊⼝只有⼀个， 就是namenode在对某个⽂件的lease进⾏release的时候。

于是，新的问题⼜来了，什么叫做“对某个⽂ 件的Lease进⾏release”？由于篇幅原因，这⾥不打算深⼊介绍namenode中的Lease原理和实现机制， 只是简单的做⼀个简介：Namenode中的所谓Lease，其实就是namenode中⽤来标识某个Client端对 HDFS中的某个⽂件正在进⾏写⼊操作的⼀个写锁。由于HDFS是⼀次写⼊，多次读取的系统，不允许 对⽂件内容进⾏modify（0.20开始HDFS⽀持对⽂件的append，但仍然不⽀持对⽂件中间的某些内容 进⾏修改），也不允许多个⽤户（客户端）对同⼀个⽂件同时进⾏修改，所以，通⼀时间，只能有⼀ 个客户端对⼀个⽂件的最后⼀个block进⾏写⼊，⽽如果其他客户端想在同⼀时间对该⽂件进⾏写⼊， 就是不允许的。这个排他的机制，就是利⽤Lease来保证的。也就是说，某个客户端要对⽂件进⾏写 ⼊，必须先申请到该⽂件的Lease，⼀旦申请到后，就会允许对该⽂件的block进⾏数据写⼊，⽽如果 有其他客户端已经持有这个⽂件的Lease，就不能再写⼊了。 知道了什么是Lease以后，再来解释什么叫做“对⼀个Lease进⾏release”。刚才有提到，⼀个Lease实 际上相当于HDFS中⼀个⽂件的写锁，对应⼀个客户端。那么，如果⼀个客户端（假设叫ClientA）在 持有某个⽂件的Lease情况下，客户端在写⼊数据过程中发⽣宕机，或者其他事故，导致⽆法继续对⽂ 件进⾏写⼊，会产⽣什么情况呢？这种情况下，由于该⽂件的Lease是由namenode来维护的，也就是 说，此时namenode认为该⽂件正在被ClientA持有，所以namenode就不允许其他client对⽂件进⾏写 ⼊，但此时ClientA已经挂了，但namenode不知道，这就会导致其他所有的Client都⽆法对⽂件进⾏写 ⼊了。这其实是不对的。所以，namenode中对某个client对应某个⽂件的Lease是有⼀个限期的，⼀旦 过了这个限期，该Lease没有发⽣任何改变（⽐如更新时间），没有写⼊任何数据，那么namenode就 认为该lease对应的客户端发⽣了异常，需要在namenode端对这个Lease进⾏释放，⼀遍其他的client 能够对⽂件进⾏写⼊操作。这个过程就叫做”对⼀个Lease进⾏release“操作。 ⾄此，就明⽩了，其实namenode中，对block做recovery的⼊⼝只有⼀个，就是namenode对某个 Lease进⾏释放的时候触发的。该函数调⽤在 NameNode.FSNamesystem.internalReleaseLeaseOne(Lease, filePath)。

datanode在recovery⼀个block的时候，实际上到底对这个block做了些什么？(datanode做block recovery的详细过程)

由于⼀个客户端（DFSClient）通常会不⽌⼀次写⼀个⽂件，可能会些多个，所以⼀个Lease对象在 namenode中通常代表⼀个客户端对⼀些⽂件的⽂件写锁，所以其实，对⼀个⽂件的lease的释放并不 ⼀定会删除掉namenode中该⽂件对应的lease对象，但是会释放这个⽂件在lease中的记录。所以⼀次 internalReleaseLeaseOne(Lease,filePath)的调⽤的参数包括⼀个lease和⼀个⽂件名，就是这个原因。 实际上，⼀次internalReleaseLeaseOne的过程就是namenode做了这么⼀件事情：

- 1， 从namenode内存中找到该filePath对应的⽂件INode，通常这个时候该INode是⼀个 INodeFileUnderConstruction的实例，表示这个⽂件是正在被写⼊，还没有complete的⼀个⽂件。
- 2， 找到这个INodeFileUnderConstruction以后，查看该⽂件的block队列是否为空，如果为空，表示这 个⽂件是个空⽂件，那么直接将该⽂件complete，删除其对应的lease记录，然后返回。


- 3， 如果该⽂件的block队列不为空，那么获取该⽂件的block队列，并找到该队列的最后⼀个block，将 该block的最后⼀个block对应的datanode设置为该⽂件的targets，这个操作的原因在于：由于HDFS的 ⽂件只能向最后⼀个block写⼊输⼊，所以lease过期肯定是出了最后⼀个block有问题外，其他block应 该都是完整的，所以获取最后⼀个block。⽽targets表示最后⼀个block应该保存在哪⼏个datanode 上，该targets是⼀个datanode队列，也就是说，namenode知道这最后⼀个block是在这么⼏台 datanode上，以便向这⼏个datanode发送block

recovery命令。

- 4， 在targets队列中选择⼀个datanode作为primary datanode
- 5， 将block recovery命令保存在namenode内存中对应的这个primary datanode的队列中，等待该 datanode的下⼀个heartbeat，然后在heartbeat的response中将block


recovery命令发送给这个datanode，让datanode完成物理block的recovery操作。

到这⾥，namenode对⼀个⽂件的block recovery操作就告⼀段落，namenode接下来要做的是等待接 收到block recovery命令的datanode对这个block进⾏recovery的物理操作，然后汇报状态。

datanode在接收到block recovery命令后（通常接收到这命令的都是⽂件最后⼀个block对应的 datanode targets数组中的primary datanode），就会对这个block进⾏真正的recovery操作。具体的 recovery操作流程如下：

- 1， 由于block recovery 是由primary datanode发起，但该recovery操作需要在三个datanode上对该 block进⾏操作（假设⽂件副本为3），所以primary datanode接收到命令的时候同时还收到了该block 的targets datanode数组（其中就包括该datanode⾃身） 接下来的问题是：datanode接收到namenode发送来的block recovery命令后，会做⼀些什么？
- 2， primary datanode遍历targets datanode数组，对每⼀个datanode，向其发送⼀个start block recovery的指令。如果是其⾃身，则直接执⾏该指令。
- 3， start block recovery指令会在datanode的磁盘中找到该block的物理块，并确认该block对应的验证 信息和meta信息正确，并返回⼀个BlockRecord对象，表示这个block正在被recovery。
- 4，对每个BlockRecord，查看keepLength标志位是否为true，如果为true，则只recovery blocksize 跟 namenode中记录的blocksize⼀致的block，否则全部都算。
- 5， 对每个物理块，⼀旦真正开始recovery操作，则进⾏如下操作：在该datanode上找到该block，同 时找到这个block对应的meta⽂件（每⼀个block都对应⼀个meta⽂件，⽤来记录该block的验证码等原 信息），更新该block的stamp号（表示该block已经被修改过⼀次），如果需要recovery成的block的 size ⼩于实际的block的size，则将实际的block截断成其需要的⼤⼩，并更新meta⽂件和验证信息。
- 6， 最后，primary datanode向namenode汇报本次recovery block的信息，如新的block stamp变成了 多少，block size被修改成了多少等，namenoe相应的更新这些信息。


原⽂地址：htp:/blog.csdn.net/ae86_fc/article/details/ 619278

