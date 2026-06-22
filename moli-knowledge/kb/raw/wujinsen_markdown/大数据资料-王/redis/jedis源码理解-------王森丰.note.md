- 1、redis是什么？ ⼀个缓存，内存操作

- 2、jedis是什么？ redis的java客户端，⽤于操作redis

- 3、jedis如何与redis交互？ 遵循redis协议，使⽤scoket连接，操作redis

- 4、jedis的连接池是怎么回事？ 通过实现comon包中的GenericObjectPol创建连接池，通过实现BasePolableObjectFactory⽣产 链接，从⽽达到创建连接池的效果

- 5、jedis如何操作？ set：jedis将传⼊的key，value和comand封装，通过scoket创建连接，⽤outputStream将命令打 出，⽤inputStream获取返回的标记 get：jedis将传⼊的key，value和comand封装，通过scoket创建连接，⽤outputStream将命令打 出，⽤inputStream获取返回的标记

- 6、shardJedis是怎么回事？ 所谓的分布式redis，是jedis客户端实现的⼀种模式。启动n台redis，他们就在那提供服务，不会⾃⼰ 做集群。 那jedis是如何实现的呢？ 创建shardJedisPol的时候，客户端将传进来的n台机器，⽤每个机器的机器名做了⼀致性hash，并给 每个机器创建160*weight个虚拟节点，虚拟节点也是将名字+i做hash， 然后将这些hash值作为key存到⼀个TreMap中，即nodes，TreMap是有序的。 为什么做虚拟节点？ 因为不做虚拟节点时，可能会出现集群中的某⼀个节点频繁被命中，⽽其他节点则没有⼯作量，造成 数据不均衡，增加了虚拟节点，数据会均衡很多。 这就是jedis的均衡性。 TreMap每个key的value存什么呢？ 存放的是对应的机器的信息bean，即JedisShardInfo，这样不难看出，如果以权重weight为1为例，那 么每个机器会创建160个节点（真实+虚拟），


如果是单⾏回复，那么第⼀个字节是「+」

如果回复的内容是错误信息，那么第⼀个字节是「 _ 」

如果回复的内容是⼀个整型数字，那么第⼀个字节是「:」

如果是bulk回复，那么第⼀个字节是「$」

如果是multi-bulk回复，那么第⼀个字节是「*」

这样如果有4个实体机安装了redis，那么在nodes中就有160*4条记录，其中每160条对应⼀个相同的 JedisShardInfo。 jedis同时⼜维护了⼀个Map<ShardInfo<R>, R> 即resources，这个map的key存放JedisShardInfo实 体，value存的是通过这个实体JedisShardInfo 中的ip：port创 建的jedis，所以，以上⾯的例⼦为例，resources中信息的条数就是4。 这样，当客户端⽤set（key，value）时，jedis将客户端的key同样⽤⼀致性hash取hash值，然后到 nodes中获取⽐key⼀致性hash出来的值⼤的值， 去第⼀个，即所谓的向右移，这样会获取⼀个JedisShardInfo，通过JedisShardInfo到resources中获 取jedis返回，这样客户端就可以通过这个jedis 做增删改查的操作了。

- 7、⼀致性hash是怎么回事？ ⼀致性hash是⼀个算法，简单来说就是将你所需要的值（如key：lilei）取hash值，然后与2^23取余 数，得到的值。如lilei取hash是37184759， 那么，37184759/2^23=376，这个376就是⼀致性hash取出来的值。这个2^23是什么意思呢？它的 值为25*25*25*25，不难发现，这个值 是⽹络中最⼤的ip数，即⼀个⽹络中最多的机器数。之所以去这个数，是为了当需要添加或者删除机器 的时候，不⾄于让其他节点失效，这就是⼀致性。


