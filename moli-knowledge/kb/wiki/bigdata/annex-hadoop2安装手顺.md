---
title: hadoop2安装手顺.note（原文插图 annex）
slug: annex-hadoop2安装手顺
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/a安装文档/hadoop2安装手顺.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

hadop2安装⽂档.docx 41.75KB

⼀、环境准备，参看hadop安装⼿顺（⼀到四章节 jdksh）

•服务器规划如下：

- master1: 192.168.10.201 主机名：master1 (active namenode,RM)

- master1-ha: 192.168.10.202 主机名：master1ha (standby namenode，jn)

master2: 192.168.10.203 主机名：master2 (active namenode,jn)

- master2-ha: 192.168.10.204 主机名：master2ha (standby namenode，jn)




- slave1: 192.168.10.205 主机名：slave1 (datanode,nodemanager)
- slave2: 192.168.10.206 主机名：slave2 (datanode,nodemanager)
- slave3: 192.168.10.207 主机名：slave3 (datanode,nodemanager)


master:active namenode,RM masterha:standby namenode，jn

- slave1：datanode,nodemanager，jn，active namenode

- slave2：datanode,nodemanager，jn，standby namenode

- slave3：datanode,nodemanager


⼆、找三台机器安装zokeper， 本例中三台服务器为masterha1、masterha2和master2 三、安装hadop2 vi /etc/profile export HADOP_HOME=/home/hadop/hadop export PATH=$PATH:$HADOP_HOME/sbin

- 1.解压缩hadop-2.2.0.tar.gz 并改名为hadop2 添加环境变量HADOP_HOME、PATH（注意除了bin ⽬录外还有sbin⽬录）
- 2.cd ~/hadop 创建以下⽬录 权限设置为75（mkdir -m 75 x） mkdir -m 75 namedir mkdir -m 75 datadir mkdir -m 75 tmp mkdir -m 75 jndir mkdir -m 75 hadopmrsys mkdir -m 75 hadopmrlocal mkdir -m 75 nodemanagerlocal


mkdir -m 75 nodemanagerlogs

cd /home/hadop/hadop2/etc/hadop

hadop2配置⽂件.zip 7.96KB

配置⽂件.txt 7. 6KB

修改core-site.xml <configuration>

<property>

<name>fs.defaultFS</name>

<value>viewfs:///</value>

</property>

<property>

<name>fs.viewfs.mounttable.default.link./tmp</name>

- <value>hdfs://hadoop-cluster1/tmp</value>

</property>

<property>

<name>fs.viewfs.mounttable.default.link./tmp2</name>

- <value>hdfs://hadoop-cluster2/tmp2</value>


</property>

</configuration>

修改hdfs-site.xml <configuration>

<!-使⽤federation时，使⽤了2个HDFS集群。这⾥抽象出两个NameService实际上就是给这2个HDFS集群起了个别 名。名字可以随便起，相互不重复即可 ->

<property> <name>dfs.nameservices</name>

<value>hadop-cluster1,hadop-cluster2</value> </property>

- <!-cluster1 start-> <!-指定NameService是cluster1时的namenode有哪些，这⾥的值也是逻辑名称，名字随便起，相互不重复即可 ->


<property> <name>dfs.ha.namenodes.hadop-cluster1</name> <value>n1,n1ha</value>

</property> <!-指定 n1的RPC地址 ->

<property> <name>dfs.namenode.rpc-adres.hadop-cluster1.n1</name> <value>master1 9 0</value>

</property> <!-指定 n1ha的RPC地址 ->

<property> <name>dfs.namenode.rpc-adres.hadop-cluster1.n1ha</name> <value>master1ha:9 0</value>

</property> <!-指定 n1的http地址 ->

<property> <name>dfs.namenode.htp-adres.hadop-cluster1.n1</name> <value>master1 5070</value>

</property> <!-指定 n1ha的http地址 ->

<property> <name>dfs.namenode.htp-adres.hadop-cluster1.n1ha</name> <value>master1ha:5070</value>

</property> <!-指定 n1的secondary的http地址 ->

<property> <name>dfs.namenode.secondary.htp-adres.hadop-cluster1.n1</name> <value>master1 901</value>

</property> <!-指定 n1ha的secondary的http地址 ->

<property> <name>dfs.namenode.secondary.htp-adres.hadop-cluster1.n1ha</name> <value>master1ha:901</value>

</property> <!-指定cluster1出故障时，哪个实现类负责执⾏故障切换 -> <property> <name>dfs.client.failover.proxy.provider.hadop-cluster1</name>

<value>org.apache.hadop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value> </property>

- <!-cluster1 end->

- <!-cluster2 start-> <property>


<name>dfs.ha.namenodes.hadop-cluster2</name> <value>n2,n2ha</value>

</property> <property>

<name>dfs.namenode.rpc-adres.hadop-cluster2.n2</name> <value>master2 9 0</value>

</property> <property>

<name>dfs.namenode.rpc-adres.hadop-cluster2.n2ha</name> <value>master2ha:9 0</value>

</property> <property>

<name>dfs.namenode.htp-adres.hadop-cluster2.n2</name> <value>master2 5070</value>

</property> <property>

<name>dfs.namenode.htp-adres.hadop-cluster2.n2ha</name> <value>master2ha:5070</value>

</property> <property>

<name>dfs.namenode.secondary.htp-adres.hadop-cluster2.n2</name> <value>master2 901</value>

</property> <property>

<name>dfs.namenode.secondary.htp-adres.hadop-cluster2.n2ha</name> <value>master2ha:901</value>

</property> <property>

<name>dfs.client.failover.proxy.provider.hadop-cluster2</name> <value>org.apache.hadop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value>

</property>

- <!-cluster2 end-> <property>


<name>dfs.namenode.name.dir</name> <value>/home/hadop/hadop/namedir</value>

</property>

- <!-nn1共享⽂件夹地址 -> <property>

<name>dfs.namenode.shared.edits.dir.hadop-cluster1.n1</name> <value>qjournal:/master1ha:8485;master2 8485;master2ha:8485/cluster1</value>

</property>

- <!-nn1ha共享⽂件夹地址 -> <property>

<name>dfs.namenode.shared.edits.dir.hadop-cluster1.n1ha</name> <value>qjournal:/master1ha:8485;master2 8485;master2ha:8485/cluster1</value>

</property> <!-nn2共享⽂件夹地址 ->

<property> <name>dfs.namenode.shared.edits.dir.hadop-cluster2.n2</name> <value>qjournal:/master1ha:8485;master2 8485;master2ha:8485/cluster2</value>

</property>

- <!-nn2ha共享⽂件夹地址 -> <property>




<name>dfs.namenode.shared.edits.dir.hadop-cluster2.n2ha</name> <value>qjournal:/master1ha:8485;master2 8485;master2ha:8485/cluster2</value>

</property> <!-datanode⽂件存放地址 ->

<property> <name>dfs.datanode.data.dir</name> <value>/home/hadop/hadop/datadir</value>

</property>

<property> <name>ha.zokeper.quorum</name> <value>master1 2181,master1ha:2181,master2 2181</value>

</property> <!-⼀旦需要NameNode切换，使⽤ssh⽅式进⾏操作 -> <property> <name>dfs.ha.fencing.methods</name>

<value>shfence</value> </property>

<!- 超时 ->

<property> <name>ha.zokeper.sesion-timeout.ms</name> <value>5 0</value>

</property> <!-指定集群是否启动⾃动故障恢复，即当NameNode出故障时，是否⾃动切换到另⼀台NameNode ->

<property> <name>dfs.ha.automatic-failover.enabled</name> <value>true</value>

</property> <!-指定JournalNode集群在对NameNode的⽬录进⾏共享时，⾃⼰存储数据的磁盘路径 ->

<property> <name>dfs.journalnode.edits.dir</name> <value>/home/hadop/hadop/jndir</value>

</property> <!-指定DataNode存储block的副本数量。默认值是3个 ->

<property> <name>dfs.replication</name> <value>2</value>

</property> <!-不做权限 ->

<property> <name>dfs.permision</name> <value>false</value>

</property> <!-是否允许web访问 ->

<property> <name>dfs.webhdfs.enabled</name> <value>true</value>

</property> <!-是否⽀持append ->

<property> <name>dfs.suport.apend</name> <value>true</value>

</property> <property>

<name>hadop.proxyuser.hduser.hosts</name> <value>*</value>

</property> <property>

<name>hadop.proxyuser.hduser.groups</name> <value>*</value>

</property> <!-如果使⽤ssh进⾏故障切换，使⽤ssh通信时⽤的密钥存储的位置 ->

<property> <name>dfs.ha.fencing.sh.private-key-files</name> <value>/home/hadop/.sh/id_rsa</value>

</property> </configuration>

cp mapred-site.xml.template mapred-site.xml 修改mapred-site.xml <configuration> <property> <name>mapreduce.framework.name</name> <value>yarn</value>

</property> <property>

<name>mapreduce.job.tracker</name> <value>master1 5431</value>

</property> <property>

<name>mapreduce.jobhistory.adres</name> <value>master1 1020</value>

</property> <property>

<name>mapreduce.jobhistory.webap.adres</name> <value>master1 19 8</value>

</property> <property>

<name>mapred.system.dir</name> <value>/home/hadop/hadop/hadopmrsys</value> <final>true</final>

</property> <property>

<name>mapred.local.dir</name> <value>/home/hadop/hadop/hadopmrlocal</value> <final>true</final>

</property> </configuration>

修改yarn-site.xml <configuration> <property> <name>yarn.nodemanager.aux-services</name> <value>mapreduce_shufle</value>

</property> <property>

<name>yarn.nodemanager.aux-services.mapreduce.shufle.clas</name> <value>org.apache.hadop.mapred.ShufleHandler</value>

</property> <property>

<name>yarn.nodemanager.local-dirs</name> <value>/home/hadop/hadop/nodemanagerlocal</value>

</property> <property>

<name>yarn.nodemanager.log-dirs</name> <value>/home/hadop/hadop/nodemanagerlogs</value>

</property> <property>

<name>yarn.nodemanager.remote-ap-log-dir</name> <value>/home/hadop/hadop/nodemanageremote</value>

</property> <!-⾃定ResourceManager的地址，还是单点，这是隐患 ->

<property> <name>yarn.resourcemanager.adres</name> <value>master1 18032</value>

</property> <property>

<name>yarn.resourcemanager.scheduler.adres</name>

- <value>master1 18030</value>

</property> <property>

<name>yarn.resourcemanager.resource-tracker.adres</name>

- <value>master1 18031</value>


</property> <property>

<name>yarn.resourcemanager.admin.adres</name> <value>master1 1803</value>

</property> <property>

<name>yarn.resourcemanager.webap.adres</name> <value>master1 1808</value>

</property> </configuration>

修改slaves

- slave1
- slave2
- slave3


修改hadop-env.sh

就是修改这⼀⾏内容，修改后的结果如下

export JAVA_HOME=/usr/jdk

【这⾥的JAVA_HOME的值是jdk的安装路径。如果你那⾥不⼀样，请修改为⾃⼰的地址】

修改yarn-env.sh

export JAVA_HOME=/usr/jdk

四、将配置好的hadop分发到其余服务器上。

- scp -r /home/hadop/hadop hadop@192.168.10.202:/home/hadop/hadop
- scp -r /home/hadop/hadop hadop@192.168.10.203:/home/hadop/hadop
- scp -r /home/hadop/hadop hadop@192.168.10.204:/home/hadop/hadop


- scp -r /home/hadop/hadop hadop@192.168.10.205:/home/hadop/hadop
- scp -r /home/hadop/hadop hadop@192.168.10.206:/home/hadop/hadop
- scp -r /home/hadop/hadop hadop@192.168.10.207:/home/hadop/hadop
- scp -r /home/hadop/hadop hadop@192.168.10.208:/home/hadop/hadop


五、启动hdfs

- 5.1启动ZooKeeper集群

- 5.2格式化ZooKeeper集群，⽬的是在ZooKeeper集群上建⽴HA的相 应节点。


启动master1、master1ha 、master2节点上的zokeper

zkServer.sh start 验证：

[hadop@master1 sbin]$ jps 760 QuorumPerMain [hadop@master1 hadop]# zkServer.sh status JMX enabled by default Using config: /usr/local/zookeeper/bin/../conf/zoo.cfg Mode: follower

在master1和master2节点上执⾏： /home/hadop/hadop/bin/hdfs zkfc -formatZK

验证：

[root@master1 hadoop]# zkCli.sh

- [zk: localhost:2181(CONNECTED) 0] ls / [hadoop-ha, zookeeper]

- [zk: localhost:2181(CONNECTED) 1] ls /hadoop-ha [cluster2, cluster1]


[zk: localhost:2181(CONNECTED) 2]

【集群c2也格式化，产⽣⼀个新的ZK节点cluster2】

## 5.3启动JournalNode集群

在master1ha、master2、master2ha节点上执⾏： /home/hadop/hadop/sbin/hadop-daemon.sh start journalnode

命令输出(以master1为例)：

[root@master1 hadoop]# /usr/local/hadoop/sbin/hadoop-daemon.sh start journalnode

starting journalnode, logging to /usr/local/hadoop/logs/hadoop-root-journalnode-hadoop101.out

[root@master1 hadoop]#

在每个节点执⾏完启动命令后，每个节点都执⾏以下验证。

[root@master1 hadoop]# jps

23396 JournalNode

23598 Jps

22491 QuorumPeerMain

[root@master1 hadoop]#

【产⽣⼀个java进程JournalNode】

## 5.4格式化master1的NameNode

- 在master1节点上执⾏： /home/hadop/hadop/bin/hdfs namenode -format -clusterId helokity


验证：

[root@master1 hadoop]# ls temp/

dfs

[root@master1 hadoop]# ls temp/dfs/

name

## 5.5启动master1中刚才格式化的NameNode

/home/hadop/hadop/sbin/hadop-daemon.sh start namenode

验证：

[root@master1 hadoop]# jps

23396 JournalNode

23598 Jps

23558 NameNode

22491 QuorumPeerMain

[root@master1 hadoop]#

【启动后，产⽣⼀个新的java进程NameNode】

通过浏览器访问，也可以看到下图所示

![image 1](assets/imageFile1.png)

## 5.6把NameNode的数据从master1同步到master1ha中

- 在master1ha节点上执⾏： /home/hadop/hadop/bin/hdfs namenode -botstrapStandby


验证： [root@master1hahadoop]# jps

## 5.7启动master1ha 的Namenode

/home/hadoop/hadoop/sbin/hadoop-daemon.sh start namenode

[root@master1hahadoop]# jps

12355 JournalNode

12611 Jps

12573 NameNode 12081 QuorumPeerMain [root@master1hahadoop]# 【产⽣java进程NameNode】 通过浏览器访问，也可以看到下图所示

![image 2](assets/imageFile2.png)

# 5.8将master1置成active状态

- 在master1,master1ha节点上执⾏：（将master1置成active状态） /home/hadop/hadop/sbin/hadop-daemon.sh start zkfc


- 或者：$bin/hdfs hadmin -ns hadop-cluster1 -transitionToActive n1 这个是⼿动的⽅法(在master1 上操作)， 如果前⾯的配置不是⾃动切换可以⽤这个 验证： 刷新浏览器


5.9格式化集群master2的⼀个NameNode

- 在master2节点上执⾏： /home/hadop/hadop/bin/hdfs namenode -format -clusterId helokity


验证： [root@master2hadoop]#jps 5.10启动master2刚才格式化的NameNode

/home/hadoop/hadoop/sbin/hadoop-daemon.sh start namenode

验证：

[root@hadoop103 hadoop]# jps

11290 JournalNode

11560 NameNode

10972 QuorumPeerMain

11600 Jps

- [root@hadoop103 hadoop]#

也可以通过浏览器访问http://hadoop103:50070，可以看到如上图⻚⾯，此处省略截图。

5.11把NameNode的数据从master2同步到master2ha中

验证：

- [root@hadoop104 hadoop]# jps


- 在master2ha节点上执⾏： /home/hadop/hadop/bin/hdfs namenode -botstrapStandby


## 5.12.启动master2ha的Namenode

/home/hadop/hadop/sbin/hadop-daemon.sh start namenode

[root@hadoop104 hadoop]# jps

8822 NameNode

8975 Jps

[root@hadoop104 hadoop]#

也可以通过浏览器访问http://hadoop104:50070，可以看到如上图⻚⾯，此处省略截图。

# 5.13将master2置成active状态

- 在master2,master2ha节点上执⾏：（将master2置成active状态） /home/hadop/hadop/sbin/hadop-daemon.sh start zkfc


- 或者：$bin/hdfs hadmin -ns hadop-cluster2 -transitionToActive n3


## 5.14启动所有的DataNode

在slave1、slave2、slave3节点上执⾏：（启动datanode） /home/hadop/hadop/sbin/hadop-daemon.sh start datanode

验证hdfs：

htp:/master1 5070

- hadop fs -mkdir hdfs:/hadop-cluster1/tmp3
- hadop fs -mkdir hdfs:/hadop-cluster2/tmp4 hadop fs -ls /


/hadop shel命令验证 /注意操作的时候需要这样写了：hadop fs -mkdir hdfs:/hadop-cluster1/ a /hadop fs -ls hdfs:/hadop-cluster1

六、启动mapreduce和yarn 在master1节点上执⾏： /home/hadop/hadop/sbin/start-yarn.sh

命令输出： [root@master1hadoop]# /usr/local/hadoop/sbin/start-yarn.sh starting yarn daemons starting resourcemanager, logging to /usr/local/hadoop/logs/yarn-root-resourcemanager-master1.out slave3: starting nodemanager, logging to /usr/local/hadoop/logs/yarn-root-nodemanager-slave3.out

slave2: starting nodemanager, logging to /usr/local/hadoop/logs/yarn-root-nodemanager-slave2.out slave1: starting nodemanager, logging to /usr/local/hadoop/logs/yarn-root-nodemanager-slave1.out [root@master1hadoop]# 验证： [root@master1hadoop]# jps 23396 JournalNode 25154 ResourceManager 25247 NodeManager 24232 DataNode 23558 NameNode 22491 QuorumPeerMain 25281 Jps [root@master1hadoop]# 【产⽣java进程ResourceManager和NodeManager】 也可以通过浏览器访问，如下图

![image 3](assets/imageFile3.png)

### 验证HDFS是否好⽤

在任意⼀个节点上执⾏以下命令（这⾥以hadoop101为例），把数据上传到HDFS集群中

[root@hadoop101 hadoop]# pwd

/usr/local/hadoop/etc/hadoop

[root@hadoop101 hadoop]# ls

capacity-scheduler.xml hadoop-metrics.properties httpfs-site.xml ssl-server.xml.example

configuration.xsl hadoop-policy.xml log4j.properties startall.sh

container-executor.cfg hdfs2-site.xml mapred-env.sh yarn-env.sh

core-site.xml hdfs-site.xml mapred-queues.xml.template yarn-site.xml

fairscheduler.xml httpfs-env.sh mapred-site.xml zookeeper.out

hadoop-env.sh httpfs-log4j.properties slaves

hadoop-metrics2.properties httpfs-signature.secret ssl-client.xml.example

[root@hadoop101 hadoop]# hadoop fs -put core-site.xml /

【上传到集群中，默认是上传到HDFS联盟的c1集群中】

验证：

[root@hadoop101 hadoop]# hadoop fs -ls /

- Found 1 items


-rw-r--r-- 2 root supergroup 446 2014-02-12 09:00 /core-site.xml

[root@hadoop101 hadoop]#

也可以通过浏览器查看，数据默认是放在第⼀个集群中的

![image 4](assets/imageFile4.png)

### 验证Yarn是否好⽤

- 在hadoop101上执⾏以下命令 hadoop jar /usr/local/hadoop/share/hadoop/mapreduce/hadoop-mapreduceexamples-2.2.0.jar wordcount /core-site.xml /out


命令输出：

[root@hadoop101 hadoop]# hadoop jar /usr/local/hadoop/share/hadoop/mapreduce/hadoopmapreduce-examples-2.2.0.jar wordcount /core-site.xml /out

14/02/12 11:43:55 INFO client.RMProxy: Connecting to ResourceManager at hadoop101/192.168.80.101:8032

14/02/12 11:43:59 INFO input.FileInputFormat: Total input paths to process : 1

14/02/12 11:43:59 INFO mapreduce.JobSubmitter: number of splits:1

14/02/12 11:43:59 INFO Configuration.deprecation: user.name is deprecated. Instead, use mapreduce.job.user.name

14/02/12 11:43:59 INFO Configuration.deprecation: mapred.jar is deprecated. Instead, use mapreduce.job.jar

14/02/12 11:43:59 INFO Configuration.deprecation: mapred.output.value.class is deprecated. Instead, use mapreduce.job.output.value.class

14/02/12 11:43:59 INFO Configuration.deprecation: mapreduce.combine.class is deprecated. Instead, use mapreduce.job.combine.class

14/02/12 11:43:59 INFO Configuration.deprecation: mapreduce.map.class is deprecated. Instead, use mapreduce.job.map.class

14/02/12 11:43:59 INFO Configuration.deprecation: mapred.job.name is deprecated. Instead, use mapreduce.job.name

- 14/02/12 11:43:59 INFO Configuration.deprecation: mapreduce.reduce.class is deprecated. Instead, use mapreduce.job.reduce.class


- 14/02/12 11:43:59 INFO Configuration.deprecation: mapred.input.dir is deprecated. Instead, use mapreduce.input.fileinputformat.inputdir


- 14/02/12 11:43:59 INFO Configuration.deprecation: mapred.output.dir is deprecated. Instead, use mapreduce.output.fileoutputformat.outputdir


- 14/02/12 11:43:59 INFO Configuration.deprecation: mapred.map.tasks is deprecated. Instead, use mapreduce.job.maps


- 14/02/12 11:43:59 INFO Configuration.deprecation: mapred.output.key.class is deprecated. Instead, use mapreduce.job.output.key.class


- 14/02/12 11:43:59 INFO Configuration.deprecation: mapred.working.dir is deprecated. Instead, use mapreduce.job.working.dir


- 14/02/12 11:44:01 INFO mapreduce.JobSubmitter: Submitting tokens for job: job_1392169506119_0002


- 14/02/12 11:44:04 INFO impl.YarnClientImpl: Submitted application application_1392169506119_0002 to ResourceManager at hadoop101/192.168.80.101:8032


- 14/02/12 11:44:05 INFO mapreduce.Job: The url to track the job:


http://hadoop101:8088/proxy/applic ation_1392169506119_0002/

- 14/02/12 11:44:05 INFO mapreduce.Job: Running job: job_1392169506119_0002


- 14/02/12 11:44:41 INFO mapreduce.Job: Job job_1392169506119_0002 running in uber mode : false


- 14/02/12 11:44:41 INFO mapreduce.Job: map 0% reduce 0%

- 14/02/12 11:45:37 INFO mapreduce.Job: map 100% reduce 0%

- 14/02/12 11:46:54 INFO mapreduce.Job: map 100% reduce 100%

- 14/02/12 11:47:01 INFO mapreduce.Job: Job job_1392169506119_0002 completed successfully


14/02/12 11:47:02 INFO mapreduce.Job: Counters: 43

File System Counters

FILE: Number of bytes read=472

FILE: Number of bytes written=164983

FILE: Number of read operations=0

FILE: Number of large read operations=0

FILE: Number of write operations=0

HDFS: Number of bytes read=540

HDFS: Number of bytes written=402

HDFS: Number of read operations=6

HDFS: Number of large read operations=0

HDFS: Number of write operations=2

Job Counters

Launched map tasks=1

Launched reduce tasks=1

Data-local map tasks=1

Total time spent by all maps in occupied slots (ms)=63094

Total time spent by all reduces in occupied slots (ms)=57228

Map-Reduce Framework

Map input records=17

Map output records=20

Map output bytes=496

Map output materialized bytes=472

Input split bytes=94

Combine input records=20

Combine output records=16

Reduce input groups=16

Reduce shuffle bytes=472

Reduce input records=16

Reduce output records=16

Spilled Records=32

Shuffled Maps =1

Failed Shuffles=0

Merged Map outputs=1

GC time elapsed (ms)=632

CPU time spent (ms)=3010

Physical memory (bytes) snapshot=255528960

Virtual memory (bytes) snapshot=1678471168

Total committed heap usage (bytes)=126660608

Shuffle Errors

BAD_ID=0

CONNECTION=0

IO_ERROR=0

WRONG_LENGTH=0

WRONG_MAP=0

WRONG_REDUCE=0

File Input Format Counters

Bytes Read=446

File Output Format Counters

Bytes Written=402

- [root@hadoop101 hadoop]#


验证：

- [root@hadoop101 hadoop]# hadoop fs -ls /out


- Found 2 items


- -rw-r--r-- 2 root supergroup 0 2014-02-12 11:46 /out/_SUCCESS

- -rw-r--r-- 2 root supergroup 402 2014-02-12 11:46 /out/part-r-00000


- [root@hadoop101 hadoop]# hadoop fs -text /out/part-r-00000


</configuration> 1

</property> 3

<?xml 1

<?xml-stylesheet 1

<configuration> 1

<name>fs.defaultFS</name> 1

<name>ha.zookeeper.quorum</name> 1

<name>hadoop.tmp.dir</name> 1

<property> 3

<value>/usr/local/hadoop/tmp</value> 1

<value>hadoop101:2181,hadoop102:2181,hadoop103:2181</value> 1

<value>hdfs://cluster1</value> 1

encoding="UTF-8"?> 1

href="configuration.xsl"?> 1

type="text/xsl" 1

version="1.0" 1

- [root@hadoop101 hadoop]#


### 验证HA的故障⾃动转移是否好⽤

观察cluster1的两个NameNode的状态，hadoop101的状态是standby，hadoop102的状态是active，如下图。

![image 5](assets/imageFile5.png)

![image 6](assets/imageFile6.png)

下⾯我们杀死hadoop102的NameNode进程，观察hadoop101的状态是否会⾃动切换成active。

- 在hadoop102执⾏以下命令


- [root@hadoop102 hadoop]# jps


13389 DFSZKFailoverController

12355 JournalNode

13056 DataNode

15660 Jps

14496 NodeManager

12573 NameNode

12081 QuorumPeerMain

- [root@hadoop102 hadoop]# kill -9 12573


- [root@hadoop102 hadoop]# jps


13389 DFSZKFailoverController

12355 JournalNode

13056 DataNode

14496 NodeManager

15671 Jps

12081 QuorumPeerMain

- [root@hadoop102 hadoop]#


再观察⻚⾯，发现如下图所示

![image 7](assets/imageFile7.png)

![image 8](assets/imageFile8.png)

证明HDFS的⾼可靠是可⽤的。

七、关闭服务 在master1节点上执⾏：

/home/hadop/hadop/sbin/stop-yarn.sh

在slave1、slave2、slave3节点上执⾏： /home/hadop/hadop/sbin/hadop-daemons.sh stop datanode

在master1，masterha1，master2，masterha2上执⾏： /home/hadop/hadop/sbin/hadop-daemon.sh stop namenode

在masterha1，master2，masterha2上执⾏： /home/hadop/hadop/sbin/hadop-daemon.sh stop journalnode

在master1，masterha1，master2，masterha2上执⾏： /home/hadop/hadop/sbin/hadop-daemon.sh stop zkfc

在masterha1，master2，masterha2上执⾏： zkServer.sh stop

⼋、QA

为什么不启动SecondaryNameNode？？

启动log：/home/hadop/hadop/sbin/hadop-daemon.sh start secondarynamenode 报错：2014-08-281 35 1,361 INFO org.apache.hadop.hdfs.server.namenode.SecondaryNameNode: registered UNIX signal handlers for [TERM, HUP, INT] 2014-08-281 35 1,807 FATAL org.apache.hadop.hdfs.server.namenode.SecondaryNameNode: Failed to start secondary namenode java.io.IOException: Canot use SecondaryNameNode in an HA cluster. The Standby Namenode wil perform checkpointing.

at org.apache.hadop.hdfs.server.namenode.SecondaryNameNode.<init> (SecondaryNameNode.java:194)

at org.apache.hadop.hdfs.server.namenode.SecondaryNameNode.main(SecondaryNameNode.java: 652) 2014-08-281 35 1,817 INFO org.apache.hadop.util.ExitUtil: Exiting with status 1 2014-08-281 35 1,819 INFO org.apache.hadop.hdfs.server.namenode.SecondaryNameNode: SHUTDOWN_MSG: / * SHUTDOWN_MSG: Shuting down SecondaryNameNode at master1/192.168.56.151

错误提示说：集群中的namenode standby从事了secondarynamenode的⼯作，不需要启动，但是如 果你想单独配置呢？

hadop2.X如何将namenode与SecondaryNameNode分开配置

- 1.如何将namenode与SecondaryNameNode分开？

- 2.SecondaryNameNode单独配置，需要修改那些配置⽂件？

- 3.masters⽂件的作⽤是什么？


![image 9](assets/imageFile9.png)

我们这⾥假设你已经安装配置了hadoop2.2，⾄于如何配置可以参考，

hadoop2.2完全分布式最新⾼可 靠安装⽂档

。 在这个基础上，我们对配置⽂件做⼀些修改： 1.增加masters⽂件

- 1. sudo vi masters


复制代码

![image 10](assets/imageFile10.png)

这⾥⾯放什么内容还是⽐较关键的，这⾥我们指定slave1节点上运⾏SecondaryNameNode。

注意：如果你想单独配置⼀台机器，那么在这个⽂件⾥⾯，填写这个节点的ip地址或则是 hostname，如果是多台，则在 s⾥⾯写上多个，⼀⾏⼀个，我们这⾥指定⼀个

master

1.

slave1

复制代码

![image 11](assets/imageFile11.png)

- 2.修改hdfs-site.xml 在下⾯⽂件中增加如下内容：（记得下⾯亦可写成ip地址，这⾥为了理解⽅便，写的是hostname）


<property> <name>dfs.http.address</name> <value>master:50070</value> <description> The address and the base port where the dfs namenode web ui will listen on. If the port is 0 then the will start on a free port. </description> </property> <property> <name>dfs.namenode.secondary.http-address</name> <value>slave1:50090</value> </property>

server

![image 12](assets/imageFile12.png)

#### 3.修改core-site.xml⽂件

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


<property> <name>fs.checkpoint.period</name> <value>3600</value> <description>The number of seconds between two periodic checkpoints. </description>

</property> <property>

<name>fs.checkpoint.size</name> <value>67108864</value>

</property>

复制代码

![image 13](assets/imageFile13.png)

上⾯修改完毕，相应的节点也做同样的修改

![image 14](assets/imageFile14.png)

下⾯我们开始启动节点：

1.

start-dfs.sh

复制代码

输出如下内容：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


Starting namenodes on [master] master: starting namenode, logging to /usr/hadoop/logs/hadoop-aboutyun-namenode-master.out slave2: starting datanode, logging to /usr/hadoop/logs/hadoop-aboutyun-datanode-slave2.out slave1: starting datanode, logging to /usr/hadoop/logs/hadoop-aboutyun-datanode-slave1.out Starting secondary namenodes [slave1] slave1: starting secondarynamenode, logging to /usr/hadoop/logs/hadoop-aboutyunsecondarynamenode-slave1.out

复制代码

![image 15](assets/imageFile15.png)

然后查看节点：

- （1）master节点：

复制代码

- （2）slave1节点


- 1.
- 2.
- 3.


aboutyun@master:/usr/hadoop/etc/hadoop$ jps 5994 NameNode 6201 Jps

![image 16](assets/imageFile16.png)

- 1.
- 2.


aboutyun@slave1:/usr/hadoop/etc/hadoop$ jps 5199 SecondaryNameNode

- 3.
- 4.


5015 DataNode 5291 Jps

复制代码

![image 17](assets/imageFile17.png)

- （3）slave2节点


- 1.
- 2.
- 3.


aboutyun@slave2:/usr/hadoop/etc/hadoop$ jps 3628 DataNode 3696 Jps

复制代码

![image 18](assets/imageFile18.png)

停⽌节点：

- 1.
- 2.
- 3.
- 4.
- 5.


master: stopping namenode

- slave1: stopping datanode
- slave2: stopping datanode Stopping secondary namenodes [slave1] slave1: stopping secondarynamenode


复制代码

![image 19](assets/imageFile19.png)
