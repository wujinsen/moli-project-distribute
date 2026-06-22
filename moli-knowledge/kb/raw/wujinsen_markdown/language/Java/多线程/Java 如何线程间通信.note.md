htps:/mp.weixin.q.com/s/zlSNJRDJ9zseVtD8Qp2VA

如何让两个线程依次执⾏？

那如何让 两个线程按照指定⽅式有序交叉运⾏呢？

四个线程 A B C D，其中 D 要等到 A B C 全执⾏完毕后才执⾏，⽽且 A B C 是同步运⾏的

三个运动员各⾃准备，等到三个⼈都准备好后，再⼀起跑

⼦线程完成某件任务后，把得到的结果回传给主线程

⼩结

Java 如何线程间通信，曾经⼩编⾯试被问哭的⼀道题。。 正常情况下，每个⼦线程完成各⾃的任务就可以结束了。不过有的时候，我们希望多个线程协同⼯作 来完成某个任务，这时就涉及到了线程间通信了。 本⽂涉及到的知识点：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


thread.join(), object.wait(), object.notify(), CountdownLatch, CyclicBarier, FutureTask, Calable 。

本⽂涉及代码：htps:/github.com/wingjay/HeloJava/blob/master/multi-thread/src/ForArticle.java

下⾯我从⼏个例⼦作为切⼊点来讲解下 Java ⾥有哪些⽅法来实现线程间通信。

- 1.
- 2.
- 3.
- 4.
- 5.


如何让两个线程依次执⾏？ 那如何让 两个线程按照指定⽅式有序交叉运⾏呢？ 四个线程 A B C D，其中 D 要等到 A B C 全执⾏完毕后才执⾏，⽽且 A B C 是同步运⾏的 三个运动员各⾃准备，等到三个⼈都准备好后，再⼀起跑 ⼦线程完成某件任务后，把得到的结果回传给主线程

# 如何让两个线程依次执⾏？

假设有两个线程，⼀个是线程 A，另⼀个是线程 B，两个线程分别依次打印 1-3 三个数字即可。我们来 看下代码：

- private static void demo1() {


- Thread A = new Thread(new Runnable() { @Override public void run() {

printNumber("A"); }

});

- Thread B = new Thread(new Runnable() { @Override


## public void run() {

printNumber("B"); }

});

- A.start();

- B.start();


}

其中的 printNumber(String) 实现如下，⽤来依次打印 1, 2, 3 三个数字：

private static void printNumber(String threadName) { int i=0; while (i++ < 3) {

try {

Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace();

} System.out.println(threadName + " print: " + i);

} }

这时我们得到的结果是：

- B print: 1 A print: 1 B print: 2 A print: 2 B print: 3 A print: 3 可以看到 A 和 B 是同时打印的。 那么，如果我们希望 B 在 A 全部打印 完后再开始打印呢？我们可以利⽤ thread.join() ⽅法，代码如


下:

- private static void demo2() {


- Thread A = new Thread(new Runnable() { @Override public void run() {

printNumber("A"); }

});

- Thread B = new Thread(new Runnable() { @Override public void run() {


System.out.println("B 开始等待 A"); try {

A.join(); } catch (InterruptedException e) { e.printStackTrace();

} printNumber("B");

}

}); B.start();

A.start(); }

得到的结果如下：

B 开始等待 A A print: 1 A print: 2 A print: 3

- B print: 1 B print: 2 B print: 3 所以我们能看到 A.join() ⽅法会让 B ⼀直等待直到 A 运⾏完毕。 那如何让 两个线程按照指定⽅式有序交叉运⾏呢？ 还是上⾯那个例⼦，我现在希望 A 在打印完 1 后，再让 B 打印 1, 2, 3，最后再回到 A 继续打印 2, 3。 这种需求下，显然 Thread.join() 已经不能满⾜了。我们需要更细粒度的锁来控制执⾏顺序。


这⾥，我们可以利⽤ object.wait() 和 object.notify() 两个⽅法来实现。代码如下：

/**

- * A 1, B 1, B 2, B 3, A 2, A 3

- */


- private static void demo3() { Object lock = new Object();


- Thread A = new Thread(new Runnable() { @Override public void run() {

synchronized (lock) {

- System.out.println("A 1"); try {

lock.wait(); } catch (InterruptedException e) {

e.printStackTrace(); }

- System.out.println("A 2");

- System.out.println("A 3");


} }

});

- Thread B = new Thread(new Runnable() { @Override public void run() {


## synchronized (lock) {

- System.out.println("B 1");

- System.out.println("B 2");

- System.out.println("B 3"); lock.notify();


} }

});

- A.start();

- B.start();


}

打印结果如下：

- A 1 A waiting…
- B 1 B 2 B 3 A 2 A 3 正是我们要的结果。 那么，这个过程发⽣了什么呢？


- 1.
- 2.
- 3.
- 4.
- 5.


⾸先创建⼀个 A 和 B 共享的对象锁 lock = new Object(); 当 A 得到锁后，先打印 1，然后调⽤ lock.wait() ⽅法，交出锁的控制权，进⼊ wait 状态； 对 B ⽽⾔，由于 A 最开始得到了锁，导致 B ⽆法执⾏；直到 A 调⽤ lock.wait() 释放控制权后， B 才得到了锁； B 在得到锁后打印 1， 2， 3；然后调⽤ lock.notify() ⽅法，唤醒正在 wait 的 A; A 被唤醒后，继续打印剩下的 2，3。

为了更好理解，我在上⾯的代码⾥加上 log ⽅便读者查看。

private static void demo3() {

Object lock = new Object();

- Thread A = new Thread(new Runnable() { @Override public void run() {

System.out.println("INFO: A 等待锁 "); synchronized (lock) {

System.out.println("INFO: A 得到了锁 lock");

- System.out.println("A 1"); try {

System.out.println("INFO: A 准备进⼊等待状态，放弃锁 lock 的控制权 "); lock.wait();

} catch (InterruptedException e) { e.printStackTrace();

} System.out.println("INFO: 有⼈唤醒了 A, A 重新获得锁 lock");

- System.out.println("A 2");

- System.out.println("A 3");


} }

});

- Thread B = new Thread(new Runnable() { @Override public void run() {


System.out.println("INFO: B 等待锁 "); synchronized (lock) {

System.out.println("INFO: B 得到了锁 lock");

- System.out.println("B 1");

- System.out.println("B 2");

- System.out.println("B 3"); System.out.println("INFO: B 打印完毕，调⽤ notify ⽅法 ");


lock.notify(); }

} });

- A.start();

- B.start();


}

打印结果如下:

INFO: A 等待锁 INFO: A 得到了锁 lock A 1 INFO: A 准备进⼊等待状态，调⽤ lock.wait() 放弃锁 lock 的控制权 INFO: B 等待锁 INFO: B 得到了锁 lock B 1 B 2 B 3 INFO: B 打印完毕，调⽤ lock.notify() ⽅法 INFO: 有⼈唤醒了 A, A 重新获得锁 lock A 2 A 3

# 四个线程 A B C D，其中 D 要等到 A B C 全执⾏完毕后才执⾏，⽽且 A B C 是同步运 ⾏的

最开始我们介绍了 thread.join()，可以让⼀个线程等另⼀个线程运⾏完毕后再继续执⾏，那我们可以在 D 线程⾥依次 join A B C，不过这也就使得 A B C 必须依次执⾏，⽽我们要的是这三者能同步运⾏。 或者说，我们希望达到的⽬的是：A B C 三个线程同时运⾏，各⾃独⽴运⾏完后通知 D；对 D ⽽⾔， 只要 A B C 都运⾏完了，D 再开始运⾏。针对这种情况，我们可以利⽤ CountdownLatch 来实现这类 通信⽅式。它的基本⽤法是：

- 1.
- 2.
- 3.
- 4.


创建⼀个计数器，设置初始值，CountdownLatch countDownLatch = new CountDownLatch(2); 在 等待线程 ⾥调⽤ countDownLatch.await() ⽅法，进⼊等待状态，直到计数值变成 0； 在 其他线程 ⾥，调⽤ countDownLatch.countDown() ⽅法，该⽅法会将计数值减⼩ 1； 当 其他线程 的 countDown() ⽅法把计数值变成 0 时，等待线程 ⾥的 countDownLatch.await() ⽴即退出，继续执⾏下⾯的代码。

实现代码如下：

private static void runDAfterABC() { int worker = 3; CountDownLatch countDownLatch = new CountDownLatch(worker); new Thread(new Runnable() {

@Override public void run() {

System.out.println("D is waiting for other three threads"); try {

countDownLatch.await(); System.out.println("All done, D starts working");

} catch (InterruptedException e) {

e.printStackTrace(); }

} }).start(); for (char threadName='A'; threadName <= 'C'; threadName++) {

final String tN = String.valueOf(threadName); new Thread(new Runnable() {

@Override

public void run() { System.out.println(tN + " is working"); try {

Thread.sleep(100); } catch (Exception e) { e.printStackTrace();

} System.out.println(tN + " finished"); countDownLatch.countDown();

}

}).start(); }

}

下⾯是运⾏结果：

D is waiting for other thre threads A is working B is working C is working A finished C finished B finished Al done, D starts working

其实简单点来说，CountDownLatch 就是⼀个倒计数器，我们把初始计数值设置为3，当 D 运⾏时， 先调⽤ countDownLatch.await() 检查计数器值是否为 0，若不为 0 则保持等待状态；当A B C 各⾃运 ⾏完后都会利⽤countDownLatch.countDown()，将倒计数器减 1，当三个都运⾏完后，计数器被减⾄ 0；此时⽴即触发 D 的 await() 运⾏结束，继续向下执⾏。 因此，CountDownLatch 适⽤于⼀个线程去等待多个线程的情况。

# 三个运动员各⾃准备，等到三个⼈都准备好后，再⼀起跑

上⾯是⼀个形象的⽐喻，针对 线程 A B C 各⾃开始准备，直到三者都准备完毕，然后再同时运⾏ 。也 就是要实现⼀种 线程之间互相等待 的效果，那应该怎么来实现呢？ 上⾯的 CountDownLatch 可以⽤来倒计数，但当计数完毕，只有⼀个线程的 await() 会得到响应，⽆ 法让多个线程同时触发。 为了实现线程间互相等待这种需求，我们可以利⽤ CyclicBarier 数据结构，它的基本⽤法是：

- 1.
- 2.
- 3.


先创建⼀个公共 CyclicBarier 对象，设置 同时等待 的线程数，CyclicBarier cyclicBarier = new CyclicBarier(3); 这些线程同时开始⾃⼰做准备，⾃身准备完毕后，需要等待别⼈准备完毕，这时调⽤ cyclicBarier.await(); 即可开始等待别⼈； 当指定的 同时等待 的线程数都调⽤了 cyclicBarier.await();时，意味着这些线程都准备完毕好， 然后这些线程才 同时继续执⾏。

实现代码如下，设想有三个跑步运动员，各⾃准备好后等待其他⼈，全部准备好后才开始跑：

private static void runABCWhenAllReady() { int runner = 3; CyclicBarrier cyclicBarrier = new CyclicBarrier(runner); final Random random = new Random(); for (char runnerName='A'; runnerName <= 'C'; runnerName++) {

final String rN = String.valueOf(runnerName); new Thread(new Runnable() {

@Override public void run() {

long prepareTime = random.nextInt(10000) + 100; System.out.println(rN + " is preparing for time: " + prepareTime); try {

Thread.sleep(prepareTime); } catch (Exception e) {

e.printStackTrace();

} try {

System.out.println(rN + " is prepared, waiting for others"); cyclicBarrier.await(); // 当前运动员准备完毕，等待别⼈准备好

} catch (InterruptedException e) { e.printStackTrace(); } catch (BrokenBarrierException e) { e.printStackTrace();

} System.out.println(rN + " starts running"); // 所有运动员都准备好了，⼀起开始跑

}

}).start(); }

}

打印的结果如下：

A is preparing for time: 4131 B is preparing for time: 6349 C is preparing for time: 8206 A is prepared, waiting for others B is prepared, waiting for others C is prepared, waiting for others C starts runing A starts runing B starts runing

# ⼦线程完成某件任务后，把得到的结果回传给主线程

实际的开发中，我们经常要创建⼦线程来做⼀些耗时任务，然后把任务执⾏结果回传给主线程使⽤， 这种情况在 Java ⾥要如何实现呢？ 回顾线程的创建，我们⼀般会把 Runable 对象传给 Thread 去执⾏。Runable定义如下：

public interface Runnable {

public abstract void run(); }

可以看到 run() 在执⾏完后不会返回任何结果。那如果希望返回结果呢？这⾥可以利⽤另⼀个类似的接 ⼝类 Calable：

@FunctionalInterface public interface Callable<V> {

/**

- * Computes a result, or throws an exception if unable to do so.

*

- * @return computed result

- * @throws Exception if unable to compute a result

- */


V call() throws Exception; }

可以看出 Calable 最⼤区别就是返回范型 V 结果。 那么下⼀个问题就是，如何把⼦线程的结果回传回来呢？在 Java ⾥，有⼀个类是配合 Calable 使⽤ 的：FutureTask，不过注意，它获取结果的 get ⽅法会阻塞主线程。 举例，我们想让⼦线程去计算从 1 加到 10，并把算出的结果返回到主线程。

private static void doTaskWithResultInWorker() {

Callable<Integer> callable = new Callable<Integer>() { @Override public Integer call() throws Exception {

System.out.println("Task starts"); Thread.sleep(1000); int result = 0; for (int i=0; i<=100; i++) {

result += i;

} System.out.println("Task finished and return result"); return result;

}

}; FutureTask<Integer> futureTask = new FutureTask<>(callable); new Thread(futureTask).start(); try {

System.out.println("Before futureTask.get()"); System.out.println("Result: " + futureTask.get()); System.out.println("After futureTask.get()");

} catch (InterruptedException e) { e.printStackTrace(); } catch (ExecutionException e) {

e.printStackTrace(); }

}

打印结果如下：

Before futureTask.get() Task starts Task finished and return result Result: 5050 After futureTask.get()

可以看到，主线程调⽤ futureTask.get() ⽅法时阻塞主线程；然后 Calable 内部开始执⾏，并返回运算 结果；此时 futureTask.get() 得到结果，主线程恢复运⾏。 这⾥我们可以学到，通过 FutureTask 和 Calable 可以直接在主线程获得⼦线程的运算结果，只不过需 要阻塞主线程。当然，如果不希望阻塞主线程，可以考虑利⽤ ExecutorService，把 FutureTask 放到 线程池去管理执⾏。

# ⼩结

多线程是现代语⾔的共同特性，⽽线程间通信、线程同步、线程安全是很重要的话题。本⽂针对 Java 的线程间通信进⾏了⼤致的讲解，后续还会对线程同步、线程安全进⾏讲解。

