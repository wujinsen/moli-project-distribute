# 1. 背景

CTAS就是create table as select的简称。 最近在使⽤SparkSQL来进⾏快速的⾃定义SQL分析，因为需要把分析的结果保存下来，所以⼀定要使⽤ CTAS功能，然⽽在使⽤的时候发现了⼀个bug，当然这个bug已经被报告了，状态依然是unresolved。 如果有下⾯⼏个标题的，⼀般和该问题关系密切

Thrift Server - CTAS fail with Unable to move source Replace hive.default.fileformat by spark.sql.default.fileformat Spark sql 2.1.1 thrift server - unable to move source hdfs to target SparkSQL cli throws exception when using with Hive 0.12 metastore in spark-1.5.0 version

# 2. 问题发现过程

- 2.1问题发现


最开始的我们使⽤beline登录

beeline -u jdbc:hive2://xxx.xxx.xxx.xxx:10000 -n user_name

然后使⽤⼀个⽤户

use test;

查看⼀下表

show tables;

+-----------+-------------------------+--------------+--+ | database | tableName | isTemporary |

+-----------+-------------------------+--------------+--+ | test | test | false | | test | test2 | false | +-----------+-------------------------+--------------+--+

然后我们drop掉该表

drop table test;

然后再创建

create table test as select * from test2;

创建成功，反复⼏次，也没有问题，⼀开始我以为好了，我们可以放⼼使⽤了，好开⼼！！ 但是不放⼼，多做⼏轮 。于是，我重新使⽤了我的 的代码执⾏了这个语句，报错了，这个让我很疑 惑，明明什么都⼀样。 错误如下：

测试 Java

Eror: org.apache. .sql.AnalysisException: org.apache. . .ql.metadata.HiveException: Unable to move source hdfs:/ns1/tmp/ /spark-test_hive_2017-07-12_15-3847_540_4854595148769740436-6/-ext-1 0/part- 0 to destination hdfs:/ns1/user/test/hive/test/part- 0; (state=,code=0)

Spark HadopHive hive

- 2.2问题重现


刚开始打算看看是不是那个地⽅配置问题，⼼想再⽤beline试试，于是重新使⽤beline进⾏JDBC连接。 上来没有drop表，直接使⽤CTAS试试，发现直接报上⾯的错误。 然后使⽤sparkSQL试试，发现报 OM错误，以为是SparkSQL启动不合理，重新调整了⼀个配置，再启动， 然后再⽤beline连接，drop后建表，好了，试验了三次，以为该问题解决，再次⾮常开⼼！！！ 但是问题重⼤，仍不放⼼，因为感觉不是 OM问题，因为毕竟查了⼀些资料，都是指向⽂件系统关闭的问 题。 这次重启直接让我们发现了这个bug。 于是再次使⽤jdbc连接，这次果然报错了！原来JDBC只有第⼀次连接，可以反复drop反复创建，第⼆次再连 接就报错。这个过程我们重现了若⼲次，已经⾮常确信。 于是继续在⽹上搜索资料。

# 3. 尝试解决问题

## 3.1⽹上建议1

第⼀个参考是这个：

htps:/stackoverflow.com/questions/ 423523/spark-sql-2-1-1-thrift-server-unable-to-move-source-h dfs-to-target

这个回答如下：

Try setting hive.exec.staging-dir in your hive-site.xml like this: <property>

<name>hive.exec.stagingdir</name> <value>/tmp/hive/spark-${user.name}</value>

</property>

This worked for a customer who upgraded from 1.6.2 to 2.1.1 and who had that same problem with CTAS. On our dev cluster, doing this got us past your particular eror, but we stil have some HDFS permision isues we are working through.

然⽽，试验过发现这个配置没有任何⽤途 ，SparkSQL根本不读这个配置。这个答案的来源最早应该是源于 下⾯的SparkSQL cli throws exception when using with Hive 0.12 metastore in spark-1.5.0 version。 其实如果细⼼⼀点，会发现这个问题中包含了⼏个回答，和后⾯的建议直接相关，

第⼀个回答后的回复和建议2是⼀样的。 第⼆个回答则和我们的结论直接⼀致，最开始看的时候其实并没有明⽩，因为问题还不太清晰。

## 3.2⽹上建议2

然后继续搜索，发现标题如下的JIRA列表

Thrift Server - CTAS fail with Unable to move source 地址如下：

htps:/isues.apache.org/jira/browse/SPARK-21067

描述如下： Description

After upgrading our Thrift cluster to 2.1.1, we ran into an isue where CTAS would fail, sometimes… Most of the time, the CTAS would work only once, after starting the thrift server. After that, droping the table and re-isuing the same CTAS would fail with the folowing mesage (Sometime, it fails right away, sometime it work for a long period of time):

这个就和我的问题是⼀模⼀样了，Spark版本⼀样，问题症状⼀样，报的错误也⼀样。 刚开始的回答和上⾯的⼀样，其实是没⽤的，后⾯有⼀句话：

We are either l oking for a fix or for a property to set hive.default.fileformat in Spark 2 to have it use parquet instead of textfile, since the isue is not present when the fileformat is set to “parquet”.

他们说Parquet格式没问题，于是我把⽂件类型设置为Parquet，但是仍然没有⽤，设置的命令如下：

set spark.sql.default.fileformat=Parquet;

由于该问题仍然属于Open状态，可以肯定的是这个bug仍然没有修复。

3.3组合⽅案

更改⽂件类型为Parquet、包括更改staging⽬录，两者的组合也试验过（在hive-site.xml⾥⾯也配置了）

set hive.exec.stagingdir=/tmp/hive/spark-test;

结果都是⼀样。 这个Parquet设置，在 ⾥⾯也提到过，标题是： Replace hive.default.fileformat by spark.sql.default.fileformat 这个⾥⾯提出了在hive-site.xml⾥⾯配置staging⽬录是⽆效的，因为根本不会去读。 在下⾯这个地⽅，对这个问题也有讨论

htps:/isues.apache.org/jira/browse/SPARK-16825

htps:/github.com/apache/spark/pul/1430

# 4解决⽅案

在⼀个客户端上将Spark版本回退到Spark1.5.x，问题解决，另外⼀个客户端使⽤Spark2.1.1，继续使⽤ 的Spark MLlib。

机器 学习

# 5最后结论

Spark2.1.1以及后续的版本在CTAS问题上存在严重bug，暂未修复，⽆法使⽤，此处为坑，慎重。

