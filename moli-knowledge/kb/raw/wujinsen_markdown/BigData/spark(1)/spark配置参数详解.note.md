以下是整理的Spark中的⼀些配置参数，官⽅⽂档请参考 。 Spark提供三个位置⽤来配置系统：

Spark Configuration

Spark属性：控制⼤部分的应⽤程序参数，可以⽤SparkConf对象或者Java系统属性设置

环境变量：可以通过每个节点的 conf/spark-env.sh 脚本设置。例如IP地址、端⼝等信息

⽇志配置：可以通过log4j.properties配置

# Spark属性

Spark属性控制⼤部分的应⽤程序设置，并且为每个应⽤程序分别配置它。这些属性可以直接在

Spark Conf

上配置，然后传递给 SparkContext 。 SparkConf 允许你配置⼀些通⽤的属性（如master URL、应⽤程序名称等等）以及通过 set() ⽅法设置的任意键值对。例如，我们可以⽤如下⽅式创建 ⼀个拥有两个线程的应⽤程序。

val conf

1

=newSparkConf().setMaster("local[2]").setAppName("CountingSheep").set("spark.exe cutor.memory","1g")val sc =newSparkContext(conf)

## 动态加载Spark属性

在⼀些情况下，你可能想在 SparkConf 中避免硬编码确定的配置。例如，你想⽤不同的master或者不 同的内存数运⾏相同的应⽤程序。Spark允许你简单地创建⼀个空conf。

1 val sc =newSparkContext(newSparkConf())

然后你在运⾏时设置变量：

./bin/spark-submit --name "My app" --master local[4] --conf spark.shuffle.spill=false

- 1

--conf "spark.executor.extraJavaOptions=-XX:+PrintGCDetails XX:+PrintGCTimeStamps" myApp.jar

- 2

- 3


Spark shel和 spark-submit ⼯具⽀持两种⽅式动态加载配置。第⼀种⽅式是命令⾏选项，例如 -master ，如上⾯shel显示的那样。 spark-submit 可以接受任何Spark属性，⽤ --conf 参数表示。 但是那些参与Spark应⽤程序启动的属性要⽤特定的参数表示。运⾏ ./bin/spark-submit --help 将 会显示选项的整个列表。

bin/spark-submit 也会从 conf/spark-defaults.conf 中读取配置选项，这个配置⽂件中，每⼀⾏ 都包含⼀对以 空格 或者 等号 分开的键和值。例如：

- 1 spark.master spark://5.6.7.8:7077

- 2 spark.executor.memory 512m

- 3 spark.eventLog.enabled true

- 4 spark.serializer org.apache.spark.serializer.KryoSerializer

- 5


任何标签指定的值或者在配置⽂件中的值将会传递给应⽤程序，并且通过 SparkConf 合并这些值。 在 SparkConf 上设置的属性具有最⾼的优先级，其次是传递给 spark-submit 或者 spark-shell 的 属性值，最后是 spark-defaults.conf ⽂件中的属性值。 优先级顺序：

- 1 SparkConf > CLI > spark-defaults.conf

- 2


查看Spark属性

在 http://<driver>:4040 上的应⽤程序Web UI在 Environment 标签中列出了所有的Spark属性。这 对你确保设置的属性的正确性是很有⽤的。 注意： 只有通过spark-defaults.conf, SparkConf以及命令⾏直接指定的值才会显示 。对于其它的配置属 性，你可以认为程序⽤到了默认的值。

可⽤的属性

控制内部设置的⼤部分属性都有合理的默认值，⼀些最通⽤的选项设置如下：

应⽤程序属性

<table>
  <tr>
    <th>属性名称</th>
    <th>默认值</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>spark.ap.name</td>
    <td>(none)</td>
    <td>你的应⽤程序的名字。这将在UI 和⽇志数据中出现</td>
  </tr>
  <tr>
    <td>spark.driver.cores</td>
    <td>1</td>
    <td>driver程序运⾏需要的cpu内核 数</td>
  </tr>
  <tr>
    <td>spark.driver.maxResultSize</td>
    <td>1g</td>
    <td>每个Spark action(如colect)所 有分区的序列化结果的总⼤⼩限 制。设置的值应该不⼩于1m，0 代表没有限制。如果总⼤⼩超过 这个限制，程序将会终⽌。⼤的 限制值可能导致driver出现内存 溢出错误（依赖于<br><br>spark.driver.memory<br><br>中对象的内存消耗）。</td>
  </tr>
  <tr>
    <td>spark.driver.memory</td>
    <td>512m</td>
    <td>和JVM driver进程使⽤的内存数</td>
  </tr>
  <tr>
    <td>spark.executor.memory</td>
    <td>512m</td>
    <td>每个executor进程使⽤的内存 数。和JVM内存串拥有相同的格<br><br>）</td>
  </tr>
  <tr>
    <td>spark.extraListeners</td>
    <td>(none)</td>
    <td>式（如512m,2g 注册监听器，需要实现</td>
  </tr>
  <tr>
    <td>spark.local.dir</td>
    <td>/tmp</td>
    <td>SparkListener Spark中暂存空间的使⽤⽬录。 在Spark1.0以及更⾼的版本中， 这个属性被 SPARK_LOCAL_DIRS(Standalo ne, Mesos)和 LOCAL_DIRS(YARN)环境变量覆 盖。</td>
  </tr>
  <tr>
    <td>spark.logConf</td>
    <td>false</td>
    <td>当SparkContext启动时，将有效 。</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td>的SparkConf记录为INFO 集群管理器连接的地⽅</td>
  </tr>
</table>


spark.master (none)

### 运⾏环境

<table>
  <tr>
    <th>属性名称</th>
    <th>默认值</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>spark.driver.extraClasPath</td>
    <td>(none)</td>
    <td>附加到driver的claspath的额外 实体。</td>
  </tr>
  <tr>
    <td>spark.driver.extraJavaOptions</td>
    <td>(none)</td>
    <td>的claspath 传递给driver的JVM选项字符 串。例如GC设置或者其它⽇志 设置。注意，<br><br>在这个选项中设置Spark属性或者堆⼤⼩ 是不合法的<br><br>。Spark属性需要⽤<br><br>--driver-class-path<br><br>设置。</td>
  </tr>
  <tr>
    <td>spark.driver.extraLibraryPath</td>
    <td>(none)</td>
    <td>指定启动driver的JVM时⽤到的 库路径</td>
  </tr>
  <tr>
    <td>spark.driver.userClasPathFirst</td>
    <td>false</td>
    <td>(实验性)当在driver中加载类 时，是否⽤户添加的jar⽐Spark ⾃⼰的jar优先级⾼。这个属性可 以降低Spark依赖和⽤户依赖的 冲突。它现在还是⼀个实验性的 特征。</td>
  </tr>
  <tr>
    <td>spark.executor.extraClasPath</td>
    <td>(none)</td>
    <td>附加到executors的claspath的 额外的claspath实体。这个设 置存在的主要⽬的是Spark与旧 版本的向后兼容问题。⽤户⼀般 不⽤设置这个选项</td>
  </tr>
  <tr>
    <td>spark.executor.extraJavaOptio ns</td>
    <td>(none)<br><br></td>
    <td>传递给executors的JVM选项字 符串。例如GC设置或者其它⽇ 志设置。注意，<br><br>在这个选项中设置Spark属性或者堆⼤⼩ 是不合法的<br><br>。Spark属性需要⽤SparkConf 对象或者<br><br>spark-submit<br><br>脚本⽤到的<br><br>spark-defaults.conf<br><br>⽂件设置。堆内存可以通过<br><br>spark.executor.memory<br><br>设置</td>
  </tr>
  <tr>
    <td>spark.executor.extraLibraryPat h</td>
    <td>(none)</td>
    <td>指定启动executor的JVM时⽤到 的库路径</td>
  </tr>
  <tr>
    <td>spark.executor.logs.roling.max RetainedFiles</td>
    <td>(none)</td>
    <td>设置被系统保留的最近滚动⽇志 ⽂件的数量。更⽼的⽇志⽂件将 被删除。默认没有开启。</td>
  </tr>
</table>


<table>
  <tr>
    <th>sprk.executor.logs.roling.size<br><br>.maxBytes</th>
    <th>(none)</th>
    <th>executor⽇志的最⼤滚动⼤⼩。 默认情况下没有开启。值设置为 字节</th>
  </tr>
  <tr>
    <td>spark.executor.logs.roling.stra tegy</td>
    <td>(none)<br><br></td>
    <td>设置executor⽇志的滚动 (roling)策略。默认情况下没有 开启。可以配置为<br><br>time<br><br>和<br><br>size<br><br>。对于<br><br>time<br><br>，⽤<br><br>spark.executor.logs.rolling.ti me.interval<br><br>设置滚动间隔；对于<br><br>size<br><br>，⽤<br><br>spark.executor.logs.rolling.si ze.maxBytes<br><br>设置最⼤的滚动⼤⼩</td>
  </tr>
  <tr>
    <td>spark.executor.logs.roling.time<br><br>.interval</td>
    <td>daily</td>
    <td>executor⽇志滚动的时间间隔。 默认情况下没有开启。合法的值 是<br><br>daily ,hourly ,minutely 以及任意的秒。<br><br></td>
  </tr>
  <tr>
    <td>spark.files.userClasPathFirst</td>
    <td>false</td>
    <td>(实验性)当在Executors中加载类 时，是否⽤户添加的jar⽐Spark ⾃⼰的jar优先级⾼。这个属性可 以降低Spark依赖和⽤户依赖的 冲突。它现在还是⼀个实验性的 特征。</td>
  </tr>
  <tr>
    <td>spark.python.worker.memory</td>
    <td>512m</td>
    <td>在聚合期间，每个python worker进程使⽤的内存数。在聚 合期间，如果内存超过了这个限 制，它将会将数据塞进磁盘中</td>
  </tr>
</table>


<table>
  <tr>
    <th>spark.python.profile</th>
    <th>false</th>
    <th>在Python worker中开启 profiling。通过<br><br>sc.show_profiles()<br><br>展示分析结果。或者在driver退 出前展示分析结果。可以通过<br><br>sc.dump_profiles(path)<br><br>将结果dump到磁盘中。如果⼀ 些分析结果已经⼿动展示，那么 在driver退出前，它们再不会⾃ 动展示</th>
  </tr>
  <tr>
    <td>spark.python.profile.dump</td>
    <td>(none)</td>
    <td>driver退出前保存分析结果的 dump⽂件的⽬录。每个RD都 会分别dump⼀个⽂件。可以通 过<br><br>ptats.Stats()<br><br>加载这些⽂件。如果指定了这个 属性，分析结果不会⾃动展示</td>
  </tr>
  <tr>
    <td>spark.python.worker.reuse</td>
    <td>true</td>
    <td>是否重⽤python worker。如果 是，它将使⽤固定数量的Python workers，⽽不需要为每个任务<br><br>fork()<br><br>⼀个Python进程。如果有⼀个⾮ 常⼤的⼴播，这个设置将⾮常有 ⽤。因为，⼴播不需要为每个任 务从JVM到Python worker传递 ⼀次</td>
  </tr>
  <tr>
    <td>spark.executorEnv. [EnvironmentVariableName]</td>
    <td>(none)</td>
    <td>通过<br><br>EnvironmentVariableName<br><br>添加指定的环境变量到executor 进程。⽤户可以指定多个<br><br>EnvironmentVariableName<br><br>，设置多个环境变量</td>
  </tr>
  <tr>
    <td>spark.mesos.executor.home</td>
    <td>driver side SPARK_HOME</td>
    <td>设置安装在Mesos的executor上 的Spark的⽬录。默认情况下， executors将使⽤driver的Spark 本地（home）⽬录，这个⽬录 对它们不可⻅。注意，如果没有 通过<br><br>spark.executor.uri<br><br>指定Spark的⼆进制包，这个设 置才起作⽤</td>
  </tr>
</table>


<table>
  <tr>
    <th>spark.mesos.executor.memory Overhead</th>
    <th>executor memory * 0.07, 最⼩ 384m<br><br></th>
    <th>这个值是<br><br>spark.executor.memory<br><br>的补充。它⽤来计算mesos任务 的总内存。另外，有⼀个7%的 硬编码设置。最后的值将选择<br><br>spark.mesos.executor.memoryOve rhead<br><br>或者<br><br>spark.executor.memory<br><br>⼆者之间的⼤者</th>
  </tr>
</table>


的7%

### Shufle⾏为

<table>
  <tr>
    <th>属性名称</th>
    <th>默认值</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>spark.reducer.maxMbInFlight</td>
    <td>48</td>
    <td>从递归任务中同时获取的map输 出数据的最⼤⼤⼩（mb）。因 为每⼀个输出都需要我们创建⼀ 个缓存⽤来接收，这个设置代表 每个任务固定的内存上限，所以 除⾮你有更⼤的内存，将其设置 ⼩⼀点</td>
  </tr>
  <tr>
    <td>spark.shufle.blockTransferSer vice</td>
    <td>nety</td>
    <td>实现⽤来在executor直接传递 shufle和缓存块。有两种可⽤的 实现：<br><br>netty<br><br>和<br><br>nio<br><br>。基于nety的块传递在具有相 同的效率情况下更简单</td>
  </tr>
  <tr>
    <td>spark.shufle.compres</td>
    <td>true</td>
    <td>是否压缩map操作的输出⽂件。 ⼀般情况下，这是⼀个好的选 择。</td>
  </tr>
  <tr>
    <td>spark.shufle.consolidateFiles</td>
    <td>false</td>
    <td>如果设置为"true"，在shufle期 间，合并的中间⽂件将会被创 建。创建更少的⽂件可以提供⽂ 件系统的shufle的效率。这些 shufle都伴随着⼤量递归任务。 当⽤ext4和dfs⽂件系统时，推 荐设置为"true"。在ext3中，因 为⽂件系统的限制，这个选项可<br><br>核）降低效率</td>
  </tr>
  <tr>
    <td>spark.shufle.file.bufer.kb</td>
    <td>32</td>
    <td>能机器（⼤于8<br><br>每个shufle⽂件输出流内存内缓 存的⼤⼩，单位是kb。这个缓存 减少了创建只中间shufle⽂件中 磁盘搜索和系统访问的数量</td>
  </tr>
  <tr>
    <td>spark.shufle.io.maxRetries</td>
    <td>3</td>
    <td>Nety only，⾃动重试次数</td>
  </tr>
  <tr>
    <td>spark.shufle.io.numConectio</td>
    <td>1</td>
    <td>Nety only</td>
  </tr>
  <tr>
    <td>nsPerPer spark.shufle.io.preferDirectBu</td>
    <td>true</td>
    <td>Nety only</td>
  </tr>
  <tr>
    <td>fs</td>
    <td> </td>
    <td> </td>
  </tr>
</table>


#### spark.shufle.io.retryWait 5 Nety only

<table>
  <tr>
    <th>spark.shufle.manager</th>
    <th>sort</th>
    <th>它的实现⽤于shufle数据。有两 种可⽤的实现：<br><br>sort<br><br>和<br><br>hash<br><br>。基于sort的shufle有更⾼的内 存使⽤率</th>
  </tr>
  <tr>
    <td>spark.shufle.memoryFraction</td>
    <td>0.2</td>
    <td>如果<br><br>spark.shuffle.spill<br><br>为true，shufle中聚合和合并组 操作使⽤的java堆内存占总内存 的⽐重。在任何时候，shufles 使⽤的所有内存内maps的集合 ⼤⼩都受这个限制的约束。超过 这个限制，spiling数据将会保存 到磁盘上。如果spiling太过频 繁，考虑增⼤这个值</td>
  </tr>
  <tr>
    <td>spark.shufle.sort.bypasMerg eThreshold</td>
    <td>20</td>
    <td>(Advanced) In the srt-based<br><br>hufle manager, avoid mergesortin data if there is no mapside agregation and there are at most this many reduce</td>
  </tr>
  <tr>
    <td>spark.shufle.spil</td>
    <td>true</td>
    <td>partitions 如果设置为"true"，通过将多出 的数据写⼊磁盘来限制内存数。 通过<br><br>spark.shuffle.memoryFraction<br><br>的阈值</td>
  </tr>
  <tr>
    <td>spark.shufle.spil.compres</td>
    <td>true</td>
    <td>来指定spiling 在shufle时，是否将spiling的 数据压缩。压缩算法通过<br><br>spark.io.compression.codec<br><br>指定。</td>
  </tr>
</table>


### Spark UI

<table>
  <tr>
    <th>属性名称</th>
    <th>默认值</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>spark.eventLog.compres</td>
    <td>false</td>
    <td>是否压缩事件⽇志。需要<br><br>spark.eventLog.enabled</td>
  </tr>
  <tr>
    <td>spark.eventLog.dir</td>
    <td>file:/tmp/spark-events</td>
    <td>为true Spark事件⽇志记录的基本⽬ 录。在这个基本⽬录下，Spark 为每个应⽤程序创建⼀个⼦⽬ 录。各个应⽤程序记录⽇志到直 到的⽬录。⽤户可能想设置这为 统⼀的地点，像HDFS⼀样，所 以历史⽂件可以通过历史服务器 读取</td>
  </tr>
  <tr>
    <td>spark.eventLog.enabled</td>
    <td>false</td>
    <td>是否记录Spark的事件⽇志。这 在应⽤程序完成后，重新构造<br><br>是有⽤的</td>
  </tr>
  <tr>
    <td>spark.ui.kilEnabled</td>
    <td>true</td>
    <td>web UI 运⾏在web UI中杀死stage和相</td>
  </tr>
  <tr>
    <td>spark.ui.port</td>
    <td>4040</td>
    <td>应的job 你的应⽤程序dashboard的端 ⼝。显示内存和⼯作量数据</td>
  </tr>
  <tr>
    <td>spark.ui.retainedJobs</td>
    <td>1 0</td>
    <td>在垃圾回收之前，Spark UI和状 数</td>
  </tr>
  <tr>
    <td>spark.ui.retainedStages</td>
    <td>1 0</td>
    <td>态API记住的job 在垃圾回收之前，Spark UI和状<br><br>数</td>
  </tr>
</table>


态API记住的stage

### 压缩和序列化

<table>
  <tr>
    <th>默认值</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>true</td>
    <td>在发送⼴播变量之前是否压缩它</td>
  </tr>
  <tr>
    <td>og.apache.sp ark.seriaier.J avaSerializer</td>
    <td>闭包⽤到的序列化类。⽬前只⽀持java序列化器</td>
  </tr>
</table>


压缩诸如RD分区、⼴播变量、shufle输出等内部数据的编码解码器。默认情况下， 择：lz4、lzf和snapy，你也可以⽤完整的类名来制定。

snapy

#### 32768 LZ4压缩中⽤到的块⼤⼩。降低这个块的⼤⼩也会降低shufle内存使⽤率

#### 32768 Snapy压缩中⽤到的块⼤⼩。降低这个块的⼤⼩也会降低shufle内存使⽤率

<table>
  <tr>
    <th>(none)</th>
    <th>如果你⽤Kryo序列化，给定的⽤逗号分隔的⾃定义类名列表表示要注册的类</th>
  </tr>
  <tr>
    <td>true</td>
    <td>当⽤Kryo序列化时，跟踪是否引⽤同⼀对象。如果你的对象图有环，这是必须的设置 象的多个副本，这个设置对效率是有⽤的。如果你知道不在这两个场景，那么可以禁</td>
  </tr>
</table>


<table>
  <tr>
    <th>false</th>
    <th>是否需要注册为Kyro可⽤。如果设置为true，然后如果⼀个没有注册的类序列化，Ky 置为false，Kryo将会同时写每个对象和其⾮注册类名。写类名可能造成显著地性能瓶</th>
  </tr>
  <tr>
    <td>(none)</td>
    <td>如果你⽤Kryo序列化，设置这个类去注册你的⾃定义类。如果你需要⽤⾃定义的⽅式 属性是有⽤的。否则<br><br>spark.kryo.classesToRegister<br><br>会更简单。它应该设置⼀个继承⾃<br><br>的类<br><br>KryoRegistrator</td>
  </tr>
</table>


#### 64 Kryo序列化缓存允许的最⼤值。这个值必须⼤于你尝试序列化的对象

<table>
  <tr>
    <th>0.064</th>
    <th>Kyro序列化缓存的⼤⼩。这样worker上的每个核都有⼀个缓存。如果有需要，缓存会<br><br>spark.kryoserializer.buffer.max.mb<br><br>设置的值那么⼤。</th>
  </tr>
  <tr>
    <td>true</td>
    <td>是否压缩序列化的RD分区。在花费⼀些额外的CPU时间的同时节省⼤量的空间</td>
  </tr>
</table>


<table>
  <tr>
    <th>og.apache.sp ark.seriaier.J avaSerializer</th>
    <th>序列化对象使⽤的类。默认的Java序列化类可以序列化任何可序列化的java对象但是它 ⽤ org.apache.spark.serializer.KryoSerializer</th>
  </tr>
  <tr>
    <td>10</td>
    <td>当⽤<br><br>org.apache.spark.serializer.JavaSerializer<br><br>序列化时，序列化器通过缓存对象防⽌写多余的数据，然⽽这会造成这些对象的垃圾 求'reset'，你从序列化器中flush这些信息并允许收集⽼的数据。为了关闭这个周期性 设为-1。默认情况下，每⼀百个对象reset⼀次</td>
  </tr>
</table>


### 运⾏时⾏为

<table>
  <tr>
    <th>属性名称</th>
    <th>默认值</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>spark.broadcast.blockSize</td>
    <td>4096</td>
    <td>TorentBroadcastFactory传输 的块⼤⼩，太⼤值会降低并发， 太⼩的值会出现性能瓶颈</td>
  </tr>
  <tr>
    <td>spark.broadcast.factory</td>
    <td>org.apache.spark.broadcast.To</td>
    <td>broadcast实现类</td>
  </tr>
  <tr>
    <td>spark.cleaner.tl</td>
    <td>rentBroadcastFactory<br><br>(infinite)</td>
    <td>spark记录任何元数据（stages ⽣成、task⽣成等）的持续时 间。定期清理可以确保将超期的 元数据丢弃，这在运⾏⻓时间任 务是很有⽤的，如运⾏7*24的 sparkstreaming任务。RD持久 化在内存中的超期数据也会被清 理</td>
  </tr>
  <tr>
    <td>spark.default.paralelism</td>
    <td>本地模式：机器核数；Mesos： 8；其他：<br><br>max(executor的core，2)</td>
    <td>如果⽤户不设置，系统使⽤集群 中运⾏shufle操作的默认任务数 （groupByKey、 reduceByKey 等）</td>
  </tr>
  <tr>
    <td>spark.executor.heartbeatInterv al</td>
    <td>1 0</td>
    <td>executor 向 the driver 汇报⼼跳 的时间间隔，单位毫秒</td>
  </tr>
  <tr>
    <td>spark.files.fetchTimeout</td>
    <td>60</td>
    <td>driver 程序获取通过<br><br>SparkContext.addFile()<br><br>添加的⽂件时的超时时间，单位 秒</td>
  </tr>
  <tr>
    <td>spark.files.useFetchCache</td>
    <td>true</td>
    <td>获取⽂件时是否使⽤本地缓存</td>
  </tr>
  <tr>
    <td>spark.files.overwrite</td>
    <td>false</td>
    <td>调⽤<br><br>SparkContext.addFile()<br><br>时候是否覆盖⽂件</td>
  </tr>
  <tr>
    <td>spark.hadop.cloneConf</td>
    <td>false</td>
    <td>每个task是否克隆⼀份hadop 的配置⽂件</td>
  </tr>
  <tr>
    <td>spark.hadop.validateOutputS</td>
    <td>true</td>
    <td>是否校验输出</td>
  </tr>
  <tr>
    <td>pecs<br><br>spark.storage.memoryFraction</td>
    <td>0.6</td>
    <td>Spark内存缓存的堆⼤⼩占⽤总 内存⽐例，该值不能⼤于⽼年代 内存⼤⼩，默认值为0.6，但 是，如果你⼿动设置⽼年代⼤ ⼩，你可以增加该值</td>
  </tr>
  <tr>
    <td>spark.storage.memoryMapThre</td>
    <td>2097152</td>
    <td>内存块⼤⼩</td>
  </tr>
</table>


#### shold

<table>
  <tr>
    <th>spark.storage.unrolFraction</th>
    <th>0.2</th>
    <th>Fraction of spark.storage.memoryFraction to use for unroling blocks in</th>
  </tr>
  <tr>
    <td>spark.tachyonStore.baseDir</td>
    <td>System.getProperty("java.io.tm</td>
    <td>memory. Tachyon File System临时⽬录</td>
  </tr>
  <tr>
    <td> </td>
    <td>pdir")</td>
    <td> </td>
  </tr>
</table>


spark.tachyonStore.url tachyon:/localhost:1 98 Tachyon File System URL

### ⽹络

<table>
  <tr>
    <th>属性名称</th>
    <th>默认值</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>spark.driver.host</td>
    <td>(local hostname)</td>
    <td>driver监听的主机名或者IP地 址。这⽤于和executors以及独<br><br>通信</td>
  </tr>
  <tr>
    <td>spark.driver.port</td>
    <td>(random)</td>
    <td>⽴的master driver监听的接⼝。这⽤于和 executors以及独⽴的master通 信</td>
  </tr>
  <tr>
    <td>spark.fileserver.port</td>
    <td>(random)</td>
    <td>driver的⽂件服务器监听的端⼝</td>
  </tr>
  <tr>
    <td>spark.broadcast.port</td>
    <td>(random)</td>
    <td>driver的HTP⼴播服务器监听的 端⼝</td>
  </tr>
  <tr>
    <td>spark.replClasServer.port</td>
    <td>(random)</td>
    <td>driver的HTP类服务器监听的端 ⼝</td>
  </tr>
  <tr>
    <td>spark.blockManager.port</td>
    <td>(random)</td>
    <td>块管理器监听的端⼝。这些同时</td>
  </tr>
  <tr>
    <td>spark.executor.port</td>
    <td>(random)</td>
    <td>存在于driver和executors executor监听的端⼝。⽤于与<br><br>通信</td>
  </tr>
  <tr>
    <td>spark.port.maxRetries</td>
    <td>16</td>
    <td>driver 当绑定到⼀个端⼝，在放弃前重 试的最⼤次数</td>
  </tr>
  <tr>
    <td>spark.aka.frameSize</td>
    <td>10</td>
    <td>在"control plane"通信中允许的 最⼤消息⼤⼩。如果你的任务需 要发送⼤的结果到driver中，调 ⼤这个值</td>
  </tr>
  <tr>
    <td>spark.aka.threads</td>
    <td>4</td>
    <td>通信的actor线程数。当driver有 核时，调⼤它是有⽤的</td>
  </tr>
  <tr>
    <td>spark.aka.timeout</td>
    <td>10</td>
    <td>很多CPU Spark节点之间的通信超时。单 位是秒</td>
  </tr>
  <tr>
    <td>spark.aka.heartbeat.pauses</td>
    <td>6 0</td>
    <td>This is set to a larger value to disable failure detector that comes inbuilt aka. It can be enabled again, if you plan to use this feature (Not recomended). Aceptable heart beat pause in seconds for aka. This can be used to control sensitivity to gc pauses. Tune this in<br><br>cospark.akka.heartbeat.intervambination of l<br><br>aspark.akka.failure-nd detector.threshold<br><br></td>
  </tr>
</table>


#### if you ned to.

<table>
  <tr>
    <th>spark.aka.failuredetector.threshold</th>
    <th>30.0<br><br></th>
    <th>This is set to a larger value to disable failure detector that comes inbuilt aka. It can be enabled again, if you plan to use this feature (Not recomended). This maps to<br><br>akakka.remote.transport-failure-a's detector.threshold<br><br>. Tspark.akka.heartbeat.pausesune this in combination of<br><br>aspark.akka.heartbeat.intervand l<br><br></th>
  </tr>
  <tr>
    <td>spark.aka.heartbeat.interval</td>
    <td>1 0<br><br></td>
    <td>if you ned to.<br><br>This is set to a larger value to disable failure detector that comes inbuilt aka. It can be enabled again, if you plan to use this feature (Not recomended). A larger interval value in seconds reduces network overhead and a smaler value ( ~ 1 s) might be more informative for aka's failure detector. Tune this in<br><br>cospark.akka.heartbeat.pausesmbination of<br><br>aspark.akka.failure-nd detector.threshold<br><br>if you ned to. Only positive use case for using failure detector can be, a sensistive failure detector can help evict rogue executors realy quick. However this is usualy not the case as gc pauses and network lags are expected in a real Spark cluster. Apart from that enabli this leads to a lot of exchanges of heart beats betwen nodes leading to fl oding the network with</td>
  </tr>
</table>


those.

### 调度相关属性

<table>
  <tr>
    <th>属性名称</th>
    <th>默认值</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>spark.task.cpus</td>
    <td>1</td>
    <td>为每个任务分配的内核数</td>
  </tr>
  <tr>
    <td>spark.task.maxFailures</td>
    <td>4</td>
    <td>Task的最⼤重试次数</td>
  </tr>
  <tr>
    <td>spark.scheduler.mode</td>
    <td>FIFO</td>
    <td>Spark的任务调度模式，还有⼀ 模式</td>
  </tr>
  <tr>
    <td>spark.cores.max</td>
    <td> </td>
    <td>种Fair<br><br>当应⽤程序运⾏在Standalone集 群或者粗粒度共享模式Mesos集 群时，应⽤程序向集群请求的最 ⼤CPU内核总数（不是指每台机 器，⽽是整个集群）。如果不设 置，对于Standalone集群将使⽤ spark.deploy.defaultCores中数 值，⽽Mesos将使⽤集群中可⽤ 的内核</td>
  </tr>
  <tr>
    <td>spark.mesos.coarse</td>
    <td>False</td>
    <td>如果设置为true，在Mesos集群 中运⾏时使⽤粗粒度共享模式</td>
  </tr>
  <tr>
    <td>spark.speculation</td>
    <td>False</td>
    <td>以下⼏个参数是关于Spark推测 执⾏机制的相关参数。此参数设 定是否使⽤推测执⾏机制，如果 设置为true则spark使⽤推测执 ⾏机制，对于Stage中拖后腿的 Task在其他节点中重新启动，并 将最先完成的Task的计算结果最 为最终结果</td>
  </tr>
  <tr>
    <td>spark.speculation.interval</td>
    <td>10</td>
    <td>Spark多⻓时间进⾏检查task运 ⾏状态⽤以推测，以毫秒为单位</td>
  </tr>
  <tr>
    <td>spark.speculation.quantile</td>
    <td> </td>
    <td>推测启动前，Stage必须要完成 的百分⽐</td>
  </tr>
  <tr>
    <td>spark.speculation.multiplier</td>
    <td>1.5</td>
    <td>总Task ⽐已完成Task的运⾏速度中位数 慢多少倍才启⽤推测</td>
  </tr>
</table>


<table>
  <tr>
    <th>spark.locality.wait</th>
    <th>3 0</th>
    <th>以下⼏个参数是关于Spark数据 本地性的。本参数是以毫秒为单 位启动本地数据task的等待时 间，如果超出就启动下⼀本地优 先级别的task。该设置同样可以 应⽤到各优先级别的本地性之间 （本地进程 -> 本地节点 -> 本地 机架 -> 任意节点 ），当然，也 可以通过 spark.locality.wait.node等参数 设置不同优先级别的本地性</th>
  </tr>
  <tr>
    <td>spark.locality.wait.proces</td>
    <td>spark.locality.wait</td>
    <td>本地进程级别的本地等待时间</td>
  </tr>
  <tr>
    <td>spark.locality.wait.node</td>
    <td>spark.locality.wait</td>
    <td>本地节点级别的本地等待时间</td>
  </tr>
  <tr>
    <td>spark.locality.wait.rack</td>
    <td>spark.locality.wait</td>
    <td>本地机架级别的本地等待时间</td>
  </tr>
  <tr>
    <td>spark.scheduler.revive.interval</td>
    <td>1 0</td>
    <td>复活重新获取资源的Task的最⻓ 时间间隔（毫秒），发⽣在Task 因为本地资源不⾜⽽将资源分配 给其他Task运⾏后进⼊等待时 间，如果这个等待时间内重新获 取⾜够的资源就继续计算</td>
  </tr>
</table>


### Dynamic Alocation

<table>
  <tr>
    <th>属性名称</th>
    <th>默认值</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>spark.dynamicAlocation.enabl</td>
    <td>false</td>
    <td>是否开启动态资源搜集</td>
  </tr>
  <tr>
    <td>ed spark.dynamicAlocation.execu</td>
    <td>60</td>
    <td> </td>
  </tr>
  <tr>
    <td>torIdleTimeout spark.dynamicAlocation.initial</td>
    <td>spark.dynamicAlocation.minEx</td>
    <td> </td>
  </tr>
  <tr>
    <td>Executors spark.dynamicAlocation.maxE</td>
    <td>ecutors Integer.MAX_VALUE</td>
    <td> </td>
  </tr>
  <tr>
    <td>xecutors spark.dynamicAlocation.minEx</td>
    <td>0</td>
    <td> </td>
  </tr>
  <tr>
    <td>ecutors spark.dynamicAlocation.sched</td>
    <td>5</td>
    <td> </td>
  </tr>
  <tr>
    <td>ulerBacklogTimeout spark.dynamicAlocation.sustai</td>
    <td>schedulerBacklogTimeout</td>
    <td> </td>
  </tr>
</table>


nedSchedulerBacklogTimeout

### 安全

<table>
  <tr>
    <th>属性名称</th>
    <th>默认值</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>spark.authenticate</td>
    <td>false</td>
    <td>是否Spark验证其内部连接。如 果不是运⾏在YARN上，请看<br><br>spark.authenticate.secret</td>
  </tr>
  <tr>
    <td>spark.authenticate.secret</td>
    <td>None</td>
    <td>设置Spark两个组件之间的密匙 验证。如果不是运⾏在YARN 上，但是需要验证，这个选项必 须设置</td>
  </tr>
  <tr>
    <td>spark.core.conection.auth.wai t.timeout</td>
    <td>30</td>
    <td>连接时等待验证的实际。单位为 秒</td>
  </tr>
  <tr>
    <td>spark.core.conection.ack.wait<br><br>.timeout</td>
    <td>60</td>
    <td>连接等待回答的时间。单位为 秒。为了避免不希望的超时，你 可以设置更⼤的值</td>
  </tr>
  <tr>
    <td>spark.ui.filters</td>
    <td>None</td>
    <td>应⽤到Spark web UI的⽤于过滤 类名的逗号分隔的列表。过滤器 必须是标准的<br><br>。通过设置java系统属性也可以 指定每个过滤器的参数。<br><br>spark.<class name of filter>.params='param1=value1,p aram2=value2'<br><br>。例如<br><br>Dspark.ui.filters=com.test.fil ter1<br><br>、<br><br>Dspark.com.test.filter1.params= 'param1=foo,param2=testing'<br><br>javax servlet Filter</td>
  </tr>
  <tr>
    <td>spark.acls.enable</td>
    <td>false</td>
    <td>是否开启Spark acls。如果开启 了，它检查⽤户是否有权限去查 看或修改job。UI利⽤使⽤过滤 器验证和设置⽤户</td>
  </tr>
  <tr>
    <td>spark.ui.view.acls</td>
    <td>empty</td>
    <td>逗号分隔的⽤户列表，列表中的 ⽤户有查看Spark web UI的权 限。默认情况下，只有启动<br><br>的⽤户有查看权限</td>
  </tr>
  <tr>
    <td>spark.modify.acls</td>
    <td>empty</td>
    <td>Spark job 逗号分隔的⽤户列表，列表中的 ⽤户有修改Spark job的权限。 默认情况下，只有启动Spark<br><br>的⽤户有修改权限</td>
  </tr>
</table>


#### job

<table>
  <tr>
    <th>spark.admin.acls</th>
    <th>empty</th>
    <th>逗号分隔的⽤户或者管理员列 表，列表中的⽤户或管理员有查 看和修改所有Spark job的权 限。如果你运⾏在⼀个共享集 群，有⼀组管理员或开发者帮助<br><br>，这个选项有⽤</th>
  </tr>
</table>


debug

### 加密

<table>
  <tr>
    <th>属性名称</th>
    <th>默认值</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>spark.sl.enabled</td>
    <td>false</td>
    <td>是否开启 sl</td>
  </tr>
  <tr>
    <td>spark.sl.enabledAlgorithms</td>
    <td>Empty</td>
    <td>JVM⽀持的加密算法列表，逗号 分隔</td>
  </tr>
  <tr>
    <td>spark.sl.keyPasword</td>
    <td>None</td>
    <td> </td>
  </tr>
  <tr>
    <td>spark.sl.keyStore</td>
    <td>None</td>
    <td> </td>
  </tr>
  <tr>
    <td>spark.sl.keyStorePasword</td>
    <td>None</td>
    <td> </td>
  </tr>
  <tr>
    <td>spark.sl.protocol</td>
    <td>None</td>
    <td> </td>
  </tr>
  <tr>
    <td>spark.sl.trustStore</td>
    <td>None</td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


spark.sl.trustStorePasword None

### Spark Streaming

<table>
  <tr>
    <th>属性名称</th>
    <th>默认值</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>spark.streaming.blockInterval</td>
    <td>20</td>
    <td>在这个时间间隔（ms）内，通 过Spark Streaming receivers接 收的数据在保存到Spark之前， chunk为数据块。推荐的最⼩值</td>
  </tr>
  <tr>
    <td>spark.streaming.receiver.maxR ate</td>
    <td>infinite</td>
    <td>为50ms 每秒钟每个receiver将接收的数 据的最⼤记录数。有效的情况 下，每个流将消耗⾄少这个数⽬ 的记录。设置这个配置为0或<br><br>将会不作限制</td>
  </tr>
  <tr>
    <td>spark.streaming.receiver.write AheadLogs.enable</td>
    <td>false</td>
    <td>者-1 Enable write aead logs for reeiers. Al the input data received through receivers wil be saved to write ahead logs that wil alow it to be</td>
  </tr>
  <tr>
    <td>spark.streaming.unpersist</td>
    <td>true</td>
    <td>recovered after driver failures<br><br>强制通过Spark Streaming⽣成 并持久化的RD⾃动从Spark内 存中⾮持久化。通过Spark Streaming接收的原始输⼊数据 也将清除。设置这个属性为false 允许流应⽤程序访问原始数据和 持久化RD，因为它们没有被⾃ 动清除。但是它会造成更⾼的内 存花费</td>
  </tr>
</table>


### 集群管理

Spark On YARN

<table>
  <tr>
    <th>属性名称</th>
    <th>默认值</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>spark.yarn.am.memory</td>
    <td>512m</td>
    <td>client 模式时，am的内存⼤⼩； cluster模式时，使⽤<br><br>spark.driver.memory<br><br>变量</td>
  </tr>
  <tr>
    <td>spark.driver.cores</td>
    <td>1</td>
    <td>claster模式时，driver使⽤的 cpu核数，这时候driver运⾏在 am中，其实也就是am和核数； client模式时，使⽤<br><br>spark.yarn.am.cores<br><br>变量</td>
  </tr>
  <tr>
    <td>spark.yarn.am.cores</td>
    <td>1</td>
    <td>client 模式时，am的cpu核数</td>
  </tr>
  <tr>
    <td>spark.yarn.am.waitTime</td>
    <td>1 0</td>
    <td>启动时等待时间</td>
  </tr>
  <tr>
    <td>spark.yarn.submit.file.replicatio n</td>
    <td>3</td>
    <td>应⽤程序上传到HDFS的⽂件的 副本数</td>
  </tr>
  <tr>
    <td>spark.yarn.preserve.staging.file s</td>
    <td>False</td>
    <td>若为true，在job结束后，将 stage相关的⽂件保留⽽不是删 除</td>
  </tr>
  <tr>
    <td>spark.yarn.cheduler.heartbeat<br><br>.interval-ms</td>
    <td>5 0</td>
    <td>Spark ApMaster发送⼼跳信息 的时间间隔</td>
  </tr>
  <tr>
    <td>park.yarn.max.executor.failure s</td>
    <td>2倍于executor数，最⼩值3</td>
    <td>给YARN RM 导致应⽤程序宣告失败的最⼤<br><br>失败次数</td>
  </tr>
  <tr>
    <td>spark.yarn.aplicationMaster.w aitTries</td>
    <td>10</td>
    <td>executor RM等待Spark ApMaster启动 重试次数，也就是SparkContext 初始化次数。超过这个数值，启 动失败</td>
  </tr>
  <tr>
    <td>spark.yarn.historyServer.adre s</td>
    <td> </td>
    <td>Spark history server的地址（不 要加http:// ）。这个地址会在Spark应⽤程 序完成后提交给YARN RM，然 后RM将信息从RM UI写到<br><br>上。</td>
  </tr>
  <tr>
    <td>spark.yarn.dist.archives</td>
    <td>(none)</td>
    <td>history server UI</td>
  </tr>
  <tr>
    <td>spark.yarn.dist.files</td>
    <td>(none)</td>
    <td> </td>
  </tr>
  <tr>
    <td>spark.executor.instances</td>
    <td>2</td>
    <td>executor实例个数</td>
  </tr>
  <tr>
    <td>spark.yarn.executor.memoryOv</td>
    <td>executorMemory * 0.07, with</td>
    <td>executor的堆内存⼤⼩设置</td>
  </tr>
</table>


#### erhead minimum of 384

<table>
  <tr>
    <th>spark.yarn.driver.memoryOver</th>
    <th>driverMemory * 0.07, with</th>
    <th>driver的堆内存⼤⼩设置</th>
  </tr>
  <tr>
    <td>head spark.yarn.am.memoryOverhea d</td>
    <td>minimum of 384 AM memory * 0.07, with minimum of 384</td>
    <td>am的堆内存⼤⼩设置，在client 模式时设置</td>
  </tr>
  <tr>
    <td>spark.yarn.queue</td>
    <td>default</td>
    <td>使⽤yarn的队列</td>
  </tr>
  <tr>
    <td>spark.yarn.jar</td>
    <td>(none)</td>
    <td> </td>
  </tr>
  <tr>
    <td>spark.yarn.aces.namenodes</td>
    <td>(none)</td>
    <td> </td>
  </tr>
  <tr>
    <td>spark.yarn.apMasterEnv.</td>
    <td>(none)</td>
    <td>设置am的环境变量</td>
  </tr>
  <tr>
    <td>[EnvironmentVariableName] spark.yarn.containerLauncher</td>
    <td>25</td>
    <td>am启动executor的最⼤线程数</td>
  </tr>
  <tr>
    <td>MaxThreads park.yarn.am.extraJavaOption</td>
    <td>(none)</td>
    <td> </td>
  </tr>
  <tr>
    <td>s spark.yarn.maxApAtempts</td>
    <td>yarn.resourcemanager.am.max</td>
    <td>am重试次数</td>
  </tr>
</table>


-atempts in YARN

### Spark on Mesos

Runing Spark on Mesos

使⽤较少，参考 。

### Spark Standalone Mode

Spark Standalone Mode

参考 。

### Spark History Server

当你运⾏Spark Standalone Mode或者Spark on Mesos模式时，你可以通过Spark History Server来查 看job运⾏情况。 Spark History Server的环境变量：

<table>
  <tr>
    <th>属性名称</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>SPARK_DAEMON_MEMORY</td>
    <td>Memory to alocate to the history server</td>
  </tr>
  <tr>
    <td>SPARK_DAEMON_JAVA_OPTS</td>
    <td>(default: 512m). JVM options for the history server (default:</td>
  </tr>
  <tr>
    <td>SPARK_PUBLIC_DNS</td>
    <td>none).</td>
  </tr>
  <tr>
    <td> </td>
    <td>属性</td>
  </tr>
</table>


SPARK_HISTORY_OPTS 配置 spark.history.*

Spark History Server的属性：

<table>
  <tr>
    <th>属性名称</th>
    <th>默认</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>spark.history.provider</td>
    <td>org.apache.spark.deploy.histor y.FsHistoryProvide</td>
    <td>应⽤历史后端实现的类名。 ⽬ 前只有⼀个实现, 由Spark提供, 它查看存储在⽂件系统⾥⾯的应 ⽤⽇志</td>
  </tr>
  <tr>
    <td>spark.history.fs.logDirectory</td>
    <td>file:/tmp/spark-events</td>
    <td> </td>
  </tr>
  <tr>
    <td>spark.history.updateInterval</td>
    <td>10</td>
    <td>以秒为单位，多⻓时间Spark history server显示的信息进⾏更 新。每次更新都会检查持久层事 件⽇志的任何变化。</td>
  </tr>
  <tr>
    <td>spark.history.retainedAplicati ons</td>
    <td>50</td>
    <td>在Spark history server上显示的 最⼤应⽤程序数量，如果超过这 个值，旧的应⽤程序信息将被删 除。</td>
  </tr>
  <tr>
    <td>spark.history.ui.port</td>
    <td>18080</td>
    <td>官⽅版本中，Spark history 的默认访问端⼝</td>
  </tr>
  <tr>
    <td>spark.history.kerberos.enabled</td>
    <td>false</td>
    <td>server 是否使⽤kerberos⽅式登录访问 history server，对于持久层位于 安全集群的HDFS上是有⽤的。 如果设置为true，就要配置下⾯ 的两个属性。</td>
  </tr>
  <tr>
    <td>spark.history.kerberos.principa l</td>
    <td>空</td>
    <td>⽤于Spark history server的 主体名称</td>
  </tr>
  <tr>
    <td>spark.history.kerberos.keytab</td>
    <td>空</td>
    <td>kerberos ⽤于Spark history server的<br><br>⽂件位置</td>
  </tr>
  <tr>
    <td>spark.history.ui.acls.enable</td>
    <td>false</td>
    <td>kerberos keytab 授权⽤户查看应⽤程序信息的时 候是否检查acl。如果启⽤，只 有应⽤程序所有者和<br><br>spark.ui.view.acls<br><br>指定的⽤户可以查看应⽤程序信 如果禁⽤，不做任何检查。</td>
  </tr>
</table>


息;

## 环境变量

通过环境变量配置确定的Spark设置。环境变量从Spark安装⽬录下的 conf/spark-env.sh 脚本读取 （或者windows的 conf/spark-env.cmd ）。在独⽴的或者Mesos模式下，这个⽂件可以给机器确定 的信息，如主机名。当运⾏本地应⽤程序或者提交脚本时，它也起作⽤。 注意，当Spark安装时， conf/spark-env.sh 默认是不存在的。你可以复制 conf/sparkenv.sh.template 创建它。

可以在 spark-env.sh 中设置如下变量：

<table>
  <tr>
    <th>环境变量</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>JAVA_HOME</td>
    <td>Java安装的路径</td>
  </tr>
  <tr>
    <td>PYSPARK_PYTHON</td>
    <td>PySpark⽤到的Python⼆进制执⾏⽂件路径</td>
  </tr>
  <tr>
    <td>SPARK_LOCAL_IP</td>
    <td>机器绑定的IP地址</td>
  </tr>
  <tr>
    <td> </td>
    <td>应⽤程序通知给其他机器的主机名</td>
  </tr>
</table>


SPARK_PUBLIC_DNS 你Spark standalone cluster scripts

除了以上这些，Spark 也可以设置⼀些选项。例如每台机器使⽤的核数以及 最⼤内存。 因为 spark-env.sh 是shel脚本，其中的⼀些可以以编程⽅式设置。例如，你可以通过特定的⽹络接 ⼝计算 SPARK_LOCAL_IP 。

## 配置⽇志

Spark⽤ loging。你可以通过在conf⽬录下添加 log4j.properties ⽂件来配置。⼀种⽅法是复 制 log4j.properties.template ⽂件。

log4j

