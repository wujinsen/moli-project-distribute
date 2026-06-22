- 1 public class StormKafkaTopo {

- 2 public static void main(String[] args) throws Exception {

- 3 // 配置Zookeeper地址

BrokerHosts brokerHosts = new ZkHosts("node04:2181,node05:2181,node06:2181");

- 4

- 5 // 配置Kafka订阅的Topic，以及zookeeper中数据节点⽬录和名字

SpoutConfig spoutConfig = new SpoutConfig(brokerHosts, "topic1", "/zkkafkaspout" , "kafkaspout");

- 6

- 7

- 8 // 配置KafkaBolt中的kafka.broker.properties

- 9 Config conf = new Config();

- 10 Map<String, String> map = new HashMap<String, String>();

- 11 // 配置Kafka broker地址

- 12 map.put("metadata.broker.list", "node04:9092");

- 13 // serializer.class为消息的序列化类

- 14 map.put("serializer.class", "kafka.serializer.StringEncoder");

- 15 conf.put("kafka.broker.properties", map);

- 16 // 配置KafkaBolt⽣成的topic

- 17 conf.put("topic", "topic2");

- 18

- 19 spoutConfig.scheme = new SchemeAsMultiScheme(new MessageScheme());

- 20 TopologyBuilder builder = new TopologyBuilder();

- 21 builder.setSpout("spout", new KafkaSpout(spoutConfig));

- 22 builder.setBolt("bolt", new SenqueceBolt()).shuffleGrouping("spout");

builder.setBolt("kafkabolt", new KafkaBolt<String, Integer> ()).shuffleGrouping("bolt");

- 23

- 24

- 25 if (args != null && args.length > 0) {

- 26 conf.setNumWorkers(3);

StormSubmitter.submitTopology(args[0], conf, builder.createTopology());

- 27

- 28 } else {

- 29

- 30 LocalCluster cluster = new LocalCluster();

- 31 cluster.submitTopology("Topo", conf, builder.createTopology());

- 32 Utils.sleep(100000);

- 33 cluster.killTopology("Topo");

- 34 cluster.shutdown();

- 35 }

- 36 }


# 37 }

