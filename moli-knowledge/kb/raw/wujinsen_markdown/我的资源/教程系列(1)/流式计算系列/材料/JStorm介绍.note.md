# ⼀、简介

Storm是开源的分布式容错实时计算系统，⽬前被托管在 上，遵循 Eclipse Public License 1.0。 最初由BackType开发，现在已被Twiter收⼊麾下。Storm最新版本是Storm 0.9，核⼼采⽤Clojure实 现。Storm为分布式实时计算提供了⼀组通⽤原语，可被⽤于“流处理”之中，实时处理消息；Storm也 可被⽤于“连续计算”（continuous computation），对数据流做连续处理，在计算时就将结果以流的形 式输出给⽤户；它还可被⽤于“分布式RPC”，以并⾏的⽅式执⾏运算。 Storm主要特点如下：

GitHub

- 0、简单的编程模型。类似于MapReduce降低了并⾏批处理复杂性，Storm降低了实时处理的复杂性。
- 1、语⾔⽆关。Storm的消息处理组件可以⽤任何语⾔来定义。默认⽀持Clojure、Java、Ruby和 Python。要增加对其他语⾔的⽀持，只需实现⼀个简单的Storm通信协议即可。
- 2、容错性。如果在消息处理过程中出了⼀些异常，Storm会重新调度出问题的处理逻辑。Storm保证 ⼀个处理单元永远运⾏，除⾮显式杀掉。
- 3、可伸缩性。Storm的可伸缩性可以使其每秒处理的消息量达到很⾼。为了扩展⼀个实时计算任务， 需要做的就是增加节点并且提⾼计算任务的并⾏度设置(paralelism seting)。Storm应⽤在10个节点的 集群上每秒可以处理⾼达1 0个消息，包括每秒⼀百多次的数据库调⽤[5]。同时Storm使⽤ ZoKeper来协调集群内的各种配置使得Storm的集群可以很容易扩展。
- 4、保证⽆数据丢失。实时系统必须保证所有的数据被成功的处理。 那些会丢失数据的系统的适⽤场 景⾮常窄，⽽Storm保证每⼀条消息都会被处理。
- 5、适⽤场景⼴泛。消息流处理、持续计算、分布式⽅法调⽤等是Storm适⽤场景⼴泛的基础，Storm 的这些基础原语可以满⾜⼤量的场景。 虽然Storm具备诸多优势，但也存在不⾜：


- 0、Storm⽬前还存在Nimbus SPOF的问题；
- 1、存在雪崩问题；
- 2、资源粒度较粗；
- 3、Clojure实现引⼊了学习成本； 为此，阿⾥巴巴中间件团队⽤Java重新实现了类Storm的JStorm，同样被托管在 上，遵循 Eclipse Public License 1.0，⽬前版本0.9.3。相关资料显示，阿⾥巴巴内部已经⼤规模部署了 Storm/JStorm集群。 JStorm继承了Storm的所有优点，同时与Storm相⽐JStorm所特有的如下特点：


GitHub

- 0、兼容Storm接⼝。开发者在Storm上运⾏的程序⽆需任何修改即可运⾏在JStorm上。
- 1、Nimbus HA。解决了Storm的Nimbus单点问题，⽀持⾃动热备切换Nimbus。
- 2、更细粒度的资源划分。JStorm从CPU、MEMORY、DISK和NET四个维度进⾏任务调度，同时不存 在任务抢占问题。
- 3、可定制的任务调度机制。（Storm的任务调度⽬前也可定制）
- 4、更好的性能。通过底层ZeroMQ和Nety使JStorm具有更好的性能，同时具有更好的稳定性。
- 5、解决了Storm的雪崩问题。通过Nety和disruptor机制实现RPC保证可以匹配的数据发送和接收速 度避免雪崩问题。


此外，JStorm通过减少对zokeper的访问量、增加反序列化线程、优化ACK、增加监控内容及JAVA 本身优势等各个⽅⾯优化了Storm的性能和稳定性。总之，

JStorm⽐Storm更强⼤、更稳定、性能更 好

。 （本⽂后⾯所述关于JStorm的部分内容同样适⽤Storm）

# ⼆、数据模型

JStorm通过⼀系列基本元素实现实时计算的⽬标，其中包括了Topology、Stream、Spout、Bolt等 等。JStorm在模型上和MapReduce有很多相似的地⽅，下表从不同维度对JStorm和MapReduce进⾏ 了⽐较。

<table>
  <tr>
    <th> </th>
    <th>MapReduce</th>
    <th>JStorm</th>
  </tr>
  <tr>
    <td rowspan="3">Role</td>
    <td>JobTracker</td>
    <td>Nimbus</td>
  </tr>
  <tr>
    <td>TaskTracker</td>
    <td>Supervisor</td>
  </tr>
  <tr>
    <td>Child</td>
    <td>Worker</td>
  </tr>
  <tr>
    <td>Application</td>
    <td>Job</td>
    <td>Topology</td>
  </tr>
  <tr>
    <td>Interface</td>
    <td>Mapper/Reducer</td>
    <td>Spout/Bolt</td>
  </tr>
</table>


实时计算任务需要打包成Topology提交，和MapReduce Job相似，不同的是，MapReduce Job在计算 完成后结束，⽽JStorm的Topology任务⼀旦提交永远不会结束，除⾮显式停⽌。 计算任务Topology是由不同的Spout和Bolt通过Stream连接起来的DAG图。下⾯是⼀个典型Topology 的结构示意图：

其中： Spout：JStorm的消息源。⽤于⽣产消息，⼀般是从外部数据源（如MQ/RDBMS/NoSQL/RTLog等） 不间断读取数据并向下游发送消息。 Bolt：JStorm的消息处理者。⽤于为Topology进⾏消息处理，Bolt可以执⾏查询、过滤、聚合及各种 复杂运算操作，Bolt的消息处理结果可以作为下游Bolt的输⼊不断迭代。 Stream：JStorm中对数据进⾏的抽象，它是时间上⽆界的Tuple元组序列。在Topology中Spout是 Stream的源头，负责从特定数据源发射Stream；Bolt可以接收任意多个Stream输⼊然后进⾏数据的加 ⼯处理，如果需要Bolt还可以发射出新Stream给下游Bolt。 Tuple：JStorm使⽤Tuple作为数据模型，存在于任意两个有数据交互的组件（Spout/Bolt）之间。每 个Tuple是⼀组具有各⾃名称的值，值可以是任何类型，JStorm⽀持所有的基本类型、字符串以及字节 数组，也可以使⽤⾃定义类型（需实现对应序列化器）作为值类型。简单来说，Tuple就是⼀组实现了 序列化器带有名称的Java对象集合。 从整个Topology上看，Spout/Bolt可以看作DAG的节点，Stream是连接不同节点之间的有向边，Tuple 则是流过Stream的数据集合。

下⾯是⼀个Topology内部Spout和Bolt之间的数据流关系：

Topology中每⼀个计算组件（Spout和Bolt）都有⼀个并⾏度，在创建Topology时指定（默认为1）， JStorm在集群内分配对应个数的线程Task并⾏。 如上图示，既然对于Spout/Bolt都会有多个线程来并⾏执⾏，那么如何在两个组件（Spout和Bolt）之 间发送Tuple会成为新的问题。 JStorm通过定义Topology时为每个Bolt指定输⼊Stream以及指定提供的若⼲种数据流分发（Stream Grouping）策略⽤来解决这⼀问题。 JStorm提供了以下⼏种Stream Grouping策略：

- 0) Shufle Grouping：随机分组，随机派发Stream⾥⾯的Tuple，保证每个Bolt接收到的Tuple数⽬⼤ 致相同，通过轮询随机的⽅式使得下游Bolt之间接收到的Tuple数⽬差值不超过1。
- 1) Fields Grouping：按字段分组，具有同样字段值的Tuple会被分到相同Bolt⾥的Task，不同字段值则 会被分配到不同Task。
- 2) Al Grouping：⼴播分组，每⼀个Tuple，所有的Bolt都会收到。
- 3) Global Grouping：全局分组，Tuple被分配到Bolt中ID值最低的的⼀个Task。
- 4) Non Grouping：不分组，Tuple会按照完全随机的⽅式分发到下游Bolt。
- 5) Direct Grouping：直接分组，Tuple需要指定由Bolt的哪个Task接收。 只有被声明为Direct Stream 的消息流可以声明这种分组⽅法。
- 6) Local or Shufle Grouping：基本同Shufle Grouping。
- 7) Custom Grouping：⽤户⾃定义分组策略，CustomStreamGrouping是⾃定义分组策略时⽤户需要 实现的接⼝。


# 三、系统架构

JStorm与Hadop相似，保持了Master/Slave的简洁优雅架构。与Hadop不同，JStorm的M/S之间不 是直接通过RPC交换⼼跳信息，⽽是借助ZK来实现，这样的设计虽然引⼊了第三⽅依赖，但是简化了 Nimbus/Supervisor的设计，同时也极⼤提⾼了系统的容错能⼒。 整个JStorm系统中共存三类不同的Daemon进程，分别是Nimbus，Supervisor和Worker。 Nimbus：JStorm中的主控节点，Nimbus类似于MR的JT，负责接收和验证客户端提交的Topology， 分配任务，向ZK写⼊任务相关的元信息，此外，Nimbus还负责通过ZK来监控节点和任务健康情况， 当有Supervisor节点变化或者Worker进程出现问题时及时进⾏任务重新分配。Nimbus分配任务的结果 不是直接下发给Supervisor，也是通过ZK维护分配数据进⾏过渡。特别地，JStorm 0.9.0领先Apache Storm实现了Nimbus HA，由于Nimbus是Stateles节点，所有的状态信息都交由ZK托管，所以HA相 对⽐较简单，热备Nimbus subscribe ZK关于Master活跃状态数据，⼀旦发现Master出现问题即从ZK ⾥恢复数据后可以⽴即接管。

Supervisor：JStorm中的⼯作节点，Supervisor类似于MR的 T，subscribe ZK分配到该节点的任务数 据，根据Nimbus的任务分配情况启动/停⽌⼯作进程Worker。Supervisor需要定期向ZK写⼊活跃端⼝ 信息以便Nimbus及时监控。Supervisor不执⾏具体的数据处理⼯作，所有的数据处理⼯作都交给 Worker完成。 Worker：JStorm中任务执⾏者，Worker类似于MR的Task，所有实际的数据处理⼯作最后都在Worker 内执⾏完成。Worker需要定期向Supervsior汇报⼼跳，由于在同⼀节点，同时为保持节点的⽆状态， Worker定期将状态信息写⼊本地磁盘，Supervisor通过读本地磁盘状态信息完成⼼跳交互过程。 Worker绑定⼀个独⽴端⼝，Worker内所有单元共享Worker的通信能⼒。 Nimbus、Supervisor和Worker均为Stateles节点，⽀持Fail-Fast，这为JStorm的扩展性和容错能⼒提 供了很好的保障。 还剩⼀个问题是Topology的各个计算组件（Spout/Bolt）如何映射到计算资源上。梳理这个问题前需 要先明确Worker/Executor/Task之间的关系：

- 0、Worker：完整的Topology任务是由分布在多个Supervisor节点上的Worker进程（JVM）来执⾏， 每个Worker都执⾏且仅执⾏Topology任务的⼀个⼦集。
- 1、Executor：Worker内部会有⼀个或多个Executor，每个Executor对应⼀个线程。Executor包括 SpoutExecutor和BoltExecutor，同⼀个Worker⾥所有的*Executor只能属于某⼀个Topology⾥的执⾏ 单元。
- 2、Task：执⾏具体数据处理实体，也就是⽤户实现的Spout/Blot实例。⼀个Executor可以对应多个 Task，定义Topology时指定，默认Executor和Task⼀⼀对应。这就是说，系统中Executor数量⼀定是 ⼩于等于Task数量（#Executor≤#Task）。 下图给出了⼀个简单的例⼦，上半部分描述的是Topology结构及相关说明，其中定义了整个Topology 的worker=2，DAG关系，各个计算组件的并⾏度；下半部分描述了Topology的Task在Supervisor节点 的分布情况。从中可以看出Topology到Executor之间的关系。


- 0、Worker数在提交Topology时在配置⽂件中指定； 例：#Worker=2
- 1、执⾏线程/Executor数在定义Topology的各计算组件并⾏度时决定，可以不指定，默认为1。其中各 个计算组件的并⾏度之和即为该Topology执⾏线程总数。 例：#Executor=sum(#paralelism hint)=2+2+6=10
- 2、Task数⽬也在定义Toplogy时确定，若不指定默认每个Executor线程对应⼀个Task，若指定Task数 ⽬会在指定数⽬的线程⾥平均分配。 例：#Task=sum(#task)=2+4+6=12，其中Executor4={Task0,Task1}


# 四、 关键流程

- 0、Topology提交 JStorm为⽤户提供了StormSubmiter. submitTopology⽤来向集群提交Topology，整个提交流程：


Client端：

- 0）客户端简单验证；
- 1）检查是否已经存在同名Topology；
- 2）提交jar包；
- 3）向Nimbus提交Topology； Nimbus端：


- 0）Nimbus端简单合法性检查；
- 1）⽣成Topology Name；
- 2）序列化配置⽂件和Topology Code；
- 3）Nimbus本地准备运⾏时所需数据；
- 4）向ZK注册Topology和Task；
- 5）将Task压⼊分配队列等待TopologyAsign分配；


- 1、任务调度策略 从0.9.0开始，JStorm提供⾮常强⼤的调度功能，基本上可以满⾜⼤部分的需求，同时⽀持⾃定义任务 调度策略。JStorm的资源不再仅是Worker的端⼝，⽽从CPU/Memory/Disk/Net等四个维度综合考虑。 Nimbus任务调度算法[2]如下：

- 0）优先使⽤⾃定义任务分配算法，当资源⽆法满⾜需求时，该任务放到下⼀级任务分配算法；
- 1）使⽤历史任务分配算法（如果打开使⽤历史任务属性），当资源⽆法满⾜需求时，该任务放到下⼀ 级任务分配算法；
- 2）使⽤默认资源平衡算法，计算每个Supervisor上剩余资源权值，取权值最⾼的Supervisor分配任 务。


- 2、Acker机制 为保证⽆数据丢失，Storm/JStorm使⽤了⾮常漂亮的可靠性处理机制，如图当定义Topology时指定 Acker，JStorm除了Topology本身任务外，还会启动⼀组称为Acker的特殊任务，负责跟踪Topolgogy DAG中的每个消息。每当发现⼀个DAG被成功处理完成，Acker就向创建根消息的Spout任务发送⼀个 Ack信号。Topology中Acker任务的并⾏度默认paralelism hint=1，当系统中有⼤量的消息时，应该适 当提⾼Acker任务的并⾏度。


Acker按照Tuple Tre的⽅式跟踪消息。当Spout发送⼀个消息的时候，它就通知对应的Acker⼀个新的 根消息产⽣了，这时Acker就会创建⼀个新的Tuple Tre。当Acker发现这棵树被完全处理之后，他就 会通知对应的Spout任务。

Acker任务保存了数据结构Map<MesageID,Map< TaskID, Value>，

其中MesageID是Spout根消息ID，TaskID是Spout任务ID，Value表示⼀个64bit的⻓整型数字，是树 中所有消息的随机ID的异或结果。通过TaskID，Acker知道当消息树处理完成后通知哪个Spout任务， 通过MesageID，Acker知道属于Spout任务的哪个消息被成功处理完成。Value表示了整棵树的的状 态，⽆论这棵树多⼤，只需要这个固定⼤⼩的数字就可以跟踪整棵树。当消息被创建和被应答的时候 都会有相同的MesageID发送过来做异或。当Acker发现⼀棵树的Value值为0的时候，表明这棵树已经 被成功处理完成。 例如，对于前⾯Topology中消息树，Acker数据的变化过程：

- Step0.A发送T0给B后：

- R0=r0 <id0,<taskA,R0>

Step1.B接收到T0并成功处理后向C发送T1，向D发送T2：

- R1=R0^r1^r2=r0^r1^r2 <id0,<taskA,R0^R1>

- =<id0,<taskA,r0^r0^r1^r2>
- =<id0,<taskA,r1^r2>

Step2.C接收到T1并成功处理后： R2=r1

- <id0,<taskA,r1^r2^R2>

- =<id0,<taskA,r1^r2^r1>
- =<id0,<taskA,r2> Step3.D接收到T2并成功处理后： R3=r2


- <id0,<taskA,r2^R3>








=<id0,<taskA,r2^r2>

=<id0,<taskA,0> 当结果为0时Acker可以通知taskA根消息id0的消息树已被成功处理完成。 需要指出的是，Acker并不是必须的，当实际业务可以容忍数据丢失情况下可以不⽤Acker，对数据丢 失零容忍的业务必须打开Acker，另外当系统的消息规模较⼤是可适当增加Acker的并⾏度。

- 3、故障恢复


- 0）节点故障 Nimbus故障。Nimbus本身⽆状态，所以Nimbus故障不会影响正在正常运⾏任务，另外Nimbus HA保 证Nimbus故障后可以及时被备份Nimbus接管。 Supervisors节点故障。Supervisor故障后，Nimbus会将故障节点上的任务迁移到其他可⽤节点上继续 运⾏，但是Supervisor故障需要外部监控并及时⼿动重启。 Worker故障。Worker健康状况监控由Supervisor负责，当Woker出现故障时，Supervisor会及时在本 机重试重启。


- Zokeper节点故障。Zokeper本身具有很好的故障恢复机制，能保证⾄少半数以上节点在线就可正 常运⾏，及时修复故障节点即可。
- 1）任务失败 Spout失败。消息不能被及时被Pul到系统中，造成外部⼤量消息不能被及时处理，⽽外部⼤量计算资 源空闲。 Bolt失败。消息不能被处理，Acker持有的所有与该Bolt相关的消息反馈值都不能回归到0，最后因为超 时最终Spout的fail将被调⽤。 Acker失败。Acker持有的所有反馈信息不管成功与否都不能及时反馈到Spout，最后同样因为超时 Spout的fail将被调⽤。 任务失败后，需要Nimbus及时监控到并重新分配失败任务。


# 五、基础接⼝

这⾥把⼏个基础接⼝中注释摘出来说明其的作⽤：

- 0、ISpout: ISpout is the core interface for implementing spouts. A Spout is responsible for feding mesages into the topology for procesing. For every tuple emited by a spout, Storm wil track the (potentialy very large) DAG of tuples generated based on a tuple emited by the spout. When Storm detects that every tuple in that DAG has ben sucesfuly procesed, it wil send an ack mesage to the Spout.
- 1、IBolt: IBolt represents a component that takes tuples as input and produces tuples as output. An IBolt can do everything from filtering to joining to functions to agregations. It does not have to proces a tuple i mediately and may hold onto tuples to proces later.
- 2、TopologyBuilder: TopologyBuilder exposes the Java API for specifying a topology for Storm to execute.
- 3、StormSubmiter: Use this clas to submit topologies to run on the Storm cluster. 针对前⾯例⼦中的Topology这⾥给出⼀个简单的实现，其中略去了BlueSpout/GreBolt/YelowBolt的 具体实现，更多参考这⾥。


<table>
  <tr>
    <th> </th>
    <th>publicstaticvoid main ( [] args){ Config conf =new Config();/ use two worker proceses conf.setNumWorkers(2);/ set paralelism hint to 2 topologyBuilder.setSpout("blue-spout", new BlueSpout(), 2); topologyBuilder.setBolt("gren-bolt", new GrenBolt(), 2)<br><br>.setNumTasks(4) .shufleGrouping("blue-spout"); topologyBuilder.setBolt("yelowbolt", new YelowBolt(), 6) .shufleGrouping("gren-bolt"); StormSubmit submitTopology("mytopology", conf, topologyBuilder.createTopology();}<br><br>String</th>
  </tr>
</table>


JStorm更多包括事务在内的接⼝详⻅源码。

六、结语

本⽂对JStorm做了简单介绍，有错误之处敬请指正。

七、参考⽂档

- [1]Storm社区.
- [2]JStorm源码.
- [3]Storm源码.
- [4]Jonathan Leibiusky, Gabriel Eisbruch, etc. Geting Started with Storm.

. OʼReily Media, Inc.

- [5]Xumingming Blog.


htp:/storm.incubator.apache.org/ htps:/github.com/alibaba/jstorm/ htps:/github.com/nathanmarz/storm/

htp:/shop.oreily.com/pro duct/063692024835.do

htp:/xumingming.sinap.com/

- [6]量⼦恒道官⽅博客.
- [7]Gogle Image.


htp:/blog.linezing.com/ htp:/images.gogle.com

