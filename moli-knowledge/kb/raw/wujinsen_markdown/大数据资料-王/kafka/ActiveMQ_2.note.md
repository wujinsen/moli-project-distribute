# 1.JMS介绍 JMS源于企业应⽤对于消息中间件的需求，使应⽤程序可以通过消息进⾏异步处理⽽互不影响。

Sun公司和它的合作伙伴设计的JMS API定义了⼀组公共的应⽤程序接⼝和相应语法，使得Java程序能 够和其他消息组件进⾏通信。JMS有四个组成部分：JMS服务提供者、消息管理对象、消息的⽣产者 消费者和消息本身。

- 1)JMS服务提供者实现消息队列和通知，同时实现消息管理的API。JMS已经是J2EE API的⼀部分， J2EE服务器都提供JMS服务。

- 2) 消息管理对象提供对消息进⾏操作的API。JMS API中有两个消息管理对象：创建jms连接使⽤的⼯ ⼚（ConnectionFactory）和⽬的地（Destination），根据消息的消费⽅式的不同ConnectionFactory可 以分为QueueConnectionFactory和TopicConnectionFactory，⽬的地（Destination）可以分为队列 （Queue）和主题（Topic）两种。

- 3)消息的⽣产者和消费者。消息的产⽣由JMS的客户端完成，JMS服务提供者负责管理这些消息，消 息的消费者可以接收消息。消息的⽣产者可以分为――点对点消息发布者（P2P）和主题消息发布者 （TopicPublisher）。所以，消息的消费者分为两类：主题消息的订阅者（TopicSubscriber)和点对点 消息的接收者（queue receiver）

- 4)消息。消息是服务提供者和客户端之间传递信息所使⽤的信息单元。JMS消息由以下三部分组成： 消息头（header）――JMS消息头包含了许多字段，它们是消息发送后由JMS提供者或消息发送


者产⽣，⽤来表示消息、设置优先权和失效时间等等，并且为消息确定路由。 属性（property）――⽤来添加删除消息头以外的附加信息。 消息体（body）――JMS中定义了5种消息体：ByteMessage、MapMessage、ObjectMessage、

StreamMessage和TextMessage。

# 2.Messages 通信⽅式 上⾯提到JMS通信⽅式分为点对点通信和发布/订阅⽅式

- 1)点对点⽅式（point-to-point） 点对点的消息发送⽅式主要建⽴在 Message Queue,Sender,reciever上，Message Queue 存贮消

息，Sneder 发送消息，receive接收消息.具体点就是Sender Client发送Message Queue ,⽽ receiver Cliernt从Queue中接收消息和"发送消息已接受"到Quere,确认消息接收。消息发送客户端与接收客户端 没有时间上的依赖，发送客户端可以在任何时刻发送信息到Queue，⽽不需要知道接收客户端是不是 在运⾏

- 2)发布/订阅 ⽅式（publish/subscriber Messaging） 发布/订阅⽅式⽤于多接收客户端的⽅式.作为发布订阅的⽅式，可能存在多个接收客户端，并且接收


端客户端与发送客户端存在时间上的依赖。⼀个接收端只能接收他创建以后发送客户端发送的信息。 作为subscriber ,在接收消息时有两种⽅法，destination的receive⽅法，和实现message listener 接⼝ 的onMessage ⽅法。

# 3.为什么选⽤ActiveMQ

- 1）ActiveMQ是⼀个开放源码

- 2）基于Apache 2.0 licenced 发布并实现了JMS 1.1。

- 3）ActiveMQ现在已经和作为很多项⽬的异步消息通信核⼼了

- 4）在很多中⼩型项⽬中采⽤ActiveMQ+SPRING+TOMCAT开发模式。


- 4.编程模式

- 4.1消息产⽣者向JMS发送消息的步骤

- (1)创建连接使⽤的⼯⼚类JMS ConnectionFactory

- (2)使⽤管理对象JMS ConnectionFactory建⽴连接Connection

- (3)使⽤连接Connection 建⽴会话Session

- (4)使⽤会话Session和管理对象Destination创建消息⽣产者MessageSender

- (5)使⽤消息⽣产者MessageSender发送消息


- 4.2消息消费者从JMS接受消息的步骤


- (1)创建连接使⽤的⼯⼚类JMS ConnectionFactory

- (2)使⽤管理对象JMS ConnectionFactory建⽴连接Connection

- (3)使⽤连接Connection 建⽴会话Session

- (4)使⽤会话Session和管理对象Destination创建消息消费者MessageReceiver

- (5)使⽤消息消费者MessageReceiver接受消息，需要⽤setMessageListener将MessageListener接⼝绑 定到MessageReceiver 消息消费者必须实现了MessageListener接⼝，需要定义onMessage事件⽅法。


- 5.ActiveMQ运⾏ ActiveMQ5.0版本默认启动时，启动了内置的jetty服务器，提供⼀个demo应⽤和⽤于监控ActiveMQ的 admin应⽤。运⾏%activemq_home%bin/⽬录下的 activemq.bat , 之后你会看⻅如下⼀段话表示启动 成功。 打开 ，可以查看相应的queue中是否有消息

- 6.SendMessage(⽤于发送消息)


htp:/localhost:8161/admin/queues.jsp

Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


import javax.jms.Conection; import javax.jms.Destination; import javax.jms.JMSException; import javax.jms.MesageProducer; import javax.jms.Sesion; import javax.jms.TextMesage;

- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.


import org.apache.activemq.ActiveMQConectionFactory;

publicclas SendMesage { privatestaticfinal String url ="tcp:/localhost:61616"; privatestaticfinal String QUEUE_NAME ="choice.queue"; protected String expectedBody = "<helo>world!</helo>"; publicvoid sendMesage() throws JMSException{

Conection conection =nul; try{

ActiveMQConectionFactory conectionFactory =new ActiveMQConectionFactory(url); conection = (Conection)conectionFactory.createConection(); conection.start(); Sesion sesion = (Sesion)conection.createSesion(false, Sesion.AUTO_ACKNOWLEDGE

);

Destination destination = sesion.createQueue(QUEUE_NAME); MesageProducer producer = sesion.createProducer(destination); TextMesage mesage = sesion.createTextMesage(expectedBody); mesage.setStringProperty("headname", "remoteB"); producer.send(mesage);

}catch(Exception e){ e.printStackTrace(); }

}

publicstaticvoid main(String[] args){ SendMesage sndMsg = new SendMesage(); try{

sndMsg.sendMesage(); }catch(Exception ex){

System.out.println(ex.toString(); }

} }

- 7.ReceiveMessage(⽤于接收消息)


Java代码 import java.io.File; import java.io.FileInputStream; import java.io.FileOutputStream; import java.io.IOException; import javax.jms.BytesMesage; import javax.jms.Conection; import javax.jms.Destination; import javax.jms.JMSException; import javax.jms.Mesage; import javax.jms.MesageConsumer; import javax.jms.Sesion; import javax.jms.TextMesage; import org.apache.activemq.ActiveMQConectionFactory;

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
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.


publicclas ReceiveMesage { privatestaticfinal String url = "tcp:/localhost:61616"; privatestaticfinal String QUEUE_NAME = "choice.queue"; publicvoid receiveMesage() { Conection conection = nul; try {

try { ActiveMQConectionFactory conectionFactory = new ActiveMQConectionFactory(url); conection = conectionFactory.createConection();

} catch (Exception e) { System.out.println(e.toString();

} conection.start(); Sesion sesion = conection.createSesion(false,

Sesion.AUTO_ACKNOWLEDGE); Destination destination = sesion.createQueue(QUEUE_NAME); MesageConsumer consumer = sesion.createConsumer(destination); consumeMesagesAndClose(conection, sesion, consumer);

} catch (Exception e) {

System.out.println(e.toString(); }

- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.


protectedvoid consumeMesagesAndClose(Conection conection,Sesion sesion, Mesa geConsumer consumer)

throws JMSException {

for (int i = 0; i < 1;) { Mesage mesage = consumer.receive(1 0); if (mesage != nul) {

i +; onMesage(mesage);

} } System.out.println("Closing conection"); consumer.close(); sesion.close(); conection.close();

}

publicvoid onMesage(Mesage mesage) { try {

if (mesage instanceof TextMesage) { TextMesage txtMsg = (TextMesage) mesage; String msg = txtMsg.getText(); System.out.println("Received: " + msg);

} } catch (Exception e) {

e.printStackTrace(); }

}

publicstaticvoid main(String args[]) { ReceiveMesage rm = new ReceiveMesage(); rm.receiveMesage();

}

