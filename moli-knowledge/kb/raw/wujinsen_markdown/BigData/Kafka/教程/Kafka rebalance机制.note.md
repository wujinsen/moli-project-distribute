htps:/zhuanlan.zhihu.com/p/348696126

前⾔： 之前项⽬中使⽤了kafka作为消息流转中间件, 也遇到过很多线上的问题, 现记录⼀下kafka的rebalance 机制 原理： Kafka 内部有rebalance机制，在⼀定条件下会触发rebalance：

组成员发⽣变更(1. 新consumer加⼊组、2. 已有consumer主动离开组或已有consumer崩溃了)

![image 1](<Kafka rebalance机制.note_images/imageFile1.png>)

group成员发⽣变化

![image 2](<Kafka rebalance机制.note_images/imageFile2.png>)

consumer 主动leave group

注意：Fetch线程执⾏时间不能过⻓，假如超过max.pol.interval.ms，则Consumer会发出leave group 请求，导致Kafka cordinator进⾏rebalance操作

# Client被动被rebalance：Consumer节点heartbeat时间超过sesion timeout，则会触发Kafka broker进⾏rebalance操作

![image 3](<Kafka rebalance机制.note_images/imageFile3.png>)

consumer⼼跳机制

![image 4](<Kafka rebalance机制.note_images/imageFile4.png>)

Consumer节点heartbeat时间超过sesion timeout

订阅topic数发⽣变更，订阅topic的partition数发⽣变更

![image 5](<Kafka rebalance机制.note_images/imageFile5.png>)

Cordinator 分配partition

线上问题： rebalance时，kafka会重新consumer分配的partition,有些consumer会离线，有些consumer接管其他 consumer的partition, 表⾯的现象就是⼀直有消费者离线

java程序虚拟机内存配置过⼩：在内存数据被撑满以后，jvm会进⾏gc，频繁的gc尤其是ful gc会导 致⼯作线程被⻓时间停⽌，⼼跳线程⽆法⼯作，导致没有在超时时间内发送，被动rebalance

hbase region server压⼒过⼤：之前java程序在接收kafka消息时，会向hbase写⼊数据。案发时 hbase region server存在问题，导致hbase client写⼊时 ，占⽤的cpu过⾼，导致consumer没有在 超时时间内发送⼼跳信息，被动rebalance

SparkStreaming kafka消息积压：查询⽇志如下

Container [pid=6263,containerID=container_1494900155967_0001_02_000001] is running beyondphysical memorylimits. Current usage: 2.5GB of 2.5 GB physical memory used; 4.5 GB of 5.3 GB virtual memory used. Killing container.

解决⽅法：

- 1. 调整driver和executor内存;

- -conf spark.yarn.driver.memoryOverhead
- -conf spark.yarn.executor.memoryOverhead
- -spark.driver.memroy
- -spark.executor.memory


- 2. 开启Spark推测执⾏(仅作为保护机制);

- -conf "spark.speculation=true" \
- -conf "spark.speculation.quantile=0.90" \
- -conf "spark.speculation.interval=10ms" \
- -conf "spark.speculation.multiplier=4" \


- 3. 分析程序内存泄露的原因，后来分析是因为redis穿透引起的问题。


