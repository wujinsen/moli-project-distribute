先把⼏个⽐较重要的⽅法列出来 / ⼊⼝，处理 mesage及mesages

def handle(events: Seq[KeyedMesage[K,V]) /处理序列化的KeyedMesage数据

private def dispatchSerializedData(mesages: Seq[KeyedMesage[K,Mesage]): Seq[KeyedMesage[K, Mesage]

/ 为mesages分区

def partitionAndColate(mesages: Seq[KeyedMesage[K,Mesage]): Option[Map[Int, colection.mutable.Map[TopicAndPartition, Seq[KeyedMesage[K,Mesage ]

/*

- * Constructs and sends the produce request based on a map from (topic, partition) -> mesages

*

- * @param brokerId the broker that wil receive the request
- * @param mesagesPerTopic the mesages as a map from (topic, partition) -> mesages
- * @return the set (topic, partitions) mesages which incured an eror sending or procesing
- */ private def send(brokerId: Int, mesagesPerTopic: colection.mutable.Map[TopicAndPartition,


ByteBuferMesageSet]) kafka在sync模式下发送消息时，是通过DefaultEventHandler的handle⽅法把mesage发送给 broker，可以是⼀条消息也可以是多条消息组成的List。 ⽤List举例： DefaultEventHandler得到mesages（可能包含多个topic的mesages）后，调⽤ dispatchSerializedData⽅法，遍历mesage为各个topic的每个mesage进⾏partition分区。如果 mesage中key!=nul, 则调⽤配置的partition.clas定义的分区规则类，得到partitionIndex，否则在 sendPartitionPerTopicCache的记录中去获取topic的partitionId,然后找到partitionIndex对应的 leaderBrokerId。所以1个mesage会分配给1个topic的1个partition的leaderBrokerId.

[java]view plaincopy package kafka.producer.async

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


import kafka.comon._ import kafka.mesage.{NoCompresionCodec, Mesage, ByteBuferMesageSet} import kafka.producer._ import kafka.serializer.Encoder import kafka.utils.{Utils, Loging, SystemTime} import scala.util.Random import scala.colection.{Seq, Map}

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


import scala.colection.mutable.{ArayBufer, HashMap, Set} import java.util.concurent.atomic._ import kafka.api.{TopicMetadata, ProducerRequest}

clas DefaultEventHandler[K,V](config: ProducerConfig, private val partitioner: Partitioner[K], private val encoder: Encoder[V], private val keyEncoder: Encoder[K], private val producerPol: ProducerPol, private val topicPartitionInfos: HashMap[String, TopicMetadata] = new Hash

Map[String, TopicMetadata]) extends EventHandler[K,V] with Loging { val isSync = ("sync" = config.producerType)

val corelationId = new AtomicInteger(0) val brokerPartitionInfo = new BrokerPartitionInfo(config, producerPol, topicPartitionInfos)

private val topicMetadataRefreshInterval = config.topicMetadataRefreshIntervalMs private var lastTopicMetadataRefreshTime = 0L private val topicMetadataToRefresh = Set.empty[String] private val sendPartitionPerTopicCache = HashMap.empty[String, Int]

private val producerStats = ProducerStatsRegistry.getProducerStats(config.clientId) private val producerTopicStats = ProducerTopicStatsRegistry.getProducerTopicStats(config.c

lientId) / ⼊⼝，处理 mesage及mesages

def handle(events: Seq[KeyedMesage[K,V]) { val serializedData = serialize(events) serializedData.foreach {

keyed => val dataSize = keyed.mesage.payloadSize producerTopicStats.getProducerTopicStats(keyed.topic).byteRate.mark(dataSize) producerTopicStats.getProducerAlTopicsStats.byteRate.mark(dataSize)

} var outstandingProduceRequests = serializedData var remainingRetries = config.mesageSendMaxRetries + 1 val corelationIdStart = corelationId.get()

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


debug("Handling %d events".format(events.size) while (remainingRetries > 0 & outstandingProduceRequests.size > 0) {

topicMetadataToRefresh += outstandingProduceRequests.map(_.topic) if (topicMetadataRefreshInterval >= 0 &

SystemTime.miliseconds - lastTopicMetadataRefreshTime > topicMetadataRefreshInterv al) {

Utils.swalowEror(brokerPartitionInfo.updateInfo(topicMetadataToRefresh.toSet, corelati

onId.getAndIncrement) sendPartitionPerTopicCache.clear() topicMetadataToRefresh.clear lastTopicMetadataRefreshTime = SystemTime.miliseconds

}

/ 处理序列化的数据 outstandingProduceRequests = dispatchSerializedData(outstandingProduceRequests) if (outstandingProduceRequests.size > 0) {

info("Back of for %d ms before retrying send. Remaining retries = %d".format(config.retr yBackofMs, remainingRetries-1)

/ back of and update the topic metadata cache before atempting another send operatio n

Thread.sl ep(config.retryBackofMs) / get topics of the outstanding produce requests and refresh metadata for those Utils.swalowEror(brokerPartitionInfo.updateInfo(outstandingProduceRequests.map(_.top

ic).toSet, corelationId.getAndIncrement) sendPartitionPerTopicCache.clear() remainingRetries -= 1 producerStats.resendRate.mark()

}

} if(outstandingProduceRequests.size > 0) {

producerStats.failedSendRate.mark() val corelationIdEnd = corelationId.get() eror("Failed to send requests for topics %s with corelation ids in [%d,%d]"

.format(outstandingProduceRequests.map(_.topic).toSet.mkString(","), corelationIdStart, corelationIdEnd-1)

thrownew FailedToSendMesageException("Failed to send mesages after " + config.mes sageSendMaxRetries + " tries.", nul)

}

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


} /处理序列化的数据

private def dispatchSerializedData(mesages: Seq[KeyedMesage[K,Mesage]): Seq[Keyed Mesage[K, Mesage] = {

/按规则为mesages分配分区 val partitionedDataOpt = partitionAndColate(mesages) partitionedDataOpt match {

case Some(partitionedData) => val failedProduceRequests = new ArayBufer[KeyedMesage[K,Mesage] try {

/遍历每个leaderBrokerId，发送mesages for(brokerid, mesagesPerBrokerMap) <- partitionedData) { if (loger.isTraceEnabled) mesagesPerBrokerMap.foreach(partitionAndEvent =>

trace("Handling event for Topic: %s, Broker: %d, Partitions: %s".format(partitionAndE vent._1, brokerid, partitionAndEvent._2)

val mesageSetPerBroker = groupMesagesToSet(mesagesPerBrokerMap)

/发送消息到brokerid val failedTopicPartitions = send(brokerid, mesageSetPerBroker) failedTopicPartitions.foreach(topicPartition => {

mesagesPerBrokerMap.get(topicPartition) match { case Some(data) => failedProduceRequests.apendAl(data) case None => / nothing

} })

} } catch {

case t: Throwable => eror("Failed to send mesages", t)

} failedProduceRequests

case None => / al produce requests failed

mesages }

}

def serialize(events: Seq[KeyedMesage[K,V]): Seq[KeyedMesage[K,Mesage] = { val serializedMesages = new ArayBufer[KeyedMesage[K,Mesage](events.size)

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
- 139.
- 140.


events.map{e => try { if(e.hasKey)

serializedMesages += KeyedMesage[K,Mesage] (topic = e.topic, key = e.key, mesage = new Mesage(key = keyEncoder.toBytes(e.key), byte s = encoder.toBytes(e.mesage)

else

serializedMesages += KeyedMesage[K,Mesage] (topic = e.topic, key = nul.asInstanceOf[K], mesage = new Mesage(bytes = encoder.toByte s(e.mesage) } catch {

case t: Throwable => producerStats.serializationErorRate.mark() if (isSync) {

throw t

} else { / curently, if in async mode, we just log the serialization eror. We ned to revisit / this when doing kafka-496

eror("Eror serializing mesage for topic %s".format(e.topic), t) }

}

} serializedMesages

} / 为mesages分区

def partitionAndColate(mesages: Seq[KeyedMesage[K,Mesage]): Option[Map[Int, cole ction.mutable.Map[TopicAndPartition, Seq[KeyedMesage[K,Mesage ] = {

/定义⼀个leaderId => (topic,mesages)的映射

val ret = new HashMap[Int, colection.mutable.Map[TopicAndPartition, Seq[KeyedMesage [K,Mesage ]

try { for (mesage <- mesages) { /获取topic的partition集合 val topicPartitionsList = getPartitionListForTopic(mesage)

/为mesage分配partition index，如果mesage的key不等于nul,则会调⽤配置的 partitioner.clas

val partitionIndex = getPartition(mesage.topic, mesage.key, topicPartitionsList)

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
- 167.
- 168.
- 169.
- 170.
- 171.
- 172.


/根据partitionIndex获取对应的partition-broker信息 val brokerPartition = topicPartitionsList(partitionIndex)

/ postpone the failure until the send operation, so that requests for other brokers are han dled corectly

/ 获取partitionIndex的leaderBrokerId

val leaderBrokerId = brokerPartition.leaderBrokerIdOpt.getOrElse(-1) / 定义leaderBrokerId需要发送的数据集合<topic,mesages> /最终发送mesages时是按照 每个leaderBrokerId的每个Topic的每个partitionId发送的

var dataPerBroker: HashMap[TopicAndPartition, Seq[KeyedMesage[K,Mesage] = nul

ret.get(leaderBrokerId) match { case Some(element) =>

dataPerBroker = element.asInstanceOf[HashMap[TopicAndPartition, Seq[KeyedMesa ge[K,Mesage ]

case None => dataPerBroker = new HashMap[TopicAndPartition, Seq[KeyedMesage[K,Mesage] ret.put(leaderBrokerId, dataPerBroker)

}

val topicAndPartition = TopicAndPartition(mesage.topic, brokerPartition.partitionId)

/ 定义topic的配个Partition发送的mesages var dataPerTopicPartition: ArayBufer[KeyedMesage[K,Mesage] = nul dataPerBroker.get(topicAndPartition) match {

case Some(element) =>

dataPerTopicPartition = element.asInstanceOf[ArayBufer[KeyedMesage[K,Mesage] ]

case None => dataPerTopicPartition = new ArayBufer[KeyedMesage[K,Mesage] dataPerBroker.put(topicAndPartition, dataPerTopicPartition)

} / 为brokerid的topic的partition 追加需要发送的消息

dataPerTopicPartition.apend(mesage) }/end for Some(ret)

}catch { / Swalow recoverable exceptions and return None so that they can be retried.

- 173.
- 174.
- 175.
- 176.
- 177.
- 178.
- 179.
- 180.
- 181.
- 182.
- 183.
- 184.
- 185.
- 186.
- 187.
- 188.
- 189.
- 190.
- 191.
- 192.
- 193.
- 194.
- 195.
- 196.
- 197.
- 198.
- 199.
- 200.
- 201.
- 202.
- 203.


case ute: UnknownTopicOrPartitionException => warn("Failed to colate mesages by topi c,partition due to: " + ute.getMesage); None

case lnae: LeaderNotAvailableException => warn("Failed to colate mesages by topic,part ition due to: " + lnae.getMesage); None

case oe: Throwable => eror("Failed to colate mesages by topic, partition due to: " + oe. getMesage); None

} }

private def getPartitionListForTopic(m: KeyedMesage[K,Mesage]): Seq[PartitionAndLeade r] = {

val topicPartitionsList = brokerPartitionInfo.getBrokerPartitionInfo(m.topic, corelationId.get AndIncrement)

debug("Broker partitions registered for topic: %s are %s"

.format(m.topic, topicPartitionsList.map(p => p.partitionId).mkString("," ) val totalNumPartitions = topicPartitionsList.length if(totalNumPartitions = 0)

thrownew NoBrokersForPartitionException("Partition key = " + m.key) topicPartitionsList

}

/*

- * Retrieves the partition id and throws an UnknownTopicOrPartitionException if
- * the value of partition is not betwen 0 and numPartitions-1
- * @param key the partition key
- * @param topicPartitionList the list of available partitions
- * @return the partition id
- */ private def getPartition(topic: String, key: K, topicPartitionList: Seq[PartitionAndLeader]): Int


= { val numPartitions = topicPartitionList.size if(numPartitions <= 0)

thrownew UnknownTopicOrPartitionException("Topic " + topic + " doesn't exist") val partition =

if(key = nul) { / If the key is nul, we don't realy ned a partitioner / So we l ok up in the send partition cache for the topic to decide the target partition

- 204.
- 205.
- 206.
- 207.
- 208.
- 209.
- 210.
- 211.
- 212.
- 213.
- 214.
- 215.
- 216.
- 217.
- 218.
- 219.
- 220.
- 221.
- 222.
- 223.
- 224.
- 225.
- 226.
- 227.
- 228.
- 229.
- 230.
- 231.
- 232.
- 233.
- 234.


val id = sendPartitionPerTopicCache.get(topic) id match {

case Some(partitionId) =>

/ directly return the partitionId without checking availability of the leader, / since we want to postpone the failure until the send operation anyways

partitionId

case None => val availablePartitions = topicPartitionList.filter(_.leaderBrokerIdOpt.isDefined) if (availablePartitions.isEmpty)

thrownew LeaderNotAvailableException("No leader for any partition in topic " + topi c)

val index = Utils.abs(Random.nextInt) % availablePartitions.size val partitionId = availablePartitions(index).partitionId sendPartitionPerTopicCache.put(topic, partitionId) partitionId

} } else

partitioner.partition(key, numPartitions) if(partition < 0| partition >= numPartitions)

thrownew UnknownTopicOrPartitionException("Invalid partition id: " + partition + " for to pic " + topic +

"; Valid values are in the inclusive range of [0, " + (numPartitions-1) + "]")

trace("Asigning mesage of topic %s and key %s to a selected partition %d".format(topic, if (key = nul) "[none]"else key.toString, partition)

partition }

/*

* Constructs and sends the produce request based on a map from (topic, partition) > mesages

*

- * @param brokerId the broker that wil receive the request
- * @param mesagesPerTopic the mesages as a map from (topic, partition) -> mesages
- * @return the set (topic, partitions) mesages which incured an eror sending or procesin


g

*/

- 235.
- 236.
- 237.
- 238.
- 239.
- 240.
- 241.
- 242.
- 243.
- 244.
- 245.
- 246.
- 247.
- 248.
- 249.
- 250.
- 251.
- 252.
- 253.
- 254.
- 255.
- 256.
- 257.
- 258.
- 259.


private def send(brokerId: Int, mesagesPerTopic: colection.mutable.Map[TopicAndPartitio n, ByteBuferMesageSet]) = {

if(brokerId < 0) {

warn("Failed to send data since partitions %s don't have a leader".format(mesagesPerTo pic.map(_._1).mkString("," )

mesagesPerTopic.keys.toSeq

} elseif(mesagesPerTopic.size > 0) { val curentCorelationId = corelationId.getAndIncrement val producerRequest = new ProducerRequest(curentCorelationId, config.clientId, config.

requestRequiredAcks,

config.requestTimeoutMs, mesagesPerTopic) var failedTopicPartitions = Seq.empty[TopicAndPartition] try {

val syncProducer = producerPol.getProducer(brokerId) debug("Producer sending mesages with corelation id %d for topics %s to broker %d o

n %s:%d"

.format(curentCorelationId, mesagesPerTopic.keySet.mkString(","), brokerId, syncPro

ducer.config.host, syncProducer.config.port) val response = syncProducer.send(producerRequest) debug("Producer sent mesages with corelation id %d for topics %s to broker %d on %

s:%d"

.format(curentCorelationId, mesagesPerTopic.keySet.mkString(","), brokerId, syncPro ducer.config.host, syncProducer.config.port)

if(response != nul) { if (response.status.size != producerRequest.data.size)

thrownew KafkaException("Incomplete response (%s) for producer request (%s)".for mat(response, producerRequest)

if (loger.isTraceEnabled) { val sucesfulySentData = response.status.filter(_._2.eror = ErorMaping.NoEror)

sucesfulySentData.foreach(m => mesagesPerTopic(m._1).foreach(mesage => trace("Sucesfuly sent mesage: %s".format(Utils.readString(mesage.mesage.pa

yload ) } val failedPartitionsAndStatus = response.status.filter(_._2.eror != ErorMaping.NoEro

- r).toSeq


- 260.
- 261.
- 262.
- 263.
- 264.
- 265.
- 266.
- 267.
- 268.
- 269.
- 270.
- 271.
- 272.
- 273.
- 274.
- 275.
- 276.
- 277.
- 278.
- 279.
- 280.
- 281.
- 282.
- 283.
- 284.
- 285.
- 286.
- 287.
- 288.
- 289.


failedTopicPartitions = failedPartitionsAndStatus.map(partitionStatus => partitionStatus. _1)

if(failedTopicPartitions.size > 0) { val erorString = failedPartitionsAndStatus

.sortWith(p1, p2) => p1._1.topic.compareTo(p2._1.topic) < 0|

(p1._1.topic.compareTo(p2._1.topic) = 0 & p1._1.partition < p2._1.partiti on)

.map{ case(topicAndPartition, status) => topicAndPartition.toString + ": " + ErorMaping.exceptionFor(status.eror).getClas

- s.getName }.mkString(",")


warn("Produce request with corelation id %d failed due to %s".format(curentCorelat

ionId, erorString) } failedTopicPartitions

} else

Seq.empty[TopicAndPartition] } catch {

case t: Throwable =>

warn("Failed to send producer request with corelation id %d to broker %d with data for partitions %s"

.format(curentCorelationId, brokerId, mesagesPerTopic.map(_._1).mkString(","), t) mesagesPerTopic.keys.toSeq

} } else {

List.empty }

}

private def groupMesagesToSet(mesagesPerTopicAndPartition: colection.mutable.Map[T opicAndPartition, Seq[KeyedMesage[K,Mesage]) = {

/* enforce the compresed.topics config here.

- * If the compresion codec is anything other than NoCompresionCodec,
- * Enable compresion only for specified topics if any
- * If the list of compresed topics is empty, then enable the specified compresion codec


for al topics

- 290.
- 291.
- 292.
- 293.
- 294.
- 295.
- 296.
- 297.
- 298.
- 299.
- 300.
- 301.
- 302.
- 303.
- 304.
- 305.
- 306.
- 307.
- 308.
- 309.
- 310.
- 311.
- 312.
- 313.
- 314.
- 315.
- 316.
- 317.
- 318.
- 319.
- 320.
- 321.
- 322.


- * If the compresion codec is NoCompresionCodec, compresion is disabled for al topic s
- */


val mesagesPerTopicPartition = mesagesPerTopicAndPartition.map { case (topicAndParti

tion, mesages) => val rawMesages = mesages.map(_.mesage) ( topicAndPartition,

config.compresionCodec match { case NoCompresionCodec =>

debug("Sending %d mesages with no compresion to %s".format(mesages.size, top icAndPartition)

new ByteBuferMesageSet(NoCompresionCodec, rawMesages: _*) case _ =>

config.compresedTopics.size match { case0 => debug("Sending %d mesages with compresion codec %d to %s"

.format(mesages.size, config.compresionCodec.codec, topicAndPartition) new ByteBuferMesageSet(config.compresionCodec, rawMesages: _*)

case _ => if(config.compresedTopics.contains(topicAndPartition.topic) { debug("Sending %d mesages with compresion codec %d to %s"

.format(mesages.size, config.compresionCodec.codec, topicAndPartition) new ByteBuferMesageSet(config.compresionCodec, rawMesages: _*)

} else {

debug("Sending %d mesages to %s with no compresion as it is not in comprese d.topics - %s"

.format(mesages.size, topicAndPartition, config.compresedTopics.toString) new ByteBuferMesageSet(NoCompresionCodec, rawMesages: _*)

} }

} )

} mesagesPerTopicPartition

}

- 323.
- 324.
- 325.
- 326.
- 327.
- 328.


def close() { if (producerPol != nul)

producerPol.close }

}

