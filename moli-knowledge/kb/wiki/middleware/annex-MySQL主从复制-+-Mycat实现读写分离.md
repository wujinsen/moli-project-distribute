---
title: MySQL主从复制 + Mycat实现读写分离.note（原文插图 annex）
slug: annex-MySQL主从复制-+-Mycat实现读写分离
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/中间件/MyCat/MySQL主从复制 + Mycat实现读写分离.note.md
related: [消息队列]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.cnblogs.com/YingYue/p/767814.html

版权所有, 转载请注明出处 说明：两台MySQL服务器都是使⽤CentOS6.5系统，MySQL版本为mysql-5.7.17

http://www.cnblogs.com/YingYue/p/7677814.html

MySQL⼀主⼀被实现主从复制

注意：写包括insert，delete，update 操作；读只有select其他操作由主master的⼆进制⽂件决定。 ⼀．在192.168.42.128上安装MySQL（主库） 开启账号root的远程登录权限，过程：略 配置主服务器的my.cnf ⽂件 并重启（开启⼆进制⽂件等）

![image 1](assets/imageFile1.png)

复制代码

##------------------------------------------周建旭2017年3⽉6⽇ 12:40:10添加---------------------------

---------#

# # server-id 唯⼀的服务辨识号,数值位于 1 到 2^32-1之间. # # 此值在master和slave上都需要设置. # # 如果 “master-host” 没有被设置,则默认为1, 但是如果忽略此选项,MySQL不会作为master⽣效.

- server-id = 1 #[必须]服务器唯⼀ID，默认是1


##------------------------------------------master上的配置开始---------------------------------------

---------## # # # # log-bin 打开⼆进制⽇志功能. # # # 在复制(replication)配置中,作为MASTER主服务器必须打开此项 # # # 如果你需要从你最后的备份中做基于时间点的恢复,你也同样需要⼆进制⽇志. # log-bin = mysql-bin #[必须]启⽤⼆进制⽇志 # # # 需要开启⽣成⼆进制⽇志记录相关配置，配置在需要被复制的服务器上，即：master # binlog-do-db = test_db #指定对名称为test_db的数据库记录⼆进制⽇志 # binlog-ignore-db = mysql #指定不对名称为mysql的数据库记录⼆进制⽇志 # binlog-ignore-db = information_schema #指定不对名称为information_schema的数据库记录⼆进制⽇志 # binlog_format = mixed #binlog⽇志格式，mysql默认采⽤，如果从服务器slave有别的slave要复制那么该slave也需要这⼀ 项# expire_logs_days = 7 #超过7天的binlog删除### # # # ##-------------------------------------------master上的配置开始--------------------------------------

---------##

##-------------------------------------------slave上的配置开始---------------------------------------

---------## # # # # replicate-do-db 需要做复制的数据库,如果复制多个数据库，重复设置这选项即可master上不需要此项，slave上需要 # #replicate-do-db = test_db #复制名称为test_db的数据库 # # # replicate-ignore-db 不需要复制的数据库，如果要忽略复制多个数据库，重复设置这个选项即可 # #replicate-ignore-db = mysql #不需要（忽略）复制名称为mysql的数据库 # #replicate-ignore-db = information_schema #不需要（忽略）复制名称为information_schema的数据库

# # # # 如果你在使⽤链式从服务器结构的复制模式 (A->B->C), # # # 你需要在服务器B上打开此项. # # # 此选项打开在从线程上重做过的更新的⽇志, # # # 并将其写⼊从服务器的⼆进制⽇志. # # # 默认值为OFF;设置log_slave_updates = 1即表示开启 # #log_slave_updates = 1 # # # ##------------------------------------------slave上的配置结束----------------------------------------

---------##

skip-external-locking #MySQL选项以避免外部锁定。该选项默认开启 default-storage-engine = InnoDB #默认存储引擎 lower_case_table_names = 1 #忽略表⼤⼩写

##------------------------------------------周建旭2017年3⽉6⽇ 12:40:10添加---------------------------

---------#

![image 2](assets/imageFile2.png)

复制代码

截图

![image 3](assets/imageFile3.png)

注意：

binlog-do-db binlog-ignore-db

= 需要复制的数据库名，如果复制多个数据库，重复设置这个选项即可

= 不需要复制的数据库库名，如果复制多个数据库，重复设置这个选项即可

⼆．在192.168.42.129 上安装MySQL（备库） 创建⽤户yingyue 授权可以远程登录和本地登录，拥有该实例所有数据库的权限。过程：略

配置slave服务器的my.cnf ⽂件 并重启（配置slave对master的⼆进制⽂件复制）

![image 4](assets/imageFile4.png)

复制代码

##------------------------------------------周建旭2017年3⽉6⽇ 12:40:10添加---------------------------

---------#

# # server-id 唯⼀的服务辨识号,数值位于 1 到 2^32-1之间. # # 此值在master和slave上都需要设置. # # 如果 “master-host” 没有被设置,则默认为1, 但是如果忽略此选项,MySQL不会作为master⽣效.

- server-id = 2 #[必须]服务器唯⼀ID，默认是1


##------------------------------------------master上的配置开始---------------------------------------

---------## # # # # log-bin 打开⼆进制⽇志功能. # # # 在复制(replication)配置中,作为MASTER主服务器必须打开此项 # # # 如果你需要从你最后的备份中做基于时间点的恢复,你也同样需要⼆进制⽇志. # #log-bin = mysql-bin #[必须]启⽤⼆进制⽇志 # # # 需要开启⽣成⼆进制⽇志记录相关配置，配置在需要被复制的服务器上，即：master # #binlog-do-db = test_db #指定对名称为test_db的数据库记录⼆进制⽇志 # #binlog-ignore-db = mysql #指定不对名称为mysql的数据库记录⼆进制⽇志 # #binlog-ignore-db = information_schema #指定不对名称为information_schema的数据库记录⼆进制⽇志 # #binlog_format = mixed #binlog⽇志格式，mysql默认采⽤，如果从服务器slave有别的slave要复制那么该slave也需要该 项 # #expire_logs_days = 7 #超过7天的binlog删除### # # # ##-------------------------------------------master上的配置开始--------------------------------------

---------##

##-------------------------------------------slave上的配置开始---------------------------------------

---------## # # # # replicate-do-db 需要做复制的数据库,如果复制多个数据库，重复设置这选项即可master上不需要此项，slave上需要 # replicate-do-db = test_db #复制名称为test_db的数据库 # # # replicate-ignore-db 不需要复制的数据库，如果要忽略复制多个数据库，重复设置这个选项即可 # replicate-ignore-db = mysql #不需要（忽略）复制名称为mysql的数据库 # replicate-ignore-db = information_schema #不需要（忽略）复制名称为information_schema的数据库

# # # # 如果你在使⽤链式从服务器结构的复制模式 (A->B->C), # # # 你需要在服务器B上打开此项. # # # 此选项打开在从线程上重做过的更新的⽇志, # # # 并将其写⼊从服务器的⼆进制⽇志. # # # 默认值为OFF;设置log_slave_updates = 1即表示开启 # #log_slave_updates = 1 # # # ##------------------------------------------slave上的配置结束----------------------------------------

---------##

skip-external-locking #MySQL选项以避免外部锁定。该选项默认开启 default-storage-engine = InnoDB #默认存储引擎 lower_case_table_names = 1 #忽略表⼤⼩写

###------------------------------------------周建旭2017年3⽉6⽇ 12:40:10添加--------------------------

----------#

![image 5](assets/imageFile5.png)

复制代码

截图

![image 6](assets/imageFile6.png)

注意：

replicate-do-db replicate-ignore-db

= 需要复制的数据库名，如果复制多个数据库，重复设置这个选项即可

= 不需要复制的数据库名，如果复制多个数据库，重复设置这个选项即可

三．连接（我使⽤的是sqlyog）192.168.42.128服务器上的MySQL（主） show master status; /*!查看主master的状态，如果没有数据，需要在my.cnf 中配置*/

![image 7](assets/imageFile7.png)

注意：show master status; 只有在my.cnf中配置了log-bin开启⽣成数据库⼆进制记录才会显示数据 grant replication slave on *.* to 'yingyue'@'%'; /*!给ip地址为所有（%表示任何ip）MySQL服务器上的 盈⽉ 授权对该master 的复制权限*/ flush privileges; /*!刷新权限*/

四．连接 （我使⽤的是sqlyog）192.168.42.129 服务器上的MySQL（从） show slave status; /*!查看该slave的状态，只有该slave执⾏命令start slave; 后才有显示记录*/

停⽌从服务器 stop slave; 重置从服务器 reset slave; 从服务器关联到主服务器 change master to master_user='rot', master_pasword='a19108', master_host='192.168.42.128',master_port=306, master_log_file='mysql-bin. 02',master_log_pos=519; 注意：上⾯的关联配置的信息都是主服务器上的，但是要在slave从服务器上配置，如果master_log_file和master_log_pos不知道可以 在master服务器（192。168.42.128）上使⽤show master status;查看

![image 8](assets/imageFile8.png)

执⾏成功！

开启从服务器start slave;

![image 9](assets/imageFile9.png)

查看从服务器连接状态show slave status\G /*!不加分号*/

![image 10](assets/imageFile10.png)

Sqlyog不⽀持\解析不了sql可以使⽤命令⾏来查看 使⽤账号：yingyue 进⾏远程登录到192.168.42.129 上

![image 11](assets/imageFile11.png)

全部信息如下：

![image 12](assets/imageFile12.png)

复制代码

Microsoft Windows [版本 6.1.7601]

版权所有 (c) 2009 Microsoft Corporation。保留所有权利。

C:\Users\Administrator>mysql -h192.168.42.129 -uyingyue -p000000

Welcome to the MySQL monitor. Commands end with ; or \g.

Your MySQL connection id is 20

Server version: 5.7.17-log Source distribution

Type 'help;' or '\h' for help. Type '\c' to clear the buffer.

mysql> show slave status\G

*************************** 1. row ***************************

Slave_IO_State: Waiting for master to send event

Master_Host: 192.168.42.128

Master_User: root

Master_Port: 3306

Connect_Retry: 60

Master_Log_File: mysql-bin.000002

Read_Master_Log_Pos: 519

Relay_Log_File: localhost-relay-bin.000002

Relay_Log_Pos: 320

Relay_Master_Log_File: mysql-bin.000002

Slave_IO_Running: Yes

Slave_SQL_Running: Yes

Replicate_Do_DB: test_db

Replicate_Ignore_DB:

Replicate_Do_Table:

Replicate_Ignore_Table:

Replicate_Wild_Do_Table:

Replicate_Wild_Ignore_Table:

Last_Errno: 0

Last_Error:

Skip_Counter: 0

Exec_Master_Log_Pos: 519

Relay_Log_Space: 531

Until_Condition: None

Until_Log_File:

Until_Log_Pos: 0

Master_SSL_Allowed: No

Master_SSL_CA_File:

Master_SSL_CA_Path:

Master_SSL_Cert:

Master_SSL_Cipher:

Master_SSL_Key:

Seconds_Behind_Master: 0

Master_SSL_Verify_Server_Cert: No

Last_IO_Errno: 0

Last_IO_Error:

Last_SQL_Errno: 0

Last_SQL_Error:

Replicate_Ignore_Server_Ids:

Master_Server_Id: 1

Master_UUID: a70b33cb-ff4a-11e6-9fdc-000c29589bdd

Master_Info_File: /usr/local/mysql-5.7.17/data/master.info

SQL_Delay: 0

SQL_Remaining_Delay: NULL

Slave_SQL_Running_State: Slave has read all relay log; waiting for more updates

Master_Retry_Count: 86400

Master_Bind:

Last_IO_Error_Timestamp:

Last_SQL_Error_Timestamp:

Master_SSL_Crl:

Master_SSL_Crlpath:

Retrieved_Gtid_Set:

Executed_Gtid_Set:

Auto_Position: 0

Replicate_Rewrite_DB:

Channel_Name:

Master_TLS_Version: row in set (0.00 sec)

mysql>

![image 13](assets/imageFile13.png)

复制代码

是否成功就看 Slave_IO_Runing: Yes Slave_SQL_Runing: Yes

是否都为Yes 如果有出现以上结果，则配置成功！

Slave_SQL_Running_State: Slave has read all relay log; waiting for more updates 表示：从服务器已经读到了所有的主服务器库的⼆进制⽇志，随时待命复制；

下⾯来测试⼀下主从复制 测试描述： DDL语句测试 在服务器192.168.42.128 的 test_db库中新建 country表，看看从服务器192.168.42.129的test_db中是否会⾃动同步复制 country表，如果可以，则表明主从复制成功； 执⾏ create table country(id int not null primary key auto_increment, name varchar(100));

![image 14](assets/imageFile14.png)

看看从服务器192.168.42.129 上从库test_db是否会⾃动同步country表

![image 15](assets/imageFile15.png)

没问题 下⾯来删除主192.68.42.128上主库test_db的country表

![image 16](assets/imageFile16.png)

看看192.168.42.129 上的从库的情况

![image 17](assets/imageFile17.png)

也是可以的，没问题，现在再重新创建表country 下⾯测试需要使⽤（在主库中创建）

![image 18](assets/imageFile18.png)

DML语句测试（包括insert， delete，update） 在192.168.42.128的主库test_db上执⾏insert into country values(1, '南⾮'),(2,'瑞典'),(3,'哥斯达黎加'),(4,'新⻄兰'),(5,'中 国'),(6,'希腊') ,(7,'意⼤利'),(8,'苏格兰') ,(9,'冰岛') ,(10,'委内瑞拉') ,(11,'⽐利时');

![image 19](assets/imageFile19.png)

查看192.168.42.129的test_db从库的country表是否多了11条记录

![image 20](assets/imageFile20.png)

没问题。 在192.168.42.128的主库test_db上执⾏update country set name='中华⼈⺠共和国' where id = 5;

![image 21](assets/imageFile21.png)

查看192.168.42.129的test_db从库的country表

![image 22](assets/imageFile22.png)

没问题。 在192.168.42.128的主库test_db上执⾏ delete from country where id = 11;

![image 23](assets/imageFile23.png)

查看192.168.42.129的test_db从库的country表，最后⼀个 ‘⽐利时’应该没了

![image 24](assets/imageFile24.png)

没问题。

读写分离（主库写， 从库读） 读写分离使⽤的中间件为mycat 安装mycat 过程略 官⽅参考⽂档：

![image 25](assets/imageFile25.png)

配置mycat环境变量 略： 注意防⽕墙开启8066 和9066 端⼝

![image 26](assets/imageFile26.png)

修改server.xml⽂件

![image 27](assets/imageFile27.png)

编辑server.xml⽂件（读写分离这些就够了，多了碍眼）

![image 28](assets/imageFile28.png)

复制代码

<?xml version="1.0" encoding="UTF-8"?>

<!-- - - Licensed under the Apache License, Version 2.0 (the "License");

- - you may not use this file except in compliance with the License. - You

may obtain a copy of the License at - - http://www.apache.org/licenses/LICENSE-2.0

- - - Unless required by applicable law or agreed to in writing, software -


distributed under the License is distributed on an "AS IS" BASIS, - WITHOUT

WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. - See the

License for the specific language governing permissions and - limitations

under the License. -->

<!DOCTYPE mycat:server SYSTEM "server.dtd">

<mycat:server xmlns:mycat="http://io.mycat/">

<system>

<property name="useSqlStat">1</property> <!-- 1为开启实时统计、0为关闭 -->

</system>

<user name="root">

<property name="password">a19911008</property>

<property name="schemas">TESTDB</property>

</user>

<user name="yingyue">

<property name="password">000000</property>

<property name="schemas">TESTDB</property>

</user>

</mycat:server>

![image 29](assets/imageFile29.png)

复制代码

配置schema.xml⽂件 vi conf/schema.xml

![image 30](assets/imageFile30.png)

复制代码

<?xml version="1.0"?>

<!DOCTYPE mycat:schema SYSTEM "schema.dtd">

<mycat:schema xmlns:mycat="http://io.mycat/">

<schema name="TESTDB" checkSQLschema="false" sqlMaxLimit="100" dataNode="dn1"></schema>

<dataNode name="dn1" dataHost="localhost1" database="test_db" />

<dataHost name="localhost1" maxCon="1000" minCon="10" balance="1"

writeType="0" dbType="mysql" dbDriver="native" switchType="1" slaveThreshold="100">

<heartbeat>select user()</heartbeat>

<!-- can have multi write hosts -->

<writeHost host="hostM1" url="192.168.42.128:3306" user="root" password="a19911008">

<!-- can have multi read hosts -->

<readHost host="hostS2" url="192.168.42.129:3306" user="yingyue" password="000000" />

</writeHost>

</dataHost>

</mycat:schema>

![image 31](assets/imageFile31.png)

复制代码

启动mycat 进⼊mycat安装怒⽬执⾏/bin/mycat start 或 /bin/mycat console

![image 32](assets/imageFile32.png)

如下启动完成

![image 33](assets/imageFile33.png)

使⽤mycat作为代理连接MySQL数据库

![image 34](assets/imageFile34.png)

yingyue是192.168.42.129上的MySQL⽤户为什么可以连接到192.168.42.128的MySQL呢？因为在刚才的mycat的server.xml中 配置了，相当于代理

参数解释 C:\Users\Administrator>mysql -h192.168.42.128 -uyingyue -p000000 -P8066 -DTESTDB 其中8066是mycat的监听端⼝，类似于mysql的3306端⼝，其中-u，-p，-h分别是⽤户名，密码和主机，-P是mycat端⼝ -D是连接 的逻辑库。⾄于为什么是这些，这个跟mycat的server.xml和schema.xml配置⽂件有关。 在命令⾏中执⾏explain select * from country;

![image 35](assets/imageFile35.png)

看到数据是从dn1节点查的。 插⼊⼀条数据

![image 36](assets/imageFile36.png)

下⾯来看看mycat监控⽇志

![image 37](assets/imageFile37.png)

从⽇志看到最终被插⼊到192.168.42.128的MySQL的test_db的country中 看⼀下主库是否有添加的数据

![image 38](assets/imageFile38.png)

果然有，说明读写分离成功! 然后从库192.168.42.129中的test_db会复制这条数据

![image 39](assets/imageFile39.png)

没问题的，读写分离也是成功的。

题外话：之前以为mycat可以实现主从库数据复制，原来是不可以的，看看下⾯，其实mycat就是代理实现了主从库的读写分离。总 结：主从复制与读写分离⽆关，主从复制是MySQL的，读写分离是Mycat的。

![image 40](assets/imageFile40.png)

-------------------------------------------------------------------------------------------------⾼级篇

MySQL的slave断了怎么办？ 如下图：

![image 41](assets/imageFile41.png)

这个错是我故意弄的，因为192.168.42.128上的test_db是很早前就建的库并且⾥⾯有account表，但是192.168.42.128上的 MySQL我今天重启过，每次重启⼆进制⽇志⽂件log-bin都不相同，在上次的⽂件版本上加⼀所以是mysql-bin.000002， 但是 192.168.42.129服务器上的MySQL的是我今天重新关联的，关联的是mysql-bin.000002这个⼆进制⽇志⽂件，这个⽂件中没有记录 account表的创建和插⼊数据记录。但是在我在主从复制正常的情况下在主服务器上将account删掉，这时删除的记录会记录到mysqlbin.000002的⼆进制⽇志⽂件中，然后slave服务器的MySQL会复制master的⽇志然后执⾏⽇志，slave上的test_db没有account表 删除肯定报错；

解决⽅案： mysql> stop slave; Query OK, 0 rows affected (0.01 sec) mysql> set GLOBAL SQL_SLAVE_SKIP_COUNTER=1;

- Query OK, 0 rows affected (0.00 sec) mysql> start slave;

- Query OK, 0 rows affected (0.01 sec)


![image 42](assets/imageFile42.png)

解释： set GLOBAL SQL_SLAVE_SKIP_COUNTER=1; 表示 跳过⼀个事物 查看从库及其⽇志，已经开始正常复制 说明：今天发现最后⼀步不⽤做，设置完set GLOBAL SQL_SLAVE_SKIP_COUNTER=1;后，开启slave后，该值⾃动变为0 了。

--------------------------------------------------------------------------------------------------

---延伸：

# mysql主从错误断开 怎样恢复

mysql主从同步常⻅异常及恢复⽅法

- 1. ⼀般的异常只需要跳过⼀步即可恢复 mysql> stop slave; mysql> set GLOBAL SQL_SLAVE_SKIP_COUNTER=1;


- mysql> slave start;
- 2.断电导致主从不能同步时，通主库的最后⼀个bin-log⽇志进⾏恢复 在主库服务器上，mysqlbinlog mysql-bin.xxxx > binxxxx.txt tail -n 100000 binxxxx.txt > tail-binxxxx.txt vim tail-binxxxx.txt 打开tail-binxxxx.txt⽂件找到最后⼀个postion值 然后在从库上，change host to 相应正确的值 mysql> stop slave; mysql> change master to master_host='ip', master_user='username', master_password='password', master_log_file='mysql-bin.xxxx', master_log_pos=xxxx; mysql> slave start; mysql> show slave status\G

- 3.主键冲突、表已存在等错误代码如1062,1032,1060等，可以在mysql主配置⽂件my.cnf或my.ini⽂件指定 略过此类异常并继续下条sql同步，这样也可以避免很多主从同步的异常中断 [mysqld] slave-skip-errors = 1062,1032,1060


------------------------------------从理想⻆度看，主从数据库应该⽆故障的运转下去，可以有时候还是会出现⼀些莫名其妙的问题，⽐如说即便从未在从服务器上⼿动更 新过数据，但还是可能遇到“Error: 1062 Duplicate entry”错误，具体原因不详，可能是MySQL本身的问题。遇到这类问题的时候， 从服务器会停⽌复制操作，我们只能⼿动解决问题，具体的操作步骤如下： mysql> stop slave; mysql> set global sql_slave_skip_counter = 1; mysql> start slave;

同样的操作可能需要进⾏多次，也可以设置⾃动处理此类操作，格式：slave-skip-errors = 错误代码 在从服务器的my.cnf⾥设置： slave-skip-errors = 1062
