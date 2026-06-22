- 1 Hystrix简介 Hystrix是Netflix开源的⼀款针对分布式系统的延迟和容错库，⽬的是⽤来隔离分布式服务 故障。它提供线程和信号量隔离，以减少不同服务之间资源竞争带来的相互影响；提供优 雅降级机制；提供熔断机制使得服务可以快速失败，⽽不是⼀直阻塞等待服务响应，并能 从中快速恢复。Hystrix通过这些机制来阻⽌级联失败并保证系统弹性、可⽤。下图是⼀个 典型的分布式服务实现。


⾸先，当⼤多数⼈在使⽤Tomcat时，多个HTTP服务会共享⼀个线程池，假设其中⼀个HTTP服务访问 的数据库响应⾮常慢，这将造成服务响应时间延迟增加，⼤多数线程阻塞等待数据响应返回，导致整 个Tomcat线程池都被该服务占⽤，甚⾄拖垮整个Tomcat。因此，如果我们能把不同HTTP服务隔离到 不同的线程池，则某个HTTP服务的线程池满了也不会对其他服务造成灾难性故障。这就需要线程隔离 或者信号量隔离来实现了。

使⽤线程隔离或信号隔离的⽬的是为不同的服务分配⼀定的资源，当⾃⼰的资源⽤完，直接返回失败 ⽽不是占⽤别⼈的资源。

同理，如“HTTP服务1”和“HTTP服务2”要分别访问远程的“分布式服务A”和“分布式服务B”，假设它们共 享线程池，那么其中⼀个服务在出现问题时也会影响到另⼀个服务，因此，我们需要进⾏访问隔离， 可以通过Hystrix的线程池隔离或信号量隔离来实现。

其次，“分布式服务B”依赖了“分布式服务D”和“分布式服务E”，其中“分布式服务D”是⼀个可降级的服 务，意思是出现故障时（如超时、⽹络故障）可以暂时屏蔽掉或者返回缓存脏数据，如访问商品详情 ⻚时，可以暂时屏蔽掉上边的商家信息，不会影响⽤户下单流程。

当我们依赖的服务访问超时时，要提供降级策略。⽐如，返回托底数据阻⽌级联故障。当因为⼀些故 障（如⽹络故障）使得服务可⽤率下降时，要能及时熔断，⼀是快速失败，⼆是可以保护远程分布式 服务。

到此我们⼤体了解了Hystrix是⽤来解决什么问题的。

- 1．限制调⽤分布式服务的资源使⽤，某⼀个调⽤的服务出现问题不会影响其他服务调⽤，通过线程池 隔离和信号量隔离实现。

- 2．Hystrix提供了优雅降级机制：超时降级、资源不⾜时（线程或信号量）降级，降级后可以配合降级 接⼝返回托底数据。

- 3．Hystrix也提供了熔断器实现，当失败率达到阀值⾃动触发降级（如因⽹络故障/超时造成的失败率 ⾼），熔断器触发的快速失败会进⾏快速恢复。

- 4．还提供了请求缓存、请求合并实现。


接下来，我们来看下如何使⽤Hystrix，本书使⽤的版本是Hystrix- 1.5.6。

- 2 隔离示例 以线程池隔离为示例，会为不同的服务设置不同的线程池，从⽽实现相互隔离。 为不同的HTTP服务设置不同的线程池，为不同的分布式服务调⽤设置不同的线程池。


假设我们现在要调⽤⼀个获取库存服务，通过封装⼀个命令GetStockServiceCommand来实现。 public class GetStockServiceCommand extends HystrixCommand<String> {

private StockService stockService; public GetStockServiceCommand() {

super(setter());

} private static Setter setter() {

//服务分组 HystrixCommandGroupKey groupKey = HystrixCommandGroupKey.Factory. asKey("stock"); //服务标识 HystrixCommandKey commandKey =HystrixCommandKey.Factory. asKey("getStock"); //线程池名称 HystrixThreadPoolKey threadPoolKey = HystrixThreadPoolKey.Factory. asKey("stock-pool"); //线程池配置 HystrixThreadPoolProperties.Setter threadPoolProperties =HystrixThreadPoolProperties.Setter

threadPoolProperties =HystrixThreadPoolProperties.Setter()

.withCoreSize(10)

.withKeepAliveTimeMinutes(5)

.withMaxQueueSize(Integer.MAX_VALUE)

.withQueueSizeRejectionThreshold(10000);

//命令属性配置 HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter()

.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.TH READ);

return HystrixCommand.Setter

.withGroupKey(groupKey)

.andCommandKey(commandKey)

.andThreadPoolKey(threadPoolKey)

.andThreadPoolPropertiesDefaults(threadPoolProperties) .andCommandPropertiesDefaults(commandProperties);

} @Override protectedString run() throws Exception {

return stockService.getStock(); }

} ⼏个重要组件如下。 HystrixCommandGroupKey：配置全局唯⼀标识服务分组的名称，⽐如，库存系统就是⼀个服务分 组。当我们监控时，相同分组的服务会聚合在⼀起，必填选项。 HystrixCommandKey：配置全局唯⼀标识服务的名称，⽐如，库存系统有⼀个获取库存服务，那么 就可以为这个服务起⼀个名字来唯⼀识别该服务，如果不配置，则默认是简单类名。 HystrixThreadPoolKey：配置全局唯⼀标识线程池的名称，相同线程池名称的线程池是同⼀个，如果 不配置，则默认是分组名，此名字也是线程池中线程名字的前缀。 HystrixThreadPoolProperties：配置线程池参数，coreSize配置核⼼线程池⼤⼩和线程池最⼤⼤⼩， keepAliveTimeMinutes是线程池中空闲线程⽣存时间（如果不进⾏动态配置，那么是没有任何作⽤ 的），maxQueueSize配置线程池队列最⼤⼤⼩，queueSizeRejectionThreshold限定当前队列⼤⼩， 即实际队列⼤⼩由这个参数决定，通过改变queueSizeRejectionThreshold可以实现动态队列⼤⼩调 整。 HystrixCommandProperties：配置该命令的⼀些参数，如executionIsolationStrategy配置执⾏隔离 策略，默认是使⽤线程隔离，此处我们配置为THREAD，即线程池隔离。

此处可以粗粒度实现隔离，也可以细粒度实现隔离，如下所示。 服务分组+线程池：粗粒度实现，⼀个服务分组/系统配置⼀个隔离线程池即可，不配置线程池名称或者 相同分组的线程池名称配置为⼀样。 服务分组+服务+线程池：细粒度实现，⼀个服务分组中的每⼀个服务配置⼀个隔离线程池，为不同的 命令实现配置不同的线程池名称即可。 混合实现：⼀个服务分组配置⼀个隔离线程池，然后对重要服务单独设置隔离线程池。

如上配置是在应⽤启动时就配置好了，在实际运⾏过程中，我们可能随时调整其中⼀些参数，如线程 池⼤⼩、队列⼤⼩，此时，可以使⽤如下⽅式进⾏动态配置。 String dynamicQueueSizeRejectionThreshold = "hystrix.threadpool."+ "stock-pool" + ".queueSizeRejectionThreshold"; Configuration configuration = ConfigurationManager.getConfigInstance(); configuration.setProperty(dynamicQueueSizeRejectionThreshold,100);

如果是改变线程池配置，则是"hystrix.threadpool."+ threadPoolKey + propertyName；如果是改变命令 属性配置，则是"hystrix.command." + commandKey + propertyName。

接下来就可以通过如下⽅式创建命令。 GetStockServiceCommand command = new GetStockServiceCommand(newStockService());

然后通过如下⽅式同步调⽤。 String result = command.execute();

或者返回Future从⽽实现异步调⽤。 Future<String> future = command.queue();

或者配合RxJava实现响应式编程。 Observable<String> observe =command.observe(); observe.asObservable().subscribe((result) -> {

System.out.println(result); });

在应⽤Hystrix时，⾸先需要把服务封装成HystrixCommand，即命令模式实现，然后就可以通过同步/ 异步/响应式模式来调⽤服务。

信号量隔离通过如下配置即可。 HystrixCommandProperties.Setter commandProperties= HystrixCommandProperties.Setter()

.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHOR E)

.withExecutionIsolationSemaphoreMaxConcurrentRequests(50);

信号量隔离只是限制了总的并发数，服务使⽤主线程进⾏同步调⽤，即没有线程池。因此，如果只是 想限制某个服务的总并发调⽤量或者调⽤的服务不涉及远程调⽤的话，可以使⽤轻量级的信号量来实 现。

GetStockServiceCommand不是单例，不能重⽤，必须每次使⽤创建⼀个。如果觉得Hystrix太麻烦或 者太重，则可以参考Hystrix思路设计⾃⼰的组件。

