- 1.准备环境（zokeper01,zokeper02和zokeper03:由于paxos算法要求半数以上存活则为可⽤， 所以必须准备单数个服务器搭建环境） 注：环境准备中如果发现ping主机名很慢但是ping ip很快，则将/etc/resolv.conf中nameserver注释掉

- 2.在zokeper01上解压缩zokeper-3.4.5.tar.gz

- 3.配置环境变量ZOKEPER_HOME、PATH 配置hbase环境变量/etc/profile exportZOKEPER_HOME=/home/hadop/zokeper exportPATH=$PATH:$ZOKEPER_HOME/bin

- 4.准备两个⽂件夹data和log⽤来存放zokeper的数据和⽇志 本例中在ZOKEPER_HOME/data、ZOKEPER_HOME/log mkdir data mkdir log chmod 75 data/ chmod 75 log/ 注意，两个⽂件夹权限必须为75

- 5.data⽬录下⾯新建myid⽂件，myid的⽂件内容为：

- 1

- 6.在conf⽬录下⾯复制zo_sample.cfg⽂件为zo.cfg⽂件，并修改配置 dataDir=/home/hadop/zokeper/data dataLogDir=/home/hadop/zokeper/log

- server.1=master1 2 8 3 8
- server.2=master1ha:2 8 3 8
- server.3=master2 2 8 3 8


- 7.将zokeper1上的⽂件发送到zokeper2和3上

- scp -r ~/zokeper hadop@slave1:~/
- scp -r ~/zokeper hadop@slave2:~/
- scp -r ~/zokeper hadop@slave3:~/


- 8.修改每个zokeper上的myid⽂件


- 2或者3


- 9.启动3台机器上的zokeper（每⼀台需要独⽴启动） zkServer.sh start zkServer.sh start zkServer.sh start

- 10.检查启动是否成功 ⾸先jps查看进程 zkServer.sh status 其中两台是folower，⼀台是leader


# 停⽌leader上的zokeper，如果其余两台中⼀台leader则启动成功 如果再停掉⼀台则剩余的⼀台状态时不可⽤的，因为paxos算法要求必须有半数以上服务存活平台才为 可⽤状态

