htps:/ w.cnblogs.com/guoyu1/p/1392103.html

# ⼀、什么是kafka的Rebalance

kafka集群模式下，⼀个topic有多个partition，对于消费端，可以有多个consumer同时消费这些 partition。为了保证⼤体上partition和consumer的均衡性，提升topic的并发消费能⼒，所以会有 Rebalance。Rebalance 本质上是⼀种协议，规定了⼀个 Consumer Group 下的所有 consumer 如何达 成⼀致，来分配订阅 Topic 的每个分区。 ⼆、什么时机触发Rebalance

- 0.10kafka的rebalance条件

当⼀个group中,有consumer加⼊或者离开时,会触发partitions均衡。Kafka的Consumer Rebalance⽅案 是基于Zookeeper的Watcher来实现的。consumer启动的时候，在zk下都维护⼀ 个”/consumers/[group_name]/ids”路径，在此路径下，使⽤临时节点记录属于此cg的消费者的Id，该 Id信息由对应的consumer在启动时创建。每个consumer都会在此路径下简历⼀个watcher，当有节点 发⽣变化时，就会触发watcher，然后触发Rebalance过程。 三、0.9之前kafka的Rebalance算法 Consumer rebalacne算法：

- 1. 将⽬标 topic 下的所有 partirtion 排序，存于PT
- 2. 对某 consumer group 下所有 consumer 排序，存于 CG，第 i 个consumer 记为 Ci
- 3. N=size(PT)/size(CG)，向上取整
- 4. 解除 Ci 对原来分配的 partition 的消费权（i从0开始）
- 5. 将第i*N到（i+1）*N-1个 partition 分配给 Ci 在Rebalance期间，consumer不能正常消费，并且这种Rebalance过程强依赖zk，存在以下问题：


- 条件1：有新的consumer加⼊

- 条件2：旧的consumer挂了

- 条件3：coordinator挂了，集群选举出新的coordinator（0.10 特有的）

- 条件4：topic的partition新加

- 条件5：consumer调⽤unsubscrible()，取消topic的订阅


herd effect（⽺群效应）：⼀个被Watch的zk节点变化，导致⼤量的watcher通知需要被发送给客户 端，这会导致在通知期间其他操作的延迟。

split brain：每个Consumer都是通过zk中保存的元数据来判断group中各其他成员的状态，以及 broker的状态，进⽽分别进⼊各⾃的Rebalance，执⾏各⾃的Rebalance逻辑。不同的Consumer在同

⼀时刻可能连接在不同的zk服务器上，看到的元数据就可能不⼀样，基于不⼀样的元数据，执⾏ Rebalance就会产⽣不⼀致（冲突）的Rebalance结果，Rebalance的冲突，会到导致consumer的 rebalance失败。

重复消费问题：因为Rebalance时，很有可能导致offset commit不成功，所以可能造成重复消费问 题。

解决办法：

加⼤Rebalance的重试时间:"rebalance.backoff.ms=5000"

加⼤Rebalance失败的retry次数: "rebalance.max.retries=10"

捕获"ConsumerRebalanceFailedException"，退出程序。

四、0.9后kafka对Rebalance过程进⾏了改进 Group Coordinator是⼀个服务，每个Broker在启动的时候都会启动⼀个该服务。Group Coordinator的 作⽤是⽤来存储Group的相关Meta信息，并将对应Partition的Offset信息记录到Kafka内置 Topic(__consumer_offsets)中。Kafka在0.9之前是基于Zookeeper来存储Partition的Offset信息 (consumers/{group}/offsets/{topic}/{partition})，因为ZK并不适⽤于频繁的写操作，所以在0.9之后通 过内置Topic的⽅式来记录对应Partition的Offset。 每个Group都会选择⼀个Coordinator来完成⾃⼰组内各Partition的Offset信息。那么consumer group如 何确定⾃⼰的coordinator是谁呢？ 简单来说分为两步：

确定consumer group位移信息写⼊__consumers_offsets的哪个分区。具体计算公式： __consumers_offsets partition# = Math.abs(groupId.hashCode() % groupMetadataTopicPartitionCount) 注意：groupMetadataTopicPartitionCount由 offsets.topic.num.partitions指定，默认是50个分区。

该分区leader所在的broker就是被选定的coordinator。

前⾯说过， rebalance本质上是⼀组协议。group与coordinator共同使⽤它来完成group的rebalance。 ⽬前kafka提供了5个协议来处理与consumer group coordination相关的问题：

Heartbeat请求：consumer需要定期给coordinator发送⼼跳来表明⾃⼰还活着

LeaveGroup请求：主动告诉coordinator我要离开consumer group

SyncGroup请求：group leader把分配⽅案告诉组内所有成员

JoinGroup请求：成员请求加⼊组

DescribeGroup请求：显示组的所有信息，包括成员信息，协议名称，分配⽅案，订阅信息等

rebalance过程分为2步：Join和Sync

- 1 Join， 顾名思义就是加⼊组。这⼀步中，所有成员都向coordinator发送JoinGroup请求，请求⼊组。 ⼀旦所有成员都发送了JoinGroup请求，coordinator会从中选择⼀个consumer担任leader的⻆⾊，并把 组成员信息以及订阅信息发给leader——注意leader和coordinator不是⼀个概念。leader负责消费分配 ⽅案的制定。
- 2 Sync，这⼀步leader开始分配消费⽅案，即哪个consumer负责消费哪些topic的哪些partition。⼀旦完 成分配，leader会将这个⽅案封装进SyncGroup请求中发给coordinator，⾮leader也会发SyncGroup请 求，只是内容为空。coordinator接收到分配⽅案之后会把⽅案塞进SyncGroup的response中发给各个 consumer。这样组内的所有成员就都知道⾃⼰应该消费哪些分区了。 五、如何避免不必要的Rebalance


除开consumer正常的添加和停掉导致rebalance外，在某些情况下，Consumer 实例会被 Coordinator 错误地认为 “已停⽌” 从⽽被“踢出”Group，导致rebalance，这种情况应该避免。 第⼀类⾮必要 Rebalance 是因为未能及时发送⼼跳，导致 Consumer 被 “踢出”Group ⽽引发的。这种 情况下我们可以设置 session.timeout.ms 和 heartbeat.interval.ms 的值，来尽量避免rebalance的出 现。（以下的配置是在⽹上找到的最佳实践，暂时还没测试过）

设置 session.timeout.ms = 6s。

设置 heartbeat.interval.ms = 2s。

要保证 Consumer 实例在被判定为 “dead” 之前，能够发送⾄少 3 轮的⼼跳请求，即 session.timeout.ms >= 3 * heartbeat.interval.ms。

这两个参数的区别 https://stackoverflow.com/questions/43881877/difference-between-heartbeatinterval-ms-and-session-timeout-ms-in-kafka-consume

将 session.timeout.ms 设置成 6s 主要是为了让 Coordinator 能够更快地定位已经挂掉的 Consumer， 早⽇把它们踢出 Group。 第⼆类⾮必要 Rebalance 是 Consumer 消费时间过⻓导致的。此时，max.poll.interval.ms 参数值的设 置显得尤为关键。如果要避免⾮预期的 Rebalance，你最好将该参数值设置得⼤⼀点，⽐你的下游最⼤ 处理时间稍⻓⼀点。

