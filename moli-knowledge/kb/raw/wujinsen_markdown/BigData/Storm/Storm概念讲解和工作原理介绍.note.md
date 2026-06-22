# Strom的结构

![image 1](<Storm概念讲解和工作原理介绍.note_images/imageFile1.png>)

Storm与传统关系型数据库 传统关系型数据库是先存后计算，⽽storm则是先算后存，甚⾄不存 传统关系型数据库很难部署实时计算，只能部署定时任务统计分析窗⼝数据 关系型数据库重视事务，并发控制，相对来说Storm⽐较简陋 Storm不Hadop，Spark等是流⾏的⼤数据⽅案

与Storm关系密切的语⾔：核⼼代码⽤clojure书写，实⽤程序⽤python开发，使⽤java开发拓扑

# topology

Storm集群中有两种节点，⼀种是控制节点(Nimbus节点)，另⼀种是⼯作节点(Supervisor节点)。所 有Topology任务的 提交必须在Storm客户端节点上进⾏(需要配置 storm.yaml⽂件)，由Nimbus节点分 配给其他Supervisor节点进⾏处理。 Nimbus节点⾸先将提交的Topology进⾏分⽚，分成⼀个个的 Task，并将Task和Supervisor相关的信息提交到 zokeper集群上，Supervisor会去zokeper集群上 认领⾃⼰的Task，通知⾃⼰的Worker进程进⾏Task的处理。

和同样是计算框架的MapReduce相⽐，MapReduce集群上运⾏的是Job，⽽Storm集群上运⾏的是 Topology。但是Job在运⾏结束之后会⾃⾏结束，Topology却只能被⼿动的kil掉，否则会⼀直运⾏下 去

Storm不处理计算结果的保存，这是应⽤代码需要负责的事情，如果数据不⼤，你可以简单地保存在 内存⾥，也可以每次都更新数据库，也可以采⽤NoSQL存储。这部分事情完全交给⽤户。

数据存储之后的展现，也是你需要⾃⼰处理的，storm UI 只提供对topology的监控和统计。

总体的Topology处理流程图为：

![image 2](<Storm概念讲解和工作原理介绍.note_images/imageFile2.png>)

# zokeper集群

storm使⽤zokeper来协调整个集群， 但是要注意的是storm并不⽤zokeper来传递消息。所以 zokeper上的负载是⾮常低的，单个节点的zokeper在⼤多数情况下 都已经⾜够了， 但是如果你 要部署⼤⼀点的storm集群， 那么你需要的zokeper也要⼤⼀点。关于如何部署zokeper，可以看 htp:/zokeper.apache.org/doc /r3.3.3/zokeperAdmin.html

部署zokeper有些需要注意的地⽅： 1、对zokeper做好监控⾮常重要， zokeper是fail-fast的系统，只要出现什么错误就会退出，

所以实际场景中要监控，更多细节看htp:/zokeper.apache.org/doc/r3.3.3 /zokeperAdmin.html#sc_supervision

2、实际场景中要配置⼀个cron job来压缩zokeper的数据和业务⽇志。zokeper⾃⼰是不会去压 缩这些的，所以你如果不设置⼀个cron job, 那么你很快就会发现磁盘不够⽤了，更多细节可以查看 htp:/zokeper.apache.org/doc/r3.3.3 /zokeperAdmin.html#sc_maintenance

# Component

Storm中，Spout和Bolt都是Component。所以，Storm定义了⼀个名叫IComponent的总接⼝ 全家普如下：绿⾊部分是我们最常⽤、⽐较简单的部分。红⾊部分是与事务相关的

![image 3](<Storm概念讲解和工作原理介绍.note_images/imageFile3.png>)

# Spout

Spout是Stream的消息产⽣源， Spout组件的实现可以通过继承BaseRichSpout类或者其他Spout类 来完成，也可以通过实现IRichSpout接⼝来实现 public interface ISpout extends Serializable {

void open(Map conf, TopologyContext context, SpoutOutputColector colector); void close(); void nextTuple(); void ack(Object msgId); void fail(Object msgId);

}

open()⽅法 - 初始化⽅法 close()- 在该spout将要关闭时调⽤。但是不保证其⼀定被调⽤，因为在集群中supervisor节点，

可以使⽤kil -9来杀死worker进程。只有当Storm是在本地模式下运⾏，如果是发送停⽌命令，可以保 证close的执⾏

ack(Object msgId)- 成功处理tuple时回调的⽅法，通常情况下，此⽅法的实现是将消息队列中的 消息移除，防⽌消息重放

fail(Object msgId)- 处理tuple失败时回调的⽅法，通常情况下，此⽅法的实现是将消息放回消息队 列中然后在稍后时间⾥重放

nextTuple()- 这是Spout类中最重要的⼀个⽅法。发射⼀个Tuple到Topology都是通过这个⽅法来 实现的。调⽤此⽅法时，storm向spout发出请求， 让spout发出元组（tuple）到输出器（ouput colector）。这种⽅法应该是⾮阻塞的，所以spout如果没有元组发出，这个⽅法应该返回。 nextTuple、ack 和fail 都在spout任务的同⼀个线程中被循环调⽤。 当没有元组的发射时，应该让 nextTuple睡眠⼀个很短的时间（如⼀毫秒），以免浪费太多的CPU。 继承了BaseRichSpout后，不⽤实现close、 activate、 deactivate、 ack、 fail 和 getComponentConfiguration ⽅法，只关⼼最基本核⼼的部分。 通常情况下（Shel和事务型的除外），实现⼀个Spout，可以直接实现接⼝IRichSpout，如果不想写多 余的代码，可以直接继承BaseRichSpout

# Bolt

Bolt类接收由Spout或者其他上游Bolt类发来的Tuple，对其进⾏处理。Bolt组件的实现可以通过继承 BasicRichBolt类或者IRichBolt接⼝等来完成

prepare⽅法 - 此⽅法和Spout中的open⽅法类似，在集群中⼀个worker中的task初始化时调⽤。

它提供了bolt执⾏的环境 declareOutputFields⽅法 - ⽤于声明当前Bolt发送的Tuple中包含的字段(field)，和Spout中类似 cleanup⽅法 - 同ISpout的close⽅法，在关闭前调⽤。同样不保证其⼀定执⾏。 execute⽅法 - 这是Bolt中最关键的⼀个⽅法，对于Tuple的处理都可以放到此⽅法中进⾏。具体的

发送是通过emit⽅法来完成的。execute接受⼀个 tuple进⾏处理，并⽤prepare⽅法传⼊的 OutputColector的ack⽅法（表示成功）或fail（表示失败）来反馈处理结果。

Storm提供了IBasicBolt接⼝，其⽬的就是实现该接⼝的Bolt不⽤在代码中提供反馈结果了，Storm内 部会⾃动反馈成功。如果你确实要反馈失败，可以抛出FailedException

通常情况下，实现⼀个Bolt，可以实现IRichBolt接⼝或继承BaseRichBolt，如果不想⾃⼰处理结果反 馈，可以实现 IBasicBolt接⼝或继承BaseBasicBolt，它实际上相当于⾃动实现了 colector.emit.ack(inputTuple)

# Topology运⾏流程

- (1)Storm提交后，会把代码⾸先存放到Nimbus节点的inbox⽬录下，之后，会把当前Storm运⾏的配

置⽣成⼀个 stormconf.ser⽂件放到Nimbus节点的stormdist⽬录中，在此⽬录中同时还有序列化之后 的Topology代码⽂件

- (2) 在设定Topology所关联的Spouts和Bolts时，可以同时设置当前Spout和Bolt的executor数⽬和


task数⽬，默认情况下， ⼀个Topology的task的总和是和executor的总和⼀致的。之后，系统根据 worker的数⽬，尽量平均的分配这些task的执⾏。 worker在哪个supervisor节点上运⾏是由storm本身 决定的

- (3)任务分配好之后，Nimbus节点会将任务的信息提交到zokeper集群，同时在zokeper集群中


会有workerbeats节点，这⾥存储了当前Topology的所有worker进程的⼼跳信息

- (4)Supervisor 节点会不断的轮询zokeper集群，在zokeper的asignments节点中保存了所有


Topology的任务分配信息、代码存储⽬ 录、任务之间的关联关系等，Supervisor通过轮询此节点的内 容，来领取⾃⼰的任务，启动worker进程运⾏

(5)⼀个Topology运⾏之后，就会不断的通过Spouts来发送Stream流，通过Bolts来不断的处理接收 到的Stream流，Stream流是⽆界的。

最后⼀步会不间断的执⾏，除⾮⼿动结束Topology。

# Topology运⾏⽅式

在开始创建项⽬之前，了解Storm的操作模式(operation modes)是很重要的。 Storm有两种运⾏⽅ 式

本地运⾏的提交⽅式，例： LocalCluster cluster = new LocalCluster(); cluster.submitTopology(TOPOLOGY_NAME, conf, builder.createTopology(); Thread.sl ep(2 0); cluster.shutdown();

分布式提交⽅式，例： StormSubmiter.submitTopology（TOPOLOGY_NAME, conf, builder.createTopology();

需要注意的是，在Storm代码编写完成之后，需要打包成jar包放到Nimbus中运⾏，打包的时候，不 需要把依赖的jar都打迚去，否则如果把依赖的 storm.jar包打进去的话，运⾏时会出现重复的配置⽂件 错误导致Topology⽆法运⾏。因为Topology运⾏之前，会加载本地的 storm.yaml 配置⽂件。

运⾏的命令如下： storm jar StormTopology.jar mainclas [args]

# storm守护进程的命令

Nimbus: storm nimbus 启动nimbus守护进程 Supervisor: storm supervisor 启动supervisor守护迚程 UI：storm ui 这将启动stormUI的守护进程,为监测storm集群提供⼀个基于web的⽤户界⾯。 DRPC: storm drpc 启动DRPC的守护进程

# storm管理命令

JAR：storm jar topology_jar topology_clas [arguments.] jar命令是⽤于提交⼀个集群拓扑.它运⾏指定参数的topology_clas中的main()⽅法，上传

topology_jar到nimbus， 由nimbus发布到集群中。⼀旦提交，storm将激活拓扑并开始处理 topology_clas 中的main()⽅法，main()⽅法负责调⽤StormSubmiter.submitTopology()⽅法，并提 供⼀个唯⼀的拓扑(集群)的 名。如果⼀个拥有该名称的拓扑已经存在于集群中，jar命令将会失败。常 ⻅的做法是在使⽤命令⾏参数来指定拓扑名称，以便拓扑在提交的时候被命名。

KI L：storm kil topology_name [-w wait_time] 杀死⼀个拓扑，可以使⽤kil命令。它会以⼀种安全的⽅式销毁⼀个拓扑，⾸先停⽤拓扑，在等待拓

扑消息的时间段内允许拓扑完成当前的数据流。执⾏ kil命令时可以通过-w [等待秒数]指定拓扑停⽤以 后的等待时间。也可以在Storm UI 界⾯上实现同样的功能

Deactivate：storm deactivate topology_name 停⽤拓扑时，所有已分发的元组都会得到处理，spouts的nextTuple⽅法将不会被调⽤。也可以在

Storm UI 界⾯上实现同样的功能

Activate：storm activate topology_name 启动⼀个停⽤的拓扑。也可以在Storm UI 界⾯上实现同样的功能

Rebalance：storm rebalance topology_name [-w wait_time] [-n worker_count] [-e component_name=executer_count].

rebalance使你重新分配集群任务。这是个很强⼤的命令。⽐如，你向⼀个运⾏中的集群增加了节 点。rebalance命令将会停⽤拓扑，然后在相应超时时间之后重分配worker，并重启拓扑 例：storm rebalance wordcount-topology -w 15 -n 5 -e sentence-spout=4 -e split-bolt=8

还有其他管理命令，如：Remoteconfvalue、REPL、Claspath等

# 新建storm项⽬注意事项

为了开发storm项⽬，你的claspath⾥⾯需要有storm的jar包。最推荐的⽅式是使⽤Maven，不使⽤ maven的话你可以⼿动把storm发⾏版⾥⾯的所有的jar包添加到claspath

storm-starter项⽬使⽤Leiningen作为build和依赖管理⼯具，你可以下载这个脚本 （htps:/raw.githubusercontent.com/technomancy/leiningen/stable/bin /lein）来安装Leiningen, 把 它加⼊到你的PATH， 使它可执⾏。要拉取storm的所有依赖包，简单地在项⽬的根⽬录执⾏ lein deps 就可以了

