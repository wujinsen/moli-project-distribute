---
title: Netty高性能之Reactor线程模型.note（原文插图 annex）
slug: annex-Netty高性能之Reactor线程模型
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/并发编程/Netty/Netty高性能之Reactor线程模型.note.md
related: [bio-nio-aio对比]
created: 2026-07-05
updated: 2026-07-05
---

Nety是⼀个⾼性能、异步事件驱动的NIO框架，它提供了对TCP、UDP和⽂件传输的⽀持，作为⼀个 异步NIO框架，Nety的所有IO操作都是异步⾮阻塞的，通过Future-Listener机制，⽤户可以⽅便的主 动获取或者通过通知机制获得IO操作结果。

作为当前最流⾏的NIO框架，Nety在互联⽹领域、⼤数据分布式计算领域、游戏⾏业、通信⾏业等获 得了⼴泛的应⽤，⼀些业界著名的开源组件也基于Nety的NIO框架构建 传统通信采⽤了同步阻塞IO，当客户端的并发压⼒或者⽹络时延增⼤之后，同步阻塞IO会由于频繁的 wait导致IO线程经常性的阻塞，由于线程⽆法⾼效的⼯作，IO处理能⼒⾃然下降。

我们通过BIO通信模型图看下BIO通信的弊端

![image 1](assets/imageFile1.png)

采⽤BIO通信模型的服务端，通常由⼀个独⽴的Aceptor线程负责监听客户端的连接，接收到客户端 连接之后为客户端连接创建⼀个新的线程处理请求消息，处理完成之后，返回应答消息给客户端，线 程销毁，这就是典型的⼀请求⼀应答模型。

该架构最⼤的问题就是不具备弹性伸缩能⼒，当并发访问量增加后，服务端的线程个数和并发访问数 成线性正⽐，由于线程是JAVA虚拟机⾮常宝贵的系统资源，当线程数膨胀之后，系统的性能急剧下 降，随着并发量的继续增加，可能会发⽣句柄溢出、线程堆栈溢出等问题，并导致服务器最终宕机。 Nety基于NIO,实现了对NIO的封装及优化，从⽽Nety的通信模式为异步⾮阻塞通信 在IO编程过程中，当需要同时处理多个客户端接⼊请求时，可以利⽤多线程或者IO多路复⽤技术进⾏ 处理。IO多路复⽤技术通过把多个IO的阻塞复⽤到同⼀个select的阻塞上，从⽽使得系统在单线程的情 况下可以同时处理多个客户端请求。与传统的多线程/多进程模型⽐，I/O多路复⽤的最⼤优势是系统开 销⼩，系统不需要创建新的额外进程或者线程，也不需要维护这些进程和线程的运⾏，降低了系统的 维护⼯作量，节省了系统资源。

JDK1.4提供了对⾮阻塞IO（NIO）的⽀持，JDK1.6版本使⽤epol替代了传统的select/pol，极⼤的提升 了NIO通信的性能 Nety架构按照Reactor模式设计和实现，它的服务端通信序列图如下

![image 2](assets/imageFile2.png)

Nety的IO线程NioEventLop由于聚合了多路复⽤器Selector，可以同时并发处理成百上千个客户端 Chanel，由于读写操作都是⾮阻塞的，这就可以充分提升IO线程的运⾏效率，避免由于频繁IO阻塞导 致的线程挂起。另外，由于Nety采⽤了异步通信模式，⼀个IO线程可以并发处理N个客户端连接和读 写操作，这从根本上解决了传统同步阻塞IO⼀连接⼀线程模型，架构的性能、弹性伸缩能⼒和可靠性 都得到了极⼤的提升。

何为Reactor线程模型？ Reactor模式是事件驱动的，有⼀个或多个并发输⼊源，有⼀个ServiceHandler，有多个Request Handlers；这个Service Handler会同步的将输⼊的请求（Event）多路复⽤的分发给相应的Request Handler

![image 3](assets/imageFile3.png)

从结构上，这有点类似⽣产者消费者模式，即有⼀个或多个⽣产者将事件放⼊⼀个Queue中，⽽⼀个 或多个消费者主动的从这个Queue中Pol事件来处理；⽽Reactor模式则并没有Queue来做缓冲，每当 ⼀个Event输⼊到Service Handler之后，该Service Handler会⽴刻的根据不同的Event类型将其分 发给对应的Request Handler来处理。

这个做的好处有很多，⾸先我们可以将处理event的Request handler实现⼀个单独的线程，即

![image 4](assets/imageFile4.png)

这样Service Handler 和request Handler实现了异步，加快了service Handler处理event的速度， 那么每⼀个request同样也可以以多线程的形式来处理⾃⼰的event,即Thread1 扩展成Thread pol 1,

Nety的Reactor线程模型1 Reactor单线程模型 Reactor机制中保证每次读写能⾮阻塞读写

![image 5](assets/imageFile5.png)

⼀个线程(单线程)来处理CONECT事件(Aceptor)，⼀个线程池（多线程）来处理read,⼀个线程池 （多线程）来处理write,那么从Reactor Thread到handler都是异步的，从⽽IO操作也多线程化。 到这⾥跟BIO对⽐已经提升了很⼤的性能，但是还可以继续提升，由于Reactor Thread依然为单线程， 从性能上考虑依然有所限制

- 2 Reactor多线程模型

、 这样通过Reactor Thread Pol来提⾼event的分发能⼒

- 3 Reactor主从模型


![image 6](assets/imageFile6.png)

![image 7](assets/imageFile7.png)

Netty的⾼效并发编程主要体现在如下⼏点：

- 1) volatile的⼤量、正确使⽤;

- 2) CAS和原⼦类的⼴泛使⽤；

- 3) 线程安全容器的使⽤；

- 4) 通过读写锁提升并发性能。 Nety除了使⽤reactor来提升性能，当然还有


- 1、零拷⻉，IO性能优化

- 2、通信上的粘包拆包


- 2、同步的设计

- 3、⾼性能的序列
