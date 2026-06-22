Spark Spark

基于 通⽤计算平台，可以很好地扩展各种计算类型的应⽤，尤其是 提供了内建的计算库⽀ 持，像Spark Streaming、Spark SQL、MLlib、GraphX，这些内建库都提供了⾼级抽象，可以⽤⾮常 简洁的代码实现复杂的计算逻辑、这也得益于Scala编程语⾔的简洁性。这⾥，我们基于1.3.0版本的 Spark搭建了计算平台，实现基于Spark Streaming的实时计算。

我们的应⽤场景是分析⽤户使⽤⼿机Ap的⾏为，描述如下所示：

- 1、⼿机客户端会收集⽤户的⾏为事件（我们以点击事件为例），将数据发送到数据服务器，我们

假设这⾥直接进⼊到 消息队列

- 2、后端的实时服务会从 消费数据，将数据读出来并进⾏实时分析，这⾥选择Spark

Streaming，因为Spark Streaming提供了与Kafka整合的内置⽀持

- 3、经过Spark Streaming实时计算程序分析，将结果写⼊ ，可以实时获取⽤户的⾏为数据，


Kafka

Kafka

Redis

并可以导出进⾏离线综合统计分析

# Kafka+Spark Streaming+Redis编程实践

下⾯，我们根据上⾯提到的应⽤场景，来编程实现这个实时计算应⽤。⾸先，写了⼀个Kafka Producer模拟程序，⽤来模拟向Kafka实时写⼊⽤户⾏为的事件数据，数据是JSON格式，示例如下：

<table>
  <tr>
    <th>{<br><br>"uid": "068b746ed4620d25e2605a9f804385f", "event_time": "1430204612405", "os_type": "Android", "click_count": 6</th>
  </tr>
</table>


}

⼀个事件包含4个字段：

- 1、uid：⽤户编号
- 2、event_time：事件发⽣时间戳
- 3、os_type：⼿机Ap操作系统类型
- 4、click_count：点击次数


下⾯是我们实现的代码，如下所示：

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>package com.iteblog.spark.streaming.utils<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>import java.util.Properties<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0</th>
    <th>import</th>
  </tr>
</table>


- 4 scala.util.Properties

<table>
  <tr>
    <th>0</th>
    <th>import</th>
  </tr>
</table>


- 5 org.codehaus.jetison.json.JSONObject


<table>
  <tr>
    <th>0</th>
    <th>import</th>
  </tr>
</table>


- 6 kafka.javapi.producer.Producer

<table>
  <tr>
    <th>0</th>
    <th>import</th>
  </tr>
</table>


- 7 kafka.producer.KeyedMesage

<table>
  <tr>
    <th>0</th>
    <th>import</th>
  </tr>
</table>


- 8 kafka.producer.KeyedMesage

<table>
  <tr>
    <th>0</th>
    <th>import</th>
  </tr>
</table>


- 9 kafka.producer.ProducerConfig


<table>
  <tr>
    <th>1</th>
    <th>import</th>
  </tr>
</table>


0 scala.util.Random

<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>object KafkaEventProducer {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>1 4<br><br></th>
    <th>private val users<br><br>= Array(<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 5<br><br></th>
    <th>"4A4D769EB9679C054DE81B973ED5D768" , "8dfeb5aaafc027d89349ac9a20b3930f" ,<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 6<br><br></th>
    <th>"011BBF43B89BFBF266C865DF0397AA71" , "f2a8474bf7bd94f0aabbd4cdd2c06dcf" ,<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 7<br><br></th>
    <th>"068b746ed4620d25e26055a9f804385f" , "97edfc08311c70143401745a03a50706" ,<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 8<br><br></th>
    <th>"d7f141563005d1b5d0d3dd30138f3f62" , "c8ee90aade1671a21336c721512b817a" ,<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 9<br><br></th>
    <th>"6b67c8c700427dee7552f81f3228c927" , "a95f22eabc4fd4b580c011a3161a9d9d" )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 0<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>2 1<br><br></th>
    <th>private val random<br><br>= new Random()<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 2<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>2<br><br>3<br></th>
    <th>private var pointer<br><br>= 1<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 4<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>2 5<br><br></th>
    <th>def getUserID() : String<br><br>= {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 6<br><br></th>
    <th>pointer<br><br>= pointer + 1<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 7<br><br></th>
    <th>if (pointer ><br><br>= users.length) {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 8<br><br></th>
    <th>pointer<br><br>= 0<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 9<br><br></th>
    <th>users(pointer)</th>
  </tr>
</table>


<table>
  <tr>
    <th>3 0<br><br></th>
    <th>} else {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3 1<br><br></th>
    <th>users(pointer)</th>
  </tr>
</table>


<table>
  <tr>
    <th>3 2<br><br></th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>3 3<br><br></th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>3<br><br>4<br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>3 5<br><br></th>
    <th>def click() : Double<br><br>= {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3 6<br><br></th>
    <th>random.nextInt( 10 )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3 7<br><br></th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>3 8<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>3 9<br><br></th>
    <th>// bin/kafka-topics.sh --zookeeper zk1:2181,zk2:2181,zk3:2181/kafka -<br><br>-create --topic user_events --replication-factor 2 --partitions 2</th>
  </tr>
</table>


<table>
  <tr>
    <th>4 0<br><br></th>
    <th>// bin/kafka-topics.sh --zookeeper zk1:2181,zk2:2181,zk3:2181/kafka -<br><br>-list</th>
  </tr>
</table>


<table>
  <tr>
    <th>4 1<br><br></th>
    <th>// bin/kafka-topics.sh --zookeeper zk1:2181,zk2:2181,zk3:2181/kafka -<br><br>-describe user_events</th>
  </tr>
</table>


<table>
  <tr>
    <th>4 2<br><br></th>
    <th>// bin/kafka-console-consumer.sh --zookeeper zk1:2181,zk2:2181,zk3:22181/kafka --topic test_json_basis_event -from-beginning</th>
  </tr>
</table>


<table>
  <tr>
    <th>4 3<br><br></th>
    <th>def main(args : Array[String]) : Unit<br><br>= {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>4 4<br><br></th>
    <th>val topic<br><br>= "user_events"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>4<br><br>5<br></th>
    <th>val brokers<br><br>= "10.10.4.126:9092,10.10.4.127:9092"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>4 6<br><br></th>
    <th>val props<br><br>= new Properties()<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>4 7<br><br></th>
    <th>props.put( "metadata.broker.list" , brokers)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>4 8<br><br></th>
    <th>props.put( "serializer.class" , "kafka.serializer.StringEncoder" )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>4 9<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>5 0<br><br></th>
    <th>val kafkaConfig<br><br>= new ProducerConfig(props)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>5 1<br><br></th>
    <th>val producer<br><br>= new Producer[String, String](kafkaConfig)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>5 2<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>5 3<br><br></th>
    <th>while ( true ) {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>5 4<br><br></th>
    <th>// prepare event data</th>
  </tr>
</table>


<table>
  <tr>
    <th>5 5<br><br></th>
    <th>val event<br><br>= new JSONObject()<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>5<br><br>6<br></th>
    <th>event</th>
  </tr>
</table>


<table>
  <tr>
    <th>5 7<br><br></th>
    <th>.put( "uid" , getUserID)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>5 8<br><br></th>
    <th>.put( "event_time" , System.currentTimeMillis.toString)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>5 9<br><br></th>
    <th>.put( "os_type" , "Android" )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>6 0<br><br></th>
    <th>.put( "click_count" , click)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>6 1<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>6 2<br><br></th>
    <th>// produce event message</th>
  </tr>
</table>


<table>
  <tr>
    <th>6 3<br><br></th>
    <th>producer.send( new KeyedMessage[String, String](topic, event.toString))<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>6 4<br><br></th>
    <th>println( "Message sent: "<br><br>+ event)</th>
  </tr>
</table>


<table>
  <tr>
    <th>6 5<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>6 6<br><br></th>
    <th>Thread.sleep( 200 )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>6<br><br>7<br></th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>6 8<br><br></th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>6 9<br><br></th>
    <th>}</th>
  </tr>
</table>


## 通过控制上⾯程序最后⼀⾏的时间间隔来控制模拟写⼊速度。下⾯我们来讨论实现实时统计每个 ⽤户的点击次数，它是按照⽤户分组进⾏累加次数，逻辑⽐较简单，关键是在实现过程中要注意⼀些 问题，如对象序列化等。先看实现代码，稍后我们再详细讨论，代码实现如下所示：

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>object UserClickCountAnalytics {<br><br></th>
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
    <th>def main(args : Array[String]) : Unit<br><br>= {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>var masterUrl<br><br>= "local[1]"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>if (args.length > 0 ) {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>masterUrl<br><br>= args(<br><br>0 )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>// Create a StreamingContext with the given master URL</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>val conf<br><br>= new SparkConf().setMaster(masterUrl).setAppName( "UserClickCountStat" )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>val ssc = new StreamingContext(conf, Seconds( 5 ))<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th>// Kafka configurations</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 4<br><br></th>
    <th>val topics<br><br>= Set( "user_events" )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 5<br><br></th>
    <th>val brokers<br><br>= "10.10.4.126:9092,10.10.4.127:9092"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 6<br><br></th>
    <th>val kafkaParams<br><br>= Map[String, String](<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 7<br><br></th>
    <th>"metadata.broker.list"<br><br>-> brokers, "serializer.class"<br><br>-> "kafka.serializer.StringEncoder" )<br><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 8<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>1 9<br><br></th>
    <th>val dbIndex<br><br>= 1<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 0<br><br></th>
    <th>val clickHashKey<br><br>= "app::users::click"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 1<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>2 2<br><br></th>
    <th>// Create a direct stream</th>
  </tr>
</table>


<table>
  <tr>
    <th>2<br><br>3<br></th>
    <th>val kafkaStream<br><br>= KafkaUtils.createDirectStream[String, String, StringDecoder,<br><br>StringDecoder](ssc, kafkaParams, topics)</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 4<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>2 5<br><br></th>
    <th>val events<br><br>= kafkaStream.flatMap(line<br><br>= > {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 6<br><br></th>
    <th>val data<br><br>= JSONObject.fromObject(line.<br><br>_ 2 )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 7<br><br></th>
    <th>Some(data)</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 8<br><br></th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>2 9<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>3 0<br><br></th>
    <th>// Compute user click times</th>
  </tr>
</table>


<table>
  <tr>
    <th>3 1<br><br></th>
    <th>val userClicks<br><br>= events.map(x<br><br>= > (x.getString( "uid" ), x.getInt( "click_count" ))).reduceByKey(<br><br>_ + _ )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3 2<br><br></th>
    <th>userClicks.foreachRDD(rdd<br><br>= > {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3 3<br><br></th>
    <th>rdd.foreachPartition(partitionOfRecords<br><br>= > {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3<br><br>4<br></th>
    <th>partitionOfRecords.foreach(pair<br><br>= > {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3 5<br><br></th>
    <th>val uid =<br><br>pair.<br><br>_ 1<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3 6<br><br></th>
    <th>val clickCount<br><br>= pair.<br><br>_ 2<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3 7<br><br></th>
    <th>val jedis<br><br>= < span<br><br>class<br><br>= "wp_keywordlink_affiliate" >< a href<br><br>= " " title<br><br>= "" target<br><br>= "_blank" data-original-title<br><br>= "View all posts in Redis" > Redis <<br><br>/a >< /span<br><br>> Client.pool.getResource<br><br>htp:/ w.iteblog.com/archives/tag/redis</th>
  </tr>
</table>


<table>
  <tr>
    <th>3 8<br><br></th>
    <th>jedis.select(dbIndex)</th>
  </tr>
</table>


<table>
  <tr>
    <th>3 9<br><br></th>
    <th>jedis.hincrBy(clickHashKey, uid, clickCount)</th>
  </tr>
</table>


<table>
  <tr>
    <th>4 0<br><br></th>
    <th>RedisClient.pool.returnResource(jedis)</th>
  </tr>
</table>


<table>
  <tr>
    <th>4 1<br><br></th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>4 2<br><br></th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>4 3<br><br></th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>4 4<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>4<br><br>5<br></th>
    <th>ssc.start()</th>
  </tr>
</table>


<table>
  <tr>
    <th>4 6<br><br></th>
    <th>ssc.awaitTermination()</th>
  </tr>
</table>


<table>
  <tr>
    <th>4 7<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>4 8<br><br></th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>4 9<br><br></th>
    <th>}</th>
  </tr>
</table>


## 上⾯代码使⽤了Jedis客户端来操作Redis，将分组计数结果数据累加写⼊Redis存储，如果其他系 统需要实时获取该数据，直接从Redis实时读取即可。RedisClient实现代码如下所示：

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>object RedisClient extends Serializable {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th>val redisHost<br><br>= "10.10.4.130"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>val redisPort<br><br>= 6379<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>val redisTimeout<br><br>= 30000<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>lazy val pool<br><br>= new JedisPool( new GenericObjectPoolConfig(), redisHost, redisPort, redisTimeout)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>lazy val hook<br><br>= new Thread {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th>override def run<br><br>= {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>println( "Execute hook thread: "<br><br>+ this )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>pool.destroy()</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th>sys.addShutdownHook(hook.run)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 4<br><br></th>
    <th>}</th>
  </tr>
</table>


## 上⾯代码我们分别在local[K]和Spark Standalone集群模式下运⾏通过。

如果我们是在开发环境进⾏调试的时候，也就是使⽤local[K]部署模式，在本地启动K个Worker线 程来计算，这K个Worker在同⼀个JVM实例⾥，上⾯的代码默认情况是，如果没有传参数则是local[K] 模式，所以如果使⽤这种⽅式在创建Redis连接池或连接的时候，可能⾮常容易调试通过，但是在使⽤ Spark Standalone、YARN Client（YARN Cluster）或Mesos集群部署模式的时候，就会报错，主要是 由于在处理Redis连接池或连接的时候出错了。我们可以看⼀下Spark架构，如图所示（来⾃官⽹）：

⽆论是在本地模式、Standalone模式，还是在Mesos或YARN模式下，整个Spark集群的结构都可 以⽤上图抽象表示，只是各个组件的运⾏环境不同，导致组件可能是分布式的，或本地的，或单个 JVM实例的。如在本地模式，则上图表现为在同⼀节点上的单个进程之内的多个组件；⽽在YARN Client模式下，Driver程序是在YARN集群之外的⼀个节点上提交Spark Aplication，其他的组件都运⾏ 在YARN集群管理的节点上。

在Spark集群环境部署Aplication后，在进⾏计算的时候会将作⽤于RD数据集上的函数 （Functions）发送到集群中Worker上的Executor上（在Spark Streaming中是作⽤于DStream的操 作），那么这些函数操作所作⽤的对象（Elements）必须是可序列化的，通过Scala也可以使⽤lazy引 ⽤来解决，否则这些对象（Elements）在跨节点序列化传输后，⽆法正确地执⾏反序列化重构成实际 可⽤的对象。上⾯代码我们使⽤lazy引⽤（Lazy Reference）来实现的，代码如下所示：

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>// lazy pool reference</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th>lazy val pool<br><br>= new JedisPool( new GenericObjectPoolConfig(), redisHost, redisPort, redisTimeout)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>...</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>partitionOfRecords.foreach(pair<br><br>= > {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>val uid =<br><br>pair.<br><br>_ 1<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>val clickCount<br><br>= pair.<br><br>_ 2<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>val jedis<br><br>= RedisClient.pool.getResource<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th>jedis.select(dbIndex)</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>jedis.hincrBy(clickHashKey, uid, clickCount)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>RedisClient.pool.returnResource(jedis)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>})</th>
  </tr>
</table>


## 另⼀种⽅式，我们将代码修改为，把对Redis连接的管理放在操作DStream的Output操作范围之 内，因为我们知道它是在特定的Executor中进⾏初始化的，使⽤⼀个单例的对象来管理，如下所示：

<table>
  <tr>
    <th>0<br><br>0<br>1<br></th>
    <th>package org.shirdrn.spark.streaming<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 2</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 3</th>
    <th>import org.apache.commons.pool 2<br><br>.impl.GenericObjectPoolConfig</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 4</th>
    <th>import org.apache.spark.SparkConf<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 5</th>
    <th>import org.apache.spark.streaming.Seconds<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 6</th>
    <th>import org.apache.spark.streaming.StreamingContext<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 7</th>
    <th>import org.apache.spark.streaming.dstream.DStream.toPairDStreamFunctions<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 8</th>
    <th>import org.apache.spark.streaming.kafka.KafkaUtils<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 9</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br>1 0<br></th>
    <th>import kafka.serializer.StringDecoder<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br>1 1<br></th>
    <th>import net.sf.json.JSONObject<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br>1<br>2<br></th>
    <th>import redis.clients.jedis.JedisPool<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br>1 3<br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br>1 4<br></th>
    <th>object UserClickCountAnalytics {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br>1 5<br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>1 6<br></th>
    <th>def main(args : Array[String]) : Unit<br><br>= {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>1 7<br></th>
    <th>var masterUrl<br><br>= "local[1]"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>1 8<br></th>
    <th>if (args.length > 0 ) {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>1 9<br></th>
    <th>masterUrl<br><br>= args(<br><br>0 )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 0</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2<br><br>1<br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 2</th>
    <th>// Create a StreamingContext with the given master URL</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2<br>3<br></th>
    <th>val conf<br><br>= new SparkConf().setMaster(masterUrl).setAppName( "UserClickCountStat" )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 4</th>
    <th>val ssc = new StreamingContext(conf, Seconds( 5 ))<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 5</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 6</th>
    <th>// Kafka configurations</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 7</th>
    <th>val topics<br><br>= Set( "user_events" )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 8</th>
    <th>val brokers<br><br>= "10.10.4.126:9092,10.10.4.127:9092"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 9</th>
    <th>val kafkaParams<br><br>= Map[String, String](<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 0</th>
    <th>"metadata.broker.list"<br><br>-> brokers, "serializer.class"<br><br>-> "kafka.serializer.StringEncoder" )<br><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3<br><br>1<br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 2</th>
    <th>val dbIndex<br><br>= 1<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 3</th>
    <th>val clickHashKey<br><br>= "app::users::click"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3<br>4<br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 5</th>
    <th>// Create a direct stream</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 6</th>
    <th>val kafkaStream<br><br>= KafkaUtils.createDirectStream[String, String, StringDecoder,<br><br>StringDecoder](ssc, kafkaParams, topics)</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 7</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 8</th>
    <th>val events<br><br>= kafkaStream.flatMap(line<br><br>= > {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 9</th>
    <th>val data<br><br>= JSONObject.fromObject(line.<br><br>_ 2 )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 0</th>
    <th>Some(data)</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4<br><br>1<br></th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 2</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 3</th>
    <th>// Compute user click times</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 4</th>
    <th>val userClicks<br><br>= events.map(x<br><br>= > (x.getString( "uid" ), x.getInt( "click_count" ))).reduceByKey(<br><br>_ + _ )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4<br>5<br></th>
    <th>userClicks.foreachRDD(rdd<br><br>= > {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 6</th>
    <th>rdd.foreachPartition(partitionOfRecords<br><br>= > {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 7</th>
    <th>partitionOfRecords.foreach(pair<br><br>= > {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 8</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 9</th>
    <th>/**</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 0</th>
    <th>* Internal Redis client for managing Redis connection {@link Jedis} based on {@link RedisPool}</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5<br><br>1<br></th>
    <th>*/</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 2</th>
    <th>object InternalRedisClient extends Serializable {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 3</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 4</th>
    <th>@ transient private var pool : JedisPool<br><br>= null<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 5</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5<br>6<br></th>
    <th>def makePool(redisHost : String, redisPort : Int, redisTimeout : Int,<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 7</th>
    <th>maxTotal : Int, maxIdle : Int, minIdle : Int) : Unit<br><br>= {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 8</th>
    <th>makePool(redisHost, redisPort, redisTimeout, maxTotal, maxIdle,<br><br>minIdle, true , false , 10000 )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 9</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 0</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6<br><br>1<br></th>
    <th>def makePool(redisHost : String, redisPort : Int, redisTimeout : Int,<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 2</th>
    <th>maxTotal : Int, maxIdle : Int, minIdle : Int, testOnBorrow : Boolean,<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 3</th>
    <th>testOnReturn : Boolean, maxWaitMillis : Long) : Unit<br><br>= {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 4</th>
    <th>if (pool<br><br>== null ) {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 5</th>
    <th>val poolConfig<br><br>= new GenericObjectPoolConfig()<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 6</th>
    <th>poolConfig.setMaxTotal(maxTotal)</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6<br>7<br></th>
    <th>poolConfig.setMaxIdle(maxIdle)</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 8</th>
    <th>poolConfig.setMinIdle(minIdle)</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 9</th>
    <th>poolConfig.setTestOnBorrow(testOnBorrow)</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 0</th>
    <th>poolConfig.setTestOnReturn(testOnReturn)</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7<br><br>1<br></th>
    <th>poolConfig.setMaxWaitMillis(maxWaitMillis)</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 2</th>
    <th>pool<br><br>= new JedisPool(poolConfig, redisHost, redisPort, redisTimeout)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 3</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 4</th>
    <th>val hook<br><br>= new Thread{<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 5</th>
    <th>override def run = pool.destroy()<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 6</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 7</th>
    <th>sys.addShutdownHook(hook.run)</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7<br>8<br></th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 9</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 0</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8<br><br>1<br></th>
    <th>def getPool : JedisPool<br><br>= {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 2</th>
    <th>assert(pool !<br><br>= null )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 3</th>
    <th>pool</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 4</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 5</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 6</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 7</th>
    <th>// Redis configurations</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 8</th>
    <th>val maxTotal<br><br>= 10<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8<br>9<br></th>
    <th>val maxIdle<br><br>= 10<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 0</th>
    <th>val minIdle<br><br>= 1<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9<br><br>1<br></th>
    <th>val redisHost<br><br>= "10.10.4.130"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 2</th>
    <th>val redisPort<br><br>= 6379<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 3</th>
    <th>val redisTimeout<br><br>= 30000<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 4</th>
    <th>val dbIndex<br><br>= 1<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 5</th>
    <th>InternalRedisClient.makePool(redisHost, redisPort, redisTimeout, maxTotal, maxIdle, minIdle)</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 6</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 7</th>
    <th>val uid =<br><br>pair.<br><br>_ 1<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 8</th>
    <th>val clickCount<br><br>= pair.<br><br>_ 2<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 9</th>
    <th>val jedis<br><br>= InternalRedisClient.getPool.getResource<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 0</th>
    <th>jedis.select(dbIndex)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0<br>1<br></th>
    <th>jedis.hincrBy(clickHashKey, uid, clickCount)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0<br><br>2<br></th>
    <th>InternalRedisClient.getPool.returnResource(jedis)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 3</th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 4</th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 5</th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 6</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 7</th>
    <th>ssc.start()</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 8</th>
    <th>ssc.awaitTermination()</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 9</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>1 0</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>1 1</th>
    <th>}</th>
  </tr>
</table>


上⾯代码实现，得益于Scala语⾔的特性，可以在代码中任何位置进⾏clas或object的定义，我们 将⽤来管理Redis连接的代码放在了特定操作的内部，就避免了瞬态（Transient）对象跨节点序列化的 问题。这样做还要求我们能够了解Spark内部是如何操作RD数据集的，更多可以参考RD或Spark相 关⽂档。

在集群上，以Standalone模式运⾏，执⾏如下命令：

- 1

cd

/usr/ local /spark

- 2

./bin/spark-submit --class org.shirdrn.spark.streaming.UserClickCountAnalytics

- 3 --master spark://hadoop1:7077

- 4 --executor-memory 1G

- 5 --total-executor-cores 2

- 6 ~/spark-0.0.SNAPSHOT.jar spark://hadoop1:7077


可以查看集群中各个Worker节点执⾏计算任务的状态，也可以⾮常⽅便地通过Web⻚⾯查看。 下⾯，看⼀下我们存储到Redis中的计算结果，如下所示：

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>127.0<br><br>.<br><br>0.1 : 6379 [<br><br>1 ]> HGETALL app :: users :: click<br><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th>1 ) "4A4D769EB9679C054DE81B973ED5D768"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>2 ) "7037"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>3 ) "8dfeb5aaafc027d89349ac9a20b3930f"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>4 ) "6992"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>5 ) "011BBF43B89BFBF266C865DF0397AA71"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>6 ) "7021"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th>7 ) "97edfc08311c70143401745a03a50706"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>8 ) "6874"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>9 ) "d7f141563005d1b5d0d3dd30138f3f62"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>10 ) "7057"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>11 ) "a95f22eabc4fd4b580c011a3161a9d9d"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th>12 ) "7092"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 4<br><br></th>
    <th>13 ) "6b67c8c700427dee7552f81f3228c927"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 5<br><br></th>
    <th>14 ) "7266"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 6<br><br></th>
    <th>15 ) "f2a8474bf7bd94f0aabbd4cdd2c06dcf"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 7<br><br></th>
    <th>16 ) "7188"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 8<br><br></th>
    <th>17 ) "c8ee90aade1671a21336c721512b817a"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 9<br><br></th>
    <th>18 ) "6950"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2 0<br><br></th>
    <th>19 ) "068b746ed4620d25e26055a9f804385f"<br><br></th>
  </tr>
</table>


# pom⽂件及相关依赖

这⾥，附上前⾯开发的应⽤所对应的依赖，以及打包Spark Streaming应⽤程序的Maven配置，以 供参考。如果使⽤maven-shade-plugin插件，配置有问题的话，打包后在Spark集群上提交 Aplication时候可能会报错Invalid signature file digest for Manifest main atributes。参考的Maven配 置，如下所示：

查看源代码打印帮助

<table>
  <tr>
    <th>0<br><br>0<br>1<br></th>
    <th><project xmlns<br><br>= " " xmlns : xsi<br><br>= " "<br><br>htp:/maven.apache.org/POM/4.0.0</th>
  </tr>
</table>


## htp:/ w.w3.org/201/XMLSchema-instance

<table>
  <tr>
    <th>0<br><br>0 2</th>
    <th>xsi : schemaLocation<br><br>= " " > htp:/maven.apache.org/POM/4.0.0 htp:/maven.apache.org/xsd/maven-4.0.0.xsd<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 3</th>
    <th><modelVersion> 4.0<br><br>. 0 </modelVersion><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 4</th>
    <th><groupId>org.shirdrn.spark</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 5</th>
    <th><artifactId>spark</artifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 6</th>
    <th><version><br><br>0.0<br><br>.<br><br>1<br><br><br>-SNAPSHOT</version></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 7</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 8</th>
    <th><dependencies></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>0 9</th>
    <th><dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>1 0<br></th>
    <th><groupId>org.apache.spark</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br>1 1<br></th>
    <th><artifactId>spark-core _ 2.10 </artifactId><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br>1<br>2<br></th>
    <th><version> 1.3<br><br>. 0 </version><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>1 3<br></th>
    <th></dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>1 4<br></th>
    <th><dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br>1 5<br></th>
    <th><groupId>org.apache.spark</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br>1 6<br></th>
    <th><artifactId>spark-streaming _ 2.10 </artifactId><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>1 7<br></th>
    <th><version> 1.3<br><br>. 0 </version><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>1 8<br></th>
    <th></dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br>1 9<br></th>
    <th><dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 0</th>
    <th><groupId>net.sf.json-lib</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2<br><br>1<br></th>
    <th><artifactId>json-lib</artifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 2</th>
    <th><version> 2.3 </version><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2<br>3<br></th>
    <th></dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 4</th>
    <th><dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 5</th>
    <th><groupId>org.apache.spark</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 6</th>
    <th><artifactId>spark-streaming-kafka _ 2.10 </artifactId><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 7</th>
    <th><version> 1.3<br><br>. 0 </version><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 8</th>
    <th></dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>2 9</th>
    <th><dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 0</th>
    <th><groupId>redis.clients</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3<br><br>1<br></th>
    <th><artifactId>jedis</artifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 2</th>
    <th><version> 2.5<br><br>. 2 </version><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 3</th>
    <th></dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3<br>4<br></th>
    <th><dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 5</th>
    <th><groupId>org.apache.commons</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 6</th>
    <th><artifactId>commons-pool 2 </artifactId><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 7</th>
    <th><version> 2.2 </version><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 8</th>
    <th></dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>3 9</th>
    <th></dependencies></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 0</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4<br><br>1<br></th>
    <th><build></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 2</th>
    <th><sourceDirectory>${basedir}/src/main/scala</sourceDirectory></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 3</th>
    <th><testSourceDirectory>${basedir}/src/test/scala</testSourceDirectory></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 4</th>
    <th><resources></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4<br>5<br></th>
    <th><resource></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 6</th>
    <th><directory>${basedir}/src/main/resources</directory></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 7</th>
    <th></resource></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 8</th>
    <th></resources></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>4 9</th>
    <th><testResources></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 0</th>
    <th><testResource></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5<br><br>1<br></th>
    <th><directory>${basedir}/src/test/resources</directory></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 2</th>
    <th></testResource></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 3</th>
    <th></testResources></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 4</th>
    <th><plugins></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 5</th>
    <th><plugin></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5<br>6<br></th>
    <th><artifactId>maven-compiler-plugin</artifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 7</th>
    <th><version> 3.1 </version><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 8</th>
    <th><configuration></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>5 9</th>
    <th><source> 1.6 </source><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 0</th>
    <th><target> 1.6 </target><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6<br><br>1<br></th>
    <th></configuration></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 2</th>
    <th></plugin></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 3</th>
    <th><plugin></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 4</th>
    <th><groupId>org.apache.maven.plugins</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 5</th>
    <th><artifactId>maven-shade-plugin</artifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 6</th>
    <th><version> 2.2 </version><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6<br>7<br></th>
    <th><configuration></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 8</th>
    <th><createDependencyReducedPom> true </createDependencyReducedPom><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>6 9</th>
    <th></configuration></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 0</th>
    <th><executions></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7<br><br>1<br></th>
    <th><execution></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 2</th>
    <th><phase> package </phase><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 3</th>
    <th><goals></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 4</th>
    <th><goal>shade</goal></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 5</th>
    <th></goals></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 6</th>
    <th><configuration></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 7</th>
    <th><artifactSet></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7<br>8<br></th>
    <th><includes></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>7 9</th>
    <th><include>* :<br><br>*</include></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 0</th>
    <th></includes></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8<br><br>1<br></th>
    <th></artifactSet></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 2</th>
    <th><filters></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 3</th>
    <th><filter></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 4</th>
    <th><artifact>* :<br><br>*</artifact></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 5</th>
    <th><excludes></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 6</th>
    <th><exclude>META-INF/*.SF</exclude></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 7</th>
    <th><exclude>META-INF/*.DSA</exclude></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8 8</th>
    <th><exclude>META-INF/*.RSA</exclude></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>8<br>9<br></th>
    <th></excludes></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 0</th>
    <th></filter></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9<br><br>1<br></th>
    <th></filters></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 2</th>
    <th><transformers></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 3</th>
    <th><transformer</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 4</th>
    <th>implementation<br><br>= "org.apache.maven.plugins.shade.resource.ServicesResourceTransformer<br><br>"<br><br>/></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 5</th>
    <th><transformer</th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 6</th>
    <th>implementation<br><br>= "org.apache.maven.plugins.shade.resource.AppendingTransformer" ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 7</th>
    <th><resource>reference.conf</resource></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 8</th>
    <th></transformer></th>
  </tr>
</table>


<table>
  <tr>
    <th>0<br><br>9 9</th>
    <th><transformer</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 0</th>
    <th>implementation<br><br>= "org.apache.maven.plugins.shade.resource.DontIncludeResourceTransfor<br><br>mer" ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0<br>1<br></th>
    <th><resource>log 4 j.properties</resource><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0<br><br>2<br></th>
    <th></transformer></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 3</th>
    <th></transformers></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 4</th>
    <th></configuration></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 5</th>
    <th></execution></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 6</th>
    <th></executions></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 7</th>
    <th></plugin></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 8</th>
    <th></plugins></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>0 9</th>
    <th></build></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>1 0</th>
    <th></project></th>
  </tr>
</table>


