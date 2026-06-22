htps:/ w.aboutyun.com/forum.php?mod=viewthread&tid=29812&highlight=mysql%2Bclickhous e

问题导读：

- 1、如何使⽤Flink导⼊数据？
- 2、如何使⽤Spark导⼊数据？
- 3、如何从MySQL中导⼊数据？
- 4、如何从Hive中导⼊数据？


本⽂分享主要是ClickHouse的数据导⼊⽅式，本⽂主要介绍如何使⽤Flink、Spark、Kafka、MySQL、 Hive将数据导⼊ClickHouse，具体内容包括：

使⽤Flink导⼊数据

使⽤Spark导⼊数据 从Kafka中导⼊数据 从MySQL中导⼊数据

从Hive中导⼊数据

使⽤Flink导⼊数据

本⽂介绍使⽤ flink-jdbc将数据导⼊ClickHouse，Maven依赖为：

- 1.
- 2.
- 3.
- 4.
- 5.


<dependency> <groupId>org.apache.flink</groupId> <artifactId>flink-jdbc_${scala.binary.version}</artifactId> <version>1.10.1</version>

</dependency>

复制代码

示例

本示例使⽤Kafka conector，通过Flink将Kafka数据实时导⼊到ClickHouse

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
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.


public class FlinkSinkClickHouse {

public static void main(String[] args) throws Exception { String url = "jdbc:clickhouse://192.168.10.203:8123/default"; String user = "default"; String passwd = "hOn0d9HT"; String driver = "ru.yandex.clickhouse.ClickHouseDriver"; int batchsize = 500; // 设 置 batch size， 测 试 的 话 可 以 设 置 ⼩ ⼀ 点 ， 这 样 可 以 ⽴ 刻 看 到 数据 被 写

⼊

// 创 建 执 ⾏ 环 境 EnvironmentSettings settings = EnvironmentSettings

.newInstance()

.useBlinkPlanner() .inStreamingMode() .build();

StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, settings);

String kafkaSource11 = "" + "CREATE TABLE user_behavior ( " + " `user_id` BIGINT, -- ⽤户id\n" + " `item_id` BIGINT, -- 商品id\n" + " `cat_id` BIGINT, -- 品类id\n" + " `action` STRING, -- ⽤户⾏为\n" + " `province` INT, -- ⽤户所在的省份\n" + " `ts` BIGINT, -- ⽤户⾏为发⽣的时间戳\n" + " `proctime` AS PROCTIME(), -- 通过计算列产⽣⼀个处理时间列\n" + " `eventTime` AS TO_TIMESTAMP(FROM_UNIXTIME(ts, 'yyyy-MM-dd

HH:mm:ss')), -- 事件时间\n" + " WATERMARK FOR eventTime AS eventTime - INTERVAL '5' SECOND -- 在

eventTime上定义watermark\n" + ") WITH ( 'connector' = 'kafka', -- 使⽤ kafka connector\n" + " 'topic' = 'user_behavior', -- kafka主题\n" + " 'scan.startup.mode' = 'earliest-offset', -- 偏移量，从起始 offset 开始读

取\n" +

" 'properties.group.id' = 'group1', -- 消费者组\n" +

- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.


" 'properties.bootstrap.servers' = 'kms-2:9092,kms-3:9092,kms-4:9092',

-- kafka broker 地址\n" + " 'format' = 'json', -- 数据源格式为 json\n" + " 'json.fail-on-missing-field' = 'true',\n" + " 'json.ignore-parse-errors' = 'false'" + ")";

// Kafka Source tEnv.executeSql(kafkaSource11); String query = "SELECT user_id,item_id,cat_id,action,province,ts FROM

user_behavior"; Table table = tEnv.sqlQuery(query);

String insertIntoCkSql = "INSERT INTO behavior_mergetree(user_id,item_id,cat_id,action,province,ts)\n" + "VALUES(?,?,?,?,?,?)";

//将 数据 写 ⼊ ClickHouse Sink JDBCAppendTableSink sink = JDBCAppendTableSink

.builder()

.setDrivername(driver)

.setDBUrl(url)

.setUsername(user)

.setPassword(passwd)

.setQuery(insertIntoCkSql)

.setBatchSize(batchsize)

.setParameterTypes(Types.LONG, Types.LONG,Types.LONG, Types.STRING,Types.INT,Types.LONG)

.build();

String[] arr = {"user_id","item_id","cat_id","action","province","ts"}; TypeInformation[] type = {Types.LONG, Types.LONG,Types.LONG,

Types.STRING,Types.INT,Types.LONG};

tEnv.registerTableSink( "sink", arr, type, sink

- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.
- 76.


);

tEnv.insertInto(table, "sink");

tEnv.execute("Flink Table API to ClickHouse Example"); }

}

复制代码

Note:

由于 ClickHouse 单次插⼊的延迟⽐较⾼，我们需要设置 BatchSize 来批量插⼊数据，提⾼性 能。

在 JDBCApendTableSink 的实现中，若最后⼀批数据的数⽬不⾜ BatchSize，则不会插⼊剩 余数据。

使⽤Spark导⼊数据

本⽂主要介绍如何通过Spark程序写⼊数据到Clickhouse中。

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


<dependency> <groupId>ru.yandex.clickhouse</groupId> <artifactId>clickhouse-jdbc</artifactId> <version>0.2.4</version>

</dependency> <!-- 如 果 报 错 ： Caused by: java.lang.ClassNotFoundException: com.google.common.escape.Escapers， 则 添 加 下 ⾯ 的 依 赖 --> <dependency>

<groupId>com.google.guava</groupId> <artifactId>guava</artifactId> <version>28.0-jre</version>

</dependency>

复制代码

示例

- 1.
- 2.


object Spark2ClickHouseExample {

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
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.


val properties = new Properties() properties.put("driver", "ru.yandex.clickhouse.ClickHouseDriver") properties.put("user", "default") properties.put("password", "hOn0d9HT") properties.put("batchsize", "1000") properties.put("socket_timeout", "300000") properties.put("numPartitions", "8") properties.put("rewriteBatchedStatements", "true")

case class Person(name: String, age: Long)

private def runDatasetCreationExample(spark: SparkSession): Dataset[Person] = { import spark.implicits._ // DataFrames转 成 DataSet val path = "file:///e:/people.json" val peopleDS = spark.read.json(path) peopleDS.createOrReplaceTempView("people") val ds = spark.sql("SELECT name,age FROM people").as[Person] ds.show() ds

}

def main(args: Array[String]) {

val url = "jdbc:clickhouse://kms-1:8123/default" val table = "people"

val spark = SparkSession

.builder()

.appName("Spark Example")

.master("local") //设 置 为 本 地 运 ⾏

.getOrCreate() val ds = runDatasetCreationExample(spark)

ds.write.mode(SaveMode.Append).option(JDBCOptions.JDBC_BATCH_INSERT_SIZE, 100000).jdbc(url, table, properties)

spark.stop()

- 40.
- 41.


} }

复制代码

从Kafka中导⼊数据

主要是使⽤ClickHouse的表引擎。

使⽤⽅式

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


CREATE TABLE [IF NOT EXISTS] [db.]table_name [ON CLUSTER cluster] (

- name1 [type1] [DEFAULT|MATERIALIZED|ALIAS expr1],

- name2 [type2] [DEFAULT|MATERIALIZED|ALIAS expr2],


... ) ENGINE = Kafka() SETTINGS

kafka_broker_list = 'host:port', kafka_topic_list = 'topic1,topic2,...', kafka_group_name = 'group_name', kafka_format = 'data_format'[,] [kafka_row_delimiter = 'delimiter_symbol',] [kafka_schema = '',] [kafka_num_consumers = N,] [kafka_max_block_size = 0,] [kafka_skip_broken_messages = N,] [kafka_commit_every_batch = 0,] [kafka_thread_per_consumer = 0]

kafka_broker_list ：逗号分隔的brokers地址 (localhost:9092). kafka_topic_list ：Kafka 主题列表，多个主题⽤逗号分隔. kafka_group_name ：消费者组. kafka_format – Message format. ⽐如JSONEachRow、JSON、CSV等等

复制代码

使⽤示例

在kafka中创建user_behavior主题，并向该主题写⼊数据，数据示例为：

- 1.
- 2.
- 3.


{"user_id":63401,"item_id":6244,"cat_id":143,"action":"pv","province":3,"ts":1573445919 } {"user_id":9164,"item_id":2817,"cat_id":611,"action":"fav","province":28,"ts":157342048 6}

{"user_id":63401,"item_id":6244,"cat_id":143,"action":"pv","province":3,"ts":1573445919 }

复制代码

在ClickHouse中创建表，选择表引擎为Kafka()，如下:

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
- 24.
- 25.
- 26.


CREATE TABLE kafka_user_behavior (

user_id UInt64 COMMENT '⽤户id', item_id UInt64 COMMENT '商品id', cat_id UInt16 COMMENT '品类id', action String COMMENT '⾏为',

province UInt8 COMMENT '省份id', ts UInt64 COMMENT '时间戳'

) ENGINE = Kafka() SETTINGS kafka_broker_list = 'cdh04:9092', kafka_topic_list = 'user_behavior', kafka_group_name = 'group1', kafka_format = 'JSONEachRow'

;

-- 查 询 cdh04 :) select * from kafka_user_behavior ;

-- 再 次 查 看 数据 ， 发 现 数据 为 空 cdh04 :) select count(*) from kafka_user_behavior;

SELECT count(*) FROM kafka_user_behavior

┌─count()─┐ │ 0 │ └─────────┘

复制代码

通过物化视图将kafka数据导⼊ClickHouse

当我们⼀旦查询完毕之后，ClickHouse会删除表内的数据，其实Kafka表引擎只是⼀个数据管道，我们 可以通过物化视图的⽅式访问Kafka中的数据。

⾸先创建⼀张Kafka表引擎的表，⽤于从Kafka中读取数据

然后再创建⼀张普通表引擎的表，⽐如MergeTre，⾯向终端⽤户使⽤

最后创建物化视图，⽤于将Kafka引擎表实时同步到终端⽤户所使⽤的表中

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
- 24.
- 25.
- 26.
- 27.


-- 创 建 Kafka引 擎 表

CREATE TABLE kafka_user_behavior_src (

user_id UInt64 COMMENT '⽤户id', item_id UInt64 COMMENT '商品id', cat_id UInt16 COMMENT '品类id', action String COMMENT '⾏为',

province UInt8 COMMENT '省份id', ts UInt64 COMMENT '时间戳'

) ENGINE = Kafka() SETTINGS kafka_broker_list = 'cdh04:9092', kafka_topic_list = 'user_behavior', kafka_group_name = 'group1', kafka_format = 'JSONEachRow'

;

-- 创 建 ⼀ 张 终 端 ⽤ 户 使 ⽤ 的 表

CREATE TABLE kafka_user_behavior ( user_id UInt64 COMMENT '⽤户id', item_id UInt64 COMMENT '商品id', cat_id UInt16 COMMENT '品类id', action String COMMENT '⾏为',

province UInt8 COMMENT '省份id', ts UInt64 COMMENT '时间戳'

) ENGINE = MergeTree()

ORDER BY user_id ;

- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.


-- 创 建 物 化 视 图 ， 同 步 数据 CREATE MATERIALIZED VIEW user_behavior_consumer TO kafka_user_behavior

AS SELECT * FROM kafka_user_behavior_src ;

-- 查 询 ， 多 次 查 询 ， 已 经 被 查 询 的 数据 依 然 会 被 输 出 cdh04 :) select * from kafka_user_behavior;

Note:

Kafka消费表不能直接作为结果表使⽤。Kafka消费表只是⽤来消费Kafka数据，没有真正的存储所有数据。

复制代码

从MySQL中导⼊数据

同kafka中导⼊数据类似，ClickHouse同样⽀持MySQL表引擎，即映射⼀张MySQL中的表到 ClickHouse中。 数据类型对应关系

MySQL中数据类型与ClickHouse类型映射关系如下表。

使⽤⽅式

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


CREATE TABLE [IF NOT EXISTS] [db.]table_name [ON CLUSTER cluster] (

- name1 [type1] [DEFAULT|MATERIALIZED|ALIAS expr1] [TTL expr1],

- name2 [type2] [DEFAULT|MATERIALIZED|ALIAS expr2] [TTL expr2],


... ) ENGINE = MySQL('host:port', 'database', 'table', 'user', 'password'[, replace_query, 'on_duplicate_clause']);

复制代码

使⽤示例

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


-- 连 接 MySQL中 clickhouse数据 库 的 test表 CREATE TABLE mysql_users(

id Int32, name String

) ENGINE = MySQL( '192.168.10.203:3306',

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
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.


'clickhouse', 'users', 'root', '123qwe');

-- 查 询 数据 cdh04 :) SELECT * FROM mysql_users;

SELECT * FROM mysql_users

┌─id─┬─name──┐

- │ 1 │ tom │

- │ 2 │ jack │

- │ 3 │ lihua │ └────┴───────┘


-- 插 ⼊ 数据 ， 会 将 数据插 ⼊ MySQL对 应 的 表 中

-- 所 以 当 查 询 MySQL数据 时 ， 会 发 现 新 增 了 ⼀ 条 数据 INSERT INTO users VALUES(4,'robin');

-- 再 次 查 询 cdh04 :) select * from mysql_users;

SELECT * FROM mysql_users

┌─id─┬─name──┐

- │ 1 │ tom │

- │ 2 │ jack │

- │ 3 │ lihua │

- │ 4 │ robin │ └────┴───────┘


复制代码

注意：对于MySQL表引擎，不⽀持UPDATE和DELETE操作，⽐如执⾏下⾯命令时，会报错：

- 1.
- 2.
- 3.
- 4.
- 5.


-- 执 ⾏ 更 新 ALTER TABLE mysql_users UPDATE name = 'hanmeimei' WHERE id = 1;

-- 执 ⾏ 删 除 ALTER TABLE mysql_users DELETE WHERE id = 1;

-- 报 错

6.

DB::Exception: Mutations are not supported by storage MySQL.

复制代码

从Hive中导⼊数据

本⽂使⽤Waterdrop进⾏数据导⼊，Waterdrop是⼀个⾮常易⽤，⾼性能，能够应对海量数据的实时数 据处理产品，它构建在Spark之上。Waterdrop拥有着⾮常丰富的插件，⽀持从Kafka、HDFS、Kudu中 读取数据，进⾏各种各样的数据处理，并将结果写⼊ClickHouse、Elasticsearch或者Kafka中。

我们仅需要编写⼀个Waterdrop Pipeline的配置⽂件即可完成数据的导⼊。配置⽂件包括四个部分，分 别是Spark、Input、filter和Output。

关于Waterdrop的安装，⼗分简单，只需要下载ZIP⽂件，解压即可。使⽤Waterdrop需要安装Spark。

在Waterdrop安装⽬录的config/⽂件夹下创建配置⽂件：hive_table_batch.conf，内容如下。主要 包括四部分：Spark、Input、filter和Output。

Spark部分是Spark的相关配置，主要配置Spark执⾏时所需的资源⼤⼩。

Input部分是定义数据源，其中pre_sql是从Hive中读取数据SQL，table_name是将读取后的数 据，注册成为Spark中临时表的表名，可为任意字段。 filter部分配置⼀系列的转化，⽐如过滤字段

Output部分是将处理好的结构化数据写⼊ClickHouse，ClickHouse的连接配置。

需要注意的是，必须保证hive的metastore是在服务状态。

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


spark { spark.app.name = "Waterdrop_Hive2ClickHouse" spark.executor.instances = 2 spark.executor.cores = 1 spark.executor.memory = "1g" // 这 个 配 置 必 需 填 写 spark.sql.catalogImplementation = "hive"

} input {

hive { pre_sql = "select * from default.users" table_name = "hive_users"

}

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
- 24.
- 25.


} filter {} output {

clickhouse { host = "kms-1:8123" database = "default" table = "users" fields = ["id", "name"] username = "default" password = "hOn0d9HT"

} }

复制代码

执⾏任务

1.

[kms@kms-1 waterdrop-1.5.1]$ bin/start-waterdrop.sh --config config/hive_table_batch.conf --master yarn --deploy-mode cluster

复制代码

这样就会启动⼀个Spark作业执⾏数据的抽取，等执⾏完成之后，查看ClickHouse的数据。

总结

本⽂主要介绍了如何通过Flink、Spark、Kafka、MySQL以及Hive，将数据导⼊到ClickHouse，对每⼀ 种⽅式都出了详细的示例，希望对你有所帮。

作者：⻄⻉ 来源：

htps:/mp.weixin.q.com/s/SOX_4JlcLYk7wHPNDLJ1Rg

