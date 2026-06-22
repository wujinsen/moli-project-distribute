# 1：Shuffle Error: Exceeded MAX_FAILED_UNIQUE_FETCHES; bailing-out

Answer：

程序⾥⾯需要打开多个⽂件，进⾏分析，系统⼀般默认数量是1024，（⽤ulimit -a可以看到）对于 正常使⽤是够了，但是对于程序来讲，就太少了。

修改办法：

修改2个⽂件。

/etc/security/limits.conf

vi /etc/security/limits.conf

加上：

- * soft nofile 102400

- * hard nofile 409600


$cd /etc/pam.d/

$sudo vi login

添加 session required /lib/security/pam_limits.so

针对第⼀个问题我纠正下答案：

这是reduce预处理阶段shuffle时获取已完成的map的输出失败次数超过上限造成的，上限默认为 5。引起此问题的⽅式可能会有很多种，⽐如⽹络连接不正常，连接超时，带宽较差以及端⼝阻塞 等。。。通常框架内⽹络情况较好是不会出现此错误的。

# 2：Too many fetch-failures

Answer:

出现这个问题主要是结点间的连通不够全⾯。

- 1) 检查 、/etc/hosts


要求本机ip 对应 服务器名

要求要包含所有的服务器ip + 服务器名

- 2) 检查 .ssh/authorized_keys


要求包含所有服务器（包括其⾃身）的public key

- 3：处理速度特别的慢 出现map很快 但是reduce很慢 ⽽且反复出现 reduce=0%

Answer:

结合第⼆点，然后

修改 conf/hadoop-env.sh 中的export HADOOP_HEAPSIZE=4000

- 4：能够启动datanode，但⽆法访问，也⽆法结束的错误

在 重新格式化⼀个新的分布式⽂件时，需要将你NameNode上所配置的dfs.name.dir这⼀ namenode⽤来存放NameNode 持久存储名字空间及事务⽇志的本地⽂件系统路径删除，同时将 各DataNode上的dfs.data.dir的路径 DataNode 存放块数据的本地⽂件系统路径的⽬录也删除。如 本此配置就是在NameNode上删除/home/hadoop/NameData，在DataNode上 删 除/home/hadoop/DataNode1和/home/hadoop/DataNode2。这是因为Hadoop在格式化⼀个新的 分布式⽂件系 统时，每个存储的名字空间都对应了建⽴时间的那个版本（可以查看/home/hadoop /NameData/current⽬录下的VERSION⽂件，上⾯记录了版本信息），在重新格式化新的分布式 系统⽂件时，最好先删除NameData ⽬录。必须删除各DataNode的dfs.data.dir。这样才可以使 namedode和datanode记录的信息版本对应。

注意：删除是个很危险的动作，不能确认的情况下不能删除！！做好删除的⽂件等通通备份！！

- 5：java.io.IOException: Could not obtain block: blk_194219614024901469_1100 file=/user/hive/warehouse/src_20090724_log/src_20090724_log 出现这种情况⼤多是结点断了，没有连接上。

- 6：java.lang.OutOfMemoryError: Java heap space


出现这种异常，明显是jvm内存不够得原因，要修改所有的datanode的jvm内存⼤⼩。

Java -Xms1024m -Xmx4096m

⼀般jvm的最⼤内存使⽤应该为总内存⼤⼩的⼀半，我们使⽤的8G内存，所以设置为4096m，这 ⼀值可能依旧不是最优的值。

本主题由 admin 于 2009-11-20 10:50 置顶

顶，这样的贴⼦⾮常好，要置顶。附件是由Hadoop技术交流群中若冰的同学提供的相关资料：

(12.58 KB)

Hadoop添加节点的⽅法

⾃⼰实际添加节点过程：

- 1. 先在slave上配置好环境，包括ssh，jdk，相关config，lib，bin等的拷⻉；

- 2. 将新的datanode的host加到集群namenode及其他datanode中去；

- 3. 将新的datanode的ip加到master的conf/slaves中；

- 4. 重启cluster,在cluster中看到新的datanode节点；

- 5. 运⾏bin/start-balancer.sh，这个会很耗时间


备注：

- 1. 如果不balance，那么cluster会把新的数据都存放在新的node上，这样会降低mr的⼯作效率；

- 2. 也可调⽤bin/start-balancer.sh 命令执⾏，也可加参数 -threshold 5 threshold 是平衡阈值，默认是10%，值越低各节点越平衡，但消耗时间也更⻓。

- 3. balancer也可以在有mr job的cluster上运⾏，默认dfs.balance.bandwidthPerSec很低，为 1M/s。在没有mr job时，可以提⾼该设置加快负载均衡时间。


其他备注：

- 1. 必须确保slave的firewall已关闭;

- 2. 确保新的slave的ip已经添加到master及其他slaves的/etc/hosts中，反之也要将master及其他 slave的ip添加到新的slave的/etc/hosts中 mapper及reducer个数 url地址： http://wiki.apache.org/hadoop/HowManyMapsAndReduces


HowManyMapsAndReduces

Partitioning your job into maps and reduces

Picking the appropriate size for the tasks for your job can radically change the performance of Hadoop. Increasing the number of tasks increases the framework overhead, but increases load balancing and lowers the cost of failures. At one extreme is the 1 map/1 reduce case where nothing is distributed. The other extreme is to have 1,000,000 maps/ 1,000,000 reduces where the framework runs out of resources for the overhead.

Number of Maps

The number of maps is usually driven by the number of DFS blocks in the input files. Although that causes people to adjust their DFS block size to adjust the number of maps. The right level of parallelism for maps seems to be around 10-100 maps/node, although we have taken it up to 300 or so for very cpu-light map tasks. Task setup takes awhile, so it is best if the maps take at least a minute to execute.

Actually controlling the number of maps is subtle. The mapred.map.tasks parameter is just a hint to the InputFormat for the number of maps. The default InputFormat behavior is to split the total number of bytes into the right number of fragments. However, in the default case the DFS block size of the input files is treated as an upper bound for input splits. A lower bound on the split size can be set via mapred.min.split.size. Thus, if you expect 10TB of input data and have 128MB DFS blocks, you'll end up with 82k maps, unless your mapred.map.tasks is even larger. Ultimately the [WWW] InputFormat determines the number of maps.

The number of map tasks can also be increased manually using the JobConf's conf.setNumMapTasks(int num). This can be used to increase the number of map tasks, but will not set the number below that which Hadoop determines via splitting the input data.

Number of Reduces

The right number of reduces seems to be 0.95 or 1.75 * (nodes * mapred.tasktracker.tasks.maximum). At 0.95 all of the reduces can launch immediately and start transfering map outputs as the maps finish. At 1.75 the faster nodes will finish their first round of reduces and launch a second round of reduces doing a much better job of load balancing.

Currently the number of reduces is limited to roughly 1000 by the buffer size for the output files (io.buffer.size * 2 * numReduces << heapSize). This will be fixed at some point, but until it is it provides a pretty firm upper bound.

The number of reduces also controls the number of output files in the output directory, but usually that is not important because the next map/reduce step will split them into even smaller splits for the maps.

The number of reduce tasks can also be increased in the same way as the map tasks, via JobConf's conf.setNumReduceTasks(int num).

⾃⼰的理解：

mapper个数的设置：跟input file 有关系，也跟filesplits有关系，filesplits的上线为dfs.block.size， 下线可以通过mapred.min.split.size设置，最后还是由InputFormat决定。

较好的建议：

The right number of reduces seems to be 0.95 or 1.75 multiplied by (<no. of nodes> * mapred.tasktracker.reduce.tasks.maximum).increasing the number of reduces increases the framework overhead, but increases load balancing and lowers the cost of failures.

<property>

<name>mapred.tasktracker.reduce.tasks.maximum</name>

<value>2</value>

<description>The maximum number of reduce tasks that will be run

simultaneously by a task tracker.

</description>

</property>

单个node新加硬盘

- 1.修改需要新加硬盘的node的dfs.data.dir，⽤逗号分隔新、旧⽂件⽬录

- 2.重启dfs


同步hadoop 代码

hadoop-env.sh

# host:path where hadoop code should be rsync'd from. Unset by default.

# export HADOOP_MASTER=master:/home/$USER/src/hadoop

⽤命令合并HDFS⼩⽂件

hadoop fs -getmerge <src> <dest>

# 重启reduce job⽅法

Introduced recovery of jobs when JobTracker restarts. This facility is off by default.

Introduced config parameters "mapred.jobtracker.restart.recover", "mapred.jobtracker.job.history.block.size", and "mapred.jobtracker.job.history.buffer.size".

还未验证过。

IO写操作出现问题

0-1246359584298, infoPort=50075, ipcPort=50020):Got exception while serving blk_-5911099437886836280_1292 to /172.16.100.165:

java.net.SocketTimeoutException: 480000 millis timeout while waiting for channel to be ready for write. ch : java.nio.channels.SocketChannel[connected local=/

172.16.100.165:50010 remote=/172.16.100.165:50930]

at org.apache.hadoop.net.SocketIOWithTimeout.waitForIO(SocketIOWithTimeout.java:185)

at org.apache.hadoop.net.SocketOutputStream.waitForWritable(SocketOutputStream.java:159)

at org.apache.hadoop.net.SocketOutputStream.transferToFully(SocketOutputStream.java:198)

at org.apache.hadoop.hdfs.server.datanode.BlockSender.sendChunks(BlockSender.java:293)

at org.apache.hadoop.hdfs.server.datanode.BlockSender.sendBlock(BlockSender.java:387)

at org.apache.hadoop.hdfs.server.datanode.DataXceiver.readBlock(DataXceiver.java:179)

at org.apache.hadoop.hdfs.server.datanode.DataXceiver.run(DataXceiver.java:94)

at java.lang.Thread.run(Thread.java:619)

It seems there are many reasons that it can timeout, the example given in

HADOOP-3831 is a slow reading client.

解决办法：在hadoop-site.xml中设置dfs.datanode.socket.write.timeout=0试试；

My understanding is that this issue should be fixed in Hadoop 0.19.1 so that

we should leave the standard timeout. However until then this can help

resolve issues like the one you're seeing.

HDFS退服节点的⽅法

⽬前版本的dfsadmin的帮助信息是没写清楚的，已经file了⼀个bug了，正确的⽅法如下：

- 1. 将 dfs.hosts 置为当前的 slaves，⽂件名⽤完整路径，注意，列表中的节点主机名要⽤⼤名，即 uname -n 可以得到的那个。

- 2. 将 slaves 中要被退服的节点的全名列表放在另⼀个⽂件⾥，如 slaves.ex，使⽤ dfs.host.exclude 参数指向这个⽂件的完整路径

- 3. 运⾏命令 bin/hadoop dfsadmin -refreshNodes

- 4. web界⾯或 bin/hadoop dfsadmin -report 可以看到退服节点的状态是 Decomission in progress，直到需要复制的数据复制完成为⽌

- 5. 完成之后，从 slaves ⾥（指 dfs.hosts 指向的⽂件）去掉已经退服的节点


附带说⼀下 -refreshNodes 命令的另外三种⽤途：

- 2. 添加允许的节点到列表中（添加主机名到 dfs.hosts ⾥来）

- 3. 直接去掉节点，不做数据副本备份（在 dfs.hosts ⾥去掉主机名）

- 4. 退服的逆操作——停⽌ exclude ⾥⾯和 dfs.hosts ⾥⾯都有的，正在进⾏ decomission 的节点的 退服，也就是把 Decomission in progress 的节点重新变为 Normal （在 web 界⾯叫 in service)


hadoop 学习借鉴

- 1. 解决hadoop OutOfMemoryError问题：


<property>

<name>mapred.child.java.opts</name>

<value>-Xmx800M -server</value>

</property>

With the right JVM size in your hadoop-site.xml , you will have to copy this

to all mapred nodes and restart the cluster.

或者：hadoop jar jarfile [main class] -D mapred.child.java.opts=-Xmx800M

- 2. Hadoop java.io.IOException: Job failed! at org.apache.hadoop.mapred.JobClient.runJob(JobClient.java:1232) while indexing.


when i use nutch1.0,get this error:

Hadoop java.io.IOException: Job failed! at org.apache.hadoop.mapred.JobClient.runJob(JobClient.java:1232) while indexing.

这个也很好解决：

可以删除conf/log4j.properties，然后可以看到详细的错误报告

我这⼉出现的是out of memory

解决办法是在给运⾏主类org.apache.nutch.crawl.Crawl加上参数：-Xms64m -Xmx512m

你的或许不是这个问题，但是能看到详细的错误报告问题就好解决了

# distribute cache使⽤

类似⼀个全局变量，但是由于这个变量较⼤，所以不能设置在config⽂件中，转⽽使⽤distribute cache

具体使⽤⽅法：(详⻅《the definitive guide》,P240)

- 1. 在命令⾏调⽤时：调⽤-files，引⼊需要查询的⽂件(可以是local file, HDFS file(使⽤ hdfs://xxx?)), 或者 -archives (JAR,ZIP, tar等)

% hadoop jar job.jar MaxTemperatureByStationNameUsingDistributedCacheFile /

-files input/ncdc/metadata/stations-fixed-width.txt input/ncdc/all output

- 2. 程序中调⽤：


public void configure(JobConf conf) {

metadata = new NcdcStationMetadata();

try {

metadata.initialize(new File("stations-fixed-width.txt"));

} catch (IOException e) {

throw new RuntimeException(e);

}

}

另外⼀种间接的使⽤⽅法：在hadoop-0.19.0中好像没有

调⽤addCacheFile()或者addCacheArchive()添加⽂件，

使⽤getLocalCacheFiles() 或 getLocalCacheArchives() 获得⽂件

# hadoop的job显示web

There are web-based interfaces to both the JobTracker (MapReduce master) and NameNode (HDFS master) which display status pages about the state of the entire system. By default, these are located at [WWW] http://job.tracker.addr:50030/ and [WWW] http://name.node.addr:50070/.

# hadoop监控

OnlyXP(52388483) 131702

⽤nagios作告警，ganglia作监控图表即可

# status of 255 error

错误类型：

java.io.IOException: Task process exit with nonzero status of 255.

at org.apache.hadoop.mapred.TaskRunner.run(TaskRunner.java:424)

错误原因：

Set mapred.jobtracker.retirejob.interval and mapred.userlog.retain.hours to higher value. By default, their values are 24 hours. These might be the reason for failure, though I'm not sure

# split size

FileInputFormat input splits: (详⻅ 《the definitive guide》P190)

mapred.min.split.size: default=1, the smallest valide size in bytes for a file split.

mapred.max.split.size: default=Long.MAX_VALUE, the largest valid size.

dfs.block.size: default = 64M, 系统中设置为128M。

如果设置 minimum split size > block size, 会增加块的数量。(猜想从其他节点拿去数据的时候，会 合并block，导致block数量增多)

如果设置maximum split size < block size, 会进⼀步拆分block。

split size = max(minimumSize, min(maximumSize, blockSize));

其中 minimumSize < blockSize < maximumSize.

# sort by value

hadoop 不提供直接的sort by value⽅法，因为这样会降低mapreduce性能。

但可以⽤组合的办法来实现，具体实现⽅法⻅《the definitive guide》, P250

基本思想：

- 1. 组合key/value作为新的key；

- 2. 重载partitioner，根据old key来分割； conf.setPartitionerClass(FirstPartitioner.class);

- 3. ⾃定义keyComparator：先根据old key排序，再根据old value排序；

conf.setOutputKeyComparatorClass(KeyComparator.class);

- 4. 重载GroupComparator, 也根据old key 来组 合； conf.setOutputValueGroupingComparator(GroupComparator.class);


# small input files的处理

对于⼀系列的small files作为input file，会降低hadoop效率。

有3种⽅法可以将small file合并处理：

- 1. 将⼀系列的small files合并成⼀个sequneceFile，加快mapreduce速度。

详⻅WholeFileInputFormat及SmallFilesToSequenceFileConverter,《the definitive guide》, P194

- 2. 使⽤CombineFileInputFormat集成FileinputFormat，但是未实现过；

- 3. 使⽤hadoop archives(类似打包)，减少⼩⽂件在namenode中的metadata内存消耗。(这个⽅法 不⼀定可⾏，所以不建议使⽤)


⽅法：

将/my/files⽬录及其⼦⽬录归档成files.har，然后放在/my⽬录下

bin/hadoop archive -archiveName files.har /my/files /my

查看files in the archive:

bin/hadoop fs -lsr har://my/files.har

# skip bad records

JobConf conf = new JobConf(ProductMR.class);

conf.setJobName("ProductMR");

conf.setOutputKeyClass(Text.class);

conf.setOutputValueClass(Product.class);

conf.setMapperClass(Map.class);

conf.setReducerClass(Reduce.class);

conf.setMapOutputCompressorClass(DefaultCodec.class);

conf.setInputFormat(SequenceFileInputFormat.class);

conf.setOutputFormat(SequenceFileOutputFormat.class);

String objpath = "abc1";

SequenceFileInputFormat.addInputPath(conf, new Path(objpath));

SkipBadRecords.setMapperMaxSkipRecords(conf, Long.MAX_VALUE);

SkipBadRecords.setAttemptsToStartSkipping(conf, 0);

SkipBadRecords.setSkipOutputPath(conf, new Path("data/product/skip/"));

String output = "abc";

SequenceFileOutputFormat.setOutputPath(conf, new Path(output));

JobClient.runJob(conf);

For skipping failed tasks try : mapred.max.map.failures.percent

# restart 单个datanode

如果⼀个datanode 出现问题，解决之后需要重新加⼊cluster⽽不重启cluster，⽅法如下：

bin/hadoop-daemon.sh start datanode

bin/hadoop-daemon.sh start jobtracker

# reduce exceed 100%

"Reduce Task Progress shows > 100% when the total size of map outputs (for a

single reducer) is high "

造成原因：

在reduce的merge过程中，check progress有误差，导致status > 100%，在统计过程中就会出现 以下错误：java.lang.ArrayIndexOutOfBoundsException: 3

at org.apache.hadoop.mapred.StatusHttpServer$TaskGraphServlet.getReduceAvarageProgresses (StatusHttpServer.java:228)

at org.apache.hadoop.mapred.StatusHttpServer$TaskGraphServlet.doGet(StatusHttpServer.java:1 59)

at javax.servlet.http.HttpServlet.service(HttpServlet.java:689)

at javax.servlet.http.HttpServlet.service(HttpServlet.java:802)

at org.mortbay.jetty.servlet.ServletHolder.handle(ServletHolder.java:427)

at org.mortbay.jetty.servlet.WebApplicationHandler.dispatch(WebApplicationHandler.java:475)

at org.mortbay.jetty.servlet.ServletHandler.handle(ServletHandler.java:567)

at org.mortbay.http.HttpContext.handle(HttpContext.java:1565)

at org.mortbay.jetty.servlet.WebApplicationContext.handle(WebApplicationContext.java:635)

at org.mortbay.http.HttpContext.handle(HttpContext.java:1517)

at org.mortbay.http.HttpServer.service(HttpServer.java:954)

jira地址：

# counters

3中counters：

- 1. built-in counters: Map input bytes, Map output records...

- 2. enum counters 调⽤⽅式：


enum Temperature {

MISSING,

MALFORMED

}

reporter.incrCounter(Temperature.MISSING, 1)

结果显示：

09/04/20 06:33:36 INFO mapred.JobClient: Air Temperature Recor

09/04/20 06:33:36 INFO mapred.JobClient: Malformed=3

09/04/20 06:33:36 INFO mapred.JobClient: Missing=66136856

- 3. dynamic countes: 调⽤⽅式： reporter.incrCounter("TemperatureQuality", parser.getQuality(),1);


结果显示：

09/04/20 06:33:36 INFO mapred.JobClient: TemperatureQuality

09/04/20 06:33:36 INFO mapred.JobClient: 2=1246032

09/04/20 06:33:36 INFO mapred.JobClient: 1=973422173

09/04/20 06:33:36 INFO mapred.JobClient: 0=1

# 7: Namenode in safe mode

解决⽅法

bin/hadoop dfsadmin -safemode leave

# 8:java.net.NoRouteToHostException: No route to host j解决⽅法： sudo /etc/init.d/iptables stop

- 9：更改namenode后，在hive中运⾏select 依旧指向之前的namenode地址 这是因为：When youcreate a table, hive actually stores the location of the table (e.g.


hdfs://ip:port/user/root/...) in the SDS and DBS tables in the metastore . So when I bring up a new cluster the master has a new IP, but hive's metastore is still pointing to the locations within the old

cluster. I could modify the metastore to update with the new IP everytime I bring up a cluster. But the easier and simpler solution was to just use an elastic IP for the master

所以要将metastore中的之前出现的namenode地址全部更换为现有的namenode地址

- 10：Your DataNode is started and you can create directories with bin/hadoop dfs -mkdir, but you get an error message when you try to put files into the HDFS (e.g., when you run a command like bin/hadoop dfs -put). 解决⽅法： Go to the HDFS info web page (open your web browser and go to http://namenode:dfs_info_port where namenode is the hostname of your NameNode and dfs_info_port is the port you chose dfs.info.port; if followed the QuickStart on your personal computer then this URL will be http://localhost:50070). Once at that page click on the number where it tells you how many DataNodes you have to look at a list of the DataNodes in your cluster. If it says you have used 100% of your space, then you need to free up room on local disk(s) of the DataNode(s).

If you are on Windows then this number will not be accurate (there is some kind of bug either in Cygwin's df.exe or in Windows). Just free up some more space and you should be okay. On one Windows machine we tried the disk had 1GB free but Hadoop reported that it was 100% full. Then we freed up another 1GB and then it said that the disk was 99.15% full and started writing data into the HDFS again. We encountered this bug on Windows XP SP2.

- 11：Your DataNodes won't start, and you see something like this in logs/*datanode*:


Incompatible namespaceIDs in /tmp/hadoop-ross/dfs/data

原因：

Your Hadoop namespaceID became corrupted. Unfortunately the easiest thing to do reformat the HDFS.

解决⽅法：

You need to do something like this:

bin/stop-all.sh

rm -Rf /tmp/hadoop-your-username/*

bin/hadoop namenode -format

# 12：You can run Hadoop jobs written in Java (like the grep example), but your HadoopStreaming jobs (such as the Python example that fetches web page titles) won't work. 原因：

You might have given only a relative path to the mapper and reducer programs. The tutorial originally just specified relative paths, but absolute paths are required if you are running in a real cluster.

解决⽅法：

Use absolute paths like this from the tutorial:

bin/hadoop jar contrib/hadoop-0.15.2-streaming.jar /

- -mapper $HOME/proj/hadoop/multifetch.py /

- -reducer $HOME/proj/hadoop/reducer.py /

- -input urls/* /

- -output titles


# 13： 2009-01-08 10:02:40,709 ERROR metadata.Hive (Hive.java:getPartitions(499)) javax.jdo.JDODataStoreException: Required table missing : ""PARTITIONS"" in Catalog "" Schema "". JPOX requires this table to perform its persistence operations. Either your MetaData is incorrect, or you need to enable "org.jpox.autoCreateTables" 原因：就是因为在 hive-default.xml ⾥把 org.jpox.fixedDatastore 设置成 true 了

starting namenode, logging to /home/hadoop/HadoopInstall/hadoop/bin/../logs/hadoop-hadoopnamenode-hadoop.out

localhost: starting datanode, logging to /home/hadoop/HadoopInstall/hadoop/bin/../logs/hadoophadoop-datanode-hadoop.out

localhost: starting secondarynamenode, logging to /home/hadoop/HadoopInstall/hadoop/bin/../logs/hadoop-hadoop-secondarynamenodehadoop.out

localhost: Exception in thread "main" java.lang.NullPointerException

localhost: at org.apache.hadoop.net.NetUtils.createSocketAddr(NetUtils.java:130)

localhost: at org.apache.hadoop.dfs.NameNode.getAddress(NameNode.java:116)

localhost: at org.apache.hadoop.dfs.NameNode.getAddress(NameNode.java:120)

localhost: at org.apache.hadoop.dfs.SecondaryNameNode.initialize(SecondaryNameNode.java:124)

localhost: at org.apache.hadoop.dfs.SecondaryNameNode.<init> (SecondaryNameNode.java:108)

localhost: at org.apache.hadoop.dfs.SecondaryNameNode.main(SecondaryNameNode.java:460)

- 14：09/08/31 18:25:45 INFO hdfs.DFSClient: Exception in createBlockOutputStream java.io.IOException:Bad connect ack with firstBadLink 192.168.1.11:50010


- > 09/08/31 18:25:45 INFO hdfs.DFSClient: Abandoning block blk_-8575812198227241296_1001


- > 09/08/31 18:25:51 INFO hdfs.DFSClient: Exception in createBlockOutputStream java.io.IOException:


Bad connect ack with firstBadLink 192.168.1.16:50010

- > 09/08/31 18:25:51 INFO hdfs.DFSClient: Abandoning block blk_-2932256218448902464_1001

- > 09/08/31 18:25:57 INFO hdfs.DFSClient: Exception in createBlockOutputStream java.io.IOException: Bad connect ack with firstBadLink 192.168.1.11:50010

- > 09/08/31 18:25:57 INFO hdfs.DFSClient: Abandoning block blk_-1014449966480421244_1001

- > 09/08/31 18:26:03 INFO hdfs.DFSClient: Exception in createBlockOutputStream java.io.IOException: Bad connect ack with firstBadLink 192.168.1.16:50010


- > 09/08/31 18:26:03 INFO hdfs.DFSClient: Abandoning block blk_7193173823538206978_1001


- > 09/08/31 18:26:09 WARN hdfs.DFSClient: DataStreamer Exception: java.io.IOException: Unable to create new block.


> at org.apache.hadoop.hdfs.DFSClient$DFSOutputStream.nextBlockOutputStream(DFSClient.java: 2731)

> at org.apache.hadoop.hdfs.DFSClient$DFSOutputStream.access$2000(DFSClient.java:1996)

> at org.apache.hadoop.hdfs.DFSClient$DFSOutputStream$DataStreamer.run(DFSClient.java:2182)

>

- > 09/08/31 18:26:09 WARN hdfs.DFSClient: Error Recovery for block blk_7193173823538206978_1001 bad datanode[2] nodes == null


- > 09/08/31 18:26:09 WARN hdfs.DFSClient: Could not get block locations. Source file "/user/umer/8GB_input"


- Aborting...

> put: Bad connect ack with firstBadLink 192.168.1.16:50010

解决⽅法：

I have resolved the issue:

What i did:

- 1) '/etc/init.d/iptables stop' -->stopped firewall

- 2) SELINUX=disabled in '/etc/selinux/config' file.-->disabled selinux


I worked for me after these two changes

# 解决jline.ConsoleReader.readLine在Windows上不⽣效问题⽅法

在 CliDriver.java的main()函数中，有⼀条语句reader.readLine，⽤来读取标准输⼊，但在 Windows平台上该语句总是 返回null，这个reader是⼀个实例jline.ConsoleReader实例，给 Windows Eclipse调试带来不便。

我们可以通过使⽤java.util.Scanner.Scanner来替代它，将原来的

while ((line=reader.readLine(curPrompt+"> ")) != null)

复制代码

替换为：

Scanner sc = new Scanner(System.in);

while ((line=sc.nextLine()) != null)

复制代码

重新编译发布，即可正常从标准输⼊读取输⼊的SQL语句了。

# Windows eclispe调试hive报does not have a scheme错误可能原因

- 1、Hive配置⽂件中的“hive.metastore.local”配置项值为false，需要将它修改为true，因为是单机 版

- 2、没有设置HIVE_HOME环境变量，或设置错误

- 3、 “does not have a scheme”很可能是因为找不到“hive-default.xml”。使⽤Eclipse调试Hive时， 遇到找不到hive- default.xml的解决⽅法：http://bbs.hadoopor.com/thread-292-1-1.html


- 1、中⽂问题


从url中解析出中⽂,但hadoop中打印出来仍是乱码?我们曾经以为hadoop是不⽀持中⽂的，后来 经过查看源代码，发现hadoop仅仅是不⽀持以gbk格式输出中⽂⽽⼰。

这是TextOutputFormat.class中的代码，hadoop默认的输出都是继承⾃FileOutputFormat来 的，FileOutputFormat的两个⼦类⼀个是基于⼆进制流的输出，⼀个就是基于⽂本的输出 TextOutputFormat。

public class TextOutputFormat<K, V> extends FileOutputFormat<K, V> {

protected static class LineRecordWriter<K, V>

implements RecordWriter<K, V> {

private static final String utf8 = “UTF-8″;//这⾥被写死成了utf-8

private static final byte[] newline;

static {

try {

newline = “/n”.getBytes(utf8);

} catch (UnsupportedEncodingException uee) {

throw new IllegalArgumentException(”can’t find ” + utf8 + ” encoding”);

}

}

…

public LineRecordWriter(DataOutputStream out, String keyValueSeparator) {

this.out = out;

try {

this.keyValueSeparator = keyValueSeparator.getBytes(utf8);

} catch (UnsupportedEncodingException uee) {

throw new IllegalArgumentException(”can’t find ” + utf8 + ” encoding”);

}

}

…

private void writeObject(Object o) throws IOException {

if (o instanceof Text) {

Text to = (Text) o;

out.write(to.getBytes(), 0, to.getLength());//这⾥也需要修改

} else {

out.write(o.toString().getBytes(utf8));

}

}

…

}

可以看出hadoop默认的输出写死为utf-8，因此如果decode中⽂正确，那么将Linux客户端的 character设为utf-8是可以看到中⽂的。因为hadoop⽤utf-8的格式输出了中⽂。

因为⼤多数数据库是⽤gbk来定义字段的，如果想让hadoop⽤gbk格式输出中⽂以兼容数据库怎 么办？

我们可以定义⼀个新的类：

public class GbkOutputFormat<K, V> extends FileOutputFormat<K, V> {

protected static class LineRecordWriter<K, V>

implements RecordWriter<K, V> {

//写成gbk即可

private static final String gbk = “gbk”;

private static final byte[] newline;

static {

try {

newline = “/n”.getBytes(gbk);

} catch (UnsupportedEncodingException uee) {

throw new IllegalArgumentException(”can’t find ” + gbk + ” encoding”);

}

}

…

public LineRecordWriter(DataOutputStream out, String keyValueSeparator) {

this.out = out;

try {

this.keyValueSeparator = keyValueSeparator.getBytes(gbk);

} catch (UnsupportedEncodingException uee) {

throw new IllegalArgumentException(”can’t find ” + gbk + ” encoding”);

}

}

…

private void writeObject(Object o) throws IOException {

if (o instanceof Text) {

// Text to = (Text) o;

// out.write(to.getBytes(), 0, to.getLength());

// } else {

out.write(o.toString().getBytes(gbk));

}

}

…

}

然后在mapreduce代码中加⼊conf1.setOutputFormat(GbkOutputFormat.class)

即可以gbk格式输出中⽂。

- 2、某次正常运⾏mapreduce实例时,抛出错误


java.io.IOException: All datanodes xxx.xxx.xxx.xxx:xxx are bad. Aborting…

at org.apache.hadoop.dfs.DFSClient$DFSOutputStream.processDatanodeError(DFSClient.java:21 58)

at org.apache.hadoop.dfs.DFSClient$DFSOutputStream.access$1400(DFSClient.java:1735)

at org.apache.hadoop.dfs.DFSClient$DFSOutputStream$DataStreamer.run(DFSClient.java:1889)

java.io.IOException: Could not get block locations. Aborting…

at org.apache.hadoop.dfs.DFSClient$DFSOutputStream.processDatanodeError(DFSClient.java:21 43)

at org.apache.hadoop.dfs.DFSClient$DFSOutputStream.access$1400(DFSClient.java:1735)

at org.apache.hadoop.dfs.DFSClient$DFSOutputStream$DataStreamer.run(DFSClient.java:1889)

经查明，问题原因是linux机器打开了过多的⽂件导致。⽤命令ulimit -n可以发现linux默认的⽂件打 开数⽬为1024，修改/ect/security/limit.conf，增加hadoop soft 65535

再重新运⾏程序（最好所有的datanode都修改），问题解决

- 3、运⾏⼀段时间后hadoop不能stop-all.sh的问题，显示报错


no tasktracker to stop ，no datanode to stop

问 题的原因是hadoop在stop的时候依据的是datanode上的mapred和dfs进程号。⽽默认的进程号 保存在/tmp下，linux默认会每 隔⼀段时间（⼀般是⼀个⽉或者7天左右）去删除这个⽬录下的⽂ 件。因此删掉hadoop-hadoop-jobtracker.pid和hadoop- hadoop-namenode.pid两个⽂件后， namenode⾃然就找不到datanode上的这两个进程了。

在配置⽂件中的export HADOOP_PID_DIR可以解决这个问题

问题：

Incompatible namespaceIDs in /usr/local/hadoop/dfs/data: namenode namespaceID = 405233244966; datanode namespaceID = 33333244

原因：

在 每次执⾏hadoop namenode -format时，都会为NameNode⽣成namespaceID,，但是在 hadoop.tmp.dir⽬录下的DataNode还是保留上次的 namespaceID，因为namespaceID的不⼀致， ⽽导致DataNode⽆法启动，所以只要在每次执⾏hadoop namenode -format之前，先删除 hadoop.tmp.dir⽬录就可以启动成功。请注意是删除hadoop.tmp.dir对应的本地⽬录，⽽不是 HDFS ⽬录。

# Problem: Storage directory not exist

2010-02-09 21:37:53,203 INFO org.apache.hadoop.hdfs.server.common.Storage: Storage directory D:/hadoop/run/dfs_name_dir does not exist.

2010-02-09 21:37:53,203 ERROR org.apache.hadoop.hdfs.server.namenode.FSNamesystem: FSNamesystem initialization failed.

org.apache.hadoop.hdfs.server.common.InconsistentFSStateException: Directory D:/hadoop/run/dfs_name_dir is in an inconsistent state: storage directory does not exist or is not accessible.

solution: 是因为存储⽬录D:/hadoop/run/dfs_name_dir不存在，所以只需要⼿动创建好这个⽬录即 可。

# Problem: NameNode is not formatted

solution: 是因为HDFS还没有格式化，只需要运⾏hadoop namenode -format⼀下，然后再启动即 可

bin/hadoop jps后报如下异常：

Exception in thread "main" java.lang.NullPointerException

at sun.jvmstat.perfdata.monitor.protocol.local.LocalVmManager.activeVms(LocalVmManager.java:1 27)

at sun.jvmstat.perfdata.monitor.protocol.local.MonitoredHostProvider.activeVms(MonitoredHostPro vider.java:133)

at sun.tools.jps.Jps.main(Jps.java:45)

原因为：

系统根⽬录/tmp⽂件夹被删除了。重新建⽴/tmp⽂件夹即可。

bin/hive中出现 unable to create log directory /tmp/...也可能是这个原因

