此系统是ubuntu， 版本是1.0.0（下载的不是源码，⽽是编译好的，在我的其他⽂章 ⾥有下载⽹盘地址）， 版本2.2.0， 版本2.10.4

# Spark Hadoop Scala

- 1.spark-sunbmit命令：spark1.0之前的版本运⾏⾃带例⼦使⽤$SPARK_HOME/bin/runexample命令，对于spark1.0以后做出了改进，使⽤$SPARK_HOME/bin/spark-submit命 令。其实，spark-shell⽤到的就是通过spark-submit，之后调⽤spark-class函数来完成这些 命令。具体可看下spark-submit.sh，会找到spark-class。

- 2.运⾏模式有很多模式，这⾥就不⼀⼀介绍模式的运⾏原理，具体可去看下书。只看下运⾏ 模式的命令和结果。模式有：（1）local（本地）模式，（2）standalone模式，（3）onyarn-cluster（on-yarn-standalone）模式，（4）on-yarn-client模式。


- 2.1 local模式 在安装的spark⽬录下敲⼊命令：./bin/spark-submit --

class org.apache.spark.examples.SparkPi --master local lib/spark-examples-1.0.0hadoop2.2.0.jar 解释下命令：

--class 类名

--master local 本地模式

lib/spark-examples-1.0.0-hadoop2.2.0.jar 是你spark安装⽬录下的lib⽬录下的examples的 jar包，其实以后在windows上⽤intellij写的spark程序也要打成jar包，放到这⾥来调⽤。 运⾏结果如下图：

- 2.2 standalone模式 ⾸先进⼊spark安装⽬录下，启动./sbin/start-all.sh，输⼊jps，主节点看到Master,从节点看 到worker 在安装的spark⽬录下敲⼊命令：./bin/spark-submit -class org.apache.spark.examples.SparkPi --master spark://192.168.123.101:7077 lib/spark-examples-1.0.0-hadoop2.2.0.jar


![image 1](<各模式下运行spark自带实例SparkPi.note_images/imageFile1.png>)

解释下命令：

--master spark://192.168.123.101:7077 这个IP是要写你在spark的conf⽬录下配置的

export SPARK_MASTER_IP的地址。

前三种⽅式不需要启动hadoop，spark有⾃⼰的资源管理模式

- 2.3 on-yarn-cluster模式 ⼤前提是master的机器和slave机器的系统时间要保持⼀致，不⼀致会有错。 ⾸先要进⼊hadoop安装⽬录下，启动./sbin/start-all.sh，看到主节点和namenode secondarynamenode resourcemanager master和从节点有datanode nodemanager worker 在安装的spark⽬录下敲⼊命令：./bin/spark-submit -class org.apache.spark.examples.SparkPi --master yarn-cluster lib/spark-examples-1.0.0hadoop2.2.0.jar

- 2.4 on-yarn-client模式 ⼤前提是master的机器和slave机器的系统时间要保持⼀致，不⼀致会有错。 要求和 on-yarn-cluster模式⼀样。 在安装的spark⽬录下敲⼊命令：./bin/spark-submit -class org.apache.spark.examples.SparkPi --master yarn-client lib/spark-examples-1.0.0hadoop2.2.0.jar


## 所有运⾏模式命令都是参考Submitting Applications ⽹址：http://spark.apache.org/docs/lat est/submitting-applications.html

