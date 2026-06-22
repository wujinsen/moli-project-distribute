- 1

- 2 /**

* Read a text file from HDFS, a local file system (available on all nodes), or any

- 3

- 4 * Hadoop-supported file system URI, and return it as an RDD of Strings.

- 5 */


- 1 //defaultMinPartitions默认2个

def textFile(path: String, minPartitions: Int = defaultMinPartitions): RDD[String] = {

- 2

- 3 assertNotStopped()

- 4 //构建HadoopRDD

- 5 hadoopFile(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text],

- 6 minPartitions).map(pair => pair._2.toString).setName(path)

- 7 }


- 1

- 2


- 1

- 2


- 1

- 2


hadopFile:

/**GetanRDDfora Hadoop ﬁlewithanarbitraryInputFormat**'''Note:''' BecauseHadoop'sRecordReaderclassre-uses thesameWritableobjectforeach*record,directlycachingthereturnedRDDordirectlypassingittoanaggregationor shufﬂe*operationwillcreatemanyreferencestothesameobject.*Ifyouplantodirectlycache,sort,oraggregateHadoop writableobjects,youshould ﬁrst*copythemusinga`map` function.*/def hadoopFile[K, V]( path: String, inputFormatClass: Class[_ <: InputFormat[K, V]], keyClass: Class[K], valueClass: Class[V], minPartitions: Int = defaultMinPartitions ): RDD[(K, V)] = { assertNotStopped() // AHadoopconﬁgurationcanbeabout10KB,whichis prettybig,sobroadcastit. val confBroadcast = broadcast(new SerializableWritable(hadoopConﬁguration)) val setInputPathsFunc = (jobConf: JobConf) => FileInputFormat.setInputPaths(jobConf, path) new HadoopRDD( this, confBroadcast, Some(setInputPathsFunc), inputFormatClass, keyClass, valueClass, minPartitions).setName(path)}

