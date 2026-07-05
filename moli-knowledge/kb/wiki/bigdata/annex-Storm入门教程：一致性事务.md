---
title: Storm入门教程：一致性事务.note（原文插图 annex）
slug: annex-Storm入门教程：一致性事务
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/storm/Storm入门教程：一致性事务.note.md
related: [flink-流批一体入门]
created: 2026-07-05
updated: 2026-07-05
---

Storm是⼀个分布式的流处理系统，利⽤anchor和ack机制保证所有tuple都被成功处理。如果tuple出 错，则可以被重传，但是如何 保证出错的tuple只被处理⼀次呢？Storm提供了⼀套事务性组件 Transaction Topology，⽤来解决这个问题。 Transactional Topology⽬前已经不再维护，由Trident来实现事务性topology，但是原理相同。 ⼀、⼀致性事务的设计 Storm如何实现即对tuple并⾏处理，⼜保证事务性。本节从简单的事务性实现⽅法⼊⼿，逐步引出 Transactional Topology的原理。

- 1、简单设计⼀：强顺序流 保证tuple只被处理⼀次，最简单的⽅法就是将tuple流变成强顺序的，并且每次只处理⼀个tuple。从1 开始，给每个tuple都顺序加上 ⼀个id。在处理tuple的时候，将处理成功的tuple id和计算结果存在数 据库中。下⼀个tuple到来的时候，将其id与数据库中的id做⽐较。如果相同，则说明这个tuple已经被 成功处理过了，忽略 它；如果不同，根据强顺序性，说明这个tuple没有被处理过，将它的id及计算结 果更新到数据库中。 以统计消息总数为例。每来⼀个tuple，如果数据库中存储的id 与当前tuple id不同，则数据库中的消息 总数加1，同时更新数据库中的当前tuple id值。如图：

但是这种机制使得系统⼀次只能处理⼀个tuple，⽆法实现分布式计算。

- 2、简单设计⼆：强顺序batch流 为了实现分布式，我们可以每次处理⼀批tuple，称为⼀个batch。⼀个batch中的tuple可以被并⾏处 理。 我们要保证⼀个batch只被处理⼀次，机制和上⼀节类似。只不过数据库中存储的是batch id。batch的 中间计算结果先存在局部变量中，当⼀个batch中的所有tuple都被处理完之后，判断batch id，如果跟 数据库中的id不同，则将中间计算结果更新到数据库中。 如何确保⼀个batch⾥⾯的所有tuple都被处理完了呢？可以利⽤Storm提供的CordinateBolt。如图：

但是强顺序batch流也有局限，每次只能处理⼀个batch，batch之间⽆法并⾏。要想实现真正的分布式 事务处理，可以使⽤storm提供的Transactional Topology。在此之前，我们先详细介绍⼀下 CordinateBolt的原理。

- 3、CordinateBolt原理


![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

CordinateBolt具体原理如下：

真正执⾏计算的bolt外⾯封装了⼀个CordinateBolt。真正执⾏任务的bolt我们称为real bolt。

每个CordinateBolt记录两个值：有哪些task给我发送了tuple（根据topology的grouping信息）； 我要给哪些tuple发送信息（同样根据groping信息）

Real bolt发出⼀个tuple后，其外层的CordinateBolt会记录下这个tuple发送给哪个task了。

等所有的tuple都发送完了之后，CordinateBolt通过另外⼀个特殊的stream以emitDirect的⽅式告 诉所有 它发送过tuple的task，它发送了多少tuple给这个task。下游task会将这个数字和⾃⼰已经接 收到的tuple数量做对⽐，如果相等，则 说明处理完了所有的tuple。

下游CordinateBolt会重复上⾯的步骤，通知其下游。

整个过程如图所示：

![image 3](assets/imageFile3.png)

CordinateBolt主要⽤于两个场景：

DRPC

Transactional Topology

CordinatedBolt对于业务是有侵⼊的，要使⽤CordinatedBolt提供的功能，你必须要保证你的每个 bolt发送的每个 tuple的第⼀个field是request-id。 所谓的“我已经处理完我的上游”的意思是说当前这 个bolt对于当前这个request-id所需要做的⼯作做完了。这个request-id在DRPC ⾥⾯代表⼀个DRPC请 求；在Transactional Topology⾥⾯代表⼀个batch。

- 4、Trasactional Topology Storm提供的Transactional Topology将batch计算分为proces和comit两个阶段。Proces阶段可以 同时处理多个batch，不⽤保证顺序 性；comit阶段保证batch的强顺序性，并且⼀次只能处理⼀个 batch，第1个batch成功提交之前，第2个batch不能被提交。 还是以统计消息总数为例，以下代码来⾃storm-starter⾥⾯的TransactionalGlobalCount。 MemoryTransactionalSpout spout = new MemoryTransactionalSpout(DATA,new Fields(“word“), PARTITION_TAKE_PER_BATCH); TransactionalTopologyBuilder builder = new TransactionalTopologyBuilder(“global-count“, “spout“, spout, 3); builder.setBolt(“partial-count“, new BatchCount(), 5).noneGrouping(“spout“); builder.setBolt(“sum“, new UpdateGlobalCount().globalGrouping(“partial-count“); TransactionalTopologyBuilder共接收四个参数。


这个Transactional Topology的id。Id⽤来在Zokeper中保存当前topology的进度，如果这个 topology重启，可以继续之前的进度执⾏。

Spout在这个topology中的id

⼀个TransactionalSpout。⼀个Trasactional Topology中只能有⼀个TrasactionalSpout.在本例中是 ⼀个MemoryTransactionalSpout，从⼀个内存变量（DATA）中读取数据。

TransactionalSpout的并⾏度（可选）。

下⾯是BatchCount的定义：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.


public static clas BatchCount extends BaseBatchBolt { Object _id; BatchOutputColector _colector; int _count = 0; @Overide public void prepare(Map conf, TopologyContext context, BatchOutputColector colector, Object id) { _colector = colector; _id = id; } @Overide public void execute(Tuple tuple) { _count+; } @Overide public void finishBatch() { _colector.emit(new Values(_id, _count); } @Overide public void declareOutputFields(OutputFieldsDeclarer declarer) { declarer.declare(new Fields(“id“, “count“); } }

BatchCount的prepare⽅法的最后⼀个参数是batch id，在Transactional Tolpoloyg⾥⾯这id是⼀个 TransactionAtempt对象。 Transactional Topology⾥发送的tuple都必须以TransactionAtempt作为第⼀个field，storm根据这个 field来判断tuple属于哪⼀个batch。 TransactionAtempt包含两个值：⼀个transaction id，⼀个atempt id。transaction id的作⽤就是我 们上⾯介绍的对于每个batch中的tuple是唯⼀的，⽽且不管这个batch replay多少次都是⼀样的。 atempt id是对于每个batch唯⼀的⼀个id， 但是对于同⼀个batch，它replay之后的atempt id跟 replay之前就不⼀样了， 我们可以把atempt id理解成replay-times， storm利⽤这个id来区别⼀个 batch发射的tuple的不同版本。

execute⽅法会为batch⾥⾯的每个tuple执⾏⼀次，你应该把这个batch⾥⾯的计算状态保持在⼀个本 地变量⾥⾯。对于这个例⼦来说， 它在execute⽅法⾥⾯递增tuple的个数。 最后， 当这个bolt接收到某个batch的所有的tuple之后， finishBatch⽅法会被调⽤。这个例⼦⾥⾯的 BatchCount类会在这个时候发射它的局部数量到它的输出流⾥⾯去。 下⾯是UpdateGlobalCount类的定义：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.


public static clas UpdateGlobalCount extends BaseTransactionalBolt implements IComiter { TransactionAtempt _atempt; BatchOutputColector _colector; int _sum = 0; @Overide public void prepare(Map conf, TopologyContext context, BatchOutputColector colector, TransactionAtempt atempt) { _colector = colector; _atempt = atempt; } @Overide public void execute(Tuple tuple) { _sum+=tuple.getInteger(1); } @Overide public void finishBatch() { Value val = DATABASE.get(GLOBAL_COUNT_KEY); Value newval; if(val = nul | !val.txid.equals(_atempt.getTransactionId( ) { newnewval = new Value(); newval.txid = _atempt.getTransactionId(); if(val =nul) { newval.count = _sum; } else { newval.count = _sum + val.count; } DATABASE.put(GLOBAL_COUNT_KEY, newval); } else { newval = val; } _colector.emit(new Values(_atempt, newval.count);

- 33.
- 34.
- 35.
- 36.
- 37.
- 38.


} @Overide public void declareOutputFields(OutputFieldsDeclarer declarer) { declarer.declare(new Fields(“id“, “sum“); } }

UpdateGlobalCount实现了IComiter接⼝，所以storm只会在comit阶段执⾏finishBatch⽅法。⽽ execute⽅法可以在任何阶段完成。 在UpdateGlobalCount的finishBatch⽅法中，将当前的transaction id与数据库中存储的id做⽐较。如 果相同，则忽略这个batch；如果不同，则把这个batch的计算结果加到总结果中，并更新数据库。 Transactional Topolgy运⾏示意图如下：

![image 4](assets/imageFile4.png)

下⾯总结⼀下Transactional Topology的⼀些特性：

Transactional Topology将事务性机制都封装好了，其内部使⽤CordinateBolt来保证⼀个batch中 的tuple被处理完。

TransactionalSpout只能有⼀个，它将所有tuple分为⼀个⼀个的batch，⽽且保证同⼀个batch的 transaction id始终⼀样。

BatchBolt处理batch在⼀起的tuples。对于每⼀个tuple调⽤execute⽅法，⽽在整个batch处理完成 的时候调⽤finishBatch⽅法。

如果BatchBolt被标记成Comiter，则只能在comit阶段调⽤finishBolt⽅法。⼀个batch的 comit阶段由storm保证只在前⼀个batch成功提交之后才会执⾏。并且它会重试直到topology⾥ ⾯的所有bolt在comit完成提 交。

Transactional Topology隐藏了anchor/ack框架，它提供⼀个不同的机制来fail⼀个batch，从⽽使得 这个batch被replay。

⼆、Trident介绍 Trident是Storm之上的⾼级抽象，提供了joins，grouping，agregations，fuctions和filters等接⼝。 如果你使⽤过Pig或Cascading，对这些接⼝就不会陌⽣。 Trident将stream中的tuples分成batches进⾏处理，API封装了对这些batches的处理过程，保证tuple 只被处理⼀次。处理batches中间结果存储在TridentState对象中。 Trident事务性原理这⾥不详细介绍，有兴趣的读者请⾃⾏查阅资料。

htp:/xumingming.sinap.com/736/twiter-storm-transactional-topolgoy/ htp:/xumingming.sinap.com/81/twiter-storm-code-analysis-cordinated-bolt/ htps:/github.com/nathanmarz/storm/wiki/Trident-tutorial

参考：

【编辑推荐】

- 1.
- 2.
- 3.
- 4.


Storm⼊⻔教程：前⾔ Storm⼊⻔教程：构建Topology Storm⼊⻔教程：安装部署步骤详解 storm⼊⻔教程：消息的可靠处理
