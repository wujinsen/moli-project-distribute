⾔归正传，今天有个同事找我，其实好像之前就找过我，⼀直因为太忙，后⾯就忘记他的事了，到今 天还没查出原因就⼜找了过来，现象是系统⽼是进⾏Ful GC，在启动没过多久就会发⽣Ful GC，这个 现象相对⽐较少⻅的，于是找他要了GC⽇志，赫然看到如下⽇志:

![image 1](<假笨说-警惕大量类加载器的创建导致诡异的Full GC.note_images/imageFile1.png>)

这个很显然就是达到了Metaspace的阈值触发的Ful GC了，但是看看Metaspace的size，使⽤了134M 左右，于是我询问他MetaspaceSize和MaxMetaspaceSize分别设置了多少，告知我设置的是256M，那就 有⼏个⽐较奇怪的地⽅了:

为什么启动没多久就因为Metaspace触发了Ful GC 从使⽤率来看并没有达到阈值 在Ful GC之后⽴⻢就能正常运⾏⼀段时间，说明Metaspace确实回收了

# 先说个JVM的BUG

从上⾯的GC⽇志，我们看到了Ful GC前后，Metaspace的使⽤变化是从137752K->71671K，其实你们 如果⽤的oracle官⽅的JDK，看到的会是137752K->137752K，也就是并没有发⽣变化，看起来好像 Metaspace并没有被回收，其实这是JVM的⼀个BUG，我们alijdk将这个问题进⾏了修复，能看到前后 是有变化的，所以如果⼤家在排查Metaspace的问题时候，希望不要被这个信息骗到

# 再聊点GC⽇志

从JDK8开始，任何GC，都会默认打印GC Cause，所以你看到上⾯的Ful GC是因为Metadata GC Threshold触发的，也就是Metaspace comited的内存加上这次要分配的内存达到了MetaspaceSize 的阈值。如果是JDK7(之前版本不⽀持)，那可以通过加JVM参数-XX:+PrintGCCause来打印原因，可以 通过下⾯的图⽚⼩程序链接点进去看看这个参数的具体⽤法及含义:

![image 2](<假笨说-警惕大量类加载器的创建导致诡异的Full GC.note_images/imageFile2.png>)

![image 3](<假笨说-警惕大量类加载器的创建导致诡异的Full GC.note_images/imageFile3.png>)

再提⼀点，Metaspce触发的GC都是Ful GC。 另外⼤家常看到的类似下⾯的Allocation Failure的GC Cause，其实是正常的，因为⼤部分GC，尤其 是YGC，都是因为分配内存失败才触发的，所以不要认为看到Failure就觉得有问题。

![image 4](<假笨说-警惕大量类加载器的创建导致诡异的Full GC.note_images/imageFile4.png>)

# 为何使⽤率这么低就触发了Ful GC

Metaspace触发Ful GC，是因为Metaspace comited的内存加上这次要分配的内存之和超过了阈值 才会触发，但是我们看使⽤了才134M，⽽阈值却是256M，那可能怀疑下⾯两种情况：

这次分配的内存达到12M以上？ 碎⽚化问题？

对于第⼀种情况，基本不太可能，因为⼀个类不可能要这么⼤内存，所以暂时先排除这种可能。

对于第⼆种情况，有⼀个场景是能满⾜的，类加载器创建⾮常多，但是每个类加载器加载的类⼜特别 少，同时Ful GC之后⼜能很快被回收掉 为了验证第⼆种情况，我尝试加两个参数-XX:+HeapDumpBeforeFullGC和-XX:+HeapDumpAfterFullGC， 在Ful GC前后分别对内存做⼀个dump，这两个参数，可以通过下⾯的图⽚⼩程序链接看到具体的使⽤ 情况

![image 5](<假笨说-警惕大量类加载器的创建导致诡异的Full GC.note_images/imageFile5.png>)

从两个dump的分析结果来看，查了下类加载器的情况，果然在Ful GC之前看到了31650个类加载器， ⽽Ful GC之后，类加载器个数变成了872个，于是开始找究竟是哪些类加载器，最终发现某个特定类 型的类加载器对象⾮常之多，咨询了业务⽅确实存在这种情况，因为没有做好缓存，所以导致了⽆⽌ 境创建

# 类加载器过多为什么会导致Ful GC

类加载器创建过多，带来的⼀个问题是，在类加载器第⼀次加载类的时候，会在Metaspace⾥会给它 分配内存块，为了分配⾼效，每个类加载器⽤来存放类信息的内存块都是独⽴的，所以哪怕你这个类 加载器只加载⼀个类，也会为之分配⼀块空的内存给这个类加载器，其实是⾄少两个内存块，于是你 有可能会发现Metaspace的内存使⽤率⾮常低，但是comited的内存已经达到了阈值，从⽽触发了 Ful GC，如果这种只加载很少类的类加载器⾮常多，那造成的后果就是很多碎⽚化的内存

# JVMPocket介绍

JVMPocket是我最近捣⿎的⼀个微信⼩程序，⼤家可以通过搜索JVMPocket或者从我公众号菜单⾥进 ⼊，该⼩程序主要缘因JVM参数⽽诞⽣，有⼈问我相关的问题，告诉他们什么参数可以解决，但是苦 于参数太⻓⽽⽆法记住，特尴尬，有了JVMPocket之后，直接找到对应的参数发个链接过去就可以看 到对应参数的具体含义，⽤法，默认值以及⼤家的使⽤建议等，希望该⼩程序也能帮到⼤家，⼤家如 果⾃⼰的JVM参数经验，都可以到对应的参数下⾯留⾔让更多⼈知道它背后的故事。 附上⼩程序进⼊的链接：

![image 6](<假笨说-警惕大量类加载器的创建导致诡异的Full GC.note_images/imageFile6.png>)

JVMPocketJVM参数锦囊

![image 7](<假笨说-警惕大量类加载器的创建导致诡异的Full GC.note_images/imageFile7.png>)

⼩程序

本⼈其他JVM相关⽂章

假笨说-查JVM参数就找JVMPocket(JVM⼝袋)⼩程序吧 假笨说-关于数组动态扩容导致频繁GC的问题，我还有话说 假笨说-⼜抓了⼀个导致频繁GC的⻤ -数组动态扩容 假笨说-类初始化死锁导致线程被打爆！打爆！爆！ 假笨说-谨防JDK8重复类定义造成的内存泄漏 假笨说-从⼀起GC⾎案谈到反射原理 假笨说-我是如何⾛上JVM这条贼船的 假笨说-从X86指令深扒JVM的位移操作 JVM源码分析之⼀个Java进程究竟能创建多少线程 JVM源码分析之String.intern()导致的YGC不断变⻓ JVM源码分析之不保证顺序的Clas.getMethods JVM源码分析之Metaspace解密 JVM源码分析之临⻔⼀脚的OutOfMemoryEror完全解读 JVM源码分析之不可控的堆外内存 JVM源码分析之jstat⼯具原理完全解读 JVM源码分析之JDK8下的僵⼫(⽆法回收)类加载器 JVM源码分析之栈溢出完全解读 JVM源码分析之Atach机制实现完全解读 JVM源码分析之⾃定义类加载器如何拉⻓YGC JVM源码分析之FinalReference完全解读

JVM源码分析之javagent原理完全解读 JVM源码分析之Object.wait/notify(Al)完全解读 JVM源码分析之堆外内存完全解读 JVM源码分析之SystemGC完全解读 JDK8在泛型类型推导上的变化 YGC前后新⽣代变⼤？ 消失的死锁 进程物理内存远⼤于Xmx的问题分析 不可逆的类初始化过程 JDK的sql设计不合理导致的驱动类初始化死锁问题 如何定位消耗CPU最多的线程 Java的时间为何从1970年1⽉1⽇开始

