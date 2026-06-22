折腾了⼏天，终于把Spark 集群安装成功了，其实⽐hadop要简单很多，由于⽹上搜索到的博客⼤部 分都还停留在需要依赖mesos的版本，⾛了不少弯路。

# 1.安装 JDK1.7

- 1 yum search openjdk-devel

- 2 sudo yum install java-1.7.0-openjdk-devel.x86_64

- 3 /usr/sbin/alternatives --config java

- 4 /usr/sbin/alternatives --config javac

- 5 sudo vim /etc/profile

- 6 # add the following lines at the end

- 7 export JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.19.x86_64

- 8 export JRE_HOME=$JAVA_HOME/jre

- 9 export PATH=$PATH:$JAVA_HOME/bin

- 10 export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

- 11 # save and exit vim

- 12 # make the bash profile take effect immediately

- 13 $ source /etc/profile

- 14 # test

- 15 $ java -version

- 16


安装和配置CentOS服务器的详细步骤

参考我的另⼀篇博客， 。

# 2.安装 Scala2.9.3

Spark 0.7.2 依赖 Scala 2.9.3, 我们必须要安装Scala 2.9.3. 下载 并 保存到home⽬录.

scala-2.9.3.tgz

- 1 $ tar -zxf scala-2.9.3.tgz

- 2 $ sudo mv scala-2.9.3 /usr/lib

- 3 $ sudo vim /etc/profile

- 4 # add the following lines at the end

- 5 export SCALA_HOME=/usr/lib/scala-2.9.3

- 6 export PATH=$PATH:$SCALA_HOME/bin

- 7 # save and exit vim

- 8 #make the bash profile take effect immediately

- 9 source /etc/profile

- 10 # test

- 11 $ scala -version

- 12


# 3.下载预编译好的Spark

下载预编译好的Spark, . 如果你想从零开始编译，则下载源码包，但是我不建议你这么做，因为有⼀个Maven仓库， twiter4j.org, 被墙了，导致编译时需要翻墙，⾮常麻烦。如果你有DIY精神，并能顺利翻墙，则可以试 试这种⽅式。

spark-0.7.2-prebuilt-hadop1.tgz

# 4.本地模式

## 4.1解压

- 1 $ tar -zxf spark-0.7.2-prebuilt-hadoop1.tgz

- 2


## 4.2设置SPARK_EXAMPLES_JAR环境变量

- 1 $ vim ~/.bash_profile

- 2 # add the following lines at the end

export SPARK_EXAMPLES_JAR=$HOME/spark-0.7.2/examples/target/scala-2.9.3/sparkexamples_2.9.3-0.7.2.jar

- 3

- 4 # save and exit vim

- 5 #make the bash profile take effect immediately

- 6 $ source /etc/profile

- 7


这⼀步其实最关键，很不幸的是，官⽅⽂档和⽹上的博客，都没有提及这⼀点。我是偶然看到了这两 篇帖⼦， ,

Runing SparkPi Nul pointer exception when runing ./run spark.examples.SparkPi local

，才补上了这⼀步，之前死活都⽆法运⾏SparkPi。

## 4.3（可选）设置 SPARK_HOME环境变量，并将SPARK_HOME/bin加⼊ PATH

- 1 $ vim ~/.bash_profile

- 2 # add the following lines at the end

- 3 export SPARK_HOME=$HOME/spark-0.7.2

- 4 export PATH=$PATH:$SPARK_HOME/bin

- 5 # save and exit vim

- 6 #make the bash profile take effect immediately

- 7 $ source /etc/profile

- 8


## 4.4现在可以运⾏SparkPi了

- 1 $ cd ~/spark-0.7.2

- 2 $ ./run spark.examples.SparkPi local

- 3


# 5.集群模式

## 5.1安装Hadop

⽤VMware Workstation 创建三台CentOS 虚拟机，hostname分别设置为 master, slave01, slave02， 设置 SH⽆密码登陆，安装hadop，然后启动hadop集群。参考我的这篇博客，

在CentOS上安装Ha dop

.

- 5.2 Scala


在三台机器上都要安装 Scala 2.9.3 , 按照第2节的步骤。JDK在安装Hadop时已经安装了。

- 5.3在master上安装并配置Spark


解压

- 1 $ tar -zxf spark-0.7.2-prebuilt-hadoop1.tgz

- 2


设置SPARK_EXAMPLES_JAR 环境变量

- 1 $ vim ~/.bash_profile

- 2 # add the following lines at the end

export SPARK_EXAMPLES_JAR=$HOME/spark-0.7.2/examples/target/scala-2.9.3/sparkexamples_2.9.3-0.7.2.jar

- 3

- 4 # save and exit vim

- 5 #make the bash profile take effect immediately

- 6 $ source /etc/profile

- 7


在 in conf/spark-env.sh 中设置 SCALA_HOME

- 1 $ cd ~/spark-0.7.2/conf

- 2 $ mv spark-env.sh.template spark-env.sh

- 3 $ vim spark-env.sh

- 4 # add the following line

- 5 export SCALA_HOME=/usr/lib/scala-2.9.3

- 6 # save and exit

- 7


在 conf/slaves , 添加Spark worker的hostname, ⼀⾏⼀个。

- 1 $ vim slaves

- 2 slave01

- 3 slave02

- 4 # save and exit

- 5


（可选）设置 SPARK_HOME环境变量，并将SPARK_HOME/bin加⼊PATH

- 1 $ vim ~/.bash_profile

- 2 # add the following lines at the end

- 3 export SPARK_HOME=$HOME/spark-0.7.2

- 4 export PATH=$PATH:$SPARK_HOME/bin

- 5 # save and exit vim

- 6 #make the bash profile take effect immediately

- 7 $ source /etc/profile

- 8


## 5.4在所有worker上安装并配置Spark

既然master上的这个⽂件件已经配置好了，把它拷⻉到所有的worker。注意，三台机器spark所在⽬ 录必须⼀致，因为master会登陆到worker上执⾏命令，master认为worker的spark路径与⾃⼰⼀ 样。

- 1 $ cd

- 2 $ scp -r spark-0.7.2 dev@slave01:~

- 3 $ scp -r spark-0.7.2 dev@slave02:~

- 4
- 5.5启动 Spark集群


按照第5.3节设置 SPARK_EXAMPLES_JAR 环境变量，配置⽂件不⽤配置了，因为是直接从master复制过 来的，已经配置好了。

在master上执⾏

- 1 $ cd ~/spark-0.7.2

- 2 $ bin/start-all.sh

- 3


检测进程是否启动

- 1 $ jps

- 2 11055 Jps

- 3 2313 SecondaryNameNode

- 4 2409 JobTracker

- 5 2152 NameNode

- 6 4822 Master

- 7


浏览master的web UI(默认 ). 这是你应该可以看到所有的word节点，以及他们的 CPU个数和内存等信息。 #5.6 运⾏SparkPi例⼦

### htp:/localhost:8080

- 1 $ cd ~/spark-0.7.2

- 2

- 3 $ ./run spark.examples.SparkPi spark://master:7077

- 4

- 5


1 (此处我在直接操作的时候会报错

Exception in thread "main" java.lang.NumberFormatException: For input string: "spark://192.168.2.160:7077"

- 1

- 2


at java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)

- 1

- 2


- 1 at java.lang.Integer.parseInt(Integer.java:492)

- 2


- 1 at java.lang.Integer.parseInt(Integer.java:527)

- 2


at scala.collection.immutable.StringLike$class.toInt(StringLike.scala:229)

1

1 采⽤的办法 ：

vi spark-defaults.conf 添加 spark.master spark:/hbase01 707

1 )

（可选）运⾏⾃带的例⼦，SparkLR 和 SparkKMeans.

- 1 #Logistic Regression

- 2 #./run spark.examples.SparkLR spark://master:7077

- 3 #kmeans

- 4 $ ./run spark.examples.SparkKMeans spark://master:7077 ./kmeans_data.txt 2 1

- 5


## 5.7从HDFS读取⽂件并运⾏WordCount

- 1 $ cd ~/spark-0.7.2

- 2 $ hadoop fs -put README.md .

- 3 $ MASTER=spark://master:7077 ./spark-shell

- 4 scala> val file = sc.textFile("hdfs://master:9000/user/dev/README.md")

scala> val count = file.flatMap(line => line.split(" ")).map(word => (word, 1)).reduceByKey(_+_)

- 5

- 6 scala> count.collect()

- 7


## 5.8停⽌ Spark集群

- 1 $ cd ~/spark-0.7.2

- 2 $ bin/stop-all.sh


