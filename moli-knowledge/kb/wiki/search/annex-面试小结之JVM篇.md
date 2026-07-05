---
title: 面试小结之JVM篇.note（原文插图 annex）
slug: annex-面试小结之JVM篇
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/面试笔试/ElasticSearch/面试小结之JVM篇.note.md
related: [elasticsearch-面试题]
created: 2026-07-05
updated: 2026-07-05
---

最近⾯试⼀些公司，被问到的关于Java虚拟机的问题，以及⾃⼰总结的回答。

Java内存区域是如何划分的？

Java堆：线程共享的，唯⼀⽬的就是⽤于存放对象实例，是垃圾收集器管理的主要区域； Java虚拟机栈：线程私有的，每个⽅法在执⾏的同时都会创建⼀个栈帧⽤于存储局部变量等，局部 变量表存放了编译器可知的各种基本数据类型和对象引⽤； 本地⽅法栈：和虚拟机栈类似，不过它是为Native⽅法服务； 程序计数器：线程私有的，可以看作是当前线程所执⾏的字节码的⾏号指示器，以便线程切换后恢 复执⾏使⽤； ⽅法区：线程共享的，⽤于存储已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的 代码等数据；该区域的内存回收主要是针对常量池的回收和类型的卸载（特别是要注意⼀些动态字 节码框架和⾃定义ClasLoader的场景下）；在HotSpot⾥经常被称为永久代，

在Java 8⾥已被废除 了，被元空间取代

；

![image 1](assets/imageFile1.png)

Java内存区域

对象是否可⽤以及引⽤类型。

由于引⽤计数法⽆法解决循环引⽤的问题，所以⼀般都是使⽤可达性分析来判断的，即通过⼀系列 称为“GC Rots”的对象（⽐如虚拟机栈引⽤的对象、⽅法区中的类静态属性和常量引⽤对象）作为 起点，从这些节点⼀直往下搜索，⾛过的路径称为引⽤链；⽽那些没有与引⽤链相连的对象即为不 可达，会被回收； 可以通过覆盖finalize⽅法来实现对象的“⾃救”，避免在标记后被回收，但通常不建议这么做； 对象的引⽤类型可分为：强引⽤、软引⽤（在内存溢出前会将这种类型的对象进⾏第⼆次回收）、 弱引⽤（弱引⽤对象只能⽣存到下次垃圾回收之前）、虚引⽤（不会对⽣存时间存在影响，也⽆法 通过它获取对象，主要⽬的就是在回收时收到⼀个系统通知）；

# 有哪些常⻅的垃圾收集算法？

标记-清除算法：⾸先标记出所有需要回收的对象，然后统⼀回收所有被标记的对象；缺点是效率不 ⾼且容易产⽣⼤量不连续的内存碎⽚； 复制算法：将可⽤内存分为⼤⼩相等的两块，每次只使⽤其中⼀块；当这⼀块⽤完了，就将还活着 的对象复制到另⼀块上，然后把已使⽤过的内存清理掉。在HotSpot⾥，考虑到⼤部分对象存活时 间很短，将内存分为Eden和两块Survivor，默认⽐例为8:1 1。代价是存在部分内存空间浪费，且可 能存在空间不够需要分配担保的情况，所以适合在新⽣代使⽤； 标记-整理算法：⾸先标记出所有需要回收的对象，然后让所有存活的对象都向⼀端移动，然后直接 清理掉端边界以外的内存。适⽤于⽼年代。 分代收集算法：⼀般把Java堆分新⽣代和⽼年代，在新⽣代⽤复制算法，在⽼年代⽤标记-清理或标 记-整理算法，是现代虚拟机通常采⽤的算法。

PS： 堆 的 划分 及 回 收 过 程 详解

- 1. Eden区最⼤，对外提供堆内存。当Eden区快要满了，则进⾏Minor GC，把存活对象放⼊Survivor A区，清空Eden区；

- 2. Eden区被清空后，继续对外提供堆内存；

- 3. 当Eden区再次被填满，此时对Eden区和Survivor A区同时进⾏Minor GC，把存活对象放⼊Survivor B区，同时清空Eden 区和Survivor A区；

- 4. Eden区继续对外提供堆内存，并重复上述过程，即在Eden区填满后，把Eden区和某个Survivor区的存活对象放到另⼀个 Survivor区；

- 5. 当某个Survivor区被填满，且仍有对象未被复制完毕时或者某些对象在反复Survive 15次左右时，则把这部分剩余对象放到 Old区；

- 6. 当Old区也被填满时，进⾏Major GC，对Old区进⾏垃圾回收。


# 有哪些常⻅的垃圾收集器？

这⾥讨论JDK 1.7 Update 14之后的HotSpot虚拟机，包含的虚拟机如下图所示（存在连线的表示可以 搭配使⽤）：

![image 2](assets/imageFile2.png)

HotSpot垃圾收集器

Serial收集器

![image 3](assets/imageFile3.png)

Serial收集器

最基本、发展历史最悠久，在JDK 1.3之前是新⽣代收集的唯⼀选择； 是⼀个单线程（只会使⽤⼀个收集线程，且必须暂停所有⼯作线程）的收集器，采⽤的是复制算 法； 现在依然是虚拟机运⾏在Client模式下的默认新⽣代收集器，主要就是因为它简单⽽⾼效（没有线 程交互的开销）；

ParNew收集器

![image 4](assets/imageFile4.png)

ParNew收集器

其实就是Serial收集器的多线程版本，采⽤的也是复制算法； ParNew收集器在单CPU环境中绝对不会有⽐Serial收集器更好的效果； 是许多运⾏在Server模式下虚拟机⾸选的新⽣代收集器，重要原因就是除了Serial收集器外，只有它 能与CMS收集器配合⼯作；

PS： 关 于 垃圾 收 集 器 的 并 ⾏ 和 并 发

并⾏（Paralel）：指多条垃圾收集线程并⾏⼯作，但此时⽤户线程仍处于等待状态； 并发（Concurent）：指⽤户线程与垃圾收集线程同时执⾏，⽤户线程在继续执⾏⽽垃圾收集程序 运⾏在另外⼀个CPU上；

CMS收集器

![image 5](assets/imageFile5.png)

CMS收集器

是⼀种以获取最短回收停顿时间为⽬标的收集器，特别适合互联⽹站或者B/S的服务端； 它是基于标记-清除 算法实现的，主要包括4个步骤：初始标记（STW，只是初始标记⼀下GC Rots能直接关联到的对象，速度很快）、并发标记（⾮STW，执⾏GC RotsTracing，耗时⽐较 ⻓）、重新标记（STW，修正并发标记期间因⽤户程序继续导致变动的那⼀部分对象标记）和并发 清除（⾮STW，耗时较⻓）； 还有3个明显的缺点：CMS收集器对CPU⾮常敏感（占⽤部分线程及CPU资源，影响总吞吐量）、 ⽆法处理浮动垃圾（默认达到92%就触发垃圾回收）、⼤量内存碎⽚产⽣（可以通过参数启动压 缩）；

# 介绍⼀下G1收集器的原理和实现。

![image 6](assets/imageFile6.png)

G1收集器

⼀款⾯向服务端应⽤的垃圾收集器，后续会替换掉CMS垃圾收集器； 特点：

并⾏与并发（充分利⽤多核多CPU缩短STW时间） 分代收集（独⽴管理整个Java堆，但针对不同年龄的对象采取不同的策略） 空间整合（局部看是基于复制算法，从整体来看是基于标记-整理算法，都不会产⽣内存碎⽚） 可预测的停顿（可以明确指定在⼀个⻓度为M毫秒的时间⽚内垃圾收集不会超过N毫秒）

将堆分为⼤⼩相等的独⽴区域，避免全区域的垃圾收集；新⽣代和⽼年代不再物理隔离，只是部分 Region的集合；

G1跟踪各个Region垃圾堆积的价值⼤⼩，在后台维护⼀个优先列表，根据允许的收集时间优先回收 价值最⼤的Region； Region之间的对象引⽤以及其他收集器中的新⽣代与⽼年代之间的对象引⽤，采⽤Remembered Set来避免全堆扫描； 分为⼏个步骤，和CMS的过程⽐较类似：

初始标记（标记⼀下GC Rots能直接关联的对象并修改TAMS值，需要STW但耗时很短） 并发标记（从GC Rot从堆中对象进⾏可达性分析找存活的对象，耗时较⻓但可以与⽤户线程并发执⾏） 最终标记（为了修正并发标记期间产⽣变动的那⼀部分标记记录，这⼀期间的变化记录在Remembered Set Log⾥，然后合并到Remembered Set⾥，该阶段需要STW但是可并⾏执⾏） 筛选回收（对各个Region回收价值排序，根据⽤户期望的GC停顿时间制定回收计划来回收）；

# 你们的服务配置的虚拟机参数是怎么样的？

我们的服务的虚拟机参数：

- -server -启⽤能够执⾏优化的编译器，显著提⾼服务器的性能

- -Xmx4 0M -堆最⼤值

- -Xms4 0M -堆初始⼤⼩

- -Xmn60M -年轻代⼤⼩

- -X PermSize=20M -持久代初始⼤⼩

- -X MaxPermSize=20M -持久代最⼤值

- -Xs256K -每个线程的栈⼤⼩

- -X:+DisableExplicitGC -关闭System.gc()

- -X SurvivorRatio=1 -年轻代中Eden区与两个Survivor区的⽐值

- -X:+UseConcMarkSwepGC -使⽤CMS内存收集

- -X:+UseParNewGC -设置年轻代为并⾏收集

- -X:+CMSParalelRemarkEnabled -降低标记停顿

- -X:+UseCMSCompactAtFulColection -在FUL GC的时候，对年⽼代进⾏压缩，可能会影响性能，但是可以消除碎⽚

- -X CMSFulGCsBeforeCompaction=0 -此值设置运⾏多少次GC以后对内存空间进⾏压缩、整理

- -X:+CMSClasUnloadingEnabled - 回 收 动 态 ⽣ 成 的 代 理 类 SE ： htp:/stackoverflow.com/questions/ 3491/what-does-jvm-flag-cmsclasunloadingenabled-actualy-do

- -X LargePageSizeInBytes=128M -内存⻚的⼤⼩不可设置过⼤， 会影响Perm的⼤⼩

- -X:+UseFastAcesorMethods -原始类型的快速优化

- -X:+UseCMSInitiatingOcupancyOnly -使⽤⼿动定义初始化定义开始CMS收集，禁⽌hostspot⾃⾏触发CMS GC

- -X CMSInitiatingOcupancyFraction=80 -使⽤cms作为垃圾回收，使⽤80％后开始CMS收集

- -X SoftRefLRUPolicyMSPerMB=0 -每兆堆空闲空间中SoftReference的存活时间

- -X:+PrintGCDetails -输出GC⽇志详情信息

- -X:+PrintGCAplicationStopedTime -输出垃圾回收期间程序暂停的时间

- -Xlogc:$WEB_AP_HOME/.tomcat/logs/gc.log -把相关⽇志信息记录到⽂件以便分析.

- -X:+HeapDumpOnOutOfMemoryEror -发⽣内存溢出时⽣成heapdump⽂件

- -X HeapDumpPath=$WEB_AP_HOME/.tomcat/logs/heapdump.hprof -heapdump⽂件地址


如何进⾏性能调优以及常⽤的JDK的命令⾏⼯具有哪些？

JVM调优：CPU使⽤率与Load值偏⼤（Thread count以及GC count）、关键接⼝响应时间很慢 （GC time以及GC log中的STW的时间）、发⽣Ful GC或者Old CMS GC⾮常频繁（内存泄露）； JVM停顿（尽量避免Ful GC、关闭偏向锁、输出GC⽇志到内存⽂件系统、关闭JVM输出的jstat⽇ 志）；

将Java性能优化分为4个层级：应⽤层、数据库层、框架层、JVM层。每层优化难度逐级增加，涉 及的知识和解决的问题也会不同。⽐如应⽤层需要理解代码逻辑，通过Java线程栈定位有问题代码 ⾏等；数据库层⾯需要分析SQL、定位死锁等；框架层需要懂源代码，理解框架机制；JVM 层需要 对GC的类型和⼯作机制有深⼊了解，对各种 JVM 参数作⽤了然于胸； 围绕Java性能优化，有两种最基本的分析⽅法：现场分析法和事后分析法。现场分析法通过保留现 场，再采⽤诊断⼯具分析定位。现场分析对线上影响较⼤，部分场景不太合适。事后分析法需要尽 可能多收集现场数据，然后⽴即恢复服务，同时针对收集的现场数据进⾏事后分析和复现。 OS 的诊断主要关注的是 CPU、Memory、I/O 三个⽅⾯。top、vmstat、 fre –m、iostat；常⽤的 Java应⽤诊断包括线程、堆栈、GC 等⽅⾯的诊断，可以使⽤jstack 、jstat、jmap；

![image 7](assets/imageFile7.png)

JDK的命令⾏⼯具

类的加载器是什么？

虚拟机设计团队把类加载阶段的“通过⼀个类的全限定名来获取描述此类的⼆进制字节流”这个动作 放到虚拟机外部去实现，实现这个动作的代码模块称为类加载器；这种设计给Java语⾔带来了⾮常 强⼤的灵活性； 双亲委派模型要求除了顶层的启动类加载器外，其他的类加载器都应当有⾃⼰的⽗类加载器，如果 ⼀个类加载器收到了类加载的请求，它⾸先不会⾃⼰去尝试加载这个类，⽽是把这个请求委派给⽗ 类加载器去完成，只有⽗类加载器反馈⾃⼰⽆法完成这个加载请求时，⼦加载器才会尝试⾃⼰去加 载；这对于保证程序的稳定运作很重要；

![image 8](assets/imageFile8.png)

类加载器

OSGI实现模块化热部署的关键是它⾃定义的类加载机制的实现，每个Bundle（通过ImportPackage和Export-Package导⼊和导出依赖）都有⾃⼰的类加载器，类加载器之间形成了更加复杂 的⽹状结构；

谈谈你对Java内存模型的理解。

虚拟机规范视图通过J M来屏蔽掉各种硬件和操作系统的内存访问差异，主要⽬标是定义程序中各 个变量的访问限制，即在虚拟机将变量存储到内存和从内存中取出变量这样的底层细节； 主内存与⼯作内存：Java内存模型规定了所有的变量都存储在主内存中，每个线程还有⾃⼰的⼯作 内存，线程的⼯作内存中保存了被该线程使⽤到的变量的主内存副本拷⻉，线程对变量的所有操作 都必须在⼯作内存中进⾏，⽽不能直接读写主内存中的变量；

![image 9](assets/imageFile9.png)

主内存与⼯作内存

可⻅性（主内存和⼯作内存）、原⼦性（volatile的long是具备原⼦性的）、有序性（hapenbefore规则）；

Java语⾔中有⼀个“先⾏发⽣”（hapen—before）的规则，它是Java内存模型中定义的两项操作之 间的偏序关系，如果操作A先⾏发⽣于操作B，其意思就是说，在发⽣操作B之前，操作A产⽣的影 响都能被操作B观察到，“影响”包括修改了内存中共享变量的值、发送了消息、调⽤了⽅法等，它与 时间上的先后发⽣基本没有太⼤关系。下⾯是Java内存模型中的⼋条可保证hapen—before的规 则，它们⽆需任何同步器协助就已经存在，可以在编码中直接使⽤。如果两个操作之间的关系不在 此列，并且⽆法从下列规则推导出来的话，它们就没有顺序性保障，虚拟机可以对它们进⾏随机地 重排序。

- 1、程序次序规则：在⼀个单独的线程中，按照程序代码的执⾏流顺序，（时间上）先执⾏的操作hapen—before（时间上） 后执⾏的操作。

- 2、管理锁定规则：⼀个unlock操作hapen—before后⾯（时间上的先后顺序，下同）对同⼀个锁的lock操作。

- 3、volatile变量规则：对⼀个volatile变量的写操作hapen—before后⾯对该变量的读操作。

- 4、线程启动规则：Thread对象的start（）⽅法hapen—before此线程的每⼀个动作。

- 5、线程终⽌规则：线程的所有操作都hapen—before对此线程的终⽌检测，可以通过Thread.join（）⽅法结束、 Thread.isAlive（）的返回值等⼿段检测到线程已经终⽌执⾏。

- 6、线程中断规则：对线程interupt（）⽅法的调⽤hapen—before发⽣于被中断线程的代码检测到中断时事件的发⽣。

- 7、对象终结规则：⼀个对象的初始化完成（构造函数执⾏结束）hapen—before它的finalize（）⽅法的开始。

- 8、传递性：如果操作A hapen—before操作B，操作B hapen—before操作C，那么可以得出A hapen—before操作C。


⽐如双重检查实现单例模式可能存在并发问题，可以使⽤内部静态类实现。

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br><br><br>12</th>
    <th>public clas Singleton {<br><br>private Singleton() {}<br><br>/ Lazy initialization holder clas idiom for static fields private static clas InstanceHolder { private static final Singleton instance = new Singleton(); }<br><br>public static Singleton getSingleton() { return InstanceHolder.instance; }</th>
  </tr>
</table>


13 }

是否了解偏向锁？

JVM锁有4种状态：⽆锁、偏向锁（通过MarkWord的线程ID）、轻量级锁（通过MarkWord的锁记 录指针）、重量级锁；

![image 10](assets/imageFile10.png)

锁的优缺点对⽐
