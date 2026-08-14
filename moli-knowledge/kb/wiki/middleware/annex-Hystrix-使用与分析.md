---
title: Hystrix 使用与分析.note（原文插图 annex）
slug: annex-Hystrix-使用与分析
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/Hystrix 使用与分析.note.md
related: [dubbo-调用原理与分层]
created: 2026-07-05
updated: 2026-07-05
---

# ⼀:为什么需要Hystrix?

在⼤中型分布式系统中，通常系统很多依赖(HTTP,hession,Netty,Dubbo等)，如下图:

![image 1](assets/imageFile1.png)

在⾼并发访问下,这些依赖的稳定性与否对系统的影响⾮常⼤,但是依赖有很多不可控问题:如⽹络连接缓 慢，资源繁忙，暂时不可⽤，服务脱机等. 如下图：QPS为50的依赖 I 出现不可⽤，但是其他依赖仍然可⽤.

![image 2](assets/imageFile2.png)

### 当依赖I 阻塞时,⼤多数服务器的线程池就出现阻塞(BLOCK),影响整个线上服务的稳定性.如下图:

![image 3](assets/imageFile3.png)

在复杂的分布式架构的应⽤程序有很多的依赖，都会不可避免地在某些时候失败。⾼并发的依赖失败 时如果没有隔离措施，当前应⽤服务就有被拖垮的⻛险。

Java代码

![image 4](assets/imageFile4.png)

收藏代码 例如:⼀个依赖30个SOA服务的系统,每个服务99.99%可⽤。 99.99%的30次⽅ ≈ 99.7% 0.3% 意味着⼀亿次请求 会有 3,000,00次失败 换算成时间⼤约每⽉有2个⼩时服务不稳定. 随着服务依赖数量的变多，服务不稳定的概率会成指数性提⾼.

- 1.
- 2.
- 3.
- 4.
- 5.


解决问题⽅案:对依赖做隔离,Hystrix就是处理依赖隔离的框架,同时也是可以帮我们做依赖服务的治理 和监控.

Netflix 公司开发并成功使⽤Hystrix,使⽤规模如下:

Java代码

![image 5](assets/imageFile5.png)

收藏代码 The Netflix API processes 10+ billion HystrixCommand executions per day using thread isolation.

- 1.
- 2.


Each API instance has 40+ thread-pools with 5-20 threads in each (most are set to 10).

# ⼆:Hystrix如何解决依赖隔离

- 1:Hystrix使⽤命令模式HystrixCommand(Command)包装依赖调⽤逻辑，每个命令在单独线程中/信号 授权下执⾏。

- 2:可配置依赖调⽤超时时间,超时时间⼀般设为⽐99.5%平均时间略⾼即可.当调⽤超时时，直接返回或 执⾏fallback逻辑。

- 3:为每个依赖提供⼀个⼩的线程池（或信号），如果线程池已满调⽤将被⽴即拒绝，默认不采⽤排队. 加速失败判定时间。

- 4:依赖调⽤结果分:成功，失败（抛出异常），超时，线程拒绝，短路。 请求失败(异常，拒绝，超时， 短路)时执⾏fallback(降级)逻辑。

- 5:提供熔断器组件,可以⾃动运⾏或⼿动调⽤,停⽌当前依赖⼀段时间(10秒)，熔断器默认错误率阈值为 50%,超过将⾃动运⾏。

- 6:提供近实时依赖的统计和监控 Hystrix依赖的隔离架构,如下图:


![image 6](assets/imageFile6.png)

# 三:如何使⽤Hystrix

## 1:使⽤maven引⼊Hystrix依赖

Html代码

![image 7](assets/imageFile7.png)

收藏代码 <!-- 依赖版本 --> <hystrix.version>1.3.16</hystrix.version> <hystrix-metrics-event-stream.version>1.1.2</hystrix-metrics-event-stream.version>

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


<dependency> <groupId>com.netflix.hystrix</groupId> <artifactId>hystrix-core</artifactId> <version>${hystrix.version}</version>

</dependency> <dependency> <groupId>com.netflix.hystrix</groupId> <artifactId>hystrix-metrics-event-stream</artifactId> <version>${hystrix-metrics-event-stream.version}</version>

</dependency> <!-- 仓库地址 --> <repository>

<id>nexus</id> <name>local private nexus</name> <url>http://maven.oschina.net/content/groups/public/</url> <releases>

<enabled>true</enabled>

</releases> <snapshots>

<enabled>false</enabled> </snapshots>

</repository>

- 2:使⽤命令模式封装依赖逻辑


Java代码

![image 8](assets/imageFile8.png)

收藏代码

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


public class HelloWorldCommand extends HystrixCommand<String> { private final String name; public HelloWorldCommand(String name) {

//最少配置:指定命令组名(CommandGroup) super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup")); this.name = name;

} @Override protected String run() {

// 依赖逻辑封装在run()⽅法中 return "Hello " + name +" thread:" + Thread.currentThread().getName();

} //调⽤实例 public static void main(String[] args) throws Exception{

//每个Command对象只能调⽤⼀次,不可以重复调⽤, //重复调⽤对应异常信

息:This instance can only be executed once. Please instantiate a new instance. HelloWorldCommand helloWorldCommand = new HelloWorldCommand("Synchronous-hystrix"); //使⽤execute()同步调⽤代码,效果等同于:helloWorldCommand.queue().get(); String result = helloWorldCommand.execute(); System.out.println("result=" + result);

helloWorldCommand = new HelloWorldCommand("Asynchronous-hystrix"); //异步调⽤,可⾃由控制获取结果时机, Future<String> future = helloWorldCommand.queue();

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


//get操作不能超过command定义的超时时间,默认:1秒 result = future.get(100, TimeUnit.MILLISECONDS); System.out.println("result=" + result); System.out.println("mainThread=" + Thread.currentThread().getName());

}

}

//运⾏结果: run()⽅法在不同的线程下执⾏ // result=Hello Synchronous-hystrix thread:hystrix-HelloWorldGroup-1 // result=Hello Asynchronous-hystrix thread:hystrix-HelloWorldGroup-2 // mainThread=main

note:异步调⽤使⽤ command.queue()get(timeout, TimeUnit.MILLISECONDS);同步调⽤使⽤ command.execute() 等同于 command.queue().get();

- 3:注册异步事件回调执⾏


Java代码

![image 9](assets/imageFile9.png)

收藏代码 //注册观察者事件拦截 Observable<String> fs = new HelloWorldCommand("World").observe(); //注册结果回调事件 fs.subscribe(new Action1<String>() {

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


@Override public void call(String result) {

//执⾏结果处理,result 为HelloWorldCommand返回的结果 //⽤户对结果做⼆次处理.

}

}); //注册完整执⾏⽣命周期事件 fs.subscribe(new Observer<String>() {

@Override public void onCompleted() {

// onNext/onError完成之后最后回调 System.out.println("execute onCompleted");

} @Override public void onError(Throwable e) {

// 当产⽣异常时回调 System.out.println("onError " + e.getMessage()); e.printStackTrace();

} @Override public void onNext(String v) {

// 获取结果后回调 System.out.println("onNext: " + v);

}

}); /* 运⾏结果 call execute result=Hello observe-hystrix thread:hystrix-HelloWorldGroup-3 onNext: Hello observe-hystrix thread:hystrix-HelloWorldGroup-3 execute onCompleted

*/

- 4:使⽤Fallback() 提供降级策略

Java代码

NOTE: 除了HystrixBadRequestException异常之外，所有从run()⽅法抛出的异常都算作失败，并触发 降级getFallback()和断路器逻辑。

HystrixBadRequestException⽤在⾮法参数或⾮系统故障异常等不应触发回退逻辑的场景。

- 5:依赖命名:CommandKey


![image 10](assets/imageFile10.png)

![image 11](assets/imageFile11.png)

收藏代码 //重载HystrixCommand 的getFallback⽅法实现逻辑 public class HelloWorldCommand extends HystrixCommand<String> {

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


private final String name; public HelloWorldCommand(String name) {

super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("HelloWorldGroup"))

/* 配置依赖超时时间,500毫秒*/

.andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionIso lationThreadTimeoutInMilliseconds(500)));

this.name = name;

} @Override protected String getFallback() {

return "exeucute Falled";

} @Override protected String run() throws Exception {

//sleep 1 秒,调⽤会超时 TimeUnit.MILLISECONDS.sleep(1000); return "Hello " + name +" thread:" + Thread.currentThread().getName();

} public static void main(String[] args) throws Exception{

HelloWorldCommand command = new HelloWorldCommand("test-Fallback"); String result = command.execute();

}

} /* 运⾏结果:getFallback() 调⽤运⾏ getFallback executed

*/

Java代码

![image 12](assets/imageFile12.png)

收藏代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


public HelloWorldCommand(String name) { super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))

/* HystrixCommandKey⼯⼚定义依赖名称 */

.andCommandKey(HystrixCommandKey.Factory.asKey("HelloWorld"))); this.name = name;

}

NOTE: 每个CommandKey代表⼀个依赖抽象,相同的依赖要使⽤相同的CommandKey名称。依赖隔离的 根本就是对相同CommandKey的依赖做隔离.

- 6:依赖分组:CommandGroup 命令分组⽤于对依赖操作分组,便于统计,汇总等. Java代码

NOTE: CommandGroup是每个命令最少配置的必选参数，在不指定ThreadPoolKey的情况下，字⾯值 ⽤于对不同依赖的线程池/信号区分.

- 7:线程池/信号:ThreadPoolKey Java代码

NOTE: 当对同⼀业务依赖做隔离时使⽤CommandGroup做区分,但是对同⼀依赖的不同远程调⽤如(⼀ 个是redis ⼀个是http),可以使⽤HystrixThreadPoolKey做隔离区分.

最然在业务上都是相同的组，但是需要在资源上做隔离时，可以使⽤HystrixThreadPoolKey区 分.

- 8:请求缓存 Request-Cache Java代码


![image 13](assets/imageFile13.png)

收藏代码 //使⽤HystrixCommandGroupKey⼯⼚定义 public HelloWorldCommand(String name) {

- 1.
- 2.
- 3.
- 4.


Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("HelloWorldGroup")) }

![image 14](assets/imageFile14.png)

收藏代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


public HelloWorldCommand(String name) { super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))

.andCommandKey(HystrixCommandKey.Factory.asKey("HelloWorld")) /* 使⽤HystrixThreadPoolKey⼯⼚定义线程池名称*/

.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("HelloWorldPool"))); this.name = name;

}

![image 15](assets/imageFile15.png)

收藏代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


public class RequestCacheCommand extends HystrixCommand<String> { private final int id; public RequestCacheCommand( int id) {

super(HystrixCommandGroupKey.Factory.asKey("RequestCacheCommand")); this.id = id;

} @Override protected String run() throws Exception {

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


System.out.println(Thread.currentThread().getName() + " execute id=" + id); return "executed=" + id;

} //重写getCacheKey⽅法,实现区分不同请求的逻辑 @Override protected String getCacheKey() {

return String.valueOf(id); }

public static void main(String[] args){ HystrixRequestContext context = HystrixRequestContext.initializeContext(); try {

- RequestCacheCommand command2a = new RequestCacheCommand(2);

- RequestCacheCommand command2b = new RequestCacheCommand(2);


- Assert.assertTrue(command2a.execute()); //isResponseFromCache判定是否是在缓存中获取结果 Assert.assertFalse(command2a.isResponseFromCache());

- Assert.assertTrue(command2b.execute()); Assert.assertTrue(command2b.isResponseFromCache());


} finally { context.shutdown();

} context = HystrixRequestContext.initializeContext(); try {

RequestCacheCommand command3b = new RequestCacheCommand(2); Assert.assertTrue(command3b.execute()); Assert.assertFalse(command3b.isResponseFromCache());

#### } finally {

context.shutdown(); }

} }

NOTE:请求缓存可以让(CommandKey/CommandGroup)相同的情况下,直接共享结果，降低依赖调⽤次 数，在⾼并发和CacheKey碰撞率⾼场景下可以提升性能. Servlet容器中，可以直接实⽤Filter机制Hystrix请求上下⽂

Java代码

![image 16](assets/imageFile16.png)

收藏代码

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


public class HystrixRequestContextServletFilter implements Filter { public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)

throws IOException, ServletException { HystrixRequestContext context = HystrixRequestContext.initializeContext(); try {

chain.doFilter(request, response); } finally {

context.shutdown(); }

}

} <filter>

<display-name>HystrixRequestContextServletFilter</display-name> <filter-name>HystrixRequestContextServletFilter</filter-name>

- 15.
- 16.
- 17.
- 18.
- 19.
- 20.


<filterclass>com.netflix.hystrix.contrib.requestservlet.HystrixRequestContextServletFilter</filterclass>

</filter> <filter-mapping>

<filter-name>HystrixRequestContextServletFilter</filter-name> <url-pattern>/*</url-pattern>

</filter-mapping>

- 9:信号量隔离:SEMAPHORE 隔离本地代码或可快速返回远程调⽤(如memcached,redis)可以直接使⽤信号量隔离,降低线程隔离开

销.

Java代码

- 10:fallback降级逻辑命令嵌套


![image 17](assets/imageFile17.png)

收藏代码

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


public class HelloWorldCommand extends HystrixCommand<String> { private final String name; public HelloWorldCommand(String name) {

super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("HelloWorldGroup"))

/* 配置信号量隔离⽅式,默认采⽤线程池隔离 */

.andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionIso lationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)));

this.name = name;

} @Override protected String run() throws Exception {

return "HystrixThread:" + Thread.currentThread().getName();

} public static void main(String[] args) throws Exception{

HelloWorldCommand command = new HelloWorldCommand("semaphore"); String result = command.execute(); System.out.println(result); System.out.println("MainThread:" + Thread.currentThread().getName());

}

} /** 运⾏结果

HystrixThread:main MainThread:main

*/

![image 18](assets/imageFile18.png)

适⽤场景:⽤于fallback逻辑涉及⽹络访问的情况,如缓存访问。

Java代码

![image 19](assets/imageFile19.png)

收藏代码

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


public class CommandWithFallbackViaNetwork extends HystrixCommand<String> { private final int id;

protected CommandWithFallbackViaNetwork(int id) { super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceX"))

.andCommandKey(HystrixCommandKey.Factory.asKey("GetValueCommand"))); this.id = id;

}

@Override protected String run() {

// RemoteService.getValue(id); throw new RuntimeException("force failure for example");

}

@Override protected String getFallback() {

return new FallbackViaNetwork(id).execute(); }

private static class FallbackViaNetwork extends HystrixCommand<String> { private final int id; public FallbackViaNetwork(int id) {

super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceX"))

.andCommandKey(HystrixCommandKey.Factory.asKey("GetValueFallbackCommand")) // 使⽤不同的线程池做隔离，防⽌上层线程池跑满，影响降级逻辑.

.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("RemoteServiceXFallback ")));

this.id = id;

} @Override protected String run() {

MemCacheClient.getValue(id);

- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.


@Override protected String getFallback() {

#### return null; }

} }

NOTE:依赖调⽤和降级调⽤使⽤不同的线程池做隔离，防⽌上层线程池跑满，影响⼆级降级逻辑调⽤.

- 11:显示调⽤fallback逻辑,⽤于特殊业务处理


![image 20](assets/imageFile20.png)

Java代码

![image 21](assets/imageFile21.png)

收藏代码

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


public class CommandFacadeWithPrimarySecondary extends HystrixCommand<String> { private final static DynamicBooleanProperty usePrimary = DynamicPropertyFactory.getInstance(

).getBooleanProperty("primarySecondary.usePrimary", true); private final int id; public CommandFacadeWithPrimarySecondary(int id) {

super(Setter

.withGroupKey(HystrixCommandGroupKey.Factory.asKey("SystemX"))

.andCommandKey(HystrixCommandKey.Factory.asKey("PrimarySecondaryCommand"))

.andCommandPropertiesDefaults( HystrixCommandProperties.Setter()

.withExecutionIsolationStrategy(ExecutionIsolationStrategy.SEMAP HORE)));

this.id = id;

} @Override protected String run() {

if (usePrimary.get()) {

return new PrimaryCommand(id).execute(); } else {

return new SecondaryCommand(id).execute(); }

} @Override protected String getFallback() {

return "static-fallback-" + id; }

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


protected String getCacheKey() { return String.valueOf(id);

} private static class PrimaryCommand extends HystrixCommand<String> {

#### private final int id; private PrimaryCommand(int id) {

super(Setter

.withGroupKey(HystrixCommandGroupKey.Factory.asKey("SystemX"))

.andCommandKey(HystrixCommandKey.Factory.asKey("PrimaryCommand"))

.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("PrimaryCommand"))

.andCommandPropertiesDefaults( // we default to a 600ms timeout for primary HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds

(600)));

this.id = id;

} @Override protected String run() {

// perform expensive 'primary' service call return "responseFromPrimary-" + id;

}

} private static class SecondaryCommand extends HystrixCommand<String> {

#### private final int id; private SecondaryCommand(int id) {

super(Setter

.withGroupKey(HystrixCommandGroupKey.Factory.asKey("SystemX"))

.andCommandKey(HystrixCommandKey.Factory.asKey("SecondaryCommand"))

.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("SecondaryCommand"))

.andCommandPropertiesDefaults( // we default to a 100ms timeout for secondary HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds

(100)));

this.id = id;

} @Override protected String run() {

// perform fast 'secondary' service call return "responseFromSecondary-" + id;

}

#### } public static class UnitTest {

@Test public void testPrimary() {

HystrixRequestContext context = HystrixRequestContext.initializeContext(); try {

ConfigurationManager.getConfigInstance().setProperty("primarySecondary.usePrimar y", true);

assertEquals("responseFromPrimary20", new CommandFacadeWithPrimarySecondary(20).execute());

} finally { context.shutdown(); ConfigurationManager.getConfigInstance().clear();

}

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


@Test public void testSecondary() {

HystrixRequestContext context = HystrixRequestContext.initializeContext(); try {

ConfigurationManager.getConfigInstance().setProperty("primarySecondary.usePrimar y", false);

assertEquals("responseFromSecondary20", new CommandFacadeWithPrimarySecondary(20).execute());

} finally { context.shutdown(); ConfigurationManager.getConfigInstance().clear();

} }

} }

NOTE:显示调⽤降级适⽤于特殊需求的场景,fallback⽤于业务处理，fallback不再承担降级职责，建议 慎重使⽤，会造成监控统计换乱等问题.

- 12:命令调⽤合并:HystrixCollapser 命令调⽤合并允许多个请求合并到⼀个线程/信号下批量执⾏。 执⾏流程图如下:


![image 22](assets/imageFile22.png)

Java代码

![image 23](assets/imageFile23.png)

收藏代码 public class CommandCollapserGetValueForKey extends HystrixCollapser<List<String>, String, Integ er> {

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


private final Integer key; public CommandCollapserGetValueForKey(Integer key) {

this.key = key;

} @Override public Integer getRequestArgument() {

return key; }

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


protected HystrixCommand<List<String>> createCommand(final Collection<CollapsedRequest<Strin

g, Integer>> requests) { //创建返回command对象 return new BatchCommand(requests);

} @Override protected void mapResponseToRequests(List<String> batchResponse, Collection<CollapsedRequest

<String, Integer>> requests) { int count = 0; for (CollapsedRequest<String, Integer> request : requests) {

//⼿动匹配请求和响应 request.setResponse(batchResponse.get(count++));

}

} private static final class BatchCommand extends HystrixCommand<List<String>> {

private final Collection<CollapsedRequest<String, Integer>> requests; private BatchCommand(Collection<CollapsedRequest<String, Integer>> requests) {

super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))

.andCommandKey(HystrixCommandKey.Factory.asKey("GetValueForKey"))); this.requests = requests;

} @Override protected List<String> run() {

ArrayList<String> response = new ArrayList<String>(); for (CollapsedRequest<String, Integer> request : requests) {

response.add("ValueForKey: " + request.getArgument());

} return response;

}

#### } public static class UnitTest {

HystrixRequestContext context = HystrixRequestContext.initializeContext(); try {

- Future<String> f1 = new CommandCollapserGetValueForKey(1).queue();

- Future<String> f2 = new CommandCollapserGetValueForKey(2).queue();

- Future<String> f3 = new CommandCollapserGetValueForKey(3).queue();

- Future<String> f4 = new CommandCollapserGetValueForKey(4).queue();


- assertEquals("ValueForKey: 1", f1.get());

- assertEquals("ValueForKey: 2", f2.get());

- assertEquals("ValueForKey: 3", f3.get());

- assertEquals("ValueForKey: 4", f4.get()); assertEquals(1, HystrixRequestLog.getCurrentRequest().getExecutedCommands().size());


HystrixCommand<? > command = HystrixRequestLog.getCurrentRequest().getExecutedCommands().toArray(new HystrixComma nd<?>[1])[0];

assertEquals("GetValueForKey", command.getCommandKey().name()); assertTrue(command.getExecutionEvents().contains(HystrixEventType.COLLAPSED)); assertTrue(command.getExecutionEvents().contains(HystrixEventType.SUCCESS));

#### } finally {

context.shutdown(); }

} }

NOTE:使⽤场景:HystrixCollapser⽤于对多个相同业务的请求合并到⼀个线程甚⾄可以合并到⼀个连 接中执⾏，降低线程交互次和IO数,但必须保证他们属于同⼀依赖.

# 四:监控平台搭建Hystrix-dashboard

- 1:监控dashboard介绍 dashboard⾯板可以对依赖关键指标提供实时监控,如下图:

- 2:实例暴露command统计数据 Hystrix使⽤Servlet对当前JVM下所有command调⽤情况作数据流输出 配置如下:

Xml代码

- 3:集群模式监控统计搭建


![image 24](assets/imageFile24.png)

![image 25](assets/imageFile25.png)

收藏代码

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


<servlet>

<display-name>HystrixMetricsStreamServlet</display-name> <servlet-name>HystrixMetricsStreamServlet</servlet-name> <servlet-

class>com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet</servletclass> </servlet> <servlet-mapping>

<servlet-name>HystrixMetricsStreamServlet</servlet-name> <url-pattern>/hystrix.stream</url-pattern>

</servlet-mapping> <!--

对应URL格式 : http://hostname:port/application/hystrix.stream

-->

- 1)使⽤Turbine组件做集群数据汇总 结构图如下;

- 2)内嵌jetty提供Servlet容器,暴露HystrixMetrics Java代码


![image 26](assets/imageFile26.png)

![image 27](assets/imageFile27.png)

收藏代码

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


public class JettyServer { private final Logger logger = LoggerFactory.getLogger(this.getClass()); private int port; private ExecutorService executorService = Executors.newFixedThreadPool(1); private Server server = null; public void init() {

#### try {

executorService.execute(new Runnable() { @Override public void run() {

try { //绑定8080端⼝,加载HystrixMetricsStreamServlet并映射url server = new Server(8080); WebAppContext context = new WebAppContext(); context.setContextPath("/"); context.addServlet(HystrixMetricsStreamServlet.class, "/hystrix.stream")

;

context.setResourceBase("."); server.setHandler(context); server.start(); server.join();

} catch (Exception e) {

logger.error(e.getMessage(), e); }

} });

} catch (Exception e) {

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


logger.error(e.getMessage(), e); }

} public void destory() {

if (server != null) {

try { server.stop(); server.destroy(); logger.warn("jettyServer stop and destroy!");

} catch (Exception e) {

logger.error(e.getMessage(), e); }

} }

}

- 3)Turbine搭建和配置


- a:配置Turbine Servlet收集器

Java代码

- b:编写config.properties配置集群实例

Java代码

- c:使⽤Dashboard配置连接Turbine 如下图 :


![image 28](assets/imageFile28.png)

收藏代码

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


<servlet> <description></description> <display-name>TurbineStreamServlet</display-name> <servlet-name>TurbineStreamServlet</servlet-name> <servlet-class>com.netflix.turbine.streaming.servlet.TurbineStreamServlet</servlet-class>

</servlet> <servlet-mapping>

<servlet-name>TurbineStreamServlet</servlet-name> <url-pattern>/turbine.stream</url-pattern>

</servlet-mapping>

![image 29](assets/imageFile29.png)

收藏代码 #配置两个集群:mobil-online,ugc-online turbine.aggregator.clusterConfig=mobil-online,ugc-online #配置mobil-online集群实例 turbine.ConfigPropertyBasedDiscovery.mobilonline.instances=10.10.*.*,10.10.*.*,10.10.*.*,10.10.*.*,10.10.*.*,10.10.*.*,10.16.*.*,10.16.*.* ,10.16.*.*,10.16.*.* #配置mobil-online数据流servlet turbine.instanceUrlSuffix.mobil-online=:8080/hystrix.stream #配置ugc-online集群实例 turbine.ConfigPropertyBasedDiscovery.ugconline.instances=10.10.*.*,10.10.*.*,10.10.*.*,10.10.*.*#配置ugc-online数据流servlet turbine.instanceUrlSuffix.ugc-online=:8080/hystrix.stream

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


![image 30](assets/imageFile30.png)

# 五:Hystrix配置与分析

## 1:Hystrix 配置

- 1):Command 配置 Command配置源码在HystrixCommandProperties,构造Command时通过Setter进⾏配置 具体配置解释和默认值如下 Java代码

- 2):熔断器（Circuit Breaker）配置


![image 31](assets/imageFile31.png)

收藏代码 //使⽤命令调⽤隔离⽅式,默认:采⽤线程隔离,ExecutionIsolationStrategy.THREAD private final HystrixProperty<ExecutionIsolationStrategy> executionIsolationStrategy; //使⽤线程隔离时，调⽤超时时间，默认:1秒 private final HystrixProperty<Integer> executionIsolationThreadTimeoutInMilliseconds; //线程池的key,⽤于决定命令在哪个线程池执⾏ private final HystrixProperty<String> executionIsolationThreadPoolKeyOverride; //使⽤信号量隔离时，命令调⽤最⼤的并发数,默认:10 private final HystrixProperty<Integer> executionIsolationSemaphoreMaxConcurrentRequests; //使⽤信号量隔离时，命令fallback(降级)调⽤最⼤的并发数,默认:10 private final HystrixProperty<Integer> fallbackIsolationSemaphoreMaxConcurrentRequests; //是否开启fallback降级策略 默认:true private final HystrixProperty<Boolean> fallbackEnabled; // 使⽤线程隔离时，是否对命令执⾏超时的线程调⽤中断（Thread.interrupt()）操作.默认:true private final HystrixProperty<Boolean> executionIsolationThreadInterruptOnTimeout; // 统计滚动的时间窗⼝,默认:5000毫秒circuitBreakerSleepWindowInMilliseconds private final HystrixProperty<Integer> metricsRollingStatisticalWindowInMilliseconds; // 统计窗⼝的Buckets的数量,默认:10个,每秒⼀个Buckets统计 private final HystrixProperty<Integer> metricsRollingStatisticalWindowBuckets; // number of buck

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


ets in the statisticalWindow //是否开启监控统计功能,默认:true private final HystrixProperty<Boolean> metricsRollingPercentileEnabled;

// 是否开启请求⽇志,默认:true private final HystrixProperty<Boolean> requestLogEnabled; //是否开启请求缓存,默认:true private final HystrixProperty<Boolean> requestCacheEnabled; // Whether request caching is enable d.

Circuit Breaker配置源码在HystrixCommandProperties,构造Command时通过Setter进⾏配置,每种依赖 使⽤⼀个Circuit Breaker

Java代码

![image 32](assets/imageFile32.png)

收藏代码 // 熔断器在整个统计时间内是否开启的阀值，默认20秒。也就是10秒钟内⾄少请求20次，熔断器才发挥起作⽤ private final HystrixProperty<Integer> circuitBreakerRequestVolumeThreshold; //熔断器默认⼯作时间,默认:5秒.熔断器中断请求5秒后会进⼊半打开状态,放部分流量过去重试 private final HystrixProperty<Integer> circuitBreakerSleepWindowInMilliseconds; //是否启⽤熔断器,默认true. 启动 private final HystrixProperty<Boolean> circuitBreakerEnabled; //默认:50%。当出错率超过50%后熔断器启动. private final HystrixProperty<Integer> circuitBreakerErrorThresholdPercentage; //是否强制开启熔断器阻断所有请求,默认:false,不开启 private final HystrixProperty<Boolean> circuitBreakerForceOpen; //是否允许熔断器忽略错误,默认false, 不开启 private final HystrixProperty<Boolean> circuitBreakerForceClosed;

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


- 3):命令合并(Collapser)配置 Command配置源码在HystrixCollapserProperties,构造Collapser时通过Setter进⾏配置 Java代码

- 4):线程池(ThreadPool)配置 Java代码


![image 33](assets/imageFile33.png)

收藏代码 //请求合并是允许的最⼤请求数,默认: Integer.MAX_VALUE private final HystrixProperty<Integer> maxRequestsInBatch; //批处理过程中每个命令延迟的时间,默认:10毫秒 private final HystrixProperty<Integer> timerDelayInMilliseconds; //批处理过程中是否开启请求缓存,默认:开启 private final HystrixProperty<Boolean> requestCacheEnabled;

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


![image 34](assets/imageFile34.png)

收藏代码 /** 配置线程池⼤⼩,默认值10个. 建议值:请求⾼峰时99.5%的平均响应时间 + 向上预留⼀些即可

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


- */ HystrixThreadPoolProperties.Setter().withCoreSize(int value) /** 配置线程值等待队列⻓度,默认值:-1 建议值:-1表示不等待直接拒绝,测试表明线程池使⽤直接决绝策略+ 合适⼤⼩的⾮回缩线程池效率最⾼.所以不建议修改此值。 当使⽤⾮回缩线程池时，queueSizeRejectionThreshold,keepAliveTimeMinutes 参数⽆效

- */ HystrixThreadPoolProperties.Setter().withMaxQueueSize(int value)


- 2:Hystrix关键组件分析


- 1):Hystrix流程结构解析


![image 35](assets/imageFile35.png)

流程说明:

- 1:每次调⽤创建⼀个新的HystrixCommand,把依赖调⽤封装在run()⽅法中.

- 2:执⾏execute()/queue做同步或异步调⽤.

- 3:判断熔断器(circuit-breaker)是否打开,如果打开跳到步骤8,进⾏降级策略,如果关闭进⼊步骤.

- 4:判断线程池/队列/信号量是否跑满，如果跑满进⼊降级步骤8,否则继续后续步骤.

- 5:调⽤HystrixCommand的run⽅法.运⾏依赖逻辑

- 5a:依赖逻辑调⽤超时,进⼊步骤8.

- 6:判断逻辑是否调⽤成功


- 6a:返回成功调⽤结果


- 6b:调⽤出错，进⼊步骤8.

- 7:计算熔断器状态,所有的运⾏状态(成功, 失败, 拒绝,超时)上报给熔断器，⽤于统计从⽽判断熔断器状 态.

- 8:getFallback()降级逻辑. 以下四种情况将触发getFallback调⽤：

- (1):run()⽅法抛出⾮HystrixBadRequestException异常。

- (2):run()⽅法调⽤超时

- (3):熔断器开启拦截调⽤

- (4):线程池/队列/信号量是否跑满


- 8a:没有实现getFallback的Command将直接抛出异常

- 8b:fallback降级逻辑调⽤成功直接返回

- 8c:降级逻辑调⽤失败抛出异常


- 9:返回执⾏成功结果


- 2):熔断器:Circuit Breaker Circuit Breaker 流程架构和统计


![image 36](assets/imageFile36.png)

- 每个熔断器默认维护10个bucket,每秒⼀个bucket,每个blucket记录成功,失败,超时,拒绝的状态， 默认错误超过50%且10秒内超过20个请求进⾏中断拦截.
- 3)隔离(Isolation)分析 Hystrix隔离⽅式采⽤线程/信号的⽅式,通过隔离限制依赖的并发量和阻塞扩散.


- (1):线程隔离 把执⾏依赖代码的线程与请求线程(如:jetty线程)分离，请求线程可以⾃由控制离开的时间(异步过

程)。 通过线程池⼤⼩可以控制并发量，当线程池饱和时可以提前拒绝服务,防⽌依赖问题扩散。 线上建议线程池不要设置过⼤，否则⼤量堵塞线程有可能会拖慢服务器。

- (2):线程隔离的优缺点 线程隔离的优点:


![image 37](assets/imageFile37.png)

- [1]:使⽤线程可以完全隔离第三⽅代码,请求线程可以快速放回。


- [2]:当⼀个失败的依赖再次变成可⽤时，线程池将清理，并⽴即恢复可⽤，⽽不是⼀个⻓时间的恢复。

- [3]:可以完全模拟异步调⽤，⽅便异步编程。 线程隔离的缺点:


- [1]:线程池的主要缺点是它增加了cpu，因为每个命令的执⾏涉及到排队(默认使⽤SynchronousQueue 避免排队)，调度和上下⽂切换。

- [2]:对使⽤ThreadLocal等依赖线程状态的代码增加复杂性，需要⼿动传递和清理线程状态。 NOTE: Netflix公司内部认为线程隔离开销⾜够⼩，不会造成重⼤的成本或性能的影响。 Netflix 内部API 每天100亿的HystrixCommand依赖请求使⽤线程隔，每个应⽤⼤约40多个线程池，每 个线程池⼤约5-20个线程。


- (3):信号隔离 信号隔离也可以⽤于限制并发访问，防⽌阻塞扩散, 与线程隔离最⼤不同在于执⾏依赖代码的线程


依然是请求线程（该线程需要通过信号申请）, 如果客户端是可信的且可以快速返回，可以使⽤信号隔离替换线程隔离,降低开销.

线程隔离与信号隔离区别如下图:

![image 38](assets/imageFile38.png)
