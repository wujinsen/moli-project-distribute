#指定storm使⽤的zk集群 storm.zokeper.servers:

- - "zk01"
- - "zk02"
- - "zk03"


#指定storm集群中的nimbus节点所在的服务器 nimbus.host: "storm01" #指定nimbus启动JVM最⼤可⽤内存⼤⼩ nimbus.childopts: "-Xmx1024m" #指定supervisor启动JVM最⼤可⽤内存⼤⼩ supervisor.childopts: "-Xmx1024m" #指定supervisor节点上，每个worker启动JVM最⼤可⽤内存⼤⼩ worker.childopts: "-Xmx768m" #指定ui启动JVM最⼤可⽤内存⼤⼩，ui服务⼀般与nimbus同在⼀个节点上。 ui.childopts: "-Xmx768m" #指定supervisor节点上，启动worker时对应的端⼝号，每个端⼝对应槽，每个槽位对应⼀个worker supervisor.slots.ports:

- - 670
- - 6701
- - 6702
- - 6703


