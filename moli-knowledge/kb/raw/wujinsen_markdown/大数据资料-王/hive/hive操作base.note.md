博客 微博 相册 收藏 留⾔ 关于我

# 使⽤Hive读取Hbase中的数据

hadop系列 HBase数据结构HadopDerbyJDBC

第⼀步，启动hadop，命令：./start-al.sh 第⼆步，启动hive，命令：

./hive-auxpath /home/dream-victor/hive-0.6.0/lib/hive_hbase-handler.jar,/home/dreamvictor/hive-0.6.0/lib/hbase-0.20.3.jar,/home/dream-victor/hive-0.6.0/lib/zokeper-3.2.2.jar hiveconf hbase.master=127.0.0.1 6 0 这⾥，-hiveconf hbase.master=指向⾃⼰在hbase-site.xml中hbase.master的值 第三步，启动hbase，命令：./start-hbase.sh 第四步，建⽴关联表，这⾥我们要查询的表在hbase中已经存在所以，使⽤CREATE EXTERNAL TABLE 来建⽴，如下：

Java代码

- 1.
- 2.
- 3.
- 4.


CREATE EXTERNAL TABLE hbase_table_2(key string, value string) STORED BY 'org.apache.hadop.hive.hbase.HBaseStorageHandler' WITH SERDEPROPERTIES ("hbase.columns.maping" = "data:1") TBLPROPERTIES("hbase.table.name" = "test");

hbase.columns.maping指向对应的列族；多列时，data:1，data:2；多列族时，data1 1,data2 1； hbase.table.name指向对应的表； hbase_table_2(key string, value string)，这个是关联表

我们看⼀下HBase中要查询的表的结构， Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


- hbase(main): 01:0> describe 'test' DESCRIPTION ENABLED

{NAME => 'test', FAMILIES => [{NAME => 'data', COMPRESION => 'NONE', true

VERSIONS => '3', TL => '2147483647', BLOCKSIZE => '6536', IN_MEMORY

=> 'false', BLOCKCACHE => 'true'}]} 1 row(s) in 0.0810 seconds

- hbase(main): 02:0>


在看⼀下表中的数据， Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


- hbase(main): 02:0> scan 'test' ROW COLUMN+CEL

- row1 column=data:1, timestamp=130847098583, value=value1

row12 column=data:1, timestamp=13084905637, value=value3

- row2 column=data:2, timestamp=13084710680, value=value2
- 3 row(s) in 0.0160 seconds


- hbase(main): 03:0>


列族：data:1、data:2两个 Key：row1、row12、row2 value：value1、value3、value2 hbase_table_2(key string, value string)中对应的test表中的row，value字段对应的是test表中的value

OK，现在可以来看看查询结果了， 我们在hive命令⾏中先查看⼀下hbase_table_2，

Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


hive> select * from hbase_table_2; OK row1 value1 row12value3 Time taken: 0.197 seconds hive>

对⽐⼀下test表中的列族为data:1的数据， Java代码

- 1.
- 2.


row1 column=data:1, timestamp=130847098583, value=value1

row12 column=data:1, timestamp=13084905637, value=value3

和查询结果相符，没问题，然后我们在hbase中在给列族data:1新增⼀条数据， Java代码

- 1.
- 2.
- 3.


- hbase(main): 03:0> put 'test','row13','data:1','value4' 0 row(s) in 0.050 seconds
- hbase(main): 04:0>


再查看hbase_table_2表， Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


hive> select * from hbase_table_2; OK row1 value1

- row12value3
- row13value4


- Time taken: 0.165 seconds hive>

hive> select * From hbase_table_2 where value='value3'; Total MapReduce jobs = 1 Launching Job 1 out of 1 Number of reduce tasks is set to 0 since there's no reduce operator Starting Job = job_2010323102_ 01, Tracking URL = htp:/localhost:5030/jobdetails.jsp? jobid=job_2010323102_ 01 Kil Comand = /home/dream-victor/hadop-0.20.2/bin/hadop job Dmapred.job.tracker=localhost:901 -kil job_2010323102_ 01 201-03-231:23:27,807 Stage-1 map = 0%, reduce = 0% 201-03-231:23:30,824 Stage-1 map = 10%, reduce = 0% 201-03-231:23: 3,854 Stage-1 map = 10%, reduce = 10% Ended Job = job_2010323102_ 01 OK row12value3

- Time taken: 1.929 seconds hive>


新增数据value4出现了，说明可以通过hbase_table_2查询hbase的test表 下⾯我们来查询⼀下test表中value值为value3的数据，

Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.


和hbase的test表对⽐⼀下， Java代码

1.

row12 column=data:1, timestamp=13084905637, value=value3

OK，这样我们就可以使⽤SQL来对hbase进⾏查询了。

以上只是在命令⾏⾥左对应的查询，我们的⽬的是使⽤JAVA代码来查询出有⽤的数据，其实这个也很 简单， ⾸先，启动Hive的命令有点变化，使⽤如下命令：

Java代码

1.

./hive-service hiveserver

这⾥我们默认使⽤嵌⼊的Derby数据库，这⾥可以在hive-site.xml⽂件中查看到：

Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


<property> <name>javax.jdo.option.ConectionURL</name> <value>jdbc:derby:;databaseName=metastore_db;create=true</value>/指定了数据库默认的

名字和地址 </property>

<property>

<name>javax.jdo.option.ConectionDriverName</name> <value>org.apache.derby.jdbc.EmbededDriver</value>

</property>

在此，数据库链接的URL可以使⽤默认的：jdbc:hive:/localhost:1 0/default 有了上⾯的准备，下⾯我们就可以使⽤JAVA代码来读取数据了，如下：

Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.


publicclas HiveTest extends TestCase {

privatestatic String driverName = "org.apache.hadop.hive.jdbc.HiveDriver"; private Conection con; privatebolean standAloneServer = true;

publicvoid testSelect() throws SQLException { Statement stmt = con.createStatement(); ResultSet res = stmt.executeQuery("select * from hbase_table_2"); bolean moreRow = res.next(); while (moreRow) {

System.out.println(res.getString(1)+","+res.getString(2); moreRow = res.next();

} }

@Overide protectedvoid setUp() throws Exception {

super.setUp(); Clas.forName(driverName); con = DriverManager.getConection(

"jdbc:hive:/localhost:1 0/default", ", "); }

24.

}

结果， Java代码

- 1.
- 2.
- 3.
- 4.


row1,value1

- row12,value3
- row13,value4
- row14,test


查看⼀下hbase中的结果， Java代码

- 1.
- 2.
- 3.
- 4.
- 5.


ROW COLUMN+CEL row1 column=data:1, timestamp=130847098583, value=value1

- row12 column=data:1, timestamp=13084905637, value=value3
- row13 column=data:1, timestamp=1308504369, value=value4
- row14 column=data:1, timestamp=13086750502, value=test


OK，完美了，不过还是希望这样的需求少⼀点，毕竟Hbase产⽣的初衷不是为了⽀持结构化查询。 分享到：

ExecutorService⽣命周期 Hive安装

|

201-03-231 24

浏览 10 5

评论(3)

分类:编程语⾔

相关推荐

评论

3 楼 2014-06-05 楼主好，我按照你的⽅法可以建表、load数据，但是在 hive中执⾏select操作就报错 Failed with exception java.io.IOException:java.lang.ClasCastException: org.apache.hadop.hbase.client.Result canot be cast to org.apache.hadop.io.Writable 请问你这种问题怎么解决，我⽤的hadop20.+hbase0.96+hive0.10 谢谢

sunyboy

2 楼 201-10-25 这个可以进⾏GROUP BY吗？ 1 楼 201-04-08 你好，能给出个hive使⽤mysql⽅⾯的例⼦吗？

siyuan

lvshuding

## 发表评论 您还没有登录,请您登录后再发表评论

