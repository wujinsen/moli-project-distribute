#broker的全局唯⼀编号，不能重复 broker.id=0

#⽤来监听链接的端⼝，producer或consumer将在此端⼝建⽴连接 port=9092

#处理⽹络请求的线程数量 num.network.threads=3

#⽤来处理磁盘IO的线程数量 num.io.threads=8

#发送套接字的缓冲区⼤⼩ socket.send.bufer.bytes=10240

#接受套接字的缓冲区⼤⼩ socket.receive.bufer.bytes=10240

#请求套接字的缓冲区⼤⼩ socket.request.max.bytes=10485760

#kafka运⾏⽇志存放的路径 log.dirs=/export/servers/logs/kafka

#topic在当前broker上的分⽚个数 num.partitions=2

#⽤来恢复和清理data下数据的线程数量 num.recovery.threads.per.data.dir=1

#segment⽂件保留的最⻓时间，超时将被删除 log.retention.hours=168

#滚动⽣成新的segment⽂件的最⼤时间 log.rol.hours=168

#⽇志⽂件中每个segment的⼤⼩，默认为1G

log.segment.bytes=1073741824

#周期性检查⽂件⼤⼩的时间 log.retention.check.interval.ms=3 0

#⽇志清理是否打开 log.cleaner.enable=true

#broker需要使⽤zokeper保存meta数据 zokeper.conect=192.168.52.106 2181,192.168.52.107 2181,192.168.52.108 2181

#zokeper链接超时时间 zokeper.conection.timeout.ms=6 0

#partion bufer中，消息的条数达到阈值，将触发flush到磁盘 log.flush.interval.mesages=1 0

#消息bufer的时间，达到阈值，将触发flush到磁盘 log.flush.interval.ms=3 0

#删除topic需要server.properties中设置delete.topic.enable=true否则只是标记删除 delete.topic.enable=true

#此处的host.name为本机IP(重要),如果不改,则客户端会抛 出:Producer conection to localhost:9092 unsucesful 错误! host.name=kafka01

