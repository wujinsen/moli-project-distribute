# 1.概述

前⾯给⼤家介绍了Kafka的背景以及⼀些应⽤场景，并附带上演示了Kafka的简单示例。然后，在 开发的过程当中，我们会发现⼀些问题，那就是消息的监控情况。虽然，在启动Kafka的相关服务后， 我们⽣产消息和消费消息会在终端控制台显示这些记录信息，但是，这样始终不够友好，⽽且，在实 际开发中，我们不会有权限去⼀直观看终端控制台，那么今天就为⼤家来介绍Kafka的⼀个监控系统 ⸺KafkaOfsetMonitor。下⾯是今天所分享的⽬录内容：

KafkaOfsetMonitor简述

KafkaOfsetMonitor安装部署 KafkaOfsetMonitor运⾏预览

下⾯开始今天的内容分享。

# 2.KafkaOfsetMonitor简述

KafkaOfsetMonitor是有由Kafka开源社区提供的⼀款Web管理界⾯，这个应⽤程序⽤来实时监控 Kafka服务的Consumer以及它们所在的Partition中的Ofset，你可以通过浏览当前的消费者组，并且每 个Topic的所有Partition的消费情况都可以观看的⼀清⼆楚。它让我们很直观的知道，每个Partition的 Mesage是否消费掉，有⽊有阻塞等等。

这个Web管理平台保留的Partition、Ofset和它的Consumer的相关历史数据，我们可以通过浏览 Web管理的相关模块，清楚的知道最近⼀段时间的消费情况。

该Web管理平台有以下功能：

对Consumer的消费监控，并列出每个Consumer的Ofset数据

保护消费者组列表信息

每个Topic的所有Partition列表包含：Topic、Pid、Ofset、LogSize、Lag以及Owner等等

浏览查阅Topic的历史消费信息

这些功能对于我们开发来说，已经绰绰有余了。

# 3.KafkaOfsetMonitor安装部署

## 3.1下载

在安装KafkaOfsetMonitor管理平台时，我们需要先下载其安装包，其资源可以在Github上找到， 考虑到Github访问的限制问题，我将安装包上传到百度云盘：

下载地址

《 》

## 3.2安装部署

KafkaOfsetMonitor的安装部署较为简单，所有的资源都打包到⼀个JAR⽂件中了，因此，直接运 ⾏即可，省去了我们去配置。这⾥我们可以新建⼀个⽬录单独⽤于Kafka的监控⽬录，我这⾥新建⼀个 kafka_monitor⽂件⽬录，然后我们在准备启动脚本，脚本内容如下所示：

- 1 #! /bin/bash

- 2 java -cp KafkaOffsetMonitor-assembly-0.2.0.jar \

- 3 com.quantifind.kafka.offsetapp.OffsetGetterWeb \

- 4 --zk zookeeper01:2181,zookeeper02:2181,zookeeper03:2181 \

- 5 --port 8089 \

- 6 --refresh 10.seconds \

- 7 --retain 1.days


给⼤家解释以下这条启动命令的含义，⾸先我们需要指明运⾏Web监控的类，然后需要⽤到 ZoKeper，所有要填写ZK集群信息，接着是Web运⾏端⼝，⻚⾯数据刷新的时间以及保留数据的时 间值。

## 3.3启动

接下来，我们开始启动，启动步骤如下所示：

- 步骤1:启动ZK（DN1～DN3节点）

1 zkServer.sh start

- 步骤2:启动Kafka服务（集群依次输⼊以下命令启动）

1 kafka-server-start.sh config/server.properties &

- 步骤3:启动Web监控服务


- 1 java -cp KafkaOffsetMonitor-assembly-0.2.0.jar \

- 2 com.quantifind.kafka.offsetapp.OffsetGetterWeb \

- 3 --zk dn1:2181,dn2:2181,dn3:2181 \

- 4 --port 8089 \

- 5 --refresh 10.seconds \

- 6 --retain 1.days


Web服务启动成功后，如下图所示：

![image 1](<Kafka实战－KafkaOffsetMonitor.note_images/imageFile1.png>)

# 4.KafkaOfsetMonitor运⾏预览

下⾯，我们来使⽤Kafka代码⽣产消费⼀些消息，使⽤Web监控来浏览消息情况。⽣产的代码⼤家 可以参考前⾯我写的《 》，这⾥直接预览演示结果，如下图所示：

Kafka实战－简单示例

![image 2](<Kafka实战－KafkaOffsetMonitor.note_images/imageFile2.png>)

![image 3](<Kafka实战－KafkaOffsetMonitor.note_images/imageFile3.png>)

![image 4](<Kafka实战－KafkaOffsetMonitor.note_images/imageFile4.png>)

![image 5](<Kafka实战－KafkaOffsetMonitor.note_images/imageFile5.png>)

![image 6](<Kafka实战－KafkaOffsetMonitor.note_images/imageFile6.png>)

![image 7](<Kafka实战－KafkaOffsetMonitor.note_images/imageFile7.png>)

![image 8](<Kafka实战－KafkaOffsetMonitor.note_images/imageFile8.png>)

# 5.总结

在运⾏KafkaOfsetMonitor的JAR包时，需要确保启动参数的配置正确，以免启动出错，另外， Github的上的KafkaOfsetMonitor的JAR中的静态资源有些链接⽤到了Gogle的超链接，所有如果直 接只⽤，若本地⽊有代理软件会启动出错，这⾥使⽤我所提供的JAR，这个JAR是经过静态资源改版后 重新编译的使⽤本地静态资源。

另外图中的⼀些参数的含义如下：

Topic：创建Topic名称

Partition：分区编号

Ofset：表示该Parition已经消费了多少Mesage

LogSize：表示该Partition⽣产了多少Mesage

Lag：表示有多少条Mesage未被消费

Owner：表示消费者

Created：表示该Partition创建时间

Last Sen：表示消费状态刷新最新时间

# 6.结束语

这篇博客就和⼤家分享到这⾥，如果⼤家在研究学习的过程当中有什么问题，可以加群进⾏讨论 或发送邮件给我，我会尽我所能为您解答，与君共勉！

