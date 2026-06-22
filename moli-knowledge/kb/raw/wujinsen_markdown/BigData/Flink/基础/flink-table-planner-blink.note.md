htps:/blog.csdn.net/hzp 6/article/details/128656184

# FlinkTableAPI&SQL编程指南(1)

![image 1](<flink-table-planner-blink.note_images/imageFile1.png>)

⼤数据技术与数仓

3 ⼈赞同了该⽂章 Apache Flink提供了两种顶层的关系型API，分别为Table API和SQL，Flink通过Table API&SQL实现了 批流统⼀。其中Table API是⽤于Scala和Java的语⾔集成查询API，它允许以⾮常直观的⽅式组合关系 运算符（例如select，where和join）的查询。Flink SQL基于Apache Calcite 实现了标准的SQL，⽤户 可以使⽤标准的SQL处理数据集。Table API和SQL与Flink的DataStream和DataSet API紧密集成在⼀ 起，⽤户可以实现相互转化，⽐如可以将DataStream或者DataSet注册为table进⾏操作数据。值得注 意的是，Table API and SQL⽬前尚未完全完善，还在积极的开发中，所以并不是所有的算⼦操作都可 以通过其实现。

## 依赖

从Flink1.9开始，Flink为Table & SQL API提供了两种planer,分别为Blink planer和old planer，其中 old planer是在Flink1.9之前的版本使⽤。主要区别如下： 尖叫提示：对于⽣产环境，⽬前推荐使⽤old planer.

flink-table-comon: 通⽤模块，包含 Flink Planer 和 Blink Planer ⼀些共⽤的代码

flink-table-api-java: java语⾔的Table & SQL API，仅针对table(处于早期的开发阶段，不推荐使⽤)

flink-table-api-scala: scala语⾔的Table & SQL API，仅针对table(处于早期的开发阶段，不推荐使 ⽤)

flink-table-api-java-bridge: java语⾔的Table & SQL API，⽀持DataStream/DataSet API(推荐使⽤)

flink-table-api-scala-bridge: scala语⾔的Table & SQL API，⽀持DataStream/DataSet API(推荐使 ⽤)

flink-table-planer:planer 和runtime. planer为Flink1,9之前的old planer(推荐使⽤)

flink-table-planer-blink: 新的Blink planer.

flink-table-runtime-blink: 新的Blink runtime.

flink-table-uber: 将上述的API模块及old planer打成⼀个jar包，形如flink-table-*.jar，位与/lib⽬录 下

flink-table-uber-blink:将上述的API模块及Blink 模块打成⼀个jar包，形如 flink-table-blink-*.jar， 位与/lib⽬录下

## Blink planer & old planer

Blink planer和old planer有许多不同的特点，具体列举如下：

Blink planer将批处理作业看做是流处理作业的特例。所以，不⽀持Table 与DataSet之间的转换， 批处理的作业也不会被转成DataSet程序，⽽是被转为DataStream程序。

Blink planer不⽀持

BatchTableSource，使⽤的是有界的StreamTableSource。

Blink planer仅⽀持新的

Catalog，不⽀持 ExternalCatalog (已过时)。

对于FilterableTableSource的实现，两种Planer是不同的。old planer会谓词下推到

PlanerExpresion(未来会被移除)，⽽Blink planer 会谓词下推到 Expresion(表示⼀个产⽣计算结果的逻辑树)。

仅仅Blink planer⽀持key-value形式的配置，即通过Configuration进⾏参数设置。

关于PlanerConfig的实现，两种planer有所不同。

Blink planer 会将多个sink优化成⼀个DAG(仅⽀持TableEnvironment，StreamTableEnvironment 不⽀持)，old planer总是将每⼀个sink优化成⼀个新的DAG，每⼀个DAG都是相互独⽴的。

old planer不⽀持catalog统计，Blink planer⽀持catalog统计。

## Flink Table & SQL程序的pom依赖

根据使⽤的语⾔不同，可以选择下⾯的依赖，包括scala版和java版，如下：

<!-- java版 --><dependency> <groupId>org.apache.flink</groupId> <artifactId>flink-table-api-java-bridge_2.11</artifactId> <version>1.10.0</version> <scope>provided</scope></dependency><!-- scala版 --> <dependency> <groupId>org.apache.flink</groupId> <artifactId>flink-table-apiscala-bridge_2.11</artifactId> <version>1.10.0</version> <scope>provided</scope></dependency>

1

除此之外，如果需要在本地的IDE中运⾏Table API & SQL的程序，则需要添加下⾯的pom依赖：

<!-- Flink 1.9之前的old planner --><dependency> <groupId>org.apache.flink</groupId> <artifactId>flink-tableplanner_2.11</artifactId> <version>1.10.0</version> <scope>provided</scope> </dependency><!-- 新的Blink planner --><dependency> <groupId>org.apache.flink</groupId> <artifactId>flink-table-plannerblink_2.11</artifactId> <version>1.10.0</version> <scope>provided</scope> </dependency>

1

另外，如果需要实现⾃定义的格式(⽐如和kafka交互)或者⽤户⾃定义函数，需要添加如下依赖：

<dependency> <groupId>org.apache.flink</groupId> <artifactId>flink-tablecommon</artifactId> <version>1.10.0</version> <scope>provided</scope> </dependency>

1

## Table API & SQL的编程模板

所有的Table API&SQL的程序(⽆论是批处理还是流处理)都有着相同的形式，下⾯将给出通⽤的编程结 构形式：

// 创建⼀个TableEnvironment对象，指定planner、处理模式(batch、 streaming)TableEnvironment tableEnv = ...; // 创建⼀个表 tableEnv.connect(...).createTemporaryTable("table1");// 注册⼀个外部的表 tableEnv.connect(...).createTemporaryTable("outputTable");// 通过Table API的查询创 建⼀个Table 对象Table tapiResult = tableEnv.from("table1").select(...);// 通过SQL 查询的查询创建⼀个Table 对象Table sqlResult = tableEnv.sqlQuery("SELECT ... FROM table1 ... ");// 将结果写⼊TableSinktapiResult.insertInto("outputTable");// 执⾏ tableEnv.execute("java_job");

1

注意：Table API & SQL的查询可以相互集成，另外还可以在DataStream或者DataSet中使⽤Table API & SQL的API，实现DataStreams、 DataSet与Table之间的相互转换。

## 创建TableEnvironment

TableEnvironment是Table API & SQL程序的⼀个⼊⼝，主要包括如下的功能：

在内部的catalog中注册Table

注册catalog

加载可插拔模块

执⾏SQL查询

注册⽤户定义函数

DataStream 、

DataSet与Table之间的相互转换

持有对

ExecutionEnvironment 、 StreamExecutionEnvironment的引⽤ ⼀个Table必定属于⼀个具体的TableEnvironment，不可以将不同TableEnvironment的表放在⼀起使⽤ (⽐如join，union等操作)。 TableEnvironment是通过调⽤ BatchTableEnvironment.create() 或者 StreamTableEnvironment.create()的静态⽅法进⾏创建的。另外，默认两个planer的jar包都存在与 claspath下，所有需要明确指定使⽤的planer。

// **********************// FLINK 流处理查询// **********************import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;import org.apache.flink.table.api.EnvironmentSettings;import org.apache.flink.table.api.java.StreamTableEnvironment; EnvironmentSettings fsSettings = EnvironmentSettings.newInstance().useOldPlanner().inStreamingMode().build();Stre amExecutionEnvironment fsEnv = StreamExecutionEnvironment.getExecutionEnvironment();StreamTableEnvironment fsTableEnv = StreamTableEnvironment.create(fsEnv, fsSettings);//或者 TableEnvironment fsTableEnv = TableEnvironment.create(fsSettings); //

1

******************// FLINK 批处理查询// ******************import org.apache.flink.api.java.ExecutionEnvironment;import org.apache.flink.table.api.java.BatchTableEnvironment; ExecutionEnvironment fbEnv = ExecutionEnvironment.getExecutionEnvironment();BatchTableEnvironment fbTableEnv = BatchTableEnvironment.create(fbEnv); // **********************// BLINK 流处理查询// **********************import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;import org.apache.flink.table.api.EnvironmentSettings;import org.apache.flink.table.api.java.StreamTableEnvironment; StreamExecutionEnvironment bsEnv = StreamExecutionEnvironment.getExecutionEnvironment();EnvironmentSettings bsSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();St reamTableEnvironment bsTableEnv = StreamTableEnvironment.create(bsEnv, bsSettings);// 或者 TableEnvironment bsTableEnv = TableEnvironment.create(bsSettings); // ******************// BLINK 批处理查询//

******************import org.apache.flink.table.api.EnvironmentSettings;import org.apache.flink.table.api.TableEnvironment; EnvironmentSettings bbSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inBatchMode().build();TableE nvironment bbTableEnv = TableEnvironment.create(bbSettings);

## 在catalog中创建表

临时表与永久表

表可以分为临时表和永久表两种，其中永久表需要⼀个catalog(⽐如Hive的Metastore)俩维护表的元数 据信息，⼀旦永久表被创建，只要连接到该catalog就可以访问该表，只有显示删除永久表，该表才可 以被删除。临时表的⽣命周期是Flink Sesion，这些表不能够被其他的Flink Sesion访问，这些表不属 于任何的catalog或者数据库，如果与临时表相对应的数据库被删除了，该临时表也不会被删除。

创建表

虚表(Virtual Tables)

⼀个Table对象相当于SQL中的视图(虚表)，它封装了⼀个逻辑执⾏计划，可以通过⼀个catalog创建， 具体如下：

// 获取⼀个TableEnvironmentTableEnvironment tableEnv = ...; // table对象，查询的结果 集Table projTable = tableEnv.from("X").select(...);// 注册⼀个表，名称为 "projectedTable"tableEnv.createTemporaryView("projectedTable", projTable);

1

### 外部数据源表(Conector Tables)

可以把外部的数据源注册成表，⽐如可以读取MySQL数据库数据、Kafka数据等

tableEnvironment .connect(...) .withFormat(...) .withSchema(...)

1

.inAppendMode() .createTemporaryTable("MyTable")

### 扩展创建表的标识属性

表的注册总是包含三部分标识属性：catalog、数据库、表名。⽤户可以在内部设置⼀个catalog和⼀个 数据库作为当前的catalog和数据库，所以对于catalog和数据库这两个标识属性是可选的，即如果不指 定，默认使⽤的是“curent catalog”和 “curent database”。

TableEnvironment tEnv = ...;tEnv.useCatalog("custom_catalog");//设置 catalogtEnv.useDatabase("custom_database");//设置数据库Table table = ...;// 注册⼀ 个名为exampleView的视图，catalog名为custom_catalog// 数据库的名为 custom_databasetableEnv.createTemporaryView("exampleView", table); // 注册⼀个名为 exampleView的视图，catalog的名为custom_catalog// 数据库的名为 other_databasetableEnv.createTemporaryView("other_database.exampleView", table); // 注册⼀个名为'View'的视图，catalog的名称为custom_catalog// 数据库的名为 custom_database，'View'是保留关键字，需要使⽤``(反引 号)tableEnv.createTemporaryView("`View`", table); // 注册⼀个名为example.View的视 图，catalog的名为custom_catalog，// 数据库名为 custom_databasetableEnv.createTemporaryView("`example.View`", table); // 注册⼀个 名为'exampleView'的视图， catalog的名为'other_catalog'// 数据库名为other_database' tableEnv.createTemporaryView("other_catalog.other_database.exampleView", table);

1

## 查询表

Table API

Table API是⼀个集成Scala与Java语⾔的查询API，与SQL相⽐，它的查询不是⼀个标准的SQL语句， ⽽是由⼀步⼀步的操作组成的。如下展示了⼀个使⽤Table API实现⼀个简单的聚合查询。

// 获取TableEnvironmentTableEnvironment tableEnv = ...;//注册Orders表 // 查询注册的 表Table orders = tableEnv.from("Orders");// 计算操作Table revenue = orders

1

.filter("cCountry === 'FRANCE'") .groupBy("cID, cName") .select("cID, cName, revenue.sum AS revSum");

### SQL

Flink SQL依赖于Apache Calcite，其实现了标准的SQL语法，如下案例：

// 获取TableEnvironmentTableEnvironment tableEnv = ...; //注册Orders表 // 计算逻辑 同上⾯的Table APITable revenue = tableEnv.sqlQuery( "SELECT cID, cName, SUM(revenue) AS revSum " + "FROM Orders " + "WHERE cCountry = 'FRANCE' "

1

+ "GROUP BY cID, cName" ); // 注册"RevenueFrance"外部输出表// 计算结果插 ⼊"RevenueFrance"表tableEnv.sqlUpdate( "INSERT INTO RevenueFrance " + "SELECT cID, cName, SUM(revenue) AS revSum " + "FROM Orders " + "WHERE cCountry = 'FRANCE' " + "GROUP BY cID, cName" );

## 输出表

⼀个表通过将其写⼊到TableSink，然后进⾏输出。TableSink是⼀个通⽤的⽀持多种⽂件格式(CSV、 Parquet, Avro)和多种外部存储系统(JDBC, Apache HBase, Apache Casandra, Elasticsearch)以及多 种消息对列(Apache Kafka, RabitMQ)的接⼝。 批处理的表只能被写⼊到 BatchTableSink,流处理的表需要指明ApendStreamTableSink、 RetractStreamTableSink或者 UpsertStreamTableSink

// 获取TableEnvironmentTableEnvironment tableEnv = ...; // 创建输出表final Schema schema = new Schema() .field("a", DataTypes.INT()) .field("b", DataTypes.STRING()) .field("c", DataTypes.LONG()); tableEnv.connect(new FileSystem("/path/to/file")) .withFormat(new Csv().fieldDelimiter('|').deriveSchema()) .withSchema(schema)

1

.createTemporaryTable("CsvSinkTable"); // 计算结果表Table result = ...// 输出结果表 到注册的TableSinkresult.insertInto("CsvSinkTable");

## Table API & SQL底层的转换与执⾏

上⽂提到了Flink提供了两种planer，分别为old planer和Blink planer，对于不同的planer⽽⾔， Table API & SQL底层的执⾏与转换是有所不同的。

Old planer

根据是流处理作业还是批处理作业，Table API &SQL会被转换成DataStream或者DataSet程序。⼀个 查询在内部表示为⼀个逻辑查询计划，会被转换为两个阶段:

- 1.逻辑查询计划优化

- 2.转换成DataStream或者DataSet程序


上⾯的两个阶段只有下⾯的操作被执⾏时才会被执⾏：

当⼀个表被输出到TableSink时，⽐如调⽤了Table.insertInto()⽅法

当执⾏更新查询时，⽐如调⽤TableEnvironment.sqlUpdate()⽅法

当⼀个表被转换为DataStream或者DataSet时

⼀旦执⾏上述两个阶段，Table API & SQL的操作会被看做是普通的DataStream或者DataSet程序，所 以当StreamExecutionEnvironment.execute()或者ExecutionEnvironment.execute() 被调⽤时，会执⾏ 转换后的程序。

### Blink planer

⽆论是批处理作业还是流处理作业，如果使⽤的是Blink planer，底层都会被转换为DataStream程 序。在⼀个查询在内部表示为⼀个逻辑查询计划，会被转换成两个阶段：

- 1.逻辑查询计划优化

- 2.转换成DataStream程序


对于TableEnvironment and StreamTableEnvironment⽽⾔，⼀个查询的转换是不同的 ⾸先对于TableEnvironment，当TableEnvironment.execute()⽅法执⾏时，Table API & SQL的查询才 会被转换，因为TableEnvironment会将多个sink优化为⼀个DAG。 对于StreamTableEnvironment，转换发⽣的时间与old planer相同。

## 与DataStream & DataSet API集成

对于Old planer与Blink planer⽽⾔，只要是流处理的操作，都可以与DataStream API集成，仅仅只 有Old planer才可以与DataSet API集成，由于Blink planer的批处理作业会被转换成DataStream程 序，所以不能够与DataSet API集成。值得注意的是，下⾯提到的table与DataSet之间的转换仅适⽤于 Old planer。 Table API & SQL的查询很容易与DataStream或者DataSet程序集成，并可以将Table API & SQL的查询 嵌⼊DataStream或者DataSet程序中。DataStream或者DataSet可以转换成表，反之，表也可以被转 换成DataStream或者DataSet。

从DataStream或者DataSet中注册临时表(视图)

*尖叫提示： *只能将DataStream或者DataSet转换为临时表(视图) 下⾯演示DataStream的转换，对于DataSet的转换类似。

// 获取StreamTableEnvironmentStreamTableEnvironment tableEnv = ...; DataStream<Tuple2<Long, String>> stream = ...// 将DataStream注册为⼀个名为myTable的 视图，其中字段分别为"f0", "f1"tableEnv.createTemporaryView("myTable", stream);// 将 DataStream注册为⼀个名为myTable2的视图,其中字段分别为"myLong", "myString"tableEnv.createTemporaryView("myTable2", stream, "myLong, myString");

1

### 将DataStream或者DataSet转化为Table对象

可以直接将DataStream或者DataSet转换为Table对象，之后可以使⽤Table API进⾏查询操作。下⾯演 示DataStream的转换，对于DataSet的转换类似。

// 获取StreamTableEnvironmentStreamTableEnvironment tableEnv = ...; DataStream<Tuple2<Long, String>> stream = ...// 将DataStream转换为Table对象，默认的 字段为"f0", "f1"Table table1 = tableEnv.fromDataStream(stream);// 将DataStream转换 为Table对象，默认的字段为"myLong", "myString"Table table2 = tableEnv.fromDataStream(stream, "myLong, myString");

1

将表转换为DataStream或者DataSet

当将Table转为DataStream或者DataSet时，需要指定DataStream或者DataSet的数据类型。通常最⽅ 便的数据类型是row类型，Flink提供了很多的数据类型供⽤户选择，具体包括Row、POJO、样例类、 Tuple和原⼦类型。

将表转换为DataStream

⼀个流处理查询的结果是动态变化的，所以将表转为DataStream时需要指定⼀个更新模式，共有两种 模式：Apend Mode和Retract Mode。

Apend Mode

如果动态表仅只有Insert操作，即之前输出的结果不会被更新，则使⽤该模式。如果更新或删除操作使 ⽤追加模式会失败报错

Retract Mode

始终可以使⽤此模式。返回值是bolean类型。它⽤true或false来标记数据的插⼊和撤回，返回true代 表数据插⼊，false代表数据的撤回。

// 获取StreamTableEnvironment. StreamTableEnvironment tableEnv = ...; // 包含两个 字段的表(String name, Integer age)Table table = ...// 将表转为DataStream，使⽤ Append Mode追加模式，数据类型为RowDataStream<Row> dsRow = tableEnv.toAppendStream(table, Row.class);// 将表转为DataStream，使⽤Append Mode追 加模式，数据类型为定义好的TypeInformationTupleTypeInfo<Tuple2<String, Integer>> tupleType = new TupleTypeInfo<>( Types.STRING(), Types.INT());DataStream<Tuple2<String, Integer>> dsTuple = tableEnv.toAppendStream(table, tupleType);// 将表转为DataStream，使⽤的模式为 Retract Mode撤回模式，类型为Row// 对于转换后的DataStream<Tuple2<Boolean, X>>，X表示流 的数据类型，// boolean值表示数据改变的类型，其中INSERT返回true，DELETE返回的是 falseDataStream<Tuple2<Boolean, Row>> retractStream = tableEnv.toRetractStream(table, Row.class);

1

### 将表转换为DataSet

// 获取BatchTableEnvironmentBatchTableEnvironment tableEnv = BatchTableEnvironment.create(env);// 包含两个字段的表(String name, Integer age)Table table = ...// 将表转为DataSet数据类型为RowDataSet<Row> dsRow = tableEnv.toDataSet(table, Row.class);// 将表转为DataSet，通过TypeInformation定义 Tuple2<String, Integer>数据类型TupleTypeInfo<Tuple2<String, Integer>> tupleType

1

= new TupleTypeInfo<>( Types.STRING(), Types.INT());DataSet<Tuple2<String, Integer>> dsTuple = tableEnv.toDataSet(table, tupleType);

表的Schema与数据类型之间的映射

表的Schema与数据类型之间的映射有两种⽅式：分别是基于字段下标位置的映射和基于字段名称的映 射。

基于字段下标位置的映射

该⽅式是按照字段的顺序进⾏⼀⼀映射，使⽤⽅式如下：

// 获取StreamTableEnvironmentStreamTableEnvironment tableEnv = ...; DataStream<Tuple2<Long, Integer>> stream = ...// 将DataStream转为表，默认的字段名 为"f0"和"f1"Table table = tableEnv.fromDataStream(stream);// 将DataStream转为表， 选取tuple的第⼀个元素，指定⼀个名为"myLong"的字段名Table table = tableEnv.fromDataStream(stream, "myLong");// 将DataStream转为表，为tuple的第⼀个元素 指定名为"myLong"，为第⼆个元素指定myInt的字段名Table table = tableEnv.fromDataStream(stream, "myLong, myInt");

1

### 基于字段名称的映射

基于字段名称的映射⽅式⽀持任意的数据类型包括POJO类型，可以很灵活地定义表Schema映射，所 有的字段被映射成⼀个具体的字段名称，同时也可以使⽤"as"为字段起⼀个别名。其中Tuple元素的第 ⼀个元素为f0,第⼆个元素为f1，以此类推。

// 获取StreamTableEnvironmentStreamTableEnvironment tableEnv = ...; DataStream<Tuple2<Long, Integer>> stream = ...// 将DataStream转为表，默认的字段名 为"f0"和"f1"Table table = tableEnv.fromDataStream(stream);// 将DataStream转为表， 选择tuple的第⼆个元素，指定⼀个名为"f1"的字段名Table table = tableEnv.fromDataStream(stream, "f1");// 将DataStream转为表，交换字段的顺序Table table = tableEnv.fromDataStream(stream, "f1, f0");// 将DataStream转为表，交换字段的 顺序，并为f1起别名为"myInt"，为f0起别名为"myLongTable table = tableEnv.fromDataStream(stream, "f1 as myInt, f0 as myLong");

1

### 原⼦类型

Flink将Integer, Double, String或者普通的类型称之为原⼦类型，⼀个数据类型为原⼦类型的 DataStream或者DataSet可以被转成单个字段属性的表，这个字段的类型与DataStream或者DataSet 的数据类型⼀致，这个字段的名称可以进⾏指定。

//获取StreamTableEnvironmentStreamTableEnvironment tableEnv = ...; // 数据类型为原 ⼦类型LongDataStream<Long> stream = ...// 将DataStream转为表，默认的字段名 为"f0"Table table = tableEnv.fromDataStream(stream);// 将DataStream转为表，指定字段 名为myLong"Table table = tableEnv.fromDataStream(stream, "myLong");

1

### Tuple类型

Tuple类型的DataStream或者DataSet都可以转为表，可以重新设定表的字段名(即根据tuple元素的位 置进⾏⼀⼀映射，转为表之后，每个元素都有⼀个别名)，如果不为字段指定名称，则使⽤默认的名称 (java语⾔默认的是f0,f1,scala默认的是_1),⽤户也可以重新排列字段的顺序，并为每个字段起⼀个别 名。

// 获取StreamTableEnvironmentStreamTableEnvironment tableEnv = ...; //Tuple2<Long, String>类型的DataStreamDataStream<Tuple2<Long, String>> stream =

1

...// 将DataStream转为表，默认的字段名为 "f0", "f1"Table table = tableEnv.fromDataStream(stream);// 将DataStream转为表，指定字段名为 "myLong", "myString"(按照Tuple元素的顺序位置)Table table = tableEnv.fromDataStream(stream, "myLong, myString");// 将DataStream转为表，指定字段名为 "f0", "f1"，并且交换顺序Table table = tableEnv.fromDataStream(stream, "f1, f0");// 将DataStream转为表，只选择 Tuple的第⼆个元素，指定字段名为"f1"Table table = tableEnv.fromDataStream(stream, "f1");// 将DataStream转为表，为Tuple的第⼆个元素指定别名为myString，为第⼀个元素指定字段 名为myLongTable table = tableEnv.fromDataStream(stream, "f1 as 'myString', f0 as 'myLong'");

### POJO类型

当将POJO类型的DataStream或者DataSet转为表时，如果不指定表名，则默认使⽤的是POJO字段本 身的名称，原始字段名称的映射需要指定原始字段的名称，可以为其起⼀个别名，也可以调换字段的 顺序，也可以只选择部分的字段。

// 获取StreamTableEnvironmentStreamTableEnvironment tableEnv = ...; //数据类型为 Person的POJO类型，字段包括"name"和"age"DataStream<Person> stream = ...// 将 DataStream转为表，默认的字段名称为"age", "name"Table table = tableEnv.fromDataStream(stream);// 将DataStream转为表，为"age"字段指定别名myAge, 为"name"字段指定别名myNameTable table = tableEnv.fromDataStream(stream, "age as myAge, name as myName");// 将DataStream转为表，只选择⼀个name字段Table table = tableEnv.fromDataStream(stream, "name");// 将DataStream转为表，只选择⼀个name字段， 并起⼀个别名myNameTable table = tableEnv.fromDataStream(stream, "name as myName");

1

### Row类型

Row类型的DataStream或者DataSet转为表的过程中，可以根据字段的位置或者字段名称进⾏映射， 同时也可以为字段起⼀个别名，或者只选择部分字段。

// 获取StreamTableEnvironmentStreamTableEnvironment tableEnv = ...; // Row类型的 DataStream，通过RowTypeInfo指定两个字段"name"和"age"DataStream<Row> stream = ...// 将DataStream转为表，默认的字段名为原始字段名"name"和"age"Table table =

1

tableEnv.fromDataStream(stream);// 将DataStream转为表，根据位置映射，为第⼀个字段指定 myName别名，为第⼆个字段指定myAge别名Table table = tableEnv.fromDataStream(stream, "myName, myAge");// 将DataStream转为表，根据字段名映射，为name字段起别名myName，为age字 段起别名myAgeTable table = tableEnv.fromDataStream(stream, "name as myName, age as myAge");// 将DataStream转为表，根据字段名映射，只选择name字段Table table = tableEnv.fromDataStream(stream, "name");// 将DataStream转为表，根据字段名映射，只选择 name字段，并起⼀个别名"myName"Table table = tableEnv.fromDataStream(stream, "name as myName");

## 查询优化

Old planer

Apache Flink利⽤Apache Calcite来优化和转换查询。当前执⾏的优化包括投影和过滤器下推，去相关 ⼦查询以及其他类型的查询重写。Old Planer⽬前不⽀持优化JOIN的顺序，⽽是按照查询中定义的顺 序执⾏它们。 通过提供⼀个CalciteConfig对象，可以调整在不同阶段应⽤的优化规则集。这可通过调⽤ CalciteConfig.createBuilder()⽅法来进⾏创建，并通过调⽤ tableEnv.getConfig.setPlanerConfig(calciteConfig)⽅法将该对象传递给TableEnvironment。

Blink planer

Apache Flink利⽤并扩展了Apache Calcite来执⾏复杂的查询优化。这包括⼀系列基于规则和基于成本 的优化(cost_based)，例如：

基于Apache Calcite的去相关⼦查询

投影裁剪 分区裁剪 过滤器谓词下推

⼦计划重复数据删除以避免重复计算 特殊的⼦查询重写，包括两个部分： 将IN和EXISTS转换为左半联接( left semi-join)

将NOT IN和NOT EXISTS转换为left anti-join

调整join的顺序，需要启⽤

table.optimizer.join-reorder-enabled 注意： IN / EXISTS / NOT IN / NOT EXISTS当前仅在⼦查询重写的结合条件下受⽀持。 查询优化器不仅基于计划，⽽且还可以基于数据源的统计信息以及每个操作的细粒度开销(例如io， cpu，⽹络和内存）,从⽽做出更加明智且合理的优化决策。 ⾼级⽤户可以通过CalciteConfig对象提供⾃定义优化规则，通过调⽤ tableEnv.getConfig.setPlanerConfig(calciteConfig)，将参数传递给TableEnvironment。

### 查看执⾏计划

SQL语⾔⽀持通过explain来查看某条SQL的执⾏计划，Flink Table API也可以通过调⽤explain()⽅法来 查看具体的执⾏计划。该⽅法返回⼀个字符串⽤来描述三个部分计划，分别为：

- 1.
- 2.
- 3.


关系查询的抽象语法树，即未优化的逻辑查询计划， 优化的逻辑查询计划 实际执⾏计划

StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);DataStream<Tuple2<Integer, String>> stream1 = env.fromElements(new Tuple2<>(1, "hello"));DataStream<Tuple2<Integer, String>> stream2 = env.fromElements(new Tuple2<>(1, "hello"));Table table1 = tEnv.fromDataStream(stream1, "count, word");Table table2 = tEnv.fromDataStream(stream2, "count, word");Table table = table1

1

.where("LIKE(word, 'F%')") .unionAll(table2);// 查看执⾏计划String explanation = tEnv.explain(table);System.out.println(explanation);

执⾏计划的结果为：

== 抽象语法树 ==LogicalUnion(all=[true]) LogicalFilter(condition=[LIKE($1, _UTF16LE'F%')]) FlinkLogicalDataStreamScan(id=[1], fields=[count, word]) FlinkLogicalDataStreamScan(id=[2], fields=[count, word]) == 优化的逻辑执⾏计划

1

==DataStreamUnion(all=[true], union all=[count, word]) DataStreamCalc(select= [count, word], where=[LIKE(word, _UTF-16LE'F%')]) DataStreamScan(id=[1], fields=[count, word]) DataStreamScan(id=[2], fields=[count, word]) == 物理执⾏计 划 ==Stage 1 : Data Source content : collect elements with

- CollectionInputFormat Stage 2 : Data Source content : collect elements with
- CollectionInputFormat Stage 3 : Operator content : from: (count, word) ship_strategy : REBALANCE Stage 4 : Operator content : where: (LIKE(word, _UTF-16LE'F%')), select: (count, word) ship_strategy : FORWARD Stage 5 : Operator content : from: (count, word) ship_strategy : REBALANCE


## ⼩结

本⽂主要介绍了Flink TableAPI &SQL，⾸先介绍了Flink Table API &SQL的基本概念 ，然后介绍了构建 Flink Table API & SQL程序所需要的依赖，接着介绍了Flink的两种planer，还介绍了如何注册表以及 DataStream、DataSet与表的相互转换，最后介绍了Flink的两种planer对应的查询优化并给出了⼀个 查看执⾏计划的案例。

