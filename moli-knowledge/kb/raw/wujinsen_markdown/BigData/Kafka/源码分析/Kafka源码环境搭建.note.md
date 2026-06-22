本⽂主要讲述的是如何搭建Kafka的源码环境，主要针对的Windows操作系统下IntelliJ IDEA编译器， 其余操作系统或者IDE可以类推。

# 1.安装和配置JDK

确认JDK版本⾄少为1.7，最好是1.8及以上。使⽤java -version命令来查看当前JDK的版本，示例如 下：

C:\Users\hidden> java -version java version "1.8.0_112" Java(TM) SE Runtime Environment (build 1.8.0_112-b15) Java HotSpot(TM) 64-Bit Server VM (build 25.112-b15, mixed mode)

# 2.下载并安装配置Gradle

下载地址为： ，笔者使⽤的版本是3.1。⼀般只需要将下载的包解压，然后 再将$GRADLE_HOME/bin的路径添加到环境变量Path中即可，其中$GRADLE_HOME指的是Gradle 的根⽬录。可以使⽤gradle -v命令来验证Gradle是否已经配置完成，示例如下：

https://gradle.org/releases/

C:\Users\hidden>gradle -v

-----------------------------------------------------------Gradle 3.1

------------------------------------------------------------

Build time: 2016-09-19 10:53:53 UTC Revision: 13f38ba699afd86d7cdc4ed8fd7dd3960c0b1f97

Groovy: 2.4.7 Ant: Apache Ant(TM) version 1.9.6 compiled on June 29 2015 JVM: 1.8.0_112 (Oracle Corporation 25.112-b15) OS: Windows 10 10.0 amd64

# 3.下载并安装配置Scala

下载地址为： ，⽬前最新的版本是2.12.4，不过笔者这⾥ 使⽤的版本是2.11.11。如Gradle⼀样，只需要解压并将$SCALA_HOME/bin的路径添加到环境变量 Path即可，其中$SCALA_HOME指的是Scala的根⽬录。可以使⽤scala -version命令来验证scala是否 已经配置完成，示例如下：

http://www.scala-lang.org/download/all.html

C:\Users\hidden>scala -version Scala code runner version 2.11.11 -- Copyright 2002-2017, LAMP/EPFL

# 4. 构建Kafka源码环境

Kafka下载地址为： ，⽬前最新的版本是1.0.0。将下载的压缩包解 压，并在Kafka的根⽬录执⾏gradle idea命令进⾏构建，如果你使⽤的是Eclipse，则只需采⽤gradle eclipse命令构建即可。构建细节如下所示：

http://kafka.apache.org/downloads

D:\IntelliJ IDEA Files\kafka-sources\kafka-1.0.0-src>gradle idea Starting a Gradle Daemon, 2 incompatible and 1 stopped Daemons could not be reused, use -status for details Building project 'core' with Scala version 2.11.11 :ideaModule :ideaProject (......省略若⼲......) :streams:examples:ideaModule :streams:examples:idea

BUILD SUCCESSFUL

Total time: 1 mins 11.991 secs

之后将Kafka导⼊到IDEA中即可。不过这样还没有结束，对于IDEA⽽⾔，还需要安装Scala插件，在 Setting->Plugin中搜索scala并安装，可以参考下图，笔者这⾥是已经安装好的状态：

![image 1](<Kafka源码环境搭建.note_images/imageFile1.png>)

这⾥写图⽚描述

# 5. 配置Kafka源码环境

前⾯⼏个步骤执⾏完成后就可以很舒适的阅读Kafka的源码，但是如果需要启动Kafka的服务还需要⼀ 些额外的步骤。 ⾸先确保gradle.properties配置⽂件中的scalaVersion与安装的⼀致。gradle.properties配置⽂件的细节 如下：

group=org.apache.kafka # NOTE: When you change this version number, you should also make sure to update # the version numbers in tests/kafkatest/__init__.py and kafka-merge-pr.py. version=1.0.0 scalaVersion=2.11.11 task=build org.gradle.jvmargs=-XX:MaxPermSize=512m -Xmx1024m -Xss2m

如果更改了scalaVersion，需要重新执⾏gradle idea命令来重新构建。虽然很多时候在操作系统中安装 其他版本的Scala也并没有什么问题，⽐如安装2.12.4版本。但是有些情况下运⾏Kafka时会出现⼀些异 常，⽽这些异常却⼜是由于Scala版本不⼀致⽽引起的，⽐如会出现下⾯示例中的报错：

- [2017-11-13 17:09:21,119] FATAL (kafka.Kafka$) java.lang.NoSuchMethodError: scala.collection.TraversableOnce.$init$(Lscala/collection/TraversableOnce;)V


at kafka.message.MessageSet.<init>(MessageSet.scala:72) at kafka.message.ByteBufferMessageSet.<init>(ByteBufferMessageSet.scala:129) at kafka.message.MessageSet$.<init>(MessageSet.scala:32) at kafka.message.MessageSet$.<clinit>(MessageSet.scala) at kafka.server.Defaults$.<init>(KafkaConfig.scala:52) at kafka.server.Defaults$.<clinit>(KafkaConfig.scala) at kafka.server.KafkaConfig$.<init>(KafkaConfig.scala:686) at kafka.server.KafkaConfig$.<clinit>(KafkaConfig.scala) at kafka.server.KafkaServerStartable$.fromProps(KafkaServerStartable.scala:28) at kafka.Kafka$.main(Kafka.scala:82) at kafka.Kafka.main(Kafka.scala)

所以为了省去⼀些不必要的麻烦，还是建议读者在安装Scala版本之前先查看下Kafka源码中 gradle.properties⽂件中配置的scalaVersion。 再确保了scalaVersion之后，需要将config⽬录下的log4j.properties⽂件拷⻉到core/src/main/scala⽬录 下，这样可以让Kafka在运⾏时能够输出⽇志信息，可以参考下图：

![image 2](<Kafka源码环境搭建.note_images/imageFile2.png>)

这⾥写图⽚描述

之后还需要配置server.properties⽂件，⼀般只需要修改以下⼀些配置项：

# 是否允许topic被删除，设置为true则topic可以被删除， # 开启这个功能⽅便Kafka在运⾏⼀段时间之后，能够删除⼀些不需要的临时topic delete.topic.enable=true # 禁⽤⾃动创建topic的功能 auto.create.topics.enable=false

# 存储log⽂件的⽬录，默认值为/tmp/kafka-logs # 示例是在Windows环境下运⾏，所以需要修改这个配置，注意这⾥的双反斜杠。 log.dir=D:\\kafka\\tmp\\kafka-logs # 配置kafka依赖的zookeeper路径地址，这⾥的前提是在本地开启了⼀个zookeeper的服务 # 如果本地没有zookeeper服务，可以参考下⼀节中zookeeper的安装、配置及运⾏ zookeeper.connect=localhost:2181/kafka

之后配置Kafka的启动参数，详细参考下图：

![image 3](<Kafka源码环境搭建.note_images/imageFile3.png>)

这 ⾥ 配 置 Main class 为 kafka.Kafka ， 并 制 定 启 动 时 所 需 要 的 配 置 ⽂ 件 地 址 ， 即 ： config/server.properties。配置JMX_PORT是为了⽅便搜集Kafka⾃身的Metrics数据。 如此便可以顺利的运⾏Kafka服务了（第⼀次启动时会有⼀个耗时较⻓的编译过程），部分启动⽇志如 下：

- [2017-11-14 00:24:14,472] INFO KafkaConfig values: advertised.host.name = null advertised.listeners = null advertised.port = null authorizer.class.name =


(......省略若⼲......) [2017-11-14 00:24:35,001] INFO Registered broker 0 at path /brokers/ids/0 with addresses: EndPoint(LAPTOP-1IN9UPT7,9092,ListenerName(PLAINTEXT),PLAINTEXT) (kafka.utils.ZkUtils)

- [2017-11-14 00:24:35,019] INFO Kafka version : 1.0.0 (org.apache.kafka.common.utils.AppInfoParser)

- [2017-11-14 00:24:35,020] INFO Kafka commitId : e89bffd6b2eff799 (org.apache.kafka.common.utils.AppInfoParser)

- [2017-11-14 00:24:35,021] INFO [Kafka Server 0], started (kafka.server.KafkaServer)


# 6. Zookeeper的安装、配置及启动

Kafka需要使⽤Zookeeper来管理元数据，⽐如记录topic、partitions（分区）以及replica（副本）的分 配信息。由于这⾥只是阐述如何构建Kafka的源码环境搭建，所以这⾥的Zookeeper的安装也以极简为 主，即采⽤单机配置。Zookeeper下载地址为： ，下载之后 解 压 ， 然 后 将 $ZOOKEEPER_HOME ⽬ 录 下 的 conf/zoo_sample.cfg 重 命 名 为 zoo.cfg ， 其 中 $ZOOKEEPER_HOME指的是ZooKeeper的根⽬录。

http://zookeeper.apache.org/releases.html

修改$ZOOKEEPER_HOME/conf/zoo.cfg配置，示例配置如下（其余配置可以不做修改）：

dataDir=D:\\zookeeper-3.4.10\\tmp\\zookeeper\\data

1

将$ZOOKEEPER_HOME/bin配置到Path中，之后直接运⾏zkServer命令即可开启Zookeeper服务。示 例如下：

C:\Users\hidden>zkServer

C:\Users\hidden>call "C:\Program Files\Java\jdk1.8.0_112"\bin\java "Dzookeeper.log.dir=D:\zookeeper-3.4.10\bin\.." "-Dzookeeper.root.logger=INFO,CONSOLE" -cp "D:\zookeeper-3.4.10\bin\..\build\classes;D:\zookeeper3.4.10\bin\..\build\lib\*;D:\zookeeper-3.4.10\bin\..\*;D:\zookeeper3.4.10\bin\..\lib\*;D:\zookeeper-3.4.10\bin\..\conf" org.apache.zookeeper.server.quorum.QuorumPeerMain "D:\zookeeper-3.4.10\bin\..\conf\zoo.cfg" 2017-11-14 00:44:20,135 [myid:] - INFO [main:QuorumPeerConfig@134] - Reading configuration from: D:\zookeeper-3.4.10\bin\..\conf\zoo.cfg

- 2017-11-14 00:44:20,147 [myid:] - INFO [main:DatadirCleanupManager@78] autopurge.snapRetainCount set to 3

- 2017-11-14 00:44:20,147 [myid:] - INFO [main:DatadirCleanupManager@79] autopurge.purgeInterval set to 0 2017-11-14 00:44:20,147 [myid:] - INFO [main:DatadirCleanupManager@101] - Purge task is not scheduled. 2017-11-14 00:44:20,150 [myid:] - WARN [main:QuorumPeerMain@113] - Either no config or no quorum defined in config, running in standalone mode 2017-11-14 00:44:20,250 [myid:] - INFO [main:QuorumPeerConfig@134] - Reading configuration from: D:\zookeeper-3.4.10\bin\..\conf\zoo.cfg 2017-11-14 00:44:20,250 [myid:] - INFO [main:ZooKeeperServerMain@96] - Starting server


## 欢迎⽀持笔者新书：《RabbitMQ实战指南》以及关注微信公众号：朱⼩厮的博客。

