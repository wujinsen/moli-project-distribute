---
title: kafka java示例.note（原文插图 annex）
slug: annex-kafka-java示例
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/kafka/kafka java示例.note.md
related: [kafka-大数据管道]
created: 2026-07-05
updated: 2026-07-05
---

我使⽤的kafka版本是：0.7.2 jdk版本是：1.6.0_20

htp:/kafka.apache.org/07/quickstart.html

官⽅给的示例并不是很完整，以下代码是经过我补充的并 且编译后能运⾏的。

Producer Code [java]

view plaincopy import java.util.*; import kafka.mesage.Mesage; import kafka.producer.ProducerConfig; import kafka.javapi.producer.Producer; import kafka.javapi.producer.ProducerData;

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


publicclas ProducerSample {

publicstaticvoid main(String[] args) { ProducerSample ps = new ProducerSample();

Properties props = new Properties(); props.put("zk.conect", "127.0.0.1 2181"); props.put("serializer.clas", "kafka.serializer.StringEncoder");

ProducerConfig config = new ProducerConfig(props); Producer<String, String> producer = new Producer<String, String>(config); ProducerData<String, String> data = new ProducerData<String, String>("test-

topic", "test-mesage2"); producer.send(data); producer.close();

} }

Consumer Code [java]

view plaincopy import java.nio.ByteBufer; import java.util.HashMap; import java.util.List;

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
- 38.


import java.util.Map; import java.util.Properties; import java.util.concurent.ExecutorService; import java.util.concurent.Executors; import kafka.consumer.Consumer; import kafka.consumer.ConsumerConfig; import kafka.consumer.KafkaStream; import kafka.javapi.consumer.ConsumerConector; import kafka.mesage.Mesage; import kafka.mesage.MesageAndMetadata;

publicclas ConsumerSample {

publicstaticvoid main(String[] args) {

/ specify some consumer properties Properties props = new Properties(); props.put("zk.conect", "localhost:2181"); props.put("zk.conectiontimeout.ms", "1 0"); props.put("groupid", "test_group");

/ Create the conection to the cluster ConsumerConfig consumerConfig = new ConsumerConfig(props); ConsumerConector consumerConector = Consumer.createJavaConsumerConector(co

nsumerConfig);

/ create 4 partitions of the stream for topic “test-

topic”, to alow 4 threads to consume HashMap<String, Integer> map = new HashMap<String, Integer>(); map.put("test-topic", 4); Map<String, List<KafkaStream<Mesage > topicMesageStreams =

consumerConector.createMesageStreams(map); List<KafkaStream<Mesage> streams = topicMesageStreams.get("test-topic");

/ create list of 4 threads to consume from each of the partitions ExecutorService executor = Executors.newFixedThreadPol(4);

/ consume the mesages in the threads

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
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.


for (final KafkaStream<Mesage> stream : streams) { executor.submit(new Runable() { publicvoid run() {

for (MesageAndMetadata msgAndMetadata : stream) { / proces mesage (msgAndMetadata.mesage() System.out.println("topic: " + msgAndMetadata.topic(); Mesage mesage = (Mesage) msgAndMetadata.mesage(); ByteBufer bufer = mesage.payload();

<span style="white-

space:pre"> </span>byte[] bytes = newbyte[mesage.payloadSize()]; bufer.get(bytes); String tmp = new String(bytes); System.out.println("mesage content: " + tmp);

} }

}); }

} }

分别启动zookeeper,kafka server之后，依次运⾏Producer,Consumer的代码 运⾏ProducerSample：

![image 1](assets/imageFile1.png)

运⾏ConsumerSample:

![image 2](assets/imageFile2.png)

由于本⼈不熟悉java的多线程，将官⽅给的Consumer Code做点⼩改动，如下所示： [java]

view plaincopy import java.nio.ByteBufer; import java.util.HashMap; import java.util.List; import java.util.Map; import java.util.Properties; import kafka.consumer.Consumer;

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
- 38.
- 39.
- 40.
- 41.
- 42.


import kafka.consumer.ConsumerConfig; import kafka.consumer.KafkaStream; import kafka.javapi.consumer.ConsumerConector; import kafka.mesage.Mesage; import kafka.mesage.MesageAndMetadata;

publicclas ConsumerSample2 {

publicstaticvoid main(String[] args) {

/ specify some consumer properties Properties props = new Properties(); props.put("zk.conect", "localhost:2181"); props.put("zk.conectiontimeout.ms", "1 0"); props.put("groupid", "test_group");

/ Create the conection to the cluster ConsumerConfig consumerConfig = new ConsumerConfig(props); ConsumerConector consumerConector = Consumer.createJavaConsumerConector(co

nsumerConfig);

HashMap<String, Integer> map = new HashMap<String, Integer>(); map.put("test-topic", 1); Map<String, List<KafkaStream<Mesage > topicMesageStreams =

consumerConector.createMesageStreams(map); List<KafkaStream<Mesage> streams = topicMesageStreams.get("test-topic");

<strong>for (final KafkaStream<Mesage> stream : streams) {

for (MesageAndMetadata msgAndMetadata : stream) { / proces mesage (msgAndMetadata.mesage() System.out.println("topic: " + msgAndMetadata.topic(); Mesage mesage = (Mesage) msgAndMetadata.mesage(); ByteBufer bufer = mesage.payload(); byte[] bytes = newbyte[mesage.payloadSize()]; bufer.get(bytes); String tmp = new String(bytes); System.out.println("mesage content: " + tmp);

}

- 43.
- 44.
- 45.


}</strong> }

}

我在Producer端⼜发送了⼀条“test-message2”的消息，Consumer收到了两条消息，如下所示：

![image 3](assets/imageFile3.png)

kafka作为分布式⽇志收集或系统监控服务，我们有必要在合适的场合使⽤它。kafka的部署包括 zokeper环境/kafka环境，同时还需要进⾏⼀些配置操作.接下来介绍如何使⽤kafka.

我们使⽤3个zokeper实例构建zk集群，使⽤2个kafka broker构建kafka集群. 其中kafka为0.8V，zokeper为3.4.5V

⼀.Zokeper集群构建 我们有3个zk实例，分别为zk-0,zk-1,zk-2;如果你仅仅是测试使⽤，可以使⽤1个zk实例.

1) zk-0 调整配置⽂件：

Php代码

- 1.
- 2.
- 3.
- 4.
- 5.


- clientPort=2181

- server.0=127.0.0.1 2 8 3 8
- server.1=127.0.0.1 289 389
- server.2=127.0.0.1 2890 3890 #只需要修改上述配置，其他配置保留默认值


./zkServer.sh start

- clientPort=2182 #只需要修改上述配置，其他配置保留默认值


启动zokeper Java代码

1.

2) zk-1 调整配置⽂件(其他配置和zk-0⼀只)：

Php代码

- 1.
- 2.


启动zokeper

Java代码

1.

./zkServer.sh start

3) zk-2

调整配置⽂件(其他配置和zk-0⼀只)： Php代码

- 1.
- 2.


clientPort=2183 #只需要修改上述配置，其他配置保留默认值

启动zokeper

Java代码

1.

./zkServer.sh start

⼆. Kafka集群构建

因为Broker配置⽂件涉及到zokeper的相关约定，因此我们先展示broker配置⽂件.我们使⽤2个 kafka broker来构建这个集群环境，分别为kafka-0,kafka-1.

1) kafka-0 在config⽬录下修改配置⽂件为：

Java代码

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


broker.id=0 port=9092 num.network.threads=2 num.io.threads=2 socket.send.bufer.bytes=1048576 socket.receive.bufer.bytes=1048576 socket.request.max.bytes=10485760 log.dir=./logs num.partitions=2 log.flush.interval.mesages=1 0 log.flush.interval.ms=1 0 log.retention.hours=168 #log.retention.bytes=1073741824 log.segment.bytes=536870912

#replication机制,让每个topic的partitions在kafka-cluster中备份2个 #⽤来提⾼cluster的容错能⼒ .

default.replication.factor=1 log.cleanup.interval.mins=10 zokeper.conect=127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183 zokeper.conection.timeout.ms=1 0

因为kafka⽤scala语⾔编写，因此运⾏kafka需要⾸先准备scala相关环境。 Java代码

- 1.
- 2.
- 3.
- 4.


> cd kafka-0 > ./sbt update > ./sbt package > ./sbt asembly-package-dependency

其中最后⼀条指令执⾏有可能出现异常，暂且不管。 启动kafka broker： Java代码

1.

- > JMS_PORT= 97 bin/kafka-server-start.sh config/server.properties &

broker.id=1 port=9093

#其他配置和kafka-0保持⼀致

- > JMS_PORT= 98 bin/kafka-server-start.sh config/server.properties &


因为zokeper环境已经正常运⾏了，我们⽆需通过kafka来挂载启动zokeper.如果你的⼀台机器 上部署了多个kafka broker，你需要声明JMS_PORT.

2) kafka-1 Java代码

- 1.
- 2.
- 3.


然后和kafka-0⼀样执⾏打包命令，然后启动此broker. Java代码

1.

仍然可以通过如下指令查看topic的"partition"/"replicas"的分布和存活情况. Java代码

- 1.
- 2.
- 3.


> bin/kafka-list-topic.sh-zokeper localhost:2181 topic: my-replicated-topic partition: 0 leader: 2 replicas: 1,2,0 isr: 2 topic: test partition: 0 leader: 0 replicas: 0 isr: 0

到⽬前为⽌环境已经OK了,那我们就开始展示编程实例吧。[ ]

配置参数详解

三.项⽬准备

项⽬基于maven构建，不得不说kafka java客户端实在是太糟糕了；构建环境会遇到很多麻烦。建议 参考如下pom.xml;其中各个依赖包必须版本协调⼀致。如果kafka client的版本和kafka server的版本不 ⼀致,将会有很多异常,⽐如"broker id not exists"等;因为kafka从0.7升级到0.8之后(正名为2.8.0),client 与server通讯的protocol已经改变. Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


<dependencies>

<dependency> <groupId>log4j</groupId> <artifactId>log4j</artifactId> <version>1.2.14</version>

</dependency> <dependency>

<groupId>org.apache.kafka</groupId> <artifactId>kafka_2.8.2</artifactId> <version>0.8.0</version> <exclusions>

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


<exclusion> <groupId>log4j</groupId> <artifactId>log4j</artifactId>

</exclusion>

</exclusions> </dependency> <dependency>

<groupId>org.scala-lang</groupId> <artifactId>scala-library</artifactId> <version>2.8.2</version>

</dependency> <dependency>

<groupId>com.ya mer.metrics</groupId> <artifactId>metrics-core</artifactId> <version>2.2.0</version>

</dependency> <dependency>

<groupId>com.101tec</groupId> <artifactId>zkclient</artifactId> <version>0.3</version>

</dependency> </dependencies>

四.Producer端代码

1) producer.properties⽂件：此⽂件放在/resources⽬录下 Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


#partitioner.clas= #broker列表可以为kafka server的⼦集,因为producer需要从broker中获取metadata #尽管每个broker都可以提供metadata,此处还是建议,将所有broker都列举出来

metadata.broker.list=127.0.0.1:9092,127.0.0.1:9093 #,127.0.0.1:9093 #同步,建议为async

producer.type=sync

compresion.codec=0 serializer.clas=kafka.serializer.StringEncoder

- 9.
- 10.
- 11.


#在producer.type=async时有效 #batch.num.mesages=10

2) LogProducer.java代码样例 Java代码

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


package com.test.kafka;

import java.util.ArayList; import java.util.Colection; import java.util.List; import java.util.Properties;

import kafka.javapi.producer.Producer; import kafka.producer.KeyedMesage; import kafka.producer.ProducerConfig; publicclas LogProducer {

private Producer<String,String> i ner; public LogProducer() throws Exception{

Properties properties = new Properties(); properties.load(ClasLoader.getSystemResourceAsStream("producer.properties"); ProducerConfig config = new ProducerConfig(properties); i ner = new Producer<String, String>(config);

}

publicvoid send(String topicName,String mesage) { if(topicName = nul | mesage = nul){ return;

} KeyedMesage<String, String> km = new KeyedMesage<String, String>

(topicName,mesage);/如果具有多个partitions,请使⽤ new KeyedMesage(String topicName,K key,V value).

i ner.send(km); }

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


publicvoid send(String topicName,Colection<String> mesages) { if(topicName = nul | mesages = nul){ return;

} if(mesages.isEmpty(){

return;

} List<KeyedMesage<String, String> kms = new ArayList<KeyedMesage<String, String

>(); for(String entry : mesages){

KeyedMesage<String, String> km = new KeyedMesage<String, String> (topicName,entry);

kms.ad(km);

} i ner.send(kms);

}

publicvoid close(){

i ner.close(); }

/*

- * @param args
- */ publicstaticvoid main(String[] args) {


LogProducer producer = nul; try{

producer = new LogProducer(); int i=0; while(true){

producer.send("test-topic", "this is a sample" + i); i +; Thread.sl ep(2 0);

} }catch(Exception e){

e.printStackTrace(); }finaly{

- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.


if(producer != nul){

producer.close(); }

}

}

}

五.Consumer端

- 1) consumer.properties:⽂件位于/resources⽬录下

Java代码

- 2) LogConsumer.java代码样例


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


zokeper.conect=127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183

#,127.0.0.1:2182,127.0.0.1:2183 # timeout in ms for conecting to zokeper zokeper.conectiontimeout.ms=1 0 #consumer group id group.id=test-group #consumer timeout #consumer.timeout.ms=5 0 auto.comit.enable=true auto.comit.interval.ms=6 0

Java代码

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


package com.test.kafka;

import java.util.HashMap; import java.util.List; import java.util.Map; import java.util.Properties; import java.util.concurent.ExecutorService; import java.util.concurent.Executors;

import kafka.consumer.Consumer; import kafka.consumer.ConsumerConfig; import kafka.consumer.ConsumerIterator; import kafka.consumer.KafkaStream;

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
- 47.
- 48.


import kafka.javapi.consumer.ConsumerConector; import kafka.mesage.MesageAndMetadata; publicclas LogConsumer {

private ConsumerConfig config; private String topic; privateint partitionsNum; private MesageExecutor executor; private ConsumerConector conector; private ExecutorService threadPol; public LogConsumer(String topic,int partitionsNum,MesageExecutor executor) throws Exc

eption{ Properties properties = new Properties(); properties.load(ClasLoader.getSystemResourceAsStream("consumer.properties"); config = new ConsumerConfig(properties); this.topic = topic; this.partitionsNum = partitionsNum; this.executor = executor;

}

publicvoid start() throws Exception{ conector = Consumer.createJavaConsumerConector(config); Map<String,Integer> topics = new HashMap<String,Integer>(); topics.put(topic, partitionsNum); Map<String, List<KafkaStream<byte[], byte[] > streams = conector.createMesageStr

eams(topics); List<KafkaStream<byte[], byte[]> partitions = streams.get(topic); threadPol = Executors.newFixedThreadPol(partitionsNum); for(KafkaStream<byte[], byte[]> partition : partitions){

threadPol.execute(new MesageRuner(partition); }

}

publicvoid close(){ try{ threadPol.shutdownNow();

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


}catch(Exception e){ / }finaly{

conector.shutdown(); }

}

clas MesageRuner implements Runable{ private KafkaStream<byte[], byte[]> partition;

MesageRuner(KafkaStream<byte[], byte[]> partition) {

this.partition = partition; }

publicvoid run(){ ConsumerIterator<byte[], byte[]> it = partition.iterator(); while(it.hasNext(){

/conector.comitOfsets();⼿动提交ofset,当autocomit.enable=false时使 ⽤

MesageAndMetadata<byte[],byte[]> item = it.next(); System.out.println("partiton:" + item.partition(); System.out.println("ofset:" + item.ofset(); executor.execute(new String(item.mesage( );/UTF-8,注意异常

} }

}

interface MesageExecutor {

publicvoid execute(String mesage); }

/*

- * @param args
- */ publicstaticvoid main(String[] args) {


- 85.
- 86.
- 87.
- 88.
- 89.
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


LogConsumer consumer = nul; try{

MesageExecutor executor = new MesageExecutor() {

publicvoid execute(String mesage) { System.out.println(mesage);

}

}; consumer = new LogConsumer("test-topic", 2, executor); consumer.start();

}catch(Exception e){

e.printStackTrace(); }finaly{

/ if(consumer != nul){ / consumer.close(); / }

}

}

}

需要提醒的是,上述LogConsumer类中,没有太多的关注异常情况,必须在MesageExecutor.execute() ⽅法中抛出异常时的情况.

在测试时，建议优先启动consumer，然后再启动producer，这样可以实时的观测到最新的消息。
