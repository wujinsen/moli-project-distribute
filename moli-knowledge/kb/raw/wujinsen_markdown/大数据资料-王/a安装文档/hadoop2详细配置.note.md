# 前⾔

hadop是分布式系统，运⾏在linux之上，配置起来相对复杂。对于hadop1，很多同学就因为不能 搭建正确的运⾏环境，导致学习兴趣锐减。不过，我有免费的学习视频下载，请点击 。

这⾥

hadop2出来后，解决了hadop1的⼏个固有缺陷，⽐如单点故障、资源利⽤率低、⽀持作业类型 少等问题，结构发⽣了很⼤变化，是hadop未来使⽤的⼀个趋势。当然，配置也更加复杂，⽹上也没 有⼀篇详细的教程来知道⼤家可以轻轻松松搭建起这个环境的。我应该算是第⼀个吧。

# hadop2体系结构

要想理解本节内容，⾸先需要了解hadop1的体系结构。在本博客中和我的视频中都有相关内容，

这⾥不再重复，只讲hadop2的内容。 hadop1的核⼼组成是两部分，即HDFS和MapReduce。在hadop2中变为HDFS和Yarn。 新的HDFS中的NameNode不再是只有⼀个了，可以有多个（⽬前只⽀持2个）。每⼀个都有相同的

职能。

这两个NameNode的地位如何哪？答：⼀个是active状态的，⼀个是standby状态的。当集群运⾏ 时，只有active状态的NameNode是正常⼯作的，standby状态的NameNode是处于待命状态的，时刻 同步active状态NameNode的数据。⼀旦active状态的NameNode不能⼯作，通过⼿⼯或者⾃动切换， standby状态的NameNode就可以转变为active状态的，就可以继续⼯作了。这就是⾼可靠。

当NameNode发⽣故障时，他们的数据如何保持⼀致哪？在这⾥，2个NameNode的数据其实是实 时共享的。新HDFS采⽤了⼀种共享机制，JournalNode集群或者NFS进⾏共享。NFS是操作系统层⾯ 的，JournalNode是hadop层⾯的，我们这⾥使⽤JournalNode集群进⾏数据共享。

如何实现NameNode的⾃动切换哪？这就需要使⽤ZoKeper集群进⾏选择了。HDFS集群中的两 个NameNode都在ZoKeper中注册，当active状态的NameNode出故障时，ZoKeper能检测到这种 情况，它就会⾃动把standby状态的NameNode切换为active状态。

HDFS Federation（HDFS联盟）是怎么回事？答：联盟的出现是有原因的。我们知道NameNode 是核⼼节点，维护着整个HDFS中的元数据信息，那么其容量是有限的，受制于服务器的内存空间。当 NameNode服务器的内存装不下数据后，那么HDFS集群就装不下数据了，寿命也就到头了。因此其扩 展性是受限的。HDFS联盟指的是有多个HDFS集群同时⼯作，那么其容量理论上就不受限了，夸张点 说就是⽆限扩展。

# 配置过程详述

⼤家从官⽹下载的apache hadop2.2.0的代码是32位操作系统下编译的，不能使⽤64位的jdk。我 下⾯部署的hadop代码是⾃⼰的64位机器上重新编译过的。服务器都是64位的，本配置尽量模拟真 实环境。⼤家可以以32位的操作系统做练习，这是没关系的。关于基本环境的详细配置，⼤家可以观 看我的视频，或者浏览 的相关⽂章。

吴超沉思录

在这⾥我们选⽤4台机器进⾏示范，各台机器的职责如下表格所示

<table>
  <tr>
    <th> </th>
    <th>hadop101</th>
    <th>hadop102</th>
    <th>hadop103</th>
    <th>hadop104</th>
  </tr>
  <tr>
    <td>是NameNode吗?</td>
    <td>是，属集群c1</td>
    <td>是，属集群c1</td>
    <td>是，属集群c2</td>
    <td>是，属集群c2</td>
  </tr>
  <tr>
    <td>是DataNode吗？</td>
    <td>是</td>
    <td>是</td>
    <td>是</td>
    <td>是</td>
  </tr>
  <tr>
    <td>是JournalNode 吗？</td>
    <td>是</td>
    <td>是</td>
    <td>是</td>
    <td>不是</td>
  </tr>
  <tr>
    <td>是ZoKeper吗？</td>
    <td>是</td>
    <td>是</td>
    <td>是</td>
    <td>不是</td>
  </tr>
  <tr>
    <td> </td>
    <td>是</td>
    <td>是</td>
    <td>是</td>
    <td>是</td>
  </tr>
</table>


是ZKFC吗?

配置⽂件⼀共包括6个，分别是hadop-env.sh、core-site.xml、hdfs-site.xml、mapred-site.xml、 yarn-site.xml和slaves。除了hdfs-site.xml⽂件在不同集群配置不同外，其余⽂件在四个节点的配置是 完全⼀样的，可以复制。

⽂件hadop-env.sh

就是修改这⼀⾏内容，修改后的结果如下 export JAVA_HOME=/usr/local/jdk 【这⾥的JAVA_HOME的值是jdk的安装路径。如果你那⾥不⼀样，请修改为⾃⼰的地址】

⽂件core-site.xml

<configuration> <property>

<name>fs.defaultFS</name> <value>hdfs:/cluster1</value>

【这⾥的值指的是默认的HDFS路径。当有多个HDFS集群同时⼯作时，⽤户如果不写集群名称，那么 默认使⽤哪个哪？在这⾥指定！该值来⾃于hdfs-site.xml中的配置】 <property>

<name>hadop.tmp.dir</name> <value>/usr/local/hadop/tmp</value>

</property> 【这⾥的路径默认是NameNode、DataNode、JournalNode等存放数据的公共⽬录。⽤户也可以⾃⼰ 单独指定这三类节点的⽬录。】 <property>

<name>ha.zokeper.quorum</name> <value>hadop101 2181,hadop102 2181,hadop103 2181</value>

</property> 【这⾥是ZoKeper集群的地址和端⼝。注意，数量⼀定是奇数，且不少于三个节点】 </configuration>

## 集群c1的⽂件hdfs-site.xml

该⽂件只配置在hadop101和hadop102上。 <configuration> <property> <name>dfs.replication</name> <value>2</value>

</property> 【指定DataNode存储block的副本数量。默认值是3个，我们现在有4个DataNode，该值不⼤于4即 可。】

<property> <name>dfs.nameservices</name> <value>cluster1,cluster2</value>

</property> 【使⽤federation时，使⽤了2个HDFS集群。这⾥抽象出两个NameService实际上就是给这2个HDFS 集群起了个别名。名字可以随便起，相互不重复即可】

<property> <name>dfs.ha.namenodes.cluster1</name> <value>hadop101,hadop102</value>

- 【指定NameService是cluster1时的namenode有哪些，这⾥的值也是逻辑名称，名字随便起，相互不 重复即可】


<property> <name>dfs.namenode.rpc-adres.cluster1.hadop101</name> <value>hadop101 9 0</value>

</property>

- 【指定hadop101的RPC地址】 <property>

<name>dfs.namenode.htp-adres.cluster1.hadop101</name> <value>hadop101 5070</value>

</property>

- 【指定hadop101的htp地址】 <property>

<name>dfs.namenode.rpc-adres.cluster1.hadop102</name> <value>hadop102 9 0</value>

</property>

- 【指定hadop102的RPC地址】 <property>


<name>dfs.namenode.htp-adres.cluster1.hadop102</name> <value>hadop102 5070</value>

</property>

- 【指定hadop102的htp地址】 <property>


<name>dfs.namenode.shared.edits.dir</name> <value>qjournal:/hadop101 8485;hadop102 8485;hadop103 8485/cluster1</value>

</property> 【指定cluster1的两个NameNode共享edits⽂件⽬录时，使⽤的JournalNode集群信息】

<property> <name>dfs.ha.automatic-failover.enabled.cluster1</name> <value>true</value>

</property> 【指定cluster1是否启动⾃动故障恢复，即当NameNode出故障时，是否⾃动切换到另⼀台 NameNode】

<property> <name>dfs.client.failover.proxy.provider.cluster1</name>

<value>org.apache.hadop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value>

【指定cluster1出故障时，哪个实现类负责执⾏故障切换】

<property> <name>dfs.ha.namenodes.cluster2</name> <value>hadop103,hadop104</value>

</property>

- 【指定NameService是cluster2时，两个NameNode是谁，这⾥是逻辑名称，不重复即可。以下配置与 cluster1⼏乎全部相似，不再添加注释】


<property> <name>dfs.namenode.rpc-adres.cluster2.hadop103</name> <value>hadop103 9 0</value>

</property> <property>

<name>dfs.namenode.htp-adres.cluster2.hadop103</name>

- <value>hadop103 5070</value>

</property> <property>

<name>dfs.namenode.rpc-adres.cluster2.hadop104</name>

- <value>hadop104 9 0</value>


</property> <property>

<name>dfs.namenode.htp-adres.cluster2.hadop104</name> <value>hadop104 5070</value>

</property> <!<property>

<name>dfs.namenode.shared.edits.dir</name> <value>qjournal:/hadop101 8485;hadop102 8485;hadop103 8485/cluster2</value>

</property> 【这段代码是注释掉的，不要打开】

->

<property> <name>dfs.ha.automatic-failover.enabled.cluster2</name> <value>true</value>

</property> <property>

<name>dfs.client.failover.proxy.provider.cluster2</name>

<value>org.apache.hadop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value> </property> <property>

<name>dfs.journalnode.edits.dir</name> <value>/usr/local/hadop/tmp/journal</value>

</property> 【指定JournalNode集群在对NameNode的⽬录进⾏共享时，⾃⼰存储数据的磁盘路径】

<property> <name>dfs.ha.fencing.methods</name> <value>shfence</value>

</property> 【⼀旦需要NameNode切换，使⽤ sh⽅式进⾏操作】

<property> <name>dfs.ha.fencing.sh.private-key-files</name> <value>/rot/.sh/id_rsa</value>

</property> 【如果使⽤ sh进⾏故障切换，使⽤ sh通信时⽤的密钥存储的位置】 </configuration>

## 集群c2的⽂件hdfs-site.xml

该⽂件只配置在hadop103和hadop104上。 该⽂件与c1中的hdfs-site.xml配置内容完全相同，只有注释位置不⼀样，⼀定要注意，不要随便改 <configuration> <property> <name>dfs.replication</name> <value>2</value>

</property> <property>

<name>dfs.nameservices</name> <value>cluster1,cluster2</value>

</property> <property>

<name>dfs.ha.namenodes.cluster1</name> <value>hadop101,hadop102</value>

<property> <name>dfs.namenode.rpc-adres.cluster1.hadop101</name> <value>hadop101 9 0</value>

</property> <property>

<name>dfs.namenode.htp-adres.cluster1.hadop101</name>

- <value>hadop101 5070</value>

</property> <property>

<name>dfs.namenode.rpc-adres.cluster1.hadop102</name>

- <value>hadop102 9 0</value>


</property> <property>

<name>dfs.namenode.htp-adres.cluster1.hadop102</name> <value>hadop102 5070</value>

</property> <!<property>

<name>dfs.namenode.shared.edits.dir</name> <value>qjournal:/hadop101 8485;hadop102 8485;hadop103 8485/cluster1</value>

</property> 【这段代码是注释掉的，不要打开】

->

<property> <name>dfs.ha.automatic-failover.enabled.cluster1</name> <value>true</value>

</property> <property>

<name>dfs.client.failover.proxy.provider.cluster1</name>

<value>org.apache.hadop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value> </property> <property>

<name>dfs.ha.namenodes.cluster2</name> <value>hadop103,hadop104</value>

<name>dfs.namenode.rpc-adres.cluster2.hadop103</name> <value>hadop103 9 0</value>

</property> <property>

<name>dfs.namenode.htp-adres.cluster2.hadop103</name>

- <value>hadop103 5070</value>

</property> <property>

<name>dfs.namenode.rpc-adres.cluster2.hadop104</name>

- <value>hadop104 9 0</value>


</property> <property>

<name>dfs.namenode.htp-adres.cluster2.hadop104</name> <value>hadop104 5070</value>

</property> <property>

<name>dfs.namenode.shared.edits.dir</name> <value>qjournal:/hadop101 8485;hadop102 8485;hadop103 8485/cluster2</value>

</property> <property>

<name>dfs.ha.automatic-failover.enabled.cluster2</name> <value>true</value>

</property> <property>

<name>dfs.client.failover.proxy.provider.cluster2</name>

<value>org.apache.hadop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value> </property> <property>

<name>dfs.journalnode.edits.dir</name> <value>/usr/local/hadop/tmp/journal</value>

</property> <property>

<name>dfs.ha.fencing.methods</name> <value>shfence</value>

<name>dfs.ha.fencing.sh.private-key-files</name> <value>/rot/.sh/id_rsa</value>

</property> </configuration>

⽂件mapred-site.xml

<configuration> <property>

<name>mapreduce.framework.name</name> <value>yarn</value>

</property> 【指定运⾏mapreduce的环境是yarn，与hadop1截然不同的地⽅】 </configuration>

⽂件yarn-site.xml

<configuration>

<property> <name>yarn.resourcemanager.hostname</name> <value>hadop101</value>

</property> 【⾃定ResourceManager的地址，还是单点，这是隐患】

<property> <name>yarn.nodemanager.aux-services</name> <value>mapreduce_shufle</value>

</property> </configuration>

⽂件slaves

- hadop101
- hadop102
- hadop103


- hadop104 【指定所有的DataNode节点列表，每⾏⼀个节点名称】 注意：以上配置中c1中的hdfs-site.xml⽂件配置在hadop101和hadop102中，c2中的hdfs-site.xml ⽂件配置在hadop103和hadop104中。其余⽂件在各个节点都相同。


# 启动过程

启动时，要⾮常⼩⼼，请严格按照我这⾥描述的步骤做，每⼀步要检查⾃⼰的操作是否正确。

- 1.⾸先检查各个节点的配置⽂件是否正确
- 2.启动ZoKeper集群


特别要注意hdfs-site.xml⽂件，在c1和c2中是不同的。

关于ZoKeper的集群配置和启动描述⻅ ，这⾥不再详述。 在hadop101、hadop102、hadop103上分别执⾏命令：zkServer.sh start 命令输出(以hadop101为例)： [rot@hadop101 hadop]# zkServer.sh status JMX enabled by default Using config: /usr/local/zokeper/bin/./conf/zo.cfg Mode: folower 三个节点都执⾏完启动命令后，在hadop101执⾏以下验证。 验证： [rot@hadop101 hadop]# zkCli.sh Conecting to localhost:2181 2014-02-12 07 20 35,509 [myid:] - INFO [main:Environment@10] - Client environment:zokeper.version=3.4.5-1392090, built on 09/30/2012 17 52 GMT

吴超沉思录

- 2014-02-12 07 20 35,523 [myid:] - INFO [main:Environment@10] - Client environment:host.name=hadop101
- 2014-02-12 07 20 35,524 [myid:] - INFO [main:Environment@10] - Client environment:java.version=1.7.0_45
- 2014-02-12 07 20 35,525 [myid:] - INFO [main:Environment@10] - Client environment:java.vendor=Oracle Corporation


- 2014-02-12 07 20 35,525 [myid:] - INFO [main:Environment@10] - Client environment:java.home=/usr/local/jdk/jre


- 2014-02-12 07 20 35,526 [myid:] - INFO [main:Environment@10] - Client environment:java.clas.path=/usr/local/zokeper/bin/./build/clases:/usr/local/zokeper/bin/./buil d/lib/*.jar:/usr/local/zokeper/bin/./lib/slf4j-log4j12-1.6.1.jar:/usr/local/zokeper/bin/./lib/slf4j-api-


- 1.6.1.jar:/usr/local/zokeper/bin/./lib/nety-3.2.2.Final.jar:/usr/local/zokeper/bin/./lib/log4j-


- 1.2.15.jar:/usr/local/zokeper/bin/./lib/jline-0.9.94.jar:/usr/local/zokeper/bin/./zokeper-


- 3.4.5.jar:/usr/local/zokeper/bin/./src/java/lib/*.jar:/usr/local/zokeper/bin/./conf:/usr/local/hadop


- 2014-02-12 07 20 35,526 [myid:] - INFO [main:Environment@10] - Client environment:java.library.path=/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
- 2014-02-12 07 20 35,527 [myid:] - INFO [main:Environment@10] - Client environment:java.io.tmpdir=/tmp
- 2014-02-12 07 20 35,528 [myid:] - INFO [main:Environment@10] - Client environment:java.compiler=<NA>

- 2014-02-12 07 20 35,528 [myid:] - INFO [main:Environment@10] - Client environment:os.name=Linux
- 2014-02-12 07 20 35,529 [myid:] - INFO [main:Environment@10] - Client environment:os.arch=amd64

2014-02-12 07 20 35,529 [myid:] - INFO [main:Environment@10] - Client environment:os.version=2.6.32-431.el6.x86_64 2014-02-12 07 20 35,530 [myid:] - INFO [main:Environment@10] - Client environment:user.name=rot

- 2014-02-12 07 20 35,530 [myid:] - INFO [main:Environment@10] - Client environment:user.home=/rot
- 2014-02-12 07 20 35,531 [myid:] - INFO [main:Environment@10] - Client environment:user.dir=/usr/local/hadop 2014-02-12 07 20 35,53 [myid:] - INFO [main:ZoKeper@438] - Initiating client conection, conectString=localhost:2181 sesionTimeout=3 0 watcher=org.apache.zokeper.ZoKeperMain$MyWatcher@10636a7e Welcome to ZoKeper! 2014-02-12 07 20 35,569 [myid:] - INFO [mainSendThread(127.0.0.1 2181):ClientCnxn$SendThread@96] - Opening socket conection to server 127.0.0.1/127.0.0.1 2181. Wil not atempt to authenticate using SASL (unknown eror) 2014-02-12 07 20 35,587 [myid:] - INFO [mainSendThread(127.0.0.1 2181):ClientCnxn$SendThread@849] - Socket conection established to 127.0.0.1/127.0.0.1 2181, initiating sesion JLine suport is enabled




2014-02-12 07 20 35,687 [myid:] - INFO [mainSendThread(127.0.0.1 2181):ClientCnxn$SendThread@1207] - Sesion establishment complete on

- server 127.0.0.1/127.0.0.1 2181, sesionid = 0x65423404e53 0, negotiated timeout = 3 0 WATCHER: WatchedEvent state:SyncConected type:None path:nul [zk: localhost:2181(CONECTED) 0] ls / [zokeper] [zk: localhost:2181(CONECTED) 1] 【可以看到ZK集群 中只有⼀个节点zokeper】


## 3.格式化ZoKeper集群，⽬的是在ZoKeper集群上建⽴HA的相应节 点。

在hadop101上执⾏命令：/usr/local/hadop/bin/hdfs zkfc –formatZK 命令输出： [rot@hadop101 hadop]# /usr/local/hadop/bin/hdfs zkfc -formatZK

- 14/02/12 07 28 56 INFO tols.DFSZKFailoverControler: Failover controler configured for NameNode NameNode at hadop101/192.168.80.101 9 0
- 14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:zokeper.version=3.4.51392090, built on 09/30/2012 17 52 GMT 14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:host.name=hadop101 14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:java.version=1.7.0_45 14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:java.vendor=Oracle Corporation 14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:java.home=/usr/local/jdk/jre


14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:java.clas.path=/usr/local/hadop/etc/hadop:/usr/local/hadop/share/hadop/como n/lib/protobuf-java-2.5.0.jar:/usr/local/hadop/share/hadop/comon/lib/snapy-java-

- 1.0.4.1.jar:/usr/local/hadop/share/hadop/comon/lib/jsch-


- 0.1.42.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-server-
- 1.9.jar:/usr/local/hadop/share/hadop/comon/lib/slf4j-log4j12-

1.7.5.jar:/usr/local/hadop/share/hadop/comon/lib/comons-digester1.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-cli-

- 1.2.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-core-asl-


- 1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-el-


- 1.0.jar:/usr/local/hadop/share/hadop/comon/lib/jets3t-

- 0.6.1.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-core-
- 1.9.jar:/usr/local/hadop/share/hadop/comon/lib/slf4j-api-


- 1.7.5.jar:/usr/local/hadop/share/hadop/comon/lib/xz-

- 1.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-lang-
- 2.5.jar:/usr/local/hadop/share/hadop/comon/lib/jasper-runtime-

- 5.5.23.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-xc-


1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-codec-

- 1.4.jar:/usr/local/hadop/share/hadop/comon/lib/activation-


1.1.jar:/usr/local/hadop/share/hadop/comon/lib/avro-

- 1.7.4.jar:/usr/local/hadop/share/hadop/comon/lib/comons-htpclient-

3.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-beanutils-

- 1.7.0.jar:/usr/local/hadop/share/hadop/comon/lib/hadop-anotations-
- 2.2.0.jar:/usr/local/hadop/share/hadop/comon/lib/jsp-api-


- 2.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-compres-

- 1.4.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-beanutils-core-


- 1.8.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-configuration-


- 1.6.jar:/usr/local/hadop/share/hadop/comon/lib/jaxb-impl-2.2.3-

- 1.jar:/usr/local/hadop/share/hadop/comon/lib/zokeper-

3.4.5.jar:/usr/local/hadop/share/hadop/comon/lib/hadop-auth-

- 2.2.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-io-

2.1.jar:/usr/local/hadop/share/hadop/comon/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/comon/lib/stax-api-

- 1.0.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-colections-

3.2.1.jar:/usr/local/hadop/share/hadop/comon/lib/servlet-api-

- 2.5.jar:/usr/local/hadop/share/hadop/comon/lib/junit-


- 4.8.2.jar:/usr/local/hadop/share/hadop/comon/lib/jety-util-












- 6.1.26.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-maper-asl-


- 1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/jsr305-

1.3.9.jar:/usr/local/hadop/share/hadop/comon/lib/mockito-al-

- 1.8.5.jar:/usr/local/hadop/share/hadop/comon/lib/comons-loging-

1.1.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-math2.1.jar:/usr/local/hadop/share/hadop/comon/lib/jaxb-api-

- 2.2.2.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-json-

1.9.jar:/usr/local/hadop/share/hadop/comon/lib/asm-

- 3.2.jar:/usr/local/hadop/share/hadop/comon/lib/comons-net-




- 3.1.jar:/usr/local/hadop/share/hadop/comon/lib/jety6.1.26.jar:/usr/local/hadop/share/hadop/comon/lib/jasper-compiler-


- 5.5.23.jar:/usr/local/hadop/share/hadop/comon/lib/guava-


- 1.0.2.jar:/usr/local/hadop/share/hadop/comon/lib/xmlenc-

- 0.52.jar:/usr/local/hadop/share/hadop/comon/lib/log4j-
- 1.2.17.jar:/usr/local/hadop/share/hadop/comon/lib/paranamer-
- 2.3.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-jaxrs-


- 1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/jetison-

1.1.jar:/usr/local/hadop/share/hadop/comon/hadop-comon-2.2.0tests.jar:/usr/local/hadop/share/hadop/comon/hadop-comon2.2.0.jar:/usr/local/hadop/share/hadop/comon/hadop-nfs-

- 2.2.0.jar:/usr/local/hadop/share/hadop/hdfs:/usr/local/hadop/share/hadop/hdfs/lib/protobufjava-2.5.0.jar:/usr/local/hadop/share/hadop/hdfs/lib/jersey-server-




- 1.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-cli-


- 1.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/jackson-core-asl-


- 1.8.8.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-el-

- 1.0.jar:/usr/local/hadop/share/hadop/hdfs/lib/jersey-core-

1.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-daemon-

- 1.0.13.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-lang-
- 2.5.jar:/usr/local/hadop/share/hadop/hdfs/lib/jasper-runtime-


- 5.5.23.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-codec-

1.4.jar:/usr/local/hadop/share/hadop/hdfs/lib/jsp-api2.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/hdfs/lib/servlet-api2.5.jar:/usr/local/hadop/share/hadop/hdfs/lib/jety-util-

- 6.1.26.jar:/usr/local/hadop/share/hadop/hdfs/lib/jackson-maper-asl-






- 1.8.8.jar:/usr/local/hadop/share/hadop/hdfs/lib/jsr305-


- 1.3.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-loging-

- 1.1.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/asm-

3.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/jety6.1.26.jar:/usr/local/hadop/share/hadop/hdfs/lib/guava-

- 1.0.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/xmlenc-

- 0.52.jar:/usr/local/hadop/share/hadop/hdfs/lib/log4j-
- 1.2.17.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-nfs-
- 2.2.0.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-2.2.0tests.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-


- 2.2.0.jar:/usr/local/hadop/share/hadop/yarn/lib/protobuf-java-

2.5.0.jar:/usr/local/hadop/share/hadop/yarn/lib/snapy-java-

- 1.0.4.1.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-server-

1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/guice3.0.jar:/usr/local/hadop/share/hadop/yarn/lib/jackson-core-asl-

- 1.8.8.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-core-
- 1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/xz-


- 1.0.jar:/usr/local/hadop/share/hadop/yarn/lib/hamcrest-core-
- 1.1.jar:/usr/local/hadop/share/hadop/yarn/lib/avro-

- 1.7.4.jar:/usr/local/hadop/share/hadop/yarn/lib/aopaliance-

- 1.0.jar:/usr/local/hadop/share/hadop/yarn/lib/hadop-anotations-
- 2.2.0.jar:/usr/local/hadop/share/hadop/yarn/lib/comons-compres-

1.4.1.jar:/usr/local/hadop/share/hadop/yarn/lib/javax.inject-

- 1.jar:/usr/local/hadop/share/hadop/yarn/lib/guice-servlet-

3.0.jar:/usr/local/hadop/share/hadop/yarn/lib/comons-io-

- 2.1.jar:/usr/local/hadop/share/hadop/yarn/lib/nety-
- 3.6.2.Final.jar:/usr/local/hadop/share/hadop/yarn/lib/jackson-maper-asl-




- 1.8.8.jar:/usr/local/hadop/share/hadop/yarn/lib/asm-

3.2.jar:/usr/local/hadop/share/hadop/yarn/lib/junit4.10.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-guice-

- 1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/log4j-


- 1.2.17.jar:/usr/local/hadop/share/hadop/yarn/lib/paranamer-


- 2.3.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-api-








- 2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-tests-


- 2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-site-


- 2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-client2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-nodemanager2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-aplications-distributedshel-


2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-resourcemanager2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-web-proxy2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-aplications-unmanaged-amlauncher-2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-comon2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-comon-

- 2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/protobuf-java-

- 2.5.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/snapy-java-

- 1.0.4.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-server-

1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/guice3.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jackson-core-asl-

- 1.8.8.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-core-
- 1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/xz-


- 1.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/hamcrest-core-
- 1.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/avro-

- 1.7.4.jar:/usr/local/hadop/share/hadop/mapreduce/lib/aopaliance-

- 1.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/hadop-anotations-
- 2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/comons-compres-

- 1.4.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/javax.inject-

- 1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/guice-servlet-

3.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/comons-io-

- 2.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/nety-
- 3.6.2.Final.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jackson-maper-asl-


1.8.8.jar:/usr/local/hadop/share/hadop/mapreduce/lib/asm3.2.jar:/usr/local/hadop/share/hadop/mapreduce/lib/junit4.10.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-guice1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/mapreduce/lib/paranamer-

- 2.3.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-hs-plugins-








- 2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-jobclient-2.2.0tests.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-jobclient2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-ap2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-core2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-shufle2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-examples2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-hs2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-comon-






- 2.2.0.jar:/usr/local/hadop/contrib/capacity-scheduler/*.jar


14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:java.library.path=/usr/local/hadop/lib/native 14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:java.io.tmpdir=/tmp 14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:java.compiler=<NA> 14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:os.name=Linux 14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:os.arch=amd64 14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:os.version=2.6.32431.el6.x86_64 14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:user.name=rot 14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:user.home=/rot 14/02/12 07 28 57 INFO zokeper.ZoKeper: Client environment:user.dir=/usr/local/hadop 14/02/12 07 28 57 INFO zokeper.ZoKeper: Initiating client conection, conectString=hadop101 2181,hadop102 2181,hadop103 2181 sesionTimeout=5 0 watcher=org.apache.hadop.ha.ActiveStandbyElector$WatcherWithClientRef@3e9c0ea 14/02/12 07 28 57 INFO zokeper.ClientCnxn: Opening socket conection to server hadop102/192.168.80.102 2181. Wil not atempt to authenticate using SASL (unknown eror) 14/02/12 07 28 57 INFO zokeper.ClientCnxn: Socket conection established to hadop102/192.168.80.102 2181, initiating sesion 14/02/12 07 28 57 INFO zokeper.ClientCnxn: Sesion establishment complete on server

- hadop102/192.168.80.102 2181, sesionid = 0x6423403971 0, negotiated timeout = 5 0 14/02/12 07 28 57 INFO ha.ActiveStandbyElector: Sesion conected. 14/02/12 07 28 57 INFO ha.ActiveStandbyElector: Sucesfuly created /hadop-ha/cluster1 in ZK. 14/02/12 07 28 57 INFO zokeper.ZoKeper: Sesion: 0x6423403971 0 closed 14/02/12 07 28 57 INFO zokeper.ClientCnxn: EventThread shut down [rot@hadop101 hadop]# 验证： [rot@hadop101 hadop]# zkCli.sh Conecting to localhost:2181 2014-02-12 07 30 24,35 [myid:] - INFO [main:Environment@10] - Client environment:zokeper.version=3.4.5-1392090, built on 09/30/2012 17 52 GMT


- 2014-02-12 07 30 24,373 [myid:] - INFO [main:Environment@10] - Client environment:host.name=hadop101
- 2014-02-12 07 30 24,374 [myid:] - INFO [main:Environment@10] - Client environment:java.version=1.7.0_45
- 2014-02-12 07 30 24,375 [myid:] - INFO [main:Environment@10] - Client environment:java.vendor=Oracle Corporation


- 2014-02-12 07 30 24,376 [myid:] - INFO [main:Environment@10] - Client environment:java.home=/usr/local/jdk/jre 2014-02-12 07 30 24,376 [myid:] - INFO [main:Environment@10] - Client environment:java.clas.path=/usr/local/zokeper/bin/./build/clases:/usr/local/zokeper/bin/./buil d/lib/*.jar:/usr/local/zokeper/bin/./lib/slf4j-log4j12-1.6.1.jar:/usr/local/zokeper/bin/./lib/slf4j-api1.6.1.jar:/usr/local/zokeper/bin/./lib/nety-3.2.2.Final.jar:/usr/local/zokeper/bin/./lib/log4j-


- 1.2.15.jar:/usr/local/zokeper/bin/./lib/jline-0.9.94.jar:/usr/local/zokeper/bin/./zokeper-


- 3.4.5.jar:/usr/local/zokeper/bin/./src/java/lib/*.jar:/usr/local/zokeper/bin/./conf:/usr/local/hadop


- 2014-02-12 07 30 24,378 [myid:] - INFO [main:Environment@10] - Client environment:java.library.path=/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
- 2014-02-12 07 30 24,379 [myid:] - INFO [main:Environment@10] - Client environment:java.io.tmpdir=/tmp
- 2014-02-12 07 30 24,380 [myid:] - INFO [main:Environment@10] - Client environment:java.compiler=<NA>
- 2014-02-12 07 30 24,381 [myid:] - INFO [main:Environment@10] - Client environment:os.name=Linux
- 2014-02-12 07 30 24,382 [myid:] - INFO [main:Environment@10] - Client environment:os.arch=amd64

- 2014-02-12 07 30 24,382 [myid:] - INFO [main:Environment@10] - Client environment:os.version=2.6.32-431.el6.x86_64
- 2014-02-12 07 30 24,383 [myid:] - INFO [main:Environment@10] - Client environment:user.name=rot


- 2014-02-12 07 30 24,383 [myid:] - INFO [main:Environment@10] - Client environment:user.home=/rot
- 2014-02-12 07 30 24,384 [myid:] - INFO [main:Environment@10] - Client environment:user.dir=/usr/local/hadop 2014-02-12 07 30 24,387 [myid:] - INFO [main:ZoKeper@438] - Initiating client conection, conectString=localhost:2181 sesionTimeout=3 0 watcher=org.apache.zokeper.ZoKeperMain$MyWatcher@10636a7e Welcome to ZoKeper! 2014-02-12 07 30 24,42 [myid:] - INFO [mainSendThread(127.0.0.1 2181):ClientCnxn$SendThread@96] - Opening socket conection to server 127.0.0.1/127.0.0.1 2181. Wil not atempt to authenticate using SASL (unknown eror) 2014-02-12 07 30 24,462 [myid:] - INFO [mainSendThread(127.0.0.1 2181):ClientCnxn$SendThread@849] - Socket conection established to 127.0.0.1/127.0.0.1 2181, initiating sesion


JLine suport is enabled 2014-02-12 07 30 24,494 [myid:] - INFO [mainSendThread(127.0.0.1 2181):ClientCnxn$SendThread@1207] - Sesion establishment complete on

- server 127.0.0.1/127.0.0.1 2181, sesionid = 0x65423404e53 01, negotiated timeout = 3 0 WATCHER: WatchedEvent state:SyncConected type:None path:nul [zk: localhost:2181(CONECTED) 0] ls / [hadop-ha, zokeper]


- [zk: localhost:2181(CONECTED) 1] ls /hadop-ha [cluster1]
- [zk: localhost:2181(CONECTED) 2] 【格式化操作的⽬的是在ZK集群中建⽴⼀个节点，⽤于保存集群c1中NameNode的状态数据】 在hadop103上执⾏命令：/usr/local/hadop/bin/hdfs zkfc –formatZK 命令输出： [rot@hadop103 hadop]# /usr/local/hadop/bin/hdfs zkfc -formatZK


- 14/02/12 07 32 14 WARN util.NativeCodeLoader: Unable to load native-hadop library for your platform. using builtin-java clases where aplicable


- 14/02/12 07 32 14 INFO tols.DFSZKFailoverControler: Failover controler configured for NameNode NameNode at hadop103/192.168.80.103 9 0


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:zokeper.version=3.4.51392090, built on 09/30/2012 17 52 GMT


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:host.name=hadop103


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:java.version=1.7.0_45


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:java.vendor=Oracle Corporation


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:java.home=/usr/local/jdk/jre


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:java.clas.path=/usr/local/hadop/etc/hadop:/usr/local/hadop/share/hadop/como n/lib/protobuf-java-2.5.0.jar:/usr/local/hadop/share/hadop/comon/lib/snapy-java1.0.4.1.jar:/usr/local/hadop/share/hadop/comon/lib/jsch0.1.42.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/comon/lib/slf4j-log4j121.7.5.jar:/usr/local/hadop/share/hadop/comon/lib/comons-digester1.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-cli1.2.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-el1.0.jar:/usr/local/hadop/share/hadop/comon/lib/jets3t0.6.1.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/comon/lib/slf4j-api1.7.5.jar:/usr/local/hadop/share/hadop/comon/lib/xz1.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-lang2.5.jar:/usr/local/hadop/share/hadop/comon/lib/jasper-runtime-


- 5.5.23.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-xc1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-codec1.4.jar:/usr/local/hadop/share/hadop/comon/lib/activation1.1.jar:/usr/local/hadop/share/hadop/comon/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/comon/lib/comons-htpclient3.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-beanutils1.7.0.jar:/usr/local/hadop/share/hadop/comon/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/comon/lib/jsp-api2.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-beanutils-core1.8.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-configuration1.6.jar:/usr/local/hadop/share/hadop/comon/lib/jaxb-impl-2.2.31.jar:/usr/local/hadop/share/hadop/comon/lib/zokeper3.4.5.jar:/usr/local/hadop/share/hadop/comon/lib/hadop-auth2.2.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/comon/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/comon/lib/stax-api1.0.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-colections3.2.1.jar:/usr/local/hadop/share/hadop/comon/lib/servlet-api2.5.jar:/usr/local/hadop/share/hadop/comon/lib/junit4.8.2.jar:/usr/local/hadop/share/hadop/comon/lib/jety-util-


- 6.1.26.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/jsr3051.3.9.jar:/usr/local/hadop/share/hadop/comon/lib/mockito-al1.8.5.jar:/usr/local/hadop/share/hadop/comon/lib/comons-loging1.1.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-math2.1.jar:/usr/local/hadop/share/hadop/comon/lib/jaxb-api2.2.2.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-json1.9.jar:/usr/local/hadop/share/hadop/comon/lib/asm3.2.jar:/usr/local/hadop/share/hadop/comon/lib/comons-net3.1.jar:/usr/local/hadop/share/hadop/comon/lib/jety6.1.26.jar:/usr/local/hadop/share/hadop/comon/lib/jasper-compiler5.5.23.jar:/usr/local/hadop/share/hadop/comon/lib/guava-


1.0.2.jar:/usr/local/hadop/share/hadop/comon/lib/xmlenc0.52.jar:/usr/local/hadop/share/hadop/comon/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/comon/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-jaxrs1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/jetison1.1.jar:/usr/local/hadop/share/hadop/comon/hadop-comon-2.2.0tests.jar:/usr/local/hadop/share/hadop/comon/hadop-comon2.2.0.jar:/usr/local/hadop/share/hadop/comon/hadop-nfs2.2.0.jar:/usr/local/hadop/share/hadop/hdfs:/usr/local/hadop/share/hadop/hdfs/lib/protobufjava-2.5.0.jar:/usr/local/hadop/share/hadop/hdfs/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-cli1.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-el1.0.jar:/usr/local/hadop/share/hadop/hdfs/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-daemon1.0.13.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-lang2.5.jar:/usr/local/hadop/share/hadop/hdfs/lib/jasper-runtime-

- 5.5.23.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-codec1.4.jar:/usr/local/hadop/share/hadop/hdfs/lib/jsp-api2.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/hdfs/lib/servlet-api2.5.jar:/usr/local/hadop/share/hadop/hdfs/lib/jety-util-
- 6.1.26.jar:/usr/local/hadop/share/hadop/hdfs/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/hdfs/lib/jsr305-


1.3.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-loging1.1.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/asm3.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/jety6.1.26.jar:/usr/local/hadop/share/hadop/hdfs/lib/guava-

1.0.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/xmlenc0.52.jar:/usr/local/hadop/share/hadop/hdfs/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-nfs2.2.0.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-2.2.0tests.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs2.2.0.jar:/usr/local/hadop/share/hadop/yarn/lib/protobuf-java2.5.0.jar:/usr/local/hadop/share/hadop/yarn/lib/snapy-java1.0.4.1.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/guice3.0.jar:/usr/local/hadop/share/hadop/yarn/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/xz1.0.jar:/usr/local/hadop/share/hadop/yarn/lib/hamcrest-core1.1.jar:/usr/local/hadop/share/hadop/yarn/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/yarn/lib/aopaliance1.0.jar:/usr/local/hadop/share/hadop/yarn/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/yarn/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/yarn/lib/javax.inject1.jar:/usr/local/hadop/share/hadop/yarn/lib/guice-servlet3.0.jar:/usr/local/hadop/share/hadop/yarn/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/yarn/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/yarn/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/yarn/lib/asm3.2.jar:/usr/local/hadop/share/hadop/yarn/lib/junit4.10.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-guice1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/yarn/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-api2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-tests2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-site-

- 2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-client2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-nodemanager2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-aplications-distributedshel-


2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-resourcemanager2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-web-proxy2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-aplications-unmanaged-amlauncher-2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-comon2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-comon2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/protobuf-java2.5.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/snapy-java1.0.4.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/guice3.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/xz1.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/hamcrest-core1.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/mapreduce/lib/aopaliance1.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/javax.inject1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/guice-servlet3.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/mapreduce/lib/asm3.2.jar:/usr/local/hadop/share/hadop/mapreduce/lib/junit4.10.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-guice1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/mapreduce/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-hs-plugins2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-jobclient-2.2.0tests.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-jobclient2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-ap2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-core2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-shufle2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-examples2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-hs2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-comon-

- 2.2.0.jar:/usr/local/hadop/contrib/capacity-scheduler/*.jar


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:java.library.path=/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:java.io.tmpdir=/tmp


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:java.compiler=<NA>


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:os.name=Linux


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:os.arch=amd64


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:os.version=2.6.32431.el6.x86_64


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:user.name=rot


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:user.home=/rot


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Client environment:user.dir=/usr/local/hadop


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Initiating client conection, conectString=hadop101 2181,hadop102 2181,hadop103 2181 sesionTimeout=5 0 watcher=org.apache.hadop.ha.ActiveStandbyElector$WatcherWithClientRef@91d3b18


- 14/02/12 07 32 14 INFO zokeper.ClientCnxn: Opening socket conection to server hadop102/192.168.80.102 2181. Wil not atempt to authenticate using SASL (unknown eror)


- 14/02/12 07 32 14 INFO zokeper.ClientCnxn: Socket conection established to hadop102/192.168.80.102 2181, initiating sesion


- 14/02/12 07 32 14 INFO zokeper.ClientCnxn: Sesion establishment complete on server


- hadop102/192.168.80.102 2181, sesionid = 0x6423403971 01, negotiated timeout = 5 0


- 14/02/12 07 32 14 INFO ha.ActiveStandbyElector: Sesion conected.


- 14/02/12 07 32 14 INFO ha.ActiveStandbyElector: Sucesfuly created /hadop-ha/cluster2 in ZK.


- 14/02/12 07 32 14 INFO zokeper.ZoKeper: Sesion: 0x6423403971 01 closed


- 14/02/12 07 32 14 INFO zokeper.ClientCnxn: EventThread shut down 验证： [rot@hadop103 hadop]# zkCli.sh Conecting to localhost:2181 2014-02-12 07 32 21,70 [myid:] - INFO [main:Environment@10] - Client environment:zokeper.version=3.4.5-1392090, built on 09/30/2012 17 52 GMT 2014-02-12 07 32 21,786 [myid:] - INFO [main:Environment@10] - Client environment:host.name=hadop103 2014-02-12 07 32 21,78 [myid:] - INFO [main:Environment@10] - Client environment:java.version=1.7.0_45 2014-02-12 07 32 21,789 [myid:] - INFO [main:Environment@10] - Client environment:java.vendor=Oracle Corporation


- 2014-02-12 07 32 21,789 [myid:] - INFO [main:Environment@10] - Client environment:java.home=/usr/local/jdk/jre


- 2014-02-12 07 32 21,790 [myid:] - INFO [main:Environment@10] - Client environment:java.clas.path=/usr/local/zokeper/bin/./build/clases:/usr/local/zokeper/bin/./buil d/lib/*.jar:/usr/local/zokeper/bin/./lib/slf4j-log4j12-1.6.1.jar:/usr/local/zokeper/bin/./lib/slf4j-api1.6.1.jar:/usr/local/zokeper/bin/./lib/nety-3.2.2.Final.jar:/usr/local/zokeper/bin/./lib/log4j-

- 1.2.15.jar:/usr/local/zokeper/bin/./lib/jline-0.9.94.jar:/usr/local/zokeper/bin/./zokeper-


3.4.5.jar:/usr/local/zokeper/bin/./src/java/lib/*.jar:/usr/local/zokeper/bin/./conf:/usr/local/hadop

- 2014-02-12 07 32 21,791 [myid:] - INFO [main:Environment@10] - Client environment:java.library.path=/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
- 2014-02-12 07 32 21,792 [myid:] - INFO [main:Environment@10] - Client environment:java.io.tmpdir=/tmp
- 2014-02-12 07 32 21,793 [myid:] - INFO [main:Environment@10] - Client environment:java.compiler=<NA>

- 2014-02-12 07 32 21,793 [myid:] - INFO [main:Environment@10] - Client environment:os.name=Linux
- 2014-02-12 07 32 21,794 [myid:] - INFO [main:Environment@10] - Client environment:os.arch=amd64


- 2014-02-12 07 32 21,794 [myid:] - INFO [main:Environment@10] - Client environment:os.version=2.6.32-431.el6.x86_64
- 2014-02-12 07 32 21,795 [myid:] - INFO [main:Environment@10] - Client environment:user.name=rot
- 2014-02-12 07 32 21,796 [myid:] - INFO [main:Environment@10] - Client environment:user.home=/rot 2014-02-12 07 32 21,796 [myid:] - INFO [main:Environment@10] - Client environment:user.dir=/usr/local/hadop 2014-02-12 07 32 21,801 [myid:] - INFO [main:ZoKeper@438] - Initiating client conection, conectString=localhost:2181 sesionTimeout=3 0 watcher=org.apache.zokeper.ZoKeperMain$MyWatcher@10636a7e Welcome to ZoKeper! 2014-02-12 07 32 21,850 [myid:] - INFO [mainSendThread(127.0.0.1 2181):ClientCnxn$SendThread@96] - Opening socket conection to server 127.0.0.1/127.0.0.1 2181. Wil not atempt to authenticate using SASL (unknown eror) JLine suport is enabled 2014-02-12 07 32 21,868 [myid:] - INFO [mainSendThread(127.0.0.1 2181):ClientCnxn$SendThread@849] - Socket conection established to 127.0.0.1/127.0.0.1 2181, initiating sesion


2014-02-12 07 32 21,906 [myid:] - INFO [mainSendThread(127.0.0.1 2181):ClientCnxn$SendThread@1207] - Sesion establishment complete on server 127.0.0.1/127.0.0.1 2181, sesionid = 0x67423403981 02, negotiated timeout = 3 0 WATCHER: WatchedEvent state:SyncConected type:None path:nul [zk: localhost:2181(CONECTED) 0] ls / [hadop-ha, zokeper]

- [zk: localhost:2181(CONECTED) 1] ls /hadop-ha [cluster2, cluster1]
- [zk: localhost:2181(CONECTED) 2] 【集群c2也格式化，产⽣⼀个新的ZK节点cluster2】


## 4.启动JournalNode集群

在hadop101、hadop102、hadop103上分别执⾏命令：/usr/local/hadop/sbin/hadopdaemon.sh start journalnode 命令输出(以hadop101为例)： [rot@hadop101 hadop]# /usr/local/hadop/sbin/hadop-daemon.sh start journalnode starting journalnode, loging to /usr/local/hadop/logs/hadop-rot-journalnode-hadop101.out [rot@hadop101 hadop]# 在每个节点执⾏完启动命令后，每个节点都执⾏以下验证。 验证(以hadop101为例)：

[rot@hadop101 hadop]# jps 2396 JournalNode 23598 Jps

2491 QuorumPerMain [rot@hadop101 hadop]# 【产⽣⼀个java进程JournalNode】 查看⼀下⽬录结构 [rot@hadop101 hadop]# jps 2396 JournalNode

2491 QuorumPerMain 2345 Jps [rot@hadop101 hadop]# pwd /usr/local/hadop [rot@hadop101 hadop]# ls tmp/ journal

- [rot@hadop101 hadop]#


【启动JournalNode后，会在本地磁盘产⽣⼀个⽬录，⽤户保存NameNode的edits⽂件的数据】

## 5.格式化集群c1的⼀个NameNode

从hadop101和hadop102中任选⼀个即可，这⾥选择的是hadop101

- 在hadop101执⾏以下命令：/usr/local/hadop/bin/hdfs namenode -format -clusterId c1 命令输出：


- [rot@hadop101 hadop]# /usr/local/hadop/bin/hdfs namenode -format -clusterId c1


- 14/02/12 08 07 59 INFO namenode.NameNode: STARTUP_MSG: / * STARTUP_MSG: Starting NameNode


- STARTUP_MSG: host = hadop101/192.168.80.101 STARTUP_MSG: args = [-format, -clusterId, c1] STARTUP_MSG: version = 2.2.0


STARTUP_MSG: claspath = /usr/local/hadop/etc/hadop:/usr/local/hadop/share/hadop/comon/lib/protobuf-java-

- 2.5.0.jar:/usr/local/hadop/share/hadop/comon/lib/snapy-java1.0.4.1.jar:/usr/local/hadop/share/hadop/comon/lib/jsch0.1.42.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/comon/lib/slf4j-log4j121.7.5.jar:/usr/local/hadop/share/hadop/comon/lib/comons-digester1.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-cli1.2.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-el1.0.jar:/usr/local/hadop/share/hadop/comon/lib/jets3t0.6.1.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/comon/lib/slf4j-api1.7.5.jar:/usr/local/hadop/share/hadop/comon/lib/xz1.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-lang2.5.jar:/usr/local/hadop/share/hadop/comon/lib/jasper-runtime5.5.23.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-xc1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-codec1.4.jar:/usr/local/hadop/share/hadop/comon/lib/activation1.1.jar:/usr/local/hadop/share/hadop/comon/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/comon/lib/comons-htpclient3.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-beanutils1.7.0.jar:/usr/local/hadop/share/hadop/comon/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/comon/lib/jsp-api2.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-beanutils-core1.8.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-configuration1.6.jar:/usr/local/hadop/share/hadop/comon/lib/jaxb-impl-2.2.31.jar:/usr/local/hadop/share/hadop/comon/lib/zokeper3.4.5.jar:/usr/local/hadop/share/hadop/comon/lib/hadop-auth2.2.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/comon/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/comon/lib/stax-api1.0.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-colections3.2.1.jar:/usr/local/hadop/share/hadop/comon/lib/servlet-api2.5.jar:/usr/local/hadop/share/hadop/comon/lib/junit4.8.2.jar:/usr/local/hadop/share/hadop/comon/lib/jety-util-


6.1.26.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/jsr3051.3.9.jar:/usr/local/hadop/share/hadop/comon/lib/mockito-al1.8.5.jar:/usr/local/hadop/share/hadop/comon/lib/comons-loging1.1.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-math2.1.jar:/usr/local/hadop/share/hadop/comon/lib/jaxb-api2.2.2.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-json1.9.jar:/usr/local/hadop/share/hadop/comon/lib/asm3.2.jar:/usr/local/hadop/share/hadop/comon/lib/comons-net3.1.jar:/usr/local/hadop/share/hadop/comon/lib/jety-

- 6.1.26.jar:/usr/local/hadop/share/hadop/comon/lib/jasper-compiler5.5.23.jar:/usr/local/hadop/share/hadop/comon/lib/guava-


1.0.2.jar:/usr/local/hadop/share/hadop/comon/lib/xmlenc0.52.jar:/usr/local/hadop/share/hadop/comon/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/comon/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-jaxrs1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/jetison1.1.jar:/usr/local/hadop/share/hadop/comon/hadop-comon-2.2.0tests.jar:/usr/local/hadop/share/hadop/comon/hadop-comon2.2.0.jar:/usr/local/hadop/share/hadop/comon/hadop-nfs2.2.0.jar:/usr/local/hadop/share/hadop/hdfs:/usr/local/hadop/share/hadop/hdfs/lib/protobufjava-2.5.0.jar:/usr/local/hadop/share/hadop/hdfs/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-cli1.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-el1.0.jar:/usr/local/hadop/share/hadop/hdfs/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-daemon1.0.13.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-lang2.5.jar:/usr/local/hadop/share/hadop/hdfs/lib/jasper-runtime5.5.23.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-codec1.4.jar:/usr/local/hadop/share/hadop/hdfs/lib/jsp-api2.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/hdfs/lib/servlet-api2.5.jar:/usr/local/hadop/share/hadop/hdfs/lib/jety-util6.1.26.jar:/usr/local/hadop/share/hadop/hdfs/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/hdfs/lib/jsr305-

1.3.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-loging1.1.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/asm3.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/jety6.1.26.jar:/usr/local/hadop/share/hadop/hdfs/lib/guava-

1.0.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/xmlenc0.52.jar:/usr/local/hadop/share/hadop/hdfs/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-nfs2.2.0.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-2.2.0tests.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs2.2.0.jar:/usr/local/hadop/share/hadop/yarn/lib/protobuf-java2.5.0.jar:/usr/local/hadop/share/hadop/yarn/lib/snapy-java1.0.4.1.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/guice3.0.jar:/usr/local/hadop/share/hadop/yarn/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/xz1.0.jar:/usr/local/hadop/share/hadop/yarn/lib/hamcrest-core1.1.jar:/usr/local/hadop/share/hadop/yarn/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/yarn/lib/aopaliance1.0.jar:/usr/local/hadop/share/hadop/yarn/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/yarn/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/yarn/lib/javax.inject1.jar:/usr/local/hadop/share/hadop/yarn/lib/guice-servlet3.0.jar:/usr/local/hadop/share/hadop/yarn/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/yarn/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/yarn/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/yarn/lib/asm3.2.jar:/usr/local/hadop/share/hadop/yarn/lib/junit4.10.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-guice1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/yarn/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-api2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-tests2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-site2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-client2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-nodemanager2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-aplications-distributedshel-

2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-resourcemanager2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-web-proxy2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-aplications-unmanaged-amlauncher-2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-comon2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-comon2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/protobuf-java2.5.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/snapy-java1.0.4.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/guice3.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/xz1.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/hamcrest-core1.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/mapreduce/lib/aopaliance1.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/javax.inject1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/guice-servlet3.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/mapreduce/lib/asm3.2.jar:/usr/local/hadop/share/hadop/mapreduce/lib/junit4.10.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-guice1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/mapreduce/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-hs-plugins2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-jobclient-2.2.0tests.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-jobclient2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-ap2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-core2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-shufle2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-examples2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-hs2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-comon2.2.0.jar:/contrib/capacity-scheduler/*.jar

STARTUP_MSG: build = Unknown -r Unknown; compiled by 'rot' on 2013-12-26T08 50Z STARTUP_MSG: java = 1.7.0_45

*/

- 14/02/12 08 07 59 INFO namenode.NameNode: registered UNIX signal handlers for [TERM, HUP, INT] Formating using clusterid: c1


- 14/02/12 08 08 01 INFO namenode.HostFileManager: read includes: HostSet( )


- 14/02/12 08 08 01 INFO namenode.HostFileManager: read excludes: HostSet( )


- 14/02/12 08 08 01 INFO blockmanagement.DatanodeManager: dfs.block.invalidate.limit=1 0


- 14/02/12 08 08 01 INFO util.GSet: Computing capacity for map BlocksMap


- 14/02/12 08 08 01 INFO util.GSet: VM type = 64-bit


- 14/02/12 08 08 01 INFO util.GSet: 2.0% max memory = 96.7 MB


- 14/02/12 08 08 01 INFO util.GSet: capacity = 2^21 = 2097152 entries


- 14/02/12 08 08 01 INFO blockmanagement.BlockManager: dfs.block.aces.token.enable=false


- 14/02/12 08 08 01 INFO blockmanagement.BlockManager: defaultReplication = 2


- 14/02/12 08 08 01 INFO blockmanagement.BlockManager: maxReplication = 512


- 14/02/12 08 08 01 INFO blockmanagement.BlockManager: minReplication = 1


- 14/02/12 08 08 01 INFO blockmanagement.BlockManager: maxReplicationStreams = 2


- 14/02/12 08 08 01 INFO blockmanagement.BlockManager: shouldCheckForEnoughRacks = false


- 14/02/12 08 08 01 INFO blockmanagement.BlockManager: replicationRecheckInterval = 3 0


- 14/02/12 08 08 01 INFO blockmanagement.BlockManager: encryptDataTransfer = false


- 14/02/12 08 08 01 INFO namenode.FSNamesystem: fsOwner = rot (auth:SIMPLE)


- 14/02/12 08 08 01 INFO namenode.FSNamesystem: supergroup = supergroup


- 14/02/12 08 08 01 INFO namenode.FSNamesystem: isPermisionEnabled = true


- 14/02/12 08 08 01 INFO namenode.FSNamesystem: Determined nameservice ID: cluster1


- 14/02/12 08 08 01 INFO namenode.FSNamesystem: HA Enabled: true


- 14/02/12 08 08 01 INFO namenode.FSNamesystem: Apend Enabled: true


- 14/02/12 08 08 01 INFO util.GSet: Computing capacity for map INodeMap


- 14/02/12 08 08 01 INFO util.GSet: VM type = 64-bit 14/02/12 08 08 01 INFO util.GSet: 1.0% max memory = 96.7 MB 14/02/12 08 08 01 INFO util.GSet: capacity = 2^20 = 1048576 entries 14/02/12 08 08 01 INFO namenode.NameNode: Caching file names ocuring more than 10 times


14/02/12 08 08 01 INFO namenode.FSNamesystem: dfs.namenode.safemode.threshold-pct =

- 0. 9 012874603 14/02/12 08 08 01 INFO namenode.FSNamesystem: dfs.namenode.safemode.min.datanodes = 0 14/02/12 08 08 01 INFO namenode.FSNamesystem: dfs.namenode.safemode.extension = 3 0 14/02/12 08 08 01 INFO namenode.FSNamesystem: Retry cache on namenode is enabled 14/02/12 08 08 01 INFO namenode.FSNamesystem: Retry cache wil use 0.03 of total heap and retry cache entry expiry time is 6 0 milis 14/02/12 08 08 01 INFO util.GSet: Computing capacity for map Namenode Retry Cache 14/02/12 08 08 01 INFO util.GSet: VM type = 64-bit 14/02/12 08 08 01 INFO util.GSet: 0.02 93294746% max memory = 96.7 MB 14/02/12 08 08 01 INFO util.GSet: capacity = 2^15 = 32768 entries 14/02/12 08 08 03 INFO comon.Storage: Storage directory /usr/local/hadop/tmp/dfs/name has ben sucesfuly formated. 14/02/12 08 08 04 INFO namenode.FSImage: Saving image file /usr/local/hadop/tmp/dfs/name/curent/fsimage.ckpt_ 0 using no compresion 14/02/12 08 08 04 INFO namenode.FSImage: Image file /usr/local/hadop/tmp/dfs/name/curent/fsimage.ckpt_ 0 of size 196 bytes saved in 0 seconds. 14/02/12 08 08 04 INFO namenode. NStorageRetentionManager: Going to retain 1 images with txid >= 0 14/02/12 08 08 04 INFO util.ExitUtil: Exiting with status 0 14/02/12 08 08 04 INFO namenode.NameNode: SHUTDOWN_MSG: / *


- SHUTDOWN_MSG: Shuting down NameNode at hadop101/192.168.80.101


*/

- [rot@hadop101 hadop]# 验证：


- [rot@hadop101 hadop]# ls tmp/ dfs journal


- [rot@hadop101 hadop]# ls tmp/dfs/ name 【格式化NameNode会在磁盘产⽣⼀个⽬录，⽤于保存NameNode的fsimage、edits等⽂件】


## 6.启动c1中刚才格式化的NameNode

- 在hadop101上执⾏命令：/usr/local/hadop/sbin/hadop-daemon.sh start namenode


命令输出：

- [rot@hadop101 hadop]# /usr/local/hadop/sbin/hadop-daemon.sh start namenode


- starting namenode, loging to /usr/local/hadop/logs/hadop-rot-namenode-hadop101.out 验证：


- [rot@hadop101 hadop]# jps 2396 JournalNode 23598 Jps 2358 NameNode 2491 QuorumPerMain


- [rot@hadop101 hadop]# 【启动后，产⽣⼀个新的java进程NameNode】 通过浏览器访问，也可以看到下图所示

在hadop102上执⾏命令：/home/hadop/hadop/bin/hdfs namenode -botstrapStandby 命令输出：

- [rot@hadop102 hadop]# /home/hadop/hadop/bin/hdfs namenode -botstrapStandby 14/02/12 08 17  4 INFO namenode.NameNode: STARTUP_MSG: / * STARTUP_MSG: Starting NameNode


## 7.把NameNode的数据从hadop101同步到hadop102中

- STARTUP_MSG: host = hadop102/192.168.80.102 STARTUP_MSG: args = [-botstrapStandby] STARTUP_MSG: version = 2.2.0


STARTUP_MSG: claspath = /usr/local/hadop/etc/hadop:/usr/local/hadop/share/hadop/comon/lib/protobuf-java2.5.0.jar:/usr/local/hadop/share/hadop/comon/lib/snapy-java-

- 1.0.4.1.jar:/usr/local/hadop/share/hadop/comon/lib/jsch0.1.42.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/comon/lib/slf4j-log4j12-


- 1.7.5.jar:/usr/local/hadop/share/hadop/comon/lib/comons-digester-
- 1.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-cli1.2.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-el1.0.jar:/usr/local/hadop/share/hadop/comon/lib/jets3t0.6.1.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/comon/lib/slf4j-api1.7.5.jar:/usr/local/hadop/share/hadop/comon/lib/xz1.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-lang2.5.jar:/usr/local/hadop/share/hadop/comon/lib/jasper-runtime5.5.23.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-xc1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-codec1.4.jar:/usr/local/hadop/share/hadop/comon/lib/activation1.1.jar:/usr/local/hadop/share/hadop/comon/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/comon/lib/comons-htpclient3.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-beanutils1.7.0.jar:/usr/local/hadop/share/hadop/comon/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/comon/lib/jsp-api2.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-beanutils-core1.8.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-configuration1.6.jar:/usr/local/hadop/share/hadop/comon/lib/jaxb-impl-2.2.31.jar:/usr/local/hadop/share/hadop/comon/lib/zokeper3.4.5.jar:/usr/local/hadop/share/hadop/comon/lib/hadop-auth2.2.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/comon/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/comon/lib/stax-api1.0.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-colections3.2.1.jar:/usr/local/hadop/share/hadop/comon/lib/servlet-api2.5.jar:/usr/local/hadop/share/hadop/comon/lib/junit4.8.2.jar:/usr/local/hadop/share/hadop/comon/lib/jety-util-


6.1.26.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/jsr3051.3.9.jar:/usr/local/hadop/share/hadop/comon/lib/mockito-al1.8.5.jar:/usr/local/hadop/share/hadop/comon/lib/comons-loging-

- 1.1.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-math-
- 2.1.jar:/usr/local/hadop/share/hadop/comon/lib/jaxb-api2.2.2.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-json1.9.jar:/usr/local/hadop/share/hadop/comon/lib/asm3.2.jar:/usr/local/hadop/share/hadop/comon/lib/comons-net-
- 3.1.jar:/usr/local/hadop/share/hadop/comon/lib/jety6.1.26.jar:/usr/local/hadop/share/hadop/comon/lib/jasper-compiler5.5.23.jar:/usr/local/hadop/share/hadop/comon/lib/guava-


- 1.0.2.jar:/usr/local/hadop/share/hadop/comon/lib/xmlenc-


0.52.jar:/usr/local/hadop/share/hadop/comon/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/comon/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-jaxrs1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/jetison-

- 1.1.jar:/usr/local/hadop/share/hadop/comon/hadop-comon-2.2.0tests.jar:/usr/local/hadop/share/hadop/comon/hadop-comon-
- 2.2.0.jar:/usr/local/hadop/share/hadop/comon/hadop-nfs2.2.0.jar:/usr/local/hadop/share/hadop/hdfs:/usr/local/hadop/share/hadop/hdfs/lib/protobufjava-2.5.0.jar:/usr/local/hadop/share/hadop/hdfs/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-cli1.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-el1.0.jar:/usr/local/hadop/share/hadop/hdfs/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-daemon1.0.13.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-lang2.5.jar:/usr/local/hadop/share/hadop/hdfs/lib/jasper-runtime5.5.23.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-codec1.4.jar:/usr/local/hadop/share/hadop/hdfs/lib/jsp-api2.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/hdfs/lib/servlet-api2.5.jar:/usr/local/hadop/share/hadop/hdfs/lib/jety-util6.1.26.jar:/usr/local/hadop/share/hadop/hdfs/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/hdfs/lib/jsr305-


1.3.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-loging1.1.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/asm3.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/jety6.1.26.jar:/usr/local/hadop/share/hadop/hdfs/lib/guava-

1.0.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/xmlenc0.52.jar:/usr/local/hadop/share/hadop/hdfs/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-nfs2.2.0.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-2.2.0tests.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs2.2.0.jar:/usr/local/hadop/share/hadop/yarn/lib/protobuf-java2.5.0.jar:/usr/local/hadop/share/hadop/yarn/lib/snapy-java1.0.4.1.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/guice3.0.jar:/usr/local/hadop/share/hadop/yarn/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/xz1.0.jar:/usr/local/hadop/share/hadop/yarn/lib/hamcrest-core1.1.jar:/usr/local/hadop/share/hadop/yarn/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/yarn/lib/aopaliance1.0.jar:/usr/local/hadop/share/hadop/yarn/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/yarn/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/yarn/lib/javax.inject1.jar:/usr/local/hadop/share/hadop/yarn/lib/guice-servlet3.0.jar:/usr/local/hadop/share/hadop/yarn/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/yarn/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/yarn/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/yarn/lib/asm3.2.jar:/usr/local/hadop/share/hadop/yarn/lib/junit4.10.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-guice1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/yarn/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-api2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-tests2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-site2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-client2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-nodemanager2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-aplications-distributedshel-

2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-resourcemanager2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-web-proxy2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-aplications-unmanaged-amlauncher-2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-comon2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-comon2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/protobuf-java2.5.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/snapy-java1.0.4.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/guice3.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/xz1.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/hamcrest-core1.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/mapreduce/lib/aopaliance1.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/javax.inject1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/guice-servlet3.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/mapreduce/lib/asm3.2.jar:/usr/local/hadop/share/hadop/mapreduce/lib/junit4.10.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-guice1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/mapreduce/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-hs-plugins2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-jobclient-2.2.0tests.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-jobclient2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-ap2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-core2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-shufle2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-examples2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-hs2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-comon2.2.0.jar:/usr/local/hadop/contrib/capacity-scheduler/*.jar

STARTUP_MSG: build = Unknown -r Unknown; compiled by 'rot' on 2013-12-26T08 50Z STARTUP_MSG: java = 1.7.0_45

*/ 14/02/12 08 17  4 INFO namenode.NameNode: registered UNIX signal handlers for [TERM, HUP, INT]

= About to botstrap Standby ID hadop102 from:

Nameservice ID: cluster1

Other Namenode ID: hadop101 Other N's HTP adres: hadop101 5070 Other N's IPC adres: hadop101/192.168.80.101 9 0

Namespace ID: 1496717450 Block pol ID: BP-20254027-192.168.80.101-139216406187 Cluster ID: c1 Layout version: -47

= 14/02/12 08 17 48 INFO comon.Storage: Storage directory /usr/local/hadop/tmp/dfs/name has ben sucesfuly formated. 14/02/12 08 17 48 INFO namenode.TransferFsImage: Opening conection to

htp:/hadop101 507 0/getimage?getimage=1&txid=0&storageInfo=-47 1496717450 0:c1

14/02/12 08 17 48 INFO namenode.TransferFsImage: Transfer tok 0.18s at 0.0 KB/s 14/02/12 08 17 48 INFO namenode.TransferFsImage: Downloaded file fsimage.ckpt_ 0 size 196 bytes. 14/02/12 08 17 48 INFO util.ExitUtil: Exiting with status 0 14/02/12 08 17 48 INFO namenode.NameNode: SHUTDOWN_MSG: / *

- SHUTDOWN_MSG: Shuting down NameNode at hadop102/192.168.80.102


*/

- [rot@hadop102 hadop]# 验证：


- [rot@hadop102 hadop]# ls tmp/ dfs journal


- [rot@hadop102 hadop]# ls tmp/dfs/ name


- [rot@hadop102 hadop]# 【在tmp⽬录下产⽣⼀个⽬录name】


- 8.启动c1中另⼀个Namenode
- 9.格式化集群c2的⼀个NameNode


- 在hadop102上执⾏命令：/home/hadop/hadop/sbin/hadop-daemon.sh start namenode 命令输出：

- [rot@hadop102 hadop]# /home/hadop/hadop/sbin/hadop-daemon.sh start namenode


- starting namenode, loging to /usr/local/hadop/logs/hadop-rot-namenode-hadop102.out 验证：


- [rot@hadop102 hadop]# jps 1235 JournalNode 1261 Jps 12573 NameNode 12081 QuorumPerMain


[rot@hadop102 hadop]# 【产⽣java进程NameNode】 通过浏览器访问，也可以看到下图所示

从hadop103和hadop104中任选⼀个即可，这⾥选择的是hadop103

- 在hadop103执⾏以下命令：/home/hadop/hadop/bin/hdfs namenode -format -clusterId c2 命令输出：


- [rot@hadop103 hadop]# /home/hadop/hadop/bin/hdfs namenode -format -clusterId c2 14/02/12 08 23 28 INFO namenode.NameNode: STARTUP_MSG: / * STARTUP_MSG: Starting NameNode


- STARTUP_MSG: host = hadop103/192.168.80.103 STARTUP_MSG: args = [-format, -clusterId, c2] STARTUP_MSG: version = 2.2.0


STARTUP_MSG: claspath = /usr/local/hadop/etc/hadop:/usr/local/hadop/share/hadop/comon/lib/protobuf-java2.5.0.jar:/usr/local/hadop/share/hadop/comon/lib/snapy-java1.0.4.1.jar:/usr/local/hadop/share/hadop/comon/lib/jsch0.1.42.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/comon/lib/slf4j-log4j121.7.5.jar:/usr/local/hadop/share/hadop/comon/lib/comons-digester1.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-cli1.2.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-el1.0.jar:/usr/local/hadop/share/hadop/comon/lib/jets3t0.6.1.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/comon/lib/slf4j-api1.7.5.jar:/usr/local/hadop/share/hadop/comon/lib/xz1.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-lang2.5.jar:/usr/local/hadop/share/hadop/comon/lib/jasper-runtime5.5.23.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-xc1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-codec1.4.jar:/usr/local/hadop/share/hadop/comon/lib/activation1.1.jar:/usr/local/hadop/share/hadop/comon/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/comon/lib/comons-htpclient3.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-beanutils1.7.0.jar:/usr/local/hadop/share/hadop/comon/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/comon/lib/jsp-api2.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-beanutils-core1.8.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-configuration1.6.jar:/usr/local/hadop/share/hadop/comon/lib/jaxb-impl-2.2.31.jar:/usr/local/hadop/share/hadop/comon/lib/zokeper3.4.5.jar:/usr/local/hadop/share/hadop/comon/lib/hadop-auth2.2.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/comon/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/comon/lib/stax-api1.0.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-colections3.2.1.jar:/usr/local/hadop/share/hadop/comon/lib/servlet-api2.5.jar:/usr/local/hadop/share/hadop/comon/lib/junit4.8.2.jar:/usr/local/hadop/share/hadop/comon/lib/jety-util-

6.1.26.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/jsr3051.3.9.jar:/usr/local/hadop/share/hadop/comon/lib/mockito-al1.8.5.jar:/usr/local/hadop/share/hadop/comon/lib/comons-loging1.1.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-math2.1.jar:/usr/local/hadop/share/hadop/comon/lib/jaxb-api2.2.2.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-json1.9.jar:/usr/local/hadop/share/hadop/comon/lib/asm3.2.jar:/usr/local/hadop/share/hadop/comon/lib/comons-net3.1.jar:/usr/local/hadop/share/hadop/comon/lib/jety6.1.26.jar:/usr/local/hadop/share/hadop/comon/lib/jasper-compiler5.5.23.jar:/usr/local/hadop/share/hadop/comon/lib/guava-

1.0.2.jar:/usr/local/hadop/share/hadop/comon/lib/xmlenc0.52.jar:/usr/local/hadop/share/hadop/comon/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/comon/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-jaxrs1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/jetison1.1.jar:/usr/local/hadop/share/hadop/comon/hadop-comon-2.2.0tests.jar:/usr/local/hadop/share/hadop/comon/hadop-comon2.2.0.jar:/usr/local/hadop/share/hadop/comon/hadop-nfs2.2.0.jar:/usr/local/hadop/share/hadop/hdfs:/usr/local/hadop/share/hadop/hdfs/lib/protobufjava-2.5.0.jar:/usr/local/hadop/share/hadop/hdfs/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-cli1.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-el1.0.jar:/usr/local/hadop/share/hadop/hdfs/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-daemon1.0.13.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-lang2.5.jar:/usr/local/hadop/share/hadop/hdfs/lib/jasper-runtime5.5.23.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-codec1.4.jar:/usr/local/hadop/share/hadop/hdfs/lib/jsp-api2.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/hdfs/lib/servlet-api2.5.jar:/usr/local/hadop/share/hadop/hdfs/lib/jety-util6.1.26.jar:/usr/local/hadop/share/hadop/hdfs/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/hdfs/lib/jsr305-

1.3.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-loging1.1.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/asm3.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/jety6.1.26.jar:/usr/local/hadop/share/hadop/hdfs/lib/guava-

1.0.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/xmlenc0.52.jar:/usr/local/hadop/share/hadop/hdfs/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-nfs2.2.0.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-2.2.0tests.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs2.2.0.jar:/usr/local/hadop/share/hadop/yarn/lib/protobuf-java2.5.0.jar:/usr/local/hadop/share/hadop/yarn/lib/snapy-java1.0.4.1.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/guice3.0.jar:/usr/local/hadop/share/hadop/yarn/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/xz1.0.jar:/usr/local/hadop/share/hadop/yarn/lib/hamcrest-core1.1.jar:/usr/local/hadop/share/hadop/yarn/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/yarn/lib/aopaliance1.0.jar:/usr/local/hadop/share/hadop/yarn/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/yarn/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/yarn/lib/javax.inject1.jar:/usr/local/hadop/share/hadop/yarn/lib/guice-servlet3.0.jar:/usr/local/hadop/share/hadop/yarn/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/yarn/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/yarn/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/yarn/lib/asm3.2.jar:/usr/local/hadop/share/hadop/yarn/lib/junit4.10.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-guice1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/yarn/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-api2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-tests2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-site2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-client2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-nodemanager2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-aplications-distributedshel-

2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-resourcemanager2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-web-proxy2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-aplications-unmanaged-amlauncher-2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-comon2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-comon2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/protobuf-java2.5.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/snapy-java1.0.4.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/guice3.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/xz1.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/hamcrest-core1.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/avro-

- 1.7.4.jar:/usr/local/hadop/share/hadop/mapreduce/lib/aopaliance1.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/javax.inject1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/guice-servlet3.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jackson-maper-asl-
- 1.8.8.jar:/usr/local/hadop/share/hadop/mapreduce/lib/asm3.2.jar:/usr/local/hadop/share/hadop/mapreduce/lib/junit4.10.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-guice-
- 1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/mapreduce/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-hs-plugins2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-jobclient-2.2.0tests.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-jobclient2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-ap2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-core2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-shufle2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-examples2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-hs2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-comon2.2.0.jar:/usr/local/hadop/contrib/capacity-scheduler/*.jar


STARTUP_MSG: build = Unknown -r Unknown; compiled by 'rot' on 2013-12-26T08 50Z STARTUP_MSG: java = 1.7.0_45

*/ 14/02/12 08 23 28 INFO namenode.NameNode: registered UNIX signal handlers for [TERM, HUP, INT]

- 14/02/12 08 23 30 WARN util.NativeCodeLoader: Unable to load native-hadop library for your platform. using builtin-java clases where aplicable Formating using clusterid: c2
- 14/02/12 08 23 31 INFO namenode.HostFileManager: read includes: HostSet( ) 14/02/12 08 23 31 INFO namenode.HostFileManager: read excludes: HostSet( ) 14/02/12 08 23 31 INFO blockmanagement.DatanodeManager: dfs.block.invalidate.limit=1 0 14/02/12 08 23 31 INFO util.GSet: Computing capacity for map BlocksMap 14/02/12 08 23 31 INFO util.GSet: VM type = 64-bit 14/02/12 08 23 31 INFO util.GSet: 2.0% max memory = 96.7 MB 14/02/12 08 23 31 INFO util.GSet: capacity = 2^21 = 2097152 entries 14/02/12 08 23 31 INFO blockmanagement.BlockManager: dfs.block.aces.token.enable=false 14/02/12 08 23 31 INFO blockmanagement.BlockManager: defaultReplication = 2 14/02/12 08 23 31 INFO blockmanagement.BlockManager: maxReplication = 512 14/02/12 08 23 31 INFO blockmanagement.BlockManager: minReplication = 1 14/02/12 08 23 31 INFO blockmanagement.BlockManager: maxReplicationStreams = 2 14/02/12 08 23 31 INFO blockmanagement.BlockManager: shouldCheckForEnoughRacks = false 14/02/12 08 23 31 INFO blockmanagement.BlockManager: replicationRecheckInterval = 3 0 14/02/12 08 23 31 INFO blockmanagement.BlockManager: encryptDataTransfer = false 14/02/12 08 23 31 INFO namenode.FSNamesystem: fsOwner = rot (auth:SIMPLE) 14/02/12 08 23 31 INFO namenode.FSNamesystem: supergroup = supergroup 14/02/12 08 23 31 INFO namenode.FSNamesystem: isPermisionEnabled = true 14/02/12 08 23 31 INFO namenode.FSNamesystem: Determined nameservice ID: cluster2 14/02/12 08 23 31 INFO namenode.FSNamesystem: HA Enabled: true 14/02/12 08 23 31 INFO namenode.FSNamesystem: Apend Enabled: true 14/02/12 08 23 31 INFO util.GSet: Computing capacity for map INodeMap 14/02/12 08 23 31 INFO util.GSet: VM type = 64-bit 14/02/12 08 23 31 INFO util.GSet: 1.0% max memory = 96.7 MB 14/02/12 08 23 31 INFO util.GSet: capacity = 2^20 = 1048576 entries


14/02/12 08 23 31 INFO namenode.NameNode: Caching file names ocuring more than 10 times 14/02/12 08 23 31 INFO namenode.FSNamesystem: dfs.namenode.safemode.threshold-pct =

- 0. 9 012874603 14/02/12 08 23 31 INFO namenode.FSNamesystem: dfs.namenode.safemode.min.datanodes = 0 14/02/12 08 23 31 INFO namenode.FSNamesystem: dfs.namenode.safemode.extension = 3 0 14/02/12 08 23 31 INFO namenode.FSNamesystem: Retry cache on namenode is enabled 14/02/12 08 23 31 INFO namenode.FSNamesystem: Retry cache wil use 0.03 of total heap and retry cache entry expiry time is 6 0 milis 14/02/12 08 23 31 INFO util.GSet: Computing capacity for map Namenode Retry Cache 14/02/12 08 23 31 INFO util.GSet: VM type = 64-bit 14/02/12 08 23 31 INFO util.GSet: 0.02 93294746% max memory = 96.7 MB 14/02/12 08 23 31 INFO util.GSet: capacity = 2^15 = 32768 entries 14/02/12 08 23  3 INFO comon.Storage: Storage directory /usr/local/hadop/tmp/dfs/name has ben sucesfuly formated. 14/02/12 08 23  3 INFO namenode.FSImage: Saving image file /usr/local/hadop/tmp/dfs/name/curent/fsimage.ckpt_ 0 using no compresion 14/02/12 08 23  3 INFO namenode.FSImage: Image file /usr/local/hadop/tmp/dfs/name/curent/fsimage.ckpt_ 0 of size 196 bytes saved in 0 seconds. 14/02/12 08 23  3 INFO namenode. NStorageRetentionManager: Going to retain 1 images with txid >= 0 14/02/12 08 23  3 INFO util.ExitUtil: Exiting with status 0 14/02/12 08 23  3 INFO namenode.NameNode: SHUTDOWN_MSG: / *


- SHUTDOWN_MSG: Shuting down NameNode at hadop103/192.168.80.103


*/

- [rot@hadop103 hadop]# 【上⾯的输出可以看到/usr/local/hadop/tmp/dfs/name 被成功格式化了】 验证：


- [rot@hadop103 hadop]# ls tmp/ dfs journal


- [rot@hadop103 hadop]# ls tmp/dfs/ name


- [rot@hadop103 hadop]#


## 10.启动c2中刚才格式化的NameNode

- 在hadop103上执⾏命令：/home/hadop/hadop/sbin/hadop-daemon.sh start namenode 命令输出：

- [rot@hadop103 hadop]#/home/hadop/hadop/sbin/hadop-daemon.sh start namenode


- starting namenode, loging to /usr/local/hadop/logs/hadop-rot-namenode-hadop103.out


- [rot@hadop103 hadop]# 验证：


- [rot@hadop103 hadop]# jps 1290 JournalNode 1560 NameNode


10972 QuorumPerMain

160 Jps [rot@hadop103 hadop]# 也可以通过浏览器访问 ，可以看到如上图⻚⾯，此处省略截图。

- 在hadop104上执⾏命令：/home/hadop/hadop/bin/hdfs namenode -botstrapStandby 命令输出： [rot@hadop104 hadop]# /usr/local/hadop/bin/hdfs namenode -botstrapStandby 14/02/12 08 28 30 INFO namenode.NameNode: STARTUP_MSG: / * STARTUP_MSG: Starting NameNode


htp:/hadop103 5070

## 1.把NameNode的数据从hadop103同步到hadop104中

- STARTUP_MSG: host = hadop104/192.168.80.104 STARTUP_MSG: args = [-botstrapStandby] STARTUP_MSG: version = 2.2.0


STARTUP_MSG: claspath = /usr/local/hadop/etc/hadop:/usr/local/hadop/share/hadop/comon/lib/protobuf-java-

- 2.5.0.jar:/usr/local/hadop/share/hadop/comon/lib/snapy-java-


- 1.0.4.1.jar:/usr/local/hadop/share/hadop/comon/lib/jsch0.1.42.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/comon/lib/slf4j-log4j121.7.5.jar:/usr/local/hadop/share/hadop/comon/lib/comons-digester1.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-cli1.2.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-el1.0.jar:/usr/local/hadop/share/hadop/comon/lib/jets3t0.6.1.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/comon/lib/slf4j-api1.7.5.jar:/usr/local/hadop/share/hadop/comon/lib/xz1.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-lang2.5.jar:/usr/local/hadop/share/hadop/comon/lib/jasper-runtime5.5.23.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-xc1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/comons-codec1.4.jar:/usr/local/hadop/share/hadop/comon/lib/activation1.1.jar:/usr/local/hadop/share/hadop/comon/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/comon/lib/comons-htpclient3.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-beanutils1.7.0.jar:/usr/local/hadop/share/hadop/comon/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/comon/lib/jsp-api2.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-beanutils-core1.8.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-configuration1.6.jar:/usr/local/hadop/share/hadop/comon/lib/jaxb-impl-2.2.31.jar:/usr/local/hadop/share/hadop/comon/lib/zokeper3.4.5.jar:/usr/local/hadop/share/hadop/comon/lib/hadop-auth2.2.0.jar:/usr/local/hadop/share/hadop/comon/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/comon/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/comon/lib/stax-api1.0.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-colections3.2.1.jar:/usr/local/hadop/share/hadop/comon/lib/servlet-api2.5.jar:/usr/local/hadop/share/hadop/comon/lib/junit4.8.2.jar:/usr/local/hadop/share/hadop/comon/lib/jety-util-


6.1.26.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/jsr3051.3.9.jar:/usr/local/hadop/share/hadop/comon/lib/mockito-al1.8.5.jar:/usr/local/hadop/share/hadop/comon/lib/comons-loging1.1.1.jar:/usr/local/hadop/share/hadop/comon/lib/comons-math2.1.jar:/usr/local/hadop/share/hadop/comon/lib/jaxb-api2.2.2.jar:/usr/local/hadop/share/hadop/comon/lib/jersey-json1.9.jar:/usr/local/hadop/share/hadop/comon/lib/asm3.2.jar:/usr/local/hadop/share/hadop/comon/lib/comons-net3.1.jar:/usr/local/hadop/share/hadop/comon/lib/jety6.1.26.jar:/usr/local/hadop/share/hadop/comon/lib/jasper-compiler5.5.23.jar:/usr/local/hadop/share/hadop/comon/lib/guava-

- 1.0.2.jar:/usr/local/hadop/share/hadop/comon/lib/xmlenc-


0.52.jar:/usr/local/hadop/share/hadop/comon/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/comon/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/comon/lib/jackson-jaxrs1.8.8.jar:/usr/local/hadop/share/hadop/comon/lib/jetison1.1.jar:/usr/local/hadop/share/hadop/comon/hadop-comon-2.2.0tests.jar:/usr/local/hadop/share/hadop/comon/hadop-comon2.2.0.jar:/usr/local/hadop/share/hadop/comon/hadop-nfs2.2.0.jar:/usr/local/hadop/share/hadop/hdfs:/usr/local/hadop/share/hadop/hdfs/lib/protobufjava-2.5.0.jar:/usr/local/hadop/share/hadop/hdfs/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-cli1.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-el1.0.jar:/usr/local/hadop/share/hadop/hdfs/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-daemon1.0.13.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-lang2.5.jar:/usr/local/hadop/share/hadop/hdfs/lib/jasper-runtime5.5.23.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-codec1.4.jar:/usr/local/hadop/share/hadop/hdfs/lib/jsp-api2.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/hdfs/lib/servlet-api2.5.jar:/usr/local/hadop/share/hadop/hdfs/lib/jety-util6.1.26.jar:/usr/local/hadop/share/hadop/hdfs/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/hdfs/lib/jsr305-

1.3.9.jar:/usr/local/hadop/share/hadop/hdfs/lib/comons-loging1.1.1.jar:/usr/local/hadop/share/hadop/hdfs/lib/asm3.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/jety6.1.26.jar:/usr/local/hadop/share/hadop/hdfs/lib/guava-

- 1.0.2.jar:/usr/local/hadop/share/hadop/hdfs/lib/xmlenc-

0.52.jar:/usr/local/hadop/share/hadop/hdfs/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-nfs2.2.0.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-2.2.0tests.jar:/usr/local/hadop/share/hadop/hdfs/hadop-hdfs-

- 2.2.0.jar:/usr/local/hadop/share/hadop/yarn/lib/protobuf-java2.5.0.jar:/usr/local/hadop/share/hadop/yarn/lib/snapy-java1.0.4.1.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/guice3.0.jar:/usr/local/hadop/share/hadop/yarn/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/xz1.0.jar:/usr/local/hadop/share/hadop/yarn/lib/hamcrest-core1.1.jar:/usr/local/hadop/share/hadop/yarn/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/yarn/lib/aopaliance1.0.jar:/usr/local/hadop/share/hadop/yarn/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/yarn/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/yarn/lib/javax.inject1.jar:/usr/local/hadop/share/hadop/yarn/lib/guice-servlet3.0.jar:/usr/local/hadop/share/hadop/yarn/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/yarn/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/yarn/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/yarn/lib/asm-
- 3.2.jar:/usr/local/hadop/share/hadop/yarn/lib/junit-
- 4.10.jar:/usr/local/hadop/share/hadop/yarn/lib/jersey-guice1.9.jar:/usr/local/hadop/share/hadop/yarn/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/yarn/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-api2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-tests2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-site2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-client2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-nodemanager2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-aplications-distributedshel-


2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-resourcemanager2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-web-proxy2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-aplications-unmanaged-amlauncher-2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-server-comon2.2.0.jar:/usr/local/hadop/share/hadop/yarn/hadop-yarn-comon-

- 2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/protobuf-java2.5.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/snapy-java1.0.4.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-server1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/guice3.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jackson-core-asl1.8.8.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-core1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/xz1.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/hamcrest-core1.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/avro1.7.4.jar:/usr/local/hadop/share/hadop/mapreduce/lib/aopaliance1.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/hadop-anotations2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/comons-compres1.4.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/javax.inject1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/guice-servlet3.0.jar:/usr/local/hadop/share/hadop/mapreduce/lib/comons-io2.1.jar:/usr/local/hadop/share/hadop/mapreduce/lib/nety3.6.2.Final.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jackson-maper-asl1.8.8.jar:/usr/local/hadop/share/hadop/mapreduce/lib/asm-
- 3.2.jar:/usr/local/hadop/share/hadop/mapreduce/lib/junit-
- 4.10.jar:/usr/local/hadop/share/hadop/mapreduce/lib/jersey-guice1.9.jar:/usr/local/hadop/share/hadop/mapreduce/lib/log4j1.2.17.jar:/usr/local/hadop/share/hadop/mapreduce/lib/paranamer2.3.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-hs-plugins2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-jobclient-2.2.0tests.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-jobclient2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-ap2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-core2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-shufle2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-examples2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-hs2.2.0.jar:/usr/local/hadop/share/hadop/mapreduce/hadop-mapreduce-client-comon2.2.0.jar:/usr/local/hadop/contrib/capacity-scheduler/*.jar


STARTUP_MSG: build = Unknown -r Unknown; compiled by 'rot' on 2013-12-26T08 50Z STARTUP_MSG: java = 1.7.0_45

*/ 14/02/12 08 28 35 INFO namenode.NameNode: registered UNIX signal handlers for [TERM, HUP, INT]

= About to botstrap Standby ID hadop104 from:

Nameservice ID: cluster2

Other Namenode ID: hadop103 Other N's HTP adres: hadop103 5070 Other N's IPC adres: hadop103/192.168.80.103 9 0

Namespace ID: 698609742 Block pol ID: BP-130458237-192.168.80.103-1392164613254 Cluster ID: c2 Layout version: -47

= 14/02/12 08 28 39 INFO comon.Storage: Storage directory /usr/local/hadop/tmp/dfs/name has ben sucesfuly formated.

- 14/02/12 08 28 39 INFO namenode.TransferFsImage: Opening conection to
- 14/02/12 08 28 40 INFO namenode.TransferFsImage: Transfer tok 0.67s at 0.0 KB/s 14/02/12 08 28 40 INFO namenode.TransferFsImage: Downloaded file fsimage.ckpt_ 0 size 196 bytes. 14/02/12 08 28 40 INFO util.ExitUtil: Exiting with status 0 14/02/12 08 28 40 INFO namenode.NameNode: SHUTDOWN_MSG: / *


htp:/hadop103 50 70/getimage?getimage=1&txid=0&storageInfo=-47 698609742 0:c2

- SHUTDOWN_MSG: Shuting down NameNode at hadop104/192.168.80.104


*/ 验证：

- [rot@hadop104 hadop]# pwd /usr/local/hadop [rot@hadop104 hadop]# ls tmp/ dfs


- [rot@hadop104 hadop]# ls tmp/dfs/ name


- [rot@hadop104 hadop]#


## 12.启动c2中另⼀个Namenode

- 在hadop104上执⾏命令：/home/hadop/hadop/sbin/hadop-daemon.sh start namenode


命令输出：

- [rot@hadop104 hadop]# /home/hadop/hadop/sbin/hadop-daemon.sh start namenode


- starting namenode, loging to /usr/local/hadop/logs/hadop-rot-namenode-hadop104.out


- [rot@hadop104 hadop]# 验证：


- [rot@hadop104 hadop]# jps 82 NameNode 8975 Jps


- [rot@hadop104 hadop]# 也可以通过浏览器访问 ，可以看到如上图⻚⾯，此处省略截图。


htp:/hadop104 5070

- 13.启动所有的DataNode
- 14.启动Yarn


在hadop101上执⾏命令：/home/hadop/hadop/sbin/hadop-daemon.sh start datanode 命令输出： [rot@hadop101 hadop]# /usr/local/hadop/sbin/hadop-daemons.sh start datanode

- hadop101: starting datanode, loging to /usr/local/hadop/logs/hadop-rot-datanode-

- hadop101.out hadop103: starting datanode, loging to /usr/local/hadop/logs/hadop-rot-datanode-

- hadop103.out

hadop102: starting datanode, loging to /usr/local/hadop/logs/hadop-rot-datanodehadop102.out hadop104: starting datanode, loging to /usr/local/hadop/logs/hadop-rot-datanode-

- hadop104.out [rot@hadop101 hadop]# 【上述命令会在四个节点分别启动DataNode进程】 验证（以hadop101为例）： [rot@hadop101 hadop]# jps 2396 JournalNode 24302 Jps 24232 DataNode 2358 NameNode 2491 QuorumPerMain [rot@hadop101 hadop]# 【可以看到java进程DataNode】






在hadop101上执⾏命令：/usr/local/hadop/sbin/start-yarn.sh 命令输出： [rot@hadop101 hadop]# /usr/local/hadop/sbin/start-yarn.sh starting yarn daemons

starting resourcemanager, loging to /usr/local/hadop/logs/yarn-rot-resourcemanagerhadop101.out hadop104: starting nodemanager, loging to /usr/local/hadop/logs/yarn-rot-nodemanagerhadop104.out

- hadop103: starting nodemanager, loging to /usr/local/hadop/logs/yarn-rot-nodemanager-


- hadop103.out hadop102: starting nodemanager, loging to /usr/local/hadop/logs/yarn-rot-nodemanagerhadop102.out hadop101: starting nodemanager, loging to /usr/local/hadop/logs/yarn-rot-nodemanagerhadop101.out [rot@hadop101 hadop]# 验证： [rot@hadop101 hadop]# jps 2396 JournalNode 25154 ResourceManager 25247 NodeManager 24232 DataNode 2358 NameNode 2491 QuorumPerMain 25281 Jps [rot@hadop101 hadop]# 【产⽣java进程ResourceManager和NodeManager】 也可以通过浏览器访问，如下图


- 15.启动ZoKeperFailoverControler
- 16.验证HDFS是否好⽤


在hadop101、hadop102、hadop103、hadop104上分别执⾏命 令：/usr/local/hadop/sbin/hadop-daemon.sh start zkfc 命令输出（以hadop101为例）： [rot@hadop101 hadop]# /usr/local/hadop/sbin/hadop-daemon.sh start zkfc starting zkfc, loging to /usr/local/hadop/logs/hadop-rot-zkfc-hadop101.out [rot@hadop101 hadop]# 验证（以hadop101为例）： [rot@hadop101 hadop]# jps 2459 DFSZKFailoverControler 2396 JournalNode 24232 DataNode 2358 NameNode 2491 QuorumPerMain 24654 Jps [rot@hadop101 hadop]# 【产⽣java进程DFSZKFailoverControler】

在任意⼀个节点上执⾏以下命令（这⾥以hadop101为例），把数据上传到HDFS集群中

[rot@hadop101 hadop]# pwd /usr/local/hadop/etc/hadop [rot@hadop101 hadop]# ls capacity-scheduler.xml hadop-metrics.properties htpfs-site.xml slserver.xml.example configuration.xsl hadop-policy.xml log4j.properties startal.sh container-executor.cfg hdfs2-site.xml mapred-env.sh yarn-env.sh core-site.xml hdfs-site.xml mapred-queues.xml.template yarn-site.xml fairscheduler.xml htpfs-env.sh mapred-site.xml zokeper.out hadop-env.sh htpfs-log4j.properties slaves hadop-metrics2.properties htpfs-signature.secret sl-client.xml.example [rot@hadop101 hadop]# hadop fs -put core-site.xml / 【上传到集群中，默认是上传到HDFS联盟的c1集群中】 验证： [rot@hadop101 hadop]# hadop fs -ls /

- Found 1 items


-rw-r-r- 2 rot supergroup 46 2014-02-12 09  0 /core-site.xml [rot@hadop101 hadop]# 也可以通过浏览器查看，数据默认是放在第⼀个集群中的

## 17.验证Yarn是否好⽤

- 在hadop101上执⾏以下命令 hadop jar /usr/local/hadop/share/hadop/mapreduce/hadopmapreduce-examples-2.2.0.jar wordcount /core-site.xml /out 命令输出： [rot@hadop101 hadop]# hadop jar /usr/local/hadop/share/hadop/mapreduce/hadopmapreduce-examples-2.2.0.jar wordcount /core-site.xml /out 14/02/121 43  5 INFO client.RMProxy: Conecting to ResourceManager at hadop101/192.168.80.101 8032 14/02/121 43 59 INFO input.FileInputFormat: Total input paths to proces : 1 14/02/121 43 59 INFO mapreduce.JobSubmiter: number of splits:1 14/02/121 43 59 INFO Configuration.deprecation: user.name is deprecated. Instead, use mapreduce.job.user.name 14/02/121 43 59 INFO Configuration.deprecation: mapred.jar is deprecated. Instead, use mapreduce.job.jar 14/02/121 43 59 INFO Configuration.deprecation: mapred.output.value.clas is deprecated. Instead, use mapreduce.job.output.value.clas


14/02/121 43 59 INFO Configuration.deprecation: mapreduce.combine.clas is deprecated. Instead, use mapreduce.job.combine.clas 14/02/121 43 59 INFO Configuration.deprecation: mapreduce.map.clas is deprecated. Instead, use mapreduce.job.map.clas 14/02/121 43 59 INFO Configuration.deprecation: mapred.job.name is deprecated. Instead, use mapreduce.job.name 14/02/121 43 59 INFO Configuration.deprecation: mapreduce.reduce.clas is deprecated. Instead, use mapreduce.job.reduce.clas 14/02/121 43 59 INFO Configuration.deprecation: mapred.input.dir is deprecated. Instead, use mapreduce.input.fileinputformat.inputdir 14/02/121 43 59 INFO Configuration.deprecation: mapred.output.dir is deprecated. Instead, use mapreduce.output.fileoutputformat.outputdir 14/02/121 43 59 INFO Configuration.deprecation: mapred.map.tasks is deprecated. Instead, use mapreduce.job.maps 14/02/121 43 59 INFO Configuration.deprecation: mapred.output.key.clas is deprecated. Instead, use mapreduce.job.output.key.clas 14/02/121 43 59 INFO Configuration.deprecation: mapred.working.dir is deprecated. Instead, use mapreduce.job.working.dir 14/02/121  4 01 INFO mapreduce.JobSubmiter: Submiting tokens for job: job_139216950619_ 02

- 14/02/121  4 04 INFO impl.YarnClientImpl: Submited aplication aplication_139216950619_ 02 to ResourceManager at hadop101/192.168.80.101 8032
- 14/02/121  4 05 INFO mapreduce.Job: The url to track the job:


htp:/hadop101 808/proxy/apli cation_139216950619_ 02/

14/02/121  4 05 INFO mapreduce.Job: Runing job: job_139216950619_ 02 14/02/121  4 41 INFO mapreduce.Job: Job job_139216950619_ 02 runing in uber mode : false 14/02/121  4 41 INFO mapreduce.Job: map 0% reduce 0%

- 14/02/121 45 37 INFO mapreduce.Job: map 10% reduce 0%
- 14/02/121 46 54 INFO mapreduce.Job: map 10% reduce 10%
- 14/02/121 47 01 INFO mapreduce.Job: Job job_139216950619_ 02 completed sucesfuly 14/02/121 47 02 INFO mapreduce.Job: Counters: 43


File System Counters FILE: Number of bytes read=472 FILE: Number of bytes writen=164983 FILE: Number of read operations=0 FILE: Number of large read operations=0

FILE: Number of write operations=0 HDFS: Number of bytes read=540 HDFS: Number of bytes writen=402 HDFS: Number of read operations=6 HDFS: Number of large read operations=0 HDFS: Number of write operations=2

Job Counters Launched map tasks=1 Launched reduce tasks=1 Data-local map tasks=1 Total time spent by al maps in ocupied slots (ms)=63094 Total time spent by al reduces in ocupied slots (ms)=5728

Map-Reduce Framework Map input records=17 Map output records=20 Map output bytes=496 Map output materialized bytes=472 Input split bytes=94 Combine input records=20 Combine output records=16 Reduce input groups=16 Reduce shufle bytes=472 Reduce input records=16 Reduce output records=16 Spiled Records=32 Shufled Maps =1 Failed Shufles=0 Merged Map outputs=1 GC time elapsed (ms)=632 CPU time spent (ms)=3010 Physical memory (bytes) snapshot=2 528960 Virtual memory (bytes) snapshot=167847168 Total comited heap usage (bytes)=12 60608

Shufle Erors BAD_ID=0 CONECTION=0 IO_EROR=0

WRONG_LENGTH=0 WRONG_MAP=0 WRONG_REDUCE=0

File Input Format Counters Bytes Read=46

File Output Format Counters Bytes Writen=402

- [rot@hadop101 hadop]# 验证：


- [rot@hadop101 hadop]# hadop fs -ls /out


- Found 2 items


- -rw-r-r- 2 rot supergroup 0 2014-02-121 46 /out/_SUCES
- -rw-r-r- 2 rot supergroup 402 2014-02-121 46 /out/part-r- 0


- [rot@hadop101 hadop]# hadop fs -text /out/part-r- 0 </configuration> 1 </property> 3 <?xml 1 <?xml-styleshet 1 <configuration> 1 <name>fs.defaultFS</name> 1 <name>ha.zokeper.quorum</name> 1 <name>hadop.tmp.dir</name> 1 <property> 3 <value>/usr/local/hadop/tmp</value> 1 <value>hadop101 2181,hadop102 2181,hadop103 2181</value> 1 <value>hdfs:/cluster1</value> 1 encoding="UTF-8"?> 1 href="configuration.xsl"?> 1 type="text/xsl" 1 version="1.0" 1


- [rot@hadop101 hadop]#


## 18.验证HA的故障⾃动转移是否好⽤

观察cluster1的两个NameNode的状态，hadop101的状态是standby，hadop102的状态是active， 如下图。

下⾯我们杀死hadop102的NameNode进程，观察hadop101的状态是否会⾃动切换成active。

- 在hadop102执⾏以下命令


- [rot@hadop102 hadop]# jps 1389 DFSZKFailoverControler 1235 JournalNode 13056 DataNode 1560 Jps 1496 NodeManager 12573 NameNode 12081 QuorumPerMain


- [rot@hadop102 hadop]# kil -9 12573


- [rot@hadop102 hadop]# jps 1389 DFSZKFailoverControler 1235 JournalNode 13056 DataNode 1496 NodeManager 15671 Jps 12081 QuorumPerMain


- [rot@hadop102 hadop]# 再观察⻚⾯，发现如下图所示


证明HDFS的⾼可靠是可⽤的。

