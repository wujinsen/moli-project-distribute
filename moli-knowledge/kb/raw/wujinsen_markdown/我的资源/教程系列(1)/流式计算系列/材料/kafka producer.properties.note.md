#指定kafka节点列表，⽤于获取metadata，不必全部指定 metadata.broker.list=kafka01 9092,kafka02 9092

# 指定分区处理类。默认kafka.producer.DefaultPartitioner，表通过key哈希到对应分区 #partitioner.clas=kafka.producer.DefaultPartitioner

# 是否压缩，默认0表示不压缩，1表示⽤gzip压缩，2表示⽤snapy压缩。压缩后消息中会有头来指明 消息压缩类型，故在消费者端消息解压是透明的⽆需指定。 compresion.codec=none

# 指定序列化处理类 serializer.clas=kafka.serializer.DefaultEncoder

# 如果要压缩消息，这⾥指定哪些topic要压缩消息，默认empty，表示不压缩。 #compresed.topics=

# 设置发送数据是否需要服务端的反馈,有三个值0,1,-1

- # 0: producer不会等待broker发送ack
- # 1: 当leader接收到消息之后发送ack # -1: 当所有的folower都同步消息成功后发送ack. request.required.acks=0


# 在向producer发送ack之前,broker允许等待的最⼤时间 ，如果超时,broker将会向producer发送⼀个 eror ACK.意味着上⼀次消息因为某种原因未能成功(⽐如folower未能同步成功) request.timeout.ms=1 0

# 同步还是异步发送消息，默认“sync”表同步，"async"表异步。异步可以提⾼发送吞吐量, 也意味着消息将会在本地bufer中,并适时批量发送，但是也可能导致丢失未发送过去的消息 producer.type=sync

# 在async模式下,当mesage被缓存的时间超过此值后,将会批量发送给broker,默认为5 0ms # 此值和batch.num.mesages协同⼯作. queue.bufering.max.ms = 5 0

# 在async模式下,producer端允许bufer的最⼤消息量 # ⽆论如何,producer都⽆法尽快的将消息发送给broker,从⽽导致消息在producer端⼤量沉积 # 此时,如果消息的条数达到阀值,将会导致producer端阻塞或者消息被抛弃，默认为1 0

queue.bufering.max.mesages=2 0

# 如果是异步，指定每次批量发送数据量，默认为20 batch.num.mesages=50

# 当消息在producer端沉积的条数达到"queue.bufering.max.mesages"后 # 阻塞⼀定时间后,队列仍然没有enqueue(producer仍然没有发送出任何消息) # 此时producer可以继续阻塞或者将消息抛弃,此timeout值⽤于控制"阻塞"的时间 # -1: ⽆阻塞超时限制,消息不会被抛弃 # 0:⽴即清空队列,消息被抛弃 queue.enqueue.timeout.ms=-1

# 当producer接收到eror ACK,或者没有接收到ACK时,允许消息重发的次数 # 因为broker并没有完整的机制来避免消息重复,所以当⽹络异常时(⽐如ACK丢失) # 有可能导致broker接收到重复的消息,默认值为3. mesage.send.max.retries=3

# producer刷新topic metada的时间间隔,producer需要知道partition leader的位置,以及当前topic的情 况 # 因此producer需要⼀个机制来获取最新的metadata,当producer遇到特定错误时,将会⽴即刷新 # (⽐如topic失效,partition丢失,leader失效等),此外也可以通过此参数来配置额外的刷新机制，默认值 6 0 topic.metadata.refresh.interval.ms=6 0

