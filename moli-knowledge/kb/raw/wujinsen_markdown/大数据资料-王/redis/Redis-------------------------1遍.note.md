# http://redis.readthedocs.org/en/latest/

Redis新⼿⼊⻔详解.pdf 525. 6KB

# Redis

Redis是⼀个开源的使⽤ANSI 编写、⽀持⽹络、可基于内存亦可持久化的⽇志型、KeyValue ，并提供多种语⾔的API。从2010年3⽉15⽇起，Redis的开发⼯作由VMware主持。

C语⾔ 数据库

简介

redis是⼀个key-value 。和Memcached类似，它⽀持存储的value类型相对更多，包括 string(字符串)、list( )、set(集合)、zset(sorted set --有序集合)和hash（哈希类型）。这些

存储系统

链表 数 据类型

都⽀持push/pop、add/remove及取交集并集和差集及更丰富的操作，⽽且这些操作都是原 ⼦性的。在此基础上，redis⽀持各种不同⽅式的排序。与memcached⼀样，为了保证效率，数据 都是缓存在内存中。区别的是redis会周期性的把更新的数据写⼊磁盘或者把修改操作写⼊追加的 记录⽂件，并且在此基础上实现了master-slave(主从)同步。

Redis 是⼀个⾼性能的key-value数据库。 redis的出现，很⼤程度补偿了 这类 key/value存储的不⾜，在部 分场合可以对关系数据库起到很好的补充作⽤。它提供了Python， Ruby，Erlang，PHP客户端，使⽤很⽅便。[1]

memcached

2性能

下⾯是官⽅的bench-mark数据： 测试完成了50个并发执⾏100000个请求。 设置和获取的值是⼀个256字节字符串。 Linux box是运⾏Linux 2.6,这是X3320 Xeon 2.5 ghz。 ⽂本执⾏使⽤loopback接⼝(127.0.0.1)。 结果:写的速度是110000次/s,读的速度是81000次/s 。

4常⽤命令

就DB来说，Redis成绩已经很惊⼈了，且不说 和tokyocabinet之流，就说原版的 memcached，速度似乎也只能达到这个级别。Redis根本是使⽤内存 ，持久化的关键是这三 条指令：SAVE BGSAVE LASTSAVE …

memcachedb

存储

连接操作相关的命令

quit：关闭连接（connection） auth：简单密码认证

持久化

save：将数据同步保存到磁盘 bgsave：将数据异步保存到磁盘 lastsave：返回上次成功将数据保存到磁盘的Unix时戳 shundown：将数据同步保存到磁盘，然后关闭服务

远程服务控制

info：提供服务器的信息和统计 monitor：实时转储收到的请求 slaveof：改变复制策略设置 config：在运⾏时配置Redis服务器

对value操作的命令

exists(key)：确认⼀个key是否存在 del(key)：删除⼀个key type(key)：返回值的类型 keys(pattern)：返回满⾜给定pattern的所有key randomkey：随机返回key空间的⼀个 keyrename(oldname, newname)：重命名key dbsize：返回当前数据库中key的数⽬ expire：设定⼀个key的活动时间（s） ttl：获得⼀个key的活动时间 select(index)：按索引查询 move(key, dbindex)：移动当前数据库中的key到dbindex数据库 flushdb：删除当前选择数据库中的所有key flushall：删除所有数据库中的所有key

对String操作的命令

set(key, value)：给数据库中名称为key的string赋予值value get(key)：返回数据库中名称为key的string的value getset(key, value)：给名称为key的string赋予上⼀次的value mget(key1, key2,…, key N)：返回库中多个string的value setnx(key, value)：添加string，名称为key，值为value setex(key, time, value)：向库中添加string，设定过期时间time mset(key N, value N)：批量设置多个string的值 msetnx(key N, value N)：如果所有名称为key i的string都不存在 incr(key)：名称为key的string增1操作 incrby(key, integer)：名称为key的string增加integer decr(key)：名称为key的string减1操作 decrby(key, integer)：名称为key的string减少integer append(key, value)：名称为key的string的值附加value substr(key, start, end)：返回名称为key的string的value的⼦串

对List操作的命令

rpush(key, value)：在名称为key的list尾添加⼀个值为value的元素 lpush(key, value)：在名称为key的list头添加⼀个值为value的 元素 llen(key)：返回名称为key的list的⻓度 lrange(key, start, end)：返回名称为key的list中start⾄end之间的元素 ltrim(key, start, end)：截取名称为key的list lindex(key, index)：返回名称为key的list中index位置的元素 lset(key, index, value)：给名称为key的list中index位置的元素赋值

lrem(key, count, value)：删除count个key的list中值为value的元素

lpop(key)：返回并删除名称为key的list中的⾸元素 rpop(key)：返回并删除名称为key的list中的尾元素 blpop(key1, key2,… key N, timeout)：lpop命令的block版本。 brpop(key1, key2,… key N, timeout)：rpop的block版本。 rpoplpush(srckey, dstkey)：返回并删除名称为srckey的list的尾元素，并将该元素添加到名称为 dstkey的list的头部

对Set操作的命令

sadd(key, member)：向名称为key的set中添加元素member srem(key, member) ：删除名称为key的set中的元素member spop(key) ：随机返回并删除名称为key的set中⼀个元素 smove(srckey, dstkey, member) ：移到集合元素 scard(key) ：返回名称为key的set的基数 sismember(key, member) ：member是否是名称为key的set的元素 sinter(key1, key2,…key N) ：求交集 sinterstore(dstkey, (keys)) ：求交集并将交集保存到dstkey的集合 sunion(key1, (keys)) ：求并集 sunionstore(dstkey, (keys)) ：求并集并将并集保存到dstkey的集合 sdiff(key1, (keys)) ：求差集 sdiffstore(dstkey, (keys)) ：求差集并将差集保存到dstkey的集合 smembers(key) ：返回名称为key的set的所有元素 srandmember(key) ：随机返回名称为key的set的⼀个元素

对Hash操作的命令

hset(key, field, value)：向名称为key的hash中添加元素field hget(key, field)：返回名称为key的hash中field对应的value hmget(key, (fields))：返回名称为key的hash中field i对应的value hmset(key, (fields))：向名称为key的hash中添加元素field hincrby(key, field, integer)：将名称为key的hash中field的value增加integer hexists(key, field)：名称为key的hash中是否存在键为field的域 hdel(key, field)：删除名称为key的hash中键为field的域 hlen(key)：返回名称为key的hash中元素个数 hkeys(key)：返回名称为key的hash中所有键 hvals(key)：返回名称为key的hash中所有键对应的value

### hgetall(key)：返回名称为key的hash中所有的键（field）及其对应的value

## 5存储

redis使⽤了两种 ：全量数据和增量请求。全量数据格式是把内存中的数据写⼊磁盘，

⽂件格式

便于下次读取⽂件进⾏加载；增量请求⽂件则是把内存中的数据序列化为操作请求，⽤于读取⽂ 件进⾏replay得到数据，序列化的操作包括SET、RPUSH、SADD、ZADD。

redis的存储分为内存存储、 和log⽂件三部分，配置⽂件中有三个参数对其进⾏配置。

磁盘存储

save seconds updates，save配置，指出在多⻓时间内，有多少次更新操作，就将 到数 据⽂件。这个可以多个条件配合，⽐如默认配置⽂件中的设置，就设置了三个条件。

数据同步

appendonly yes/no ，appendonly配置，指出是否在每次更新操作后进⾏⽇志记录，如果不开启， 可能会在断电时导致⼀段时间内的数据丢失。因为redis本身同步数据⽂件是按上⾯的save条件来 同步的，所以有的数据会在⼀段时间内只存在于内存中。

appendfsync no/always/everysec ，appendfsync配置，no表示等 进⾏ 同步到磁 盘，always表示每次更新操作后⼿动调⽤fsync()将数据写到磁盘，everysec表示每秒同步⼀次。

操作系统 数据缓存

## 6安装

获取源码、解压、进⼊源码⽬录 使⽤wget⼯具等下载： wget （百度不让⽤链接） tar xzf redis-1.2.6.tar.gz cd redis-1.2.6。 编译⽣成可执⾏⽂件 由于makefile⽂件已经写好，我们只需要直接在源码⽬录执⾏make命令进⾏编译即可： make make命令执⾏完成后，会在当前⽬录下⽣成本个 ，分别是redis-server、redis-cli、 redis-benchmark、redis-stat，它们的作⽤如下： redis-server：Redis服务器的daemon启动程序 redis-cli：Redis命令⾏操作⼯具。当然，你也可以⽤telnet根据其纯⽂本协议来操作 redis-benchmark：Redis ⼯具，测试Redis在你的系统及你的配置下的读写性能 redis-stat：Redis状态检测⼯具，可以检测Redis当前状态参数及延迟状况。

可执⾏⽂件

性能测试

建⽴Redis⽬录（⾮必须） 这个过程不是必须的，只是为了将Redis相关的 ⽽进⾏的操作。 执⾏以下命令建⽴相关⽬录并拷⻉相关⽂件⾄⽬录中： sudo -s mkdir -p /usr/local/redis/bin mkdir -p /usr/local/redis/etc mkdir -p /usr/local/redis/var cp redis-server redis-cli redis-benchmark redis-stat /usr/local/redis/bin/ cp redis.conf /usr/local/redis/etc/ 配置参数

资源统⼀管理

在我们成功安装Redis后，我们直接执⾏redis-server即可运⾏Redis，此时它是按照默认配置来运 ⾏的（默认配置甚⾄不是 运⾏）。我们希望Redis按我们的要求运⾏，则我们需要修改配置⽂ 件，Redis的配置⽂件就是我们上⾯第⼆个cp操作的redis.conf⽂件，它被我们拷⻉到 了/usr/local/redis/etc/⽬录下。修改它就可以配置我们的server了。如何修改？下⾯是redis.conf的 主要配置参数的意义：

后台

daemonize：是否以 daemon⽅式运⾏ pidfile：pid⽂件位置 port：监听的端⼝号 timeout：请求超时时间 loglevel：log信息级别 logfile：log⽂件位置 databases：开启数据库的数量 save * *：保存 的频率，第⼀个*表示多⻓时间，第三个*表示执⾏多少次写操作。在⼀定时间 内执⾏⼀定数量的写操作时，⾃动保存 。可设置多个条件。 rdbcompression：是否使⽤压缩 dbfilename：数据 ⽂件名（只是⽂件名，不包括⽬录） dir：数据 的保存⽬录（这个是⽬录）

后台

快照

快照

快照 快照

appendonly：是否开启appendonlylog，开启的话每次写操作会记⼀条log，这会提⾼数据抗⻛险 能⼒，但影响效率。

appendfsync：appendonlylog如何同步到磁盘（三个选项，分别是每次写都强制调⽤fsync、每秒 启⽤⼀次fsync、不调⽤fsync等待系统⾃⼰同步）

下⾯是⼀个略做修改后的配置⽂件内容： daemonize yes pidfile /usr/local/redis/var/redis.pid port 6379 timeout 300 loglevel debug logfile /usr/local/redis/var/redis.log databases 16 save 900 1 save 300 10 save 60 10000 rdbcompression yes dbfilename dump.rdb dir /usr/local/redis/var/ appendonly no appendfsync always glueoutputbuf yes shareobjects no shareobjectspoolsize 1024 将上⾯内容写为redis.conf并保存到/usr/local/redis/etc/⽬录下 然后在命令⾏执⾏： /usr/local/redis/bin/redis-server /usr/local/redis/etc/redis.conf 即可在后台启动redis服务，这时你通过

telnet 6379 即可连接到你的redis服务 Redis常⽤内存优化⼿段与参数 [2]通过我们上⾯的⼀些实现上的分析可以看出redis实际上的内存管理成本⾮常⾼，即占⽤了过多 的内存，作者对这点也⾮常清楚，所以提供了⼀系列的参数和⼿段来控制和节省内存，我们分别 来讨论下。

127.0.0.1

⾸先最重要的⼀点是不要开启Redis的VM选项，即虚拟内存功能，这个本来是作为Redis存储 超出物理内存数据的⼀种数据在内存与磁盘换⼊换出的⼀个持久化策略，但是其内存管理成本也 ⾮常的⾼，并且我们后续会分析此种持久化策略并不成熟，所以要关闭VM功能，请检查你的 redis.conf⽂件中 vm-enabled 为 no。

其次最好设置下redis.conf中的maxmemory选项，该选项是告诉Redis当使⽤了多少物理内存 后就开始拒绝后续的写⼊请求，该参数能很好的保护好你的Redis不会因为使⽤了过多的物理内存 ⽽导致swap,最终严重影响性能甚⾄崩溃。

另外Redis为不同数据类型分别提供了⼀组参数来控制内存使⽤，我们在前⾯详细分析过 Redis Hash是value内部为⼀个HashMap，如果该Map的成员数⽐较少，则会采⽤类似⼀维线性的 紧凑格式来存储该Map, 即省去了⼤量指针的内存开销，这个参数控制对应在redis.conf配置⽂件中 下⾯2项：

- 1.
- 2.
- 3.


hash-max-zipmap-entries 64 hash-max-zipmap-value 512

hash-max-zipmap-entries含义是当value这个Map内部不超过多少个成员时会采⽤线性紧凑格式存 储，默认是64,即value内部有64个以下的成员就是使⽤线性紧凑存储，超过该值⾃动转成真正的 HashMap。

hash-max-zipmap-value 含义是当 value这个Map内部的每个成员值⻓度不超过多少字节就会 采⽤线性紧凑存储来节省空间。

以上2个条件任意⼀个条件超过设置值都会转换成真正的HashMap，也就不会再节省内存了， 那么这个值是不是设置的越⼤越好呢，答案当然是否定的，HashMap的优势就是查找和操作的时 间复杂度都是O(1)的，⽽放弃Hash采⽤⼀维存储则是O(n)的时间复杂度，如果

成员数量很少，则影响不⼤，否则会严重影响性能，所以要权衡好这个值的设置，总体上还 是最根本的时间成本和空间成本上的权衡。 同样类似的参数 [2]list-max-ziplist-entries 512

说明：list数据类型多少节点以下会采⽤去指针的紧凑存储格式。 list-max-ziplist-value 64 说明：list数据类型节点值⼤⼩⼩于多少字节会采⽤紧凑存储格式。 set-max-intset-entries 512 说明：set数据类型内部数据如果全部是数值型，且包含多少节点以下会采⽤紧凑格式存储。

最后想说的是Redis内部实现没有对内存分配⽅⾯做过多的优化，在⼀定程度上会存在内存碎 ⽚，不过⼤多数情况下这个不会成为Redis的性能瓶颈，不过如果在Redis内部存储的⼤部分数据 是数值型的话，Redis内部采⽤了⼀个shared integer的⽅式来省去分配内存的开销，即在系统启 动时先分配⼀个从1~n 那么多个数值对象放在⼀个池⼦中，如果存储的数据恰好是这个数值范围 内的数据，则直接从池⼦⾥取出该对象，并且通过引⽤计数的⽅式来共享，这样在系统存储了⼤ 量数值下，也能⼀定程度上节省内存并且提⾼性能，这个参数值n的设置需要修改源代码中的⼀⾏ 宏定义REDIS_SHARED_INTEGERS，该值默认是10000，可以根据⾃⼰的需要进⾏修改，修改 后重新编译就可以了。

另外redis 的6种过期策略[2]redis 中的默认的过期策略是volatile-lru 。设置⽅式 config set maxmemory-policy volatile-lru maxmemory-policy 六种⽅式 volatile-lru：只对设置了过期时间的key进⾏LRU（默认值）

allkeys-lru ： 是从所有key⾥ 删除 不经常使⽤的key volatile-random：随机删除即将过期key allkeys-random：随机删除 volatile-ttl ： 删除即将过期的 noeviction ： 永不过期，返回错误 maxmemory-samples 3 是说每次进⾏淘汰的时候 会随机抽取3个key 从⾥⾯淘汰最不经常使

⽤的（默认选项）

## 7版本发布

2013年11⽉25⽇，Redis 2.8.1发布。[3] 2013年4⽉30⽇Redis 2.6.13 发布，⾼性能K/V服务器[7] 2012年11⽉7⽇ Redis 2.6.3 发布，⾼性能K/V服务器[6] 2012年08⽉31⽇ ，Redis 2.4.17 ⼩更新版本 NoSQL。[5] 2012年08⽉02⽇，Redis 2.4.16 ⼩更新版本 NoSQL。[4]

