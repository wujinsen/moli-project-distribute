DirectKafkaInputDStream

spark.streaming.kafka.maxRatePerPartition

- * The spark configuration spark.streaming.kafka.maxRatePerPartition gives the maximum number
- * of mesages
- * per second that each'partition' wil acept.
- * Starting ofsets are specified in advance,
- * and this DStream is not responsible for comiting ofsets,
- * so that you can control exactly-once semantics.


# Approach 2: Direct Approach (No Receivers)

This new receiver-less “direct” approach has been introduced in Spark 1.3 to ensure stronger end-to-end guarantees. Instead of using receivers to receive data, this approach periodically queries Kafka for the latest offsets in each topic+partition, and accordingly defines the offset ranges to process in each batch. When the jobs to process the data are launched, Kafka’s simple consumer API is used to read the defined ranges of offsets from Kafka (similar to read files from a file system). Note that this is an experimental feature introduced in Spark 1.3 for the Scala and Java API. Spark 1.4 added a Python API, but it is not yet at full feature parity.

This approach has the following advantages over the receiver-based approach (i.e. Approach 1).

Simplified Parallelism: No need to create multiple input Kafka streams and union them. With directStream, Spark Streaming will create as many RDD partitions as there are Kafka partitions to consume, which will all read data from Kafka in parallel. So there is a one-to-one mapping between Kafka and RDD partitions, which is easier to understand and tune.

Efficiency: Achieving zero-data loss in the first approach required the data to be stored in a Write Ahead Log, which further replicated the data. This is actually inefficient as the data effectively gets replicated twice - once by Kafka, and a second time by the Write Ahead Log. This second approach eliminates the problem as there is no receiver, and hence no need for Write Ahead Logs. As long as you have sufficient Kafka retention, messages can be recovered from Kafka. Exactly-once semantics: The first approach uses Kafka’s high level API to store consumed offsets in Zookeeper. This is traditionally the way to consume data from Kafka. While this approach (in combination with write ahead logs) can ensure zero data loss (i.e. at-least once semantics), there is a small chance some records may get consumed twice under some failures. This occurs because of inconsistencies between data reliably received by Spark Streaming and offsets tracked by Zookeeper. Hence, in this second approach, we use simple Kafka API that does not use Zookeeper. Offsets are tracked by Spark Streaming within its checkpoints. This eliminates inconsistencies between Spark Streaming and Zookeeper/Kafka, and so each record is received by Spark Streaming effectively exactly once despite failures. In order to achieve exactly-once semantics for output of your results, your output operation that saves the data to an external data store must be either idempotent, or an atomic transaction that saves results and offsets (see

Sem anitcs of output operations

in the main programming guide for further information).

Note that one disadvantage of this approach is that it does not update offsets in Zookeeper, hence Zookeeper-based Kafka monitoring tools will not show progress. However, you can access the offsets processed by this approach in each batch and update Zookeeper yourself (see below).

Next, we discuss how to use this approach in your streaming application.

- 1.
- 2.


Linking: This approach is supported only in Scala/Java application. Link your SBT/Maven project with the following artifact (see in the main programming guide for further information).

Linking section

groupId = org.apache.spark artifactId = spark-streaming-kafka_2.10 version = 1.4.1

Programming: In the streaming application code, import KafkaUtils and create an input DStream as follows.

Scala Java Python

import org.apache.spark.streaming.kafka._

val directKafkaStream = KafkaUtils.createDirectStream[ [key class], [value class], [key decoder class], [value decoder class] ]( streamingContext, [map of Kafka parameters], [set of topics to consume])

See the and the .

API docs example

In the Kafka parameters, you must specify either metadata.broker.list or bootstrap.servers. By default, it will start consuming from the latest offset of each Kafka partition. If you set configuration auto.offset.reset in Kafka parameters to smallest, then it will start consuming from the smallest offset.

You can also start consuming from any arbitrary offset using other variations of KafkaUtils.createDirectStream. Furthermore, if you want to access the Kafka offsets consumed in each batch, you can do the following.

## Scala Java Python

// Hold a reference to the current offset ranges, so it can be used downstream var offsetRanges = Array[OffsetRange]()

directKafkaStream.transform { rdd => offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges rdd }.map {

... }.foreachRDD { rdd => for (o <- offsetRanges) {

println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}") }

... }

You can use this to update Zookeeper yourself if you want Zookeeper-based Kafka monitoring tools to show progress of the streaming application.

Note that the typecast to HasOffsetRanges will only succeed if it is done in the first method called on the directKafkaStream, not later down a chain of methods. You can use transform() instead of foreachRDD() as your first method call in order to access offsets, then call further Spark methods. However, be aware that the one-to-one mapping between RDD partition and Kafka partition does not remain after any methods that shuffle or repartition, e.g. reduceByKey() or window().

Another thing to note is that since this approach does not use Receivers, the standard receiverrelated (that is, of the formspark.streaming.receiver.* ) will not apply to the input DStreams created by this approach (will apply to other input DStreams though). Instead, use the

configurations

spark.streaming.kafka.*. An important one is spark.streaming.kafka.maxRatePerPartition which is the maximum rate (in messages per second) at which each Kafka partition will be read by this direct API.

configurations

- 3. Deploying: This is same as the first approach, for Scala, Java and Python.


