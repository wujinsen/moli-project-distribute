# kafka集群原理介绍

@(博客⽂章)[kafka|⼤数据]

kafka集群原理介绍 ⼀基础理论 ⼆配置⽂件 三错误处理

本系统⽂章共三篇，分别为

- 1、kafka集群原理介绍了以下⼏个⽅⾯的内容：

- （1）kafka基础理论

- （2）参数配置

- （3）错误处理

- （4）kafka集群在zokeper集群中的内容


- 2、kafka集群操作介绍了kafka集群的安装与操作

- （1）单机版安装

- （2）集群安装

- （3）集群启停操作

- （4）topic相关操作

- （5）某个broker挂掉，重启本机器

- （6）某个broker挂掉且⽆法重启，使⽤其它机器代替

- （7）扩容

- （8）数据迁移

- （9）机器下线

- （10）增加副本数量


（ 1）平衡leader

- 3、kafka集群编程介绍了…


# （⼀）基础理论

- 1、相关资料

官⽅资料，⾮常详细：

以下部分内容来源于此⽂档。

- 2、kafka是什么？

- （1）Kafka is a distributed, partitioned, replicated comit log service. It provides the functionality of a mesaging system, but with a unique design.

Kafka是⼀个 分布式的、可分区的、可复制的消息系统。它提供了普通消息系统的功能，但具有⾃ ⼰独特的设计。

- （2）可以简单的理解为：kafka是⼀个⽇志集群，各种各样的服务器将它们⾃身的⽇志发送到集 群中进⾏统⼀汇总和存储，然后其它机器从集群中拉取消息进⾏分析处理，如ELT、数据挖掘 等。

- （3）kafka使⽤scala语⾔实现，提供了JAVA API，同时对多种语⾔都提供了⽀持。


- 3、⼏个关键术语

topic: Kafka将消息以topic为单位进⾏归纳。

producer: 将向Kafka topic发布消息的程序称为producers.

consumer: 将预订topics并消费消息的程序称为consumer.

broker: Kafka以集群的⽅式运⾏，可以由⼀个或多个服务组成，每个服务叫做⼀个broker.

- 4、分区与副本


htp:/kafka.apache.org/documentation.html#quickstart

- （1）⼀个topic是对⼀组消息的归纳。对每个topic，Kafka 对它的⽇志进⾏了分区。

- （2）⼀般⽽⾔，⼀个topic会有多个分区，每个分区会有多个副本。 分区是分了将⼀个topic分到多个地⽅存储，提⾼并⾏处理的能⼒。副本是为了容错，保证数据不 丢失。

- （3）对于每⼀个分区，都会选取⼀个leader，这个分区的所有读取都在这个leader中进⾏，⽽其 它副本会同步leader中的数据，且只做备份。


即leader只是针对⼀个分区⽽⾔，⽽⾮整个集群。⼀个服务器对于某个分区是leader，对于其它分 区可能是folower。

- （4） Producer将消息发布到它指定的topic中,并负责决定发布到哪个分区。通常简单的由负载均 衡机制随机选择分区，但也可以通过特定的分区函数选择分区。

- （5）发布消息通常有两种模式：队列模式（queuing）和发布-订阅模式(publish-subscribe)。队 列模式中，consumers可以同时从服务端读取消息，每个消息只被其中⼀个consumer读到；发 布-订阅模式中消息被⼴播到所有的consumer中。

Consumers可以加⼊⼀个consumer 组，共同竞争⼀个topic，topic中的消息将被分发到组中的⼀ 个成员中。同⼀组中的consumer可以在不同的程序中，也可以在不同的机器上。如果所有的 consumer都在⼀个组中，这就成为了传统的队列模式，在各consumer中实现负载均衡。

如果所有的consumer都不在不同的组中，这就成为了发布-订阅模式，所有的消息都被分发到所 有的consumer中。

更常⻅的是，每个topic都有若⼲数量的consumer组，每个组都是⼀个逻辑上的“订阅者”，为了容 错和更好的稳定性，每个组由若⼲consumer组成。这其实就是⼀个发布-订阅模式，只不过订阅 者是个组⽽不是单个consumer。

- （6）有序性 相⽐传统的消息系统，Kafka可以很好的保证有序性。


传统的队列在服务器上保存有序的消息，如果多个consumers同时从这个服务器消费消息，服务 器就会以消息存储的顺序向consumer分 发消息。虽然服务器按顺序发布消息，但是消息是被异步 的分发到各consumer上，所以当消息到达时可能已经失去了原来的顺序，这意味着并发消费将导 致 顺序错乱。为了避免故障，这样的消息系统通常使⽤“专⽤consumer”的概念，其实就是只允许 ⼀个消费者消费消息，当然这就意味着失去了并发性。

在这⽅⾯Kafka做的更好，通过分区的概念，Kafka可以在多个consumer组并发的情况下提供较好 的有序性和负载均衡。将每个分区分 只分发给⼀个consumer组，这样⼀个分区就只被这个组的⼀ 个consumer消费，就可以顺序的消费这个分区的消息。因为有多个分区，依然可以在多 个 consumer组之间进⾏负载均衡。注意consumer组的数量不能多于分区的数量，也就是有多少分 区就允许多少并发消费。

Kafka只能保证⼀个分区之内消息的有序性，在不同的分区之间是不可以的，这已经可以满⾜⼤部 分应⽤的需求。如果需要topic中所有消息的有序性，那就只能让这个topic只有⼀个分区，当然也 就只有⼀个consumer组消费它。

- 5、数据持久化（本部分内容直接翻译⾃官⽅⽂档） 不要畏惧⽂件系统!


Kafka⼤量依赖⽂件系统去存储和缓存消息。对于硬盘有个传统的观念是硬盘总是很慢，这使很多 ⼈怀疑基于⽂件系统的架构能否提供优异的性能。实际上硬盘的快慢完全取决于使⽤它的⽅式。 设计良好的硬盘架构可以和内存⼀样快。

在6块720转的SATA RAID-5磁盘阵列的线性写速度差不多是60MB/s，但是随即写的速度却是 10k/s，差了差不多6 0倍。现代的操作系统都对次做了⼤量的优化，使⽤了 read-ahead 和 write-behind的技巧，读取的时候成块的预读取数据，写的时候将各种微⼩琐碎的逻辑写⼊组织合 并成⼀次较⼤的物理写⼊。对此的深⼊讨论可以查看这⾥，它们发现线性的访问磁盘，很多时候 ⽐随机的内存访问快得多。

为了提⾼性能，现代操作系统往往使⽤内存作为磁盘的缓存，现代操作系统乐于把所有空闲内存 ⽤作磁盘缓存，虽然这可能在缓存回收和重新分配时牺牲⼀些性能。所有的磁盘读写操作都会经 过这个缓存，这不太可能被绕开除⾮直接使⽤I/O。所以虽然每个程序都在⾃⼰的线程⾥只缓存了 ⼀份数据，但在操作系统的缓存⾥还有⼀份，这等于存了两份数据。

另外再来讨论⼀下JVM,以下两个事实是众所周知的：

- •Java对象占⽤空间是⾮常⼤的，差不多是要存储的数据的两倍甚⾄更⾼。

- •随着堆中数据量的增加，垃圾回收回变的越来越困难。


基于以上分析，如果把数据缓存在内存⾥，因为需要存储两份，不得不使⽤两倍的内存空间， Kafka基于JVM，⼜不得不将空间再次加倍,再加上要避免GC带来的性能影响，在⼀个32G内存的 机器上，不得不使⽤到28-30G的内存空间。并且当系统重启的时候，⼜必须要将数据刷到内存中 （ 10GB 内存差不多要⽤10分钟），就算使⽤冷刷新（不是⼀次性刷进内存，⽽是在使⽤数据的 时候没有就刷到内存）也会导致最初的时候新能⾮常慢。但是使⽤⽂件系统，即使系统重启了， 也不需要刷新数据。使⽤⽂件系统也简化了维护数据⼀致性的逻辑。

所以与传统的将数据缓存在内存中然后刷到硬盘的设计不同，Kafka直接将数据写到了⽂件系统的 ⽇志中。

常量时间的操作效率

在⼤多数的消息系统中，数据持久化的机制往往是为每个cosumer提供⼀个B树或者其他的随机读 写的数据结构。B树当然是很棒的，但是也带了⼀些代价：⽐如B树的复杂度是O(log N)，O(log N)通常被认为就是常量复杂度了，但对于硬盘操作来说并⾮如此。磁盘进⾏⼀次搜索需要10ms， 每个硬盘在同⼀时间只能进⾏⼀次搜索，这样并发处理就成了问题。虽然存储系统使⽤缓存进⾏ 了⼤量优化，但是对于树结构的性能的观察结果却表明，它的性能往往随着数据的增⻓⽽线性下 降，数据增⻓⼀倍，速度就会降低⼀倍。

直观的讲，对于主要⽤于⽇志处理的消息系统，数据的持久化可以简单的通过将数据追加到⽂件 中实现，读的时候从⽂件中读就好了。这样做的好处是读和写都是 O(1) 的，并且读操作不会阻塞 写操作和其他操作。这样带来的性能优势是很明显的，因为性能和数据的⼤⼩没有关系了。

既然可以使⽤⼏乎没有容量限制（相对于内存来说）的硬盘空间建⽴消息系统，就可以在没有性 能损失的情况下提供⼀些⼀般消息系统不具备的特性。⽐如，⼀般的消息系统都是在消息被消费 后⽴即删除，Kafka却可以将消息保存⼀段时间（⽐如⼀星期），这给consumer提供了很好的机 动性和灵活性。

- 6、事务性


之前讨论了consumer和producer是怎么⼯作的，现在来讨论⼀下数据传输⽅⾯。数据传输的事务 定义通常有以下三种级别：

最多⼀次: 消息不会被重复发送，最多被传输⼀次，但也有可能⼀次不传输。

最少⼀次: 消息不会被漏发送，最少被传输⼀次，但也有可能被重复传输.

精确的⼀次（Exactly once）: 不会漏传输也不会重复传输,每个消息都传输被⼀次⽽且仅仅被传输 ⼀次，这是⼤家所期望的。

⼤多数消息系统声称可以做到“精确的⼀次”，但是仔细阅读它们的的⽂档可以看到⾥⾯存在误 导，⽐如没有说明当consumer或producer失败时怎么样，或者当有多个consumer并⾏时怎么 样，或写⼊硬盘的数据丢失时⼜会怎么样。kafka的做法要更先进⼀些。当发布消息时，Kafka有 ⼀个“comited”的概念，⼀旦消息被提交了，只要消息被写⼊的分区的所在的副本broker是活动 的，数据就不会丢失。关于副本的活动的概念，下节⽂档会讨论。现在假设broker是不会down 的。

如果producer发布消息时发⽣了⽹络错误，但⼜不确定实在提交之前发⽣的还是提交之后发⽣ 的，这种情况虽然不常⻅，但是必须考虑进去，现在Kafka版本还没有解决这个问题，将来的版本 正在努⼒尝试解决。

并不是所有的情况都需要“精确的⼀次”这样⾼的级别，Kafka允许producer灵活的指定级别。⽐如 producer可以指定必须等待消息被提交的通知，或者完全的异步发送消息⽽不等待任何通知，或 者仅仅等待leader声明它拿到了消息（folowers没有必要）。

现在从consumer的⽅⾯考虑这个问题，所有的副本都有相同的⽇志⽂件和相同的ofset， consumer维护⾃⼰消费的消息的ofset，如果consumer不会崩溃当然可以在内存中保存这个值， 当然谁也不能保证这点。如果consumer崩溃了，会有另外⼀个consumer接着消费消息，它需要 从⼀个合适的ofset继续处理。这种情况下可以有以下选择：

consumer可以先读取消息，然后将ofset写⼊⽇志⽂件中，然后再处理消息。这存在⼀种可能就 是在存储ofset后还没处理消息就crash了，新的consumer继续从这个ofset处理，那么就会有些 消息永远不会被处理，这就是上⾯说的“最多⼀次”。

consumer可以先读取消息，处理消息，最后记录ofset，当然如果在记录ofset之前就crash了， 新的consumer会重复的消费⼀些消息，这就是上⾯说的“最少⼀次”。

“精确⼀次”可以通过将提交分为两个阶段来解决：保存了ofset后提交⼀次，消息处理成功之后再 提交⼀次。但是还有个更简单的做法：将消息的ofset和消息被处理后的结果保存在⼀起。⽐如⽤ Hadop ETL处理消息时，将处理后的结果和ofset同时保存在HDFS中，这样就能保证消息和 ofser同时被处理了

- 7、关于性能优化


Kafka在提⾼效率⽅⾯做了很⼤努⼒。Kafka的⼀个主要使⽤场景是处理⽹站活动⽇志，吞吐量是 ⾮常⼤的，每个⻚⾯都会产⽣好多次写操作。读⽅⾯，假设每个消息只被消费⼀次，读的量的也 是很⼤的，Kafka也尽量使读的操作更轻量化。

我们之前讨论了磁盘的性能问题，线性读写的情况下影响磁盘性能问题⼤约有两个⽅⾯：太多的 琐碎的I/O操作和太多的字节拷⻉。I/O问题发⽣在客户端和服务端之间，也发⽣在服务端内部的持 久化的操作中。

消息集（mesage set）

为了避免这些问题，Kafka建⽴了“消息集（mesage set）”的概念，将消息组织到⼀起，作为处 理的单位。以消息集为单位处理消息，⽐以单个的消息为单位处理，会提升不少性能。Producer 把消息集⼀块发送给服务端，⽽不是⼀条条的发送；服务端把消息集⼀次性的追加到⽇志⽂件 中，这样减少了琐碎的I/O操作。consumer也可以⼀次性的请求⼀个消息集。

另外⼀个性能优化是在字节拷⻉⽅⾯。在低负载的情况下这不是问题，但是在⾼负载的情况下它 的影响还是很⼤的。为了避免这个问题，Kafka使⽤了标准的⼆进制消息格式，这个格式可以在 producer,broker和producer之间共享⽽⽆需做任何改动。

zero copy

Broker维护的消息⽇志仅仅是⼀些⽬录⽂件，消息集以固定队的格式写⼊到⽇志⽂件中，这个格 式producer和consumer是共享的，这使得Kafka可以⼀个很重要的点进⾏优化：消息在⽹络上的 传递。现代的unix操作系统提供了⾼性能的将数据从⻚⾯缓存发送到socket的系统函数，在linux 中，这个函数是sendfile.

为了更好的理解sendfile的好处，我们先来看下⼀般将数据从⽂件发送到socket的数据流向：

操作系统把数据从⽂件拷⻉内核中的⻚缓存中

应⽤程序从⻚缓存从把数据拷⻉⾃⼰的内存缓存中

应⽤程序将数据写⼊到内核中socket缓存中

操作系统把数据从socket缓存中拷⻉到⽹卡接⼝缓存，从这⾥发送到⽹络上。

这显然是低效率的，有4次拷⻉和2次系统调⽤。Sendfile通过直接将数据从⻚⾯缓存发送⽹卡接 ⼝缓存，避免了重复拷⻉，⼤⼤的优化了性能。

在⼀个多consumers的场景⾥，数据仅仅被拷⻉到⻚⾯缓存⼀次⽽不是每次消费消息的时候都重 复的进⾏拷⻉。这使得消息以近乎⽹络带宽的速率发送出去。这样在磁盘层⾯你⼏乎看不到任何 的读操作，因为数据都是从⻚⾯缓存中直接发送到⽹络上去了。

- 8、数据压缩

很多时候，性能的瓶颈并⾮CPU或者硬盘⽽是⽹络带宽，对于需要在数据中⼼之间传送⼤量数据 的应⽤更是如此。当然⽤户可以在没有Kafka⽀持的情况下各⾃压缩⾃⼰的消息，但是这将导致较 低的压缩率，因为相⽐于将消息单独压缩，将⼤量⽂件压缩在⼀起才能起到最好的压缩效果。

Kafka采⽤了端到端的压缩：因为有“消息集”的概念，客户端的消息可以⼀起被压缩后送到服务 端，并以压缩后的格式写⼊⽇志⽂件，以压缩的格式发送到consumer，消息从producer发出到 consumer拿到都被是压缩的，只有在consumer使⽤的时候才被解压缩，所以叫做“端到端的压 缩”。

Kafka⽀持GZIP和Snapy压缩协议。

- 9、producer和consumer Kafka Producer


消息发送

producer直接将数据发送到broker的leader(主节点)，不需要在多个节点进⾏分发。为了帮助 producer做到这点，所有的Kafka节点都可以及时的告知:哪些节点是活动的，⽬标topic⽬标分区 的leader在哪。这样producer就可以直接将消息发送到⽬的地了。

客户端控制消息将被分发到哪个分区。可以通过负载均衡随机的选择，或者使⽤分区函数。Kafka 允许⽤户实现分区函数，指定分区的key，将消息hash到不同的分区上(当然有需要的话，也可以 覆盖这个分区函数⾃⼰实现逻辑).⽐如如果你指定的key是user id，那么同⼀个⽤户发送的消息都 被发送到同⼀个分区上。经过分区之后，consumer就可以有⽬的的消费某个分区的消息。

异步发送

批量发送可以很有效的提⾼发送效率。Kafka producer的异步发送模式允许进⾏批量发送，先将 消息缓存在内存中，然后⼀次请求批量发送出去。这个策略可以配置的，⽐如可以指定缓存的消 息达到某个量的时候就发出去，或者缓存了固定的时间后就发送出去（⽐如10条消息就发送，或 者每5秒发送⼀次）。这种策略将⼤⼤减少服务端的I/O次数。

既然缓存是在producer端进⾏的，那么当producer崩溃时，这些消息就会丢失。Kafka0.8.1的异 步发送模式还不⽀持回调，就不能在发送出错时进⾏处理。Kafka 0.9可能会增加这样的回调函 数。⻅Proposed Producer API.

Kafka Consumer

Kafa consumer消费消息时，向broker发出”fetch”请求去消费特定分区的消息。consumer指定消 息在⽇志中的偏移量（ofset），就可以消费从这个位置开始的消息。customer拥有了ofset的控 制权，可以向后回滚去重新消费之前的消息，这是很有意义的。

- 10、推还是拉？


Kafka最初考虑的问题是，customer应该从brokes拉取消息还是brokers将消息推送到consumer， 也就是pul还push。在这⽅⾯，Kafka遵循了⼀种⼤部分消息系统共同的传统的设计：producer将 消息推送到broker，consumer从broker拉取消息。

⼀些消息系统⽐如Scribe和Apache Flume采⽤了push模式，将消息推送到下游的consumer。这 样做有好处也有坏处：由broker决定消息推送的速率，对于不同消费速率的consumer就不太好处 理了。消息系统都致⼒于让consumer以最⼤的速率最快速的消费消息，但不幸的是，push模式 下，当broker推送的速率远⼤于consumer消费的速率时，consumer恐怕就要崩溃了。最终Kafka 还是选取了传统的pul模式。

Pul模式的另外⼀个好处是consumer可以⾃主决定是否批量的从broker拉取数据。Push模式必须 在不知道下游consumer消费能⼒和消费策略的情况下决定是⽴即推送每条消息还是缓存之后批量 推送。如果为了避免consumer崩溃⽽采⽤较低的推送速率，将可能导致⼀次只推送较少的消息⽽ 造成浪费。Pul模式下，consumer就可以根据⾃⼰的消费能⼒去决定这些策略。

Pul有个缺点是，如果broker没有可供消费的消息，将导致consumer不断在循环中轮询，直到新 消息到t达。为了避免这点，Kafka有个参数可以让consumer阻塞知道新消息到达(当然也可以阻塞 知道消息的数量达到某个特定的量这样就可以批量发送)。

1、消费状态跟踪

对消费消息状态的记录也是很重要的。

⼤部分消息系统在broker端的维护消息被消费的记录：⼀个消息被分发到consumer后broker就⻢ 上进⾏标记或者等待customer的通知后进⾏标记。这样也可以在消息在消费后⽴⻢就删除以减少 空间占⽤。

但是这样会不会有什么问题呢？如果⼀条消息发送出去之后就⽴即被标记为消费过的，⼀旦 consumer处理消息时失败了（⽐如程序崩溃）消息就丢失了。为了解决这个问题，很多消息系统 提供了另外⼀个个功能：当消息被发送出去之后仅仅被标记为已发送状态，当接到consumer已经 消费成功的通知后才标记为已被消费的状态。这虽然解决了消息丢失的问题，但产⽣了新问题， ⾸先如果consumer处理消息成功了但是向broker发送响应时失败了，这条消息将被消费两次。第 ⼆个问题时，broker必须维护每条消息的状态，并且每次都要先锁住消息然后更改状态然后释放 锁。这样麻烦⼜来了，且不说要维护⼤量的状态数据，⽐如如果消息发送出去但没有收到消费成 功的通知，这条消息将⼀直处于被锁定的状态，

Kafka采⽤了不同的策略。Topic被分成了若⼲分区，每个分区在同⼀时间只被⼀个consumer消 费。这意味着每个分区被消费的消息在⽇志中的位置仅仅是⼀个简单的整数：ofset。这样就很容 易标记每个分区消费状态就很容易了，仅仅需要⼀个整数⽽已。这样消费状态的跟踪就很简单 了。

这带来了另外⼀个好处：consumer可以把ofset调成⼀个较⽼的值，去重新消费⽼的消息。这对 传统的消息系统来说看起来有些不可思议，但确实是⾮常有⽤的，谁规定了⼀条消息只能被消费 ⼀次呢？consumer发现解析数据的程序有bug，在修改bug后再来解析⼀次消息，看起来是很合 理的额呀！

- 12、离线处理消息

⾼级的数据持久化允许consumer每个隔⼀段时间批量的将数据加载到线下系统中⽐如Hadop或 者数据仓库。这种情况下，Hadop可以将加载任务分拆，拆成每个broker或每个topic或每个分区 ⼀个加载任务。Hadop具有任务管理功能，当⼀个任务失败了就可以重启⽽不⽤担⼼数据被重新 加载，只要从上次加载的位置继续加载消息就可以了。

- 13、副本与主从关系（本部分直接翻译⾃官⽅⽂档）


Kafka允许topic的分区拥有若⼲副本，这个数量是可以配置的，你可以为每个topci配置副本的数 量。Kafka会⾃动在每个个副本上备份数据，所以当⼀个节点down掉时数据依然是可⽤的。

Kafka的副本功能不是必须的，你可以配置只有⼀个副本，这样其实就相当于只有⼀份数据。

创建副本的单位是topic的分区，每个分区都有⼀个leader和零或多个folowers.所有的读写操作都 由leader处理，⼀般分区的数量都⽐broker的数量多的多，各分区的leader均匀的分布在brokers 中。所有的folowers都复制leader的⽇志，⽇志中的消息和顺序都和leader中的⼀致。flowers向 普通的consumer那样从leader那⾥拉取消息并保存在⾃⼰的⽇志⽂件中。

许多分布式的消息系统⾃动的处理失败的请求，它们对⼀个节点是否

着（alive）”有着清晰的定义。Kafka判断⼀个节点是否活着有两个条件：

节点必须可以维护和ZoKeper的连接，Zokeper通过⼼跳机制检查每个节点的连接。

如果节点是个folower,他必须能及时的同步leader的写操作，延时不能太久。

符合以上条件的节点准确的说应该是“同步中的（in sync）”，⽽不是模糊的说是“活着的”或是“失 败的”。Leader会追踪所有“同步中”的节点，⼀旦⼀个down掉了，或是卡住了，或是延时太久， leader就会把它移除。⾄于延时多久算是“太久”，是由参数replica.lag.max.mesages决定的，怎 样算是卡住了，怎是由参数replica.lag.time.max.ms决定的。

只有当消息被所有的副本加⼊到⽇志中时，才算是“comited”，只有comited的消息才会发送 给consumer，这样就不⽤担⼼⼀旦leader down掉了消息会丢失。Producer也可以选择是否等待 消息被提交的通知，这个是由参数request.required.acks决定的。

Kafka保证只要有⼀个“同步中”的节点，“comited”的消息就不会丢失。

- 14、Leader的选择

Kafka的核⼼是⽇志⽂件，⽇志⽂件在集群中的同步是分布式数据系统最基础的要素。

如果leaders永远不会down的话我们就不需要folowers了！⼀旦leader down掉了，需要在 folowers中选择⼀个新的leader.但是folowers本身有可能延时太久或者crash，所以必须选择⾼质 量的folower作为leader.必须保证，⼀旦⼀个消息被提交了，但是leader down掉了，新选出的 leader必须可以提供这条消息。⼤部分的分布式系统采⽤了多数投票法则选择新的leader,对于多数 投票法则，就是根据所有副本节点的状况动态的选择最适合的作为leader.Kafka并不是使⽤这种⽅ 法。

Kafaka动态维护了⼀个同步状态的副本的集合（a set of in-sync replicas），简称ISR，在这个集 合中的节点都是和leader保持⾼度⼀致的，任何⼀条消息必须被这个集合中的每个节点读取并追加 到⽇志中了，才回通知外部这个消息已经被提交了。因此这个集合中的任何⼀个节点随时都可以 被选为leader.ISR在ZoKeper中维护。ISR中有f+1个节点，就可以允许在f个节点down掉的情况 下不会丢失消息并正常提供服。ISR的成员是动态的，如果⼀个节点被淘汰了，当它重新达到“同 步中”的状态时，他可以重新加⼊ISR.这种leader的选择⽅式是⾮常快速的，适合kafka的应⽤场 景。

⼀个邪恶的想法：如果所有节点都down掉了怎么办？Kafka对于数据不会丢失的保证，是基于⾄ 少⼀个节点是存活的，⼀旦所有节点都down了，这个就不能保证了。

实际应⽤中，当所有的副本都down掉时，必须及时作出反应。可以有以下两种选择:

等待ISR中的任何⼀个节点恢复并担任leader。

选择所有节点中（不只是ISR）第⼀个恢复的节点作为leader.

这是⼀个在可⽤性和连续性之间的权衡。如果等待ISR中的节点恢复，⼀旦ISR中的节点起不起来 或者数据都是了，那集群就永远恢复不了了。如果等待ISR意外的节点恢复，这个节点的数据就会 被作为线上数据，有可能和真实的数据有所出⼊，因为有些数据它可能还没同步到。Kafka⽬前选 择了第⼆种策略，在未来的版本中将使这个策略的选择可配置，可以根据场景灵活的选择。

这种窘境不只Kafka会遇到，⼏乎所有的分布式数据系统都会遇到。

- 15、副本管理


以上仅仅以⼀个topic⼀个分区为例⼦进⾏了讨论，但实际上⼀个Kafka将会管理成千上万的topic 分区.Kafka尽量的使所有分区均匀的分布到集群所有的节点上⽽不是集中在某些节点上，另外主从 关系也尽量均衡这样每个⼏点都会担任⼀定⽐例的分区的leader.

优化leader的选择过程也是很重要的，它决定了系统发⽣故障时的空窗期有多久。Kafka选择⼀个 节点作为“controler”,当发现有节点down掉的时候它负责在游泳分区的所有节点中选择新的leader, 这使得Kafka可以批量的⾼效的管理所有分区节点的主从关系。如果controler down掉了，活着的 节点中的⼀个会备切换为新的controler.

- 16、消息格式


- （1）消息格式


消息由⼀个固定⻓度的头部和可变⻓度的字节数组组成。头部包含了⼀个版本号和CRC32校验 码。

/**

- * 具有N个字节的消息的格式如下

*

- * 如果版本号是0

*

- * 1. 1个字节的 "magic" 标记

*

- * 2. 4个字节的CRC32校验码

*

- * 3. N - 5个字节的具体信息

*

- * 如果版本号是1

*

- * 1. 1个字节的 "magic" 标记

*

- * 2.1个字节的参数允许标注⼀些附加的信息⽐如是否压缩了，解码类型等

*

- * 3.4个字节的CRC32校验码

*

- * 4. N - 6 个字节的具体信息

*

- */


- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8

- 9

- 10


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

- 30

- 31

- 32


- 3


- 34

- 35

- 36


- 37

- 38

- 39

- 40

- 41

- 42

- 43


- 4


- （2）⽇志

⼀个叫做“my_topic”且有两个分区的的topic,它的⽇志有两个⽂件夹组成，my_topic_0和 my_topic_1,每个⽂件夹⾥放着具体的数据⽂件，每个数据⽂件都是⼀系列的⽇志实体，每个⽇志 实体有⼀个4个字节的整数N标注消息的⻓度，后边跟着N个字节的消息。每个消息都可以由⼀个 64位的整数ofset标注，ofset标注了这条消息在发送到这个分区的消息流中的起始位置。每个⽇ 志⽂件的名称都是这个⽂件第⼀条⽇志的ofset.所以第⼀个⽇志⽂件的名字就是

0.kafka.所以每相邻的两个⽂件名字的差就是⼀个数字S,S差不多就是配置⽂件中指 定的⽇志⽂件的最⼤容量。

消息的格式都由⼀个统⼀的接⼝维护，所以消息可以在producer,broker和consumer之间⽆缝的传 递。存储在硬盘上的消息格式如下所示：

消息⻓度: 4 bytes (value: 1+4+n)

版本号: 1 byte

CRC校验码: 4 bytes

具体的消息: n bytes

- （3）写操作

消息被不断的追加到最后⼀个⽇志的末尾，当⽇志的⼤⼩达到⼀个指定的值时就会产⽣⼀个新的 ⽂件。对于写操作有两个参数，⼀个规定了消息的数量达到这个值时必须将数据刷新到硬盘上， 另外⼀个规定了刷新到硬盘的时间间隔，这对数据的持久性是个保证，在系统崩溃的时候只会丢 失⼀定数量的消息或者⼀个时间段的消息。

- （4）读操作


读操作需要两个参数：⼀个64位的ofset和⼀个S字节的最⼤读取量。S通常⽐单个消息的⼤⼩要 ⼤，但在⼀些个别消息⽐较⼤的情况下，S会⼩于单个消息的⼤⼩。这种情况下读操作会不断重 试，每次重试都会将读取量加倍，直到读取到⼀个完整的消息。可以配置单个消息的最⼤值，这 样服务器就会拒绝⼤⼩超过这个值的消息。也可以给客户端指定⼀个尝试读取的最⼤上限，避免 为了读到⼀个完整的消息⽽⽆限次的重试。

在实际执⾏读取操纵时，⾸先需要定位数据所在的⽇志⽂件，然后根据ofset计算出在这个⽇志中 的ofset(前⾯的的ofset是整个分区的ofset),然后在这个ofset的位置进⾏读取。定位操作是由⼆ 分查找法完成的，Kafka在内存中为每个⽂件维护了ofset的范围。

下⾯是发送给consumer的结果的格式：

MessageSetSend (fetch result)

total length : 4 bytes

error code : 2 bytes

message 1 : x bytes

...

message n : x bytes

MultiMessageSetSend (multiFetch result)

total length : 4 bytes

error code : 2 bytes

messageSetSend 1

...

messageSetSend n

1 2 3 4 5 6 7 8 9

10

- 1

12 13 14 15 16 17 18 19 20 21

- 2


- 23

- 24

- 25

- 26


- （5）删除

⽇志管理器允许定制删除策略。⽬前的策略是删除修改时间在N天之前的⽇志（按时间删除），也 可以使⽤另外⼀个策略：保留最后的N GB数据的策略(按⼤⼩删除)。为了避免在删除时阻塞读操 作，采⽤了copy-on-write形式的实现，删除操作进⾏时，读取操作的⼆分查找功能实际是在⼀个 静态的快照副本上进⾏的，这类似于Java的CopyOnWriteArayList。

- （6）可靠性保证


⽇志⽂件有⼀个可配置的参数M，缓存超过这个数量的消息将被强⾏刷新到硬盘。⼀个⽇志矫正线 程将循环检查最新的⽇志⽂件中的消息确认每个消息都是合法的。合法的标准为：所有⽂件的⼤ ⼩的和最⼤的ofset⼩于⽇志⽂件的⼤⼩，并且消息的CRC32校验码与存储在消息实体中的校验码 ⼀致。如果在某个ofset发现不合法的消息，从这个ofset到下⼀个合法的ofset之间的内容将被 移除。

有两种情况必须考虑：1，当发⽣崩溃时有些数据块未能写⼊。2，写⼊了⼀些空⽩数据块。第⼆ 种情况的原因是，对于每个⽂件，操作系统都有⼀个inode（inode是指在许多“类Unix⽂件系统” 中的⼀种数据结构。每个inode保存了⽂件系统中的⼀个⽂件系统对象,包括⽂件、⽬录、⼤⼩、设 备⽂件、socket、管道, 等等），但⽆法保证更新inode和写⼊数据的顺序，当inode保存的⼤⼩信 息被更新了，但写⼊数据时发⽣了崩溃，就产⽣了空⽩数据块。CRC校验码可以检查这些块并移 除，当然因为崩溃⽽未写⼊的数据块也就丢失了

# ⼆、配置⽂件

（⼀）java调优

特别说明⼀下JVM配置 在bin/kafka-server-start.sh中添加以下内容：

export KAFKA_HEAP_OPTS="-Xmx4G -Xms4G"

1

官⽅的推荐使⽤G1GC，但感觉还不稳定，还是先⽤CMS算了。以下为官⽅推荐内容

- -Xms4g -Xmx4g -XX:PermSize=48m -XX:MaxPermSize=48m -XX:+UseG1GC -XX:MaxGCPauseMillis=20

- -XX:InitiatingHeapOccupancyPercent=35


For reference, here are the stats on one of LinkedIn's busiest clusters (at peak): - 15 brokers - 15.5k partitions (replication factor 2) - 400k messages/sec in - 70 MB/sec inbound, 400 MB/sec+ outbound The tuning looks fairly aggressive, but all of the brokers in that cluster have a 90% GC pause time of about 21ms, and they're doing less than 1 young GC per second.

- 1

- 2

- 3


（⼆）参数说明

kafka中有很多的配置参数，⼤致可以分为以下4类：

Broker Configs Consumer Configs Producer Configs New Producer Configs

- 1

- 2

- 3

- 4


htp:/kafka.apache.org/docume ntation.html#consumerconfigs

以下仅对部分重要参数说明并不断完善，全部的参数说明请参考

broker中的配置只有3个参数是必须提供的：broker.id，log,dir, zokeper.conect.

- 1、broker.id=0 ⽤于区分broker，确保每台机器不同,要求是正数。当该服务器的IP地址发⽣改变 时，broker.id没有变化，则不会影响consumers的消息情况

- 2、log.dirs=/home/data/kafka kafka⽤于放置消息的⽬录，默认为/tmp/kafka-logs。它可以是以 逗号分隔的多个⽬录，创建新分区时，默认会选择存在最少分区的⽬录。

- 3、zokeper.conect=192.168.169.91 2181,192.168.169.92 2181,192.168.169.93 2181/kafka zk ⽤于放置kafka信息的地⽅。注意⼀般情况下，直接使⽤ 192.168.169.91 2181,192.168.169.92 2181,192.168.169.93 2181即可，此时kafka的相关信息会放 在zk的根⽬录下，但如果这个zk集群同时为多个kafka集群，或者其它集群服务，则信息会很混 乱，甚⾄有冲突。因此⼀般会建⼀个⽬录⽤于放置kafka集群信息的⽬录，此处的⽬录为/kafka。 注意，这个⽬录必须⼿⼯创建，kafka不会⾃动创建这个⽬录。此外，在conusmer中也必须使⽤ 192.168.169.91 2181,192.168.169.92 2181,192.168.169.93 2181/kafka来读取topic内容。

- 4、num.partitions=1 创建topic时，默认的分区数

- 5、num.network.threads=10 broker⽤于处理⽹络请求的线程数，如不配置默认为3

- 6、zokeper.conection.timeout.ms=6 0

- 7、mesage.max.bytes=1 0 replica.fetch.max.bytes=1073741824 ⼀条消息的最⼤字节数，说明如下：


kafka中出现以下异常：

[2015-06-09 17:03:05,094] ERROR [KafkaApi-0] Error processing ProducerRequest with correlation id 616 from client kafka-client on partition [test3,0] (kafka.server.KafkaApis) kafka.common.MessageSizeTooLargeException: Message size is 2211366 bytes which exceeds the maximum configured message size of 1000012.

原因是集群默认每次只能接受约1M的消息，如果客户端⼀次发送的消息⼤于这个数值则会导致异 常。

在server.properties中添加以下参数

message.max.bytes=1000000000 replica.fetch.max.bytes=1073741824

- 1

- 2


同时在consumer.properties中添加以下参数：

fetch.message.max.bytes=1073741824

1

然后重启kafka进程即可，现在每次最⼤可接收10M的消息。

- 8、delete.topic.enable=true 默认为false，即delete topic时只是marked for deletion，但并不会 真正删除topic。

- 9、关于⽇志的保存时间或量：


- （1）log.retention.hours=24 消息被删除前保存多少⼩时，默认1周168⼩时

- （2）log.retention.bytes 默认为-1，即不限制⼤⼩。注意此外的⼤⼩是指⼀个topic的⼀个分区的 最⼤字节数。


当超出上述2个限制的任何⼀个时，⽇志均会被删除。

也可以在topic级别定义这个参数：

retention.bytes＝3298534883328 #3T retention.bytes与retention.ms

- 10、同步发送还是异步发送，异步吞吐量较⼤，但可能引⼊错误，默认为sync


producer.type＝sync|async

This parameter specifies whether the mesages are sent asynchronously in a background thread. Valid values are (1) async for asynchronous send and (2) sync for synchronous send. By seting the producer to async we alow batching together of requests (which is great for throughput) but open the posibility of a failure of the client machine droping unsent data.

1、batch.size 默认值为16384

在async模式下，producer缓存多少个消息后再⼀起发送

- 12、compresion.type 默认值为none，可选gzip snapy

The compresion type for al data generated by the producer. The default is none (i.e. no compresion). Valid values are none, gzip, or snapy. Compresion is of ful batches of data, so the eficacy of batching wil also impact the compresion ratio (more batching means beter compresion).

- 13、default.replication.factor 消息副本的数量，默认为1，即没有副本


还有⼀些需要关注的配置项：

Replication configurations

⽤于folower从leader复制消息的线程数，默认为1

num.replica.fetchers=4

folower每次从leader复制消息的字节数，默认为1M，即1024*1024

replica.fetch.max.bytes=1048576

当folow向leader发送数据请求后，最⼤的等待时⻓，默认为50ms replica.fetch.wait.max.ms=50

每隔多久，folower会将其复制的highwater写到磁盘中，以便出错时恢复。 replica.high.watermark.checkpoint.interval.ms=5 0

folower与leader之间的time out时⻓，默认为30秒 replica.socket.timeout.ms=3 0

socket每次的bufer字节数 replica.socket.receive.bufer.bytes=6536

如果⼀个folower在这段时⻓内都没有向leader发出复制请求，则leader会认为其已经down掉，并 从ISR中去掉。

replica.lag.time.max.ms=1 0

如果⼀个folower⽐leader落后超过这个数据的消息数，则leader会将其从isr中去掉。 replica.lag.max.mesages=4 0 partition management controler 与replica之间的超时时⻓ controler.socket.timeout.ms=3 0

The bufer size for controler-to-broker-chanels

controler.mesage.queue.size=10

Log configuration

如果在创建topic时没有指定分区⼤⼩，默认的分区⼤⼩如下 num.partitions=8

kafka集群可以接收的最⼤消息字节数，默认为1M.注意，如果增⼤了这个数值，在consumer中也 必须增⼤这个数值，否则consumer将⽆法消费这个消息。

mesage.max.bytes=1 0

当向⼀个不存在的topic发送消息时，是否允许⾃动创建topic auto.create.topics.enable=true

kafka保存多久的数据，单位是⼩时

log.retention.hours=72

The number of mesages writen to a log partition before we force an fsync on the log. Seting this lower wil sync data to disk more

often but wil have a major impact on performance. We generaly recomend that people make use of replication for durability rather

than depending on single-server fsync, however this seting can be used to be extra certain.下 ⾯2个值默认都是Long.MaxValue。

log.flush.interval.ms=1 0 log.flush.interval.mesages=2 0 log.flush.scheduler.interval.ms=2 0 log.rol.hours=168 log.retention.check.interval.ms=3 0 log.segment.bytes=1073741824 # ZK configuration zokeper.conection.timeout.ms=6 0 zokeper.sync.time.ms=2 0 # Socket server configuration

执⾏请求的线程数，⾄少与你的磁盘数量相同。 num.io.threads=8

服务器⽤于处理⽹络请求的线程数，⼀般不需要更改，默认为3. num.network.threads=8

服务器允许最⼤的请求⼤⼩。它可以预防out of memory，⽽且应该⼩于java 堆⼤⼩。 socket.request.max.bytes=10485760 socket.receive.bufer.bytes=1048576 socket.send.bufer.bytes=1048576 queued.max.requests=16 fetch.purgatory.purge.interval.requests=10 producer.purgatory.purge.interval.requests=10

# 三、错误处理

- 1、配置kafka时，如果使⽤zokeper create /kafka创建了节点，kafka与storm集成时new ZkHosts(zks) 需要改成 new ZkHosts(zks,”/kafka/brokers”),不然会报

java.lang.RuntimeException: java.lang.RuntimeException: org.apache.zookeeper.KeeperException$NoNodeException: KeeperErrorCode = NoNode for /brokers/topics/my-replicated-topic5/partitions。

1

storm-kafka插件默认kafka的 zk_path如下：

public class ZkHosts implements BrokerHosts { private static final String DEFAULT_ZK_PATH = “/brokers”;

- 1

- 2


- 2、如果出现以下问题，代表偏移量出错，建议重新开⼀个topic

ERROR [KafkaApi-3] Error when processing fetch request for partition [xxxxx,1] offset 112394 from consumer with correlation id 0 (kafka.server.KafkaApis) kafka.common.OffsetOutOfRangeException: Request for offset 112394 but we only have log segments in the range 0 to 665.

- 1

- 2


- 3、当没有某个topic，或者是某个topic的node放置不在默认位置时，会有以下异常：


java.lang.RuntimeException: java.lang.RuntimeException: org.apache.zookeeper.KeeperException$NoNodeException: KeeperErrorCode = NoNode for /kafka/brokers/topics/mytest/partitions at storm.kafka.Dynam

1

- 4、kafka中出现以下异常：


[2015-06-09 17 03 05,094] EROR [KafkaApi-0] Eror procesing ProducerRequest with corelation id 616 from client kafka-client on partition [test3,0] (kafka.server.KafkaApis)

kafka.comon.MesageSizeToLargeException: Mesage size is 2136 bytes which exceds the maximum configured mesage size of 1 012.

原因是集群默认每次只能接受约1M的消息，如果客户端⼀次发送的消息⼤于这个数值则会导致异 常。

在server.properties中添加以下参数

message.max.bytes=1000000000 replica.fetch.max.bytes=1073741824

- 1

- 2


同时在consumer.properties中添加以下参数：

fetch.message.max.bytes=1073741824 `` 然后重启kafka进程即可，现在每次最⼤可接收100M的消息。

- 5、open too many files kafka出现异常，⽇志提示open too many file 查找⽂件打开数量 lsof -p 30353 | wc 如果在1000以上，⼀般都是不正常，⾛过65535就会出错。 原因打开了太多producer，没关闭，调⽤producer.close()即可。


#四、zookeeper中的内容 默认情况，kafka在zk的/brokers⽬录下记录topic相关的信息，但如果在创建topic时，指定了路径，则放置到固 定的路径中，如：

- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8

- 9

- 10 1


- 12

- 13

- 14


bin/kafka-topics.sh –create –zokeper 192.168.169.91 2181,192.168.169.92 2181,192.168.169.93 2181/kafka –replication-factor 3 – partitions 5 –topic test_topic

创建的topic，其相关信息会放置到/kafka/brokers中，这个⽬录中主要包括2个⼦⽬录：ids 和 topics

- 1、ids：记录这个kafka集群中有多少个broker 如：


ls /kafka/brokers/ids/ 3 2 5 4

这个集群有4个节点，节点id分别为2，3，4，5。 我们看⼀下内容

``` [zk: localhost:2181(CONNECTED) 27] get /kafka/brokers/ids/2 {"jmx_port":-1,"timestamp":"1435833841290","host":"kafka02log.i.nease.net","version":1,"port":9092} cZxid = 0x1000e8a68 ctime = Thu Jul 02 18:44:01 HKT 2015 mZxid = 0x1000e8a68 mtime = Thu Jul 02 18:44:01 HKT 2015 pZxid = 0x1000e8a68 cversion = 0 dataVersion = 0 aclVersion = 0 ephemeralOwner = 0x44e440d0bdf06eb dataLength = 104 numChildren = 0

1 2 3 4 5 6 7 8 9

10

- 1 12 13 14 15 16 17 18 19 20 21

- 2


记录着这个节点的⼀些基本情况。

- 2、topics 先看⼀下有哪些内容：


- [zk: localhost:2181(CONNECTED) 29] ls /kafka/brokers/topics/test30/partitions [3, 2, 1, 0, 4]

- [zk: localhost:2181(CONNECTED) 30] ls /kafka/brokers/topics/test30/partitions/0 [state] [zk: localhost:2181(CONNECTED) 1] get /kafka/brokers/topics/test30/partitions/0/state {"controller_epoch":4,"leader":5,"version":1,"leader_epoch":2,"isr":[5]} cZxid = 0x100017c5e ctime = Wed Jul 01 14:54:24 HKT 2015 mZxid = 0x1000e8a84 mtime = Thu Jul 02 18:44:01 HKT 2015 pZxid = 0x100017c5e cversion = 0 dataVersion = 2 aclVersion = 0 ephemeralOwner = 0x0 dataLength = 72 numChildren = 0


- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8

- 9

- 10 1


- 12

- 13

- 14

- 15

- 16

- 17


可以看某个分区的leader是哪个，从⽽读取kafka消息时，可以从这个leader中读取数据。

以下内容来⾃官⽅⽂档：

下⾯给出了zk中⽤于保存consumber与brokers相关信息的⽬录结构与算法介绍。

关于⽬录结构的前提说明：默认情况下，kafka相关的信息放在zk根⽬录下的某个路径中，但也可 以设置为单独的路径，设置⽅法⻅配置选项部分。在我们的集群中，我们建⽴了⼀个⽬录/kafka作 为所有kafka相关信息的保存位置。因此我们在这⾥所列的/kafka/xyz，对于默认情况应该是/xyz。

broker节点的注册

[zk: localhost:2181(CONNECTED) 140] get /kafka/brokers/ids/2 {"jmx_port":-1,"timestamp":"1437460315901","host":"gdc-kafka02log.i.nease.net","version":1,"port":9092}

- 1

- 2


在zk中，有⼀个broker节点的列表，列表中的每⼀项表示⼀个逻辑broker。在启动时，broker节点 会在zk中的/kafka/broker/ids/⽬录下创建⼀个znode，名称为配置⽂件中定义的broker id，如上⾯ 所示的/kafka/brokers/ids/2。建⽴逻辑broker id的⽬的是允许⼀个broker节点迁移到另⼀台机器 上，⽽不会影响到consumer的消费。如果想注册⼀个已经存在的broker id会引起错误（⽐如说有 2个broker的配置⽂件都写了同⼀个broker id）。

由于broker在zk中注册的是⼀个ephemeral znodes，因此当这个broker关机或者挂掉的时候，这 个注册信息会⾃动删除，从⽽会通知consumer这个节点已经不可⽤。

Topic注册

ls /kafka/brokers/topics/testtopic/partitions/

3 2 1 0 4

get /kafka/brokers/topics/testtopic/partitions/0/state

{"controller_epoch":9,"leader":5,"version":1,"leader_epoch":26,"isr":[5]}

- 3

- 4

- 5

- 6

- 7


每个topic都会在zk中注册，如上⾯的testopic有5个分区。

consumer与consumer组

为了彼此协调以及平衡数据的消费，consumer也会在zk中注册信息。通过设置 ofsets.storage=zokeper，可以将consumer的ofset保存在zk中，不过这种做法会被逐步淘 汰。现在推荐使⽤kafka作为ofset的保存。

⼀个组内的consumer可以共同消费⼀个topic，它们拥有同⼀个group_id。组内的consumer会尽 可能公平的将topic的分区切分。

consumer id注册

每⼀个consumer都会在zk注册信息，如：

get /kafka/consumers/console-consumer-30094/ids/console-consumer-30094_gdc-kafka03log.i.nease.net-1437029151314-d7cdc855 {"version":1,"subscription": {"streaming_ma30_sdc":1},"pattern":"white_list","timestamp":"1437459282749"}

- 1

- 2


consumer ofset

conusumer会根据它已经消费的最⼤的ofset，默念会存储在zk的⽬录下（也可以设置为 kafka）。

get /kafka/consumers/testtopic/offsets/testtopic/0 1413950858

注意这是⼀个永久节点，因此当consumer挂掉重启时可以继续读取。

分区owner注册

每⼀个broker分区会官能⼀个consumer组⾥的⼀个consumer消费，这个consumer必须建⽴它对 这个分区的占有（ownership），再开始消费。为了建⽴这个占有关系，consumer会在zk中建⽴ 相关的信息。

/kafka/consumers/[group_id]/owners/[topic]/[broker_id-partition_id] --> consumer_node_id (ephemeral node)

- 1

- 2


