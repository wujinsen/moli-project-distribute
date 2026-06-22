## 2.1.2 部署步骤

- 1) 部署WEB管理⼯具(实时监控使⽤) Ø 环境：JDK1.6、Tomcat7 Ø 将SharedCacheMonitor.war包放⼊tomcat/webaps ⽬录 执⾏tomcat/bin/startup.sh，启动tomcat

- 2) 部署Redis Ø 环境：JDK1.6 kepalived依赖库 rpm -ivhipvsadm-1.24-10.x86_64.rpm rpm -ivhkernel-devel-2.6.18-164.el5.x86_64.rpm


Ø 安装RPM

rpm-ivh ctfo-sharedcache-2.1.0-10.x86_64.rpm

或指定路径安装 rpm -ivh-prefix=/usr/local ctfo-sharedcache-2.1.0-4.x86_64.rpm

Ø 替换jar包 将rpm包安装后的⽂件夹中的⽂件夹ctfo-sharedcache\lib下的datacenter.jar和jedis.jar替换成⾃⼰的jar 修改 ctfo-initcache.sh ⽂件(把启动类的路径中的ctfo修改为dms，包括⼤⼩写)

Ø 配置节点

配置在当前机器上的节点，进⼊ctfo-sharedcache⽬录，修改tols/config.conf（例⼦在后⾯） 主

备： (注意key名称已comon开头的只需配置⼀次，每个机器都相同) 配置节点:地址和基本配置： comon_config_virtualI Port=0.0.0.0  0/配置虚拟地址 comon_config_masterI Port=0.0.0.0  0/配置主地址 comon_config_slaveI Port=0.0.0.0  0/配置从地址 comon_monitor_serverI Port=0.0.0.0 16 0/应⽤地址(端⼜⽆需修改)

数据节点地址：(整个共享缓存有⼏个数据节点就配置⼏次)

- comon_data_virtualI Port_1=0.0.0.0  0/节点1虚拟地址


- comon_data_masterI Port_1=0.0.0.0  0/节点1主地址


- comon_data_slaveI Port_1=0.0.0.0  0/节点1从地址


当前机器节点配置：(“当前机器”有⼏个节点就配置⼏次)⽤于kepalive redis_port_1=6379/redis端⼜ redis_logfile_1=/var/log/redis.log/redis⽇志 kepalived_state_1=MASTER/MASTER或者BACKUP kepalived_routerID_1=61/Kepalived唯⼀标识（主备⼀样） kepalived_virtualIP_1=0.0.0.0/节点1虚拟地址 kepalived_masterIP_1=0.0.0.0/节点1主地址 kepalived_slaveIP_1=0.0.0.0/节点1从地址 monitor_agentPort_1=1601/本地端⼜

修改完毕运⾏tols/init-config.sh脚本(只能在初始化使⽤此脚本，将所有shell脚本，转移到安装⽬录， 并且替换相应变量)

注意：在配置其他机器的时候只需将此⽂件替换已安装好的config.conf⽂件，只修改当前机器节点配 置就可以。 Ø 启动服务 进⼊ctfo-sharedcache⽬录，启动所有以ctfo-redis开头的脚本 shctfo-redis.sh start 启动Kepalived脚本 （确保⽹卡是eth0，否则需要修改ctfo-redis6383/keepalive.conf，将eth0修改为对应的⽹卡。如报其他 错，见下⽂） shctfo-kepalived.sh start

## 启动Kepalived报错

报错信息：

eror while loading shared libraries: libcrypto.so.6: canot open shared object file: No such file or directory

eror while loading shared libraries: libsl.so.6: canot open shared object file: No such file or directory

启动所有以ctfo-agent开头的脚本(实时监控使⽤，需要部署web管理⼯具，否则报错) sh ctfo-agent.shstart

初始化共享缓存(在配置服务器使⽤⼀次,⽤于初始化配置库，设置环境变量，执⾏启动类) shctfo-initcache.shstart

## 2.1.3 动态扩容步骤

Ø 配置扩容节点

配置在当前机器上扩容的节点，进⼊ctfo-sharedcache⽬录，修改tols/config.conf (注意key名称已comon开头的只需配置⼀次，每个机器都相同) 添加节点地址： comon_ad_virtualI Port_1=0.0.0.0  0/添加节点1虚拟地址 comon_ad_masterI Port_1=0.0.0.0  0/添加节点1主地址 comon_ad_slaveI Port_1=0.0.0.0  0/添加节点1从地址

数据节点地址：(在现有节点的基础上按顺序把扩容节点地址加⼊进去)

- comon_data_virtualI Port_2=0.0.0.0  0/节点2虚拟地址


- comon_data_masterI Port_2=0.0.0.0  0/节点2主地址


- comon_data_slaveI Port_2=0.0.0.0  0/节点2从地址


当前机器添加节点配置： redis_port_1=6379/redis端⼜ redis_logfile_1=/var/log/redis.log/redis⽇志 kepalived_state_1=MASTER/MASTER或者BACKUP kepalived_routerID_1=61/Kepalived唯⼀标识（主备⼀样） kepalived_virtualIP_1=0.0.0.0/节点1虚拟地址 kepalived_masterIP_1=0.0.0.0/节点1主地址 kepalived_slaveIP_1=0.0.0.0/节点1从地址 monitor_agentPort_1=1601/本地端⼜

修改完毕运⾏tols/server/ad-config.sh脚本 Ø 启动服务 进⼊ctfo-sharedcache⽬录，启动新添加以ctfo-redis开头的脚本 shctfo-redis.sh start 重加载Kepalived脚本 shctfo-kepalived.sh reload 启动新添加以ctfo-agent开头的脚本(实时监控使⽤) sh ctfo-agent.shstart

# 4.1 常见问题解答

4.1.1 启动Kepalived报错

报错信息： eror while loading shared libraries:libcrypto.so.6: canot open shared object file: No such file or directoryeror while loading shared libraries: libsl.so.6: canot open shared objectfile: No such file or directory

解决办法：从rhel5.X的版本上copy这两个⽂件（libcrypto.so.0.9.8e、libsl.so.0.9.8e）到rhel6.2上 scp rhel5.X：/lib/libcrypto.so.0.9.8erhel6.2：/libscp rhel5.X：/lib/libsl.so.0.9.8erhel6.2：/lib scp rhel5.X：/lib64/libcrypto.so.0.9.8erhel6.2：/lib64 scp rhel5.X：/lib64/libsl.so.0.9.8erhel6.2：/lib64 RHEL6.2:cd /lib/ln -s libcrypto.so.0.9.8e libcrypto.so.6 ln -s libsl.so.0.9.8e libsl.so.6cd /lib64ln -s libcrypto.so.0.9.8e libcrypto.so.6ln -s libsl.so.0.9.8e libsl.so.6

# comon_config # comon_config_virtualI Port=192.168.10.201 6383 comon_config_masterI Port=192.168.10.102 6383 comon_config_slaveI Port=192.168.10.103 6383 comon_monitor_serverI Port=192.168.10.102 16 0 comon_monitor_period=3 comon_ad_moveThread=5 comon_ad_deleteThread=5 comon_thrift_port=1690

# comon_data #

- comon_data_virtualI Port_1=192.168.10.201 6384

- comon_data_masterI Port_1=192.168.10.102 6384

- comon_data_slaveI Port_1=192.168.10.103 6384


comon_data_virtualI Port_2=192.168.10.201 6385

- comon_data_masterI Port_2=192.168.10.102 6385




- comon_data_slaveI Port_2=192.168.10.103 6385

comon_data_virtualI Port_3=192.168.10.201 6386 comon_data_masterI Port_3=192.168.10.102 6386

- comon_data_slaveI Port_3=192.168.10.103 6386


# redis_kepalived #

- redis_port_1=6383

- redis_logfile_1=/var/log/redis1.log

- kepalived_state_1=MASTER

- kepalived_routerID_1=61

- kepalived_virtualIP_1=192.168.10.201

- kepalived_masterIP_1=192.168.10.102

- kepalived_slaveIP_1=192.168.10.103

- monitor_agentPort_1=1601

redis_port_2=6384 redis_logfile_2=/var/log/redis2.log kepalived_state_2=MASTER kepalived_routerID_2=62 kepalived_virtualIP_2=192.168.10.201 kepalived_masterIP_2=192.168.10.102 kepalived_slaveIP_2=192.168.10.103

- monitor_agentPort_2=1602

redis_port_3=6385 redis_logfile_3=/var/log/redis3.log kepalived_state_3=MASTER kepalived_routerID_3=63 kepalived_virtualIP_3=192.168.10.201 kepalived_masterIP_3=192.168.10.102 kepalived_slaveIP_3=192.168.10.103

- monitor_agentPort_3=1603














- redis_port_4=6386


- redis_logfile_4=/var/log/redis4.log


- kepalived_state_4=MASTER


- kepalived_routerID_4=64


- kepalived_virtualIP_4=192.168.10.201


- kepalived_masterIP_4=192.168.10.102


- kepalived_slaveIP_4=192.168.10.103


- monitor_agentPort_4=1604


# comon_a data #

# comon_config # comon_config_virtualI Port=192.168.10.201 6383 comon_config_masterI Port=192.168.10.102 6383 comon_config_slaveI Port=192.168.10.103 6383 comon_monitor_serverI Port=192.168.10.102 16 0 comon_monitor_period=3 comon_ad_moveThread=5 comon_ad_deleteThread=5 comon_thrift_port=1690

# comon_data #

- comon_data_virtualI Port_1=192.168.10.201 6384

- comon_data_masterI Port_1=192.168.10.102 6384

- comon_data_slaveI Port_1=192.168.10.103 6384

comon_data_virtualI Port_2=192.168.10.201 6385 comon_data_masterI Port_2=192.168.10.102 6385

- comon_data_slaveI Port_2=192.168.10.103 6385






- comon_data_virtualI Port_3=192.168.10.201 6386


- comon_data_masterI Port_3=192.168.10.102 6386


- comon_data_slaveI Port_3=192.168.10.103 6386


# redis_kepalived #

- redis_port_1=6383 redis_logfile_1=/var/log/redis1.log kepalived_state_1=BACKUP kepalived_routerID_1=61 kepalived_virtualIP_1=192.168.10.201 kepalived_masterIP_1=192.168.10.102 kepalived_slaveIP_1=192.168.10.103 monitor_agentPort_1=1601
- redis_port_2=6384 redis_logfile_2=/var/log/redis2.log kepalived_state_2=BACKUP kepalived_routerID_2=62 kepalived_virtualIP_2=192.168.10.201 kepalived_masterIP_2=192.168.10.102 kepalived_slaveIP_2=192.168.10.103 monitor_agentPort_2=1602
- redis_port_3=6385 redis_logfile_3=/var/log/redis3.log kepalived_state_3=BACKUP kepalived_routerID_3=63 kepalived_virtualIP_3=192.168.10.201 kepalived_masterIP_3=192.168.10.102 kepalived_slaveIP_3=192.168.10.103 monitor_agentPort_3=1603
- redis_port_4=6386 redis_logfile_4=/var/log/redis4.log


kepalived_state_4=BACKUP kepalived_routerID_4=64 kepalived_virtualIP_4=192.168.10.201 kepalived_masterIP_4=192.168.10.102 kepalived_slaveIP_4=192.168.10.103 monitor_agentPort_4=1604

# comon_a data #

