---
title: hive入门学习线路指导.note（原文插图 annex）
slug: annex-hive入门学习线路指导
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/Hadoop/Amabri/Hive/hive入门学习线路指导.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

hive被⼤多数企业使⽤，学习它，利于⾃⼰掌握企业所使⽤的技术，这⾥从安装使⽤到概念、原理及如 何使⽤遇到的问题，来讲解hive，希望对⼤家有所帮助。 此篇内容较多： 看完之后需要达到的⽬标

- 1.hive是什么

- 2.明⽩hive的原理

- 3.会使⽤hive

- 4.会使⽤hive编程


![image 1](assets/imageFile1.png)

1.hive ⾸先我们需要hive是什么？

让你真正明⽩什么是hive

上⾯讲的很明⽩

- 1.hive是⼀个数据仓库

- 2.hive基于hadoop。 总结为⼀句话：hive是基于hadoop的数据仓库。 hive明⽩之后，如同我们明⽩了关系数据库是什么了，那么我们该如何使⽤操作它： ⾸先我们要安装，安装分为很多种分为单机遇集群安装。 可参考下⾯内容：（正在更新）


Hive安装指导

HIVE完全分布式集群安装过程（元数据库: MySQL）

上⾯需要说明的是hive默认元数据库并不是mysql，但是因为默认元数据库存在局限，所以最好使⽤ mysql。下⾯我们附⼀张图，先从整体了解。

![image 2](assets/imageFile2.png)

![image 3](assets/imageFile3.png)

安装完毕，那么我们就需要练练⼿了，可以根据下⾯内容

hive基本操作

会了基本的操作还不够，如同我们需要了解sql语法⼀样，我们需要了解hive各种语法

- Hadoop Hive sql语法详解1-认识hive及DDL操作

- Hadoop Hive sql语法详解2-修改表结构

- Hadoop Hive sql语法详解3--DML 操作:元数据存储


- Hadoop Hive sql语法详解4--DQL 操作:数据查询SQL

- Hadoop Hive sql语法详解5--HiveQL与SQL区别


这⾥⾯包含了加载数据、查询数据等各种操作。 上⾯是⼀些基本的操作，下⾯还有⼀些负责⼀些操作如：

Hive快捷查询：不启⽤Mapreduce job启⽤Fetch task三种⽅式介绍

Hive如何执⾏⽂件中的sql语句

Hive四种数据导⼊⽅式介绍

Hive中的三种不同的数据导出⽅式介绍

Hive如何创建索引

上⾯基本属于实战类型，我们还需要⾼级进阶，我们需要了解⾥⾯的原理，我们需要知道遇到问题， 该如何解决。 原理：

全⾯了解hive

Hive体系结构介绍

hive实现原理

hive内部表与外部表区别详细介绍

HIVE中Join的专题---Join详解

让你彻底明⽩hive数据存储各种模式

Hive配置⽂件中配置项的含义详解（收藏版）

HIVE与传统数据库对⽐

hive详解 hive 配置参数说明（收藏版）

hive⽀持sql⼤全（收藏版）

hive 创建/删除/截断 表

原理⼤致懂了，基本操作会了,我们可能⼜有了更进⼀步的认识，那么hive如何结合hadoop,hbase发挥 作⽤，该如何发挥它的实际意义，被我们所⽤。

⾸先我们使⽤hive的时候，很多都与hbase相结合，这样发挥hive的⻓处，在各种查询数据⽅⾯相⽐ hbase使⽤更⽅便。

hive为什么与hbase整合

Hive与HBase整合完整指导

hadoop、hbase、hive版本对应关系

上⾯是与hbase结合使⽤，那么我们该如何与编程语⾔向结合，可以参考下⾯内容：

通过JDBC驱动连接Hive操作实例

Hive:⽤Java代码通过JDBC连接Hiveserver介绍

从 MapReduce 到 Hive 实战分析

hive实战

上⾯或许你已经对hive有所了解，下⾯的⾯试及遇到的问题，⼤家可参考

hive找不到创建的表了

Shark对Hive的⽀持与不⽀持的语法介绍

⾯试题：分别使⽤Hadoop MapReduce、hive统计⼿机流量

hive⾯试题⽬：表⼤概有2T左右，对表数据转换

hive如何通过设置⽇志定位错误

hive在腾讯分布式数据库时间分享
