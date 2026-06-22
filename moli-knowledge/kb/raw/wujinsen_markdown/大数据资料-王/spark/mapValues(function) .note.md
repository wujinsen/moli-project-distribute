groupBy(_._1).mapValues(function) 对groupBy出来的key、value对的value进⾏处理

map(function)

map是对RD中的每个元素都执⾏⼀个指定的函数来产⽣⼀个新的RD。任何原RD中的元素在 新RD中都有且只有⼀个元素与之对应。

举例：

- val a = sc.parallelize(1 to 9, 3)

- val b = a.map(x => x*2)//x => x*2是⼀个函数，x是传⼊参数即RDD的每个元素，x*2是返回值


- a.collect

- //结果Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9)

b.collect

- //结果Array[Int] = Array(2, 4, 6, 8, 10, 12, 14, 16, 18)




- 1

- 2

- 3

- 4

- 5

- 6


当然map也可以把Key变成Key-Value对

- val a = sc.parallelize(List("dog", "tiger", "lion", "cat", "panther", " eagle"), 2)

- val b = a.map(x => (x, 1)) b.collect.foreach(println(_)) /* (dog,1) (tiger,1) (lion,1) (cat,1) (panther,1) ( eagle,1)


*/

1 12

mapPartitions(function)

map()的输⼊函数是应⽤于RD中每个元素，⽽mapPartitions()的输⼊函数是应⽤于每个分区

package test

import scala.Iterator

import org.apache.spark.SparkConf import org.apache.spark.SparkContext

object TestRdd {

def sumOfEveryPartition(input: Iterator[Int]): Int = { var total = 0 input.foreach { elem =>

total += elem

} total

} def main(args: Array[String]) {

val conf = new SparkConf().setAppName("Spark Rdd Test") val spark = new SparkContext(conf) val input = spark.parallelize(List(1, 2, 3, 4, 5, 6), 2)//RDD有6个元素，分成2个

partition val result = input.mapPartitions(

partition => Iterator(sumOfEveryPartition(partition)))//partition是传⼊的参数，是个 list，要求返回也是list，即Iterator(sumOfEveryPartition(partition))

result.collect().foreach { println(_)//6 15

} spark.stop()

} }

- 1

- 12

- 13

- 14

- 15

- 16

- 17

- 18

- 19

- 20

- 21


- 2


- 23

- 24

- 25

- 26

- 27

- 28

- 29


mapValues(function)

原RD中的Key保持不变，与新的Value⼀起组成新的RD中的元素。因此，该函数只适⽤于元素 为KV对的RD。

- val a = sc.parallelize(List("dog", "tiger", "lion", "cat", "panther", " eagle"), 2)

- val b = a.map(x => (x.length, x)) b.mapValues("x" + _ + "x").collect


- 1

- 2

- 3


/"x" + _ + "x"等同于everyInput =>"x" + everyInput + "x"

/结果

Aray(

- (3,xdogx),

(5,xtigerx),

- (4,xlionx), (3,xcatx), (7,xpantherx),

- (5,xeaglex) )


mapWith和flatMapWith

感觉⽤得不多，参考htp:/blog.csdn.net/jewes/article/details/39896301

flatMap(function)

与map类似，区别是原RD中的元素经map处理后只能⽣成⼀个元素，⽽原RD中的元素经 flatmap处理后可⽣成多个元素

- val a = sc.parallelize(1 to 4, 2)

- val b = a.flatMap(x => 1 to x)//每个元素扩展 b.collect /* 结果 Array[Int] = Array( 1,


1, 2, 1, 2, 3, 1, 2, 3, 4)

*/

- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8

- 9


flatMapValues(function)

- val a = sc.parallelize(List((1,2),(3,4),(5,6)))

- val b = a.flatMapValues(x=>1 to x) b.collect.foreach(println(_)) /*


- (1,1)

- (1,2)


- (3,1)

- (3,2)

- (3,3)

- (3,4)


- (5,1)

- (5,2)

- (5,3)

- (5,4)

- (5,5)

- (5,6)


*/

