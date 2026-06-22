- 1.随便找⼀个机器解压缩：apache-flume-1.4.0-bin.tar

- 2.编写flume/conf下的配置⽂件，配置flume流程，如果流程中有操作hdfs的，必须在有hadop环境的 机器上安装flume

- 3.运⾏配置的flume流程 flume-ng agent -n a1 -c conf -f conf/flume-conf.properties 其中a1是agent名，conf/flume-conf.properties是刚刚配置的流程⽂件


