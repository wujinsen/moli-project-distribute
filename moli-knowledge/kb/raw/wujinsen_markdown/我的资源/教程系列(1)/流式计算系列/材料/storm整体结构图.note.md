![image 1](<storm整体结构图.note_images/imageFile1.png>)

客户端提交拓扑到nimbus。 Nimbus针对该拓扑建⽴本地的⽬录根据topology的配置计算task，分配task，在zokeper上建⽴ asignments节点存储task和supervisor机器节点中woker的对应关系； 在zokeper上创建taskbeats节点来监控task的⼼跳；启动topology。 Supervisor去zokeper上获取分配的tasks，启动多个woker进⾏，每个woker⽣成task，⼀个task⼀ 个线程；根据topology信息初始化建⽴task之间的连接;Task和Task之间是通过zeroMQ管理的；后整个 拓扑运⾏起来。

![image 2](<storm整体结构图.note_images/imageFile2.png>)

