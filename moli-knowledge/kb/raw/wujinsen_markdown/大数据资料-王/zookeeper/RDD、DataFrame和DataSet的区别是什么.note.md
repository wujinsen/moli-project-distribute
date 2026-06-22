RD、DataFrame和DataSet是容易产⽣混淆的概念，必须对其相互之间对⽐，才可以知道其中异同：DataFrame多 了数据的结构信息，即schema。RD是分布式的 Java对象的集合。DataFrame是分布式的Row对象的集合。

RD、DataFrame和DataSet是容易产⽣混淆的概念，必须对其相互之间对⽐，才可以知道其中异同。 RD和DataFrame

RD-DataFrame 上图直观地体现了DataFrame和RD的区别。左侧的RD[Person]虽然以Person为类型参数，但Spark 框架本身不了解 Person类的内部结构。⽽右侧的DataFrame却提供了详细的结构信息，使得Spark SQL可以清楚地知道该数据集中包含哪些列，每列的名称和类型各是什么。DataFrame多了数据的结 构信息，即schema。RD是分布式的 Java对象的集合。DataFrame是分布式的Row对象的集合。 DataFrame除了提供了⽐RD更丰富的算⼦以外，更重要的特点是提升执⾏效率、减少数据读取以及 执⾏计划的优化，⽐如filter下推、裁剪等。 提升执⾏效率 RD API是函数式的，强调不变性，在⼤部分场景下倾向于创建新对象⽽不是修改⽼对象。这⼀特点虽 然带来了⼲净整洁的API，却也使得Spark应⽤程序在运⾏期倾向于创建⼤量临时对象，对GC造成压 ⼒。在现有RD API的基础之上，我们固然可以利⽤mapPartitions⽅法来重载RD单个分⽚内的数据 创建⽅式，⽤复⽤可变对象的⽅式来减⼩对象分配和GC的开销，但这牺牲了代码的可读性，⽽且要求 开发者对Spark运⾏时机制有⼀定的了解，⻔槛较⾼。另⼀⽅⾯，Spark SQL在框架内部已经在各种可 能的情况下尽量重⽤对象，这样做虽然在内部会打破了不变性，但在将数据返回给⽤户时，还会重新 转为不可变数据。利⽤ DataFrame API进⾏开发，可以免费地享受到这些优化效果。 减少数据读取 分析⼤数据，最快的⽅法就是 ⸺忽略它。这⾥的“忽略”并不是熟视⽆睹，⽽是根据查询条件进⾏恰当 的剪枝。 上⽂讨论分区表时提到的分区剪 枝便是其中⼀种⸺当查询的过滤条件中涉及到分区列时，我们可以 根据查询条件剪掉肯定不包含⽬标数据的分区⽬录，从⽽减少IO。 对于⼀些“智能”数据格 式，Spark SQL还可以根据数据⽂件中附带的统计信息来进⾏剪枝。简单来 说，在这类数据格式中，数据是分段保存的，每段数据都带有最⼤值、最⼩值、nul值数量等 ⼀些基本 的统计信息。当统计信息表名某⼀数据段肯定不包括符合查询条件的⽬标数据时，该数据段就可以直 接跳过(例如某整数列a某段的最⼤值为10，⽽查询条件要求a > 20)。 此外，Spark SQL也可以充分利⽤RCFile、ORC、Parquet等列式存储格式的优势，仅扫描查询真正涉 及的列，忽略其余列的数据。 执⾏优化

⼈⼝数据分析示例

为了说明查询优化，我们来看上图展示的⼈⼝数据分析的示例。图中构造了两个DataFrame，将它们 join之后⼜做了⼀次filter操作。如果原封不动地执⾏这个执⾏计划，最终的执⾏效率是不⾼的。因为 join是⼀个代价较⼤的操作，也可能会产⽣⼀个较⼤的数据集。如果我们能将filter 下推到 join下⽅，先 对DataFrame进⾏过滤，再join过滤后的较⼩的结果集，便可以有效缩短执⾏时间。⽽Spark SQL的查 询优化器正是这样做的。简⽽⾔之，逻辑查询计划优化就是⼀个利⽤基于关系代数的等价变换，将⾼ 成本的操作替换为低成本操作的过程。 得到的优化执⾏计划在转换成物 理执⾏计划的过程中，还可以根据具体的数据源的特性将过滤条件下 推⾄数据源内。最右侧的物理执⾏计划中Filter之所以消失不⻅，就是因为溶⼊了⽤于执⾏最终的读取 操作的表扫描节点内。 对于普通开发者⽽⾔，查询优化 器的意义在于，即便是经验并不丰富的程序员写出的次优的查询，也 可以被尽量转换为⾼效的形式予以执⾏。 RD和DataSet

DataSet以Catalyst逻辑执⾏计划表示，并且数据以编码的⼆进制形式被存储，不需要反序列化就可 以执⾏sorting、shufle等操作。

DataSet创⽴需要⼀个显式的Encoder，把对象序列化为⼆进制，可以把对象的scheme映射为 SparkSQl类型，然⽽RD依赖于运⾏时反射机制。

通过上⾯两点，DataSet的性能⽐RD的要好很多。 DataFrame和DataSet Dataset可以认为是DataFrame的⼀个特例，主要区别是Dataset每⼀个record存储的是⼀个强类型值 ⽽不是⼀个Row。因此具有如下三个特点： DataSet可以在编译时检查类型 并且是⾯向对象的编程接⼝。⽤wordcount举例：

- 1 //DataFrame

- 2

- 3 // Load a text file and interpret each line as a java.lang.String

- 4 val ds = sqlContext.read.text("/home/spark/1.6/lines").as[String]

- 5 val result = ds

- 6 .flatMap(_.split(" ")) // Split on whitespace

- 7 .filter(_ != "") // Filter empty words

.toDF() // Convert to DataFrame to perform aggregation / sorting

- 8

.groupBy($"value") // Count number of occurences of each word

- 9

- 10 .agg(count("*") as "numOccurances")

- 11 .orderBy($"numOccurances" desc) // Show most common words first


后⾯版本DataFrame会继承DataSet，DataFrame是⾯向Spark SQL的接⼝。

- 1 //DataSet,完全使⽤scala编程，不要切换到DataFrame

- 2

- 3 val wordCount =

- 4 ds.flatMap(_.split(" "))

- 5 .filter(_ != "")

.groupBy(_.toLowerCase()) // Instead of grouping on a column expression (i.e. $"value") we pass a lambda function

- 6

- 7 .count()


DataFrame和DataSet可以相互转化， df.as[ElementType] 这样可以把DataFrame转化为DataSet， ds.toDF() 这样可以把DataSet转化为DataFrame。

