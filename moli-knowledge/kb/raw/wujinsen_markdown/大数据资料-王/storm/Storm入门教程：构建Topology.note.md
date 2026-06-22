⼀、Storm基本概念 在运⾏⼀个Storm任务之前，需要了解⼀些概念：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


Topologies（拓扑） Streams（数据流） Spouts（⽔泵） Bolts（螺栓） Stream groupings（数据流组） Reliability（可靠性） Tasks（任务） Workers（⼯⼈） Configuration（配置）

Storm集群和Hadop集群表⾯上看很类似。但是Hadop上运⾏的是MapReduce jobs，⽽在Storm 上运⾏的是拓扑（topology），这两者之间是⾮常不⼀样的。⼀个关键的区别是： ⼀个MapReduce job最终会结束， ⽽⼀个topology永远会运⾏（除⾮你⼿动kil掉）。

在Storm的集群⾥⾯有两种节点： 控制节点（master node）和⼯作节点（worker node）。控制节 点上⾯运⾏⼀个叫Nimbus后台程序，它的作⽤类似Hadop⾥⾯的JobTracker。Nimbus负责在集群⾥ ⾯分发代码，分配计算任务给机器， 并且监控状态。

每⼀个⼯作节点上⾯运⾏⼀个叫做Supervisor的节点。Supervisor会监听分配给它那台机器的⼯作， 根据需要启动/关闭⼯作进程。每⼀个⼯作进程执⾏⼀个topology的⼀个⼦集；⼀个运⾏的topology由 运⾏在很多机器上的很多⼯作进程组成。

Nimbus和Supervisor之间的所有协调⼯作都是通过Zokeper集群完成。另外，Nimbus进程和 Supervisor进程都是快速失败（fail-fast)和⽆状态的。所有的状态要么在zokeper⾥⾯， 要么在本地 磁盘上。这也就意味着你可以⽤kil -9来杀死Nimbus和Supervisor进程， 然后再重启它们，就好像什 么都没有发⽣过。这个设计使得Storm异常的稳定。

- 1、Topologies ⼀个topology是spouts和bolts组成的图， 通过stream groupings将图中的spouts和bolts连接起来，如 下图：


⼀个topology会⼀直运⾏直到你⼿动kil掉，Storm⾃动重新分配执⾏失败的任务， 并且Storm可以保 证你不会有数据丢失（如果开启了⾼可靠性的话）。如果⼀些机器意外停机它上⾯的所有任务会被转 移到其他机器上。 运⾏⼀个topology很简单。⾸先，把你所有的代码以及所依赖的jar打进⼀个jar包。然后运⾏类似下⾯ 的这个命令： storm jar al-my-code.jar backtype.storm.MyTopology arg1 arg2

这个命令会运⾏主类: backtype.strom.MyTopology, 参数是arg1, arg2。这个类的main函数定义这个 topology并且把它提交给Nimbus。storm jar负责连接到Nimbus并且上传jar包。 Topology的定义是⼀个Thrift结构，并且Nimbus就是⼀个Thrift服务， 你可以提交由任何语⾔创建的 topology。上⾯的⽅⾯是⽤JVM-based语⾔提交的最简单的⽅法。

- 2、Streams 消息流stream是storm⾥的关键抽象。⼀个消息流是⼀个没有边界的tuple序列， ⽽这些tuple序列会以 ⼀种分布式的⽅式并⾏地创建和处理。通过对stream中tuple序列中每个字段命名来定义stream。在默 认的情况下，tuple的字段类型可以是：integer，long，short， byte，string，double，float， bolean和byte aray。你也可以⾃定义类型（只要实现相应的序列化器）。 每个消息流在定义的时候会被分配给⼀个id，因为单向消息流使⽤的相当普遍， OutputFieldsDeclarer 定义了⼀些⽅法让你可以定义⼀个stream⽽不⽤指定这个id。在这种情况下这个stream会分配个值 为‘defaultʼ默认的id 。 Storm提供的最基本的处理stream的原语是spout和bolt。你可以实现spout和bolt提供的接⼝来处理你 的业务逻辑。
- 3、Spouts 消息源spout是Storm⾥⾯⼀个topology⾥⾯的消息⽣产者。⼀般来说消息源会从⼀个外部源读取数据 并且向topology⾥⾯发出消息：tuple。Spout可以是可靠的也可以是不可靠的。如果这个tuple没有被 storm成功处理，可靠的消息源spouts可以重新发射⼀个tuple， 但是不可靠的消息源spouts⼀旦发出 ⼀个tuple就不能重发了。 消息源可以发射多条消息流stream。使⽤OutputFieldsDeclarer.declareStream来定义多个stream，然 后使⽤SpoutOutputColector来发射指定的stream。 Spout类⾥⾯最重要的⽅法是nextTuple。要么发射⼀个新的tuple到topology⾥⾯或者简单的返回如果 已经没有新的tuple。要注意的是nextTuple⽅法不能阻塞，因为storm在同⼀个线程上⾯调⽤所有消息 源spout的⽅法。 另外两个⽐较重要的spout⽅法是ack和fail。storm在检测到⼀个tuple被整个topology成功处理的时候 调⽤ack，否则调⽤fail。storm只对可靠的spout调⽤ack和fail。
- 4、Bolts 所有的消息处理逻辑被封装在bolts⾥⾯。Bolts可以做很多事情：过滤，聚合，查询数据库等等。 Bolts可以简单的做消息流的传递。复杂的消息流处理往往需要很多步骤，从⽽也就需要经过很多 bolts。⽐如算出⼀堆图⽚⾥⾯被转发最多的图⽚就⾄少需要两步：第⼀步算出每个图⽚的转发数量。 第⼆步找出转发最多的前10个图⽚。（如果要把这个过程做得更具有扩展性那么可能需要更多的步 骤）。 Bolts可以发射多条消息流， 使⽤OutputFieldsDeclarer.declareStream定义stream，使⽤ OutputColector.emit来选择要发射的stream。


- Bolts的主要⽅法是execute, 它以⼀个tuple作为输⼊，bolts使⽤OutputColector来发射tuple，bolts必 须要为它处理的每⼀个tuple调⽤OutputColector的ack⽅法，以通知Storm这个tuple被处理完成了， 从⽽通知这个tuple的发射者spouts。 ⼀般的流程是： bolts处理⼀个输⼊tuple, 发射0个或者多个 tuple, 然后调⽤ack通知storm⾃⼰已经处理过这个tuple了。storm提供了⼀个IBasicBolt会⾃动调⽤ ack。
- 5、Stream groupings 定义⼀个topology的其中⼀步是定义每个bolt接收什么样的流作为输⼊。stream grouping就是⽤来定 义⼀个stream应该如果分配数据给bolts上⾯的多个tasks。 Storm⾥⾯有7种类型的stream grouping
- 6、Reliability Storm保证每个tuple会被topology完整的执⾏。Storm会追踪由每个spout tuple所产⽣的tuple树（⼀ 个bolt处理⼀个tuple之后可能会发射别的tuple从⽽形成树状结构），并且跟踪这棵tuple树什么时候成 功处理完。每个topology都有⼀个消息超时的设置，如果storm在这个超时的时间内检测不到某个 tuple树到底有没有执⾏成功， 那么topology会把这个tuple标记为执⾏失败，并且过⼀会⼉重新发射这 个tuple。 为了利⽤Storm的可靠性特性，在你发出⼀个新的tuple以及你完成处理⼀个tuple的时候你必须要通知 storm。这⼀切是由OutputColector来完成的。通过emit⽅法来通知⼀个新的tuple产⽣了，通过ack⽅ 法通知⼀个tuple处理完成了。


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


Shufle Grouping: 随机分组， 随机派发stream⾥⾯的tuple，保证每个bolt接收到的tuple数⽬⼤ 致相同。 Fields Grouping：按字段分组， ⽐如按userid来分组， 具有同样userid的tuple会被分到相同的 Bolts⾥的⼀个task， ⽽不同的userid则会被分配到不同的bolts⾥的task。 Al Grouping：⼴播发送，对于每⼀个tuple，所有的bolts都会收到。

Global Grouping：全局分组， 这个tuple被分配到storm中的⼀个bolt的其中⼀个task。再具体⼀ 点就是分配给id值最低的那个task。 Non Grouping：不分组，这个分组的意思是说stream不关⼼到底谁会收到它的tuple。⽬前这种分 组和Shufle grouping是⼀样的效果， 有⼀点不同的是storm会把这个bolt放到这个bolt的订阅者 同⼀个线程⾥⾯去执⾏。 Direct Grouping： 直接分组， 这是⼀种⽐较特别的分组⽅法，⽤这种分组意味着消息的发送者指定由消息接收者的 哪个task处理这个消息。 只有被声明为Direct Stream的消息流可以声明这种分组⽅法。⽽且这种消息tuple必须使⽤emitDirect⽅法来发射。消 息处理者可以通过 TopologyContext来获取处理它的消息的task的id （OutputColector.emit⽅法也会返回task的 id）。 Local or shufle grouping：如果⽬标bolt有⼀个或者多个task在同⼀个⼯作进程中，tuple将会被 随机发⽣给这些tasks。否则，和普通的Shufle Grouping⾏为⼀致。

Storm的可靠性我们在第四章会深⼊介绍。

- 7、Tasks 每⼀个spout和bolt会被当作很多task在整个集群⾥执⾏。每⼀个executor对应到⼀个线程，在这个线 程上运⾏多个task，⽽stream grouping则是定义怎么从⼀堆task发射tuple到另外⼀堆task。你可以调 ⽤TopologyBuilder类的setSpout和setBolt来设置并⾏度（也就是有多少个task）。
- 8、Workers ⼀个topology可能会在⼀个或者多个worker（⼯作进程）⾥⾯执⾏，每个worker是⼀个物理JVM并且 执⾏整个topology的⼀部分。⽐如，对于并⾏度是30的topology来说，如果我们使⽤50个⼯作进程 来执⾏，那么每个⼯作进程会处理其中的6个tasks。Storm会尽量均匀的⼯作分配给所有的worker。
- 9、Configuration Storm⾥⾯有⼀堆参数可以配置来调整Nimbus, Supervisor以及正在运⾏的topology的⾏为，⼀些配置 是系统级别的，⼀些配置是topology级别的。default.yaml⾥⾯有所有的默认配置。你可以通过定义个 storm.yaml在你的claspath⾥来覆盖这些默认配置。并且你也可以在代码⾥⾯设置⼀些topology相关 的配置信息（使⽤StormSubmiter）。 ⼆、构建Topology


- 1. 实现的⽬标： 我们将设计⼀个topology，来实现对⼀个句⼦⾥⾯的单词出现的频率进⾏统计。这是⼀个简单的例 ⼦，⽬的是让⼤家对于topology快速上⼿，有⼀个初步的理解。
- 2. 设计Topology结构： 在开始开发Storm项⽬的第⼀步，就是要设计topology。确定好你的数据处理逻辑，我们今天将的这个 简单的例⼦，topology也⾮常简单。整个topology如下：

整个topology分为三个部分：

- 3. 设计数据流 这个topology从kestrel queue读取句⼦,并把句⼦划分成单词,然后汇总每个单词出现的次数,⼀个tuple 负责读取句⼦,每⼀个tuple分别对应计算每⼀个单词出现的次数,⼤概样⼦如下所示：
- 4. 代码实现：


KestrelSpout:数据源，负责发送sentence

Splitsentence:负责将sentence切分

Wordcount:负责对单词的频率进⾏累加

- 1) 构建maven环境： 为了开发storm topology, 你需要把storm相关的jar包添加到claspath⾥⾯去： 要么⼿动添加所有相关 的jar包， 要么使⽤maven来管理所有的依赖。storm的jar包发布在Clojars(⼀个maven库), 如果你使⽤ maven的话，把下⾯的配置添加在你项⽬的pom.xml⾥⾯。


<repository> <id>clojars.org</id> <url> </url> </repository> <dependenc y> <groupId>storm</groupId> <artifactId>storm</artifactId> <version>0.5.3</version> <scope>test</scope> </dependency>

htp:/clojars.org/repo

- 2) 定义topology： TopologyBuilder builder = new TopologyBuilder(); builder.setSpout(1, new KestrelSpout(“kestrel.ba cktype.com”,213,” sentence_queue”, new StringScheme( ); builder.setBolt(2, new SplitSentence(), 10) .shufleGroupi ng(1); builder.setBolt(3, new WordCount(), 20) .fieldsGrouping(2, new Fields(“word”); 这种topology的spout从句⼦队列中读取句⼦，在kestrel.backtype.com位于⼀个Kestrel的服务器端⼝


213。 Spout⽤setSpout⽅法插⼊⼀个独特的id到topology。 Topology中的每个节点必须给予⼀个id，id是由 其他bolts⽤于订阅该节点的输出流。 KestrelSpout在topology中id为1。 setBolt是⽤于在Topology中插⼊bolts。 在topology中定义的第⼀个bolts 是切割句⼦的bolts。 这个 bolts 将句⼦流转成成单词流。 让我们看看SplitSentence实施： public clas SplitSentence implements IBasicBolt{ public void prepare(Map conf, TopologyCon text context) { } public void execute(Tuple tuple, BasicOutputColector colector) {

String sentence = tuple.getString(0); for(String word: sentence.split(“ ”) { c olector.emit(new Values(word); } } public void cleanup() { } public void declareOutputFields(OutputFieldsDeclarer declarer) { declarer.declare(new Fields(“w ord”); }} 关键的⽅法是 execute⽅法。 正如你可以看到，它将句⼦拆分成单词，并发出每个单词作为⼀个新的 元组。 另⼀个重要的⽅法是declareOutputFields，其中宣布bolts输出元组的架构。 在这⾥宣布，它 发出⼀个域为word的元组。 setBolt的最后⼀个参数是你想为bolts的并⾏量。 SplitSentence bolts 是10个并发，这将导致在storm 集群中有⼗个线程并⾏执⾏。 你所要做的的是增加bolts的并⾏量在遇到topology的瓶颈时。 setBolt⽅法返回⼀个对象，⽤来定义bolts的输⼊。 例如，SplitSentence螺栓订阅组件“1”使⽤随机分 组的输出流。 “1”是指已经定义KestrelSpout。 我将解释在某⼀时刻的随机分组的⼀部分。 到⽬前为 ⽌，最要紧的是，SplitSentence bolts会消耗KestrelSpout发出的每⼀个元组。 下⾯在让我们看看wordcount的实现：

public clas WordCount implements IBasicBolt { private Map<String, Integer> _counts = new H ashMap<String, Integer> (); public void prepare(Map conf, TopologyContext context) { } public void execute(Tu ple tuple, BasicOutputColector colector) { String word = tuple.getString(0); int co unt; if(_counts.containsKey(word) { count = _counts.get(word); } else { count = 0; } count+; _counts.put(word, count); colector.emit (new Values(word, count); } public void cleanup() { } public void declareOutputFiel ds(OutputFieldsDeclarer declarer) { declarer.declare(new Fields(“word”, “count”); } } SplitSentence对于句⼦⾥⾯的每个单词发射⼀个新的tuple, WordCount在内存⾥⾯维护⼀个单词->次 数的maping， WordCount每收到⼀个单词， 它就更新内存⾥⾯的统计状态。

- 5. 运⾏Topology storm的运⾏有两种模式: 本地模式和分布式模式.


- 1) 本地模式： storm⽤⼀个进程⾥⾯的线程来模拟所有的spout和bolt. 本地模式对开发和测试来说⽐较有⽤。 你运⾏ storm-starter⾥⾯的topology的时候它们就是以本地模式运⾏的， 你可以看到topology⾥⾯的每⼀个 组件在发射什么消息。
- 2) 分布式模式： storm由⼀堆机器组成。当你提交topology给master的时候， 你同时也把topology的代码提交了。 master负责分发你的代码并且负责给你的topolgoy分配⼯作进程。如果⼀个⼯作进程挂掉了， master 节点会把认为重新分配到其它节点。
- 3) 下⾯是以本地模式运⾏的代码： Config conf = new Config(); conf.setDebug(true); conf.setNumWorkers(2); LocalCluster cluster = n ew LocalCluster(); cluster.submitTopology(“test”, conf, builder.createTopology(); Utils.sl ep(1 0 ); cluster.kilTopology(“test”); cluster.shutdown(); ⾸先， 这个代码定义通过定义⼀个LocalCluster对象来定义⼀个进程内的集群。提交topology给这个 虚拟的集群和提交topology给分布式集群是⼀样的。通过调⽤submitTopology⽅法来提交topology， 它接受三个参数：要运⾏的topology的名字，⼀个配置对象以及要运⾏的topology本身。 topology的名字是⽤来唯⼀区别⼀个topology的，这样你然后可以⽤这个名字来杀死这个topology 的。前⾯已经说过了， 你必须显式的杀掉⼀个topology， 否则它会⼀直运⾏。 Conf对象可以配置很多东⻄， 下⾯两个是最常⻅的： TOPOLOGY_WORKERS(setNumWorkers) 定义你希望集群分配多少个⼯作进程给你来执⾏这个 topology. topology⾥⾯的每个组件会被需要线程来执⾏。每个组件到底⽤多少个线程是通过setBolt和 setSpout来指定的。这些线程都运⾏在⼯作进程⾥⾯. 每⼀个⼯作进程包含⼀些节点的⼀些⼯作线程。 ⽐如， 如果你指定30个线程，60个进程， 那么每个⼯作进程⾥⾯要执⾏6个线程， ⽽这6个线程可 能属于不同的组件(Spout, Bolt)。你可以通过调整每个组件的并⾏度以及这些线程所在的进程数量来调 整topology的性能。


TOPOLOGY_DEBUG(setDebug), 当它被设置成true的话， storm会记录下每个组件所发射的每条消 息。这在本地环境调试topology很有⽤， 但是在线上这么做的话会影响性能的。 结论： 本章从storm的基本对象的定义，到⼴泛的介绍了storm的开发环境，从⼀个简单的例⼦讲解了 topology的构建和定义。希望⼤家可以从本章的内容对storm有⼀个基本的理解和概念，并且已经可以 构建⼀个简单的topology！！ 【编辑推荐】

- 1.
- 2.
- 3.
- 4.


从问题域出发认识Hadop⽣态系统 技术⼩⽩：Hadop 到底是啥？ 关于Hadop的六⼤误解 Storm⼊⻔教程：前⾔

