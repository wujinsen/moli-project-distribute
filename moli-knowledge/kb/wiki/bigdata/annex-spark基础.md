---
title: spark基础.note（原文插图 annex）
slug: annex-spark基础
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/spark/spark基础.note.md
related: [spark-核心概念与实践]
created: 2026-07-05
updated: 2026-07-05
---

spark

# 1.概述

- 1.1.简介

Apache Spark是⼀个围绕速度、易⽤性和复杂分析构建的⼤数据处理框架。最初在209年由加州⼤学 伯克利分校的AMPLab开发，并于2010年成为Apache的开源项⽬之⼀。 Spark为我们提供了⼀个全⾯、统⼀的框架⽤于管理各种有着不同性质（⽂本数据、图表数据等）的数 据集和数据源（批量数据或实时的流数据）的⼤数据处理的需求。 利⽤内存数据存储和接近实时的处理能⼒，Spark⽐其他的⼤数据处理技术的性能要快很多倍。

- 1.2. Mapreduce和Spark

MapReduce是⼀路计算的优秀解决⽅案，不过对于需要多路计算和算法的⽤例来说，并⾮⼗分⾼效。 如果想要完成⽐较复杂的⼯作，就必须将⼀系列的MapReduce作业串联起来然后顺序执⾏这些作业。 每⼀个作业都是⾼时延的，⽽且只有在前⼀个作业完成之后下⼀个作业才能开始启动。 在下⼀步开始之前，上⼀步的作业输出数据必须要存储到分布式⽂件系统中。因此，复制和磁盘存储 会导致这种⽅式速度变慢。 ⽽Spark则允许程序开发者使⽤有向⽆环图（DAG）开发复杂的多步数据管道。⽽且还⽀持跨作业的内 存数据共享，以便不同的作业可以共同处理同⼀个数据。

Spark将中间结果保存在内存中⽽不是将其写⼊磁盘，当需要多次处理同⼀数据集时，这⼀点特别实 ⽤。

Spark会尝试在内存中存储尽可能多的数据然后将其写⼊磁盘。它可以将某个数据集的⼀部分存⼊内存 ⽽剩余部分存⼊磁盘。从⽽Spark可以⽤于处理⼤于集群内存容量总和的数据集。

- 1.3. Hadop为什么慢


![image 1](assets/imageFile1.png)

## 1.4. mapreduce和spark对⽐

![image 2](assets/imageFile2.png)

![image 3](assets/imageFile3.png)

- 1.5. spark的其他特性


- 1、⽀持⽐Map和Reduce更多的函数。
- 2、可以通过延迟计算帮助优化整体数据处理流程。
- 3、提供简明、⼀致的Scala，Java和Python API。
- 4、提供交互式Scala和Python Shel。帮助进⾏原型验证和逻辑测试 （⽬前暂不⽀持Java）


# 2. Spark⽣态系统

除了Spark核⼼API之外，Spark⽣态系统中还包括其他附加库，可以在⼤数据分析和机器学习领域提供 更多的能⼒。

![image 4](assets/imageFile4.png)

- 2.1. SparkStreaming:


Spark Streaming基于微批量⽅式的计算和处理，可以⽤于处理实时的流数据。它使⽤DStream，简单 来说就是⼀个弹性分布式数据集（RD）系列，处理实时数据。

- 2.2. SparkSQL:

Spark SQL可以通过JDBC API将Spark数据集暴露出去，⽽且还可以⽤传统的BI和可视化⼯具在Spark 数据上执⾏类似SQL的查询。⽤户还可以⽤Spark SQL对不同格式的数据（如JSON，Parquet以及数据 库等）执⾏ETL，将其转化，然后暴露给特定的查询。

- 2.3. SparkMLlib:

MLlib是⼀个可扩展的Spark机器学习库，由通⽤的学习算法和⼯具组成，包括⼆元分类、线性回归、 聚类、协同过滤、梯度下降以及底层优化原语

- 2.4. SparkGraphX:

GraphX是⽤于图计算和并⾏图计算的新的（alpha）Spark API。通过引⼊弹性分布式属性图 （Resilient Distributed Property Graph），⼀种顶点和边都带有属性的有向多重图，扩展了Spark RD。 Tachyon是⼀个以内存为中⼼的分布式⽂件系统，能够提供内存级别速度的跨集群框架（如Spark和 MapReduce）的可信⽂件共享。它将⼯作集⽂件缓存在内存中，从⽽避免到磁盘中加载需要经常读取 的数据集。通过这⼀机制，不同的作业/查询和框架可以以内存级的速度访问缓存的⽂件。

BlinkDB是⼀个近似查询引擎，⽤于在海量数据上执⾏交互式SQL查询。BlinkDB可以通过牺牲数据精 度来提升查询响应时间。通过在数据样本上执⾏查询并展⽰包含有意义的错误线注解的结果，操作⼤ 数据集合。

- 2.5. BDAS


![image 5](assets/imageFile5.png)

# 3. Spark体系架构

Spark体系架构包括如下三个主要组件： 数据存储

API 资源管理框架

## 3.1.资源管理：

Spark既可以部署在⼀个单独的服务器集群上（Standalone） 也可以部署在像Mesos或YARN这样的分布式计算框架之上。

## 3.2. SparkAPI：

应⽤开发者可以⽤标准的API接⼜创建基于Spark的应⽤ Spark提供三种程序设计语⾔的API： Scala Java Python

## 3.3.数据存储：

Spark⽤HDFS⽂件系统存储数据。它可⽤于存储任何兼容于Hadop的数据源，包括HDFS，HBase， Casandra等。 Spark在对数据的处理过程中，会将数据封装成RD数据结构

# 4. RD

RD(Resilient Distributed Datasets)，弹性分布式数据集，是分布式内存的⼀个抽象概念 RD作为数据结构，本质上是⼀个只读的分区记录集合。⼀个RD可以包含多个分区，每个分区就是 ⼀个dataset⽚段 RD并不保存真正的数据，仅保存元数据信息 RD之间可以存在依赖关系

## 4.1. RD -弹性分布式数据集：核⼼

RD是Spark框架中的核⼼概念。 可以将RD视作数据库中的⼀张表。其中可以保存任何类型的数据，可以通过API来处理RD及RD中 的数据 类似于Mapreduce，RD也有分区的概念 RD是不可变的，可以⽤变换（Transformation）操作RD，但是这个变换所返回的是⼀个全新的 RD，⽽原有的RD仍然保持不变

## 4.2. RD创建的三种⽅式

集合并⾏化 val ar = Aray(1,2,3,4,5,6,7,8) val rd1 = sc.paralelize(ar, 2) /2代表分区数量

从外部⽂件系统 分布式⽂件系统：如hdfs⽂件系统，S3 val rd2 = sc.textFile("hdfs:/node1 9 0/words.txt")

从⽗RD转换成新的⼦RD Transformation

- 4.3. RD -弹性分布式数据集

RD⽀持两种类型的操作： 变换（Transformation） 变换：变换的返回值是⼀个新的RD集合，⽽不是单个值。调⽤⼀个变换⽅法，不会有任何求值计 算，它只获取⼀个RD作为参数，然后返回⼀个新的RD。Transformation是lazy模式，延迟执⾏ 变换函数包括：map，filter，flatMap，groupByKey，reduceByKey，agregateByKey，pipe和 coalesce。

⾏动（Action） ⾏动：⾏动操作计算并返回⼀个新的值。当在⼀个RD对象上调⽤⾏动函数时，会在这⼀时刻计算全 部的数据处理查询并返回结果值。 ⾏动操作包括：reduce，colect，count，first，take，countByKey以及foreach。

- 4.4. RD操作流程⽰意

- 4.5. RD的转换与操作


![image 6](assets/imageFile6.png)

![image 7](assets/imageFile7.png)

## 4.6. RD -源码中的注释

Internaly, each RD is characterized by five main properties:

A list of partitions A function for computing each split A list of dependencies on other RDs Optionaly, a Partitioner for key-value RDs (e.g. to say that the RD is hash-partitioned) Optionaly, a list of prefered locations to compute each split on (e.g. block locations for an HDFS file)

## 4.7.安装spark集群（Standalone）

见⽂档

## 4.8. spark交互式shel

spark提供⼀个scala-shel提供交互式操作 启动spark-shel bin/spark-shel -master spark:/masterip:port（707） 集群运⾏模式 bin/spark-shel -master local local运⾏模式

wordcount⽰例 scala>sc.textFile("hdfs:/namenode:port/data").flatMap(_.split("\t").map(_,1).reduceByKey().col ect

# 5. spark命令

- 5.1.查看spark的官⽅⽂档


![image 8](assets/imageFile8.png)

![image 9](assets/imageFile9.png)

![image 10](assets/imageFile10.png)

transformation

![image 11](assets/imageFile11.png)

action

![image 12](assets/imageFile12.png)

### 练习

#通过并⾏化scala集合创建RD val rd1 = sc.paralelize(Aray(1,2,3,4,5,6,7,8)

#查看该rd的分区数量 rd1.partitions.length #更改分区，因为rd是只读的，所以重新分区后会⽣成新的rd来使⽤⼼得分区 val rd2 = sc.paralelize(Aray(1,2,3,4,5,6,7,8),2) val rd3 = rd2.repartition(3) rd3.partitions.length

#union求并集，注意类型要⼀致 val rd6 = sc.paralelize(List(5,6,4,7) val rd7 = sc.paralelize(List(1,2,3,4) val rd8 = rd6.union(rd7) rd8.distinct.sortBy(x=>x).colect

#intersection求交集 val rd9 = rd6.intersection(rd7)

#join：keyvalue形式的值，key相同join出来 val rd1 = sc.paralelize(List("tom", 1), ("jery", 3), ("kity", 2) val rd2 = sc.paralelize(List("jery", 2), ("tom", 1), ("shuke", 2) val rd3 = rd1.join(rd2)

#groupByKey val rd3 = rd1 union rd2 rd3.groupByKey rd3.groupByKey.map(x=>(x._1,x._2.sum)

#WordCount sc.textFile("/rot/words.txt").flatMap(x=>x.split(" ").map(_,1).reduceByKey(_+_).sortBy(_._2,false).colect

sc.textFile("/rot/words.txt").flatMap(x=>x.split(" ").map(_,1).groupByKey.map(t=>(t._1, t._2.sum).colect

#cogroup：在⾃⼰的集合中分组，将分组的结果和其他集合中的结果取并集 val rd1 = sc.paralelize(List("tom", 1), ("tom", 2), ("jery", 3), ("kity", 2) val rd2 = sc.paralelize(List("jery", 2), ("tom", 1), ("shuke", 2) val rd3 = rd1.cogroup(rd2) val rd4 = rd3.map(t=>(t._1, t._2._1.sum + t._2._2.sum)

#cartesian笛卡尔积 val rd1 = sc.paralelize(List("tom", "jery") val rd2 = sc.paralelize(List("tom", "kity", "shuke") val rd3 = rd1.cartesian(rd2)

#

#spark action #并⾏化创建rd val rd1 = sc.paralelize(List(1,2,3,4,5)

#colect：将rd的数据计算，转换成scala的集合打印控制台，数据量⼩时⽤。 rd1.colect

#reduce：将元素进⾏reduce计算，直接显⽰结果 val rd2 = rd1.reduce(_+_)

#count：求个数

rd1.count

#top：取rd中的最⼤的前两个 rd1.top(2)

#take：取前⼏个 rd1.take(2)

#first：取集合的第⼀个元素，相当于take（1） rd1.first

#takeOrdered：取排序的前⼏个 rd1.takeOrdered(3)

#将结果保存成⽂本⽂件 saveAsTextFile（“路径orhdfs”）

## 5.2. sparkshel

启动local模式的spark shel

./bin/spark-shel

启动集群的spark shel

./bin/spark-shel -master spark:/master1 707

启动集群的spark shel，配置参数

./bin/spark-shel -master spark:/master1 707-executor-memory 512m-total-executor-cores 3

参数解释： spark:/master1 707 : 指定主机

- -executor-memory 512m：每个work使⽤多⼤内存
- -total-executor-cores 3：指定work总共使⽤的核数


## 5.3. spark的演⽰

sc: spark context,启动spark会⾃动创建的对象，客户端和spark交互的桥梁 创建RD： val rd1 = sc.paralelize(Aray(1,2,3,4,5,6,7,8) 查看RD的分区:

rd1.partitions.length 转换： rd1.filter(_%2=0) 执⾏： res1.colect map： rd1. filter(_%2=0).map(_*10).colect sortby: rd1. filter(_%2=0).map(_*10).sortBy(x=>x,false).colect

# 6. spark api wordcount

- 6.1.创建项⽬


- 6.2.导包


- 6.3.写wordcount


<table>
  <tr>
    <th>por or.apa .park. parkof import org.apache.spark.SparkContext clas WordCount { } object WordCount {<br><br>def main(args: Aray[String]) { /创建配置，设置ap的name valconf = new SparkConf().setApName("WordCount") //创建sparkcontext，将conf传进来 valsc = new SparkContext(conf) //从⽂件中读取数据，做wordcount，写到⽂件系统 sc.textFile(args(0).flatMap(_.split(" ").map(_,1).reduceByKey(_+_).saveAsTextFile(args(1) //停⽌ sc.stop()<br><br>}</th>
  </tr>
</table>


}

- 6.4.打jar包

略

- 6.5.提交


注意提交的时候，涉及到ip的地⽅尽量⽤域名，否则报错

<table>
  <tr>
    <th>spark-submit -clas WordCount -master spark:/master1 707-executor-memory 512mtotal-executor-cores 3 /home/hadop/wordcount.jar hdfs:/master1ha:9 0/core-site.xml</th>
  </tr>
</table>


hdfs:/master1ha:9 0/out1

# 7. spark源码分析

- 7.1.从哪⼊⼿


看start-master.sh脚本得知要对org.apache.spark.deploy.master.Master

- 7.2.远程debug


<table>
  <tr>
    <th>#调试Master，在master节点的spark-env.sh中添加SPARK_MASTER_OPTS变量 export SPARK_MASTER_OPTS="-Xdebug -<br><br>Xrunjdwp:transport=dt_socket,server=y,suspend=y,adres=1 0" #启动Master sbin/start-master.sh<br><br>#调试Worker，在worker节点的spark-env.sh中添加SPARK_WORKER_OPTS变量 export SPARK_WORKER_OPTS="-Xdebug -<br><br>Xrunjdwp:transport=dt_socket,server=y,suspend=y,adres=1 01" #启动Worker sbin/start-slave.sh 1 spark:/node1.itcast.cn:707<br><br>#调试spark-submit +app bin/spark-submit --clas cn.itcast.wc.WordCount -master spark:/node1.itcast.cn:707-executormemory 1G /rot/wc.jar hdfs:/192.168.80.10 9 0/words.txt hdfs:/192.168.80.10 9 0/wcount3-<br><br>-driver-java-options "-Xdebug-<br><br>Xrunjdwp:transport=dt_socket,server=y,suspend=y,adres=1 02"<br><br><br>#调试spark-submit +app + executor bin/spark-submit --clas cn.itcast.wc.WordCount -master spark:/node1.itcast.cn:707-conf "spark.executor.extraJavaOptions=-Xdebug Xrunjdwp:transport=dt_socket,server=y,suspend=y,adress=1 03"--executor-memory 1G /root/wc.jar hdfs:/192.168.80.10 9 0/words.txt hdfs:/192.168.80.10 9 0/wcount3-driver-javaoptions "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,adres=1 02"</th>
  </tr>
</table>


通过远程debug的⽅式可以调试本地的代码

- 7.3.在master所在的节点上，修改spark-env.sh,添加debuge调


试：

export SPARK_MASTER_OPTS="-Xdebug Xrunjdwp:transport=dt_socket,server=y,suspend=y,adres=1 0"

- 7.4.重新启动Master


sbin/start-master.sh

## 7.5.是⽤IDEA连接远程调试端⼜

![image 13](assets/imageFile13.png)

![image 14](assets/imageFile14.png)

![image 15](assets/imageFile15.png)

重点看org.apache.spark.deploy.master.Master -> 869⾏ -> 906(⽤来创建aka的Actor createActorSystem)

- ->org.apache.spark.util.Utils 1837⾏ ->org.apache.spark.util.AkaUtils 60⾏(准备aka的参数，并且 创建ActorSystem)
- ->org.apache.spark.deploy.master.Master 908⾏（创建actor,⽤于通信） -》 org.apache.spark.deploy.master.Master 136⾏（调⽤preStart⽅法）
- ->创建web管理界⾯-> org.apache.spark.deploy.master.Master 143⾏（启动⼀个定时器，⽤来检测 出现故障的Worker节点）
- ->org.apache.spark.deploy.master.Master 43⾏的case -> timeOutDeadWorkers()


- 7.6. master启动UML图

- 7.7. work启动UML图


![image 16](assets/imageFile16.png)

![image 17](assets/imageFile17.png)

- 7.8.任务提交流程

- 7.9.任务运⾏流程


![image 18](assets/imageFile18.png)

![image 19](assets/imageFile19.png)

# 8. Spark Runtime

![image 20](assets/imageFile20.png)

- 8.1. Spark任务执⾏流程


![image 21](assets/imageFile21.png)

# 9. RD依赖

- 9.1. RD简介


RD作为数据结构，本质上是⼀个只读的分区记录集合。 ⼀个RD可以包含多个分区，每个分区就是⼀个dataset⽚段。 RD可以相互依赖。 如果RD的每个分区最多只能被⼀个Child RD的⼀个分区使⽤，则称之为narow dependency； 若多个Child RD分区都可以依赖，则称之为wide dependency。 的操作依据其特性，可能会产⽣不同的依赖。例如map操作会产⽣narow dependency，⽽join操作则 产⽣wide dependency。

## 9.2. RD的宽依赖和窄依赖

![image 22](assets/imageFile22.png)

宽依赖(wide dependencies) ⼦RD的每个分区依赖于所有的⽗RD分区 对单个RD基于key进⾏重组和reduce，如groupByKey，reduceByKey 对两个RD基于key进⾏join和重组，如join 注意：经过⼤量shufle⽣成的RD，建议进⾏缓存。这样避免失败后重新计算带来的开销。

窄依赖(narow dependencies) ⼦RD的每个分区依赖于常数个⽗分区（与数据规模⽆关） 输⼊输出⼀对⼀的算⼦，且结果RD的分区结构不变。主要是map/flatmap 输⼊输出⼀对⼀的算⼦，但结果RD的分区结构发⽣了变化，如union 从输⼊中选择部分元素的算⼦，如filter、distinct、substract、sample

## 9.3. DAG的stage划分

![image 23](assets/imageFile23.png)

从后向前推导，当遇到宽依赖的时候，stage加1，最后再加1

## 9.4. hadopRD

分区：每个hdfs block 依赖：⽆ 函数：读取每⼀个block 最佳位置：hdfs block所在的位置 分区策略：⽆

## 9.5.操作

查看rd依赖关系 rd1.toDebugString

wordcount的rd转换

<table>
  <tr>
    <th>poror.apa .park. parkof import org.apache.spark.SparkContext clas WordCount { } object WordCount {<br><br>def main(args: Aray[String]) { /创建配置，设置ap的name<br><br>val conf = new SparkConf().setApName("WordCount") /创建sparkcontext，将conf传进来<br><br>val sc = new SparkContext(conf) /从⽂件中读取数据，做wordcount，写到⽂件系统 al rd1 = sc.textFile(args(0) al rd2 rd1.flatMap(_.split(" ") a rd3 rd2map(_,1)<br><br>rd4 rd3reduceByKey(_+_) val rd5 = rd4.saveAsTextFile(args(1) /停⽌<br><br>sc.stop() }</th>
  </tr>
</table>


}

### RD隐式转换成PairRDFunctions的⼀些案例

- 1、flatMapValues：将value压平处理，key不动 val a = sc.paralelize(List("a", "1 2"), ("b", "3 4")

/转换为 Aray(a,1), (a,2), (b,3), (b,4) 可以这样： a.flatMap{case(k,v)=>v.split(" ").map(x=>(k,x)} 也可以这样： a.flatMapValues(_.split(" ")

- 2、mapValues：将value直接进⾏处理，key不动 val a = sc.paralelize(List("a", 1), ("b", 2) a.mapValues(_*2) 结果： Aray(a,2), (b,4)
