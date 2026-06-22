htp:/ w.iteblog.com/archives/1605

内部提供了许多管理脚本，这些脚本都放在 $KAFKA_HOME/bin ⽬录下，⽽这些类的实现都是 放在源码的 kafka/core/src/main/scala/kafka/tools/ 路径下。

Kafka

⽂章⽬录 [hide]

- 1 Consumer Ofset Checker

- 2 Dump Log Segment

- 3 导出Zokeper中Group相关的偏移量

- 4 通过JMX获取metrics信息

- 5 Kafka数据迁移⼯具

- 6 ⽇志重放⼯具

- 7 Simple Consume脚本

- 8 更新Zokeper中的偏移量


# Consumer Ofset Checker

Consumer Ofset Checker主要是运⾏ kafka.tools.ConsumerOffsetChecker 类，对应的 脚本是kafka-consumer-ofset-checker.sh，会显示出Consumer的Group、Topic、分区ID、分区对应 已经消费的Ofset、logSize⼤⼩，Lag以及Owner等信息。

bin/kafka-run-class.sh kafka.tools.ConsumerOffsetChecker --zookeeper zookeeper01:2181 --group test2

1

bin/kafka-consumer-offset-checker.sh kafka.tools.ConsumerOffsetChecker -zookeeper zookeeper01:2181 --group test2

- 1

- 2

- 3


如果运⾏ kafka-consumer-offset-checker.sh 脚本的时候什么信息都不输⼊，那么会显示以 下信息：

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>[iteblog@www.iteblog.com /]$ bin/kafka-consumer-offset-checker.sh</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th>Check the offset of your consumers.</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>Option Description</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>------ ----------<br><br>-</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>--broker-info Print broker info</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>--group Consumer group.</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>--help Print this message.</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th>--retry.backoff.ms <Integer> Retry back-off to use for failed<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>offset queries. (default: 3000)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>--socket.timeout.ms <Integer> Socket timeout to use when querying</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>for offsets. (default: 6000)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>--topic Comma-separated list of consumer</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th>topics (all topics if absent).<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 4<br><br></th>
    <th>--zookeeper ZooKeeper connect string. (default:</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 5<br><br></th>
    <th>localhost:2181)</th>
  </tr>
</table>


## 我们根据提示，输⼊的命令如下：

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>[iteblog@www.iteblog.com /]$ bin/kafka-consumer-offset-checker.sh -<br><br>-zookeeper www.iteblog.com:2181 --topic test<br><br>--group spark --broker-info<br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th>Group Topic Pid Offset logSize Lag Owner</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>spark test 0 34666914 34674392 7478 none<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>spark test 1 34670481 34678029 7548 none<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>spark test 2 34670547 34678002 7455 none<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>spark test 3 34664512 34671961 7449 none<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>spark test 4 34680143 34687562 7419 none<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th>spark test 5 34672309 34679823 7514 none<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>spark test 6 34674660 34682220 7560 none<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>BROKER INFO</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>2 -> www.iteblog.com:9092</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>5 -> www.iteblog.com:9093</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th>4 -> www.iteblog.com:9094</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 4<br><br></th>
    <th>7 -> www.iteblog.com:9095</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 5<br><br></th>
    <th>1 -> www.iteblog.com:9096</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 6<br><br></th>
    <th>3 -> www.iteblog.com:9097</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 7<br><br></th>
    <th>6 -> www.iteblog.com:9098</th>
  </tr>
</table>


# Dump Log Segment

有时候我们需要验证⽇志索引是否正确，或者仅仅想从log⽂件中直接打印消息，我们可以使⽤ kafka.tools.DumpLogSegments 类来实现，先来看看它需要的参数：

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>[iteblog@www.iteblog.com /]$ bin/kafka-run-class.sh kafka.tools.DumpLogSegments</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th>Parse a log file and dump its contents to the console, useful for debugging a seemingly corrupt log segment.<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>Option Description</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>------ ----------<br><br>-</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>--deep-iteration if set , uses deep instead of shallow<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>iteration</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>--files <file1, file2, ...> REQUIRED: The comma separated list of</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th>data and index log files to be dumped</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>--key-decoder-class if set , used to deserialize the keys.<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>This class should implement kafka.</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>serializer.Decoder trait. Custom jar</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>should be available in kafka/libs<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th>directory. (default: kafka.</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 4<br><br></th>
    <th>serializer.StringDecoder)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 5<br><br></th>
    <th>--max-message-size <Integer: size> Size of largest message. (default:</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 6<br><br></th>
    <th>5242880)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 7<br><br></th>
    <th>--print-data-log if set , printing the messages content<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 8<br><br></th>
    <th>when dumping data logs</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 9<br><br></th>
    <th>--value-decoder-class if set , used to deserialize the<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 0<br><br></th>
    <th>messages. This class should</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 1<br><br></th>
    <th>implement kafka.serializer.Decoder</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 2<br><br></th>
    <th>trait. Custom jar should be</th>
  </tr>
</table>


<table>
  <tr>
    <th>2<br><br>3<br></th>
    <th>available in kafka/libs directory.<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 4<br><br></th>
    <th>(default: kafka.serializer.</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 5<br><br></th>
    <th>StringDecoder)</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 6<br><br></th>
    <th>--verify-index-only if set , just verify the index log<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 7<br><br></th>
    <th>without printing its content</th>
  </tr>
</table>


很明显，我们在使⽤ kafka.tools.DumpLogSegments 的时候必须输⼊ -files，这个参数指 的就是 中Topic分区所在的绝对路径。分区所在的⽬录由 config/server.properties ⽂件 中 log.dirs 参数决定。⽐如我们想看/home/q/kafka/kafka_2.10-0.8.2.1/data/test4/ 034245135.log⽇志⽂件的相关情况可以 使⽤下⾯的命令：

Kafka

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>[iteblog@www.iteblog.com /]$ bin/kafka-run-class.sh kafka.tools.DumpLogSegments --files /iteblog/data/<br><br>test<br><br>-4/00000000000034245135.log</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th>Dumping /home/q/kafka/kafka_2.10-0.8.2.1/data/ test<br><br>-4/00000000000034245135.log</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>Starting offset: 34245135</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>offset: 34245135 position: 0 isvalid: true payloadsize: 4213 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>865449274 keysize: 4213</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>offset: 34245136 position: 8452 isvalid: true payloadsize: 4657 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>4123037760 keysize: 4657</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>offset: 34245137 position: 17792 isvalid: true payloadsize: 3921 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>541297511 keysize: 3921</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>offset: 34245138 position: 25660 isvalid: true payloadsize: 2290 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>1346104996 keysize: 2290</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th>offset: 34245139 position: 30266 isvalid: true payloadsize: 2284 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>1930558677 keysize: 2284</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>offset: 34245140 position: 34860 isvalid: true payloadsize: 268 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>57847488 keysize: 268</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>offset: 34245141 position: 35422 isvalid: true payloadsize: 263 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>2964399224 keysize: 263</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>offset: 34245142 position: 35974 isvalid: true payloadsize: 1875 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>647039113 keysize: 1875</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>offset: 34245143 position: 39750 isvalid: true payloadsize: 648 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>865445580 keysize: 648</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th>offset: 34245144 position: 41072 isvalid: true payloadsize: 556 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>1174686061 keysize: 556</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 4<br><br></th>
    <th>offset: 34245145 position: 42210 isvalid: true payloadsize: 4211 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>3691302513 keysize: 4211</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 5<br><br></th>
    <th>offset: 34245146 position: 50658 isvalid: true payloadsize: 2299 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>2367114411 keysize: 2299</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 6<br><br></th>
    <th>offset: 34245147 position: 55282 isvalid: true payloadsize: 642 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>4122061921 keysize: 642</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 7<br><br></th>
    <th>offset: 34245148 position: 56592 isvalid: true payloadsize: 4211 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>3257991653 keysize: 4211</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 8<br><br></th>
    <th>offset: 34245149 position: 65040 isvalid: true payloadsize: 2278 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>2103489307 keysize: 2278</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 9<br><br></th>
    <th>offset: 34245150 position: 69622 isvalid: true payloadsize: 269 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>792857391 keysize: 269</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 0<br><br></th>
    <th>offset: 34245151 position: 70186 isvalid: true payloadsize: 640 magic: 0 compresscodec: NoCompressionCodec crc:<br><br>791599616 keysize: 640</th>
  </tr>
</table>


可以看出，这个命令将 中Mesage中Header的相关信息和偏移量都显示出来了，但是没有看到 ⽇志的内容，我们可以通过 -print-data-log来设置。如果需要查看多个⽇志⽂件，可以以逗号分割。

Kafka

# 导出Zokeper中Group相关的偏移量

有时候我们需要导出某个Consumer group各个分区的偏移量，我们可以通过使⽤Kafka的 kafka.tools.ExportZkOffsets 类来满⾜。来看看这个类需要的参数：

<table>
  <tr>
    <th>1</th>
    <th>[iteblog@www.iteblog.com /]$ bin/kafka-run-class.sh kafka.tools.ExportZkOffsets</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>Export consumer offsets to an output file<br><br>.</th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>Option Description</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>------ ----------<br><br>-</th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>--group Consumer group.</th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th>--help Print this message.</th>
  </tr>
</table>


<table>
  <tr>
    <th>7</th>
    <th>--outputfile Output file<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>8</th>
    <th>--zkconnect ZooKeeper connect string. (default:</th>
  </tr>
</table>


<table>
  <tr>
    <th>9</th>
    <th>localhost:2181)</th>
  </tr>
</table>


我们需要输⼊Consumer group，Zokeper的地址以及保存⽂件路径：

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>[iteblog@www.iteblog.com /]$ bin/kafka-run-class.sh kafka.tools.ExportZkOffsets --group spark --zkconnect www.iteblog.com:2181 --output-<br><br>file ~/offset<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>[iteblog@www.iteblog.com /]$ vim ~/offset</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>/consumers/spark/offsets/ test /3:34846274<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>/consumers/spark/offsets/ test /2:34852378<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>/consumers/spark/offsets/ test /1:34852360<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>/consumers/spark/offsets/ test /0:34848170<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th>/consumers/spark/offsets/ test /6:34857010<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>/consumers/spark/offsets/ test /5:34854268<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>/consumers/spark/offsets/ test /4:34861572<br><br></th>
  </tr>
</table>


注意， --output-file 参数必须在指定，否则会出错。

# 通过JMX获取metrics信息

我们可以通过 kafka.tools.JmxTool 类打印出Kafka相关的metrics信息。

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>[iteblog@www.iteblog.com /]$ bin/kafka-run-class.sh kafka.tools.JmxTool</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th>Dump JMX values to standard output.</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>Option Description</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>------ ----------<br><br>-</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>--attributes <name> The whitelist of attributes to query.</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>This is a comma-separated list. If</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>no attributes are specified all</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th>objects will be queried.</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>-date<br><br>format < format > The date format to use for formatting<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>the time field. See java.text.<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>SimpleDateFormat for options.<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>--help Print usage information.</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th>--jmx-url <service-url> The url to connect to to poll JMX</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 4<br><br></th>
    <th>data. See Oracle javadoc for<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 5<br><br></th>
    <th>JMXServiceURL for details. (default:<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 6<br><br></th>
    <th>service:jmx:rmi:///jndi/rmi://:</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 7<br><br></th>
    <th>9999/jmxrmi)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 8<br><br></th>
    <th>--object-name <name> A JMX object name to use as a query.</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 9<br><br></th>
    <th>This can contain wild cards, and</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 0<br><br></th>
    <th>this option can be given multiple</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 1<br><br></th>
    <th>times to specify more than one<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 2<br><br></th>
    <th>query. If no objects are specified</th>
  </tr>
</table>


<table>
  <tr>
    <th>2<br><br>3<br></th>
    <th>all objects will be queried.</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 4<br><br></th>
    <th>--reporting-interval <Integer: ms> Interval in MS with which to poll jmx<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 5<br><br></th>
    <th>stats. (default: 2000)</th>
  </tr>
</table>


可以这么使⽤

<table>
  <tr>
    <th>1</th>
    <th>[iteblog@www.iteblog.com /]$ bin/kafka-run-class.sh kafka.tools.JmxTool --jmx-url service:jmx:rmi:///jndi/rmi://www.iteblog.com:1099/jmxrmi</th>
  </tr>
</table>


运⾏上⾯命令前提是在启动kafka集群的时候指定 export JMX_PORT= ，这样才会开启JMX。然后 就可以通过上⾯命令打印出Kafka所有的metrics信息。

# Kafka数据迁移⼯具

这个⼯具主要有两个： kafka.tools.KafkaMigrationTool 和 kafka.tools.MirrorMaker 。第⼀个主要是⽤于将Kafka 0.7上⾯的数据迁移到Kafka 0.8（

htp s:/cwiki.apache.org/confluence/display/KAFKA/Migrating+from+0.7+to+0.8

）；⽽后者可以同步两 个Kafka集群的数据（

htps:/cwiki.apache.org/confluence/pages/viewpage.action?pageId=278463 0

）。都是从原端消费Mesages，然后发布到⽬标端。

<table>
  <tr>
    <th>1</th>
    <th>[iteblog@www.iteblog.com /]$ bin/kafka-run-class.sh kafka.tools.KafkaMigrationTool --kafka.07.jar kafka-0.7.19.jar -zkclient.01.jar zkclient-0.2.0.jar --num.producers 16 -consumer.config=sourceCluster2Consumer.config -producer.config=targetClusterProducer.config --whitelist=.*</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>[iteblog@www.iteblog.com /]$ bin/kafka-run-class.sh kafka.tools.MirrorMaker --consumer.config<br><br>sourceCluster1Consumer.config --consumer.config<br>sourceCluster2Consumer.config --num.streams 2 --producer.config targetClusterProducer.config --whitelist=<br><br><br>".*"</th>
  </tr>
</table>


# ⽇志重放⼯具

这个⼯具主要作⽤是从⼀个Kafka集群⾥⾯读取指定Topic的消息，并将这些消息发送到其他集群 的指定topic中：

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>[iteblog@www.iteblog.com /]$ bin/kafka-replay-log-producer.sh</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th>Missing required argument "[broker-list]"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>Option Description</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>------ ----------<br><br>-</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>--broker-list < hostname :port> REQUIRED: the broker list must be<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>specified.</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>--inputtopic <input-topic> REQUIRED: The topic to consume from.</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th>--messages <Integer: count> The number of messages to send.</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>(default: -1)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>--outputtopic <output-topic> REQUIRED: The topic to produce to</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>--property <producer properties> A mechanism to pass<br><br>properties in the<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>form key=value to the producer. This</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th>allows the user to override producer</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 4<br><br></th>
    <th>properties that are not exposed by</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 5<br><br></th>
    <th>the existing command line arguments<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 6<br><br></th>
    <th>--reporting-interval <Integer: size> Interval at which to print progress<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 7<br><br></th>
    <th>info. (default: 5000)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 8<br><br></th>
    <th>-sync If set message send requests to the<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 9<br><br></th>
    <th>brokers are synchronously, one at a</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 0<br><br></th>
    <th>time as they arrive.<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 1<br><br></th>
    <th>--threads <Integer: threads> Number of sending threads. (default: 1)</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 2<br><br></th>
    <th>--zookeeper <zookeeper url> REQUIRED: The connection string<br><br>for</th>
  </tr>
</table>


<table>
  <tr>
    <th>2<br><br>3<br></th>
    <th>the zookeeper connection in the form<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 4<br><br></th>
    <th>host:port. Multiple URLS can be</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 5<br><br></th>
    <th>given to allow fail-over. (default:</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 6<br><br></th>
    <th>127.0.0.1:2181)</th>
  </tr>
</table>


Simple Consume脚本

kafka-simple-consumer-shell.sh ⼯具主要是使⽤Simple Consumer API从指定Topic的分区 读取数据并打印在终端：

<table>
  <tr>
    <th>1</th>
    <th>bin/kafka-simple-consumer-shell.sh --broker-list www.iteblog.com:9092 --topic<br><br>test<br><br>--partition 0</th>
  </tr>
</table>


更新Zokeper中的偏移量

kafka.tools.UpdateOffsetsInZK ⼯具可以更新Zokeper中指定Topic所有分区的偏移量， 可以指定成 earliest或者latest：

<table>
  <tr>
    <th>1</th>
    <th>[iteblog@www.iteblog.com /]$ bin/kafka-run-class.sh kafka.tools.UpdateOffsetsInZK</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>USAGE: kafka.tools.UpdateOffsetsInZK$ [earliest | latest] consumer.properties topic</th>
  </tr>
</table>


需要指定是更新成earliest或者latest，consumer.properties⽂件的路径以及topic的名称

