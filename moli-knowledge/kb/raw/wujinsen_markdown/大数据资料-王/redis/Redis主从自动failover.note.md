Redis主从架构持久化存在⼀个问题，即前次测试的结论，持久化需要配置在主实例上才能跨越实例保 证数据不丢失，这样以来主实例在持久化数据到硬盘的过程中，势必会造成磁盘的I/O等待，经过实际 测试，这个持久化写硬盘的过程给应⽤程序带来的影响⽆法忍受；因⽽在⼤多数场景下，会考虑把持 久化配置在从实例上，当主实例宕机后，通过⼿动或者⾃动的⽅式将从实例提升为主实例，继续提供 服务！当主实例恢复后，先从原从实例上同步数据，同步完成后再恢复到原始的主从状态！要实现这 种的要求，需要有kepalive的配合，⼀⽅⾯kepalive提供了VIP，可以避免修改应⽤程序连接，同时 redis实例的配置⽂件监听部分也需要修改为全⽹监听；另⼀⽅⾯kepalive定时调度脚本来监控主从实 例的状态，根据具体情况进⾏切换！本⽂将重点介绍下使⽤kepalive实现redis主从⾃动failover！ 环境介绍 操作系统版本均为：rhel5.4 64bit redis版本：2.6.4 redis实例端⼝均为：6379 redis实例密码均为：123 VIP：192.168.1.120 主实例为server1(192.168.1.12) 从实例为server12(192.168.1.13，开启快照持久化) ⼀：安装kepalive软件,server1安装完成后直接scp⾄server12上即可 [rot@server1 ~]# wget

htp:/kepalived.org/software/kepalived-1.1.19.tar.gz

[rot@server1 ~]# tar -zxvf ./tarbag/kepalived-1.1.19.tar.gz [rot@server1 ~]# cd kepalived1.1.19/ [rot@server1 ~]# ./configure-prefix=/usr/local/kepalived & make & make instal ⼆：配置主节点server1配置⽂件 [rot@server1 ~]# cat /usr/local/kepalived/etc/kepalived/kepalived.conf ! Configuration File for kepalived global_defs { router_id LVS_DEVEL } vrp_script Monitor_redis { script "/usr/local/scripts/redis_monitor.sh" interval 2 weight 2 } vrp_instance VI_1{ state MASTER interface eth0 virtual_router_id 51 mcast_src_ip 192.168.1.12 priority 10 advert_int 1 authentication { auth_type PAS auth_pas pasword_123 } track_script { Monitor_redis } virtual_ipadres { 192.168.1.120 } notify_fault/usr/local/scripts/redis_fault.sh notify_stop/usr/local/scripts/redis_stop.sh }

三：配置从节点server12配置⽂件

[rot@server12 ~]# cat /usr/local/kepalived/etc/kepalived/kepalived.conf

! Configuration File for kepalived global_defs { router_id LVS_DEVEL } vrp_script Monitor_redis { script "/usr/local/scripts/redis_monitor.sh" interval 2 weight 2 } vrp_instance VI_1{ state BACKUP interface eth0 virtual_router_id 51 mcast_src_ip 192.168.1.13 priority 9 advert_int 1 authentication { auth_type PAS auth_pas pasword_123 } track_script { Monitor_redis } virtual_ipadres { 192.168.1.120 } notify_master /usr/local/scripts/redis_master.sh notify_backup /usr/local/scripts/redis_backup.sh notify_fault/usr/local/scripts/redis_fault.sh notify_stop/usr/local/scripts/redis_stop.sh }

四：准备相关的脚本，主从实例上都需要存在这些脚本，同时注意脚本需要由可执⾏权限 [rot@server1 ~]# cat /usr/local/scripts/redis_monitor.sh #!/bin/bash

ALIVE=$(/usr/local/redis2/bin/redis-cli -h 192.168.1.12 -p 6379 -a 123 PING) if [ "$ALIVE" = "PONG" ]; then echo $ALIVE exit 0 else echo $ALIVE

kilal -9 kepalived service network restart exit 1 fi [rot@server1 ~]# sh /usr/local/scripts/redis_monitor.sh PONG [rot@server1 ~]# cat /usr/local/scripts/redis_master.sh #!/bin/bash

REDISCLI="/usr/local/redis2/bin/redis-cli -h 192.168.1.12 -p 6379 -a 123" LOGFILE="/usr/local/redis2/var/kepalived-redis-state.log" echo "[master]" > $LOGFILE date > $LOGFILE echo "Being master ." > $LOGFILE 2>&1 echo "Run SLAVEOF cmd." > $LOGFILE $REDISCLI SLAVEOF 192.168.1.13 6379 > $LOGFILE 2>&1 sl ep 10 echo "Run SLAVEOF NO ONE cmd." > $LOGFILE $REDISCLI SLAVEOF NO ONE > $LOGFILE 2>&1

[rot@server1 ~]# cat /usr/local/scripts/redis_backup.sh #!/bin/bash REDISCLI="/usr/local/redis2/bin/redis-cli -h 192.168.1.12 -p 6379 -a 123" LOGFILE="/usr/local/redis2/var/kepalived-redis-state.log" echo "[backup]" > $LOGFILE date > $LOGFILE echo "Being slave." > $LOGFILE 2>&1 sl ep 15 echo "Run SLAVEOF cmd." > $LOGFILE $REDISCLI SLAVEOF 192.168.1.13 6379 > $LOGFILE 2>&1

[rot@server1 ~]# cat /usr/local/scripts/redis_stop.sh #!/bin/bash LOGFILE="/usr/local/redis2/var/kepalived-redis-state.log" echo "[stop]" > $LOGFILE date > $LOGFILE[rot@server1 ~]# cat /usr/local/scripts/redis_fault.sh #!/bin/bash

LOGFILE="/usr/local/redis2/var/kepalived-redis-state.log" echo "[fault]" > $LOGFILE date > $LOGFILE 五：主从实例分别启动kepalive进程，测试VIP是否正常(这⾥就要修改redis配置⽂件的监听地址为 0.0.0.0)

[rot@server1 ~]# /usr/local/kepalived/sbin/kepalived -D f/usr/local/kepalived/etc/kepalived/kepalived.conf [rot@server1 ~]# tail -f /var/log/mesages

- Dec 12 09 25 49 server1 Kepalived_healthcheckers[710]: Configuration is using : 549 Bytes


- Dec 12 09 25 49 server1 Kepalived_healthcheckers[710]: Using LinkWatch kernel netlink reflec tor.


- Dec 12 09 25 49 server1 Kepalived_vrp[712]: VRP sockpol: [ifindex(2), proto(12), fd(12,13)

- ] Dec 12 09 25 49 server1 Kepalived_vrp[712]: VRP_Script(Monitor_redis) suceded

- Dec 12 09 25 50 server1 Kepalived_vrp[712]: VRP_Instance(VI_1{) Transition to MASTER STA TE

- Dec 12 09 25 51 server1 Kepalived_vrp[712]: VRP_Instance(VI_1{) Entering MASTER STATE


- Dec 12 09 25 51 server1 Kepalived_vrp[712]: VRP_Instance(VI_1{) seting protocol VIPs.


- Dec 12 09 25 51 server1 Kepalived_vrp[712]: VRP_Instance(VI_1{) Sending gratuitous ARPs o n eth0 for 192.168.1.120 Dec 12 09 25 51 server1 avahidaemon[4519]: Registering new adres record for 192.168.1.120 on eth0.

- Dec 12 09 25 51 server1 Kepalived_healthcheckers[710]: Netlink reflector reports IP 192.168.1.1 20 aded

- Dec 12 09 25 51 server1 Kepalived_vrp[712]: Netlink reflector reports IP 192.168.1.120 aded

- Dec 12 09 25 56 server1 Kepalived_vrp[712]: VRP_Instance(VI_1{) Sending gratuitous ARPs on eth0 for 192.168.1.120 [rot@server1 ~]# ip a |grep 192

inet 192.168.1.12/24 brd 192.168.1.25 scope global eth0 inet 192.168.1.120/32 scope global eth0

[rot@server12 ~]# /usr/local/kepalived/sbin/kepalived -D f /usr/local/kepalived/etc/kepalived/kepalived.conf [rot@server12 ~]# tail -f /var/log/mesages

- Dec 12 09 26  5 server12 Kepalived_healthcheckers[3106]: Configuration is using : 595 Bytes


- Dec 12 09 26  5 server12 Kepalived_vrp[3108]: VRP_Instance(VI_1{) Entering BACKUP STATE


- Dec 12 09 26  5 server12 Kepalived_healthcheckers[3106]: Using LinkWatch kernel netlink refle ctor.


- Dec 12 09 26  5 server12 Kepalived_vrp[3108]: VRP sockpol: [ifindex(2), proto(12), fd(12,13)


- ] Dec 12 09 26  5 server12 Kepalived_vrp[3108]: VRP_Script(Monitor_redis) suceded [rot@server1 ~]# /usr/local/redis2/bin/redis-cli -h 192.168.1.120 -a 123 info |grep -




A 3 'Replication' # Replication role:master conected_slaves:1 slave0 192.168.1.13,6379,online 六：主实例写⼊测试数据，该脚本原则上会写⼊25条测试数据，不过由于未优化redis默认并发数，会 导致⼀些写⼊请求失败，最终功写⼊231839条测试数据，占内存总⼤⼩为25M左右，写⼊过程中可以 观察主从实例的持久化⽂件变化情况，主实例的持久化⽂件维持在30k，从实例的则不断的扩展！

[rot@server1 ~]# cat test.sh #!/bin/bash REDISCLI="/usr/local/redis2/bin/redis-cli h 192.168.1.120 -a 123 -n 1 SET" ID=1 while($ID<5 01) doINSTANCE_NAME="i-2-$IDVM" UID=`cat /proc/sys/kernel/random/uid`

PRIVATE_IP_ADRES=10.`echo "$RANDOM % 25 + 1" | bc`.`echo "$RANDOM % 25 + 1" | bc`

.`echo "$RANDOM % 25 + 1" | bc`\ CREATED=`date "+%Y-%m-%d %H:%M:%S"` $REDISCLI vm_instance:$ID:instance_name "$INSTANCE_NAME" $REDISCLI vm_instance:$ID:uid "$UID" $REDISCLI vm_instance:$ID:private_ip_adres "$PRIVATE_IP_ADRES" $REDISCLI vm_instance:$ID:created "$CREATED" $REDISCLI vm_instance:$INSTANCE_NAME:id "$ID" ID=$($ID+1) done

[rot@server1 ~]# sh test.sh [rot@server1 redis2]# /usr/local/redis2/bin/redis-cli h 192.168.1.120 -a 123 info |egrep 'used_memory_peak_human|db1:keys'

used_memory_peak_human:24.98Mdb1:keys=231839,expires=0

七：模拟主实例故障，观察⽇志输出，验证从实例是否能成功接管VIP，同时将实例变成读写模式

[rot@server1 ~]# kilal -9 redis-server [rot@server1 ~]# ip a |grep 192

inet 192.168.1.12/24 brd 192.168.1.25 scope global eth0 [rot@server1 ~]# ps -ef |grep redis rot 1586 6458 0 09 49 pts/0 0  0  0 grep redis [rot@server1 ~]# ps -ef |grep kep rot 16029 6458 0 09 49 pts/0 0  0  0 grep kep [rot@server12 ~]# tail -

f /usr/local/redis2/var/kepalived-redis-state.log [master] Wed Dec 12 09 48 52 CST 2012 Being master . Run SLAVEOF cmd. OK Already conected to specified master Run SLAVEOF NO ONE cmd. OK [rot@server12 ~]# tail -f /var/log/mesages

- Dec 12 09 48 51 server12 Kepalived_vrp[3108]: VRP_Instance(VI_1{) Transition to MASTER STA TE

- Dec 12 09 48 52 server12 Kepalived_vrp[3108]: VRP_Instance(VI_1{) Entering MASTER STATE


- Dec 12 09 48 52 server12 Kepalived_vrp[3108]: VRP_Instance(VI_1{) seting protocol VIPs.


- Dec 12 09 48 52 server12 Kepalived_vrp[3108]: VRP_Instance(VI_1{) Sending gratuitous ARPs on eth0 for 192.168.1.120


- Dec 12 09 48 52 server12 Kepalived_vrp[3108]: Netlink reflector reports IP 192.168.1.120 aded


- Dec 12 09 48 52 server12 avahidaemon[2921]: Registering new adres record for 192.168.1.120 on eth0.


- Dec 12 09 48 52 server12 Kepalived_healthcheckers[3106]: Netlink reflector reports IP 192.168.1. 120 aded


- Dec 12 09 48 57 server12 Kepalived_vrp[3108]: VRP_Instance(VI_1{) Sending gratuitous ARPs on eth0 for 192.168.1.120 [rot@server12 ~]# ip a |grep 192


inet 192.168.1.13/24 brd 192.168.1.25 scope global eth0 inet 192.168.1.120/32 scope global eth0 [rot@server12 ~]# /usr/local/redis2/bin/redis-cli -

- h 192.168.1.120 -a 123 info |grep -A 3 'Replication' # Replication role:master conected_slaves:0 [rot@server12 ~]# sh test.sh [rot@server12 ~]# /usr/local/redis2/bin/redis-cli -h 192.168.1.120 a 123 info |egrep 'used_memory_peak_human|db1:keys' used_memory_peak_human:26.78M

db1:keys=24925,expires=0 九:主实例⻆⾊的恢复过程，使⽤shel脚本⾃动恢复 [rot@server1 ~]#sh-keygen [rot@server1 ~]# cd .sh/ [rot@server1 .sh]#sh-copy-id -

- i id_rsa.pub rot@192.168.1.13 [rot@server1 ~]# cat /usr/local/scripts/recover_mastart.sh #!/bin/shALIVE=$(/usr/local/redis2/bin/redis-cli -h 192.168.1.13 -p 6379 -a 123 PING) MDB=/usr/local/redis2/master_dump.rdbSDB=/usr/local/redis2/slave_dump.rdb


if [ "$ALIVE" = "PONG" ]; then echo $ALIVE scp rot@192.168.1.13:$SDB $MDB else echo $ALIVE exit 1 fi /usr/local/redis2/bin/redis-

server /usr/local/redis2/etc/redis.conf /usr/local/kepalived/sbin/kepalived -D -f /usr/local/kepalived/etc/kepalived/kepalived.conf [rot@server1 ~]# chmod +x /usr/local/scripts/recover_mastart.sh

[rot@server1 ~]# sh /usr/local/scripts/recover_mastart.sh

# ⼗：验证数据完整性和主从⻆⾊恢复情况

[rot@server1 ~]# /usr/local/redis2/bin/redis-cli -h 192.168.1.120 a 123 info |egrep 'used_memory_peak_human|db1:keys' used_memory_peak_human:26.78M

db1:keys=24925,expires=0 [rot@server1 ~]# /usr/local/redis2/bin/redis-cli -h 192.168.1.120 a 123 info |grep -A 3 'Replication' # Replication role:master conected_slaves:1

slave0 192.168.1.13,6379,online [rot@server12 ~]# /usr/local/redis2/bin/redis-cli h 192.168.1.13 -a 123 info |grep -A 3 'Replication' # Replication role:slave

master_host:192.168.1.12 master_port:6379 [rot@server12 ~]# /usr/local/redis2/bin/redis-cli -

h 192.168.1.120 -a 123 info |egrep 'used_memory_peak_human|db1:keys' used_memory_peak_human:26.78Mdb1:keys=24925,expires=0 主实例kepalive⽇志： [rot@server1 ~]# tail -f /var/log/mesages

- Dec 12 10 08 13 server1 Kepalived_vrp[20231]: VRP sockpol: [ifindex(2), proto(12), fd(1,12) ] Dec 12 10 08 13 server1 Kepalived_vrp[20231]: VRP_Script(Monitor_redis) suceded


- Dec 12 10 08 13 server1 Kepalived_vrp[20231]: VRP_Instance(VI_1{) Transition to MASTER ST ATE


- Dec 12 10 08 13 server1 Kepalived_vrp[20231]: VRP_Instance(VI_1{) Received higher prio adv ert


- Dec 12 10 08 13 server1 Kepalived_vrp[20231]: VRP_Instance(VI_1{) Entering BACKUP STATE


- Dec 12 10 08 15 server1 Kepalived_vrp[20231]: VRP_Instance(VI_1{) forcing a new MASTER el ection


- Dec 12 10 08 16 server1 Kepalived_vrp[20231]: VRP_Instance(VI_1{) Transition to MASTER ST ATE


- Dec 12 10 08 17 server1 Kepalived_vrp[20231]: VRP_Instance(VI_1{) Entering MASTER STATE


- Dec 12 10 08 17 server1 Kepalived_vrp[20231]: VRP_Instance(VI_1{) seting protocol VIPs.


- Dec 12 10 08 17 server1 Kepalived_healthcheckers[20230]: Netlink reflector reports IP 192.168.1


.120 aded Dec 12 10 08 17 server1 Kepalived_vrp[20231]: VRP_Instance(VI_1{) Sending gratuitous ARPs

on eth0 for 192.168.1.120 Dec 12 10 08 17 server1 Kepalived_vrp[20231]: Netlink reflector reports IP 192.168.1.120 aded Dec 12 10 08 17 server1 avahi-

daemon[4519]: Registering new adres record for 192.168.1.120 on eth0. [rot@server1 ~]# ip a |grep 192 inet 192.168.1.12/24 brd 192.168.1.25 scope global eth0

inet 192.168.1.120/32 scope global eth0 从实例kepalive⽇志： [rot@server12 ~]# tail f /var/log/mesages Dec 12 09 56 01 server12 last mesage repeated 4 times

Dec 12 10 08 13 server12 Kepalived_vrp[3108]: VRP_Instance(VI_1{) Received lower prio advert , forcing new election

Dec 12 10 08 13 server12 Kepalived_vrp[3108]: VRP_Instance(VI_1{) Sending gratuitous ARPs o n eth0 for 192.168.1.120

Dec 12 10 08 15 server12 Kepalived_vrp[3108]: VRP_Instance(VI_1{) Received higher prio adve

rt Dec 12 10 08 15 server12 Kepalived_vrp[3108]: VRP_Instance(VI_1{) Entering BACKUP STATE Dec 12 10 08 15 server12 Kepalived_vrp[3108]: VRP_Instance(VI_1{) removing protocol VIPs. Dec 12 10 08 15 server12 Kepalived_healthcheckers[3106]: Netlink reflector reports IP 192.168.1.

120 removed

Dec 12 10 08 15 server12 Kepalived_vrp[3108]: Netlink reflector reports IP 192.168.1.120 remove d Dec 12 10 08 15 server12 avahidaemon[2921]: Withdrawing adres record for 192.168.1.120 on eth0. 从实例⻆⾊转换⽇志：

[rot@server12 ~]# tail -f /usr/local/redis2/var/kepalived-redis-state.log [backup] Wed Dec 12 10 08 15 CST 2012 Being slave. Run SLAVEOF cmd. OK

