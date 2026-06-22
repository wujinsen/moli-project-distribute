linux设置开机服务⾃动启动

[root@localhost ~]# chkconﬁg --list 显⽰开机可以⾃动启动的服务

[root@localhost ~]# chkconﬁg --add *** 添加开机⾃动启动***服务

[root@localhost ~]# chkconﬁg --del *** 删除开机⾃动启动***服务

www.2cto.com

[root@localhost ~]# setup 可以在shell图形终端⾥⾯配置的命令，去service⾥选择

[root@localhost ~]# ntsysv 在shell终端图形配置开机启动服务命令，选项没上⾯那个多

setup 、rc.local 和chkconﬁg三种⽅式都可以设置

第⼀种)

输⼊#setup指令进⼊系统服务菜单，选择你想启动的服务⽐如httpd，然后重起机器或 者/etc/rc.d./init.d/httpd

start

www.2cto.com

第⼆种)

把启动命令放到/etc/rc.d/rc.local⽂件⾥这样就可以每次启动的时候⾃动启动服务了,例如对于 apache,编译好apache后会在安装⽬录的bin下⽣成apachectl⽂件,这是个启动脚本,我们只需要把这个 命令加到rc.local⾥就可以了

（suse没有rc.local。SUSE是可以这么定义⾃⼰的脚本的，如果希望在切换运⾏级之前和之后运⾏ ⾃⼰的脚本，那么可以分别创建：

/etc/init.d/before.local

/etc/init.d/after.local）

echo /usr/local/apache/bin/apachectl >> /etc/rc.d/rc.local

设置服务⾃动启动的⽅式是在rc.local⾥还可以加⼊类似以下的⼀些脚本：

#sshd

/usr/local/sbin/sshd

#proftpd

/usr/local/sbin/proftpd

#apache

/home/apache/bin/apachectl start

#mysql

/home/mysql/bin/safe_mysqld --port=3306 &

#start oracle8i listener ﬁrst

su - oracle -c 'lsnrctl start'

#start oracle8i

su - oracle -c 'dbstart'

第三种)

通过chkconﬁg指令.

使⽤chkconﬁg命令来把某项服务加到系统的各项运⾏级别中,步骤如下,

- 1 创建启动脚本.

对于apache,mysql,ssh这样的软件都是⾃⼰带的,我们只要稍微修改⼀下使之⽀持chkconﬁg就可以了

- 2 修改脚本


我们需要在脚本的前⾯加上⼀下2⾏,才能⽀持chkconﬁg命令

# chkconﬁg: 2345 08 92

#

# description: Automates a packet ﬁltering ﬁrewall withipchains.

#

chkconﬁg:后⾯定义的使启动服务的运⾏级别(例⼦中使2345启动改服务),以及关闭和启动服务的顺 序,(上例中关闭服务的顺序使8,启动的顺序使92)

descriptions:对改服务的描述(上例中是ipchains包过滤),你可以换成⾃⼰想要的

修改好之后执⾏

cp 你的脚本 /etc/rc.d/init.d/脚本名

chmod 700 /etc/rc.d/init.d/脚本名

chkconﬁg --add 脚本名

例如:

将其加⼊Linux启动过程，仅在level 3, level 5级别下运⾏

[root@Tester init.d]/sbin/chkconﬁg --add apache-httpd

[root@Tester init.d]/sbin/chkconﬁg --level 35 apache-httpdon

之后就可以了,以后每次重新启动服务器都会⾃动启动和关闭我们的服务了

