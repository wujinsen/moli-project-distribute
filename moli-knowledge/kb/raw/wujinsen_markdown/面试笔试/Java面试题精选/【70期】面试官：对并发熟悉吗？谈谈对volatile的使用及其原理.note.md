# htps:/mp.weixin.q.com/s/HYct6C_LJ0wR8ociQCEt8A

# ⼀、volatile的作⽤

我们已经知道可⻅性、有序性及原⼦性问题，通常情况下我们可以通过Synchronized关键字来解决这些个问 题，不过如果对Synchronized原理有了解的话，应该知道Synchronized是⼀个⽐较重量级的操作，对系统的 性能有⽐较⼤的影响，所以，如果有其他解决⽅案，我们通常都避免使⽤Synchronized来解决问题。 ⽽volatile关键字就是Java中提供的另⼀种解决可⻅性和有序性问题的⽅案。对于原⼦性，需要强调⼀点，也 是⼤家容易误解的⼀点：对volatile变量的单次读/写操作可以保证原⼦性的，如long和double类型变量，但是 并不能保证i +这种操作的原⼦性，因为本质上i +是读、写两次操作。

# ⼆、volatile的使⽤

关于volatile的使⽤，我们可以通过⼏个例⼦来说明其使⽤⽅式和场景。

- 1、防⽌重排序 我们从⼀个最经典的例⼦来分析重排序问题。⼤家应该都很熟悉单例模式的实现，⽽在并发环境下的单例实 现⽅式，我们通常可以采⽤双重检查加锁（DCL）的⽅式来实现。其源码如下： package com.paddx.test.concurrent;


public class Singleton { public static volatile Singleton singleton;

/**

- * 构造函数私有，禁⽌外部实例化

- */


private Singleton() {};

public static Singleton getInstance() { if (singleton == null) { synchronized (singleton) { if (singleton == null) {

singleton = new Singleton(); }

}

} return singleton;

} }

现在我们分析⼀下为什么要在变量singleton之间加上volatile关键字。要理解这个问题，先要了解对象的构造 过程，实例化⼀个对象其实可以分为三个步骤：

分配内存空间。 初始化对象。 将内存空间的地址赋值给对应的引⽤。

但是由于操作系统可以对指令进⾏重排序，所以上⾯的过程也可能会变成如下过程：

# 分配内存空间。 将内存空间的地址赋值给对应的引⽤。 初始化对象

如果是这个流程，多线程环境下就可能将⼀个未初始化的对象引⽤暴露出来，从⽽导致不可预料的结果。因 此，为了防⽌这个过程的重排序，我们需要将变量设置为volatile类型的变量。

- 2、实现可⻅性 可⻅性问题主要指⼀个线程修改了共享变量值，⽽另⼀个线程却看不到。引起可⻅性问题的主要原因是每个 线程拥有⾃⼰的⼀个⾼速缓存区⸺线程⼯作内存。 volatile关键字能有效的解决这个问题，我们看下下⾯的例⼦，就可以知道其作⽤： package com.paddx.test.concurrent;


public class VolatileTest {

- int a = 1;

- int b = 2;


public void change(){

- a = 3;

- b = a;


}

public void print(){

System.out.println("b="+b+";a="+a); }

public static void main(String[] args) {

while (true){ final VolatileTest test = new VolatileTest(); new Thread(new Runnable() {

@Override public void run() {

try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace();

} test.change();

} }).start();

new Thread(new Runnable() { @Override public void run() {

try {

Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace();

} test.print();

} }).start();

} }

}

直观上说，这段代码的结果只可能有两种：b=3;a=3 或 b=2;a=1。不过运⾏上⾯的代码（可能时间上要⻓⼀ 点），你会发现除了上两种结果之外，还出现了第三种结果：

......

- b=2;a=1

- b=2;a=1

- b=3;a=3


- b=3;a=3 b=3;a=1 b=3;a=3


- b=2;a=1

- b=3;a=3 b=3;a=3


......

为什么会出现b=3;a=1这种结果呢？正常情况下，如果先执⾏change⽅法，再执⾏print⽅法，输出结果应该 为b=3;a=3。相反，如果先执⾏的print⽅法，再执⾏change⽅法，结果应该是 b=2;a=1。那b=3;a=1的结果 是怎么出来的？ 原因就是第⼀个线程将值a=3修改后，但是对第⼆个线程是不可⻅的，所以才出现这⼀结果。如果将a和b都 改成volatile类型的变量再执⾏，则再也不会出现b=3;a=1的结果了。

- 3、保证原⼦性 关于原⼦性的问题，上⾯已经解释过。volatile只能保证对单次读/写的原⼦性。这个问题可以看下JLS中的描


述：

17.7 Non-Atomic Treatment of double and long

For the purposes of the Java progra ming language memory model, a single write to a non-volatile long or double value is treated as two separate writes: one to each 32-bit half. This can result in a situation where a thread ses the first 32 bits of a 64-bit value from one write, and the second 32 bits from another write.

Writes and reads of volatile long and double values are always atomic.

Writes to and reads of references are always atomic, regardles of whether they are implemented as 32-bit or 64-bit values.

Some implementations may find it convenient to divide a single write action on a 64-bit long or double value into two write actions on adjacent 32-bit values. For eficiency's sake, this behavior is implementation-specific; an implementation of the Java Virtual Machine is fre to perform writes to long and double values atomicaly or in two parts.

Implementations of the Java Virtual Machine are encouraged to avoid spliting 64-bit values where posible. Progra mers are encouraged to declare shared 64-bit values as volatile or synchronize their programs correctly to avoid posible complications.

这段话的内容跟我前⾯的描述内容⼤致类似。因为long和double两种数据类型的操作可分为⾼32位和低32位 两部分，因此普通的long或double类型读/写可能不是原⼦的。因此，⿎励⼤家将共享的long和double变量设 置为volatile类型，这样能保证任何情况下对long和double的单次读/写操作都具有原⼦性。 关于volatile变量对原⼦性保证，有⼀个问题容易被误解。现在我们就通过下列程序来演示⼀下这个问题：

package com.paddx.test.concurrent;

public class VolatileTest01 { volatile int i;

public void addI(){

i++; }

public static void main(String[] args) throws InterruptedException { final VolatileTest01 test01 = new VolatileTest01(); for (int n = 0; n < 1000; n++) {

new Thread(new Runnable() { @Override public void run() {

try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace();

} test01.addI();

}

}).start(); }

Thread.sleep(10000);//等待10秒，保证上⾯程序执⾏完成

System.out.println(test01.i); }

}

⼤家可能会误认为对变量i加上关键字volatile后，这段程序就是线程安全的。⼤家可以尝试运⾏上⾯的程序。 下⾯是我本地运⾏的结果：

![image 1](<【70期】面试官：对并发熟悉吗？谈谈对volatile的使用及其原理.note_images/imageFile1.png>)

可能每个⼈运⾏的结果不相同。不过应该能看出，volatile是⽆法保证原⼦性的（否则结果应该是1 0）。 原因也很简单，i +其实是⼀个复合操作，包括三步骤：

读取i的值。 对i加1。 将i的值写回内存。

volatile是⽆法保证这三个操作是具有原⼦性的，我们可以通过AtomicInteger或者Synchronized来保证+1操 作的原⼦性。

注：上⾯⼏段代码中多处执⾏了Thread.sl ep()⽅法，⽬的是为了增加并发问题的产⽣⼏率，⽆其他作⽤。

# 三、volatile的原理

通过上⾯的例⼦，我们基本应该知道了volatile是什么以及怎么使⽤。现在我们再来看看volatile的底层是怎么 实现的。

- 1、可⻅性实现： 在前⽂中已经提及过，线程本身并不直接与主内存进⾏数据的交互，⽽是通过线程的⼯作内存来完成相应的 操作。这也是导致线程间数据不可⻅的本质原因。因此要实现volatile变量的可⻅性，直接从这⽅⾯⼊⼿即 可。对volatile变量的写操作与普通变量的主要区别有两点：

通过这两个操作，就可以解决volatile变量的可⻅性问题。

- 2、有序性实现： 在解释这个问题前，我们先来了解⼀下Java中的hapen-before规则，JSR 13中对Hapen-before的定义如


修改volatile变量时会强制将修改后的值刷新的主内存中。 修改volatile变量后会导致其他线程⼯作内存中对应的变量值失效。因此，再读取该变量值的时候就 需要重新从读取主内存中的值。

下：

Two actions can be ordered by a hapens-before relationship.If one action hapens before another, then the first is visible to and ordered before the second.

通俗⼀点说就是如果a hapen-before b，则a所做的任何操作对b是可⻅的。（这⼀点⼤家务必记住，因为 hapen-before这个词容易被误解为是时间的前后）。我们再来看看JSR 13中定义了哪些hapen-before规 则：

Each action in a thread hapens before every subsequent action in that thread. An unlock on a monitor hapens before every subsequent lock on that monitor. A write to a volatile field hapens before every subsequent read of that volatile. A cal to start() on a thread hapens before any actions in the started thread. Al actions in a thread hapen before any other thread sucesfuly returns from a join() on that thread.

If an action a hapens before an action b, and b hapens before an action c, then a hapens before c.

翻译过来为：

同⼀个线程中的，前⾯的操作 hapen-before 后续的操作。（即单线程内按代码顺序执⾏。但是， 在不影响在单线程环境执⾏结果的前提下，编译器和处理器可以进⾏重排序，这是合法的。换句话 说，这⼀是规则⽆法保证编译重排和指令重排）。 监视器上的解锁操作 hapen-before 其后续的加锁操作。（Synchronized 规则） 对volatile变量的写操作 hapen-before 后续的读操作。（volatile 规则） 线程的start() ⽅法 hapen-before 该线程所有的后续操作。（线程启动规则） 线程所有的操作 hapen-before 其他线程在该线程上调⽤ join 返回成功后的操作。 如果 a hapen-before b，b hapen-before c，则a hapen-before c（传递性）。

这⾥我们主要看下第三条：volatile变量的保证有序性的规则。为了实现volatile内存语义，J M会对volatile 变量限制这两种类型的重排序。 下⾯是J M针对volatile变量所规定的重排序规则表：

![image 2](<【70期】面试官：对并发熟悉吗？谈谈对volatile的使用及其原理.note_images/imageFile2.png>)

- 3、内存屏障 为了实现volatile可⻅性和hapen-befor的语义。JVM底层是通过⼀个叫做“内存屏障”的东⻄来完成。内存屏 障，也叫做内存栅栏，是⼀组处理器指令，⽤于实现对内存操作的顺序限制。 下⾯是完成上述规则所要求的内存屏障：


![image 3](<【70期】面试官：对并发熟悉吗？谈谈对volatile的使用及其原理.note_images/imageFile3.png>)

LoadLoad 屏障

执⾏顺序：Load1—>Loadload—>Load2 确保Load2及后续Load指令加载数据之前能访问到Load1加载的数据。

StoreStore 屏障

执⾏顺序：Store1—>StoreStore—>Store2 确保Store2以及后续Store指令执⾏前，Store1操作的数据对其它处理器可⻅。

LoadStore 屏障

执⾏顺序：Load1—>LoadStore—>Store2 确保Store2和后续Store指令执⾏前，可以访问到Load1加载的数据。

StoreLoad 屏障

执⾏顺序: Store1—> StoreLoad—>Load2 确保Load2和后续的Load指令读取之前，Store1的数据对其他处理器是可⻅的。

最后我可以通过⼀个实例来说明⼀下JVM中是如何插⼊内存屏障的：

package com.paddx.test.concurrent;

public class MemoryBarrier { int a, b; volatile int v, u;

void f() { int i, j;

- i = a;

- j = b;


- i = v; //LoadLoad

- j = u; //LoadStore


- a = i;

- b = j; //StoreStore


v = i; //StoreStore u = j; //StoreLoad

- i = u; //LoadLoad //LoadStore

- j = b; a = i;


} }

四、总结

总体上来说volatile的理解还是⽐较困难的，如果不是特别理解，也不⽤急，完全理解需要⼀个过程，在后续 的⽂章中也还会多次看到volatile的使⽤场景。这⾥暂且对volatile的基础知识和原来有⼀个基本的了解。 总体来说，volatile是并发编程中的⼀种优化，在某些场景下可以代替Synchronized。但是，volatile的不能完 全取代Synchronized的位置，只有在⼀些特殊的场景下，才能适⽤volatile。总的来说，必须同时满⾜下⾯两 个条件才能保证在并发环境的线程安全：

对变量的写操作不依赖于当前值。 该变量没有包含在具有其他变量的不变式中。

来 源： w.cnblogs.com/padix/p/5428507.html

最近五期

【65期】Spring的IOC是啥?有什么好处? 【 6期】Java容器⾯试题：谈谈你对 HashMap 的理解

- 【67期】谈谈ConcurentHashMap是如何保证线程安全的？

- 【68期】⾯试官：对并发熟悉吗？说说Synchronized及实现原理

- 【69期】⾯试官：对并发熟悉吗？谈谈线程间的协作(wait/notify/sl ep/yield/join)


