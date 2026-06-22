Kafka简介 Kafka是⼀种分布式的，基于发布/订阅的消息系统。主要设计⽬标如下：

以时间复杂度为O(1)的⽅式提供消息持久化能⼒，即使对TB级以上数据也能保证常数时间的访问性 能

⾼吞吐率。即使在⾮常廉价的商⽤机器上也能做到单机⽀持每秒10K条消息的传输

⽀持Kafka Server间的消息分区，及分布式消费，同时保证每个partition内的消息顺序传输

同时⽀持离线数据处理和实时数据处理

# 为什么要⽤Mesage Queue

解耦在项⽬启动之初来预测将来项⽬会碰到什么需求，是极其困难的。消息队列在处理过程中间插 ⼊了⼀个隐含的、基于数据的接⼝层，两边的处理过程都要实现这⼀接⼝。这允许你独⽴的扩展或 修改两边的处理过程，只要确保它们遵守同样的接⼝约束

冗余有些情况下，处理数据的过程会失败。除⾮数据被持久化，否则将造成丢失。消息队列把数据 进⾏持久化直到它们已经被完全处理，通过这⼀⽅式规避了数据丢失⻛险。在被许多消息队列所采 ⽤的”插⼊-获取-删除”范式中，在把⼀个消息从队列中删除之前，需要你的处理过程明确的指出该 消息已经被处理完毕，确保你的数据被安全的保存直到你使⽤完毕。

扩展性因为消息队列解耦了你的处理过程，所以增⼤消息⼊队和处理的频率是很容易的；只要另外 增加处理过程即可。不需要改变代码、不需要调节参数。扩展就像调⼤电⼒按钮⼀样简单。

灵活性 & 峰值处理能⼒在访问量剧增的情况下，应⽤仍然需要继续发挥作⽤，但是这样的突发流量 并不常⻅；如果为以能处理这类峰值访问为标准来投⼊资源随时待命⽆疑是巨⼤的浪费。使⽤消息 队列能够使关键组件顶住突发的访问压⼒，⽽不会因为突发的超负荷的请求⽽完全崩溃。

可恢复性当体系的⼀部分组件失效，不会影响到整个系统。消息队列降低了进程间的耦合度，所以 即使⼀个处理消息的进程挂掉，加⼊队列中的消息仍然可以在系统恢复后被处理。⽽这种允许重试 或者延后处理请求的能⼒通常是造就⼀个略感不便的⽤户和⼀个沮丧透顶的⽤户之间的区别。

送达保证消息队列提供的冗余机制保证了消息能被实际的处理，只要⼀个进程读取了该队列即可。 在此基础上，IronMQ提供了⼀个”只送达⼀次”保证。⽆论有多少进程在从队列中领取数据，每⼀个 消息只能被处理⼀次。这之所以成为可能，是因为获取⼀个消息只是”预定”了这个消息，暂时把它 移出了队列。除⾮客户端明确的表示已经处理完了这个消息，否则这个消息会被放回队列中去，在 ⼀段可配置的时间之后可再次被处理。

顺序保证在⼤多使⽤场景下，数据处理的顺序都很重要。消息队列本来就是排序的，并且能保证数 据会按照特定的顺序来处理。IronMO保证消息通过FIFO（先进先出）的顺序来处理，因此消息在队 列中的位置就是从队列中检索他们的位置。

缓冲在任何重要的系统中，都会有需要不同的处理时间的元素。例如,加载⼀张图⽚⽐应⽤过滤器花 费更少的时间。消息队列通过⼀个缓冲层来帮助任务最⾼效率的执⾏—写⼊队列的处理会尽可能的 快速，⽽不受从队列读的预备处理的约束。该缓冲有助于控制和优化数据流经过系统的速度。

理解数据流在⼀个分布式系统⾥，要得到⼀个关于⽤户操作会⽤多⻓时间及其原因的总体印象，是 个巨⼤的挑战。消息系列通过消息被处理的频率，来⽅便的辅助确定那些表现不佳的处理过程或领 域，这些地⽅的数据流都不够优化。

异步通信很多时候，你不想也不需要⽴即处理消息。消息队列提供了异步处理机制，允许你把⼀个 消息放⼊队列，但并不⽴即处理它。你想向队列中放⼊多少消息就放多少，然后在你乐意的时候再 去处理它们。

# 常⽤Mesage Queue对⽐

RabitMQRabitMQ是使⽤Erlang编写的⼀个开源的消息队列，本身⽀持很多的协议：AMQP， XMP, SMTP, STOMP，也正因如此，它⾮常重量级，更适合于企业级的开发。同时实现了Broker 构架，这意味着消息在发送给客户端时先在中⼼队列排队。对路由，负载均衡或者数据持久化都有 很好的⽀持。

RedisRedis是⼀个基于Key-Value对的NoSQL数据库，开发维护很活跃。虽然它是⼀个Key-Value 数据库存储系统，但它本身⽀持MQ功能，所以完全可以当做⼀个轻量级的队列服务来使⽤。对于 RabitMQ和Redis的⼊队和出队操作，各执⾏10万次，每10万次记录⼀次执⾏时间。测试数据分 为128Bytes、512Bytes、1K和10K四个不同⼤⼩的数据。实验表明：⼊队时，当数据⽐较⼩时 Redis的性能要⾼于RabitMQ，⽽如果数据⼤⼩超过了10K，Redis则慢的⽆法忍受；出队时，⽆论 数据⼤⼩，Redis都表现出⾮常好的性能，⽽RabitMQ的出队性能则远低于Redis。

ZeroMQZeroMQ号称最快的消息队列系统，尤其针对⼤吞吐量的需求场景。ZMQ能够实现 RabitMQ不擅⻓的⾼级/复杂的队列，但是开发⼈员需要⾃⼰组合多种技术框架，技术上的复杂度 是对这MQ能够应⽤成功的挑战。ZeroMQ具有⼀个独特的⾮中间件的模式，你不需要安装和运⾏⼀ 个消息服务器或中间件，因为你的应⽤程序将扮演了这个服务⻆⾊。你只需要简单的引⽤ZeroMQ 程序库，可以使⽤NuGet安装，然后你就可以愉快的在应⽤程序之间发送消息了。但是ZeroMQ仅 提供⾮持久性的队列，也就是说如果宕机，数据将会丢失。其中，Twiter的Storm 0.9.0以前的版本 中默认使⽤ZeroMQ作为数据流的传输（Storm从0.9版本开始同时⽀持ZeroMQ和Nety作为传输模 块）。

ActiveMQActiveMQ是Apache下的⼀个⼦项⽬。 类似于ZeroMQ，它能够以代理⼈和点对点的技术 实现队列。同时类似于RabitMQ，它少量代码就可以⾼效地实现⾼级应⽤场景。

Kafka/JafkaKafka是Apache下的⼀个⼦项⽬，是⼀个⾼性能跨语⾔分布式发布/订阅消息队列系 统，⽽Jafka是在Kafka之上孵化⽽来的，即Kafka的⼀个升级版。具有以下特性：快速持久化，可以 在O(1)的系统开销下进⾏消息持久化；⾼吞吐，在⼀台普通的服务器上既可以达到10W/s的吞吐速 率；完全的分布式系统，Broker、Producer、Consumer都原⽣⾃动⽀持分布式，⾃动实现复杂均 衡；⽀持Hadop数据并⾏加载，对于像Hadop的⼀样的⽇志数据和离线分析系统，但⼜要求实时 处理的限制，这是⼀个可⾏的解决⽅案。Kafka通过Hadop的并⾏加载机制来统⼀了在线和离线的 消息处理。Apache Kafka相对于ActiveMQ是⼀个⾮常轻量级的消息系统，除了性能⾮常好之外， 还是⼀个⼯作良好的分布式系统。

# Kafka解析

Terminology

BrokerKafka集群包含⼀个或多个服务器，这种服务器被称为broker

Topic每条发布到Kafka集群的消息都有⼀个类别，这个类别被称为topic。（物理上不同topic的消息 分开存储，逻辑上⼀个topic的消息虽然保存于⼀个或多个broker上但⽤户只需指定消息的topic即可 ⽣产或消费数据⽽不必关⼼数据存于何处）

Partitionparition是物理上的概念，每个topic包含⼀个或多个partition，创建topic时可指定parition 数量。每个partition对应于⼀个⽂件夹，该⽂件夹下存储该partition的数据和索引⽂件

Producer负责发布消息到Kafka broker

Consumer消费消息。每个consumer属于⼀个特定的consuer group（可为每个consumer指定 group name，若不指定group name则属于默认的group）。使⽤consumer high level API时，同⼀ topic的⼀条消息只能被同⼀个consumer group内的⼀个consumer消费，但多个consumer group 可同时消费这⼀消息。

Kafka架构

![image 1](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile1.png>)

如上图所示，⼀个典型的kafka集群中包含若⼲producer（可以是web前端产⽣的page view，或者是 服务器⽇志，系统CPU、memory等），若⼲broker（Kafka⽀持⽔平扩展，⼀般broker数量越多，集 群吞吐率越⾼），若⼲consumer group，以及⼀个 集群。Kafka通过Zokeper管理集群配 置，选举leader，以及在consumer group发⽣变化时进⾏rebalance。producer使⽤push模式将消息 发布到broker，consumer使⽤pul模式从broker订阅并消费消息。 Push vs. Pul 作为⼀个mesaging system，Kafka遵循了传统的⽅式，选择由producer向broker push消息并由 consumer从broker pul消息。⼀些loging-centric system，⽐如Facebok的 和Cloudera的

Zokeper

Scribe Flu me

,采⽤⾮常不同的push模式。事实上，push模式和pul模式各有优劣。 push模式很难适应消费速率不同的消费者，因为消息发送速率是由broker决定的。push模式的⽬标是 尽可能以最快速度传递消息，但是这样很容易造成consumer来不及处理消息，典型的表现就是拒绝服 务以及⽹络拥塞。⽽pul模式则可以根据consumer的消费能⼒以适当的速率消费消息。 Topic & Partition

Topic在逻辑上可以被认为是⼀个在的queue，每条消费都必须指定它的topic，可以简单理解为必须指 明把这条消息放进哪个queue⾥。为了使得Kafka的吞吐率可以⽔平扩展，物理上把topic分成⼀个或多 个partition，每个partition在物理上对应⼀个⽂件夹，该⽂件夹下存储这个partition的所有消息和索引 ⽂件。

每个⽇志⽂件都是“log entries”序列，每⼀个log entry包含⼀个4字节整型数（值为N），其后跟N个 字节的消息体。每条消息都有⼀个当前partition下唯⼀的64字节的ofset，它指明了这条消息的起始位 置。磁盘上存储的消费格式如下： mesage length ： 4 bytes (value: 1+4+n) “magic” value ： 1 byte crc ： 4 bytes payload ： n bytes 这个“log entries”并⾮由⼀个⽂件构成，⽽是分成多个segment，每个segment名为该segment第⼀条 消息的ofset和“.kafka”组成。另外会有⼀个索引⽂件，它标明了每个segment下包含的log entry的 ofset范围，如下图所示。

![image 2](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile2.png>)

因为每条消息都被apend到该partition中，是顺序写磁盘，因此效率⾮常⾼（经验证，顺序写磁盘效 率⽐随机写内存还要⾼，这是Kafka⾼吞吐率的⼀个很重要的保证）。 每⼀条消息被发送到broker时，会根据paritition规则选择被存储到哪⼀个partition。如果partition规则 设置的合理，所有消息可以均匀分布到不同的partition⾥，这样就实现了⽔平扩展。（如果⼀个topic 对应⼀个⽂件，那这个⽂件所在的机器I/O将会成为这个topic的性能瓶颈，⽽partition解决了这个问 题）。在创建topic时可以在$KAFKA_HOME/config/server.properties中指定这个partition的数量(如 下所示)，当然也可以在topic创建之后去修改parition数量。 # The default number of log partitions per topic. More partitions alow greater

# paralelism for consumption, but this wil also result in more files acros # the brokers. num.partitions=3

在发送⼀条消息时，可以指定这条消息的key，producer根据这个key和partition机制来判断将这条消 息发送到哪个parition。paritition机制可以通过指定producer的paritition. clas这⼀参数来指定，该 clas必须实现kafka.producer.Partitioner接⼝。本例中如果key可以被解析为整数则将对应的整数 与partition总数取余，该消息会被发送到该数对应的partition。（每个parition都会有个序号） import kafka.producer.Partitioner; import kafka.utils.VerifiableProperties;

public clas JasonPartitioner<T> implements Partitioner { public JasonPartitioner(VerifiableProperties verifiableProperties) {} @Overide public int partition(Object key, int numPartitions) { try {

int partitionNum = Integer.parseInt(String) key); return Math.abs(Integer.parseInt(String) key) % numPartitions);

} catch (Exception e) { return Math.abs(key.hashCode() % numPartitions); } } }

如果将上例中的clas作为partition.clas，并通过如下代码发送20条消息（key分别为0，1，2，3）⾄ topic2（包含4个partition）。 public void sendMesage() throws InteruptedException{ for(int i = 1; i <= 5; i +){

List mesageList = new ArayList<KeyedMesage<String, String>(); for(int j = 0; j < 4; j +）{ mesageList.ad(new KeyedMesage<String, String>

("topic2", j+", "The " + i + " mesage for key " + j); } producer.send(mesageList); }

producer.close(); } 则key相同的消息会被发送并存储到同⼀个partition⾥，⽽且key的序号正好和partition序号相同。 （partition序号从0开始，本例中的key也正好从0开始）。如下图所示。

![image 3](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile3.png>)

对于传统的mesage queue⽽⾔，⼀般会删除已经被消费的消息，⽽Kafka集群会保留所有的消息，⽆ 论其被消费与否。当然，因为磁盘限制，不可能永久保留所有数据（实际上也没必要），因此Kafka提 供两种策略去删除旧数据。⼀是基于时间，⼆是基于partition⽂件⼤⼩。例如可以通过配置

$KAFKA_HOME/config/server.properties，让Kafka删除⼀周前的数据，也可通过配置让Kafka在 partition⽂件超过1GB时删除旧数据，如下所示。

# Log Retention Policy #

# # The folowing configurations control the disposal of log segments. The policy can # be set to delete segments after a period of time, or after a given size has acumulated. # A segment wil be deleted whenever *either* of these criteria are met. Deletion always hapens # from the end of the log. # The minimum age of a log file to be eligible for deletion log.retention.hours=168 # A size-

based retention policy for logs. Segments are pruned from the log as long as the remaining # segments don't drop below log.retention.bytes. #log.retention.bytes=1073741824 # The maximum size of a log segment file. When this size is rea

ched a new log segment wil be created.

log.segment.bytes=1073741824 # The interval at which log segments are checked to se if they c an be deleted acording # to the retention policies

log.retention.check.interval.ms=3 0 # By default the log cleaner is disabled and the log retent

ion policy wil default to #just delete segments after their retention expires. # If log.cleaner.enable=true is set the cleaner wil be enabled and individual logs #can then be marked for log compaction. log.cleaner.enable=false

这⾥要注意，因为Kafka读取特定消息的时间复杂度为O(1)，即与⽂件⼤⼩⽆关，所以这⾥删除⽂件与 Kafka性能⽆关，选择怎样的删除策略只与磁盘以及具体的需求有关。另外，Kafka会为每⼀个 consumer group保留⼀些metadata信息—当前消费的消息的position，也即ofset。这个ofset由 consumer控制。正常情况下consumer会在消费完⼀条消息后线性增加这个ofset。当然，consumer 也可将ofset设成⼀个较⼩的值，重新消费⼀些消息。因为ofet由consumer控制，所以Kafka broker 是⽆状态的，它不需要标记哪些消息被哪些consumer过，不需要通过broker去保证同⼀个consumer group只有⼀个consumer能消费某⼀条消息，因此也就不需要锁机制，这也为Kafka的⾼吞吐率提供了 有⼒保障。

## Replication & Leader election

Kafka从0.8开始提供partition级别的replication，replication的数量可在 $KAFKA_HOME/config/server.properties中配置。 default.replication.factor = 1

该 Replication与leader election配合提供了⾃动的failover机制。replication对Kafka的吞吐率是有⼀定 影响的，但极⼤的增强了可⽤性。默认情况下，Kafka的replication数量为1。每个partition都有⼀个唯 ⼀的leader，所有的读写操作都在leader上完成，leader批量从leader上pul数据。⼀般情况下partition 的数量⼤于等于broker的数量，并且所有partition的leader均匀分布在broker上。folower上的⽇志和 其leader上的完全⼀样。 和⼤部分分布式系统⼀样，Kakfa处理失败需要明确定义⼀个broker是否alive。对于Kafka⽽⾔，Kafka 存活包含两个条件，⼀是它必须维护与Zokeper的sesion(这个通过Zokeper的heartbeat机制来 实现)。⼆是folower必须能够及时将leader的writing复制过来，不能“落后太多”。 leader会track“in sync”的node list。如果⼀个folower宕机，或者落后太多，leader将把它从”in sync” list中移除。这⾥所描述的“落后太多”指folower复制的消息落后于leader后的条数超过预定值，该值可 在$KAFKA_HOME/config/server.properties中配置 #If a replica fals more than this many mesages behind the leader, the leader wil remove the folow er from ISR and treat it as dead

replica.lag.max.mesages=4 0 #If a folower hasn't sent any fetch requests for this window of ti me, the leader wil remove the folower from ISR (in-sync replicas) and treat it as dead

replica.lag.time.max.ms=1 0 需要说明的是，Kafka只解决”fail/recover”，不处理“Byzantine”（“拜占庭”）问题。 ⼀条消息只有被“in sync” list⾥的所有folower都从leader复制过去才会被认为已提交。这样就避免了 部分数据被写进了leader，还没来得及被任何folower复制就宕机了，⽽造成数据丢失（consumer⽆ 法消费这些数据）。⽽对于producer⽽⾔，它可以选择是否等待消息comit，这可以通过 request.required.acks来设置。这种机制确保了只要“in sync” list有⼀个或以上的flolower，⼀条被 comit的消息就不会丢失。 这⾥的复制机制即不是同步复制，也不是单纯的异步复制。事实上，同步复制要求“活着的”folower都 复制完，这条消息才会被认为comit，这种复制⽅式极⼤的影响了吞吐率（⾼吞吐率是Kafka⾮常重 要的⼀个特性）。⽽异步复制⽅式下，folower异步的从leader复制数据，数据只要被leader写⼊log就 被认为已经comit，这种情况下如果folwer都落后于leader，⽽leader突然宕机，则会丢失数据。⽽ Kafka的这种使⽤“in sync” list的⽅式则很好的均衡了确保数据不丢失以及吞吐率。folower可以批量的 从leader复制数据，这样极⼤的提⾼复制性能（批量写磁盘），极⼤减少了folower与leader的差距 （前⽂有说到，只要folower落后leader不太远，则被认为在“in sync” list⾥）。 上⽂说明了Kafka是如何做replication的，另外⼀个很重要的问题是当leader宕机了，怎样在folower中 选举出新的leader。因为folower可能落后许多或者crash了，所以必须确保选择“最新”的folower作为 新的leader。⼀个基本的原则就是，如果leader不在了，新的leader必须拥有原来的leader comit的 所有消息。这就需要作⼀个折衷，如果leader在标明⼀条消息被comit前等待更多的folower确认，那 在它die之后就有更多的folower可以作为新的leader，但这也会造成吞吐率的下降。

⼀种⾮常常⽤的选举leader的⽅式是“majority 灵秀”（“少数服从多数”），但Kafka并未采⽤这种⽅ 式。这种模式下，如果我们有2f+1个replica（包含leader和folower），那在comit之前必须保证有 f+1个replica复制完消息，为了保证正确选出新的leader，fail的replica不能超过f个。因为在剩下的任 意f+1个replica⾥，⾄少有⼀个replica包含有最新的所有消息。这种⽅式有个很⼤的优势，系统的 latency只取决于最快的⼏台server，也就是说，如果replication factor是3，那latency就取决于最快的 那个folower⽽⾮最慢那个。majority vote也有⼀些劣势，为了保证leader election的正常进⾏，它所 能容忍的fail的folower个数⽐较少。如果要容忍1个folower挂掉，必须要有3个以上的replica，如果要 容忍2个folower挂掉，必须要有5个以上的replica。也就是说，在⽣产环境下为了保证较⾼的容错程 度，必须要有⼤量的replica，⽽⼤量的replica⼜会在⼤数据量下导致性能的急剧下降。这就是这种算 法更多⽤在 这种共享集群配置的系统中⽽很少在需要存储⼤量数据的系统中使⽤的原因。 例如HDFS的HA feature是基于 ，但是它的数据存储并没有使⽤这种 expensive的⽅式。 实际上，leader election算法⾮常多，⽐如Zokeper的 , 和 。⽽ Kafka所使⽤的leader election算法更像微软的 算法。 Kafka在Zokeper中动态维护了⼀个ISR（in-sync replicas） set，这个set⾥的所有replica都跟上了 leader，只有ISR⾥的成员才有被选为leader的可能。在这种模式下，对于f+1个replica，⼀个Kafka topic能在保证不丢失已经omit的消息的前提下容忍f个replica的失败。在⼤多数使⽤场景中，这种模 式是⾮常有利的。事实上，为了容忍f个replica的失败，majority vote和ISR在comit前需要等待的 replica数量是⼀样的，但是ISR需要的总的replica的个数⼏乎是majority vote的⼀半。 虽然majority vote与ISR相⽐有不需等待最慢的server这⼀优势，但是Kafka作者认为Kafka可以通过 producer选择是否被comit阻塞来改善这⼀问题，并且节省下来的replica和磁盘使得ISR模式仍然值 得。 上⽂提到，在ISR中⾄少有⼀个folower时，Kafka可以确保已经comit的数据不丢失，但如果某⼀个 partition的所有replica都挂了，就⽆法保证数据不丢失了。这种情况下有两种可⾏的⽅案：

Zokeper

majority-vote-based journal

ZabRaft Viewstamped Replication PacificA

等待ISR中的任⼀个replica“活”过来，并且选它作为leader

选择第⼀个“活”过来的replica（不⼀定是ISR中的）作为leader

这就需要在可⽤性和⼀致性当中作出⼀个简单的平衡。如果⼀定要等待ISR中的replica“活”过来，那不 可⽤的时间就可能会相对较⻓。⽽且如果ISR中的所有replica都⽆法“活”过来了，或者数据都丢失了， 这个partition将永远不可⽤。选择第⼀个“活”过来的replica作为leader，⽽这个replica不是ISR中的 replica，那即使它并不保证已经包含了所有已comit的消息，它也会成为leader⽽作为consumer的数 据源（前⽂有说明，所有读写都由leader完成）。Kafka0.8.*使⽤了第⼆种⽅式。根据Kafka的⽂档， 在以后的版本中，Kafka⽀持⽤户通过配置选择这两种⽅式中的⼀种，从⽽根据不同的使⽤场景选择⾼ 可⽤性还是强⼀致性。

上⽂说明了⼀个parition的replication过程，然尔Kafka集群需要管理成百上千个partition，Kafka通过 round-robin的⽅式来平衡partition从⽽避免⼤量partition集中在了少数⼏个节点上。同时Kafka也需要 平衡leader的分布，尽可能的让所有partition的leader均匀分布在不同broker上。另⼀⽅⾯，优化 leadership election的过程也是很重要的，毕竟这段时间相应的partition处于不可⽤状态。⼀种简单的 实现是暂停宕机的broker上的所有partition，并为之选举leader。实际上，Kafka选举⼀个broker作为 controler，这个controler通过watch Zokeper检测所有的broker failure，并负责为所有受影响的 parition选举leader，再将相应的leader调整命令发送⾄受影响的broker，过程如下图所示。

![image 4](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile4.png>)

这样做的好处是，可以批量的通知leadership的变化，从⽽使得选举过程成本更低，尤其对⼤量的 partition⽽⾔。如果controler失败了，幸存的所有broker都会尝试在Zokeper中创建/controler-> {this broker id}，如果创建成功（只可能有⼀个创建成功），则该broker会成为controler，若创建不 成功，则该broker会等待新controler的命令。

![image 5](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile5.png>)

# Consumer group

（本节所有描述都是基于consumer hight level API⽽⾮low level API）。 每⼀个consumer实例都属于⼀个consumer group，每⼀条消息只会被同⼀个consumer group⾥的⼀ 个consumer实例消费。（不同consumer group可以同时消费同⼀条消息）

![image 6](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile6.png>)

很多传统的mesage queue都会在消息被消费完后将消息删除，⼀⽅⾯避免重复消费，另⼀⽅⾯可以 保证queue的⻓度⽐较少，提⾼效率。⽽如上⽂所将，Kafka并不删除已消费的消息，为了实现传统 mesage queue消息只被消费⼀次的语义，Kafka保证保证同⼀个consumer group⾥只有⼀个 consumer会消费⼀条消息。与传统mesage queue不同的是，Kafka还允许不同consumer group同时 消费同⼀条消息，这⼀特性可以为消息的多元化处理提供了⽀持。实际上，Kafka的设计理念之⼀就是 同时提供离线处理和实时处理。根据这⼀特性，可以使⽤Storm这种实时流处理系统对消息进⾏实时在 线处理，同时使⽤Hadop这种批处理系统进⾏离线处理，还可以同时将数据实时备份到另⼀个数据中 ⼼，只需要保证这三个操作所使⽤的consumer在不同的consumer group即可。下图展示了Kafka在 Linkedin的⼀种简化部署。

![image 7](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile7.png>)

为了更清晰展示Kafka consumer group的特性，笔者作了⼀项测试。创建⼀个topic (名为topic1)，创 建⼀个属于group1的consumer实例，并创建三个属于group2的consumer实例，然后通过producer向 topic1发送key分别为1，2，3r的消息。结果发现属于group1的consumer收到了所有的这三条消息，同 时group2中的3个consumer分别收到了key为1，2，3的消息。如下图所示。

![image 8](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile8.png>)

# Consumer Rebalance

（本节所讲述内容均基于Kafka consumer high level API） Kafka保证同⼀consumer group中只有⼀个consumer会消息某条消息，实际上，Kafka保证的是稳定 状态下每⼀个consumer实例只会消费某⼀个或多个特定partition的数据，⽽某个partition的数据只会 被某⼀个特定的consumer实例所消费。这样设计的劣势是⽆法让同⼀个consumer group⾥的 consumer均匀消费数据，优势是每个consumer不⽤都跟⼤量的broker通信，减少通信开销，同时也 降低了分配难度，实现也更简单。另外，因为同⼀个partition⾥的数据是有序的，这种设计可以保证每 个partition⾥的数据也是有序被消费。 如果某consumer group中consumer数量少于partition数量，则⾄少有⼀个consumer会消费多个 partition的数据，如果consumer的数量与partition数量相同，则正好⼀个consumer消费⼀个partition 的数据，⽽如果consumer的数量多于partition的数量时，会有部分consumer⽆法消费该topic下任何 ⼀条消息。 如下例所示，如果topic1有0，1，2共三个partition，当group1只有⼀个consumer(名为consumer1) 时，该 consumer可消费这3个partition的所有数据。

![image 9](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile9.png>)

增加⼀个consumer(consumer2)后，其中⼀个consumer（consumer1）可消费2个partition的数据， 另外⼀个consumer(consumer2)可消费另外⼀个partition的数据。

![image 10](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile10.png>)

再增加⼀个consumer(consumer3)后，每个consumer可消费⼀个partition的数据。consumer1消费 partition0，consumer2消费partition1，consumer3消费partition2

![image 11](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile11.png>)

再增加⼀个consumer（consumer4）后，其中3个consumer可分别消费⼀个partition的数据，另外⼀ 个consumer（consumer4）不能消费topic1任何数据。

![image 12](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile12.png>)

此时关闭consumer1，剩下的consumer可分别消费⼀个partition的数据。

![image 13](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile13.png>)

接着关闭consumer2，剩下的consumer3可消费2个partition，consumer4可消费1个partition。

![image 14](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile14.png>)

再关闭consumer3，剩下的consumer4可同时消费topic1的3个partition。

![image 15](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile15.png>)

consumer rebalance算法如下：

Sort PT (al partitions in topic T)

Sort CG(al consumers in consumer group G)

Let i be the index position of Ci in CG and let N=size(PT)/size(CG)

Remove curent entries owned by Ci from the partition owner registry

Asign partitions from iN to (i+1)N-1 to consumer Ci

Ad newly asigned partitions to the partition owner registry

⽬前consumer rebalance的控制策略是由每⼀个consumer通过Zokeper完成的。具体的控制⽅式如 下：

Register itself in the consumer id registry under its group.

Register a watch on changes under the consumer id registry.

Register a watch on changes under the broker id registry.

If the consumer creates a mesage stream using a topic filter, it also registers a watch on changes under the broker topic registry.

Force itself to rebalance within in its consumer group.

在这种策略下，每⼀个consumer或者broker的增加或者减少都会触发consumer rebalance。因为每个 consumer只负责调整⾃⼰所消费的partition，为了保证整个consumer group的⼀致性，所以当⼀个 consumer触发了rebalance时，该consumer group内的其它所有consumer也应该同时触发 rebalance。 ⽬前（2015-01-19）最新版（0.8.2）Kafka采⽤的是上述⽅式。但该⽅式有不利的⽅⾯：

Herd efect任何broker或者consumer的增减都会触发所有的consumer的rebalance

Split Brain每个consumer分别单独通过Zokeper判断哪些partition down了，那么不同consumer 从Zokeper“看”到的view就可能不⼀样，这就会造成错误的reblance尝试。⽽且有可能所有的 consumer都认为rebalance已经完成了，但实际上可能并⾮如此。

根据Kafka官⽅⽂档，Kafka作者正在考虑在还未发布的 。⼤ 体思想是选举出⼀个broker作为cordinator，由它watch Zokeper，从⽽判断是否有partition或者 consumer的增减，然后⽣成rebalance命令，并检查是否这些rebalance在所有相关的consumer中被 执⾏成功，如果不成功则重试，若成功则认为此次rebalance成功（这个过程跟replication controler⾮ 常类似，所以我很奇怪为什么当初设计replication controler时没有使⽤类似⽅式来解决consumer rebalance的问题）。流程如下：

0.9.x版本中使⽤中⼼协调器(cordinator)

![image 16](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile16.png>)

# 消息Deliver guarante

通过上⽂介绍，想必读者已经明天了producer和consumer是如何⼯作的，以及Kafka是如何做 replication的，接下来要讨论的是Kafka如何确保消息在producer和consumer之间传输。有这么⼏种 可能的delivery guarante：

At most once 消息可能会丢，但绝不会重复传输 At least one 消息绝不会丢，但可能会重复传输

Exactly once 每条消息肯定会被传输⼀次且仅传输⼀次，很多时候这是⽤户所想要的。Kafka的 delivery guarante semantic⾮常直接。当producer向broker发送消息时，⼀旦这条消息被 comit，因数replication的存在，它就不会丢。但是如果producer发送数据给broker后，遇到的⽹ 络问题⽽造成通信中断，那producer就⽆法判断该条消息是否已经comit。这⼀点有点像向⼀个⾃ 动⽣成primary key的数据库表中插⼊数据。虽然Kafka⽆法确定⽹络故障期间发⽣了什么，但是 producer可以⽣成⼀种类似于primary key的东⻄，发⽣故障时幂等性的retry多次，这样就做到了 Exactly one。截⽌到⽬前(Kafka 0.8.2版本，2015-01-25)，这⼀feature还并未实现，有希望在 Kafka未来的版本中实现。（所以⽬前默认情况下⼀条消息从producer和broker是确保了At least once，但可通过设置producer异步发送实现At most once）。接下来讨论的是消息从broker到 consumer的delivery guarante semantic。（仅针对Kafka consumer high level API）。consumer 在从broker读取消息后，可以选择comit，该操作会在Zokeper中存下该consumer在该partition 下读取的消息的ofset。该consumer下⼀次再读该partition时会从下⼀条开始读取。如未comit， 下⼀次读取的开始位置会跟上⼀次comit之后的开始位置相同。当然可以将consumer设置为 autocomit，即consumer⼀旦读到数据⽴即⾃动comit。如果只讨论这⼀读取消息的过程，那 Kafka是确保了Exactly once。但实际上实际使⽤中consumer并⾮读取完数据就结束了，⽽是要进 ⾏进⼀步处理，⽽数据处理与comit的顺序在很⼤程度上决定了消息从broker和consumer的 delivery guarante semantic。

读完消息先comit再处理消息。这种模式下，如果consumer在comit后还没来得及处理消息就 crash了，下次重新开始⼯作后就⽆法读到刚刚已提交⽽未处理的消息，这就对应于At most once

读完消息先处理再comit。这种模式下，如果处理完了消息在comit之前consumer crash了，下 次重新开始⼯作时还会处理刚刚未comit的消息，实际上该消息已经被处理过了。这就对应于At least once。在很多情况使⽤场景下，消息都有⼀个primary key，所以消息的处理往往具有幂等 性，即多次处理这⼀条消息跟只处理⼀次是等效的，那就可以认为是Exactly once。（⼈个感觉这 种说法有些牵强，毕竟它不是Kafka本身提供的机制，⽽且primary key本身不保证操作的幂等性。 ⽽且实际上我们说delivery guarante semantic是讨论被处理多少次，⽽⾮处理结果怎样，因为处 理⽅式多种多样，我们的系统不应该把处理过程的特性—如是否幂等性，当成Kafka本身的 feature）

如果⼀定要做到Exactly once，就需要协调ofset和实际操作的输出。精典的做法是引⼊两阶段提 交。如果能让ofset和操作输⼊存在同⼀个地⽅，会更简洁和通⽤。这种⽅式可能更好，因为许多 输出系统可能不⽀持两阶段提交。⽐如，consumer拿到数据后可能把数据放到HDFS，如果把最新 的ofset和数据本身⼀起写到HDFS，那就可以保证数据的输出和ofset的更新要么都完成，要么都 不完成，间接实现Exactly once。（⽬前就high level API⽽⾔，ofset是存于Zokeper中的，⽆ 法存于HDFS，⽽low level API的ofset是由⾃⼰去维护的，可以将之存于HDFS中）总之，Kafka默 认保证At least once，并且允许通过设置producer异步提交来实现At most once。⽽Exactly once要求与⽬标存储系统协作，幸运的是Kafka提供的ofset可以使⽤这种⽅式⾮常直接⾮常容易。

# Benchmark

纸上得来终觉浅，绝知些事要躬⾏。笔者希望能亲⾃测⼀下Kafka的性能，⽽⾮从⽹上找⼀些测试数 据。所以笔者曾在0.8发布前两个⽉做过详细的Kafka0.8性能测试，不过很可惜测试报告不慎丢失。所 幸在⽹上找到了Kafka的创始⼈之⼀的 。以下描述皆基于该benchmark。（该 benchmark基于Kafka0.8.1） 测试环境 该benchmark⽤到了六台机器，机器配置如下

Jay Kreps的bechmark

Intel Xeon 2.5 GHz procesor with six cores

Six 720 RPM SATA drives

32GB of RAM

1Gb Ethernet这6台机器其中3台⽤来搭建Kafka broker集群，另外3台⽤来安装Zokeper及⽣成测 试数据。6个drive都直接以⾮RAID⽅式挂载。实际上kafka对机器的需求与Hadop的类似。

producer吞吐率 该项测试只测producer的吞吐率，也就是数据只被持久化，没有consumer读数据。

# 1个producer线程，⽆replication

在这⼀测试中，创建了⼀个包含6个partition且没有replication的topic。然后通过⼀个线程尽可能快的 ⽣成50 milion条⽐较短（payload10字节⻓）的消息。测试结果是821,57 records/second （78.3MB/second）。 之所以使⽤短消息，是因为对于消息系统来说这种使⽤场景更难。因为如果使⽤MB/second来表征吞 吐率，那发送⻓消息⽆疑能使得测试结果更好。 整个测试中，都是⽤每秒钟delivery的消息的数量乘以payload的⻓度来计算MB/second的，没有把消 息的元信息算在内，所以实际的⽹络使⽤量会⽐这个⼤。对于本测试来说，每次还需传输额外的 2个 字节，包括⼀个可选的key，消息⻓度描述，CRC等。另外，还包含⼀些请求相关的overhead，⽐如 topic，partition，acknowledgement等。这就导致我们⽐较难判断是否已经达到⽹卡极限，但是把这 些overhead都算在吞吐率⾥⾯应该更合理⼀些。因此，我们已经基本达到了⽹卡的极限。 初步观察此结果会认为它⽐⼈们所预期的要⾼很多，尤其当考虑到Kafka要把数据持久化到磁盘当中。 实际上，如果使⽤随机访问数据系统，⽐如RDBMS，或者key-velue store，可预期的最⾼访问频率⼤ 概是5 0到5 0个请求每秒，这和⼀个好的RPC层所能接受的远程请求量差不多。⽽该测试中远超 于此的原因有两个。

Kafka确保写磁盘的过程是线性磁盘I/O，测试中使⽤的6块廉价磁盘线性I/O的最⼤吞吐量是 82MB/second，这已经远⼤于1Gb⽹卡所能带来的吞吐量了。许多消息系统把数据持久化到磁盘 当成是⼀个开销很⼤的事情，这是因为他们对磁盘的操作都不是线性I/O。

在每⼀个阶段，Kafka都尽量使⽤批量处理。如果想了解批处理在I/O操作中的重要性，可以参考 David Paterson的”Latency Lags Bandwidth“

1个producer线程，3个异步replication

该项测试与上⼀测试基本⼀样，唯⼀的区别是每个partition有3个replica（所以⽹络传输的和写⼊磁盘 的总的数据量增加了3倍）。每⼀个broker即要写作为leader的partition，也要读（从leader读数据） 写（将数据写到磁盘）作为folower的partition。测试结果为786,980 records/second （75.1MB/second）。 该项测试中replication是异步的，也就是说broker收到数据并写⼊本地磁盘后就acknowledge producer，⽽不必等所有replica都完成replication。也就是说，如果leader crash了，可能会丢掉⼀些 最新的还未备份的数据。但这也会让mesage acknowledgement延迟更少，实时性更好。 这项测试说明，replication可以很快。整个集群的写能⼒可能会由于3倍的replication⽽只有原来的三 分之⼀，但是对于每⼀个producer来说吞吐率依然⾜够好。

1个producer线程，3个同步replication

该项测试与上⼀测试的唯⼀区别是replication是同步的，每条消息只有在被in sync集合⾥的所有 replica都复制过去后才会被置为comited（此时broker会向producer发送acknowledgement）。在 这种模式下，Kafka可以保证即使leader crash了，也不会有数据丢失。测试结果为421,823 records/second（40.2MB/second）。 Kafka同步复制与异步复制并没有本质的不同。leader会始终track folower replica从⽽监控它们是否还 alive，只有所有in sync集合⾥的replica都acknowledge的消息才可能被consumer所消费。⽽对 folower的等待影响了吞吐率。可以通过增⼤batch size来改善这种情况，但为了避免特定的优化⽽影 响测试结果的可⽐性，本次测试并没有做这种调整。

3个producer,3个异步replication

该测试相当于把上⽂中的1个producer,复制到了3台不同的机器上（在1台机器上跑多个实例对吞吐率的 增加不会有太⼤帮忙，因为⽹卡已经基本饱和了），这3个producer同时发送数据。整个集群的吞吐率 为2,024,032 records/second（193,0MB/second）。 Producer Throughput Vs. Stored Data 消息系统的⼀个潜在的危险是当数据能都存于内存时性能很好，但当数据量太⼤⽆法完全存于内存中 时（然后很多消息系统都会删除已经被消费的数据，但当消费速度⽐⽣产速度慢时，仍会造成数据的 堆积），数据会被转移到磁盘，从⽽使得吞吐率下降，这⼜反过来造成系统⽆法及时接收数据。这样 就⾮常糟糕，⽽实际上很多情景下使⽤queue的⽬的就是解决数据消费速度和⽣产速度不⼀致的问题。 但Kafka不存在这⼀问题，因为Kafka始终以O（1）的时间复杂度将数据持久化到磁盘，所以其吞吐率 不受磁盘上所存储的数据量的影响。为了验证这⼀特性，做了⼀个⻓时间的⼤数据量的测试，下图是 吞吐率与数据量⼤⼩的关系图。

![image 17](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile17.png>)

上图中有⼀些variance的存在，并可以明显看到，吞吐率并不受磁盘上所存数据量⼤⼩的影响。实际上 从上图可以看到，当磁盘数据量达到1TB时，吞吐率和磁盘数据只有⼏百MB时没有明显区别。 这个variance是由Linux I/O管理造成的，它会把数据缓存起来再批量flush。上图的测试结果是在⽣产 环境中对Kafka集群做了些tuning后得到的，这些tuning⽅法可参考 。 consumer吞吐率 需要注意的是，replication factor并不会影响consumer的吞吐率测试，因为consumer只会从每个 partition的leader读数据，⽽与replicaiton factor⽆关。同样，consumer吞吐率也与同步复制还是异步 复制⽆关。

这⾥

# 1个consumer

该测试从有6个partition，3个replication的topic消费50 milion的消息。测试结果为940,521 records/second（89.7MB/second）。 可以看到，Kafkar的consumer是⾮常⾼效的。它直接从broker的⽂件系统⾥读取⽂件块。Kafka使⽤

s endfile API

来直接通过操作系统直接传输，⽽不⽤把数据拷⻉到⽤户空间。该项测试实际上从log的起 始处开始读数据，所以它做了真实的I/O。在⽣产环境下，consumer可以直接读取producer刚刚写下 的数据（它可能还在缓存中）。实际上，如果在⽣产环境下跑 ，你可以看到基本上没有物理 “读”。也就是说⽣产环境下consumer的吞吐率会⽐该项测试中的要⾼。

I/O stat

# 3个consumer

将上⾯的consumer复制到3台不同的机器上，并且并⾏运⾏它们（从同⼀个topic上消费数据）。测试 结果为2,615,968 records/second（249.5MB/second）。 正如所预期的那样，consumer的吞吐率⼏乎线性增涨。 Producer and Consumer 上⾯的测试只是把producer和consumer分开测试，⽽该项测试同时运⾏producer和consumer，这更 接近使⽤场景。实际上⽬前的replication系统中folower就相当于consumer在⼯作。 该项测试，在具有6个partition和3个replica的topic上同时使⽤1个producer和1个consumer，并且使⽤ 异步复制。测试结果为795,064 records/second（75.8MB/second）。

可以看到，该项测试结果与单独测试1个producer时的结果⼏乎⼀致。所以说consumer⾮常轻量级。 消息⻓度对吞吐率的影响 上⾯的所有测试都基于短消息（payload 10字节），⽽正如上⽂所说，短消息对Kafka来说是更难处 理的使⽤⽅式，可以预期，随着消息⻓度的增⼤，records/second会减⼩，但MB/second会有所提 ⾼。下图是records/second与消息⻓度的关系图。

![image 18](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile18.png>)

正如我们所预期的那样，随着消息⻓度的增加，每秒钟所能发送的消息的数量逐渐减⼩。但是如果看 每秒钟发送的消息的总⼤⼩，它会随着消息⻓度的增加⽽增加，如下图所示。

![image 19](<发布&订阅的消息系统 Kafka的深度解析.note_images/imageFile19.png>)

从上图可以看出，当消息⻓度为10字节时，因为要频繁⼊队，花了太多时间获取锁，CPU成了瓶颈， 并不能充分利⽤带宽。但从10字节开始，我们可以看到带宽的使⽤逐渐趋于饱和（虽然MB/second还 是会随着消息⻓度的增加⽽增加，但增加的幅度也越来越⼩）。 端到端的Latency 上⽂中讨论了吞吐率，那消息传输的latency如何呢？也就是说消息从producer到consumer需要多少 时间呢？该项测试创建1个producer和1个consumer并反复计时。结果是，2 ms (median), 3ms (9th percentile, 14ms (9.9th percentile)。 （这⾥并没有说明topic有多少个partition，也没有说明有多少个replica，replication是同步还是异步。 实际上这会极⼤影响producer发送的消息被comit的latency，⽽只有comited的消息才能被 consumer所消费，所以它会最终影响端到端的latency）

# 重现该benchmark

如果读者想要在⾃⼰的机器上重现本次benchmark测试，可以参考 。 实际上Kafka Distribution提供了producer性能测试⼯具，可通过bin/kafka-producer-perf-test.sh 脚本来启动。所使⽤的命令如下

本次测试的配置和所使⽤的命令

Producer Setup bin/kafka-topics.sh-zokeper esv4-hcl197.grid.linkedin.com:2181-createtopic test-rep-one-partitions 6-replication-factor 1 bin/kafka-topics.sh-zokeper esv4-

- hcl197.grid.linkedin.com:2181-create-topic test -partitions 6-replication-factor 3 Single thread, no replication bin/kafka-run-

clas.sh org.apache.kafka.clients.tols.ProducerPerformance test7 5 0 10 -1 acks=1 bots trap.servers=esv4-

- hcl198.grid.linkedin.com:9092 bufer.memory=6710864 batch.size=8196 Singlethread, async 3x replication bin/kafktopics.sh-zokeper esv4-hcl197.grid.linkedin.com:2181create-topic test -partitions 6-replication-factor 3 bin/kafka-runclas.sh org.apache.kafka.clients.tols.ProducerPerformance test6 5 0 10 -1 acks=1 bots trap.servers=esv4hcl198.grid.linkedin.com:9092 bufer.memory=6710864 batch.size=8196 Singlethread, sync 3x replication bin/kafka-runclas.sh org.apache.kafka.clients.tols.ProducerPerformance test 5 0 10 -1 acks=-1 bots trap.servers=esv4hcl198.grid.linkedin.com:9092 bufer.memory=6710864 batch.size=64 0 Thre Producers, 3x async replication bin/kafka-runclas.sh org.apache.kafka.clients.tols.ProducerPerformance test 5 0 10 -1 acks=1 botstr ap.servers=esv4hcl198.grid.linkedin.com:9092 bufer.memory=6710864 batch.size=8196 Throughput Versus Sto red Data bin/kafka-runclas.sh org.apache.kafka.clients.tols.ProducerPerformance test 5 0 10 -1 acks=1 bo otstrap.servers=esv4hcl198.grid.linkedin.com:9092 bufer.memory=6710864 batch.size=8196 Efect of mesage size


for i in 10 10 1 0 1 0 1 0; do echo" echo $i bin/kafka-runclas.sh org.apache.kafka.clients.tols.ProducerPerformance test $(1 0*1024*1024/$i) $i -1 ack s=1 botstrap.servers=esv4hcl198.grid.linkedin.com:9092 bufer.memory=6710864 batch.size=128 0 done; Consumer

Consumer throughput bin/kafka-consumer-perf-test.sh-zokeper esv4-

- hcl197.grid.linkedin.com:2181-mesages 5 0-topic test -threads 1 3 Consumers On thre servers, run: bin/kafka-consumer-perf-test.sh-zokeper esv4-


- hcl197.grid.linkedin.com:2181-mesages 5 0-topic test -threads 1 End-to-end Latency bin/kafka-run-clas.sh kafka.tols.TestEndToEndLatency esv4-
- hcl198.grid.linkedin.com:9092 esv4-hcl197.grid.linkedin.com:2181 test 5 0 Producer and consumer bin/kafka-run-


clas.sh org.apache.kafka.clients.tols.ProducerPerformance test 5 0 10 -1 acks=1 botstr ap.servers=esv4-

- hcl198.grid.linkedin.com:9092 bufer.memory=6710864 batch.size=8196 bin/kafka-consumerperf-test.sh-zokeper esv4-hcl197.grid.linkedin.com:2181-mesages 5 0-topic test threads 1 broker配置如下


# Server Basics # # The id of the broker. This must be set to a unique integer for each broker.

broker.id=0 # Socket Server Setings #

# # The port the socket server listens on port=9092 # Hostname the broker wil bind to and advertise to producers and consumers. # If not set, the server wil bind to al interfaces and advertise the value returned from # from java.net.InetAdres.getCanonicalHostName(). #host.name=localhost# The number of threads handling network requests num.network.threads=4 # The number of threads doing disk I/O num.io.threads=8 # The send bufer (SO_SNDBUF) used by the socket server socket.send.bufer.bytes=1048576 # The receive bufer (SO_RCVBUF) used by the socket server socket.receive.bufer.bytes=1048576 # The maximum size of a request that the socket server wil

acept (protection against OM) socket.request.max.bytes=10485760 # Log Basics # # # The directory under which to store log files

log.dirs=/grid/a/dfs-data/kafka-logs,/grid/b/dfs-data/kafka-logs,/grid/c/dfs-data/kafkalogs,/grid/d/dfs-data/kafka-logs,/grid/e/dfs-data/kafka-logs,/grid/f/dfs-data/kafka-logs

# The number of logical partitions per topic per server. More partitions alow greater paralelism # for consumption, but also mean more files. num.partitions=8 # Log Flush Policy #

# # The folowing configurations control the flush of data to disk. This is the most # important performance knob in kafka. # There are a few important trade-ofs here:

- # 1. Durability: Unflushed data is at greater risk of los in the event of a crash.
- # 2. Latency: Data is not made available to consumers until it is flushed (which ads latency).
- # 3. Throughput: The flush is generaly the most expensive operation. # The setings below alow one to configure the flush policy to flush data after a period of time or # every N mesages (or both). This can be done globaly and overi den on a per-topic basis.


# Per-topic overides for log.flush.interval.ms #log.flush.intervals.ms.per.topic=topic1 1 0, topic2 3 0 # Log Retention Policy #

# # The folowing configurations control the disposal of log segments. The policy can # be set to delete segments after a period of time, or after a given size has acumulated. # A segment wil be deleted whenever *either* of these criteria are met. Deletion always hapens # from the end of the log. # The minimum age of a log file to be eligible for deletion log.retention.hours=168 # A size-

based retention policy for logs. Segments are pruned from the log as long as the remaining # segments don't drop below log.retention.bytes.

#log.retention.bytes=1073741824 # The maximum size of a log segment file. When this size is rea ched a new log segment wil be created.

log.segment.bytes=536870912 # The interval at which log segments are checked to se if they c an be deleted acording # to the retention policies

log.cleanup.interval.mins=1 # Zokeper #

# # Zokeper conection string (se zokeper docs for details). # This is a coma separated host:port pairs, each coresponding to a zk # server. e.g. "127.0.0.1 3 0,127.0.0.1 301,127.0.0.1 302". # You can also apend an optional chrot string to the urls to specify the # rot directory for al kafka znodes. zokeper.conect=esv4-hcl197.grid.linkedin.com:2181

# Timeout in ms for conecting to zokeper zokeper.conection.timeout.ms=1 0 # metrics reporter properties kafka.metrics.poling.interval.secs=5 kafkakafka.metrics.reporters=kafka.metrics.KafkaCSVMetric

sReporter kafka.csv.metrics.dir=/tmp/kafka_metrics # Disable csv reporting by default.

kafka.csv.metrics.reporter.enabled=false replica.lag.max.mesages=1 0 读者也可参考另外⼀份 参考

Kafka性能测试报告

使⽤消息队列的 10 个理由

Apache Kafka

Eficient data transfer through zero copy

Benchmarking Apache Kafka: 2 Milion Writes Per Second (On Thre Cheap Machines)

Kafka 0.8 Producer Performance

