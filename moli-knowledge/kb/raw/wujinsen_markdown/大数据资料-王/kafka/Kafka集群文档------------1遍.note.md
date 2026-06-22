### Kafka集群⽂档

- 1、kafka的数据只会 顺序apend, 不⽀持随机写，顺序读写的性能⾮常⾼效
- 2、数据的删除策略是 累积到⼀定程度或者超过⼀定时间再删除 （默认是7天）
- 3、Kafka另⼀个独特的地⽅是 将消费者信息保存在客户端⽽不是MQ服务器 （zokeper）
- 4、消息的投递过程也是采⽤客户端 主动pul的模型
- 5、客户端在pul数据的时候，尽量以 zero-copy（nio）的⽅式传输， 利⽤sendfile（对应java⾥的 FileChanel.transferTo/transferFrom） 这样的⾼级IO函数来减少拷⻉开销


Kafka存储策略

- 1. kafka以topic来进⾏消息管理，每个topic包含多个part（ition），每个part对应⼀个逻辑log，有多个 segment组成。
- 2. 每个segment中存储多条消息（见下图），消息id由其逻辑位置决定，即从消息id可直接定位到消息 的存储位置，避免id到位置的额外映射。
- 3. 每个part在内存中对应⼀个index，记录每个segment中的第⼀条消息偏移。

- 4. 发布者发到某个topic的消息会被均匀的分布到多个part上（随机或根据⽤户指定的回调函数进⾏分 布），broker收到发布消息往对应part的最后⼀个segment上添加该消息，当某个segment上的消息条数 达到配置值或消息发布时间超过阈值时，segment上的消息会被ﬂush到磁盘，只有ﬂush到磁盘上的消息 订阅者才能订阅到，segment达到⼀定的⼤⼩后将不会再往该segment写数据，broker会创建新的 segment。


发布与订阅接⼜

发布消息时，kafka client先构造⼀条消息，将消息加⼊到消息集set中（kafka⽀持批量发布，可以往消 息集合中添加多条消息，⼀次⾏发布），send消息时，client需指定消息所属的topic。

订阅消息时，kafka client需指定topic以及partition num（每个partition对应⼀个逻辑⽇志流，如topic代 表某个产品线，partition代表产品线的⽇志按天切分的结果） client订阅后，就可迭代读取消息，如果没有消息，client会阻塞直到有新的消息发布。

consumer可以累积确认接收到的消息，当其确认了某个offset的消息，意味着之前的消息也都已成功接 收到，此时broker会更新zookeeper上地offset registry（后⾯会讲到）。

⾼效的数据传输

- 1. 发布者每次可发布多条消息（将消息加到⼀个消息集合中发布）， sub每次迭代⼀条消息。
- 2. 不创建单独的cache，使⽤系统的page cache。发布者顺序发布，订阅者通常⽐发布者滞后⼀点点， 直接使⽤linux的page cache效果也⽐较后，同时减少了cache管理及垃圾收集的开销。
- 3. 使⽤sendﬁle优化⽹络传输，减少⼀次内存拷贝（nio）。


⽆状态broker

- 1. Broker没有副本机制，⼀旦broker宕机，该broker的消息将都不可⽤。
- 2. Broker不保存订阅者的状态，由订阅者⾃⼰保存。
- 3. ⽆状态导致消息的删除成为难题（可能删除的消息正在被订阅），kafka采⽤基于时间的SLA(服务⽔ 平保证)，消息保存⼀定时间（通常为7天）后会被删除。
- 4. 消息订阅者可以rewind back到任意位置重新进⾏消费，当订阅者故障时，可以选择最⼩的offset进⾏ 重新读取消费消息。


Consumer group（包含多个consumer）

- 1.⼀个topic可以被多个Consumer group分别消费，但是每个Consumer group中只能有⼀个Consumer消费 此消息。

- 2. ⼀个group内的consumer只能消费不同的partition，即⼀个partition只能被⼀个consumer消费。


Zookeeper 协调控制

- 1. 管理broker与consumer的动态加⼊与离开。 每个broker启动后，会在zookeeper上注册⼀个临时的节点（broker registry）：包含broker的ip地址和端⼜号，所存储的topics和partitions信 息。 每个consumer启动后会在zookeeper上注册⼀个临时的节点（consumer registry）：包含consumer所属的consumer group以及订阅的topics。

- 2. 触发负载均衡，当broker或consumer加⼊或离开时会触发负载均衡算法，使得⼀ 个consumer group内的多个consumer的订阅负载平衡。
- 3. 维护消费关系及每个partion的消费信息。 每个consumer group关联⼀个临时的owner registry和⼀个持久的offset registry。对于被订阅的每个 partition包含⼀个owner registry，内容为订阅这个partition的consumer id；同时包含⼀个offset registry，内容为上⼀次订阅的offset。


消息交付保证

- 1. kafka对消息的重复、丢失、错误以及顺序型没有严格的要求。
- 2. kafka提供at-least-once delivery（交付）,即当consumer宕机后，有些消息可能会被重复delivery。
- 3. 因每个partition只会被每个consumer group内的⼀个consumer消费，故kafka保证每个partition内 的消息会被顺序的订阅。
- 4. Kafka为每条消息为每条消息计算CRC校验，⽤于错误检测，crc校验不通过的消息会直接被丢弃 掉。


# ⼀、⼊门

## 1、简介

消息：根据Topic进⾏归类， 发送消息者：Producer, 消息接受者：Consumer, 每个kafka实例(server)：broker。 ⽆论是kafka集群，还是producer和consumer都依赖于zookeeper来保证系统可⽤性集群保存⼀些meta

信息。

## 2、Topics/logs

Topic：⼀类消息，每个topic将被分成多个partition(区) partition：在存储层⾯是apend log⽂件。任何发布到此partition的消息都会被直接追加到log⽂件的

尾部。 offset：每条消息在⽂件中的位置（偏移量）。offset为⼀个long型数字，它是唯⼀标记⼀条消息。 随机读写：不允许。

消息删除策略:保存⼀定时间或者⼀定量，才会删除。 即使消息被消费,消息仍然不会被⽴即删除.⽇志⽂件将会根据broker中的配置要求,保留⼀定 的时间之后删除;⽐如log⽂件保留7天,那么7天后,⽂件会被清除,⽆论其中的消息是否被消费.kafka通过

这种简单的⼿段,来释放磁盘空间,以及减少消息消费之后对⽂件内容改动的磁盘IO开⽀.

消息消费：读取offset（可以任意）处的消息，消费成功后，通知zookeeper"线性"的向前驱动offset。

![image 1](<Kafka集群文档------------1遍.note_images/imageFile1.png>)

对于consumer⽽⾔,它需要保存消费消息的ofset,对于offset的保存和使⽤,有consumer来控制; 当consumer正常消费消息时,offset将会"线性"的向前驱动,即消息将依次顺序被消费.事实上consumer可

以使⽤任意顺序消费消息,它只需要将offset重置为任意值..(offset将会保存在zookeeper中,参见下 ⽂)

消息的保存：在zokeper上。

![image 2](<Kafka集群文档------------1遍.note_images/imageFile2.png>)

kafka集群⼏乎不需要维护任何consumer和producer状态信息,这些信息有zookeeper保存;因此 producer和consumer的客户端实现⾮常轻量级,它们可以随意离开,⽽不会对集群造成额外的影响.

partitions：分区到不同的server上，⼀个partition保存在⼀个server上，避免⼀个server上的⽂件过⼤， 同时可以容纳更多的consumer消费,有效提升并发消费的能⼒

![image 3](<Kafka集群文档------------1遍.note_images/imageFile3.png>)

Partitions的设计⽬的有多个.最根本原因是kafka基于⽂件存储.通过分区,可以将⽇志内容分散 到多个server上,来避免⽂件尺⼨达到单机磁盘的上限,每个partiton都会被当前server(kafka实例)保

存;可以将⼀个topic切分多任意多个partitions,来消息保存/消费的效率.此外越多的partitions意 味着可以容纳更多的consumer,有效提升并发消费的能⼒.(具体原理参见下⽂).

## 3、Distribution（分布式）

partitions分布：⼀个partition分到⼀个server上，这个server负责partition的读写。可以配置备份。

![image 4](<Kafka集群文档------------1遍.note_images/imageFile4.png>)

⼀个Topic的多个partitions,被分布在kafka集群中的多个server上;每个server(kafka实例)负责 partitions中消息的读写操作;此外kafka还可以配置partitions需要备份的个数(replicas),每

个partition将会被备份到多台机器上,以提⾼可⽤性.

备份进⾏调度：每个partition都有⼀个server为"leader"，负责读写，其余的相对备份机为follower， follower同步leader数据，负责leader死了之后的接管。n个leader均衡的分散在每个server上。

![image 5](<Kafka集群文档------------1遍.note_images/imageFile5.png>)

每个partition都有⼀个server为"leader";leader负责所有的读写操作,如果leader失效,那么将会有 其他follower来接管(成为新的leader);follower只是单调的和leader跟进,同步消息即可..由此

可⻅作为leader的server承载了全部的请求压⼒,因此从集群的整体考虑,有多少个partitions就 意味着有多少个"leader",kafka会将"leader"均衡的分散在每个实例上,来确保整体的性能稳定.

Producers发消息：Producer将消息发布到指定的Topic中（可以指定partition），不配置⾛默认（轮 询）。

![image 6](<Kafka集群文档------------1遍.note_images/imageFile6.png>)

Producer将消息发布到指定的Topic中,同时Producer也能决定将此消息归属于哪个partition;⽐如 基于"round-robin"⽅式或者通过其他的⼀些算法等.

Consumers消费：⼀个partition对应每个group中的⼀个consumer；⼀个Topic的partition可以被多个 consumer group消费，但是consumer group中只能有⼀个consumer消费此消息。

![image 7](<Kafka集群文档------------1遍.note_images/imageFile7.png>)

本质上kafka只⽀持Topic.每个consumer属于⼀个consumer group;反过来说,每个group中可以 有多个consumer.发送到Topic的消息,只会被订阅此Topic的每个group中的⼀个consumer消费.

如果所有的consumer都具有相同的group,这种情况和queue模式很像;消息将会在consumers之 间负载均衡.

如果所有的consumer都具有不同的group,那这就是"发布-订阅";消息将会⼴播给所有的消费 者.

在kafka中,⼀个partition中的消息只会被group中的⼀个consumer消费;每个group中consumer 消息消费互相独⽴;我们可以认为⼀个group是⼀个"订阅"者,⼀个Topic中的每个partions,只会被⼀

个"订阅者"中的⼀个consumer消费,不过⼀个consumer可以消费多个partitions中的消息.kafka 只能保证⼀个partition中的消息被某个consumer消费时,消息是顺序的.事实上,从Topic⾓度来

说,消息仍不是有序的.

kafka的设计原理决定,对于⼀个topic,同⼀个group中不能有多于partitions个数的consumer同 时消费,否则将意味着某些consumer将⽆法得到消息.

Guarantes（消息保证）：

- 1) 发送到partitions中的消息将会按照它接收的顺序追加到⽇志中


- 2) 对于消费者⽽⾔,顺序消费消息。
- 3) 如果Topic的"replication factor"为N,那么允许N-1个kafka实例失效.有⼀个好使就⾏。


# ⼆、使⽤场景

1、Mesaging ：不保证消息的可靠性。 对于⼀些常规的消息系统,kafka是个不错的选择;partitons/replication和容错,可以使kafka具有良好的 扩展性和性能优势.不过到⽬前为⽌,我们应该很清楚认识到,kafka并没有提供JMS中的"事务性""消息传输 担保(消息确认机制)""消息分组"等企业级特性;kafka只能使⽤作为"常规"的消息系统,在⼀定程度上,尚未 确保消息的发送与接收绝对可靠(⽐如,消息重发,消息发送丢失等)

- 2、Websit activity tracking:⽹站活性跟踪 kafka可以作为"⽹站活性跟踪"的最佳⼯具;可以将⽹页/⽤户操作等信息发送到kafka中.并实时监控,或


者离线统计分析等 3、Log Agregation:⽇志收集中⼼ kafka的特性决定它⾮常适合作为"⽇志收集中⼼";application可以将操作⽇志"批量""异步"的发送到

kafka集群中,⽽不是保存在本地或者DB中;kafka可以批量提交消息/压缩消息等,这对producer端⽽⾔,⼏ 乎感觉不到性能的开⽀.此时consumer端可以使hadoop等其他系统化的存储和分析系统.

# 三、设计原理

kafka的设计初衷是希望作为⼀个统⼀的信息收集平台,能够实时的收集反馈信息,并需要能够⽀撑较⼤ 的数据量,且具备良好的容错能⼒.

1、持久性:通过nio进⾏消息读写，同时利⽤buffer缓存数据，多次写⼊，⼤批量刷进磁盘，较少io.

![image 8](<Kafka集群文档------------1遍.note_images/imageFile8.png>)

![image 9](<Kafka集群文档------------1遍.note_images/imageFile9.png>)

kafka使⽤⽂件存储消息,这就直接决定kafka在性能上严重依赖⽂件系统的本⾝特性.且⽆论任何OS下, 对⽂件系统本⾝的优化⼏乎没有可能.⽂件缓存/直接内存映射等是常⽤的⼿段.因为kafka是对⽇志⽂件 进⾏append操作,因此磁盘检索的开⽀是较⼩的;同时为了减少磁盘写⼊的次数,broker会将消息暂时 buffer起来,当消息的个数(或尺⼨)达到⼀定阀值时,再ﬂush到磁盘,这样减少了磁盘IO调⽤的次数. 2、性 能：压缩+nio。producer和consumer都会缓存buffer，批量ﬂush。

需要考虑的影响性能点很多,除磁盘IO之外,我们还需要考虑⽹络IO,这直接关系到kafka的吞吐量问 题.kafka并没有提供太多⾼超的技巧;对于producer端,可以将消息buffer起来,当消息的条数达到⼀定阀值 时,批量发送给broker;对于consumer端也是⼀样,批量fetch多条消息.不过消息量的⼤⼩可以通过配置⽂件 来指定.对于kafka broker端,似乎有个sendﬁle系统调⽤可以潜在的提升⽹络IO的性能:将⽂件的数据映射 到系统内存中,socket直接读取相应的内存区域即可,⽽⽆需进程再次copy和交换. 其实对于 producer/consumer/broker三者⽽⾔,CPU的开⽀应该都不⼤,因此启⽤消息压缩机制是⼀个良好的策略;压 缩需要消耗少量的CPU资源,不过对于kafka⽽⾔,⽹络IO更应该需要考虑.可以将任何在⽹络上传输的消 息都经过压缩.kafka⽀持gzip/snappy等多种压缩⽅式.

3、⽣产者

![image 10](<Kafka集群文档------------1遍.note_images/imageFile10.png>)

负载均衡: producer将会和Topic下所有partition leader保持socket连接;消息由producer直接通过socket发

送到broker,中间不会经过任何"路由层".事实上,消息被路由到哪个partition上,有producer客户端决定.⽐如 可以采⽤"random""key-hash""轮询"等,如果⼀个topic中有多个partitions,那么在producer端实现"消息均衡 分发"是必要的.

其中partition leader的位置(host:port)注册在zookeeper中,producer作为zookeeper client,已经注册了watch ⽤来监听partition leader的变更事件.

异步发送：将多条消息暂且在客户端buffer起来，并将他们批量的发送到broker，⼩数据IO太多，会 拖慢整体的⽹络延迟，批量延迟发送事实上提升了⽹络效率。不过这也有⼀定的隐患，⽐如说当 producer失效时，那些尚未发送的消息将会丢失。

4、消费者

![image 11](<Kafka集群文档------------1遍.note_images/imageFile11.png>)

consumer端向broker发送"fetch"请求,并告知其获取消息的offset;此后consumer将会获得⼀定条数的消 息;consumer端也可以重置offset来重新消费消息.

在JMS实现中,Topic模型基于push⽅式,即broker将消息推送给consumer端.不过在kafka中,采⽤了pull⽅

式,即consumer在和broker建⽴连接之后,主动去pull(或者说fetch)消息;这中模式有些优点,⾸先consumer端 可以根据⾃⼰的消费能⼒适时的去fetch消息并处理,且可以控制消息消费的进度(offset);此外,消费者可以 良好的控制消息消费的数量,batch fetch.

其他JMS实现,消息消费的位置是有prodiver保留,以便避免重复发送消息或者将没有消费成功的消息重 发等,同时还要控制消息的状态.这就要求JMS broker需要太多额外的⼯作.在kafka中,partition中的消息只 有⼀个consumer在消费,且不存在消息状态的控制,也没有复杂的消息确认机制,可见kafka broker端是相当 轻量级的.当消息被consumer接收之后,consumer可以在本地保存最后消息的offset,并间歇性的向 zookeeper注册offset.由此可见,consumer客户端也很轻量级.

5、kafka消息传送机制的单重情况

- 1) at most once: 最多⼀次（保存offset到zookeeper成功，消息处理失败，这条消息将不能被fetch到）.
- 2) at least once: 消息⾄少发送⼀次（消息处理成功了，保存offset到zookeeper失败了，下次会重新

fetch这条消息）.

- 3) exactly once: 消息只会发送⼀次（offset和处理数据都成功了）. at most once: 消费者fetch（拿）消息,然后保存offset,然后处理消息;当client保存offset之后,但是在消息


处理过程中出现了异常,导致部分消息未能继续处理.那么此后"未处理"的消息将不能被fetch到,这就是"at most once".

at least once: 消费者fetch消息,然后处理消息,然后保存offset.如果消息处理成功之后,但是在保存offset 阶段zookeeper异常导致保存操作未能执⾏成功,这就导致接下来再次fetch时可能获得上次已经处理过的 消息,这就是"at least once"，原因offset没有及时的提交给zookeeper，zookeeper恢复正常还是之前offset 状态.

exactly once: kafka中并没有严格的去实现(基于2阶段提交,事务),我们认为这种策略在kafka中是没有必

要的. 通常情况下"at-least-once"是我们搜选.(相⽐at most once⽽⾔,重复接收数据总⽐丢失数据要好). 6、复制备份：备份分主从，leader跟踪ﬂoewr状态，太差就从列表中⼲掉，保证⼀个好⽤就⾏。所以

producer保存数据时，leaderheﬂower都成功才算成功。leader死了再选⼀个数据最全的，不过也要考虑 这个

broker上是不是太多leader了。

kafka将每个partition数据复制到多个server上,任何⼀个partition有⼀个leader和多个follower(可以没 有);备份的个数可以通过broker配置⽂件来设定.leader处理所有的read-write请求,follower需要和leader保 持同步.Follower和consumer⼀样,消费消息并保存在本地⽇志中;leader负责跟踪所有的follower状态,如果 follower"落后"太多或者失效,leader将会把它从replicas同步列表中删除.当所有的follower都将⼀条消息保 存成功,此消息才被认为是"committed",那么此时consumer才能消费它.即使只有⼀个replicas实例存活,仍 然可以保证消息的正常发送和接收,只要zookeeper集群存活即可.(不同于其他分布式存储,⽐如hbase需 要"多数派"存活才⾏)

当leader失效时,需在followers中选取出新的leader,可能此时follower落后于leader,因此需要选择⼀ 个"up-to-date"的follower.选择follower时需要兼顾⼀个问题,就是新leader server上所已经承载的partition leader的个数,如果⼀个server上有过多的partition leader,意味着此server将承受着更多的IO压⼒.在选举新 leader,需要考虑到"负载均衡".

7.⽇志⽂件格式

![image 12](<Kafka集群文档------------1遍.note_images/imageFile12.png>)

![image 13](<Kafka集群文档------------1遍.note_images/imageFile13.png>)

如果⼀个topic的名称为"my_topic",它有2个partitions,那么⽇志将会保存在my_topic_0和my_topic_1两 个⽬录中;⽇志⽂件中保存了⼀系列"log entries"(⽇志条⽬),每个log entry格式为"4个字节的数字N表⽰消 息的长度" + "N个字节的消息内容";每个⽇志都有⼀个offset来唯⼀的标记⼀条消息,offset的值为8个字节 的数字,表⽰此消息在此partition中所处的起始位置..每个partition在物理存储层⾯,有多个log ﬁle组成(称 为segment).segment ﬁle的命名为"最⼩offset".kafka.例如"00000000000.kafka";其中"最⼩offset"表⽰此 segment中起始消息的offset.

注意：

- 1）、其中每个partiton中所持有的segments列表信息会存储在zookeeper中.
- 2）、当segment⽂件尺⼨达到⼀定阀值时(可以通过配置⽂件设定,默认1G),将会创建⼀个新的⽂件;
- 3）、当buffer中消息的条数达到阀值时（或者距离最近⼀次flush的时间差"达到阀值）将会触发⽇

志信息ﬂush到⽇志⽂件中。

- 4）、验证与修复：如果broker失效,极有可能会丢失那些尚未ﬂush到⽂件的消息.因为server意外实现,

仍然会导致log⽂件格式的破坏(⽂件尾部),那么就要求当server启动是需要检测最后⼀个segment的⽂件结 构

是 否合法并进⾏必要的修复.

- 5）、获取消息时,需要指定offset和最⼤chunk尺⼨：offset⽤来表⽰消息的起始位置,chunk size⽤来表

⽰最⼤获取消息的总长度(间接的表⽰消息的条数).根据offset,可以找到此消息所在segment⽂件,然后根 据segment的最⼩offset取差值,得到它在ﬁle中的相对位置,直接读取输出即可.

- 6）、⽇志⽂件的删除策略⾮常简单:启动⼀个后台线程定期扫描log ﬁle列表,把保存时间超过阀值的⽂


件直接删除(根据⽂件的创建时间).为了避免删除⽂件时仍然有read操作(consumer消费),采取copy-on-

write⽅式（在复制⼀个对象的时候并不是真正的把原先的对象复制到内存的另外⼀个位置上，⽽是在新 对象的内存映射表中设置⼀个指针，指向源对象的位置，并把那块内存的Copy-On-Write位设

置为1.这样，在对新的对象执⾏读操作的时候，内存数据不发⽣任何变动，直接执⾏读操作；⽽ 在对新的对象执⾏写操作时，将真正的对象复制到新的内存地址中，并修改新对象的内存映射表

指向这个新的位置，并在新的内存位置上执⾏写操作。）.

8、分配 kafka使⽤zookeeper来存储⼀些meta信息,并使⽤了zookeeper watch机制来发现meta信息的变更并作出

相应的动作(⽐如consumer失效,触发负载均衡等)

- 1) Broker node registry: 当⼀个kafka broker启动后,⾸先会向zookeeper注册⾃⼰的节点信息(临时znode),

同时当broker和zookeeper断开连接时,此znode也会被删除. 格式: /broker/ids/[0...N] -->host:port; 其中[0..N]表⽰broker id,每个broker的配置⽂件中都需要指定⼀个数字类型的id(全局

不可重复),znode的值为此broker的host:port信息.

- 2) Broker Topic Registry: 当⼀个broker启动时,会向zookeeper注册⾃⼰持有的topic和partitions信息,仍然


是⼀个临时znode. 格式: /broker/topics/[topic]/[0...N] 其中[0..N]表⽰partition索引号.

- 3) Consumer and Consumer group: 每个consumer客户端被创建时,会向zookeeper注册⾃⼰的信息;此作


⽤主要是为了"负载均衡".

⼀个group中的多个consumer可以交错的消费⼀个topic的所有partitions;简⽽⾔之,保证此topic的所有 partitions都能被此group所消费,且消费时为了性能考虑,让partition相对均衡的分散到每个consumer上.

4) Consumer id Registry: 每个consumer都有⼀个唯⼀的ID(host:uuid,可以通过配置⽂件指定,也可以由系

统⽣成),此id⽤来标记消费者信息. 格式: /consumers/[group_id]/ids/[consumer_id] 仍然是⼀个临时的znode,此节点的值为{"topic_name":#streams...},即表⽰此consumer⽬前所消费的

topic + partitions列表.

5) Consumer offset Tracking: ⽤来跟踪每个consumer⽬前所消费的partition中最⼤的offset. 格式: /consumers/[group_id]/offsets/[topic]/[broker_id-partition_id]-->offset_value 此znode为持久节点,可以看出offset跟group_id有关,以表明当group中⼀个消费者失效,其他consumer可

以继续消费.

6) Partition Owner registry: ⽤来标记partition被哪个consumer消费.临时znode 格式: /consumers/[group_id]/owners/[topic]/[broker_id-partition_id] -->consumer_node_id

当consumer启动时,所触发的操作:

- A) ⾸先进⾏"Consumer id Registry";
- B) 然后在"Consumer id Registry"节点下注册⼀个watch⽤来监听当前group中其他consumer


的"leave"和"join";

只要此znode path下节点列表变更,都会触发此group下consumer的负载均衡.(⽐如⼀个consumer失效, 那么其他consumer接管partitions).

C) 在"Broker id registry"节点下,注册⼀个watch⽤来监听broker的存活情况;如果broker列表变更,将会触 发所有的groups下的consumer重新balance.

- 1) Producer端使⽤zookeeper⽤来"发现"broker列表,以及和Topic下每个partition leader建⽴socket连接并

发送消息.

- 2) Broker端使⽤zookeeper⽤来注册broker信息,以及监测partition leader存活性.
- 3) Consumer端使⽤zookeeper⽤来注册consumer信息,其中包括consumer消费的partition列表等,同时也


⽤来发现broker列表,并和partition leader建⽴socket连接,并获取消息. 四、主要配置

1、Broker配置

broker.id=1 port=9091 num.network.threads=2 num.io.threads=2

socket.send.buffer.bytes=1048576 socket.receive.buffer.bytes=1048576

socket.request.max.bytes=104857600 log.dir=./logs num.partitions=2 log.ﬂush.interval.messages=10000 log.ﬂush.interval.ms=1000 log.retention.hours=168 #log.retention.bytes=1073741824 log.segment.bytes=536870912 num.replica.fetchers=2 log.cleanup.interval.mins=10 zookeeper.connect=192.168.0.1:2181,192.168.0.2:2182,192.168.0.3:2183 zookeeper.connection.timeout.ms=1000000 kafka.metrics.polling.interval.secs=5 kafka.metrics.reporters=kafka.metrics.KafkaCSVMetricsReporter kafka.csv.metrics.dir=/tmp/kafka_metrics

kafka.csv.metrics.reporter.enabled=false

- 2.Consumer主要配置
- 3.Producer主要配置


补充说明：

1、public Map<String, List<KafkaStream<byte[], byte[]>>> createMessageStreams(Map<String, Integer> topicCountMap)，其中该⽅法的参数Map的key 为topic名称，value为topic对应的分区数，譬如说如果在kafka中不存在相应的topic时，则会创建⼀个 topic，分区数为value，如果存在的话，该处的value则不起什么作⽤

1

2、关于⽣产者向指定的分区发送数据，通过设置partitioner.class的属性来指定向那个分区发送数据， 如果⾃⼰指定必须编写相应的程序，默认是kafka.producer.DefaultPartitioner,分区程序是基于散 列的键。

1

- 3、在多个消费者读取同⼀个topic的数据，为了保证每个消费者读取数据的唯⼀性，必须将这些消费者 group_id定义为同⼀个值，这样就构建了⼀个类似队列的数据结构，如果定义不同，则类似⼀种⼴播结 构的。
- 4、在consumer api中，参数设计到数字部分，类似Map<String,Integer>, numStream,指的都是在topic不存在的时，会创建⼀个topic，并且分区个数为Integer,numStream,注意如 果数字⼤于broker的配置中num.partitions属性，会以num.partitions为依据创建分区个数的。
- 5、producer api，调⽤send时，如果不存在topic，也会创建topic，在该⽅法中没有提供分区个数的参 数，在这⾥分区个数是由服务端broker的配置中num.partitions属性决定的


以上是关于kafka⼀些基础说明，在其中我们知道如果要kafka正常运⾏，必须配置zookeeper，否则⽆论 是kafka集群还是客户端的⽣存者和消费者都⽆法正常的⼯作的，以下是对zookeeper进⾏⼀些简单的介 绍： 五、zookeeper集群

zookeeper是⼀个为分布式应⽤提供⼀致性服务的软件，它是开源的Hadoop项⽬的⼀个⼦项⽬，并根 据google发表的⼀篇论⽂来实现的。zookeeper为分布式系统提供了⾼笑且易于使⽤的协同服务，它可以 为分布式应⽤提供相当多的服务，诸如统⼀命名服务，配置管理，状态同步和组服务等。zookeeper接 ⼜简单，我们不必过多地纠结在分布式系统编程难于处理的同步和⼀致性问题上，你可以使⽤ zookeeper提供的现成(off-the-shelf)服务来实现来实现分布式系统额配置管理，组管理，Leader选举等功 能。

zookeeper集群的安装,准备三台服务器server1:192.168.0.1,server2:192.168.0.2, server3:192.168.0.3. 1)下载zookeeper 到 去下载最新版本Zookeeper-3.4.5的安装包zookeeper-

htp:/zokeper.apache.org/releases.html

- 3.4.5.tar.gz.将⽂件保存server1的~⽬录下 2)安装zookeeper 先在服务器server分别执⾏a-c步骤 a)解压 tar -zxvf zookeeper-3.4.5.tar.gz 解压完成后在⽬录~下会发现多出⼀个⽬录zookeeper-3.4.5,重新命令为zookeeper b）配置 将conf/zoo_sample.cfg拷贝⼀份命名为zoo.cfg，也放在conf⽬录下。然后按照如下值修改其中的配


置：

# The number of milliseconds of each tick tickTime=2000 # The number of ticks that the initial

# synchronization phase can take initLimit=10 # The number of ticks that can pass between # sending a request and getting an acknowledgement syncLimit=5 # the directory where the snapshot is stored. # do not use /tmp for storage, /tmp here is just # example sakes. dataDir=/home/wwb/zookeeper /data dataLogDir=/home/wwb/zookeeper/logs # the port at which the clients will connect clientPort=2181 # # Be sure to read the maintenance section of the # administrator guide before turning on autopurge. # # # The number of snapshots to retain in dataDir #autopurge.snapRetainCount=3 # Purge task interval in hours # Set to "0" to disable auto purge feature #autopurge.purgeInterval=1

htp:/zokeper.apache.org/doc/curent/zokeperAdmin.html#sc_maintenance

- server.1=192.168.0.1:3888:4888
- server.2=192.168.0.2:3888:4888
- server.3=192.168.0.3:3888:4888 tickTime：这个时间是作为 Zookeeper 服务器之间或客户端与服务器之间维持⼼跳的时间间隔，也就


是每个 tickTime 时间就会发送⼀个⼼跳。

dataDir：顾名思义就是 Zookeeper 保存数据的⽬录，默认情况下，Zookeeper 将写数据的⽇志⽂件也 保存在这个⽬录⾥。

clientPort：这个端⼜就是客户端连接 Zookeeper 服务器的端⼜，Zookeeper 会监听这个端⼜，接受客 户端的访问请求。

initLimit：这个配置项是⽤来配置 Zookeeper 接受客户端（这⾥所说的客户端不是⽤户连 接 Zookeeper 服务器的客户端，⽽是 Zookeeper 服务器集群中连接到 Leader 的 Follower 服务器）初始 化连接时最长能忍受多少个⼼跳时间间隔数。当已经超过 5个⼼跳的时间（也就是 tickTime）长度 后 Zookeeper 服务器还没有收到客户端的返回信息，那么表明这个客户端连接失败。总的时间长度就 是 5*2000=10 秒

syncLimit：这个配置项标识 Leader 与 Follower 之间发送消息，请求和应答时间长度，最长不能超过 多少个 tickTime 的时间长度，总的时间长度就是 2*2000=4 秒

server.A=B：C：D：其中 A 是⼀个数字，表⽰这个是第⼏号服务器；B 是这个服务器的 ip 地址；

- C 表⽰的是这个服务器与集群中的 Leader 服务器交换信息的端⼜；D 表⽰的是万⼀集群中的 Leader 服 务器挂了，需要⼀个端⼜来重新进⾏选举，选出⼀个新的 Leader，⽽这个端⼜就是⽤来执⾏选举时服 务器相互通信的端⼜。如果是伪集群的配置⽅式，由于 B 都是⼀样，所以不同的 Zookeeper 实例通信 端⼜号不能⼀样，所以要给它们分配不同的端⼜号 注意:dataDir,dataLogDir中的wwb是当前登录⽤户名，data，logs⽬录开始是不存在，需要使⽤mkdir命 令创建相应的⽬录。并且在该⽬录下创建⽂件myid,serve1,server2,server3该⽂件内容分别为1,2,3。 针对服务器server2,server3可以将server1复制到相应的⽬录，不过需要注意dataDir,dataLogDir⽬录,并且 ⽂件myid内容分别为2,3


- 3)依次启动server1，server2,server3的zookeeper. /home/wwb/zookeeper/bin/zkServer.sh start,出现类似以下内容 JMX enabled by default Using conﬁg: /home/wwb/zookeeper/bin/../conf/zoo.cfg Starting zookeeper ... STARTED
- 4) 测试zookeeper是否正常⼯作，在server1上执⾏以下命令 /home/wwb/zookeeper/bin/zkCli.sh -server 192.168.0.2:2181,出现类似以下内容 JLine support is enabled 2013-11-27 19:59:40,560 - INFO [main-


SendThread(localhost.localdomain:2181):ClientCnxn$SendThread@736] - Session establishment complete on server localhost.localdomain/127.0.0.1:2181, sessionid = 0x1429cdb49220000, negotiated timeout = 30000

WATCHER::

WatchedEvent state:SyncConnected type:None path:null [zk: 127.0.0.1:2181(CONNECTED) 0] [root@localhost zookeeper2]# 即代表集群构建成功了,如果出现错误那应该是第三部时没有启动好集群，

运⾏，先利⽤

ps aux | grep zookeeper查看是否有相应的进程的，没有话，说明集群启动出现问题，可以在每个服务 器上使⽤

./home/wwb/zookeeper/bin/zkServer.sh stop。再依次使⽤./home/wwb/zookeeper/bin zkServer.sh start，这时在执⾏4⼀般是没有问题，如果还是有问题，那么先stop再到bin的上级⽬录执 ⾏./bin/zkServer.sh start试试。

注意：zookeeper集群时，zookeeper要求半数以上的机器可⽤，zookeeper才能提供服务。

六、kafka集群(利⽤上⾯server1,server2,server3,下⾯以server1为实例)

- 1)下载kafka0.8( 保存到服务器/home/wwb⽬录下kafka-

0.8.0-beta1-src.tgz(kafka_2.8.0-0.8.0-beta1.tgz)

- 2)解压 tar -zxvf kafka-0.8.0-beta1-src.tgz,产⽣⽂件夹kafka-0.8.0-beta1-src更改为kafka01
- 3)配置 修改kafka01/conﬁg/server.properties,其中broker.id,log.dirs,zookeeper.connect必须根据实际情况进⾏修


htp:/kafka.apache.org/downloads.html),

改，其他项根据需要⾃⾏斟酌。⼤致如下： broker.id=1 port=9091 num.network.threads=2 num.io.threads=2 socket.send.buffer.bytes=1048576

socket.receive.buffer.bytes=1048576

socket.request.max.bytes=104857600 log.dir=./logs num.partitions=2 log.ﬂush.interval.messages=10000 log.ﬂush.interval.ms=1000 log.retention.hours=168 #log.retention.bytes=1073741824 log.segment.bytes=536870912 num.replica.fetchers=2 log.cleanup.interval.mins=10 zookeeper.connect=192.168.0.1:2181,192.168.0.2:2182,192.168.0.3:2183 zookeeper.connection.timeout.ms=1000000 kafka.metrics.polling.interval.secs=5 kafka.metrics.reporters=kafka.metrics.KafkaCSVMetricsReporter kafka.csv.metrics.dir=/tmp/kafka_metrics kafka.csv.metrics.reporter.enabled=false

4）初始化因为kafka⽤scala语⾔编写，因此运⾏kafka需要⾸先准备scala相关环境。 > cd kafka01 > ./sbt update > ./sbt package > ./sbt assembly-package-dependency

在第⼆个命令时可能需要⼀定时间，由于要下载更新⼀些依赖包。所以请⼤家 耐⼼点。

- 5) 启动kafka01


>JMX_PORT=9997 bin/kafka-server-start.sh conﬁg/server.properties &

- a)kafka02操作步骤与kafka01雷同，不同的地⽅如下 修改kafka02/conﬁg/server.properties broker.id=2 port=9092 ##其他配置和kafka-0保持⼀致 启动kafka02

- JMX_PORT=9998 bin/kafka-server-start.sh conﬁg/server.properties &

b)kafka03操作步骤与kafka01雷同，不同的地⽅如下 修改kafka03/conﬁg/server.properties broker.id=3 port=9093 ##其他配置和kafka-0保持⼀致 启动kafka02

- JMX_PORT=9999 bin/kafka-server-start.sh conﬁg/server.properties &




- 6)创建Topic(包含⼀个分区，三个副本) >bin/kafka-create-topic.sh --zookeeper 192.168.0.1:2181 --replica 3 --partition 1 --topic my-replicated-topic
- 7)查看topic情况 >bin/kafka-list-top.sh --zookeeper 192.168.0.1:2181 topic: my-replicated-topic partition: 0 leader: 1 replicas: 1,2,0 isr: 1,2,0
- 8)创建发送者 >bin/kafka-console-producer.sh --broker-list 192.168.0.1:9091 --topic my-replicated-topic

- my test message1
- my test message2 ^C


- 9)创建消费者 >bin/kafka-console-consumer.sh --zookeeper 127.0.0.1:2181 --from-beginning --topic my-replicated-topic

...

- my test message1
- my test message2


^C

- 10)杀掉server1上的broker >pkill -9 -f conﬁg/server.properties
- 11)查看topic >bin/kafka-list-top.sh --zookeeper 192.168.0.1:2181 topic: my-replicated-topic partition: 0 leader: 1 replicas: 1,2,0 isr: 1,2,0


发现topic还正常的存在

11）创建消费者，看是否能查询到消息 >bin/kafka-console-consumer.sh --zookeeper 192.168.0.1:2181 --from-beginning --topic my-replicated-topic

...

- my test message 1
- my test message 2 ^C


说明⼀切都是正常的。

OK,以上就是对Kafka个⼈的理解，不对之处请⼤家及时指出。

关于kafka说明可以参考： htp:/kafka.apache.org/documentation.html

