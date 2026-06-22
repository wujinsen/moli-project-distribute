其实kafka提供了⼀系列的⼯具可以查看offset.

如:

MacPro:kafka-0.8.2.2-src ajian$ kafka-consumer-offset-checker.sh --zookeeper localhost:2181

--topic my-replicated-topic --group console-consumer-11758 Group Topic Pid Offset logSize Lag Owner console-consumer-11758 my-replicated-topic 0 3715607 3715608 1 console-consumer-11758_MacPro-1449666659508-e5c5524d-0

其实consumer的offset都是在zookeeper上⾯可以找到对应的数字. 具体查看kafka在zk中的数据结 构:

https://cwiki.apache.org/confluence/display/KAFKA/Kafka+data+structures+in+Zookeeper

但是⼀直没有办法找到logSize的数据从何⽽来(不⽤脚本⼯具), 这⾥⾯的logsize其实就是topic的最 新offset或者说是message的⻓度.

延迟就是: logSize - Offset=Lag

本⼈的⽬的就是通过原理获取这些数据进⾏监控,不想使⽤sh⼯具的原因1.格式还需要转 2.速度慢 3.依赖环境 4.需要的数据过多或者过少.

请熟悉kafka的同学看看.

补充下⼀个开源⼯具KafkaOffsetMonitor的情况(本⼈对java不是⾮常的熟悉 看着有些费⼒)

https://github.com/quantifind/KafkaOffsetMonitor/blob/d9c2a64d233043dcdc65d4d4974a044f664 9615b/src/main/scala/com/quantifind/kafka/OffsetGetter.scala#L130

