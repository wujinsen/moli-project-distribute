Parquet是⾯向分析型业务的列式存储格式，由Twiter和Cloudera合作开发，2015年5⽉从Apache的 孵化器⾥毕业成为Apache顶级项⽬，最新的版本是1.8.0。

# 列式存储

列式存储和⾏式存储相⽐有哪些优势呢？

- 1.
- 2.
- 3.


可以跳过不符合条件的数据，只读取需要的数据，降低IO数据量。 压缩编码可以降低磁盘存储空间。由于同⼀列的数据类型是⼀样的，可以使⽤更⾼效的压缩编码 （例如Run Length Encoding和Delta Encoding）进⼀步节约存储空间。 只读取需要的列，⽀持向量运算，能够获取更好的扫描性能。

当时Twiter的⽇增数据量达到压缩之后的10TB+，存储在HDFS上，⼯程师会使⽤多种计算框架（例 如MapReduce, Hive, Pig等）对这些数据做分析和挖掘；⽇志结构是复杂的嵌套数据类型，例如⼀个 典型的⽇志的schema有87列，嵌套了7层。所以需要设计⼀种列式存储格式，既能⽀持关系型数据 （简单数据类型），⼜能⽀持复杂的嵌套类型的数据，同时能够适配多种数据处理框架。 关系型数据的列式存储，可以将每⼀列的值直接排列下来，不⽤引⼊其他的概念，也不会丢失数据。 关系型数据的列式存储⽐较好理解，⽽嵌套类型数据的列存储则会遇到⼀些麻烦。如图1所示，我们把 嵌套数据类型的⼀⾏叫做⼀个记录（record)，嵌套数据类型的特点是⼀个record中的column除了可以 是Int, Long, String这样的原语（primitive）类型以外，还可以是List, Map, Set这样的复杂类型。在⾏ 式存储中⼀⾏的多列是连续的写在⼀起的，在列式存储中数据按列分开存储，例如可以只读取A.B.C这 ⼀列的数据⽽不去读A.E和A.B.D，那么如何根据读取出来的各个列的数据重构出⼀⾏记录呢？

![image 1](<Parquet格式详解.note_images/imageFile1.png>)

- 图1 ⾏式存储和列式存储 Gogle的 系统解决了这个问题，核⼼思想是使⽤“record shreding and asembly algorithm” 来表示复杂的嵌套数据类型，同时辅以按列的⾼效压缩和编码技术，实现降低存储空间，提⾼IO效 率，降低上层应⽤延迟。Parquet就是基于Dremel的数据模型和算法实现的。


Dremel

Parquet适配多种计算框架

Parquet是语⾔⽆关的，⽽且不与任何⼀种数据处理框架绑定在⼀起，适配多种语⾔和组件，能够与 Parquet配合的组件有： 查询引擎: Hive, Impala, Pig, Presto, Dril, Tajo, HAWQ, IBM Big SQL 计算框架: MapReduce, Spark, Cascading, Crunch, Scalding, Kite 数据模型: Avro, Thrift, Protocol Bufers, POJOs 那么Parquet是如何与这些组件协作的呢？这个可以通过图2来说明。数据从内存到Parquet⽂件或者反 过来的过程主要由以下三个部分组成：

- 1, 存储格式(storage format) 项⽬定义了Parquet内部的数据类型、存储格式等。
- 2, 对象模型转换器(object model converters) 这部分功能由 项⽬来实现，主要完成外部对象模型与Parquet内部数据类型的映射。
- 3, 对象模型(object models) 对象模型可以简单理解为内存中的数据表示，Avro, Thrift, Protocol Bufers, Hive SerDe, Pig Tuple, Spark SQL InternalRow等这些都是对象模型。Parquet也提供了⼀个 帮助⼤家 理解。 例如 项⽬⾥的parquet-pig项⽬就是负责把内存中的Pig Tuple序列化并按列存储成Parquet 格式，以及反过来把Parquet⽂件的数据反序列化成Pig Tuple。 这⾥需要注意的是Avro, Thrift, Protocol Bufers都有他们⾃⼰的存储格式，但是Parquet并没有使⽤他 们，⽽是使⽤了⾃⼰在 项⽬⾥定义的存储格式。所以如果你的应⽤使⽤了Avro等对象 模型，这些数据序列化到磁盘还是使⽤的 定义的转换器把他们转换成Parquet⾃⼰的存储格 式。


parquet-format

parquet-mr

example object model

parquet-mr

parquet-format

parquet-mr

![image 2](<Parquet格式详解.note_images/imageFile2.png>)

- 图2 Parquet项⽬的结构


# Parquet数据模型

理解Parquet⾸先要理解这个列存储格式的数据模型。我们以⼀个下⾯这样的schema和数据为例来说 明这个问题。

- 1 message AddressBook {

- 2 required string owner;

- 3 repeated string ownerPhoneNumbers;

- 4 repeated group contacts {

- 5 required string name;

- 6 optional string phoneNumber;

- 7 }

- 8 }


这个schema中每条记录表示⼀个⼈的AdresBok。有且只有⼀个owner，owner可以有0个或者多个 ownerPhoneNumbers，owner可以有0个或者多个contacts。每个contact有且只有⼀个name，这个 contact的phoneNumber可有可⽆。这个schema可以⽤图3的树结构来表示。 每个schema的结构是这样的：根叫做mesage，mesage包含多个fields。每个field包含三个属性： repetition, type, name。repetition可以是以下三种：required（出现1次），optional（出现0次或者1 次），repeated（出现0次或者多次）。type可以是⼀个group或者⼀个primitive类型。 Parquet格式的数据类型没有复杂的Map, List, Set等，⽽是使⽤repeated fields 和 groups来表示。例 如List和Set可以被表示成⼀个repeated field，Map可以表示成⼀个包含有key-value 对的repeated field，⽽且key是required的。

![image 3](<Parquet格式详解.note_images/imageFile3.png>)

- 图3 AdresBok的树结构表示


# Parquet⽂件的存储格式

那么如何把内存中每个AdresBok对象按照列式存储格式存储下来呢？ 在Parquet格式的存储中，⼀个schema的树结构有⼏个叶⼦节点，实际的存储中就会有多少column。 例如上⾯这个schema的数据存储实际上有四个column，如图4所示。

![image 4](<Parquet格式详解.note_images/imageFile4.png>)

- 图4 AdresBok实际存储的列 Parquet⽂件在磁盘上的分布情况如图5所示。所有的数据被⽔平切分成Row group，⼀个Row group包 含这个Row group对应的区间内的所有列的column chunk。⼀个column chunk负责存储某⼀列的数 据，这些数据是这⼀列的Repetition levels, Definition levels和values（详⻅后⽂）。⼀个column chunk是由Page组成的，Page是压缩和编码的单元，对数据模型来说是透明的。⼀个Parquet⽂件最后 是Foter，存储了⽂件的元数据信息和统计信息。Row group是数据读写时候的缓存单元，所以推荐设 置较⼤的Row group从⽽带来较⼤的并⾏度，当然也需要较⼤的内存空间作为代价。⼀般情况下推荐配 置⼀个Row group⼤⼩1G，⼀个HDFS块⼤⼩1G，⼀个HDFS⽂件只含有⼀个块。
- 图5 Parquet⽂件格式在磁盘的分布 拿我们的这个schema为例，在任何⼀个Row group内，会顺序存储四个column chunk。这四个 column都是string类型。这个时候Parquet就需要把内存中的AdresBok对象映射到四个string类型 的column中。如果读取磁盘上的4个column要能够恢复出AdresBok对象。这就⽤到了我们前⾯提 到的 “record shreding and asembly algorithm”。


![image 5](<Parquet格式详解.note_images/imageFile5.png>)

# Striping/Asembly算法

对于嵌套数据类型，我们除了存储数据的value之外还需要两个变量Repetition Level(R), Definition Level(D) 才能存储其完整的信息⽤于序列化和反序列化嵌套数据类型。Repetition Level和 Definition Level可以说是为了⽀持嵌套类型⽽设计的，但是它同样适⽤于简单数据类型。在Parquet中我们只需 定义和存储schema的叶⼦节点所在列的Repetition Level和Definition Level。

Definition Level

嵌套数据类型的特点是有些field可以是空的，也就是没有定义。如果⼀个field是定义的，那么它的所 有的⽗节点都是被定义的。从根节点开始遍历，当某⼀个field的路径上的节点开始是空的时候我们记 录下当前的深度作为这个field的Definition Level。如果⼀个field的Definition Level等于这个field的最⼤ Definition Level就说明这个field是有数据的。对于required类型的field必须是有定义的，所以这个 Definition Level是不需要的。在关系型数据中，optional类型的field被编码成0表示空和1表示⾮空（或 者反之）。

Repetition Level

记录该field的值是在哪⼀个深度上重复的。只有repeated类型的field需要Repetition Level，optional 和 required类型的不需要。Repetition Level = 0 表示开始⼀个新的record。在关系型数据中， repetion level总是0。 下⾯⽤AdresBok的例⼦来说明Striping和asembly的过程。 对于每个column的最⼤的Repetion Level和 Definition Level如图6所示。

![image 6](<Parquet格式详解.note_images/imageFile6.png>)

- 图6 AdresBok的Max Definition Level和Max Repetition Level 下⾯这样两条record：


- 1 AddressBook {

- 2 owner: "Julien Le Dem",

- 3 ownerPhoneNumbers: "555 123 4567",

- 4 ownerPhoneNumbers: "555 666 1337",

- 5 contacts: {

- 6 name: "Dmitriy Ryaboy",

- 7 phoneNumber: "555 987 6543",

- 8 },

- 9 contacts: {

- 10 name: "Chris Aniszczyk"

- 11 }

- 12 }

- 13 AddressBook {

- 14 owner: "A. Nonymous"

- 15 }

- 16


以contacts.phoneNumber这⼀列为例，" 5 987 6543"这个contacts.phoneNumber的Definition Level是最⼤Definition Level=2。⽽如果⼀个contact没有phoneNumber，那么它的Definition Level就 是1。如果连contact都没有，那么它的Definition Level就是0。 下⾯我们拿掉其他三个column只看contacts.phoneNumber这个column，把上⾯的两条record简化成 下⾯的样⼦：

- 1 AddressBook {

- 2 contacts: {

- 3 phoneNumber: "555 987 6543"

- 4 }

- 5 contacts: {

- 6 }

- 7 }

- 8 AddressBook {

- 9 }

- 10


这两条记录的序列化过程如图7所示：

![image 7](<Parquet格式详解.note_images/imageFile7.png>)

- 图7 ⼀条记录的序列化过程 如果我们要把这个column写到磁盘上，磁盘上会写⼊这样的数据（图8）：
- 图8 ⼀条记录的磁盘存储 注意：NUL实际上不会被存储，如果⼀个column value的Definition Level⼩于该column最⼤ Definition Level的话，那么就表示这是⼀个空值。 下⾯是从磁盘上读取数据并反序列化成AdresBok对象的过程：


![image 8](<Parquet格式详解.note_images/imageFile8.png>)

- 1，读取第⼀个三元组R=0, D=2, Value=” 5 987 6543”

- R=0 表示是⼀个新的record，要根据schema创建⼀个新的nested record直到Definition Level=2。 D=2 说明Definition Level=Max Definition Level，那么这个Value就是contacts.phoneNumber这⼀列 的值，赋值操作contacts.phoneNumber=” 5 987 6543”。

2，读取第⼆个三元组 R=1, D=1

- R=1 表示不是⼀个新的record，是上⼀个record中⼀个新的contacts。 D=1 表示contacts定义了，但是contacts的下⼀个级别也就是phoneNumber没有被定义，所以创建⼀ 个空的contacts。


- 3，读取第三个三元组 R=0, D=0


R=0 表示⼀个新的record，根据schema创建⼀个新的nested record直到Definition Level=0，也就是 创建⼀个AdresBok根节点。 可以看出在Parquet列式存储中，对于⼀个schema的所有叶⼦节点会被当成column存储，⽽且叶⼦节 点⼀定是primitive类型的数据。对于这样⼀个primitive类型的数据会衍⽣出三个sub columns (R, D, Value)，也就是从逻辑上看除了数据本身以外会存储⼤量的Definition Level和Repetition Level。那么 这些Definition Level和Repetition Level是否会带来额外的存储开销呢？实际上这部分额外的存储开销 是可以忽略的。因为对于⼀个schema来说level都是有上限的，⽽且⾮repeated类型的field不需要 Repetition Level，required类型的field不需要Definition Level，也可以缩短这个上限。例如对于 Twiter的7层嵌套的schema来说，只需要3个bits就可以表示这两个Level了。 对于存储关系型的record，record中的元素都是⾮空的（NOT NUL in SQL）。Repetion Level和 Definition Level都是0，所以这两个sub column就完全不需要存储了。所以在存储⾮嵌套类型的时候， Parquet格式也是⼀样⾼效的。 上⾯演示了⼀个column的写⼊和重构，那么在不同column之间是怎么跳转的呢，这⾥⽤到了有限状态 机的知识，详细介绍可以参考 。

Dremel

# 数据压缩算法

列式存储给数据压缩也提供了更⼤的发挥空间，除了我们常⻅的snapy, gzip等压缩⽅法以外，由于列 式存储同⼀列的数据类型是⼀致的，所以可以使⽤更多的压缩算法。

<table>
  <tr>
    <th>压缩算法</th>
    <th>使⽤场景</th>
  </tr>
  <tr>
    <td>Run Length Encoding</td>
    <td>重复数据</td>
  </tr>
  <tr>
    <td>Delta Encoding</td>
    <td>有序数据集，例如timestamp，⾃动⽣成的ID，以</td>
  </tr>
  <tr>
    <td>Dictionary Encoding</td>
    <td>及监控的各种metrics ⼩规模的数据集合，例如IP地址</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
  </tr>
</table>


Prefix Encoding Delta Encoding for strings

# 性能

Parquet列式存储带来的性能上的提⾼在业内已经得到了充分的认可，特别是当你们的表⾮常宽 （column⾮常多）的时候，Parquet⽆论在资源利⽤率还是性能上都优势明显。具体的性能指标详⻅参 考⽂档。 Spark已经将Parquet设为默认的⽂件存储格式，Cloudera投⼊了很多⼯程师到Impala+Parquet相关开 发中，Hive/Pig都原⽣⽀持Parquet。Parquet现在为Twiter⾄少节省了1/3的存储空间，同时节省了⼤ 量的表扫描和反序列化的时间。这两⽅⾯直接反应就是节约成本和提⾼性能。 如果说HDFS是⼤数据时代⽂件系统的事实标准的话，Parquet就是⼤数据时代存储格式的事实标准。

