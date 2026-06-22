# ⼀、虚拟机

![image 1](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile1.png>)

同样的java代码在不同平台⽣成的机器码肯定是不⼀样的，因为不同的操作系统底层的硬件指令 集是不同的。

同⼀个java代码在windows上⽣成的机器码可能是0101 .，在linux上⽣成的可能是 10 .， 那么这是怎么实现的呢？

不知道同学们还记不记得，在下载jdk的时候，我们在oracle官⽹，基于不同的操作系统或者位数 版本要下载不同的jdk版本，也就是说针对不同的操作系统，jdk虚拟机有不同的实现。

那么虚拟机⼜是什么东⻄呢，如图是从软件层⾯屏蔽不同操作系统在底层硬件与指令上的区别， 也就是跨平台的由来。

说到这⾥同学们可能还是有点不太明⽩，说的还是太宏观了，那我们来了解下java虚拟机的组 成。

# ⼆、虚拟机组成

![image 2](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile2.png>)

1.栈

我们先讲⼀下其中的⼀块内存区域栈，⼤家都知道栈是存储局部变量的，也是线程独有的区域， 也就是每⼀个线程都会有⾃⼰独⽴的栈区域。

public class Math { public static int initData = 666; public static User user = new User();

public int compute() {

- int a = 1;

- int b = 2;

- int c = (a+b) * 10; return c;


}

public static void main(String[] args) {

Math math = new Math(); math.compute(); System.out.println("test");

} }

说起栈⼤家都不会陌⽣，数据结构中就有学，这⾥线程栈中存储数据的部分使⽤的就是栈，先进 后出。

⼤家都知道每个⽅法都有⾃⼰的局部变量，⽐如上图中main⽅法中的math，compute⽅法中的a b c，那么java虚拟机为了区分不同⽅法中局部变量作⽤域范围的内存区域，每个⽅法在运⾏的时 候都会分配⼀块独⽴的栈帧内存区域，我们试着按上图中的程序来简单画⼀下代码执⾏的内存活 动。

![image 3](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile3.png>)

执⾏main⽅法中的第⼀⾏代码是，栈中会分配main()⽅法的栈帧，并存储math局部变量,，接着 执⾏compute()⽅法，那么栈⼜会分配compute()的栈帧区域。

这⾥的栈存储数据的⽅式和数据结构中学习的栈是⼀样的，先进后出。当compute()⽅法执⾏完 之后，就会出栈被释放，也就符合先进后出的特点，后调⽤的⽅法先出栈。

## 栈帧

那么栈帧内部其实不只是存放局部变量的，它还有⼀些别的东⻄，主要由四个部分组成。

![image 4](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile4.png>)

那么要讲这个就会涉及到更底层的原理 -字节码。我们先看下我们上⾯代码的字节码⽂件。

![image 5](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile5.png>)

看着就是⼀个16字节的⽂件，看着像乱码，其实每个都是有对应的含义的，oracle官⽅是有专⻔ 的jvm字节码指令⼿册来查询每组指令对应的含义的。那我们研究的，当然不是这个。

jdk有⾃带⼀个javap的命令，可以将上述clas⽂件⽣成⼀种更可读的字节码⽂件。

![image 6](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile6.png>)

我们使⽤javap -c命令将clas⽂件反编译并输出到TXT⽂件中。

Compiled from "Math.java" public class com.example.demo.test1.Math {

public static int initData;

public static com.example.demo.bean.User user;

public com.example.demo.test1.Math(); Code:

- 0: aload_0

- 1: invokespecial #1 // Method java/lang/Object."<init>":()V 4: return


public int compute(); Code:

- 0: iconst_1

- 1: istore_1

- 2: iconst_2

- 3: istore_2

- 4: iload_1

- 5: iload_2

- 6: iadd

- 7: bipush 10


- 9: imul

- 10: istore_3

- 11: iload_3

- 12: ireturn


public static void main(java.lang.String[]);

Code: 0: new #2 // class com/example/demo/test1/Math

- 3: dup

- 4: invokespecial #3 // Method "<init>":()V


- 7: astore_1

- 8: aload_1

- 9: invokevirtual #4 // Method compute:()I


- 12: pop

- 13: getstatic #5 // Field java/lang/System.out:Ljava/io/PrintStream; 16: ldc #6 // String test 18: invokevirtual #7 // Method java/io/PrintStream.println:


(Ljava/lang/String;)V 21: return

static {};

Code: 0: sipush 666 3: putstatic #8 // Field initData:I 6: new #9 // class com/example/demo/bean/User

- 9: dup

- 10: invokespecial #10 // Method com/example/demo/bean/User."<init>":


()V

13: putstatic #11 // Field user:Lcom/example/demo/bean/User; 16: return

}

此时的jvm指令码就清晰很多了，⼤体结构是可以看懂的，类、静态变量、构造⽅法、 compute()⽅法、main()⽅法。

其中⽅法中的指令还是有点懵，我们举compute()⽅法来看⼀下：

Code:

- 0: iconst_1

- 1: istore_1

- 2: iconst_2

- 3: istore_2

- 4: iload_1

- 5: iload_2

- 6: iadd

- 7: bipush 10


- 9: imul

- 10: istore_3

- 11: iload_3

- 12: ireturn


这⼏⾏代码就是对应的我们代码中compute()⽅法中的四⾏代码。⼤家都知道越底层的代码，代 码实现的⾏数越多，因为他会包含⼀些java代码在运⾏时底层隐藏的⼀些细节原理。

那么⼀样的，这个jvm指令官⽅也是有⼿册可以查阅的，⽹上也有很多翻译版本，⼤家如果想了 解可⾃⾏百度。

这⾥我只讲解本博⽂设计代码中的部分指令含义：

- 0. 将int类型常量1压⼊操作数栈

- 0: iconst_1

这⼀步很简单，就是将1压⼊操作数栈

1. 将int类型值存⼊局部变量1

- 1: istore_1




![image 7](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile7.png>)

局部变量1，在我们代码中也就是第⼀个局部变量a，先给a在局部变量表中分配内存，然后将int 类型的值，也就是⽬前唯⼀的⼀个1存⼊局部变量a

![image 8](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile8.png>)

### 2. 将int类型常量2压⼊操作数栈

- 2: iconst_2

3. 将int类型值存⼊局部变量2

- 3: istore_2

这两⾏代码就和前两⾏类似了。

4. 从局部变量1中装载int类型值

- 4: iload_1

5. 从局部变量2中装载int类型值

- 5: iload_2


![image 9](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile9.png>)

这两个代码是将局部变量1和2，也就是a和b的值装载到操作数栈中

![image 10](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile10.png>)

- 6. 执⾏int类型的加法

- 6: iadd

iad指令⼀执⾏，会将操作数栈中的1和2依次从栈底弹出并相加，然后把运算结果3在压⼊操作 数栈底。

7. 将⼀个8位带符号整数压⼊栈

- 7: bipush 10




![image 11](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile11.png>)

这个指令就是将10压⼊栈

![image 12](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile12.png>)

- 8. 执⾏int类型的乘法

- 9: imul

这⾥就类似上⾯的加法了，将3和10弹出栈，把结果30压⼊栈

9. 将将int类型值存⼊局部变量3

- 10: istore_3




![image 13](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile13.png>)

这⾥⼤家就不陌⽣了吧，和第⼆步第三步是⼀样的，将30存⼊局部变量3，也就是c

![image 14](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile14.png>)

- 10. 从局部变量3中装载int类型值


- 11: iload_3

这个前⾯也说了

- 1. 返回int类型值


- 12: ireturn


![image 15](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile15.png>)

这个就不⽤多说了，就是将操作数栈中的30返回

到这⾥就把我们compute()⽅法讲解完了，讲完有没有对局部变量表和操作数栈的理解有所加深 呢？说⽩了赋值号=后⾯的就是操作数，在这些操作数进⾏赋值，运算的时候需要内存存放，那 就是存放在操作数栈中，作为临时存放操作数的⼀⼩块内存区域。

接下来我们再说说⽅法出⼝。

⽅法出⼝说⽩了不就是⽅法执⾏完了之后要出到哪⾥，那么我们知道上⾯compute()⽅法执⾏完 之后应该回到main()⽅法第三⾏那么当main()⽅法调⽤compute()的时候，compute()栈帧中的⽅ 法出⼝就存储了当前要回到的位置，那么当compute()⽅法执⾏完之后，会根据⽅法出⼝中存储 的相关信息回到main()⽅法的相应位置。

那么main()⽅同样有⾃⼰的栈帧，在这⾥有些不同的地⽅我们讲⼀下。

我们上⾯已经知道局部变量会存放在栈帧中的局部变量表中，那么main()⽅法中的math会存⼊其 中，但是这⾥的math是⼀个对象，我们知道new出来的对象是存放在堆中的

![image 16](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile16.png>)

那么这个math变量和堆中的对象有什么联系呢？是同⼀个概念么？

当然不是的，局部变量表中的math存储的是堆中那个math对象在堆中的内存地址

- 2.程序计数器 程序计数器也是线程私有的区域，每个线程都会分配程序计数器的内存，是⽤来存放当前线程正 在运⾏或者即将要运⾏的jvm指令码对应的地址，或者说⾏号位置。

上述代码中每个指令码前⾯都有⼀个⾏号，你就可以把它看作当前线程执⾏到某⼀⾏代码位置的 ⼀个标识，这个值就是程序计数器的值。

那么jvm虚拟机为什么要设置程序计数器这个结构呢？就是为了多线程的出现，多线程之间的切 换，当⼀个程序被挂起的时候，总是要恢复的，那么恢复到哪个位置呢，总不能⼜重新开始执⾏ 吧，那么程序计数器就解决了这个问题。

- 3.⽅法区


在jdk1.8之前，有⼀个名称叫做持久带/永久代，很多同学应该听过，在jdk1.8之后，oracle官⽅改 名为元空间。存放常量、静态变量、类元信息。

public static int initData = 666;

这个initData就是静态变量，毋庸置疑是存放在⽅法区的

public static User user = new User();

那么这个user就有点不⼀样了，user变量放在⽅法区，new的User是存放在堆中的

到这⾥我们就能意识到栈，堆，⽅法区之间都是有联系的。

![image 17](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile17.png>)

栈中的局部变量，⽅法区中的静态变量，如果是对象类型的话都会指向堆中new出来中的对象， 那么红⾊的联系代表什么呢？我们先来了解⼀下对象。

## 对象组成

你对对象的了解有多少呢，天天⽤对象，你是否知道对象在虚拟机中的存储结构呢？

对象在内存中存储的布局可以分为3块区域：对象头（Header）、实例数据（Instance Data）和 对⻬填充（Pading）。下图是普通对象实例与数组对象实例的数据结构：

![image 18](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile18.png>)

对象头

HotSpot虚拟机的对象头包括两部分信息：

Mark Word

第⼀部分markword,⽤于存储对象⾃身的运⾏时数据，如哈希码（HashCode）、GC分代年 龄、锁状态标志、线程持有的锁、偏向线程ID、偏向时间戳等，这部分数据的⻓度在32位和64 位的虚拟机（未开启压缩指针）中分别为32bit和64bit，官⽅称它为“MarkWord”。

Klas Pointer

对象头的另外⼀部分是klas类型指针，即对象指向它的类元数据的指针，虚拟机通过这个 指针来确定这个对象是哪个类的实例.

数组⻓度（只有数组对象有） 如果对象是⼀个数组, 那在对象头中还必须有⼀块数据⽤于记录数组⻓度.

实例数据

实例数据部分是对象真正存储的有效信息，也是在程序代码中所定义的各种类型的字段内 容。⽆论是从⽗类继承下来的，还是在⼦类中定义的，都需要记录起来。

对⻬填充

第三部分对⻬填充并不是必然存在的，也没有特别的含义，它仅仅起着占位符的作⽤。由于 HotSpot VM的⾃动内存管理系统要求对象起始地址必须是8字节的整数倍，换句话说，就是对象 的⼤⼩必须是8字节的整数倍。⽽对象头部分正好是8字节的倍数（1倍或者2倍），因此，当对 象实例数据部分没有对⻬时，就需要通过对⻬填充来补全。

其中的klas类型指针就是那条红⾊的联系，那是怎么联系的呢？

new Thread().start();

![image 19](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile19.png>)

类加载其实最终是以类元信息的形式存储在⽅法区中的，math和math2都是由同⼀个类new出来 的，当对象被new时，都会在对象头中存储⼀个指向类元信息的指针，这就是Klas Pointer.

到这⾥我们就讲解了栈，程序计数器和⽅法区，下⾯我们简单介绍⼀下本地⽅法区，最后再终点 讲解堆。

- 4.本地⽅法栈 实际上现在本地⽅法栈已经⽤的⽐较少了，⼤家应该都有听过本地⽅法吧


如何经常⽤的线程类

new Thread().start();

public synchronized void start() { if (threadStatus != 0)

throw new IllegalThreadStateException(); group.add(this); boolean started = false; try {

start0(); started = true;

} finally { try { if (!started) { group.threadStartFailed(this);

} } catch (Throwable ignore) { }

} }

其中底层调⽤了⼀个start0()的⽅法

private native void start0();

这个⽅法没有实现，但⼜不是接⼝，是使⽤native修饰的，是属于本地⽅法，底层通过C语⾔实 现的，那java代码⾥为什么会有C语⾔实现的本地⽅法呢？

⼤家都知道JAVA是问世的，在那之前⼀个公司的系统百分之九⼗九都是使⽤C语⾔实现的，但是 java出现后，很多项⽬都要转为java开发，那么新系统和旧系统就免不了要有交互，那么就需要 本地⽅法来实现了，底层是调⽤C语⾔中的dl库⽂件，就类似于java中的jar包，当然，如今跨语 ⾔的交互⽅式就很多了，⽐如thrift，htp接⼝⽅式，webservice等，当时并没有这些⽅式，就只 能通过本地⽅法来实现了。

那么本地⽅法始终也是⽅法，每个线程在运⾏的时候，如果有运⾏到本地⽅法，那么必然也要产 ⽣局部变量等，那么就需要存储在本地⽅法栈了。如果没有本地⽅法，也就没有本地⽅法栈了。

- 5.堆 最后我们讲堆，堆是最重要的⼀块内存区域，我相信⼤部分⼈对堆都不陌⽣。但是对于它的内部 结构，运作细节想要搞清楚也没那么简单。


![image 20](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile20.png>)

对于这个基本组成⼤家应该都有所了解，对就是由年轻代和⽼年代组成，年轻代⼜分为伊甸园区 和survivor区，survivor区中⼜有from区和to区.

我们new出来的对象⼤家都知道是放在堆中，那具体放在堆中的哪个位置呢？

其实new出来的对象⼀般都放在Eden区，那么为什么叫伊甸园区呢，伊甸园就是亚当夏娃住的地 ⽅，不就是造⼈的地⽅么？所以我们new出来的对象就是放在这⾥的，那当Eden区满了之后呢？

假设我们给对分配60M内存，这个是可以通过参数调节的，我们后⽂再讲。那么⽼年代默认是 占2/3的，也就是差不多40M，那年轻代就是20M，Eden区160M，Survivor区40M。

## GC

![image 21](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile21.png>)

⼀个程序只要在运⾏，那么就不会不停的new对象，那么总有⼀刻Eden区会放满，那么⼀旦 Eden区被放满之后，虚拟机会⼲什么呢？没错，就是gc，不过这⾥的gc属于minor gc，就是垃 圾收集，来收集垃圾对象并清理的，那么什么是垃圾对象呢？

好⽐我们上⾯说的math对象，我们假设我们是⼀个web应⽤程序，main线程执⾏完之后程序不 会结束，但是main⽅法结束了，那么main()⽅法栈帧会被释放，局部变量会被释放，但是局部变 量对应的堆中的对象还是依然存在的，但是⼜没有指针指向它，那么它就是⼀个垃圾对象，那就 应该被回收掉了，之后如果还会new Math对象，也不会⽤这个之前的了，因为已经⽆法找到它 了，如果留着这个对象只会占⽤内存，显然是不合适的。

这⾥就涉及到了⼀个GC Rot根以及可达性分析算法的概念，也是⾯试偶尔会被问到的。

可达性分析算法是将GC Rots对象作为起点，从这些起点开始向下搜索引⽤的对象，找到的对 象都标记为⾮垃圾对象，其余未标记的都是垃圾对象。

那么GC Rots根对象⼜是什么呢，GC Rots根就是判断⼀个对象是否可以回收的依据，只要能 通过GC Rots根向下⼀直搜索能搜索到的对象，那么这个对象就不算垃圾对象，⽽可以作为GC Rots根的有线程栈的本地变量，静态变量，本地⽅法栈的变量等等，说⽩了就是找到和根节点 有联系的对象就是有⽤的对象，其余都认为是垃圾对象来回收。

![image 22](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile22.png>)

经历了第⼀次minor gc后，没有被清理的对象就会被移到From区，如上图。

![image 23](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile23.png>)

上⾯在说对象组成的时候有写到，在对象头的Mark Word中有存储GC分代年龄，⼀个对象每经 历⼀次gc，那么它的gc分代年龄就会+1，如上图。

![image 24](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile24.png>)

那么如果第⼆次新的对象⼜把Eden区放满了，那么⼜会执⾏minor gc，但是这次会连着From区 ⼀起gc，然后将Eden区和From区存活的对象都移到To区域，对象头中分代年龄都+1，如上图。

![image 25](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile25.png>)

那么当第三次Eden区⼜满的时候，minor gc就是回收Eden区和To区域了，TEden区和To区域还 活着的对象就会都移到From区，如上图。说⽩了就是Survivor区中总有⼀块区域是空着的，存活 的对象存放是在From区和To区轮流存放，也就是互相复制拷⻉，这也就是垃圾回收算法中的复 制-回收算法。

如果⼀个对象经历了⼀个限值15次gc的时候，就会移⾄⽼年代。那如果还没有到限值，From区 或者To区域也放不下了，就会直接挪到⽼年代，这只是举例了两种常规规则，还有其他规则也是 会把对象存放⾄⽼年代的。

那么随着应⽤程序的不断运⾏，⽼年代最终也是会满的，那么此时也会gc，此时的gc就是Ful gc 了。

## GC案例

下⾯我们通过⼀个简单的演示案例来更加清楚的了解GC。

public class HeapTest { byte[] a = new byte[1024*100]; public static void main(String[] args) throws InterruptedException {

ArrayList<HeapTest> heapTest = new ArrayList<>(); while(true) {

heapTest.add(new HeapTest()); Thread.sleep(10);

} }

}

这块代码很明显，就是⼀个死循环，不断的往list中添加new出来的对象。

我们这⾥使⽤jdk⾃带的⼀个jvm调优⼯具jvisualvm来观察⼀下这个代码执⾏的的内存结构。

运⾏代码打开之后我们可以看到这样的界⾯：

![image 26](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile26.png>)

我们在左边的应⽤程序中可以看到我们运⾏的这个代码，右边是它的⼀些jvm，内存信息，我们 这⾥不关注，我们需要⽤到的是最后⼀个Visual GC⾯板，这是⼀个插件，如果你的打开没有这 ⼀栏的话，可以再⼯具栏的插件中进⾏下载安装。

打开visual GC，我们先看⼀下界⾯⼤概的布局，

![image 27](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile27.png>)

其中⽼年代(Olc)，伊甸园区(Eden)，S0(From)，S1(To)⼏个区域的内存和动态分配图都是清晰 可⻅，以⼀对应的。

![image 28](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile28.png>)

我们选择中间⼀张图给⼤家对应⼀下上⾯所讲的内容：

- 1：对象放⼊Eden区
- 2：Eden区满发⽣minor gc
- 3：第⼆步的存活对象移⾄From(Survivor 0)区
- 4：Eden区再满发⽣minor gc
- 5：第四步存活的对象移⾄To(Survivor 1)区


![image 29](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile29.png>)

这⾥可以注意到From和To区域和我们上⾯所说移⾄，总有⼀个是空的。

![image 30](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile30.png>)

⼤家还可以注意到⽼年代这⾥，都是⼀段⼀段的直线，中间是突然的增加，这就是在minor gc中 ⼀批⼀批符合规则的对象被批量移⼊⽼年代。

那当我们⽼年代满了会发⽣什么呢？当然是我们上⾯说过的Ful GC，但是你仔细看我们写的这 个程序，我们所有new出来的HeapTest对象都是存放在heapLists中的，那就会被这个局部变量 所引⽤，那么Ful GC就不会有什么垃圾对象可以回收，可是内存⼜满了，那怎么办？

![image 31](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile31.png>)

没错，就是我们就算没⻅过也总听过的 OM。 到这⾥jvm内存模型简单介绍就结束了，看到这⾥还不点个赞嘛！

# 推荐阅读：

我画了20张图，终于让⼥朋友都学会了翻转链表

⽀付宝的架构到底有多⽜逼！还没看完我就跪了！

HashMap图解原理与数据结构

⽜X，试⽤了下GitHub上 2k Star的第⼀抢票神器，3秒钟抢到！

![image 32](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile32.png>)

喜欢我可以给我设为星标哦

![image 33](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile33.png>)

![image 34](<小白都能看得懂的java虚拟机内存模型.note_images/imageFile34.png>)

