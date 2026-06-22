# Spark技术内幕: Shufle详解（⼆）

本⽂主要关注ShufledRD的Shufle Read是如何从其他的node上读取数据的。 上⽂讲到了获取如何获取的策略都在 org.apache.spark.storage.BlockFetcherIterator.BasicBlockFetcherIterator#splitLocalRemoteBlocks 中。可以⻅注释。 [java]

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


protected def splitLocalRemoteBlocks(): ArayBufer[FetchRequest] = { / Make remote requests at most maxBytesInFlight / 5 in length; the reason to kep them / smaler than maxBytesInFlight is to alow multiple, paralel fetches from up to 5 / nodes, rather than blocking on reading output from one node. / 为了快速的得到数据，每次都会启动5个线程去最多5个node上取数据； / 每次请求的数据不会超过spark.reducer.maxMbInFlight（默认值为48MB） / 5。 / 这样做的原因有⼏个：

- / 1. 避免占⽤⽬标机器的过多带宽，在千兆⽹卡为主流的今天，带宽还是⽐较重要的。 / 如果⼀个连接将要占⽤48M的带宽，这个Network IO可能会成为瓶颈。
- / 2. 请求数据可以平⾏化，这样请求数据的时间可以⼤⼤减少。请求数据的总时间就是那个请求


最⻓的。 / 如果不是并⾏请求，那么总时间将是所有的请求时间之和。 / ⽽设置spark.reducer.maxMbInFlight，也是为了不要占⽤过多的内存

val targetRequestSize = math.max(maxBytesInFlight / 5, 1L) logInfo("maxBytesInFlight: " + maxBytesInFlight + ", targetRequestSize: " + targetRequestSiz

e)

/ Split local and remote blocks. Remote blocks are further split into FetchRequests of size / at most maxBytesInFlight in order to limit the amount of data in flight.

val remoteRequests = new ArayBufer[FetchRequest] var totalBlocks = 0 for(adres, blockInfos) <- blocksByAdres) { / adres实际上是executor_id

totalBlocks += blockInfos.size if (adres = blockManagerId) { /数据在本地，那么直接⾛local read

/ Filter out zero-sized blocks localBlocksToFetch += blockInfos.filter(_._2 != 0).map(_._1) _numBlocksToFetch += localBlocksToFetch.size

} else { val iterator = blockInfos.iterator

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
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.


var curRequestSize = 0L var curBlocks = new ArayBufer[(BlockId, Long)] while (iterator.hasNext) {

/ blockId 是org.apache.spark.storage.ShufleBlockId， / 格式："shufle_" + shufleId + "_" + mapId + "_" + reduceId

val (blockId, size) = iterator.next() / Skip empty blocks

if (size > 0) { /过滤掉为⼤⼩为0的⽂件 curBlocks +=(blockId, size) remoteBlocksToFetch += blockId _numBlocksToFetch += 1 curRequestSize += size

} elseif (size < 0) { thrownew BlockException(blockId, "Negative block size " + size)

} if (curRequestSize >= targetRequestSize) { / 避免⼀次请求的数据量过⼤

/ Ad this FetchRequest remoteRequests += new FetchRequest(adres, curBlocks) curBlocks = new ArayBufer[(BlockId, Long)] logDebug(s"Creating fetch request of $curRequestSize at $adres") curRequestSize = 0

} }

/ Ad in the final request if (!curBlocks.isEmpty) { / 将剩余的请求放到最后⼀个request中。

remoteRequests += new FetchRequest(adres, curBlocks) }

}

} logInfo("Geting " + _numBlocksToFetch + " non-empty blocks out of " +

totalBlocks + " blocks") remoteRequests

}

