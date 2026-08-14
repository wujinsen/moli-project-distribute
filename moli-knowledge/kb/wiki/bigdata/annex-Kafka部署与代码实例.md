---
title: Kafka部署与代码实例.note（原文插图 annex）
slug: annex-Kafka部署与代码实例
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/Kafka/Kafka部署与代码实例.note.md
related: [kafka-大数据管道]
created: 2026-07-05
updated: 2026-07-05
---

kafka作为分布式⽇志收集或系统监控服务，我们有必要在合适的场合使⽤它。kafka的部署包括 zookeeper环境/kafka环境，同时还需要进⾏⼀些配置操作.接下来介绍如何使⽤kafka.

我们使⽤3个zookeeper实例构建zk集群，使⽤2个kafka broker构建kafka集群. 其中kafka为0.8V，zookeeper为3.4.5V

# ⼀.Zookeeper集群构建

我们有3个zk实例，分别为zk-0,zk-1,zk-2;如果你仅仅是测试使⽤，可以使⽤1个zk实例.（本示例基 于伪分布式部署）

1) zk-0 调整配置⽂件：

Php代码

![image 1](assets/imageFile1.png)

- 1.
- 2.
- 3.
- 4.
- 5.


- clientPort=2181

- server.0=127.0.0.1:2888:3888

- server.1=127.0.0.1:2889:3889

- server.2=127.0.0.1:2890:3890 ##只需要修改上述配置，其他配置保留默认值


![image 2](assets/imageFile2.png)

./zkServer.sh start

![image 3](assets/imageFile3.png)

- clientPort=2182 ##只需要修改上述配置，其他配置保留默认值


启动zookeeper

Java代码

1.

2) zk-1 调整配置⽂件(其他配置和zk-0⼀只)：

Php代码

- 1.
- 2.


启动zookeeper

Java代码

![image 4](assets/imageFile4.png)

1.

./zkServer.sh start

3) zk-2 调整配置⽂件(其他配置和zk-0⼀只)：

Php代码

![image 5](assets/imageFile5.png)

clientPort=2183 ##只需要修改上述配置，其他配置保留默认值

2.

启动zookeeper

Java代码

![image 6](assets/imageFile6.png)

1.

./zkServer.sh start

⼆. Kafka集群构建

因为Broker配置⽂件涉及到zookeeper的相关约定，因此我们先展示broker配置⽂件.我们使⽤2个 kafka broker来构建这个集群环境，分别为kafka-0,kafka-1.

1) kafka-0 在config⽬录下修改配置⽂件为：

Java代码

![image 7](assets/imageFile7.png)

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


broker.id=0 port=9092 num.network.threads=2 num.io.threads=2 socket.send.buffer.bytes=1048576 socket.receive.buffer.bytes=1048576 socket.request.max.bytes=104857600 log.dir=./logs num.partitions=2 log.flush.interval.messages=10000 log.flush.interval.ms=1000 log.retention.hours=168 #log.retention.bytes=1073741824 log.segment.bytes=536870912 ##replication机制,让每个topic的partitions在kafka-cluster中备份2个 ##⽤来提⾼cluster的容错能⼒.. default.replication.factor=1 log.cleanup.interval.mins=10 zookeeper.connect=127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183 zookeeper.connection.timeout.ms=1000000

因为kafka⽤scala语⾔编写，因此运⾏kafka需要⾸先准备scala相关环境。

Java代码

![image 8](assets/imageFile8.png)

> cd kafka-0 > ./sbt update > ./sbt package > ./sbt assembly-package-dependency

- 2.
- 3.
- 4.


其中最后⼀条指令执⾏有可能出现异常，暂且不管。 启动kafka broker：

Java代码

![image 9](assets/imageFile9.png)

1.

- > JMS_PORT=9997 bin/kafka-server-start.sh config/server.properties &

![image 10](assets/imageFile10.png)

broker.id=1 port=9093 ##其他配置和kafka-0保持⼀致

![image 11](assets/imageFile11.png)

- > JMS_PORT=9998 bin/kafka-server-start.sh config/server.properties &


因为zookeeper环境已经正常运⾏了，我们⽆需通过kafka来挂载启动zookeeper.如果你的⼀台机器上 部署了多个kafka broker，你需要声明JMS_PORT.

# 2) kafka-1

Java代码

- 1.
- 2.
- 3.


然后和kafka-0⼀样执⾏打包命令，然后启动此broker.

Java代码

1.

仍然可以通过如下指令查看topic的"partition"/"replicas"的分布和存活情况.

Java代码

![image 12](assets/imageFile12.png)

- 1.
- 2.
- 3.


> bin/kafka-list-topic.sh --zookeeper localhost:2181 topic: my-replicated-topic partition: 0 leader: 2 replicas: 1,2,0 isr: 2 topic: test partition: 0 leader: 0 replicas: 0 isr: 0

到⽬前为⽌环境已经OK了,那我们就开始展示编程实例吧。[ ]

配置参数详解

三.项⽬准备

项⽬基于maven构建，不得不说kafka 客户端实在是太糟糕了；构建环境会遇到很多麻烦。建 议参考如下pom.xml;其中各个依赖包必须版本协调⼀致。如果kafka client的版本和kafka server的版本 不⼀致,将会有很多异常,⽐如"broker id not exists"等;因为kafka从0.7升级到0.8之后(正名为 2.8.0),client与server通讯的protocol已经改变.

Java

Java代码

![image 13](assets/imageFile13.png)

<dependencies>

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


<dependency> <groupId>log4j</groupId> <artifactId>log4j</artifactId> <version>1.2.14</version>

</dependency> <dependency>

<groupId>org.apache.kafka</groupId> <artifactId>kafka_2.8.2</artifactId> <version>0.8.0</version> <exclusions>

<exclusion> <groupId>log4j</groupId> <artifactId>log4j</artifactId>

</exclusion>

</exclusions> </dependency> <dependency>

<groupId>org.scala-lang</groupId> <artifactId>scala-library</artifactId> <version>2.8.2</version>

</dependency> <dependency>

<groupId>com.yammer.metrics</groupId> <artifactId>metrics-core</artifactId> <version>2.2.0</version>

</dependency> <dependency>

<groupId>com.101tec</groupId> <artifactId>zkclient</artifactId> <version>0.3</version>

</dependency> </dependencies>

# 四.Producer端代码

- 1) producer.properties⽂件：此⽂件放在/resources⽬录下


Java代码

![image 14](assets/imageFile14.png)

#partitioner.class= ##broker列表可以为kafka server的⼦集,因为producer需要从broker中获取metadata ##尽管每个broker都可以提供metadata,此处还是建议,将所有broker都列举出来 ##此值,我们可以在spring中注⼊过来 ##metadata.broker.list=127.0.0.1:9092,127.0.0.1:9093 ##,127.0.0.1:9093 ##同步,建议为async producer.type=sync compression.codec=0 serializer.class=kafka.serializer.StringEncoder ##在producer.type=async时有效 #batch.num.messages=100

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


# 2) KafkaProducerClient.java代码样例

Java代码

![image 15](assets/imageFile15.png)

import java.util.ArrayList; import java.util.Collection; import java.util.List; import java.util.Properties;

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
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.


import kafka.javaapi.producer.Producer; import kafka.producer.KeyedMessage; import kafka.producer.ProducerConfig;

/**

- * User: guanqing-liu

- */


public class KafkaProducerClient {

private Producer<String, String> inner;

private String brokerList;//for metadata discovery,spring setter private String location = "kafka-producer.properties";//spring setter

private String defaultTopic;//spring setter

public void setBrokerList(String brokerList) {

this.brokerList = brokerList; }

public void setLocation(String location) {

this.location = location; }

public void setDefaultTopic(String defaultTopic) {

this.defaultTopic = defaultTopic; }

public KafkaProducerClient(){}

public void init() throws Exception { Properties properties = new Properties(); properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(

location));

if(brokerList != null) {

properties.put("metadata.broker.list", brokerList); }

ProducerConfig config = new ProducerConfig(properties);

- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.
- 88.
- 89.


inner = new Producer<String, String>(config); }

public void send(String message){

send(defaultTopic,message); }

public void send(Collection<String> messages){

send(defaultTopic,messages); }

public void send(String topicName, String message) { if (topicName == null || message == null) { return;

} KeyedMessage<String, String> km = new KeyedMessage<String, String>

(topicName,message);

inner.send(km); }

public void send(String topicName, Collection<String> messages) { if (topicName == null || messages == null) { return;

} if (messages.isEmpty()) {

## return;

} List<KeyedMessage<String, String>> kms = new ArrayList<KeyedMessage<String, String>

>();

int i= 0; for (String entry : messages) {

KeyedMessage<String, String> km = new KeyedMessage<String, String>

(topicName,entry); kms.add(km); i++; if(i % 20 == 0){

inner.send(kms); kms.clear();

} }

if(!kms.isEmpty()){

inner.send(kms); }

}

public void close() {

- 90.
- 91.
- 92.
- 93.
- 94.
- 95.
- 96.
- 97.
- 98.
- 99.
- 100.
- 101.
- 102.
- 103.
- 104.
- 105.
- 106.
- 107.
- 108.
- 109.
- 110.
- 111.
- 112.
- 113.
- 114.
- 115.
- 116.
- 117.


inner.close(); }

/**

- * @param args

- */


public static void main(String[] args) { KafkaProducerClient producer = null; try {

producer = new KafkaProducerClient(); //producer.setBrokerList(""); int i = 0; while (true) {

producer.send("test-topic", "this is a sample" + i); i++; Thread.sleep(2000);

} } catch (Exception e) {

e.printStackTrace(); } finally {

if (producer != null) {

producer.close(); }

}

}

}

- 3) 配置


# spring

Java代码

![image 16](assets/imageFile16.png)

- 1.
- 2.
- 3.
- 4.


<bean id="kafkaProducerClient" class="com.test.kafka.KafkaProducerClient" initmethod="init" destroy-method="close">

<property name="zkConnect" value="${zookeeper_cluster}"></property> <property name="defaultTopic" value="${kafka_topic}"></property>

</bean>

# 五.Consumer端

- 1) consumer.properties:⽂件位于/resources⽬录下


Java代码

![image 17](assets/imageFile17.png)

## 此值可以配置,也可以通过spring注⼊ ##zookeeper.connect=127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183 ##,127.0.0.1:2182,127.0.0.1:2183 # timeout in ms for connecting to zookeeper zookeeper.connectiontimeout.ms=1000000 #consumer group id group.id=test-group #consumer timeout #consumer.timeout.ms=5000 auto.commit.enable=true auto.commit.interval.ms=60000

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


# 2) KafkaConsumerClient.java代码样例

Java代码

![image 18](assets/imageFile18.png)

package com.test.kafka; import java.nio.ByteBuffer; import java.nio.CharBuffer; import java.nio.charset.Charset; import java.util.HashMap; import java.util.List; import java.util.Map; import java.util.Properties; import java.util.concurrent.ExecutorService; import java.util.concurrent.Executors;

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
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.


import kafka.consumer.Consumer; import kafka.consumer.ConsumerConfig; import kafka.consumer.ConsumerIterator; import kafka.consumer.KafkaStream; import kafka.javaapi.consumer.ConsumerConnector; import kafka.message.Message; import kafka.message.MessageAndMetadata;

/**

- * User: guanqing-liu

- */


public class KafkaConsumerClient {

private String groupid; //can be setting by spring private String zkConnect;//can be setting by spring private String location = "kafka-consumer.properties";//配置⽂件位置 private String topic; private int partitionsNum = 1; private MessageExecutor executor; //message listener private ExecutorService threadPool;

private ConsumerConnector connector;

private Charset charset = Charset.forName("utf8");

public void setGroupid(String groupid) {

this.groupid = groupid; }

public void setZkConnect(String zkConnect) {

this.zkConnect = zkConnect; }

public void setLocation(String location) { this.location = location;

- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.
- 88.
- 89.
- 90.
- 91.


}

public void setTopic(String topic) {

this.topic = topic; }

public void setPartitionsNum(int partitionsNum) {

this.partitionsNum = partitionsNum; }

public void setExecutor(MessageExecutor executor) {

this.executor = executor; }

public KafkaConsumerClient() {}

//init consumer,and start connection and listener public void init() throws Exception {

if(executor == null){ throw new RuntimeException("KafkaConsumer,exectuor cant be null!");

} Properties properties = new Properties(); properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(

location));

if(groupid != null){

properties.put("groupid", groupid);

} if(zkConnect != null){

properties.put("zookeeper.connect", zkConnect);

} ConsumerConfig config = new ConsumerConfig(properties);

connector = Consumer.createJavaConsumerConnector(config); Map<String, Integer> topics = new HashMap<String, Integer>(); topics.put(topic, partitionsNum); Map<String, List<KafkaStream<byte[], byte[]>>> streams = connector.createMessageStr

eams(topics); List<KafkaStream<byte[], byte[]>> partitions = streams.get(topic); threadPool = Executors.newFixedThreadPool(partitionsNum * 2);

//start for (KafkaStream<byte[], byte[]> partition : partitions) {

threadPool.execute(new MessageRunner(partition)); }

}

- 92.
- 93.
- 94.
- 95.
- 96.
- 97.
- 98.
- 99.
- 100.
- 101.
- 102.
- 103.
- 104.
- 105.
- 106.
- 107.
- 108.
- 109.
- 110.
- 111.
- 112.
- 113.
- 114.
- 115.
- 116.
- 117.
- 118.
- 119.
- 120.
- 121.
- 122.
- 123.
- 124.
- 125.
- 126.
- 127.
- 128.
- 129.
- 130.
- 131.
- 132.
- 133.
- 134.
- 135.
- 136.
- 137.
- 138.


## public void close() { try {

threadPool.shutdownNow(); } catch (Exception e) {

## // } finally {

connector.shutdown(); }

}

class MessageRunner implements Runnable { private KafkaStream<byte[], byte[]> partition;

MessageRunner(KafkaStream<byte[], byte[]> partition) {

this.partition = partition; }

public void run() { ConsumerIterator<byte[], byte[]> it = partition.iterator(); while (it.hasNext()) {

// connector.commitOffsets();⼿动提交offset,当autocommit.enable=false时使⽤ MessageAndMetadata<byte[], byte[]> item = it.next(); try{

executor.execute(new String(item.message(),charset));// UTF-8,注意异常 }catch(Exception e){

// }

} }

public String getContent(Message message){ ByteBuffer buffer = message.payload(); if (buffer.remaining() == 0) {

## return null;

} CharBuffer charBuffer = charset.decode(buffer); return charBuffer.toString();

} }

## public static interface MessageExecutor {

public void execute(String message); }

/**

- 139.
- 140.
- 141.
- 142.
- 143.
- 144.
- 145.
- 146.
- 147.
- 148.
- 149.
- 150.
- 151.
- 152.
- 153.
- 154.
- 155.
- 156.
- 157.
- 158.
- 159.
- 160.
- 161.
- 162.
- 163.
- 164.
- 165.
- 166.


- * @param args

- */


public static void main(String[] args) { KafkaConsumerClient consumer = null; try {

MessageExecutor executor = new MessageExecutor() {

public void execute(String message) {

System.out.println(message); }

}; consumer = new KafkaConsumerClient();

consumer.setTopic("test-topic"); consumer.setPartitionsNum(2); consumer.setExecutor(executor); consumer.init();

} catch (Exception e) {

e.printStackTrace(); } finally {

if(consumer != null){

consumer.close(); }

}

}

}

# 3) spring配置(略)

需要提醒的是,上述LogConsumer类中,没有太多的关注异常情况,必须在MessageExecutor.execute() ⽅法中抛出异常时的情况.

在测试时，建议优先启动consumer，然后再启动producer，这样可以实时的观测到最新的消息。

test-kafka.zip (5.3 KB)
