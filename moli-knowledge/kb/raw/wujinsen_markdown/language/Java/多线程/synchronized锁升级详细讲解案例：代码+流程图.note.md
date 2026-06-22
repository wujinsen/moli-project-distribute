声明：

以下是JDK1.8的版本，1.8默认是开启偏向锁延迟的，时间⼤约4s； 为什么默认4s才开启偏向锁？

–jvm启动的时候，⾃⼰会创建⼗⼏个线程分别去初始化很多带有synchronized同步代码块的类。所有 jvm启动的时候内部就存在线程的竞争，Java为了避免对象锁从偏向锁-轻量锁-重量锁的升级带来的开 销； 锁是针对“同步块”的，只有遇到“同步块”才会出现锁，这句话就解释了“⽆锁可偏向”的语义，因为没有 遇到“代码块”，所以即使具备“偏向标识”也是⽆锁状态； 如果是多个线程交替执⾏：轻量锁；如果是多个线程并发执⾏：重量锁； ⼀、锁升级案例演示

1.⽆锁到偏向锁

场景：

JDK1.8默认开启偏向锁延迟，延迟时间⼤约4s，然后“偏向锁”才开启。我们故意等偏向锁开启后，再 来执⾏代码。 运⾏时对象头锁状态分析⼯具JOL，他是OpenJDK开源⼯具包，引⼊下⽅maven依赖

<dependency> <groupId>org.openjdk.jol</groupId> <artifactId>jol-core</artifactId> <version>0.10</version>

</dependency>

- 1
- 2
- 3
- 4
- 5 public clas T0_ObjectSize {


public static void main(String[] args) throws InteruptedException {

/关闭偏向锁延迟 TimeUnit.SECONDS.sl ep(5); Object o = new Object();

/( 0101 0 0 0) (5)⽆锁可偏向 System.out.println(ClasLayout.parseInstance(o).toPrintable(); synchronized (o){

/( 0101 0101 0 01010 01) (5759045)偏向锁 System.out.println(ClasLayout.parseInstance(o).toPrintable();

} }

}

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10


- 1

- 12
- 13
- 14


- 2.⽆锁到轻量锁


场景：

JDK1.8默认开启偏向锁延迟，延迟时间⼤约4s，然后“偏向锁”才开启。我们就来试试在这4s期间，执 ⾏我们的代码。 ⾄于为什么4s后JVM才开启偏向锁，请看我⽂章开头写的【声明】。 public clas T0_ObjectSize {

public static void main(String[] args) throws InteruptedException {

/关闭偏向锁延迟 / TimeUnit.SECONDS.sl ep(5); Object o = new Object();

/( 0101 0 0 0) (5)⽆锁可偏向 System.out.println(ClasLayout.parseInstance(o).toPrintable(); synchronized (o){

/( 0101 0101 0 01010 01) (5759045)偏向锁 System.out.println(ClasLayout.parseInstance(o).toPrintable();

} }

}

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10 1


- 12
- 13
- 14
- 15 解释：


因为偏向锁在4s后才会有，因此这在这4s期间凡是遇到“同步块”都是“轻量锁”。

- 3.偏向锁升级为轻量锁


场景：

两个线程交替获取这把锁执⾏。 @Slf4j public clas T0_BasicLock {

public static void main(String[] args) { try { /休眠5s，JVM启动了偏向锁

Thread.sl ep(5 0); } catch (InteruptedException e) {

e.printStackTrace(); }

Object o = new Object();

/( 0101 0 0 0) (5) ⽆锁可偏向（只有遇到同步块，才会有锁） System.out.println(ClasLayout.parseInstance(o).toPrintable();

new Thread()->{ synchronized (o){ /( 0101 101 0 0101010 01010) (41890821) 偏向锁

System.out.println(ClasLayout.parseInstance(o).toPrintable(); }

}).start();

try {

/等上⼀个线程执⾏结束，此处在模拟让两个线程交替执⾏ Thread.sl ep(2 0);

} catch (InteruptedException e) {

e.printStackTrace(); }

/( 0101 101 0 0101010 01010) (41890821) 偏向锁 System.out.println(ClasLayout.parseInstance(o).toPrintable(); new Thread()->{

synchronized (o){

/( 1 0 1010 01010 0101) (45606268) 轻量锁，存在两个线程交替执⾏。 System.out.println(ClasLayout.parseInstance(o).toPrintable();

}

}).start(); }

}

- 1
- 2
- 3


- 4
- 5
- 6
- 7
- 8
- 9
- 10


- 1

12 13 14 15 16 17 18 19 20 21

- 2

- 23
- 24
- 25
- 26
- 27
- 28
- 29
- 30
- 31
- 32


- 3

- 34
- 35
- 36
- 37
- 38
- 39


- 4.重量级锁


场景：

两个线程同时去竞争锁，任意⼀个线程竞争成功，故意让它睡眠2s。 为什么要故意睡眠呢？

结论： 刻意避免“⾃旋”优化机制⽣效。 解释： 因为JVM提供了“⾃旋锁”优化机制，当线程1获取锁的时候，线程2不会⽴⻢挂起（1.线程挂起 不仅仅涉及cpu线程上下⽂切换，2.挂起还会使cpu从⽤户态到内核态转换），⽽是通过“⾃旋”10次或 者50次（时间查不到为⼏⼗、⼏百毫秒）的⽅式等线程1释放锁，这样线程2就节省了由于线程挂起造 成的时间成本。 此处的睡眠，就是为了避免“⾃旋锁”的优化机制⽣效。 public clas T0_heavyWeightMonitor {

public static void main(String[] args) throws InteruptedException { Thread.sl ep(5 0); Object a = new Object();

Thread thread1 = new Thread(){ @Overide public void run() {

synchronized (a){ System.out.println("thread1 locking");

/( 01010 1 01010 1010101 010) (4796426)10锁标志是：重量级锁 System.out.println(ClasLayout.parseInstance(a).toPrintable(); try {

/让线程晚点⼉死亡，造成锁的竞争 Thread.sl ep(2 0);

} catch (InteruptedException e) {

e.printStackTrace(); }

} }

}; Thread thread2 = new Thread(){

@Overide public void run() {

synchronized (a){

System.out.println("thread2 locking");

/( 01010 1 01010 1010101 010) (4796426)10锁标志是：重量级锁 System.out.println(ClasLayout.parseInstance(a).toPrintable(); try {

Thread.sl ep(2 0); } catch (InteruptedException e) {

e.printStackTrace(); }

} }

}; thread1.start(); thread2.start();

}

}

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10


- 1


- 12
- 13
- 14
- 15
- 16
- 17
- 18
- 19
- 20
- 21


- 2

- 23
- 24
- 25
- 26
- 27
- 28
- 29
- 30
- 31
- 32


- 3


- 34
- 35
- 36
- 37
- 38
- 39
- 40
- 41
- 42 ⾄此，锁升级的详细案例写完了。但是我想再添加⼀个案例，案例的⽬的是为了让我们更加深刻的理 解⼀个概念- - - 锁状态是记录在对象中的。


⼆、加深“锁状态记录在对象”中概念

- 1.不同对象在不同阶段记录的锁状态可能不同


场景：

jdk8，在两个sout中间休眠4s，等待jvm启动偏向锁；第⼀个sout是 01，第⼆个是101； 注意：

该场景分别new两个对象打印的。 偏向模式在对象初始化的时候就开始作⽤了，不会因为你睡眠了⼏秒之后对象头就变的，开头睡眠⼏ 秒是为了避开 JVM 默认关闭偏向锁，或者你关闭偏向锁延迟，那么程序开始所有对象都是默认有偏向 模式。

- 2.不遇“同步块”，对象中的锁状态不变


场景：

jdk8，我们使⽤的都是⼀个a对象，jvm启动的时候已经给a确定状态为 01了，即使我们后⾯休眠了

- 6s，也改变不了a的锁状态。 注意：


偏向模式在对象初始化的时候就开始作⽤了，不会因为你睡眠了⼏秒之后对象头就变的，开头睡眠⼏ 秒是为了避开 JVM 默认关闭偏向锁，或者你关闭偏向锁延迟，那么程序开始所有对象都是默认有偏向 模式

A a = new A(); System.out.println(ClasLayout.parseInstance(a).toPrintable(); / 01⽆锁可偏向 Thread.sl ep(6 0);

/ 01⽆锁不可偏向，因为此时markword没有记录线程id，只有记录线程id的 01才是 System.out.println(ClasLayout.parseInstance(a).toPrintable(); synchronized (a){

System.out.println(ClasLayout.parseInstance(a).toPrintable(); / 0轻量锁 }

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8 三、JVM锁的膨胀升级详细图例


![image 1](<synchronized锁升级详细讲解案例：代码+流程图.note_images/imageFile1.png>)

![image 2](<synchronized锁升级详细讲解案例：代码+流程图.note_images/imageFile2.png>)

![image 3](<synchronized锁升级详细讲解案例：代码+流程图.note_images/imageFile3.png>)

![image 4](<synchronized锁升级详细讲解案例：代码+流程图.note_images/imageFile4.png>)

来看图中第⼀个例⼦： 这⾥有两个线程，都去操作同⼀个对象Object，对象头⾥有MarkWord，刚开始线程1访问对象的时 候，线程2未进⼊到同步代码块，⽽线程1进⼊了同步代码块，它先要做⼀点事情，即检查当前对象头 中的ThreadID是否是线程1，如果不是，会使⽤CAS修改MarkWord，将对象头中的ThreadID指向线程 1，然后执⾏同步代码块，如果是，则直接执⾏同步代码块。

现在线程2启动，访问同步代码块，也会检查对象头中的ThreadID是否是线程2，尝试使⽤CAS修改 MarkWord，但修改不了（ThreadID是Nul才能改，若不是Nul则不能改），则CAS失败，会开启偏向 锁的撤销，在线程1到达安全点时会暂停它（STW，Stop The World，与GC有关），然后检查线程1是 否退出了同步代码块，如果退出了，则解锁，将对象头中的ThreadID置位空，偏向锁状态改为0，恢复 为⽆所状态，如果未退出，则会升级为⼀个轻量级锁。

这⾥就反映出了偏向锁的性能问题，它的撤销过程要做的事情⾮常多，因此少量同步的场景，不要使 ⽤偏向锁，偏向锁只适合⼀个线程使⽤的场景，⽽轻量级锁适合竞争不激烈的场景，业务⽐较简单， 很快可以执⾏完成，线程间顺序交替执⾏的场景，⽽⼤量同步的场景，不要使⽤重量级锁，有性能问 题（那么使⽤什么呢？）

再来看图中第⼆个例⼦： 还是有两个线程，线程1和线程2都会在栈上分配内存空间，拷⻉MarkWord到Lock Record中，然后通 过CAS去修改对象的MarkWord，此时有可能线程1修改成功，线程2修改失败，若成功，则对象头中的 锁记录指针指向当前栈上Lock Record的指针，升级为轻量级锁，然后执⾏同步代码块，若失败，会发 ⽣⾃旋获取锁（次数可设置，参数-X PreBlockSpin，默认是10次，且是⾃适应⾃旋），⾃旋⼀定次 数依然没有成功，则会发⽣锁膨胀，升级为重量级锁，线程阻塞。

这其中的优化点即是，当线程2修改失败时，并没有让⻢上阻塞，⽽是进⾏⾃适应⾃旋，若⼀直失败， 则锁膨胀，升级为重量级锁，线程阻塞。

线程1在执⾏同步代码块后，会去使⽤CAS修改Mark Word，若成功，则释放锁，若失败，则释放锁， 唤醒阻塞的线程，开始新⼀轮的锁竞争（重量级锁的撤销）。

再谈⼀下重量级锁的撤销，即GC线程在垃圾回收时，会看当前的锁对象除了GC线程外有⽆其他线程， 若没有，则重量级锁会直接降级为⽆锁，重量级锁的降级不是降级为轻量级锁、偏向锁，⽽是垃圾回 收器将它降级为⽆锁。

上⾯的例⼦理解起来⽐较困难的话，看下图，尝试理解.

四、synchronized锁实现与升级过程

![image 5](<synchronized锁升级详细讲解案例：代码+流程图.note_images/imageFile5.png>)

⸻版权声明：本⽂为CSDN博主「来杯咖啡@」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上 原⽂出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/ q_43783527/article/details/14652396

