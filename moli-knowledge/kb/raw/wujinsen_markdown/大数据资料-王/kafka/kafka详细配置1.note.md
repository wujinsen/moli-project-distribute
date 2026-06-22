group.id 默认值：⽆ 唯⼀的指明了consumer的group的名字，group名⼀样的进程属于同⼀个consumer group。

zokeper.conect 默认值：⽆ 指定了ZoKeper的conect string，以hostname:port的形式，hostname和port就是ZoKeper集群 各个节点的hostname和port。 ZoKeper集群中的某个节点可能会挂掉，所以可以指定多个节点的 conect string。如下所式： hostname1:port1,hostname2:port2,hostname3:port3.

ZoKeper也可以允许你指定⼀个"chrot"的路径，可以让Kafka集群将需要存储在ZoKeper的数据 存储到指定的路径下这可以让多个Kafka集群或其他应⽤程序公⽤同⼀个ZoKeper集群。可以使⽤如 下的conect string：

hostname1:port1,hostname2:port2,hostname3:port3/chrot/path

consumer.id 默认值：nul 如果没有设置的话则⾃动⽣成。

socket.timeout.ms 默认值：30 * 1 0 socket请求的超时时间。实际的超时时间为max.fetch.wait+ socket.timeout.ms。

socket.receive.bufer.bytes 默认值：64 * 1024 socket的receiver bufer的字节⼤⼩。

fetch.mesage.max.bytes 默认值：1024 *1024 每⼀个获取某个topic的某个partition的请求，得到最⼤的字节数，每⼀个partition的要被读取的数据会 加载⼊内存，所以这可以帮助控制consumer使⽤的内存。这个值的设置不能⼩于在server端设置的最 ⼤消息的字节数，否则producer可能会发送⼤于consumer可以获取的字节数限制的消息。

auto.comit.enable 默认值：true 如果设为true，consumer会定时向ZoKeper发送已经获取到的消息的ofset。当consumer进程挂掉 时，已经提交的ofset可以继续使⽤，让新的consumer继续⼯作。

auto.comit.interval.ms 默认值：60 * 1 0 consumer向ZoKeper发送ofset的时间间隔。

queued.max.mesage.chunks 默认值：10 缓存⽤来消费的消息的chunk的最⼤数量，每⼀个chunk最⼤可以达到fetch.mesage.max.bytes。

rebalance.max.retries 默认值：4 当⼀个新的consumer加⼊⼀个consumer group时，会有⼀个rebalance的操作，导致每⼀个 consumer和partition的关系重新分配。如果这个重分配失败的话，会进⾏重试，此配置就代表最⼤的 重试次数。

fetch.min.bytes 默认值：1 ⼀个fetch请求最少要返回多少字节的数据，如果数据量⽐这个配置少，则会等待，知道有⾜够的数据 为⽌。

fetch.wait.max.ms 默认值：10 在server回应fetch请求前，如果消息不⾜，就是说⼩于fetch.min.bytes时，server最多阻塞的时间。 如果超时，消息将⽴即发送给consumer.。

rebalance.backof.ms 默认值：2 0 在rebalance重试时的backof时间。

refresh.leader.backof.ms 默认值：20 在consumer发现失去某个partition的leader后，在leader选出来前的等待的backof时间。

auto.ofset.reset 默认值：largest 在Consumer在ZoKeper中发现没有初始的ofset时或者发现ofset不在范围呢，该怎么做：

- * smalest : ⾃动把ofset设为最⼩的ofset。
- * largest : ⾃动把ofset设为最⼤的ofset。
- * anything else: 抛出异常。


consumer.timeout.ms 默认值：-1 如果在指定的时间间隔后，没有发现可⽤的消息可消费，则抛出⼀个timeout异常。

client.id 默认值： group id value 每⼀个请求中⽤户⾃定义的client id，可帮助追踪调⽤情况。

zokeper.sesion.timeout.ms 默认值：6 0 ZoKeper的sesion的超时时间，如果在这段时间内没有收到ZK的⼼跳，则会被认为该Kafka server 挂掉了。如果把这个值设置得过低可能被误认为挂掉，如果设置得过⾼，如果真的挂了，则需要很长 时间才能被server得知。

zokeper.conection.timeout.ms 默认值：6 0 client连接到ZK server的超时时间。

zokeper.sync.time.ms 默认值：2 0 ⼀个ZK folower能落后leader多久。

