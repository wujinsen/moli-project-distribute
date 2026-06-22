ExecutorService 建⽴多线程的步骤：

<table>
  <tr>
    <th>1。定义线程类</th>
    <th>clas Handler implements Runable{</th>
  </tr>
  <tr>
    <td>2。建⽴ExecutorService线程池</td>
    <td>}<br><br>xecutorService executorService = Executors.newCachedThreadPol(); 或者<br><br>/获取当前系统的CPU 数⽬ int cpuNums = Runtime.getRuntime().availableProcesors();<br><br>/ExecutorService通常根据系统资源情况灵活定 义线程池⼤⼩ ExecutorService executorService<br><br>=Executors.newFixedThreadPol(cpuNums * POL_SIZE);</td>
  </tr>
  <tr>
    <td>3。调⽤线程池操作</td>
    <td>循环操作，成为daemon,把新实例放⼊Executor 池中<br><br>while(true){<br><br>executorService.execute(new Handler(socket);<br><br>/ clas Handler implements Runable{ 或者 executorService.execute(createTask(i);<br><br>/private static Runable createTask(final int taskID)<br><br>}<br><br>execute(Runable对象)⽅法 其实就是对Runable对象调⽤start()⽅法 （当然还有⼀些其他后台动作，⽐如队列，优先<br><br>激活等）</td>
  </tr>
</table>


级，IDLE timeout，active

⼏种不同的ExecutorService线程池对象

<table>
  <tr>
    <th>1.newCachedThreadPol()</th>
    <th>-缓存型池⼦，先查看池中有没有以前建⽴的线 程，如果有，就reuse.如果没有，就建⼀个新的 线程加⼊池中<br>-缓存型池⼦通常⽤于执⾏⼀些⽣存期很短的异步 型任务<br><br>因此在⼀些⾯向连接的daemon型SERVER中⽤得 不多。<br><br>-能reuse的线程，必须是timeout IDLE内的池中 线程，缺省timeout是60s,超过这个IDLE时⻓，线 程实例将被终⽌及移出池。<br><br><br>注意，放⼊CachedThreadPol的线程不必担⼼ 其结束，超过TIMEOUT不活动，其会⾃动被终 ⽌。</th>
  </tr>
  <tr>
    <td>2. newFixedThreadPol</td>
    <td>-newFixedThreadPol与cacheThreadPol差不 多，也是能reuse就⽤，但不能随时建新的线程<br>-其独特之处:任意时间点，最多只能有固定数⽬的 活动线程存在，此时如果有新的线程要建⽴，只 能放在另外的队列中等待，直到当前的线程中某 个线程终⽌直接被移出池⼦<br>-和cacheThreadPol不同，FixedThreadPol没 有IDLE机制（可能也有，但既然⽂档没提，肯定 ⾮常⻓，类似依赖上层的TCP或UDP IDLE机制之 类的），所以FixedThreadPol多数针对⼀些很稳 定很固定的正规并发线程，多⽤于服务器<br>-从⽅法的源代码看，cache池和fixed 池调⽤的是 同⼀个底层池，只不过参数不同: fixed池线程数固定，并且是0秒IDLE（⽆IDLE） cache池线程数⽀持0-Integer.MAX_VALUE(显然<br></td>
  </tr>
  <tr>
    <td>3.ScheduledThreadPol</td>
    <td>完全没考虑主机的资源承受能⼒），60秒IDLE<br><br>-调度型线程池<br>-这个池⼦⾥的线程可以按schedule依次delay执 ⾏，或周期执⾏<br></td>
  </tr>
  <tr>
    <td>4.SingleThreadExecutor</td>
    <td>-单例线程，任意时间池中只能有⼀个线程<br>-⽤的是和cache池和fixed池相同的底层池，但线 ）<br></td>
  </tr>
</table>


程数⽬是1-1,0秒IDLE（⽆IDLE

上⾯四种线程池，都使⽤Executor的缺省线程⼯⼚建⽴线程，也可单独定义⾃⼰的线程⼯⼚ 下⾯是缺省线程⼯⼚代码:

<table>
  <tr>
    <th>static clas DefaultThreadFactory implements ThreadFactory { static final AtomicInteger polNumber = new AtomicInteger(1); a ThreadGroup group;<br><br>ina AtomicInteger threadNumber = new AtomicInteger(1); final String namePrefix; DefaultThreadFactory() {<br><br>SecurityManager s = System.getSecurityManager(); group = (s != nul)? s.getThreadGroup() :Thread.curentThread().getThreadGroup();<br><br>namePrefix = "pol-" + polNumber.getAndIncrement() + "-thread-";<br><br>} public Thread newThread(Runable r) {<br><br>Thread t = new Thread(group, r,namePrefix + threadNumber.getAndIncrement(),0); if (t.isDaemon()<br><br>t.setDaemon(false); if (t.getPriority() != Thread.NORM_PRIORITY)<br><br>t.setPriority(Thread.NORM_PRIORITY); return t;<br><br>}</th>
  </tr>
</table>


}

也可⾃⼰定义ThreadFactory，加⼊建⽴池的参数中

<table>
  <tr>
    <th> </th>
  </tr>
</table>


public static ExecutorService newCachedThreadPol(ThreadFactory threadFactory) {

Executor的execute()⽅法 execute() ⽅法将Runable实例加⼊pol中,并进⾏⼀些pol size计算和优先级处理 execute() ⽅法本身在Executor接⼝中定义,有多个实现类都定义了不同的execute()⽅法 如ThreadPolExecutor类（cache,fiexed,single三种池⼦都是调⽤它）的execute⽅法如下：

<table>
  <tr>
    <th>public void execute(Runable comand) { if (comand = nul) throw new NulPointerException(); if (polSize >= corePolSize| !adIfUnderCorePolSize(comand) { if (runState = RUNING & workQueue.ofer(comand) { if (runState != RUNING| polSize = 0) ensureQueuedTaskHandled(comand);<br><br>} else if (!adIfUnderMaximumPolSize(comand) reject(comand); / is shutdown or saturated }</th>
  </tr>
</table>


}

