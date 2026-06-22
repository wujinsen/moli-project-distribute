# Spark性能调优

发表于2015-07-08 13 37| 6465次阅读| 来源程序员电⼦刊| 1 条评论| 作者程序员电⼦刊

监控⼯具程序员Spark2015年3⽉A

摘要：通常我们对⼀个系统进⾏性能优化⽆怪乎两个步骤⸺性能监控和参数调整，本⽂主要分享的也 是这两⽅⾯内容。 通常我们对⼀个系统进⾏性能优化⽆怪乎两个步骤⸺性能监控和参数调整，本⽂主要分享的也是这两 ⽅⾯内容。 性能监控⼯具 【Spark监控⼯具】 Spark提供了⼀些基本的Web监控⻚⾯，对于⽇常监控⼗分有⽤。

- 1. Aplication Web UI （默认端⼝是4040，可以通过spark.ui.port修改）可获得这些信息：（1）stages

和tasks调度情况；（2）RD⼤⼩及内存使⽤；（3）系统环境信息；（4）正在执⾏的executor信 息。

- 2. history server 当Spark应⽤退出后，仍可以获得历史Spark应⽤的stages和tasks执⾏信息，便于分析程序不明原因挂 掉的情况。配置⽅法如下：

- （1）$SPARK_HOME/conf/spark-env.sh export SPARK_HISTORY_OPTS="-Dspark.history.retainedAplications=50 Dspark.history.fs.logDirectory=hdfs:/hadop 0 8020/directory" 说明：spark.history.retainedAplica-tions仅显示最近50个应⽤spark.history.fs.logDirectory：Spark History Server⻚⾯只展示该路径下的信息。
- （2）$SPARK_HOME/conf/spark-defaults.conf spark.eventLog.enabled true spark.eventLog.dir hdfs:/hadop 0 8020/directory #应⽤在运⾏过程中所有的信息均记录在该属性 指定的路径下


- 3. spark.eventLog.compres true

- （1）HistoryServer启动 $SPARK_HOMR/bin/start-histrory-server.sh
- （2）HistoryServer停⽌ $SPARK_HOMR/bin/stop-histrory-server.sh


- 4. ganglia 通过配置ganglia，可以分析集群的使⽤状况和资源瓶颈，但是默认情况下ganglia是未被打包的，需要 在mvn编译时添加-Pspark-ganglia-lgpl，并修改配置⽂件$SPARK_HOME/conf/metrics.properties。
- 5. Executor logs


htp:/master:4040

Standalone模式：$SPARK_HOME/logs YARN模式：在yarn-site.xml⽂件中配置了YARN⽇志的存放位置：yarn.nodemanager.log-dirs，或使 ⽤命令获取yarn logs -aplicationId。 【其他监控⼯具】

- 1. Nmon（ ） Nmon 输⼊：c：CPU n：⽹络 m：内存 d：磁盘
- 2. Jmeter（ apache.org/） 通常使⽤Jmeter做系统性能参数的实时展示，JMeter的安装⾮常简单，从官⽅⽹站上下载，解压之后 即可使⽤。运⾏命令在%JMETER_HOME%/bin下，对于 Windows ⽤户，直接使⽤jmeter.bat。 启动jmeter：创建测试计划，设置线程组设置循环次数。 添加监听器：jp@gc - PerfMon Metrics Colector。


htp:/ w.ibm.com/developerworks/aix/library/au-analyze_aix/

![image 1](<Spark性能调优.note_images/imageFile1.png>)

htp:/jmeter.

![image 2](<Spark性能调优.note_images/imageFile2.png>)

设置监听器：监听主机端⼝及监听内容，例如CPU。

![image 3](<Spark性能调优.note_images/imageFile3.png>)

启动监听：可以实时获得节点的CPU状态信息，从图4可看出CPU已出现瓶颈。

![image 4](<Spark性能调优.note_images/imageFile4.png>)

- 3. Jprofiler（ ） JProfiler是⼀个全功能的Java剖析⼯具（profiler），专⽤于分析J2SE和J2E应⽤程式。它把CPU、线 程和内存的剖析组合在⼀个强⼤的应⽤中。JProfiler的GUI可以更⽅便地找到性能瓶颈、抓住内存泄漏 （memory leaks），并解决多线程的问题。例如分析哪个对象占⽤的内存⽐较多；哪个⽅法占⽤较⼤ 的CPU资源等；我们通常使⽤Jprofiler来监控Spark应⽤在local模式下运⾏时的性能瓶颈和内存泄漏情 况。


htp:/ w.ej-technologies.com/products/jprofiler/overview.html

![image 5](<Spark性能调优.note_images/imageFile5.png>)

上述⼏个⼯具可以直接通过提供的链接了解详细的使⽤⽅法。 Spark调优 【Spark集群并⾏度】 在Spark集群环境下，只有⾜够⾼的并⾏度才能使系统资源得到充分的利⽤，可以通过修改sparkenv.sh来调整Executor的数量和使⽤资源，Standalone和YARN⽅式资源的调度管理是不同的。 在Standalone模式下:

- 1. 每个节点使⽤的最⼤内存数：SPARK_WORKER_INSTANCES*SPARK_WORKER_MEMORY；
- 2. 每个节点的最⼤并发task数：SPARK_WORKER_INSTANCES*SPARK_WORKER_CORES。 在YARN模式下：


- 1. 集群task并⾏度：SPARK_ EXECUTOR_INSTANCES* SPARK_EXECUTOR_CORES；
- 2. 集群内存总量：(executor个数) * (SPARK_EXECUTOR_MEMORY+ spark.yarn.executor.memoryOverhead)+ (SPARK_DRIVER_MEMORY+spark.yarn.driver.memoryOverhead)。 重点强调：Spark对Executor和Driver额外添加堆内存⼤⼩，Executor端：由 spark.yarn.executor.memoryOverhead设置，默认值executorMemory * 0.07与384的最⼤值。Driver 端：由spark.yarn.driver.memoryOverhead设置，默认值driverMemory * 0.07与384的最⼤值。 通过调整上述参数，可以提⾼集群并⾏度，让系统同时执⾏的任务更多，那么对于相同的任务，并⾏ 度⾼了，可以减少轮询次数。举例说明：如果⼀个stage有10task，并⾏度为50，那么执⾏完这次任 务，需要轮询两次才能完成，如果并⾏度为10，那么⼀次就可以了。 但是在资源相同的情况，并⾏度⾼了，相应的Executor内存就会减少，所以需要根据实际实况协调内 存和core。此外，Spark能够⾮常有效的⽀持短时间任务（例如：20ms），因为会对所有的任务复⽤ JVM，这样能减⼩任务启动的消耗，Standalone模式下，core可以允许1-2倍于物理core的数量进⾏超 配。 【Spark任务数量调整】


Spark的任务数由stage中的起始的所有RD的partition之和数量决定，所以需要了解每个RD的 partition的计算⽅法。以Spark应⽤从HDFS读取数据为例，HadopRD的partition切分⽅法完全继承 于MapReduce中的FileInputFormat，具体的partition数量由HDFS的块⼤⼩、mapred.min.split.size的 ⼤⼩、⽂件的压缩⽅式等多个因素决定，详情需要参⻅FileInputFormat的代码。 【Spark内存调优】 内存优化有三个⽅⾯的考虑：对象所占⽤的内存，访问对象的消耗以及垃圾回收所占⽤的开销。

- 1. 对象所占内存，优化数据结构 Spark 默认使⽤Java序列化对象，虽然Java对象的访问速度更快，但其占⽤的空间通常⽐其内部的属 性数据⼤2-5倍。为了减少内存的使⽤，减少Java序列化后的额外开销，下⾯列举⼀些Spar k官⽹ （ ）提供的⽅法。

- （1）使⽤对象数组以及原始类型（primitive type）数组以替代Java或者Scala集合类（colection clas)。fastutil 库为原始数据类型提供了⾮常⽅便的集合类，且兼容Java标准类库。
- （2）尽可能地避免采⽤含有指针的嵌套数据结构来保存⼩对象。
- （3）考虑采⽤数字ID或者枚举类型以便替代String类型的主键。
- （4）如果内存少于32GB，设置JVM参数-X:+UseCom presedOops以便将8字节指针修改成4字 节。与此同时，在Java 7或者更⾼版本，设置JVM参数-X:+UseC ompresedStrings以便采⽤8⽐特 来编码每⼀个ASCI字符。


- 2. 内存回收

- （1）获取内存统计信息：优化内存前需要了解集群的内存回收频率、内存回收耗费时间等信息，可以 在spark-env.sh中设置SPARK_JAVA_OPTS=“-verbose:gc -X:+PrintGCDetails -

X:+PrintGCTimeStamps $ SPARK_JAVA_OPTS”来获取每⼀次内存回收的信息。

- （2）优化缓存⼤⼩：默认情况Spark采⽤运⾏内存（spark.executor.memory）的60%来进⾏RD缓 存。这表明在任务执⾏期间，有40%的内存可以⽤来进⾏对象创建。如果任务运⾏速度变慢且JVM频 繁进⾏内存回收，或者内存空间不⾜，那么降低缓存⼤⼩设置可以减少内存消耗，可以降低 spark.storage.memoryFraction的⼤⼩。


- 3. 频繁GC或者 OM 针对这种情况，⾸先要确定现象是发⽣在Driver端还是在Executor端，然后在分别处理。 Driver端：通常由于计算过⼤的结果集被回收到Driver端导致，需要调⼤Driver端的内存解决，或者进 ⼀步减少结果集的数量。 Executor端：


htp:/spark.apache.org/docs/latest/tuning.html#tuning-data-structures

- （1）以外部数据作为输⼊的Stage：这类Stage中出现GC通常是因为在Map侧进⾏map-side-combine 时，由于group过多引起的。解决⽅法可以增加partition的数量（即task的数量）来减少每个task要处 理的数据，来减少GC的可能性。


- （2）以shufle作为输⼊的Stage：这类Stage中出现GC的通常原因也是和shufle有关，常⻅原因是某 ⼀个或多个group的数据过多，也就是所谓的数据倾斜，最简单的办法就是增加shufle的task数量，⽐ 如在SparkSQL中设置SET spark.sql.shufle.partitions=40，如果调⼤shufle的task⽆法解决问题， 说明你的数据倾斜很严重，某⼀个group的数据远远⼤于其他的group，需要你在业务逻辑上进⾏调 整，预先针对较⼤的group做单独处理。 【修改序列化】 使⽤Kryo序列化，因为Kryo序列化结果⽐Java标准序列化更⼩，更快速。具体⽅法：sparkdefault.conf ⾥设置spark.serializer为org.apache.spark.serializer.KryoSerializer 。 参考官⽅⽂档（ ）：对于⼤多数程序⽽ ⾔，采⽤Kryo框架以及序列化能够解决性能相关的⼤部分问题。 【Spark 磁盘调优】 在集群环境下，如果数据分布不均匀，造成节点间任务分布不均匀，也会导致节点间源数据不必要的 ⽹络传输，从⽽⼤⼤影响系统性能，那么对于磁盘调优最好先将数据资源分布均匀。除此之外，还可 以对源数据做⼀定的处理：


htp:/spark.apache.org/docs/latest/tuning.html#sumary

- 1. 在内存允许范围内，将频繁访问的⽂件或数据置于内存中；
- 2. 如果磁盘充裕，可以适当增加源数据在HDFS上的备份数以减少⽹络传输；
- 3. Spark⽀持多种⽂件格式及压缩⽅式，根据不同的应⽤环境进⾏合理的选择。如果每次计算只需要其 中的某⼏列，可以使⽤列式⽂件格式，以减少磁盘I/O，常⽤的列式有parquet、rcfile。如果⽂件过 ⼤，将原⽂件压缩可以减少磁盘I/O，例如：gzip、snapy、lzo。 【其他】 ⼴播变量（broadcast） 当task中需要访问⼀个Driver端较⼤的数据时，可以通过使⽤SparkContext的⼴播变量来减⼩每⼀个任 务的⼤⼩以及在集群中启动作业的消耗。参考官⽅⽂档


htp:/spark.apache.org/docs/latest/tuning.ht ml#broadcasting-large-variables

。

开启推测机制 推测机制后，如果集群中，某⼀台机器的⼏个task特别慢，推测机制会将任务分配到其他机器执⾏， 最后Spark会选取最快的作为最终结果。 在spark-default.conf 中添加：spark.speculation true 推测机制与以下⼏个参数有关：

- 1. spark.speculation.interval 10：检测周期，单位毫秒；
- 2. spark.speculation.quantile 0.75：完成task的百分⽐时启动推测；
- 3. spark.speculation.multiplier 1.5：⽐其他的慢多少倍时启动推测。 总结 Spark系统的性能调优是⼀个很复杂的过程，需要对Spark以及Hadop有⾜够的知识储备。从业务应⽤ 平台（Spark）、存储（HDFS）、操作系统、硬件等多个层⾯都会对性能产⽣很⼤的影响。借助于多 种性能监控⼯具，我们可以很好地了解系统的性能表现，并根据上⾯介绍的经验进⾏调整。


作者简介：⽥毅，亚信科技⼤数据平台部⻔研发经理，Spark Contributor，北京Spark Metup发起 ⼈，主要关注SparkSQL与Spark Streaming。

![image 6](<Spark性能调优.note_images/imageFile6.png>)

本⽂选⾃程序员电⼦版2015年3⽉A刊，该期更多⽂章请查看这⾥。2 0年创刊⾄今所有⽂章⽬录请 查看程序员封⾯秀。欢迎订阅程序员电⼦版（含iPad版、Android版、PDF版）。 本⽂为《程序员》电⼦刊原创⽂章，未经允许不得转载，如需转载请联系market#csdn.net(#换成@)

