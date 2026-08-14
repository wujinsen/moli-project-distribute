---
title: MAPREDUCE详解.note（原文插图 annex）
slug: annex-MAPREDUCE详解
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/Hadoop/MAPREDUCE详解.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

⽬录 课程⼤纲（MAPREDUCE详解） . 3

- 1. MAPREDUCE⼊⻔ . 4

- 1.1 为什么要MAPREDUCE . 4
- 1.2 MAPREDUCE程序运⾏演⽰ . 4
- 1.3 MAPREDUCE ⽰例编写及编程规范 . 4

- 1.3.1 编程规范 . 4
- 1.3.2 wordcount⽰例编写 . 5


- 1.4 MAPREDUCE程序运⾏模式及debug⽅法 . 7


- 1.4.1 本地运⾏模式 . 7
- 1.4.2 集群运⾏模式 . 7


- 2. Mapreduce程序的核⼼运⾏机制 . 8

- 2.1 概述 . 8
- 2.2 mr程序运⾏流程 . 8


- 2.2.1 流程⽰意图 . 8
- 2.2.2 流程解析 . 8
- 2.3 Maptask实例数的决定机制 . 10

2.3.1 maptask数量的决定机制 . 10

- 2.3.2切⽚机制： . 10

2.4 ReduceTask实例数的决定 . 1

- 3. MAPREDUCE中的Combiner . 12
- 4. MAPREDUCE中的序列化 . 12

- 4.1 概述 . 12

- 4.2 Jdk序列化和MR序列化之间的⽐较 . 12
- 4.3 ⾃定义对象实现MR中的序列化接⼜ . 13


- 5. Mapreduce中的排序初步 . 16


- 5.1 需求： . 16

- 5.2 分析 . 16
- 5.3 实现 . 16


- 6. Mapreduce中的分区Partitioner . 20

- 6.1 需求： . 20

- 6.2 分析 . 20
- 6.3 实现 . 20


- 7. mapreduce的shufle机制 . 2


- 7.1 概述： . 2






- 7.2 主要流程： . 2
- 7.3 详细流程 . 2


- 课程⼤纲（MAPREDUCE详解）
- 7.4 详细流程⽰意图 . 23


- 8. mapreduce数据压缩 . 24

- 8.1 概述 . 24

- 8.2 MR⽀持的压缩编码 . 24
- 8.3 Reducer输出压缩 . 24
- 8.4 Maper输出压缩 . 25
- 8.5 压缩⽂件的读取 . 25


- 9. MapReduce与YARN . 27


- 9.1 YARN概述 . 27

- 9.2 YARN的重要概念 . 27
- 9.3 Yarn中运⾏运算程序的⽰例 . 27


- 10. MapReduce编程案例 . 28


- 10.1 reduce端join算法实现 . 28
- 10.2 map端join算法实现 . 29
- 10.3 web⽇志预处理 . 32 附：Mapreduce参数优化 . 36


- 1.1 资源相关参数 . 36
- 1.2 容错相关参数 . 37
- 1.3 本地运⾏mapreduce 作业 . 37
- 1.4 效率和稳定性相关参数 . 37


<table>
  <tr>
    <th rowspan="4">MapReduce快速⼊门</th>
    <th>如何理解map、reduce计算模型</th>
  </tr>
  <tr>
    <td>Mapreudce程序运⾏演⽰</td>
  </tr>
  <tr>
    <td>Mapreduce编程规范及⽰例编写</td>
  </tr>
  <tr>
    <td>Mapreduce程序运⾏模式及debug⽅法</td>
  </tr>
  <tr>
    <td rowspan="7">MapReduce⾼级特性</td>
    <td>Mapreduce程序的核⼼机制</td>
  </tr>
  <tr>
    <td>MapReduce的序列化框架</td>
  </tr>
  <tr>
    <td>MapReduce的排序实现</td>
  </tr>
  <tr>
    <td>MapReduce的分区机制及⾃定义</td>
  </tr>
  <tr>
    <td>Mapreduce的数据压缩</td>
  </tr>
  <tr>
    <td>Mapreduce与yarn的结合</td>
  </tr>
  <tr>
    <td>Mapreduce编程案例</td>
  </tr>
  <tr>
    <td> </td>
    <td>参数优化</td>
  </tr>
</table>


Mapreduce

⽬标： 掌握mapreduce分布式运算框架的编程思想 掌握mapreduce常⽤算法的编程套路 掌握mapreduce分布式运算框架的运⾏机制，具备⼀定⾃定义开发的能⼒

# 1. MAPREDUCE原理篇（1）

Mapreduce是⼀个分布式运算程序的编程框架，是⽤户开发“基于hadop的数据分析应⽤”的核⼼框 架； Mapreduce核⼼功能是将⽤户编写的业务逻辑代码和⾃带默认组件整合成⼀个完整的分布式运算程 序，并发运⾏在⼀个hadop集群上；

## 1.1 为什么要MAPREDUCE

- （1）海量数据在单机上处理因为硬件资源限制，⽆法胜任
- （2）⽽⼀旦将单机版程序扩展到集群来分布式运⾏，将极⼤增加程序的复杂度和开发难度
- （3）引⼊mapreduce框架后，开发⼈员可以将绝⼤部分⼯作集中在业务逻辑的开发上，⽽将分布式计 算中的复杂性交由框架来处理


#### 设想⼀个海量数据场景下的wordcount需求：

<table>
  <tr>
    <th>单机版：内存受限，磁盘受限，运算能⼒受限 分布式： ⽂件分布式存储（HDFS） 运算逻辑需要⾄少分成2个阶段（⼀个阶段独⽴并发，⼀个阶段汇聚） 运算程序如何分发 程序如何分配运算任务（切⽚） 两阶段的程序如何启动？如何协调？ 整个程序运⾏过程中的监控？容错？重试？</th>
  </tr>
</table>


可见在程序由单机版扩成分布式时，会引⼊⼤量的复杂⼯作。为了提⾼开发效率，可以将分布式程序 中的公共功能封装成框架，让开发⼈员可以将精⼒集中于业务逻辑。

⽽mapreduce就是这样⼀个分布式程序的通⽤框架，其应对以上问题的整体结构如下：

<table>
  <tr>
    <th>MRApMaster(mapreduce aplication master) MapTask</th>
  </tr>
</table>


ReduceTask

## 1.2MAPREDUCE框架结构及核心运行机制

- 1.2.1结构 ⼀个完整的mapreduce程序在分布式运⾏时有三类实例进程：


- 1、MRApMaster：负责整个程序的过程调度及状态协调
- 2、mapTask：负责map阶段的整个数据处理流程
- 3、ReduceTask：负责reduce阶段的整个数据处理流程


- 1.2.2 MR程序运⾏流程


- 1.2.2.1流程⽰意图

- 1.2.2.2流程解析


![image 1](assets/imageFile1.png)

1.

⼀个mr程序启动的时候，最先启动的是MRApMaster，MRApMaster启动后根据本次job的描述 信息，计算出需要的maptask实例数量，然后向集群申请机器启动相应数量的maptask进程

1.

maptask进程启动之后，根据给定的数据切⽚范围进⾏数据处理，主体流程为： 利⽤客户指定的inputformat来获取RecordReader读取数据，形成输⼊KV对 将输⼊KV对传递给客户定义的map()⽅法，做逻辑运算，并将map()⽅法输出的KV对收集到缓 存 将缓存中的KV对按照K分区排序后不断溢写到磁盘⽂件

- a.
- b.
- c.


- 1.


MRApMaster监控到所有maptask进程任务完成之后，会根据客户指定的参数启动相应数量的 reducetask进程，并告知reducetask进程要处理的数据范围（数据分区）

- 1.


Reducetask进程启动之后，根据MRApMaster告知的待处理数据所在位置，从若⼲台maptask运

⾏所在机器上获取到若⼲个maptask输出结果⽂件，并在本地进⾏重新归并排序，然后按照相同 key的KV为⼀个组，调⽤客户定义的reduce()⽅法进⾏逻辑运算，并收集运算输出的结果KV，然 后调⽤客户指定的outputformat将结果数据输出到外部存储

- 1.3MapTask并行度决定机制


maptask的并⾏度决定map阶段的任务处理并发度，进⽽影响到整个job的处理速度 那么，mapTask并⾏实例是否越多越好呢？其并⾏度又是如何决定呢？

- 1.3.1 mapTask并⾏度的决定机制 ⼀个job的map阶段并⾏度由客户端在提交job时决定 ⽽客户端对map阶段并⾏度的规划的基本逻辑为： 将待处理数据执⾏逻辑切⽚（即按照⼀个特定切⽚⼤⼩，将待处理数据划分成逻辑上的多个split）， 然后每⼀个split分配⼀个mapTask并⾏实例处理


这段逻辑及形成的切⽚规划描述⽂件，由InputFormat实现类的getSplits()⽅法完成，其过程如下图：

![image 2](assets/imageFile2.png)

### 1.3.2 FileInputFormat切⽚机制

- 1、切⽚定义在InputFormat类中的getSplit()⽅法

- 2、FileInputFormat中默认的切⽚机制： 简单地按照⽂件的内容长度进⾏切⽚ 切⽚⼤⼩，默认等于block⼤⼩


- 1.
- 2.


- 3.


切⽚时不考虑数据集整体，⽽是逐个针对每⼀个⽂件单独切⽚

⽐如待处理数据有两个⽂件：

<table>
  <tr>
    <th>1.txt 320M</th>
  </tr>
</table>


file2.txt 10M

经过FileInputFormat的切⽚机制运算后，形成的切⽚信息如下：

<table>
  <tr>
    <th>il 1.tt. lit1- 0~128<br>il 1.tt. lit2-128~256 il 1.txt.split3-256~320<br></th>
  </tr>
</table>


file2.txt.split1- 0~10M

- 3、FileInputFormat中切⽚的⼤⼩的参数配置 通过分析源码，在FileInputFormat中，计算切⽚⼤⼩的逻辑：Math.max(minSize, Math.min(maxSize, blockSize); 切⽚主要由这⼏个值来运算决定


<table>
  <tr>
    <th>mins 配置参数：ize：默认值：1 .inpu</th>
  </tr>
  <tr>
    <td>mapreduce t.fileinputformat.split.minsize maxsize：默认值：Long.MAXValue<br><br>配置参数：map putfo</td>
  </tr>
  <tr>
    <td>reduce.input.filein rmat.split.maxsize</td>
  </tr>
</table>


blocksize

因此，默认情况下，切⽚⼤⼩=blocksize maxsize（切⽚最⼤值）： 参数如果调得⽐blocksize⼩，则会让切⽚变⼩，⽽且就等于配置的这个参数的值 minsize （切⽚最⼩值）： 参数调的⽐blockSize⼤，则可以让切⽚变得⽐blocksize还⼤

选择并发数的影响因素：

- 1.
- 2.
- 3.


运算节点的硬件配置 运算任务的类型：CPU密集型还是IO密集型 运算任务的数据量

## 1.4map并行度的经验之谈

如果硬件配置为2*12core + 64G，恰当的map并⾏度是⼤约每个节点20-10个map，最好每个map的 执⾏时间⾄少⼀分钟。

如果job的每个map或者 reduce task的运⾏时间都只有30-40秒钟，那么就减少该job的map或者

reduce数，每⼀个task(map|reduce)的setup和加⼊到调度器中进⾏调度，这个中间的过程可能都 要花费⼏秒钟，所以如果每个task都⾮常快就跑完了，就会在task的开始和结束的时候浪费太多的 时间。

配置task的JVM重⽤ 可以改善该问题： （mapred.job.reuse.jvm.num.tasks，默认是1，表⽰⼀个JVM上最多可以顺序执⾏的task 数⽬（属于同⼀个Job）是1。也就是说⼀个task启⼀个JVM）

[dht1]

如果input的⽂件⾮常的⼤，⽐如1TB，可以考虑将hdfs上的每个block size设⼤，⽐如设成256MB 或者512MB

## 1.5ReduceTask并行度的决定

reducetask的并⾏度同样影响整个job的执⾏并发度和执⾏效率，但与maptask的并发数由切⽚数决定 不同，Reducetask数量的决定是可以直接⼿动设置：

/默认值是1，⼿动设置为4 job.setNumReduceTasks(4);

如果数据分布不均匀，就有可能在reduce阶段产⽣数据倾斜 注意： reducetask数量并不是任意设置，还要考虑业务逻辑需求，有些情况下，需要计算全局汇总结 果，就只能有1个reducetask

尽量不要运⾏太多的reduce task。对⼤多数job来说，最好rduce的个数最多和集群中的reduce持平， 或者⽐集群的 reduce slots⼩。这个对于⼩集群⽽⾔，尤其重要。

- 1.6MAPREDUCE程序运行演示

Hadop的发布包中内置了⼀个hadop-mapreduce-example-2.4.1.jar，这个jar包中有各种MR⽰例程 序，可以通过以下步骤运⾏： 启动hdfs，yarn 然后在集群中的任意⼀台服务器上启动执⾏程序（⽐如运⾏wordcount）： hadop jar hadop-mapreduce-example-2.4.1.jar wordcount /wordcount/data /wordcount/out

- 2. MAPREDUCE实践篇（1）


## 2.1MAPREDUCE 示例编写及编程规范

- 2.1.1编程规范 ⽤户编写的程序分成三个部分：Maper，Reducer，Driver(提交运⾏mr程序的客户端) Maper的输⼊数据是KV对的形式（KV的类型可⾃定义） Maper的输出数据是KV对的形式（KV的类型可⾃定义） Maper中的业务逻辑写在map()⽅法中 map()⽅法（maptask进程）对每⼀个<K,V>调⽤⼀次 Reducer的输⼊数据类型对应Maper的输出数据类型，也是KV Reducer的业务逻辑写在reduce()⽅法中 Reduce()⽅法对每⼀组相同k的<k,v>组调⽤⼀次 ⽤户的Maper和Reducer都要继承各⾃的⽗类 整个程序需要⼀个Drvier来进⾏提交，提交的是⼀个描述了各种必要信息的job对象


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


- 1.7.2 wordcount⽰例编写 需求：在⼀堆给定的⽂本⽂件中统计输出每⼀个单词出现的总次数


- (1)定义⼀个maper类
- (2)定义⼀个reducer类


<table>
  <tr>
    <th>/⾸先要定义四个泛型的类型 /keyin: LongWritable valuein: Text /keyout: Text valueout:IntWritable<br><br>public class WordCountMaper extends Maper<LongWritable, Text, Text, IntWritable>{ //map⽅法的⽣命周期： 框架每传⼀⾏数据就被调⽤⼀次 //key : 这⼀⾏的起始点在⽂件中的偏移量 //value: 这⼀⾏的内容<br><br>@Overprotectreidde void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {<br><br>/拿到⼀⾏数据转换为string String line = value.toString(); /将这⼀⾏切分出各个单词 String[] words = line.split(" ");<br><br>/遍历数组，输出<单词，1> for(String word:words){<br><br>context.write(new Text(word), new IntWritable(1);<br><br>} }</th>
  </tr>
</table>


}

<table>
  <tr>
    <th>/⽣命周期：框架每传递进来⼀个kv 组，reduce⽅法被调⽤⼀次 @Override protected//定义⼀个void red计数器uce(Text key, Iterable<IntWritable>values, Context context) throws IOException, InterruptedException {<br><br>//遍历这⼀int count= 组k 0; v的所有v，累加到count中<br><br>for(In} cotWurnitta +b=levalue. value:vagetlu()e;s){ context.write(key, new IntWritable(count);<br><br>}</th>
  </tr>
</table>


}

- (3)定义⼀个主类，⽤来描述job并提交job


<table>
  <tr>
    <th>public clas WordCountRuner { /把业务逻辑相关的信息（哪个是maper，哪个是reducer，要处理的数据在哪⾥，输出的结果放哪⾥……）描述成⼀个job对象 /把这个描述好的job提交给集群去运⾏<br><br>public static void main(String[] args) throws Exception { Configuration conf = new Conﬁguration(); Job wcjob =Job.getInstance(conf);<br><br>/指定我这个job所在的jar包 / wcjob.setJar("/home/hadop/wordcount.jar"); wcjob.setJarByClas(WordCountRunner.clas);<br><br>wcjob.setMapperClas(WordCountMapper.clas); wcjob.setReducerClas(WordCountReducer.class);<br><br>/设置我们的业务逻辑Maper类的输出key和value的数据类型 wcjob.setMapOutputKeyClass(Text.class); wcjob.setMapOutputValueClass(IntWritable.class);<br><br>/设置我们的业务逻辑Reducer类的输出key和value的数据类型 wcjob.setOutputKeyClas(Text.class); wcjob.setOutputValueClass(IntWritable.clas);<br><br>/指定要处理的数据所在的位置 FileInputFormat.setInputPaths(wcjob, "hdfs://hdp-server01:9000/wordcount/data/big.txt"); /指定处理完成之后的结果所保存的位置 FileOutputFormat.setOutputPath(wcjob, new Path("hdfs://hdp-server01:9000/wordcount/output/");<br><br>/向yarn集群提交这个job bolean res= wcjob.waitForCompletion(true); System.exit(res?0 1);</th>
  </tr>
</table>


}

## 2.2MAPREDUCE程序运行模式

- 2.2.1本地运⾏模式


- 1.
- 2.


mapreduce程序是被提交给LocalJobRuner在本地以单进程的形式运⾏ ⽽处理的数据及输出结果可以在本地⽂件系统，也可以在hdfs上

- 3.
- 4.


怎样实现本地运⾏？写⼀个程序，不要带集群的配置⽂件（本质是你的mr程序的conf中是否有 mapreduce.framework.name=local以及yarn.resourcemanager.hostname参数） 本地模式⾮常便于进⾏业务逻辑的debug，只要在eclipse中打断点即可

如果在windows下想运⾏本地模式来测试程序逻辑，需要在windows中配置环境变量： ％HADOP_HOME％ = d:/hadop-2.6.1 %PATH% =％HADOP_HOME％\bin 并且要将d:/hadop-2.6.1的lib和bin⽬录替换成windows平台编译的版本

### 2.2.2集群运⾏模式

- 1.
- 2.
- 3.


将mapreduce程序提交给yarn集群resourcemanager，分发到很多的节点上并发执⾏ 处理的数据和输出结果应该位于hdfs⽂件系统 提交集群的实现步骤：

- A、将程序打成JAR包，然后在集群的任意⼀个节点上⽤hadop命令启动

$ hadop jar wordcount.jar cn.itcast.bigdata.mrsimple.WordCountDriver inputpath outputpath

- B、直接在linux的eclipse中运⾏main⽅法 （项⽬中要带参数：mapreduce.framework.name=yarn以及yarn的两个基本配置）

- C、如果要在windows的eclipse中提交job给集群，则要修改YarnRuner类


mapreduce程序在集群中运⾏时的⼤体流程：

![image 3](assets/imageFile3.png)

附：在windows平台上访问hadop时改变⾃⾝⾝份标识的⽅法之⼆：

![image 4](assets/imageFile4.png)

## 3.MAPREDUCE中的Combiner

[dht2]

- 1.
- 2.
- 3.


combiner是MR程序中Maper和Reducer之外的⼀种组件 combiner组件的⽗类就是Reducer combiner和reducer的区别在于运⾏的位置：

Combiner是在每⼀个maptask所在的节点运⾏ Reducer是接收全局所有Maper的输出结果；

- (4) combiner的意义就是对每⼀个maptask的输出进⾏局部汇总，以减⼩⽹络传输量 具体实现步骤：
- (5) combiner能够应⽤的前提是不能影响最终的业务逻辑 ⽽且，combiner的输出kv应该跟reducer的输⼊kv类型要对应起来


- 1.
- 2.


⾃定义⼀个combiner继承Reducer，重写reduce⽅法 在job中设置： job.setCombinerClas(CustomCombiner.clas)

# 3. MAPREDUCE原理篇（2）

- 3.1mapreduce的shufle机制


- 3.1.1 概述：


mapreduce中，map阶段处理的数据如何传递给reduce阶段，是mapreduce框架中最关键的⼀个流 程，这个流程就叫shufle；

shufle: 洗牌、发牌——（核⼼机制：数据分区，排序，缓存）；

具体来说：就是将maptask输出的处理结果数据，分发给reducetask，并在分发的过程中，对数据 按key进⾏了分区和排序；

## 3.1.2 主要流程：

Shufle缓存流程：

![image 5](assets/imageFile5.png)

shufle是MR处理流程中的⼀个过程，它的每⼀个处理步骤是分散在各个map task和reduce task节点 上完成的，整体来看，分为3个操作：

1. 2. 3.

分区partition Sort根据key排序 Combiner进⾏局部value的合并

## 3.1.3 详细流程

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


maptask收集我们的map()⽅法输出的kv对，放到内存缓冲区中 从内存缓冲区不断溢出本地磁盘⽂件，可能会溢出多个⽂件 多个溢出⽂件会被合并成⼤的溢出⽂件 在溢出过程中，及合并的过程中，都要调⽤partitoner进⾏分组和针对key进⾏排序 reducetask根据⾃⼰的分区号，去各个maptask机器上取相应的结果分区数据 reducetask会取到同⼀个分区的来⾃不同maptask的结果⽂件，reducetask会将这些⽂件再进⾏ 合并（归并排序） 合并成⼤⽂件后，shufle的过程也就结束了，后⾯进⼊reducetask的逻辑运算过程（从⽂件中取 出⼀个⼀个的键值对group，调⽤⽤户⾃定义的reduce()⽅法）

Shufle中的缓冲区⼤⼩会影响到mapreduce程序的执⾏效率，原则上说，缓冲区越⼤，磁盘io的次数 越少，执⾏速度就越快 缓冲区的⼤⼩可以通过参数调整, 参数：io.sort.mb 默认10M(如果数据超出缓冲区⼤⼩，会溢写⾄磁 盘临时⽂件)

- 3.1.4 详细流程示意图


![image 6](assets/imageFile6.png)

- 3.2.MAPREDUCE中的序列化


- 3.2.1 概述


Java的序列化是⼀个重量级序列化框架（Serializable），⼀个对象被序列化后，会附带很多额外的信 息（各种校验信息，header，继承体系。。。。），不便于在⽹络中⾼效传输； 所以，hadop⾃⼰开发了⼀套序列化机制（Writable），精简，⾼效

## 3.2.2Jdk序列化和MR序列化之间的比较

简单代码验证两种序列化机制的差别：

<table>
  <tr>
    <th>public clas TestSeri { public static void main(String[] args) throws Exception {<br><br>/定 义 两个 ByteArayOutputStream， ⽤ 来 接收 不 同 序 列化 机 制 的 序 列化 结 果 ByteArayOutputStream a = new ByteArayOutputStream(); ByteArayOutputStream ba2 = new ByteArayOutputStream();<br><br>/定 义 两个 DataOutputStream， ⽤ 于 将 普 通 对 象 进 ⾏ jdk标 准 序 列化 DataOutputStream ut = new DataOutputStream(ba); DataOutputStream dout2 = new DataOutputStream(ba2); ObjectOutputStream obout = new ObjectOutputStream(dout2);<br><br>/定 义 两个 bean， 作为 序 列化 的 源 对 象 ItemBeanSer itemBeanSer = new ItemBeanSer(1 0L, 89.9f); ItemBean itemBean = new ItemBean(1 0L, 89.9f);<br><br>/⽤ 于 ⽐较 String类 型 和 Text类 型 的 序 列化 差 别 Textatext = new Text("a");<br><br>/ atext.write(dout); itemBean.write(dout); byte[] byteAray = ba.toByteAray();<br><br>/⽐ 较 序列化 结 果 System.out.println(byteAray.length); for (byte b : byteAray) {<br><br>t . t. tb); System.out.print(":");<br><br>} System.out.println(" -"); String astr = "a";<br><br>/ dout2.writeUTF(astr); obout.writeObject(itemBeanSer); byte[] byteAray2 = ba2.toByteAray( System.out.println(byteAray2.length); for (byte b : byteAray2) {<br><br>t . t. tb);<br><br>System.out.print(":"); }<br><br>}</th>
  </tr>
</table>


}

## 3.2.3 自定义对象实现MR中的序列化接口

如果需要将⾃定义的bean放在key中传输，则还需要实现comparable接⼜，因为mapreduce框中的 shufle过程⼀定会对key进⾏排序,此时，⾃定义的bean实现的接⼜应该是： public clas FlowBean implements WritableComparable<FlowBean>

需要⾃⼰实现的⽅法是：

<table>
  <tr>
    <th>/*<br><br>* 反序列化的⽅法，反序列化时，从流中读取到的各个字段的顺序应该与序列化时写出去的顺 序保持⼀致<br>*/ @Overide public void readFields(DataInput in) throws IOException {<br><br><br>upflow= in.readLong(); dflow = in.readLong(); sumflow = in.readLong();<br><br>} /*<br><br>* 序列化的⽅法<br>*/ @Overide public void write(DataOutput out) throws IOException {<br><br><br>outwiteLong(upﬂow); out.writeLong(dﬂow);<br><br>/可以考虑不序列化总流量，因为总流量是可以通过上⾏流量和下⾏流量计算出来的 out.writeLong(sumﬂow);<br><br>} @Overide public int compareTo(FlowBean o) {<br><br>/实现按照sumﬂow的⼤⼩倒序排序 returnsumﬂow>o.getSumﬂow()?-1:1;</th>
  </tr>
</table>


}

- 3.3.MapReduce与YARN


- 3.3.1YARN概述


Yarn是⼀个资源调度平台，负责为运算程序提供服务器运算资源，相当于⼀个分布式的操作系统平 台，⽽mapreduce等运算程序则相当于运⾏于操作系统之上的应⽤程序

- 3.3.2YARN的重要概念


- 1.
- 2.
- 3.
- 4.


yarn并不清楚⽤户提交的程序的运⾏机制 yarn只提供运算资源的调度（⽤户程序向yarn申请资源，yarn就负责分配资源） yarn中的主管⾓⾊叫ResourceManager yarn中具体提供运算资源的⾓⾊叫NodeManager

- 4.1.2分析 基本思路：实现⾃定义的bean来封装流量信息，并将bean作为map输出的key来传输

这样⼀来，yarn其实就与运⾏的⽤户程序完全解耦，就意味着yarn上可以运⾏各种类型的分布式 运算程序（mapreduce只是其中的⼀种），⽐如mapreduce、storm程序，spark程序，tez …… 所以，spark、storm等运算框架都可以整合在yarn上运⾏，只要他们各⾃的框架中有符合yarn规 范的资源请求机制即可

Yarn就成为⼀个通⽤的资源调度平台，从此，企业中以前存在的各种运算集群都可以整合在⼀个 物理集群上，提⾼资源利⽤率，⽅便数据共享

![image 7](assets/imageFile7.png)

<table>
  <tr>
    <th>1363157985066 13726230503 00-FD-07-A4-72-B8 CMCC 120.196.100.82 24 27 2481 24681 200 1363157995052 13826544101 5C-0E-8B-C7-F1-E0 CMCC 120.197.40.4 4 0 264 0 200 1363157991076 13926435656 20-10-7A-28-CC-0A CMCC 120.196.100.99 2 4 132 1512 200 1363154400022 13926251106 5C-0E-8B-8B-B1-50 CMCC 120.197.40.4 4 0 240 0 200</th>
  </tr>
</table>


- 5.
- 6.
- 7.


- 3.3.3Yarn中运行运算程序的示例


mapreduce程序的调度过程，如下图

# 4. MAPREDUCE实践篇（2）

- 4.1.Mapreduce中的排序初步


- 4.1.1需求 对⽇志数据中的上下⾏流量信息汇总，并输出按照总流量倒序排序的结果 数据如下：


MR程序在处理数据的过程中会对数据排序(map输出的kv对传输到reduce之前，会排序)，排序的依据 是map输出的key 所以，我们如果要实现⾃⼰需要的排序规则，则可以考虑将排序因素放到key中，让key实现接⼜： WritableComparable 然后重写key的compareTo⽅法

### 4.1.3实现

1. ⾃定义的bean

public clas FlowBean implements WritableComparable<FlowBean>{

lo upflow; lon downflow; long sumflow;

/如果空参构造函数被覆盖，⼀定要显示定义⼀下，否则在反序列时会抛异常 public FlowBean(){} public FlowBean(long upflow, long downflow) {

super(); upflow = upflow; downflow = downflow;

this.sumflow = upflow + downflow;

} public long getSumflow() {

return sumflow;

} public void setSumflow(long sumflow) {

this.sumflow = sumflow;

} public long getUpflow() {

return upflow;

} public void setUpflow(long upflow) {

this.upflow = upflow;

} public long getDownflow() {

return downflow;

} public void setDownflow(long downflow) {

this.downflow = downflow; }

/序列化，将对象的字段信息写⼊输出流 @Overide public void write(DataOutput out) throws IOException {

outwt o upflow); otwt on downflow); out.writeLong(sumflow);

}

/反序列化，从输⼊流中读取各个字段信息 @Overide public void readFields(DataInput in) throws IOException {

upflow = in.readLong(); downflow = in.readLong(); sumflow = in.readLong();

}

@Overide public String toString() {

return upflow + "\t" + downflow + "\t" + sumflow;

} @Overide public int compareTo(FlowBean o) {

/⾃定义倒序⽐较规则

return sumflow > o.getSumflow() ? -1 1; }

}

1. maper 和 reducer

public clas FlowCount { static clas FlowCountMaper extends Maper<LongWritable, Text, FlowBean,Text > {

@Overide protected void map(LongWritable key, Text value, Context context) throws

IOException, InteruptedException {

String line = value.toString(); String[] fields = line.split("\t"); try {

String phonenbr = fields[0]; long upflow = Long.parseLong(fields[1]); long dflow = Long.parseLong(fields[2]); FlowBean flowBean = new FlowBean(upflow, dflow); context.write(flowBean,new Text(phonenbr);

} catch (Exception e) {

e.printStackTrace(); }

}

} static clas FlowCountReducer extends Reducer<FlowBean,Text,Text, FlowBean> {

@Overide protected void reduce(FlowBean bean, Iterable<Text> phonenbr, Context context)

throws IOException, InteruptedException { Text phoneNbr = phonenbr.iterator().next(); context.write(phoneNbr, bean);

}

} public static void main(String[] args) throws Exception {

Configuration conf = new Configuration(); Job job = Job.getInstance(conf); job.setJarByClas(FlowCount.clas); o.setMaperClas(FlowCountMaper.clas); job.setReducerClas(FlowCountReducer.clas);

o.set aputputKeyClas(FlowBean.clas); job.setMapOutputValueClas(Text.clas);

.set ututKeyClas(Text.clas); job.setOutputValueClas(FlowBean.clas);

/ job.setInputFormatClas(TextInputFormat.clas);

FeInputFormat.setInputPaths(job, new Path(args[0]); FileOutputFormat.setOutputPath(job, new Path(args[1]);

job.waitForCompletion(true); }

}

## 4.2.Mapreduce中的分区Partitioner

- 4.2.1需求 根据归属地输出流量统计数据结果到不同⽂件，以便于在查询统计结果时可以定位到省级范围进⾏


- 4.2.2分析 Mapreduce中会将map输出的kv对，按照相同key分组，然后分发给不同的reducetask 默认的分发规则为：根据key的hashcode%reducetask数来分发 所以：如果要按照我们⾃⼰的需求进⾏分组，则需要改写数据分发（分组）组件Partitioner ⾃定义⼀个CustomPartitioner继承抽象类：Partitioner 然后在job对象中，设置⾃定义partitioner： job.setPartitionerClas(CustomPartitioner.clas)


- 4.2.3实现


<table>
  <tr>
    <th>/*<br><br>* 定义⾃⼰的从map到reduce之间的数据（分组）分发规则 按照⼿机号所属的省份来分发（分组）ProvincePartitioner<br>* 默认的分组组件是HashPartitioner<br><br>*<br><br>* @author<br><br>*<br><br>*/ public class ProvincePartitioner extends Partitioner<Text, FlowBean> {<br><br><br>static HashMap<String, Integer> provinceMap = new HashMap<String, Integer>();<br><br>static {<br><br>provinceMap.put("135", 0);<br>provinceMap.put("136", 1);<br>provinceMap.put("137", 2);<br>provinceMap.put("138", 3);<br>provinceMap.put("139", 4);<br><br><br>}<br><br>publ@Oveicr irnidte getPartition(Textkey, FlowBean value, int numPartitions){ Integer code = provinceMap.get(key.toString().substring(0,3));<br><br>return code == null ? 5 : code; }</th>
  </tr>
</table>


}

## 4.3.mapreduce数据压缩

- 4.3.1概述 这是mapreduce的⼀种优化策略：通过压缩编码对maper或者reducer的输出进⾏压缩，以减少磁 盘IO，提⾼MR程序运⾏速度（但相应增加了cpu运算负担）


- 1.
- 2.
- 3.


Mapreduce⽀持将map输出的结果或者reduce输出的结果进⾏压缩，以减少⽹络IO或最终输出数 据的体积 压缩特性运⽤得当能提⾼性能，但运⽤不当也可能降低性能 基本原则：

运算密集型的job，少⽤压缩 IO密集型的job，多⽤压缩

### 4.3.2 MR⽀持的压缩编码

![image 8](assets/imageFile8.png)

- 4.3.3 Reducer输出压缩 在配置参数或在代码中都可以设置reduce的输出压缩


- 1、在配置参数中设置

mapreduce.output.fileoutputformat.compres=false

mapreduce.output.fileoutputformat.compres.codec=org.apache.hadop.io.compres.DefaultCodec

mapreduce.output.fileoutputformat.compres.type=RECORD

- 2、在代码中设置


<table>
  <tr>
    <th>Job job = Job.getInstance(conf); FileOutputFormat.setCompresOutput(job, true);</th>
  </tr>
</table>


FileOutputFormat.setOutputCompressorClas(job, (Clas<? extends CompresionCodec>) Clas.forName("");

- 4.3.4 Maper输出压缩 在配置参数或在代码中都可以设置reduce的输出压缩


- 1、在配置参数中设置 mapreduce.map.output.compres=false mapreduce.map.output.compres.codec=org.apache.hadop.io.compres.DefaultCodec
- 2、在代码中设置：


<table>
  <tr>
    <th>conf.setBolean(Job.MAP_OUTPUT_COMPRES, true);</th>
  </tr>
</table>


conf.setClas(Job.MAP_OUTPUT_COMPRES_CODEC, GzipCodec.clas, CompresionCodec.clas);

- 4.3.5压缩⽂件的读取 Hadop⾃带的InputFormat类内置⽀持压缩⽂件的读取，⽐如TextInputformat类，在其initialize⽅法 中：


<table>
  <tr>
    <th>public void initialize(InputSplit genericSplit,<br><br>TaskAtemptContext context) throws IOException { FileSplit split = (FileSplit) genericSplit; Configuration job = context.getConfiguration(); this.maxLineLength = job.getInt(MAX_LINE_LENGTH, Integer.MAX_VALUE); start = split.getStart(); end = start + split.getLength(); final Path file = split.getPath();<br><br>/ open the file and sek to the start of thesplit final FileSystem fs = file.getFileSystem(job); fileIn = fs.open(file);<br><br>/根据⽂件后缀名创建相应压缩编码的codec CompresionCodec codec = new CompresionCodecFactory(job).getCodec(file); if (nul!=codec) {<br><br>isCompresedInput = true; decompresor = CodecPol.getDecompresor(codec);<br><br>/判断是否属于可切⽚压缩编码类型<br><br>if (codec instanceof SplitableCompresionCodec) {<br><br>final SplitCompresionInputStream cIn =<br><br>(SplitableCompresionCodec)codec).createInputStream( fileIn, decompresor, start, end, SplitableCompresionCodec.READ_MODE.BYBLOCK);<br><br>/如果是可切⽚压缩编码，则创建⼀个CompresedSplitLineReader读取压缩数据 in = new CompresedSplitLineReader(cIn, job,<br><br>this.recordDelimiterBytes); start = cIn.getAdjustedStart(); end = cIn.getAdjustedEnd(); filePosition = cIn;<br><br>} else {<br><br>/如果是不可切⽚压缩编码，则创建⼀个SplitLineReader读取压缩数据，并将⽂件输⼊流转换成解压数据流传递给普通 SplitLineReader读取<br><br>in = new SplitLineReader(codec.createInputStream(fileIn,<br><br>decompresor), job, this.recordDelimiterBytes); filePosition = fileIn;<br><br>} } else { fileIn.sek(start);<br><br>/如果不是压缩⽂件，则创建普通SplitLineReader读取数据 in = new SplitLineReader(fileIn, job, this.recordDelimiterBytes); filePosition = fileIn;</th>
  </tr>
</table>


##### }

## 4.4. 更多MapReduce编程案例

- 4.4.1 reduce端join算法实现


- 1、需求： 订单数据表t_order：

商品信息表t_product

假如数据量巨⼤，两表的数据是以⽂件的形式存储在HDFS中，需要⽤mapreduce程序来实现⼀下SQL 查询运算：

- 2、实现机制： 通过将关联的条件作为map输出的key，将两表满⾜join条件的数据并携带数据所来源的⽂件信息，发 往同⼀个reduce task，在reduce中进⾏数据的串联


<table>
  <tr>
    <th>id</th>
    <th>date</th>
    <th>pid</th>
    <th>amount</th>
  </tr>
  <tr>
    <td>101</td>
    <td>20150710</td>
    <td>P 01</td>
    <td>2</td>
  </tr>
  <tr>
    <td>102</td>
    <td>20150710</td>
    <td>P 02</td>
    <td>3</td>
  </tr>
</table>


<table>
  <tr>
    <th>id</th>
    <th>name</th>
    <th>category_id</th>
    <th>price</th>
  </tr>
  <tr>
    <td>P 01</td>
    <td>⼩⽶5</td>
    <td>C01</td>
    <td>2</td>
  </tr>
  <tr>
    <td>P 02</td>
    <td>锤⼦T1</td>
    <td>C01</td>
    <td>3</td>
  </tr>
</table>


<table>
  <tr>
    <th>select a.id,a.date,b.name,b.category_id,b.price from t_order a join t_product b on a.pid = b.id</th>
  </tr>
</table>


<table>
  <tr>
    <th>public clas OrderJoin {<br><br>static clas OrderJoinMaper extends Maper<LongWritable, Text, Text, OrderJoinBean> {<br><br>@Override protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {<br><br>/ 拿到⼀⾏数据，并且要分辨出这⾏数据所属的⽂件 String line = value.toString();<br><br>String[] fields = line.split("\t");<br><br>/ 拿到itemid String itemid = fields[0];<br><br>/ 获取到这⼀⾏所在的⽂件名（通过inpusplit） String name = "你拿到的⽂件名";<br><br>/ 根据⽂件名，切分出各字段（如果是a，切分出两个字段，如果是b，切分出3个字段）<br><br>OrderJoinBean bean = new OrderJoinBean(); bean.set(nul, nul, nul, nul, nul); context.write(new Text(itemid), bean);<br><br>}<br><br>} static clas OrderJoinReducer extends Reducer<Text, OrderJoinBean, OrderJoinBean, NulWritable> {<br><br>@Override protected void reduce(Text key, Iterable<OrderJoinBean> beans, Context context) throws IOException,<br><br>InterruptedException {<br><br>/拿到的key是某⼀个itemid,⽐如1 0 /拿到的beans是来⾃于两类⽂件的bean / {1 0,amount} {1 0,amount} {1 0,amount} - {1 0,price,name}<br><br>/将来⾃于b⽂件的bean⾥⾯的字段，跟来⾃于a的所有bean进⾏字段拼接并输出 }<br><br>}</th>
  </tr>
</table>


}

缺点：这种⽅式中，join的操作是在reduce阶段完成，reduce端的处理压⼒太⼤，map节点的运算负载 则很低，资源利⽤率不⾼，且在reduce阶段极易产⽣数据倾斜

解决⽅案： map端join实现⽅式

### 4.4.2 map端join算法实现

- 1、原理阐述 适⽤于关联表中有⼩表的情形； 可以将⼩表分发到所有的map节点，这样，map节点就可以在本地对⾃⼰所读到的⼤表数据进⾏join并 输出最终结果，可以⼤⼤提⾼join操作的并发度，加快处理速度
- 2、实现⽰例


- -先在maper类中预先定义好⼩表，进⾏join
- -引⼊实际场景中的解决⽅案：⼀次加载数据库或者⽤distributedcache


public clas TestDistributedCache {

static clas TestDistribtedCacheMaper extends Maper<LongWritable, Text, Text, Text>{ FileReaer in = nul; BuferedReader reader = nul; HashMap<String,String> b_tab = new HashMap<String, String>();

tn localpath =nul; String uirpath = nul;

/是在map任务初始化的时候调⽤⼀次 @Overide protected void setup(Context context) throws IOException, InteruptedException {

/通过这⼏句代码可以获取到cache file的本地绝对路径，测试验证⽤ Path[] files = context.getLocalCacheFiles(); localpath = files[0].tString(); URI[] cacheFiles = context.getCacheFiles();

/缓存⽂件的⽤法⸺直接⽤本地IO来读取 /这⾥读的数据是map task所在机器本地⼯作⽬录中的⼀个⼩⽂件

in = new FileReader("b.txt"); reader =new BuferedReader(in); String line =nul; while(nul!=(line=reader.readLine( ){

String[] fields = line.split("," b_tab.put(fields[0],fields[1]);

}

tlsl setrea reader); IOUtils.closeStream(in);

} @Overide protected void map(LongWritable key, Text value, Context context) throws

IOException, InteruptedException {

/这⾥读的是这个map task所负责的那⼀个切⽚数据（在hdfs上） String[] fields = value.toString().split("\t");

ti itemid = fields[0];

- String a_amount = fields[1];
- String b_name = b_tab.get(a_itemid);


/ 输出结果 101 98.9 banan

context.write(new Text(a_itemid), new Text(a_amount + "\t" + ":" + localpath + "\t" +b_name);

}

}

public static void main(String[] args) throws Exception {

Configuration conf = new Configuration(); Job job = Job.getInstance(conf);

job.setJarByClas(TestDistributedCache.clas); job.setMaperClas(TestDistributedCacheMaper.clas);

.set ututKeyClas(Text.clas); job.setOutputValueClas(LongWritable.clas);

/这⾥是我们正常的需要处理的数据所在路径 FeInputFormat.setInputPaths(job, new Path(args[0]); FileOutputFormat.setOutputPath(job, new Path(args[1]);

/不需要reducer job.setNumReduceTasks(0); /分发⼀个⽂件到task进程的⼯作⽬录 job.adCacheFile(new URI("hdfs:/hadop-server01 9 0/cachefile/b.txt");

/分发⼀个归档⽂件到task进程的⼯作⽬录 / job.adArchiveToClasPath(archive);

/分发jar包到task节点的claspath下 / job.adFileToClasPath(jarfile);

job.waitForCompletion(true); }

}

### 4.4.3 web⽇志预处理

- 1、需求： 对web访问⽇志中的各字段识别切分 去除⽇志中不合法的记录 根据KPI统计需求，⽣成各类访问请求过滤数据
- 2、实现代码：


- a) 定义⼀个bean，⽤来记录⽇志数据中的各数据字段


public clas WebLogBean {

private String remote_adr;/ 记录客户端的ip地址 private String remote_user;/ 记录客户端⽤户名称,忽略属性"-" private String time_local;/ 记录访问时间与时区 private String request;/ 记录请求的url与htp协议 private String status;/ 记录请求状态；成功是20 private String body_bytes_sent;/ 记录发送给客户端⽂件主体内容⼤⼩ private String htp_referer;/⽤来记录从那个⻚⾯链接访问过来的 private String htp_user_agent;/ 记录客户浏览器的相关信息

private bolean valid= true;// 判断数据是否合法

public String getRemote_addr() { returnremote_addr;

} public void setRemote_adr(String remote_adr) {

this.remote_adr = remote_adr;

} public String getRemote_user() {

returnremote_user;

} public void setRemote_user(String remote_user) {

this.remote_user = remote_user;

} public String getTime_local() {

returntime_local;

} public void setTime_local(String time_local) {

this.time_local= time_local;

} public String getRequest() {

returnrequest;

} public void setRequest(String request) {

this.request = request;

} public String getStatus(){ returnstatus;

} public void setStatus(String status) {

this.status = status;

} public String getBody_bytes_sent() { returnbody_bytes_sent;

} public void setBody_bytes_sent(String body_bytes_sent) {

this.body_bytes_sent = body_bytes_sent;

} public String getHtp_referer() { returnhtp_referer;

} public void setHttp_referer(String htp_referer) {

this.http_referer = htp_referer;

} public String getHtp_user_agent() { returnhtp_user_agent;

} public void setHttp_user_agent(String htp_user_agent) {

this.http_user_agent = htp_user_agent;

} public bolean isValid() {

returnvalid;

} public void setValid(bolean valid) {

this.valid = valid; }

@Override public String toString() {

StringBuilder sb = new StringBuilder(); sb.apend(this.valid); sb.apend("\ 01").apend(t is.remote_adr); sb.apend("\ 01").apend(t is.remote_user); sb.apend("\ 01").apend(t is.time_local); sb.apend("\ 01").apend(t is.request); sb.apend("\ 01").apend(t is.status); sb.apend("\ 01").apend(t is.body_bytes_sent); sb.apend("\ 01").apend(this.htp_referer); sb.apend("\ 01").apend(this.htp_user_agent); return sb.toString();

} }

- b)定义⼀个parser⽤来解析过滤web访问⽇志原始记录


<table>
  <tr>
    <th>public clas WebLogParser {<br><br>public static WebLogBean parser(String line) { WebLogBean webLogBean = new WebLogBean(); String[] arr = line.split(" "); if (arr.length >1) {<br><br>webLogBean.setRe ote_adr(arr[0]); webLogBean.setRemote_user(arr[1]); webLogBean.setTime_local(arr[3].substring(1); webLogBean.setRequest(arr[6]); webLogBean.setStatus(arr[8]); webLogBean.setBody_bytes_sent(arr[9]); webLogBean.setHttp_referer(arr[10]);<br><br>if (arr.length > 12) {<br><br>webLogBean.setHtp_user_agent(arr[1] + " " + arr[12]); } else {<br><br>webLogBean.setHtp_user_agent(arr[1]);<br><br>} if (Integer.parseInt(webLogBean.getStatus() >= 40) {/ ⼤于40，HTP错误<br><br>webLogBean.setValid(false); }<br><br>} else {<br><br>webLogBean.setValid(false);<br><br>} return webLogBean;<br><br>} public static String parserTime(String time) { time.replace("/", "-"); return time; }</th>
  </tr>
</table>


}

- c) mapreduce程序


<table>
  <tr>
    <th>public clas WeblogPreProces {<br><br>static clas WeblogPreProcesMaper extends Maper<LongWritable, Text, Text, NulWritable> { Text k = newText(); NulWritablev= NulWritable.get();<br><br>@Override protected voidmap(LongWritable key, Text value, Context context) throws IOException, InterruptedException {<br><br>String line = value.toString(); WebLogBean webLogBean = WebLogParser.parser(line); if (!webLogBean.isValid()<br><br>return; k.set(webLogBean.toString(); context.write(k, v);<br><br>}<br><br>} public static void main(String[] args) throws Exception {<br><br>Configurationconf = new Configuration(); Job job = Job.getInstance(conf);<br><br>job.setJarByClas(WeblogPreProces.clas); job.setMaperClas(WeblogPreProcesMaper.clas); job.setOutputKeyClas(Text.clas); job.setOutputValueClas(NulWritable.clas); FileInputFormat.setInputPaths(job, new Path(args[0]); FileOutputFormat.setOutputPath(job, new Path(args[1]); job.waitForCompletion(true);<br><br>}</th>
  </tr>
</table>


}

JVM重⽤技术不是指同⼀Job的两个或两个以上的task可以同时运⾏于同⼀JVM上，⽽是排队按顺序执⾏。

Combiner的使⽤要⾮常谨慎 因为combiner在mapreduce过程中可能调⽤也肯能不调⽤，可能调⼀次也可能调多次 所以：combiner使⽤的原则是：有或没有都不能影响业务逻辑
