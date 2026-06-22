htps:/ w.aboutyun.com/forum.php?mod=viewthread&tid=1282

问题导读

- 1.Kafka独特设计在什么地⽅？
- 2.Kafka如何搭建及创建topic、发送消息、消费消息？
- 3.如何书写Kafka程序？
- 4.数据传输的事务定义有哪三种？
- 5.Kafka判断⼀个节点是否活着有哪两个条件？
- 6.producer是否直接将数据发送到broker的leader(主节点)？
- 7.Kafa consumer是否可以消费指定分区消息？
- 8.Kafka消息是采⽤Pul模式，还是Push模式？
- 9.Procuder API有哪两种？
- 10.Kafka存储在硬盘上的消息格式是什么？


# ⼀、基本概念

## 介绍

Kafka是⼀个分布式的、可分区的、可复制的消息系统。它提供了普通消息系统的功能，但具有⾃⼰独 特的设计。

这个独特的设计是什么样的呢？

⾸先让我们看⼏个基本的消息系统术语： Kafka将消息以topic为单位进⾏归纳。 将向Kafka topic发布消息的程序成为producers. 将预订topics并消费消息的程序成为consumer. Kafka以集群的⽅式运⾏，可以由⼀个或多个服务组成，每个服务叫做⼀个broker. producers通过⽹络将消息发送到Kafka集群，集群向消费者提供消息，如下图所示：

客户端和服务端通过TCP协议通信。Kafka提供了Java客户端，并且对多种语⾔都提供了⽀持。

## Topics和Logs

先来看⼀下Kafka提供的⼀个抽象概念:topic. ⼀个topic是对⼀组消息的归纳。对每个topic，Kafka 对它的⽇志进⾏了分区，如下图所示：

每个分区都由⼀系列有序的、不可变的消息组成，这些消息被连续的追加到分区中。分区中的每个消 息都有⼀个连续的序列号叫做ofset,⽤来在分区中唯⼀的标识这个消息。 在⼀个可配置的时间段内，Kafka集群保留所有发布的消息，不管这些消息有没有被消费。⽐如，如果 消息的保存策略被设置为2天，那么在⼀个消息被发布的两天时间内，它都是可以被消费的。之后它将 被丢弃以释放空间。Kafka的性能是和数据量⽆关的常量级的，所以保留太多的数据并不是问题。

实际上每个consumer唯⼀需要维护的数据是消息在⽇志中的位置，也就是ofset.这个ofset有 consumer来维护：⼀般情况下随着consumer不断的读取消息，这ofset的值不断增加，但其实 consumer可以以任意的顺序读取消息，⽐如它可以将ofset设置成为⼀个旧的值来重读之前的消息。

以上特点的结合，使Kafka consumers⾮常的轻量级：它们可以在不对集群和其他consumer造成影响 的情况下读取消息。你可以使⽤命令⾏来"tail"消息⽽不会对其他正在消费消息的consumer造成影响。

将⽇志分区可以达到以下⽬的：⾸先这使得每个⽇志的数量不会太⼤，可以在单个服务上保存。另外 每个分区可以单独发布和消费，为并发操作topic提供了⼀种可能。

## 分布式

每个分区在Kafka集群的若⼲服务中都有副本，这样这些持有副本的服务可以共同处理数据和请求，副 本数量是可以配置的。副本使Kafka具备了容错能⼒。 每个分区都由⼀个服务器作为“leader”，零或若⼲服务器作为“folowers”,leader负责处理消息的读和 写，folowers则去复制leader.如果leader down了，folowers中的⼀台则会⾃动成为leader。集群中的 每个服务都会同时扮演两个⻆⾊：作为它所持有的⼀部分分区的leader，同时作为其他分区的 folowers，这样集群就会据有较好的负载均衡。

## Producers

Producer将消息发布到它指定的topic中,并负责决定发布到哪个分区。通常简单的由负载均衡机制随机 选择分区，但也可以通过特定的分区函数选择分区。使⽤的更多的是第⼆种。

## Consumers

发布消息通常有两种模式：队列模式（queuing）和发布-订阅模式(publish-subscribe)。队列模式 中，consumers可以同时从服务端读取消息，每个消息只被其中⼀个consumer读到；发布-订阅模式 中消息被⼴播到所有的consumer中。Consumers可以加⼊⼀个consumer 组，共同竞争⼀个topic， topic中的消息将被分发到组中的⼀个成员中。同⼀组中的consumer可以在不同的程序中，也可以在不 同的机器上。如果所有的consumer都在⼀个组中，这就成为了传统的队列模式，在各consumer中实 现负载均衡。如果所有的consumer都不在不同的组中，这就成为了发布-订阅模式，所有的消息都被 分发到所有的consumer中。更常⻅的是，每个topic都有若⼲数量的consumer组，每个组都是⼀个逻 辑上的“订阅者”，为了容错和更好的稳定性，每个组由若⼲consumer组成。这其实就是⼀个发布-订阅 模式，只不过订阅者是个组⽽不是单个consumer。

由两个机器组成的集群拥有4个分区 (P0-P3) 2个consumer组. A组有两个consumerB组有4个

相⽐传统的消息系统，Kafka可以很好的保证有序性。 传统的队列在服务器上保存有序的消息，如果多个consumers同时从这个服务器消费消息，服务器就 会以消息存储的顺序向consumer分发消息。虽然服务器按顺序发布消息，但是消息是被异步的分发到 各consumer上，所以当消息到达时可能已经失去了原来的顺序，这意味着并发消费将导致顺序错乱。 为了避免故障，这样的消息系统通常使⽤“专⽤consumer”的概念，其实就是只允许⼀个消费者消费消 息，当然这就意味着失去了并发性。

在这⽅⾯Kafka做的更好，通过分区的概念，Kafka可以在多个consumer组并发的情况下提供较好的有 序性和负载均衡。将每个分区分只分发给⼀个consumer组，这样⼀个分区就只被这个组的⼀个 consumer消费，就可以顺序的消费这个分区的消息。因为有多个分区，依然可以在多个consumer组 之间进⾏负载均衡。注意consumer组的数量不能多于分区的数量，也就是有多少分区就允许多少并发 消费。

Kafka只能保证⼀个分区之内消息的有序性，在不同的分区之间是不可以的，这已经可以满⾜⼤部分应 ⽤的需求。如果需要topic中所有消息的有序性，那就只能让这个topic只有⼀个分区，当然也就只有⼀ 个consumer组消费它。

#

# ⼆、环境搭建

- Step 1: 下载Kafka

点击下载最新的版本并解压.

复制代码

- Step 2: 启动服务


- 1.
- 2.


> tar -xzf kafka_2.9.2-0.8.1.1.tgz > cd kafka_2.9.2-0.8.1.1

Kafka⽤到了Zokeper，所有⾸先启动Zokper，下⾯简单的启⽤⼀个单实例的Zokeper服务。可 以在命令的结尾加个&符号，这样就可以启动后离开控制台。

- 1.
- 2.
- 3.


> bin/zookeeper-server-start.sh config/zookeeper.properties & [2013-04-22 15:01:37,495] INFO Reading configuration from: config/zookeeper.properties (org.apache.zookeeper.server.quorum.QuorumPeerConfig)

...

复制代码

现在启动Kafka:

- 1.
- 2.
- 3.
- 4.


> bin/kafka-server-start.sh config/server.properties [2013-04-22 15:01:47,028] INFO Verifying properties (kafka.utils.VerifiableProperties) [2013-04-22 15:01:47,051] INFO Property socket.send.buffer.bytes is overridden to 1048576 (kafka.utils.VerifiableProperties)

...

复制代码

- Step 3: 创建 topic

创建⼀个叫做“test”的topic，它只有⼀个分区，⼀个副本。

复制代码

可以通过list命令查看创建的topic:

复制代码

除了⼿动创建topic，还可以配置broker让它⾃动创建topic.

- Step 4:发送消息.

Kafka 使⽤⼀个简单的命令⾏producer，从⽂件中或者从标准输⼊中读取消息并发送到服务端。默认 的每条命令将发送⼀条消息。

运⾏producer并在控制台中输⼀些消息，这些消息将被发送到服务端：

复制代码

ctrl+c可以退出发送。

- Step 5: 启动consumer


1.

> bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 -partitions 1 --topic test

- 1.
- 2.


> bin/kafka-topics.sh --list --zookeeper localhost:2181 test

- 1.
- 2.


> bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test This is a messageThis is another message

Kafka also has a comand line consumer that wil dump out mesages to standard output. Kafka也有⼀个命令⾏consumer可以读取消息并输出到标准输出：

- 1.
- 2.


> bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic test --frombeginning This is a message

- config/server-1.properties: broker.id=1 port=9093

- log.dir=/tmp/kafka-logs-1

config/server-2.properties: broker.id=2 port=9094

- log.dir=/tmp/kafka-logs-2


- 3.


This is another message

复制代码

你在⼀个终端中运⾏consumer命令⾏，另⼀个终端中运⾏producer命令⾏，就可以在⼀个终端输⼊消 息，另⼀个终端读取消息。 这两个命令都有⾃⼰的可选参数，可以在运⾏的时候不加任何参数可以看到帮助信息。

- Step 6: 搭建⼀个多个broker的集群


刚才只是启动了单个broker，现在启动有3个broker组成的集群，这些broker节点也都是在本机上的： ⾸先为每个节点编写配置⽂件：

- 1.
- 2.


- > cp config/server.properties config/server-1.properties

- > cp config/server.properties config/server-2.properties


复 制 代 码

在拷⻉出的新⽂件中添加以下参数：

- 1.
- 2.
- 3.
- 4.


复制代码

- 1.
- 2.
- 3.
- 4.


复制代码

broker.id在集群中唯⼀的标注⼀个节点，因为在同⼀个机器上，所以必须制定不同的端⼝和⽇志⽂ 件，避免数据被覆盖。

We already have Zokeper and our single node started, so we just ned to start the two new nodes:

刚才已经启动可Zokeper和⼀个节点，现在启动另外两个节点：

- 1.
- 2.
- 3.
- 4.


- > bin/kafka-server-start.sh config/server-1.properties &

...

- > bin/kafka-server-start.sh config/server-2.properties &


...

复制代码

创建⼀个拥有3个副本的topic:

1.

> bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 -partitions 1 --topic my-replicated-topic

复制代码

现在我们搭建了⼀个集群，怎么知道每个节点的信息呢？运⾏“"describe topics”命令就可以了：

1.

> bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic my-replicated-topic

复制代码

- 1.
- 2.


Topic:my-replicated-topic PartitionCount:1 ReplicationFactor:3 Configs:

Topic: my-replicated-topic Partition: 0 Leader: 1 Replicas: 1,2,0 Isr: 1,2,0

复制代码

下⾯解释⼀下这些输出。第⼀⾏是对所有分区的⼀个描述，然后每个分区都会对应⼀⾏，因为我们只 有⼀个分区所以下⾯就只加了⼀⾏。 leader：负责处理消息的读和写，leader是从所有节点中随机选择的. replicas：列出了所有的副本节点，不管节点是否在服务中. isr：是正在服务中的节点. 在我们的例⼦中，节点1是作为leader运⾏。 向topic发送消息：

1.

> bin/kafka-console-producer.sh --broker-list localhost:9092 --topic my-replicatedtopic

复制代码

- 1.
- 2.


... my test message 1my test message 2^C

复制代码

消费这些消息：

1.

> bin/kafka-console-consumer.sh --zookeeper localhost:2181 --from-beginning --topic myreplicated-topic

复制代码

- 1.
- 2.
- 3.
- 4.


...

- my test message 1

- my test message 2 ^C


复制代码

测试⼀下容错能⼒.Broker 1作为leader运⾏，现在我们kil掉它：

- 1.
- 2.


> ps | grep server-1.properties7564 ttys002 0:15.91 /System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home/bin/java... > kill -9 7564

复制代码

另外⼀个节点被选做了leader,node 1 不再出现在 in-sync 副本列表中：

- 1.
- 2.
- 3.


> bin/kafka-topics.sh --describe --zookeeper localhost:218192 --topic my-replicatedtopic Topic:my-replicated-topic PartitionCount:1 ReplicationFactor:3

Configs:

Topic: my-replicated-topic Partition: 0 Leader: 2 Replicas: 1,2,0 Isr: 2,0

复制代码

虽然最初负责续写消息的leader down掉了，但之前的消息还是可以消费的：

- 1.
- 2.
- 3.
- 4.


> bin/kafka-console-consumer.sh --zookeeper localhost:2181 --from-beginning --topic myreplicated-topic

...

- my test message 1

- my test message 2


复制代码

看来Kafka的容错机制还是不错的。

#

# 三、搭建Kafka开发环境

我们搭建了kafka的服务器，并可以使⽤Kafka的命令⾏⼯具创建topic，发送和接收消息。下⾯我们来 搭建kafka的开发环境。 添加依赖

搭建开发环境需要引⼊kafka的jar包，⼀种⽅式是将Kafka安装包中lib下的jar包加⼊到项⽬的claspath 中，这种⽐较简单了。不过我们使⽤另⼀种更加流⾏的⽅式：使⽤maven管理jar包依赖。 创建好maven项⽬后，在pom.xml中添加以下依赖：

- 1.
- 2.
- 3.
- 4.
- 5.


<dependency>

<groupId> org.apache.kafka</groupId > <artifactId> kafka_2.10</artifactId > <version> 0.8.0</ version>

</dependency>

复制代码

添加依赖后你会发现有两个jar包的依赖找不到。没关系我都帮你想好了，点击这⾥下载这两个jar包， 解压后你有两种选择，第⼀种是使⽤mvn的instal命令将jar包安装到本地仓库，另⼀种是直接将解压后 的⽂件夹拷⻉到mvn本地仓库的com⽂件夹下，⽐如我的本地仓库是d:\mvn,完成后我的⽬录结构是这 样的：

配置程序

⾸先是⼀个充当配置⽂件作⽤的接⼝,配置了Kafka的各种连接参数：

1.

package com.sohu.kafkademon;

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


public interface KafkaProperties {

final static String zkConnect = "10.22.10.139:2181"; final static String groupId = "group1"; final static String topic = "topic1"; final static String kafkaServerURL = "10.22.10.139"; final static int kafkaServerPort = 9092; final static int kafkaProducerBufferSize = 64 * 1024; final static int connectionTimeOut = 20000; final static int reconnectInterval = 10000; final static String topic2 = "topic2"; final static String topic3 = "topic3"; final static String clientId = "SimpleConsumerDemoClient";

}

复制代码

producer

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


package com.sohu.kafkademon; import java.util.Properties; import kafka.producer.KeyedMessage; import kafka.producer.ProducerConfig; /**

- * @author leicui bourne_cui@163.com

- */


public class KafkaProducer extends Thread {

private final kafka.javaapi.producer.Producer<Integer, String> producer; private final String topic; private final Properties props = new Properties(); public KafkaProducer(String topic) {

props.put("serializer.class", "kafka.serializer.StringEncoder"); props.put("metadata.broker.list", "10.22.10.139:9092"); producer = new kafka.javaapi.producer.Producer<Integer, String>(new

ProducerConfig(props));

this.topic = topic; }

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


@Override public void run() {

int messageNo = 1; while (true) {

String messageStr = new String("Message_" + messageNo); System.out.println("Send:" + messageStr); producer.send(new KeyedMessage<Integer, String>(topic, messageStr)); messageNo++; try {

sleep(3000);

} catch (InterruptedException e) { // TODO Auto-generated catch block e.printStackTrace();

} }

} }

复制代码

consumer

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


package com.sohu.kafkademon; import java.util.HashMap; import java.util.List; import java.util.Map; import java.util.Properties; import kafka.consumer.ConsumerConfig; import kafka.consumer.ConsumerIterator; import kafka.consumer.KafkaStream; import kafka.javaapi.consumer.ConsumerConnector; /**

- * @author leicui bourne_cui@163.com

- */


public class KafkaConsumer extends Thread {

private final ConsumerConnector consumer; private final String topic; public KafkaConsumer(String topic)

{

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
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.


consumer = kafka.consumer.Consumer.createJavaConsumerConnector(

createConsumerConfig()); this.topic = topic;

} private static ConsumerConfig createConsumerConfig() {

Properties props = new Properties(); props.put("zookeeper.connect", KafkaProperties.zkConnect); props.put("group.id", KafkaProperties.groupId); props.put("zookeeper.session.timeout.ms", "40000"); props.put("zookeeper.sync.time.ms", "200"); props.put("auto.commit.interval.ms", "1000"); return new ConsumerConfig(props);

} @Override public void run() {

Map<String, Integer> topicCountMap = new HashMap<String, Integer>(); topicCountMap.put(topic, new Integer(1)); Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap =

consumer.createMessageStreams(topicCountMap); KafkaStream<byte[], byte[]> stream = consumerMap.get(topic).get(0); ConsumerIterator<byte[], byte[]> it = stream.iterator(); while (it.hasNext()) {

System.out.println("receive：" + new String(it.next().message())); try {

sleep(3000); } catch (InterruptedException e) {

e.printStackTrace(); }

} }

}

复制代码

简单的发送接收

运⾏下⾯这个程序，就可以进⾏简单的发送接收消息了：

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


package com.sohu.kafkademon; /**

- * @author leicui bourne_cui@163.com

- */


public class KafkaConsumerProducerDemo {

public static void main(String[] args) {

KafkaProducer producerThread = new KafkaProducer(KafkaProperties.topic); producerThread.start(); KafkaConsumer consumerThread = new KafkaConsumer(KafkaProperties.topic); consumerThread.start();

} }

复制代码

⾼级别的consumer

下⾯是⽐较负载的发送接收的程序：

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


package com.sohu.kafkademon; import java.util.HashMap; import java.util.List; import java.util.Map; import java.util.Properties; import kafka.consumer.ConsumerConfig; import kafka.consumer.ConsumerIterator; import kafka.consumer.KafkaStream; import kafka.javaapi.consumer.ConsumerConnector; /**

- * @author leicui bourne_cui@163.com

- */


public class KafkaConsumer extends Thread {

private final ConsumerConnector consumer; private final String topic; public KafkaConsumer(String topic) {

consumer = kafka.consumer.Consumer.createJavaConsumerConnector(

createConsumerConfig()); this.topic = topic;

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
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.


} private static ConsumerConfig createConsumerConfig() {

Properties props = new Properties(); props.put("zookeeper.connect", KafkaProperties.zkConnect); props.put("group.id", KafkaProperties.groupId); props.put("zookeeper.session.timeout.ms", "40000"); props.put("zookeeper.sync.time.ms", "200"); props.put("auto.commit.interval.ms", "1000"); return new ConsumerConfig(props);

} @Override public void run() {

Map<String, Integer> topicCountMap = new HashMap<String, Integer>(); topicCountMap.put(topic, new Integer(1)); Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap =

consumer.createMessageStreams(topicCountMap); KafkaStream<byte[], byte[]> stream = consumerMap.get(topic).get(0); ConsumerIterator<byte[], byte[]> it = stream.iterator(); while (it.hasNext()) {

System.out.println("receive：" + new String(it.next().message())); try {

sleep(3000); } catch (InterruptedException e) {

e.printStackTrace(); }

} }

}

复制代码

#

四、数据持久化

不要畏惧⽂件系统!

Kafka⼤量依赖⽂件系统去存储和缓存消息。对于硬盘有个传统的观念是硬盘总是很慢，这使很多⼈怀 疑基于⽂件系统的架构能否提供优异的性能。实际上硬盘的快慢完全取决于使⽤它的⽅式。设计良好 的硬盘架构可以和内存⼀样快。

在6块720转的SATA RAID-5磁盘阵列的线性写速度差不多是60MB/s，但是随即写的速度却是 10k/s，差了差不多6 0倍。现代的操作系统都对次做了⼤量的优化，使⽤了 read-ahead 和 writebehind的技巧，读取的时候成块的预读取数据，写的时候将各种微⼩琐碎的逻辑写⼊组织合并成⼀次 较⼤的物理写⼊。对此的深⼊讨论可以查看这⾥，它们发现线性的访问磁盘，很多时候⽐随机的内存 访问快得多。

为了提⾼性能，现代操作系统往往使⽤内存作为磁盘的缓存，现代操作系统乐于把所有空闲内存⽤作 磁盘缓存，虽然这可能在缓存回收和重新分配时牺牲⼀些性能。所有的磁盘读写操作都会经过这个缓 存，这不太可能被绕开除⾮直接使⽤I/O。所以虽然每个程序都在⾃⼰的线程⾥只缓存了⼀份数据，但 在操作系统的缓存⾥还有⼀份，这等于存了两份数据。

另外再来讨论⼀下JVM,以下两个事实是众所周知的：

&#826;Java对象占⽤空间是⾮常⼤的，差不多是要存储的数据的两倍甚⾄更⾼。

&#826;随着堆中数据量的增加，垃圾回收回变的越来越困难。

基于以上分析，如果把数据缓存在内存⾥，因为需要存储两份，不得不使⽤两倍的内存空间，Kafka基 于JVM，⼜不得不将空间再次加倍,再加上要避免GC带来的性能影响，在⼀个32G内存的机器上，不得 不使⽤到28-30G的内存空间。并且当系统重启的时候，⼜必须要将数据刷到内存中（ 10GB 内存差不 多要⽤10分钟），就算使⽤冷刷新（不是⼀次性刷进内存，⽽是在使⽤数据的时候没有就刷到内存） 也会导致最初的时候新能⾮常慢。但是使⽤⽂件系统，即使系统重启了，也不需要刷新数据。使⽤⽂ 件系统也简化了维护数据⼀致性的逻辑。

所以与传统的将数据缓存在内存中然后刷到硬盘的设计不同，Kafka直接将数据写到了⽂件系统的⽇志 中。

常量时间的操作效率

在⼤多数的消息系统中，数据持久化的机制往往是为每个cosumer提供⼀个B树或者其他的随机读写的 数据结构。B树当然是很棒的，但是也带了⼀些代价：⽐如B树的复杂度是O(log N)，O(log N)通常被 认为就是常量复杂度了，但对于硬盘操作来说并⾮如此。磁盘进⾏⼀次搜索需要10ms，每个硬盘在同 ⼀时间只能进⾏⼀次搜索，这样并发处理就成了问题。虽然存储系统使⽤缓存进⾏了⼤量优化，但是 对于树结构的性能的观察结果却表明，它的性能往往随着数据的增⻓⽽线性下降，数据增⻓⼀倍，速 度就会降低⼀倍。

直观的讲，对于主要⽤于⽇志处理的消息系统，数据的持久化可以简单的通过将数据追加到⽂件中实 现，读的时候从⽂件中读就好了。这样做的好处是读和写都是 O(1) 的，并且读操作不会阻塞写操作和 其他操作。这样带来的性能优势是很明显的，因为性能和数据的⼤⼩没有关系了。

既然可以使⽤⼏乎没有容量限制（相对于内存来说）的硬盘空间建⽴消息系统，就可以在没有性能损 失的情况下提供⼀些⼀般消息系统不具备的特性。⽐如，⼀般的消息系统都是在消息被消费后⽴即删 除，Kafka却可以将消息保存⼀段时间（⽐如⼀星期），这给consumer提供了很好的机动性和灵活 性，这点在今后的⽂章中会有详述。

#

## 五、消息传输的事务定义

之前讨论了consumer和producer是怎么⼯作的，现在来讨论⼀下数据传输⽅⾯。数据传输的事务定义 通常有以下三种级别：

最多⼀次: 消息不会被重复发送，最多被传输⼀次，但也有可能⼀次不传输。

最少⼀次: 消息不会被漏发送，最少被传输⼀次，但也有可能被重复传输.

精确的⼀次（Exactly once）: 不会漏传输也不会重复传输,每个消息都传输被⼀次⽽且仅仅被传输 ⼀次，这是⼤家所期望的。

⼤多数消息系统声称可以做到“精确的⼀次”，但是仔细阅读它们的的⽂档可以看到⾥⾯存在误导，⽐ 如没有说明当consumer或producer失败时怎么样，或者当有多个consumer并⾏时怎么样，或写⼊硬 盘的数据丢失时⼜会怎么样。kafka的做法要更先进⼀些。当发布消息时，Kafka有⼀个“comited”的 概念，⼀旦消息被提交了，只要消息被写⼊的分区的所在的副本broker是活动的，数据就不会丢失。 关于副本的活动的概念，下节⽂档会讨论。现在假设broker是不会down的。

如果producer发布消息时发⽣了⽹络错误，但⼜不确定实在提交之前发⽣的还是提交之后发⽣的，这 种情况虽然不常⻅，但是必须考虑进去，现在Kafka版本还没有解决这个问题，将来的版本正在努⼒尝 试解决。

并不是所有的情况都需要“精确的⼀次”这样⾼的级别，Kafka允许producer灵活的指定级别。⽐如 producer可以指定必须等待消息被提交的通知，或者完全的异步发送消息⽽不等待任何通知，或者仅 仅等待leader声明它拿到了消息（folowers没有必要）。

现在从consumer的⽅⾯考虑这个问题，所有的副本都有相同的⽇志⽂件和相同的ofset，consumer维 护⾃⼰消费的消息的ofset，如果consumer不会崩溃当然可以在内存中保存这个值，当然谁也不能保 证这点。如果consumer崩溃了，会有另外⼀个consumer接着消费消息，它需要从⼀个合适的ofset继 续处理。这种情况下可以有以下选择：

consumer可以先读取消息，然后将ofset写⼊⽇志⽂件中，然后再处理消息。这存在⼀种可能就是 在存储ofset后还没处理消息就crash了，新的consumer继续从这个ofset处理，那么就会有些消息 永远不会被处理，这就是上⾯说的“最多⼀次”。

consumer可以先读取消息，处理消息，最后记录ofset，当然如果在记录ofset之前就crash了，新 的consumer会重复的消费⼀些消息，这就是上⾯说的“最少⼀次”。

“精确⼀次”可以通过将提交分为两个阶段来解决：保存了ofset后提交⼀次，消息处理成功之后再提 交⼀次。但是还有个更简单的做法：将消息的ofset和消息被处理后的结果保存在⼀起。⽐如⽤ Hadop ETL处理消息时，将处理后的结果和ofset同时保存在HDFS中，这样就能保证消息和ofser 同时被处理了。

#

## 六、性能优化

Kafka在提⾼效率⽅⾯做了很⼤努⼒。Kafka的⼀个主要使⽤场景是处理⽹站活动⽇志，吞吐量是⾮常 ⼤的，每个⻚⾯都会产⽣好多次写操作。读⽅⾯，假设每个消息只被消费⼀次，读的量的也是很⼤ 的，Kafka也尽量使读的操作更轻量化。

我们之前讨论了磁盘的性能问题，线性读写的情况下影响磁盘性能问题⼤约有两个⽅⾯：太多的琐碎 的I/O操作和太多的字节拷⻉。I/O问题发⽣在客户端和服务端之间，也发⽣在服务端内部的持久化的操 作中。

消息集（mesage set）

为了避免这些问题，Kafka建⽴了“消息集（mesage set）”的概念，将消息组织到⼀起，作为处理的 单位。以消息集为单位处理消息，⽐以单个的消息为单位处理，会提升不少性能。Producer把消息集 ⼀块发送给服务端，⽽不是⼀条条的发送；服务端把消息集⼀次性的追加到⽇志⽂件中，这样减少了 琐碎的I/O操作。consumer也可以⼀次性的请求⼀个消息集。

另外⼀个性能优化是在字节拷⻉⽅⾯。在低负载的情况下这不是问题，但是在⾼负载的情况下它的影 响还是很⼤的。为了避免这个问题，Kafka使⽤了标准的⼆进制消息格式，这个格式可以在 producer,broker和producer之间共享⽽⽆需做任何改动。

zero copy Broker维护的消息⽇志仅仅是⼀些⽬录⽂件，消息集以固定队的格式写⼊到⽇志⽂件中，这个格式 producer和consumer是共享的，这使得Kafka可以⼀个很重要的点进⾏优化：消息在⽹络上的传递。 现代的unix操作系统提供了⾼性能的将数据从⻚⾯缓存发送到socket的系统函数，在linux中，这个函 数是sendfile.

为了更好的理解sendfile的好处，我们先来看下⼀般将数据从⽂件发送到socket的数据流向：

操作系统把数据从⽂件拷⻉内核中的⻚缓存中

应⽤程序从⻚缓存从把数据拷⻉⾃⼰的内存缓存中

应⽤程序将数据写⼊到内核中socket缓存中

操作系统把数据从socket缓存中拷⻉到⽹卡接⼝缓存，从这⾥发送到⽹络上。

这显然是低效率的，有4次拷⻉和2次系统调⽤。Sendfile通过直接将数据从⻚⾯缓存发送⽹卡接⼝缓 存，避免了重复拷⻉，⼤⼤的优化了性能。 在⼀个多consumers的场景⾥，数据仅仅被拷⻉到⻚⾯缓存⼀次⽽不是每次消费消息的时候都重复的 进⾏拷⻉。这使得消息以近乎⽹络带宽的速率发送出去。这样在磁盘层⾯你⼏乎看不到任何的读操 作，因为数据都是从⻚⾯缓存中直接发送到⽹络上去了。

详细介绍了sendfile和zero-copy技术在Java⽅⾯的应⽤。

这篇⽂章

数据压缩 很多时候，性能的瓶颈并⾮CPU或者硬盘⽽是⽹络带宽，对于需要在数据中⼼之间传送⼤量数据的应 ⽤更是如此。当然⽤户可以在没有Kafka⽀持的情况下各⾃压缩⾃⼰的消息，但是这将导致较低的压缩 率，因为相⽐于将消息单独压缩，将⼤量⽂件压缩在⼀起才能起到最好的压缩效果。 Kafka采⽤了端到端的压缩：因为有“消息集”的概念，客户端的消息可以⼀起被压缩后送到服务端，并 以压缩后的格式写⼊⽇志⽂件，以压缩的格式发送到consumer，消息从producer发出到consumer拿 到都被是压缩的，只有在consumer使⽤的时候才被解压缩，所以叫做“端到端的压缩”。 Kafka⽀持GZIP和Snapy压缩协议。更详细的内容可以查看 。

这⾥

#

# 七、Producer和Consumer

KafkaProducer消息发送

producer直接将数据发送到broker的leader(主节点)，不需要在多个节点进⾏分发。为了帮助producer 做到这点，所有的Kafka节点都可以及时的告知:哪些节点是活动的，⽬标topic⽬标分区的leader在 哪。这样producer就可以直接将消息发送到⽬的地了。

客户端控制消息将被分发到哪个分区。可以通过负载均衡随机的选择，或者使⽤分区函数。Kafka允许 ⽤户实现分区函数，指定分区的key，将消息hash到不同的分区上(当然有需要的话，也可以覆盖这个 分区函数⾃⼰实现逻辑).⽐如如果你指定的key是user id，那么同⼀个⽤户发送的消息都被发送到同⼀ 个分区上。经过分区之后，consumer就可以有⽬的的消费某个分区的消息。

异步发送 批量发送可以很有效的提⾼发送效率。Kafka producer的异步发送模式允许进⾏批量发送，先将消息 缓存在内存中，然后⼀次请求批量发送出去。这个策略可以配置的，⽐如可以指定缓存的消息达到某 个量的时候就发出去，或者缓存了固定的时间后就发送出去（⽐如10条消息就发送，或者每5秒发送 ⼀次）。这种策略将⼤⼤减少服务端的I/O次数。

既然缓存是在producer端进⾏的，那么当producer崩溃时，这些消息就会丢失。Kafka0.8.1的异步发 送模式还不⽀持回调，就不能在发送出错时进⾏处理。Kafka 0.9可能会增加这样的回调函数。⻅

Prop osed Producer API

.

KafkaConsumer

Kafa consumer消费消息时，向broker发出"fetch"请求去消费特定分区的消息。consumer指定消息在 ⽇志中的偏移量（ofset），就可以消费从这个位置开始的消息。customer拥有了ofset的控制权，可 以向后回滚去重新消费之前的消息，这是很有意义的。

推还是拉？ Kafka最初考虑的问题是，customer应该从brokes拉取消息还是brokers将消息推送到consumer，也就 是pul还push。在这⽅⾯，Kafka遵循了⼀种⼤部分消息系统共同的传统的设计：producer将消息推送 到broker，consumer从broker拉取消息。

⼀些消息系统⽐如Scribe和Apache Flume采⽤了push模式，将消息推送到下游的consumer。这样做 有好处也有坏处：由broker决定消息推送的速率，对于不同消费速率的consumer就不太好处理了。消 息系统都致⼒于让consumer以最⼤的速率最快速的消费消息，但不幸的是，push模式下，当broker推 送的速率远⼤于consumer消费的速率时，consumer恐怕就要崩溃了。最终Kafka还是选取了传统的 pul模式。

Pul模式的另外⼀个好处是consumer可以⾃主决定是否批量的从broker拉取数据。Push模式必须在不 知道下游consumer消费能⼒和消费策略的情况下决定是⽴即推送每条消息还是缓存之后批量推送。如 果为了避免consumer崩溃⽽采⽤较低的推送速率，将可能导致⼀次只推送较少的消息⽽造成浪费。 Pul模式下，consumer就可以根据⾃⼰的消费能⼒去决定这些策略。

Pul有个缺点是，如果broker没有可供消费的消息，将导致consumer不断在循环中轮询，直到新消息 到t达。为了避免这点，Kafka有个参数可以让consumer阻塞知道新消息到达(当然也可以阻塞知道消息 的数量达到某个特定的量这样就可以批量发送)。

消费状态跟踪 对消费消息状态的记录也是很重要的。 ⼤部分消息系统在broker端的维护消息被消费的记录：⼀个消息被分发到consumer后broker就⻢上进 ⾏标记或者等待customer的通知后进⾏标记。这样也可以在消息在消费后⽴⻢就删除以减少空间占 ⽤。

但是这样会不会有什么问题呢？如果⼀条消息发送出去之后就⽴即被标记为消费过的，⼀旦consumer 处理消息时失败了（⽐如程序崩溃）消息就丢失了。为了解决这个问题，很多消息系统提供了另外⼀ 个个功能：当消息被发送出去之后仅仅被标记为已发送状态，当接到consumer已经消费成功的通知后 才标记为已被消费的状态。这虽然解决了消息丢失的问题，但产⽣了新问题，⾸先如果consumer处理 消息成功了但是向broker发送响应时失败了，这条消息将被消费两次。第⼆个问题时，broker必须维护 每条消息的状态，并且每次都要先锁住消息然后更改状态然后释放锁。这样麻烦⼜来了，且不说要维 护⼤量的状态数据，⽐如如果消息发送出去但没有收到消费成功的通知，这条消息将⼀直处于被锁定 的状态， Kafka采⽤了不同的策略。Topic被分成了若⼲分区，每个分区在同⼀时间只被⼀个consumer消费。这 意味着每个分区被消费的消息在⽇志中的位置仅仅是⼀个简单的整数：ofset。这样就很容易标记每个 分区消费状态就很容易了，仅仅需要⼀个整数⽽已。这样消费状态的跟踪就很简单了。

这带来了另外⼀个好处：consumer可以把ofset调成⼀个较⽼的值，去重新消费⽼的消息。这对传统 的消息系统来说看起来有些不可思议，但确实是⾮常有⽤的，谁规定了⼀条消息只能被消费⼀次呢？ consumer发现解析数据的程序有bug，在修改bug后再来解析⼀次消息，看起来是很合理的额呀！

离线处理消息 ⾼级的数据持久化允许consumer每个隔⼀段时间批量的将数据加载到线下系统中⽐如 或者数 据仓库。这种情况下，Hadop可以将加载任务分拆，拆成每个broker或每个topic或每个分区⼀个加载 任务。Hadop具有任务管理功能，当⼀个任务失败了就可以重启⽽不⽤担⼼数据被重新加载，只要从 上次加载的位置继续加载消息就可以了。

Hadop

#

## ⼋、主从同步

Kafka允许topic的分区拥有若⼲副本，这个数量是可以配置的，你可以为每个topci配置副本的数量。 Kafka会⾃动在每个个副本上备份数据，所以当⼀个节点down掉时数据依然是可⽤的。

Kafka的副本功能不是必须的，你可以配置只有⼀个副本，这样其实就相当于只有⼀份数据。 创建副本的单位是topic的分区，每个分区都有⼀个leader和零或多个folowers.所有的读写操作都由 leader处理，⼀般分区的数量都⽐broker的数量多的多，各分区的leader均匀的分布在brokers中。所 有的folowers都复制leader的⽇志，⽇志中的消息和顺序都和leader中的⼀致。flowers向普通的 consumer那样从leader那⾥拉取消息并保存在⾃⼰的⽇志⽂件中。

许多分布式的消息系统⾃动的处理失败的请求，它们对⼀个节点是否

着（alive）”有着清晰的定义。Kafka判断⼀个节点是否活着有两个条件：

节点必须可以维护和ZoKeper的连接，Zokeper通过⼼跳机制检查每个节点的连接。

如果节点是个folower,他必须能及时的同步leader的写操作，延时不能太久。

符合以上条件的节点准确的说应该是“同步中的（in sync）”，⽽不是模糊的说是“活着的”或是“失败 的”。Leader会追踪所有“同步中”的节点，⼀旦⼀个down掉了，或是卡住了，或是延时太久，leader就 会把它移除。⾄于延时多久算是“太久”，是由参数replica.lag.max.mesages决定的，怎样算是卡住 了，怎是由参数replica.lag.time.max.ms决定的。

只有当消息被所有的副本加⼊到⽇志中时，才算是“comited”，只有comited的消息才会发送给 consumer，这样就不⽤担⼼⼀旦leader down掉了消息会丢失。Producer也可以选择是否等待消息被 提交的通知，这个是由参数request.required.acks决定的。 Kafka保证只要有⼀个“同步中”的节点，“comited”的消息就不会丢失。

Leader的选择 Kafka的核⼼是⽇志⽂件，⽇志⽂件在集群中的同步是分布式数据系统最基础的要素。

如果leaders永远不会down的话我们就不需要folowers了！⼀旦leader down掉了，需要在folowers中 选择⼀个新的leader.但是folowers本身有可能延时太久或者crash，所以必须选择⾼质量的folower作 为leader.必须保证，⼀旦⼀个消息被提交了，但是leader down掉了，新选出的leader必须可以提供这 条消息。⼤部分的分布式系统采⽤了多数投票法则选择新的leader,对于多数投票法则，就是根据所有 副本节点的状况动态的选择最适合的作为leader.Kafka并不是使⽤这种⽅法。

Kafaka动态维护了⼀个同步状态的副本的集合（a set of in-sync replicas），简称ISR，在这个集合中 的节点都是和leader保持⾼度⼀致的，任何⼀条消息必须被这个集合中的每个节点读取并追加到⽇志中 了，才回通知外部这个消息已经被提交了。因此这个集合中的任何⼀个节点随时都可以被选为 leader.ISR在ZoKeper中维护。ISR中有f+1个节点，就可以允许在f个节点down掉的情况下不会丢失 消息并正常提供服。ISR的成员是动态的，如果⼀个节点被淘汰了，当它重新达到“同步中”的状态时， 他可以重新加⼊ISR.这种leader的选择⽅式是⾮常快速的，适合kafka的应⽤场景。

⼀个邪恶的想法：如果所有节点都down掉了怎么办？Kafka对于数据不会丢失的保证，是基于⾄少⼀ 个节点是存活的，⼀旦所有节点都down了，这个就不能保证了。 实际应⽤中，当所有的副本都down掉时，必须及时作出反应。可以有以下两种选择:

等待ISR中的任何⼀个节点恢复并担任leader。

选择所有节点中（不只是ISR）第⼀个恢复的节点作为leader.

这是⼀个在可⽤性和连续性之间的权衡。如果等待ISR中的节点恢复，⼀旦ISR中的节点起不起来或者 数据都是了，那集群就永远恢复不了了。如果等待ISR意外的节点恢复，这个节点的数据就会被作为线 上数据，有可能和真实的数据有所出⼊，因为有些数据它可能还没同步到。Kafka⽬前选择了第⼆种策 略，在未来的版本中将使这个策略的选择可配置，可以根据场景灵活的选择。 这种窘境不只Kafka会遇到，⼏乎所有的分布式数据系统都会遇到。

副本管理 以上仅仅以⼀个topic⼀个分区为例⼦进⾏了讨论，但实际上⼀个Kafka将会管理成千上万的topic分 区.Kafka尽量的使所有分区均匀的分布到集群所有的节点上⽽不是集中在某些节点上，另外主从关系也 尽量均衡这样每个⼏点都会担任⼀定⽐例的分区的leader. 优化leader的选择过程也是很重要的，它决定了系统发⽣故障时的空窗期有多久。Kafka选择⼀个节点 作为“controler”,当发现有节点down掉的时候它负责在游泳分区的所有节点中选择新的leader,这使得 Kafka可以批量的⾼效的管理所有分区节点的主从关系。如果controler down掉了，活着的节点中的⼀ 个会备切换为新的controler.

#

# 九、客户端API

Kafka Producer APIs Procuder API有两种：kafka.producer.SyncProducer和kafka.producer.async.AsyncProducer.它们都 实现了同⼀个接⼝：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


class Producer { /* 将 消 息 发 送 到 指 定 分 区 */ publicvoid send(kafka.javaapi.producer.ProducerData<K,V> producerData); /* 批 量 发 送 ⼀ 批 消 息 */ publicvoid send(java.util.List<kafka.javaapi.producer.ProducerData<K,V>> producerData); /* 关 闭 producer */ publicvoid close(); }

复制代码

Producer API提供了以下功能：

可以将多个消息缓存到本地队列⾥，然后异步的批量发送到broker，可以通过参数 producer.type=async做到。缓存的⼤⼩可以通过⼀些参数指定：queue.time和batch.size。⼀个后 台线程（(kafka.producer.async.ProducerSendThread）从队列中取出数据并让 kafka.producer.EventHandler将消息发送到broker，也可以通过参数event.handler定制handler， 在producer端处理数据的不同的阶段注册处理器，⽐如可以对这⼀过程进⾏⽇志追踪，或进⾏⼀些 监控。只需实现kafka.producer.async.CalbackHandler接⼝，并在calback.handler中配置。

⾃⼰编写Encoder来序列化消息，只需实现下⾯这个接⼝。默认的Encoder是 kafka.serializer.DefaultEncoder。

interface Encoder<T> {

public Mesage toMesage(T data);

}

提供了基于Zokeper的broker⾃动感知能⼒，可以通过参数zk.conect实现。如果不使⽤ Zokeper，也可以使⽤broker.list参数指定⼀个静态的brokers列表，这样消息将被随机的发送到 ⼀个broker上，⼀旦选中的broker失败了，消息发送也就失败了。

通过分区函数kafka.producer.Partitioner类对消息分区。

interface Partitioner<T> {

int partition(T key, int numPartitions);

}

分区函数有两个参数：key和可⽤的分区数量，从分区列表中选择⼀个分区并返回id。默认的分区策略 是hash(key)%numPartitions.如果key是nul,就随机的选择⼀个。可以通过参数partitioner.clas定制分 区函数。

KafKa Consumer APIs

Consumer API有两个级别。低级别的和⼀个指定的broker保持连接，并在接收完消息后关闭连接，这 个级别是⽆状态的，每次读取消息都带着ofset。 ⾼级别的API隐藏了和brokers连接的细节，在不必关⼼服务端架构的情况下和服务端通信。还可以⾃ ⼰维护消费状态，并可以通过⼀些条件指定订阅特定的topic,⽐如⽩名单⿊名单或者正则表达式。

低级别的API

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


class SimpleConsumer { /*向 ⼀ 个 broker发 送 读 取 请 求 并得 到 消 息 集 */ public ByteBufferMessageSet fetch(FetchRequest request); /*向 ⼀ 个 broker发 送 读 取 请 求 并得 到 ⼀ 个 相 应 集 */ public MultiFetchResponse multifetch(List<FetchRequest> fetches); /**

- * 得 到 指 定 时 间 之 前 的 offsets

- * 返 回 值 是 offsets列 表 ， 以倒 序 排 序

- * @param time: 时 间 ， 毫 秒 ,

- * 如 果 指 定 为 OffsetRequest$.MODULE$.LATIEST_TIME(), 得 到 最 新 的 offset.

- * 如 果 指 定 为 OffsetRequest$.MODULE$.EARLIEST_TIME(),得 到 最 ⽼ 的 offset.

- */


publiclong[] getOffsetsBefore(String topic, int partition, long time, int maxNumOffsets); }

复制代码

低级别的API是⾼级别API实现的基础，也是为了⼀些对维持消费状态有特殊需求的场景，⽐如 consumer这样的离线consumer。

Hadop

⾼级别的API

- 1.
- 2.


/* 创 建 连 接 */ ConsumerConnector connector = Consumer.create(consumerConfig);

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


interface ConsumerConnector { /**

- * 这 个 ⽅ 法 可 以 得 到 ⼀ 个 流 的 列 表 ， 每 个 流 都 是 MessageAndMetadata的 迭 代 ， 通过 MessageAndMetadata可 以 拿 到 消 息 和 其 他 的 元 数据 （ ⽬ 前 之 后 topic）

- * Input: a map of <topic, #streams>

- * Output: a map of <topic, list of message streams>

- */ public Map<String,List<KafkaStream>> createMessageStreams(Map<String,Int> topicCountMap); /**

- * 你也 可 以 得 到 ⼀ 个 流 的 列 表 ， 它 包 含 了 符 合 TopicFiler的 消 息 的 迭 代 ，

- * ⼀ 个 TopicFilter是 ⼀ 个 封 装 了 ⽩ 名单 或 ⿊ 名单 的 正 则 表 达 式 。

- */ public List<KafkaStream> createMessageStreamsByFilter( TopicFilter topicFilter, int numStreams); /* 提 交 ⽬ 前 消 费 到 的 offset */ public commitOffsets() /* 关 闭 连 接 */ public shutdown() }


复制代码

这个API围绕着由KafkaStream实现的迭代器展开，每个流代表⼀系列从⼀个或多个分区多和broker上 汇聚来的消息，每个流由⼀个线程处理，所以客户端可以在创建的时候通过参数指定想要⼏个流。⼀ 个流是多个分区多个broker的合并，但是每个分区的消息只会流向⼀个流。

每调⽤⼀次createMesageStreams都会将consumer注册到topic上，这样consumer和brokers之间的 负载均衡就会进⾏调整。API⿎励每次调⽤创建更多的topic流以减少这种调整。 createMesageStreamsByFilter⽅法注册监听可以感知新的符合filter的tipic。

#

⼗、消息和⽇志

消息由⼀个固定⻓度的头部和可变⻓度的字节数组组成。头部包含了⼀个版本号和CRC32校验码。

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


/**

- * 具有N个字节的消息的格式如下

*

- * 如果版本号是0

*

- * 1. 1个字节的 "magic" 标记

*

- * 2. 4个字节的CRC32校验码

*

- * 3. N - 5个字节的具体信息

*

- * 如果版本号是1

*

- * 1. 1个字节的 "magic" 标记

*

- * 2.1个字节的参数允许标注⼀些附加的信息⽐如是否压缩了，解码类型等

*

- * 3.4个字节的CRC32校验码

*

- * 4. N - 6 个字节的具体信息

*

- */


复制代码

⽇志⼀个叫做“my_topic”且有两个分区的的topic,它的⽇志有两个⽂件夹组成，my_topic_0和 my_topic_1,每个⽂件夹⾥放着具体的数据⽂件，每个数据⽂件都是⼀系列的⽇志实体，每个⽇志实体 有⼀个4个字节的整数N标注消息的⻓度，后边跟着N个字节的消息。每个消息都可以由⼀个64位的整 数ofset标注，ofset标注了这条消息在发送到这个分区的消息流中的起始位置。每个⽇志⽂件的名称 都是这个⽂件第⼀条⽇志的ofset.所以第⼀个⽇志⽂件的名字就是 0.kafka.所以每相邻的 两个⽂件名字的差就是⼀个数字S,S差不多就是配置⽂件中指定的⽇志⽂件的最⼤容量。 消息的格式都由⼀个统⼀的接⼝维护，所以消息可以在producer,broker和consumer之间⽆缝的传递。 存储在硬盘上的消息格式如下所示：

消息⻓度: 4 bytes (value: 1+4+n)

版本号: 1 byte

CRC校验码: 4 bytes 具体的消息: n bytes

写操作消息被不断的追加到最后⼀个⽇志的末尾，当⽇志的⼤⼩达到⼀个指定的值时就会产⽣⼀个新 的⽂件。对于写操作有两个参数，⼀个规定了消息的数量达到这个值时必须将数据刷新到硬盘上，另 外⼀个规定了刷新到硬盘的时间间隔，这对数据的持久性是个保证，在系统崩溃的时候只会丢失⼀定 数量的消息或者⼀个时间段的消息。

读操作 读操作需要两个参数：⼀个64位的ofset和⼀个S字节的最⼤读取量。S通常⽐单个消息的⼤⼩要⼤， 但在⼀些个别消息⽐较⼤的情况下，S会⼩于单个消息的⼤⼩。这种情况下读操作会不断重试，每次重 试都会将读取量加倍，直到读取到⼀个完整的消息。可以配置单个消息的最⼤值，这样服务器就会拒 绝⼤⼩超过这个值的消息。也可以给客户端指定⼀个尝试读取的最⼤上限，避免为了读到⼀个完整的 消息⽽⽆限次的重试。 在实际执⾏读取操纵时，⾸先需要定位数据所在的⽇志⽂件，然后根据ofset计算出在这个⽇志中的 ofset(前⾯的的ofset是整个分区的ofset),然后在这个ofset的位置进⾏读取。定位操作是由⼆分查找 法完成的，Kafka在内存中为每个⽂件维护了ofset的范围。

下⾯是发送给consumer的结果的格式：

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


MessageSetSend (fetch result)

total length : 4 bytes error code : 2 bytes message 1 : x bytes ... message n : x bytes MultiMessageSetSend (multiFetch result)

total length : 4 bytes error code : 2 bytes messageSetSend 1

...

14. messageSetSend n

复制代码

删除 ⽇志管理器允许定制删除策略。⽬前的策略是删除修改时间在N天之前的⽇志（按时间删除），也可以 使⽤另外⼀个策略：保留最后的N GB数据的策略(按⼤⼩删除)。为了避免在删除时阻塞读操作，采⽤ 了copy-on-write形式的实现，删除操作进⾏时，读取操作的⼆分查找功能实际是在⼀个静态的快照副 本上进⾏的，这类似于Java的CopyOnWriteArayList。

可靠性保证 ⽇志⽂件有⼀个可配置的参数M，缓存超过这个数量的消息将被强⾏刷新到硬盘。⼀个⽇志矫正线程将 循环检查最新的⽇志⽂件中的消息确认每个消息都是合法的。合法的标准为：所有⽂件的⼤⼩的和最 ⼤的ofset⼩于⽇志⽂件的⼤⼩，并且消息的CRC32校验码与存储在消息实体中的校验码⼀致。如果在 某个ofset发现不合法的消息，从这个ofset到下⼀个合法的ofset之间的内容将被移除。 有两种情况必须考虑：

- 1，当发⽣崩溃时有些数据块未能写⼊。

- 2，写⼊了⼀些空⽩数据块。第⼆种情况的原因是，对于每个⽂件，操作系统都有⼀个inode（inode是 指在许多“类Unix⽂件系统”中的⼀种数据结构。每个inode保存了⽂件系统中的⼀个⽂件系统对象,包括 ⽂件、⽬录、⼤⼩、设备⽂件、socket、管道, 等等），但⽆法保证更新inode和写⼊数据的顺序，当 inode保存的⼤⼩信息被更新了，但写⼊数据时发⽣了崩溃，就产⽣了空⽩数据块。CRC校验码可以检 查这些块并移除，当然因为崩溃⽽未写⼊的数据块也就丢失了。


