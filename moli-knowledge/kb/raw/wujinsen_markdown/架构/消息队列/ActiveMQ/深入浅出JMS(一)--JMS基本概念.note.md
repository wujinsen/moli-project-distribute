摘要：The Mesage Service (JMS) API is a mesaging standard that alows aplication components based on the Platform Enterprise Edition ( ) to create, send, receive, and read mesages. It enables distributed comunication that is l osely coupled, reliable, and asynchronous.

Java java Java E

JMS（ Mesage Service,java消息服务）API是⼀个消息服务的标准或者说是规范，允许应⽤程序组 件基于JavaE平台创建、发送、接收和读取消息。它使分布式通信耦合度更低，消息服务更加可靠以及 异步性。

Java

这篇博⽂我们主要介绍J2E中的⼀个重要规范JMS，因为这个规范在企业中的应⽤⼗分的⼴泛，也⽐较重 要，我们主要介绍JMS的基本概念和它的模式，消息的消费以及JMS编程步骤。

- 1.
- 2.
- 3.

- a.
- b.

- ⅰ.
- ⅱ.
- ⅲ.
- ⅳ.


- c.


- ⅰ.
- ⅱ.
- ⅲ.


- 4. a.


基本概念

JMS是java的消息服务，JMS的客户端之间可以通过JMS服务进⾏异步的消息传输。

消息模型

- ○ Point-to-Point(P2P)
- ○ Publish/Subscribe(Pub/Sub) 即点对点和发布订阅模型


P2P P2P模式图

![image 1](<深入浅出JMS(一)--JMS基本概念.note_images/imageFile1.png>)

这⾥写图⽚描述

涉及到的概念 消息队列（Queue） 发送者(Sender) 接收者(Receiver) 每个消息都被发送到⼀个特定的队列，接收者从队列中获取消息。队列保留着消息，直到他们被 消费或超时。

P2P的特点 每个消息只有⼀个消费者（Consumer）(即⼀旦被消费，消息就不再在消息队列中) 发送者和接收者之间在时间上没有依赖性，也就是说当发送者发送了消息之后，不管接收者有没 有正在运⾏，它不会影响到消息被发送到队列 接收者在成功接收消息之后需向队列应答成功

如果你希望发送的每个消息都应该被成功处理的话，那么你需要P2P模式。

Pub/Sub Pub/Sub模式图

![image 2](<深入浅出JMS(一)--JMS基本概念.note_images/imageFile2.png>)

这⾥写图⽚描述

- b.

- ⅰ.
- ⅱ.
- ⅲ.


- c.


涉及到的概念 主题（Topic） 发布者（Publisher） 订阅者（Subscriber）

客户端将消息发送到主题。多个发布者将消息发送到Topic,系统将这些消息传递给多个订阅者。

Pub/Sub的特点 每个消息可以有多个消费者 发布者和订阅者之间有时间上的依赖性。针对某个主题（Topic）的订阅者，它必须创建⼀个订 阅者之后，才能消费发布者的消息，⽽且为了消费消息，订阅者必须保持运⾏的状态。 为了缓和这样严格的时间相关性，JMS允许订阅者创建⼀个可持久化的订阅。这样，即使订阅者 没有被激活（运⾏），它也能接收到发布者的消息。

- ⅰ.
- ⅱ.
- ⅲ.


如果你希望发送的消息可以不被做任何处理、或者被⼀个消息者处理、或者可以被多个消费者处理的话，那 么可以采⽤Pub/Sub模型

- 5.
- 6.


消息的消费

在JMS中，消息的产⽣和消息是异步的。对于消费来说，JMS的消息者可以通过两种⽅式来消费消息。

- ○ 同步 订阅者或接收者调⽤receive⽅法来接收消息，receive⽅法在能够接收到消息之前（或超时之前）将⼀直阻塞

- ○ 异步 订阅者或接收者可以注册为⼀个消息监听器。当消息到达之后，系统⾃动调⽤监听器的onMesage⽅法。


JMS编程模型

- (1) ConectionFactory 创建Conection对象的⼯⼚，针对两种不同的jms消息模型，分别有QueueConectionFactory和 TopicConectionFactory两种。可以通过JNDI来查找ConectionFactory对象。

- (2) Destination Destination的意思是消息⽣产者的消息发送⽬标或者说消息消费者的消息来源。对于消息⽣产者来说，它的 Destination是某个队列（Queue）或某个主题（Topic）;对于消息消费者来说，它的Destination也是某个队 列或主题（即消息来源）。 所以，Destination实际上就是两种类型的对象：Queue、Topic可以通过JNDI来查找Destination。

- (3) Conection


Conection表示在客户端和JMS系统之间建⽴的链接（对TCP/IP socket的包装）。Conection可以产⽣⼀个 或多个Sesion。跟ConectionFactory⼀样，Conection也有两种类型：QueueConection和 TopicConection。

- (4) Sesion Sesion是我们操作消息的接⼝。可以通过sesion创建⽣产者、消费者、消息等。Sesion提供了事务的功 能。当我们需要使⽤sesion发送/接收多个消息时，可以将这些发送/接收动作放到⼀个事务中。同样，也分 QueueSesion和TopicSesion。

- (5) 消息的⽣产者 消息⽣产者由Sesion创建，并⽤于将消息发送到Destination。同样，消息⽣产者分两种类型： QueueSender和TopicPublisher。可以调⽤消息⽣产者的⽅法（send或publish⽅法）发送消息。

- (6) 消息消费者 消息消费者由Sesion创建，⽤于接收被发送到Destination的消息。两种类型：QueueReceiver和 TopicSubscriber。可分别通过sesion的createReceiver(Queue)或createSubscriber(Topic)来创建。当然， 也可以sesion的creatDurableSubscriber⽅法来创建持久化的订阅者。

- (7) MesageListener 消息监听器。如果注册了消息监听器，⼀旦消息到达，将⾃动调⽤监听器的onMesage⽅法。EJB中的MDB （Mesage-Driven Bean）就是⼀种MesageListener。


7.

企业消息系统的好处

我们先来看看下图，应⽤程序A将Mesage发送到服务器上，然后应⽤程序B从服务器中接收A发来的消息， 通过这个图我们⼀起来分析⼀下JMS的好处：

![image 3](<深入浅出JMS(一)--JMS基本概念.note_images/imageFile3.png>)

这⾥写图⽚描述

- 1.
- 2.
- 3.


提供消息灵活性 松散耦合 异步性

对于JMS的基本概念我们就介绍这么多，下篇博⽂介绍⼀种JMS的实现。

