这三种Node你晓得伐

Node Name Node Role 看不懂英⽂？ 配置（其他配成false） Master-eligible node A node that has node.master set to true (default), which makes it eligible to be elected as the master node, which controls the cluster. 主节点：负责集群层⾯的相关操 作，管理集群变更。尽可能做少量的⼯作，⽣产环境应该尽量分离主节点和数据节点。 node.master: true Data node A node that has node.data set to true (default). Data nodes hold data and perform data related operations such as CRUD, search, and agregations. 数据节点：负责保存数据、执 ⾏数据相关操作：CRUD、搜索、聚合等。对CPU、内存、IO要求较⾼。⼀般情况下，数据读写流程只 和数据节点交互，不会和主节点打交道（异常情况除外）。 node.data: true Ingest node A node that has node.ingest set to true (default). Ingest nodes are able to aply an ingest pipeline to a document in order to transform and enrich the document before indexing. With a heavy ingest load, it makes sense to use dedicated ingest nodes and to mark the master and data nodes as node.ingest: false. 预处理节点：从5.0引⼊的概念。通过定义⼀系列的procesors处理器 和pipeline管道，对数据进⾏某种转换、富化。 node.ingest: true Master节点的特殊性

ES中有⼀项⼯作是Master独有的：维护集群状态。集群状态信息，只由Master节点进⾏维护，并且同 步到集群中所有节点，其他节点只负责接收从Master同步过来的集群信息⽽没有维护的权利。集群状 态包括以下信息：

集群层⾯的配置 集群内有哪些节点 各索引的设置，映射，分析器和别名等 索引内各分⽚所在的节点位置 【思维拓展】ES集群中的每个节点都会存储集群状态，知道索引内各分⽚所在的节点位置，因此在整 个集群中的任意节点都可以知道⼀条数据该往哪个节点分⽚上存储。反之也知道该去哪个分⽚读。所 以，Elasticsearch不需要将读写请求发送到Master节点，任何节点都可以作为数据读写的切⼊点对请 求进⾏响应。这样进⼀步减轻了Master节点的⽹络压⼒，同时提⾼了集群的整体路由性能。 主从模式 VS. ⽆主模式

分布式系统的集群⽅式⼤致可以分为主从模式（Master-Slave）和⽆主模式。

模式 代表组件 优点 缺点 主从模式 ES/HDFS/HBase 简化系统设计，Master作为权威节点，负责维护集群原信息。 Master节点存在单点故障，需要解决在被问题，并且集群规模会受限于Master节点的管理能⼒。 ⽆主模式 Casandra 分布式哈希表（DHT），⽀持每⼩时数千个节点的离开和加⼊。集群没有 master的概念，所有节点都是同样的⻆⾊，彻底避免了整个系统的单点问题导致的不稳定性。 多个 节点可能操作同⼀条数据，数据⼀致性上可能⽐较难以保证。 为什么主从模式更适合ES

ES的典型场景中的节点数没有那么多（⽬前官⽅推荐是⼏百节点）。⼀般情况下，节点的数量远远⼩ 于单个节点能够维护的连接数，并且⽹络环境下不必经常处理节点的加⼊和离开。这就是为什么主从 模式更加适合ES。

主节点的选举机制

【举个栗⼦】通常⼀个HBase集群存在多个HMaster节点（有资格成为Active HMaster），每个节点都 会向ZoKeper注册，在正常情况下有且仅有⼀个节点会成为Active Master，其余都为Backup Master。它们将⼀直处于阻塞状态，直⾄/hbase/master节点发⽣delete事件，当Zokeper Watcher 监听到此事件，回唤醒阻塞的Backup Master再次去/master节点注册，如果注册成功就会成为Active HMaster，对外提供服务；如果注册失败，说明已经有节点注册成功，就只能再次阻塞等待被唤醒。 Elasticsearch不像Solr，HDFS和HBase依赖于ZoKeper，Elasticsearch⾃⼰有⼀套选举机制来保证 集群的协同服务。

Buly算法 Leader选举的基本算法之⼀，优点是易于实现，该算法和Solr Leader Shard选举⾮常相似。 该算法假定所有节点都有⼀个唯⼀的ID，使⽤该ID对节点进⾏排序，选择最⼩的节点作为Master。参 考ElectMasterService的函数electMaster 但是节点处于不稳定状态下会出问题，⽐如Master负载过重⽽假死（推迟选举解决假死 + 法定得票过 半解决脑裂）。 防⽌脑裂、防⽌数据丢失的极其重要的参数： discovery.zen.minimum_master_nodes=(master_eligible_nodes)/2+1 这个参数的实际作⽤早已超越了其表⾯的含义（那建议换⼀个更霸⽓侧漏的名字以彰显其重要性）， 会⽤于⾄少以下多个重要时机的判断：

- 1. 触发选主：进⼊选举临时的Master之前，参选的节点数需要达到法定⼈数。
- 2. 决定Master：选出临时的Master之后，得票数需要达到法定⼈数，才确认选主成功。
- 3. gateway选举元信息：向有Master资格的节点发起请求，获取元数据，获取的响应数量必须达到法 定⼈数，也就是参与元信息选举的节点数。


- 4. Master发布集群状态：成功向节点发布集群状态信息的数量要达到法定⼈数。
- 5. NodesFaultDetection事件中是否触发rejoin：当发现有节点连不上时，会执⾏removeNode。接着 审视此时的法定⼈数是否达标（discovery.zen.minimum_master_nodes），不达标就主动放弃Master 身份执⾏rejoin以避免脑裂。 Master扩容场景：⽬前有3个master_eligible_nodes，可以配置quorum为2。如果将 master_eligible_nodes扩容到4个，那么quorum就要提⾼到3。此时需要先把 discovery.zen.minimum_master_nodes配置设置为3，再扩容Master节点。这个配置可以动态设置： PUT /_cluster/setings { “persistent”: { “discovery.zen.minimum_master_nodes”: 3 } } Master减容场景：缩容与扩容是完全相反的流程，需要先缩减Master节点，再把quorum数降低。 修改Master以及集群相关的配置⼀定要⾮常谨慎！配置错误很有可能导致脑裂，甚⾄数据写坏、数据 丢失等场景。 注意：最新版本ES 7已经移除minimum_master_nodes配置，让Elasticsearch⾃⼰选择可以形成仲裁 的节点。 Paxos算法 ⾮常强⼤，选举的灵活性⽐简单的Buly算法有很⼤的优势。但Paxos实现起来⾮常复杂。 流程解析


【举个栗⼦】节点启动场景 Node.java -> start() ZenDiscovery.java -> startInitialJoin() -> i nerJoinCluster()

ping所有节点，并获取PingResponse返回结果（findMaster） 过滤出具有Master资格的节点（filterPingResponses） 选出临时Master。根据PingResponse结果构建两个列表：activeMasters和masterCandidates。

- – 如果activeMasters⾮空，则从activeMasters中选择最合适的作为Master；
- – 如果activeMasters为空，则从masterCandidates中选举，结果可能选举成功，也可能选举失败。 判断临时Master是否是本节点。
- – 如果临时Master是本节点：则等待其他节点选我，默认30秒超时，成功的话就发布新的 clusterState。（当选总统候选⼈，只等选票过半了）
- – 如果临时Master是其他节点：则不再接受其他节点的join请求，并向Master节点发送加⼊请求。（没 资格选举，就只能送⼈头了）


private DiscoveryNode findMaster() { loger.trace("starting to ping"); List<ZenPing.PingResponse> fulPingResponses = pingAndWait(pingTimeout).toList(); / ping

所有节点，并获取返回结果

if (fulPingResponses = nul) { loger.trace("No ful ping responses"); return nul;

} if (loger.isTraceEnabled() {

StringBuilder sb = new StringBuilder(); if (fulPingResponses.size() = 0) {

sb.apend(" {none}"); } else {

for (ZenPing.PingResponse pingResponse : fulPingResponses) {

sb.apend("\n\t-> ").apend(pingResponse); }

} loger.trace("ful ping responses:{}", sb);

}

final DiscoveryNode localNode = transportService.getLocalNode();

/ ad our selves asert fulPingResponses.stream().map(ZenPing.PingResponse:node)

.filter(n -> n.equals(localNode).findAny().isPresent() = false;

fulPingResponses.ad(new ZenPing.PingResponse(localNode, nul, this.clusterState( );

/ filter responses 选出具有Master资格的节点

final List<ZenPing.PingResponse> pingResponses = filterPingResponses(fulPingResponses, masterElectionIgnoreNonMasters, loger);

List<DiscoveryNode> activeMasters = new ArayList<>(); for (ZenPing.PingResponse pingResponse : pingResponses) {

/ We can't include the local node in pingMasters list, otherwise we may up electing ourselves without

/ any check / verifications from other nodes in ZenDiscover#i nerJoinCluster()

if (pingResponse.master() != nul & !localNode.equals(pingResponse.master( ) {

activeMasters.ad(pingResponse.master(); }

}

/ nodes discovered during pinging List<ElectMasterService.MasterCandidate> masterCandidates = new ArayList<>(); for (ZenPing.PingResponse pingResponse : pingResponses) {

if (pingResponse.node().isMasterNode() {

masterCandidates.ad(new ElectMasterService.MasterCandidate(pingResponse.node(), pingResponse.getClusterStateVersion( );

} }

if (activeMasters.isEmpty() { if (electMaster.hasEnoughCandidates(masterCandidates) { final ElectMasterService.MasterCandidate wi ner =

electMaster.electMaster(masterCandidates); loger.trace("candidate {} won election", wi ner); return wi ner.getNode();

} else {

/ if we don't have enough master nodes, we bail, because there are not enough master to elect from

loger.warn("not enough master nodes discovered during pinging (found [{}], but neded [{}]), pinging again",

masterCandidates, electMaster.minimumMasterNodes(); return nul;

} } else { asert !activeMasters.contains(localNode) :

"local node should never be elected as master when other nodes indicate an active master";

/ lets tie break betwen discovered nodes

return electMaster.tieBreakActiveMasters(activeMasters); }

}

/*

- * the main function of a join thread. This function is guaranted to join the cluster
- * or spawn a new join thread upon failure to do so.
- */ private void i nerJoinCluster() {


DiscoveryNode masterNode = nul; final Thread curentThread = Thread.curentThread(); nodeJoinControler.startElectionContext(); while (masterNode = nul & joinThreadControl.joinThreadActive(curentThread) {

masterNode = findMaster(); }

if (!joinThreadControl.joinThreadActive(curentThread) { loger.trace("thread is no longer in curentJoinThread. Stoping."); return;

}

if (transportService.getLocalNode().equals(masterNode) {/ 如果是本节点当选为Master

final int requiredJoins = Math.max(0, electMaster.minimumMasterNodes() - 1); / we count as one

loger.debug("elected as master, waiting for incoming joins ([{}] neded)", requiredJoins);

nodeJoinControler.waitToBeElectedAsMaster(requiredJoins, masterElectionWaitForJoinsTimeout, / （1）等待其他节点的投票数超过requiredJoins数（即为 discovery.zen.minimum_master_nodes配置数）。

new NodeJoinControler.ElectionCalback() { @Overide public void onElectedAsMaster(ClusterState state) {/ （2）成功选举⾃⼰

为Master之后，发送集群状态到所有节点 synchronized (stateMutex) {

joinThreadControl.markThreadAsDone(curentThread); }

}

@Overide

public void onFailure(Throwable t) {/ （3）如果等待超时后投票数没有超

过半数，则认为选举失败，重新开始 loger.trace("failed while waiting for nodes to join, rejoining", t); synchronized (stateMutex) {

joinThreadControl.markThreadAsDoneAndStartNew(curentThread);

} }

}

); } else {/ 如果是其他节点当选为Master / proces any incoming joins (they wil fail because we are not the master)

nodeJoinControler.stopElectionContext(masterNode + " elected"); / （1）不再接受其他 节点的join请求。

/ send join request

final bolean suces = joinElectedMaster(masterNode); / （2）向Master发送请求，申 请加⼊集群。最终当选的Master会先发布集群状态，才确认客户的join请求。

synchronized (stateMutex) { if (suces) {

DiscoveryNode curentMasterNode = this.clusterState().getNodes().getMasterNode();

if (curentMasterNode = nul) {/ 检查收到的集群状态中的Master节点如果为 空，则重新选举。

/ Post 1.3.0, the master should publish a new cluster state before acking our join request. we now should have

/ a valid master.

loger.debug("no master node is set, despite of join request completing. retrying pings.");

joinThreadControl.markThreadAsDoneAndStartNew(curentThread); } else if (curentMasterNode.equals(masterNode) = false) {/ 检查当选的

Master是不是之前选择的节点，不符合的话则重新选举。 / update cluster state

joinThreadControl.stopRuningThreadAndRejoin("master_switched_while_finalizing_join");

}

joinThreadControl.markThreadAsDone(curentThread); } else {/ 获取集群状态，如果集群状态中与选择的Master不⼀致，则重新开始 / failed to join. Try again.

joinThreadControl.markThreadAsDoneAndStartNew(curentThread); }

} }

}

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10


- 1

- 12
- 13
- 14
- 15
- 16
- 17
- 18
- 19
- 20
- 21


- 2


- 23
- 24
- 25
- 26
- 27


- 28
- 29
- 30
- 31
- 32


- 3

- 34
- 35
- 36
- 37
- 38
- 39
- 40
- 41
- 42
- 43


- 4

- 45
- 46
- 47
- 48
- 49
- 50
- 51
- 52
- 53
- 54


- 5


- 56
- 57
- 58
- 59
- 60
- 61
- 62
- 63
- 64


- 65


- 6


67 /*

- * checks if there is an on going request to become master and if it has enough pending joins. If so, the node wil
- * become master via a ClusterState update task.
- */ private synchronized void checkPendingJoinsAndElectIfNeded() {/ waitToBeElectedAsMaster等 待时间结束，检查投票数是否⾜够。


asert electionContext != nul : "election check requested but no active context"; final int pendingMasterJoins = electionContext.getPendingMasterJoinsCount(); if (electionContext.isEnoughPendingJoins(pendingMasterJoins) = false) {/ 选票不够，需要

进⾏新⼀轮选举。 if (loger.isTraceEnabled() {

loger.trace("not enough joins for election. Got [{}], required [{}]", pendingMasterJoins,

electionContext.requiredMasterJoins); }

} else {/ 票数过半，即将成为Master。 if (loger.isTraceEnabled() {

loger.trace("have enough joins for election. Got [{}], required [{}]", pendingMasterJoins,

electionContext.requiredMasterJoins);

} electionContext.closeAndBecomeMaster(); electionContext = nul; / clear this out so future joins won't be acumulated

} }

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


- 9
- 10 1


- 12
- 13
- 14
- 15
- 16
- 17
- 18
- 19
- 20
- 21 public synchronized void adIncomingJoin(DiscoveryNode node, MembershipAction.JoinCalback calback) {


ensureOpen(); joinRequestAcumulator.computeIfAbsent(node, n -> new ArayList<>().ad(calback);

}

/ 查看投票数是否已经⾜够，标准是达到requiredMasterJoins数（即为 discovery.zen.minimum_master_nodes配置数） public synchronized bolean isEnoughPendingJoins(int pendingMasterJoins) {

final bolean hasEnough; if (requiredMasterJoins < 0) {

/ requiredMasterNodes is unknown yet, return false and kep on waiting hasEnough = false;

} else { asert calback != nul : "requiredMasterJoins is set but not the calback"; hasEnough = pendingMasterJoins >= requiredMasterJoins;

} return hasEnough;

}

private Map<DiscoveryNode, ClusterStateTaskListener> getPendingAsTasks() { Map<DiscoveryNode, ClusterStateTaskListener> tasks = new HashMap<>(); joinRequestAcumulator.entrySet().stream().forEach(e -> tasks.put(e.getKey(), new

JoinTaskListener(e.getValue(), loger );

return tasks; }

/ 统计各个候选⼈的得票数，如果被推选为Master，则pendingMasterJoins⾃增1。

public synchronized int getPendingMasterJoinsCount() { int pendingMasterJoins = 0; for (DiscoveryNode node : joinRequestAcumulator.keySet() {

if (node.isMasterNode() {

pendingMasterJoins+; }

} return pendingMasterJoins;

}

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10


- 1

- 12
- 13
- 14
- 15
- 16
- 17
- 18
- 19
- 20
- 21


- 2


- 23
- 24


- 25
- 26
- 27
- 28
- 29
- 30
- 31
- 32


- 3


34 节点失效检测

选主流程之后不可或缺的步骤，不执⾏失效检测可能会产⽣脑裂现象。 定期（默认为1s）发送ping请求探测节点是否正常，当失败达到⼀定次数（默认为3次），或者收到节 点的离线通知时，开始处理节点离开事件。 我们需要启动两种失效探测器：

NodesFaultDetection：在Master节点启动，简称NodesFD。定期探测加⼊集群的节点是否活跃。当 有节点连不上时，会执⾏removeNode。然后需要审视此时的法定⼈数是否达标（没错！⽼坛酸菜⽜⾁ ⾯，仍然是那个熟悉的配⽅：discovery.zen.minimum_master_nodes），不达标就主动放弃Master身 份执⾏rejoin以避免脑裂。 @Overide public ClusterTasksResult<Task> execute(final ClusterState curentState, final List<Task> tasks) throws Exception {

final DiscoveryNodes.Builder remainingNodesBuilder =

DiscoveryNodes.builder(curentState.nodes(); bolean removed = false; for (final Task task : tasks) {

if (curentState.nodes().nodeExists(task.node( ) { remainingNodesBuilder.remove(task.node(); removed = true;

} else {

loger.debug("node [{}] does not exist in cluster state, ignoring", task); }

}

if (!removed) {

/ no nodes to remove, kep the curent cluster state

return ClusterTasksResult.<Task>builder().suceses(tasks).build(curentState); }

final ClusterState remainingNodesClusterState = remainingNodesClusterState(curentState, remainingNodesBuilder);

final ClusterTasksResult.Builder<Task> resultBuilder = ClusterTasksResult. <Task>builder().suceses(tasks);

if (electMasterService.hasEnoughMasterNodes(remainingNodesClusterState.nodes() = false) {

final int masterNodes = electMasterService.countMasterNodes(remainingNodesClusterState.nodes();

rejoin.acept(LogerMesageFormat.format("not enough master nodes (has [{}], but neded [{}])",

masterNodes, electMasterService.minimumMasterNodes( );

return resultBuilder.build(curentState); } else {

ClusterState ptasksDisasociatedState = PersistentTasksCustomMetaData.disasociateDeadNodes(remainingNodesClusterState);

return resultBuilder.build(alocationService.disasociateDeadNodes(ptasksDisasociatedState, true, describeTasks(tasks);

} }

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10


- 1


- 12
- 13
- 14
- 15
- 16
- 17
- 18
- 19
- 20
- 21


- 2


- 23
- 24
- 25
- 26
- 27
- 28
- 29
- 30
- 31 MasterFaultDetection：在⾮Master节点启动，简称MasterFD。定期探测Master节点是否活跃， Master下线则触发rejoin重新选举。 private void handleMasterGone(final DiscoveryNode masterNode, final Throwable cause, final String reason) {


if (lifecycleState() != Lifecycle.State.STARTED) { / not started, ignore a master failure return;

} if (localNodeMaster() {

/ we might get this on both a master teling us shuting down, and then the disconect failure

return; }

loger.info() -> new ParameterizedMesage("master_left [{}], reason [{}]", masterNode, reason), cause);

synchronized (stateMutex) { if (localNodeMaster() = false & masterNode.equals(comitedState.get().nodes().getMasterNode( ) {

/ flush any pending cluster states from old master, so it wil not be set as master again

pendingStatesQueue.failAlStatesAndClear(new ElasticsearchException("master left [{}]", reason);

rejoin("master left (reason = " + reason + ")"); }

} }

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10 1


- 12
- 13
- 14
- 15
- 16
- 17
- 18
- 19
- 20 触发选举的时机


集群启动，从⽆主状态到产⽣新主时 集群在正常运⾏过程中，Master探测到节点离开时（NodesFaultDetection） 集群在正常运⾏过程中，⾮Master节点探测到Master离开时（MasterFaultDetection） 遗留问题

集群状态信息的同步⽅式是怎么样的？ ES官⽅推荐⼏百节点 -> 我们期望⼤集群，怎么拓展Master的管理能⼒？ Master扩容场景资料优化 Master扩容是否会触发选主？只扩容Master节点不会触发选主，只要当前设置的法定⼈数不变，ES集 群就认为⾃⼰的选举是合法的。 现⽹遇到Master⻓时间⽆法选出，根因未知。⽬前是靠重启所有Master节点来规避。最新版本ES 7已 经移除minimum_master_nodes配置。 ES实例管理界⾯添加Master主备显示？如果有查看Master的必要，可以加上主备信息的显示。 配置discovery.zen.minimum_master_nodes修改完之后，Master重启，需要重启普通节点吗？ ⸺ 动态⽣效！！！推荐！可以动态⽣效，为什么要重启节点呢？ Reference

Solr选主流程 ES Master机制及脑裂分析 Elasticsearch Reference [7.0] » Modules » Node Elasticsearch 7.0中引⼊的新集群协调⼦系统如何使⽤？ Leader Election, Why Should I Care? 开源分布式NoSQL数据库系统⸺Casandra 分布式数据库的取舍⸺Casandra的选择及其后果

⸻版权声明：本⽂为CSDN博主「⼩肥⻢」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上原⽂ 出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/weixin_4257250/article/details/9023017

