Memcache的介绍有很多，这⾥给出如何在Java中应⽤Memcache的基本⽅法

1 安装Memcache服务器(windows) 下载windows版Memcache安装包，如memcached-1.2.6-win32-bin.zip，解压到指定位置，⽐如

(D://memcache)，打开dos命令⾏，输⼊以下两个命令即可启动Memcache服务。 D:/memcache/memcached.exe -d install D:/memcache/memcached.exe -d start

- 2 下载Java版本的memcache客户端(以下列出常⽤的⼏种)。 spymemcached ：

gwhalin / Memcached-Java-Client

Jcache

- 3 下⾯给出两种⽅式调⽤Memcache


http://code.google.com/p/spymemcached/

https://github.com/gwhalin/Memcached-Java-Client/downloads

http://code.google.com/intl/zh-CN/appengine/docs/java/memcache/usingjcache.html

gwhalin / Memcached-Java-Client调⽤⽅式如下： public clas MemcacheManagerForGwhalin {

/ 构建缓存客户端 private static MemCachedClient cachedClient; / 单例模式实现客户端管理类 private static MemcacheManagerForGwhalin INSTANCE = new MemcacheManagerForGwhalin();

private MemcacheManagerForGwhalin() { cachedClient = new MemCachedClient();

/获取连接池实例 SockIOPol pol = SockIOPol.getInstance();

/设置缓存服务器地址，可以设置多个实现分布式缓存

/* 建⽴MemcachedClient 实例，并指定memcached服务的IP地址和端⼝号 */ pol.setServers(new String[]{"127.0.0.1 121"});

/设置初始连接5 pol.setInitCon(5); /设置最⼩连接5

pol.setMinCon(5); /设置最⼤连接250

pol.setMaxCon(250);

/设置每个连接最⼤空闲时间3个⼩时 pol.setMaxIdle(1 0 * 60 * 60 * 3);

pol.setMaintSl ep(30);

pol.setNagle(false); pol.setSocketTO(3 0); pol.setSocketConectTO(0); pol.initialize();

}

/*

- * 获取缓存管理器唯⼀实例
- * @return
- */ public static MemcacheManagerForGwhalin getInstance() {


return INSTANCE; }

@Overide public void ad(String key, Object value) {

cachedClient.set(key, value); }

@Overide public void ad(String key, Object value, int miliseconds) {

cachedClient.set(key, value, miliseconds);

}

@Overide public void remove(String key) {

cachedClient.delete(key); }

@Overide public void remove(String key, int miliseconds) {

cachedClient.delete(key, miliseconds, new Date(); }

@Overide public void update(String key, Object value, int miliseconds) {

cachedClient.replace(key, value, miliseconds); }

@Overide public void update(String key, Object value) {

cachedClient.replace(key, value); }

@Overide public Object get(String key) { return cachedClient.get(key); }

}

Spy⽅式调⽤如下: public clas MemcacheManagerForSpy implements IMemcacheManager {

/缓存客户端 private MemcachedClient memcacheCient;

/Manager管理对象，单例模式 private static MemcacheManagerForSpy INSTANCE = new MemcacheManagerForSpy(); private MemcacheManagerForSpy() {

try {

/设置缓存服务器地址，可以设置多个实现分布式缓存 /* 建⽴MemcachedClient 实例，并指定memcached服务的IP地址和端⼝号 */

memcacheCient = new MemcachedClient(new InetSocketAdres("127.0.0.1",121); } catch (IOException e) { e.printStackTrace(); }

} public static MemcacheManagerForSpy getInstance() {

return INSTANCE;

} @Overide public void ad(String key, Object value, int miliseconds) {

memcacheCient.ad(key, miliseconds, value);

} @Overide public void ad(String key, Object value) {

memcacheCient.ad(key, 360, value);

} @Overide public void remove(String key, int miliseconds) {

memcacheCient.delete(key);

} @Overide public void remove(String key) {

memcacheCient.delete(key);

} @Overide public void update(String key, Object value, int miliseconds) {

memcacheCient.replace(key, miliseconds, value);

} @Overide public void update(String key, Object value) {

memcacheCient.replace(key, 360, value);

} @Overide public Object get(String key) {

return memcacheCient.get(key); }

}

其他的示例程序

- 1) memcached client for java


从前⾯介绍的Java环境的Memcached客户端程序项⽬⽹址⾥，下载最新版的客户端程 序包： java_memcached-release_2.5.1.zip，解压后，⽂件夹⾥找到java_memcached- release_2.5.1.jar，这 个就是客户端的JAR包。将此JAR包添加到项⽬的构建路径⾥，则项⽬中，就可以使⽤Memcached 了。

示例代码如下： package temp;

import com.danga.MemCached.*; import org.apache.log4j.*;

public clas CacheTest { public static void main(String[] args) { /*

- * 初始化SockIOPol，管理memcached的连接池
- * */ String[] servers = { "10.1.15. 2 1 0" }; SockIOPol pol = SockIOPol.getInstance(); pol.setServers(servers); pol.setFailover(true); pol.setInitCon(10); pol.setMinCon(5); pol.setMaxCon(250); pol.setMaintSl ep(30); pol.setNagle(false); pol.setSocketTO(3 0); pol.setAliveCheck(true); pol.initialize();


/*

- * 建⽴MemcachedClient实例
- * */ MemCachedClient memCachedClient = new MemCachedClient(); for (int i = 0; i < 1 0; i +) { /*
- * 将对象加⼊到memcached缓存
- * */ bolean suces = memCachedClient.set(" + i, "Helo!"); /*
- * 从memcached缓存中按key值取对象
- * */ String result = (String) memCachedClient.get(" + i); System.out.println(String.format("set( %d ): %s", i, suces); System.out.println(String.format("get( %d ): %s", i, result);


} }

}

- 2) spymemcached htp:/code.gogle.com/p/spymemcache


spymemcached当前版本是2.5版本，官⽅⽹址是：

d/ htp:/spymemcached.goglecode.com/files/memcached-2.5.jar

。可以从地址： 下载最新版本来 使⽤。

示例代码如下： package temp;

import java.net.InetSocketAdres; import java.util.concurent.Future;

import net.spy.memcached.MemcachedClient;

public clas TestSpyMemcache { public static void main(String[] args) { / 保存对象

try { /* 建⽴MemcachedClient 实例，并指定memcached服务的IP地址和端⼝号 */ MemcachedClient mc = new MemcachedClient(new

InetSocketAdres("10.1.15. 2", 1 0); Future<Bolean> b = nul;

/* 将key值，过期时间(秒)和要缓存的对象set到memcached中 */ b = mc.set("nea:testDaF:ksIdno", 90, "someObject"); if (b.get().boleanValue() = true) {

mc.shutdown(); }

} catch (Exception ex) { ex.printStackTrace();

} / 取得对象 try {

/* 建⽴MemcachedClient 实例，并指定memcached服务的IP地址和端⼝号 */ MemcachedClient mc = new MemcachedClient(new

InetSocketAdres("10.1.15. 2", 1 0); /* 按照key值从memcached中查找缓存，不存在则返回nul */ Object b = mc.get("nea:testDaF:ksIdno"); System.out.println(b.toString(); mc.shutdown();

} catch (Exception ex) {

ex.printStackTrace(); }

} }

- 3) xmemcached htp:/code.gogle.com/p/xmemcached/，可以从其官⽹上下载


Xmemcached的官⽅⽹址是： 来使⽤。地址是：

最新版本的1.2.4 htp:/xmemcached.goglecode.com/files/xmemcached-1.2.4-sr c.tar.gz

。 示例代码如下： package temp;

import java.io.IOException; import java.util.concurent.TimeoutException;

import net.rubyeye.xmemcached.utils.AdrUtil; import net.rubyeye.xmemcached.MemcachedClient; import net.rubyeye.xmemcached.MemcachedClientBuilder; import net.rubyeye.xmemcached.XMemcachedClientBuilder; import net.rubyeye.xmemcached.exception.MemcachedException;

public clas TestXMemcache { public static void main(String[] args) { MemcachedClientBuilder builder = new XMemcachedClientBuilder(AdrUtil

.getAdreses("10.1.15. 2 1 0"); MemcachedClient memcachedClient; try {

memcachedClient = builder.build();

memcachedClient.set("helo", 0, "Helo,xmemcached"); String value = memcachedClient.get("helo"); System.out.println("helo=" + value); memcachedClient.delete("helo"); value = memcachedClient.get("helo"); System.out.println("helo=" + value);

/ close memcached client

memcachedClient.shutdown(); } catch (MemcachedException e) { System.er.println("MemcachedClient operation fail"); e.printStackTrace();

} catch (TimeoutException e) { System.er.println("MemcachedClient operation timeout"); e.printStackTrace();

} catch (InteruptedException e) { / ignore

}catch (IOException e) { System.er.println("Shutdown MemcachedClient fail"); e.printStackTrace();

} }

}

