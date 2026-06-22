今天，磊哥给⼤家科普⼀下，⾯试官经常问的刁钻问题，Kafka 会不会丢消息？怎么处理的?

这个Kafka确实存在丢消息的问题，消息丢失会发⽣在Broker，Producer和Consumer三种。

![image 1](<kafka丢消息处理.note_images/imageFile1.png>)

# Broker

Broker丢失消息是由于Kafka本身的原因造成的，kafka为了得到更⾼的性能和吞吐量，将数据异步批 量的存储在磁盘中。消息的刷盘过程，为了提⾼性能，减少刷盘次数，kafka采⽤了批量刷盘的做法。 即，按照⼀定的消息量，和时间间隔进⾏刷盘。这种机制也是由于linux操作系统决定的。将数据存储 到linux操作系统种，会先存储到⻚缓存（Page cache）中，按照时间或者其他条件进⾏刷盘（从page cache到file），或者通过fsync命令强制刷盘。数据在page cache中时，如果系统挂掉，数据会丢失。

![image 2](<kafka丢消息处理.note_images/imageFile2.png>)

Broker在linux服务器上⾼速读写以及同步到Replica

上图简述了broker写数据以及同步的⼀个过程。broker写数据只写到PageCache中，⽽pageCache位 于内存。这部分数据在断电后是会丢失的。pageCache的数据通过linux的flusher程序进⾏刷盘。刷盘 触发条件有三：

主动调⽤sync或fsync函数

可⽤内存低于阀值

dirty data时间达到阀值。dirty是pagecache的⼀个标识位，当有数据写⼊到pageCache时，pagecache 被标注为dirty，数据刷盘以后，dirty标志清除。

Broker配置刷盘机制，是通过调⽤fsync函数接管了刷盘动作。从单个Broker来看，pageCache的数据 会丢失。

Kafka没有提供同步刷盘的⽅式。同步刷盘在RocketMQ中有实现，实现原理是将异步刷盘的流程进⾏ 阻塞，等待响应，类似ajax的calback或者是java的future。下⾯是⼀段rocketmq的源码。

GroupCo mitRequest request = new GroupCo mitRequest(result.getWroteOfset() + result.getWroteBytes(); service.putRequest(request); bolean flushOK = request.waitForFlush(this.defaultMesageStore.getMesageStoreConfig().getSyncFlushTimeout(); / 刷盘

也就是说，理论上，要完全让kafka保证单个broker不丢失消息是做不到的，只能通过调整刷盘机制的 参数缓解该情况。⽐如，减少刷盘间隔，减少刷盘数据量⼤⼩。时间越短，性能越差，可靠性越好 （尽可能可靠）。这是⼀个选择题。 为了解决该问题，kafka通过producer和broker协同处理单个broker丢失参数的情况。⼀旦producer发 现broker消息丢失，即可⾃动进⾏retry。除⾮retry次数超过阀值（可配置），消息才会丢失。此时需 要⽣产者客户端⼿动处理该情况。那么producer是如何检测到数据丢失的呢？是通过ack机制，类似于 htp的三次握⼿的⽅式。

The number of acknowledgments the producer requires the leader to have received before conside ring a request complete. This controls the durability of records that are sent. The folowing setings are alowed: acks=0 If set to zero then the producer wil not wait for any acknowledgment from the

server at al. The record wil be i mediately aded to the socket bufer and considered sent. No g uarante can be made that the server has received the record in this case, and the retries configur ation wil not take efect (as the client wonʼt generaly know of any failures). The ofset given back f or each record wil always be set to -1. acks=1 This wil mean the leader wil write the record to its lo cal log but wil respond without awaiting ful acknowledgement from al folowers. In this case shoul d the leader fail i mediately after acknowledging the record but before the folowers have replicate

- d it then the record wil be lost. acks=alThis means the leader wil wait for the ful set of in-sync rep licas to acknowledge the record. This guarantes that the record wil not be lost as long as at least

one in-sync replica remains alive. This is the strongest available guarante. This is equivalent to th

- e acks=-1 seting. 以上的引⽤是kafka官⽅对于参数acks的解释（在⽼版本中，该参数是request.required.acks）。


- acks=0，producer不等待broker的响应，效率最⾼，但是消息很可能会丢。

- acks=1，leader broker收到消息后，不等待其他folower的响应，即返回ack。也可以理解为ack数为1。此 时，如果folower还没有收到leader同步的消息leader就挂了，那么消息会丢失。按照上图中的例⼦，如果 leader收到消息，成功写⼊PageCache后，会返回ack，此时producer认为消息发送成功。但此时，按照 上图，数据还没有被同步到folower。如果此时leader断电，数据会丢失。


acks=-1，leader broker收到消息后，挂起，等待所有ISR列表中的folower返回结果后，再返回ack。-1等 效与al。这种配置下，只有leader写⼊数据到pagecache是不会返回ack的，还需要所有的ISR返回“成功” 才会触发ack。如果此时断电，producer可以知道消息没有被发送成功，将会重新发送。如果在folower收 到数据以后，成功返回ack，leader断电，数据将存在于原来的folower中。在重新选举以后，新的leader 会持有该部分数据。数据从leader同步到folower，需要2步：

数据从pageCache被刷盘到disk。因为只有disk中的数据才能被同步到replica。

数据同步到replica，并且replica成功将数据写⼊PageCache。在producer得到ack后，哪怕是所有机 器都停电，数据也⾄少会存在于leader的磁盘内。

上⾯第三点提到了ISR的列表的folower，需要配合另⼀个参数才能更好的保证ack的有效性。ISR是 Broker维护的⼀个“可靠的folower列表”，in-sync Replica列表，broker的配置包含⼀个参数： min.insync.replicas。该参数表示ISR中最少的副本数。如果不设置该值，ISR中的folower列表可能为 空。此时相当于acks=1。

![image 3](<kafka丢消息处理.note_images/imageFile3.png>)

如上图中：

- acks=0，总耗时f(t) = f(1)。

- acks=1，总耗时f(t) = f(1) + f(2)。


acks=-1，总耗时f(t) = f(1) + max( f(A) , f(B) ) + f(2)。

性能依次递减，可靠性依次升⾼。

Producer

Producer丢失消息，发⽣在⽣产者客户端。 为了提升效率，减少IO，producer在发送数据时可以将多个请求进⾏合并后发送。被合并的请求咋发 送⼀线缓存在本地bufer中。缓存的⽅式和前⽂提到的刷盘类似，producer可以将请求打包成“块”或者 按照时间间隔，将bufer中的数据发出。通过bufer我们可以将⽣产者改造为异步的⽅式，⽽这可以提 升我们的发送效率。 但是，bufer中的数据就是危险的。在正常情况下，客户端的异步调⽤可以通过calback来处理消息发 送失败或者超时的情况，但是，⼀旦producer被⾮法的停⽌了，那么bufer中的数据将丢失，broker将 ⽆法收到该部分数据。⼜或者，当Producer客户端内存不够时，如果采取的策略是丢弃消息（另⼀种 策略是block阻塞），消息也会被丢失。抑或，消息产⽣（异步产⽣）过快，导致挂起线程过多，内存 不⾜，导致程序崩溃，消息丢失。

![image 4](<kafka丢消息处理.note_images/imageFile4.png>)

producer采取批量发送的示意图

![image 5](<kafka丢消息处理.note_images/imageFile5.png>)

异步发送消息⽣产速度过快的示意图

根据上图，可以想到⼏个解决的思路：

异步发送消息改为同步发送消。或者service产⽣消息时，使⽤阻塞的线程池，并且线程数有⼀定上限。整 体思路是控制消息产⽣速度。

扩⼤Bufer的容量配置。这种⽅式可以缓解该情况的出现，但不能杜绝。

service不直接将消息发送到bufer（内存），⽽是将消息写到本地的磁盘中（数据库或者⽂件），由另⼀ 个（或少量）⽣产线程进⾏消息发送。相当于是在bufer和service之间⼜加了⼀层空间更加富裕的缓冲 层。

Consumer

Consumer消费消息有下⾯⼏个步骤：

接收消息 处理消息 反馈“处理完毕”（comited）

Consumer的消费⽅式主要分为两种：

⾃动提交ofset，Automatic Ofset Comiting

⼿动提交ofset，Manual Ofset Control

Consumer⾃动提交的机制是根据⼀定的时间间隔，将收到的消息进⾏comit。comit过程和消费消 息的过程是异步的。也就是说，可能存在消费过程未成功（⽐如抛出异常），comit消息已经提交 了。此时消息就丢失了。

Properties props =new Properties(); props.put("botstrap.servers","localhost:9092"); props.put("group.id","test");

/ ⾃动提交开关 props.put("enable.auto.co mit","true");

/ ⾃动提交的时间间隔，此处是1s props.put("auto.co mit.interval.ms","1 0"); props.put("key.deserializer","org.apache.kafka.co mon.serialization.StringDeserializer"); props.put("value.deserializer","org.apache.kafka.co mon.serialization.StringDeserializer"); KafkaConsumer<String, String> consumer =new KafkaConsumer<>(props); consumer.subscribe(Arrays.asList("fo","bar"); while (true) {

/ 调⽤pol后，1 0ms后，消息状态会被改为 co mited ConsumerRecords<String, String> records = consumer.pol(10); for (ConsumerRecord<String, String> record : records)

insertIntoDB(record); / 将消息⼊库，时间可能会超过1 0ms

上⾯的示例是⾃动提交的例⼦。如果此时，insertIntoDB(record)发⽣异常，消息将会出现丢失。接下 来是⼿动提交的例⼦：

Properties props =new Properties(); props.put("botstrap.servers","localhost:9092"); props.put("group.id","test"); / 关闭⾃动提交，改为⼿动提交 props.put("enable.auto.co mit","false"); props.put("key.deserializer","org.apache.kafka.co mon.serialization.StringDeserializer"); props.put("value.deserializer","org.apache.kafka.co mon.serialization.StringDeserializer"); KafkaConsumer<String, String> consumer =new KafkaConsumer<>(props); consumer.subscribe(Arrays.asList("fo","bar"); finalint minBatchSize =20; List<ConsumerRecord<String, String> bufer =new ArrayList<>(); while (true) {

/ 调⽤pol后，不会进⾏auto co mit ConsumerRecords<String, String> records = consumer.pol(10); for (ConsumerRecord<String, String> record : records) {

bufer.ad(record);

} if (bufer.size() >= minBatchSize) {

insertIntoDb(bufer);

/ 所有消息消费完毕以后，才进⾏co mit操作 consumer.co mitSync(); bufer.clear();

}

将提交类型改为⼿动以后，可以保证消息“⾄少被消费⼀次”(at least once)。但此时可能出现重复消费 的情况，重复消费不属于本篇讨论范围。 上⾯两个例⼦，是直接使⽤Consumer的High level API，客户端对于ofset等控制是透明的。也可以采 ⽤Low level API的⽅式，⼿动控制ofset，也可以保证消息不丢，不过会更加复杂。

try {

while(runing) { ConsumerRecords<String, String> records = consumer.pol(Long.MAX_VALUE); for (TopicPartition partition : records.partitions() {

List<ConsumerRecord<String, String> partitionRecords = records.records(partition); for (ConsumerRecord<String, String> record : partitionRecords) {

System.out.println(record.ofset() +": " + record.value();

} long lastOfset = partitionRecords.get(partitionRecords.size() -1).ofset();

/ 精确控制ofset

consumer.co mitSync(Colections.singletonMap(partition,new OfsetAndMetadata(lastOfset +1 ); }

} }finaly {

consumer.close(); }

来源 | htps:/blog.dogchao.cn/?p=305

# 近期技术热⽂

- 1、

- 2、

- 3、

- 4、

- 5、


⾯试官欺负⼈：new Object()占⽤⼏个字节？ 不要封装⼯具类了，这款神仙框架，真好⽤！ 退税 = 磊哥发财了？ 磊哥，做副业=9034.75元？ Java 8 中 Map 骚操作之 merge() 的⽤法

