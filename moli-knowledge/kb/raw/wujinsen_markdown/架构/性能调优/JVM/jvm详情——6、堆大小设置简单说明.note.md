htp:/t.zoukankan.com/lemon-flm-p-1019429.html

年轻代的设置很关键 JVM中最⼤堆⼤⼩有三⽅⾯限制：相关操作系统的数据模型（32-bt还是64-bit）限制；系统的可⽤虚 拟内存限制；系统的可⽤物理内存限制。32位系统下，⼀般限制在1.5G~2G；64为操作系统对内存⽆ 限制。在Windows Server 203 系统，3.5G物理内存，JDK5.0下测试，最⼤可设置为1478m。（⼀般 最⼤堆配置不能超过内存的⼀半）

典型设置： java -Xmx350m -Xms350m -Xmn2g –Xs128k

- -Xmx350m：设置JVM最⼤可⽤内存为350M。
- -Xms350m：设置JVM促使内存为350m。此值可以设置与-Xmx相同，以避免每次垃圾回收完成 后JVM重新分配内存。
- -Xmn2g：设置年轻代⼤⼩为2G。整个堆⼤⼩=年轻代⼤⼩ + 年⽼代⼤⼩ + 持久代⼤⼩。持久代⼀般 固定⼤⼩为64m，所以增⼤年轻代后，将会减⼩年⽼代⼤⼩。此值对系统性能影响较⼤，Sun官⽅推荐 配置为整个堆的3/8。
- -Xs128k：设置每个线程的堆栈⼤⼩。JDK5.0以后每个线程堆栈⼤⼩为1M，以前每个线程堆栈⼤⼩ 为256K。更具应⽤的线程所需内存⼤⼩进⾏调整。在相同物理内存下，减⼩这个值能⽣成更多的线 程。但是操作系统对⼀个进程内的线程数还是有限制的，不能⽆限⽣成，经验值在3 0~5 0左 右。

java -Xmx350m -Xms350m -Xs128k -X NewRatio=4 -X SurvivorRatio=4 X MaxPermSize=16m -X MaxTenuringThreshold=0

- -X NewRatio=4:设置年轻代（包括Eden和两个Survivor区）与年⽼代的⽐值（除去持久代）。设置 为4，则年轻代与年⽼代所占⽐值为1：4，年轻代占整个堆栈的1/5
- -X SurvivorRatio=4：设置年轻代中Eden区与Survivor区的⼤⼩⽐值。设置为4，则两个Survivor区 与⼀个Eden区的⽐值为2:4，⼀个Survivor区占整个年轻代的1/6
- -X MaxTenuringThreshold=0：设置垃圾最⼤年龄。如果设置为0的话，则年轻代对象不经过 Survivor区，直接进⼊年⽼代。对于年⽼代⽐较多的应⽤，可以提⾼效率。如果将此值设置为⼀个较⼤ 值，则年轻代对象会在Survivor区进⾏多次复制，这样可以增加对象再年轻代的存活时间，增加在年轻 代即被回收的概论。 回收器选择


JVM给了三种选择：串⾏收集器、并⾏收集器、并发收集器，但是串⾏收集器只适⽤于⼩数据量的情 况，所以这⾥的选择主要针对并⾏收集器和并发收集器。默认情况下，JDK5.0以前都是使⽤串⾏收集 器，如果想使⽤其他收集器需要在启动时加⼊相应参数。JDK5.0以后，JVM会根据当前系统配置进⾏ 判断。

- 1、吞吐量优先的并⾏收集器 如上⽂所述，并⾏收集器主要以到达⼀定的吞吐量为⽬标，适⽤于科学技术和后台处理等。 典型配置： java -Xmx380m -Xms380m -Xmn2g -Xs128k-X:+UseParalelGC -

X ParalelGCThreads=20

- -X:+UseParalelGC：选择垃圾收集器为并⾏收集器。此配置仅对年轻代有效。即上述配置下，年轻 代使⽤并发收集，⽽年⽼代仍旧使⽤串⾏收集。
- -X ParalelGCThreads=20：配置并⾏收集器的线程数，即：同时多少个线程⼀起进⾏垃圾回收。 此值最好配置与处理器数⽬相等。


java -Xmx350m -Xms350m -Xmn2g -Xs128k -X:+UseParalelGC -X ParalelGCThreads=20X:+UseParalelOldGC

-X:+UseParalelOldGC：配置年⽼代垃圾收集⽅式为并⾏收集。JDK6.0⽀持对年⽼代并⾏收集。

java -Xmx350m -Xms350m -Xmn2g -Xs128k -X:+UseParalelGC X MaxGCPauseMilis=10

-X MaxGCPauseMilis=10:设置每次年轻代垃圾回收的最⻓时间，如果⽆法满⾜此时间，JVM会 ⾃动调整年轻代⼤⼩，以满⾜此值。 java -Xmx350m -Xms350m -Xmn2g -Xs128k -X:+UseParalelGC-

X MaxGCPauseMilis=10 -X:+UseAdaptiveSizePolicy

-X:+UseAdaptiveSizePolicy：设置此选项后，并⾏收集器会⾃动选择年轻代区⼤⼩和相应的 Survivor区⽐例，以达到⽬标系统规定的最低相应时间或者收集频率等，此值建议使⽤并⾏收集器时， ⼀直打开。

- 2、响应时间优先的并发收集器 如上⽂所述，并发收集器主要是保证系统的响应时间，减少垃圾收集时的停顿时间。适⽤于应⽤服务 器、电信领域等。


典型配置： java -Xmx350m -Xms350m -Xmn2g -Xs128k -X ParalelGCThreads=20 -

X:+UseConcMarkSwepGC -X:+UseParNewGC

- -X:+UseConcMarkSwepGC：设置年⽼代为并发收集。测试中配置这个以后，-X NewRatio=4 的配置失效了，原因不明。所以，此时年轻代⼤⼩最好⽤-Xmn设置。


- -X:+UseParNewGC: 设置年轻代为并⾏收集。可与CMS收集同时使⽤。JDK5.0以上，JVM会根据 系统配置⾃⾏设置，所以⽆需再设置此值。

java -Xmx350m -Xms350m -Xmn2g -Xs128k -X:+UseConcMarkSwepGCX CMSFulGCsBeforeCompaction=5 -X:+UseCMSCompactAtFulColection

- -X CMSFulGCsBeforeCompaction：由于并发收集器不对内存空间进⾏压缩、整理，所以运⾏⼀ 段时间以后会产⽣“碎⽚”，使得运⾏效率降低。此值设置运⾏多少次GC以后对内存空间进⾏压缩、整 理。
- -X:+UseCMSCompactAtFulColection：打开对年⽼代的压缩。可能会影响性能，但是可以消除 碎⽚


