volatile的两⼤特性：禁⽌重排序、内存可⻅性，这两个概念，不太清楚的同学可以看这篇⽂章 -> java volatile 关键字解惑 概念是知道了，但还是很迷糊，它们到底是如何实现的？ 本⽂会涉及到⼀些汇编⽅⾯的内容，如果多看⼏遍，应该能看懂。

# 重排序

为了理解重排序，先看⼀段简单的代码

public clas VolatileTest {

- int a = 0;
- int b = 0; public void set() {


- a = 1;
- b = 1;


} public void l op() {

while (b = 0) continue; if (a = 1) {

System.out.println("i'm here"); } else {

System.out.println("what's wrong"); }

} }

VolatileTest类有两个⽅法，分别是set()和loop()，假设线程B执⾏loop⽅法，线程A执⾏set⽅法，会得到什么 结果？ 答案是不确定，因为这⾥涉及到了编译器的重排序和CPU指令的重排序。

编译器重排序

编译器在不改变单线程语义的前提下，为了提⾼程序的运⾏速度，可以对字节码指令进⾏重新排序，所以代 码中a、b的赋值顺序，被编译之后可能就变成了先设置b，再设置a。 因为对于线程A来说，先设置哪个，都不影响⾃身的结果。

CPU指令重排序

CPU指令重排序⼜是怎么回事？ 在深⼊理解之前，先看看x86的cpu缓存结构。

![image 1](<volatile.note_images/imageFile1.png>)

- 1、各种寄存器，⽤来存储本地变量和函数参数，访问⼀次需要1cycle，耗时⼩于1ns；

- 2、L1 Cache，⼀级缓存，本地core的缓存，分成32K的数据缓存L1d和32k指令缓存L1i，访问L1需要 3cycles，耗时⼤约1ns；

- 3、L2 Cache，⼆级缓存，本地core的缓存，被设计为L1缓存与共享的L3缓存之间的缓冲，⼤⼩为256K，访 问L2需要12cycles，耗时⼤约3ns；

- 4、L3 Cache，三级缓存，在同插槽的所有core共享L3缓存，分为多个2M的段，访问L3需要38cycles，耗时 ⼤约12ns； 当然了，还有平时熟知的DRAM，访问内存⼀般需要65ns，所以CPU访问⼀次内存和缓存⽐较起来显得很 慢。 对于不同插槽的CPU，L1和L2的数据并不共享，⼀般通过MESI协议保证Cache的⼀致性，但需要付出代价。 在MESI协议中，每个Cache line有4种状态，分别是：


- 1、M(Modified) 这⾏数据有效，但是被修改了，和内存中的数据不⼀致，数据只存在于本Cache中

- 2、E(Exclusive) 这⾏数据有效，和内存中的数据⼀致，数据只存在于本Cache中

- 3、S(Shared) 这⾏数据有效，和内存中的数据⼀致，数据分布在很多Cache中

- 4、I(Invalid) 这⾏数据⽆效 每个Core的Cache控制器不仅知道⾃⼰的读写操作，也监听其它Cache的读写操作，假如有4个Core：


- 1、Core1从内存中加载了变量X，值为10，这时Core1中缓存变量X的cache line的状态是E；

- 2、Core2也从内存中加载了变量X，这时Core1和Core2缓存变量X的cache line状态转化成S；


- 3、Core3也从内存中加载了变量X，然后把X设置成了20，这时Core3中缓存变量X的cache line状态转化成 M，其它Core对应的cache line变成I（⽆效） 当然了，不同的处理器内部细节也是不⼀样的，⽐如Intel的core i7处理器使⽤从MESI中演化出的MESIF协 议，F(Forward)从Share中演化⽽来，⼀个cache line如果是F状态，可以把数据直接传给其它内核，这⾥就不 纠结了。 CPU在cache line状态的转化期间是阻塞的，经过⻓时间的优化，在寄存器和L1缓存之间添加了LoadBuffer、 StoreBuffer来降低阻塞时间，Buffer与L1进⾏数据传输时，CPU⽆须等待。


- 1、CPU执⾏load读数据时，把读请求放到LoadBuffer，这样就不⽤等待其它CPU响应，先进⾏下⾯操作，稍 后再处理这个读请求的结果。

- 2、CPU执⾏store写数据时，把数据写到StoreBuffer中，待到某个适合的时间点，把StoreBuffer的数据刷到 主存中。 因为StoreBuffer的存在，CPU在写数据时，真实数据并不会⽴即表现到内存中，所以对于其它CPU是不可⻅ 的；同样的道理，LoadBuffer中的请求也⽆法拿到其它CPU设置的最新数据； 由于StoreBuffer和LoadBuffer是异步执⾏的，所以在外⾯看来，先写后读，还是先读后写，没有严格的固定 顺序。 内存可⻅性如何实现 从上⾯的分析可以看出，其实是CPU执⾏load、store数据时的异步性，造成了不同CPU之间的内存不可⻅， 那么如何做到CPU在load的时候可以拿到最新数据呢？ 设置volatile变量 写⼀段简单的java代码，声明⼀个volatile变量，并赋值 public clas VolatileTest {


static volatile int i; public static void main(String[] args){

i = 10; }

}

这段代码本身没什么意义，只是想看看加了volatile之后，编译出来的字节码有什么不同，执⾏ javap verbose VolatileTest 之后，结果如下：

![image 2](<volatile.note_images/imageFile2.png>)

让⼈很失望，没有找类似关键字synchronize编译之后的字节码指令（monitorenter、monitorexit），volatile 编译之后的赋值指令putstatic没有什么不同，唯⼀不同是变量i的修饰flags多了⼀个 ACC_VOLATILE标识。 不过，我觉得可以从这个标识⼊⼿，先全局搜下 ACC_VOLATILE，⽆从下⼿的时候，先看看关键字在哪⾥被使 ⽤了，果然在accessFlags.hpp⽂件中找到类似的名字。

![image 3](<volatile.note_images/imageFile3.png>)

通过 is_volatile()可以判断⼀个变量是否被volatile修饰，然后再全局搜"is_volatile"被使⽤的地⽅，最后 在 bytecodeInterpreter.cpp⽂件中，找到putstatic字节码指令的解释器实现，⾥⾯有 is_volatile()⽅ 法。

![image 4](<volatile.note_images/imageFile4.png>)

当然了，在正常执⾏时，并不会⾛这段逻辑，都是直接执⾏字节码对应的机器码指令，这段代码可以在debug 的时候使⽤，不过最终逻辑是⼀样的。 其中cache变量是java代码中变量i在常量池缓存中的⼀个实例，因为变量i被volatile修饰，所以 cache>is_volatile()为真，给变量i的赋值操作由 release_int_field_put⽅法实现。 再来看看 release_int_field_put⽅法

![image 5](<volatile.note_images/imageFile5.png>)

内部的赋值动作被包了⼀层，OrderAccess::release_store究竟做了魔法，可以让其它线程读到变量i的最新 值。

![image 6](<volatile.note_images/imageFile6.png>)

奇怪，在OrderAccess::release_store的实现中，第⼀个参数强制加了⼀个volatile，很明显，这是c/c++的关 键字。 c/c++中的volatile关键字，⽤来修饰变量，通常⽤于语⾔级别的 memory barrier，在"The C++ Programming Language"中，对volatile的描述如下：

A volatile specifier is a hint to a compiler that an object may change its value in ways not specified by the language so that aggressive optimizations must be avoided.

volatile是⼀种类型修饰符，被volatile声明的变量表示随时可能发⽣变化，每次使⽤时，都必须从变量i对应的 内存地址读取，编译器对操作该变量的代码不再进⾏优化，下⾯写两段简单的c/c++代码验证⼀下

#include <iostream> int fo = 10;

- int a = 1; int main(int argc, const char * argv[]) {

/ insert code here. a = 2; a = fo + 10;

- int b = a + 20; return b;


}

代码中的变量i其实是⽆效的，执⾏ g++-S-O2 main.cpp得到编译之后的汇编代码如下：

![image 7](<volatile.note_images/imageFile7.png>)

可以发现，在⽣成的汇编代码中，对变量a的⼀些⽆效负责操作果然都被优化掉了，如果在声明变量a时加上 volatile

#include <iostream> int fo = 10; volatile int a = 1; int main(int argc, const char * argv[]) {

/ insert code here. a = 2; a = fo + 10; int b = a + 20; return b;

}

再次⽣成汇编代码如下：

![image 8](<volatile.note_images/imageFile8.png>)

和第⼀次⽐较，有以下不同：

- 1、对变量a赋值2的语句，也保留了下来，虽然是⽆效的动作，所以volatile关键字可以禁⽌指令优化，其实这 ⾥发挥了编译器屏障的作⽤； 编译器屏障可以避免编译器优化带来的内存乱序访问的问题，也可以⼿动在代码中插⼊编译器屏障，⽐如下 ⾯的代码和加volatile关键字之后的效果是⼀样 #include <iostream> int fo = 10;


- int a = 1; int main(int argc, const char * argv[]) {

/ insert code here. a = 2;

_asm_ volatile (" : : : "memory"); /编译器屏障 a = fo + 10;

_asm_ volatile (" : : : "memory");

- int b = a + 20; return b;


}

编译之后，和上⾯类似

![image 9](<volatile.note_images/imageFile9.png>)

- 2、其中 _a(%rip)是变量a的每次地址，通过 movl $2,_a(%rip)可以把变量a所在的内存设置成2，关于 RIP，可以查看 x64下PIC的新寻址⽅式：RIP相对寻址 所以，每次对变量a的赋值，都会写⼊到内存中；每次对变量的读取，都会从内存中重新加载。 感觉有点跑偏了，让我们回到JVM的代码中来。


![image 10](<volatile.note_images/imageFile10.png>)

执⾏完赋值操作后，紧接着执⾏ OrderAccess::storeload()，这⼜是啥？ 其实这就是经常会念叨的内存屏障，之前只知道念，却不知道是如何实现的。从CPU缓存结构分析中已经知 道：⼀个load操作需要进⼊LoadBuffer，然后再去内存加载；⼀个store操作需要进⼊StoreBuffer，然后再写 ⼊缓存，这两个操作都是异步的，会导致不正确的指令重排序，所以在JVM中定义了⼀系列的内存屏障来指 定指令的执⾏顺序。 JVM中定义的内存屏障如下，JDK1.7的实现

![image 11](<volatile.note_images/imageFile11.png>)

- 1、loadload屏障（load1，loadload， load2）

- 2、loadstore屏障（load，loadstore， store） 这两个屏障都通过 acquire()⽅法实现

其中 __asm__，表示汇编代码的开始。 volatile，之前分析过了，禁⽌编译器对代码进⾏优化。 把这段指令编 译之后，发现没有看懂....最后的"memory"是编译器屏障的作⽤。 在LoadBuffer中插⼊该屏障，清空屏障之前的load操作，然后才能执⾏屏障之后的操作，可以保证load操作的 数据在下个store指令之前准备好

- 3、storestore屏障（store1，storestore， store2），通过"release()"⽅法实现：

在StoreBuffer中插⼊该屏障，清空屏障之前的store操作，然后才能执⾏屏障之后的store操作，保证store1写 ⼊的数据在执⾏store2时对其它CPU可⻅。

- 4、storeload屏障（store，storeload， load） 对java中的volatile变量进⾏赋值之后，插⼊的就是这个屏障， 通过"fence()"⽅法实现：


![image 12](<volatile.note_images/imageFile12.png>)

![image 13](<volatile.note_images/imageFile13.png>)

![image 14](<volatile.note_images/imageFile14.png>)

看到这个有没有很兴奋？ 先通过 os::is_MP()判断是不是多核，如果只有⼀个CPU的话，就不存在这些问题了。 storeload屏障由下⾯这些指令实现

1. __asm__ volatile ("lock; addl $0,0(%%rsp)" : : : "cc", "memory");

为了试验这些指令到底有什么⽤，我们再写点c++代码编译⼀下

#include <iostream> int fo = 10; int main(int argc, const char * argv[]) {

/ insert code here.

- volatile int a = fo + 10; / _asm_ volatile ("lock; adl $0,0( %rsp)" : : : "c", "memory");
- volatile int b = fo + 20; return 0;


}

为了变量a和b不被编译器优化掉，这⾥使⽤了volatile进⾏修饰，编译后的汇编指令如下：

![image 15](<volatile.note_images/imageFile15.png>)

从编译后的代码可以发现，第⼆次使⽤foo变量时，并没有从内存重新加载，⽽是使⽤了寄存器的值。 把 __asm__volatile***指令加上之后重新编译，结果如下

![image 16](<volatile.note_images/imageFile16.png>)

相⽐之前，这⾥多了两个指令，⼀个lock，⼀个addl。 lock指令的作⽤是：在执⾏lock后⾯指令时，会设置处理器的LOCK#信号（这个信号会锁定总线，阻⽌其它 CPU通过总线访问内存，直到这些指令执⾏结束），这条指令的执⾏变成原⼦操作，之前的读写请求都不能 越过lock指令进⾏重排，相当于⼀个内存屏障。 另⼀个不同的是：第⼆次使⽤foo变量时，从内存中重新加载，保证可以拿到foo变量的最新值，这是由如下指 令实现

1. __asm__ volatile ( : : : "cc", "memory");

这个在之前已经提过，是⼀个编译器屏障，通知编译器重新⽣成加载指令(不可以从缓存寄存器中取)。

## 读取volatile变量

同样在 bytecodeInterpreter.cpp⽂件中，找到getstatic字节码指令的解释器实现。

![image 17](<volatile.note_images/imageFile17.png>)

通过 obj->obj_field_acquire(field_offset)获取变量值

![image 18](<volatile.note_images/imageFile18.png>)

最终通过 OrderAccess::load_acquire实现

1. inline jint OrderAccess::load_acquire(volatile jint* p) { return *p; }

底层基于C++的volatile实现，因为volatile⾃带了编译器屏障的功能，总能拿到内存中的最新值。

