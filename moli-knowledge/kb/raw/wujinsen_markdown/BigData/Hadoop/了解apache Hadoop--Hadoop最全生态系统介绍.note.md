下⾯详细介绍⽣态系统的组成。

![image 1](<了解apache Hadoop--Hadoop最全生态系统介绍.note_images/imageFile1.png>)

- 1. HDFS HDFS（Hadoop Distributed File System，Hadoop分布式⽂件系统）是Hadoop体系中数据存储管理的 基础。它是⼀个⾼度容错的系统，能检测和应对硬件故障，⽤于在低成本的通⽤硬件上运⾏。HDFS简 化了⽂件的⼀致性模型，通过流式数据访问，提供⾼吞吐量应⽤程序数据访问功能，适合带有⼤型数 据集的应⽤程序。

- 2. MapReduce MapReduce是⼀种计算模型，⽤以进⾏⼤数据量的计算。Hadoop的MapReduce实现，和Common、 HDFS⼀起，构成了Hadoop发展初期的三个组件。MapReduce将应⽤划分为Map和Reduce两个步骤， 其中Map对数据集上的独⽴元素进⾏指定的操作，⽣成键-值对形式中间结果。Reduce则对中间结果中 相同“键”的所有“值”进⾏规约，以得到最终结果。MapReduce这样的功能划分，⾮常适合在⼤量计算机 组成的分布式并⾏环境⾥进⾏数据处理。

- 3. Hive Hive是Hadoop中的⼀个重要⼦项⽬，最早由Facebook设计，是建⽴在Hadoop基础上的数据仓库架构， 它为数据仓库的管理提供了许多功能，包括：数据ETL（抽取、转换和加载）⼯具、数据存储管理和⼤ 型数据集的查询和分析能⼒。Hive提供的是⼀种结构化数据的机制，定义了类似于传统关系数据库中 的类SQL语⾔：Hive QL，通过该查询语⾔，数据分析⼈员可以很⽅便地运⾏数据分析业务。

- 4. HBase


- Google发表了BigTable系统论⽂后，开源社区就开始在HDFS上构建相应的实现HBase。HBase是⼀个针 对结构化数据的可伸缩、⾼可靠、⾼性能、分布式和⾯向列的动态模式数据库。和传统关系数据库不 同，HBase采⽤了BigTable的数据模型：增强的稀疏排序映射表（Key/Value），其中，键由⾏关键字、 列关键字和时间戳构成。HBase提供了对⼤规模数据的随机、实时读写访问，同时，HBase中保存的数 据可以使⽤MapReduce来处理，它将数据存储和并⾏计算完美地结合在⼀起。
- 5. Pig Pig运⾏在Hadoop上，是对⼤型数据集进⾏分析和评估的平台。它简化了使⽤Hadoop进⾏数据分析的 要求，提供了⼀个⾼层次的、⾯向领域的抽象语⾔：Pig Latin。通过Pig Latin，数据⼯程师可以将复杂 且相互关联的数据分析任务编码为Pig操作上的数据流脚本，通过将该脚本转换为MapReduce任务链， 在Hadoop上执⾏。和Hive⼀样，Pig降低了对⼤型数据集进⾏分析和评估的⻔槛。

- 6. Hadoop Common

从Hadoop 0.20版本开始，原来Hadoop项⽬的Core部分更名为Hadoop Common。Common为Hadoop 的其他项⽬提供了⼀些常⽤⼯具，主要包括系统配置⼯具Configuration、远程过程调⽤RPC、序列化机 制和Hadoop抽象⽂件系统FileSystem等。它们为在通⽤硬件上搭建云计算环境提供基本的服务，并为 运⾏在该平台上的软件开发提供了所需的API。

- 7. ZooKeeper 在分布式系统中如何就某个值（决议）达成⼀致，是⼀个⼗分重要的基础问题。ZooKeeper作为⼀个分 布式的服务框架，解决了分布式计算中的⼀致性问题。在此基础上，ZooKeeper可⽤于处理分布式应⽤ 中经常遇到的⼀些数据管理问题，如统⼀命名服务、状态同步服务、集群管理、分布式应⽤配置项的 管理等。ZooKeeper常作为其他Hadoop相关项⽬的主要组件，发挥着越来越重要的作⽤。

- 8. Avro

Avro由Doug Cutting牵头开发，是⼀个数据序列化系统。类似于其他序列化机制，Avro可以将数据结构

或者对象转换成便于存储和传输的格式，其设计⽬标是⽤于⽀持数据密集型应⽤，适合⼤规模数据的 存储与交换。Avro提供了丰富的数据结构类型、快速可压缩的⼆进制数据格式、存储持久性数据的⽂ 件集、远程调⽤RPC和简单动态语⾔集成等功能。

- 9. Mahout


- Mahout起源于2008年，最初是Apache Lucent的⼦项⽬，它在极短的时间内取得了⻓⾜的发展，现在 是Apache的顶级项⽬。Mahout的主要⽬标是创建⼀些可扩展的机器学习领域经典算法的实现，旨在帮 助开发⼈员更加⽅便快捷地创建智能应⽤程序。Mahout现在已经包含了聚类、分类、推荐引擎（协同 过滤）和频繁集挖掘等⼴泛使⽤的数据挖掘⽅法。除了算法，Mahout还包含数据的输⼊/输出⼯具、与 其他存储系统（如数据库、MongoDB 或Cassandra）集成等数据挖掘⽀持架构。
- 10. X-RIME X-RIME是⼀个开源的社会⽹络分析⼯具，它提供了⼀套基于Hadoop的⼤规模社会⽹络/复杂⽹络分析 ⼯具包。X-RIME在MapReduce 的框架上对⼗⼏种社会⽹络分析算法进⾏了并⾏化与分布式化，从⽽实 现了对互联⽹级⼤规模社会⽹络/复杂⽹络的分析。它包括HDFS存储系统上的⼀套适合⼤规模社会⽹络 分析的数据模型、基于MapReduce实现的⼀系列社会⽹络分析分布式并⾏算法和X-RIME处理模型，即 X-RIME⼯具链等三部分。

- 11. Crossbow Crossbow是在Bowtie和SOAPsnp基础上，结合Hadoop的可扩展⼯具，该⼯具能够充分利⽤集群进⾏⽣ 物计算。其中，Bowtie是⼀个快速、⾼效的基因短序列拼接⾄模板基因组⼯具；SOAPsnp则是⼀个重 测序⼀致性序列建造程序。它们在复杂遗传病和肿瘤易感的基因定位，到群体和进化遗传学研究中发 挥着重要的作⽤。Crossbow利⽤了Hadoop Stream，将Bowtie、SOAPsnp上的计算任务分布到Hadoop 集群中，满⾜了新⼀代基因测序技术带来的海量数据存储及计算分析要求。

- 12. Chukwa Chukwa是开源的数据收集系统，⽤于监控⼤规模分布式系统（2000+以上的节点, 系统每天产⽣的监 控数据量在T级别）。它构建在Hadoop的HDFS和MapReduce基础之上，继承了Hadoop的可伸缩性和鲁 棒性。Chukwa包含⼀个强⼤和灵活的⼯具集，提供了数据的⽣成、收集、排序、去重、分析和展示等 ⼀系列功能，是Hadoop使⽤者、集群运营⼈员和管理⼈员的必备⼯具。

- 13. Flume Flume是Cloudera开发维护的分布式、可靠、⾼可⽤的⽇志收集系统。它将数据从产⽣、传输、处理并 最终写⼊⽬标的路径的过程抽象为数据流，在具体的数据流中，数据源⽀持在Flume中定制数据发送 ⽅，从⽽⽀持收集各种不同协议数据。同时，Flume数据流提供对⽇志数据进⾏简单处理的能⼒，如过 滤、格式转换等。此外，Flume还具有能够将⽇志写往各种数据⽬标（可定制）的能⼒。总的来说， Flume是⼀个可扩展、适合复杂环境的海量⽇志收集系统。


- 14. Sqoop Sqoop是SQL-to-Hadoop的缩写，是Hadoop的周边⼯具，它的主要作⽤是在结构化数据存储与Hadoop 之间进⾏数据交换。Sqoop可以将⼀个关系型数据库（例如MySQL、Oracle、PostgreSQL等）中的数据 导⼊Hadoop的HDFS、Hive中，也可以将HDFS、Hive中的数据导⼊关系型数据库中。Sqoop充分利⽤了 Hadoop的优点，整个数据导⼊导出过程都是⽤MapReduce实现并⾏化，同时，该过程中的⼤部分步骤 ⾃动执⾏，⾮常⽅便。

- 15. Oozie 在Hadoop中执⾏数据处理⼯作，有时候需要把多个作业连接到⼀起，才能达到最终⽬的。针对上述需 求，Yahoo开发了开源⼯作流引擎Oozie，⽤于管理和协调多个运⾏在Hadoop平台上的作业。在Oozie 中，计算作业被抽象为动作，控制流节点则⽤于构建动作间的依赖关系，它们⼀起组成⼀个有向⽆环 的⼯作流，描述了⼀项完整的数据处理⼯作。Oozie⼯作流系统可以提⾼数据处理流程的柔性，改善 Hadoop集群的效率，并降低开发和运营⼈员的⼯作量。

- 16. Karmasphere Karmasphere包括Karmasphere Analyst和Karmasphere Studio。其中，Analyst提供了访问保存在 Hadoop⾥⾯的结构化和⾮结构化数据的能⼒，⽤户可以运⽤SQL或其他语⾔，进⾏即时查询并做进⼀ 步的分析。Studio则是基于NetBeans的MapReduce集成开发环境，开发⼈员可以利⽤它⽅便快速地创 建基于Hadoop的MapReduce应⽤。同时，该⼯具还提供了⼀些可视化⼯具，⽤于监控任务的执⾏，显 示任务间的输⼊输出和交互等。需要注意的是，在上⾯提及的这些项⽬中，Karmasphere是唯⼀不开源 的⼯具。

- 17.JaqlJaql 针对半结构化⼤数据量的查询语⾔应运⽽⽣，运⽤Jaql 语⾔，程序员并不需要关⼼ MapReduce 框架原理⽽只需要应⽤更容易理解和⼈性化的脚本语⾔。

- 18.NutchNutch 是⼀个开源Java 实现的搜索引擎。它提供了我们运⾏⾃⼰的搜索引擎所需的全部⼯ 具。包括全⽂搜索和Web爬⾍。 htp:/ w.aboutyun.com/forum.php?mod=viewthread&tid=6521&highlight=hadop%2B%2B%2 B%2B%C9%FA%C%AC%CF%B5%CD%B3


