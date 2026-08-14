---
title: MySQL 与 Redis 缓存的同步方案.note（原文插图 annex）
slug: annex-MySQL-与-Redis-缓存的同步方案
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/DataBase/mysql/MySQL 与 Redis 缓存的同步方案.note.md
related: [mysql-索引面试题]
created: 2026-07-05
updated: 2026-07-05
---

htps:/mp.weixin.q.com/s/SXZODKvXjdyZSWCVS_W2sg

本⽂介绍MySQL与Redis缓存的同步的两种⽅案

- ⽅案1：通过MySQL⾃动同步刷新Redis，MySQL触发器+UDF函数实现

- ⽅案2：解析MySQL的binlog实现，将数据库中的数据同步到Redis


⼀、⽅案1（UDF）

场景分析： 当我们对MySQL数据库进⾏数据操作时，同时将相应的数据同步到Redis中，同步到 Redis之后，查询的操作就从Redis中查找 过程⼤致如下：

在MySQL中对要操作的数据设置触发器Trigger，监听操作 客户端（NodeServer）向MySQL中写⼊数据时，触发器会被触发，触发之后调⽤MySQL的UDF函数 UDF函数可以把数据写⼊到Redis中，从⽽达到同步的效果

![image 1](assets/imageFile1.png)

⽅案分析：

这种⽅案适合于读多写少，并且不存并发写的场景 因为MySQL触发器本身就会造成效率的降低，如果⼀个表经常被操作，这种⽅案显示是不合适的

演示案例

下⾯是MySQL的表

![image 2](assets/imageFile2.png)

下⾯是UDF的解析代码

![image 3](assets/imageFile3.png)

## 定义对应的触发器

![image 4](assets/imageFile4.png)

![image 5](assets/imageFile5.png)

![image 6](assets/imageFile6.png)

# ⼆、⽅案2（解析binlog）

在介绍⽅案2之前我们先来介绍⼀下MySQL复制的原理，如下图所示：

主服务器操作数据，并将数据写⼊Bin log 从服务器调⽤I/O线程读取主服务器的Bin log，并且写⼊到⾃⼰的Relay log中，再调⽤SQL线程从Relay log中解析数据，从⽽同步到⾃⼰的数据库中

![image 7](assets/imageFile7.png)

⽅案2就是：

上⾯MySQL的整个复制流程可以总结为⼀句话，那就是：从服务器读取主服务器Bin log中的数据，从 ⽽同步到⾃⼰的数据库中 我们⽅案2也是如此，就是在概念上把主服务器改为MySQL，把从服务器改为Redis⽽已（如下图所 示），当MySQL中有数据写⼊时，我们就解析MySQL的Bin log，然后将解析出来的数据写⼊到Redis 中，从⽽达到同步的效果

![image 8](assets/imageFile8.png)

例如下⾯是⼀个云数据库实例分析：

云数据库与本地数据库是主从关系。云数据库作为主数据库主要提供写，本地数据库作为从数据库从 主数据库中读取数据 本地数据库读取到数据之后，解析Bin log，然后将数据写⼊写⼊同步到Redis中，然后客户端从Redis读 数据

![image 9](assets/imageFile9.png)

这个技术⽅案的难点就在于： 如何解析MySQL的Bin Log。但是这需要对binlog⽂件以及MySQL有⾮常深 ⼊的理解，同时由于binlog存在Statement/Row/Mixedlevel多种形式，分析binlog实现同步的⼯作量是⾮常⼤ 的

Canal开源技术

canal是阿⾥巴巴旗下的⼀款开源项⽬，纯Java开发。基于数据库增量⽇志解析，提供增量数据订阅 &消费，⽬前主要⽀持了MySQL（也⽀持mariaDB） 开源参考地址有：https://github.com/liukelin/canal_mysql_nosql_sync ⼯作原理（模仿MySQL复制）：

canal模拟mysql slave的交互协议，伪装⾃⼰为mysql slave，向mysql master发送dump协议 mysql master收到dump请求，开始推送binary log给slave（也就是canal） canal解析binary log对象（原始为byte流）

![image 10](assets/imageFile10.png)

架构：

eventParser (数据源接⼊，模拟slave协议和master进⾏交互，协议解析) eventSink (Parser和Store链接器，进⾏数据过滤，加⼯，分发的⼯作) eventStore (数据存储) metaManager (增量订阅&消费信息管理器) server代表⼀个canal运⾏实例，对应于⼀个jvm instance对应于⼀个数据队列 （1个server对应1..n个instance) instance模块：

![image 11](assets/imageFile11.png)

⼤致的解析过程如下：

parse解析MySQL的Bin log，然后将数据放⼊到sink中 sink对数据进⾏过滤，加⼯，分发 store从sink中读取解析好的数据存储起来 然后⾃⼰⽤设计代码将store中的数据同步写⼊Redis中就可以了 其中parse/sink是框架封装好的，我们做的是store的数据读取那⼀步

![image 12](assets/imageFile12.png)

更多关于Cancl可以百度搜索 下⾯是运⾏拓扑图

![image 13](assets/imageFile13.png)

MySQL表的同步，采⽤责任链模式，每张表对应⼀个Filter 。例如zvsync中要⽤到的类设计如下：

![image 14](assets/imageFile14.png)

下⾯是具体化的zvsync中要⽤到的类 ，每当新增或者删除表时，直接进⾏增删就可以了

![image 15](assets/imageFile15.png)

# 三、附加

本⽂上⾯所介绍的都是从MySQL中同步到缓存中。

但是在实际开发中可能有⼈会⽤下⾯的⽅案：

客户端有数据来了之后，先将其保存到Redis中，然后再同步到MySQL中 这种⽅案本身也是不安全/不可靠的，因此如果Redis存在短暂的宕机或失效，那么会丢失数据

![image 16](assets/imageFile16.png)

![image 17](assets/imageFile17.png)

往期推荐

阿⾥⾯试：“说⼀下从 url 输⼊到返回请求的过程”的难度就是不⼀样！ ⼴州⼀公司招开发，“不加班的都是垃圾”引热议！ “容灾”和“备份”的区别？原来如此！ 继Elastic怒喷云服务商⽩嫖之后，AWS 终于退出ES的开源分⽀：OpenSearch！ 淘宝⾯试：说⼀下 ThreadLocal 的原理？⽹友：现在⾯试不看源码不⾏ ⽤低代码平台开发⽐⽤IDEA还⽜逼吗？
