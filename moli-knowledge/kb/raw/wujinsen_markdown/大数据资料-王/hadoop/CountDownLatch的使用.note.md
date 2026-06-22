# CountDownLatch是⼀个同步辅助类，犹如倒计时计数器，创建对象时通过构造⽅法设置初始值，调 ⽤CountDownLatch对象的await()⽅法则处于等待状态，调⽤countDown()⽅法就将计数器减1，当计 数到达0时，则所有等待者或单个等待者开始执⾏。

- 1 1 package com.thread;

- 2 2 import java.util.concurrent.CountDownLatch;

- 3 3 import java.util.concurrent.CyclicBarrier;

- 4 4 import java.util.concurrent.ExecutorService;

- 5 5 import java.util.concurrent.Executors;

- 6 6 /**

- 7 7 *

- 8 8 * @author Administrator

9 *该程序⽤来模拟发送命令与执⾏命令，主线程代表指挥官，新建3个线程代表战⼠，战⼠⼀直等待着指 挥官下达命令，

- 9

10 *若指挥官没有下达命令，则战⼠们都必须等待。⼀旦命令下达，战⼠们都去执⾏⾃⼰的任务，指挥官处 于等待状态，战⼠们任务执⾏完毕则报告给

- 10

- 11 11 *指挥官，指挥官则结束等待。

- 12 12 */

- 13 13 public class CountdownLatchTest {

- 14 14

- 15 15 public static void main(String[] args) {

16 ExecutorService service = Executors.newCachedThreadPool(); //创建⼀个 线程池

- 16

17 final CountDownLatch cdOrder = new CountDownLatch(1);//指挥官的命令，设 置为1，指挥官⼀下达命令，则cutDown,变为0，战⼠们执⾏任务

- 17

18 final CountDownLatch cdAnswer = new CountDownLatch(3);//因为有三个战 ⼠，所以初始值为3，每⼀个战⼠执⾏任务完毕则cutDown⼀次，当三个都执⾏完毕，变为0，则指挥官停⽌ 等待。

- 18

- 19 19 for(int i=0;i<3;i++){

- 20 20 Runnable runnable = new Runnable(){

- 21 21 public void run(){

- 22 22 try {

23 System.out.println("线程" + Thread.currentThread().getName() +

- 23

- 24 24 "正准备接受命令");

- 25 25 cdOrder.await(); //战⼠们都处于等待命令状态

26 System.out.println("线程" + Thread.currentThread().getName() +

- 26

- 27 27 "已接受命令");

- 28 28 Thread.sleep((long)(Math.random()*10000));

29 System.out.println("线程" + Thread.currentThread().getName() +

- 29

- 30 30 "回应命令处理结果");

31 cdAnswer.countDown(); //任务执⾏完毕，返回给指挥官， cdAnswer减1。

- 31

- 32 32 } catch (Exception e) {


- 33 33 e.printStackTrace();

- 34 34 }

- 35 35 }

- 36 36 };

- 37 37 service.execute(runnable);//为线程池添加任务

- 38 38 }

- 39 39 try {

- 40 40 Thread.sleep((long)(Math.random()*10000));

- 41 41

- 42 42 System.out.println("线程" + Thread.currentThread().getName() +

- 43 43 "即将发布命令");

44 cdOrder.countDown(); //发送命令，cdOrder减1，处于等待的战⼠们停⽌等待转 去执⾏任务。

- 44

- 45 45 System.out.println("线程" + Thread.currentThread().getName() +

- 46 46 "已发送命令，正在等待结果");

47 cdAnswer.await(); //命令发送后指挥官处于等待状态，⼀旦cdAnswer为0时停⽌ 等待继续往下执⾏

- 47

- 48 48 System.out.println("线程" + Thread.currentThread().getName() +

- 49 49 "已收到所有响应结果");

- 50 50 } catch (Exception e) {

- 51 51 e.printStackTrace();

- 52 52 }

- 53 53 service.shutdown(); //任务结束，停⽌线程池的所有线程

- 54 54

- 55 55 }

- 56 56 }


程序运⾏结果如下：

- 1 线程pool-1-thread-2正准备接受命令

- 2 线程pool-1-thread-3正准备接受命令

- 3 线程pool-1-thread-1正准备接受命令

- 4 线程main即将发布命令

- 5 线程pool-1-thread-2已接受命令

- 6 线程pool-1-thread-3已接受命令

- 7 线程pool-1-thread-1已接受命令

- 8 线程main已发送命令，正在等待结果

- 9 线程pool-1-thread-2回应命令处理结果

- 10 线程pool-1-thread-1回应命令处理结果

- 11 线程pool-1-thread-3回应命令处理结果

- 12 线程main已收到所有响应结果


我喜欢，驾驭着代码在⻛驰电掣中创造完美！我喜欢，操纵着代码在随必所欲中体验⽣活！我喜欢， 书写着代码在时代浪潮中完成经典！每⼀段新的代码在我⼿中诞⽣对我来说就象观看刹那花开的感 动！ 欢迎分享与转载 0

