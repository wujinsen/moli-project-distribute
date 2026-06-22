# zokeper连接服务器地址 zokeper.conect=zk01 2181,zk02 2181,zk03 2181

# zokeper的sesion过期时间，默认5 0ms，⽤于检测消费者是否挂掉 zokeper.sesion.timeout.ms=5 0

#当消费者挂掉，其他消费者要等该指定时间才能检查到并且触发重新负载均衡 zokeper.conection.timeout.ms=1 0

# 指定多久消费者更新ofset到zokeper中。注意ofset更新时基于time⽽不是每次获得的消息。⼀旦 在更新zokeper发⽣异常并重启，将可能拿到已拿到过的消息 zokeper.sync.time.ms=2 0

#指定消费组 group.id= x

# 当consumer消费⼀定量的消息之后,将会⾃动向zokeper提交ofset信息 # 注意ofset信息并不是每消费⼀次消息就向zk提交⼀次,⽽是现在本地保存(内存),并定期提交,默认为 true auto.comit.enable=true

# ⾃动更新时间。默认60 * 1 0 auto.comit.interval.ms=1 0

# 当前consumer的标识,可以设定,也可以有系统⽣成,主要⽤来跟踪消息消费情况,便于观察 conusmer.id= x

# 消费者客户端编号，⽤于区分不同客户端，默认客户端程序⾃动产⽣ client.id= x

# 最⼤取多少块缓存到消费者(默认10) queued.max.mesage.chunks=50

# 当有新的consumer加⼊到group时,将会reblance,此后将会有partitions的消费端迁移到新 的 consumer上,如果⼀个consumer获得了某个partition的消费权限,那么它将会向zk注 册 "Partition Owner registry"节点信息,但是有可能此时旧的consumer尚没有释放此节点, 此值⽤于控 制,注册节点的重试次数.

rebalance.max.retries=5

# 获取消息的最⼤尺⼨,broker不会像consumer输出⼤于此值的消息chunk 每次feth将得到多条消息,此 值为总⼤⼩,提升此值,将会消耗更多的consumer端内存 fetch.min.bytes=65360

# 当消息的尺⼨不⾜时,server阻塞的时间,如果超时,消息将⽴即发送给consumer fetch.wait.max.ms=5 0 socket.receive.bufer.bytes=65360

# 如果zokeper没有ofset值或ofset值超出范围。那么就给个初始的ofset。有smalest、largest、 anything可选，分别表示给当前最⼩的ofset、当前最⼤的ofset、抛异常。默认largest auto.ofset.reset=smalest

# 指定序列化处理类 derializer.clas=kafka.serializer.DefaultDecoder

