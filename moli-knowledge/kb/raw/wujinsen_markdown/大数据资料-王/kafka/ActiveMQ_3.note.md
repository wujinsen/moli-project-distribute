- 1 JMS 在介绍ActiveMQ之前，⾸先简要介绍⼀下JMS规范。 1．1 JMS的基本构件 1．1．1 连接⼯⼚ 连接⼯⼚是客户⽤来创建连接的对象，例如ActiveMQ提供的ActiveMQConectionFactory。


- 1．1．2 连接 JMS Conection封装了客户与JMS提供者之间的⼀个虚拟的连接。
- 1．1．3 会话 JMS Sesion是⽣产和消费消息的⼀个单线程上下⽂。会话⽤于创建消息⽣产者（producer）、

消息消费者（consumer）和消息（mesage）等。会话提供了⼀个事务性的上下⽂，在这个上下 ⽂中，⼀组发送和接收被组合到了⼀个原⼦操作中。

- 1．1．4 ⽬的地 ⽬的地是客户⽤来指定它⽣产的消息的⽬标和它消费的消息的来源的对象。JMS1.0.2规范中定

义了两种消息传递域：点对点（PTP）消息传递域和发布/订阅消息传递域。 点对点消息传递域的特点如下：

每个消息只能有⼀个消费者。

消息的⽣产者和消费者之间没有时间上的相关性。⽆论消费者在⽣产者发送消息的时候是否处 于运⾏状态，它都可以提取消息。

发布/订阅消息传递域的特点如下：

每个消息可以有多个消费者。

⽣产者和消费者之间有时间上的相关性。订阅⼀个主题的消费者只能消费⾃它订阅之后发布的 消息。JMS规范允许客户创建持久订阅，这在⼀定程度上放松了时间上的相关性要求。持久订阅允 许消费者消费它在未处于激活状态时发送的消息。

在点对点消息传递域中，⽬的地被成为队列（queue）；在发布/订阅消息传递域中，⽬的地被成 为主题（topic）。

- 1．1．5 消息⽣产者 消息⽣产者是由会话创建的⼀个对象，⽤于把消息发送到⼀个⽬的地。
- 1．1．6 消息消费者 消息消费者是由会话创建的⼀个对象，它⽤于接收发送到⽬的地的消息。消息的消费可以采⽤


以下两种⽅法之⼀：

同步消费。通过调⽤消费者的receive⽅法从⽬的地中显式提取消息。receive⽅法可以⼀直阻塞到 消息到达。 异步消费。客户可以为消费者注册⼀个消息监听器，以定义在消息到达时所采取的 动作。

- 1．1．7 消息 JMS消息由以下三部分组成：


消息头。每个消息头字段都有相应的geter和seter⽅法。

消息属性。如果需要除消息头字段以外的值，那么可以使⽤消息属性。 消息体。JMS定义的 消息类型有TextMesage、MapMesage、BytesMesage、StreamMesage和 ObjectMesage。

- 1．2 JMS的可靠性机制 1．2．1 确认 JMS消息只有在被确认之后，才认为已经被成功地消费了。消息的成功消费通常包含三个阶


段：客户接收消息、客户处理消息和消息被确认。

在事务性会话中，当⼀个事务被提交的时候，确认⾃动发⽣。在⾮事务性会话中，消息何时被 确认取决于创建会话时的应答模式（acknowledgement mode）。该参数有以下三个可选值：

Sesion.AUTO_ACKNOWLEDGE。当客户成功的从receive⽅法返回的时候，或者从 MesageListener.onMesage⽅法成功返回的时候，会话⾃动确认客户收到的消息。

Sesion.CLIENT_ACKNOWLEDGE。客户通过消息的acknowledge⽅法确认消息。需要注意的 是，在这种模式中，确认是在会话层上进⾏：确认⼀个被消费的消息将⾃动确认所有已被会话消费 的消息。例如，如果⼀个消息消费者消费了10个消息，然后确认第5个消息，那么所有10个消息都 被确认。

Sesion.DUPS_ACKNOWLEDGE。该选择只是会话迟钝第确认消息的提交。如果JMS provider 失败，那么可能会导致⼀些重复的消息。如果是重复的消息，那么JMS provider必须把消息头的 JMSRedelivered字段设置为true。

- 1．2．2 持久性 JMS ⽀持以下两种消息提交模式：


PERSISTENT。指示JMS provider持久保存消息，以保证消息不会因为JMS provider的失败⽽丢 失。

NON_PERSISTENT。不要求JMS provider持久保存消息。

- 1．2．3 优先级 可以使⽤消息优先级来指示JMS provider⾸先提交紧急的消息。优先级分10个级别，从0（最

低）到9（最⾼）。如果不指定优先级，默认级别是4。需要注意的是，JMS provider并不⼀定保 证按照优先级的顺序提交消息。

- 1．2．4 消息过期 可以设置消息在⼀定时间后过期，默认是永不过期。
- 1．2．5 临时⽬的地 可以通过会话上的createTemporaryQueue⽅法和createTemporaryTopic⽅法来创建临时⽬的

地。它们的存在时间只限于创建它们的连接所保持的时间。只有创建该临时⽬的地的连接上的消息 消费者才能够从临时⽬的地中提取消息。

- 1．2．6 持久订阅 ⾸先消息⽣产者必须使⽤PERSISTENT提交消息。客户可以通过会话上的

createDurableSubscriber⽅法来创建⼀个持久订阅，该⽅法的第⼀个参数必须是⼀个topic。第⼆ 个参数是订阅的名称。

JMS provider会存储发布到持久订阅对应的topic上的消息。如果最初创建持久订阅的客户或者 任何其它客户使⽤相同的连接⼯⼚和连接的客户ID、相同的主题和相同的订阅名再次调⽤会话上的 createDurableSubscriber⽅法，那么该持久订阅就会被激活。JMS provider会象客户发送客户处 于⾮激活状态时所发布的消息。

持久订阅在某个时刻只能有⼀个激活的订阅者。持久订阅在创建之后会⼀直保留，直到应⽤程 序调⽤会话上的unsubscribe⽅法。

- 1．2．7 本地事务 在⼀个JMS客户端，可以使⽤本地事务来组合消息的发送和接收。JMS Sesion接⼝提供了


comit和rolback⽅法。事务提交意味着⽣产的所有消息被发送，消费的所有消息被确认；事务回 滚意味着⽣产的所有消息被销毁，消费的所有消息被恢复并重新提交，除⾮它们已经过期。 事 务性的会话总是牵涉到事务处理中，comit或rolback⽅法⼀旦被调⽤，⼀个事务就结束了，⽽另 ⼀个事务被开始。关闭事务性会话将回滚其中的事务。 需要注意的是，如果使⽤请求/回复机制， 即发送⼀个消息，同时希望在同⼀个事务中等待接收该消息的回复，那么程序将被挂起，因为知道 事务提交，发送操作才会真正执⾏。

需要注意的还有⼀个，消息的⽣产和消费不能包含在同⼀个事务中。

- 1．3 JMS 规范的变迁 JMS的最新版本的是1.1。它和同1.0.2版本之间最⼤的差别是，JMS1.1通过统⼀的消息传递域简


化了消息传递。这不仅简化了JMS API，也有利于开发⼈

员灵活选择消息传递域，同时也有助于程序的重⽤和维护。 以下是不同消息传递域的相应接⼝： JMS 公共 点对点域 发布/订阅域 ConectionFactory QueueConectionFactory TopicConectionFactory Conection QueueConection TopicConection Destination Queue Topic Sesion QueueSesion TopicSesion MesageProducer QueueSender TopicPublisher MesageConsumer QueueReceiver TopicSubscriber

- 2 ActiveMQ 2．1 Broker


- 2．1．1 Runing Broker ActiveMQ5.0 的⼆进制发布包中bin⽬录中包含⼀个名为activemq的脚本，直接运⾏这个脚本就


可以启动⼀个broker。

此外也可以通过Broker Configuration URI或Broker XBean URI对broker进⾏配置，以下是⼀些 命令⾏参数的例⼦： Example Description activemq Runs a broker using the default 'xbean:activemq.xml' as the broker configuration file. activemq xbean:myconfig.xml Runs a broker using the file myconfig.xml as the broker configuration file that is located in the claspath. activemq xbean:file:./conf/broker1.xml

- Runs a broker using the file broker1.xml as the broker configuration file that is located in the relative file path ./conf/broker1.xml activemq xbean:file:C:/ActiveMQ/conf/broker2.xml
- Runs a broker using the file broker2.xml as the broker configuration file that is located in the absolute


file path C:/ActiveMQ/conf/broker2.xml activemq broker:(tcp:/localhost:61616, tcp:/localhost:5 0)?useJmx=true Runs a broker with two transport conectors and JMX enabled.

activemq broker:(tcp:/localhost:61616, network:tcp:/localhost:5 0)?persistent=f alse Runs a broker with 1 transport conector and 1 network conector with persistence disabled.

- 2．1．2 Embeded Broker 可以通过在应⽤程序中以编码的⽅式启动broker，例如： Java代码

1. BrokerService broker = new BrokerService(); 2. broker.adConector("tcp:/localhost:61616"); 3. broker.start();

如果需要启动多个broker，那么需要为broker设置⼀个名字。例如： Java代码

1. BrokerService broker = new BrokerService(); 2. broker.setName("fred");

- 3. broker.adConector("tcp:/localhost:61616"); 4. broker.start(); 如果希望在同⼀个JVM内访问这个broker，那么可以使⽤VM Transport，URI是：


vm:/brokerName。关于更多的broker属性，可以参考Apache的官⽅⽂档。 此外，也可以通过BrokerFactory来创建broker，例如： Java代码

1. BrokerService broker = BrokerFactory.createBroker(new URI(someURI); someURI的可选 值如下： URI scheme Example Description xbean: xbean:activemq.xml Searches the claspath for an XML

