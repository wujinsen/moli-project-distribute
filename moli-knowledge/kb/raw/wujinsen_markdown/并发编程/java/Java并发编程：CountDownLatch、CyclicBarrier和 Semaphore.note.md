在java 1.5中，提供了⼀些⾮常有⽤的辅助类来帮助我们进⾏并发编程，⽐如CountDownLatch， CyclicBarier和Semaphore，今天我们就来学习⼀下这三个辅助类的⽤法。 以下是本⽂⽬录⼤纲： ⼀.CountDownLatch⽤法 ⼆.CyclicBarier⽤法 三.Semaphore⽤法

# ⼀.CountDownLatch⽤法

CountDownLatch类位于java.util.concurent包下，利⽤它可以实现类似计数器的功能。⽐如有⼀个任 务A，它要等待其他4个任务执⾏完毕之后才能执⾏，此时就可以利⽤CountDownLatch来实现这种功 能了。 CountDownLatch类只提供了⼀个构造器：

<table>
  <tr>
    <th>1</th>
    <th>public CountDownLatch(int count) { }; /参数count为计数值</th>
  </tr>
</table>


然后下⾯这3个⽅法是CountDownLatch类中最重要的⽅法：

<table>
  <tr>
    <th>1<br>2<br>3<br></th>
    <th>public void await() throws InterruptedException { }; /调⽤ await()⽅法的线程会被挂起，它会等待直到count值为0才继续执 ⾏ public bolean await(long timeout, TimeUnit unit) throws InterruptedException { }; /和await()类似，只不过等待⼀定的时 间后count值还没变为0的话就会继续执⾏</th>
  </tr>
</table>


public void countDown() { }; /将count值减1

下⾯看⼀个例⼦⼤家就清楚CountDownLatch的⽤法了：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br>23<br>24<br>25<br>26<br>27<br>28<br>29<br>30<br>31<br>32<br><br><br>3<br><br><br>34<br>35<br>36<br>37<br>38<br>39<br>40<br></th>
    <th>public clas Test { public static void main(String[] args) { final CountDownLatch latch = new CountDownLatch(2);<br><br>new Thread(){ public void run() { try { System.out.println("⼦线<br><br>程"+Thread.currentThread().getName()+"正在执⾏"); Thread.sl ep(3 0); System.out.println("⼦线<br><br>程"+Thread.currentThread().getName()+"执⾏完毕"); latch.countDown(); } catch (InterruptedException e) { e.printStackTrace(); } }; }.start();<br><br>new Thread(){ public void run() { try { System.out.println("⼦线<br><br>程"+Thread.currentThread().getName()+"正在执⾏"); Thread.sl ep(3 0); System.out.println("⼦线<br><br>程"+Thread.currentThread().getName()+"执⾏完毕"); latch.countDown(); } catch (InterruptedException e) { e.printStackTrace(); } }; }.start();<br><br>try { System.out.println("等待2个⼦线程执⾏完毕 ."); latch.await(); System.out.println("2个⼦线程已经执⾏完毕"); System.out.println("继续执⾏主线程"); } catch (InterruptedException e) { e.printStackTrace(); } }</th>
  </tr>
</table>


}

执⾏结果：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br></th>
    <th>线程Thread-0正在执⾏<br>线程Thread-1正在执⾏ 等待2个⼦线程执⾏完毕 .<br><br><br>线程Thread-0执⾏完毕<br>线程Thread-1执⾏完毕 2个⼦线程已经执⾏完毕 继续执⾏主线程<br></th>
  </tr>
</table>


# ⼆.CyclicBarrier⽤法

字⾯意思回环栅栏，通过它可以实现让⼀组线程等待⾄某个状态之后再全部同时执⾏。叫做回环是因 为当所有等待线程都被释放以后，CyclicBarier可以被重⽤。我们暂且把这个状态就叫做barier，当调 ⽤await()⽅法之后，线程就处于barier了。 CyclicBarier类位于java.util.concurent包下，CyclicBarier提供2个构造器：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br></th>
    <th>public CyclicBarrier(int parties, Runable barrierAction) { }<br><br>public CyclicBarrier(int parties) {</th>
  </tr>
</table>


5 }

参数parties指让多少个线程或者任务等待⾄barier状态；参数barierAction为当这些线程都达到 barier状态时会执⾏的内容。 然后CyclicBarier中最重要的⽅法就是await⽅法，它有2个重载版本：

<table>
  <tr>
    <th>1<br>2<br></th>
    <th>public int await() throws InterruptedException, BrokenBarrierException { }; public int await(long timeout, TimeUnit unit)throws InterruptedException,BrokenBarrierException,TimeoutExcepti</th>
  </tr>
</table>


on { };

第⼀个版本⽐较常⽤，⽤来挂起当前线程，直⾄所有线程都到达barier状态再同时执⾏后续任务； 第⼆个版本是让这些线程等待⾄⼀定的时间，如果还有线程没有到达barier状态就直接让到达barier 的线程执⾏后续任务。 下⾯举⼏个例⼦就明⽩了： 假若有若⼲个线程都要进⾏写数据操作，并且只有所有线程都完成写数据操作之后，这些线程才能继 续做后⾯的事情，此时就可以利⽤CyclicBarier了：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br><br>23<br>24<br>25<br>26<br>27<br>28<br>29<br></th>
    <th>public clas Test { public static void main(String[] args) { int N = 4; CyclicBarrier barrier = new CyclicBarrier(N); for(int i=0;i<N;i +) new Writer(barrier).start(); } static clas Writer extends Thread{ private CyclicBarrier cyclicBarrier; public Writer(CyclicBarrier cyclicBarrier) { this.cyclicBarrier = cyclicBarrier; }<br><br>@Override public void run() { System.out.println("线<br><br>程"+Thread.currentThread().getName()+"正在写⼊数据 ."); try { Thread.sl ep(5 0); /以睡眠来模拟写⼊数据操作 System.out.println("线<br><br>程"+Thread.currentThread().getName()+"写⼊数据完毕，等待 其他线程写⼊完毕");<br><br>cyclicBarrier.await(); } catch (InterruptedException e) { e.printStackTrace(); }catch(BrokenBarrierException e){ e.printStackTrace(); } System.out.println("所有线程写⼊完毕，继续处理其他任务 .");<br><br>} }</th>
  </tr>
</table>


}

执⾏结果：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br><br><br>12</th>
    <th>线程Thread-0正在写⼊数据 . 线程Thread-3正在写⼊数据 . 线程Thread-2正在写⼊数据 .<br>线程Thread-1正在写⼊数据 .<br>线程Thread-2写⼊数据完毕，等待其他线程写⼊完毕<br><br>线程Thread-0写⼊数据完毕，等待其他线程写⼊完毕<br><br>线程Thread-3写⼊数据完毕，等待其他线程写⼊完毕<br><br>线程Thread-1写⼊数据完毕，等待其他线程写⼊完毕 所有线程写⼊完毕，继续处理其他任务 . 所有线程写⼊完毕，继续处理其他任务 . 所有线程写⼊完毕，继续处理其他任务 . 所有线程写⼊完毕，继续处理其他任务<br><br><br></th>
  </tr>
</table>


.

从上⾯输出结果可以看出，每个写⼊线程执⾏完写数据操作之后，就在等待其他线程写⼊操作完毕。 当所有线程线程写⼊操作完毕之后，所有线程就继续进⾏后续的操作了。 如果说想在所有线程写⼊操作完之后，进⾏额外的其他操作可以为CyclicBarier提供Runable参数：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br>23<br>24<br>25<br>26<br>27<br>28<br>29<br>30<br>31<br>32<br><br><br>3<br><br><br>34<br>35<br></th>
    <th>public clas Test { public static void main(String[] args) { int N = 4; CyclicBarrier barrier = new CyclicBarrier(N,new Runable() { @Override public void run() { System.out.println("当前线<br><br>程"+Thread.currentThread().getName(); } });<br><br>for(int i=0;i<N;i +) new Writer(barrier).start(); } static clas Writer extends Thread{ private CyclicBarrier cyclicBarrier; public Writer(CyclicBarrier cyclicBarrier) { this.cyclicBarrier = cyclicBarrier; }<br><br>@Override public void run() { System.out.println("线<br><br>程"+Thread.currentThread().getName()+"正在写⼊数据 ."); try { Thread.sl ep(5 0); /以睡眠来模拟写⼊数据操作 System.out.println("线<br><br>程"+Thread.currentThread().getName()+"写⼊数据完毕，等待 其他线程写⼊完毕");<br><br>cyclicBarrier.await(); } catch (InterruptedException e) { e.printStackTrace(); }catch(BrokenBarrierException e){ e.printStackTrace(); } System.out.println("所有线程写⼊完毕，继续处理其他任务 .");<br><br>} }</th>
  </tr>
</table>


}

运⾏结果：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br><br><br>12<br>13<br></th>
    <th>线程Thread-0正在写⼊数据 .<br>线程Thread-1正在写⼊数据 .<br>线程Thread-2正在写⼊数据 .<br>线程Thread-3正在写⼊数据 .<br><br><br>线程Thread-0写⼊数据完毕，等待其他线程写⼊完毕<br>线程Thread-1写⼊数据完毕，等待其他线程写⼊完毕<br>线程Thread-2写⼊数据完毕，等待其他线程写⼊完毕<br>线程Thread-3写⼊数据完毕，等待其他线程写⼊完毕 当前线程Thread-3 所有线程写⼊完毕，继续处理其他任务 . 所有线程写⼊完毕，继续处理其他任务 . 所有线程写⼊完毕，继续处理其他任务 . 所有线程写⼊完毕，继续处理其他任务<br></th>
  </tr>
</table>


.

从结果可以看出，当四个线程都到达barier状态后，会从四个线程中选择⼀个线程去执⾏Runable。 下⾯看⼀下为await指定时间的效果：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br>23<br>24<br>25<br>26<br>27<br>28<br>29<br>30<br>31<br>32<br><br><br>3<br><br>34<br>35<br>36<br>37<br>38<br>39<br>40<br>41<br>42<br>43<br><br><br>4<br><br><br>45</th>
    <th>public clas Test { public static void main(String[] args) { int N = 4; CyclicBarrier barrier = new CyclicBarrier(N);<br><br>for(int i=0;i<N;i +) { if(i<N-1) new Writer(barrier).start(); else { try { Thread.sl ep(5 0); } catch (InterruptedException e) { e.printStackTrace(); } new Writer(barrier).start(); } } } static clas Writer extends Thread{ private CyclicBarrier cyclicBarrier; public Writer(CyclicBarrier cyclicBarrier) { this.cyclicBarrier = cyclicBarrier; }<br><br>@Override public void run() { System.out.println("线<br><br>程"+Thread.currentThread().getName()+"正在写⼊数据 ."); try { Thread.sl ep(5 0); /以睡眠来模拟写⼊数据操作 System.out.println("线<br><br>程"+Thread.currentThread().getName()+"写⼊数据完毕，等待 其他线程写⼊完毕");<br><br>try { cyclicBarrier.await(2 0, TimeUnit.MI LISECONDS); } catch (TimeoutException e) {<br><br>/ TODO Auto-generated catch block e.printStackTrace(); } } catch (InterruptedException e) { e.printStackTrace(); }catch(BrokenBarrierException e){ e.printStackTrace(); } System.out.println(Thread.currentThread().getName()+"所有<br><br>线程写⼊完毕，继续处理其他任务 .");<br><br>} }</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br><br>23<br>24<br>25<br>26<br>27<br>28<br></th>
    <th>线程Thread-0正在写⼊数据 .<br><br>线程Thread-2正在写⼊数据 .<br><br>线程Thread-1正在写⼊数据 .<br><br>线程Thread-2写⼊数据完毕，等待其他线程写⼊完毕<br><br>线程Thread-0写⼊数据完毕，等待其他线程写⼊完毕<br>线程Thread-1写⼊数据完毕，等待其他线程写⼊完毕<br><br><br>线程Thread-3正在写⼊数据 . java.util.concurrent.TimeoutException<br><br>Thread-1所有线程写⼊完毕，继续处理其他任务 . Thread-0所有线程写⼊完毕，继续处理其他任务 .<br><br>at java.util.concurrent.CyclicBarrier.dowait(Unknown Source) at java.util.concurrent.CyclicBarrier.await(Unknown Source) at com.cxh.test1.Test$Writer.run(Test.java:58)<br><br>java.util.concurrent.BrokenBarrierException at java.util.concurrent.CyclicBarrier.dowait(Unknown Source) at java.util.concurrent.CyclicBarrier.await(Unknown Source) at com.cxh.test1.Test$Writer.run(Test.java:58)<br><br>java.util.concurrent.BrokenBarrierException at java.util.concurrent.CyclicBarrier.dowait(Unknown Source) at java.util.concurrent.CyclicBarrier.await(Unknown Source) at com.cxh.test1.Test$Writer.run(Test.java:58)<br><br>Thread-2所有线程写⼊完毕，继续处理其他任务 . java.util.concurrent.BrokenBarrierException<br><br><br>线程Thread-3写⼊数据完毕，等待其他线程写⼊完毕 at java.util.concurrent.CyclicBarrier.dowait(Unknown Source) at java.util.concurrent.CyclicBarrier.await(Unknown Source) at com.cxh.test1.Test$Writer.run(Test.java:58)<br><br><br><br><br><br><br>所有线程写⼊完毕，继续处理其他任务</th>
  </tr>
</table>


Thread-3 .

上⾯的代码在main⽅法的for循环中，故意让最后⼀个线程启动延迟，因为在前⾯三个线程都达到 barier之后，等待了指定的时间发现第四个线程还没有达到barier，就抛出异常并继续执⾏后⾯的任 务。 另外CyclicBarier是可以重⽤的，看下⾯这个例⼦：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br>23<br>24<br>25<br>26<br>27<br>28<br>29<br>30<br>31<br>32<br><br><br>3<br><br>34<br>35<br>36<br>37<br>38<br>39<br>40<br>41<br>42<br>43<br><br><br>4<br></th>
    <th>public clas Test { public static void main(String[] args) { int N = 4; CyclicBarrier barrier = new CyclicBarrier(N);<br><br>for(int i=0;i<N;i +) { new Writer(barrier).start(); }<br><br>try { Thread.sl ep(25 0); } catch (InterruptedException e) { e.printStackTrace(); }<br><br>System.out.println("CyclicBarrier重⽤");<br><br>for(int i=0;i<N;i +) { new Writer(barrier).start(); } } static clas Writer extends Thread{ private CyclicBarrier cyclicBarrier; public Writer(CyclicBarrier cyclicBarrier) { this.cyclicBarrier = cyclicBarrier; }<br><br>@Override public void run() { System.out.println("线<br><br>程"+Thread.currentThread().getName()+"正在写⼊数据 ."); try { Thread.sl ep(5 0); /以睡眠来模拟写⼊数据操作 System.out.println("线<br><br>程"+Thread.currentThread().getName()+"写⼊数据完毕，等待 其他线程写⼊完毕");<br><br>cyclicBarrier.await(); } catch (InterruptedException e) { e.printStackTrace(); }catch(BrokenBarrierException e){ e.printStackTrace(); } System.out.println(Thread.currentThread().getName()+"所有<br><br>线程写⼊完毕，继续处理其他任务 .");<br><br>} }</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br><br>23<br>24<br>25<br></th>
    <th>线程Thread-0正在写⼊数据 .<br>线程Thread-1正在写⼊数据 .<br><br>线程Thread-3正在写⼊数据 .<br><br>线程Thread-2正在写⼊数据 .<br><br>线程Thread-1写⼊数据完毕，等待其他线程写⼊完毕<br><br>线程Thread-3写⼊数据完毕，等待其他线程写⼊完毕<br><br>线程Thread-2写⼊数据完毕，等待其他线程写⼊完毕 线程Thread-0写⼊数据完毕，等待其他线程写⼊完毕<br><br>Thread-0所有线程写⼊完毕，继续处理其他任务 .<br><br>Thread-3所有线程写⼊完毕，继续处理其他任务 .<br><br>Thread-1所有线程写⼊完毕，继续处理其他任务 . Thread-2所有线程写⼊完毕，继续处理其他任务 . CyclicBarrier重⽤ 线程Thread-4正在写⼊数据 . 线程Thread-5正在写⼊数据 . 线程Thread-6正在写⼊数据 . 线程Thread-7正在写⼊数据 . 线程Thread-7写⼊数据完毕，等待其他线程写⼊完毕<br><br>线程Thread-5写⼊数据完毕，等待其他线程写⼊完毕<br>线程Thread-6写⼊数据完毕，等待其他线程写⼊完毕<br><br><br>线程Thread-4写⼊数据完毕，等待其他线程写⼊完毕<br><br>Thread-4所有线程写⼊完毕，继续处理其他任务 .<br>Thread-5所有线程写⼊完毕，继续处理其他任务 .<br>Thread-6所有线程写⼊完毕，继续处理其他任务 . 所有线程写⼊完毕，继续处理其他任务<br><br><br><br><br><br><br><br><br><br><br></th>
  </tr>
</table>


Thread-7 .

从执⾏结果可以看出，在初次的4个线程越过barier状态后，⼜可以⽤来进⾏新⼀轮的使⽤。⽽ CountDownLatch⽆法进⾏重复使⽤。

# 三.Semaphore⽤法

Semaphore翻译成字⾯意思为 信号量，Semaphore可以控同时访问的线程个数，通过 acquire() 获取 ⼀个许可，如果没有就等待，⽽ release() 释放⼀个许可。 Semaphore类位于java.util.concurent包下，它提供了2个构造器：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br></th>
    <th>public Semaphore(int permits) { /参数permits表示许可数⽬， 即同时可以允许多少线程进⾏访问<br><br>sync = new NonfairSync(permits); } public Semaphore(int permits, bolean fair) { /这个多了⼀个 参数fair表示是否是公平的，即等待时间越久的越先获取许可<br><br>sync = (fair)? new FairSync(permits) : new NonfairSync(permits);</th>
  </tr>
</table>


}

下⾯说⼀下Semaphore类中⽐较重要的⼏个⽅法，⾸先是acquire()、release()⽅法：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br></th>
    <th>public void acquire() throws InterruptedException { } /获取⼀ 个许可 public void acquire(int permits) throws InterruptedException { } /获取permits个许可 public void release() { } /释放⼀个许可<br><br>个许可</th>
  </tr>
</table>


public void release(int permits) { } /释放permits

acquire()⽤来获取⼀个许可，若⽆许可能够获得，则会⼀直等待，直到获得许可。 release()⽤来释放许可。注意，在释放许可之前，必须先获获得许可。 这4个⽅法都会被阻塞，如果想⽴即得到执⾏结果，可以使⽤下⾯⼏个⽅法：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br></th>
    <th>public bolean tryAcquire() { }; /尝试获取⼀个许可，若获取成 功，则⽴即返回true，若获取失败，则⽴即返回false public bolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException { }; /尝试获取⼀个许可，若在指定的时间 内获取成功，则⽴即返回true，否则则⽴即返回false public bolean tryAcquire(int permits) { }; /尝试获取permits个 许可，若获取成功，则⽴即返回true，若获取失败，则⽴即返回 false public bolean tryAcquire(int permits, long timeout, TimeUnit unit) throws InterruptedException { }; /尝试获取permits个许 可，若在指定的时间内获取成功，则⽴即返回true，否则则⽴即返</th>
  </tr>
</table>


回false

另外还可以通过availablePermits()⽅法得到可⽤的许可数⽬。 下⾯通过⼀个例⼦来看⼀下Semaphore的具体使⽤： 假若⼀个⼯⼚有5台机器，但是有8个⼯⼈，⼀台机器同时只能被⼀个⼯⼈使⽤，只有使⽤完了，其他 ⼯⼈才能继续使⽤。那么我们就可以通过Semaphore来实现：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br><br>23<br>24<br>25<br>26<br>27<br>28<br>29<br>30<br></th>
    <th>public clas Test { public static void main(String[] args) { int N = 8; /⼯⼈数 Semaphore semaphore = new Semaphore(5); /机器数⽬ for(int i=0;i<N;i +) new Worker(i,semaphore).start(); }<br><br>static clas Worker extends Thread{ private int num; private Semaphore semaphore; public Worker(int num,Semaphore semaphore){ this.num = num; this.semaphore = semaphore; }<br><br>@Override public void run() { try { semaphore.acquire(); System.out.println("⼯⼈"+this.num+"占⽤⼀个机器在⽣产 ."); Thread.sl ep(2 0); System.out.println("⼯⼈"+this.num+"释放出机器"); semaphore.release(); } catch (InterruptedException e) { e.printStackTrace(); } } }</th>
  </tr>
</table>


}

执⾏结果：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br><br><br>12<br>13<br>14<br>15<br>16<br></th>
    <th>⼯⼈0占⽤⼀个机器在⽣产 .<br>⼯⼈1占⽤⼀个机器在⽣产 .<br>⼯⼈2占⽤⼀个机器在⽣产 .<br><br>⼯⼈4占⽤⼀个机器在⽣产 .<br>⼯⼈5占⽤⼀个机器在⽣产 .<br><br>⼯⼈0释放出机器<br><br>⼯⼈2释放出机器<br><br>⼯⼈3占⽤⼀个机器在⽣产 . ⼯⼈7占⽤⼀个机器在⽣产 .<br><br>⼯⼈4释放出机器<br>⼯⼈5释放出机器<br><br><br>⼯⼈1释放出机器 ⼯⼈6占⽤⼀个机器在⽣产 .<br><br>⼯⼈3释放出机器 ⼯⼈7释放出机器<br><br><br><br><br><br><br><br><br>释放出机器</th>
  </tr>
</table>


⼯⼈6

下⾯对上⾯说的三个辅助类进⾏⼀个总结：

- 1）CountDownLatch和CyclicBarier都能够实现线程之间的等待，只不过它们侧重点不同： CountDownLatch⼀般⽤于某个线程A等待若⼲个其他线程执⾏完任务之后，它才执⾏； ⽽CyclicBarier⼀般⽤于⼀组线程互相等待⾄某个状态，然后这⼀组线程再同时执⾏； 另外，CountDownLatch是不能够重⽤的，⽽CyclicBarier是可以重⽤的。

- 2）Semaphore其实和锁有点类似，它⼀般⽤于控制对某组资源的访问权限。 参考资料： 《 》


Java编程思想 htp:/ w.itzhai.com/the-introduction-and-use-of-a-countdownlatch.html htp:/leaver.me/archives/320.html htp:/developer.51cto.com/art/201403/432095.htm htp:/blog.csdn.net/yanhandle/article/details/9016329 htp:/blog.csdn.net/cutesource/article/details/5780740 htp:/ w.cnblogs.com/whgw/archive/201/09/29/219 5.html

