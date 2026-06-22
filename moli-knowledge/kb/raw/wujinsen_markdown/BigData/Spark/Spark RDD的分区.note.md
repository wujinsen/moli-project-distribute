通过⽹络资源，整理出Spark RD分区的⼀些总结。

分区是为了更好的利⽤集群中的众多CPU，提⾼并⾏度。

实际分区应该考虑处理问题的类型，如果是IO密集型，考虑等待的时间，每个CPU上对应的分区可 以适当多点，如果是计算密集型，每个CPU处理的分区就不能太多，不然相当于排队等待。是推荐 的分区⼤⼩是⼀个CPU上⾯有2-4个分区。

Spark会⾃动根据集群情况设置分区的个数。参考spark.default.paralelism参数和 defaultMinPartitions成员。

编程的时候可以通过paralelize函数设置分区数⽬(e.g. sc.paralelize(data, 10)。

对于来⾃HDFS的数据，默认⼀个块对应⼀个分区（默认快⼤⼩64M），你可以编程设置⾃⼰的分 区，但不能少于块数。

Spark每个块的⼤⼩有2G的限制。

RD的数据本地性。

很多操作会影响分区，包括cogroup, groupWith, join, leftOuterJoin, rightOuterJoin, groupByKey, reduceByKey, combineByKey, partitionBy, sort, mapValues (如果⽗RD存在partitioner), flatMapValues(如果⽗RD存在partitioner), 和 filter (如果⽗RD存在partitioner)。其他的 transform操作不会影响到输出RD的partitioner，⼀般来说是None，也就是没有partitioner

从实现上看，每个RD都有⼀个Partitioner。

