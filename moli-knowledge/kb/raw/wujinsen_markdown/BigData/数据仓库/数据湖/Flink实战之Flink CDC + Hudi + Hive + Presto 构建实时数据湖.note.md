## htps:/mp.weixin.q.com/s/5kv2KNI5-_ZazSc5J1STkQ

摘要：本⽂作者罗⻰⽂，分享了如何通过 Flink CDC、Hudi、Hive、Presto 等构建数据湖。主要内容包括：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


测试过程环境版本说明 集群服务器基础环境 Hudi 编译环境配置 Flink 环境配置 启动 Flink Yarn Sesion 服务 MySQL binlog 开启配置 Flink CDC sink Hudi 测试代码过程

Tips：点击「阅读原⽂」预约 FA 2021～

# ⼀、测试过程环境版本说明

Flink 1.13.1 Scala 2.1 CDH 6.2.0 Hadop 3.0.0 Hive 2.1.1 Hudi 0.10(master) PrestoDB 0.256 Mysql 5.7

# ⼆、集群服务器基础环境

- 2.1 Maven 和 JDK 环境版本


![image 1](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile1.png>)

- 2.2 Hadop 集群环境版本

- 2.3 HADOP环境变量配置


![image 2](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile2.png>)

export HADOOP_HOME=/opt/cloudera/parcels/CDH/lib/hadoopexport HADOOP_CALSSPATH=`$HADOOP_HOME/bin/hadoop classpath`

# 三、Hudi 编译环境配置

- 3.1 Maven Home setings.xml 配置修改

说明：指定 aliyun maven 地址 (⽀持 CDH cloudera 依赖) miror 库

<mirrors><mirror><id>alimaven</id><mirrorOf>central,!cloudera</mirrorOf><name>aliyun maven</name><url>http://maven.aliyun.com/nexus/content/groups/public/</url></mirror> </mirrors>

- 3.2 下载 Hudi 源码包


![image 3](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile3.png>)

git clone https://github.com/apache/hudi.git

![image 4](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile4.png>)

Hudi 社区建议版本适配

- Hudi0.9 适配 Flink 1.12.2

- Hudi0.10(master) 适配 Flink 1.13.X (说明 master 分⽀上版本还未 release)


- 3.3 Hudi 客户端命令⾏

- 3.4 修改 Hudi 集成 Flink 和 Hive 编译依赖版本配置


![image 5](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile5.png>)

hudi-master/packaging/hudi-flink-bundle

![image 6](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile6.png>)

pom.xml ⽂件 (笔者环境 CDH 6.2.0，Hive 2.1.1)

![image 7](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile7.png>)

<profile><id>flink-bundle-shade-hive2</id><properties><hive.version>2.1.1cdh6.2.0</hive.version><flink.bundle.hive.scope>compile</flink.bundle.hive.scope> </properties><dependencies><dependency><groupId>${hive.groupid}</groupId><artifactId>hiveservice-rpc</artifactId><version>${hive.version}</version><scope>${flink.bundle.hive.scope} </scope></dependency></dependencies></profile>

- 3.5 编译 Hudi 指定 Hadop 和 Hive 版本信息


mvn clean install -DskipTests -Drat.skip=true -Dscala-2.11 -Dhadoop.version=3.0.0 -Pflinkbundle-shade-hive2

(可加 –e –X 参数查看编译 EROR 异常和 DEBUG 信息)

说明：默认 Scala 2.1、默认不包含 Hive 依赖

![image 8](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile8.png>)

⾸次编译耗时较⻓ 笔者⾸次编译⼤概花费 50min+ (也和服务器⽹络有关)

后续编译会快⼀些 ⼤约 15min 左右

- 3.6 Hudi 编译异常


![image 9](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile9.png>)

![image 10](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile10.png>)

- 修改 Hudi master pom.xml 增加 CDH repository 地址
- 3.7 Hudi 重新编译


![image 11](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile11.png>)

![image 12](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile12.png>)

- 3.8 Hudi 编译结果说明


hudi-master/packaging/hudi-flink-bundle/target

![image 13](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile13.png>)

hudi-flink-bundle_2.1-0.10.0-SNAPSHOT.jar

说明：hudi-flink-bundle jar 是 Flink ⽤来写⼊和读取数据

hudi-master/packaging/hudi-hadop-mr-bundle/target

![image 14](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile14.png>)

hudi-hadop-mr-bundle-0.10.0-SNAPSHOT.jar

说明：hudi-mr-bundle jar 是 Hive 需要⽤来读 Hudi 数据

# 四、Flink 环境配置

版本说明：Flink 1.13.1，Scala 2.1 版本

- 4.1 FLINK_HOME 下 sql-client-defaults.yaml 配置

- 4.2 flink-conf.yaml 配置修改


![image 15](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile15.png>)

![image 16](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile16.png>)

![image 17](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile17.png>)

# state.backend: filesystemstate.backend: rocksdb# 开 启 增 量 checkpointstate.backend.incremental: true# state.checkpoints.dir: hdfs://namenodehost:port/flink-checkpointsstate.checkpoints.dir: hdfs://nameservice/flink/flinkcheckpointsclassloader.check-leaked-classloader: falseclassloader.resolve-order: parentfirst

- 4.3 FLINK_HOME lib下添加依赖


![image 18](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile18.png>)

flink-sql-connector-mysql-cdc-1.4.0.jarflink-sql-connector-oracle-cdc-2.1-SNAPSHOT.jar.BAK – oracle cdc 依赖 flink-format-changelog-json-1.4.0.jarflink-sql-connector-kafka_2.111.13.1.jar--- Hadoop home lib下copy过来hadoop-mapreduce-client-common-3.0.0cdh6.2.0.jarhadoop-mapreduce-client-core-3.0.0-cdh6.2.0.jarhadoop-mapreduce-clientjobclient-3.0.0-cdh6.2.0.jar--- hudi编译jar copy过来hudi-flink-bundle_2.11-0.10.0SNAPSHOT.jar

说明：⽬前 oracle cdc jar 和 mysql cdc jar ⼀起在 lib 下发现有冲突异常

# 五、启动 Flink Yarn Sesion 服务

- 5.1 FLINK_HOME shel 命令

$FLINK_HOME/bin/yarn-session.sh -s 2-jm 2048-tm 2048-nm ys-hudi01 -d

- 5.2 Yarn Web UI


![image 19](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile19.png>)

![image 20](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile20.png>)

- 5.3 Flinksql Client 启动命令


$FLINK_HOME/bin/sql-client.sh embedded -j ./lib/hudi-flink-bundle_2.11-0.10.0-SNAPSHOT.jar shell

说明：-j 指定 hudi-flink 依赖 jar

![image 21](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile21.png>)

Show table / show catalogs

![image 22](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile22.png>)

# 六、MySQL binlog 开启配置

- 6.1 创建 binlog ⽇志存储路径

mkdir logs

- 6.2 修改⽬录属主和 group

chown -R mysql:mysql /mysqldata/logs

- 6.3 修改 mysql 配置信息

vim /etc/my.cnfserver-id=2log-bin= /mysqldata/logs/mysqlbinbinlog_format=rowexpire_logs_days=15binlog_row_image=full

- 6.4 修改完，重启 mysql server

service mysqld restart

- 6.5 客户端查看 binlog ⽇志情况


show master logs;

![image 23](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile23.png>)

Mysql 版本：5.7.30

![image 24](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile24.png>)

- 6.6 创建 mysql sources 表 DL


create table users_cdc( id bigint auto_increment primary key, name varchar(20) null, birthday timestamp default CURRENT_TIMESTAMP notnull, ts timestamp default CURRENT_TIMESTAMP notnull);

![image 25](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile25.png>)

# 七、Flink CDC sink Hudi 测试代码过程

- 7.1 Flink sql cdc DL 语句：(具体参数说明可参考 Flink 官⽹)


CREATE TABLE mysql_users ( id BIGINT PRIMARY KEY NOT ENFORCED , name STRING, birthday TIMESTAMP(3), ts TIMESTAMP(3)) WITH ('connector'= 'mysql-cdc','hostname'= '127.0.0.1','port'= '3306','username'= '','password'=’’,'server-time-zone'= 'Asia/Shanghai','debezium.snapshot.mode'='initial','database-name'= 'luo','table-name'= 'users_cdc');

![image 26](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile26.png>)

- 7.2 查询 mysql cdc 表


Flink SQL> select * from mysql_users;

![image 27](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile27.png>)

由于⽬前 MySQL users_cdc 表是空，所以 flinksql 查询没有数据 只有表结构；

![image 28](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile28.png>)

Flink web UI：

![image 29](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile29.png>)

![image 30](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile30.png>)

- 7.3 创建⼀个临时视图，增加分区列⽅便后续同步 Hive 分区表


Flink SQL> create view mycdc_v AS SELECT *, DATE_FORMAT(birthday, 'yyyyMMdd') as partition FROM mysql_users;

说明：partition 关键字需要 ` 引起来

![image 31](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile31.png>)

查询视图数据也是空结构，但增加了分区字段：

Flink SQL> select * from mycdc_v;

![image 32](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile32.png>)

![image 33](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile33.png>)

Flink web UI：

![image 34](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile34.png>)

- 7.4 设置 checkpoint 间隔时间，存储路径已在 flink-conf 配置设置全局路径

建议:测试环境 可设置秒级别（不能太⼩），⽣产环境可设置分钟级别。

Flink SQL> set execution.checkpointing.interval=30sec;

- 7.5 Flinksql 创建 cdc sink hudi ⽂件，并⾃动同步 Hive 分区表 DL 语句


![image 35](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile35.png>)

CREATE TABLE mysqlcdc_sync_hive01(id bigint ,name string,birthday TIMESTAMP(3),ts TIMESTAMP(3),`partition` VARCHAR(20),primary key(id) not enforced --必 须 指 定 uuid 主 键 )PARTITIONED BY (`partition`)with('connector'='hudi','path'= 'hdfs://nameservice

/luo/hudi/mysqlcdc_sync_hive01', 'hoodie.datasource.write.recordkey.field'= 'id'-- 主 键 , 'write.precombine.field'= 'ts'-- ⾃ 动 precombine的 字 段 , 'write.tasks'= '1', 'compaction.tasks'= '1', 'write.rate.limit'= '2000'-- 限 速 , 'table.type'= 'MERGE_ON_READ'-默 认 COPY_ON_WRITE,可 选 MERGE_ON_READ , 'compaction.async.enabled'= 'true'-- 是 否 开 启 异 步 压 缩 , 'compaction.trigger.strategy'= 'num_commits'-- 按 次 数 压 缩 , 'compaction.delta_commits'= '1'-默 认 为 5, 'changelog.enabled'= 'true'-- 开 启 changelog变 更 , 'read.streaming.enabled'= 'true'-开 启 流 读 , 'read.streaming.check-interval'= '3'-- 检 查 间隔 ， 默 认 60s, 'hive_sync.enable'= 'true'-

- 开 启 ⾃ 动 同 步 hive, 'hive_sync.mode'= 'hms'-- ⾃ 动 同 步 hive模 式 ， 默 认 jdbc模 式 , 'hive_sync.metastore.uris'= 'thrift://hadoop:9083'-- hive metastore地址 -- , 'hive_sync.jdbc_url'= 'jdbc:hive2://hadoop:10000'-- hiveServer地址 , 'hive_sync.table'= 'mysqlcdc_sync_hive01'-- hive 新 建 表 名 , 'hive_sync.db'= 'luo'-- hive 新 建 数据 库 名 , 'hive_sync.username'= ''-- HMS ⽤ 户 名 , 'hive_sync.password'= ''-- HMS 密 码 , 'hive_sync.support_timestamp'= 'true'-- 兼 容 hive timestamp类 型 );

说明：Hudi ⽬前⽀持 MOR 和 COW 两种模式

- 1.
- 2.


Copy on Write：使⽤列式存储来存储数据 (例如：parquet),通过在写⼊期间执⾏同步合并来简单地更新 和重现⽂件

Merge on Read：使⽤列式存储 (parquet) + ⾏式⽂件 (arvo) 组合存储数据。更新记录到增量⽂件中， 然后进⾏同步或异步压缩来⽣成新版本的列式⽂件。

COW：Copy on Write (写时复制)，快照查询 + 增量查询 MOR：Merge on Read (读时合并)，快照查询 + 增量查询 + 读取优化查询 (近实时)

使⽤场景上：

- 1.
- 2.


COW 适⽤写少读多的场景 ，MOR 适⽤写多读少的场景；

MOR 适合 CDC 场景，更新延迟要求较低，COW ⽬前不⽀持 changelog mode 不适合处理 cdc 场景；

![image 36](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile36.png>)

![image 37](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile37.png>)

### Flink web UI

![image 38](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile38.png>)

### 7.6 Flink sql mysql cdc 数据写⼊ Hudi ⽂件数据

Flink SQL> insert into mysqlcdc_sync_hive01 select id,name,birthday,ts,`partition` from mycdc_v;

![image 39](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile39.png>)

Flink web UI DAG 图：

![image 40](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile40.png>)

### 7.7 HDFS 上 Hudi ⽂件⽬录情况

![image 41](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile41.png>)

![image 42](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile42.png>)

说明：⽬前还没写⼊测试数据，Hudi ⽬录只⽣成⼀些状态标记⽂件，还未⽣成分区⽬录以及 .log 和 .parquet 数据⽂件，具体含义可⻅ Hudi 官⽅⽂档。

- 7.8 Mysql 数据源写⼊测试数据


- insert into users_cdc (name) values ('cdc01');


![image 43](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile43.png>)

### 7.9 Flinksql 查询 mysql cdc insert 数据

Flink SQL> set execution.result-mode=tableau; [WARNING] The specified key 'execution.result-mode' is deprecated. Please use 'sqlclient.execution.result-mode' instead. [INFO] Session property has been set. Flink SQL> select * from mysql_users; -- 查询到⼀条insert数据

![image 44](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile44.png>)

### 7.10 Flink web UI ⻚⾯可以看到 DAG 各个环节产⽣⼀条测试数据

![image 45](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile45.png>)

- 7.1 Flinksql 查询 sink 的 Hudi 表数据


Flink SQL> select * from mysqlcdc_sync_hive01; --已查询到⼀条insert数据

![image 46](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile46.png>)

- 7.12 Hdfs 上 Hudi ⽂件⽬录变化情况


![image 47](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile47.png>)

- 7.13 Hive 分区表和数据⾃动同步情况

- 7.14 查看⾃动创建 Hive 表结构


![image 48](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile48.png>)

hive> show create table mysqlcdc_sync_hive01_ro;

![image 49](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile49.png>)

hive> show create table mysqlcdc_sync_hive01_rt;

![image 50](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile50.png>)

- 7.15 查看⾃动⽣成的表分区信息


hive> show partitions mysqlcdc_sync_hive01_ro;hive> show partitions mysqlcdc_sync_hive01_rt;

![image 51](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile51.png>)

说明：已⾃动⽣产 Hudi MOR 模式的

mysqlcdc_sync_hive01_romysqlcdc_sync_hive01_rt

ro 表和 rt 表区别：

ro 表全称 read oprimized table，对于 MOR 表同步的 x_ro 表，只暴露压缩后的 parquet。其查询⽅式 和 COW 表类似。设置完 hiveInputFormat 之后和普通的 Hive 表⼀样查询即可；

rt 表示增量视图，主要针对增量查询的 rt 表；

ro 表只能查 parquet ⽂件数据；rt 表 parquet ⽂件数据和 log ⽂件数据都可查。

- 7.16 Hive 访问 Hudi 数据


说明：需要引⼊ hudi-hadop-mr-bundle-0.10.0-SNAPSHOT.jar

引⼊ Hudi 依赖 jar ⽅式：

- 1.
- 2.
- 3.


引⼊到 $HIVE_HOME/lib 下；

引⼊到 $HIVE_HOME/auxlib ⾃定义第三⽅依赖 修改 hive-site.xml 配置⽂件；

Hive shel 命令⾏引⼊ Sesion 级别有效；

其中（1）和（3）配置完后需要重启 hive-server 服务;

查询 Hive 分区表数据：

hive> select * from mysqlcdc_sync_hive01_ro; --已查询到mysq insert的⼀条数据

![image 52](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile52.png>)

hive> select * from mysqlcdc_sync_hive01_rt; --已查询到mysq insert的⼀条数据

![image 53](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile53.png>)

Hive 条件查询：

hive> select name,ts from mysqlcdc_sync_hive01_ro where partition='20211109';

![image 54](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile54.png>)

Hive ro 表 count 查询

hive> select count(1) from mysqlcdc_sync_hive01_ro;

![image 55](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile55.png>)

Hive Count 异常解决：

引⼊ hudi-hadop-mr-bundle-0.10.0-SNAPSHOT.jar 依赖

hive> add jar hdfs://nameservice /luo/hudi-hadoop-mr-bundle-0.10.0-SNAPSHOT.jar; hive> set hive.input.format = org.apache.hudi.hadoop.hive.HoodieCombineHiveInputFormat;

![image 56](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile56.png>)

hive> select count(1) from mysqlcdc_sync_hive01_ro; --可正常count

![image 57](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile57.png>)

Hive rt 表 count 查询

hive> select count(1) from mysqlcdc_sync_hive01_rt;

![image 58](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile58.png>)

说明：rt 表 count 还是异常，和 Hudi 社区⼈员沟通 Hudi master ⽬前还没 release 这块存在 bug 正在修复 中

具体⻅：htps:/isues.apache.org/jira/browse/HUDI-2649

- 7.17 Mysql 数据源写⼊多条测试数据


- insert into users_cdc (name) values ('cdc02');insert into users_cdc (name) values ('cdc03');insert into users_cdc (name) values ('cdc04');insert into users_cdc (name) values ('cdc05');insert into users_cdc (name) values ('cdc06');


![image 59](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile59.png>)

Flink web UI DAG 中数据链路情况：

![image 60](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile60.png>)

- 7.18 Flinksql 中新写⼊数据查询情况


![image 61](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile61.png>)

Yarn web UI aplication_1626256835287_40351[1] 资源使⽤情况

![image 62](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile62.png>)

Hdfs 上 Hudi ⽂件⽬录变化情况

![image 63](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile63.png>)

Hudi 状态⽂件说明：

- 1.
- 2.
- 3.


requested：表示⼀个动作已被安排，但尚未启动

inflight：表示当前正在执⾏操作

completed：表示在时间线上完成了操作

### Flink jobmanager log sync hive过程详细⽇志

![image 64](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile64.png>)

![image 65](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile65.png>)

![image 66](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile66.png>)

### 7.19 Mysql 数据源更新数据

update users_cdc set name = 'cdc05-bj'where id = 5;

![image 67](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile67.png>)

### 7.20 Flinksql 查询 cdc update 数据产⽣两条 binlog 数据

![image 68](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile68.png>)

说明：Flinksql 查询最终只有⼀条 +I 有效数据，且数据已更新

Flink web UI DAG 接受到两条 binlog 数据，但最终 compact 和 sink 只有⼀条有效数据

![image 69](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile69.png>)

- 7.21 MySQL 数据源 delete ⼀条数据


deletefrom users_cdc where id = 3;

![image 70](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile70.png>)

Flink Web UI job DAG 中捕获⼀条新数据：

![image 71](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile71.png>)

Flinksql changlog delete 数据变化查询

![image 72](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile72.png>)

### HDFS 上 Hudi 数据⽂件⽣成情况

![image 73](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile73.png>)

![image 74](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile74.png>)

Hudi ⽂件类型说明：

- 1.
- 2.
- 3.
- 4.
- 5.


co mits：表示将⼀批数据原⼦性写⼊表中；

cleans：清除表中不在需要的旧版本⽂件的后台活动；

delta_co mit：增量提交是指将⼀批数据原⼦性写⼊ MergeOnRead 类型的表中，其中部分或者所有数 据可以写⼊增量⽇志中；

compaction：协调 Hudi 中差异数据结构的后台活动，例如：将更新从基于⾏的⽇志⽂件变成列格式。 在内部，压缩的表现为时间轴上的特殊提交；

rolback：表示提交操作不成功且已经回滚，会删除在写⼊过程中产⽣的数据。

![image 75](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile75.png>)

说明：Hudi 分区⽂件以及 .log 和 .parquet ⽂件都已⽣成

两种⽂件区别：Hudi 会在 DFS 分布式⽂件系统上的 basepath 基本路径下组织成⽬录结构。每张对应的表都 会成多个分区，这些分区是包含该分区的数据⽂件的⽂件夹，与 Hive 的⽬录结构⾮常相似。在每个分区内， ⽂件被组织成⽂件组，⽂件 id 为唯⼀标识。每个⽂件组包含多个切⽚，其中每个切⽚包含在某个提交 / 压缩 即时时间⽣成的基本列⽂件 (parquet ⽂件)，以及⾃⽣成基本⽂件以来对基本⽂件的插⼊ / 更新的⼀组⽇志⽂ 件 (*.log)。Hudi 采⽤ MVC 设计,其中压缩操作会将⽇志和基本⽂件合并成新的⽂件⽚，清理操作会将未使 ⽤/较旧的⽂件⽚删除来回收 DFS 上的空间。

Flink 任务 checkpoint 情况：

设置 30s ⼀次

![image 76](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile76.png>)

![image 77](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile77.png>)

- 7.2 Hive shel 查询数据 update 和 delete 变化情况


hive> select * from mysqlcdc_sync_hive01_ro;

![image 78](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile78.png>)

hive> select * from mysqlcdc_sync_hive01_rt;

![image 79](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile79.png>)

- 7.23 Hudi Client 端操作 Hudi 表


进⼊ Hudi 客户端命令⾏

hudi-master/hudi-cli/hudi-cli.sh

连接 Hudi 表，查看表信息

hudi->connect --path hdfs://nameservice1/tmp/luo/hudi/mysqlcdc_sync_hive01

![image 80](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile80.png>)

查看 Hudi co mit 信息

hudi:mysqlcdc_sync_hive01->commits show --sortBy "CommitTime"

![image 81](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile81.png>)

查看 Hudi compactions 计划

hudi:mysqlcdc_sync_hive01->compactions show all

![image 82](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile82.png>)

- 7.24 PrestoDB 查询 Hive 表 Hudi 数据


版本说明：PrestoDB 0.256 DBeaver7.0.4

PrestoDB 集群配置和 Hive 集成参考 PrestoDB 官⽹

presto-server- */etc/catalog/hive.properties 配置 hive catalog

可通过 presto-cli 连接 hive metastore 开启查询，presto-cli 的设置参考 presto官⽅配置；

DBeaver 客户端查询 Hive ro 表数据：

![image 83](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile83.png>)

Hive ro 表 count 正常：

![image 84](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile84.png>)

查询 Hive rt 表数据查询异常：

![image 85](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile85.png>)

Hive rt 表 count 异常：

![image 86](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile86.png>)

### Presto Web UI:

![image 87](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile87.png>)

![image 88](<Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note_images/imageFile88.png>)

Flink Forward Asia 2021

202 年 1 ⽉ 8-9 ⽇， FA 2021 重磅开启，全球 40+ 多⾏业⼀线⼚商，80+ ⼲货议题，带来专属于开发者 的技术盛宴。

⼤会官⽹：

htps:/flink-forward.org.cn

⼤会线上观看地址 (记得预约哦)：

htps:/developer.aliyun.com/special/fa2021/live

