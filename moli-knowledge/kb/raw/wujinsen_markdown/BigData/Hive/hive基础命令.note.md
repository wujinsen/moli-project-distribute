简介 这篇⽂章主要介绍了hive表信息查询：查看表结构、表操作等(示例代码)以及相关的经验技巧，⽂章约 7271字，浏览量830，点赞数8，值得推荐！ 转⾃⽹友的，主要是⾃⼰备份下 有时候不记得！ 问题导读：

- 1.如何查看hive表结构？

- 2.如何查看表结构信息？

- 3.如何查看分区信息？

- 4.哪个命令可以模糊搜索表


- 1.hive模糊搜索表 show tables like ‘*name*‘;

- 2.查看表结构信息 desc formatted table_name; desc table_name;

- 3.查看分区信息 show partitions table_name;

- 4.根据分区查询数据 select table_coulm from table_name where partition_name = ‘2014-02-25‘;

- 5.查看hdfs⽂件信息 dfs -ls /user/hive/warehouse/table02;

- 6.从⽂件加载数据进表(OVERWRITE覆盖,追加不需要OVERWRITE关键字) LOAD DATA LOCAL INPATH ‘dim_csl_rule_config.txt‘ OVERWRITE into table dim.dim_csl_rule_config;

--从查询语句给table插⼊数据 INSERT OVERWRITE TABLE test_h02_click_log PARTITION(dt) select * from stage.s_h02_click_log where dt=‘2014-01-22‘ limit 100;

- 7.导出数据到⽂件 insert overwrite directory ‘/tmp/csl_rule_cfg‘ select a.* from dim.dim_csl_rule_config a; hive -e "select day_id,pv,uv,ip_count,click_next_count,second_bounce_rate,return_visit,pg_type from


tmp.tmp_h02_click_log_baitiao_ag_sum where day_id in (‘2014-03-06‘,‘2014-03-07‘,‘2014-03-08‘,‘2014-0309‘,‘2014-03-10‘);"> /home/jrjt/testan/baitiao.dat;

- 8.⾃定义udf函数

- 1.继承UDF类

- 2.重写evaluate⽅法

- 3.把项⽬打成jar包

- 4.hive中执⾏命令add jar /home/jrjt/dwetl/PUB/UDF/udf/GetProperty.jar;

- 5.创建函数create temporary function get_pro as ‘jd.Get_Property‘//jd.jd.Get_Property为类路径;


- 9.查询显示列名 及 ⾏转列显示 set hive.cli.print.header=true; // 打印列名 set hive.cli.print.row.to.vertical=true; // 开启⾏转列功能, 前提必须开启打印列名功能 set hive.cli.print.row.to.vertical.num=1; // 设置每⾏显示的列数

- 10.查看表⽂件⼤⼩,下载⽂件到某个⽬录,显示多少⾏到某个⽂件 dfs -du hdfs://BJYZH3-HD-JRJT-4137.jd.com:54310/user/jrjt/warehouse/stage.db/s_h02_click_log; dfs -get /user/jrjt/warehouse/ods.db/o_h02_click_log_i_new/dt=2014-01-21/000212_0 /home/jrjt/testan/; head -n 1000 ⽂件名 > ⽂件名

- 11.杀死某个任务 不在hive shell中执⾏ hadoop job -kill job_201403041453_58315

- 12.hive-wui路径

- 13.删除分区 alter table tmp_h02_click_log_baitiao drop partition(dt=‘2014-03-01‘); alter table d_h02_click_log_basic_d_fact drop partition(dt=‘2014-01-17‘);

- 14.hive命令⾏操作 执⾏⼀个查询,在终端上显示mapreduce的进度，执⾏完毕后，最后把查询结果输出到终端上，接着hive进

程退出，不会进⼊交互模式。

hive -e ‘select table_cloum from table‘

-S，终端上的输出不会有mapreduce的进度，执⾏完毕，只会把查询结果输出到终端上。这个静⾳模式很实

⽤，,通过第三⽅程序调⽤，第三⽅程序通过hive的标准输出获取结果集。 hive -S -e ‘select table_cloum from table‘ 执⾏sql⽂件 hive -f hive_sql.sql

- 15.hive上操作hadoop⽂件基本命令


http://172.17.41.38/jobtracker.jsp

查看⽂件⼤⼩ dfs -du /user/jrjt/warehouse/tmp.db/tmp_h02_click_log/dt=2014-02-15; 删除⽂件 dfs -rm /user/jrjt/warehouse/tmp.db/tmp_h02_click_log/dt=2014-02-15;

- 16.插⼊数据sql、导出数据sql 1.insert 语法格式为： 基本的插⼊语法： INSERT OVERWRITE TABLE tablename [PARTITON(partcol1=val1,partclo2=val2)]select_statement


FROM from_statement insert overwrite table test_insert select * from test_table;

对多个表进⾏插⼊操作： FROM fromstatte

- INSERT OVERWRITE TABLE tablename1 [PARTITON(partcol1=val1,partclo2=val2)]select_statement1

- INSERT OVERWRITE TABLE tablename2 [PARTITON(partcol1=val1,partclo2=val2)]select_statement2


from test_table

- insert overwrite table test_insert1 select key

- insert overwrite table test_insert2 select value;


insert的时候，from⼦句即可以放在select ⼦句后⾯，也可以放在 insert⼦句前⾯。 hive不⽀持⽤insert语句⼀条⼀条的进⾏插⼊操作，也不⽀持update操作。数据是以load的⽅式加载到建⽴

好的表中。数据⼀旦导⼊就不可以修改。

2.通过查询将数据保存到filesystem INSERT OVERWRITE [LOCAL] DIRECTORY directory SELECT.... FROM .....

导⼊数据到本地⽬录： insert overwrite local directory ‘/home/zhangxin/hive‘ select * from test_insert1; 产⽣的⽂件会覆盖指定⽬录中的其他⽂件，即将⽬录中已经存在的⽂件进⾏删除。

导出数据到HDFS中： insert overwrite directory ‘/user/zhangxin/export_test‘ select value from test_table;

同⼀个查询结果可以同时插⼊到多个表或者多个⽬录中： from test_insert1

insert overwrite local directory ‘/home/zhangxin/hive‘ select * insert overwrite directory ‘/user/zhangxin/export_test‘ select value;

- 17.mapjoin的使⽤ 应⽤场景：1.关联操作中有⼀张表⾮常⼩ 2.不等值的链接操作 select /*+ mapjoin(A)*/ f.a,f.b from A t join B f on ( f.a=t.a and f.ftime=20110802)

- 18.perl启动任务 perl /home/jrjt/dwetl/APP/APP/A_H02_CLICK_LOG_CREDIT_USER/bin/a_h02_click_log_credit_user.pl APP_A_H02_CLICK_LOG_CREDIT_USER_20140215.dir >&

/home/jrjt/dwetl/LOG/APP/20140306/a_h02_click_log_credit_user.pl.4.log

- 19.查看perl进程 ps -ef|grep perl

- 20.hive命令移动表数据到另外⼀张表⽬录下并添加分区 dfs -cp /user/jrjt/warehouse/tmp.db/tmp_h02_click_log/dt=2014-02-18

/user/jrjt/warehouse/ods.db/o_h02_click_log/; dfs -cp /user/jrjt/warehouse/tmp.db/tmp_h02_click_log_baitiao/* /user/jrjt/warehouse/dw.db/d_h02_click_log_baitiao_basic_d_fact/;--复制所有分区数据

alter table d_h02_click_log_baitiao_basic_d_fact add partition(dt=‘2014-03-11‘) location ‘/user/jrjt/warehouse/dw.db/d_h02_click_log_baitiao_basic_d_fact/dt=2014-03-11‘;

- 21.导出⽩条数据 hive -e "select day_id,pv,uv,ip_count,click_next_count,second_bounce_rate,return_visit,pg_type from

tmp.tmp_h02_click_log_baitiao_ag_sum where day_id like ‘2014-03%‘;"> /home/jrjt/testan/baitiao.xlsx;

- 22.hive修改表名 ALTER TABLE o_h02_click_log_i RENAME TO o_h02_click_log_i_bk;

- 23.hive复制表结构 CREATE TABLE d_h02_click_log_baitiao_ag_sum LIKE tmp.tmp_h02_click_log_baitiao_ag_sum;

- 24.hive官⽹⽹址

- 25.hive添加字段


https://cwiki.apache.org/conflue ... ionandConfiguration http://www.360doc.com/content/12/0111/11/7362_178698714.shtml

alter table tmp_h02_click_log_baitiao_ag_sum add columns(current_session_timelenth_count bigint comment ‘⻚⾯停留总时⻓‘);

ALTER TABLE tmp_h02_click_log_baitiao CHANGE current_session_timelenth current_session_timelenth bigint comment ‘当前会话停留时间‘;

- 26.hive开启简单模式不启⽤mr set hive.fetch.task.conversion=more;

- 27.以json格式输出执⾏语句会读取的input table和input partition信息 Explain dependency query


以上就是本⽂的全部内容，希望对⼤家的学习有所帮助，版权归原作者或者来源机构所有，感谢作者，如果 未能解决你的问题，请参考以下⽂章。 pyspark读取textfile形成DataFrame以及查询表的属性信息 Hive笔记整理（⼆）(示例代码) 查看hive中某个表中的数据、表结构及所在路径

mysql 查询表结构(示例代码) mysql 查询表结构(示例代码)

