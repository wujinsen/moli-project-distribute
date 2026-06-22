⼀、卸载掉原有mysql

- 1、[rot@xiaoluo ~]# rpm -qa | grep mysql / 这个命令就会查看该操作系统上是否已经安装了 mysql数据库
- 2、[rot@xiaoluo ~]# rpm -e-nodeps mysql / 强⼒删除模式，如果使⽤上⾯命令删除时，提示 有依赖的其它⽂件，则⽤该命令可以对其进⾏强⼒删除

问题⼀：#rpm -e mysql-server-5.0.7-4.e15_4.2eror: package mysql-server-5.0.7-4.e15_4.2 is not instaled解决：rpm -e mysql-server-5.0.7（这个原因很奇怪，估计是后⾯的⼀串数字不是表示 版本号的 …）问题⼆：dependencies问题#rpm -e mysql-server-5.0.7eror：Failed dependencies：解决：在卸载命令后加参数以不考虑dependencies，问题解决#rpm -e mysqlserver-5.0.7-nodeps

- 3、查看是否卸载成功： rpm -qa | grep mysql ⼆、通过yum来进⾏mysql的安装


1 可以看到安装的mysql，如：mysql-server-5.0.77-4.el5_4.2

1 使⽤rpm -e 卸载mysql，但是卸载时出现两个问题：

- 1、[rot@xiaoluo ~]# yum list | grep mysql /查看yum上提供的mysql数据库可下载的版本

- 2、[rot@xiaoluo ~]# yum instal -y mysql-server mysql mysql-deve 在等待了⼀番时间后，yum会帮我们选择好安装mysql数据库所需要的软件以及其它附属的⼀些软件


我们发现，通过yum⽅式安装mysql数据库省去了很多没必要的麻烦，当出现下⾯的结果时，就代表 mysql数据库安装成功了

![image 1](<MySQL主从安装文档（ok）.note_images/imageFile1.png>)

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


![image 2](<MySQL主从安装文档（ok）.note_images/imageFile2.png>)

- 10、关闭iptables防⽕墙：


- （1） 重启后永久性⽣效： 开启：chkconfig iptables on关闭：chkconfig iptables of
- （2） 即时⽣效，重启后失效： 开启：service iptables start 关闭：service iptables stop 需要说明的是对于Linux下的其它服务都可以⽤以上命令执⾏开启和关闭操作。


在开启了防⽕墙时，做如下设置，开启相关端⼝， 修改/etc/sysconfig/iptables ⽂件，添加以下内 容：

- -A RH-Firewal-1-INPUT -m state ⸺state NEW -m tcp -p tcp ⸺dport 80 -j ACEPT
- -A RH-Firewal-1-INPUT -m state ⸺state NEW -m tcp -p tcp ⸺dport 2 -j ACEPT或者： /etc/init.d/iptables status 会得到⼀系列信息，说明防⽕墙开着。


/etc/rc.d/init.d/iptables stop 关闭防⽕墙 最后： 在根⽤户下输⼊setup，进⼊⼀个图形界⾯，选择Firewal configuration，进⼊下⼀界⾯，选

择Security Level为Disabled，保存。重启即可。

- 1、查看SELinux状态：


- 1）、/usr/sbin/sestatus -v #如果SELinux status参数为enabled即为开启状态 SELinux status: enabled
- 2）、getenforce #也可以⽤这个命令检查


12、关闭SELinux：

- 1、临时关闭（不⽤重启机器）： setenforce 0 #设置SELinux 成为permisive模式

#setenforce 1 设置SELinux 成为enforcing模式

- 2、修改配置⽂件需要重启机器： 修改/etc/selinux/config ⽂件 将SELINUX=enforcing改为SELINUX=disabled


重启机器即可 结果如上所示，Linux系统监听的 306端⼝号就是我们的mysql数据库！！！！ 值得⼀提的是，我的安装过程都是源码包编译安装的，并且所有的配置与数据等都统⼀规划到 了/opt/mysql⽬录中， 因此在⼀台服务器上安装完成以后，可以将整个mysql⽬录打包，然后传到其它服务器上解包，便可⽴ 即使⽤。 四、mysql数据库的主要配置⽂件

- 1./etc/my.cnf 这是mysql的主配置⽂件 我们可以查看⼀下这个⽂件的⼀些信息 [rot@xiaoluo etc]# ls my.cnf


- my.cnf[rot@xiaoluo etc]# cat my.cnf [mysqld]datadir=/var/lib/mysqlsocket=/var/lib/mysql/mysql.sockuser=mysql# Disabling symboliclinks is recomended to prevent asorted security risksymbolic-links=0[mysqld_safe]logeror=/var/log/mysqld.logpid-file=/var/run/mysqld/mysqld.pid
- 2./ver/lib/mysql mysql数据库的数据库⽂件存放位置 我们的mysql数据库的数据库⽂件通常是存放在了/ver/lib/mysql这个⽬录下 [rot@xiaoluo ~]# cd /var/lib/mysql/ [rot@xiaoluo mysql]# ls -l总⽤量 2048-rw-rw -. 1 mysql mysql 10485760 4⽉ 6 2 01 ibdata1-rw-rw -. 1 mysql mysql 524280 4⽉ 6 2 01 ib_logfile0-rw-rw -. 1 mysql mysql 524280 4⽉ 6 21 59 ib_logfile1drwx -. 2 mysql mysql 4096 4⽉ 6 21 59 mysql / 这两 个是mysql数据库安装时默认的两个数据库⽂件srwxrwxrwx. 1 mysql mysql 0 4⽉ 6 2 01 mysql.sockdrwx -. 2 mysql mysql 4096 4⽉ 6 21 59 test / 这两个是mysql数据库安装时 默认的两个数据库⽂件 我们可以⾃⼰创建⼀个数据库，来验证⼀下该数据库⽂件的存放位置 创建⼀个我们⾃⼰的数据库： mysql> create database xiaoluo;Query OK, 1 row afected (0.0 sec)[rot@xiaoluo mysql]# ls -l总 ⽤量 20492-rw-rw -. 1 mysql mysql 10485760 4⽉ 6 2 01 ibdata1-rw-rw -. 1 mysql mysql 524280 4⽉ 6 2 01 ib_logfile0-rw-rw -. 1 mysql mysql 524280 4⽉ 6 21 59 ib_logfile1drwx -. 2 mysql mysql 4096 4⽉ 6 21 59 mysqlsrwxrwxrwx. 1 mysql mysql 0 4⽉ 6 2 01 mysql.sockdrwx -. 2 mysql mysql 4096 4⽉ 6 21 59 testdrwx -. 2 mysql mysql 4096 4⽉ 6 2 15 xiaoluo / 这个就是我们刚⾃⼰创建的xiaoluo数据库[rot@xiaoluo mysql]# cd xiaoluo/[rot@xiaoluo xiaoluo]# lsdb.opt
- 3./var/log mysql数据库的⽇志输出存放位置 我们的mysql数据库的⼀些⽇志输出存放位置都是在/var/log这个⽬录下 [rot@xiaoluo xiaoluo]# cd [rot@xiaoluo ~]# cd /var/log[rot@xiaoluo log]# lsamanda cron mailog-2013031 spice-vdagent.loganaconda.ifcfg.log cron-2013031mcelog spoleranaconda.log cups mesages spoler-2013031anaconda.program.log dirsrv mesages2013031 sdanaconda.storage.log dmesg mysqld.log talyloganaconda.syslog dmesg.old ntpstats tomcat6anaconda.xlog dracut.log piranha wpa_suplicant.loganaconda.yum.log gdm pm-powersave.logwtmpaudit htpd


p Xorg.0.logbot.log ibacm.log prelink Xorg.0.log.oldbtmp lastlog sa Xorg.1.logbtmp-20130401 libvirt samba Xorg.2.logcluster luci secure Xorg.9.logConsoleKit mailog secure-2013031 yum.log 其中mysqld.log 这个⽂件就是我们存放我们跟mysql数据库进⾏操作⽽产⽣的⼀些⽇志信息，通过查看 该⽇志⽂件，我们可以从中获得很多信息

因为我们的mysql数据库是可以通过⽹络访问的，并不是⼀个单机版数据库，其中使⽤的协议是 tcp/ip 协议，我们都知道mysql数据库绑定的端⼝号是 306 ，所以我们可以通过 netstat -anp 命令来查看⼀ 下，Linux系统是否在监听 306 这个端⼝号：

![image 3](<MySQL主从安装文档（ok）.note_images/imageFile3.png>)

五、MySQL主从复制 原理：

- 1、binlog：记录 dl、dml的句⼦，slave数据库拿到master数据库的binlog，就可以⽣成表，并 且插⼊数据
- 2、master端写binlog，每⼀步都会记录在binlog中，通过file和position告知slave上次读取到哪 ⾥。

file：mysql-bin. 01第⼏个⽂件， position：106，记录第⼏⾏

- 3、slave端读取binlog并分析，做数据库修改


mysql主从复制配置.docx 19.76KB

# 配置步骤1、配置主库：

⾸先在主数据库上建⽴存储需要的表，表均为i nodb引擎

修改/etc/my.cnf，在mysqld之后加。Mysqld_safe之前.

<table>
  <tr>
    <th>server-id=1</th>
  </tr>
</table>


log-bin=mysql-bin

- 2、重新启动主数据库 [rot@xiaoluo ~]# service mysqld restart3、进⼊主数据库，对主数据库进⾏复制⽤户添加，并设 置权限 mysql -u rot -p


<table>
  <tr>
    <th>mysql>GRANT REPLICATION SLAVE ON.* TO 'rep'@'%' IDENTIFIED BY 'rep'; mysql>flush privileges;</th>
  </tr>
</table>


mysql>show master status

注：本例使⽤rep/rep作为复制⽤户

- 4、查看主数据库复制状态,并记录file和position
- 5、配置从库：
- 6、重新启动从数据库 [rot@xiaoluo ~]# service mysqld restart
- 7、进⼊从数据库，并关闭之前可能有的slave复制 mysql -u root -p
- 8、配置与主数据库的复制


<table>
  <tr>
    <th> </th>
  </tr>
</table>


show master status

⾸先在从数据库上建⽴存储需要的表，表均为memory引擎（表结构与主数据库完全⼀致），并 且将2个event和所执⾏的存储过程加⼊到该数据库

修改/etc/my.cnf在mysqld之后加。Mysqld_safe之前.

<table>
  <tr>
    <th> </th>
  </tr>
</table>


server-id=2

<table>
  <tr>
    <th> </th>
  </tr>
</table>


mysql>stop slave;

<table>
  <tr>
    <th>mysql>CHANGE MASTER TO ST HOST='195.170.10.10', #主库ip SERUSER='rep', #主库复制⽤户的⽤户名<br><br>AS RPASWORD='rep', #密码 E_L _FILE'mysql-bin. 05', #show master status查询出来的值 查询出来的值</th>
  </tr>
  <tr>
    <td>MASTER_LOG_POS=107; #show master status</td>
  </tr>
  <tr>
    <td> </td>
  </tr>
</table>


- 9、启动从数据库，并启动从数据库的复制
- 10、查看从数据库的复制状态,查看从的状态，即⽇志，从中可以看出错误信息


mysql>start slave;

<table>
  <tr>
    <th>mysql>show slave status\G</th>
  </tr>
  <tr>
    <td> </td>
  </tr>
</table>


存储程序链接主数据库，监控应⽤链接从数据库即可。

测试：在master上创建表，在slave端就会同步。Master端创建表成功后，修改slave表的表类型： memery

结果：master创建表成功，并插⼊数据，从库会同步

错误：如果不同步，查看slave⽇志mysql>show slave status\G 可能是设置的rep⽤户权限不够，授权即可。

GRANTALPRIVILEGESON*.* TO'rep'@'%'IDENTIFIED BY'rep'WITHGRANTOPTION;

六、MySQL读写分离（还为实践）

Amoeba搞定mysql主从读写分离.pdf 706.17KB

场景描述： 数据库Master主服务器：192.168.10.130 数据库Slave从服务器：192.168.10.131 MySQL-Proxy调度服务器：192.168.10.132 以下操作，均是在192.168.10.132即MySQL-Proxy调度服务器 上进⾏的。

- 3.1 MySQL的安装与配置 具体的安装过程与上⽂相同。


- 3.2 检查系统所需软件包 通过 rpm -qa | grep name 的⽅式验证以下软件包是否已全部安装。 gc* gc-c+* autoconf* automake* zlib* libxml* ncurses-devel* libmcrypt* libtol* flex* pkgconfig* libevent* glib* 若缺少相关的软件包，可通过yum -y instal⽅式在线安装，或直接从系统安装光盘中找到并通过rpm ivh⽅式安装。
- 3.3 编译安装lua MySQL-Proxy的读写分离主要是通过rw-spliting.lua脚本实现的，因此需要安装lua。 lua可通过以下⽅式获得 从 下载源码包 从rpm.pbone.net搜索相关的rpm包 download.fedora.redhat.com/pub/fedora/epel/5/i386/lua-5.1.4-4.el5.i386.rpm download.fedora.redhat.com/pub/fedora/epel/5/x86_64/lua-5.1.4-4.el5.x86_64.rpm 这⾥我们建议采⽤源码包进⾏安装 cd /opt/instal wget tar zvfx lua-5.1.4.tar.gz cd lua-5.1.4 vi src/Makefile 在 CFLAGS= -O2 -Wal $(MYCFLAGS) 这⼀⾏记录⾥加上-fPIC，更改为 CFLAGS= -O2 -Wal -fPIC $(MYCFLAGS) 来避免编译过程中出现错误。 make linux make instal cp etc/lua.pc /usr/lib/pkgconfig/ export PKG_CONFIG_PATH=$PKG_CONFIG_PATH:/usr/lib/pkgconfig
- 3.4 安装配置MySQL-Proxy MySQL-Proxy可通过以下⽹址获得：


htp:/ w.lua.org/download.html

htp:/ w.lua.org/ftp/lua-5.1.4.tar.gz

htp:/mysql.cdpa.nsysu.edu.tw/Downloads/MySQL-Proxy/

推荐采⽤已经编译好的⼆进制版本，因为采⽤源码包进⾏编译时，最新版的MySQL-Proxy对 automake，glib以及libevent的版本都有很⾼的要求，⽽这些软件包都是系统的基础套件，不建议强⾏ 进⾏更新。 并且这些已经编译好的⼆进制版本在解压后都在统⼀的⽬录内，因此建议选择以下版本： 32位RHEL5平台：

htp:/mysql.cdpa.nsysu.edu.tw/Downloads/MySQL-Proxy/mysql-proxy-0.8.1-linux-rhel5-x86-32bit. tar.gz

64位RHEL5平台：

htp:/mysql.cdpa.nsysu.edu.tw/Downloads/MySQL-Proxy/mysql-proxy-0.8.1-linux-rhel5-x86-64bit. tar.gz

测试平台为RHEL5 32位，因此选择32位的软件包 wget

htp:/mysql.cdpa.nsysu.edu.tw/Downloads/MySQL-Proxy/mysql-proxy-0.8.1-linux-rhel5-x8632bit.tar.gz

tar xzvf mysql-proxy-0.8.1-linux-rhel5-x86-32bit.tar.gz mv mysql-proxy-0.8.1-linux-rhel5-x86-32bit /opt/mysql-proxy 创建mysql-proxy服务管理脚本 mkdir /opt/mysql-proxy/init.d/ vim mysql-proxy

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 01 #!/bin/sh

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 02 #


<table>
  <tr>
    <th>03</th>
    <th># mysql-proxy This script starts and stops the</th>
  </tr>
</table>


mysql-proxy daemon

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 04 #

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 05 # chkconfig: - 78 30

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 06 # procesname: mysql-proxy


<table>
  <tr>
    <th>07</th>
    <th># description: mysql-proxy is a proxy daemon</th>
  </tr>
</table>


to mysql

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 08

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 09 # Source function library.

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 10 . /etc/rc.d/init.d/functions


<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


1

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 12 #PROXY_PATH=/usr/local/bin

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 13 PROXY_PATH=/opt/mysql-proxy/bin


<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 14


<table>
  <tr>
    <th>15</th>
    <th>prog=</th>
  </tr>
</table>


"mysql-proxy"

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 16

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 17 # Source networking configuration.

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 18 . /etc/sysconfig/network

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 19

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 20 # Check that networking is up.


<table>
  <tr>
    <th>21</th>
    <th>[ ${NETWORKING} = "no"<br><br>] & exit</th>
  </tr>
</table>


0

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 2


<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 23 # Set default mysql-proxy configuration.

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 24 #PROXY_OPTIONS="-daemon"


<table>
  <tr>
    <th>25</th>
    <th>PROXY_OPTIONS= "-admin-username=rot -adminpasword=pasword-proxy-read-onlybackend-adreses=192.168.10.131  306proxy-backendadreses=192.168.10.130  306 -admin-luascript=/opt/mysql-proxy/lib/mysqlproxy/lua/admin.lua-proxy-lua-</th>
  </tr>
</table>


script=/opt/mysql-proxy/scripts/rw-spliting.lua"

<table>
  <tr>
    <th>26</th>
    <th>PROXY_PID=/opt/mysql-proxy/run/mysql-</th>
  </tr>
</table>


proxy.pid

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 27

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 28 # Source mysql-proxy configuration.


<table>
  <tr>
    <th>29</th>
    <th>if [ -f /etc/sysconfig/mysql-proxy ];</th>
  </tr>
</table>


then

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 30 . /etc/sysconfig/mysql-proxy

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 31 fi

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 32


<table>
  <tr>
    <th>3</th>
    <th>PATH=$PATH:/usr/bin:/usr/ local</th>
  </tr>
</table>


/bin:$PROXY_PATH

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 34

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 35 # By default it's al god

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 36 RETVAL=0

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 37

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 38 # Se how we were caled.


<table>
  <tr>
    <th>39</th>
    <th>case "$1"</th>
  </tr>
</table>


in

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 40 start)

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 41 # Start daemon.


<table>
  <tr>
    <th>42</th>
    <th>echo -n $</th>
  </tr>
</table>


"Starting $prog: "

<table>
  <tr>
    <th>43</th>
    <th>$NICELEVEL $PROXY_PATH/mysql-proxy $PROXY_OPTIONS-daemon-pidfile<br><br>=$PROXY_PID-user=mysql -loglevel=warning-logfile</th>
  </tr>
</table>


=/opt/mysql-proxy/log/mysql-proxy.log

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


4 RETVAL=$?

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


45 echo

<table>
  <tr>
    <th>46</th>
    <th>if [ $RETVAL = 0 ];</th>
  </tr>
</table>


then

<table>
  <tr>
    <th>47</th>
    <th>touch</th>
  </tr>
</table>


/var/lock/subsys/mysql-proxy

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 48 fi

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 49 ;

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 50 stop)

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 51 # Stop daemons.


<table>
  <tr>
    <th>52</th>
    <th>echo -n $</th>
  </tr>
</table>


"Stoping $prog: "

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 53 kilproc $prog

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 54 RETVAL=$?


<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


5 echo

<table>
  <tr>
    <th>56</th>
    <th>if [ $RETVAL = 0 ];</th>
  </tr>
</table>


then

<table>
  <tr>
    <th>57</th>
    <th>rm</th>
  </tr>
</table>


- -f /var/lock/subsys/mysql-proxy

<table>
  <tr>
    <th>58</th>
    <th>rm</th>
  </tr>
</table>


- -f $PROXY_PID


<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 59 fi

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 60 ;


<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 61 restart)

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 62 $0 stop


<table>
  <tr>
    <th>63</th>
    <th>sl ep</th>
  </tr>
</table>


3

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 64 $0 start

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 65 ;


<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


6 condrestart)

<table>
  <tr>
    <th>67</th>
    <th>[ -e /var/lock/subsys/mysql-proxy ] & $0</th>
  </tr>
</table>


restart

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 68 ;

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 69 status)

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 70 status mysql-proxy

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 71 RETVAL=$?

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 72 ;

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 73 *)


<table>
  <tr>
    <th>74</th>
    <th>echo "Usage: $0</th>
  </tr>
</table>


{start|stop|restart|status|condrestart}"

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 75 RETVAL=1

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 76 ;


<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


7 esac

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


78

<table>
  <tr>
    <th>79</th>
    <th>exit</th>
  </tr>
</table>


$RETVAL

脚本参数详解：

= PROXY_PATH=/opt/mysql-proxy/bin/定义mysql-proxy服务⼆进制⽂件路径 PROXY_OPTIONS="-admin-username=rot \ /定义内部管理服务器账号

- -admin-pasword=pasword \ /定义内部管理服务器密码
- -proxy-read-only-backend-adreses=192.168.10.131  306 \ /定义后端只读从服务器地址
- -proxy-backend-adreses=192.168.10.130  306 \ /定义后端主服务器地址
- -admin-lua-script=/opt/mysql-proxy/lib/mysql-proxy/lua/admin.lua \ /定义lua管理脚本路径
- -proxy-lua-script=/opt/mysql-proxy/scripts/rw-spliting.lua" \ /定义lua读写分离脚本路径


PROXY_PID=/opt/mysql-proxy/run/mysql-proxy.pid/定义mysql-proxy PID⽂件路径 $NICELEVEL $PROXY_PATH/mysql-proxy $PROXY_OPTIONS \

- -daemon \ /定义以守护进程模式启动
- -kepalive \ /使进程在异常关闭后能够⾃动恢复
- -pid-file=$PROXY_PID \ /定义mysql-proxy PID⽂件路径
- -user=mysql \ /以mysql⽤户身份启动服务
- -log-level=warning \ /定义log⽇志级别，由⾼到低分别有(eror|warning|info|mesage|debug)
- -log-file=/opt/mysql-proxy/log/mysql-proxy.log/定义log⽇志⽂件路径


=

cp mysql-proxy /opt/mysql-proxy/init.d/ chmod +x /opt/mysql-proxy/init.d/mysql-proxy mkdir /opt/mysql-proxy/run mkdir /opt/mysql-proxy/log mkdir /opt/mysql-proxy/scripts 配置并使⽤rw-spliting.lua读写分离脚本 最新的脚本我们可以从最新的mysql-proxy源码包中获取 cd /opt/instal wget tar xzvf mysql-proxy-0.8.1.tar.gz cd mysql-proxy-0.8.1 cp lib/rw-spliting.lua /opt/mysql-proxy/scripts 修改读写分离脚本rw-spliting.lua 修改默认连接，进⾏快速测试，不修改的话要达到连接数为4时才启⽤读写分离 vim /opt/mysql-proxy/scripts/rw-spliting.lua

htp:/mysql.cdpa.nsysu.edu.tw/Downloads/MySQL-Proxy/mysql-proxy-0.8.1.tar.gz

=

- conection pol

if not proxy.global.config.rwsplit then proxy.global.config.rwsplit = { min_idle_conections = 1, /默认为4 max_idle_conections = 1, /默认为8 is_debug = false } end

=

修改完成后，启动mysql-proxy /opt/mysql-proxy/init.d/mysql-proxy start

- 3.5 测试读写分离效果 创建⽤于读写分离的数据库连接⽤户 登陆主数据库服务器192.168.10.130，通过命令⾏登录管理MySQL服务器 /opt/mysql/bin/mysql -urot -p'new-pasword' mysql> GRANT AL ON.* TO 'proxy1'@'192.168.10.132' IDENTIFIED BY 'pasword'; 由于我们配置了主从复制功能，因此从数据库服务器192.168.10.131上已经同步了此操作。 为了清晰的看到读写分离的效果，需要暂时关闭MySQL主从复制功能 登陆从数据库服务器192.168.10.131，通过命令⾏登录管理MySQL服务器 /opt/mysql/bin/mysql -urot -p'new-pasword' 关闭Slave同步进程 mysql> stop slave; Query OK, 0 rows afected (0.0 sec) 连接MySQL-Proxy /opt/mysql/bin/mysql -uproxy1 -p'pasword' -P4040 -h192.168.10.132 登陆成功后，在first_db数据的first_tb表中插⼊两条记录 mysql> use first_db; Database changed mysql> insert into first_tb values (07,ʼfirstʼ); Query Ok, 1 row afected (0.0 sec) mysql> insert into first_tb values (10,ʼsecondʼ); Query Ok, 1 row afected (0.0 sec) 查询记录 mysql> select * from first_tb;


=

+ -+ -+ | id | name |

+ -+ -+

| 1 | myself |

+ -+ -+ 1 rows in set (0.0 sec)

=

通过读操作并没有看到新记录 mysql> quit 退出MySQL-Proxy 下⾯，分别登陆到主从数据库服务器，对⽐记录信息 ⾸先，检查主数据库服务器 mysql> select * from first_tb;

=

+ -+ -+ | id | name |

+ -+ -+ | 1 | myself |

+ -+ -+ | 07 | first |

+ -+ -+ | 10 | second |

+ -+ -+

- 3 rows in set (0.0 sec)


=

两条新记录都已经存在 然后，检查从数据库服务器 mysql> select * from first_tb;

=

+ -+ -+ | id | name |

+ -+ -+ | 1 | myself |

+ -+ -+ 1 rows in set (0.0 sec)

=

没有新记录存在 由此验证，我们已经实现了MySQL读写分离，⽬前所有的写操作都全部在Master主服务器上，⽤来避 免数据的不同步； 另外，所有的读操作都分摊给了其它各个Slave从服务器上，⽤来分担数据库压⼒。

经验分享：

- 1.当MySQL主从复制在 show slave status\G 时出现Slave_IO_Runing或Slave_SQL_Runing 的值不 为YES时，需要⾸先通过 stop slave 来停⽌从服务器，然后再执⾏⼀次本⽂ 2.1与2.2 章节中的步骤即 可恢复，但如果想尽可能的同步更多的数据，可以在Slave上将master_log_pos节点的值在之前同步失 效的值的基础上增⼤⼀些，然后反复测试，直到同步OK。因为MySQL主从复制的原理其实就是从服务 器读取主服务器的binlog，然后根据binlog的记录来更新数据库。
- 2.MySQL-Proxy的rw-spliting.lua脚本在⽹上有很多版本，但是最准确⽆误的版本仍然是源码包中所附 带的lib/rw-spliting.lua脚本，如果有lua脚本编程基础的话，可以在这个脚本的基础上再进⾏优化；
- 3.MySQL-Proxy实际上⾮常不稳定，在⾼并发或有错误连接的情况下，进程很容易⾃动关闭，因此打 开 -kepalive参数让进程⾃动恢复是个⽐较好的办法，但还是不能从根本上解决问题，因此通常最稳 妥的做法是在每个从服务器上安装⼀个MySQL-Proxy供⾃身使⽤，虽然⽐较低效但却能保证稳定性；
- 4.⼀主多从的架构并不是最好的架构，通常⽐较优的做法是通过程序代码和中间件等⽅⾯，来规划，⽐ 如设置对表数据的⾃增id值差异增⻓等⽅式来实现两个或多个主服务器，但⼀定要注意保证好这些主服 务器数据的完整性，否则效果会⽐多个⼀主多从的架构还要差；
- 5.MySQL-Cluster 的稳定性也不是太好；
- 6.Amoeba for MySQL 是⼀款优秀的中间件软件，同样可以实现读写分离，负载均衡等功能，并且稳 定性要⼤⼤超过MySQL-Proxy，建议⼤家⽤来替代MySQL-Proxy，甚⾄MySQL-Cluster。


