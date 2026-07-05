---
title: mysql-覆盖索引.note（原文插图 annex）
slug: annex-mysql-覆盖索引
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/DataBase/mysql/索引/mysql-覆盖索引.note.md
related: [mysql-索引]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.cnblogs.com/hapyflyingpig/p/76281.html

# 什么叫做覆盖索引？

在了解覆盖索引之前我们先⼤概了解⼀下什么是聚集索引(主键索引)和辅助索引(⼆级索引)

聚集索引（主键索引）： 聚集索引就是按照每张表的主键构造⼀颗B+树，同时叶⼦节点中存放的即为整张表的记录数据。 聚集索引的叶⼦节点称为数据⻚，聚集索引的这个特性决定了索引组织表中的数据也是索引的⼀部分。

辅助索引（⼆级索引）：

⾮主键索引，叶⼦节点=键值+书签。Innodb存储引擎的书签就是相应⾏数据的主键索引值。 再来看看什么是覆盖索引，有下⾯三种理解：

解释⼀： 就是select的数据列只⽤从索引中就能够取得，不必从数据表中读取，换句话说查询列要被所使⽤的索引覆盖。 解释⼆： 索引是⾼效找到⾏的⼀个⽅法，当能通过检索索引就可以读取想要的数据，那就不需要再到数据表中读取⾏了。 如果⼀个索引包含了（或覆盖了）满⾜查询语句中字段与条件的数据就叫 做覆盖索引。

解释三：是⾮聚集组合索引的⼀种形式，它包括在查询⾥的Select、Join和Where⼦句⽤到的所有列（即建⽴索引的字段 正好是覆盖查询语句[select⼦句]与查询条件[Where⼦句]中所涉及的字段，也即，索引包含了查询正在查找的所有数 据）。

不是所有类型的索引都可以成为覆盖索引。覆盖索引必须要存储索引的列，⽽哈希索引、空间索引和全⽂索引等都不存储 索引列的值，所以MySQL只能使⽤B-Tree索引做覆盖索引

当发起⼀个被索引覆盖的查询(也叫作索引覆盖查询)时，在EXPLAIN的Extra列可以看到“Using index”的信息

![image 1](assets/imageFile1.png)

从执⾏结果上看，这个SQL语句只通过索引，就取到了所需要的数据，这个过程就叫做索引覆盖。

# ⼏种优化场景：

1.⽆WHERE条件的查询优化：

![image 2](assets/imageFile2.png)

执⾏计划中，type 为ALL，表示进⾏了全表扫描 如何改进？优化措施很简单，就是对这个查询列建⽴索引。如下，

ALERT TABLE t1 ADD KEY(staff_id);

再看⼀下执⾏计划

<table>
  <tr>
    <th>![image 3](assets/imageFile3.png)</th>
  </tr>
</table>


### explain select sql_no_cache count(staff_id) from t1

*************************** 1. row *************************** id: 1 select_type: SIMPLE table: t1

type: index possible_keys: NULL

key: staff_id key_len: 1

### ref: NULL rows: 1023849 Extra: Using index

1 row in set (0.00 sec)

<table>
  <tr>
    <th>![image 4](assets/imageFile4.png)</th>
  </tr>
</table>


possible_key: NULL，说明没有WHERE条件时查询优化器⽆法通过索引检索数据，这⾥使⽤了索引的另外⼀个优点， 即从索引中获取数据，减少了读取的数据块的数量。 ⽆where条件的查询，可以通过索引来实现索引覆盖查询，但前提条件 是，查询返回的字段数⾜够少，更不⽤说select *之类的了。毕竟，建⽴key length过⻓的索引，始终不是⼀件好事情。

查询消耗

![image 5](assets/imageFile5.png)

从时间上看，⼩了0.13 sec

# 2、⼆次检索优化

如下这个查询：

<table>
  <tr>
    <th>![image 6](assets/imageFile6.png)</th>
  </tr>
</table>


### select sql_no_cache rental_date from t1 where inventory_id<80000;

… …

- | 2005-08-23 15:08:00 |

- | 2005-08-23 15:09:17 |

- | 2005-08-23 15:10:42 | | 2005-08-23 15:15:02 |


- | 2005-08-23 15:15:19 |

- | 2005-08-23 15:16:32 |


+---------------------+ 79999 rows in set (0.13 sec)

<table>
  <tr>
    <th>![image 7](assets/imageFile7.png)</th>
  </tr>
</table>


## 执⾏计划：

<table>
  <tr>
    <th>![image 8](assets/imageFile8.png)</th>
  </tr>
</table>


### explain select sql_no_cache rental_date from t1 where inventory_id<80000***************************

1. row ***************************

id: 1 select_type: SIMPLE table: t1 type: range possible_keys: inventory_id

key: inventory_id key_len: 3

ref: NULL rows: 153734

Extra: Using index condition 1 row in set (0.00 sec)

<table>
  <tr>
    <th>![image 9](assets/imageFile9.png)</th>
  </tr>
</table>


Extra：Using index condition 表示使⽤的索引⽅式为⼆级检索，即79999个书签值被⽤来进⾏回表查询。可想⽽知， 还是会有⼀定的性能消耗的

尝试针对这个SQL建⽴联合索引，如下：

alter table t1 add key(inventory_id,rental_date);

执⾏计划：

<table>
  <tr>
    <th>![image 10](assets/imageFile10.png)</th>
  </tr>
</table>


### explain select sql_no_cache rental_date from t1 where inventory_id<80000***************************

1. row ***************************

id: 1 select_type: SIMPLE table: t1 type: range possible_keys: inventory_id,inventory_id_2

key: inventory_id_2 key_len: 3

ref: NULL rows: 162884

Extra: Using index 1 row in set (0.00 sec)

<table>
  <tr>
    <th>![image 11](assets/imageFile11.png)</th>
  </tr>
</table>


Extra：Using index 表示没有会标查询的过程，实现了索引覆盖

# 3、分⻚查询优化

如下这个查询场景

<table>
  <tr>
    <th>![image 12](assets/imageFile12.png)</th>
  </tr>
</table>


### select tid,return_date from t1 order by inventory_id limit 50000,10;

+-------+---------------------+ | tid | return_date | +-------+---------------------+

- | 50001 | 2005-06-17 23:04:36 |

- | 50002 | 2005-06-23 03:16:12 |

- | 50003 | 2005-06-20 22:41:03 |

- | 50004 | 2005-06-23 04:39:28 |

- | 50005 | 2005-06-24 04:41:20 |

- | 50006 | 2005-06-22 22:54:10 |

- | 50007 | 2005-06-18 07:21:51 |

- | 50008 | 2005-06-25 21:51:16 |

- | 50009 | 2005-06-21 03:44:32 |

- | 50010 | 2005-06-19 00:00:34 |


+-------+---------------------+ 10 rows in set (0.75 sec)

<table>
  <tr>
    <th>![image 13](assets/imageFile13.png)</th>
  </tr>
</table>


## 在未优化之前，我们看到它的执⾏计划是如此的糟糕

<table>
  <tr>
    <th>![image 14](assets/imageFile14.png)</th>
  </tr>
</table>


explain select tid,return_date from t1 order by inventory_id limit 50000,10*************************** 1. row ***************************

id: 1 select_type: SIMPLE table: t1 type: ALL

possible_keys: NULL key: NULL key_len: NULL ref: NULL

### rows: 1023675

1 row in set (0.00 sec)

<table>
  <tr>
    <th>![image 15](assets/imageFile15.png)</th>
  </tr>
</table>


看出是全表扫描。加上⽽外的排序，性能消耗是不低的 如何通过覆盖索引优化呢？ 我们创建⼀个索引，包含排序列以及返回列，由于tid是主键字段，因此，下⾯的复合索引就包含了tid的字段值

alter table t1 add index liu(inventory_id,return_date);

那么，效果如何呢？

<table>
  <tr>
    <th>![image 16](assets/imageFile16.png)</th>
  </tr>
</table>


select tid,return_date from t1 order by inventory_id limit 50000,10;

+-------+---------------------+ | tid | return_date | +-------+---------------------+ | 50001 | 2005-06-17 23:04:36 | | 50002 | 2005-06-23 03:16:12 | | 50003 | 2005-06-20 22:41:03 | | 50004 | 2005-06-23 04:39:28 | | 50005 | 2005-06-24 04:41:20 | | 50006 | 2005-06-22 22:54:10 | | 50007 | 2005-06-18 07:21:51 | | 50008 | 2005-06-25 21:51:16 | | 50009 | 2005-06-21 03:44:32 | | 50010 | 2005-06-19 00:00:34 | +-------+---------------------+ 10 rows in set (0.03 sec)

<table>
  <tr>
    <th>![image 17](assets/imageFile17.png)</th>
  </tr>
</table>


## 可以发现，添加复合索引后，速度提升0.7s！ 我们看⼀下改进后的执⾏计划

<table>
  <tr>
    <th>![image 18](assets/imageFile18.png)</th>
  </tr>
</table>


### explain select tid,return_date from t1 order by inventory_id limit 50000,10\G

*************************** 1. row *************************** id: 1 select_type: SIMPLE table: t1

type: index possible_keys: NULL

key: liu key_len: 9

ref: NULL rows: 50010

Extra: Using index 1 row in set (0.00 sec)

<table>
  <tr>
    <th>![image 19](assets/imageFile19.png)</th>
  </tr>
</table>


执⾏计划也可以看到，使⽤到了复合索引，并且不需要回表 对⽐⼀下如下的改写SQL，思想是通过索引消除排序

select a.tid,a.return_date from t1 a inner join (select tid from t1 order by inventory_id limit 800000,10) b on a.tid=b.tid;

并在此基础上，我们为inventory_id列创建索引，并删除之前的覆盖索引

alter table t1 add index idx_inid(inventory_id)； drop index liu;

然后收集统计信息。

<table>
  <tr>
    <th>![image 20](assets/imageFile20.png)</th>
  </tr>
</table>


### select a.tid,a.return_date from t1 a inner join (select tid from t1 order by inventory_id limit 800000,10) b on a.tid=b.tid;

+--------+---------------------+ | tid | return_date | +--------+---------------------+

- | 800001 | 2005-08-24 13:09:34 |

- | 800002 | 2005-08-27 11:41:03 |

- | 800003 | 2005-08-22 18:10:22 |

- | 800004 | 2005-08-22 16:47:23 |

- | 800005 | 2005-08-26 20:32:02 |

- | 800006 | 2005-08-21 14:55:42 |

- | 800007 | 2005-08-28 14:45:55 |

- | 800008 | 2005-08-29 12:37:32 |

- | 800009 | 2005-08-24 10:38:06 |

- | 800010 | 2005-08-23 12:10:57 |


+--------+---------------------+

<table>
  <tr>
    <th>![image 21](assets/imageFile21.png)</th>
  </tr>
</table>


这种优化⼿段较前者时间多消耗了⼤约140ms。这种优化⼿段虽然使⽤索引消除了排序，但是还是要通过主键值回表查 询。因此，在select返回列较少或列宽较⼩的时候，我们可以通过建⽴复合索引的⽅式优化分⻚查询，效果更佳，因为它不需 要回表！

# 4、建了索引但是查询不⾛索引

表结构：

<table>
  <tr>
    <th>![image 22](assets/imageFile22.png)</th>
  </tr>
</table>


CREATE TABLE `t_order` ( `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, `order_code` char(12) NOT NULL, `order_amount` decimal(12,2) NOT NULL, PRIMARY KEY (`id`), UNIQUE KEY `uni_order_code` (`order_code`) USING BTREE ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

<table>
  <tr>
    <th>![image 23](assets/imageFile23.png)</th>
  </tr>
</table>


查询语句：

select order_code,order_amount from t_order order by order_code limit 1000;

发现虽然在order_code上建了索引，但是看查询计划却不⾛索引，为什么呢？因为数据⾏读取order_amount，所以是随 机IO。那怎么办？重新建索引，使⽤覆盖索引。

ALTER TABLE `t_order` ADD INDEX `idx_ordercode_orderamount` USING BTREE (`order_code` ASC, `order_amount` ASC);

这样再查看SQL的执⾏计划，就发现可以⾛到索引了。

总结：覆盖索引的优化及限制

覆盖索引是⼀种⾮常强⼤的⼯具，能⼤⼤提⾼查询性能，只需要读取索引⽽不需要读取数据，有以下优点：

- 1、索引项通常⽐记录要⼩，所以MySQL访问更少的数据。
- 2、索引都按值得⼤⼩存储，相对于随机访问记录，需要更少的I/O。
- 3、数据引擎能更好的缓存索引，⽐如MyISAM只缓存索引。
- 4、覆盖索引对InnoDB尤其有⽤，因为InnoDB使⽤聚集索引组织数据，如果⼆级索引包含查询所需的数据，就不再需要在


聚集索引中查找了。 限制：

- 1、覆盖索引也并不适⽤于任意的索引类型，索引必须存储列的值。
- 2、Hash和full-text索引不存储值，因此MySQL只能使⽤BTree。
- 3、不同的存储引擎实现覆盖索引都是不同的，并不是所有的存储引擎都⽀持覆盖索引。
- 4、如果要使⽤覆盖索引，⼀定要注意SELECT列表值取出需要的列，不可以SELECT * ，因为如果将所有字段⼀起做索引会


导致索引⽂件过⼤，查询性能下降。

参考⽂献:

- 【1】 袋⿏云技术团队博客，
- 【2】MySQL覆盖索引优化，
- 【3】MySQL SQL优化之索引覆盖
- 【4】 Baron Schwartz等 著，宁海元等 译 ；《⾼性能MySQL》（第3版）； 电⼦⼯业出版社 ，2013


https://yq.aliyun.com/articles/62419 https://yq.aliyun.com/articles/709783
