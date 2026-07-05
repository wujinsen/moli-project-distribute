---
title: Hystrix使用入门手册（中文）.note（原文插图 annex）
slug: annex-Hystrix使用入门手册（中文）
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/Hystrix使用入门手册（中文）.note.md
related: [dubbo-调用原理与分层]
created: 2026-07-05
updated: 2026-07-05
---

导语：⽹上资料（尤其中⽂⽂档）对hystrix基础功能的解释⽐较笼统，看了往往⼀头雾⽔。为此，本 ⽂将通过若⼲demo，加⼊对 的理解和翻译，⼒求更清晰解释hystrix的基础功能。

官⽹How-it-Works

# hystrix实现原理

hystrix语义为“豪猪”，具有⾃我保护的能⼒。hystrix的出现即为解决雪崩效应，它通过四个⽅⾯的机制 来解决这个问题

隔离（线程池隔离和信号量隔离）：限制调⽤分布式服务的资源使⽤，某⼀个调⽤的服务出现问题 不会影响其他服务调⽤。

优雅的降级机制：超时降级、资源不⾜时(线程或信号量)降级，降级后可以配合降级接⼝返回托底 数据。

融断：当失败率达到阀值⾃动触发降级(如因⽹络故障/超时造成的失败率⾼)，熔断器触发的快速失 败会进⾏快速恢复。

缓存：提供了请求缓存、请求合并实现。 ⽀持实时监控、报警、控制（修改配置）

2.1隔离

<table>
  <tr>
    <th>![image 1](assets/imageFile1.png)</th>
  </tr>
</table>


<table>
  <tr>
    <th> </th>
    <th>线程池隔离</th>
    <th>信号量隔离</th>
  </tr>
  <tr>
    <td>线程</td>
    <td>与调⽤线程⾮相同线程</td>
    <td>与调⽤线程相同（jety线程）</td>
  </tr>
  <tr>
    <td>开销</td>
    <td>排队、调度、上下⽂开销等</td>
    <td>⽆线程切换，开销低</td>
  </tr>
  <tr>
    <td>异步</td>
    <td>⽀持</td>
    <td>不⽀持</td>
  </tr>
  <tr>
    <td>并发⽀持</td>
    <td>⽀持（最⼤线程池⼤⼩）</td>
    <td>⽀持（最⼤信号量上限）</td>
  </tr>
</table>


## 2.1隔离

<table>
  <tr>
    <th>![image 2](assets/imageFile2.png)</th>
  </tr>
</table>


<table>
  <tr>
    <th> </th>
    <th>线程池隔离</th>
    <th>信号量隔离</th>
  </tr>
  <tr>
    <td>线程</td>
    <td>与调⽤线程⾮相同线程</td>
    <td>与调⽤线程相同（jety线程）</td>
  </tr>
  <tr>
    <td>开销</td>
    <td>排队、调度、上下⽂开销等</td>
    <td>⽆线程切换，开销低</td>
  </tr>
  <tr>
    <td>异步</td>
    <td>⽀持</td>
    <td>不⽀持</td>
  </tr>
  <tr>
    <td>并发⽀持</td>
    <td>⽀持（最⼤线程池⼤⼩）</td>
    <td>⽀持（最⼤信号量上限）</td>
  </tr>
</table>


## 2.2融断

正常状态下，电路处于关闭状态(Closed)，如果调⽤持续出错或者超时，电路被打开进⼊熔断状态 (Open)，后续⼀段时间内的所有调⽤都会被拒绝(Fail Fast)，⼀段时间以后，保护器会尝试进⼊半熔断 状态(Half-Open)，允许少量请求进来尝试，如果调⽤仍然失败，则回到熔断状态，如果调⽤成功，则 回到电路闭合状态;

![image 3](assets/imageFile3.png)

Paste_Image.png HystrixCircuitBreaker（断路器的具体实现）：

![image 4](assets/imageFile4.png)

Paste_Image.png 详细的⼯作流程：

htp:/hot6hot.iteye.com/blog/215036

## 2.3降级

可能⼤家会混淆“融断”和“降级”两个概念。 在股票市场，熔断这个词⼤家都不陌⽣，是指当股指波幅达到某个点后，交易所为控制⻛险采取的暂 停交易措施。相应的，服务熔断⼀般是指软件系统中，由于某些原因使得服务出现了过载现象，为防 ⽌造成整个系统故障，从⽽采⽤的⼀种保护措施，所以很多地⽅把熔断亦称为过载保护。 ⼤家都⻅过⼥⽣旅⾏吧，⼤号的旅⾏箱是必备物，平常⾛⾛近处绰绰有余，但⼀旦出个远⻔，再⼤的 箱⼦都⽩搭了，怎么办呢？常⻅的情景就是把物品拿出来分分堆，⽐了⼜⽐，最后⼀些⾮必需品的就 忍痛放下了，等到下次箱⼦够⽤了，再带上⽤⼀⽤。⽽服务降级，就是这么回事，整体资源快不够 了，忍痛将某些服务先关掉，待渡过难关，再开启回来。 ⼆者的⽬标是⼀致的，⽬的都是保证上游服务的稳定性。但其关注的重点并不⼀样，融断对下层依赖 的服务并不级（或者说孰轻孰重），⼀旦产⽣故障就断掉；⽽降级需要对下层依赖的业务分级，把产 ⽣故障的丢了，换⼀个轻量级的⽅案，是⼀种退⽽求其次的⽅法。 根据业务场景的不同，⼀般采⽤以下两种模式： 第⼀种（最常⽤）如果服务失败，则我们通过falback进⾏降级，返回静态值。

![image 5](assets/imageFile5.png)

Paste_Image.png 第⼆种采⽤服务级联的模式，如果第⼀个服务失败，则调⽤备⽤服务，例如失败重试或者访问缓存失 败再去取数据库。服务级联的⽬的则是尽最⼤努⼒保证返回数据的成功性，但如果考虑不充分，则有 可能导致级联的服务崩溃（⽐如，缓存失败了，把全部流量打到数据库，瞬间导致数据库挂掉）。因 此级联模式，也要慎⽤，增加了管理的难度。

![image 6](assets/imageFile6.png)

所⽤demo均对 进⾏了⼆次修改，⻅ Hystrix是Netflix开源的⼀款容错系统，能帮助使⽤者码出具备强⼤的容错能⼒和鲁棒性的程序。如果 某程序或clas要使⽤Hystrix，只需简单继承HystrixCommand/HystrixObservableCommand并重写 run()/construct()，然后调⽤程序实例化此clas并执⾏ execute()/queue()/observe()/toObservable()。

官⽹How-To-Use htps:/github.com/star2478/java-hystrix

// HelloWorldHystrixCommand要使⽤Hystrix功能 public class HelloWorldHystrixCommand extends HystrixCommand {

private final String name; public HelloWorldHystrixCommand(String name) {

super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup")); this.name = name;

} // 如果继承的是HystrixObservableCommand，要重写Observable construct() @Override protected String run() {

return "Hello " + name; }

} /* 调⽤程序对HelloWorldHystrixCommand实例化，执⾏execute()即触发HelloWorldHystrixCommand.run()的 执⾏ */ String result = new HelloWorldHystrixCommand("HLX").execute(); System.out.println(result); // 打印出Hello HLX

pom.xml加上以下依赖。spring cloud也集成了hystrix，不过本⽂只介绍原⽣hystrix。

<dependency> <groupId>com.netflix.hystrix</groupId> <artifactId>hystrix-core</artifactId> <version>1.5.8</version>

</dependency>

本⽂重点介绍的是Hystrix各项基础能⼒的⽤法及其效果，不从零介绍hystrix，要了解基础知识推 荐 或

官⽹wiki ⺠间blog

## 1、HystrixCo mandvsHystrixObservableCo mand

要想使⽤hystrix，只需要继承HystrixCommand或HystrixObservableCommand，简单⽤法⻅上⾯例⼦。两 者主要区别是：

前者的命令逻辑写在run()；后者的命令逻辑写在construct()

前者的run()是由新创建的线程执⾏；后者的construct()是由调⽤程序线程执⾏

前者⼀个实例只能向调⽤程序发送（emit）单条数据，⽐如上⾯例⼦中run()只能返回⼀个String结 果；后者⼀个实例可以顺序发送多条数据，⽐如 中顺序调⽤多个onNext()，便实现了向调⽤程 序发送多条数据，

demo 甚⾄还能发送⼀个范围的数据集

## 2、4个命令执⾏⽅法

execute()、queue()、observe()、toObservable()这4个⽅法⽤来触发执⾏run()/construct()，⼀个 实例只能执⾏⼀次这4个⽅法，特别说明的是HystrixObservableCommand没有execute()和queue()。 4个⽅法的主要区别是：

execute()：以同步堵塞⽅式执⾏run()。以 为例，调⽤execute()后，hystrix先创建⼀个新线 程运⾏run()，接着调⽤程序要在execute()调⽤处⼀直堵塞着，直到run()运⾏完成

demo

queue()：以异步⾮堵塞⽅式执⾏run()。以 为例，⼀调⽤queue()就直接返回⼀个Future对 象，同时hystrix创建⼀个新线程运⾏run()，调⽤程序通过Future.get()拿到run()的返回结果，⽽ Future.get()是堵塞执⾏的

demo

observe()：事件注册前执⾏run()/construct()。以 为例，第⼀步是事件注册前，先调⽤ observe()⾃动触发执⾏run()/construct()（如果继承的是HystrixCommand，hystrix将创建新线程⾮ 堵塞执⾏run()；如果继承的是HystrixObservableCommand，将以调⽤程序线程堵塞执⾏ construct()），第⼆步是从observe()返回后调⽤程序调⽤subscribe()完成事件注册，如果 run()/construct()执⾏成功则触发onNext()和onCompleted()，如果执⾏异常则触发onError()

demo

toObservable()：事件注册后执⾏run()/construct()。以 为例，第⼀步是事件注册前，⼀调 ⽤toObservable()就直接返回⼀个Observable<String>对象，第⼆步调⽤subscribe()完成事件注册 后⾃动触发执⾏run()/construct()（如果继承的是HystrixCommand，hystrix将创建新线程⾮堵塞执 ⾏run()，调⽤程序不必等待run()；如果继承的是HystrixObservableCommand，将以调⽤程序线程 堵塞执⾏construct()，调⽤程序等待construct()执⾏完才能继续往下⾛），如果 run()/construct()执⾏成功则触发onNext()和onCompleted()，如果执⾏异常则触发onError()

demo

⼏个重要组件如下。 HystrixCommandGroupKey：配置全局唯⼀标识服务分组的名称，⽐如，库存系统就是⼀个服务分 组。当我们监控时，相同分组的服务会聚合在⼀起，必填选项。 HystrixCommandKey：配置全局唯⼀标识服务的名称，⽐如，库存系统有⼀个获取库存服务，那么 就可以为这个服务起⼀个名字来唯⼀识别该服务，如果不配置，则默认是简单类名。 HystrixThreadPoolKey：配置全局唯⼀标识线程池的名称，相同线程池名称的线程池是同⼀个，如果 不配置，则默认是分组名，此名字也是线程池中线程名字的前缀。 HystrixThreadPoolProperties：配置线程池参数，coreSize配置核⼼线程池⼤⼩和线程池最⼤⼤⼩， keepAliveTimeMinutes是线程池中空闲线程⽣存时间（如果不进⾏动态配置，那么是没有任何作⽤ 的），maxQueueSize配置线程池队列最⼤⼤⼩，queueSizeRejectionThreshold限定当前队列⼤⼩， 即实际队列⼤⼩由这个参数决定，通过改变queueSizeRejectionThreshold可以实现动态队列⼤⼩调 整。 HystrixCommandProperties：配置该命令的⼀些参数，如executionIsolationStrategy配置执⾏隔离 策略，默认是使⽤线程隔离，此处我们配置为THREAD，即线程池隔离。

此处可以粗粒度实现隔离，也可以细粒度实现隔离，如下所示。 服务分组+线程池：粗粒度实现，⼀个服务分组/系统配置⼀个隔离线程池即可，不配置线程池名称或者 相同分组的线程池名称配置为⼀样。 服务分组+服务+线程池：细粒度实现，⼀个服务分组中的每⼀个服务配置⼀个隔离线程池，为不同的 命令实现配置不同的线程池名称即可。 混合实现：⼀个服务分组配置⼀个隔离线程池，然后对重要服务单独设置隔离线程池。

## 3、falback（降级）

使⽤falback机制很简单，继承HystrixCommand只需重写getFallback()，继承 HystrixObservableCommand只需重写resumeWithFallback()，⽐如HelloWorldHystrixCommand加上下⾯ 代码⽚段： @Override protected String getFallback() {

return "fallback: " + name;

} falback实际流程是当run()/construct()被触发执⾏时或执⾏中发⽣错误时，将转向执⾏ getFallback()/resumeWithFallback()。结合下图，4种错误情况将触发falback：

⾮HystrixBadRequestException异常：以 为例，当抛出HystrixBadRequestException时， 调⽤程序可以捕获异常，没有触发getFallback()，⽽其他异常则会触发getFallback()，调⽤程序 将获得getFallback()的返回

demo

run()/construct()运⾏超时：以 为例，使⽤⽆限while循环或sl ep模拟超时，触发了 getFallback()

demo

熔断器启动：以 为例，我们配置10s内请求数⼤于3个时就启动熔断器，请求错误率⼤于80% 时就熔断，然后for循环发起请求，当请求符合熔断条件时将触发getFallback()。更多熔断策略⻅ 下⽂

demo

线程池/信号量已满：以 为例，我们配置线程池数⽬为3，然后先⽤⼀个for循环执⾏queue()， 触发的run()sl ep 2s，然后再⽤第2个for循环执⾏execute()，发现所有execute()都触发了 falback，这是因为第1个for的线程还在sl ep，占⽤着线程池所有线程，导致第2个for的所有命令都 ⽆法获取到线程

demo

![image 7](assets/imageFile7.png)

来⾃hystrix github wiki 调⽤程序可以通过isResponseFromFallback()查询结果是由run()/construct()还是 getFallback()/resumeWithFallback()返回的

## 4、隔离策略

hystrix提供了两种隔离策略：线程池隔离和信号量隔离。hystrix默认采⽤线程池隔离。

demo

线程池隔离：不同服务通过使⽤不同线程池，彼此间将不受影响，达到隔离效果。以 为例， 我们通过andThreadPolKey配置使⽤命名为ThreadPoolTest的线程池，实现与其他命名的线程池天 然隔离，如果不配置andThreadPolKey则使⽤withGroupKey配置来命名线程池

信号量隔离：线程隔离会带来线程开销，有些场景（⽐如⽆⽹络请求场景）可能会因为⽤开销换隔 离得不偿失，为此hystrix提供了信号量隔离，当服务的并发数⼤于信号量阈值时将进⼊falback。以

为例，通过withExecutionIsolationStrategy(ExecutionIsolationStrategy.SEMAPHORE)配置 为信号量隔离，通过withExecutionIsolationSemaphoreMaxConcurrentRequests配置执⾏并发数不能 ⼤于3，由于信号量隔离下⽆论调⽤哪种命令执⾏⽅法，hystrix都不会创建新线程执⾏ run()/construct()，所以调⽤程序需要⾃⼰创建多个线程来模拟并发调⽤execute()，最后看到⼀ 旦并发线程>3，后续请求都进⼊falback

demo

## 5、熔断机制

熔断机制相当于电路的跳闸功能，举个栗⼦，我们可以配置熔断策略为当请求错误⽐例在10s内>50% 时，该服务将进⼊熔断状态，后续请求都会进⼊falback。 以 为例，我们通过withCircuitBreakerRequestVolumeThreshold配置10s内请求数超过3个时熔断 器开始⽣效，通过withCircuitBreakerErrorThresholdPercentage配置错误⽐例>80%时开始熔断，然 后for循环执⾏execute()触发run()，在run()⾥，如果name是⼩于10的偶数则正常返回，否则超时，通 过多次循环后，超时请求占所有请求的⽐例将⼤于80%，就会看到后续请求都不进⼊run()⽽是进⼊ getFallback()，因为不再打印"running run():" + name了。 除此之外，hystrix还⽀持多⻓时间从熔断状态⾃动恢复等功能，⻅下⽂附录。

demo

## 6、结果cache

hystrix⽀持将⼀个请求结果缓存起来，下⼀个具有相同key的请求将直接从缓存中取出结果，减少请求 开销。要使⽤hystrix cache功能，第⼀个要求是重写getCacheKey()，⽤来构造cache key；第⼆个要 求是构建context，如果请求B要⽤到请求A的结果缓存，A和B必须同处⼀个context。通过 HystrixRequestContext.initializeContext()和context.shutdown()可以构建⼀个context，这两条语 句间的所有请求都处于同⼀个context。 以 的testWithCacheHits()为例，comand2a、comand2b、comand2c同处⼀个context， 前两者的cache key都是2HLX（⻅getCacheKey()），所以comand2a执⾏完后把结果缓存， comand2b执⾏时就不⾛run()⽽是直接从缓存中取结果了，⽽comand2c的cache key是2HLX1，⽆ 法从缓存中取结果。此外，通过isResponseFromCache()可检查返回结果是否来⾃缓存。

demo

## 7、合并请求colapsing

hystrix⽀持N个请求⾃动合并为⼀个请求，这个功能在有⽹络交互的场景下尤其有⽤，⽐如每个请求都 要⽹络访问远程资源，如果把请求合并为⼀个，将使多次⽹络交互变成⼀次，极⼤节省开销。重要⼀ 点，两个请求能⾃动合并的前提是两者⾜够“近”，即两者启动执⾏的间隔时⻓要⾜够⼩，默认为 10ms，即超过10ms将不⾃动合并。 以 为例，我们连续发起多个queue请求，依次返回f1~f6共6个Future对象，根据打印结果可知 f1~f5同处⼀个线程，说明这5个请求被合并了，⽽f6由另⼀个线程执⾏，这是因为f5和f6中间隔了⼀个 sl ep，超过了合并要求的最⼤间隔时⻓。

demo

## 附录：各种策略配置

htp:/hot6hot.iteye.com/blog/215036

根据 整理⽽得。

HystrixComandProperties

/* --------------统计相关------------------*/ // 统计滚动的时间窗⼝,默认:5000毫秒（取⾃circuitBreakerSleepWindowInMilliseconds） private final HystrixProperty metricsRollingStatisticalWindowInMilliseconds; // 统计窗⼝的Buckets的数量,默认:10个,每秒⼀个Buckets统计 private final HystrixProperty metricsRollingStatisticalWindowBuckets; // number of buckets in the statisticalWindow // 是否开启监控统计功能,默认:true private final HystrixProperty metricsRollingPercentileEnabled; /* --------------熔断器相关------------------*/ // 熔断器在整个统计时间内是否开启的阀值，默认20。也就是在 metricsRollingStatisticalWindowInMilliseconds（默认10s）内⾄少请求20次，熔断器才发挥起作⽤ private final HystrixProperty circuitBreakerRequestVolumeThreshold; //熔断器默认⼯作时间,默认:5秒.熔断器中断请求5秒后会进⼊半打开状态,放部分流量过去重试 private final HystrixProperty circuitBreakerSleepWindowInMilliseconds; //是否启⽤熔断器,默认true. 启动 private final HystrixProperty circuitBreakerEnabled; //默认:50%。当出错率超过50%后熔断器启动 private final HystrixProperty circuitBreakerErrorThresholdPercentage; //是否强制开启熔断器阻断所有请求,默认:false,不开启。置为true时，所有请求都将被拒绝，直接到fallback private final HystrixProperty circuitBreakerForceOpen; //是否允许熔断器忽略错误,默认false, 不开启 private final HystrixProperty circuitBreakerForceClosed; /* --------------信号量相关------------------*/ //使⽤信号量隔离时，命令调⽤最⼤的并发数,默认:10 private final HystrixProperty executionIsolationSemaphoreMaxConcurrentRequests; //使⽤信号量隔离时，命令fallback(降级)调⽤最⼤的并发数,默认:10 private final HystrixProperty fallbackIsolationSemaphoreMaxConcurrentRequests; /* --------------其他------------------*/ //使⽤命令调⽤隔离⽅式,默认:采⽤线程隔离,ExecutionIsolationStrategy.THREAD private final HystrixProperty executionIsolationStrategy; //使⽤线程隔离时，调⽤超时时间，默认:1秒 private final HystrixProperty executionIsolationThreadTimeoutInMilliseconds; //线程池的key,⽤于决定命令在哪个线程池执⾏ private final HystrixProperty executionIsolationThreadPoolKeyOverride; //是否开启fallback降级策略 默认:true private final HystrixProperty fallbackEnabled; // 使⽤线程隔离时，是否对命令执⾏超时的线程调⽤中断（Thread.interrupt()）操作.默认:true private final HystrixProperty executionIsolationThreadInterruptOnTimeout; // 是否开启请求⽇志,默认:true private final HystrixProperty requestLogEnabled; //是否开启请求缓存,默认:true private final HystrixProperty requestCacheEnabled; // Whether request caching is enabled.

HystrixColapserProperties

//请求合并是允许的最⼤请求数,默认: Integer.MAX_VALUE private final HystrixProperty maxRequestsInBatch; //批处理过程中每个命令延迟的时间,默认:10毫秒 private final HystrixProperty timerDelayInMilliseconds; //批处理过程中是否开启请求缓存,默认:开启 private final HystrixProperty requestCacheEnabled;

HystrixThreadPolProperties

/* 配置线程池⼤⼩,默认值10个. 建议值:请求⾼峰时99.5%的平均响应时间 + 向上预留⼀些即可 */ private final HystrixProperty corePoolSize; /* 配置线程值等待队列⻓度,默认值:-1 建议值:-1表示不等待直接拒绝,测试表明线程池使⽤直接决绝策略+ 合适⼤⼩的 ⾮回缩线程池效率最⾼.所以不建议修改此值。 当使⽤⾮回缩线程池时， queueSizeRejectionThreshold,keepAliveTimeMinutes 参数⽆效 */ private final HystrixProperty maxQueueSize;

## 参考⽂献

htps:/github.com/Netflix/Hystrix htps:/github.com/Netflix/Hystrix/wiki/How-To-Use htp:/hot6hot.iteye.com/blog/215036

### ⼀、hystrixdashboard 作⽤：

监控各个hystrixcommand的各种值。 通过dashboards的实时监控来动态修改配置，直到满意为⽌

仪表盘：

![image 8](assets/imageFile8.png)

⼆、启动hystrix

- 1、下载standalone-hystrix-dashboard-1.5.3-all.jar


https://github.com/kennedyoliveira/standalone-hystrix-dashboard:该⻚⾯提供了⼀个很好的视频教学。

- 2、启动hystrix-dashboard

- 3、测试


java -jar -DserverPort=7979 -DbindAddress=localhost standalone-hystrix-dashboard-1.5.3-all.jar 注意：其中的serverPort、bindAddress是可选参数，若不添加，默认是7979和localhost

浏览器输⼊http://localhost:7979/hystrix-dashboard/，出现⼩熊⻚⾯就是正确了。

三、代码

- 1、pom.xml

- 1 <dependency>

- 2 <groupId>com.netflix.hystrix</groupId>

- 3 <artifactId>hystrix-core</artifactId>

- 4 <version>1.4.10</version>

- 5 </dependency>

- 6 <!-- http://mvnrepository.com/artifact/com.netflix.hystrix/hystrix-metrics-event-stream

-->

- 7 <dependency>

- 8 <groupId>com.netflix.hystrix</groupId>

- 9 <artifactId>hystrix-metrics-event-stream</artifactId>

- 10 <version>1.4.10</version>

- 11 </dependency>


说明：

- 2、配置HystrixMetricsStreamServlet


![image 9](assets/imageFile9.png)

复制代码

![image 10](assets/imageFile10.png)

复制代码

hystrix-core：hystrix核⼼接⼝包 hystrix-metrics-event-stream：只要客户端连接还连着，hystrix-metrics-event-stream就会不断的向客户端以 text/event-stream的形式推送计数结果（metrics）

![image 11](assets/imageFile11.png)

复制代码

- 1 package com.xxx.firstboot.hystrix.dashboard;

- 2

- 3 import org.springframework.boot.context.embedded.ServletRegistrationBean;

- 4 import org.springframework.context.annotation.Bean;

- 5 import org.springframework.context.annotation.Configuration;

- 6

- 7 import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;

- 8

- 9 @Configuration

- 10 public class HystrixConfig {

- 11

- 12 @Bean

- 13 public HystrixMetricsStreamServlet hystrixMetricsStreamServlet(){

- 14 return new HystrixMetricsStreamServlet();

- 15 }

- 16

- 17 @Bean

- 18 public ServletRegistrationBean registration(HystrixMetricsStreamServlet servlet){

- 19 ServletRegistrationBean registrationBean = new ServletRegistrationBean();

- 20 registrationBean.setServlet(servlet);

- 21 registrationBean.setEnabled(true);//是否启⽤该registrationBean

- 22 registrationBean.addUrlMappings("/hystrix.stream");

- 23 return registrationBean;

- 24 }

- 25 }


![image 12](assets/imageFile12.png)

复制代码

说明：以上⽅式是springboot注⼊servlet并进⾏配置的⽅式。 参考：

第⼆⼗四章 springboot注⼊servlet

四、测试

![image 13](assets/imageFile13.png)

说明：启动服务后，输⼊localhost:8001/hystrix.stream，之后点击"Add Stream"，最后点击"Monitor Stream"即可。

![image 14](assets/imageFile14.png)

### 说明： getHotelInfo - commandKey（其实就是servicename下的⼀个⽅法） hotelService - ThreadPoolKey（不配置的情况下就是commandGroupKey，其实就是servicename）
