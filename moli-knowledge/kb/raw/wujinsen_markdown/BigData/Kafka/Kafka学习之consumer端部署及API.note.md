1、consumer.properties:⽂件位于/resources⽬录下

zokeper.conect=192.168.0.1 2181test-datacenter/test-server # timeout in ms for conecting to zokeper zokeper.conectiontimeout.ms=1 0 #consumer group id group.id=test-group #consumer timeout #consumer.timeout.ms=5 0

2、JAVA API实现

import java.io.UnsuportedEncodingException; import java.util.List; import java.util.Properties; import java.util.concurent.TimeUnit;

import kafka.consumer.*; import kafka.javapi.consumer.ConsumerConector; import kafka.mesage.MesageAndMetadata;

import org.apache.comons.colections.ColectionUtils;

public clas kafkaConsumer {

public static void main(String[] args) throws InteruptedException, UnsuportedEncodingException {

Properties properties = new Properties(); properties.put("zokeper.conect", "192.168.0.1 2181/test-datacenter/test-server"); properties.put("auto.comit.enable", "true"); properties.put("auto.comit.interval.ms", "6 0"); properties.put("group.id", "test");

ConsumerConfig consumerConfig = new ConsumerConfig(properties);

ConsumerConector javaConsumerConector = Consumer.createJavaConsumerConector(consumerConfig);

/topic的过滤器 Whitelist whitelist = new Whitelist("test"); List<KafkaStream<byte[], byte[]> partitions =

javaConsumerConector.createMesageStreamsByFilter(whitelist);

if (ColectionUtils.isEmpty(partitions) {

System.out.println("empty!"); TimeUnit.SECONDS.sl ep(1);

}

/消费消息 for (KafkaStream<byte[], byte[]> partition : partitions) {

ConsumerIterator<byte[], byte[]> iterator = partition.iterator(); while (iterator.hasNext() {

MesageAndMetadata<byte[], byte[]> next = iterator.next(); System.out.println("partiton:" + next.partition(); System.out.println("ofset:" + next.ofset(); System.out.println("mesage:" + new String(next.mesage(), "utf-8");

} }

} }

