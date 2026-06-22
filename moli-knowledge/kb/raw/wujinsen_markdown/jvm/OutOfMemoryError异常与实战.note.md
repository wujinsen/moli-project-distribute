StackOverflowEror：线程请求的栈深度⼤于虚拟机所允许的深度 OutOfMemoryEror：虚拟机栈可以动态扩展（当前⼤部分的Java虚拟机都可动态扩展，只不过Java虚 拟机规范中也允许固定⻓度的虚拟机

栈），如果扩展时⽆法申请到⾜够的内存 OutOfMemoryEror Java heap space：java堆内存溢出 OutOfMemoryEror PermGen space：运⾏时常量池溢出，也就是⽅法区永久代溢出

Exception in thread"main"java.lang.OutOfMemoryEror：由DirectMemory导致的内存溢出，物理内 存溢出 at sun.misc.Unsafe.alocateMemory（Native Method） at org.fenixsoft.om.DMOM.main（DMOM.java：20） 从整个软件开发的范围来看，各种语⾔和框架使⽤句柄来访问的情况 也⼗分常⻅。

在Java虚拟机规范的描述中，除了程序计数器外，虚拟机内存的其他⼏个运⾏时区域都有发⽣ OutOfMemoryEror（下⽂称 OM）异常的可能，本节将通过若⼲实例来验证异常发⽣的场景（代码 清单2-3～代码清 单2-9的⼏段简单代码），并且会初步介绍⼏个与内存相关的最基本的虚拟机参数。 本节内容的⽬的有两个：第⼀，通过代码验证Java虚拟机规范中描述的各个运⾏时区域存储的内容； 第⼆，希 望读者在⼯作中遇到实际的内存溢出异常时，能根据异常的信息快速判断是哪个区域的内存溢出，知 道什么样的代 码可能会导致这些区域内存溢出，以及出现这些异常后该如何处理。 下⽂代码的开头都注释了执⾏时所需要设置的虚拟机启动参数（注释中“VM Args”后⾯跟着的参数）， 这些 参数对实验的结果有直接影响，读者调试代码的时候千万不要忽略。如果读者使⽤控制台命令来执⾏ 程序，那直接 跟在Java命令之后书写就可以。如果读者使⽤Eclipse IDE，则可以参考图2-4在Debug/Run⻚签中的设 置。

- 图 2-4 在Eclipse的Debug⻚签中设置虚拟机参数 下⽂的代码都是基于Sun公司的HotSpot虚拟机运⾏的，对于不同公司的不同版本的虚拟机，参数和程 序运⾏的 结果可能会有所差别。


- 2.4.1 Java堆溢出 Java堆⽤于存储对象实例，只要不断地创建对象，并且保证GC Rots到对象之间有可达路径来避免垃 圾回收机 制清除这些对象，那么在对象数量到达最⼤堆的容量限制后就会产⽣内存溢出异常。 代码清单2-3中代码限制Java堆的⼤⼩为20MB，不可扩展（将堆的最⼩值-Xms参数与最⼤值-Xmx参 数设置为⼀ 样即可避免堆⾃动扩展），通过参数-X：+HeapDumpOnOutOfMemoryEror可以让虚拟机在出现内 存溢出异常时Dump 出当前的内存堆转储快照以便事后进⾏分析[1]。


- 代码清单2-3 Java堆内存溢出异常测试 /*


- *VM Args：-Xms20m-Xmx20m-X：+HeapDumpOnOutOfMemoryEror
- *@authorzm
- */ public clas HeapOM{ static clas OMObject{ } public static void main（String[]args）{ List＜ OMObject＞list=new ArayList＜ OMObject＞（）； while（true）{ list.ad（new OMObject（））； } } } 运⾏结果： java.lang.OutOfMemoryEror：Java heap space Dumping heap to java_pid3404.hprof … Heap dump file created[2045981 bytes in 0. 63 secs] Java堆内存的 OM异常是实际应⽤中常⻅的内存溢出异常情况。当出现Java堆内存溢出时，异常堆栈 信 息“java.lang.OutOfMemoryEror”会跟着进⼀步提示“Java heap space”。 要解决这个区域的异常，⼀般的⼿段是先通过内存映像分析⼯具（如Eclipse Memory Analyzer）对 Dump出来 的堆转储快照进⾏分析，重点是确认内存中的对象是否是必要的，也就是要先分清楚到底是出现了内 存泄漏 （Memory Leak）还是内存溢出（Memory Overflow）。图2-5显示了使⽤Eclipse Memory Analyzer 打开的堆转储快


照⽂件。

- 图 2-5 使⽤Eclipse Memory Analyzer打开的堆转储快照⽂件 如果是内存泄露，可进⼀步通过⼯具查看泄露对象到GC Rots的引⽤链。于是就能找到泄露对象是通 过怎样的 路径与GC Rots相关联并导致垃圾收集器⽆法⾃动回收它们的。掌握了泄露对象的类型信息及 GC Rots引⽤链的信 息，就可以⽐较准确地定位出泄露代码的位置。 如果不存在泄露，换句话说，就是内存中的对象确实都还必须存活着，那就应当检查虚拟机的堆参数 （-Xmx 与-Xms），与机器物理内存对⽐看是否还可以调⼤，从代码上检查是否存在某些对象⽣命周期过⻓、 持有状态时间 过⻓的情况，尝试减少程序运⾏期的内存消耗。 以上是处理Java堆内存问题的简单思路，处理这些问题所需要的知识、⼯具与经验是后⾯3章的主题。 [1]关于堆转储快照⽂件分析⽅⾯的内容，可参⻅第4章。


- 2.4.2 虚拟机栈和本地⽅法栈溢出 由于在HotSpot虚拟机中并不区分虚拟机栈和本地⽅法栈，因此，对于HotSpot来说，虽然-Xos参数 （设置本 地⽅法栈⼤⼩）存在，但实际上是⽆效的，栈容量只由-Xs参数设定。关于虚拟机栈和本地⽅法栈， 在Java虚拟机 规范中描述了两种异常： 如果线程请求的栈深度⼤于虚拟机所允许的最⼤深度，将抛出StackOverflowEror异常。 如果虚拟机在扩展栈时⽆法申请到⾜够的内存空间，则抛出OutOfMemoryEror异常。 这⾥把异常分成两种情况，看似更加严谨，但却存在着⼀些互相重叠的地⽅：当栈空间⽆法继续分配 时，到底 是内存太⼩，还是已使⽤的栈空间太⼤，其本质上只是对同⼀件事情的两种描述⽽已。 在笔者的实验中，将实验范围限制于单线程中的操作，尝试了下⾯两种⽅法均⽆法让虚拟机产⽣ OutOfMemoryEror异常，尝试的结果都是获得StackOverflowEror异常，测试代码如代码清单2-4所 示。 使⽤-Xs参数减少栈内存容量。结果：抛出StackOverflowEror异常，异常出现时输出的堆栈深度相 应缩⼩。 定义了⼤量的本地变量，增⼤此⽅法帧中本地变量表的⻓度。结果：抛出StackOverflowEror异常时输 出的堆 栈深度相应缩⼩。


- 代码清单2-4 虚拟机栈和本地⽅法栈 OM测试（仅作为第1点测试程序） /*


- *VM Args：-Xs128k
- *@authorzm


- */ public clas JavaVMStackSOF{ private int stackLength=1； public void stackLeak（）{ stackLength+； stackLeak（）； } public static void main（String[]args）throws Throwable{ JavaVMStackSOF om=new JavaVMStackSOF（）； try{


om.stackLeak（）； }catch（Throwable e）{ System.out.println（"stack length："+om.stackLength）； throw e； } } } 运⾏结果： stack length：2402 Exception in thread"main"java.lang.StackOverflowEror

- at org.fenixsoft.om.VMStackSOF.leak（VMStackSOF.java：20）
- at org.fenixsoft.om.VMStackSOF.leak（VMStackSOF.java：21） at org.fenixsoft.om.VMStackSOF.leak（VMStackSOF.java：21）


…后续异常堆栈信息省略 实验结果表明：在单个线程下，⽆论是由于栈帧太⼤还是虚拟机栈容量太⼩，当内存⽆法分配的时 候，虚拟机 抛出的都是StackOverflowEror异常。 如果测试时不限于单线程，通过不断地建⽴线程的⽅式倒是可以产⽣内存溢出异常，如代码清单2-5所 示。但 是这样产⽣的内存溢出异常与栈空间是否⾜够⼤并不存在任何联系，或者准确地说，在这种情况下， 为每个线程的 栈分配的内存越⼤，反⽽越容易产⽣内存溢出异常。 其实原因不难理解，操作系统分配给每个进程的内存是有限制的，譬如32位的Windows限制为2GB。 虚拟机提供 了参数来控制Java堆和⽅法区的这两部分内存的最⼤值。剩余的内存为2GB（操作系统限制）减去 Xmx（最⼤堆容

量），再减去MaxPermSize（最⼤⽅法区容量），程序计数器消耗内存很⼩，可以忽略掉。如果虚拟 机进程本身耗 费的内存不计算在内，剩下的内存就由虚拟机栈和本地⽅法栈“⽠分”了。每个线程分配到的栈容量越 ⼤，可以建 ⽴的线程数量⾃然就越少，建⽴线程时就越容易把剩下的内存耗尽。 这⼀点读者需要在开发多线程的应⽤时特别注意，出现StackOverflowEror异常时有错误堆栈可以阅 读，相对 来说，⽐较容易找到问题的所在。⽽且，如果使⽤虚拟机默认参数，栈深度在⼤多数情况下（因为每 个⽅法压⼊栈 的帧⼤⼩并不是⼀样的，所以只能说在⼤多数情况下）达到1 0～2 0完全没有问题，对于正常的⽅ 法调⽤（包括 递归），这个深度应该完全够⽤了。但是，如果是建⽴过多线程导致的内存溢出，在不能减少线程数 或者更换64位 虚拟机的情况下，就只能通过减少最⼤堆和减少栈容量来换取更多的线程。如果没有这⽅⾯的处理经 验，这种通 过“减少内存”的⼿段来解决内存溢出的⽅式会⽐较难以想到。

- 代码清单2-5 创建线程导致内存溢出异常 /*


- *VM Args：-Xs2M（这时候不妨设置⼤些）
- *@authorzm
- */ public clas JavaVMStackOM{ private void dontStop（）{ while（true）{ } } public void stackLeakByThread（）{ while（true）{ Thread thread=new Thread（new Runable（）{ @Overide public void run（）{ dontStop（）； } }）； thread.start（）； } }


public static void main（String[]args）throws Throwable{ JavaVMStackOM om=new JavaVMStackOM（）；

om.stackLeakByThread（）；

} } 注意 特别提示⼀下，如果读者要尝试运⾏上⾯这段代码，记得要先保存当前的⼯作。由于在 Windows平台的 虚拟机中，Java的线程是映射到操作系统的内核线程上的[1]，因此上述代码执⾏时有较⼤的⻛险，可 能会导致操 作系统假死。 运⾏结果： Exception in thread"main"java.lang.OutOfMemoryEror：unable to create new native thread [1]关于虚拟机线程实现⽅⾯的内容可以参考本书第12章。

- 2.4.3 ⽅法区和运⾏时常量池溢出 由于运⾏时常量池是⽅法区的⼀部分，因此这两个区域的溢出测试就放在⼀起进⾏。前⾯提到JDK 1.7 开始逐 步“去永久代”的事情，在此就以测试代码观察⼀下这件事对程序的实际影响。 String.intern（）是⼀个Native⽅法，它的作⽤是：如果字符串常量池中已经包含⼀个等于此String对 象的 字符串，则返回代表池中这个字符串的String对象；否则，将此String对象包含的字符串添加到常量池 中，并且返 回此String对象的引⽤。在JDK 1.6及之前的版本中，由于常量池分配在永久代内，我们可以通过-X： PermSize 和-X：MaxPermSize限制⽅法区⼤⼩，从⽽间接限制其中常量池的容量，如代码清单2-6所示。


- 代码清单2-6 运⾏时常量池导致的内存溢出异常 /*


- *VM Args：-X：PermSize=10M-X：MaxPermSize=10M
- *@authorzm
- */ public clas RuntimeConstantPol OM{ public static void main（String[]args）{


/使⽤List保持着常量池引⽤，避免Ful GC回收常量池⾏为 List＜String＞list=new ArayList＜String＞（）；

/10MB的PermSize在integer范围内⾜够产⽣ OM了 int i=0； while（true）{ list.ad（String.valueOf（i +）.intern（））；

} 运⾏结果： Exception in thread"main"java.lang.OutOfMemoryEror：PermGen space at java.lang.String.intern（Native Method） at org.fenixsoft.om.RuntimeConstantPol OM.main（RuntimeConstantPol OM.java：18） 从运⾏结果中可以看到，运⾏时常量池溢出，在OutOfMemoryEror后⾯跟随的提示信息是“PermGen space”，说明运⾏时常量池属于⽅法区（HotSpot虚拟机中的永久代）的⼀部分。 ⽽使⽤JDK 1.7运⾏这段程序就不会得到相同的结果，while循环将⼀直进⾏下去。关于这个字符串常量 池的实 现问题，还可以引申出⼀个更有意思的影响，如代码清单2-7所示。

- 代码清单2-7 String.intern（）返回引⽤的测试 public clas RuntimeConstantPol OM{ public static void main（String[]args）{ public static void main（String[]args）{


- String str1=new StringBuilder（"计算机"）.apend（"软件"）.toString（）；

- System.out.println（str1.intern（） =str1）；

String str2=new StringBuilder（"ja"）.apend（"va"）.toString（）；

- System.out.println（str2.intern（） =str2）； } } } 这段代码在JDK 1.6中运⾏，会得到两个false，⽽在JDK 1.7中运⾏，会得到⼀个true和⼀个false。产⽣ 差异 的原因是：在JDK 1.6中，intern（）⽅法会把⾸次遇到的字符串实例复制到永久代中，返回的也是永 久代中这个 字符串实例的引⽤，⽽由StringBuilder创建的字符串实例在Java堆上，所以必然不是同⼀个引⽤，将 返回false。 ⽽JDK 1.7（以及部分其他虚拟机，例如JRockit）的intern（）实现不会再复制实例，只是在常量池中 记录⾸次出 现的实例引⽤，因此intern（）返回的引⽤和由StringBuilder创建的那个字符串实例是同⼀个。对str2 ⽐较返回 false是因为“java”这个字符串在执⾏StringBuilder.toString（）之前已经出现过，字符串常量池中已经 有它 的引⽤了，不符合“⾸次出现”的原则，⽽“计算机软件”这个字符串则是⾸次出现的，因此返回true。




⽅法区⽤于存放Clas的相关信息，如类名、访问修饰符、常量池、字段描述、⽅法描述等。对于这些 区域的 测试，基本的思路是运⾏时产⽣⼤量的类去填满⽅法区，直到溢出。虽然直接使⽤Java SE API也可以 动态产⽣类 （如反射时的GeneratedConstructorAcesor和动态代理等），但在本次实验中操作起来⽐较麻烦。 在代码清单28中，笔者借助CGLib[1]直接操作字节码运⾏时⽣成了⼤量的动态类。 值得特别注意的是，我们在这个例⼦中模拟的场景并⾮纯粹是⼀个实验，这样的应⽤经常会出现在实 际应⽤ 中：当前的很多主流框架，如Spring、Hibernate，在对类进⾏增强时，都会使⽤到CGLib这类字节码 技术，增强的 类越多，就需要越⼤的⽅法区来保证动态⽣成的Clas可以加载⼊内存。另外，JVM上的动态语⾔（例 如Grovy等） 通常都会持续创建类来实现语⾔的动态性，随着这类语⾔的流⾏，也越来越容易遇到与代码清单2-8相 似的溢出场 景。

- 代码清单2-8 借助CGLib使⽅法区出现内存溢出异常 /*


- *VM Args：-X：PermSize=10M-X：MaxPermSize=10M
- *@authorzm
- */ public clas JavaMethodAreaOM{ public static void main（String[]args）{ while（true）{ Enhancer enhancer=new Enhancer（）； enhancer.setSuperclas（ OMObject.clas）； enhancer.setUseCache（false）； enhancer.setCalback（new MethodInterceptor（）{ public Object intercept（Object obj,Method method,Object[]args,MethodProxy proxy） throws Throwable{ return proxy.invokeSuper（obj,args）； } }）； enhancer.create（）； } } static clas OMObject{


运⾏结果： Caused by：java.lang.OutOfMemoryEror：PermGen space at java.lang.ClasLoader.defineClas1（Native Method） at java.lang.ClasLoader.defineClasCond（ClasLoader.java：632） at java.lang.ClasLoader.defineClas（ClasLoader.java：616）

…8 more ⽅法区溢出也是⼀种常⻅的内存溢出异常，⼀个类要被垃圾收集器回收掉，判定条件是⽐较苛刻的。 在经常动 态⽣成⼤量Clas的应⽤中，需要特别注意类的回收状况。这类场景除了上⾯提到的程序使⽤了CGLib 字节码增强和 动态语⾔之外，常⻅的还有：⼤量JSP或动态产⽣JSP⽂件的应⽤（JSP第⼀次运⾏时需要编译为Java 类）、基于 OSGi的应⽤（即使是同⼀个类⽂件，被不同的加载器加载也会视为不同的类）等。 [1]CGLib开源项⽬： 。

htp:/cglib.sourceforge.net/

- 2.4.4 本机直接内存溢出 DirectMemory容量可通过-X：MaxDirectMemorySize指定，如果不指定，则默认与Java堆最⼤值 （-Xmx指定） ⼀样，代码清单2-9越过了DirectByteBufer类，直接通过反射获取Unsafe实例进⾏内存分配（Unsafe 类的 getUnsafe（）⽅法限制了只有引导类加载器才会返回实例，也就是设计者希望只有rt.jar中的类才能使 ⽤Unsafe 的功能）。因为，虽然使⽤DirectByteBufer分配内存也会抛出内存溢出异常，但它抛出异常时并没有 真正向操作 系统申请分配内存，⽽是通过计算得知内存⽆法分配，于是⼿动抛出异常，真正申请分配内存的⽅法 是 unsafe.alocateMemory（）。


- 代码清单2-9 使⽤unsafe分配本机内存 /*


- *VM Args：-Xmx20M-X：MaxDirectMemorySize=10M
- *@authorzm
- */ public clas DirectMemoryOM{ private static final int_1MB=1024*1024； public static void main（String[]args）throws Exception{ Field unsafeField=Unsafe.clas.getDeclaredFields（）[0]；


unsafeField.setAcesible（true）； Unsafe unsafe=（Unsafe）unsafeField.get（nul）； while（true）{ unsafe.alocateMemory（_1MB）； } } } 运⾏结果： Exception in thread"main"java.lang.OutOfMemoryEror at sun.misc.Unsafe.alocateMemory（Native Method） at org.fenixsoft.om.DMOM.main（DMOM.java：20） 由DirectMemory导致的内存溢出，⼀个明显的特征是在Heap Dump⽂件中不会看⻅明显的异常，如果 读者发现

OM之后Dump⽂件很⼩，⽽程序中⼜直接或间接使⽤了NIO，那就可以考虑检查⼀下是不是这⽅⾯的 原因。

