JMS介绍

JMS(Java Mesage Service) 是java平台关于⾯向消息中间件的api接⼝，⽤于在应⽤程序和分布式 系统中发送消息，进⾏异步通信。

JMS提供了⼀套类似JDBC的技术规范，服务的实现由具体的实现提供商提供。

JMS可以解决诸多的体系结构性问题，⽐如异构系统集成通信，缓解系统瓶颈，提⾼系统的伸缩性 （异步、⾮点对点的模式使得处理消息的应⽤可以⽔平扩展），增强系统⽤户体验，使得系统模块 化和组件化变得可⾏并更加灵活。

JMS的集群系统有以下三个⻆⾊：消息传送客户端（⽣产者）、服务器（消息存放服务）、消息接 受客户端（消费者）

消息传送客户端：客户端向服务器端发送消息，我们⼀般称之为消息⽣产者。

服务端：将消息保存在服务端的某个介质上。

消息接受客户端：服务主动或被动的将消息发送给⼀个或者多个消息接收的客户端，我们⼀般称之为消息 消费者。

JMS⽀持两类消息传送模型：点对点模式和发布/订阅模式。

点对点适⽤于⼀对⼀的消息传送，⽽发布/订阅模型则适⽤于消息组播的场景。点对点模型通常 是⼀个基于拉取或者轮询的消息传送模型，这种模型从队列中请求信息，⽽不是将消息推送到 客户端。这个模型的特点是发送到队列的消息被⼀个且只有⼀个接收者接收处理，即使有多个 消息监听者也是如此。基于这⼀点，JMS可以使⽤这种消息传送模型做负载均衡。点对点模型 还可以允许接收者在接收消息之前查看消息的内容，⽽发布订阅模型则不⾏。

发布订阅模型则是⼀个基于推送的消息传送模型。发布订阅模型可以⽤多种不同的订阅者，临 时订阅者只在主动监听主题时才接收消息，⽽持久订阅者则监听主题的所有消息，即时当前订 阅者不可⽤，处于离线状态。 两者的区别：对于点到点模型，消息⽣产者产⽣⼀个消息后，把这个消息发送到⼀个Queue （队列）中，然后消息接收者再从这个Queue中读取，⼀旦这个消息被⼀个接收者读取之后， 它就在这个Queue中消失了，所以⼀个消息只能被⼀个接收者消费。与点到点模型不同，发布/ 订阅模型中，消息⽣产者产⽣⼀个消息后，把这个消息发送到⼀个Topic中，这个Topic可以同 时有多个接收者在监听，当⼀个消息到达这个Topic之后，所有消息接收者都会收到这个消息。

JMS的组件介绍

Destination ：消息发送的⽬的地，也就是前⾯说的Queue和Topic。

创建好⼀个消息之后，只需要把这个消息发送到⽬的地，消息的发送者就可以继续做⾃⼰的事 情，⽽不⽤等待消息被处理完成。⾄于这个消息什么时候，会被哪个消费者消费，完全取决于消息的 接受者。Mesage ：从字⾯上就可以看出是被发送的消息。它有下⾯⼏种类型：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


StreamMesage：Java 数据流消息，⽤标准流操作来顺序的填充和读取。

MapMesage：⼀个Map类型的消息；名称为 string 类型，⽽值为 Java 的基本类型。

TextMesage：普通字符串消息，包含⼀个String。

ObjectMesage：对象消息，包含⼀个可序列化的Java 对象

BytesMesage：⼆进制数组消息，包含⼀个byte[]。

XMLMesage: ⼀个XML类型的消息。

最常⽤的是TextMesage和ObjectMesage。Sesion： 与JMS提供者所建⽴的会话，通过 Sesion我们才可以创建⼀个Mesage。Conection： 与JMS提供者建⽴的⼀个连接。可以从这个连 接创建⼀个会话，即Sesion。ConectionFactory: 那如何创建⼀个Conection呢？这就需要下⾯讲到 的ConectionFactory了。通过这个⼯⼚类就可以得到⼀个与JMS提供者的连接，即Conection。 Producer： 消息的⽣产者，要发送⼀个消息，必须通过这个⽣产者来发送。MesageConsumer： 与 ⽣产者相对应，这是消息的消费者或接收者，通过它来接收⼀个消息。

JMS的的应⽤场景 消息系统的核⼼作⽤就是三点：解耦，异步和并⾏。 下⾯让我以⼀个实际的例⼦来说明⼀下解耦异步和并⾏分别所代表的具体意义吧： 假设我们有这么⼀个应⽤场景，为了完成⼀个⽤户注册淘宝的操作，可能需要将⽤户信息写⼊到⽤户 库中，然后通知给红包中⼼给⽤户发新⼿红包，然后还需要通知⽀付宝给⽤户准备对应的⽀付宝账 号，进⾏合法性验证，告知sns系统给⽤户导⼊新的⽤户等10步操作。那么针对这个场景，⼀个最简单 的设计⽅法就是串⾏的执⾏整个流程，如图3-1所示：图3-1-⽤户注册流程这种⽅式的最⼤问题是，随 着后端流程越来越多，每步流程都需要额外的耗费很多时间，从⽽会导致⽤户更⻓的等待延迟。⾃然 的，我们可以采⽤并⾏的⽅式来完成业务，能够极⼤的减少延迟，如图3-2所示。图3-2-⽤户注册流 程-并⾏⽅式但并⾏以后⼜会有⼀个新的问题出现了，在⽤户注册这⼀步，系统并⾏的发起了4个请 求，那么这四个请求中，如果通知SNS这⼀步需要的时间很⻓，⽐如需要10秒钟的话，那么就算是发 新⼿包，准备⽀付宝账号，进⾏合法性验证这⼏个步骤的速度再快，⽤户也仍然需要等待10秒以后才 能完成⽤户注册过程。因为只有当所有的后续操作全部完成的时候，⽤户的注册过程才算真正的“完 成”了。⽤户的信息状态才是完整的。⽽如果这时候发⽣了更严重的事故，⽐如发新⼿红包的所有服务 器因为业务逻辑bug导致down机，那么因为⽤户的注册过程还没有完全完成，业务流程也就是失败的 了。这样明显是不符合实际的需要的，随着下游步骤的逐渐增多，那么⽤户等待的时间就会越来越 ⻓，并且更加严重的是，随着下游系统越来越多，整个系统出错的概率也就越来越⼤。通过业务分析 我们能够得知，⽤户的实际的核⼼流程其实只有⼀个，就是⽤户注册。⽽后续的准备⽀付宝，通知sns 等操作虽然必须要完成，但却是不需要让⽤户等待的。 这种模式有个专业的名词，就叫最终⼀致。为 了达到最终⼀致，我们引⼊了MQ系统。业务流程如下：主流程如图3-3所示：图3-3-⽤户注册流程-引 ⼊MQ系统-主流程异步流程如图3-4所示：案列来源于：

htp:/blog.csdn.net/pony12/article/details/3 8919751

JMS的相关产品

JMS消息服务器 ActiveMQ ActiveMQ 是Apache出品，最流⾏的，能⼒强劲的开源消息总线。ActiveMQ 是⼀个完全⽀持JMS1.1 和J2E 1.4规范的 JMS Provider实现,尽管JMS规范出台已经是很久的事情了,但是JMS在当今的J2E 应⽤中间仍然扮演着特殊的地位。主要特点：

多种语⾔和协议编写客户端。语⾔: Java, C, C+, C#, Ruby, Perl, Python, PHP。应⽤协 议: OpenWire,Stomp REST,WS Notification,XMP,AMQP

完全⽀持JMS1.1和J2E 1.4规范 (持久化,XA消息,事务)

对Spring的⽀持,ActiveMQ可以很容易内嵌到使⽤Spring的系统⾥⾯去,⽽且也⽀持Spring2.0的特性

通过了常⻅J2E服务器(如 Geronimo,JBos 4, GlasFish,WebLogic)的测试,其中通过 JCA 1.5 resource adaptors的配置,可以让ActiveMQ可以⾃动的部署到任何兼容J2E 1.4 商业服务 器上

⽀持多种传送协议:in-VM,TCP, SL,NIO,UDP,JGroups,JXTA

⽀持通过JDBC和journal提供⾼速的消息持久化

从设计上保证了⾼性能的集群,客户端-服务器,点对点

⽀持Ajax

⽀持与Axis的整合

可以很容易得调⽤内嵌JMS provider,进⾏测试分布式发布订阅消息系统 Kafkakafka是⼀种⾼吞吐 量的分布式发布订阅消息系统，她有如下特性：通过O(1)的磁盘数据结构提供消息的持久化，这种 结构对于即使数以TB的消息存储也能够保持⻓时间的稳定性能。⾼吞吐量：即使是⾮常普通的硬件 kafka也可以⽀持每秒数⼗万的消息。⽀持通过kafka服务器和消费机集群来分区消息。⽀持Hadop 并⾏数据加载。卡夫卡的⽬的是提供⼀个发布订阅解决⽅案，它可以处理消费者规模的⽹站中的所 有动作流数据。 这种动作（⽹页浏览，搜索和其他⽤户的⾏动）是在现代⽹络上的许多社会功能的 ⼀个关键因素。 这些数据通常是由于吞吐量的要求⽽通过处理⽇志和⽇志聚合来解决。 对于像 Hadoop的⼀样的⽇志数据和离线分析系统，但又要求实时处理的限制，这是⼀个可⾏的解决⽅案。 kafka的⽬的是通过Hadoop的并⾏加载机制来统⼀线上和离线的消息处理，也是为了通过集群机来 提供实时的消费。

分布式消息中间件 Metamorphosis Metamorphosis (MetaQ) 是⼀个⾼性能、⾼可⽤、可扩展的分布式消息中间件，类似于LinkedIn的 Kafka，具有消息存储顺序写、吞吐量⼤和⽀持本地和XA事务等特性，适⽤于⼤吞吐量、顺序消息、⼴ 播和⽇志数据传输等场景，在淘宝和⽀付宝有着⼴泛的应⽤，现已开源。主要特点：

⽣产者、服务器和消费者都可分布

消息存储顺序写

性能极⾼,吞吐量⼤

⽀持消息顺序

⽀持本地和XA事务

客户端pul，随机读,利⽤sendfile系统调⽤，zero-copy ,批量拉数据

⽀持消费端事务

⽀持消息⼴播模式 ⽀持异步发送消息 ⽀持htp协议

⽀持消息重试和recover

数据迁移、扩容对⽤户透明

消费状态保存在客户端

⽀持同步和异步复制两种HA

⽀持group comit

更多 …

分布式消息中间件 RocketMQ

RocketMQ 是⼀款分布式、队列模型的消息中间件，具有以下特点：

能够保证严格的消息顺序 提供丰富的消息拉取模式 ⾼效的订阅者⽔平扩展能⼒

实时的消息订阅机制

亿级消息堆积能⼒

Metaq3.0 版本改名，产品名称改为RocketMQ

其他MQ

.NET消息中间件 DotNetMQ

基于HBase的消息队列 HQueue

Go 的 MQ 框架 KiteQ

AMQP消息服务器 RabitMQ

MemcacheQ 是⼀个基于 MemcacheDB 的消息队列服务器。

更多 …

Kafka的基础

ApacheKafka是⼀个开源消息系统，由Scala写成。是由Apache软件基⾦会开发的⼀个开源消息系 统项⽬。

Kafka最初是由LinkedIn开发，并于201年初开源。2012年10⽉从Apache Incubator毕业。该项⽬ 的⽬标是为处理实时数据提供⼀个统⼀、⾼通量、低等待的平台。

kafka是⼀个分布式消息队列：⽣产者、消费者的功能。它提供了类似于JMS的特性，但是在设计实 现上完全不同，此外它并不是JMS规范的实现。

kafka对消息保存时根据Topic进⾏归类，发送消息者称为Producer,消息接受者称为Consumer,此外 kafka集群有多个kafka实例组成，每个实例(server)成为broker。

⽆论是kafka集群，还是producer和consumer都依赖于zokeper集群保存⼀些meta信息，来保证 系统可⽤性。

Kafka的应⽤场景

Websit activity tracking:⽹站活性跟踪，kafka可以作为"⽹站活性跟踪"的最佳⼯具;可以将⽹⻚/⽤ 户操作等信息发送到kafka中.并实时监控,或者离线统计分析等

Log Agregation:⽇志收集中⼼，kafka的特性决定它⾮常适合作为"⽇志收集中⼼";aplication可以 将操作⽇志"批量 "异步"的发送到kafka集群中,⽽不是保存在本地或者DB中;kafka可以批量提交消 息/压缩消息等,这对producer端⽽⾔,⼏乎感觉不到性能的开⽀.此时consumer端可以使hadop等其 他系统化的存储和分析系统.

kafka的⽬的是通过Hadop的并⾏加载机制来统⼀线上和离线的消息处理，也是为了通过集群机来 提供实时的消费。

Kafka的组件介绍

Topic ：消息根据Topic进⾏归类

Producer：发送消息者

Consumer：消息接受者

broker：每个kafka实例(server)

Zokeper：依赖集群保存meta信息。

![image 1](<第三章 JMS原理及kafka基础.note_images/imageFile1.png>)

Kafka的安装部署及配置⽂件 安装前的准备⼯作（zk集群已经部署完毕）

关闭防⽕墙

chkconfig iptables of & setenforce 0

创建⽤户

groupad realtime & userad realtime & usermod -a -G realtime realtime

创建⼯作⽬录并赋权

mkdir /export mkdir /export/servers chmod 75 -R /export

切换到realtime⽤户下

su realtime

- 1、进⼊kafka官⽅⽹站：
- 2、进⼊安装包下载⻚⾯：
- 3、在linux中使⽤wget命令下载安装包 wget
- 4、解压⽂件并创建软连接 tar -zxvf /export/software/kafka_2.1-0.8.2.2.tgz -C /export/servers/ cd /export/servers/ ln -s kafka_2.1-0.8.2.2 kafka
- 5、修改配置⽂件 cp /export/servers/kafka/config/server.properties /export/servers/kafka/config/server.properties.bak


htp:/kafka.apache.org/

![image 2](<第三章 JMS原理及kafka基础.note_images/imageFile2.png>)

htp:/kafka.apache.org/downloads.html

![image 3](<第三章 JMS原理及kafka基础.note_images/imageFile3.png>)

htp:/mirors.hust.edu.cn/apache/kafka/0.8.2.2/kafka_2.1-0.8.2.2.tgz

![image 4](<第三章 JMS原理及kafka基础.note_images/imageFile4.png>)

vi/export/servers/kafka/config/server.properties 输⼊以下内容：

![image 5](<第三章 JMS原理及kafka基础.note_images/imageFile5.png>)

- 6、将配置好的⽂件kafka安装⽂件拷⻉到其它机器 scp -r /export/servers/kafka_2.1-0.8.2.2 kafka02:/export/servers 然后分别在各机器上创建软连 cd /export/servers/ ln -skafka_2.1-0.8.2.2 kafka

- 7、依次修改各服务器上配置⽂件的的broker.id，分别是0,1,2不得重复。
- 8、启动kafka集群 依次在各节点上启动kafka bin/kafka-server-start.sh config/server.properties 结果如下


![image 6](<第三章 JMS原理及kafka基础.note_images/imageFile6.png>)

8、常⽤命令

查看当前服务器中的所有topic

bin/kafka-topics.sh-list -zokeper zk01 2181

创建topic

bin/kafka-topics.sh-create-zokeper zk01 2181-replication-factor 1-partitions 1topic test

删除topic

sh bin/kafka-topics.sh-delete-zokeper zk01 2181-topic test 删除topic需要server.properties中设置delete.topic.enable=true否则只是标记删除或者直接重 启。

通过shel命令发送消息

bin/kafka-console-producer.sh-broker-list localhost:9092-topic test

![image 7](<第三章 JMS原理及kafka基础.note_images/imageFile7.png>)

通过shel消费消息

sh kafka-console-consumer.sh-zokeper zk01 2181-from-begi ning-topic test

![image 8](<第三章 JMS原理及kafka基础.note_images/imageFile8.png>)

查看消费位置 sh kafka-run-clas.sh kafka.tols.ConsumerOfsetChecker-zokeper zk01 2181group testGroup

![image 9](<第三章 JMS原理及kafka基础.note_images/imageFile9.png>)

产看某个Top的详情 sh kafka-topics.sh-topic test -describe-zokeper zk01 2181

![image 10](<第三章 JMS原理及kafka基础.note_images/imageFile10.png>)

Kafka源码下载

- 1、登陆github并寻找到kafka的开源分⽀：
- 2、使⽤subversion checkout 最新的分⽀： 2 具体过程详⻅storm源码下载。


htps:/github.com/apache/kafka htps:/github.com/apache/kafka/tre/0.8.2.

Kafka的⽣产者（Java API）

![image 11](<第三章 JMS原理及kafka基础.note_images/imageFile11.png>)

Kafka的消费者(Java API)

![image 12](<第三章 JMS原理及kafka基础.note_images/imageFile12.png>)

附件：

