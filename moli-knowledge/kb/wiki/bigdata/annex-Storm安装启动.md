---
title: Storm安装启动.note（原文插图 annex）
slug: annex-Storm安装启动
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/Storm/Storm安装启动.note.md
related: [flink-流批一体入门]
created: 2026-07-05
updated: 2026-07-05
---

- 1、集群部署的基本流程 集群部署的流程：下载安装包、解压安装包、修改配置⽂件、分发 安装包、启动集群

- 2、集群部署的基础环境准备 安装前的准备⼯作（zk集群已经部署完毕）

chkconfigiptablesof &setenforce0

groupadrealtime &useradrealtime&usermod-a-G realtimerealtime

mkdir/export mkdir/export/servers chmod75-R/export

surealtime

- 3、Storm集群部署


关闭防⽕墙

创建⽤户

创建⼯作⽬录并赋权

切换到realtime⽤户下

- 3.1、下载安装包

wget

- 3.2、解压安装包


## htp:/124.202.164.6/files/139 06794ECA/apa che.fayea.com/storm/apache-storm-0.9.5/apache-storm-0.9.5. tar.gz

tar-zxvfapache-storm-0.9.5.tar.gz-C/export/servers/

## cd/export/servers/ ln-sapache-storm-0.9.5storm

- 3.3、修改配置⽂件


## mv/export/servers/storm/conf/storm.yaml /export/servers/storm/conf/storm.yaml.bak vi/export/servers/storm/conf/storm.yaml 输⼊以下内容：

![image 1](assets/imageFile1.png)

<table>
  <tr>
    <th>storm.zokeper.servers:<br><br>" 1" “zk02”<br><br>- "zk03" #指定storm集群中的nimbus节点所在的服务器 nimbus.host:"storm01" #指定nimbus启动JVM最⼤可⽤内存⼤⼩ supervisor.childopts: "-Xmx1024m" #指定supervisor节点上，每个worker启动JVM最⼤可⽤内存⼤⼩ worker.chilopts: "-Xmx768m" #指定UI启动JVM最⼤可⽤内存⼤⼩,UI服务⼀般与nimbus在同⼀个节点上 ui.childopts: "-Xmx768m" #指定supervisor节点上，启动worker时对应的端⼝号，每个端⼝对应槽，每个槽位对应⼀个worker supervisor.slots.ports:<br><br>0<br><br>1<br><br>2<br><br><br>-6703<br></th>
  </tr>
</table>


- 3.4、分发安装包

scp-r/export/servers/apache-storm-0.9.5 storm02:/export/servers 然后分别在各机器上创建软连接 cd/export/servers/ ln-sapache-storm-0.9.5storm

- 3.5、启动集群


在nimbus.host所属的机器上启动 nimbus服务

cd/export/servers/storm/bin/ nohup./stormnimbus&

在nimbus.host所属的机器上启动ui服务

cd/export/servers/storm/bin/ nohup./stormui&

在其它个点击上启动supervisor服务

cd/export/servers/storm/bin/ nohup./stormsupervisor&

- 3.6、查看集群


访问nimbus.host:/8080，即可看到storm的ui界⾯。

![image 2](assets/imageFile2.png)

安装结束

# 4、Storm常⽤操作命令

有许多简单且有⽤的命令可以⽤来管理拓扑，它们可以提交、杀 死、禁⽤、再平衡拓扑。

提交任务命令格式：stormjar【jar路径】【拓扑包名.拓扑类 名】【拓扑名称】

bin/stormjarexamples/storm-starter/storm-starter-topologies0.10.0.jarstorm.starter.WordCountTopologywordcount

杀死任务命令格式：stormkil【拓扑名称】 -w10（执⾏kil命令 时可以通过-w[等待秒数]指定拓扑停⽤以后的等待时间）

stormkiltopology-name-w10

停⽤任务命令格式：stormdeactivte【拓扑名称】

stormdeactivtetopology-name 我们能够挂起或停⽤运⾏中的拓扑。当停⽤拓扑时，所有已分发的 元组都会得到处理，但是spouts的nextTuple⽅法不会被调⽤。销毁 ⼀个拓扑，可以使⽤kil命令。它会以⼀种安全的⽅式销毁⼀个拓 扑，⾸先停⽤拓扑，在等待拓扑消息的时间段内允许拓扑完成当前 的数据流。

启⽤任务命令格式：stormactivate【拓扑名称】

stormactivatetopology-name

重新部署任务命令格式：stormrebalance【拓扑名称】

stormrebalancetopology-name

再平衡使你重分配集群任务。这是个很强⼤的命令。⽐如，你向 ⼀个运⾏中的集群增加了节点。再平衡命令将会停⽤拓扑，然后在 相应超时时间之后重分配⼯⼈，并重启拓扑。

# 5、Storm集群的进程及⽇志熟悉

- 5.1、部署成功之后，启动storm集群。 依次启动集群的各种⾓⾊

- 5.2、查看nimbus的⽇志信息 在nimbus的服务器上 cd/export/servers/storm/logs tail-10f/export/servers/storm/logs/nimbus.log


- 5.3、查看ui运⾏⽇志信息 在ui的服务器上，⼀般和nimbus⼀个服务器 cd/export/servers/storm/logs tail-10f/export/servers/storm/logs/ui.log

- 5.4、查看supervisor运⾏⽇志信息 在supervisor服务上 cd/export/servers/storm/logs tail-10f/export/servers/storm/logs/supervisor.log

- 5.5、查看supervisor上worker运⾏⽇志信息 在supervisor服务上 cd/export/servers/storm/logs tail-10f/export/servers/storm/logs/worker-6702.log

(该worker正在运⾏wordcount程序)

- 6、Storm源码下载及⽬录熟悉


![image 3](assets/imageFile3.png)

- 6.1、在Storm官⽅⽹站上寻找源码地址

- 6.2、点击⽂字标签进⼊github 点击Apache/storm⽂字标签，进⼊github


htp:/storm.apache.org/downloads.html

![image 4](assets/imageFile4.png)

htps:/github.com/apache/storm

- 6.3、拷贝storm源码地址 在⽹页右侧，拷贝storm源码地址

- 6.4、使⽤Subversion客户端下载

- 6.5、Storm源码⽬录分析（重要）


![image 5](assets/imageFile5.png)

![image 6](assets/imageFile6.png)

htps:/github.com/apache/storm/tags/v0.9.5

![image 7](assets/imageFile7.png)

扩展包中的三个项⽬，使storm能与hbase、hdfs、kafka交互

![image 8](assets/imageFile8.png)

# 7、Storm单词技术案例（重点掌握）

- 7.1、功能说明 设计⼀个topology，来实现对⽂档⾥⾯的单词出现的频率进⾏统 计。 整个topology分为三个部分：

- 7.2、项⽬主要流程

- 7.3、RandomSentenceSpout的实现及⽣命周期


RandomSentenceSpout：数据源，在已知的英⽂句⼦中，随机 发送⼀条句⼦出去。

SplitSentenceBolt：负责将单⾏⽂本记录（句⼦）切分成单词

WordCountBolt：负责对单词的频率进⾏累加

![image 9](assets/imageFile9.png)

![image 10](assets/imageFile10.png)

- 7.4、SplitSentenceBolt的实现及⽣命周期

- 7.5、WordCountBolt的实现及⽣命周期


![image 11](assets/imageFile11.png)

![image 12](assets/imageFile12.png)

- 7.6、StreamGrouping详解 Storm⾥⾯有7种类型的streamgrouping


ShufleGrouping:随机分组，随机派发stream⾥⾯的tuple，保 证每个bolt接收到的tuple数⽬⼤致相同。

FieldsGrouping：按字段分组，⽐如按userid来分组，具有同样 userid的tuple会被分到相同的Bolts⾥的⼀个task，⽽不同的 userid则会被分配到不同的bolts⾥的task。

AlGrouping：⼴播发送，对于每⼀个tuple，所有的bolts都会收 到。

GlobalGrouping：全局分组，这个tuple被分配到storm中的⼀个 bolt的其中⼀个task。再具体⼀点就是分配给id值最低的那个 task。

NonGrouping：不分组，这streamgrouping个分组的意思是说 stream不关⼼到底谁会收到它的tuple。⽬前这种分组和Shufle grouping是⼀样的效果，有⼀点不同的是storm会把这个bolt放到 这个bolt的订阅者同⼀个线程⾥⾯去执⾏。

DirectGrouping：直接分组，这是⼀种⽐较特别的分组⽅法，⽤ 这种分组意味着消息的发送者指定由消息接收者的哪个task处理 这个消息。只有被声明为DirectStream的消息流可以声明这种分 组⽅法。⽽且这种消息tuple必须使⽤emitDirect⽅法来发射。消 息处理者可以通过TopologyContext来获取处理它的消息的task 的id（OutputColector.emit⽅法也会返回task的id）。

Localorshuflegrouping：如果⽬标bolt有⼀个或者多个task在 同⼀个⼯作进程中，tuple将会被随机发⽣给这些tasks。否则， 和普通的ShufleGrouping⾏为⼀致。
