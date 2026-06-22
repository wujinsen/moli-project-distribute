本帖最后由 pig2 于 2014-7-16 02 56 编辑

问题导读：1.什么是flume？

- 2.如何安装flume？3.flume的配置⽂件与其它软件有什么不同？ ⼀、认识flume


- 1.flume是什么？ 这⾥简单介绍⼀下，它是Cloudera的⼀个产品
- 2.flume是⼲什么的？

收集⽇志的

- 3.flume如何搜集⽇志？


我们把flume⽐作情报⼈员

- （1）搜集信息
- （2）获取记忆信息
- （3）传递报告间谍信息


flume是怎么完成上⾯三件事情的，三个组件：

source： 搜集信息

chanel：传递信息

sink：存储信息

上⾯有点简练，详细可以参考

Flume内置chanel,source,sink三组件介绍

上⾯我们认识了，flume。

下⾯我们来安装flume1.5

![image 1](<让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2.note_images/imageFile1.png>)

⼆、安装flume1.5

- 1.下载安装包


- （1）官⽹下载


apache-flume-1.5.0-bin.tar.gz apache-flume-1.5.0-src.tar.gz

- （2）百度⽹盘下载


链接:

htp:/pan.baidu.com/s/1dDip8RZ

密码: 268r

我们⾛到这⼀步，我们会想到⼀个问题，我的电脑是32位的，不知道能否安装？如果我的电脑是64 位的，能否安装。之前我们装的hadop就分为32位和64位，想到这个问题是正常的，但是这⾥不⽤ 担⼼，因为我们下载的是⼆进制包，也就是说你32位和64位都可以安装。

- 2.分别解压： 下载之后，我们看到下⾯两个包：


- （1）上传Linux

上⾯两个包，可以下载window，然后通过WinSCP,如果不会

- （2）解压包


![image 2](<让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2.note_images/imageFile2.png>)

新⼿指导：使⽤ WinSCP（下载） 上⽂件到 Linux图⽂教程

解压apache-flume-1.5.0-bin.tar.gz， 解压到usr⽂件夹下⾯

1.

sudo tar zxvf apache-flume-1.5.0-bin.tar.gz

复制代码

![image 3](<让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2.note_images/imageFile3.png>)

解压apache-flume-1.5.0-src.tar.gz， 解压到usr⽂件夹下⾯

1.

sudo tar zxvf apache-flume-1.5.0-src.tar.gz

复制代码

![image 4](<让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2.note_images/imageFile4.png>)

- (3) src⾥⾯⽂件内容，覆盖解压后bin⽂件⾥⾯的内容


1.

sudo cp -ri apache-flume-1.5.0-src/* apache-flume-1.5.0-bin

复制代码

![image 5](<让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2.note_images/imageFile5.png>)

# (4)重命名

1.

mv apache-flume-1.5.0-bin/ flume

复制代码

![image 6](<让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2.note_images/imageFile6.png>)

- 3.配置环境变量：


![image 7](<让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2.note_images/imageFile7.png>)

配置环境变量⽣效

1.

source /etc/environment

复制代码

- 3.建⽴配置⽂件 这⾥⾯的配置⽂件还是⽐较特别的，不同于以往我们安装的软件，我们这⾥可以⾃⼰建⽴配置⽂件。


⾸先我们建⽴⼀个 example⽂件

1.

vi example

复制代码

，然后把下⾯内容，粘帖到⾥⾯就可以了，注意不要有乱码，有乱码的话，可以直接创建⼀个⽂件， 然后上传。⽅法也有很多，能解决就好。

对于下⾯红字部分，记得创建

⽂件夹

，并且注意他们的权限⼀致，这个⽐较简单的，就不在书写了。对于下⾯的配置项，可以参考

flume参考⽂档

，这⾥⾯的参数很详细。

agent1表示代理名称 agent1.sources=source1 agent1.sinks=sink1 agent1.chanels=chanel1

#配置source1 agent1.sources.source1.type=spoldir agent1.sources.source1.spolDir=

/usr/aboutyunlog agent1.sources.source1.chanels=chanel1 agent1.sources.source1.fileHeader = false

#配置sink1 agent1.sinks.sink1.type=hdfs agent1.sinks.sink1.hdfs.path=

hdfs:/ :8020/aboutyunlog agent1.sinks.sink1.hdfs.fileType=DataStream agent1.sinks.sink1.hdfs.writeFormat=TEXT agent1.sinks.sink1.hdfs.rolInterval=4 agent1.sinks.sink1.chanel=chanel1

master

#配置chanel1 agent1.chanels.chanel1.type=file agent1.chanels.chanel1.checkpointDir=

/usr/aboutyun_tmp123 agent1.chanels.chanel1.dataDirs= /usr/aboutyun_tmp

![image 8](<让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2.note_images/imageFile8.png>)

- 4.启动flume

flume-ng agent -n agent1 -c conf -f / usr/flume/conf/example

-Dflume.rot.loger=DEBUG,console

上⾯注意红字部分，是我们⾃⼰建⽴的⽂件，⽽对于绿⾊部分，则是输出调试信息，也可以在配置⽂ 件中配置。

- 5.我们启动flume之后


会看到下⾯信息，并且信息不停的重复。这个其实是在 空⽂件的时候， 监控的信息输出。

![image 9](<让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2.note_images/imageFile9.png>)

⼀旦有⽂件输⼊，我们会看到下⾯信息。

shel 监控

注意：这个不要关闭，我们另外开启⼀个 ，在 ⽂件夹中放⼊要上传的⽂件

⽐如我们在监控⽂件夹下，创建⼀个test1⽂件，内容如下

这时候flume监控shel，会有相应的如下下⾯变化

2014-06-02 12 01 04,06 (pol-6-thread-1) [INFO org.apache.flume.client.avro.ReliableSpolingFileEventReader.rolCurentFile(ReliableSpolingFile EventReader.java: 32)] Preparing to move file /usr/aboutyunlog/test1 to /usr/aboutyunlog/test1.COMPLETED

2014-06-02 12 01 04,070 (pol-6-thread-1) [EROR org.apache.flume.source.SpolDirectorySource$SpolDirectoryRunable.run(SpolDirectorySou rce.java:256)] FATAL: Spol Directory source source1: { spolDir: /usr/aboutyunlog }: Uncaught exception in SpolDirectorySource thread. Restart or reconfigure Flume to continue procesing.

java.lang.IlegalStateException: File name has ben re-used with diferent files. Spoling asumptions violated for /usr/aboutyunlog/test1.COMPLETED

at org.apache.flume.client.avro.ReliableSpolingFileEventReader.rolCurentFile(ReliableSpolingFile EventReader.java:362)

at org.apache.flume.client.avro.ReliableSpolingFileEventReader.retireCurentFile(ReliableSpolingF ileEventReader.java:314)

at org.apache.flume.client.avro.ReliableSpolingFileEventReader.readEvents(ReliableSpolingFileEv entReader.java:243)

at org.apache.flume.source.SpolDirectorySource$SpolDirectoryRunable.run(SpolDirectorySou rce.java: 27)

at java.util.concurent.Executors$RunableAdapter.cal(Executors.java:471) at java.util.concurent.FutureTask.runAndReset(FutureTask.java:304) at java.util.concurent.ScheduledThreadPolExecutor$ScheduledFutureTask.aces$301(Schedule dThreadPolExecutor.java:178) at java.util.concurent.ScheduledThreadPolExecutor$ScheduledFutureTask.run(ScheduledThreadP

olExecutor.java:293) at java.util.concurent.ThreadPolExecutor.runWorker(ThreadPolExecutor.java:145) at java.util.concurent.ThreadPolExecutor$Worker.run(ThreadPolExecutor.java:615) at java.lang.Thread.run(Thread.java:74) 2014-06-02 12 01 07,749 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.hdfs.HDFSDataStream.configure(HDFSDataStream.java:58)] Serializer = TEXT, UseRawLocalFileSystem = false 2014-06-02 12 01 07,803 (SinkRuner-PolingRuner-DefaultSinkProcesor) [INFO org.apache.flume.sink.hdfs.BucketWriter.open(BucketWriter.java:261)] Creating hdfs:/ :8020/aboutyunlog/FlumeData.14016816750.tmp 2014-06-02 12 01 07,871 (hdfs-sink1-cal-runer-2) [DEBUG org.apache.flume.sink.hdfs.AbstractHDFSWriter.reflectGetNumCurentReplicas(AbstractHDFSWr iter.java:195)] Using getNumCurentReplicas-HDFS-826

master

2014-06-02 12 01 07,871 (hdfs-sink1-cal-runer-2) [DEBUG org.apache.flume.sink.hdfs.AbstractHDFSWriter.reflectGetDefaultReplication(AbstractHDFSWrite r.java: 23)] Using FileSystem.getDefaultReplication(Path) from HADOP-8014

2014-06-02 12 01 10,945 (Log-BackgroundWorker-chanel1) [INFO org.apache.flume.chanel.file.EventQueueBackingStoreFile.beginCheckpoint(EventQueueBackin gStoreFile.java:214)] Start checkpoint for /usr/aboutyun_tmp123/checkpoint, elements to sync =

- 3


2014-06-02 12 01 10,949 (Log-BackgroundWorker-chanel1) [INFO org.apache.flume.chanel.file.EventQueueBackingStoreFile.checkpoint(EventQueueBackingStore File.java:239)] Updating checkpoint metadata: logWriteOrderID: 140168143098, queueSize: 0, queueHead: 1

- 2014-06-02 12 01 10,952 (Log-BackgroundWorker-chanel1) [INFO org.apache.flume.chanel.file.Log.writeCheckpoint(Log.java:105)] Updated checkpoint for file: /usr/aboutyun_tmp/log-8 position: 2482 logWriteOrderID: 140168143098
- 2014-06-02 12 01 10,953 (Log-BackgroundWorker-chanel) [DEBUG org.apache.flume.chanel.file.Log.removeOldLogs(Log.java:1067)] Files curently in use: [8]


- 2014-06-02 12 01 1,872 (hdfs-sink1-rol-timer-0) [DEBUG -

- org.apache.flume.sink.hdfs.BucketWriter$2.cal(BucketWriter.java:303)] Roling file (hdfs:/master:8020/aboutyunlog/FlumeData.14016816750.tmp): Rol scheduled after 4 sec elapsed.

2014-06-02 12 01 1,873 (hdfs-sink1-rol-timer-0) [INFO org.apache.flume.sink.hdfs.BucketWriter.close(BucketWriter.java:409)] Closing hdfs:/ :8020/aboutyunlog/FlumeData.14016816750.tmp

2014-06-02 12 01 1,873 (hdfs-sink1-cal-runer-7) [INFO -

- org.apache.flume.sink.hdfs.BucketWriter$3.cal(BucketWriter.java: 39)] Close tries incremented




master

2014-06-02 12 01 1,895 (hdfs-sink1-cal-runer-8) [INFO org.apache.flume.sink.hdfs.BucketWriter$8.cal(BucketWriter.java: 69)] Renaming hdfs:/

mas ter

:8020/aboutyunlog/FlumeData.14016816750.tmp to hdfs:/master:8020/aboutyunlog/FlumeData.14016816750 2014-06-02 12 01 1,897 (hdfs-sink1-rol-timer-0) [INFO org.apache.flume.sink.hdfs.HDFSEventSink$1.run(HDFSEventSink.java:402)] Writer calback caled. 2014-06-02 12 01 12,423 (conf-file-poler-0) [DEBUG org.apache.flume.node.PolingPropertiesFileConfigurationProvider$FileWatcherRunable.run(Pol ingPropertiesFileConfigurationProvider.java:126)] Checking file:conf/example for changes 2014-06-02 12 01 40,953 (Log-BackgroundWorker-chanel1) [DEBUG org.apache.flume.chanel.file.FlumeEventQueue.checkpoint(FlumeEventQueue.java:137)] Checkpoint not required 上传成功之后，我们去hdfs上，查看上传⽂件：

![image 10](<让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2.note_images/imageFile10.png>)

这样我们做到了flume上传到hadop2.2。

完毕

