# map

map是对RD中的每个元素都执⾏⼀个指定的函数来产⽣⼀个新的RD。 任何原RD中的元素在新 RD中都有且只有⼀个元素与之对应。 举例：

- 1 scala> val a = sc.parallelize(1 to 9, 3)

- 2 scala> val b = a.map(x => x*2)

- 3 scala> a.collect

- 4 res10: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9)

- 5 scala> b.collect

- 6 res11: Array[Int] = Array(2, 4, 6, 8, 10, 12, 14, 16, 18)

- 7


上述例⼦中把原RD中每个元素都乘以2来产⽣⼀个新的RD。

# mapPartitions

mapPartitions是map的⼀个变种。map的输⼊函数是应⽤于RD中每个元素，⽽mapPartitions的输⼊ 函数是应⽤于每个分区，也就是把每个分区中的内容作为整体来处理的。 它的函数定义为：

def mapPartitions[U: ClassTag](f: Iterator[T] => Iterator[U], preservesPartitioning: Boolean = false): RDD[U]

- 1

- 2


f即为输⼊函数，它处理每个分区⾥⾯的内容。每个分区中的内容将以Iterator[T]传递给输⼊函数f，f的 输出结果是Iterator[U]。最终的RD由所有分区经过输⼊函数处理后的结果合并起来的。 举例：

- 1 scala> val a = sc.parallelize(1 to 9, 3)

- 2 scala> def myfunc[T](iter: Iterator[T]) : Iterator[(T, T)] = {

- 3 var res = List[(T, T)]()

- 4 var pre = iter.next while (iter.hasNext) {

- 5 val cur = iter.next;

- 6 res .::= (pre, cur) pre = cur;

- 7 }

- 8 res.iterator

- 9 }

- 10 scala> a.mapPartitions(myfunc).collect

- 11 res0: Array[(Int, Int)] = Array((2,3), (1,2), (5,6), (4,5), (8,9), (7,8))

- 12


上述例⼦中的函数myfunc是把分区中⼀个元素和它的下⼀个元素组成⼀个Tuple。因为分区中最后⼀个 元素没有下⼀个元素了，所以(3,4)和(6,7)不在结果中。 mapPartitions还有些变种，⽐如 mapPartitionsWithContext，它能把处理过程中的⼀些状态信息传递给⽤户指定的输⼊函数。还有 mapPartitionsWithIndex，它能把分区的index传递给⽤户指定的输⼊函数。

# mapValues

mapValues顾名思义就是输⼊函数应⽤于RD中Kev-Value的Value，原RD中的Key保持不变，与新的 Value⼀起组成新的RD中的元素。因此，该函数只适⽤于元素为KV对的RD。 举例：

scala> val a = sc.parallelize(List("dog", "tiger", "lion", "cat", "panther", " eagle"), 2)

- 1

- 2 scala> val b = a.map(x => (x.length, x))

- 3 scala> b.mapValues("x" + _ + "x").collect

res5: Array[(Int, String)] = Array((3,xdogx), (5,xtigerx), (4,xlionx), (3,xcatx), (7,xpantherx), (5,xeaglex))

- 4

- 5


# mapWith

mapWith是map的另外⼀个变种，map只需要⼀个输⼊函数，⽽mapWith有两个输⼊函数。它的定义 如下：

def mapWith[A: ClassTag, U: ](constructA: Int => A, preservesPartitioning: Boolean = false)(f: (T, A) => U): RDD[U]

- 1

- 2


第⼀个函数constructA是把RD的partition index（index从0开始）作为输⼊，输出为新类型A；

第⼆个函数f是把⼆元组(T, A)作为输⼊（其中T为原RD中的元素，A为第⼀个函数的输出），输出 类型为U。

举例：把partition index 乘以10，然后加上2作为新的RD的元素。

- 1 val x = sc.parallelize(List(1,2,3,4,5,6,7,8,9,10), 3)

- 2 x.mapWith(a => a * 10)((a, b) => (b + 2)).collect

- 3 res4: Array[Int] = Array(2, 2, 2, 12, 12, 12, 22, 22, 22, 22)

- 4


# flatMap

与map类似，区别是原RD中的元素经map处理后只能⽣成⼀个元素，⽽原RD中的元素经flatmap处 理后可⽣成多个元素来构建新RD。 举例：对原RD中的每个元素x产⽣y个元素（从1到y，y为元素x 的值）

- 1 scala> val a = sc.parallelize(1 to 4, 2)

- 2 scala> val b = a.flatMap(x => 1 to x)

- 3 scala> b.collect

- 4 res12: Array[Int] = Array(1, 1, 2, 1, 2, 3, 1, 2, 3, 4)

- 5


# flatMapWith

flatMapWith与mapWith很类似，都是接收两个函数，⼀个函数把partitionIndex作为输⼊，输出是⼀个 新类型A；另外⼀个函数是以⼆元组（T,A）作为输⼊，输出为⼀个序列，这些序列⾥⾯的元素组成了 新的RD。它的定义如下：

def flatMapWith[A: ClassTag, U: ClassTag](constructA: Int => A, preservesPartitioning: Boolean = false)(f: (T, A) => Seq[U]): RDD[U]

- 1

- 2


举例：

- 1 scala> val a = sc.parallelize(List(1,2,3,4,5,6,7,8,9), 3)

- 2 scala> a.flatMapWith(x => x, true)((x, y) => List(y, x)).collect

- 3 res58: Array[Int] = Array(0, 1, 0, 2, 0, 3, 1, 4, 1, 5, 1, 6, 2, 7, 2,

- 4 8, 2, 9)

- 5


# flatMapValues

flatMapValues类似于mapValues，不同的在于flatMapValues应⽤于元素为KV对的RD中Value。每个 ⼀元素的Value被输⼊函数映射为⼀系列的值，然后这些值再与原RD中的Key组成⼀系列新的KV对。 举例

- 1 scala> val a = sc.parallelize(List((1,2),(3,4),(3,6)))

- 2 scala> val b = a.flatMapValues(x=>x.to(5))

- 3 scala> b.collect

- 4 res3: Array[(Int, Int)] = Array((1,2), (1,3), (1,4), (1,5), (3,4), (3,5))

- 5


上述例⼦中原RD中每个元素的值被转换为⼀个序列（从其当前值到5），⽐如第⼀个KV对(1,2), 其值 2被转换为2，3，4，5。然后其再与原KV对中Key组成⼀系列新的KV对(1,2),(1,3),(1,4),(1,5)。

# reduce

reduce将RD中元素两两传递给输⼊函数，同时产⽣⼀个新的值，新产⽣的值与RD中下⼀个元素再 被传递给输⼊函数直到最后只有⼀个值为⽌。 举例

- 1 scala> val c = sc.parallelize(1 to 10)

- 2 scala> c.reduce((x, y) => x + y)

- 3 res4: Int = 55

- 4


上述例⼦对RD中的元素求和。

# reduceByKey

顾名思义，reduceByKey就是对元素为KV对的RD中Key相同的元素的Value进⾏reduce，因此，Key 相同的多个元素的值被reduce为⼀个值，然后与原RD中的Key组成⼀个新的KV对。 举例:

- 1 scala> val a = sc.parallelize(List((1,2),(3,4),(3,6)))

- 2 scala> a.reduceByKey((x,y) => x + y).collect

- 3 res7: Array[(Int, Int)] = Array((1,2), (3,10))

- 4


上述例⼦中，对Key相同的元素的值求和，因此Key为3的两个元素被转为了(3,10)。

