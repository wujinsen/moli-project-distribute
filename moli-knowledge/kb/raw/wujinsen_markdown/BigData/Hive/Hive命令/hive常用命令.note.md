建表语句:

drop table if exists ods_order_info; create external table ods_order_info(

id string COMENT 'bianhao', total_amount decimal(10,2) COMENT 'jine'

)PARTITIONED BY (`dt` string) row format delimited fields terminated by '\t' location '/warehouse/gmal/ods/ods_order_info/';

create external table ods_order_info2( id string COMENT 'bianhao', total_amount decimal(10,2) COMENT 'jine'

) row format delimited fields terminated by '\t' location '/warehouse/gmal/ods/ods_order_info/';

# 两者区别：insert into直接追加到表中数据的尾部，⽽insert overwrite会重写数 据，既先进⾏删除，再写⼊。

查看表结构:

![image 1](<hive常用命令.note_images/imageFile1.png>)

insert into table ods_order_info partition(dt='2021-06-18')values('1', 10); insertOVERWRITE table ods_order_info partition(dt='2021-06-18')values('1', 10);

insert into ods_order_info2values('1', 10);

