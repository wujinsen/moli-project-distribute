http://www.05112.com/anquan/wlgf/2014/0525/10454.html 在已经成功安装Flume的基础上，本⽂将总结使⽤Flume进⾏数据的实时收集处理，具体步骤如下： 第⼀步 ，在$FLUME_HOME/conf⽬录下，编写Flume的配置⽂件，命名为flume_first_conf， 具体内容如下： #agent1表示代理名称 agent1.sources=source1 agent1.sinks=sink1 agent1.channels=channel1

#Spooling Directory是监控指定⽂件夹中新⽂件的变化，⼀旦新⽂件出现， #就解析该⽂件内容， #然后写⼊到channle。写⼊完成后，标记该⽂件已完成或者删除该⽂件。 #配置source1 agent1.sources.source1.type=spooldir agent1.sources.source1.spoolDir=/home/yujianxin/hmbbs #配置往channel1传输数据 agent1.sources.source1.channels=channel1 agent1.sources.source1.fileHeader = false agent1.sources.source1.interceptors = i1 agent1.sources.source1.interceptors.i1.type = timestamp

#配置channel1 agent1.channels.channel1.type=file agent1.channels.channel1.checkpointDir=/home/yujianxin/hmbbs/hmbbs_tmp123 agent1.channels.channel1.dataDirs=/home/yujianxin/hmbbs/hmbbs_tmp

#配置sink1 agent1.sinks.sink1.type=hdfs agent1.sinks.sink1.hdfs.path=hdfs://slave3:9000/hmbbs agent1.sinks.sink1.hdfs.fileType=DataStream agent1.sinks.sink1.hdfs.writeFormat=TEXT agent1.sinks.sink1.hdfs.rollInterval=1 #配置从channel1接收数据 agent1.sinks.sink1.channel=channel1 #配置⽂件写⼊HDFS后的前缀 agent1.sinks.sink1.hdfs.filePrefix=%Y-%m-%d

第⼆步 ，编写Shell脚本，执⾏Flume任务 vi flume1.sh #!/bin/sh flume-ng agent -n agent1 -c conf

- -f /home/yujianxin/flume/apache-flume-1.4.0-bin/conf/flume_first_conf
- -Dflume.root.logger=DEBUG,console >./flume1.log 2>&1 &


让⽇志收集任务以后台进程运⾏，且将运⾏⽇志重定向到 ./flume1.log 保存。 验证 通过命令⾏查看新启的进程

![image 1](<使用Flume进行数据的实时收集处理.note_images/imageFile1.png>)

不断往Flume监控的⽬录/home/yujianxin/hmbbs1 下放置⽂件

![image 2](<使用Flume进行数据的实时收集处理.note_images/imageFile2.png>)

查看./flume.log运⾏⽇志，截部分关键图如下

![image 3](<使用Flume进行数据的实时收集处理.note_images/imageFile3.png>)

![image 4](<使用Flume进行数据的实时收集处理.note_images/imageFile4.png>)

查看 成功上传到HDFS中的⽂件

![image 5](<使用Flume进行数据的实时收集处理.note_images/imageFile5.png>)

# OK！使⽤Flume⾃动监控指定⽬录下⽂件的变化，⾃动处理，上传到HDFS。⽐以前⾃⼰写Shell脚本 ⽅便、快捷、⾼效多了，哈哈

