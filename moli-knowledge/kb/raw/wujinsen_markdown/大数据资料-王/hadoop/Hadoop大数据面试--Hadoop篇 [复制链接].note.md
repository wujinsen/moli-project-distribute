# 捐助：hadop⼤全（增加yarn、flume|storm、hadop⼀套视频））

本帖最后由 netman 于 2015-4-6 19 06 编辑

本篇⼤部分内容参考⽹上，其中性能部分参考：

htp:/blog.cloudera.com/blog/20. reduce-performance/

、

htp:/ w.idryman.org/blog/2014. ing-best-practices/

原理篇：

- 1. Hadop2.X的各个模块⼀句话简介

- 1）Hadop Comon：为Hadop其他模块提供⽀持的公共⼯具包；
- 2）HDFS：Hadop分布式⽂件系统；
- 3）YARN：任务调度和集群资源管理框架；
- 4）MapReduce：⽤于处理⼤数据集的框架，可扩展和并⾏；


- 2. HDFS数据上传原理


- 1） Client端发送⼀个添加⽂件到HDFS的请求给NameNode；
- 2） NameNode告诉Client端如何来分发数据块以及分发到哪⾥；
- 3） Client端把数据分为块（block）然后把这些块分发到DataNode中；
- 4） DataNode在NameNode的指导下复制这些块，保持冗余； 可以在讲解的时候，拿只笔和纸画下：


![image 1](<Hadoop大数据面试--Hadoop篇 [复制链接].note_images/imageFile1.png>)

Tips:

- a. NameNode之存储⽂件的元数据，⽽不存储具体的数据；
- b. HDFS Federation： 解决HA单点故障问题，⽀持NameNode⽔平扩展，每个NameNode对应⼀个 NameSpace；


- 3. MapReduce概述

- 1）map和reduce任务在NodeManager节点上各⾃有⾃⼰的JVM；
- 2）所有的Maper完成后，实时的key/value对会经过⼀个 和sort的阶段，在这个阶段中所有共同的key会被合并，发送到相同的Reducer中；
- 3）Maper的个数根据输⼊的格式确定，Reducer的个数根据 作业的配置决定；
- 4）Partitioner分区器决定key/value对应该被送往哪个Reducer中；
- 5）Combiner合并器可以合并Maper的输出，这样可以提⾼性能；


- 4. map-》shufle、sort-》reduce map阶段：


shufle

job

- 1） InputFormat确定输⼊数据应该被分为多少个分⽚，并且为每个分⽚创建⼀个InputSplit实例；
- 2） 针对每个InputSplit实例MR框架使⽤⼀个map任务来进⾏处理；在InputSplit中的每个KV键值对 被传送到Maper的map函数进⾏处理；
- 3） map函数产⽣新的序列化后的KV键值对到⼀个没有排序的内存

中；

- 4） 当缓冲区装满或者map任务完成后，在该缓冲区的KV键值对就会被排序同时流⼊到磁盘中，形 成spil⽂件，溢出⽂件；
- 5） 当有不⽌⼀个溢出⽂件产⽣后，这些⽂件会全部被排序，并且合并到⼀个⽂件中；
- 6） ⽂件中排序后的KV键值对等待被Reducer取⾛； 同样的，可以简单画个图：


缓冲区

![image 2](<Hadoop大数据面试--Hadoop篇 [复制链接].note_images/imageFile2.png>)

reduce阶段：

主要包括三个⼩阶段：

- 1） shufle：或者称为fetch阶段（获取阶段），在这个阶段所有拥有相同键的记录都被合并⽽且发 送到同⼀个Reducer中；
- 2） sort： 和

同时发⽣，在记录被合并和发送的过程中，记录会按照key进⾏排序；

- 3） reduce：针对每个键会进⾏reduce


shufle

函数

调⽤；

reduce数据流：

- 1） 当Maper完成map任务后，Reducer开始获取记录，同时对他们进⾏排序并存⼊⾃⼰的JVM内存 中的缓冲区；
- 2） 当⼀个缓冲区数据装满，则会流⼊到

；

- 3） 当所有的Maper完成并且Reducer获取到所有和他相关的输⼊后，该Reducer的所有记录会被合 并和排序，包括还在

中的；

- 4） 合并、排序完成后调⽤reduce⽅法；输出到HDFS或者根据作业配置到其他地⽅；


磁盘

缓冲区

![image 3](<Hadoop大数据面试--Hadoop篇 [复制链接].note_images/imageFile3.png>)

图⽚来⾃《Hadop权威指南》3rd Edition

- 5. YARN相关 YARN包括的组件有：ResourceManager、NodeManager、AplicationMaster，其中 ResourceManager可以分为：Scheduler、AplicationsManager


Hadop1.X中的JobTracker被分为两部分：ResourceManager和AplicationMaster，前者提供集群 资源给应⽤，后者为应⽤提供运⾏时环境；

YARN应⽤⽣命周期：

- 1） 客户端提交⼀个应⽤请求到ResourceManager；
- 2） ResourceManager中的AplicationsManager在集群中寻找⼀个可⽤的、负载较⼩的 NodeManager；
- 3） 被找到的NodeManager创建⼀个AplicationMaster实例；
- 4） AplicationMaster向ResourceManager发送⼀个资源请求，ResourceManager回复⼀个 Container的列表，包括这些Container是在哪些NodeManager上启动的信息；
- 5） AplicationMaster在ResourceManager的指导下在每个NodeManager上启动⼀个Container， Container在AplicationMaster的控制下执⾏⼀个任务； 简单画图：


![image 4](<Hadoop大数据面试--Hadoop篇 [复制链接].note_images/imageFile4.png>)

Tips：

- a. 客户端可以从AplicationMaster中获取任务信息；
- b. ⼀个作业⼀个AplicationMaster，⼀个Aplication可以有多个Container，⼀个NodeManager也 可以有多个Container；


性能篇： 性能涉及较多内容，这⾥参考前⽂中给出的链接，并按照作业运⾏、map阶段、reduce阶段的顺序 来组织性能相关的点。

- 1. 命令⾏参数：

在⾃定义集群的参数时，不修改集群的⽂件，⽽在命令⾏使⽤参数，这样可以针对不同的参数设置⽅ 便，从⽽不必修改集群中的配置⽂件，⼀般有下⾯两种⽅式：

- 1）hadop jar ExampleJob-0.0.1.jar ExampleJob -conf my-conf.xml arg0 arg1

使⽤配置⽂件的⽅式，把需要修改的地⽅设置在配置⽂件⾥⾯，使⽤-conf指定配置⽂件（上⾯命令 ⾏来⾃：

）；

- 2）hadop jar ExampleJob-0.0.1.jar ExampleJob -Dmapred.reduce.tasks=20 arg0


使⽤-D参数来这是相应的值也是可以的（上⾯的命令⾏来⾃：

）；

- 2. map阶段


htp:/ w.idryman.org/blog/2014. ing-best-practices/

htp:/ w.idryman.org/blog/2014. ing-best-practices/

- 1） map的个数问题


map的个数是不能直接设置的，如果有很多maper的执⾏时间⼩于1分钟，那么建议设置 mapred.min.split.size的⼤⼩，提⾼分⽚的⼤⼩，这样来减⼩Maper的个数，可以减⼩Maper初始 化的时间；或者设置JVM重⽤（图⽚来⾃：

htp:/ w.idryman.org/blog/2014. ing-best-practices/

）

![image 5](<Hadoop大数据面试--Hadoop篇 [复制链接].note_images/imageFile5.png>)

- 2） 设置mapred.child.java.opts参数

使⽤Ganglia、Nagios等监控⼯具检测slave节点的内存使⽤情况，设置合适的mapred.child.java.opts 参数，避免交换的发⽣；

- 3）map的输出使⽤压缩 当map的输出较多时，可以考虑使⽤压缩，这能提⾼很⼤的性能（图⽚来⾃：

）：

- 4）使⽤合适的Writable作为key（键）和value（值）类型

这⼀点在maper和reducer的编程中都可以使⽤，如果全部数据都使⽤Text的话，那么数据的占有空 间将会很⼤，导致效率低下。如果有必要可以⾃定义Writable类型。

- 5）重⽤已有变量


htp:/ w.idryman.org/blog/2014. ing-best-practices/

![image 6](<Hadoop大数据面试--Hadoop篇 [复制链接].note_images/imageFile6.png>)

在maper或者reducer的编程中重⽤已经定义的变量，可以避免重复的⽣成新对象，⽽导致垃圾回收 频繁的调⽤，如下代码1和2（代码参考：

htp:/blog.cloudera.com/blog/20. reduce-performance/

）；

- 1.
- 2.


public void map(.) {

.

- 3.
- 4.
- 5.
- 6.


for (String word : words) {

output.colect(new Text(word), new IntWritable(1); }

}

复制代码

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


clas MyMaper. { Text wordText = new Text(); IntWritable one = new IntWritable(1); public void map(.) {

.

for (String word : words) { wordText.set(word); output.colect(word, one);

} }

}

复制代码

- 6） 设置mapreduce.reduce.

.paralelcopies参数

设置此参数，可以使 Reducer在⼀个Maper完成后就开始获取数据，并⾏化数据获取；

- 7） 最⼩化maper输出：


shufle

- a. 在Maper端过滤，⽽不是在Reducer端过滤；
- b. 使⽤更⼩的数据来存储map输出的key和value（参考第4）点）；
- c. 设置Maper的输出进⾏压缩（参考第3）点）；


- 3. reduce阶段Reducer负载均衡：


- 1） Reducer的个数，根据实际集群的数量来设置Reducer的个数，使其负载均衡。⽐如集群有10个 节点，那么Reducer的个数设置为101个则应该是不合理的，在第⼀次任务分配时分配了10个作业， 这10个作业是并⾏的，但是最后⼀个作业并不是并⾏的。
- 2）Reducer中部分因为相同key的数据量⼤，导致个别Reducer运⾏耗时相⽐其他Reducer耗时⻓很 多。


可以考虑：

- a. 实现⼀个更好的hash函数继承⾃Partitioner类；
- b. 如果知道有⼤量相同的key的数据，可以写⼀个预处理的作业把相同的key分到不同的输出中， 然后再使⽤⼀个MR作业来处理这个特殊的key的数据；


- 4. 设置输⼊输出如果有多个连续的MR作业，可以设置输⼊输出为序列⽂件，这样可以达到更好的性 能。


个⼈整理，如有错误，敬请指教。

分享，成⻓，快乐

脚踏实地，专注

转载请注明blog地址： htp:/blog.csdn.net/fansy190

