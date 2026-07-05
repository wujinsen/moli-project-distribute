---
title: JDK的命令行工具.note（原文插图 annex）
slug: annex-JDK的命令行工具
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/jvm/JDK的命令行工具.note.md
related: [jvm-gc调优实战]
created: 2026-07-05
updated: 2026-07-05
---

![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

jps：虚拟机进程状况⼯具

jsp命令格式： jps[options][hostid]

![image 3](assets/imageFile3.png)

jps执⾏样例

![image 4](assets/imageFile4.png)

jstat：虚拟机统计信息监视⼯具 可以显示本地或者远程虚拟机进程中的类装载、内存、垃圾收集、JIT编译等运⾏数据

jstat命令格式为： jstat[option vmid[interval[s|ms][count ]

如果是本地虚拟机进程，VMID与LVMID是⼀致的，如果是远程虚拟机进程，那VMID的格式应当是： [protocol：][/]lvmid[@hostname[：port]/servername]

参数interval和count代表查询间隔和次数，如果省略这两个参数，说明只查询⼀次。 假设需要每250毫秒查询⼀次进程2764垃圾收集状况，⼀共查询20次，

那命令应当是：jstat-gc 2764 250 20

![image 5](assets/imageFile5.png)

jstat执⾏样例：jstat-gcutil 2764

![image 6](assets/imageFile6.png)

jinfo：Java配置信息⼯具

实时地查看和调整虚拟机各项参数

jinfo命令格式：jinfo[option]pid

执⾏样例：查询CMSInitiatingOcupancyFraction参数值。 jinfo-flag CMSInitiatingOcupancyFraction 1 4

-X：CMSInitiatingOcupancyFraction=85

jinfo（Configuration Info for Java）的作⽤是实时地查看和调整虚拟机各项参数。使⽤jps命令的-v参 数 可以查看虚拟机启动时显式指定的参数列表，但如果想知道未被显式指定的参数的系统默认值，除了 去找资料外， 就只能使⽤jinfo的-flag选项进⾏查询了（如果只限于JDK 1.6或以上版本的话，使⽤java-X：

+PrintFlagsFinal 查看参数默认值也是⼀个很好的选择），jinfo还可以使⽤-sysprops选项把虚拟机进程的

System.getProperties（）的内容打印出来。这个命令在JDK 1.5时期已经随着Linux版的JDK发布，当 时只提供了 信息查询的功能，JDK 1.6之后，jinfo在Windows和Linux平台都有提供，并且加⼊了运⾏期修改参数的 能⼒，可以 使⽤-flag[+|-]name或者-flag name=value修改⼀部分运⾏期可写的虚拟机参数值。JDK 1.6中，jinfo对 于 Windows平台功能仍然有较⼤限制，只提供了最基本的-flag选项。

jmap：Java内存映像⼯具 ⽤于⽣成堆转储快照（⼀般称为heapdump或dump⽂件） jmap命令格式： jmap[option]vmid

![image 7](assets/imageFile7.png)

使⽤jmap⽣成⼀个正在运⾏的Eclipse的dump快照⽂件的例⼦，例⼦中的350是通过jps命令查询到的 LVMID。

jmap-dump：format=b,file=eclipse.bin 350

Dumping heap to C：\Users\IcyFenix\eclipse.bin… Heap dump file created

jhat：虚拟机堆转储快照分析⼯具 Sun JDK提供jhat（JVM Heap Analysis Tol）命令与jmap搭配使⽤，来分析jmap⽣成的堆转储快照

使⽤jhat分析4.2.4节中采⽤jmap⽣成的Eclipse IDE的内存快照⽂件。 代码清单4-3 使⽤jhat分析dump⽂件

C：\Users\IcyFenix＞jhat eclipse.bin Reading from eclipse.bin… Dump file created Fri Nov 19 2：07：21 CST 2010 Snapshot read,resolving… Resolving 125951 objects… Chasing references,expect 245 dots… Eliminating duplicate references… Snapshot resolved. Started HTP server on port 7 0 Server is ready. 屏幕显示“Server is ready.”的提示后，⽤户在浏览器中键⼊ ：7 0/就可以看到分析结 果，如图4-3所示

htp:/localhost

![image 8](assets/imageFile8.png)

分析结果默认是以包为单位进⾏分组显示，分析内存泄漏问题主要会使⽤到其中的“Heap Histogram” （与 jmap-histo功能⼀样）与OQL⻚签的功能，前者可以找到内存中总容量最⼤的对象，后者是标准的对 象查询语⾔， 使⽤类似SQL的语法对内存中的对象进⾏查询统计

jstack：Java堆栈跟踪⼯具 jstack（Stack Trace for Java）命令⽤于⽣成虚拟机当前时刻的线程快照（⼀般称为threadump或者 javacore⽂件） 线程快照就是当前虚拟机内每⼀条线程正在执⾏的⽅法堆栈的集合，⽣成线程快照的主要⽬的是 定位线程出现⻓时间停顿的原因，如线程间死锁、死循环、请求外部资源导致的⻓时间等待等都是导 致线程⻓时间 停顿的常⻅原因。线程出现停顿的时候通过jstack来查看各个线程的调⽤堆栈，就可以知道没有响应的 线程到底在 后台做些什么事情，或者等待着什么资源。

jstack命令格式： jstack[option]vmid

![image 9](assets/imageFile9.png)

HSDIS：JIT⽣成代码反汇编

在Java虚拟机规范中，详细描述了虚拟机指令集中每条指令的执⾏过程、执⾏前后对操作数栈、局部 变量表的 影响等细节。这些细节描述与Sun的早期虚拟机（Sun Clasic VM）⾼度吻合，但随着技术的发展，⾼ 性能虚拟机 真正的细节实现⽅式已经渐渐与虚拟机规范所描述的内容产⽣了越来越⼤的差距，虚拟机规范中的描 述逐渐成了虚 拟机实现的“概念模型”⸺即实现只能保证规范描述等效。基于这个原因，我们分析程序的执⾏语义问 题（虚拟 机做了什么）时，在字节码层⾯上分析完全可⾏，但分析程序的执⾏⾏为问题（虚拟机是怎样做的、 性能如何） 时，在字节码层⾯上分析就没有什么意义了，需要通过其他⽅式解决。 分析程序如何执⾏，通过软件调试⼯具（GDB、Windbg等）来断点调试是最常⻅的⼿段，但是这样的 调试⽅式 在Java虚拟机中会遇到很⼤困难，因为⼤量执⾏代码是通过JIT编译器动态⽣成到CodeBufer中的，没 有很简单的 ⼿段来处理这种混合模式的调试（不过相信虚拟机开发团队内部肯定是有内部⼯具的）。因此，不得 不通过⼀些特 别的⼿段来解决问题，基于这种背景，本节的主⻆⸺HSDIS插件就正式登场了。

HSDIS是⼀个Sun官⽅推荐的HotSpot虚拟机JIT编译代码的反汇编插件，它包含在HotSpot虚拟机的源 码之中， 但没有提供编译后的程序。在Project Kenai的⽹站[1]也可以下载到单独的源码。它的作⽤是让HotSpot 的-X：

+PrintAsembly指令调⽤它来把动态⽣成的本地代码还原为汇编代码输出，同时还⽣成了⼤量⾮常有 价值的注释， 这样我们就可以通过输出的代码来分析问题。读者可以根据⾃⼰的操作系统和CPU类型从 Project Kenai的⽹站上下 载编译好的插件，直接放到JDK_HOME/jre/bin/client和JDK_HOME/jre/bin/server⽬录中即可。如果没 有找到所需 操作系统（譬如Windows的就没有）的成品，那就得⾃⼰使⽤源码编译⼀下[2]。 还需要注意的是，如果读者使⽤的是Debug或者FastDebug版的HotSpot，那可以直接通过-X：

+PrintAsembly 指令使⽤插件；如果使⽤的是Product版的HotSpot，那还要额外加⼊⼀个-X：

+UnlockDiagnosticVMOptions参 数。笔者以代码清单4-6中的简单测试代码为例演示⼀下这个插件的使⽤。

- 代码清单4-6 测试代码 public clas Bar{ int a=1； static int b=2； public int sum（int c）{ return a+b+c； } public static void main（String[]args）{ new Bar（）.sum（3）； } } 编译这段代码，并使⽤以下命令执⾏。 java-X：+PrintAsembly-Xcomp-X：CompileComand=dontinline，*Bar.sum-X：Compi leComand=compileonly，*Bar.sum test.Bar 其中，参数-Xcomp是让虚拟机以编译模式执⾏代码，这样代码可以“偷懒”，不需要执⾏⾜够次数来预 热就能 触发JIT编译[3]。两个-X：CompileComand意思是让编译器不要内联sum（）并且只编译sum （），-X：

+PrintAsembly就是输出反汇编内容。如果⼀切顺利的话，那么屏幕上会出现类似下⾯代码清单4-7所 示的内容。

- 代码清单4-7 测试代码


[Disasembling for mach='i386'] [Entry Point] [Constants] #{method}'sum'（I）I'in'test/Bar' #this：ecx='test/Bar' #parm0：edx=int #[sp+0x20]（sp of caler）

… 0x01cac407：cmp 0x4（%ecx），%eax 0x01cac40a：jne 0x01c6b050；{runtime_cal} [Verified Entry Point] 0x01cac410：mov%eax，-0x8 0（%esp）

- 0x01cac417：push%ebp
- 0x01cac418：sub$0x18，%esp；*aload_0 ；-test.Bar： （line 8） ；block B0[0，10] 0x01cac41b：mov 0x8（%ecx），%eax；*getfield a ；-test.Bar： （line 8） 0x01cac41e：mov$0x3d2fad8，%esi；{op（a 'java/lang/Clas'='test/Bar'）} 0x01cac423：mov 0x68（%esi），%esi；*getstatic b ；-test.Bar： （line 8） 0x01cac426：ad%esi，%eax 0x01cac428：ad%edx，%eax 0x01cac42a：ad$0x18，%esp


- sum@0
- sum@1


sum@4

- 0x01cac42d：pop%ebp
- 0x01cac42e：test%eax，0x2b010；{pol_return} 0x01cac434：ret 上段代码并不多，下⾯⼀句句进⾏说明。


- 1）mov%eax，-0x8 0（%esp）：检查栈溢。
- 2）push%ebp：保存上⼀栈帧基址。
- 3）sub$0x18，%esp：给新帧分配空间。
- 4）mov 0x8（%ecx），%eax：取实例变量a，这⾥0x8（%ecx）就是ecx+0x8的意思，前⾯ “[Constants]”节中 提示了“this：ecx='test/Bar'”，即ecx寄存器中放的就是this对象的地址。偏移0x8是越过this对象的对 象头， 之后就是实例变量a的内存位置。这次是访问“Java堆”中的数据。


- 5）mov$0x3d2fad8，%esi：取test.Bar在⽅法区的指针。
- 6）mov 0x68（%esi），%esi：取类变量b，这次是访问“⽅法区”中的数据。
- 7）ad%esi，%eax和ad%edx，%eax：做两次加法，求a+b+c的值，前⾯的代码把a放在eax中，把 b放在esi中， ⽽c在[Constants]中提示了，“parm0：edx=int”，说明c在edx中。
- 8）ad$0x18，%esp：撤销栈帧。
- 9）pop%ebp：恢复上⼀栈帧。
- 10）test%eax，0x2b010：轮询⽅法返回处的SafePoint。 1）ret：⽅法返回
