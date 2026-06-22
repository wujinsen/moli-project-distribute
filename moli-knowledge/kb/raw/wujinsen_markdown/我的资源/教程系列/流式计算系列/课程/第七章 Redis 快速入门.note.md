Redis概述 Redis是⼀个开源，先进的key-value存储，并⽤于构建⾼性能，可扩展的Web应⽤程序的完美解决⽅ 案。 Redis从它的许多竞争继承来的三个主要特点：

Redis数据库完全在内存中，使⽤磁盘仅⽤于持久性。

相⽐许多键值数据存储，Redis拥有⼀套较为丰富的数据类型。

Redis可以将数据复制到任意数量的从服务器。

Redis 优势

异常快速：Redis的速度⾮常快，每秒能执⾏约 1万集合，每秒约81 0+条记录。

⽀持丰富的数据类型：Redis⽀持最⼤多数开发⼈员已经知道像列表，集合，有序集合，散列数据类 型。这使得它⾮常容易解决各种各样的问题，因为我们知道哪些问题是可以处理通过它的数据类型 更好。

操作都是原⼦性：所有Redis操作是原⼦的，这保证了如果两个客户端同时访问的Redis服务器将获 得更新后的值。

多功能实⽤⼯具：Redis是⼀个多实⽤的⼯具，可以在多个⽤例如缓存，消息，队列使⽤(Redis原⽣ ⽀持发布/订阅)，任何短暂的数据，应⽤程序，如Web应⽤程序会话，⽹⻚命中计数等。

Redis安装部署

下载redis安装⽂件 wget htp:/download.redis.io/releases/redis-3.0.5.tar.gz

解压redis⽂件到指定的⽬录并创建软连接

tar -zxvf /tmp/redis-3.0.5.tar.gz /export/servers/cd /export/servers ln -s redis-3.0.5 redis

安装gc

yum instal -y gc

编译redis

cd redis/src make

启动redis服务

./redis-server

![image 1](<第七章 Redis 快速入门.note_images/imageFile1.png>)

启动redis客户端 在服务端窗⼝不关闭的情况下，新开⼀个窗⼝ cd /export/servers/redis/src

./redis-cli

![image 2](<第七章 Redis 快速入门.note_images/imageFile2.png>)

Redis Api Redis提供了丰富的命令（comand）对数据库和各种数据类型进⾏操作，这些comand可以在 Linux终端使⽤。在编程时，⽐如使⽤Redis 的Java语⾔包，这些命令都有对应的⽅法。下⾯将Redis提 供的命令做⼀总结。官⽹命令列表： （英⽂）

htp:/redis.io/comands

- 1、连接操作相关的命令
- 2、对value操作的命令


quit：关闭连接（conection）

auth：简单密码认证

exists(key)：确认⼀个key是否存在

del(key)：删除⼀个key

type(key)：返回值的类型

keys(patern)：返回满⾜给定patern的所有key

randomkey：随机返回key空间的⼀个key

rename(oldname, newname)：将key由oldname重命名为newname，若newname存在则删除 newname表示的key

dbsize：返回当前数据库中key的数⽬

expire：设定⼀个key的活动时间（s）

tl：获得⼀个key的活动时间

select(index)：按索引查询

move(key, dbindex)：将当前数据库中的key转移到有dbindex索引的数据库

flushdb：删除当前选择数据库中的所有key

flushal：删除所有数据库中的所有key

- 3、对String操作的命令
- 4、对List操作的命令
- 5、对Set操作的命令


set(key, value)：给数据库中名称为key的string赋予值value

get(key)：返回数据库中名称为key的string的value

getset(key, value)：给名称为key的string赋予上⼀次的value

mget(key1, key2,…, key N)：返回库中多个string（它们的名称为key1，key2…）的value

setnx(key, value)：如果不存在名称为key的string，则向库中添加string，名称为key，值为value

setex(key, time, value)：向库中添加string（名称为key，值为value）同时，设定过期时间time

mset(key1, value1, key2, value2,…key N, value N)：同时给多个string赋值，名称为key i的string赋 值value i

msetnx(key1, value1, key2, value2,…key N, value N)：如果所有名称为key i的string都不存在，则 向库中添加string，名称key i赋值为value i

incr(key)：名称为key的string增1操作

incrby(key, integer)：名称为key的string增加integer

decr(key)：名称为key的string减1操作

decrby(key, integer)：名称为key的string减少integer

apend(key, value)：名称为key的string的值附加value

substr(key, start, end)：返回名称为key的string的value的⼦串

rpush(key, value)：在名称为key的list尾添加⼀个值为value的元素 lpush(key, value)：在名称为key的list头添加⼀个值为value的 元素

len(key)：返回名称为key的list的⻓度

lrange(key, start, end)：返回名称为key的list中start⾄end之间的元素（下标从0开始，下同）

ltrim(key, start, end)：截取名称为key的list，保留start⾄end之间的元素

lindex(key, index)：返回名称为key的list中index位置的元素

lset(key, index, value)：给名称为key的list中index位置的元素赋值为value

lrem(key, count, value)：删除count个名称为key的list中值为value的元素。count为0，删除所有值 为value的元素，count>0从头⾄尾删除count个值为value的元素，count<0从尾到头删除|count|个 值为value的元素。 lpop(key)：返回并删除名称为key的list中的⾸元素 rpop(key)：返回并删除名称 为key的list中的尾元素 blpop(key1, key2,… key N, timeout)：lpop命令的block版本。即当timeout 为0时，若遇到名称为key i的list不存在或该list为空，则命令结束。如果timeout>0，则遇到上述情 况时，等待timeout秒，如果问题没有解决，则对keyi+1开始的list执⾏pop操作。

brpop(key1, key2,… key N, timeout)：rpop的block版本。参考上⼀命令。

rpoplpush(srckey, dstkey)：返回并删除名称为srckey的list的尾元素，并将该元素添加到名称为 dstkey的list的头部

sad(key, member)：向名称为key的set中添加元素member

srem(key, member) ：删除名称为key的set中的元素member

spop(key) ：随机返回并删除名称为key的set中⼀个元素

smove(srckey, dstkey, member) ：将member元素从名称为srckey的集合移到名称为dstkey的集合

scard(key) ：返回名称为key的set的基数

sismember(key, member) ：测试member是否是名称为key的set的元素

sinter(key1, key2,…key N) ：求交集

sinterstore(dstkey, key1, key2,…key N) ：求交集并将交集保存到dstkey的集合

sunion(key1, key2,…key N) ：求并集

sunionstore(dstkey, key1, key2,…key N) ：求并集并将并集保存到dstkey的集合

sdif(key1, key2,…key N) ：求差集

sdifstore(dstkey, key1, key2,…key N) ：求差集并将差集保存到dstkey的集合

smembers(key) ：返回名称为key的set的所有元素

srandmember(key) ：随机返回名称为key的set的⼀个元素

- 6、对zset（sorted set）操作的命令


zad(key, score, member)：向名称为key的zset中添加元素member，score⽤于排序。如果该元 素已经存在，则根据score更新该元素的顺序。

zrem(key, member) ：删除名称为key的zset中的元素member

zincrby(key, increment, member) ：如果在名称为key的zset中已经存在元素member，则该元素的 score增加increment；否则向集合中添加该元素，其score的值为increment

zrank(key, member) ：返回名称为key的zset（元素已按score从⼩到⼤排序）中member元素的 rank（即index，从0开始），若没有member元素，返回“nil”

zrevrank(key, member) ：返回名称为key的zset（元素已按score从⼤到⼩排序）中member元素的 rank（即index，从0开始），若没有member元素，返回“nil”

zrange(key, start, end)：返回名称为key的zset（元素已按score从⼩到⼤排序）中的index从start 到end的所有元素

zrevrange(key, start, end)：返回名称为key的zset（元素已按score从⼤到⼩排序）中的index从 start到end的所有元素

zrangebyscore(key, min, max)：返回名称为key的zset中score >= min且score <= max的所有元 素 zcard(key)：返回名称为key的zset的基数 zscore(key, element)：返回名称为key的zset中元素 element的score zremrangebyrank(key, min, max)：删除名称为key的zset中rank >= min且 rank <= max的所有元素 zremrangebyscore(key, min, max) ：删除名称为key的zset中 score >= min且score <= max的所有元素

zunionstore / zinterstore(dstkeyN, key1,…,keyN, WEIGHTS w1,… wN, AGREGATE SUM|MIN|MAX)：对N个zset求并集和交集，并将最后的集合保存在dstkeyN中。 对于集合中每⼀个元素的score，在进⾏AGREGATE运算前，都要乘以对于的WEIGHT参数。如果 没有提供WEIGHT，默认为1。默认的AGREGATE是SUM，即结果集合中元素的score是所有集合 对应元素进⾏SUM运算的值，⽽MIN和MAX是指，结果集合中元素的score是所有集合对应元素中 最⼩值和最⼤值。

- 7、对Hash操作的命令
- 8、持久化
- 9、远程服务控制
- 10、Redis Java客户端Jedis


hset(key, field, value)：向名称为key的hash中添加元素field<—>value

hget(key, field)：返回名称为key的hash中field对应的value

hmget(key, field1, …,field N)：返回名称为key的hash中field i对应的value

hmset(key, field1, value1,…,field N, value N)：向名称为key的hash中添加元素field i<—>value i

hincrby(key, field, integer)：将名称为key的hash中field的value增加integer

hexists(key, field)：名称为key的hash中是否存在键为field的域

hdel(key, field)：删除名称为key的hash中键为field的域

hlen(key)：返回名称为key的hash中元素个数

hkeys(key)：返回名称为key的hash中所有键

hvals(key)：返回名称为key的hash中所有键对应的value

hgetal(key)：返回名称为key的hash中所有的键（field）及其对应的value

save：将数据同步保存到磁盘

bgsave：将数据异步保存到磁盘

lastsave：返回上次成功将数据保存到磁盘的Unix时戳

shundown：将数据同步保存到磁盘，然后关闭服务

info：提供服务器的信息和统计

monitor：实时转储收到的请求

slaveof：改变复制策略设置

config：在运⾏时配置Redis服务器

- 1、Jedis介绍 Jedis 是 Redis 官⽅⾸选的 Java 客户端开发包。

- 2、Jedis 的maven依赖 <dependency> <groupId>redis.clients</groupId> <artifactId>jedis</artifactId> <version>2.7.3</version>


</dependency>

- 3、简单使⽤

- 4、JEDIS的池使⽤

- 5、JEDIS的分布式Redis在容灾处理⽅⾯可以通过服务器端配置Master-Slave模式来实现。⽽在分布式 集群⽅⾯⽬前只能通过客户端⼯具来实现⼀致性哈希分布存储，即key分⽚存储。


![image 3](<第七章 Redis 快速入门.note_images/imageFile3.png>)

![image 4](<第七章 Redis 快速入门.note_images/imageFile4.png>)

![image 5](<第七章 Redis 快速入门.note_images/imageFile5.png>)

Redis适⽤场景

取最新N个数据的操作

⽐如典型的取你⽹站的最新⽂章，通过下⾯⽅式，我们可以将最新的5 0条评论的ID放在Redis的List 集合中，并将超出集合部分从数据库获取 使⽤LPUSH latest.coments<ID>命令，向list集合中插⼊数 据 插⼊完成后再⽤LTRIM latest.coments 0 5 0命令使其永远只保存最近5 0个ID 然后我们在客 户端获取某⼀⻚评论时可以⽤下⾯的逻辑（伪代码） 如果你还有不同的筛选维度，⽐如某个分类的最 新N条，那么你可以再建⼀个按此分类的List，只存ID的话，Redis是⾮常⾼效的

排⾏榜应⽤，取TOP N操作

这个需求与上⾯需求的不同之处在于，前⾯操作以时间为权重，这个是以某个条件为权重，⽐如按顶 的次数排序，这时候就需要我们的sorted set出⻢了，将你要排序的值设置成sortedset的score，将具 体的数据设置成相应的value，每次只需要执⾏⼀条ZAD命令即可。

需要精准设定过期时间的应⽤

# ⽐如你可以把上⾯说到的sorted set的score值设置成过期时间的时间戳，那么就可以简单地通过过期 时间排序，定时清除过期数据了，不仅是清除Redis中的过期数据，你完全可以把Redis⾥这个过期时 间当成是对数据库中数据的索引，⽤Redis来找出哪些数据需要过期删除，然后再精准地从数据库中删 除相应的记录。

计数器应⽤

# Redis的命令都是原⼦性的，你可以轻松地利⽤INCR，DECR命令来构建计数器系统。

Uniq操作，获取某段时间所有数据排重值

这个使⽤Redis的set数据结构最合适了，只需要不断地将数据往set中扔就⾏了，set意为集合，所以会 ⾃动排重

实时系统，反垃圾系统

通过上⾯说到的set功能，你可以知道⼀个终端⽤户是否进⾏了某个操作，可以找到其操作的集合并进 ⾏分析统计对⽐等。没有做不到，只有想不到。

Pub/Sub构建实时消息系统

Redis的Pub/Sub系统可以构建实时的消息系统，⽐如很多⽤Pub/Sub构建的实时聊天系统的例⼦。

构建队列系统

使⽤list可以构建队列系统，使⽤sorted set甚⾄可以构建有优先级的队列系统。

缓存

这个不必说了，性能优于Memcached，数据结构更多样化。

Redis数据库持久化【扩展】1、总体介绍

总的来说有两种持久化⽅案：RDB和AOF

RDB⽅式按照⼀定的时间间隔对数据集创建基于时间点的快照。

AOF⽅式记录Server收到的写操作到⽇志⽂件，在Server重启时通过回放这些写操作来重建数据 集。该⽅式类似于MySQL中基于语句格式的binlog。当⽇志变⼤时Redis可在后台重写⽇志。

若仅期望数据在Server运⾏期间存在则可禁⽤两种持久化⽅案。

在同⼀Redis实例中同时开启AOF和RDB⽅式的数据持久化⽅案也是可以的。该情况下Redis重启时 AOF⽂件将⽤于重建原始数据集，因为较RDB⽅式⽽⾔，AOF⽅式能最⼤限度的保证数据完整性。 2、两种⽅案各⾃的优缺点

RDB优点：

RDB是Redis数据集的基于时间点的紧凑的副本，⾮常适合于备份场景。⽐如每个⼩时对RDB⽂件 做⼀次⼩的归档，每天对RDB⽂件做⼀次⼤的归档，每⽉对RDB⽂件做⼀次更⼤的归档。这样可以 在必要的时刻选择不同的备份版本进⾏数据恢复。由于是⼀个紧凑的⽂件，易于传输到远程数据中⼼，因 此RDB⾮常适合于灾难恢复。RDB⽅式的开销较低，在该种⽅式下Redis⽗进程所要做的仅是开辟⼀个⼦进程来 做剩下的事情。与AOF相⽐RDB在数据集较⼤时能够以更快的速度恢复。

RDB缺点

若需在Redis停⽌⼯作时（例如意外断电）尽可能保证数据不丢失，那么RDB不是最好的⽅案。例 如，通常会每隔5分钟或者更⻓的时间来创建⼀次快照，如若Redis没有被正确的关闭就可能丢失

最近⼏分钟的数据。RDB⽅式需经常调⽤fork()函数以开辟⼦进程来实现持久化。在数据集较⼤、CPU性能不 够强悍时fork()调⽤可能很耗时从⽽会导致Redis在⼏毫秒甚⾄⼀秒中的时间内不能服务clients。AOF也需要调⽤ fork()但却可以在不影响数据持久性的条件下调整重写logs的频率。

AOF优点

使⽤AOF⽅式时Redis持久化更可靠：有三种不同的fsync策略供选择：no fsync at al、fsync every second、 fsync at every query。默认为fsync every second此时的写性能仍然很好，且最 坏的情况下可能丢失⼀秒钟的写操作。

AOF⽇志是apend only⽅式产⽣的⽇志，因此不存在随机访问问题以及意外断电时造成的损毁问 题。即使出于某种原因（如磁盘满）⽇志以⼀个写了⼀半的命令结尾，仍可以使⽤redis-checkaof⼯具快速进⾏修复。 当AOF⽇志逐渐变⼤后，Redis可在后台⾃动的重写AOF⽇志。当Redis在继续追加旧的AOF⽇志⽂ 件时重写⽇志是完全安全的。Redis利⽤可以重建当前数据集的最少的命令产⽣⼀个全新的⽇志⽂ 件，⼀旦新的⽇志⽂件创建完成Redis开始向新的⽇志⽂件追加⽇志。 AOF⽇志的格式易于理解易于解析。这在某些场景⾮常有⽤。⽐如，不下⼼使⽤FLUSHAL命令清 空了所有的数据，同时AOF⽇志没有发⽣重写操作，那么就可以简单的通过停⽌Redis Server移除 ⽇志中的最后⼀条FLUSHAL命令重启Redis Server来恢复数据。

AOF缺点

同样的数据集AOF⽂件要⽐RDB⽂件⼤很多。 根据使⽤的fsync⽅式不同AOF可能⽐RDB慢很多。在使⽤no fsync at al时AOF的性能基本与RDB 持平，在使⽤fsync every second时性能有所下降但仍然较⾼，在使⽤ fsync at every query时性 能较低。然⽽RDB⽅式却能在⾼负载的情况下保证延迟尽可能⼩。 ⼀些特定的命令可能存在bug从⽽导致重载AOF⽇志时不能重建出完全⼀样的数据集。这样的bugs ⾮常⾮常罕⻅，已经通过测试套件做了充分的测试。这种类型的bugs对于RDB来说⼏乎是不可能 的。说的更清晰⼀点：Redis AOF增量的更新既存的状态⽽RDB快照每次都重新创建，从概念上讲 RDB⽅式更加健壮。然⽽，需要注意两点：每次AOF⽇志被Redis重写的时候⽇志由包含数据集的 实际数据重新⽣成，与追加AOF⽂件的⽅式相⽐该⽅式能有效减少bugs出现的概率；现实的应⽤ 场景中还未收到过任何⽤户关于AOF损毁的报告。

- 3、如何选择持久化⽅式？

取决于具体的应⽤场景，通常，两种⽅式可同时使⽤。若⽐较关⼼数据但仍能忍受⼏分钟的数据丢 失，那么可以简单的使⽤RDB⽅式。有许多⽤户只使⽤AOF⽅式，不建议这种做法，⼀⽅⾯以⼀定 时间间隔创建RDB快照是创建数据备份并快速恢复数据的极好的办法，⼀⽅⾯可以避免AOF⽅式可 能存在的bugs。出于上述原因，将来可能将AOF和RDB⽅式合⼆为⼀。

- 4、RDB持久化设置


默认情况下Redis在磁盘上创建⼆进制格式的命名为dump.rdb的数据快照。可以通过配置⽂件配置 每隔N秒且数据集上⾄少有M个变化时创建快照、是否对数据进⾏压缩、快照名称、存放快照的⼯ 作⽬录。⼀般配置如下：

#90秒后且⾄少1个key发⽣变化时创建快照 save 90 1 #30秒后且⾄少10个key发⽣变化时创建快照 save 30 10 #60秒后且⾄少1 0个key发⽣变化时创建快照 save 60 1 0 #可通过注释所有save开头的⾏来禁⽤RDB持久化 #创建快照时对数据进⾏压缩 rdbcompresion yes #快照名称 dbfilename dump.rdb #存放快照的⽬录（AOF⽂件也会被存放在此⽬录） dir /var/lib/redis/

关于配置参数的详细信息可参阅redis.conf中的说明。

除了通过配置⽂件进⾏设置外也可以通过⼿⼯执⾏命令来创建快照。SAVE命令执⾏⼀个同步操作，以RDB⽂件的 ⽅式保存实例中所有数据的快照。⼀般不在⽣产环境直接使⽤SAVE 命令，因为会阻塞所有的客户端的请求，可以 使⽤BGSAVE命令代替。BGSAVE后台创建数据快照。命名执⾏结果的状态码会⽴即返回。Redis开辟⼀个⼦进 程，⽗进程继续相应客户端请求，⼦进程保存DB到磁盘后退出。客户端可通过执⾏LASTSAVE命令检查操作是否 成功。

创建RDB快照的⼯作流程

Redis需dump数据集到磁盘时会执⾏下列过程：

Redis forks⼀个⼦进程； ⼦进程写数据集到临时的RDB⽂件； ⼦进程写完新的RDB⽂件后替换旧的RDB⽂件。 该⽅式使Redis可以利⽤copy-on-write机制的好处。

- 5、AOF持久化设置


利⽤快照的持久化⽅式不是⾮常可靠，当运⾏Redis的计算机停⽌⼯作、意外掉电、意外杀掉了 Redis进程那么最近写⼊Redis的数据将会丢。对于某些应⽤这或许不成问题，但对于持久化要求⾮ 常⾼的应⽤场景快照⽅式不是理想的选择。AOF⽂件是⼀个替代⽅案，⽤以最⼤限度的持久化数 据。同样，可以通过配置⽂件来开闭AOF：

#关闭AOF apendonly no #打开AOF apendonly yes

当设置apendonly为yes后，每次Redis接收到的改变数据集的命令都会被追加到AOF⽂件。重启Redis后会重放 AOF⽂件来重建数据。还可以通过配置⽂件配置AOF⽂件名、调⽤fsync的频率、调⽤fsync的⾏为、重写AOF的条 件。redis 2.4.10的默认配置如下：

#默认AOF⽂件名 apendfilename apendonly.aof #每秒调⽤⼀次fsync刷新数据到磁盘 apendfsync everysec #当进程中BGSAVE或BGREWRITEAOF命令正在执⾏时不阻⽌主进程中的fsync()调⽤（默认为 no，当存在延迟问题时需调整为yes） no-apendfsync-on-rewrite no #当AOF增⻓率为10%且达到了64mb时开始⾃动重写AOF auto-aof-rewrite-percentage 10 auto-aof-rewrite-min-size 64mb 各参数含义可参阅redis.conf中详细说明。

- 6、⼏点说明


⽇志重写

随着Redis接收到的命令的增加AOF⽂件会变得越来越⼤。Redis⽀持⽇志重写特性，可以在不影响响应客户端的 前提下在后台重构AOF⽂件。当在Redis中执⾏BGREWRITEAOF后Redis将使⽤构建数据集所需的最少的命令来 重构⽇志⽂件。Redis2.2中需要经常⼿动运⾏BGREWRITEAOF,Redis2.2开始⽀持⾃动触发⽇志重写。 ⽇志重写同样使⽤copy-on-write机制，流程⼤致如下：

Redis开辟⼀个⼦进程； ⼦进程在临时⽂件中写新的AOF⽂件；

⽗进程将所有新的更改缓存在memory中（同时新更改被写⼊旧的AOF，这样即使重写操作失 败了也是安全的）； 在⼦进程重写好临时AOF后⽗进程收到⼀个信号并追加memory中缓冲的更改到⼦进程产⽣的 临时⽂件的末尾； Redis进⾏⽂件重命名⽤新的⽂件替换旧的⽂件并开始追加新的数据到新⽂件。

fsync调⽤模式

该模式决定了Redis刷新数据到磁盘的频率，有三个可选项： no fsync at al 全由操作系统决定刷数据的时机。最快但最不安全。 fsync every second 每秒⼀次刷新。⾜够快，最多可丢失⼀秒的数据。 fsync at every query 每次记录⼀条新的命令到AOF便刷⼀次数据到磁盘。最慢但最安全。 默认策略（也是默认策略）为fsync every second

AOF损坏时的对策

若在写AOF⽂件时Server崩溃则可能导致AOF⽂件损坏⽽不能被Redis载⼊。可通过如下步骤修 复：

创建⼀个AOF⽂件的备份； 使⽤redis-check-aof⼯具修复原始的AOF⽂件； $ redis-check-aof -fix 使⽤dif -u 检查备份⽂件和修复后⽂件的异同（可选步骤）； 使⽤修复后的AOF⽂件重启Redis。

- 7、如何由RDB持久化转换到AOF持久化？ Redis >=2.2时 创建最近的RDB⽂件的备份； 将备份保存在安全的位置； 发起如下命令； $redis-cli config set apendonly yes $redis-cli config set save"（可选，若不执⾏RDB和AOF⽅式将并存） 确认数据库包含相同的keys； 确认write操作被正确追加到了AOF⽂件。 注意事项：记得修改redis.conf中对应的配置以免Redis Server重启后通过命令进⾏的配置更新丢 失⽽重新使⽤旧的配置⽂件中配置。

Redis2.0时 创建最近的RDB⽂件的备份； 将备份存放在安全的位置； 停⽌数据库上的所有写操作； 发起 redis-cli bgrewriteaof命令创建AOF⽂件； 当AOF⽂件⽣成后停⽌Redis Server； 编辑redis.conf开启AOF持久化； 重启Redis Server； 确认数据库包含相同的keys； 确认write操作被正确追加到了AOF⽂件。

- 8、AOF与RDB之间的相互影响

Redis2.4以上的版本会确保在RDB快照创建时不触发AOF重写或者在AOF重写时不允许BGSAVE操 作，以避免Redis后台进程同时做繁重的磁盘I/O操作。 当创建RDB快照时对于⽤户使⽤BGREWRITEAOF明确发起的⽇志重写操作server会⽴刻回应⼀个 ok状态码告知⽤户操作将回被执⾏，当且仅当快照创建完成后重写操作开始被执⾏。 在同时使⽤了AOF和RDB⽅式的情况下，Redis重启后会优先使⽤AOF⽂件来重构原始数据集。

- 9、备份Redis 数据


- 务必做好数据备份以防意外丢失。Redis是备份友好的，可在数据库运⾏时拷⻉RDB⽂件。建议的 备份⽅案： 创建⼀个cron作业在⼀个⽬录中每⼩时创建⼀次RDB快照在另⼀⽬录中每天创建⼀次RDB快照； cron作业每次运⾏的时候使⽤find命令确保过时的RDB快照⽂件被清理掉（可以通过在快照命中包 含数据和时间信息来进⾏标记）； 确保将RDB快照转移到外部的数据中⼼或者⾄少是运⾏Redis实例的物理机之外的机器（⾄少每天 ⼀次）。
- 10、灾难恢复


在Redis中灾难恢复和数据备份基本上是同样的过程。可考虑将备份分布到不同的远程数据中⼼以 最⼤限度的避免数据丢失。⼏种低成本的灾难恢复计划：

可将每天会每⼩时的RDB快照以加密的⽅式（可使⽤gpg -c加密）传输到数据中⼼。确保将密 码存储在不同的安全的地⽅。建议使⽤不同的存储服务以提⾼数据安全性。

使⽤SCP命令将快照传输到远程服务器。

最简单和安全的⽅式：获取⼀个⼩的远程VPS，在其上安装 sh，⽣成⽆密码的 sh client key添加到VPS的 authorized_keys⽂件，此后便可使⽤SCP传输备份到VPS了。建议搞两个不同的VPS以提⾼安全性。

需要注意的是，⽂件传输完成后⼀定要校验⽂件的完整性正确性。可通过MD5或SHA1进⾏验证。 另外需要搭建⼀套告警系统，当备份传输发⽣问题时能及时的告知。

Redis集群【扩展】

3.0以前的处理⽅案 htp:/ w.cnblogs.com/lulu/archive/2013/06/10/3130878.html

3.0的处理⽅案 htp:/blog.csdn.net/myrainblues/article/details/2581535

