# 理解Spark的核⼼RD

作者 发布于 2014年9⽉2⽇ |

张逸 6 讨论

分享到：微博微信FacebokTwiter有道云笔记邮件分享

稍后阅读

我的阅读清单

![image 1](<理解Spark的核心RDD.note_images/imageFile1.png>)

与许多专有的⼤数据处理平台不同，Spark建⽴在统⼀抽象的RD之上，使得它可以以基本⼀致的⽅式 应对不同的⼤数据处理场景，包括MapReduce，Streaming，SQL，Machine Learning以及Graph等。 这即Matei Zaharia所谓的“设计⼀个通⽤的编程抽象（Unified Progra ming Abstraction）。这正是 Spark这朵⼩⽕花让⼈着迷的地⽅。 要理解Spark，就需得理解RD。

## RD是什么？

RD，全称为Resilient Distributed Datasets，是⼀个容错的、并⾏的数据结构，可以让⽤户显式地将 数据存储到磁盘和内存中，并能控制数据的分区。同时，RD还提供了⼀组丰富的操作来操作这些数 据。在这些操作中，诸如map、flatMap、filter等转换操作实现了monad模式，很好地契合了Scala的 集合操作。除此之外，RD还提供了诸如join、groupBy、reduceByKey等更为⽅便的操作（注意， reduceByKey是action，⽽⾮transformation），以⽀持常⻅的数据运算。 通常来讲，针对数据处理有⼏种常⻅模型，包括：Iterative Algorithms，Relational Queries， MapReduce，Stream Procesing。例如Hadop MapReduce采⽤了MapReduces模型，Storm则采⽤ 了Stream Procesing模型。RD混合了这四种模型，使得Spark可以应⽤于各种⼤数据处理场景。

RD作为数据结构，本质上是⼀个只读的分区记录集合。⼀个RD可以包含多个分区，每个分区就是 ⼀个dataset⽚段。RD可以相互依赖。如果RD的每个分区最多只能被⼀个Child RD的⼀个分区使 ⽤，则称之为narow dependency；若多个Child RD分区都可以依赖，则称之为wide dependency。 不同的操作依据其特性，可能会产⽣不同的依赖。例如map操作会产⽣narow dependency，⽽join操 作则产⽣wide dependency。 Spark之所以将依赖分为narow与wide，基于两点原因。 ⾸先，narow dependencies可以⽀持在同⼀个cluster node上以管道形式执⾏多条命令，例如在执⾏ 了map后，紧接着执⾏filter。相反，wide dependencies需要所有的⽗分区都是可⽤的，可能还需要调 ⽤类似MapReduce之类的操作进⾏跨节点传递。 其次，则是从失败恢复的⻆度考虑。narow dependencies的失败恢复更有效，因为它只需要重新计算 丢失的parent partition即可，⽽且可以并⾏地在不同节点进⾏重计算。⽽wide dependencies牵涉到 RD各级的多个Parent Partitions。下图说明了narow dependencies与wide dependencies之间的区 别：

![image 2](<理解Spark的核心RDD.note_images/imageFile2.png>)

本图来⾃Matei Zaharia撰写的论⽂An Architecture for Fast and General Data Procesing on Large Clusters。图中，⼀个box代表⼀个RD，⼀个带阴影的矩形框代表⼀个partition。

## RD如何保障数据处理效率？

RD提供了两⽅⾯的特性persistence和patitioning，⽤户可以通过persist与patitionBy函数来控制RD 的这两个⽅⾯。RD的分区特性与并⾏计算能⼒(RD定义了paralerize函数)，使得Spark可以更好地 利⽤可伸缩的硬件资源。若将分区与持久化⼆者结合起来，就能更加⾼效地处理海量数据。例如：

1 input.map(parseArticle _).partitionBy(partitioner).cache()

partitionBy函数需要接受⼀个Partitioner对象，如：

1 val partitioner = new HashPartitioner(sc.defaultParallelism)

RD本质上是⼀个内存数据集，在访问RD时，指针只会指向与操作相关的部分。例如存在⼀个⾯向 列的数据结构，其中⼀个实现为Int的数组，另⼀个实现为Float的数组。如果只需要访问Int字段，RD 的指针可以只访问Int数组，避免了对整个数据结构的扫描。 RD将操作分为两类：transformation与action。⽆论执⾏了多少次transformation操作，RD都不会 真正执⾏运算，只有当action操作被执⾏时，运算才会触发。⽽在RD的内部实现机制中，底层接⼝则 是基于迭代器的，从⽽使得数据访问变得更⾼效，也避免了⼤量中间结果对内存的消耗。 在实现时，RD针对transformation操作，都提供了对应的继承⾃RD的类型，例如map操作会返回 MapedRD，⽽flatMap则返回FlatMapedRD。当我们执⾏map或flatMap操作时，不过是将当前 RD对象传递给对应的RD对象⽽已。例如：

1 def map[U: ClassTag](f: T => U): RDD[U] = new MappedRDD(this, sc.clean(f))

这些继承⾃RD的类都定义了compute函数。该函数会在action操作被调⽤时触发，在函数内部是通过 迭代器进⾏对应的转换操作：

- 1 private[spark]

- 2 class MappedRDD[U: ClassTag, T: ClassTag](prev: RDD[T], f: T => U)

- 3 extends RDD[U](prev) {

- 4

- 5 override def getPartitions: Array[Partition] = firstParent[T].partitions

- 6

- 7 override def compute(split: Partition, context: TaskContext) =

- 8 firstParent[T].iterator(split, context).map(f)

- 9 }


## RD对容错的⽀持

⽀持容错通常采⽤两种⽅式：数据复制或⽇志记录。对于以数据为中⼼的系统⽽⾔，这两种⽅式都⾮ 常昂贵，因为它需要跨集群⽹络拷⻉⼤量数据，毕竟带宽的数据远远低于内存。 RD天⽣是⽀持容错的。⾸先，它⾃身是⼀个不变的(i mutable)数据集，其次，它能够记住构建它的 操作图（Graph of Operation），因此当执⾏任务的Worker失败时，完全可以通过操作图获得之前执 ⾏的操作，进⾏重新计算。由于⽆需采⽤replication⽅式⽀持容错，很好地降低了跨⽹络的数据传输成 本。

不过，在某些场景下，Spark也需要利⽤记录⽇志的⽅式来⽀持容错。例如，在Spark Streaming中， 针对数据进⾏update操作，或者调⽤Streaming提供的window操作时，就需要恢复执⾏过程的中间状 态。此时，需要通过Spark提供的checkpoint机制，以⽀持操作能够从checkpoint得到恢复。 针对RD的wide dependency，最有效的容错⽅式同样还是采⽤checkpoint机制。不过，似乎Spark的 最新版本仍然没有引⼊auto checkpointing机制。

## 总结

RD是Spark的核⼼，也是整个Spark的架构基础。它的特性可以总结如下：

它是不变的数据结构存储

它是⽀持跨集群的分布式数据结构

可以根据数据记录的key对结构进⾏分区

提供了粗粒度的操作，且这些操作都⽀持分区 它将数据存储在内存中，从⽽提供了低延迟性

## 作者简介

![image 3](<理解Spark的核心RDD.note_images/imageFile3.png>)

张逸，现就职于ThoughtWorks中国。作为⼀名咨询师，主要为客户提供组织的敏捷转型、过程改进、 企业系统架构、领域驱动设计、⼤数据、代码质量提升、测试驱动开发等咨询与培训⼯作。 互联⽹发展对IT技术进⾏了专业细分，这在提⾼⽣产⼒的同时，也造成了领域间的壁垒。全栈⼯程师的 应运⽽⽣，看似逆向地回到了软件开发最初。这究竟是进步，还是倒退？身兼全职的全栈⼯程师⻆⾊ ⽆疑是个巨⼤的挑战。【QCon北京2015⼤会】⾸次带来“ ”专题，由朴灵担任出品⼈，带 来业内全栈探索的实践和案例。

挑战全栈开发

