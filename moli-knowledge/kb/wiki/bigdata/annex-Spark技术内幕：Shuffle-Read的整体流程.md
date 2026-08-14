---
title: Spark技术内幕：Shuffle Read的整体流程.note（原文插图 annex）
slug: annex-Spark技术内幕：Shuffle-Read的整体流程
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Shuffle Read的整体流程.note.md
related: [spark-核心概念与实践]
created: 2026-07-05
updated: 2026-07-05
---

2015-01-1208 079041⼈阅读 Spark技术内幕：Shufle Read的整体流程 Spark架构 探索

分类：

(15)

评论 收藏举报 SparkShufleShufle Read

(?)[+]

⽬录 回忆⼀下，每个Stage的上边界，要么需要从外部存储读取数据，要么需要读取上⼀个Stage的输出； ⽽下边界，要么是需要写⼊本地⽂件系统（需要Shufle），以供childStage读取，要么是最后⼀个 Stage，需要输出结果。这⾥的Stage，在运⾏时的时候就是可以以pipeline的⽅式运⾏的⼀组Task， 除了最后⼀个Stage对应的是ResultTask，其余的Stage对应的都是ShufleMap Task。 ⽽除了需要从外部存储读取数据和RD已经做过cache或者checkpoint的Task，⼀般Task的开始都是 从ShufledRD的ShufleRead开始的。本节将详细讲解Shufle Read的过程。 先看⼀下ShufleRead的整体架构图。

![image 1](assets/imageFile1.png)

org.apache.spark.rd.ShufledRD#compute 开始，通过调⽤ org.apache.spark.shufle.ShufleManager的getReader⽅法，获取到 org.apache.spark.shufle.ShufleReader，然后调⽤其read()⽅法进⾏读取。在Spark1.2.0中，不管是 Hash BasedShufle或者是Sort BasedShufle，内置的Shufle Reader都是 org.apache.spark.shufle.hash.HashShufleReader。核⼼实现： [java]view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
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
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.


overide def read(): Iterator[Product2[K, C] = { val ser =Serializer.getSerializer(dep.serializer)

/ 获取结果 val iter = BlockStoreShufleFetcher.fetch(handle.shufleId,startPartition, context, ser) / 处理结果 val agregatedIter: Iterator[Product2[K, C] = if(dep.agregator.isDefined) {/需要聚合 if (dep.mapSideCombine) {/需要map side的聚合 new InteruptibleIterator(context, dep.agregator.get.combineCombinersByKey( iter, context) } else {/只需要reducer端的聚合 new InteruptibleIterator(context,dep.agregator.get.combineValuesByKey( iter, context)

}

}else { / ⽆需聚合操作

iter.asInstanceOf[Iterator[Product2[K,C].map(pair => (pair._1, pair._2) }

/ Sort the output if there is a sort ordering defined. dep.keyOrdering match {/判断是否需要排序

case Some(keyOrd: Ordering[K]) => /对于需要排序的情况 / 使⽤ExternalSorter进⾏排序，注意如果spark.shufle.spil是false，那么数据是 / 不会spil到硬盘的

val sorter = new ExternalSorter[K, C, C](ordering = Some(keyOrd),

serializer= Some(ser) sorter.insertAl(agregatedIter) context.taskMetrics.memoryBytesSpiled += sorter.memoryBytesSpiled context.taskMetrics.diskBytesSpiled += sorter.diskBytesSpiled sorter.iterator

case None => /⽆需排序 agregatedIter

- 32.
- 33.


} }

org.apache.spark.shufle.hash.BlockStoreShufleFetcher#fetch会获得数据，它⾸先会通过 org.apache.spark.MapOutputTracker#getServerStatuses来获得数据的meta信息，这个过程有可能 需要向org.apache.spark.MapOutputTrackerMasterActor发送读请求，这个读请求是在 org.apache.spark.MapOutputTracker#askTracker发出的。在获得了数据的meta信息后，它会将这些 数据存⼊Seq[(BlockManagerId,Seq[(BlockId, Long)])]中，然后调⽤ org.apache.spark.storage.ShufleBlockFetcherIterator最终发起请求。ShufleBlockFetcherIterator 根据数据的本地性原则进⾏数据获取。如果数据在本地，那么会调⽤ org.apache.spark.storage.BlockManager#getBlockData进⾏本地数据块的读取。⽽getBlockData对 于shufle类型的数据，会调⽤ShufleManager的ShufleBlockManager的getBlockData。 如果数据在其他的Executor上，那么如果⽤户使⽤的spark.shufle.blockTransferService是nety，那 么就会通过org.apache.spark.network.nety.NetyBlockTransferService#fetchBlocks获取；如果使⽤ 的是nio，那么就会通过org.apache.spark.network.nio.NioBlockTransferService#fetchBlocks获取。

# 数据读取策略的划分

org.apache.spark.storage.ShufleBlockFetcherIterator会通过splitLocalRemoteBlocks划分数据的读 取策略：如果在本地有，那么可以直接从BlockManager中获取数据；如果需要从其他的节点上获取， 那么需要⾛⽹络。由于Shufle的数据量可能会很⼤，因此这⾥的⽹络读有以下的策略：

- 1) 每次最多启动5个线程去最多5个节点上读取数据
- 2) 每次请求的数据⼤⼩不会超过spark.reducer.maxMbInFlight(默认值为48MB)/5 这样做的原因有⼏个：


- 1)避免占⽤⽬标机器的过多带宽，在千兆⽹卡为主流的今天，带宽还是⽐较重要的。如果机器使⽤的 万兆⽹卡，那么可以通过设置spark.reducer.maxMbInFlight来充分利⽤带宽。
- 2)请求数据可以平⾏化，这样请求数据的时间可以⼤⼤减少。请求数据的总时间就是请求中耗时最⻓ 的。这样可以缓解⼀个节点出现⽹络拥塞时的影响。 主要的实现： [java]


view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


private[this] def splitLocalRemoteBlocks():ArayBufer[FetchRequest] = { val targetRequestSize = math.max(maxBytesInFlight / 5, 1L) val remoteRequests = new ArayBufer[FetchRequest] for(adres, blockInfos) <- blocksByAdres) {

if (adres.executorId = blockManager.blockManagerId.executorId) { / Block在本地，需要过滤⼤⼩为0的block。

- 7.
- 8.
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
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.


localBlocks += blockInfos.filter(_._2 != 0).map(_._1) numBlocksToFetch += localBlocks.size

} else { /需要远程获取的Block val iterator = blockInfos.iterator

var curRequestSize = 0L var curBlocks = new ArayBufer[(BlockId, Long)] while (iterator.hasNext) {

/blockId 是org.apache.spark.storage.ShufleBlockId， / 格式："shufle_" +shufleId + "_" + mapId + "_" + reduceId

val (blockId, size) = iterator.next() / Skip empty blocks

if (size > 0) { curBlocks +=(blockId, size) remoteBlocks += blockId numBlocksToFetch += 1 curRequestSize += size

}

if (curRequestSize >= targetRequestSize) {

/ 当前总的size已经可以批量放⼊⼀次request中 remoteRequests += new FetchRequest(adres, curBlocks) curBlocks = new ArayBufer[(BlockId, Long)] curRequestSize = 0

} }

/ 剩余的请求组成⼀次request if (curBlocks.nonEmpty) {

remoteRequests += new FetchRequest(adres, curBlocks) }

} }

remoteRequests }

本地读取

fetchLocalBlocks() 负责本地Block的获取。在splitLocalRemoteBlocks中，已经将本地的Block列表存 ⼊了localBlocks：private[this] val localBlocks = newArayBufer[BlockId]() 具体过程如下： [java]

view plaincopy val iter = localBlocks.iterator

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.


while (iter.hasNext) { val blockId = iter.next() try {

val buf = blockManager.getBlockData(blockId) shufleMetrics.localBlocksFetched += 1 buf.retain() results.put(new SucesFetchResult(blockId, 0, buf)

} catch { }

}

⽽blockManager.getBlockData(blockId)的实现是： [java]

view plaincopy overide def getBlockData(blockId:BlockId): ManagedBufer = { if (blockId.isShufle) {

- 1.
- 2.
- 3.
- 4.


shufleManager.shufleBlockManager.getBlockData(blockId.asInstanceOf[ShufleBlockId]) }这就调⽤了ShufleBlockManager的getBlockData⽅法。在Shufle Plugable框架中我们介绍了 实现⼀个Shufle Service之⼀就是要实现ShufleBlockManager。

以Hash BasedShufle为例，它的ShufleBlockManager是 org.apache.spark.shufle.FileShufleBlockManager。FileShufleBlockManager有两种情况，⼀种是 File consolidate的，这种的话需要根据Map ID和 Reduce ID⾸先获得FileGroup的⼀个⽂件，然后根据 在⽂件中的ofset和size来获取需要的数据；如果是没有File consolidate，那么直接根据Shufle Block ID直接读取整个⽂件就可以。 [java]

view plaincopy overide def getBlockData(blockId:ShufleBlockId): ManagedBufer = {

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


if (consolidateShufleFiles) { val shufleState = shufleStates(blockId.shufleId) val iter = shufleState.alFileGroups.iterator

while(iter.hasNext) {

/ 根据Map ID和Reduce ID获取File Segment的信息 val segmentOpt = iter.next.getFileSegmentFor(blockId.mapId,blockId.reduceId) if (segmentOpt.isDefined) {

val segment = segmentOpt.get

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


/ 根据File Segment的信息，从FileGroup中找到相应的File和Block在 / ⽂件中的ofset和size returnnew FileSegmentManagedBufer(

transportConf, segment.file, segment.ofset, segment.length) }

} thrownew IlegalStateException("Failed to find shufle block:" + blockId)

}else { val file = blockManager.diskBlockManager.getFile(blockId) /直接获取⽂件句柄 new FileSegmentManagedBufer(transportConf, file, 0, file.length)

} }

对于Sort BasedShufle，它需要通过索引⽂件来获得数据块在数据⽂件中的具体位置信息，从⽽读取 这个数据。 具体实现在org.apache.spark.shufle.IndexShufleBlockManager#getBlockData中。 [java]

view plaincopy overide def getBlockData(blockId: ShufleBlockId): ManagedBufer = {

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.


/ 根据ShufleID和MapID从org.apache.spark.storage.DiskBlockManager 获取索引⽂件 val indexFile = getIndexFile(blockId.shufleId, blockId.mapId) val in = new DataInputStream(new FileInputStream(indexFile) try {

ByteStreams.skipFuly(in, blockId.reduceId * 8) /跳到本次Block的数据区 val ofset = in.readLong() /数据⽂件中的开始位置 val nextOfset = in.readLong() /数据⽂件中的结束位置 new FileSegmentManagedBufer(

transportConf, getDataFile(blockId.shufleId, blockId.mapId), ofset, nextOfset - ofset)

}finaly {

in.close() }

}
