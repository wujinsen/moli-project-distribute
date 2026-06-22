htp:/jimgreat.iteye.com/blog/158671

由于项⽬中使⽤Redis，所以使⽤它的Java客户端Jedis也有⼤半年的时间（后续会分享经验）。

最近看了⼀下源码，源码清晰、流畅、简洁，学到了不少东⻄，在此分享⼀下。

https://github.com/xetorthio/jedis

（源码地址： ）

协议

和Redis Server通信的协议规则都在redis.clients.jedis.Protocol这个类中，主要是通过对RedisInputStream和 RedisOutputStream对读写操作来完成。

命令的发送都是通过redis.clients.jedis.Protocol的sendCommand来完成的，就是对RedisOutputStream写⼊字节流

Java代码

![image 1](<redis源码分析.note_images/imageFile1.png>)

<span style="font-size: small;"><span style="fontsize: small;"> private void sendCommand(final RedisOutputStream os, final byte[] command ,

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


final byte[]... args) {

try { os.write(ASTERISK_BYTE); os.writeIntCrLf(args.length + 1); os.write(DOLLAR_BYTE); os.writeIntCrLf(command.length); os.write(command); os.writeCrLf();

for (final byte[] arg : args) { os.write(DOLLAR_BYTE); os.writeIntCrLf(arg.length); os.write(arg); os.writeCrLf();

} } catch (IOException e) {

throw new JedisConnectionException(e); }

}</span></span>

从这⾥可以看出redis的命令格式

[*号][消息元素个数]\r\n ( 消息元素个数 = 参数个数 + 1个命令)

[$号][命令字节个数]\r\n

[命令内容]\r\n

[$号][参数字节个数]\r\n

[参数内容]\r\n

[$号][参数字节个数]\r\n

[参数内容]\r\n

返回的数据是通过读取RedisInputStream 进⾏解析处理后得到的

Java代码

![image 2](<redis源码分析.note_images/imageFile2.png>)

private Object process(final RedisInputStream is) {

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


try { byte b = is.readByte(); if (b == MINUS_BYTE) {

processError(is); } else if (b == ASTERISK_BYTE) {

return processMultiBulkReply(is); } else if (b == COLON_BYTE) {

return processInteger(is); } else if (b == DOLLAR_BYTE) {

return processBulkReply(is); } else if (b == PLUS_BYTE) {

return processStatusCodeReply(is); } else {

throw new JedisConnectionException("Unknown reply: " + (char) b); }

} catch (IOException e) {

throw new JedisConnectionException(e);

# } return null;

}

通过返回数据的第⼀个字节来判断返回的数据类型，调⽤不同的处理函数

[-号] 错误信息

[*号] 多个数据 结构和发送命令的结构⼀样

[:号] ⼀个整数

[$号] ⼀个数据 结构和发送命令的结构⼀样

[+号] ⼀个状态码

连接

和Redis Sever的Socket通信是由 redis.clients.jedis.Connection 实现的

Connection 中维护了⼀个底层Socket连接和⾃⼰的I/O Stream 还有Protocol

I/O Stream是在Connection中Socket建⽴连接后获取并在使⽤时传给Protocol的

Connection还实现了各种返回消息由byte转为String的操作

Java代码

![image 3](<redis源码分析.note_images/imageFile3.png>)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


private String host; private int port = Protocol.DEFAULT_PORT; private Socket socket; private Protocol protocol = new Protocol(); private RedisOutputStream outputStream; private RedisInputStream inputStream; private int pipelinedCommands = 0; private int timeout = Protocol.DEFAULT_TIMEOUT;

Java代码

![image 4](<redis源码分析.note_images/imageFile4.png>)

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


public void connect() { if (!isConnected()) {

try { socket = new Socket(); socket.connect(new InetSocketAddress(host, port), timeout); socket.setSoTimeout(timeout); outputStream = new RedisOutputStream(socket.getOutputStream()); inputStream = new RedisInputStream(socket.getInputStream());

} catch (IOException ex) {

throw new JedisConnectionException(ex); }

} }

可以看到，就是⼀个基本的Socket

这⾥分享个经验，timeout这个参数默认是2000，我做的项⽬中有部分是离线运算的，如果读取⽐ 较⼤的数据(⼤Set ⼤List之类的)有可能会超过这个时间，可以在JedisPool的构造参数中增⼤这个 值。在线服务⼀般不要修改。

原⽣客户端

redis.clients.jedis.BinaryClient 继承 Connection, 封装了Redis的所有命令( )

http://redis.io/command s

从名⼦可以看出 BinaryClient 是Redis客户端的⼆进制版本，参数都是byte[]的

BinaryClient 是通过Connection的sendCommand 调⽤Protocol的sendCommand 向Redis Server发 送命令

Java代码

![image 5](<redis源码分析.note_images/imageFile5.png>)

- 1.
- 2.
- 3.


# public void get(final byte[] key) {

sendCommand(Command.GET, key); }

redis.clients.jedis.Client可以看成是BinaryClient 的⾼级版本，函数的参数都是String int long 这类 的，并由redis.clients.util.SafeEncoder 转成byte后 再调⽤BinaryClient 对应的函数

Java代码

![image 6](<redis源码分析.note_images/imageFile6.png>)

- 1.
- 2.
- 3.


public void get(final String key) {

get(SafeEncoder.encode(key)); }

这⼆个client只完成了发送命令的封装，并没有处理返回数据

Jedis客户端

我们平时⽤的基本都是由redis.clients.jedis.Jedis类封装的客户端

Jedis是通过对Client的调⽤， 完成命令发送和返回数据 这个完整过程的

以GET命令为例，其它命令类似

Jedis中的get函数如下

Java代码

![image 7](<redis源码分析.note_images/imageFile7.png>)

- 1.
- 2.
- 3.
- 4.
- 5.


public String get(final String key) { checkIsInMulti(); client.sendCommand(Protocol.Command.GET, key); return client.getBulkReply();

}

checkIsInMulti();

是进⾏⽆事务检查 Jedis不能进⾏有事务的操作 带事务的连接要⽤redis.clients.jedis.Transaction 类

client.sendCommand(Protocol.Command.GET, key);

调⽤Client发送命令

return client.getBulkReply();

处理返回值

分析到这⾥ ⼀个Jedis客户端的基本实现原理应该很清楚了

连接池

在实现项⽬中，要使⽤连接池来管理Jedis的⽣命周期，满⾜多线程的需求，并对资源合理使⽤。

jedis有两个连接池类型， ⼀个是管理 Jedis， ⼀个是管理ShardedJedis（jedis通过java实现的 多 Redis实例的⾃动分⽚功能，后⾯会分析）

![image 8](<redis源码分析.note_images/imageFile8.png>)

他们都是Pool<T>的不同实现

Java代码

![image 9](<redis源码分析.note_images/imageFile9.png>)

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


public abstract class Pool<T> { private final GenericObjectPool internalPool;

public Pool(final GenericObjectPool.Config poolConfig,

PoolableObjectFactory factory) {

this.internalPool = new GenericObjectPool(factory, poolConfig); }

@SuppressWarnings("unchecked") public T getResource() {

# try {

return (T) internalPool.borrowObject(); } catch (Exception e) {

throw new JedisConnectionException(

"Could not get a resource from the pool", e); }

}

...... ......

从代码中可以看出，Pool<T>是通过 Apache Commons Pool 中的GenericObjectPool这个对象池 来实现的

（Apache Commons Pool内容可参考 ）

http://phil-xzh.iteye.com/blog/320983

在JedisPool中，实现了⼀个符合 Apache Commons Pool 相应接⼝的JedisFactory， GenericObjectPool就是通过这个JedisFactory来产⽣Jedis对你的

其实JedisPoolConfig也是对Apache Commons Pool 中的Config进⾏的⼀个封装

当你在调⽤ getResource 获取Jedis时， 实际上是Pool<T>内部的internalPool调⽤borrowObject() 借给你了⼀个实例

⽽internalPool 这个 GenericObjectPool ⼜调⽤了 JedisFactory 的 makeObject() 来完成实例的⽣成 (在Pool中资源不够的时候)

Java代码

![image 10](<redis源码分析.note_images/imageFile10.png>)

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


public Object makeObject() throws Exception {

final Jedis jedis; if (timeout > 0) {

jedis = new Jedis(this.host, this.port, this.timeout); } else {

jedis = new Jedis(this.host, this.port); }

jedis.connect(); if (null != this.password) {

jedis.auth(this.password);

} return jedis;

}

客户端的⾃动分⽚

![image 11](<redis源码分析.note_images/imageFile11.png>)

从这个结构图上可以看出 ShardedJedis 和 BinaryShardedJedis 正好是 Jedis 和 BinaryJedis 的 分⽚版本

其实它们都是 先获取hash(key)后对应的 Jedis 再有这个Jedis进⾏操作

Java代码

![image 12](<redis源码分析.note_images/imageFile12.png>)

- 1.
- 2.
- 3.
- 4.


public String get(String key) { Jedis j = getShard(key); return j.get(key);

}

分⽚逻辑都是在 Sharded<R, S extends ShardInfo<R>> 中实现的

它的构造函数如下

Java代码

![image 13](<redis源码分析.note_images/imageFile13.png>)

- 1.
- 2.
- 3.
- 4.
- 5.


public Sharded(List<S> shards, Hashing algo, Pattern tagPattern) { this.algo = algo; this.tagPattern = tagPattern; initialize(shards);

}

shards是⼀组ShardInfo, 具体实现是JedisShardInfo, 每个⾥⾯记录分⽚信息和权重，并负责完成 分⽚对应Jedis实例创建

Sharded的初始化和⼀致性哈希（Consistent Hashing）的思想是⼀样的，但这个并不能实现节点 的动态变更，只能体现出节点的 权重分配

Java代码

![image 14](<redis源码分析.note_images/imageFile14.png>)

1.

nodes = new TreeMap<Long, S>();

这个nodes就是⼀个虚拟的结点分布环，由TreeMap实现，保证按Key有序，Value就是对应的 ShardInfo

Java代码

![image 15](<redis源码分析.note_images/imageFile15.png>)

1.

160 * shardInfo.getWeight()

根据每个shard的weight值，默认是1，⽣成160倍的虚拟节点，hash后放到nodes中，也就是分布 到环上

Java代码

![image 16](<redis源码分析.note_images/imageFile16.png>)

1.

resources.put(shardInfo, shardInfo.createResource());

每个shardInfo对应的jedis，也就是真正的操作节点，放到resources中

Java代码

![image 17](<redis源码分析.note_images/imageFile17.png>)

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


private void initialize(List<S> shards) {

nodes = new TreeMap<Long, S>();

for (int i = 0; i != shards.size(); ++i) { final S shardInfo = shards.get(i); if (shardInfo.getName() == null)

for (int n = 0; n < 160 * shardInfo.getWeight(); n++) {

nodes.put(this.algo.hash("SHARD-" + i + "-NODE-" + n), shardInfo); }

# else

for (int n = 0; n < 160 * shardInfo.getWeight(); n++) {

nodes.put(this.algo.hash(shardInfo.getName() + "*" + shardInfo.getWeight()

+ n), shardInfo); }

resources.put(shardInfo, shardInfo.createResource()); }

}

通过Key获取对应的jedis时，先对key进⾏hash，和前⾯初始化节点环时，使⽤相同的算法

再从nodes这个虚拟的环中取出 ⼤于等于 这个hash值的第⼀个节点（shardinfo），没有就取 nodes中第⼀个节点（所谓的环 其实是逻辑上实现的）

最后从resources中取出jedis来

Java代码

![image 18](<redis源码分析.note_images/imageFile18.png>)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


public S getShardInfo(byte[] key) { SortedMap<Long, S> tail = nodes.tailMap(algo.hash(key)); if (tail.size() == 0) {

return nodes.get(nodes.firstKey());

} return tail.get(tail.firstKey());

}

# Java代码

![image 19](<redis源码分析.note_images/imageFile19.png>)

- 1.
- 2.
- 3.


public R getShard(String key) {

return resources.get(getShardInfo(key)); }

