# Spark技术内幕：Shufle的性能调优

分类： 2015-01-18 19 09 718⼈阅读

Spark架构探索

(3)

评论 收藏 举报 sparkspark shufle性能调优

⽬录(?)

[+]

通过上⾯的架构和源码实现的分析，不难得出Shufle是Spark Core⽐较复杂的模块的结论。它也是⾮ 常影响性能的操作之⼀。因此，在这⾥整理了会影响Shufle性能的各项配置。尽管⼤部分的配置项在 前⽂已经解释过它的含义，由于这些参数的确是⾮常重要，这⾥算是做⼀个详细的总结。

## 1.1.1 spark.shufle.manager

前⽂也多次提到过，Spark1.2.0官⽅⽀持两种⽅式的Shufle，即Hash Based Shufle和Sort Based Shufle。其中在Spark 1.0之前仅⽀持Hash Based Shufle。Spark 1.1的时候引⼊了Sort Based Shufle。Spark 1.2的默认Shufle机制从Hash变成了Sort。如果需要Hash Based Shufle，可以将 spark.shufle.manager设置成“hash”即可。 如果对性能有⽐较苛刻的要求，那么就要理解这两种不同的Shufle机制的原理，结合具体的应⽤场景 进⾏选择。 Hash Based Shufle，就是将数据根据Hash的结果，将各个Reducer partition的数据写到单独的⽂件 中去，写数据时不会有排序的操作。这个问题就是如果Reducer的partition⽐较多的时候，会产⽣⼤量 的磁盘⽂件。这会带来两个问题：

- 1) 同时打开的⽂件⽐较多，那么⼤量的⽂件句柄和写操作分配的临时内存会⾮常⼤，对于内存的使 ⽤和GC带来很多的压⼒。尤其是在Sparkon YARN的模式下，Executor分配的内存普遍⽐较⼩的时 候，这个问题会更严重。
- 2) 从整体来看，这些⽂件带来⼤量的随机读，读性能可能会遇到瓶颈。 更加细节的讨论可以参⻅7.1节和7.6.6（尝试去解决写的⽂件太多的问题）。


Sort Based Shufle会根据实际情况对数据采⽤不同的⽅式进⾏Sort。这个排序可能仅仅是按照 Reducer的partition进⾏排序，保证同⼀个Shufle Map Task的对应于不同的Reducer的partition的数 据都可以写到同⼀个数据⽂件，通过⼀个Ofset来标记不同的Reducer partition的分界。因此⼀个 Shufle Map Task仅仅会⽣成⼀个数据⽂件（还有⼀个index索引⽂件），从⽽避免了Hash Based Shufle⽂件数量过多的问题。

选择Hash还是Sort，取决于内存，排序和⽂件操作等因素的综合影响。

对于不需要进⾏排序的Shufle⽽且Shufle产⽣的⽂件数量不是特别多，Hash Based Shufle可能是个 更好的选择；毕竟Sort Based Shufle⾄少会按照Reducer的partition进⾏排序。 ⽽Sort BasedShufle的优势就在于Scalability，它的出现实际上很⼤程度上是解决Hash Based Shufle 的Scalability的问题。由于Sort Based Shufle还在不断的演进中，因此Sort Based Shufle的性能会得 到不断的改善。 对选择那种Shufle，如果对于性能要求苛刻，最好还是通过实际的场景中测试后再决定。不过选择默 认的Sort，可以满⾜⼤部分的场景需要。

- 1.1.2 spark.shufle.spil
- 1.1.3 spark.shufle.memoryFraction和spark.shufle.safetyFraction
- 1.1.4 spark.shufle.sort.bypasMergeThreshold


这个参数的默认值是true，⽤于指定Shufle过程中如果内存中的数据超过阈值（参考 spark.shufle.memoryFraction的设置），那么是否需要将部分数据临时写⼊外部存储。如果设置为 false，那么这个过程就会⼀直使⽤内存，会有Out Of Memory的⻛险。因此只有在确定内存⾜够使⽤ 时，才可以将这个选项设置为false。 对于Hash BasedShufle的Shufle Write过程中使⽤的 org.apache.spark.util.colection.ApendOnlyMap就是全内存的⽅式，⽽ org.apache.spark.util.colection.ExternalApendOnlyMap对 org.apache.spark.util.colection.ApendOnlyMap有了进⼀步的封装，在内存使⽤超过阈值时会将它 spil到外部存储，在最后的时候会对这些临时⽂件进⾏Merge。 ⽽Sort BasedShufle Write使⽤到的org.apache.spark.util.colection.ExternalSorter也会有类似的 spil。 ⽽对于ShufleRead，如果需要做agregate，也可能在agregate的过程中将数据spil的外部存储。

在启⽤spark.shufle.spil的情况下，spark.shufle.memoryFraction决定了当Shufle过程中使⽤的内存 达到总内存多少⽐例的时候开始Spil。在Spark 1.2.0⾥，这个值是0.2。通过这个参数可以设置Shufle 过程占⽤内存的⼤⼩，它直接影响了Spil的频率和GC。

如果Spil的频率太⾼，那么可以适当的增加spark.shufle.memoryFraction来增加Shufle过程的可⽤ 内存数，进⽽减少Spil的频率。当然为了避免 OM（内存溢出），可能就需要减少RD cache所⽤的 内存，即需要减少spark.storage.memoryFraction的值；但是减少RD cache所⽤的内存有可能会带 来其他的影响，因此需要综合考量。 在Shufle过程中，Shufle占⽤的内存数是估计出来的，并不是每次新增的数据项都会计算⼀次占⽤的 内存⼤⼩，这样做是为了降低时间开销。但是估计也会有误差，因此存在实际使⽤的内存数⽐估算值 要⼤的情况，因此参数 spark.shufle.safetyFraction作为⼀个保险系数降低实际Shufle过程所需要的 内存值，降低实际内存超出⽤户配置值的⻛险。

这个配置的默认值是20，⽤于设置在Reducer的partition数⽬少于多少的时候，Sort Based Shufle内 部不使⽤Merge Sort的⽅式处理数据，⽽是直接将每个partition写⼊单独的⽂件。这个⽅式和Hash Based的⽅式是类似的，区别就是在最后这些⽂件还是会合并成⼀个单独的⽂件，并通过⼀个index索 引⽂件来标记不同partition的位置信息。从Reducer看来，数据⽂件和索引⽂件的格式和内部是否做过 Merge Sort是完全相同的。 这个可以看做SortBased Shufle在Shufle量⽐较⼩的时候对于Hash Based Shufle的⼀种折衷。当然 了它和Hash Based Shufle⼀样，也存在同时打开⽂件过多导致内存占⽤增加的问题。因此如果GC⽐ 较严重或者内存⽐较紧张，可以适当的降低这个值。

- 1.1.5 spark.shufle.blockTransferService
- 1.1.6 spark.shufle.consolidateFiles
- 1.1.7 spark.shufle.service.enabled
- 1.1.8 spark.shufle.compres和 spark.shufle.spil.compres


在Spark 1.2.0，这个配置的默认值是nety，⽽之前是nio。这个主要是⽤于在各个Executor之间传输 Shufle数据。Nety的实现更加简洁，但实际上⽤户不⽤太关⼼这个选项。除⾮是有特殊的需求，否则 采⽤默认配置就可以。

这个配置的默认配置是false。主要是为了解决在Hash Based Shufle过程中产⽣过多⽂件的问题。如 果配置选项为true，那么对于同⼀个Core上运⾏的Shufle Map Task不会新产⽣⼀个Shufle⽂件⽽是 重⽤原来的。但是每个Shufle Map Task还是需要产⽣下游Task数量的⽂件，因此它并没有减少同时 打开⽂件的数量。如果需要了解更加详细的细节，可以阅读7.1节。 但是consolidateFiles的机制在Spark 0.8.1就引⼊了，到Spark 1.2.0还是没有稳定下来。从源码实现的 ⻆度看，实现源码是⾮常简单的，但是由于涉及本地的⽂件系统等限制，这个策略可能会带来各种各 样的问题。由于它并没有减少同时打开⽂件的数量，因此不能减少由⽂件句柄带来的内存消耗。如果 ⾯临Shufle的⽂件数量⾮常⼤，那么是否打开这个选项最好还是通过实际测试后再决定。

(false)

这两个参数的默认配置都是true。spark.shufle.compres和spark.shufle.spil.compres都是⽤来设 置Shufle过程中是否对Shufle数据进⾏压缩；其中前者针对最终写⼊本地⽂件系统的输出⽂件，后者 针对在处理过程需要spil到外部存储的中间数据，后者针对最终的shufle输出⽂件。

如何设置spark.shufle.compres?

如果下游的Task通过⽹络获取上游Shufle Map Task的结果的⽹络IO成为瓶颈，那么就需要考虑将它 设置为true：通过压缩数据来减少⽹络IO。由于上游Shufle Map Task和下游的Task现阶段是不会并⾏ 处理的，即上游Shufle Map Task处理完成，然后下游的Task才会开始执⾏。因此如果需要压缩的时 间消耗就是Shufle MapTask压缩数据的时间 + ⽹络传输的时间 + 下游Task解压的时间；⽽不需要压缩 的时间消耗仅仅是⽹络传输的时间。因此需要评估压缩解压时间带来的时间消耗和因为数据压缩带来 的时间节省。如果⽹络成为瓶颈，⽐如集群普遍使⽤的是千兆⽹络，那么可能将这个选项设置为true是 合理的；如果计算是CPU密集型的，那么可能将这个选项设置为false才更好。

如何设置spark.shufle.spil.compres？ 如果设置为true，代表处理的中间结果在spil到本地硬盘时都会进⾏压缩，在将中间结果取回进⾏ merge的时候，要进⾏解压。因此要综合考虑CPU由于引⼊压缩解压的消耗时间和Disk IO因为压缩带 来的节省时间的⽐较。在Disk IO成为瓶颈的场景下，这个被设置为true可能⽐较合适；如果本地硬盘 是 SD，那么这个设置为false可能⽐较合适。

## 1.1.9 spark.reducer.maxMbInFlight

这个参数⽤于限制⼀个ReducerTask向其他的Executor请求Shufle数据时所占⽤的最⼤内存数，尤其 是如果⽹卡是千兆和千兆以下的⽹卡时。默认值是48MB。设置这个值需要中和考虑⽹卡带宽和内存。

