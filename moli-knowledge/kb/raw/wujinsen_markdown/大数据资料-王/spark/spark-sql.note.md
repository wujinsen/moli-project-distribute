spark SQL Spark SQL is Spark's module（模块） for working with structured（结构化） data.

- 1. Spark Stack
- 2. Hive兼容

spark sql可以使⽤hive的元数据库

- 3. DataFrames


![image 1](<spark-sql.note_images/imageFile1.png>)

![image 2](<spark-sql.note_images/imageFile2.png>)

A DataFrame is a distributed colection of data organized（组成） into named columns. It is conceptualy（概念地） equivalent（等价） to a table in a relational （相关的）database or a data frame in R/Python, but with richer optimizations（优化） under the hod（引擎盖）. DataFrames can be constructed from a wide aray of sources such as: structured data files, tables in Hive, external databases, or existing RDs

在Spark中，DataFrame是⼀种以RD为基础的分布式数据集，类似于传统数据库中的⼆维表格。 DataFrame与RD的主要区别在于，前者带有schema元信息，即DataFrame所表⽰的⼆维表数据集的 每⼀列都带有名称和类型。这使得Spark SQL得以洞察更多的结构信息，从⽽对藏于DataFrame背后的 数据源以及作⽤于DataFrame之上的变换进⾏了针对性的优化，最终达到⼤幅提升运⾏时效率的⽬ 标。反观RD，由于⽆从得知所存数据元素的具体内部结构，Spark Core只能在stage层⾯进⾏简单、 通⽤的流⽔线优化

# 4. RD与DataFrames

![image 3](<spark-sql.note_images/imageFile3.png>)

RD只能装在数据，并没有对数据进⾏具体的划分，如person中有name，但是RD并不知道 DataFrames将数据划分出了schema，也就是变成了结构化信息

# 5. Spark SQL Core

Spark SQL的核⼼是把已有的RD，带上Schema信息，然后注册成类似sql⾥的"Table"，对其进⾏sql 查询。这⾥⾯主要分两部分，⼀是⽣成SchemaRD，⼆是执⾏查询 正如RD的各种变换实际上只是在构造RD DAG，DataFrame的各种变换同样也是lazy的。它们并不 直接求出计算结果，⽽是将各种变换组装成与RD DAG类似的逻辑查询计划。如前所述，由于 DataFrame带有schema元信息，Spark SQL的查询优化器得以洞察数据和计算的精细结构，从⽽施⾏ 具有很强针对性的优化。随后，经过优化的逻辑执⾏计划被翻译为物理执⾏计划，并最终落实为RD DAG

# 6.外部数据源API增强

![image 4](<spark-sql.note_images/imageFile4.png>)

spark sql可以从多种数据源构建数据，例如：parquet、hive、json、hdfs、mysql、openstack等等

# 7. Parquet File

Apache Parquet 最初的设计动机是存储嵌套式数据，⽐如Protocolbufer，thrift，json等，将这类数 据存储成列式格式，以⽅便对其⾼效压缩和编码，且使⽤更少的IO操作取出需要的数据，这也是 Parquet相⽐于ORC的优势，它能够透明地将Protobuf和thrift类型的数据进⾏列式存储

# 8.使⽤spark sql

## 8.1.读取数据，将每⼀⾏的数据使⽤列分隔符分割

val lineRD = sc.textFile("hdfs:/hadop.itcast.cn:9 0/person.txt", 1).map(_.split(" ")

## 8.2.定义caseclas（相当于表的schema）

case clas Person(name:String, age:Int)

## 8.3.将lineRD转换成personRD

val personRD = lineRD.map(x=>Person(x(0), x(1).toInt)

## 8.4.将personRD转换成DataFrame

val personDF = personRD.toDF

## 8.5. 6.对personDF进⾏处理

#(DSL风格语法)

personDF.show personDF.select(personDF.col("name").show personDF.select(col("name").show personDF.select("name").show personDF.printSchema

#(SQL风格语法)

将DF注册成表 personDF.registerTempTable("t_person") 查询： sqlContext.sql("select * from t_person order by age desc limit 2").show sqlContext.sql("desc t_person").show val result = sqlContext.sql("select * from t_person order by age desc")

- 8.6.保存结果

- result.save("hdfs:/hadop.itcast.cn:9 0/sql/res1")
- result.save("hdfs:/hadop.itcast.cn:9 0/sql/res2", "json")


#以JSON⽂件格式覆写HDFS上的JSON⽂件 import org.apache.spark.sql.SaveMode._ result.save("hdfs:/hadop.itcast.cn:9 0/sql/res2", "json" , Overwrite)

- 8.7.重新加载以前的处理结果（可选）


- sqlContext.load("hdfs:/hadop.itcast.cn:9 0/sql/res1")
- sqlContext.load("hdfs:/hadop.itcast.cn:9 0/sql/res2", "json")


# 9. spark sql结合hive

- 9.1.安装hive

略

- 9.2.配置

将配置好的hive-site.xml、core-site.xml、hdfs-site.xml放⼊$SPARK-HOME/conf⽬录下

- 9.3.启动spark-sql时指定mysql连接驱动位置

bin/spark-sql -master spark:/spark1.itcast.cn:707-executor-memory 1g-total-executor-cores 2-driver-clas-path /usr/local/spark-1.3.1-bin-hadop2.4/lib/mysql-conector-java-5.1.35-bin.jar

- 9.4.使⽤spark-sql

我们可以像使⽤hive⼀样使⽤spark-sql了

- 9.5.使⽤sqlContext.sql调⽤HQL


sqlContext.sql("select * from spark.person limit 2")

或使⽤org.apache.spark.sql.hive.HiveContext import org.apache.spark.sql.hive.HiveContext val hiveContext = new HiveContext(sc) hiveContext.sql("select * from spark.person")

