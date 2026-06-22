操作系统中，前台进程和后台进程有什么区别？特征是什么？

后台程序基本上不和⽤户交互，优先级别稍微低⼀点

前台的程序和⽤户交互，需要较⾼的响应速度，优先级别稍微⾼⼀点

直接从后台⼿⼯启动⼀个进程⽤得⽐较少⼀些，除⾮是该进程甚为耗时，且⽤户也不急着需要结 果的时候。假设⽤户要启动⼀个需要⻓时间运⾏的格式化⽂本⽂件的进程。为了不使整个shell在 格式化过程中都处于“瘫痪”状态，从后台启动这个进程是明智的选择。

LINUX后台进程与前台进程的区别

LINUX后台进程也叫守护进程（Daemon），是运⾏在后台的⼀种特殊进程。它独⽴于控制终端并且周期性地执⾏某种任 务或等待处理某些发⽣的事件。

⼀般⽤作系统服务，可以⽤crontab提交，编辑或者删除相应得作业。

守护的意思就是不受终端控制。Linux的⼤多数服务器就是⽤守护进程实现的。⽐如，Internet服务器inetd，Web服务器 httpd等。同时，守护进程完成许多系统任务。⽐如，作业规划进程crond，打印进程lpd等。

前台进程就是⽤户使⽤的有控制终端的进程

# shell下，进程的前台与后台运⾏

跟系统任务相关的⼏个命令：fg、bg、jobs、&、ctrl+z

- 1. & 最经常被⽤到

这个⽤在⼀个命令的最后，可以把这个命令放到后台执⾏

- 2. ctrl + z

可以将⼀个正在前台执⾏的命令放到后台，并且暂停

- 3. jobs

查看当前有多少在后台运⾏的命令

- 4. fg

将后台中的命令调⾄前台继续运⾏

如果后台中有多个命令，可以⽤ fg %jobnumber将选中的命令调出，%jobnumber是通过jobs命令查到的后台正在执⾏的命 令的序号(不是pid)

- 5. bg 将⼀个在后台暂停的命令，变成继续执⾏


如果后台中有多个命令，可以⽤bg %jobnumber将选中的命令调出，%jobnumber是通过jobs命令查到的后台正在执⾏的命 令的序号(不是pid)

- 1. jobs列举出后台作业信息。（[作业号] 运⾏状态 作业名称）

- 2. ctrl+z 将任务放到后台去，并暂停；


- 3. bg <%int> 将后台任务唤醒,在后台运⾏；

- 4. fg <%int> 将后任务的程序放到前台；


- 1. ctrl+z 将任务放到后台去，并暂停.

主进程waitpid(pid,&status,WUNTRACED)时，⼦进程

退出时，⽗进程被唤醒

- 2. 将后台任务唤醒，在后台运⾏；

kill(pid,SIGCONT);

- 3. 将后台运⾏的程序放到前台；


kill(pid,SIGCONT);

waitpid(pid,&status,WUNTRACED);

//可见，后台运⾏与前台运⾏的区别只在于前台运⾏等待⼦进程的退出⽽阻塞⽗进程操作。⽽后台运⾏时，可以在⽗进 程中输⼊命令继续其他操作。本质上没有区别，都是给⼦进程发送SIGCONT信号。

Configure 参数选项

1 –prefix=<path> - Nginx安装路径。如果没有指定，默认为 /usr/local/nginx。

--help 查看帮助⽂档

Linux ⾃启动程序

下⾯⽤⾃启动apache为例：

有两种⽅法可以让Apache在系统启动时⾃动启动

- 1. 在/etc/rc.d/rc.local中增加启动apache的命令，例如：/usr/local/httpd/bin/apachectl start

- 2. 将apache注册为系统服务


⾸先将apachectl命令拷贝⾄/etc/rc.d/init.d⽬录下，改名为httpd

使⽤编辑器打开httpd⽂件，并在第⼀⾏#!/bin/sh下增加两⾏⽂字如下

# chkconfig: 35 70 30

# description: Apache

接着注册该服务

chkconfig –add httpd

⼀切OK了，启动服务

service httpd start

其中所增加的第⼆⾏中三个数字第⼀个表示在运⾏级别3和5下启动apache，第⼆、三是关于 启动和停⽌的优先级配置，⽆关紧要。

在Red Hat Linux中⾃动运⾏程序

1．开机启动时⾃动运⾏程序

Linux加载后, 它将初始化硬件和设备驱动, 然后运⾏第⼀个进程init。init根据配置⽂件继续引 导过程，启动其它进程。通常情况下，修改放置在 /etc/rc或 /etc/rc.d 或 /etc/rc?.d ⽬录下的脚本⽂ 件，可以使init⾃动启动其它程序。例如：编辑 /etc/rc.d/rc.local ⽂件，在⽂件最末加上⼀⾏”xinit” 或”startx”，可以在开机启动后直接进⼊X－Window。

- 2．登录时⾃动运⾏程序


⽤户登录时，bash⾸先⾃动执⾏系统管理员建⽴的全局登录script ：/etc/profile。然后bash在 ⽤户起始⽬录下按顺序查找三个特殊⽂件中的⼀个：/.bash_profile、/.bash_login、 /.profile，但 只执⾏最先找到的⼀个。

因此，只需根据实际需要在上述⽂件中加⼊命令就可以实现⽤户登录时⾃动运⾏某些程序

- 3．退出登录时⾃动运⾏程序

退出登录时，bash⾃动执⾏个⼈的退出登录脚本/.bash_logout。例如，在/.bash_logout中加 ⼊命令”tar －cvzf c.source.tgz ＊.c”，则在每次退出登录时⾃动执⾏ “tar” 命令备份 ＊.c ⽂件。

- 4．定期⾃动运⾏程序


Linux有⼀个称为crond的守护程序，主要功能是周期性地检查 /var/spool/cron⽬录下的⼀组命 令⽂件的内容，并在设定的时间执⾏这些⽂件中的命令。⽤户可以通过crontab 命令来建⽴、修 改、删除这些命令⽂件。

例如，建⽴⽂件crondFile，内容为”00 9 23 Jan ＊ HappyBirthday”，运⾏”crontab cronFile” 命令后，每当元⽉23⽇上午9:00系统⾃动执⾏”HappyBirthday”的程序（”＊”表⽰不管当天是星期 ⼏）。

Linux启动细节：

1）redhat的启动⽅式和执⾏次序是：

加载内核

执⾏init程序

/etc/rc.d/rc.sysinit # 由init执⾏的第⼀个脚本

/etc/rc.d/rc $RUNLEVEL # $RUNLEVEL为缺省的运⾏模式

/etc/rc.d/rc.local #相应级别服务启动之后、在执⾏该⽂件（其实也可以把需要执⾏的命令写到该⽂件中）

/sbin/mingetty # 等待⽤户登录

在Redhat中，/etc/rc.d/rc.sysinit主要做在各个运⾏模式中相同的初始化⼯作，包括：

调⼊keymap以及系统字体

启动swapping

设置主机名

设置NIS域名

检查（fsck）并mount⽂件系统

打开quota

装载声卡模块

设置系统时钟

/etc/rc.d/rc则根据其参数指定的运⾏模式(运⾏级别，你在inittab⽂件中可以设置)来执⾏相应⽬录下的脚本。凡是以Kxx开头 的，都以stop为参数来调⽤；凡是以Sxx开头的，都以start为参数来调⽤。调⽤的顺序按xx从⼩到⼤来执⾏。(其中xx是数字、 表⽰的是启动顺序)例如，假设缺省的运⾏模式是3，/etc/rc.d/rc就会按上述⽅式调⽤/etc/rc.d/rc3.d/下的脚本。

值得⼀提的是，Redhat中的运⾏模式2、3、5都把/etc/rc.d/rc.local做为初始化脚本中

的最后⼀个，所以⽤户可以⾃⼰在这个⽂件中添加⼀些需要在其他初始化⼯作之后，登录之前执⾏的命令。

init在等待/etc/rc.d/rc执⾏完毕之后（因为在/etc/inittab中/etc/rc.d/rc的action是wait），将在指定的各个虚拟终端上 运/sbin/mingetty，等待⽤户的登录。

⾄此，LINUX的启动结束。

）init运⾏级别及指令

⼀、什么是INIT:

init是Linux系统操作中不可缺少的程序之⼀。

所谓的init进程，它是⼀个由内核启动的⽤户级进程。

内核⾃⾏启动（已经被载⼊内存，开始运⾏，并已初始化所有的设备驱动程序和数据结构等）之后，就通过启动⼀个⽤户级程 序init的⽅式，完成引导进程。所以,init始终是第⼀个进程（其进程编号始终为1）。

内核会在过去曾使⽤过init的⼏个地⽅查找它，它的正确位置（对Linux系统来说）是/sbin/init。如果内核找不到init，它就 会试着运⾏/bin/sh，如果运⾏失败，系统的启动也会失败。

⼆、运⾏级别

那么，到底什么是运⾏级呢？

简单的说，运⾏级就是操作系统当前正在运⾏的功能级别。这个级别从1到6 ，具有不同的功能。

不同的运⾏级定义如下

- # 0 – 停机（千万不能把initdefault 设置为0 ）

- # 1 – 单⽤户模式 # s init s = init 1

- # 2 – 多⽤户，没有 NFS

- # 3 – 完全多⽤户模式(标准的运⾏级)

- # 4 – 没有⽤到

- # 5 – X11 多⽤户图形模式（xwindow)

- # 6 – 重新启动 （千万不要把initdefault 设置为6 ）


这些级别在/etc/inittab ⽂件⾥指定。这个⽂件是init 程序寻找的主要⽂件，最先运⾏的服务是放在/etc/rc.d ⽬录下的⽂ 件。在⼤多数的Linux 发⾏版本中，启动脚本都是位于 /etc/rc.d/init.d中的。这些脚本被⽤ln 命令连接到 /etc/rc.d/rcn.d ⽬ 录。(这⾥的n 就是运⾏级0-6)

3）：chkconfig 命令（redhat 操作系统下）

不像DOS 或者 Windows，Linux 可以有多种运⾏级。常见的就是多⽤户的2,3,4,5 ，很多⼈知道 5 是运⾏ X-Windows 的 级别，⽽ 0 就是关机了。运⾏级的改变可以通过 init 命令来切换。例如，假设你要维护系统进⼊单⽤户状态，那么，可以使 ⽤ init 1 来切换。在 Linux 的运⾏级的切换过程中，系统会⾃动寻找对应运⾏级的⽬录/etc/rc[0-6].d下的K 和 S 开头的⽂件， 按后⾯的数字顺序，执⾏这些脚本。对这些脚本的维护，是很繁琐的⼀件事情，Linux 提供了chkconfig 命令⽤来更新和查询不同 运⾏级上的系统服务。

语法为：

chkconfig –list [name]

chkconfig –add name

chkconfig –del name

chkconfig [--level levels] name

chkconfig [--level levels] name

chkconfig 有五项功能：添加服务，删除服务，列表服务，改变启动信息以及检查特定服务的启动状态。

chkconfig 没有参数运⾏时，显⽰⽤法。如果加上服务名，那么就检查这个服务是否在当前运⾏级启动。如果是，返回 true， 否则返回false。 –level 选项可以指定要查看的运⾏级⽽不⼀定是当前运⾏级。

如果在服务名后⾯指定了on，off 或者 reset，那么 chkconfig 会改变指定服务的启动信息。on 和 off 分别指服务在改变运 ⾏级时的启动和停⽌。reset 指初始化服务信息，⽆论有问题的初始化脚本指定了什么。

对于 on 和 off 开关，系统默认只对运⾏级 3，4， 5有效，但是 reset 可以对所有运⾏级有效。指定 –level 选项时，可以选 择特定的运⾏级。

需要说明的是，对于每个运⾏级，只能有⼀个启动脚本或者停⽌脚本。当切换运⾏级时，init 不会重新启动已经启动的服务， 也不会再次去停⽌已经停⽌的服务。

选项介绍：

- –level levels

指定运⾏级，由数字 0 到 7 构成的字符串，如：

- –level 35 表⽰指定运⾏级3 和5。

要在运⾏级别3、4、5中停运 nfs 服务，使⽤下⾯的命令：chkconfig –level 345 nfs off

- –add name


这个选项增加⼀项新的服务，chkconfig 确保每个运⾏级有⼀项 启动(S) 或者 杀死(K) ⼊⼜。如有缺少，则会从缺省的 init 脚本⾃动建⽴。

- –del name


⽤来删除服务，并把相关符号连接从 /etc/rc[0-6].d 删除。

- –list name


列表，如果指定了name 那么只是显⽰指定的服务名，否则，列出全部服务在不同运⾏级的状态。

运⾏级⽂件

每个被chkconfig 管理的服务需要在对应的init.d 下的脚本加上两⾏或者更多⾏的注释。

第⼀⾏告诉 chkconfig 缺省启动的运⾏级以及启动和停⽌的优先级。如果某服务缺省不在任何运⾏级启动，那么使⽤ – 代替 运⾏级。

第⼆⾏对服务进⾏描述，可以⽤ 跨⾏注释。

例如，random.init 包含三⾏：

# chkconfig: 2345 20 80

# description: Saves and restores system entropy pool for

# higher quality random number generation.

表明 random 脚本应该在运⾏级 2, 3, 4, 5 启动，启动优先权为20，停⽌优先权为 80。

好了，介绍就到这⾥了，去看看⾃⼰⽬录下的/etc/rc.d/init.d 下的脚本吧。

2. 实例介绍：

1、在linux下安装了apache 服务（通过下载⼆进制⽂件经济编译安装、⽽⾮rpm包）、apache 服务启动命 令： /server/apache/bin/apachectl start 。让apache服务运⾏在运⾏级别3下⾯。 命令如下：

1）touch /etc/rc.d/init.d/apache

vi /etc/rc.d/init.d/apache

chown -R root /etc/rc.d/init.d/apache

chmod 700 /etc/rc.d/init.d/apache

ln -s /etc/rc.d/init.d/apache /etc/rc.d/rc3.d/S60apache #S 是start的简写、代表启动、K是kill的简写、代表关闭。 60数字代表启动的顺序。

apache的内容：

#!/bin/bash

#Start httpd service

/server/apache/bin/apachectl start

⾄此 apache服务就可以在运⾏级别3下 随机⾃动启动了。（可以结合chkconfig 对启动服务进⾏相应的调整）

