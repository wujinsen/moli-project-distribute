htps:/ w.cnblogs.com/importbigdata/p/1521390.html

在上⼀章节中，我们讲到实时数仓的建设，互联⽹⼤数据技术发展到今天，各个领域基本已经成熟，有各式各样的解决⽅案可 以供我们选择。 在实时数仓建设中，解决⽅案成熟，消息队列Kafka、Redis、Hbase鲜有敌⼿，⼏乎已成垄断之势。⽽OLAP的选择则制约整 个实时数仓的能⼒。开源盛世的今天，可以供我们选择和使⽤的OLAP数据库令⼈眼花缭乱，这章我们选取了⼏个最常⽤的 OLAP开源数据引擎进⾏分析，希望能给正在做技术选型和未来架构升级的你提供⼀些帮助。 本⽂给出了常⽤的开源OLAP引擎的性能测评：

https://blog.csdn.net/oDaiLiDong/article/details/86570211

## OLAP百家争鸣

### OLAP简介

OLAP，也叫联机分析处理（Online Analytical Processing）系统，有的时候也叫DSS决策⽀持系统，就是我们说的数据仓 库。与此相对的是OLTP（on-line transaction processing）联机事务处理系统。 联机分析处理 (OLAP) 的概念最早是由关系数据库之⽗E.F.Codd于1993年提出的。OLAP的提出引起了很⼤的反响，OLAP 作为⼀类产品同联机事务处理 (OLTP) 明显区分开来。

Codd认为联机事务处理（OLTP）已不能满⾜终端⽤户对数据库查询分析的要求，SQL对⼤数据库的简单查询也不能满⾜⽤户 分析的需求。⽤户的决策分析需要对关系数据库进⾏⼤量计算才能得到结果，⽽查询的结果并不能满⾜决策者提出的需求。因 此，Codd提出了多维数据库和多维分析的概念，即OLAP。

OLAP委员会对联机分析处理的定义为：从原始数据中转化出来的、能够真正为⽤户所理解的、并真实反映企业多维特性的数 据称为信息数据，使分析⼈员、管理⼈员或执⾏⼈员能够从多种⻆度对信息数据进⾏快速、⼀致、交互地存取，从⽽获得对数 据的更深⼊了解的⼀类软件技术。OLAP的⽬标是满⾜决策⽀持或多维环境特定的查询和报表需求，它的技术核⼼是"维"这个 概念，因此OLAP也可以说是多维数据分析⼯具的集合。

OLAP的准则和特性

E.F.Codd提出了关于OLAP的12条准则：

- 准则1 OLAP模型必须提供多维概念视图

- 准则2 透明性准则

- 准则3 存取能⼒准则

- 准则4 稳定的报表能⼒

- 准则5 客户/服务器体系结构

- 准则6 维的等同性准则

- 准则7 动态的稀疏矩阵处理准则

- 准则8 多⽤户⽀持能⼒准则

- 准则9 ⾮受限的跨维操作

- 准则10 直观的数据操纵

- 准则11 灵活的报表⽣成

- 准则12 不受限的维与聚集层次


⼀⾔以蔽之： OLTP系统强调数据库内存效率，强调内存各种指标的命令率，强调绑定变量，强调并发操作，强调事务性； OLAP系统则强调数据分析，强调SQL执⾏时⻓，强调磁盘I/O，强调分区。

## OLAP开源引擎

⽬前市⾯上主流的开源OLAP引擎包含不限于：Hive、Hawq、Presto、Kylin、Impala、Sparksql、Druid、Clickhouse、 Greeplum等，可以说⽬前没有⼀个引擎能在数据量，灵活程度和性能上做到完美，⽤户需要根据⾃⼰的需求进⾏选型。

组件特点和简介 Hive

https://hive.apache.org/

Hive是基于Hadoop的⼀个数据仓库⼯具，可以将结构化的数据⽂件映射为⼀张数据库表，并提供完整的sql查询功能，可以 将sql语句转换为MapReduce任务进⾏运⾏。其优点是学习成本低，可以通过类SQL语句快速实现简单的MapReduce统计， 不必开发专⻔的MapReduce应⽤，⼗分适合数据仓库的统计分析。 对于hive主要针对的是OLAP应⽤，其底层是hdfs分布式⽂件系统，hive⼀般只⽤于查询分析统计，⽽不能是常⻅的CUD操 作，Hive需要从已有的数据库或⽇志进⾏同步最终⼊到hdfs⽂件系统中，当前要做到增量实时同步都相当困难。 Hive的优势是完善的SQL⽀持，极低的学习成本，⾃定义数据格式，极⾼的扩展性可轻松扩展到⼏千个节点等等。 但是Hive 在加载数据的过程中不会对数据进⾏任何处理，甚⾄不会对数据进⾏扫描，因此也没有对数据中的某些 Key 建⽴索 引。Hive 要访问数据中满⾜条件的特定值时，需要暴⼒扫描整个数据库，因此访问延迟较⾼。 Hive真的太慢了。⼤数据量聚合计算或者联表查询，Hive的耗时动辄以⼩时计算，在某⼀个瞬间，我甚⾄想把它开除出 OLAP"国籍"，但是不得不承认Hive仍然是基于Hadoop体系应⽤最⼴泛的OLAP引擎。

### Hawq

http://hawq.apache.org https://blog.csdn.net/wzy0623/article/details/55047696 https://www.oschina.net/p/hawq

Hawq是⼀个Hadoop原⽣⼤规模并⾏SQL分析引擎，Hawq采⽤ MPP 架构，改进了针对 Hadoop 的基于成本的查询优化 器。除了能⾼效处理本身的内部数据，还可通过 PXF 访问 HDFS、Hive、HBase、JSON 等外部数据源。HAWQ全⾯兼容 SQL 标准，能编写 SQL UDF，还可⽤ SQL 完成简单的数据挖掘和机器学习。⽆论是功能特性，还是性能表现，HAWQ 都 ⽐较适⽤于构建 Hadoop 分析型数据仓库应⽤。 ⼀个典型的Hawq集群组件如下：

![image 1](<你需要的不是实时数仓 你需要的是一款强大的OLAP数据库(下).note_images/imageFile1.png>)

![image 2](<你需要的不是实时数仓 你需要的是一款强大的OLAP数据库(下).note_images/imageFile2.png>)

⽹络上有⼈对Hawq与Hive查询性能进⾏了对⽐测试，总体来看，使⽤Hawq内部表⽐Hive快的多（4-50倍）。 原⽂链接：

https://blog.csdn.net/wzy0623/article/details/71479539

### Spark SQL

https://spark.apache.org/sql/

SparkSQL的前身是Shark，它将 SQL 查询与 Spark 程序⽆缝集成,可以将结构化数据作为 Spark 的 RDD 进⾏查询。 SparkSQL作为Spark⽣态的⼀员继续发展，⽽不再受限于Hive，只是兼容Hive。 Spark SQL在整个Spark体系中的位置如下：

![image 3](<你需要的不是实时数仓 你需要的是一款强大的OLAP数据库(下).note_images/imageFile3.png>)

SparkSQL的架构图如下：

![image 4](<你需要的不是实时数仓 你需要的是一款强大的OLAP数据库(下).note_images/imageFile4.png>)

Spark SQL对熟悉Spark的同学来说，很容易理解并上⼿使⽤： 相⽐于Spark RDD API，Spark SQL包含了对结构化数据和在其上运算的更多信息，Spark SQL使⽤这些信息进⾏了额外的 优化，使对结构化数据的操作更加⾼效和⽅便。 SQL提供了⼀个通⽤的⽅式来访问各式各样的数据源，包括Hive, Avro, Parquet, ORC, JSON, and JDBC。 Hive兼容性极好。

### Presto

https://prestodb.github.io/

Presto is an open source distributed SQL query engine for running interactive analytic queries against data sources of all sizes ranging from gigabytes to petabytes. Presto allows querying data where it lives, including Hive, Cassandra, relational databases or even proprietary data stores. A single Presto query can combine data from multiple sources, allowing for analytics across your entire organization. Presto is targeted at analysts who expect response times ranging from sub-second to minutes. Presto breaks the false choice between having fast analytics using an expensive commercial solution or using a slow "free" solution that requires excessive hardware.

这是Presto官⽅的简介。Presto 是由 Facebook 开源的⼤数据分布式 SQL 查询引擎，适⽤于交互式分析查询，可⽀持众多 的数据源，包括 HDFS，RDBMS，KAFKA 等，⽽且提供了⾮常友好的接⼝开发数据源连接器。 Presto⽀持标准的ANSI SQL，包括复杂查询、聚合（aggregation）、连接（join）和窗⼝函数（window functions)。作 为Hive和Pig（Hive和Pig都是通过MapReduce的管道流来完成HDFS数据的查询）的替代者，Presto 本身并不存储数据， 但是可以接⼊多种数据源，并且⽀持跨数据源的级联查询。

https://blog.csdn.net/u012535605/article/details/83857079

Presto没有使⽤MapReduce，它是通过⼀个定制的查询和执⾏引擎来完成的。它的所有的查询处理是在内存中，这也是它的 性能很⾼的⼀个主要原因。Presto和Spark SQL有很⼤的相似性，这是它区别于Hive的最根本的区别。 但Presto由于是基于内存的，⽽hive是在磁盘上读写的，因此presto⽐hive快很多，但是由于是基于内存的计算当多张⼤表 关联操作时易引起内存溢出错误。

![image 5](<你需要的不是实时数仓 你需要的是一款强大的OLAP数据库(下).note_images/imageFile5.png>)

https://www.cnblogs.com/tgzhu/p/6033373.html

# Kylin

http://kylin.apache.org/cn/ https://www.infoq.cn/article/kylin-apache-in-meituan-olap-scenarios-practice/

提到Kylin就不得不说说ROLAP和MOLAP。

传统OLAP根据数据存储⽅式的不同分为ROLAP（relational olap）以及MOLAP（multi-dimension olap）

ROLAP 以关系模型的⽅式存储⽤作多为分析⽤的数据，优点在于存储体积⼩，查询⽅式灵活，然⽽缺点也显⽽易⻅，每次 查询都需要对数据进⾏聚合计算，为了改善短板，ROLAP使⽤了列存、并⾏查询、查询优化、位图索引等技术。

MOLAP 将分析⽤的数据物理上存储为多维数组的形式，形成CUBE结构。维度的属性值映射成多维数组的下标或者下标范 围，事实以多维数组的值存储在数组单元中，优势是查询快速，缺点是数据量不容易控制，可能会出现维度爆炸的问题。

⽽Kylin⾃身就是⼀个MOLAP系统，多维⽴⽅体（MOLAP Cube）的设计使得⽤户能够在Kylin⾥为百亿以上数据集定义数据 模型并构建⽴⽅体进⾏数据的预聚合。 Apache Kylin™是⼀个开源的分布式分析引擎，提供Hadoop/Spark之上的SQL查询接⼝及多维分析（OLAP）能⼒以⽀持 超⼤规模数据，最初由eBay Inc. 开发并贡献⾄开源社区。它能在亚秒内查询巨⼤的Hive表。

![image 6](<你需要的不是实时数仓 你需要的是一款强大的OLAP数据库(下).note_images/imageFile6.png>)

Kylin的优势有：

提供ANSI-SQL接⼝

交互式查询能⼒

MOLAP Cube 的概念

与BI⼯具可⽆缝整合

所以适合Kylin的场景包括：

⽤户数据存在于Hadoop HDFS中，利⽤Hive将HDFS⽂件数据以关系数据⽅式存取，数据量巨⼤，在500G以上

每天有数G甚⾄数⼗G的数据增量导⼊

有10个以内较为固定的分析维度

简单来说，Kylin中数据⽴⽅的思想就是以空间换时间，通过定义⼀系列的纬度，对每个纬度的组合进⾏预先计算并存储。有N 个纬度，就会有2的N次种组合。所以最好控制好纬度的数量，因为存储量会随着纬度的增加爆炸式的增⻓，产⽣灾难性后 果。

# Impala

https://impala.apache.org/

Impala也是⼀个SQL on Hadoop的查询⼯具，底层采⽤MPP技术，⽀持快速交互式SQL查询。与Hive共享元数据存储。 Impalad是核⼼进程，负责接收查询请求并向多个数据节点分发任务。statestored进程负责监控所有Impalad进程，并向集 群中的节点报告各个Impalad进程的状态。catalogd进程负责⼴播通知元数据的最新信息。 Impala的架构图如下：

![image 7](<你需要的不是实时数仓 你需要的是一款强大的OLAP数据库(下).note_images/imageFile7.png>)

Impala的特性包括：

⽀持Parquet、Avro、Text、RCFile、SequenceFile等多种⽂件格式

⽀持存储在HDFS、HBase、Amazon S3上的数据操作

⽀持多种压缩编码⽅式：Snappy、Gzip、Deflate、Bzip2、LZO

⽀持UDF和UDAF

⾃动以最有效的顺序进⾏表连接 允许定义查询的优先级排队策略 ⽀持多⽤户并发查询

⽀持数据缓存

提供计算统计信息（COMPUTE STATS）

提供窗⼝函数（聚合 OVER PARTITION, RANK, LEAD, LAG, NTILE等等）以⽀持⾼级分析功能

⽀持使⽤磁盘进⾏连接和聚合，当操作使⽤的内存溢出时转为磁盘操作

允许在where⼦句中使⽤⼦查询

允许增量统计——只在新数据或改变的数据上执⾏统计计算

⽀持maps、structs、arrays上的复杂嵌套查询

可以使⽤impala插⼊或更新HBase

同样，Impala经常会和Hive、Presto放在⼀起做⽐较，Impala的劣势也同样明显：

Impala不提供任何对序列化和反序列化的⽀持。

Impala只能读取⽂本⽂件，⽽不能读取⾃定义⼆进制⽂件。

每当新的记录/⽂件被添加到HDFS中的数据⽬录时，该表需要被刷新。这个缺点会导致正在执⾏的查询sql遇到刷新会挂 起，查询不动。

# Druid

https://druid.apache.org/ https://blog.csdn.net/warren288/article/details/80629909

Druid 是⼀种能对历史和实时数据提供亚秒级别的查询的数据存储。Druid ⽀持低延时的数据摄取，灵活的数据探索分析，⾼ 性能的数据聚合，简便的⽔平扩展。适⽤于数据量⼤，可扩展能⼒要求⾼的分析型查询系统。 Druid解决的问题包括：数据的快速摄⼊和数据的快速查询。 所以要理解Druid，需要将其理解为两个系统，即输⼊系统和查询系统。 Druid的架构如下：

![image 8](<你需要的不是实时数仓 你需要的是一款强大的OLAP数据库(下).note_images/imageFile8.png>)

![image 9](<你需要的不是实时数仓 你需要的是一款强大的OLAP数据库(下).note_images/imageFile9.png>)

Druid的特点包括：

Druid实时的数据消费，真正做到数据摄⼊实时、查询结果实时

Druid⽀持 PB 级数据、千亿级事件快速处理，⽀持每秒数千查询并发

Druid的核⼼是时间序列，把数据按照时间序列分批存储，⼗分适合⽤于对按时间进⾏统计分析的场景

Druid把数据列分为三类：时间戳、维度列、指标列

Druid不⽀持多表连接

Druid中的数据⼀般是使⽤其他计算框架(Spark等)预计算好的低层次统计数据

Druid不适合⽤于处理透视维度复杂多变的查询场景

Druid擅⻓的查询类型⽐较单⼀，⼀些常⽤的SQL(groupby 等)语句在druid⾥运⾏速度⼀般

Druid⽀持低延时的数据插⼊、更新，但是⽐hbase、传统数据库要慢很多

与其他的时序数据库类似，Druid在查询条件命中⼤量数据情况下可能会有性能问题，⽽且排序、聚合等能⼒普遍不太好，灵 活性和扩展性不够，⽐如缺乏Join、⼦查询等。 我个⼈对Druid的理解在于，Druid保证数据实时写⼊，但查询上对SQL⽀持的不够完善(不⽀持Join)，适合将清洗好的记录 实时录⼊，然后迅速查询包含历史的结果，在我们⽬前的业务上没有实际应⽤。 Druid的应⽤可以参考： 《Druid 在有赞的使⽤场景及应⽤实践》

https://blog.csdn.net/weixin_34273481/article/details/89238947

# Greeplum

https://greenplum.org/ https://blog.csdn.net/yongshenghuang/article/details/84925941 https://www.jianshu.com/p/b5c85cadb362

Greenplum是⼀个开源的⼤规模并⾏数据分析引擎。借助MPP架构，在⼤型数据集上执⾏复杂SQL分析的速度⽐很多解决⽅ 案都要快。

GPDB完全⽀持ANSI SQL 2008标准和SQL OLAP 2003 扩展；从应⽤编程接⼝上讲，它⽀持ODBC和JDBC。完善的标准 ⽀持使得系统开发、维护和管理都⼤为⽅便。⽀持分布式事务，⽀持ACID。保证数据的强⼀致性。做为分布式数据库，拥有 良好的线性扩展能⼒。GPDB有完善的⽣态系统，可以与很多企业级产品集成，譬如SAS，Cognos，Informatic，Tableau 等；也可以很多种开源软件集成，譬如Pentaho,Talend 等。

GreenPulm的架构如下：

![image 10](<你需要的不是实时数仓 你需要的是一款强大的OLAP数据库(下).note_images/imageFile10.png>)

GreenPulm的技术特点如下：

⽀持海量数据存储和处理

⽀持Just In Time BI：通过准实时、实时的数据加载⽅式，实现数据仓库的实时更新，进⽽实现动态数据仓库 （ADW），基于动态数据仓库，业务⽤户能对当前业务数据进⾏BI实时分析（Just In Time BI）

⽀持主流的sql语法，使⽤起来⼗分⽅便，学习成本低

扩展性好，⽀持多语⾔的⾃定义函数和⾃定义类型等

提供了⼤量的维护⼯具，使⽤维护起来很⽅便

⽀持线性扩展：采⽤MPP并⾏处理架构。在MPP结构中增加节点就可以线性提供系统的存储容量和处理能⼒

较好的并发⽀持及⾼可⽤性⽀持除了提供硬件级的Raid技术外，还提供数据库层Mirror机制保护，提供Master/Stand by 机制进⾏主节点容错，当主节点发⽣错误时，可以切换到Stand by节点继续服务

⽀持MapReduce

数据库内部压缩

⼀个重要的信息：Greenplum基于Postgresql，也就是说GreenPulm和TiDB的定位类似，想要在OLTP和OLAP上进⾏统 ⼀。

# ClickHouse

https://clickhouse.yandex/ https://clickhouse.yandex/docs/zh/development/architecture/ http://www.clickhouse.com.cn/ https://www.jianshu.com/p/a5bf490247ea

官⽹对ClickHouse的介绍：

ClickHouse is an open source column-oriented database management system capable of real time generation of analytical data reports using SQL queries.

Clickhouse由俄罗斯yandex公司开发。专为在线数据分析⽽设计。Yandex是俄罗斯搜索引擎公司。官⽅提供的⽂档表名， ClickHouse ⽇处理记录数"⼗亿级"。 特性:采⽤列式存储；数据压缩；⽀持分⽚，并且同⼀个计算任务会在不同分⽚上并⾏执⾏，计算完成后会将结果汇总；⽀持 SQL；⽀持联表查询；⽀持实时更新；⾃动多副本同步；⽀持索引；分布式存储查询。 ⼤家都Nginx不陌⽣吧，战⽃⺠族开源的软件普遍的特点包括：轻量级，快。 ClickHouse最⼤的特点就是快，快，快，重要的话说三遍！ 与Hadoop、Spark这些巨⽆霸组件相⽐，ClickHouse很轻量级，其特点：

列式存储数据库，数据压缩

关系型、⽀持SQL

分布式并⾏计算，把单机性能压榨到极限

⾼可⽤

数据量级在PB级别

实时数据更新

索引

使⽤ClickHouse也有其本身的限制，包括：

缺少⾼频率，低延迟的修改或删除已存在数据的能⼒。仅能⽤于批量删除或修改数据。

没有完整的事务⽀持

不⽀持⼆级索引

有限的SQL⽀持，join实现与众不同

不⽀持窗⼝功能

元数据管理需要⼈⼯⼲预维护

## 总结

上⾯给出了常⽤的⼀些OLAP引擎，它们各⾃有各⾃的特点，我们将其分组：

Hive，Hawq，Impala - 基于SQL on Hadoop

Presto和Spark SQL类似 - 基于内存解析SQL⽣成执⾏计划

Kylin - ⽤空间换时间，预计算

Druid - ⼀个⽀持数据的实时摄⼊

ClickHouse - OLAP领域的Hbase，单表查询性能优势巨⼤

Greenpulm - OLAP领域的Postgresql

如果你的场景是基于HDFS的离线计算任务，那么Hive，Hawq和Imapla就是你的调研⽬标； 如果你的场景解决分布式查询问题，有⼀定的实时性要求，那么Presto和SparkSQL可能更符合你的期望； 如果你的汇总维度⽐较固定，实时性要求较⾼，可以通过⽤户配置的维度+指标进⾏预计算，那么不妨尝试Kylin和Druid； ClickHouse则在单表查询性能上独领⻛骚，远超过其他的OLAP数据库； Greenpulm作为关系型数据库产品，性能可以随着集群的扩展线性增⻓，更加适合进⾏数据分析。 就像美团在调研Kylin的报告中所说的： ⽬前还没有⼀个OLAP系统能够满⾜各种场景的查询需求。 其本质原因是，没有⼀个系统能同时在数据量、性能、和灵活性三个⽅⾯做到完美，每个系统在设计时都需要在这三者间做出 取舍。

