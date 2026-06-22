# Spark技术内幕: Shufle详解（⼀）

通过上⾯⼀系列⽂章，我们知道在集群启动时，在Standalone模式下，Worker会向Master注册，使得 Master可以感知进⽽管理整个集群；Master通过借助ZK，可以简单的实现HA；⽽应⽤⽅通过 SparkContext这个与集群的交互接⼝，在创建SparkContext时就完成了Aplication的注册，Master为 其分配Executor；在应⽤⽅创建了RD并且在这个RD上进⾏了很多的Transformation后，触发 action，通过DAGScheduler将DAG划分为不同的Stage后，将Stage转换为TaskSet交给 TaskSchedulerImpl；TaskSchedulerImpl通过SparkDeploySchedulerBackend的reviveOfers，最终 向ExecutorBackend发送LaunchTask的消息；ExecutorBackend接收到消息后，启动Task，开始在集 群中启动计算。 接下来，会介绍⼀些更详细的细节实现。 Shufle，⽆疑是性能调优的⼀个重点，本⽂将从源码实现的⻆度，深⼊解析Spark Shufle的实现细 节。 每个Stage的上边界，要不是需要从外部存储读取数据，要么需要读取上⼀个Stage的输出；⽽下边 界，要么是需要写⼊本地⽂件系统，以供child Stage读取，要么是ResultTask，需要输出结果了。 ⾸先从org.apache.spark.rd.ShufledRD开始, 因为ShufledRD是⼀个Stage的开始，它需要获取 上⼀个Stage的输出结果，然后进⾏接下来的运算。那么这个数据获取是如何实现的？顺着 ShufledRD的实现，我们可以理清这条线。⾸先可以看⼀下compute是如何实现的。 [java]

view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


overide def compute(split: Partition, context: TaskContext): Iterator[(K, C)] = { val dep = dependencies.head.asInstanceOf[ShufleDependency[K, V, C] SparkEnv.get.shufleManager.getReader(dep.shufleHandle, split.index, split.index + 1, contex

t)

.read()

.asInstanceOf[Iterator[(K, C)] }

它需要从ShufleManager获取shufleReader，然后读取数据进⾏计算。看⼀下shufleManager： [java]

view plaincopy / Let the user specify short names for shufle managers

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


val shortShufleMgrNames = Map( "hash" -> "org.apache.spark.shufle.hash.HashShufleManager", "sort" -> "org.apache.spark.shufle.sort.SortShufleManager") val shufleMgrName = conf.get("spark.shufle.manager", "hash") val shufleMgrClas = shortShufleMgrNames.getOrElse(shufleMgrName.toLowerCase, shuf

fleMgrName) val shufleManager = instantiateClas[ShufleManager](shufleMgrClas)

ShufleManager分为hash和sort，hash是默认的，即Shufle时不排序。熟悉MapReduce的同学都知 道，MapReduce是⽆论如何都要排序的，即到Reduce端的都是已经排序好的，当然这么做也是为了可 以处理海量的数据。在Spark1.1之前，只⽀持hash based的Shufle，sort based Shufle是1.1新加⼊的 实验功能。 hash顾名思义，在Reduce时的数据需要求有序，因此可以在Reduce获得了数据后，⽴即进⾏处理； ⽽不需要等待所有的数据都得到后再处理。这个接下来会通过源码进⾏解释。⽽sort，意味着排序，实 际上对于sortByKey这种转换可能sort是更有意义的。 ShufledRD是通过org.apache.spark.shufle.hash.HashShufleReader获取上⼀个Stage的结果。⽽ HashShufleReader通过org.apache.spark.shufle.hash.BlockStoreShufleFetcher$#fetch来获取结 果。⽽fetch通过调⽤org.apache.spark.storage.BlockManager#getMultiple来转发请求： [java]

view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


def getMultiple( blocksByAdres: Seq[(BlockManagerId, Seq[(BlockId, Long)])], serializer: Serializer, readMetrics: ShufleReadMetrics): BlockFetcherIterator = {

val iter = new BlockFetcherIterator.BasicBlockFetcherIterator(this, blocksByAdres, serialize r,

readMetrics) iter.initialize() iter

}

⽽最终的实现在org.apache.spark.storage.BlockFetcherIterator.BasicBlockFetcherIterator#initialize 中， [java]

view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


overide def initialize() { / Split local and remote blocks. / 获得需要远程请求的数据列表，并且将已经在本地的数据的blockid放在localBlocksToFetch

中，

/ 并且在 org.apache.spark.storage.BlockFetcherIterator.BasicBlockFetcherIterator.getLocalBlocks进⾏ 本地读取

val remoteRequests = splitLocalRemoteBlocks()

/ Ad the remote requests into our queue in a random order fetchRequests += Utils.randomize(remoteRequests)

- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.


/ Send out initial requests for blocks, up to our maxBytesInFlight while (!fetchRequests.isEmpty & /保证占⽤内存不超过设定的值

spark.reducer.maxMbInFlight，默认值是48M (bytesInFlight = 0| bytesInFlight + fetchRequests.front.size <= maxBytesInFlight) { sendRequest(fetchRequests.dequeue()

}

val numFetches = remoteRequests.size - fetchRequests.size logInfo("Started " + numFetches + " remote fetches in" + Utils.getUsedTimeMs(startTime)

/ Get Local Blocks startTime = System.curentTimeMilis getLocalBlocks() / 从本地获取 logDebug("Got local blocks in " + Utils.getUsedTimeMs(startTime) + " ms")

}

具体获取如何获取的策略都在 org.apache.spark.storage.BlockFetcherIterator.BasicBlockFetcherIterator#splitLocalRemoteBlocks 中。这个会在下⼀篇博⽂中详解。

