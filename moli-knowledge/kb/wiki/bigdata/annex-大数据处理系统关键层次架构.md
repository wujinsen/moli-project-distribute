---
title: 大数据处理系统关键层次架构.note（原文插图 annex）
slug: annex-大数据处理系统关键层次架构
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/大数据技术文章/大数据处理系统关键层次架构.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

在数据存储层，还有很多类似的系统和某些系统的变种，这⾥，我仅仅列出较为出名的⼏个。如漏掉 某些重要系统，还请谅解。

![image 1](assets/imageFile1.png)

以下是对上图中各层次架构的说明 ⼀、数据存储层 宽泛地讲，据对⼀致性(consistency)要求的强弱不同， 策略，可分为ACID和BASE两 ⼤阵营。 ACID是指数据库事务具有的四个特性：原⼦性(Atomicity)、⼀致性(Consistency)、隔离性 (Isolation)、持久性(Durability)。ACID中的⼀致性要求⽐较强，事务执⾏的结果必须是使数据库从⼀个 ⼀致性状态变到另⼀个⼀致性状态。 BASE对⼀致性要求较弱，它的三个特征分别是：基本可⽤(Basicaly Available), 软状态/柔性事务 (Soft-state，即状态可以有⼀段时间的不同步), 最终⼀致性(Eventual consistency)。BASE还进⼀步细 分基于键值的，基于⽂档的和基于列和图形的 – 细分的依据取决于底层架构和所⽀持的数据结构(注： BASE完全不同于ACID模型，它以牺牲强⼀致性，获得基本可⽤性和柔性可靠性，并要求达到最终⼀致 性)。 在数据存储层，还有很多类似的系统和某些系统的变种，这⾥，我仅仅列出较为出名的⼏个。如漏掉 某些重要系统，还请谅解。

分布式数据存储

- 1、BASE


- (1)键值存储(Key Value Stores) Dynamo：这是由亚⻢逊⼯程师们设计的基于键值的⾼可⽤的分布式存储系统(注：Dynamo放弃了数据 建模的能⼒，所有的数据对象采⽤最简单的Key-value模型存储，可简单地将Dynamo理解为⼀个巨⼤ 的Map。Dynamo是牺牲了部分⼀致性，来换取整个系统的⾼可⽤性)。 Casandra：这是由Facebok⼯程师设计的⼀个离散的分布式结构化存储系统，受亚⻢逊的Dynamo 启发，Casandra采⽤的是⾯向多维的键值或⾯向列的数据存储格式(注：Casandra可⽤来管理分布 在⼤量廉价服务器上的巨量结构化数据，并同时提供没有单点故障的⾼可⽤服务)。 Voldemort：这⼜是⼀个受亚⻢逊的Dynamo启发的分布式存储作品，由全球最⼤的职业社交⽹站 LinkedIn的⼯程师们开发⽽成。
- (2)⾯向列的存储(Column Oriented Stores) BigTable：Bigtable是⼀个基于Gogle⽂件系统的分布式数据存储系统，是为⾕歌打拼天下的“三驾⻢ ⻋”之⼀，另外两驾⻢⻋分别是分布式锁服务系统Chuby和下⽂将提到的MapReduce。 HBase：Hbase是⼀个分布式的、⾯向列的开源数据库。其设计理念源⾃⾕歌的 BigTable，⽤Java语 ⾔编写⽽成。 Hypertable：Hypertable也是⼀个开源、⾼性能、可伸缩的数据库，它采⽤与Gogle的Bigtable类似 的模型。
- (3)⾯向⽂档的存储(Document Oriented Stores) CouchDB：这是⼀款⾯向⽂档的、开源数据存储管理系统。 MongoDB：是⽬前⾮常流⾏的⼀种⾮关系型(NoSQL)数据库。
- (4)⾯向图(Graph)的存储 Neo4j：Neo4j是⼀款⽬前最为流⾏的⾼性能NoSQL 图数据库，它使⽤图来描述数据模型，把数据保存 为图中的节点以及节点之间的关系。这是最流⾏的图数据库。 Titan：Titan是⼀款Apache许可证框架下的分布式的开源图数据库，特别为存储和处理⼤规模图⽽做 了⼤量优化。


- 2、ACID Megastore：这是⼀个构建于BigTable之上的、⾼可⽤的分布式存储系统。 Spaner：这是由⾕歌研发的、可扩展的、全球分布式的、同步复制数据库，⽀持SQL查询访问。 MESA：亦是由⾕歌研发的、跨地域复制(geo-replicated)、⾼可⽤的、可容错的、可扩展的近实时数 据仓库系统。 CockroachDB：该系统是由Gogle前⼯程师Spencer Kimbal领导开发的Spaner 的开源版本。 ⼆、资源管理器层(Resource Managers) 第⼀代Hadop的⽣态系统，其资源管理是以整体单⼀的调度器起家的，其代表作品为YARN。⽽当前 的调度器则是朝着分层调度的⽅向演进(Mesos则是这个⽅向的代表作)，这种分层的调度⽅式，可以管 理不同类型的计算⼯作负载，从⽽可获取更⾼的资源利⽤率和调度效率。 YARN：这是新⼀代的MapReduce计算框架，简称MRv2，它是在第⼀代MapReduce的基础上演变⽽ 来的(注：MRv2的设计初衷是，为了解决第⼀代Hadop系统扩展性差、不⽀持多计算框架等问题。 Mesos：这是⼀个开源的计算框架，可对多集群中的资源做弹性管理。


这些计算框架和调度器之间是松散耦合的，调度器的主要功能就是基于⼀定的调度策略和调度配置， 完成作业调度，以达到⼯作负载均衡，使有限的资源有较⾼的利⽤率。 三、调度器(Schedulers)

- (1)作业调度器，通常以插件的⽅式加载于计算框架之上，常⻅的作业调度器有4种： 计算能⼒调度器 公平调度器 延迟调度 公平与能⼒调度器
- (2)协调器(Cordination) 在分布式数据系统中，协调器主要⽤于协调服务和进⾏状态管理。 Paxos：Gogle的Chuby和Apache的Zokeper，都是⽤Paxos作为其理论基础实现的。 Chuby：本质上就是前⽂提到的Paxos的⼀个实现版本，主要⽤于⾕歌分布式锁服务。 Zokeper：这是Apache Hadop框架下的Chuby开源版本。它不仅仅提供简单地上锁服务，⽽事实 上，它还是⼀个通⽤的分布式协调器，其设计灵感来⾃⾕歌的Chuby。


四、计算框架(Computational Frameworks)

- (0)运⾏时计算框架 可为不同种类的计算，提供运⾏时(runtime)环境。最常⽤的是运⾏时计算框架是Spark和Flink。 Spark：Spark是⼀个基于内存计算的开源的集群计算系统，其⽬的在于，让数据分析更加快速。Spark 是由加州⼤学伯克利分校的AMP实验室采⽤Scala语⾔开发⽽成。Spark的内存计算框架，适合各种迭 代算法和交互式数据分析，能够提升⼤数据处理的实时性和准确性，现已逐渐获得很多企业的⽀持， 如阿⾥巴巴、百度、⽹易、英特尔等公司均是其⽤户。 Flink：这是⼀个⾮常类似于Spark的计算框架，但在迭代式数据处理上，⽐Spark更给⼒(注：⽬前⼤数 据分析引擎Flink，已升级成为Apache顶级项⽬)。 Spark和Flink都属于基础性的⼤数据处理引擎。具体的计算框架，⼤体上，可根据采⽤的模型及延迟的 处理不同，来进⾏分⻔别类。
- (1)批处理(Batch) MapReduce
- (2)迭代式(BSP) Pregel：Pregel是⼀种⾯向图算法的分布式编程框架，其采⽤的是迭代式的计算模型。它被称之为 Gogle后Hadop时代的新“三驾⻢⻋”之⼀。另外两驾⻢⻋分别是：“交互式”⼤数据分析系统Dremel和 ⽹络搜索引擎Cafeine。 Giraph：该系统建模于⾕歌的Pregel，可视为Pregel的开源版本，它是⼀个基于 Hadop架构的、可扩 展的分布式迭代图处理系统。


- GraphX：这是⼀个同时采⽤图并⾏计算和数据并⾏的计算框架，GraphX最先是加州⼤学伯克利分校 AMPLab实验室的⼀个分布式图计算框架项⽬，后来整合到Spark中，成为其中的⼀个核⼼组件。 GraphX最⼤的贡献在于，在Spark之上提供⼀栈式数据解决⽅案，可⽅便⾼效地完成图计算的⼀整套 流⽔作业。 Hama：是⼀个构建Hadop之上的基于BSP模型的分布式计算引擎，Hama的运⾏环境需要关联 Zokeper、HBase、HDFS 组件。Hama中最关键的技术，就是采⽤了BSP模型(Bulk Synchronous Paralel，即整体同步并⾏计算模型，⼜名⼤同步模型)。
- (3)流式(Streaming) Storm：Storm有时也被⼈们称为实时处理领域的Hadop，它⼤⼤简化了⾯向庞⼤规模数据流的处理 机制，从⽽在实时处理领域扮演着重要⻆⾊。 Samza：这是⼀款由Linkedin公司开发的分布式的流式数据处理框架(注：所谓流式数据，是指要在处 理单位内得到的数据，这种⽅式更注重于实时性，流式数据有时也称为快数据)。 Spark流：Spark Streaming是Spark 核⼼API的⼀个扩展，它并不会像Storm那样逐个处理数据流，⽽ 是在处理前，按时间间隔预先将其切分为很多⼩段的批处理作业。
- (4)交互式(Interactive) Dremel该论⽂是多个基于Hadop的开源SQL系统的理论基础。 Impala：这是⼀个⼤规模并⾏处理(MP)式 SQL ⼤数据分析引擎，Impala像Dremel⼀样，其借鉴了 MP(Masively Paralel Procesing，⼤规模并⾏处理)并⾏数据库的思想，抛弃了MapReduce这个不 太适合做SQL查询的范式，从⽽让Hadop⽀持处理交互式的⼯作负载。 Dril：这是⾕歌 Dremel的开源版本，Dril是⼀个低延迟的、能对海量数据(包括结构化、半结构化及嵌 套数据)实施交互式查询的分布式数据引擎。 Shark：Shark即“Hive on Spark”的含义，本质上是通过Hive的HQL解析，把HQL翻译成Spark上的 RD操作。然后通过Hive的元数据获，取数据库⾥的表信息。HDFS上的数据和⽂件，最后会由Shark 获取，并放到Spark上运算。Shark基于 Scala语⾔的算⼦推导，可实现良好的容错机制，对执⾏失败 的⻓/短任务，均能从上⼀个“快照点(Snapshot)”进⾏快速恢复。 Dryad：Dryad是⼀个通⽤的粗颗粒度的分布式计算和资源调度引擎，其核⼼特性之⼀，就是允许⽤户 ⾃⼰构建DAG调度拓扑图。 Tez：其核⼼思想来源于Dryad，可视为利⽤Yarn(即MRv2)对Dryad的开源实现。Apache Tez是基于 Hadop Yarn之上的DAG计算框架。 BlinkDB：可在抽样数据上实现交互式查询，其呈现出的查询结果，附带有误差标识。BlinkDB 是⼀个 ⽤于在海量数据上运⾏交互式 SQL 查询的⼤规模并⾏查询引擎。BlinkDB允许⽤户通过适当降低数据 精度，对数据进⾏先采样后计算，其通过其独特的优化技术，实现了⽐Hive快百倍的交互式查询速 度，⽽查询进度误差仅降低2~10%。
- (5)实时系统(RealTime) Druid：这是⼀个开源的分布式实时数据分析和存储系统，旨在快速处理⼤规模的数据，并能做到快速 查询和分析。


Pinot：这是由LinkedIn公司出品的⼀个开源的、实时分布式的 OLAP数据分析存储系统，⾮常类似于 前⾯提到的Druid，LinkedIn 使⽤它实现低延迟可伸缩的实时分析。 五、数据分析层(Data Analysis) 数据分析层中的⼯具，涵盖范围很⼴，从诸如SQL的声明式编程语⾔，到诸如Pig的过程化编程语⾔， 均有涉及。另⼀⽅⾯，数据分析层中的库也很丰富，可⽀持常⻅的数据挖掘和机器学习算法，这些类 库可拿来即⽤，甚是⽅便。

- (1)⼯具(Tols) Pig：Pig Latin原是⼀种⼉童⿊话，属于是⼀种英语语⾔游戏，形式是在英语上加上⼀点规则使发⾳改 变，让⼤⼈们听不懂，从⽽完成孩⼦们独懂的交流。雅⻁的⼯程师们于208年发表在SIGMOD的⼀篇 论⽂，论⽂的题⽬是“Pig Latin：并不是太⽼外的⼀种数据语⾔”，⾔外之意，他们发明了⼀种数据处理 的“⿊话”⸺Pig Latin，⼀开始你可能不懂，等你熟悉了，就会发现这种数据查询语⾔的乐趣所在。 Hive：Hive是⼀个建⽴于 Hadop 上的数据仓库基础构架。它⽤来进⾏数据的提取、转化和加载(即 Extract-Transform-Load ，ETL)，它是⼀种可以存储、查询和分析存储在 Hadop 中的⼤规模数据的 机制。 Phoenix：它是 HBase 的 SQL 驱动，Phoenix可将 SQL 查询转成 HBase 的扫描及相应的动作。
- (2)库(Libraires) MLlib：这是在Spark计算框架中对常⽤的机器学习算法的实现库，该库还包括相关的测试和数据⽣成 器。 SparkR：这是AMPLab发布的⼀个R开发包，为Apache Spark提供轻量级的前端。 Mahout：这是⼀个功能强⼤的数据挖掘⼯具，是⼀个基于传统Map Reduce的分布式机器学习框架， Mahout的中⽂含义就是“驭象之⼈”，⽽Hadop的Logo正是⼀头⼩⻩象。很明显，这个库是帮助⽤户 ⽤好Hadop这头难⽤的⼤象。 六、数据集成层(Data Integration) 数据集成框架提供了良好的机制，以协助⾼效地摄取和输出⼤数据系统之间的数据。从业务流程线到 元数据框架，数据集成层皆有涵盖，从⽽提供全⽅位的数据在整个⽣命周期的管理和治理。


- (1)摄⼊/消息传递(Ingest/Mesaging) Flume：这是Apache旗下的⼀个分布式的、⾼可靠的、⾼可⽤的服务框架，可协助从分散式或集中式 数据源采集、聚合和传输海量⽇志。 Sqop：该系统主要⽤来在Hadop和关系数据库中传递数据，Sqop⽬前已成为Apache的顶级项⽬ 之⼀。 Kafka：这是由LinkedIn开发的⼀个分布式消息系统，由Scala编写⽽成。由于可⽔平扩展、吞吐率⾼等 特性，得到⼴泛应⽤。
- (2)ETL/⼯作流 ETL是数据抽取(Extract)、清洗(Cleaning)、转换(Transform)、装载(Load)的过程，是构建数据仓库的 重要⼀环。 Crunch：这是Apache旗下的⼀套Java API函数库，它能够⼤⼤简化编写、测试、运⾏MapReduce 处 理⼯作流的程序。


- Falcon：这是Apache旗下的Falcon⼤数据管理框架，可以帮助⽤户⾃动迁移和处理⼤数据集合。 Cascading：这是⼀个架构在Hadop上的API函数库，⽤来创建复杂的可容错的数据处理⼯作流。 Oozie：是⼀个⼯作流引擎，⽤来协助Hadop作业管理，Oozie字⾯含义是驯象之⼈，其寓意和 Mahout⼀样，帮助⽤户更好地搞定Hadop这头⼤象。
- (3)元数据(Metadata) HCatalog： 它提供了⾯向Apache Hadop的数据表和存储管理服务，Apache HCatalog提供⼀个共享 的模式和数据类型的机制，它抽象出表，使⽤户不必关⼼数据怎么存储，并提供了可操作的跨数据处 理⼯具。
- (4)序列化(Serialization) Protocol Bufers：由Gogle推⼴的⼀种与语⾔⽆关的、对结构化数据进⾏序列化和反序列化的机制。 Avro：这是⼀个建模于Protocol Bufers之上的、Hadop⽣态系统中的⼦项⽬，Avro本身既是⼀个序 列化框架，同时也实现了RPC的功能。 七、操作框架(Operational Frameworks) 最后，我们还需要⼀个操作性框架，来构建⼀套衡量标准和测试基准，从⽽来评价各种计算框架的性 能优劣。在这个操作性框架中，还需要包括性能优化⼯具，借助它来平衡⼯作负载。


- (1)监测管理框架(Monitoring Frameworks) OpenTSDB：这是构建于HBase之上的实时性能评测系统。 Ambari：这是⼀款基于Web的系统，⽀持Apache Hadop集群的供应、管理和监控。
- (2)基准测试(Benchmarking) YCSB：YCSB是雅⻁云服务基准测试(Yaho! Cloud Serving Benchmark)的简写。⻅名知意，它是由 雅⻁出品的⼀款通⽤云服务性能测试⼯具。 GridMix：该系统通过运⾏⼤量合成的作业，对Hadop系统进⾏基准测试，从⽽获得性能评价指标。
