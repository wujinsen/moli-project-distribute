GC 堆

- 1、GC 分为两种：Minor GC、Ful GC ( 或称为 Major GC )。
- 2、Minor GC 是发⽣在新⽣代中的垃圾收集动作，所采⽤的是复制算法。

- a、新⽣代⼏乎是所有 Java 对象出⽣的地⽅，即 Java 对象申请的内存以及存放都是在这个地⽅。
- b、Java 中的⼤部分对象通常不需⻓久存活，具有朝⽣夕灭的性质。
- c、当⼀个对象被判定为 “死亡” 的时候，GC 就有责任来回收掉这部分对象的内存空间。
- d、新⽣代是 GC 收集垃圾的频繁区域。
- e、当对象在 Eden（+from） 出⽣后，在经过⼀次 Minor GC 后，如果对象还存活，并且能够被


另外⼀块 Survivor 区域

所容纳( 这⾥应为 to 区域 )，则使⽤复制算法将这些仍然还存活的对象复制到另外⼀块 Survivor 区域 ( 即 to 区域 ) 中，

- f、 然后清理所使⽤过的 Eden 以及 Survivor 区域 ( 即 from 区域 )，并且将这些对象的年龄设置

为1，

- g、以后对象在 Survivor 区每熬过⼀次 Minor GC，就将对象的年龄 + 1，
- h、当对象的年龄达到某个值时 ( 默认是 15 岁，可以通过参数 -X MaxTenuringThreshold 来设


定 )，这些对象就会成为 ⽼年代。

j、但这也不是⼀定的，对于⼀些较⼤的对象 ( 即需要分配⼀块较⼤的连续内存空间 ) 则是直接进 ⼊到⽼年代。

- 3、Ful GC 是发⽣在⽼年代的垃圾收集动作，所采⽤的是标记-清除算法。


- a、⽼年代⾥⾯的对象⼏乎个个都是在 Survivor 区域中熬过来的，它们是不会那么容易就 “死掉”

了的。

- b、Ful GC 发⽣的次数不会有 Minor GC 那么频繁，并且做⼀次 Ful GC 要⽐进⾏⼀次 Minor GC

的时间更⻓。

- c、标记-清除算法收集垃圾的时候会产⽣许多的内存碎⽚ ( 即不连续的内存空间 )，
- d、此后需要为较⼤的对象分配内存空间时，若⽆法找到⾜够的连续的内存空间，就会提前触发⼀


次 GC 的收集动作。

- GC ⽇志1


![image 1](<理解GC日志.note_images/imageFile1.png>)

- 1、“3.125：”和“10. 67：”代表了 GC发⽣的时间，这个数字的含义是 从Java虚拟机启动以来经过的秒数。


- 2、“[GC”和“[Ful GC”说明了这次 垃圾收集的停顿类型，⽽ 不是⽤来区分新⽣代GC还是⽼年代GC的。

“Ful”，说明这次GC是发⽣了Stop-The-World的，例如下⾯这段新⽣代收集器ParNew的⽇志也会出 现“[Ful GC”（这⼀般是因为出现了分配担保失败之类的问题，所以才导致STW）。如果是调⽤ System.gc（）⽅ 法所触发的收集，那么在这⾥将显示“[Ful GC（System）”。[Ful GC 283.736：[ParNew： 26159K-＞26159K（261952K），0. 028 secs]

- 3、[DefNew”、“[Tenured”、“[Perm”表示GC发⽣的区域，这⾥显示的区域名称与使⽤的GC收集器 是密切相关的， “[DefNew”：Serial收集器中的新⽣代名为“Default New Generation”

“[ParNew”：ParNew收集器，新⽣代，意为“Paralel New Generation”。 “PSYoungGen”： Paralel Scavenge收集器

⽼年代和永久代同理，名称也是由收集器决定的。Tenured表示⽼年代

- 4、“324K-＞152K（3712K）”是“GC前该内存区域已使⽤容量-＞GC后该内存区域已使⽤ 容量（该内存区域总容量）”。
- 5、⽅括号之外“324K-＞152K（ 1904K）”表示“GC前Java堆已使⽤容量-＞ GC后Java堆已使⽤容量（Java堆总容量）”。
- 6、“0.025925 secs”表示该 内存区域GC所占⽤的时间， 单位是秒。
- 7、“[Times：user=0.01 sys=0.0，real=0.02 secs]” user：⽤户态消耗的CPU时间 sys：内核态消耗的CPU时间 real：操作从开始到结束所经过的墙钟时间（Wal Clock Time）


CPU时间与墙钟时间的区别是，墙钟时间包括各种⾮运算的等待耗时，例如等待磁盘I/O、等待线程阻 塞，⽽CPU时间不包括这些耗时，但当系统有多CPU或者多核的话，多线程操作会叠加这些CPU时 间，所以读者看到user或sys时间超过real时间是完全正常的。

# GC ⽇志2

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br></th>
    <th>publicstaticvoid main(String[] args) { Object obj = new Object(); System.gc(); System.out.println(); obj = new Object(); obj = new Object(); System.gc(); System.out.println();<br><br>}</th>
  </tr>
</table>


设置 JVM 参数为 -X:+PrintGCDetails，使得控制台能够显示 GC 相关的⽇志信息，执⾏上⾯代码， 下⾯是其中⼀次执⾏的结果。

# GC ⽇志3

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br><br><br>25<br><br></th>
    <th>1 /**<br><br>2 -Xms60m<br><br>3 -Xmx60m<br><br>4 -Xmn20m<br><br>5 -XX:NewRatio=2 ( 若 Xms = Xmx, 并且设定了 Xmn, 那么该项<br><br>配置就不需要配置了 )<br><br>6 -XX:SurvivorRatio=8<br><br>7 -XX:PermSize=30m<br><br>8 -XX:MaxPermSize=30m<br><br>9 -XX:+PrintGCDetails<br>10 */<br><br>11 publicstaticvoid main(String[] args) {<br><br>12 new Test().doTest();<br><br>13 }<br><br>14<br><br>15 publicvoid doTest(){<br><br>16 Integer M<br><br>= new Integer( 1024 * 1024 * 1 ); //单位, 兆 (M)<br><br>17 byte [] bytes = newbyte [ 1 * M]; //申请 1M ⼤<br><br>⼩的内存空间<br><br>18 bytes = null ; //断开引⽤链<br><br>19 System.gc(); //通知 GC 收集垃圾<br><br>20 System.out.println();<br><br>21 bytes = newbyte [ 1 * M]; //重新申请 1M ⼤⼩的内<br><br>存空间<br><br>22 bytes = newbyte [ 1 * M]; //再次申请 1M ⼤⼩的内<br><br>存空间<br><br>23 System.gc();<br><br>24 System.out.println();<br><br>25 }<br><br><br></th>
  </tr>
</table>


## 按上⾯代码中注释的信息设定 jvm 相关的参数项，并执⾏程序，下⾯是⼀次执⾏完成控制台打印的结 果：

[ GC [ PSYoungGen: 1351K -> 288K (18432K) ] 1351K -> 288K (59392K), 0.00 [ Full GC (System) [ PSYoungGen: 288K -> 0K (18432K) ] [ PSOldGen: 0K

0.0057649 secs ] [ Times: user=0.00 sys=0.00, real=0.01 secs ] [ GC [ PSYoungGen: 2703K -> 1056K (18432K) ] 2863K -> 1216K(59392K), 0 [ Full GC (System) [ PSYoungGen: 1056K -> 0K (18432K) ] [ PSOldGen: 16

0.0052445 secs ] [ Times: user=0.02 sys=0.00, real=0.01 secs ] Heap

PSYoungGen total 18432K, used 327K [0x00000000fec00000, 0x0000000100 eden space 16384K, 2% used [0x00000000fec00000,0x00000000fec51f58,0x0000 from space 2048K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x00000 to space 2048K, 0% used [0x00000000ffc00000,0x00000000ffc00000,0x00000

PSOldGen total 40960K, used 1184K [0x00000000fc400000, 0x00000000f

object space 40960K, 2% used [0x00000000fc400000,0x00000000fc5281f8,0x00 PSPermGen total 30720K, used 2959K [0x00000000fa600000, 0x00000000f

object space 30720K, 9% used [0x00000000fa600000,0x00000000fa8e3ce0,0x00

- 1、堆中新⽣代的内存空间为 18432K ( 约 18M )，eden 的内存空间为 16384K ( 约 16M)，from / to survivor 的内存空间为 2048K ( 约 2M)。
- 2、新⽣代 = eden + from + to = 16 + 2 + 2 = 20M，可⻅新⽣代的内存空间确实是按 Xmn 参数分配得 到的。
- 3、 SurvivorRatio = 8，因此，eden = 8/10 的新⽣代空间 = 8/10 * 20 = 16M。from = to = 1/10 的新 ⽣代空间 = 1/10 * 20 = 2M。
- 4、堆信息中新⽣代的 total 18432K 是这样来的： eden + 1 个 survivor = 16384K + 2048K = 18432K，即约为 18M。因为 jvm 每次只是⽤新⽣代中的 eden 和 ⼀个 survivor，因此新⽣代实际的可 ⽤内存空间⼤⼩为所指定的 90%。因此可以知道，这⾥新⽣代的内存空间指的是新⽣代可⽤的总的内 存空间，⽽不是指整个新⽣代的空间⼤⼩。
- 5、另外，可以看出⽼年代的内存空间为 40960K ( 约 40M )，堆⼤⼩ = 新⽣代 + ⽼年代。因此在这 ⾥，⽼年代 = 堆⼤⼩ – 新⽣代 = 60 – 20 = 40M。
- 6、最后，这⾥还指定了 PermSize = 30m，PermGen 即永久代 ( ⽅法区 )，它还有⼀个名字，叫⾮ 堆，主要⽤来存储由 jvm 加载的类⽂件信息、常量、静态变量等。
- 7、回到 doTest() ⽅法中，可以看到代码在第 17、21、 2 这三⾏中分别申请了⼀块 1M ⼤⼩的内存空 间，并在 19 和 23 这两⾏中分别显式的调⽤了 System.gc()。从控制台打印的信息来看，每次调 System.gc()，是先进⾏ Minor GC，然后再进⾏ Ful GC。


12345678910111213141516171819202122232425 1 /** 2 -Xms60m 3 -Xmx60m 4 Xmn20m 5 -XX:NewRatio=2 ( 若 Xms = Xmx, 并且设定了 Xmn, 那么该项配置就不需要配置了 ) 6 XX:SurvivorRatio=8 7 -XX:PermSize=30m 8 -XX:MaxPermSize=30m 9 XX:+PrintGCDetails10 */ 11 publicstaticvoid main(String[] args) { 12 new Test().doTest(); 13 } 1415 publicvoid doTest(){ 16 Integer M

= new Integer( 1024 * 1024 * 1 ); //单位, 兆(M) 17 byte [] bytes

= newbyte [ 1 * M]; //申请 1M ⼤⼩的内存空间 18 bytes = null ; //断开引⽤ 链 19 System.gc(); //通知 GC 收集垃圾 20 System.out.println(); 21 bytes

= newbyte [ 1 * M]; //重新申请 1M ⼤⼩的内存空间 22 bytes = newbyte [ 1 * M]; //再次申请 1M ⼤⼩的内存空间

23 System.gc(); 24 System.out.println(); 25 }

[ GC [ PSYoungGen: 1351K -> 288K (18432K) ] 1351K -> 288K (59392K), 0.00 [ Full GC (System) [ PSYoungGen: 288K -> 0K (18432K) ] [ PSOldGen: 0K

0.0057649 secs ] [ Times: user=0.00 sys=0.00, real=0.01 secs ] [ GC [ PSYoungGen: 2703K -> 1056K (18432K) ] 2863K -> 1216K(59392K), 0 [ Full GC (System) [ PSYoungGen: 1056K -> 0K (18432K) ] [ PSOldGen: 16

0.0052445 secs ] [ Times: user=0.02 sys=0.00, real=0.01 secs ] Heap

PSYoungGen total 18432K, used 327K [0x00000000fec00000, 0x0000000100 eden space 16384K, 2% used [0x00000000fec00000,0x00000000fec51f58,0x0000 from space 2048K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x00000 to space 2048K, 0% used [0x00000000ffc00000,0x00000000ffc00000,0x00000

PSOldGen total 40960K, used 1184K [0x00000000fc400000, 0x00000000f

object space 40960K, 2% used [0x00000000fc400000,0x00000000fc5281f8,0x00 PSPermGen total 30720K, used 2959K [0x00000000fa600000, 0x00000000f

object space 30720K, 9% used [0x00000000fa600000,0x00000000fa8e3ce0,0x00

- 8、第 19 ⾏触发的 Minor GC 收集分析： 从信息 PSYoungGen : 1351K -> 28K，可以知道，在第 17 ⾏为 bytes 分配的内存空间已经被回收完 成。 引起 GC 回收这 1M 内存空间的因素是第 18 ⾏的 bytes = nul; bytes 为 nul 表明之前申请的那 1M ⼤ ⼩的内存空间现在已经没有任何引⽤变量在使⽤它了， 并且在内存中它处于⼀种不可到达状态 ( 即没有任何引⽤链与 GC Rots 相连 )。那么，当 Minor GC 发⽣的时候，GC 就会来回收掉这部分的内存空间。
- 9、第 19 ⾏触发的 Ful GC 收集分析： 在 Minor GC 的时候，信息显示 PSYoungGen : 1351K -> 28K，再看看 Ful GC 中显示的 PSYoungGen : 28K -> 0K，可以看出，Ful GC 后，新⽣代的内存使⽤变成0K 了


刚刚说到 Ful GC 后，新⽣代的内存使⽤从 28K 变成 0K 了，那么这 28K 到底哪去了 ? 难道都被 GC 当成垃圾回收掉了 ? 当然不是了。我还特意在 main ⽅法中 new 了⼀个 Test 类的实例，这⾥的 Test 类的实例属于⼩对象，它应该被分配到新⽣代内存当中，现在还在调⽤这个实例的 doTest ⽅法 呢，GC 不可能在这个时候来回收它的。 接着往下看 Ful GC 的信息，会发现⼀个很有趣的现象，PSOldGen: 0K -> 160K，可以看到，Ful GC 后，⽼年代的内存使⽤从 0K 变成了 160K，想必你已经猜到⼤概是怎么回事了。当 Ful GC 进⾏的 时候，默认的⽅式是尽量清空新⽣代 ( YoungGen )，因此在调 System.gc() 时，新⽣代 ( YoungGen ) 中存活的对象会提前进⼊⽼年代。

- 10、第 23 ⾏触发的 Minor GC 收集分析： 从信息 PSYoungGen : 2703K -> 1056K，可以知道，在第 21 ⾏创建的，⼤⼩为 1M 的数组被 GC 回 收了。在第 2 ⾏创建的，⼤⼩也为 1M 的数组由于 bytes 引⽤变量还在引⽤它，因此，它暂时未被 GC 回收。


1、第 23 ⾏触发的 Ful GC 收集分析： 在 Minor GC 的时候，信息显示 PSYoungGen : 2703K -> 1056K，Ful GC 中显示的 PSYoungGen :

1056K -> 0K，以及 PSOldGen: 160K ->184K，可以知道，新⽣代 ( YoungGen ) 中存活的对象⼜提 前进⼊⽼年代了。

