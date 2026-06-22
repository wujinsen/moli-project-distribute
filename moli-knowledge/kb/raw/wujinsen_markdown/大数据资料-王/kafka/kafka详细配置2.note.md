kafka的配置分为 broker、producter、consumer三个不同的配置 ⼀ BROKER 的全局配置 最为核⼼的三个配置 broker.id、log.dir、zokeper.conect 。

- 系统 相关 -

#每⼀个broker在集群中的唯⼀标⽰，要求是正数。在改变IP地址，不改变broker.id的话不会影响 consumers broker.id = 1

#kafka数据的存放地址，多个地址的话⽤逗号分割 /tmp/kafka-logs-1，/tmp/kafka-logs-2 log.dirs = /tmp/kafka-logs

#提供给客户端响应的端⼜ port = 67

#消息体的最⼤⼤⼩，单位是字节 mesage.max.bytes = 1 0

# broker 处理消息的最⼤线程数，⼀般情况下不需要去修改 num.network.threads = 3

# broker处理磁盘IO 的线程数 ，数值应该⼤于你的硬盘数 num.io.threads = 8

# ⼀些后台任务处理的线程数，例如过期消息⽂件的删除等，⼀般情况下不需要去做修改 background.threads = 4

# 等待IO线程处理的请求队列最⼤数，若是等待IO的请求超过这个数值，那么会停⽌接受外部消息， 算是⼀种⾃我保护机制 queued.max.requests = 50

#broker的主机地址，若是设置了，那么会绑定到这个地址上，若是没有，会绑定到所有的接⼜上， 并将其中之⼀发送到ZK，⼀般不设置 host.name

# 打⼴告的地址，若是设置的话，会提供给producers, consumers,其他broker连接，具体如何使⽤还 未深究 advertised.host.name

# ⼴告地址端⼜，必须不同于port中的设置 advertised.port

# socket的发送缓冲区，socket的调优参数SO_SNDBUF socket.send.bufer.bytes = 10 * 1024

# socket的接受缓冲区，socket的调优参数SO_RCVBUF socket.receive.bufer.bytes = 10 * 1024

# socket请求的最⼤数值，防⽌server OM，mesage.max.bytes必然要⼩于 socket.request.max.bytes，会被topic创建时的指定参数覆盖 socket.request.max.bytes = 10 * 1024 * 1024

- LOG 相关 -

# topic的分区是以⼀堆segment⽂件存储的，这个控制每个segment的⼤⼩，会被topic创建时的指 定参数覆盖 log.segment.bytes = 1024 * 1024 * 1024

# 这个参数会在⽇志segment没有达到log.segment.bytes设置的⼤⼩，也会强制新建⼀个segment 会被 topic创建时的指定参数覆盖 log.rol.hours = 24*7

# ⽇志清理策略 选择有：delete和compact 主要针对过期数据的处理，或是⽇志⽂件达到限制的额 度，会被 topic创建时的指定参数覆盖 log.cleanup.policy = delete

# 数据存储的最⼤时间 超过这个时间 会根据log.cleanup.policy设置的策略处理数据，也就是消费端 能够多久去消费数据

# log.retention.bytes和log.retention.minutes任意⼀个达到要求，都会执⾏删除，会被topic创建时的 指定参数覆盖 log.retention.minutes=7 days

# topic每个分区的最⼤⽂件⼤⼩，⼀个topic的⼤⼩限制 = 分区数*log.retention.bytes 。-1 没有⼤⼩ 限制

# log.retention.bytes和log.retention.minutes任意⼀个达到要求，都会执⾏删除，会被topic创建时的 指定参数覆盖

log.retention.bytes=-1

# ⽂件⼤⼩检查的周期时间，是否处罚 log.cleanup.policy中设置的策略 log.retention.check.interval.ms=5 minutes

# 是否开启⽇志压缩 log.cleaner.enable=false

# ⽇志压缩运⾏的线程数 log.cleaner.threads =1

# ⽇志压缩时候处理的最⼤⼤⼩ log.cleaner.io.max.bytes.per.second=None

# ⽇志压缩去重时候的缓存空间 ，在空间允许的情况下，越⼤越好 log.cleaner.dedupe.bufer.size=50*1024*1024

# ⽇志清理时候⽤到的IO块⼤⼩ ⼀般不需要修改 log.cleaner.io.bufer.size=512*1024

# ⽇志清理中hash表的扩⼤因⼦ ⼀般不需要修改 log.cleaner.io.bufer.load.factor = 0.9

# 检查是否处罚⽇志清理的间隔 log.cleaner.backof.ms =15 0

# ⽇志清理的频率控制，越⼤意味着更⾼效的清理，同时会存在⼀些空间上的浪费，会被topic创建时 的指定参数覆盖 log.cleaner.min.cleanable.ratio=0.5

# 对于压缩的⽇志保留的最长时间，也是客户端消费消息的最长时间，同log.retention.minutes的区 别在于⼀个控制未压缩数据，⼀个控制压缩后的数据。会被topic创建时的指定参数覆盖 log.cleaner.delete.retention.ms = 1 day

# 对于segment⽇志的索引⽂件⼤⼩限制，会被topic创建时的指定参数覆盖 log.index.size.max.bytes = 10 * 1024 * 1024

# 当执⾏⼀个fetch操作后，需要⼀定的空间来扫描最近的ofset⼤⼩，设置越⼤，代表扫描速度越 快，但是也更好内存，⼀般情况下不需要搭理这个参数 log.index.interval.bytes = 4096

# log⽂件"sync"到磁盘之前累积的消息条数 # 因为磁盘IO操作是⼀个慢操作,但又是⼀个"数据可靠性"的必要⼿段 # 所以此参数的设置,需要在"数据可靠性"与"性能"之间做必要的权衡. # 如果此值过⼤,将会导致每次"fsync"的时间较长(IO阻塞) # 如果此值过⼩,将会导致"fsync"的次数较多,这也意味着整体的client请求有⼀定的延迟. # 物理server故障,将会导致没有fsync的消息丢失.

log.flush.interval.mesages=None

# 检查是否需要固化到硬盘的时间间隔 log.flush.scheduler.interval.ms = 3 0

# 仅仅通过interval来控制消息的磁盘写⼊时机,是不⾜的. # 此参数⽤于控制"fsync"的时间间隔,如果消息量始终没有达到阀值,但是离上⼀次磁盘同步的时间间

隔 # 达到阀值,也将触发. log.flush.interval.ms = None

# ⽂件在索引中清除后保留的时间 ⼀般不需要去修改 log.delete.delay.ms = 6 0

# 控制上次固化硬盘的时间点，以便于数据恢复 ⼀般不需要去修改 log.flush.ofset.checkpoint.interval.ms =6 0

- TOPIC 相关 # 是否允许⾃动创建topic ，若是false，就需要通过命令创建topic

auto.create.topics.enable =true

# ⼀个topic ，默认分区的replication个数 ，不得⼤于集群中broker的个数 default.replication.factor =1

# 每个topic的分区个数，若是在topic创建时候没有指定的话 会被topic创建时的指定参数覆盖 num.partitions = 1

实例 -replication-factor 3-partitions 1-topic replicated-topic ：名称replicated-topic有⼀个分 区，分区被复制到三个broker上。

- 复制(Leader、replicas) 相关 -

-

# partition leader与replicas之间通讯时,socket的超时时间 controler.socket.timeout.ms = 3 0

# partition leader与replicas数据同步时,消息的队列尺⼨ controler.mesage.queue.size=10

# replicas响应partition leader的最长等待时间，若是超过这个时间，就将replicas列⼊ISR(in-sync replicas)，并认为它是死的，不会再加⼊管理中 replica.lag.time.max.ms = 1 0

# 如果folower落后与leader太多,将会认为此folower[或者说partition relicas]已经失效 # 通常,在folower与leader通讯时,因为⽹络延迟或者链接断开,总会导致replicas中消息同步滞后 # 如果消息之后太多,leader将认为此folower⽹络延迟较⼤或者消息吞吐能⼒有限,将会把此replicas

迁移 # 到其他folower中. # 在broker数量较少,或者⽹络不⾜的环境中,建议提⾼此值.

replica.lag.max.mesages = 4 0

#folower与leader之间的socket超时时间 replica.socket.timeout.ms= 30 * 1 0

# leader复制时候的socket缓存⼤⼩ replica.socket.receive.bufer.bytes=64 * 1024

# replicas每次获取数据的最⼤⼤⼩ replica.fetch.max.bytes = 1024 * 1024

# replicas同leader之间通信的最⼤等待时间，失败了会重试 replica.fetch.wait.max.ms = 50

# fetch的最⼩数据尺⼨,如果leader中尚未同步的数据不⾜此值,将会阻塞,直到满⾜条件 replica.fetch.min.bytes =1

# leader 进⾏复制的线程数，增⼤这个数值会增加folower的IO num.replica.fetchers=1

# 每个replica检查是否将最⾼⽔位进⾏固化的频率 replica.high.watermark.checkpoint.interval.ms = 5 0

# 是否允许控制器关闭broker ,若是设置为true,会关闭所有在这个broker上的leader，并转移到其他 broker controled.shutdown.enable = false

# 控制器关闭的尝试次数 controled.shutdown.max.retries = 3

# 每次关闭尝试的时间间隔 controled.shutdown.retry.backof.ms = 5 0

# 是否⾃动平衡broker之间的分配策略 auto.leader.rebalance.enable = false

# leader的不平衡⽐例，若是超过这个数值，会对分区进⾏重新的平衡 leader.imbalance.per.broker.percentage = 10

# 检查leader是否不平衡的时间间隔 leader.imbalance.check.interval.seconds = 30

# 客户端保留ofset信息的最⼤空间⼤⼩ ofset.metadata.max.bytes

- ZoKeper 相关 -

-

#zokeper集群的地址，可以是多个，多个之间⽤逗号分割 hostname1:port1,hostname2:port2,hostname3:port3 zokeper.conect = localhost:2181

# ZoKeper的最⼤超时时间，就是⼼跳的间隔，若是没有反映，那么认为已经死了，不易过⼤ zokeper.sesion.timeout.ms=6 0

# ZoKeper的连接超时时间 zokeper.conection.timeout.ms = 6 0

# ZoKeper集群中leader和folower之间的同步实际那 zokeper.sync.time.ms = 2 0 配置的修改 其中⼀部分配置是可以被每个topic⾃⾝的配置所代替，例如 新增配置 bin/kafka-topics.sh-zokeper localhost:2181-create-topic my-topic-partitions 1replication-factor 1-config max.mesage.bytes=64 0-config flush.mesages=1

修改配置 bin/kafka-topics.sh-zokeper localhost:2181-alter-topic my-topic-config max.mesage.bytes=128 0

删除配置 ： bin/kafka-topics.sh-zokeper localhost:2181-alter-topic my-topic-deleteConfig max.mesage.bytes ⼆ CONSUMER 配置 最为核⼼的配置是group.id、zokeper.conect

# Consumer归属的组ID，broker是根据group.id来判断是队列模式还是发布订阅模式，⾮常重要 group.id

# 消费者的ID，若是没有设置的话，会⾃增 consumer.id

# ⼀个⽤于跟踪调查的ID ，最好同group.id相同 client.id = group id value

# 对于zokeper集群的指定，可以是多个 hostname1:port1,hostname2:port2,hostname3:port3 必 须和broker使⽤同样的zk配置

zokeper.conect=localhost:2182

# zokeper的⼼跳超时时间，查过这个时间就认为是dead消费者 zokeper.sesion.timeout.ms = 6 0

# zokeper的等待连接时间 zokeper.conection.timeout.ms = 6 0

# zokeper的folower同leader的同步时间 zokeper.sync.time.ms = 2 0

# 当zokeper中没有初始的ofset时候的处理⽅式 。smalest ：重置为最⼩值 largest:重置为最⼤值 anything else：抛出异常

auto.ofset.reset = largest

# socket的超时时间，实际的超时时间是：max.fetch.wait + socket.timeout.ms. socket.timeout.ms= 30 * 1 0

# socket的接受缓存空间⼤⼩ socket.receive.bufer.bytes=64 * 1024

#从每个分区获取的消息⼤⼩限制 fetch.mesage.max.bytes = 1024 * 1024

# 是否在消费消息后将ofset同步到zokeper，当Consumer失败后就能从zokeper获取最新的 ofset

auto.comit.enable = true

# ⾃动提交的时间间隔 auto.comit.interval.ms = 60 * 1 0

# ⽤来处理消费消息的块，每个块可以等同于fetch.mesage.max.bytes中数值 queued.max.mesage.chunks = 10

# 当有新的consumer加⼊到group时,将会reblance,此后将会有partitions的消费端迁移到新 # 的consumer上,如果⼀个consumer获得了某个partition的消费权限,那么它将会向zk注册 # "Partition Owner registry"节点信息,但是有可能此时旧的consumer尚没有释放此节点, # 此值⽤于控制,注册节点的重试次数.

rebalance.max.retries = 4

# 每次再平衡的时间间隔 rebalance.backof.ms = 2 0

# 每次重新选举leader的时间 refresh.leader.backof.ms

# server发送到消费端的最⼩数据，若是不满⾜这个数值则会等待，知道满⾜数值要求 fetch.min.bytes = 1

# 若是不满⾜最⼩⼤⼩(fetch.min.bytes)的话，等待消费端请求的最长等待时间 fetch.wait.max.ms = 10

# 指定时间内没有消息到达就抛出异常，⼀般不需要改

consumer.timeout.ms = -1 三 PRODUCER 的配置 ⽐较核⼼的配置：metadata.broker.list、request.required.acks、producer.type、serializer.clas

# 消费者获取消息元信息(topics, partitions and replicas)的地址,配置格式是： host1:port1,host2:port2，也可以在外⾯设置⼀个vip

metadata.broker.list

#消息的确认模式

- # 0：不保证消息的到达确认，只管发送，低延迟但是会出现消息的丢失，在某个server失败的情况

下，有点像TCP

- # 1：发送消息，并会等待leader 收到确认后，⼀定的可靠性 # -1：发送消息，等待leader收到确认，并进⾏复制操作后，才返回，最⾼的可靠性


request.required.acks = 0

# 消息发送的最长等待时间 request.timeout.ms = 1 0

# socket的缓存⼤⼩ send.bufer.bytes=10*1024

# key的序列化⽅式，若是没有设置，同serializer.clas key.serializer.clas

# 分区的策略，默认是取模 partitioner.clas=kafka.producer.DefaultPartitioner

# 消息的压缩模式，默认是none，可以有gzip和snapy compresion.codec = none

# 可以针对默写特定的topic进⾏压缩 compresed.topics=nul

# 消息发送失败后的重试次数 mesage.send.max.retries = 3

# 每次失败后的间隔时间 retry.backof.ms = 10

# ⽣产者定时更新topic元信息的时间间隔 ，若是设置为0，那么会在每个消息发送后都去更新数据 topic.metadata.refresh.interval.ms = 60 * 1 0

# ⽤户随意指定，但是不能重复，主要⽤于跟踪记录消息 client.id="

- 消息模式 相关 -

-

# ⽣产者的类型 async:异步执⾏消息的发送 sync：同步执⾏消息的发送 producer.type=sync

# 异步模式下，那么就会在设置的时间缓存消息，并⼀次性发送 queue.bufering.max.ms = 5 0

# 异步的模式下 最长等待的消息数 queue.bufering.max.mesages = 1 0

# 异步模式下，进⼊队列的等待时间 若是设置为0，那么要么进⼊队列，要么直接抛弃 queue.enqueue.timeout.ms = -1

# 异步模式下，每次发送的最⼤消息数，前提是触发了queue.bufering.max.mesages或是 queue.bufering.max.ms的限制

batch.num.mesages=20

# 消息体的系列化处理类 ，转化为字节流进⾏传输

# serializer.clas = kafka.serializer.DefaultEncoder 更多的配置可见 kafka.producer.ProducerConfig

