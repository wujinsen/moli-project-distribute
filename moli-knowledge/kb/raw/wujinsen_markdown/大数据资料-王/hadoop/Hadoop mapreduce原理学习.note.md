最近整了很⻓⼀段时间才了解了map reduce的⼯作原理，shufle是mapreduce的⼼脏，了解了这 个过程，有助于编写效率更⾼的mapreduce程序和hadop调优。⾃⼰画了⼀幅流程图（点击查看全 图）：

![image 1](<Hadoop mapreduce原理学习.note_images/imageFile1.png>)

另外，还找到⼀篇⽂章，很好，引⽤⼀下。 Hadop 是Apache 下的⼀个项⽬，由HDFS、MapReduce、HBase、Hive 和ZoKeper等成员组成。其中， HDFS 和MapReduce 是两个最基础最重要的成员。 HDFS是Gogle GFS 的开源版本，⼀个⾼度容错的分布式⽂件系统，它能够提供⾼吞吐量的数据访 问，适合存储海量（PB 级）的⼤⽂件（通常超过64M），其原理如下图所示：

![image 2](<Hadoop mapreduce原理学习.note_images/imageFile2.png>)

采⽤Master/Slave 结构。NameNode 维护集群内的元数据，对外提供创建、打开、删除和重命名⽂件 或⽬录的功能。DatanNode 存储数据，并提负责处理数据的读写请求。DataNode定期向 NameNode 上报⼼跳，NameNode 通过响应⼼跳来控制DataNode。

InfoWord将MapReduce 评为209 年⼗⼤新兴技术的冠军。MapReduce 是⼤规模数据（TB 级）计算 的利器，Map 和Reduce 是它的主要思想，来源于函数式编程语⾔，它的原理如下图所示：Map负责 将数据打散，Reduce负责对数据进⾏聚集，⽤户只需要实现map 和reduce 两个接⼝，即可完成TB级 数据的计算，常⻅的应⽤包括：⽇志分析和数据挖掘等数据分析应⽤。另外，还可⽤于科学数据计 算，如圆周率PI 的计算等。Hadop MapReduce的实现也采⽤了Master/Slave 结构。Master 叫做 JobTracker，⽽Slave 叫做TaskTracker。⽤户提交的计算叫做Job，每⼀个Job会被划分成若⼲个 Tasks。JobTracker负责Job 和Tasks 的调度，⽽TaskTracker负责执⾏Tasks。

![image 3](<Hadoop mapreduce原理学习.note_images/imageFile3.png>)

MapReduce中的Shufle和Sort分析 MapReduce 是现今⼀个⾮常流⾏的分布式计算框架，它被设计⽤于并⾏计算海量数据。第⼀个提出该 技术框架的是Gogle 公司，⽽Gogle 的灵感则来⾃于函数式编程语⾔，如LISP，Scheme，ML 等。 MapReduce 框架的核⼼步骤主要分两部分：Map 和Reduce。当你向MapReduce 框架提交⼀个计算 作业时，它会⾸先把计算作业拆分成若⼲个Map 任务，然后分配到不同的节点上去执⾏，每⼀个Map 任务处理输⼊数据中的⼀部分，当Map 任务完成后，它会⽣成⼀些中间⽂件，这些中间⽂件将会作为 Reduce 任务的输⼊数据。Reduce 任务的主要⽬标就是把前⾯若⼲个Map 的输出汇总到⼀起并输出。 从⾼层抽象来看，MapReduce的数据流图如图1 所示：

![image 4](<Hadoop mapreduce原理学习.note_images/imageFile4.png>)

本⽂的重点是剖析MapReduce的核⼼过程 -Shufle和Sort。在本⽂中，Shufle是指从Map产⽣输出 开始，包括系统执⾏排序以及传送Map输出到Reducer作为输⼊的过程。在这⾥我们将去探究Shufle 是如何⼯作的，因为对基础的理解有助于对MapReduce程序进⾏调优。

⾸先从Map端开始分析，当Map开始产⽣输出的时候，他并不是简单的把数据写到磁盘，因为频繁的 操作会导致性能严重下降，他的处理更加复杂，数据⾸先是写到内存中的⼀个缓冲区，并作⼀些预排 序，以提升效率，如图：

![image 5](<Hadoop mapreduce原理学习.note_images/imageFile5.png>)

每个Map任务都有⼀个⽤来写⼊输出数据的循环内存缓冲区，这个缓冲区默认⼤⼩是10M，可以通过 io.sort.mb属性来设置具体的⼤⼩，当缓冲区中的数据量达到⼀个特定的阀值 (io.sort.mb * io.sort.spil.percent，其中io.sort.spil.percent 默认是0.80)时，系统将会启动⼀个后台 线程把缓冲区中的内容spil 到磁盘。在spil过程中，Map的输出将会继续写⼊到缓冲区，但如果缓冲区 已经满了，Map就会被阻塞直道spil完成。spil线程在把缓冲区的数据写到磁盘前，会对他进⾏⼀个⼆ 次排序，⾸先根据数据所属的partition排序，然后每个partition中再按Key排序。输出包括⼀个索引⽂ 件和数据⽂件，如果设定了Combiner，将在排序输出的基础上进⾏。Combiner就是⼀个Mini Reducer，它在执⾏Map任务的节点本身运⾏，先对Map的输出作⼀次简单的Reduce，使得Map的输 出更紧凑，更少的数据会被写⼊磁盘和传送到Reducer。Spil⽂件保存在由mapred.local.dir指定的⽬ 录中，Map任务结束后删除。 每当内存中的数据达到spil阀值的时候，都会产⽣⼀个新的spil⽂件，所以在Map任务写完他的最后⼀ 个输出记录的时候，可能会有多个spil⽂件，在Map任务完成前，所有的spil⽂件将会被归并排序为⼀ 个索引⽂件和数据⽂件。如图3 所示。这是⼀个多路归并过程，最⼤归并路数由io.sort.factor 控制(默 认是10)。如果设定了Combiner，并且spil⽂件的数量⾄少是3（由min.num.spils.for.combine 属性控 制），那么Combiner 将在输出⽂件被写⼊磁盘前运⾏以压缩数据。

![image 6](<Hadoop mapreduce原理学习.note_images/imageFile6.png>)

对写⼊到磁盘的数据进⾏压缩（这种压缩同Combiner 的压缩不⼀样）通常是⼀个很好的⽅法，因为这 样做使得数据写⼊磁盘的速度更快，节省磁盘空间，并减少需要传送到Reducer 的数据量。默认输出 是不被压缩的， 但可以很简单的设置mapred.compres.map.output为true 启⽤该功能。压缩所使⽤ 的库由mapred.map.output.compresion.codec来设定

当spil ⽂件归并完毕后，Map 将删除所有的临时spil ⽂件，并告知TaskTracker 任务已完成。 Reducers 通过HTP 来获取对应的数据。⽤来传输partitions 数据的⼯作线程个数由 tasktracker.htp.threads 控制，这个设定是针对每⼀个TaskTracker 的，并不是单个Map，默认值为 40，在运⾏⼤作业的⼤集群上可以增⼤以提升数据传输速率。

现在让我们转到Shufle的Reduce部分。Map的输出⽂件放置在运⾏Map任务的TaskTracker的本地磁 盘上（注意：Map输出总是写到本地磁盘，但是Reduce输出不是，⼀般是写到HDFS），它是运⾏ Reduce任务的TaskTracker所需要的输⼊数据。Reduce任务的输⼊数据分布在集群内的多个Map任务 的输出中，Map任务可能会在不同的时间内完成，只要有其中⼀个Map任务完成，Reduce任务就开始 拷⻉他的输出。这个阶段称为拷⻉阶段，Reduce任务拥有多个拷⻉线程，可以并⾏的获取Map输出。 可以通过设定mapred.reduce.paralel.copies来改变线程数。 Reduce是怎么知道从哪些TaskTrackers中获取Map的输出呢？当Map任务完成之后，会通知他们的⽗ TaskTracker，告知状态更新，然后TaskTracker再转告JobTracker，这些通知信息是通过⼼跳通信机制 传输的，因此针对以⼀个特定的作业，jobtracker知道Map输出与tasktrackers的映射关系。Reducer 中有⼀个线程会间歇的向JobTracker询问Map输出的地址，直到把所有的数据都取到。在Reducer取⾛ 了Map输出之后，TaskTracker不会⽴即删除这些数据，因为Reducer可能会失败，他们会在整个作业 完成之后，JobTracker告知他们要删除的时候才去删除。 如果Map输出⾜够⼩，他们会被拷⻉到Reduce TaskTracker的内存中（缓冲区的⼤⼩由 mapred.job.shufle.input.bufer.percnet控制），或者达到了Map输出的阀值的⼤⼩(由 mapred.inmem.merge.threshold控制)，缓冲区中的数据将会被归并然后spil到磁盘。 拷⻉来的数据叠加在磁盘上，有⼀个后台线程会将它们归并为更⼤的排序⽂件，这样做节省了后期归 并的时间。对于经过压缩的Map 输出，系统会⾃动把它们解压到内存⽅便对其执⾏归并。 当所有的Map 输出都被拷⻉后，Reduce 任务进⼊排序阶段（更恰当的说应该是归并阶段，因为排序 在Map 端就已经完成），这个阶段会对所有的Map 输出进⾏归并排序，这个⼯作会重复多次才能完 成。

假设这⾥有50 个Map 输出（可能有保存在内存中的），并且归并因⼦是10（由io.sort.factor控制，就 像Map 端的merge ⼀样），那最终需要5 次归并。每次归并会把10个⽂件归并为⼀个，最终⽣成5 个 中间⽂件。在这⼀步之后，系统不再把5 个中间⽂件归并成⼀个，⽽是排序后直接“喂”给Reduce 函 数，省去向磁盘写数据这⼀步。最终归并的数据可以是混合数据，既有内存上的也有磁盘上的。由于 归并的⽬的是归并最少的⽂件数⽬，使得在最后⼀次归并时总⽂件个数达到归并因⼦的数⽬，所以每 次操作所涉及的⽂件个数在实际中会更微妙些。譬如，如果有40 个⽂件，并不是每次都归并10 个最终 得到4 个⽂件，相反第⼀次只归并4 个⽂件，然后再实现三次归并，每次10 个，最终得到4 个归并好 的⽂件和6 个未归并的⽂件。要注意，这种做法并没有改变归并的次数，只是最⼩化写⼊磁盘的数据 优化措施，因为最后⼀次归并的数据总是直接送到Reduce 函数那⾥。在Reduce 阶段，Reduce 函数 会作⽤在排序输出的每⼀个key 上。这个阶段的输出被直接写到输出⽂件系统，⼀般是HDFS。在 HDFS 中，因为TaskTracker 节点也运⾏着⼀个DataNode 进程，所以第⼀个块备份会直接写到本地磁 盘。到此，MapReduce 的Shufle 和Sort 分析完毕。

# 最后，个⼈还有⼀点没有理解。map端的htpfetch和reduce端的 copy阶段是⼀个过程吗，如何区分？盼⾼⼿解答！

