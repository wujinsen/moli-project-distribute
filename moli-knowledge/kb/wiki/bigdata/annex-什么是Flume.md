---
title: 什么是Flume.note（原文插图 annex）
slug: annex-什么是Flume
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/flume/什么是Flume.note.md
related: [flume-与-数据采集]
created: 2026-07-05
updated: 2026-07-05
---

问题导读

- 1.什么是flume
- 2.flume的官⽅⽹站在哪⾥？
- 3.flume有哪些术语？
- 4.如何配置flume数据源码？


![image 1](assets/imageFile1.png)

⼀、什么是Flume?

flume 作为 cloudera 开发的实时⽇志收集系统，受到了业界的认可与⼴泛应⽤。Flume 初始的 发⾏版本⽬前被统称为 Flume OG（original generation），属于 cloudera。但随着 FLume 功能的扩 展，Flume OG 代码⼯程臃肿、核⼼组件设计不合理、核⼼配置不标准等缺点暴露出来，尤其是在 Flume OG 的最后⼀个发⾏版本 0.94.0 中，⽇志传输不稳定的现象尤为严重，为了解决这些问题， 201 年 10 ⽉ 2 号，cloudera 完成了 Flume-728，对 Flume 进⾏了⾥程碑式的改动：重构核⼼组 件、核⼼配置以及代码架构，重构后的版本统称为 Flume NG（next generation）；改动的另⼀原因 是将 Flume 纳⼊ apache 旗下，cloudera Flume 改名为 Apache Flume。

flume的特点：

flume是⼀个分布式、可靠、和⾼可⽤的海量⽇志采集、聚合和传输的系统。⽀持在⽇志系统中 定制各类

数据

发送⽅，⽤于收集数据;同时，Flume提供对数据进⾏简单处理，并写到各种数据接受⽅(⽐如⽂本、 HDFS、Hbase等)的能⼒ 。

flume的数据流由事件(Event)贯穿始终。事件是Flume的基本数据单位，它携带⽇志数据(字节数 组形式)并且携带有头信息，这些Event由Agent外部的Source⽣成，当Source捕获事件后会进⾏特定 的格式化，然后Source会把事件推⼊(单个或多个)Chanel中。你可以把Chanel看作是⼀个缓冲 区，它将保存事件直到Sink处理完该事件。Sink负责持久化⽇志或者把事件推向另⼀个Source。

flume的可靠性

当节点出现故障时，⽇志能够被传送到其他节点上⽽不会丢失。Flume提供了三种级别的可靠性 保障，从强到弱依次分别为：end-to-end（收到

数据

agent⾸先将event写到磁盘上，当数据传送成功后，再删除；如果数据发送失败，可以重新发 送。），Store on failure（这也是scribe采⽤的策略，当数据接收⽅crash时，将数据写到本地，待 恢复后，继续发送），Bestefort（数据发送到接收⽅后，不会进⾏确认）。

flume的可恢复性：

还是靠Chanel。推荐使⽤FileChanel，事件持久化在本地⽂件系统⾥(性能较差)。

flume的⼀些核⼼概念：

Agent 使⽤JVM 运⾏Flume。每台机器运⾏⼀个agent，但是可以在⼀个agent中包含多个 sources和sinks。

Client ⽣产数据，运⾏在⼀个独⽴的线程。

Source 从Client收集数据，传递给Chanel。

Sink 从Chanel收集 数据

，运⾏在⼀个独⽴线程。

Chanel 连接 sources 和 sinks ，这个有点像⼀个队列。

Events 可以是⽇志记录、 avro 对象等。

Flume以agent为最⼩的独⽴运⾏单位。⼀个agent就是⼀个JVM。单agent由Source、Sink和 Chanel三⼤组件构成，如下图：

![image 2](assets/imageFile2.png)

值得注意的是，Flume提供了⼤量内置的Source、Chanel和Sink类型。不同类型的 Source,Chanel和Sink可以⾃由组合。组合⽅式基于⽤户设置的配置⽂件，⾮常灵活。⽐如： Chanel可以把事件暂存在内存⾥，也可以持久化到本地硬盘上。Sink可以把⽇志写⼊HDFS, HBase，甚⾄是另外⼀个Source等等。Flume⽀持⽤户建⽴多级流，也就是说，多个agent可以协同 ⼯作，并且⽀持Fan-in、Fan-out、Contextual Routing、Backup Routes，这也正是NB之处。如下 图所示:

![image 3](assets/imageFile3.png)

⼆、flume的官⽅⽹站在哪⾥？

htp:/flume.apache.org/

三、在哪⾥下载？

htp:/ w.apache.org/dyn/closer.cgi/flume/1.5.0/apache-flume-1.5.0-bin.tar.gz

四、如何安装？

- 1)将下载的flume包，解压到/home/hadop⽬录中，你就已经完成了50%：）简单吧
- 2)修改 flume-env.sh 配置⽂件,主要是JAVA_HOME变量设置


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


rot@m1:/home/hadop/flume-1.5.0-bin# cp conf/flume-env.sh.template conf/flume-env.sh rot@m1:/home/hadop/flume-1.5.0-bin# vi conf/flume-env.sh # Licensed to the Apache Software Foundation (ASF) under one # or more contributor license agrements. Se the NOTICE file # distributed with this work for aditional information # regarding copyright ownership. The ASF licenses this file # to you under the Apache License, Version 2.0 (the # "License"); you may not use this file except in compliance # with the License. You may obtain a copy of the License at #

- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.


# htp:/ w.apache.org/licenses/LICENSE-2.0 # # Unles required by aplicable law or agred to in writing, software # distributed under the License is distributed on an "AS IS" BASIS, # WITHOUT WARANTIES OR CONDITIONS OF ANY KIND, either expres or implied. # Se the License for the specific language governing permisions and # limitations under the License.

# If this file is placed at FLUME_CONF_DIR/flume-env.sh, it wil be sourced # during Flume startup.

# Enviroment variables can be set here.

JAVA_HOME=/usr/lib/jvm/java-7-oracle

# Give Flume more memory and pre-alocate, enable remote monitoring via JMX #JAVA_OPTS="-Xms10m -Xmx20m -Dcom.sun.management.jmxremote"

# Note that the Flume conf directory is always included in the claspath. #FLUME_CLASPATH="

复制代码

3)验证是否安装成功

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng version Flume 1.5.0 Source code repository: htps:/git-wip-us.apache.org/repos/asf/flume.git Revision: 86320df808c4cd0c13d1cf0320454a94f1ea97 Compiled by hshredharan on Wed May 7 14 49 18 PDT 2014 From source with checksum a01fe726e4380ba0c9f7a7d 2db961f rot@m1:/home/hadop#

复制代码

出现上⾯的信息，表示安装成功了

五、flume的案例

1)案例1：Avro

Avro可以发送⼀个给定的⽂件给Flume，Avro 源使⽤AVRO RPC机制。

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
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.


rot@m1:/home/hadop#vi /home/hadop/flume-1.5.0-bin/conf/avro.conf

a1.sources = r1 a1.sinks = k1 a1.chanels = c1

# Describe/configure the source a1.sources.r1.type = avro a1.sources.r1.chanels = c1 a1.sources.r1.bind = 0.0.0.0 a1.sources.r1.port = 4141

# Describe the sink a1.sinks.k1.type = loger

# Use a chanel which bufers events in memory a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

# Bind the source and sink to the chanel a1.sources.r1.chanels = c1 a1.sinks.k1.chanel = c1

复制代码

- b)启动flume agent a1

复制代码

- c)创建指定⽂件

复制代码

- d)使⽤avro-client发送⽂件


1.

rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/avro.conf -n a1 -Dflume.rot.loger=INFO,console

1.

rot@m1:/home/hadop# echo "helo world" > /home/hadop/flume-1.5.0-bin/log.0

rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng avro-client -c . -H m1

- -p 4141 -F /home/hadop/flume-1.5.0-bin/log.0

rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng avro-client -c . -H m1

- -p 4141 -F /


复制代码

d)使⽤avro-client发送⽂件

1.

复制代码

f)在m1的控制台，可以看到以下信息，注意最后⼀⾏：

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


rot@m1:/home/hadop/flume-1.5.0-bin/conf# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/avro.conf -n a1 Dflume.rot.loger=INFO,console Info: Sourcing environment configuration script /home/hadop/flume-1.5.0-bin/conf/flumeenv.sh Info: Including Hadop libraries found via (/home/hadop/hadop-2.2.0/bin/hadop) for HDFS aces Info: Excluding /home/hadop/hadop-2.2.0/share/hadop/comon/lib/slf4j-api-1.7.5.jar from claspath Info: Excluding /home/hadop/hadop-2.2.0/share/hadop/comon/lib/slf4j-log4j12-1.7.5.jar from claspath

. 2014-08-10 10 43 25,12 (New I/O worker #1) [INFO org.apache.avro.ipc.NetyServer$NetyServerAvroHandler.handleUpstream(NetyServer.java: 171)] [id: 0x92464c4f, /192.168.1.50 59850 :> /192.168.1.50 4141] UNBOUND 2014-08-10 10 43 25,12 (New I/O worker #1) [INFO org.apache.avro.ipc.NetyServer$NetyServerAvroHandler.handleUpstream(NetyServer.java: 171)] [id: 0x92464c4f, /192.168.1.50 59850 :> /192.168.1.50 4141] CLOSED

- 2014-08-10 10 43 25,12 (New I/O worker #1) [INFO org.apache.avro.ipc.NetyServer$NetyServerAvroHandler.chanelClosed(NetyServer.java:2 09)] Conection to /192.168.1.50 59850 disconected.
- 2014-08-10 10 43 26,718 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 68 65 6C 6C 6F 20 7 6F 72 6C 64 helo world }


复制代码

2)案例2：Spol

Spol监测配置的⽬录下新增的⽂件，并将⽂件中的数据读取出来。需要注意两点：

- 1) 拷⻉到spol⽬录下的⽂件不可以再打开编辑。
- 2) spol⽬录下不可包含相应的⼦⽬录


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
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.


rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0-bin/conf/spol.conf

a1.sources = r1 a1.sinks = k1 a1.chanels = c1

# Describe/configure the source a1.sources.r1.type = spoldir a1.sources.r1.chanels = c1 a1.sources.r1.spolDir = /home/hadop/flume-1.5.0-bin/logs a1.sources.r1.fileHeader = true

# Describe the sink a1.sinks.k1.type = loger

# Use a chanel which bufers events in memory a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

# Bind the source and sink to the chanel a1.sources.r1.chanels = c1 a1.sinks.k1.chanel = c1

复制代码

b)启动flume agent a1

1.

rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/spol.conf -n a1 -Dflume.rot.loger=INFO,console

复制代码

c)追加⽂件到/home/hadop/flume-1.5.0-bin/logs⽬录

1.

rot@m1:/home/hadop# echo "spol test1" > /home/hadop/flume-1.5.0bin/logs/spol_text.log

复制代码

d)在m1的控制台，可以看到以下相关信息：

- 14/08/101 37 13 INFO source.SpolDirectorySource: Spoling Directory Source runer has shutdown.

- 14/08/101 37 13 INFO source.SpolDirectorySource: Spoling Directory Source runer has shutdown.
- 14/08/101 37 14 INFO avro.ReliableSpolingFileEventReader: Preparing to move file /home/hadop/flume-1.5.0-bin/logs/spol_text.log to /home/hadop/flume-1.5.0bin/logs/spol_text.log.COMPLETED


- 14/08/101 37 14 INFO source.SpolDirectorySource: Spoling Directory Source runer has shutdown.


- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.


- 14/08/101 37 14 INFO source.SpolDirectorySource: Spoling Directory Source runer has shutdown.

- 14/08/101 37 14 INFO sink.LogerSink: Event: { headers:{file=/home/hadop/flume-1.5.0bin/logs/spol_text.log} body: 73 70 6F 6F 6C 20 74 65 73 74 31 spol test1 }
- 14/08/101 37 15 INFO source.SpolDirectorySource: Spoling Directory Source runer has shutdown.

14/08/101 37 15 INFO source.SpolDirectorySource: Spoling Directory Source runer has shutdown. 14/08/101 37 16 INFO source.SpolDirectorySource: Spoling Directory Source runer has shutdown.

- 14/08/101 37 16 INFO source.SpolDirectorySource: Spoling Directory Source runer has shutdown.
- 14/08/101 37 17 INFO source.SpolDirectorySource: Spoling Directory Source runer has shutdown.




复制代码

3)案例3：Exec

EXEC执⾏⼀个给定的命令获得输出的源,如果要使⽤tail命令，必选使得file⾜够⼤才能看到 输出内容

a)创建agent配置⽂件

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


rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0-bin/conf/exec_tail.conf

a1.sources = r1 a1.sinks = k1 a1.chanels = c1

# Describe/configure the source a1.sources.r1.type = exec a1.sources.r1.chanels = c1 a1.sources.r1.comand = tail -F /home/hadop/flume-1.5.0-bin/log_exec_tail

- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.


# Describe the sink a1.sinks.k1.type = loger

# Use a chanel which bufers events in memory a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

# Bind the source and sink to the chanel a1.sources.r1.chanels = c1 a1.sinks.k1.chanel = c1

复制代码

- b)启动flume agent a1

复制代码

- c)⽣成⾜够多的内容在⽂件⾥


1.

rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/exec_tail.conf -n a1 -Dflume.rot.loger=INFO,console

1.

rot@m1:/home/hadop# for i in {1.10};do echo "exec tail$i" > /home/hadop/flume-1.5.0bin/log_

复制代码

e)在m1的控制台，可以看到以下信息：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


2014-08-10 10 59 25,513 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 65 78 65 63 20 74 61 69 6C 20 74 65 73 74 exec tail test } 2014-08-10 10 59 34,535 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 65 78 65 63 20 74 61 69 6C 20 74 65 73 74 exec tail test }

- 2014-08-101 01 40,57 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 65

- 78 65 63 20 74 61 69 6C 31 exec tail1 }

2014-08-101 01 41,180 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 65

- 78 65 63 20 74 61 69 6C 32 exec tail2 }




2014-08-101 01 41,180 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 65 78 65 63 20 74 61 69 6C 3 exec tail3 } 2014-08-101 01 41,181 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 65 78 65 63 20 74 61 69 6C 34 exec tail4 }

- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.


2014-08-101 01 41,181 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 65

- 78 65 63 20 74 61 69 6C 35 exec tail5 }

2014-08-101 01 41,181 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 65

- 78 65 63 20 74 61 69 6C 36 exec tail6 }


. . .

- 2014-08-101 01 51,50 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 65

- 78 65 63 20 74 61 69 6C 39 36 exec tail96 }

- 2014-08-101 01 51,50 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 65

78 65 63 20 74 61 69 6C 39 37 exec tail97 }

- 2014-08-101 01 51,51 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 65


- 78 65 63 20 74 61 69 6C 39 38 exec tail98 }

2014-08-101 01 51,51 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 65

- 78 65 63 20 74 61 69 6C 39 39 exec tail 9 }




2014-08-101 01 51,51 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.LogerSink.proces(LogerSink.java:70)] Event: { headers:{} body: 65 78 65 63 20 74 61 69 6C 31 30 30 exec tail10 }

复制代码

4)案例4：Syslogtcp

Syslogtcp监听TCP的端⼝做为

数据

源

a)创建agent配置⽂件

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


rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0-bin/conf/syslog_tcp.conf

a1.sources = r1 a1.sinks = k1 a1.chanels = c1

# Describe/configure the source a1.sources.r1.type = syslogtcp a1.sources.r1.port = 5140 a1.sources.r1.host = localhost

a1.sources.r1.chanels = c1

- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.


# Describe the sink a1.sinks.k1.type = loger

# Use a chanel which bufers events in memory a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

# Bind the source and sink to the chanel a1.sources.r1.chanels = c1 a1.sinks.k1.chanel = c1

复制代码

b)启动flume agent a1

1.

rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/syslog_tcp.conf -n a1 -Dflume.rot.loger=INFO,console

复制代码

c)测试产⽣syslog

1.

rot@m1:/home/hadop# echo "helo idoal.org syslog" | nc localhost 5140

复制代码

d)在m1的控制台，可以看到以下信息：

- 1.
- 2. 3. 4. 5.


14/08/101 41 45 INFO node.PolingPropertiesFileConfigurationProvider: Reloading configuration file:/home/hadop/flume-1.5.0-bin/conf/syslog_tcp.conf 14/08/101 41 45 INFO conf.FlumeConfiguration: Aded sinks: k1 Agent: a1

14/08/101 41 45 INFO conf.FlumeConfiguration: Procesing:k1 14/08/101 41 45 INFO conf.FlumeConfiguration: Procesing:k1 14/08/101 41 45 INFO conf.FlumeConfiguration: Post-validation flume configuration contains configuration for agents: [a1] 14/08/101 41 45 INFO node.AbstractConfigurationProvider: Creating chanels 14/08/101 41 45 INFO chanel.DefaultChanelFactory: Creating instance of chanel c1 type memory 14/08/101 41 45 INFO node.AbstractConfigurationProvider: Created chanel c1 14/08/101 41 45 INFO source.DefaultSourceFactory: Creating instance of source r1, type syslogtcp 14/08/101 41 45 INFO sink.DefaultSinkFactory: Creating instance of sink: k1, type: loger

6. 7.

8. 9.

10.

14/08/101 41 45 INFO node.AbstractConfigurationProvider: Chanel c1 conected to [r1, k1] 14/08/101 41 45 INFO node.Aplication: Starting new configuration:{ sourceRuners: {r1=EventDrivenSourceRuner: { source:org.apache.flume.source.SyslogTcpSource{name:r1,state:IDLE}} sinkRuners: {k1=SinkRuner: { policy:org.apache.flume.sink.DefaultSinkProcesor@6538b14 counterGroup:{ name:nul counters:{} }} chanels: {c1=org.apache.flume.chanel.MemoryChanel{name: c1} } 14/08/101 41 45 INFO node.Aplication: Starting Chanel c1 14/08/101 41 45 INFO instrumentation.MonitoredCounterGroup: Monitored counter group for type: CHANEL, name: c1: Sucesfuly registered new MBean. 14/08/101 41 45 INFO instrumentation.MonitoredCounterGroup: Component type: CHANEL, name: c1 started 14/08/101 41 45 INFO node.Aplication: Starting Sink k1

- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.


- 14/08/101 41 45 INFO node.Aplication: Starting Source r1

- 14/08/101 41 45 INFO source.SyslogTcpSource: Syslog TCP Source starting.
- 14/08/101 42 15 WARN source.SyslogUtils: Event created from Invalid Syslog data.


- 14/08/101 42 15 INFO sink.LogerSink: Event: { headers:{Severity=0, flume.syslog.status=Invalid, Facility=0} body: 68 65 6C 6C 6F 20 69 64 6F 61 6C 6C 2E 6F 72 67 helo idoal.org }


复制代码

5)案例5：JSONHandler

a)创建agent配置⽂件

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
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.


rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0-bin/conf/post_json.conf

a1.sources = r1 a1.sinks = k1 a1.chanels = c1

# Describe/configure the source a1.sources.r1.type = org.apache.flume.source.htp.HTPSource a1.sources.r1.port = 8 a1.sources.r1.chanels = c1

# Describe the sink a1.sinks.k1.type = loger

# Use a chanel which bufers events in memory a1.chanels.c1.type = memory

- 17.
- 18.
- 19.
- 20.
- 21.
- 22.


a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

# Bind the source and sink to the chanel a1.sources.r1.chanels = c1 a1.sinks.k1.chanel = c1

复制代码

- b)启动flume agent a1

复制代码

- c)⽣成JSON 格式的POST request

复制代码

- d)在m1的控制台，可以看到以下信息：


1.

rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/post_json.conf -n a1 -Dflume.rot.loger=INFO,console

1.

rot@m1:/home/hadop# curl -X POST -d '[{ "headers" :{"a" : "a1","b" : "b1"},"body" : "idoal.org_body"}]' htp:/localhost: 8

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
- 11.


14/08/101 49 59 INFO node.Aplication: Starting Chanel c1 14/08/101 49 59 INFO instrumentation.MonitoredCounterGroup: Monitored counter group for type: CHANEL, name: c1: Sucesfuly registered new MBean. 14/08/101 49 59 INFO instrumentation.MonitoredCounterGroup: Component type: CHANEL, name: c1 started 14/08/101 49 59 INFO node.Aplication: Starting Sink k1

- 14/08/101 49 59 INFO node.Aplication: Starting Source r1

- 14/08/101 49 59 INFO mortbay.log: Loging to org.slf4j.impl.Log4jLogerAdapter(org.mortbay.log) via org.mortbay.log.Slf4jLog

- 14/08/101 49 59 INFO mortbay.log: jety-6.1.26
- 14/08/101 50  0 INFO mortbay.log: Started SelectChanelConector@0.0.0.0  8


- 14/08/101 50  0 INFO instrumentation.MonitoredCounterGroup: Monitored counter group for type: SOURCE, name: r1: Sucesfuly registered new MBean.


- 14/08/101 50  0 INFO instrumentation.MonitoredCounterGroup: Component type: SOURCE, name: r1 started


14/08/10 12 14 32 INFO sink.LogerSink: Event: { headers:{b=b1, a=a1} body: 69 64 6F 61 6C 6C 2E 6F 72 67 5F 62 6F 64 79 idoal.org_body }

复制代码

6)案例6：Hadop sink

rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0-bin/conf/hdfs_sink.conf

- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.


a1.sources = r1 a1.sinks = k1 a1.chanels = c1

# Describe/configure the source a1.sources.r1.type = syslogtcp a1.sources.r1.port = 5140 a1.sources.r1.host = localhost a1.sources.r1.chanels = c1

# Describe the sink a1.sinks.k1.type = hdfs a1.sinks.k1.chanel = c1 a1.sinks.k1.hdfs.path = hdfs:/m1 9 0/user/flume/syslogtcp a1.sinks.k1.hdfs.filePrefix = Syslog a1.sinks.k1.hdfs.round = true a1.sinks.k1.hdfs.roundValue = 10 a1.sinks.k1.hdfs.roundUnit = minute

# Use a chanel which bufers events in memory a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

# Bind the source and sink to the chanel a1.sources.r1.chanels = c1 a1.sinks.k1.chanel = c1

复制代码

b)启动flume agent a1

1.

rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/hdfs_sink.conf -n a1 -Dflume.rot.loger=INFO,console

复制代码

c)测试产⽣syslog

rot@m1:/home/hadop# echo "helo idoal flume -> hadop testing one" | nc localhost 5140

复制代码

d)在m1的控制台，可以看到以下信息：

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
- 11.
- 12.
- 13.
- 14.


14/08/10 12 20 39 INFO instrumentation.MonitoredCounterGroup: Monitored counter group for type: CHANEL, name: c1: Sucesfuly registered new MBean. 14/08/10 12 20 39 INFO instrumentation.MonitoredCounterGroup: Component type: CHANEL, name: c1 started 14/08/10 12 20 39 INFO node.Aplication: Starting Sink k1 14/08/10 12 20 39 INFO node.Aplication: Starting Source r1

- 14/08/10 12 20 39 INFO instrumentation.MonitoredCounterGroup: Monitored counter group for type: SINK, name: k1: Sucesfuly registered new MBean.

- 14/08/10 12 20 39 INFO instrumentation.MonitoredCounterGroup: Component type: SINK, name: k1 started

- 14/08/10 12 20 39 INFO source.SyslogTcpSource: Syslog TCP Source starting.
- 14/08/10 12 21 46 WARN source.SyslogUtils: Event created from Invalid Syslog data.


- 14/08/10 12 21 49 INFO hdfs.HDFSequenceFile: writeFormat = Writable, UseRawLocalFileSystem = false


- 14/08/10 12 21 49 INFO hdfs.BucketWriter: Creating hdfs:/m1 9 0/user/flume/syslogtcp/Syslog.140764509504.tmp


14/08/10 12  2 20 INFO hdfs.BucketWriter: Closing hdfs:/m1 9 0/user/flume/syslogtcp/Syslog.140764509504.tmp 14/08/10 12  2 20 INFO hdfs.BucketWriter: Close tries incremented 14/08/10 12  2 20 INFO hdfs.BucketWriter: Renaming

fs/m19 0/use/flume/syslogtp/ yslog.140 4509504.tmp to hdfs:/m1 9 0/user/flume/syslogtcp/Syslog.140764509504 14/08/10 12  2 20 INFO hdfs.HDFSEventSink: Writer calback caled.

复制代码

e)在m1上再打开⼀个窗⼝，去hadop上检查⽂件是否⽣成

- 1.
- 2.
- 3.
- 4.
- 5.


rot@m1:/home/hadop# /home/hadop/hadop-2.2.0/bin/hadop fs -ls /user/flume/syslogtcp Found 1 items

-rw-r-r- 3 rot supergroup 15 2014-08-10 12  2 /user/flume/syslogtcp/Syslog.140764509504 rot@m1:/home/hadop# /home/hadop/hadop-2.2.0/bin/hadop fs -cat /user/flume/syslogtcp/Syslog.140764509504 SEQ!org.apache.hadop.io.LongWritable"org.apache.hadop.io.BytesWritable^;>Gv$helo idoal flume -> hadop testing one

复制代码

7)案例7：File Rol Sink

rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0-bin/conf/file_rol.conf

2. 3. 4. 5. 6. 7. 8. 9.

a1.sources = r1 a1.sinks = k1 a1.chanels = c1

# Describe/configure the source a1.sources.r1.type = syslogtcp a1.sources.r1.port = 5 a1.sources.r1.host = localhost a1.sources.r1.chanels = c1

10. 11. 12. 13. 14. 15. 16. 17. 18. 19. 20. 21. 22. 23. 24.

# Describe the sink a1.sinks.k1.type = file_rol a1.sinks.k1.sink.directory = /home/hadop/flume-1.5.0-bin/logs

# Use a chanel which bufers events in memory a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

# Bind the source and sink to the chanel a1.sources.r1.chanels = c1 a1.sinks.k1.chanel = c1

复制代码

b)启动flume agent a1

1.

rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/file_rol.conf -n a1 -Dflume.rot.loger=INFO,console

复制代码

c)测试产⽣log

- 1.
- 2.


rot@m1:/home/hadop# echo "helo idoal.org syslog" | nc localhost 5 rot@m1:/home/hadop# echo "helo idoal.org syslog 2" | nc localhost 5

复制代码

d)查看/home/hadop/flume-1.5.0-bin/logs下是否⽣成⽂件,默认每30秒⽣成⼀个新⽂ 件

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


rot@m1:/home/hadop#l /home/hadop/flume-1.5.0-bin/logs 总⽤量 272 drwxr-xr-x 3 rot rot 4096 Aug 10 12 50 ./ drwxr-xr-x 9 rot rot 4096 Aug 10 10 59./

- -rw-r-r- 1 rot rot 50 Aug 10 12 49 1407646164782-1
- -rw-r-r- 1 rot rot 0 Aug 10 12 49 1407646164782-2
- -rw-r-r- 1 rot rot 0 Aug 10 12 50 1407646164782-3


rot@m1:/home/hadop# cat /home/hadop/flume-1.5.0-bin/logs/1407646164782-1 /home/hadop/flume-1.5.0-bin/logs/1407646164782-2 helo idoal.org syslog helo idoal.org syslog 2

复制代码

8)案例8：Replicating Chanel Selector

Flume⽀持Fan out流从⼀个源到多个通道。有两种模式的Fan out，分别是复制和复⽤。在 复制的情况下，流的事件被发送到所有的配置通道。在复⽤的情况下，事件被发送到可⽤的渠道中的 ⼀个⼦集。Fan out流需要指定源和Fan out通道的规则。

这次我们需要⽤到m1,m2两台机器

a)在m1创建replicating_Chanel_Selector配置⽂件

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
- 11.
- 12.
- 13.


rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0bin/conf/replicating_Chanel_Selector.conf

a1.sources = r1 a1.sinks = k1 k2 a1.chanels = c1 c2

# Describe/configure the source a1.sources.r1.type = syslogtcp a1.sources.r1.port = 5140 a1.sources.r1.host = localhost a1.sources.r1.chanels = c1 c2 a1.sources.r1.selector.type = replicating

- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.


# Describe the sink

- a1.sinks.k1.type = avro

- a1.sinks.k1.chanel = c1

- a1.sinks.k1.hostname = m1

- a1.sinks.k1.port = 5
- a1.sinks.k2.type = avro


- a1.sinks.k2.chanel = c2


- a1.sinks.k2.hostname = m2


- a1.sinks.k2.port = 5


# Use a chanel which bufers events in memory a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

a1.chanels.c2.type = memory a1.chanels.c2.capacity = 1 0 a1.chanels.c2.transactionCapacity = 10

复制代码

b)在m1创建replicating_Chanel_Selector_avro配置⽂件

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
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.


rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0bin/conf/replicating_Chanel_Selector_avro.conf

a1.sources = r1 a1.sinks = k1 a1.chanels = c1

# Describe/configure the source a1.sources.r1.type = avro a1.sources.r1.chanels = c1 a1.sources.r1.bind = 0.0.0.0 a1.sources.r1.port = 5

# Describe the sink a1.sinks.k1.type = loger

# Use a chanel which bufers events in memory

- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.


a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

# Bind the source and sink to the chanel a1.sources.r1.chanels = c1 a1.sinks.k1.chanel = c1

复制代码

c)在m1上将2个配置⽂件复制到m2上⼀份

- 1.
- 2.


rot@m1:/home/hadop/flume-1.5.0-bin# scp -r /home/hadop/flume-1.5.0-

in/conf/replicatin_hanel_elector.conf rot@m2:/home/hadop/flume-1.5.0bin/conf/replicating_Chanel_Selector.conf rot@m1:/home/hadop/flume-1.5.0-bin# scp -r /home/hadop/flume-1.5.0-

in/conf/replicatin_hanel_elector_aro.conf rot@m2:/home/hadop/flume-1.5.0bin/conf/replicating_Chanel_Selector_avro.conf

复制代码

- d)打开4个窗⼝，在m1和m2上同时启动两个flume agent

复制代码

- e)然后在m1或m2的任意⼀台机器上，测试产⽣syslog

复制代码

- f)在m1和m2的sink窗⼝，分别可以看到以下信息,这说明信息得到了同步：


- 1.
- 2.


rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c /home/h o/f me/of/ -f /home/hadop/flume/conf/replicating_Chanel_Selector_avro.conf -n a1 Dflume.rot.loger=INFO,console rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent c/home/hadop/flume/conf/ -f /home/hadop/flume/conf/replicating_Chanel_Selector.conf

-n a1 -Dflume.rot.loger=INFO,console

1.

rot@m1:/home/hadop# echo "helo idoal.org syslog" | nc localhost 5140

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


14/08/10 14 08 18 INFO ipc.NetyServer: Conection to /192.168.1.51 4684 disconected. 14/08/10 14 08 52 INFO ipc.NetyServer: [id: 0x90f8fe1f, /192.168.1.50 35873 => /192.168.1.50  5] OPEN 14/08/10 14 08 52 INFO ipc.NetyServer: [id: 0x90f8fe1f, /192.168.1.50 35873 => /192.168.1.50  5] BOUND: /192.168.1.50  5 14/08/10 14 08 52 INF ipc.NetyServer: [id: 0x90f8fe1f, /192.168.1.50 35873 =>

- /192.168.1.50  5] CONECTED: /192.168.1.50 35873

14/08/10 14 08 59 INFO ipc.NetyServer: [id: 0xd6318635, /192.168.1.51 46858 => /192.168.1.50  5] OPEN 14/08/10 14 08 59 INFO ipc.NetyServer: [id: 0xd6318635, /192.168.1.51 46858 => /192.168.1.50  5] BOUND: /192.168.1.50  5 14/08/10 14 08 59 INF ipc.NetyServer: [id: 0xd6318635, /192.168.1.51 46858 =>

- /192.168.1.50  5] CONECTED: /192.168.1.51 46858


- 8.


14/08/10 14 09 20 INFO sink.LogerSink: Event: { headers:{Severity=0, flume.syslog.status=Invalid, Facility=0} body: 68 65 6C 6C 6F 20 69 64 6F 61 6C 6C 2E 6F 72 67 helo idoal.org }

复制代码

9)案例9：Multiplexing Chanel Selector

a)在m1创建Multiplexing_Chanel_Selector配置⽂件

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
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.


rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0bin/conf/Multiplexing_Chanel_Selector.conf

a1.sources = r1 a1.sinks = k1 k2 a1.chanels = c1 c2

# Describe/configure the source a1.sources.r1.type = org.apache.flume.source.htp.HTPSource a1.sources.r1.port = 5140 a1.sources.r1.chanels = c1 c2 a1.sources.r1.selector.type = multiplexing

a1.sources.r1.selector.header = type #映射允许每个值通道可以重叠。默认值可以包含任意数量的通道。 a1.sources.r1.selector.maping.baidu = c1 a1.sources.r1.selector.maping.ali = c2 a1.sources.r1.selector.default = c1

# Describe the sink

- a1.sinks.k1.type = avro

- a1.sinks.k1.chanel = c1

- a1.sinks.k1.hostname = m1

- a1.sinks.k1.port = 5
- a1.sinks.k2.type = avro


- a1.sinks.k2.chanel = c2


- a1.sinks.k2.hostname = m2


- a1.sinks.k2.port = 5


- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.


# Use a chanel which bufers events in memory a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

a1.chanels.c2.type = memory a1.chanels.c2.capacity = 1 0 a1.chanels.c2.transactionCapacity = 10

复制代码

b)在m1创建Multiplexing_Chanel_Selector_avro配置⽂件

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
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.


rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0bin/conf/Multiplexing_Chanel_Selector_avro.conf

a1.sources = r1 a1.sinks = k1 a1.chanels = c1

# Describe/configure the source a1.sources.r1.type = avro a1.sources.r1.chanels = c1 a1.sources.r1.bind = 0.0.0.0 a1.sources.r1.port = 5

# Describe the sink a1.sinks.k1.type = loger

# Use a chanel which bufers events in memory a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

# Bind the source and sink to the chanel a1.sources.r1.chanels = c1 a1.sinks.k1.chanel = c1

复制代码

c)将2个配置⽂件复制到m2上⼀份

- 1.
- 2.


rot@m1:/home/hadop/flume-1.5.0-bin# scp -r /home/hadop/flume-1.5.0-

in/conf/ ultiplein_hanel_elector.confrot@m2:/home/hadop/flume-1.5.0bin/conf/Multiplexing_Chanel_Selector.conf rot@m1:/home/hadop/flume-1.5.0-bin# scp -r /home/hadop/flume-1.5.0-

in/conf/ ultiplein_hanel_elector_aro.conf rot@m2:/home/hadop/flume-1.5.0bin/conf/Multiplexing_Chanel_Selector_avro.conf

复制代码

d)打开4个窗⼝，在m1和m2上同时启动两个flume agent

- 1.
- 2.


rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/Multiplexing_Chanel_Selector_avro.conf -n a1 Dflume.rot.loger=INFO,console rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/Multiplexing_Chanel_Selector.conf -n a1 Dflume.rot.loger=INFO,console

复制代码

e)然后在m1或m2的任意⼀台机器上，测试产⽣syslog

1.

rot@m1:/home/hadop# curl -X POST -d '[{ "headers" :{"type" : "baidu"},"body" : "idoal_TEST1"}]' & curl -X POST -d '[{ "headers" :{"type" : "ali"},"body" : "idal_TEST2"}]' & curl -X POST -d '[{ "headers" : {"type" : "q"},"body" : "idoal_TEST3"}]'

htp:/localhost:5140 htp:/localhost:5140 htp:/localhost:5140

复制代码

f)在m1的sink窗⼝，可以看到以下信息：

1. 2. 3.

14/08/10 14 32 21 INFO node.Aplication: Starting Sink k1 14/08/10 14 32 21 INFO node.Aplication: Starting Source r1 14/08/10 14 32 21 INFO source.AvroSource: Starting Avro source r1: { bindAdres: 0.0.0.0, port: 5 }. 14/08/10 14 32 21 INFO instrumentation.MonitoredCounterGroup: Monitored counter group for type: SOURCE, name: r1: Sucesfuly registered new MBean. 14/08/10 14 32 21 INFO instrumentation.MonitoredCounterGroup: Component type: SOURCE, name: r1 started 14/08/10 14 32 21 INFO source.AvroSource: Avro source r1 started. 14/08/10 14 32 36 INFO ipc.NetyServer: [id: 0xcf0ea6, /192.168.1.50 35916 => /192.168.1.50  5] OPEN 14/08/10 14 32 36 INF ipc.NetyServer: [id: 0xcf0ea6, /192.168.1.50 35916 => /192.168.1.50  5] BOUND: /192.168.1.50  5 14/08/10 14 32 36 INF ipc.NetyServer: [id: 0xcf0ea6, /192.168.1.50 35916 => /192.168.1.50  5] CONECTED: /192.168.1.50 35916 14/08/10 14 32  4 INFO ipc.NetyServer: [id: 0x432f5468, /192.168.1.51 46945 => /192.168.1.50  5] OPEN

- 4.
- 5.
- 6. 7.


- 8.
- 9.


10.

- 11.
- 12.
- 13.
- 14.


14/08/10 14 32  4 INFO ipc.NetyServer: [id: 0x432f5468, /192.168.1.51 46945 =>

- /192.168.1.50  5] BOUND: /192.168.1.50  5 14/08/10 14 32  4 INF ipc.NetyServer: [id: 0x432f5468, /192.168.1.51 46945 =>

- /192.168.1.50  5] CONECTED: /192.168.1.51 46945

14/08/10 14 34 1 INFO sink.LogerSink: vent: { headers:{type=baidu} body: 69 64 6F 61 6C 6C 5F 54 45 53 54 31 idoal_TEST1 } 14/08/10 14 34 57 INFO sink.LogerSink: Event: { headers:{type=q} body: 69 64 6F 61 6C 6C 5F 54 45 53 54 3 idoal_TEST3 }

14/08/10 14 32 27 INFO node.Aplication: Starting Sink k1 14/08/10 14 32 27 INFO node.Aplication: Starting Source r1 14/08/10 14 32 27 INFO source.AvroSource: Starting Avro source r1: { bindAdres: 0.0.0.0, port: 5 }. 14/08/10 14 32 27 INFO instrumentation.MonitoredCounterGroup: Monitored counter group for type: SOURCE, name: r1: Sucesfuly registered new MBean. 14/08/10 14 32 27 INFO instrumentation.MonitoredCounterGroup: Component type: SOURCE, name: r1 started 14/08/10 14 32 27 INFO source.AvroSource: Avro source r1 started. 14/08/10 14 32 36 INFO ipc.NetyServer: [id: 0x7c2f0aec, /192.168.1.50 38104 =>

- /192.168.1.51  5] OPEN


14/08/10 14 32 36 INFO ipc.NetyServer: [id: 0x7c2f0aec, /192.168.1.50 38104 =>

- /192.168.1.51  5] BOUND: /192.168.1.51  5 14/08/10 14 32 36 INFO ipc.NetyServer: [id: 0x7c2f0aec, /192.168.1.50 38104 =>


复制代码

g)在m2的sink窗⼝，可以看到以下信息：

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
- 11.
- 12.
- 13.


- /192.168.1.51  5] CONECTED: /192.168.1.50 38104

14/08/10 14 32  4 INFO ipc.NetyServer: [id: 0x3d36f53, /192.168.1.51 4859 => /192.168.1.51  5] OPEN 14/08/10 14 32  4 INFO ipc.NetyServer: [id: 0x3d36f53, /192.168.1.51 4859 => /192.168.1.51  5] BOUND: /192.168.1.51  5 14/08/10 14 32  4 INFO ipc.NetyServer: [id: 0x3d36f53, /192.168.1.51 4859 =>

- /192.168.1.51  5] CONECTED: /192.168.1.51 4859


14/08/10 14 34  3 INFO sink.LogerSink: Event: { headers:{type=ali} body: 69 64 6F 61 6C 6C 5F 54 45 53 54 32 idoal_TEST2 }

复制代码

可以看到，根据header中不同的条件分布到不同的chanel上

10)案例10：Flume Sink Procesors

failover的机器是⼀直发送给其中⼀个sink，当这个sink不可⽤的时候，⾃动发送到下⼀个 sink。

a)在m1创建Flume_Sink_Procesors配置⽂件

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
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.


rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0bin/conf/Flume_Sink_Procesors.conf

a1.sources = r1 a1.sinks = k1 k2 a1.chanels = c1 c2

#这个是配置failover的关键，需要有⼀个sink group a1.sinkgroups = g1 a1.sinkgroups.g1.sinks = k1 k2 #处理的类型是failover a1.sinkgroups.g1.procesor.type = failover #优先级，数字越⼤优先级越⾼，每个sink的优先级必须不相同

- a1.sinkgroups.g1.procesor.priority.k1 = 5
- a1.sinkgroups.g1.procesor.priority.k2 = 10 #设置为10秒，当然可以根据你的实际状况更改成更快或者很慢 a1.sinkgroups.g1.procesor.maxpenalty = 1 0


# Describe/configure the source a1.sources.r1.type = syslogtcp a1.sources.r1.port = 5140 a1.sources.r1.chanels = c1 c2 a1.sources.r1.selector.type = replicating

# Describe the sink

- a1.sinks.k1.type = avro

- a1.sinks.k1.chanel = c1

- a1.sinks.k1.hostname = m1

- a1.sinks.k1.port = 5
- a1.sinks.k2.type = avro


- a1.sinks.k2.chanel = c2


- a1.sinks.k2.hostname = m2


- a1.sinks.k2.port = 5


# Use a chanel which bufers events in memory a1.chanels.c1.type = memory

- 38.
- 39.
- 40.
- 41.
- 42.
- 43.


- a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10
- a1.chanels.c2.type = memory a1.chanels.c2.capacity = 1 0 a1.chanels.c2.transactionCapacity = 10


复制代码

- b)在m1创建Flume_Sink_Procesors_avro配置⽂件

复制代码

- c)将2个配置⽂件复制到m2上⼀份


- 1.
- 2. 3. 4. 5. 6. 7. 8. 9.


rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0bin/conf/Flume_Sink_Procesors_avro.conf

a1.sources = r1 a1.sinks = k1 a1.chanels = c1

# Describe/configure the source a1.sources.r1.type = avro a1.sources.r1.chanels = c1 a1.sources.r1.bind = 0.0.0.0 a1.sources.r1.port = 5

10. 11. 12. 13. 14. 15. 16. 17. 18. 19. 20. 21. 22. 23.

# Describe the sink a1.sinks.k1.type = loger

# Use a chanel which bufers events in memory a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

# Bind the source and sink to the chanel a1.sources.r1.chanels = c1 a1.sinks.k1.chanel = c1

1.

rot@m1:/home/hadop/flume-1.5.0-bin# scp -r /home/hadop/flume-1.5.0-

in/conf/lume_in_rocesors.confrot@m2:/home/hadop/flume-1.5.0bin/conf/Flume_Sink_Procesors.conf

2.

rot@m1:/home/hadop/flume-1.5.0-bin# scp -r /home/hadop/flume-1.5.0-

in/conf/lume_in_rocesors_aro.conf rot@m2:/home/hadop/flume-1.5.0bin/conf/Flume_Sink_Procesors_avro.conf

复制代码

- d)打开4个窗⼝，在m1和m2上同时启动两个flume agent

复制代码

- e)然后在m1或m2的任意⼀台机器上，测试产⽣log

复制代码

- f)因为m2的优先级⾼，所以在m2的sink窗⼝，可以看到以下信息，⽽m1没有：

复制代码

- g)这时我们停⽌掉m2机器上的sink(ctrl+c)，再次输出测试数据：

复制代码

- h)可以在m1的sink窗⼝，看到读取到了刚才发送的两条测试数据：


- 1.
- 2.


rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/Flume_Sink_Procesors_avro.conf -n a1 Dflume.rot.loger=INFO,console rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/Flume_Sink_Procesors.conf -n a1 Dflume.rot.loger=INFO,console

1.

- rot@m1:/home/hadop# echo "idoal.org test1 failover" | nc localhost 5140

- 14/08/10 15 02 46 INFO ipc.NetyServer: Conection to /192.168.1.51 48692 disconected.
- 14/08/10 15 03 12 INFO ipc.NetyServer: [id: 0x09a14036, /192.168.1.51 48704 => /192.168.1.51  5] OPEN


14/08/10 15 03 12 INF ipc.NetyServer: [id: 0x09a14036, /192.168.1.51 48704 => /192.168.1.51  5] BOUND: /192.168.1.51  5 14/08/10 15 03 12 INF ipc.NetyServer: [id: 0x09a14036, /192.168.1.51 48704 => /192.168.1.51  5] CONECTED: /192.168.1.51 48704 14/08/10 15 03 26 INFO sink.LogerSink: Event: { headers:{Severity=0, flume.syslog.status=Invalid, Facility=0} body: 69 64 6F 61 6C 6C 2E 6F 72 67 20 74 65 73 74 31 idoal.org test1 }

- rot@m1:/home/hadop# echo "idoal.org test2 failover" | nc localhost 5140


- 1.
- 2.
- 3.
- 4.
- 5.


1.

- 1.
- 2.
- 3.
- 4.
- 5.


- 14/08/10 15 02 46 INFO ipc.NetyServer: Conection to /192.168.1.51 47036 disconected.
- 14/08/10 15 03 12 INFO ipc.NetyServer: [id: 0xbcf79851, /192.168.1.51 47048 => /192.168.1.50  5] OPEN


14/08/10 15 03 12 INF ipc.NetyServer: [id: 0xbcf79851, /192.168.1.51 47048 => /192.168.1.50  5] BOUND: /192.168.1.50  5 14/08/10 15 03 12 INFO ipc.NetyServer: [id: 0xbcf79851, /192.168.1.51 47048 => /192.168.1.50  5] CONECTED: /192.168.1.51 47048 14/08/10 15 07 56 INFO sink.LogerSink: Event: { headers:{Severity=0, flume.syslog.status=Invalid, Facility=0} body: 69 64 6F 61 6C 6C 2E 6F 72 67 20 74 65 73 74 31 idoal.org test1 }

6.

14/08/10 15 07 56 INFO sink.LogerSink: Event: { headers:{Severity=0, flume.syslog.status=Invalid, Facility=0} body: 69 64 6F 61 6C 6C 2E 6F 72 67 20 74 65 73 74 32 idoal.org test2 }

复制代码

- i)我们再在m2的sink窗⼝中，启动sink：

复制代码

- j)输⼊两批测试

：

复制代码

- k)在m2的sink窗⼝，我们可以看到以下信息，因为优先级的关系，log消息会再次落到m2


1.

rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/Flume_Sink_Procesors_avro.conf -n a1 Dflume.rot.loger=INFO,console

数据

1.

rot@m1:/home/hadop# echo "idoal.org test3 failover" | nc localhost 5140 & echo "idoal.org test4 failover" | nc localhost 5140

上：

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
- 11.
- 12.
- 13.
- 14.


14/08/10 15 09 47 INFO node.Aplication: Starting Sink k1 14/08/10 15 09 47 INFO node.Aplication: Starting Source r1 14/08/10 15 09 47 INFO source.AvroSource: Starting Avro source r1: { bindAdres: 0.0.0.0, port: 5 }. 14/08/10 15 09 47 INFO instrumentation.MonitoredCounterGroup: Monitored counter group for type: SOURCE, name: r1: Sucesfuly registered new MBean. 14/08/10 15 09 47 INFO instrumentation.MonitoredCounterGroup: Component type: SOURCE, name: r1 started 14/08/10 15 09 47 INFO source.AvroSource: Avro source r1 started.

- 14/08/10 15 09 54 INFO ipc.NetyServer: [id: 0x9615732, /192.168.1.51 48741 => /192.168.1.51  5] OPEN

- 14/08/10 15 09 54 INFO ipc.NetyServer: [id: 0x9615732, /192.168.1.51 48741 => /192.168.1.51  5] BOUND: /192.168.1.51  5

- 14/08/10 15 09 54 INFO ipc.NetyServer: [id: 0x9615732, /192.168.1.51 48741 => /192.168.1.51  5] CONECTED: /192.168.1.51 48741

14/08/10 15 09 57 INFO sink.LogerSink: Event: { headers:{Severity=0, flume.syslog.status=Invalid, Facility=0} body: 69 64 6F 61 6C 6C 2E 6F 72 67 20 74 65 73 74 32 idoal.org test2 } 14/08/10 15 10 43 INFO ipc.NetyServer: [id: 0x12621f9a, /192.168.1.50 3816 => /192.168.1.51  5] OPEN

- 14/08/10 15 10 43 INFO ipc.NetyServer: [id: 0x12621f9a, /192.168.1.50 3816 => /192.168.1.51  5] BOUND: /192.168.1.51  5


- 14/08/10 15 10 43 INF ipc.NetyServer: [id: 0x12621f9a, /192.168.1.50 3816 => /192.168.1.51  5] CONECTED: /192.168.1.50 3816


- 14/08/10 15 10 43 INFO sink.LogerSink: Event: { headers:{Severity=0, flume.syslo.status=Invalid, Facility=0} body: 69 64 6F 61 6C 6C 2E 6F 72 67 20 74 65 73 74


3 idoal.org test3 }

15.

14/08/10 15 10 43 INFO sink.LogerSink: Event: { headers:{Severity=0, flume.syslo.status=Invalid, Facility=0} body: 69 64 6F 61 6C 6C 2E 6F 72 67 20 74 65 73 74 34 idoal.org test4 }

复制代码

1)案例 1：Load balancing Sink Procesor

load balance type和failover不同的地⽅是，load balance有两个配置，⼀个是轮询，⼀个是 随机。两种情况下如果被选择的sink不可⽤，就会⾃动尝试发送到下⼀个可⽤的sink上⾯。

a)在m1创建Load_balancing_Sink_Procesors配置⽂件

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
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.


rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0bin/conf/Load_balancing_Sink_Procesors.conf

a1.sources = r1 a1.sinks = k1 k2 a1.chanels = c1

#这个是配置Load balancing的关键，需要有⼀个sink group a1.sinkgroups = g1 a1.sinkgroups.g1.sinks = k1 k2 a1.sinkgroups.g1.procesor.type = load_balance a1.sinkgroups.g1.procesor.backof = true a1.sinkgroups.g1.procesor.selector = round_robin

# Describe/configure the source a1.sources.r1.type = syslogtcp a1.sources.r1.port = 5140 a1.sources.r1.chanels = c1

# Describe the sink a1.sinks.k1.type = avro a1.sinks.k1.chanel = c1 a1.sinks.k1.hostname = m1

- a1.sinks.k1.port = 5
- a1.sinks.k2.type = avro


- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.


a1.sinks.k2.chanel = c1 a1.sinks.k2.hostname = m2 a1.sinks.k2.port = 5

# Use a chanel which bufers events in memory a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

复制代码

b)在m1创建Load_balancing_Sink_Procesors_avro配置⽂件

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
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.


rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0bin/conf/Load_balancing_Sink_Procesors_avro.conf

a1.sources = r1 a1.sinks = k1 a1.chanels = c1

# Describe/configure the source a1.sources.r1.type = avro a1.sources.r1.chanels = c1 a1.sources.r1.bind = 0.0.0.0 a1.sources.r1.port = 5

# Describe the sink a1.sinks.k1.type = loger

# Use a chanel which bufers events in memory a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

# Bind the source and sink to the chanel a1.sources.r1.chanels = c1 a1.sinks.k1.chanel = c1

复制代码

c)将2个配置⽂件复制到m2上⼀份

- 1.
- 2.


rot@m1:/home/hadop/flume-1.5.0-bin# scp -r /home/hadop/flume-1.5.0bin/conf/oad_balancin_in_rocesors.confrot@m2:/home/hadop/flume-1.5.0bin/conf/Load_balancing_Sink_Procesors.conf rot@m1:/home/hadop/flume-1.5.0-bin# scp -r /home/hadop/flume-1.5.0bin/conf/oad_balancin_in_rocesors_aro.conf rot@m2:/home/hadop/flume-1.5.0bin/conf/Load_balancing_Sink_Procesors_avro.conf

复制代码

- d)打开4个窗⼝，在m1和m2上同时启动两个flume agent

复制代码

- e)然后在m1或m2的任意⼀台机器上，测试产⽣log，⼀⾏⼀⾏输⼊，输⼊太快，容易落

到⼀台机器上

复制代码

- f)在m1的sink窗⼝，可以看到以下信息：

复制代码

- g)在m2的sink窗⼝，可以看到以下信息：


- 1.
- 2.


rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/Load_balancing_Sink_Procesors_avro.conf -n a1 Dflume.rot.loger=INFO,console

rot@m1:/home/hadop# /home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0-bin/conf/Load_balancing_Sink_Procesors.conf -n a1 Dflume.rot.loger=INFO,console

- 1.
- 2.
- 3.
- 4.


- rot@m1:/home/hadop# echo "idoal.org test1" | nc localhost 5140
- rot@m1:/home/hadop# echo "idoal.org test2" | nc localhost 5140
- rot@m1:/home/hadop# echo "idoal.org test3" | nc localhost 5140
- rot@m1:/home/hadop# echo "idoal.org test4" | nc localhost 5140


- 1.
- 2.


14/08/10 15 35 29 INFO sink.LogerSink: Event: { headers:{Severity=0, flume.syslog.status=Invalid, Facility=0} body: 69 64 6F 61 6C 6C 2E 6F 72 67 20 74 65 73 74 32 idoal.org test2 } 14/08/10 15 35  3 INFO sink.LogerSink: Event: { headers:{Severity=0, flume.syslo.status=Invalid, Facility=0} body: 69 64 6F 61 6C 6C 2E 6F 72 67 20 74 65 73 74 34 idoal.org test4 }

- 1.
- 2.


14/08/10 15 35 27 INFO sink.LogerSink: Event: { headers:{Severity=0, flume.syslog.status=Invalid, Facility=0} body: 69 64 6F 61 6C 6C 2E 6F 72 67 20 74 65 73 74 31 idoal.org test1 } 14/08/10 15 35 29 INFO sink.LogerSink: Event: { headers:{Severity=0, flume.syslo.status=Invalid, Facility=0} body: 69 64 6F 61 6C 6C 2E 6F 72 67 20 74 65 73 74

3 idoal.org test3 }

复制代码

说明轮询模式起到了作⽤。

12)案例12：Hbase sink

- a)在测试之前，请先参考《

》将hbase启动

- b)然后将以下⽂件复制到flume中：

复制代码

- c)确保test_idoal_org表在hbase中已经存在
- d)在m1创建hbase_simple配置⽂件


ubuntu12.04+hadop2.2.0+zokeper3.4.5+hbase0.96.2+hive0.13.1分布式环境部署

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


cp /home/hadop/hbase-0.96.2-hadop2/lib/protobuf-java-2.5.0.jar /home/hadop/flume1.5.0-bin/lib cp /home/hadop/hbase-0.96.2-hadop2/lib/hbase-client-0.96.2-hadop2.jar /home/hadop/flume-1.5.0-bin/lib cp /home/hadop/hbase-0.96.2-hadop2/lib/hbase-comon-0.96.2-hadop2.jar /home/hadop/flume-1.5.0-bin/lib cp /home/hadop/hbase-0.96.2-hadop2/lib/hbase-protocol-0.96.2-hadop2.jar /home/hadop/flume-1.5.0-bin/lib cp /home/hadop/hbase-0.96.2-hadop2/lib/hbase-server-0.96.2-hadop2.jar /home/hadop/flume-1.5.0-bin/lib cp /home/hadop/hbase-0.96.2-hadop2/lib/hbase-hadop2-compat-0.96.2-hadop2.jar /home/hadop/flume-1.5.0-bin/lib cp /home/hadop/hbase-0.96.2-hadop2/lib/hbase-hadop-compat-0.96.2-hadop2.jar /home/hadop/flume-1.5.0-bin/lib @ cp /home/hadop/hbase-0.96.2-hadop2/lib/htrace-core-2.04.jar /home/hadop/flume1.5.0-bin/lib

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
- 11.
- 12.


rot@m1:/home/hadop# vi /home/hadop/flume-1.5.0-bin/conf/hbase_simple.conf

a1.sources = r1 a1.sinks = k1 a1.chanels = c1

# Describe/configure the source a1.sources.r1.type = syslogtcp a1.sources.r1.port = 5140 a1.sources.r1.host = localhost a1.sources.r1.chanels = c1

- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.


# Describe the sink a1.sinks.k1.type = loger a1.sinks.k1.type = hbase a1.sinks.k1.table = test_idoal_org a1.sinks.k1.columnFamily = name a1.sinks.k1.column = idoal a1.sinks.k1.serializer = org.apache.flume.sink.hbase.RegexHbaseEventSerializer a1.sinks.k1.chanel = memoryChanel

# Use a chanel which bufers events in memory a1.chanels.c1.type = memory a1.chanels.c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10

# Bind the source and sink to the chanel a1.sources.r1.chanels = c1 a1.sinks.k1.chanel = c1

复制代码

e)启动flume agent

1.

/home/hadop/flume-1.5.0-bin/bin/flume-ng agent -c . -f /home/hadop/flume-1.5.0bin/conf/hbase_simple.conf -n a1 -Dflume.rot.loger=INFO,console

复制代码

f)测试产⽣syslog

1.

rot@m1:/home/hadop# echo "helo idoal.org from flume" | nc localhost 5140

复制代码

g)这时登录到hbase中，可以发现新

数据

已经插⼊

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


rot@m1:/home/hadop# /home/hadop/hbase-0.96.2-hadop2/bin/hbase shel 2014-08-10 16 09 48,984 INFO [main] Configuration.deprecation: hadop.native.lib is deprecated. Instead, use io.native.lib.available HBase Shel; enter 'help<RETURN>' for list of suported comands. Type "exit<RETURN>" to leave the HBase Shel Version 0.96.2-hadop2, r1581096, Mon Mar 24 16 03 18 PDT 2014

hbase(main): 01 0> list TABLE

- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.


SLF4J: Clas path contains multiple SLF4J bindings. SLF4J: Found binding in [jar:file:/home/hadop/hbase-0.96.2-hadop2/lib/slf4j-log4j12-

- 1.6.4.jar!/org/slf4j/impl/StaticLogerBinder.clas] SLF4J: Found binding in [jar:file:/home/hadop/hadop-
- 2.2.0/share/hadop/comon/lib/slf4j-log4j121.7.5.jar!/org/slf4j/impl/StaticLogerBinder.clas] SLF4J: Sehtp:/ w.slf4j.org/codes.html#multiple_bindings for an explanation. hbase2hive_idoal


hive2hbase_idoal

test_idoal_org

3 row(s) in 2.680 seconds

=> ["hbase2hive_idoal", "hive2hbase_idoal", "test_idoal_org"]

- hbase(main): 02 0> scan "test_idoal_org" ROW COLUMN+CEL

1086 column=name:idoal, timestamp=1406424831473, value=idoalvalue 1 row(s) in 0.050 seconds

- hbase(main): 03 0> scan "test_idoal_org" ROW COLUMN+CEL

1086 column=name:idoal, timestamp=1406424831473, value=idoalvalue 14076584958-XbQCOZrK8-0 column=name:payload, timestamp=1407658498203, value=helo idoal.org from flume

2 row(s) in 0.020 seconds

- hbase(main): 04 0> quit


复制代码

经过这么多flume的例⼦测试，如果你全部做完后，会发现flume的功能真的很强⼤，可以进⾏各种 搭配来完成你想要的⼯作，俗话说师傅领进⻔，修⾏在个⼈，如何能够结合你的产品业务，将flume 更好的应⽤起来，快去动⼿实践吧。

迦壹

# htp:/idoal.org/home.php?mod=s. ;do=blog&id=50
