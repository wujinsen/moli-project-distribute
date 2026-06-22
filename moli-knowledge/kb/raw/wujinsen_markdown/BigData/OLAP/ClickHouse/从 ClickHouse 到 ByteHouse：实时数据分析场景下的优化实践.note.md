Spark Spark

基于 通⽤计算平台，可以很好地扩展各种计算类型的应⽤，尤其是 提供了内建的计算库⽀持，像 Spark Streaming、Spark SQL、MLlib、GraphX，这些内建库都提供了⾼级抽象，可以⽤⾮常简洁的代码实 现复杂的计算逻辑、这也得益于Scala编程语⾔的简洁性。这⾥，我们基于1.3.0版本的Spark搭建了计算平 台，实现基于Spark Streaming的实时计算。

我们的应⽤场景是分析⽤户使⽤⼿机Ap的⾏为，描述如下所示：

- 1、⼿机客户端会收集⽤户的⾏为事件（我们以点击事件为例），将数据发送到数据服务器，我们假设这

⾥直接进⼊到 消息队列

- 2、后端的实时服务会从 消费数据，将数据读出来并进⾏实时分析，这⾥选择Spark Streaming，因

为Spark Streaming提供了与Kafka整合的内置⽀持

- 3、经过Spark Streaming实时计算程序分析，将结果写⼊ ，可以实时获取⽤户的⾏为数据，并可以


Kafka

Kafka

Redis

导出进⾏离线综合统计分析

# Kafka+Spark Streaming+Redis编程实践

下⾯，我们根据上⾯提到的应⽤场景，来编程实现这个实时计算应⽤。⾸先，写了⼀个Kafka Producer 模拟程序，⽤来模拟向Kafka实时写⼊⽤户⾏为的事件数据，数据是JSON格式，示例如下：

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
    <th>01</th>
    <th>packagecom.iteblog.spark.streaming.u tils</th>
  </tr>
</table>


<table>
  <tr>
    <th>03</th>
    <th>importjava.util.Properties</th>
  </tr>
</table>


<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 04 importscala.util.Properties

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 05 importorg.codehaus.jetison.json.JSONObject


<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


## 06 importkafka.javapi.producer.Producer

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


## 07 importkafka.producer.KeyedMesage

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


## 08 importkafka.producer.KeyedMesage

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


## 09 importkafka.producer.ProducerConfig

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


## 10 importscala.util.Random

<table>
  <tr>
    <th>12</th>
    <th>objectKafkaEventProducer {</th>
  </tr>
</table>


<table>
  <tr>
    <th>13</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>14</th>
    <th>privatevalusers =Array(</th>
  </tr>
</table>


<table>
  <tr>
    <th>15</th>
    <th>"4A4D769EB9679C054DE81B973ED5D 768", "8dfeb5aaafc027d89349ac9a20b39 30f",</th>
  </tr>
</table>


<table>
  <tr>
    <th>16</th>
    <th>"011BBF43B89BFBF266C865DF0397A A71", "f2a8474bf7bd94f0aabbd4cdd2c06 dcf",</th>
  </tr>
</table>


<table>
  <tr>
    <th>17</th>
    <th>"068b746ed4620d25e26055a9f8043 85f", "97edfc08311c70143401745a03a50 706",</th>
  </tr>
</table>


<table>
  <tr>
    <th>18</th>
    <th>"d7f141563005d1b5d0d3dd30138f3 f62", "c8ee90aade1671a21336c721512b8 17a",</th>
  </tr>
</table>


<table>
  <tr>
    <th>19</th>
    <th>"6b67c8c700427dee7552f81f3228c 927", "a95f22eabc4fd4b580c011a3161a9 d9d")</th>
  </tr>
</table>


<table>
  <tr>
    <th>20</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>21</th>
    <th>privatevalrandom =newRandom()</th>
  </tr>
</table>


<table>
  <tr>
    <th>22</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>23</th>
    <th>privatevarpointer =-1</th>
  </tr>
</table>


<table>
  <tr>
    <th>24</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>25</th>
    <th>defgetUserID() :String ={</th>
  </tr>
</table>


<table>
  <tr>
    <th>26</th>
    <th>pointer =pointer + 1</th>
  </tr>
</table>


<table>
  <tr>
    <th>27</th>
    <th>if(pointer >=users.length) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>28</th>
    <th>pointer =0</th>
  </tr>
</table>


<table>
  <tr>
    <th>29</th>
    <th>users(pointer)</th>
  </tr>
</table>


<table>
  <tr>
    <th>30</th>
    <th>} else{</th>
  </tr>
</table>


<table>
  <tr>
    <th>31</th>
    <th>users(pointer)</th>
  </tr>
</table>


<table>
  <tr>
    <th>32</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>33</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>34</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>35</th>
    <th>defclick() :Double ={</th>
  </tr>
</table>


<table>
  <tr>
    <th>36</th>
    <th>random.nextInt(10)</th>
  </tr>
</table>


<table>
  <tr>
    <th>37</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>38</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>39</th>
    <th>// bin/kafka-topics.sh --zookeeper zk1:2181,zk2:2181,zk3:2181/kafka -create --topic user_events -replication-factor 2 --partitions 2</th>
  </tr>
</table>


<table>
  <tr>
    <th>40</th>
    <th>// bin/kafka-topics.sh --zookeeper zk1:2181,zk2:2181,zk3:2181/kafka -list</th>
  </tr>
</table>


<table>
  <tr>
    <th>41</th>
    <th>// bin/kafka-topics.sh --zookeeper zk1:2181,zk2:2181,zk3:2181/kafka -describe user_events</th>
  </tr>
</table>


<table>
  <tr>
    <th>42</th>
    <th>// bin/kafka-console-consumer.sh -<br><br>-zookeeper zk1:2181,zk2:2181,zk3:22181/kafka -topic test_json_basis_event --frombeginning</th>
  </tr>
</table>


<table>
  <tr>
    <th>43</th>
    <th>defmain(args:Array[String]):Unit = {</th>
  </tr>
</table>


<table>
  <tr>
    <th>44</th>
    <th>valtopic ="user_events"</th>
  </tr>
</table>


<table>
  <tr>
    <th>45</th>
    <th>valbrokers ="10.10.4.126:9092,10<br><br>.10.4.127:9092"</th>
  </tr>
</table>


<table>
  <tr>
    <th>46</th>
    <th>valprops =newProperties()</th>
  </tr>
</table>


<table>
  <tr>
    <th>47</th>
    <th>props.put("metadata.broker.list" , brokers)</th>
  </tr>
</table>


<table>
  <tr>
    <th>48</th>
    <th>props.put("serializer.class", "k afka.serializer.StringEncoder")</th>
  </tr>
</table>


<table>
  <tr>
    <th>49</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>50</th>
    <th>valkafkaConfig =newProducerConfi g(props)</th>
  </tr>
</table>


<table>
  <tr>
    <th>51</th>
    <th>valproducer =newProducer[String, String](kafkaConfig)</th>
  </tr>
</table>


<table>
  <tr>
    <th>52</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>53</th>
    <th>while(true) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>54</th>
    <th>// prepare event data</th>
  </tr>
</table>


<table>
  <tr>
    <th>55</th>
    <th>valevent =newJSONObject()</th>
  </tr>
</table>


<table>
  <tr>
    <th>56</th>
    <th>event</th>
  </tr>
</table>


<table>
  <tr>
    <th>57</th>
    <th>.put("uid", getUserID)</th>
  </tr>
</table>


<table>
  <tr>
    <th>58</th>
    <th>.put("event_time", System.currentTimeMillis.toString)</th>
  </tr>
</table>


<table>
  <tr>
    <th>59</th>
    <th>.put("os_type", "Android")</th>
  </tr>
</table>


<table>
  <tr>
    <th>60</th>
    <th>.put("click_count", click)</th>
  </tr>
</table>


<table>
  <tr>
    <th>61</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>62</th>
    <th>// produce event message</th>
  </tr>
</table>


<table>
  <tr>
    <th>63</th>
    <th>producer.send(newKeyedMessage[ String, String](topic, event.toString))</th>
  </tr>
</table>


<table>
  <tr>
    <th>64</th>
    <th>println("Message sent: "+ event)</th>
  </tr>
</table>


<table>
  <tr>
    <th>65</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>66</th>
    <th>Thread.sleep(200)</th>
  </tr>
</table>


<table>
  <tr>
    <th>67</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>68</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>69</th>
    <th>}</th>
  </tr>
</table>


### 通过控制上⾯程序最后⼀⾏的时间间隔来控制模拟写⼊速度。下⾯我们来讨论实现实时统计每个⽤户的 点击次数，它是按照⽤户分组进⾏累加次数，逻辑⽐较简单，关键是在实现过程中要注意⼀些问题，如对象 序列化等。先看实现代码，稍后我们再详细讨论，代码实现如下所示：

<table>
  <tr>
    <th>01</th>
    <th>objectUserClickCountAnalytics {</th>
  </tr>
</table>


<table>
  <tr>
    <th>02</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>03</th>
    <th>defmain(args:Array[String]):Unit = {</th>
  </tr>
</table>


<table>
  <tr>
    <th>04</th>
    <th>varmasterUrl ="local[1]"</th>
  </tr>
</table>


<table>
  <tr>
    <th>05</th>
    <th>if(args.length > 0) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>06</th>
    <th>masterUrl =args(0)</th>
  </tr>
</table>


<table>
  <tr>
    <th>07</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>08</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>09</th>
    <th>// Create a StreamingContext with the given master URL</th>
  </tr>
</table>


<table>
  <tr>
    <th>10</th>
    <th>valconf =newSparkConf().setMaste r(masterUrl).setAppName("UserClickCo untStat")</th>
  </tr>
</table>


<table>
  <tr>
    <th>11</th>
    <th>valssc =newStreamingContext(conf , Seconds(5))</th>
  </tr>
</table>


<table>
  <tr>
    <th>12</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>13</th>
    <th>// Kafka configurations</th>
  </tr>
</table>


<table>
  <tr>
    <th>14</th>
    <th>valtopics =Set("user_events")</th>
  </tr>
</table>


<table>
  <tr>
    <th>15</th>
    <th>valbrokers ="10.10.4.126:9092,10<br><br>.10.4.127:9092"</th>
  </tr>
</table>


<table>
  <tr>
    <th>16</th>
    <th>valkafkaParams =Map[String, String](</th>
  </tr>
</table>


<table>
  <tr>
    <th>17</th>
    <th>"metadata.broker.list"-> brokers, "serializer.class"> "kafka.serializer.StringEncoder")</th>
  </tr>
</table>


<table>
  <tr>
    <th>18</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>19</th>
    <th>valdbIndex =1</th>
  </tr>
</table>


<table>
  <tr>
    <th>20</th>
    <th>valclickHashKey ="app::users::cl ick"</th>
  </tr>
</table>


<table>
  <tr>
    <th>21</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>22</th>
    <th>// Create a direct stream</th>
  </tr>
</table>


<table>
  <tr>
    <th>23</th>
    <th>valkafkaStream =KafkaUtils.creat eDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)</th>
  </tr>
</table>


<table>
  <tr>
    <th>24</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>25</th>
    <th>valevents =kafkaStream.flatMap(l ine => {</th>
  </tr>
</table>


<table>
  <tr>
    <th>26</th>
    <th>valdata =JSONObject.fromObject (line._2)</th>
  </tr>
</table>


<table>
  <tr>
    <th>27</th>
    <th>Some(data)</th>
  </tr>
</table>


<table>
  <tr>
    <th>28</th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>29</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>30</th>
    <th>// Compute user click times</th>
  </tr>
</table>


<table>
  <tr>
    <th>31</th>
    <th>valuserClicks =events.map(x => (x.getString("uid"), x.getInt("click_count"))).reduceByKe y(_+ _)</th>
  </tr>
</table>


<table>
  <tr>
    <th>32</th>
    <th>userClicks.foreachRDD(rdd => {</th>
  </tr>
</table>


<table>
  <tr>
    <th>33</th>
    <th>rdd.foreachPartition(partition OfRecords => {</th>
  </tr>
</table>


<table>
  <tr>
    <th>34</th>
    <th>partitionOfRecords.foreach(p air => {</th>
  </tr>
</table>


<table>
  <tr>
    <th>35</th>
    <th>valuid =pair._1</th>
  </tr>
</table>


<table>
  <tr>
    <th>36</th>
    <th>valclickCount =pair._2</th>
  </tr>
</table>


<table>
  <tr>
    <th>37</th>
    <th>valjedis = <span class="wp_keywordlink_affiliat e"><a href="<br><br>"title=""target="_b lank"data-original-title="View all posts in Redis">Redis</a> </span>Client.pool.getResource<br><br>http://www.iteblog.com/a rchives/tag/redis</th>
  </tr>
</table>


<table>
  <tr>
    <th>38</th>
    <th>jedis.select(dbIndex)</th>
  </tr>
</table>


<table>
  <tr>
    <th>39</th>
    <th>jedis.hincrBy(clickHashKey , uid, clickCount)</th>
  </tr>
</table>


<table>
  <tr>
    <th>40</th>
    <th>RedisClient.pool.returnRes ource(jedis)</th>
  </tr>
</table>


<table>
  <tr>
    <th>41</th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>42</th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>43</th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>44</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>45</th>
    <th>ssc.start()</th>
  </tr>
</table>


<table>
  <tr>
    <th>46</th>
    <th>ssc.awaitTermination()</th>
  </tr>
</table>


<table>
  <tr>
    <th>47</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>48</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>49</th>
    <th>}</th>
  </tr>
</table>


### 上⾯代码使⽤了Jedis客户端来操作Redis，将分组计数结果数据累加写⼊Redis存储，如果其他系统需要 实时获取该数据，直接从Redis实时读取即可。RedisClient实现代码如下所示：

<table>
  <tr>
    <th>01</th>
    <th>objectRedisClient extendsSerializabl e {</th>
  </tr>
</table>


<table>
  <tr>
    <th>02</th>
    <th>valredisHost ="10.10.4.130"</th>
  </tr>
</table>


<table>
  <tr>
    <th>03</th>
    <th>valredisPort =6379</th>
  </tr>
</table>


<table>
  <tr>
    <th>04</th>
    <th>valredisTimeout =30000</th>
  </tr>
</table>


<table>
  <tr>
    <th>05</th>
    <th>lazyvalpool =newJedisPool(newGener icObjectPoolConfig(), redisHost, redisPort, redisTimeout)</th>
  </tr>
</table>


<table>
  <tr>
    <th>06</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>07</th>
    <th>lazyvalhook =newThread {</th>
  </tr>
</table>


<table>
  <tr>
    <th>08</th>
    <th>overridedefrun ={</th>
  </tr>
</table>


<table>
  <tr>
    <th>09</th>
    <th>println("Execute hook thread: "+ this)</th>
  </tr>
</table>


<table>
  <tr>
    <th>10</th>
    <th>pool.destroy()</th>
  </tr>
</table>


<table>
  <tr>
    <th>11</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>12</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>13</th>
    <th>sys.addShutdownHook(hook.run)</th>
  </tr>
</table>


<table>
  <tr>
    <th>14</th>
    <th>}</th>
  </tr>
</table>


上⾯代码我们分别在local[K]和Spark Standalone集群模式下运⾏通过。 如果我们是在开发环境进⾏调试的时候，也就是使⽤local[K]部署模式，在本地启动K个Worker线程来计 算，这K个Worker在同⼀个JVM实例⾥，上⾯的代码默认情况是，如果没有传参数则是local[K]模式，所以如 果使⽤这种⽅式在创建Redis连接池或连接的时候，可能⾮常容易调试通过，但是在使⽤Spark Standalone、 YARN Client（YARN Cluster）或Mesos集群部署模式的时候，就会报错，主要是由于在处理Redis连接池或 连接的时候出错了。我们可以看⼀下Spark架构，如图所示（来⾃官⽹）：

![image 1](<从 ClickHouse 到 ByteHouse：实时数据分析场景下的优化实践.note_images/imageFile1.png>)

⽆论是在本地模式、Standalone模式，还是在Mesos或YARN模式下，整个Spark集群的结构都可以⽤上

图抽象表示，只是各个组件的运⾏环境不同，导致组件可能是分布式的，或本地的，或单个JVM实例的。如 在本地模式，则上图表现为在同⼀节点上的单个进程之内的多个组件；⽽在YARN Client模式下，Driver程序 是在YARN集群之外的⼀个节点上提交Spark Aplication，其他的组件都运⾏在YARN集群管理的节点上。

在Spark集群环境部署Aplication后，在进⾏计算的时候会将作⽤于RD数据集上的函数（Functions） 发送到集群中Worker上的Executor上（在Spark Streaming中是作⽤于DStream的操作），那么这些函数操作 所作⽤的对象（Elements）必须是可序列化的，通过Scala也可以使⽤lazy引⽤来解决，否则这些对象 （Elements）在跨节点序列化传输后，⽆法正确地执⾏反序列化重构成实际可⽤的对象。上⾯代码我们使⽤ lazy引⽤（Lazy Reference）来实现的，代码如下所示：

<table>
  <tr>
    <th>01</th>
    <th>// lazy pool reference</th>
  </tr>
</table>


<table>
  <tr>
    <th>02</th>
    <th>lazyvalpool =newJedisPool(newGeneric ObjectPoolConfig(), redisHost, redisPort, redisTimeout)</th>
  </tr>
</table>


<table>
  <tr>
    <th>03</th>
    <th>...</th>
  </tr>
</table>


<table>
  <tr>
    <th>04</th>
    <th>partitionOfRecords.foreach(pair => {</th>
  </tr>
</table>


<table>
  <tr>
    <th>05</th>
    <th>valuid =pair._1</th>
  </tr>
</table>


<table>
  <tr>
    <th>06</th>
    <th>valclickCount =pair._2</th>
  </tr>
</table>


<table>
  <tr>
    <th>07</th>
    <th>valjedis =RedisClient.pool.getReso urce</th>
  </tr>
</table>


<table>
  <tr>
    <th>08</th>
    <th>jedis.select(dbIndex)</th>
  </tr>
</table>


<table>
  <tr>
    <th>09</th>
    <th>jedis.hincrBy(clickHashKey, uid, clickCount)</th>
  </tr>
</table>


<table>
  <tr>
    <th>10</th>
    <th>RedisClient.pool.returnResource(je dis)</th>
  </tr>
</table>


<table>
  <tr>
    <th>11</th>
    <th>})</th>
  </tr>
</table>


### 另⼀种⽅式，我们将代码修改为，把对Redis连接的管理放在操作DStream的Output操作范围之内，因为 我们知道它是在特定的Executor中进⾏初始化的，使⽤⼀个单例的对象来管理，如下所示：

<table>
  <tr>
    <th>001</th>
    <th>packageorg.shirdrn.spark.streaming</th>
  </tr>
</table>


<table>
  <tr>
    <th>002</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>003</th>
    <th>importorg.apache.commons.pool2.impl. GenericObjectPoolConfig</th>
  </tr>
</table>


<table>
  <tr>
    <th>004</th>
    <th>importorg.apache.spark.SparkConf</th>
  </tr>
</table>


<table>
  <tr>
    <th>005</th>
    <th>importorg.apache.spark.streaming.Sec onds</th>
  </tr>
</table>


<table>
  <tr>
    <th>006</th>
    <th>importorg.apache.spark.streaming.Str eamingContext</th>
  </tr>
</table>


<table>
  <tr>
    <th>007</th>
    <th>importorg.apache.spark.streaming.dst ream.DStream.toPairDStreamFunctions</th>
  </tr>
</table>


<table>
  <tr>
    <th>008</th>
    <th>importorg.apache.spark.streaming.kaf ka.KafkaUtils</th>
  </tr>
</table>


<table>
  <tr>
    <th>009</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>010</th>
    <th>importkafka.serializer.StringDecoder</th>
  </tr>
</table>


<table>
  <tr>
    <th>011</th>
    <th>importnet.sf.json.JSONObject</th>
  </tr>
</table>


<table>
  <tr>
    <th>012</th>
    <th>importredis.clients.jedis.JedisPool</th>
  </tr>
</table>


<table>
  <tr>
    <th>013</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>014</th>
    <th>objectUserClickCountAnalytics {</th>
  </tr>
</table>


<table>
  <tr>
    <th>015</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>016</th>
    <th>defmain(args:Array[String]):Unit = {</th>
  </tr>
</table>


<table>
  <tr>
    <th>017</th>
    <th>varmasterUrl ="local[1]"</th>
  </tr>
</table>


<table>
  <tr>
    <th>018</th>
    <th>if(args.length > 0) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>019</th>
    <th>masterUrl =args(0)</th>
  </tr>
</table>


<table>
  <tr>
    <th>020</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>021</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>022</th>
    <th>// Create a StreamingContext with the given master URL</th>
  </tr>
</table>


<table>
  <tr>
    <th>023</th>
    <th>valconf =newSparkConf().setMaste r(masterUrl).setAppName("UserClickCo untStat")</th>
  </tr>
</table>


<table>
  <tr>
    <th>024</th>
    <th>valssc =newStreamingContext(conf , Seconds(5))</th>
  </tr>
</table>


<table>
  <tr>
    <th>025</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>026</th>
    <th>// Kafka configurations</th>
  </tr>
</table>


<table>
  <tr>
    <th>027</th>
    <th>valtopics =Set("user_events")</th>
  </tr>
</table>


<table>
  <tr>
    <th>028</th>
    <th>valbrokers ="10.10.4.126:9092,10<br><br>.10.4.127:9092"</th>
  </tr>
</table>


<table>
  <tr>
    <th>029</th>
    <th>valkafkaParams =Map[String, String](</th>
  </tr>
</table>


<table>
  <tr>
    <th>030</th>
    <th>"metadata.broker.list"-> brokers, "serializer.class"> "kafka.serializer.StringEncoder")</th>
  </tr>
</table>


<table>
  <tr>
    <th>031</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>032</th>
    <th>valdbIndex =1</th>
  </tr>
</table>


<table>
  <tr>
    <th>033</th>
    <th>valclickHashKey ="app::users::cl ick"</th>
  </tr>
</table>


<table>
  <tr>
    <th>034</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>035</th>
    <th>// Create a direct stream</th>
  </tr>
</table>


<table>
  <tr>
    <th>036</th>
    <th>valkafkaStream =KafkaUtils.creat eDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)</th>
  </tr>
</table>


<table>
  <tr>
    <th>037</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>038</th>
    <th>valevents =kafkaStream.flatMap(l ine => {</th>
  </tr>
</table>


<table>
  <tr>
    <th>039</th>
    <th>valdata =JSONObject.fromObject (line._2)</th>
  </tr>
</table>


<table>
  <tr>
    <th>040</th>
    <th>Some(data)</th>
  </tr>
</table>


<table>
  <tr>
    <th>041</th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>042</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>043</th>
    <th>// Compute user click times</th>
  </tr>
</table>


<table>
  <tr>
    <th>044</th>
    <th>valuserClicks =events.map(x => (x.getString("uid"), x.getInt("click_count"))).reduceByKe y(_+ _)</th>
  </tr>
</table>


<table>
  <tr>
    <th>045</th>
    <th>userClicks.foreachRDD(rdd => {</th>
  </tr>
</table>


<table>
  <tr>
    <th>046</th>
    <th>rdd.foreachPartition(partition OfRecords => {</th>
  </tr>
</table>


<table>
  <tr>
    <th>047</th>
    <th>partitionOfRecords.foreach(p air => {</th>
  </tr>
</table>


<table>
  <tr>
    <th>048</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>049</th>
    <th>/**</th>
  </tr>
</table>


<table>
  <tr>
    <th>050</th>
    <th>* Internal Redis client for managing Redis connection {@link Jedis} based on {@link RedisPool}</th>
  </tr>
</table>


<table>
  <tr>
    <th>051</th>
    <th>*/</th>
  </tr>
</table>


<table>
  <tr>
    <th>052</th>
    <th>objectInternalRedisClient extendsSerializable {</th>
  </tr>
</table>


<table>
  <tr>
    <th>053</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>054</th>
    <th>@transient privatevarpoo l:JedisPool =null</th>
  </tr>
</table>


<table>
  <tr>
    <th>055</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>056</th>
    <th>defmakePool(redisHost:St ring, redisPort:Int, redisTimeout:Int,</th>
  </tr>
</table>


<table>
  <tr>
    <th>057</th>
    <th>maxTotal:Int, maxIdle:Int, minIdle:Int):Unit ={</th>
  </tr>
</table>


<table>
  <tr>
    <th>058</th>
    <th>makePool(redisHost, redisPort, redisTimeout, maxTotal, maxIdle, minIdle, true, false, 10000)</th>
  </tr>
</table>


<table>
  <tr>
    <th>059</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>060</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>061</th>
    <th>defmakePool(redisHost:St ring, redisPort:Int, redisTimeout:Int,</th>
  </tr>
</table>


<table>
  <tr>
    <th>062</th>
    <th>maxTotal:Int, maxIdle:Int, minIdle:Int, testOnBorrow:Boolean,</th>
  </tr>
</table>


<table>
  <tr>
    <th>063</th>
    <th>testOnReturn:Boolean , maxWaitMillis:Long):Unit ={</th>
  </tr>
</table>


<table>
  <tr>
    <th>064</th>
    <th>if(pool ==null) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>065</th>
    <th>valpoolConfig =ne wGenericObjectPoolConfig()</th>
  </tr>
</table>


<table>
  <tr>
    <th>066</th>
    <th>poolConfig.setMax Total(maxTotal)</th>
  </tr>
</table>


<table>
  <tr>
    <th>067</th>
    <th>poolConfig.setMax Idle(maxIdle)</th>
  </tr>
</table>


<table>
  <tr>
    <th>068</th>
    <th>poolConfig.setMin Idle(minIdle)</th>
  </tr>
</table>


<table>
  <tr>
    <th>069</th>
    <th>poolConfig.setTes tOnBorrow(testOnBorrow)</th>
  </tr>
</table>


<table>
  <tr>
    <th>070</th>
    <th>poolConfig.setTes tOnReturn(testOnReturn)</th>
  </tr>
</table>


<table>
  <tr>
    <th>071</th>
    <th>poolConfig.setMax WaitMillis(maxWaitMillis)</th>
  </tr>
</table>


<table>
  <tr>
    <th>072</th>
    <th>pool =newJedisPoo l(poolConfig, redisHost, redisPort, redisTimeout)</th>
  </tr>
</table>


<table>
  <tr>
    <th>073</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>074</th>
    <th>valhook =newThrea d{</th>
  </tr>
</table>


<table>
  <tr>
    <th>075</th>
    <th>overridedefr un =pool.destroy()</th>
  </tr>
</table>


<table>
  <tr>
    <th>076</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>077</th>
    <th>sys.addShutdownHo ok(hook.run)</th>
  </tr>
</table>


<table>
  <tr>
    <th>078</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>079</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>080</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>081</th>
    <th>defgetPool:JedisPool ={</th>
  </tr>
</table>


<table>
  <tr>
    <th>082</th>
    <th>assert(pool !=null)</th>
  </tr>
</table>


<table>
  <tr>
    <th>083</th>
    <th>pool</th>
  </tr>
</table>


<table>
  <tr>
    <th>084</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>085</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>086</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>087</th>
    <th>// Redis configurations</th>
  </tr>
</table>


<table>
  <tr>
    <th>088</th>
    <th>valmaxTotal =10</th>
  </tr>
</table>


<table>
  <tr>
    <th>089</th>
    <th>valmaxIdle =10</th>
  </tr>
</table>


<table>
  <tr>
    <th>090</th>
    <th>valminIdle =1</th>
  </tr>
</table>


<table>
  <tr>
    <th>091</th>
    <th>valredisHost ="10.10.4.130 "</th>
  </tr>
</table>


<table>
  <tr>
    <th>092</th>
    <th>valredisPort =6379</th>
  </tr>
</table>


<table>
  <tr>
    <th>093</th>
    <th>valredisTimeout =30000</th>
  </tr>
</table>


<table>
  <tr>
    <th>094</th>
    <th>valdbIndex =1</th>
  </tr>
</table>


<table>
  <tr>
    <th>095</th>
    <th>InternalRedisClient.makePo ol(redisHost, redisPort, redisTimeout, maxTotal, maxIdle, minIdle)</th>
  </tr>
</table>


<table>
  <tr>
    <th>096</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>097</th>
    <th>valuid =pair._1</th>
  </tr>
</table>


<table>
  <tr>
    <th>098</th>
    <th>valclickCount =pair._2</th>
  </tr>
</table>


<table>
  <tr>
    <th>099</th>
    <th>valjedis =InternalRedisCli ent.getPool.getResource</th>
  </tr>
</table>


<table>
  <tr>
    <th>100</th>
    <th>jedis.select(dbIndex)</th>
  </tr>
</table>


<table>
  <tr>
    <th>101</th>
    <th>jedis.hincrBy(clickHashKey , uid, clickCount)</th>
  </tr>
</table>


<table>
  <tr>
    <th>102</th>
    <th>InternalRedisClient.getPoo l.returnResource(jedis)</th>
  </tr>
</table>


<table>
  <tr>
    <th>103</th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>104</th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>105</th>
    <th>})</th>
  </tr>
</table>


<table>
  <tr>
    <th>106</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>107</th>
    <th>ssc.start()</th>
  </tr>
</table>


<table>
  <tr>
    <th>108</th>
    <th>ssc.awaitTermination()</th>
  </tr>
</table>


<table>
  <tr>
    <th>109</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>110</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>111</th>
    <th>}</th>
  </tr>
</table>


上⾯代码实现，得益于Scala语⾔的特性，可以在代码中任何位置进⾏clas或object的定义，我们将⽤来 管理Redis连接的代码放在了特定操作的内部，就避免了瞬态（Transient）对象跨节点序列化的问题。这样做 还要求我们能够了解Spark内部是如何操作RD数据集的，更多可以参考RD或Spark相关⽂档。

在集群上，以Standalone模式运⾏，执⾏如下命令：

<table>
  <tr>
    <th>1</th>
    <th>cd/usr/local/spark</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>./bin/spark-submit --class org.shirdrn.spark.streaming.UserClic kCountAnalytics</th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>-master spark://hadoop1:7077</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>--executor-memory 1G</th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>--total-executorcores 2</th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th>~/spark0.0.SNAPSHOT.jar spark://hadoop1:707 7</th>
  </tr>
</table>


### 可以查看集群中各个Worker节点执⾏计算任务的状态，也可以⾮常⽅便地通过Web⻚⾯查看。 下⾯，看⼀下我们存储到Redis中的计算结果，如下所示：

<table>
  <tr>
    <th>01</th>
    <th>127.0.0.1:6379[1]> HGETALL app::users::click</th>
  </tr>
</table>


<table>
  <tr>
    <th>02</th>
    <th>1) "4A4D769EB9679C054DE81B973ED5D768 "</th>
  </tr>
</table>


<table>
  <tr>
    <th>03</th>
    <th>2) "7037"</th>
  </tr>
</table>


<table>
  <tr>
    <th>04</th>
    <th>3) "8dfeb5aaafc027d89349ac9a20b3930f "</th>
  </tr>
</table>


<table>
  <tr>
    <th>05</th>
    <th>4) "6992"</th>
  </tr>
</table>


<table>
  <tr>
    <th>06</th>
    <th>5) "011BBF43B89BFBF266C865DF0397AA71 "</th>
  </tr>
</table>


<table>
  <tr>
    <th>07</th>
    <th>6) "7021"</th>
  </tr>
</table>


<table>
  <tr>
    <th>08</th>
    <th>7) "97edfc08311c70143401745a03a50706 "</th>
  </tr>
</table>


<table>
  <tr>
    <th>09</th>
    <th>8) "6874"</th>
  </tr>
</table>


<table>
  <tr>
    <th>10</th>
    <th>9) "d7f141563005d1b5d0d3dd30138f3f62 "</th>
  </tr>
</table>


<table>
  <tr>
    <th>11</th>
    <th>10) "7057"</th>
  </tr>
</table>


<table>
  <tr>
    <th>12</th>
    <th>11) "a95f22eabc4fd4b580c011a3161a9d9 d"</th>
  </tr>
</table>


<table>
  <tr>
    <th>13</th>
    <th>12) "7092"</th>
  </tr>
</table>


<table>
  <tr>
    <th>14</th>
    <th>13) "6b67c8c700427dee7552f81f3228c92 7"</th>
  </tr>
</table>


<table>
  <tr>
    <th>15</th>
    <th>14) "7266"</th>
  </tr>
</table>


<table>
  <tr>
    <th>16</th>
    <th>15) "f2a8474bf7bd94f0aabbd4cdd2c06dc f"</th>
  </tr>
</table>


<table>
  <tr>
    <th>17</th>
    <th>16) "7188"</th>
  </tr>
</table>


<table>
  <tr>
    <th>18</th>
    <th>17) "c8ee90aade1671a21336c721512b817 a"</th>
  </tr>
</table>


<table>
  <tr>
    <th>19</th>
    <th>18) "6950"</th>
  </tr>
</table>


<table>
  <tr>
    <th>20</th>
    <th>19) "068b746ed4620d25e26055a9f804385 f"</th>
  </tr>
</table>


# pom⽂件及相关依赖

这⾥，附上前⾯开发的应⽤所对应的依赖，以及打包Spark Streaming应⽤程序的Maven配置，以供参 考。如果使⽤maven-shade-plugin插件，配置有问题的话，打包后在Spark集群上提交Aplication时候可能 会报错Invalid signature file digest for Manifest main atributes。参考的Maven配置，如下所示：

<table>
  <tr>
    <th>001</th>
    <th><project xmlns=" "xmlns:xsi="<br><br>"<br><br>http://maven.apache. org/POM/4.0.0 http://www. w3.org/2001/XMLSchema-instance</th>
  </tr>
</table>


<table>
  <tr>
    <th>002</th>
    <th>xsi:schemaLocation="<br><br>"><br><br>http://mave n.apache.org/POM/4.0.0 http://maven. apache.org/xsd/maven-4.0.0.xsd</th>
  </tr>
</table>


<table>
  <tr>
    <th>003</th>
    <th><modelVersion>4.0.0</modelVersi on></th>
  </tr>
</table>


<table>
  <tr>
    <th>004</th>
    <th><groupId>org.shirdrn.spark</gro upId></th>
  </tr>
</table>


<table>
  <tr>
    <th>005</th>
    <th><artifactId>spark</artifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>006</th>
    <th><version>0.0.1SNAPSHOT</version></th>
  </tr>
</table>


<table>
  <tr>
    <th>007</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>008</th>
    <th><dependencies></th>
  </tr>
</table>


<table>
  <tr>
    <th>009</th>
    <th><dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>010</th>
    <th><groupId>org.apache.s park</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>011</th>
    <th><artifactId>sparkcore_2.10</artifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>012</th>
    <th><version>1.3.0</versi on></th>
  </tr>
</table>


<table>
  <tr>
    <th>013</th>
    <th></dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>014</th>
    <th><dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>015</th>
    <th><groupId>org.apache.s park</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>016</th>
    <th><artifactId>sparkstreaming_2.10</artifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>017</th>
    <th><version>1.3.0</versi on></th>
  </tr>
</table>


<table>
  <tr>
    <th>018</th>
    <th></dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>019</th>
    <th><dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>020</th>
    <th><groupId>net.sf.jsonlib</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>021</th>
    <th><artifactId>jsonlib</artifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>022</th>
    <th><version>2.3</version ></th>
  </tr>
</table>


<table>
  <tr>
    <th>023</th>
    <th></dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>024</th>
    <th><dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>025</th>
    <th><groupId>org.apache.s park</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>026</th>
    <th><artifactId>sparkstreaming-kafka_2.10</artifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>027</th>
    <th><version>1.3.0</versi on></th>
  </tr>
</table>


<table>
  <tr>
    <th>028</th>
    <th></dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>029</th>
    <th><dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>030</th>
    <th><groupId>redis.client s</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>031</th>
    <th><artifactId>jedis</ar tifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>032</th>
    <th><version>2.5.2</versi on></th>
  </tr>
</table>


<table>
  <tr>
    <th>033</th>
    <th></dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>034</th>
    <th><dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>035</th>
    <th><groupId>org.apache.c ommons</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>036</th>
    <th><artifactId>commonspool2</artifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>037</th>
    <th><version>2.2</version ></th>
  </tr>
</table>


<table>
  <tr>
    <th>038</th>
    <th></dependency></th>
  </tr>
</table>


<table>
  <tr>
    <th>039</th>
    <th></dependencies></th>
  </tr>
</table>


<table>
  <tr>
    <th>040</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>041</th>
    <th><build></th>
  </tr>
</table>


<table>
  <tr>
    <th>042</th>
    <th><sourceDirectory>${basedir }/src/main/scala</sourceDirectory></th>
  </tr>
</table>


<table>
  <tr>
    <th>043</th>
    <th><testSourceDirectory>${bas edir}/src/test/scala</testSourceDire ctory></th>
  </tr>
</table>


<table>
  <tr>
    <th>044</th>
    <th><resources></th>
  </tr>
</table>


<table>
  <tr>
    <th>045</th>
    <th><resource></th>
  </tr>
</table>


<table>
  <tr>
    <th>046</th>
    <th><directory>${bas edir}/src/main/resources</directory></th>
  </tr>
</table>


<table>
  <tr>
    <th>047</th>
    <th></resource></th>
  </tr>
</table>


<table>
  <tr>
    <th>048</th>
    <th></resources></th>
  </tr>
</table>


<table>
  <tr>
    <th>049</th>
    <th><testResources></th>
  </tr>
</table>


<table>
  <tr>
    <th>050</th>
    <th><testResource></th>
  </tr>
</table>


<table>
  <tr>
    <th>051</th>
    <th><directory>${bas edir}/src/test/resources</directory></th>
  </tr>
</table>


<table>
  <tr>
    <th>052</th>
    <th></testResource></th>
  </tr>
</table>


<table>
  <tr>
    <th>053</th>
    <th></testResources></th>
  </tr>
</table>


<table>
  <tr>
    <th>054</th>
    <th><plugins></th>
  </tr>
</table>


<table>
  <tr>
    <th>055</th>
    <th><plugin></th>
  </tr>
</table>


<table>
  <tr>
    <th>056</th>
    <th><artifactId>mave n-compiler-plugin</artifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>057</th>
    <th><version>3.1</ve rsion></th>
  </tr>
</table>


<table>
  <tr>
    <th>058</th>
    <th><configuration></th>
  </tr>
</table>


<table>
  <tr>
    <th>059</th>
    <th><source>1.6 </source></th>
  </tr>
</table>


<table>
  <tr>
    <th>060</th>
    <th><target>1.6 </target></th>
  </tr>
</table>


<table>
  <tr>
    <th>061</th>
    <th></configuration></th>
  </tr>
</table>


<table>
  <tr>
    <th>062</th>
    <th></plugin></th>
  </tr>
</table>


<table>
  <tr>
    <th>063</th>
    <th><plugin></th>
  </tr>
</table>


<table>
  <tr>
    <th>064</th>
    <th><groupId>org.apa che.maven.plugins</groupId></th>
  </tr>
</table>


<table>
  <tr>
    <th>065</th>
    <th><artifactId>mave n-shade-plugin</artifactId></th>
  </tr>
</table>


<table>
  <tr>
    <th>066</th>
    <th><version>2.2</ve rsion></th>
  </tr>
</table>


<table>
  <tr>
    <th>067</th>
    <th><configuration></th>
  </tr>
</table>


<table>
  <tr>
    <th>068</th>
    <th><createDepe ndencyReducedPom>true</createDepende ncyReducedPom></th>
  </tr>
</table>


<table>
  <tr>
    <th>069</th>
    <th></configuration></th>
  </tr>
</table>


<table>
  <tr>
    <th>070</th>
    <th><executions></th>
  </tr>
</table>


<table>
  <tr>
    <th>071</th>
    <th><execution></th>
  </tr>
</table>


<table>
  <tr>
    <th>072</th>
    <th><phase >package</phase></th>
  </tr>
</table>


<table>
  <tr>
    <th>073</th>
    <th><goals ></th>
  </tr>
</table>


<table>
  <tr>
    <th>074</th>
    <th>< goal>shade</goal></th>
  </tr>
</table>


<table>
  <tr>
    <th>075</th>
    <th></goal s></th>
  </tr>
</table>


<table>
  <tr>
    <th>076</th>
    <th><confi guration></th>
  </tr>
</table>


<table>
  <tr>
    <th>077</th>
    <th>< artifactSet></th>
  </tr>
</table>


<table>
  <tr>
    <th>078</th>
    <th><includes></th>
  </tr>
</table>


<table>
  <tr>
    <th>079</th>
    <th><include>*:*</include></th>
  </tr>
</table>


<table>
  <tr>
    <th>080</th>
    <th></includes></th>
  </tr>
</table>


<table>
  <tr>
    <th>081</th>
    <th>< /artifactSet></th>
  </tr>
</table>


<table>
  <tr>
    <th>082</th>
    <th>< filters></th>
  </tr>
</table>


<table>
  <tr>
    <th>083</th>
    <th><filter></th>
  </tr>
</table>


<table>
  <tr>
    <th>084</th>
    <th><artifact>*:*</artifact></th>
  </tr>
</table>


<table>
  <tr>
    <th>085</th>
    <th><excludes></th>
  </tr>
</table>


<table>
  <tr>
    <th>086</th>
    <th><exclude>METAINF/*.SF</exclude></th>
  </tr>
</table>


<table>
  <tr>
    <th>087</th>
    <th><exclude>METAINF/*.DSA</exclude></th>
  </tr>
</table>


<table>
  <tr>
    <th>088</th>
    <th><exclude>METAINF/*.RSA</exclude></th>
  </tr>
</table>


<table>
  <tr>
    <th>089</th>
    <th></excludes></th>
  </tr>
</table>


<table>
  <tr>
    <th>090</th>
    <th></filter></th>
  </tr>
</table>


<table>
  <tr>
    <th>091</th>
    <th>< /filters></th>
  </tr>
</table>


<table>
  <tr>
    <th>092</th>
    <th>< transformers></th>
  </tr>
</table>


<table>
  <tr>
    <th>093</th>
    <th><transformer</th>
  </tr>
</table>


<table>
  <tr>
    <th>094</th>
    <th>implementation="org.apache. maven.plugins.shade.resource.Service sResourceTransformer"/></th>
  </tr>
</table>


<table>
  <tr>
    <th>095</th>
    <th><transformer</th>
  </tr>
</table>


<table>
  <tr>
    <th>096</th>
    <th>implementation="org.apache. maven.plugins.shade.resource.Appendi ngTransformer"></th>
  </tr>
</table>


<table>
  <tr>
    <th>097</th>
    <th><resource>reference.conf</r esource></th>
  </tr>
</table>


<table>
  <tr>
    <th>098</th>
    <th></transformer></th>
  </tr>
</table>


<table>
  <tr>
    <th>099</th>
    <th><transformer</th>
  </tr>
</table>


<table>
  <tr>
    <th>100</th>
    <th>implementation="org.apache. maven.plugins.shade.resource.DontInc ludeResourceTransformer"></th>
  </tr>
</table>


<table>
  <tr>
    <th>101</th>
    <th><resource>log4j.properties< /resource></th>
  </tr>
</table>


<table>
  <tr>
    <th>102</th>
    <th></transformer></th>
  </tr>
</table>


<table>
  <tr>
    <th>103</th>
    <th>< /transformers></th>
  </tr>
</table>


<table>
  <tr>
    <th>104</th>
    <th></conf iguration></th>
  </tr>
</table>


<table>
  <tr>
    <th>105</th>
    <th></execution ></th>
  </tr>
</table>


<table>
  <tr>
    <th>106</th>
    <th></executions></th>
  </tr>
</table>


<table>
  <tr>
    <th>107</th>
    <th></plugin></th>
  </tr>
</table>


<table>
  <tr>
    <th>108</th>
    <th></plugins></th>
  </tr>
</table>


<table>
  <tr>
    <th>109</th>
    <th></build></th>
  </tr>
</table>


<table>
  <tr>
    <th>110</th>
    <th></project></th>
  </tr>
</table>


