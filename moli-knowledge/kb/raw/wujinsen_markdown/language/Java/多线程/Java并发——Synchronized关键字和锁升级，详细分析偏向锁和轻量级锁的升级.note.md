⼀、Synchronized使⽤场景

Synchronized是⼀个同步关键字，在某些多线程场景下，如果不进⾏同步会导致数据不安全，⽽ Synchronized关键字就是⽤于代码同步。什么情况下会数据不安全呢，要满⾜两个条件：⼀是数据共 享（临界资源），⼆是多线程同时访问并改变该数据。

例如：

public clas AcountingSync implements Runable{

/共享资源(临界资源) static int i=0;

/*

- * synchronized 修饰实例⽅法
- */ public synchronized void increase(){


i +;

} @Overide public void run() {

for(int j=0;j<1 0;j +){

increase(); }

} public static void main(String[] args) throws InteruptedException {

AcountingSync instance=new AcountingSync();

- Thread t1=new Thread(instance);
- Thread t2=new Thread(instance);


- t1.start();
- t2.start();


- t1.join();
- t2.join(); System.out.println(i);


} 该段程序的输出为：2 0

但是如果increase的synchronized被删除，那么很可能输出结果就会⼩于2 0，这是因为多个线 程同时访问临界资源i，如果⼀个线程A对i=8的⾃增到89没有被B线程读取到，线程B认为i仍然是

8，那么线程B对i的⾃增结果还是89，那么这⾥就会出现问题。

Synchronized锁的3种使⽤形式（使⽤场景）：

Synchronized修饰普通同步⽅法：锁对象当前实例对象； Synchronized修饰静态同步⽅法：锁对象是当前的类Clas对象； Synchronized修饰同步代码块：锁对象是Synchronized后⾯括号⾥配置的对象，这个对象可以是某个 对象（xlock），也可以是某个类（Xlock.clas）； 注意：

使⽤synchronized修饰⾮静态⽅法或者使⽤synchronized修饰代码块时制定的为实例对象时，同⼀个 类的不同对象拥有⾃⼰的锁，因此不会相互阻塞。 使⽤synchronized修饰类和对象时，由于类对象和实例对象分别拥有⾃⼰的监视器锁，因此不会相互 阻塞。 使⽤使⽤synchronized修饰实例对象时，如果⼀个线程正在访问实例对象的⼀个synchronized⽅法 时，其它线程不仅不能访问该synchronized⽅法，该对象的其它synchronized⽅法也不能访问，因为 ⼀个对象只有⼀个监视器锁对象，但是其它线程可以访问该对象的⾮synchronized⽅法。 线程A访问实例对象的⾮static synchronized⽅法时，线程B也可以同时访问实例对象的static synchronized⽅法，因为前者获取的是实例对象的监视器锁，⽽后者获取的是类对象的监视器锁，两 者不存在互斥关系。

⼆、Synchronized实现原理

- 1、Java对象头


⾸先，我们要知道对象在内存中的布局：

已知对象是存放在堆内存中的，对象⼤致可以分为三个部分，分别是对象头、实例变量和填充字节。

对象头的zhuyao是由MarkWord和Klas Point(类型指针)组成，其中Klas Point是是对象指向它的类元 数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例，Mark Word⽤于存储对象⾃身的 运⾏时数据。如果对象是数组对象，那么对象头占⽤3个字宽（Word），如果对象是⾮数组对象，那 么对象头占⽤2个字宽。（1word = 2 Byte = 16 bit） 实例变量存储的是对象的属性信息，包括⽗类的属性信息，按照4字节对⻬ 填充字符，因为虚拟机要求对象字节必须是8字节的整数倍，填充字符就是⽤于凑⻬这个整数倍的

通过第⼀部分可以知道，Synchronized不论是修饰⽅法还是代码块，都是通过持有修饰对象的锁来实 现同步，那么Synchronized锁对象是存在哪⾥的呢？答案是存在锁对象的对象头的MarkWord中。那么 MarkWord在对象头中到底⻓什么样，也就是它到底存储了什么呢？

在32位的虚拟机中：

在64位的虚拟机中：

上图中的偏向锁和轻量级锁都是在java6以后对锁机制进⾏优化时引进的，下⽂的锁升级部分会具体讲 解，Synchronized关键字对应的是重量级锁，接下来对重量级锁在Hotspot JVM中的实现锁讲解。

- 2、Synchronized在JVM中的实现原理


重量级锁对应的锁标志位是10，存储了指向重量级监视器锁的指针，在Hotspot中，对象的监视器 （monitor）锁对象由ObjectMonitor对象实现（C+），其跟同步相关的数据结构如下：

ObjectMonitor() { _count = 0; /⽤来记录该对象被线程获取锁的次数 _waiters = 0; _recursions = 0; /锁的重⼊次数 _owner = NUL; /指向持有ObjectMonitor对象的线程 _WaitSet = NUL; /处于wait状态的线程，会被加⼊到_WaitSet _WaitSetLock = 0 ; _EntryList = NUL ; /处于等待锁block状态的线程，会被加⼊到该列表

光看这些数据结构对监视器锁的⼯作机制还是⼀头雾⽔，那么我们⾸先看⼀下线程在获取锁的⼏个状 态的转换：

线程的⽣命周期存在5个状态，start、runing、waiting、blocking和dead

对于⼀个synchronized修饰的⽅法(代码块)来说：

当多个线程同时访问该⽅法，那么这些线程会先被放进_EntryList队列，此时线程处于blocking状态 当⼀个线程获取到了实例对象的监视器（monitor）锁，那么就可以进⼊runing状态，执⾏⽅法，此 时，ObjectMonitor对象的_owner指向当前线程，_count加1表示当前对象锁被⼀个线程获取 当runing状态的线程调⽤wait()⽅法，那么当前线程释放monitor对象，进⼊waiting状态， ObjectMonitor对象的_owner变为nul，_count减1，同时线程进⼊_WaitSet队列，直到有线程调⽤ notify()⽅法唤醒该线程，则该线程重新获取monitor对象进⼊_Owner区 如果当前线程执⾏完毕，那么也释放monitor对象，进⼊waiting状态，ObjectMonitor对象的_owner变 为nul，_count减1 那么Synchronized修饰的代码块/⽅法如何获取monitor对象的呢？

在JVM规范⾥可以看到，不管是⽅法同步还是代码块同步都是基于进⼊和退出monitor对象来实现，然 ⽽⼆者在具体实现上⼜存在很⼤的区别。通过javap对clas字节码⽂件反编译可以得到反编译后的代 码。

- （1）Synchronized修饰代码块：


Synchronized代码块同步在需要同步的代码块开始的位置插⼊monitorentry指令，在同步结束的位置 或者异常出现的位置插⼊monitorexit指令；JVM要保证monitorentry和monitorexit都是成对出现的， 任何对象都有⼀个monitor与之对应，当这个对象的monitor被持有以后，它将处于锁定状态。

例如，同步代码块如下：

public clas SyncCodeBlock { public int i; public void syncTask(){

synchronized (this){

i +; }

}

} 对同步代码块编译后的clas字节码⽂件反编译，结果如下（仅保留⽅法部分的反编译内容）：

public void syncTask(); descriptor: ()V flags: AC_PUBLIC Code:

stack=3, locals=3, args_size=1

- 0: aload_0
- 1: dup
- 2: astore_1
- 3: monitorenter /注意此处，进⼊同步⽅法
- 4: aload_0
- 5: dup
- 6: getfield #2 / Field i:I


- 9: iconst_1
- 10: iad


- 1: putfield #2 / Field i:I

- 14: aload_1
- 15: monitorexit /注意此处，退出同步⽅法
- 16: goto 24


- 19: astore_2
- 20: aload_1
- 21: monitorexit /注意此处，退出同步⽅法


- 2: aload_2


- 23: athrow
- 24: return


Exception table:

/省略其他字节码 . 可以看出同步⽅法块在进⼊代码块时插⼊了monitorentry语句，在退出代码块时插⼊了monitorexit语 句，为了保证不论是正常执⾏完毕（第15⾏）还是异常跳出代码块（第21⾏）都能执⾏monitorexit语 句，因此会出现两句monitorexit语句。

- （2）Synchronized修饰⽅法：


Synchronized⽅法同步不再是通过插⼊monitorentry和monitorexit指令实现，⽽是由⽅法调⽤指令来 读取运⾏时常量池中的AC_SYNCHRONIZED标志隐式实现的，如果⽅法表结构（method_info Structure）中的AC_SYNCHRONIZED标志被设置，那么线程在执⾏⽅法前会先去获取对象的 monitor对象，如果获取成功则执⾏⽅法代码，执⾏完毕后释放monitor对象，如果monitor对象已经被 其它线程获取，那么当前线程被阻塞。

同步⽅法代码如下：

public clas SyncMethod { public int i; public synchronized void syncTask(){

i +; }

} 对同步⽅法编译后的clas字节码反编译，结果如下（仅保留⽅法部分的反编译内容）：

public synchronized void syncTask(); descriptor: ()V

/⽅法标识AC_PUBLIC代表public修饰，AC_SYNCHRONIZED指明该⽅法为同步⽅法 flags: AC_PUBLIC, AC_SYNCHRONIZED Code:

stack=3, locals=1, args_size=1

- 0: aload_0
- 1: dup
- 2: getfield #2 / Field i:I


- 5: iconst_1
- 6: iad
- 7: putfield #2 / Field i:I 10: return


LineNumberTable:

- line 12: 0
- line 13: 10


} 可以看出⽅法开始和结束的地⽅都没有出现monitorentry和monitorexit指令，但是出现的 AC_SYNCHRONIZED标志位。

三、锁的优化

- 1、锁升级


锁的4中状态：⽆锁状态、偏向锁状态、轻量级锁状态、重量级锁状态（级别从低到⾼）

- （1）偏向锁：

为什么要引⼊偏向锁？

因为经过HotSpot的作者⼤量的研究发现，⼤多数时候是不存在锁竞争的，常常是⼀个线程多次获得同 ⼀个锁，因此如果每次都要竞争锁会增⼤很多没有必要付出的代价，为了降低获取锁的代价，才引⼊ 的偏向锁。

偏向锁的升级

当线程1访问代码块并获取锁对象时，会在java对象头和栈帧中记录偏向的锁的threadID，因为偏向锁 不会主动释放锁，因此以后线程1再次获取锁的时候，需要⽐较当前线程的threadID和Java对象头中的 threadID是否⼀致，如果⼀致（还是线程1获取锁对象），则⽆需使⽤CAS来加锁、解锁；如果不⼀致 （其他线程，如线程2要竞争锁对象，⽽偏向锁不会主动释放因此还是存储的线程1的threadID），那 么需要查看Java对象头中记录的线程1是否存活，如果没有存活，那么锁对象被重置为⽆锁状态，其它 线程（线程2）可以竞争将其设置为偏向锁；如果存活，那么⽴刻查找该线程（线程1）的栈帧信息， 如果还是需要继续持有这个锁对象，那么暂停当前线程1，撤销偏向锁，升级为轻量级锁，如果线程1 不再使⽤该锁对象，那么将锁对象状态设为⽆锁状态，重新偏向新的线程。

偏向锁的取消：

偏向锁是默认开启的，⽽且开始时间⼀般是⽐应⽤程序启动慢⼏秒，如果不想有这个延迟，那么可以 使⽤-X BiasedLockingStartUpDelay=0；

如果不想要偏向锁，那么可以通过-X:-UseBiasedLocking = false来设置；

- （2）轻量级锁


为什么要引⼊轻量级锁？

轻量级锁考虑的是竞争锁对象的线程不多，⽽且线程持有锁的时间也不⻓的情景。因为阻塞线程需要 CPU从⽤户态转到内核态，代价较⼤，如果刚刚阻塞不久这个锁就被释放了，那这个代价就有点得不 偿失了，因此这个时候就⼲脆不阻塞这个线程，让它⾃旋这等待锁释放。

轻量级锁什么时候升级为重量级锁？

线程1获取轻量级锁时会先把锁对象的对象头MarkWord复制⼀份到线程1的栈帧中创建的⽤于存储锁记 录的空间（称为DisplacedMarkWord），然后使⽤CAS把对象头中的内容替换为线程1存储的锁记录 （DisplacedMarkWord）的地址；

如果在线程1复制对象头的同时（在线程1CAS之前），线程2也准备获取锁，复制了对象头到线程2的 锁记录空间中，但是在线程2CAS的时候，发现线程1已经把对象头换了，线程2的CAS失败，那么线程 2就尝试使⽤⾃旋锁来等待线程1释放锁。

但是如果⾃旋的时间太⻓也不⾏，因为⾃旋是要消耗CPU的，因此⾃旋的次数是有限制的，⽐如10次 或者10次，如果⾃旋次数到了线程1还没有释放锁，或者线程1还在执⾏，线程2还在⾃旋等待，这时 ⼜有⼀个线程3过来竞争这个锁对象，那么这个时候轻量级锁就会膨胀为重量级锁。重量级锁把除了拥 有锁的线程都阻塞，防⽌CPU空转。

*注意：为了避免⽆⽤的⾃旋，轻量级锁⼀旦膨胀为重量级锁就不会再降级为轻量级锁了；偏向锁升级 为轻量级锁也不能再降级为偏向锁。⼀句话就是锁可以升级不可以降级，但是偏向锁状态可以被重置 为⽆锁状态。

- （3）这⼏种锁的优缺点（偏向锁、轻量级锁、重量级锁）


- 2、锁粗化


按理来说，同步块的作⽤范围应该尽可能⼩，仅在共享数据的实际作⽤域中才进⾏同步，这样做的⽬ 的是为了使需要同步的操作数量尽可能缩⼩，缩短阻塞时间，如果存在锁竞争，那么等待锁的线程也 能尽快拿到锁。 但是加锁解锁也需要消耗资源，如果存在⼀系列的连续加锁解锁操作，可能会导致不必要的性能损 耗。

锁粗化就是将多个连续的加锁、解锁操作连接在⼀起，扩展成⼀个范围更⼤的锁，避免频繁的加锁解 锁操作。

- 3、锁消除


Java虚拟机在JIT编译时(可以简单理解为当某段代码即将第⼀次被执⾏时进⾏编译，⼜称即时编译)， 通过对运⾏上下⽂的扫描，经过逃逸分析，去除不可能存在共享资源竞争的锁，通过这种⽅式消除没 有必要的锁，可以节省毫⽆意义的请求锁时间

⸻版权声明：本⽂为CSDN博主「tongdanping」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上 原⽂出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/tongdanping/article/details/7964737

