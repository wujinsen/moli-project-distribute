A high-throughput distributed mesaging system.

-Apache Kafka

Kafka作为时下最流⾏的开源消息系统，被⼴泛地应⽤在数据缓冲、异步通信、汇集⽇志、系统解 耦等⽅⾯。相⽐较于RocketMQ等其他常⻅消息系统，Kafka在保障了⼤部分功能特性的同时，还 提供了超⼀流的读写性能。

本⽂将针对Kafka性能⽅⾯进⾏简单分析，⾸先简单介绍⼀下Kafka的架构和涉及到的名词：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


Topic：⽤于划分Mesage的逻辑概念，⼀个Topic可以分布在多个Broker上。 Partition：是Kafka中横向扩展和⼀切并⾏化的基础，每个Topic都⾄少被切分为1个Partition。 Ofset：消息在Partition中的编号，编号顺序不跨Partition。 Consumer：⽤于从Broker中取出/消费Mesage。 Producer：⽤于往Broker中发送/⽣产Mesage。 Replication：Kafka⽀持以Partition为单位对Mesage进⾏冗余备份，每个Partition都可以配置⾄ 少1个Replication(当仅1个Replication时即仅该Partition本身)。 Leader：每个Replication集合中的Partition都会选出⼀个唯⼀的Leader，所有的读写请求都由 Leader处理。其他Replicas从Leader处把数据更新同步到本地，过程类似⼤家熟悉的MySQL中的 Binlog同步。 Broker：Kafka中使⽤Broker来接受Producer和Consumer的请求，并把Mesage持久化到本地磁 盘。每个Cluster当中会选举出⼀个Broker来担任Controler，负责处理Partition的Leader选举，协 调Partition迁移等⼯作。 ISR(In-Sync Replica)：是Replicas的⼀个⼦集，表示⽬前Alive且与Leader能够“Catch-up”的 Replicas集合。由于读写都是⾸先落到Leader上，所以⼀般来说通过同步机制从Leader上拉取数 据的Replica都会和Leader有⼀些延迟(包括了延迟时间和延迟条数两个维度)，任意⼀个超过阈值 都会把该Replica踢出ISR。每个Partition都有它⾃⼰独⽴的ISR。

以上⼏乎是我们在使⽤Kafka的过程中可能遇到的所有名词，同时也⽆⼀不是最核⼼的概念或组 件，感觉到从设计本身来说，Kafka还是⾜够简洁的。这次本⽂围绕Kafka优异的吞吐性能，逐个 介绍⼀下其设计与实现当中所使⽤的各项“⿊科技”。

Broker

不同于Redis和MemcacheQ等内存消息队列，Kafka的设计是把所有的Mesage都要写⼊速度低容 量⼤的硬盘，以此来换取更强的存储能⼒。实际上，Kafka使⽤硬盘并没有带来过多的性能损失， “规规矩矩”的抄了⼀条“近道”。

⾸先，说“规规矩矩”是因为Kafka在磁盘上只做Sequence I/O，由于消息系统读写的特殊性，这并 不存在什么问题。关于磁盘I/O的性能，引⽤⼀组Kafka官⽅给出的测试数据(Raid-5，720rpm)：

Sequence I/O: 60MB/s

Random I/O: 10KB/s

所以通过只做Sequence I/O的限制，规避了磁盘访问速度低下对性能可能造成的影响。

接下来我们再聊⼀聊Kafka是如何“抄近道的”。

⾸先，Kafka重度依赖底层操作系统提供的PageCache功能。当上层有写操作时，操作系统只是将 数据写⼊PageCache，同时标记Page属性为Dirty。当读操作发⽣时，先从PageCache中查找，如 果发⽣缺⻚才进⾏磁盘调度，最终返回需要的数据。实际上PageCache是把尽可能多的空闲内存 都当做了磁盘缓存来使⽤。同时如果有其他进程申请内存，回收PageCache的代价⼜很⼩，所以 现代的OS都⽀持PageCache。

使⽤PageCache功能同时可以避免在JVM内部缓存数据，JVM为我们提供了强⼤的GC能⼒，同时 也引⼊了⼀些问题不适⽤与Kafka的设计。

如果在Heap内管理缓存，JVM的GC线程会频繁扫描Heap空间，带来不必要的开销。如果Heap过 ⼤，执⾏⼀次Ful GC对系统的可⽤性来说将是极⼤的挑战。 所有在在JVM内的对象都不免带有⼀个Object Overhead(千万不可⼩视)，内存的有效空间利⽤率会 因此降低。 所有的In-Proces Cache在OS中都有⼀份同样的PageCache。所以通过将缓存只放在 PageCache，可以⾄少让可⽤缓存空间翻倍。 如果Kafka重启，所有的In-Proces Cache都会失效，⽽OS管理的PageCache依然可以继续使⽤。

PageCache还只是第⼀步，Kafka为了进⼀步的优化性能还采⽤了Sendfile技术。在解释Sendfile 之前，⾸先介绍⼀下传统的⽹络I/O操作流程，⼤体上分为以下4步。

- 1.
- 2.
- 3.
- 4.


OS 从硬盘把数据读到内核区的PageCache。 ⽤户进程把数据从内核区Copy到⽤户区。 然后⽤户进程再把数据写⼊到Socket，数据流⼊内核区的Socket Bufer上。 OS 再把数据从Bufer中Copy到⽹卡的Bufer上，这样完成⼀次发送。

![image 1](<Kafka 高性能吞吐揭秘.note_images/imageFile1.png>)

整个过程共经历两次Context Switch，四次System Cal。同⼀份数据在内核Bufer与⽤户Bufer之 间重复拷⻉，效率低下。其中2、3两步没有必要，完全可以直接在内核区完成数据拷⻉。这也正 是Sendfile所解决的问题，经过Sendfile优化后，整个I/O过程就变成了下⾯这个样⼦。

![image 2](<Kafka 高性能吞吐揭秘.note_images/imageFile2.png>)

通过以上的介绍不难看出，Kafka的设计初衷是尽⼀切努⼒在内存中完成数据交换，⽆论是对外作 为⼀整个消息系统，或是内部同底层操作系统的交互。如果Producer和Consumer之间⽣产和消费 进度上配合得当，完全可以实现数据交换零I/O。这也就是我为什么说Kafka使⽤“硬盘”并没有带来 过多性能损失的原因。下⾯是我在⽣产环境中采到的⼀些指标。

(20 Brokers, 75 Partitions per Broker, 10k msg/s)

![image 3](<Kafka 高性能吞吐揭秘.note_images/imageFile3.png>)

此时的集群只有写，没有读操作。10M/s左右的Send的流量是Partition之间进⾏Replicate⽽产⽣ 的。从recv和writ的速率⽐较可以看出，写盘是使⽤Asynchronous+Batch的⽅式，底层OS可能还 会进⾏磁盘写顺序优化。⽽在有Read Request进来的时候分为两种情况，第⼀种是内存中完成数 据交换。

![image 4](<Kafka 高性能吞吐揭秘.note_images/imageFile4.png>)

Send流量从平均10M/s增加到了到平均60M/s，⽽磁盘Read只有不超过50KB/s。PageCache降低 磁盘I/O效果⾮常明显。

接下来是读⼀些收到了⼀段时间，已经从内存中被换出刷写到磁盘上的⽼数据。

![image 5](<Kafka 高性能吞吐揭秘.note_images/imageFile5.png>)

其他指标还是⽼样⼦，⽽磁盘Read已经飚⾼到40+MB/s。此时全部的数据都已经是⾛硬盘了(对硬 盘的顺序读取OS层会进⾏Prefil PageCache的优化)。依然没有任何性能问题。

Tips

- 1.
- 2.
- 3.
- 4.
- 5.


Kafka官⽅并不建议通过Broker端的log.flush.interval.mesages和log.flush.interval.ms来强制写 盘，认为数据的可靠性应该通过Replica来保证，⽽强制Flush数据到磁盘会对整体性能产⽣影响。 可以通过调整/proc/sys/vm/dirty_background_ratio和/proc/sys/vm/dirty_ratio来调优性能。 脏⻚率超过第⼀个指标会启动pdflush开始Flush Dirty PageCache。 脏⻚率超过第⼆个指标会阻塞所有的写操作来进⾏Flush。 根据不同的业务需求可以适当的降低dirty_background_ratio和提⾼dirty_ratio。

Partition

Partition是Kafka可以很好的横向扩展和提供⾼并发处理以及实现Replication的基础。

扩展性⽅⾯。⾸先，Kafka允许Partition在集群内的Broker之间任意移动，以此来均衡可能存在的 数据倾斜问题。其次，Partition⽀持⾃定义的分区算法，例如可以将同⼀个Key的所有消息都路由 到同⼀个Partition上去。 同时Leader也可以在In-Sync的Replica中迁移。由于针对某⼀个Partition 的所有读写请求都是只由Leader来处理，所以Kafka会尽量把Leader均匀的分散到集群的各个节点 上，以免造成⽹络流量过于集中。

并发⽅⾯。任意Partition在某⼀个时刻只能被⼀个Consumer Group内的⼀个Consumer消费(反过 来⼀个Consumer则可以同时消费多个Partition)，Kafka⾮常简洁的Ofset机制最⼩化了Broker和 Consumer之间的交互，这使Kafka并不会像同类其他消息队列⼀样，随着下游Consumer数⽬的增 加⽽成⽐例的降低性能。此外，如果多个Consumer恰巧都是消费时间序上很相近的数据，可以达 到很⾼的PageCache命中率，因⽽Kafka可以⾮常⾼效的⽀持⾼并发读操作，实践中基本可以达到 单机⽹卡上限。

不过，Partition的数量并不是越多越好，Partition的数量越多，平均到每⼀个Broker上的数量也就 越多。考虑到Broker宕机(Network Failure, Ful GC)的情况下，需要由Controler来为所有宕机的 Broker上的所有Partition重新选举Leader，假设每个Partition的选举消耗10ms，如果Broker上有 50个Partition，那么在进⾏选举的5s的时间⾥，对上述Partition的读写操作都会触发 LeaderNotAvailableException。

再进⼀步，如果挂掉的Broker是整个集群的Controler，那么⾸先要进⾏的是重新任命⼀个Broker 作为Controler。新任命的Controler要从Zokeper上获取所有Partition的Meta信息，获取每个 信息⼤概3-5ms，那么如果有1 0个Partition这个时间就会达到30s-50s。⽽且不要忘记这只是 重新启动⼀个Controler花费的时间，在这基础上还要再加上前⾯说的选举Leader的时间 -_- !

此外，在Broker端，对Producer和Consumer都使⽤了Bufer机制。其中Bufer的⼤⼩是统⼀配置 的，数量则与Partition个数相同。如果Partition个数过多，会导致Producer和Consumer的Bufer 内存占⽤过⼤。

Tips

- 1.
- 2.
- 3.


Partition的数量尽量提前预分配，虽然可以在后期动态增加Partition，但是会冒着可能破坏 Mesage Key和Partition之间对应关系的⻛险。 Replica的数量不要过多，如果条件允许尽量把Replica集合内的Partition分别调整到不同的Rack。 尽⼀切努⼒保证每次停Broker时都可以Clean Shutdown，否则问题就不仅仅是恢复服务所需时间 ⻓，还可能出现数据损坏或其他很诡异的问题。

Producer

Kafka的研发团队表示在0.8版本⾥⽤Java重写了整个Producer，据说性能有了很⼤提升。我还没 有亲⾃对⽐试⽤过，这⾥就不做数据对⽐了。本⽂结尾的扩展阅读⾥提到了⼀套我认为⽐较好的 对照组，有兴趣的同学可以尝试⼀下。

其实在Producer端的优化⼤部分消息系统采取的⽅式都⽐较单⼀，⽆⾮也就化零为整、同步变异 步这么⼏种。

Kafka系统默认⽀持MesageSet，把多条Mesage⾃动地打成⼀个Group后发送出去，均摊后拉 低了每次通信的RT。⽽且在组织MesageSet的同时，还可以把数据重新排序，从爆发流式的随 机写⼊优化成较为平稳的线性写⼊。

此外，还要着重介绍的⼀点是，Producer⽀持End-to-End的压缩。数据在本地压缩后放到⽹络上 传输，在Broker⼀般不解压(除⾮指定要Dep-Iteration)，直⾄消息被Consume之后在客户端解 压。

当然⽤户也可以选择⾃⼰在应⽤层上做压缩和解压的⼯作(毕竟Kafka⽬前⽀持的压缩算法有限， 只有GZIP和Snapy)，不过这样做反⽽会意外的降低效率！！！！ Kafka的End-to-End压缩与 MesageSet配合在⼀起⼯作效果最佳，上⾯的做法直接割裂了两者间联系。⾄于道理其实很简 单，压缩算法中⼀条基本的原理“重复的数据量越多，压缩⽐越⾼”。⽆关于消息体的内容，⽆关 于消息体的数量，⼤多数情况下输⼊数据量⼤⼀些会取得更好的压缩⽐。

不过Kafka采⽤MesageSet也导致在可⽤性上⼀定程度的妥协。每次发送数据时，Producer都是 send()之后就认为已经发送出去了，但其实⼤多数情况下消息还在内存的MesageSet当中，尚未 发送到⽹络，这时候如果Producer挂掉，那就会出现丢数据的情况。

为了解决这个问题，Kafka在0.8版本的设计借鉴了⽹络当中的ack机制。如果对性能要求较⾼，⼜ 能在⼀定程度上允许Mesage的丢失，那就可以设置request.required.acks=0来关闭ack，以全 速发送。如果需要对发送的消息进⾏确认，就需要设置request.required.acks为1或-1，那么1和-1 ⼜有什么区别呢？这⾥⼜要提到前⾯聊的有关Replica数量问题。如果配置为1，表示消息只需要被 Leader接收并确认即可，其他的Replica可以进⾏异步拉取⽆需⽴即进⾏确认，在保证可靠性的同 时⼜不会把效率拉得很低。如果设置为-1，表示消息要Comit到该Partition的ISR集合中的所有 Replica后，才可以返回ack，消息的发送会更安全，⽽整个过程的延迟会随着Replica的数量正⽐ 增⻓，这⾥就需要根据不同的需求做相应的优化。

Tips

- 0.8版本的request.required.acks默认是0(同0.7)。

Consumer

Consumer端的设计⼤体上还算是⽐较常规的。

通过Consumer Group，可以⽀持⽣产者消费者和队列访问两种模式。 Consumer API分为High level和Low level两种。前⼀种重度依赖Zokeper，所以性能差⼀些且不 ⾃由，但是超省⼼。第⼆种不依赖Zokeper服务，⽆论从⾃由度和性能上都有更好的表现，但是 所有的异常(Leader迁移、Ofset越界、Broker宕机等)和Ofset的维护都需要⾃⾏处理。 ⼤家可以关注下不⽇发布的0.9 Release。开发⼈员⼜⽤Java重写了⼀套Consumer。把两套API合并 在⼀起，同时去掉了对Zokeper的依赖。据说性能有⼤幅度提升哦 ~

- 1.
- 2.


Producer的线程不要配置过多，尤其是在Miror或者Migration中使⽤的时候，会加剧⽬标集群 Partition消息乱序的情况(如果你的应⽤场景对消息顺序很敏感的话)。

Tips

强烈推荐使⽤Low level API，虽然繁琐⼀些，但是⽬前只有这个API可以对Eror数据进⾏⾃定义处 理，尤其是处理Broker异常或由于Unclean Shutdown导致的Corupted Data时，否则⽆法Skip只 能等着“坏消息”在Broker上被Rotate掉，在此期间该Replica将会⼀直处于不可⽤状态。

扩展阅读

Sendfile: htps:/ w.ibm.com/developerworks/cn/java/j-zerocopy/

So whatʼs wrong with 1975 progra ming: htps:/ w.varnishcache.org/trac/wiki/ArchitectNotes

Benchmarking: htps:/enginering.linkedin.com/kafka/benchmarking-apache-kafka-2-milionwrites-second-thre-cheap-machines

