---
title: hive.note（原文插图 annex）
slug: annex-hive
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hive/hive.note.md
related: [hive-数仓与-sql]
created: 2026-07-05
updated: 2026-07-05
---

Hive开发

- 1.1.Hive安装部署


- 1.1.1.Hive概述 Hive是建⽴在Hadop上的数据仓库基础构架。它提供了⼀系列的⼯具，可以⽤来进⾏数据提取转化加 载（ETL），这是⼀种可以存储、查询和分析存储在Hadop中的⼤规模数据的机制。Hive定义了简单 的类SQL查询语⾔，称为HQL，它允许熟悉SQL的⽤户查询数据。同时，这个语⾔也允许熟悉 MapReduce开发者的开发⾃定义的maper和reducer来处理内建的maper和reducer⽆法完成的复杂 的分析⼯作。 Hive是SQL解析引擎，它将SQL语句转译成M/R Job然后在Hadop执⾏。 Hive的表其实就是HDFS的⽬录/⽂件，按表名把⽂件夹分开。如果是分区表，则分区值是⼦⽂件夹， 可以直接在M/R Job⾥使⽤这些数据。
- 1.1.2.Hive体系结构 Hive的体系结构如下图所示：


- 图 1 Hive体系结构


![image 1](assets/imageFile1.png)

如图所示，⽤户接⼝，包括CLI，JDBC/ODBC，WebUI： CLI，即Shel命令⾏

JDBC/ODBC 是 Hive 的Java，与使⽤传统数据库JDBC的⽅式类似 WebGUI是通过浏览器访问 Hive Hive的元数据存储通常是存储在关系数据库如mysql, derby 中，⽤ HDFS 进⾏存储利⽤ MapReduce进 ⾏计算，Hive包括解释器、编译器、优化器、执⾏器四个部分。 Hive 将元数据存储在数据库中(metastore)，⽬前只⽀持 mysql、derby。Hive 中的元数据包括表的名 字，表的列和分区及其属性，表的属性（是否为外部表等），表的数据所在⽬录等 解释器、编译器、优化器完成 HQL 查询语句从词法分析、语法分析、编译、优化以及查询计划 （plan）的⽣成。⽣成的查询计划存储在 HDFS 中，并在随后有 MapReduce 调⽤执⾏ Hive 的数据存储在 HDFS 中，⼤部分的查询由MapReduce 完成（包含 * 的查询，⽐如 select * from table 不会⽣成 MapRedcue 任务）。 下图对Hive和传统关系数据库进⾏对⽐：

- 图 2 Hive与传统关系数据库的对⽐


![image 2](assets/imageFile2.png)

- 1.1.3.Hive安装 具体安装步骤如下所示：


- 1)在有hadop环境的client机器上解压缩hive-0.9.0-bin.tar.gz包，并重命名为hive；
- 2)打开hive/conf下的配置⽂件: #cp hive-default.xml.template hive-default.xml #cp hive-default.xml hive-site.xml #cp hive-env.sh.template hive-env.sh
- 3)然后在hive-env.sh配置HADOP_HOME
- 4)配置环境变量HIVE_HOME和PATH 最好配置⼀下jvm堆⼤⼩，否则使⽤jdbc服务的时候很容易内存溢出 安装后进⾏验证，查看命令⾏⼯具是否可⽤：


#hive-service cli 进⼊后查看数据库： hive>show databases; 如果可以正常运⾏则安装成功。 可以开启hive web界⾯， 端⼝号 9： #hive-service hwi & ⽤于通过浏览器来访问 启动hive 远程服务 (端⼝号1 0)，该服务可以使⽤java通过jdbc协议访问，运⾏命令如下： #hive-service hiveserver &

htp:/hadop0  9/hwi/

- 1.2.Hive开发


- 1.2.1.Hive的数据类型 Hive的数据类型分为原⽣和复合两种数据类型，其中原⽣数据类型有： 1)TINYINT SMALINT INT BIGINT BOLEAN FLOAT DOUBLE STRING BINARY (Hive 0.8.0以上才可⽤) TIMESTAMP (Hive 0.8.0以上才可⽤) 复合数据类型有： 1)arays: ARAY<data_type> maps: MAP<primitive_type, data_type> structs: STRUCT<col_name : data_type [COMENTcol_coment], .> union: UNIONTYPE<data_type, data_type, .>
- 1.2.2.Hive的数据存储 Hive的数据存储基于Hadop HDFS，Hive没有专⻔的数据存储格式，存储结构主要包括：数据库、⽂ 件、表、视图。 创建表时，指定Hive数据的列分隔符与⾏分隔符即可解析数据。 Hive默认可以直接加载⽂本⽂件（TextFile），还⽀持sequence file，另外还⽀持⼀种特殊格式 RCFile。 RCFile存储的表是⽔平划分的，分为多个⾏组，每个⾏组再被垂直划分， 以便每列单独存储，在每个 ⾏组中利⽤⼀个列维度的数据压缩，并提供⼀种Lazy解压（decompresion）技术来在查询执⾏时避 免不必要的列解压；


RCFile⽀持弹性的⾏组⼤⼩，⾏组⼤⼩需要权衡数据压缩性能和查询性能两⽅⾯，具体结构如下图所 示：

- 图 3 RCFile


![image 3](assets/imageFile3.png)

RCFile创建语法如下： CREATE TABLE fc_rc_test (datatime string, section string, domain string, province string, city string, idc string, ext string, ip string, file_size string, down_sudo string)STORED AS RCFILE ; 因rcfile 格式的表的数据必须要从textfile ⽂件格式表通过 insert 操作才能完成，故先要创建textfile 的 表。可以采⽤外部表的形式导⼊数据: CREATE TABLE fc_rc_ext (datatime string,section string,domain string,provincestring, city string,idc string,ext string,ip string,file_size string, down_sudo string)ROW FORMAT DELIMITED FIELDS TERMINATED BY "\t"STORED AS textfileLOCATION '/user/hive/warehouse/log/fc'; 导⼊rcfile 格式的数据： insert overwrite table fc_rc_test select * fromfc_rc_ext ;

- 1.2.3.Hive的数据模型 数据库，类似传统数据库的DataBase，默认数据库"default“。使⽤#hive命令后，不使⽤： hive>use <数据库名> 系统默认的数据库。可以显式使⽤ hive> use default; 创建⼀个新库 hive > create database test_dw; Hive对表的操作语句类似于mysql的语法： SHOW TABLES; # 查看所有的表 SHOW TABLES '*TMP*'; #⽀持模糊查询 DESCRIBE TMP_TABLE; #查看表结构 表分为以下四种类型：


1)Table 内部表 与数据库中的 Table 在概念上是类似，每⼀个Table 在 Hive 中都有⼀个相应的⽬录存储数据。例如， ⼀个表 test，它在 HDFS 中的路径为：/ warehouse/test。 warehouse是在 hive-site.xml 中由 ${hive.metastore.warehouse.dir} 指定的数据仓库的⽬录，所有的 Table 数据（不包括 External Table）都保存在这个⽬录中。 删除表时，元数据与数据都会被删除。 创建表：

/⾸先需要创建要加载的数据⽂件 hive>create table i ner_table (key string); 加载数据： hive>load data local inpath '/rot/i ner_table.dat' into table i ner_table; 当数据被加载⾄表中时，不会对数据进⾏任何转换。Load 操作只是将数据复制/移动⾄ Hive 表对应的 位置。 LOAD DATA [LOCAL] INPATH 'filepath' [OVERWRITE] INTO TABLE tablename [PARTITION (partcol1=val1, partcol2=val2.)] 把⼀个Hive表导⼊到另⼀个已建Hive表 INSERT OVERWRITE TABLE tablename[PARTITION (partcol1=val1, partcol2=val2

.)]select_statement FROM from_statement Create Table As Select（CTAS） CREATE [EXTERNAL] TABLE [IF NOT EXISTS]table_name (col_name data_type, .) … AS SELECT … 例： create table new_external_test as select * from external_table1; 查看数据： select * from i ner_table select count(*) from i ner_table 删除表： drop table i ner_table Partition 分区表 Partition 对应于数据库的 Partition 列的密集索引，在 Hive 中，表中的⼀个 Partition 对应于表下的⼀ 个⽬录，所有的 Partition 的数据都存储在对应的⽬录中，例如：test表中包含 date 和 city 两 个 Partition，则对应于date=20130201, city = bj 的 HDFS ⼦⽬录为：

- /warehouse/test/date=20130201/city=bj 对应于date=20130202, city=sh 的HDFS ⼦⽬录为；
- /warehouse/test/date=20130202/city=sh 创建表：


/⾸先需要创建要加载的数据⽂件 create table partition_table(rectime string,msisdnstring) partitioned by(daytime string,city string) row format delimited fields terminated by '\t' stored as TEXTFILE; 加载数据到分区 load data local inpath '/home/partition_table.dat' into table partition_table partition (daytime='2013-02-01',city='bj'); 查看数据 select * from partition_table select count(*) from partition_table 删除表 drop table partition_table 扩展分区： alter table partition_table ad partition (daytime='2013-02-04',city='bj'); 删除分区： alter table partition_table drop partition (daytime='2013-02-04',city='bj') 元数据，数据⽂件删除，但⽬录daytime=2013-02-04还在。 其他⼀下命令： SHOW PARTITIONS TMP_TABLE; #查看表有哪些分区 Bucket Table 桶表 桶表是对数据进⾏哈希取值，然后放到不同⽂件中存储。 创建表 create table bucket_table(id string) clustered by(id) into 4 buckets; 加载数据 set hive.enforce.bucketing = true;可以⾃动控制上⼀轮reduce的数量从⽽适配bucket的个数，当然， ⽤户也可以⾃主设置mapred.reduce.tasks去适配bucket 个数，推荐使 ⽤'set hive.enforce.bucketing = true' insert into table bucket_table select name from stu; insert overwrite table bucket_table select name fromstu; 数据加载到桶表时，会对字段取hash值，然后与桶的数量取模（hash mod分⽚）。把数据放到对应的 ⽂件中。 抽样查询： select * from bucket_table tablesample(bucket 1 out of 4 on id); 语法： TABLESAMPLE(BUCKET x OUT OF y)

y必须是table总bucket数的倍数或者因⼦。hive根据y的⼤⼩，决定抽样的⽐例。例如，table总共分了 64份，当y=32时，抽取 (64/32=)2个bucket的数据，当y=128时，抽取(64/128=)1/2个bucket的数 据。x表示从哪个bucket开始抽取。例 如，table总bucket数为32，tablesample(bucket 3 out of 16)， 表示总共抽取（32/16=）2个bucket的数据，分别为第3个bucket和第（3+16=）19个bucket的数据。 External Table 外部表 指向已经在HDFS中存在的数据，可以创建Partition，它和内部表在元数据的组织上是相同的，⽽实际 数据的存储则有较⼤的差异： 内部表 的创建过程和数据加载过程（这两个过程可以在同⼀个语句中完成），在加载数据的过程中， 实际数据会被移动到数据仓库⽬录中；之后对数据对访问将会直接在数据仓库⽬录中完成。删除表 时，表中的数据和元数据将会被同时删除 外部表只有⼀个过程，加载数据和创建表同时完成，并不会移动到数据仓库⽬录中，只是与外部数据 建⽴⼀个链接。当删除⼀个外部表时，仅删除该链接。 创建表

/⾸先需要创建要加载的数据⽂件i ner_table.dat hive>create external table external_table1 (key string) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' location '/home/external'; 在HDFS创建⽬录/home/external #hadop fs -put /home/external_table.dat /home/external 加载数据 LOAD DATA INPATH '/home/external_table1.dat' INTO TABLE external_table1; 查看数据 select * from external_table select count(*) from external_table 删除表 drop table external_table 除表外，Hive也有视图，创建视图的代码如下： CREATE VIEW v1 AS select * from t1;

- 1.2.4.Hive的查询 Hive的查询语法如下： SELECT [AL | DISTINCT] select_expr, select_expr, . FROM table_reference [WHERE where_condition] [GROUP BY col_list] [ CLUSTER BY col_list | [DISTRIBUTE BYcol_list] [SORT BY col_list] | [ORDER BY col_list] ] [LIMIT number] 语法与mysql基本类似，值得注意的是DISTRIBUTE BY，这个参数实质是指定分发器（Partitioner）， 终究Hive最终实现还是MapReduce。


- 对于分区表，可以基于Partition的查询，⼀般SELECT 查询是全表扫描。但如果是分区表，查询就可以 利⽤分区剪枝（input pruning）的特性，类似“分区索引“”，只扫描⼀个表中它关⼼的那⼀部分。Hive 当前的实现是，只有分区断⾔（Partitioned by）出现在离 FROM ⼦句最近的那个WHERE ⼦句中，才 会启⽤分区剪枝。例如，如果 page_views 表（按天分区）使⽤ date 列分区，以下语句只会读取分区 为‘208-03-01ʼ的数据。 SELECT page_views.* FROM page_views WHERE page_views.date >= '2013-03-01' ANDpage_views.date <= '2013-03-01' Limit 可以限制查询的记录数。查询的结果是随机选择的。下⾯的查询语句从 t1 表中随机查询5条记 录： SELECT * FROM t1 LIMIT 5 Top N查询，下⾯的查询语句查询销售记录最⼤的5 个销售代表。 SET mapred.reduce.tasks = 1 #该设置在MapReduce课程中也看到过，因此执⾏Hive必须要有 MapReduce的基础。 SELECT * FROM sales SORT BY amount DESC LIMIT 5 表连接查询相关操作如下： 内连接 select b.name,a.* from dim_ac a join acinfo b on(a.ac=b.acip) limit 10; 左外连接 select b.name,a.* from dim_ac a left outer join acinfob on a.ac=b.acip limit 10; 具体链接的含义在mysql阶段已经很明确，此处不做解释。
- 1.2.5.JDBC Hive可以通过java的jdbc协议调⽤，但是事实上这个操作意义并不是很⼤，除⾮我们要⼀个数据分析的 web应⽤，终究联机分析和联机事务处理还是有区别的（性能就是最⼤的区别）。 要进⾏JDBC链接⾸先需要将Hive远程服务启动： #hive-service hiveserver >/dev/nul 2>/dev/nul & JAVA客户端相关代码 Clas.forName("org.apache.hadop.hive.jdbc.HiveDriver"); Conection con =DriverManager.getConection("jdbc:hive:/master:1 0/wlan_dw", ", "); Statement stmt = con.createStatement(); String querySQL="SELECT * FROMwlan_dw.dim_m order by flux desc limit 10"; ResultSet res = stmt.executeQuery(querySQL); while (res.next() { System.out.println(res.getString(1) +"\t" +res.getLong(2)+"\t" +res.getLong(3)+"\t"


+res.getLong(4)+"\t" +res.getLong(5); }

- 1.3.UDF


- 1.3.1.UDF概述


- Hive可以允许⽤户编写⾃⼰定义的函数UDF，来在查询中使⽤。Hive中有3种UDF： 1)UDF：操作单个数据⾏，产⽣单个数据⾏； UDAF：操作多个数据⾏，产⽣⼀个数据⾏。 UDTF：操作⼀个数据⾏，产⽣多个数据⾏⼀个表作为输出。
- 1.3.2.UDF ⽤户构建的UDF使⽤过程如下： 第⼀步：继承UDF或者UDAF或者UDTF，实现特定的⽅法。 第⼆步：将写好的类打包为jar。如hivefirst.jar. 第三步：进⼊到Hive外壳环境中，利⽤ad jar /home/hadop/hivefirst.jar.注册该jar⽂件 第四步：为该类起⼀个别名，create temporary function mylength as 'com.whut.StringLength';这⾥ 注意UDF只是为这个Hive会话临时定义的。 第五步：在select中使⽤mylength(); 具体参考本课程代码中的实例。
- 1.3.3.UDAF ⽤户的UDAF必须继承了org.apache.hadop.hive.ql.exec.UDAF； ⽤户的UDAF必须包含⾄少⼀个实现了org.apache.hadop.hive.ql.exec的静态类，诸如常⻅的实现 了 UDAFEvaluator。 ⼀个计算函数必须实现的5个⽅法的具体含义如下： init()：主要是负责初始化计算函数并且重设其内部状态，⼀般就是重设其内部字段。⼀般在静态类中 定义⼀个内部字段来存放最终的结果。 iterate()：每⼀次对⼀个新值进⾏聚集计算时候都会调⽤该⽅法，计算函数会根据聚集计算结果更新内 部状态。当输⼊值合法或者正确计算了，则就返回true。 terminatePartial()：Hive需要部分聚集结果的时候会调⽤该⽅法，必须要返回⼀个封装了聚集计算当 前状态的对象。 merge()：Hive进⾏合并⼀个部分聚集和另⼀个部分聚集的时候会调⽤该⽅法。 terminate()：Hive最终聚集结果的时候就会调⽤该⽅法。计算函数需要把状态作为⼀个值返回给⽤ 户。 部分聚集结果的数据类型和最终结果的数据类型可以不同。
- 1.3.4.UDTF UDTF(User-Defined Table-Generating Functions)⽤来解决 输⼊⼀⾏输出多⾏(On-tomany maping) 的需求。 编写⾃⼰需要的UDTF继承 org.apache.hadop.hive.ql.udf.generic.GenericUDTF 实现initialize, proces, close三个⽅法 UDTF⾸先会调⽤initialize⽅法，此⽅法返回UDTF的返回⾏的信息（返回个数，类型）。初始化完成 后，会调⽤proces⽅法，对传⼊的参数进⾏处理，可以通过forword()⽅法把结果返回。最后close() ⽅法调⽤，对需要清理的⽅法进⾏清理。


UDTF有两种使⽤⽅法，⼀种直接放到select后⾯，⼀种和lateral view⼀起使⽤。 1)直接select中使⽤： select explode_map(properties) as (col1,col2) fromsrc; 不可以添加其他字段使⽤： select a, explode_map(properties) as (col1,col2) fromsrc 不可以嵌套调⽤： select explode_map(explode_map(properties) fromsrc 不可以和group by/cluster by/distribute by/sort by⼀起使⽤： select explode_map(properties) as (col1,col2) from srcgroup by col1, col2 和lateral view⼀起使⽤： select src.id, mytable.col1, mytable.col2 from srclateral view explode_map(properties) mytable as col1, col2; lateral view⽤于和split, explode等UDTF⼀起使⽤，它能够将⼀⾏数据拆成多⾏数据，在此基础上可以 对拆分后的数据进⾏聚合。lateral view⾸先为原始表的每⾏调⽤UDTF，UTDF会把⼀⾏拆分成⼀或者 多⾏，lateral view再把结果组合，产⽣⼀个⽀持别名表的虚拟表。 LATERAL VIEW udtf(expresion) tableAlias AS columnAlias 其中 columnAlias是多个⽤,ʼ分割的虚拟列名，这些列名从属于表tableAlias 传统开源ETL⼯具KETLE使⽤

- 1.4.Ketle使⽤


- 1.4.1.Ketle概述 ETL（Extract-Transform-Load的缩写，即数据抽取、转换、装载的过程），对于⾦融IT来说，经常会 遇到⼤数据量的处理，转换，迁移，所以了解并掌握⼀种etl⼯具的使⽤，必不可少。 Ketle是⼀款国外开源的etl⼯具，纯java编写，绿⾊⽆需安装，数据抽取⾼效稳定。Ketle中有两种脚 本⽂件，transformation和job，transformation完成针对数据的基础转换，job则完成整个⼯作流的控 制。 Ketle可以在 ⽹站下载，下载ketle压缩包，因ketle为绿⾊软件，解压缩到 任意本地路径即可（⽬前最⾼版本已经不提供完整开源代码，可以下载早期开源版本使⽤，推荐3.54.0）。 进⼊到Ketle⽬录，如果Ketle部署在windows环境下，双击运⾏spon.bat⽂件，出现如下界⾯：


htp:/ketle.pentaho.org/

- 图 4 Ketle启动


![image 4](assets/imageFile4.png)

Ketle家族⽬前包括3个核⼼产品：Spon、Pan、Kitchen。 SPON 允许你通过图形界⾯来设计ETL转换过程（Transformation）。 PAN 允许你批量运⾏由Spon设计的ETL转换 (例如使⽤⼀个时间调度器)。Pan是⼀个后台执⾏的程 序，没有图形界⾯。 KITCHEN 允许你批量运⾏⽤Chef设计的jobs。KITCHEN 允许你批量使⽤由Chef设计的任务(例如使⽤ ⼀个时间调度器)。KITCHEN也是⼀个后台运⾏的程序。

- 1.4.2.Ketle转换 转换是ETL过程的完整流程，换句话说转换包含了数据抽取、转换和加载过程。通过KETLE的转换， ⽤户可以将抽取转换和加载过程编排成⼀个数据流，其中抽取转换和加载都是流程中的插件，具体如 下图所示：


- 图 5 KETLE转换流程


![image 5](assets/imageFile5.png)

每⼀个组件将结果通过⼀个集合队列传递到下⼀个组件，两个组件之间相当于链接了⼀根线，这就是 流程编排了。 Ketle的具体转换组件如下所示： 输⼊插件： ⽂本⽂件输⼊、⽣成记录、表输⼊、Fixed file input、Get data from XML 输出插件：

XML输出、删除、插⼊/更新、⽂本⽂件输出、更新、表输出 转换插件： Ad a checksum、Replace in string、Set field value、Unique rows（HashSet）、增加常量、增加序 列、字段选择、拆分字段 Flow插件： Abort、Switch/case、空操作、过滤记录 脚本插件： Modified Java Script Value、执⾏SQL脚本 查询插件： File exists、Table exists、调⽤DB存储过程 转换的执⾏过程如下所示：

- 图 6 KETLE转换流程执⾏过程


![image 6](assets/imageFile6.png)

- 1.4.3.Ketle作业 作业⽤⼀句最简单的话就是将多个转换按照既定的任务序列执⾏，并且可以定期的执⾏。


- 图－7 作业具体执⾏流程


![image 7](assets/imageFile7.png)

作业的插件如下所示： 通⽤插件： START、DUMY、Transformation ⽂件管理插件： Copy Files、Compare folders、Create a folder、Create file、Delete files、Delete folders、File Compare、Move Files、Wait for file、Zip file、Unzip file 条件插件： Check Db conections、Check files locked、Check if a folder is empty、Check if files exist、File Exists、Table exists、Wait for 脚本插件： Shel、SQL Utility插件： Ping a host、Truncate tables ⽂件传输插件： Secure FTP=get a file with SFTP、put a file with SFTP、FTP Delete

- 1.4.4.Ketle流程部署 我们不能总是依赖图形界⾯来执⾏作业和转换，这样系统的执⾏效率会有损耗，因此我们需要使⽤命 令⾏⼯具执⾏。 对于转换可以⽤Pan⼯具执⾏，Pan命令来执⾏转换，下⾯给出的是pan参数：


- 图－8 Pan


![image 8](assets/imageFile8.png)

- 作业可以⽤Kitchen⼯具执⾏，下⾯给出的是Kitchen参数：
- 图－9 Kitchen


![image 9](assets/imageFile9.png)

- 1.4.5.传统数据同步 数据同步有五种⽅式，以下将介绍这五种⽅式以及优缺点： 1)全表拷⻉ 定时清空⽬的数据源，将源数据源的数据全盘拷⻉到⽬的数据源。⼀般⽤于数据量不⼤，实时性要求 不⾼的场景 优点： 基本不影响业务系统,开发、部署都很简单 缺点： 效率低


数据⽐较 通过⽐较两边数据源数据，来完成数据同步。⼀般⽤于实时性要求不⾼的场景。 优点： 基本不影响业务系统 缺点： 效率低 时间戳 通过⽐较两边数据源数据，来完成数据同步。⼀般⽤于实时性要求不⾼的场景。 优点： 基本不影响业务系统 缺点： 效率低 触发器 在源数据库建⽴增、删、改触发器，每当源数据库有数据变化，相应触发器就会激活，触发器会将变 更的数据保存在⼀个临时表⾥。ORCLE 的 同步CDC (synchronized CDC) 实际上就是使⽤的触发器 优点： 能做到实时同步 缺点： 降低业务系统性能，ORCLE 的synchronized CDC ⼤概降低10% 左右。 影响到业务系统，因 为需要在业务系统建⽴触发器。 ⽇志 通过解析预写⽇志的⽅式进⾏数据同步。 预写⽇志中记录的是⽤户的操作。 ⽐如oracle、hbase均有预写⽇志。
