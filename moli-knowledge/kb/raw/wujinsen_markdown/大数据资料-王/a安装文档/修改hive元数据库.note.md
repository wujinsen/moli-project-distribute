⼀、卸载掉原有mysql

- 1、[rot@xiaoluo ~]# rpm -qa | grep mysql / 这个命令就会查看该操作系统上是否已经安装了 mysql数据库
- 2、[rot@xiaoluo ~]# rpm -e-nodeps mysql / 强⼒删除模式，如果使⽤上⾯命令删除时，提示 有依赖的其它⽂件，则⽤该命令可以对其进⾏强⼒删除 使⽤rpm -e 卸载mysql，但是卸载时出现两个问题： 问题⼀：#rpm -e mysql-server-5.0.7-4.e15_4.2eror: package mysql-server-5.0.7-4.e15_4.2 is not instaled解决：rpm -e mysql-server-5.0.7（这个原因很奇怪，估计是后⾯的⼀串数字不是表示 版本号的 …）问题⼆：dependencies问题#rpm -e mysql-server-5.0.7eror：Failed dependencies：解决：在卸载命令后加参数以不考虑dependencies，问题解决#rpm -e mysqlserver-5.0.7-nodeps
- 3、查看是否卸载成功： rpm -qa | grep mysql ⼆、通过yum来进⾏mysql的安装


1 可以看到安装的mysql，如：mysql-server-5.0.77-4.el5_4.2

- 1、[rot@xiaoluo ~]# yum list | grep mysql /查看yum上提供的mysql数据库可下载的版本

- 2、[rot@xiaoluo ~]# yum instal -y mysql-server mysql mysql-deve 在等待了⼀番时间后，yum会帮我们选择好安装mysql数据库所需要的软件以及其它附属的⼀些软件


我们发现，通过yum⽅式安装mysql数据库省去了很多没必要的麻烦，当出现下⾯的结果时，就代表 mysql数据库安装成功了

![image 1](<修改hive元数据库.note_images/imageFile1.png>)

- 3、[rot@xiaoluo ~]# rpm -qi mysql-server /查看刚安装好的mysql-server的版本 ⾄此我们的mysql数据库已经安装完成了。 三、启动mysql数据库的初始化及相关配置


- 1、[rot@xiaoluo ~]# service mysqld start /启动我们的mysql服务 初始化 MySQL 数据库： WARNING: The host 'xiaoluo' could not be l oked up with resolveip.This probably means that your libc libraries are not 10 % compatiblewith this binary MySQL version. The MySQL daemon, mysqld, should worknormaly with the exception that host name resolving wil not work.This means that you should use IP adreses instead of hostnameswhen specifying MySQL privileges !Instaling MySQL system tables.OKFiling help tables.OKTo start mysqld at bot time you have to copysuport-files/mysql.server to the right place for your systemPLEASE REMEMBER TO SET A PASWORD FOR THE MySQL rot USER !To do so, start the server, then isue the folowing comands:/usr/bin/mysqladmin -u rot pasword 'newpasword'/usr/bin/mysqladmin -u rot -h xiaoluo pasword 'new-pasword'Alternatively you can run:/usr/bin/mysql_secure_instalationwhich wil also give you the option of removing the testdatabases and anonymous user created by default. This istrongly recomended for production servers.Se the manual for more instructions.You can start the MySQL daemon with:cd /usr ; /usr/bin/mysqld_safe &You can test the MySQL daemon with mysql-test-run.plcd /usr/mysqltest ; perl mysql-test-run.plPlease report any problems with the /usr/bin/mysqlbug script! [确定]正在启动 mysqld： [确定]

- 2、[rot@xiaoluo ~]# service mysqld restart/这时我们会看到第⼀次启动mysql服务器以后会提⽰ ⾮常多的信息，⽬的就是对mysql数据库进⾏初始化操作，

//当我们再次重新启动mysql服务时，就不会提⽰这么多信息了停⽌ mysqld： [确定]正在启动 mysqld： [确定]

- 3、[rot@xiaoluo ~]# chkconfig-list | grep mysqld/查看mysql服务是不是开机⾃动启动mysqld 0:关闭 1:关闭 2:关闭 3:关闭 4:关闭 5:关闭 6:关闭

- 4、[rot@xiaoluo ~]# chkconfig mysqld on /设置开机⾃动启动5、[rot@xiaoluo ~]# chkconfiglist | grep mysqlmysqld 0:关闭 1:关闭 2:启⽤ 3:启⽤ 4:启⽤ 5:启⽤ 6:关闭


mysql数据库安装完以后只会有⼀个rot管理员账号，但是此时的rot账号还并没有为其设置密码， 在第⼀次启动mysql服务时，会进⾏数据库的⼀些初始化⼯作，在输出的⼀⼤串信息中，我们看到有这 样⼀⾏信息 ： /usr/bin/mysqladmin -u rot pasword 'new-pasword' / 为rot账号设置密码 所以我们可以通过 该命令来给我们的rot账号设置密码(注意：这个rot账号是mysql的rot账号，⾮ Linux的rot账号)

- 6、[rot@xiaoluo ~]# mysqladmin -u rot pasword 'rot' / 通过该命令给rot账号设置密码为 rot 此时我们就可以通过 mysql -u rot -p 命令来登录我们的mysql数据库了
- 7、开启 306端⼝： 修改/etc/sysconfig/iptables，如何写，参考已开端⼝，或者按照上⾯的命令填写即可
- 8、重新启动iptables service iptables restart
- 9、rot第⼀次不允许远程访问，必须授权。⽤户开启远程访问： GRANT AL PRIVILEGES ON.* TO 'rot'@'%' IDENTIFIED BY 'rot' WITH GRANT OPTION;
- 10、关闭iptables防⽕墙：


![image 2](<修改hive元数据库.note_images/imageFile2.png>)

- （1） 重启后永久性⽣效： 开启：chkconfig iptables on关闭：chkconfig iptables of
- （2） 即时⽣效，重启后失效： 开启：service iptables start 关闭：service iptables stop 需要说明的是对于Linux下的其它服务都可以⽤以上命令执⾏开启和关闭操作。


在开启了防⽕墙时，做如下设置，开启相关端⼝， 修改/etc/sysconfig/iptables ⽂件，添加以下内 容：

- -A RH-Firewal-1-INPUT -m state ⸺state NEW -m tcp -p tcp ⸺dport 80 -j ACEPT
- -A RH-Firewal-1-INPUT -m state ⸺state NEW -m tcp -p tcp ⸺dport 2 -j ACEPT或者： /etc/init.d/iptables status 会得到⼀系列信息，说明防⽕墙开着。


/etc/rc.d/init.d/iptables stop 关闭防⽕墙

最后： 在根⽤户下输⼊setup，进⼊⼀个图形界⾯，选择Firewal configuration，进⼊下⼀界⾯，选 择Security Level为Disabled，保存。重启即可。

1、查看SELinux状态：

- 1）、/usr/sbin/sestatus -v #如果SELinux status参数为enabled即为开启状态 SELinux status: enabled
- 2）、getenforce #也可以⽤这个命令检查


12、关闭SELinux：

- 1、临时关闭（不⽤重启机器）： setenforce 0 #设置SELinux 成为permisive模式

#setenforce 1 设置SELinux 成为enforcing模式

- 2、修改配置⽂件需要重启机器： 修改/etc/selinux/config ⽂件 将SELINUX=enforcing改为SELINUX=disabled


重启机器即可 结果如上所示，Linux系统监听的 306端⼝号就是我们的mysql数据库！！！！ 值得⼀提的是，我的安装过程都是源码包编译安装的，并且所有的配置与数据等都统⼀规划到 了/opt/mysql⽬录中， 因此在⼀台服务器上安装完成以后，可以将整个mysql⽬录打包，然后传到其它服务器上解包，便可⽴ 即使⽤。

连接： Mysql–u rot 创建hive账户：Mysql>CREATE USER 'hive' IDENTIFIED BY 'hive';

Mysql> GRANT ALL PRIVILEGES ON*.* TO Identified by 'hive';

'hive'@'%'

GRANT ALL PRIVILEGES ON*.* TO Identified by 'hive';

'hive'@'localhost'

GRANT ALL PRIVILEGES ON*.* TO Identified by 'hive'; Mysql>flush privileges; Mysql>

'hive'@'127.0.0.1'

创建数据库：Mysql>create database hive_metastore;

2. Hive中配置mysql连接 ⾸先把mysql-conector-java-5.1.12.jar拷⻉到/home/hadop/hive-0.6.0-bin/lib下

再修改hive-default.xml配置

<property> <name>javax.jdo.option.ConectionURL</name> <value>jdbc:mysql:/localhost: 306/hive_metastore?createDatabaseIfNotExist=true</value> <description>JDBC conect string for a JDBC metastore</description> </property> <property> <name>javax.jdo.option.ConectionDriverName</name> <value>com.mysql.jdbc.Driver</value> <description>Driver clas name for a JDBC metastore</description> </property> <property> <name>javax.jdo.option.ConectionUserName</name> <value>hive</value> <description>username to use against metastore database</description> </property> <property> <name>javax.jdo.option.ConectionPasword</name> <value>hive</value> <description>pasword to use against metastore database</description> </property> 保存退出，连接mysql配置完成

