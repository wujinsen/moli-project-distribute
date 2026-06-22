CountDownLatch，⼀个同步辅助类，在完成⼀组正在其他线程中执⾏的操作之前，它允许⼀个或多个 等待。 主要⽅法

uic CountDownLatch(int count); uicoi countDown(); public void await() throws

InteruptedException

构造⽅法参数指定了计数的次数 countDown⽅法，当前线程调⽤此⽅法，则计数减⼀ awaint⽅法，调⽤此⽅法会⼀直阻塞当前线程，直到计时器的值为0

例⼦ Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.


publicclas CountDownLatchDemo { finalstatic SimpleDateFormat sdf=new SimpleDateFormat(" y- M-d H: m:s"); publicstaticvoid main(String[] args) throws InteruptedException {

CountDownLatch latch=new CountDownLatch(2);/两个⼯⼈的协作

- Worker worker1=new Worker("zhang san", 5 0, latch);
- Worker worker2=new Worker("li si", 8 0, latch);


- worker1.start();/
- worker2.start();/ latch.await();/等待所有⼯⼈完成⼯作 System.out.println("al work done at "+sdf.format(new Date( );


}

staticclas Worker extends Thread{ String workerName; int workTime; CountDownLatch latch; public Worker(String workerName ,int workTime ,CountDownLatch latch){

this.workerName=workerName; this.workTime=workTime; this.latch=latch;

} publicvoid run(){

System.out.println("Worker "+workerName+" do work begin at "+sdf.format(new Date( doWork();/⼯作了 System.out.println("Worker "+workerName+" do work complete at "+sdf.format(new Da latch.countDown();/⼯⼈完成⼯作，计数器减⼀

- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.


}

privatevoid doWork(){ try { Thread.sl ep(workTime); } catch (InteruptedException e) {

e.printStackTrace(); }

} }

}

输出： orker zhang san do work begin at 201-04-141 05 1 orker li si do work begin at 201-04-141 05 1 orker zhang san do work complete at 201-04-141 05 16

Worker li si do work complete at 201-04-141 05 19 al work done at 201-04-141 05 19

声明：ITeye⽂章版权属于作者，受法律保护。没有作者书⾯许可不得转载。 推荐链接

返回顶楼

xiaobao0501

等级: 初级会员

性别:

⽂章: 27 积分: 30 来⾃: 北京

返回顶楼

zk1878

等级:

性别:

⽂章: 40

积分: 150

来⾃: 深圳

