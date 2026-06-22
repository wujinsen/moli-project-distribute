我们通过 KafkaUtils.createStream 函数可以创建 KafkaReceiver 类（这是默认的Kafka Receiver，如果 spark.streaming.receiver.writeAheadLog.enable 配置选项设置为true，则

会使⽤ ReliableKafkaReceiver ，其中会使⽤WAL机制来保证数据的可靠性，也就是保证数据不丢 失。）

在 KafkaReceiver 类中⾸先会在onStart⽅法中初始化⼀些环境，⽐如创建Consumer(这个就是 ⽤来从Kafka的Topic中读取消息的消费者)。在初始化完相关环境之后会在线程池中启动

MessageHandler 来从Kafka中接收数据：

/ Handles Kafka mesages

private clas MesageHandler(stream: KafkaStream[K, V]) extends Runable { def run() {

logInfo("Starting MesageHandler.") try {

val streamIterator = stream.iterator() while (streamIterator.hasNext() {

val msgAndMetadata = streamIterator.next() store(msgAndMetadata.key, msgAndMetadata.mesage)

} } catch {

case e: Throwable => logEror("Eror handling mesage; exiting", e) }

} }

该线程负责从Kafka中读取数据，并将读取到的数据存储到 BlockGenerator 中（通过调⽤store ⽅法实现）， msgAndMetadata.key 其实就是Topic的Key值；⽽ msgAndMetadata.message 就是我 们要的消息。

store函数是 Receiver 类提供的，所有继承⾃该类的⼦类(KafkaReceiver、ActorReceiver、 ReliableKafkaReceiver等)都拥有该⽅法。其内部的实现是调⽤了 blockGenerator 的adData⽅法， 最终是将数据存储在 currentBuffer 中，⽽ currentBuffer 其实就是⼀个 ArrayBuffer[Any] 。

在 blockGenerator 内部存在两个线程：（1）、定期地⽣成新的batch，然后再将之前⽣成的 batch封装成block。这⾥的定期其实就是 spark.streaming.blockInterval 参数配置的。（2）、 将⽣成的block发送到Block Manager中。

第⼀个线程定期地调⽤ updateCurrentBuffer 函数将存储在 currentBuffer 中的数据封装成 Block，然后放在 blocksForPushing 中， blocksForPushing 是 ArrayBlockingQueue[Block] 类型的队列，其⼤⼩默认是10，我们可以通过 spark.streaming.blockQueueSize 参数配置（当 然，在很多情况下这个值不需要我们去配置）。当 blocksForPushing 没有多余的空间，那么该线程 就会阻塞，直到有剩余的空间可⽤于存储新⽣成的Block。如果你的数据量真的很⼤，⼤到

blocksForPushing ⽆法及时存储那些block，这时候你就得考虑加⼤ spark.streaming.blockQueueSize 的⼤⼩了。 updateCurrentBuffer 函数的实现如下：

/* Change the bufer to which single records are aded to. */ private def updateCurentBufer(time: Long): Unit = synchronized {

try { val newBlockBufer = curentBufer curentBufer = new ArayBufer[Any] if (newBlockBufer.size > 0) {

val blockId = StreamBlockId(receiverId, time - blockInterval) val newBlock = new Block(blockId, newBlockBufer) listener.onGenerateBlock(blockId) blocksForPushing.put(newBlock) / put is blocking when queue is ful logDebug("Last element in " + blockId + " is " + newBlockBufer.last)

} } catch { case ie: InteruptedException =>

logInfo("Block updating timer thread was interupted") case e: Exception =>

reportEror("Eror in block updating thread", e) }

}

第⼆个线程不断地调⽤ keepPushingBlocks 函数从 blocksForPushing 阻塞队列中获取⽣成的 Block，然后调⽤pushBlock⽅法将Block存储到BlockManager中。当存储到 BlockManager 中后，会 返回⼀个 blockStoreResult 结果，这就是成功存储到 BlockManager 的 StreamBlockId 。然后 下⼀步就是将 blockStoreResult 封装成 ReceivedBlockInfo ，这也就是最新的未处理过的数据， 然后通过Aka告诉 ReceiverTracker 有新的块加⼊， ReceiverTracker 会调⽤adBlock⽅法将

ReceivedBlockInfo 存储到 streamIdToUnallocatedBlockQueues 队列中。关键代码如下：

/* Kep pushing blocks to the BlockManager. */ private def kepPushingBlocks() {

logInfo("Started block pushing thread") try {

while(!stoped) {

Option(blocksForPushing.pol(10, TimeUnit.MI LISECONDS) match { case Some(block) => pushBlock(block) case None =>

} }

/ Push out the blocks that are stil left logInfo("Pushing out the last " + blocksForPushing.size() + " blocks") while (!blocksForPushing.isEmpty) {

logDebug("Geting block ") val block = blocksForPushing.take() pushBlock(block) logInfo("Blocks left to push " + blocksForPushing.size()

} logInfo("Stoped block pushing thread")

} catch { case ie: InteruptedException =>

logInfo("Block pushing thread was interupted") case e: Exception =>

reportEror("Eror in block pushing thread", e) }

}

private def pushBlock(block: Block) { listener.onPushBlock(block.id, block.bufer) logInfo("Pushed block " + block.id)

}

/* Store block and report it to driver */ def pushAndReportBlock(

receivedBlock: ReceivedBlock, metadataOption: Option[Any], blockIdOption: Option[StreamBlockId]

) { val blockId = blockIdOption.getOrElse(nextBlockId)

val numRecords = receivedBlock match { case ArayBuferBlock(arayBufer) => arayBufer.size case _ => -1

}

val time = System.curentTimeMilis val blockStoreResult = receivedBlockHandler.storeBlock(blockId, receivedBlock) logDebug(s"Pushed block $blockId in ${(System.curentTimeMilis - time)} ms")

val blockInfo = ReceivedBlockInfo(streamId, numRecords, blockStoreResult) val future = trackerActor.ask(AdBlock(blockInfo)(askTimeout) Await.result(future, askTimeout) logDebug(s"Reported block $blockId")

以上就是从Kafka读取数据，并把接收到的数据存储到 streamIdToUnallocatedBlockQueues ⾥ ⾯的全过程。关于Spark Streaming如何进⼀步处理 streamIdToUnallocatedBlockQueues 中的数 据，并划分作业的流程下篇⽂章我将会继续讲解

