⽹站的访问量慢慢上来了。为了⽹站的性能⽅⾯，开始⽤了redis做缓存策略。刚开始的时候， redis是⼀个单点，当⼀台机器岩机的时候，redis的 服务完全停⽌，这时就会影响其他服务的正常 运⾏。费话不多说了，下⾯利⽤redis sentinel做⼀个主从切换的集群管理。做这个集群管理的时 候，查过很多资料才完全了解，他是怎么做的。

java 客户端请看：

http://blog.mkfree.com/posts/52b146e6479e5a64742fddd0

http://redis.io/topics/sentinel

参考资料： 我也是看这篇⽂章。

环境配置：

由于我这次配置没有太多的机器，我⽤了vagrant 去开了多台虚拟机。然后搭好了环境。

redis的安装请参考：

redis 简单官⽅脚本安装⽅法(linux)

集群配置最少需要三台机器，那么我就三台虚拟机,三台虚拟机分别安装同样的redis的环境

ip分别：

- 192.168.9.17 （redis sentinel 集群监控）

- 192.168.9.18 （redis 主）

- 192.168.9.19 （redis 从）


redis配置:

主的redis配置⽂件,使⽤默认的配置⽂件就可以了，如果你需要设计其他参数

从的redis配置⽂件，添加

#从的redis配置⽂件，需要添加 vim/etc/redis/6379.conf slaveof192.168.9.186379

启动主从redis

#启动主redis（192.168.9.18） /etc/init.d/redis_6379.conf start #启动从redis（192.168.9.19） /etc/init.d/redis_6379.conf start

查看主redis信息

#查看主redis的信息

- redis-cli-h192.168.9.18 infoReplication

#Replication role:master#代表192.168.9.18:6379这台redis是主 conected_slaves:1 slave0:192.168.9.18,6379,online

查看从redis信息

#查看主redis的信息

- redis-cli-h192.168.9.19 infoReplication


#Replication role:slave#代表192.168.9.18:6379这台redis是主 master_host:192.168.9.18 master_port:6379 master_link_status:up master_last_io_seconds_ago:4 master_sync_in_progres:0 slave_priority:10 slave_read_only:1 conected_slaves:0

配置redis sentinel集群监控服务 1.添加⼀份redis sentinel 配置⽂件

vim/etc/redis/sentinel.conf

#redis-0 #sentinel实例之间的通讯端⼝

port26379

- #master1 sentinel monitor master1192.168.9.1863791 sentinel down-after-miliseconds master15 0 sentinel failover-timeout master19 0 sentinel can-failover master1 yes sentinel paralel-syncs master12

- #master2可以添加多组主从的redis监听


.

. .

2.有配置⽂件了，那么启动redis sentinel做redis集群监听redis-sentinel sentinel.conf --sentinel

好了，所有环境都搭好了。下⾯开始正式的演示 1.正常演示。

把主的redis启动 把从的redis启动 把redis sentinel 集群监听启动

观察redis sentinel ⽇志信息

![image 1](<redis主从切换的集群管理.note_images/imageFile1.png>)

这⾥很清楚地看到，从的redis加⼊了集群

[4925]15Oct03:42:21. 89*+slave slave192.168.9.19:6379192.168.9.196379@ master1 192.168.9.186379

执⾏以下命令，查看redis主从信息

[rot@localhost vagrant]# redis-cli-h192.168.9.17-p26379 infoSentinel #Sentinel sentinel_masters:1 sentinel_tilt:0 sentinel_runing_scripts:0 sentinel_scripts_queue_length:0

- master0:name=master1,status=ok,adres=192.168.9.18:6379,slaves=1,sentinels=1 那么表示⼀切都正常了。你的redis sentinel集群已经配置成功！


2.故障演示

- 2.1当主的redis 服务器岩机了，会发⽣什么情况呢？ 执⾏以下命令使⽤主的redis服务停⽌


redis-cli-h192.168.9.18-p6379 shutdown#表示把192.168.9.18这台redis关闭

![image 2](<redis主从切换的集群管理.note_images/imageFile2.png>)

这张图⽚很清晰地反应到，redis sentinel 监控到主的redis服务停⽌，然后⾃动把从的redis切换到 主。

再执⾏以下命令，查看redis主从信息

[rot@localhost vagrant]# redis-cli-h192.168. 3. 1-p26379 infoSentinel #Sentinel sentinel_masters:1 sentinel_tilt:0 sentinel_runing_scripts:0 sentinel_scripts_queue_length:0

- master0:name=master1,status=ok,adres=192.168.9.19:6379,slaves=1,sentinels=1


- 2.2 当我们已经发现，⼀台redis发⽣故障了，可能会收到⼀些故障信息，那么再把服务已关闭的 redis恢复服务状态，会发⽣怎么样的情况呢？


![image 3](<redis主从切换的集群管理.note_images/imageFile3.png>)

redis sentinel 集群服务，会把上次主redis重新加⼊服务中，但是他再以不是主的redis了，变成从 的reids。

