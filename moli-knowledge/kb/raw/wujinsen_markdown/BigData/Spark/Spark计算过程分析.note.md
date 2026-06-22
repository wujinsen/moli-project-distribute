基本概念

Spark是⼀个分布式的内存计算框架，其特点是能处理⼤规模数据，计算速度快。Spark延续了 Hadoop的MapReduce计算模型，相⽐之下Spark的计算过程保持在内存中，减少了硬盘读写，能够将 多个操作进⾏合并后计算，因此提升了计算速度。同时Spark也提供了更丰富的计算API。

MapReduce是Hadoop和Spark的计算模型，其特点是Map和Reduce过程⾼度可并⾏化;过程间耦合 度低，单个过程的失败后可以重新计算，⽽不会导致整体失败;最重要的是数据处理中的计算逻辑可以 很好的转换为Map和Reduce操作。对于⼀个数据集来说，Map对每条数据做相同的转换操作，Reduce 可以按条件对数据分组，然后在分组上做操作。除了Map和Reduce操作之外，Spark还延伸出了如 filter，flatMap，count，distinct等更丰富的操作。

RDD的是Spark中最主要的数据结构，可以直观的认为RDD就是要处理的数据集。RDD是分布式的 数据集，每个RDD都⽀持MapReduce类操作，经过MapReduce操作后会产⽣新的RDD，⽽不会修改原 有RDD。RDD的数据集是分区的，因此可以把每个数据分区放到不同的分区上进⾏计算，⽽实际上⼤ 多数MapReduce操作都是在分区上进⾏计算的。Spark不会把每⼀个MapReduce操作都发起运算，⽽是 尽量的把操作累计起来⼀起计算。Spark把操作划分为转换(transformation)和动作(action)，对RDD进 ⾏的转换操作会叠加起来，直到对RDD进⾏动作操作时才会发起计算。这种特性也使Spark可以减少中 间结果的吞吐，可以快速的进⾏多次迭代计算。

系统结构 Spark⾃身只对计算负责，其计算资源的管理和调度由第三⽅框架来实现。常⽤的框架有YARN和

Mesos。本⽂以YARN为例进⾏介绍。先看⼀下Spark on YARN的系统结构图：

![image 1](<Spark计算过程分析.note_images/imageFile1.png>)

- 1


Spark on YARN系统结构图 图中共分为三⼤部分：Spark Driver， Worker， Cluster manager。其中Driver program负责将

RDD转换为任务，并进⾏任务调度。Worker负责任务的执⾏。YARN负责计算资源的维护和分配。

Driver可以运⾏在⽤户程序中，或者运⾏在其中⼀个Worker上。Spark中的每⼀个应⽤(Application) 对应着⼀个Driver。这个Driver可以接收RDD上的计算请求，每个动作(Action)类型的操作将被作为⼀个 Job进⾏计算。Spark会根据RDD的依赖关系构建计算阶段(Stage)的有向⽆环图，每个阶段有与分区数 相同的任务(Task)。这些任务将在每个分区(Partition)上进⾏计算，任务划分完成后Driver将任务提交到 运⾏于Worker上的Executor中进⾏计算，并对任务的成功、失败进⾏记录和重启等处理。

Worker⼀般对应⼀台物理机，每个Worker上可以运⾏多个Executor，每个Executor都是独⽴的JVM 进程，Driver提交的任务就是以线程的形式运⾏在Executor中的。如果使⽤YARN作为资源调度框架的 话，其中⼀个Worker上还会有Executor launcher作为YARN的ApplicationMaster，⽤于向YARN申请计算 资源，并启动、监测、重启Executor。

计算过程 这⾥我们从RDD到输出结果的整个计算过程为主线，探究Spark的计算过程。这个计算过程可以分

为：

RDD构建：构建RDD之间的依赖关系，将RDD转换为阶段的有向⽆环图。 任务调度：根据空闲计算资源情况进⾏任务提交，并对任务的运⾏状态进⾏监测和处理。 任务计算：搭建任务运⾏环境，执⾏任务并返回任务结果。 Shuffle过程：两个阶段之间有宽依赖时，需要进⾏Shuffle操作。 计算结果收集：从每个任务收集并汇总结果。 在这⾥我们⽤⼀个简洁的CharCount程序为例，这个程序把含有a-z字符的列表转化为RDD，对此

RDD进⾏了Map和Reduce操作计算每个字⺟的频数，最后将结果收集。其代码如下：

![image 2](<Spark计算过程分析.note_images/imageFile2.png>)

0

CharCount例⼦程序 RDD构建和转换 RDD按照其作⽤可以分为两种类型，⼀种是对数据源的封装，可以把数据源转换为RDD，这种类

型的RDD包括NewHadoopRDD，ParallelCollectionRDD，JdbcRDD等。另⼀种是对RDD的转换，从⽽实 现⼀种计算⽅法，这种类型的RDD包括MappedRDD，ShuffledRDD，FilteredRDD等。数据源类型的 RDD不依赖于其他RDD，计算类的RDD拥有⾃⼰的RDD依赖。

RDD有三个要素：分区，依赖关系，计算逻辑。分区是保证RDD分布式的特性，分区可以对RDD 的数据进⾏划分，划分后的分区可以分布到不同的Executor中，⼤部分对RDD的计算都是在分区上进⾏ 的。依赖关系维护着RDD的计算过程，每个计算类型的RDD在计算时，会将所依赖的RDD作为数据源 进⾏计算。根据⼀个分区的输出是否被多分区使⽤，Spark还将依赖分为窄依赖和宽依赖。RDD的计算 逻辑是其功能的体现，其计算过程是以所依赖的RDD为数据源进⾏的。

例⼦中共产⽣了三个RDD，除了第⼀个RDD之外，每个RDD与上级RDD有依赖关系。 spark.parallelize(data, partitionSize)⽅法将产⽣⼀个数据源型的ParallelCollectionRDD，这个RDD

的分区是对列表数据的切分，没有上级依赖，计算逻辑是直接返回分区数据。

map函数将会创建⼀个MappedRDD，其分区与上级依赖相同，会有⼀个依赖于 ParallelCollectionRDD的窄依赖，计算逻辑是对ParallelCollectionRDD的数据做map操作。

reduceByKey函数将会产⽣⼀个ShuffledRDD，分区数量与上⾯的MappedRDD相同，会有⼀个依赖 于MappedRDD的宽依赖，计算逻辑是Shuffle后在分区上的聚合操作。

- 2

![image 3](<Spark计算过程分析.note_images/imageFile3.png>)

- 3


RDD的依赖关系 Spark在遇到动作类操作时，就会发起计算Job，把RDD转换为任务，并发送任务到Executor上执

⾏。从RDD到任务的转换过程是在DAGScheduler中进⾏的。其总体思路是根据RDD的依赖关系，把窄 依赖合并到⼀个阶段中，遇到宽依赖则划分出新的阶段，最终形成⼀个阶段的有向⽆环图，并根据图 的依赖关系先后提交阶段。每个阶段按照分区数量划分为多个任务，最终任务被序列化并提交到 Executor上执⾏。

![image 4](<Spark计算过程分析.note_images/imageFile4.png>)

RDD到Task的构建过程 当RDD的动作类操作被调⽤时，RDD将调⽤SparkContext开始提交Job，SparkContext将调⽤ DAGScheduler把RDD转化为阶段的有向⽆环图，然后⾸先将有向⽆环图中没有未完成的依赖的阶段进 ⾏提交。在阶段被提交时，每个阶段将产⽣与分区数量相同的任务，这些任务称之为⼀个TaskSet。任 务的类型分为 ShuffleMapTask和ResultTask，如果阶段的输出将⽤于下个阶段的输⼊，也就是需要进⾏

Shuffle操作，则任务类型为ShuffleMapTask。如果阶段的输⼊即为Job结果，则任务类型为 ResultTask。任务创建完成后会交给TaskSchedulerImpl进⾏TaskSet级别的调度执⾏。

任务调度 在任务调度的分⼯上，DAGScheduler负责总体的任务调度，SchedulerBackend负责与Executors通

信，维护计算资源信息，并负责将任务序列化并提交到Executor。TaskSetManager负责对⼀个阶段的 任务进⾏管理，其中会根据任务的数据本地性选择优先提交的任务。TaskSchedulerImpl负责对TaskSet 进⾏调度，通过调度策略确定TaskSet优先级。同时是⼀个中介者，其将DAGScheduler， SchedulerBackend和TaskSetManager联结起来，对Executor和Task的相关事件进⾏转发。

在任务提交流程上，DAGScheduler提交TaskSet到TaskSchedulerImpl，使TaskSet在此注册。 TaskSchedulerImpl通知SchedulerBackend有新的任务进⼊，SchedulerBackend调⽤makeOffers根据注 册到⾃⼰的Executors信息，确定是否有计算资源执⾏任务，如有资源则通知TaskSchedulerImpl去分配 这些资源。 TaskSchedulerImpl根据TaskSet调度策略优先分配TaskSet接收此资源。TaskSetManager再 根据任务的数据本地性，确定提交哪些任务。最终任务的闭包被SchedulerBackend序列化，并传输给

Executor进⾏执⾏。

![image 5](<Spark计算过程分析.note_images/imageFile5.png>)

- 4


Spark的任务调度 根据以上过程，Spark中的任务调度实际上分了三个层次。第⼀层次是基于阶段的有向⽆环图进⾏

Stage的调度，第⼆层次是根据调度策略(FIFO，FAIR)进⾏TaskSet调度，第三层次是根据数据本地性 (Process，Node，Rack)在TaskSet内进⾏调度。

任务计算 任务的计算过程是在Executor上完成的，Executor监听来⾃SchedulerBackend的指令，接收到任务

时会启动TaskRunner线程进⾏任务执⾏。在TaskRunner中⾸先将任务和相关信息反序列化，然后根据 相关信息获取任务所依赖的Jar包和所需⽂件，完成准备⼯作后执⾏任务的run⽅法，实际上就是执⾏ ShuffleMapTask或ResultTask的run⽅法。任务执⾏完毕后将结果发送给Driver进⾏处理。

在Task.run⽅法中可以看到ShuffleMapTask和ResultTask有着不同的计算逻辑。ShuffleMapTask是 将所依赖RDD的输出写⼊到ShuffleWriter中，为后⾯的Shuffle过程做准备。ResultTask是在所依赖RDD 上应⽤⼀个函数，并返回函数的计算结果。在这两个Task中只能看到数据的输出⽅式，⽽看不到应有 的计算逻辑。实际上计算过程是包含在RDD中的，调⽤RDD. Iterator⽅法获取RDD的数据将触发这个 RDD的计算动作(RDD. Iterator)，由于此RDD的计算过程中也会使⽤所依赖RDD的数据。从⽽RDD的计

算过程将递归向上直到⼀个数据源类型的RDD，再递归向下计算每个RDD的值。需要注意的是，以上 的计算过程都是在分区上进⾏的，⽽不是整个数据集，计算完成得到的是此分区上的结果，⽽不是最 终结果。

从RDD的计算过程可以看出，RDD的计算过程是包含在RDD的依赖关系中的，只要RDD之间是连 续窄依赖，那么多个计算过程就可以在同⼀个Task中进⾏计算，中间结果可以⽴即被下个操作使⽤， ⽽⽆需在进程间、节点间、磁盘上进⾏交换。

![image 6](<Spark计算过程分析.note_images/imageFile6.png>)

- 5


RDD计算过程 Shuffle过程 Shuffle是⼀个对数据进⾏分组聚合的操作过程，原数据将按照规则进⾏分组，然后使⽤⼀个聚合

函数应⽤于分组上，从⽽产⽣新数据。Shuffle操作的⽬的是把同组数据分配到相同分区上，从⽽能够 在分区上进⾏聚合计算。为了提⾼Shuffle性能，还可以先在原分区对数据进⾏聚合 (mapSideCombine)，然后再分配部分聚合的数据到新分区，第三步在新分区上再次进⾏聚合。

在划分阶段时，只有遇到宽依赖才会产⽣新阶段，才需要Shuffle操作。宽依赖与窄依赖取决于原 分区被新分区的使⽤关系，只要⼀个原分区会被多个新分区使⽤，则为宽依赖，需要Shuffle。否则为 窄依赖，不需要Shuffle。

以上也就是说只有阶段与阶段之间需要Shuffle，最后⼀个阶段会输出结果，因此不需要Shuffle。 例⼦中的程序会产⽣两个阶段，第⼀个我们简称Map阶段，第⼆个我们简称Reduce阶段。Shuffle是通 过Map阶段的ShuffleMapTask与Reduce阶段的ShuffledRDD配合完成的。其中ShuffleMapTask会把任务 的计算结果写⼊ShuffleWriter，ShuffledRDD从ShuffleReader中读取数据，Shuffle过程会在写⼊和读取 过程中完成。以HashShuffle为例，HashShuffleWriter在写⼊数据时，会决定是否在原分区做聚合，然 后根据数据的Hash值写⼊相应新分区。HashShuffleReader再根据分区号取出相应数据，然后对数据进 ⾏聚合。

![image 7](<Spark计算过程分析.note_images/imageFile7.png>)

- 6


Spark的Shuffle过程 计算结果收集 ResultTask任务计算完成后可以得到每个分区的计算结果，此时需要在Driver上对结果进⾏汇总从

⽽得到最终结果。

RDD在执⾏collect，count等动作时，会给出两个函数，⼀个函数在分区上执⾏，⼀个函数在分区 结果集上执⾏。例如collect动作在分区上(Executor中)执⾏将Iterator转换为Array的函数，并将此函数 结果返回到Driver。Driver 从多个分区上得到Array类型的分区结果集，然后在结果集上(Driver中)执⾏ 合并Array的操作，从⽽得到最终结果。

总结

Spark对于RDD的设计是其精髓所在。⽤RDD操作数据的感觉就⼀个字：爽!。想到RDD背后是⼏吨 重的 集，⽽我们随⼿调⽤下map(), reduce()就可以把它转换来转换去，⼀种半两拨千⽄的感觉 就会油然⽽⽣。我想是以下特性给我们带来了这些：

⼤数据

RDD把不同来源，不同类型的数据进⾏了统⼀，使我们⾯对RDD的时候就会产⽣⼀种信⼼，就会

认为这是某种类型的RDD，从⽽可以进⾏RDD的所有操作。 对RDD的操作可以叠加到⼀起计算，我们不必担⼼中间结果吞吐对性能的影响。 RDD提供了更丰富的数据集操作函数，这些函数⼤都是在MapReduce基础上扩充的，使⽤起来很

⽅便。

RDD为提供了⼀个简洁的编程界⾯，背后复杂的分布式计算过程对开发者是透明的。从⽽能够让 我们把关注点更多的放在业务上。

