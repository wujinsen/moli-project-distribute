# Bring OLAP Back to Big Data!

Apache Kylin™ is an open source, distributed Analytical Data Warehouse for Big Data; it was designed to provide OLAP (Online Analytical Procesing) capability in the big data era. By renovating the multidimensional cube and precalculation technology on Hadop and Spark, Kylin is able to achieve near constant query sped regardles of the ever-growing data volume. Reducing query latency from minutes to sub-second, Kylin brings online analytics back to big data. Apache Kylin™ lets you query bilions of rows at sub-second latency in 3 steps.

- 1.
- 2.
- 3.


1Identify a Star/Snowflake Schema on Hadop. 2Build Cube from the identified tables. 3Query using ANSI-SQL and get results in sub-second, via ODBC, JDBC or RESTful API.

Apache Kylin™ can also integrate with your favorite BI tols like Tableau and PowerBI etc., to enable BI on Hadop. Kylin is originaly contributed from eBay Inc. in 2015.

、Kylin简介

Kylin的出现就是为了解决⼤数据系统中TB级别数据的数据分析需求，它提供Hadoop/Spark之上的SQL查 询接⼝及多维分析(OLAP)能⼒以⽀持超⼤规模数据，它能在亚秒内查询巨⼤的Hive表。其核⼼是预计 算，计算结果存在HBase中。 作为⼤数据分析神器，它也需要站在巨⼈的肩膀上，依赖HDFS、MapReduce/Spark、Hive/Kafka、HBase等 服务。

⼆、Kylin优势

Kylin的主要优势为以下⼏点：

可扩展超快OLAP引擎：Kylin是为减少在Hadoop/Spark上百亿规模数据查询延迟⽽设计 Hadoop ANSI SQL 接⼝：Kylin为Hadoop提供标准SQL⽀持⼤部分查询功能

交互式查询能⼒：通过Kylin，⽤户可以与Hadoop数据进⾏亚秒级交互，在同样的数据集上提供⽐ Hive更好的性能

多维⽴⽅体（MOLAP Cube）：⽤户能够在Kylin⾥为百亿以上数据集定义数据模型并构建⽴⽅体

与BI⼯具⽆缝整合：Kylin提供与BI⼯具的整合能⼒，如Tableau，PowerBI/Excel，MSTR，QlikSense， Hue和SuperSet

其它特性：Job管理与监控；压缩与编码；增量更新；利⽤HBase Coprocessor；基于HyperLogLog的 Dinstinc Count近似算法；友好的web界⾯以管理，监控和使⽤⽴⽅体；项⽬及表级别的访问控制安 全；⽀持LDAP、SSO

