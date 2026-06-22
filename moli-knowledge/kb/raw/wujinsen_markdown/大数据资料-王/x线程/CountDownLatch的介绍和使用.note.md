- 1、类介绍

java.util.concurent.CountDownLatch ⼀个同步辅助类，在完成⼀组正在其他线程中执⾏的操作之前，它允许⼀个或多个线程⼀直等待。 ⽤给定的计数 初始化 CountDownLatch。由于调⽤了 countDown() ⽅法，所以在当前计数到达零之 前，await ⽅法会⼀直受阻塞。之后，会释放所有等待的线程，await 的所有后续调⽤都将⽴即返回。 这种现象只出现⼀次⸺计数⽆法被重置。如果需要重置计数，请考虑使⽤ CyclicBarier。

- 2、使⽤场景 在⼀些应⽤场合中，需要等待某个条件达到要求后才能做后⾯的事情；同时当线程都完成后也会触发 事件，以便进⾏后⾯的操作。 这个时候就可以使⽤CountDownLatch。CountDownLatch最重要的⽅ 法是countDown()和await()，前者主要是倒数⼀次，后者是等待倒数到0，如果没有到达0，就只有阻 塞等待了。
- 3、⽅法说明

public void countDown() 递减锁存器的计数，如果计数到达零，则释放所有等待的线程。如果当前计数⼤于零，则将计数减 少。如果新的计数为零，出于线程调度⽬的，将重新启⽤所有的等待线程。如果当前计数等于零，则 不发⽣任何操作。

public bolean await(long timeout, unit) throws 使当前线程在锁存器倒计数⾄零之前⼀直等待，除⾮线程被 或超出了指定的等待时间。如果当前 计数为零，则此⽅法⽴刻返回 true 值。如果当前计数⼤于零，则出于线程调度⽬的，将禁⽤当前 线程，且在发⽣以下三种情况之⼀前，该线程将⼀直处于休眠状态：由于调⽤ countDown() ⽅ 法，计数到达零；或者其他某个线程中断当前线程；或者已超出指定的等待时间。如果计数到达零， 则该⽅法返回 true 值。如果当前线程：在进⼊此⽅法时已经设置了该线程的中断状态；或者在等 待时被中断，则抛出 InterruptedException ，并且清除当前线程的已中断状态。如果超出了指 定的等待时间，则返回值为 false 。如果该时间⼩于等于零，则此⽅法根本不会等待。 参数： timeout - 要等待的最⻓时间 unit - timeout 参数的时间单位。返回：如果计数到达 零，则返回 true ；如果在计数到达零之前超过了等待时间，则返回 false 抛出： InteruptedException - 如果当前线程在等待时被中断

- 4、相关实例


# java.util.concurent类 CountDownLatch

java.lang.Object

countDown

await

TimeUnit InteruptedException 中断

// ⼀个CountDouwnLatch实例是不能重复使⽤的，也就是说它是⼀次性的，锁⼀经被打开就不能再关闭使 ⽤了，如果想重复使⽤，请考虑使⽤CyclicBarrier。

- 1

- 2 public class CountDownLatchTest {

- 3

// 模拟了100⽶赛跑，10名选⼿已经准备就绪，只等裁判⼀声令下。当所有⼈都到达终点时，⽐赛结 束。

- 4

- 5 public static void main(String[] args) throws InterruptedException {

- 6

- 7 // 开始的倒数锁

- 8 final CountDownLatch begin = new CountDownLatch(1);

- 9

- 10 // 结束的倒数锁

- 11 final CountDownLatch end = new CountDownLatch(10);

- 12

- 13 // ⼗名选⼿

- 14 final ExecutorService exec = Executors.newFixedThreadPool(10);

- 15

- 16 for (int index = 0; index < 10; index++) {

- 17 final int NO = index + 1;

- 18 Runnable run = new Runnable() {

- 19 public void run() {

- 20 try {

- 21 // 如果当前计数为零，则此⽅法⽴即返回。

- 22 // 等待

- 23 begin.await();

- 24 Thread.sleep((long) (Math.random() * 10000));

- 25 System.out.println("No." + NO + " arrived");

- 26 } catch (InterruptedException e) {

- 27 } finally {

- 28 // 每个选⼿到达终点时，end就减⼀

- 29 end.countDown();

- 30 }

- 31 }

- 32 };

- 33 exec.submit(run);

- 34 }

- 35 System.out.println("Game Start");

- 36 // begin减⼀，开始游戏

- 37 begin.countDown();

- 38 // 等待end变为0，即所有选⼿到达终点


- 39 end.await();

- 40 System.out.println("Game Over");

- 41 exec.shutdown();

- 42 }

- 43 }


除了⽂章中有特别说明，均为IT宅原创⽂章，转载请以链接形式注明出处。 本⽂链接：

htp:/ w.itzhai.com/the-introduction-and-use-of-a-countdownlatch.html

