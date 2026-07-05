---
title: mysql left join 慢如何优化.note（原文插图 annex）
slug: annex-mysql-left-join-慢如何优化
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/性能优化/DATABASE/mysql left join 慢如何优化.note.md
related: [mysql-索引]
created: 2026-07-05
updated: 2026-07-05
---

- 1.
- 2.
- 3.


今天遇到⼀个left join优化的问题，搞了⼀下午，中间查了不少资料，对MySQL的查询计划还有查 询优化有了更进⼀步的了解，做⼀个简单的记录： select c.* from hotel_info_original c left join hotel_info_colection h on c.hotel_type=h.hotel_type and c.hotel_id =h.hotel_id where h.hotel_id is nul

这个sql是⽤来查询出c表中有h表中⽆的记录，所以想到了⽤left join的特性（返回左边全部记录，右表 不满⾜匹配条件的记录对应⾏返回nul）来满⾜需求，不料这个查询⾮常慢。先来看查询计划：

![image 1](assets/imageFile1.png)

mysql left join 慢如何优化

rows代表这个步骤相对上⼀步结果的每⼀⾏需要扫描的⾏数，可以看到这个sql需要扫描的⾏数为 3573*8134，⾮常⼤的⼀个数字。本来c和h表的记录条数分别为4 0+和1 0+，这⼏乎是 两个表做笛卡尔积的开销了（select * from c,h）。

于是我上⽹查了下MySQL实现join的原理，原来MySQL内部采⽤了⼀种叫做 nested l op join的算法。 Nested Lop Join 实际上就是通过驱动表的结果集作为循环基础数据，然后⼀条⼀条的通过该结果集 中的数据作为过滤条件到下⼀个表中查询数据，然后合并结果。如果还有第三个参与 Join，则再通过 前两个表的 Join 结果集作为循环基础数据，再⼀次通过循环查询条件到第三个表中查询数据，如此往 复，基本上MySQL采⽤的是最容易理解的算法来实现join。所以驱动表的选择⾮常重要，驱动表的数 据⼩可以显著降低扫描的⾏数。 那么为什么⼀般情况下join的效率要⾼于left join很多？很多⼈说不明⽩原因，只⼈云亦云，我今天下 午感悟出来了⼀点。⼀般情况下参与联合查询的两张表都会⼀⼤⼀⼩，如果是join，在没有其他过滤条 件的情况下MySQL会选择⼩表作为驱动表，但是left join⼀般⽤作⼤表去join⼩表，⽽left join本身的特 性决定了MySQL会⽤⼤表去做驱动表，这样下来效率就差了不少，如果我把上⾯那个sql改成： select c.*fromhotel_info_original cjoinhotel_info_colectionhonc.hotel_type=h.hotel_typeand c.hotel_id =h.hotel_id 查询计划如下：

![image 2](assets/imageFile2.png)

mysql left join 慢如何优化

很明显，MySQL选择了⼩表作为驱动表，再配合(hotel_id,hotel_type)上的索引瞬间降低了好多个 数量级。。。。。 另外，我今天还明⽩了⼀个关于left join 的通⽤法则，即：如果where条件中含 有右表的⾮空条件（除开is nul），则left join语句等同于join语句，可直接改写成join语句。 后 记：

随着查看MySQL reference manual对这个问题进⾏了更进⼀步的了解。MySQL在执⾏join时会把join 分为system/const/eq_ref/ref/range/index/ALl等好⼏类，连接的效率从前往后 依次递减，对于我的第 ⼀个sql，连接类型是index，所以⼏乎是全表扫描的效果。但是我很奇怪我在(hotel_id,hotel_type)两 列上声明了unique key，根据官⽅⽂档连接类型应该是eq_ref才对，

这个问题⼀直困扰了我两天，在gogle和stackoverflow上都没有找到能够解释这个问题的⽂章，莫⾮ 我这个问题⽆解了？抱着解决这个问题的决⼼今天⼜翻看了⼀遍MySQL官⽅⽂档 关于优化查询的部 分，看到了这样⼀句：这⾥的⼀个问题是MySQL能更⾼效地在声明具有相同类型和尺⼨的列上使⽤索 引。我感觉我找到了问题所在，于是我将original和 colection表的(hotel_type,hotel_id)的encoding和 colation（决定字符⽐较的规则）全部改成统⼀的utf8_general_ci，然后再次运⾏第⼀条sql的查询计 划，得到如下结果： 连接类型已经由index优化到了ref，如果将hotel_type申明为not nul可以优化到eq_ref，不过这⾥影响 不⼤了，优化后这条sql能在0.01ms内运⾏完。

![image 3](assets/imageFile3.png)

mysql left join 慢如何优化

4. 4

那么如何优化left join： 1、条件中尽量能够过滤⼀些⾏将驱动表变得⼩⼀点，⽤⼩表去驱动⼤表 2、右表的条件列⼀定要加上 索 引 （ 主 键 、 唯 ⼀ 索 引 、 前 缀 索 引 等 ） ， 最 好 能 够 使 type达 到 range及 以 上 （ref,eq_ref,const,system） 3、⽆视以上两点，⼀般不要⽤left join~！

system/const/eq_ref/ref/range/index/AL
