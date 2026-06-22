⼀.JVM空间说明

- JDK 1.7及以前，Java 类信息、常量池、静态变量都存储在 Perm（永久代）⾥。类的元数据和静态变 量在类加载的时候分配到 Perm，当类被卸载的时候垃圾收集器从 Perm 处理掉。
- JDK 1.8 的对 JVM 架构的改造将类元数据放到本地内存中，另外，将常量池和静态变量放到 Java 堆 ⾥。HotSopt VM 将会为类的元数据明确分配和释放本地内存。在这种架构下，类元信息就突破了原来


- -X MaxPermSize 的限制,所以PermSize的配置也是⽆效的，现在可以使⽤更多的本地内存。这样就 从⼀定程度上解决了原来在运⾏时⽣成⼤量类的造成经常 Ful GC 问题，如运⾏时使⽤反射、代理等

jvm内存 ⼲货：可以发现最明显的⼀个变化是元空间从虚拟机转移到本地内存；默认情况下，元空间的⼤⼩仅 受本地内存的限制。这意味着以后不会因为永久代空间不够⽽抛出 OM异常了。 jdk1.8以前版本的clas和jar包数据存储在permGen下⾯ ，permGen⼤⼩是固定的，⽽且项⽬之间⽆法 共⽤公有的clas，所以很容易碰到 OM异常。 改成metaSpaces后，各个项⽬会共享同样的clas内存空间，⽐如多个项⽬都引⽤了apache-comon 包，在metaSpaces中只会存储⼀份apache-comon的clas，提⾼了内存的利⽤率，垃圾回收更有效 率。 ⼆.JVM参数配置

在jdk1.8以前，⽣产环境⼀般有如下配置

- -X PermSize=512M -X MaxPermSize=1024M 表示在JVM⾥存储Java类信息，常量池和静态变量的永久代区域初始⼤⼩为512M，最⼤为1024M。在 项⽬启动后，这个值是固定的，如果项⽬clas过多，很可能遇到OutOfMemoryEror: PermGen异常。

升级JDK1.8之后，上⾯的perm配置已经变成

- -X MetaspaceSize=512M X MaxMetaspaceSize=1024M MetaspaceSize如果不做配置，通过jinfo查看默认MetaspaceSize⼤⼩（约21M）,MaxMetaspaceSize 很⼤很⼤，前⾯说过MetaSpace只受本地内存⼤⼩限制。


jinfo -flag MetaspaceSize 1234 #结果为：-X MetaspaceSize=21807104 jinfo -flag MaxMetaspaceSize 1234 #结果为：-X MaxMetaspaceSize=184674073709547520

⼲货：MetaspaceSize为出发FulGC的阈值，默认约为21M，如做了配置，最⼩阈值为⾃定义配置⼤ ⼩。空间使⽤达到阈值，触发FulGC，同时对该值扩⼤。当然如果元空间实际使⽤⼩于阈值，在GC的 时候也会对该值缩⼩。 MaxMetaspaceSize为元空间的最⼤值，如果设置太⼩，可能会导致频繁FulGC，甚⾄ OM。 三.GC（GarbageColection）过程

⾸先贴⼀张⽹上盗来的⼤图，⽤它来说明下GC的过程再合适不过。

image.png 新new的对象都放在Eden区（伊甸园嘛，创造的地⽅） Eden区满或者快满的时候进⾏⼀次清理（Minor Gc），不被引⽤的对象直接被⼲掉；还有引⽤的对 象，但是年龄⽐较⼤的，挪到S0区 下次Eden区快满的时候，会进⾏上⼀步的操作，并且将Eden和S0区的年纪⼤的对象放到S1区【原理上 随时保持S0和S1有⼀个是空的，⽤来存下⼀次的对象】 下下次，Eden区快满的时候，会进⾏上⼀步操作，并且将Eden和S1区的年纪⼤的对象放到S0区【此时 S1区就是空的】 直到Eden区快满，S0或者S1也快满的时候，这时候就把这两个区的年纪⼤的对象放到Old区 依次循环，直到Old区也快满的时候，Eden区也快满的时候，会对整个这⼀块内存区域进⾏⼀次⼤清洗 （FulGC），腾出内存，为之后的对象创建，程序运⾏腾地⽅。 清理Eden区和Survivor区叫Minor GC；清理Old区叫Major GC；清理整个堆空间—包括年轻代和⽼年 代叫Ful GC。 四. JVM参数配置指南

前⾯三个部分对JVM进⾏了整体的了解，接下来是本⽂的重点。

- -X MetaspaceSize=128M -X MaxMetaspaceSize=256M -Xms256m -Xmx256m ⽂章看下来上⾯这段配置的意思很简单，设置元空间的初始值和最⼤值，设置堆空间的初始值和最⼤ 值。


为什么MetaspaceSize要设置为128M？为什么堆内存初始值Xms设置为256M⽽不是512M？ 按照Java官⽅的指导

image.png

Java堆⼤⼩设置，Xms 和 Xmx设置为⽼年代存活对象的3-4倍，即FulGC之后的⽼年代内存占⽤的34倍 永久代 PermSize和MaxPermSize(元空间)设置为⽼年代存活对象的1.2-1.5倍。 年轻代Xmn的设置为⽼年代存活对象的1-1.5倍。 ⽼年代的内存⼤⼩设置为⽼年代存活对象的2-3倍。 可以让系统运⾏⼀段时间后查看系统的各个指标，然后在进⾏配置。如下⽤jstat⼯具查看jvm的情况

jstat -gc 12345 #

S0C S1C S0U S1U EC EU OC OU MC MU CSC CSU YGC YGCT FGC FGCT GCT 13824.0 2528.0 137.0 0.054864.0 535257.2 13152.046189.373984.0 7 19.8 9728.0 9196.2 14 0.2593 0.287 0.546

OU表示⽼年代所占⽤的内存为 46189.3 K（⼤约45M）；那么jvm相应的配置参数应该做如下修改

-X MetaspaceSize=64M -X MaxMetaspaceSize=64M -Xms180m -Xmx180m

作者：蓝⼭牧童 链接：htps:/ w.jianshu.com/p/30e8f0f7d9 来源：简书 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

