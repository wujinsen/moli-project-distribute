- 1 public KafkaSpout(SpoutConfig spoutConf) {

- 2 _spoutConfig = spoutConf;

- 3 }


SpoutConfig继承⾃KafkaConfig。由于SpoutConfig和KafkaConfig所有的instance field全是public, 因 此在使⽤构造⽅法后，可以直接设置各个域的值。

- 1 public class SpoutConfig extends KafkaConfig implements Serializable {

- 2 public List<String> zkServers = null; //记录Spout读取进度所⽤的zookeeper的host

- 3 public Integer zkPort = null;//记录进度⽤的zookeeper的端⼝

- 4 public String zkRoot = null;//进度信息记录于zookeeper的哪个路径下

public String id = null;//进度记录的id，想要⼀个新的Spout读取之前的记录，应把它的id 设为跟之前的⼀样。

- 5

- 6 public long stateUpdateIntervalMs = 2000;//⽤于metrics,多久更新⼀次状态。

- 7

public SpoutConfig(BrokerHosts hosts, String topic, String zkRoot, String id) {

- 8

- 9 super(hosts, topic);

- 10 this.zkRoot = zkRoot;

- 11 this.id = id;

- 12 }

- 13 }


- 1 public class KafkaConfig implements Serializable {

- 2

- 3 public final BrokerHosts hosts; //⽤以获取Kafka broker和partition的信息

- 4 public final String topic;//从哪个topic读取消息

- 5 public final String clientId; // SimpleConsumer所⽤的client id

- 6

public int fetchSizeBytes = 1024 * 1024; //发给Kafka的每个FetchRequest中，⽤此 指定想要的response中总的消息的⼤⼩

- 7

- 8 public int socketTimeoutMs = 10000;//与Kafka broker的连接的socket超时时间

- 9 public int fetchMaxWait = 10000; //当服务器没有新消息时，消费者会等待这些时间

public int bufferSizeBytes = 1024 * 1024;//SimpleConsumer所使⽤的 SocketChannel的读缓冲区⼤⼩

- 10

public MultiScheme scheme = new RawMultiScheme();//从Kafka中取出的byte[]，该如 何反序列化

- 11

- 12 public boolean forceFromStart = false;//是否强制从Kafka中offset最⼩的开始读起

public long startOffsetTime = kafka.api.OffsetRequest.EarliestTime();//从何时 的offset时间开始读，默认为最旧的offset

- 13

public long maxOffsetBehind = 100000;//KafkaSpout读取的进度与⽬标进度相差多少，相 差太多，Spout会丢弃中间的消息

- 14

public boolean useStartOffsetTimeIfOffsetOutOfRange = true;//如果所请求的 offset对应的消息在Kafka中不存在，是否使⽤startOffsetTime

- 15

- 16 public int metricsTimeBucketSizeInSecs = 60;//多⻓时间统计⼀次metrics

- 17

- 18 public KafkaConfig(BrokerHosts hosts, String topic) {

- 19 this(hosts, topic, kafka.api.OffsetRequest.DefaultClientId());

- 20 }

- 21

- 22 public KafkaConfig(BrokerHosts hosts, String topic, String clientId) {

- 23 this.hosts = hosts;

- 24 this.topic = topic;

- 25 this.clientId = clientId;

- 26 }

- 27

- 28 }


# 对Zokeper的使⽤

KafkaSpout的配置中有两个地⽅可以⽤到Zokeper

- 1.
- 2.


⽤Zokeper来记录KafkaSpout的处理进度，在topology重新提交或者task重启后继续之前的处 理进度。在SpoutConfig中的zkServers, zkPort和zkRot与此相关。如果zkServer和zkPort没有设 置，那么KafkaSpout会使⽤Storm集群所⽤的Zokeper记录这些信息。 ⽤Zokeper来获取Kafka中⼀个topic的所有partition，和每个partition的leader。这需要实现 BrokerHosts的⼦类ZkHosts.但是，这个Zokepr是可选的。如果使⽤BrokerHosts的另⼀个⼦类 StaticHosts,把partition和leader的对应关系硬编码，则不需要Zokeper来提供此功能。 KafkaSpout会从Kafka集群使⽤的Zokeper中提取partition和leader的对应关系。⽽且：

如果使⽤StatisHosts，那么KafkaSpout会使⽤StaticCordinator，这个cordinator不能响应 partition leader的变化。

如果使⽤ZkHosts，那么KafkaSpout会使⽤ZkCordinator, 当其refresh()⽅法被调⽤后，这个 coridnator会检查发⽣leader变更的partition，并为之⽣成新的PartitionManager.从⽽能够在 leader变更后，继续读取消息。

# 影响初始读取进度的配置项

在⼀个topology上线后，它从哪个ofset开始读取消息呢？有⼀些配置项对此有影响：

- 1.
- 2.
- 3.
- 4.
- 5.


SpoutConfig中的id字段。如果想要⼀个topology从另⼀个topology之前的处理进度继续处理，它 们需要有相同的id。 KafkaConfig的forceFromStart字段。如果此字段设为true, 那么它⼀个topology上线后，它会忽略 之前相同id的topology的进度，并且从Kafka中最早的消息开始处理。 KafkaConfig的startOfsetTime字段。默认为kafka.api.OfsetRequest.EarliestTime()开始读，也 就是从Kafka中最早的消息开始处理。也可以设成kafka.api.OfsetRequest.LatestOfset,也就是最 早的消息开始读。也可以⾃⼰指定具体的值。 KafkaConfig的maxOfsetBehind字段。这个字段对于KafkaSpout的多个处理流程都有影响。当提 交⼀个新topology时，如果没有forceFromStart, 当KafkaSpout对某个partition的处理进度落后 startOfsetTime对应的ofset多于此值时，KafkaSpout会丢弃中间的消息，从⽽强制赶上⽬标进 度.⽐如，如果startOfsetTime设成了lastestTime，那么如果进度落后超过maxOfsetBehind， KafkaSpout会直接从latestTime对应的ofset开始处理。如果设成了froceFromStart，则在提交新 任务时，始终会从EarliestTime开始读。 KafkaSpout的userStartOfsetTimeIfOfsetOutOfRange字段。如果设成true，那么当fetch消息时 出错，且FetchResponse显示的出错原因是OFSET_OUT_OF_RANGE，那么就会尝试从 KafkaSpout指定的startOfsetTime对应的消息开始读。例如，如果有⼀批消息因为超过了保存期 限被Kafka删除，并且zk⾥记录的消息在这批被删除的消息⾥。如果KafkaSpout试图从zk的记录继 续读，那么就会出现OFSET_OUT_OF_RANGE的错误，从⽽触发这个配置。

实际上maxOfsetBehind有时候有点名不符实。当startOfsetTime为A, zk⾥的进度为B， A - B > maxOfsetBehind时，应该从A - maxOfsetBehind除开始读或许更好⼀些，⽽不是直接跳到 startOfsetTime。此处的逻辑参⻅PartitionManager的实现。

