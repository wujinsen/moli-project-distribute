Kafka为broker,producer和consumer提供了很多的配置参数。 了解并理解这些配置参数对于 我们使⽤kafka是⾮常重要的。

本⽂列出了⼀些重要的配置参数。

Configuration

官⽅的⽂档 ⽐较⽼了，很多参数有所变动， 有些名字也有所改变。我在整理的过 程中根据0.8.2的代码也做了修正。

# Boker配置参数

下表列出了Boker的重要的配置参数， 更多的配置请参考 kafka.server.KafkaConfig

<table>
  <tr>
    <th>name</th>
    <th>默认值</th>
    <th>描述</th>
  </tr>
  <tr>
    <td>brokerid</td>
    <td>none</td>
    <td>每⼀个boker都有⼀个唯⼀的id 作为它们的名字。 这就允许 boker切换到别的主机/端⼝上，<br><br>依然知道</td>
  </tr>
  <tr>
    <td>enable.zokeper</td>
    <td>true</td>
    <td>consumer 允许注册到zokeper</td>
  </tr>
  <tr>
    <td>log.flush.interval.mesages</td>
    <td>Long.MaxValue</td>
    <td>在数据被写⼊到硬盘和消费者可 ⽤前最⼤累积的消息的数量</td>
  </tr>
  <tr>
    <td>log.flush.interval.ms</td>
    <td>Long.MaxValue</td>
    <td>在数据被写⼊到硬盘前的最⼤时 间</td>
  </tr>
  <tr>
    <td>log.flush.scheduler.interval.ms</td>
    <td>Long.MaxValue</td>
    <td>检查数据是否要写⼊到硬盘的时 间间隔。</td>
  </tr>
  <tr>
    <td>log.retention.hours</td>
    <td>168</td>
    <td>控制⼀个log保留多⻓个⼩时</td>
  </tr>
  <tr>
    <td>log.retention.bytes</td>
    <td>-1</td>
    <td>控制log⽂件最⼤尺⼨</td>
  </tr>
  <tr>
    <td>log.cleaner.enable</td>
    <td>false</td>
    <td>是否log cleaning</td>
  </tr>
  <tr>
    <td>log.cleanup.policy</td>
    <td>delete</td>
    <td>delete还是compat. 其它控制参 数还包括log.cleaner.threads， log.cleaner.io.max.bytes.per.se cond， log.cleaner.dedupe.bufer.size ，log.cleaner.io.bufer.size， log.cleaner.io.bufer.load.factor ，log.cleaner.backof.ms， log.cleaner.min.cleanable.ratio ，</td>
  </tr>
  <tr>
    <td>log.dir</td>
    <td>/tmp/kafka-logs</td>
    <td>log.cleaner.delete.retention.ms 指定log⽂件的根⽬录</td>
  </tr>
  <tr>
    <td>log.segment.bytes</td>
    <td>110241024*1024</td>
    <td>单⼀的log segment⽂件⼤⼩</td>
  </tr>
  <tr>
    <td>log.rol.hours</td>
    <td>24 * 7</td>
    <td>开始⼀个新的log⽂件⽚段的最 ⼤时间</td>
  </tr>
  <tr>
    <td>mesage.max.bytes</td>
    <td>1 0 +</td>
    <td>⼀个socket 请求的最⼤字节数</td>
  </tr>
  <tr>
    <td>num.network.threads</td>
    <td>MesageSet.LogOverhead 3</td>
    <td>处理⽹络请求的线程数</td>
  </tr>
  <tr>
    <td>num.io.threads</td>
    <td>8</td>
    <td>处理IO的线程数</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td>后台线程序</td>
  </tr>
</table>


### background.threads 10

<table>
  <tr>
    <th>num.partitions</th>
    <th>1</th>
    <th>默认分区数</th>
  </tr>
  <tr>
    <td>socket.send.bufer.bytes</td>
    <td>10240</td>
    <td>socket SO_SNDBUF参数</td>
  </tr>
  <tr>
    <td>socket.receive.bufer.bytes</td>
    <td>10240</td>
    <td>socket SO_RCVBUF参数</td>
  </tr>
  <tr>
    <td>zokeper.conect</td>
    <td>localhost:2182/kafka</td>
    <td>指定zokeper连接字符串， 格 式如hostname:port/chrot。</td>
  </tr>
  <tr>
    <td>zokeper.conection.timeout. ms</td>
    <td>6 0</td>
    <td>chrot是⼀个namespace 指定客户端连接zokeper的最 ⼤超时时间</td>
  </tr>
  <tr>
    <td>zokeper.sesion.timeout.ms</td>
    <td>6 0</td>
    <td>连接zk的sesion超时时间</td>
  </tr>
  <tr>
    <td>zokeper.sync.time.ms</td>
    <td>2 0</td>
    <td>zk folower落后于zk leader的最 ⻓时间</td>
  </tr>
</table>


# High-level Consumer配置参数

下表列出了high-level consumer的重要的配置参数。

更多的配置请参考 kafka.consumer.ConsumerConfig

<table>
  <tr>
    <th>name</th>
    <th>默认值</th>
    <th>描述</th>
  </tr>
  <tr>
    <td>groupid</td>
    <td>groupid</td>
    <td>⼀个字符串⽤来指示⼀组 所在的组</td>
  </tr>
  <tr>
    <td>socket.timeout.ms</td>
    <td>3 0</td>
    <td>consumer socket超时时间</td>
  </tr>
  <tr>
    <td>socket.bufersize</td>
    <td>64*1024</td>
    <td>socket receive bufer</td>
  </tr>
  <tr>
    <td>fetch.size</td>
    <td>30 * 1024</td>
    <td>控制在⼀个请求中获取的消息的 字节数。 这个参数在0.8.x中由 fetch.mesage.max.bytes,fetch<br><br>取代</td>
  </tr>
  <tr>
    <td>backof.increment.ms</td>
    <td>1 0</td>
    <td>.min.bytes 这个参数避免在没有新数据的情 况下重复频繁的拉数据。 如果 拉到空数据，则多推后这个时间</td>
  </tr>
  <tr>
    <td>queued.max.mesage.chunks</td>
    <td>2</td>
    <td>high level consumer内部缓存拉 回来的消息到⼀个队列中。 这 个值控制这个队列的⼤⼩</td>
  </tr>
  <tr>
    <td>auto.comit.enable</td>
    <td>true</td>
    <td>如果true,consumer定期地往 zokeper写⼊每个分区的</td>
  </tr>
  <tr>
    <td>auto.comit.interval.ms</td>
    <td>1 0</td>
    <td>ofset 往zokeper上写ofset的频率</td>
  </tr>
  <tr>
    <td>auto.ofset.reset</td>
    <td>largest</td>
    <td>如果ofset出了返回， 则 smallest: ⾃动设置reset到最 ⼩的ofset.largest : ⾃动设置 ofset到最⼤的ofset. 其它值不<br><br></td>
  </tr>
  <tr>
    <td>consumer.timeout.ms</td>
    <td>-1</td>
    <td>允许，会抛出异常.<br><br>默认-1,consumer在没有新消息 时⽆限期的block。如果设置⼀<br><br>⼀个超时异常会抛出</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td>个正值，<br><br>时的最⼤尝试次数</td>
  </tr>
</table>


rebalance.retries.max 4 rebalance

# Producer配置参数

下表列出了producer的重要的参数。

更多的配置请参考 kafka.producer.ProducerConfig

<table>
  <tr>
    <th>name</th>
    <th>默认值</th>
    <th>描述</th>
  </tr>
  <tr>
    <td>serializer.clas</td>
    <td>kafka.serializer.DefaultEncoder</td>
    <td>必须实现 kafka.serializer.Encoder接⼝， 将T类型的对象encode成kafka</td>
  </tr>
  <tr>
    <td>key.serializer.clas</td>
    <td>serializer.clas</td>
    <td>mesage key对象的serializer类</td>
  </tr>
  <tr>
    <td>partitioner.clas</td>
    <td>kafka.producer.DefaultPartition er</td>
    <td>必须实现 kafka.producer.Partitioner，根 提供⼀个分区策略</td>
  </tr>
  <tr>
    <td>producer.type</td>
    <td>sync</td>
    <td>据Key 指定消息发送是同步还是异步。 异步asyc成批发送⽤ kafka.producer.AyncProducer ， 同步sync⽤</td>
  </tr>
  <tr>
    <td>metadata.broker.list</td>
    <td>boker list</td>
    <td>kafka.producer.SyncProducer 使⽤这个参数传⼊boker和分区 的静态信息，如 host1:port1,host2:port2, 这个可<br><br>的⼀部分</td>
  </tr>
  <tr>
    <td>compresion.codec</td>
    <td>NoCompresionCodec</td>
    <td>以是全部boker 消息压缩，默认不压缩</td>
  </tr>
  <tr>
    <td>compresed.topics</td>
    <td>nul</td>
    <td>在设置了压缩的情况下，可以指 定特定的topic压缩，为指定则 全部压缩</td>
  </tr>
  <tr>
    <td>mesage.send.max.retries</td>
    <td>3</td>
    <td>消息发送最⼤尝试次数</td>
  </tr>
  <tr>
    <td>retry.backof.ms</td>
    <td>30</td>
    <td>每次尝试增加的额外的间隔时间</td>
  </tr>
  <tr>
    <td>topic.metadata.refresh.interval. ms</td>
    <td>6 0</td>
    <td>定期的获取元数据的时间。当分 区丢失，leader不可⽤时 producer也会主动获取元数据， 如果为0，则每次发送完消息就 获取元数据，不推荐。如果为负 值，则只有在失败的情况下获取 元数据。</td>
  </tr>
  <tr>
    <td>queue.bufering.max.ms</td>
    <td>5 0</td>
    <td>在producer queue的缓存的数据</td>
  </tr>
  <tr>
    <td>queue.bufering.max.mesage</td>
    <td>1 0</td>
    <td>最⼤时间，仅仅for asyc producer 缓存的消息的最⼤数</td>
  </tr>
  <tr>
    <td>queue.enqueue.timeout.ms</td>
    <td>-1</td>
    <td>量，仅仅for asyc 0当queue满时丢掉，负值是 queue满时block,正值是queue 满时block相应的时间，仅仅for</td>
  </tr>
</table>


### asyc

<table>
  <tr>
    <th>batch.num.mesages</th>
    <th>20</th>
    <th>⼀批消息的数量，仅仅for asyc</th>
  </tr>
  <tr>
    <td>request.required.acks</td>
    <td>0</td>
    <td>0表示producer毋须等待leader 的确认，1代表需要leader确认 写⼊它的本地log并⽴即确 认，-1代表所有的备份都完成后</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td>确认。 仅仅for sync 确认超时时间</td>
  </tr>
</table>


request.timeout.ms 1 0

## kafka.serializer.DefaultEncoder

默认的这个Encoder事实上不做任何处理，接收到什么byte[]就返回什么byte[]:

<table>
  <tr>
    <th>1<br>2<br>3<br></th>
    <th>classDefaultEncoder(props: VerifiableProperties = null)extendsEncoder[Array[Byte]] { overridedef toBytes(value: Array[Byte]): Array[Byte] = value }</th>
  </tr>
</table>


NullEncoder则不管接收什么都返回null:

<table>
  <tr>
    <th>1<br>2<br>3<br></th>
    <th>classNullEncoder[T](props: VerifiableProperties = null)extendsEncoder[T] { overridedef toBytes(value: T): Array[Byte] = null }</th>
  </tr>
</table>


StringEncoder则返回字符串，默认UTF-8格式：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br>11<br>12<br>13<br></th>
    <th>classStringEncoder(props: VerifiableProperties = null)extendsEncoder[String] { val encoding = if(props == null) "UTF8" else<br><br>props.getString("serializer.encoding ", "UTF8") overridedef toBytes(s: String): Array[Byte] = if(s == null) null else<br><br>s.getBytes(encoding) }</th>
  </tr>
</table>


## kafka.producer.DefaultPartitioner

默认的分区函数为DefaultPartitioner,它根据key的hashcode与分区数取余，得到相应的分区。

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br></th>
    <th>classDefaultPartitioner(props: VerifiableProperties = null)extendsPartitioner{ privateval random = new java.util.Random def partition(key: Any, numPartitions: Int): Int = {<br><br>Utils.abs(key.hashCode) % numPartitions<br><br>} }</th>
  </tr>
</table>


但是如果key为null时会发送到哪个分区？在⼀定时间内往⼀个特定的分区发送，超过⼀定时间⼜ 会随机选择⼀个，请参考 .所以推荐你发送Kafka消 息时总是指定⼀个key,以便消息能均匀的分到每个分区上。

key为null时Kafka会将消息发送给哪个分区?

