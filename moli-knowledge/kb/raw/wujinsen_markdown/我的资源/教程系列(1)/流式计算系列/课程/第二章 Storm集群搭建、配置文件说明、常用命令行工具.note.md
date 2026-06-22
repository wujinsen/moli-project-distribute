安装前的准备⼯作（zk集群已经部署完毕）

关闭防⽕墙

chkconfig iptables of & setenforce 0

创建⽤户

groupad realtime & userad realtime & usermod -a -G realtime realtime

创建⼯作⽬录并赋权

mkdir /export mkdir /export/servers chmod 75 -R /export

切换到realtime⽤户下

su realtime

Storm集群安装

- 1、下载storm安装包

wget

- 2、解压storm安装包

tar -zxvf apache-storm-0.9.5.tar.gz -C /export/servers/

cd /export/servers/

ln -s apache-storm-0.9.5 storm

- 3、修改配置⽂件


htp:/124.202.164.6/files/139 06794ECA/apache.fayea.com/storm/apache-sto rm-0.9.5/apache-storm-0.9.5.tar.gz

mv /export/servers/storm/conf/storm.yaml /export/servers/storm/conf/storm.yaml.bak

vi /export/servers/storm/conf/storm.yaml

输⼊以下内容：

![image 1](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile1.png>)

- 4、将配置好的⽂件storm安装⽂件拷⻉到其它机器上 scp -r /export/servers/apache-storm-0.9.5 storm02:/export/servers

然后分别在各机器上创建软连接

cd /export/servers/

ln -s apache-storm-0.9.5 storm

- 5、启动各组件

cd /export/servers/storm/bin/

nohup ./storm nimbus &

cd /export/servers/storm/bin/

nohup ./storm ui &

cd /export/servers/storm/bin/

nohup ./storm supervisor &

- 6、查看stormui


在nimbus.host所属的机器上启动 nimbus服务

在nimbus.host所属的机器上启动ui服务

在其它个点击上启动supervisor服务

访问nimbus.host:/8080，即可看到storm的ui界⾯。

![image 2](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile2.png>)

7、任务管理

有许多简单且有⽤的命令可以⽤来管理拓扑，它们可以提交、杀死、禁⽤、再平衡拓扑。

提交任务命令格式：storm jar 【jar路径】 【拓扑包名.拓扑类名】 【拓扑名称】

storm jar /export/servers/storm/examples/storm-starter/storm-starter-topologies0.9.6.jar storm.starter.WordCountTopology wordcount

杀死任务命令格式：storm kil 【拓扑名称】 -w 10（执⾏kil命令时可以通过-w [等待秒数]指定拓 扑停⽤以后的等待时间）

storm kil topology-name -w 10

停⽤任务命令格式：stormdeactivte 【拓扑名称】

storm deactivte topology-name 我们能够挂起或停⽤运⾏中的拓扑。当停⽤拓扑时，所有已分发的元组都会得到处理，但是spouts 的nextTuple⽅法不会被调⽤。 销毁⼀个拓扑，可以使⽤kil命令。它会以⼀种安全的⽅式销毁⼀个拓扑，⾸先停⽤拓扑，在等待 拓扑消息的时间段内允许拓扑完成当前的数据流。

启⽤任务命令格式：storm activate【拓扑名称】

storm activate topology-name

重新部署任务命令格式：storm rebalance 【拓扑名称】

storm rebalance topology-name 再平衡使你重分配集群任务。这是个很强⼤的命令。⽐如，你向⼀个运⾏中的集群增加了节点。

再平衡命令将会停⽤拓扑，然后在相应超时时间之后重分配⼯⼈，并重启拓扑

Storm集群各组件熟悉

- 1、部署成功之后，启动storm集群。
- 2、查看nimbus的⽇志信息（在nimbus-strom01窗⼝） cd /export/servers/storm/logs tail -10f/export/servers/storm/logs/nimbus.log

- 3、查看ui运⾏⽇志信息（在nimbus-storm01窗⼝）

在⻚⾯访问时，可以看到实时的RPC框架通信

- 4、查看supervisor运⾏⽇志信息（在supervisor-storm02/ …）
- 5、产看supervisor上workerd运⾏⽇志信息（在supervisor-storm02/ …） cd /export/servers/storm/logs tail -10f /export/servers/storm/logs/worker-6702.log


![image 3](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile3.png>)

![image 4](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile4.png>)

cd /export/servers/storm/logs

tail -10f /export/servers/storm/logs/ui.log

![image 5](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile5.png>)

cd /export/servers/storm/logs

tail -10f /export/servers/storm/logs/supervisor.log

![image 6](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile6.png>)

![image 7](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile7.png>)

![image 8](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile8.png>)

该worker正在运⾏wordcount程序。

Storm源码下载及⽬录分析

- 1、从storm官⽅⽹站上寻找storm源码地址

htp:/storm.apache.org/downloads.html

![image 9](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile9.png>)

- 2、点击Apache/storm⽂字标签，进⼊github

- 3、在⽹⻚右侧，拷⻉storm源码地址


htps:/github.com/apache/storm

![image 10](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile10.png>)

- 4、使⽤Subversion客户端下载

![image 11](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile11.png>)

- 5、⽬录分析


htps:/github.com/apache/storm/tags/v0.9.5

![image 12](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile12.png>)

扩展包中的三个项⽬，使storm能与hbase、hdfs、kafka交互

![image 13](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile13.png>)

单词计数

- 1、功能说明： 设计⼀个topology，来实现对⼀个句⼦⾥⾯的单词出现的频率进⾏统计。 整个topology分为三个部分：
- 2、topology的驱动启动类

- 3、RandomSentenceSpout的实现及⽣命周期


RandomSentenceSpout：数据源，在已知的英⽂句⼦中，随机发送⼀条句⼦出去。

SplitSentenceBolt：负责将单⾏⽂本记录（句⼦）切分成单词

WordCountBolt：负责对单词的频率进⾏累加

![image 14](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile14.png>)

![image 15](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile15.png>)

- 4、SplitSentenceBolt的实现及⽣命周期

- 5、WordCountBolt的实现及⽣命周期

Storm⾥⾯有7种类型的stream grouping

![image 16](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile16.png>)

![image 17](<第二章 Storm集群搭建、配置文件说明、常用命令行工具.note_images/imageFile17.png>)

- 6、Stream Grouping


Shufle Grouping: 随机分组， 随机派发stream⾥⾯的tuple，保证每个bolt接收到的tuple数⽬ ⼤致相同。 Fields Grouping：按字段分组，⽐如按userid来分组，具有同样userid的tuple会被分到相同的 Bolts⾥的⼀个task，⽽不同的userid则会被分配到不同的bolts⾥的task。 Al Grouping：⼴播发送，对于每⼀个tuple，所有的bolts都会收到。 Global Grouping：全局分组， 这个tuple被分配到storm中的⼀个bolt的其中⼀个task。再具体 ⼀点就是分配给id值最低的那个task。 Non Grouping：不分组，这stream grouping个分组的意思是说stream不关⼼到底谁会收到它 的tuple。⽬前这种分组和Shufle grouping是⼀样的效果， 有⼀点不同的是storm会把这个bolt 放到这个bolt的订阅者同⼀个线程⾥⾯去执⾏。 Direct Grouping： 直接分组， 这是⼀种⽐较特别的分组⽅法，⽤这种分组意味着消息的发送 者指定由消息接收者的哪个task处理这个消息。只有被声明为Direct Stream的消息流可以声明 这种分组⽅法。⽽且这种消息tuple必须使⽤emitDirect⽅法来发射。消息处理者可以通过 TopologyContext来获取处理它的消息的task的id （OutputColector.emit⽅法也会返回task的 id）。 Local or shufle grouping：如果⽬标bolt有⼀个或者多个task在同⼀个⼯作进程中，tuple将会 被随机发⽣给这些tasks。否则，和普通的Shufle Grouping⾏为⼀致。

