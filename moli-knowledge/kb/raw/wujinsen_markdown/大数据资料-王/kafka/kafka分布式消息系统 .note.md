2011-08-28 18:32:46

分类： LINUX

Kafka[1]是linkedin⽤于⽇志处理的分布式消息队列，linkedin的⽇志数据容量⼤，但对可靠性要求 不⾼，其⽇志数据主要包括⽤户⾏为（登录、浏览、点击、分享、喜欢）以及系统运⾏⽇志 （CPU、内存、磁盘、⽹络、系统及进程状态）。

当前很多的消息队列服务提供可靠交付保证，并默认是即时消费（不适合离线）。⾼可靠交付对 linkedin的⽇志不是必须的，故可通过降低可靠性来提⾼性能，同时通过构建分布式的集群，允许 消息在系统中累积，使得kafka同时⽀持离线和在线⽇志处理。

注：本⽂中发布者（publisher）与⽣产者（producer）可以互换，订阅者（subscriber）与消费者 （consumer）可以互换。

Kafka是⼀个⾼吞吐量分布式消息系统。linkedin开源的kafka。 Kafka就跟这个名字⼀样，设计⾮常独特。⾸先，kafka的开发 者们认为不需要在内存⾥缓存什么数据，操作系统的⽂件缓存已经⾜够完善和强⼤，只要你不搞随机写，顺序读写的性能是⾮常⾼ 效的。kafka的数据只会顺序append，数据的删除策略是累积到⼀定程度或者超过⼀定时间再删除。Kafka另⼀个独特的地⽅是 将消费者信息保存在客户端⽽不是MQ服务器，这样服务器就不⽤记录消息的投递过程，每个客户端都⾃⼰知道⾃⼰下⼀次应该从 什么地⽅什么位置读取消息，消息的投递过程也是采⽤客户端主动pull的模型，这样⼤⼤减轻了服务器的负担。Kafka还强调减少 数据的序列化和拷⻉开销，它会将⼀些消息组织成Message Set做批量存储和发送，并且客户端在pull数据的时候，尽量以zero-

copy的⽅式传输，利⽤sendfile（对应java⾥的 FileChannel.transferTo/transferFrom）这样的⾼级IO函数来减少拷⻉开销。 可⻅，kafka是⼀个精⼼设计，特定于某些应⽤的MQ系统，这种偏向特定领域的MQ系统我估计会越来越多，垂直化的产品策略值 的考虑。

Kafka的架构如下图所⽰：

![image 1](<kafka分布式消息系统 .note_images/imageFile1.png>)

Kafka存储策略

- 1. kafka以topic来进⾏消息管理，每个topic包含多个part（ition），每个part对应⼀个逻辑log，有 多个segment组成。

- 2. 每个segment中存储多条消息（见下图），消息id由其逻辑位置决定，即从消息id可直接定位到 消息的存储位置，避免id到位置的额外映射。

- 3. 每个part在内存中对应⼀个index，记录每个segment中的第⼀条消息偏移。

- 4. 发布者发到某个topic的消息会被均匀的分布到多个part上（随机或根据⽤户指定的回调函数进 ⾏分布），broker收到发布消息往对应part的最后⼀个segment上添加该消息，当某个segment上的 消息条数达到配置值或消息发布时间超过阈值时，segment上的消息会被ﬂush到磁盘，只有ﬂush到 磁盘上的消息订阅者才能订阅到，segment达到⼀定的⼤⼩后将不会再往该segment写数据，broker 会创建新的segment。


![image 2](<kafka分布式消息系统 .note_images/imageFile2.png>)

发布与订阅接⼜

![image 3](<kafka分布式消息系统 .note_images/imageFile3.png>)

发布消息时，kafka client先构造⼀条消息，将消息加⼊到消息集set中（kafka⽀持批量发布，可以 往消息集合中添加多条消息，⼀次⾏发布），send消息时，client需指定消息所属的topic。

![image 4](<kafka分布式消息系统 .note_images/imageFile4.png>)

订阅消息时，kafka client需指定topic以及partition num（每个partition对应⼀个逻辑⽇志流，如 topic代表某个产品线，partition代表产品线的⽇志按天切分的结果），client订阅后，就可迭代读 取消息，如果没有消息，client会阻塞直到有新的消息发布。consumer可以累积确认接收到的消 息，当其确认了某个offset的消息，意味着之前的消息也都已成功接收到，此时broker会更新 zookeeper上地offset registry（后⾯会讲到）。

⾼效的数据传输

- 1. 发布者每次可发布多条消息（将消息加到⼀个消息集合中发布）， sub每次迭代⼀条消息。

- 2. 不创建单独的cache，使⽤系统的page cache。发布者顺序发布，订阅者通常⽐发布者滞后⼀点 点，直接使⽤linux的page cache效果也⽐较后，同时减少了cache管理及垃圾收集的开销。

- 3. 使⽤sendﬁle优化⽹络传输，减少⼀次内存拷贝。


⽆状态broker

- 1. Broker没有副本机制，⼀旦broker宕机，该broker的消息将都不可⽤。

- 2. Broker不保存订阅者的状态，由订阅者⾃⼰保存。

- 3. ⽆状态导致消息的删除成为难题（可能删除的消息正在被订阅），kafka采⽤基于时间的SLA(服 务⽔平保证)，消息保存⼀定时间（通常为7天）后会被删除。

- 4. 消息订阅者可以rewind back到任意位置重新进⾏消费，当订阅者故障时，可以选择最⼩的offset 进⾏重新读取消费消息。


Consumer group

- 1. 允许consumer group（包含多个consumer，如⼀个集群同时消费）对⼀个topic进⾏消费，不同的 consumer group之间独⽴订阅。

- 2. 为了对减⼩⼀个consumer group中不同consumer之间的分布式协调开销，指定partition为最⼩的 并⾏消费单位，即⼀个group内的consumer只能消费不同的partition。


Zookeeper 协调控制

- 1. 管理broker与consumer的动态加⼊与离开。


- 2. 触发负载均衡，当broker或consumer加⼊或离开时会触发负载均衡算法，使得⼀ 个consumer group内的多个consumer的订阅负载平衡。

- 3. 维护消费关系及每个partion的消费信息。


Zookeeper上的细节：

- 1. 每个broker启动后会在zookeeper上注册⼀个临时的broker registry，包含broker的ip地址和端⼜ 号，所存储的topics和partitions信息。

- 2. 每个consumer启动后会在zookeeper上注册⼀个临时的consumer registry：包含consumer所属的 consumer group以及订阅的topics。

- 3. 每个consumer group关联⼀个临时的owner registry和⼀个持久的offset registry。对于被订阅的每个 partition包含⼀个owner registry，内容为订阅这个partition的consumer id；同时包含⼀个offset registry，内容为上⼀次订阅的offset。


消息交付保证

- 1. kafka对消息的重复、丢失、错误以及顺序型没有严格的要求。

- 2. kafka提供at-least-once delivery,即当consumer宕机后，有些消息可能会被重复delivery。

- 3. 因每个partition只会被consumer group内的⼀个consumer消费，故kafka保证每个partition内 的消息会被顺序的订阅。

- 4. Kafka为每条消息为每条消息计算CRC校验，⽤于错误检测，crc校验不通过的消息会直接被丢 弃掉。


Linkedin的应⽤环境

如下图，左边的应⽤于⽇志数据的在线实时处理，右边的应⽤于⽇志数据的离线分析（现将⽇志 pull⾄hadoop或DWH中）。

![image 5](<kafka分布式消息系统 .note_images/imageFile5.png>)

Kafka的性能

测试环境： 2 Linux machines, each with 8 2GHz cores, 16GB of memory, 6 disks with RAID 10. The two machines are connected with a 1Gb network link. One of the machines was used as the broker and the other machine was used as the producer or the consumer.

测试评价(by me)：（1）环境过于简单，不⾜以说明问题。（2）对于producer持续的波动没有进⾏ 分析。（3）只有两台机器zookeeper都省了？？

测试结果：如下图，完胜其他的message queue，单条消息发送（每条200bytes）,能到 50000messages/sec，50条batch⽅式发送，平均为400000messages/sec.

![image 6](<kafka分布式消息系统 .note_images/imageFile6.png>)

Kafka未来研究⽅向

- 1. 数据压缩（节省⽹络带宽及存储空间）

- 2. Broker多副本

- 3. 流式处理应⽤


参考资料

- 【1】

- 【2】


http://research.microsoft.com/en-us/um/people/srikanth/netdb11/netdb11papers/ne tdb11-ﬁnal12.pdf

https://cwiki.apache.org/KAFKA/kafka-papers-and-presentations.data/Kafka-netd b-06-2011.pdf

