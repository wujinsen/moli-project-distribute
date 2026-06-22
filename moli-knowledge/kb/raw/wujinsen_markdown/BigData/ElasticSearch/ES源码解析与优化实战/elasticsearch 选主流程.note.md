# 设计思想

所有分布式系统都需要以某种⽅式处理⼀致性.⼀般的，可以将策略分为两组:试图避免不⼀ 致,和定义发⽣之后如何协调他们.后者对于适⽤这种⽅案问题来说⾮常强⼤,但对数据模型有 ⽐较严格的限制。因此这⾥研究第⼀类，以及如何应对⽹络故障。

# 为什么使⽤ Master

另⼀种选择是分布式哈希表(DHT),可以⽀持每⼩时数千个节点的离开和加⼊,他可以在不了 解底层⽹络拓扑的异构⽹络中⼯作,查询响应时间⼤约为4到10跳(中转次数)，但是在相对稳 定的对等⽹络中,Master模式会更好 Elasticsearch的典型场景中的另⼀个简化是集群中没有那么多节点。 通常，节点的数量远 远⼩于单个节点能够维护的连接数，并且⽹格环境不必经常处理节点加⼊和离开。 这就是 为什么领导者的做法更适合Elasticsearch。

# 选举算法

不重复造轮⼦,最好实现⼀个众所周知的算法，其中的优点和缺陷是已知的 Buly算法 Leader选举的基本算法之⼀。 它假定所有节点都有⼀个惟⼀的ID，该ID对节点进⾏排序。 任何时候的当前Leader都是参与集群的最⾼id节点。 该算法的优点是易于实现,但是,当拥有 最⼤ id 的节点处于不稳定状态的场景下会有问题,例如 Master 负载过重⽽假死,集群拥有第 ⼆⼤id 的节点被选为 新主,这时原来的 Master 恢复,再次被选为新主,然后⼜假死… elasticsearch 通过推迟选举直到当前的 Master 失效来解决上述问题,但是容易产⽣脑裂,再 通过 法 定 得 票 ⼈ 数 过 半 解决脑裂 Paxos算法 Paxos实现起来⾮常复杂,但⾮常强⼤，尤其在什么时机,以及如何进⾏选举⽅⾯的灵活性⽐ 简单的Buly算法有很⼤的优势，因为在现实⽣活中，存在⽐⽹络链接异常更多的故障模 式。

# 选主流程

只有⼀个 Leader将当前版本的全局集群状态推送到每个节点。 ZenDiscovery（默认）过程 就是这样的:

每个节点计算最低的已知节点ID，并向该节点发送领导投票

如果⼀个节点收到⾜够多的票数，并且该节点也为⾃⼰投票，那么它将扮演领导者的⻆ ⾊，开始发布集群状态。

所有节点都会参数选举,并参与投票,但是,只有有资格成为 master 的节点的投票才有效.

有多少选票赢得选举的定义就是所谓的法定⼈数。 在弹性搜索中，法定⼤⼩是⼀个可配置 的参数。 （⼀般配置成:可以成为master节点数n/2+1）

# 详细流程

路径:discovery.zen.ZenDiscovery#i nerJoinCluster

![image 1](<elasticsearch 选主流程.note_images/imageFile1.png>)

## 在 es 中,发送投票就是发送加⼊集群请求.在 handleJoinRequest 过程统计投票,收到的连接 被存储到 pendingJoinRequests. 在 checkPendingJoinsAndElectIfNeded 中检查投票是否⾜够,其中会过滤掉没有 Master 资格节点的投票

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br></th>
    <th>int pendingMasterJoins = 0; synchronized (pendingJoinRequests) {<br><br>for (DiscoveryNodenode : pendingJoinRequests.keySet()) {<br><br>if (node.isMa terNode()) {<br><br>pendingMasterJoins++; }<br><br>} }</th>
  </tr>
</table>


- 1.
- 2.
- 3.

- a.
- b.
- c.


- 4.

- a.
- b.
- c.
- d.


- 5.
- 6.
- 7.

- a.
- b.
- c.


- 8.


Ping所有节点并获取PingResponse

pingService.pingAndWait(pingTimeout);

过滤有成为 Master 资格的节点 创建了三个列表

ActiveNodes: ping 结果 + localnode NodesJoinedAtleastOnceBefore: 如果以前已加⼊群集，则将其添加到此列表中(内 存已有 clusterState, ⾮磁盘中的),可能包含 localnode pingMasters: 主节点列表, ping 返回的节点中指示的 master 节点,正常是重复的同 ⼀个节点,不包含 localnode,因为可能会在没有任何其他节点的检查/验证的情况 下，在ZenDiscover # i nerJoinCluster()中进⾏选举

其中,joinedOnceActiveNodes.size <= activeNodes.size,差别在于是否含有 localnode, 其他 的内容都⼀样,都是来⾃ping 的结果.

如果pingMasters不为空,当前集群认为存在 Master 在这些pingMasters中选择主。 此列表不包含本地节点

如果 pingMasters 为空, 当前集群认为不存在 Master

⾸先在 NodesJoinedAtleastOnceBefore 中选举 如果没有选中，则在 ActiveNodes 上进⾏选举。

选举算法就是把 node 列表根据 nodeid排序,取第⼀个 现在已经选出了⼀个 master, 但只是临时的,准备向其投票 如果localnode被选为Master

等待⾮主节点的连接到⾜够数量(投票达到法定⼈数)，以完成选举

nodeJoinController.waitToBeElectedAsMaster

超时后还没有满⾜数量的join请求，则选举失败，需要新⼀轮选举 成功后发布新的 clusterState

如果其他节点被选为Master

- a.
- b.
- c.


停⽌累加连接 向 Master 发送加⼊请求,请求发送完毕就认为成功,⽆论 Master 如何处理.通过集群 状态更新线程完成连接

如果收到的clusterState 中, Master 不是之前选出的,则重新选举.

membership.sendJoinRequestBlocking(masterNode, clusterService.localNode(), joinTimeout);

clusterService.submitStateUpdateTask("finalize_join (" + masterNode

+ ")", new ClusterStateNonMasterUpdateTask()

# 什么时候触发选主?

- 1.
- 2.


集群启动 Master 失效

⾮ Master 节点运⾏的 MasterFaultDetection 检测到 Master 失效,在其注册的 listener 中执 ⾏ handleMasterGone,执⾏ rejoin 操作,重新选主.注意,即使⼀个节点认为 Master 失效也会 进⼊选主流程

# 为什么不⽤ zk?

elasticsearch 第⼀版发与2010,zk208,也许因为当时 zk 不流⾏?

# 如何获取最新数据

现在 Master 已成功当选,但是他未必有最新的 clusterState 信息,这些信息如何得到? gateway 模块负责 clusterState 持久化和恢复,Master 节点在当选后,会通过下⾯的流程获取 到集群最新 clusterState:

- 1. 枚举集群中有资格成为 Master 的节点列表
- 2. 通过listGatewayMetaState获取这些节点上存储的 clusterState
- 3. 对⽐这些节点的 clusterState 版本号,选择最新的作为 clusterState 并应⽤.


# ⼀个⼩问题

技术分析会议中有同学提出⼀个有趣的问题: 假设10台机器组成的集群产⽣⽹络分区,3台⼀组,7台⼀组,产⽣分区前, Master位于3台中的 ⼀个,此时7台1组的节点会重新并成功选取 Master, 这种情况如何处理? ES 对应的处理机制是这样的:

当有节点从集群离开时, Master 节点会检查⼀下当前集群总节点数是否具备法定节点数(过 半),如果不具备,他会重新加⼊集群,放弃 Master 资格,因此不会产⽣双主. 参考链接:

- 1.
- 2.
- 3.


htps:/ w.elastic.co/blog/found-leader-election-in-general htp:/ w.cnblogs.com/ziawanblog/p/657383.html htps:/ w.linkedin.com/pulse/elasticsearch-zen-discovery-explained-gaurav-kukal

相关⽂章:

- 1.
- 2.
- 3.


elasticsearch 写流程 elasticsearch 机制和架构

