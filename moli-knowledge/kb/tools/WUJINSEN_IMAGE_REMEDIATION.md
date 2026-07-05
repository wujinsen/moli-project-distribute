# wujinsen 图片回迁 manifest（T22 · R1 审计）

> 由 `audit_wujinsen_images.py` 生成。策略说明见 `docs/product/wujinsen-wiki-image-remediation-prd.md`。

## 汇总

| 指标 | 值 |
|------|-----|
| raw 含图 md | 397 |
| 已被 wiki cite 的含图 raw | 252 |
| 优先回迁（非 skip-deleted） | 252 |
| png 文件合计（抽样统计） | 2768 |

## 策略档

| 档 | 含义 |
|----|------|
| **A** | 新建 annex 页 + `.assets` |
| **B** | 枢纽页追加 `## 原文插图` |
| **C-or-A** | 单 cite：annex 或谨慎全文替换 |
| **D** | 过渡：仅 `/kb/raw/asset` 直链 |
| **defer** | 未 cite，暂不回迁 |
| **skip-deleted** | #1331 已删 raw，不可恢复 |

## 优先集（已 cite · 待回迁）

| raw | 图(ref/png) | cite 数 | wiki slugs | 建议 |
|-----|-------------|---------|------------|------|
| `raw/wujinsen_markdown/BigData/Flink/基础/Flink从入门到入土（详细教程）.note.md` | 148/74 | 2 | bigdata/flink-流批一体入门, bigdata/flink-面试题 | **A** |
| `raw/wujinsen_markdown/BigData/Hadoop/安装部署/mac搭建hadoop集群.note.md` | 98/49 | 1 | bigdata/hadoop-生态入门 | **A** |
| `raw/wujinsen_markdown/架构/容器/k8s/K8s运维锦囊，19个常见故障解决方法.note.md` | 98/49 | 1 | ops/k8s入门与容器编排 | **A** |
| `raw/wujinsen_markdown/BigData/Hadoop/HDFS/HDFS体系结构(NameNode、DataNode详解).note.md` | 43/43 | 1 | bigdata/hadoop-生态入门 | **A** |
| `raw/wujinsen_markdown/架构/安全框架/开源项目/OAuth2实现单点登录SSO.note.md` | 78/39 | 1 | security/shiro-鉴权体系 | **A** |
| `raw/wujinsen_markdown/jvm/小白都能看得懂的java虚拟机内存模型.note.md` | 68/34 | 5 | java/jvm-gc调优实战, java/jvm-oom与排查入门, java/jvm-内存与gc (+2) | **A** |
| `raw/wujinsen_markdown/BigData/Spark/算子/Spark常用算子详解.note.md` | 62/31 | 1 | bigdata/spark-核心概念与实践 | **A** |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/数仓建设/网易传媒数据指标体系建设实践.note.md` | 54/27 | 1 | bigdata/数仓分层与建模 | **A** |
| `raw/wujinsen_markdown/BigData/OLAP/ClickHouse/老司机教你如何调教Presto和ClickHouse，应对业务难题！.note.md` | 48/24 | 1 | bigdata/olap-与-实时数仓 | **A** |
| `raw/wujinsen_markdown/DataBase/mysql/索引/mysql-覆盖索引.note.md` | 46/23 | 3 | database/mysql-索引, database/mysql-索引面试题, database/mysql-覆盖索引与回表优化 | **A** |
| `raw/wujinsen_markdown/大数据资料-王/spark/spark基础.note.md` | 46/23 | 1 | bigdata/spark-核心概念与实践 | **A** |
| `raw/wujinsen_markdown/前端/前端解决跨域问题的8种方案.note.md` | 44/22 | 3 | frontend/前端基础面试题, frontend/前端技术栈, middleware/跨域与前后端分离 | **A** |
| `raw/wujinsen_markdown/架构/分布式事务/redis/Redisson基本用法.note.md` | 44/22 | 2 | cache/redisson-看门狗与分布式锁, middleware/分布式事务 | **A** |
| `raw/wujinsen_markdown/BigData/Kafka/Kafka深度解析(1).note.md` | 21/21 | 1 | bigdata/kafka-大数据管道 | **A** |
| `raw/wujinsen_markdown/DataBase/mysql/正确的理解MySQL的MVCC及实现原理.note.md` | 42/21 | 3 | database/mysql-事务面试题, database/mysql-索引面试题, database/mysql-隔离级别与mvcc | **A** |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/捐助：hadoop大全（增加yarn、flume_storm、hadoop一套视频））.note.md` | 40/20 | 1 | bigdata/hadoop-生态入门 | **A** |
| `raw/wujinsen_markdown/BigData/Hive/Hive安装与配置详解.note.md` | 38/19 | 1 | bigdata/hive-数仓与-sql | **A** |
| `raw/wujinsen_markdown/大数据资料-王/a安装文档/hadoop2安装手顺.note.md` | 38/19 | 1 | bigdata/hadoop-生态入门 | **A** |
| `raw/wujinsen_markdown/大数据资料-王/linux/堡垒机.note.md` | 38/19 | 1 | ops/linux-运维基础 | **A** |
| `raw/wujinsen_markdown/大数据资料-王/redis/redis源码分析.note.md` | 38/19 | 3 | cache/redis-数据结构与使用场景, cache/redis-集群与哨兵实践, cache/redis-面试题 | **A** |
| `raw/wujinsen_markdown/BigData/Kafka/Kafka部署与代码实例.note.md` | 36/18 | 1 | bigdata/kafka-大数据管道 | **A** |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/数仓基础/关于OLAP数仓，这大概是史上最全面的总结！（万字干货）.note.md` | 36/18 | 1 | bigdata/数仓分层与建模 | **A** |
| `raw/wujinsen_markdown/前端/Bootstrap/JS组件Bootstrap实现弹出框和提示框效果代码.note.md` | 36/18 | 3 | frontend/前端基础面试题, frontend/前端技术栈, middleware/跨域与前后端分离 | **A** |
| `raw/wujinsen_markdown/并发编程/java/volatile.note.md` | 36/18 | 2 | java/bio-nio-aio对比, java/java-并发面试题 | **A** |
| `raw/wujinsen_markdown/DataBase/Redis/Redis夺命16问.note.md` | 34/17 | 1 | cache/redis-面试题 | **A** |
| `raw/wujinsen_markdown/数据结构与算法/B树.note.md` | 34/17 | 1 | patterns/算法面试题精选 | **A** |
| `raw/wujinsen_markdown/BigData/spark(1)/GC调优在Spark应用中的实践(1).note.md` | 16/16 | 2 | bigdata/spark-核心概念与实践, bigdata/spark-面试题 | **A** |
| `raw/wujinsen_markdown/BigData/spark(1)/GC调优在Spark应用中的实践.note.md` | 32/16 | 2 | bigdata/spark-核心概念与实践, bigdata/spark-面试题 | **A** |
| `raw/wujinsen_markdown/DataBase/mysql/事务隔离级别中的可重复读能防幻读吗.note.md` | 30/15 | 3 | database/mysql-事务面试题, database/mysql-索引面试题, database/mysql-隔离级别与mvcc | **A** |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/聊聊高并发系统/聊聊高并发系统之HTTP缓存.note.md` | 30/15 | 5 | middleware/dubbo-调用原理与分层, middleware/feign-开发踩坑, middleware/sentinel-限流与熔断 (+2) | **A** |
| `raw/wujinsen_markdown/DataBase/mysql/MySQL索引背后的数据结构及算法原理.note.md` | 28/14 | 3 | database/b-plus树与-innodb索引结构, database/mysql-索引, database/mysql-索引面试题 | **A** |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/Hystrix使用入门手册（中文）.note.md` | 28/14 | 5 | middleware/dubbo-调用原理与分层, middleware/feign-开发踩坑, middleware/sentinel-限流与熔断 (+2) | **A** |
| `raw/wujinsen_markdown/架构/消息队列/RabbitMQ/RabbitMQ安装教程.note.md` | 28/14 | 1 | middleware/rabbitmq-入门与使用场景 | **A** |
| `raw/wujinsen_markdown/DataBase/mysql/mysql规范.note.md` | 26/13 | 1 | database/mysql-索引面试题 | **A** |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/MapReduce源码分析总结(转).note.md` | 13/13 | 1 | bigdata/hadoop-生态入门 | **A** |
| `raw/wujinsen_markdown/BigData/ElasticSearch/安装/Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note.md` | 12/12 | 3 | search/elasticsearch-搜索, search/elasticsearch-面试题, search/es-match与bool查询 | **A** |
| `raw/wujinsen_markdown/BigData/Hive/安装部署/hive2.3.2安装使用.note.md` | 24/12 | 1 | bigdata/hive-数仓与-sql | **A** |
| `raw/wujinsen_markdown/BigData/Storm/Storm安装启动.note.md` | 24/12 | 1 | bigdata/flink-流批一体入门 | **A** |
| `raw/wujinsen_markdown/架构/DevOps/nexus/nexus私服搭建.note.md` | 24/12 | 1 | ops/jenkins-ci入门 | **A** |
| `raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Stage划分及提交源码分析.note.md` | 22/11 | 2 | bigdata/spark-核心概念与实践, bigdata/spark-面试题 | **A** |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/hadoop入门之设置datanode的心跳时间的方法.note.md` | 22/11 | 1 | bigdata/hadoop-生态入门 | **A** |
| `raw/wujinsen_markdown/大数据资料-王/linux/“网络中的跳板是什么意思.note.md` | 22/11 | 1 | ops/linux-运维基础 | **A** |
| `raw/wujinsen_markdown/架构/分布式事务/redis/Redis分布式锁的正确实现方式.note.md` | 22/11 | 1 | middleware/分布式事务 | **A** |
| `raw/wujinsen_markdown/BigData/Spark/Spark性能优化指南——高级篇.note.md` | 20/10 | 1 | bigdata/spark-核心概念与实践 | **A** |
| `raw/wujinsen_markdown/Spring/SpringMVC/SpringMVC接收复杂集合参数.note.md` | 20/10 | 11 | patterns/spring框架中的设计模式, spring/enableautoconfiguration原理, spring/spring-application启动流程 (+8) | **A** |
| `raw/wujinsen_markdown/大数据资料-王/flume/让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2.note.md` | 10/10 | 1 | bigdata/flume-与-数据采集 | **A** |
| `raw/wujinsen_markdown/面试笔试/ElasticSearch/面试小结之JVM篇.note.md` | 20/10 | 1 | search/elasticsearch-面试题 | **A** |
| `raw/wujinsen_markdown/面试笔试/树/B+树介绍.note.md` | 20/10 | 1 | database/b-plus树与-innodb索引结构 | **A** |
| `raw/wujinsen_markdown/jvm/JDK的命令行工具.note.md` | 18/9 | 5 | java/jvm-gc调优实战, java/jvm-oom与排查入门, java/jvm-内存与gc (+2) | **A** |
| `raw/wujinsen_markdown/jvm/调优/九大工具助你玩转Java性能优化.note.md` | 18/9 | 6 | java/java-cpu-100排查实战, java/jvm-gc调优实战, java/jvm-oom与排查入门 (+3) | **A** |
| `raw/wujinsen_markdown/大数据资料-王/hive/hive.note.md` | 18/9 | 1 | bigdata/hive-数仓与-sql | **A** |
| `raw/wujinsen_markdown/大数据资料-王/jvm/JDK的命令行工具.note.md` | 18/9 | 3 | java/jvm-gc调优实战, java/jvm-内存与gc, java/jvm-面试题 | **A** |
| `raw/wujinsen_markdown/大数据资料-王/netty/Netty介绍.note.md` | 18/9 | 2 | middleware/netty-pipeline与编解码, middleware/netty-reactor与线程模型 | **A** |
| `raw/wujinsen_markdown/架构/容器/Docker/某小公司项目环境部署演变之路.note.md` | 18/9 | 1 | ops/容器与-docker | **A** |
| `raw/wujinsen_markdown/BigData/Hadoop/MAPREDUCE详解.note.md` | 16/8 | 1 | bigdata/hadoop-生态入门 | **A** |
| `raw/wujinsen_markdown/BigData/Kafka/Kafka实战－KafkaOffsetMonitor.note.md` | 16/8 | 1 | bigdata/kafka-大数据管道 | **A** |
| `raw/wujinsen_markdown/BigData/spark(1)/Parquet格式详解.note.md` | 16/8 | 1 | bigdata/spark-核心概念与实践 | **A** |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/数据仓库/数仓（十四）从0到1简单搭建加载数仓DWD层（业务数据解析）.note.md` | 16/8 | 1 | bigdata/数仓分层与建模 | **A** |
| `raw/wujinsen_markdown/jvm/垃圾回收器.note.md` | 16/8 | 5 | java/jvm-gc调优实战, java/jvm-oom与排查入门, java/jvm-内存与gc (+2) | **A** |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/分布式存储技术及应用（1）.note.md` | 16/8 | 1 | bigdata/hadoop-生态入门 | **A** |
| `raw/wujinsen_markdown/大数据资料-王/jvm/垃圾回收器.note.md` | 16/8 | 3 | java/jvm-gc调优实战, java/jvm-内存与gc, java/jvm-面试题 | **A** |
| `raw/wujinsen_markdown/架构/分库分表/分库分表的几种常见形式以及可能遇到的难.note.md` | 16/8 | 1 | database/sharding-分库分表入门 | **A** |
| `raw/wujinsen_markdown/源码分析/OpenJDK/openJDK之如何下载各个版本的openJDK源码.note.md` | 16/8 | 1 | java/jvm-内存与gc | **A** |
| `raw/wujinsen_markdown/面试笔试/树/B树和B+树的总结.note.md` | 16/8 | 1 | database/b-plus树与-innodb索引结构 | **A** |
| `raw/wujinsen_markdown/BigData/Hudi/方案/百信银行基于ApacheHudi实时数据湖演进方案.note.md` | 14/7 | 1 | bigdata/olap-与-实时数仓 | **A** |
| `raw/wujinsen_markdown/BigData/Spark/Spark计算过程分析.note.md` | 14/7 | 1 | bigdata/spark-核心概念与实践 | **A** |
| `raw/wujinsen_markdown/BigData/Storm/Storm的基本概念.note.md` | 14/7 | 1 | bigdata/flink-流批一体入门 | **A** |
| `raw/wujinsen_markdown/BigData/spark(1)/论SparkStreaming的数据可靠性和一致性.note.md` | 14/7 | 1 | bigdata/spark-核心概念与实践 | **A** |
| `raw/wujinsen_markdown/DataBase/Redis/redis线程模型.note.md` | 14/7 | 1 | cache/redis-面试题 | **A** |
| `raw/wujinsen_markdown/Spring/SpringMVC/ModelMap、ModelAndView和@Modelattribute的区别.note.md` | 14/7 | 11 | patterns/spring框架中的设计模式, spring/enableautoconfiguration原理, spring/spring-application启动流程 (+8) | **A** |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/申请域名.note.md` | 14/7 | 1 | bigdata/hadoop-生态入门 | **A** |
| `raw/wujinsen_markdown/并发编程/Netty/Netty高性能之Reactor线程模型.note.md` | 14/7 | 4 | java/bio-nio-aio对比, middleware/io模型与-netty, middleware/netty-pipeline与编解码 (+1) | **A** |
| `raw/wujinsen_markdown/面试笔试/ElasticSearch/面试小结之Elasticsearch篇.note.md` | 14/7 | 1 | search/elasticsearch-面试题 | **A** |
| `raw/wujinsen_markdown/面试笔试/面试小结/面试小结之Elasticsearch篇.note.md` | 14/7 | 4 | search/elasticsearch-搜索, search/elasticsearch-面试题, search/es-搜索与分片路由 (+1) | **A** |
| `raw/wujinsen_markdown/面试笔试/面试小结/面试小结之并发篇.note.md` | 14/7 | 6 | java/completablefuture-异步编排, java/concurrenthashmap原理, java/java-并发 (+3) | **A** |
| `raw/wujinsen_markdown/BigData/Kafka/Kafka入门经典教程.note.md` | 12/6 | 1 | bigdata/kafka-大数据管道 | **A** |
| `raw/wujinsen_markdown/BigData/spark(1)/Spark性能调优.note.md` | 12/6 | 2 | bigdata/spark-核心概念与实践, bigdata/spark-面试题 | **A** |
| `raw/wujinsen_markdown/DataBase/mysql/采坑/MySql的时区（serverTimezone）引发的血案.note.md` | 12/6 | 1 | database/mysql-索引面试题 | **A** |
| `raw/wujinsen_markdown/Linux/VM利用host-only上网.note.md` | 12/6 | 1 | ops/linux-运维基础 | **A** |
| `raw/wujinsen_markdown/Spring/Spring、SpringMVC和SpringBoot看这一篇就够了！.note.md` | 12/6 | 11 | patterns/spring框架中的设计模式, spring/enableautoconfiguration原理, spring/spring-application启动流程 (+8) | **A** |
| `raw/wujinsen_markdown/大数据资料-王/hbase/hbase源码系列（十二）Get、Scan在服务端是如何处理.note.md` | 12/6 | 1 | bigdata/hbase-列式存储入门 | **A** |
| `raw/wujinsen_markdown/大数据资料-王/x线程/Java并发编程：Lock.note.md` | 12/6 | 1 | java/java-并发面试题 | **A** |
| `raw/wujinsen_markdown/面试笔试/Java面试题精选/【49期】面试官：SpringMVC的控制器是单例的吗.note.md` | 12/6 | 1 | java/java-并发面试题 | **A** |
| `raw/wujinsen_markdown/面试笔试/高级java/Java高级程序员面试大纲——备战金三银四跳槽季.note.md` | 12/6 | 1 | java/hashmap-面试题 | **A** |
| `raw/wujinsen_markdown/BigData/Zookeeper/Zookeeper介绍.note.md` | 10/5 | 2 | middleware/zookeeper-与协调服务, middleware/zookeeper-面试题 | **A** |
| `raw/wujinsen_markdown/BigData/spark(1)/SparkStreaming编程指南.note.md` | 10/5 | 1 | bigdata/spark-核心概念与实践 | **A** |
| `raw/wujinsen_markdown/DataBase/mysql/MySQL死锁案例，我一口气说了6个.note.md` | 10/5 | 5 | database/mysql-innodb锁机制, database/mysql-事务与锁, database/mysql-事务面试题 (+2) | **A** |
| `raw/wujinsen_markdown/DataBase/mysql/数据库事务的四大特性以及事务的隔离级别.note.md` | 10/5 | 5 | database/mysql-事务与锁, database/mysql-事务面试题, database/mysql-索引面试题 (+2) | **A** |
| `raw/wujinsen_markdown/DataBase/mysql/索引/梳理了一遍MySQL索引，发现也不过如此.note.md` | 10/5 | 4 | database/b-plus树与-innodb索引结构, database/mysql-索引, database/mysql-索引面试题 (+1) | **A** |
| `raw/wujinsen_markdown/Linux/Linux常用软件安装.note.md` | 10/5 | 1 | ops/linux-运维基础 | **A** |
| `raw/wujinsen_markdown/javaweb/为什么我再也不使用MVC框架了？.note.md` | 10/5 | 2 | database/mybatis-与-druid持久层, security/认证与会话机制 | **A** |
| `raw/wujinsen_markdown/jvm/个人笔记/JVM虚拟机笔记.note.md` | 10/5 | 5 | java/jvm-gc调优实战, java/jvm-oom与排查入门, java/jvm-内存与gc (+2) | **A** |
| `raw/wujinsen_markdown/并发编程/java/深入理解并发之CompareAndSet(CAS).note.md` | 5/5 | 2 | java/bio-nio-aio对比, java/java-并发面试题 | **B** |
| `raw/wujinsen_markdown/数据结构与算法/怎么遍历二叉树.note.md` | 10/5 | 1 | patterns/算法面试题精选 | **A** |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/it隐/使用hystrix保护你的应用.note.md` | 10/5 | 5 | middleware/dubbo-调用原理与分层, middleware/feign-开发踩坑, middleware/sentinel-限流与熔断 (+2) | **A** |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/官网文档/Hystrix.note.md` | 10/5 | 5 | middleware/dubbo-调用原理与分层, middleware/feign-开发踩坑, middleware/sentinel-限流与熔断 (+2) | **A** |
| `raw/wujinsen_markdown/架构/消息队列/RabbitMQ/RabbitMQ系列(一)：Windows下RabbitMQ安装及入门.note.md` | 5/5 | 1 | middleware/rabbitmq-入门与使用场景 | **C-or-A** |
| `raw/wujinsen_markdown/面试笔试/kafka/kafka丢消息处理.note.md` | 10/5 | 1 | middleware/kafka-与-mq选型 | **A** |
| `raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/ClickHouse主键索引最佳实践.note.md` | 8/4 | 1 | bigdata/olap-与-实时数仓 | **A** |
| `raw/wujinsen_markdown/BigData/spark(1)/Spark常见问题总结3.note.md` | 8/4 | 1 | bigdata/spark-核心概念与实践 | **A** |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/数仓案例/数据仓库维度建模之事实表设计.note.md` | 8/4 | 1 | bigdata/数仓分层与建模 | **A** |
| `raw/wujinsen_markdown/DataBase/Redis/监控/Redis监控方案.note.md` | 8/4 | 1 | cache/redis-面试题 | **A** |
| `raw/wujinsen_markdown/DataBase/Redis/缓存策略/缓存更新的套路.note.md` | 8/4 | 1 | cache/redis-面试题 | **A** |
| `raw/wujinsen_markdown/DataBase/mysql/索引/MySQL索引索引不生效的情况.note.md` | 8/4 | 3 | database/mysql-索引, database/mysql-索引失效场景, database/mysql-索引面试题 | **A** |
| `raw/wujinsen_markdown/javaweb/jwt/Cookie,Session和Token机制和区别..note.md` | 8/4 | 2 | security/api-接口安全设计, security/认证与会话机制 | **A** |
| `raw/wujinsen_markdown/javaweb/jwt/Cookie和Token.note.md` | 8/4 | 2 | security/api-接口安全设计, security/认证与会话机制 | **A** |
| `raw/wujinsen_markdown/前端/JQuery/JQuery教程.note.md` | 8/4 | 3 | frontend/前端基础面试题, frontend/前端技术栈, middleware/跨域与前后端分离 | **A** |
| `raw/wujinsen_markdown/大数据资料-王/flume/Flume的体系结构介绍以及Flume入门案例(往HDFS上传数据).note.md` | 4/4 | 1 | bigdata/flume-与-数据采集 | **C-or-A** |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/HDFS源码学习（10）——NameNode与DataNode间的通信.note.md` | 8/4 | 1 | bigdata/hadoop-生态入门 | **A** |
| `raw/wujinsen_markdown/大数据资料-王/spark/spark-sql.note.md` | 8/4 | 1 | bigdata/spark-核心概念与实践 | **A** |
| `raw/wujinsen_markdown/大数据资料-王/storm/Storm入门教程：一致性事务.note.md` | 8/4 | 1 | bigdata/flink-流批一体入门 | **A** |
| `raw/wujinsen_markdown/大数据资料-王/x线程/Java并发编程：synchronized.note.md` | 8/4 | 1 | java/java-并发面试题 | **A** |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/聊聊高并发系统/聊聊高并发系统之队列术.note.md` | 8/4 | 5 | middleware/dubbo-调用原理与分层, middleware/feign-开发踩坑, middleware/sentinel-限流与熔断 (+2) | **A** |
| `raw/wujinsen_markdown/面试笔试/Dubbo/Dubbo剖析-线程模型.note.md` | 8/4 | 1 | middleware/dubbo-调用原理与分层 | **A** |
| `raw/wujinsen_markdown/面试笔试/分布式/分布式锁的实现.note.md` | 8/4 | 1 | cache/分布式锁面试题 | **A** |
| `raw/wujinsen_markdown/面试笔试/面试小结/面试小结之综合篇.note.md` | 8/4 | 2 | database/mysql-索引面试题, java/java-并发面试题 | **A** |
| `raw/wujinsen_markdown/面试笔试/高级java/缓存更新的套路.note.md` | 8/4 | 7 | cache/cache-aside与缓存更新模式, cache/redis-实现延迟队列, cache/redis-数据结构与使用场景 (+4) | **A** |
| `raw/wujinsen_markdown/BigData/ElasticSearch/ElasticSearch同步MySql.note.md` | 6/3 | 3 | search/elasticsearch-搜索, search/elasticsearch-面试题, search/es-match与bool查询 | **B** |
| `raw/wujinsen_markdown/BigData/Hadoop/Amabri/Hive/hive入门学习线路指导.note.md` | 6/3 | 1 | bigdata/hadoop-生态入门 | **C-or-A** |
| `raw/wujinsen_markdown/BigData/Kafka/源码分析/Kafka源码环境搭建.note.md` | 6/3 | 1 | bigdata/kafka-大数据管道 | **C-or-A** |

> 仅展示 png 最多的前 120 行；全量 252 行见 `--json`。

## 全量

<details><summary>展开全量 397 行</summary>

| raw | ref/png | cited | strategy | status |
|-----|---------|-------|----------|--------|
| `raw/wujinsen_markdown/BigData/Apache Griffin/2016-12-21.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Cloudera/Apache、CDH和Cloudera三者有什么区别？.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/Cloudera/cloudera安装部署/Cloudera Manager、CDH零基础入门、线路指导.note.md` | 44/22 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/ElasticSearch/Elasticsearch之elasticsearch5.x 新特性.note.md` | 46/23 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/ElasticSearch/ElasticSearch同步MySql.note.md` | 6/3 | 3 | B | pending |
| `raw/wujinsen_markdown/BigData/ElasticSearch/Elasticsearch和mysql数据增量同步.note.md` | 2/1 | 3 | B | pending |
| `raw/wujinsen_markdown/BigData/ElasticSearch/ES源码解析与优化实战/elasticsearch 选主流程.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/ElasticSearch/同步mysql数据到ElasticSearch的最佳实践.note.md` | 4/2 | 3 | B | pending |
| `raw/wujinsen_markdown/BigData/ElasticSearch/安装/Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note.md` | 12/12 | 3 | A | pending |
| `raw/wujinsen_markdown/BigData/ElasticSearch/安装/Windows环境搭建ElasticSearch 5.6并配置head.note.md` | 24/12 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/FileBeat/filebeat-kafka日志收集.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/Flink/aboutyun/深入解析 Flink 细粒度资源管理.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Flink/Flink 原理与实现：Window 机制.note.md` | 14/7 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Flink/flinkcdc/flink cdc mysql到clickhouse.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Flink/基础/flink-table-planner-blink.note.md` | 2/1 | 2 | B | pending |
| `raw/wujinsen_markdown/BigData/Flink/基础/Flink从入门到入土（详细教程）.note.md` | 148/74 | 2 | A | pending |
| `raw/wujinsen_markdown/BigData/Hadoop/Amabri/Hive/hive入门学习线路指导.note.md` | 6/3 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/Hadoop/HDFS/HDFS体系结构(NameNode、DataNode详解).note.md` | 43/43 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Hadoop/MAPREDUCE详解.note.md` | 16/8 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Hadoop/了解apache Hadoop--Hadoop最全生态系统介绍.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Hadoop/安装部署/mac搭建hadoop集群.note.md` | 98/49 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Hadoop/本地YUM源制作.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/Hadoop/用户画像.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/Hadoop/解读Secondary NameNode的功能.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Hive/Hive命令/hive常用命令.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/Hive/Hive安装与配置详解.note.md` | 38/19 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Hive/MongoDBToHive/Hortonwork Ambari配置Hive集成Hbase的java开发maven配置.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Hive/MongoDBToHive/采坑记录.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/Hive/安装部署/hive2.3.2安装使用.note.md` | 24/12 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Hudi/方案/百信银行基于ApacheHudi实时数据湖演进方案.note.md` | 14/7 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Kafka/apache kafka系列之在zookeeper中存储结构.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Kafka/Kafka Consumer开发的一些关键点.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Kafka/Kafka入门经典教程.note.md` | 12/6 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Kafka/Kafka实战－KafkaOffsetMonitor.note.md` | 16/8 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Kafka/Kafka深度解析(1).note.md` | 21/21 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Kafka/Kafka部署与代码实例.note.md` | 36/18 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Kafka/为什么要在Kubernetes上运行Kafka，有哪些问题？.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/Kafka/安装部署/kafka集群资源评估.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/Kafka/教程/Kafka rebalance机制.note.md` | 10/5 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Kafka/源码分析/Kafka源码环境搭建.note.md` | 6/3 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/MongoDB/MongoDB数据同步工具之 MongoShake.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/OLAP/ClickHouse/从 ClickHouse 到 ByteHouse：实时数据分析场景下的优化实践.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/ClickHouse主键索引最佳实践.note.md` | 8/4 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/clickhouse精确去重.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/优化查询性能-深入理解ClickHouse跳数索引.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/OLAP/ClickHouse/老司机教你如何调教Presto和ClickHouse，应对业务难题！.note.md` | 48/24 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Spark/Preview of Apache Spark 2.0 now on Databricks Community Edition Easier, Faster,.note.md` | 6/3 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Spark/spark yarn 模式安装.note.md` | 26/13 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Spark/Spark性能优化指南——基础篇.note.md` | 6/3 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/Spark/Spark性能优化指南——高级篇.note.md` | 20/10 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Spark/Spark计算过程分析.note.md` | 14/7 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Spark/【Kafka二】Kafka工作原理详解.note.md` | 2/1 | 2 | B | pending |
| `raw/wujinsen_markdown/BigData/Spark/各模式下运行spark自带实例SparkPi.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/Spark/异常问题/spark on yarn提交任务时报ClosedChannelException解决方案.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Spark/消息队列设计精要.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/Spark/算子/Spark常用算子详解.note.md` | 62/31 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/GC调优在Spark应用中的实践(1).note.md` | 16/16 | 2 | A | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/GC调优在Spark应用中的实践.note.md` | 32/16 | 2 | A | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/Parquet格式详解.note.md` | 16/8 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/Sharethrough使用Spark Streaming优化实时竞价.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/Spark on yarn中的内存溢出案例.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/Spark1.0.0 的一些小经验.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/SparkStreaming编程指南.note.md` | 10/5 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/Spark常见问题总结2.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/Spark常见问题总结3.note.md` | 8/4 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/Spark性能调优.note.md` | 12/6 | 2 | A | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/spark性能调优总结1.note.md` | 4/2 | 2 | B | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Shuffle Map Task运算结果的处理.note.md` | 22/11 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Shuffle Read的整体流程.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Sort Based Shuffle实现解析.note.md` | 6/3 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Stage划分及提交源码分析.note.md` | 22/11 | 2 | A | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Storage 模块整体架构.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/使用Spark MLlib给豆瓣用户推荐电影.note.md` | 14/7 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/开发自己的Shuffle Service？.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/理解Spark的核心RDD.note.md` | 6/3 | 2 | B | pending |
| `raw/wujinsen_markdown/BigData/spark(1)/论SparkStreaming的数据可靠性和一致性.note.md` | 14/7 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Sqoop/Sqoop 数据导出：全量、增量、更新.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Storm/storm 入门原理介绍.note.md` | 16/8 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Storm/Storm安装启动.note.md` | 24/12 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Storm/Storm概念讲解和工作原理介绍.note.md` | 6/3 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/Storm/Storm的坑/Storm 集群空闲 CPU 飙高问题排查.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/Storm/Storm的基本概念.note.md` | 14/7 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/Zookeeper/Zookeeper介绍.note.md` | 10/5 | 2 | A | pending |
| `raw/wujinsen_markdown/BigData/在大厂的数据工程师工作日常工作分享.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/大数据技术文章/大数据处理系统关键层次架构.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/数据仓库/实时数仓实战项目：架构、分层、设计、场景、框架、以及流批一体....note.md` | 6/3 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/数据仓库/数据湖/Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note.md` | 176/88 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/数据采集/利用 Log-Pilot + Kafka + Elasticsearch + Kibana 搭建 kubernetes日志解决方案.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/你需要的不是实时数仓 你需要的是一款合适且强大的OLAP数据库(上).note.md` | 11/11 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/你需要的不是实时数仓 你需要的是一款强大的OLAP数据库(下).note.md` | 10/10 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/接了个破烂的数据仓库，我该如何自救？.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/数仓基础/关于OLAP数仓，这大概是史上最全面的总结！（万字干货）.note.md` | 36/18 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/数仓建设/指标管理.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/数仓建设/数据仓库主题四--（表命名规范）.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/数仓建设/网易传媒数据指标体系建设实践.note.md` | 54/27 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/数仓案例/如何优雅的设计DWS层.note.md` | 6/3 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/数仓案例/数据仓库维度建模之事实表设计.note.md` | 8/4 | 1 | A | pending |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/数仓案例/有赞指标库实践 .note.md` | 18/9 | 0 | defer | pending |
| `raw/wujinsen_markdown/BigData/架构设计/Daas/数据仓库/数仓（十四）从0到1简单搭建加载数仓DWD层（业务数据解析）.note.md` | 16/8 | 1 | A | pending |
| `raw/wujinsen_markdown/DataBase/mongodb/MongoDB 分片集群技术.note.md` | 26/13 | 0 | defer | pending |
| `raw/wujinsen_markdown/DataBase/mysql/MySQL 与 Redis 缓存的同步方案.note.md` | 34/17 | 0 | defer | pending |
| `raw/wujinsen_markdown/DataBase/mysql/mysql 同一张表查询 left join.note.md` | 6/3 | 0 | defer | pending |
| `raw/wujinsen_markdown/DataBase/mysql/MySQL死锁案例，我一口气说了6个.note.md` | 10/5 | 5 | A | pending |
| `raw/wujinsen_markdown/DataBase/mysql/MySQL索引背后的数据结构及算法原理.note.md` | 28/14 | 3 | A | pending |
| `raw/wujinsen_markdown/DataBase/mysql/mysql规范.note.md` | 26/13 | 1 | A | pending |
| `raw/wujinsen_markdown/DataBase/mysql/事务隔离级别中的可重复读能防幻读吗.note.md` | 30/15 | 3 | A | pending |
| `raw/wujinsen_markdown/DataBase/mysql/优化/解决mysql占用cpu高的问题.note.md` | 4/2 | 3 | B | pending |
| `raw/wujinsen_markdown/DataBase/mysql/全局锁和表锁 ：给表加个字段怎么有这么多阻碍？.note.md` | 6/3 | 0 | defer | pending |
| `raw/wujinsen_markdown/DataBase/mysql/安装/MySQL5.6安装步骤.note.md` | 6/3 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/DataBase/mysql/数据库事务的四大特性以及事务的隔离级别.note.md` | 10/5 | 5 | A | pending |
| `raw/wujinsen_markdown/DataBase/mysql/正确的理解MySQL的MVCC及实现原理.note.md` | 42/21 | 3 | A | pending |
| `raw/wujinsen_markdown/DataBase/mysql/索引/mysql-覆盖索引.note.md` | 46/23 | 3 | A | pending |
| `raw/wujinsen_markdown/DataBase/mysql/索引/mysql索引命中规则.note.md` | 4/2 | 4 | B | pending |
| `raw/wujinsen_markdown/DataBase/mysql/索引/MySQL索引索引不生效的情况.note.md` | 8/4 | 3 | A | pending |
| `raw/wujinsen_markdown/DataBase/mysql/索引/一次 SQL 查询优化原理分析（900W+ 数据，从 17s 到 300ms）.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/DataBase/mysql/索引/二叉树，B树，B+树.note.md` | 6/3 | 2 | B | pending |
| `raw/wujinsen_markdown/DataBase/mysql/索引/复合索引的优点和注意事项.note.md` | 2/1 | 3 | B | pending |
| `raw/wujinsen_markdown/DataBase/mysql/索引/梳理了一遍MySQL索引，发现也不过如此.note.md` | 10/5 | 4 | A | pending |
| `raw/wujinsen_markdown/DataBase/mysql/采坑/MySql的时区（serverTimezone）引发的血案.note.md` | 12/6 | 1 | A | pending |
| `raw/wujinsen_markdown/DataBase/Redis/Codis作者黄东旭细说分布式Redis架构设计和踩过的那些坑们.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/DataBase/Redis/Redis夺命16问.note.md` | 34/17 | 1 | A | pending |
| `raw/wujinsen_markdown/DataBase/Redis/Redis如何通过Spring Session实现分布式Session共享.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/DataBase/Redis/redis线程模型.note.md` | 14/7 | 1 | A | pending |
| `raw/wujinsen_markdown/DataBase/Redis/一口气说出 Redis 16 个常见使用场景.note.md` | 10/5 | 0 | defer | pending |
| `raw/wujinsen_markdown/DataBase/Redis/教程/Redis 数据类型.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/DataBase/Redis/教程/Redis连接问题.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/DataBase/Redis/教程/一口气说出 Redis 16 个常见使用场景.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/DataBase/Redis/监控/Redis开源监控--python环境依赖.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/DataBase/Redis/监控/Redis监控方案.note.md` | 8/4 | 1 | A | pending |
| `raw/wujinsen_markdown/DataBase/Redis/缓存策略/缓存更新的套路.note.md` | 8/4 | 1 | A | pending |
| `raw/wujinsen_markdown/javaweb/jackson-mapper-asl总结一下自己使用jackson处理对象与JSON之间相互转换的心得。.note.md` | 2/1 | 2 | B | pending |
| `raw/wujinsen_markdown/javaweb/jwt/Cookie,Session和Token机制和区别..note.md` | 8/4 | 2 | A | pending |
| `raw/wujinsen_markdown/javaweb/jwt/Cookie和Token.note.md` | 8/4 | 2 | A | pending |
| `raw/wujinsen_markdown/javaweb/jwt/什么是 JWT -- JSON WEB TOKEN.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/javaweb/jwt/讲真，别再使用JWT了！.note.md` | 4/2 | 2 | B | pending |
| `raw/wujinsen_markdown/javaweb/Mybatis/MyBatis 通过包含的jdbcType类型.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/javaweb/为什么我再也不使用MVC框架了？.note.md` | 10/5 | 2 | A | pending |
| `raw/wujinsen_markdown/jvm/cpu 100%.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/jvm/eclipse memory analyzer 安装使用示例.note.md` | 18/9 | 0 | defer | pending |
| `raw/wujinsen_markdown/jvm/JDK的命令行工具.note.md` | 18/9 | 5 | A | pending |
| `raw/wujinsen_markdown/jvm/JVM内存划分.note.md` | 2/1 | 5 | B | pending |
| `raw/wujinsen_markdown/jvm/Minor GC和Full GC区别.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/jvm/《深入理解 Java 内存模型》读书笔记.note.md` | 50/25 | 0 | defer | pending |
| `raw/wujinsen_markdown/jvm/个人笔记/JVM虚拟机笔记.note.md` | 10/5 | 5 | A | pending |
| `raw/wujinsen_markdown/jvm/周志明的书.note.attach/Java虚拟机：JVM高级特性与最佳实践（第2版）.md` | 193/0 | 5 | A | pending |
| `raw/wujinsen_markdown/jvm/垃圾回收器.note.md` | 16/8 | 5 | A | pending |
| `raw/wujinsen_markdown/jvm/垃圾收集算法.note.md` | 6/3 | 5 | B | pending |
| `raw/wujinsen_markdown/jvm/对象的内存布局.note.md` | 2/1 | 5 | B | pending |
| `raw/wujinsen_markdown/jvm/对象的创建.note.md` | 2/1 | 5 | B | pending |
| `raw/wujinsen_markdown/jvm/对象的访问定位.note.md` | 4/2 | 5 | B | pending |
| `raw/wujinsen_markdown/jvm/小白都能看得懂的java虚拟机内存模型.note.md` | 68/34 | 5 | A | pending |
| `raw/wujinsen_markdown/jvm/新生代Eden与两个Survivor区的解释.note.md` | 2/1 | 5 | B | pending |
| `raw/wujinsen_markdown/jvm/理解GC日志.note.md` | 2/1 | 5 | B | pending |
| `raw/wujinsen_markdown/jvm/调优/java CPU 100% 排查.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/jvm/调优/九大工具助你玩转Java性能优化.note.md` | 18/9 | 6 | A | pending |
| `raw/wujinsen_markdown/jvm/调优/假笨说-警惕大量类加载器的创建导致诡异的Full GC.note.md` | 14/7 | 0 | defer | pending |
| `raw/wujinsen_markdown/Linux/Linux常用软件安装.note.md` | 10/5 | 1 | A | pending |
| `raw/wujinsen_markdown/Linux/ubuntu创建新用户并增加管理员权限.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/Linux/VM利用host-only上网.note.md` | 12/6 | 1 | A | pending |
| `raw/wujinsen_markdown/Linux/在CentOS中用yum命令下载RPM包但不进行安装的方法.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/Linux/本地YUM源制作.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/Spring/@Autowired注解实现原理（Spring Bean的自动装配）.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/Spring/Spring 事务管理探究.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/Spring/SpringMVC/@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note.md` | 14/14 | 0 | defer | pending |
| `raw/wujinsen_markdown/Spring/SpringMVC/ModelMap、ModelAndView和@Modelattribute的区别.note.md` | 14/7 | 11 | A | pending |
| `raw/wujinsen_markdown/Spring/SpringMVC/Spring 中经典的 9 种设计模式，打死也要记住啊！.note.md` | 6/3 | 0 | defer | pending |
| `raw/wujinsen_markdown/Spring/SpringMVC/SpringMVC工作原理.note.md` | 6/3 | 11 | B | pending |
| `raw/wujinsen_markdown/Spring/SpringMVC/SpringMVC接收复杂集合参数.note.md` | 20/10 | 11 | A | pending |
| `raw/wujinsen_markdown/Spring/Spring、SpringMVC和SpringBoot看这一篇就够了！.note.md` | 12/6 | 11 | A | pending |
| `raw/wujinsen_markdown/Spring/Spring解析，加载及实例化Bean的顺序（零配置）.note.md` | 4/2 | 11 | B | pending |
| `raw/wujinsen_markdown/Spring/事务/深入理解 Spring 事务原理.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/前端/Bootstrap/JS组件Bootstrap实现弹出框和提示框效果代码.note.md` | 36/18 | 3 | A | pending |
| `raw/wujinsen_markdown/前端/JavaScript/js表单提交和submit提交的区别.note.md` | 4/2 | 3 | B | pending |
| `raw/wujinsen_markdown/前端/JQuery/JQuery教程.note.md` | 8/4 | 3 | A | pending |
| `raw/wujinsen_markdown/前端/JSON.stringify 语法实例讲解.note.md` | 32/16 | 0 | defer | pending |
| `raw/wujinsen_markdown/前端/Vue/[Abp vNext微服务实践] - vue-element-admin登录一.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/前端/前端解决跨域问题的8种方案.note.md` | 44/22 | 3 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/a安装文档/Hadoop Ambari 安装.note.md` | 28/14 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/a安装文档/hadoop2安装手顺.note.md` | 38/19 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/a安装文档/hbase多master.note.md` | 6/3 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/a安装文档/hbase安装手顺(ok).note.md` | 1/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/a安装文档/hbase管理工具 phpHBaseAdmin.note.md` | 18/9 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/a安装文档/hive web页面的搭建  .note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/a安装文档/MySQL主从安装文档（ok）.note.attach/Amoeba搞定mysql主从读写分离.md` | 25/0 | 2 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/a安装文档/MySQL主从安装文档（ok）.note.md` | 6/3 | 2 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/a安装文档/修改hive元数据库.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/flume/Flume的体系结构介绍以及Flume入门案例(往HDFS上传数据).note.md` | 4/4 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/flume/什么是Flume.note.md` | 6/3 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/flume/让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2.note.md` | 10/10 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/DataNode工作和服务原理.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/Getting LeaderNotAvailableException In Console Producer After Increasing Partiti.note.md` | 24/12 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/Hadoop mapreduce原理学习.note.md` | 12/6 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/hadoop rpc.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/hadoop1.0 和hadoop2.0 任务处理架构比较.note.md` | 6/3 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/hadoop2.2.0 HDFS HA  Federation.note.md` | 6/3 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/Hadoop2.6.0eclipse插件制作与安装.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/hadoop入门之设置datanode的心跳时间的方法.note.md` | 22/11 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/Hadoop大数据面试--Hadoop篇 [复制链接].note.md` | 12/6 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/Hadoop应用开发技术详解.note.attach/Hadoop应用开发技术详解》迷你书.md` | 148/0 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/hadoop的原生比较器RawComparator_T_ public WritableCom....note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/HDFS HA-Quorum Journal Manager.note.md` | 12/6 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/HDFS学习(三) – Namenode and Datanode.note.md` | 6/6 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/HDFS源码学习（10）——NameNode与DataNode间的通信.note.md` | 8/4 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/Mapreduce作业的工作原理.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/MapReduce源码分析总结(转).note.md` | 13/13 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/wordcount学习--------------1遍.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/ytvpn云梯VPN连接方法.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/分布式存储技术及应用（1）.note.md` | 16/8 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/大数据架构师、开发人员、公司必读：国外大数据应用的10个项目案例（图表） .note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/对称与非对称加密------------1遍.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/捐助：hadoop大全（增加yarn、flume_storm、hadoop一套视频））.note.md` | 40/20 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/架构师阅读--------------------1遍.note.attach/architect-201312.md` | 97/0 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/海量分布式存储技术HDFS（2）.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hadoop/申请域名.note.md` | 14/7 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hbase/hbase HTable之Put、delete、get等源码分析.note.md` | 10/5 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hbase/Hbase 源码分析之 Regionserver上的 Get 全流程.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hbase/HBase查询一条数据的过程. .note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hbase/hbase源码系列（十二）Get、Scan在服务端是如何处理.note.md` | 12/6 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/hbase/HBase连接池 -- HTablePool被Deprecated以及可能原因是什么 .note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/hive/hive.note.md` | 18/9 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/jvm/JDK的命令行工具.note.md` | 18/9 | 3 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/jvm/JVM内存划分.note.md` | 2/1 | 3 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/jvm/周志明的书.note.attach/Java虚拟机：JVM高级特性与最佳实践（第2版）.md` | 193/0 | 3 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/jvm/垃圾回收器.note.md` | 16/8 | 3 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/jvm/垃圾收集算法.note.md` | 6/3 | 3 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/jvm/对象的内存布局.note.md` | 2/1 | 3 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/jvm/对象的创建.note.md` | 2/1 | 3 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/jvm/对象的访问定位.note.md` | 4/2 | 3 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/jvm/理解GC日志.note.md` | 2/1 | 3 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/kafka/ActiveMQ-readme-王森丰.note.md` | 6/3 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/kafka/kafka java示例.note.md` | 6/3 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/kafka/kafka.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/kafka/kafka分布式消息系统 .note.md` | 12/6 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/kafka/分布式发布订阅消息系统 Kafka 架构设计.note.md` | 20/10 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/kafka/快速理解Kafka分布式消息队列框架.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/linux/_/dev/null 2_&1 的作用 .note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/linux/linux 内存清理 释放命令 清理linux内存.note.md` | 6/3 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/linux/linux虚拟机配置双网卡(1)(21-43-59).note.md` | 2/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/linux/linux虚拟机配置双网卡(1)(23-14-15).note.md` | 2/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/linux/linux虚拟机配置双网卡.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/linux/“网络中的跳板是什么意思.note.md` | 22/11 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/linux/堡垒机.note.md` | 38/19 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/mysql/c3p0源码分析.note.md` | 4/2 | 2 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/mysql/数据仓库中的SQL性能优化（Hive篇）.note.md` | 2/1 | 2 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/netty/Netty In Action.note.md` | 274/139 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/netty/Netty介绍.note.md` | 18/9 | 2 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/nginx+ka+lvs/Keepalived原理与实战精讲.note.md` | 2/1 | 2 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/nginx+ka+lvs/nginx upstream的分配方式。.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Buffer.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Channel.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Pipe.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Scatter Gather.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/nio/Java NIO 概述.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/redis/Jedis类图及方法说明.note.md` | 4/2 | 3 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/redis/MongoDB—readme-王森丰.note.md` | 2/1 | 3 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/redis/MongoDB——windows2.note.md` | 6/3 | 3 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/redis/redis session共享.note.md` | 20/10 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/redis/redis 学习笔记(4)-HA高可用方案Sentinel配置.note.md` | 4/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/redis/Redis-------------------------1遍.note.attach/Redis新手入门详解.md` | 1/0 | 3 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/redis/Redis.note.attach/Redis新手入门详解.md` | 1/0 | 3 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/redis/redis主从切换的集群管理.note.md` | 6/3 | 3 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/redis/redis源码分析.note.md` | 38/19 | 3 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/redis/一致性哈希算法与Java实现.note.md` | 6/3 | 3 | B | pending |
| `raw/wujinsen_markdown/大数据资料-王/rpc/轻量级分布式 RPC 框架.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/大数据资料-王/spark/spark-sql.note.md` | 8/4 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/spark/spark基础.note.md` | 46/23 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/spark/spark源码分析/executor启动和任务处理流程.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/spark/spark源码分析/master启动流程.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/spark/spark源码分析/worker启动流程.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/spark/spark源码分析/任务启动流程submit.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/spark/spark源码分析/任务提交流程.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/大数据资料-王/storm/Storm入门教程：一致性事务.note.md` | 8/4 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/x线程/Java并发编程：Lock.note.md` | 12/6 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/x线程/Java并发编程：synchronized.note.md` | 8/4 | 1 | A | pending |
| `raw/wujinsen_markdown/大数据资料-王/x线程/多线程单元测试.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/并发编程/java/synchronized与static synchronized 的区别.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/并发编程/java/volatile.note.md` | 36/18 | 2 | A | pending |
| `raw/wujinsen_markdown/并发编程/java/深入理解并发之CompareAndSet(CAS).note.md` | 5/5 | 2 | B | pending |
| `raw/wujinsen_markdown/并发编程/Netty/netty源码分析之服务端启动全解析.note.md` | 2/1 | 3 | B | pending |
| `raw/wujinsen_markdown/并发编程/Netty/Netty高性能之Reactor线程模型.note.md` | 14/7 | 4 | A | pending |
| `raw/wujinsen_markdown/并发编程/Netty/翻译文章/Java Netty 4.x 用户指南.note.md` | 80/42 | 0 | defer | pending |
| `raw/wujinsen_markdown/并发编程/Netty/翻译文章/Preface.note.md` | 6/3 | 3 | B | pending |
| `raw/wujinsen_markdown/性能优化/DATABASE/mysql left join 慢如何优化.note.md` | 6/3 | 0 | defer | pending |
| `raw/wujinsen_markdown/性能优化/DATABASE/总结   慢 SQL 问题经验总结！.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/数据结构与算法/B树.note.md` | 34/17 | 1 | A | pending |
| `raw/wujinsen_markdown/数据结构与算法/五分钟理解一致性哈希算法(consistent hashing).note.md` | 7/7 | 0 | defer | pending |
| `raw/wujinsen_markdown/数据结构与算法/哈希表（散列表）原理详解.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/数据结构与算法/怎么遍历二叉树.note.md` | 10/5 | 1 | A | pending |
| `raw/wujinsen_markdown/数据结构与算法/我们假设计算机运行一行基础代码需要执行一次运算。.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/数据结构与算法/链表反转.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/架构/DDD领域驱动/《中台架构与实现 DDD和微服务》核心思想.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/DevOps/jira/JIRA 7.8 版本的安装与破解.note.md` | 84/42 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/DevOps/nexus/nexus私服搭建.note.md` | 24/12 | 1 | A | pending |
| `raw/wujinsen_markdown/架构/DevOps/nginx/Nginx proxy_set_header Host $host 和 proxy_set_header Host $http_host 的作用对比.note.md` | 12/6 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/DevOps/nginx/Nginx 安装.note.md` | 12/6 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/DevOps/nginx/nginx配置二级目录，反向代理不同ip+端口.note.md` | 6/3 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/架构/DevOps/nginx/使用nginx部署多个前端项目.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/架构/MicroServer/Dubbo/服务架构演进.note.md` | 2/1 | 4 | B | pending |
| `raw/wujinsen_markdown/架构/MicroServer/Java 微服务实践.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/MicroServer/Service mesh ：下一代微服务？.note.md` | 76/38 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringBoot/2.0/Spring Boot 2.0 - WebFlux framework.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/Hystrix dashboard.note.md` | 10/5 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/Hystrix 使用与分析.note.md` | 76/38 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/Hystrix使用入门手册（中文）.note.md` | 28/14 | 5 | A | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/it隐/Hystrix的简单介绍.note.md` | 2/1 | 5 | B | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/it隐/使用hystrix保护你的应用.note.md` | 10/5 | 5 | A | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/it隐/使用Hystrix提高系统可用性.note.md` | 4/2 | 5 | B | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/RxJava/RxJava 从入门到放弃再到不离不弃.note.md` | 12/6 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/RxJava/RxJava 驯服数据流之 hot & cold Observable.note.md` | 6/3 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/RxJava/彻底搞清楚 RxJava 是什么东西.note.md` | 12/6 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/官网文档/How it Works.note.md` | 22/11 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/官网文档/Hystrix.note.md` | 10/5 | 5 | A | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/聊聊高并发系统/聊聊高并发系统之HTTP缓存.note.md` | 30/15 | 5 | A | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/聊聊高并发系统/聊聊高并发系统之队列术.note.md` | 8/4 | 5 | A | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/聊聊高并发系统/聊聊高并发系统之限流特技-1.note.md` | 4/2 | 6 | B | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/聊聊高并发系统/聊聊高并发系统之限流特技-2.note.md` | 4/2 | 5 | B | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/SpringCloudGateWay/gateway网关与前端请求跨域问题的解决方案.note.md` | 4/2 | 6 | B | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/SpringCloudGateWay/深入剖析网关gateway原理.note.md` | 2/1 | 5 | B | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/spring cloud Dalston.SR4 feign 实际开发中踩坑(一).note.md` | 5/5 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/spring cloud Dalston.SR4 feign 实际开发中踩坑(二).note.md` | 2/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/Spring Cloud Feign 上传文件的常见问题.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/中间件/MyCat/MySQL主从复制 + Mycat实现读写分离.note.md` | 84/42 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/中间件/MyCat/学会数据库读写分离、分表分库——用Mycat，这一篇就够了！.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/架构/分布式事务/CAP 定理的含义.note.md` | 14/7 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/分布式事务/redis/Redis 分布式锁没这么简单，网上大多数都有 bug.note.md` | 10/5 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/分布式事务/redis/Redis 分布式锁进化史解读+缺陷分析.note.md` | 12/6 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/分布式事务/redis/Redisson基本用法.note.md` | 44/22 | 2 | A | pending |
| `raw/wujinsen_markdown/架构/分布式事务/redis/Redis分布式锁的正确实现方式.note.md` | 22/11 | 1 | A | pending |
| `raw/wujinsen_markdown/架构/分布式事务/分布式事务.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/架构/分布式事务/基于RocketMQ实现分布式事务 - 完整示例.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/分库分表/分库分表的几种常见形式以及可能遇到的难.note.md` | 16/8 | 1 | A | pending |
| `raw/wujinsen_markdown/架构/安全/top无法查看病毒进程解决方案.note.md` | 6/3 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/架构/安全框架/shiro/SpringBoot 整合Shiro实现动态权限加载更新+Session共享+单点登录.note.md` | 18/9 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/安全框架/开源项目/OAuth2实现单点登录SSO.note.md` | 78/39 | 1 | A | pending |
| `raw/wujinsen_markdown/架构/容器/Docker/docker 部署 java 项目.note.md` | 12/6 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/容器/Docker/某小公司项目环境部署演变之路.note.md` | 18/9 | 1 | A | pending |
| `raw/wujinsen_markdown/架构/容器/k8s/K8s运维锦囊，19个常见故障解决方法.note.md` | 98/49 | 1 | A | pending |
| `raw/wujinsen_markdown/架构/容器/k8s/最新、最全、最详细的 K8S 学习笔记总结（2021最新版）（一）.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/容器/k8s/最新、最全、最详细的 K8S 学习笔记总结（2021最新版）（二）.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/文件存储/minio/上传文件到minio文件大小限制.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/架构/服务注册发现/nacos/War包部署无法注册到Nacos.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/架构/消息队列/RabbitMQ/RabbitMQ安装教程.note.md` | 28/14 | 1 | A | pending |
| `raw/wujinsen_markdown/架构/消息队列/RabbitMQ/RabbitMQ系列(一)：Windows下RabbitMQ安装及入门.note.md` | 5/5 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/架构/消息队列/RocketMQ/Spring Cloud异步场景分布式事务怎样做？试试RocketMQ.note.md` | 10/5 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/消息队列/RocketMQ/安装部署/rocketmq  web管理界面安装.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/消息队列/RocketMQ/问题解决/Rocketmq之No route info of this topic解决思路.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/缓存/REDIS缓存穿透，缓存击穿，缓存雪崩原因+解决方案.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/架构/编码规范/程序编码/如何更规范化编写Java 代码.note.md` | 78/39 | 0 | defer | pending |
| `raw/wujinsen_markdown/架构/轻量级分布式 RPC 框架.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/源码分析/dubbo/一、Dubbo 源码分析 – SPI 机制.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/源码分析/MyCat/Mycat源码篇 起步,Mycat源码阅读调试环境搭建.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/源码分析/nacos/Nacos 架构.note.md` | 2/1 | 0 | defer | pending |
| `raw/wujinsen_markdown/源码分析/OpenJDK/openJDK之如何下载各个版本的openJDK源码.note.md` | 16/8 | 1 | A | pending |
| `raw/wujinsen_markdown/源码分析/RocketMQ/RocketMQ 源码分析 —— 事务消息.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/源码分析/RocketMQ/事务消息.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/源码分析/spring/Spring 是如何解决循环依赖的？.note.md` | 32/16 | 0 | defer | pending |
| `raw/wujinsen_markdown/源码分析/芋道源码/精尽 Dubbo 源码分析 —— 序列化（二）之 Dubbo 实现.note.md` | 20/10 | 0 | defer | pending |
| `raw/wujinsen_markdown/面试笔试/Database/mysql/B树与B+树.note.md` | 4/2 | 2 | B | pending |
| `raw/wujinsen_markdown/面试笔试/Database/mysql/MySQL InnoDB 行记录格式（ROW_FORMAT）.note.md` | 22/11 | 0 | defer | pending |
| `raw/wujinsen_markdown/面试笔试/Dubbo/dubbo--zookeeper面试中问题解答.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/面试笔试/Dubbo/Dubbo剖析-线程模型.note.md` | 8/4 | 1 | A | pending |
| `raw/wujinsen_markdown/面试笔试/Dubbo/Dubbo剖析-负载均衡.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/面试笔试/Dubbo/Dubbo剖析-集群容错.note.md` | 4/2 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/面试笔试/Dubbo/Dubbo是阿里巴巴SOA服务化治理方案的核心框架，每天为2,000+个服务提供3,000,000,000+次访问量支持，并被广泛应用于阿里巴巴集团的各成员站点.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/面试笔试/Dubbo/精尽 Dubbo 面试题.note.md` | 10/5 | 0 | defer | pending |
| `raw/wujinsen_markdown/面试笔试/ElasticSearch/面试小结之Elasticsearch篇.note.md` | 14/7 | 1 | A | pending |
| `raw/wujinsen_markdown/面试笔试/ElasticSearch/面试小结之JVM篇.note.md` | 20/10 | 1 | A | pending |
| `raw/wujinsen_markdown/面试笔试/Java/Java常见的异常类之间的继承关系.note.md` | 2/1 | 1 | C-or-A | pending |
| `raw/wujinsen_markdown/面试笔试/Java/JVM/垃圾收集器.note.md` | 6/3 | 2 | B | pending |
| `raw/wujinsen_markdown/面试笔试/Java/基础/Java 的这些坑，你踩到了吗？.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/面试笔试/Java面试题精选/【49期】面试官：SpringMVC的控制器是单例的吗.note.md` | 12/6 | 1 | A | pending |
| `raw/wujinsen_markdown/面试笔试/Java面试题精选/【67期】谈谈ConcurrentHashMap是如何保证线程安全的？.note.md` | 4/2 | 2 | B | pending |
| `raw/wujinsen_markdown/面试笔试/Java面试题精选/【68期】面试官：对并发熟悉吗？说说Synchronized及实现原理.note.md` | 4/2 | 3 | B | pending |
| `raw/wujinsen_markdown/面试笔试/Java面试题精选/【70期】面试官：对并发熟悉吗？谈谈对volatile的使用及其原理.note.md` | 6/3 | 2 | B | pending |
| `raw/wujinsen_markdown/面试笔试/kafka/kafka丢消息处理.note.md` | 10/5 | 1 | A | pending |
| `raw/wujinsen_markdown/面试笔试/kafka/精尽 Kafka 面试题（最新更新时间：2019-12-14）.note.md` | 16/8 | 0 | defer | pending |
| `raw/wujinsen_markdown/面试笔试/kafka/面试问：Kafka 为什么速度那么快？.note.md` | 4/2 | 0 | defer | pending |
| `raw/wujinsen_markdown/面试笔试/Spring/一文带你深入理解 Spring 事务原理.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/面试笔试/Spring/关于Spring事务的面试题.note.md` | 2/1 | 2 | B | pending |
| `raw/wujinsen_markdown/面试笔试/分布式/分布式锁的实现.note.md` | 8/4 | 1 | A | pending |
| `raw/wujinsen_markdown/面试笔试/教你如何迅速秒杀掉：99%的海量数据处理面试题.note.md` | 6/3 | 2 | B | pending |
| `raw/wujinsen_markdown/面试笔试/树/B+树介绍.note.md` | 20/10 | 1 | A | pending |
| `raw/wujinsen_markdown/面试笔试/树/B树和B+树的总结.note.md` | 16/8 | 1 | A | pending |
| `raw/wujinsen_markdown/面试笔试/框架/zookeeper/精尽 Zookeeper 面试题（最新更新时间：2020-09-01.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/面试笔试/精尽面试题/dubbo/精尽 Dubbo 面试题.note.md` | 10/5 | 0 | defer | pending |
| `raw/wujinsen_markdown/面试笔试/精尽面试题/JVM/精尽 Java【虚拟机】面试题.note.md` | 34/17 | 0 | defer | pending |
| `raw/wujinsen_markdown/面试笔试/面试小结/面试小结之Elasticsearch篇.note.md` | 14/7 | 4 | A | pending |
| `raw/wujinsen_markdown/面试笔试/面试小结/面试小结之并发篇.note.md` | 14/7 | 6 | A | pending |
| `raw/wujinsen_markdown/面试笔试/面试小结/面试小结之综合篇.note.md` | 8/4 | 2 | A | pending |
| `raw/wujinsen_markdown/面试笔试/面试题整理/java CPU 100% 排查.note.md` | 8/4 | 0 | defer | pending |
| `raw/wujinsen_markdown/面试笔试/面试题整理/Java后台面试 常见问题.note.md` | 32/16 | 0 | defer | pending |
| `raw/wujinsen_markdown/面试笔试/面试题整理/复合索引的优点和注意事项.note.md` | 2/1 | 5 | B | pending |
| `raw/wujinsen_markdown/面试笔试/面试题整理/游戏排行榜算法设计实现比较.note.md` | 2/1 | 4 | B | pending |
| `raw/wujinsen_markdown/面试笔试/高级java/2018年一线互联网公司Java高级面试题总结.note.md` | 6/3 | 2 | B | pending |
| `raw/wujinsen_markdown/面试笔试/高级java/Java高级程序员面试大纲——备战金三银四跳槽季.note.md` | 12/6 | 1 | A | pending |
| `raw/wujinsen_markdown/面试笔试/高级java/缓存更新的套路.note.md` | 8/4 | 7 | A | pending |
| `raw/wujinsen_markdown/面试笔试/高级java/面试：HashMap 夺命二十一问！.note.md` | 6/3 | 0 | defer | pending |

</details>
