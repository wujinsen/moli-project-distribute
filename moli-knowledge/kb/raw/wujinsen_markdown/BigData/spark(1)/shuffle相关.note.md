# Shufle相关

Shufle操作⼤概是对Spark性能影响最⼤的步骤之⼀（因为可能涉及到排序，磁盘IO，⽹络IO等众多 CPU或IO密集的操作），这也是为什么在Spark 1.1的代码中对整个Shufle框架代码进⾏了重构，将 Shufle相关读写操作抽象封装到Plugable的Shufle Manager中，便于试验和实现不同的Shufle功能 模块。例如为了解决Hash Based的Shufle Manager在⽂件读写效率⽅⾯的问题⽽实现的Sort Base的 Shufle Manager。

spark.shufle.manager

⽤来配置所使⽤的Shufle Manager，⽬前可选的Shufle Manager包括默认的 org.apache.spark.shufle.sort.HashShufleManager（配置参数值为hash）和新的 org.apache.spark.shufle.sort.SortShufleManager（配置参数值为sort）。 这两个ShufleManager如何选择呢，⾸先需要了解他们在实现⽅式上的区别。 HashShufleManager，故名思义也就是在Shufle的过程中写数据时不做排序操作，只是将数据根据 Hash的结果，将各个Reduce分区的数据写到各⾃的磁盘⽂件中。带来的问题就是如果Reduce分区的 数量⽐较⼤的话，将会产⽣⼤量的磁盘⽂件。如果⽂件数量特别巨⼤，对⽂件读写的性能会带来⽐较 ⼤的影响，此外由于同时打开的⽂件句柄数量众多，序列化，以及压缩等操作需要分配的临时内存空 间也可能会迅速膨胀到⽆法接受的地步，对内存的使⽤和GC带来很⼤的压⼒，在Executor内存⽐较⼩ 的情况下尤为突出，例如Spark on Yarn模式。 SortShufleManager，是1.1版本之后实现的⼀个试验性（也就是⼀些功能和接⼝还在开发演变中）的 ShufleManager，它在写⼊分区数据的时候，⾸先会根据实际情况对数据采⽤不同的⽅式进⾏排序操 作，底线是⾄少按照Reduce分区Partition进⾏排序，这样来⾄于同⼀个Map任务Shufle到不同的 Reduce分区中去的所有数据都可以写⼊到同⼀个外部磁盘⽂件中去，⽤简单的Ofset标志不同Reduce 分区的数据在这个⽂件中的偏移量。这样⼀个Map任务就只需要⽣成⼀个shufle⽂件，从⽽避免了上 述HashShufleManager可能遇到的⽂件数量巨⼤的问题 两者的性能⽐较，取决于内存，排序，⽂件操作等因素的综合影响。 对于不需要进⾏排序的Shufle操作来说，如repartition等，如果⽂件数量不是特别巨⼤， HashShufleManager⾯临的内存问题不⼤，⽽SortShufleManager需要额外的根据Partition进⾏排 序，显然HashShufleManager的效率会更⾼。 ⽽对于本来就需要在Map端进⾏排序的Shufle操作来说，如ReduceByKey等，使⽤ HashShufleManager虽然在写数据时不排序，但在其它的步骤中仍然需要排序，⽽ SortShufleManager则可以将写数据和排序两个⼯作合并在⼀起执⾏，因此即使不考虑 HashShufleManager的内存使⽤问题，SortShufleManager依旧可能更快。

spark.shufle.sort.bypasMergeThreshold

这个参数仅适⽤于SortShufleManager，如前所述，SortShufleManager在处理不需要排序的Shufle 操作时，由于排序带来性能的下降。这个参数决定了在这种情况下，当Reduce分区的数量⼩于多少的 时候，在SortShufleManager内部不使⽤Merge Sort的⽅式处理数据，⽽是与Hash Shufle类似，直 接将分区⽂件写⼊单独的⽂件，不同的是，在最后⼀步还是会将这些⽂件合并成⼀个单独的⽂件。这 样通过去除Sort步骤来加快处理速度，代价是需要并发打开多个⽂件，所以内存消耗量增加，本质上 是相对HashShufleMananger⼀个折衷⽅案。 这个参数的默认值是20个分区，如果内存GC问题严 重，可以降低这个值。

spark.shufle.consolidateFiles

这个配置参数仅适⽤于HashShufleMananger的实现，同样是为了解决⽣成过多⽂件的问题，采⽤的 ⽅式是在不同批次运⾏的Map任务之间重⽤Shufle输出⽂件，也就是说合并的是不同批次的Map任务 的输出数据，但是每个Map任务所需要的⽂件还是取决于Reduce分区的数量，因此，它并不减少同时 打开的输出⽂件的数量，因此对内存使⽤量的减少并没有帮助。只是HashShufleManager⾥的⼀个折 中的解决⽅案。 需要注意的是，这部分的代码实现尽管原理上说很简单，但是涉及到底层具体的⽂件系统的实现和限 制等因素，例如在并发访问等⽅⾯，需要处理的细节很多，因此⼀直存在着这样那样的bug或者问题， 导致在例如EXT3上使⽤时，特定情况下性能反⽽可能下降，因此从Spark 0.8的代码开始，⼀直到 Spark 1.1的代码为⽌也还没有被标志为Stable，不是默认采⽤的⽅式。此外因为并不减少同时打开的输 出⽂件的数量，因此对性能具体能带来多⼤的改善也取决于具体的⽂件数量的情况。所以即使你⾯临 着Shufle⽂件数量巨⼤的问题，这个配置参数是否使⽤，在什么版本中可以使⽤，也最好还是实际测 试以后再决定。

spark.shufle.spil

shufle的过程中，如果涉及到排序，聚合等操作，势必会需要在内存中维护⼀些数据结构，进⽽占⽤ 额外的内存。如果内存不够⽤怎么办，那只有两条路可以⾛，⼀就是out of memory 出错了，⼆就是 将部分数据临时写到外部存储设备中去，最后再合并到最终的Shufle输出⽂件中去。 这⾥spark.shufle.spil 决定是否Spil到外部存储设备（默认打开）,如果你的内存⾜够使⽤，或者数据 集⾜够⼩，当然也就不需要Spil，毕竟Spil带来了额外的磁盘操作。

spark.shufle.memoryFraction / spark.shufle.safetyFraction

在启⽤Spil的情况下，spark.shufle.memoryFraction（1.1后默认为0.2）决定了当Shufle过程中使⽤ 的内存达到总内存多少⽐例的时候开始Spil。

通过spark.shufle.memoryFraction可以调整Spil的触发条件，即Shufle占⽤内存的⼤⼩，进⽽调整 Spil的频率和GC的⾏为。总的来说，如果Spil太过频繁，可以适当增加spark.shufle.memoryFraction 的⼤⼩，增加⽤于Shufle的内存，减少Spil的次数。当然这样⼀来为了避免内存溢出，对应的可能需 要减少RD cache占⽤的内存，即减⼩spark.storage.memoryFraction的值，这样RD cache的容量 减少，有可能带来性能影响，因此需要综合考虑。 由于Shufle数据的⼤⼩是估算出来的，⼀来为了降低开销，并不是每增加⼀个数据项都完整的估算⼀ 次，⼆来估算也会有误差，所以实际暂⽤的内存可能⽐估算值要⼤，这⾥spark.shufle.safetyFraction （默认为0.8）⽤来作为⼀个保险系数，降低实际Shufle使⽤的内存阀值，增加⼀定的缓冲，降低实际 内存占⽤超过⽤户配置值的概率。

spark.shufle.spil.compres / spark.shufle.compres

这两个配置参数都是⽤来设置Shufle过程中是否使⽤压缩算法对Shufle数据进⾏压缩，前者针对Spil 的中间数据，后者针对最终的shufle输出⽂件，默认都是True 理论上说，spark.shufle.compres设置为True通常都是合理的，因为如果使⽤千兆以下的⽹卡，⽹络 带宽往往最容易成为瓶颈。此外，⽬前的Spark任务调度实现中，以Shufle划分Stage，下⼀个Stage 的任务是要等待上⼀个Stage的任务全部完成以后才能开始执⾏，所以shufle数据的传输和CPU计算任 务之间通常不会重叠，这样Shufle数据传输量的⼤⼩和所需的时间就直接影响到了整个任务的完成速 度。但是压缩也是要消耗⼤量的CPU资源的，所以打开压缩选项会增加Map任务的执⾏时间，因此如 果在CPU负载的影响远⼤于磁盘和⽹络带宽的影响的场合下，也可能将spark.shufle.compres 设置 为False才是最佳的⽅案 对于spark.shufle.spil.compres⽽⾔，情况类似，但是spil数据不会被发送到⽹络中，仅仅是临时写 ⼊本地磁盘，⽽且在⼀个任务中同时需要执⾏压缩和解压缩两个步骤，所以对CPU负载的影响会更⼤ ⼀些，⽽磁盘带宽（如果标配12HD的话）可能往往不会成为Spark应⽤的主要问题，所以这个参数相 对⽽⾔，或许更有机会需要设置为False。 总之，Shufle过程中数据是否应该压缩，取决于CPU/DISK/NETWORK的实际能⼒和负载，应该综合考 虑。

# Storage相关配置参数

spark.local.dir

这个看起来很简单，就是Spark⽤于写中间数据，如RD Cache，Shufle，Spil等数据的位置，那么 有什么可以注意的呢。

⾸先，最基本的当然是我们可以配置多个路径（⽤逗号分隔）到多个磁盘上增加整体IO带宽，这个⼤ 家都知道。 其次，⽬前的实现中，Spark是通过对⽂件名采⽤hash算法分布到多个路径下的⽬录中去，如果你的存 储设备有快有慢，⽐如 SD+HD混合使⽤，那么你可以通过在 SD上配置更多的⽬录路径来增⼤它被 Spark使⽤的⽐例，从⽽更好地利⽤ SD的IO带宽能⼒。当然这只是⼀种变通的⽅法，终极解决⽅案还 是应该像⽬前HDFS的实现⽅向⼀样，让Spark能够感知具体的存储设备类型，针对性的使⽤。 需要注意的是，在Spark 1.0 以后，SPARK_LOCAL_DIRS (Standalone, Mesos) or LOCAL_DIRS (YARN)参数会覆盖这个配置。⽐如Spark On YARN的时候，Spark Executor的本地路径依赖于Yarn的 配置，⽽不取决于这个参数。

spark.executor.memory

Executor 内存的⼤⼩，和性能本身当然并没有直接的关系，但是⼏乎所有运⾏时性能相关的内容都或 多或少间接和内存⼤⼩相关。这个参数最终会被设置到Executor的JVM的heap尺⼨上，对应的就是 Xmx和Xms的值 理论上Executor 内存当然是多多益善，但是实际受机器配置，以及运⾏环境，资源共享，JVM GC效 率等因素的影响，还是有可能需要为它设置⼀个合理的⼤⼩。 多⼤算合理，要看实际情况 Executor的内存基本上是Executor内部所有任务共享的，⽽每个Executor上可以⽀持的任务的数量取 决于Executor所管理的CPU Core资源的多少，因此你需要了解每个任务的数据规模的⼤⼩，从⽽推算 出每个Executor⼤致需要多少内存即可满⾜基本的需求。 如何知道每个任务所需内存的⼤⼩呢，这个很难统⼀的衡量，因为除了数据集本身的开销，还包括算 法所需各种临时内存空间的使⽤，⽽根据具体的代码算法等不同，临时内存空间的开销也不同。但是 数据集本身的⼤⼩，对最终所需内存的⼤⼩还是有⼀定的参考意义的。 通常来说每个分区的数据集在内存中的⼤⼩，可能是其在磁盘上源数据⼤⼩的若⼲倍（不考虑源数据 压缩，Java对象相对于原始裸数据也还要算上⽤于管理数据的数据结构的额外开销），需要准确的知 道⼤⼩的话，可以将RD cache在内存中，从BlockManager的Log输出可以看到每个Cache分区的⼤ ⼩（其实也是估算出来的，并不完全准确） 如： BlockManagerInfo: Aded rd_0_1 on disk on sr438 4134 (size: 495.3 MB) 反过来说，如果你的Executor的数量和内存⼤⼩受机器物理配置影响相对固定，那么你就需要合理规 划每个分区任务的数据规模，例如采⽤更多的分区，⽤增加任务数量（进⽽需要更多的批次来运算所 有的任务）的⽅式来减⼩每个任务所需处理的数据⼤⼩。

spark.storage.memoryFraction

如前⾯所说spark.executor.memory决定了每个Executor可⽤内存的⼤⼩，⽽ spark.storage.memoryFraction则决定了在这部分内存中有多少可以⽤于Memory Store管理RD Cache数据，剩下的内存⽤来保证任务运⾏时各种其它内存空间的需要。

spark.executor.memory默认值为0.6，官⽅⽂档建议这个⽐值不要超过JVM Old Gen区域的⽐值。这 也很容易理解，因为RD Cache数据通常都是⻓期驻留内存的，理论上也就是说最终会被转移到Old Gen区域（如果该RD还没有被删除的话），如果这部分数据允许的尺⼨太⼤，势必把Old Gen区域占 满，造成频繁的FUL GC。 如何调整这个⽐值，取决于你的应⽤对数据的使⽤模式和数据的规模，粗略的来说，如果频繁发⽣Ful GC，可以考虑降低这个⽐值，这样RD Cache可⽤的内存空间减少（剩下的部分Cache数据就需要通 过Disk Store写到磁盘上了），会带来⼀定的性能损失，但是腾出更多的内存空间⽤于执⾏任务，减少 Ful GC发⽣的次数，反⽽可能改善程序运⾏的整体性能

## spark.streaming.blockInterval

这个参数⽤来设置Spark Streaming⾥Stream Receiver⽣成Block的时间间隔，默认为20ms。具体的 ⾏为表现是具体的Receiver所接收的数据，每隔这⾥设定的时间间隔，就从Bufer中⽣成⼀个 StreamBlock放进队列，等待进⼀步被存储到BlockManager中供后续计算过程使⽤。理论上来说，为 了每个Streaming Batch 间隔⾥的数据是均匀的，这个时间间隔当然应该能被Batch的间隔时间⻓度所 整除。总体来说，如果内存⼤⼩够⽤，Streaming的数据来得及处理，这个blockInterval时间间隔的影 响不⼤，当然，如果数据Cache Level是Memory+Ser，即做了序列化处理，那么BlockInterval的⼤⼩ 会影响序列化后数据块的⼤⼩，对于Java 的GC的⾏为会有⼀些影响。 此外spark.streaming.blockQueueSize决定了在StreamBlock被存储到BlockMananger之前，队列中最 多可以容纳多少个StreamBlock。默认为10，因为这个队列Pol的时间间隔是10ms，所以如果CPU不 是特别繁忙的话，基本上应该没有问题。

