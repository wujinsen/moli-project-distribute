进⼊ home/itcast ⽬录

cd /home/itcast

ls 显示⽬录内容：

ls

mkdir 创建⼀个⽬录 家庭A： mkdir familyA

cd 切换⽬录 进⼊familyA ⽬录： cd familyA

touch 新建空⽂件

touch father.txt 家庭A中有⼀个⽗亲 touch mother.txt 有⼀个⺟亲 touch son.txt ⼉⼦ touch daughter.txt ⼥⼉

cd 切换⽬录

cd house

mkdir 创建⽬录 mkdir kitchen 厨房 mkdir bathrom 卫⽣间 mkdir romA mkdir romB mkdir romC touch sofa.txt 沙发

touch 新建空⽂件

touch bed.txt echo "Your are the best boy." > bok.txt 建⽴带简单内容的⽂件 cat bok.txt 查看⽂件内容

cd 切换⽬录 cd. 返回上⼀级⽬录 cd romC

touch 新建空⽂件 touch bed.txt echo "Your are the best girl." > bok.txt cat bok.txt touch dol.txt

cp 拷⻉⽂件 cp dol.txt /home/itcast/familyA/house/romB

cd 切换⽬录 cd. cd romB

ls 显示⽬录内容：

ls 显示

cd 切换⽬录 cd.

mv 移动⽂件 mv sofa.txt /home/itcast/familyA/house/romB

cd 切换⽬录 cd romB

ls 显示⽬录内容：

ls 显示

rm 删除⽂件⽬录 rm dol.txt

more les 分⻚显示

more bok.txt

wc 显示⽂档⾏数，字数，字符数 wc bok.txt

find 查找指定的⽂件 find -name bok.txt

grep 查找指定字符串

grep best bok.txt

pwd 显示当前⽬录

tre 显示⽬录树

rmdir 删除空⽬录 rmdir bathrom

ln -s 建⽴软连接

ln -s /home/itcast/familyA/house/romB /home/romB

who、w 显示在线登录⽤户 who

whoami 显示⽤户⾃⼰的身份

hostname 显示主机名称

hostname hostname -i 显示主机IP

uname 显示系统信息

uname -a 显示全部信息 (内核名称，主机名，内核版本号，内核版本，硬件名，处理器类型，硬 件平台类型，操作系统名称)

top 显示当前系统中耗费资源最多的进程 动态显示过程，实时监控

ps 显示瞬间进程状态 ps -aux 显示所有瞬间进程状态

du 显示指定的⽂件（⽬录）已使⽤的磁盘空间的总量 .可以使⽤ -help查看帮助 du du familyA du -h familyA

df 显示⽂件系统磁盘空间的使⽤情况 df df -h

fre 显示当前内存和交换空间的使⽤情况

ifconfig 显示⽹络接⼝信息

ping 测试⽹络的连通性

netstat 显示⽹络状态信息 netstat -anp|grep 9217

gzip 命令

把/home/itcast⽬录下的familyA⽬录下所有⽂件压缩成.gz⽂件 cd /home/itcast tar -cvf /home/itcast/familyA.tar /home/itcast/familyA ⾸先进⾏打包，因为gzip不能直接对⽬录

进⾏压缩

gzip familyA.tar 进⾏压缩 gzip -l familyA.tar.gz 查看压缩包详细信息

解压缩familyA.tar gzip -dv familyA.tar.gz gzip -v -9 familyA.tar ⾼压缩⽐ gzip -l familyA.tar.gz

gzip -dv familyA.tar.gz gizp -v -1 familyA.tar 低压缩⽐ gzip -l familyA.tar.gz

bzip2 命令

把/home/itcast⽬录下的familyA⽬录下所有⽂件压缩成.bz2⽂件 cd /home/itcast tar -cvf /home/itcast/familyA.tar /home/itcast/familyA bzip2 -z familyA.tar 压缩需加上参数-z

解压缩itcast.tar.bz2 bzip2 -d familyA.tar.bz2

tar 命令

将整个/home/itcast/familyA⽬录下的⽂件全部打包成为/home/itcast/familyA.tar

仅打包，不压缩

tar -cvf /home/itcast/familyA.tar /home/itcast/familyA

打包后，以gzip压缩

tar -zcvf /home/itcast/familyA.tar.gz /home/itcast/familyA

打包后，以bzip2压缩

tar -jcvf /home/itcast/familyA.tar.bz2 /home/itcast/familyA

# 特别注意，在参数f之后的⽂件档名是⾃⼰取的，我们习惯上都⽤.tar来作为辨识 # 如果加z参数，则以.tar.gz或.tgz来代表gzip压缩过的tar file # 如果加j参数，则以.tar.bz2来作为⽂档名

VIM 编辑器

在/home/itcast/⽬录下建⽴⼀个bank.txt⽂件

cd /home/itcast/familyA/ touch bank.txt vim bank.txt

数据命令i 进⼊插⼊模式

输⼊内容 ICBC RMB 1 0 USD 1 0 user:familyA.father

ctrl+C 退出插⼊模式或者敲ESC切换⾄命令模式

:wq 回⻋ 保存

编辑bank.txt 内容不保存 退出

vim bank.txt

数据命令i 进⼊插⼊模式

随便输⼊内容

ctrl+C 退出插⼊模式或者敲ESC

:q! 回⻋ 强制退出

编辑bank.txt 内容并显示⾏号

vim bank.txt

:set number 回⻋

:q 回⻋ 正常退出

添加⼀个账户 userad -m ltw 参数-m⽤来设定系统添加账户时⾃动建⽴⽤户根⽬录 aduser 修改ltw账户的 登录名称 usermod -l litingwei ltw 修改litingwei账户的 登录⽬录 (注意：修改前需要提前⼿动建⽴好litingwei⽬录) usermod -d /home/litingwei litingwei 锁定⽤户litingwei账号密码 usermod -L litingwei 锁定后账号不可使⽤ 解锁⽤户litingwei账号密码 usermod -U litingwei

添加⼀个分组 groupad superman 修改superman分组 groupmod -g 35 superman 删除 superman 分组 groupdel superman 修改rot 密码（rot密码尚未设定，需要设定密码后⽅可使⽤） paswd rotrot ，litingwei 账户切换 su - rot 或sudo -i 切换rot 删除litingwei账号 userdel -r litingwei (-r 连同⽤户⽬录⼀起删除)

显示出⽂件 /home/itcast/familyA/bank.txt 的权限

cd /home/itcast/familyA/ ls -l

切换⾄litingwei⽤户

su litingwei

⽤litingwei账户查看是否可以读写bank.txt

cat bank.txt

vim bank.txt 此时litingwei没有权限进⾏修改⽂件

切换回 itcast su itcast

修改bank.txt 权限为 其他⽤户可读写

chmod o+w bank.txt

再切换回litingwei

su litingwei

修改bank.txt

vim bank.txt

第⼀步 安装jdk

su - rot 切换成rot⽤户

sudo -i 不需要密码直接切换成rot

- 1.进⼊usr⽬录

cd /usr

- 2.在usr⽬录下建⽴java安装⽬录


- mkdir java
- 3.将jdk-6u24-linux-i586.bin拷⻉到java⽬录下

cp /home/itcast/Desktop/jdk-6u24-linux-i586.bin /usr/java

- 4.安装jdk

cd /usr/java

./jdk-6u24-linux-i586.bin

- 5.安装完毕为他建⽴⼀个链接以节省⽬录⻓度

ln -s /usr/java/jdk1.6.0_24/ /usr/jdk

- 6.编辑配置⽂件


vim /etc/profile

添加如下内容： JAVA_HOME=/usr/jdk CLASPATH=$JAVA_HOME/lib/ PATH=$PATH:$JAVA_HOME/bin export PATH JAVA_HOME CLASPATH

- 8.重启机器

sudo shutdown -r now

- 9.查看安装情况 java -version


java version "1.6.0_24"

Java(TM) SE Runtime Environment (build 1.6.0_24-b07) Java HotSpot(TM) Client VM (build 19.1-b02, mixed mode, sharing)

第⼆步安装 tomcat

tar -zxvf apache-tomcat-6.0.29.tar.gz -C /opt (解压到/opt下) ln -s /opt/apache-tomcat-6.0.29/ /opt/tomcat (建⽴链接⽂件) 启动tomcat cd /opt/tomcat/bin/

./startup.sh (注意：点代表当前⽬录下) 如果启动不了，请尝试

-i 切换到rot⽤户再重新启动

./startup.sh 测试

htp:/127.0.0.1 8080/

第三步 安装eclipse

tar -zxvf eclipse-j e-helios-linux-gtk.tar.gz -C /opt (解压到/usr/local⽬录下并⽣成/usr/local/eclipse ⽬录) cd /opt/eclipse/

./eclipse (注意：点代表当前⽬录下)

telnet

- 1.安装telnet-server

sudo dpkg -i xinetd_1%3a2.3.14-7ubuntu3_i386.deb sudo dpkg -i telnetd_0.17-36build1_i386.deb

如果连⽹的情况下可以 sudo apt-get instal telnet 进⾏安装

- 2.设置⼀下ip


sudo ifconfig eth0 192.168.1. 2 netmask 25.25.25.0

- 3.修改/etc/xinetd.conf配置⽂件

vim /etc/xinetd.conf

加⼊如下内容：

defaults { # Please note that you ned a log_type line to be able to use log_on_suces # and log_on_failure. The default is the folowing : # log_type = SYSLOG daemon info(插⼊如下部分） instances = 60 log_type = SYSLOG authpriv log_on_suces = HOST PID log_on_failure = HOST cps = 25 30 }

- 4.修改/etc/xinetd.d/telnet 配置⽂件


vim /etc/xinetd.d/telnet

加⼊如下内容：

# default: on # description: The telnet server serves telnet sesions; it uses \ # unencrypted username/pasword pairs for authentication. service telnet { disable = no flags = REUSE socket_type = stream wait = no user = rot server = /usr/sbin/in.telnetd

log_on_failure += USERID }

- 5.重启⽹络服务

sudo /etc/init.d/xinetd restart

- 6.打开window命令⾏ telnet 192.168.1. 2


- 1.安装tre软件包

sudo dpkg -i tre_1.5.3-1_i386.deb

- 2.删除tre软件包

sudo dpkg -r tre

- 3.查看软件包中信息

sudo dpkg -c tre_1.5.3-1_i386.deb

- 4.查看Ubuntu系统已安装所有软件包列表


sudo dpkg -l

sudo及其配置⽂件sudoers

sudo是linux下常⽤的允许普通⽤户使⽤超级⽤户权限的⼯具。

它的主要配置⽂件是sudoers,linux下通常在/etc⽬录下，如果是solaris，缺省不装sudo的，编译安装 后通常在安装⽬录的etc⽬录下，不过不管sudoers⽂件在哪⼉，sudo都提供了⼀个编辑该⽂件的命 令：visudo来对该⽂件进⾏修改。强烈推荐使⽤该命令修改sudoers，因为它会帮你校验⽂件配置是否 正确，如果不正确，在保存退出时就会提示你哪段配置出错的。 ⾔归正传，下⾯介绍如何配置sudoers

⾸先写sudoers的缺省配置：

#

# sudoers file. # # This file MUST be edited with the 'visudo' comand as rot. # # Se the sudoers man page for the details on how to write a sudoers file. # # Host alias specification # User alias specification # Cmnd alias specification # Defaults specification # User privilege specification rot AL=(AL) AL # Uncoment to alow people in group whel to run al comands # %whel AL=(AL) AL # Same thing without a pasword # %whel AL=(AL) NOPASWD: AL # Samples # %users AL=/sbin/mount /cdrom,/sbin/umount /cdrom # %users localhost=/sbin/shutdown -h now

#

- 1. 最简单的配置，让普通⽤户suport具有rot的所有权限 执⾏visudo之后，可以看⻅缺省只有⼀条配置： rot AL=(AL) AL 那么你就在下边再加⼀条配置： suport AL=(AL) AL 这样，普通⽤户suport就能够执⾏rot权限的所有命令 以suport⽤户登录之后，执⾏： sudo su 然后输⼊suport⽤户⾃⼰的密码，就可以切换成rot⽤户了
- 2. 让普通⽤户suport只能在某⼏台服务器上，执⾏rot能执⾏的某些命令 ⾸先需要配置⼀些Alias，这样在下⾯配置权限时，会⽅便⼀些，不⽤写⼤段⼤段的配置。Alias主要分 成4种 Host_Alias Cmnd_Alias User_Alias


Runas_Alias

- 1) 配置Host_Alias：就是主机的列表 Host_Alias HOST_FLAG = hostname1, hostname2, hostname3
- 2) 配置Cmnd_Alias：就是允许执⾏的命令的列表 Cmnd_Alias COMAND_FLAG = comand1, comand2, comand3
- 3) 配置User_Alias：就是具有sudo权限的⽤户的列表 User_Alias USER_FLAG = user1, user2, user3
- 4) 配置Runas_Alias：就是⽤户以什么身份执⾏（例如rot，或者oracle）的列表 Runas_Alias RUNAS_FLAG = operator1, operator2, operator3
- 5) 配置权限 配置权限的格式如下： USER_FLAG HOST_FLAG=(RUNAS_FLAG) COMAND_FLAG 如果不需要密码验证的话，则按照这样的格式来配置 USER_FLAG HOST_FLAG=(RUNAS_FLAG) NOPASWD: COMAND_FLAG 配置示例：


#

# # sudoers file. # # This file MUST be edited with the 'visudo' comand as rot. # # Se the sudoers man page for the details on how to write a sudoers file. # # Host alias specification Host_Alias EPG = 192.168.1.1, 192.168.1.2 # User alias specification # Cmnd alias specification Cmnd_Alias SQUID = /opt/vtbin/squid_refresh, /sbin/service, /bin/rm # Defaults specification # User privilege specification rot AL=(AL) AL suport EPG=(AL) NOPASWD: SQUID # Uncoment to alow people in group whel to run al comands # %whel AL=(AL) AL # Same thing without a pasword # %whel AL=(AL) NOPASWD: AL # Samples

# %users AL=/sbin/mount /cdrom,/sbin/umount /cdrom # %users localhost=/sbin/shutdown -h now

# 我们不可以使⽤su让他们直接变成rot，因为这些⽤户都必须知道rot的密码，这种⽅法很不安全，⽽ 且也不符合我们的分⼯需求。⼀般的做法是利⽤权限的设置，依⼯作性质分类，让特殊身份的⽤户成 为同⼀个⼯作组，并设置⼯作组权限。例如：要 wadm这位⽤户负责管理⽹站数据，⼀般Apache Web Server的进程htpd的所有者是 w，您可以设置⽤户 wadm与 w为同⼀⼯作组，并设置 Apache默认存放⽹⻚⽬录 /usr/local/htpd/htdocs的⼯作组权限为可读、可写、可执⾏，这样属于此 ⼯作组的每位⽤户就可以进⾏⽹⻚的管理了。 但这并不是最好的解决办法，例如管理员想授予⼀个普通⽤户关机的权限，这时使⽤上述的办法就不 是很理想。这时您也许会想，我只让这个⽤户可以以 rot身份执⾏shutdown命令就⾏了。完全没错， 可惜在通常的Linux系统中⽆法实现这⼀功能，不过已经有了⼯具可以实现这样的功能⸺ sudo。 sudo通过维护⼀个特权到⽤户名映射的数据库将特权分配给不同的⽤户，这些特权可由数据库中所列 的⼀些不同的命令来识别。为了获得某⼀特权项，有资格的⽤户只需简单地在命令⾏输⼊sudo与命令 名之后，按照提示再次输⼊⼝令（⽤户⾃⼰的⼝令，不是rot⽤户⼝令）。例如，sudo允许普通⽤户 格式化磁盘，但是却没有赋予其他的rot⽤户特权。

- 1、sudo⼯具由⽂件/etc/sudoers进⾏配置，该⽂件包含所有可以访问sudo⼯具的⽤户列表并定义了他 们的特权。⼀个典型的/etc/sudoers条⽬如下： 代码: liming AL=(AL) AL 这个条⽬使得⽤户liming作为超级⽤户访问所有应⽤程序，如⽤户liming需要作为超级⽤户运⾏命令， 他只需简单地在命令前加上前缀sudo。因此，要以rot⽤户的身份执⾏命令format，liming可以输⼊如 下命令： 代码: # sudo /usr/sbin/userad sam 注意：命令要写绝对路径，/usr/sbin默认不在普通⽤户的搜索路径中，或者加⼊此路径： PATH=$PATH:/usr/sbin;export PATH。另外，不同系统命令的路径不尽相同，可以使⽤命令“whereis 命令名”来查找其路径。 这时会显示下⾯的输出结果： 代码: We trust you have received the usual lecture from the local System Administrator. It usualy boils down to these two things:


- #1) Respect the privacy of others.
- #2) Think before you type. Pasword: 如果liming正确地输⼊了⼝令，命令userad将会以rot⽤户身份执⾏。 注意：配置⽂件/etc/sudoers必须使⽤命令 Visudo来编辑。


只要把相应的⽤户名、主机名和许可的命令列表以标准的格式加⼊到⽂件/etc/sudoers，并保存就可以 ⽣效，再看⼀个例⼦。

- 2、例⼦：管理员需要允许gem⽤户在主机sun上执⾏rebot和shutdown命令，在/etc/sudoers中加 ⼊： 代码: gem sun=/usr/sbin/rebot，/usr/sbin/shutdown 注意：命令⼀定要使⽤绝对路径，以避免其他⽬录的同名命令被执⾏，从⽽造成安全隐患。 然后保存退出，gem⽤户想执⾏rebot命令时，只要在提示符下运⾏下列命令： 代码: $ sudo /usr/sbin/rebot 输⼊正确的密码，就可以重启服务器了。 如果您想对⼀组⽤户进⾏定义，可以在组名前加上%，对其进⾏设置，如： 代码: %cug AL=(AL) AL
- 3、另外，还可以利⽤别名来简化配置⽂件。别名类似组的概念，有⽤户别名、主机别名和命令别名。 多个⽤户可以⾸先⽤⼀个别名来定义，然后在规定他们可以执⾏什么命令的时候使⽤别名就可以了， 这个配置对所有⽤户都⽣效。主机别名和命令别名也是如此。注意使⽤前先要在/etc/sudoers中定义： User_Alias, Host_Alias, Cmnd_Alias项，在其后⾯加⼊相应的名称，也以逗号分隔开就可以了，举例 如下： 代码: Host_Alias SERVER=no1 User_Alias ADMINS=liming，gem Cmnd_Alias SHUTDOWN=/usr/sbin/halt，/usr/sbin/shutdown，/usr/sbin/rebot ADMINS SERVER=SHUTDOWN 、再看这个例⼦： 代码: ADMINS AL=(AL) NOPASWD: AL 表示允许ADMINS不⽤⼝令执⾏⼀切操作，其中“NOPASWD:”项定义了⽤户执⾏操作时不需要输⼊⼝ 令。


- 5、sudo命令还可以加上⼀些参数，完成⼀些辅助的功能，如 代码: $ sudo –l 会显示出类似这样的信息： 代码: User liming may run the folowing comands on this host: (rot) /usr/sbin/rebot


- 说明rot允许⽤户liming执⾏/usr/sbin/rebot命令。这个参数可以使⽤户查看⾃⼰⽬前可以在sudo中 执⾏哪些命令。
- 6、在命令提示符下键⼊sudo命令会列出所有参数，其他⼀些参数如下： 代码:


- -V 显示版本编号。
- -h 显示sudo命令的使⽤参数。
- -v 因为sudo在第⼀次执⾏时或是在N分钟内没有执⾏（N预设为5）会询问密码。这个参数是重新做⼀ 次确认，如果超过N分钟，也会问密码。
- -k 将会强迫使⽤者在下⼀次执⾏sudo时询问密码（不论有没有超过N分钟）。
- -b 将要执⾏的命令放在背景执⾏。
- -p prompt 可以更改问密码的提示语，其中%u会替换为使⽤者的账号名称，%h会显示主机名称。
- -u username/#uid 不加此参数，代表要以rot的身份执⾏命令，⽽加了此参数，可以以username的身 份执⾏命令（#uid为该username的UID）。
- -s 执⾏环境变量中的 SHEL 所指定的 Shel ，或是 /etc/paswd ⾥所指定的 Shel。
- -H 将环境变量中的HOME（宿主⽬录）指定为要变更身份的使⽤者的宿主⽬录。（如不加-u参数就是 系统管理者rot。） 要以系统管理者身份（或以-u更改为其他⼈）执⾏的命令。


# 实 例 #

实例⼀：

beinan AL=/bin/chown,/bin/chmod

假如我们在/etc/sudoers 中添加这⼀⾏，表示beinan 能够在任何可能出现的主机名的系统中，能够转 换到rot⽤户下执⾏ /bin/chown 和/bin/chmod 命令，通过sudo -l 来查看beinan 在这台主机上允许和 禁⽌运⾏的命令；

值得注意的是，在这⾥省略了指定转换到哪个⽤户下执⾏/bin/shown 和/bin/chmod命令；在省略的情 况下默认为是转换到rot⽤户下执⾏；同时也省略了是不是需要beinan⽤户输⼊验证密码，假如省略 了，默认为是需要验证密码。

为了更周详的说明这些，我们能够构造⼀个更复杂⼀点的公式；

授权⽤户 主机=[(转换到哪些⽤户或⽤户组)] [是否需要密码验证] 命令1,[(转换到哪些⽤户或⽤户组)] [是否需要密码验证] [命令2],[(转换到哪些⽤户或⽤户组)] [是否需要密码验证] [命令3] .

注解：

凡是[ ]中的内容，是能够省略；命令和命令之间⽤,号分隔；通过本⽂的例⼦，能够对照着看哪些是省 略了，哪些地⽅需要有空格；

在[(转换到哪些⽤户或⽤户组)] ，假如省略，则默认为rot⽤户；假如是AL ，则代表能转换到任何⽤ 户；注意要转换到的⽬的⽤户必须⽤()号括起来，⽐如(AL)、(beinan)

实例⼆：

beinan AL=(rot) /bin/chown, /bin/chmod

假如我们把第⼀个实例中的那⾏去掉，换成这⾏；表示的是beinan 能够在任何可能出现的主机名的主 机中，能够转换到rot下执⾏ /bin/chown ，能够转换到任何⽤户招执⾏/bin/chmod 命令，通过sudo -l 来查看beinan 在这台主机上允许和禁⽌运⾏的命令；

实例三：

beinan AL=(rot) NOPASWD: /bin/chown,/bin/chmod

假如换成这个例⼦呢？表示的是beinan 能够在任何可能出现的主机名的主机中，能够转换到rot下执 ⾏ /bin/chown ，⽆需输⼊beinan⽤户的密码；并且能够转换到任何⽤户下执⾏/bin/chmod 命令，但执 ⾏chmod时需要beinan输⼊⾃⼰的密码；通过sudo -l 来查看beinan 在这台主机上允许和禁⽌运⾏的 命令；

关于⼀个命令动作是不是需要密码，我们能够发现在系统在默认的情况下是需要⽤户密码的，除⾮特 加指出⽆需⽤户需要输⼊⾃⼰密码，所以要在执⾏动作之前加⼊NOPASWD: 参数；

有可能有的弟兄对系统管理的命令不太懂，不知道其⽤法，这样就影响了他对 sudoers定义的理解， 下⾯我们再举⼀个最简单，最有说服务⼒的例⼦；

实例四：

⽐如我们想⽤beinan普通⽤户通过more /etc/shadow⽂档的内容时，可能会出现下⾯的情况；

[beinan@localhost ~]?$ more /etc/shadow/etc/shadow: 权限不够

这时我们能够⽤sudo more /etc/shadow 来读取⽂档的内容；就需要在/etc/soduers中给beinan授权

于是我们就能够先su 到rot⽤户下通过visudo 来改/etc/sudoers ；（⽐如我们是以beinan⽤户登录系 统的）

[beinan@localhost ~]?$ su

Pasword: 注：在这⾥输⼊rot密码

下⾯运⾏visodu；

[rot@localhost beinan]# visudo 注：运⾏visudo 来改 /etc/sudoers

加⼊如下⼀⾏，退出保存；退出保存，在这⾥要会⽤vi，visudo也是⽤的vi编辑器；⾄于vi的⽤法不多 说了； beinan AL=/bin/more 表示beinan能够转换到rot下执⾏more 来查看⽂档；

退回到beinan⽤户下，⽤exit命令；

[rot@localhost beinan]# exit

exit

[beinan@localhost ~]?$

查看beinan的通过sudo能执⾏哪些命令？

[beinan@localhost ~]?$ sudo -l

Pasword: 注：在这⾥输⼊beinan⽤户的密码

User beinan may run the folowing comands on this host: 注：在这⾥清楚的说明在本台主机上， beinan⽤户能够以rot权限运⾏more ；在rot权限下的more ，能够查看任何⽂本⽂档的内容的；

(rot) /bin/more

最后，我们看看是不是beinan⽤户有能⼒看到/etc/shadow⽂档的内容；

[beinan@localhost ~]?$ sudo more /etc/shadow

beinan 不但能看到 /etc/shadow⽂档的内容，还能看到只有rot权限下才能看到的其他⽂档的内容， ⽐如；

[beinan@localhost ~]?$ sudo more /etc/gshadow

对于beinan⽤户查看和读取任何系统⽂档中，我只想把/etc/shadow 的内容能够让他查看；能够加⼊下 ⾯的⼀⾏；

beinan AL=/bin/more /etc/shadow

题外话：有的弟兄会说，我通过su 转换到rot⽤户就能看到任何想看的内容了，哈哈，对啊。但咱们 现在不是在讲述sudo的⽤法吗？假如主机上有多个⽤户并且不知道rot⽤户的密码，但⼜想查看某些 他们看不到的⽂档，这时就需要管理员授权了；这就是sudo的好处；

实例五：练习⽤户组在/etc/sudoers中写法；

假如⽤户组出现在/etc/sudoers 中，前⾯要加%号，⽐如%beinan ，中间不能有空格；%beinan AL=/usr/sbin/*,/sbin/*

假如我们在 /etc/sudoers 中加上如上⼀⾏，表示beinan⽤户组下的任何成员，在任何可能的出现的主 机名下，都能转换到rot⽤户下运⾏ /usr/sbin和/sbin⽬录下的任何命令；

实例六：练习取消某类程式的执⾏：

取消程式某类程式的执⾏，要在命令动作前⾯加上!号； 在本例中也出现了通配符的*的⽤法；

beinan AL=/usr/sbin/*,/sbin/*,!/usr/sbin/fdisk 注：把这⾏规则加⼊到/etc/sudoers中；但您得有 beinan这个⽤户组，并且beinan也是这个组中的才⾏；

本规则表示beinan⽤户在任何可能存在的主机名的主机上运⾏/usr/sbin和/sbin下任何的程式，但fdisk 程式除外；

[beinan@localhost ~]?$ sudo -l

Pasword: 注：在这⾥输⼊beinan⽤户的密码；

User beinan may run the folowing comands on this host:(rot) /usr/sbin/*(rot) /sbin/*(rot) !/sbin/fdisk[beinan@localhost ~]?$ sudo /sbin/fdisk -lSory, user beinan is not alowed to execute '/sbin/fdisk -l' as rot on localhost.

注：不能转换到rot⽤户下运⾏fdisk 程式；

实例七：别名的运⽤实践；

假如我们就⼀台主机localhost，能通过hostname 来查看，我们在这⾥就不定义主机别名了，⽤AL来 匹配任何可能出现的主机名；并且有beinan、linuxsir、lanhaitun ⽤户；主要是通过⼩例⼦能更好理 解；sudo虽然简单好⽤，但能把说的明⽩的确是件难事；最好的办法是多看例⼦和man soduers ；

User_Alias SYSADER=beinan,linuxsir,%beinan

User_Alias DISKADER=lanhaitun

Runas_Alias OP=rot

Cmnd_Alias SYDCMD=/bin/chown,/bin/chmod,/usr/sbin/aduser,/usr/bin/paswd [A-Zaz]*,!/usr/bin/paswd rot

Cmnd_Alias DSKCMD=/sbin/parted,/sbin/fdisk 注：定义命令别名DSKCMD，下有成员parted和fdisk ；

SYSADER AL= SYDCMD,DSKCMDISKADER AL=(OP) DSKCMD

注解：

第⼀⾏：定义⽤户别名SYSADER 下有成员 beinan、linuxsir和beinan⽤户组下的成员，⽤户组前⾯必 须加%号；

第⼆⾏：定义⽤户别名 DISKADER ，成员有lanhaitun

第三⾏：定义Runas⽤户，也就是⽬标⽤户的别名为OP，下有成员rot

第四⾏：定义SYSCMD命令别名，成员之间⽤,号分隔，最后的!/usr/bin/paswd rot 表示不能通过 paswd 来更改rot密码；

第五⾏：定义命令别名DSKCMD，下有成员parted和fdisk ；

第六⾏：表示授权SYSADER下的任何成员，在任何可能存在的主机名的主机下运⾏或禁⽌ SYDCMD 和DSKCMD下定义的命令。更为明确遥说，beinan、linuxsir和beinan⽤户组下的成员能以rot身份运 ⾏ chown 、chmod 、aduser、paswd，但不能更改rot的密码；也能够以rot身份运⾏ parted和 fdisk ，本条规则的等价规则是；

beinan,linuxsir,%beinan AL=/bin/chown,/bin/chmod,/usr/sbin/aduser,/usr/bin/paswd [A-Zaz]*,!/usr/bin/paswd rot,/sbin/parted,/sbin/fdisk

第七⾏：表示授权DISKADER 下的任何成员，能以OP的身份，来运⾏ DSKCMD ，⽆需密码；更为明 确的说 lanhaitun 能以rot身份运⾏ parted和fdisk 命令；其等价规则是：

lanhaitun AL=(rot) /sbin/parted,/sbin/fdisk

可能有的弟兄会说我想不输⼊⽤户的密码就能转换到rot并运⾏SYDCMD和DSKCMD 下的命令，那应 该把把NOPASWD:加在哪⾥为好？理解下⾯的例⼦吧，能明⽩的；

SYSADER AL= NOPASWD: SYDCMD, NOPASWD: DSKCMD

