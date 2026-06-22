问题导读： 1.hadoop有master与slave，Storm与之对应的节点是什么？2.Storm控制节点上⾯运⾏⼀个后台程 序被称之为什么？3.Supervisor的作⽤是什么？4.Topology与Worker之间的关系是什么？ 5.Nimbus和Supervisor之间的所有协调⼯作有master来完成，还是Zookeeper集群完成？6.storm 稳定的原因是什么？7.如何运⾏Topology？strom jar all-your-code.jar backtype.storm.MyTopology arg1 arg28.spout是什么？9.bolt是什么？10.Topology由两部分 组成？11.stream grouping有⼏种？

![image 1](<storm 入门原理介绍.note_images/imageFile1.png>)

Storm是⼀个分布式的、⾼容错的实时计算系统。 Storm对于实时计算的的意义相当于Hadop对于批处理的意义。Hadop为我们提供了Map和Reduce 原语，使我们对数据进⾏批处理变的⾮常的简单和优美。同样，Storm也对数据的实时计算提供了简单 Spout和Bolt原语。 Storm适⽤的场景：

- 1、流数据处理：Storm可以⽤来⽤来处理源源不断的消息，并将处理之后的结果保存到持久化介质 中。
- 2、分布式RPC：由于Storm的处理组件都是分布式的，⽽且处理延迟都极低，所以可以Storm可以做 为⼀个通⽤的分布式RPC框架来使⽤。 在这个教程⾥⾯我们将学习如何创建Topologies, 并且把topologies部署到storm的集群⾥⾯去。Java 将是我们主要的示范语⾔， 个别例⼦会使⽤python以演示storm的多语⾔特性。1、准备⼯作 这个教程使⽤ 项⽬⾥⾯的例⼦。我推荐你们下载这个项⽬的代码并且跟着教程⼀起做。 先读⼀下： 和 这两篇⽂章把你的机器设置好。2、⼀个Storm 集群的基本组件 storm的集群表⾯上看和hadop的集群⾮常像。但是在Hadop上⾯你运⾏的是MapReduce的Job, ⽽ 在Storm上⾯你运⾏的是Topology。它们是⾮常不⼀样的 — ⼀个关键的区别是： ⼀个MapReduce Job最终会结束， ⽽⼀个Topology运永远运⾏（除⾮你显式的杀掉他）。 在Storm的集群⾥⾯有两种节点： 控制节点(master node)和⼯作节点(worker node)。控制节点上⾯运 ⾏⼀个后台程序：Nimbus， 它的作⽤类似Hadop⾥⾯的JobTracker。Nimbus负责在集群⾥⾯分布代 码，分配⼯作给机器， 并且监控状态。 每⼀个⼯作节点上⾯运⾏⼀个叫做Supervisor的节点（类似 TaskTracker）。Supervisor会监听分配给 它那台机器的⼯作，根据需要 启动/关闭⼯作进程。每⼀个⼯作进程执⾏⼀个Topology（类似 Job）的 ⼀个⼦集；⼀个运⾏的Topology由运⾏在很多机器上的很多⼯作进程 Worker（类似 Child）组成。


storm-starter 配置storm开发环境 新建⼀个strom项⽬

![image 2](<storm 入门原理介绍.note_images/imageFile2.png>)

storm topology结构

![image 3](<storm 入门原理介绍.note_images/imageFile3.png>)

Storm VS MapReduce Nimbus和Supervisor之间的所有协调⼯作都是通过⼀个Zokeper集群来完成。并且，nimbus进程和 supervisor都是快速失败（fail-fast)和⽆状态的。所有的状态要么在Zokeper⾥⾯， 要么在本地磁盘 上。这也就意味着你可以⽤kil -9来杀死nimbus和supervisor进程， 然后再重启它们，它们可以继续 ⼯作， 就好像什么都没有发⽣过似的。这个设计使得storm不可思议的稳定。3、Topologies 为了在storm上⾯做实时计算， 你要去建⽴⼀些topologies。⼀个topology就是⼀个计算节点所组成的 图。Topology⾥⾯的每个处理节点都包含处理逻辑， ⽽节点之间的连接则表示数据流动的⽅向。 运⾏⼀个Topology是很简单的。⾸先，把你所有的代码以及所依赖的jar打进⼀个jar包。然后运⾏类似 下⾯的这个命令。

1. strom jar al-your-code.jar backtype.storm.MyTopology arg1 arg2复制代码

这个命令会运⾏主类: backtype.strom.MyTopology, 参数是arg1, arg2。这个类的main函数定义这个 topology并且把它提交给Nimbus。storm jar负责连接到nimbus并且上传jar⽂件。 因为topology的定义其实就是⼀个Thrift结构并且nimbus就是⼀个Thrift服务， 有可以⽤任何语⾔创建 并且提交topology。上⾯的⽅⾯是⽤JVM

-based语⾔提交的最简单的⽅法, 看⼀下⽂章: 去看看怎么启动以及停⽌ topologies。4、Stream Stream是storm⾥⾯的关键抽象。⼀个stream是⼀个没有边界的tuple序列。storm提供⼀些原语来分 布式地、可靠地把⼀个stream传输进⼀个新的stream。⽐如： 你可以把⼀个twets流传输到热⻔话题 的流。 storm提供的最基本的处理stream的原语是spout和bolt。你可以实现Spout和Bolt对应的接⼝以处理你 的应⽤的逻辑。 spout的流的源头。⽐如⼀个spout可能从Kestrel队列⾥⾯读取消息并且把这些消息发射成⼀个流。⼜ ⽐如⼀个spout可以调⽤twiter的⼀个api并且把返回的twets发射成⼀个流。 通常Spout会从外部数据源（队列、数据库等）读取数据，然后封装成Tuple形式，之后发送到Stream 中。Spout是⼀个主动的⻆⾊，在接⼝内部有个nextTuple函数，Storm框架会不停的调⽤该函数。

在⽣产集群上运⾏topology

bolt可以接收任意多个输⼊stream， 作⼀些处理， 有些bolt可能还会发射⼀些新的stream。⼀些复杂 的流转换， ⽐如从⼀些twet⾥⾯计算出热⻔话题， 需要多个步骤， 从⽽也就需要多个bolt。 Bolt可 以做任何事情: 运⾏函数， 过滤tuple, 做⼀些聚合， 做⼀些合并以及访问数据库等等。 Bolt处理输⼊的Stream，并产⽣新的输出Stream。Bolt可以执⾏过滤、函数操作、Join、操作数据库 等任何操作。Bolt是⼀个被动的⻆⾊，其接⼝中有⼀个execute(Tuple input)⽅法，在接收到消息之后 会调⽤此函数，⽤户可以在此⽅法中执⾏⾃⼰的处理逻辑。

spout和bolt所组成⼀个⽹络会被打包成topology， topology是storm⾥⾯最⾼⼀级的抽象（类似 Job）， 你可以把topology提交给storm的集群来运⾏。topology的结构在Topology那⼀段已经说过 了，这⾥就不再赘述了。

topology结构

topology⾥⾯的每⼀个节点都是并⾏运⾏的。 在你的topology⾥⾯， 你可以指定每个节点的并⾏度， storm则会在集群⾥⾯分配那么多线程来同时计算。 ⼀个topology会⼀直运⾏直到你显式停⽌它。storm⾃动重新分配⼀些运⾏失败的任务， 并且storm保 证你不会有数据丢失， 即使在⼀些机器意外停机并且消息被丢掉的情况下。5、数据模型(Data Model)

storm使⽤tuple来作为它的数据模型。每个tuple是⼀堆值，每个值有⼀个名字，并且每个值可以是任 何类型， 在我的理解⾥⾯⼀个tuple可以看作⼀个没有⽅法的java对象。总体来看，storm⽀持所有的 基本类型、字符串以及字节数组作为tuple的值类型。你也可以使⽤你⾃⼰定义的类型来作为值类型， 只要你实现对应的序列化器(serializer)。 ⼀个Tuple代表数据流中的⼀个基本的处理单元，例如⼀条cokie⽇志，它可以包含多个Field，每个 Field表示⼀个属性。

Tuple本来应该是⼀个Key-Value的Map，由于各个组件间传递的tuple的字段名称已经事先定义好了， 所以Tuple只需要按序填⼊各个Value，所以就是⼀个Value List。 ⼀个没有边界的、源源不断的、连续的Tuple序列就组成了Stream。

topology⾥⾯的每个节点必须定义它要发射的tuple的每个字段。 ⽐如下⾯这个bolt定义它所发射的 tuple包含两个字段，类型分别是: double和triple。

<table>
  <tr>
    <th>public class DoubleAndTripleBoltimplementsIRichBolt {<br><br>private OutputCollectorBase _collector;<br><br>@Override初始化<br><br>public void prepare(Map conf, TopologyContext context, OutputCollectorBase collector) {<br><br>_collector = collector;<br><br>}<br><br>//Bolt⾥⾯的⽅法,Spout⾥⾯有 nextTuple()<br><br>public void execute(Tuple input) {<br><br>intval = input.getInteger(0);<br><br>_collector.emit(input,newValues(val*2, val*3)); _collector.ack(input);<br><br>}<br><br>@Override<br><br>public void cleanup() {<br><br>}<br><br>//消息源可以发射多条消息流stream。多条消息流可以理解为多种类型的数据。<br><br>public void declareOutputFields(OutputFieldsDeclarer declarer) {<br><br>declarer.declare(new Fields("double","triple"));<br><br>}<br><br>}</th>
  </tr>
</table>


declareOutputFields⽅法定义要输出的字段 ： ["double", "triple"]。这个bolt的其它部分我们接下来会 解释。

6、⼀个简单的Topology 让我们来看⼀个简单的topology的例⼦， 我们看⼀下storm-starter⾥⾯的ExclamationTopology:

<table>
  <tr>
    <th>TopologyBuilder builder =newTopologyBuilder();<br><br>builder.setSpout(1,newTestWordSpout(),10);<br><br>builder.setBolt(2,newExclamationBolt(),3)<br><br>.shuffleGrouping(1);<br><br>builder.setBolt(3,newExclamationBolt(),2)<br><br>.shuffleGrouping(2);<br><br><br></th>
  </tr>
</table>


这个Topology包含⼀个Spout和两个Bolt。Spout发射单词， 每个bolt在每个单词后⾯加个”!”。这三 个节点被排成⼀条线: spout发射单词给第⼀个bolt， 第⼀个bolt然后把处理好的单词发射给第⼆个 bolt。如果spout发射的单词是["bob"]和["john"], 那么第⼆个bolt会发射["bolt !"]和["john !"]出 来。

我们使⽤setSpout和setBolt来定义Topology⾥⾯的节点。这些⽅法接收我们指定的⼀个id， ⼀个包含 处理逻辑的对象(spout或者bolt), 以及你所需要的并⾏度。

这个包含处理的对象如果是spout那么要实现IRichSpout的接⼝， 如果是bolt，那么就要实现IRichBolt 接⼝. 最后⼀个指定并⾏度的参数是可选的。它表示集群⾥⾯需要多少个thread来⼀起执⾏这个节点。如果 你忽略它那么storm会分配⼀个线程来执⾏这个节点。

setBolt⽅法返回⼀个InputDeclarer对象， 这个对象是⽤来定义Bolt的输⼊。 这⾥第⼀个Bolt声明它要 读取spout所发射的所有的tuple — 使⽤shufle grouping。⽽第⼆个bolt声明它读取第⼀个bolt所发射 的tuple。shufle grouping表示所有的tuple会被随机的分发给bolt的所有task。给task分发tuple的策略 有很多种，后⾯会介绍。

如果你想第⼆个bolt读取spout和第⼀个bolt所发射的所有的tuple， 那么你应该这样定义第⼆个bolt:

builder.setBolt(3,newExclamationBolt(),5).shuffleGrouping(1).shuffleGrouping(2);

让我们深⼊地看⼀下这个topology⾥⾯的spout和bolt是怎么实现的。Spout负责发射新的tuple到这个 topology⾥⾯来。TestWordSpout从["nathan", "mike", "jackson", "golda", "bertels"]⾥⾯随机选择⼀ 个单词发射出来。TestWordSpout⾥⾯的nextTuple()⽅法是这样定义的：public void nextTuple() {

Utils.sleep(100); finalString[] words =newString[] {"nathan","mike","jackson","golda","bertels"};

finalRandom rand =newRandom(); finalString word = words[rand.nextInt(words.length)]; _collector.emit(newValues(word));

}

可以看到，实现很简单。

ExclamationBolt把”!”拼接到输⼊tuple后⾯。我们来看下ExclamationBolt的完整实现。public static class ExclamationBoltimplementsIRichBolt {

OutputCollector _collector; public void prepare(Map conf, TopologyContext context,OutputCollector collector) {

_collector = collector;

} public void execute(Tuple tuple){_collector.emit(tuple,newValues(tuple.getString(0) +"!!!"));

_collector.ack(tuple);

} public void cleanup() { } public void declareOutputFields(OutputFieldsDeclarer declarer) {

declarer.declare(newFields("word")); }

}

prepare⽅法提供给bolt⼀个Outputcolector⽤来发射tuple。Bolt可以在任何时候发射tuple — 在 prepare, execute或者cleanup⽅法⾥⾯, 或者甚⾄在另⼀个线程⾥⾯异步发射。这⾥prepare⽅法只是 简单地把OutputColector作为⼀个类字段保存下来给后⾯execute⽅法使⽤。 execute⽅法从bolt的⼀个输⼊接收tuple(⼀个bolt可能有多个输⼊源). ExclamationBolt获取tuple的第 ⼀个字段，加上”!”之后再发射出去。如果⼀个bolt有多个输⼊源，你可以通过调⽤ Tuple#getSourceComponent⽅法来知道它是来⾃哪个输⼊源的。 execute⽅法⾥⾯还有其它⼀些事情值得⼀提： 输⼊tuple被作为emit⽅法的第⼀个参数，并且输⼊ tuple在最后⼀⾏被ack。这些呢都是Storm可靠性API的⼀部分，后⾯会解释。 cleanup⽅法在bolt被关闭的时候调⽤， 它应该清理所有被打开的资源。但是集群不保证这个⽅法⼀定 会被执⾏。⽐如执⾏task的机器down掉了，那么根本就没有办法来调⽤那个⽅法。cleanup设计的时 候是被⽤来在local mode的时候才被调⽤(也就是说在⼀个进程⾥⾯模拟整个storm集群), 并且你想在关 闭⼀些topology的时候避免资源泄漏。 最后，declareOutputFields定义⼀个叫做”word”的字段的tuple。以local mode运⾏ ExclamationTopology 让我们看看怎么以local mode运⾏ExclamationToplogy。 storm的运⾏有两种模式: 本地模式和分布式模式. 在本地模式中， storm⽤⼀个进程⾥⾯的线程来模拟 所有的spout和bolt. 本地模式对开发和测试来说⽐较有⽤。 你运⾏storm-starter⾥⾯的topology的时 候它们就是以本地模式运⾏的， 你可以看到topology⾥⾯的每⼀个组件在发射什么消息。 在分布式模式下， storm由⼀堆机器组成。当你提交topology给master的时候， 你同时也把topology 的代码提交了。master负责分发你的代码并且负责给你的topolgoy分配⼯作进程。如果⼀个⼯作进程 挂掉了， master节点会把认为重新分配到其它节点。关于如何在⼀个集群上⾯运⾏topology， 你可以 看看Runing topologies on a production cluster⽂章。 下⾯是以本地模式运⾏ExclamationTopology的代码:

<table>
  <tr>
    <th>Config conf =newConfig(); etDebug(true); conf.setNumWorkers(2); LocalCluster cluster =newLocalCluster(); cluster.submitTopology("test", conf, builder.createTopology(); Utils.sl ep(1 0);<br><br>l stekilTopology("test");</th>
  </tr>
</table>


cluster.shutdown();

⾸先， 这个代码定义通过定义⼀个LocalCluster对象来定义⼀个进程内的集群。提交topology给这个 虚拟的集群和提交topology给分布式集群是⼀样的。通过调⽤submitTopology⽅法来提交topology， 它接受三个参数：要运⾏的topology的名字，⼀个配置对象以及要运⾏的topology本身。 topology的名字是⽤来唯⼀区别⼀个topology的，这样你然后可以⽤这个名字来杀死这个topology 的。前⾯已经说过了， 你必须显式的杀掉⼀个topology， 否则它会⼀直运⾏。 Conf对象可以配置很多东⻄， 下⾯两个是最常⻅的：

TOPOLOGY_WORKERS(setNumWorkers) 定义你希望集群分配多少个⼯作进程给你来执⾏这个 topology. topology⾥⾯的每个组件会被需要线程来执⾏。每个组件到底⽤多少个线程是通过 setBolt和setSpout来指定的。这些线程都运⾏在⼯作进程⾥⾯. 每⼀个⼯作进程包含⼀些节点的⼀ 些⼯作线程。⽐如， 如果你指定30个线程，60个进程， 那么每个⼯作进程⾥⾯要执⾏6个线程， ⽽这6个线程可能属于不同的组件(Spout, Bolt)。你可以通过调整每个组件的并⾏度以及这些线程所 在的进程数量来调整topology的性能。

TOPOLOGY_DEBUG(setDebug), 当它被设置成true的话， storm会记录下每个组件所发射的每条 消息。这在本地环境调试topology很有⽤， 但是在线上这么做的话会影响性能的。感兴趣的话可以 去看看Conf对象的Javadoc去看看topology的所有配置。

可以看看 去看看怎么配置开发环境以使你能够以本地模式运⾏topology. 运⾏中的Topology主要由以下三个组件组成的：Worker processes（进程） Executors (threads)（线程） Tasks

创建⼀个新storm项⽬

![image 4](<storm 入门原理介绍.note_images/imageFile4.png>)

Spout或者Bolt的Task个数⼀旦指定之后就不能改变了，⽽Executor的数量可以根据情况来进⾏动态 的调整。默认情况下# executor = #tasks即⼀个Executor中运⾏着⼀个Task

<table>
  <tr>
    <th>Config conf = new Config();<br><br>/设置Worer数 conf.setNumWorkers(2); topolo Bul esetSpout("blue-spout", new BlueSpout(), 2); topologyBuilder.setBolt("gren-bolt", new GrenBolt(), 2).setNumTasks(4).shufleGrouping("blue-spout");/设置Task数量</th>
  </tr>
</table>


topologyBuilder.setBolt("yelow-bolt", new YelowBolt(), 6).shufleGrouping("gren-bolt");

![image 5](<storm 入门原理介绍.note_images/imageFile5.png>)

![image 6](<storm 入门原理介绍.note_images/imageFile6.png>)

The gren bolt was configured to use two executors and four tasks.For this reason each executor runs two tasks for this bolt 绿⾊bolt设置2个executors和4个tasks。因此在这个bolt中每个executor运⾏两个tasks

![image 7](<storm 入门原理介绍.note_images/imageFile7.png>)

reconfigure the topology "mytopology" to use 5 worker proceses the spout "blue-spout" to use 3 executors an the bolt "yelow" to use 10 executors storm rebalace mytopology -n 5 -e blue-spout=3 -e yelow-bolt=10 重新设置mytopology使⽤5个worker7、流分组策略(Stream grouping) 流分组策略告诉topology如何在两个组件之间发送tuple。 要记住， spouts和bolts以很多task的形式 在topology⾥⾯同步执⾏。如果从task的粒度来看⼀个运⾏的topology， 它应该是这样的:

从task⻆度来看topology

当Bolt A的⼀个task要发送⼀个tuple给Bolt B， 它应该发送给Bolt B的哪个task呢？

stream grouping专⻔回答这种问题的。在我们深⼊研究不同的stream grouping之前， 让我们看⼀下

s torm-starter

⾥⾯的另外⼀个topology。WordCountTopology读取⼀些句⼦， 输出句⼦⾥⾯每个单词 出现的次数.

TopologyBuilder builder =newTopologyBuilder(); builder.setSpout(1,newRandomSentenceSpout(),5);

- builder.setBolt(2,newSplitSentence(),8).shuffleGrouping(1);

- builder.setBolt(3,newWordCount(),12)


.fieldsGrouping(2,newFields("word"));

SplitSentence对于句⼦⾥⾯的每个单词发射⼀个新的tuple, WordCount在内存⾥⾯维护⼀个单词->次 数的maping， WordCount每收到⼀个单词， 它就更新内存⾥⾯的统计状态。 有好⼏种不同的stream grouping:

最简单的grouping是shufle grouping, 它随机发给任何⼀个task。上⾯例⼦⾥⾯ RandomSentenceSpout和SplitSentence之间⽤的就是shufle grouping, shufle grouping对各个 task的tuple分配的⽐较均匀。

⼀种更有趣的grouping是fields grouping, SplitSentence和WordCount之间使⽤的就是fields grouping, 这种grouping机制保证相同field值的tuple会去同⼀个task， 这对于WordCount来说⾮常 关键，如果同⼀个单词不去同⼀个task， 那么统计出来的单词次数就不对了。

fields grouping是stream合并，stream聚合以及很多其它场景的基础。在背后呢， fields grouping使 ⽤的⼀致性哈希来分配tuple的。 还有⼀些其它类型的stream grouping. 你可以在Concepts⼀章⾥更详细的了解。 下⾯是⼀些常⽤的 “路由选择” 机制： Storm的Grouping即消息的Partition机制。当⼀个Tuple被发送时，如何确定将它发送个某个（些） Task来处理？？l ShuffleGrouping：随机选择⼀个Task来发送。 l FiledGrouping：根据Tuple中Fields来做⼀致性hash，相同hash值的Tuple被发送到相同的Task。 l AllGrouping：⼴播发送，将每⼀个Tuple发送到所有的Task。 l GlobalGrouping：所有的Tuple会被发送到某个Bolt中的id最⼩的那个Task。 l NoneGrouping：不关⼼Tuple发送给哪个Task来处理，等价于ShuffleGrouping。 l DirectGrouping：直接将Tuple发送到指定的Task来处理。

- 8、使⽤别的语⾔来定义Bolt Bolt可以使⽤任何语⾔来定义。⽤其它语⾔定义的bolt会被当作⼦进程(subproces)来执⾏， storm使 ⽤JSON消息通过stdin/stdout来和这些subproces通信。这个通信协议是⼀个只有10⾏的库， storm 团队给这些库开发了对应的Ruby, Python和Fancy版本。 下⾯是WordCountTopology⾥⾯的SplitSentence的定


义:publicstaticclassSplitSentenceextendsShellBoltimplementsIRichBolt {

publicSplitSentence() { super("python","splitsentence.py");

}

publicvoiddeclareOutputFields(OutputFieldsDeclarer declarer) {

declarer.declare(newFields("word")); }

}

SplitSentence继承⾃ShelBolt并且声明这个Bolt⽤python来运⾏，并且参数是: splitsentence.py。下 ⾯是splitsentence.py的定义:

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


import storm

clasSplitSentenceBolt(storm.BasicBolt):

defproces(self, tup): words=tup.values[0].split(" ") forwordinwords:

storm.emit([word])

SplitSentenceBolt().run()复制代码

- 9、可靠的消息处理 在这个教程的前⾯，我们跳过了有关tuple的⼀些特征。这些特征就是storm的可靠性API： storm如何 保证spout发出的每⼀个tuple都被完整处理。看看 以更深⼊了解storm 的可靠性API. Storm允许⽤户在Spout中发射⼀个新的源Tuple时为其指定⼀个MesageId，这个MesageId可以是任 意的Object对象。多个源Tuple可以共⽤同⼀个MesageId，表示这多个源Tuple对⽤户来说是同⼀个 消息单元。Storm的可靠性是指Storm会告知⽤户每⼀个消息单元是否在⼀个指定的时间内被完全处 理。完全处理的意思是该MesageId绑定的源Tuple以及由该源Tuple衍⽣的所有Tuple都经过了 Topology中每⼀个应该到达的Bolt的处理。


《storm如何保证消息不丢失》

![image 8](<storm 入门原理介绍.note_images/imageFile8.png>)

在Spout中由mesage 1绑定的tuple1和tuple2分别经过bolt1和bolt2的处理，然后⽣成了两个新的 Tuple，并最终流向了bolt3。当bolt3处理完之后，称mesage 1被完全处理了。 Storm中的每⼀个Topology中都包含有⼀个Acker组件。Acker组件的任务就是跟踪从Spout中流出的每 ⼀个mesageId所绑定的Tuple树中的所有Tuple的处理情况。如果在⽤户设置的最⼤超时时间内这些 Tuple没有被完全处理，那么Acker会告诉Spout该消息处理失败，相反则会告知Spout该消息处理成 功。 那么Acker是如何记录Tuple的处理结果呢？？ A xor A = 0. A xor B…xor B xor A = 0，其中每⼀个操作数出现且仅出现两次。 在Spout中，Storm系统会为⽤户指定的MesageId⽣成⼀个对应的64位的整数，作为整个Tuple Tre 的RotId。RotId会被传递给Acker以及后续的Bolt来作为该消息单元的唯⼀标识。同时，⽆论Spout 还是Bolt每次新⽣成⼀个Tuple时，都会赋予该Tuple⼀个唯⼀的64位整数的Id。 当Spout发射完某个MesageId对应的源Tuple之后，它会告诉Acker⾃⼰发射的RotId以及⽣成的那些 源Tuple的Id。⽽当Bolt处理完⼀个输⼊Tuple并产⽣出新的Tuple时，也会告知Acker⾃⼰处理的输⼊ Tuple的Id以及新⽣成的那些Tuple的Id。Acker只需要对这些Id进⾏异或运算，就能判断出该RotId对 应的消息单元是否成功处理完成了。 open-open

