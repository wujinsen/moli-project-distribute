典型JVM参数设置格式： java -Xmx350m -Xms350m -Xmn2g -Xs128k

- -Xmx350m：最⼤堆内存
- -Xms350m：初始化堆内存
- -Xmn2g：年轻代内存。
- -Xs128k：每个线程的堆栈⼤⼩。

- -X NewRatio=4:年轻代与年⽼代的⽐值。
- -X SurvivorRatio=4：年轻代中Eden区与Survivor区的⼤⼩⽐值

- -XX:PermSize： 永久代(⽅法区)的初始⼤⼩

- -X MaxPermSize=16m:持久代⼤⼩为16m。
- -X MaxTenuringThreshold=0：设置垃圾最⼤年龄。如果设置为0的话，则年轻代对象不经过 Survivor区，直 接进⼊年⽼代。

- -X:+UseParalelGC：选择垃圾收集器为并⾏收集器。此配置仅对年轻代有效。⽽年⽼代仍旧使⽤串 ⾏收集。

- -X ParalelGCThreads=20：配置并⾏收集器的线程数，即：同时多少个线程⼀起进⾏垃圾回收。此 值最好配置与处理器数⽬相等。

- -X:+UseParalelOldGC：配置年⽼代垃圾收集⽅式为并⾏收集。JDK6.0⽀持对年⽼代并⾏收集。
- -X MaxGCPauseMilis=10:设置每次年轻代垃圾回收的最⻓时间，如果⽆法满⾜此时间，JVM会⾃ 动调整年轻代⼤⼩，以满⾜此值。
- -X:+UseAdaptiveSizePolicy：设置此选项后，并⾏收集器会⾃动选择年轻代区⼤⼩和相应的Survivor 区⽐例，以达到⽬标系统规定的最低相应时间或者收集频率等，此值建议使⽤并⾏收集器时，⼀直打 开。

- -X:+UseConcMarkSwepGC：设置年⽼代为并发收集。
- -X:+UseParNewGC:设置年轻代为并⾏收集。可与CMS收集同时使⽤。

- -X CMSFulGCsBeforeCompaction：由于并发收集器不对内存空间进⾏压缩、整理，所以运⾏⼀段 时间以后会产⽣“碎⽚”，使得运⾏效率降低。此值设置运⾏多少次GC以后对内存空间进⾏压缩、整 理。

- -X:+UseCMSCompactAtFulColection：打开对年⽼代的压缩。可能会影响性能，但是可以消除碎⽚

- -XX:+HeapDumpOnOutOfMemoryError：让虚拟机在发⽣内存溢出时 Dump 出当前的内存堆转储快照，以便分析⽤

- -X:+PrintGC：打印gc⽇志

- -XX:+PrintGCDetails： 打印 GC 信息

- -X:+PrintGCTimeStamps -X:+PrintGC：PrintGCTimeStamps可与上⾯两个混合使⽤ 输出形式： 1.851: [GC 98328K->93620K(13012K), 0.082960 secs]
- -X:+PrintGCAplicationConcurentTime:打印每次垃圾回收前，程序未中断的执⾏时间。可与上⾯混 合使⽤ 输出形式：Aplication time: 0.5291524 seconds


- -X:+PrintGCAplicationStopedTime：打印垃圾回收期间程序暂停的时间。可与上⾯混合使⽤ 输出形式：Total time for which aplication threads were stoped: 0.046829 seconds
- -X PrintHeapAtGC:打印GC前后的详细堆栈信息
- -Xlogc:filename:与上⾯⼏个配合使⽤，把相关⽇志信息记录到⽂件以便分析。

- -Xnoclasgc关闭CLAS的垃圾回收功能,就是虚拟机加载的类,即便是不使⽤,没有实例也不会回收 收集器设置

- -X:+UseSerialGC:设置串⾏收集器
- -X:+UseParalelGC:设置并⾏收集器
- -X:+UseParaledlOldGC:设置并⾏年⽼代收集器
- -X:+UseConcMarkSwepGC:设置并发收集器 并⾏收集器设置

- -X ParalelGCThreads=n:设置并⾏收集器收集时使⽤的CPU数。并⾏收集线程数。
- -X MaxGCPauseMilis=n:设置并⾏收集最⼤暂停时间
- -X GCTimeRatio=n:设置垃圾回收时间占程序运⾏时间的百分⽐。公式为1/(1+n) 并发收集器设置
- -X:+CMSIncrementalMode:设置为增量模式。适⽤于单CPU情况。
- -X ParalelGCThreads=n:设置并发收集器年轻代收集⽅式为并⾏收集时，使⽤的CPU数。并⾏收集 线程数。 参数及其默认值 描述

- -X:-DisableExplicitGC 禁⽌调⽤System.gc()；但jvm的gc仍然有效
- -X:+MaxFDLimit 最⼤化⽂件描述符的数量限制
- -X:+ScavengeBeforeFulGC 新⽣代GC优先于Ful GC执⾏
- -X:+UseGCOverheadLimit 在抛出 OM之前限制jvm耗费在GC上的时间⽐例
- -X:-UseConcMarkSwepGC 对⽼⽣代采⽤并发标记交换算法进⾏GC
- -X:-UseParalelGC 启⽤并⾏GC
- -X:-UseParalelOldGC 对Ful GC启⽤并⾏，当-X:-UseParalelGC启⽤时该项⾃动启⽤
- -X:-UseSerialGC 启⽤串⾏GC
- -X:+UseThreadPriorities 启⽤本地线程优先级 参数及其默认值 描述

- -X LargePageSizeInBytes=4m 设置⽤于Java堆的⼤⻚⾯尺⼨
- -X MaxHeapFreRatio=70 GC后java堆中空闲量占的最⼤⽐例
- -X MaxNewSize=size 新⽣成对象能占⽤内存的最⼤值
- -X MaxPermSize=64m ⽼⽣代对象能占⽤内存的最⼤值
- -X MinHeapFreRatio=40 GC后java堆中空闲量占的最⼩⽐例
- -X NewRatio=2 新⽣代内存容量与⽼⽣代内存容量的⽐例
- -X NewSize=2.125m 新⽣代对象⽣成时占⽤内存的默认值
- -X ReservedCodeCacheSize=32m 保留代码占⽤的内存容量


- -X ThreadStackSize=512 设置线程栈⼤⼩，若为0则使⽤系统默认值
- -X:+UseLargePages 使⽤⼤⻚⾯内存 参数及其默认值 描述

- -X:-CITime 打印消耗在JIT编译的时间
- -X ErorFile=./hs_er_pid<pid>.log 保存错误⽇志或者数据到⽂件中
- -X:-ExtendedDTraceProbes 开启solaris特有的dtrace探针
- -X HeapDumpPath=./java_pid<pid>.hprof 指定导出堆信息时的路径或⽂件名
- -X:-HeapDumpOnOutOfMemoryEror 当⾸次遭遇 OM时导出此时堆中相关信息
- -X OnEror="<cmd args>;<cmd args>" 出现致命EROR之后运⾏⾃定义命令
- -X OnOutOfMemoryEror="<cmd args>;<cmd args>" 当⾸次遭遇 OM时执⾏⾃定义命令
- -X:-PrintClasHistogram 遇到Ctrl-Break后打印类实例的柱状信息，与jmap -histo功能相同
- -X:-PrintConcurentLocks 遇到Ctrl-Break后打印并发锁的相关信息，与jstack -l功能相同
- -X:-PrintComandLineFlags 打印在命令⾏中出现过的标记
- -X:-PrintCompilation 当⼀个⽅法被编译时打印相关信息
- -X:-PrintGC 每次GC时打印相关信息
- -X:-PrintGC Details 每次GC时打印详细信息
- -X:-PrintGCTimeStamps 打印每次GC的时间戳
- -X:-TraceClasLoading 跟踪类的加载信息
- -X:-TraceClasLoadingPreorder 跟踪被引⽤到的所有类的加载信息
- -X:-TraceClasResolution 跟踪常量池
- -X:-TraceClasUnloading 跟踪类的卸载信息
- -X:-TraceLoaderConstraints 跟踪类加载器约束的相关信息
- -X:+UseAdaptiveSizePolicy：设置此选项后，并⾏收集器会⾃动选择年轻代区⼤⼩和相应的 Survivor区⽐例，以达到⽬标系统规定的最低相应时间或者收集频率等，此值建议使⽤并⾏收集器时， ⼀直打开。
- -X MaxTenuringThreshold=0：设置垃圾最⼤年龄。如果设置为0的话，则年轻代对象不经过 Survivor区，直接进⼊年 ⽼代。对于年⽼代⽐较多的应⽤，可以提⾼效率。如果将此值设置为⼀个较 ⼤值，则年轻代对象会在Survivor区进⾏多次复制，这样可以增加对象再年轻代 的存活时间，增加在 年轻代即被回收的概论。
- -X MaxGCPauseMilis=10:设置每次年轻代垃圾回收的最⻓时间，如果⽆法满⾜此时间，JVM 会⾃ 动调整年轻代⼤⼩，以满⾜此值。


<table>
  <tr>
    <th>参数名称</th>
    <th>含义</th>
    <th>默认值</th>
    <th> </th>
  </tr>
  <tr>
    <td>-Xms</td>
    <td>初始堆⼤⼩</td>
    <td>物理内存的1/64(<1GB)</td>
    <td>默认 (MinHeapFreRatio参 数可以调整)空余堆内存 ⼩于40%时，JVM就会 增⼤堆直到-Xmx的最⼤</td>
  </tr>
  <tr>
    <td>-Xmx</td>
    <td>最⼤堆⼤⼩</td>
    <td>物理内存的1/4(<1GB)</td>
    <td>限制. 默认 (MaxHeapFreRatio参 数可以调整)空余堆内存 ⼤于70%时，JVM会减 少堆直到 -Xms的最⼩ 限制</td>
  </tr>
  <tr>
    <td>-Xmn</td>
    <td>年轻代⼤⼩(1.4or lator)</td>
    <td> </td>
    <td>注意 ：此处的⼤⼩是 （eden+ 2 survivor space).与jmap -heap中 显示的New gen是不同 的。<br><br>整个堆⼤⼩=年轻代⼤ ⼩ + 年⽼代⼤⼩ + 持久 代⼤⼩.<br><br>增⼤年轻代后,将会减⼩ 年⽼代⼤⼩.此值对系统 性能影响较⼤,Sun官⽅</td>
  </tr>
  <tr>
    <td>-X NewSize</td>
    <td>设置年轻代⼤⼩(for</td>
    <td> </td>
    <td>推荐配置为整个堆的3/8</td>
  </tr>
  <tr>
    <td>-X MaxNewSize</td>
    <td>1.3/1.4) 年轻代最⼤值(for</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>-X PermSize</td>
    <td>1.3/1.4) 设置持久代(perm gen) 初始值</td>
    <td>物理内存的1/64</td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td>设置持久代最⼤值</td>
    <td> </td>
    <td> </td>
  </tr>
</table>


# -X MaxPermSize 物理内存的1/4

<table>
  <tr>
    <th>-Xs</th>
    <th>每个线程的堆栈⼤⼩</th>
    <th> </th>
    <th>JDK5.0以后每个线程堆 栈⼤⼩为1M,以前每个 线程堆栈⼤⼩为256K. 更具应⽤的线程所需内 存⼤⼩进⾏ 调整.在相同 物理内存下,减⼩这个值 能⽣成更多的线程.但是 操作系统对⼀个进程内 的线程数还是有限制的, 不能⽆限⽣成,经验值在 3 0~5 0左右<br><br>⼀般⼩的应⽤， 如果栈 不是很深， 应该是128k 够⽤的 ⼤的应⽤建议使 ⽤256k。这个选项对性 能影响⽐较⼤，需要严 格的测试。（校⻓）<br><br>和threadstacksize选项 解释很类似,官⽅⽂档似 乎没有解释,在论坛中有 这样⼀句话:"”<br><br>-Xs is translated in a VM flag named ThreadStackSize”<br><br>⼀般设置这个值就可以 了。</th>
  </tr>
  <tr>
    <td>-<br><br>X:ThreadStackSize</td>
    <td>Thread Stack Size</td>
    <td> </td>
    <td>(0 means use default stack size) [Sparc: 512; Solaris x86: 320 (was 256 prior in 5.0 and earlier); Sparc 64 bit:<br><br>4; Linux amd64: 1024 (was 0 in 5.0 and</td>
  </tr>
  <tr>
    <td>-X NewRatio</td>
    <td>年轻代(包括Eden和两 个Survivor区)与年⽼代 的⽐值(除去持久代)</td>
    <td> </td>
    <td>earlier); al others 0.] -X NewRatio=4表示 年轻代与年⽼代所占⽐ 值为1:4,年轻代占整个 堆栈的1/5 Xms=Xmx并且设置了 Xmn的情况下，该参数 不需要进⾏设置。</td>
  </tr>
</table>


<table>
  <tr>
    <th>-X SurvivorRatio</th>
    <th>Eden区与Survivor区的 ⼤⼩⽐值</th>
    <th> </th>
    <th>设置为8,则两个 Survivor区与⼀个Eden 区的⽐值为2:8,⼀个 Survivor区占整个年轻</th>
  </tr>
  <tr>
    <td>-<br><br>X LargePageSizeInBy tes</td>
    <td>内存⻚的⼤⼩不可设置 过⼤， 会影响Perm的 ⼤⼩</td>
    <td> </td>
    <td>代的1/10<br><br>=128m</td>
  </tr>
  <tr>
    <td>-<br><br>X:+UseFastAcesor</td>
    <td>原始类型的快速优化</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>Methods<br><br>-<br><br>X:+DisableExplicitGC</td>
    <td>关闭System.gc()</td>
    <td> </td>
    <td>这个参数需要严格的测 试</td>
  </tr>
  <tr>
    <td>-<br><br>X MaxTenuringThres hold</td>
    <td>垃圾最⼤年龄</td>
    <td> </td>
    <td>如果设置为0的话,则年 轻代对象不经过 Survivor区,直接进⼊年 ⽼代. 对于年⽼代⽐较多 的应⽤,可以提⾼效率.如 果将此值设置为⼀个较 ⼤值,则年轻代对象会在 Survivor区进⾏多次复 制,这样可以增加对象再 年轻代的存活 时间,增加 在年轻代即被回收的概 率<br><br>该参数只有在串⾏GC时</td>
  </tr>
  <tr>
    <td>-X:+AgresiveOpts</td>
    <td>加快编译</td>
    <td> </td>
    <td>才有效.</td>
  </tr>
  <tr>
    <td>-<br><br>X:+UseBiasedLockin</td>
    <td>锁机制的性能改善</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>g<br><br>-Xnoclasgc</td>
    <td>禁⽤垃圾回收</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>-<br><br>X SoftRefLRUPolicyM SPerMB</td>
    <td>每兆堆空闲空间中 SoftReference的存活时 间</td>
    <td>1s</td>
    <td>softly reachable objects wil remain alive for some amount of time after the last time they were referenced. The default value is one second of lifetime per fre megabyte in the</td>
  </tr>
</table>


# heap

<table>
  <tr>
    <th>-<br><br>X PretenureSizeThres hold</th>
    <th>对象超过多⼤是直接在 旧⽣代分配</th>
    <th>0</th>
    <th>单位字节 新⽣代采⽤ Paralel Scavenge GC 时⽆效<br><br>另⼀种直接在旧⽣代分 配的情况是⼤的数组对 象,且数组中⽆外部引⽤</th>
  </tr>
  <tr>
    <td>-<br><br>X TLABWasteTargetP</td>
    <td>TLAB占eden区的百分 ⽐</td>
    <td>1%</td>
    <td>对象.</td>
  </tr>
  <tr>
    <td>ercent -X:+</td>
    <td>FulGC时是否先YGC</td>
    <td>false</td>
    <td> </td>
  </tr>
</table>


ColectGen0First

并⾏收集器相关参数

<table>
  <tr>
    <th>-X:+UseParalelGC</th>
    <th>Ful GC采⽤paralel MSC<br><br>(此项待验证)</th>
    <th> </th>
    <th>选择垃圾收集器为并⾏ 收集器.此配置仅对年轻 代有效.即上述配置下,年 轻代使⽤并发收集,⽽年 ⽼代仍旧使⽤串⾏收集.</th>
  </tr>
  <tr>
    <td>-X:+UseParNewGC</td>
    <td>设置年轻代为并⾏收集</td>
    <td> </td>
    <td>(此项待验证) 可与CMS收集同时使⽤ JDK5.0以上,JVM会根 据系统配置⾃⾏设置,所 以⽆需再设置此值</td>
  </tr>
  <tr>
    <td>-X ParalelGCThreads</td>
    <td>并⾏收集器的线程数</td>
    <td> </td>
    <td>此值最好配置与处理器 数⽬相等 同样适⽤于</td>
  </tr>
  <tr>
    <td>-<br><br>X:+UseParalelOldGC</td>
    <td>年⽼代垃圾收集⽅式为 并⾏收集(Paralel</td>
    <td> </td>
    <td>CMS<br><br>这个是JAVA 6出现的参 数选项</td>
  </tr>
  <tr>
    <td>-X MaxGCPauseMilis</td>
    <td>Compacting) 每次年轻代垃圾回收的 最⻓时间(最⼤暂停时</td>
    <td> </td>
    <td>如果⽆法满⾜此时 间,JVM会⾃动调整年轻</td>
  </tr>
  <tr>
    <td>-<br><br>X:+UseAdaptiveSizeP olicy</td>
    <td>间)<br><br>⾃动选择年轻代区⼤⼩ 和相应的Survivor区⽐ 例</td>
    <td> </td>
    <td>代⼤⼩,以满⾜此值. 设置此选项后,并⾏收集 器会⾃动选择年轻代区 ⼤⼩和相应的Survivor 区⽐例,以达到⽬标系统 规定的最低相应时间或 者收集频率等,此值建议 使⽤并⾏收集器时,⼀直</td>
  </tr>
  <tr>
    <td>-X GCTimeRatio</td>
    <td>设置垃圾回收时间占程 序运⾏时间的百分⽐</td>
    <td> </td>
    <td>打开.<br><br>公式为1/(1+n)</td>
  </tr>
  <tr>
    <td>-X:+ ScavengeBeforeFulGC</td>
    <td>Ful GC前调⽤YGC</td>
    <td>true</td>
    <td>Do young generation GC prior to a ful GC.</td>
  </tr>
</table>


(Introduced in 1.4.1.)

CMS相关参数

<table>
  <tr>
    <th>-<br><br>X:+UseConcMarkSw epGC</th>
    <th>使⽤CMS内存收集</th>
    <th> </th>
    <th>测试中配置这个以后,-<br><br>X NewRatio=4的配置 失效了,原因不明.所以, 此时年轻代⼤⼩最好⽤-</th>
  </tr>
  <tr>
    <td>-X:+AgresiveHeap</td>
    <td> </td>
    <td> </td>
    <td>Xmn设置. ? 试图是使⽤⼤量的物理 内存 ⻓时间⼤内存使⽤的优 化，能检查计算资源 （内存， 处理器数量） ⾄少需要256MB内存 ⼤量的CPU／内存， （在1.4.1在4CPU的机 器上已经显示有提升）</td>
  </tr>
  <tr>
    <td>-<br><br>X CMSFulGCsBefore Compaction</td>
    <td>多少次后进⾏内存压缩</td>
    <td> </td>
    <td>由于并发收集器不对内 存空间进⾏压缩,整理,所 以运⾏⼀段时间以后会 产⽣"碎⽚",使得运⾏效 率降低.此值设置运⾏多 少次GC以后对内存空间</td>
  </tr>
  <tr>
    <td>-<br><br>X:+CMSParalelRema</td>
    <td>降低标记停顿</td>
    <td> </td>
    <td>进⾏压缩,整理.</td>
  </tr>
  <tr>
    <td>rkEnabled<br><br>-<br><br>X+UseCMSCompact AtFulColection</td>
    <td>在FUL GC的时候， 对 年⽼代的压缩</td>
    <td> </td>
    <td>CMS是不会移动内存 的， 因此， 这个⾮常容 易产⽣碎⽚， 导致内存 不够⽤， 因此， 内存的 压缩这个时候就会被启 ⽤。 增加这个参数是个 好习惯。<br><br>可能会影响性能,但是可 以消除碎⽚</td>
  </tr>
  <tr>
    <td>-<br><br>X:+UseCMSInitiating</td>
    <td>使⽤⼿动定义初始化定 收集</td>
    <td> </td>
    <td>禁⽌hostspot⾃⾏触发</td>
  </tr>
</table>


# OcupancyOnly 义开始CMS CMS GC

<table>
  <tr>
    <th>-<br><br>X CMSInitiatingOcu pancyFraction=70</th>
    <th>使⽤cms作为垃圾回收<br><br>使⽤70％后开始CMS收 集</th>
    <th>92</th>
    <th>为了保证不出现 promotion failed(⻅下 ⾯介绍)错误,该值的设 置需要满⾜以下公式 CMSInitiatingOcupa<br><br>计算公式</th>
  </tr>
  <tr>
    <td>-<br><br>X CMSInitiatingPerm</td>
    <td>设置Perm Gen使⽤到达 多少⽐率时触发</td>
    <td>92</td>
    <td>ncyFraction</td>
  </tr>
  <tr>
    <td>OcupancyFraction<br><br>-<br><br>X:+CMSIncremental</td>
    <td>设置为增量模式</td>
    <td> </td>
    <td>⽤于单CPU情况</td>
  </tr>
  <tr>
    <td>Mode<br><br>-<br><br>X:+CMSClasUnloadi</td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


ngEnabled

辅助信息

<table>
  <tr>
    <th>-X:+PrintGC</th>
    <th> </th>
    <th> </th>
    <th>输出形式: [GC18250K>1353K(13012K), 0.094143 secs] [Ful GC 121376K>10414K(13012K),</th>
  </tr>
  <tr>
    <td>-X:+PrintGCDetails</td>
    <td> </td>
    <td> </td>
    <td>0.0650971 secs] 输出形式:[GC [DefNew: 8614K>781K(908K), 0.0123035 secs]<br><br>1820K>13543K(13012K), 0.012463 secs] [GC [DefNew: 8614K>8614K(908K),<br><br>0. 065 secs] [Tenured: 12761K>10414K(121024K), 0.04348 secs] 121376K>10414K(13012K),</td>
  </tr>
  <tr>
    <td>-<br><br>X:+PrintGCTimeStam</td>
    <td> </td>
    <td> </td>
    <td>0.0436268 secs]</td>
  </tr>
  <tr>
    <td>ps<br><br>-<br><br>X:+PrintGC PrintGCTi meStamps</td>
    <td> </td>
    <td> </td>
    <td>可与-X:+PrintGC -<br><br>X:+PrintGCDetails混 合使⽤<br><br>输出形式:1.851: [GC 9828K>93620K(13012K),</td>
  </tr>
  <tr>
    <td>-<br><br>X:+PrintGCAplicatio nStopedTime</td>
    <td>打印垃圾回收期间程序 暂停的时间.可与上⾯混 合使⽤</td>
    <td> </td>
    <td>0.082960 secs] 输出形式:Total time for which aplication threads were st ped:</td>
  </tr>
  <tr>
    <td>-<br><br>X:+PrintGCAplicatio nConcurentTime</td>
    <td>打印每次垃圾回收前,程 序未中断的执⾏时间.可 与上⾯混合使⽤</td>
    <td> </td>
    <td>0.046829 seconds 输出形式:Aplication time: 0.5291524 seconds</td>
  </tr>
  <tr>
    <td>-X:+PrintHeapAtGC</td>
    <td>打印GC前后的详细堆栈 信息</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>-Xlogc:filename</td>
    <td>把相关⽇志信息记录到 ⽂件以便分析.<br><br>与上⾯⼏个配合使⽤</td>
    <td> </td>
    <td> </td>
  </tr>
</table>


<table>
  <tr>
    <th>-<br><br>X:+PrintClasHistogr</th>
    <th>garbage colects before printing the</th>
    <th> </th>
    <th> </th>
  </tr>
  <tr>
    <td>am<br><br>-X:+PrintTLAB</td>
    <td>histogram. 查看TLAB空间的使⽤情 况</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>X:+PrintTenuringDistr ibution</td>
    <td>查看每次minor GC后新 的存活周期的阈值</td>
    <td> </td>
    <td>Desired survivor size 1048576 bytes, new threshold 7 (max 15)<br><br>new threshold 7即标识 新的存活周期的阈值为<br><br>。</td>
  </tr>
</table>


# 7

