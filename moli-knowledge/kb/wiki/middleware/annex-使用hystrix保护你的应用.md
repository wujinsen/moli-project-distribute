---
title: 使用hystrix保护你的应用.note（原文插图 annex）
slug: annex-使用hystrix保护你的应用
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/it隐/使用hystrix保护你的应用.note.md
related: [dubbo-调用原理与分层]
created: 2026-07-05
updated: 2026-07-05
---

hystrix([hɪst'rɪks])是豪猪的意思。豪猪是⼀种哺乳动物，全身是刺⽤以更好的保护⾃⼰。netflix使⽤ 这畜⽣来命名这框架实在是⾮常的贴切，意味着hystrix能够像豪猪的刺⼀样保护着你的应⽤。下⾯是 ⼀张豪猪的⾼清⽆码⼤图。

本⽂专⻔探讨netflix的hystrix框架。⾸先会说明在⼀次请求中调⽤多个远程服务时可能会出现的雪崩问 题，然后提出⼏个解决这些问题的办法，从⽽引出了hystrix框架；之后我们会给出⼀个简单的例⼦试 图说明hystrix是如何解决上述问题的；⽂章主要探讨了线程池隔离技术、信号量隔离技术、优雅降 级、熔断器机制。

从雪崩看应⽤防护 ⼀个现实中常⻅的场景 产⽣原因 常⻅的解决⽅案

使⽤hystrix 从简单例⼦⼊⼿ 创建命令开销 key的意义 正确选择隔离模式 使⽤优雅降级 使⽤熔断器

后记

参考资料

从雪崩看应⽤防护

# ⼀个现实中常⻅的场景

我们先来看⼀个分布式系统中常⻅的简化的模型。此图来⾃hystrix的 ，因为模型⽐较简单我这 ⾥就在不在重复画图，直接使⽤现成的图⽚做补充说明。

官⽅wiki

<table>
  <tr>
    <th>![image 1](assets/imageFile1.png)</th>
  </tr>
</table>


### ⼀个简单的模型

App Container可以是我们的应⽤容器，⽐如jetty，tomcat，也可以是⼀个⽤来处理外部请求的线程池 （⽐如netty的worker线程池）。⼀个⽤户请求有可能依赖其他多个外部服务，⽐如上图中的A,H,I,P， 在不可靠的⽹络环境下，任何的RPC都可能会⾯临三种情况：成功、失败、超时。如果⼀次⽤户请求 所依赖外部服务(A,H,I,P)有任何⼀个不可⽤，就有可能导致整个⽤户请求被阻塞。考虑到应⽤容器的线 程数⽬基本都是固定的（⽐如tomcat的线程池默认200），当在⾼并发的情况下，某⼀外部依赖的服务 超时阻塞，就有可能使得整个主线程池被占满，这是⻓请求拥塞反模式。

<table>
  <tr>
    <th>![image 2](assets/imageFile2.png)</th>
  </tr>
</table>


### soa2

更进⼀步，线程池被占满就会导致整个服务不可⽤，⽽依赖该服务的其他服务，就⼜可能会重复产⽣ 上述问题。因此整个系统就像雪崩⼀样逐渐的扩散、坍塌、崩溃了！

# 产⽣原因

服务提供者不可⽤，从⽽导致服务调⽤者线程资源耗尽是产⽣雪崩的原因之⼀。除此之外还有其他因 素能够产⽣雪崩效应：

服务调⽤者⾃身流量激增，导致系统负载升⾼。⽐如异常流量、⽤户重试、代码逻辑重复 缓存到期刷新，使得请求都流向数据库 重试机制，⽐如我们rpc框架的retry次数，每次重试都可能会进⼀步恶化服务提供者 硬件故障，⽐如机房断电，电缆被挖了….

# 常⻅的解决⽅案

针对上述雪崩情景，有很多应对⽅案，但没有⼀个万能的模式能够应对所有情况。

- 1.
- 2.
- 3.
- 4.
- 5.


针对服务调⽤者⾃身流量激增，我们可以采⽤auto-scaling⽅式进⾏⾃动扩容以应对突发流量，或 在负载均衡器上安装限流模块。参考微博：春节⽇活跃⽤户超⼀亿，探秘如何实现服务器分钟级 扩容 针对缓存到期刷新，我们也有很多⽅案，参考Cache应⽤中的服务过载案例研究 针对重试机制，我们可以减少或关闭重试，直接采⽤failfast，或采⽤failsafe进⾏优雅降级。 针对硬件故障，我们可以做多机房容灾，异地多活等。 针对服务提供者不可⽤，我们可以使⽤资源隔离，熔断器机制等。参考Martin Fowler的熔断器模式

hystrix能够解决服务提供者不可⽤的场景。他采⽤了资源隔离模式，通过线程隔离和信号量隔离保护主线 程池；使⽤熔断器避免⽆节操的重试，并提供断路⾃动复位功能。下⾯我们就来看⼀看如何使⽤ hystrix。

## 使⽤hystrix

hystrix采⽤了命令模式，客户端需要继承抽象类HystrixCommand并实现其特定⽅法。为什么使⽤命令模 式呢？使⽤过RPC框架都应该知道⼀个远程接⼝所定义的⽅法可能不⽌⼀个，为了更加细粒度的保护单 个⽅法调⽤，命令模式就⾮常适合这种场景。命令模式的本质就是分离⽅法调⽤和⽅法实现，在这⾥ 我们通过将接⼝⽅法抽象成HystricCommand的⼦类，从⽽获得安全防护能⼒，并使得的控制⼒度下沉到 ⽅法级别。

<table>
  <tr>
    <th>![image 3](assets/imageFile3.png)</th>
  </tr>
</table>


### 命令模式

# 从简单例⼦⼊⼿

先来看⼀个简单的例⼦，TagService是⼀个远程接⼝，queryTags()是其中⼀个⽅法，我们将其封装为 ⼀个命令：

<table>
  <tr>
    <th>publi clasTagQueryComandextendsHystrixComand<List<String>{<br><br>/ queryTags()的⼊参 intgroupId;<br><br>/ dubo的实现接⼝ TagService remoteServiceRef;<br><br>/ 构造⽅法⽤来进⾏⽅法参数传递 protectedTagQueryComand(intgroupId){ super(Seter.withGroupKey(HystrixComandGroupKey.Factory.asKey("TagService")<br><br>.andComandKey(HystrixComandKey.Factory.asKey("TagQueryComand")<br><br>.adThreadPolKey(HystrixThreadPolKey.Factory.asKey("TagervicePol")<br><br>.andComandPropertiesDefaults(HystrixComandProperties.Seter() itExecutionIsolationStrategy(THREAD)<br><br>.withCircuitBreakerEnabled(true) );<br><br>groupId = groupId; this.remoteServiceRef = AplicationContextHelper.getBean(TagService.clas); }<br><br>/ 我们调⽤远程⽅法定义在这⾥⾯ @Overide protectedList<String>run()throwsException{ returnremoteServiceRef.queryTags(groupId); }<br><br>/ 降级⽅式 @Overide protectedList<String>getFalback(){ returnColections.emptyList();</th>
  </tr>
</table>


}

在以往的编程实战中，我们⼤多是直接通过依赖注⼊的⽅式，注⼊rpc接⼝代理。但经过命令模式包装 之后（使⽤HystrixCommand封装了TagService.queryTags()⽅法），我们每次的调⽤都需要动态的创建 ⼀个命令：

<table>
  <tr>
    <th>/ 带有隔离机制和熔断器的远程调⽤</th>
  </tr>
</table>


List<String> tags =newTagQueryComand(1).execute()

以上的调⽤是阻塞的，他也等同于下⾯的代码：

<table>
  <tr>
    <th>Future<List<String> f =newTagQueryComand(1).queue();</th>
  </tr>
</table>


List<String> tags = f.get();

我们也可以直接使⽤Future模式接⼝执⾏异步调⽤：

<table>
  <tr>
    <th>Future<List<String> f =newTagQueryComand(1).queue();<br><br>/ 做⼀些额外⼯作 if(f.isDone() { f.get();</th>
  </tr>
</table>


}

对于上述实例我们还有以下⼏个问题需要弄明⽩：

- 1.
- 2.
- 3.
- 4.
- 5.


每次new命令对象开销怎么样？ 构造⽅法中的那⼏个key分别是什么意思？ 这⾥的隔离策略配置是什么意思？ 如何去做优雅降级？ 怎么开启和配置熔断器？

# 创建命令开销

每次new⼀个命令确实会有开销。但是如果查看HystrixCommand的源码，你会发现这个类的内部成员 变量⼤都是共享对象。由于使⽤共享对象，每次创建⼀个新的command对象也就仅仅消耗⼀些引⽤空 间以及⼀些⾮共享的原⼦状态变量。因此这个类仍然是⽐较轻量的，我们在继承这个类时，也应该继 续保持轻量。由于做了⼀层封装，对cpu的额外消耗不可避免，但经过netflix的测试发现，带来的额外 性能消耗与他能带来的好处相⽐是可以 。

忽略不计

# key的意义

接着，我们再来说⼀下构造⽅法中key的意义：

- 1.
- 2.
- 3.


HystrixCommandKey他⽤于唯⼀区分⼀个命令对象，并且唯⼀标识熔断器、metric等资源。我们可 以为每⼀个远程⽅法都建⽴⼀个独⼀⽆⼆的key。如果key相同，意味着此时会共⽤熔断器和metric 资源。 HystrixCommandGroupKey将command进⾏分组，主要⽤于统计以便于我们进⾏监控。 HystrixThreadPoolKey⽤来标示线程池，每⼀个command默认配备⼀个线程池（线程隔离模式 下）。如果key相同，则会共⽤⼀个线程池资源。

⼀般实践中，我们将⼀个接⼝中的所有⽅法都⽤不同的命令key区分，组key采⽤类名，线程池则根据 需要选择性的采⽤共享线程池或独⽴线程池。

# 正确选择隔离模式

hystrix之所以能够防⽌雪崩的本质原因，是其运⽤了资源隔离模式。要解释资源隔离的概念，我们可 以⽤船舱做⽐喻。⼀艘游轮⼀般都是⼀个⼀个舱室隔离开来的，这样如果某⼀个舱室出现⽕灾，就不 会波及到其他船舱，从⽽影响整艘游轮（这个是弹性⼯程学的⼀个关键概念：舱壁）。软件资源隔离 如出⼀辙，上⽂已经说过，由于服务提供者不可⽤，可能导致服务调⽤端主线程池被占满。此时如果 采⽤资源隔离模式，将对远程服务的调⽤隔离到⼀个单独的线程池后，若服务提供者不可⽤，那么受 到影响的只会是这个独⽴的线程池。如图：

<table>
  <tr>
    <th>![image 4](assets/imageFile4.png)</th>
  </tr>
</table>


### soa3

hystrix的线程池抽象是HystrixThreadPool类，它封装了JDK的ThreadPoolExecutor，然后通过并发策略 HystrixConcurrencyStrategy对外提供⼯⼚⽅法。我们这⾥只关⼼该线程池的核⼼配置，如下表：

<table>
  <tr>
    <th>参数</th>
    <th>解释</th>
  </tr>
  <tr>
    <td>coreSize</td>
    <td>核⼼线程数，maxSize也是该值</td>
  </tr>
  <tr>
    <td>kepAliveTime</td>
    <td>空闲线程保活时间</td>
  </tr>
  <tr>
    <td>maxQueueSize</td>
    <td>最⼤队列⼤⼩，如果-1则会使⽤交换队列</td>
  </tr>
  <tr>
    <td>queueSizeRejectionThreashold</td>
    <td>当等待队列多⼤的时候，将会执⾏决绝策略</td>
  </tr>
  <tr>
    <td>timeoutInMiliseconds</td>
    <td>执⾏线程的超时时间</td>
  </tr>
</table>


这⾥我们需要注意的是queueSizeRejectionThreashold配置，直接⽤maxQueueSize去限制队列⼤⼩⾏不 ⾏？⾏，但是不好，因为maxQueueSize是在初始化BlockingQueue时写死的，灵活性较差， queueSizeRejectionThreashold则能够动态进⾏配置，灵活性好，我们在调节线程池配置的时候也会 重点关注这个值，如果设置的过⾼，则起不到隔离的⽬的（试想把他和maxQueueSize设置的⾮常⼤， 则基本不会触发拒绝策略），如果设置过⼩，就难以应对突发流量，因为你的缓存队列⼩了，当并发 突然上来后很快就会触发拒绝策略。因此需要根据实际的业务情况求得⼀个最佳值，当然也可以去做 弹性感知。 除了线程池隔离，hystrix还提供了信号量隔离机制。所谓信号量隔离(TryableSemaphore)，说的⽐较⽞ 乎，其实很简单，就是采⽤资源计数法，每⼀个线程来了就去资源池判断⼀下是否有可⽤资源，有就 继续执⾏，然后资源池信号量⾃减，使⽤完再⾃增回来；没有则调⽤降级策略或抛出异常。通过这种 ⽅式能够限制资源的最⼤并发数，但它有两个不好的地⽅：其⼀是他⽆法使⽤异步调⽤，因为使⽤信 号量，意味着在调⽤者线程中执⾏run()⽅法；其⼆信号量不像线程池⾃带缓冲队列，⽆法应对突发情 况，当达设定的并发后，就会执⾏失败。因此信号量更适⽤于⾮⽹络请求的场景中。信号量隔离模式 下的最主要配置就是semaphoreMaxConcurrentRequests，⽤来设定最⼤并发量。我们再来看⼀下信号量 的实现类，TrableSemaphore：

<table>
  <tr>
    <th>privatestati clasTryableSemaphore{ / 总资源数 privatefinalHystrixProperty<Integer> numberOfPermits; / 当前资源数<br><br>rivatefinalAtomicInteger count =newAtomicInteger(0); publicTryableSemaphore(HystrixProperty<Integer> numberOfPermits){ this.numberOfPermits = numberOfPermits; } publicboleantryAcquire(){ intcurentCount = count.incrementAndGet(); if(curentCount > numberOfPermits.get() { count.decrementAndGet(); returnfalse; }else{ returntrue;<br><br>} publicvoidrelease(){ count.decrementAndGet(); } publicintgetNumberOfPermitsUsed(){ returncount.get();</th>
  </tr>
</table>


}

# 使⽤优雅降级

所谓的优雅降级本质上就是指当服务提供者不可⽤时，我们能够通过某种⼿段容忍这种不可⽤，以不 影响正常请求。我们这⾥举个查询标签服务的例⼦，如果该服务不可⽤，是可以返回⼀组默认标签以 提供优雅降级。⽐如，我们要查看⼤品类，它包括：家电、图书、⾳响等，这时我们可以在系统初始 化中默认装载这⼀批兜底数据，当服务不可⽤，我们则降级到这些兜底数据上，虽然数据可能不完备， 但基本可⽤。使⽤hystrix可以⾮常⽅便的添加优雅降级策略，只需要OverridegetFallback()⽅法就可 以了。

<table>
  <tr>
    <th>/ 降级⽅式 @Overide protectedList<String>getFalback(){<br><br>/ 这⾥我们可以返回兜底数据 returnColections.emptyList();</th>
  </tr>
</table>


}

⽗类的getFallback()是直接抛出异常的，因此要想开启优雅降级，必须重写这个⽅法，并且需要确保 配置withFallbackEnabled被开启。有的时候我们可能会在降级代码中访问远程数据（⽐如访问 redis），那么当并发量上来之后，也需要保护我们的降级调⽤，此时可以配置 withFallbackIsolationSemaphoreMaxConcurrentRequests参数，当调⽤降级代码的并发数超过阈值时会 抛出REJECTED_SEMAPHORE_FALLBACK异常 降级有很多种玩法， 也说了⼏种降级策略，我们可以根据实际情况选择合适的降级策略：

官⽅wiki failfast：表示⻢上抛出异常，即不会降级，⽐较适⽤于关键服务。 fail silent：或者叫做failsafe，默默的什么都不做，并发度最⼤

failback static：⽐如返回0，true，false等 failback stubbed：返回默认的数据，⽐如上⽂的默认标签 failback cache via network：通过⽹络访问缓存数据

# 使⽤熔断器

熔断器与家⾥⾯的保险丝有些类似，当电流过⼤，保险丝熔断以保护我们的电器。在没有熔断器机制 保护下，我们可能会⽆节操的重试，这会持续加⼤服务端压⼒，造成恶性循环；如果直接关闭重试功 能，当服务端⼜可⽤的时候，我们⼜该如何恢复？熔断器正好适合这种场景：当请求失败⽐率(失败/总 数)达到⼀定阈值后，熔断器开启，并休眠⼀段时间，这段休眠期过后熔断器将处与半开状态(halfopen)，在此状态下将试探性的放过⼀部分流量(hystrix只⽀持single request)，如果这部分流量调⽤成 功后，再次将熔断器闭合，否则熔断器继续保持开启并进⼊下⼀轮休眠周期。

<table>
  <tr>
    <th>![image 5](assets/imageFile5.png)</th>
  </tr>
</table>


### 熔断器状态变迁

我们知道了熔断器的原理后，再重点看⼀下hystrix都⽀持哪些熔断器配置：

<table>
  <tr>
    <th>参数</th>
    <th>解释</th>
  </tr>
  <tr>
    <td>enabled</td>
    <td>熔断器是否开启，默认开启</td>
  </tr>
  <tr>
    <td>erorThresholdPercentage</td>
    <td>熔断器错误⽐率阈值</td>
  </tr>
  <tr>
    <td>forceClosed</td>
    <td>是否强制闭合</td>
  </tr>
  <tr>
    <td>forceOpen</td>
    <td>是否强制打开</td>
  </tr>
  <tr>
    <td>requestVolumeThreshold</td>
    <td>表示请求数⾄少达到多⼤才进⾏熔断计算</td>
  </tr>
  <tr>
    <td>sl epWindowInMiliseconds</td>
    <td>半开的触发试探休眠时间</td>
  </tr>
</table>


errorThresholdPercentage⽤来设定错误⽐率，默认50%，⽐如在⼀段时间内我们有100个调⽤请求，

其中有70个超时了，那么这段时间的错误⽐率是70%，它⼤于50%则会触发熔断器熔断。这个值的设 定⾮常重要，他表示我们对错误的容忍程度，值越⼩我们对错误的容忍程度越⼩。强制闭合和强制打 开是两个运⾏时调节动态参数，如果强制闭合则忽略统计信息，熔断器⻢上闭合，相反强制打开则会 保证熔断器始终处于open状态。requestVolumeThreshold也是⼀个⽐较重要的参数，默认是20，表示 ⾄少有20个请求才进⾏熔断错误⽐率计算。什么意思？⽐如我有19个请求，但是全部失败了，错误⽐ 率100%，但也不会触发熔断，因为我的volume设定的是20。sleepWindowInMilliseconds是半开试探 休眠时间，默认是5000ms，什么是试探休眠时间？上⾯我们说到了熔断器⾃动恢复的原理：当熔断器

开启⼀段时间之后，再放过⼀部分流量进⾏试探。这⼀段时间就是试探休眠时间。如果这个值⽐较 ⼤，意味着我们可能需要⼀段⽐较⻓的恢复时间。如果值⽐较⼩，则表示我们需要更好地应对⽹络抖 动情况。 hystrix抽象出HystrixCircuitBreaker接⼝⽤来提供熔断器功能，其在内部维护了AtomicBoolean circuitOpen作为熔断器状态开关。下⾯我们来看⼀下其实现的核⼼代码：

<table>
  <tr>
    <th>/ 相关配置，就是我们上⽂在构造⽅法中的命令配置 privatefinalHystrixComandProperties properties;<br><br>/ 统计信息，按照时间窗⼝进⾏统计 privatefinalHystrixComandMetrics metrics; / 熔断器状态 privateAtomicBolean circuitOpen =newAtomicBolean(false); / 熔断器打开时间或者上⼀次半开测试的时间，主要⽤于从休眠期恢复 privateAtomicLong circuitOpenedOrLastTestedTime =newAtomicLong();<br><br>/ 外部调⽤者主要通过该⽅法获取熔断器状态 publicboleanisOpen(){ if(circuitOpen.get() {<br><br>/ 如果熔断器是打开的，则返回true returntrue; }<br><br>/ metric能够统计服务调⽤情况 HealthCounts health = metrics.getHealthCounts();<br><br>/ 如果没有达到熔断器设定的volumn值则false，肯定是关闭的 if(health.getTotalRequests() < properties.circuitBreakerRequestVolumeThreshold().get() { returnfalse; }<br><br>/ 如果错误⽐率也没有达到设定值，也会关闭的 if(health.getErorPercentage() < properties.circuitBreakerErorThresholdPercentage().get() { returnfalse; }else{<br><br>/ 熔断器开启 if(circuitOpen.compareAndSet(false,true) {<br><br>/设定熔断器打开时间，主要为了进⾏休眠期判断 circuitOpenedOrLastTestedTime.set(System.curentTimeMilis(); returntrue; }else{ returnfalse;<br><br>}<br><br>/做single request测试 publicboleanalowSingleTest(){ longtimeCircuitOpenedOrWasLastTested = circuitOpenedOrLastTestedTime.get();<br><br>/ 判断是否已经过了熔断器打开休眠期 if(circuitOpen.get() & System.curentTimeMilis() > timeCircuitOpenedOrWasLastTested + properties.circuitBreakerSl epWindowInMiliseconds().get() { / 这⾥将上⼀次测试时间设置为当前时间，主要为了休眠期判断 if(circuitOpenedOrLastTestedTime.compareAndSet(timeCircuitOpenedOrWasLastTested, System.curentTimeMilis( ) { returntrue;<br><br>} returnfalse;</th>
  </tr>
</table>


}

后记

第⼀次听说熔断器模式还是在公司的tech邮件讨论组⾥，同事都在讨论⼀个故障：由于代码bug，导致请 求时间变⻓，调⽤⽅⼜不断重试，结果使整组服务崩溃。这件事过后没多久，公司的RPC框架中就增 加了熔断器机制。最近也在做motan的开源代码，想在其中增加⼀个熔断器的实现，于是翻了翻hystrix 源代码，从中学习到了不少好东⻄：线程池隔离、信号量隔离、熔断器的实现、RxJava等等。当然 hystrix的功能还不仅限于此，由于篇幅原因，还有很多内容并没有涉及到，⽐如请求缓存与上下⽂、 collapse请求合并、metrics的实现、hystrix扩展钩⼦。

# 参考资料

hystrix wiki 防雪崩利器：熔断器 Hystrix 的原理与使⽤ hystrix⽂档译⽂ 性能优化模式 Cache应⽤中的服务过载案例研究 微博：春节⽇活跃⽤户超⼀亿，探秘如何实现服务器分钟级扩容 熔断器模式
