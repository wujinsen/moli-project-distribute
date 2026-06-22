htps:/ w.jianshu.com/p/ebc96cf0cabe

# ⼀、背景

随着mongo中数据量越来越⼤，全量同步到数仓，已不太现实，考虑增量同步的⽅式，我们在探索增 量同步的过程中，⽅案不断在改进优化，这⾥记录⼀下我们mongo增量同步的变迁史吧

# ⼆、⽅案⼀，通过BSON⽂件映射到临时表,然后 insert overwrite到正式表

具体思路是：⾸先针对存量数据，通过mongodump，dump⼀份完整的bson⽂件，put到HDFS，然后 建⼀个原始表映射到对应的bson⽂件，然后通过insert overwrite table final_table select * from origin_table转存到正式表，顺便还可以建分区，样例脚本如下：

前提：

- 1. mongodb需要下载tol: mongodb-database-tols-macos-x86_64-10.3.1
- 2. 需要下载三个包，选择合适的版本号 mongo-hadoop-core-1.5.1.jar mongo-hadoop-hive-1.5.1.jar mongo-java-driver-3.2.1.jar


add jar /home/hadoop/opt/hive/lib/mongo-hadoop-core-1.5.1.jar;

- 1、先从mongo dump⼀份bson⽂件到本地 mongodump --host $host --port $port --username=$username --password=$password --collection


$coll --db $db --out ${localPath}/ --authenticationDatabase=$db

mongodump -h 127.0.0.1 27017 -d my_db -c user -o /Users/jinsenwu/mongoDumptest mongodump -h 127.0.0.1 27017 -d my_db -c staf_crowd_group -o /Users/jinsenwu/mongoDumptest

mongoexport -d my_db -c user -o /Users/jinsenwu/user.dat mongoexport -d my_db -c a -o /Users/jinsenwu/a.dat

mongoexport --port 27030 -u sa -p Expressin@0618 -d mapdb -c bike -f bikeId,lat,lng,current_time,source --type=json -o bike.csv --query='{"source":"ofo"}'

mongoexport --port 27017 -d my_db-c staff_crowd_group --type=json -o staff_crowd_group.json mongoexport -h 192.168.32.170:17017 -d yzl-c staff_crowd_group -u yzl -p Yzl12345#--type=json

-o staff_crowd_group.json

- 2、然后从本地put到HDFS hadoop fs -put ${localPath}/$db/$coll.bson $hdfsPath/$db/$coll/ hadop fs -put /Users/jinsenwu/mongoDumptest/my_db/user.bson /user/jinsenwu/mongoDumptest/ hadop fs -put /Users/jinsenwu/mongoDumptest/my_db/staf_crowd_group.bson /user/jinsenwu/mongoDumptest2/
- 3、然后在hive中建表，跟HDFS上的bson做映射 CREATE external TABLE if not exists table_origin( `_id` string, `batch` string, `content` string, `createtime` timestamp, `mobile` string, `type` string, `updatetime` timestamp ) comment '注释' row format serde 'com.mongodb.hadoop.hive.BSONSerDe' stored as inputformat 'com.mongodb.hadoop.mapred.BSONFileInputFormat' outputformat 'com.mongodb.hadoop.hive.output.HiveBSONFileOutputFormat' location 'HDFS上bson⽂件所在⽬录';
- 4、创建正式表，指定存储⽬录


<table>
  <tr>
    <th>create external table a (<br><br>id string, name string<br><br>) ROW FORMAT SERDE "com.mongodb.ha op.hive.BSONSerDe" STORED AS INPUTFORMAT "com.mongodb.hadop.mapred.BSONFileInputFormat" OUTPUTFORMAT "com.mongodb.hadop.hive.output.HiveBSONFileOutputFormat"</th>
  </tr>
</table>


location '/user/jinsenwu/mongoDumptest/';

CREATE TABLE if not exists table( `_id` string, `batch` string, `content` string, `createtime` timestamp, `mobile` string, `type` string, `updatetime` timestamp, ) comment '注释' partitioned by (pyear int,pmonth int,pday int) ROW FORMAT SERDE 'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe' STORED AS INPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat' OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat' LOCATION '/user/hive/warehouse/mongodb/data/sdk_pro/table' TBLPROPERTIES ( 'orc.compress'='snappy');

- 5、insert overwrite到正式表 INSERT INTO TABLE table PARTITION (pyear,pmonth,pday) SELECT t.`_id` , t.`batch` , t.`content` , t.`createtime` , t.`mobile` , t.`type` , t.`updatetime` , year(t.createTime) pyear,month(t.createTime) pmonth,day(t.createTime) pday from table_origin t;
- 6、对于增量数据，根据条件dump增量数据，put到HDFS，建临时表（脚本同上），关键的不同点在 于合并增量数据，合并脚本如下：


--合并数据到总表 with t_delta as (SELECT t.*,year(t.createAt) pyear,month(t.createAt) pmonth,day(t.createAt) pday from ${table_name} t), t_base as (select b.* from sdk_call_nxcloud_voice_sms b where b.pyear =${pt_year} and b.pmonth = ${pt_month} and b.pday = ${pt_day}) INSERT OVERWRITE TABLE sdk_call_nxcloud_voice_sms PARTITION (pyear,pmonth,pday) select coalesce(base.id, delta.id) id, if(delta.id is NULL, base.countryCode,delta.countryCode) countryCode, if(delta.id is NULL, base.voiceType,delta.voiceType) voiceType, if(delta.id is NULL, base.messageid,delta.messageid) messageid, if(delta.id is NULL, base.thirdNotifyState,delta.thirdNotifyState) thirdNotifyState, if(delta.id is NULL, base.firstData,delta.firstData) firstData, if(delta.id is NULL, base.secondData,delta.secondData) secondData, if(delta.id is NULL, base.pyear,delta.pyear) pyear, if(delta.id is NULL, base.pmonth,delta.pmonth) pmonth, if(delta.id is NULL, base.pday,delta.pday) pday from t_base base full outer join t_delta delta on base.pyear = delta.pyear and base.pmonth = delta.pmonth and base.pday = delta.pday and base.id = delta.id;

# 三、存在的问题

此⽅案虽然简单易懂易上⼿，但是过程复杂，重复占⽤⼤量存储空间，有待改进。

作者：杨杨_f97a 链接：htps:/ w.jianshu.com/p/ebc96cf0cabe 来源：简书 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

- 1. mongodump -h 127.0.0.1 27017 -d my_db -c staf_crowd_group -o /Users/jinsenwu/mongoDumptest
- 2.


hdfs dfs -mkdir/user/jinsenwu/mongoDumptest2 hadop fs -chmod 7 /user/jinsenwu/mongoDumptest2

hadop fs -put /Users/jinsenwu/mongoDumptest/my_db/staf_crowd_group.bson /user/jinsenwu/mongoDumptest2/

- 3.


create external table a (

id string, name string,

description string, status string, stafCodeList string, personCount string, updateCode string, updateTime string, createCode string ) ROW FORMAT SERDE "com.mongodb.hadop.hive.BSONSerDe" STORED AS INPUTFORMAT "com.mongodb.hadop.mapred.BSONFileInputFormat" OUTPUTFORMAT "com.mongodb.hadop.hive.output.HiveBSONFileOutputFormat" location '/user/jinsenwu/mongoDumptest2/';

