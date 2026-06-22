htps:/ w.kanzhun.com/jiaocheng/167186.html

sqoop系列-sqoop MongoDB导⼊Hive⽅案

## ⽅案

最近数据异构的项⽬时需要将MongoDB的数据导⼊Hive数据仓库中，总结了下，得出⼀下四种导⼊⽅ 案

- 1. mongoexport json⽂件导⼊
- 2. mongoexport csv⽂件导⼊
- 3. hive映射mongo库
- 4. mongodump bson 导⼊


# mongoexport json⽂件导⼊

缺点：mongo导出的json⽂件中，存在“$”符号，这在hive中⽆法识别

### ⽅案实现

准备 准备这个3个jar包，版本根据⾃⼰的hive版本选定。 json-hive-schema-1.0-jar-with-dependencies.jar json-serde-1.3.8-jar-with-dependencies.jar json-udf-1.3.8-jar-with-dependencies.jar 配置 将json-serde-1.3.8-jar-with-dependencies.jar和json-udf-1.3.8-jar-with-dependencies.jar放 到/data/cloudera/var/lib/hive下 在Hive的 hive-site.xml 的 Hive 服务⾼级配置代码段（安全阀） 中添加以下参数，以便在beeline中可 执⾏admin的操作

<property> <name>hive.server2.authorization.external.exec</name> <value>true</value>

#### </property> <property>

<name>hive.security.authorization.enabled</name> <value>false</value>

#### </property> <property>

<name>hive.aux.jars.path</name> <value>/data/cloudera/var/lib/hive</value>

#### </property>

步骤

⽣成创建表的语句：java -jar json-hive-schema-1.0-jar-with-dependencies.jar students.dat（⽂件名） students_text1（表名） 在hive命令⾏中：LOAD DATA LOCAL INPATH ‘/data/cloudera/students.dat’（数据⽂件） OVERWRITE INTO TABLE students_text1（导⼊的表） 备注 如导⼊数据时出现json格式不兼容，在创建表时可添加属性：WITH SERDEPROPERTIES (“ignore.malformed.json” = “true”); hive中⽆法⽀持$符号。 参考 https://github.com/rcongiu/Hive-JSON-Serde

# mongoexport csv⽂件导⼊

缺点：mongo命令导出时⽆法指定分隔符，默认的分隔符“,”在数组列中⽆法分辨，导⼊时数据会出现错 乱

### ⽅案实现

步骤 使⽤mongo的⼯具导出csv⽂件，csv⽂件的分隔符不能使⽤“,”，因为使⽤“,”时，导⼊数组列会识别 错； 将csv⽂件写⼊到HDFS中； 创建Hive表：

CREATE EXTERNAL TABLE table ( `_id` string,

...... text string) ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde' WITH SERDEPROPERTIES ( "separatorChar" = "\;", "quoteChar" = "'", "escapeChar" = "\\" ) STORED AS TEXTFILE LOCATION '/user/hive/';，

location不能与hdfs中的csv⽂件路径⼀样；

- - 导⼊数据：LOAD DATA INPATH ‘/user/seewo/mongo.csv’ OVERWRITE INTO TABLE table;
- - 使⽤select语句查看数据。Done！ 备注 如使⽤mongoexport命令，⽆法指定csv⽂件的分隔符，需要写js才能完成。


# hive映射mongo库

缺点： hive中⽆实体数据，如mongo数据丢失或⽆法访问，hive就⽆法使⽤

### ⽅案实现

准备 准备这个3个jar包，版本根据⾃⼰的hive版本选定。

mongo-hadoop-core-2.0.0.jar mongo-hadoop-hive-2.0.0.jar mongo-java-driver-3.4.2.jar

步骤 将mongo-hadoop-core-2.0.0.jar、mongo-hadoop-hive-2.0.0.jar和mongo-java-driver-3.4.2.jar放 到/data/cloudera/var/lib/hive下，/data/cloudera/var/lib/hive是参数hive.aux.jars.path的值； 在Hive中创建外部表，添加以下参数：

STORED BY 'com.mongodb.hadoop.hive.MongoStorageHandler' WITH SERDEPROPERTIES('mongo.columns.mapping'='{"id":"_id"}') TBLPROPERTIES('mongo.uri'='mongodb://user:password@ip:port/db.collection');

例⼦：

CREATE EXTERNAL TABLE ep_class_student_performance_detail ( `_id` string, app_key string,

...... )

STORED BY 'com.mongodb.hadoop.hive.MongoStorageHandler' WITH SERDEPROPERTIES('mongo.columns.mapping'='{"id":"_id"}') TBLPROPERTIES('mongo.uri'='mongodb://${ip}:${port}/${mongodb}.${mongo_collection}');

使⽤select语句查看数据。Done！ 备注 此⽅案基于开源项⽬mongo-hadoop：https://github.com/mongodb/mongo-hadoop

# mongodump bson 导⼊

缺点：⽬前⾮没发现缺点

### ⽅案实现

准备 准备⼀下3个jar包，版本根据hive和MongoDB版本选择。

mongo-hadoop-core-2.0.0.jar mongo-hadoop-hive-2.0.0.jar mongo-java-driver-3.4.2.jar

将mongo-hadoop-core-2.0.0.jar、mongo-hadoop-hive-2.0.0.jar和mongo-java-driver-3.4.2.jar放 到/data/cloudera/var/lib/hive下，/data/cloudera/var/lib/hive是参数hive.aux.jars.path的值 步骤 ⽣成bson⽂件：mongodump -h host -d db -c collectio -o output_file，例⼦：

mongodump -h ${ip}:${port} -d ${db} -c ${collection} -o /data/cloudera/mongodump；

将bson⽂件放⼊HDFS中：hdfs dfs -put output_file hdfs_file_path，例⼦：

hdfs dfs -put /data/cloudera/mongodump/${db}/${collection}.bson /user/hive/；

创建Hive表：

CREATE TABLE ${table_name} ( `_id` string,

...... value int) row format serde 'com.mongodb.hadoop.hive.BSONSerDe' with serdeproperties('mongo.columns.mapping'='{"id":"_id"}') stored as inputformat 'com.mongodb.hadoop.mapred.BSONFileInputFormat' outputformat 'com.mongodb.hadoop.hive.output.HiveBSONFileOutputFormat' location '/user/hive/';，

使⽤select语句查看数据。Done！

