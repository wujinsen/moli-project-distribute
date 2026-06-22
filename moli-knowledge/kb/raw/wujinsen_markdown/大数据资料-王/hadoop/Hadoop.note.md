⼀个 基础架构，由Apache基⾦会所开发。 ⽤户可以在不了解分布式底层细节的情况下，开发分布式程序。充分利⽤集群的威⼒进⾏⾼速运算和 存储。

分布式系统

- [1]Hadop实现了⼀个 （Hadop Distributed File System），简称HDFS。HDFS有⾼ 的特点，并且设计⽤来部署在低廉的（low-cost）硬件上；⽽且它提供⾼吞吐量（high

throughput）来访问 的数据，适合那些有着超⼤数据集（large data set）的应⽤程序。HDFS 放宽了（relax）POSIX的要求，可以以流的形式访问（streaming aces）⽂件系统中的数据。 Hadop的框架最核⼼的设计就是：HDFS和MapReduce.HDFS为海量的数据提供了存储，则 MapReduce为海量的数据提供了计算。

- [2]中⽂名 海杜普外⽂名


分布式⽂件系统 容错性

应⽤程序

Hadop

### ⽬录

1起源

▪ ▪ ▪

项⽬起源 发展历程 名字起源

2诸多优点 3核心架构

▪ ▪ ▪ ▪ ▪

HDFS NameNode DataNode ⽂件操作 Linux 集群

4集群系统 5应用程序 6MapReduce与Hadoop之比较 7开源实现 8子项目 9研究 10大事记

1认证

▪ ▪ 12信息安全 13Hadoop之父

Cloudera Hortonworks

14图册

# 1起源编辑

Luc ene

项⽬起源Hadop由 Apache Software Foundation 公司于 205 年秋天作为 的⼦

hadop logo项⽬ 的⼀部分正式引⼊。它受到最先由 Gogle Lab 开发的 Map/Reduce 和 Gogle File System( ) 的启发。

Nutch GFS

- 206 年 3 ⽉份，Map/Reduce 和 Nutch Distributed File System (NDFS) 分别被纳⼊称为 Hadop 的 项⽬中。 Hadop 是最受欢迎的在 Internet 上对搜索 进⾏内容分类的⼯具，但它也可以解决许多要求极 ⼤伸缩性的问题。例如，如果您要 grep ⼀个 10TB 的巨型⽂件，会出现什么情况？在传统的系统上， 这将需要很⻓的时间。但是 Hadop 在设计时就考虑到这些问题，采⽤ 机制，因此能⼤⼤提 ⾼效率。


关键字

并⾏执⾏

发展历程

Hadop原本来⾃于⾕歌⼀款名为MapReduce的编程模型包。⾕歌的MapReduce框架可以把⼀个应⽤ 程序分解为许多并⾏计算指令，跨⼤量的计算节点运⾏⾮常巨⼤的数据集。使⽤该框架的⼀个典型例 ⼦就是在⽹络数据上运⾏的搜索算法。Hadop

- [3]最初只与⽹⻚索引有关，迅速发展成为分析⼤数据的领先平台。Cloudera是⼀家企业软件公司，该 公司在208年开始提供基于Hadop的软件和服务。 GoGrid是⼀家云计算基础设施公司，在2012年，该公司与Cloudera合作加速了企业采纳基于Hadop 应⽤的步伐。Dataguise公司是⼀家数据安全公司，同样在2012年该公司推出了⼀款针对Hadop的数 据保护和⻛险评估。


名字起源

Hadop这个名字不是⼀个缩写，⽽是⼀个虚构的名字。该项⽬的创建者，Doug Cuting解释Hadop 的得名 ：“这个名字是我孩⼦给⼀个棕⻩⾊的⼤象玩具命名的。我的命名标准就是简短，容易发⾳和拼 写，没有太多的意义，并且不会被⽤于别处。⼩孩⼦恰恰是这⽅⾯的⾼⼿。” Hadop的发⾳是 [hædu:p]。

# 2诸多优点编辑

Hadop是⼀个能够对⼤量数据进⾏ 的 框架。但是 Hadop 是以⼀种可靠、⾼效、可伸 缩的⽅式进⾏处理的。 Hadop 是可靠的，因为它假设计算元素和存储会失败，因此它维护多个⼯作数据副本，确保能够针对 失败的节点重新 。

分布式处理 软件

分布处理

Hadop 是⾼效的，因为它以并⾏的⽅式⼯作，通过 加快处理速度。 Hadop 还是可伸缩的，能够处理 级数据。 此外，Hadop 依赖于社区服务，因此它的成本⽐较低，任何⼈都可以使⽤。 Hadop是⼀个能够让⽤户轻松架构和使⽤的 平台。⽤户可以轻松地在Hadop上开发和运 ⾏处理海量数据的 。它主要有以下⼏个优点：

并⾏处理 PB

分布式计算

应⽤程序 ⾼可靠性。Hadop按位存储和处理数据的能⼒值得⼈们信赖。 ⾼扩展性。Hadop是在可⽤的计算机集簇间分配数据并完成计算任务的，这些集簇可以⽅便地扩 展到数以千计的节点中。 ⾼效性。Hadop能够在节点之间动态地移动数据，并保证各个节点的 ，因此处理速度⾮ 常快。

- 1.
- 2.
- 3.
- 4.
- 5.


动态平衡

⾼容错性。Hadop能够⾃动保存数据的多个副本，并且能够⾃动将失败的任务重新分配。 低成本。与⼀体机、商⽤数据仓库以及QlikView、Yonghong Z-Suite等数据集市相⽐，hadop是 开源的，项⽬的软件成本因此会⼤⼤降低。

Hadop带有⽤ 语⾔编写的框架，因此运⾏在 Linux ⽣产平台上是⾮常理想的。Hadop 上的

Java 应⽤ 程序 C+

也可以使⽤其他语⾔编写，⽐如 。 hadop⼤数据处理的意义Hadop得以在⼤数据处理应⽤中⼴泛应⽤得益于其⾃身在数据提取、变形 和加载(ETL)⽅⾯上的天然优势。Hadop的分布式架构，将⼤数据处理引擎尽可能的靠近存储，对例 如像ETL这样的批处理操作相对合适，因为类似这样操作的批处理结果可以直接⾛向存储。Hadop的 MapReduce功能实现了将单个任务打碎，并将碎⽚任务发送(Map)到多个节点上，之后再以单个数据 集的形式加载(Reduce)到数据仓库⾥。 [1]

# 3核⼼架构编辑

Hadop 集群的简化视图Hadop 由许多元素构成。其最底部是 Hadop Distributed File System （HDFS），它存储 Hadop 集群中所有存储节点上的⽂件。HDFS（对于本⽂）的上⼀层是

MapRedu ce

引擎，该引擎由 JobTrackers 和 TaskTrackers 组成。通过对Hadop分布式计算平台最核⼼的分布 式⽂件系统HDFS、MapReduce处理过程，以及数据仓库⼯具Hive和分布式数据库Hbase的介绍，基 本涵盖了Hadop分布式平台的所有技术核⼼。

- [4]


## HDFS

HDFS 重命名

对外部客户机⽽⾔， 就像⼀个传统的分级⽂件系统。可以创建、删除、移动或 ⽂件，等 等。但是 HDFS 的架构是基于⼀组特定的节点构建的（参⻅图 1），这是由它⾃身的特点决定的。这些 节点包括 NameNode（仅⼀个），它在 HDFS 内部提供元数据服务；DataNode，它为 HDFS 提供存 储块。由于仅存在⼀个 NameNode，因此这是 HDFS 的⼀个缺点（单点失败）。

存储在 HDFS 中的⽂件被分成块，然后将这些块复制到多个计算机中（DataNode）。这与传统的 RAID 架构⼤不相同。块的⼤⼩（通常为 64MB）和复制的块数量在创建⽂件时由客户机决定。 NameNode 可以控制所有⽂件操作。HDFS 内部的所有通信都基于标准的 协议。

TCP/IP

## NameNode

NameNode 是⼀个通常在 实例中的单独机器上运⾏的 。它负责管理⽂件系统 和控 制外部客户机的访问。NameNode 决定是否将⽂件映射到 DataNode 上的复制块上。对于最常⻅的 3 个复制块，第⼀个复制块存储在同⼀机架的不同节点上，最后⼀个复制块存储在不同机架的某个节点 上。注意，这⾥需要您了解集群架构。 实际的 I/O 并没有经过 NameNode，只有表示 DataNode 和块的⽂件映射的元数据经过 NameNode。当外部客户机发送请求要求创建⽂件时，NameNode 会以块标识和该块的第⼀个副本的 DataNode IP 地址作为响应。这个 NameNode 还会通知其他将要接收该块的副本的 DataNode。 NameNode 在⼀个称为 FsImage 的⽂件中存储所有关于⽂件系统 的信息。这个⽂件和⼀个包 含所有事务的 （这⾥是 EditLog）将存储在 NameNode 的本地⽂件系统上。FsImage 和 EditLog ⽂件也需要复制副本，以防⽂件损坏或 NameNode 系统丢失。 NameNode本身不可避免地具有SPOF（Single Point Of Failure）单点失效的⻛险，主备模式并不能解 决这个问题，通过Hadop Non-stop namenode才能实现10% uptime可⽤时间。

HDFS 软件 名称空间

事务

名称空间 记录⽂件

## DataNode

DataNode 也是⼀个通常在 实例中的单独机器上运⾏的软件。Hadop 集群包含⼀个 NameNode 和⼤量 DataNode。DataNode 通常以机架的形式组织，机架通过⼀个 将所有系统 连接起来。Hadop 的⼀个假设是：机架内部 之间的传输速度快于机架间节点的传输速度。 DataNode 响应来⾃ HDFS 客户机的读写请求。它们还响应来⾃ NameNode 的创建、删除和复制块的 命令。NameNode 依赖来⾃每个 DataNode 的定期⼼跳（heartbeat）消息。每条消息都包含⼀个块报 告，NameNode 可以根据这个报告验证块映射和其他⽂件系统元数据。如果 DataNode 不能发送⼼跳 消息，NameNode 将采取修复措施，重新复制在该节点上丢失的块。

HDFS

交换机 节点

## ⽂件操作

可⻅，HDFS 并不是⼀个万能的⽂件系统。它的主要⽬的是⽀持以流的形式访问写⼊的⼤型⽂件。 如果客户机想将⽂件写到 HDFS 上，⾸先需要将该⽂件缓存到本地的临时存储。如果缓存的数据⼤于 所需的 HDFS 块⼤⼩，创建⽂件的请求将发送给 NameNode。NameNode 将以 DataNode 标识和⽬ 标块响应客户机。 同时也通知将要保存⽂件块副本的 DataNode。当客户机开始将 发送给第⼀个 DataNode 时，将⽴即通过管道⽅式将块内容转发给副本 DataNode。客户机也负责创建保存在相同 HDFS

临时⽂件

名称空 间

中的校验和（checksum）⽂件。

在最后的⽂件块发送之后，NameNode 将⽂件创建提交到它的持久化元 （在 EditLog 和 FsImage ⽂件）。

数据存储

Linux 集群

Hadop 框架可在单⼀的 Linux 平台上使⽤（开发和调试时），官⽅提供MiniCluster作为单元测试使 ⽤，不过使⽤存放在机架上的商业服务器才能发挥它的⼒量。这些机架组成⼀个 Hadop 。它通 过集群拓扑知识决定如何在整个集群中分配作业和⽂件。Hadop 假定节点可能失败，因此采⽤本机⽅ 法处理单个计算机甚⾄所有机架的失败。

集群

# 4集群系统编辑

Gogle的 使⽤廉价的Linux PC机组成集群，在上⾯运⾏各种应⽤。即使是 的新⼿ 也可以迅速使⽤Gogle的基础设施。核⼼组件是3个：

数据中⼼ 分布式开发

⒈GFS（Gogle File System）。⼀个 ，隐藏下层 ， 复制等细节，对上层 程序提供⼀个统⼀的⽂件系统 。Gogle根据⾃⼰的需求对它进⾏了特别优化，包括：超⼤⽂件 的访问，读操作⽐例远超过写操作，PC机极易发⽣故障造成节点失效等。GFS把⽂件分成64MB的 块，分布在 的机器上，使⽤Linux的⽂件系统存放。同时每块⽂件⾄少有3份以上的 。中⼼是 ⼀个Master节点，根据⽂件索引，找寻⽂件块。详⻅Gogle的⼯程师发布的GFS论⽂。

分布式⽂件系统 负载均衡 冗余 API接⼝

集群 冗余

⒉MapReduce。Gogle发现⼤多数分布式运算可以抽象为MapReduce操作。Map是把输⼊Input分解 成中间的Key/Value对，Reduce把Key/Value合成最终输出Output。这两个函数由 提供给系统， 下层设施把Map和Reduce操作分布在 上运⾏，并把结果存储在GFS上。

程序员 集群

⒊BigTable。⼀个⼤型的 ，这个数据库不是关系式的数据库。像它的名字⼀样，就是⼀ 个巨⼤的 ，⽤来存储结构化的数据。 以上三个设施 均有论⽂发表。

分布式数据库 表格

Gogle 《The Gogle File System 》 203年[5] 《MapReduce: Simplified Data Procesing on Large Clusters》 204年[6] 《Bigtable: A Distributed Storage System for Structured Data》 206年[7]

- 1.
- 2.
- 3.


# 5应⽤程序编辑

Hadop 的最常⻅⽤法之⼀是 Web 搜索。虽然它不是惟⼀的 框架 ，但作为⼀个并⾏

软件 应⽤程序 数据 处理 Gogle 爬⾏器

引擎，它的表现⾮常突出。Hadop 最有趣的⽅⾯之⼀是 Map and Reduce 流程，它受到 开发的启发。这个流程称为创建索引，它将 Web 检索到的⽂本 Web ⻚⾯作为输⼊，并且将这 些⻚⾯上的单词的频率报告作为结果。然后可以在整个 Web 搜索过程中使⽤这个结果从已定义的搜索 参数中识别内容。

MapReduce

最简单的 MapReduce ⾄少包含 3 个部分：⼀个 Map 、⼀个 Reduce 函数和⼀个 main 函数。main 函数将 和⽂件输⼊/输出结合起来。在这点上，Hadop 提供了⼤量的接⼝和

应⽤程序 函数

作业控制 抽象 类 应⽤程序开发

，从⽽为 Hadop ⼈员提供许多⼯具，可⽤于调试和性能度量等。 MapReduce 本身就是⽤于 ⼤数据集的 框架。MapReduce 的根源是函数性编程中的 map 和 reduce 函数。它由两个可能包含有许多实例（许多 Map 和 Reduce）的操作组成。Map 函数接受 ⼀组数据并将其转换为⼀个键/值对列表，输⼊域中的每个元素对应⼀个键/值对。Reduce 函数接受 Map 函数⽣成的列表，然后根据它们的键（为每个键⽣成⼀个键/值对）缩⼩键/值对列表。 这⾥提供⼀个示例，帮助您理解它。假设输⼊域是 one smal step for man,one giant leap for mankind。在这个域上运⾏ Map 函数将得出以下的键/值对列表：（one,1） (smal,1） (step,1） (for,1） (man,1） MapReduce 流程的概念流(one,1） (giant,1） (leap,1） (for,1） (mankind,1） 如果对这个键/值对列表应⽤ Reduce 函数，将得到以下⼀组键/值对： （one,2） (smal,1） (step,1） (for,2） (man,1）（giant,1） (leap,1） (mankind,1）结果是对输⼊域中 的单词进⾏计数，这⽆疑对处理索引⼗分有⽤。但是，假 显示处理和存储的物理分布的 Hadop 集群设有两个输⼊域，第⼀个是 one smal step for man，第⼆ 个是 one giant leap for mankind。您可以在每个域上执⾏ Map 函数和 Reduce 函数，然后将这两个 键/值对列表应⽤到另⼀个 Reduce 函数，这时得到与前⾯⼀样的结果。换句话说，可以在输⼊域并⾏ 使⽤相同的操作，得到的结果是⼀样的，但速度更快。这便是 MapReduce 的威⼒；它的并⾏功能可 在任意数量的系统上使⽤。图 2 以区段和迭代的形式演示这种思想。 回到 Hadop 上，它是如何实现这个功能的？⼀个代表客户机在单个主系统上启动的 MapReduce

并⾏处理 软件

应⽤ 程序 应⽤程序

称为 JobTracker。类似于 NameNode，它是 Hadop 集群中惟⼀负责控制 MapReduce 的系统。在 提交之后，将提供包含在 HDFS 中的输⼊和输出⽬录。JobTracker 使⽤⽂件块信 息（物理量和位置）确定如何创建其他 TaskTracker 从属任务。MapReduce 被复制到每个出 现输⼊⽂件块的节点。将为特定节点上的每个⽂件块创建⼀个惟⼀的从属任务。每个 TaskTracker 将 状态和完成信息报告给 JobTracker。图 3 显示⼀个示例集群中的⼯作分布。 Hadop 的这个特点⾮常重要，因为它并没有将存储移动到某个位置以供处理，⽽是将处理移动到存 储。这通过根据集群中的节点数调节处理，因此⽀持⾼效的 。

应⽤程序

应⽤程序

数据处理

# 6MapReduce与Hadop之⽐较编辑

Hadop是Apache软件基⾦会发起的⼀个项⽬，在⼤数据分析以及⾮结构化数据蔓延的背景下， Hadop受到了前所未有的关注。

- [8] 是⼀种分布式数据和计算的框架。它很擅⻓存储⼤量的半结构化的数据集。数据可以随机存


Hadop

放，所以⼀个磁盘的失败并不会带来数据丢失。Hadop也⾮常擅⻓分布式计算⸺快速地跨多台机器 处理⼤型数据集合。

MapReduce

是处理⼤量半结构化数据集合的编程模型。编程模型是⼀种处理并结构化特定问题的⽅ 式。例如，在⼀个关系数据库中，使⽤⼀种集合语⾔执⾏查询，如SQL。告诉语⾔想要的结果，并将 它提交给系统来计算出如何产⽣计算。还可以⽤更传统的语⾔( ，Java)，⼀步步地来解决问题。这 是两种不同的编程模型，MapReduce就是另外⼀种。MapReduce和Hadop是相互独⽴的，实际上⼜ 能相互配合⼯作得很好。

C+

- [9]


# 7开源实现编辑

Hadop是项⽬的总称。主要是由HDFS和MapReduce组成。 HDFS是Gogle File System（GFS）的开源实现。 MapReduce是Gogle MapReduce的开源实现。 这个分布式框架很有创造性，⽽且有极⼤的扩展性，使得Gogle在系统吞吐量上有很⼤的竞争⼒。因 此Apache基⾦会⽤Java实现了⼀个开源版本，⽀持Fedora、Ubuntu等Linux平台。雅⻁和硅⾕⻛险投 资公司Benchmark Capital 联合成⽴⼀家名为Hortonworks的新公司，接管被⼴泛应⽤的数据分析 Hadop的开发⼯作。 Hadop实现了HDFS⽂件系统和MapRecue。⽤户只要继承MapReduceBase，提供分别实现Map和 Reduce的两个类，并注册Job即可⾃动分布式运⾏。 ⾄今为⽌是2.4.0，稳定版本是1.2.1 和 yarn 的 2.4.0。 HDFS把 分成两类：NameNode和DataNode。NameNode是唯⼀的，程序与之通信，然后从 DataNode上存取⽂件。这些操作是透明的，与普通的⽂件系统API没有区别。 MapReduce则是JobTracker节点为主，分配⼯作以及负责和 通信。 HDFS和MapReduce实现是完全分离的，并不是没有HDFS就不能MapReduce运算。 Hadop也跟其他 项⽬有共同点和⽬标：实现海量数据的计算。⽽进⾏海量计算需要⼀个稳定 的，安全的数据容器，才有了Hadop （HDFS，Hadop Distributed File System）。 HDFS通信部分使⽤org.apache.hadop.ipc，可以很快使⽤RPC.Server.start()构造⼀个节点，具体业 务功能还需⾃⼰实现。针对HDFS的业务则为数据流的读写，NameNode/DataNode的通信等。 MapReduce主要在org.apache.hadop.mapred，实现提供的接⼝类，并完成节点通信（可以不是 hadop通信接⼝），就能进⾏MapReduce运算。

软件

节点

⽤户程序

云计算

分布式⽂件系统

# 8⼦项⽬编辑

Hadop Comon: 在0.20及以前的版本中，包含HDFS、MapReduce和其他项⽬公共内容，从0.21开 始HDFS和MapReduce被分离为独⽴的⼦项⽬，其余内容为Hadop Comon

HDFS 分布式⽂件系统 MapReduce 并⾏计算

: Hadop (Distributed File System) － HDFS (Hadop Distributed File System)

： 框架，0.20前使⽤ org.apache.hadop.mapred 旧接⼝，0.20版本开始引⼊ org.apache.hadop.mapreduce的新API

: 类似Gogle BigTable的分布式NoSQL列数据库。（ 和 已经于2010年5⽉成为顶级 Apache 项⽬）

HBase HBase Avro

Hive Zokeper Avro

：数据仓库⼯具，由Facebok贡献。 ：分布式锁设施，提供类似Gogle Chuby的功能，由Facebok贡献。

：新的数据序列化格式与传输⼯具，将逐步取代Hadop原有的IPC机制。 Pig: ⼤数据分析平台，为⽤户提供多种接⼝。 Ambari：Hadop管理⼯具，可以快捷的监控、部署、管理集群。 Sqop：于在HADOP与传统的数据库间进⾏数据的传递。

# 9研究编辑

Hadop是原Yaho的Doug Cuting根据Gogle发布的学术论⽂研究⽽来。Doug Cuting给这个 Project起了个名字，就叫Hadop。 Doug Cuting在Cloudera公司任职。Cloudera的Hadop是商⽤版。不同于Apache的开源版。 如果要研究Hadop的话，下载Apache的开源版本是⼀种不错的选择。 只研究Apache版本的，不⾜以对Hadop的理念理解。再对Cloudera版本的研究，会更上⼀层楼。

的AsterData，也是Hadop的⼀个商⽤版，AsterData的MP理念，Aplications Within理念等 等，也都是值得研究。 Gogle的成功已经说明了RDB的下⼀代就是Nosql（Not Only SQL），⽐如说GFS，Hadop等等。 Hadop作为开源软件来说，其魅⼒更是不可估量。 上⽂中说到Gogle的学术论⽂，其中包涵有：

美国

- 1.
- 2.
- 3.
- 4.


Gogle File System（⼤规模分散⽂件系统） MapReduce （⼤规模分散FrameWork） BigTable（⼤规模分散数据库） Chuby（分散锁服务）

# 10⼤事记编辑

201年12⽉27⽇ -1.0.0版本释出。标志着Hadop已经初具⽣产规模。 209年4⽉ - 赢得每分钟排序，59秒内排序50 GB（在140个节点上）和173分钟内排序10 TB数 据（在340个节点上）。 209年3⽉ - 17个集群总共24 0台机器。 208年10⽉ - 研究集群每天装载10 TB的数据。 208年4⽉ - 赢得世界最快1 TB 在90个节点上⽤时209秒。

数据排序

- 207年4⽉ - 研究集群达到两个1 0个节点的集群。 207年1⽉ - 研究集群到达90个节点。 206年12⽉ - 标准排序在20个节点上运⾏1.8个⼩时，10个节点3.3⼩时，50个节点5.2⼩时，90 个节点7.8个⼩时。


206年 1⽉ - 研究集群增加到60个节点。 206年5⽉ - 标准排序在50个节点上运⾏42个⼩时（硬件配置⽐4⽉的更好）。 206年5⽉ - 雅⻁建⽴了⼀个30个节点的Hadop研究集群。 206年4⽉ - 标准排序（10 GB每个节点）在18个节点上运⾏47.9个⼩时。 206年2⽉ - 雅⻁的 团队采⽤Hadop。 206年2⽉ - Apache Hadop项⽬正式启动以⽀持MapReduce和HDFS的独⽴发展。 206年1⽉ - Doug Cuting加⼊雅⻁。 205年12⽉ - Nutch移植到新的框架，Hadop在20个节点上稳定运⾏。 204年 - 最初的版本（称为HDFS和MapReduce）由Doug Cuting和Mike Cafarela开始实施。

⽹格计算

# 1认证编辑

Cloudera

Cloudera公司主要提供Apache Hadop开发⼯程师认证（Cloudera CertifiedDeveloper for Apache Hadop ， CDH）和Apache Hadop管理⼯程师认证（Cloudera CertifiedAdministrator for Apache Hadop ， CAH），更多相关信息，请参阅Cloudera公司官⽅⽹站。

Hortonworks

Hortonworks Hadop培训课程是由Apache Hadop项⽬的领导者和核⼼开发⼈员所设计，代表了这 ⼀⾏业的最⾼⽔平。Hortonworks是国际领先的开发、推⼴和⽀持Apache Hadop的商业供应商，它 的Hadop认证也是业界公认的Hadop权威认证，分为开发者认证(HCAHD

- [10], Hortonworks Certified Apache HadopDeveloper)和管理员认证(HCAHA, Hortonwork Certified Apache HadopAdministrator)。


# 12信息安全编辑

通过Hadop安全部署经验总结，开发出以下⼗⼤建议，以确保⼤型和复杂多样环境下的数据信息安 全。1、先下⼿为强!在规划部署阶段就确定数据的隐私保护策略，最好是在将数据放⼊到Hadop之前 就确定好保护策略。

- 2、确定哪些数据属于企业的敏感数据。根据公司的隐私保护政策，以及相关的⾏业法规和政府规

章来综合确定。

- 3、及时发现敏感数据是否暴露在外，或者是否导⼊到Hadop中。
- 4、搜集信息并决定是否暴露出安全⻛险。
- 5、确定商业分析是否需要访问真实数据，或者确定是否可以使⽤这些敏感数据。然后，选择合适


的加密技术。如果有任何疑问，对其进⾏加密隐藏处理，同时提供最安全的加密技术和灵活的应对策 略，以适应未来需求的发展。

- 6、确保数据保护⽅案同时采⽤了隐藏和 ，尤其是如果我们需要将敏感数据在Hadop中

保持独⽴的话。

- 7、确保数据保护⽅案适⽤于所有的数据⽂件，以保存在数据汇总中实现数据分析的准确性。
- 8、确定是否需要为特定的数据集量身定制保护⽅案，并考虑将Hadop的⽬录分成较⼩的更为安

全的组。

- 9、确保选择的加密解决⽅案可与公司的访问控制技术互操作，允许不同⽤户可以有选择性地访问

Hadop集群中的数据。

- 10、确保需要加密的时候有合适的技术(⽐如 、Pig等)可被部署并⽀持⽆缝解密和快速访问数


加密技术

Java

据。 [1]

- 13Hadop之⽗编辑⽣活中，可能所有⼈都间接⽤过他的作品，他是 Lucene、Nutch、Hadop等项⽬的发起⼈。是

- 14图册编辑


他，把⾼深莫测的搜索技术形成产品，贡献给普通⼤众；还是他，打造了在云计算和⼤数据领域⾥如 ⽇中天的Hadop [12]。他是某种意义上的盗⽕者，他就是Doug Cuting。

HDFS流程图(2张) 更多图册

词条图册

◆

参考资料 1． 为什么hadop对你⼤数据处理的意义重⼤ ．中国⼤数据 [引⽤⽇期2014-03-27] ．2． 告诉你 Hadop是什么 ．⼤数据 [引⽤⽇期2014-06-19] ．3． Hadop发展历程 ．中国⼤数据交流 [引⽤⽇ 期2014-03-27] ．4． 详解Hadop核⼼架构 ．⼤数据中国 [引⽤⽇期2014-07-8] ．5． The Gogle File System ．Gogle Research Publications [引⽤⽇期2014-04-16] ．6． MapReduce: Simplified Data Procesing on Large Clusters ．Gogle Research Publications [引⽤⽇期2014-0416] ．7． Bigtable: A Distributed Storage System for Structured Data ．Gogle Research Publications [引⽤⽇期2014-04-16] ．8． 数据库技术漫谈之⼤话Hadop MapReduce ． TechTarget数据库 ．2012-02-29 [引⽤⽇期2014-07-24] ．9． 数据库新技术：Hadop和 MapReduce的⽐较 ．techTarget 数据库 ．201-06-02 [引⽤⽇期2014-07-25] ．10． Hortonworks Hadop Training ．Hortonworks官⽹ [引⽤⽇期2013-06-24] ． 1． Joe Austin ．Dataguise Enhances DG for Hadop with Selective Encryption to Enable Secure, High-Performance Analytics for Hadop Users ：⽹络出版 ，2013年 ．12． Hadop之⽗Doug Cuting ．⼤数据 [引⽤⽇期 2014-04-30] ．

