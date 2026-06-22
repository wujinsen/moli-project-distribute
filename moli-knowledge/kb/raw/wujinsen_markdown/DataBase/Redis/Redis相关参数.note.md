Redis是⼀种⾼级key-value数据库。它跟memcached类似，不过数据可以持久化，⽽且⽀持的数据 类型很丰富。有字符串，链表、哈希、集合和有序集合5种。⽀持在服务器端计算集合的并、交和补集 (difference)等，还⽀持多种排序功能。所以Redis也可以被看成是⼀个数据结构服务器。Redis的所

有数据都是保存在内存中，然后不定期的通过异步⽅式保存到磁盘上(这称为“半持久化模式”)；也可以 把每⼀次数据变化都写⼊到⼀个append only file(aof)⾥⾯(这称为“全持久化模式”)。

# Redis监控

⾸先判断客户端和服务器连接是否正常

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br></th>
    <th># 客户端和服务器连接正常，返回PONG redis> PING PONG<br><br># 客户端和服务器连接不正常(⽹络不正常或服务器未能正常运 ⾏)，返回连接异常 redis 127.0.0.1 6379> PING Could not conect to Redis at 127.0.0.1 6379: Conection</th>
  </tr>
</table>


refused

Redis 监控最直接的⽅法就是使⽤系统提供的 info 命令，只需要执⾏下⾯⼀条命令，就能获得 Redis 系统的状态报告。

<table>
  <tr>
    <th>1</th>
    <th>redis-cli info</th>
  </tr>
</table>


结果会返回 Server、Clients、Memory、Persistence、Stats、Replication、CPU、Keyspace 8 个部分。从info⼤返回结果中提取相关信息，就可以达到有效监控的⽬的。 先解释下各个参数含义

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10


# Server redis_version:2.8.8 # Redis 的版本 redis_git_sha1  0 redis_git_dirty:0 redis_build_id:bf5d1747be5380f redis_mode:standalone os:Linux 2.6.32-20.7.1.el6.x86_64 x86_64 arch_bits:64 multiplexing_api:epol gc_version:4.4.7 #gc版本 proces_id:49324 # 当前 Redis 服务器进程id run_id: bd7b17efcf108fde285d8987e50392f6a38f48 tcp_port:6379 uptime_in_seconds:1739082 # 运⾏时间(秒) uptime_in_days:20 # 运⾏时间(天) hz:10 lru_clock:1734729 config_file:/home/s/aps/RedisMulti_video_so/conf/ z.conf

- 1

- 12
- 13
- 14
- 15
- 16
- 17
- 18
- 19
- 20
- 21


- 2

- 23
- 24
- 25
- 26
- 27
- 28
- 29
- 30
- 31
- 32


- 3

- 34
- 35
- 36
- 37
- 38
- 39
- 40
- 41
- 42
- 43


- 4

- 45
- 46
- 47
- 48
- 49
- 50
- 51
- 52
- 53
- 54


- 5

- 56
- 57
- 58
- 59
- 60
- 61
- 62
- 63
- 64
- 65


- 6


# Clients conected_clients:1#连接的客户端数量 client_longest_output_list:0 client_bi gest_input_buf:0 blocked_clients:0

# Memory used_memory:821848 #Redis分配的内存总量 used_memory_human:802.59K used_memory_rs:8532672 #Redis分配的内存总量(包括内存 碎⽚) used_memory_peak:178987632 used_memory_peak_human:170.70M #Redis所⽤内存的⾼峰值 used_memory_lua: 3792 mem_fragmentation_ratio:104.07 #内存碎⽚⽐率 mem_alocator:tcmaloc-2.0

# Persistence loading:0 rdb_changes_since_last_save:0 #上次保存数据库之后，执⾏ 命令的次数 rdb_bgsave_in_progres:0 #后台进⾏中的 save 操作的数量 rdb_last_save_time:1410848505 #最后⼀次成功保存的时间 点，以 UNIX 时间戳格式显示 rdb_last_bgsave_status:ok rdb_last_bgsave_time_sec:0 rdb_current_bgsave_time_sec:-1 aof_enabled:0 #redis是否开启了aof aof_rewrite_in_progres:0 aof_rewrite_scheduled:0 aof_last_rewrite_time_sec:-1 aof_current_rewrite_time_sec:-1 aof_last_bgrewrite_status:ok aof_last_write_status:ok

# Stats total_conections_received:5705 #运⾏以来连接过的客户端的 总数量 total_co mands_procesed:204013 # 运⾏以来执⾏过的命令 的总数量 instantaneous_ops_per_sec:0 rejected_conections:0 sync_ful:0 sync_partial_ok:0 sync_partial_err:0 expired_keys:3401#运⾏以来过期的 key 的数量

- 67
- 68


- 69
- 70
- 71
- 72
- 73
- 74
- 75
- 76
- 77
- 78
- 79
- 80
- 81
- 82
- 83
- 84


evicted_keys:0 #运⾏以来删除过的key的数量 keyspace_hits:2129 #命中key 的次数 keyspace_mi ses:3148 #没命中key 的次数 pubsub_chanels:0 #当前使⽤中的频道数量 pubsub_paterns:0 #当前使⽤中的模式数量 latest_fork_usec:4391

# Replication role:master #当前实例的⻆⾊master还是slave conected_slaves:0 master_repl_ofset:0 repl_backlog_active:0 repl_backlog_size:1048576 repl_backlog_first_byte_ofset:0 repl_backlog_histlen:0

# CPU used_cpu_sys:151.61 used_cpu_user:1083.37 used_cpu_sys_children:2.52 used_cpu_user_children:16.79

# Keyspace db0:keys=3,expires=0,avg_tl=0 #各个数据库的 key 的数量， 以及带有⽣存期的 key 的数量

内存使⽤ 如果 Redis 使⽤的内存超出了可⽤的物理内存⼤⼩，那么 Redis 很可能系统会被杀掉。针对这⼀点， 你可以通过 info 命令对 used_memory 和 used_memory_peak 进⾏监控，为使⽤内存量设定阀 值，并设定相应的报警机制。当然，报警只是⼿段，重要的是你得预先计划好，当内存使⽤量过⼤ 后，你应该做些什么，是清除⼀些没⽤的冷数据，还是把 Redis 迁移到更强⼤的机器上去。 持久化 如果因为你的机器或 Redis 本身的问题导致 Redis 崩溃了，那么你唯⼀的救命稻草可能就是 dump 出来的rdb⽂件了，所以，对 Redis dump ⽂件进⾏监控也是很重要的。可以通过对 rdb_last_save_time 进⾏监控，了解最近⼀次 dump 数据操作的时间，还可以通过对 rdb_changes_since_last_save进⾏监控来获得如果这时候出现故障，会丢失（即已改变）多少数 据。 Keys 通过获取Keyspace中的结果得到各个数据库中key的数量 QPS 即每分钟执⾏的命令个数，即：(total_commands_processed2-

total_commands_processed1)/span，为了实时得到QPS，可以设定脚本在后台运⾏，记录过去 ⼏分钟的total_commands_processed。在计算QPS时，利⽤过去的信息和当前的信息得出QPS的 估计值。

