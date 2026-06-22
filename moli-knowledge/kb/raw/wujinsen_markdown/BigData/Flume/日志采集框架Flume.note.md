htps:/ w.cnblogs.com/winter-shadow/p/1 4572.html

# ⽇志采集框架Flume

## Flume介绍

概述

Flume是⼀个分布式、可靠、和⾼可⽤的海量⽇志采集、聚合和传输的系统。 Flume可以采集⽂件，socket数据包、⽂件、⽂件夹、kafka等各种形式源数据，⼜可以将采集到的数 据(下沉sink)输出到HDFS、hbase、hive、kafka等众多外部存储系统中

运⾏机制

Flume分布式系统最核⼼的⻆⾊是agent，flume采集系统就是由⼀个个agent所连接起来⽽成 每⼀个agent相当于⼀个数据传递员，内部有三个组件：

Source：采集组件，⽤于跟数据源对接，获取数据 Sink：下沉组件，⽤于往下⼀级agent传递数据或者往最终存储系统传递数据 Channel：传输通道组件，⽤于从source将数据传递到sink

采集系统结构图 简单结构

复杂结构

多级agent之间串联 Flume实战案例

安装部署

第⼀步：下载解压修改配置⽂件

Flume的安装⾮常简单，只需要解压即可，当然，前提是已有hadoop环境 # 上传安装包到数据源所在节点上 这⾥采⽤在第三台机器来进⾏安装 软件⽬录 => flume-ng1.6.0-cdh5.14.0.tar.gz tar -zxvf flume-ng-1.6.0-cdh5.14.0.tar.gz -C ../servers/ cd ../servers/apache-flume-1.6.0-cdh5.14.0-bin/conf/ cp flume-env.sh.template flume-env.sh vim flume-env.sh #只添加⼀个java环境就可以了 export JAVA_HOME=/export/servers/jdk1.8.0_141

第⼆步：开发配置⽂件

# 根据数据采集的需求配置采集⽅案，描述在配置⽂件中(⽂件名可任意⾃定义) # 配置我们的⽹络收集的配置⽂件 # 在flume的conf⽬录下新建⼀个配置⽂件（采集⽅案） vim /export/servers/apache-flume-1.6.0-cdh5.14.0-bin/conf/netcatlogger.conf

<table>
  <tr>
    <th># 定义这个 a1.sourcesagent中各组件的名字= r1 a1.sinks = k1 a1.channels = c1<br><br># 描述和配置source组件：r1 a1.sources.r1.type = netcat a1.sources.r1.bind = 192.168.52.120 a1.sources.r1.port = 44444<br><br># a1.sinks.k1.typ描述和配置sink组件：e= loggerk1<br><br># 描述和配置channel组件，此处使⽤是内存缓存的⽅式 a1.channels.c1.type = memory a1.channels.c1.capacity = 1000 a1.channels.c1.transactionCapacity =100<br><br># 描述和配置source channel sink之间的连接关系 a1.sources.r1.channels = c1 a1.sinks.k1.cha nel = c1</th>
  </tr>
</table>


n

启动配置⽂件

指定采集⽅案配置⽂件，在相应的节点上启动flume agent cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin bin/flume-ng agent -c conf -f conf/netcat-logger.conf -n a1 Dflume.root.logger=INFO,console # -c conf 指定flume⾃身的配置⽂件所在⽬录 # -f conf/netcat-logger.conf 指定所描述的采集⽅案 # -n a1 指定这个agent的名字

安装telent准备测试

在node02上安装telnet客户端⽤于模拟数据的发送 yum -y install telnet telnet node03 44444 # 使⽤telnet模拟数据发送

采集案例

采集⽬录到HDFS

某服务器的特定⽬录下会不断产⽣新的⽂件，每当有新⽂件出现，就需要把⽂件采集到HDFS中去 根据需求，⾸先定义以下3⼤要素

数据源组件，即source -- 监控⽂件⽬录：spooldirspooldir特性： 监视⼀个⽬录，只要⽬录中出现新⽂件，就会采集⽂件中的内容 采集完成的⽂件，会被agent⾃动添加⼀个后缀：COMPLETED 所监视的⽬录中不允许重复出现相同⽂件名的⽂件

下沉组件，妈sink -- HDFS⽂件系统：hdfs sink 通道组件，妈channel -- 可⽤file channel 也可以⽤内存 memory channel

flume配置⽂件开发

mkdir -p /export/servers/dirfile vim spooldir.conf

# 定义agent的组件名字 a1.sources=sr1 a1.sinks=sk1 a1.channels=scn1

# 配置数据源source a1.sources.sr1.type=spooldir a1.sources.sr1.spoolDir=/export/servers/dirfile a1.sources.sr1.fileHeader=true

# 配置下沉组件sink a1.sinks.sk1.type=hdfs a1.sinks.sk1.channel=scn1 # hdfs⽬录路径 a1.sinks.sk1.hdfs.path=hdfs://node01:8020/spooldir/files/%y-%m-

%d/%H%M/ # 写⼊hdfs的⽂件名前缀 可以使⽤flume提供的⽇期及%{host}表达式 a1.sinks.sk1.hdfs.filePrefix=events# 表示到了需要触发的时间时，是否要更新⽂件夹，true:表示要 a1.sinks.sk1.hdfs.round=true # 表示每隔value分钟改变⼀次(在0~24之间) a1.sinks.sk1.hdfs.roundValue=10 # 切换⽂件的时候的时间单位是分钟 a1.sinks.sk1.hdfs.roundUnit=minute # 多久时间后close hdfs⽂件。单位是秒，默认30秒。设置为0的话表示不根据时间close

hdfs⽂件 a1.sinks.sk1.hdfs.rollInterval=3 # ⽂件⼤⼩超过⼀定值后，close⽂件。默认值1024，单位是字节。设置为0的话表示不基于⽂

件⼤⼩,134217728表 示128m，决定了多⼤块可以切⼀个⽂件。 a1.sinks.sk1.hdfs.rollSize=134217728 # 写⼊了多少个事件后close⽂件。默认值是10个。设置为0的话表示不基于事件个数 a1.sinks.sk1.hdfs.rollCount=0 # 批次数，HDFS Sink每次从Channel中拿的事件个数。默认值100 a1.sinks.sk1.hdfs.batchSize=100

# 使⽤本地时间戳 a1.sinks.sk1.hdfs.useLocalTimeStamp=true #⽣成的⽂件类型默认是 Sequencefile,可⽤DataStream则为普通⽂本 a1.sinks.sk1.hdfs.fileType=DataStream

# 配置通道channel a1.channels.scn1.type=memory a1.channels.scn1.capacity=1000 a1.channels.scn1.transactionCapacity=100

bin/flume-ng agent -c ./conf/ -f ./conf/spooldir.conf -n a1 Dflume.root.logger=INFO,console # 运⾏flume

采集⽂件到HDFS

⽐如业务系统使⽤Log4j⽣成的⽇志，⽇志内容不断增加，需要把追加到⽇志⽂件中的数据实时采集到 hdfs

根据需求，⾸先定义以下3⼤要素 采集源，即source——监控⽂件内容更新 : exec ‘tail -F file’ 下沉⽬标，即sink——HDFS⽂件系统 : hdfs sink Source和sink之间的传递通道——channel，可⽤filechannel 也可以⽤ 内存channel

定义flume的配置⽂件

vim tail-file.conf agent1.sources = source1 agent1.sinks = sink1 agent1.channels = channel1

# Describe/configure tail -F source1 agent1.sources.source1.type = exec agent1.sources.source1.command = tail -F

/export/servers/taillogs/access_log agent1.sources.source1.channels = channel1

#configure host for source #agent1.sources.source1.interceptors = i1 #agent1.sources.source1.interceptors.i1.type = host #agent1.sources.source1.interceptors.i1.hostHeader = hostname

# Describe sink1 agent1.sinks.sink1.type = hdfs #a1.sinks.k1.channel = c1 agent1.sinks.sink1.hdfs.path = hdfs://node01:8020/weblog/flume-

collection/%y-%m-%d/%H-%M agent1.sinks.sink1.hdfs.filePrefix = access_log agent1.sinks.sink1.hdfs.maxOpenFiles = 5000 agent1.sinks.sink1.hdfs.batchSize= 100 agent1.sinks.sink1.hdfs.fileType = DataStream agent1.sinks.sink1.hdfs.writeFormat =Text agent1.sinks.sink1.hdfs.rollSize = 102400 agent1.sinks.sink1.hdfs.rollCount = 1000000 agent1.sinks.sink1.hdfs.rollInterval = 60 agent1.sinks.sink1.hdfs.round = true agent1.sinks.sink1.hdfs.roundValue = 10 agent1.sinks.sink1.hdfs.roundUnit = minute agent1.sinks.sink1.hdfs.useLocalTimeStamp = true

# Use a channel which buffers events in memory agent1.channels.channel1.type = memory

agent1.channels.channel1.keep-alive = 120 agent1.channels.channel1.capacity = 500000 agent1.channels.channel1.transactionCapacity = 600

# Bind the source and sink to the channel agent1.sources.source1.channels = channel1 agent1.sinks.sink1.channel = channel1

bin/flume-ng agent -c conf -f conf/tail-file.conf -n agent1 Dflume.root.logger=INFO,console #启动Flume # 开发shell脚本定时追加⽂件内容 mkdir -p /export/servers/shells/ cd /export/servers/shells/

vim tail-file.sh #!/bin/bash while true do

date >> /export/servers/taillogs/access_log;

两个agent级联

第⼀个agent负责收集⽂件当中的数据，通过⽹络发送到第⼆个agent当中去，第⼆个agent负责接收第 ⼀个agent发送的数据，并将数据保存到hdfs上⾯去

第⼀步：node02安装flume

cd /export/servers scp -r apache-flume-1.6.0-cdh5.14.0-bin/ node02:$PWD

第⼆步：node02配置flume配置⽂件

vim tail-avro-avro-logger.conf ################## # Name the components on this agent a1.sources = r1 a1.sinks = k1 a1.channels = c1 # Describe/configure the source a1.sources.r1.type = exec a1.sources.r1.command = tail -F /export/servers/taillogs/access_log a1.sources.r1.channels = c1 # Describe the sink ##sink端的avro是⼀个数据发送者 a1.sinks = k1 a1.sinks.k1.type = avro a1.sinks.k1.channel = c1 a1.sinks.k1.hostname = 192.168.52.120 a1.sinks.k1.port = 4141 a1.sinks.k1.batch-size = 10 # Use a channel which buffers events in memory a1.channels.c1.type = memory a1.channels.c1.capacity = 1000 a1.channels.c1.transactionCapacity = 100 # Bind the source and sink to the channel a1.sources.r1.channels = c1 a1.sinks.k1.channel = c1

第三步：node02开发脚本⽂件往⽂件写⼊数据

# 直接把node03的脚本拷⻉⾄node02 cd /export/servers scp -r shells/ taillogs/ node02:$PWD

第四步node03开发Flume配置⽂件

# 在node03机器上开发flume的配置⽂件 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin/conf vim avro-hdfs.conf #配置如下 # Name the components on this agent a1.sources = r1 a1.sinks = k1 a1.channels = c1 # Describe/configure the source ##source中的avro组件是⼀个接收者服务 a1.sources.r1.type = avro a1.sources.r1.channels = c1 a1.sources.r1.bind = 192.168.52.120 a1.sources.r1.port = 4141 # Describe the sink a1.sinks.k1.type = hdfs a1.sinks.k1.hdfs.path = hdfs://node01:8020/avro/hdfs/%y-%m-%d/%H%M/ a1.sinks.k1.hdfs.filePrefix = eventsa1.sinks.k1.hdfs.round = true a1.sinks.k1.hdfs.roundValue = 10 a1.sinks.k1.hdfs.roundUnit = minute a1.sinks.k1.hdfs.rollInterval = 3 a1.sinks.k1.hdfs.rollSize = 20 a1.sinks.k1.hdfs.rollCount = 5 a1.sinks.k1.hdfs.batchSize = 1 a1.sinks.k1.hdfs.useLocalTimeStamp = true #⽣成的⽂件类型，默认是Sequencefile，可⽤DataStream，则为普通⽂本 a1.sinks.k1.hdfs.fileType = DataStream # Use a channel which buffers events in memory a1.channels.c1.type = memory a1.channels.c1.capacity = 1000 a1.channels.c1.transactionCapacity = 100

# Bind the source and sink to the channel a1.sources.r1.channels = c1 a1.sinks.k1.channel = c1

第五步顺序启动

# node03机器启动flume进程 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin bin/flume-ng agent -c conf -f conf/avro-hdfs.conf -n a1 Dflume.root.logger=INFO,console

- # node02机器启动flume进程 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin/ bin/flume-ng agent -c conf -f conf/tail-avro-avro-logger.conf -n a1 Dflume.root.logger=INFO,console


- # node02机器启shell脚本⽣成⽂件 cd /export/servers/shells


sh tail-file.sh

更多source和sink组件

http://archive.cloudera.com/cdh5/cdh/5/flume-ng-1.6.0-cdh5.14.0/FlumeUserGuide.html

参⻅： ⾼可⽤Flume-NG配置案例failover

⻆⾊分配

<table>
  <tr>
    <th>名称</th>
    <th>HOST</th>
    <th>⻆⾊</th>
  </tr>
  <tr>
    <td>Agent1</td>
    <td>node01</td>
    <td>Web Server</td>
  </tr>
  <tr>
    <td>Colector1</td>
    <td>node02</td>
    <td>AgentMstr1</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


Colector2 node03 AgentMstr2 node01安装配置flume

- # node03机器执⾏以下命令 cd /export/servers scp -r apache-flume-1.6.0-cdh5.14.0-bin/ node01:$PWD scp -r shells/ taillogs/ node01:$PWD


- # node01机器配置agent的配置⽂件 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin/conf vim agent.conf #配置如下


#agent1 name agent1.channels = c1 agent1.sources = r1 agent1.sinks = k1 k2 # ##set gruop agent1.sinkgroups = g1 # ##set channel agent1.channels.c1.type = memory agent1.channels.c1.capacity = 1000 agent1.channels.c1.transactionCapacity = 100 # agent1.sources.r1.channels = c1 agent1.sources.r1.type = exec agent1.sources.r1.command = tail -F /export/servers/taillogs/access_log # agent1.sources.r1.interceptors = i1 i2 agent1.sources.r1.interceptors.i1.type = static agent1.sources.r1.interceptors.i1.key = Type agent1.sources.r1.interceptors.i1.value = LOGIN agent1.sources.r1.interceptors.i2.type = timestamp #

- ## set sink1


- agent1.sinks.k1.channel = c1


- agent1.sinks.k1.type = avro


- agent1.sinks.k1.hostname = node02


- agent1.sinks.k1.port = 52020


#

- ## set sink2


- agent1.sinks.k2.channel = c1


- agent1.sinks.k2.type = avro


- agent1.sinks.k2.hostname = node03


- agent1.sinks.k2.port = 52020 # ##set sink group agent1.sinkgroups.g1.sinks = k1 k2 # ##set failover agent1.sinkgroups.g1.processor.type = failover


- agent1.sinkgroups.g1.processor.priority.k1 = 10

- agent1.sinkgroups.g1.processor.priority.k2 = 1 agent1.sinkgroups.g1.processor.maxpenalty = 10000 #


node02与node03配置flumecollection

- # node02机器修改配置⽂件 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin/conf vim collector.conf #set Agent name a1.sources = r1 a1.channels = c1 a1.sinks = k1 # ##set channel a1.channels.c1.type = memory a1.channels.c1.capacity = 1000 a1.channels.c1.transactionCapacity = 100 # ## other node,nna to nns a1.sources.r1.type = avro

- a1.sources.r1.bind = node02 a1.sources.r1.port = 52020 a1.sources.r1.interceptors = i1 a1.sources.r1.interceptors.i1.type = static a1.sources.r1.interceptors.i1.key = Collector


- a1.sources.r1.interceptors.i1.value = node02 a1.sources.r1.channels = c1 # ##set sink to hdfs a1.sinks.k1.type=hdfs a1.sinks.k1.hdfs.path= hdfs://node01:8020/flume/failover/ a1.sinks.k1.hdfs.fileType=DataStream a1.sinks.k1.hdfs.writeFormat=TEXT a1.sinks.k1.hdfs.rollInterval=10 a1.sinks.k1.channel=c1 a1.sinks.k1.hdfs.filePrefix=%Y-%m-%d


- # node03机器修改配置⽂件 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin/conf vim collector.conf #set Agent name a1.sources = r1


a1.channels = c1 a1.sinks = k1 # ##set channel a1.channels.c1.type = memory a1.channels.c1.capacity = 1000 a1.channels.c1.transactionCapacity = 100 # ## other node,nna to nns a1.sources.r1.type = avro

- a1.sources.r1.bind = node03 a1.sources.r1.port = 52020 a1.sources.r1.interceptors = i1 a1.sources.r1.interceptors.i1.type = static a1.sources.r1.interceptors.i1.key = Collector


- a1.sources.r1.interceptors.i1.value = node03 a1.sources.r1.channels = c1 # ##set sink to hdfs a1.sinks.k1.type=hdfs a1.sinks.k1.hdfs.path= hdfs://node01:8020/flume/failover/ a1.sinks.k1.hdfs.fileType=DataStream a1.sinks.k1.hdfs.writeFormat=TEXT a1.sinks.k1.hdfs.rollInterval=10 a1.sinks.k1.channel=c1 a1.sinks.k1.hdfs.filePrefix=%Y-%m-%d


顺序启动命令

- # node03机器上⾯启动flume cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin bin/flume-ng agent -n a1 -c conf -f conf/collector.conf Dflume.root.logger=DEBUG,console


# node02机器上⾯启动flume cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin bin/flume-ng agent -n a1 -c conf -f conf/collector.conf Dflume.root.logger=DEBUG,console

# node01机器上⾯启动flume cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin bin/flume-ng agent -n agent1 -c conf -f conf/agent.conf Dflume.root.logger=DEBUG,console

# node01机器启动⽂件产⽣脚本 cd /export/servers/shells sh tail-file.sh

FAILOVER测试 Collector1宕机，Collector2获取优先上传权限 重启Collector1服务，Collector1重新获得优先上传的权限

Flume的负载均衡 load balancer 负载均衡是⽤于解决⼀台机器(⼀个进程)⽆法解决所有请求⽽产⽣的⼀种算法。Load balancing Sink Processor 能够实现 load balance 功能，如下图Agent1 是⼀个路由节点，负责将 Channel 暂存的 Event 均衡到对应的多个 Sink组件上，⽽每个 Sink 组件分别连接到⼀个独⽴的 Agent 上，示例配置，如下所示： 在此处我们通过三台机器来进⾏模拟flume的负载均衡 三台机器规划如下：

- node01：采集数据，发送到node02和node03机器上去

- node02：接收node01的部分数据

- node03：接收node01的部分数据


第⼀步：开发node01服务器的flume配置

- # node01服务器配置： cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin/conf vim load_banlancer_client.conf


#agent name a1.channels = c1 a1.sources = r1 a1.sinks = k1 k2

#set gruop a1.sinkgroups = g1

#set channel a1.channels.c1.type = memory a1.channels.c1.capacity = 1000 a1.channels.c1.transactionCapacity = 100

a1.sources.r1.channels = c1 a1.sources.r1.type = exec a1.sources.r1.command = tail -F /export/servers/taillogs/access_log

- # set sink1

- a1.sinks.k1.channel = c1

- a1.sinks.k1.type = avro

- a1.sinks.k1.hostname = node02

- a1.sinks.k1.port = 52020

# set sink2

- a1.sinks.k2.channel = c1


- a1.sinks.k2.type = avro


- a1.sinks.k2.hostname = node03


- a1.sinks.k2.port = 52020




#set sink group a1.sinkgroups.g1.sinks = k1 k2

#set failover

a1.sinkgroups.g1.processor.type = load_balance a1.sinkgroups.g1.processor.backoff = true a1.sinkgroups.g1.processor.selector = round_robin a1.sinkgroups.g1.processor.selector.maxTimeOut=10000

第⼆步：开发node02服务器的flume配置

- # node02服务器配置： cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin/conf vim load_banlancer_server.conf


# Name the components on this agent a1.sources = r1 a1.sinks = k1 a1.channels = c1

# Describe/configure the source a1.sources.r1.type = avro a1.sources.r1.channels = c1

- a1.sources.r1.bind = node02 a1.sources.r1.port = 52020


# Describe the sink a1.sinks.k1.type = logger

# Use a channel which buffers events in memory a1.channels.c1.type = memory a1.channels.c1.capacity = 1000 a1.channels.c1.transactionCapacity = 100

# Bind the source and sink to the channel a1.sources.r1.channels = c1 a1.sinks.k1.channel = c1

第三步：开发node03服务器flume配置

- # node03服务器配置 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin/conf vim load_banlancer_server.conf


# Name the components on this agent a1.sources = r1 a1.sinks = k1 a1.channels = c1

# Describe/configure the source a1.sources.r1.type = avro a1.sources.r1.channels = c1

- a1.sources.r1.bind = node03 a1.sources.r1.port = 52020


# Describe the sink a1.sinks.k1.type = logger

# Use a channel which buffers events in memory a1.channels.c1.type = memory a1.channels.c1.capacity = 1000 a1.channels.c1.transactionCapacity = 100

# Bind the source and sink to the channel a1.sources.r1.channels = c1 a1.sinks.k1.channel = c1

第四步：准备启动flume服务

# 启动node03的flume服务 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin bin/flume-ng agent -n a1 -c conf -f conf/load_banlancer_server.conf Dflume.root.logger=DEBUG,console

# 启动node02的flume服务 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin bin/flume-ng agent -n a1 -c conf -f conf/load_banlancer_server.conf Dflume.root.logger=DEBUG,console

# 启动node01的flume服务 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin bin/flume-ng agent -n a1 -c conf -f conf/load_banlancer_client.conf Dflume.root.logger=DEBUG,console

# node01服务器运⾏脚本产⽣数据 cd /export/servers/shells sh tail-file.sh Flume案例⼀ 把A、B 机器中的access.log、nginx.log、web.log 采集汇总到C机器上然后统⼀收集到hdfs中。 但是在hdfs中要求的⽬录为： /source/logs/access/20180101/** /source/logs/nginx/20180101/** /source/logs/web/20180101/**

采集端配置⽂件开发

# node01与node02服务器开发flume的配置⽂件 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin/conf vim exec_source_avro_sink.conf

# Name the components on this agent a1.sources = r1 r2 r3 a1.sinks = k1 a1.channels = c1

# Describe/configure the source

- a1.sources.r1.type = exec

- a1.sources.r1.command = tail -F /export/servers/taillogs/access.log

- a1.sources.r1.interceptors = i1

- a1.sources.r1.interceptors.i1.type = static ## static拦截器的功能就是往采集到的数据的header中插⼊⾃⼰定## 义的key-value对

- a1.sources.r1.interceptors.i1.key = type

- a1.sources.r1.interceptors.i1.value = access

- a1.sources.r2.type = exec

- a1.sources.r2.command = tail -F /export/servers/taillogs/nginx.log a1.sources.r2.interceptors = i2 a1.sources.r2.interceptors.i2.type = static a1.sources.r2.interceptors.i2.key = type a1.sources.r2.interceptors.i2.value = nginx

- a1.sources.r3.type = exec a1.sources.r3.command = tail -F /export/servers/taillogs/web.log a1.sources.r3.interceptors = i3 a1.sources.r3.interceptors.i3.type = static a1.sources.r3.interceptors.i3.key = type


- a1.sources.r3.interceptors.i3.value = web












# Describe the sink a1.sinks.k1.type = avro a1.sinks.k1.hostname = node03 a1.sinks.k1.port = 41414

# Use a channel which buffers events in memory a1.channels.c1.type = memory a1.channels.c1.capacity = 20000 a1.channels.c1.transactionCapacity = 10000

# Bind the source and sink to the channel

- a1.sources.r1.channels = c1

- a1.sources.r2.channels = c1

- a1.sources.r3.channels = c1 a1.sinks.k1.channel = c1


服务端配置⽂件开发

# 在node03上⾯开发flume配置⽂件 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin/conf vim avro_source_hdfs_sink.conf

a1.sources = r1 a1.sinks = k1 a1.channels = c1 #定义source a1.sources.r1.type = avro a1.sources.r1.bind = 192.168.52.120 a1.sources.r1.port =41414

#添加时间拦截器 a1.sources.r1.interceptors = i1 a1.sources.r1.interceptors.i1.type = org.apache.flume.interceptor.TimestampInterceptor$Builder

#定义channels a1.channels.c1.type = memory a1.channels.c1.capacity = 20000 a1.channels.c1.transactionCapacity = 10000

#定义sink a1.sinks.k1.type = hdfs a1.sinks.k1.hdfs.path=hdfs://192.168.52.100:8020/source/logs/% {type}/%Y%m%d a1.sinks.k1.hdfs.filePrefix =events a1.sinks.k1.hdfs.fileType = DataStream a1.sinks.k1.hdfs.writeFormat = Text #时间类型 a1.sinks.k1.hdfs.useLocalTimeStamp = true #⽣成的⽂件不按条数⽣成 a1.sinks.k1.hdfs.rollCount = 0 #⽣成的⽂件按时间⽣成 a1.sinks.k1.hdfs.rollInterval = 30 #⽣成的⽂件按⼤⼩⽣成 a1.sinks.k1.hdfs.rollSize = 10485760

#批量写⼊hdfs的个数 a1.sinks.k1.hdfs.batchSize = 10000 #flume操作hdfs的线程数（包括新建，写⼊等） a1.sinks.k1.hdfs.threadsPoolSize=10 #操作hdfs超时时间 a1.sinks.k1.hdfs.callTimeout=30000

#组装source、channel、sink a1.sources.r1.channels = c1 a1.sinks.k1.channel = c1

采集端⽂件⽣成脚本

cd /export/servers/shells vim server.sh

#!/bin/bash while true do

date >> /export/servers/taillogs/access.log; date >> /export/servers/taillogs/web.log; date >> /export/servers/taillogs/nginx.log;

sleep 0.5; done

顺序启动服务

# node03启动flume实现数据收集 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin bin/flume-ng agent -c conf -f conf/avro_source_hdfs_sink.conf -name a1 Dflume.root.logger=DEBUG,console

# node01与node02启动flume实现数据监控 cd /export/servers/apache-flume-1.6.0-cdh5.14.0-bin bin/flume-ng agent -c conf -f conf/exec_source_avro_sink.conf -name a1 Dflume.root.logger=DEBUG,console

# node01与node02启动⽣成⽂件脚本 cd /export/servers/shells sh server.sh

