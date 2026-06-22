# background

nety 是⼀个异步事件驱动的⽹络通信层框架，其官⽅⽂档的解释为

Nety is a NIO client server framework which enables quick and easy development of network aplications such as protocol servers and clients. It greatly simplifies and streamlines network progra ming such as TCP and UDP socket server.

我们在新美⼤消息推送系统sailfish(⽇均推送消息量50亿)，新美⼤移动端代理优化系统shark(⽇均吞 吐量30亿)中，均选择了nety作为底层⽹络通信框架。 既然两⼤如此重要的系统底层都使⽤到了nety，所以必然要对nety的机制，甚⾄源码了若指掌，于 是，便催⽣了nety源码系列⽂章。后⾯，我会通过⼀系列的主题把我从nety源码⾥所学到的毫⽆保留 地介绍给你，源码基于4.1.6.Final

# whynety

nety底层基于jdk的NIO，我们为什么不直接基于jdk的nio或者其他nio框架？下⾯是我总结出来的原因

- 1.使⽤jdk⾃带的nio需要了解太多的概念，编程复杂
- 2.nety底层IO模型随意切换，⽽这⼀切只需要做微⼩的改动
- 3.nety⾃带的拆包解包，异常检测等机制让你从nio的繁重细节中脱离出来，让你只需要关⼼业务逻辑
- 4.nety解决了jdk的很多包括空轮训在内的bug
- 5.nety底层对线程，selector做了很多细⼩的优化，精⼼设计的reactor线程做到⾮常⾼效的并发处理
- 6.⾃带各种协议栈让你处理任何⼀种通⽤协议都⼏乎不⽤亲⾃动⼿
- 7.nety社区活跃，遇到问题随时邮件列表或者isue
- 8.nety已经历各⼤rpc框架，消息中间件，分布式通信中间件线上的⼴泛验证，健壮性⽆⽐强⼤


# diveintonety

了解了这么多，今天我们就从⼀个例⼦出来，开始我们的nety源码之旅。 本篇主要讲述的是nety是如何绑定端⼝，启动服务。启动服务的过程中，你将会了解到nety各⼤核⼼ 组件，我先不会细讲这些组件，⽽是会告诉你各⼤组件是怎么串起来组成nety的核⼼

example

下⾯是⼀个⾮常简单的服务端启动代码

public final class SimpleServer {

public static void main(String[] args) throws Exception { EventLoopGroup bossGroup = new NioEventLoopGroup(1); EventLoopGroup workerGroup = new NioEventLoopGroup();

try { ServerBootstrap b = new ServerBootstrap(); b.group(bossGroup, workerGroup)

.channel(NioServerSocketChannel.class)

.handler(new SimpleServerHandler())

.childHandler(new ChannelInitializer<SocketChannel>() { @Override public void initChannel(SocketChannel ch) throws Exception { }

});

ChannelFuture f = b.bind(8888).sync();

f.channel().closeFuture().sync();

} finally { bossGroup.shutdownGracefully(); workerGroup.shutdownGracefully();

} }

private static class SimpleServerHandler extends ChannelInboundHandlerAdapter { @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {

System.out.println("channelActive"); }

@Override public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

System.out.println("channelRegistered"); }

@Override public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

System.out.println("handlerAdded"); }

} }

简单的⼏⾏代码就能开启⼀个服务端，端⼝绑定在 8，使⽤nio模式，下⾯讲下每⼀个步骤的处理细 节

EventLoopGroup 已经在我的 中详细剖析过，说⽩了，就是⼀个死循环，不停地检测IO事件， 处理IO事件，执⾏任务 ServerBootstrap 是服务端的⼀个启动辅助类，通过给他设置⼀系列参数来绑定端⼝启动服务 group(bossGroup, workerGroup) 我们需要两种类型的⼈⼲活，⼀个是⽼板，⼀个是⼯⼈，⽼板负责从 外⾯接活，接到的活分配给⼯⼈⼲，放到这⾥，bossGroup的作⽤就是不断地acept到新的连接，将新 的连接丢给workerGroup来处理

其他⽂章

.channel(NioServerSocketChannel.class) 表示服务端启动的是nio相关的chanel，chanel在nety⾥ ⾯是⼀⼤核⼼概念，可以理解为⼀条chanel就是⼀个连接或者⼀个服务端bind动作，后⾯会细说

.handler(new SimpleServerHandler() 表示服务器启动过程中，需要经过哪些流程，这⾥ SimpleServerHandler最终的顶层接⼝为ChannelHander，是nety的⼀⼤核⼼概念，表示数据流经过的 处理器，可以理解为流⽔线上的每⼀道关卡 childHandler(new ChannelInitializer<SocketChannel>)...表示⼀条新的连接进来之后，该怎么处 理，也就是上⾯所说的，⽼板如何给⼯⼈配活 ChannelFuture f = b.bind(8888).sync(); 这⾥就是真正的启动过程了，绑定 8端⼝，等待服务器 启动完毕，才会进⼊下⾏代码 f.channel().closeFuture().sync(); 等待服务端关闭socket bossGroup.shutdownGracefully(); workerGroup.shutdownGracefully(); 关闭两组死循环 上述代码可以很轻松地再本地跑起来，最终控制台的输出为： handlerAdded channelRegistered channelActive 关于为什么会顺序输出这些，深⼊分析之后其实很easy

## 深⼊细节

ServerBootstrap ⼀系列的参数配置其实没啥好讲的，⽆⾮就是使⽤ 的⽅式将启动服 务器需要的参数保存到filed。我们的重点落⼊到下⾯这段代码 b.bind(8888).sync(); 这⾥说⼀句：我们刚开始看源码，对细节没那么清楚的情况下可以借助IDE的debug功能，step by step，one step one test或者⼆分test的⽅式，来确定哪⾏代码是最终启动服务的⼊⼝，在这⾥，我们 已经确定了bind⽅法是⼊⼝，我们跟进去，分析 public ChannelFuture bind(int inetPort) {

method chaining

return bind(new InetSocketAddress(inetPort));

} 通过端⼝号创建⼀个 InetSocketAddress，然后继续bind

public ChannelFuture bind(SocketAddress localAddress) { validate(); if (localAddress == null) {

throw new NullPointerException("localAddress");

} return doBind(localAddress);

} validate() 验证服务启动需要的必要参数，然后调⽤doBind() private ChannelFuture doBind(final SocketAddress localAddress) {

//... final ChannelFuture regFuture = initAndRegister(); //... final Channel channel = regFuture.channel(); //... doBind0(regFuture, channel, localAddress, promise); //... return promise;

} 这⾥，我去掉了细枝末节，让我们专注于核⼼⽅法，其实就两⼤核⼼⼀个是 initAndRegister()，以及 doBind0() 其实，从⽅法名上⾯我们已经可以略窥⼀⼆，init->初始化，register->注册，那么到底要注册到什么 呢？联系到nio⾥⾯轮询器的注册，可能是把某个东⻄初始化好了之后注册到selector上⾯去，最后 bind，像是在本地绑定端⼝号，带着这些猜测，我们深⼊下去 initAndRegister() final ChannelFuture initAndRegister() {

Channel channel = null; // ... channel = channelFactory.newChannel(); //... init(channel); //... ChannelFuture regFuture = config().group().register(channel); //... return regFuture;

} 我们还是专注于核⼼代码，抛开边⻆料，我们看到 initAndRegister() 做了⼏件事情

- 1.new⼀个chanel
- 2.init这个chanel
- 3.将这个chanel register到某个对象 我们逐步分析这三件事情


- 1.new⼀个chanel 我们⾸先要搞懂chanel的定义，nety官⽅对chanel的描述如下


A nexus to a network socket or a component which is capable of I/O operations such as read, write, conect, and bind

这⾥的chanel，由于是在服务启动的时候创建，我们可以和普通Socket编程中的ServerSocket对应 上，表示服务端绑定的时候经过的⼀条流⽔线 我们发现这条chanel是通过⼀个 channelFactory new出来的，channelFactory 的接⼝很简单 public interface ChannelFactory<T extends Channel> extends io.netty.bootstrap.ChannelFactory<T> {

/**

- * Creates a new channel.
- */


@Override T newChannel();

}

就⼀个⽅法，我们查看chanelFactory被赋值的地⽅ AbstractBotstrap.java

public B channelFactory(ChannelFactory<? extends C> channelFactory) { if (channelFactory == null) { throw new NullPointerException("channelFactory");

} if (this.channelFactory != null) {

throw new IllegalStateException("channelFactory set already"); }

this.channelFactory = channelFactory; return (B) this;

}

在这⾥被赋值，我们层层回溯，查看该函数被调⽤的地⽅，发现最终是在这个函数中， ChanelFactory被new出

public B channel(Class<? extends C> channelClass) { if (channelClass == null) { throw new NullPointerException("channelClass");

} return channelFactory(new ReflectiveChannelFactory<C>(channelClass));

} 这⾥，我们的demo程序调⽤channel(channelClass)⽅法的时候，将channelClass作为 ReflectiveChannelFactory的构造函数创建出⼀个ReflectiveChannelFactory demo端的代码如下：

.channel(NioServerSocketChannel.class);

然后回到本节最开始 channelFactory.newChannel(); 我们就可以推断出，最终是调⽤到 ReflectiveChannelFactory.newChannel() ⽅法，跟进

public class ReflectiveChannelFactory<T extends Channel> implements ChannelFactory<T> {

private final Class<? extends T> clazz;

public ReflectiveChannelFactory(Class<? extends T> clazz) { if (clazz == null) { throw new NullPointerException("clazz");

} this.clazz = clazz;

}

@Override public T newChannel() {

try {

return clazz.newInstance(); } catch (Throwable t) {

throw new ChannelException("Unable to create Channel from class " + clazz, t); }

}

} 看到clazz.newInstance();，我们明⽩了，原来是通过反射的⽅式来创建⼀个对象，⽽这个clas就是 我们在ServerBootstrap中传⼊的NioServerSocketChannel.class 结果，绕了⼀圈，最终创建chanel相当于调⽤默认构造函数new出⼀个 NioServerSocketChannel对象 这⾥提⼀下，读源码细节，有两种读的⽅式，⼀种是回溯，⽐如⽤到某个对象的时候可以逐层追溯， ⼀定会找到该对象的最开始被创建的代码区块，还有⼀种⽅式就是⾃顶向下，逐层分析，⼀般⽤在分 析某个具体的⽅法，庖丁解⽜，最后拼接出完整的流程 接下来我们就可以将重⼼放到 NioServerSocketChannel的默认构造函数 private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider(); public NioServerSocketChannel() {

this(newSocket(DEFAULT_SELECTOR_PROVIDER));

} private static ServerSocketChannel newSocket(SelectorProvider provider) {

//... return provider.openServerSocketChannel();

} 通过SelectorProvider.openServerSocketChannel()创建⼀条server端chanel，然后进⼊到以下⽅法 public NioServerSocketChannel(ServerSocketChannel channel) {

super(null, channel, SelectionKey.OP_ACCEPT); config = new NioServerSocketChannelConfig(this, javaChannel().socket());

} 这⾥第⼀⾏代码就跑到⽗类⾥⾯去了，第⼆⾏，new出来⼀个 NioServerSocketChannelConfig，其顶层 接⼝为 ChannelConfig，nety官⽅的描述如下

A set of configuration properties of a Chanel. 基本可以判定，ChannelConfig 也是nety⾥⾯的⼀⼤核⼼模块，初次看源码，看到这⾥，我们⼤可不 必深挖这个对象，⽽是在⽤到的时候再回来深究，只要记住，这个对象在创建NioServerSocketChannel 对象的时候被创建即可 我们继续追踪到 NioServerSocketChannel 的⽗类

AbstractNioMesageChanel.java

protected AbstractNioMessageChannel(Channel parent, SelectableChannel ch, int readInterestOp) {

super(parent, ch, readInterestOp); }

继续往上追 AbstractNioChanel.java

protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) { super(parent); this.ch = ch; this.readInterestOp = readInterestOp; //... ch.configureBlocking(false); //...

} 这⾥，简单地将前⾯ provider.openServerSocketChannel(); 创建出来的 ServerSocketChannel 保存到 成员变量，然后调⽤ch.configureBlocking(false);设置该chanel为⾮阻塞模式，标准的jdk nio编程 的玩法 这⾥的 readInterestOp 即前⾯层层传⼊的 SelectionKey.OP_ACCEPT，接下来重点分析 super(parent); (这⾥的parent其实是nul，由前⾯写死传⼊)

AbstractChanel.java

protected AbstractChannel(Channel parent) { this.parent = parent; id = newId(); unsafe = newUnsafe(); pipeline = newChannelPipeline();

}

到了这⾥，⼜new出来三⼤组件，赋值到成员变量，分别为

id = newId(); protected ChannelId newId() {

return DefaultChannelId.newInstance(); }

id是nety中每条chanel的唯⼀标识，这⾥不细展开，接着

unsafe = newUnsafe(); protected abstract AbstractUnsafe newUnsafe();

查看Unsafe的定义

Unsafe operations that should never be caled from user-code. These methods are only provided to implement the actual transport, and must be invoked from an I/O thread

成功捕捉nety的⼜⼀⼤组件，我们可以先不⽤管TA是⼲嘛的，只需要知道这⾥的 newUnsafe⽅法最终 属于类NioServerSocketChannel中 最后 pipeline = newChannelPipeline();

protected DefaultChannelPipeline newChannelPipeline() {

return new DefaultChannelPipeline(this); }

protected DefaultChannelPipeline(Channel channel) { this.channel = ObjectUtil.checkNotNull(channel, "channel"); succeededFuture = new SucceededChannelFuture(channel, null); voidPromise = new VoidChannelPromise(channel, true);

tail = new TailContext(this); head = new HeadContext(this);

head.next = tail; tail.prev = head;

} 初次看这段代码，可能并不知道 DefaultChannelPipeline 是⼲嘛⽤的，我们仍然使⽤上⾯的⽅式，查 看顶层接⼝ChannelPipeline的定义

A list of ChanelHandlers which handles or intercepts inbound events and outbound operations of a Chanel

从该类的⽂档中可以看出，该接⼝基本上⼜是nety的⼀⼤核⼼模块 到了这⾥，我们总算把⼀个服务端chanel创建完毕了，将这些细节串起来的时候，我们顺带提取出 nety的⼏⼤基本组件，先总结如下

Chanel

ChanelConfig

ChanelId

Unsafe

Pipeline

ChanelHander

初次看代码的时候，我们的⽬标是跟到服务器启动的那⼀⾏代码，我们先把以上这⼏个组件记下来， 等代码跟完，我们就可以⾃顶向下，逐层分析，我会放到后⾯源码系列中去深⼊到每个组件 总结⼀下，⽤户调⽤⽅法 Bootstrap.bind(port) 第⼀步就是通过反射的⽅式new⼀个 NioServerSocketChannel对象，并且在new的过程中创建了⼀系列的核⼼组件，仅此⽽已，并⽆他，真 正的启动我们还需要继续跟

### 2.init这个chanel 到了这⾥，你最好跳到⽂章最开始的地⽅回忆⼀下，第⼀步newChanel完毕，这⾥就对这个chanel 做init，init⽅法具体⼲啥，我们深⼊

@Override void init(Channel channel) throws Exception {

final Map<ChannelOption<?>, Object> options = options0(); synchronized (options) {

channel.config().setOptions(options); }

final Map<AttributeKey<?>, Object> attrs = attrs0(); synchronized (attrs) {

for (Entry<AttributeKey<?>, Object> e: attrs.entrySet()) { @SuppressWarnings("unchecked") AttributeKey<Object> key = (AttributeKey<Object>) e.getKey(); channel.attr(key).set(e.getValue());

} }

ChannelPipeline p = channel.pipeline();

final EventLoopGroup currentChildGroup = childGroup; final ChannelHandler currentChildHandler = childHandler; final Entry<ChannelOption<?>, Object>[] currentChildOptions; final Entry<AttributeKey<?>, Object>[] currentChildAttrs; synchronized (childOptions) { currentChildOptions =

childOptions.entrySet().toArray(newOptionArray(childOptions.size())); } synchronized (childAttrs) {

currentChildAttrs = childAttrs.entrySet().toArray(newAttrArray(childAttrs.size())); }

p.addLast(new ChannelInitializer<Channel>() { @Override public void initChannel(Channel ch) throws Exception {

final ChannelPipeline pipeline = ch.pipeline(); ChannelHandler handler = config.handler(); if (handler != null) {

pipeline.addLast(handler); }

ch.eventLoop().execute(new Runnable() { @Override public void run() {

pipeline.addLast(new ServerBootstrapAcceptor(

currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));

} });

} });

}

初次看到这个⽅法，可能会觉得，哇塞，⽼⻓了，这可这么看？还记得我们前⾯所说的吗，庖丁解 ⽜，逐步拆解，最后归⼀，下⾯是我的拆解步骤

- 1.设置option和atr final Map<ChannelOption<?>, Object> options = options0();

synchronized (options) {

channel.config().setOptions(options); }

final Map<AttributeKey<?>, Object> attrs = attrs0(); synchronized (attrs) {

for (Entry<AttributeKey<?>, Object> e: attrs.entrySet()) { @SuppressWarnings("unchecked") AttributeKey<Object> key = (AttributeKey<Object>) e.getKey(); channel.attr(key).set(e.getValue());

}

} 通过这⾥我们可以看到，这⾥先调⽤options0()以及attrs0()，然后将得到的options和atrs注⼊到 chanelConfig或者chanel中，关于option和atr是⼲嘛⽤的，其实你现在不⽤了解得那么深⼊，只需 要查看最顶层接⼝ChannelOption以及查看⼀下chanel的具体继承关系，就可以了解，我把这两个也 放到后⾯的源码分析系列再讲

- 2.设置新接⼊chanel的option和atr final EventLoopGroup currentChildGroup = childGroup; final ChannelHandler currentChildHandler = childHandler; final Entry<ChannelOption<?>, Object>[] currentChildOptions; final Entry<AttributeKey<?>, Object>[] currentChildAttrs; synchronized (childOptions) {

currentChildOptions = childOptions.entrySet().toArray(newOptionArray(childOptions.size())); } synchronized (childAttrs) {

currentChildAttrs = childAttrs.entrySet().toArray(newAttrArray(childAttrs.size())); }

这⾥，和上⾯类似，只不过不是设置当前chanel的这两个属性，⽽是对应到新进来连接对应的 chanel，由于我们这篇⽂章只关⼼到server如何启动，接⼊连接放到下⼀篇⽂章中详细剖析

- 3.加⼊新连接处理器


p.addLast(new ChannelInitializer<Channel>() { @Override public void initChannel(Channel ch) throws Exception {

final ChannelPipeline pipeline = ch.pipeline(); ChannelHandler handler = config.handler(); if (handler != null) {

pipeline.addLast(handler); }

ch.eventLoop().execute(new Runnable() { @Override public void run() {

pipeline.addLast(new ServerBootstrapAcceptor(

currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));

} });

}

}); 到了最后⼀步，p.addLast()向serverChanel的流⽔线处理器中加⼊了⼀个 ServerBootstrapAcceptor，从名字上就可以看出来，这是⼀个接⼊器，专⻔接受新请求，把新的请求 扔给某个事件循环器，我们先不做过多分析 来，我们总结⼀下，我们发现其实init也没有启动服务，只是初始化了⼀些基本的配置和属性，以及在 pipeline上加⼊了⼀个接⼊器，⽤来专⻔接受新连接，我们还得继续往下跟

- 3.将这个chanel register到某个对象 这⼀步，我们是分析如下⽅法 ChannelFuture regFuture = config().group().register(channel);


调⽤到 NioEventLoop 中的register @Override public ChannelFuture register(Channel channel) {

return register(new DefaultChannelPromise(channel, this));

} @Override public ChannelFuture register(final ChannelPromise promise) {

ObjectUtil.checkNotNull(promise, "promise"); promise.channel().unsafe().register(this, promise); return promise;

} 好了，到了这⼀步，还记得这⾥的unsafe()返回的应该是什么对象吗？不记得的话可以看下前⾯关于 unsafe的描述，或者最快的⽅式就是debug到这边，跟到register⽅法⾥⾯，看看是哪种类型的unsafe 我们跟进去之后发现是

AbstractUnsafe.java

@Override public final void register(EventLoop eventLoop, final ChannelPromise promise) {

// ... AbstractChannel.this.eventLoop = eventLoop; // ... register0(promise);

} 这⾥我们依然只需要focus重点，先将EventLop事件循环器绑定到该NioServerSocketChanel上，然 后调⽤ register0() private void register0(ChannelPromise promise) {

try { boolean firstRegistration = neverRegistered; doRegister(); neverRegistered = false; registered = true;

pipeline.invokeHandlerAddedIfNeeded();

safeSetSuccess(promise); pipeline.fireChannelRegistered(); if (isActive()) {

if (firstRegistration) { pipeline.fireChannelActive(); } else if (config().isAutoRead()) {

beginRead(); }

}

} catch (Throwable t) { closeForcibly(); closeFuture.setClosed(); safeSetFailure(promise, t);

}

} 这⼀段其实也很清晰，先调⽤ doRegister();，具体⼲啥待会再讲，然后调⽤

invokeHandlerAddedIfNeeded(), 于是乎，控制台第⼀⾏打印出来的就是 handlerAdded

关于最终是如何调⽤到的，我们后⾯详细剖析pipeline的时候再讲 然后调⽤ pipeline.fireChannelRegistered(); 调⽤之后，控制台的显示为 handlerAdded channelRegistered 继续往下跟

if (isActive()) { if (firstRegistration) { pipeline.fireChannelActive(); } else if (config().isAutoRead()) {

beginRead(); }

}

读到这，你可能会想当然地以为，控制台最后⼀⾏ pipeline.fireChannelActive(); 由这⾏代码输出，我们不妨先看⼀下 isActive() ⽅法 @Override public boolean isActive() {

return javaChannel().socket().isBound(); }

最终调⽤到jdk中 ServerSocket.java

/**

- * Returns the binding state of the ServerSocket.

*

- * @return true if the ServerSocket succesfuly bound to an address
- * @since 1.4
- */


public boolean isBound() { // Before 1.3 ServerSockets were always bound during creation return bound || oldImpl;

} 这⾥isBound()返回false，但是从⽬前我们跟下来的流程看，我们并没有将⼀个ServerSocket绑定到⼀ 个adres，所以 isActive() 返回false，我们没有成功进⼊到pipeline.fireChannelActive();⽅法， 那么最后⼀⾏到底是谁输出的呢，我们有点抓狂，其实，只要熟练运⽤IDE，要定位函数调⽤栈，⽆⽐ 简单 下⾯是我⽤intelij定位函数调⽤的具体⽅法

![image 1](<netty源码分析之服务端启动全解析.note_images/imageFile1.png>)

Intelij函数调⽤定位 我们先在最终输出⽂字的这⼀⾏代码处打⼀个断点，然后debug，运⾏到这⼀⾏，intelij⾃动给我们拉 起了调⽤栈，我们唯⼀要做的事，就是移动⽅向键，就能看到函数的完整的调⽤链 如果你看到⽅法的最近的发起端是⼀个线程Runable的run⽅法，那么就在提交Runable对象⽅法的 地⽅打⼀个断点，去掉其他断点，重新debug，⽐如我们⾸次debug发现调⽤栈中的最近的⼀个 Runable如下

if (!wasActive && isActive()) {

invokeLater(new Runnable() { @Override public void run() {

pipeline.fireChannelActive(); }

});

} 我们停在了这⼀⾏pipeline.fireChannelActive();, 我们想看最初始的调⽤，就得跳出来，断点打到 if (!wasActive && isActive())，因为nety⾥⾯很多任务执⾏都是异步线程即reactor线程调⽤的(具 体可以看reactor线程三部曲中的 )，如果我们要查看最先发起的⽅法调⽤，我们必须得查看 Runable被提交的地⽅，逐次递归下去，就能找到那⾏"消失的代码" 最终，通过这种⽅式，终于找到了 pipeline.fireChannelActive(); 的发起调⽤的代码，不巧，刚好就 是下⾯的doBind0()⽅法 doBind0()

最后⼀曲

private static void doBind0( final ChannelFuture regFuture, final Channel channel, final SocketAddress localAddress, final ChannelPromise promise) {

channel.eventLoop().execute(new Runnable() { @Override public void run() {

if (regFuture.isSuccess()) { channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE); } else {

promise.setFailure(regFuture.cause()); }

} });

} 我们发现，在调⽤doBind0(...)⽅法的时候，是通过包装⼀个Runable进⾏异步化的，关于异步化 task，可以看下我前⾯的⽂章， 好，接下来我们进⼊到channel.bind()⽅法

nety源码分析之揭开reactor线程的⾯纱（三）

AbstractChanel.java

@Override public ChannelFuture bind(SocketAddress localAddress) {

return pipeline.bind(localAddress); }

发现是调⽤pipeline的bind⽅法

@Override public final ChannelFuture bind(SocketAddress localAddress) {

return tail.bind(localAddress); }

相信你对tail是什么不是很了解，可以翻到最开始，tail在创建pipeline的时候出现过，关于pipeline和 tail对应的类，我后⾯源码系列会详细解说，这⾥，你要想知道接下来代码的⾛向，唯⼀⼀个⽐较好的 ⽅式就是debug 单步进⼊，篇幅原因，我就不详细展开 最后，我们来到了如下区域 HeadContext.java

@Override public void bind(

ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {

unsafe.bind(localAddress, promise);

} 这⾥的unsafe就是前⾯提到的 AbstractUnsafe, 准确点，应该是 NioMessageUnsafe 我们进⼊到它的bind⽅法

@Override public final void bind(final SocketAddress localAddress, final ChannelPromise promise) {

// ... boolean wasActive = isActive(); // ... doBind(localAddress);

if (!wasActive && isActive()) {

invokeLater(new Runnable() { @Override public void run() {

pipeline.fireChannelActive(); }

});

} safeSetSuccess(promise);

} 显然按照正常流程，我们前⾯已经分析到 isActive(); ⽅法返回false，进⼊到 doBind()之后，如果 chanel被激活了，就发起pipeline.fireChannelActive();调⽤，最终调⽤到⽤户⽅法，在控制台打印 出了最后⼀⾏，所以到了这⾥，你应该清楚为什么最终会在控制台按顺序打印出那三⾏字了吧

doBind()⽅法也很简单 protected void doBind(SocketAddress localAddress) throws Exception {

if (PlatformDependent.javaVersion() >= 7) { //noinspection Since15 javaChannel().bind(localAddress, config.getBacklog());

} else {

javaChannel().socket().bind(localAddress, config.getBacklog()); }

} 最终调到了jdk⾥⾯的bind⽅法，这⾏代码过后，正常情况下，就真正进⾏了端⼝的绑定。 另外，通过⾃顶向下的⽅式分析，在调⽤pipeline.fireChannelActive();⽅法的时候，会调⽤到如下 ⽅法

HeadContext.java

public void channelActive(ChannelHandlerContext ctx) throws Exception { ctx.fireChannelActive();

readIfIsAutoRead();

} 进⼊ readIfIsAutoRead

private void readIfIsAutoRead() { if (channel.config().isAutoRead()) {

channel.read(); }

} 分析isAutoRead⽅法 private volatile int autoRead = 1; public boolean isAutoRead() { return autoRead == 1;

} 由此可⻅，isAutoRead⽅法默认返回true，于是进⼊到以下⽅法 public Channel read() {

pipeline.read(); return this;

}

最终调⽤到 AbstractNioUnsafe.java

protected void doBeginRead() throws Exception { final SelectionKey selectionKey = this.selectionKey; if (!selectionKey.isValid()) {

return; }

readPending = true;

final int interestOps = selectionKey.interestOps(); if ((interestOps & readInterestOp) == 0) {

selectionKey.interestOps(interestOps | readInterestOp); }

} 这⾥的this.selectionKey就是我们在前⾯register步骤返回的对象，前⾯我们在register的时候，注册 测ops是0 回忆⼀下注册

AbstractNioChanel

selectionKey = javaChannel().register(eventLoop().selector, 0, this)

这⾥相当于把注册过的ops取出来，通过了if条件，然后调⽤ selectionKey.interestOps(interestOps | readInterestOp); ⽽这⾥的 readInterestOp 就是前⾯newChanel的时候传⼊的SelectionKey.OP_ACCEPT，⼜是标准的 jdk nio的玩法，到此，你需要了解的细节基本已经差不多了，就这样结束吧！

## sumary

最后，我们来做下总结，nety启动⼀个服务所经过的流程

- 1.设置启动类参数，最重要的就是设置chanel


- 2.创建server对应的chanel，创建各⼤组件，包括 ChanelConfig,ChanelId,ChanelPipeline,ChanelHandler,Unsafe等
- 3.初始化server对应的chanel，设置⼀些atr，option，以及设置⼦chanel的atr，option，给server 的chanel添加新chanel接⼊器，并出发adHandler,register等事件
- 4.调⽤到jdk底层做端⼝绑定，并触发active事件，active触发的时候，真正做服务费端⼝绑定 另外，⽂章中阅读源码的思路详细或许也可以给你带来⼀些帮助，完。


⽂／the_flash（简书作者） 原⽂链接：htp:/ w.jianshu.com/p/c5068cab217 著作权归作者所有，转载请联系作者获得授权，并标注“简书作者”。

