# mapPartitions

def mapPartitions[U](f: (Iterator[T]) => Iterator[U], preservesPartitioning: Bolean = false)(implicit arg0: ClasTag[U]): RD[U] 该函数和map函数类似，只不过映射函数的参数由RD中的每⼀个元素变成了RD中每⼀个分区的迭代器。 如果在映射的过程中需要频繁创建额外的对象，使⽤mapPartitions要⽐map⾼效的过。 ⽐如，将RD中的所有数据通过JDBC连接写⼊数据库，如果使⽤map函数，可能要为每⼀个元素都创建⼀个 conection，这样开销很⼤，如果使⽤mapPartitions，那么只需要针对每⼀个分区建⽴⼀个conection。 参数preservesPartitioning表示是否保留⽗RD的partitioner分区信息。

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


var rdd1 = sc.makeRDD(1 to 5,2) //rdd1有两个分区 scala> var rdd3 = rdd1.mapPartitions{ x => {

| var result = List[Int]() | var i = 0 | while(x.hasNext){ | i += x.next() | } | result.::(i).iterator | }}

rdd3: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[84] at mapPartitions at :23

//rdd3将rdd1中每个分区中的数值累加 scala> rdd3.collect

- res65: Array[Int] = Array(3, 12) scala> rdd3.partitions.size

- res66: Int = 2


mapPartitionsWithIndex

def mapPartitionsWithIndex[U](f: (Int, Iterator[T]) => Iterator[U], preservesPartitioning: Bolean = false) (implicit arg0: ClasTag[U]): RD[U] 函数作⽤同mapPartitions，不过提供了两个参数，第⼀个参数为分区的索引。

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


var rdd1 = sc.makeRDD(1 to 5,2) //rdd1有两个分区 var rdd2 = rdd1.mapPartitionsWithIndex{

(x,iter) => {

var result = List[String]() var i = 0 while(iter.hasNext){ i += iter.next()

} result.::(x + "|" + i).iterator

}

} //rdd2将rdd1中每个分区的数字累加，并在每个分区的累加结果前⾯加了分区索引 scala> rdd2.collect res13: Array[String] = Array(0|3, 1|12)

