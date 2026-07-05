---
title: Netty介绍.note（原文插图 annex）
slug: annex-Netty介绍
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/netty/Netty介绍.note.md
related: [netty-pipeline与编解码]
created: 2026-07-05
updated: 2026-07-05
---

- 1、Netty是⼀个NIO client-server(客户端服务器)框架，使⽤Netty可以快速开发⽹络应⽤，例如服

务器和客户端协议。

- 2、Netty提供了⼀种新的⽅式来使开发⽹络应⽤程序，这种新的⽅式使得它很容易使⽤和有很强

的扩展性。

- 3、Netty的内部实现时很复杂的，但是Netty提供了简单易⽤的api从⽹络处理代码中解耦业务逻

辑。

- 4、Netty是完全基于NIO实现的，所以整个Netty都是异步的。


⽹络应⽤程序通常需要有较⾼的可扩展性，⽆论是Netty还是其他的基于Java NIO的框架，都 会提供可扩展性的解决⽅案。Netty中⼀个关键组成部分是它的异步特性，本章将讨论同步(阻塞) 和异步(⾮阻塞)的IO来说明为什么使⽤异步代码来解决扩展性问题以及如何使⽤异步。

## 1.1 为什么使⽤Netty？

- 1.1.1 不是所有的⽹络框架都是⼀样的

Netty的“quick and easy(⾼性能和简单易⽤)”并不意味着编写的程序的性能和可维护性会受到 影响。从Netty中实现的协议如FTP，SMTP，HTTP，WebSocket，SPDY以及各种⼆进制和基于 ⽂本的传统协议中获得的经验导致Netty的创始⼈要⾮常⼩⼼它的设计。Netty成功的提供了易于开 发，⾼性能和⾼稳定性，以及较强的扩展性。

⾼调的公司和开源项⽬有RedHat, Twitter, Infinispan, and HornetQ, Vert.x, Finagle, Akka, Apache Cassandra, Elasticsearch，以及其他⼈的使⽤有助于Netty的发展，Netty的⼀些特性也是 这些项⽬的需要所致。多年来，Netty变的更⼴为⼈知，它是Java⽹络的⾸选框架，在⼀些开源或 ⾮开源的项⽬中可以体现。并且，Netty在2011年获得Duke's Choice Award(Duke's Choice奖)。

此外，在2011年，Netty的创始⼈Trustion Lee离开RedHat后加⼊Twitter，在这⼀点上，

Netty项⽬奖会成为⼀个独⽴的项⽬组织。RedHat和Twitter都使⽤Netty，所以它毫不奇怪。在撰 写本书时RedHat和Twitter这两家公司是最⼤的贡献者。使⽤Netty的项⽬越来越多，Netty的⽤户 群体和项⽬以及Netty社区都是⾮常活跃的。

- 1.1.2 Netty的功能⾮常丰富


通过本书可以学习Netty丰富的功能。下图是Netty框架的组成

![image 1](assets/imageFile1.png)

#### Netty除了提供传输和协议，在其他各领域都有发展。Netty为开发者提供了⼀套完整的⼯ 具，看下⾯表格：

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

## 1.2 异步设计

整个Netty的API都是异步的，异步处理不是⼀个新的机制，这个机制出来已经有⼀些时间 了。对⽹络应⽤来说，IO⼀般是性能的瓶颈，使⽤异步IO可以较⼤程度上提⾼程序性能，因为异 步变的越来越重要。但是它是如何⼯作的呢？以及有哪些不同的模式可⽤呢？

异步处理提倡更有效的使⽤资源，它允许你创建⼀个任务，当有事件发⽣时将获得通知并等 待事件完成。这样就不会阻塞，不管事件完成与否都会及时返回，资源利⽤率更⾼，程序可以利 ⽤剩余的资源做⼀些其他的事情。

本节将说明⼀起⼯作或实现异步API的两个最常⽤的⽅法，并讨论这些技术之间的差异。

### 1.2.1 Callbacks(回调)

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

### 1.2.2 Futures

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

# 1.3 Java中的Blocking和non-blocking IO对⽐

本节主要讲解Java的IO和NIO的差异，这⾥不过多赘述，⽹络已有很多相关⽂章。

## 1.4 NIO的问题和Netty中是如何解决这些问题的

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

### 1.4.4 Squashing the famous epoll bug

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

# 1.5 Summary

This chapter provided an overview of Netty's features, design and benefits. I discussed the difference between blocking and non-blocking processing to give you a fundamental understanding of the reasons to use a non-blocking framework. You learned how to use the JDK API to write network code in both blocking and non-blocking modes. This

included the new non-blocking API, which comes with JDK 7. After seeing the NIO APIs in action, it was also important to understand some of the known issues that you may run into. In fact, this is why so many people use Netty: to take care of workarounds and other JVM quirks. In the next chapter, you'll learn the basics of the Netty API and programming model, and, finally, use Netty to write some useful code.
