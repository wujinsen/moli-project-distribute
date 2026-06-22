看⽹上很多⼈说mysql集群不是很稳定,因此这2天做了下mysql的集群,打算配置没有什么问题了,过2 天做下相关的性能测试,我的配置环境如下:

操作系统:

Centos5.2

软件包:

mysql-cluster-gpl-7.0.8a-linux-i686-glibc23.tar.gz

数据库管理节点: 192.168.100.151

数据库数据节点: 192.168.100.65,192.168.100.58

数据库sql节点: 192.168.100.65,192.168.100.58

我这⾥数据节点和sql节点⽤相同的2台机器承担.

- 1.管理节点的安装 #groupadd mysql


#useradd mysql -g mysql

#mv mysql-cluster-gpl-7.0.8a-linux-i686-glibc23.tar.gz /usr/local/

#cd /usr/local/

#tar zxvf mysql-cluster-gpl-7.0.8a-linux-i686-glibc23.tar.gz

#rm -f mysql-cluster-gpl-7.0.8a-linux-i686-glibc23.tar.gz

#mv mysql-cluster-gpl-7.0.8a-linux-i686-glibc23 mysql

#chown -R mysql:mysql mysql

#cd mysql

#scripts/mysql_install_db --user=mysql

- 2.管理节点的配置


#mkdir /var/lib/mysql-cluster

#cd /var/lib/mysql-cluster

#vi conﬁg.ini //这⾥需要⼿动添加如下内容

[ndbd default]

NoOfReplicas=2

DataMemory=80M

IndexMemory=18M //这⾥有很多参数,⼤家可以⾃⼰找下相关资料

[ndb_mgmd]

- Id=1

Hostname=10.10.1.151 //管理节点IP

datadir=/usr/local/mysql/logs

[ndbd]

- Id=2

Hostname=10.10.1.65 //数据节点IP

datadir=/usr/local/mysql/data/ //数据节点的数据⽬录,这⾥要与数据节点的配置⽂件my.cnf

的数据指定相同

[ndbd]

- Id=3


Hostname=10.10.1.58 //数据节点IP

datadir=/usr/local/mysql/data/

[MYSQLD]

[MYSQLD]

- 3.安装和配置数据节点


这⾥2台机器数据节点安装是相同的.

#groupadd mysql

#useradd mysql -g mysql

#mv mysql-cluster-gpl-7.0.8a-linux-i686-glibc23.tar.gz /usr/local/

#cd /usr/local/

#tar zxvf mysql-cluster-gpl-7.0.8a-linux-i686-glibc23.tar.gz

#rm -f mysql-cluster-gpl-7.0.8a-linux-i686-glibc23.tar.gz

#mv mysql-cluster-gpl-7.0.8a-linux-i686-glibc23 mysql

#chown -R mysql:mysql mysql

#cd mysql

#scripts/mysql_install_db --user=mysql

#cp support-ﬁles/my-medium.cnf /etc/my.cnf

#cp support-ﬁles/mysql.server /etc/init.d/mysqld

#vi /etc/my.cnf //这⾥需要⼿动添加如下的内容

datadir=/usr/local/mysql/data/ //这⾥要与管理节点的conﬁg.ini相同

ndbcluster

ndb-connectstring=10.10.1.151

[ndbd]

connect-string=10.10.1.151

[mysql_cluster]

ndb-connectstring=10.10.1.151

[ndb_mgm]

connect-string=192.168.1.151

[ndb_mgmd]

conﬁg-ﬁle=/var/lib/mysql-cluster/conﬁg.ini //这⾥要指定管理节点配置⽂件路径

- 4.启动相关服务


在管理节点启动相关服务:

#/usr/local/mysql/bin/ndb_mgmd -f /var/lib/mysql-cluster/conﬁg.ini

#netstat -lntpu

tcp 0 0 0.0.0.0:1186 0.0.0.0:*

LISTEN 22907/ndb_mgmd

看到1186端⼜开放了说明启动是正常的.

在数据节点启动相关服务:

#/usr/local/mysql/bin/ndbd --initial

#netstat -lntpu

- tcp 0 0 10.10.1.65:32975 0.0.0.0:* LISTEN 1901/ndbd

- tcp 0 0 10.10.1.65:32976 0.0.0.0:* LISTEN 1901/ndbd

- tcp 0 0 10.10.1.65:32977 0.0.0.0:* LISTEN 1901/ndbd


#service mysqld start

#netstat -lntpu

tcp 0 0 :::3306 :::* LISTEN 2258/mysqld

可以看到相关的ndbd服务以及mysql已经启动ok了.

- 5.功能测试


到管理节点查看下相关服务状态

# ndb_mgm

ndb_mgm> show

Connected to Management Server at: localhost:1186

Cluster Conﬁguration

---------------------

[ndbd(NDB)] 2 node(s)

- id=2 @10.10.1.65 (mysql-5.1.37 ndb-7.0.8, Nodegroup: 0, Master)

- id=3 @10.10.1.58 (mysql-5.1.37 ndb-7.0.8, Nodegroup: 0)

[ndb_mgmd(MGM)] 1 node(s)

id=1 @10.10.1.151 (mysql-5.1.37 ndb-7.0.8)

[mysqld(API)] 2 node(s)

- id=4 @10.10.1.65 (mysql-5.1.37 ndb-7.0.8)

- id=5 @10.10.1.58 (mysql-5.1.37 ndb-7.0.8)


可以看到这⾥的数据节点、管理节点、sql节点都是正常的.

现在我们在其中⼀个数据节点上进⾏相关数据库的创建,然后到另外⼀个数据节点上看看数据是否 同步

# /usr/local/mysql/bin/mysql -u root -p

mysql> show databases;

+--------------------+

| Database |

+--------------------+

| information_schema |

| mysql |

- | ndb_2_fs | | test |


+--------------------+

mysql> create database aa;

mysql> use aa

mysql> CREATE TABLE ctest2 (i INT) ENGINE=NDB; //这⾥必须指定数据库表的引擎为NDB,否 则同

步失败

mysql> INSERT INTO ctest2 () VALUES (1);

mysql> SELECT * FROM ctest2;

+------+

| i |

+------+

| 1 |

+------+

现在到另外⼀个数据节点查看下aa数据库是否同步过来了.

#/usr/local/mysql/bin/mysql -u root -p

mysql> show databases;

+--------------------+

| Database |

+--------------------+

| information_schema |

- | aa |

- | bb |


| mysql |

- | ndb_3_fs |


| test |

+--------------------+

mysql> use aa

- mysql> select * from ctest2;


+------+

| i |

+------+

| 1 |

+------+

从上⾯可以看到数据已经同步了,mysql集群环境已经搭建完成.

- 6.破坏性测试


⼤家在上⾯可以看到10.10.1.65作为主的数据节点,我现在把10.10.1.65这台机器关闭,看下有什么结果

ndb_mgm> show

Cluster Conﬁguration

---------------------

[ndbd(NDB)] 2 node(s)

- id=2 (not connected, accepting connect from 10.10.1.65)

- id=3 @10.10.1.58 (mysql-5.1.37 ndb-7.0.8, Nodegroup: 0, Master)


[ndb_mgmd(MGM)] 1 node(s)

id=1 @10.10.1.151 (mysql-5.1.37 ndb-7.0.8)

[mysqld(API)] 2 node(s)

- id=4 (not connected, accepting connect from any host)

- id=5 @10.10.1.58 (mysql-5.1.37 ndb-7.0.8)


从上⾯可以发现现在10.10.1.65这台机器的数据节点和sql节点已经连接不上了,10.10.1.58成为了主数

据节点,我们现在在10.10.1.58数据节点上创建⼀个表,然后恢复10.10.1.65的数据节点,看下它是否

把数据同步过来了.

先在10.10.1.58数据节点做如下操作:

mysql> create table ctest3(id int(11)) engine=NDB;

mysql> show tables;

+--------------+

| Tables_in_aa |

+--------------+

- | ctest2 |

- | ctest3 |


+--------------+

mysql> insert into ctest3 values(1);

- mysql> select * from ctest3;


+------+

| id |

+------+

| 1 |

+------+

然后我们恢复10.10.1.65数据节点,查看下ctest3数据是否同步过来了.

mysql> show databases;

+--------------------+

| Database |

+--------------------+

| information_schema |

- | aa |

- | bb |


| mysql |

| ndb_2_fs |

| test |

+--------------------+

mysql> use aa

mysql> show tables;

+--------------+

| Tables_in_aa |

+--------------+

| ctest |

- | ctest2 |

- | ctest3 |


+--------------+

mysql> select * from ctest3;

+------+

| id |

+------+

| 1 |

+------+

可以看到10.10.1.65数据节点已经把10.10.1.58数据节点的数据同步过来了,说明mysql集群是没有问题 的了.随后做下mysql性能相关的测试.

