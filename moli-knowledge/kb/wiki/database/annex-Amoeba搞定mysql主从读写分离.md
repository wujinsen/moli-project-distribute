---
title: Amoeba搞定mysql主从读写分离（原文插图 annex）
slug: annex-Amoeba搞定mysql主从读写分离
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/a安装文档/MySQL主从安装文档（ok）.note.attach/Amoeba搞定mysql主从读写分离.md
related: [mysql-索引]
created: 2026-07-05
updated: 2026-07-05
---

![image 1](assets/imageFile1.png)

<table>
  <tr>
    <th> </th>
  </tr>
</table>


![image 2](assets/imageFile2.png)

![image 3](assets/imageFile3.png)

Chinaunix首页 | 论坛 | 问答 | 博客 登录 | 注册

博文

<table>
  <tr>
    <th>【原创评选】12-02月原创博文评选</th>
  </tr>
</table>


# 从网络管理员到DBA的奋斗之路

feihong.blog.chinaunix.net

身未动，心已远，喧嚣的城市需要一份心灵的关怀，飞哥普洱！htt://feigetea.taobao.com

首页 | 博文目录 | 关于我

<table>
  <tr>
    <th>加关注 短消息<br><br>论坛 加好友<br><br>博客访问： 716517 博文数量： 368 博客积分： 9635 博客等级： 中将 技术积分： 8873 用 户 组： 普通用户 注册时间： 2009-03-16 10:37<br><br><table>
  <tr>
    <th>![image 4](assets/imageFile4.png)</th>
  </tr>
</table>
<br><br>飞鸿无痕</th>
  </tr>
</table>


## Amoeba搞定mysql主从读写分离 2010-10-21 16:36:14

分类： LINUX 原创作品，转载请保留原文链接：来自飞鸿无痕CU博客

前言：一直想找一个工具，能很好的实现mysql主从的读写分离架构，曾经试用过mysql-proxy发现lua用起来很不爽，尤其是不懂lua脚

本，突然发现了Amoeba这个项目，试用了下，感觉还不错，写出文档，希望对大家有帮助！ 一、Amoeba 是什么 Amoeba(变形虫)项目，专注 分布式数据库 proxy 开发。座落与Client、DB Server(s)之间。对客户端透明。具有负载均衡、高可用 性、sql过滤、读写分离、可路由相关的query到目标数据库、可并发请求多台数据库合并结果。 主要解决：

- • 降低 数据切分带来的复杂多数据库结构
- • 提供切分规则并降低 数据切分规则 给应用带来的影响
- • 降低db 与客户端的连接数
- • 读写分离


二、为什么要用Amoeba

文章分类

目前要实现mysql的主从读写分离，主要有以下几种方案：

全部博文（368） 普洱茶（4） 自动化运维（6） 学习笔记（3） IT职场（3） 分布式文件系统学（1） perl学习（11） 安全（2） windows（3） oracle学习笔记（6） electro-server（1） SmartFoxServer（2） 监控（12） 工作记录（22） English Study（7） 电脑维护（6）

- 1、 通过程序实现，网上很多现成的代码，比较复杂，如果添加从服务器要更改多台服务器的代码。
- 2、 通过mysql-proxy来实现，由于mysql-proxy的主从读写分离是通过lua脚本来实现，目前lua的脚本的开发跟不上节奏，而写没有完 美的现成的脚本，因此导致用于生产环境的话风险比较大，据网上很多人说mysql-proxy的性能不高。
- 3、 自己开发接口实现，这种方案门槛高，开发成本高，不是一般的小公司能承担得起。
- 4、 利用阿里巴巴的开源项目Amoeba来实现，具有负载均衡、高可用性、sql过滤、读写分离、可路由相关的query到目标数据库，并且 安装配置非常简单。国产的开源软件，应该支持，目前正在使用，不发表太多结论，一切等测试完再发表结论吧，哈哈！


三、快速架设amoeba，实现mysql主从读写分离

假设amoeba的前提条件：

n Java SE 1.5 或以上 Amoeba 框架是基于JDK1.5开发的，采用了JDK1.5的特性。

n 支持Mysql 协议版本10（mysql 4.1以后的版本）。

n 您的网络环境至少运行有一个mysql 4.1以上的服务

1、首先介绍下我的实验环境。

高可用（18） mail（3） LVM_RAID（6）

System： CentOS release 5.4

Master mysql：192.168.1.121

Slave mysql：192.168.1.108

mysql（111） DNS--bind（4） vpn（3） cache（7） web_server（34） 生活杂谈（6）

Amoeba server： 192.168.1.159

架构如如下所示：

linux系统（57） shell学习（26）

未分配的博文（4）

![image 5](assets/imageFile5.png)

<table>
  <tr>
    <th>文章存档<br><br>2013年（8）<br><br>2012年（96） 2011年（67） 2010年（149）<br><br>2009年（48）</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 6](assets/imageFile6.png)<br><br>bamboo78 jackson1 qingheli<br><br>![image 7](assets/imageFile7.png)<br><br>![image 8](assets/imageFile8.png)<br><br>![image 9](assets/imageFile9.png)<br><br>devilkin joepayne 左右江南<br><br>![image 10](assets/imageFile10.png)<br><br>![image 11](assets/imageFile11.png)<br><br>![image 12](assets/imageFile12.png)<br><br>justlook gddzbq hkebao<br><br>![image 13](assets/imageFile13.png)<br><br>![image 14](assets/imageFile14.png)<br><br>我的朋友</th>
  </tr>
</table>


我这里只用了一个从数据库！

- 2、安装配置mysql主从环境，mysql的安装省略，请自行编译或者用rpm安装


###在master mysql创建同步用户

grant replication slave,file on *.* to 'replication'@'192.168.1.108' identified by '123456';

flush privileges;

####修改master的my.cnf的配置

<table>
  <tr>
    <th>![image 15](assets/imageFile15.png)<br><br>左右江南 hxl llhhtt77<br><br>![image 16](assets/imageFile16.png)<br><br>![image 17](assets/imageFile17.png)<br><br>![image 18](assets/imageFile18.png)<br><br>airmy lshdcr conquery<br><br>![image 19](assets/imageFile19.png)<br><br>![image 20](assets/imageFile20.png)<br><br>![image 21](assets/imageFile21.png)<br><br>wangtish keinbj bamboo78<br><br>![image 22](assets/imageFile22.png)<br><br>![image 23](assets/imageFile23.png)<br><br>最近访客</th>
  </tr>
</table>


<table>
  <tr>
    <th>log-bin=mysql-bin #打开mysql二进制日志 server-id = 1 #设置mysql_id，主从不能相同 binlog-do-db=test #设置二进制日志记录的库 binlog-ignore-db=mysql ##设置二进制日志不记录的库 sync_binlog=1</th>
  </tr>
</table>


####修改slave的my.cnf的配置

<table>
  <tr>
    <th>log-bin=mysql-bin server-id = 2 replicate-do-db=test #设置同步的库 replicate-ignore-db=mysql #设置不同步的库 log-slave-updates #同步后记录二进制日志 slave-skip-errors=all sync_binlog=1 slave-net-timeout=60</th>
  </tr>
</table>


<table>
  <tr>
    <th>订阅</th>
  </tr>
</table>


<table>
  <tr>
    <th>推荐博文<br><br>·mysql 如何提高批量导入的速... ·详解C中volatile关键字 ... ·win7 下开发node.js c++ addo... ·AIX小机在线更换及升级硬件设... ·oracle 回收站</th>
  </tr>
</table>


分别重启主从mysqld服务，登录主mysql，在主上执行flush tables with read lock;后将test数据库的数据copy到从上，并记录 下主上show master status\G的结果： 如：

<table>
  <tr>
    <th>mysql> show master status\G;<br><br>*************************** 1. row ***************************<br><br>File: mysql-bin.000022<br><br>Position: 1237<br><br>Binlog_Do_DB: test<br><br>Binlog_Ignore_DB: mysql</th>
  </tr>
</table>


<table>
  <tr>
    <th>热词专题<br><br>·硬盘安装 CentOS-6.0 ·linux iso文件制作 ·Ylmf OS 4.0正式版发布 集成1... ·Linux下使用source insight... ·ubuntu 11.10 搭建samba</th>
  </tr>
</table>


然后执行unlock tables

登录从mysql，在从上执行： stop slave; change master to master_host='192.168.1.121',master_user='replication',master_password='123456',master_log_file='mysqlbin.000022', master_log_pos=1237; start slave；

show slave status\G;

如果出现下面的情况，说明主从同步已经成功！

Slave_IO_Running: Yes

Slave_SQL_Running: Yes

- 3、安装JDK环境

下载jdk1.5或者更新版本,地址 http://java.sun.com/javase/downloads/index.jsp

我用的是：jdk-6u20-linux-i586-rpm.bin

在Amoeba server上执行

chmod +x jdk-6u20-linux-i586-rpm.bin

./ jdk-6u20-linux-i586-rpm.bin

##然后按n次空格键，然后输入yes就可以了！^_ ^

ln -s /usr/java/jdk1.6.0_20/ /usr/java/jdk1.6

vi /etc/profile

#添加如下两行内容

export JAVA_HOME=/usr/java/jdk1.6

export PATH=$JAVA_HOME/bin:$JAVA_HOME/jre/bin:$PATH

#执行下面的命令使更改的内容生效

source /etc/profile

- 4、 安装amoeba-mysql


下载amoeba-mysql，目前最新版本为amoeba-mysql-1.3.1-BETA.zip

<table>
  <tr>
    <th>mkdir /usr/local/amoeba/<br><br>wget http://blogimg.chinaunix.net/blog/upfile2/101027160252.zip<br><br>unzip 101027160252.zip</th>
  </tr>
</table>


配置文件位于conf目录下，执行文件位于bin目录下,解压后发现bin目录下的启动文件没有可执行权限，请执行：chmod -R +x /usr/local/amoeba/bin/

Amoeba For MySQL 的使用非常简单，所有的配置文件都是标准的XML 文件，总共有四个配置文件。分别为：

◆ amoeba.xml：主配置文件，配置所有数据源以及Amoeba 自身的参数设置；实现主从的话配置这个文件就可以了；

◆ rule.xml：配置所有Query 路由规则的信息；

◆ functionMap.xml：配置用于解析Query 中的函数所对应的Java 实现类；

◆ rullFunctionMap.xml：配置路由规则中需要使用到的特定函数的实现类；

下面我们就来通过更改amoeba.xml配置文件实现mysql主从读写分离，我的配置如下：

<?xml version="1.0" encoding="gbk"?>

<!DOCTYPE amoeba:configuration SYSTEM "amoeba.dtd"> <amoeba:configuration xmlns:amoeba="http://amoeba.meidusa.com/">

<server> <!- proxy server绑定的端口 --> <property name="port">8066</property>

<!- proxy server绑定的IP --> <property name="ipAddress">192.168.1.10</property> <!- proxy server net IO Read thread size -> <property name="readThreadPoolSize">20</property>

<!- proxy server client process thread size --> <property name="clientSideThreadPoolSize">30</property>

<!- mysql server data packet process thread size --> <property name="serverSideThreadPoolSize">30</property>

<!- socket Send and receive BuferSize(unit:K) --> <property name="netBufferSize">128</property>

<!- Enable/disable TCP_NODELAY (disable/enable Nagle's algorithm). --> <property name="tcpNoDelay">true</property>

<!- 对外验证的用户名 --> <property name="user">root</property>

<!- 对外验证的密码 -> <property name="pasword">password</property>

<!- query timeout( default: 60 second , TimeUnit:second) --> <property name="queryTimeout">60</property>

</server>

<!-每个ConnectionManager都将作为一个线程启动。 manager负责Connection IO读写/死亡检测

--> <conectionManagerList>

<connectionManager name="defaultManager" class="com.meidusa.amoeba.net.MultiConectionManagerWrapper"> <property name="subManagerClassName">com.meidusa.amoeba.net.AuthingableConnectionManager</property>

<!-default value is avaliable Processors <property name="procesors">5</property>

->

</connectionManager> </connectionManagerList>

<dbServerList>

<!一台mysqlServer 需要配置一个pool， 如果多台 平等的mysql需要进行loadBalance， 平台已经提供一个具有负载均衡能力的objectPool：com.meidusa.amoeba.mysql.server.MultipleServerPool 简单的配置是属性加上 virtual="true",该Pool 不允许配置factoryConfig 或者自己写一个ObjectPol。

-->

- <dbServer name="server1">

<!-- PolableObjectFactory实现类 -> <factoryConfig class="com.meidusa.amoeba.mysql.net.MysqlServerConnectionFactory">

<property name="manager">defaultManager</property>

<!- 真实mysql数据库端口 --> <property name="port">3306</property>

<!- 真实mysql数据库IP -> <property name="ipAddres">192.168.1.121</property> <property name="schema">test</property>

<!- 用于登陆mysql的用户名 --> <property name="user">zhang</property>

<!- 用于登陆mysql的密码 -->

<property name="password">zhang123</property>

</factoryConfig>

<!-- ObjectPool实现类 --> <poolConfig class="com.meidusa.amoeba.net.polable.PoolableObjectPool">

<property name="maxActive">200</property> <property name="maxIdle">200</property> <property name="minIdle">10</property> <property name="minEvictableIdleTimeMillis">600000</property> <property name="timeBetweenEvictionRunsMillis">60000</property> <property name="testOnBorrow">true</property> <property name="testWhileIdle">true</property>

</polConfig> </dbServer>

- <dbServer name="server2">


<!-- PolableObjectFactory实现类 -> <factoryConfig class="com.meidusa.amoeba.mysql.net.MysqlServerConnectionFactory">

<property name="manager">defaultManager</property>

<!- 真实mysql数据库端口 --> <property name="port">3306</property>

<!- 真实mysql数据库IP ->

<property name="ipAddres">192.168.1.108</property> <property name="schema">test</property>

<!- 用于登陆mysql的用户名 --> <property name="user">zhang</property>

<!- 用于登陆mysql的密码 -->

<property name="password">zhang123</property>

</factoryConfig>

<!-- ObjectPool实现类 --> <poolConfig class="com.meidusa.amoeba.net.polable.PoolableObjectPool">

<property name="maxActive">200</property> <property name="maxIdle">200</property> <property name="minIdle">10</property> <property name="minEvictableIdleTimeMillis">600000</property> <property name="timeBetweenEvictionRunsMillis">60000</property> <property name="testOnBorrow">true</property> <property name="testWhileIdle">true</property>

</polConfig> </dbServer>

<dbServer name="master" virtual="true">

<poolConfig class="com.meidusa.amoeba.server.MultipleServerPool"> <!- 负载均衡参数 1=ROUNDROBIN , 2=WEIGHTBASED , 3=HA--> <property name="loadbalance">1</property>

<!- 参与该pol负载均衡的poolName列表以逗号分割 --> <property name="polNames">server1</property>

</polConfig> </dbServer>

<dbServer name="slave" virtual="true">

<poolConfig class="com.meidusa.amoeba.server.MultipleServerPool"> <!- 负载均衡参数 1=ROUNDROBIN , 2=WEIGHTBASED , 3=HA--> <property name="loadbalance">1</property>

<!- 参与该pol负载均衡的poolName列表以逗号分割 --> <property name="polNames">server1,server2</property>

</polConfig> </dbServer>

</dbServerList>

<queryRouter clas="com.meidusa.amoeba.mysql.parser.MysqlQueryRouter"> <property name="ruleConfig">${amoeba.home}/conf/rule.xml</property> <property name="functionConfig">${amoeba.home}/conf/functionMap.xml</property> <property name="ruleFunctionConfig">${amoeba.home}/conf/ruleFunctionMap.xml</property> <property name="LRUMapSize">1500</property> <property name="defaultPool">master</property>

<property name="writePool">master</property> <property name="readPool">slave</property> <property name="nedParse">true</property>

</queryRouter> </amoeba:configuration>

启动amoeba

/usr/local/amoeba/bin/amoeba &

检验启动是否成功（使用的是默认的8066端口）：

<table>
  <tr>
    <th>[root@Centos2 amoeba]# ps aux | grep amoeba<br><br>root 24580 0.2 19.2 408912 49264 pts/1 Sl 12:52 0:11 /usr/java/jdk1.6/bin/java -server -Xms256m -Xmx256m<br><br>-Xss128k -Damoeba.home=/usr/local/amoeba -Dclassworlds.conf=/usr/local/amoeba/bin/amoeba.classworlds -classpath /usr/local/amoeba/lib/classworlds-1.0.jar org.codehaus.classworlds.Launcher [root@Centos2 amoeba]# netstat -lnp | grep java tcp 0 0 ::ffff:192.168.1.159:8066 :::* LISTEN 24580/java</th>
  </tr>
</table>


- 5、 测试


测试之前先要保证amoeba-server有访问两个主从服务器test库的权限,在主从mysql上都执行：

grant all on test.* to zhang@'192.168.1.%' identified by 'zhang123';

#用户名密码要和前面配置的意志

flush privileges;

测试的时候和我们平时使用一样，amoeba-mysql对我们应用透明，就是个mysql的代理了！

登录mysql使用如下命令（用户名密码和上面配置的要一致）：

mysql -uroot -ppassword -h192.168.1.159 -P8066

登录上去后，为了测试读和写必须，先把mysql的主从复制停掉，才能更清楚地看出读写的服务器是哪台，在从上使用stop slave;登录 到amoeba-mysql上，使用命令mysql -uroot -ppassword -h192.168.1.159 -P8066，然后执行写和读操作，查看写的是哪台服务器，读 的是哪台服务器，实验结果显示：写只在主上进行，读在主和从都进行，比率是1:1

测试步骤：

还没有停掉从同步之前，创建一个表：

create table zhang (id int(10) ,name varchar(10),address varchar(20));

在从上执行stop slave;

然后在主从上各插入一条不同数据（供测试读的时候用），

在主上插入：insert into zhang values('1','zhang','this_is_master');

在从上插入：insert into zhang values('2','zhang','this_is_slave');

接下来通过登录amoeba-mysql上来测试读写：

[root@Centos2 ~]# mysql -uroot -ppassword -h192.168.1.159 -P8066

Welcome to the MySQL monitor. Commands end with ; or \g.

Your MySQL connection id is 14556042

Server version: 5.1.45-mysql-amoeba-proxy-1.3.1-BETA Source distribution

Type 'help;' or '\h' for help. Type '\c' to clear the buffer.

mysql> use test;

Database changed

mysql> select * from zhang; ###第一次执行显示在主上读取的数据！

+------+-------+----------------+

| id | name | address |

+------+-------+----------------+

- | 1 | zhang | this_is_master |

+------+-------+----------------+

1 row in set (0.02 sec)

mysql> select * from zhang; ####第二次执行select语句显示是在从上读取的数据

+------+-------+---------------+

| id | name | address |

+------+-------+---------------+

- | 2 | zhang | this_is_slave |


+------+-------+---------------+

1 row in set (0.02 sec)

mysql> insert into zhang values('3','hhh','test_write'); ###插入一条数据，然后查询

Query OK, 1 row affected (0.01 sec)

mysql> select * from zhang; ###我们可以看到插入的数据被添加到了主上！

+------+-------+----------------+ ####可以多插入几次数据看看是否会出现错误！

+------+-------+----------------+

- | 1 | zhang | this_is_master |

| 3 | hhh | test_write |

+------+-------+----------------+

mysql> select * from zhang; ###从上还是没有插入，因为执行了stop slave；

+------+-------+---------------+

| id | name | address |

+------+-------+---------------+

- | 2 | zhang | this_is_slave |


+------+-------+---------------+

- 6、 简单主从权重配置


大家可能会想到，我们加入只有两台数据库服务器，一台主，一台从，按照上面的配置只能是主和从的读取比率是1:1,而写又全部 在主上进行，这样主的压力就很大了，所以如果能让主和从的读设置权重，比如设置成1:3,这样就可以很好的解决主从不压力均衡 的问题！通过研究确实可以！

配置就是将上面的读的池的配置更改一下：

将<property name="poolNames">server1,server2</property>更改成

<property name="poolNames">server1,server2,server2,server2</property>

我测试的结果刚好为1:3,如下：

mysql> select * from zhang;

+------+-------+----------------+

| id | name | address |

+------+-------+----------------+

- | 1 | zhang | this_is_master |

| 3 | hhh | test_write |

+------+-------+----------------+

2 rows in set (0.01 sec)

mysql> select * from zhang;

+------+-------+---------------+

| id | name | address |

+------+-------+---------------+

- | 2 | zhang | this_is_slave |


+------+-------+---------------+

1 row in set (0.04 sec)

mysql> select * from zhang;

+------+-------+---------------+

| id | name | address |

+------+-------+---------------+

| 2 | zhang | this_is_slave |

+------+-------+---------------+

1 row in set (0.01 sec)

mysql> select * from zhang;

+------+-------+---------------+

+------+-------+---------------+

| 2 | zhang | this_is_slave |

+------+-------+---------------+

学习链接：

开发者博客链接： http://amoeba.sourceforge.net/wordpress/

amoeba 中文文档下载地址：http://amoeba.meidusa.com/amoeba.pdf

amoeba 未来发展方向：http://amoeba.meidusa.com/amoeba-big-picture.pdf

阅读(2413) | 评论(2) | 转发(3) |

上一篇：MySQL Cluster 详细配置文件(config.ini) 下一篇：Memcache的部署和使用

#### 0

相关热门文章

android-- A10开发板--Tslib ... 终端IO编程B（《Linux编程技...

linux 常见服务端口

LNMP 老是会出现502？

【ROOTFS搭建】busybox的httpd...

suse 运用一个shell获取本机和...

MySQL-PROXY实现读写分离...

什么是shell

虚拟机 unix 配置ip

Xtrabackup安装及使用

linux socket的bug??

hp-un 主机新系统读不到磁盘阵... mysql出现问题：Starting MySQ...

mysql安装，配置主从同步...

linux的线程是否受到了保护？...

给主人留下些什么吧！~~

<table>
  <tr>
    <th>![image 24](assets/imageFile24.png)</th>
  </tr>
</table>


飞鸿无痕 2012-04-10 14:13:45 kerlion: JAVA实现的，为什么不用C++/C实现，我觉得比JAVA难不到哪儿去吧？ Java的性能到底怎么样？？..... 以前测试的时候，比mysql-proxy的性能要好一点！可惜不支持事务。

回复 | 举报

<table>
  <tr>
    <th>![image 25](assets/imageFile25.png)</th>
  </tr>
</table>


kerlion 2012-02-01 16:04:26

JAVA实现的，为什么不用C++/C实现，我觉得比JAVA难不到哪儿去吧？ Java的性能到底怎么样？？

回复 | 举报

评论热议

<table>
  <tr>
    <th>请登录后评论。 登录 注册<br><br></th>
  </tr>
</table>


关于我们 | 关于IT168 | 联系方式 | 广告合作 | 法律声明 | 免费注册 Copyright 2001-2010 ChinaUnix.net All Rights Reserved 北京皓辰网域网络信息技术有限公司. 版权所有

感谢所有关心和支持过ChinaUnix的朋友们 京ICP证041476号 京ICP证060528号
