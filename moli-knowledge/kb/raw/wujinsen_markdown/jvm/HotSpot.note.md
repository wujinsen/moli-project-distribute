提起HotSpot VM，相信所有Java程序员都知道，它是Sun JDK和OpenJDK中所带的虚拟机， 也是⽬前使⽤范围最

⼴的Java虚拟机。但不⼀定所有⼈都知道的是，这个⽬前看起来“⾎统纯正”的虚拟机在最初并⾮由 Sun公司开 发，⽽是由⼀家名为“Longview Technologies”的⼩公司设计的；甚⾄这个虚拟机最初并⾮是为 Java语⾔⽽开发 的，它来源于Strongtalk VM，⽽这款虚拟机中相当多的技术⼜是来源于⼀款⽀持Self语⾔实现“达 到C语⾔50%以 上的执⾏效率”的⽬标⽽设计的虚拟机，Sun公司注意到了这款虚拟机在JIT编译上有许多优秀的理 念和实际效果， 在197年收购了Longview Technologies公司，从⽽获得了HotSpot VM。

HotSpot VM既继承了Sun之前两款商⽤虚拟机的优点（如前⾯提到的准确式内存管理），也 有许多⾃⼰新的技

术优势，如它名称中的HotSpot指的就是它的热点代码探测技术（其实两个VM基本上是同时期的 独⽴产品，HotSpot 还稍早⼀些，HotSpot⼀开始就是准确式GC，⽽Exact VM之中也有与HotSpot⼏乎⼀样的热点探 测。为了Exact VM和 HotSpot VM哪个成为Sun主要⽀持的VM产品，在Sun公司内部还有过争论，HotSpot打败Exact并 不能算技术上的胜 利），HotSpot VM的热点代码探测能⼒可以通过执⾏计数器找出最具有编译价值的代码，然后通 知JIT编译器以⽅ 法为单位进⾏编译。如果⼀个⽅法被频繁调⽤，或⽅法中有效循环次数很多，将会分别触发标准编 译和OSR（栈上 替换）编译动作。通过编译器与解释器恰当地协同⼯作，可以在最优化的程序响应时间与最佳执⾏ 性能中取得平 衡，⽽且⽆须等待本地代码输出才能执⾏程序，即时编译的时间压⼒也相对减⼩，这样有助于引⼊ 更多的代码优化 技术，输出质量更⾼的本地代码。

在206年的JavaOne⼤会上，Sun公司宣布最终会把Java开源，并在随后的⼀年，陆续将JDK 的各个部分（其中

当然也包括了HotSpot VM）在GPL协议下公开了源码，并在此基础上建⽴了OpenJDK。这样， HotSpot VM便成为了 Sun JDK和OpenJDK两个实现极度接近的JDK项⽬的共同虚拟机。

在208年和209年，Oracle公司分别收购了BEA公司和Sun公司，这样Oracle就同时拥有了 两款优秀的Java虚拟

机：JRockit VM和HotSpot VM。Oracle公司宣布在不久的将来（⼤约应在发布JDK 8的时候）会 完成这两款虚拟机 的整合⼯作，使之优势互补。整合的⽅式⼤致上是在HotSpot的基础上，移植JRockit的优秀特 性，譬如使⽤ JRockit的垃圾回收器与MisionControl服务，使⽤HotSpot的JIT编译器与混合的运⾏时系统。

