Spark Streaming Spark Streaming makes it easy to buildscalable fault-tolerant streaming aplications Spark StreamingQ轻易的构建⼀个容错的streamingaplications

- 1. SparkStack
- 2. SparkStreaming概览

- 3. 原理：将输⼊的数据切分成⼀个个批次

- 4. Dstreams离散流


spark的栈 spark sql：相当于hive，将sql解析成rd的transformation spark streaming：流式处理，相当于storm Mlib：机械学习，数学知识要求很⾼ GrathX：图计算 ApacheSpark：spark的核⼼代码

由消息队列向spark streaming⽣产数据，在spark streaming上执⾏，最后存储到数据底层，或者展⽰ 在控制台

spark steaming不是严格的实时计算，他是分批次的提交任务，任务间隔在毫秒值范围内。

Spark Streaming provides a high-levelabstraction caled discretized stream or DStream, which represents a continuoustream of data. DStreams can be created either from input data streams fromsources such as Kafka, Flume, and Kinesis, or by aplying high-level operationson other DStreams. Internaly, a DStream is represented as a sequence of RDs

# 演⽰helloword

- 5.1.创建scala项⽬，并导包


- 5.2.创建scala程序

<table>
  <tr>
    <th>package org.apache.wangsf<br><br>por or.apa .park.SparkConf importorg.apa e.spar.streaming. treamingContext import org.apache.spark.streaming.Seconds<br><br>object TCPWordCount { def main(args: Aray[String]) { /setMaster("local[2]")本地执⾏2个线程，⼀个⽤来接收消息，⼀个⽤来计算 valconf = new SparkConf().setMaster("local[2]").setApName("TCPWordCount")<br><br>/创建spark的streaming,传⼊间隔多长时间处理⼀次，间隔在5秒左右，否则打印控制台信息会被 冲掉<br><br>valsc =new StreamingContext(conf,Seconds(5) /读取数据的地址：从某个ip和端⼜收集数据 vallines = sc.socketTextStream("192.168.56.157", 8) /进⾏rd处理 valresults =lines.flatMap(_.split(" ").map(_,1).reduceByKey(_+_)<br><br>/将结果打印控制台 result.print()<br><br>/启动spark streaming sc.start() /等待终⽌<br><br>sc.awaitTermination() }<br><br>}</th>
  </tr>
</table>


- 5.3.在linux对应的机器上发送模拟命令


启动模拟窗⼜

nc -lk 8

在模拟窗⼜上输⼊数据

helo world

# 更新状态

在上⾯的计算过程中，数据时批次处理的，但是不是历史数据的求和，我们需要⽤到⼀个函数如下：

- 6.1.更新数据的流式处理


<table>
  <tr>
    <th>package org.apache.wangsf<br><br>poror.apa .park.SparkConf importorg.apache.spark.streaming.Seconds importorg.apache.spark.streaming.StreamingContext i portor.apahe.spark.HashPartitioner<br><br>pororg.apa e.og.oger pororg.apache.log4j.Level<br><br>import org.slf4j.LogerFactory object StateFulWordCount {<br><br>/*<br><br>tring:某个单词 Seq：[1,1,1,1,1,1]，当前批次出现的次数的序列 Option:历史的结果的sum<br><br>*/ val updateFunction = (iter: Iterator[(String,Seq[Int],Option[Int])])=> {<br><br>/将iter中的历史次数和现有的数据叠加，然后将单词和出现的最后次数输出 /iter.flatMap(t=>Some(t._2.sum + t._3.getOrElse(0).map(x=>(t._1,x)<br><br>iter.flatMap{case(x,y,z)=>Some(y.sum+z.getOrElse(0).map(v =>(x,v)} }<br><br>def main(args: Aray[String]) { /设置⽇志级别 Loger.getRotLoger.setLevel(Level.WARN) /创建 al conf = new SparkConf().setMaster("local[2]").setApName("StateFulWordCount")<br><br>val sc = new StreamingContext(conf,Seconds(5) /回滚点设置在本地 /sc.checkpoint("./") /将回滚点写到hdfs sc.checkpoint("hdfs:/master1 9 0/ceshi")<br><br>val lines =sc.socketTextStream("192.168.56.157", 8) /*<br><br>updateStateByKey()更新数据<br><br>1、更新数据的具体实现函数<br>2、分区信息<br>3、bolean值<br><br><br>*/<br><br>val results = lines.flatMap(_.split(" ").map(_,1).updateStateByKey(updateFunction,new HashPartitioner(sc.sparkContext.defaultParalelism),true)<br><br>results.print() sc.start() sc.awaitTermination()<br><br>}</th>
  </tr>
</table>


}

## 6.2.线上模式

打包

export

上传

将jar包上传到spark的机器

提交

spark-submit -clasorg.apache.wangsf.StateFulWordCount statefulwordcount.jar

测试

启动模拟窗⼜

nc -lk 8

在模拟窗⼜上输⼊数据

helo world

# 7. 从flume拉取数据

- 7.1.代码


<table>
  <tr>
    <th>package org.apache.wangsf i portor.apahe.spar.HashPartitioner<br><br>por or.apa .park.SparkConf importorg.apace.spar.streaming. econds importorg.apa e.spar.streaming.StreamingContext importorg.apache.spark.streaming.flume.FlumeUtils<br><br>ortjava.net.InetSocketAdres import org.apache.spark.storage.StorageLevel object FlumeWordCount {<br><br>valupdateFunction = (iter: Iterator[(String,Seq[Int],Option[Int])]) => {<br><br>iter.flatMap{case(x,y,z)=>Some(y.sum+z.getOrElse(0).map(v =>(x,v)} }<br><br>def main(args: Aray[String]) {<br><br>alconf = new SparkConf().setMaster("local[2]").setApName("FlumeWordCount") val sc = new StreamingContext(conf,Seconds(5)<br><br>sc.checkpoint("hdfs:/master1 9 0/ceshi") /设置flume的多台地址<br><br>valadres = Seq(new InetSocketAdres("192.168.56.157", 9),new InetSocketAdres("192.168.56.156", 9)<br><br>/从fl 中拉取数据 valflumeStream =<br><br>FlumeUtils.createPolingStream(sc,adres,StorageLevel.MEMORY_AND_DISK) /将flume数据中body部分的aray转换为string，进⾏rd<br><br>valresults =flumeStream.flatMap(x =>new String(x.event.getBody().aray().split(" ").map(_,1).updateStateByKey(updateFunction,new HashPartitioner(sc.sparkContext.defaultParalelism),true)<br><br>results.print() s start() sc.awaitTermination()<br><br>}</th>
  </tr>
</table>


}

## 7.2. flume的agent

分为主动拉取和被动接收数据，我们经常⽤主动拉取的⽅式

<table>
  <tr>
    <th># Name the components on this agent 1sources = r1 1sinks = k1<br><br>a1.chanels = c1 # source<br><br>1.sores.r1.type = spoldir a1.soures.r1.spolDir =/var/log/flume a1.sources.r1.fileHeader = true<br><br># Describe the sink a1.sinks.k1.type = org.apache.spark.streaming.flume.sink.SparkSink a1.snks.k1.hstname = 192.168.45.85 a1.sinks.k1.port = 9<br><br># Use a chanel which bufers events in memory a1.cane.c1.type = memory a1.can .c1.capacity = 1 0 a1.chanels.c1.transactionCapacity = 10 # Bind the source and sink to the chanel a1.sources.r1.chanels = c1</th>
  </tr>
</table>


a1.sinks.k1.chanel = c1

- 7.3.上传依赖包

- 7.4.创建⽂件夹

- 7.5.启动

启动flume

<table>
  <tr>
    <th>flume-ng agent -n a1 -c /home/hadop/flume/conf/ -f /home/hadop/flume /conf/flume-pol.conf</th>
  </tr>
</table>


-Dflume.rot.loger=WARN,console

再启动spark-streaming应⽤程序

- 7.6.测试


将依赖包上传到flume的lib下

在flume的根⽬录下创建test⽂件夹

1.

1.

向flume监听的⽂件夹下⽅⽂件即可

- 8. spark从kafka拉取数据


## 8.1.代码

<table>
  <tr>
    <th>package org.apache.wangsf import java.net.InetSocketAdres i portor.apahe.spar.HashPartitioner<br><br>por or.apa .park.SparkConf portorg.apa e.spar.storage.StorageLevel<br><br>importorg.apace.spar.streaming. econds importorg.apa e.spar.streaming.StreamingContext importorg.apa e.spar.streami g.flume.FlumeUtils import org.apache.spark.streaming.kafka.KafkaUtils<br><br>object KafkaWordCount { valupdateFunction = (iter: Iterator[(String, Seq[Int], Option[Int])]) => { iter.flatMap { case (x, y, z) =>Some(y.sum +z.getOrElse(0).map(v => (x, v) }<br><br>} def main(args: Aray[String] {<br><br>alconf = new SparkConf().setMaster("local[2]").setApName("FlumeWordCount") val sc = new StreamingContext(conf, Seconds(5)<br><br>sc.checkpoint("hdfs:/master1 9 0/ceshi")<br><br>a Aray(zkQuorum, groupId, topics, numThreads) = args altopicMap =topics.split(",").map(_,numThreads.toInt).toMap allines = KafkaUtils.createStream(sc, zkQuorum, groupId, topicMap).map(_._2)<br><br>valresults =lines.flatMap(_.split(" ").map(_,1).updateStateByKey(updateFunction, new HashPartitioner(sc.sparkContext.defaultParalelism), true)<br><br>results.print() s start() sc.awaitTermination()<br><br>}</th>
  </tr>
</table>


}

## 8.2.其他步骤待续

结合kafka #⾸先启动zk bin/kafka-server-start.shconfig/server.properties #创建topic bin/kafka-topics.sh-create-zokeper192.168.80.10 2181-replication-factor 1-partitions 1topic wordcount #查看主题 bin/kafka-topics.sh-list -zokeper192.168.80.10 2181 #启动⼀个⽣产者发送消息 bin/kafka-console-producer.sh-broker-list192.168.80.10 9092-topic wordcount #启动spark-streaming应⽤程序

bin/spark-submit -clascn.itcast.spark.streaming.KafkaWordCount /rot/streaming1.0.jar192.168.80.10 2181 group1 wordcount 1

# 9. WindowOperations

计算⼀个时间段的数据，⽐如⼀分钟之内的wordcount

batch interval window length sliding interval window length and sliding interval must bemultiples of the batch interval of the source DStream

