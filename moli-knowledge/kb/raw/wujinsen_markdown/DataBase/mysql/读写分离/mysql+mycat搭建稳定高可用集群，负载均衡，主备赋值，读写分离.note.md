主要思路 测试环境 实现mysql主备复制

配置A主mysql 配置B备mysql 验证同步配置结果

验证是否同步

关闭B备mysql的同步，验证读写分离

实现读写分离

安装mycat 配置mycat 启动mycat 测试读写分离

验证是否同步

关闭B备mysql的同步，验证读写分离

数据库性能优化普遍采⽤集群⽅式，oracle集群软硬件投⼊昂贵，今天花了⼀天时间搭建基于mysql的集群环境。 主要思路 简单说，实现mysql主备复制-->利⽤mycat实现负载均衡。 ⽐较了常⽤的读写分离⽅式，推荐mycat，社区活跃，性能稳定。 测试环境 MYSQL版本：Server version: 5.5.53，到官⽹可以下载WINDWOS安装包。 注意：确保mysql版本为5.5以后，以前版本主备同步配置⽅式不同。 linux实现思路类似，修改my.cnf即可。

- A主mysql。192.168.110.1:3306, ⽤户root，密码root。操作系统：win7 x64，内存：4g

- B备mysql。192.168.110.2:3306, ⽤户root，密码root。操作系统：win2003 x64，内存：1g


安装路径：C:\Program Files\MySQL\MySQL Server 5.5\bin

安装路径：C:\Program Files\MySQL\MySQL Server 5.5\bin

A主、B备的mysql中创建sync_test数据库

实现mysql主备复制 主要思路：A主mysql开启⽇志，B备mysql读取操作⽇志，同步执⾏。 ⼀般为主备同步，主主同步不推荐使⽤。 配置A主mysql

- 1）修改my.ini。需要在log-bin="C:/Program Files/MySQL/MySQL Server 5.5/log/mysql-bin.log"的相关位置创建log⽬录，以及mysql-bin.log ⽂件。 [mysqld]

- server-id=1 #主机标示，整数 port=3306 log-bin="C:/Program Files/MySQL/MySQL Server 5.5/log/mysql-bin.log" #确保此⽂件可写 read-only=0 #主机，读写都可以 binlog-do-db=sync_test #需要备份数据库，多个写多⾏ binlog-ignore-db=mysql #不需要备份的数据库，多个写多⾏


- 2）允许MYSQL远程访问


#登录mysql console 进⼊%home%/bin，执⾏mysql -uroot -proot #授权。允许root⽤户，从192.168.110.*的IP范围 远程访问A主mysql mysql>GRANT ALL PRIVILEGES ON *.* TO 'root'@'192.168.110.*' IDENTIFIED BY 'root' WITH GRANT OPTION; #⽣效。该操作很重要！ mysql>FLUSH PRIVILEGES;

- 3）重启A主mysql数据库 进⼊%home%/bin，执⾏mysql -uroot -proot mysql>net stop mysql; mysql>net start mysql;

- 4）查看主mysql⽇志状态


mysql> show master status\G;

*************************** 1. row ***************************

File: mysql-bin.000003 Position: 107

Binlog_Do_DB: sync_test

Binlog_Ignore_DB: mysql 1 row in set (0.00 sec)

ERROR: No query specified 配置B备mysql

- 1）修改my.ini。需要在log-bin="C:/Program Files/MySQL/MySQL Server 5.5/log/mysql-bin.log"的相关位置创建log⽬录，以及mysql-bin.log ⽂件。 [mysqld] # add for sycn test

server-id=2 #从机标识 log-bin="C:/Program Files/MySQL/MySQL Server 5.5/log/mysql-bin.log" #确保此⽂件可写 #master-host="192.168.110.1" #主机Ip #master-user=root #数据库访问⽤户名 #master-pass=root #数据库访问密码 #master-port=3306 #主机端⼝ #master-connect-retry=60 #如果从服务器发现主服务器断掉，重新连接的时间差(秒) replicate-do-db=sync_test #只复制某个库 replicate-ignore-db=mysql #不复制某个库

- 2）重启B备mysql数据库 进⼊%home%/bin，执⾏mysql -uroot -proot mysql>net stop mysql; mysql>net start mysql;

- 3）配置B备数据库的数据来源，核实⾼亮处的状态是否正常。


mysql>change master to master_host='192.168.110.1',master_port='3306',master_user='root',master_password='root'; mysql>slave start; mysql>show slave status\G;

*************************** 1. row ***************************

Slave_IO_State: Waiting for master to send even Master_Host: 192.168.110.1 Master_User: root Master_Port: 3306

Connect_Retry: 60

Master_Log_File: mysql-bin.000003 Read_Master_Log_Pos: 107

Relay_Log_File: wjt-1c698d8a032-relay-bin.00001 Relay_Log_Pos: 253 Relay_Master_Log_File: mysql-bin.000003

Slave_IO_Running: Yes Slave_SQL_Running: Yes

Replicate_Do_DB: sync_test Replicate_Ignore_DB: mysql

Replicate_Do_Table: Replicate_Ignore_Table:

Replicate_Wild_Do_Table:

Replicate_Wild_Ignore_Table: Last_Errno: 0 Last_Error: Skip_Counter: 0

Exec_Master_Log_Pos: 107 Relay_Log_Space: 565 Until_Condition: None

Until_Log_File:

Until_Log_Pos: 0 Master_SSL_Allowed: No Master_SSL_CA_File: Master_SSL_CA_Path:

Master_SSL_Cert: Master_SSL_Cipher:

Master_SSL_Key: Seconds_Behind_Master: 0

Master_SSL_Verify_Server_Cert: No Last_IO_Errno: 0 Last_IO_Error:

Last_SQL_Errno: 0 Last_SQL_Error:

Replicate_Ignore_Server_Ids:

Master_Server_Id: 1 1 row in set (0.00 sec)

ERROR: No query specified 验证同步配置结果

A主mysql：使⽤navicat⼯具，在sync_test库中创建sync_table表，并添加⼀些数据 B备mysql：使⽤navicat⼯具，查看sync_test库，可以看到sync_table表和数据已被同步

实现读写分离 主要思路：使⽤mycat中间件，转发sql指令到后端mysql节点。mycat不负责数据库同步。 安装mycat mycat是什么？可以认为它是⼀个数据库访问中间件，但更像f5、ngnix等产品，具备访问路由、多表分表分⽚操作等功能。总之很强⼤。

下载：http://www.mycat.io/ ，本⽂使⽤的是：1.6-RELEASE 解压Mycat-server-1.6-RELEASE-20161012170031-win.tar，到D:\dev-bin\mycat⽬录 确保java环境为jdk1.7以上，否则mycat将不⽀持

安装完毕 配置mycat

- 1）server.xml。配置访问⽤户及权限。修改⾼亮处信息，其中admin、user为访问mycat的⽤户，TESTDB为mycat虚拟的数据库，供上层应 ⽤访问。 <user name="admin">

<property name="password">admin</property> <property name="schemas">TESTDB</property> <!-- 表级 DML 权限设置 --> <!-<privileges check="false">

<schema name="TESTDB" dml="0110" >

- <table name="tb01" dml="0000"></table>

- <table name="tb02" dml="1111"></table>


</schema> </privileges>

--> </user> <user name="user">

<property name="password">user</property> <property name="schemas">TESTDB</property> <property name="readOnly">true</property>

</user>

- 2）schema.xml。这部分不太好理解，精简了⼀下，主要分schema、dataNode、dataHost三个主要配置。 <scheme>节点定义了mycat的虚拟数据库为TESTDB， balance="1"：write操作路由到A机，读操作路由到B。 <?xml version="1.0"?> <!DOCTYPE mycat:schema SYSTEM "schema.dtd"> <mycat:schema xmlns:mycat="http://io.mycat/">


<schema name="TESTDB" checkSQLschema="false" sqlMaxLimit="100" dataNode="dn1">

<!-- 这⾥不配置，代表所有的表分⽚到dn1节点--> </schema> <dataNode name="dn1" dataHost="localhost1" database="sync_test" /> <dataHost name="localhost1" maxCon="1000" minCon="10" balance="1"

writeType="0" dbType="mysql" dbDriver="native" switchType="1" slaveThreshold="100"> <heartbeat>select user()</heartbeat> <!-- can have multi write hosts --> <writeHost host="hostM1" url="192.168.110.1:3306" user="root" password="root">

<!-- can have multi read hosts --> <readHost host="hostS2" url="192.168.110.2:3306" user="root" password="root" />

</writeHost>

</dataHost> </mycat:schema> 启动mycat 1）启动mycat D:\dev-bin\mycat\bin>startup_nowrap.bat 后台信息如下： D:\dev-bin\mycat\bin>startup_nowrap.bat D:\dev-bin\mycat\bin>REM check JAVA_HOME & java D:\dev-bin\mycat\bin>set "JAVA_CMD=C:\Program Files (x86)\Java\jdk1.7.0_13/bin/java" D:\dev-bin\mycat\bin>if "C:\Program Files (x86)\Java\jdk1.7.0_13" == "" goto noJavaHome D:\dev-bin\mycat\bin>if exist "C:\Program Files (x86)\Java\jdk1.7.0_13\bin\java.exe" goto mainEntry D:\dev-bin\mycat\bin>REM set HOME_DIR D:\dev-bin\mycat\bin>set "CURR_DIR=D:\dev-bin\mycat\bin" D:\dev-bin\mycat\bin>cd .. D:\dev-bin\mycat>set "MYCAT_HOME=D:\dev-bin\mycat" D:\dev-bin\mycat>cd D:\dev-bin\mycat\bin #如果启动失败，请修改D:\dev-bin\mycat\bin\startup_nowrap.bat⽂件中的以下参数。默认占⽤内存为2G D:\dev-bin\mycat\bin>"C:\Program Files (x86)\Java\jdk1.7.0_13/bin/java" -server -Xms512m -Xmx512m XX:MaxPermSize=64M -XX:+AggressiveOpts -XX:MaxDirectMemorySize=768m -DMYCAT_HOME=D:\

p "..\conf;..\lib\*" io.mycat.MycatStartup MyCAT Server startup successfully. see logs in logs/mycat.log #启动成功将看到如下信息。

注意：如⽇志中出现192.168.110.2 not connected 等信息，请允许B备mysql远程访问。

#登录mysql console 进⼊%home%/bin，执⾏mysql -uroot -proot #授权。允许root⽤户，从192.168.110.*的IP范围 远程访问Bmysql mysql>GRANT ALL PRIVILEGES ON *.* TO 'root'@'192.168.110.*' IDENTIFIED BY 'root' WITH GRANT OPTION; #⽣效,该操作很重要！ mysql>FLUSH PRIVILEGES;

测试读写分离 验证是否同步

使⽤navicat连接mycat，操作⽅式和连接物理mysql库⼀致，⽤户admin，密码admin，端⼝8066 在TESTDB虚拟库中，创建新表test2，增加⼀些数据 查看A节点、B节点数据已同步

关闭B备mysql的同步，验证读写分离 mysql> slave stop; Query OK, 0 rows affected (0.00 sec)

mysql> show slave status\G;

*************************** 1. row *************************** Slave_IO_State: Master_Host: 192.168.110.1

Master_User: root Master_Port: 3306

Connect_Retry: 60

Master_Log_File: mysql-bin.000003 Read_Master_Log_Pos: 478

Relay_Log_File: wjt-1c698d8a032-relay-bin.00001 Relay_Log_Pos: 624 Relay_Master_Log_File: mysql-bin.000003

Slave_IO_Running: No Slave_SQL_Running: No

Replicate_Do_DB: sync_test Replicate_Ignore_DB: mysql

Replicate_Do_Table: Replicate_Ignore_Table:

Replicate_Wild_Do_Table:

Replicate_Wild_Ignore_Table: Last_Errno: 0 Last_Error: Skip_Counter: 0

Exec_Master_Log_Pos: 478 Relay_Log_Space: 936 Until_Condition: None

Until_Log_File: Until_Log_Pos: 0 Master_SSL_Allowed: No

Master_SSL_CA_File: Master_SSL_CA_Path:

Master_SSL_Cert: Master_SSL_Cipher:

Master_SSL_Key: Seconds_Behind_Master: NULL

Master_SSL_Verify_Server_Cert: No Last_IO_Errno: 0 Last_IO_Error:

Last_SQL_Errno: 0 Last_SQL_Error:

Replicate_Ignore_Server_Ids:

Master_Server_Id: 1 1 row in set (0.00 sec)

ERROR: No query specified

- 3）使⽤navicat连接mycat，操作⽅式和连接物理mysql库⼀致，⽤户admin，密码admin，端⼝8066


连接成功后，将看到TESTDB数据库和test数据表

在test表中添加⼀些数据，保存 执⾏select * from test查看test操作，将看到数据未更新

原因：mycat将查询sq路由到B，因此读取的结果集不⼀致。 最后，mycat使⽤可以参⻅官⽹的权威指南学习。双主双备架构待后续更新。

crazyacking

An IT developer focusing on server development. A strong advocate of open source. | | | | |

Github Facebook Twitter Google+

