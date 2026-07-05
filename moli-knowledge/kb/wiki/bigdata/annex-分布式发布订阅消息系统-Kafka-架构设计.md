---
title: 分布式发布订阅消息系统 Kafka 架构设计.note（原文插图 annex）
slug: annex-分布式发布订阅消息系统-Kafka-架构设计
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/kafka/分布式发布订阅消息系统 Kafka 架构设计.note.md
related: [kafka-大数据管道]
created: 2026-07-05
updated: 2026-07-05
---

Consumer state In Kafka, the consumers are responsible for maintaining state information (ofset) on what has ben consumed. Typicaly, the Kafka consumer library writes their state data to zokeper. Hoever, it may be beneficial for consumers to write state data into the same datastore where they are writing the results of their procesing. For example, the consumer may simply be entering some agregate value into a centralized transactional OLTP database. In this case the consumer can store the state of what is consumed in the same transaction as the database modification. This solves a distributed consensus problem, by removing the distributed part! A similar trick works for some non-transactional systems as wel. A search system can store its consumer state with its index segments. Though it may provide no durability guarantes, this means that the index is always in sync with the consumer state: if an unflushed index segment is lost in a crash, the indexes can always resume consumption from the latest checkpointed ofset. Likewise our Hadop load job which does paralel loads from Kafka, does a similar trick. Individual mapers write the ofset of the last consumed mesage to HDFS at the end of the map task. If a job fails and gets restarted, each maper simply restarts from the ofsets stored in HDFS. There is a side benefit of this decision. A

译者信息 使⽤者的状态 在Kafka中，由使⽤者负责维护反映哪些消息

已被使⽤的状态信息（偏移量）。典型情况下， Kafka使⽤者的library会把状态数据保存到 Zokeper之中。然⽽，让使⽤者将状态信息保 存到保存它们的消息处理结果的那个数据存储 （datastore）中也许会更佳。例如，使⽤者也许 就是要把⼀些统计值存储到集中式事物OLTP数据 库中，在这种情况下，使⽤者可以在进⾏那个数 据库数据更改的同⼀个事务中将消息使⽤状态信 息存储起来。这样就消除了分布式的部分，从⽽ 解决了分布式中的⼀致性问题！这在⾮事务性系 统中也有类似的技巧可⽤。搜索系统可⽤将使⽤ 者状态信息同它的索引段（index segment）存储 到⼀起。尽管这么做可能⽆法保证数据的持久性 （durability），但却可⽤让索引同使⽤者状态信 息保存同步：如果由于宕机造成有⼀些没有刷新 到磁盘的索引段信息丢了，我们总是可⽤从上次 建⽴检查点（checkpoint）的偏移量处继续对索 引进⾏处理。与此类似，Hadop的加载作业 （load job）从Kafka中并⾏加载，也有相同的技 巧可⽤。每个Maper在map任务结束前，将它使 ⽤的最后⼀个消息的偏移量存⼊HDFS。

这个决策还带来⼀个额外的好处。使⽤者可⽤ 故意回退（rewind）到以前的偏移量处，再次使 ⽤⼀遍以前使⽤过的数据。虽然这么做违背了队 列的⼀般协约（contract），但对很多使⽤者来 讲却是个很基本的功能。举个例⼦，如果使⽤者 的代码⾥有个Bug，⽽且是在它处理完⼀些消息 之后才被发现的，那么当把Bug改正后，使⽤者 还有机会重新处理⼀遍那些消息。

consumer can deliberately rewind back to an old ofset and re-consume data. This violates the comon contract of a queue, but turns out to be an esential feature for many consumers. For example, if the consumer code has a bug and is discovered after some mesages are consumed, the consumer can re-consume those mesages once the bug is fixed.

<table>
  <tr>
    <th>Push vs. pul A related question is whether consumers<br><br>should pul data from brokers or brokers should push data to the subscriber. In this respect Kafka folows a more traditional design, shared by most mesaging systems, where data is pushed to the broker from the producer and puled from the broker by the consumer. Some recent systems, such as scribe and flu<br><br>cusin log agregation, folow a very diferent push based path where each node acts as a broker and data is pushed downstream. There are pros and cons to both aproaches. However a push-based system has dificulty dealing with diverse consumers as the broker controls the rate at which data is transfered. The goal, is generaly for the consumer to be able to consume at the maximum posible rate; unfortunately in a push system this means the consumer tends to be overwhelmed when its rate of consumption fals below the rate of production (a denial of service atack, in esence). A pul-based system has the nicer property that the consumer simply fals behind and catches up when it can. This can be mitigated with some kind of backof protocol by which the consumer can indicate it is overwhelmed, but geting the rate of transfer to fuly utilize (but never overutilize) the consumer is trickier than it sems. Previous atempts at building systems in this fashion led us to go with a more traditional pul model.<br><br>me, fo g on</th>
    <th>译者信息 Push和Pul 相关问题还有⼀个，就是到底是应该让使<br><br>⽤者从代理那⾥吧数据Pul（拉）回来还是应该让 代理把数据Push（推）给使⽤者。和⼤部分消息 系统⼀样，Kafka在这⽅⾯遵循了⼀种更加传统的 设计思路：由⽣产者将数据Push给代理，然后由 使⽤者将数据代理那⾥Pul回来。近来有些系统， ⽐如scribe和flume，更着重于⽇志统计功能，遵 循了⼀种⾮常不同的基于Push的设计思路，其中 每个节点都可以作为代理，数据⼀直都是向下游 Push的。上述两种⽅法都各有优缺点。然⽽，因 为基于Push的系统中代理控制着数据的传输速 率，因此它难以应付⼤量不同种类的使⽤者。我 们的设计⽬标是，让使⽤者能以它最⼤的速率使 ⽤数据。不幸的是，在Push系统中当数据的使⽤ 速率低于产⽣的速率时，使⽤者往往会处于超载 状态（这实际上就是⼀种拒绝服务攻击）。基于 Pul的系统在使⽤者的处理速度稍稍落后的情况下 会表现更佳，⽽且还可以让使⽤者在有能⼒的时 候往往前赶赶。让使⽤者采⽤某种退避协议 （backof protocol）向代理表明⾃⼰处于超载状 态，可以解决部分问题，但是，将传输速率调整 到正好可以完全利⽤（但从不能过度利⽤）使⽤ 者的处理能⼒可⽐初看上去难多了。以前我们尝 试过多次，想按这种⽅式构建系统，得到的经验<br><br>模型。</th>
  </tr>
  <tr>
    <td>Distribution Kafkaisbuilt to be run acros a cluster of<br><br>machines as the comon case. There is no central "master" node. Brokers are pers to each other and can be aded and removed at anytime without any manual configuration changes. Similarly, producers and consumers can be started dynamicaly at any time. Each broker registers some metadata (e.g., available topics) in Zokeper. Producers and consumers can use Zokeper to discover topics and to co-ordinate the production and consuption. The details of producers and</td>
    <td>教训使得我们选择了更加常规的Pul<br><br>译者信息 分发<br><br>Kafka通常情况下是运⾏在集群中的服务器上。 没有中央的“主”节点。代理彼此之间是对等的， 不需要任何⼿动配置即可可随时添加和删除。同 样，⽣产者和消费者可以在任何时候开启。 每个 代理都可以在Zokeper(分布式协调系统)中注册 的⼀些元数据（例如，可⽤的主题）。⽣产者和 消费者可以使⽤Zokeper发现主题和相互协 调。关于⽣产者和消费者的细节将在下⾯描述。</td>
  </tr>
</table>


#### consumers wil be described below.

<table>
  <tr>
    <th>Producer Automatic producer load balancing Kafkasupports client-side load balancing for mesage producers or use of a<br><br>dedicated load balancer to balance TCP conections. A dedicated layer-4 load balancer works by balancing TCP conections over Kafka brokers. In this configuration al mesages from a given producer go to a single roker. The advantage of using a level-4 load balancer is that each producer only neds a single TCP conection, and no conection to zokeper is neded. The disadvantage is that the balancing is done at the TCP conection level, and hence it may not be wel balanced (if some producers produce many more mesages than others, evenly dividing up the conections per broker may not result in evenly dividing up</th>
    <th>译者信息 ⽣产者<br><br>⽣产者⾃动负载均衡 对于⽣产者，Kafka⽀持客户端负载均衡，也可<br><br>以使⽤⼀个专⽤的负载均衡器对TCP连接进⾏负 载均衡调整。专⽤的第四层负载均衡器在Kafka代 理之上对TCP连接进⾏负载均衡。在这种配置的 情况，⼀个给定的⽣产者所发送的消息都会发送 给⼀个单个的代理。使⽤第四层负载均衡器的好 处是，每个⽣产者仅需⼀个单个的TCP连接⽽⽆ 须同Zokeper建⽴任何连接。不好的地⽅在于 所有均衡⼯作都是在TCP连接的层次完成的，因 ⽽均衡效果可能并不佳（如果有些⽣产者产⽣的 消息远多于其它⽣产者，按每个代理对TCP连接 进⾏平均分配可能会导致每个代理接收到的消息 总数并不平均）。</th>
  </tr>
  <tr>
    <td>the mesages per broker).<br><br>Client-side zokeper-based load balancing solves some of these problems. It alows the producer to dynamicaly discover new brokers, and balance load on a per-request basis. Likewise it alows the producer to partition data acording to some key instead of randomly, which enables stickines on the consumer (e.g. partitioning data consumption by user id). This feature is caled "semantic partitioning", and is described in more detail below. The working of the zokeper-based load balancing is described below. Zokeper watchers are registered on the folowing events<br><br>new broker comes up broker goedown new topic is registered<br><br>a broker gets registered for an existing topic Internaly, the producer maintains an elastic pol of conections tohe brokers, one per broker. This pol is kept updated to establish/maintain conections to al the live brokers, through the zokeper watcher calbacks. When a producer request for a particular topic comes in, a broker partition is picked by the partitioner (se section on semantic partitioning). The available producer conection is used from the pol to send the</td>
    <td>译者信息 采⽤客户端基于zokeper的负载均衡可以解决部分 问题。如果这么做就能让⽣产者动态地发现新的 代理，并按请求数量进⾏负载均衡。类似的，它 还能让⽣产者按照某些键值（key）对数据进⾏分 区（partition）⽽不是随机乱分，因⽽可以保存 同使⽤者的关联关系（例如，按照⽤户id对数据 使⽤进⾏分区）。这种分法叫做“语义分区” （semantic partitioning），下⽂再讨论其细节。 下⾯讲解基于zokeper的负载均衡的⼯作原理。在 发⽣下列事件时要对zokeper的监视器 （watcher）进⾏注册： 加⼊了新的代理 有⼀个代理下线了 注册了新的话题 代理注册了已有话题。 ⽣产者在其内部为每⼀个代理维护了⼀个弹性的连接 （同代理建⽴的连接）池。通过使⽤zokeper 监视器的回调函数（calback），该连接池在建 ⽴/保持同所有在线代理的连接时都要进⾏更新。 当⽣产者要求进⼊某特定话题时，由分区者 （partitioner）选择⼀个代理分区（参加语义分区 ⼩结）。从连接池中找出可⽤的⽣产者连接，并 通过它将数据发送到刚才所选的代理分区。<br><br></td>
  </tr>
</table>


#### data to the selected broker partition.

<table>
  <tr>
    <th>sn onossend Asynchronous non-blocking operations are fundamental to scaling mesaging systems. In Kafka, the producer provides an option to use asynchronous dispatch of produce requests (producer.type=async). This alows bufering of produce requests in a in-memor queue and batch sends that are tri gered by a time interval or a pre-configured batch size. Since data is typicaly published from set of heterogenous machines prducing data at variable rates, this asynchronous bufering helps enerate uniform trafic to the brokers, leading to beter network utilization and higher throughput.</th>
    <th>译者信息 异步发送<br><br>对于可伸缩的消息系统⽽⾔，异步⾮阻塞式操作 是不可或缺的。在Kafka中，⽣产者有个选项 （producer.type=async）可⽤指定使⽤异步分发 出产请求（produce request）。这样就允许⽤⼀ 个内存队列（in-memory queue）把⽣产请求放 ⼊缓冲区，然后再以某个时间间隔或者事先配置 好的批量⼤⼩将数据批量发送出去。因为⼀般来 说数据会从⼀组以不同的数据速度⽣产数据的异 构的机器中发布出，所以对于代理⽽⾔，这种异 步缓冲的⽅式有助于产⽣均匀⼀致的流量，因⽽ 会有更佳的⽹络利⽤率和更⾼的吞吐量。</th>
  </tr>
  <tr>
    <td>Semantic partitioning Consider an aplication that would like to<br><br>maintain an agregtion of the number of profile visitors for each member. It would like to send al profile visit events for a member to a particular partition and, hence, have al updates for a member to apear in the same stream for the same consumer thread. The producer has the capability to be able to semanticaly map mesages to the available kafka nods and partitions. This alows partitioning the stream of mesages with some semantic partition function based on some key in the mesage to spread them over broker machines. The<br><br>artitioning function can be customized by providing an implementation of the kafka.producer.Partitioner interface, default being the random partitioner. Fo the example above, the key would be member_id and the prtitioning function would be</td>
    <td>译者信息 语义分区<br><br>下⾯看看⼀个想要为每个成员统计⼀个个⼈空间 访客总数的程序该怎么做。应该把⼀个成员的所 有个⼈空间访问事件发送给某特定分区，因此就 可以把对⼀个成员的所有更新都放在同⼀个使⽤ 者线程中的同⼀个事件流中。⽣产者具有从语义 上将消息映射到有效的Kafka节点和分区之上的能 ⼒。这样就可以⽤⼀个语义分区函数将消息流按 照消息中的某个键值进⾏分区，并将不同分区发 送给各⾃相应的代理。通过实现 kafak.producer.Partitioner接⼝，可以对分区函数 进⾏定制。在缺省情况下使⽤的是随即分区函 数。上例中，那个键值应该是member_id，分区 函数可以是hash(member_id)%num_partitions。</td>
  </tr>
</table>


#### hash(member_id)%num_partitions.

<table>
  <tr>
    <th>SuportforHadopandother batchdataload Scalablepersistencealowsfor theposibility of suporting batch data<br><br>loads that periodicaly snapshot data into an ofline system for batch procesing. We make use of this for loading data into our data warehouse and Hadop clusters.<br><br>Batchprocesinghapensin stagesbegi ning with the data load stage and proceding in an acyclic graph of procesing and output stages (e.g. as suported here). An esential feature of suport for this model is the ability to re-run the data load from a point in time (in case anything goes wrong). InthecaseofHadopwe paralelizethedata load by spliting<br><br>the load over individual map tasks, one for each node/topic/partition combination, alowing ful paralelism in the loading. Hadop provides the task management, and tasks which fail can restart without danger of duplicate data.</th>
    <th>译者信息 对Hadoop以及其它批量数据装载的 ⽀持 具有伸缩性的持久化⽅案使得Kafka可 ⽀持批量数据装载，能够周期性将快照数 据载⼊进⾏批量处理的离线系统。我们利⽤这个 功能将数据载⼊我们的数据仓库（data warehouse）和Hadop集群。 批量处理始于数据载⼊阶段，然后进⼊ ⾮循环图（acyclic graph）处理过程以及<br><br>输出阶段（⽀持情况在这⾥）。⽀持这种处理模 型的⼀个重要特性是，要有重新装载从某个时间 点开始的数据的能⼒（以防处理中有任何错误发 ⽣）。<br><br>对于Hadop，我们通过在单个的 map任务之上分割装载任务对数据的装载<br><br>进⾏了并⾏化处理，分割时，所有节点/话题/分区 的每种组合都要分出⼀个来。Hadop提供了任务 管理，失败的任务可以重头再来，不存在数据被 重复的危险。</th>
  </tr>
  <tr>
    <td>Implementation Details The following gives a<br><br>brief description of some relevant lower-level implementation details for some parts of the system described in the above section.<br><br>API Design Producer APIs The Producer API that wraps the 2 low-level producers -<br><br>kafka.producer.SyncProducerandkafka.produce r.async.AsyncProducer.<br><br>clas Producer {</td>
    <td>译者信息 实施细则<br><br>下⾯给出了⼀些在上⼀节所描述的低层相关 的实现系统的某些部分的细节的简要说明。 API 设计 ⽣产者APIs ⽣产者API 是给两个底层⽣产者的再封装kafka.producer.SyncProducerandkafka.pro ducer.async.AsyncProducer. class Producer { /*Sends the data, partitioned by key to<br><br>the topicusing either the */<br><br>/*synchronous or the asynchronous<br><br>producer */</td>
  </tr>
</table>


<table>
  <tr>
    <th>/* Sends the data, partitioned by key to the topic using either the<br><br>*/<br><br>/* synchronous or the asynchronous producer */ public void<br><br>send(kafka.javapi.producer.Produc<br><br>erData producerData);<br><br>/* Sends a list of data,<br><br>partitioned by key to the topic using either */<br><br>/* the synchronous or the asynchronous producer */<br><br>public void<br><br>send(java.util.List< kafka.javapi.producer.ProducerData> producerData);<br><br>/* Closes the producer and cleans up */<br><br>public void close();</th>
    <th>public void<br><br>send(kafka.javapi.producer.ProducerData<br><br>producerData);<br><br>/*Sends a list of data, partitioned by key<br><br>to thetopic using either */<br><br>/*the synchronous or the asynchronous<br><br>producer*/<br><br>public void send(java.util.List<<br><br>kafka.javapi.producer.ProducerData><br><br>producerData);<br><br>/*Closes the producer and cleans up */ public void close(); }<br><br></th>
  </tr>
  <tr>
    <td>}<br><br>The goal is to expose al the producer functionality through a single API to the client. The new producer can handle ueueing/bufering of multiple producer requests and asynchronous dispatch of the batched data -</td>
    <td>译者信息 该API的⽬的是将⽣产者的所有功能通过⼀个单个 的API公开给其使⽤者（client）。新建的⽣产者 可以：<br><br></td>
  </tr>
</table>


kafka.producer.Producerprovides the ability to batch multiple produce requests (producer.type=async), before serializing and dispatching them to the apropriate kafka broker partition. The size of the batch can be controled by a few config parameters. As events enter a queue, they are bufered in a queue, until eitherqueue.timeorbatch.sizeis reached. A background thread (kafka.producer.async.ProducerSendThread) dequeues the batch of data and lets thekafka.producer.EventHandlerserialize and send the data to the apropriate kafka broker

对多个⽣产者请求进⾏排队/缓冲并异步发送批量数 据 ⸺ kafka.producer.Producer提供了在将多个 ⽣产请求序列化并发送给适当的Kafka代理分区之 前，对这些⽣产请求进⾏批量处理的能⼒ （producer.type=async）。批量的⼤⼩可以通过 ⼀些配置参数进⾏控制。当事件进⼊队列时会先 放⼊队列进⾏缓冲，直到时间到了queue.time或 者批量⼤⼩到达batch.size为⽌，后台线程 （kafka.producer.async.ProducerSendThread） 会将这批数据从队列中取出，交给 kafka.producer.EventHandler进⾏序列化并发送 给适当的kafka代理分区。通过event.handler这个 配置参数，可以在系统中插⼊⼀个⾃定义的事件 处理器。在该⽣产者队列管道中的各个不同阶 段，为了插⼊⾃定义的⽇志/跟踪代码或者⾃定义 的监视逻辑，如能注⼊回调函数会⾮常有⽤。通 过实现kafka.producer.asyn.CalbackHandler接 ⼝并将配置参数calback.handler设置为实现类就 能够实现注⼊。 使⽤⽤户指定的Encoder处理数据的序列化 （serialization） interface Encoder<T> {

artition. A custom event handler can be luged in through thevent.handlerconfig parameter. At various stages of this producer queue pipeline, it is helpful to be able to inject calbacks, either for pluging in custom

oging/tracing code or custom monitoring logic. This is posible by implementing thekafka.producer.async.CalbackHandlerinterf ace and setingcalback.handlerconfig parameter to that clas. handles the serialization of data through a user-specifiedEncoderinterface Encoder<T> {

public Mesage toMesage(T data);

public Mesage toMesage(T data);

} The default is the noopkafka.serializer.DefultEncoder provides zokeper based automatic broker discovery The zokeper based broker discovery and load balancing can be used by specifying the zokeper conection url through thezk.conectconfig parameter. For some aplications, however, the dependence on zokeper is inapropriate. In that case, the producer can take in a static list of brokers through thebroker.listconfig parameter. Each produce requests gets routed to a random broker partition in this case. If that broker is down, the produce request fails. provides software load balancing through an optionaly user-specifiedPartitionerThe routing decision is influenced by thekafka.producer.Partitioner. interface Partitioner<T> {

} Encoder的缺省值是⼀个什么活都不⼲的 kafka.serializer.DefaultEncoder。 提供基于zokeper的代理⾃动发现功能 ⸺ 通过 使⽤zk.conect配置参数指定zokeper的连接 url，就能够使⽤基于zokeper的代理发现和负 载均衡功能。在有些应⽤场合，可能不太适合于 依赖zokeper。在这种情况下，⽣产者可以从 broker.list这个配置参数中获得⼀个代理的静态列 表，每个⽣产请求会被随即的分配给各代理分 区。如果相应的代理宕机，那么⽣产请求就会失 败。

通过使⽤⼀个可选性的、由⽤户指定的Partitioner， 提供由软件实现的负载均衡功能 ⸺ 数据发送路 径选择决策受kafka.producer.Partitioner的影响。

interface Partitioner<T> { int partition(T key, int numPartitions);

} 分区API根据相关的键值以及系统中具有的代理分区

int partition(T key, int numPartitions); }

的数量返回⼀个分区id。将该id⽤作索引，在 broker_id和partition组成的经过排序的列表中为 相应的⽣产者请求找出⼀个代理分区。缺省的分 区策略是hash(key)%numPartitions。如果key为 nul，那就进⾏随机选择。使⽤partitioner.clas 这个配置参数也可以插⼊⾃定义的分区策略。

The partition API uses the key and the number of available broker partitions to return a patition id. This id is used as an index into a sorted list of broker_ids and partitions to pick a broker partition for the producer request. The default partitioning strategy ishash(key)%numPartitions. If the key is nul, then a random broker partition is picked. A custom partitioning strategy can also be pluged in using thepartitioner.clasconfig parameter.

![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

![image 3](assets/imageFile3.png)

![image 4](assets/imageFile4.png)

![image 5](assets/imageFile5.png)

![image 6](assets/imageFile6.png)

![image 7](assets/imageFile7.png)

![image 8](assets/imageFile8.png)

![image 9](assets/imageFile9.png)

![image 10](assets/imageFile10.png)
