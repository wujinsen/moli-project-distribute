写在前⾯

⾸先感谢⼤家在盖楼的间隙阅读本篇⽂章，通过阅读本篇⽂章你将了解到：

线程池的定义 Executors创建线程池的⼏种⽅式 ThreadPoolExecutor对象 线程池执⾏任务逻辑和线程池参数的关系 Executors创建返回ThreadPoolExecutor对象 OOM异常测试 如何定义线程池参数

如果只想知道原因可以直接拉到总结那

线程池的定义

管理⼀组⼯作线程。通过线程池复⽤线程有以下⼏点优点：

减少资源创建 => 减少内存开销，创建线程占⽤内存 降低系统开销 => 创建线程需要时间，会延迟处理的请求 提⾼稳定稳定性 => 避免⽆限创建线程引起的OutOfMemoryError【简称OOM】

Executors创建线程池的⽅式

根据返回的对象类型创建线程池可以分为三类：

创建返回ThreadPoolExecutor对象 创建返回ScheduleThreadPoolExecutor对象 创建返回ForkJoinPool对象

本⽂只讨论创建返回ThreadPoolExecutor对象

ThreadPoolExecutor对象

在介绍Executors创建线程池⽅法前先介绍⼀下ThreadPoolExecutor，因为这些创建线程池的静态⽅法 都是返回ThreadPoolExecutor对象，和我们⼿动创建ThreadPoolExecutor对象的区别就是我们不需要 ⾃⼰传构造函数的参数。 ThreadPoolExecutor的构造函数共有四个，但最终调⽤的都是同⼀个：

public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler)

构造函数参数说明：

corePoolSize => 线程池核⼼线程数量 maximumPoolSize => 线程池最⼤数量 keepAliveTime => 空闲线程存活时间 unit => 时间单位 workQueue => 线程池所使⽤的缓冲队列 threadFactory => 线程池创建线程使⽤的⼯⼚

handler => 线程池对拒绝任务的处理策略

线程池执⾏任务逻辑和线程池参数的关系

![image 1](<为什么阿里巴巴要禁用 Executors 创建线程池？.note_images/imageFile1.png>)

执⾏逻辑说明：

判断核⼼线程数是否已满，核⼼线程数⼤⼩和corePoolSize参数有关，未满则创建线程执⾏任务 若核⼼线程池已满，判断队列是否满，队列是否满和workQueue参数有关，若未满则加⼊队列中 若队列已满，判断线程池是否已满，线程池是否已满和maximumPoolSize参数有关，若未满创建线 程执⾏任务 若线程池已满，则采⽤拒绝策略处理⽆法执执⾏的任务，拒绝策略和handler参数有关

Executors创建返回ThreadPoolExecutor对象

Executors创建返回ThreadPoolExecutor对象的⽅法共有三种：

Executors#newCachedThreadPool => 创建可缓存的线程池 Executors#newSingleThreadExecutor => 创建单线程的线程池 Executors#newFixedThreadPool => 创建固定⻓度的线程池

# Executors#newCachedThreadPool⽅法

public static ExecutorService newCachedThreadPool() {

return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

}

CachedThreadPool是⼀个根据需要创建新线程的线程池

corePoolSize => 0，核⼼线程池的数量为0 maximumPoolSize => Integer.MAX_VALUE，可以认为最⼤线程数是⽆限的 keepAliveTime => 60L unit => 秒 workQueue => SynchronousQueue

当⼀个任务提交时，corePoolSize为0不创建核⼼线程，SynchronousQueue是⼀个不存储元素的队 列，可以理解为队⾥永远是满的，因此最终会创建⾮核⼼线程来执⾏任务。

对于⾮核⼼线程空闲60s时将被回收。因为Integer.MAX_VALUE⾮常⼤，可以认为是可以⽆限创建线程 的，在资源有限的情况下容易引起OOM异常 Executors#newSingleThreadExecutor⽅法

public static ExecutorService newSingleThreadExecutor() { return new FinalizableDelegatedExecutorService

(new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()));

}

SingleThreadExecutor是单线程线程池，只有⼀个核⼼线程

corePoolSize => 1，核⼼线程池的数量为1 maximumPoolSize => 1，只可以创建⼀个⾮核⼼线程 keepAliveTime => 0L unit => 秒 workQueue => LinkedBlockingQueue

当⼀个任务提交时，⾸先会创建⼀个核⼼线程来执⾏任务，如果超过核⼼线程的数量，将会放⼊队列 中，因为LinkedBlockingQueue是⻓度为Integer.MAX_VALUE的队列，可以认为是⽆界队列，因此往 队列中可以插⼊⽆限多的任务，在资源有限的时候容易引起OOM异常，同时因为⽆界队列，

maximumPoolSize和keepAliveTime参数将⽆效，压根就不会创建⾮核⼼线程 Executors#newFixedThreadPool⽅法

public static ExecutorService newFixedThreadPool(int nThreads) {

return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

}

FixedThreadPool是固定核⼼线程的线程池，固定核⼼线程数由⽤户传⼊

corePoolSize => 1，核⼼线程池的数量为1 maximumPoolSize => 1，只可以创建⼀个⾮核⼼线程 keepAliveTime => 0L unit => 秒 workQueue => LinkedBlockingQueue 它和SingleThreadExecutor类似，唯⼀的区别就是核⼼线程数不同，并且由于使⽤的是 LinkedBlockingQueue，在资源有限的时候容易引起OOM异常

总结：

FixedThreadPool和SingleThreadExecutor => 允许的请求队列⻓度为Integer.MAX_VALUE，可能会 堆积⼤量的请求，从⽽引起OOM异常 CachedThreadPool => 允许创建的线程数为Integer.MAX_VALUE，可能会创建⼤量的线程，从⽽ 引起OOM异常

这就是为什么禁⽌使⽤Executors去创建线程池，⽽是推荐⾃⼰去创建ThreadPoolExecutor的原因

OOM异常测试

理论上会出现OOM异常，必须测试⼀波验证之前的说法： 测试类：TaskTest.java

public class TaskTest {

public static void main(String[] args) { ExecutorService es = Executors.newCachedThreadPool(); int i = 0; while (true) {

es.submit(new Task(i++)); }

} }

使⽤Executors创建的CachedThreadPool，往线程池中⽆限添加线程 在启动测试类之前先将JVM内存调整⼩⼀点，不然很容易将电脑跑出问题【别问我为什么知道，是铁 憨憨甜没错了！！！】，在idea⾥：Run -> Edit Configurations

![image 2](<为什么阿里巴巴要禁用 Executors 创建线程池？.note_images/imageFile2.png>)

JVM参数说明：

- -Xms10M => Java Heap内存初始化值

- -Xmx10M => Java Heap内存最⼤值


运⾏结果：

Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "main" Disconnected from the target VM, address: '127.0.0.1:60416', transport: 'socket'

创建到3w多个线程的时候开始报OOM错误 另外两个线程池就不做测试了，测试⽅法⼀致，只是创建的线程池不⼀样

如何定义线程池参数

CPU密集型 => 线程池的⼤⼩推荐为CPU数量 + 1，CPU数量可以根据Runtime.availableProcessors⽅ 法获取 IO密集型 => CPU数量 * CPU利⽤率 * (1 + 线程等待时间/线程CPU时间)

混合型 => 将任务分为CPU密集型和IO密集型，然后分别使⽤不同的线程池去处理，从⽽使每个线程 池可以根据各⾃的⼯作负载来调整 阻塞队列 => 推荐使⽤有界队列，有界队列有助于避免资源耗尽的情况发⽣ 拒绝策略 => 默认采⽤的是AbortPolicy拒绝策略，直接在程序中抛出RejectedExecutionException异常 【因为是运⾏时异常，不强制catch】，这种处理⽅式不够优雅。处理拒绝策略有以下⼏种⽐较推荐：

在程序中捕获RejectedExecutionException异常，在捕获异常中对任务进⾏处理。针对默认拒 绝策略

使⽤CalerRunsPolicy拒绝策略，该策略会将任务交给调⽤execute的线程执⾏【⼀般为主线 程】，此时主线程将在⼀段时间内不能提交任何任务，从⽽使⼯作线程处理正在执⾏的任 务。此时提交的线程将被保存在TCP队列中，TCP队列满将会影响客户端，这是⼀种平缓的性 能降低

⾃定义拒绝策略，只需要实现RejectedExecutionHandler接⼝即可

如果任务不是特别重要，使⽤DiscardPolicy和DiscardOldestPolicy拒绝策略将任务丢弃也是 可以的

如果使⽤Executors的静态⽅法创建ThreadPoolExecutor对象，可以通过使⽤Semaphore对任务的执⾏ 进⾏限流也可以避免出现OOM异常 由于线程池参数定义经验较少，都是理论知识，欢迎有经验的⼤佬补充

