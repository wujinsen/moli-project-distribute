摘要：1 checkpoint （实质将RD写进disk举⾏做检讨⾯）checkpoint是为了lineage做辅佐，⾎缘太 ⻓会形成容错本钱太⾼，如许的话便没有如往中央做先checkpoint然后⾎缘从checkpoint最先算起 正 在RD private[sp]

1 checkpoint （本质将RD写⼊disk进⾏做检查点）checkpoint是为了lineage做辅助，⾎统过⻓会造 成容错成本过⾼，这样的话就不如去中间做先checkpoint然后⾎统从checkpoint开始算起

在RD private[spark] def doCheckpoint() {

if (!doCheckpointCaled) { doCheckpointCaled = true if (checkpointData.isDefined) {

checkpointData.get.doCheckpoint() } else {

dependencies.foreach(_.rd.doCheckpoint() }

} }

在RDCheckpointData中

def doCheckpoint() { / If it is marked for checkpointing AND checkpointing is not already in progres, / then set it to be in progres, else return

RDCheckpointData.synchronized {

if (cpState = MarkedForCheckpoint) { cpState = CheckpointingInProgres } else {

return }

}

/ Create the output path for the checkpoint val path = new Path(rd.context.checkpointDir.get, "rd-" + rd.id) val fs = path.getFileSystem(new Configuration() if (!fs.mkdirs(path) {

throw new SparkException("Failed to create checkpoint path " + path)

}

/ Save to file, and reload it as an RD此处重要 rd.context.runJob(rd, CheckpointRD.writeToFile(path.toString) _) val newRD = new CheckpointRD[T](rd.context, path.toString)

/ Change the dependencies and partitions of the RD

RDCheckpointData.synchronized { cpFile = Some(path.toString) cpRD = Some(newRD) rd.markCheckpointed(newRD) / Update the RD's dependencies and partitions cpState = Checkpointed RDCheckpointData.clearTaskCaches() logInfo("Done checkpointing RD " + rd.id + ", new parent is RD " + newRD.id)

} }

在CheckPointRD

def writeToFile[T](path: String, blockSize: Int = -1)(ctx: TaskContext, iterator: Iterator[T]) { val env = SparkEnv.get val outputDir = new Path(path)

/本质相当于在hadop 的 分布式⽂件系统将RD写进去 val fs = outputDir.getFileSystem(env.hadop.newConfiguration() val finalOutputName = splitIdToFile(ctx.splitId) val finalOutputPath = new Path(outputDir, finalOutputName) val tempOutputPath = new Path(outputDir, "." + finalOutputName + "-atempt-" + ctx.atemptId) if (fs.exists(tempOutputPath) {

throw new IOException("Checkpoint failed: temporary path " +

tempOutputPath + " already exists") }

/此处体现 缓冲区⼤⼩的设置，根据数据量不同设置不同的缓冲区⼤⼩,即为 写⼊hdfs 的使⽤的缓冲

区⼤⼩ val buferSize = System.getProperty("spark.bufer.size", "6536").toInt val fileOutputStream = if (blockSize < 0) {

fs.create(tempOutputPath, false, buferSize)

} else { / This is mainly for testing purpose

fs.create(tempOutputPath, false, buferSize, fs.getDefaultReplication, blockSize)

} val serializer = env.serializer.newInstance() val serializeStream = serializer.serializeStream(fileOutputStream)

/此处为写⼊操作 关键在iterator上相当于将iteraor迭代器的对象序列化写到hdfs中与数据库的检查点

略不相同 serializeStream.writeAl(iterator) serializeStream.close() if (!fs.rename(tempOutputPath, finalOutputPath) {

if (!fs.exists(finalOutputPath) { logInfo("Deleting tempOutputPath " + tempOutputPath) fs.delete(tempOutputPath, false) throw new IOException("Checkpoint failed: failed to save output of task: "

+ ctx.atemptId + " and final output path does not exist") } else {

/ Some other copy of this task must've finished before us and renamed it logInfo("Final output path " + finalOutputPath + " already exists; not overwriting it") fs.delete(tempOutputPath, false)

} }

}

/写⼊操作

trait SerializationStream { def writeObject[T](t: T): SerializationStream def flush(): Unit def close(): Unit def writeAl[T](iter: Iterator[T]): SerializationStream = {

while (iter.hasNext) { writeObject(iter.next()

} this

} }

kryo的序列化

private[spark] clas KryoSerializationStream(kryo: Kryo, outStream: OutputStream) extends SerializationStream {

val output = new KryoOutput(outStream) def writeObject[T](t: T): SerializationStream = {

kryo.writeClasAndObject(output, t) this

} def flush() { output.flush() } def close() { output.close() }

}

