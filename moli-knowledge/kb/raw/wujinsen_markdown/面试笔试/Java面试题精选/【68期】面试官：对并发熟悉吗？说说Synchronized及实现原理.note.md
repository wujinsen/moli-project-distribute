>号外：往期⾯试题，10篇为⼀个单位归置到本公众号菜单栏->⾯试题，有需要的欢迎翻阅。

⼀、Synchronized的基本使⽤

Synchronized是Java中解决并发问题的⼀种最常⽤的⽅法，也是最简单的⼀种⽅法。 Synchronized的作⽤主要有三个：

确保线程互斥的访问同步代码 保证共享变量的修改能够及时可⻅ 有效解决重排序问题。

从语法上讲，Synchronized总共有三种⽤法：

修饰普通⽅法 修饰静态⽅法 修饰代码块

接下来我就通过⼏个例⼦程序来说明⼀下这三种使⽤⽅式（为了便于⽐较，三段代码除了Synchronized的使 ⽤⽅式不同以外，其他基本保持⼀致）。

- 1、没有同步的情况： 代码段⼀： package com.paddx.test.concurrent;


public class SynchronizedTest {

public void method1(){ System.out.println("Method 1 start"); try {

System.out.println("Method 1 execute"); Thread.sleep(3000);

} catch (InterruptedException e) {

e.printStackTrace(); }

- System.out.println("Method 1 end");

}

public void method2(){

- System.out.println("Method 2 start"); try {


System.out.println("Method 2 execute"); Thread.sleep(1000);

} catch (InterruptedException e) { e.printStackTrace();

} System.out.println("Method 2 end");

}

public static void main(String[] args) { final SynchronizedTest test = new SynchronizedTest();

new Thread(new Runnable() { @Override public void run() {

test.method1(); }

}).start();

new Thread(new Runnable() { @Override public void run() {

test.method2(); }

}).start(); }

}

执⾏结果如下，线程1和线程2同时进⼊执⾏状态，线程2执⾏速度⽐线程1快，所以线程2先执⾏完成，这个过 程中线程1和线程2是同时执⾏的。

- Method 1 start

- Method 1 execute

- Method 2 start


- Method 2 execute Method 2 end Method 1 end


- 2、对普通⽅法同步： 代码段⼆： package com.paddx.test.concurrent;


public class SynchronizedTest {

public synchronized void method1(){ System.out.println("Method 1 start"); try {

System.out.println("Method 1 execute"); Thread.sleep(3000);

} catch (InterruptedException e) {

e.printStackTrace(); }

- System.out.println("Method 1 end");

}

public synchronized void method2(){

- System.out.println("Method 2 start"); try {


System.out.println("Method 2 execute"); Thread.sleep(1000);

} catch (InterruptedException e) { e.printStackTrace();

} System.out.println("Method 2 end");

}

public static void main(String[] args) { final SynchronizedTest test = new SynchronizedTest();

new Thread(new Runnable() { @Override public void run() {

test.method1(); }

}).start();

new Thread(new Runnable() { @Override public void run() {

test.method2(); }

}).start(); }

}

执⾏结果如下，跟代码段⼀⽐较，可以很明显的看出，线程2需要等待线程1的method1执⾏完成才能开始执 ⾏method2⽅法。

- Method 1 start

- Method 1 execute

- Method 1 end

- Method 2 start


- Method 2 execute


- Method 2 end


- 3、静态⽅法（类）同步


代码段三：

package com.paddx.test.concurrent;

public class SynchronizedTest {

public static synchronized void method1(){ System.out.println("Method 1 start"); try {

System.out.println("Method 1 execute"); Thread.sleep(3000);

} catch (InterruptedException e) {

e.printStackTrace(); }

- System.out.println("Method 1 end");

}

public static synchronized void method2(){

- System.out.println("Method 2 start"); try {


System.out.println("Method 2 execute"); Thread.sleep(1000);

} catch (InterruptedException e) { e.printStackTrace();

} System.out.println("Method 2 end");

}

public static void main(String[] args) { final SynchronizedTest test = new SynchronizedTest(); final SynchronizedTest test2 = new SynchronizedTest();

new Thread(new Runnable() { @Override public void run() {

test.method1(); }

}).start();

new Thread(new Runnable() { @Override public void run() {

test2.method2(); }

}).start(); }

}

执⾏结果如下，对静态⽅法的同步本质上是对类的同步（静态⽅法本质上是属于类的⽅法，⽽不是对象上的 ⽅法），所以即使test和test2属于不同的对象，但是它们都属于SynchronizedTest类的实例，所以也只能顺 序的执⾏method1和method2，不能并发执⾏。

- Method 1 start

- Method 1 execute

- Method 1 end

- Method 2 start


- Method 2 execute


- Method 2 end


- 4、代码块同步


代码段四：

package com.paddx.test.concurrent;

public class SynchronizedTest {

public void method1(){ System.out.println("Method 1 start"); try {

synchronized (this) { System.out.println("Method 1 execute"); Thread.sleep(3000);

} } catch (InterruptedException e) {

e.printStackTrace(); }

- System.out.println("Method 1 end");

}

public void method2(){

- System.out.println("Method 2 start"); try {


synchronized (this) { System.out.println("Method 2 execute"); Thread.sleep(1000);

} } catch (InterruptedException e) { e.printStackTrace();

} System.out.println("Method 2 end");

}

public static void main(String[] args) { final SynchronizedTest test = new SynchronizedTest();

new Thread(new Runnable() { @Override public void run() {

test.method1(); }

}).start();

new Thread(new Runnable() { @Override public void run() {

test.method2();

}

}).start(); }

}

执⾏结果如下，虽然线程1和线程2都进⼊了对应的⽅法开始执⾏，但是线程2在进⼊同步块之前，需要等待线 程1中同步块执⾏完成。

- Method 1 start

- Method 1 execute

- Method 2 start


- Method 1 end

- Method 2 execute


- Method 2 end ⼆、Synchronized 原理 如果对上⾯的执⾏结果还有疑问，也先不⽤急，我们先来了解Synchronized的原理，再回头上⾯的问题就⼀ ⽬了然了。我们先通过反编译下⾯的代码来看看Synchronized是如何实现对代码块进⾏同步的： package com.paddx.test.concurrent;


public class SynchronizedDemo { public void method() { synchronized (this) {

System.out.println("Method 1 start"); }

} }

反编译结果：

![image 1](<【68期】面试官：对并发熟悉吗？说说Synchronized及实现原理.note_images/imageFile1.png>)

关于这两条指令的作⽤，我们直接参考JVM规范中描述： monitorenter ：

Each object is asociated with a monitor. A monitor is locked if and only if it has an owner. The thread that executes monitorenter atempts to gain ownership of the monitor asociated with objectref, as folows:

If the entry count of the monitor asociated with objectref is zero, the thread enters the monitor and sets its entry count to one. The thread is then the owner of the monitor. If the thread already owns the monitor asociated with objectref, it renters the monitor, incrementing its entry count. If another thread already owns the monitor asociated with objectref, the thread blocks until the monitor's entry count is zero, then tries again to gain ownership.

这段话的⼤概意思为： 每 个 对 象 有 ⼀ 个 监 视 器 锁 （ monitor） 。当 monitor被 占 ⽤ 时 就 会 处 于 锁 定 状 态 ， 线 程 执 ⾏ monitorenter指 令 时 尝 试 获 取 monitor的 所 有权 ， 过 程 如 下：

如 果 monitor的 进 ⼊ 数 为 0， 则 该 线 程 进 ⼊ monitor， 然 后 将 进 ⼊ 数 设 置 为 1， 该 线 程 即 为 monitor的 所 有 者 。 如 果 线 程 已 经 占 有 该 monitor， 只 是 重 新 进 ⼊ ， 则 进 ⼊ monitor的 进 ⼊ 数 加 1. 如 果 其 他 线 程 已 经 占 ⽤ 了 monitor， 则 该 线 程 进 ⼊ 阻 塞 状 态 ， 直 到 monitor的 进 ⼊ 数 为 0， 再 重 新 尝 试 获 取 monitor的 所 有权 。

monitorexit：

The thread that executes monitorexit must be the owner of the monitor asociated with the instance referenced by objectref. The thread decrements the entry count of the monitor asociated with objectref. If as a result the value of the entry count is zero, the thread exits the monitor and is no longer its owner. Other threads that are blocking to enter the monitor are alowed to atempt to do so.

这段话的⼤概意思为： 执 ⾏ monitorexit的 线 程 必 须 是 objectref所 对 应 的 monitor的 所 有 者 。 指 令 执 ⾏ 时 ， monitor的 进 ⼊ 数 减 1， 如 果 减 1后 进 ⼊ 数 为 0， 那 线 程 退 出 monitor，不 再 是 这 个 monitor的 所 有 者 。其 他 被 这 个 monitor阻 塞 的 线 程 可 以 尝 试 去 获 取 这 个 monitor 的 所 有权 。 通过这两段描述，我们应该能很清楚的看出Synchronized的实现原理，Synchronized的语义底层是通过⼀个 monitor的对象来完成，其实wait/notify等⽅法也依赖于monitor对象，这就是为什么只有在同步的块或者⽅法 中才能调⽤wait/notify等⽅法，否则会抛出java.lang.IlegalMonitorStateException的异常的原因。 我们再来看⼀下同步⽅法的反编译结果： 源代码：

package com.paddx.test.concurrent;

public class SynchronizedMethod { public synchronized void method() {

System.out.println("Hello World!"); }

}

反编译结果：

![image 2](<【68期】面试官：对并发熟悉吗？说说Synchronized及实现原理.note_images/imageFile2.png>)

从反编译的结果来看，⽅法的同步并没有通过指令monitorenter和monitorexit来完成（理论上其实也可以通 过这两条指令来实现），不过相对于普通⽅法，其常量池中多了AC_SYNCHRONIZED标示符。 JVM就是根据该标示符来实现⽅法的同步的：当⽅法调⽤时，调⽤指令将会检查⽅法的 AC_SYNCHRONIZED 访问标志是否被设置，如果设置了，执⾏线程将先获取monitor，获取成功之后才能 执⾏⽅法体，⽅法执⾏完后再释放monitor。 在⽅法执⾏期间，其他任何线程都⽆法再获得同⼀个monitor对象。其实本质上没有区别，只是⽅法的同步是 ⼀种隐式的⽅式来实现，⽆需通过字节码来完成。

# 三、运⾏结果解释

有了对Synchronized原理的认识，再来看上⾯的程序就可以迎刃⽽解了。

- 1、代码段2结果： 虽然method1和method2是不同的⽅法，但是这两个⽅法都进⾏了同步，并且是通过同⼀个对象去调⽤的， 所以调⽤之前都需要先去竞争同⼀个对象上的锁（monitor），也就只能互斥的获取到锁，因此，method1和 method2只能顺序的执⾏。

- 2、代码段3结果： 虽然test和test2属于不同对象，但是test和test2属于同⼀个类的不同实例，由于method1和method2都属于 静态同步⽅法，所以调⽤的时候需要获取同⼀个类上monitor（每个类只对应⼀个clas对象），所以也只能顺 序的执⾏。

- 3、代码段4结果： 对于代码块的同步实质上需要获取Synchronized关键字后⾯括号中对象的monitor，由于这段代码中括号的内 容都是this，⽽method1和method2⼜是通过同⼀的对象去调⽤的，所以进⼊同步块之前需要去竞争同⼀个对 象上的锁，因此只能顺序执⾏同步块。 四 总结 Synchronized是Java并发编程中最常⽤的⽤于保证线程安全的⽅式，其使⽤相对也⽐较简单。但是如果能够 深⼊了解其原理，对监视器锁等底层知识有所了解，⼀⽅⾯可以帮助我们正确的使⽤Synchronized关键字， 另⼀⽅⾯也能够帮助我们更好的理解并发编程机制，有助我们在不同的情况下选择更优的并发策略来完成任 务。对平时遇到的各种并发问题，也能够从容的应对。 来 源： w.cnblogs.com/padix/p/536716.html


最近五期

- 【63期】谈谈MySQL 索引，B+树原理，以及建索引的⼏⼤原则（MySQL⾯试第六弹）

- 【64期】MySQL 服务占⽤cpu 10%，如何排查问题? （MySQL⾯试第七弹）


# 【65期】Spring的IOC是啥?有什么好处? 【 6期】Java容器⾯试题：谈谈你对 HashMap 的理解 【67期】谈谈ConcurentHashMap是如何保证线程安全的？

