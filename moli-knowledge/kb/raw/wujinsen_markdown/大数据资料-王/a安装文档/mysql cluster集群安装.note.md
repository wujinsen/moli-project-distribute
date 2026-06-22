集群安装

# 1. MySQL集群简介

- 1.1.什么是MySQL集群

- 1.2.集群 MySQL中名称概念


MySQL集群是⼀个⽆共享的(shared-nothing)、分布式节点架构的存储⽅案，其⽬的是提供容错性和 ⾼性能。 数据更新使⽤读已提交隔离级别（read-comitedisolation)来保证所有节点数据的⼀致性，使⽤两阶 段提交机制（two-phasedcomit)保证所有节点都有相同的数据(如果任何⼀个写操作失败，则更新失 败）。

⽆共享的对等节点使得某台服务器上的更新操作在其他服务器上⽴即可见。传播更新使⽤⼀种复杂的 通信机制，这⼀机制专⽤来提供跨⽹络的⾼吞吐量。 通过多个MySQL服务器分配负载，从⽽最⼤程序地达到⾼性能，通过在不同位置存储数据保证⾼可⽤ 性和冗余。

- 1) Sql 结点（SQL node—下图对应为 mysqld）：分布式数据库。包括⾃⾝数据和查询中⼼结点数据
- 2) 数据结点（Data node – ndbd）：集群共享数据（内存中） 3) 管理服务器 （Management Server – ndb_mgmd）：集群管理 SQL node,Data node 详情见下图：


# 2. 环境配置

hosts：

<table>
  <tr>
    <th>12.16.6.21 manager<br>12.16.6.22 daa d1<br></th>
  </tr>
</table>


192.168.56.203 sql-datanode2

# 3. 安装前要

不管是Management Server，还是Data node、SQL node，都需要先安装MySQL集群版本，然后根据 不⽤的配置来决定当前服务器有哪⼏个⾓⾊。

- 4. 创建musql⽤户和组（n台）

- 5. 关闭防⽕墙（n台）

- 6. 安装MySQL集群版本


安装之前准备好mysql⽤户和mysql⽤户组，相关命令： groupad mysql userad mysql -g mysql

chkconfig iptables of service iptables stop setenforce 0

- 6.1.主节点


- 6.1.1. 上传

- 6.1.2. 解压


su - rot cd /usr/local rz –y 上传安装包 mysql-cluster-gpl-7.4.6-linux-glibc2.5-x86_64.tar.gz

tar –zxvf mysql-cluster-gpl-7.4.6-linux-glibc2.5-x86_64.tar.gz

- 6.1.3. 重命名

- 6.1.4. 授权

- 6.1.5.下发

- 6.1.6. 安装

- 6.1.7. 配置Management Server


mv mysql-cluster-gpl-7.4.6-linux-glibc2.5-x86_64mysql

chown –R mysql:mysql mysql

su - rot scp -r /usr/local/mysql rot@slave2:/usr/local scp -r /usr/local/mysqlrot@slave3:/usr/local

/usr/local/mysql/scripts/mysql_instal_db-user=mysql-basedir=/usr/local/mysql datadir=/usr/local/mysql/data

mkdir /var/lib/mysql-cluster vi /var/lib/mysql-cluster/config.ini

<table>
  <tr>
    <th>[ndbd default] NoOfRplicas=2 DataMemory=80M IndexMemory=18M [ndb_mgmd]<br><br>Id=1 Hostname=192.168.56.20 datadir=/usr/local/mysql/logs [ndbd]<br>Id=2<br><br>Hostname=192.168.56.201 datadir=/usr/local/mysql/data/ [ndbd]<br><br>Id=3<br><br>Hostname=192.168.56.202 datadir=/usr/local/mysql/data/<br><br><br></th>
  </tr>
</table>


[MYSQLD]

## 6.2. datanode(n台)

- 6.2.1. 授权

- 6.2.2. 安装

- 6.2.3. 配置Data Node

<table>
  <tr>
    <th>datadir=/usr/local/mysql/data/<br><br>cluster ndb-conectstring=192.168.56.20 [ndbd] conect-string=192.168.56.20 [mysql_cluster] ndb-conectstring=192.168.56.20 [ndb_mgm] conect-string=192.168.56.20 [ndb_mgmd]</th>
  </tr>
</table>


config-file=/var/lib/mysql-cluster/config.ini

- 6.2.4. 在管理节点启动相关服务:


su - rot chown -R mysql:mysql /usr/local/mysql

/usr/local/mysql/scripts/mysql_instal_db-user=mysql-basedir=/usr/local/mysql datadir=/usr/local/mysql/data

su – rot cp /usr/local/mysql/suport-files/my-default.cnf/etc/my.cnf cp /usr/local/mysql/suport-files/mysql.server/etc/init.d/mysqld vi /etc/mycnf

#/usr/local/mysql/bin/ndb_mgmd -f /var/lib/mysql-cluster/conﬁg.ini #netstat -lntpu tcp 0 00.0.0.0:1186 0.0.0.0:* LISTEN 22907/ndb_mgmd

看到1186端⼜开放了说明启动是正常的.

- 6.2.5.


在数据节点启动相关服务:

#/usr/local/mysql/bin/ndbd --initial #netstat -lntpu tcp 0

- 010.10.1.65:32975 0.0.0.0:* LISTEN 1901/ndbd tcp 0

- 010.10.1.65:32976 0.0.0.0:* LISTEN 1901/ndbd tcp 0

- 010.10.1.65:32977 0.0.0.0:* LISTEN 1901/ndbd


#service mysqld start #netstat -lntpu tcp 0 0:::3306 :::* LISTEN 2258/mysqld

可以看到相关的ndbd服务以及mysql已经启动ok了.

# 7. 功能测试

到管理节点查看下相关服务状态 # ndb_mgm ndb_mgm> show Connected to Management Server at: localhost:1186 Cluster Conﬁguration ----

----------------- [ndbd(NDB)] 2 node(s) id=2 @10.10.1.65 (mysql-5.1.37 ndb-7.0.8,Nodegroup: 0, Master) id=3 @10.10.1.58 (mysql-5.1.37 ndb-7.0.8,Nodegroup: 0)

[ndb_mgmd(MGM)] 1 node(s) id=1 @10.10.1.151 (mysql-5.1.37 ndb-7.0.8) [mysqld(API)] 2 node(s) id=4 @10.10.1.65 (mysql-5.1.37 ndb-7.0.8) id=5 @10.10.1.58 (mysql-5.1.37 ndb-

- 7.0.8)


可以看到这⾥的数据节点、管理节点、sql节点都是正常的.

现在我们在其中⼀个数据节点上进⾏相关数据库的创建,然后到另外⼀个数据节点上看看数据是否同步

# /usr/local/mysql/bin/mysql -u root -p

mysql> show databases;+--------------------+| Database |+--------------------+| information_schema | |mysql | | ndb_2_fs | |test | +--------------------+mysql> create database aa;mysql> use aamysql> CREATE TABLE ctest2 (i INT) ENGINE=NDB; //这⾥必须指定数据库表的引擎为NDB,否 则同步失败，只有这样⼦，这两台机器才能共享数据库的数据结构mysql> INSERT INTO ctest2 () VALUES

(1);mysql> SELECT * FROM ctest2;+------+| i |+------+| 1 | +------+

现在到另外⼀个数据节点查看下aa数据库是否同步过来了.

#/usr/local/mysql/bin/mysql -u root -p mysql> show databases;+--------------------+| Database |+--------------------+| information_schema | |aa | |bb | |mysql | | ndb_3_fs | | test | +--------------------

+mysql> use aamysql> select * from ctest2;+------+| i |+------+| 1 | +------+

从上⾯可以看到数据已经同步了,mysql集群环境已经搭建完成.

# 8. 破坏性测试

⼤家在上⾯可以看到10.10.1.65作为主的数据节点,我现在把10.10.1.65这台机器关闭,看下有什么结果 ndb_mgm> show Cluster Conﬁguration --------------------- [ndbd(NDB)] 2 node(s) id=2 (not connected,

accepting connect from 10.10.1.65) id=3 @10.10.1.58 (mysql-5.1.37 ndb-7.0.8,Nodegroup: 0, Master) [ndb_mgmd(MGM)] 1 node(s) id=1 @10.10.1.151 (mysql-5.1.37 ndb-7.0.8) [mysqld(API)] 2 node(s) id=4 (not connected, accepting connect from any host) id=5 @10.10.1.58

(mysql-5.1.37 ndb-7.0.8)

从上⾯可以发现现在10.10.1.65这台机器的数据节点和sql节点已经连接不上了,10.10.1.58成为了主数 据节点,我们现在在10.10.1.58数据节点上创建⼀个表,然后恢复10.10.1.65的数据节点,看下它是否 把数据同步过来了.

先在10.10.1.58数据节点做如下操作: mysql> create table ctest3(id int(11)) engine=NDB; mysql> show tables;+--------------+| Tables_in_aa |+----

----------+| ctest2 | | ctest3 | +--------------+mysql> insert into ctest3 values(1);mysql> select * from ctest3;+------+| id |+------+| 1 | +------+

然后我们恢复10.10.1.65数据节点,查看下ctest3数据是否同步过来了.

mysql> show databases;+--------------------+| Database |+--------------------+| information_schema | |aa | |bb | |mysql | | ndb_2_fs | |test | +--------------------

+mysql> use aamysql> show tables; +--------------+| Tables_in_aa |+--------------+| ctest | | ctest2 | | ctest3 | +--------------+mysql> select * from ctest3;+------+| id |+------+| 1 | +------+

可以看到10.10.1.65数据节点已经把10.10.1.58数据节点的数据同步过来了,说明mysql集群是没有问题的了. 随后做下mysql性能相关的测试.

- 9. 修改rot密码

- 10. 开启远程连接


/usr/local/mysql/bin/mysqladmin -u rot pasword 'rot'

mysql –urot -prot GRANT AL PRIVILEGES ON.* TO 'rot'@'%'IDENTIFIED BY 'rot' WITH GRANT OPTION;

