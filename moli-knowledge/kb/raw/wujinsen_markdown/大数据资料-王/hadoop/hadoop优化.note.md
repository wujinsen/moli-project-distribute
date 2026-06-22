查看linux的服务，可以关闭不必要的服务

ntsysv 停⽌打印服务#/etc/init.d/cups stop#chkconfig cups of 关闭ipv6 #vim /etc/modprobe.conf 添加内容 alias net-pf-10 of alias ipv6 of

调整⽂件最⼤打开数 查看： ulimit -a 结果：open files (-n) 1024 临时修改： ulimit -n 4096持久修改：

vi /etc/security/limits.conf 在⽂件最后加上两⾏

* soft nofile 6535* hard nofile 6535* soft nproc 6535* hard nproc 6535

修改linux内核参数 vi /etc/sysctl.conf 添加 net.core.somaxcon = 32768 #web应⽤中listen函数的backlog默认会给我们内核参数的net.core.somaxcon限制到128，⽽nginx定 义的NGX_LISTEN_BACKLOG默认为51，所以有必要调整这个值。 调整swap分区什么时候使⽤： 查看：cat /proc/sys/vm/swapines 设置：vi /etc/sysctl.conf

在这个⽂档的最后加上这样⼀⾏: vm.swapines=10 表示物理内存使⽤到90%（10-10=90）的时候才使⽤swap交换区

关闭noatime

vi /etc/fstab /dev/sda2 /data ext3 noatime,nodiratime 0 0 设置readahead buferblockdev-setra READAHEAD 512 /dev/sda

修改最⼤槽位数

槽位数是在各个tasktracker上的mapred-site.xml上设置的，默认都是2 <property>

<name>mapred.tasktracker.map.tasks.maximum</name> # +maptask的最⼤数 <value>2</value>

</property> <property>

<name>mapred.tasktracker.reduce.tasks.maximum</name> # +reducetask的最⼤数 <value>2</value>

</property> 调整⼼跳间隔 集群规模⼩于30是，⼼跳间隔为30毫秒 mapreduce.jobtracker.heartbeat.interval.min ⼼跳时间 mapred.heartbeats.in.second 集群每增加多少节点，时间增加下⾯的值

mapreduce.jobtracker.heartbeat.scaling.factor 集群每增加上⾯的个数，⼼跳增多少

启动带外⼼跳 mapreduce.tasktracker.outofband.heartbeat 默认是false 配置多块磁盘 mapreduce.local.dir 配置RPC hander数⽬ mapred.job.tracker.handler.count 默认是10，可以改成50，根据机器的能⼒ 配置HTP线程数⽬ tasktracker.htp.threads 默认是40，可以改成10 根据机器的能⼒ 选择合适的压缩⽅式 以snapy为例： <property>

<name>mapred.compres.map.output</name> <value>true</value>

</property> <property>

<name>mapred.map.output.compresion.codec</name> <value>org.apache.hadop.io.compres.SnapyCodec</value>

</property> 启⽤推测执⾏机制 mapred.map.tasks.speculative.execution 默认是true mapred.rduce.tasks.speculative.execution 默认是true

设置是失败容忍度 mapred.max.map.failures.percent 作业允许失败的map最⼤⽐例 默认值0，即0%

mapred.max.reduce.failures.percent作业允许失败的reduce最⼤⽐例 默认值0，即0% mapred.map.max.atemps 失败后最多重新尝试的次数 默认是4 mapred.reduce.max.atemps 失败后最多重新尝试的次数 默认是4

启动jvm重⽤功能 mapred.job.reuse.jvm.num.tasks 默认值1，表示只能启动⼀个task，若为-1，表示可以最多运⾏数不 限制 设置任务超时时间 mapred.task.timeout 默认值6 0毫秒，也就是10分钟。 合理的控制reduce的启动时间 mapred.reduce.slowstart.completed.maps 默认值0.05 表示map任务完成5%时，开始启动reduce任 务 跳过坏记录

当任务失败次数达到该值时，才会进⼊skip mode，即启⽤跳过坏记录数功能,也就是先试⼏次，不⾏ 就开启

mapred.skip.atempts.to.start.ski ping 默认值 2

map最多允许跳过的记录数 mapred.skip.map.max.skip.records 默认值0，为不启⽤

reduce最多允许跳过的记录数 mapred.skip.reduce.max.skip.records 默认值0，为不启⽤ 换记录存放的⽬录mapred.skip.out.dir 默认值${mapred.output.dir}/_logs/

