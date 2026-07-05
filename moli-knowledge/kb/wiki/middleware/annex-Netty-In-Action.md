---
title: Netty In Action.note（原文插图 annex）
slug: annex-Netty-In-Action
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/netty/Netty In Action.note.md
related: [netty-pipeline与编解码]
created: 2026-07-05
updated: 2026-07-05
---

### 第⼀章：Nety介绍

本章介绍

Netty介绍 为什么要使⽤non-blocking IO(NIO) 阻塞IO(blocking IO)和⾮阻塞IO(non-blocking IO)对⽐ Java NIO的问题和在Netty中的解决⽅案

Netty是基于Java NIO的⽹络应⽤框架，如果你是Java⽹络⽅⾯的新⼿，那么本章将是你学习 Java⽹络应⽤的开始；对于有经验的开发者来说，学习本章内容也是很好的复习。如果你熟悉NIO和 NIO2，你可以随时跳过本章直接从第⼆章开始学习。在你的机器上运⾏第⼆章编写的Netty服务器和客 户端。

Netty是⼀个NIO client-server(客户端服务器)框架，使⽤Netty可以快速开发⽹络应⽤，例如 服务器和客户端协议。Netty提供了⼀种新的⽅式来使开发⽹络应⽤程序，这种新的⽅式使得它很 容易使⽤和有很强的扩展性。Netty的内部实现时很复杂的，但是Netty提供了简单易⽤的api从⽹ 络处理代码中解耦业务逻辑。Netty是完全基于NIO实现的，所以整个Netty都是异步的。

⽹络应⽤程序通常需要有较⾼的可扩展性，⽆论是Netty还是其他的基于Java NIO的框架，都 会提供可扩展性的解决⽅案。Netty中⼀个关键组成部分是它的异步特性，本章将讨论同步(阻塞) 和异步(⾮阻塞)的IO来说明为什么使⽤异步代码来解决扩展性问题以及如何使⽤异步。

对于那些初学⽹络变成的读者，本章将帮助您对⽹络应⽤的理解，以及Netty是如何实现他们 的。它说明了如何使⽤基本的Java⽹络API，探讨Java⽹络API的优点和缺点并阐述Netty是如何解 决Java中的问题的，⽐如Eploo错误或内存泄露问题。

在本章的结尾，你会明⽩什么是Netty以及Netty提供了什么，你会理解Java NIO和异步处理 机制，并通过本书的其他章节加强理解。

##### 1.1 为什么使⽤Netty？

David John Wheeler说过“在计算机科学中的所有问题都可以通过间接的⽅法解决。”作为⼀ 个NIO client-server框架，Netty提供了这样的⼀个间接的解决⽅法。Netty提供了⾼层次的抽象来 简化TCP和UDP服务器的编程，但是你仍然可以使⽤底层地API。

(David John Wheeler有⼀句名⾔“计算机科学中的任何问题都可以通过加上⼀层逻辑层来解 决”，这个原则在计算机各技术领域被⼴泛应⽤)

- 1.1.1 不是所有的⽹络框架都是⼀样的


Netty的“quick and easy(⾼性能和简单易⽤)”并不意味着编写的程序的性能和可维护性会受到 影响。从Netty中实现的协议如FTP，SMTP，HTTP，WebSocket，SPDY以及各种⼆进制和基于 ⽂本的传统协议中获得的经验导致Netty的创始⼈要⾮常⼩⼼它的设计。Netty成功的提供了易于开 发，⾼性能和⾼稳定性，以及较强的扩展性。

⾼调的公司和开源项⽬有RedHat, Twitter, Infinispan, and HornetQ, Vert.x, Finagle, Akka, Apache Cassandra, Elasticsearch，以及其他⼈的使⽤有助于Netty的发展，Netty的⼀些特性也是 这些项⽬的需要所致。多年来，Netty变的更⼴为⼈知，它是Java⽹络的⾸选框架，在⼀些开源或 ⾮开源的项⽬中可以体现。并且，Netty在2011年获得Duke's Choice Award(Duke's Choice奖)。

此外，在2011年，Netty的创始⼈Trustion Lee离开RedHat后加⼊Twitter，在这⼀点上，

Netty项⽬奖会成为⼀个独⽴的项⽬组织。RedHat和Twitter都使⽤Netty，所以它毫不奇怪。在撰 写本书时RedHat和Twitter这两家公司是最⼤的贡献者。使⽤Netty的项⽬越来越多，Netty的⽤户 群体和项⽬以及Netty社区都是⾮常活跃的。

- 1.1.2 Netty的功能⾮常丰富


通过本书可以学习Netty丰富的功能。下图是Netty框架的组成

![image 1](assets/imageFile1.png)

Netty除了提供传输和协议，在其他各领域都有发展。Netty为开发者提供了⼀套完整的⼯ 具，看下⾯表格：

<table>
  <tr>
    <th>Development Area</th>
    <th>Netty Features</th>
  </tr>
  <tr>
    <td>Design(设计)</td>
    <td>各种传输类型，阻塞和⾮阻塞套接字统⼀的API 使⽤灵活 简单但功能强⼤的线程模型 ⽆连接的DatagramSocket⽀持 链逻辑，易于重⽤<br><br></td>
  </tr>
  <tr>
    <td>Ease of Use(易于使⽤)</td>
    <td>提供⼤量的⽂档和例⼦ 除了依赖jdk1.6+，没有额外的依赖关系。某些功 能依赖jdk1.7+，其他特性可能有相关依赖，但都 是可选的。<br><br></td>
  </tr>
  <tr>
    <td>Performance(性能)</td>
    <td>⽐Java APIS更好的吞吐量和更低的延迟 因为线程池和重⽤所有消耗较少的资源 尽量减少不必要的内存拷贝<br><br></td>
  </tr>
  <tr>
    <td>Robustness(鲁棒性)</td>
    <td>鲁棒性，可以理解为健壮性 链接快或慢或超载不会导致更多的 OutOfMemoryError 在⾼速的⽹络程序中不会有不公平的read/write<br><br></td>
  </tr>
  <tr>
    <td>Security(安全性)</td>
    <td>完整的SSL/TLS和StartTLS⽀持 可以在如Applet或OSGI这些受限制的环境中运⾏<br><br></td>
  </tr>
  <tr>
    <td>Community(社区)</td>
    <td>版本发布频繁 社区活跃<br><br></td>
  </tr>
</table>


除了列出的功能外，Netty为Java NIO中的bug和限制也提供了解决⽅案。我们需要深刻理解 Netty的功能以及它的异步处理机制和它的架构。NIO和Netty都⼤量使⽤了异步代码，并且封装的 很好，我们⽆需了解底层的事件选择机制。下⾯我们来看看为什么需要异步APIS。

##### 1.2 异步设计

整个Netty的API都是异步的，异步处理不是⼀个新的机制，这个机制出来已经有⼀些时间 了。对⽹络应⽤来说，IO⼀般是性能的瓶颈，使⽤异步IO可以较⼤程度上提⾼程序性能，因为异 步变的越来越重要。但是它是如何⼯作的呢？以及有哪些不同的模式可⽤呢？

异步处理提倡更有效的使⽤资源，它允许你创建⼀个任务，当有事件发⽣时将获得通知并等 待事件完成。这样就不会阻塞，不管事件完成与否都会及时返回，资源利⽤率更⾼，程序可以利 ⽤剩余的资源做⼀些其他的事情。

本节将说明⼀起⼯作或实现异步API的两个最常⽤的⽅法，并讨论这些技术之间的差异。

###### 1.2.1 Callbacks(回调)

回调⼀般是异步处理的⼀种技术。⼀个回调是被传递到并且执⾏完该⽅法。你可能认为这种 模式来⾃JavaScript，在Javascript中，回调是它的核⼼。下⾯的代码显⽰了如何使⽤这种技术来 获取数据。下⾯代码是⼀个简单的回调

[java] view plaincopy

![image 2](assets/imageFile2.png)

publicclas Worker {

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


publicvoid doWork() { Fetcher fetcher =new MyFetcher(new Data(1,0); fetcher.fetchData(new FetcherCalback() {

@Override publicvoid onError(Throwable cause) {

System.out.println("An error acour: " + cause.getMesage(); }

@Override publicvoid onData(Data data) {

System.out.println("Data received: " + data); }

}); }

publicstaticvoid main(String[] args) { Worker w =new Worker(); w.doWork();

}

}

[java] view plaincopy

![image 3](assets/imageFile3.png)

- 1.
- 2.
- 3.
- 4.
- 5.


package nety.in.action;

publicinterface Fetcher {

void fetchData(FetcherCalback calback); }

[java] view plaincopy

![image 4](assets/imageFile4.png)

publicclas MyFetcherimplements Fetcher {

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


final Data data;

public MyFetcher(Data data){

this.data = data; }

@Override publicvoid fetchData(FetcherCalback calback) {

try {

calback.onData(data); }catch (Exception e) {

calback.onError(e); }

}

}

[java] view plaincopy

![image 5](assets/imageFile5.png)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


package nety.in.action;

publicinterface FetcherCalback { void onData(Data data)throws Exception; void onError(Throwable cause);

}

[java] view plaincopy

![image 6](assets/imageFile6.png)

publicclas Data {

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


privateint n; privateint m;

public Data(int n,int m){ this.n = n; this.m = m;

}

@Override public String toString() {

int r = n/m; return n +"/" + m +" = " + r;

} }

上⾯的例⼦只是⼀个简单的模拟回调，要明⽩其所表达的含义。Fetcher.fetchData()⽅法需传 递⼀个FetcherCallback类型的参数，当获得数据或发⽣错误时被回调。对于每种情况都提供了同 意的⽅法：

FetcherCallback.onData()，将接收数据时被调⽤ FetcherCallback.onError()，发⽣错误时被调⽤

因为可以将这些⽅法的执⾏从"caller"线程移动到其他的线程执⾏；但也不会保证 FetcherCallback的每个⽅法都会被执⾏。回调过程有个问题就是当你使⽤链式调⽤

很多不同的⽅法会导致线性代码；有些⼈认为这种链式调⽤⽅法会导致代码难以阅读，但是我认 为这是⼀种风格和习惯问题。例如，基于Javascript的Node.js越来越受欢迎，它使⽤了⼤量的回 调，许多⼈都认为它的这种⽅式利于阅读和编写。

###### 1.2.2 Futures

第⼆种技术是使⽤Futures。Futures是⼀个抽象的概念，它表⽰⼀个值，该值可能在某⼀点 变得可⽤。⼀个Future要么获得计算完的结果，要么获得计算失败后的异常。Java在

java.util.concurrent包中附带了Future接⼜，它使⽤Executor异步执⾏。例如下⾯的代码，每传递 ⼀个Runnable对象到ExecutorService.submit()⽅法就会得到⼀个回调的Future，你能使⽤它检测 是否执⾏完成。

[java] view plaincopy

![image 7](assets/imageFile7.png)

import java.util.concurrent.Calable; import java.util.concurrent.ExecutorService; import java.util.concurrent.Executors; import java.util.concurrent.Future;

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
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.


publicclas FutureExample {

publicstaticvoid main(String[] args)throws Exception { ExecutorService executor = Executors.newCachedThreadPol(); Runable task1 =new Runable() {

@Override publicvoid run() {

/do something

System.out.println("i am task1 ."); }

}; Calable<Integer> task2 =new Calable<Integer>() {

@Override public Integer cal()throws Exception {

/do something

returnnew Integer(10); }

}; Future<?> f1 = executor.submit(task1); Future<Integer> f2 = executor.submit(task2);

- System.out.println("task1 is completed? " + f1.isDone();

- System.out.println("task2 is completed? " + f2.isDone(); /waiting task1 completed


- while(f1.isDone(){ System.out.println("task1 completed."); break;

} /waiting task2 completed

- while(f2.isDone(){ System.out.println("return value by task2: " + f2.get(); break;


} }

}

有时候使⽤Future感觉很丑陋，因为你需要间隔检查Future是否已完成，⽽使⽤回调会直接 收到返回通知。看完这两个常⽤的异步执⾏技术后，你可能想知道使⽤哪个最好？这⾥没有明确 的答案。事实上，Netty两者都使⽤，提供两全其美的⽅案。下⼀节将在JVM上⾸先使⽤阻塞，然 后再使⽤NIO和NIO2写⼀个⽹络程序。这些是本书后续章节必不可少的基础知识，如果你熟悉 Java⽹络AIPs，你可以快速翻阅即可。

#### 1.3 Java中的Blocking和non-blocking IO对⽐

本节主要讲解Java的IO和NIO的差异，这⾥不过多赘述，⽹络已有很多相关⽂章。

##### 1.4 NIO的问题和Netty中是如何解决这些问题的

本节中将介绍Netty是如何解决NIO中的⼀些问题和限制。Java的NIO相对⽼的IO APIs有着⾮ 常⼤的进步，但是使⽤NIO是受限制的。这些问题往往是设计的问题，有些是缺陷知道的。

- 1.4.1 跨平台和兼容性问题

NIO是⼀个⽐较底层的APIs，它依赖于操作系统的IO APIs。Java实现了统⼀的接⼜来操作 IO，其在所有操作系统中的⼯作⾏为是⼀样的，这是很伟⼤的。使⽤NIO会经常发现代码在Linux 上正常运⾏，但在Windows上就会出现问题。我建议你如果使⽤NIO编写程序，就应该在所有的操 作系统上进⾏测试来⽀持，使程序可以在任何操作系统上正常运⾏；即使在所有的Linux系统上都 测试通过了，也要在其他的操作系统上进⾏测试；你若不验证，以后就可能会出问题。

NIO2看起来很理想，但是NIO2只⽀持Jdk1.7+，若你的程序在Java1.6上运⾏，则⽆法使⽤ NIO2。另外，Java7的NIO2中没有提供DatagramSocket的⽀持，所以NIO2只⽀持TCP程序，不 ⽀持UDP程序。

Netty提供⼀个统⼀的接⼜，同⼀语义⽆论在Java6还是Java7的环境下都是可以运⾏的，开 发者⽆需关⼼底层APIs就可以轻松实现相关功能。

- 1.4.2 扩展ByteBuffer

ByteBuffer是⼀个数据容器，但是可惜的是JDK没有开发ByteBuffer实现的源码；ByteBuffer

允许包装⼀个byte[]来获得⼀个实例，如果你希望尽量减少内存拷贝，那么这种⽅式是⾮常有⽤ 的。若果你想将ByteBuffer重新实现，那么不要浪费你的时间了，ByteBuffer的构造函数是私有 的，所以它不能被扩展。Netty提供了⾃⼰的ByteBuffer实现，Netty通过⼀些简单的APIs对

ByteBuffer进⾏构造、使⽤和操作，以此来解决NIO中的⼀些限制。

- 1.4.3 NIO对缓冲区的聚合和分散操作可能会操作内存泄露


很多Channel的实现⽀持Gather和Scatter。这个功能允许从从多个ByteBuffer中读⼊或写⼊到 过个ByteBuffer，这样做可以提供性能。操作系统底层知道如何处理这些被写⼊/读出，并且能以 最有效的⽅式处理。如果要分割的数据再多个不同的ByteBuffer中，使⽤Gather/Scatter是⽐较好 的⽅式。

例如，你可能希望header在⼀个ByteBuffer中，⽽body在另外的ByteBuffer中；

下图显⽰的是Scatter(分散)，将ScatteringByteBuffer中的数据分散读取到多个ByteBuffer中：

![image 8](assets/imageFile8.png)

下图显⽰的是Gather(聚合)，将多个ByteBuffer的数据写⼊到GatheringByteChannel：

![image 9](assets/imageFile9.png)

可惜Gather/Scatter功能会导致内存泄露，知道Java7才解决内存泄露问题。使⽤这个功能必 须⼩⼼编码和Java版本。

###### 1.4.4 Squashing the famous epoll bug

压碎著名的epoll缺陷。

On Linux-like OSs the selector makes use of the epoll- IO event notification facility. This is a high-performance technique in which the OS works asynchronously with the networking stack.Unfortunately, even today the "famous" epoll- bug can lead to an "invalid" state in

the selector, resulting in 100% CPU-usage and spinning. The only way to recover is to recycle the old selector and transfer the previously registered Channel instances to the newly

created Selector.

Linux-like OSs的选择器使⽤的是epoll-IO事件通知⼯具。这是⼀个在操作系统以异步⽅式⼯ 作的⽹络stack.Unfortunately，即使是现在，著名的epoll-bug也可能会导致⽆效的状态的选择和 100%的CPU利⽤率。要解决epoll-bug的唯⼀⽅法是回收旧的选择器，将先前注册的通道实例转移 到新创建的选择器上。

What happens here is that the Selector.select() method stops to block and returns immediately-even if there are no selected SelectionKeys present. This is against the contract, which is in the Javadocs of the Selector.select() method:Selector.select()

must not unblock if nothing is selected.

这⾥发⽣的是，不管有没有已选择的SelectionKey，Selector.select()⽅法总是不会阻塞并且 会⽴刻返回。这违反了Javadoc中对Selector.select()⽅法的描述，Javadoc中的描述： Selector.select() must not unblock if nothing is selected. (Selector.select()⽅法若未选中任何事件 将会阻塞。)

The range of solutions to this epoll- problem is limited, but Netty attempts to automatically detect and prevent it. The following listing is an example of the epoll- bug.

NIO中对epoll问题的解决⽅案是有限制的，Netty提供了更好的解决⽅案。下⾯是epoll-bug的 ⼀个例⼦：

...

while (true) {

int selected = selector.select();

Set<SelectedKeys> readyKeys = selector.selectedKeys();

Iterator iterator = readyKeys.iterator();

while (iterator.hasNext()) {

...

...

}

}

...

The effect of this code is that the while loop eats CPU:

这段代码的作⽤是while循环消耗CPU：

...

while (true) {

}

...

The value will never be false, and the code keeps your CPU spinning and eats resources. This can have some undesirable side effects as it can consume all of your CPU, preventing any other CPU-bound work.

该值将永远是假的，代码将持续消耗你的CPU资源。这会有⼀些副作⽤，因为CPU消耗完了 就⽆法再去做其他任何的⼯作。

These are only a few of the possible problems you may see while using nonblocking IO. Unfortunately, even after years of development in this area, issues still need to be resolved; thankfully, Netty addresses them for you.

这些仅仅是在使⽤NIO时可能会出现的⼀些问题。不幸的是，虽然在这个领域发展了多年， 问题依然存在；幸运的是，Netty给了你解决⽅案。

#### 1.5 Summary

This chapter provided an overview of Netty's features, design and benefits. I discussed the difference between blocking and non-blocking processing to give you a fundamental understanding of the reasons to use a non-blocking framework. You learned how to use the JDK API to write network code in both blocking and non-blocking modes. This

included the new non-blocking API, which comes with JDK 7. After seeing the NIO APIs in action, it was also important to understand some of the known issues that you may run into. In fact, this is why so many people use Netty: to take care of workarounds and other JVM quirks. In the next chapter, you'll learn the basics of the Netty API and programming model, and, finally, use Netty to write some useful code.

### 第⼆章：第⼀个Nety程序

本章介绍

获取Netty4最新版本 设置运⾏环境来构建和运⾏netty程序 创建⼀个基于Netty的服务器和客户端 拦截和处理异常 编写和运⾏Netty服务器和客户端

本章将简单介绍Netty的核⼼概念，这个狠⼼概念就是学习Netty是如何拦截和处理异常，对于刚 开始学习netty的读者，利⽤netty的异常拦截机制来调试程序问题很有帮助。本章还会介绍其他⼀些核 ⼼概念，如服务器和客户端的启动以及分离通道的处理程序。本章学习⼀些基础以便后⾯章节的深⼊ 学习。本章中将编写⼀个基于netty的服务器和客户端来互相通信，我们⾸先来设置netty的开发环境。

##### 2.1 设置开发环境

设置开发环境的步骤如下：

安装JDK7，下载地址http://www.oracle.com/technetwork/java/javase/archive-139210.html 下载netty包，下载地址http://netty.io/ 安装Eclipse

《Netty In Action》中描述的⽐较多，没啥⽤，这⾥就不多说了。本系列博客将使⽤Netty4， 需要JDK1.7+

##### 2.2 Netty客户端和服务器概述

本节将引导你构建⼀个完整的Netty服务器和客户端。⼀般情况下，你可能只关⼼编写服务 器，如⼀个http服务器的客户端是浏览器。然后在这个例⼦中，你若同时实现了服务器和客户端， 你将会对他们的原理更加清晰。

⼀个Netty程序的⼯作图如下

![image 10](assets/imageFile10.png)

客户端连接到服务器 建⽴连接后，发送或接收数据

- 2.
- 3.


服务器处理所有的客户端连接

从上图中可以看出，服务器会写数据到客户端并且处理多个客户端的并发连接。从理论上来 说，限制程序性能的因素只有系统资源和JVM。为了⽅便理解，这⾥举了个⽣活例⼦，在⼭⾕或 ⾼⼭上⼤声喊，你会听见回声，回声是⼭返回的；在这个例⼦中，你是客户端，⼭是服务器。喊 的⾏为就类似于⼀个Netty客户端将数据发送到服务器，听到回声就类似于服务器将相同的数据返 回给你，你离开⼭⾕就断开了连接，但是你可以返回进⾏重连服务器并且可以发送更多的数据。

虽然将相同的数据返回给客户端不是⼀个典型的例⼦，但是客户端和服务器之间数据的来来 回回的传输和这个例⼦是⼀样的。本章的例⼦会证明这⼀点，它们会越来越复杂。

接下来的⼏节将带着你完成基于Netty的客户端和服务器的应答程序。

##### 2.3 编写⼀个应答服务器

写⼀个Netty服务器主要由两部分组成：

配置服务器功能，如线程、端⼜ 实现服务器处理程序，它包含业务逻辑，决定当有⼀个请求连接或接收数据时该做什么

- 2.3.1 启动服务器


通过创建ServerBootstrap对象来启动服务器，然后配置这个对象的相关选项，如端⼜、线程 模式、事件循环，并且添加逻辑处理程序⽤来处理业务逻辑(下⾯是个简单的应答服务器例⼦)

[java] view plaincopy

![image 11](assets/imageFile11.png)

import io.nety.botstrap.ServerBotstrap; import io.nety.chanel.Chanel; import io.nety.chanel.ChanelFuture; import io.nety.chanel.ChanelInitializer; import io.nety.chanel.EventLopGroup; import io.nety.chanel.nio.NioEventLopGroup; import io.nety.chanel.socket.nio.NioServerSocketChanel;

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


publicclas EchoServer {

privatefinalint port;

public EchoServer(int port) {

this.port = port; }

publicvoid start()throws Exception { EventLopGroup group =new NioEventLopGroup(); try {

/create ServerBotstrap instance

ServerBotstrap b =new ServerBotstrap(); /Specifies NIO transport, local socket adres /Ads handler to chanel pipeline

b.group(group).chanel(NioServerSocketChanel.clas).localAdres(port)

.childHandler(new ChanelInitializer<Chanel>() { @Override protectedvoid initChanel(Chanel ch)throws Exception {

ch.pipeline().adLast(new EchoServerHandler(); }

});

/Binds server, waits for server to close, and releases resources ChanelFuture f = b.bind().sync(); System.out.println(EchoServer.clas.getName() +"started and listen on “" + f.chanel().localAdres(); f.chanel().closeFuture().sync();

}finaly {

group.shutdownGracefuly().sync(); }

}

publicstaticvoid main(String[] args)throws Exception {

new EchoServer(6535).start(); }

}

从上⾯这个简单的服务器例⼦可以看出，启动服务器应先创建⼀个ServerBootstrap对象，因 为使⽤NIO，所以指定NioEventLoopGroup来接受和处理新连接，指定通道类型为 NioServerSocketChannel，设置InetSocketAddress让服务器监听某个端⼜已等待客户端连接。

接下来，调⽤childHandler放来指定连接后调⽤的ChannelHandler，这个⽅法传 ChannelInitializer类型的参数，ChannelInitializer是个抽象类，所以需要实现initChannel⽅法，这 个⽅法就是⽤来设置ChannelHandler。

最后绑定服务器等待直到绑定完成，调⽤sync()⽅法会阻塞直到服务器完成绑定，然后服务 器等待通道关闭，因为使⽤sync()，所以关闭操作也会被阻塞。现在你可以关闭EventLoopGroup 和释放所有资源，包括创建的线程。

这个例⼦中使⽤NIO，因为它是⽬前最常⽤的传输⽅式，你可能会使⽤NIO很长时间，但是你 可以选择不同的传输实现。例如，这个例⼦使⽤OIO⽅式传输，你需要指定 OioServerSocketChannel。Netty框架中实现了多重传输⽅式，将再后⾯讲述。

本⼩节重点内容：

创建ServerBootstrap实例来引导绑定和启动服务器 创建NioEventLoopGroup对象来处理事件，如接受新连接、接收数据、写数据等等 指定InetSocketAddress，服务器监听此端⼜ 设置childHandler执⾏所有的连接请求 都设置完毕了，最后调⽤ServerBootstrap.bind() ⽅法来绑定服务器

- 2.3.2 实现服务器业务逻辑


Netty使⽤futures和回调概念，它的设计允许你处理不同的事件类型，更详细的介绍将再后⾯ 章节讲述，但是我们可以接收数据。你的channel handler必须继承 ChannelInboundHandlerAdapter并且重写channelRead⽅法，这个⽅法在任何时候都会被调⽤来 接收数据，在这个例⼦中接收的是字节。

下⾯是handler的实现，其实现的功能是将客户端发给服务器的数据返回给客户端：

[java] view plaincopy

![image 12](assets/imageFile12.png)

import io.nety.bufer.Unpoled; import io.nety.chanel.ChanelFutureListener; import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.ChanelInboundHandlerAdapter;

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


publicclas EchoServerHandlerextends ChanelInboundHandlerAdapter {

@Override publicvoid chanelRead(ChanelHandlerContext ctx, Object msg)throws Exception {

System.out.println("Server received: " + msg); ctx.write(msg);

}

@Override publicvoid chanelReadComplete(ChanelHandlerContext ctx)throws Exception {

ctx.writeAndFlush(Unpoled.EMPTY_BUFER).adListener(ChanelFutureListener.CLOSE); }

@Override publicvoid exceptionCaught(ChanelHandlerContext ctx, Throwable cause)throws Exception {

cause.printStackTrace(); ctx.close();

}

}

Netty使⽤多个Channel Handler来达到对事件处理的分离，因为可以很容的添加、更新、删 除业务逻辑处理handler。Handler很简单，它的每个⽅法都可以被重写，它的所有的⽅法中只有 channelRead⽅法是必须要重写的。

- 2.3.3 捕获异常

重写ChannelHandler的exceptionCaught⽅法可以捕获服务器的异常，⽐如客户端连接服务 器后强制关闭，服务器会抛出"客户端主机强制关闭错误"，通过重写exceptionCaught⽅法就可以 处理异常，⽐如发⽣异常后关闭ChannelHandlerContext。

- 2.4 编写应答程序的客户端


服务器写好了，现在来写⼀个客户端连接服务器。应答程序的客户端包括以下⼏步：

连接服务器 写数据到服务器 等待接受服务器返回相同的数据 关闭连接

- 2.4.1 引导客户端


引导客户端启动和引导服务器很类似，客户端需同时指定host和port来告诉客户端连接哪个服 务器。看下⾯代码：

[java] view plaincopy

![image 13](assets/imageFile13.png)

import io.nety.botstrap.Botstrap; import io.nety.chanel.ChanelFuture; import io.nety.chanel.ChanelInitializer; import io.nety.chanel.EventLopGroup; import io.nety.chanel.nio.NioEventLopGroup; import io.nety.chanel.socket.SocketChanel; import io.nety.chanel.socket.nio.NioSocketChanel; import io.nety.example.echo.EchoClientHandler;

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
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.


import java.net.InetSocketAdres;

publicclas EchoClient {

privatefinal String host; privatefinalint port;

public EchoClient(String host,int port) { this.host = host; this.port = port;

}

publicvoid start()throws Exception { EventLopGroup group =new NioEventLopGroup(); try {

Botstrap b =new Botstrap(); b.group(group).chanel(NioSocketChanel.clas).remoteAdres(new InetSocketAdres(host, port)

.handler(new ChanelInitializer<SocketChanel>() { @Override protectedvoid initChanel(SocketChanel ch)throws Exception {

ch.pipeline().adLast(new EchoClientHandler(); }

}); ChanelFuture f = b.conect().sync(); f.chanel().closeFuture().sync();

}finaly {

group.shutdownGracefuly().sync(); }

}

publicstaticvoid main(String[] args)throws Exception {

new EchoClient("localhost",2 0).start(); }

}

创建启动⼀个客户端包含下⾯⼏步：

创建Bootstrap对象⽤来引导启动客户端 创建EventLoopGroup对象并设置到Bootstrap中，EventLoopGroup可以理解为是⼀个线程池， 这个线程池⽤来处理连接、接受数据、发送数据 创建InetSocketAddress并设置到Bootstrap中，InetSocketAddress是指定连接的服务器地址 添加⼀个ChannelHandler，客户端成功连接服务器后就会被执⾏ 调⽤Bootstrap.connect()来连接服务器 最后关闭EventLoopGroup来释放资源

- 2.4.2 实现客户端的业务逻辑


客户端的业务逻辑的实现依然很简单，更复杂的⽤法将在后⾯章节详细介绍。和编写服务器 的ChannelHandler⼀样，在这⾥将⾃定义⼀个继承SimpleChannelInboundHandler的 ChannelHandler来处理业务；通过重写⽗类的三个⽅法来处理感兴趣的事件：

channelActive()：客户端连接服务器后被调⽤ channelRead0()：从服务器接收到数据后调⽤ exceptionCaught()：发⽣异常时被调⽤

实现代码如下

[java] view plaincopy

![image 14](assets/imageFile14.png)

import io.nety.bufer.ByteBuf; import io.nety.bufer.ByteBufUtil; import io.nety.bufer.Unpoled; import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.SimpleChanelInboundHandler; import io.nety.util.CharsetUtil;

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


publicclas EchoClientHandlerextends SimpleChanelInboundHandler<ByteBuf> {

@Override publicvoid chanelActive(ChanelHandlerContext ctx)throws Exception {

ctx.write(Unpoled.copiedBufer("Nety rocks!",CharsetUtil.UTF_8); }

@Override protectedvoid chanelRead0(ChanelHandlerContext ctx, ByteBuf msg)throws Exception {

System.out.println("Client received: " + ByteBufUtil.hexDump(msg.readBytes(msg.readableBytes( ); }

@Override publicvoid exceptionCaught(ChanelHandlerContext ctx, Throwable cause)throws Exception {

cause.printStackTrace(); ctx.close();

} }

可能你会问为什么在这⾥使⽤的是SimpleChannelInboundHandler⽽不使⽤

ChannelInboundHandlerAdapter？主要原因是ChannelInboundHandlerAdapter在处理完消息后需 要负责释放资源。在这⾥将调⽤ByteBuf.release()来释放资源。SimpleChannelInboundHandler会 在完成channelRead0后释放消息，这是通过Netty处理所有消息的ChannelHandler实现了

ReferenceCounted接⼜达到的。

为什么在服务器中不使⽤SimpleChannelInboundHandler呢？因为服务器要返回相同的消息 给客户端，在服务器执⾏完成写操作之前不能释放调⽤读取到的消息，因为写操作是异步的，⼀ 旦写操作完成后，Netty中会⾃动释放消息。

客户端的编写完了，下⾯让我们来测试⼀下

##### 2.5 编译和运⾏echo(应答)程序客户端和服务器

###### 注意，netty4需要jdk1.7+。

本⼈测试，可以正常运⾏。

##### 2.6 总结

本章介绍了如何编写⼀个简单的基于Netty的服务器和客户端并进⾏通信发送数据。介绍了如 何创建服务器和客户端以及Netty的异常处理机制。

### 第三章：Nety核⼼概念

在这⼀章我们将讨论Netty的10个核⼼类，清楚了解他们的结构对使⽤Netty很有⽤。可能有⼀些不 会再⼯作中⽤到，但是也有⼀些很常⽤也很核⼼，你会遇到。

Bootstrap or ServerBootstrap EventLoop EventLoopGroup ChannelPipeline Channel Future or ChannelFuture ChannelInitializer ChannelHandler

本节的⽬的就是介绍以上这些概念，帮助你了解它们的⽤法。

#### 3.1 Netty Crash Course

在我们开始之前，如果你了解Netty程序的⼀般结构和⼤致⽤法(客户端和服务器都有⼀个类 似的结构)会更好。

⼀个Netty程序开始于Bootstrap类，Bootstrap类是Netty提供的⼀个可以通过简单配置来设置 或"引导"程序的⼀个很重要的类。Netty中设计了Handlers来处理特定的"event"和设置Netty中的事 件，从⽽来处理多个协议和数据。事件可以描述成⼀个⾮常通⽤的⽅法，因为你可以⾃定义⼀个 handler,⽤来将Object转成byte[]或将byte[]转成Object；也可以定义个handler处理抛出的异常。

你会经常编写⼀个实现ChannelInboundHandler的类，ChannelInboundHandler是⽤来接收 消息，当有消息过来时，你可以决定如何处理。当程序需要返回消息时可以在 ChannelInboundHandler⾥write/flush数据。可以认为应⽤程序的业务逻辑都是在 ChannelInboundHandler中来处理的，业务罗的⽣命周期在ChannelInboundHandler中。

Netty连接客户端端或绑定服务器需要知道如何发送或接收消息，这是通过不同类型的 handlers来做的，多个Handlers是怎么配置的？Netty提供了ChannelInitializer类⽤来配置 Handlers。ChannelInitializer是通过ChannelPipeline来添加ChannelHandler的，如发送和接收消 息，这些Handlers将确定发的是什么消息。ChannelInitializer⾃⾝也是⼀个ChannelHandler，在 添加完其他的handlers之后会⾃动从ChannelPipeline中删除⾃⼰。

所有的Netty程序都是基于ChannelPipeline。ChannelPipeline和EventLoop和 EventLoopGroup密切相关，因为它们三个都和事件处理相关，所以这就是为什么它们处理IO的⼯ 作由EventLoop管理的原因。

Netty中所有的IO操作都是异步执⾏的，例如你连接⼀个主机默认是异步完成的；写⼊/发送 消息也是同样是异步。也就是说操作不会直接执⾏，⽽是会等⼀会执⾏，因为你不知道返回的操 作结果是成功还是失败，但是需要有检查是否成功的⽅法或者是注册监听来通知；Netty使⽤ Futures和ChannelFutures来达到这种⽬的。Future注册⼀个监听，当操作成功或失败时会通知。 ChannelFuture封装的是⼀个操作的相关信息，操作被执⾏时会⽴刻返回ChannelFuture。

#### 3.2 Channels,Events and Input/Output(IO)

Netty是⼀个⾮阻塞、事件驱动的⽹络框架。Netty实际上是使⽤多线程处理IO事件，对于熟 悉多线程编程的读者可能会需要同步代码。这样的⽅式不好，因为同步会影响程序的性能，Netty 的设计保证程序处理事件不会有同步。

下图显⽰⼀个EventLoopGroup和⼀个Channel关联⼀个单⼀的EventLoop，Netty中的 EventLoopGroup包含⼀个或多个EventLoop，⽽EventLoop就是⼀个Channel执⾏实际⼯作的线 程。EventLoop总是绑定⼀个单⼀的线程，在其⽣命周期内不会改变。

![image 15](assets/imageFile15.png)

当注册⼀个Channel后，Netty将这个Channel绑定到⼀个EventLoop，在Channel的⽣命周期内总 是被绑定到⼀个EventLoop。在Netty IO操作中，你的程序不需要同步，因为⼀个指定通道的所有 IO始终由同⼀个线程来执⾏。

为了帮助理解，下图显⽰了EventLoop和EventLoopGroup的关系：

![image 16](assets/imageFile16.png)

EventLoop和EventLoopGroup的关联不是直观的，因为我们说过EventLoopGroup包含⼀个或多 个EventLoop，但是上⾯的图显⽰EventLoop是⼀个EventLoopGroup，这意味着你可以只使⽤⼀ 个特定的EventLoop。

##### 3.3 什么是Bootstrap?为什么使⽤它？

“引导”是Netty中配置程序的过程，当你需要连接客户端或服务器绑定指定端⼜时需要使⽤ bootstrap。如前⾯所述，“引导”有两种类型，⼀种是⽤于客户端的Bootstrap(也适⽤于 DatagramChannel)，⼀种是⽤于服务端的ServerBootstrap。不管程序使⽤哪种协议，⽆论是创建 ⼀个客户端还是服务器都需要使⽤“引导”。

两种bootsstraps之间有⼀些相似之处，其实他们有很多相似之处，也有⼀些不同。Bootstrap 和ServerBootstrap之间的差异：

Bootstrap⽤来连接远程主机，有1个EventLoopGroup ServerBootstrap⽤来绑定本地端⼜，有2个EventLoopGroup

事件组(Groups)，传输(transports)和处理程序(handlers)分别在本章后⾯讲述，我们在这⾥只 讨论两种"引导"的差异(Bootstrap和ServerBootstrap)。第⼀个差异很明显，“ServerBootstrap”监听 在服务器监听⼀个端⼜轮询客户端的“Bootstrap”或DatagramChannel是否连接服务器。通常需要 调⽤“Bootstrap”类的connect()⽅法，但是也可以先调⽤bind()再调⽤connect()进⾏连接，之后使⽤ 的Channel包含在bind()返回的ChannelFuture中。

第⼆个差别也许是最重要的。客户端bootstraps/applications使⽤⼀个单例 EventLoopGroup，⽽ServerBootstrap使⽤2个EventLoopGroup(实际上使⽤的是相同的实例)，它 可能不是显⽽易见的，但是它是个好的⽅案。⼀个ServerBootstrap可以认为有2个channels组，第 ⼀组包含⼀个单例ServerChannel，代表持有⼀个绑定了本地端⼜的socket；第⼆组包含所有的 Channel，代表服务器已接受了的连接。下图形象的描述了这种情况：

![image 17](assets/imageFile17.png)

上图中，EventLoopGroup A唯⼀的⽬的就是接受连接然后交给EventLoopGroup B。Netty可以使 ⽤两个不同的Group，因为服务器程序需要接受很多客户端连接的情况下，⼀个EventLoopGroup 将是程序性能的瓶颈，因为事件循环忙于处理连接请求，没有多余的资源和空闲来处理业务逻 辑，最后的结果会是很多连接请求超时。若有两EventLoops，即使在⾼负载下，所有的连接也都 会被接受，因为EventLoops接受连接不会和哪些已经连接了的处理共享资源。

EventLoopGroup和EventLoop是什么关系？EventLoopGroup可以包含很多个EventLoop， 每个Channel绑定⼀个EventLoop不会被改变，因为EventLoopGroup包含少量的EventLoop的

Channels，很多Channel会共享同⼀个EventLoop。这意味着在⼀个Channel保持EventLoop繁忙 会禁⽌其他Channel绑定到相同的EventLoop。我们可以理解为EventLoop是⼀个事件循环线程， ⽽EventLoopGroup是⼀个事件循环集合。

如果你决定两次使⽤相同的EventLoopGroup实例配置Netty服务器，下图显⽰了它是如何改 变的：

![image 18](assets/imageFile18.png)

Netty允许处理IO和接受连接使⽤同⼀个EventLoopGroup，这在实际中适⽤于多种应⽤。上图显 ⽰了⼀个EventLoopGroup处理连接请求和IO操作。

下⼀节我们将介绍Netty是如何执⾏IO操作以及在什么时候执⾏。

#### 3.4 Channel Handlers and Data Flow(通道处理和数据流)

本节我们⼀起来看看当你发送或接收数据时发⽣了什么？回想本章开始提到的handler概念。 要明⽩Netty程序wirte或read时发⽣了什么，⾸先要对Handler是什么有⼀定的了解。Handlers⾃ ⾝依赖于ChannelPipeline来决定它们执⾏的顺序，因此不可能通过ChannelPipeline定义处理程序 的某些⽅⾯,反过来不可能定义也不可能通过ChannelHandler定义ChannelPipeline的某些⽅⾯。没 必要说我们必须定义⼀个⾃⼰和其他的规定。本节将介绍ChannelHandler和ChannelPipeline在某 种程度上细微的依赖。

在很多地⽅，Netty的ChannelHandler是你的应⽤程序中处理最多的。即使你没有意思到这⼀ 点，若果你使⽤Netty应⽤将⾄少有⼀个ChannelHandler参与，换句话说，ChannelHandler对很多 事情是关键的。那么ChannelHandler究竟是什么？给ChannelHandler⼀个定义不容易，我们可以 理解为ChannelHandler是⼀段执⾏业务逻辑处理数据的代码，它们来来往往的通过 ChannelPipeline。实际上，ChannelHandler是定义⼀个handler的⽗接⼜， ChannelInboundHandler和ChannelOutboundHandler都实现ChannelHandler接⼜，如下图：

![image 19](assets/imageFile19.png)

上图显⽰的⽐较容易，更重要的是ChannelHandler在数据流⽅⾯的应⽤，在这⾥讨论的例⼦只是 ⼀个简单的例⼦。ChannelHandler被应⽤在许多⽅⾯，在本书中会慢慢学习。

Netty中有两个⽅向的数据流，上图显⽰的⼊站(ChannelInboundHandler)和出站 (ChannelOutboundHandler)之间有⼀个明显的区别：若数据是从⽤户应⽤程序到远程主机则是“出 站(outbound)”，相反若数据时从远程主机到⽤户应⽤程序则是“⼊站(inbound)”。

为了使数据从⼀端到达另⼀端，⼀个或多个ChannelHandler将以某种⽅式操作数据。这些 ChannelHandler会在程序的“引导”阶段被添加ChannelPipeline中，并且被添加的顺序将决定处理 数据的顺序。ChannelPipeline的作⽤我们可以理解为⽤来管理ChannelHandler的⼀个容器，每个 ChannelHandler处理各⾃的数据(例如⼊站数据只能由ChannelInboundHandler处理)，处理完成后 将转换的数据放到ChannelPipeline中交给下⼀个ChannelHandler继续处理，直到最后⼀个 ChannelHandler处理完成。

下图显⽰了ChannelPipeline的处理过程：

![image 20](assets/imageFile20.png)

上图显⽰ChannelInboundHandler和ChannelOutboundHandler都要经过相同的ChannelPipeline。

在ChannelPipeline中，如果消息被读取或有任何其他的⼊站事件，消息将从ChannelPipeline 的头部开始传递给第⼀个ChannelInboundHandler，这个ChannelInboundHandler可以处理该消息 或将消息传递到下⼀个ChannelInboundHandler中，⼀旦在ChannelPipeline中没有剩余的 ChannelInboundHandler后，ChannelPipeline就知道消息已被所有的饿Handler处理完成了。

反过来也是如此，任何出站事件或写⼊将从ChannelPipeline的尾部开始，并传递到最后⼀个 ChannelOutboundHandler。ChannelOutboundHandler的作⽤和ChannelInboundHandler相同， 它可以传递事件消息到下⼀个Handler或者⾃⼰处理消息。不同的是ChannelOutboundHandler是 从ChannelPipeline的尾部开始，⽽ChannelInboundHandler是从ChannelPipeline的头部开始，当 处理完第⼀个ChannelOutboundHandler处理完成后会出发⼀些操作，⽐如⼀个写操作。

⼀个事件能传递到下⼀个ChannelInboundHandler或上⼀个ChannelOutboundHandler，在 ChannelPipeline中通过使⽤ChannelHandlerContext调⽤每⼀个⽅法。Netty提供了抽象的事件基 类称为ChannelInboundHandlerAdapter和ChannelOutboundHandlerAdapter。每个都提供了在 ChannelPipeline中通过调⽤相应的⽅法将事件传递给下⼀个Handler的⽅法的实现。我们能覆盖的 ⽅法就是我们需要做的处理。

可能有读者会奇怪，出站和⼊站的操作不同，能放在同⼀个ChannelPipeline⼯作？Netty的设 计是很巧妙的，⼊站和出站Handler有不同的实现，Netty能跳过⼀个不能处理的操作，所以在出站 事件的情况下，ChannelInboundHandler将被跳过，Netty知道每个handler都必须实现 ChannelInboundHandler或ChannelOutboundHandler。

当⼀个ChannelHandler添加到ChannelPipeline中时获得⼀个ChannelHandlerContext。通常 是安全的获得这个对象的引⽤，但是当⼀个数据报协议如UDP时这是不正确的，这个对象可以在 之后⽤来获取底层通道，因为要⽤它来read/write消息，因此通道会保留。也就是说Netty中发送消 息有两种⽅法：直接写⼊通道或写⼊ChannelHandlerContext对象。这两种⽅法的主要区别如下：

直接写⼊通道导致处理消息从ChannelPipeline的尾部开始 写⼊ChannelHandlerContext对象导致处理消息从ChannelPipeline的下⼀个handler开始

##### 3.5 编码器、解码器和业务逻辑：细看Handlers

如前⾯所说，有很多不同类型的handlers，每个handler的依赖于它们的基类。Netty提供了⼀ 系列的“Adapter”类，这让事情变的很简单。每个handler负责转发时间到ChannelPipeline的下⼀个 handler。在*Adapter类(和⼦类)中是⾃动完成的，因此我们只需要在感兴趣的*Adapter中重写⽅ 法。这些功能可以帮助我们⾮常简单的编码/解码消息。有⼏个适配器(adapter)允许⾃定义 ChannelHandler，⼀般⾃定义ChannelHandler需要继承编码/解码适配器类中的⼀个。Netty有⼀ 下适配器：

ChannelHandlerAdapter ChannelInboundHandlerAdapter ChannelOutboundHandlerAdapter

三个ChannelHandler涨，我们重点看看ecoders,decoders和SimpleChannelInboundHandler<I>， SimpleChannelInboundHandler<I>继承ChannelInboundHandlerAdapter。

###### 3.5.1 Encoders(编码器), decoders(解码器)

发送或接收消息后，Netty必须将消息数据从⼀种形式转化为另⼀种。接收消息后，需要将消 息从字节码转成Java对象(由某种解码器解码)；发送消息前，需要将Java对象转成字节(由某些类 型的编码器进⾏编码)。这种转换⼀般发⽣在⽹络程序中，因为⽹络上只能传输字节数据。

有多种基础类型的编码器和解码器，要使⽤哪种取决于想实现的功能。要弄清楚某种类型的 编解码器，从类名就可以看出，如“ByteToMessageDecoder”、“MessageToByteEncoder”，还有 Google的协议“ProtobufEncoder”和“ProtobufDecoder”。

严格的说其他handlers可以做编码器和适配器，使⽤不同的Adapter classes取决你想要做什 么。如果是解码器则有⼀个ChannelInboundHandlerAdapter或ChannelInboundHandler，所有的 解码器都继承或实现它们。“channelRead”⽅法/事件被覆盖，这个⽅法从⼊站(inbound)通道读取 每个消息。重写的channelRead⽅法将调⽤每个解码器的“decode”⽅法并通过

ChannelHandlerContext.fireChannelRead(Object msg)传递给ChannelPipeline中的下⼀个 ChannelInboundHandler。

类似⼊站消息，当你发送⼀个消息出去(出站)时，除编码器将消息转成字节码外还会转发到 下⼀个ChannelOutboundHandler。

###### 3.5.2 业务逻辑(Domain logic)

也许最常见的是应⽤程序处理接收到消息后进⾏解码，然后供相关业务逻辑模块使⽤。所以 应⽤程序只需要扩展SimpleChannelInboundHandler<I>，也就是我们⾃定义⼀个继承 SimpleChannelInboundHandler<I>的handler类，其中<I>是handler可以处理的消息类型。通过重 写⽗类的⽅法可以获得⼀个ChannelHandlerContext的引⽤，它们接受⼀个 ChannelHandlerContext的参数，你可以在class中当⼀个属性存储。

处理程序关注的主要⽅法是“channelRead0(ChannelHandlerContext ctx, I msg)”，每当Netty

调⽤这个⽅法，对象“I”是消息，这⾥使⽤了Java的泛型设计，程序就能处理I。如何处理消息完全 取决于程序的需要。在处理消息时有⼀点需要注意的，在Netty中事件处理IO⼀般有很多线程，程 序中尽量不要阻塞IO线程，因为阻塞会降低程序的性能。

必须不阻塞IO线程意味着在ChannelHandler中使⽤阻塞操作会有问题。幸运的是Netty提供了 解决⽅案，我们可以在添加ChannelHandler到ChannelPipeline中时指定⼀个 EventExecutorGroup，EventExecutorGroup会获得⼀个EventExecutor，EventExecutor将执⾏ ChannelHandler的所有⽅法。EventExecutor将使⽤不同的线程来执⾏和释放EventLoop。

### 第四章：Transports(传输)

本章内容

Transports(传输) NIO(non-blocking IO,New IO), OIO(Old IO,blocking IO), Local(本地), Embedded(嵌⼊式) Use-case(⽤例) APIs(接⼜)

⽹络应⽤程序⼀个很重要的⼯作是传输数据。传输数据的过程不⼀样取决是使⽤哪种交通⼯具， 但是传输的⽅式是⼀样的：都是以字节码传输。Java开发⽹络程序传输数据的过程和⽅式是被抽象了 的，我们不需要关注底层接⼜，只需要使⽤Java API或其他⽹络框架如Netty就能达到传输数据的⽬ 的。发送数据和接收数据都是字节码。Nothing more,nothing less。

如果你曾经使⽤Java提供的⽹络接⼜⼯作过，你可能已经遇到过想从阻塞传输切换到⾮阻塞

传输的情况，这种切换是⽐较困难的，因为阻塞IO和⾮阻塞IO使⽤的API有很⼤的差异；Netty提 供了上层的传输实现接⼜使得这种情况变得简单。我们可以让所写的代码尽可能通⽤，⽽不会依 赖⼀些实现相关的APIs。当我们想切换传输⽅式的时候不需要花很⼤的精⼒和时间来重构代码。

本章将介绍统⼀的API以及如何使⽤它们，会拿Netty的API和Java的API做⽐较来告诉你为什

么Netty可以更容易的使⽤。本章也提供了⼀些优质的⽤例代码，以便最佳使⽤Netty。使⽤Netty 不需要其他的⽹络框架或⽹络编程经验，若有则只是对理解netty有帮助，但不是必要的。下⾯让 我们来看看真是世界⾥的传输⼯作。

##### 4.1 案例研究：切换传输⽅式

为了让你想象如何运输，我会从⼀个简单的应⽤程序开始，这个应⽤程序什么都不做，只是 接受客户端连接并发送“Hi!”字符串消息到客户端，发送完了就断开连接。我不会详细讲解这个过 程的实现，它只是⼀个例⼦。

###### 4.1.1 使⽤Java的I/O和NIO

我们将不⽤Netty实现这个例⼦，下⾯代码是使⽤阻塞IO实现的例⼦：

###### [java] view plaincopy

import java.io.IOException; import java.io.OutputStream; import java.net.ServerSocket; import java.net.Socket; import java.nio.charset.Charset;

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


/*

- * Blocking networking without Nety

- * @author c.k

*

- */ publicclas PlainOioServer {


publicvoid server(int port)throws Exception {

/bind server to port final ServerSocket socket =new ServerSocket(port); try {

while(true){

/acept conection final Socket clientSocket = socket.acept(); System.out.println("Acepted conection from " + clientSocket);

/create new thread to handle conection

new Thread(new Runable() { @Override publicvoid run() {

OutputStream out; try{

out = clientSocket.getOutputStream();

/write mesage to conected client out.write("Hi!\r\n".getBytes(Charset.forName("UTF-8" ); out.flush();

/close conection once mesage writen and flushed clientSocket.close();

}catch(IOException e){ try {

clientSocket.close(); }catch (IOException e1) { e1.printStackTrace(); }

} }

}).start();/start thread to begin handling }

}catch(Exception e){ e.printStackTrace(); socket.close();

} }

- 50.
- 51.
- 52.


}

上⾯的⽅式很简洁，但是这种阻塞模式在⼤连接数的情况就会有很严重的问题，如客户端连接超 时，服务器响应严重延迟。为了解决这种情况，我们可以使⽤异步⽹络处理所有的并发连接，但 问题在于NIO和OIO的API是完全不同的，所以⼀个⽤OIO开发的⽹络应⽤程序想要使⽤NIO重构代 码⼏乎是重新开发。

下⾯代码是使⽤Java NIO实现的例⼦：

[java] view plaincopy

import java.net.ServerSocket; import java.nio.ByteBufer; import java.nio.chanels.SelectionKey; import java.nio.chanels.Selector; import java.nio.chanels.ServerSocketChanel; import java.nio.chanels.SocketChanel; import java.util.Iterator; /*

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


- * Asynchronous networking without Nety

- * @author c.k

*

- */ publicclas PlainNioServer {


publicvoid server(int port)throws Exception { System.out.println("Listening for conections on port " + port);

/open Selector that handles chanels Selector selector = Selector.open();

/open ServerSocketChanel ServerSocketChanel serverChanel = ServerSocketChanel.open(); /get ServerSocket ServerSocket serverSocket = serverChanel.socket(); /bind server to port serverSocket.bind(new InetSocketAdres(port); /set to non-blocking serverChanel.configureBlocking(false);

/register ServerSocket to selector and specify that it is interested in new acepted clients serverChanel.register(selector, SelectionKey.OP_ACEPT); final ByteBufer msg = ByteBufer.wrap("Hi!\r\n".getBytes(); while (true) {

/Wait for new events that are ready for proces. This wil block until something hapens int n = selector.select(); if (n >0) {

/Obtain al SelectionKey instances that received events Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); while (iter.hasNext() {

SelectionKey key = iter.next(); iter.remove(); try {

/Check if event was because new client ready to get acepted

if (key.isAceptable() { ServerSocketChanel server = (ServerSocketChanel) key.chanel(); SocketChanel client = server.acept(); System.out.println("Acepted conection from " + client); client.configureBlocking(false);

/Acept client and register it to selector

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
- 72.
- 73.


client.register(selector, SelectionKey.OP_WRITE, msg.duplicate(); }

/Check if event was because socket is ready to write data

if (key.isWritable() { SocketChanel client = (SocketChanel) key.chanel(); ByteBufer buf = (ByteBufer) key.atachment();

/write data to conected client while (buf.hasRemaining() {

if (client.write(buf) =0) {

break; }

} client.close();/close client

}

}catch (Exception e) { key.cancel(); key.chanel().close();

} }

} }

}

}

如你所见，即使它们实现的功能是⼀样，但是代码完全不同。下⾯我们将⽤Netty来实现相同的功 能。

###### 4.1.2 Netty中使⽤I/O和NIO

下⾯代码是使⽤Netty作为⽹络框架编写的⼀个阻塞IO例⼦：

[java] view plaincopy

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


import io.nety.botstrap.ServerBotstrap; import io.nety.bufer.ByteBuf; import io.nety.bufer.Unpoled; import io.nety.chanel.Chanel; import io.nety.chanel.ChanelFuture; import io.nety.chanel.ChanelFutureListener; import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.ChanelInboundHandlerAdapter; import io.nety.chanel.ChanelInitializer; import io.nety.chanel.EventLopGroup; import io.nety.chanel.nio.NioEventLopGroup; import io.nety.chanel.socket.oio.OioServerSocketChanel; import io.nety.util.CharsetUtil;

publicclas NetyOioServer {

publicvoid server(int port)throws Exception { final ByteBuf buf = Unpoled.unreleasableBufer(Unpoled.copiedBufer("Hi!\r\n", CharsetUtil.UTF_8);

/事件循环组 EventLopGroup group =new NioEventLopGroup(); try {

/⽤来引导服务器配置 ServerBotstrap b =new ServerBotstrap(); /使⽤OIO阻塞模式 b.group(group).chanel(OioServerSocketChanel.clas).localAdres(new InetSocketAdres(port) /指定ChanelInitializer初始化handlers

.childHandler(new ChanelInitializer<Chanel>() { @Override protectedvoid initChanel(Chanel ch)throws Exception {

53.

}

上⾯代码实现功能⼀样，但结构清晰明了，这只是Netty的优势之⼀。

- 4.1.3 Netty中实现异步⽀持


下⾯代码是使⽤Netty实现异步，可以看出使⽤Netty由OIO切换到NIO是⾮常的⽅便。

[java] view plaincopy

import io.nety.botstrap.ServerBotstrap; import io.nety.bufer.ByteBuf; import io.nety.bufer.Unpoled; import io.nety.chanel.ChanelFuture; import io.nety.chanel.ChanelFutureListener; import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.ChanelInboundHandlerAdapter; import io.nety.chanel.ChanelInitializer; import io.nety.chanel.EventLopGroup; import io.nety.chanel.nio.NioEventLopGroup; import io.nety.chanel.socket.SocketChanel; import io.nety.chanel.socket.nio.NioServerSocketChanel; import io.nety.util.CharsetUtil;

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


import java.net.InetSocketAdres;

publicclas NetyNioServer {

publicvoid server(int port)throws Exception { final ByteBuf buf = Unpoled.unreleasableBufer(Unpoled.copiedBufer("Hi!\r\n", CharsetUtil.UTF_8);

/事件循环组 EventLopGroup group =new NioEventLopGroup(); try {

/⽤来引导服务器配置 ServerBotstrap b =new ServerBotstrap(); /使⽤NIO异步模式 b.group(group).chanel(NioServerSocketChanel.clas).localAdres(new InetSocketAdres(port) /指定ChanelInitializer初始化handlers

.childHandler(new ChanelInitializer<SocketChanel>() { @Override protectedvoid initChanel(SocketChanel ch)throws Exception {

}

因为Netty使⽤相同的API来实现每个传输，它并不关⼼你使⽤什么来实现。Netty通过操作 Channel接⼜和ChannelPipeline、ChannelHandler来实现传输。

#### 4.2 Transport API

传输API的核⼼是Channel接⼜，它⽤于所有出站的操作。Channel接⼜的类层次结构如下

![image 21](assets/imageFile21.png)

如上图所⽰，每个Channel都会分配⼀个ChannelPipeline和ChannelConfig。ChannelConfig负责 设置并存储配置，并允许在运⾏期间更新它们。传输⼀般有特定的配置设置，只作⽤于传输，没 有其他的实现。ChannelPipeline容纳了使⽤的ChannelHandler实例，这些ChannelHandler将处理 通道传递的“⼊站”和“出站”数据。ChannelHandler的实现允许你改变数据状态和传输数据，本书有 章节详细讲解ChannelHandler，ChannelHandler是Netty的重点概念。

现在我们可以使⽤ChannelHandler做下⾯⼀些事情：

传输数据时，将数据从⼀种格式转换到另⼀种格式 异常通知 Channel变为有效或⽆效时获得通知 Channel被注册或从EventLoop中注销时获得通知 通知⽤户特定事件

这些ChannelHandler实例添加到ChannelPipeline中，在ChannelPipeline中按顺序逐个执 ⾏。它类似于⼀个链条，有使⽤过Servlet的读者可能会更容易理解。

ChannelPipeline实现了拦截过滤器模式，这意味着我们连接不同的ChannelHandler来拦截并

处理经过ChannelPipeline的数据或事件。可以把ChannelPipeline想象成UNIX管道，它允许不同 的命令链(ChannelHandler相当于命令)。你还可以在运⾏时根据需要添加ChannelHandler实例到 ChannelPipeline或从ChannelPipeline中删除，这能帮助我们构建⾼度灵活的Netty程序。此外，

访问指定的ChannelPipeline和ChannelConfig，你能在Channel⾃⾝上进⾏操作。Channel提供了 很多⽅法，如下列表：

eventLoop()，返回分配给Channel的EventLoop pipeline()，返回分配给Channel的ChannelPipeline isActive()，返回Channel是否激活，已激活说明与远程连接对等 localAddress()，返回已绑定的本地SocketAddress remoteAddress()，返回已绑定的远程SocketAddress write()，写数据到远程客户端，数据通过ChannelPipeline传输过去

后⾯会越来越熟悉这些⽅法，现在只需要记住我们的操作都是在相同的接⼜上运⾏，Netty的⾼灵 活性让你可以以不同的传输实现进⾏重构。

写数据到远程已连接客户端可以调⽤Channel.write()⽅法，如下代码：

[java] view plaincopy

Chanel chanel = . /Create ByteBuf that holds data to write ByteBuf buf = Unpoled.copiedBufer("your data", CharsetUtil.UTF_8); /Write data ChanelFuture cf = chanel.write(buf); /Ad ChanelFutureListener to get notified after write completes

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


cf.adListener(new ChanelFutureListener() { @Override publicvoid operationComplete(ChanelFuture future) {

/Write operation completes without error if (future.isSuces() {

System.out.println(.Write sucesful.); }else {

/Write operation completed but because of error System.err.println(.Write error.); future.cause().printStacktrace();

} }

});

Channel是线程安全(thread-safe)的，它可以被多个不同的线程安全的操作，在多线程环境 下，所有的⽅法都是安全的。正因为Channel是安全的，我们存储对Channel的引⽤，并在学习的 时候使⽤它写⼊数据到远程已连接的客户端，使⽤多线程也是如此。下⾯的代码是⼀个简单的多 线程例⼦：

[java] view plaincopy

final Chanel chanel = . /Create ByteBuf that holds data to write final ByteBuf buf = Unpoled.copiedBufer("your data",CharsetUtil.UTF_8); /Create Runable which writes data to chanel

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


Runable writer =new Runable() { @Override publicvoid run() {

chanel.write(buf.duplicate(); }

}; /Obtain reference to the Executor which uses threads to execute tasks

Executor executor = Executors.newChachedThreadPol(); / write in one thread /Hand over write task to executor for execution in thread

executor.execute(writer); / write in another thread /Hand over another write task to executor for execution in thread

executor.execute(writer);

此外，这种⽅法保证了写⼊的消息以相同的顺序通过写⼊它们的⽅法。想了解所有⽅法的使⽤可 以参考Netty API⽂档。

##### 4.3 Netty包含的传输实现

Netty⾃带了⼀些传输协议的实现，虽然没有⽀持所有的传输协议，但是其⾃带的已⾜够我们 来使⽤。Netty应⽤程序的传输协议依赖于底层协议，本节我们将学习Netty中的传输协议。

Netty中的传输⽅式有如下⼏种：

NIO，io.netty.channel.socket.nio，基于java.nio.channels的⼯具包，使⽤选择器作为基础的⽅ 法。 OIO，io.netty.channel.socket.oio，基于java.net的⼯具包，使⽤阻塞流。 Local，io.netty.channel.local，⽤来在虚拟机之间本地通信。 Embedded，io.netty.channel.embedded，嵌⼊传输，它允许在没有真正⽹络的运输中使⽤ ChannelHandler，可以⾮常有⽤的来测试ChannelHandler的实现。

###### 4.3.1 NIO - Nonblocking I/O

NIO传输是⽬前最常⽤的⽅式，它通过使⽤选择器提供了完全异步的⽅式操作所有的I/O， NIO从Java 1.4才被提供。NIO中，我们可以注册⼀个通道或获得某个通道的改变的状态，通道状 态有下⾯⼏种改变：

⼀个新的Channel被接受并已准备好 Channel连接完成 Channel中有数据并已准备好读取

Channel发送数据出去

处理完改变的状态后需重新设置他们的状态，⽤⼀个线程来检查是否有已准备好的 Channel，如果有则执⾏相关事件。在这⾥可能只同时⼀个注册的事件⽽忽略其他的。选择器所⽀ 持的操作在SelectionKey中定义，具体如下：

OP_ACCEPT，有新连接时得到通知 OP_CONNECT，连接完成后得到通知 OP_READ，准备好读取数据时得到通知 OP_WRITE，写⼊数据到通道时得到通知

Netty中的NIO传输就是基于这样的模型来接收和发送数据，通过封装将⾃⼰的接⼜提供给⽤ 户使⽤，这完全隐藏了内部实现。如前⾯所说，Netty隐藏内部的实现细节，将抽象出来的API暴 露出来供使⽤，下⾯是处理流程图：

![image 22](assets/imageFile22.png)

NIO在处理过程也会有⼀定的延迟，若连接数不⼤的话，延迟⼀般在毫秒级，但是其吞吐量 依然⽐OIO模式的要⾼。Netty中的NIO传输是“zero-file-copy”,也就是零⽂件复制，这种机制可以 让程序速度更快，更⾼效的从⽂件系统中传输内容，零复制就是我们的应⽤程序不会将发送的数 据先复制到JVM堆栈在进⾏处理，⽽是直接从内核空间操作。接下来我们将讨论OIO传输，它是阻 塞的。

###### 4.3.2 OIO - Old blocking I/O

OIO就是java中提供的Socket接⼜，java最开始只提供了阻塞的Socket，阻塞会导致程序性能 低。下⾯是OIO的处理流程图，若想详细了解，可以参阅其他相关资料。

![image 23](assets/imageFile23.png)

###### 4.3.3 Local - In VM transport

Netty包含了本地传输，这个传输实现使⽤相同的API⽤于虚拟机之间的通信，传输是完全异 步的。每个Channel使⽤唯⼀的SocketAddress，客户端通过使⽤SocketAddress进⾏连接，在服 务器会被注册为长期运⾏，⼀旦通道关闭，它会⾃动注销，客户端⽆法再使⽤它。

连接到本地传输服务器的⾏为与其他的传输实现⼏乎是相同的，需要注意的⼀个重点是只能 在本地的服务器和客户端上使⽤它们。Local未绑定任何Socket，值提供JVM进程之间的通信。

###### 4.3.4 Embedded transport

Netty还包括嵌⼊传输，与之前讲述的其他传输实现⽐较，它是不是⼀个真的传输呢？若不是 ⼀个真的传输，我们⽤它可以做什么呢？Embedded transport允许更容易的使⽤不同的

ChannelHandler之间的交互，这也更容易嵌⼊到其他的ChannelHandler实例并像⼀个辅助类⼀样 使⽤它们。它⼀般⽤来测试特定的ChannelHandler实现，也可以在ChannelHandler中重新使⽤⼀ 些ChannelHandler来进⾏扩展，为了实现这样的⽬的，它⾃带了⼀个具体的Channel实现，即：

EmbeddedChannel。

##### 4.4 每种传输⽅式在什么时候使⽤？

不多加赘述，看下⾯列表：

OIO，在低连接数、需要低延迟时、阻塞时使⽤ NIO，在⾼连接数时使⽤ Local，在同⼀个JVM内通信时使⽤ Embedded，测试ChannelHandler时使⽤

### 第五章：Bufers(缓冲)

本章介绍

ByteBuf ByteBufHolder ByteBufAllocator 使⽤这些接⼜分配缓冲和执⾏操作

每当你需要传输数据时，它必须包含⼀个缓冲区。Java NIO API⾃带的缓冲区类是相当有限的， 没有经过优化，使⽤JDK的ByteBuffer操作更复杂。缓冲区是⼀个重要的组建，它是API的⼀部分。 Netty提供了⼀个强⼤的缓冲区实现⽤于表⽰⼀个字节序列，并帮助你操作原始字节或⾃定义的 POJO。Netty的ByteBuf相当于JDK的ByteBuffer，ByteBuf的作⽤是在Netty中通过Channel传输数据。 它被重新设计以解决JDK的ByteBuffer中的⼀些问题，从⽽使开发⼈员开发⽹络应⽤程序显得更有效 率。本章将讲述Netty中的缓冲区，并了解它为什么⽐JDK⾃带的缓冲区实现更优秀，还会深⼊了解在 Netty中使⽤ByteBuf访问数据以及如何使⽤它。

#### 5.1 Buffer API

Netty的缓冲API有两个接⼜：

ByteBuf ByteBufHolder

Netty使⽤reference-counting(引⽤计数)的时候知道安全释放Buf和其他资源，虽然知道Netty有效 的使⽤引⽤计数，这都是⾃动完成的。这允许Netty使⽤池和其他技巧来加快速度和保持内存利⽤ 率在正常⽔平，你不需要做任何事情来实现这⼀点，但是在开发Netty应⽤程序时，你应该处理数 据尽快释放池资源。

Netty缓冲API提供了⼏个优势：

可以⾃定义缓冲类型 通过⼀个内置的复合缓冲类型实现零拷贝 扩展性好，⽐如StringBuffer 不需要调⽤flip()来切换读/写模式 读取和写⼊索引分开 ⽅法链 引⽤计数 Pooling(池)

##### 5.2 ByteBuf - 字节数据容器

当需要与远程进⾏交互时，需要以字节码发送/接收数据。由于各种原因，⼀个⾼效、⽅便、 易⽤的数据接⼜是必须的，⽽Netty的ByteBuf满⾜这些需求，ByteBuf是⼀个很好的经过优化的数 据容器，我们可以将字节数据有效的添加到ByteBuf中或从ByteBuf中获取数据。ByteBuf有2部 分：⼀个⽤于读，⼀个⽤于写。我们可以按顺序的读取数据，并且可以跳到开始重新读⼀遍。所 有的数据操作，我们只需要做的是调整读取数据索引和再次开始读操作。

###### 5.2.1 ByteBuf如何在⼯作？

写⼊数据到ByteBuf后，写⼊索引是增加的字节数量。开始读字节后，读取索引增加。你可以 读取字节，直到写⼊索引和读取索引处理相同的位置，次数若继续读取，则会抛出 IndexOutOfBoundsException。调⽤ByteBuf的任何⽅法开始读/写都会单独维护读索引和写索引。 ByteBuf的默认最⼤容量限制是Integer.MAX_VALUE，写⼊时若超出这个值将会导致⼀个异常。

ByteBuf类似于⼀个字节数组，最⼤的区别是读和写的索引可以⽤来控制对缓冲区数据的访 问。下图显⽰了⼀个容量为16的ByteBuf：

![image 24](assets/imageFile24.png)

###### 5.2.2 不同类型的ByteBuf

使⽤Netty时会遇到3种不同类型的ByteBuf

###### Heap Buffer(堆缓冲区)

最常⽤的类型是ByteBuf将数据存储在JVM的堆空间，这是通过将数据存储在数组的实现。堆 缓冲区可以快速分配，当不使⽤时也可以快速释放。它还提供了直接访问数组的⽅法，通过 ByteBuf.array()来获取byte[]数据。

访问⾮堆缓冲区ByteBuf的数组会导致UnsupportedOperationException，可以使⽤ ByteBuf.hasArray()来检查是否⽀持访问数组。

###### Direct Buffer(直接缓冲区)

直接缓冲区，在堆之外直接分配内存。直接缓冲区不会占⽤堆空间容量，使⽤时应该考虑到 应⽤程序要使⽤的最⼤内存容量以及如何限制它。直接缓冲区在使⽤Socket传递数据时性能很 好，因为若使⽤间接缓冲区，JVM会先将数据复制到直接缓冲区再进⾏传递；但是直接缓冲区的 缺点是在分配内存空间和释放内存时⽐堆缓冲区更复杂，⽽Netty使⽤内存池来解决这样的问题， 这也是Netty使⽤内存池的原因之⼀。直接缓冲区不⽀持数组访问数据，但是我们可以间接的访问 数据数组，如下⾯代码：

[java] view plaincopy

![image 25](assets/imageFile25.png)

ByteBuf directBuf = Unpoled.directBufer(16); if(!directBuf.hasArray(){

- 2.
- 3.
- 4.
- 5.
- 6.


int len = directBuf.readableBytes(); byte[] arr =newbyte[len]; directBuf.getBytes(0, arr);

}

访问直接缓冲区的数据数组需要更多的编码和更复杂的操作，建议若需要在数组访问数据使⽤堆 缓冲区会更好。

Composite Buffer(复合缓冲区)

复合缓冲区，我们可以创建多个不同的ByteBuf，然后提供⼀个这些ByteBuf组合的视图。复 合缓冲区就像⼀个列表，我们可以动态的添加和删除其中的ByteBuf，JDK的ByteBuffer没有这样 的功能。Netty提供了CompositeByteBuf类来处理复合缓冲区，CompositeByteBuf只是⼀个视 图，CompositeByteBuf.hasArray()总是返回false，因为它可能包含⼀些直接或间接的不同类型的 ByteBuf。

例如，⼀条消息由header和body两部分组成，将header和body组装成⼀条消息发送出去，可 能body相同，只是header不同，使⽤CompositeByteBuf就不⽤每次都重新分配⼀个新的缓冲区。 下图显⽰CompositeByteBuf组成header和body：

![image 26](assets/imageFile26.png)

若使⽤JDK的ByteBuffer就不能这样简单的实现，只能创建⼀个数组或创建⼀个新的ByteBuffer， 再将内容复制到新的ByteBuffer中。下⾯是使⽤CompositeByteBuf的例⼦：

[java] view plaincopy

![image 27](assets/imageFile27.png)

CompositeByteBuf compBuf = Unpoled.compositeBufer(); ByteBuf heapBuf = Unpoled.bufer(8); ByteBuf directBuf = Unpoled.directBufer(16);

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


/添加ByteBuf到CompositeByteBuf compBuf.adComponents(heapBuf,directBuf);

/删除第⼀个ByteBuf compBuf.removeComponent(0); Iterator<ByteBuf> iter = compBuf.iterator(); while(iter.hasNext(){

System.out.println(iter.next().toString(); }

/使⽤数组访问数据

if(!compBuf.hasArray(){ int len = compBuf.readableBytes(); byte[] arr =newbyte[len]; compBuf.getBytes(0, arr);

}

CompositeByteBuf是ByteBuf的⼦类，我们可以像操作BytBuf⼀样操作CompositeByteBuf。并且 Netty优化套接字读写的操作是尽可能的使⽤CompositeByteBuf来做的，使⽤CompositeByteBuf 不会操作内存泄露问题。

#### 5.3 ByteBuf的字节操作

ByteBuf提供了许多操作，允许修改其中的数据内容或只是读取数据。ByteBuf和JDK的 ByteBuffer很像，但是ByteBuf提供了更好的性能。

- 5.3.1 随机访问索引


ByteBuf使⽤zero-based-indexing(从0开始的索引)，第⼀个字节的索引是0，最后⼀个字节的 索引是ByteBuf的capacity - 1，下⾯代码是遍历ByteBuf的所有字节：

- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


注意通过索引访问时不会推进读索引和写索引，我们可以通过ByteBuf的readerIndex()或 writerIndex()来分别推进读索引或写索引。

- 5.3.2 顺序访问索引

ByteBuf提供两个指针变量⽀付读和写操作，读操作是使⽤readerIndex()，写操作时使⽤ writerIndex()。这和JDK的ByteBuffer不同，ByteBuffer只有⼀个⽅法来设置索引，所以需要使⽤ flip()⽅法来切换读和写模式。

ByteBuf⼀定符合：0 <= readerIndex <= writerIndex <= capacity。

/create a ByteBuf of capacity is 16 ByteBuf buf = Unpoled.bufer(16);

/write data to buf for(int i=0;i<16;i +){

buf.writeByte(i+1); }

/read data from buf for(int i=0;i<buf.capacity();i +){

System.out.println(buf.getByte(i); }

![image 29](assets/imageFile29.png)

- 5.3.3 Discardable bytes废弃字节


我们可以调⽤ByteBuf.discardReadBytes()来回收已经读取过的字节，discardReadBytes()将 丢弃从索引0到readerIndex之间的字节。调⽤discardReadBytes()⽅法后会变成如下图：

![image 30](assets/imageFile30.png)

ByteBuf.discardReadBytes()可以⽤来清空ByteBuf中已读取的数据，从⽽使ByteBuf有多余的 空间容纳新的数据，但是discardReadBytes()可能会涉及内存复制，因为它需要移动ByteBuf中可 读的字节到开始位置，这样的操作会影响性能，⼀般在需要马上释放内存的时候使⽤收益会⽐较 ⼤。

- 5.3.4 可读字节(实际内容)

任何读操作会增加readerIndex，如果读取操作的参数也是⼀个ByteBuf⽽没有指定⽬的索 引，指定的⽬的缓冲区的writerIndex会⼀起增加，没有⾜够的内容时会抛出 IndexOutOfBoundException。新分配、包装、复制的缓冲区的readerIndex的默认值都是0。下⾯ 代码显⽰了获取所有可读数据：

[java] view plaincopy

![image 31](assets/imageFile31.png)

(代码于原书中有出⼊，原书可能是基于Netty4之前的版本讲解的，此处基于Netty4)

- 5.3.5 可写字节Writable bytes


- 1.
- 2.
- 3.
- 4.


ByteBuf buf = Unpoled.bufer(16); while(buf.isReadable(){

System.out.println(buf.readByte(); }

任何写的操作会增加writerIndex。若写操作的参数也是⼀个ByteBuf并且没有指定数据源索 引，那么指定缓冲区的readerIndex也会⼀起增加。若没有⾜够的可写字节会抛出 IndexOutOfBoundException。新分配的缓冲区writerIndex的默认值是0。下⾯代码显⽰了随机⼀个 int数字来填充缓冲区，直到缓冲区空间耗尽：

Random random =new Random(); ByteBuf buf = Unpoled.bufer(16); while(buf.writableBytes() >=4){

- 2.
- 3.
- 4.
- 5.


buf.writeInt(random.nextInt(); }

###### 5.3.6 清除缓冲区索引Clearing the buffer indexs

调⽤ByteBuf.clear()可以设置readerIndex和writerIndex为0，clear()不会清除缓冲区的内容， 只是将两个索引值设置为0。请注意ByteBuf.clear()与JDK的ByteBuffer.clear()的语义不同。

下图显⽰了ByteBuf调⽤clear()之前：

![image 33](assets/imageFile33.png)

下图显⽰了调⽤clear()之后：

![image 34](assets/imageFile34.png)

和discardReadBytes()相⽐，clear()是便宜的，因为clear()不会复制任何内存。

###### 5.3.7 搜索操作Search operations

各种indexOf()⽅法帮助你定位⼀个值的索引是否符合，我们可以⽤ByteBufProcessor复杂动 态顺序搜索实现简单的静态单字节搜索。如果你想解码可变长度的数据，如null结尾的字符串，你 会发现bytesBefore(byte value)⽅法有⽤。例如我们写⼀个集成的flash sockets的应⽤程序，这个 应⽤程序使⽤NULL结束的内容，使⽤bytesBefore(byte value)⽅法可以很容易的检查数据中的空 字节。没有ByteBufProcessor的话，我们需要⾃⼰做这些事情，使⽤ByteBufProcessor效率更 好。

###### 5.3.8 标准和重置Mark and reset

每个ByteBuf有两个标注索引，⼀个存储readerIndex，⼀个存储writerIndex。你可以通过调

⽤⼀个重置⽅法重新定位两个索引之⼀，它类似于InputStream的标注和重置⽅法，没有读限制。 我们可以通过调⽤readerIndex(int readerIndex)和writerIndex(int writerIndex)移动读索引和写索引 到指定位置，调⽤这两个⽅法设置指定索引位置时可能抛出IndexOutOfBoundException。

###### 5.3.9 衍⽣的缓冲区Derived buffers

调⽤duplicate()、slice()、slice(int index, int length)、order(ByteOrder endianness)会创建⼀ 个现有缓冲区的视图。衍⽣的缓冲区有独⽴的readerIndex、writerIndex和标注索引。如果需要现 有缓冲区的全新副本，可以使⽤copy()或copy(int index, int length)获得。看下⾯代码：

[java] view plaincopy

![image 35](assets/imageFile35.png)

/ get a Charset of UTF-8 Charset utf8 = Charset.forName("UTF-8"); / get a ByteBuf ByteBuf buf = Unpoled.copiedBufer("“Nety in Action rocks!“", utf8); / slice ByteBuf sliced = buf.slice(0,14); / copy ByteBuf copy = buf.copy(0,14); / print "“Nety in Action rocks!“" System.out.println(buf.toString(utf8); / print " Nety in Act" System.out.println(sliced.toString(utf8); / print " Nety in Act" System.out.println(copy.toString(utf8);

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


- 5.3.10 读/写操作以及其他⼀些操作


有两种主要类型的读写操作：

get/set操作以索引为基础，在给定的索引设置或获取字节 从当前索引开始读写，递增当前的写索引或读索引

ByteBuf的各种读写⽅法或其他⼀些检查⽅法可以看ByteBuf的源码，这⾥不赘述了。

#### 5.4 ByteBufHolder

ByteBufHolder是⼀个辅助类，是⼀个接⼜，其实现类是DefaultByteBufHolder，还有⼀些实 现了ByteBufHolder接⼜的其他接⼜类。ByteBufHolder的作⽤就是帮助更⽅便的访问ByteBuf中的 数据，当缓冲区没⽤了后，可以使⽤这个辅助类释放资源。ByteBufHolder很简单，提供的可供访 问的⽅法也很少。如果你想实现⼀个“消息对象”有效负载存储在ByteBuf，使⽤ByteBufHolder是⼀ 个好主意。

尽管Netty提供的各种缓冲区实现类已经很容易使⽤，但Netty依然提供了⼀些使⽤的⼯具 类，使得创建和使⽤各种缓冲区更加⽅便。下⾯会介绍⼀些Netty中的缓冲区⼯具类。

###### 5.4.1 ByteBufAllocator

Netty⽀持各种ByteBuf的池实现，来使Netty提供⼀种称为ByteBufAllocator成为可能。 ByteBufAllocator负责分配ByteBuf实例，ByteBufAllocator提供了各种分配不同ByteBuf的⽅法，如 需要⼀个堆缓冲区可以使⽤ByteBufAllocator.heapBuffer()，需要⼀个直接缓冲区可以使⽤ ByteBufAllocator.directBuffer()，需要⼀个复合缓冲区可以使⽤ ByteBufAllocator.compositeBuffer()。其他⽅法的使⽤可以看ByteBufAllocator源码及注释。

获取ByteBufAllocator对象很容易，可以从Channel的alloc()获取，也可以从 ChannelHandlerContext的alloc()获取。看下⾯代码：

[java] view plaincopy

![image 36](assets/imageFile36.png)

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


ServerBotstrap b =new ServerBotstrap(); b.group(group).chanel(NioServerSocketChanel.clas).localAdres(new InetSocketAdres(port)

.childHandler(new ChanelInitializer<SocketChanel>() { @Override protectedvoid initChanel(SocketChanel ch)throws Exception {

/ get ByteBufAlocator instance by Chanel.aloc() ByteBufAlocator aloc0 = ch.aloc(); ch.pipeline().adLast(new ChanelInboundHandlerAdapter() {

@Override publicvoid chanelActive(ChanelHandlerContext ctx)throws Exception {

/get ByteBufAlocator instance by ChanelHandlerContext.aloc() ByteBufAlocator aloc1 = ctx.aloc(); ctx.writeAndFlush(buf.duplicate().adListener(ChanelFutureListener.CLOSE);

} });

} });

Netty有两种不同的ByteBufAllocator实现，⼀个实现ByteBuf实例池将分配和回收成本以及内 存使⽤降到最低；另⼀种实现是每次使⽤都创建⼀个新的ByteBuf实例。Netty默认使⽤ PooledByteBufAllocator，我们可以通过ChannelConfig或通过引导设置⼀个不同的实现来改变。 更多细节在后⾯讲述。

###### 5.4.2 Unpooled

Unpooled也是⽤来创建缓冲区的⼯具类，Unpooled的使⽤也很容易。Unpooled提供了很多 ⽅法，详细⽅法及使⽤可以看API⽂档或Netty源码。看下⾯代码：

[java] view plaincopy

![image 37](assets/imageFile37.png)

/创建复合缓冲区 CompositeByteBuf compBuf = Unpoled.compositeBufer(); /创建堆缓冲区 ByteBuf heapBuf = Unpoled.bufer(8); /创建直接缓冲区 ByteBuf directBuf = Unpoled.directBufer(16);

- 2.
- 3.
- 4.
- 5.
- 6.


###### 5.4.3 ByteBufUtil

ByteBufUtil提供了⼀些静态的⽅法，在操作ByteBuf时⾮常有⽤。ByteBufUtil提供了 Unpooled之外的⼀些⽅法，也许最有价值的是hexDump(ByteBuf buffer)⽅法，这个⽅法返回指定 ByteBuf中可读字节的⼗六进制字符串，可以⽤于调试程序时打印ByteBuf的内容，⼗六进制字符 串相⽐字节⽽⾔对⽤户更友好。

#### 5.5 Summary

本章主要学习Netty提供的缓冲区类ByteBuf的创建和简单实⽤以及⼀些操作ByteBuf的⼯具 类。

### 第六章：ChanelHandler

本章介绍

ChannelPipeline ChannelHandlerContext ChannelHandler Inbound vs outbound(⼊站和出站)

接受连接或创建他们只是你的应⽤程序的⼀部分，虽然这些任何很重要，但是⼀个⽹络应⽤程序 旺旺是更复杂的，需要更多的代码编写，如处理传⼊和传出的数据。Netty提供了⼀个强⼤的处理这些 事情的功能，允许⽤户⾃定义ChannelHandler的实现来处理数据。使得ChannelHandler更强⼤的是可 以连接每个ChannelHandler来实现任务，这有助于代码的整洁和重⽤。但是处理数据只是

ChannelHandler所做的事情之⼀，也可以压制I/O操作，例如写请求。所有这些都可以动态实现。

#### 6.1 ChannelPipeline

ChannelPipeline是ChannelHandler实例的列表，⽤于处理或截获通道的接收和发送数据。 ChannelPipeline提供了⼀种⾼级的截取过滤器模式，让⽤户可以在ChannelPipeline中完全控制⼀ 个事件及如何处理ChannelHandler与ChannelPipeline的交互。

对于每个新的通道，会创建⼀个新的ChannelPipeline并附加⾄通道。⼀旦连接，Channel和 ChannelPipeline之间的耦合是永久性的。Channel不能附加其他的ChannelPipeline或从 ChannelPipeline分离。

下图描述了ChannelHandler在ChannelPipeline中的I/O处理，⼀个I/O操作可以由⼀个 ChannelInboundHandler或ChannelOutboundHandler进⾏处理，并通过调⽤ ChannelInboundHandler处理⼊站IO或通过ChannelOutboundHandler处理出站IO。

![image 38](assets/imageFile38.png)

如上图所⽰，ChannelPipeline是ChannelHandler的⼀个列表；如果⼀个⼊站I/O事件被触发，这个 事件会从第⼀个开始依次通过ChannelPipeline中的ChannelHandler；若是⼀个⼊站I/O事件，则会 从最后⼀个开始依次通过ChannelPipeline中的ChannelHandler。ChannelHandler可以处理事件并 检查类型，如果某个ChannelHandler不能处理则会跳过，并将事件传递到下⼀个

ChannelHandler。ChannelPipeline可以动态添加、删除、替换其中的ChannelHandler，这样的机 制可以提⾼灵活性。

修改ChannelPipeline的⽅法：

addFirst(...)，添加ChannelHandler在ChannelPipeline的第⼀个位置 addBefore(...)，在ChannelPipeline中指定的ChannelHandler名称之前添加ChannelHandler addAfter(...)，在ChannelPipeline中指定的ChannelHandler名称之后添加ChannelHandler addLast(ChannelHandler...)，在ChannelPipeline的末尾添加ChannelHandler remove(...)，删除ChannelPipeline中指定的ChannelHandler replace(...)，替换ChannelPipeline中指定的ChannelHandler

[java] view plaincopy

![image 39](assets/imageFile39.png)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


ChanelPipeline pipeline = ch.pipeline(); FirstHandler firstHandler =new FirstHandler(); pipeline.adLast("handler1", firstHandler); pipeline.adFirst("handler2",new SecondHandler(); pipeline.adLast("handler3",new ThirdHandler(); pipeline.remove("“handler3“"); pipeline.remove(firstHandler); pipeline.replace("handler2","handler4",new FourthHandler();

被添加到ChannelPipeline的ChannelHandler将通过IO-Thread处理事件，这意味了必须不能 有其他的IO-Thread阻塞来影响IO的整体处理；有时候可能需要阻塞，例如JDBC。因此，Netty允 许通过⼀个EventExecutorGroup到每⼀个ChannelPipeline.add*⽅法，⾃定义的事件会被包含在 EventExecutorGroup中的EventExecutor来处理，默认的实现是DefaultEventExecutorGroup。

ChannelPipeline除了⼀些修改的⽅法，还有很多其他的⽅法，具体是⽅法及使⽤可以看API ⽂档或源码。

#### 6.2 ChannelHandlerContext

每个ChannelHandler被添加到ChannelPipeline后，都会创建⼀个ChannelHandlerContext并 与之创建的ChannelHandler关联绑定。ChannelHandlerContext允许ChannelHandler与其他的 ChannelHandler实现进⾏交互，这是相同ChannelPipeline的⼀部分。ChannelHandlerContext不 会改变添加到其中的ChannelHandler，因此它是安全的。

###### 6.2.1 通知下⼀个ChannelHandler

在相同的ChannelPipeline中通过调⽤ChannelInboundHandler和ChannelOutboundHandler 中各个⽅法中的⼀个⽅法来通知最近的handler，通知开始的地⽅取决你如何设置。下图显⽰了 ChannelHandlerContext、ChannelHandler、ChannelPipeline的关系：

![image 40](assets/imageFile40.png)

如果你想有⼀些事件流全部通过ChannelPipeline，有两个不同的⽅法可以做到：

调⽤Channel的⽅法 调⽤ChannelPipeline的⽅法

这两个⽅法都可以让事件流全部通过ChannelPipeline。⽆论从头部还是尾部开始，因为它主 要依赖于事件的性质。如果是⼀个“⼊站”事件，它开始于头部；若是⼀个“出站”事件，则开始于尾 部。

下⾯的代码显⽰了⼀个写事件如何通过ChannelPipeline从尾部开始：

[java] view plaincopy

![image 41](assets/imageFile41.png)

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


@Override protectedvoid initChanel(SocketChanel ch)throws Exception { ch.pipeline().adLast(new ChanelInboundHandlerAdapter() { @Override publicvoid chanelActive(ChanelHandlerContext ctx)throws Exception {

/Event via Chanel Chanel chanel = ctx.chanel(); chanel.write(Unpoled.copiedBufer("nety in action", CharsetUtil.UTF_8);

/Event via ChanelPipeline ChanelPipeline pipeline = ctx.pipeline(); pipeline.write(Unpoled.copiedBufer("nety in action", CharsetUtil.UTF_8);

} });

}

下图表⽰通过Channel或ChannelPipeline的通知：

![image 42](assets/imageFile42.png)

可能你想从ChannelPipeline的指定位置开始，不想流经整个ChannelPipeline，如下情况：

为了节省开销，不感兴趣的ChannelHandler不让通过 排除⼀些ChannelHandler

在这种情况下，你可以使⽤ChannelHandlerContext的ChannelHandler通知起点。它使⽤ ChannelHandlerContext执⾏下⼀个ChannelHandler。下⾯代码显⽰了直接使⽤ ChannelHandlerContext操作：

[java] view plaincopy

![image 43](assets/imageFile43.png)

- 1.
- 2.
- 3.
- 4.


/ Get reference of ChanelHandlerContext ChanelHandlerContext ctx =.;

/ Write bufer via ChanelHandlerContext ctx.write(Unpoled.copiedBufer("Nety in Action", CharsetUtil.UTF_8);

该消息流经ChannelPipeline到下⼀个ChannelHandler，在这种情况下使⽤ ChannelHandlerContext开始下⼀个ChannelHandler。下图显⽰了事件流：

![image 44](assets/imageFile44.png)

如上图显⽰的，从指定的ChannelHandlerContext开始，跳过前⾯所有的ChannelHandler，使⽤ ChannelHandlerContext操作是常见的模式，最常⽤的是从ChannelHanlder调⽤操作，也可以在 外部使⽤ChannelHandlerContext，因为这是线程安全的。

###### 6.2.2 修改ChannelPipeline

调⽤ChannelHandlerContext的pipeline()⽅法能访问ChannelPipeline，能在运⾏时动态的增 加、删除、替换ChannelPipeline中的ChannelHandler。可以保持ChannelHandlerContext供以后 使⽤，如外部Handler⽅法触发⼀个事件，甚⾄从⼀个不同的线程。

下⾯代码显⽰了保存ChannelHandlerContext供之后使⽤或其他线程使⽤：

[java] view plaincopy

![image 45](assets/imageFile45.png)

publicclas WriteHandlerextends ChanelHandlerAdapter { private ChanelHandlerContext ctx;

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


@Override publicvoid handlerAded(ChanelHandlerContext ctx)throws Exception {

this.ctx = ctx; }

publicvoid send(String msg){

ctx.write(msg); }

}

请注意，ChannelHandler实例如果带有@Sharable注解则可以被添加到多个 ChannelPipeline。也就是说单个ChannelHandler实例可以有多个ChannelHandlerContext，因此 可以调⽤不同ChannelHandlerContext获取同⼀个ChannelHandler。如果添加不带@Sharable注 解的ChannelHandler实例到多个ChannelPipeline则会抛出异常；使⽤@Sharable注解后的 ChannelHandler必须在不同的线程和不同的通道上安全使⽤。怎么是不安全的使⽤？看下⾯代 码：

[java] view plaincopy

![image 46](assets/imageFile46.png)

@Sharable publicclas NotSharableHandlerextends ChanelInboundHandlerAdapter {

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


privateint count;

@Override publicvoid chanelRead(ChanelHandlerContext ctx, Object msg)throws Exception {

count+; System.out.println("chanelRead(.) caled the " + count +" time“"); ctx.fireChanelRead(msg);

}

}

上⾯是⼀个带@Sharable注解的Handler，它被多个线程使⽤时，⾥⾯count是不安全的，会导致 count值错误。

为什么要共享ChannelHandler？使⽤@Sharable注解共享⼀个ChannelHandler在⼀些需求中 还是有很好的作⽤的，如使⽤⼀个ChannelHandler来统计连接数或来处理⼀些全局数据等等。

##### 6.3 状态模型

Netty有⼀个简单但强⼤的状态模型，并完美映射到ChannelInboundHandler的各个⽅法。下 ⾯是Channel⽣命周期四个不同的状态：

channelUnregistered channelRegistered channelActive channelInactive

Channel的状态在其⽣命周期中变化，因为状态变化需要触发，下图显⽰了Channel状态变化：

![image 47](assets/imageFile47.png)

还可以看到额外的状态变化，因为⽤户允许从EventLoop中注销Channel暂停事件执⾏，然后 再重新注册。在这种情况下，你会看到多个channelRegistered和channelUnregistered状态的变 化，⽽永远只有⼀个channelActive和channelInactive的状态，因为⼀个通道在其⽣命周期内只能 连接⼀次，之后就会被回收；重新连接，则是创建⼀个新的通道。

下图显⽰了从EventLoop中注销Channel后再重新注册的状态变化：

![image 48](assets/imageFile48.png)

#### 6.4 ChannelHandler和其⼦类

Netty中有3个实现了ChannelHandler接⼜的类，其中2个是接⼜，⼀个是抽象类。如下图：

![image 49](assets/imageFile49.png)

###### 6.4.1 ChannelHandler中的⽅法

Netty定义了良好的类型层次结构来表⽰不同的处理程序类型，所有的类型的⽗类是 ChannelHandler。ChannelHandler提供了在其⽣命周期内添加或从ChannelPipeline中删除的⽅ 法。

handlerAdded，ChannelHandler添加到实际上下⽂中准备处理事件 handlerRemoved，将ChannelHandler从实际上下⽂中删除，不再处理事件 exceptionCaught，处理抛出的异常

上⾯三个⽅法都需要传递ChannelHandlerContext参数，每个ChannelHandler被添加到 ChannelPipeline时会⾃动创建ChannelHandlerContext。ChannelHandlerContext允许在本地通道 安全的存储和检索值。Netty还提供了⼀个实现了ChannelHandler的抽象类： ChannelHandlerAdapter。ChannelHandlerAdapter实现了⽗类的所有⽅法，基本上就是传递事件 到ChannelPipeline中的下⼀个ChannelHandler直到结束。

###### 6.4.2 ChannelInboundHandler

ChannelInboundHandler提供了⼀些⽅法再接收数据或Channel状态改变时被调⽤。下⾯是 ChannelInboundHandler的⼀些⽅法：

channelRegistered，ChannelHandlerContext的Channel被注册到EventLoop； channelUnregistered，ChannelHandlerContext的Channel从EventLoop中注销 channelActive，ChannelHandlerContext的Channel已激活 channelInactive，ChannelHanderContxt的Channel结束⽣命周期 channelRead，从当前Channel的对端读取消息 channelReadComplete，消息读取完成后执⾏ userEventTriggered，⼀个⽤户事件被处罚 channelWritabilityChanged，改变通道的可写状态，可以使⽤Channel.isWritable()检查 exceptionCaught，重写⽗类ChannelHandler的⽅法，处理异常

Netty提供了⼀个实现了ChannelInboundHandler接⼜并继承ChannelHandlerAdapter的类： ChannelInboundHandlerAdapter。ChannelInboundHandlerAdapter实现了 ChannelInboundHandler的所有⽅法，作⽤就是处理消息并将消息转发到ChannelPipeline中的下 ⼀个ChannelHandler。ChannelInboundHandlerAdapter的channelRead⽅法处理完消息后不会⾃ 动释放消息，若想⾃动释放收到的消息，可以使⽤SimpleChannelInboundHandler<I>。

看下⾯代码：

[java] view plaincopy

![image 50](assets/imageFile50.png)

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


/*

- *实现ChanelInboundHandlerAdapter的Handler，不会⾃动释放接收的消息对象

- * @author c.k

*

- */ publicclas DiscardHandlerextends ChanelInboundHandlerAdapter {


@Override publicvoid chanelRead(ChanelHandlerContext ctx, Object msg)throws Exception {

/⼿动释放消息

ReferenceCountUtil.release(msg); }

}

[java] view plaincopy

![image 51](assets/imageFile51.png)

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


/*

- *继承SimpleChanelInboundHandler，会⾃动释放消息对象

- * @author c.k

*

- */ publicclas SimpleDiscardHandlerextends SimpleChanelInboundHandler<Object> {


@Override protectedvoid chanelRead0(ChanelHandlerContext ctx, Object msg)throws Exception {

/不需要⼿动释放 }

}

如果需要其他状态改变的通知，可以重写Handler的其他⽅法。通常⾃定义消息类型来解码字 节，可以实现ChannelInboundHandler或ChannelInboundHandlerAdapter。有⼀个更好的解决⽅ 法，使⽤编解码器的框架可以很容的实现。使⽤ChannelInboundHandler、 ChannelInboundHandlerAdapter、SimpleChannelInboundhandler这三个中的⼀个来处理接收消 息，使⽤哪⼀个取决于需求；⼤多数时候使⽤SimpleChannelInboundHandler处理消息，使⽤ ChannelInboundHandlerAdapter处理其他的“⼊站”事件或状态改变。

ChannelInitializer⽤来初始化ChannelHandler，将⾃定义的各种ChannelHandler添加到 ChannelPipeline中。

###### 6.4.3 ChannelOutboundHandler

ChannelOutboundHandler⽤来处理“出站”的数据消息。ChannelOutboundHandler提供了下 ⾯⼀些⽅法：

bind，Channel绑定本地地址 connect，Channel连接操作 disconnect，Channel断开连接 close，关闭Channel deregister，注销Channel read，读取消息，实际是截获ChannelHandlerContext.read() write，写操作，实际是通过ChannelPipeline写消息，Channel.flush()属性到实际通道 flush，刷新消息到通道

ChannelOutboundHandler是ChannelHandler的⼦类，实现了ChannelHandler的所有⽅法。 所有最重要的⽅法采取ChannelPromise，因此⼀旦请求停⽌从ChannelPipeline转发参数则必须得 到通知。Netty提供了ChannelOutboundHandler的实现：ChannelOutboundHandlerAdapter。 ChannelOutboundHandlerAdapter实现了⽗类的所有⽅法，并且可以根据需要重写感兴趣的⽅ 法。所有这些⽅法的实现，在默认情况下，都是通过调⽤ChannelHandlerContext的⽅法将事件转 发到ChannelPipeline中下⼀个ChannelHandler。

看下⾯的代码：

[java] view plaincopy

![image 52](assets/imageFile52.png)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


publicclas DiscardOutboundHandlerextends ChanelOutboundHandlerAdapter { @Override publicvoid write(ChanelHandlerContext ctx, Object msg, ChanelPromise promise)throws Exception {

ReferenceCountUtil.release(msg); promise.setSuces();

} }

重要的是要记得释放致远并直通ChannelPromise，若ChannelPromise没有被通知可能会导 致其中⼀个ChannelFutureListener不被通知去处理⼀个消息。

如果消息被消费并且没有被传递到ChannelPipeline中的下⼀个ChannelOutboundHandler， 那么就需要调⽤ReferenceCountUtil.release(message)来释放消息资源。⼀旦消息被传递到实际 的通道，它会⾃动写⼊消息或在通道关闭是释放。

### 第七章：编解码器Codec

本章介绍

Codec，编解码器 Decoder，解码器 Encoder，编码器

Netty提供了编解码器框架，使得编写⾃定义的编解码器很容易，并且也很容易重⽤和封装。本章 讨论Netty的编解码器框架以及使⽤。

#### 7.1 编解码器Codec

编写⼀个⽹络应⽤程序需要实现某种编解码器，编解码器的作⽤就是讲原始字节数据与⾃定 义的消息对象进⾏互转。⽹络中都是以字节码的数据形式来传输数据的，服务器编码数据后发送 到客户端，客户端需要对数据进⾏解码，因为编解码器由两部分组成：

Decoder(解码器) Encoder(编码器)

解码器负责将消息从字节或其他序列形式转成指定的消息对象，编码器则相反；解码器负责 处理“⼊站”数据，编码器负责处理“出站”数据。编码器和解码器的结构很简单，消息被编码后解码 后会⾃动通过ReferenceCountUtil.release(message)释放，如果不想释放消息可以使⽤ ReferenceCountUtil.retain(message)，这将会使引⽤数量增加⽽没有消息发布，⼤多数时候不需 要这么做。

##### 7.2 解码器

Netty提供了丰富的解码器抽象基类，我们可以很容易的实现这些基类来⾃定义解码器。下⾯ 是解码器的⼀个类型：

解码字节到消息 解码消息到消息 解码消息到字节

本章将概述不同的抽象基类，来帮助了解解码器的实现。深⼊了解Netty提供的解码器之前先 了解解码器的作⽤是什么？解码器负责解码“⼊站”数据从⼀种格式到另⼀种格式，解码器处理⼊站 数据是抽象ChannelInboundHandler的实现。实践中使⽤解码器很简单，就是将⼊站数据转换格式 后传递到ChannelPipeline中的下⼀个ChannelInboundHandler进⾏处理；这样的处理时很灵活 的，我们可以将解码器放在ChannelPipeline中，重⽤逻辑。

###### 7.2.1 ByteToMessageDecoder

通常你需要将消息从字节解码成消息或者从字节解码成其他的序列化字节。这是⼀个常见的 任务，Netty提供了抽象基类，我们可以使⽤它们来实现。Netty中提供的ByteToMessageDecoder 可以将字节消息解码成POJO对象，下⾯列出了ByteToMessageDecoder两个主要⽅法：

decode(ChannelHandlerContext, ByteBuf, List<Object>)，这个⽅法是唯⼀的⼀个需要⾃⼰实 现的抽象⽅法，作⽤是将ByteBuf数据解码成其他形式的数据。 decodeLast(ChannelHandlerContext, ByteBuf, List<Object>)，实际上调⽤的是decode(...)。

例如服务器从某个客户端接收到⼀个整数值的字节码，服务器将数据读⼊ByteBuf并经过 ChannelPipeline中的每个ChannelInboundHandler进⾏处理，看下图：

![image 53](assets/imageFile53.png)

###### 上图显⽰了从“⼊站”ByteBuf读取bytes后由ToIntegerDecoder进⾏解码，然后向解码后的消息传递 到ChannelPipeline中的下⼀个ChannelInboundHandler。看下⾯ToIntegerDecoder的实现代码：

[java] view plaincopy

- * Integer解码器,ByteToMesageDecoder实现

- * @author c.k

*

- */ publicclas ToIntegerDecoderextends ByteToMesageDecoder {


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


@Override protectedvoid decode(ChanelHandlerContext ctx, ByteBuf in, List<Object> out)throws Exception {

if(in.readableBytes() >=4){

out.ad(in.readInt(); }

} }

从上⾯的代码可能会发现，我们需要检查ByteBuf读之前是否有⾜够的字节，若没有这个检查岂不 更好？是的，Netty提供了这样的处理允许byte-to-message解码,在下⼀节讲解。除了 ByteToMessageDecoder之外，Netty还提供了许多其他的解码接⼜。

###### 7.2.2 ReplayingDecoder

ReplayingDecoder是byte-to-message解码的⼀种特殊的抽象基类，读取缓冲区的数据之前 需要检查缓冲区是否有⾜够的字节，使⽤ReplayingDecoder就⽆需⾃⼰检查；若ByteBuf中有⾜够 的字节，则会正常读取；若没有⾜够的字节则会停⽌解码。也正因为这样的包装使得 ReplayingDecoder带有⼀定的局限性。

不是所有的操作都被ByteBuf⽀持，如果调⽤⼀个不⽀持的操作会抛出DecoderException。 ByteBuf.readableBytes()⼤部分时间不会返回期望值

如果你能忍受上⾯列出的限制，相⽐ByteToMessageDecoder，你可能更喜欢 ReplayingDecoder。在满⾜需求的情况下推荐使⽤ByteToMessageDecoder，因为它的处理⽐较 简单，没有ReplayingDecoder实现的那么复杂。ReplayingDecoder继承与 ByteToMessageDecoder，所以他们提供的接⼜是相同的。下⾯代码是ReplayingDecoder的实 现：

[java] view plaincopy

- * Integer解码器,ReplayingDecoder实现

- * @author c.k

*

- */ publicclas ToIntegerReplayingDecoderextends ReplayingDecoder<Void> {


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


@Override protectedvoid decode(ChanelHandlerContext ctx, ByteBuf in, List<Object> out)throws Exception {

out.ad(in.readInt(); }

}

当从接收的数据ByteBuf读取integer，若没有⾜够的字节可读，decode(...)会停⽌解码，若有 ⾜够的字节可读，则会读取数据添加到List列表中。使⽤ReplayingDecoder或 ByteToMessageDecoder是个⼈喜好的问题，Netty提供了这两种实现，选择哪⼀个都可以。

上⾯讲了byte-to-message的解码实现⽅式，那message-to-message该如何实现呢？Netty提 供了MessageToMessageDecoder抽象类。

###### 7.2.3 MessageToMessageDecoder

将消息对象转成消息对象可是使⽤MessageToMessageDecoder，它是⼀个抽象类，需要我 们⾃⼰实现其decode(...)。message-to-message同上⾯讲的byte-to-message的处理机制⼀样，看 下图：

![image 54](assets/imageFile54.png)

看下⾯的实现代码：

- *将接收的Integer消息转成String类型，MesageToMesageDecoder实现

- * @author c.k

*

- */ publicclas IntegerToStringDecoderextends MesageToMesageDecoder<Integer> {


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


@Override protectedvoid decode(ChanelHandlerContext ctx, Integer msg, List<Object> out)throws Exception {

out.ad(String.valueOf(msg); }

}

- 7.2.4 解码器总结


解码器是⽤来处理⼊站数据，Netty提供了很多解码器的实现，可以根据需求详细了解。那我 们发送数据需要将数据编码，Netty中也提供了编码器的⽀持。下⼀节将讲解如何实现编码器。

##### 7.3 编码器

Netty提供了⼀些基类，我们可以很简单的编码器。同样的，编码器有下⾯两种类型：

消息对象编码成消息对象 消息对象编码成字节码

相对解码器，编码器少了⼀个byte-to-byte的类型，因为出站数据这样做没有意义。编码器的 作⽤就是将处理好的数据转成字节码以便在⽹络中传输。对照上⾯列出的两种编码器类型，Netty 也分别提供了两个抽象类：MessageToByteEncoder和MessageToMessageEncoder。下⾯是类关 系图：

![image 55](assets/imageFile55.png)

###### 7.3.1 MessageToByteEncoder

MessageToByteEncoder是抽象类，我们⾃定义⼀个继承MessageToByteEncoder的编码器 只需要实现其提供的encode(...)⽅法。其⼯作流程如下图：

![image 56](assets/imageFile56.png)

实现代码如下：

- *编码器，将Integer值编码成byte[]，MesageToByteEncoder实现

- * @author c.k

*

- */ publicclas IntegerToByteEncoderextends MesageToByteEncoder<Integer> {


- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.


@Override protectedvoid encode(ChanelHandlerContext ctx, Integer msg, ByteBuf out)throws Exception {

out.writeInt(msg); }

}

###### 7.3.2 MessageToMessageEncoder

需要将消息编码成其他的消息时可以使⽤Netty提供的MessageToMessageEncoder抽象类来 实现。例如将Integer编码成String，其⼯作流程如下图：

![image 57](assets/imageFile57.png)

代码实现如下：

[java] view plaincopy

- *编码器，将Integer编码成String，MesageToMesageEncoder实现

- * @author c.k

*

- */ publicclas IntegerToStringEncoderextends MesageToMesageEncoder<Integer> {


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


@Override protectedvoid encode(ChanelHandlerContext ctx, Integer msg, List<Object> out)throws Exception {

out.ad(String.valueOf(msg); }

}

##### 7.4 编解码器

实际编码中，⼀般会将编码和解码操作封装太⼀个类中，解码处理“⼊站”数据，编码处理“出 站”数据。知道了编码和解码器，对于下⾯的情况不会感觉惊讶：

byte-to-message编码和解码 message-to-message编码和解码

如果确定需要在ChannelPipeline中使⽤编码器和解码器，需要更好的使⽤⼀个抽象的编解码 器。同样，使⽤编解码器的时候，不可能只删除解码器或编码器⽽离开ChannelPipeline导致某种 不⼀致的状态。使⽤编解码器将强制性的要么都在ChannelPipeline，要么都不在 ChannelPipeline。

考虑到这⼀点，我们在下⾯⼏节将更深⼊的分析Netty提供的编解码抽象类。

###### 7.4.1 byte-to-byte编解码器

Netty4较之前的版本，其结构有很⼤的变化，在Netty4中实现byte-to-byte提供了2个类： ByteArrayEncoder和ByteArrayDecoder。这两个类⽤来处理字节到字节的编码和解码。下⾯是这 两个类的源码，⼀看就知道是如何处理的：

[java] view plaincopy

publicclas ByteArrayDecoderextends MesageToMesageDecoder<ByteBuf> { @Override protectedvoid decode(ChanelHandlerContext ctx, ByteBuf msg, List<Object> out)throws Exception {

- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


/ copy the ByteBuf content to a byte array byte[] array =newbyte[msg.readableBytes()]; msg.getBytes(0, array);

out.ad(array); }

}

[java] view plaincopy

@Sharable publicclas ByteArrayEncoderextends MesageToMesageEncoder<byte[]> {

- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


@Override protectedvoid encode(ChanelHandlerContext ctx,byte[] msg, List<Object> out)throws Exception {

out.ad(Unpoled.wrapedBufer(msg); }

}

###### 7.4.2 ByteToMessageCodec

ByteToMessageCodec⽤来处理byte-to-message和message-to-byte。如果想要解码字节消 息成POJO或编码POJO消息成字节，对于这种情况，ByteToMessageCodec<I>是⼀个不错的选 择。ByteToMessageCodec是⼀种组合，其等同于ByteToMessageDecoder和 MessageToByteEncoder的组合。MessageToByteEncoder是个抽象类，其中有2个⽅法需要我们 ⾃⼰实现：

encode(ChannelHandlerContext, I, ByteBuf)，编码 decode(ChannelHandlerContext, ByteBuf, List<Object>)，解码

###### 7.4.3 MessageToMessageCodec

MessageToMessageCodec⽤于message-to-message的编码和解码，可以看成是 MessageToMessageDecoder和MessageToMessageEncoder的组合体。 MessageToMessageCodec是抽象类，其中有2个⽅法需要我们⾃⼰实现：

encode(ChannelHandlerContext, OUTBOUND_IN, List<Object>) decode(ChannelHandlerContext, INBOUND_IN, List<Object>)

但是，这种编解码器能有⽤吗？

有许多⽤例，最常见的就是需要将消息从⼀个API转到另⼀个API。这种情况下需要⾃定义 API或旧的API使⽤另⼀种消息类型。下⾯的代码显⽰了在WebSocket框架APIs之间转换消息：

[java] view plaincopy

package nety.in.action;

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


import java.util.List;

import io.nety.bufer.ByteBuf; import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.ChanelHandler.Sharable; import io.nety.handler.codec.MesageToMesageCodec; import io.nety.handler.codec.htp.websocketx.BinaryWebSocketFrame; import io.nety.handler.codec.htp.websocketx.CloseWebSocketFrame; import io.nety.handler.codec.htp.websocketx.ContinuationWebSocketFrame; import io.nety.handler.codec.htp.websocketx.PingWebSocketFrame; import io.nety.handler.codec.htp.websocketx.PongWebSocketFrame; import io.nety.handler.codec.htp.websocketx.TextWebSocketFrame; import io.nety.handler.codec.htp.websocketx.WebSocketFrame;

@Sharable publicclas WebSocketConvertHandlerextends

MesageToMesageCodec<WebSocketFrame, WebSocketConvertHandler.MyWebSocketFrame> {

publicstaticfinal WebSocketConvertHandler INSTANCE =new WebSocketConvertHandler();

@Override protectedvoid encode(ChanelHandlerContext ctx, MyWebSocketFrame msg, List<Object> out)throws Exceptio

n {

switch (msg.getType() { case BINARY:

out.ad(new BinaryWebSocketFrame(msg.getData( ); break;

case CLOSE: out.ad(new CloseWebSocketFrame(true,0, msg.getData( ); break; case PING: out.ad(new PingWebSocketFrame(msg.getData( ); break;

case PONG: out.ad(new PongWebSocketFrame(msg.getData( ); break; case TEXT: out.ad(new TextWebSocketFrame(msg.getData( ); break;

case CONTINUATION: out.ad(new ContinuationWebSocketFrame(msg.getData( ); break;

default:

thrownew IlegalStateException("Unsuported websocket msg " + msg); }

}

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
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.
- 88.
- 89.
- 90.
- 91.
- 92.
- 93.
- 94.
- 95.


@Override protectedvoid decode(ChanelHandlerContext ctx, WebSocketFrame msg, List<Object> out)throws Exception {

if (msginstanceof BinaryWebSocketFrame) { out.ad(new MyWebSocketFrame(MyWebSocketFrame.FrameType.BINARY, msg.content().copy( ); return;

} if (msginstanceof CloseWebSocketFrame) {

out.ad(new MyWebSocketFrame(MyWebSocketFrame.FrameType.CLOSE, msg.content().copy( ); return;

} if (msginstanceof PingWebSocketFrame) {

out.ad(new MyWebSocketFrame(MyWebSocketFrame.FrameType.PING, msg.content().copy( ); return;

} if (msginstanceof PongWebSocketFrame) {

out.ad(new MyWebSocketFrame(MyWebSocketFrame.FrameType.PONG, msg.content().copy( ); return;

} if (msginstanceof TextWebSocketFrame) {

out.ad(new MyWebSocketFrame(MyWebSocketFrame.FrameType.TEXT, msg.content().copy( ); return;

} if (msginstanceof ContinuationWebSocketFrame) {

out.ad(new MyWebSocketFrame(MyWebSocketFrame.FrameType.CONTINUATION, msg.content().copy( ); return;

} thrownew IlegalStateException("Unsuported websocket msg " + msg);

}

publicstaticfinalclas MyWebSocketFrame { publicenum FrameType {

BINARY, CLOSE, PING, PONG, TEXT, CONTINUATION }

privatefinal FrameType type; privatefinal ByteBuf data;

public MyWebSocketFrame(FrameType type, ByteBuf data) {

this.type = type; this.data = data;

}

public FrameType getType() {

return type; }

public ByteBuf getData() {

- 96.
- 97.
- 98.
- 99.
- 100.


return data; }

} }

##### 7.5 其他编解码⽅式

使⽤编解码器来充当编码器和解码器的组合失去了单独使⽤编码器或解码器的灵活性，编解 码器是要么都有要么都没有。你可能想知道是否有解决这个僵化问题的⽅式，还可以让编码器和 解码器在ChannelPipeline中作为⼀个逻辑单元。幸运的是，Netty提供了⼀种解决⽅案，使⽤ CombinedChannelDuplexHandler。虽然这个类不是编解码器API的⼀部分，但是它经常被⽤来简 历⼀个编解码器。

###### 7.5.1 CombinedChannelDuplexHandler

如何使⽤CombinedChannelDuplexHandler来结合解码器和编码器呢？下⾯我们从两个简单 的例⼦看了解。

[java] view plaincopy

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


/*

- *解码器，将byte转成char

- * @author c.k

*

- */ publicclas ByteToCharDecoderextends ByteToMesageDecoder {


@Override protectedvoid decode(ChanelHandlerContext ctx, ByteBuf in, List<Object> out)throws Exception {

while(in.readableBytes() >=2){

out.ad(Character.valueOf(in.readChar( ); }

}

}

[java] view plaincopy

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


/*

- *编码器，将char转成byte

- * @author Administrator

*

- */ publicclas CharToByteEncoderextends MesageToByteEncoder<Character> {


@Override protectedvoid encode(ChanelHandlerContext ctx, Character msg, ByteBuf out)throws Exception {

out.writeChar(msg); }

}

[java] view plaincopy

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


/*

- *继承CombinedChanelDuplexHandler，⽤于绑定解码器和编码器

- * @author c.k

*

- */ publicclas CharCodecextends CombinedChanelDuplexHandler<ByteToCharDecoder, CharToByteEncoder> {


public CharCodec(){

super(new ByteToCharDecoder(),new CharToByteEncoder(); }

}

从上⾯代码可以看出，使⽤CombinedChannelDuplexHandler绑定解码器和编码器很容易实 现，⽐使⽤*Codec更灵活。

Netty还提供了其他的协议⽀持，放在io.netty.handler.codec包下，如：

Google的protobuf，在io.netty.handler.codec.protobuf包下 Google的SPDY协议 RTSP(Real Time Streaming Protocol，实时流传输协议)，在io.netty.handler.codec.rtsp包下 SCTP(Stream Control Transmission Protocol，流控制传输协议)，在 io.netty.handler.codec.sctp包下

......

第⼋章：附带的ChanelHandler和Codec

本章介绍

使⽤SSL/TLS创建安全的Netty程序 使⽤Netty创建HTTP/HTTPS程序 处理空闲连接和超时 解码分隔符和基于长度的协议 写⼤数据 序列化数据

上⼀章讲解了如何创建⾃⼰的编解码器，我们现在可以⽤上⼀章的知识来编写⾃⼰的编解码器。 不过Netty提供了⼀些标准的ChannelHandler和Codec。Netty提供了很多协议的⽀持，所以我们不必⾃ ⼰发明轮⼦。Netty提供的这些实现可以解决我们的⼤部分需求。本章讲解Netty中使⽤SSL/TLS编写安 全的应⽤程序，编写HTTP协议服务器，以及使⽤如WebSocket或Google的SPDY协议来使HTTP服务 获得更好的性能；这些都是很常见的应⽤，本章还会介绍数据压缩，在数据量⽐较⼤的时候，压缩数 据是很有必要的。

##### 8.1 使⽤SSL/TLS创建安全的Netty程序

通信数据在⽹络上传输⼀般是不安全的，因为传输的数据可以发送纯⽂本或⼆进制的数据，

很容易被破解。我们很有必要对⽹络上的数据进⾏加密。SSL和TLS是众所周知的标准和分层的协 议，它们可以确保数据时私有的。例如，使⽤HTTPS或SMTPS都使⽤了SSL/TLS对数据进⾏了加 密。

对于SSL/TLS，Java中提供了抽象的SslContext和SslEngine。实际上，SslContext可以⽤来 获取SslEngine来进⾏加密和解密。使⽤指定的加密技术是⾼度可配置的，但是这不在本章范围。 Netty扩展了Java的SslEngine，添加了⼀些新功能，使其更适合基于Netty的应⽤程序。Netty提供 的这个扩展是SslHandler，是SslEngine的包装类，⽤来对⽹络数据进⾏加密和解密。

下图显⽰SslHandler实现的数据流：

![image 58](assets/imageFile58.png)

上图显⽰了如何使⽤ChannelInitializer将SslHandler添加到ChannelPipeline，看下⾯代码：

###### [java] view plaincopy

![image 59](assets/imageFile59.png)

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


publicclas SslChanelInitializerextends ChanelInitializer<Chanel> {

privatefinal SLContext context; privatefinalbolean client; privatefinalbolean startTls;

public SslChanelInitializer(SLContext context,bolean client,bolean startTls) { this.context = context; this.client = client; this.startTls = startTls;

}

@Override protectedvoid initChanel(Chanel ch)throws Exception {

SLEngine engine = context.createSLEngine(); engine.setUseClientMode(client); ch.pipeline().adFirst("sl",new SslHandler(engine, startTls);

} }

需要注意⼀点，SslHandler必须要添加到ChannelPipeline的第⼀个位置，可能有⼀些例外， 但是最好这样来做。回想⼀下之前讲解的ChannelHandler，ChannelPipeline就像是⼀个在处理“⼊ 站”数据时先进先出，在处理“出站”数据时后进先出的队列。最先添加的SslHandler会啊在其他 Handler处理逻辑数据之前对数据进⾏加密，从⽽确保Netty服务端的所有的Handler的变化都是安 全的。

SslHandler提供了⼀些有⽤的⽅法，可以⽤来修改其⾏为或得到通知，⼀旦SSL/TLS完成握 ⼿(在握⼿过程中的两个对等通道互相验证对⽅，然后选择⼀个加密密码)，SSL/TLS是⾃动执⾏ 的。看下⾯⽅法列表：

setHandshakeTimeout(long handshakeTimeout, TimeUnit unit)，设置握⼿超时时间， ChannelFuture将得到通知 setHandshakeTimeoutMillis(long handshakeTimeoutMillis)，设置握⼿超时时间， ChannelFuture将得到通知 getHandshakeTimeoutMillis()，获取握⼿超时时间值 setCloseNotifyTimeout(long closeNotifyTimeout, TimeUnit unit)，设置关闭通知超时时间，若 超时，ChannelFuture会关闭失败 setHandshakeTimeoutMillis(long handshakeTimeoutMillis)，设置关闭通知超时时间，若超 时，ChannelFuture会关闭失败

getCloseNotifyTimeoutMillis()，获取关闭通知超时时间 handshakeFuture()，返回完成握⼿后的ChannelFuture close()，发送关闭通知请求关闭和销毁

#### 8.2 使⽤Netty创建HTTP/HTTPS程序

HTTP/HTTPS是最常⽤的协议之⼀，可以通过HTTP/HTTPS访问⽹站，或者是提供对外公开 的接⼜服务等等。Netty附带了使⽤HTTP/HTTPS的handlers，⽽不需要我们⾃⼰来编写编解码 器。

- 8.2.1 Netty的HTTP编码器，解码器和编解码器


HTTP是请求-响应模式，客户端发送⼀个http请求，服务就响应此请求。Netty提供了简单的 编码解码HTTP协议消息的Handler。下图显⽰了http请求和响应：

![image 60](assets/imageFile60.png)

![image 61](assets/imageFile61.png)

如上⾯两个图所⽰，⼀个HTTP请求/响应消息可能包含不⽌⼀个，但最终都会有LastHttpContent 消息。FullHttpRequest和FullHttpResponse是Netty提供的两个接⼜，分别⽤来完成http请求和响 应。所有的HTTP消息类型都实现了HttpObject接⼜。下⾯是类关系图：

![image 62](assets/imageFile62.png)

Netty提供了HTTP请求和响应的编码器和解码器，看下⾯列表：

HttpRequestEncoder，将HttpRequest或HttpContent编码成ByteBuf HttpRequestDecoder，将ByteBuf解码成HttpRequest和HttpContent HttpResponseEncoder，将HttpResponse或HttpContent编码成ByteBuf HttpResponseDecoder，将ByteBuf解码成HttpResponse和HttpContent

看下⾯代码：

[java] view plaincopy

![image 63](assets/imageFile63.png)

publicclas HtpDecoderEncoderInitializerextends ChanelInitializer<Chanel> {

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


privatefinalbolean client;

public HtpDecoderEncoderInitializer(bolean client) {

this.client = client; }

@Override protectedvoid initChanel(Chanel ch)throws Exception {

ChanelPipeline pipeline = ch.pipeline(); if (client) {

pipeline.adLast("decoder",new HtpResponseDecoder(); pipeline.adLast(",new HtpRequestEncoder();

}else { pipeline.adLast("decoder",new HtpRequestDecoder(); pipeline.adLast("encoder",new HtpResponseEncoder();

} }

}

如果你需要在ChannelPipeline中有⼀个解码器和编码器，还分别有⼀个在客户端和服务器简 单的编解码器：HttpClientCodec和HttpServerCodec。

在ChannelPipelien中有解码器和编码器(或编解码器)后就可以操作不同的HttpObject消息 了；但是HTTP请求和响应可以有很多消息数据，你需要处理不同的部分，可能也需要聚合这些消 息数据，这是很⿇烦的。为了解决这个问题，Netty提供了⼀个聚合器，它将消息部分合并到 FullHttpRequest和FullHttpResponse，因此不需要担⼼接收碎⽚消息数据。

###### 8.2.2 HTTP消息聚合

处理HTTP时可能接收HTTP消息⽚段，Netty需要缓冲直到接收完整个消息。要完成的处理 HTTP消息，并且内存开销也不会很⼤，Netty为此提供了HttpObjectAggregator。通过 HttpObjectAggregator，Netty可以聚合HTTP消息，使⽤FullHttpResponse和FullHttpRequest到 ChannelPipeline中的下⼀个ChannelHandler，这就消除了断裂消息，保证了消息的完整。下⾯代 码显⽰了如何聚合：

[java] view plaincopy

![image 64](assets/imageFile64.png)

- *添加聚合htp消息的Handler

*

- * @author c.k

*

- */ publicclas HtpAgregatorInitializerextends ChanelInitializer<Chanel> {


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


privatefinalbolean client;

public HtpAgregatorInitializer(bolean client) {

this.client = client; }

@Override protectedvoid initChanel(Chanel ch)throws Exception {

ChanelPipeline pipeline = ch.pipeline(); if (client) {

pipeline.adLast("codec",new HtpClientCodec(); }else {

pipeline.adLast("codec",new HtpServerCodec();

} pipeline.adLast("agegator",new HtpObjectAgregator(512 *1024);

}

}

如上⾯代码，很容使⽤Netty⾃动聚合消息。但是请注意，为了防⽌Dos攻击服务器，需要合 理的限制消息的⼤⼩。应设置多⼤取决于实际的需求，当然也得有⾜够的内存可⽤。

###### 8.2.3 HTTP压缩

使⽤HTTP时建议压缩数据以减少传输流量，压缩数据会增加CPU负载，现在的硬件设施都 很强⼤，⼤多数时候压缩数据时⼀个好主意。Netty⽀持“gzip”和“deflate”，为此提供了两个 ChannelHandler实现分别⽤于压缩和解压。看下⾯代码：

[java] view plaincopy

![image 65](assets/imageFile65.png)

@Override protectedvoid initChanel(Chanel ch)throws Exception {

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


ChanelPipeline pipeline = ch.pipeline(); if (client) {

pipeline.adLast("codec",new HtpClientCodec(); /添加解压缩Handler

pipeline.adLast("decompresor",new HtpContentDecompresor(); }else {

pipeline.adLast("codec",new HtpServerCodec(); /添加解压缩Handler pipeline.adLast("decompresor",new HtpContentDecompresor();

} pipeline.adLast("agegator",new HtpObjectAgregator(512 *1024);

}

###### 8.2.4 使⽤HTTPS

⽹络中传输的重要数据需要加密来保护，使⽤Netty提供的SslHandler可以很容易实现，看下 ⾯代码：

[java] view plaincopy

![image 66](assets/imageFile66.png)

- *使⽤ SL对HTP消息加密

*

- * @author c.k

*

- */ publicclas HtpsCodecInitializerextends ChanelInitializer<Chanel> {


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


privatefinal SLContext context; privatefinalbolean client;

public HtpsCodecInitializer(SLContext context,bolean client) { this.context = context; this.client = client;

}

@Override protectedvoid initChanel(Chanel ch)throws Exception {

SLEngine engine = context.createSLEngine(); engine.setUseClientMode(client); ChanelPipeline pipeline = ch.pipeline(); pipeline.adFirst("sl",new SslHandler(engine); if (client) {

pipeline.adLast("codec",new HtpClientCodec(); }else {

pipeline.adLast("codec",new HtpServerCodec(); }

}

}

###### 8.2.5 WebSocket

HTTP是不错的协议，但是如果需要实时发布信息怎么做？有个做法就是客户端⼀直轮询请求 服务器，这种⽅式虽然可以达到⽬的，但是其缺点很多，也不是优秀的解决⽅案，为了解决这个 问题，便出现了WebSocket。

WebSocket允许数据双向传输，⽽不需要请求-响应模式。早期的WebSocket只能发送⽂本数 据，然后现在不仅可以发送⽂本数据，也可以发送⼆进制数据，这使得可以使⽤WebSocket构建 你想要的程序。下图是WebSocket的通信⽰例图：

![image 67](assets/imageFile67.png)

在应⽤程序中添加WebSocket⽀持很容易，Netty附带了WebSocket的⽀持，通过 ChannelHandler来实现。使⽤WebSocket有不同的消息类型需要处理。下⾯列表列出了Netty中 WebSocket类型：

BinaryWebSocketFrame，包含⼆进制数据 TextWebSocketFrame，包含⽂本数据 ContinuationWebSocketFrame，包含⼆进制数据或⽂本数据，BinaryWebSocketFrame和 TextWebSocketFrame的结合体 CloseWebSocketFrame，WebSocketFrame代表⼀个关闭请求，包含关闭状态码和短语 PingWebSocketFrame，WebSocketFrame要求PongWebSocketFrame发送数据 PongWebSocketFrame，WebSocketFrame要求PingWebSocketFrame响应

为了简化，我们只看看如何使⽤WebSocket服务器。客户端使⽤可以看Netty⾃带的 WebSocket例⼦。

Netty提供了许多⽅法来使⽤WebSocket，但最简单常⽤的⽅法是使⽤ WebSocketServerProtocolHandler。看下⾯代码：

[java] view plaincopy

![image 68](assets/imageFile68.png)

- * WebSocket Server，若想使⽤ SL加密，将SslHandler加载ChanelPipeline的最前⾯即可

- * @author c.k

*

- */ publicclas WebSocketServerInitializerextends ChanelInitializer<Chanel> {


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
- 37.
- 38.


@Override protectedvoid initChanel(Chanel ch)throws Exception {

ch.pipeline().adLast(new HtpServerCodec(), new HtpObjectAgregator(6536), new WebSocketServerProtocolHandler("/websocket"), new TextFrameHandler(), new BinaryFrameHandler(), new ContinuationFrameHandler();

}

publicstaticfinalclas TextFrameHandlerextends SimpleChanelInboundHandler<TextWebSocketFrame> { @Override protectedvoid chanelRead0(ChanelHandlerContext ctx, TextWebSocketFrame msg)throws Exception {

/ handler text frame }

}

publicstaticfinalclas BinaryFrameHandlerextends SimpleChanelInboundHandler<BinaryWebSocketFrame> {

@Override protectedvoid chanelRead0(ChanelHandlerContext ctx, BinaryWebSocketFrame msg)throws Exception {

/handler binary frame }

}

publicstaticfinalclas ContinuationFrameHandlerextends SimpleChanelInboundHandler<ContinuationWebSo

cketFrame>{ @Override protectedvoid chanelRead0(ChanelHandlerContext ctx, ContinuationWebSocketFrame msg)throws Excepti

on {

/handler continuation frame }

} }

- 8.2.6 SPDY


SPDY（读作“SPeeDY”）是Google开发的基于TCP的应⽤层协议，⽤以最⼩化⽹络延迟，提 升⽹络速度，优化⽤户的⽹络使⽤体验。SPDY并不是⼀种⽤于替代HTTP的协议，⽽是对HTTP 协议的增强。新协议的功能包括数据流的多路复⽤、请求优先级以及HTTP报头压缩。⾕歌表⽰， 引⼊SPDY协议后，在实验室测试中页⾯加载速度⽐原先快64%。

SPDY的定位：

将页⾯加载时间减少50%。 最⼤限度地减少部署的复杂性。SPDY使⽤TCP作为传输层，因此⽆需改变现有的⽹络设施。 避免⽹站开发者改动内容。⽀持SPDY唯⼀需要变化的是客户端代理和Web服务器应⽤程序。

SPDY实现技术：

单个TCP连接⽀持并发的HTTP请求。 压缩报头和去掉不必要的头部来减少当前HTTP使⽤的带宽。 定义⼀个容易实现，在服务器端⾼效率的协议。通过减少边缘情况、定义易解析的消息格式来 减少HTTP的复杂性。 强制使⽤SSL，让SSL协议在现存的⽹络设施下有更好的安全性和兼容性。 允许服务器在需要时发起对客户端的连接并推送数据。

SPDY具体的细节知识及使⽤可以查阅相关资料，这⾥不作赘述了。

##### 8.3 处理空闲连接和超时

处理空闲连接和超时是⽹络应⽤程序的核⼼部分。当发送⼀条消息后，可以检测连接是否还 处于活跃状态，若很长时间没⽤了就可以断开连接。Netty提供了很好的解决⽅案，有三种不同的 ChannelHandler处理闲置和超时连接：

IdleStateHandler，当⼀个通道没有进⾏读写或运⾏了⼀段时间后出发IdleStateEvent ReadTimeoutHandler，在指定时间内没有接收到任何数据将抛出ReadTimeoutException WriteTimeoutHandler，在指定时间内有写⼊数据将抛出WriteTimeoutException

最常⽤的是IdleStateHandler，下⾯代码显⽰了如何使⽤IdleStateHandler，如果60秒内没有接收 数据或发送数据，操作将失败，连接将关闭：

[java] view plaincopy

![image 69](assets/imageFile69.png)

publicclas IdleStateHandlerInitializerextends ChanelInitializer<Chanel> {

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


@Override protectedvoid initChanel(Chanel ch)throws Exception {

ChanelPipeline pipeline = ch.pipeline(); pipeline.adLast(new IdleStateHandler(0,0,60, TimeUnit.SECONDS); pipeline.adLast(new HeartbeatHandler();

}

publicstaticfinalclas HeartbeatHandlerextends ChanelInboundHandlerAdapter { privatestaticfinal ByteBuf HEARTBEAT_SEQUENCE = Unpoled.unreleasableBufer(Unpoled.copiedBufer( "HEARTBEAT", CharsetUtil.UTF_8);

@Override publicvoid userEventTri gered(ChanelHandlerContext ctx, Object evt)throws Exception {

if (evtinstanceof IdleStateEvent) {

ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate().adListener(ChanelFutureListener.CLOSE_ON_FA ILURE);

}else {

super.userEventTri gered(ctx, evt); }

} }

}

##### 8.4 解码分隔符和基于长度的协议

使⽤Netty时会遇到需要解码以分隔符和长度为基础的协议，本节讲解Netty如何解码这些协 议。

- 8.4.1 分隔符协议


经常需要处理分隔符协议或创建基于它们的协议，例如SMTP、POP3、IMAP、Telnet等等； Netty附带的handlers可以很容易的提取⼀些序列分隔：

DelimiterBasedFrameDecoder，解码器，接收ByteBuf由⼀个或多个分隔符拆分，如NUL或换 ⾏符 LineBasedFrameDecoder，解码器，接收ByteBuf以分割线结束，如"\n"和"\r\n"

下图显⽰了使⽤"\r\n"分隔符的处理：

![image 70](assets/imageFile70.png)

###### 下⾯代码显⽰使⽤LineBasedFrameDecoder提取"\r\n"分隔帧：

[java] view plaincopy

![image 71](assets/imageFile71.png)

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


/*

- *处理换⾏分隔符消息

- * @author c.k

*

- */ publicclas LineBasedHandlerInitializerextends ChanelInitializer<Chanel> {


@Override protectedvoid initChanel(Chanel ch)throws Exception {

ch.pipeline().adLast(new LineBasedFrameDecoder(65 *1204),new FrameHandler(); }

publicstaticfinalclas FrameHandlerextends SimpleChanelInboundHandler<ByteBuf> { @Override protectedvoid chanelRead0(ChanelHandlerContext ctx, ByteBuf msg)throws Exception {

/ do something with the frame }

} }

如果框架的东西除了换⾏符还有别的分隔符，可以使⽤DelimiterBasedFrameDecoder，只需 要将分隔符传递到构造⽅法中。如果想实现⾃⼰的以分隔符为基础的协议，这些解码器是有⽤ 的。例如，现在有个协议，它只处理命令，这些命令由名称和参数形成，名称和参数由⼀个空格 分隔，实现这个需求的代码如下：

[java] view plaincopy

![image 72](assets/imageFile72.png)

/*

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


- *⾃定义以分隔符为基础的协议

- * @author c.k

*

- */ publicclas CmdHandlerInitializerextends ChanelInitializer<Chanel> {


@Override protectedvoid initChanel(Chanel ch)throws Exception {

ch.pipeline().adLast(new CmdDecoder(65 *1024),new CmdHandler(); }

publicstaticfinalclas Cmd { privatefinal ByteBuf name; privatefinal ByteBuf args;

public Cmd(ByteBuf name, ByteBuf args) { this.name = name; this.args = args;

}

public ByteBuf getName() {

return name; }

public ByteBuf getArgs() {

return args; }

}

publicstaticfinalclas CmdDecoderextends LineBasedFrameDecoder {

public CmdDecoder(int maxLength) {

super(maxLength); }

@Override protected Object decode(ChanelHandlerContext ctx, ByteBuf bufer)throws Exception {

ByteBuf frame = (ByteBuf)super.decode(ctx, bufer); if (frame =nul) {

returnnul;

} int index = frame.indexOf(frame.readerIndex(), frame.writerIndex(), (byte)' '); returnnew Cmd(frame.slice(frame.readerIndex(), index), frame.slice(index +1, frame.writerIndex( );

} }

publicstaticfinalclas CmdHandlerextends SimpleChanelInboundHandler<Cmd> {

- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.


@Override protectedvoid chanelRead0(ChanelHandlerContext ctx, Cmd msg)throws Exception {

/ do something with the co mand }

}

}

- 8.4.2 长度为基础的协议


⼀般经常会碰到以长度为基础的协议，对于这种情况Netty有两个不同的解码器可以帮助我们 来解码：

FixedLengthFrameDecoder LengthFieldBasedFrameDecoder

下图显⽰了FixedLengthFrameDecoder的处理流程：

![image 73](assets/imageFile73.png)

如上图所⽰，FixedLengthFrameDecoder提取固定长度，例⼦中的是8字节。⼤部分时候帧的⼤⼩ 被编码在头部，这种情况可以使⽤LengthFieldBasedFrameDecoder，它会读取头部长度并提取帧 的长度。下图显⽰了它是如何⼯作的：

![image 74](assets/imageFile74.png)

如果长度字段是提取框架的⼀部分，可以在LengthFieldBasedFrameDecoder的构造⽅法中 配置，还可以指定提供的长度。FixedLengthFrameDecoder很容易使⽤，我们重点讲解 LengthFieldBasedFrameDecoder。下⾯代码显⽰如何使⽤LengthFieldBasedFrameDecoder提取 8字节长度：

[java] view plaincopy

![image 75](assets/imageFile75.png)

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


publicclas LengthBasedInitializerextends ChanelInitializer<Chanel> {

@Override protectedvoid initChanel(Chanel ch)throws Exception {

ch.pipeline().adLast(new LengthFieldBasedFrameDecoder(65*1024,0,8)

.adLast(new FrameHandler(); }

publicstaticfinalclas FrameHandlerextends SimpleChanelInboundHandler<ByteBuf>{ @Override protectedvoid chanelRead0(ChanelHandlerContext ctx, ByteBuf msg)throws Exception {

/do something with the frame }

} }

##### 8.5 写⼤数据

写⼤量的数据的⼀个有效的⽅法是使⽤异步框架，如果内存和⽹络都处于饱满负荷状态，你 需要停⽌写，否则会报OutOfMemoryError。Netty提供了写⽂件内容时zero-memory-copy机制， 这种⽅法再将⽂件内容写到⽹络堆栈空间时可以获得最⼤的性能。使⽤零拷贝写⽂件的内容时通 过DefaultFileRegion、ChannelHandlerContext、ChannelPipeline，看下⾯代码：

[java] view plaincopy

![image 76](assets/imageFile76.png)

@Override publicvoid chanelRead(ChanelHandlerContext ctx, Object msg)throws Exception {

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


File file =new File("test.txt"); FileInputStream fis =new FileInputStream(file); FileRegion region =new DefaultFileRegion(fis.getChanel(),0, file.length(); Chanel chanel = ctx.chanel(); chanel.writeAndFlush(region).adListener(new ChanelFutureListener() {

@Override publicvoid operationComplete(ChanelFuture future)throws Exception {

if(!future.isSuces(){ Throwable cause = future.cause();

/ do something }

} });

}

如果只想发送⽂件中指定的数据块应该怎么做呢？Netty提供了ChunkedWriteHandler，允许 通过处理ChunkedInput来写⼤的数据块。下⾯是ChunkedInput的⼀些实现类：

ChunkedFile ChunkedNioFile ChunkedStream ChunkedNioStream

看下⾯代码：

[java] view plaincopy

![image 77](assets/imageFile77.png)

publicclas ChunkedWriteHandlerInitializerextends ChanelInitializer<Chanel> { privatefinal File file;

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


public ChunkedWriteHandlerInitializer(File file) {

this.file = file; }

@Override protectedvoid initChanel(Chanel ch)throws Exception {

ch.pipeline().adLast(new ChunkedWriteHandler()

.adLast(new WriteStreamHandler(); }

publicfinalclas WriteStreamHandlerextends ChanelInboundHandlerAdapter { @Override publicvoid chanelActive(ChanelHandlerContext ctx)throws Exception {

super.chanelActive(ctx); ctx.writeAndFlush(new ChunkedStream(new FileInputStream(file);

} }

}

##### 8.6 序列化数据

开发⽹络程序过程中，很多时候需要传输结构化对象数据POJO,Java中提供了 ObjectInputStream和ObjectOutputStream及其他的⼀些对象序列化接⼜。Netty中提供基于JDK序 列化接⼜的序列化接⼜。

- 8.6.1 普通的JDK序列化

如果你使⽤ObjectInputStream和ObjectOutputStream，并且需要保持兼容性，不想有外部依 赖，那么JDK的序列化是⾸选。Netty提供了下⾯的⼀些接⼜，这些接⼜放在 io.netty.handler.codec.serialization包下⾯：

- 8.6.2 通过JBoss编组序列化


CompatibleObjectEncoder CompactObjectInputStream CompactObjectOutputStream ObjectEncoder ObjectDecoder ObjectEncoderOutputStream ObjectDecoderInputStream

如果你想使⽤外部依赖的接⼜，JBoss编组是个好⽅法。JBoss Marshalling序列化的速度是 JDK的3倍，并且序列化的结构更紧凑，从⽽使序列化后的数据更⼩。Netty附带了JBoss编组序列 化的实现，这些实现接⼜放在io.netty.handler.codec.marshalling包下⾯：

CompatibleMarshallingEncoder CompatibleMarshallingDecoder MarshallingEncoder MarshallingDecoder

看下⾯代码：

[java] view plaincopy

![image 78](assets/imageFile78.png)

- *使⽤JBos Marshaling

- * @author c.k

*

- */ publicclas MarshalingInitializerextends ChanelInitializer<Chanel> {


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


privatefinal MarshalerProvider marshalerProvider; privatefinal UnmarshalerProvider unmarshalerProvider;

public MarshalingInitializer(MarshalerProvider marshalerProvider, UnmarshalerProvider unmarshalerProvider) { this.marshalerProvider = marshalerProvider; this.unmarshalerProvider = unmarshalerProvider;

}

@Override protectedvoid initChanel(Chanel ch)throws Exception {

ch.pipeline().adLast(new MarshalingDecoder(unmarshalerProvider)

.adLast(new MarshalingEncoder(marshalerProvider)

.adLast(new ObjectHandler(); }

publicfinalclas ObjectHandlerextends SimpleChanelInboundHandler<Serializable> { @Override protectedvoid chanelRead0(ChanelHandlerContext ctx, Serializable msg)throws Exception {

/ do something }

} }

###### 8.6.3 使⽤ProtoBuf序列化

最有⼀个序列化⽅案是Netty附带的ProtoBuf。protobuf是Google开源的⼀种编码和解码技 术，它的作⽤是使序列化数据更⾼效。并且⾕歌提供了protobuf的不同语⾔的实现，所以protobuf 在跨平台项⽬中是⾮常好的选择。Netty附带的protobuf放在io.netty.handler.codec.protobuf包下 ⾯：

ProtobufDecoder ProtobufEncoder ProtobufVarint32FrameDecoder ProtobufVarint32LengthFieldPrepender

看下⾯代码：

[java] view plaincopy

![image 79](assets/imageFile79.png)

- *使⽤protobuf序列化数据，进⾏编码解码

- *注意：使⽤protobuf需要protobuf-java-2.5.0.jar

- * @author Administrator

*

- */ publicclas ProtoBufInitializerextends ChanelInitializer<Chanel> {


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


privatefinal MesageLite lite;

public ProtoBufInitializer(MesageLite lite) {

this.lite = lite; }

@Override protectedvoid initChanel(Chanel ch)throws Exception {

ch.pipeline().adLast(new ProtobufVarint32FrameDecoder()

.adLast(new ProtobufEncoder()

.adLast(new ProtobufDecoder(lite)

.adLast(new ObjectHandler(); }

publicfinalclas ObjectHandlerextends SimpleChanelInboundHandler<Serializable> { @Override protectedvoid chanelRead0(ChanelHandlerContext ctx, Serializable msg)throws Exception {

/ do something }

} }

### 第九章：引导Nety应⽤程序

本章介绍

引导客户端和服务器 从Channel引导客户端 添加多个ChannelHandler 使⽤通道选项和属性

上⼀章学习了编写⾃⼰的ChannelHandler和编解码器并将它们添加到Channel的ChannelPipeline 中。本章将讲解如何将它们结合在⼀起使⽤。

Netty提供了简单统⼀的⽅法来引导服务器和客户端。引导是配置Netty服务器和客户端程序 的⼀个过程，Bootstrap允许这些应⽤程序很容易的重复使⽤。Netty程序的客户端和服务器都可以 使⽤Bootstrap，其⽬的是简化编码过程，Bootstrap还提供了⼀个机制就是让⼀些组件 (channels,pipeline,handlers等等)都可以在后台⼯作。本章将具体结合以下部分⼀起使⽤开发Netty 程序：

EventLoopGroup Channel 设置ChannelOption Channel被注册后将调⽤ChannelHandler 添加指定的属性到Channel 设置本地和远程地址 绑定、连接(取决于类型)

知道如何使⽤各个Bootstrap后就可以使⽤它们配置服务器和客户端了。本章还将学习在什么会后 可以共享⼀个Bootstrap以及为什么这样做，结合我们之前学习的知识点来编写Netty程序。

##### 9.1 不同的引导类型

Netty包含了2个不同类型的引导，第⼀个是使⽤服务器的ServerBootstrap，⽤来接受客户端 连接以及为已接受的连接创建⼦通道；第⼆个是⽤于客户端的Bootstrap，不接受新的连接，并且 是在⽗通道类完成⼀些操作。

还有⼀种情况是处理DatagramChannel实例，这些⽤于UDP协议，是⽆连接的。换句话说， 由于UDP的性质，所以当处理UDP数据时没有必要每个连接通道与TCP连接⼀样。因为通道不需 要连接后才能发送数据，UDP是⽆连接协议。⼀个通道可以处理所有的数据⽽不需要依赖⼦通 道。

下图是引导的类关系图：

![image 80](assets/imageFile80.png)

我们在前⾯讨论了许多⽤于客户端和服务器的知识，为了对客户端和服务器之间的关系提供 了⼀个共同点，Netty使⽤AbstractBootstrap类。通过⼀个共同的⽗类，在本章中讨论的客户端和 服务器的引导程序能够重复使⽤通⽤功能，⽽⽆需复制代码或逻辑。通常情况下，多个通道使⽤ 相同或⾮常类似的设置时有必要的。⽽不是为每⼀个通道创建⼀个新的引导，Netty使得 AbstractBootstrap可复制。也就是说克隆⼀个已配置的引导，其返回的是⼀个可重⽤⽽⽆需配置 的引导。Netty的克隆操作只能浅拷贝引导的EventLoopGroup，也就是说EventLoopGroup在所有 的克隆的通道中是共享的。这是⼀个好事情，克隆的通道⼀般是短暂的，例如⼀个通道创建⼀个 HTTP请求。

本章主要讲解Bootstrap和ServerBootstrap，⾸先我们来看看ServerBootstrap。

##### 9.2 引导客户端和⽆连接协议

当需要引导客户端或⼀些⽆连接协议时，需要使⽤Bootstrap类。

- 9.2.1 引导客户端的⽅法

创建Bootstrap实例使⽤new关键字，下⾯是Bootstrap的⽅法：

- 9.2.2 怎么引导客户端


group(...)，设置EventLoopGroup,EventLoopGroup⽤来处理所有通道的IO事件 channel(...)，设置通道类型 channelFactory(...)，使⽤ChannelFactory来设置通道类型 localAddress(...)，设置本地地址，也可以通过bind(...)或connect(...) option(ChannelOption<T>, T)，设置通道选项，若使⽤null，则删除上⼀个设置的 ChannelOption attr(AttributeKey<T>, T)，设置属性到Channel，若值为null，则指定键的属性被删除 handler(ChannelHandler)，设置ChannelHandler⽤于处理请求事件 clone()，深度复制Bootstrap，Bootstrap的配置相同 remoteAddress(...)，设置连接地址 connect(...)，连接远程通道 bind(...)，创建⼀个新的Channel并绑定

引导负责客户端通道连接或断开连接，因此它将在调⽤bind(...)或connect(...)后创建通道。下 图显⽰了如何⼯作：

![image 81](assets/imageFile81.png)

###### 下⾯代码显⽰了引导客户端使⽤NIO TCP传输：

[html] view plaincopy

![image 82](assets/imageFile82.png)

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
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.


package nety.in.action;

import io.nety.botstrap.Botstrap; import io.nety.bufer.ByteBuf; import io.nety.chanel.ChanelFuture; import io.nety.chanel.ChanelFutureListener; import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.EventLopGroup; import io.nety.chanel.SimpleChanelInboundHandler; import io.nety.chanel.nio.NioEventLopGroup; import io.nety.chanel.socket.nio.NioSocketChanel;

/*

- *引导配置客户端

*

- * @author c.k

*

- */ public clas BotstrapingClient {


public static void main(String[] args) throws Exception { EventLopGroupgroup =new NioEventLopGroup(); Botstrapb =new Botstrap(); b.group(group).chanel(NioSocketChanel.clas).handler(new SimpleChanelInboundHandler<ByteBuf>() {

@Override protected void chanelRead0(ChanelHandlerContext ctx, ByteBuf msg) throws Exception {

System.out.println("Received data"); msg.clear();

}

}); ChanelFuturef =b.conect("127.0.0.1", 2048); f.adListener(new ChanelFutureListener() {

@Override public void operationComplete(ChanelFuture future) throws Exception {

if (future.isSuces() { System.out.println("conection finished");

} else { System.out.println("conection failed"); future.cause().printStackTrace();

} }

}); }

}

- 9.2.3 选择兼容通道实现


Channel的实现和EventLoop的处理过程在EventLoopGroup中必须兼容，哪些Channel是和 EventLoopGroup是兼容的可以查看API⽂档。经验显⽰，相兼容的实现⼀般在同⼀个包下⾯，例 如使⽤NioEventLoop，NioEventLoopGroup和NioServerSocketChannel在⼀起。请注意，这些都 是前缀“Nio”，然后不会⽤这些代替另⼀个实现和另⼀个前缀，如“Oio”，也就是说 OioEventLoopGroup和NioServerSocketChannel是不相容的。

Channel和EventLoopGroup的EventLoop必须相容，例如NioEventLoop、 NioEventLoopGroup、NioServerSocketChannel是相容的，但是OioEventLoopGroup和 NioServerSocketChannel是不相容的。从类名可以看出前缀是“Nio”的只能和“Nio”的⼀起使⽤， “Oio”前缀的只能和Oio*⼀起使⽤，将不相容的⼀起使⽤会导致错误异常，如OioSocketChannel和 NioEventLoopGroup⼀起使⽤时会抛出异常：Exception in thread "main" java.lang.IllegalStateException: incompatible event loop type。

#### 9.3 使⽤ServerBootstrap引导服务器

- 9.3.1 引导服务器的⽅法

先看看ServerBootstrap提供了哪些⽅法

- 9.3.2 怎么引导服务器


group(...)，设置EventLoopGroup事件循环组 channel(...)，设置通道类型 channelFactory(...)，使⽤ChannelFactory来设置通道类型 localAddress(...)，设置本地地址，也可以通过bind(...)或connect(...) option(ChannelOption<T>, T)，设置通道选项，若使⽤null，则删除上⼀个设置的 ChannelOption childOption(ChannelOption<T>, T)，设置⼦通道选项 attr(AttributeKey<T>, T)，设置属性到Channel，若值为null，则指定键的属性被删除 childAttr(AttributeKey<T>, T)，设置⼦通道属性 handler(ChannelHandler)，设置ChannelHandler⽤于处理请求事件 childHandler(ChannelHandler)，设置⼦ChannelHandler clone()，深度复制ServerBootstrap，且配置相同 bind(...)，创建⼀个新的Channel并绑定

下图显⽰ServerBootstrap管理⼦通道：

![image 83](assets/imageFile83.png)

child*⽅法是在⼦Channel上操作，通过ServerChannel来管理。

下⾯代码显⽰使⽤ServerBootstrap引导配置服务器：

[java] view plaincopy

![image 84](assets/imageFile84.png)

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
- 37.
- 38.
- 39.
- 40.
- 41.


package nety.in.action;

import io.nety.botstrap.ServerBotstrap; import io.nety.bufer.ByteBuf; import io.nety.chanel.ChanelFuture; import io.nety.chanel.ChanelFutureListener; import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.EventLopGroup; import io.nety.chanel.SimpleChanelInboundHandler; import io.nety.chanel.nio.NioEventLopGroup; import io.nety.chanel.socket.nio.NioServerSocketChanel;

/*

- *引导服务器配置

- * @author c.k

*

- */ publicclas BotstrapingServer {


publicstaticvoid main(String[] args)throws Exception { EventLopGroup bosGroup =new NioEventLopGroup(1); EventLopGroup workerGroup =new NioEventLopGroup(); ServerBotstrap b =new ServerBotstrap(); b.group(bosGroup, workerGroup).chanel(NioServerSocketChanel.clas)

.childHandler(new SimpleChanelInboundHandler<ByteBuf>() {

@Override

protectedvoid chanelRead0(ChanelHandlerContext ctx, ByteBuf msg)throws Exception { System.out.println("Received data"); msg.clear();

} });

ChanelFuture f = b.bind(2048);

f.adListener(new ChanelFutureListener() {

@Override

publicvoid operationComplete(ChanelFuture future)throws Exception { if (future.isSuces() {

System.out.println("Server bound");

}else { System.err.println("bound fail"); future.cause().printStackTrace();

}

- 42.
- 43.
- 44.
- 45.


} });

} }

##### 9.4 从Channel引导客户端

有时候需要从另⼀个Channel引导客户端，例如写⼀个代理或需要从其他系统检索数据。从 其他系统获取数据时⽐较常见的，有很多Netty应⽤程序必须要和企业现有的系统集成，如Netty程 序与内部系统进⾏⾝份验证，查询数据库等。

当然，你可以创建⼀个新的引导，这样做没有什么不妥，只是效率不⾼，因为要为新创建的 客户端通道使⽤另⼀个EventLoop，如果需要在已接受的通道和客户端通道之间交换数据则需要切 换上下⽂线程。Netty对这⽅⾯进⾏了优化，可以讲已接受的通道通过eventLoop(...)传递到 EventLoop，从⽽使客户端通道在相同的EventLoop⾥运⾏。这消除了额外的上下⽂切换⼯作，因 为EventLoop继承于EventLoopGroup。除了消除上下⽂切换，还可以在不需要创建多个线程的情 况下使⽤引导。

为什么要共享EventLoop呢？⼀个EventLoop由⼀个线程执⾏，共享EventLoop可以确定所有 的Channel都分配给同⼀线程的EventLoop，这样就避免了不同线程之间切换上下⽂，从⽽减少资 源开销。

下图显⽰相同的EventLoop管理两个Channel：

![image 85](assets/imageFile85.png)

看下⾯代码：

[java] view plaincopy

![image 86](assets/imageFile86.png)

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
- 37.
- 38.
- 39.
- 40.


package nety.in.action;

import java.net.InetSocketAdres;

import io.nety.botstrap.Botstrap; import io.nety.botstrap.ServerBotstrap; import io.nety.bufer.ByteBuf; import io.nety.chanel.ChanelFuture; import io.nety.chanel.ChanelFutureListener; import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.EventLopGroup; import io.nety.chanel.SimpleChanelInboundHandler; import io.nety.chanel.nio.NioEventLopGroup; import io.nety.chanel.socket.nio.NioServerSocketChanel; import io.nety.chanel.socket.nio.NioSocketChanel;

/*

- *从Chanel引导客户端

*

- * @author c.k

*

- */ publicclas BotstrapingFromChanel {


publicstaticvoid main(String[] args)throws Exception { EventLopGroup bosGroup =new NioEventLopGroup(1); EventLopGroup workerGroup =new NioEventLopGroup(); ServerBotstrap b =new ServerBotstrap(); b.group(bosGroup, workerGroup).chanel(NioServerSocketChanel.clas)

.childHandler(new SimpleChanelInboundHandler<ByteBuf>() {

ChanelFuture conectFuture;

@Override

publicvoid chanelActive(ChanelHandlerContext ctx)throws Exception { Botstrap b =new Botstrap(); b.chanel(NioSocketChanel.clas).handler(

new SimpleChanelInboundHandler<ByteBuf>() {

@Override

protectedvoid chanelRead0(ChanelHandlerContext ctx, ByteBuf msg)throws Exception {

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


System.out.println("Received data"); msg.clear();

} });

b.group(ctx.chanel().eventLop();

conectFuture = b.conect(new InetSocketAdres("127.0.0.1",2048); }

@Override

protectedvoid chanelRead0(ChanelHandlerContext ctx, ByteBuf msg) throws Exception { if (conectFuture.isDone() {

/ do something with the data }

} });

ChanelFuture f = b.bind(2048);

f.adListener(new ChanelFutureListener() {

@Override

publicvoid operationComplete(ChanelFuture future)throws Exception { if (future.isSuces() {

System.out.println("Server bound");

}else { System.err.println("bound fail"); future.cause().printStackTrace();

} }

}); }

}

#### 9.5 添加多个ChannelHandler

在所有的例⼦代码中，我们在引导过程中通过handler(...)或childHandler(...)都只添加了⼀个 ChannelHandler实例，对于简单的程序可能⾜够，但是对于复杂的程序则⽆法满⾜需求。例如， 某个程序必须⽀持多个协议，如HTTP、WebSocket。若在⼀个ChannelHandler中处理这些协议 将导致⼀个庞⼤⽽复杂的ChannelHandler。Netty通过添加多个ChannelHandler，从⽽使每个 ChannelHandler分⼯明确，结构清晰。

Netty的⼀个优势是可以在ChannelPipeline中堆叠很多ChannelHandler并且可以最⼤程度的 重⽤代码。如何添加多个ChannelHandler呢？Netty提供ChannelInitializer抽象类⽤来初始化 ChannelPipeline中的ChannelHandler。ChannelInitializer是⼀个特殊的ChannelHandler，通道被

注册到EventLoop后就会调⽤ChannelInitializer，并允许将ChannelHandler添加到 CHannelPipeline；完成初始化通道后，这个特殊的ChannelHandler初始化器会从 ChannelPipeline中⾃动删除。

听起来很复杂，其实很简单，看下⾯代码：

[java] view plaincopy

![image 87](assets/imageFile87.png)

package nety.in.action;

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
- 37.
- 38.


import io.nety.botstrap.ServerBotstrap; import io.nety.chanel.Chanel; import io.nety.chanel.ChanelFuture; import io.nety.chanel.ChanelInitializer; import io.nety.chanel.EventLopGroup; import io.nety.chanel.nio.NioEventLopGroup; import io.nety.chanel.socket.nio.NioServerSocketChanel; import io.nety.handler.codec.htp.HtpClientCodec; import io.nety.handler.codec.htp.HtpObjectAgregator;

/*

- *使⽤ChanelInitializer初始化ChanelHandler

- * @author c.k

*

- */ publicclas InitChanelExample {


publicstaticvoid main(String[] args)throws Exception { EventLopGroup bosGroup =new NioEventLopGroup(1); EventLopGroup workerGroup =new NioEventLopGroup(); ServerBotstrap b =new ServerBotstrap(); b.group(bosGroup, workerGroup).chanel(NioServerSocketChanel.clas)

.childHandler(new ChanelInitializerImpl(); ChanelFuture f = b.bind(2048).sync(); f.chanel().closeFuture().sync();

}

staticfinalclas ChanelInitializerImplextends ChanelInitializer<Chanel>{

@Override

protectedvoid initChanel(Chanel ch)throws Exception { ch.pipeline().adLast(new HtpClientCodec()

.adLast(new HtpObjectAgregator(Integer.MAX_VALUE); }

}

}

- 9.6 使⽤通道选项和属性


⽐较⿇烦的是创建通道后不得不⼿动配置每个通道，为了避免这种情况，Netty提供了 ChannelOption来帮助引导配置。这些选项会⾃动应⽤到引导创建的所有通道，可⽤的各种选项可 以配置底层连接的详细信息，如通道“keep-alive(保持活跃)”或“timeout(超时)”的特性。

Netty应⽤程序通常会与组织或公司其他的软件进⾏集成，在某些情况下，Netty的组件如通 道、传递和Netty正常⽣命周期外使⽤；在这样的情况下并不是所有的⼀般属性和数据时可⽤的。 这只是⼀个例⼦，但在这样的情况下，Netty提供了通道属性(channel attributes)。

属性可以将数据和通道以⼀个安全的⽅式关联，这些属性只是作⽤于客户端和服务器的通 道。例如，例如客户端请求web服务器应⽤程序，为了跟踪通道属于哪个⽤户，应⽤程序可以存储 ⽤的ID作为通道的⼀个属性。任何对象或数据都可以使⽤属性被关联到⼀个通道。

使⽤ChannelOption和属性可以让事情变得很简单，例如Netty WebSocket服务器根据⽤户⾃

动路由消息，通过使⽤属性，应⽤程序能在通道存储⽤户ID以确定消息应该发送到哪⾥。应⽤程 序可以通过使⽤⼀个通道选项进⼀步⾃动化，给定时间内没有收到消息将⾃动断开连接。看下⾯ 代码：

[java] view plaincopy

![image 88](assets/imageFile88.png)

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


/创建属性键对象

final AtributeKey<Integer> id = AtributeKey.valueOf("ID"); /客户端引导对象

Botstrap b =new Botstrap(); /设置EventLop，设置通道类型

b.group(new NioEventLopGroup().chanel(NioSocketChanel.clas) /设置ChanelHandler

.handler(new SimpleChanelInboundHandler<ByteBuf>() {

@Override

protectedvoid chanelRead0(ChanelHandlerContext ctx, ByteBuf msg)

throws Exception { System.out.println("Reveived data"); msg.clear();

}

@Override

publicvoid chanelRegistered(ChanelHandlerContext ctx)throws Exception {

/通道注册后执⾏，获取属性值 Integer idValue = ctx.chanel().atr(id).get(); System.out.println(idValue);

/do something with the idValue }

}); /设置通道选项，在通道注册后或被创建后设置

b.option(ChanelOption.SO_KEPALIVE,true).option(ChanelOption.CONECT_TIMEOUT_MI LIS,5 0);

/设置通道属性 b.atr(id,123456); ChanelFuture f = b.conect(" w.maning.com",80); f.syncUninterruptibly();

}

前⾯都是引导基于TCP的SocketChannel，引导也可以⽤于⽆连接的传输协议如UDP，Netty 提供了DatagramChannel，唯⼀的区别是不会connecte(...)，只能bind(...)。看下⾯代码：

[java] view plaincopy

![image 89](assets/imageFile89.png)

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


Botstrap b =new Botstrap(); b.group(new OioEventLopGroup().chanel(OioDatagramChanel.clas)

.handler(new SimpleChanelInboundHandler<DatagramPacket>() {

@Override

protectedvoid chanelRead0(ChanelHandlerContext ctx, DatagramPacket msg) throws Exception {

/ do something with the packet }

}); ChanelFuture f = b.bind(new InetSocketAdres(0); f.adListener(new ChanelFutureListener() {

@Override

publicvoid operationComplete(ChanelFuture future)throws Exception { if (future.isSuces() {

System.out.println("Chanel bound");

}else { System.err.println("Bound atempt failed"); future.cause().printStackTrace();

} }

}); }

Netty有默认的配置设置，多数情况下，我们不需要改变这些配置，但是在需要时，我们可以 细粒度的控制如何⼯作及处理数据。

#### 9.7 Summary

In this chapter you learned how to bootstrap your Netty-based server and client implementation. You learned how you can specify configuration options that affect the and how you can use attributes to attach information to a channel and use it later. You also learned how to bootstrap connectionless protocol-based applications and how they are different from connection-based ones. The next chapters will focus on Netty in Action by using it to implement

real-world applications. This will help you extract all interesting pieces for reuse in your next application. At this point you should be able to start coding!

第⼗章：单元测试代码

本章介绍

单元测试 EmbeddedChannel

学会了使⽤⼀个或多个ChannelHandler处理接收/发送数据消息，但是如何测试它们呢？Netty提 供了2个额外的类使得测试ChannelHandler变得很容易，本章讲解如何测试Netty程序。测试使⽤ JUnit4，如果不会⽤可以慢慢了解。JUnit4很简单，但是功能很强⼤。本章将重点讲解测试已实现的 ChannelHandler和编解码器。

#### 10.1 General

正如前⾯所学的，Netty提供了⼀个简单的⽅法在ChannelPipeline上“堆叠”不同的 ChannelHandler实现。所有的ChannelHandler都会参与处理事件，这个设计允许独⽴出可重⽤的 ⼩逻辑块，它只处理⼀个任务。这不仅使代码更清晰，也更容易测试。

测试ChannelHandler可以通过使⽤“嵌⼊式”传输很容易的传递事件槽管道以测试你的实现。 对于这个嵌⼊式传输，Netty提供了⼀个特定的Channel实现：EmbeddedChannel。但是它是如何 ⼯作的呢？EmbeddedChannel的⼯作⾮常简单，它允许写⼊⼊站或出站数据，然后检查 ChannelPipeline的结束。这允许你检查消息编码/解码或触发ChannelHandler任何⾏为。

编写⼊站和出站的却别是什么？⼊站数据是通过ChannelInboundHandler处理，代表从远程 对等通道读取数据；出站数据是通过ChannelOutboundHandler处理，代表写⼊数据到远程对等通 道。因此测试ChannelHandler就会选择writeInbound(...)或writeOutbound()(或者都选择)。

EmbeddedChannel提供了下⾯⼀些⽅法：

writeInbound(Object...)，写⼀个消息到⼊站通道 writeOutbound(Object...)，写消息到出站通道 readInbound()，从EmbeddedChannel读取⼊站消息，可能返回null readOutbound()，从EmbeddedChannel读取出站消息，可能返回null finish()，标⽰EmbeddedChannel已结束，任何写数据都会失败

为了更清楚的了解其处理过程，看下图：

![image 90](assets/imageFile90.png)

如上图所⽰，使⽤writeOutbound(...)写消息到通道，消息在出站⽅法通过ChannelPipeline， 之后就可以使⽤readOutbound()读取消息。着同样使⽤与⼊站，使⽤writeInbound(...)和 readInbound()。处理⼊站和出站是相似的，它总是遍历整个ChannelPipeline直到ChannelPipeline 结束，并将处理过的消息存储在EmbeddedChannel中。下⾯来看看如何测试你的逻辑。

#### 10.2 测试ChannelHandler

测试ChannelHandler最好的选择是使⽤EmbeddedChannel。

- 10.2.1 测试处理⼊站消息的handler


我们来编写⼀个简单的ByteToMessageDecoder实现，有⾜够的数据可以读取时将产⽣固定 ⼤⼩的包，如果没有⾜够的数据可以读取，则会等待下⼀个数据块并再次检查是否可以产⽣⼀个 完整包。下图显⽰了重新组装接收的字节：

![image 91](assets/imageFile91.png)

如上图所⽰，它可能会占⽤⼀个以上的“event”以获取⾜够的字节产⽣⼀个数据包，并将它传 递到ChannelPipeline中的下⼀个ChannelHandler，看下⾯代码：

[java] view plaincopy

![image 92](assets/imageFile92.png)

import java.util.List;

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


import io.nety.bufer.ByteBuf; import io.nety.chanel.ChanelHandlerContext; import io.nety.handler.codec.ByteToMesageDecoder;

publicclas FixedLengthFrameDecoderextends ByteToMesageDecoder {

privatefinalint frameLength;

public FixedLengthFrameDecoder(int frameLength) { if (frameLength <=0) { thrownew IlegalArgumentException(

"frameLength must be a positive integer: " + frameLength); }

this.frameLength = frameLength; }

@Override

protectedvoid decode(ChanelHandlerContext ctx, ByteBuf in, List<Object> out)throws Exception {

while (in.readableBytes() >= frameLength) { ByteBuf buf = in.readBytes(frameLength); out.ad(buf);

} }

}

解码器的实现完成了，写⼀个单元测试的⽅法是个好主意。即使代码看起来没啥问题，但是 也应该进⾏单元测试，这样能在部署到⽣产之前就发现问题。现在让我们来看看如何使⽤ EmbeddedChannel来完成测试，看下⾯代码：

[java] view plaincopy

![image 93](assets/imageFile93.png)

import io.nety.bufer.ByteBuf; import io.nety.bufer.Unpoled; import io.nety.chanel.embeded.EmbededChanel;

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
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.


import org.junit.Asert; import org.junit.Test;

publicclas FixedLengthFrameDecoderTest {

@Test

publicvoid testFramesDecoded() {

ByteBuf buf = Unpoled.bufer();

for (int i =0; i <9; i +) {

buf.writeByte(i);

} ByteBuf input = buf.duplicate();

EmbededChanel chanel =new EmbededChanel( new FixedLengthFrameDecoder(3);

/ write bytes Asert.asertTrue(chanel.writeInbound(input); Asert.asertTrue(chanel.finish();

/ read mesage

Asert.asertEquals(buf.readBytes(3), chanel.readInbound(); Asert.asertEquals(buf.readBytes(3), chanel.readInbound(); Asert.asertEquals(buf.readBytes(3), chanel.readInbound(); Asert.asertNul(chanel.readInbound();

}

@Test

publicvoid testFramesDecoded2() {

ByteBuf buf = Unpoled.bufer();

for (int i =0; i <9; i +) {

buf.writeByte(i);

} ByteBuf input = buf.duplicate();

EmbededChanel chanel =new EmbededChanel(

new FixedLengthFrameDecoder(3); Asert.asertFalse(chanel.writeInbound(input.readBytes(2); Asert.asertTrue(chanel.writeInbound(input.readBytes(7); Asert.asertTrue(chanel.finish(); Asert.asertEquals(buf.readBytes(3), chanel.readInbound();

- 44.
- 45.
- 46.
- 47.
- 48.
- 49.


Asert.asertEquals(buf.readBytes(3), chanel.readInbound(); Asert.asertEquals(buf.readBytes(3), chanel.readInbound(); Asert.asertNul(chanel.readInbound();

}

}

如上⾯代码，testFramesDecoded()⽅法想测试⼀个ByteBuf，这个ByteBuf包含9个可读字 节，被解码成包含了3个可读字节的ByteBuf。你可能注意到，它写⼊9字节到通道是通过调⽤ writeInbound()⽅法，之后再执⾏finish()来将EmbeddedChannel标记为已完成，最后调⽤ readInbound()⽅法来获取EmbeddedChannel中的数据，直到没有可读字节。 testFramesDecoded2()⽅法采取同样的⽅式，但有⼀个区别就是⼊站ByteBuf分两步写的，当调⽤ writeInbound(input.readBytes(2))后返回false时，FixedLengthFrameDecoder值会产⽣输出，⾄少 有3个字节是可读，testFramesDecoded2()测试的⼯作相当于testFramesDecoded()。

- 10.2.2 测试处理出站消息的handler


测试处理出站消息和测试处理⼊站消息不太⼀样，例如有⼀个继承 MessageToMessageEncoder的AbsIntegerEncoder类，它所做的事情如下：

将已接收的数据flush()后将从ByteBuf读取所有整数并调⽤Math.abs(...) 完成后将字节写⼊ChannelPipeline中下⼀个ChannelHandler的ByteBuf中

看下图处理过程：

![image 94](assets/imageFile94.png)

看下⾯代码：

[java] view plaincopy

![image 95](assets/imageFile95.png)

import java.util.List;

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


import io.nety.bufer.ByteBuf; import io.nety.chanel.ChanelHandlerContext; import io.nety.handler.codec.MesageToMesageEncoder;

publicclas AbsIntegerEncoderextends MesageToMesageEncoder<ByteBuf> {

@Override

protectedvoid encode(ChanelHandlerContext ctx, ByteBuf msg,

List<Object> out)throws Exception { while(msg.readableBytes() >=4){

int value = Math.abs(msg.readInt(); out.ad(value);

} }

}

下⾯代码是测试AbsIntegerEncoder：

[java] view plaincopy

![image 96](assets/imageFile96.png)

import io.nety.bufer.ByteBuf; import io.nety.bufer.Unpoled; import io.nety.chanel.embeded.EmbededChanel;

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


import org.junit.Asert; import org.junit.Test;

publicclas AbsIntegerEncoderTest {

@Test

publicvoid testEncoded() {

/创建⼀个能容纳10个int的ByteBuf ByteBuf buf = Unpoled.bufer();

for (int i =1; i <10; i +) {

buf.writeInt(i * -1); }

/创建EmbededChanel对象

EmbededChanel chanel =new EmbededChanel(new AbsIntegerEncoder(); /将buf数据写⼊出站EmbededChanel

Asert.asertTrue(chanel.writeOutbound(buf); /标⽰EmbededChanel完成 Asert.asertTrue(chanel.finish(); /读取出站数据 ByteBuf output = (ByteBuf) chanel.readOutbound();

for (int i =1; i <10; i +) {

Asert.asertEquals(i, output.readInt();

} Asert.asertFalse(output.isReadable(); Asert.asertNul(chanel.readOutbound();

}

}

##### 10.3 测试异常处理

有时候传输的⼊站或出站数据不够，通常这种情况也需要处理，例如抛出⼀个异常。这可能 是你错误的输⼊或处理⼤的资源或其他的异常导致。我们来写⼀个实现，如果输⼊字节超出限制 长度就抛出TooLongFrameException，这样的功能⼀般⽤来防⽌资源耗尽。看下图：

![image 97](assets/imageFile97.png)

上图显⽰帧的⼤⼩被限制为3字节，若输⼊的字节超过3字节，则超过的字节被丢弃并抛出 TooLongFrameException。在ChannelPipeline中的其他ChannelHandler实现可以处理 TooLongFrameException或者忽略异常。处理异常在ChannelHandler.exceptionCaught()⽅法中完 成，ChannelHandler提供了⼀些具体的实现，看下⾯代码：

[java] view plaincopy

![image 98](assets/imageFile98.png)

import java.util.List;

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


import io.nety.bufer.ByteBuf; import io.nety.chanel.ChanelHandlerContext; import io.nety.handler.codec.ByteToMesageDecoder; import io.nety.handler.codec.ToLongFrameException;

publicclas FrameChunkDecoderextends ByteToMesageDecoder {

/限制⼤⼩

privatefinalint maxFrameSize;

public FrameChunkDecoder(int maxFrameSize) {

this.maxFrameSize = maxFrameSize; }

@Override

protectedvoid decode(ChanelHandlerContext ctx, ByteBuf in,

List<Object> out)throws Exception { /获取可读字节数

int readableBytes = in.readableBytes();

/若可读字节数⼤于限制值,清空字节并抛出异常

if (readableBytes > maxFrameSize) {

in.clear();

thrownew ToLongFrameException(); }

/读取ByteBuf并放到List中 ByteBuf buf = in.readBytes(readableBytes); out.ad(buf);

}

}

测试FrameChunkDecoder的代码如下：

[java] view plaincopy

![image 99](assets/imageFile99.png)

import io.nety.bufer.ByteBuf; import io.nety.bufer.Unpoled; import io.nety.chanel.embeded.EmbededChanel; import io.nety.handler.codec.ToLongFrameException;

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
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.


import org.junit.Asert; import org.junit.Test;

publicclas FrameChunkDecoderTest {

@Test

publicvoid testFramesDecoded() {

/创建ByteBuf并填充9字节数据 ByteBuf buf = Unpoled.bufer();

for (int i =0; i <9; i +) {

buf.writeByte(i); }

/复制⼀个ByteBuf ByteBuf input = buf.duplicate(); /创建EmbededChanel

EmbededChanel chanel =new EmbededChanel(new FrameChunkDecoder(3); /读取2个字节写⼊⼊站通道

- Asert.asertTrue(chanel.writeInbound(input.readBytes(2); try {

/读取4个字节写⼊⼊站通道 chanel.writeInbound(input.readBytes(4); Asert.fail();

}catch (ToLongFrameException e) {

} /读取3个字节写⼊⼊站通道

- Asert.asertTrue(chanel.writeInbound(input.readBytes(3); /标识完成


Asert.asertTrue(chanel.finish(); /从EmbededChanel⼊去⼊站数据 Asert.asertEquals(buf.readBytes(2), chanel.readInbound(); Asert.asertEquals(buf.skipBytes(4).readBytes(3),

chanel.readInbound(); }

}

#### 10.4 Summary

In this chapter you learned how you are be able to test your custom ChannelHandler and so make sure it works like you expected. Using the shown techniques you are now be able

to make use of JUnit and so ultimately test your code as your are used to. Using the techniques shown in the chapter you will be able to guarantee a high quality of your code and also guard it from misbehavior.. In the next chapters we will focus on writing "real" applications on top of Netty and so show you how you can make real use of it. Even if the applications don't

contain any test-code remember it is quite important to do so when you will write your next-gen application.

### 第⼗⼀章：WebSocket

本章介绍

WebSocket ChannelHandler,Decoder and Encoder 引导⼀个Netty基础程序 测试WebSocket

“real-time-web”实时web现在随处可见，很多的⽤户希望能从web站点实时获取信息。Netty⽀持

WebSocket实现，并包含了不同的版本，我们可以⾮常容易的实现WebSocket应⽤。使⽤Netty附带的 WebSocket，我们不需要关注协议内部实现，只需要使⽤Netty提供的⼀些简单的⽅法就可以实现。本 章将通过的例⼦应⽤帮助你来使⽤WebSocket并了解它是如何⼯作。

# 11.1 WebSockets some background

关于WebSocket的⼀些概念和背景，可以查询⽹上相关介绍。这⾥不赘述。

## 11.2 ⾯临的挑战

要显⽰“real-time”⽀持的WebSocket，应⽤程序将显⽰如何使⽤Netty中的WebSocket实现⼀ 个在浏览器中进⾏聊天的IRC应⽤程序。你可能知道从Facebook可以发送⽂本消息到另⼀个⼈， 在这⾥，我们将进⼀步了解其实现。在这个应⽤程序中，不同的⽤户可以同时交谈，⾮常像 IRC(Internet Relay Chat，互联⽹中继聊天)。

![image 100](assets/imageFile100.png)

上图显⽰的逻辑很简单：

- 1.
- 2.


⼀个客户端发送⼀条消息 消息被⼴播到其他已连接的客户端

它的⼯作原理就像聊天室⼀样，在这⾥例⼦中，我们将编写服务器，然后使⽤浏览器作为客 户端。带着这样的思路，我们将会很简单的实现它。

## 11.3 实现

WebSocket使⽤HTTP升级机制从⼀个普通的HTTP连接WebSocket，因为这个应⽤程序使⽤ WebSocket总是开始于HTTP(s)，然后再升级。什么时候升级取决于应⽤程序本⾝。直接执⾏升级 作为第⼀个操作⼀般是使⽤特定的url请求。

在这⾥，如果url的结尾以/ws结束，我们将只会升级到WebSocket，否则服务器将发送⼀个 ⽹页给客户端。升级后的连接将通过WebSocket传输所有数据。逻辑图如下：

![image 101](assets/imageFile101.png)

#### 11.3.1 处理http请求

服务器将作为⼀种混合式以允许同时处理http和websocket，所以服务器还需要html页⾯， html⽤来充当客户端⾓⾊，连接服务器并交互消息。因此，如果客户端不发送/ws的uri，我们需要 写⼀个ChannelInboundHandler⽤来处理FullHttpRequest。看下⾯代码：

[java] view plaincopy

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
- 37.
- 38.
- 39.
- 40.


package nety.in.action;

import io.nety.chanel.ChanelFuture; import io.nety.chanel.ChanelFutureListener; import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.DefaultFileRegion; import io.nety.chanel.SimpleChanelInboundHandler; import io.nety.handler.codec.htp.DefaultFulHtpResponse; import io.nety.handler.codec.htp.DefaultHtpResponse; import io.nety.handler.codec.htp.FulHtpRequest; import io.nety.handler.codec.htp.FulHtpResponse; import io.nety.handler.codec.htp.HtpHeaders; import io.nety.handler.codec.htp.HtpResponse; import io.nety.handler.codec.htp.HtpResponseStatus; import io.nety.handler.codec.htp.HtpVersion; import io.nety.handler.codec.htp.LastHtpContent; import io.nety.handler.sl.SslHandler; import io.nety.handler.stream.ChunkedNioFile;

import java.io.RandomAcesFile;

/*

- * WebSocket，处理htp请求

*

- * @author c.k

*

- */ publicclas HtpRequestHandlerextends


SimpleChanelInboundHandler<FulHtpRequest> { /websocket标识

privatefinal String wsUri;

public HtpRequestHandler(String wsUri) {

this.wsUri = wsUri; }

@Override

protectedvoid chanelRead0(ChanelHandlerContext ctx, FulHtpRequest msg) throws Exception {

/如果是websocket请求，请求地址uri等于wsuri

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
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.


if (wsUri.equalsIgnoreCase(msg.getUri( ) {

/将消息转发到下⼀个ChanelHandler ctx.fireChanelRead(msg.retain();

}else {/如果不是websocket请求

if (HtpHeaders.is10ContinueExpected(msg) { /如果HTP请求头部包含Expect: 10-continue， /则响应请求

FulHtpResponse response =new DefaultFulHtpResponse(

HtpVersion.HTP_1_1, HtpResponseStatus.CONTINUE); ctx.writeAndFlush(response);

} /获取index.html的内容响应给客户端

RandomAcesFile file =new RandomAcesFile(

System.getProperty("user.dir") +"/index.html","r"); HtpResponse response =new DefaultHtpResponse(

msg.getProtocolVersion(), HtpResponseStatus.OK); response.headers().set(HtpHeaders.Names.CONTENT_TYPE,

"text/html; charset=UTF-8");

bolean kepAlive = HtpHeaders.isKepAlive(msg); /如果htp请求保持活跃，设置htp请求头部信息 /并响应请求

if (kepAlive) {

response.headers().set(HtpHeaders.Names.CONTENT_LENGTH, file.length(); response.headers().set(HtpHeaders.Names.CONECTION, HtpHeaders.Values.KEP_ALIVE);

} ctx.write(response);

/如果不是htps请求，将index.html内容写⼊通道

if (ctx.pipeline().get(SslHandler.clas) =nul) { ctx.write(new DefaultFileRegion(file.getChanel(),0, file

.length( ); }else {

ctx.write(new ChunkedNioFile(file.getChanel( ); }

/标识响应内容结束并刷新通道 ChanelFuture future = ctx

.writeAndFlush(LastHtpContent.EMPTY_LAST_CONTENT); if (!kepAlive) {

/如果htp请求不活跃，关闭htp连接 future.adListener(ChanelFutureListener.CLOSE);

} file.close();

}

- 85.
- 86.
- 87.
- 88.
- 89.
- 90.
- 91.
- 92.
- 93.


}

@Override

publicvoid exceptionCaught(ChanelHandlerContext ctx, Throwable cause)

throws Exception { cause.printStackTrace(); ctx.close();

} }

#### 11.3.2 处理WebSocket框架

WebSocket⽀持6种不同框架，如下图：

![image 102](assets/imageFile102.png)

我们的程序只需要使⽤下⾯4个框架：

CloseWebSocketFrame PingWebSocketFrame PongWebSocketFrame TextWebSocketFrame

我们只需要显⽰处理TextWebSocketFrame，其他的会⾃动由 WebSocketServerProtocolHandler处理，看下⾯代码：

[java] view plaincopy

import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.SimpleChanelInboundHandler; import io.nety.chanel.group.ChanelGroup; import io.nety.handler.codec.htp.websocketx.TextWebSocketFrame; import io.nety.handler.codec.htp.websocketx.WebSocketServerProtocolHandler;

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
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.


/*

- * WebSocket，处理消息

- * @author c.k

*

- */ publicclas TextWebSocketFrameHandlerextends


SimpleChanelInboundHandler<TextWebSocketFrame> { privatefinal ChanelGroup group;

public TextWebSocketFrameHandler(ChanelGroup group) {

this.group = group; }

@Override

publicvoid userEventTri gered(ChanelHandlerContext ctx, Object evt)

throws Exception { /如果WebSocket握⼿完成

if (evt = WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {

/删除ChanelPipeline中的HtpRequestHandler

ctx.pipeline().remove(HtpRequestHandler.clas); /写⼀个消息到ChanelGroup

group.writeAndFlush(new TextWebSocketFrame("Client " + ctx.chanel()

+" joined");

/将Chanel添加到ChanelGroup group.ad(ctx.chanel();

}else {

super.userEventTri gered(ctx, evt); }

}

@Override

protectedvoid chanelRead0(ChanelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

/将接收的消息通过ChanelGroup转发到所以已连接的客户端

- 43.
- 44.
- 45.


group.writeAndFlush(msg.retain(); }

}

#### 11.3.3 初始化ChannelPipeline

看下⾯代码：

[java] view plaincopy

import io.nety.chanel.Chanel; import io.nety.chanel.ChanelInitializer; import io.nety.chanel.ChanelPipeline; import io.nety.chanel.group.ChanelGroup; import io.nety.handler.codec.htp.HtpObjectAgregator; import io.nety.handler.codec.htp.HtpServerCodec; import io.nety.handler.codec.htp.websocketx.WebSocketServerProtocolHandler; import io.nety.handler.stream.ChunkedWriteHandler;

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
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.


/*

- * WebSocket,初始化ChanelHandler

- * @author c.k

*

- */ publicclas ChatServerInitializerextends ChanelInitializer<Chanel> {


privatefinal ChanelGroup group;

public ChatServerInitializer(ChanelGroup group){

this.group = group; }

@Override

protectedvoid initChanel(Chanel ch)throws Exception {

ChanelPipeline pipeline = ch.pipeline(); /编解码htp请求

pipeline.adLast(new HtpServerCodec(); /写⽂件内容

pipeline.adLast(new ChunkedWriteHandler(); /聚合解码HtpRequest/HtpContent/LastHtpContent到FulHtpRequest /保证接收的Htp请求的完整性

pipeline.adLast(new HtpObjectAgregator(64 *1024); /处理FulHtpRequest

pipeline.adLast(new HtpRequestHandler("/ws"); /处理其他的WebSocketFrame

pipeline.adLast(new WebSocketServerProtocolHandler("/ws"); /处理TextWebSocketFrame

pipeline.adLast(new TextWebSocketFrameHandler(group); }

}

WebSocketServerProtcolHandler不仅处理Ping/Pong/CloseWebSocketFrame，还和它⾃⼰ 握⼿并帮助升级WebSocket。这是执⾏完成握⼿和成功修改ChannelPipeline，并且添加需要的编 码器/解码器和删除不需要的ChannelHandler。

看下图：

![image 103](assets/imageFile103.png)

ChannelPipeline通过ChannelInitializer的initChannel(...)⽅法完成初始化，完成握⼿后就会更 改事情。⼀旦这样做了，WebSocketServerProtocolHandler将取代HttpRequestDecoder、 WebSocketFrameDecoder13和HttpResponseEncoder、WebSocketFrameEncoder13。另外也要 删除所有不需要的ChannelHandler已获得最佳性能。这些都是HttpObjectAggregator和 HttpRequestHandler。下图显⽰ChannelPipeline握⼿完成：

![image 104](assets/imageFile104.png)

我们甚⾄没注意到它，因为它是在底层执⾏的。以⾮常灵活的⽅式动态更新ChannelPipeline 让单独的任务在不同的ChannelHandler中实现。

## 11.4 结合在⼀起使⽤

###### ⼀如既往，我们要将它们结合在⼀起使⽤。使⽤Bootstrap引导服务器和设置正确的 ChannelInitializer。看下⾯代码：

[java] view plaincopy

import io.nety.botstrap.ServerBotstrap; import io.nety.chanel.Chanel; import io.nety.chanel.ChanelFuture; import io.nety.chanel.ChanelInitializer; import io.nety.chanel.EventLopGroup; import io.nety.chanel.group.ChanelGroup; import io.nety.chanel.group.DefaultChanelGroup; import io.nety.chanel.nio.NioEventLopGroup; import io.nety.chanel.socket.nio.NioServerSocketChanel; import io.nety.util.concurrent.I mediateEventExecutor;

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
- 37.
- 38.
- 39.
- 40.
- 41.


import java.net.InetSocketAdres;

/*

- *访问地址：htp:/localhost:2048

*

- * @author c.k

*

- */ publicclas ChatServer {


privatefinal ChanelGroup group =new DefaultChanelGroup(

I mediateEventExecutor.INSTANCE); privatefinal EventLopGroup workerGroup =new NioEventLopGroup(); private Chanel chanel;

public ChanelFuture start(InetSocketAdres adres) { ServerBotstrap b =new ServerBotstrap(); b.group(workerGroup).chanel(NioServerSocketChanel.clas)

.childHandler(createInitializer(group); ChanelFuture f = b.bind(adres).syncUninterruptibly(); chanel = f.chanel();

return f; }

publicvoid destroy() { if (chanel !=nul)

chanel.close(); group.close();

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


workerGroup.shutdownGracefuly(); }

protected ChanelInitializer<Chanel> createInitializer(ChanelGroup group) {

returnnew ChatServerInitializer(group); }

publicstaticvoid main(String[] args) { final ChatServer server =new ChatServer(); ChanelFuture f = server.start(new InetSocketAdres(2048); Runtime.getRuntime().adShutdownHok(new Thread() {

@Override

publicvoid run() {

server.destroy(); }

}); f.chanel().closeFuture().syncUninterruptibly();

}

}

另外，需要将index.html⽂件放在项⽬根⽬录，index.html内容如下：

[html] view plaincopy

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


<html> <head> <title>Web Socket Test</title> </head> <body> <scripttype="text/javascript"> var socket; if (!window.WebSocket) {

window.WebSocket =window.MozWebSocket;

} if (window.WebSocket) {

socket =new WebSocket("ws:/localhost:2048/ws"); socket.onmesage =function(event) {

varta =document.getElementById('responseText'); ta.value =ta.value + '\n' + event.data

}; socket.onopen =function(event) {

varta =document.getElementById('responseText'); ta.value ="Web Socket opened!";

}; socket.onclose =function(event) {

varta =document.getElementById('responseText'); ta.value =ta.value + "Web Socket closed";

}; } else {

alert("Your browser does not suport Web Socket."); }

function send(mesage) { if (!window.WebSocket) { return; } if (socket.readyState = WebSocket.OPEN) {

socket.send(mesage); } else {

alert("The socket is not open."); }

} </script>

<formonsubmit="return false;">

<inputtype="text"name="mesage"value="Helo, World!"><input type="buton"value="Send Web Socket Data" onclick="send(this.form.mesage.value)">

<h3>Output</h3> <textareaid="responseText"style="width: 50px; height: 30px;"></textarea>

</form> </body> </html>

最后在浏览器中输⼊：http://localhost:2048，多开⼏个窗⼜就可以聊天了。

# 11.5 给WebSocket加密

上⾯的应⽤程序虽然⼯作的很好，但是在⽹络上收发消息存在很⼤的安全隐患，所以有必要 对消息进⾏加密。添加这样⼀个加密的功能⼀般⽐较复杂，需要对代码有较⼤的改动。但是使⽤ Netty就可以很容易的添加这样的功能，只需要将SslHandler加⼊到ChannelPipeline中就可以了。 实际上还需要添加SslContext，但这不在本例⼦范围内。

⾸先我们创建⼀个⽤于添加加密Handler的handler初始化类，看下⾯代码：

[java] view plaincopy

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


package nety.in.action;

import io.nety.chanel.Chanel; import io.nety.chanel.group.ChanelGroup; import io.nety.handler.sl.SslHandler;

import javax.net.sl. SLContext; import javax.net.sl. SLEngine;

publicclas SecureChatServerIntializerextends ChatServerInitializer { privatefinal SLContext context;

public SecureChatServerIntializer(ChanelGroup group, SLContext context) { super(group); this.context = context;

}

@Override

protectedvoid initChanel(Chanel ch)throws Exception { super.initChanel(ch); SLEngine engine = context.createSLEngine(); engine.setUseClientMode(false); ch.pipeline().adFirst(new SslHandler(engine);

} }

最后我们创建⼀个⽤于引导配置的类，看下⾯代码：

[java] view plaincopy

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
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.


package nety.in.action;

import io.nety.chanel.Chanel; import io.nety.chanel.ChanelFuture; import io.nety.chanel.ChanelInitializer; import io.nety.chanel.group.ChanelGroup; import java.net.InetSocketAdres; import javax.net.sl. SLContext;

/*

- *访问地址：htps:/localhost:4096

*

- * @author c.k

*

- */ publicclas SecureChatServerextends ChatServer {


privatefinal SLContext context;

public SecureChatServer(SLContext context) {

this.context = context; }

@Override

protected ChanelInitializer<Chanel> createInitializer(ChanelGroup group) {

returnnew SecureChatServerIntializer(group, context); }

/*

- *获取 SLContext需要相关的keystore⽂件，这⾥没有 关于HTPS可以查阅相关资料，这⾥只介绍在Nety中如何使⽤

*

- * @return

- */ privatestatic SLContext getSslContext() {


returnnul; }

publicstaticvoid main(String[] args) { SLContext context = getSslContext(); final SecureChatServer server =new SecureChatServer(context);

ChanelFuture future = server.start(new InetSocketAdres(4096); Runtime.getRuntime().adShutdownHok(new Thread() {

@Override

- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.


publicvoid run() {

server.destroy(); }

}); future.chanel().closeFuture().syncUninterruptibly();

} }

# 11.6 Summary

第⼗⼆章：SPDY

本章我将不会直接翻译Netty In Action书中的原⽂，感觉原书中本章讲的很多废话，我翻译起来也 吃⼒。所以，本章内容我会根据其他资料和个⼈理解来讲述。

## 12.1 SPDY概念及背景

SPDY 是 Google 开发的基于传输控制协议 (TCP) 的应⽤层协议，开发组正在推动 SPDY 成 为正式标准（现为互联⽹草案）。SPDY 协议旨在通过压缩、多路复⽤和优先级来缩短⽹页的加 载时间和提⾼安全性。（SPDY 是 Speedy 的昵⾳，意思是更快）。

为什么需要SPDY？SPDY 协议只是在性能上对 HTTP 做了很⼤的优化，其核⼼思想是尽量

减少连接个数，⽽对于 HTTP 的语义并没有做太⼤的修改。具体来说是，SPDY 使⽤了 HTTP 的 ⽅法和页眉，但是删除了⼀些头并重写了 HTTP 中管理连接和数据转移格式的部分，所以基本上 是兼容 HTTP 的。

Google 在 SPDY ⽩⽪书⾥表⽰要向协议栈下⾯渗透并替换掉传输层协议（TCP），但是因 为这样⽆论是部署起来还是实现起来暂时相当困难，因此 Google 准备先对应⽤层协议 HTTP 进 ⾏改进，先在 SSL 之上增加⼀个会话层来实现 SPDY 协议，⽽ HTTP 的 GET 和 POST 消息格式 保持不变，即现有的所有服务端应⽤均不⽤做任何修改。因此在⽬前，SPDY 的⽬的是为了加强 HTTP，是对 HTTP ⼀个更好的实现和⽀持。⾄于未来 SPDY 得到⼴泛应⽤后会不会演⼀出狸猫 换太⼦，替换掉 HTTP 并彻底颠覆整个 Internet 就是 Google 的事情了。

距离万维⽹之⽗蒂姆·伯纳斯-李发明并推动 HTTP 成为如今互联⽹最流⾏的协议已经过去⼗ ⼏年了（现⽤ HTTP 1.1 规范也停滞了 13 年了），随着现在 WEB 技术的飞速发展尤其是 HTML5 的不断演进，包括 WebSockets 协议的出现以及当前⽹络环境的改变、传输内容的变化， 当初的 HTTP 规范已经逐渐⽆法满⾜⼈们的需要了，HTTP 需要进⼀步发展，因此 HTTPbis ⼯作 组已经被组建并被授权考虑 HTTP 2.0 ，希望能解决掉⽬前 HTTP 所带来的诸多限制。⽽ SPDY 正是 Google 在 HTTP 即将从 1.1 跨越到 2.0 之际推出的试图成为下⼀代互联⽹通信的协议，长期 以来⼀直被认为是 HTTP 2.0 唯⼀可⾏选择。

SPDY相⽐HTTP有如下优点：

- 1.
- 2.
- 3.
- 4.


SPDY多路复⽤，请求优化；⽽HTTP单路连接，请求低效 SPDY⽀持服务器推送技术；⽽HTTP只允许由客户端主动发起请求 SPDY压缩了HTTP头信息，节省了传输数据的带宽流量；⽽HTTP头冗余，同⼀个会话会反 复送头信息 SPDY强制使⽤SSL传输协议，全部请求SSL加密后，信息传输更安全

⾕歌表⽰，引⼊SPDY协议后，在实验室测试中页⾯加载速度⽐原先快64%。

⽀持SPDY协议的浏览器：

Google Chrome 19+和Chromium 19+ Mozilla Firefox 11+，从13开始默认⽀持 Opera 12.10+ Internet Explorer 11+

## 12.2 本例⼦流程图

![image 105](assets/imageFile105.png)

# 12.3 Netty中使⽤SPDY

⽀持SPDY的ChannelPipeline如下图：

![image 106](assets/imageFile106.png)

不⽀持SPDY的ChannelPipeline如下图：

![image 107](assets/imageFile107.png)

###### 例⼦代码如下：

[java] view plaincopy

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


package nety.in.action.spdy;

import java.util.Arrays; import java.util.Colections; import java.util.List;

import org.eclipse.jety.npn.NextProtoNego.ServerProvider;

publicclas DefaultServerProviderimplements ServerProvider {

privatestaticfinal List<String> PROTOCOLS = Colections.unmodifiableList(Arrays

.asList("spdy/3.1","htp/1.1","htp/1.0","Unknown");

private String protocol;

public String getSelectedProtocol() {

return protocol; }

@Override

publicvoid protocolSelected(String arg0) {

this.protocol = arg0; }

@Override

public List<String> protocols() {

return PROTOCOLS; }

@Override

publicvoid unsuported() {

protocol ="htp/1.1"; }

}

[java] view plaincopy

import io.nety.chanel.ChanelFuture; import io.nety.chanel.ChanelFutureListener; import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.SimpleChanelInboundHandler; import io.nety.handler.codec.htp.DefaultFulHtpResponse; import io.nety.handler.codec.htp.FulHtpRequest; import io.nety.handler.codec.htp.FulHtpResponse; import io.nety.handler.codec.htp.HtpHeaders; import io.nety.handler.codec.htp.HtpResponseStatus; import io.nety.handler.codec.htp.HtpVersion; import io.nety.util.CharsetUtil;

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
- 37.
- 38.
- 39.
- 40.
- 41.


publicclas HtpRequestHandlerextends SimpleChanelInboundHandler<FulHtpRequest> {

@Override

protectedvoid chanelRead0(ChanelHandlerContext ctx, FulHtpRequest request) throws Exception { if (HtpHeaders.is10ContinueExpected(request) {

send10Continue(ctx); }

FulHtpResponse response =new DefaultFulHtpResponse( request.getProtocolVersion(), HtpResponseStatus.OK); response.content().writeBytes(getContent().getBytes(CharsetUtil.UTF_8); response.headers().set(HtpHeaders.Names.CONTENT_TYPE,

"text/plain; charset=UTF-8"); bolean kepAlive = HtpHeaders.isKepAlive(request); if (kepAlive) {

response.headers().set(HtpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes(); response.headers().set(HtpHeaders.Names.CONECTION, HtpHeaders.Values.KEP_ALIVE);

} ChanelFuture future = ctx.writeAndFlush(response);

if (!kepAlive) {

future.adListener(ChanelFutureListener.CLOSE); }

}

privatestaticvoid send10Continue(ChanelHandlerContext ctx) {

FulHtpResponse response =new DefaultFulHtpResponse(HtpVersion.HTP_1_1,

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


HtpResponseStatus.CONTINUE); ctx.writeAndFlush(response);

}

protected String getContent() {

return"This content is transmited via HTP\r\n"; }

@Override

publicvoid exceptionCaught(ChanelHandlerContext ctx, Throwable cause)

throws Exception { cause.printStackTrace(); ctx.close();

} }

[java] view plaincopy

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


package nety.in.action.spdy;

publicclas SpdyRequestHandlerextends HtpRequestHandler {

@Override

protected String getContent() {

return"This content is transmited via SPDY\r\n"; }

}

[java] view plaincopy

import io.nety.chanel.ChanelInboundHandler; import io.nety.handler.codec.spdy.SpdyOrHtpChoser;

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
- 37.
- 38.
- 39.
- 40.
- 41.


import javax.net.sl. SLEngine;

import org.eclipse.jety.npn.NextProtoNego;

publicclas DefaultSpdyOrHtpChoserextends SpdyOrHtpChoser {

protected DefaultSpdyOrHtpChoser(int maxSpdyContentLength,int maxHtpContentLength) {

super(maxSpdyContentLength, maxHtpContentLength); }

@Override

protected SelectedProtocol getProtocol(SLEngine engine) {

DefaultServerProvider provider = (DefaultServerProvider) NextProtoNego

.get(engine); String protocol = provider.getSelectedProtocol();

if (protocol =nul) {

return SelectedProtocol.UNKNOWN; }

switch (protocol) { case"spdy/3.1":

return SelectedProtocol.SPDY_3_1;

- case"htp/1.0":

- case"htp/1.1": return SelectedProtocol.HTP_1_1;


default:

return SelectedProtocol.UNKNOWN; }

}

@Override

protected ChanelInboundHandler createHtpRequestHandlerForHtp() {

returnnew HtpRequestHandler(); }

@Override

protected ChanelInboundHandler createHtpRequestHandlerForSpdy() {

returnnew SpdyRequestHandler(); }

- 43.
- 44.
- 45.


}

[java] view plaincopy

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


package nety.in.action.spdy;

import io.nety.chanel.Chanel; import io.nety.chanel.ChanelInitializer; import io.nety.chanel.ChanelPipeline; import io.nety.handler.sl.SslHandler;

import javax.net.sl. SLContext; import javax.net.sl. SLEngine;

import org.eclipse.jety.npn.NextProtoNego;

publicclas SpdyChanelInitializerextends ChanelInitializer<Chanel> { privatefinal SLContext context;

public SpdyChanelInitializer(SLContext context) {

this.context = context; }

@Override

protectedvoid initChanel(Chanel ch)throws Exception {

ChanelPipeline pipeline = ch.pipeline(); SLEngine engine = context.createSLEngine(); engine.setUseClientMode(false); NextProtoNego.put(engine,new DefaultServerProvider(); NextProtoNego.debug =true; pipeline.adLast("slHandler",new SslHandler(engine);

pipeline.adLast("choser",

new DefaultSpdyOrHtpChoser(1024 *1024,1024 *1024); }

}

###### [java] view plaincopy

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
- 37.
- 38.
- 39.
- 40.
- 41.


package nety.in.action.spdy;

import io.nety.botstrap.ServerBotstrap; import io.nety.chanel.Chanel; import io.nety.chanel.ChanelFuture; import io.nety.chanel.nio.NioEventLopGroup; import io.nety.chanel.socket.nio.NioServerSocketChanel; import io.nety.example.securechat.SecureChatSslContextFactory;

import java.net.InetSocketAdres;

import javax.net.sl. SLContext;

publicclas SpdyServer {

privatefinal NioEventLopGroup group =new NioEventLopGroup(); privatefinal SLContext context; private Chanel chanel;

public SpdyServer(SLContext context) {

this.context = context; }

public ChanelFuture start(InetSocketAdres adres) { ServerBotstrap botstrap =new ServerBotstrap(); botstrap.group(group).chanel(NioServerSocketChanel.clas)

.childHandler(new SpdyChanelInitializer(context); ChanelFuture future = botstrap.bind(adres); future.syncUninterruptibly(); chanel = future.chanel();

return future; }

publicvoid destroy() { if (chanel !=nul) {

chanel.close();

} group.shutdownGracefuly();

}

publicstaticvoid main(String[] args) {

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


SLContext context = SecureChatSslContextFactory.getServerContext(); final SpdyServer endpoint =new SpdyServer(context);

ChanelFuture future = endpoint.start(new InetSocketAdres(4096); Runtime.getRuntime().adShutdownHok(new Thread() {

@Override

publicvoid run() { endpoint.destroy(); }

}); future.chanel().closeFuture().syncUninterruptibly();

}

}

使⽤SSL需要使⽤到SSLContext，下⾯代买是获取SSLContext对象：

[java] view plaincopy

*/

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
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.


package nety.in.action.spdy;

import javax.net.sl.ManagerFactoryParameters; import javax.net.sl.TrustManager; import javax.net.sl.TrustManagerFactorySpi; import javax.net.sl.X509TrustManager; import java.security.InvalidAlgorithmParameterException; import java.security.KeyStore; import java.security.KeyStoreException; import java.security.cert.X509Certificate;

/*

- * Bogus {@link TrustManagerFactorySpi} which acepts any certificate

- * even if it is invalid.

- */ publicclas SecureChatTrustManagerFactoryextends TrustManagerFactorySpi {


privatestaticfinal TrustManager DUMY_TRUST_MANAGER =new X509TrustManager() {

@Override

public X509Certificate[] getAceptedIsuers() {

returnnew X509Certificate[0]; }

@Override

publicvoid checkClientTrusted(X509Certificate[] chain, String authType) { / Always trust - it is an example. / You should do something in the real world. / You wil reach here only if you enabled client certificate auth, / as described in SecureChatSslContextFactory.

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
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.


System.err.println(

"UNKNOWN CLIENT CERTIFICATE: " + chain[0].getSubjectDN(); }

@Override

publicvoid checkServerTrusted(X509Certificate[] chain, String authType) { / Always trust - it is an example. / You should do something in the real world.

System.err.println(

"UNKNOWN SERVER CERTIFICATE: " + chain[0].getSubjectDN(); }

};

publicstatic TrustManager[] getTrustManagers() {

returnnew TrustManager[] { DUMY_TRUST_MANAGER }; }

@Override

protected TrustManager[] engineGetTrustManagers() {

return getTrustManagers(); }

@Override

protectedvoid engineInit(KeyStore keystore)throws KeyStoreException {

/ Unused }

@Override

protectedvoid engineInit(ManagerFactoryParameters managerFactoryParameters)

throws InvalidAlgorithmParameterException { / Unused

} }

[java] view plaincopy

*/

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
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.


package nety.in.action.spdy;

import java.io.ByteArrayInputStream; import java.io.InputStream;

/*

- * A bogus key store which provides al the required information to

- * create an example SL conection.

*

- * To generate a bogus key store:

- * <pre>

- * keytol -genkey -alias securechat -keysize 2048 -validity 3650

- * -keyalg RSA -dname "CN=securechat"

- * -keypas secret -storepas secret

- * -keystore cert.jks

- * </pre>

- */ publicfinalclas SecureChatKeyStore {


privatestaticfinalshort[] DATA = { 0xfe,0xed,0xfe,0xed,0x0,0x0,0x0,0x02, 0x0,0x0,0x0,0x02,0x0,0x0,0x0,0x01, 0x0,0x07,0x65,0x78,0x61,0x6d,0x70,0x6c, 0x65,0x0,0x0,0x01,0x1a,0x9f,0x57,0xa5, 0x27,0x0,0x0,0x01,0x9a,0x30,0x82,0x01, 0x96,0x30,0x0e,0x06,0x0a,0x2b,0x06,0x01, 0x04,0x01,0x2a,0x02,0x1,0x01,0x01,0x05, 0x0,0x04,0x82,0x01,0x82,0x48,0x6d,0xcf, 0x16,0xb5,0x50,0x95,0x36,0xbf,0x47,0x27, 0x50,0x58,0x0d,0xa2,0x52,0x7e,0x25,0xab, 0x14,0x1a,0x26,0x5e,0x2d,0x8a,0x23,0x90, 0x60,0x7f,0x12,0x20,0x56,0xd1,0x43,0xa2, 0x6b,0x47,0x5d,0xed,0x9d,0xd4,0xe5,0x83,

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
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.
- 88.
- 89.
- 90.
- 91.
- 92.
- 93.
- 94.
- 95.
- 96.


0x28,0x89,0xc2,0x16,0x4c,0x76,0x06,0xad, 0x8e,0x8c,0x29,0x1a,0x9b,0x0f,0xd,0x60, 0x4b,0xb4,0x62,0x82,0x9e,0x4a,0x63,0x83, 0x2e,0xd2,0x43,0x78,0xc2,0x32,0x1f,0x60, 0xa9,0x8a,0x7f,0x0f,0x7c,0xa6,0x1d,0xe6, 0x92,0x9e,0x52,0xc7,0x7d,0xb,0x35,0x3b, 0xa,0x89,0x73,0x4c,0xfb,0x9,0x54,0x97, 0x9,0x28,0x6e,0x6,0x5b,0xf7,0x9b,0x7e, 0x6d,0x8a,0x2f,0xfa,0xc3,0x1e,0x71,0xb9, 0xbd,0x8f,0xc5,0x63,0x25,0x31,0x20,0x02, 0xf,0x02,0xf0,0xc9,0x2c,0xd,0x3a,0x10, 0x30,0xab,0xe5,0xad,0x3d,0x1a,0x82,0x77, 0x46,0xed,0x03,0x38,0xa4,0x73,0x6d,0x36, 0x36,0x3,0x70,0xb2,0x63,0x20,0xca,0x03, 0xbf,0x5a,0xf4,0x7c,0x35,0xf0,0x63,0x1a, 0x12,0x3,0x12,0x58,0xd9,0xa2,0x63,0x6b, 0x63,0x82,0x41,0x65,0x70,0x37,0x4b,0x9, 0x04,0x9f,0xd,0x5e,0x07,0x01,0x95,0x9f, 0x36,0xe8,0xc3,0x6,0x2a,0x21,0x69,0x68, 0x40,0xe6,0xbc,0xb,0x85,0x81,0x21,0x13, 0xe6,0xa4,0xcf,0xd3,0x67,0xe3,0xfd,0x75, 0xf0,0xdf,0x83,0xe0,0xc5,0x36,0x09,0xac,

- 0x1b,0xd4,0xf7,0x2a,0x23,0x57,0x1c,0x5c, 0x0f,0xf4,0xcf,0xa2,0xcf,0xf5,0xbd,0x9c, 0x69,0x98,0x78,0x3a,0x25,0xe4,0xfd,0x85, 0x1,0xc,0x7d,0xef,0xeb,0x74,0x60,0xb1, 0xb7,0xfb,0x1f,0x0e,0x62,0xf,0xfe,0x09, 0x0a,0xc3,0x80,0x2f,0x10,0x49,0x89,0x78, 0xd2,0x08,0xfa,0x89,0x2,0x45,0x91,0x21, 0xbc,0x90,0x3e,0xad,0xb3,0x0a,0xb4,0x0e,

- 0x1c,0xa1,0x93,0x92,0xd8,0x72,0x07,0x54, 0x60,0xe7,0x91,0xfc,0xd9,0x3c,0xe1,0x6f, 0x08,0xe4,0x56,0xf6,0x0b,0xb0,0x3c,0x39, 0x8a,0x2d,0x48,0x4,0x28,0x13,0xca,0xe9, 0xf7,0xa3,0xb6,0x8a,0x5f,0x31,0xa9,0x72, 0xf2,0xde,0x96,0xf2,0xb1,0x53,0xb1,0x3e, 0x24,0x57,0xfd,0x18,0x45,0x1f,0xc5,0x3, 0x1b,0xa4,0xe8,0x21,0xfa,0x0e,0xb2,0xb9, 0xcb,0xc7,0x07,0x41,0xd,0x2f,0xb6,0x6a, 0x23,0x18,0xed,0xc1,0xef,0xe2,0x4b,0xec, 0xc9,0xba,0xfb,0x46,0x43,0x90,0xd7,0xb5, 0x68,0x28,0x31,0x2b,0x8d,0xa8,0x51,0x63, 0xf7,0x53,0x9,0x19,0x68,0x85,0x6,0x0, 0x0,0x0,0x01,0x0,0x05,0x58,0x2e,0x35, 0x30,0x39,0x0,0x0,0x02,0x3a,0x30,0x82, 0x02,0x36,0x30,0x82,0x01,0xe0,0xa0,0x03, 0x02,0x01,0x02,0x02,0x04,0x48,0x59,0xf1, 0x92,0x30,0x0d,0x06,0x09,0x2a,0x86,0x48, 0x86,0xf7,0x0d,0x01,0x01,0x05,0x05,0x0,


- 97.
- 98.
- 99.
- 100.
- 101.
- 102.
- 103.
- 104.
- 105.
- 106.
- 107.
- 108.
- 109.
- 110.
- 111.
- 112.
- 113.
- 114.
- 115.
- 116.
- 117.
- 118.
- 119.
- 120.
- 121.
- 122.
- 123.
- 124.
- 125.
- 126.
- 127.
- 128.
- 129.
- 130.
- 131.
- 132.
- 133.
- 134.
- 135.
- 136.
- 137.
- 138.
- 139.
- 140.
- 141.
- 142.
- 143.
- 144.
- 145.


- 0x30,0x81,0xa0,0x31,0x0b,0x30,0x09,0x06, 0x03,0x5,0x04,0x06,0x13,0x02,0x4b,0x52,

- 0x31,0x13,0x30,0x1,0x06,0x03,0x5,0x04, 0x08,0x13,0x0a,0x4b,0x79,0x75,0x6e,0x67,


- 0x67,0x69,0x2d,0x64,0x6f,0x31,0x14,0x30,

- 0x12,0x06,0x03,0x5,0x04,0x07,0x13,0x0b, 0x53,0x65,0x6f,0x6e,0x67,0x6e,0x61,0x6d, 0x2d,0x73,0x69,0x31,0x1a,0x30,0x18,0x06,

- 0x03,0x5,0x04,0x0a,0x13,0x1,0x54,0x68, 0x65,0x20,0x4e,0x65,0x74,0x74,0x79,0x20, 0x50,0x72,0x6f,0x6a,0x65,0x63,0x74,0x31, 0x18,0x30,0x16,0x06,0x03,0x5,0x04,0x0b,

0x13,0x0f,0x45,0x78,0x61,0x6d,0x70,0x6c, 0x65,0x20,0x41,0x75,0x74,0x68,0x6f,0x72, 0x73,0x31,0x30,0x30,0x2e,0x06,0x03,0x5,

- 0x04,0x03,0x13,0x27,0x73,0x65,0x63,0x75, 0x72,0x65,0x63,0x68,0x61,0x74,0x2e,0x65, 0x78,0x61,0x6d,0x70,0x6c,0x65,0x2e,0x6e, 0x65,0x74,0x74,0x79,0x2e,0x67,0x6c,0x65, 0x61,0x6d,0x79,0x6e,0x6f,0x64,0x65,0x2e, 0x6e,0x65,0x74,0x30,0x20,0x17,0x0d,0x30, 0x38,0x30,0x36,0x31,0x39,0x30,0x35,0x34, 0x31,0x3,0x38,0x5a,0x18,0x0f,0x32,0x31, 0x38,0x37,0x31,0x31,0x32,0x34,0x30,0x35, 0x34,0x31,0x3,0x38,0x5a,0x30,0x81,0xa0, 0x31,0x0b,0x30,0x09,0x06,0x03,0x5,0x04, 0x06,0x13,0x02,0x4b,0x52,0x31,0x13,0x30, 0x1,0x06,0x03,0x5,0x04,0x08,0x13,0x0a, 0x4b,0x79,0x75,0x6e,0x67,0x67,0x69,0x2d,

- 0x64,0x6f,0x31,0x14,0x30,0x12,0x06,0x03,

0x5,0x04,0x07,0x13,0x0b,0x53,0x65,0x6f, 0x6e,0x67,0x6e,0x61,0x6d,0x2d,0x73,0x69, 0x31,0x1a,0x30,0x18,0x06,0x03,0x5,0x04, 0x0a,0x13,0x1,0x54,0x68,0x65,0x20,0x4e,

- 0x65,0x74,0x74,0x79,0x20,0x50,0x72,0x6f, 0x6a,0x65,0x63,0x74,0x31,0x18,0x30,0x16, 0x06,0x03,0x5,0x04,0x0b,0x13,0x0f,0x45,






- 0x78,0x61,0x6d,0x70,0x6c,0x65,0x20,0x41, 0x75,0x74,0x68,0x6f,0x72,0x73,0x31,0x30, 0x30,0x2e,0x06,0x03,0x5,0x04,0x03,0x13, 0x27,0x73,0x65,0x63,0x75,0x72,0x65,0x63,

0x68,0x61,0x74,0x2e,0x65,0x78,0x61,0x6d, 0x70,0x6c,0x65,0x2e,0x6e,0x65,0x74,0x74,

- 0x79,0x2e,0x67,0x6c,0x65,0x61,0x6d,0x79, 0x6e,0x6f,0x64,0x65,0x2e,0x6e,0x65,0x74, 0x30,0x5c,0x30,0x0d,0x06,0x09,0x2a,0x86, 0x48,0x86,0xf7,0x0d,0x01,0x01,0x01,0x05, 0x0,0x03,0x4b,0x0,0x30,0x48,0x02,0x41, 0x0,0xc3,0xe3,0x5e,0x41,0xa7,0x87,0x1,




- 146.
- 147.
- 148.
- 149.
- 150.
- 151.
- 152.
- 153.
- 154.
- 155.
- 156.
- 157.
- 158.
- 159.
- 160.
- 161.
- 162.
- 163.
- 164.
- 165.
- 166.
- 167.
- 168.
- 169.
- 170.
- 171.
- 172.
- 173.
- 174.
- 175.
- 176.
- 177.
- 178.
- 179.
- 180.
- 181.
- 182.
- 183.
- 184.
- 185.
- 186.
- 187.
- 188.
- 189.
- 190.
- 191.
- 192.
- 193.
- 194.


0x0,0x42,0x2a,0xb0,0x4b,0xed,0xb2,0xe0, 0x23,0xdb,0xb1,0x3d,0x58,0x97,0x35,0x60,

- 0x0b,0x82,0x59,0xd3,0x0,0xea,0xd4,0x61, 0xb8,0x79,0x3f,0xb6,0x3c,0x12,0x05,0x93, 0x2e,0x9a,0x59,0x68,0x14,0x77,0x3a,0xc8, 0x50,0x25,0x57,0xa4,0x49,0x18,0x63,0x41, 0xf0,0x2d,0x28,0xec,0x06,0xfb,0xb4,0x9f, 0xbf,0x02,0x03,0x01,0x0,0x01,0x30,0x0d, 0x06,0x09,0x2a,0x86,0x48,0x86,0xf7,0x0d,

- 0x01,0x01,0x05,0x05,0x0,0x03,0x41,0x0, 0x65,0x6c,0x30,0x01,0xc2,0x8e,0x3e,0xcb, 0xb3,0x77,0x48,0xe9,0x6,0x61,0x9a,0x40, 0x86,0xaf,0xf6,0x03,0xeb,0xba,0x6a,0xf2, 0xfd,0xe2,0xaf,0x36,0x5e,0x7b,0xa,0x2, 0x04,0xd,0x2c,0x20,0xc4,0xfc,0xd,0xd0, 0x82,0x20,0x1c,0x3d,0xd7,0x9e,0x5e,0x5c, 0x92,0x5a,0x76,0x71,0x28,0xf5,0x07,0x7d, 0xa2,0x81,0xba,0x77,0x9f,0x2a,0xd9,0x4,


- 0x0,0x0,0x0,0x01,0x0,0x05,0x6d,0x79, 0x6b,0x65,0x79,0x0,0x0,0x01,0x1a,0x9f, 0x5b,0x56,0xa0,0x0,0x0,0x01,0x9,0x30, 0x82,0x01,0x95,0x30,0x0e,0x06,0x0a,0x2b, 0x06,0x01,0x04,0x01,0x2a,0x02,0x1,0x01,

- 0x01,0x05,0x0,0x04,0x82,0x01,0x81,0x29, 0xa8,0xb6,0x08,0x0c,0x85,0x75,0x3e,0xd, 0xb5,0xe5,0x1a,0x87,0x68,0xd1,0x90,0x4b, 0x29,0x31,0xe,0x90,0xbc,0x9d,0x73,0xa0, 0x3f,0xe9,0x0b,0xa4,0xef,0x30,0x9b,0x36, 0x9a,0xb2,0x54,0x77,0x81,0x07,0x4b,0xa, 0xa5,0x77,0x98,0xe1,0xeb,0xb5,0x7c,0x4e, 0x48,0xd5,0x08,0xfc,0x2c,0x36,0xe2,0x65, 0x03,0xac,0xe5,0xf3,0x96,0xb7,0xd0,0xb5, 0x3b,0x92,0xe4,0x14,0x05,0x7a,0x6a,0x92, 0x56,0xfe,0x4e,0xab,0xd3,0x0e,0x32,0x04,

- 0x2,0x2,0x74,0x47,0x7d,0xec,0x21,0x9, 0x30,0x31,0x64,0x46,0x64,0x9b,0xc7,0x13, 0xbf,0xbe,0xd0,0x31,0x49,0xe7,0x3c,0xbf, 0xba,0xb1,0x20,0xf9,0x42,0xf4,0xa9,0xa9, 0xe5,0x13,0x65,0x32,0xbf,0x7c,0xc,0x91, 0xd3,0xfd,0x24,0x47,0x0b,0xe5,0x53,0xad, 0x50,0x30,0x56,0xd1,0xfa,0x9c,0x37,0xa8, 0xc1,0xce,0xf6,0x0b,0x18,0xa,0x7c,0xab, 0xbd,0x1f,0xdf,0xe4,0x80,0xb8,0xa7,0xe0, 0xad,0x7d,0x50,0x74,0xf1,0x98,0x78,0xbc, 0x58,0xb9,0xc2,0x52,0xbe,0xd2,0x5b,0x81, 0x94,0x83,0x8f,0xb9,0x4c,0xe,0x01,0x2b, 0x5e,0xc9,0x6e,0x9b,0xf5,0x63,0x69,0xe4, 0xd8,0x0b,0x47,0xd8,0xfd,0xd8,0xe0,0xed, 0xa8,0x27,0x03,0x74,0x1e,0x5d,0x32,0xe6,


- 195.
- 196.
- 197.
- 198.
- 199.
- 200.
- 201.
- 202.
- 203.
- 204.
- 205.
- 206.
- 207.
- 208.
- 209.
- 210.
- 211.
- 212.
- 213.
- 214.
- 215.
- 216.
- 217.
- 218.
- 219.
- 220.
- 221.
- 222.
- 223.
- 224.
- 225.
- 226.
- 227.
- 228.
- 229.
- 230.
- 231.
- 232.
- 233.
- 234.
- 235.
- 236.
- 237.
- 238.
- 239.
- 240.
- 241.
- 242.
- 243.


0x5c,0x63,0xc2,0xfb,0x3f,0xe,0xb4,0x13, 0xc6,0x0e,0x6e,0x74,0xe0,0x2,0xac,0xce, 0x79,0xf9,0x43,0x68,0xc1,0x03,0x74,0x2b, 0xe1,0x18,0xf8,0x7f,0x76,0x9a,0xea,0x82, 0x3f,0xc2,0xa6,0xa7,0x4c,0xfe,0xae,0x29, 0x3b,0xc1,0x10,0x7c,0xd5,0x77,0x17,0x79, 0x5f,0xcb,0xad,0x1f,0xd8,0xa1,0xfd,0x90, 0xe1,0x6b,0xb2,0xef,0xb9,0x41,0x26,0xa4, 0x0b,0x4f,0xc6,0x83,0x05,0x6f,0xf0,0x64, 0x40,0xe1,0x4,0xc4,0xf9,0x40,0x2b,0x3b, 0x40,0xdb,0xaf,0x35,0xa4,0x9b,0x9f,0xc4, 0x74,0x07,0xe5,0x18,0x60,0xc5,0xfe,0x15, 0x0e,0x3a,0x25,0x2a,0x1,0xe,0x78,0x2f, 0xb8,0xd1,0x6e,0x4e,0x3c,0x0a,0xb5,0xb9, 0x40,0x86,0x27,0x6d,0x8f,0x53,0xb7,0x77, 0x36,0xec,0x5d,0xed,0x32,0x40,0x43,0x82, 0xc3,0x52,0x58,0xc4,0x26,0x39,0xf3,0xb3, 0xad,0x58,0xab,0xb7,0xf7,0x8e,0x0e,0xba, 0x8e,0x78,0x9d,0xbf,0x58,0x34,0xbd,0x77,

- 0x73,0xa6,0x50,0x5,0x0,0x60,0x26,0xbf, 0x6d,0xb4,0x98,0x8a,0x18,0x83,0x89,0xf8, 0xcd,0x0d,0x49,0x06,0xae,0x51,0x6e,0xaf, 0xbd,0xe2,0x07,0x13,0xd8,0x64,0xc,0xbf, 0x0,0x0,0x0,0x01,0x0,0x05,0x58,0x2e, 0x35,0x30,0x39,0x0,0x0,0x02,0x34,0x30, 0x82,0x02,0x30,0x30,0x82,0x01,0xda,0xa0,

- 0x03,0x02,0x01,0x02,0x02,0x04,0x48,0x59, 0xf2,0x84,0x30,0x0d,0x06,0x09,0x2a,0x86, 0x48,0x86,0xf7,0x0d,0x01,0x01,0x05,0x05, 0x0,0x30,0x81,0x9d,0x31,0x0b,0x30,0x09, 0x06,0x03,0x5,0x04,0x06,0x13,0x02,0x4b, 0x52,0x31,0x13,0x30,0x1,0x06,0x03,0x5,

- 0x04,0x08,0x13,0x0a,0x4b,0x79,0x75,0x6e,


- 0x67,0x67,0x69,0x2d,0x64,0x6f,0x31,0x14,

- 0x30,0x12,0x06,0x03,0x5,0x04,0x07,0x13, 0x0b,0x53,0x65,0x6f,0x6e,0x67,0x6e,0x61, 0x6d,0x2d,0x73,0x69,0x31,0x1a,0x30,0x18, 0x06,0x03,0x5,0x04,0x0a,0x13,0x1,0x54,

0x68,0x65,0x20,0x4e,0x65,0x74,0x74,0x79, 0x20,0x50,0x72,0x6f,0x6a,0x65,0x63,0x74,

- 0x31,0x15,0x30,0x13,0x06,0x03,0x5,0x04, 0x0b,0x13,0x0c,0x43,0x6f,0x6e,0x74,0x72,


- 0x69,0x62,0x75,0x74,0x6f,0x72,0x73,0x31, 0x30,0x30,0x2e,0x06,0x03,0x5,0x04,0x03, 0x13,0x27,0x73,0x65,0x63,0x75,0x72,0x65, 0x63,0x68,0x61,0x74,0x2e,0x65,0x78,0x61, 0x6d,0x70,0x6c,0x65,0x2e,0x6e,0x65,0x74,


- 0x74,0x79,0x2e,0x67,0x6c,0x65,0x61,0x6d, 0x79,0x6e,0x6f,0x64,0x65,0x2e,0x6e,0x65,


- 244.
- 245.
- 246.
- 247.
- 248.
- 249.
- 250.
- 251.
- 252.
- 253.
- 254.
- 255.
- 256.
- 257.
- 258.
- 259.
- 260.
- 261.
- 262.
- 263.
- 264.
- 265.
- 266.
- 267.
- 268.
- 269.
- 270.
- 271.
- 272.
- 273.
- 274.
- 275.
- 276.
- 277.
- 278.
- 279.
- 280.
- 281.
- 282.
- 283.
- 284.
- 285.
- 286.
- 287.
- 288.
- 289.
- 290.
- 291.
- 292.


- 0x74,0x30,0x20,0x17,0x0d,0x30,0x38,0x30, 0x36,0x31,0x39,0x30,0x35,0x34,0x35,0x34,

- 0x30,0x5a,0x18,0x0f,0x32,0x31,0x38,0x37,

- 0x31,0x31,0x32,0x3,0x30,0x35,0x34,0x35, 0x34,0x30,0x5a,0x30,0x81,0x9d,0x31,0x0b,


- 0x30,0x09,0x06,0x03,0x5,0x04,0x06,0x13,

- 0x02,0x4b,0x52,0x31,0x13,0x30,0x1,0x06,

- 0x03,0x5,0x04,0x08,0x13,0x0a,0x4b,0x79,

0x75,0x6e,0x67,0x67,0x69,0x2d,0x64,0x6f, 0x31,0x14,0x30,0x12,0x06,0x03,0x5,0x04, 0x07,0x13,0x0b,0x53,0x65,0x6f,0x6e,0x67, 0x6e,0x61,0x6d,0x2d,0x73,0x69,0x31,0x1a, 0x30,0x18,0x06,0x03,0x5,0x04,0x0a,0x13, 0x1,0x54,0x68,0x65,0x20,0x4e,0x65,0x74, 0x74,0x79,0x20,0x50,0x72,0x6f,0x6a,0x65, 0x63,0x74,0x31,0x15,0x30,0x13,0x06,0x03, 0x5,0x04,0x0b,0x13,0x0c,0x43,0x6f,0x6e, 0x74,0x72,0x69,0x62,0x75,0x74,0x6f,0x72, 0x73,0x31,0x30,0x30,0x2e,0x06,0x03,0x5,

- 0x04,0x03,0x13,0x27,0x73,0x65,0x63,0x75, 0x72,0x65,0x63,0x68,0x61,0x74,0x2e,0x65, 0x78,0x61,0x6d,0x70,0x6c,0x65,0x2e,0x6e, 0x65,0x74,0x74,0x79,0x2e,0x67,0x6c,0x65, 0x61,0x6d,0x79,0x6e,0x6f,0x64,0x65,0x2e, 0x6e,0x65,0x74,0x30,0x5c,0x30,0x0d,0x06, 0x09,0x2a,0x86,0x48,0x86,0xf7,0x0d,0x01, 0x01,0x01,0x05,0x0,0x03,0x4b,0x0,0x30,

- 0x48,0x02,0x41,0x0,0x95,0xb3,0x47,0x17, 0x95,0x0f,0x57,0xcf,0x6,0x72,0x0a,0x7e,

0x5b,0x54,0xea,0x8c,0x6f,0x79,0xde,0x94, 0xac,0x0b,0x5a,0xd4,0xd6,0x1b,0x58,0x12, 0x1a,0x16,0x3d,0xfe,0xdf,0xa5,0x2b,0x86, 0xbc,0x64,0xd4,0x80,0x1e,0x3f,0xf9,0xe2, 0x04,0x03,0x79,0x9b,0xc1,0x5c,0xf0,0xf1, 0xf3,0xf1,0xe3,0xbf,0x3f,0xc0,0x1f,0xd, 0xdb,0xc0,0x5b,0x21,0x02,0x03,0x01,0x0, 0x01,0x30,0x0d,0x06,0x09,0x2a,0x86,0x48, 0x86,0xf7,0x0d,0x01,0x01,0x05,0x05,0x0, 0x03,0x41,0x0,0x02,0xd7,0xd,0xbd,0x0c, 0x8e,0x21,0x20,0xef,0x9e,0x4f,0x1f,0xf5,

- 0x49,0xf1,0xae,0x58,0x9b,0x94,0x3a,0x1f, 0x70,0x3,0xf0,0x9b,0xb,0xe9,0xc0,0xf3, 0x72,0xcb,0xde,0xb6,0x56,0x72,0xc,0x1c, 0xf0,0xd6,0x5a,0x2a,0xbc,0xa1,0x7e,0x23, 0x83,0xe9,0xe7,0xcf,0x9e,0xa5,0xf9,0xc, 0xc2,0x61,0xf4,0xdb,0x40,0x93,0x1d,0x63, 0x8a,0x50,0x4c,0x1,0x39,0xb1,0x91,0xc1, 0xe6,0x9d,0xd9,0x1a,0x62,0x1b,0xb8,0xd3, 0xd6,0x9a,0x6d,0xb9,0x8e,0x15,0x51 };








- 293.
- 294.
- 295.
- 296.
- 297.
- 298.
- 299.
- 300.
- 301.
- 302.
- 303.
- 304.
- 305.
- 306.
- 307.
- 308.
- 309.
- 310.
- 311.
- 312.
- 313.


publicstatic InputStream asInputStream() { byte[] data =newbyte[DATA.length]; for (int i =0; i < data.length; i +) {

data[i] = (byte) DATA[i]; }

returnnew ByteArrayInputStream(data); }

publicstaticchar[] getCertificatePasword() {

return"secret".toCharArray(); }

publicstaticchar[] getKeyStorePasword() {

return"secret".toCharArray(); }

private SecureChatKeyStore() {

/ Unused }

}

[java] view plaincopy

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
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.


/*

- * Copyright 2012 The Nety Project

*

- * The Nety Project licenses this file to you under the Apache License,

- * version 2.0 (the "License"); you may not use this file except in compliance

- * with the License. You may obtain a copy of the License at:

*

- * htp:/ w.apache.org/licenses/LICENSE-2.0

*

- * Unles required by aplicable law or agred to in writing, software

- * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT

- * WARANTIES OR CONDITIONS OF ANY KIND, either expres or implied. Se the

- * License for the specific language governing permi sions and limitations

- * under the License.

- */ package nety.in.action.spdy;


import io.nety.handler.sl.SslHandler; import io.nety.util.internal.SystemPropertyUtil;

import java.security.KeyStore; import java.security.SecureRandom;

import javax.net.sl.KeyManager; import javax.net.sl.KeyManagerFactory; import javax.net.sl. SLContext; import javax.net.sl. SLEngine; import javax.net.sl.TrustManager;

/*

- * Creates a bogus {@link SLContext}. A client-side context created by this

- * factory acepts any certificate even if it is invalid. A server-side context

- * created by this factory sends a bogus certificate defined in {@link SecureChatKeyStore}.

- * <p>

- * You wil have to create your context diferently in a real world aplication.

*

- * <h3>Client Certificate Authentication</h3>

*

- * To enable client certificate authentication:

- * <ul>

- * <li>Enable client authentication on the server side by caling

- * {@link SLEngine#setNedClientAuth(bolean)} before creating

- * {@link SslHandler}.</li>

- * <li>When initializing an {@link SLContext} on the client side,

- * specify the {@link KeyManager} that contains the client certificate as


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
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.
- 88.
- 89.


- * the first argument of {@link SLContext#init(KeyManager[], TrustManager[], SecureRandom)}.</li>

- * <li>When initializing an {@link SLContext} on the server side,

- * specify the proper {@link TrustManager} as the second argument of

- * {@link SLContext#init(KeyManager[], TrustManager[], SecureRandom)}

- * to validate the client certificate.</li>

- * </ul>

- */ publicfinalclas SecureChatSslContextFactory {


privatestaticfinal String PROTOCOL ="TLS"; privatestaticfinal SLContext SERVER_CONTEXT; privatestaticfinal SLContext CLIENT_CONTEXT;

static {

String algorithm = SystemPropertyUtil.get("sl.KeyManagerFactory.algorithm");

if (algorithm =nul) {

algorithm ="SunX509"; }

SLContext serverContext; SLContext clientContext; try {

KeyStore ks = KeyStore.getInstance("JKS"); ks.load(SecureChatKeyStore.asInputStream(),

SecureChatKeyStore.getKeyStorePasword();

/ Set up key manager factory to use our key store KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm); kmf.init(ks, SecureChatKeyStore.getCertificatePasword();

/ Initialize the SLContext to work with our key managers. serverContext = SLContext.getInstance(PROTOCOL);

serverContext.init(kmf.getKeyManagers(),nul,nul); }catch (Exception e) { thrownew Error(

"Failed to initialize the server-side SLContext", e); }

try {

clientContext = SLContext.getInstance(PROTOCOL);

clientContext.init(nul, SecureChatTrustManagerFactory.getTrustManagers(),nul); }catch (Exception e) { thrownew Error(

"Failed to initialize the client-side SLContext", e);

- 90.
- 91.
- 92.
- 93.
- 94.
- 95.
- 96.
- 97.
- 98.
- 99.
- 100.
- 101.
- 102.
- 103.
- 104.
- 105.
- 106.
- 107.


}

SERVER_CONTEXT = serverContext; CLIENT_CONTEXT = clientContext;

}

publicstatic SLContext getServerContext() {

return SERVER_CONTEXT; }

publicstatic SLContext getClientContext() {

return CLIENT_CONTEXT; }

private SecureChatSslContextFactory() {

/ Unused }

}

# 12.4 Summary

这⼀章没有详细的按照netty in action书中来翻译，因为我感觉书中讲的很多都不是netty的重 点，鄙⼈英⽂能实在有限，所以也就把精⼒不放在⾮核⼼上⾯了。若有读者需要详细在netty中使 ⽤spdy可以查看其它相关资料或⽂章，或者看本篇博⽂的例⼦代码。后⾯⼏章也会如此。

第⼗三章：通过UDP⼴播事件

本章介绍

UDP介绍 UDP程序结构和设计 ⽇志事件POJO 编写⼴播器 编写监听者 使⽤⼴播器和监听者 Summary

前⾯的章节都是在⽰例中使⽤TCP协议，这⼀章，我们将使⽤UDP。UDP是⼀种⽆连接协议，若 需要很⾼的性能和对数据的完成性没有严格要求，那使⽤UDP是⼀个很好的⽅法。最著名的基于UDP 协议的是⽤来域名解析的DNS。

Netty使⽤了统⼀的传输API，这使得编写基于UDP的应⽤程序很容易。可以重⽤现有的 ChannelHandler和其他公共组件来编写另外的Netty程序。看完本章后，你就会知道什么事⽆连接 协议以及为什么UDP可能适合你的应⽤程序。

# 13.1 UDP介绍

在深⼊探讨UDP之前，我们先了解UDP是什么，以及UDP有什么限制或问题。UDP是⼀种⽆ 连接的协议，也就是说客户端和服务器在交互数据之前不会像TCP那样事先建⽴连接。

UDP是User Datagram Protocol的简称，即⽤户数据报协议。UDP有不提供数据报分组、组 装和不能对数据报进⾏排序的缺点，也就是说，当数据报发送之后是⽆法确认数据是否完整到达 的。

UDP协议的主要作⽤是将⽹络数据流量压缩成数据包的形式。⼀个典型的数据包就是⼀个⼆ 进制数据的传输单位。每⼀个数据包的前8个字节⽤来包含报头信息，剩余字节则⽤来包含具体的 传输数据。

在选择使⽤协议的时候，选择UDP必须要谨慎。在⽹络质量令⼈⼗分不满意的环境下，UDP 协议数据包丢失会⽐较严重。但是由于UDP的特性：它不属于连接型协议，因⽽具有资源消耗 ⼩，处理速度快的优点，所以通常⾳频、视频和普通数据在传送时使⽤UDP较多，因为它们即使 偶尔丢失⼀两个数据包，也不会对接收结果产⽣太⼤影响。⽐如我们聊天⽤的ICQ和QQ就是使⽤ 的UDP协议。

UDP就介绍到这⾥，更详细的资料可以百度或⾕歌。

## 13.2 UDP程序结构和设计

本章例⼦中，程序打开⼀个⽂件并将⽂件内容⼀⾏⼀⾏的通过UDP⼴播到其他的接收主机， 这很像UNIX操作系统的⽇志系统。对于像发送⽇志的需求，UDP⾮常适合这样的应⽤程序，并可 以使⽤UDP通过⽹络发送⼤量的“事件”。

使⽤UDP可以在同⼀个主机上启动多个应⽤程序并能独⽴的进⾏数据报的发送和接收，UDP 使⽤底层的互联⽹协议来传送报⽂，同IP⼀样提供不可靠的⽆连接数据报传输服务，它不提供报 ⽂到达确认、排序、及流量控制等功能。每个UDP报⽂分UDP报头和UDP数据区两部分，报头由 四个16位长（2字节）字段组成，分别说明该报⽂的源端⼜、⽬的端⼜、报⽂长度以及校验值；数 据库就是传输的具体数据。

UDP最好在局域⽹内使⽤，这样可以⼤⼤减少丢包概率。UDP有如下特性：

UDP是⼀个⽆连接协议，传输数据之前源端和终端不建⽴连接，当它想传送时就简单地去抓 取来⾃应⽤程序的数据，并尽可能快地把它扔到⽹络上。在发送端，UDP传送数据的速度仅 仅是受应⽤程序⽣成数据的速度、计算机的能⼒和传输带宽的限制；在接收端，UDP把每个 消息段放在队列中，应⽤程序每次从队列中读⼀个消息段。 由于传输数据不建⽴连接，因此也就不需要维护连接状态，包括收发状态等，因此⼀台服务 机可同时向多个客户机传输相同的消息。 UDP信息包的标题很短，只有8个字节，相对于TCP的20个字节信息包的额外开销很⼩。 吞吐量不受拥挤控制算法的调节，只受应⽤软件⽣成数据的速率、传输带宽、源端和终端主 机性能的限制。 UDP使⽤尽最⼤努⼒交付，即不保证可靠交付，因此主机不需要维持复杂的链接状态表（这 ⾥⾯有许多参数）。

- 2.
- 3.
- 4.
- 5.
- 6.


UDP是⾯向报⽂的。发送⽅的UDP对应⽤程序交下来的报⽂，在添加⾸部后就向下交付给IP 层。既不拆分，也不合并，⽽是保留这些报⽂的边界，因此，应⽤程序需要选择合适的报⽂ ⼤⼩。

本章UDP程序例⼦的⽰意图⼊如下：

![image 108](assets/imageFile108.png)

从上图可以看出，例⼦程序由两部分组成：⼴播⽇志⽂件和“监控器”，监控器⽤于接收⼴播。为了 简单，我们将不做任何形式的⾝份验证或加密。

# 13.3 ⽇志事件POJO

我们的应⽤程序通常需要某种“消息POJO”⽤于保存消息，我们把这个消息POJO看成是⼀个 “事件消息”在本例⼦中我们也创建⼀个POJO叫做LogEvent，LogEvent⽤来存储事件数据，然后将 数据输出到⽇志⽂件。看下⾯代码：

###### [java] view plaincopy

package nety.in.action.udp;

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
- 37.
- 38.
- 39.
- 40.
- 41.


import java.net.InetSocketAdres;

publicclas LogEvent {

publicstaticfinalbyte SEPARATOR = (byte)'|';

privatefinal InetSocketAdres source; privatefinal String logfile; privatefinal String msg; privatefinallong received;

public LogEvent(String logfile, String msg) {

this(nul, -1, logfile, msg); }

public LogEvent(InetSocketAdres source,long received, String logfile, String msg) { this.source = source; this.logfile = logfile; this.msg = msg; this.received = received;

}

public InetSocketAdres getSource() {

return source; }

public String getLogfile() {

return logfile; }

public String getMsg() {

return msg; }

publiclong getReceived() {

return received; }

}

接下来的章节，我们将⽤这个POJO类来实现具体的逻辑。

## 13.4 编写⼴播器

我们要做的是⼴播⼀个DatagramPacket⽇志条⽬，如下图所⽰：

![image 109](assets/imageFile109.png)

上图显⽰我们有⼀个从⽇志条路到DatagramPacket⼀对⼀的关系。如同所有的基于Netty的

应⽤程序⼀样，它由⼀个或多个ChannelHandler和⼀些实体对象绑定，⽤于引导该应⽤程序。⾸ 先让我们来看看LogEventBroadcaster的ChannelPipeline以及作为数据载体的LogEvent的流向， 看下图：

![image 110](assets/imageFile110.png)

上图显⽰，LogEventBroadcaster使⽤LogEvent消息并将消息写⼊本地Channel，所有的信息 封装在LogEvent消息中，这些消息被传到ChannelPipeline中。流进ChannelPipeline的LogEvent 消息被编码成DatagramPacket消息，最后通过UDP⼴播到远程对等通道。

这可以归结为有⼀个⾃定义的ChannelHandler，从LogEvent消息编程成DatagramPacket消 息。回忆我们在第七章讲解的编解码器，我们定义个LogEventEncoder，代码如下：

[java] view plaincopy

import io.nety.bufer.ByteBuf; import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.socket.DatagramPacket; import io.nety.handler.codec.MesageToMesageEncoder; import io.nety.util.CharsetUtil;

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


import java.net.InetSocketAdres; import java.util.List;

publicclas LogEventEncoderextends MesageToMesageEncoder<LogEvent> {

privatefinal InetSocketAdres remoteAdres;

public LogEventEncoder(InetSocketAdres remoteAdres){

this.remoteAdres = remoteAdres; }

@Override

protectedvoid encode(ChanelHandlerContext ctx, LogEvent msg, List<Object> out)

throws Exception { ByteBuf buf = ctx.aloc().bufer(); buf.writeBytes(msg.getLogfile().getBytes(CharsetUtil.UTF_8); buf.writeByte(LogEvent.SEPARATOR); buf.writeBytes(msg.getMsg().getBytes(CharsetUtil.UTF_8);

out.ad(new DatagramPacket(buf, remoteAdres); }

}

下⾯我们再编写⼀个⼴播器：

[java] view plaincopy

import io.nety.botstrap.Botstrap; import io.nety.chanel.Chanel; import io.nety.chanel.ChanelOption; import io.nety.chanel.EventLopGroup; import io.nety.chanel.nio.NioEventLopGroup; import io.nety.chanel.socket.nio.NioDatagramChanel;

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
- 37.
- 38.
- 39.


import java.io.File; import java.io.IOException; import java.io.RandomAcesFile; import java.net.InetSocketAdres;

publicclas LogEventBroadcaster {

privatefinal EventLopGroup group; privatefinal Botstrap botstrap; privatefinal File file;

public LogEventBroadcaster(InetSocketAdres adres, File file) { group =new NioEventLopGroup(); botstrap =new Botstrap(); botstrap.group(group).chanel(NioDatagramChanel.clas)

.option(ChanelOption.SO_BROADCAST,true)

.handler(new LogEventEncoder(adres); this.file = file;

}

publicvoid run()throws IOException {

Chanel ch = botstrap.bind(0).syncUninterruptibly().chanel(); long pointer =0; for (;) {

long len = file.length(); if (len < pointer) {

pointer = len; }else {

RandomAcesFile raf =new RandomAcesFile(file,"r"); raf.sek(pointer);

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
- 72.
- 73.


String line;

while(line = raf.readLine() !=nul) { ch.write(new LogEvent(nul, -1, file.getAbsolutePath(), line);

} ch.flush(); pointer = raf.getFilePointer(); raf.close();

}

try {

Thread.sl ep(1 0);

}catch (InterruptedException e) {

Thread.interrupted();

break; }

} }

publicvoid stop() {

group.shutdownGracefuly(); }

publicstaticvoid main(String[] args)throws Exception { int port =4096;

String path = System.getProperty("user.dir") +"/log.txt";

LogEventBroadcaster broadcaster =new LogEventBroadcaster(new InetSocketAdres(

"25.25.25.25", port),new File(path); try {

broadcaster.run(); }finaly {

broadcaster.stop(); }

}

}

## 13.5 编写监听者

这⼀节我们编写⼀个监听者：EventLogMonitor，也就是⽤来接收数据的程序。 EventLogMonitor做下⾯事情：

接收LogEventBroadcaster⼴播的DatagramPacket 解码LogEvent消息 输出LogEvent

EventLogMonitor的⽰意图如下：

![image 111](assets/imageFile111.png)

解码器代码如下：

[java] view plaincopy

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


package nety.in.action.udp;

import io.nety.bufer.ByteBuf; import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.socket.DatagramPacket; import io.nety.handler.codec.MesageToMesageDecoder; import io.nety.util.CharsetUtil;

import java.util.List;

publicclas LogEventDecoderextends MesageToMesageDecoder<DatagramPacket> {

@Override

protectedvoid decode(ChanelHandlerContext ctx, DatagramPacket msg, List<Object> out)

throws Exception { ByteBuf buf = msg.content();

int i = buf.indexOf(0, buf.readableBytes(), LogEvent.SEPARATOR); String filename = buf.slice(0, i).toString(CharsetUtil.UTF_8); String logMsg = buf.slice(i +1, buf.readableBytes().toString(CharsetUtil.UTF_8);

LogEvent event =new LogEvent(msg.sender(),

System.currentTimeMilis(), filename, logMsg); out.ad(event);

}

}

处理消息的Handler代码如下：

[java] view plaincopy

import io.nety.chanel.ChanelHandlerContext; import io.nety.chanel.SimpleChanelInboundHandler;

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


publicclas LogEventHandlerextends SimpleChanelInboundHandler<LogEvent> {

@Override

protectedvoid chanelRead0(ChanelHandlerContext ctx, LogEvent msg)throws Exception {

StringBuilder builder =new StringBuilder(); builder.apend(msg.getReceived(); builder.apend(" ["); builder.apend(msg.getSource().toString(); builder.apend("] ["); builder.apend(msg.getLogfile(); builder.apend("] : "); builder.apend(msg.getMsg(); System.out.println(builder.toString();

} }

EventLogMonitor代码如下：

[java] view plaincopy

import io.nety.botstrap.Botstrap; import io.nety.chanel.Chanel; import io.nety.chanel.ChanelInitializer; import io.nety.chanel.ChanelOption; import io.nety.chanel.ChanelPipeline; import io.nety.chanel.EventLopGroup; import io.nety.chanel.nio.NioEventLopGroup; import io.nety.chanel.socket.nio.NioDatagramChanel;

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
- 37.
- 38.
- 39.
- 40.


import java.net.InetSocketAdres;

publicclas LogEventMonitor {

privatefinal EventLopGroup group; privatefinal Botstrap botstrap;

public LogEventMonitor(InetSocketAdres adres) { group =new NioEventLopGroup(); botstrap =new Botstrap(); botstrap.group(group).chanel(NioDatagramChanel.clas)

.option(ChanelOption.SO_BROADCAST,true)

.handler(new ChanelInitializer<Chanel>() {

@Override

protectedvoid initChanel(Chanel chanel)throws Exception {

ChanelPipeline pipeline = chanel.pipeline(); pipeline.adLast(new LogEventDecoder(); pipeline.adLast(new LogEventHandler();

}

}).localAdres(adres); }

public Chanel bind() {

return botstrap.bind().syncUninterruptibly().chanel(); }

publicvoid stop() {

group.shutdownGracefuly(); }

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


publicstaticvoid main(String[] args)throws InterruptedException {

LogEventMonitor monitor =new LogEventMonitor(new InetSocketAdres(4096); try {

Chanel chanel = monitor.bind(); System.out.println("LogEventMonitor runing"); chanel.closeFuture().sync();

}finaly {

monitor.stop(); }

} }

# 13.6 使⽤LogEventBroadcaster和LogEventMonitor

为避免LogEventMonitor接收不到数据，我们必须先启动LogEventMonitor后，再启动 LogEventBroadcaster，输出内容这么就不贴图了，读者可以⾃⼰运营本例⼦测试。

# 13.7 Summary

本章依然没按照原书中的来翻译，主要是以⼀个例⼦来说明UDP在Netty中的使⽤。概念性的 东西都是从⽹上复制的，读者只需要了解UDP的概念再了解清楚例⼦代码的含义，并试着运⾏⼀ 些例⼦。

第⼗四章：实现⾃定义的编码解码器

本章讲述Netty中如何轻松实现定制的编解码器，由于Netty架构的灵活性，这些编解码器易于重 ⽤和测试。为了更容易实现，使⽤Memcached作为协议例⼦是因为它更⽅便我们实现。

Memcached是免费开源、⾼性能、分布式的内存对象缓存系统，其⽬的是加速动态Web应⽤ 程序的响应，减轻数据库负载；Memcache实际上是⼀个以key-value存储任意数据的内存⼩块。 可能有⼈会问“为什么使⽤Memcached？”，因为Memcached协议⾮常简单，便于讲解。

## 14.1 编解码器的范围

我们将只实现Memcached协议的⼀个⼦集，这⾜够我们进⾏添加、检索、删除对象；在 Memcached中是通过执⾏SET,GET,DELETE命令来实现的。Memcached⽀持很多其他的命令， 但我们只使⽤其中三个命令，简单的东西，我们才会理解的更清楚。

Memcached有⼀个⼆进制和纯⽂本协议，它们都可以⽤来与Memcached服务器通信，使⽤ 什么类型的协议取决于服务器⽀持哪些协议。本章主要关注实现⼆进制协议，因为⼆进制在⽹络 编程中最常⽤。

# 14.2 实现Memcached的编解码器

当想要实现⼀个给定协议的编解码器，我们应该花⼀些事件来了解它的运作原理。通常情况 下，协议本⾝都有⼀些详细的记录。在这⾥你会发现多少细节？幸运的是Memcached的⼆进制协 议可以很好的扩展。

在RFC中有相应的规范，并提供了Memcached⼆进制协议下载地址： http://code.google.com/p/memcached/wiki/BinaryProtocolRevamped。我们不会执⾏Memcached 的所有命令，只会执⾏三种操作：SET,GET和DELETE。这样做事为了让事情变得简单。

# 14.3 了解Memcached⼆进制协议

可以在http://code.google.com/p/memcached/wiki/BinaryProtocolRevamped上详细了解 Memcached⼆进制协议结构。不过这个⽹站如果不翻墙的话好像访问不了。

## 14.4 Netty编码器和解码器

#### 14.4.1 实现Memcached编码器

###### 先定义memcached操作码(Opcode)和响应状态码(Status)：

[java] view plaincopy

![image 112](assets/imageFile112.png)

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


/*

- * memcached operation codes

- * @author c.king

*

- */ publicclas Opcode {


publicstaticfinalbyte GET =0x0; publicstaticfinalbyte SET =0x01; publicstaticfinalbyte DELETE =0x04;

}

[java] view plaincopy

![image 113](assets/imageFile113.png)

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


package nety.in.action.mem;

/*

- * memcached response statuses

- * @author c.king

*

- */ publicclas Status {


publicstaticfinalshort NO_EROR =0x 0; publicstaticfinalshort KEY_NOT_FOUND =0x 01; publicstaticfinalshort KEY_EXISTS =0x 02; publicstaticfinalshort VALUE_TO_LARGE =0x 03; publicstaticfinalshort INVALID_ARGUMENTS =0x 04; publicstaticfinalshort ITEM_NOT_STORED =0x 05; publicstaticfinalshort INC_DEC_NON_NUM_VAL =0x 06;

}

继续编写memcached请求消息体：

[java] view plaincopy

![image 114](assets/imageFile114.png)

import java.util.Random;

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
- 37.
- 38.
- 39.
- 40.
- 41.


/*

- * memcached request mesage object

- * @author c.king

*

- */ publicclas MemcachedRequest {


privatestaticfinal Random rand =new Random(); privateint magic =0x80;/ fixed so hard coded privatebyte opCode; / the operation e.g. set or get private String key; / the key to delete, get or set privateint flags =0xdeadbef; / random privateint expires; / 0 = item never expires private String body; / if opCode is set, the value privateint id = rand.nextInt(); / Opaque privatelong cas; / data version check.not used privatebolean hasExtras; / not al ops have extras

public MemcachedRequest(byte opcode, String key, String value) { this.opCode = opcode; this.key = key; this.body = value =nul ?" : value;

/ only set co mand has extras in our example hasExtras = opcode = Opcode.SET;

}

public MemcachedRequest(byte opCode, String key) {

this(opCode, key,nul); }

publicint getMagic() {

return magic; }

publicbyte getOpCode() {

return opCode; }

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


public String getKey() {

return key; }

publicint getFlags() {

return flags; }

publicint getExpires() {

return expires; }

public String getBody() {

return body; }

publicint getId() {

return id; }

publiclong getCas() {

return cas; }

publicbolean isHasExtras() {

return hasExtras; }

}

最后编写memcached请求编码器：

[java] view plaincopy

![image 115](assets/imageFile115.png)

import io.nety.bufer.ByteBuf; import io.nety.chanel.ChanelHandlerContext; import io.nety.handler.codec.MesageToByteEncoder; import io.nety.util.CharsetUtil;

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
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.


/*

- * memcached request encoder

- * @author c.king

*

- */ publicclas MemcachedRequestEncoderextends MesageToByteEncoder<MemcachedRequest> {


@Override

protectedvoid encode(ChanelHandlerContext ctx, MemcachedRequest msg, ByteBuf out) throws Exception {

/ convert key and body to bytes array byte[] key = msg.getKey().getBytes(CharsetUtil.UTF_8); byte[] body = msg.getBody().getBytes(CharsetUtil.UTF_8);

/ total size of body = key size + body size + extras size

int bodySize = key.length + body.length + (msg.isHasExtras() ?8 :0); / write magic int

out.writeInt(msg.getMagic(); / write opcode byte out.writeByte(msg.getOpCode();

/ write key length (2 byte) i.e a Java short out.writeShort(key.length);

/ write extras length (1 byte)

int extraSize = msg.isHasExtras() ?0x08 :0x0;

out.writeByte(extraSize); / byte is the data type, not currently implemented in Memcached / but required

out.writeByte(0); / next two bytes are reserved, not currently implemented / but are required

out.writeShort(0); / write total body length ( 4 bytes - 32 bit int)

out.writeInt(bodySize); / write opaque ( 4 bytes) - a 32 bit int that is returned / in the response

out.writeInt(msg.getId(); / write CAS ( 8 bytes) / 24 byte header finishes with the CAS

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


out.writeLong(msg.getCas();

if(msg.isHasExtras(){ / write extras / (flags and expiry, 4 bytes each), 8 bytes total

out.writeInt(msg.getFlags(); out.writeInt(msg.getExpires();

} /write key out.writeBytes(key); /write value

out.writeBytes(body); }

}

#### 14.4.2 实现Memcached解码器

编写memcached响应消息体：

[java] view plaincopy

![image 116](assets/imageFile116.png)

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
- 37.
- 38.
- 39.


/*

- * memcached response mesage object

- * @author c.king

- * */


publicclas MemcachedResponse {

privatebyte magic; privatebyte opCode; privatebyte dataType; privateshort status; privateint id; privatelong cas; privateint flags; privateint expires; private String key; private String data;

public MemcachedResponse(byte magic,byte opCode,byte dataType,short status,

int id,long cas,int flags,int expires, String key, String data) { this.magic = magic; this.opCode = opCode; this.dataType = dataType; this.status = status; this.id = id; this.cas = cas; this.flags = flags; this.expires = expires; this.key = key; this.data = data;

}

publicbyte getMagic() {

return magic; }

publicbyte getOpCode() {

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
- 72.
- 73.
- 74.
- 75.


return opCode; }

publicbyte getDataType() {

return dataType; }

publicshort getStatus() {

return status; }

publicint getId() {

return id; }

publiclong getCas() {

return cas; }

publicint getFlags() {

return flags; }

publicint getExpires() {

return expires; }

public String getKey() {

return key; }

public String getData() {

return data; }

}

编写memcached响应解码器：

[java] view plaincopy

![image 117](assets/imageFile117.png)

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
- 37.
- 38.
- 39.
- 40.
- 41.


import io.nety.bufer.ByteBuf; import io.nety.chanel.ChanelHandlerContext; import io.nety.handler.codec.ByteToMesageDecoder; import io.nety.util.CharsetUtil;

import java.util.List;

publicclas MemcachedResponseDecoderextends ByteToMesageDecoder {

privateenum State {

Header, Body }

private State state = State.Header; privateint totalBodySize; privatebyte magic; privatebyte opCode; privateshort keyLength; privatebyte extraLength; privatebyte dataType; privateshort status; privateint id; privatelong cas;

@Override

protectedvoid decode(ChanelHandlerContext ctx, ByteBuf in, List<Object> out)

throws Exception { switch (state) { case Header:

/ response header is 24 bytes if (in.readableBytes() <24) {

return; }

/ read header magic = in.readByte(); opCode = in.readByte(); keyLength = in.readShort(); extraLength = in.readByte(); dataType = in.readByte();

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
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.


status = in.readShort(); totalBodySize = in.readInt(); id = in.readInt(); cas = in.readLong(); state = State.Body;

break; case Body:

if (in.readableBytes() < totalBodySize) {

return; }

int flags =0; int expires =0; int actualBodySize = totalBodySize; if (extraLength >0) {

flags = in.readInt(); actualBodySize -=4;

}

if (extraLength >4) {

expires = in.readInt(); actualBodySize -=4;

} String key =";

if (keyLength >0) { ByteBuf keyBytes = in.readBytes(keyLength); key = keyBytes.toString(CharsetUtil.UTF_8); actualBodySize -= keyLength;

} ByteBuf body = in.readBytes(actualBodySize); String data = body.toString(CharsetUtil.UTF_8);

out.ad(new MemcachedResponse(magic, opCode, dataType, status,

id, cas, flags, expires, key, data); state = State.Header;

break; default:

break; }

}

}

## 14.5 测试编解码器

基于netty的编解码器都写完了，下⾯我们来写⼀个测试它的类：

###### [java] view plaincopy

![image 118](assets/imageFile118.png)

import io.nety.chanel.embeded.EmbededChanel; import io.nety.util.CharsetUtil;

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
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.


import org.junit.Asert; import org.junit.Test;

/*

- * test memcached encoder

- * @author c.king

*

- */ publicclas MemcachedRequestEncoderTest {


@Test

publicvoid testMemcachedRequestEncoder() { MemcachedRequest request =new MemcachedRequest(Opcode.SET,"k1","v1"); EmbededChanel chanel =new EmbededChanel(

new MemcachedRequestEncoder(); Asert.asertTrue(chanel.writeOutbound(request); ByteBuf encoded = (ByteBuf) chanel.readOutbound(); Asert.asertNotNul(encoded); Asert.asertEquals(request.getMagic(), encoded.readInt(); Asert.asertEquals(request.getOpCode(), encoded.readByte(); Asert.asertEquals(2, encoded.readShort();

Asert.asertEquals(byte)0x08, encoded.readByte(); Asert.asertEquals(byte)0, encoded.readByte();

Asert.asertEquals(0, encoded.readShort(); Asert.asertEquals(2 +2 +8, encoded.readInt(); Asert.asertEquals(request.getId(), encoded.readInt(); Asert.asertEquals(request.getCas(), encoded.readLong(); Asert.asertEquals(request.getFlags(), encoded.readInt(); Asert.asertEquals(request.getExpires(), encoded.readInt();

byte[] data =newbyte[encoded.readableBytes()]; encoded.readBytes(data); Asert.asertArrayEquals(request.getKey() + request.getBody()

.getBytes(CharsetUtil.UTF_8), data); Asert.asertFalse(encoded.isReadable(); Asert.asertFalse(chanel.finish(); Asert.asertNul(chanel.readInbound();

}

45.

}

[java] view plaincopy

![image 119](assets/imageFile119.png)

import io.nety.bufer.Unpoled; import io.nety.chanel.embeded.EmbededChanel; import io.nety.util.CharsetUtil;

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
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.


import org.junit.Asert; import org.junit.Test;

/*

- * test memcached decoder

*

- * @author c.king

*

- */ publicclas MemcachedResponseDecoderTest {


@Test

publicvoid testMemcachedResponseDecoder() { EmbededChanel chanel =new EmbededChanel(

new MemcachedResponseDecoder(); byte magic =1; byte opCode = Opcode.SET; byte dataType =0; byte[] key ="Key1".getBytes(CharsetUtil.UTF_8); byte[] body ="Value".getBytes(CharsetUtil.UTF_8); int id = (int) System.currentTimeMilis(); long cas = System.currentTimeMilis();

ByteBuf bufer = Unpoled.bufer(); bufer.writeByte(magic); bufer.writeByte(opCode); bufer.writeShort(key.length); bufer.writeByte(0); bufer.writeByte(dataType); bufer.writeShort(Status.KEY_EXISTS); bufer.writeInt(body.length + key.length); bufer.writeInt(id); bufer.writeLong(cas); bufer.writeBytes(key); bufer.writeBytes(body); Asert.asertTrue(chanel.writeInbound(bufer);

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


MemcachedResponse response = (MemcachedResponse) chanel.readInbound(); asertResponse(response, magic, opCode, dataType, Status.KEY_EXISTS,0,

0, id, cas, key, body); }

privatestaticvoid asertResponse(MemcachedResponse response,byte magic, byte opCode,byte dataType,short status,int expires,int flags, int id,long cas,byte[] key,byte[] body) {

Asert.asertEquals(magic, response.getMagic(); Asert.asertArrayEquals(key,

response.getKey().getBytes(CharsetUtil.UTF_8); Asert.asertEquals(opCode, response.getOpCode(); Asert.asertEquals(dataType, response.getDataType(); Asert.asertEquals(status, response.getStatus(); Asert.asertEquals(cas, response.getCas(); Asert.asertEquals(expires, response.getExpires(); Asert.asertEquals(flags, response.getFlags(); Asert.asertArrayEquals(body,

response.getData().getBytes(CharsetUtil.UTF_8); Asert.asertEquals(id, response.getId();

}

}

# 14.6 Summary

本章主要是使⽤netty写了个模拟memcached⼆进制协议的处理。⾄于memcached⼆进制协 议具体是个啥玩意，可以单独了解，这⾥也没有详细说明。

### 第⼗五章：选择正确的线程模型

本章介绍

线程模型(thread-model) 事件循环(EventLop) 并发(Concurency) 任务执⾏(task execution) 任务调度(task scheduling)

线程模型定义了应⽤程序或框架如何执⾏你的代码，选择应⽤程序/框架的正确的线程模型是很重 要的。Nety提供了⼀个简单强⼤的线程模型来帮助我们简化代码，Nety对所有的核⼼代码都进⾏了 同步。所有ChanelHandler，包括业务逻辑，都保证由⼀个线程同时执⾏特定的通道。这并不意味着 Nety不能使⽤多线程，只是Nety限制每个连接都由⼀个线程处理，这种设计适⽤于⾮阻塞程序。我 们没有必要去考虑多线程中的任何问题，也不⽤担⼼会抛ConcurentModificationException或其他⼀ 些问题，如数据冗余、加锁等，这些问题在使⽤其他框架进⾏开发时是经常会发⽣的。

读完本章就会深刻理解Nety的线程模型以及Nety团队为什么会选择这样的线程模型，这些 信息可以让我们在使⽤Nety时让程序由最好的性能。此外，Nety提供的线程模型还可以让我们 编写整洁简单的代码，以保持代码的整洁性；我们还会学习Nety团队的经验，过去使⽤其他的线 程模型，现在我们将使⽤Nety提供的更容易更强⼤的线程模型来开发。

尽管本章讲述的是Nety的线程模型，但是我们仍然可以使⽤其他的线程模型；⾄于如何选择 ⼀个完美的线程模型应该根据应⽤程序的实际需求来判断。

本章假设如下：

你明⽩线程是什么以及如何使⽤，并有使⽤线程的⼯作经验；若不是这样，就请花些时间来了 解清楚这些知识。推荐⼀本书：Java并发编程实战。 你了解多线程应⽤程序及其设计，也包括如何保证线程安全和获取最佳性能。 你了解java.util.concurent以及ExecutorService和ScheduledExecutorService。

## 15.1 线程模型概述

本节将简单介绍⼀般的线程模型，Nety中如何使⽤指定的线程模型，以及Nety不同的版本 中使⽤的线程模型。你会更好的理解不同的线程模型的所有利弊。

如果思考⼀下，在我们的⽣活中会发现很多情况都会使⽤线程模型。例如，你有⼀个餐厅， 向你的客户提供⻝品，⻝物需要在厨房煮熟后才能给客户；某个客户下了订单后，你需要将煮熟 事物这个任务发送到厨房，⽽厨房可以以不同的⽅式来处理，这就像⼀个线程模型，定义了如何 执⾏任务。

只有⼀个厨师：

这种⽅法是单线程的，⼀次只执⾏⼀个任务，完成当前订单后再处理下⼀个。 你有多个厨师，每个厨师都可以做，空闲的厨师准备着接单做饭：

这种⽅式是多线程的，任务由多个线程(厨师)执⾏，可以并⾏同时执⾏。

你有多个厨师并分成组，⼀组做晚餐，⼀个做其他： 这种情况也是多线程，但是带有额外的限制；同时执⾏多个任务是由实际执⾏的任务类型 (晚餐或其他)决定。

从上⾯的例⼦看出，⽇常活动适合在⼀个线程模型。但是Nety在这⾥适⽤吗？不幸的是，它 没有那么简单，Nety的核⼼是多线程，但隐藏了来⾃⽤户的⼤部分。Nety使⽤多个线程来完成 所有的⼯作，只有⼀个线程模型线型暴露给⽤户。⼤多数现代应⽤程序使⽤多个线程调度⼯作， 让应⽤程序充分使⽤系统的资源来有效⼯作。在早期的Java中，这样做是通过按需创建新线程并 ⾏⼯作。但很快发现者不是完美的⽅案，因为创建和回收线程需要较⼤的开销。在Java5中加⼊了 线程池，创建线程和重⽤线程交给⼀个任务执⾏，这样使创建和回收线程的开销降到最低。

下图显示使⽤⼀个线程池执⾏⼀个任务，提交⼀个任务后会使⽤线程池中空闲的线程来执 ⾏，完成任务后释放线程并将线程重新放回线程池：

![image 120](assets/imageFile120.png)

上图每个任务线程的创建和回收不需要新线程去创建和销毁，但这只是⼀半的问题，我们稍 后学习。你可能会问为什么不使⽤多线程，使⽤⼀个ExecutorService可以有助于防⽌线程创建和 回收的成本？

使⽤多线程会有太多的上下⽂切换，提⾼了资源和管理成本，这种副作⽤会随着运⾏线程的 数量和执⾏的任务数量的增加⽽愈加明显。使⽤多线程在刚开始可能没有什么问题，但随着系统 的负载增加，可能在某个点就会让系统崩溃。

除了这些技术上的限制和问题，在项⽬⽣命周期内维护应⽤程序/框架可能还会发⽣其他问

题。它有效的说明了增加应⽤程序的复杂性取决于它是平⾏的，简单的陈述：编写多线程应⽤程 序时⼀个⾟苦的⼯作！我们怎么来解决这个问题呢？在实际的场景中需要多个线程模型。让我们 来看看Nety是如何解决这个问题的。

## 15.2 事件循环

事件循环所做的正如它的名字，它运⾏的事件在⼀个循环中，直到循环终⽌。这⾮常适合⽹ 络框架的设计，因为它们需要为⼀个特定的连接运⾏⼀个事件循环。这不是Nety的新发明，其他 的框架和实现已经很早就这样做了。

在Nety中使⽤EventLop接⼝代表事件循环，EventLop是从EventExecutor和 ScheduledExecutorService扩展⽽来，所以可以讲任务直接交给EventLop执⾏。类关系图如 下：

![image 121](assets/imageFile121.png)

##### 15.2.1 使⽤事件循环

下⾯代码显示如何访问已分配给通道的EventLop并在EventLop中执⾏任务：

[java] view plaincopy

![image 122](assets/imageFile122.png)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


Chanel ch = .;

ch.eventLop().execute(new Runable() {

@Override

publicvoid run() {

System.out.println("run in the eventl op"); }

});

使⽤事件循环的好处是不需要担⼼同步问题，在同⼀线程中执⾏所有其他关联通道的其他事 件。这完全符合Nety的线程模型。检查任务是否已执⾏，使⽤返回的Future，使⽤Future可以访 问很多不同的操作。下⾯的代码是检查任务是否执⾏：

[java] view plaincopy

![image 123](assets/imageFile123.png)

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


Chanel ch = .;

Future<?> future = ch.eventLop().submit(new Runable() {

@Override

publicvoid run() {

} });

if(future.isDone(){

System.out.println("task complete"); }else {

System.out.println("task not complete"); }

检查执⾏任务是否在事件循环中:

[java] view plaincopy

![image 124](assets/imageFile124.png)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


Chanel ch = .;

if(ch.eventLop().inEventLop(){

System.out.println("in the EventLop"); }else {

System.out.println("outside the EventLop"); }

只有确认没有其他EventLop使⽤线程池了才能关闭线程池，否则可能会产⽣未定义的副作 ⽤。

#### 15.2.2 Netty4中的I/O操作

这个实现很强⼤，甚⾄Nety使⽤它来处理底层I/O事件，在socket上触发读和写操作。这些读 和写操作是⽹络API的⼀部分，通过java和底层操作系统提供。下图显示在EventLop上下⽂中执 ⾏⼊站和出站操作，如果执⾏线程绑定到EventLop，操作会直接执⾏；如果不是，该线程将排队 执⾏：

![image 125](assets/imageFile125.png)

需要⼀次处理⼀个事件取决于事件的性质，通常从⽹络堆栈读取或传输数据到你的应⽤程 序，有时在另外的⽅向做同样的事情，例如从你的应⽤程序传输数据到⽹络堆栈再发送到远程对 等通道，但不限于这种类型的事物；更重要的是使⽤的逻辑是通⽤的，灵活处理各种各样的案 例。

应该指出的是，线程模型(事件循环的顶部)描述并不总是由Nety使⽤。我们在了解Nety3后 会更容易理解为什么新的线程模型是可取的。

#### 15.2.3 Netty3中的I/O操作

在以前的版本有点不同，Nety保证在I/O线程中只有⼊站事件才被执⾏，所有的出站时间被调 ⽤线程处理。这看起来是个好⽅案，但很容易出错。它还将负责同步ChanelHandler来处理这些 事件，因为它不保证只有⼀个线程同时操作；这可能发⽣在你去掉通道下游事件的同时，例如， 在不同的线程调⽤Chanel.write(.)。下图显示Nety3的执⾏流程：

![image 126](assets/imageFile126.png)

除了需要负担同步ChanelHandler，这个线程模型的另⼀个问题是你可能需要去掉⼀个⼊站 事件作为⼀个出站事件的结果，例如Chanel.write(.)操作导致异常。在这种情况下，捕获的异常 必须⽣成并抛出去。乍看之下这不像是⼀个问题，但我们知道，捕获异常由⼊站事件涉及，会让 你知道问题出在哪⾥。问题是，事实上，你现在的情况是在调⽤线程上执⾏，但捕获到异常事件 必须交给⼯作线程来执⾏。这是可⾏的，但如果你忘了传递过去，它会导致线程模型失效；假设 ⼊站事件只有⼀个线程不是真，这可能会给你各种各样的竞争条件。

以前的实现有⼀个唯⼀的积极影响，在某些情况下它可以提供更好的延迟；成本是值得的， 因为它消除了复杂性。实际上，在⼤多数应⽤程序中，你不会遵守任何差异延迟，还取决于其他 因数，如：

字节写⼊到远程对等通道有多快 I/O线程是否繁忙 上下⽂切换 锁定

你可以看到很多细节影响整体延迟。

##### 15.2.4 Netty线程模型内部

Nety的内部实现使其线程模型表现优异，它会检查正在执⾏的线程是否是已分配给实际通道 (和EventLop)，在Chanel的⽣命周期内，EventLop负责处理所有的事件。如果线程是相同的 EventLop中的⼀个，讨论的代码块被执⾏；如果线程不同，它安排⼀个任务并在⼀个内部队列后 执⾏。通常是通过EventLop的Chanel只执⾏⼀次下⼀个事件，这允许直接从任何线程与通道交 互，同时还确保所有的ChanelHandler是线程安全，不需要担⼼并发访问问题。

下图显示在EventLop中调度任务执⾏逻辑，这适合Nety的线程模型：

![image 127](assets/imageFile127.png)

设计是⾮常重要的，以确保不要把任何⻓时间运⾏的任务放在执⾏队列中，因为⻓时间运⾏ 的任务会阻⽌其他在相同线程上执⾏的任务。这多少会影响整个系统依赖于EventLop实现⽤于特 殊传输的实现。传输之间的切换在你的代码库中可能没有任何改变，重要的是：切勿阻塞I/O线 程。如果你必须做阻塞调⽤(或执⾏需要⻓时间才能完成的任务)，使⽤EventExecutor。

下⼀节将讲解⼀个在应⽤程序中经常使⽤的功能，就是调度执⾏任务(定期执⾏)。Java对这 个需求提供了解决⽅案，但Nety提供了⼏个更好的⽅案。

## 15.3 调度任务执⾏

每隔⼀段时间需要调度任务执⾏，也许你想注册⼀个任务在客户端完成连接5分钟后执⾏，⼀ 个常⻅的⽤例是发送⼀个消息“你还活着？”到远程对等通道，如果远程对等通道没有反应，则可 以关闭通道(连接)和释放资源。就像你和朋友打电话，沉默了⼀段时间后，你会说“你还在吗？”， 如果朋友没有回复，就可能是断线或朋友睡着了；不管是什么问题，你都可以挂断电话，没有什 么可等待的；你挂了电话后，收起电话可以做其他的事。

本节介绍使⽤强⼤的EventLop实现任务调度，还会简单介绍Java API的任务调度，以⽅便和 Nety⽐较加深理解。

- 15.3.1 使⽤普通的Java API调度任务


在Java中使⽤JDK提供的ScheduledExecutorService实现任务调度。使⽤Executors提供的静 态⽅法创建ScheduledExecutorService，有如下⽅法：

newScheduledThreadPol(int) newScheduledThreadPol(int, ThreadFactory) newSingleThreadScheduledExecutor() newSingleThreadScheduledExecutor(ThreadFactory)

看下⾯代码：

[java] view plaincopy

![image 128](assets/imageFile128.png)

ScheduledExecutorService executor = Executors.newScheduledThreadPol(10);

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


ScheduledFuture<?> future = executor.schedule(new Runable() {

@Override

publicvoid run() {

System.out.println("now it is 60 seconds later"); }

},60, TimeUnit.SECONDS);

if(future.isDone(){

System.out.println("scheduled completed"); }

/ . executor.shutdown();

#### 15.3.2 使⽤EventLoop调度任务

使⽤ScheduledExecutorService⼯作的很好，但是有局限性，⽐如在⼀个额外的线程中执⾏ 任务。如果需要执⾏很多任务，资源使⽤就会很严重；对于像Nety这样的⾼性能的⽹络框架来 说，严重的资源使⽤是不能接受的。Nety对这个问题提供了很好的⽅法。

Nety允许使⽤EventLop调度任务分配到通道，如下⾯代码：

[java] view plaincopy

![image 129](assets/imageFile129.png)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


Chanel ch = .;

ch.eventLop().schedule(new Runable() {

@Override

publicvoid run() {

System.out.println("now it is 60 seconds later"); }

},60, TimeUnit.SECONDS);

如果想任务每隔多少秒执⾏⼀次，看下⾯代码：

[java] view plaincopy

![image 130](assets/imageFile130.png)

Chanel ch = .;

- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


ScheduledFuture<?> future = ch.eventLop().scheduleAtFixedRate(new Runable() {

@Override

publicvoid run() {

System.out.println("after run 60 seconds,and run every 60 seconds"); }

},60,60, TimeUnit.SECONDS); / cancel the task

future.cancel(false);

##### 15.3.3 调度的内部实现

Nety内部实现其实是基于George Varghese提出的“Hashed and hierarchical timing whels: Data structures to eficiently implement timer facility(散列和分层定时轮：数据结构有 效实现定时器)”。这种实现只保证⼀个近似执⾏，也就是说任务的执⾏可能不是10%准确；在实 践中，这已经被证明是⼀个可容忍的限制，不影响多数应⽤程序。所以，定时执⾏任务不可能 10%准确的按时执⾏。

为了更好的理解它是如何⼯作，我们可以这样认为：

- 1.
- 2.
- 3.
- 4.
- 5.


在指定的延迟时间后调度任务； 任务被插⼊到EventLop的Schedule-Task-Queue(调度任务队列)； 如果任务需要⻢上执⾏，EventLop检查每个运⾏； 如果有⼀个任务要执⾏，EventLop将⽴刻执⾏它，并从队列中删除； EventLop等待下⼀次运⾏，从第4步开始⼀遍⼜⼀遍的重复。

因为这样的实现计划执⾏不可能10%正确，对于多数⽤例不可能10%准备的执⾏计划任 务；在Nety中，这样的⼯作⼏乎没有资源开销。但是如果需要更准确的执⾏呢？很容易，你需要 使⽤ScheduledExecutorService的另⼀个实现，这不是Nety的内容。记住，如果不遵循Nety的 线程模型协议，你将需要⾃⼰同步并发访问。

## 15.4 I/O线程分配细节

Nety使⽤线程池来为Chanel的I/O和事件服务，不同的传输实现使⽤不同的线程分配⽅式； 异步实现是只有⼏个线程给通道之间共享，这样可以使⽤最⼩的线程数为很多的平道服务，不需 要为每个通道都分配⼀个专⻔的线程。

下图显示如何分配线程池：

![image 131](assets/imageFile131.png)

如上图所示，使⽤⼀个固定⼤⼩的线程池管理三个线程，创建线程池后就把线程分配给线程 池，确保在需要的时候，线程池中有可⽤的线程。这三个线程会分配给每个新创建的已连接通 道，这是通过EventLopGroup实现的，使⽤线程池来管理资源；实际会平均分配通道到所有的线 程上，这种分布以循环的⽅式完成，因此它可能不会10%准确，但⼤部分时间是准确的。

⼀个通道分配到⼀个线程后，在这个通道的⽣命周期内都会⼀直使⽤这个线程。这⼀点在以 后的版本中可能会被改变，所以我们不应该依赖这种⽅式；不会被改变的是⼀个线程在同⼀时间 只会处理⼀个通道的I/O操作，我们可以依赖这种⽅式，因为这种⽅式可以确保不需要担⼼同步。

下图显示OIO(Old Blocking I/O)传输：

![image 132](assets/imageFile132.png)

从上图可以看出，每个通道都有⼀个单独的线程。我们可以使⽤java.io.*包⾥的类来开发基于 阻塞I/O的应⽤程序，即使语义改变了，但有⼀件事仍然保持不变，每个通道的I/O在同时只能被⼀ 个线程处理；这个线程是由Chanel的EventLop提供，我们可以依靠这个硬性的规则，这也是 Nety框架⽐其他⽹络框架更容易编写的原因。

# 15.5 Summary

本章主要讲解Nety的线程模型，其核⼼接⼝是EventLop；并和OIO中的线程模型做了⽐较，以 突显Nety的优异性。

第⼗六章：从EventLop取消注册和重新注册

本章介绍

EventLoop 从EventLoop注册和取消注册 在Netty中使⽤旧的Socket和Channel

Netty提供了⼀个简单的⽅法来连接Socket/Channel，这是在Netty之外创建并转移他们的责任到 Netty。这允许你将遗留的集成框架以⽆缝⽅式⼀步⼀步迁移到Netty；Netty还允许取消注册的通道来 停⽌处理IO，这可以暂停程序处理并释放资源。

这些功能在某些情况或某种程度上可能不是⾮常有⽤，但使⽤这些特性可以解决⼀些困难的 问题。举个例⼦，有⼀个⾮常受欢迎的社交⽹络，其⽤户增⻓⾮常快，系统程序需要处理每秒⼏ 千个交互或消息，如果⽤户持续增⻓，系统将会处理每秒数以万计的交互；这很令⼈兴奋，但随 着⽤户数的增⻓，系统将消耗⼤量的内存和CPU⽽导致性能低下；此时最需要做的就是改进他 们，并且不要花太多的钱在硬件设备上。这种情况下，系统必须保持功能正常能处理⽇益增⻓的 数据量，此时，注册/注销事件循环就派上⽤场了。

通过允许外部Socket/Channel来注册和注销，Netty能够以这样的⽅式改进旧系统的缺陷，所 有的Netty程序都可以通过⼀种有效精巧的⽅式整合到现有系统，本章将重点讲解Netty是如何整 合。

# 16.1 注册和取消注册的Channel和Socket

前⾯章节讲过，每个通道需要注册到⼀个EventLoop来处理IO或事件，这是在引导过程中⾃ 动完成。下图显⽰了他们的关系：

![image 133](assets/imageFile133.png)

上图只是显⽰了他们关系的⼀部分，通道关闭时，还需要将注册到EventLoop中的 Socket/Channel注销以释放资源。

有时不得不处理java.nio.channels.SocketChannel或其他java.nio.channes.Channel实现，这 可能是遗留程序或框架的⼀些原因所致。我们可以使⽤Netty来包装预先创建的 java.nio.channels.Channel，然后再注册到EventLoop。我们可以使⽤Netty的所有特性，同时还能 重⽤现有的东西。下⾯代码显⽰了此功能：

[java] view plaincopy

![image 134](assets/imageFile134.png)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


/nio

java.nio.chanels.SocketChanel mySocket = java.nio.chanels.SocketChanel.open();

/nety SocketChanel ch =new NioSocketChanel(mySocket); EventLopGroup group =new NioEventLopGroup();

/register chanel

ChanelFuture registerFuture = group.register(ch);

/de-register chanel

ChanelFuture deregisterFuture = ch.deregister();

Netty也适⽤于包装OIO，看下⾯代码：

[java] view plaincopy

![image 135](assets/imageFile135.png)

/oio Socket mySocket =new Socket(" w.baidu.com",80);

- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


/nety SocketChanel ch =new OioSocketChanel(mySocket); EventLopGroup group =new OioEventLopGroup();

/register chanel

ChanelFuture registerFuture = group.register(ch);

/de-register chanel

ChanelFuture deregisterFuture = ch.deregister();

只有2个重点如下：

- 1.
- 2.


使⽤Netty包装已创建的Socket或Channel必须使⽤与之对应的实现，如Socket是OIO，则使 ⽤Netty的OioSocketChannel；SocketChannel是NIO，则使⽤NioSocketChannel。 EventLoop.register(...)和Channel.deregister(...)都是⾮阻塞异步的，也就是说它们可能不会理

解执⾏完成，可能稍后完成。它们返回ChannelFuture，我们在需要进⼀步操作或确认完成操 作时可以添加⼀个ChannelFutureLister或在ChannelFuture上同步等待⾄完成；选择哪⼀种⽅ 式看实际需求，⼀般建议使⽤ChannelFutureLister，应避免阻塞。

## 16.2 挂起IO处理

在⼀些情况下可能需要停⽌⼀个指定通道的处理操作，⽐如程序耗尽内存、崩溃、失去⼀些 消息，此时，我们可以停⽌处理事件的通道来清理系统资源，以保持程序稳定继续处理后续消 息。若这样做，最好的⽅式就是从EventLoop取消注册的通道，这可以有效阻⽌通道再处理任何事 件。若需要被取消的通道再次处理事件，则只需要将该通道重新注册到EventLooop即可。看下 图：

![image 136](assets/imageFile136.png)

###### 看下⾯代码：

[java] view plaincopy

![image 137](assets/imageFile137.png)

Botstrap botstrap =new Botstrap(); botstrap.group(group).chanel(NioSocketChanel.clas)

3. 4.

.handler(new SimpleChanelInboundHandler<ByteBuf>() {

5.

@Override

6. 7. 8. 9.

protectedvoid chanelRead0(ChanelHandlerContext ctx, ByteBuf msg)throws Exception {

/remove this ChanelHandler and de-register ctx.pipeline().remove(this);

10. 11. 12. 13.

ctx.deregister(); }

}); ChanelFuture future = botstrap.conect(

14. 15.

new InetSocketAdres(" w.baidu.com",80).sync(); / .

- 16.
- 17. 18.


Chanel chanel = future.chanel();

/re-register chanel and ad ChanelFutureLister group.register(chanel).adListener(new ChanelFutureListener() {

- 19.
- 20. 21.


@Override

publicvoid operationComplete(ChanelFuture future)throws Exception { if(future.isSuces(){

- 22.
- 23.
- 24. 25. 26. 27. 28.


System.out.println("Chanel registered");

}else{ System.out.println("register chanel on EventLop fail"); future.cause().printStackTrace();

} }

});

## 16.3 迁移通道到另⼀个事件循环

另⼀个取消注册和注册⼀个Channel的⽤例是将⼀个活跃的Channel移到另⼀个EventLoop， 有下⾯⼀些原因可能导致需要这么做：

当前EventLoop太忙碌，需要将Channel移到⼀个不是很忙碌的EventLoop； 终⽌EventLoop释放资源同时保持活跃Channel可以继续使⽤； 迁移Channel到⼀个执⾏级别较低的⾮关键业务的EventLoop中。

下图显⽰迁移Channel到另⼀个EventLoop：

![image 138](assets/imageFile138.png)

###### 看下⾯代码：

[java] view plaincopy

![image 139](assets/imageFile139.png)

final EventLopGroup group2 =new NioEventLopGroup(); Botstrap b =new Botstrap(); b.group(group).chanel(NioSocketChanel.clas)

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


.handler(new SimpleChanelInboundHandler<ByteBuf>() {

@Override

protectedvoid chanelRead0(ChanelHandlerContext ctx, ByteBuf msg)throws Exception {

/ remove this chanel handler and de-register ctx.pipeline().remove(this);

ChanelFuture f = ctx.deregister();

/ ad ChanelFutureListener f.adListener(new ChanelFutureListener() {

@Override

publicvoid operationComplete(ChanelFuture future) throws Exception { / migrate this handler register to group2

group2.register(future.chanel(); }

}); }

}); ChanelFuture future = b.conect(" w.baidu.com",80);

future.adListener(new ChanelFutureListener() {

@Override

publicvoid operationComplete(ChanelFuture future)

throws Exception { if (future.isSuces() {

System.out.println("conection established");

}else { System.out.println("conection atempt failed"); future.cause().printStackTrace();

} }

});

# 16.4 Summary

⾄此，netty in action中⽂版系列博⽂已完成了，⼀次不经意的baidu，发现在51cto上都出现本系 列博客的pdf⽂件了，下载下来⼀看发现和本系列内容⼀模⼀样，呵呵，看来netty中⽂资料的需求 还是有⼀些的。
