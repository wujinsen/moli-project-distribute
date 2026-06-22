问题导读：

- 1.什么是sbt？

- 2.sbt项⽬环境如何建⽴？
- 3.如何使⽤sbt编译打包scala?


![image 1](<用SBT编译Spark的WordCount程序.note_images/imageFile1.png>)

sbt介绍 sbt是⼀个代码编译⼯具，是scala界的mvn，可以编译scala，java等，需要java1.6以上。

sbt项⽬环境建⽴ sbt编译需要固定的⽬录格式，并且需要联⽹，sbt会将依赖的jar包下载到⽤户home的.ivy2下⾯，⽬录 结构如下：

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


|-build.sbt |-lib |-project |-src | |-main | | |-scala | |-test | |-scala |-sbt |-target复制代码以上建⽴⽬录如下：

- 1.
- 2.
- 3.
- 4.
- 5.


mkdir -p ~/spark_wordcount/lib mkdir -p ~/spark_wordcount/project mkdir -p ~/spark_wordcount/src/main/scala mkdir -p ~/spark_wordcount/src/test/scala mkdir -p ~/spark_wordcount/target复制代码

然后拷⻉spark安装⽬录的sbt⽬录的 sbt脚本和sbt的jar包

1.

cp /path/to/spark/sbt/sbt* ~/spark_wordcount/复制代码

由于spark的sbt脚本默认查找./sbt⽬录，修改如下

- 1.
- 2.
- 3.


JAR=sbt/sbt-launch-${SBT_VERSION}.jar to JAR=sbt-launch-${SBT_VERSION}.jar复制代码

拷⻉spark的jar包到，sbt的lib⽬录

- 1.
- 2.


cp /path/to/spark/asembly/target/scala-2.10/spark-asembly_2.10-0.9.0-incubatinghadop2.2.0.jar \ > ~/spark_wordcount/lib/复制代码

建⽴build.sbt配置⽂件,各⾏需要有⼀个空⾏分割(类似maven的pom)

- 1.
- 2.
- 3.
- 4.
- 5.


name := "WordCount" [this is bank line] version := "1.0.0" [this is bank line] scalaVersion := "2.10.3"复制代码

由于spark的sbt脚本需要到project的build.properties⽂件找sbt的版本号，我们建⽴该⽂件，增加如下 内容：

1.

sbt.version=0.12.4复制代码

Spark WordCount程序编写及编译 建⽴WordCount.scala源⽂件，假设需要包为spark.example

- 1.
- 2.


mkdir -p ~/spark_wordcount/src/main/scala/spark/example vi -p ~/spark_wordcount/src/main/scala/spark/example/WordCount.scala复制代码

添加具体的程序代码，并保存

- 1.
- 2.


package spark.example

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


import org.apache.spark._ import SparkContext._

object WordCount { def main(args: Aray[String]) {

/命令⾏参数个数检查 if (args.length = 0) { System.er.println("Usage: spark.example.WordCount <input> <output>") System.exit(1)

} /使⽤hdfs⽂件系统 val hdfsPathRot = "hdfshost:9 0" /实例化spark的上下⽂环境 val spark = new SparkContext(args(0), "WordCount",

System.getenv("SPARK_HOME"),SparkContext.jarOfClas(this.getClas) /读取输⼊⽂件

val inputFile = spark.textFile(hdfsPathRot + args(1) /执⾏WordCount计数 /读取inputFile执⾏⽅法flatMap，将每⾏通过空格分词 /然后将该词输出该词和计数的⼀个元组，并初始化计数 /为 1，然后执⾏reduceByKey⽅法，对相同的词计数累 /加

val countResult = inputFile.flatMap(line => line.split(" ")

.map(word => (word, 1)

.reduceByKey(_ + _) /输出WordCount结果到指定⽬录

countResult.saveAsTextFile(hdfsPathRot + args(2) }

}复制代码

到spark_wordcount⽬录，执⾏编译：

- 1.
- 2.


cd ~/spark_wordcount/

./sbt compile复制代码

打成jar包

1.

./sbt package复制代码

编译过程，sbt需要上⽹下载依赖⼯具包，jna，scala等。编译完成后可以在target/scala-2.10/⽬录找到 打包好的jar

- 1.
- 2.
- 3.
- 4.


[rot@bd01 scala-2.10]# pwd /usr/local/hadop/spark_wordcount/target/scala-2.10 [rot@bd01 scala-2.10]# ls cache clases wordcount_2.10-1.0.0.jar复制代码

WordCount执⾏ 可以参考Spark分布式运⾏于hadoop的yarn上的⽅法，写⼀个执⾏脚本

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


#!/usr/bin/env bash

SPARK_JAR=./asembly/target/scala-2.10/spark-asembly_2.10-0.9.0-incubatinghadop2.2.0.jar \

./bin/spark-clas org.apache.spark.deploy.yarn.Client \

- -jar ~/spark_wordcount/target/scala-2.10/wordcount_2.10-1.0.0.jar \
- -clas spark.example.WordCount \
- -args yarn-standalone \
- -args /testWordCount.txt \
- -args /resultWordCount \
- -num-workers 3 \
- -master-memory 4g \
- -worker-memory 2g \
- -worker-cores 2复制代码


然后，拷⻉⼀个名为testWordCount.txt的⽂件进hdfs

1.

hdfs dfs -copyFromLocal ./testWordCount.txt /testWordCount.txt复制代码

然后执⾏脚本，过⼀会就可以看到结果了

