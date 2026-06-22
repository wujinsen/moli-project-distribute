现实的企业中，对于消息通信的应⽤⼀直都⾮常的⽕热，⽽且在J2E的企业应⽤中扮演着特殊的⻆⾊，所以 对于它研究是⾮常有必要的。 上篇博⽂ ，我们介绍了消息通信的规范JMS，我们这篇博⽂介绍⼀款开源的 JMS具体实现⸺ActiveMQ。ActiveMQ是⼀个易于使⽤的消息中间件。

深⼊浅出JMS(⼀)–JMS基本概念

# 消息中间件

我们简单的介绍⼀下消息中间件，对它有⼀个基本认识就好，消息中间件（MOM：Mesage Orient mi dleware）。 消息中间件有很多的⽤途和优点：

- 1. 将数据从⼀个应⽤程序传送到另⼀个应⽤程序，或者从软件的⼀个模块传送到另外⼀个模块；

- 2. 负责建⽴⽹络通信的通道，进⾏数据的可靠传送。

- 3. 保证数据不重发，不丢失

- 4. 能够实现跨平台操作，能够为不同 上的软件集成技⼯数据传送服务


操作系统

# MQ

⾸先简单的介绍⼀下MQ，MQ英⽂名MesageQueue，中⽂名也就是⼤家⽤的消息队列，⼲嘛⽤的呢，说⽩ 了就是⼀个消息的接受和转发的容器，可⽤于消息推送。 下⾯进⼊我们今天的主题，为⼤家介绍ActiveMQ：

# ActiveMQ

简要概述ActiveMQ

Apache ActiveMQ ™ is the most popular and powerful open source messaging and Integration Patterns server.

Apache ActiveMQ is fast, supports many Cross Language Clients and Protocols, comes with easy to use Enterprise Integration Patterns and many advanced features while fully supporting JMS 1.1 and J2EE 1.4.

ActiveMQ是由Apache出品的，⼀款最流⾏的，能⼒强劲的开源消息总线。ActiveMQ是⼀个完全⽀持JMS1.1 和J2E 1.4规范的 JMS Provider实现，它⾮常快速，⽀持多种语⾔的客户端和协议，⽽且可以⾮常容易的嵌 ⼊到企业的应⽤环境中，并有许多⾼级功能。 下⾯我们下载⼀个版本，玩⼀玩。 下载ActiveMQ 官⽅⽹站： 现在ActiveMQ最新的版本是5.1.1，下载挺简单的，就不再截图了。 运⾏ActiveMQ服务

htp:/activemq.apache.org/

- 1. 下载，解压缩


⼤家现在好之后，将apache-activemq-5.1.1-bin.zip解压缩，我们可以看到它的整体⽬录结构：

![image 1](<深入浅出JMS(二)--ActiveMQ简单介绍以及安装.note_images/imageFile1.png>)

这⾥写图⽚描述

从它的⽬录来说，还是很简单的：

bin存放的是脚本⽂件 conf存放的是基本配置⽂件 data存放的是⽇志⽂件 docs存放的是说明⽂档 examples存放的是简单的实例 lib存放的是activemq所需jar包 webaps⽤于存放项⽬的⽬录

- 2. 启动ActiveMQ


我们了解activemq的基本⽬录，下⾯我们运⾏⼀下activemq服务，双击bin⽬录下的activemq.bat脚本⽂件或 运⾏⾃⼰电脑版本下的activemq.bat，就可以看下图的效果。

![image 2](<深入浅出JMS(二)--ActiveMQ简单介绍以及安装.note_images/imageFile2.png>)

这⾥写图⽚描述

- 从上图我们可以看到activemq的存放地址，以及浏览器要访问的地址.
- 3. ActiveMQ默认使⽤的TCP连接端⼝是61616, 通过查看该端⼝的信息可以测试ActiveMQ是否成功启动 netstat

-an|find “61616”

C:\Documents and Settings\Administrator>netstat -an|find "61616" TCP 0.0.0.0:61616 0.0.0.0:0 LISTENING

- 4. 监控 ActiveMQ默认启动时，启动了内置的jety服务器，提供⼀个⽤于监控ActiveMQ的admin应⽤。 admin： ⽤户名和密码都是admin


测试

htp:/127.0.0.1 8161/admin/

![image 3](<深入浅出JMS(二)--ActiveMQ简单介绍以及安装.note_images/imageFile3.png>)

这⾥写图⽚描述

- 5. ⾄此，服务端启动完毕 停⽌服务器，只需要按着Ctrl+Shift+C，之后输⼊y即可。 我们简单说说ActiveMQ特性，⽹上很多，只是为了保证博⽂的完整。 ActiveMQ特性列表


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


多种语⾔和协议编写客户端。语⾔: Java, C, C+, C#, Ruby, Perl, Python, PHP。应⽤协议: OpenWire,Stomp REST,WS Notification,XMP,AMQP 完全⽀持JMS1.1和J2E 1.4规范 (持久化,XA消息,事务) 对Spring的⽀持,ActiveMQ可以很容易内嵌到使⽤Spring的系统⾥⾯去,⽽且也⽀持Spring2.0的特性 通过了常⻅J2E服务器(如 Geronimo,JBos 4, GlasFish,WebLogic)的测试,其中通过JCA 1.5 resource adaptors的配置,可以让ActiveMQ可以⾃动的部署到任何兼容J2E 1.4 商业服务器上 ⽀持多种传送协议:in-VM,TCP, SL,NIO,UDP,JGroups,JXTA ⽀持通过JDBC和journal提供⾼速的消息持久化 从设计上保证了⾼性能的集群,客户端-服务器,点对点 ⽀持Ajax ⽀持与Axis的整合 可以很容易得调⽤内嵌JMS provider,进⾏测试

什么情况下使⽤ActiveMQ?

- 1.
- 2.
- 3.


多个项⽬之间集成

- (1) 跨平台

- (2) 多语⾔

- (3) 多项⽬


降低系统间模块的耦合度，解耦

(1) 软件扩展性

系统前后端隔离

(1) 前后端隔离，屏蔽⾼安全区 其实ActiveMQ的应⽤还有很多，⼤家可以上⽹查查，不再⼀⼀举例。

# 总结

ActiveMQ并不难，具有很多的优势。 下篇博⽂，我们做⼀个简单实例，真正的体会⼀把ActiveMQ的魅⼒。

