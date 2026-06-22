第⼀部分：Storm的并⾏度 概念：Workers (JVMs): 在⼀个节点上可以运⾏⼀个或多个独⽴的JVM 进程。⼀个Topology可以包含 ⼀个或多个worker(并⾏的跑在不同的machine上), 所以worker proces就是执⾏⼀个topology的⼦集, 并且worker只能对应于⼀个topology Executors (threads): 在⼀个worker JVM进程中运⾏着多个Java 线程。⼀个executor线程可以执⾏⼀个或多个tasks。但⼀般默认每个executor只执⾏⼀个task。⼀个 worker可以包含⼀个或多个executor, 每个component (spout或bolt)⾄少对应于⼀个executor, 所以可 以说executor执⾏⼀个compenent的⼦集, 同时⼀个executor只能对应于⼀个 component。 Tasks(bolt/spout instances)：Task就是具体的处理逻辑对象，每⼀个Spout和Bolt会被 当作很多task在整个集群⾥⾯执⾏。每⼀个task对应到⼀个线程，⽽stream grouping则是定义怎么从 ⼀堆task发射tuple到另外⼀堆task。你可以调⽤TopologyBuilder.setSpout和TopBuilder.setBol来设置 并⾏度 — 也就是有多少个task。

配置并⾏度 对于并发度的配置, 在storm⾥⾯可以在多个地⽅进⾏配置, 优先级为： defaults.yaml < storm.yaml < topology-specific configuration < internal component-specific configuration < external component-specific configuration

worker proceses的数⽬, 可以通过配置⽂件和代码中配置, worker就是执⾏进程, 所以考虑并发的效 果, 数⽬⾄少应该⼤亍machines的数⽬ executor的数⽬, component的并发线程数，只能在代码中配置 (通过setBolt和setSpout的参数), 例如, setBolt("gren-bolt", new GrenBolt(), 2) tasks的数⽬, 可以不 配置, 默认和executor1 1, 也可以通过setNumTasks()配置

Topology的worker数通过config设置，即执⾏该topology的worker（java）进程数。它可以通过 storm rebalance 命令任意调整。

Config conf = newConfig(); conf.setNumWorkers(2); / use two worker proceses topolo Buil ersetSpout("blue-spout", newBlueSpout(), 2); / set paralelism hint to 2 topologyBuilder.setBolt("gren-bolt", newGrenBolt(), 2).setNumTasks(4).shufleGrouping("blu

4 topologyBuilder.setBolt("yelow-bolt", newYelowBolt(), 6).shufleGrouping("gren-bolt"); StormSubmiter.submitTopology("mytopology", conf, topologyBuilder.createTopology();

3个组件的并发度加起来是10，就是说拓扑⼀共有10个executor，⼀共有2个worker，每个worker 产⽣10 / 2 = 5条线程。 绿⾊的bolt配置成2个executor和4个task。为此每个executor为这个bolt运⾏2个task。

动态的改变并⾏度 Storm⽀持在不 restart topology 的情况下, 动态的改变(增减) worker proceses 的数⽬和 executors 的数⽬, 称为rebalancing. 通过Storm web UI，或者通过storm rebalance命令实现：

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


1 storm rebalance mytopology -n 5 -e blue-spout=3 -e yelow-bolt=10

第⼆部分：流分组策略 -Stream GroupingStream Grouping，告诉topology如何在两个组件之间发 送tuple ，定义⼀个topology的其中⼀步是定义每个bolt接收什么样的流作为输⼊。stream grouping就 是⽤来定义⼀个stream应该如果分配数据给bolts上⾯的多个tasks Storm⾥⾯有7种类型的stream grouping，你也可以通过实现CustomStreamGrouping接⼝来实现⾃定义流分组 Shufle Grouping 随 机分组，随机派发stream⾥⾯的tuple，保证每个bolt task接收到的tuple数⽬⼤致相同。 Fields Grouping 按字段分组，⽐如，按"user-id"这个字段来分组，那么具有同样"user-id"的 tuple 会被分到 相同的Bolt⾥的⼀个task， ⽽不同的"user-id"则可能会被分配到不同的task。 Al Grouping ⼴播发 送，对亍每⼀个tuple，所有的bolts都会收到 Global Grouping 全局分组，整个stream被分配到storm 中的⼀个bolt的其中⼀个task。再具体⼀点就是分配给id值最低的那个task。 None Grouping 不分组， 这个分组的意思是说stream不关⼼到底怎样分组。⽬前这种分组和Shufle grouping是⼀样的效果， 有⼀点不同的是storm会把使⽤none grouping的这个bolt放到这个bolt的订阅者同⼀个线程⾥⾯去执⾏ （如果可能的话）。 Direct Grouping 指向型分组， 这是⼀种⽐较特别的分组⽅法，⽤这种分组意味 着消息（tuple）的发送者指定由消息接收者的哪个task处理这个消息。只有被声明为 Direct Stream 的消息流可以声明这种分组⽅法。⽽且这种消息tuple必须使⽤ emitDirect ⽅法来发射。消息处理者可 以通过 TopologyContext 来获取处理它的消息的task的id (OutputColector.emit⽅法也会返回task的 id) Local or shufle grouping 本地或随机分组。如果⽬标bolt有⼀个或者多个task与源bolt的task在同 ⼀个⼯作进程中，tuple将会被随机发送给这些同进程中的tasks。否则，和普通的Shufle Grouping⾏ 为⼀致。

第三部分：消息的可靠处理机制总体介绍 在storm中，可靠的信息处理机制是从spout开始的。⼀个提供了可靠的处理机制的spout需要记录 他发射出去的tuple，当下游bolt处理tuple或者⼦tuple失败时spout能够重新发射。

Storm通过调⽤Spout的nextTuple()发送⼀个tuple。为实现可靠的消息处理，⾸先要给每个发出的 tuple带上唯⼀的ID，并且将ID作为参数传递给SoputOutputColector的emit()⽅法： colector.emit(new Values("value1","value2"), msgId); 给tuple指定ID告诉Storm系统，⽆论处理成功 还是失败，spout都要接收tuple树上所有节点返回的通知。

如果处理成功，spout的ack()⽅法将会对编号是msgId的消息应答确认；如果处理失败或者超时， 会调⽤fail()⽅法。

可以总结为两个步骤：

当发射衍⽣的tuple时，需要锚定读⼊的tuple；当处理消息成功或失败时分别确认应答或者报错。 术语解释：锚定⼀个tuple的意思是，建⽴读⼊tuple和衍⽣出的tuple之间的对应关系，这样下游的 bolt就可以通过应答确认、报错或超时来加⼊到tuple树结构中。可以通过调⽤OutputColector的 emit()的⼀个重载函数锚定⼀个或⼀组tuple：colector.emit(tuple, new Values(word) ⾮锚定（colector.emit(new Values(word);）的tuple不会对数据流的可靠性起作⽤。如果⼀个⾮ 锚定的tuple在下游处理失败，原始的根tuple不会重新发送。 超时时间可以通过任务级参数Config.TOPOLOGY_MESAGE_TIMEOUT_SECS进⾏配置，默认超 时值为30秒。

基本原理

Storm 系统中有⼀组叫做"acker"的特殊的任务，它们负责跟踪DAG（有向⽆环图）中的每个消 息。acker任务保存了spout消息id到⼀对值的映射。第⼀个值就是spout的任务id，通过这个id， acker就知道消息处理完成时该通知哪个spout任务。第⼆个值是⼀个64bit的数字，我们称之 为"ack val"， 它是树中所有消息的随机id的异或计算结果。 ack val表示了整棵树的的状态，⽆论这棵树多⼤，只需要这个固定⼤⼩的数字就可以跟踪整棵 树。

当消息被创建 和被应答的时候都会有相同的消息id发送过来做异或。 每当acker发现⼀棵树的 ack val值为0的时候，它就知道这棵树已经被完全处理了。因为消息的随机ID是⼀个64bit的 值，因此ack val在树处理完之前被置为0的概率⾮常⼩。假设你每秒钟发送⼀万个消息，从概 率上说，⾄少需要50, 0, 0年才会有机会发⽣⼀次错误。即使如此，也只有在这个消息确 实处理失败的情况下才会有数据的丢失！

有三种⽅法可以去掉消息的可靠性： 将参数Config.TOPOLOGY_ACKERS设置为0，通过此⽅法，当 Spout发送⼀个消息的时候，它的ack⽅法将⽴刻被调⽤； Spout发送⼀个消息时，不指定此消息的 mesageID。当需要关闭特定消息可靠性的时候，可以使⽤此⽅法； 最后，如果你不在意某个消息派 ⽣出来的⼦孙消息的可靠性，则此消息派⽣出来的⼦消息在发送时不要做锚定，即在emit⽅法中不指 定输⼊消息。因为这些⼦孙消息没有被锚定在任何tuple tre中，因此他们的失败不会引起任何spout 重新发送消息。 第四部分：Storm nimbus⽬录树& supervisor⽬录树 ⽬录结构⾥⾯， nimbus机器上⾯只有/nimbus⽬录;⽬录结构⾥⾯，supervisor机器上⾯只 有/supervisor⽬录和/workers⽬录。

第五部分：Strom zokeper⽬录树

第六部分：Storm任务提交过程及任务分配过程 Topology运⾏流程Storm提交后，会把代码⾸先存放到Nimbus节点的inbox⽬录下，之后，会把当前 Storm运⾏的配置⽣成⼀个 stormconf.ser⽂件放到Nimbus节点的stormdist⽬录中，在此⽬录中同时 还有序列化之后的Topology代码⽂件 在设定Topology所关联的Spouts和Bolts时，可以同时设置当前 Spout和Bolt的executor数⽬和task数⽬，默认情况下， ⼀个Topology的task的总和是和executor的总 和⼀致的。之后，系统根据worker的数⽬，尽量平均的分配这些task的执⾏。 worker在哪个 supervisor节点上运⾏是由storm本身决定的 任务分配好之后，Nimbus节点会将任务的信息提交到 zokeper集群，同时在zokeper集群中会有workerbeats节点，这⾥存储了当前Topology的所有 worker进程的⼼跳信息 Supervisor 节点会不断的轮询zokeper集群，在zokeper的asignments节 点中保存了所有Topology的任务分配信息、代码存储⽬ 录、任务之间的关联关系等，Supervisor通过 轮询此节点的内容，来领取⾃⼰的任务，启动worker进程运⾏ ⼀个Topology运⾏之后，就会不断的通 过Spouts来发送Stream流，通过Bolts来不断的处理接收到的Stream流，Stream流是⽆界的。 最后⼀ 步会不间断的执⾏，除⾮⼿动结束Topology。

第7部分：storm通信机制

在Storm中，worker进程内部的thread通信与worker进程间的通信有⼀些差别，worker间的通信经常需要通过⽹ 络跨节点进⾏，Storm使⽤ZeroMQ或Nety(0.9以后默认使⽤)作为进程间通信的消息框架。worker进程内部通信 或在同⼀个物理节点的不同worker的thread通信使⽤LMAX Disruptor来完成。

同⼀worker间消息的发送使⽤的是LMAX Disruptor，它负责同⼀节点（同⼀进程内）上线程间的通 信；

Disruptor使⽤了⼀个RingBufer替代队列，⽤⽣产者消费者指针替代锁。 ⽣产者消费者指针使⽤CPU⽀持的整数⾃增，⽆需加锁并且速度很快。Java的实现在Unsafe package中。

不同worker间通信使⽤ZeroMQ（0.8）或Nety（0.9.0）；

不同topologey之间的通信，Storm不负责，我们需要⾃⼰想办法实现，例如使⽤kafka等；

第⼀部分：分析storm的worker进程间消息传递机制，消息的接收和处理的⼤概流程⻅下图

对于worker进程来说，为了管理流⼊和传出的消息，每个worker进程有⼀个独⽴的接收线程(对配置的 TCP端⼝supervisor.slots.ports进⾏监听)。

接收线程将收到的消息传递给对应的executor(⼀个或多个)的incoming-queues。参数 topology.receiver.bufer.size代表接收线程⼀次最多能接收多少条消息，⽤户可以⾃定义配置。

对应接收线程，每个worker存在⼀个独⽴的发送线程，它负责从worker的transfer-queue中读取消 息，并通过⽹络发送给其他worker

transfer-queue的⼤⼩由参数topology.transfer.bufer.size来设置。transfer-queue的每个元素实 际上代表⼀个tuple的集合，当executor的outgoing-queue中的tuple达到⼀定的阀值，executor的 发送线程将批量获取outgoing-queue中的tuple,并发送到transfer-queue中。

每个worker进程控制⼀个或多个executor线程，⽤户可在代码中进⾏配置。每个executor有⾃⼰的 incoming-queue和outgoing-queue。⼀个worker进程运⾏⼀个专⽤的接收线程来负责将外部发送过来 的消息移动到对应的executor线程的incoming-queue中，executor中的发送线程在outgoing-queue到 达⼀定的阀值后，将outgoing-queue中的消息批量发送给所在worker的transfer-queue。executor的 incoming-queue和outgoing-queue的⼤⼩⽤户可以⾃定义配置。每个executor有单独的线程分别来处 理spout/bolt的业务逻辑和从outgoing-queue消费数据并发送到transfer-queue中。

# 步骤⼀

监听端⼝准备就绪，接收线程在收到外部的消息后，⾯临的问题就是如何确定由哪个task来处理该消 息。接收到的tuple中含有task-id，根 据task-id可以知道运⾏该task的executor，executor中有 receive-mesage-queue即(incoming queue)来存放外部的tuple. 定义的数据结构需要反映这个转换过 程task-id->executor->receive-queue-map. 那么在worker-data中哪些数据项与这个过程相关呢

- 1.
- 2.
- 3.
- 4.
- 5.


:port :executor-receive-queue-map :short-executor-receive-queue-map :task->short-executor :transfer-local-fn

transfer-local-fn将数据从接收线程发送到spout或bolt所在的executor线程。

# 步骤⼆

接下来数据会被传递到executor，于是⼜牵涉到executor的数据结构问题。executor-data由函数mkexecutor-data创建，其内容与worker-data⽐较起来相对较少。 executor收到tuple之后，第⼀步需要进⾏反序列化，storm中使⽤kyro来进⾏序列化和反序列化，这也 是为什么在executor中有该数据项的原因。

- executor中与步骤2相关的数据项


1.

:type executor-type

- 2.
- 3.


:receive-queue :deserializer (executor-data中的数据项)

# 步骤三：

步骤2处理结束，会产⽣相应的tuple发送到外部。这个过程需要多解释⼀下，⾸先tuple不是直接发送 给worker的transfer- thread(负责向其它进程发送消息），⽽是发送给send-handler线程，每⼀个 executor在创建的时候最起码会有两个线程被创建，⼀个 ⽤于运⾏bolt或spout的处理逻辑，另⼀个⽤ 以负责缓存bolt或spout产⽣的对外发送的tuple。 ⼀旦snd-hander中的tuple数量达到阀值，这些被缓存的tuple会⼀次性发送给worker级别的transferthread.

- executor中与步骤3相关的数据项


- 1.
- 2.


:transfer-fn (mk-executor-transfer-fn batch-transfer->worker) :batch-transfer-queue

在步骤3中⽣成outgoing的tuple，tuple⽣成的时候需要回答两个基本问题

- 1.
- 2.


tuple中含有哪些字段 - 该问题的解答由spout或bolt中的declareOutFields来解决 由哪个node+port来接收该tuple- 由grouping来解决，这个时候就可以看出为什么需要task这⼀ 层的逻辑抽象了，有关grouping的详细解释，请参考fxjwind撰写的Storm-源码分析-Streaming Gr ouping (backtype.storm.daemon.executor)

步骤四：

处理逻辑很简单，先将数据缓存，然后在达到阀值之后，⼀起传送给transfer-thread.

第⼆部分：worker的thread通信使⽤LMAX Disruptor来完成

