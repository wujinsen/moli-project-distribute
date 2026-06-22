共享变量

# 通常情况下，当向Spark操作(如map,reduce)传递⼀个函数时，它会在⼀个远程集群节点上执⾏，它会 使⽤函数中所有变量的副本。这些变量被复制到所有的机器上，远程机器上并没有被更新的变量会向 驱动程序回传。在任务之间使⽤通⽤的，⽀持读写的共享变量是低效的。尽管如此，Spark提供了两种 有限类型的共享变量，⼴播变量和累加器。

⼴播变量

⼴播变量允许程序员将⼀个只读的变量缓存在每台机器上，⽽不⽤在任务之间传递变量。⼴播变量可 被⽤于有效地给每个节点⼀个⼤输⼊数据集的副本。Spark还尝试使⽤⾼效地⼴播算法来分发变量，进 ⽽减少通信的开销。 Spark的动作通过⼀系列的步骤执⾏，这些步骤由分布式的洗牌操作分开。Spark⾃动地⼴播每个步骤 每个任务需要的通⽤数据。这些⼴播数据被序列化地缓存，在运⾏任务之前被反序列化出来。这意味 着当我们需要在多个阶段的任务之间使⽤相同的数据，或者以反序列化形式缓存数据是⼗分重要的时 候，显式地创建⼴播变量才有⽤。 通过在⼀个变量v上调⽤SparkContext.broadcast(v)可以创建⼴播变量。⼴播变量是围绕着v的封装， 可以通过value⽅法访问这个变量。举例如下：

- 1 scala> val broadcastVar = sc.broadcast(Array(1, 2, 3))

- 2 broadcastVar: org.apache.spark.broadcast.Broadcast[Array[Int]] = Broadcast(0)

- 3

- 4 scala> broadcastVar.value

- 5 res0: Array[Int] = Array(1, 2, 3)


在创建了⼴播变量之后，在集群上的所有函数中应该使⽤它来替代使⽤v.这样v就不会不⽌⼀次地在节 点之间传输了。另外，为了确保所有的节点获得相同的变量，对象v在被⼴播之后就不应该再修改。

累加器

累加器是仅仅被相关操作累加的变量，因此可以在并⾏中被有效地⽀持。它可以被⽤来实现计数器和 总和。Spark原⽣地只⽀持数字类型的累加器，编程者可以添加新类型的⽀持。如果创建累加器时指定 了名字，可以在Spark的UI界⾯看到。这有利于理解每个执⾏阶段的进程。（对于python还不⽀持） 累加器通过对⼀个初始化了的变量v调⽤SparkContext.acumulator(v)来创建。在集群上运⾏的任务可 以通过ad或者"+="⽅法在累加器上进⾏累加操作。但是，它们不能读取它的值。只有驱动程序能够读 取它的值，通过累加器的value⽅法。 下⾯的代码展示了如何把⼀个数组中的所有元素累加到累加器上:

- 1 scala> val accum = sc.accumulator(0, "My Accumulator")

- 2 accum: spark.Accumulator[Int] = 0

- 3

- 4 scala> sc.parallelize(Array(1, 2, 3, 4)).foreach(x => accum += x)

- 5 ...

- 6 10/09/29 18:41:08 INFO SparkContext: Tasks finished in 0.317106 s

- 7

- 8 scala> accum.value

- 9 res2: Int = 10


# 尽管上⾯的例⼦使⽤了内置⽀持的累加器类型Int,但是开发⼈员也可以通过继承AcumulatorParam类 来创建它们⾃⼰的累加器类型。AcumulatorParam接⼝有两个⽅法： zero⽅法为你的类型提供⼀个0值。 adInPlace⽅法将两个值相加。 假设我们有⼀个代表数学vector的Vector类。我们可以向下⾯这样实现：

- 1 object VectorAccumulatorParam extends AccumulatorParam[Vector] {

- 2 def zero(initialValue: Vector): Vector = {

- 3 Vector.zeros(initialValue.size)

- 4 }

- 5 def addInPlace(v1: Vector, v2: Vector): Vector = {

- 6 v1 += v2

- 7 }

- 8 }

- 9

- 10 // Then, create an Accumulator of this type:

- 11 val vecAccum = sc.accumulator(new Vector(...))(VectorAccumulatorParam)


在Scala⾥，Spark提供更通⽤的累加接⼝来累加数据，尽管结果的类型和累加的数据类型可能不⼀致（例如，通过收 集在⼀起的元素来创建⼀个列表）。同时,SparkContext. .accumulableCollection ⽅法来累加通⽤的Scala的 集合类型。

累加器仅仅在动作操作内部被更新，Spark保证每个任务在累加器上的更新操作只被执⾏⼀次，也就是 说，重启任务也不会更新。在转换操作中，⽤户必须意识到每个任务对累加器的更新操作可能被不只 ⼀次执⾏，如果重新执⾏了任务和作业的阶段。 累加器并没有改变Spark的惰性求值模型。如果它们被RD上的操作更新，它们的值只有当RD因为动 作操作被计算时才被更新。因此，当执⾏⼀个惰性的转换操作,⽐如map时，不能保证对累加器值的更 新被实际执⾏了。下⾯的代码⽚段演示了此特性：

- 1 val accum = sc.accumulator(0)

- 2 data.map { x => accum += x; f(x) }

- 3 //在这⾥,accum的值仍然是0，因为没有动作操作引起map被实际的计算.


