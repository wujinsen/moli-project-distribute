# 上次留下来的问题

如果消息是发给很多不同的topic的， async producer如何在按batch发送的同时区分topic的 它是如何⽤key来做partition的？ 是如何实现对消息成批量的压缩的？

- 1.
- 2.
- 3.


## async producer如何在按batch发送的同时区分topic的

这个问题的答案是： DefaultEventHandler会把发给它的⼀个batch的消息（实际上是 Seq[KeyedMessage[K,V]]类型）拆开，确定每条消息该发送给哪个broker。对发给每个broker的消息，会按topic和 partition来组合。即：拆包=>根据metaData组装

这个功能是通过partitionAndCollate⽅法实现的

<table>
  <tr>
    <th>1</th>
    <th>def partitionAndCollate(messages: Seq[KeyedMessage[K,Message]]): Option[Map[Int, collection.mutable.Map[TopicAndParti tion, Seq[KeyedMessage[K,Message]]]]]<br><br></th>
  </tr>
</table>


它返回⼀个Option对象，这个Option的元素是⼀个Map，Key是brokerId，value是发给这个broker的消息。对 每⼀条消息，先确定它要被发给哪⼀个topic的哪个parition。然后确定这个parition的leader broker，然后去

Map[Int, collection.mutable.Map[TopicAndPartition, Seq[KeyedMessage[K,Message]]]]这个Map⾥找到对应 的broker,然后把这条消息填充给对应的topic+partition对应的Seq[KeyedMessage[K,Message]]。这样就得到了最 后的结果。这个结果表示了哪些消息要以怎样的结构发给⼀个broker。真正发送的时候，会按照brokerId的不同，把打 包好的消息发给不同的broker。

⾸先，看⼀下kafka protocol⾥对于Producer Request结构的说明：

ProduceRequest => RequiredAcks Timeout [TopicName [Partition MessageSetSize MessageSet]]

RequiredAcks => int16

Timeout => int32

Partition => int32

MessageSetSize => int32

发给⼀个broker的消息就是这样的结构。

同时，在kafka wiki⾥对于Produce API 有如下说明：

The produce API is used to send message sets to the server. For efficiency it allows sending message sets intended for many topic partitions in a single request.

即在⼀个produce request⾥，可以同时发消息给多个topic+partition的组合。当然⼀个produce request是发给⼀ 个broker的。

使⽤

<table>
  <tr>
    <th> </th>
    <th>send(brokerid, messageSetPerBroker)</th>
  </tr>
</table>


1

把消息set发给对应的brokerid。

### 它是如何⽤key来做partition的？

⾸先看下KeyedMessage类的定义：

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br><br>8<br><br>9<br><br><br>0<br><br>1<br><br>2<br><br>3<br><br>4<br><br><br>15</th>
    <th>case class KeyedMessage[K, V](val topic: String, val key: K, val partKey: Any, val message: V) {<br><br>if(topic == null)<br><br>throw new IllegalArgumentExcepti on("Topic cannot be null.")<br><br>def this(topic: String, message: V)<br><br>= this(topic, null.asInstanceOf[K], null, message)<br><br>def this(topic: String, key: K, message: V) = this(topic, key, key, message)<br><br>def partitionKey = { if(partKey != null) partKey else if(hasKey)<br><br>key else<br><br>null<br><br>} def hasKey = key != null<br><br>}</th>
  </tr>
</table>


当使⽤三个参数的构造函数时， partKey会等于key。partKey是⽤来做partition的，但它不会最当成消息的⼀部 分被存储。

前边提到了，在确定⼀个消息应该发给哪个broker之前，要先确定它发给哪个partition,这样才能根据paritionId去找到 对应的leader所在的broker。

<table>
  <tr>
    <th>1</th>
    <th>val topicPartitionsList = getPartitionListForTopic(message) //<br><br>获取这个消息发送给的<br>val partitionIndextopic的=partition信息 getPartition(message.topic, message.partitionKey, topicPartitionsList)//确定这个消息发给 哪个partition<br><br></th>
  </tr>
</table>


注意传给getPartition⽅法中时使⽤的是partKey。getPartition⽅法为：

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br><br>8<br><br>9<br><br><br>0<br><br>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br><br>8<br><br><br>19<br><br>0<br><br>1<br><br>2<br><br>3<br><br>4<br><br><br>25</th>
    <th>private def getPartition(topic: String, key: Any, topicPartitionList: Seq[PartitionAndLeader]): Int = {<br><br>val numPartitions = topicPartitionList.size<br><br>if(numPartitions <= 0)<br><br>throw new UnknownTopicOrPartitio nException("Topic " + topic + " doesn't exist")<br><br>val partition = if(key == null) { // If the key is null, we don't really need a partitioner<br><br>// So we look up in the send partition cache for the topic to decide the target partition<br><br>val id = sendPartitionPerTopicCache.get(topic )<br><br>idmatch { case Some(partitionId) =><br><br>// directly return the partitionId without checking availability of the leader,<br><br>// since we want to postpone the failure until the send operation anyways<br><br>partitionId case None =><br><br>val availablePartitions = topicPartitionList.filter(_.leaderBr okerIdOpt.isDefined)<br><br>if (availablePartitions.is Empty)<br><br>throw new LeaderNotAvail ableException("No leader for any partition in topic " + topic)<br><br>val index = Utils.abs(Random.nextInt) % availablePartitions.size<br><br>val partitionId = availablePartitions(index).partition Id<br><br>sendPartitionPerTopicCache<br><br>.put(topic, partitionId)<br><br>partitionId } } else<br><br>partitioner.partition(key, numPartitions)<br><br></th>
  </tr>
</table>


当partKey为null时，⾸先它从sendParitionPerTopicCache⾥取这个topic缓存的partitionId，这个cache是⼀ 个Map.如果之前⼰经使⽤sendPartitionPerTopicCache.put(topic, partitionId)缓存了⼀个，就直接取出它。否则就 随机从可⽤的partitionId⾥取出⼀个，把它缓存到sendParitionPerTopicCache。这就使得当 sendParitionPerTopicCache⾥有⼀个可⽤的partitionId时，很多消息都会被发送给这同⼀个partition。因此若所有 消息的partKey都为空，在⼀段时间内只会有⼀个partition能收到消息。之所以会说“⼀段”时间，⽽不是永久，是因为 handler隔⼀段时间会重新获取它发送过的消息对应的topic的metadata，这个参数通过 topic.metadata.refresh.interval.ms来设置。当它重新获取metadata之后，会消空⼀些缓存，就包括这个 sendParitionPerTopicCache。因此，接下来就会⽣成另⼀个随机的被缓存的partitionId。

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br></th>
    <th>if (topicMetadataRefreshInterval >= 0 &&<br><br>SystemTime.milliseconds lastTopicMetadataRefreshTime > topicMetadataRefreshInterval) { // 若该refresh topic metadata 了，do the refresh<br><br>Utils.swallowError(brokerParti tionInfo.updateInfo(topicMetadataToRe fresh.toSet, correlationId.getAndIncrement))<br><br>sendPartitionPerTopicCache.cle ar()<br><br>topicMetadataToRefresh.clear lastTopicMetadataRefreshTime =<br><br>SystemTime.milliseconds }<br><br></th>
  </tr>
</table>


当partKey不为null时，就⽤传给handler的partitioner的partition⽅法，根据partKey和numPartitions来确定

这个消息被发给哪个partition。注意这⾥的numPartition是topicPartitionList.size获取的，有可能会有parition不存 在可⽤的leader。这样的问题将留给send时解决。实际上发⽣这种情况时，partitionAndCollate会将这个消息分派给 brokerId为-1的broker。⽽send⽅法会在发送前判断brokerId

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br></th>
    <th>if(brokerId < 0) {<br><br>warn("Failed to send data since partitions %s don't have a leader".format(messagesPerTopic.map( _._1).mkString(",")))<br><br>messagesPerTopic keys.toSeq<br><br></th>
  </tr>
</table>


.

当brokerId<0时，就返回⼀个⾮空的Seq，包括了所有没有leader的topic+partition的组合，如果重试了指定次 数还不能发送，将最终导致handle⽅法抛出⼀个 FailedToSendMessageException异常。

### 是如何实现对消息成批量的压缩的？

这个是在

<table>
  <tr>
    <th>1</th>
    <th>private def groupMessagesToSet(messagesPerTopicA ndPartition: collection.mutable.Map[TopicAndParti tion, Seq[KeyedMessage[K,Message]]])<br><br></th>
  </tr>
</table>


中处理。

说明为：

/** enforce the compressed.topics config here.

- * If the compression codec is anything other than NoCompressionCodec,

- * Enable compression only for specified topics if any

- * If the list of compressed topics is empty, then enable the specified compression codec for all topics

- * If the compression codec is NoCompressionCodec, compression is disabled for all topics

- */


即，如果没有设置压缩，就所有topic对应的消息集都不压缩。如果设置了压缩，并且没有设置对个别topic启⽤压缩， 就对所有topic都使⽤压缩；否则就只对设置了压缩的topic压缩。

在这个gruopMessageToSet中，并不有具体的压缩逻辑。⽽是返回⼀个ByteBufferMessageSet对象。它的注释为：

/**

- * A sequence of messages stored in a byte buffer

*

- * There are two ways to create a ByteBufferMessageSet

*

- * Option 1: From a ByteBuffer which already contains the serialized message set. Consumers will use this method.

*

- * Option 2: Give it a list of messages along with instructions relating to serialization format. Producers will use this method.


看来它是对于消息集进⾏序列化和反序列化的⼯具。

在它的实现⾥⽤到了CompressionFactory对象。从它的实现⾥可以看到Kafka只⽀持GZIP和Snappy两种压缩⽅式。

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br><br>8<br></th>
    <th>compressionCodec match { case DefaultCompressionCodec<br><br>=> new GZIPOutputStream(stream)<br><br>case GZIPCompressionCodec => new GZIPOutputStream(stream)<br><br>ppyOutputStreamcaseimportSnappyCompresorg.xeriasl.snappionCody.Snec =a> m) new SnappyOutputStream(strea<br><br>case _ =><br><br>wnCodecException("Unknownthrow new kafka.common.Codec:Unkn" o+ compressionCodec)<br><br></th>
  </tr>
</table>


