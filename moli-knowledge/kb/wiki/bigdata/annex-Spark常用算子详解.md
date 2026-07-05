---
title: Spark常用算子详解.note（原文插图 annex）
slug: annex-Spark常用算子详解
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/Spark/算子/Spark常用算子详解.note.md
related: [spark-核心概念与实践]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.cnblogs.com/kpsmile/p/10434390.html

# Spark常⽤算⼦详解

Spark的算⼦的分类 从⼤⽅向来说，Spark 算⼦⼤致可以分为以下两类: 1）Transformation 变换/转换算⼦：这种变换并不触发提交作业，完成作业中间过程处理。

Transformation 操作是延迟计算的，也就是说从⼀个RD 转换⽣成另⼀个 RD 的转换操作不是 ⻢上执⾏，需要等到有 Action 操作的时候才会真正触发运算。

2）Action ⾏动算⼦：这类算⼦会触发 SparkContext 提交 Job 作业。 Action 算⼦会触发 Spark 提交作业（Job），并将数据输出 Spark系统。

从⼩⽅向来说，Spark 算⼦⼤致可以分为以下三类:

- 1）Value数据类型的Transformation算⼦，这种变换并不触发提交作业，针对处理的数据项是Value型的

数据。

- 2）Key-Value数据类型的Transfromation算⼦，这种变换并不触发提交作业，针对处理的数据项是Key-

Value型的数据对。

- 3）Action算⼦，这类算⼦会触发SparkContext提交Job作业。


- 1）Value数据类型的Transformation算⼦ ⼀、输⼊分区与输出分区⼀对⼀型


- 1、map算⼦
- 2、flatMap算⼦
- 3、mapPartitions算⼦
- 4、glom算⼦

⼆、输⼊分区与输出分区多对⼀型

- 5、union算⼦
- 6、cartesian算⼦

三、输⼊分区与输出分区多对多型

- 7、grouBy算⼦

四、输出分区为输⼊分区⼦集型

- 8、filter算⼦
- 9、distinct算⼦
- 10、subtract算⼦ 1、sample算⼦


12、takeSample算⼦ 五、Cache型

- 13、cache算⼦
- 14、persist算⼦


- 2）Key-Value数据类型的Transfromation算⼦ ⼀、输⼊分区与输出分区⼀对⼀

15、mapValues算⼦ ⼆、对单个RD或两个RD聚集 单个RD聚集

- 16、combineByKey算⼦
- 17、reduceByKey算⼦
- 18、partitionBy算⼦

两个RD聚集

- 19、Cogroup算⼦

三、连接

- 20、join算⼦
- 21、leftOutJoin和 rightOutJoin算⼦


- 3）Action算⼦ ⼀、⽆输出


2、foreach算⼦ ⼆、HDFS

- 23、saveAsTextFile算⼦
- 24、saveAsObjectFile算⼦

三、Scala集合和数据类型

- 25、colect算⼦
- 26、colectAsMap算⼦
- 27、reduceByKeyLocaly算⼦
- 28、l okup算⼦
- 29、count算⼦
- 30、top算⼦
- 31、reduce算⼦
- 32、fold算⼦ 3、agregate算⼦


## 1. Transformations 算⼦ （1） map

将原来 RD 的每个数据项通过 map 中的⽤户⾃定义函数 f 映射转变为⼀个新的元素。源码中 map 算⼦ 相当于初始化⼀个 RD， 新 RD 叫做 MapedRD(this, sc.clean(f)。

图 1中每个⽅框表示⼀个 RD 分区，左侧的分区经过⽤户⾃定义函数 f:T->U 映射为右侧的新 RD 分区。 但是，实际只有等到 Action算⼦触发后，这个 f 函数才会和其他函数在⼀个stage 中对数据进⾏运算。在图 1 中的第⼀个分区，数据记录 V1 输⼊ f，通过 f 转换输出为转换后的分区中的数据记录 Vʼ1。

![image 1](assets/imageFile1.png)

图1 map 算⼦对 RD 转换

### （2） flatMap

将原来 RD 中的每个元素通过函数 f 转换为新的元素，并将⽣成的 RD 的每个集合中的元素合并为⼀个 集合，内部创建 FlatMapedRD(this，sc.clean(f)。

图 2 表 示 RD 的 ⼀ 个 分 区 ，进 ⾏ flatMap函 数 操 作， flatMap 中 传 ⼊ 的 函 数 为 f:T->U， T和 U 可以是任意的数据类型。将分区中的数据通过⽤户⾃定义函数 f 转换为新的数据。外部⼤⽅框可以认为是⼀ 个 RD 分区，⼩⽅框代表⼀个集合。 V1、 V2、 V3 在⼀个集合作为 RD 的⼀个数据项，可能存储为数组或 其他容器，转换为Vʼ1、 Vʼ2、 Vʼ3 后，将原来的数组或容器结合拆散，拆散的数据形成为 RD 中的数据项。

![image 2](assets/imageFile2.png)

图2 flapMap 算⼦对 RD 转换

### （3） mapPartitions

mapPartitions 函 数 获 取 到 每 个 分 区 的 迭 代器，在 函 数 中 通 过 这 个 分 区 整 体 的 迭 代 器 对整 个 分 区 的 元 素 进 ⾏ 操 作。 内 部 实 现 是 ⽣ 成 MapPartitionsRD。图 3 中的⽅框代表⼀个 RD 分区。图 3 中，⽤户通过函数 f (iter)=>iter.f ilter(_>=3) 对分区中所有数据进⾏过滤，⼤于和等于 3 的数据保留。⼀个⽅块代表⼀个 RD 分区，含有 1、 2、 3 的分 区过滤只剩下元素 3。

![image 3](assets/imageFile3.png)

图3 mapPartitions 算⼦对 RD 转换

### （4）glom

glom函数将每个分区形成⼀个数组，内部实现是返回的GlomedRD。 图4中的每个⽅框代表⼀个RD 分区。图4中的⽅框代表⼀个分区。 该图表示含有V1、 V2、 V3的分区通过函数glom形成⼀数组 Aray[（V1），（V2），（V3）]。

![image 4](assets/imageFile4.png)

图 4 glom算⼦对RD转换

### （5） union

使⽤ union 函数时需要保证两个 RD 元素的数据类型相同，返回的 RD 数据类型和被合并的 RD 元素 数据类型相同，并不进⾏去重操作，保存所有元素。如果想去重 可以使⽤ distinct()。同时 Spark 还提供更为简洁的使⽤ union 的 API，通过 + 符号相当于 union 函数操 作。

图 5 中左侧⼤⽅框代表两个 RD，⼤⽅框内的⼩⽅框代表 RD 的分区。右侧⼤⽅框代表合并后的 RD， ⼤⽅框内的⼩⽅框代表分区。

含有V1、V2、U1、U2、U3、U4的RD和含有V1、V8、U5、U6、U7、U8的RD合并所有元素形成⼀ 个RD。V1、V1、V2、V8形成⼀个分区，U1、U2、U3、U4、U5、U6、U7、U8形成⼀个分区。

![image 5](assets/imageFile5.png)

图 5 union 算⼦对 RD 转换

### （6） cartesian

对 两 个 RD 内 的 所 有 元 素 进 ⾏ 笛 卡 尔 积 操 作。 操 作 后， 内 部 实 现 返 回CartesianRD。图6 中左侧⼤⽅框代表两个 RD，⼤⽅框内的⼩⽅框代表 RD 的分区。右侧⼤⽅框代表合并后的 RD，⼤⽅框 内的⼩⽅框代表分区。图6中的⼤⽅框代表RD，⼤⽅框中的⼩⽅框代表RD分区。

例 如： V1 和 另 ⼀ 个 RD 中 的 W1、 W2、 Q5 进 ⾏ 笛 卡 尔 积 运 算 形 成 (V1,W1)、(V1,W2)、 (V1,Q5)。

![image 6](assets/imageFile6.png)

图 6 cartesian 算⼦对 RD 转换

### （7） groupBy

groupBy ：将元素通过函数⽣成相应的 Key，数据就转化为 Key-Value 格式，之后将 Key 相同的元素分 为⼀组。

函数实现如下：

- 1）将⽤户函数预处理： val cleanF = sc.clean(f)
- 2）对数据 map 进⾏函数操作，最后再进⾏ groupByKey 分组操作。


this.map(t => (cleanF(t), t).groupByKey(p) 其中， p 确定了分区个数和分区函数，也就决定了并⾏化的程度。

图7 中⽅框代表⼀个 RD 分区，相同key 的元素合并到⼀个组。例如 V1 和 V2 合并为 V， Value 为 V1,V2。形成 V,Seq(V1,V2)。

![image 7](assets/imageFile7.png)

图 7 groupBy 算⼦对 RD 转换

### （8） filter

filter 函数功能是对元素进⾏过滤，对每个 元 素 应 ⽤ f 函 数， 返 回 值 为 true 的 元 素 在RD 中保留，

返回值为 false 的元素将被过滤掉。 内 部 实 现 相 当 于 ⽣ 成 FilteredRD(this，sc.clean(f)。 下⾯代码为函数的本质实现： defilter(f:T=>Bolean):RD[T]=newFilteredRD(this,sc.clean(f)

图 8 中每个⽅框代表⼀个 RD 分区， T 可以是任意的类型。通过⽤户⾃定义的过滤函数 f，对每个数据 项操作，将满⾜条件、返回结果为 true 的数据项保留。例如，过滤掉 V2 和 V3 保留了 V1，为区分命名为 Vʼ1。

![image 8](assets/imageFile8.png)

图 8 filter 算⼦对 RD 转换

### （9）distinct

distinct将RD中的元素进⾏去重操作。图9中的每个⽅框代表⼀个RD分区，通过distinct函数，将数据 去重。 例如，重复数据V1、 V1去重后只保留⼀份V1。

![image 9](assets/imageFile9.png)

图9 distinct算⼦对RD转换

### （10）subtract

subtract相当于进⾏集合的差操作，RD 1去除RD 1和RD 2交集中的所有元素。图10中左侧的⼤⽅框 代表两个RD，⼤⽅框内的⼩⽅框代表RD的分区。 右侧⼤⽅框 代表合并后的RD，⼤⽅框内的⼩⽅框代表分区。 V1在两个RD中均有，根据差集运算规则，新RD不保 留，V2在第⼀个RD有，第⼆个RD没有，则在新RD元素中包含V2。

![image 10](assets/imageFile10.png)

图10 subtract算⼦对RD转换

### （ 1） sample

sample 将 RD 这个集合内的元素进⾏采样，获取所有元素的⼦集。⽤户可以设定是否有放回的抽样、 百分⽐、随机种⼦，进⽽决定采样⽅式。内部实现是⽣成 SampledRD(withReplacement， fraction， sed)。

函数参数设置： ‰ withReplacement=true，表示有放回的抽样。 ‰ withReplacement=false，表示⽆放回的抽样。

图 1中 的 每 个 ⽅ 框 是 ⼀ 个 RD 分 区。 通 过 sample 函 数， 采 样 50% 的 数 据。V1、 V2、 U1、

- U2、U3、U4 采样出数据 V1 和 U1、 U2 形成新的 RD。


![image 11](assets/imageFile11.png)

图 1 sample 算⼦对 RD 转换

### （12）takeSample

takeSample（）函数和上⾯的sample函数是⼀个原理，但是不使⽤相对⽐例采样，⽽是按设定的采样个 数进⾏采样，同时返回结果不再是RD，⽽是相当于对采样后的数据进⾏ Colect（），返回结果的集合为单机的数组。

- 图12中左侧的⽅框代表分布式的各个节点上的分区，右侧⽅框代表单机上返回的结果数组。 通过


takeSample对数据采样，设置为采样⼀份数据，返回结果为V1。

![image 12](assets/imageFile12.png)

图12 takeSample算⼦对RD转换

### （13） cache

cache 将 RD 元素从磁盘缓存到内存。 相当于 persist(MEMORY_ONLY) 函数的功能。

- 图13 中每个⽅框代表⼀个 RD 分区，左侧相当于数据分区都存储在磁盘，通过 cache 算⼦将数据缓存在


内存。

![image 13](assets/imageFile13.png)

图 13 Cache 算⼦对 RD 转换

### （14） persist

persist 函数对 RD 进⾏缓存操作。数据缓存在哪⾥依据 StorageLevel 这个枚举类型进⾏确定。 有以下

⼏种类型的组合（⻅10）， DISK 代表磁盘，MEMORY 代表内存， SER 代表数据是否进⾏序列化存储。 下⾯为函数定义， StorageLevel 是枚举类型，代表存储模式，⽤户可以通过图 14-1 按需进⾏选择。 persist(newLevel:StorageLevel)

- 图 14-1 中列出persist 函数可以进⾏缓存的模式。例如，MEMORY_AND_DISK_SER 代表数据可以存储


在内存和磁盘，并且以序列化的⽅式存储，其他同理。

![image 14](assets/imageFile14.png)

图 14-1 persist 算⼦对 RD 转换

- 图 14-2 中⽅框代表 RD 分区。 disk 代表存储在磁盘， mem 代表存储在内存。数据最初全部存储在磁


盘，通过 persist(MEMORY_AND_DISK) 将数据缓存到内存，但是有的分区⽆法容纳在内存，将含有 V1、

- V2、 V3 的RD存储到磁盘，将含有U1，U2的RD仍旧存储在内存。


![image 15](assets/imageFile15.png)

图 14-2 Persist 算⼦对 RD 转换

### （15） mapValues

mapValues ：针对（Key， Value）型数据中的 Value 进⾏ Map 操作，⽽不对 Key 进⾏处理。

图 15 中的⽅框代表 RD 分区。 a=>a+2 代表对 (V1,1) 这样的 Key Value 数据对，数据只对 Value 中的 1 进⾏加 2 操作，返回结果为 3。

![image 16](assets/imageFile16.png)

图 15 mapValues 算⼦ RD 对转换

### （16） combineByKey

下⾯代码为 combineByKey 函数的定义： combineByKey[C](createCombiner:(V) C, mergeValue:(C, V) C, mergeCombiners:(C, C) C, partitioner:Partitioner, mapSideCombine:Bolean=true, serializer:Serializer=nul):RD[(K,C)]

说明： ‰ createCombiner： V => C， C 不存在的情况下，⽐如通过 V 创建 seq C。 ‰ mergeValue： (C， V) => C，当 C 已经存在的情况下，需要 merge，⽐如把 item V 加到 seq C 中，或者叠加。

mergeCombiners： (C， C) => C，合并两个 C。 ‰ partitioner： Partitioner, Shuf le 时需要的 Partitioner。 ‰ mapSideCombine ： Bolean = true，为了减⼩传输量，很多 combine 可以在 map

端先做，⽐如叠加，可以先在⼀个 partition 中把所有相同的 key 的 value 叠加， 再 shuf le。 ‰ serializerClas： String = nul，传输需要序列化，⽤户可以⾃定义序列化类：

例如，相当于将元素为 (Int， Int) 的 RD 转变为了 (Int， Seq[Int]) 类型元素的 RD。图 16中的⽅框代 表 RD 分区。如图，通过 combineByKey， 将 (V1,2)， (V1,1)数据合并为（ V1,Seq(2,1)）。

![image 17](assets/imageFile17.png)

图 16 comBineByKey 算⼦对 RD 转换

### （17） reduceByKey

reduceByKey 是⽐ combineByKey 更简单的⼀种情况，只是两个值合并成⼀个值，（ Int， Int V）to （Int， Int C），⽐如叠加。所以 createCombiner reduceBykey 很简单，就是直接返回 v，⽽ mergeValue 和 mergeCombiners 逻辑是相同的，没有区别。

函数实现： def reduceByKey(partitioner: Partitioner, func: (V, V) => V): RD[(K, V)]

= { combineByKey[V](v: V) => v, func, func, partitioner) }

图17中的⽅框代表 RD 分区。通过⽤户⾃定义函数 (A,B) => (A + B) 函数，将相同 key 的数据 (V1,2) 和 (V1,1) 的 value 相加运算，结果为（ V1,3）。

![image 18](assets/imageFile18.png)

图 17 reduceByKey 算⼦对 RD 转换

### （18）partitionBy

partitionBy函数对RD进⾏分区操作。 函数定义如下。 partitionBy（partitioner：Partitioner） 如果原有RD的分区器和现有分区器（partitioner）⼀致，则不重分区，如果不⼀致，则相当于根据分区

器⽣成⼀个新的ShufledRD。

图18中的⽅框代表RD分区。 通过新的分区策略将原来在不同分区的V1、 V2数据都合并到了⼀个分 区。

![image 19](assets/imageFile19.png)

图18 partitionBy算⼦对RD转换

### （19）Cogroup

cogroup函数将两个RD进⾏协同划分，cogroup函数的定义如下。

cogroup[W]（other： RD[（K， W）]， numPartitions： Int）： RD[（K， （Iterable[V]， Iterable[W]））]

对在两个RD中的Key-Value类型的元素，每个RD相同Key的元素分别聚合为⼀个集合，并且返回两个

RD中对应Key的元素集合的迭代器。 （K， （Iterable[V]， Iterable[W]）） 其中，Key和Value，Value是两个RD下相同Key的两个数据集合的迭代器所构成的元组。 图19中的⼤⽅框代表RD，⼤⽅框内的⼩⽅框代表RD中的分区。 将RD1中的数据（U1，1）、

（U1，2）和RD2中的数据（U1，2）合并为（U1，（（1，2），（2）））。

![image 20](assets/imageFile20.png)

图19 Cogroup算⼦对RD转换

### （20） join

join 对两个需要连接的 RD 进⾏ cogroup函数操作，将相同 key 的数据能够放到⼀个分区，在 cogroup 操作之后形成的新 RD 对每个key 下的元素进⾏笛卡尔积的操作，返回的结果再展平，对应 key 下的所有元 组形成⼀个集合。最后返回 RD[(K， (V， W)]。

下 ⾯ 代 码 为 join 的 函 数 实 现， 本 质 是通 过 cogroup 算 ⼦ 先 进 ⾏ 协 同 划 分， 再 通 过 flatMapValues 将合并的数据打散。

this.cogroup(other,partitioner).f latMapValues{case(vs,ws) => for(v<-vs;w<-ws)yield(v,w) } 图 20是对两个 RD 的 join 操作示意图。⼤⽅框代表 RD，⼩⽅框代表 RD 中的分区。函数对相同 key 的 元素，如 V1 为 key 做连接后结果为 (V1,(1,1) 和 (V1,(1,2)。

![image 21](assets/imageFile21.png)

图 20 join 算⼦对 RD 转换

### （21）eftOutJoin和rightOutJoin

LeftOutJoin（左外连接）和RightOutJoin（右外连接）相当于在join的基础上先判断⼀侧的RD元素是 否为空，如果为空，则填充为空。 如果不为空，则将数据进⾏连接运算，并 返回结果。 下⾯代码是leftOutJoin的实现。 if （ws.isEmpty） { vs.map（v => （v， None）） } else { for （v <- vs； w <- ws） yield （v， Some（w）） }

## 2. Actions 算⼦

本质上在 Action 算⼦中通过 SparkContext 进⾏了提交作业的 runJob 操作，触发了RD DAG 的执⾏。 例如， Action 算⼦ colect 函数的代码如下，感兴趣的读者可以顺着这个⼊⼝进⾏源码剖析： /*

- * Return an aray that contains al of the elements in this RD.
- */ def colect(): Aray[T] = { /* 提交 Job*/ val results = sc.runJob(this, (iter: Iterator[T]) => iter.toAray) Aray.concat(results: _*) }


（ 2） foreach

foreach 对 RD 中的每个元素都应⽤ f 函数操作，不返回 RD 和 Aray， ⽽是返回Uint。图 2表示 foreach 算⼦通过⽤户⾃定义函数对每个数据项进⾏操作。本例中⾃定义函数为 println()，控制台打印所有数 据项。

![image 22](assets/imageFile22.png)

图 2 foreach 算⼦对 RD 转换

（23） saveAsTextFile

函数将数据输出，存储到 HDFS 的指定⽬录。 下⾯为 saveAsTextFile 函数的内部实现，其内部

通过调⽤ saveAsHadopFile 进⾏实现：

this.map(x => (NulWritable.get(), new Text(x.toString).saveAsHadopFile[TextOutputFormat[NulWritable, Text](path) 将 RD 中的每个元素映射转变为 (nul， x.toString)，然后再将其写⼊ HDFS。

图 23中左侧⽅框代表 RD 分区，右侧⽅框代表 HDFS 的 Block。通过函数将RD 的每个分区存储为 HDFS 中的⼀个 Block。

![image 23](assets/imageFile23.png)

图 23 saveAsHadopFile 算⼦对 RD 转换

- （24）saveAsObjectFile saveAsObjectFile将分区中的每10个元素组成⼀个Aray，然后将这个Aray序列化，映射为（Nul，


BytesWritable（Y））的元素，写⼊HDFS为SequenceFile的格式。 下⾯代码为函数内部实现。 map（x=>（NulWritable.get（），new BytesWritable（Utils.serialize（x）））） 图24中的左侧⽅框代表RD分区，右侧⽅框代表HDFS的Block。 通过函数将RD的每个分区存储为

HDFS上的⼀个Block。

![image 24](assets/imageFile24.png)

图24 saveAsObjectFile算⼦对RD转换

### （25） colect

colect 相当于 toAray， toAray 已经过时不推荐使⽤， colect 将分布式的 RD 返回为⼀个单机的 scala Aray 数组。在这个数组上运⽤ scala 的函数式操作。

图 25中左侧⽅框代表 RD 分区，右侧⽅框代表单机内存中的数组。通过函数操作，将结果返回到 Driver 程序所在的节点，以数组形式存储。

![image 25](assets/imageFile25.png)

图 25 Colect 算⼦对 RD 转换

### （26）colectAsMap

colectAsMap对（K，V）型的RD数据返回⼀个单机HashMap。 对于重复K的RD元素，后⾯的元素覆 盖前⾯的元素。

图26中的左侧⽅框代表RD分区，右侧⽅框代表单机数组。 数据通过colectAsMap函数返回给Driver程 序计算结果，结果以HashMap形式存储。

![image 26](assets/imageFile26.png)

图26 ColectAsMap算⼦对RD转换

### （27）reduceByKeyLocaly

实现的是先reduce再colectAsMap的功能，先对RD的整体进⾏reduce操作，然后再收集所有结果返回 为⼀个HashMap。

### （28）l okup

下⾯代码为l okup的声明。 l okup（key：K）：Seq[V] Lokup函数对（Key，Value）型的RD操作，返回指定Key对应的元素形成的Seq。 这个函数处理优化的部 分在于，如果这个RD包含分区器，则只会对应处理K所在的分区，然后返回由（K，V）形成的Seq。 如果 RD不包含分区器，则需要对全RD元素进⾏暴⼒扫描处理，搜索指定K对应的元素。

图28中的左侧⽅框代表RD分区，右侧⽅框代表Seq，最后结果返回到Driver所在节点的应⽤中。

![image 27](assets/imageFile27.png)

图28 l okup对RD转换

### （29） count

count 返回整个 RD 的元素个数。 内部函数实现为： defcount():Long=sc.runJob(this,Utils.getIteratorSize_).sum 图 29中，返回数据的个数为 5。⼀个⽅块代表⼀个 RD 分区。

![image 28](assets/imageFile28.png)

图29 count 对 RD 算⼦转换

### （30）top

top可返回最⼤的k个元素。 函数定义如下。 top（num：Int）（implicit ord：Ordering[T]）：Aray[T] 相近函数说明如下。 ·top返回最⼤的k个元素。 ·take返回最⼩的k个元素。 ·takeOrdered返回最⼩的k个元素，并且在返回的数组中保持元素的顺序。 ·first相当于top（1）返回整个RD中的前k个元素，可以定义排序的⽅式Ordering[T]。 返回的是⼀个含前k个元素的数组。

### （31）reduce

reduce函数相当于对RD中的元素进⾏reduceLeft函数的操作。 函数实现如下。 Some（iter.reduceLeft（cleanF）） reduceLeft先对两个元素<K，V>进⾏reduce函数操作，然后将结果和迭代器取出的下⼀个元素<k，V>

进⾏reduce函数操作，直到迭代器遍历完所有元素，得到最后结果。在RD中，先对每个分区中的所有元素 <K，V>的集合分别进⾏reduceLeft。 每个分区形成的结果相当于⼀个元素<K，V>，再对这个结果集合进⾏ reduceleft操作。

例如：⽤户⾃定义函数如下。 f：（A，B）=>（A._1+”@”+B._1，A._2+B._2） 图31中的⽅框代表⼀个RD分区，通过⽤户⾃定函数f将数据进⾏reduce运算。 示例

最后的返回结果为V1@[1]V2U！@U2@U3@U4，12。

![image 29](assets/imageFile29.png)

图31 reduce算⼦对RD转换

### （32）fold

fold和reduce的原理相同，但是与reduce不同，相当于每个reduce时，迭代器取的第⼀个元素是 zeroValue。

图32中通过下⾯的⽤户⾃定义函数进⾏fold运算，图中的⼀个⽅框代表⼀个RD分区。 读者可以参照 reduce函数理解。

fold（（”V0@”，2））（ （A，B）=>（A._1+”@”+B._1，A._2+B._2））

![image 30](assets/imageFile30.png)

图32 fold算⼦对RD转换

### （ 3）agregate

agregate先对每个分区的所有元素进⾏agregate操作，再对分区的结果进⾏fold操作。

agreagate与fold和reduce的不同之处在于，agregate相当于采⽤归并的⽅式进⾏数据聚集，这种聚集 是并⾏化的。 ⽽在fold和reduce函数的运算过程中，每个分区中需要进⾏串⾏处理，每个分区串⾏计算完结 果，结果再按之前的⽅式进⾏聚集，并返回最终聚集结果。

函数的定义如下。

agregate[B]（z： B）（seqop： （B，A） => B，combop： （B，B） => B）： B 图 3通过⽤户⾃定义函数对RD 进⾏agregate的聚集操作，图中的每个⽅框代表⼀个RD分区。 rd.agregate（”V0@”，2）（（A，B）=>（A._1+”@”+B._1，A._2+B._2）），（A，B）=>（A._1+”

@”+B_1，A._@+B_.2）） 最后，介绍两个计算模型中的两个特殊变量。 ⼴播（broadcast）变量：其⼴泛⽤于⼴播Map Side Join中的⼩表，以及⼴播⼤变量等场景。 这些数据

集合在单节点内存能够容纳，不需要像RD那样在节点之间打散存储。

Spark运⾏时把⼴播变量数据发到各个节点，并保存下来，后续计算可以复⽤。 相⽐Hado的distributed cache，⼴播的内容可以跨作业共享。 Broadcast的底层实现采⽤了BT机制。

![image 31](assets/imageFile31.png)

图 3 agregate算⼦对RD转换

- ②代表V。
- ③代表U。 acumulator变量：允许做全局累加操作，如acumulator变量⼴泛使⽤在应⽤中记录当前的运⾏指标的


情景。

-
