htps:/cloud.tencent.com/developer/article/147341

此次遇到这个问题的原因是缺少fastjson包，引⼊解决。

# 前⾔

⽤过rocketmq的⼈，采⽤客户端调⽤的时候，可能会相对⾼频的出现的 No route info of this topic这个 异常问题，然后你可能会拿着这个问题，直接⾕歌百度⼀把，会发现⽹上告诉你的答案，可能会有如 下⼏种

- 1、配置autoCreateTopicEnable=true，如果这个属性没有配置，且你没有⼿动创建topic，就会出现上 ⾯的异常 注：这个属性在⾼版本已经默认配置了
- 2、fastjson版本太低
- 3、防⽕墙问题


... 最后你可能试了他们提供的⽅法，发现，坑爹呢，⼀点⽤都没有，后边⾕歌百度⽆⻔了，你只好去官 ⽹溜达溜达，看能不能捡到宝，接着你在官⽹的FAQ你会找到如下相关的问题以及答案回复 Producer complains “No Topic Route Info”, how to diagnose?

Frequently Asked Questions The following questions are frequently asked with regard to the RocketMQ project in general.

General

- 1. Why did we create rocketmq project instead of selecting other products? Please refer to Why RocketMQ

- 2. Do I have to install other softewares, such as zookeeper, to use RocketMQ? No. RocketMQ can run independently.


Usage

- 1. Where does the newly created Consumer ID start consuming messages? If the topic sends a message within three days, then the consumer start consuming messages from the first message saved in the server. If the topic sends a message three days ago, the consumer starts to consume messages from the latest message in the server, in other words, starting from the tail of message queue. If such consumer is rebooted, then it starts to consume messages from the last consumption location.

- 2. How to reconsume message when consumption fails? Cluster consumption pattern The consumer business logic code returns Action.ReconsumerLater, NULL, or throws an exception, if a message failed to be consumed, it will retry for up to 16 times, after that, the message would be descarded.

Broadcast consumption pattern The broadcaset consumption still ensures that a message is consumered at least once, but no resend option is provided.

- 3. How to query the failed message if there is a consumption failure? Using topic query by time, you can query messages within a period of time. Using Topic and Message Id to accurately query the message. Using Topic and Message Key accurately query a class of messages with the same Message Key.

- 4. Are messages delivered exactly once? RocketMQ ensures that all messages are delivered at least once. In most cases, the messages are not repeated.

- 5. How to add a new broker? Start up a new broker and register it to the same list of name servers. By default, only internal system topics and consumer groups are created automatically. If you would like to have your business topic and consumer groups on the new node, please replicate them from


the existing broker. Admin tool and command lines are provided to handle this. Configuration related The following answers are all default values and can be modified by configuration.

- 1. How long are the messages saved on the server? Stored messages are will be saved for up to 3 days, and messages that are not consumed for more than 3 days will be deleted.

- 2. What is the size limit for message Body? Generally 256KB.

- 3. How to set the number of consumer threads? When you start Consumer, set a ConsumeThreadNums property, example is as follows:


consumer.setConsumeThreadMin(20); consumer.setConsumeThreadMax(20); Errors

- 1. If you start a producer or consumer failed and the error message is producer group or consumer repeat? Reason：Using the same Producer /Consumer Group to launch multiple instances of Producer/Consumer in the same JVM may cause the client fail to start.

Solution: Make sure that a JVM corresponding to one Producer /Consumer Group starts only with one Producer/Consumer instance.

- 2. If consumer failed to start loading json file in broadcast mode? Reason: Fastjson version is too low to allow the broadcast consumer to load local offsets.json, causing the consumer boot failure. Damaged fastjson file can also cause the same problem.

Solution: Fastjson version has to be upgraded to rocketmq client dependent version to ensure that the local offsets.json can be loaded. By default offsets.json file is in /home/{user}/.rocketmq_offsets. Or check the integrity of fastjson.

- 3. What is the impact of a broker crash? Master crashes Messages can no longer be sent to this broker set, but if you have another broker set available, messages can still be sent given the topic is present. Messages can still be consumed from slaves.


Some slave crash As long as there is another working slave, there will be no impact on sending messages. There will also be no impact on consuming messages except when the consumer group is set to consume from this slave preferably. By default, comsumer group consumes from master.

All slaves crash There will be no impact on sending messages to master, but, if the master is SYNC_MASTER, producer will get a SLAVE_NOT_AVAILABLE indicating that the message is not sent to any slaves. There will also be no impact on consuming messages except that if the consumer group is set to consume from slave preferably. By default, comsumer group consumes from master.

- 4. Producer complains “No Topic Route Info”, how to diagnose? This happens when you are trying to send messages to a topic whose routing info is not available to the producer.


Make sure that the producer can connect to a name server and is capable of fetching routing meta info from it. Make sure that name servers do contain routing meta info of the topic. You may query the routing meta info from name server through topicRoute using admin tools or web console. Make sure that your brokers are sending heartbeats to the same list of name servers your producer is connecting to. Make sure that the topic’s permssion is 6(rw-), or at least 2(-w-). If you can’t find this topic, create it on a broker via admin tools command updateTopic or web console. 嗯，很好，问题和咱们的差不多⼀样，还有提供答案，肯定能解决，好的，⻢上⾏动，结果⼜坑爹 了，依然没有效果 被坑之后，开始静下⼼来重新思考问题，下边是具体的解决⽅法

- 1、⽇志⼤法


- 1、查看broker⽇志


- a、关注broker是否有注册到nameserver register broker to name server localhost:9876 OK

- b、关注⽣产者是否连接到broker new producer connected, group: msgProduce channel: ClientChannelInfo [channel=[id: 0x13c55e87, L:/127.0.0.1:10911 - R:/127.0.0.1:59916], clientId=127.0.0.1@Producer-135915d0c39-dbdb-46a4b75c-efe368b718fe, language=JAVA, version=252, lastUpdateTimestamp=1548498274245]

- c、查看已经创建的topic是否包含⾃⼰想要的topic


- 2019-01-26 17:03:31 INFO main - load exist local topic, TopicConfig [topicName=messageTopic, readQueueNums=8, writeQueueNums=8, perm=RW-, topicFilterType=SINGLE_TAG, topicSysFlag=0, order=false]

- 2019-01-26 17:03:31 INFO main - load exist local topic, TopicConfig [topicName=interTopic, readQueueNums=8, writeQueueNums=8, perm=RW-, topicFilterType=SINGLE_TAG, topicSysFlag=0, order=false]

- 2019-01-26 17:03:31 INFO main - load exist local topic, TopicConfig [topicName=TopicTest, readQueueNums=4, writeQueueNums=4, perm=RW-, topicFilterType=SINGLE_TAG, topicSysFlag=0, order=false]

- 2019-01-26 17:03:31 INFO main - load exist local topic, TopicConfig [topicName=broker-a, readQueueNums=1, writeQueueNums=1, perm=RWX, topicFilterType=SINGLE_TAG, topicSysFlag=0, order=false]

d、查看消费者是否连接到broker new consumer connected, group: msgConsumer CONSUME_PASSIVELY CLUSTERING channel: ClientChannelInfo [channel=[id: 0x4d6d00e6, L:/127.0.0.1:10911 - R:/127.0.0.1:59928], clientId=127.0.0.1@8b8ac95a-9514-43b0-abfa-6b1dd9923b5e, language=JAVA, version=252, lastUpdateTimestamp=1548498275493] 2、查看nameserver⽇志 和broker⼤同⼩异 ⽐如查看topic

- 2019-01-26 17:03:32 INFO RemotingExecutorThread_1 - new topic registered, messageTopic QueueData [brokerName=broker-a, readQueueNums=8, writeQueueNums=8, perm=6, topicSynFlag=0]


- 2019-01-26 17:03:32 INFO RemotingExecutorThread_1 - new topic registered, interTopic QueueData [brokerName=broker-a, readQueueNums=8, writeQueueNums=8, perm=6, topicSynFlag=0]


- 2019-01-26 17:03:32 INFO RemotingExecutorThread_1 - new topic registered, TopicTest QueueData [brokerName=broker-a, readQueueNums=4, writeQueueNums=4, perm=6, topicSynFlag=0]


- 2019-01-26 17:03:32 INFO RemotingExecutorThread_1 - new topic registered, broker-a QueueData [brokerName=broker-a, readQueueNums=1, writeQueueNums=1, perm=7, topicSynFlag=0] 2019-01-26 17:03:32 INFO RemotingExecutorThread_1 - new topic registered, %RETRY%message10-35-51-82 QueueData [brokerName=broker-a, readQueueNums=1, writeQueueNums=1, perm=6, topicSynFlag=0] ⽐如关注broker是否注册到nameserver new broker registered, localhost:10911 HAServer: localhost:10912


- 3、创建topic


- a、在shell⾥⾯执⾏如下 sh mqadmin updateTopic -blocalhost:10911 -nlocalhost:9876 -tinterTopic


- 3、创建topic
- b、通过可视化web控制台rocketmq-console进⾏创建，其控制台下载地址如下 https://github.com/apache/rocketmq-externals


![image 1](<Rocketmq之No route info of this topic解决思路.note_images/imageFile1.png>)

当你发现nameserver，broker，topic都已经正常的情况下，却仍然报上⾯的异常，这种情况下，你可 以试下第⼆种⽅法

- 2、代码调试跟踪⼤法 对于上⾯的异常，你调试的断点可以设置在如下


- 1、org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl中的 TopicPublishInfo topicPublishInfo = this.tryToFindTopicPublishInfo(msg.getTopic());
- 2、org.apache.rocketmq.client.impl.factory.MQClientInstance中的 public TopicRouteData getTopicRouteInfoFromNameServer⽅法
- 3、项⽬组的出现那个异常的最后排查出来的原因 netty包冲突了 冲突的原因是项⽬采⽤的rocketmq的客户端是4.2版本，⽽项⽬组采⽤的springcloud，其eureka客户端 引⽤的netty包是4.0版本，这个问题其实也隐藏很深，因为业务项⽬组是没有⾃⼰引⼊netty包，除了 rocketmq客户端，这个是属于jar包依赖传递问题
- 4、最后解决⽅案 去除冲突的netty包，去除后成功消费
- 5、附录依赖传递的图


![image 2](<Rocketmq之No route info of this topic解决思路.note_images/imageFile2.png>)

# 总结

这个问题前前后后⼤概花了4左右天的时间，其中百度⾕歌花了3天半的时间，最后静下⼼排查，通过 ⽇志，源码跟踪，花了不到半天的时间解决。这次的排查给我的感受的⽹上的答案可能五花⼋⻔，本 来是为了节约时间，采⽤拿来主义的⽅法，结果适得其反。。。

原⽂发布于微信公众号 - Linyb极客之路（gh_c420b2cf6b47） 原⽂发表时间：2019-06-08 本⽂参与 ，欢迎正在阅读的你也加⼊，⼀起分享。 发表于 2019-06-18

腾讯云⾃媒体分享计划

