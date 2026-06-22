# 1.配置

Ø Broker主要配置

<table>
  <tr>
    <th>参数</th>
    <th>默认值</th>
    <th>说明(解释)</th>
  </tr>
  <tr>
    <td>broker.id =0</td>
    <td> </td>
    <td>每⼀个broker在集群中的唯⼀表⽰，要求是正 数。当该服务器的IP地址发⽣改变时， broker.id没有变化，则不会影响 consumers的消息情况</td>
  </tr>
  <tr>
    <td>log.dirs=/data/kafka-logs</td>
    <td> </td>
    <td>kafka数据的存放地址，多个地址的话⽤逗号<br><br>分割/data/kafka-logs1，/data/kafka-logs-2</td>
  </tr>
  <tr>
    <td>port =9092</td>
    <td> </td>
    <td>broker server服务端⼜</td>
  </tr>
  <tr>
    <td>message.max.bytes =6525000</td>
    <td> </td>
    <td>表⽰消息体的最⼤⼤⼩，单位是字节</td>
  </tr>
  <tr>
    <td>num.network.threads =4</td>
    <td> </td>
    <td>broker处理消息的最⼤线程数，⼀般情况下不 需要去修改</td>
  </tr>
  <tr>
    <td>num.io.threads =8</td>
    <td> </td>
    <td>broker处理磁盘IO的线程数，数值应该⼤于 你的硬盘数</td>
  </tr>
  <tr>
    <td>background.threads =4</td>
    <td> </td>
    <td>⼀些后台任务处理的线程数，例如过期消息⽂ 件的删除等，⼀般情况下不需要去做修改</td>
  </tr>
  <tr>
    <td>queued.max.requests =500</td>
    <td> </td>
    <td>等待IO线程处理的请求队列最⼤数，若是等待 IO的请求超过这个数值，那么会停⽌接受外部 消息，应该是⼀种⾃我保护机制。</td>
  </tr>
  <tr>
    <td>host.name</td>
    <td> </td>
    <td>broker的主机地址，若是设置了，那么会绑定 到这个地址上，若是没有，会绑定到所有的接 ⼜上，并将其中之⼀发送到ZK，⼀般不设置</td>
  </tr>
  <tr>
    <td>socket.send.buffer.bytes=100*1024</td>
    <td> </td>
    <td>socket的发送缓冲区，socket的调优参数 SO_SNDBUFF</td>
  </tr>
  <tr>
    <td>socket.receive.buffer.bytes<br><br>=100*1024</td>
    <td> </td>
    <td>socket的接受缓冲区，socket的调优参数 SO_RCVBUFF</td>
  </tr>
  <tr>
    <td>socket.request.max.bytes<br><br>=100*1024*1024</td>
    <td> </td>
    <td>socket请求的最⼤数值，防⽌serverOOM， message.max.bytes必然要⼩于 socket.request.max.bytes，会被topic 创建时的指定参数覆盖</td>
  </tr>
  <tr>
    <td>log.segment.bytes<br><br>=1024*1024*1024</td>
    <td> </td>
    <td>topic的分区是以⼀堆segment⽂件存储的， 这个控制每个segment的⼤⼩，会被topic创 建时的指定参数覆盖</td>
  </tr>
  <tr>
    <td>log.roll.hours =24*7</td>
    <td> </td>
    <td>这个参数会在⽇志segment没有达到 log.segment.bytes设置的⼤⼩，也会强制 新建⼀个segment会被 topic创建时的指定 参数覆盖</td>
  </tr>
  <tr>
    <td>log.cleanup.policy = delete</td>
    <td> </td>
    <td>⽇志清理策略选择有：delete和compact主 要针对过期数据的处理，或是⽇志⽂件达到限 制的额度，会被 topic创建时的指定参数覆盖</td>
  </tr>
  <tr>
    <td>log.retention.minutes=3days</td>
    <td> </td>
    <td>数据存储的最⼤时间超过这个时间会根据 log.cleanup.policy设置的策略处理数 据，也就是消费端能够多久去消费数据 log.retention.bytes和 log.retention.minutes任意⼀个达到要 求，都会执⾏删除，会被topic创建时的指定 参数覆盖</td>
  </tr>
  <tr>
    <td>log.retention.bytes=-1</td>
    <td> </td>
    <td>topic每个分区的最⼤⽂件⼤⼩，⼀个topic 的⼤⼩限制 =分区数<br><br>*log.retention.bytes。-1没有⼤⼩限 log.retention.bytes和 log.retention.minutes任意⼀个达到要 求，都会执⾏删除，会被topic创建时的指定 参数覆盖</td>
  </tr>
  <tr>
    <td>log.retention.check.interval.ms=5m inutes</td>
    <td> </td>
    <td>⽂件⼤⼩检查的周期时间，是否处 罚 log.cleanup.policy中设置的策略</td>
  </tr>
  <tr>
    <td>log.cleaner.enable=false</td>
    <td> </td>
    <td>是否开启⽇志压缩</td>
  </tr>
</table>


<table>
  <tr>
    <th>log.cleaner.threads = 2</th>
    <th> </th>
    <th>⽇志压缩运⾏的线程数</th>
  </tr>
  <tr>
    <td>log.cleaner.io.max.bytes.per.secon d=None</td>
    <td> </td>
    <td>⽇志压缩时候处理的最⼤⼤⼩</td>
  </tr>
  <tr>
    <td>log.cleaner.dedupe.buffer.size=500<br><br>*1024*1024</td>
    <td> </td>
    <td>⽇志压缩去重时候的缓存空间，在空间允许的 情况下，越⼤越好</td>
  </tr>
  <tr>
    <td>log.cleaner.io.buffer.size=512*102 4</td>
    <td> </td>
    <td>⽇志清理时候⽤到的IO块⼤⼩⼀般不需要修改</td>
  </tr>
  <tr>
    <td>log.cleaner.io.buffer.load.factor<br><br>=0.9</td>
    <td> </td>
    <td>⽇志清理中hash表的扩⼤因⼦⼀般不需要修改</td>
  </tr>
  <tr>
    <td>log.cleaner.backoff.ms =15000</td>
    <td> </td>
    <td>检查是否处罚⽇志清理的间隔</td>
  </tr>
  <tr>
    <td>log.cleaner.min.cleanable.ratio=0. 5</td>
    <td> </td>
    <td>⽇志清理的频率控制，越⼤意味着更⾼效的清 理，同时会存在⼀些空间上的浪费，会被 topic创建时的指定参数覆盖</td>
  </tr>
  <tr>
    <td>log.cleaner.delete.retention.ms<br><br>=1day</td>
    <td> </td>
    <td>对于压缩的⽇志保留的最长时间，也是客户端 消费消息的最长时间，同 log.retention.minutes的区别在于⼀个控 制未压缩数据，⼀个控制压缩后的数据。会被 topic创建时的指定参数覆盖</td>
  </tr>
  <tr>
    <td>log.index.size.max.bytes<br><br>=10*1024*1024</td>
    <td> </td>
    <td>对于segment⽇志的索引⽂件⼤⼩限制，会被 topic创建时的指定参数覆盖</td>
  </tr>
  <tr>
    <td>log.index.interval.bytes =4096</td>
    <td> </td>
    <td>当执⾏⼀个fetch操作后，需要⼀定的空间来 扫描最近的offset⼤⼩，设置越⼤，代表扫描 速度越快，但是也更好内存，⼀般情况下不需 要搭理这个参数</td>
  </tr>
  <tr>
    <td>log.flush.interval.messages=None</td>
    <td> </td>
    <td>log⽂件”sync”到磁盘之前累积的消息条数, 因为磁盘IO操作是⼀个慢操作,但又是⼀个”数 据可靠性"的必要⼿段,所以此参数的设置,需 要在"数据可靠性"与"性能"之间做必要的权 衡.如果此值过⼤,将会导致每次"fsync"的时 间较长(IO阻塞),如果此值过⼩,将会导 致"fsync"的次数较多,这也意味着整体的 client请求有⼀定的延迟.物理server故障, 将会导致没有fsync的消息丢失.<br><br></td>
  </tr>
  <tr>
    <td>log.flush.scheduler.interval.ms<br><br>=3000</td>
    <td> </td>
    <td>检查是否需要固化到硬盘的时间间隔</td>
  </tr>
  <tr>
    <td>log.flush.interval.ms = None</td>
    <td> </td>
    <td>仅仅通过interval来控制消息的磁盘写⼊时 机,是不⾜的.此参数⽤于控制"fsync"的时间 间隔,如果消息量始终没有达到阀值,但是离上 ⼀次磁盘同步的时间间隔达到阀值,也将触发.</td>
  </tr>
  <tr>
    <td>log.delete.delay.ms =60000</td>
    <td> </td>
    <td>⽂件在索引中清除后保留的时间⼀般不需要去 修改</td>
  </tr>
  <tr>
    <td>log.flush.offset.checkpoint.interv al.ms =60000</td>
    <td> </td>
    <td>控制上次固化硬盘的时间点，以便于数据恢复 ⼀般不需要去修改</td>
  </tr>
  <tr>
    <td>auto.create.topics.enable =true</td>
    <td> </td>
    <td>是否允许⾃动创建topic，若是false，就需 要通过命令创建topic</td>
  </tr>
  <tr>
    <td>default.replication.factor =1</td>
    <td> </td>
    <td>是否允许⾃动创建topic，若是false，就需 要通过命令创建topic</td>
  </tr>
  <tr>
    <td>num.partitions =1</td>
    <td> </td>
    <td>每个topic的分区个数，若是在topic创建时 候没有指定的话会被topic创建时的指定参数 覆盖</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>以下是kafka中Leader,replicas配置参数</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>controller.socket.timeout.ms<br><br>=30000</td>
    <td> </td>
    <td>partition leader与replicas之间通讯 时,socket的超时时间</td>
  </tr>
  <tr>
    <td>controller.message.queue.size=10</td>
    <td> </td>
    <td>partition leader与replicas数据同步 时,消息的队列尺⼨</td>
  </tr>
</table>


<table>
  <tr>
    <th>replica.lag.time.max.ms =10000</th>
    <th> </th>
    <th>replicas响应partition leader的最长等 待时间，若是超过这个时间，就将replicas 列⼊ISR(in-sync replicas)，并认为它是 死的，不会再加⼊管理中</th>
  </tr>
  <tr>
    <td>replica.lag.max.messages =4000</td>
    <td> </td>
    <td>如果follower落后与leader太多,将会认为 此follower[或者说partition relicas] 已经失效 ##通常,在follower与leader通讯时,因为 ⽹络延迟或者链接断开,总会导致replicas中 消息同步滞后 ##如果消息之后太多,leader将认为此 follower⽹络延迟较⼤或者消息吞吐能⼒有 限,将会把此replicas迁移 ##到其他follower中. ##在broker数量较少,或者⽹络不⾜的环境 中,建议提⾼此值.</td>
  </tr>
  <tr>
    <td>replica.socket.timeout.ms=30*1000</td>
    <td> </td>
    <td>follower与leader之间的socket超时时间</td>
  </tr>
  <tr>
    <td>replica.socket.receive.buffer.byte s=64*1024</td>
    <td> </td>
    <td>leader复制时候的socket缓存⼤⼩</td>
  </tr>
  <tr>
    <td>replica.fetch.max.bytes<br><br>=1024*1024</td>
    <td> </td>
    <td>replicas每次获取数据的最⼤⼤⼩</td>
  </tr>
  <tr>
    <td>replica.fetch.wait.max.ms =500</td>
    <td> </td>
    <td>replicas同leader之间通信的最⼤等待时 间，失败了会重试</td>
  </tr>
  <tr>
    <td>replica.fetch.min.bytes =1</td>
    <td> </td>
    <td>fetch的最⼩数据尺⼨,如果leader中尚未同 步的数据不⾜此值,将会阻塞,直到满⾜条件</td>
  </tr>
  <tr>
    <td>num.replica.fetchers=1</td>
    <td> </td>
    <td>leader进⾏复制的线程数，增⼤这个数值会增 加follower的IO</td>
  </tr>
  <tr>
    <td>replica.high.watermark.checkpoint. interval.ms =5000</td>
    <td> </td>
    <td>每个replica检查是否将最⾼⽔位进⾏固化的 频率</td>
  </tr>
  <tr>
    <td>controlled.shutdown.enable<br><br>=false</td>
    <td> </td>
    <td>是否允许控制器关闭broker ,若是设置为 true,会关闭所有在这个broker上的 leader，并转移到其他broker</td>
  </tr>
  <tr>
    <td>controlled.shutdown.max.retries<br><br>=3</td>
    <td> </td>
    <td>控制器关闭的尝试次数</td>
  </tr>
  <tr>
    <td>controlled.shutdown.retry.backoff. ms =5000</td>
    <td> </td>
    <td>每次关闭尝试的时间间隔</td>
  </tr>
  <tr>
    <td>leader.imbalance.per.broker.percen tage =10</td>
    <td> </td>
    <td>leader的不平衡⽐例，若是超过这个数值，会 对分区进⾏重新的平衡</td>
  </tr>
  <tr>
    <td>leader.imbalance.check.interval.se conds =300</td>
    <td> </td>
    <td>检查leader是否不平衡的时间间隔</td>
  </tr>
  <tr>
    <td>offset.metadata.max.bytes</td>
    <td> </td>
    <td>客户端保留offset信息的最⼤空间⼤⼩</td>
  </tr>
  <tr>
    <td>kafka中zookeeper参数配置</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>zookeeper.connect = localhost:2181</td>
    <td> </td>
    <td>zookeeper集群的地址，可以是多个，多个之 间⽤逗号分割 hostname1:port1,hostname2:port2,ho stname3:port3</td>
  </tr>
  <tr>
    <td>zookeeper.session.timeout.ms=6000</td>
    <td> </td>
    <td>ZooKeeper的最⼤超时时间，就是⼼跳的间 隔，若是没有反映，那么认为已经死了，不易 过⼤</td>
  </tr>
  <tr>
    <td>zookeeper.connection.timeout.ms<br><br>=6000</td>
    <td> </td>
    <td>ZooKeeper的连接超时时间</td>
  </tr>
  <tr>
    <td>zookeeper.sync.time.ms =2000</td>
    <td> </td>
    <td>ZooKeeper集群中leader和follower之间 的同步实际那</td>
  </tr>
</table>


Ø Producer 主要配置

Ø Consumer 主要配置

其它参数参见官⽹

# 2.设计原理

kafka的设计初衷是希望作为⼀个统⼀的信息收集平台,能够实时的收集反馈信息,并需要能够⽀撑较⼤的 数据量,且具备良好的容错能⼒.

- a、持久性

- b、性能

- c、⽣产者


kafka使⽤⽂件存储消息,这就直接决定kafka在性能上严重依赖⽂件系统的本⾝特性.且⽆论任何OS下, 对⽂件系统本⾝的优化⼏乎没有可能.⽂件缓存/直接内存映射等是常⽤的⼿段.因为kafka是对⽇志⽂件 进⾏append操作,因此磁盘检索的开⽀是较⼩的;同时为了减少磁盘写⼊的次数,broker会将消息暂时 buffer起来,当消息的个数(或尺⼨)达到⼀定阀值时,再ﬂush到磁盘,这样减少了磁盘IO调⽤的次数.

需要考虑的影响性能点很多,除磁盘IO之外,我们还需要考虑⽹络IO,这直接关系到kafka的吞吐量问 题.kafka并没有提供太多⾼超的技巧;对于producer端,可以将消息buffer起来,当消息的条数达到⼀定阀值 时,批量发送给broker;对于consumer端也是⼀样,批量fetch多条消息.不过消息量的⼤⼩可以通过配置⽂件 来指定.对于kafka broker端,似乎有个sendﬁle系统调⽤可以潜在的提升⽹络IO的性能:将⽂件的数据映射 到系统内存中,socket直接读取相应的内存区域即可,⽽⽆需进程再次copy和交换. 其实对于 producer/consumer/broker三者⽽⾔,CPU的开⽀应该都不⼤,因此启⽤消息压缩机制是⼀个良好的策略;压 缩需要消耗少量的CPU资源,不过对于kafka⽽⾔,⽹络IO更应该需要考虑.可以将任何在⽹络上传输的消 息都经过压缩.kafka⽀持gzip/snappy等多种压缩⽅式.

负载均衡: producer将会和Topic下所有partition leader保持socket连接;消息由producer直接通过socket发 送到broker,中间不会经过任何"路由层".事实上,消息被路由到哪个partition上,有producer客户端决定.⽐如 可以采⽤"random""key-hash""轮询"等,如果⼀个topic中有多个partitions,那么在producer端实现"消息均衡 分发"是必要的.

其中partition leader的位置(host:port)注册在zookeeper中,producer作为zookeeper client,已经注册了watch ⽤来监听partition leader的变更事件.

异步发送：将多条消息暂且在客户端buffer起来，并将他们批量的发送到broker，⼩数据IO太多，会 拖慢整体的⽹络延迟，批量延迟发送事实上提升了⽹络效率。不过这也有⼀定的隐患，⽐如说当 producer失效时，那些尚未发送的消息将会丢失。

- d、消费者

- e、消息传送机制

- f、复制备份


consumer端向broker发送"fetch"请求,并告知其获取消息的offset;此后consumer将会获得⼀定条数的消 息;consumer端也可以重置offset来重新消费消息.

在JMS实现中,Topic模型基于push⽅式,即broker将消息推送给consumer端.不过在kafka中,采⽤了pull⽅

式,即consumer在和broker建⽴连接之后,主动去pull(或者说fetch)消息;这种模式有些优点,⾸先consumer端 可以根据⾃⼰的消费能⼒适时的去fetch消息并处理,且可以控制消息消费的进度(offset);此外,消费者可以 良好的控制消息消费的数量,batch fetch.

其他JMS实现,消息消费的位置是有prodiver保留,以便避免重复发送消息或者将没有消费成功的消息重 发等,同时还要控制消息的状态.这就要求JMS broker需要太多额外的⼯作.在kafka中,partition中的消息只 有⼀个consumer在消费,且不存在消息状态的控制,也没有复杂的消息确认机制,可见kafka broker端是相当 轻量级的.当消息被consumer接收之后,consumer可以在本地保存最后消息的offset,并间歇性的向 zookeeper注册offset.由此可见,consumer客户端也很轻量级.

对于JMS实现,消息传输担保⾮常直接:有且只有⼀次(exactly once).在kafka中稍有不同:

- 1) at most once: 最多⼀次,这个和JMS中"⾮持久化"消息类似.发送⼀次,⽆论成败,将不会重发.
- 2) at least once: 消息⾄少发送⼀次,如果消息未能接受成功,可能会重发,直到接收成功.
- 3) exactly once: 消息只会发送⼀次. at most once: 消费者fetch消息,然后保存offset,然后处理消息;当client保存offset之后,但是在消息处理过


程中出现了异常,导致部分消息未能继续处理.那么此后"未处理"的消息将不能被fetch到,这就是"at most once".

at least once: 消费者fetch消息,然后处理消息,然后保存offset.如果消息处理成功之后,但是在保存offset 阶段zookeeper异常导致保存操作未能执⾏成功,这就导致接下来再次fetch时可能获得上次已经处理过的 消息,这就是"at least once"，原因offset没有及时的提交给zookeeper，zookeeper恢复正常还是之前offset 状态.

exactly once: kafka中并没有严格的去实现(基于2阶段提交,事务),我们认为这种策略在kafka中是没有必 要的.

通常情况下"at-least-once"是我们⾸选.(相⽐at most once⽽⾔,重复接收数据总⽐丢失数据要好).

kafka将每个partition数据复制到多个server上,任何⼀个partition有⼀个leader和多个follower(可以没 有);备份的个数可以通过broker配置⽂件来设定.leader处理所有的read-write请求,follower需要和leader保 持同步.Follower和consumer⼀样,消费消息并保存在本地⽇志中;leader负责跟踪所有的follower状态,如果 follower"落后"太多或者失效,leader将会把它从replicas同步列表中删除.当所有的follower都将⼀条消息保 存成功,此消息才被认为是"committed",那么此时consumer才能消费它.即使只有⼀个replicas实例存活,仍 然可以保证消息的正常发送和接收,只要zookeeper集群存活即可.(不同于其他分布式存储,⽐如hbase需 要"多数派"存活才⾏)

当leader失效时,需在followers中选取出新的leader,可能此时follower落后于leader,因此需要选择⼀ 个"up-to-date"的follower.选择follower时需要兼顾⼀个问题,就是新leader server上所已经承载的partition leader的个数,如果⼀个server上有过多的partition leader,意味着此server将承受着更多的IO压⼒.在选举新 leader,需要考虑到"负载均衡".

- g.⽇志

- h、分配


如果⼀个topic的名称为"my_topic",它有2个partitions,那么⽇志将会保存在my_topic_0和my_topic_1两 个⽬录中;⽇志⽂件中保存了⼀序列"log entries"(⽇志条⽬),每个log entry格式为"4个字节的数字N表⽰消 息的长度" + "N个字节的消息内容";每个⽇志都有⼀个offset来唯⼀的标记⼀条消息,offset的值为8个字节 的数字,表⽰此消息在此partition中所处的起始位置..每个partition在物理存储层⾯,有多个log ﬁle组成(称 为segment).segment ﬁle的命名为"最⼩offset".kafka.例如"00000000000.kafka";其中"最⼩offset"表⽰此 segment中起始消息的offset.

其中每个partiton中所持有的segments列表信息会存储在zookeeper中. 当segment⽂件尺⼨达到⼀定阀值时(可以通过配置⽂件设定,默认1G),将会创建⼀个新的⽂件;当buffer

中消息的条数达到阀值时将会触发⽇志信息ﬂush到⽇志⽂件中,同时如果"距离最近⼀次ﬂush的时间 差"达到阀值时,也会触发ﬂush到⽇志⽂件.如果broker失效,极有可能会丢失那些尚未ﬂush到⽂件的消息.因 为server意外失败,仍然会导致log⽂件格式的破坏(⽂件尾部),那么就要求当server启动时需要检测最后⼀ 个segment的⽂件结构是否合法并进⾏必要的修复.

获取消息时,需要指定offset和最⼤chunk尺⼨,offset⽤来表⽰消息的起始位置,chunk size⽤来表⽰最⼤ 获取消息的总长度(间接的表⽰消息的条数).根据offset,可以找到此消息所在segment⽂件,然后根据 segment的最⼩offset取差值,得到它在ﬁle中的相对位置,直接读取输出即可.

⽇志⽂件的删除策略⾮常简单:启动⼀个后台线程定期扫描log ﬁle列表,把保存时间超过阀值的⽂件直 接删除(根据⽂件的创建时间).为了避免删除⽂件时仍然有read操作(consumer消费),采取copy-on-write⽅ 式.

kafka使⽤zookeeper来存储⼀些meta信息,并使⽤了zookeeper watch机制来发现meta信息的变更并作出 相应的动作(⽐如consumer失效,触发负载均衡等)

1) Broker node registry: 当⼀个kafka broker启动后,⾸先会向zookeeper注册⾃⼰的节点信息(临时znode), 同时当broker和zookeeper断开连接时,此znode也会被删除.

格式: /broker/ids/[0...N] -->host:port;其中[0..N]表⽰broker id,每个broker的配置⽂件中都需要指定⼀ 个数字类型的id(全局不可重复),znode的值为此broker的host:port信息.

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

6) Partition Owner registry: ⽤来标记partition被哪个consumer消费.临时znode 格式: /consumers/[group_id]/owners/[topic]/[broker_id-partition_id] -->consumer_node_id当consumer启

动时,所触发的操作:

- A) ⾸先进⾏"Consumer id Registry";
- B) 然后在"Consumer id Registry"节点下注册⼀个watch⽤来监听当前group中其他consumer

的"leave"和"join";只要此znode path下节点列表变更,都会触发此group下consumer的负载均衡.(⽐如⼀个 consumer失效,那么其他consumer接管partitions).

- C) 在"Broker id registry"节点下,注册⼀个watch⽤来监听broker的存活情况;如果broker列表变更,将会触


发所有的groups下的consumer重新balance.

- 1) Producer端使⽤zookeeper⽤来"发现"broker列表,以及和Topic下每个partition leader建⽴socket连接并

发送消息.

- 2) Broker端使⽤zookeeper⽤来注册broker信息,已经监测partition leader存活性.
- 3) Consumer端使⽤zookeeper⽤来注册consumer信息,其中包括consumer消费的partition列表等,同时也


⽤来发现broker列表,并和partition leader建⽴socket连接,并获取消息.

- 3. Producer编码

- 4. Consumer编码


