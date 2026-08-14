---
title: hadoop2.2.0 HDFS HA  Federation.note（原文插图 annex）
slug: annex-hadoop2.2.0-HDFS-HA-Federation
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hadoop/hadoop2.2.0 HDFS HA  Federation.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

在虚拟机上安装Hadop 2环境，版本使⽤Hadop2.2.0。 Hadop2 HDFS 的具有HA和Federation 两种形式，配置上略有不同。以下把过程记录下来，留待以后查阅，也希望给⼈以便利。

准备⼯作

下载

htp:/mirors.cnic.cn/apache/hadop/comon/stable/hadop-2.2.0.tar.gz

操作系统

ubuntu12.04 虚拟机

Vmware 安装操作系统， 此步骤略 本次测试安装四台虚拟机

<table>
  <tr>
    <th>hostname</th>
    <th>IP</th>
    <th> </th>
  </tr>
  <tr>
    <td>1</td>
    <td>hw-010</td>
    <td>192.168.1.10</td>
  </tr>
  <tr>
    <td>2</td>
    <td>hw-0101</td>
    <td>192.168.1.101</td>
  </tr>
  <tr>
    <td>3</td>
    <td>hw-0102</td>
    <td>192.168.1.102</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


4 hw-0150 192.168.1.150

安装JAVA，此步骤略

创建⽤户

#userad hadop #paswd hadop 使 ⽤ sudo操 作 。 编 辑 /etc/sudoers编 辑 ⽂ 件 ， 在 rot AL=(AL)AL⾏ 下 添 加 hadop AL=(AL)AL

配置 sh免密码

$sh-keygen -t rsa $ cat ~/.sh/id_rsa.pub >~/.sh/authorized_keys

sh localhost 测 试 通过 后 ， 把 authorized_keys拷 ⻉ 到其 他 节 点 的 ～ /.sh⽬ 录 即可

准备⼯作完毕

配置HA 先看⼀下简单的架构图

![image 1](assets/imageFile1.png)

JAVA_HOME

配置$HADOP_HOME/etc/hadop/hadop-env.sh中JAVA_HOME

.

export JAVA_HOME =[java_local_dir]

.

配置core-site.xml, slaves, hdfs-site.xml

slaves

- 1 hw-0101

- 2 hw-0102

- 3 hw-0150


core-site.xml

- 1 <configuration>

- 2 <property>

- 3 <name>fs.defaultFS</name>

- 4 <value>hdfs://hadoop-cluster1</value>

- 5 </property>

- 6 </configuration>


hdfs-site.xml

- 1 <configuration>

- 2 <property>

- 3 <name>dfs.nameservices</name>

- 4 <value>hadoop-cluster1</value>

- 5 </property>

- 6

- 7 <property>

- 8 <name>dfs.ha.namenodes.hadoop-cluster1</name>

- 9 <value>nn1,nn2</value>

- 10 </property>

- 11

- 12

- 13 <property>

- 14 <name>dfs.namenode.rpc-address.hadoop-cluster1.nn1</name>

- 15 <value>hw-0100:9000</value>

- 16 </property>

- 17 <property>

- 18 <name>dfs.namenode.rpc-address.hadoop-cluster1.nn2</name>

- 19 <value>hw-0101:9000</value>

- 20 </property>

- 21

- 22

- 23 <property>

- 24 <name>dfs.namenode.http-address.hadoop-cluster1.nn1</name>

- 25 <value>hw-0100:50070</value>

- 26 </property>

- 27

- 28 <property>

- 29 <name>dfs.namenode.http-address.hadoop-cluster1.nn2</name>

- 30 <value>hw-0101:50070</value>

- 31 </property>

- 32 <property>

- 33 <name>dfs.client.failover.proxy.provider.hadoop-cluster1</name>

<value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider </value>

- 34

- 35 </property>

- 36

- 37

- 38 <property>


- 39 <name>dfs.namenode.name.dir</name>

- 40 <value>file:///home/hadoop/hadoop-hdfs3/hdfs/name</value>

- 41 </property>

- 42

- 43 <property>

- 44 <name>dfs.namenode.shared.edits.dir.hadoop-cluster1</name>

<value>qjournal://hw-0101:8485;hw-0102:8485;hw-0150:8485/hadoopcluster1</value>

- 45

- 46 </property>

- 47

- 48

- 49

- 50

- 51 <property>

- 52 <name>dfs.datanode.data.dir</name>

- 53 <value>file:///home/hadoop/hadoop-hdfs3/hdfs/data</value>

- 54 </property>

- 55

- 56 <property>

- 57 <name>dfs.ha.automatic-failover.enabled</name>

- 58 <value>false</value>

- 59 </property>

- 60

- 61 <property>

- 62 <name>dfs.journalnode.edits.dir</name>

- 63 <value>/home/hadoop/hadoop-hdfs3/hdfs/journal</value>

- 64 </property>

- 65

- 66

- 67 </configuration>


把以上三个⽂件分别拷⻉到所有节点的etc/hadop/⾥。

## 启动namenode, datanode(本⽂hadop的命令均在$HADOP_HOME⽬录下运⾏)

配置HA，需要保证ActiveN与StandByN有相同的NameSpace ID，在format⼀台机器之后，让另外 ⼀台 N同步⽬录下的数据。 在hw-010节点上执⾏：

- 1 如果使⽤HQJM,需要先启动jouranlnode

- 2 $sbin/hadoop-daemons.sh start journalnode

- 3

- 4 $bin/hdfs namenode -format

- 5

- 6 $sbin/hadoop-daemon.sh start namenode


- 在hw-0101节点上执⾏：


- 1 $bin/hdfs namenode -bootstrapStandby

- 2

- 3 $sbin/hadoop-daemon.sh start namenode


此时hw-010,hw-0101均处于standby状态 切换hw-010节点为Active状态 在hw-010节点上执⾏：

1 $bin/hdfs haadmin -transitionToActive nn1

启动所有datanode节点

1 $sbin/hadoop-daemons.sh start datanode

## 验证 htp:/192.168.1.10 5070

访问 查看监控⻚⾯ 访问HDFS，命令如下：

创 建 ⽬ 录

1 $bin/hadoop fs -mkdir /tmp

查 看 ⽬ 录

1 $bin/hadoop fs -ls /tmp

配置Federation

![image 2](assets/imageFile2.png)

## 配置core-site.xml, slaves, hdfs-site.xml

slaves

- 1 hw-0101

- 2 hw-0102

- 3 hw-0150


core-site.xml

- 2

- 3 <property>

- 4 <name>fs.defaultFS</name>

- 5 <value>viewfs:///</value>

- 6 </property>

- 7

- 8

- 9 <property>

- 10 <name>fs.viewfs.mounttable.default.link./tmp</name>

- 11 <value>hdfs://hw-0100:9000/tmp</value>

- 12 </property>

- 13

- 14 <property>

- 15 <name>fs.viewfs.mounttable.default.link./tmp1</name>

- 16 <value>hdfs://hw-0102:9000/tmp2</value>

- 17 </property>

- 18

- 19

- 20 </configuration>


hdfs-site.xml

- 2 <property>

- 3 <name>dfs.federation.nameservices</name>

- 4 <value>hadoop-cluster1,hadoop-cluster2</value>

- 5 </property>

- 6 <!--cluster1-->

- 7 <property>

- 8 <name>dfs.namenode.rpc-address.hadoop-cluster1</name>

- 9 <value>hw-0100:9000</value>

- 10 </property>

- 11

- 12 <property>

- 13 <name>dfs.namenode.http-address.hadoop-cluster1</name>

- 14 <value>hw-0100:50070</value>

- 15 </property>

- 16

- 17 <!--cluster1-->

- 18 <!--cluster2-->

- 19 <property>

- 20 <name>dfs.namenode.rpc-address.hadoop-cluster2</name>

- 21 <value>hw-0102:9000</value>

- 22 </property>

- 23

- 24 <property>

- 25 <name>dfs.namenode.http-address.hadoop-cluster2</name>

- 26 <value>hw-0102:50070</value>

- 27 </property>

- 28

- 29 <!--cluster2-->

- 30

- 31

- 32 <property>

- 33 <name>dfs.namenode.name.dir</name>

- 34 <value>file:///home/hadoop/hadoop-hdfs3/hdfs/name</value>

- 35 </property>

- 36

- 37

- 38 <property>

- 39 <name>dfs.datanode.data.dir</name>


- 40 <value>file:///home/hadoop/hadoop-hdfs3/hdfs/data</value>

- 41 </property>

- 42

- 43

- 44 </configuration>


把以上三个⽂件分别拷⻉到所有节点的etc/hadop/⾥。

## 启动namenode, datanode(本⽂hadop的命令均在$HADOP_HOME⽬录下运⾏)

配置Federation，需要在启动多个NameNode上format时，指定clusterid，从⽽保证2个NameService 可以共享所有的DataNodes，否则两个NameService在format之后，⽣成的clusterid不⼀致， DataNode会随机注册到不同的NameNode上。 在hw-010节点上执⾏：

- 1 $bin/hdfs namenode -format –clusterId hadoop-cluster

- 2

- 3 $sbin/hadoop-daemon.sh start namenode


- 在hw-0102节点上执⾏：


- 1 $bin/hdfs namenode -format –clusterId hadoop-cluster

- 2

- 3 $sbin/hadoop-daemon.sh start namenode


启动所有datanode节点

1 $sbin/hadoop-daemons.sh start datanode

## 验证 htp:/192.168.1.10 5070

访问 查看监控⻚⾯ 访问HDFS，命令如下：

查 看 ⽬ 录

- 1 $bin/hadoop fs -ls /

- 2

- 3 可以查看到 /tmp /tmp1的两个⽬录。


HA&Federation 先看看简单架构图

![image 3](assets/imageFile3.png)

## 配置core-site.xml, slaves, hdfs-site.xml

slaves

- 1 hw-0101

- 2 hw-0102

- 3 hw-0150


core-site.xml

2 3 <property> 4 <name>fs.defaultFS</name> 5 <value>viewfs:///</value> 6 </property> 7

8 9 <property>

10 <name>fs.viewfs.mounttable.default.link./tmp</name> 11 <value>hdfs://hadoop-cluster1/tmp</value> 12 </property> 13 14 <property> 15 <name>fs.viewfs.mounttable.default.link./tmp1</name> 16 <value>hdfs://hadoop-cluster2/tmp2</value> 17 </property> 18

19 20 </configuration>

hdfs-site.xml

- 2 <property>

- 3 <name>dfs.nameservices</name>

- 4 <value>hadoop-cluster1,hadoop-cluster2</value>

- 5 </property>

- 6

- 7 <property>

- 8 <name>dfs.ha.namenodes.hadoop-cluster1</name>

- 9 <value>nn1,nn2</value>

- 10 </property>

- 11

- 12 <property>

- 13 <name>dfs.namenode.rpc-address.hadoop-cluster1.nn1</name>

- 14 <value>hw-0100:9000</value>

- 15 </property>

- 16 <property>

- 17 <name>dfs.namenode.rpc-address.hadoop-cluster1.nn2</name>

- 18 <value>hw-0101:9000</value>

- 19 </property>

- 20

- 21 <property>

- 22 <name>dfs.namenode.http-address.hadoop-cluster1.nn1</name>

- 23 <value>hw-0100:50070</value>

- 24 </property>

- 25 <property>

- 26 <name>dfs.namenode.http-address.hadoop-cluster1.nn2</name>

- 27 <value>hw-0101:50070</value>

- 28 </property>

- 29

- 30 <property>

- 31 <name>dfs.client.failover.proxy.provider.hadoop-cluster1</name>

<value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider </value>

- 32

- 33 </property>

- 34

- 35 <!--cluster2-->

- 36 <property>

- 37 <name>dfs.ha.namenodes.hadoop-cluster2</name>

- 38 <value>nn3,nn4</value>


- 39 </property>

- 40

- 41 <property>

- 42 <name>dfs.namenode.rpc-address.hadoop-cluster2.nn3</name>

- 43 <value>hw-0102:9000</value>

- 44 </property>

- 45 <property>

- 46 <name>dfs.namenode.rpc-address.hadoop-cluster2.nn4</name>

- 47 <value>hw-0150:9000</value>

- 48 </property>

- 49

- 50 <property>

- 51 <name>dfs.namenode.http-address.hadoop-cluster2.nn3</name>

- 52 <value>hw-0102:50070</value>

- 53 </property>

- 54 <property>

- 55 <name>dfs.namenode.http-address.hadoop-cluster2.nn4</name>

- 56 <value>hw-0150:50070</value>

- 57 </property>

- 58

- 59 <property>

- 60 <name>dfs.client.failover.proxy.provider.hadoop-cluster2</name>

<value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider </value>

- 61

- 62 </property>

- 63

- 64 <!--cluster2-->

- 65

- 66

- 67 <property>

- 68 <name>dfs.namenode.name.dir</name>

- 69 <value>file:///home/hadoop/hadoop-hdfs3/hdfs/name</value>

- 70 </property>

- 71

- 72 <property>

- 73 <name>dfs.namenode.shared.edits.dir.hadoop-cluster1.nn1</name>

<value>qjournal://hw-0101:8485;hw-0102:8485;hw-0150:8485/hadoopcluster1</value>

- 74

- 75 </property>

- 76


- 77 <property>

- 78 <name>dfs.namenode.shared.edits.dir.hadoop-cluster1.nn2</name> <value>qjournal://hw-0101:8485;hw-0102:8485;hw-0150:8485/hadoop-

- cluster1</value>

- 79

- 80 </property> 81 82 <property> 83 <name>dfs.namenode.shared.edits.dir.hadoop-cluster2.nn3</name>


<value>qjournal://hw-0101:8485;hw-0102:8485;hw-0150:8485/hadoop-

- cluster2</value>


- 84

- 85 </property>

- 86

- 87 <property>

- 88 <name>dfs.namenode.shared.edits.dir.hadoop-cluster2.nn4</name>

<value>qjournal://hw-0101:8485;hw-0102:8485;hw-0150:8485/hadoopcluster2</value>

- 89

- 90 </property>

- 91

- 92

- 93

- 94 <property>

- 95 <name>dfs.datanode.data.dir</name>

- 96 <value>file:///home/hadoop/hadoop-hdfs3/hdfs/data</value>

- 97 </property>

- 98

- 99 <property>

- 100 <name>dfs.ha.automatic-failover.enabled</name>

- 101 <value>false</value>

- 102 </property>

- 103

- 104 <property>

- 105 <name>dfs.journalnode.edits.dir</name>

- 106 <value>/home/hadoop/hadoop-hdfs3/hdfs/journal</value>

- 107 </property>

- 108

- 109 </configuration>


把以上三个⽂件分别拷⻉到所有节点的etc/hadop/⾥。

## 启动namenode, datanode(本⽂hadop的命令均在$HADOP_HOME⽬录下运⾏)

在hw-010节点上执⾏：

- 1 如果使⽤HQJM,需要先启动jouranlnode

- 2 $sbin/hadoop-daemons.sh start journalnode


- 1 $bin/hdfs namenode -format –clusterId hadoop-cluster-new

- 2

- 3 $sbin/hadoop-daemon.sh start namenode


- 在hw-0101节点上执⾏：

此时hw-010,hw-0101均处于standby状态 切换hw-010节点为Active状态 在hw-010节点上执⾏：

- 在hw-0102节点上执⾏：


- 1 $bin/hdfs namenode -bootstrapStandby

- 2

- 3 $sbin/hadoop-daemon.sh start namenode


1 $bin/hdfs haadmin -ns hadoop-cluster1 -transitionToActive nn1

- 1 $bin/hdfs namenode -format –clusterId hadoop-cluster-new

- 2

- 3 $sbin/hadoop-daemon.sh start namenode


在hw-0150节点上执⾏：

- 1 $bin/hdfs namenode -bootstrapStandby

- 2

- 3 $sbin/hadoop-daemon.sh start namenode


此时hw-0102,hw-0150均处于standby状态 切换hw-0102节点为Active状态 在hw-010节点上执⾏：

1 $bin/hdfs haadmin -ns hadoop-cluster2 -transitionToActive nn3

在hw-010节点上，启动所有datanode节点

1 $sbin/hadoop-daemons.sh start datanode

## 验证 htp:/192.168.1.10 5070

访问 查看监控⻚⾯ 访问HDFS，命令如下：

# 查 看 ⽬ 录

- 1 $bin/hadoop fs -ls /

- 2 显示在不同namenode中的⽬录 /tmp /tmp1


参考：

htp:/hadop.apache.org/docs/curent/hadop-project-dist/hadop-hdfs/Federation.html htp:/ w.binospace.com/index.php/hdfs-ha-quorum-journal-manager/

知识源于⽹络 转载请注明出处

htp:/ w.cnblogs.com/nb591/p/353562.html
