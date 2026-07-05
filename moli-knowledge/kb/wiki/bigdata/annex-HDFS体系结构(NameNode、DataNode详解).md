---
title: HDFS体系结构(NameNode、DataNode详解).note（原文插图 annex）
slug: annex-HDFS体系结构(NameNode、DataNode详解)
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/Hadoop/HDFS/HDFS体系结构(NameNode、DataNode详解).note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.cnblogs.com/jackchen-Net/p/6506321.html

要点导航

NameNode、DataNode详解

hadoop项⽬地址:http://hadoop.apache.org/

回到导航

### NameNode、DataNode详解

(⼀)分布式⽂件系统概述

数据量越来越多，在⼀个操作系统管辖的范围存不下了，那么就分配到更多的操作系统管理的磁盘中，但是不⽅便管理和 维护，因此迫切需要⼀种系统来管理多台机器上的⽂件，这就是分布式⽂件管理系统 。

是⼀种允许⽂件通过⽹络在多台主机上分享的⽂件系统，可让多机器上的多⽤户分享⽂件和存储空间。

通透性。让实际上是通过⽹络来访问⽂件的动作，由程序与⽤户看来，就像是访问本地的磁盘⼀般。 容错。即使系统中有 某些节点脱机，整体来说系统仍然可以持续运作⽽不会有数据损失。

分布式⽂件管理系统很多，hdfs只是其中⼀种，不合适⼩⽂件。

(⼆)HDFS的简单使⽤

![image 1](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile1.png>)

![image 2](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile2.png>)

#### 通过hdfs命令查看所有的可使⽤参数

<table>
  <tr>
    <th>![image 3](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile3.png>)</th>
  </tr>
</table>


[root@neusoft-master bin]# hdfs Usage: hdfs [--config confdir 可选项，指定配置⽂件⽬录，默认在/etc/hadoop⽬录下] COMMAND

where COMMAND is one of: dfs run a filesystem command on the file systems supported in Hadoop. #重点 namenode -format format the DFS filesystem #最好只执⾏⼀次 secondarynamenode run the DFS secondary namenode #在hadoop2中不⽤ namenode run the DFS namenode journalnode run the DFS journalnode zkfc run the ZK Failover Controller daemon datanode run a DFS datanode dfsadmin run a DFS admin client haadmin run a DFS HA admin client fsck run a DFS filesystem checking utility balancer run a cluster balancing utility jmxget get JMX exported values from NameNode or DataNode. mover run a utility to move block replicas across

storage types oiv apply the offline fsimage viewer to an fsimage oiv_legacy apply the offline fsimage viewer to an legacy fsimage oev apply the offline edits viewer to an edits file fetchdt fetch a delegation token from the NameNode getconf get config values from configuration groups get the groups which users belong to snapshotDiff diff two snapshots of a directory or diff the

current directory contents with a snapshot lsSnapshottableDir list all snapshottable dirs owned by the current user Use -help to see options

portmap run a portmap service nfs3 run an NFS version 3 gateway cacheadmin configure the HDFS cache crypto configure HDFS encryption zones storagepolicies list/get/set block storage policies version print the version

Most commands print help when invoked w/o parameters.

<table>
  <tr>
    <th>![image 4](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile4.png>)</th>
  </tr>
</table>


#### 如果对某⼀个命令不知道怎么使⽤，可以直接输⼊命令即可，如下

![image 5](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile5.png>)

<table>
  <tr>
    <th>![image 6](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile6.png>)</th>
  </tr>
</table>


[root@neusoft-master bin]# hdfs dfs #⼀定在bin⽬录下执⾏hdfs命令 Usage: hadoop fs [generic options]

[-appendToFile <localsrc> ... <dst>] [-cat [-ignoreCrc] <src> ...] [-checksum <src> ...] [-chgrp [-R] GROUP PATH...] [-chmod [-R] <MODE[,MODE]... | OCTALMODE> PATH...] [-chown [-R] [OWNER][:[GROUP]] PATH...] [-copyFromLocal [-f] [-p] [-l] <localsrc> ... <dst>] [-copyToLocal [-p] [-ignoreCrc] [-crc] <src> ... <localdst>] [-count [-q] [-h] [-v] <path> ...] [-cp [-f] [-p | -p[topax]] <src> ... <dst>] [-createSnapshot <snapshotDir> [<snapshotName>]] [-deleteSnapshot <snapshotDir> <snapshotName>] [-df [-h] [<path> ...]] [-du [-s] [-h] <path> ...] [-expunge] [-find <path> ... <expression> ...] [-get [-p] [-ignoreCrc] [-crc] <src> ... <localdst>] [-getfacl [-R] <path>] [-getfattr [-R] {-n name | -d} [-e en] <path>] [-getmerge [-nl] <src> <localdst>] [-help [cmd ...]] [-ls [-d] [-h] [-R] [<path> ...]] [-mkdir [-p] <path> ...] [-moveFromLocal <localsrc> ... <dst>] [-moveToLocal <src> <localdst>] [-mv <src> ... <dst>] [-put [-f] [-p] [-l] <localsrc> ... <dst>] [-renameSnapshot <snapshotDir> <oldName> <newName>] [-rm [-f] [-r|-R] [-skipTrash] <src> ...] [-rmdir [--ignore-fail-on-non-empty] <dir> ...] [-setfacl [-R] [{-b|-k} {-m|-x <acl_spec>} <path>]|[--set <acl_spec> <path>]] [-setfattr {-n name [-v value] | -x name} <path>] [-setrep [-R] [-w] <rep> <path> ...] [-stat [format] <path> ...] [-tail [-f] <file>] [-test -[defsz] <path>] [-text [-ignoreCrc] <src> ...] [-touchz <path> ...] [-usage [cmd ...]]

Generic options supported are

- -conf <configuration file> specify an application configuration file

- -D <property=value> use value for given property

- -fs <local|namenode:port> specify a namenode

- -jt <local|resourcemanager:port> specify a ResourceManager

- -files <comma separated list of files> specify comma separated files to be copied to the map reduce cluster


- -libjars <comma separated list of jars> specify comma separated jar files to include in the classpath.

- -archives <comma separated list of archives> specify comma separated archives to be unarchived on the compute machines.


The general command line syntax is bin/hadoop command [genericOptions] [commandOptions]

<table>
  <tr>
    <th>![image 7](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile7.png>)</th>
  </tr>
</table>


在hadoop1中使⽤hadoop dfs ...，上述的命令提示还是⽼版本的命令。在hadoop2中使⽤hdfs dfs ... 命令，如下图 所示：

![image 8](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile8.png>)

提示：linux中的ls -l命令，需要明确了解显示的内容的含义

![image 9](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile9.png>)

d表示⽬录。-表示⽂件，l表示链接，之后9位的每三位是⼀组，第⼀组表示创建者，第⼆组表示创建者所在组，第三个表示 其他⼈。

## （三）HttpFS访问⽅式

- 1：httpfs是⼀个hadoop hdfs的⼀个http接⼝，通过WebHDFS REST API 可以对hdfs进⾏读写等 访问
- 2：与WebHDFS的区别是不需要客户端可以访问hadoop集群的每⼀个节点，通过httpfs可以访问放置在防⽕墙后⾯的 hadoop集群
- 3：httpfs是⼀个Web应⽤,部署在内嵌的tomcat中 操作⽅式如下：


- 1.编辑⽂件httpfs-env.sh，将端⼝为14000的打开即可


![image 10](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile10.png>)

![image 11](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile11.png>)

![image 12](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile12.png>)

#### 2.编辑⽂件core-site.xml，添加

<table>
  <tr>
    <th>![image 13](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile13.png>)</th>
  </tr>
</table>


<property> <name>hadoop.proxyuser.root.hosts</name> <value>*</value>

</property> <property>

<name>hadoop.proxyuser.root.groups</name> <value>*</value>

</property>

<table>
  <tr>
    <th>![image 14](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile14.png>)</th>
  </tr>
</table>


#### 3.重新启动namenode，执⾏sbin/httpfs.sh start

<table>
  <tr>
    <th>![image 15](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile15.png>)</th>
  </tr>
</table>


[root@neusoft-master sbin]# ./stop-all.sh #停⽌集群 [root@neusoft-master sbin]# ./start-all.sh #重新开启集群 [root@neusoft-master sbin]# jps 38143 NameNode 38444 SecondaryNameNode 38696 NodeManager 38248 DataNode 38835 Jps 38599 ResourceManager [root@neusoft-master sbin]# ./httpfs.sh start #开启httpfs

<table>
  <tr>
    <th>![image 16](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile16.png>)</th>
  </tr>
</table>


开启后为了确认httpfs命令已开启，重新执⾏该命令，可以查看到PID已经存在的信息。

![image 17](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile17.png>)

- 4.执⾏命令curl -i "http://neusoft-master:14000/webhdfs/v1/?user.name=root&op=GETHOMEDIRECTORY"


<table>
  <tr>
    <th>![image 18](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile18.png>)</th>
  </tr>
</table>


[root@neusoft-master sbin]# curl -i "http://neusoft-master:14000/webhdfs/v1/? user.name=root&op=GETHOMEDIRECTORY" 下⾯的红⾊信息为http所带的head信息 HTTP/1.1 200 OK Server: Apache-Coyote/1.1 Set-Cookie: hadoop.auth="u=root&p=root&t=simple-dt&e=1486432504558&s=o83ImIOyH8z6T2ZhI/YRH3secGk="; Path=/; Expires=Tue, 07-Feb-2017 01:55:04 GMT; HttpOnly Content-Type: application/json Transfer-Encoding: chunked Date: Mon, 06 Feb 2017 15:55:06 GMT GETHOMEDIRECTORY查看主⽂件⽬录的结果如下：

##### {"Path":"\/user\/root"} #结果信息

<table>
  <tr>
    <th>![image 19](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile19.png>)</th>
  </tr>
</table>


更多命令参考http://hadoop.apache.org/docs/r2.6.0/hadoop-project-dist/hadoop-hdfs/WebHDFS.html

![image 20](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile20.png>)

## （四）HDFS体系结构

![image 21](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile21.png>)

Client客户端+Namenode+DataNode

- 1.Namenode 是整个⽂件系统的管理节点。它维护着1.整个⽂件系统的⽂件⽬录树，2.⽂件/⽬录的元信息和每个⽂件对应的数据块列


表。3.接收⽤户的操作请求。 (⻅源码) ⽂件包括：（hdfs-site.xml的dfs.namenode.name.dir属性）

fsimage:元数据镜像⽂件。存储某⼀时段NameNode内存元数据信息。

edits:操作⽇志⽂件。

fstime:保存最近⼀次checkpoint的时间 以上这些⽂件是保存在linux的⽂件系统中。

总结： NameNode维护着2张表：

- 1.⽂件系统的⽬录结构，以及元数据信息
- 2.⽂件与数据块（block）列表的对应关系 元数据存放在fsimage中，在运⾏的时候加载到内存中的(读写⽐较快)。 操作⽇志写到edits中。（类似于LSM树中的log） （刚开始的写⽂件会写⼊到内存中和edits中，edits会记录⽂件系统的每⼀步操作，当达到⼀定的容量会将其内容写⼊ fsimage中） 实验：


- (a)通过maven下载源代码，查看hdfs-default.xml配置⽂件


<table>
  <tr>
    <th>![image 22](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile22.png>)</th>
  </tr>
</table>


<property> <name>dfs.namenode.name.dir</name> <value>file://${hadoop.tmp.dir}/dfs/name</value> <description>Determines where on the local filesystem the DFS name node

should store the name table(fsimage). If this is a comma-delimited list of directories then the name table is replicated in all of the directories, for redundancy. </description>

</property>

<table>
  <tr>
    <th>![image 23](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile23.png>)</th>
  </tr>
</table>


描述信息为：确定在本地⽂件系统上的DFS名称节点应存储名称表（fsimage）。 fsimage的内容会被存储到以逗号分隔的 列表的⽬录中，然后在所有的⽬录中复制名称表⽬录，⽤于冗余。

****在实际应⽤中只需要将上述的源代码复制到hdfs-site.xml中，将<value>中的值改为以逗号分隔的列表即可。（注 意：逗号后千万不可加空格在写⽂件）

- (b)通过源代码信息的查找，寻找dfs.namenode.name.dir的信息，⾸先应该找到hadoop.tmp.dir的配置信息，从⽽寻找到 core-site.xml


<table>
  <tr>
    <th>![image 24](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile24.png>)</th>
  </tr>
</table>


[root@neusoft-master sbin]# vi ../etc/hadoop/core-site.xml <!-- Put site-specific property overrides in this file. --> <configuration>

<property> <name>fs.default.name</name> <value>hdfs://neusoft-master:9000</value>

</property>

<property> <name>hadoop.tmp.dir</name> <value>/opt/hadoop-2.6.0-cdh5.6.0/tmp</value>

</property>

<table>
  <tr>
    <th>![image 25](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile25.png>)</th>
  </tr>
</table>


- (c)根据上述分析查找tmp⽬录以及其⼦⽬录的详细信息
- (d)VERSION信息的内容 [root@neusoft-master sbin]# more /opt/hadoop-2.6.0-cdh5.6.0/tmp/dfs/namesecondary/current/VERSION 显示内容：


![image 26](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile26.png>)

<table>
  <tr>
    <th>![image 27](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile27.png>)</th>
  </tr>
</table>


#Mon Feb 06 23:54:55 CST 2017 namespaceID=457699475 #命名空间，hdfs格式化会改变命名空间id，当⾸次格式化的时候datanode和namenode会产⽣⼀个 相同的namespaceID，然后读取数据就可以，如果你重新执⾏格式化的时候，namenode的namespaceID改变了，但是datanode的 namespaceID没有改变，两边就不⼀致了，如果重新启动或进⾏读写hadoop就会挂掉。 clusterID=CID-409e0084-39f0-4386-8184-dd555478a3d6 #hdfs集群 cTime=0 storageType=NAME_NODE blockpoolID=BP-625280320-192.168.191.130-1483628038952 #hdfs联邦中使⽤，就是⾥⾯的namenode是共享的 layoutVersion=-60

<table>
  <tr>
    <th>![image 28](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile28.png>)</th>
  </tr>
</table>


多次格式化namenode的问题原因解释？

答：hdfs格式化会改变命名空间id，当⾸次格式化的时候datanode和namenode会产⽣⼀个相同的namespaceID，然后 读取数据就可以，如果你重新执⾏格式化的时候，namenode的namespaceID改变了，但是datanode的namespaceID没 有改变，两边就不⼀致了，如果重新启动或进⾏读写hadoop就会挂掉。

解决⽅案：hdfs namenode -format -force 进⾏强制的格式化会同时格式化namenode和datanode

-format [-clusterid cid ] [-force] [-nonInteractive] （完整的命令为hdfs namenode [-format [-clusterid cid ] [-force] [-nonInteractive]]）。

查看NameNode内容

启动服务器bin/hdfs oiv -i 某个fsimage⽂件

查看内容bin/hdfs dfs -ls -R webhdfs://127.0.0.1:5978/

导出结果bin/hdfs oiv -p XML -i tmp/dfs/name/current/fsimage_0000000000000000055 -o fsimage.xml

查看edtis内容bin/hdfs oev -i tmp/dfs/name/current/edits_0000000000000000057-0000000000000000186

-o edits.xml

![image 29](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile29.png>)

![image 30](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile30.png>)

![image 31](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile31.png>)

在hadoop2中，namenode的50030端⼝换成8088，新的yarn平台默认是8088，也可以通过yarn-site.xml配置，如 下

<property> <name>yarn.resourcemanager.webapp.address</name> <value>neusoft-master:8088</value>

</property>

![image 32](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile32.png>)

# 2.Datanode

#### 提供真实⽂件数据的存储服务。

⽂件块（block）：最基本的存储单位。对于⽂件内容⽽⾔，⼀个⽂件的⻓度⼤⼩是size，那么从⽂件的０偏移开始，按照 固定的⼤⼩，顺序对⽂件进⾏划分并编号，划分好的每⼀个块称⼀个Block。

HDFS默认Block⼤⼩是128MB，以⼀个256MB⽂件，共有256/128=2个Block. 不同于普通⽂件系统的是，HDFS中， 如果⼀个⽂件⼩于⼀个数据块的⼤⼩，并不占⽤整个数据块存储空间。ruc

(这样设置可以减轻namenode压⼒，因为namonode维护者⽂件与数据块列表的对应⼤⼩)

Replication。多复本。默认是三个。（hdfs-site.xml的dfs.replication属性）

- （1）Hdfs块⼤⼩如何设定？ hdfs-default.xml


<table>
  <tr>
    <th>![image 33](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile33.png>)</th>
  </tr>
</table>


<property> <name>dfs.blocksize</name> #block块存储的配置信息 <value>134217728</value> #这⾥的块的容量最⼤是128M，请注意 <description>

The default block size for new files, in bytes. You can use the following suffix (case insensitive): k(kilo), m(mega), g(giga), t(tera), p(peta), e(exa) to specify the size (such as 128k, 512m,

1g, etc.),

Or provide complete size in bytes (such as 134217728 for 128 MB). </description>

</property>

<table>
  <tr>
    <th>![image 34](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile34.png>)</th>
  </tr>
</table>


描述信息翻译： 新⽂件的默认块⼤⼩（以字节为单位）。 您可以使⽤以下后缀（不区分⼤⼩写）： 指定⼤⼩（例如128k，512m，1g等）的k（千），m（兆），g（giga），t（tera），p（peta） 或提供完整的⼤⼩（以128 MB为单位的134217728）。

***如何修改默认⼤⼩的blocksize？答：只需要修改上述配置⽂件即可。但是这种⽅式是全局的修改。 64M=67108864

如果想针对⽂件修改，只需要使⽤命令修改即可 hadoop fs -Ddfs.blocksize=134217728 -put ./test.txt /test

- （2）修改数据块的测试： [root@neusoft-master filecontent]# hdfs dfs -Ddfs.blocksize=67108864 -put hellodemo /neusoft/hello2


![image 35](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile35.png>)

源数据信息：

![image 36](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile36.png>)

上传之后在hdfs的配置⽬录查看，其⼤⼩等于19字节，⽽⾮64M

![image 37](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile37.png>)

或者 下⾯通过浏览器查看：

http://192.168.191.130:50070/explorer.html#/neusoft/ #如果windows上配置了hosts，这⾥可以写主机名

![image 38](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile38.png>)

![image 39](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile39.png>)

注意区别：⼀个⽂件可以产⽣多个快，多个⽂件是不可能成为⼀个块信息的，处于减轻namenode的压⼒，最好的⽅式就是 ⼀个⽂件⼀个块

- （3）⽂件块存放路径查看与具体信息解释


- （a）查找datanode存放数据的位置，配置信息在hdfs-site.xml中
- （b）进⼊datanode存放信息的⽬录查看


![image 40](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile40.png>)

[root@neusoft-master subdir0]# cd /opt/hdfs/data/current/BP-625280320-192.168.191.1301483628038952/current/finalized/subdir0/subdir0

可以查看到元数据的信息以及数据信息

![image 41](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile41.png>)

Tips：可以在本地新建⼀个⽂件，上传到HDFS中，查看是否增加了块信息。

## 副本机制：默认为3

vi hdfs-site.xml,可以修改，配置⽂件对全局⽣效

<configuration>

<property> <name>dfs.replication</name> <value>3</value>

</property> </configuration>

如果想⼀部分⽂件副本为3，⼀部分⽂件副本位2，这个需求也同样在命令⾏执⾏操作即可

![image 42](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile42.png>)

[root@neusoft-master hadoop]# hdfs dfs -setrep 2 /neusoft/hello1

![image 43](<HDFS体系结构(NameNode、DataNode详解).note_images/imageFile43.png>)

总结：DataNode 使 ⽤ block形式 存 储 。在 hadoop2中 ， 默 认 的 ⼤ ⼩ 是 128MB。 使 ⽤ 副 本 形式 保 存 数据 的 安 全 ， 默 认 的 数 量 是 3个 。

END~
