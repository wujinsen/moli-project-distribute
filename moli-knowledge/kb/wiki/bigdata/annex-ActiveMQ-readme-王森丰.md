---
title: ActiveMQ-readme-王森丰.note（原文插图 annex）
slug: annex-ActiveMQ-readme-王森丰
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/kafka/ActiveMQ-readme-王森丰.note.md
related: [kafka-大数据管道]
created: 2026-07-05
updated: 2026-07-05
---

# 1.下载ActiveMQ

htp:/activemq.apache.org/

去官⽅⽹站下载：

# 2.运⾏ActiveMQ

解压缩apache-activemq-5.5.1-bin.zip，然后双击apache-activemq-5.5.1\bin\activemq.bat运⾏ ActiveMQ程序。 启动ActiveMQ以后，登陆： ，创建⼀个Queue，命名为FirstQueue。

htp:/localhost:8161/admin/

# 3.创建Eclipse项⽬并运⾏

创建project：ActiveMQ-5.5，并导⼊apache-activemq-5.5.1\lib⽬录下需要⽤到的jar⽂件，项⽬结构 如下图所示：

![image 1](assets/imageFile1.png)

- 3.1.Sender.java


- 5 import javax.jms.DeliveryMode;

- 6 import javax.jms.Destination;

- 7 import javax.jms.MessageProducer;

- 8 import javax.jms.Session;

- 9 import javax.jms.TextMessage;

- 10 import org.apache.activemq.ActiveMQConnection;

- 11 import org.apache.activemq.ActiveMQConnectionFactory;

- 12

- 13 public class Sender {

- 14 private static final int SEND_NUMBER = 5;

- 15

- 16 public static void main(String[] args) {

- 17 // ConnectionFactory ：连接⼯⼚，JMS ⽤它创建连接

- 18 ConnectionFactory connectionFactory;

- 19 // Connection ：JMS 客户端到JMS Provider 的连接

- 20 Connection connection = null;

- 21 // Session： ⼀个发送或接收消息的线程

- 22 Session session;

- 23 // Destination ：消息的⽬的地;消息发送给谁.

- 24 Destination destination;

- 25 // MessageProducer：消息发送者

- 26 MessageProducer producer;

- 27 // TextMessage message;

- 28 // 构造ConnectionFactory实例对象，此处采⽤ActiveMq的实现jar

- 29 connectionFactory = new ActiveMQConnectionFactory(

- 30 ActiveMQConnection.DEFAULT_USER,

- 31 ActiveMQConnection.DEFAULT_PASSWORD,

- 32 "tcp://localhost:61616");

- 33 try {

- 34 // 构造从⼯⼚得到连接对象

- 35 connection = connectionFactory.createConnection();

- 36 // 启动

- 37 connection.start();

- 38 // 获取操作连接

- 39 session = connection.createSession(Boolean.TRUE,


- 40 Session.AUTO_ACKNOWLEDGE);

// 获取session注意参数值xingbo.xu-queue是⼀个服务器的queue，须在在ActiveMq 的console配置

- 41

- 42 destination = session.createQueue("FirstQueue");

- 43 // 得到消息⽣成者【发送者】

- 44 producer = session.createProducer(destination);

- 45 // 设置不持久化，此处学习，实际根据项⽬决定

- 46 producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

- 47 // 构造消息，此处写死，项⽬就是参数，或者⽅法获取

- 48 sendMessage(session, producer);

- 49 session.commit();

- 50 } catch (Exception e) {

- 51 e.printStackTrace();

- 52 } finally {

- 53 try {

- 54 if (null != connection)

- 55 connection.close();

- 56 } catch (Throwable ignore) {

- 57 }

- 58 }

- 59 }

- 60

- 61 public static void sendMessage(Session session, MessageProducer producer)

- 62 throws Exception {

- 63 for (int i = 1; i <= SEND_NUMBER; i++) {

- 64 TextMessage message = session

- 65 .createTextMessage("ActiveMq 发送的消息" + i);

- 66 // 发送消息到⽬的地⽅

- 67 System.out.println("发送消息：" + "ActiveMq 发送的消息" + i);

- 68 producer.send(message);

- 69 }

- 70 }

- 71 }


## 3.2.Receiver.java

- 5 import javax.jms.Destination;

- 6 import javax.jms.MessageConsumer;

- 7 import javax.jms.Session;

- 8 import javax.jms.TextMessage;

- 9 import org.apache.activemq.ActiveMQConnection;

- 10 import org.apache.activemq.ActiveMQConnectionFactory;

- 11

- 12 public class Receiver {

- 13 public static void main(String[] args) {

- 14 // ConnectionFactory ：连接⼯⼚，JMS ⽤它创建连接

- 15 ConnectionFactory connectionFactory;

- 16 // Connection ：JMS 客户端到JMS Provider 的连接

- 17 Connection connection = null;

- 18 // Session： ⼀个发送或接收消息的线程

- 19 Session session;

- 20 // Destination ：消息的⽬的地;消息发送给谁.

- 21 Destination destination;

- 22 // 消费者，消息接收者

- 23 MessageConsumer consumer;

- 24 connectionFactory = new ActiveMQConnectionFactory(

- 25 ActiveMQConnection.DEFAULT_USER,

- 26 ActiveMQConnection.DEFAULT_PASSWORD,

- 27 "tcp://localhost:61616");

- 28 try {

- 29 // 构造从⼯⼚得到连接对象

- 30 connection = connectionFactory.createConnection();

- 31 // 启动

- 32 connection.start();

- 33 // 获取操作连接

- 34 session = connection.createSession(Boolean.FALSE,

- 35 Session.AUTO_ACKNOWLEDGE);

// 获取session注意参数值xingbo.xu-queue是⼀个服务器的queue，须在在ActiveMq 的console配置

- 36

- 37 destination = session.createQueue("FirstQueue");

- 38 consumer = session.createConsumer(destination);

- 39 while (true) {


- 40 //设置接收者接收消息的时间，为了便于测试，这⾥谁定为100s

- 41 TextMessage message = (TextMessage) consumer.receive(100000);

- 42 if (null != message) {

- 43 System.out.println("收到消息" + message.getText());

- 44 } else {

- 45 break;

- 46 }

- 47 }

- 48 } catch (Exception e) {

- 49 e.printStackTrace();

- 50 } finally {

- 51 try {

- 52 if (null != connection)

- 53 connection.close();

- 54 } catch (Throwable ignore) {

- 55 }

- 56 }

- 57 }

- 58 }


# 4.注意事项

- 1.
- 2.


最后接收者跟发送者在不同的机器上测试 项⽬所引⽤的jar最后在ActiveMQ下的lib中找，这样不会出现版本冲突。

# 5.测试过程

因为是在单机上测试，所以需要开启两个eclipse，每⼀个eclipse都有⾃身的workspace。我们在

- eclipse1中运⾏Receiver，在eclipse2中运⾏Sender。 刚开始eclipse1中运⾏Receiver以后console介⾯没有任何信息，在eclipse2中运⾏Sender以后，
- eclipse2中的console显示如下信息：


- 发送消息：ActiveMq 发送的消息1
- 发送消息：ActiveMq 发送的消息2
- 发送消息：ActiveMq 发送的消息3
- 发送消息：ActiveMq 发送的消息4
- 发送消息：ActiveMq 发送的消息5 ⽽回到eclipse1中发现console界⾯出现如下信息：


- 收到消息ActiveMq 发送的消息1


- 收到消息ActiveMq 发送的消息2
- 收到消息ActiveMq 发送的消息3
- 收到消息ActiveMq 发送的消息4
- 收到消息ActiveMq 发送的消息5


# PS 2012-2-27

今天发现测试并不需要开启两个eclipse，在⼀个eclipse下⻚可以启动多个程序，并且有多个 console，在上⾯的Receiver.java中，设置⼀个较⼤的时间，⽐如receive(5 0)，如下代码所示：

1 TextMessage message = (TextMessage) consumer.receive(500000);

这个时候运⾏Receiver.java的话，会使得这个Receiver.java⼀直运⾏50秒，在eclipse中可以发现：

![image 2](assets/imageFile2.png)

点击那个红⾊⽅块可以⼿动停⽌运⾏程序。 运⾏玩receiver以后我们在运⾏sender，在运⾏完sender以后，我们要切换到receiver的console，如 下图所示：

![image 3](assets/imageFile3.png)
