Kafka的consumer是以pul的形式获取消息数据的。不同于队列和发布-订阅模式，kafka采⽤了 consumer group的模式。通常的，⼀般采⽤⼀个consumer中的⼀个group对应⼀个业务，配合多 个producer提供数据。

![image 1](<Kafka Consumer开发的一些关键点.note_images/imageFile1.png>)

# ⼀. 消费过的数据⽆法再次消费

在user level上，⼀旦消费过topic⾥的数据，那么就⽆法再次⽤同⼀个groupid消费同⼀组数据。 如果想要再次消费数据，要么换另⼀个groupid，要么使⽤镜像：

![image 2](<Kafka Consumer开发的一些关键点.note_images/imageFile2.png>)

此外，low level的api提供了⼀些机制去设置partion和ofset。

# ⼆. ofset管理

kafka会记录ofset到zk中。但是，zk client api对zk的频繁写⼊是⼀个低效的操作。0.8.2 kafka引 ⼊了native ofset storage，将ofset管理从zk移出，并且可以做到⽔平扩展。其原理就是利⽤了 kafka的compacted topic，ofset以consumer group,topic与partion的组合作为key直接提交到 compacted topic中。同时Kafka⼜在内存中维护了的三元组来维护最新的ofset信息，consumer 来取最新ofset信息的时候直接内存⾥拿即可。当然，kafka允许你快速的checkpoint最新的ofset 信息到磁盘上。

# 三. stream

This API is centered around iterators, implemented by the KafkaStream clas. Each KafkaStream represents the stream of mesages from one or more partitions on one or more servers. Each stream is used for single threaded procesing, so the client can provide the number of desired streams in the create cal. Thus a stream may represent the merging of multiple server partitions (to corespond to the number of procesing threads), but each partition only goes to one stream.

根据官⽅⽂档所说，stream即指的是来⾃⼀个或多个服务器上的⼀个或者多个partition的消息。 每⼀个stream都对应⼀个单线程处理。因此，client能够设置满⾜⾃⼰需求的stream数⽬。总之， ⼀个stream也许代表了多个服务器partion的消息的聚合，但是每⼀个 partition都只能到⼀个 stream。

# 四. consumer和partition

- 1.
- 2.
- 3.
- 4.
- 5.


如果consumer⽐partition多，是浪费，因为kafka的设计是在⼀个partition上是不允许并发 的，所以consumer数不要⼤于partition数 如果consumer⽐partition少，⼀个consumer会对应于多个partitions，这⾥主要合理分配 consumer数和partition数，否则会导致partition⾥⾯的数据被取的不均匀 如果consumer从多个partition读到数据，不保证数据间的顺序性，kafka只保证在⼀个 partition上数据是有序的，但多个partition，根据你读的顺序会有不同 增减consumer，broker，partition会导致rebalance，所以rebalance后consumer对应的 partition会发⽣变化 High-level接⼝中获取不到数据的时候是会block的

负载低的情况下可以每个线程消费多个partition。但负载⾼的情况下，Consumer 线程数最好和 Partition数量保持⼀致。如果还是消费不过来，应该再开 Consumer 进程，进程内线程数同样和 分区数⼀致。（多谢 @shadyxu 指出）

# 五. high-level的consumer⼯具

- 1.
- 2.


bin/kafka-run-clas.shkafka.tols.ConsumerOfsetChecker-group pv

可以看到当前group ofset的状况。

bin/kafka-run-clas.shkafka.tols.UpdateOfsetsInZK earliest config/consumer.properties page_visits

3个参数， [earliest | latest]，表示将offset置到哪⾥ consumer.properties ，这⾥是配置⽂件的路径 topic，topic名，这⾥是page_visits

# 六. SimpleConsumer

kafka的low-level接⼝，使⽤场景：

- 1.
- 2.
- 3.


Read a mesage multiple times Consume only a subset of the partitions in a topic in a proces Manage transactions to make sure a mesage is procesed once and only once

⽤这个接⼝需要注意：

- 1.
- 2.
- 3.


You must kep track of the ofsets in your aplication to know where you left of consuming. You must figure out which Broker is the lead Broker for a topic and partition You must handle Broker leader changes

使⽤步骤：

- 1.
- 2.
- 3.
- 4.
- 5.


Find an active Broker and find out which Broker is the leader for your topic and partition： 你必须知道读哪个topic的哪个partition Determine who the replica Brokers are for your topic and partition： 找到负责该partition的 broker leader，从⽽找到存有该partition副本的那个broker Build the request defining what data you are interested in：⾃⼰去写request并fetch数据 Fetch the data Identify and recover from leader changes：还要注意需要识别和处理broker leader的改变

