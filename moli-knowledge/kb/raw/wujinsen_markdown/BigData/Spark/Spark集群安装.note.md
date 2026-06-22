准备两台以上Linux服务器，安装好JDK1.7

- 1.1.1. 下载Spark

htp:/ w.apache.org/dyn/closer.lua/spark/spark-1.5.2/spark-1.5.2-bin-hadop2.6.tgz 上传解压

- 1.1.2. 配置Spark


安装包

安装包 上传spark-1.5.2-bin-hadop2.6.tgz安装包到Linux上 解压安装包到指定位置

tar -zxvfspark-1.5.2-bin-hadop2.6.tgz -C /usr/local

进⼊到Spark安装⽬录

cd/usr/local/spark-1.5.2-bin-hadop2.6

进⼊conf⽬录并重命名并修改spark-env.sh.template⽂件

cd conf/ mvspark-env.sh.template spark-env.sh vi spark-env.sh

在该配置⽂件中添加如下配置

exportJAVA_HOME=/usr/java/jdk1.7.0_45 exportSPARK_MASTER_IP=node1 /说明 master节点配置 exportSPARK_MASTER_PORT=707

保存退出 重命名并修改slaves.template⽂件

mv slaves.templateslaves vi slaves

在该⽂件中添加⼦节点所在的位置（Worker节点）

- node2
- node3
- node4 保存退出


将配置好的Spark拷贝到其他节点上

scp -rspark-1.5.2-bin-hadop2.6/ node2:/usr/local/ scp -r spark-1.5.2-bin-hadop2.6/node3:/usr/local/ scp -rspark-1.5.2-bin-hadop2.6/ node4:/usr/local/

Spark集群配置完毕，⽬前是1个Master，3个Work，在node1上启动Spark集群

/usr/local/spark-1.5.2-bin-hadop2.6/sbin/start-all.sh

启动后执⾏jps命令，主节点上有Master进程，其他⼦节点上有Work进⾏，登录Spark管理界⾯查看集 群状态（主节点）：

htp:/node1 8080

到此为⽌，Spark集群安装完毕，但是有⼀个很⼤的问题，那就是Master节点存在单点故障，要解决此 问题，就要借助zokeper，并且启动⾄少两个Master节点来实现⾼可靠，配置⽅式⽐较简单： Spark集群规划：node1，node2是Master；node3，node4，node5是Worker 安装配置zk集群，并启动zk集群 停⽌spark所有服务，修改配置⽂件spark-env.sh，在该配置⽂件中删掉SPARK_MASTER_IP并添加如 下配置 exportSPARK_DAEMON_JAVA_OPTS="-Dspark.deploy.recoveryMode=ZOKEPERDspark.deploy.zokeper.url=zk1,zk2,zk3-Dspark.deploy.zokeper.dir=/spark"

- 1.在node1节点上修改slaves配置⽂件内容指定worker节点
- 2.在node1上执⾏sbin/start-al.sh脚本，然后在node2上执⾏sbin/start-master.sh启动第⼆个Master


1. 执⾏Spark

程序

- 1.1. 执⾏第⼀个spark程序

/usr/local/spark-1.5.2-bin-hadop2.6/bin/spark-submit \--clas org.apache.spark.examples.SparkPi \--master spark://node1 7077 \--executor-memory 1G \--total-executor-cores 2 \/usr/local/spark-1.5.2-bin-hadop2.6/lib/spark-examples-1.5.2-hadop2.6.0.jar

\10该算法是利⽤蒙特·卡罗算法求PI

- 1.2. 启动Spark Shel


## spark-shell是Spark⾃带的交互式Shell程序，⽅便⽤户进⾏交 互式编程，⽤户可以在该命令⾏下⽤scala编写spark程序。

- 1.2.1. 启动spark shel


/usr/local/spark-1.5.2-bin-hadop2.6/bin/spark-shell \--master spark://node1 7077 \--executor-memory 2g \--total-executor-cores

2参数说明： -master spark://node1 707指定Master的地址 -executor-memory 2g指定每个worker可⽤内存为2G-total-executor-cores2 指定整个集群使⽤的cup核数为2个 注意：如果启动spark shell时没有指定master地址，但是也可以正常启动sparkshell 和执⾏sparkshell中的程序，其实是启动了spark的local模 式，该模式仅在本机启动⼀个进程，没有与集群建⽴联 系。 SparkShell中已经默认将SparkContext类初始化为对象 sc。⽤户代码如果需要⽤到，则直接应⽤sc即可

- 1.2.2. 在spark shel


中编写WordCount程序

- ⾸先启动hdfs向hdfs上传⼀个⽂件到 hdfs://node1 9 0/words.txt在sparkshell中⽤scala语⾔编 写spark程序sc.textFile("hdfs://node1 9 0/words.txt").flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_).saveAsTextFile("hdfs://node1 9 0/out")使⽤hdfs命令查看结果hdfs dfs -ls hdfs://node1 9 0/out/p*说明：sc是SparkContext对象，该对象时提交 spark程序的⼊⼜textFile(hdfs://node1 9 0/words.txt)是 hdfs中读取数据flatMap(_.split("")先map在压平map(_,1) 将单词和1构成元组reduceByKey(_+_)按照key进⾏reduce， 并将value累加saveAsTextFile("hdfs://node1 9 0/out")将 结果写⼊到hdfs中
- 1.3. 在IDEA中编写WordCount程序


sparkshell仅在测试和验证我们的程序时使⽤的较多，在⽣产 环境中，通常会在IDE中编制程序，然后打成jar包，然后提交 到集群，最常⽤的是创建⼀个Maven项⽬，利⽤Maven来管理 jar包的依赖。 1.创建⼀个项⽬ 2.选择Maven项⽬，然后点击 next3.填写maven的GAV，然后点击next填写项⽬名称，然后 点击finish5.创建好maven项⽬后，点击Enable Auto-Import配置Maven的pom.xml<?xml version="1.0"encoding="UTF-8"?> <projectxmlns=" "

http://maven.apache.org/POM/4.0.0 http:// w.w3.org/201/XMLSchema-instance

xmlns:xsi=" "

http://maven.apache.org/POM/4.0.0http://ma ven.apache.org/xsd/maven-4.0.0.xsd

xsi:schemaLocation="

">

<modelVersion>4.0.0</modelVersion> <groupId>cn.test.spark</groupId> <artifactId>spark-mvn</artifactId> <version>1.0-SNAPSHOT</version> <properties>

<maven.compiler.source>1.7</maven.compiler.source> <maven.compiler.target>1.7</maven.compiler.target> <encoding>UTF-8</encoding> <scala.version>2.10.6</scala.version> <scala.compat.version>2.10</scala.compat.version>

</properties> <dependencies>

<dependency> <groupId>org.scala-lang</groupId> <artifactId>scala-library</artifactId> <version>${scala.version}</version>

</dependency> <dependency>

<groupId>org.apache.spark</groupId> <artifactId>spark-core_2.10</artifactId> <version>1.5.2</version>

</dependency> <dependency>

<groupId>org.apache.spark</groupId> <artifactId>spark-streaming_2.10</artifactId>

- <version>1.5.2</version>

</dependency> <dependency>

<groupId>org.apache.hadop</groupId> <artifactId>hadop-client</artifactId>

- <version>2.6.2</version>


</dependency> </dependencies> <build>

<sourceDirectory>src/main/scala</sourceDirectory> <testSourceDirectory>src/test/scala</testSourceDirectory> <plugins>

<plugin> <groupId>net.alchim31.maven</groupId> <artifactId>scala-maven-plugin</artifactId> <version>3.2.0</version> <executions>

<execution> <goals> <goal>compile</goal> <goal>testCompile</goal>

</goals> <configuration>

<args>

<arg>-make:transitive</arg> <arg>-dependencyfile</arg> <arg>${project.build.directory}/.scala_dependencies</arg>

</args>

</configuration> </execution>

</executions> </plugin> <plugin>

<groupId>org.apache.maven.plugins</groupId> <artifactId>maven-surefire-plugin</artifactId> <version>2.18.1</version> <configuration>

<useFile>false</useFile> <disableXmlReport>true</disableXmlReport> <includes>

<include>*/*Test.*</include> <include>*/*Suite.*</include>

</includes>

</configuration> </plugin> <plugin>

<groupId>org.apache.maven.plugins</groupId> <artifactId>maven-shade-plugin</artifactId> <version>2.3</version>

<executions>

<execution> <phase>package</phase> <goals>

<goal>shade</goal> </goals> <configuration>

<filters>

<filter> <artifact>*:*</artifact> <excludes>

<exclude>META-INF/*.SF</exclude> <exclude>META-INF/*.DSA</exclude> <exclude>META-INF/*.RSA</exclude>

</excludes>

</filter> </filters> <transformers>

<transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">

<mainClas>cn.test.spark.WordCount</mainClas> </transformer>

</transformers> </configuration>

</execution> </executions>

</plugin> </plugins>

</build></project>将src/main/java和src/test/java分别修改成 src/main/scala和src/test/scala，与pom.xml中的配置保持⼀ 致 新建⼀个scalaclas，类型为Object编写spark程序package cn.test.sparkimportorg.apache.spark.{SparkContext, SparkConf}objectWordCount {

defmain(args: Aray[String]) {

/创 建 SparkConf()并 设 置 Ap名 称

valconf =newSparkConf().setApName("WC")

/创 建 SparkContext，该 对 象 是 提 交 spark Ap的 ⼊ ⼝ valsc =newSparkContext(conf)

/使 ⽤ sc创 建 RD并 执 ⾏ 相 应 的 transformation和 action

sc.textFile(args(0).flatMap(_.split(" ").map(_,1).reduceByKey(_+_,1).sortBy(_._2, false).saveAsTextFile(args(1)

/停 ⽌ sc，结 束 该 任 务 sc.stop()

}

# }//创建SparkConf

# val conf = new SparkConf().setAppName("WordCount").setMaster("local")//本地 模式//.setMaster("spark://hadoop01:7077") //spark提交程序的路⼜SparkContext

val sc = new SparkContext(conf)//本地模式引⽤jar包

sc.addJar("D:\\Users\\wjs\\IdeaProjects\\BigData\\target\\big data-2.0.jar")

//调⽤sparkContext的⽅法操作RDD val rdd1 = sc.textFile("d://words.txt") val p0 = rdd1.partitions(0) val prefer = rdd1.preferredLocations(p0) println("prefer:" +prefer) val result = rdd1.ﬂatMap(_.split(" ")).map((_,

1)).reduceByKey(_+_, 1).sortBy(_._2).collect()// rdd1.ﬂatMap(_.split("

# ")).map((_,1)).reduceByKey(_+_,

1).sortBy(_._2).saveAsTextFile(args(1)) println(result.toBuffer) sc.stop() 使⽤Maven打包：⾸先修改pom.xml中的main clas点击idea右侧的MavenProject选项 点击Lifecycle,选择 clean和package，然后点击RunMavenBuild选择编译成功的

## jar包，并将该jar上传到Spark集群中的某个节点上 ⾸先启动 hdfs和Spark集群启动hdfs/usr/local/hadop-2.6.1/sbin/start-dfs.sh启动 spark/usr/local/spark-1.5.2-bin-hadop2.6/sbin/start-all.sh使⽤spark-submit命令提交 Spark应⽤（注意参数的顺序）/usr/local/spark-1.5.2-bin-hadop2.6/bin/spark-submit

\--clas cn.test.spark.WordCount \--master spark://node1 7077 \--executor-memory 2G \--total-executor-cores

- 4 \/rot/spark-mvn-1.0-SNAPSHOT.jar \hdfs://node1 9 0/words.txt


\hdfs://node1 9 0/out查看程序执⾏结果hdfs dfs -cat hdfs://node1 9 0/out/part- 0(hello,6)(tom,3)(kitty,2)(jery,1) bin/sparkshell -clas cn.test.day3.JdbcR Demo -master spark://hadop01 707/rot/bigdata-2.0.jar

-jars/rot/mysql-conector-java-5.1.28.jar -driverclas-path /rot/mysql-conector-java-5.1.28.jar

