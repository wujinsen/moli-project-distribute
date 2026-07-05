---
title: hadoop rpc.note（原文插图 annex）
slug: annex-hadoop-rpc
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hadoop/hadoop rpc.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

htp:/weixiaolu.iteye.com/blog/1504898

源码级强⼒分析hadop的RPC机制

![image 1](assets/imageFile1.png)

分析对象：

![image 2](assets/imageFile2.png)

hadop版本：hadop 0.20.203.0

必备技术点：

- 1. 动态代理（参考 ： ）
- 2. Java NIO（参考 ： ）


htp:/weixiaolu.iteye.com/blog/14 74 htp:/weixiaolu.iteye.com/blog/1479656

- 3. Java⽹络编程


⽬录： ⼀．RPC协议 ⼆．ipc.RPC源码分析 三．ipc.Client源码分析 四．ipc.Server源码分析

分析： ⼀．RPC协议

当客户端发送⼀个字节给服务端时，服务端必须也要有⼀个读字节的⽅法在阻塞等待； 反之亦然。 这种我把它称为底层的通信协议。 可是对于⼀个⼤型的⽹络通信系统来说， 很显然这种说法的协议粒度太⼩，不⽅便我们理解整个⽹络通信的流程及架构， 所以我造了个说法：架构层次的协议。 通俗⼀点说，就是我把某些接⼝和接⼝中的⽅法称为协议， 客户端和服务端只要实现这些接⼝中的⽅法就可以进⾏通信了， 从这个⻆度来说，架构层次协议的说法就可以成⽴了

（注：如果从架构层次的协议来分析系统，我们就先不要太在意⽅法的具体实现）。

架构层次协议：就是我把某些接⼝和接⼝中的⽅法称为协议，客户端和服务端只要实现这些接⼝中的 ⽅法就可以进⾏通信了。例如thrift，webservice Hadop的RPC机制正是采⽤了这种“架构层次的协议”，有⼀整套作为协议的接⼝。如图：

# 重点的⼏个协议接⼝：

（1）RPC相关

VersionedProtocol ：它是所有RPC协议接⼝的⽗接⼝，其中只有⼀个⽅法：getProtocolVersion （）

（1）HDFS相关

ClientDatanodeProtocol ：⼀个客户端和datanode之间的协议接⼝，⽤于数据块恢复 ClientProtocol ：client与Namenode交互的接⼝，所有控制流的请求均在这⾥，如：创建⽂件、删

除⽂件等； DatanodeProtocol : Datanode与Namenode交互的接⼝，如⼼跳、blockreport等； NamenodeProtocol ：SecondaryNode与Namenode交互的接⼝。

(2）Mapreduce相关 InterDatanodeProtocol ：Datanode内部交互的接⼝，⽤来更新block的元数据； I nerTrackerProtocol ：TaskTracker与JobTracker交互的接⼝，功能与DatanodeProtocol相似； JobSubmisionProtocol ：JobClient与JobTracker交互的接⼝，⽤来提交Job、获得Job等与Job

相关的操作；

TaskUmbilicalProtocol ：Task中⼦进程与⺟进程交互的接⼝，⼦进程即map、reduce等操作，⺟ 进程即TaskTracker，该接⼝可以回报⼦进程的运⾏状态（词汇扫盲: umbilical 脐带的, 关系亲密的） 。

⼆．ipc.RPC源码分析

ipc.RPC类中有⼀些内部类，为了⼤家对RPC类有个初步的印象，就先罗列⼏个我们感兴趣的分析⼀下 吧：

Invocation ：⽤于封装⽅法名和参数，作为数据传输层，相当于VO吧。

ClientCache ：⽤于存储client对象，⽤socket factory作为hash key,存储结构为hashMap <SocketFactory, Client>。

Invoker ：是动态代理中的调⽤实现类，继承了InvocationHandler.

Server ：是ipc.Server的实现类。

从以上的分析可以知道， Invocation类仅作为VO， ClientCache类只是作为缓存， Server类⽤于服务端的处理，他们都和客户端的数据流和业务逻辑没有关系。 现在就只剩下Invoker类了。

htp:/weixiaolu.iteye.com/blog/14 74

如果你对动态代理⽐较了解的话（参考： ）， 你⼀下就会想到，我们接下来去研究的就是RPC.Invoker类中的invoke()⽅法了。代码如下：

Java代码

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


public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

final bolean logDebug = LOG.isDebugEnabled(); long startTime = 0; if (logDebug) {

startTime = System.currentTimeMilis(); }

ObjectWritable value = (ObjectWritable)client.call(new Invocation(method, args), remoteId); if (logDebug) {

long calTime = System.currentTimeMilis() - startTime; LOG.debug("Cal: " + method.getName() + " " + calTime);

} return value.get();

}

如果你发现这个invoke()⽅法实现的有些奇怪的话，那你就对了。

⼀般我们看到的动态代理的invoke()⽅法中总会有 method.invoke(ac, arg); 这句代码。

⽽上⾯代码中却没有，

这是为什么呢？

其实使⽤ method.invoke(ac, arg); 是在本地JVM中调⽤；

⽽在hadop中，是将数据发送给服务端，服务端将处理的结果再返回给客户端，

所以这⾥的invoke()⽅法必然需要进⾏⽹络通信。⽽⽹络通信就是下⾯的这段代码实现的：

Java代码

1.

ObjectWritable value = (ObjectWritable)client.call(new Invocation(method, args), remoteId);

Invocation类在这⾥封装了⽅法名和参数，充当VO。

其实这⾥⽹络通信只是调⽤了Client类的cal()⽅法。

那我们接下来分析⼀下ipc.Client源码吧。

不过在分析ipc.Client源码之前，

为了不让我们像盲⽬的苍蝇⼀样乱撞，

我想先确定⼀下我们分析的⽬的是什么，

我总结出了三点需要解决的问题：

- 1. 客户端和服务端的连接是怎样建⽴的？
- 2. 客户端是怎样给服务端发送数据的？
- 3. 客户端是怎样获取服务端的返回数据的？


基于以上三个问题，我们开始吧！！！

三．ipc.Client源码分析

同样，为了对Client类有个初步的了解，我们也先罗列⼏个我们感兴趣的内部类：

Cal ：⽤于封装Invocation对象，作为VO，写到服务端，同时也⽤于存储从服务端返回的数据

Conection ：⽤以处理远程连接对象。继承了Thread ConectionId ：唯⼀确定⼀个连接

- 问题1：客户端和服务端的连接是怎样建⽴的？


下⾯我们来看看 Client类中的cal()⽅法吧：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


public Writable cal(Writable param, ConectionId remoteId)

throws InterruptedException, IOException { Cal cal = new Cal(param); /将传⼊的数据封装成cal对象 Conection conection = getConection(remoteId, cal); /获得⼀个连接 conection.sendParam(cal); / 向服务端发送cal对象 bolean interrupted = false; synchronized (cal) {

while (!cal.done) { try {

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


cal.wait(); / 等待结果的返回，在Cal类的calComplete()⽅法⾥有notify()⽅法⽤于唤醒线程 } catch (InterruptedException ie) {

/ 因中断异常⽽终⽌，设置标志interrupted为true interrupted = true;

}

} if (interrupted) {

Thread.currentThread().interrupt(); }

if (cal.error != nul) {

if (cal.error instanceof RemoteException) { cal.error.filInStackTrace(); throw cal.error;

} else { / 本地异常

throw wrapException(remoteId.getAdres(), cal.error); }

} else {

return cal.value; /返回结果数据 }

} }

具体代码的作⽤我已做了注释，所以这⾥不再赘述。

但到⽬前为⽌，你依然不知道RPC机制底层的⽹络连接是怎么建⽴的。

呵呵，那我们只好再去深究了，

分析代码后，我们会发现和⽹络通信有关的代码只会是下⾯的两句了：

- 1.
- 2.


Conection conection = getConection(remoteId, cal); /获得⼀个连接 conection.sendParam(cal); / 向服务端发送cal对象

先看看是怎么获得⼀个到服务端的连接吧，下⾯贴出ipc.Client类中的getConection()⽅法。

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


private Conection getConection(ConectionId remoteId, Cal cal) throws IOException, InterruptedException {

if (!runing.get() { / 如果client关闭了 thrownew IOException("The client is stoped");

} Conection conection;

/如果conections连接池中有对应的连接对象，就不需重新创建了；如果没有就需重新创建⼀个连接对象。 /但请注意，该 /连接对象只是存储了remoteId的信息，其实还并没有和服务端建⽴连接。

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


do {

synchronized (conections) { conection = conections.get(remoteId); if (conection = nul) {

conection = new Conection(remoteId); conections.put(remoteId, conection);

}

} } while (!conection.adCal(cal); /将cal对象放⼊对应连接中的cals池，就不贴出源码了 /这句代码才是真正的完成了和服务端建⽴连接哦~ conection.setupIOstreams(); return conection;

}

如果你还有兴趣继续分析下去，那我们就⼀探建⽴连接的过程吧,

下⾯贴出Client.Conection类中的setupIOstreams()⽅法：

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


privatesynchronizedvoid setupIOstreams() throws InterruptedException {

• try {

•

while (true) { setupConection(); /建⽴连接 InputStream inStream = NetUtils.getInputStream(socket); /获得输⼊流 OutputStream outStream = NetUtils.getOutputStream(socket); /获得输出流 writeRpcHeader(outStream);

• this.in = new DataInputStream(new BuferedInputStream

(new PingInputStream(inStream); /将输⼊流装饰成DataInputStream this.out = new DataOutputStream (new BuferedOutputStream(outStream); /将输出流装饰成DataOutputStream writeHeader(); / 跟新活动时间 touch();

/当连接建⽴时，启动接受线程等待服务端传回数据，注意：Conection继承了Tread start(); return;

}

} catch (IOException e) { markClosed(e); close();

} }

再有⼀步我们就知道客户端的连接是怎么建⽴的啦，

下⾯贴出Client.Conection类中的setupConection()⽅法：

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


privatesynchronizedvoid setupConection() throws IOException { short ioFailures = 0; short timeoutFailures = 0; while (true) {

try { this.socket = socketFactory.createSocket(); /终于看到创建socket的⽅法了 this.socket.setTcpNoDelay(tcpNoDelay);

•

/ 设置连接超时为20s NetUtils.conect(this.socket, remoteId.getAdres(), 2 0); this.socket.setSoTimeout(pingInterval); return;

} catch (SocketTimeoutException toe) { /* 设置最多连接重试为45次。

- * 总共有20s*45 = 15 分钟的重试时间。
- */ handleConectionFailure(timeoutFailures+, 45, toe);


} catch (IOException ie) {

handleConectionFailure(ioFailures+, maxRetries, ie); }

} }

终于，我们知道了客户端的连接是怎样建⽴的了，其实就是创建⼀个普通的socket进⾏通信。

那服务端是不是也是创建⼀个ServerSocket进⾏通信的呢？

呵呵，先不要急，到这⾥我们只解决了客户端的第⼀个问题，

下⾯还有两个问题没有解决呢，我们⼀个⼀个地来解决吧。

- 问题2：客户端是怎样给服务端发送数据的？


我们回顾⼀下代码四吧。第⼀句为了完成连接的建⽴，我们已经分析完毕；⽽第⼆句是为了发送数 据，

呵呵，分析下去，看能不能解决我们的问题呢。 下⾯贴出

Client.Conection类的sendParam()⽅法吧：

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


publicvoid sendParam(Cal cal) { if (shouldCloseConection.get() { return;

} DataOutputBufer d=nul; try {

synchronized (this.out) { if (LOG.isDebugEnabled() LOG.debug(getName() + " sending #" + cal.id);

/创建⼀个缓冲区 d = new DataOutputBufer(); d.writeInt(cal.id); cal.param.write(d); byte[] data = d.getData(); int dataLength = d.getLength(); out.writeInt(dataLength); /⾸先写出数据的⻓度 out.write(data, 0, dataLength); /向服务端写数据 out.flush();

} } catch(IOException e) {

markClosed(e); } finaly {

IOUtils.closeStream(d); }

}

其实这就是java io的socket发送数据的⼀般过程哦，没有什么特别之处。

到这⾥问题⼆也解决了，来看看问题三吧。

- 问题3：客户端是怎样获取服务端的返回数据的？


我们再回顾⼀下代码六吧。

代码六中，当连接建⽴时会启动⼀个线程⽤于处理服务端返回的数据，

我们看看这个处理线程是怎么实现的吧， 下⾯贴出 Client.Conection类和Client.Cal类中的相关⽅法吧：

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
- 47.


⽅法⼀： publicvoid run() {

• while (waitForWork() { receiveResponse(); /具体的处理⽅法

} close();

• }

⽅法⼆： privatevoid receiveResponse() {

if (shouldCloseConection.get() { return;

} touch(); try {

int id = in.readInt(); / 阻塞读取id if (LOG.isDebugEnabled()

LOG.debug(getName() + " got value #" + id); Cal cal = cals.get(id); /在cals池中找到发送时的那个对象

int state = in.readInt(); / 阻塞读取cal对象的状态 if (state = Status.SUCES.state) {

Writable value = ReflectionUtils.newInstance(valueClas, conf); value.readFields(in); / 读取数据

/将读取到的值赋给cal对象，同时唤醒Client等待线程，贴出setValue()代码⽅法三 cal.setValue(value); cals.remove(id); /删除已处理的cal

} elseif (state = Status.EROR.state) {

- • } elseif (state = Status.FATAL.state) {
- • }


} catch (IOException e) {

markClosed(e); }

}

⽅法三： publicsynchronizedvoid setValue(Writable value) {

this.value = value; calComplete(); /具体实现

} protectedsynchronizedvoid calComplete() {

this.done = true; notify(); / 唤醒client等待线程

}

代码九完成的功能主要是：

启动⼀个处理线程，读取从服务端传来的cal对象，将cal对象读取完毕后，唤醒client处理线程。

就这么简单，客户端就获取了服务端返回的数据了哦~。

客户端的源码分析就到这⾥了哦，下⾯我们来分析Server端的源码吧。

四．ipc.Server源码分析

同样，为了让⼤家对ipc.Server有个初步的了解，我们先分析⼀下它的⼏个内部类吧：

Cal ：⽤于存储客户端发来的请求

Listener ： 监听类，⽤于监听客户端发来的请求，

同时Listener内部还有⼀个静态类，Listener.Reader， 当监听器监听到⽤户请 求，便让Reader读取⽤户请求。 Responder ：响应RPC请求类，请求处理完毕，由Responder发送给请求客户端。 Conection ：连接类，真正的客户端请求读取逻辑在这个类中。 Handler ：请求处理类，会循环阻塞读取calQueue中的cal对象，并对其进⾏操作。

如果你看过ipc.Server的源码，

你会发现其实ipc.Server是⼀个abstract修饰的抽象类。

那随之⽽来的问题就是：

hadop是怎样初始化RPC的Server端的呢？

这个问题着实也让我想了好⻓时间。

不过后来我想到Namenode初始化时⼀定初始化了RPC的Sever端，

那我们去看看Namenode的初始化源码吧：

- 1. 初始化Server


代码⼗：

Java代码

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


privatevoid initialize(Configuration conf) throws IOException {

•

/ 创建 rpc server InetSocketAdres dnSocketAdr = getServiceRpcServerAdres(conf); if (dnSocketAdr != nul) {

int serviceHandlerCount = conf.getInt(DFSConfigKeys.DFS_NAMENODE_SERVICE_HANDLER_COUNT_KEY,

DFSConfigKeys.DFS_NAMENODE_SERVICE_HANDLER_COUNT_DEFAULT); /获得serviceRpcServer

this.serviceRpcServer = RPC.getServer(this, dnSocketAdr.getHostName(), dnSocketAdr.getPort(), serviceHandlerCount, false, conf, namesystem.getDelegationTokenSecretManager();

this.serviceRPCAdres = this.serviceRpcServer.getListenerAdres(); setRpcServiceServerAdres(conf);

} /获得server this.server = RPC.getServer(this, socAdr.getHostName(), socAdr.getPort(), handlerCount, false, conf, namesystem

.getDelegationTokenSecretManager();

• this.server.start(); /启动 RPC server Clients只允许连接该server if (serviceRpcServer != nul) {

serviceRpcServer.start(); /启动 RPC serviceRpcServer 为HDFS服务的server

} startTrashEmptier(conf);

}

查看Namenode初始化源码得知：

RPC的server对象是通过ipc.RPC类的getServer()⽅法获得的。

下⾯咱们去看看ipc.RPC类中的getServer()源码吧：

代码⼗⼀：

Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


publicstatic Server getServer(final Object instance, final String bindAdres, finalint port, finalint numHandlers, finalbolean verbose, Configuration conf, SecretManager<? extends TokenIdentifier> secretManager)

throws IOException { returnnew Server(instance, conf, bindAdres, port, numHandlers, verbose, secretManager);

}

这时我们发现getServer()是⼀个创建Server对象的⼯⼚⽅法，

但创建的却是RPC.Server类的对象。

哈哈，现在你明⽩了我前⾯说的“RPC.Server是ipc.Server的实现类”了吧。

不过RPC.Server的构造函数还是调⽤了ipc.Server类的构造函数的，因篇幅所限，就不贴出相关源码 了。

- 2. 运⾏Server 如代码⼗所示，初始化Server后，Server端就运⾏起来了，看看 ipc.Server的start()源码吧：


代码⼗⼆：

Java代码

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


/* 启动服务 */ publicsynchronizedvoid start() {

responder.start(); /启动responder listener.start(); /启动listener handlers = new Handler[handlerCount];

for (int i = 0; i < handlerCount; i +) { handlers[i] = new Handler(i); handlers[i].start(); /逐个启动Handler

} }

- 3. Server处理请求


- 1）建⽴连接 分析过ipc.Client源码后，我们知道Client端的底层通信直接采⽤了阻塞式IO编程，


当时我们曾做出猜测：

Server端是不是也采⽤了阻塞式IO。

现在我们仔细地分析⼀下吧，

如果Server端也采⽤阻塞式IO，当连接进来的Client端很多时，势必会影响Server端的性能。

hadop的实现者们考虑到了这点，所以他们采⽤了java NIO来实现Server端，

java NIO可参考博客： 。

htp:/weixiaolu.iteye.com/blog/1479656

那Server端采⽤java NIO是怎么建⽴连接的呢？

分析源码得知，Server端采⽤Listener监听客户端的连接， 下⾯先分析⼀下 Listener的构造函数吧：

代码⼗三：

Java代码

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


public Listener() throws IOException { adres = new InetSocketAdres(bindAdres, port);

/ 创建ServerSocketChanel,并设置成⾮阻塞式 aceptChanel = ServerSocketChanel.open(); aceptChanel.configureBlocking(false);

/ 将server socket绑定到本地端⼝ bind(aceptChanel.socket(), adres, backlogLength); port = aceptChanel.socket().getLocalPort();

/ 获得⼀个selector selector= Selector.open();

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


readers = new Reader[readThreads]; readPol = Executors.newFixedThreadPol(readThreads);

/启动多个reader线程，为了防⽌请求多时服务端响应延时的问题

for (int i = 0; i < readThreads; i +) { Selector readSelector = Selector.open(); Reader reader = new Reader(readSelector); readers[i] = reader; readPol.execute(reader);

}

/ 注册连接事件 aceptChanel.register(selector, SelectionKey.OP_ACEPT); this.setName("IPC Server listener on " + port); this.setDaemon(true);

}

在启动Listener线程时，服务端会⼀直等待客户端的连接，

下⾯贴出Server.Listener类的run()⽅法：

代码⼗四：

Java代码

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


publicvoid run() {

•

while (runing) { SelectionKey key = nul; try {

selector.select(); Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); while (iter.hasNext() {

key = iter.next(); iter.remove(); try {

if (key.isValid() { if (key.isAceptable() doAcept(key); /具体的连接⽅法

- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.


} } catch (IOException e) { } key = nul;

} } catch (OutOfMemoryEror e) {

• }

下⾯贴出Server.Listener类中doAcept ()⽅法中的关键源码吧：

代码⼗五：

Java代码

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


void doAcept(SelectionKey key) throws IOException, OutOfMemoryEror { Conection c = nul; ServerSocketChanel server = (ServerSocketChanel) key.chanel(); SocketChanel chanel; while(chanel = server.acept() != nul) {/建⽴连接

chanel.configureBlocking(false); chanel.socket().setTcpNoDelay(tcpNoDelay); Reader reader = getReader(); /从readers池中获得⼀个reader try {

reader.startAd(); / 激活readSelector，设置ading为true SelectionKey readKey = reader.registerChanel(chanel);/将读事件设置成兴趣事件 c = new Conection(readKey, chanel, System.curentTimeMilis();/创建⼀个连接对象 readKey.atach(c); /将conection对象注⼊readKey synchronized (conectionList) {

conectionList.ad(numConections, c); numConections+;

}

•

} finaly { /设置ading为false，采⽤notify()唤醒⼀个reader,其实代码⼗三中启动的每个reader都使 /⽤了wait()⽅法等待。因篇幅有限，就不贴出源码了。

reader.finishAd();

} }

25.

当reader被唤醒，reader接着执⾏doRead()⽅法。

- 2）接收请求 下⾯贴出 Server.Listener.Reader类中的doRead()⽅法和 Server.Conection类中的readAndProces()⽅法源码：


代码⼗六：

Java代码

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


⽅法⼀：

void doRead(SelectionKey key) throws InteruptedException { int count = 0; Conection c = (Conection)key.atachment(); /获得conection对象 if (c = nul) {

return;

} c.setLastContact(System.curentTimeMilis(); try {

count = c.readAndProces(); / 接受并处理请求 } catch (InteruptedException ieo) {

- • }
- • }


⽅法⼆： publicint readAndProces() throws IOException, InteruptedException {

while (true) {

• if (!rpcHeaderRead) { if (rpcHeaderBufer = nul) { rpcHeaderBufer = ByteBufer.alocate(2);

/读取请求头 count = chanelRead(chanel, rpcHeaderBufer); if (count < 0| rpcHeaderBufer.remaining() > 0) {

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
- 49.
- 50.
- 51.
- 52.


return count; }

/ 读取请求版本号 int version = rpcHeaderBufer.get(0); byte[] method = newbyte[] {rpcHeaderBufer.get(1)};

•

data = ByteBufer.alocate(dataLength); }

/ 读取请求 count = chanelRead(chanel, data);

if (data.remaining() = 0) {

- • if (useSasl) {
- • } else {


procesOneRpc(data.aray();/处理请求 }

• }

} return count;

} }

- 3）获得cal对象 下⾯贴出 Server.Conection类中的procesOneRpc()⽅法和 procesData()⽅法的源码。


代码⼗七：

Java代码

1. 2. 3. 4. 5. 6. 7. 8. 9.

⽅法⼀： privatevoid procesOneRpc(byte[] buf) throws IOException,

InteruptedException { if (headerRead) {

procesData(buf);

} else { procesHeader(buf); headerRead = true; if (!authorizeConection() {

10. 11. 12. 13. 14. 15. 16. 17. 18. 19. 20. 21. 22. 23. 24. 25. 26. 27.

thrownew AcesControlException("Conection from " + this

+ " for protocol " + header.getProtocol()

+ " is unauthorized for user " + user); }

}

} ⽅法⼆：

privatevoid procesData(byte[] buf) throws IOException, InteruptedException { DataInputStream dis =

new DataInputStream(new ByteArayInputStream(buf); int id = dis.readInt(); / 尝试读取id Writable param = ReflectionUtils.newInstance(paramClas, conf);/读取参数 param.readFields(dis);

Cal cal = new Cal(id, param, this); /封装成cal calQueue.put(cal); / 将cal存⼊calQueue incRpcCount(); / 增加rpc请求的计数

}

- 4）处理cal对象


你还记得Server类中还有个Handler内部类吗？

呵呵，对cal对象的处理就是它⼲的。 下⾯贴出 Server.Handler类中run()⽅法中的关键代码：

代码⼗⼋：

Java代码

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


while (runing) { try { final Cal cal = calQueue.take(); /弹出cal，可能会阻塞

•

/调⽤ipc.Server类中的cal()⽅法，但该cal()⽅法是抽象⽅法，具体实现在RPC.Server类中 value = cal(cal.conection.protocol, cal.param, cal.timestamp); synchronized (cal.conection.responseQueue) {

setupResponse(buf, cal, (eror = nul) ? Status.SUCES : Status.EROR, value, erorClas, eror);

• /给客户端响应请求

responder.doRespond(cal); }

}

- 5）返回请求 下⾯贴出 Server.Responder类中的doRespond()⽅法源码：


代码⼗九：

Java代码

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


⽅法⼀： void doRespond(Cal cal) throws IOException {

synchronized (cal.conection.responseQueue) { cal.conection.responseQueue.adLast(cal); if (cal.conection.responseQueue.size() = 1) {

/ 返回响应结果，并激活writeSelector

procesResponse(cal.conection.responseQueue, true); }

} }
