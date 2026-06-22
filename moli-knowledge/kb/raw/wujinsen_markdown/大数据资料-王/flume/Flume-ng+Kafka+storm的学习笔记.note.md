下⾯介绍下kafka以及kafka和flume的整合 Kafka：

接下来 我们将flume 和kafka进⾏整合：

在flume的source数据源接收到数据后 通过管道 到达sink，我们需要写⼀个kafkaSink 来将sink从 chanel接收的数据作为kafka的⽣产者 将数据 发送给消费者。

具体代码：

publi clas KafkaSink extends AbstractSinkimplementsConfigurable { privatestaticfinal Log loger = LogFactory.getLog(KafkaSink.clas); private Stringtopic; private Producer<String, String>producer;

@Overide public Status proces()throwsEventDeliveryException { Chanel chanel =getChanel();

Transaction tx =chanel.getTransaction(); try {

tx.begin(); Event e = chanel.take();

if(e =nul) {

tx.rolback();

return Status.BACKOF; } KeyedMesage<String,String> data = new KeyedMesage<String, String>

(topic,newString(e.getBody( ); producer.send(data); loger.info("Mesage: {}"+new String( e.getBody( ); tx.comit();

return Status.READY; } catch(Exceptione) { loger.eror("KafkaSinkException:{}",e); tx.rolback(); return Status.BACKOF; } finaly {

tx.close(); }

}

@Overide publicvoid configure(Context context) { topic = "kafka"; Properties props = newProperties(); props.setProperty("metadata.broker.list","x.x.x.x:9092");

props.setProperty("serializer.clas","kafka.serializer.StringEncoder"); / props.setProperty("producer.type", "async"); / props.setProperty("batch.num.mesages", "1");

props.put("request.required.acks","1"); ProducerConfigconfig = new ProducerConfig(props); producer = newProducer<String, String>(config);

} }

将此⽂件打成jar包 传到flume的lib下⾯ 如果你也⽤的是maven的话 需要⽤到asembly 将依赖的jar包 ⼀起打包进去。

在flume的配置是如下：

agent1.sources = source1 agent1.sinks = sink1 agent1.chanels =chanel1 # Describe/configuresource1 agent1.sources.source1.type= avro agent1.sources.source1.bind= localhost agent1.sources.source1.port= 4 # Describe sink1 agent1.sinks.sink1.type=x.x.x.KafkaSink(这是类的路径地址) # Use a chanel whichbufers events in memory agent1.chanels.chanel1.type= memory agent1.chanels.chanel1.capacity= 1 0 agent1.chanels.chanel1.transactionCapactiy= 10 # Bind the source andsink to the chanel agent1.sources.source1.chanels= chanel1 agent1.sinks.sink1.chanel= chanel1

测试的话是avro的⽅式传送数据的 可以这样测试

bin/flume-ng avro-client-conf conf -H localhost -p 4 -F /data/flumetmp/a 这 个为⽂件的地址. 测试的时候在本地 ⼀定要把上⾯写的消费者程序打开 以便接收数据测试是否成功。

/data/flumetmp/a

我们接下来开始整合storm和kafka。 从上⾯的介绍得知storm的spout是负责从外部读取数据的 所以我们需要开发⼀个KafkaSpout 来作为 kafka的消费者和storm的数据接收源。

public clas KafkaSpout implements IRichSpout {

privatestaticfinal Log loger = LogFactory.getLog(KafkaSpout.clas);

/* * */

privatestaticfinalong serialVersionUID = -5698572173547938L;

SpoutOutputColector colector; private ConsumerConectorconsumer; private Stringtopic; public KafkaSpout(String topic) { this.topic = topic;

} @Overide

publicvoid open(Map conf, TopologyContext context, SpoutOutputColector colector) { this.colector = colector;

}

privatestatic ConsumerConfig createConsumerConfig() { Properties props = newProperties(); props.put("zokeper.conect","x.x.x.x:2181"); props.put("group.id","0"); props.put("zokeper.sesion.timeout.ms","1 0");

/props.put("zokeper.sync.time.ms", "20"); /props.put("auto.comit.interval.ms", "1 0");

returnew ConsumerConfig(props); } @Overide

publicvoid close() {

/ TODOAuto-generated method stub

} @Overide

publicvoid activate() { this.consumer = Consumer.createJavaConsumerConector(createConsumerConfig();

Map<String, Integer> topickMap = newHashMap<String, Integer>(); topickMap.put(topic,new Integer(1); Map<String, List<KafkaStream<byte[],byte[] >streamMap =consumer.createMesageStrea

ms(topickMap); KafkaStream<byte[],byte[]>stream = streamMap.get(topic).get(0); ConsumerIterator<byte[],byte[]> it =stream.iterator();

while (it.hasNext() { String value = newString(it.next().mesage(); System.out.println("(consumer)->" + value); colector.emit(new Values(value), value);

}

} @Overide

publicvoid deactivate() {

/ TODOAuto-generated method stub }

privatebolean isComplete; @Overide

publicvoid nextTuple() { } @Overide

publicvoid ack(Object msgId) { / TODOAuto-generated method stub

} @Overide

publicvoid fail(Object msgId) { / TODOAuto-generated method stub

} @Overide

publicvoid declareOutputFields(OutputFieldsDeclarer declarer) {

declarer.declare(new Fields("KafkaSpout"); }

@Overide public Map<String, Object> getComponentConfiguration() {

/ TODOAuto-generated method stub returnul;

}

} publi clas FileBlots implementsIRichBolt{

OutputColector colector; publicvoid prepare(Map stormConf, TopologyContext context,

OutputColector colector) { this.colector = colector;

} publicvoid execute(Tuple input) { String line = input.getString(0);

for(String str : line.split("\s+"){ List a = newArayList(); a.ad(input);

this.colector.emit(a,newValues(str); } this.colector.ack(input); } publicvoid cleanup() { } publicvoid declareOutputFields(OutputFieldsDeclarer declarer) {

declarer.declare(new Fields("words"); }

public Map<String, Object> getComponentConfiguration() {

/ TODOAuto-generated method stub returnul;

}

} publi clas WordsCounterBlots implementsIRichBolt{

OutputColector colector; Map<String, Integer> counter;

publicvoid prepare(Map stormConf, TopologyContext context,

OutputColector colector) { this.colector = colector;

this.counter =new HashMap<String, Integer>(); }

publicvoid execute(Tuple input) { String word = input.getString(0); Integer integer = this.counter.get(word);

if(integer !=nul){

integer +=1; this.counter.put(word, integer);

}else{

this.counter.put(word, 1); } System.out.println("execute"); Jedis jedis = JedisUtils.getJedis(); jedis.incrBy(word, 1); System.out.println(" =");

this.colector.ack(input);

} publicvoid cleanup() { for(Entry<String, Integer> entry :this.counter.entrySet(){

System.out.println(" -:"+entry.getKey()+"="+entry.getValue(); }

} publicvoid declareOutputFields(OutputFieldsDeclarer declarer) { } public Map<String, Object> getComponentConfiguration() {

/ TODOAuto-generated method stub returnul;

} }

Topology测试：

1.

publi clas KafkaTopology { publicstaticvoid main(String[] args) { try {

JedisUtils.initialPol("x.x.x.x", 6379); } catch (Exception e) {

e.printStackTrace();

} TopologyBuilder builder = newTopologyBuilder();

builder.setSpout("kafka",new KafkaSpout("kafka"); builder.setBolt("file-blots",new FileBlots().shufleGrouping("kafka"); builder.setBolt("words-counter",new WordsCounterBlots(),2).fieldsGrouping("file-

blots",new Fields("words"); Config config = new Config(); config.setDebug(true);

LocalCluster local = newLocalCluster(); local.submitTopology("counter", config, builder.createTopology();

} }

⾄此flume + kafka+storm的整合就写完了。注意 这个是 初始学习阶段做的测试 不可正式⽤于线上环 境，在写本⽂之时 已经离测试过去了⼀段时间 所以可能会有些错误 请⻅谅。

