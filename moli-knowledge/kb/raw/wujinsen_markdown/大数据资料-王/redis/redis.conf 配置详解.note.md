# Redis configuration file example

# Note on units: when memory size is neded, it is posible to specify # it in the usual form of 1k 5GB 4M and so forth: # # 1k => 1 0 bytes # 1kb => 1024 bytes # 1m => 1 0 bytes # 1mb => 1024*1024 bytes # 1g => 1 0 bytes # 1gb => 1024*1024*1024 bytes # # units are case insensitive so 1GB 1Gb 1gB are al the same.

# INCLUDES:⾃定义配置⽂件 #

# include和c⾥⾯的include概念类似，可以把redis的的参数分散到其他⽂件， #当运⾏多个redis实例时，通过include⼀个comon的配置⽂件让所有redis-server共享，简化 redis.conf的内容。 # include /path/to/local.conf # include /path/to/other.conf

# GENERAL：全局配置 #

# 这个参数指定redis是否以后台进程⽅式启动，默认不是，⽣产环境可以改成yes，另外，Redis是单 线程的。 daemonize no

# 当daemonize参数为yes时，后台进程⽅式启动后，redis会创建进程⽂件，这个参数是指定此⽂件的 路径。 pidfile /var/run/redis.pid

# redistcp 监听端⼝，客户端通过此端⼝与redis服务器交互。⾮集群模式下，redis只有这个监听端⼝ port 6379

# 客户端连接队列值，这个值是socket⾥⾯listen函数的参数。如果服务器caps很⾼，需要把这个参数 改⼤些。 tcp-backlog 51

# 监听地址，默认是监听所有⽹卡，也可以指定监听⼀个或多个。多个的格式：bind 192.168.1.1 192.168.1. # Examples: # bind 192.168.1.10 10.0.0.1 # bind 127.0.0.1

#指定⽤来监听Unix套接字的路径。没有默认值， 所以在没有指定的情况下Redis不会监听Unix套接字 # unixsocket /tmp/redis.sock # unixsocketperm 70

# client空闲⼏秒后断开连接，0为将这个功能职位不可⽤ timeout 0

# TCP kepalive. # 如果⾮零，则设置SO_KEPALIVE选项来向空闲连接的客户端发送ACK，由于以下两个原因这是很有 ⽤的：

- # 1）能够检测⽆响应的客户端
- # 2）让该连接中间的⽹络设备知道这个连接还存活 # 在Linux上，这个指定的值(单位：秒)就是发送ACK的时间间隔。 # 注意：要关闭这个连接需要两倍的这个时间值。 # 在其他内核上这个时间间隔由内核配置决定 # 这个选项的⼀个合理值是60秒 tcp-kepalive 0


#指定log级别，默认是notice，包括debug,verbose ,notice, , warning. #⽣产环境建议⽤notice,测试阶段可以⽤debug或者verbose,debug的log最多。 # debug （⼤量信息，对开发/测试有⽤） # verbose （很多精简的有⽤信息，但是不像debug等级那么多） # notice （适量的信息，基本上是你⽣产环境中需要的） # warning （只有很重要/严重的信息会记录下来） loglevel notice

# log ⽂件指定，默认是空，打印到控制台，可指定路径,如/var/log/redis.log

logfile"

# 要使⽤系统⽇志记录器，只要设置 "syslog-enabled" 为 "yes" 就可以了。 # 然后根据需要设置其他⼀些syslog参数就可以了。 # syslog-enabled no

# 指明syslog身份 # syslog-ident redis

# 指明syslog的设备。必须是user或LOCAL0 ~ LOCAL7之⼀。 # syslog-facility local0

# 设置数据库个数。默认数据库是 DB 0， # 可以通过select <dbid> (0 <= dbid <= 'databases' - 1 ）来为每个连接使⽤不同的数据库。 databases 16

# SNAPSHOTING：持久化 #

#这个参数是redis持久化的⽀持，基于snapshot机制，定期执⾏持久化存储，⽣成rdb⽂件， #save 命令打开此配置， #第⼀个参数指定多久（单位是秒）执⾏⼀次，第⼆个是在第⼀个参数指定的时间内执⾏多少次写操作 才执⾏持久化操作。 #如果不想持久化到硬盘，可以修改为如下： #save" save 90 1 save 30 10 save 60 1 0

#保存失败，停⽌⼯作 # 默认如果开启持久化(⾄少⼀条save指令)并且最新的后台保存失败，Redis将会停⽌接受写操作 # 这将使⽤户知道数据没有正确的持久化到硬盘，否则可能没⼈注意到并且造成⼀些灾难。 # # 如果后台保存进程能重新开始⼯作，Redis将⾃动允许写操作 # # 然⽽如果你已经部署了适当的Redis服务器和持久化的监控，你可能想关掉这个功能以便于即使是 # 硬盘，权限等出问题了Redis也能够像平时⼀样正常⼯作 stop-writes-on-bgsave-eror yes

# 当导出到 .rdb 数据库时是否⽤LZF压缩字符串对象？ # 默认设置为 "yes"，因为⼏乎在任何情况下它都是不错的。 # 如果你想节省CPU的话你可以把这个设置为 "no"，但是如果你有可压缩的key和value的话， # 那数据⽂件就会更⼤了。 rdbcompresion yes

# 因为版本5的RDB有⼀个CRC64算法的校验和放在了⽂件的最后。这将使⽂件格式更加可靠但在 # ⽣产和加载RDB⽂件时，这有⼀个性能消耗(⼤约10%)，所以你可以关掉它来获取最好的性能。 # ⽣成的关闭校验的RDB⽂件有⼀个0的校验和，它将告诉加载代码跳过检查 rdbchecksum yes

# 持久化数据库的⽂件名 dbfilename dump.rdb

# ⼯作⽬录 # 数据库会写到这个⽬录下，⽂件名就是上⾯的 "dbfilename" 的值。 # 累加⽂件也放这⾥。 # 注意你这⾥指定的必须是⽬录，不是⽂件名。 dir ./

# REPLICATION：主从 #

#Redis 主从复制功能，通过这个参数可以让⼀个redis启动时作为某个master的slave， #Slaveof 第⼀个参数是master的ip，第⼆个参数是master的port. #也可以通过redis-cli控制台执⾏slaveof命令动态把某个redis作为另⼀个master的slave。 # slaveof <masterip> <masterport>

# If the master is pasword protected (using the "requirepas" configuration # directive below) it is posible to tel the slave to authenticate before # starting the replication synchronization proces, otherwise the master wil # refuse the slave request. #这个参数只有在当前redis作为slave时管⽤，⽤于slave和master之间的鉴权，如果master开启了鉴权 功能，则masterauth的意思是此slave和master通信时⽤这个密码。 #另外，这个参数只有在master的redis.conf⽂件中开启了reqireuth参数时才被使⽤。

# masterauth <master-pasword>

# 当⼀个slave失去和master的连接，或者同步正在进⾏中，slave的⾏为有两种可能：

- # 1) 如果 slave-serve-stale-data 设置为 "yes" (默认值)，slave会继续响应客户端请求， # 可能是正常数据，也可能是还没获得值的空数据。
- # 2) 如果 slave-serve-stale-data 设置为 "no"，slave会回复"正在从master同步 # （SYNC with master in progres）"来处理各种请求，除了 INFO 和 SLAVEOF 命令。 slave-serve-stale-data yes


# 你可以配置salve实例是否接受写操作。可写的slave实例可能对存储临时数据⽐较有⽤(因为写⼊ salve # 的数据在同master同步之后将很容被删除)，但是如果客户端由于配置错误在写⼊时也可能产⽣⼀些 问题。 # 从Redis2.6默认所有的slave为只读 # 注意:只读的slave不是为了暴露给互联⽹上不可信的客户端⽽设计的。它只是⼀个防⽌实例误⽤的保 护层。 # ⼀个只读的slave⽀持所有的管理命令⽐如config,debug等。为了限制你可以⽤'renamecomand'来 # 隐藏所有的管理和危险命令来增强只读slave的安全性 slave-read-only yes

# Replication SYNC strategy: disk or socket. # # # WARNING: DISKLES REPLICATION IS EXPERIMENTAL CURENTLY # # # New slaves and reconecting slaves that are not able to continue the replication # proces just receiving diferences, ned to do what is caled a "ful # synchronization". An RDB file is transmited from the master to the slaves. # The transmision can hapen in two diferent ways: #

- # 1) Disk-backed: The Redis master creates a new proces that writes the RDB # file on disk. Later the file is transfered by the parent # proces to the slaves incrementaly.
- # 2) Diskles: The Redis master creates a new proces that directly writes the


# RDB file to slave sockets, without touching the disk at al. # # With disk-backed replication, while the RDB file is generated, more slaves # can be queued and served with the RDB file as son as the curent child producing # the RDB file finishes its work. With diskles replication instead once # the transfer starts, new slaves ariving wil be queued and a new transfer # wil start when the curent one terminates. # # When diskles replication is used, the master waits a configurable amount of # time (in seconds) before starting the transfer in the hope that multiple slaves # wil arive and the transfer can be paralelized. # # With slow disks and fast (large bandwidth) networks, diskles replication # works beter. repl-diskles-sync no

# When diskles replication is enabled, it is posible to configure the delay # the server waits in order to spawn the child that trnasfers the RDB via socket # to the slaves. # # This is important since once the transfer starts, it is not posible to serve # new slaves ariving, that wil be queued for the next RDB transfer, so the server # waits a delay in order to let more slaves arive. # # The delay is specified in seconds, and by default is 5 seconds. To disable # it entirely just set it to 0 seconds and the transfer wil start ASAP. repl-diskles-sync-delay 5

# slave根据指定的时间间隔向master发送ping请求。 # 时间间隔可以通过 repl_ping_slave_period 来设置。 # 默认10秒。 # repl-ping-slave-period 10

# 以下选项设置同步的超时时间

- # 1）slave在与master SYNC期间有⼤量数据传输，造成超时
- # 2）在slave⻆度，master超时，包括数据、ping等
- # 3）在master⻆度，slave超时，当master发送REPLCONF ACK pings


# 确保这个值⼤于指定的repl-ping-slave-period，否则在主从间流量不⾼时每次都会检测到超时 # repl-timeout 60

# 是否在slave套接字发送SYNC之后禁⽤ TCP_NODELAY ？ # 如果你选择“yes”Redis将使⽤更少的TCP包和带宽来向slaves发送数据。但是这将使数据传输到slave # 上有延迟，Linux内核的默认配置会达到40毫秒 # 如果你选择了 "no" 数据传输到salve的延迟将会减少但要使⽤更多的带宽 # 默认我们会为低延迟做优化，但⾼流量情况或主从之间的跳数过多时，把这个选项设置为“yes” # 是个不错的选择。 repl-disable-tcp-nodelay no

# 设置数据备份的backlog⼤⼩。backlog是⼀个slave在⼀段时间内断开连接时记录salve数据的缓冲， # 所以⼀个slave在重新连接时，不必要全量的同步，⽽是⼀个增量同步就⾜够了，将在断开连接的这 段 # 时间内slave丢失的部分数据传送给它。 # 同步的backlog越⼤，slave能够进⾏增量同步并且允许断开连接的时间就越⻓。 # backlog只分配⼀次并且⾄少需要⼀个slave连接 # repl-backlog-size 1mb

# 当master在⼀段时间内不再与任何slave连接，backlog将会释放。以下选项配置了从最后⼀个 # slave断开开始计时多少秒后，backlog缓冲将会释放。 # 0表示永不释放backlog # repl-backlog-tl 360

# slave的优先级是⼀个整数展示在Redis的Info输出中。如果master不再正常⼯作了，哨兵将⽤它来 # 选择⼀个slave提升=升为master。 # 优先级数字⼩的salve会优先考虑提升为master，所以例如有三个slave优先级分别为10，10，25， # 哨兵将挑选优先级最⼩数字为10的slave。 # 0作为⼀个特殊的优先级，标识这个slave不能作为master，所以⼀个优先级为0的slave永远不会被 # 哨兵挑选提升为master # 默认优先级为10 slave-priority 10

# 如果master少于N个延时⼩于等于M秒的已连接slave，就可以停⽌接收写操作。 # N个slave需要是“oneline”状态 # 延时是以秒为单位，并且必须⼩于等于指定值，是从最后⼀个从slave接收到的ping（通常每秒发 送）

# 开始计数。 # This option does not GUARANTES that N replicas wil acept the write, but # wil limit the window of exposure for lost writes in case not enough slaves # are available, to the specified number of seconds. # 例如⾄少需要3个延时⼩于等于10秒的slave⽤下⾯的指令： # min-slaves-to-write 3 # min-slaves-max-lag 10 # 两者之⼀设置为0将禁⽤这个功能。 # 默认 min-slaves-to-write 值是0（该功能禁⽤）并且 min-slaves-max-lag 值是10。

# SECURITY：安全机制 #

#此redis的密码，如果开启，客户端连接时需要设置密码，另外⼀个作⽤是redis作为slave时， #如果master开启了此参数，slave需要设置masterauth参数的值为此参数(requirepas)的值, # requirepas fobared

# 命令重命名 # 在共享环境下，可以为危险命令改变名字。⽐如，你可以为 CONFIG 改个其他不太容易猜到的名 字， # 这样内部的⼯具仍然可以使⽤，⽽普通的客户端将不⾏。 # 例如： # rename-comand CONFIG b840fc02d52404542941c15f59e41cb7be6c52 # 也可以通过改名为空字符串来完全禁⽤⼀个命令 # rename-comand CONFIG" # 请注意：改变命令名字被记录到AOF⽂件或被传送到从服务器可能产⽣问题。

# LIMITS：资源限制 #

# 设置最多同时连接的客户端数量。默认这个限制是1 0个客户端，然⽽如果Redis服务器不能配置 # 处理⽂件的限制数来满⾜指定的值，那么最⼤的客户端连接数就被设置成当前⽂件限制数减32（因 # 为Redis服务器保留了⼀些⽂件描述符作为内部使⽤） # ⼀旦达到这个限制，Redis会关闭所有新连接并发送错误'max number of clients reached' # maxclients 1 0

#redis申请的最⼤内存，字节为单位，默认不限制。

# ⼀旦内存使⽤达到上限，Redis会根据选定的回收策略（参⻅： maxmemory-policy）删除key # 如果因为删除策略Redis⽆法删除key，或者策略设置为 "noeviction"，Redis会回复需要更 # 多内存的错误信息给命令。例如，SET,LPUSH等等，但是会继续响应像Get这样的只读命令。 # 在使⽤Redis作为LRU缓存，或者为实例设置了硬性内存限制的时候（使⽤ "noeviction" 策略） # 的时候，这个选项通常事很有⽤的。 # 警告：当有多个slave连上达到内存上限的实例时，master为同步slave的输出缓冲区所需 # 内存不计算在使⽤内存中。这样当驱逐key时，就不会因⽹络问题 / 重新同步事件触发驱逐key # 的循环，反过来slaves的输出缓冲区充满了key被驱逐的DEL命令，这将触发删除更多的key， # 直到这个数据库完全被清空为⽌ # 总之 .如果你需要附加多个slave，建议你设置⼀个稍⼩maxmemory限制，这样系统就会有空闲 # 的内存作为slave的输出缓存区(但是如果最⼤内存策略设置为"noeviction"的话就没必要了) # maxmemory <bytes>

# 最⼤内存策略：如果达到内存限制了，Redis如何选择删除key。你可以在下⾯五个⾏为⾥选： # volatile-lru -> 根据LRU算法⽣成的过期时间来删除。 # alkeys-lru -> 根据LRU算法删除任何key。 # volatile-random -> 根据过期设置来随机删除key。 # alkeys->random -> ⽆差别随机删。 # volatile-tl -> 根据最近过期时间来删除（辅以 TL） # noeviction -> 谁也不删，直接在写操作时返回错误。 # 注意：对所有策略来说，如果Redis找不到合适的可以删除的key都会在写操作时返回⼀个错误。 # ⽬前为⽌涉及的命令：set setnx setex apend # incr decr rpush lpush rpushx lpushx linsert lset rpoplpush sad # sinter sinterstore sunion sunionstore sdif sdifstore zad zincrby # zunionstore zinterstore hset hsetnx hmset hincrby incrby decrby # getset mset msetnx exec sort # 默认值如下： # maxmemory-policy volatile-lru # LRU和最⼩ TL算法的实现都不是很精确，但是很接近（为了省内存），所以你可以⽤样本量做检 测。 # 例如：默认Redis会检查3个key然后取最旧的那个，你可以通过下⾯的配置指令来设置样本的个数。 # maxmemory-samples 3

# APEND ONLY MODE：仅追加模式 #

#Redis持久化第⼆种⽅式，AOF，默认开启，默认每秒执⾏⼀次持久化保存，也可以设置成实时保 持，这个⽐save更安全。

# 默认情况下，Redis是异步的把数据导出到磁盘上。这种模式在很多应⽤⾥已经⾜够好，但Redis进程 # 出问题或断电时可能造成⼀段时间的写操作丢失(这取决于配置的save指令)。 # AOF是⼀种提供了更可靠的替代持久化模式，例如使⽤默认的数据写⼊⽂件策略（参⻅后⾯的配置） # 在遇到像服务器断电或单写情况下Redis⾃身进程出问题但操作系统仍正常运⾏等突发事件时，Redis # 能只丢失1秒的写操作。 # AOF和RDB持久化能同时启动并且不会有问题。 # 如果AOF开启，那么在启动时Redis将加载AOF⽂件，它更能保证数据的可靠性。 # 请查看 来获取更多信息. apendonly no

htp:/redis.io/topics/persistence

#AOF⽅式下，保持的数据⽂件名字(default: "apendonly.aof") apendfilename "apendonly.aof"

# fsync() 系统调⽤告诉操作系统把数据写到磁盘上，⽽不是等更多的数据进⼊输出缓冲区。 # 有些操作系统会真的把数据⻢上刷到磁盘上；有些则会尽快去尝试这么做。 # Redis⽀持三种不同的模式： # no：不要⽴刻刷，只有在操作系统需要刷的时候再刷。⽐较快。 # always：每次写操作都⽴刻写⼊到aof⽂件。慢，但是最安全。 # everysec：每秒写⼀次。折中⽅案。 # 默认的 "everysec" 通常来说能在速度和数据安全性之间取得⽐较好的平衡。根据你的理解来 # 决定，如果你能放宽该配置为"no" 来获取更好的性能(但如果你能忍受⼀些数据丢失，可以考虑使⽤ # 默认的快照持久化模式)，或者相反，⽤“always”会⽐较慢但⽐everysec要更安全。 # 请查看下⾯的⽂章来获取更多的细节 # # 如果不能确定，就⽤ "everysec" # apendfsync always apendfsync everysec # apendfsync no

htp:/antirez.com/post/redis-persistence-demystified.html

# 如果AOF的同步策略设置成 "always" 或者 "everysec"，并且后台的存储进程（后台存储或写⼊AOF # ⽇志）会产⽣很多磁盘I/O开销。某些Linux的配置下会使Redis因为 fsync()系统调⽤⽽阻塞很久。 # 注意，⽬前对这个情况还没有完美修正，甚⾄不同线程的 fsync() 会阻塞我们同步的write(2)调⽤。 # 为了缓解这个问题，可以⽤下⾯这个选项。它可以在 BGSAVE 或 BGREWRITEAOF 处理时阻⽌ fsync()。 # 这就意味着如果有⼦进程在进⾏保存操作，那么Redis就处于"不可同步"的状态。 # 这实际上是说，在最差的情况下可能会丢掉30秒钟的⽇志数据。（默认Linux设定） # 如果把这个设置成"yes"带来了延迟问题，就保持"no"，这是保存持久数据的最安全的⽅式。

no-apendfsync-on-rewrite no

# ⾃动重写AOF⽂件 # 如果AOF⽇志⽂件增⼤到指定百分⽐，Redis能够通过 BGREWRITEAOF ⾃动重写AOF⽇志⽂件。 # ⼯作原理：Redis记住上次重写时AOF⽂件的⼤⼩（如果重启后还没有写操作，就直接⽤启动时的 AOF⼤⼩） # 这个基准⼤⼩和当前⼤⼩做⽐较。如果当前⼤⼩超过指定⽐例，就会触发重写操作。你还需要指定被 重写 # ⽇志的最⼩尺⼨，这样避免了达到指定百分⽐但尺⼨仍然很⼩的情况还要重写。 # 指定百分⽐为0会禁⽤AOF⾃动重写特性。 auto-aof-rewrite-percentage 10 auto-aof-rewrite-min-size 64mb

# An AOF file may be found to be truncated at the end during the Redis # startup proces, when the AOF data gets loaded back into memory. # This may hapen when the system where Redis is runing # crashes, especialy when an ext4 filesystem is mounted without the # data=ordered option (however this can't hapen when Redis itself # crashes or aborts but the operating system stil works corectly). # # Redis can either exit with an eror when this hapens, or load as much # data as posible (the default now) and start if the AOF file is found # to be truncated at the end. The folowing option controls this behavior. # # If aof-load-truncated is set to yes, a truncated AOF file is loaded and # the Redis server starts emiting a log to inform the user of the event. # Otherwise if the option is set to no, the server aborts with an eror # and refuses to start. When the option is set to no, the user requires # to fix the AOF file using the "redis-check-aof" utility before to restart # the server. # # Note that if the AOF file wil be found to be corupted in the mi dle # the server wil stil exit with an eror. This option only aplies when # Redis wil try to read more data from the AOF file but not enough bytes # wil be found. aof-load-truncated yes

# LUA SCRIPTING :脚本 #

# Lua 脚本的最⼤执⾏时间，毫秒为单位 # 如果达到了最⼤的执⾏时间，Redis将要记录在达到最⼤允许时间之后⼀个脚本仍然在执⾏，并且将 # 开始对查询进⾏错误响应。 # 当⼀个⻓时间运⾏的脚本超过了最⼤执⾏时间，只有 SCRIPT KI L 和 SHUTDOWN NOSAVE 两个 # 命令可⽤。第⼀个可以⽤于停⽌⼀个还没有调⽤写命名的脚本。第⼆个是关闭服务器唯⼀⽅式，当 # 写命令已经通过脚本开始执⾏，并且⽤户不想等到脚本的⾃然终⽌。 # 设置成0或者负值表示不限制执⾏时间并且没有任何警告 lua-time-limit 5 0

# SLOW LOG：慢⽇志 #

# Redis慢查询⽇志可以记录超过指定时间的查询。运⾏时间不包括各种I/O时间，例如：连接客户端， # 发送响应数据等，⽽只计算命令执⾏的实际时间（这只是线程阻塞⽽⽆法同时为其他请求服务的命令 执 # ⾏阶段） # # 你可以为慢查询⽇志配置两个参数:⼀个指明Redis的超时时间(单位为微秒)来记录超过这个时间的命 令 # 另⼀个是慢查询⽇志⻓度。当⼀个新的命令被写进⽇志的时候，最⽼的那个记录从队列中移除。 # # 下⾯的时间单位是微秒，所以1 0就是1秒。注意，负数时间会禁⽤慢查询⽇志，⽽0则会强制 记录 # 所有命令。 slowlog-log-slower-than 1 0

# 这个⻓度没有限制。只是要主要会消耗内存。你可以通过 SLOWLOG RESET 来回收内存。 slowlog-max-len 128

# LATENCY MONITOR：监控 #

# The Redis latency monitoring subsystem samples diferent operations # at runtime in order to colect data related to posible sources of

# latency of a Redis instance. # # Via the LATENCY comand this information is available to the user that can # print graphs and obtain reports. # # The system only logs operations that were performed in a time equal or # greater than the amount of miliseconds specified via the # latency-monitor-threshold configuration directive. When its value is set # to zero, the latency monitor is turned of. # # By default latency monitoring is disabled since it is mostly not neded # if you don't have latency isues, and colecting data has a performance # impact, that while very smal, can be measured under big load. Latency # monitoring can easily be enalbed at runtime using the comand # "CONFIG SET latency-monitor-threshold <miliseconds>" if neded. latency-monitor-threshold 0

# Event notification #

# Redis 能通知 Pub/Sub 客户端关于键空间发⽣的事件 # 这个功能⽂档位于 # 例如：如果键空间事件通知被开启，并且客户端对 0 号数据库的键 fo 执⾏ DEL 命令时，将通过 # Pub/Sub发布两条消息： # PUBLISH :fo del # PUBLISH :del fo # 可以在下表中选择Redis要通知的事件类型。事件类型由单个字符来标识： # K 键空间通知，以 _keyspace@<db>_为前缀 # E 键事件通知，以 _keysevent@<db>_为前缀

htp:/redis.io/topics/keyspace-events

_keyspace@0_ _keyevent@0_

- # g DEL , EXPIRE , RENAME 等类型⽆关的通⽤命令的通知, . # $ String命令 # l List命令 # s Set命令
- # h Hash命令 # z 有序集合命令 # x 过期事件（每次key过期时⽣成） # e 驱逐事件（当key在内存满了被清除时⽣成） # A g$lshzxe的别名，因此”AKE”意味着所有的事件


# notify-keyspace-events 带⼀个由0到多个字符组成的字符串参数。空字符串意思是通知被禁⽤。 # 例⼦：启⽤List和通⽤事件通知： # notify-keyspace-events Elg # 例⼦2：为了获取过期key的通知订阅名字为 :expired 的频道，⽤以下配置 # notify-keyspace-events Ex # 默认所⽤的通知被禁⽤，因为⽤户通常不需要该特性，并且该特性会有性能损耗。 # 注意如果你不指定⾄少K或E之⼀，不会发送任何事件。 notify-keyspace-events"

_keyevent@_

# ADVANCED CONFIG：⾼级配置 #

# 当hash只有少量的entry时，并且最⼤的entry所占空间没有超过指定的限制时，会⽤⼀种节省内存的 # 数据结构来编码。可以通过下⾯的指令来设定限制 hash-max-ziplist-entries 512 hash-max-ziplist-value 64

# 与hash似，数据元素较少的list，可以⽤另⼀种⽅式来编码从⽽节省⼤量空间。 # 这种特殊的⽅式只有在符合下⾯限制时才可以⽤： list-max-ziplist-entries 512 list-max-ziplist-value 64

# set有⼀种特殊编码的情况：当set数据全是⼗进制64位有符号整型数字构成的字符串时。 # 下⾯这个配置项就是⽤来设置set使⽤这种编码来节省内存的最⼤⻓度。 set-max-intset-entries 512

# 与hash和list相似，有序集合也可以⽤⼀种特别的编码⽅式来节省⼤量空间。 # 这种编码只适合⻓度和元素都⼩于下⾯限制的有序集合： zset-max-ziplist-entries 128 zset-max-ziplist-value 64

# HyperLogLog sparse representation bytes limit. The limit includes the # 16 bytes header. When an HyperLogLog using the sparse representation croses # this limit, it is converted into the dense representation. # A value greater than 16 0 is totaly useles, since at that point the # dense representation is more memory eficient. # The sugested value is ~ 3 0 in order to have the benefits of

# the space eficient encoding without slowing down to much PFAD, # which is O(N) with the sparse encoding. The value can be raised to # ~ 1 0 when CPU is not a concern, but space is, and the data set is # composed of many HyperLogLogs with cardinality in the 0 - 15 0 range. hl-sparse-max-bytes 3 0

# 启⽤哈希刷新，每10个CPU毫秒会拿出1个毫秒来刷新Redis的主哈希表（顶级键值映射表）。 # redis所⽤的哈希表实现（⻅dict.c）采⽤延迟哈希刷新机制：你对⼀个哈希表操作越多，哈希刷新 # 操作就越频繁；反之，如果服务器是空闲的，那么哈希刷新就不会完成，哈希表就会占⽤更多的⼀些 # 内存⽽已。 # 默认是每秒钟进⾏10次哈希表刷新，⽤来刷新字典，然后尽快释放内存。 # 建议： # 如果你对延迟⽐较在意，不能够接受Redis时不时的对请求有2毫秒的延迟的话，就⽤ # "activerehashing no"，如果不太在意延迟⽽希望尽快释放内存就设置"activerehashing yes" activerehashing yes

# 客户端的输出缓冲区的限制，可⽤于强制断开那些因为某种原因从服务器读取数据的速度不够快的客 户端， # （⼀个常⻅的原因是⼀个发布/订阅客户端消费消息的速度⽆法赶上⽣产它们的速度） # 可以对三种不同的客户端设置不同的限制： # normal -> 正常客户端 # slave -> slave和 MONITOR 客户端 # pubsub -> ⾄少订阅了⼀个pubsub chanel或patern的客户端 # 下⾯是每个client-output-bufer-limit语法: # client-output-bufer-limit <clas><hard limit> <soft limit> <soft seconds>

# ⼀旦达到硬限制客户端会⽴即被断开，或者达到软限制并持续达到指定的秒数（连续的）。 # 例如，如果硬限制为32兆字节和软限制为16兆字节/10秒，客户端将会⽴即断开 # 如果输出缓冲区的⼤⼩达到32兆字节，或客户端达到16兆字节并连续超过了限制10秒，就将断开连 接。 # 默认normal客户端不做限制，因为他们在不主动请求时不接收数据（以推的⽅式），只有异步客户 端 # 可能会出现请求数据的速度⽐它可以读取的速度快的场景。 # pubsub和slave客户端会有⼀个默认值，因为订阅者和slaves以推的⽅式来接收数据 # 把硬限制和软限制都设置为0来禁⽤该功能 client-output-bufer-limit normal 0 0 0 client-output-bufer-limit slave 256mb 64mb 60

client-output-bufer-limit pubsub 32mb 8mb 60

# Redis调⽤内部函数来执⾏许多后台任务，如关闭客户端超时的连接，清除未被请求过的过期Key等 等。 # 不是所有的任务都以相同的频率执⾏，但Redis依照指定的“hz”值来执⾏检查任务。 # 默认情况下，“hz”的被设定为10。提⾼该值将在Redis空闲时使⽤更多的CPU时，但同时当有多个 key # 同时到期会使Redis的反应更灵敏，以及超时可以更精确地处理。 # 范围是1到50之间，但是值超过10通常不是⼀个好主意。 # ⼤多数⽤户应该使⽤10这个默认值，只有在⾮常低的延迟要求时有必要提⾼到10。 hz 10

# 当⼀个⼦进程重写AOF⽂件时，如果启⽤下⾯的选项，则⽂件每⽣成32M数据会被同步。为了增量式 的 # 写⼊硬盘并且避免⼤的延迟⾼峰这个指令是⾮常有⽤的 aof-rewrite-incremental-fsync yes

