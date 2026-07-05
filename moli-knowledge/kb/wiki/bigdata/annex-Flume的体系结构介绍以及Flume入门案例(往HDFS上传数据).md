---
title: Flume的体系结构介绍以及Flume入门案例(往HDFS上传数据).note（原文插图 annex）
slug: annex-Flume的体系结构介绍以及Flume入门案例(往HDFS上传数据)
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/flume/Flume的体系结构介绍以及Flume入门案例(往HDFS上传数据).note.md
related: [flume-与-数据采集]
created: 2026-07-05
updated: 2026-07-05
---

# #Flume的体系结构

# 学习前⾔ 想学习⼀下Flume，⽹上找了好多⽂章基本上都说的很简单，只有⼀半什么的，简直就是坑爹，饿顿时 怒⽕就上来了，学个东⻄真不容易，然后⾃⼰耐⼼的把这些零零碎碎的东⻄整理整理，各种搭环境实 验之后才弄好的，也不容易啊，希望可以帮到想学Flume的你 、、、对云计算有兴趣的朋友可以加上⾯说的21429307这 个群哦，⼀起学习，共同进步 ...

## # Flume介绍

Flume是Cloudera提供的⼀个⾼可⽤的，⾼可靠的，分布式的海量⽇志采集、聚合和传输的系统， Flume⽀持在⽇志系统中定制各类数据发送⽅，⽤于收集数据；同时，Flume提供对数据进⾏简单处 理，并写到各种数据接受⽅（可定制）的能⼒。

# 系统功能

# ⽇志收集

Flume最早是Cloudera提供的⽇志收集系统，⽬前是Apache下的⼀个孵化项⽬，Flume⽀持在⽇志系 统中定制各类数据发送⽅，⽤于收集数据。

# 数据处理

Flume提供对数据进⾏简单处理，并写到各种数据接受⽅（可定制）的能⼒Flume提供了从console （控制台）、RPC（Thrift-RPC）、text（⽂件）、tail（UNIX tail）、syslog（syslog⽇志系统，⽀持 TCP和UDP等2种模式），exec（命令执⾏）等数据源上收集数据的能⼒。

# ⼯作⽅式 （Flume-NG旧版本的概念，新版本已经丢弃）

Flume采⽤了多Master的⽅式。为了保证配置数据的⼀致性，Flume引⼊了ZoKeper，⽤于保存配置 数据，ZoKeper本身可保证配置数据的⼀致性和⾼可⽤，另外，在配置数据发⽣变化时， ZoKeper可以通知FlumeMaster节点。Flume Master间使⽤gosip协议同步数据。

## # Flume的设计⽬标（百度百科）

# 可靠性

当节点出现故障时，⽇志能够被传送到其他节点上⽽不会丢失。Flume提供了三种级别的可靠性保障， 从强到弱依次分别为：end-to-end（收到数据agent⾸先将event写到磁盘上，当数据传送成功后，再 删除；如果数据发送失败，可以重新发送。），Store on failure（这也是scribe采⽤的策略，当数据接 收⽅crash时，将数据写到本地，待恢复后，继续发送），Bestefort（数据发送到接收⽅后，不会进 ⾏确认）。

# 可扩展性（Flume-NG旧版本的概念，新版本已经丢弃）

Flume采⽤了三层架构，分别为agent，colector和storage，每⼀层均可以⽔平扩展。其中，所有 agent和colector由master统⼀管理，这使得系统容易监控和维护，且master允许有多个（使⽤ ZoKeper进⾏管理和负载均衡），这就避免了单点故障问题。

# 可管理性（Flume-NG旧版本的概念，新版本已经丢弃）

所有agent和coletor由master统⼀管理，这使得系统便于维护。多master情况，Flume利⽤ ZoKeper和gosip，保证动态配置数据的⼀致性。⽤户可以在master上查看各个数据源或者数据流 执⾏情况，且可以对各个数据源配置和动态加载。Flume提供了web和shel script comand两种形式 对数据流进⾏管理。

# 功能可扩展性

⽤户可以根据需要添加⾃⼰的agent，colector或者storage。此外，Flume⾃带了很多组件，包括各种 agent（file， syslog等），colector和storage（file，HDFS等）。（这⾥看下⾯的Flume架构图你就 明⽩了）

## # Flume架构

# Flume基础架构，如下图：

![image 1](<Flume的体系结构介绍以及Flume入门案例(往HDFS上传数据).note_images/imageFile1.png>)

这是⼀个flume-ng 最简单的图。flume-ng 是由⼀个个agent组成的。⼀个agent就像⼀个细胞⼀样。

# Flume的多agent架构，如下图：

![image 2](<Flume的体系结构介绍以及Flume入门案例(往HDFS上传数据).note_images/imageFile2.png>)

上⾯是两个agent链接在⼀起的，再看看更多的 .

# Flume的合并（合作）架构，如下图：

![image 3](<Flume的体系结构介绍以及Flume入门案例(往HDFS上传数据).note_images/imageFile3.png>)

你是不是觉得这种设计是不是吊炸天了，可以随意组合，跟搭积⽊⼀样。跟Storm的设计思想是差不多 的，何⽌吊炸天啊，简直就是吊炸天 、、、

# Flume的多路复⽤架构，如下图：

![image 4](<Flume的体系结构介绍以及Flume入门案例(往HDFS上传数据).note_images/imageFile4.png>)

# agent的构造

每个agent⾥都有三部分构成：source、chanel和sink。 就相当于source接收数据，通过chanel传输数据，sink把数据写到下⼀端。这就完了，就这么简单。 其中source有很多种可以选择，chanel有很多种可以选择，sink也同样有多种可以选择，并且都⽀持 ⾃定义。饿靠！太灵活了。想怎么玩就怎么玩，这你妹的！ 同时，如上上图所示，agent还⽀持选择器，就是⼀个source⽀持多个chanel和多个sink，这样就完 成了数据的分发。 这就完了，flume-ng就这么简单 . 从看到最后⽤，⼀天⾜可以搞定。剩下的就是怎么组织你的agent的问题了。也就是搭积⽊的过程 . 另外有⼀点需要强调的是，flume-ng提供了⼀种特殊的启动⽅式（不同于agent），那就是client启 动。cilent是⼀个特殊的agent, 他的source是⽂件，chanel是内存，sink是arvo。实际上是为了⽅便 ⼤家⽤，直接来传递⽂件的。具体可以看看官⽅使⽤⼿册。 估计到这⼉，应该对flume-ng有了解了吧 、、、on my god、、、

## # Flume的安装

### # 下载 flume（使⽤wget下载）

[rot@rs29 flume]# wget -c -P /rot

htp:/mirors.cnic.cn/apache/flume/1.5.0/apache-flume-1.5. 0-bin.tar.gz

# 安装

rot@rs29 flume]# pwd /usr/local/adsit/yting/apache/flume [rot@rs29 flume]#l total 4 drwxr-xr-x 3 rot rot 4096 Jun 24 17 25mirors.cnic.cn [rot@rs29 flume]# cpmirors.cnic.cn/apache/flume/1.5.0/apache-flume-1.5.0-bin.tar.gz . [rot@rs29 flume]#l total 25276

- -rw-r-r- 1 rot rot 25876246 Jun 24 17 27apache-flume-1.5.0-bin.tar.gz drwxr-xr-x 3 rot rot 4096 Jun 24 17 25 mirors.cnic.cn [rot@rs29 flume]# tar -zxvfapache-flume-1.5.0-bin.tar.gz

[rot@rs29 flume]#l total 25280 drwxr-xr-x 7 rot rot 4096 Jun 24 17 27 apache-flume-1.5.0-bin

- -rw-r-r- 1 rot rot 25876246 Jun 24 17 27apache-flume-1.5.0-bin.tar.gz

- drwxr-xr-x 3 rot rot 4096 Jun 24 17 25 mirors.cnic.cn [rot@rs29 flume]# rm -rfapache-flume-1.5.0-bin.tar.gz [rot@rs29 flume]# rm -rf mirors.cnic.cn/ [rot@rs29 flume]#l total 4 drwxr-xr-x 7 rot rot 4096 Jun 24 17 27apache-flume-1.5.0-bin [rot@rs29 flume]#


[rot@rs29 conf]# pwd /usr/local/adsit/yting/apache/flume/apache-flume-1.5.0-bin/conf [rot@rs29 conf]#l total 12

- -rw-r-r- 1 501 games 161 Mar 29 06 15flume-conf.properties.template

- -rw-r-r- 1 501 games197 Mar 29 06 15 flume-env.sh.template

- -rw-r-r- 1 501 games 3063 Mar 29 06 15log4j.properties [rot@rs29 conf]# cp flume-env.sh.templateflume-env.sh [rot@rs29 conf]# vi flume-env.sh


### # 修改 flume-env.sh 配置⽂件

# Enviroment variables can be sethere. JAVA_HOME=/usr/local/adsit/yting/jdk/jdk1.7.0_60

# 修改 flume-site.xml 配置⽂件（貌似没有该步骤，貌似也可以修改，研究后再 来弄吧！）

# 验证 flume是否安装成功

[rot@rs29 conf]#./bin/flume-ng version Flume 1.5.0 Source code repository: Revision: 86320df808c4cd0c13d1cf0320454a94f1ea97 Compiled by hshredharan on Wed May 7 14 49 18 PDT 2014 From source with checksuma01fe726e4380ba0c9f7a7d 2db961f 出现这样的信息表示安装成功了

htps:/git-wip-us.apache.org/repos/asf/flume.git

## # Flume⼊⻔案例

# Flume监控指定⽬录下的⽇志信息，并将⽇志信息上传到HDFS中去

# 在conf⽬录下新建example.conf配置⽂件

新建⽂件：在conf⽬录下新建⼀个example.conf⽂件（随便起什么名字），当然随便哪⾥都⾏ 注意：⽂件名最好跟配置中的名字⼀样，⽐如⾥⾯的agent1最好跟外⾯的⽂件名⼀样，⻅名知意 [rot@rs29 conf]# pwd /usr/local/adsit/yting/apache/flume/apache-flume-1.5.0-bin/conf [rot@rs29 apache-flume-1.5.0-bin]# catconf/example.conf # agent1 : yting first flume example agent1.sources=source1 agent1.sinks=sink1 agent1.chanels=chanel1

# configure source1 agent1.sources.source1.type=spoldir agent1.sources.source1.spolDir=/usr/local/yting/flume/tdata/tdir1 agent1.sources.source1.chanels=chanel1 agent1.sources.source1.fileHeader = false

# configure sink1 agent1.sinks.sink1.type=hdfs agent1.sinks.sink1.hdfs.path=hdfs:/rs29 9 0/yting/flumet agent1.sinks.sink1.hdfs.fileType=DataStream

agent1.sinks.sink1.hdfs.writeFormat=TEXT agent1.sinks.sink1.hdfs.rolInterval=4 agent1.sinks.sink1.chanel=chanel1

# configure chanel1 agent1.chanels.chanel1.type=file agent1.chanels.chanel1.checkpointDir=/usr/local/yting/flume/checkpointdir/tcpdir/example_agent 1_01 agent1.chanels.chanel1.dataDirs=/usr/local/yting/flume/datadirs/tdirs/example_agent1_01 注意：红⾊字体部分⾃⼰修改成⾃⼰对应的⽬录了

### # 运⾏Flume 使⽤example.conf

#命令参数说明

- -c conf 指定配置⽬录为conf
- -f conf/example.conf 指定配置⽂件为conf/example.conf
- -n agent1 指定agent名字为agent1,需要与example.conf中的⼀致（这⾥不⼀致，可能会⼀直停在那 ⾥，请参考笔记中后⾯的错误全集Flume部分，那⾥介绍了错误的分析，原因，解决）
- -Dflume.rot.loger=INFO,console 指定DEBUF模式在console输出INFO信息

[rot@rs29conf]# ./bin/flume-ng agent -c conf/ -f conf/example.conf-n agent1Dflume.rot.loger=INFO,console

- -bash:./bin/flume-ng: No such file or directory [rot@rs29conf]# cd. [rot@rs29apache-flume-1.5.0-bin]# ./bin/flume-ng agent -c conf/ -f conf/example.conf -nagent1

- -Dflume.rot.loger=INFO,console Info: Sourcingenvironment configuration script/usr/local/adsit/yting/apache/flume/apache-flume-


- 1.5.0-bin/conf/flume-env.sh Info: IncludingHadop libraries found via(/usr/local/adsit/yting/apache/hadop/hadop-

- 2.2.0/bin/hadop) for HDFS aces Info: Excluding/usr/local/adsit/yting/apache/hadop/hadop-2.2.0/share/hadop/comon/lib/slf4japi-1.7.5.jarfrom claspath Info: Excluding/usr/local/adsit/yting/apache/hadop/hadop-2.2.0/share/hadop/comon/lib/slf4jlog4j12-1.7.5.jarfrom claspath Info: IncludingHBASE libraries found via(/usr/local/adsit/yting/apache/hbase/hbase-0.96.2hadop2/bin/hbase) for HBASEaces


Info: Excluding/usr/local/adsit/yting/apache/hbase/hbase-0.96.2-hadop2/bin/./lib/slf4j-api1.6.4.jarfrom claspath Info: Excluding/usr/local/adsit/yting/apache/hbase/hbase-0.96.2-hadop2/bin/./lib/slf4j-log4j12-

- 1.6.4.jarfrom claspath Info: Excluding/usr/local/adsit/yting/apache/hadop/hadop-2.2.0/share/hadop/comon/lib/slf4japi-1.7.5.jarfrom claspath Info: Excluding/usr/local/adsit/yting/apache/hadop/hadop-2.2.0/share/hadop/comon/lib/slf4jlog4j12-1.7.5.jarfrom claspath ….capacity-scheduler/*.jar:/conf'-Djava.library.path=:/usr/local/adsit/yting/apache/hadop/hadop-

- 2.2.0/lib:/usr/local/adsit/yting/apache/hadop/hadop-2.2.0/liborg.apache.flume.node.Aplication f conf/example.conf -n agent1


- 2014-06-2510 37 45,763 (lifecycleSupervisor-1-0) [INFO org.apache.flume.node.PolingPropertiesFileConfigurationProvider.start(PolingPropertiesFileConfig urationProvider.java:61)]Configuration provider starting


- 2014-06-2510 37 45,72 (conf-file-poler-0) [INFO org.apache.flume.node.PolingPropertiesFileConfigurationProvider$FileWatcherRunable.run(Polin gPropertiesFileConfigurationProvider.java:13)]Reloading configuration file:conf/example.conf


- 2014-06-2510 37 45,781 (conf-file-poler-0) [INFO org.apache.flume.conf.FlumeConfiguration$AgentConfiguration.adProperty(FlumeConfiguration.j ava:1016)]Procesing:sink1


- 2014-06-2510 37 45,783 (conf-file-poler-0) [INFO org.apache.flume.conf.FlumeConfiguration$AgentConfiguration.adProperty(FlumeConfiguration.j ava:1016)]Procesing:sink1


- 2014-06-2510 37 45,783 (conf-file-poler-0) [INFO org.apache.flume.conf.FlumeConfiguration$AgentConfiguration.adProperty(FlumeConfiguration.j ava:930)]Aded sinks: sink1 Agent: agent1


- 2014-06-2510 37 45,783 (conf-file-poler-0) [INFO org.apache.flume.conf.FlumeConfiguration$AgentConfiguration.adProperty(FlumeConfiguration.j ava:1016)]Procesing:sink1


- 2014-06-2510 37 45,783 (conf-file-poler-0) [INFO org.apache.flume.conf.FlumeConfiguration$AgentConfiguration.adProperty(FlumeConfiguration.j ava:1016)]Procesing:sink1


- 2014-06-2510 37 45,783 (conf-file-poler-0) [INFO org.apache.flume.conf.FlumeConfiguration$AgentConfiguration.adProperty(FlumeConfiguration.j ava:1016)]Procesing:sink1


- 2014-06-2510 37 45,784 (conf-file-poler-0) [INFO org.apache.flume.conf.FlumeConfiguration$AgentConfiguration.adProperty(FlumeConfiguration.j ava:1016)]Procesing:sink1


- 2014-06-2510 37 45,809 (conf-file-poler-0) [INFO org.apache.flume.conf.FlumeConfiguration.validateConfiguration(FlumeConfiguration.java:140)]Pos t-validation flume configuration contains configuration for agents: [agent1]

- 2014-06-2510 37 45,809 (conf-file-poler-0) [INFO org.apache.flume.node.AbstractConfigurationProvider.loadChanels(AbstractConfigurationProvide r.java:150)]Creating chanels

- 2014-06-2510 37 45,823 (conf-file-poler-0) [INFO org.apache.flume.chanel.DefaultChanelFactory.create(DefaultChanelFactory.java:40)]Creating instance of chanel chanel1 type file

- 2014-06-2510 37 45,828 (conf-file-poler-0) [INFO org.apache.flume.node.AbstractConfigurationProvider.loadChanels(AbstractConfigurationProvide r.java:205)]Created chanel chanel1

- 2014-06-2510 37 45,829 (conf-file-poler-0) [INFO org.apache.flume.source.DefaultSourceFactory.create(DefaultSourceFactory.java:39)]Creating instance of source source1, type spoldir


- 2014-06-2510 37 45,84 (conf-file-poler-0) [INFO org.apache.flume.sink.DefaultSinkFactory.create(DefaultSinkFactory.java:40)]Creating instance of sink: sink1, type: hdfs

- 2014-06-2510 37 46,293 (conf-file-poler-0) [WARN -org.apache.hadop.util.NativeCodeLoader. <clinit>(NativeCodeLoader.java:62)]Unable to load native-hadop library for your platform. using builtin-javaclases where aplicable


- 2014-06-2510 37 46,572 (conf-file-poler-0) [INFO org.apache.flume.sink.hdfs.HDFSEventSink.authenticate(HDFSEventSink.java: 5)]Hadop Security enabled: false


- 2014-06-2510 37 46,576 (conf-file-poler-0) [INFO org.apache.flume.node.AbstractConfigurationProvider.getConfiguration(AbstractConfigurationProv ider.java:19)]Chanel chanel1 conected to [source1, sink1]


- 2014-06-2510 37 46,587 (conf-file-poler-0) [INFO org.apache.flume.node.Aplication.startAlComponents(Aplication.java:138)]Starting new configuration:{ sourceRuners:{source1=EventDrivenSourceRuner: {source:Spol Directory source source1: { spolDir:/usr/local/yting/flume/tdata/tdir1 }} sinkRuners:{sink1=SinkRuner: {policy:org.apache.flume.sink.DefaultSinkProcesor@7205c140 counterGroup:{name:nul counters: {} }} chanels:{chanel1=FileChanel chanel1 { dataDirs: [/usr/local/yting/flume/datadirs/tdirs/example_agent1_01]} }


- 2014-06-2510 37 46,593 (conf-file-poler-0) [INFO org.apache.flume.node.Aplication.startAlComponents(Aplication.java:145)]Starting Chanel chanel1


- 2014-06-2510 37 46,593 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.FileChanel.start(FileChanel.java:259)] StartingFileChanel chanel1 { dataDirs:[/usr/local/yting/flume/datadirs/tdirs/example_agent1_01] }.


- 2014-06-2510 37 46,617 (lifecycleSupervisor-1-0) [INFO -org.apache.flume.chanel.file.Log.<init> (Log.java:328)] Encryption is notenabled

2014-06-2510 37 46,618 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.Log.replay(Log.java:373)] Replay started 2014-06-2510 37 46,620 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.Log.replay(Log.java:385)] Found NextFileID 0,from [] 2014-06-2510 37 46, 61 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.EventQueueBackingStoreFile.<init> (EventQueueBackingStoreFile.java:91)]Prealocated/usr/local/yting/flume/checkpointdir/tcpdir/exam ple_agent1_01/checkpoint to808232 for capacity 1 0 2014-06-2510 37 46, 63 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.EventQueueBackingStoreFileV3.<init> (EventQueueBackingStoreFileV3.java:53)]Starting up with/usr/local/yting/flume/checkpointdir/tcpdir/example_agent1_01/checkpoint and/usr/local/yting/flume/checkpointdir/tcpdir/example_agent1_01/checkpoint.meta 2014-06-2510 37 47,095 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.FlumeEventQueue.<init>(FlumeEventQueue.java:14)]QueueSet population inserting 0 tok 0 2014-06-2510 37 47,10 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.Log.replay(Log.java:423)] Last Checkpoint Wed Jun25 10 37 46 CST 2014, queue depth = 0 2014-06-2510 37 47,105 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.Log.doReplay(Log.java:507)] Replaying logs withv2 replay logic 2014-06-2510 37 47,109 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.ReplayHandler.replayLog(ReplayHandler.java:249)]Starting replay of []

- 2014-06-2510 37 47,109 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.ReplayHandler.replayLog(ReplayHandler.java:346)]read: 0, put: 0, take: 0, rolback: 0, comit: 0, skip: 0, eventCount:0


- 2014-06-2510 37 47,10 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.FlumeEventQueue.replayComplete(FlumeEventQueue.java:407)]Sear ch Count = 0, Search Time = 0, Copy Count = 0, Copy Time = 0


- 2014-06-2510 37 47,19 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.Log.replay(Log.java:470)] Roling/usr/local/yting/flume/datadirs/tdirs/example_agent1_01


- 2014-06-2510 37 47,120 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.Log.rol(Log.java:932)] Rol start/usr/local/yting/flume/datadirs/tdirs/example_agent1_01


- 2014-06-2510 37 47,137 (lifecycleSupervisor-1-0) [INFO org.apache.flume.tols.DirectMemoryUtils.getDefaultDirectMemorySize(DirectMemoryUtils.java:1 3)]Unable to get maxDirectMemory from VM: NoSuchMethodException:sun.misc.VM.maxDirectMemory(nul)


- 2014-06-2510 37 47,140 (lifecycleSupervisor-1-0) [INFO org.apache.flume.tols.DirectMemoryUtils.alocate(DirectMemoryUtils.java:47)]Direct Memory Alocation: Alocation =1048576, Alocated = 0, MaxDirectMemorySize = 1874368, Remaining = 1874368


- 2014-06-2510 37 47,195 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.LogFile$Writer.<init>(LogFile.java:214)]Opened /usr/local/yting/flume/datadirs/tdirs/example_agent1_01/log-1 2014-06-2510 37 47,208 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.Log.rol(Log.java:948)] Rol end 2014-06-2510 37 47,208 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.EventQueueBackingStoreFile.beginCheckpoint(EventQueueBackingS toreFile.java:214)]Start checkpoint


- for/usr/local/yting/flume/checkpointdir/tcpdir/example_agent1_01/checkpoint,elements to sync = 0 2014-06-2510 37 47,21 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.EventQueueBackingStoreFile.checkpoint(EventQueueBackingStoreFi le.java:239)]Updating checkpoint metadata: logWriteOrderID: 140363867120, queueSize: 0,queueHead: 0 2014-06-2510 37 47,235 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.Log.writeCheckpoint(Log.java:105)] Updatedcheckpoint for file:/usr/local/yting/flume/datadirs/tdirs/example_agent1_01/log-1 position: 0logWriteOrderID: 140363867120 2014-06-2510 37 47,235 (lifecycleSupervisor-1-0) [INFO org.apache.flume.chanel.file.FileChanel.start(FileChanel.java:285)] QueueSize after replay: 0 [chanel=chanel1]


- 2014-06-2510 37 47,296 (lifecycleSupervisor-1-0) [INFO org.apache.flume.instrumentation.MonitoredCounterGroup.register(MonitoredCounterGroup.java:1 19)]Monitored counter group for type: CHANEL, name: chanel1: Sucesfulyregistered new MBean.

- 2014-06-2510 37 47,296 (lifecycleSupervisor-1-0) [INFO org.apache.flume.instrumentation.MonitoredCounterGroup.start(MonitoredCounterGroup.java:95)] Component type: CHANEL, name: chanel1 started

- 2014-06-2510 37 47,297 (conf-file-poler-0) [INFO org.apache.flume.node.Aplication.startAlComponents(Aplication.java:173)] StartingSink sink1


- 2014-06-2510 37 47,297 (conf-file-poler-0) [INFO org.apache.flume.node.Aplication.startAlComponents(Aplication.java:184)]Starting Source source1

- 2014-06-2510 37 47,298 (lifecycleSupervisor-1-0) [INFO org.apache.flume.source.SpolDirectorySource.start(SpolDirectorySource.java:7)]SpolDirector ySource source starting with directory:/usr/local/yting/flume/tdata/tdir1 2014-06-2510 37 47,30 (lifecycleSupervisor-1-1) [INFO org.apache.flume.instrumentation.MonitoredCounterGroup.register(MonitoredCounterGroup.java:1 19)]Monitored counter group for type: SINK, name: sink1: Sucesfuly registerednew MBean. 2014-06-2510 37 47,30 (lifecycleSupervisor-1-1) [INFO org.apache.flume.instrumentation.MonitoredCounterGroup.start(MonitoredCounterGroup.java:95)] Component type: SINK, name: sink1 started 2014-06-2510 37 47, 30 (lifecycleSupervisor-1-0) [INFO org.apache.flume.instrumentation.MonitoredCounterGroup.register(MonitoredCounterGroup.java:1 19)]Monitored counter group for type: SOURCE, name: source1: Sucesfulyregistered new MBean.


- 2014-06-2510 37 47, 30 (lifecycleSupervisor-1-0) [INFO org.apache.flume.instrumentation.MonitoredCounterGroup.start(MonitoredCounterGroup.java:95)] Component type: SOURCE, name: source1 started

- 2014-06-2510 37 47, 31 (pol-6-thread-1) [INFO org.apache.flume.source.SpolDirectorySource$SpolDirectoryRunable.run(SpolDirectorySourc e.java:254)]Spoling Directory Source runer has shutdown.


- 2014-06-2510 37 47,831 (pol-6-thread-1) [INFO org.apache.flume.source.SpolDirectorySource$SpolDirectoryRunable.run(SpolDirectorySourc e.java:254)]Spoling Directory Source runer has shutdown.

- 2014-06-2510 37 48, 32 (pol-6-thread-1) [INFO org.apache.flume.source.SpolDirectorySource$SpolDirectoryRunable.run(SpolDirectorySourc e.java:254)]Spoling Directory Source runer has shutdown.


到了这⾥说明你的程序运⾏正常了，但是你的监视⽬录 /usr/local/yting/flume/tdata/tdir1下没有新⽂件 的产⽣，所以会⼀直出现上⾯的那条信息

# 在/usr/local/yting/flume/tdata/tdir1这个flume监视⽬录下添加⼀个新⽂件 yting_flume_example_agent1_ 01.log

[rot@rs29 hadop-2.2.0]# ./bin/hadop fs -ls /yting 14/06/25 10 48  0 WARN util.NativeCodeLoader: Unableto load native-hadop library for your platform. using builtin-java claseswhere aplicable

- Found 1 items


- -rw-r-r- 3rot supergroup 4278 2014-06-1018 29 /yting/yarn-daemon.sh [rot@rs29 tdir1]#l total 0 [rot@rs29 tdir1]#l -a total 12

- drwxr-xr-x 3 rot rot 4096 Jun 25 10 37 . drwxr-xr-x 3 rot rot 4096 Jun 24 2 25. drwxr-xr-x 2 rot rot 4096 Jun 25 09 48 .flumespol（隐藏⽂件） [rot@rs29 tdir1]# viyting_flume_example_agent1_ 01.log The you smile until forever . [rot@rs29 tdir1]#l total 4


- -rw-r-r- 1 rot rot 50 Jun 25 10 51yting_flume_example_agent1_ 01.log.COMPLETED


# ⽂件名变成.COMPLETED结尾

说明该⽂件yting_flume_example_agent1_ 01.log已经被flume处理了，处理过后的⽂件名变成 yting_flume_example_agent1_ 01.log.COMPLETED,接下来看看flume那边的信息，应该发⽣变化 了

# 查看flume shel的信息变化

2014-06-25 10 51  0,530 (pol-6-thread-1) [INFO org.apache.flume.source.SpolDirectorySource$SpolDirectoryRunable.run(SpolDirectorySourc e.java:254)]Spoling Directory Source runer has shutdown.

- 2014-06-25 10 51 01,434 (pol-6-thread-1) [INFO org.apache.flume.client.avro.ReliableSpolingFileEventReader.rolCurentFile(ReliableSpolingFileE ventReader.java: 32)]Preparing to move file/usr/local/yting/flume/tdata/tdir1/yting_flume_example_agent1_ 01.log to/usr/local/yting/flume/tdata/tdir1/yting_flume_example_agent1_ 01.log.COMPLETED

- 2014-06-25 10 51 02,436 (pol-6-thread-1) [INFO org.apache.flume.source.SpolDirectorySource$SpolDirectoryRunable.run(SpolDirectorySourc e.java:254)]Spoling Directory Source runer has shutdown. 2014-06-25 10 51 02,473(SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.hdfs.BucketWriter.open(BucketWriter.java:261)]Creatinghdfs:/rs29 9 0/y ting/flumet/FlumeData.14036462360.tmp 2014-06-25 10 51 07, 40 (pol-6-thread-1) [INFO org.apache.flume.source.SpolDirectorySource$SpolDirectoryRunable.run(SpolDirectorySourc e.java:254)]Spoling Directory Source runer has shutdown. 2014-06-25 10 51 07,519 (hdfs-sink1-rol-timer-0)[INFO org.apache.flume.sink.hdfs.BucketWriter.close(BucketWriter.java:409)]Closinghdfs:/rs29 9 0/yt ing/flumet/FlumeData.14036462360.tmp 2014-06-25 10 51 07,521 (hdfs-sink1-cal-runer-3)[INFO org.apache.flume.sink.hdfs.BucketWriter$3.cal(BucketWriter.java: 39)]Close tries incremented 2014-06-25 10 51 07,549 (hdfs-sink1-cal-runer-4)[INFO org.apache.flume.sink.hdfs.BucketWriter$8.cal(BucketWriter.java: 69)]Renaminghdfs:/rs29 90 0/yting/flumet/FlumeData.14036462360.tmp tohdfs:/rs29 9 0/yting/flumet/FlumeData.14036462360 2014-06-25 10 51 07,57 (hdfs-sink1-rol-timer-0)[INFO org.apache.flume.sink.hdfs.HDFSEventSink$1.run(HDFSEventSink.java:402)]Writer calback caled. 2014-06-25 10 51 16, 48 (pol-6-thread-1) [INFO org.apache.flume.source.SpolDirectorySource$SpolDirectoryRunable.run(SpolDirectorySourc e.java:254)]Spoling Directory Source runer has shutdown. 2014-06-25 10 51 16,626 (Log-BackgroundWorker-chanel1)[INFO org.apache.flume.chanel.file.EventQueueBackingStoreFile.beginCheckpoint(EventQueueBackingS toreFile.java:214)]Start checkpoint


- for/usr/local/yting/flume/checkpointdir/tcpdir/example_agent1_01/checkpoint,elements to sync = 1 2014-06-25 10 51 16,628(Log-BackgroundWorker-chanel1) [INFO org.apache.flume.chanel.file.EventQueueBackingStoreFile.checkpoint(EventQueueBackingStoreFi le.java:239)]Updating checkpoint metadata: logWriteOrderID: 140363867125, queueSize: 0,queueHead: 0


2014-06-25 10 51 16,630(Log-BackgroundWorker-chanel1) [INFO org.apache.flume.chanel.file.Log.writeCheckpoint(Log.java:105)] Updatedcheckpoint for file:/usr/local/yting/flume/datadirs/tdirs/example_agent1_01/log-1 position: 206logWriteOrderID: 140363867125 注意：这⾥的分析请看下⾯的分析整个过程

# 查看hdfs上flume是否上传了数据

[rot@rs29 tdir1]# hadop fs -ls /yting

- Found 2 items drwxr-xr-x -rot supergroup 0 2014-06-2510 51 /yting/flumet


- -rw-r-r- 3rot supergroup 4278 2014-06-1018 29 /yting/yarn-daemon.sh [rot@rs29 tdir1]# hadop fs -ls /yting/flumet Found 1 items

- -rw-r-r- 3rot supergroup 50 2014-06-2510 51 /yting/flumet/FlumeData.14036462360 [rot@rs29 tdir1]# hadop fs -cat /yting/flumet/FlumeData.14036462360 The you smile until forever .（⽇志信息以及被上传了，OK、、、） [rot@rs29 tdir1]#


# 分析整个过程

通过分析flume shel的⽇志信息可以发现当我们在监视⽬录下新⽂件被创建保存的时候flume进⾏处理 并且重命名该⽂件，在原⽂件命后⾯添加.COMPLETE,然后将⽂件中的数据上传到hdfs中并创建⼀个临 时⽂件filename.tmp,上传成功后重命名hdfs上的临时⽂件，将⽂件后缀.tmp去掉就ok了，最后flume将 本次操作写⼊⾃⼰的⽇志信息。

# 初学者注意的地⽅

# 配置⽂件的⽂件名命 # 配置⽂件中的agent1与flume-ng 的-n 参数⼀直 # 最好配置⽂件的⽂件名与配置⽂件内容的名字⼀样，这样-n参数就不会敲错了
