---
title: 精尽 Kafka 面试题（最新更新时间：2019-12-14）.note（原文插图 annex）
slug: annex-精尽-Kafka-面试题（最新更新时间：2019-12-14）
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/面试笔试/kafka/精尽 Kafka 面试题（最新更新时间：2019-12-14）.note.md
related: [kafka-与-mq选型]
created: 2026-07-05
updated: 2026-07-05
---

以下⾯试题，基于⽹络整理，和⾃⼰编辑。具体参考的⽂章，会在⽂末给出所有的链接。 如果胖友有⾃⼰的疑问，欢迎在星球提问，我们⼀起整理吊吊的 Kafka ⾯试题的⼤保健。 ⽽题⽬的难度，艿艿尽量按照从容易到困难的顺序，逐步下去。 另外，这个⾯试题是建⽴在胖友看过 。 如果可能的话，推荐胖友先阅读了 ，更加系统可靠。

《精尽【消息队列 】⾯试题》 《Kafka 权威指南》

Apache Kafka 是什么?

Kafka 是基于发布与订阅的消息系统。它最初由 LinkedIn 公司开发，之后成为 Apache 项⽬的⼀部 分。Kafka 是⼀个分布式的，可分区的，冗余备份的持久性的⽇志服务。它主要⽤于处理活跃的流式数 据。 在⼤数据系统中，常常会碰到⼀个问题，整个⼤数据是由各个⼦系统组成，数据需要在各个⼦系统中 ⾼性能、低延迟的不停流转。传统的企业消息系统并不是⾮常适合⼤规模的数据处理。为了同时搞定 在线应⽤（消息）和离线应⽤（数据⽂件、⽇志），Kafka 就出现了。Kafka 可以起到两个作⽤：

降低系统组⽹复杂度。

降低编程复杂度，各个⼦系统不在是相互协商接⼝，各个⼦系统类似插⼝插在插座上，Kafka 承担 ⾼速数据总线的作⽤。

🦅 Kafka 的主要特点？

- 1、同时为发布和订阅提供⾼吞吐量。据了解，Kafka 每秒可以⽣产约 25 万消息（50MB），每秒 处理 55 万消息（110MB）。

- 2、可进⾏持久化操作。将消息持久化到磁盘，因此可⽤于批量消费，例如 ETL ，以及实时应⽤程 序。通过将数据持久化到硬盘，以及replication ，可以防⽌数据丢失。

- 3、分布式系统，易于向外扩展。所有的 Producer、Broker 和Consumer 都会有多个，均为分布式 的。并且，⽆需停机即可扩展机器。

- 4、消息被处理的状态是在 Consumer 端维护，⽽不是由 Broker 端维护。当失败时，能⾃动平衡。

消息是否被处理完成，是通过 Consumer 提交消费进度给 Broker ，⽽ 不是 Broker 消息被 Consumer 拉取后，就标记为已消费。 当 Consumer 异常崩溃时，可以重新分配消息分区到其它的 Consumer 们，然后继续消费。

- 5、⽀持 online 和 offline 的场景。


# 这段是从⽹络上找来的。感觉想要表达的意思是

🦅 聊聊 Kafka 的设计要点？

- 1）吞吐量 ⾼吞吐是 Kafka 需要实现的核⼼⽬标之⼀，为此 kafka 做了以下⼀些设计：


- 1、数据磁盘持久化：消息不在内存中 Cache ，直接写⼊到磁盘，充分利⽤磁盘的顺序读写性能。

- 2、zero-copy：减少 IO 操作步骤

传统的数据发送需要发送 4 次上下⽂切换。 采⽤ sendfile 系统调⽤之后，数据直接在内核态交换，系统上下⽂切 换减少为 2 次。根据测试结果，可以提⾼ 60% 的数据发送性能。 Zero-Copy 详细的技术细节可以参考

⽂章。

《Efficient data transfer through ze ro copy》

- 3、数据批量发送

- 4、数据压缩

- 5、Topic 划分为多个 Partition ，提⾼并⾏度。


直接使⽤ Linux ⽂件系统的 Cache ，来⾼效缓存数据。

采⽤ Linux Zero-Copy 提⾼发送性能。

数据在磁盘上存取代价为 O(1)。

Kafka 以 Topic 来进⾏消息管理，每个 Topic 包含多个 Partition ，每个 Partition 对应⼀个逻辑 log ，有多个 segment ⽂件组成。 每个 segment 中存储多条消息（⻅下图），消息 id 由其逻辑位置决 定，即从消息 id 可直接定位到消息的存储位置，避免 id 到位置的额 外映射。 每个 Partition 在内存中对应⼀个 index ，记录每个 segment 中的第⼀ 条消息偏移。

发布者发到某个 Topic 的消息会被均匀的分布到多个 Partition 上（随机或 根据⽤户指定的回调函数进⾏分布），Broker 收到发布消息往对应 Partition 的最后⼀个 segment 上添加该消息。 当某个 segment上 的消息条数达到配置值或消息发布时间超过阈值时， segment上 的消息会被 flush 到磁盘，只有 flush 到磁盘上的消息订阅者才能 订阅到，segment 达到⼀定的⼤⼩后将不会再往该 segment 写数据，Broker 会创建新的 segment ⽂件。

- 2）负载均衡


- 1、Producer 根据⽤户指定的算法，将消息发送到指定的 Partition 中。

- 2、Topic 存在多个 Partition ，每个 Partition 有⾃⼰的replica ，每个 replica 分布在不同的 Broker 节点上。多个Partition 需要选取出 Leader partition ，Leader Partition 负责读写，并由 Zookeeper 负责 fail over 。

- 3、相同 Topic 的多个 Partition 会分配给不同的 Consumer 进⾏拉取消息，进⾏消费。


- 3）拉取系统 由于 Kafka Broker 会持久化数据，Broker 没有内存压⼒，因此， Consumer ⾮常适合采取 pull 的⽅式 消费数据，具有以下⼏点好处：
- 4）可扩展性


- 1、简化 Kafka 设计。

- 2、Consumer 根据消费能⼒⾃主控制消息拉取速度。

- 3、Consumer 根据⾃身情况⾃主选择消费模式，例如批量，重复消费，从尾端开始消费等。


# 通过 Zookeeper 管理 Broker 与 Consumer 的动态加⼊与离开。

当需要增加 Broker 节点时，新增的 Broker 会向 Zookeeper 注册，⽽ Producer 及 Consumer 会根 据注册在 Zookeeper 上的 watcher 感知这些变化，并及时作出调整。

当新增和删除 Consumer 节点时，相同 Topic 的多个 Partition 会分配给剩余的 Consumer 们。 《为什么 Kafka 这么快？》

另外，推荐阅读 ⽂章，写的更加细致。

Kafka 的架构是怎么样的？

![image 1](assets/imageFile1.png)

Kafka 架构图 Kafka 的整体架构⾮常简单，是分布式架构，Producer、Broker 和Consumer 都可以有多个。

Producer，Consumer 实现 Kafka 注册的接⼝。

数据从 Producer 发送到 Broker 中，Broker 承担⼀个中间缓存和分发的作⽤。

Broker 分发注册到系统中的 Consumer。Broker 的作⽤类似于缓存，即活跃的数据和离线处理系统 之间的缓存。

客户端和服务器端的通信，是基于简单，⾼性能，且与编程语⾔⽆关的 TCP 协议。

⼏个重要的基本概念：

Topic：特指 Kafka 处理的消息源（feeds of messages）的不同分类。

Partition：Topic 物理上的分组（分区），⼀个 Topic 可以分为多个 Partition 。每个 Partition 都是 ⼀个有序的队列。Partition 中的每条消息都会被分配⼀个有序的 id（offset）。

replicas：Partition 的副本集，保障 Partition 的⾼可⽤。 leader：replicas 中的⼀个⻆⾊，Producer 和 Consumer 只跟 Leader 交 互。 follower：replicas 中的⼀个⻆⾊，从 leader 中复制数据，作为副本， ⼀旦 leader 挂掉，会从它的 followers 中选举出⼀个新的 leader 继续提 供服务。

Message：消息，是通信的基本单位，每个 Producer 可以向⼀个Topic（主题）发布⼀些消息。

Producers：消息和数据⽣产者，向 Kafka 的⼀个 Topic 发布消息的过程，叫做 producers 。

Consumers：消息和数据消费者，订阅 Topic ，并处理其发布的消息的过程，叫做 consumers 。

Consumer group：每个 Consumer 都属于⼀个 Consumer group，每条消息只 能被 Consumer group 中的⼀个 Consumer 消费，但可以被多个 Consumer group 消费。

Broker：缓存代理，Kafka 集群中的⼀台或多台服务器统称为 broker 。

Controller：Kafka 集群中，通过 Zookeeper 选举某个 Broker 作为 Controller ，⽤来进⾏ leader election 以及 各种 failover 。

ZooKeeper：Kafka 通过 ZooKeeper 来存储集群的 Topic、Partition 等元信息等。

😈 单纯⻆⾊来说，Kafka 和 RocketMQ 是基本⼀致的。⽐较明显的差异是：

RocketMQ 从 Kafka 演化⽽来。

- 1、Kafka 使⽤ Zookeeper 作为命名服务；RocketMQ ⾃⼰实现了⼀个轻量级的 Namesrv 。

- 2、Kafka Broker 的每个分区都有⼀个⾸领分区；RocketMQ 每个分区的“⾸领”分区，都在 Broker Master 节点上。

- 3、Kafka Consumer 使⽤ poll 的⽅式拉取消息；RocketMQ Consumer 提供 poll 的⽅式的同时，封 装了⼀个 push 的⽅式。


RocketMQ 没有⾸领分区⼀说，所以打上了引号。

RocketMQ 的 push 的⽅式，也是基于 poll 的⽅式的封装。

… 当然还有其它 …

🦅 Kafka 为什么要将 Topic 进⾏分区？

正如我们在 「聊聊 Kafka 的设计要点？」 问题中所看到的，是为了负载均衡，从⽽能够⽔平拓展。

Topic 只是逻辑概念，⾯向的是 Producer 和 Consumer ，⽽ Partition 则是物理概念。如果 Topic 不 进⾏分区，⽽将 Topic 内的消息存储于⼀个 Broker，那么关于该 Topic 的所有读写请求都将由这⼀ 个 Broker 处理，吞吐量很容易陷⼊瓶颈，这显然是不符合⾼吞吐量应⽤场景的。

有了 Partition 概念以后，假设⼀个 Topic 被分为 10 个 Partitions ，Kafka 会根据⼀定的算法将 10 个 Partition 尽可能均匀的分布到不同的 Broker（服务器）上。

当 Producer 发布消息时，Producer 客户端可以采⽤ random、key-hash 及轮询等算法选定⽬标 Partition ，若不指定，Kafka 也将根据⼀定算法将其置于某⼀分区上。

当 Consumer 拉取消息时，Consumer 客户端可以采⽤ Range、轮询 等算法分配 Partition ，从⽽ 从不同的 Broker 拉取对应的 Partition 的 leader 分区。

所以，Partiton 机制可以极⼤的提⾼吞吐量，并且使得系统具备良好的⽔平扩展能⼒。

## Kafka 的应⽤场景有哪些？

![image 2](assets/imageFile2.png)

Kafka 的应⽤场景

- 1）消息队列 ⽐起⼤多数的消息系统来说，Kafka 有更好的吞吐量，内置的分区，冗余及容错性，这让 Kafka 成为 了⼀个很好的⼤规模消息处理应⽤的解决⽅案。消息系统⼀般吞吐量相对较低，但是需要更⼩的端到 端延时，并常常依赖于 Kafka 提供的强⼤的持久性保障。在这个领域，Kafka ⾜以媲美传统消息系 统，如 ActiveMQ 或 RabbitMQ 。
- 2）⾏为跟踪


Kafka 的另⼀个应⽤场景，是跟踪⽤户浏览⻚⾯、搜索及其他⾏为，以发布订阅的模式实时记录到对应 的 Topic ⾥。那么这些结果被订阅者拿到后，就可以做进⼀步的实时处理，或实时监控，或放到 Hadoop / 离线数据仓库⾥处理。

- 3）元信息监控 作为操作记录的监控模块来使⽤，即汇集记录⼀些操作信息，可以理解为运维性质的数据监控吧。
- 4）⽇志收集 ⽇志收集⽅⾯，其实开源产品有很多，包括 Scribe、Apache Flume 。很多⼈使⽤ Kafka 代替⽇志聚 合（log aggregation）。⽇志聚合⼀般来说是从服务器上收集⽇志⽂件，然后放到⼀个集中的位置 （⽂件服务器或 HDFS）进⾏处理。 然⽽， Kafka 忽略掉⽂件的细节，将其更清晰地抽象成⼀个个⽇志或事件的消息流。这就让 Kafka 处 理过程延迟更低，更容易⽀持多数据源和分布式数据处理。⽐起以⽇志为中⼼的系统⽐如 Scribe 或者 Flume 来说，Kafka 提供同样⾼效的性能和因为复制导致的更⾼的耐⽤性保证，以及更低的端到端延 迟。
- 5）流处理 这个场景可能⽐较多，也很好理解。保存收集流数据，以提供之后对接的 Storm 或其他流式计算框架 进⾏处理。很多⽤户会将那些从原始 Topic 来的数据进⾏阶段性处理，汇总，扩充或者以其他的⽅式 转换到新的 Topic 下再继续后⾯的处理。 例如⼀个⽂章推荐的处理流程，可能是先从 RSS 数据源中抓取⽂章的内容，然后将其丢⼊⼀个叫做 “⽂章”的 Topic 中。后续操作可能是需要对这个内容进⾏清理，⽐如回复正常数据或者删除重复数据， 最后再将内容匹配的结果返还给⽤户。这就在⼀个独⽴的 Topic 之外，产⽣了⼀系列的实时数据处理 的流程。Strom 和 Samza 是⾮常著名的实现这种类型数据转换的框架。
- 6）事件源 事件源，是⼀种应⽤程序设计的⽅式。该⽅式的状态转移被记录为按时间顺序排序的记录序列。Kafka 可以存储⼤量的⽇志数据，这使得它成为⼀个对这种⽅式的应⽤来说绝佳的后台。⽐如动态汇总 （News feed）。
- 7）持久性⽇志（Commit Log） Kafka 可以为⼀种外部的持久性⽇志的分布式系统提供服务。这种⽇志可以在节点间备份数据，并为故 障节点数据回复提供⼀种重新同步的机制。Kafka 中⽇志压缩功能为这种⽤法提供了条件。在这种⽤法 中，Kafka 类似于 Apache BookKeeper 项⽬。 Kafka 消息发送和消费的简化流程是什么？


![image 3](assets/imageFile3.png)

Kafka 消息发送和消费

- 1、Producer ，根据指定的 partition ⽅法（round-robin、hash等），将消息发布到指定 Topic 的 Partition ⾥⾯。

- 2、Kafka 集群，接收到 Producer 发过来的消息后，将其持久化到硬盘，并保留消息指定时⻓（可 配置），⽽不关注消息是否被消费。

- 3、Consumer ，从 Kafka 集群 pull 数据，并控制获取消息的 offset 。⾄于消费的进度，可⼿动或 者⾃动提交给 Kafka 集群。


- 🦅 1）Producer 发送消息 Producer 采⽤ push 模式将消息发布到 Broker，每条消息都被 append 到 Patition 中，属于顺序写磁 盘（顺序写磁盘效率⽐随机写内存要⾼，保障 Kafka 吞吐率）。Producer 发送消息到 Broker 时，会 根据分区算法选择将其存储到哪⼀个 Partition 。 其路由机制为：

写⼊流程：

注意噢，Producer 只和 Partition 的 leader 进⾏交互。

- 🦅 2）Broker 存储消息


- 1、指定了 Partition ，则直接使⽤。

- 2、未指定 Partition 但指定 key ，通过对 key 进⾏ hash 选出⼀个 Partition 。

- 3、Partition 和 key 都未指定，使⽤轮询选出⼀个 Partition 。


- 1、Producer 先从 ZooKeeper 的 "/brokers/.../state" 节点找到该 Partition 的 leader 。

- 2、Producer 将消息发送给该 leader 。

- 3、leader 将消息写⼊本地 log 。

- 4、followers 从 leader pull 消息，写⼊本地 log 后 leader 发送 ACK 。

- 5、leader 收到所有 ISR 中的 replica 的 ACK 后，增加 HW（high watermark ，最后 commit 的 offset） 并向 Producer 发送 ACK 。


物理上把 Topic 分成⼀个或多个 Patition，每个 Patition 物理上对应⼀个⽂件夹（该⽂件夹存储该 Patition 的所有消息和索引⽂件）。

- 🦅 3）Consumer 消费消息 high-level Consumer API 提供了 consumer group 的语义，⼀个消息只能被 group 内的⼀个 Consumer 所消费，且 Consumer 消费消息时不关注 offset ，最后⼀个 offset 由 ZooKeeper 保存（下 次消费时，该 group 中的 Consumer 将从 offset 记录的位置开始消费）。 注意：


- 1、如果消费线程⼤于 Patition 数量，则有些线程将收不到消息。

- 2、如果 Patition 数量⼤于消费线程数，则有些线程多收到多个 Patition 的消息。

- 3、如果⼀个线程消费多个 Patition，则⽆法保证你收到的消息的顺序，⽽⼀个 Patition 内的消息是 有序的。


Consumer 采⽤ pull 模式从 Broker 中读取数据。

push 模式，很难适应消费速率不同的消费者，因为消息发送速率是由 Broker 决定的。它的⽬标是 尽可能以最快速度传递消息，但是这样很容易造成 Consumer 来不及处理消息，典型的表现就是拒 绝服务以及⽹络拥塞。⽽ pull 模式，则可以根据 Consumer 的消费能⼒以适当的速率消费消息。

对于 Kafka ⽽⾔，pull 模式更合适，它可简化 Broker 的设计，Consumer 可⾃主控制消费消息的速 率，同时 Consumer 可以⾃⼰控制消费⽅式——即可批量消费也可逐条消费，同时还能选择不同的 提交⽅式从⽽实现不同的传输语义。

🦅 Kafka Producer 有哪些发送模式？ Kafka 的发送模式由 Producer 端的配置参数 producer.type来设置。

这个参数指定了在后台线程中消息的发送⽅式是同步的还是异步的，默认是同步的⽅式， 即 producer.type=sync 。

如果设置成异步的模式，即 producer.type=async ，可以是 Producer 以 batch 的形式 push 数 据，这样会极⼤的提⾼ Broker的性能，但是这样会增加丢失数据的⻛险。

如果需要确保消息的可靠性，必须要将 producer.type设置为 sync 。

对于异步模式，还有 4 个配套的参数，如下：

![image 4](assets/imageFile4.png)

参数

以 batch 的⽅式推送数据可以极⼤的提⾼处理效率，Kafka Producer 可以将消息在内存中累计到⼀ 定数量后作为⼀个 batch 发送请求。batch 的数量⼤⼩可以通过 Producer 的参数 （batch.num.messages）控制。通过增加 batch 的⼤⼩，可以减少⽹络请求和磁盘 IO 的次数， 当然具体参数设置需要在效率和时效性⽅⾯做⼀个权衡。

在⽐较新的版本中还有 batch.size 这个参数。Producer 会尝试批量发送属于同⼀个 Partition 的 消息以减少请求的数量. 这样可以提升客户端和服务端的性能。默认⼤⼩是 16348 byte (16k).

发送到 Broker 的请求可以包含多个 batch ，每个 batch 的数据属于同⼀个 Partition 。

太⼩的 batch 会降低吞吐. 太⼤会浪费内存。 《芋道 Spring Boot 消息队列 Kafka ⼊⻔》

具体的代码实现，可以看看 的「3. 快速⼊⻔」和「4. 批量 发送消息」⼩节。 🦅 Kafka Consumer 是否可以消费指定的分区消息？ Consumer 消费消息时，向 Broker 发出“fetch”请求去消费特定分区的消息，Consumer 指定消息在⽇ 志中的偏移量(offset)，就可以消费从这个位置开始的消息，Consumer 拥有了 offset 的控制权，可以 向后回滚去重新消费之前的消息，这是很有意义的。 🦅 Kafka 的 high-level API 和 low-level API 的区别？ High Level API

屏蔽了每个 Topic 的每个 Partition 的 offset 管理（⾃动读取Zookeeper 中该 Consumer group 的 last offset）、Broker 失败转移、以及增减 Partition 时 Consumer 时的负载均衡（Kafka ⾃动进⾏ 负载均衡）。

如果 Consumer ⽐ Partition 多，是⼀种浪费。⼀个 Partition 上是不允许并发的，所以 Consumer 数不要⼤于 Partition 数。

Low Level API

# Low-level API 也就是 Simple Consumer API ，实际上⾮常复杂。

API 控制更灵活，例如消息重复读取，消息 offset 跳读，Exactly Once 原语。

API 更复杂，offset 不再透明，需要⾃⼰管理，Broker ⾃动失败转移需要处理，增加 Consumer、 Partition、Broker 需要⾃⼰做负载均衡。

Kafka 的⽹络模型是怎么样的？

Kafka 基于⾼吞吐率和效率考虑，并没有使⽤第三⽅⽹络框架，⽽且⾃⼰基于 Java NIO 封装的。

- 🦅 1）KafkaClient ，单线程 Selector 模型。


![image 5](assets/imageFile5.png)

KafkaClient

# 实际上，就是 NettyClient 的 NIO ⽅式。

单线程模式适⽤于并发链接数⼩，逻辑简单，数据量⼩。

在 Kafka 中，Consumer 和 Producer 都是使⽤的上⾯的单线程模式。这种模式不适合 Kafka 的服 务端，在服务端中请求处理过程⽐较复杂，会造成线程阻塞，⼀旦出现后续请求就会⽆法处理，会 造成⼤量请求超时，引起雪崩。⽽在服务器中应该充分利⽤多线程来处理执⾏逻辑。

- 🦅 2）KafkaServer ，多线程 Selector 模型。


# KafkaServer ，指的是 Kafka Broker 。

![image 6](assets/imageFile6.png)

KafkaServer

Broker 的内部处理流⽔线化，分为多个阶段来进⾏(SEDA)，以提⾼吞吐量和性能，尽量避免 Thead 盲等待，以下为过程说明。

实际上，就是 NettyServer 的 NIO ⽅式。

Accept Thread 负责与客户端建⽴连接链路，然后把 Socket 轮转交给Process Thread 。

相当于 Netty 的 Boss EventLoop 。

Process Thread 负责接收请求和响应数据，Process Thread 每次基于 Selector 事件循环，⾸先从 Response Queue 读取响应数据，向客户端回复响应，然后接收到客户端请求后，读取数据放⼊ Request Queue 。

相当于 Netty 的 Worker EventLoop 。

Work Thread 负责业务逻辑、IO 磁盘处理等，负责从 Request Queue 读取请求，并把处理结果放 ⼊ Response Queue 中，待 Process Thread 发送出去。

相当于业务线程池。

😈 实际上，艿艿的想法，如果⾃⼰实现 MQ ，完全可以直接使⽤ Netty 作为⽹络通信框架。包括， RocketMQ 就是如此实现的。 🦅 解释如何提⾼远程⽤户的吞吐量? 如果 Producer、Consumer 位于与 Broker 不同的数据中⼼，则可能需要调优套接⼝缓冲区⼤⼩，以对 ⻓⽹络延迟进⾏摊销。

Kafka 的数据存储模型是怎么样的?

Kafka 每个 Topic 下⾯的所有消息都是以 Partition 的⽅式分布式的存储在多个节点上。同时在 Kafka 的机器上，每个 Partition 其实都会对应⼀个⽇志⽬录，在⽬录下⾯会对应多个⽇志分段 （LogSegment）。

<table>
  <tr>
    <th>MacBok-Pro-5:test-0 yunai$ ls<br><br>0.index 0.timeindex leader-epochcheckpoint<br><br></th>
  </tr>
</table>


0.log 04.snapshot

Topic 为 test ，Partition 为 0 ，所以⽂件⽬录是 test-0 。

LogSegment ⽂件由两部分组成，分别为 .index ⽂件和 .log ⽂件，分别表示为 segment 索引⽂件 和数据⽂件。这两个⽂件的命令规则为：Partition 全局的第⼀个 segment 从 0 开始，后续每个 segment ⽂件名为上⼀个 segment ⽂件最后⼀条消息的 offset 值，数值⼤⼩为 64 位，20 位数字字符 ⻓度，没有数字⽤ 0 填充，如下，假设有 1000 条消息，每个 LogSegment ⼤⼩为 100 ，下⾯展现了 900-1000 的 .index 和 .log ⽂件：

.index 和 .log ⽂件

由于 Kafka 消息数据太⼤，如果全部建⽴索引，即占了空间⼜增加了耗时，所以 Kafka 选择了稀疏 索引的⽅式（通过 .index 索引 .log ⽂件），这样的话索引可以直接进⼊内存，加快偏查询速 度。

🦅 简单介绍⼀下如何读取数据？ 如果我们要读取第 911 条数据。

⾸先第⼀步，找到它是属于哪⼀段的，根据⼆分法查找到他属于的⽂件，找 到 0000900.index 和 00000900.log 之后。

然后，去 .index 中去查找 (911-900) =11 这个索引或者⼩于 11 最近的索引，在这⾥通过⼆分 法我们找到了索引是 [10,1367] 。

# 10 表示，第 10 条消息开始。 1367 表示，在 .log 的第 1367 字节开始。

# 😈 所以，本图的第 911 条的“1360”是错的，相⽐“1367” 反倒⼩了。

然后，我们通过这条索引的物理位置 1367 ，开始往后找，直到找到 911 条数据。

上⾯讲的是如果要找某个 offset 的流程，但是我们⼤多数时候并不需要查找某个 offset ，只需要按照 顺序读即可。⽽在顺序读中，操作系统会对内存和磁盘之间添加 page cahe ，也就是我们平常⻅到的 预读操作，所以我们的顺序读操作时速度很快。但是 Kafka 有个问题，如果分区过多，那么⽇志分段 也会很多，写的时候由于是批量写，其实就会变成随机写了，随机 I/O 这个时候对性能影响很⼤。所 以⼀般来说 Kafka 不能有太多的Partition 。针对这⼀点，RocketMQ 把所有的⽇志都写在⼀个⽂件⾥ ⾯，就能变成顺序写，通过⼀定优化，读也能接近于顺序读。

# 并且，截⽌到 RocketMQ4 版本，索引⽂件，对每个数据⽂件中的消息，都 有对应的索引。这个是和 Kafka 的稀疏索引不太⼀样的地⽅。

《Kafka 之数据存储》

更详尽的，推荐阅读 ⽂章。 🦅 为什么不能以 Partition 作为存储单位？ 如果就以 Partition 为最⼩存储单位，可以想象，当 Kafka Producer 不断发送消息，必然会引起 Partition ⽂件的⽆限扩张，将对消息⽂件的维护以及已消费的消息的清理带来严重的影响，因此，需 以 segment 为单位将 Partition 进⼀步细分。 每个 Partition（⽬录）相当于⼀个巨型⽂件，被平均分配到多个⼤⼩相等的 segment（段）数据⽂件 中（每个 segment ⽂件中消息数量不⼀定相等），这种特性也⽅便 old segment 的删除，即⽅便已被 消费的消息的清理，提⾼磁盘的利⽤率。每个 Partition 只需要⽀持顺序读写就⾏，segment 的⽂件⽣ 命周期由服务端配置参数（log.segment.bytes，log.roll.{ms,hours} 等若⼲参数）决定。

Kafka 的消息格式是怎么样的？

message 中的物理结构为：

![image 7](assets/imageFile7.png)

message 物理结构 参数说明：

<table>
  <tr>
    <th>关键字</th>
    <th>解释说明</th>
  </tr>
  <tr>
    <td>8 byte ofset</td>
    <td>在parition(分区)内的每条消息都有⼀个有序的id 号，这个id号被称为偏移(ofset),它可以唯⼀确定 每条消息在parition(分区)内的位置。即ofset表</td>
  </tr>
  <tr>
    <td>4 byte mesage size</td>
    <td>示partion的第多少mesage mesage⼤⼩</td>
  </tr>
  <tr>
    <td>4 byte CRC32</td>
    <td>⽤crc32校验mesage</td>
  </tr>
  <tr>
    <td>1 byte “magic”</td>
    <td>表示本次发布Kafka服务程序协议版本号</td>
  </tr>
  <tr>
    <td>1 byte “atributes”</td>
    <td>表示为独⽴版本、或标识压缩类型、或编码类型</td>
  </tr>
  <tr>
    <td>4 byte key length</td>
    <td>表示key的⻓度,当key为-1时，K byte key字段不 填</td>
  </tr>
  <tr>
    <td>K byte key</td>
    <td>可选</td>
  </tr>
  <tr>
    <td> </td>
    <td>表示实际消息数据</td>
  </tr>
</table>


value bytes payload

不过，这是早期 Kafka 的版本，最新版本的格式，推荐阅读如下两篇⽂章：

《⼀⽂看懂 Kafka 消息格式的演变》

《Kafka 消息格式中的变⻓字段（Varints）》

当然，看懂这个数据格式，基本也能知道消息的⼤体格式。

Kafka 的副本机制是怎么样的？

Kafka 的副本机制，是多个 Broker 节点对其他节点的 Topic 分区的⽇志进⾏复制。当集群中的某个节

点出现故障，访问故障节点的请求会被转移到其他正常节点(这⼀过程通常叫 Reblance)，Kafka 每个 主题的每个分区都有⼀个主副本以及 0 个或者多个副本，副本保持和主副本的数据同步，当主副本出 故障时就会被替代。

![image 8](assets/imageFile8.png)

副本机制

# 注意哈，下⾯说的 Leader 指的是每个 Topic 的某个分区的 Leader ，⽽不是 Broker 集群中的【集群控制器】。

在 Kafka 中并不是所有的副本都能被拿来替代主副本，所以在 Kafka 的Leader 节点中维护着⼀个 ISR （In sync Replicas）集合，翻译过来也叫正在同步中集合，在这个集合中的需要满⾜两个条件:

- 1、节点必须和 Zookeeper 保持连接。

- 2、在同步的过程中这个副本不能落后主副本太多。


另外还有个 AR（Assigned Replicas）⽤来标识副本的全集，OSR ⽤来表示由于落后被剔除的副本集 合，所以公式如下：

ISR = Leader + 没有落后太多的副本。

AR = OSR + ISR 。

这⾥先要说下两个名词：HW 和 LEO 。

HW（⾼⽔位 HighWatermark），是 Consumer 能够看到的此 Partition 的位置。

LEO（logEndOffset），是每个 Partition 的 log 最后⼀条 Message 的位置。

HW 能保证 Leader 所在的 Broker 失效，该消息仍然可以从新选举的Leader 中获取，不会造成消息 丢失。

当 Producer 向 Leader 发送数据时，可以通过request.required.acks 参数来设置数据可靠性的 级别：

1（默认）：这意味着 Producer 在 ISR 中的 Leader 已成功收到的数据并得到确认后发送下⼀条 message 。如果 Leader 宕机了，则会丢失数据。

0：这意味着 Producer ⽆需等待来⾃ Broker 的确认⽽继续发送下⼀批消息。这种情况下数据传输 效率最⾼，但是数据可靠性确是最低的。

-1：Producer 需要等待 ISR 中的所有 Follower 都确认接收到数据后才算⼀次发送完成，可靠性最 ⾼。但是这样也不能保证数据不丢失，⽐如当 ISR 中只有 Leader 时(其他节点都和 Zookeeper 断 开连接，或者都没追上)，这样就变成了 acks=1 的情况。

关于这块详详细的内容，推荐阅读

《Kafka 数据可靠性深度解读》 的 「3 ⾼可靠性存储分析」 ⼩节

《Kafka 集群内复制功能深⼊剖析》

ZooKeeper 在 Kafka 中起到什么作⽤？

关于 ZooKeeper 是什么，不了解的胖友，直接去看 。 在基于 Kafka 的分布式消息队列中，ZooKeeper 的作⽤有：

《精尽 Zookeeper ⾯试题》

- 1、Broker 在 ZooKeeper 中的注册。

- 2、Topic 在 ZooKeeper 中的注册。

- 3、Consumer 在 ZooKeeper 中的注册。

- 4、Producer 负载均衡。

- 5、Consumer 负载均衡。

- 6、记录消费进度 Offset 。

- 7、记录 Partition 与 Consumer 的关系。


主要指的是，Producer 从 Zookeeper 拉取 Topic 元数据，从⽽能够将消息发 送负载均衡到对应 Topic 的分区中。

Kafka 已推荐将 consumer 的 Offset 信息保存在 Kafka 内部的 Topic 中。

其实，总结起来，就是两类功能：

Broker、Producer、Consumer 和 Zookeeper 的交互。

对应 1、2、3、5 。

相应的状态存储到 Zookeeper 中。

对应 4、6、7 。

详细的每⼀点，看 的 「Kafka 架构中 ZooKeeper 以怎样的形式存在？」 ⼩节。

《再谈基于 Kafka 和 ZooKeeper 的分布式消息队列原理》

Kafka 如何实现⾼可⽤？

在 「Kafka 的架构是怎么样的？」 问题中，已经基本回答了这个问题。

Kafka 集群

Zookeeper 部署 2N+1 节点，形成 Zookeeper 集群，保证⾼可⽤。

Kafka Broker 部署集群。每个 Topic 的 Partition ，基于【副本机制】，在 Broker 集群中复制，形 成 replica 副本，保证消息存储的可靠性。每个 replica 副本，都会选择出⼀个 leader 分区 （Partition），提供给客户端（Producer 和 Consumer）进⾏读写。

Kafka Producer ⽆需考虑集群，因为和业务服务部署在⼀起。Producer 从 Zookeeper 拉取到 Topic 的元数据后，选择对应的 Topic 的 leader 分区，进⾏消息发送写⼊。⽽ Broker 根据 Producer 的 request.required.acks 配置，是写⼊⾃⼰完成就响应给 Producer 成功，还是写⼊所有 Broker 完成再响应。这个，就是胖友⾃⼰对消息的可靠性的选择。

Kafka Consumer 部署集群。每个 Consumer 分配其对应的 Topic Partition ，根据对应的分配策 略。并且，Consumer 只从 leader 分区（Partition）拉取消息。另外，当有新的 Consumer 加⼊或 者⽼的 Consumer 离开，都会将 Topic Partition 再均衡，重新分配给 Consumer 。

# 注意噢，此处说的都是同⼀个 Kafka Consumer group 。

总的来说，Kafka 和 RocketMQ 的⾼可⽤⽅式是⽐较类似的，主要的差异在 Kafka Broker 的副本机 制，和 RocketMQ Broker 的主从复制，两者的差异，以及差异带来的⽣产和消费不同。😈 当然，实际 上，都是和“主” Broker 做消息的发送和读取不是？！

什么是 Kafka 事务？

《Kafka 事务简介》

推荐阅读 ⽂章。 😈 和想象中的，是不是有点差别？！

- 具体的代码实现，可以看看 的「11. 事务消息」⼩节。 Kafka 是否会弄丢数据？ 艿艿：注意，Kafka 是否会丢数据，主要取决于我们如何使⽤。这点，⾮ 常重要噢。

🦅 消费端弄丢了数据？ 唯⼀可能导致消费者弄丢数据的情况，就是说，你消费到了这个消息，然后消费者那边⾃动提交了 offset ，让 Broker 以为你已经消费好了这个消息，但其实你才刚准备处理这个消息，你还没处理，你 ⾃⼰就挂了，此时这条消息就丢咯。 这不是跟 RabbitMQ 差不多吗，⼤家都知道 Kafka 会⾃动提交 offset ，那么只要关闭⾃动提交 offset ，在处理完之后⾃⼰⼿动提交 offset ，就可以保证数据不会丢。但是此时确实还是可能会有重复消 费，⽐如你刚处理完，还没提交 offset ，结果⾃⼰挂了，此时肯定会重复消费⼀次，⾃⼰保证幂等性 就好了。

RocketMQ push 模式下，在确认消息被消费完成，才会提交 Offset 给 Broker 。

⽣产环境碰到的⼀个问题，就是说我们的 Kafka 消费者消费到了数据之后是写到⼀个内存的 queue ⾥ 先缓冲⼀下，结果有的时候，你刚把消息写⼊内存 queue ，然后消费者会⾃动提交 offset 。然后此时 我们重启了系统，就会导致内存 queue ⾥还没来得及处理的数据就丢失了。

- 具体的代码实现，可以看看 的「12. 消费进度的提交机制」 ⼩节。 🦅 Broker 弄丢了数据？


《芋道 Spring Boot 消息队列 Kafka ⼊⻔》

《芋道 Spring Boot 消息队列 Kafka ⼊⻔》

这块⽐较常⻅的⼀个场景，就是 Kafka 某个 Broker 宕机，然后重新选举 Partition 的 leader。⼤家想 想，要是此时其他的 follower 刚好还有些数据没有同步，结果此时 leader 挂了，然后选举某个 follower 成 leader 之后，不就少了⼀些数据？这就丢了⼀些数据啊。 ⽣产环境也遇到过，我们也是，之前 Partition 的 leader 机器宕机了，将 follower 切换为 leader 之 后，就会发现说这个数据就丢了。 所以此时⼀般是要求起码设置如下 4 个参数：

给 Topic 设置 replication.factor 参数：这个值必须⼤于 1，要求每个 partition 必须有⾄少 2 个副本。

在 Kafka 服务端设置 min.insync.replicas 参数：这个值必须⼤于 1 ，这个是要求⼀个 leader ⾄少感知到有⾄少⼀个 follower 还跟⾃⼰保持联系，没掉队，这样才能确保 leader 挂了还有⼀个 follower 吧。

在 Producer 端设置 acks=all：这个是要求每条数据，必须是写⼊所有 replica 之后，才能认为是 写成功了。

不过这个也不⼀定能够绝对保证，例如说，Broker 集群⾥，所有节点都挂 了，只剩下⼀个节点。此时，acks=all 和 acks=1 就等价了。当然，也 可以通过设置 min.insync.replics 参数，每次写⼊要求最⼩的同步副 本数。 这块也和朋友交流了下，他们⾦融场景下，acks=all 也是这么配置的。 原因嘛，因为他们是⾦融场景呀。

在 Producer 端设置 retries=MAX（很⼤很⼤很⼤的⼀个值，⽆限次重试的意思）：这个是要求⼀ 旦写⼊失败，就⽆限重试，卡在这⾥了。

我们⽣产环境就是按照上述要求配置的，这样配置之后，⾄少在 Kafka broker 端就可以保证在 leader 所在 Broker 发⽣故障，进⾏ leader 切换时，数据不会丢失。 🦅 ⽣产者会不会弄丢数据？ 如果按照上述的思路设置了 acks=all ，⼀定不会丢，要求是，你的 leader 接收到消息，所有的 follower 都同步到了消息之后，才认为本次写成功了。如果没满⾜这个条件，⽣产者会⾃动不断的重 试，重试⽆限次。

关于 Kafka Producer 重试发送消息的逻辑的源码解析，可以看看 《Kafka 重试机制解读》 。

《360 度测试：KAFKA 会丢数据么？其⾼可⽤是否满⾜需求？》

😈 另外，在推荐⼀篇⽂章 ，提供了 ⼀些测试示例。 关于这⼀块，可以重点看看 的 「6.4 在可靠的系统⾥使⽤⽣产者」 和 「6.5 在可 靠的系统⾥使⽤消费者」 ⼩节。

《Kafka 权威指南》

Kafka 如何保证消息的顺序性？

Kafka 本身，并不像 RocketMQ ⼀样，提供顺序性的消息。所以，提供的⽅案，都是相对有损的。如 下：

这⾥的顺序消息，我们更多指的是，单个 Partition 的消息，被顺序消费。

⽅式⼀，Consumer ，对每个 Partition 内部单线程消费，单线程吞吐量太低，⼀般不会⽤这个。

⽅式⼆，Consumer ，拉取到消息后，写到 N 个内存 queue，具有相同 key 的数据都到同⼀个内存 queue 。然后，对于 N 个线程，每个线程分别消费⼀个内存 queue 即可，这样就能保证顺序性。

这种⽅式，相当于对【⽅式⼀】的改进，将相同 Partition 的消息进⼀步拆 分，保证相同 key 的数据消费是顺序的。 不过这种⽅式，消费进度的更新会⽐较麻烦。

当然，实际情况也不太需要考虑消息的顺序性，基本没有业务需要。 具体的代码实现，可以看看 的「10. 顺序消息」⼩节。

《芋道 Spring Boot 消息队列 Kafka ⼊⻔》

666. 彩蛋

😈 略显仓促的⼀篇⽂章，后续会重新在梳理⼀下。如果胖友对 Kafka 有什么疑惑，⼀定要在星球⾥提 出，我们⼀起在讨论和解答⼀波，然后整理到这篇⽂章中。 同时，期待下厮⼤的 Kafka 新书。 参考与推荐如下⽂章：

《⾼并发⾯试必问：分布式消息系统 Kafka 简介》

《14 个最常⻅的 Kafka ⾯试题及答案》

这篇博客，有点傻逼。。。。

《你需要知道的 Kafka》

《Kafka 内部⽹络框架模型分析》

《再谈基于 Kafka 和 ZooKeeper 的分布式消息队列原理》

《如何保证消息的可靠性传输？（如何处理消息丢失的问题）》
