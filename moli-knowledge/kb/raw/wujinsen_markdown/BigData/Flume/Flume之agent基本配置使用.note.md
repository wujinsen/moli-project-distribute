htps:/blog.csdn.net/ q_4128793/article/details/ 8920359

mac ⽆法使⽤telnet

Flume之agent基本配置使⽤

⼀、agent组成

agent由source、chanel、sink三部分组成

source：数据源头，主要负责接收数据，将数据转换成事件，交由chanel进⾏缓冲拦截处理，⼀般有 netcat、实时⽇志收集（exec）、批量监控、序列source、压⼒source，还有avro source等等

chanel：中间缓冲拦截处理机制，主要有内存通道，⽂件通道，内存溢出通道等等

sink：数据输出机制，对抓取的chanel通道缓存的事件进⾏拉取，并且进⾏下⼀步存放或者跃点操 作，跃点相当于从当前的agent传递给下⼀个agent，sink⼀般包含hive sink，hbase sink，hdfs sink等 等

更多详细信息请查看Flume官⽅⽂档

⼆、demo测试配置⽂件编辑

准备数据源为netcat，通道为内存通道，sink为hdfs sink

在flume/bin下新建⼀个nc_memory_hdfs.conf⽂件，编辑内容：

# 这⾥的agent名称定义为agent，其他名称同理 agent.sources = r1 agent.chanels = c1 agent.sinks = k1

# sources # 定义资源类型为nc，绑定主机为localhost，暴露端⼝为 9 agent.sources.r1.type=netcat agent.sources.r1.bind=localhost agent.sources.r1.port= 9

# sink # 这⾥的hdfs.path为hdfs⽂件路径，启动flume之前需要在hdfs⽂件系统上新 建/usr/centos/flume/sinkData # hdfs dfs -mkdir . agent.sinks.k1.type = hdfs agent.sinks.k1.hdfs.path=/usr/centos/flume/sinkData/%y-%m-%d/%H/%M/%S agent.sinks.k1.hdfs.filePrefix=eventsagent.sinks.k1.hdfs.round = true # 每20秒钟创建⼀次⽂件夹（有源头数据来的情况下才创建，没有数据过来不会创建） agent.sinks.k1.hdfs.roundValue = 20 agent.sinks.k1.hdfs.roundUnit=second # 每⼗秒钟在当前⽂件夹下创建⼀个⽂件（同样需要有源数据过来） agent.sinks.k1.hdfs.rolInterval=10 agent.sinks.k1.hdfs.rolSize=1024 agent.sinks.k1.hdfs.rolCount=10 agent.sinks.k1.hdfs.useLocalTimeStamp=true

# chanels agent.chanels.c1.type=memory

# bind_al agent.sources.r1.chanels=c1 agent.sinks.k1.chanel=c1

三、执⾏启动flume

flume-ng agent -f ./conf/nc_memory_hdfs.conf -n agent

该指令在flume的安装⽬下执⾏的，在任何路径都可以执⾏，只要对应编辑的配置⽂件的路径就⾏

- -f ：对应配置⽂件的路径
- -n：对应配置⽂件中agent的名称


四、客户端连接服务产⽣源数据

nc localhost 9 这⾥ip和端⼝需要对应配置⽂件中source的bind和port的值

成功连接后编辑任意测试内容进⾏回⻋，然后查看hdfs⽂件系统产⽣的sink输出数据，数据为⼆进制数 据 使⽤：hdfs dfs -lsr /usr/centos/sinkData列出产⽣⽂件⽬录结构，使⽤-text指令可以查看产⽣的⽂件 内容

