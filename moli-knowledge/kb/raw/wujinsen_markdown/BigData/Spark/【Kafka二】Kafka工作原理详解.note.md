- 1.Kafka有哪些⻆⾊？

- 2.Partition的作⽤是什么？

- 3.Offset的作⽤是什么？

- 4.消息系统有哪两类？

- 5.什么是topic消息⼴播和单播？

- 6.Kafka的元数据和Topic是否都存储在zookeeper？


![image 1](<【Kafka二】Kafka工作原理详解.note_images/imageFile1.png>)

Kafka系统的⻆⾊

Broker ：⼀台kafka服务器就是⼀个broker。⼀个集群由多个broker组成。⼀个broker可以容纳多个 topic

topic： 可以理解为⼀个MQ消息队列的名字

Partition：为了实现扩展性，⼀个⾮常⼤的topic可以分布到多个 broker（即服务器）上，⼀个topic 可以分为多个partition，每个partition是⼀个有序的队列。partition中的每条消息 都会被分配⼀个 有序的id（ofset）。kafka只保证按⼀个partition中的顺序将消息发给consumer，不保证⼀个topic 的整体 （多个partition间）的顺序。也就是说，⼀个topic在集群中可以有多个partition，那么分区 的策略是什么？(消息发送到哪个分区上，有两种基本的策略，⼀是采⽤Key Hash算法，⼀是采⽤ Round Robin算法)

Ofset：kafka的存储⽂件都是按照ofset.kafka来命名，⽤ofset做名字的好处是⽅便查找。例如你 想找位于2049的位置，只要找到2048.kafka的⽂件即可。当然the first ofset就是

0.kafka

Producer ：消息⽣产者，就是向kafka broker发消息的客户端。

Consumer ：消息消费者，向kafka broker取消息的客户端

Consumer Group （CG）：消息系统有两类，⼀是⼴播，⼆是订阅发布。⼴播是把消息发送给所有 的消费者；发布订阅是把消息只发送给订阅者。Kafka通过Consumer Group组合实现了这两种机 制： 实现⼀个topic消息⼴播（发给所有的consumer）和单播（发给任意⼀个consumer）。⼀个 topic可以有多个CG。topic的消息会复制（不是真的复制，是概念上的）到所有的CG，但每个CG 只会把消息发给该CG中的⼀个 consumer（这是实现⼀个Topic多Consumer的关键点：为⼀个 Topic定义⼀个CG，CG下定义多个Consumer）。如果需要实现⼴播，只要每个consumer有⼀个独 ⽴的CG就可以了。要实现单播只要所有的consumer在同⼀个CG。⽤CG还 可以将consumer进⾏ ⾃由的分组⽽不需要多次发送消息到不同的topic。典型的应⽤场景是，多个Consumer来读取⼀个 Topic(理想情况下是⼀个Consumer读取Topic的⼀个Partition）,那么可以让这些Consumer属于同 ⼀个Consumer Group即可实现消息的多Consumer并⾏处理，原理是Kafka将⼀个消息发布出去 后，ConsumerGroup中的Consumers可以通过Round Robin的⽅式进⾏消费(Consumers之间的负 载均衡使⽤Zokeper来实现)

A two server Kafka cluster hosting four partitions (P0-P3) with two consumer groups. Consumer group A has two consumer instances and group B has four.

# 总结：Topic、Partition和Replica的关系：

如上图，⼀个Topic有四个Partition，每个Partition两个replication。

Zookeeper在Kakfa中扮演的⻆⾊Kafka将元数据信息保存在Zookeeper中，但是发送给Topic本身的数 据是不会发到Zk上的，否则Zk就疯了。

kafka使⽤zokeper来实现动态的集群扩展，不需要更改客户端（producer和consumer）的配 置。broker会在zokeper注册并保持相关的元数据（topic，partition信息等）更新。

⽽客户端会在zokeper上注册相关的watcher。⼀旦zokeper发⽣变化，客户端能及时感知并作 出相应调整。这样就保证了添加或去除broker时，各broker间仍能⾃动实现负载均衡。这⾥的客户 端指的是Kafka的消息⽣产端(Producer)和消息消费端(Consumer)

Broker端使⽤zokeper来注册broker信息,以及监测partition leader存活性.

Consumer端使⽤zokeper⽤来注册consumer信息,其中包括consumer消费的partition列表等,同 时也⽤来发现broker列表,并和partition leader建⽴socket连接,并获取消息.

Zoker和Producer没有建⽴关系，只和Brokers、Consumers建⽴关系以实现负载均衡，即同⼀个 Consumer Group中的Consumers可以实现负载均衡

问题：

- 1.Topic有多个Partition，那么消息分配到某个Partition的依据是什么？Key Hash或者Round Robin

- 2. 如何查看⼀个Topic有多少个Partition？ 使⽤kakfa-topic.sh --list topic topicName --zookeeper zookeeper.servers.list


Zookeeper记录的信息如下列出了在 ⼀⽂中操作Kafka时，Zk上 记录的信息(可⻅，Zookeeper上没有记录Producer的信息，因为Producer是瞬态的，可以发送后关闭， ⽆需直接等待)

htp:/bit129.iteye.com/blog/2174791

- 1.
- 2.


[zk: localhost:2181(CONECTED) 0] ls / [admin, consumers, config, brokers]复制代码

admin：

- 1.
- 2.
- 3.
- 4.


- [zk: localhost:2181(CONECTED) 15] ls /admin [delete_topics]
- [zk: localhost:2181(CONECTED) 16] ls /admin/delete_topics []复制代码


consumers：（consumers底下是consumer group，consumer group之下有owner，owner是topic的名 字）

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


- [zk: localhost:2181(CONECTED) 7] ls /consumers [test-consumer-group]
- [zk: localhost:2181(CONECTED) 8] ls /consumers/test-consumer-group [owners, ids]
- [zk: localhost:2181(CONECTED) 9] ls /consumers/test-consumer-group/owners [test]
- [zk: localhost:2181(CONECTED) 10] ls /consumers/test-consumer-group/ids []复制代码config:


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


[zk: localhost:2181(CONECTED)1] ls /config [topics, changes]

- [zk: localhost:2181(CONECTED) 12] ls /config/topics [test]
- [zk: localhost:2181(CONECTED) 13] ls /config/changes []复制代码


brokers:

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


- [zk: localhost:2181(CONECTED) 3] ls /brokers [topics, ids]
- [zk: localhost:2181(CONECTED) 4] ls /brokers/topics [test]
- [zk: localhost:2181(CONECTED) 5] ls /brokers/ids []复制代码


