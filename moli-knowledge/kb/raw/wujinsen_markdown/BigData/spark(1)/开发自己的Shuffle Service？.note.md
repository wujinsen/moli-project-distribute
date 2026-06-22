# Spark技术内幕：ShuflePlugable框架详解，你怎么开发⾃ ⼰的ShufleService？

分类： 2015-01-08 07 58 8171⼈阅读

Spark云计算

(5)

评论 收藏 举报 Sparkshufle

⽬录(?) ⾸先介绍⼀下需要实现的接⼝。框架的类图如图所示（今天CSDN抽⻛，竟然上传不了图⽚。如果需要 实现新的Shufle机制，那么需要实现这些接⼝。

[+]

![image 1](<开发自己的Shuffle Service？.note_images/imageFile1.png>)

## 1.1.1 org.apache.spark.shufle.ShufleManager

Driver和每个Executor都会持有⼀个ShufleManager，这个ShufleManager可以通过配置项 spark.shufle.manager指定，并且由SparkEnv创建。Driver中的ShufleManager负责注册Shufle的元 数据，⽐如Shufle ID，map task的数量等。Executor中的ShufleManager 则负责读和写Shufle的数 据。 需要实现的函数及其功能说明：

- 1) 由Driver注册元数据信息 defregisterShufle[K, V, C](


shufleId: Int, numMaps: Int,

dependency:ShufleDependency[K, V, C]): ShufleHandle

⼀般如果没有特殊的需求，可以使⽤下⾯的实现，实际上Hash BasedShufle 和Sort BasedShufle都 是这么实现的。

overide def registerShufle[K, V, C](

shufleId: Int, numMaps: Int, dependency: ShufleDependency[K, V, C]):ShufleHandle = {

new BaseShufleHandle(shufleId, numMaps,dependency) }

- 2) 获得Shufle Writer， 根据Shufle Map Task的ID为其创建Shufle Writer。 def getWriter[K, V](handle: ShufleHandle, mapId: Int, context:TaskContext): ShufleWriter[K, V]
- 3) 获得Shufle Reader，根据Shufle ID和partition的ID为其创建ShufleReader。 def getReader[K, C](

handle: ShufleHandle, startPartition: Int, endPartition: Int, context: TaskContext): ShufleReader[K,C]

- 4) 为数据成员shufleBlockManager赋值，以保存实际的ShufleBlockManager
- 5) defunregisterShufle(shufleId: Int): Bolean，删除本地的Shufle的元数据。
- 6) def stop(): Unit，停⽌Shufle Manager。 每个接⼝的具体实现的例⼦，可以参照org.apache.spark.shufle.sort.SortShufleManager 和 org.apache.spark.shufle.hash.HashShufleManager。


- 1.1.2 org.apache.spark.shufle.ShufleWriter
- 1.1.3 org.apache.spark.shufle.ShufleBlockManager


Shufle Map Task通过ShufleWriter将Shufle数据写⼊本地。这个Writer主要通过 ShufleBlockManager来写⼊数据，因此它的功能是⽐较轻量级的。

- 1) def write(records: Iterator[_ <:Product2[K, V]): Unit， 写⼊所有的数据。需要注意的是如果 需要在Map端做聚合。（agregate），那么写⼊前需要将records做聚合。
- 2) def stop(suces: Bolean): Option[MapStatus]，写⼊完成后提交本次写⼊。 对于Hash BasedShufle，请查看org.apache.spark.shufle.hash.HashShufleWriter；对于Sort Based Shufle，请查看org.apache.spark.shufle.sort.SortShufleWriter。


主要使⽤从本地读取Shufle数据的功能。这些接⼝都是通过org.apache.spark.storage.BlockManager 调⽤的。

- 1) def getBytes(blockId: ShufleBlockId):Option[ByteBufer], ⼀般通过调⽤下⼀个接⼝实现，只 不过将ManagedBufer转换成了ByteBufer。


- 2) def getBlockData(blockId:ShufleBlockId): ManagedBufer，核⼼读取逻辑。⽐如Hash Based Shufle的从本地读取⽂件都是通过这个接⼝实现的。因为不同的实现可能⽂件的组织⽅式是不⼀样 的，⽐如Sort Based Shufle需要通过先读取Index索引⽂件获得每个partition的起始位置后，才能读取 真正的数据⽂件。
- 3) def stop(): Unit，停⽌该Manager。 对于Hash Based Shufle，请查看org.apache.spark.shufle.FileShufleBlockManager；对于Sort Based Shufle，请查看org.apache.spark.shufle.IndexShufleBlockManager。


## 1.1.4 org.apache.spark.shufle.ShufleReader

ShufleReader实现了下游的Task如何读取上游的ShufleMapTask的Shufle输出的逻辑。这个逻辑⽐ 较复杂，简单来说就是通过org.apache.spark.MapOutputTracker获得数据的位置信息，然后如果数据 在本地那么调⽤org.apache.spark.storage.BlockManager的getBlockData读取本地数据（实际上 getBlockData最终会调⽤org.apache.spark.shufle.ShufleBlockManager的getBlockData）。具体的 Shufle Read的逻辑请查看下⾯的章节。

1) def read():Iterator[Product2[K, C]

如何开发⾃⼰的Shufle机制？到这⾥你应该知道怎么做了。不知道？ 再看⼀遍吧。

