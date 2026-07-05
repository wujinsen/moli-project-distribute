---
title: Hadoop Ambari 安装.note（原文插图 annex）
slug: annex-Hadoop-Ambari-安装
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/a安装文档/Hadoop Ambari 安装.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

⼀、准备⼯作： 基本⼯具 yum

rpm

scp

curl

wget

pdsh

前⼏个⼀般系统都⾃带了，pdsh需要⾃⼰装

[plain]

view plaincopy

1.

yum instal pdsh

2、配置hosts vim /etc/hosts [plain]

view plaincopy

- 1.
- 2.
- 3.
- 4.


- 10..*.120 master master.hadop.test

- 10..*.121dn1 dn1.hadop.test


10..*.12 dn2 dn2.hadop.test 10..*.123 dn3 dn3.hadop.test

3、配置 sh免登录 选定master.hadop.test作为ambari server，需要配置该节点到其它节点的 sh免登录 [plain]

view plaincopy

- 1.
- 2.


cd ~

sh-keygen

⼀直按回⻋，会⽣成默认的公钥和私钥 [sql]

view plaincopy

- 1.
- 2.


.sh/id_rsa

.sh/id_rsa.pub

执⾏ [plain]

view plaincopy

- 1.
- 2.


chmod 70 ~/.sh

chmod 60 ~/.sh/authorized_keys

配置本地的免登录 [plain]

view plaincopy

1.

cat ~/.sh/id_rsa.pub > ~/.sh/authorized_keys

配置其它节点的免登陆 [plain]

view plaincopy

- 1.
- 2.
- 3.


- scp ~/.sh/authorized_keys dn1:/rot/.sh/

- scp ~/.sh/authorized_keys dn2:/rot/.sh/

- scp ~/.sh/authorized_keys dn3:/rot/.sh/


将私钥从master上下载下来，配置ambari agent时会⽤到 [plain]

view plaincopy

1.

.sh/id_rsa

同步集群的时间（ntp） 其它 [plain]

view plaincopy

- 1.
- 2.
- 3.
- 4.


setenforce 0

chkconfig iptables of

/etc/init.d/iptables stop

umask 02

Disable PackageKit [plain]

view plaincopy

1.

vim /etc/yum/pluginconf.d/refresh-packagekit.conf

将内容改为enabled=0 ⼆、安装Ambari Server

添加yum资源库 [plain]

view plaincopy

- 1.
- 2.
- 3.


wget htp:/public-repo-1.hortonworks.com/ambari/centos6/1.x/updates/1.2.4.9/ambari.rep o

cp ambari.repo /etc/yum.repos.d

yum instal epel-release

由于公司安全机制，这个地⽅可能不会安装成功 [plain]

view plaincopy

1.

yum repolist

看下有没有

[plain]

view plaincopy

- 1.
- 2.
- 3.
- 4.


repo id repo name

AMBARI-1.x | Ambari 1.x

HDP-UTILS-1.1.0.15| Hortonworks Data Platform Utils Version - HDP-UT

epel | Extra Packages for Enterprise Linux 6 - x86_64

如果有的话，就可以开始安装了 [plain]

view plaincopy

1.

yum instal ambari-server

实验证明，⽹速巨慢，耐⼼。。。

等待安装完成后

[plain]

view plaincopy ambari-server setup

1.

会提示安装jdk，⽹速好的可以确定，否则可以下载jdk-6u31-linux-x64.bin，放到/var/lib/ambariserver/resources/下⾯

接着会提示配置⽤的数据库，可以选择Oracle或postgresql，选择n会按默认配置

数据库类型：postgresql

数据库：ambari

⽤户名：ambari

密码：bigdata

如果提示Oracle JDK license，yes

等待安装完成

启动Ambari Server

[plain]

view plaincopy

1.

ambari-server start

htp:/master:8080

然后访问：

不出意外的话就安装完成了

⽤户名/密码:admin/admin

三、配置Ambari 给集群起个名字 选择HDP的版本 配置Agent： 不⽀持ip，输⼊hosts 选择ssh私钥，选择第⼀步⽣成的id_rsa

等待配置完成，然后选择服务，等待安装完成

ambari 1.2.4 下载地址：

htp:/ w.apache.org/dist/incubator/ambari/ambari-1.2.4/ambari-1.2.4

-incubating.tar.gz

htp:/incubator.apache.org/ambari/1.2.4/instaling-hadop-using-ambari/content/in dex.html

官⽅⼿册：

安装过程 ⼀、准备

- 1、安装包
- 2、集群中ambari-server（管理节点）到客户端配置⽆密码登录。
- 3、集群同步时间
- 4、SELinux，iptables都处于关闭状态
- 5、安装的服务器通过连接到⽹络
- 6、本⽂件安装是在redhat enterprise 6.0环境下，最好将yum替换成免费的，以⽅便安装


⼆、安装

- 1、下载repo

根据对应的系统下载相应的repo⽂件，并按要求拷⻉到/etc相应的⽬录下

- 2、安装epel仓库 yum instal epel-release # 查看仓库列表，应该有HDP，EPEL yum repolist
- 3、通过yum安装amabari bits，这同时也会安装PostgreSQL yum instal ambari-server 这个步骤要等⼀会，它需要上⽹下载，约39M的包
- 4、运⾏ambari-server setup，安装ambari-server，它会⾃动安装配置PostgreSQL，同时要求输 ⼊⽤户名和密码，如果按n，它⽤默认的⽤户名/密码值：ambari-server/bigdata。接着就开始下载 安装JDK。安装完成后，ambari-server就可以启动了。


htp:/incubator.apache.org/ambari/1.2.4/instaling-hadop-using-ambari/content/ambari-c hap9-2.html

在这步之前安装集群的时候始终不成功，最后检查发现是 SH免密码登录时设置有问题，要在命令 ⾏下测试IP和主机名登录是否都没有问题。

三、Ambari安装集群：

增加新的机器

如果选择使⽤私钥的⽅式安装不成功的话，请多试⼏次。 如果还是不成功，使⽤⼿动⽅式，但前提是在⽬标机器上要安装ambari-agent并启动。 推荐使⽤这种⽅法，成功率⾮常⾼。

## 如果安装失败了，确认免登录没有问题的前提下，多retry⼏次。 如果安装的时间太久，可以删除安装重来。

做⼤数据相关的后端开发⼯作⼀年多来,随着Hadop社区的不断发展,也在不断尝试新的东⻄,本⽂ 着重来讲解下 ,这个新的Apache的项⽬,旨在让⼤家能够⽅便快速的配置和部署Hadop⽣态 圈相关的组件的环境,并提供维护和监控的功能. 作为新⼿,我讲讲我⾃⼰的学习经历,刚刚开始学习的时候,当然最简单的 Gogle 下Hadop ,然后下 载相关的包,在⾃⼰的虚拟机(CentOS 6.3) 上安装⼀个单机的Hadop版本⽤来做测试,写⼏个测试 类,然后做下CRUD测试之类的,跑跑Map/Reduce的测试,当然这个时候对于Hadop还不是很了解, 不断的看别⼈的⽂章,了解下整体的架构,⾃⼰所做的就是修改conf下的⼏个配置⽂件,让Hadop能 够正常的跑起来,这个时候⼏种在修改配置上,这个阶段之后,⼜⽤到了HBase,这个Hadop⽣态圈的 另外⼀个产品,当然还是修改配置,然后 start-al.sh , start-hbase.sh 把服务起起来,然后就是修改⾃ ⼰的程序,做测试,随着⽤Hbase 学了下 Zokeper 和Hive等, 接着过了这个操作阶段了之后,开始研 究Hadop2.0看了 的相关⽂章,还有CSDN上很多⼤⽜的⽂章了之后, 算是对Hadop的⽣ 态圈整体有⼀些了解,介于⾃⼰在公司所承担的开发所涉及到相关的技术仅仅就这些.但是作为⼀个 爱好探索的⼈,是否想多了解下呢,它的性能怎么样? 它是具体如何运作的? 看⼤公司的那些 PT,⼈ 家(淘宝等⼤公司)动不动就是⼏⼗个,⼏百个,乃⾄⼏千个节点,⼈家是如何管理的,性能是怎么样的? 看着 PT⾥⾯的那些性能测试的曲线,你是否也能够详细的了解,并且对⾃⼰的项⽬进⾏性能调优呢? 我貌似找到答案了,那就是 Ambari , 由 开发的⼀个Hadop相关的项⽬,具体可以上官 ⽅去了解.

Ambari

董的博客

HortonWorks

# 了解Hadop⽣态圈

现在我们经常看到的⼀些关键字有: HDFS,MapReduce,HBase,Hive,ZoKeper,Pig,Sqop,Oozie,Ganglia,Nagios,CDH3,CDH4,Flume, Scribe,Fluented,HtpFS等等,其实应该还有更多,Hadop⽣态圈现在发展算是相当繁荣了,⽽在这些 繁荣的背后⼜是谁在推动的呢? 读过Hadop历史的朋友可能知道,Hadop最早是始于Yaho,但是 现在主要是由 HortonWorks

![image 1](assets/imageFile1.png)

和 Cloudera

![image 2](assets/imageFile2.png)

这2家公司在维护者,⼤部分的comiter 都属于这2家公司,所以现在市⾯上看到的主要有2个版

本,CDH系列,和社区版, 我最早⽤的是社区版本,后来换到CDH3,现在⼜换回社区版,因为有Ambari. 当然,⽤什么和不⽤什么,只要⾃⼰的技术到家,还是都能修改的跑的正常的.这⾥就不多说了. 讲了这 么多废话了,开始讲 Ambari安装吧.

# 开始部署

⾸先了解下Ambari, 项⽬地址在: 安装⽂档在:

htp:/incubator.apache.org/ambari/

htp:/incubator.apache.org/ambari/1.2.2/instaling-hadop-using-ambari/content/i ndex.html

HortonWorks的⼈写的⼀篇介绍安装的⽂章我翻译了下:

htp:/ w.cnblogs.com/scotoma/archiv e/2013/05/18/3085040.html

安装的时候请⼤家先看下安装⽂档吧,安装⽂档必须认真看,结合⾃⼰ 当前所使⽤的系统版本,配置不同的源,⽽且安装过程中需要的时间相对⽐较⻓,所以需要认真的做好 安装⽂档的每个步骤. 这⾥我就说我遇到的⼀些问题. 以下说说我⾃⼰的安装过程. 机器准备: 我的测试环境采⽤ 9 台 HP 的烂机器,分别是 cloud10 - cloud108 , cloud108做为管理节点. Ambari安装的环境路径: 各台机器的安装⽬录: /usr/lib/hadop /usr/lib/hbase /usr/lib/zokeper /usr/lib/hcatalog /usr/lib/hive Log路径, 这⾥需要看出错信息都可以在⽬录下找到相关的⽇志 /var/log/hadop /var/log/hbase

配置⽂件的路径 /etc/hadop /etc/hbase /etc/hive HDFS的存储路径 /hadop/hdfs

安装过程需要注意的点:

- 1, 安装的时候,需要做好每台机器的 sh免密码登陆,这个之前的⽂章 中提到了,做好之后,从 管理节点到各个集群节点之间,都

能使⽤这个登陆.

- 2, 如果你的机器之前安装过 Hadop的相关服务,特别是Hbase ⾥⾯配置了 HBASE_HOME 的环境 变量,需要 unset掉, 这个环境变量会影响,因为我之前把这些路径放到 /etc/profile ⾥⾯导致影响了 HBase,因为Ambari安装的路径和你之前安装的可能不⼀样.


htp:/ w.cnblogs.com/scot oma/archive/2012/09/18/268902.html

- 3,在服务选择⻚⾯的时候, NameNode 和 SNameNode 需要布置在⼀起, 我之前尝试做 HA ⽽把他 们分开,但是SNameNode⼀直起不来,导致整个启动失败,接下来时间需要花在HA上.
- 4. JobTrakcer 不和Namenode在⼀起也会导致 启动不起来.
- 5. Datanode的节点 不能少于 Block replication 中数, 基本都是需要 >= 3.


![image 3](assets/imageFile3.png)

![image 4](assets/imageFile4.png)

- 6. Confirm Hosts 的时候,需要注意⾥⾯的 Warning 信息,把相关的Warning都处理掉,有⼀些 Warning会导致安装出错.
- 7. 记住安装中所新建的⽤户,接下来需要⽤到这些⽤户.


![image 5](assets/imageFile5.png)

- 8. Hive和HBase Master 部署在同⼀个节点,这⾥当然你也可以分开. 设置好后就开始安装了.
- 9.如果安装失败的情况下,如何重新安装.


![image 6](assets/imageFile6.png)

⾸先,先删除掉系统已经安装的⽂件相关的⽬录, sh cmd "rm -rf /usr/lib/hadop & rm -rf /usr/lib/hbase & rm -rf /usr/lib/zokeper" sh cmd "rm -rf /etc/hadop & rm -rf /etc/hbase & rm -rf /hadop & rm -rf /var/log/hadop" sh cmd "rm -rf /etc/ganglia & rm -rf /etc/hcatalog & rm -rf /etc/hive & rm -rf /etc/nagios & rm -rf /etc/sqop & rm -rf /var/log/hbase & rm -rf /var/log/nagios & rm -rf /var/log/hive & rm -rf /var/log/zokeper & rm -rf /var/run/hadop & rm -rf /var/run/hbase

file_cp.sh file_cp.sh

file_cp.sh

& rm -rf /var/run/zokeper " 再在Yum remove 掉安装的相关的包. sh file_cp.sh cmd "yum -y remove ambari-log4j hadop hadop-lzo hbase hive libconfuse nagios sqop zokeper" 我这⾥使⽤到了⾃⼰写的Shel,⽅便在多台机器之间执⾏命令:

htps:/github.com/xinqiyang/opshel/tre/master/hadop

Reset下Ambari-Server

ambari-server stop

ambari-server reset

ambari-server start

- 10.注意时间的同步,时间问题会导致regionserver起不来


![image 7](assets/imageFile7.png)

1. iptables 需要关闭,有的时候可能机器会重新启动,所以不单单需要 service stop 也需要 chkconfig 关闭掉. 最后安装完成后,登陆地址查看下服务的情况: htp:/管理节点ip:8080 , ⽐如我这⾥的: 登陆之后,需要设置之前在安 装Ambari-server时候输⼊的账号和密码,进⼊

htp:/192.168.1.108 8080/

![image 8](assets/imageFile8.png)

查看 ganglia的监控

![image 9](assets/imageFile9.png)

查看 nagios 的监控

![image 10](assets/imageFile10.png)

# 测试

安装完成后,看着这些都正常了,是否需要⾃⼰验证⼀下呢? 不过基本跑了冒烟测试后,正常的话,基本 还是正常的,但是我们⾃⼰也得来操作下吧. 验证HDFS

![image 11](assets/imageFile11.png)

验证Map/Reduce

![image 12](assets/imageFile12.png)

验证HBase

![image 13](assets/imageFile13.png)

验证Hive

![image 14](assets/imageFile14.png)

# 总结

到这⾥,相关的 hadop 及 hbase 及hive 的相关配置就都配置完成了,接下来需要做⼀些压⼒测试. 还有其他⽅⾯的测试, 对于Ambari带的是 HortonWorks 打包的rpm版本的 Hadop相关的源码,所 以这⾥可能会和其他的版本有⼀些不同,但是作为开发环境来说,暂时还是没有很多⼤的影响的,但是 现在还没有在⽣产上使⽤, 所以也不管说如何的稳定,接下来我会在开发项⽬的过程中,将所遇到的 Bug给列出来. 总体来说Ambari还是很值得使⽤的,毕竟能够减少很多不必要的配置时间,⽽且相对 在单机环境下, 在集群环境下更能贴近⽣产做⼀些相关的性能测试和调优测试等等,⽽且配置的 ganglia和nagios的监控也能够发布的让我们查看到集群相关的数据,总体来说还是推荐使⽤的,新东 ⻄有Bug是在所难免的,但是在⽤的过程中我们会不断的完善. 接下来如果有时间,会对Ambariserver 的功能进⾏扩展,添加诸如redis/nginx之类的常⽤的⾼性能模块的监控选项. 这个有时间在弄了. 总 之,欢迎使⽤Ambari.

/update: 最近遇到Ambari的⼀些问题: 1.在⾃定义⾥⾯开启了 apend选项后,还是依旧⽆法apend.
