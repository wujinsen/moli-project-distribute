概念：

新⽣代 GC（Minor GC）：指发⽣在新⽣代的垃圾收集动作，因为 Java 对象⼤多都具

备朝⽣夕灭的特性，所以 Minor GC ⾮常频繁，⼀般回收速度也⽐较快。

⽼年代 GC（Major GC / Full GC）：指发⽣在⽼年代的 GC，出现了 Major GC，经常

会伴随⾄少⼀次的 Minor GC（但⾮绝对的，在 ParallelScavenge 收集器的收集策略⾥ 就有直接进⾏ Major GC 的策略选择过程） 。MajorGC 的速度⼀般会⽐ Minor GC 慢 10 倍以上。 Minor GC触发机制： 当年轻代满时就会触发Minor GC，这⾥的年轻代满指的是Eden代满，Survivor满不会引发GC Full GC触发机制： 当年⽼代满时会引发Full GC，Full GC将会同时回收年轻代、年⽼代， 当永久代满时也会引发Full GC，会导致Class、Method元信息的卸载 其中Minor GC如下图所示

![image 1](<Minor GC和Full GC区别.note_images/imageFile1.png>)

虚拟机给每个对象定义了⼀个对象年龄（Age）计数器。如果对象在 Eden 出⽣并经过第⼀次 Minor GC 后仍然存活，并且能被 Survivor 容纳的话，将被移动到 Survivor 空间中，并将对象年龄设为 1。 对象在 Survivor 区中每熬过⼀次 Minor GC，年龄就增加 1 岁，当它的年龄增加到⼀定程度（默认为 15 岁 ） 时 ， 就 会 被 晋 升 到 ⽼ 年 代 中 。 对 象 晋 升 ⽼ 年 代 的 年 龄 阈 值 ， 可 以 通 过 参 数 XX:MaxTenuringThreshold (阈值)来设置。

