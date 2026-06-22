# RD[T]

Transformations

<table>
  <tr>
    <th>rd api</th>
    <th>备注</th>
  </tr>
  <tr>
    <td>persist/cache</td>
    <td> </td>
  </tr>
  <tr>
    <td>map(f: T => U)</td>
    <td> </td>
  </tr>
  <tr>
    <td>keyBy(f: T => K)</td>
    <td>特殊的map，提key</td>
  </tr>
  <tr>
    <td>flatMap(f: T => Iterable[U])</td>
    <td>map的⼀种，类似UDTF</td>
  </tr>
  <tr>
    <td>filter(f: T => Bolean)</td>
    <td>map的⼀种</td>
  </tr>
  <tr>
    <td>distinct(numPartitions)<br><br></td>
    <td>rdmap(x的实现为=> (x, null)).reduceByKey((x, y) => x, numPartitions).map(_._1)<br><br>reduceByKey是特殊的combineByKey，其 mergeValue函数和mergeCombiners函数⼀致， 都是<br><br>(x, y) => x</td>
  </tr>
  <tr>
    <td>repartition(numPartitions)/coalesce(numPartitio ns)</td>
    <td>repartition⽤于增减rd分区。coalesce特指减少</td>
  </tr>
  <tr>
    <td>sample()/randomSplit()/takeSample()</td>
    <td>分区，可以通过⼀次窄依赖的映射避免shufle 采样</td>
  </tr>
  <tr>
    <td>union(RD[T])</td>
    <td>不去重。使⽤distinct()去重</td>
  </tr>
  <tr>
    <td>sortBy[K](f: (T) => K)</td>
    <td>传⼊的fkeyBy(f).sortByKey().values()是提key函数，rd的实现为 这次操作为RD设置了⼀个<br><br>RangePartitioner</td>
  </tr>
  <tr>
    <td>intersection(RD[T])<br><br></td>
    <td>两个集合取交集，并去重。RD的实现为<br><br>map(v => (v, null)).cogroup(other.map(v => (v, null))).filter(两边都空).keys()<br><br>cogroup是⽣成<br><br>K, List[V], List[V]<br><br>的形态，这个过程可能内含⼀次shufle操作，为 的分区对⻬。</td>
  </tr>
  <tr>
    <td>glom():RD[Aray[T]</td>
    <td>了两边RD 把每个分区的数据合并成⼀个Aray。原本每个分<br><br>的迭代器。</td>
  </tr>
  <tr>
    <td>cartesian(RD[U]): RD[(T, U)]</td>
    <td>区是T 求两个集合的笛卡尔积。RD的做法是两个RD</td>
  </tr>
  <tr>
    <td>groupBy[K](f: T => K): RD[(K, Iterable[T])]</td>
    <td>内循环、外循环yield出每对(x, y) RD建议如果后续跟ag的话，直接使⽤<br><br>aggregateByKey<br><br>或<br><br>reduceByKey</td>
  </tr>
</table>


### 更省时，这两个操作本质上就是combineByKey

<table>
  <tr>
    <th>pipe(comand: String)</th>
    <th>把RD数据通过<br><br>ProcessBuilder<br><br>创建额外的进程输出⾛</th>
  </tr>
  <tr>
    <td>mapPartitions(f: Iterator[T] => IteratorU])/mapPartitionsWithIndex(f: (Int,</td>
    <td>RD的每个分区做map变换</td>
  </tr>
  <tr>
    <td>Iterator[T]) => Iterator[U])<br><br>zip(RD[U]): RD[(T, U)]</td>
    <td>两个RD分区数⽬⼀致，且每个分区数据条数⼀ 致</td>
  </tr>
</table>


## Actions

<table>
  <tr>
    <th>rd api</th>
    <th>备注</th>
  </tr>
  <tr>
    <td>foreach(f: T => Unit)</td>
    <td>rd实现为调⽤sc.runJob()，把f作⽤于每个分区 的每条记录</td>
  </tr>
  <tr>
    <td>foreachPartition(f: Iterator[T] => Unit)</td>
    <td>rd实现为调⽤sc.runJob()，把f作⽤于每个分区</td>
  </tr>
  <tr>
    <td>colect(): Aray[T]</td>
    <td>rd实现为调⽤sc.runJob()，得到results，把多</td>
  </tr>
  <tr>
    <td>toLocalIterator()</td>
    <td>个result的aray合并成⼀个aray 把所有数据以迭代器返回，rd实现是调⽤ sc.runJob()，每个分区迭代器转aray，收集到 driver端再flatMap⼀次打散成⼤迭代器。理解为</td>
  </tr>
  <tr>
    <td>colect[U](f: PartailFunction[T, U]): RD[U]</td>
    <td>⼀种⽐较特殊的driver端cache rd实现为<br><br>filter(f.isDefinedAt).map(f)<br><br>先做⼀次filter找出满⾜的数据，然后⼀次map操 作执⾏这个偏函数。</td>
  </tr>
  <tr>
    <td>subtract(RD[T])</td>
    <td>rd实现为<br><br>map(x => (x, null)).subtractByKey(other.map((_, null)), p2).keys<br><br>与求交类似</td>
  </tr>
  <tr>
    <td>reduce(f: (T, T) => T)</td>
    <td>rd实现为调⽤sc.runJob()，让f在rd每个分区计 的时候再计算⼀次。</td>
  </tr>
  <tr>
    <td>treReduce(f: (T, T) => T, depth = 2)</td>
    <td>算⼀次，最后汇总merge ⻅treAgregate</td>
  </tr>
  <tr>
    <td>fold(zeroValue: T)(op: (T, T) => T)</td>
    <td>特殊的reduce，带初始值，函数式语义的fold</td>
  </tr>
  <tr>
    <td>agregate(zeroValue: U)(seqOp: (U, T) => U, combOp: (U, U) => U)</td>
    <td>带初始值、reduce聚合、merge聚合三个完整条 件的聚合⽅法。rd的做法是把函数传⼊分区⾥去 做计算，最后汇总各分区的结果再⼀次combOp 计算。</td>
  </tr>
  <tr>
    <td>treAgregate(zeroValue: U)(seqOp: (U, T) => U, combOp: (U, U) => U)(depth = 2)</td>
    <td>在分区处，做两次及以上的merge聚合，即每个 分区的merge计算可能也会带shufle。其余部分</td>
  </tr>
  <tr>
    <td>count()</td>
    <td>同agregate。理解为更复杂的多阶agregate rd实现为调⽤sc.runJob()，把每个分区的size汇<br><br>⼀次</td>
  </tr>
  <tr>
    <td>countAprox(timeout, confidence)</td>
    <td>总在driver端再sum<br><br>提交个体DAGScheduler特殊的任务，⽣成特殊的 任务监听者，在timeout时间内返回，没计算完的 话返回⼀个⼤致结果，返回值的计算逻辑可⻅<br><br>的⼦类</td>
  </tr>
</table>


### AproximateEvaluator

<table>
  <tr>
    <th>countByValue(): Map[T, Long]</th>
    <th>rd实现为<br><br>map(value => (value, null)).countByKey()<br><br>本质上是⼀次简单的combineByKey，返回 Map，会全load进driver的内存⾥，需要数据集规 模较⼩</th>
  </tr>
  <tr>
    <td>countByValueAprox()</td>
    <td>同countAprox()</td>
  </tr>
  <tr>
    <td>countAproxDistinct()</td>
    <td>实验性⽅法，⽤streamlib库实现的HyperLogLog 做</td>
  </tr>
  <tr>
    <td>zipWithIndex(): RD[(T,</td>
    <td>与⽣成的index做zip操作</td>
  </tr>
  <tr>
    <td>Long)]/zipWithUniqueId(): RD[(T, Long)] take(num): Aray[T]</td>
    <td>扫某个分区</td>
  </tr>
  <tr>
    <td>first()</td>
    <td>即take(1)</td>
  </tr>
  <tr>
    <td>top(n)(ordering)</td>
    <td>每个分区内传⼊top的处理函数，得到分区的堆， 使⽤rd.reduce()，把每个分区的堆合起来，排<br><br>个</td>
  </tr>
  <tr>
    <td>max()/min()</td>
    <td>序，取前n 特殊的reduce，传⼊max/min⽐较函数</td>
  </tr>
  <tr>
    <td>saveAs X</td>
    <td>输出存储介质</td>
  </tr>
  <tr>
    <td> </td>
    <td>声明</td>
  </tr>
</table>


checkpoint 显示cp

# 特殊RD

PairRDFunctions

<table>
  <tr>
    <th>rd api</th>
    <th>备注</th>
  </tr>
  <tr>
    <td>combineByKey[C](createCombiner: V => C, mergeValue: (C, V) => C, mergeCombiners: (C,</td>
    <td>传统MR定义拆分，重要基础api</td>
  </tr>
  <tr>
    <td>C) => C): RD[(K, C)]<br><br>agregateByKey[U](zeroValue: U, seqOp: (U, V)<br><br>=> U, combOp: (U, U) => U): RD[(K, U)]</td>
    <td>rd⾥，把zeroValue转成了⼀个createCombiner ⽅法，然后调⽤了combineByKey()。本质上两者 是⼀样的。</td>
  </tr>
  <tr>
    <td>foldByKey(zeroValue: V, func: (V, V) => V): RD[(K, V)]</td>
    <td>func即被当作mergeValue，⼜被当作</td>
  </tr>
  <tr>
    <td>sampleByKey()</td>
    <td>mergeCombiners，调⽤了combineByKey() ⽣成⼀个与key相关的sampleFunc，调⽤</td>
  </tr>
  <tr>
    <td>reduceByKey()</td>
    <td>rd.mapPartitionsWithIndex(sampleFunc) 调⽤combineByKey</td>
  </tr>
  <tr>
    <td>reduceByKeyLocaly(func: (V, V) => V): Map[K, V]</td>
    <td>rd实现为<br><br>self.mapPartitions(reducePartition).reduce( mergeMaps)<br><br>reducePartition是在每个分区⽣成⼀个</td>
  </tr>
  <tr>
    <td>countByKey()</td>
    <td>HashMap，mergeMaps是合并多个HashMap rd实现为<br><br>mapValues(_ => 1L).reduceByKey(_ + _).collect().toMap<br><br></td>
  </tr>
  <tr>
    <td>countByKeyAprox()</td>
    <td>rd实现为<br><br>map(_._1).countByValueApprox</td>
  </tr>
  <tr>
    <td>countAproxDistinctByKey()</td>
    <td>类似rd的countAproxDistinct⽅法，区别是把 ⾥⾯</td>
  </tr>
  <tr>
    <td>groupByKey()</td>
    <td>⽅法作⽤在了combineByKey 简单的combineByKey实现</td>
  </tr>
  <tr>
    <td>partitionBy(partitioner)</td>
    <td>为rd设置新的分区结构</td>
  </tr>
  <tr>
    <td>join(RD[(K, W)]): RD[(K, (V, W)]<br><br></td>
    <td>rd实现为<br><br>cogroup(other, partitioner).flatMapValues(...)</td>
  </tr>
  <tr>
    <td>leftOuterJoin(…)</td>
    <td>实现同上，只是flatMapValues⾥⾯遍历两个 出结果的判断逻辑变了下</td>
  </tr>
  <tr>
    <td>rightOuterJoin(…)</td>
    <td>rd，yield 同上</td>
  </tr>
  <tr>
    <td>fulOuterJoin(…)</td>
    <td>同上</td>
  </tr>
  <tr>
    <td>colectAsMap()</td>
    <td>rd实现为<br><br>collect().foreach(pairToMap)</td>
  </tr>
  <tr>
    <td>mapValues(f: V => U)</td>
    <td>⼀种简单的map()操作</td>
  </tr>
  <tr>
    <td> </td>
    <td>操作</td>
  </tr>
</table>


### flatMapValues(f: V => Iterable[U]) ⼀种简单的map()

<table>
  <tr>
    <th>cogroup(RD[(K, W)]): RD[(K, (Iterable[V],</th>
    <th>做集合性操作的基础api，包括各种join、求交等</th>
  </tr>
  <tr>
    <td>Iterable[W])] subtractByKey(RD[(K, W)]): RD[(K, V)]</td>
    <td>从原来的rd⾥排除右侧有的keys</td>
  </tr>
  <tr>
    <td>l okup(key: K): Seq[V]</td>
    <td>rd实现的时候，然后分区是基于key的，那⽐较 ⾼效可以直接遍历对应分区，否则全部遍历。全 部遍历的实现为<br><br>filter(_._1 == key).map(_._2).collect()</td>
  </tr>
  <tr>
    <td>saveAs X</td>
    <td>写外部存储</td>
  </tr>
  <tr>
    <td>keys()</td>
    <td>⼀种简单的map()操作</td>
  </tr>
  <tr>
    <td> </td>
    <td>操作</td>
  </tr>
</table>


values() ⼀种简单的map()

AsyncRDActions

countAsync, colectAsync, takeAsync, foreachAsync, foreachPartitionAsync

OrderedRDFunctions

针对RD[K: Ordering, V]

<table>
  <tr>
    <th>rd api</th>
    <th>备注</th>
  </tr>
  <tr>
    <td>sortByKey()</td>
    <td>⻅rd.sortBy()⾥的解释</td>
  </tr>
  <tr>
    <td>filterByRange(lower: K, uper: K)</td>
    <td>当rd分区是RangePartition的时候可以做这样的</td>
  </tr>
</table>


filter

## DoubleRDFunctions

针对RD[Double]

<table>
  <tr>
    <th>rd api</th>
    <th>备注</th>
  </tr>
  <tr>
    <td>sum()</td>
    <td>rd实现是<br><br>reduce(_ + _)</td>
  </tr>
  <tr>
    <td>stats()</td>
    <td>rd实现是<br><br>mapPartitions(nums => Iterator(StatCounter(nums))).reduce((a, b)<br><br>=> a.merge(b))<br><br>StatCounter在⼀次遍历⾥统计出中位数、⽅差、 是他内部的⽅法</td>
  </tr>
  <tr>
    <td>mean()</td>
    <td>count三个值，merge() rd实现是<br><br>stats().mean</td>
  </tr>
  <tr>
    <td>variance()/sampleVariance()</td>
    <td>rd实现是<br><br>stats().variance</td>
  </tr>
  <tr>
    <td>stdev()/sampleStdev()</td>
    <td>rd实现是<br><br>stats().stdev<br><br>求标准差</td>
  </tr>
  <tr>
    <td>meanAprox()/sumAprox()</td>
    <td>调⽤runAproximateJob</td>
  </tr>
  <tr>
    <td>histogram()</td>
    <td>⽐较复杂的计算，rd实现是先mapPartitions再 ，包含⼏次递归</td>
  </tr>
</table>


### reduce

