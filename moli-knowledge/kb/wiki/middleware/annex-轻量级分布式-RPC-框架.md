---
title: 轻量级分布式 RPC 框架.note（原文插图 annex）
slug: annex-轻量级分布式-RPC-框架
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/rpc/轻量级分布式 RPC 框架.note.md
related: [dubbo-调用原理与分层]
created: 2026-07-05
updated: 2026-07-05
---

htp:/my.oschina.net/huangyong/blog/361751?fromer=edJzaoFc

第⼀步：编写服务接⼝

第⼆步：编写服务接⼝的实现类

第三步：配置服务端

第四步：启动服务器并发布服务

第五步：实现服务注册

第六步：实现 RPC 服务器

第七步：配置客户端

第⼋步：实现服务发现

第九步：实现 RPC 代理

第⼗步：发送 RPC 请求

总结

附录：Maven 依赖

RPC，即 Remote Procedure Cal（远程过程调⽤），说得通俗⼀点就是：调⽤远程计算机上的服务，就像调⽤本地服务⼀样。

RPC 可基于 HTP 或 TCP 协议，Web Service 就是基于 HTP 协议的 RPC，它具有良好的跨平台性，但其性能却不如基于 TCP 协议的 RPC。会两⽅⾯会直接影响 RPC 的性能，⼀是传输⽅式，⼆是序列化。

众所周知，TCP 是传输层协议，HTP 是应⽤层协议，⽽传输层较应⽤层更加底层，在数据传输⽅⾯，越底层越快，因此，在⼀般 情况下，TCP ⼀定⽐ HTP 快。就序列化⽽⾔，Java 提供了默认的序列化⽅式，但在⾼并发的情况下，这种⽅式将会带来⼀些性 能上的瓶颈，于是市⾯上出现了⼀系列优秀的序列化框架，⽐如：Protobuf、Kryo、Hesian、Jackson 等，它们可以取代 Java 默认的序列化，从⽽提供更⾼效的性能。

为了⽀持⾼并发，传统的阻塞式 IO 显然不太合适，因此我们需要异步的 IO，即 NIO。Java 提供了 NIO 的解决⽅案，Java 7 也提 供了更优秀的 NIO.2 ⽀持，⽤ Java 实现 NIO 并不是遥不可及的事情，只是需要我们熟悉 NIO 的技术细节。

我们需要将服务部署在分布式环境下的不同节点上，通过服务注册的⽅式，让客户端来⾃动发现当前可⽤的服务，并调⽤这些服 务。这需要⼀种服务注册表（Service Registry）的组件，让它来注册分布式环境下所有的服务地址（包括：主机名与端⼝号）。

应⽤、服务、服务注册表之间的关系⻅下图：

![image 1](assets/imageFile1.png)

每台 Server 上可发布多个 Service，这些 Service 共⽤⼀个 host 与 port，在分布式环境下会提供 Server 共同对外提供 Service。此外，为防⽌ Service Registry 出现单点故障，因此需要将其搭建为集群环境。

本⽂将为您揭晓开发轻量级分布式 RPC 框架的具体过程，该框架基于 TCP 协议，提供了 NIO 特性，提供⾼效的序列化⽅式，同 时也具备服务注册与发现的能⼒。

根据以上技术需求，我们可使⽤如下技术选型：

- 1.
- 2.
- 3.
- 4.


Spring：它是最强⼤的依赖注⼊框架，也是业界的权威标准。 Nety：它使 NIO 编程更加容易，屏蔽了 Java 底层的 NIO 细节。 Protostuf：它基于 Protobuf 序列化框架，⾯向 POJO，⽆需编写 .proto ⽂件。 ZoKeper：提供服务注册与发现功能，开发分布式系统的必备选择，同时它也具备天⽣的集群能⼒。

相关 Maven 依赖请⻅附录。

第⼀步：编写服务接⼝

<!-- lang: java --> public interface HelloService {

String hello(String name); }

将该接⼝放在独⽴的客户端 jar 包中，以供应⽤使⽤。

# 第⼆步：编写服务接⼝的实现类

<!-- lang: java --> @RpcService(HelloService.class) // 指 定 远 程 接 ⼝ publicclassHelloServiceImplimplementsHelloService {

@Override public String hello(String name) {

## return"Hello! " + name; }

}

使⽤RpcService注解定义在服务接⼝的实现类上，需要对该实现类指定远程接⼝，因为实现类可能会实现多个接⼝，⼀定要告诉 框架哪个才是远程接⼝。

RpcService代码如下：

<!-- lang: java --> @Target({ElementType.TYPE}) @Retention(RetentionPolicy.RUNTIME) @Component // 表明可被 Spring 扫描 public @interface RpcService {

Class<?> value(); }

该注解具备 Spring 的Component注解的特性，可被 Spring 扫描。

该实现类放在服务端 jar 包中，该 jar 包还提供了⼀些服务端的配置⽂件与启动服务的引导程序。

# 第三步：配置服务端

服务端 Spring 配置⽂件名为spring.xml，内容如下：

<!-- lang: xml --><beans...><context:component-scanbase-package="com.xxx.rpc.sample.server"/> <context:property-placeholderlocation="classpath:config.properties"/><!-- 配 置 服 务 注 册 组 件 --> <beanid="serviceRegistry"class="com.xxx.rpc.registry.ServiceRegistry"><constructorargname="registryAddress"value="${registry.address}"/></bean><!-- 配 置 RPC 服 务 器 --> <beanid="rpcServer"class="com.xxx.rpc.server.RpcServer"><constructor-

argname="serverAddress"value="${server.address}"/><constructorargname="serviceRegistry"ref="serviceRegistry"/></bean></beans>

具体的配置参数在config.properties⽂件中，内容如下：

<!-- lang: java --> # ZooKeeper 服务器 registry.address=127.0.0.1:2181

# RPC 服务器 server.address=127.0.0.1:8000

以上配置表明：连接本地的 ZoKeper 服务器，并在 8 0 端⼝上发布 RPC 服务。

# 第四步：启动服务器并发布服务

为了加载 Spring 配置⽂件来发布服务，只需编写⼀个引导程序即可：

public class RpcBootstrap {

publicstaticvoidmain(String[] args) {

newClassPathXmlApplicationContext("spring.xml"); }

}

运⾏RpcBootstrap类的main⽅法即可启动服务端，但还有两个重要的组件尚未实现，它们分别是：ServiceRegistry与 RpcServer，下⽂会给出具体实现细节。

# 第五步：实现服务注册

使⽤ ZoKeper 客户端可轻松实现服务注册功能，ServiceRegistry代码如下：

public class ServiceRegistry {

privatestaticfinal Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

private CountDownLatch latch = new CountDownLatch(1);

private String registryAddress;

publicServiceRegistry(String registryAddress) { this.registryAddress = registryAddress; }

publicvoidregister(String data) {

if (data != null) { ZooKeeper zk = connectServer(); if (zk != null) {

createNode(zk, data); }

} }

private ZooKeeper connectServer() { ZooKeeper zk = null; try {

zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() { @Override publicvoidprocess(WatchedEvent event) {

if (event.getState() == Event.KeeperState.SyncConnected) {

latch.countDown(); }

}

}); latch.await();

} catch (IOException | InterruptedException e) { LOGGER.error("", e);

## } return zk;

}

privatevoidcreateNode(ZooKeeper zk, String data) {

try { byte[] bytes = data.getBytes(); String path = zk.create(Constant.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE,

CreateMode.EPHEMERAL_SEQUENTIAL);

LOGGER.debug("create zookeeper node ({} => {})", path, data); } catch (KeeperException | InterruptedException e) {

LOGGER.error("", e); }

} }

其中，通过Constant配置了所有的常量：

<!-- lang: java --> public interface Constant {

intZK_SESSION_TIMEOUT = 5000;

StringZK_REGISTRY_PATH = "/registry"; StringZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";

}

注意：⾸先需要使⽤ ZoKeper 客户端命令⾏创建/registry永久节点，⽤于存放所有的服务临时节点。

# 第六步：实现 RPC服务器

使⽤ Nety 可实现⼀个⽀持 NIO 的 RPC 服务器，需要使⽤ServiceRegistry注册服务地址，RpcServer代码如下：

publicclassRpcServerimplementsApplicationContextAware, InitializingBean {

privatestaticfinal Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

private String serverAddress; private ServiceRegistry serviceRegistry;

private Map<String, Object> handlerMap = new HashMap<>(); // 存 放接 ⼝ 名 与 服 务 对 象 之 间 的 映 射 关 系 public RpcServer(String serverAddress) {

this.serverAddress = serverAddress; }

public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) { this.serverAddress = serverAddress; this.serviceRegistry = serviceRegistry;

}

@Override public void setApplicationContext(ApplicationContext ctx) throws BeansException {

Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class); // 获 取 所 有 带 有 RpcService 注 解 的 Spring Beanif (MapUtils.isNotEmpty(serviceBeanMap)) {

for (Object serviceBean : serviceBeanMap.values()) {

String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();

handlerMap.put(interfaceName, serviceBean); }

} }

@Override public void afterPropertiesSet() throws Exception {

EventLoopGroup bossGroup = new NioEventLoopGroup(); EventLoopGroup workerGroup = new NioEventLoopGroup(); try {

ServerBootstrap bootstrap = new ServerBootstrap(); bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)

.childHandler(new ChannelInitializer<SocketChannel>() { @Override public void initChannel(SocketChannel channel) throws Exception {

channel.pipeline()

.addLast(new RpcDecoder(RpcRequest.class)) // 将 RPC 请 求 进 ⾏ 解 码 （ 为 了 处 理 请 求 ）

.addLast(new RpcEncoder(RpcResponse.class)) // 将 RPC 响 应 进 ⾏ 编 码 （ 为 了 返 回 响 应 ）

.addLast(new RpcHandler(handlerMap)); // 处 理 RPC 请 求 }

})

.option(ChannelOption.SO_BACKLOG, 128)

.childOption(ChannelOption.SO_KEEPALIVE, true);

String[] array = serverAddress.split(":"); String host = array[0]; int port = Integer.parseInt(array[1]);

ChannelFuture future = bootstrap.bind(host, port).sync(); LOGGER.debug("server started on port {}", port);

if (serviceRegistry != null) {

serviceRegistry.register(serverAddress); // 注 册 服 务 地址 }

future.channel().closeFuture().sync();

} finally { workerGroup.shutdownGracefully(); bossGroup.shutdownGracefully();

} }

}

以上代码中，有两个重要的 POJO 需要描述⼀下，它们分别是RpcRequest与RpcResponse。

使⽤RpcRequest封装 RPC 请求，代码如下：

<!-- lang: java --> public class RpcRequest {

private String requestId; private String className; private String methodName; private Class<?>[] parameterTypes; private Object[] parameters;

// getter/setter... }

使⽤RpcResponse封装 RPC 响应，代码如下：

public class RpcResponse {

privateStringrequestId; privateThrowableerror; privateObjectresult;

// getter/setter... }

使⽤RpcDecoder提供 RPC 解码，只需扩展 Nety 的ByteToMessageDecoder抽象类的decode⽅法即可，代码如下：

<!-- lang: java --> publicclassRpcDecoderextendsByteToMessageDecoder {

private Class<?> genericClass;

publicRpcDecoder(Class<?> genericClass) { this.genericClass = genericClass; }

@Override publicvoiddecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception

{

if (in.readableBytes() < 4) { return;

} in.markReaderIndex(); int dataLength = in.readInt(); if (dataLength < 0) {

ctx.close();

} if (in.readableBytes() < dataLength) {

in.resetReaderIndex(); return;

} byte[] data = newbyte[dataLength]; in.readBytes(data);

Object obj = SerializationUtil.deserialize(data, genericClass); out.add(obj);

} }

使⽤RpcEncoder提供 RPC 编码，只需扩展 Nety 的MessageToByteEncoder抽象类的encode⽅法即可，代码如下：

<!-- lang: java -->publicclass RpcEncoder extends MessageToByteEncoder {

privateClass<?> genericClass;

public RpcEncoder(Class<?> genericClass) {

this.genericClass = genericClass; }

@Override public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {

if (genericClass.isInstance(in)) { byte[] data = SerializationUtil.serialize(in); out.writeInt(data.length); out.writeBytes(data);

} }

}

编写⼀个SerializationUtil⼯具类，使⽤Protostuff实现序列化：

publicclassSerializationUtil {

privatestatic Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

privatestatic Objenesis objenesis = new ObjenesisStd(true);

privateSerializationUtil() { }

@SuppressWarnings("unchecked") privatestatic <T> Schema<T> getSchema(Class<T> cls) {

Schema<T> schema = (Schema<T>) cachedSchema.get(cls); if (schema == null) {

schema = RuntimeSchema.createFrom(cls); if (schema != null) {

cachedSchema.put(cls, schema); }

} return schema;

}

@SuppressWarnings("unchecked") publicstatic <T> byte[] serialize(T obj) {

Class<T> cls = (Class<T>) obj.getClass(); LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE); try {

Schema<T> schema = getSchema(cls); return ProtostuffIOUtil.toByteArray(obj, schema, buffer);

} catch (Exception e) {

thrownew IllegalStateException(e.getMessage(), e); } finally {

buffer.clear(); }

}

publicstatic <T> T deserialize(byte[] data, Class<T> cls) {

try { T message = (T) objenesis.newInstance(cls); Schema<T> schema = getSchema(cls); ProtostuffIOUtil.mergeFrom(data, message, schema); return message; } catch (Exception e) {

thrownew IllegalStateException(e.getMessage(), e); }

} }

以上了使⽤ Objenesis 来实例化对象，它是⽐ Java 反射更加强⼤。

注意：如需要替换其它序列化框架，只需修改SerializationUtil即可。当然，更好的实现⽅式是提供配置项来决定使⽤哪种 序列化⽅式。

使⽤RpcHandler中处理 RPC 请求，只需扩展 Nety 的SimpleChannelInboundHandler抽象类即可，代码如下：

public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

private static final LoggerLOGGER = LoggerFactory.getLogger(RpcHandler.class);

private final Map<String, Object> handlerMap;

## public RpcHandler(Map<String, Object> handlerMap) {

this.handlerMap = handlerMap; }

@Override public void channelRead0(final ChannelHandlerContext ctx, RpcRequest request) throws

Exception { RpcResponse response = new RpcResponse(); response.setRequestId(request.getRequestId()); try {

Objectresult = handle(request); response.setResult(result);

} catch (Throwable t) { response.setError(t);

} ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

}

private Object handle(RpcRequest request) throws Throwable { String className = request.getClassName(); Object serviceBean = handlerMap.get(className);

Class<?> serviceClass = serviceBean.getClass(); String methodName = request.getMethodName(); Class<?>[] parameterTypes = request.getParameterTypes(); Object[] parameters = request.getParameters();

/*Methodmethod = serviceClass.getMethod(methodName, parameterTypes); method.setAccessible(true); returnmethod.invoke(serviceBean, parameters);*/

FastClass serviceFastClass = FastClass.create(serviceClass); FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes); return serviceFastMethod.invoke(serviceBean, parameters);

}

@Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

LOGGER.error("server caught exception", cause); ctx.close();

} }

为了避免使⽤ Java 反射带来的性能问题，我们可以使⽤ CGLib 提供的反射 API，如上⾯⽤到的FastClass与FastMethod。

# 第七步：配置客户端

同样使⽤ Spring 配置⽂件来配置 RPC 客户端，spring.xml代码如下：

<!-- lang: java --><beans...><context:propertyplaceholderlocation="classpath:config.properties"/><!-- 配 置 服 务 发 现 组 件 --> <beanid="serviceDiscovery"class="com.xxx.rpc.registry.ServiceDiscovery"><constructorargname="registryAddress"value="${registry.address}"/></bean><!-- 配 置 RPC 代 理 --> <beanid="rpcProxy"class="com.xxx.rpc.client.RpcProxy"><constructorargname="serviceDiscovery"ref="serviceDiscovery"/></bean></beans>

其中config.properties提供了具体的配置：

<!-- lang: java --> # ZooKeeper 服务器 registry.address=127.0.0.1:2181

# 第⼋步：实现服务发现

同样使⽤ ZoKeper 实现服务发现功能，⻅如下代码：

public class ServiceDiscovery {

privatestaticfinal Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

private CountDownLatch latch = new CountDownLatch(1);

privatevolatile List<String> dataList = new ArrayList<>();

privateString registryAddress;

public ServiceDiscovery(String registryAddress) { this.registryAddress = registryAddress;

ZooKeeper zk = connectServer(); if (zk != null) {

watchNode(zk); }

}

publicString discover() { String data = null; int size = dataList.size(); if (size > 0) {

if (size == 1) { data = dataList.get(0); LOGGER.debug("using only data: {}", data);

} else { data = dataList.get(ThreadLocalRandom.current().nextInt(size)); LOGGER.debug("using random data: {}", data);

}

## } return data;

}

private ZooKeeper connectServer() { ZooKeeper zk = null; try {

zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() { @Override publicvoid process(WatchedEvent event) {

if (event.getState() == Event.KeeperState.SyncConnected) {

latch.countDown(); }

}

}); latch.await();

} catch (IOException | InterruptedException e) { LOGGER.error("", e);

## } return zk;

}

privatevoid watchNode(final ZooKeeper zk) { try {

List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() { @Override publicvoid process(WatchedEvent event) {

if (event.getType() == Event.EventType.NodeChildrenChanged) {

watchNode(zk); }

}

}); List<String> dataList = new ArrayList<>(); for (String node : nodeList) {

byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null); dataList.add(newString(bytes));

} LOGGER.debug("node data: {}", dataList); this.dataList = dataList;

} catch (KeeperException | InterruptedException e) {

LOGGER.error("", e); }

} }

# 第九步：实现 RPC代理

这⾥使⽤ Java 提供的动态代理技术实现 RPC 代理（当然也可以使⽤ CGLib 来实现），具体代码如下：

public class RpcProxy {

private String serverAddress; private ServiceDiscovery serviceDiscovery;

public RpcProxy(String serverAddress) {

this.serverAddress = serverAddress; }

public RpcProxy(ServiceDiscovery serviceDiscovery) {

this.serviceDiscovery = serviceDiscovery; }

@SuppressWarnings("unchecked") public <T> T create(Class<?> interfaceClass) {

return (T) Proxy.newProxyInstance( interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {

@Override public Object invoke(Object proxy, Methodmethod, Object[] args) throws

Throwable {

RpcRequest request = new RpcRequest(); // 创建并初始化 RPC 请求 request.setRequestId(UUID.randomUUID().toString()); request.setClassName(method.getDeclaringClass().getName()); request.setMethodName(method.getName()); request.setParameterTypes(method.getParameterTypes()); request.setParameters(args);

if (serviceDiscovery != null) {

serverAddress = serviceDiscovery.discover(); // 发现服务 }

String[] array = serverAddress.split(":"); String host = array[0]; int port = Integer.parseInt(array[1]);

RpcClient client = new RpcClient(host, port); // 初始化 RPC 客户端 RpcResponse response = client.send(request); // 通过 RPC 客户端发送 RPC 请求并

获取 RPC 响应

if (response.isError()) {

throw response.getError(); } else {

return response.getResult(); }

} }

); }

}

使⽤RpcClient类实现 RPC 客户端，只需扩展 Nety 提供的SimpleChannelInboundHandler抽象类即可，代码如下：

publicclassRpcClientextendsSimpleChannelInboundHandler<RpcResponse> {

privatestaticfinal Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

private String host; private int port;

private RpcResponse response;

privatefinal Object obj = new Object();

public RpcClient(String host, int port) {

this.host = host; this.port = port;

}

@Override public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception

{

this.response = response;

## synchronized (obj) {

obj.notifyAll(); // 收 到 响 应 ， 唤 醒 线 程 }

}

@Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

LOGGER.error("client caught exception", cause); ctx.close();

}

public RpcResponse send(RpcRequest request) throws Exception { EventLoopGroup group = new NioEventLoopGroup(); try {

Bootstrap bootstrap = new Bootstrap(); bootstrap.group(group).channel(NioSocketChannel.class)

.handler(new ChannelInitializer<SocketChannel>() { @Override public void initChannel(SocketChannel channel) throws Exception {

channel.pipeline()

.addLast(new RpcEncoder(RpcRequest.class)) // 将 RPC 请 求 进 ⾏ 编 码 （ 为 了 发 送 请 求 ）

.addLast(new RpcDecoder(RpcResponse.class)) // 将 RPC 响 应 进 ⾏ 解 码 （ 为 了 处 理 响 应 ）

.addLast(RpcClient.this); // 使 ⽤ RpcClient 发 送 RPC 请 求 }

})

.option(ChannelOption.SO_KEEPALIVE, true);

ChannelFuture future = bootstrap.connect(host, port).sync(); future.channel().writeAndFlush(request).sync();

## synchronized (obj) {

obj.wait(); // 未 收 到 响 应 ， 使 线 程等 待 }

if (response != null) {

future.channel().closeFuture().sync();

} return response;

## } finally {

group.shutdownGracefully(); }

} }

# 第⼗步：发送 RPC请求

使⽤ JUnit 结合 Spring 编写⼀个单元测试，代码如下：

<!-- lang: java --> @RunWith(SpringJUnit4ClassRunner.class) @ContextConfiguration(locations = "classpath:spring.xml") public class HelloServiceTest {

@Autowired private RpcProxy rpcProxy;

@Test public void helloTest() {

HelloService helloService = rpcProxy.create(HelloService.class); Stringresult = helloService.hello("World"); Assert.assertEquals("Hello! World", result);

} }

运⾏以上单元测试，如果不出意外的话，您应该会看到绿条。

# 总结

本⽂通过 Spring + Nety + Protostuf + ZoKeper 实现了⼀个轻量级 RPC 框架，使⽤ Spring 提供依赖注⼊与参数配置，使⽤ Nety 实现 NIO ⽅式的数据传输，使⽤ Protostuf 实现对象序列化，使⽤ ZoKeper 实现服务注册与发现。使⽤该框架，可将 服务部署到分布式环境中的任意节点上，客户端通过远程接⼝来调⽤服务端的具体实现，让服务端与客户端的开发完全分离，为实 现⼤规模分布式应⽤提供了基础⽀持。

# 附录：Maven依赖

<!-- lang: xml --><!-- JUnit --><dependency><groupId>junit</groupId> <artifactId>junit</artifactId><version>4.11</version><scope>test</scope></dependency><!-- SLF4J

--><dependency><groupId>org.slf4j</groupId><artifactId>slf4j-log4j12</artifactId> <version>1.7.7</version></dependency><!-- Spring --><dependency> <groupId>org.springframework</groupId><artifactId>spring-context</artifactId> <version>3.2.12.RELEASE</version></dependency><dependency> <groupId>org.springframework</groupId><artifactId>spring-test</artifactId> <version>3.2.12.RELEASE</version><scope>test</scope></dependency><!-- Netty --><dependency> <groupId>io.netty</groupId><artifactId>netty-all</artifactId><version>4.0.24.Final</version> </dependency><!-- Protostuff --><dependency><groupId>com.dyuproject.protostuff</groupId> <artifactId>protostuff-core</artifactId><version>1.0.8</version></dependency><dependency> <groupId>com.dyuproject.protostuff</groupId><artifactId>protostuff-runtime</artifactId> <version>1.0.8</version></dependency><!-- ZooKeeper --><dependency> <groupId>org.apache.zookeeper</groupId><artifactId>zookeeper</artifactId>

- <version>3.4.6</version></dependency><!-- Apache Commons Collections --><dependency> <groupId>org.apache.commons</groupId><artifactId>commons-collections4</artifactId>

- <version>4.0</version></dependency><!-- Objenesis --><dependency> <groupId>org.objenesis</groupId><artifactId>objenesis</artifactId><version>2.1</version> </dependency><!-- CGLib --><dependency><groupId>cglib</groupId><artifactId>cglib</artifactId> <version>3.1</version></dependency>


源码地址：htp:/ w.oschina.net/code/sni pet_23750_45050
