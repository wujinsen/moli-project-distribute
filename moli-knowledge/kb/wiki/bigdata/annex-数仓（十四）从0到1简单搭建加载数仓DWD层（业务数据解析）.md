---
title: 数仓（十四）从0到1简单搭建加载数仓DWD层（业务数据解析）.note（原文插图 annex）
slug: annex-数仓（十四）从0到1简单搭建加载数仓DWD层（业务数据解析）
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/架构设计/Daas/数据仓库/数仓（十四）从0到1简单搭建加载数仓DWD层（业务数据解析）.note.md
related: [数仓分层与建模]
created: 2026-07-05
updated: 2026-07-05
---

上⼀节我们讲解了数仓DWD层（⽤户⾏为⽇志数据）的搭建、解析、加载。并且讲解了通过编写java 代码来实现UDTF功能。

这节详解数仓DWD层（关于⽤户交易等业务数据）的搭建、解析加载。

# ⼀、DWD层结构

前⾯⼀节已经说过了，DWD层是对⽤户的⽇志⾏为进⾏解析，以及对交易业务数据采⽤维度模型的⽅ 式重新建模（即维度退化）。

- 1、回顾DWD层概念 我们在来回顾⼀下数 仓 （ 四 ） 数据 仓 库 分 层 对DWD层（Data Warehouse Detail）的定义：“明细粒度 事实层：是以业务过程来作为建模驱动，基于每个具体的业务过程特点，构建最细粒度的明细层事实 表（注意是最细粒度）。需要结合企业的数据使⽤特点，将明细事实表的某些重要维度属性字段做适 当冗余，即宽表化处理。明细粒度事实层的表通常也被称为逻辑事实表。”

- 2、DWD层建模4步骤 DWD层是事实建模层，这层建模主要做的4个步骤：


![image 1](assets/imageFile1.png)

我们⽬前已经完成了：

- 2.1、选择业务过程 选择了事实表，⽐如：订单事实表、⽀付事实表等；
- 2.2、声明粒度 即确认每⼀⾏数据是什么，要保证事实表的最⼩粒度。
- 2.3、确认维度 在前⾯两节中我们确定了6个维度；⽐如时间、⽤户、地点、商品、优惠券、活动这6个维度。数 仓 （ ⼗ ⼆ ） 从 0到 1简 单 搭 建 加 载 数 仓 DIM层 以 及 拉 链 表 处 理 ， 思路是其他ODS层表的维度需要向这6个维 度进⾏退化到DIM层，这样做的⺟的是减少后期的⼤量表之间的join操作。


![image 2](assets/imageFile2.png)

- 6个维度表的退化操作其实我们在前⾯的第⼗⼆章节已经做了即DIM层。除了第3张表即商品维度表是5 个表退化到1张表上，其他都是1-2张表退化到1张表上，相对⽐较简单。
- 2.4、确认事实 就是确认事实表的每张事实表的度量值。


![image 3](assets/imageFile3.png)

下⾯我们根据事实表的加载⽅式来选择⼏个实战操作⼀下。

# ⼆、DWD层-事务型事实表

关于事实表分类，我们在数仓（三）关系建模和维度建模，⾥⾯说过，分为6类事实表。

- 1、事务型事实表的概念 适⽤于不会发⽣变化的业务。业务表的同步策略是增量同步。以每个事务或事件为单位，例如⼀个销 售订单记录，⼀笔⽀付记录等，作为事实表⾥的⼀⾏数据。⼀旦事务被提交，事实表数据被插⼊，数 据就不再进⾏更改，其更新⽅式为增量更新。 8张表⾥⾯包含：⽀付事实表、评价事实表、退款事实表、订单明细(详情)事实表
- 2、解析思路 根据事实表（⾏），选择不同的维度（列）来建表。


![image 4](assets/imageFile4.png)

- 3、⽀付事实表（事务型事实表） 需要时间、⽤户、地区三个维度，查看ODS层表ods_payment_info，发现没有地区维度字段。所以通 过ods_order_info表关联做join获取该字段。


- 3.1、建表语句 drop table if exists dwd_fact_payment_info; create external table dwd_fact_payment_info (

`id` string COMMENT 'id', `out_trade_no` string COMMENT '对外业务编号', `order_id` string COMMENT '订单编号', `user_id` string COMMENT '⽤户编号', `alipay_trade_no` string COMMENT '⽀付宝交易流⽔编号', `payment_amount` decimal(16,2) COMMENT '⽀付⾦额', `subject` string COMMENT '交易内容', `payment_type` string COMMENT '⽀付类型', `payment_time` string COMMENT '⽀付时间', `province_id` string COMMENT '省份ID'

) COMMENT '⽀付事实表表' PARTITIONED BY (`dt` string) stored as parquet location '/warehouse/gmall/dwd/dwd_fact_payment_info/' tblproperties ("parquet.compression"="lzo");

- 3.2、装载语句


province_id省份ID这个字段通过 ods_order_info表做join获取

SET hive.input.format=org.apache.hadoop.hive.ql.io.HiveInputFormat; insert overwrite table dwd_fact_payment_info partition(dt='2021-05-03') select

pi.id, pi.out_trade_no, pi.order_id, pi.user_id, pi.alipay_trade_no, pi.total_amount, pi.subject, pi.payment_type, pi.payment_time, oi.province_id

from (

select * from ods_payment_info where dt='2021-05-03'

)pi join (

select id, province_id from ods_order_info where dt='2021-05-03'

)oi on pi.order_id = oi.id;

- 4、退款事实表（事务型事实表） 需要时间、⽤户、商品三个维度，查看ODS层表ods_order_refund_info，所有字段都有，那么直接取 数装载。

- 4.1、创建表 drop table if exists dwd_fact_order_refund_info; create external table dwd_fact_order_refund_info(

`id` string COMMENT '编号', `user_id` string COMMENT '⽤户ID', `order_id` string COMMENT '订单ID', `sku_id` string COMMENT '商品ID', `refund_type` string COMMENT '退款类型', `refund_num` bigint COMMENT '退款件数', `refund_amount` decimal(16,2) COMMENT '退款⾦额', `refund_reason_type` string COMMENT '退款原因类型', `create_time` string COMMENT '退款时间'

) COMMENT '退款事实表' PARTITIONED BY (`dt` string) stored as parquet location '/warehouse/gmall/dwd/dwd_fact_order_refund_info/' tblproperties ("parquet.compression"="lzo");

- 4.2、装载时间


直接从ODS层查到数据后装载。

insert overwrite table dwd_fact_order_refund_info partition(dt='2021-05-03') select

id, user_id, order_id, sku_id, refund_type, refund_num, refund_amount, refund_reason_type, create_time

from ods_order_refund_info where dt='2021-05-03';

- 5、评价事实表、订单明细事实表（事务型事实表） 都和上⾯“退款事实表”处理⽅法⼀样，并且所有字段均从ODS层ods_coment_info直接获取。你是否 可以⾃⼰创建呢？


# 三、DW层-周期型快照事实表

- 1、周期型快照事实表的概念 周期型快照事实表，表中不会保留所有数据，只保留固定时间间隔的数据，例如每天或者每⽉的销售 额或每⽉的账户余额等。例如购物⻋，有加减商品，随时都有可能变化，但是我们更关⼼每天结束时 这⾥⾯有多少商品，⽅便我们后期统计分析。相当于每天⼀个全量快照，业务表的同步策略是全量同 步。

- 2、解析思路 每天做⼀次快照，导⼊的数据是全量，区别于事务型事实表是每天导⼊新增。 存储的数据⽐较讲究时效性，时间太久了的意义不⼤，可以删除以前的数据。

- 3、加购事实表（周期型快照事实表）


![image 5](assets/imageFile5.png)

- 3.1、创建表结构 所有字段ODS层，fact_cart_info表都有。 drop table if exists dwd_fact_cart_info; create external table dwd_fact_cart_info(

`id` string COMMENT '编号', `user_id` string COMMENT '⽤户id', `sku_id` string COMMENT 'skuid', `cart_price` string COMMENT '放⼊购物⻋时价格', `sku_num` string COMMENT '数量', `sku_name` string COMMENT 'sku名称 (冗余)', `create_time` string COMMENT '创建时间', `operate_time` string COMMENT '修改时间', `is_ordered` string COMMENT '是否已经下单。1为已下单;0为未下单', `order_time` string COMMENT '下单时间', `source_type` string COMMENT '来源类型', `srouce_id` string COMMENT '来源编号'

) COMMENT '加购事实表' PARTITIONED BY (`dt` string) stored as parquet location '/warehouse/gmall/dwd/dwd_fact_cart_info/' tblproperties ("parquet.compression"="lzo");

- 3.2、装载数据 insert overwrite table dwd_fact_cart_info partition(dt='2021-05-03')


select id, user_id, sku_id, cart_price, sku_num, sku_name, create_time, operate_time, is_ordered, order_time, source_type, source_id

from ods_cart_info

- where dt='2020-06-14';


- 4、收藏事实表 收藏事实表的操作和加购事实表⼀样，从时间、商品、⽤户三个维度来创建表。


# 四、DWD层-累积型快照事实表

- 1、累积型快照事实表的概念 累积型快照事实表，⽤于周期性发⽣变化的业务，即需要周期性的跟踪业务事实的变化。例如：数据 仓库中可能需要累积或者存储订单从下订单开始，到订单商品被打包、运输、和签收的各个业务阶段 的时间点数据来跟踪订单声明周期的进展情况。当这个业务过程进⾏时，事实表的记录也要不断更 新。 业务表的同步策略是新增以及变化同步。

- 2、解析思路 我们以优惠券领⽤事实表为例。⾸先要了解优惠卷的⽣命周期：领取优惠卷⸺>⽤优惠卷下单⸺>优 惠卷参与⽀付 累积型快照事实表使⽤：统计优惠卷领取次数、优惠卷下单次数、优惠卷参与⽀付次数。

- 3、优惠券领⽤事实表（累积型快照事实表）


![image 6](assets/imageFile6.png)

3.1、创建表结构

drop table if exists dwd_fact_coupon_use; create external table dwd_fact_coupon_use(

`id` string COMMENT '编号', `coupon_id` string COMMENT '优惠券ID',

`user_id` string COMMENT 'userid', `order_id` string COMMENT '订单id', `coupon_status` string COMMENT '优惠券状态', `get_time` string COMMENT '领取时间', `using_time` string COMMENT '使⽤时间(下单)', `used_time` string COMMENT '使⽤时间(⽀付)'

) COMMENT '优惠券领⽤事实表' PARTITIONED BY (`dt` string) stored as parquet location '/warehouse/gmall/dwd/dwd_fact_coupon_use/' tblproperties ("parquet.compression"="lzo");

注意：这⾥dt是按照优惠卷领⽤时间get_time做为分区

`get_time` string COMMENT '领取时间', `using_time` string COMMENT '使⽤时间(下单)', `used_time` string COMMENT '使⽤时间(⽀付)'

3.2装载数据 ⾸⽇装载分析

![image 7](assets/imageFile7.png)

⾸⽇装载SQL代码,注意是动态分区。

insert overwrite table dwd_coupon_use partition(dt) select

id, coupon_id, user_id, order_id, coupon_status, get_time, using_time,

used_time, expire_time, coalesce(date_format(used_time,'yyyy-MM-dd'),date_format(expire_time,'yyyy-MM-

dd'),'9999-99-99') from ods_coupon_use

- where dt='2021-05-03';


每⽇装载思路分析

![image 8](assets/imageFile8.png)

SQL代码

set hive.exec.dynamic.partition.mode=nonstrict; set hive.input.format=org.apache.hadoop.hive.ql.io.HiveInputFormat; insert overwrite table dwd_fact_coupon_use partition(dt) select

if(new.id is null,old.id,new.id), if(new.coupon_id is null,old.coupon_id,new.coupon_id), if(new.user_id is null,old.user_id,new.user_id), if(new.order_id is null,old.order_id,new.order_id), if(new.coupon_status is null,old.coupon_status,new.coupon_status), if(new.get_time is null,old.get_time,new.get_time), if(new.using_time is null,old.using_time,new.using_time), if(new.used_time is null,old.used_time,new.used_time), date_format(if(new.get_time is null,old.get_time,new.get_time),'yyyy-MM-dd')

from (

select id, coupon_id, user_id, order_id, coupon_status, get_time, using_time, used_time

from dwd_fact_coupon_use

where dt in (

select

date_format(get_time,'yyyy-MM-dd') from ods_coupon_use where dt='2021-05-04'

)

)old full outer join (

select id, coupon_id, user_id, order_id, coupon_status, get_time, using_time, used_time

from ods_coupon_use where dt='2021-05-04'

)new on old.id=new.id;

其他类似的累积型事实表也是这个操作思路。

这样我们就完成了DWD层业务数据的建模和设计、搭建和使⽤包括简要的SQL代码的编写。

现在我们来总结⼀下： DWD层是对事实表的处理，代表的是业务的最⼩粒度层。任何数据的记录都可以从这⼀层获取，为后 续的DWS和DWT层做准备。DWD层是站在选择好事实表的基础上，对维度建模的视⻆，这层维度建模主要 做的4个步骤：选择业务过程、声明粒度、确认维度、确认事实。
