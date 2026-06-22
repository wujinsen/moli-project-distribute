java.util.concurrent

类 Executors

java.lang.Object

继承者 java.util.concurrent.Executors

此类是个⼯具类，它提供对Executor、ExecutorService、ScheduledExecutorService、 ThreadFactory 和 Callable 类的⼀些实⽤⽅法。

此类⽀持以下各种⽅法：

- * 创建并返回设置有常⽤配置的ExecutorService的⽅法。

- * 创建并返回设置有常⽤配置的ScheduledExecutorService 的⽅法。

- * 创建并返回“包装的”ExecutorService ⽅法，它使特定于实现的⽅法不可访问，只让


ExecutorService接⼝的⽅法可⽤。

- * 创建并返回 ThreadFactory 的⽅法，它可将新创建的线程设置为已知的状态。

- * 创建并返回⾮闭包形式的 Callable 的⽅法，这样可将其⽤于需要 Callable 的执⾏⽅法中。


主要⽅法：

public static ExecutorService newFixedThreadPool(int nThreads)

创建⼀个可重⽤固定线程数的线程池，以共享的⽆界队列⽅式来运⾏这些线程。

在任意点，在⼤多数 nThreads 线程会处于处理任务的活动状态。如果在所有线程处于活动状 态时提交附加任务，

则在有可⽤线程之前，附加任务将在队列中等待。如果在关闭前的执⾏期间由于失败⽽导致任 何线程终⽌，

那么⼀个新线程将代替它执⾏后续的任务（如果需要）。在某个线程被显式地关闭之前，池中 的线程将⼀直存在。

参数：

nThreads - 池中的线程数

返回：

新创建的线程池

抛出：

IllegalArgumentException - 如果 nThreads <= 0

注意：它的全是core线程。其源码如下：

return new ThreadPoolExecutor(nThreads, nThreads,0L, TimeUnit.MILLISECONDS,new Linked BlockingQueue<Runnable>());

public static ExecutorService newFixedThreadPool(int nThreads,ThreadFactory threadFactory)

创建⼀个可重⽤固定线程数的线程池，以共享的⽆界队列⽅式来运⾏这些线程，在需要时使⽤ 提供的 ThreadFactory 创建新线程。在任意点，在⼤多数 nThreads 线程会处于处理任务的活动状 态。如果在所有线程处于活动状态时提交附加任务，则在有可⽤线程之前，附加任务将在队列中 等待。如果在关闭前的执⾏期间由于失败⽽导致任何线程终⽌，那么⼀个新线程将代替它执⾏后 续的任务（如果需要）。在某个线程被显式地关闭之前，池中的线程将⼀直存在。

参数：

nThreads - 池中的线程数

threadFactory - 创建新线程时使⽤的⼯⼚

返回：

新创建的线程池

抛出：

NullPointerException - 如果 threadFactory 为 null

IllegalArgumentException - 如果 nThreads <= 0

public static ExecutorService newSingleThreadExecutor()

创建⼀个使⽤单个 worker 线程的 Executor，以⽆界队列⽅式来运⾏该线程。

（注意，如果因为在关闭前的执⾏期间出现失败⽽终⽌了此单个线程，那么如果需要，⼀个新 线程将代替它执⾏后续的任务）。

可保证顺序地执⾏各个任务，并且在任意给定的时间不会有多个线程是活动的。

与其他等效的 newFixedThreadPool(1) 不同，可保证不能对ThreadPoolExecutor重新进⾏配置 来使⽤更多的线程。

返回：

新创建的单线程 Executor

- 注意1:newSingleThreadExecutor与newFixedThreadPool(1)不同之出在于：


newSingleThreadExecutor返回的ExcutorService在析构函数finalize()会调⽤shutdown()，即如果 我们没有对它调⽤shutdown()，那么可以确保它在被回收时调⽤shutdown()来终⽌线程。

- 注意2:源码如下： public static ExecutorService newSingleThreadExecutor() {


return new FinalizableDelegatedExecutorService

(new ThreadPoolExecutor(1, 1,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue <Runnable>()));

}

public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory)

创建⼀个使⽤单个 worker 线程的 Executor，以⽆界队列⽅式来运⾏该线程，并在需要时使⽤ 提供的 ThreadFactory 创建新线程。与其他等效的 newFixedThreadPool(1, threadFactory) 不 同，可保证不能对ThreadPoolExecutor重新进⾏配置来使⽤更多的线程。

参数：

threadFactory - 创建新线程时使⽤的⼯⼚

返回：

新创建的单线程 Executor

抛出：

NullPointerException - 如果 threadFactory 为 null

注意：newSingleThreadExecutor返回的ExcutorService在析构函数finalize()会调⽤shutdown()， 即如果我们没有对它调⽤shutdown()，那么可以确保它在被回收时调⽤shutdown()来终⽌线程。

public static ExecutorService newCachedThreadPool()

创建⼀个可根据需要创建新线程的线程池，但是在以前构造的线程可⽤时将重⽤它们。对于执 ⾏很多短期异步任务的程序⽽⾔，

这些线程池通常可提⾼程序性能。调⽤ execute 将重⽤以前构造的线程（如果线程可⽤）。

如果现有线程没有可⽤的，则创建⼀个新线程并添加到池中。终⽌并从缓存中移除那些已 有 60 秒钟未被使⽤的线程。

因此，⻓时间保持空闲的线程池不会使⽤任何资源。

注意，可以使⽤ ThreadPoolExecutor 构造⽅法创建具有类似属性但细节不同（例如超时参数） 的线程池。

返回：

新创建的线程池

注意1：它没有core线程。源码如下：

public static ExecutorService newCachedThreadPool() {

return new ThreadPoolExecutor(0, Integer.MAX_VALUE,60L, TimeUnit.SECONDS,new Synchro nousQueue<Runnable>());

}

public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory)

创建⼀个可根据需要创建新线程的线程池，但是在以前构造的线程可⽤时将重⽤它们，并在需 要时使⽤提供的 ThreadFactory 创建新线程。

参数：

threadFactory - 创建新线程时使⽤的⼯⼚

返回：

新创建的线程池

抛出：

NullPointerException - 如果 threadFactory 为 null

public static ScheduledExecutorService newSingleThreadScheduledExecutor()

创建⼀个单线程执⾏程序，它可安排在给定延迟后运⾏命令或者定期地执⾏。

（注意，如果因为在关闭前的执⾏期间出现失败⽽终⽌了此单个线程，那么如果需要，⼀个新 线程会代替它执⾏后续的任务）。

可保证顺序地执⾏各个任务，并且在任意给定的时间不会有多个线程是活动的。

与其他等效的 newScheduledThreadPool(1) 不同，可保证不能对 ScheduledThreadPoolExecutor重新进⾏配置来使⽤更多的线程。

返回：

新创建的安排执⾏程序

- 注意1：newSingleThreadScheduledExecutor与newScheduledThreadPool(1)不同之出在于：

newSingleThreadScheduledExecutor在析构函数finalize()会调⽤shutdown()，即如果我们没有对 它调⽤shutdown()，那么可以确保它在被回收时调⽤shutdown()来终⽌线程。

源码如下：public static ScheduledExecutorService newSingleThreadScheduledExecutor() {

return new DelegatedScheduledExecutorService

(new ScheduledThreadPoolExecutor(1));

}

- 注意2：这⾥的ScheduledThreadPoolExecutor是core线程固定，且只有core线程，它的队列是⽆ 界的。


public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory thr eadFactory)

创建⼀个单线程执⾏程序，它可安排在给定延迟后运⾏命令或者定期地执⾏。

（注意，如果因为在关闭前的执⾏期间出现失败⽽终⽌了此单个线程，那么如果需要，⼀个新 线程会代替它执⾏后续的任务）。

可保证顺序地执⾏各个任务，并且在任意给定的时间不会有多个线程是活动的。与其他等效 的 newScheduledThreadPool(1, threadFactory) 不同，可保证不能对 ScheduledThreadPoolExecutor重新进⾏配置来使⽤更多的线程。

参数：

threadFactory - 创建新线程时使⽤的⼯⼚

返回：

新创建的安排执⾏程序

抛出：

NullPointerException - 如果 threadFactory 为 null

public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize)

创建⼀个线程池，它可安排在给定延迟后运⾏命令或者定期地执⾏。

参数：

corePoolSize - 池中所保存的线程数，即使线程是空闲的也包括在内。

返回：

新创建的安排线程池

抛出：

NullPointerException - 如果 threadFactory 为 null

public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, ThreadFac tory threadFactory)

创建⼀个线程池，它可安排在给定延迟后运⾏命令或者定期地执⾏。

参数：

corePoolSize - 池中所保存的线程数，即使线程是空闲的也包括在内

threadFactory - 执⾏程序创建新线程时使⽤的⼯⼚

返回：

新创建的安排线程池

抛出：

IllegalArgumentException - 如果 corePoolSize < 0

NullPointerException - 如果 threadFactory 为 null

public static ExecutorService unconfigurableExecutorService(ExecutorService executor)

返回⼀个将所有已定义的 ExecutorService ⽅法委托给指定执⾏程序的对象，这样就⽆法使⽤ 强制转换来访问其他的⽅法。

这提供了⼀种可安全地“冻结”配置并且不允许调整给定具体实现的⽅法。

参数：

executor - 底层实现

返回：

⼀个 ExecutorService 实例

抛出：

NullPointerException - 如果 executor 为 null

注意：它的⽬的是只暴露ExecutorService接⼝⽅法，使特定于实现的⽅法不可访问。它是通过⼀ 个类来包装executor来实现的，该类实现了ExecutorService接⼝。具体来说只是调⽤executor的 相应函数。具体可以查阅源码。

public static ScheduledExecutorService unconfigurableScheduledExecutorService(ScheduledEx ecutorService executor)

返回⼀个将所有已定义的 ExecutorService ⽅法委托给指定执⾏程序的对象，这样就⽆法使⽤ 强制转换来访问其他的⽅法。。这提供了⼀种可安全地“冻结”配置并且不允许调整给定具体实现的 ⽅法。

参数：

executor - 底层实现

返回：

⼀个 ScheduledExecutorService 实例

抛出：

NullPointerException - 如果 executor 为 null

注意：其⽬的和unconfigurableExecutorService相似。

public static ThreadFactory defaultThreadFactory()

返回⽤于创建新线程的默认线程⼯⼚。此⼯⼚创建同⼀ ThreadGroup 中 Executor 使⽤的所有 新线程。

如果有 SecurityManager，则它使⽤ System.getSecurityManager() 组来调⽤ 此 defaultThreadFactory ⽅法，其他情况则使⽤线程组。

每个新线程都作为⾮守护程序⽽创建，并且具有设置为 Thread.NORM_PRIORITY 中较⼩者的 优先级以及线程组中允许的最⼤优先级。

新线程具有可通过 pool-N-thread-M 的 Thread.getName() 来访问的名称，其中 N 是此⼯⼚的 序列号，M 是此⼯⼚所创建线程的序列号。

返回：

线程⼯⼚

public static ThreadFactory privilegedThreadFactory()

返回⽤于创建新线程的线程⼯⼚，这些新线程与当前线程具有相同的权限。此⼯⼚创建具有 与 defaultThreadFactory() 相同设置的线程，

新线程的 AccessControlContext 和 contextClassLoader 的其他设置与调⽤ 此 privilegedThreadFactory ⽅法的线程相同。可以 在 AccessController.doPrivileged(java.security.PrivilegedAction) 操作中创建⼀个 新 privilegedThreadFactory，设置当前线程的访问控制上下⽂，以便创建具有该操作中保持的所 选权限的线程。

注意，虽然运⾏在此类线程中的任务具有与当前线程相同的访问控制和类加载器，但是它们⽆ 需具有相同的 ThreadLocal

或 InheritableThreadLocal 值。如有必要，使 ⽤ ThreadPoolExecutor.beforeExecute(java.lang.Thread, java.lang.Runnable)

在 ThreadPoolExecutor ⼦类中运⾏任何任务前，可以设置或重置线程局部变量的特定值。

另外，如果必须初始化 worker 线程，以具有与某些其他指定线程相同 的 InheritableThreadLocal 设置，

则可以在线程等待和服务创建请求的环境中创建⾃定义的 ThreadFactory，⽽不是继承其值。

返回：

线程⼯⼚

抛出：

AccessControlException - 如果当前访问控制上下⽂没有获取和设置上下⽂类加载器的权 限。

public static <T> Callable<T> callable(Runnable task,T result)

返回 Callable 对象，调⽤它时可运⾏给定的任务并返回给定的结果。这在把需要 Callable 的⽅ 法应⽤到其他⽆结果的操作时很有⽤。

参数：

task - 要运⾏的任务

result - 返回的结果

返回：

⼀个 callable 对象

抛出：

NullPointerException - 如果 task 为 null

public static Callable<Object> callable(Runnable task)

返回 Callable 对象，调⽤它时可运⾏给定的任务并返回 null。

参数：

task - 要运⾏的任务

返回：

⼀个 callable 对象

抛出：

NullPointerException - 如果 task 为 null

public static Callable<Object> callable(PrivilegedAction<?> action)

返回 Callable 对象，调⽤它时可运⾏给定特权的操作并返回其结果。

参数：

action - 要运⾏的特权操作

返回：

⼀个 callable 对象

抛出：

NullPointerException - 如果 action 为 null

