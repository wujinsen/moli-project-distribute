namenode是hadop的核⼼，他管理⽂件系统的命名空间，维护⽂件系统树以及这个树的 所有的⽂件和索引⽬录。这些信息通过两种形式将⽂件持久化到本地磁盘：命名空间镜像 （fsImage）和编辑⽇志(edit log).名称节点记录着每个⽂件的每个块所在的数据节点，但是 不永久保存块的位置，这些信息会在系统启动是由数据节点推送过来。 主要管理两个内容： ⽂件名 ->数据块 数据块 ->DataNode列表 ⽂件名 ->数据块保存在磁盘上（持久化）；但NameNode上不保存数据块 ->DataNode列 表，该列表是通过DataNode上报给namenode建⽴起来的。 Namenode实现了ClientProtocol 、DatanodeProtocol NamenodeProtocol、可以提供给客 户端、datanoe、secondory namenode调⽤的⽅法，是通过rpc机制调⽤的。

# ClientProtocol

ClientProtocol提供给客户端使⽤，⽤于访问NameNode。从⽂件使⽤⻆度完美封装HDFS 功能，在使⽤过程中感受不到在操作⼀个分布式的⽂件系统。和GFS⼀样，HDFS不提供 POSIX形式的接⼝，⽽是使⽤了⼀个私有接⼝。⼀般来说，程序员通过 org.apache.hadop.fs.FileSystem来和HDFS打交道：最常⽤的分布式⽂件类是 DistributedFileSystem，包含有DFSClient dfs，包含了ClientProtocol rpcNamenode实例， 分布式⽂件实例通过dfs client跟Namenode进⾏间接关联，具体可以参考详细的api

DatanodeProtocol：⽤于DataNode向NameNode通信，主要包括： register，⽤于DataNode注册，⽣成⼀个特有的storage id； sendHeartbeat datanode定期向namenode发送⼼跳告知⾃⼰还存活，namenode更新该 datanode的信息，并且返回⼀些命令供datanode执⾏，包括失效block或者复制块。 blockReport datanoe定期告知namenode⾃⼰所有的⽂件块，namenode返回需要删除的⽂ 件块列表。 NamenodeProtocol⽤于从NameNode到NameNode的通信。主要是secondary NameNode 到NameNode的调⽤ namenode启动：

- 1初始化ipc server, 主要负责接收并处理来⾃客户端/datanode的连接（详细情况⻅hadop rpc学习）
- 2 Namenode通过FSNamesystem来实现对内部⽂件的管理：


初始化FSNamesystem， 然后查找StorageDirectory，查找配置⽂件（dfs.name.edits.dir和 dfs.name.dir）就是正在修改的命名空间⽂件和命名空间⽂件，放到 List<StorageDirectory> storageDirs 中，循环找出需要进⾏修复或者回滚的⽂件内容，进 ⾏处理。查找到最新的StorageDirectory，装载image⽂件 （\home\baqun\tmp\dfs\name\curent\fsimage其中包括了版本信息、⽂件个数、最后修改 时间、包含的block的信息、权限控制信息、datanode信息、正在创建的⽂件信息） (这是⼀个复杂的过程，把blocks的信息、inode的信息，permision、parent inode然后组 装起来 读取datanode\) 然后把edit log也装载进来，merge进内存。 3提供服务（供其他机器进⾏rpc调⽤）

# FSNamesystem的初始化是namenode启动的⼀个重要过程，主要 ⼯作包括：

1初始化FSDirectory 2装载最新的命名空间⽂件 3机器名、端⼝号设置 4配置信息初始化（⽂件副本数、最⼤/⼩⽂件副本数、默认block⼤⼩、⼼跳时间间隔） 5设置safeMode，安全模式： 安全模式是这样⼀种状态，系统处于这个状态时，不接受任何对名字空间的修改，同时也 不会对数据块进⾏复制或删除数据块。NameNode启动的时候会⾃动进⼊安全模式，同时 也可以⼿⼯进⼊（不会⾃动离开）。系统启动以后，DataNode会报告⽬前它拥有的数据块 的信息，当系统接收到的Block信息到达⼀定⻔槛，同时每个Block都有dfs.replication.min 个副本后，系统等待⼀段时间后就离开安全模式。这个⻔槛定义的参数包括： l dfs.safemode.threshold.pct：接受到的Block的⽐例，缺省为95%，就是说，必须 DataNode报告的数据块数⽬占总数的95%，才到达⻔槛； l dfs.replication.min：缺省为1，即每个副本都存在系统中； l dfs.replication.min：等待时间，缺省为0，单位秒。 6构造线程：

[java]view plaincopy Daemonhbthread = nul; /HeartbeatMonitor thread

- 1.
- 2.
- 3.
- 4.


public Daemonlmthread = nul; / LeaseMonitorthread Daemon s mthread = nul; / SafeModeMonitor thread public Daemonreplthread = nul; / Replicationthread

都是NameNode启动时新建的线程，分别对应DataNode⼼跳检查，租约检查，安全模式检 查和数据块复制。 租约线程： ⼀个租约由⼀个holder（客户端名），lastUpdate（上次更新时间）和paths（该客户端操 作的⽂件集合）构成。LeaseManager对Lease进⾏管理。租约线程Monitor通过对Lease的 最后更新时间来检测Lease是否过期，如果过期，就调⽤FSNamesystem的 internalReleaseLease⽅法。

复制线程replthread： replthread运⾏ReplicationMonitor，这个线程会定期调⽤computeDatanodeWork和 procesPendingReplications。 computeDatanodeWork会执⾏computeDatanodeWork或computeInvalidateWork。 computeDatanodeWork从nededReplications中扫描，取出需要复制的项，然后： l 检查⽂件不存在或者处于构造状态；如果是，从队列中删除复制项，退出对复制项 的处理（接着处理下⼀个）； l 得到当前数据块副本数并选择复制的源DataNode，如果空，退出对复制项的处理； l 再次检查副本数（很可能有DataNode从故障中恢复），如果发现不需要复制，从队 列中删除复制项，退出对复制项的处理； l 选择复制的⽬标，如果⽬标空，退出对复制项的处理； l 将复制的信息（数据块和⽬标DataNode）加⼊到源⽬标DataNode中；在⽬标 DataNode中记录复制请求； l 从队列中将复制项移动到pendingReplications。这个⽅法执⾏后，复制项从 neededReplications挪到pendingReplications中。DataNode在某次⼼跳的应答中，可以拿 到相应的信息，执⾏复制操作。

Heartbeat线程(主要由FSNamesystem实现)： HeartbeatMonitor 有⼀个ArayList<DatanodeDescriptor>heartbeats,记录的是datanode的 注册信息，这个线程定时去轮询所有的⼼跳连接，检测这个datanode是不是超时（死亡， ⼀个时间段内⽆连接），如果已经认定死亡，就删除这个节点，紧接着做如下处理：

- （1） 更新系统全部可⽤空间，全部的容量，剩余容量
- （2） heartbeats列表⾥删除这个⼼跳，删除这个node的所有block信息，在整个的 blockMap⾥也删除block信息，重置这个节点的信息（为了加速gc）


- （3） 在NetworkTopology⾥删除这个节点的信息（在⽹络结构中删除这个节点，他是⼀个 树状的⽹络拓扑结构，⼀个集群是由数据中⼼组成的，这个数据中⼼包括rack（机架）， 机架上有多台机器，在这个结构中，叶⼦节点代表了datanode,中间节点代表了交换机/路由 器，他们负责管理rack/数据中⼼数据的传输,这个类主要负责计算两个节点之间的关系，是 通过⼀个算法来维护的，⽅法就是 getDistance(Node node1, Nodenode2) 7 DNSToSwitchMaping对象实例化： 是HDFS节点之间的⽹络拓扑的实现 这个抽象类完美的抽象了⽹络top结构，是⾮常精妙的实现⽅式 节点之间的距离由近到远如下 同⼀个节点之间 同⼀机架上的节点传输 同⼀个数据中⼼的不同机架上的节点传输 不同数据中⼼的节点数据传输 对应图如下：


在dfs的副本⽂件存储的规则（下图）是 第⼀个副本放在同⼀个节点上 第⼆个放在不同rack的节点上 第三个放在不同rack的不同节点上

参考：

## Hadop源代码分析

