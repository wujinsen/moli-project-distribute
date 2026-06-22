5个⼯具帮助写出更好的Java代码 IDR解决⽅案

之前我写过⼀篇关于 的⽂章。合适的⼯具可以改进我们的代码，并且 提⾼开发效率。在 中，我们⼀直在找⼀些⽅法来改进我们的代码。但最近，我们在考虑改 进 和 代码时，发现最近我们的关注点已经开始转换到如何提升Java性 能。 在这篇⽂章中，我会带着⼤家⼀起看⼀下9个可以帮助我们优化Java性能的⼯具。有⼀些我们已经在 IDR Solutions中使⽤了，⽽另外⼀些有可能在个⼈项⽬中使⽤。

PDF HTML5转换器 Java PDF类库

# NetBeans Profiler

![image 1](<九大工具助你玩转Java性能优化.note_images/imageFile1.png>)

是⼀个NetBeans IDE插件，主要为NetBeans IDE提供性能分析相关的功。NetBeans IDE是⼀个开源的集成开发环境。它很好地⽀持所有Java应⽤类型（包括Java SE、JavaFX、Java ME、Web、EJB和移动应⽤）的开发。 这个性能分析器包含了诸如CPU、内存和线程性能分析功能，并且提供了⼀些⽤于基本JVM监控的附 加⼯具和功能。对于需要解决内存和性能相关问题的开发者⾮常有⽤。

NetBeans profiler

# JProfiler

![image 2](<九大工具助你玩转Java性能优化.note_images/imageFile2.png>)

在 中有提及。同样，它也是⼀个很好的Java性能分析⼯具。JProfiler集CPU、 内存和线程性能分析于⼀体，可以⽤于分析性能瓶颈、内存泄漏、CPU负载和解决线程相关的问题， 并且⽀持本地性能分析（分析与JProfiler软件安装在同⼀台机的应⽤）和远程性能分析（它可以分析远 程没有安装JProfiler机器上的应⽤），这⼀点对开发⼈员⾮常有⽤。 JProfiler由ej-technologies GmbH开发的商业授权的Java性能分析⼯具，主要为Java E和Java SE应 ⽤所设计。

JProfiler 我之前的⽂章

# GC Viewer

![image 3](<九大工具助你玩转Java性能优化.note_images/imageFile3.png>)

GC viewer截 图

可以（从 和 ）免费获取。GC Viewer是⼀个开源⼯具，可以对vmflags verboase:gc和-Xlogc:等Java VM选项产⽣的数据进⾏可视化分析。GC Viewer可以⽤于计算GC（垃 圾回收）相关的性能数据记录，包括吞吐、累积暂停、最⻓时间的暂停等等。当你想要通过改变⽣成 ⼤⼩和设置初始堆⼤⼩来调整某个特定应⽤的GC时，它尤其有⽤。 GC Viewer是⼀个开源⼯具，由Tagtraum Industries Incorporated开发。这是⼀个很⼩的初创软件咨询 公司，位于罗利、北卡罗莱纳州，由成⽴于1 9年的⾮盈利项⽬Tagtraum Industries在204年创⽴。

GC Viewer 主⻚ Github

# VisualVM

![image 4](<九大工具助你玩转Java性能优化.note_images/imageFile4.png>)

是⼀个由NetBeans平台派⽣的⼯具，遵循模块化的架构思想。这意味着可以通过插件⽅便 的进⾏扩展。 Visual VM允许你获取Java程序的详细信息，只要它在⼀个Java虚拟机（JVM）上运⾏即可。⽣成的数 据可以由JDK⼯具⽣成和读取，多个Java程序的所有数据和信息都可以很⽅便地进⾏查看，包括本地 和远程的运⾏程序。同时可以保存JVM软件的数据快照，把数据保存在本地，⽅便以后进⾏查看或者 和其他⼈⼀起分享。 Visual VM可以进⾏CPU性能分析、内存性能分析，进⾏GC（译者注：可以进⾏强制调⽤GC)、保存快 照等。

VisualVM

# Paty *Beta

![image 5](<九大工具助你玩转Java性能优化.note_images/imageFile5.png>)

Paty in action (图 ⽚ 来 源 于 htp:/paty.sourceforge.net.)

是⼀个开源项⽬，可以从 下载。它致⼒于为Java 1.5.0和更⾼版本的虚拟机 提供性能分析⼯具。与其他性能分析⼯具不同之处在于，它专注性能分析，并且允许⽤户在运⾏时开 启和关闭性能分析功能。 ⽬前Paty正处于beta版阶段，但随着更多强⼤的功能如⽅法执⾏、代码覆盖、线程竞争（Thread Contention)分析的加⼊，Paty可以⽤于（内存、CPU等）性能调优，并且可以将信息通过TCP/IP Socket发送到其他电脑上。Paty有着易⽤的GUI，可以⽤于分析堆。还可以在Java编译流程中的准备 阶段，对应⽤程序运⾏时对clas进⾏监测（instrument）和取消监测（de-instrument）。

“Paty” 项⽬ Source Forge

JRockit⸺任务管理

![image 6](<九大工具助你玩转Java性能优化.note_images/imageFile6.png>)

是⼀个原由Apeal Virtual Machines开发的专有Java虚拟机。202年由BEA Systems收购， 之后从Sum Microsystems处转到Oracle。 Oracle的JRockit是⼀套完整的Java SE解决⽅案，包含了⼀个⾼性能JVM、性能分析、监控和排错⼯ 具。可⽤于预测Java程序中的延迟。 现在，JRockit打包有⼀系列的称为JRockit任务控制（JRockit Mision Control）的⼯具。这些⼯具包 括：⼀个⽤于管理的控制台（console，译者注：此console跟终端不⼀样，是特指管理后台系统）， 进⾏GC数据可视化和其他的⼀些性能统计。它同样可以作为运⾏时性能分析⼯具Runtime Analyzer来 使⽤，也可以分析内存问题。

JRockit

# Eclipse Memory Analyzer

![image 7](<九大工具助你玩转Java性能优化.note_images/imageFile7.png>)

Memory Analyzer (MAT) 可 以 在 Eclipse IDE中 找 到 .

Eclipse内存分析器(Eclipse Memory Analyzer)

是⼀个可以帮助你找到内存泄漏和减少内存损耗的Java 堆分析器。它更适合作为⼀个分析Java堆栈和计算⼤⼩的⼯具集，也可以⽤于监测内存泄漏和反模式 带来的内存损耗。

# Java Interactive Profiler

![image 8](<九大工具助你玩转Java性能优化.note_images/imageFile8.png>)

JIP BSD许可协议 Source Forge

是⼀个⽤Java开发的⾼性能、低损耗性能分析器。基于 发布，可以从 下 载。使⽤JIP的开发者可以在VM运⾏时开启和关闭性能分析，并且可以过滤类和包、控制输出。

Profiler4J

![image 9](<九大工具助你玩转Java性能优化.note_images/imageFile9.png>)

是⼀个专注于CPU性能分析的⼯具。它具有友好的⽤户界⾯，⽀持远程性能分析，并且⽀持 动态修改配置。Profiler4j值得关注的特性有：基于动态字节码增强⽅式，这就表明它不需要任何本地 的库和外部可执⾏⽂件⽀持。更重要的是，它完全由Java编写，可以提供图形化的调⽤图形信息、调 ⽤树结构、内存监控和类列表，⽀持细粒度配置⽂件。它基于 发布，可以从 Source Forge上 希望你觉得这些⼯具有⽤。 你使⽤什么⼯具来改进你的代码，请告诉我们吧！ 这篇⽂章是我们” “系列的⼀部分。在这些⽂章中，我们致⼒于深⼊Java和JavaFx。看⼀ 下吧！ 如果你是第⼀次看这个系列，或者想要在我们发布新⽂章和更新时，得到通知，你可以通过社交媒体 来关注( , 和 )或者 原⽂链接： 翻译： 译⽂链接： [转载请保留原⽂出处、译者和译⽂链接。]

Profiler4j

Apache License v2.0协议 下载

Java⽂章索引

TwiterFacebok Gogle+ Blog RS idrsolutions ImportNew.com陈 晓舜 htp:/ w.importnew.com/12324.html

# Profiler4J

Source Forge上的profiler4j是206年的，并且是beta阶段，不建议使⽤。

