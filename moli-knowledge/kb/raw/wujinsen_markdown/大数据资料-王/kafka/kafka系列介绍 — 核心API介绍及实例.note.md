⼀ PRODUCER的API

- 1.Producer的创建，依赖于ProducerConfig public Producer(ProducerConfig config);
- 2.单个或是批量的消息发送 public void send(KeyedMesage<K,V> mesage); public void send(List<KeyedMesage<K,V> mesages);
- 3.关闭Producer到所有broker的连接 public void close();⼆ CONSUMER的⾼层API 主要是Consumer和ConsumerConnector，这⾥的Consumer是ConsumerConnector的静态⼯⼚类 class Consumer { public static kafka.javapi.consumer.ConsumerConector createJavaConsumerConector(config: ConsumerConfig);} 具体的消息的消费都是在ConsumerConector中 创建⼀个消息处理的流，包含所有的topic，并根据指定的Decoder public <K,V> Map<String, List<KafkaStream<K,V > createMesageStreams(Map<String, Integer> topicCountMap, Decoder<K> keyDecoder, Decoder<V> valueDecoder); 创建⼀个消息处理的流，包含所有的topic，使⽤默认的Decoder public Map<String, List<KafkaStream<byte[], byte[] > createMesageStreams(Map<String, Integer> topicCountMap); 获取指定消息的topic,并根据指定的Decoder public <K,V> List<KafkaStream<K,V> createMesageStreamsByFilter(TopicFilter topicFilter, int numStreams, Decoder<K> keyDecoder, Decoder<V> valueDecoder); 获取指定消息的topic,使⽤默认的Decoder public List<KafkaStream<byte[], byte[]> createMesageStreamsByFilter(TopicFilter topicFilter); 提交偏移量到这个消费者连接的topic public void comitOfsets(); 关闭消费者 public void shutdown(); ⾼层的API中⽐较常⽤的就是 public List<KafkaStream<byte[], byte[]> createMesageStreamsByFilter(TopicFilter topicFilter); 和 public void comitOfsets();三 CONSUMER的简单API–SIMPLECONSUMER 批量获取消息 public FetchResponse fetch(request: kafka.javapi.FetchRequest); 获取topic的元信息


public kafka.javapi.TopicMetadataResponse send(request: kafka.javapi.TopicMetadataRequest); 获取⽬前可⽤的偏移量 public kafka.javapi.OfsetResponse getOfsetsBefore(request: OfsetRequest); 关闭连接 public void close(); 对于⼤部分应⽤来说，⾼层API就已经⾜够使⽤了，但是若是想做更进⼀步的控制的话，可以使⽤简单 的API，例如消费者重启的情况下，希望得到最新的ofset，就该使⽤SimpleConsumer.四 KAFKA HADOOP CONSUMER API 提供了⼀个可⽔平伸缩的解决⽅案来结合hadop的使⽤参⻅

htps:/github.com/linkedin/camus/tre/camus-kafka-0.8/

五 实战

maven依赖： <dependency> <groupId>org.apache.kafka</groupId> <artifactId>kafka_2.10</artifactId> <version>0.8.0</version> </dependency>

⽣产者代码：

import kafka.javaapi.producer.Producer; import kafka.producer.KeyedMessage; import kafka.producer.ProducerConfig; import java.util.Properties; /**

- * <pre>

- * Created by zhaoming on 14-5-4 下午3:23

- * </pre>

- */


publicclass KafkaProductor { publicstaticvoid main(String[] args) throws InterruptedException { Properties properties = new Properties();

properties.put( "zk.connect" , "127.0.0.1:2181" ); properties.put( "metadata.broker.list" , "localhost:9092" );

properties.put( "serializer.class" , "kafka.serializer.StringEncoder" ProducerConfig producerConfig = new ProducerConfig(properties);

Producer<String, String> producer = new Producer<String, String>(prod // 构建消息体

KeyedMessage<String, String> keyedMessage = new KeyedMessage<String, topic" , "test-message" );

producer.send(keyedMessage); Thread.sleep( 1000 ); producer.close();

} }

# 消费端代码

import java.io.UnsupportedEncodingException; import java.util.List; import java.util.Properties; import java.util.concurrent.TimeUnit; import kafka.consumer.*; import kafka.javaapi.consumer.ConsumerConnector; import kafka.message.MessageAndMetadata; import org.apache.commons.collections.CollectionUtils; /**

- * <pre>

- * Created by zhaoming on 14-5-4 下午3:32

- * </pre>

- */


publicclass kafkaConsumer { publicstaticvoid main(String[] args) throws InterruptedException,

UnsupportedEncodingException {

Properties properties = new Properties(); properties.put( "zookeeper.connect" , "127.0.0.1:2181" ); properties.put( "auto.commit.enable" , "true" ); properties.put( "auto.commit.interval.ms" , "60000" ); properties.put( "group.id" , "test-group" );

ConsumerConfig consumerConfig = new ConsumerConfig(properties); ConsumerConnector javaConsumerConnector =

Consumer.createJavaConsumerConnector(consumerConfig);

//topic的过滤器 Whitelist whitelist = new Whitelist( "test-topic" ); List<KafkaStream< byte [], byte []>> partitions =

javaConsumerConnector.createMessageStreamsByFilter(whitelist);

if (CollectionUtils.isEmpty(partitions)) { System.out.println( "empty!" ); TimeUnit.SECONDS.sleep( 1 ); }

//消费消息

for (KafkaStream< byte [], byte []> partition : partitions) { ConsumerIterator< byte [], byte []> iterator = partition.iterator();

while (iterator.hasNext()) { MessageAndMetadata< byte [], byte []> next = iterator.next(); System.out.println( "partiton:" + next.partition()); System.out.println( "offset:" + next.offset()); System.out.println( "message:" + new String(next.message(), "utf}

} } }

# PS:感觉消费端的API设计实在太难⽤了。

