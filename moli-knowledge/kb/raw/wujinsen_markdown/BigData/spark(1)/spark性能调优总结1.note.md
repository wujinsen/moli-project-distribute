下⾯这些关于Spark的性能调优项，有的是来⾃官⽅的，有的是来⾃别的的⼯程师，有的则是我⾃⼰总 结的。

基本概念和原则 ⾸先，要搞清楚Spark的⼏个基本概念和原则，否则系统的性能调优⽆从谈起：

每⼀台host上⾯可以并⾏N个worker，每⼀个worker下⾯可以并⾏M个executor，task们会被分配 到executor上⾯去执⾏。Stage指的是⼀组并⾏运⾏的task，stage内部是不能出现shufle的，因为 shufle的就像篱笆⼀样阻⽌了并⾏task的运⾏，遇到shufle就意味着到了stage的边界。

CPU的core数量，每个executor可以占⽤⼀个或多个core，可以通过观察CPU的使⽤率变化来了解 计算资源的使⽤情况，例如，很常⻅的⼀种浪费是⼀个executor占⽤了多个core，但是总的CPU使 ⽤率却不⾼（因为⼀个executor并不总能充分利⽤多核的能⼒），这个时候可以考虑让么个 executor占⽤更少的core，同时worker下⾯增加更多的executor，或者⼀台host上⾯增加更多的 worker来增加并⾏执⾏的executor的数量，从⽽增加CPU利⽤率。但是增加executor的时候需要考 虑好内存消耗的控制，以免出现Out of Memory的情况。

partition和paralelism，partition指的就是数据分⽚的数量，每⼀次task只能处理⼀个partition的数 据，这个值太⼩了会导致每⽚数据量太⼤，导致内存压⼒，或者诸多executor的计算能⼒⽆法利⽤ 充分；但是如果太⼤了则会导致分⽚太多，执⾏效率降低。在执⾏action类型操作的时候（⽐如各 种reduce操作），partition的数量会选择parent RD中最⼤的那⼀个。⽽paralelism则指的是在 RD进⾏reduce类操作的时候，默认返回数据的paritition数量（⽽在进⾏map类操作的时候， partition数量通常取⾃parent RD中较⼤的⼀个，⽽且也不会涉及shufle，因此这个paralelism的 参数没有影响）。所以说，这两个概念密切相关，都是涉及到数据分⽚的，作⽤⽅式其实是统⼀ 的。通过spark.default.paralelism可以设置默认的分⽚数量，⽽很多RD的操作都可以指定⼀个 partition参数来显式控制具体的分⽚数量。

上⾯这两条原理上看起来很简单，但是却⾮常重要，根据硬件和任务的情况选择不同的取值。想要 取⼀个放之四海⽽皆准的配置是不现实的。看这样⼏个例⼦：（1）实践中跑的EMR Spark job，有 的特别慢，查看CPU利⽤率很低，我们就尝试减少每个executor占⽤CPU core的数量，增加并⾏的 executor数量，同时配合增加分⽚，整体上增加了CPU的利⽤率，加快数据处理速度。（2）发现 某job很容易发⽣内存溢出，我们就增⼤分⽚数量，从⽽减少了每⽚数据的规模，同时还减少并⾏的 executor数量，这样相同的内存资源分配给数量更少的executor，相当于增加了每个task的内存分 配，这样运⾏速度可能慢了些，但是总⽐ OM强。（3）数据量特别少，有⼤量的⼩⽂件⽣成，就 减少⽂件分⽚，没必要创建那么多task，这种情况，如果只是最原始的input⽐较⼩，⼀般都能被注 意到；但是，如果是在运算过程中，⽐如应⽤某个reduceBy或者某个filter以后，数据⼤量减少，这 种低效情况就很少被留意到。

最后再补充⼀点，随着参数和配置的变化，性能的瓶颈是变化的，在分析问题的时候不要忘记。例 如在每台机器上部署的executor数量增加的时候，性能⼀开始是增加的，同时也观察到CPU的平均 使⽤率在增加；但是随着单台机器上的executor越来越多，性能下降了，因为随着executor的数量 增加，被分配到每个executor的内存数量减⼩，在内存⾥直接操作的越来越少，spil over到磁盘上 的数据越来越多，⾃然性能就变差了。下⾯给这样⼀个直观的例⼦，当前总的cpu利⽤率并不⾼：

但是经过根据上述原则的的调整之后，可以显著发现cpu总利⽤率增加了：

其次，涉及性能调优我们经常要改配置，在Spark⾥⾯有三种常⻅的配置⽅式，虽然有些参数的配置是 可以互相替代，但是作为最佳实践，还是需要遵循不同的情形下使⽤不同的配置：

设置环境变量，这种⽅式主要⽤于和环境、硬件相关的配置；

命令⾏参数，这种⽅式主要⽤于不同次的运⾏会发⽣变化的参数，⽤双横线开头；

代码⾥⾯（⽐如Scala）显式设置（SparkConf对象），这种配置通常是aplication级别的配置，⼀ 般不改变。

举⼀个配置的具体例⼦。Node、worker和executor之间的⽐例调整。我们经常需要调整并⾏的 executor的数量，那么简单说有两种⽅式：

⼀个是调整并⾏的worker的数量，⽐如，SPARK_WORKER_INSTANCE可以设置每个node的worker 的数量，但是在改变这个参数的时候，⽐如改成2，⼀定要相应设置SPARK_WORKER_CORES的 值，让每个worker使⽤原有⼀半的core，这样才能让两个worker⼀同⼯作；

另⼀个是调整worker内executor的数量，我们是在YARN框架下采⽤这个调整来实现executor数量改 变的，⼀种典型办法是，⼀个host只跑⼀个worker，然后配置spark.executor.cores为host上CPU core的N分之⼀，同时也设置spark.executor.memory为host上分配给Spark计算内存的N分之⼀， 这样这个host上就能够启动N个executor。

有的配置在不同的MR框架/⼯具下是不⼀样的，⽐如YARN下有的参数的默认取值就不同，这点需要注 意。 明确这些基础的事情以后，再来⼀项⼀项看性能调优的要点。

内存

Memory Tuning，Java对象会占⽤原始数据2~5倍甚⾄更多的空间。最好的检测对象内存消耗的办法 就是创建RD，然后放到cache⾥⾯去，然后在UI上⾯看storage的变化；当然也可以使⽤ SizeEstimator来估算。使⽤-X:+UseCompresedOops选项可以压缩指针（8字节变成4字节）。在 调⽤colect等等API的时候也要⼩⼼⸺⼤块数据往内存拷⻉的时候⼼⾥要清楚。内存要留⼀些给操作 系统，⽐如20%，这⾥⾯也包括了OS的bufer cache，如果预留得太少了，会⻅到这样的错误：

[Bash shel] 纯⽂本查看复制代码

?

<table>
  <tr>
    <th>1</th>
    <th>Required executor memory (235520+23552 MB) is above the max threshold (241664 MB) of this cluster! Please increase the value of ‘yarn.scheduler.maximum-allocation-mb’.</th>
  </tr>
</table>


或者⼲脆就没有这样的错误，但是依然有因为内存不⾜导致的问题，有的会有警告，⽐如这个：

[Bash shel] 纯⽂本查看复制代码

?

<table>
  <tr>
    <th>1</th>
    <th>16/01/1323:54:48 WARN scheduler.TaskSchedulerImpl: Initial job has not accepted any resources; check your cluster UI to ensure that workers are registered and have sufficient memory</th>
  </tr>
</table>


Reduce Task的内存使⽤。在某些情况下reduce task特别消耗内存，⽐如当shufle出现的时候，⽐如 sortByKey、groupByKey、reduceByKey和join等，要在内存⾥⾯建⽴⼀个巨⼤的hash table。其中⼀ 个解决办法是增⼤level of paralelism，这样每个task的输⼊规模就相应减⼩。

注意原始input的⼤⼩，有很多操作始终都是需要某类全集数据在内存⾥⾯完成的，那么并⾮拼命增加 paralelism和partition的值就可以把内存占⽤减得⾮常⼩的。我们遇到过某些性能低下甚⾄ OM的问 题，是改变这两个参数所难以缓解的。但是可以通过增加每台机器的内存，或者增加机器的数量都可 以直接或间接增加内存总量来解决。

在选择EC2机器类型的时候，要明确瓶颈（可以借由测试来明确），⽐如我们遇到的情况就是使⽤r3.8 xlarge和c3.8 xlarge选择的问题，运算能⼒相当，前者⽐后者贵50%，但是内存是后者的5倍。

CPU

Level of Paralelism。指定它以后，在进⾏reduce类型操作的时候，默认partition的数量就被指定了。 这个参数在实际⼯程中通常是必不可少的，⼀般都要根据input和每个executor内存的⼤⼩来确定。设 置level of paralelism或者属性spark.default.paralelism来改变并⾏级别，通常来说，每⼀个CPU核可 以分配2~3个task。

CPU core的访问模式是共享还是独占。即CPU核是被同⼀host上的executor共享还是⽠分并独占。⽐ 如YARN环境，⼀台机器上共有32个CPU core的资源，同时部署了两个executor，总内存是50G，那 么⼀种⽅式是配置spark.executor.cores为16，spark.executor.memory为20G，这样由于内存的限 制，这台机器上会部署两个executor，每个都使⽤20G内存，并且各使⽤独占的16个CPU core资源； ⽽如果把spark.executor.cores配置为32，那么依然会部署两个executor，但是⼆者会共享这32个 core。根据我的测试，独占模式的性能要略好与共享模式。同时，独占模式也是Spark官⽅⽂档上推荐 的⽅式。

GC调优。打印GC信息：-verbose:gc -X:+PrintGCDetails -X:+PrintGCTimeStamps。默认60%的 executor内存可以被⽤来作为RD的缓存，因此只有40%的内存可以被⽤来作为对象创建的空间，这 ⼀点可以通过设置spark.storage.memoryFraction改变。如果有很多⼩对象创建，但是这些对象在不 完全GC的过程中就可以回收，那么增⼤Eden区会有⼀定帮助。如果有任务从HDFS拷⻉数据，内存消 耗有⼀个简单的估算公式⸺⽐如HDFS的block size是64MB，⼯作区内有4个task拷⻉数据，⽽解压 缩⼀个block要增⼤3倍⼤⼩，那么内存消耗就是：4*3*64MB。另外，⼯作中遇到过这样的⼀个问 题：GC默认情况下有⼀个限制，默认是GC时间不能超过2%的CPU时间，但是如果⼤量对象创建（在 Spark⾥很容易出现，代码模式就是⼀个RD转下⼀个RD），就会导致⼤量的GC时间，从⽽出现 OutOfMemoryEror: GC overhead limit exceded，可以通过设置-X:-UseGCOverheadLimit关掉 它。 序列化和传输 Data Serialization，默认使⽤的是Java Serialization，这个程序员最熟悉，但是性能、空间表现都⽐较 差。还有⼀个选项是Kryo Serialization，更快，压缩率也更⾼，但是并⾮⽀持任意类的序列化。在 Spark UI上能够看到序列化占⽤总时间开销的⽐例，如果这个⽐例⾼的话可以考虑优化内存使⽤和序列 化。

Broadcasting Large Variables。在task使⽤静态⼤对象的时候，可以把它broadcast出去。Spark会打 印序列化后的⼤⼩，通常来说如果它超过20KB就值得这么做。有⼀种常⻅情形是，⼀个⼤表join⼀个 ⼩表，把⼩表broadcast后，⼤表的数据就不需要在各个node之间疯跑，安安静静地呆在本地等⼩表 broadcast过来就好了。

Data Locality。数据和代码要放到⼀起才能处理，通常代码总⽐数据要⼩⼀些，因此把代码送到各处会 更快。Data Locality是数据和处理的代码在屋⾥空间上接近的程度：PROCES_LOCAL（同⼀个 JVM）、NODE_LOCAL（同⼀个node，⽐如数据在HDFS上，但是和代码在同⼀个node）、 NO_PREF、RACK_LOCAL（不在同⼀个server，但在同⼀个机架）、ANY。当然优先级从⾼到低，但 是如果在空闲的executor上⾯没有未处理数据了，那么就有两个选择：（1）要么等如今繁忙的CPU闲 下来处理尽可能“本地”的数据，（1）要么就不等直接启动task去处理相对远程的数据。默认当这种情 况发⽣Spark会等⼀会⼉（spark.locality），即策略（1），如果繁忙的CPU停不下来，就会执⾏策略 （2）。

代码⾥对⼤对象的引⽤。在task⾥⾯引⽤⼤对象的时候要⼩⼼，因为它会随着task序列化到每个节点上 去，引发性能问题。只要序列化的过程不抛出异常，引⽤对象序列化的问题事实上很少被⼈重视。如 果，这个⼤对象确实是需要的，那么就不如⼲脆把它变成RD好了。绝⼤多数时候，对于⼤对象的序 列化⾏为，是不知不觉发⽣的，或者说是预期之外的，⽐如在我们的项⽬中有这样⼀段代码： [Bash shel] 纯⽂本查看复制代码

?

<table>
  <tr>
    <th>1<br>2<br></th>
    <th>rdd.map(r => {<br><br>println(BackfillTypeIndex) })<br><br></th>
  </tr>
</table>


3

其实呢，它等价于这样： [Bash shel] 纯⽂本查看复制代码

?

<table>
  <tr>
    <th>1<br>2<br></th>
    <th>rdd.map(r => {<br><br>println(this.BackfillTypeIndex) })<br><br></th>
  </tr>
</table>


3

对于这样的问题，⼀种最直接的解决⽅法就是： [Bash shel] 纯⽂本查看复制代码

?

<table>
  <tr>
    <th>1<br>2<br><br><br></th>
    <th>val dereferencedVariable = this.BackfillTypeIndex rdd.map(r => println(dereferencedVariable)) // "this" is not<br><br>serialized</th>
  </tr>
</table>


相关地，注解@transient⽤来标识某变量不要被序列化，这对于将⼤对象从序列化的陷阱中排除掉是 很有⽤的。另外，注意clas之间的继承层级关系，有时候⼀个⼩的case clas可能来⾃⼀棵⼤树。

⽂件读写 ⽂件存储和读取的优化。⽐如对于⼀些case⽽⾔，如果只需要某⼏列，使⽤rcfile和parquet这样的格 式会⼤⼤减少⽂件读取成本。再有就是存储⽂件到S3上或者HDFS上，可以根据情况选择更合适的格 式，⽐如压缩率更⾼的格式。另外，特别是对于shufle特别多的情况，考虑留下⼀定量的额外内存给 操作系统作为操作系统的bufer cache，⽐如总共50G的内存，JVM最多分配到40G多⼀点。

⽂件分⽚。⽐如在S3上⾯就⽀持⽂件以分⽚形式存放，后缀是part X。使⽤coalesce⽅法来设置分成 多少⽚，这个调整成并⾏级别或者其整数倍可以提⾼读写性能。但是太⾼太低都不好，太低了没法充 分利⽤S3并⾏读写的能⼒，太⾼了则是⼩⽂件太多，预处理、合并、连接建⽴等等都是时间开销啊， 读写还容易超过throtle。

任务 Spark的Speculation。通过设置spark.speculation等⼏个相关选项，可以让Spark在发现某些task执⾏ 特别慢的时候，可以在不等待完成的情况下被重新执⾏，最后相同的task只要有⼀个执⾏完了，那么 最快执⾏完的那个结果就会被采纳。

减少Shufle。其实Spark的计算往往很快，但是⼤量开销都花在⽹络和IO上⾯，⽽shufle就是⼀个典 型。举个例⼦，如果(k, v1) join (k, v2) => (k, v3)，那么，这种情况其实Spark是优化得⾮常好的，因 为需要join的都在⼀个node的⼀个partition⾥⾯，join很快完成，结果也是在同⼀个node（这⼀系列操 作可以被放在同⼀个stage⾥⾯）。但是如果数据结构被设计为(obj1) join (obj2) => (obj3)，⽽其中的 join条件为obj1.column1 = obj2.column1，这个时候往往就被迫shufle了，因为不再有同⼀个key使得 数据在同⼀个node上的强保证。在⼀定要shufle的情况下，尽可能减少shufle前的数据规模，⽐如这 个避免groupByKey的例⼦。下⾯这个⽐较的图⽚来⾃Spark Sumit 2013的⼀个演讲，讲的是同⼀件 事情：

![image 1](<spark性能调优总结1.note_images/imageFile1.png>)

Repartition。运算过程中数据量时⼤时⼩，选择合适的partition数量关系重⼤，如果太多partition就导 致有很多⼩任务和空任务产⽣；如果太少则导致运算资源没法充分利⽤，必要时候可以使⽤repartition 来调整，不过它也不是没有代价的，其中⼀个最主要代价就是shufle。再有⼀个常⻅问题是数据⼤⼩ 差异太⼤，这种情况主要是数据的partition的key其实取值并不均匀造成的（默认使⽤ HashPartitioner），需要改进这⼀点，⽐如重写hash算法。测试的时候想知道partition的数量可以调 ⽤rd.partitions().size()获知。

Task时间分布。关注Spark UI，在Stage的详情⻚⾯上，可以看得到shufle写的总开销，GC时间，当 前⽅法栈，还有task的时间花费。如果你发现task的时间花费分布太散，就是说有的花费时间很⻓，有 的很短，这就说明计算分布不均，需要重新审视数据分⽚、key的hash、task内部的计算逻辑等等，瓶 颈出现在耗时⻓的task上⾯。

![image 2](<spark性能调优总结1.note_images/imageFile2.png>)

重⽤资源。有的资源申请开销巨⼤，⽽且往往相当有限，⽐如建⽴连接，可以考虑在partition建⽴的时 候就创建好（⽐如使⽤mapPartition⽅法），这样对于每个partition内的每个元素的操作，就只要重⽤ 这个连接就好了，不需要重新建⽴连接。

