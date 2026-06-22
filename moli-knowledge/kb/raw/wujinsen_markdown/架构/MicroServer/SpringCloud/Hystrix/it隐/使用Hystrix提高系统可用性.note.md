今天稍微复杂点的互联⽹应⽤，服务端基本都是分布式的，⼤量的服务⽀撑起整个系统，服务之间也 难免有⼤量的依赖关系，依赖都是通过⽹络连接起来。

（图⽚来源：https://github.com/Netflix/Hystrix/wiki） 然⽽任何⼀个服务的可⽤性都不是 100% 的，⽹络亦是脆弱的。当我依赖的某个服务不可⽤的时候， 我⾃身是否会被拖死？当⽹络不稳定的时候，我⾃身是否会被拖死？这些在单机环境下不太需要考虑 的问题，在分布式环境下就不得不考虑了。假设我有5个依赖的服务，他们的可⽤性都是99.95%，即 ⼀年不可⽤时间约为4个多⼩时，那么是否意味着我的可⽤性最多就是 99.95% 的5次⽅，99.75%（近 乎⼀天），再加上⽹络不稳定因素、依赖服务可能更多，可⽤性会更低。考虑到所依赖的服务必定会 在某些时间不可⽤，考虑到⽹络必定会不稳定，我们应该怎么设计⾃身服务？即，怎么为出错设计？ Michael T. Nygard 在在精彩的 ⼀书中总结了很多提⾼系统可⽤性的模式，其中我认为 ⾮常重要的两条是：

《Release It!》

- 1.
- 2.


使⽤超时 使⽤断路器

第⼀条，通过⽹络调⽤外部依赖服务的时候，都必须应该设置超时。在健康的情况下，⼀般局域往的 ⼀次远程调⽤在⼏⼗毫秒内就返回了，但是当⽹络拥堵的时候，或者所依赖服务不可⽤的时候，这个 时间可能是好多秒，或者压根就僵死了。通常情况下，⼀次远程调⽤对应了⼀个线程或者进程，如果 响应太慢，或者僵死了，那⼀个进程/线程，就被拖死，短时间内得不到释放，⽽进程/线程都对应了系 统资源，这就等于说我⾃身服务资源会被耗尽，导致⾃身服务不可⽤。假设我的服务依赖于很多服 务，其中⼀个⾮核⼼的依赖如果不可⽤，⽽且没有超时机制，那么这个⾮核⼼依赖就能拖死我的服 务，尽管理论上即使没有它我在⼤部分情况还能健康运转的。 断路器其实我们⼤家都不陌⽣（你会换保险丝么？），如果你家没有断路器，当电流过载，或者短路 的时候，电路不断开，电线就会升温，造成⽕灾，烧掉房⼦。有了断路器之后，电流过载的时候，保 险丝就会⾸先烧掉，断开电路，不⾄于引起更⼤的灾难（只不过这个时候你得换保险丝）。

<table>
  <tr>
    <th>![image 1](<使用Hystrix提高系统可用性.note_images/imageFile1.png>)</th>
  </tr>
</table>


# cb

当我们的服务访问某项依赖有⼤量超时的时候，再让新的请求去访问已经没有太⼤意义，那只会⽆谓 的消耗现有资源。即使你已经设置超时1秒了，那明知依赖不可⽤的情况下再让更多的请求，⽐如100 个，去访问这个依赖，也会导致100个线程1秒的资源浪费。这个时候，断路器就能帮助我们避免这种 资源浪费，在⾃身服务和依赖之间放⼀个断路器，实时统计访问的状态，当访问超时或者失败达到某 个阈值的时候（如50%请求超时，或者连续20次请失败），就打开断路器，那么后续的请求就直接返 回失败，不⾄于浪费资源。断路器再根据⼀个时间间隔（如5分钟）尝试关闭断路器（或者更换保险 丝），看依赖是否恢复服务了。 超时机制和断路器能够很好的保护我们的服务，不受依赖服务不可⽤的影响太⼤。然⽽具体实现这两 个模式还是有⼀定的复杂度的，所幸 Netflix 开源的 帮我们⼤⼤简化了超时机制和断路器 的实现。

Hystrix框架

<table>
  <tr>
    <th>![image 2](<使用Hystrix提高系统可用性.note_images/imageFile2.png>)</th>
  </tr>
</table>


# hystrix-logo

先上POM依赖： com.netflix.hystrix hystrix-core 1.3.13

使⽤Hystrix，需要通过Command封装对远程依赖的调⽤： public clas ComandHeloWorld extends HystrixComand {

public ComandHeloWorld() {

super(HystrixComandGroupKey.Factory.asKey("ExampleGroup"); }

@Overide protected String run() {

return dependencyService.cal(); /this cal may timeout or block }

} 然后在需要的时候调⽤这个Command： String s = new ComandHeloWorld().execute(); 上述是同步调⽤，当然如果业务逻辑允许且更追求性能，或许可以选择异步调⽤： Observable observable = new ComandHeloWorld().observe(); observable.subscribe(new Observer() {

@Overide public void onCompleted() {

/ do nothing }

@Overide public void onEror(Throwable e) {

e.printStackTrace(); }

@Overide public void onNext(String v) {

/ do nothing }

});

该例中，不论 dependencyService.call() ⾃身有没有超时机制（可能你会发现很多远程调⽤接⼝⾃身并 没有给你提供超时机制），⽤ HystrixCommand 封装过后，超时是强制的，默认超时时间是1秒，当然 你可以根据需要⾃⼰在构造函数中调节 Command 的超时时间，例如说2秒： super(Seter

.withGroupKey(HystrixComandGroupKey.Factory.asKey("ExampleGroup")

.andComandPropertiesDefaults(HystrixComandProperties.Seter().withExecutionIsolationThrea dTimeoutInMiliseconds(2 0)

); 当Hystrix执⾏命令超时后，它会抛出如下的异常： com.netflix.hystrix.exception.HystrixRuntimeException: MyComand timed-out and no falback available.

at

com.netflix.hystrix.HystrixComand.getFalbackOrThrowException(HystrixComand.java:164) at com.netflix.hystrix.HystrixComand.aces$190(HystrixComand.java:98) at

- com.netflix.hystrix.HystrixComand$TimeoutObservable$1$1.run(HystrixComand.java:1019) at

com.netflix.hystrix.strategy.concurency.HystrixContextRunable$1.cal(HystrixContextRunable.ja va:41)

at com.netflix.hystrix.strategy.concurency.HystrixContextRunable$1.cal(HystrixContextRunable.ja va:37)

at com.netflix.hystrix.strategy.concurency.HystrixContextRunable.run(HystrixContextRunable.java: 57)

at

- com.netflix.hystrix.HystrixComand$TimeoutObservable$1$2.tick(HystrixComand.java:1043) at com.netflix.hystrix.util.HystrixTimer$1.run(HystrixTimer.java:101) at java.util.concurent.Executors$RunableAdapter.cal(Executors.java:439)


. Caused by: java.util.concurent.TimeoutException

. 15 more 注意异常信息中包含“MyCommand timed-out and no fallback available.”，也就是说 Hystrix 执⾏命令 超时或者失败之后，是会尝试去调⽤⼀个 fallback 的，这个 fallback 即⼀个备⽤⽅案，要为 HystrixCommand 提供 fallback，只要重写 protected String getFallback() ⽅法即可。

⼀般情况下，Hystrix 会为 Command 分配专⻔的线程池，池中的线程数量是固定的，这也是⼀个保护 机制，假设你依赖很多个服务，你不希望对其中⼀个服务的调⽤消耗过多的线程以致于其他服务都没 线程调⽤了。默认这个线程池的⼤⼩是10，即并发执⾏的命令最多只能有是个了，超过这个数量的调 ⽤就得排队，如果队伍太⻓了（默认超过5），Hystrix就⽴刻⾛ fallback 或者抛异常。 根据你的具体需要，你可能会想要调整某个Command的线程池⼤⼩，例如你对某个依赖的调⽤平均响 应时间为200ms，⽽峰值的QPS是200，那么这个并发⾄少就是 0.2 x 200 = 40 ( )，考虑到 ⼀定的宽松度，这个线程池的⼤⼩设置为60可能⽐较合适： super(Seter

Little’s Law

.withGroupKey(HystrixComandGroupKey.Factory.asKey("ExampleGroup")

.andThreadPolPropertiesDefaults(HystrixThreadPolProperties.Seter().withCoreSize(60) );

说了这么多，还没提到Hystrix的断路器，其实对于使⽤者来说，断路器机制默认是启⽤的，但是编程 接⼝默认⼏乎不需要关⼼这个，机制和前⾯讲的也差不多，Hystrix会统计命令调⽤，看其中失败的⽐ 例，默认当超过50%失败后，开启断路器，那之后⼀段时间的命令调⽤直接返回失败（或者⾛

fallback），5秒之后，Hystrix再尝试关闭断路器，看看请求是否能正常响应。下⾯的⼏⾏Hystrix源码 展示了它如何统计失败率的： long totalCount = failure + suces + timeout + threadPolRejected + shortCircuited + semaphoreRejected;

long erorCount = failure + timeout + threadPolRejected + shortCircuited + semaphoreRejected; int erorPercentage = 0;

if (totalCount > 0) { erorPercentage = (int)(double) erorCount / totalCount * 10);

} 其中 failure 表示命令本身发⽣错误、success ⾃然不必说，timeout 是超时、threadPoolRejected 表示 当线程池满后拒绝的命令调⽤、shortCircuited 表示断路器打开后拒绝的命令调⽤， semaphoreRejected 使⽤信号量机制（⽽不是线程池）拒绝的命令调⽤。 本⽂并不打算完整地介绍 Hystrix，这⾥只是介绍了为什么要⽤ Hystrix 以及使⽤它需要关⼼的⼀些基 本核⼼概念，Hystrix 是 Netflix 的核⼼中间件，在保证他们系统可⽤性上起到了⾮常核⼼的作⽤，它还 有更多的功能都在⽂档完整地介绍了： ，其中最重要的⽽且本⽂ 没有介绍的，可能就是监控功能了，当系统⾜够复杂，相互依赖错综发杂的时候，快速定位到故障 点，是运维⾮常关⼼的问题。

https://github.com/Netflix/Hystrix/wiki

