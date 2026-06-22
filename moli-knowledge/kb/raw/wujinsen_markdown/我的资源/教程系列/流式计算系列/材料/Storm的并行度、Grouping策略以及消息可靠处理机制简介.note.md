概念：Workers (JVMs): 在⼀个节点上可以运⾏⼀个或多个独⽴的JVM 进程。⼀个Topology可以包含⼀个或多个worker(并

⾏的跑在不同的machine上), 所以worker process就是执⾏⼀个topology的⼦集, 并且worker只能对应于⼀个topology

Executors (threads): 在⼀个worker JVM进程中运⾏着多个Java线程。⼀个executor线程可以执⾏⼀个或多个tasks。但⼀般默认每 个executor只执⾏⼀个task。⼀个worker可以包含⼀个或多个executor, 每个component (spout或bolt)⾄少对应于⼀个executor, 所以可以说executor执⾏⼀个compenent的⼦集, 同时⼀个executor只能对应于⼀个component。

Tasks(bolt/spout instances)：Task就是具体的处理逻辑对象，每⼀个Spout和Bolt会被当作很多task在整个集群⾥⾯执⾏。每⼀个 task对应到⼀个线程，⽽stream grouping则是定义怎么从⼀堆task发射tuple到另外⼀堆task。你可以调⽤ TopologyBuilder.setSpout和TopBuilder.setBolt来设置并⾏度 — 也就是有多少个task。

配置并⾏度对于并发度的配置, 在storm⾥⾯可以在多个地⽅进⾏配置, 优先级为：defaults.yaml < storm.yaml < topology-specific configuration < internal component-specific configuration < external componentspecific configuration

worker processes的数⽬, 可以通过配置⽂件和代码中配置, worker就是执⾏进程, 所以考虑并发的效果, 数⽬⾄少应该⼤亍machines 的数⽬

executor的数⽬, component的并发线程数，只能在代码中配置(通过setBolt和setSpout的参数), 例如, setBolt("green-bolt", new GreenBolt(), 2)

tasks的数⽬, 可以不配置, 默认和executor1:1, 也可以通过setNumTasks()配置

Topology的worker数通过config设置，即执⾏该topology的worker（java）进程数。它可以通过 storm rebalance 命令任意调 整。

?

Config conf = new Config(); conf.setNumWorkers( 2 ); // use two worker processes topologyBuilder.setSpout( "blue-spout" , new BlueSpout(), 2 );

hint to 2

topologyBuilder.setBolt( "greenbolt" , new GreenBolt(), 2 ).setNumTasks( 4 ).shuffleGrouping( " spout" ); //set tasks number to 4 topologyBuilder.setBolt( "yellow-

bolt" , new YellowBolt(), 6 ).shuffleGrouping( "green-bolt" ); StormSubmitter.submitTopology( "mytopology" , conf, topologyBuilder.cre

动态的改变并⾏度Storm⽀持在不 restart topology 的情况下, 动态的改变(增减) worker processes 的数⽬和

## executors 的数⽬, 称为rebalancing. 通过Storm web UI，或者通过storm rebalance命令实现：

?

<table>
  <tr>
    <th> </th>
    <th>storm rebalance mytopology -n 5 -e blue-spout=3 -e yellow-bolt=10</th>
  </tr>
</table>


1

# 流分组策略 -Stream GroupingStream Grouping，告诉topology如何在两个组件之间发送tuple

定义⼀个topology的其中⼀步是定义每个bolt接收什么样的流作为输⼊。stream grouping就是⽤来定义⼀个stream应该如果分配数 据给bolts上⾯的多个tasks

Storm⾥⾯有7种类型的stream grouping，你也可以通过实现CustomStreamGrouping接⼝来实现⾃定义流分组

- 1. Shuffle Grouping 随机分组，随机派发stream⾥⾯的tuple，保证每个bolt task接收到的tuple数⽬⼤致相同。

- 2. Fields Grouping 按字段分组，⽐如，按"user-id"这个字段来分组，那么具有同样"user-id"的 tuple 会被分到相同的Bolt⾥的⼀个task， ⽽不同 的"user-id"则可能会被分配到不同的task。

- 3. All Grouping ⼴播发送，对亍每⼀个tuple，所有的bolts都会收到

- 4. Global Grouping 全局分组，整个stream被分配到storm中的⼀个bolt的其中⼀个task。再具体⼀点就是分配给id值最低的那个task。

- 5. None Grouping 不分组，这个分组的意思是说stream不关⼼到底怎样分组。⽬前这种分组和Shuffle grouping是⼀样的效果， 有⼀点不同的是storm 会把使⽤none grouping的这个bolt放到这个bolt的订阅者同⼀个线程⾥⾯去执⾏（如果可能的话）。

- 6. Direct Grouping 指向型分组， 这是⼀种⽐较特别的分组⽅法，⽤这种分组意味着消息（tuple）的发送者指定由消息接收者的哪个task处理这个消息。 只有被声明为 Direct Stream 的消息流可以声明这种分组⽅法。⽽且这种消息tuple必须使⽤ emitDirect ⽅法来发射。消息处理者可 以通过 TopologyContext 来获取处理它的消息的task的id (OutputCollector.emit⽅法也会返回task的id)

- 7. Local or shuffle grouping 本地或随机分组。如果⽬标bolt有⼀个或者多个task与源bolt的task在同⼀个⼯作进程中，tuple将会被随机发送给这些同进程中的 tasks。否则，和普通的Shuffle Grouping⾏为⼀致。


消息的可靠处理机制 在storm中，可靠的信息处理机制是从spout开始的。⼀个提供了可靠的处理机制的spout需要记录他发射出去 的tuple，当下游bolt处理tuple或者⼦tuple失败时spout能够重新发射。 Storm通过调⽤Spout的nextTuple()发送⼀个tuple。为 实现可靠的消息处理，⾸先要给每个发出的tuple带上唯⼀的ID，并且将ID作为参数传递给SoputOutputCollector的emit()⽅法：

collector.emit(new Values("value1","value2"), msgId); 给tuple指定ID告诉Storm系统，⽆论处理成功还是失败，spout都要接 收tuple树上所有节点返回的通知。如果处理成功，spout的ack()⽅法将会对编号是msgId的消息应答确认；如果处理失败或者超时， 会调⽤fail()⽅法。 bolt要实现可靠的信息处理机制包含两个步骤：1.当发射衍⽣的tuple时，需要锚定读⼊的tuple；2.当处理消息 成功或失败时分别确认应答或者报错。 锚定⼀个tuple的意思是，建⽴读⼊tuple和衍⽣出的tuple之间的对应关系，这样下游的bolt 就可以通过应答确认、报错或超时来加⼊到tuple树结构中。可以通过调⽤OutputCollector的emit()的⼀个重载函数锚定⼀个或⼀组

tuple：collector.emit(tuple, new Values(word)) ⾮锚定（collector.emit(new Values(word));）的tuple不会对数据流的可靠 性起作⽤。如果⼀个⾮锚定的tuple在下游处理失败，原始的根tuple不会重新发送。 超时时间可以通过任务级参数 Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS进⾏配置，默认超时值为30秒。 Storm 系统中有⼀组叫做"acker"的特殊的任 务，它们负责跟踪DAG（有向⽆环图）中的每个消息。acker任务保存了spout消息id到⼀对值的映射。第⼀个值就是spout的任务id， 通过这个id，acker就知道消息处理完成时该通知哪个spout任务。第⼆个值是⼀个64bit的数字，我们称之为"ack val"， 它是树中所有 消息的随机id的异或计算结果。ack val表示了整棵树的的状态，⽆论这棵树多⼤，只需要这个固定⼤⼩的数字就可以跟踪整棵树。当消 息被创建和被应答的时候都会有相同的消息id发送过来做异或。 每当acker发现⼀棵树的ack val值为0的时候，它就知道这棵树已经 被完全处理了。因为消息的随机ID是⼀个64bit的值，因此ack val在树处理完之前被置为0的概率⾮常⼩。假设你每秒钟发送⼀万个消 息，从概率上说，⾄少需要50,000,000年才会有机会发⽣⼀次错误。即使如此，也只有在这个消息确实处理失败的情况下才会有数据的 丢失！ 有三种⽅法可以去掉消息的可靠性： 1、将参数Config.TOPOLOGY_ACKERS设置为0，通过此⽅法，当Spout发送⼀个消息 的时候，它的ack⽅法将⽴刻被调⽤； 2、Spout发送⼀个消息时，不指定此消息的messageID。当需要关闭特定消息可靠性的时候， 可以使⽤此⽅法； 3、最后，如果你不在意某个消息派⽣出来的⼦孙消息的可靠性，则此消息派⽣出来的⼦消息在发送时不要做锚定， 即在emit⽅法中不指定输⼊消息。因为这些⼦孙消息没有被锚定在任何tuple tree中，因此他们的失败不会引起任何spout重新发送消 息。

