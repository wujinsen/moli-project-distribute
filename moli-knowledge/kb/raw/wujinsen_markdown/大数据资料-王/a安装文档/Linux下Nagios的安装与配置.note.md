⼀、Nagios简介

Nagios是⼀款开源的电脑系统和⽹络监视⼯具，能有效监控Windows、Linux和Unix的主机状态， 交换机路由器等⽹络设置，打印机等。在系统或服务状态异常时发出邮件或短信报警第⼀时间通知⽹ 站运维⼈员，在状态恢复后发出正常的邮件或短信通知。

Nagios原名为NetSaint，由Ethan Galstad开发并维护⾄今。NAGIOS是⼀个缩写形式: "Nagios Ain't Gona Insist On Sainthod" Sainthod 翻译为圣徒，⽽"Agios"是"saint"的希腊表示⽅法。 Nagios被开发在Linux下使⽤，但在Unix下也⼯作得⾮常好。 主要功能

⽹络服务监控（SMTP、POP3、HTP、 NTP、ICMP、SNMP、FTP、 SH）

主机资源监控（CPU load、disk usage、system logs），也包括Windows主机（使⽤NSClient+ plugin）

可以指定⾃⼰编写的Plugin通过⽹络收集数据来监控任何情况（温度、警告 …）

可以通过配置Nagios远程执⾏插件远程执⾏脚本

远程监控⽀持 SH或 SL加通道⽅式进⾏监控

简单的plugin设计允许⽤户很容易的开发⾃⼰需要的检查服务，⽀持很多开发语⾔（shel scripts、 C+、Perl、ruby、Python、PHP、C#等）

包含很多图形化数据Plugins（Nagiosgraph、Nagiosgrapher、PNP4Nagios等）

可并⾏服务检查

能够定义⽹络主机的层次，允许逐级检查，就是从⽗主机开始向下检查

当服务或主机出现问题时发出通告，可通过email, pager, sms 或任意⽤户⾃定义的plugin进⾏通知

能够⾃定义事件处理机制重新激活出问题的服务或主机

⾃动⽇志循环 ⽀持冗余监控 包括Web界⾯可以查看当前⽹络状态，通知，问题历史，⽇志⽂件等

⼆、Nagios⼯作原理

Nagios的功能是监控服务和主机，但是他⾃身并不包括这部分功能，所有的监控、检测功能都是 通过各种插件来完成的。

启动Nagios后，它会周期性的⾃动调⽤插件去检测服务器状态，同时Nagios会维持⼀个队列，所 有插件返回来的状态信息都进⼊队列，Nagios每次都从队⾸开始读取信息，并进⾏处理后，把状态结 果通过web显示出来。

Nagios提供了许多插件，利⽤这些插件可以⽅便的监控很多服务状态。安装完成后，在nagios主 ⽬录下的/libexec⾥放有nagios⾃带的可以使⽤的所有插件，如，check_disk是检查磁盘空间的插件， check_load是检查CPU负载的，等等。每⼀个插件可以通过运⾏./check_ x –h 来查看其使⽤⽅法和 功能。

Nagios可以识别4种状态返回信息，即 0(OK)表示状态正常/绿⾊、1(WARNING)表示出现警告/⻩ ⾊、2(CRITICAL)表示出现⾮常严重的错误/红⾊、3(UNKNOWN)表示未知错误/深⻩⾊。Nagios根据插 件返回来的值，来判断监控对象的状态，并通过web显示出来，以供管理员及时发现故障。 四种监控状态

再说报警功能，如果监控系统发现问题不能报警那就没有意义了，所以报警也是nagios很重要的 功能之⼀。但是，同样的，Nagios ⾃身也没有报警部分的代码，甚⾄没有插件，⽽是交给⽤户或者其 他相关开源项⽬组去完成的。

Nagios 安装，是指基本平台，也就是Nagios软件包的安装。它是监控体系的框架，也是所有监控 的基础。

打开Nagios官⽅的⽂档，会发现Nagios基本上没有什么依赖包，只要求系统是Linux或者其他 Nagios⽀持的系统。不过如果你没有安装apache（htp服务），那么你就没有那么直观的界⾯来查看 监控信息了，所以apache姑且算是⼀个前提条件。关于apache的安装，⽹上有很多，照着安装就是 了。安装之后要检查⼀下是否可以正常⼯作。

知道Nagios 是如何通过插件来管理服务器对象后，现在开始研究它是如何管理远端服务器对象 的。Nagios 系统提供了⼀个插件NRPE。Nagios 通过周期性的运⾏它来获得远端服务器的各种状态信 息。它们之间的关系如下图所示：

Nagios 通过NRPE 来远端管理服务

- 1. Nagios 执⾏安装在它⾥⾯的check_nrpe 插件，并告诉check_nrpe 去检测哪些服务。
- 2. 通过 SL，check_nrpe 连接远端机⼦上的NRPE daemon
- 3. NRPE 运⾏本地的各种插件去检测本地的服务和状态(check_disk,.etc)
- 4. 最后，NRPE 把检测的结果传给主机端的check_nrpe，check_nrpe 再把结果送到Nagios状态队列 中。
- 5. Nagios 依次读取队列中的信息，再把结果显示出来。 三、实验环境


<table>
  <tr>
    <th>Host Name</th>
    <th>OS</th>
    <th>IP</th>
    <th>Software</th>
  </tr>
  <tr>
    <td>Nagios-Server</td>
    <td>CentOS release 6.3 (Final)</td>
    <td>192.168.1.108</td>
    <td>Apache、Php、 Nagios、nagios-</td>
  </tr>
  <tr>
    <td>Nagios-Linux</td>
    <td>CentOS release 5.8</td>
    <td>192.168.1. 1</td>
    <td>plugins nagios-plugins、nrpe</td>
  </tr>
  <tr>
    <td> </td>
    <td>(Final)</td>
    <td> </td>
    <td> </td>
  </tr>
</table>


Nagios-Windows Windows XP 192.168.1.13 NSClient+

Server 安装了nagios软件，对监控的数据做处理，并且提供web界⾯查看和管理。当然也可以对本机 ⾃身的信息进⾏监控。 Client 安装了NRPE等客户端，根据监控机的请求执⾏监控，然后将结果回传给监控机。

防⽕墙已关闭/iptables: Firewal is not runing. SELINUX=disabled 四、实验⽬标

![image 1](<Linux下Nagios的安装与配置.note_images/imageFile1.png>)

五、Nagios服务端安装

- 5.1 基础⽀持套件：gc glibc glibc-comon gd gd-devel xinetd opensl-devel # rpm -q gc glibc glibc-comon gd gd-devel xinetd opensl-devel

如果系统中没有这些套件，使⽤yum 安装 # yum instal -y gc glibc glibc-comon gd gd-devel xinetd opensl-devel

- 5.2 创建nagios⽤户和⽤户组


- 1 # useradd -s /sbin/nologin nagios

- 2 # mkdir /usr/local/nagios

- 3 # chown -R nagios.nagios /usr/local/nagios


查看nagios ⽬录的权限 #l -d /usr/local/nagios/

- 5.3 编译安装Nagios # wget # tar zxvf nagios-3.4.3.tar.gz # cd nagios # ./configure-prefix=/usr/local/nagios

# make al

# make instal

# make instal-init

# make instal-comandmode

# make instal-config

# chkconfig-ad nagios # chkconfig-level 35 nagios on # chkconfig-list nagios

- 5.4 验证程序是否被正确安装 切换⽬录到安装路径（这⾥是/usr/local/nagios），看是否存在etc、bin、sbin、share、var 这五个⽬ 录，如果存在则可以表明程序被正确的安装到系统了。Nagios 各个⽬录⽤途说明如下：


htp:/prdownloads.sourceforge.net/sourceforge/nagios/nagios-3.4.3.tar.gz

<table>
  <tr>
    <th>bin</th>
    <th>Nagios 可执⾏程序所在⽬录</th>
  </tr>
  <tr>
    <td>etc</td>
    <td>Nagios 配置⽂件所在⽬录</td>
  </tr>
  <tr>
    <td>sbin</td>
    <td>Nagios CGI ⽂件所在⽬录，也就是执⾏外部命令 所需⽂件所在的⽬录</td>
  </tr>
  <tr>
    <td>share</td>
    <td>Nagios⽹⻚⽂件所在的⽬录</td>
  </tr>
  <tr>
    <td>libexec</td>
    <td>Nagios 外部插件所在⽬录</td>
  </tr>
  <tr>
    <td>var</td>
    <td>Nagios ⽇志⽂件、lock 等⽂件所在的⽬录</td>
  </tr>
  <tr>
    <td>var/archives</td>
    <td>Nagios ⽇志⾃动归档⽬录</td>
  </tr>
  <tr>
    <td> </td>
    <td>⽤来存放外部命令⽂件的⽬录</td>
  </tr>
</table>


var/rw

- 5.5 安装Nagios 插件 # wget # tar zxvf nagios-plugins-1.4.16.tar.gz # cd nagios-plugins-1.4.16 # ./configure-prefix=/usr/local/nagios # make & make instal
- 5.6 安装与配置Apache和Php Apache 和Php 不是安装nagios 所必须的，但是nagios提供了web监控界⾯，通过web监控界⾯可以清 晰的看到被监控主机、资源的运⾏状态，因此，安装⼀个web服务是很必要的。 需要注意的是，nagios在nagios3.1.x版本以后，配置web监控界⾯时需要php的⽀持。这⾥我们下载的 nagios版本为nagios-3.4.3，因此在编译安装完成apache后，还需要编译php模块，这⾥选取的php版 本为php5.4.10。


htp:/prdownloads.sourceforge.net/sourceforge/nagiosplug/nagios-plugins-1.4.16.tar.gz

- a. 安装Apache # wget # tar zxvf htpd-2.2.23.tar.gz # cd htpd-2.2.23 # ./configure-prefix=/usr/local/apache2 # make & make instal


htp:/archive.apache.org/dist/htpd/htpd-2.2.23.tar.gz

若出现错误：

![image 2](<Linux下Nagios的安装与配置.note_images/imageFile2.png>)

则在编译时⼊加 -with-included-apr 即可解决。

- b. 安装Php # wget # tar zxvf php-5.4.10.tar.gz # cd php-5.4.10 # ./configure-prefix=/usr/local/php-with-apxs2=/usr/local/apache2/bin/apxs

# make & make instal

- c. 配置apache 找到apache 的配置⽂件/usr/local/apache2/conf/htpd.conf 找到：


htp:/cn2.php.net/distributions/php-5.4.10.tar.gz

- 1 User daemon

- 2 Group daemon


修改为

- 1 User nagios

- 2 Group nagios


然后找到

- 1 <IfModule dir_module>

- 2 DirectoryIndex index.html

- 3 </IfModule>


修改为

- 1 <IfModule dir_module>

- 2 DirectoryIndex index.html index.php

- 3 </IfModule>


接着增加如下内容：

1 AddType application/x-httpd-php .php

为了安全起⻅，⼀般情况下要让nagios 的web 监控⻚⾯必须经过授权才能访问，这需要增加验证配 置，即在htpd.conf ⽂件最后添加如下信息：

![image 3](<Linux下Nagios的安装与配置.note_images/imageFile3.png>)

- 1 #setting for nagios

- 2 ScriptAlias /nagios/cgi-bin "/usr/local/nagios/sbin"

- 3 <Directory "/usr/local/nagios/sbin">

- 4 AuthType Basic

- 5 Options ExecCGI

- 6 AllowOverride None

- 7 Order allow,deny

- 8 Allow from all

- 9 AuthName "Nagios Access"

AuthUserFile /usr/local/nagios/etc/nagiospasswd //⽤于此⽬录访问 身份验证的⽂件

- 10

- 11 Require valid-user

- 12 </Directory>

- 13 Alias /nagios "/usr/local/nagios/share"

- 14 <Directory "/usr/local/nagios/share">

- 15 AuthType Basic

- 16 Options None

- 17 AllowOverride None

- 18 Order allow,deny

- 19 Allow from all

- 20 AuthName "nagios Access"

- 21 AuthUserFile /usr/local/nagios/etc/nagiospasswd

- 22 Require valid-user

- 23 </Directory>


![image 4](<Linux下Nagios的安装与配置.note_images/imageFile4.png>)

- d. 创建apache⽬录验证⽂件 在上⾯的配置中，指定了⽬录验证⽂件htpaswd，下⾯要创建这个⽂件： #htpaswd -c /usr/local/nagios/etc/nagiospaswd nagios

这样就在/usr/local/nagios/etc ⽬录下创建了⼀个nagiospaswd验证⽂件，当通过 访问时就需要输⼊⽤户名和密码了。

- e. 查看认证⽂件的内容 # cat /usr/local/nagios/etc/htpaswd
- f. 启动apache 服务 # /usr/local/apache2/bin/apachectl start 到这⾥nagios 的安装也就基本完成了，你可以通过web来访问了。


htp:/192.168.1.108/ nagios/

![image 5](<Linux下Nagios的安装与配置.note_images/imageFile5.png>)

![image 6](<Linux下Nagios的安装与配置.note_images/imageFile6.png>)

六、配置Nagios Nagios 主要⽤于监控⼀台或者多台本地主机及远程的各种信息，包括本机资源及对外的服务等。默认 的Nagios 配置没有任何监控内容，仅是⼀些模板⽂件。若要让Nagios 提供服务，就必须修改配置⽂ 件，增加要监控的主机和服务，下⾯将详细介绍。

- 6.1 默认配置⽂件介绍 Nagios 安装完毕后，默认的配置⽂件在/usr/local/nagios/etc⽬录下。 每个⽂件或⽬录含义如下表所示：
- 6.2 配置⽂件之间的关系 在nagios的配置过程中涉及到的⼏个定义有：主机、主机组，服务、服务组，联系⼈、联系⼈组，监 控时间，监控命令等，从这些定义可以看出，nagios各个配置⽂件之间是互为关联，彼此引⽤的。 成功配置出⼀台nagios监控系统，必须要弄清楚每个配置⽂件之间依赖与被依赖的关系，最重要的有 四点： 第⼀：定义监控哪些主机、主机组、服务和服务组；


<table>
  <tr>
    <th>⽂件名或⽬录名</th>
    <th>⽤途</th>
  </tr>
  <tr>
    <td>cgi.cfg</td>
    <td>控制CGI访问的配置⽂件</td>
  </tr>
  <tr>
    <td>nagios.cfg</td>
    <td>Nagios 主配置⽂件</td>
  </tr>
  <tr>
    <td>resource.cfg</td>
    <td>变量定义⽂件，⼜称为资源⽂件，在些⽂件中定</td>
  </tr>
  <tr>
    <td>objects</td>
    <td>义变量，以便由其他配置⽂件引⽤，如$USER1$ objects 是⼀个⽬录，在此⽬录下有很多配置⽂件<br><br>对象</td>
  </tr>
  <tr>
    <td>objects/comands.cfg</td>
    <td>模板，⽤于定义Nagios 命令定义配置⽂件，其中定义的命令可以被其他 配置⽂件引⽤</td>
  </tr>
  <tr>
    <td>objects/contacts.cfg</td>
    <td>定义联系⼈和联系⼈组的配置⽂件</td>
  </tr>
  <tr>
    <td>objects/localhost.cfg</td>
    <td>定义监控本地主机的配置⽂件</td>
  </tr>
  <tr>
    <td>objects/printer.cfg</td>
    <td>定义监控打印机的⼀个配置⽂件模板，默认没有 启⽤此⽂件</td>
  </tr>
  <tr>
    <td>objects/switch.cfg</td>
    <td>定义监控路由器的⼀个配置⽂件模板，默认没有 启⽤此⽂件</td>
  </tr>
  <tr>
    <td>objects/templates.cfg</td>
    <td>定义主机和服务的⼀个模板配置⽂件，可以在其 他配置⽂件中引⽤</td>
  </tr>
  <tr>
    <td>objects/timeperiods.cfg</td>
    <td>定义Nagios 监控时间段的配置⽂件</td>
  </tr>
  <tr>
    <td>objects/windows.cfg</td>
    <td>监控Windows 主机的⼀个配置⽂件模板，默认没 有启⽤此⽂件</td>
  </tr>
</table>


第⼆：定义这个监控要⽤什么命令实现； 第三：定义监控的时间段； 第四：定义主机或服务出现问题时要通知的联系⼈和联系⼈组。

- 6.3 配置Nagios 为了能更清楚的说明问题，同时也为了维护⽅便，建议将nagios各个定义对象创建独⽴的配置⽂件：


创建hosts.cfg⽂件来定义主机和主机组

创建services.cfg⽂件来定义服务

⽤默认的contacts.cfg⽂件来定义联系⼈和联系⼈组

⽤默认的comands.cfg⽂件来定义命令

⽤默认的timeperiods.cfg来定义监控时间段

⽤默认的templates.cfg⽂件作为资源引⽤⽂件

- a. templates.cfg⽂件 nagios主要⽤于监控主机资源以及服务，在nagios配置中称为对象，为了不必重复定义⼀些监控对 象，Nagios引⼊了⼀个模板配置⽂件，将⼀些共性的属性定义成模板，以便于多次引⽤。这就是 templates.cfg的作⽤。 下⾯详细介绍下templates.cfg⽂件中每个参数的含义：


![image 7](<Linux下Nagios的安装与配置.note_images/imageFile7.png>)

- 1 define contact{

- 2 name generic-contact ; 联系⼈名称

service_notification_period 24x7 ; 当服务出现异常时，发送 通知的时间段，这个时间段"24x7"在timeperiods.cfg⽂件中定义

- 3

host_notification_period 24x7 ; 当主机出现异常时，发送 通知的时间段，这个时间段"24x7"在timeperiods.cfg⽂件中定义

- 4

service_notification_options w,u,c,r ; 这个定义的是“通知可以 被发出的情况”。w即warn，表示警告状态，u即unknown，表示不明状态;

- 5

; c即criticle，表示紧急 状态，r即recover，表示恢复状态;

- 6

; 也就是在服务出现警告状 态、未知状态、紧急状态和重新恢复状态时都发送通知给使⽤者。

- 7

host_notification_options d,u,r ; 定义主机在什么状 态下需要发送通知给使⽤者，d即down，表示宕机状态;

- 8

; u即 unreachable，表示不可到达状态，r即recovery，表示重新恢复状态。

- 9

service_notification_commands notify-service-by-email ; 服务故障时，发送 通知的⽅式，可以是邮件和短信，这⾥发送的⽅式是邮件;

- 10

; 其中“notifyservice-by-email”在commands.cfg⽂件中定义。

- 11

host_notification_commands notify-host-by-email ; 主机故障时，发送 通知的⽅式，可以是邮件和短信，这⾥发送的⽅式是邮件;

- 12

; 其中“notifyhost-by-email”在commands.cfg⽂件中定义。

- 13

register 0 ; DONT REGISTER THIS DEFINITION - ITS NOT A REAL CONTACT, JUST A TEMPLATE!

- 14

- 15 }

- 16 define host{

name generic-host ; 主机名称，这⾥的主机名，并 不是直接对应到真正机器的主机名;

- 17

; 乃是对应到在主机配置⽂件⾥ 所设定的主机名。

- 18

notifications_enabled 1 ; Host notifications are enabled

- 19

event_handler_enabled 1 ; Host event handler is enabled

- 20

flap_detection_enabled 1 ; Flap detection is enabled

- 21

failure_prediction_enabled 1 ; Failure prediction is enabled

- 22

process_perf_data 1 ; 其值可以为0或1，其作⽤为是 否启⽤Nagios的数据输出功能;

- 23

; 如果将此项赋值为1，那么 Nagios就会将收集的数据写⼊某个⽂件中，以备提取。

- 24

retain_status_information 1 ; Retain status information across program restarts

- 25


- retain_nonstatus_information 1 ; Retain non-status information across program restarts
- 26

notification_period 24x7 ; 指定“发送通知”的时间段，也 就是可以在什么时候发送通知给使⽤者。

- 27

register 0 ; DONT REGISTER THIS DEFINITION - ITS NOT A REAL HOST, JUST A TEMPLATE!

- 28

- 29 }

- 30 define host{

- 31 name linux-server ; 主机名称

use generic-host ; use表示引⽤，也就是将主机 generic-host的所有属性引⽤到linux-server中来;

- 32

; 在nagios配置中，很多情况 下会⽤到引⽤。

- 33

check_period 24x7 ; 这⾥的check_period告诉 nagios检查主机的时间段

- 34

check_interval 5 ; nagios对主机的检查时间间 隔，这⾥是5分钟。

- 35

retry_interval 1 ; 重试检查时间间隔，单位是分 钟。

- 36

max_check_attempts 10 ; nagios对主机的最⼤检查次 数，也就是nagios在检查发现某主机异常时，并不⻢上判断为异常状况;

- 37

; ⽽是多试⼏次，因为有可能只 是⼀时⽹络太拥挤，或是⼀些其他原因，让主机受到了⼀点影响;

- 38

; 这⾥的10就是最多试10次的 意思。

- 39

check_command check-host-alive ; 指定检查主机状态的命令， 其中“check-host-alive”在commands.cfg⽂件中定义。

- 40

notification_period 24x7 ; 主机故障时，发送通知的时间 范围，其中“workhours”在timeperiods.cfg中进⾏了定义;

- 41

- 42 ; 下⾯会陆续讲到。

notification_interval 10 ; 在主机出现异常后，故障⼀直 没有解决，nagios再次对使⽤者发出通知的时间。单位是分钟;

- 43

; 如果你觉得，所有的事件只需 要⼀次通知就够了，可以把这⾥的选项设为0

- 44

notification_options d,u,r ; 定义主机在什么状态下可以发 送通知给使⽤者，d即down，表示宕机状态;

- 45

; u即unreachable，表示不可 到达状态;

- 46

; r即recovery，表示重新恢 复状态。

- 47

contact_groups ts ; 指定联系⼈组，这个 “admins”在contacts.cfg⽂件中定义。

- 48

register 0 ; DONT REGISTER THIS DEFINITION - ITS NOT A REAL HOST, JUST A TEMPLATE!

- 49

- 50 }

- 51 define host{


- 52 name windows-server ; The name of this host template

use generic-host ; Inherit default values from the generic-host template

- 53

check_period 24x7 ; By default, Windows servers are monitored round the clock

- 54

check_interval 5 ; Actively check the server every 5 minutes

- 55

retry_interval 1 ; Schedule host check retries at 1 minute intervals

- 56

max_check_attempts 10 ; Check each server 10 times (max)

- 57

check_command check-host-alive ; Default command to check if servers are "alive"

- 58

notification_period 24x7 ; Send notification out at any time - day or night

- 59

notification_interval 10 ; Resend notifications every 30 minutes

- 60

notification_options d,r ; Only send notifications for specific host states

- 61

contact_groups ts ; Notifications get sent to the admins by default

- 62

hostgroups windows-servers ; Host groups that Windows servers should be a member of

- 63

register 0 ; DONT REGISTER THIS - ITS JUST A TEMPLATE

- 64

- 65 }

- 66 define service{

- 67 name generic-service ; 定义⼀个服务名称

active_checks_enabled 1 ; Active service checks are enabled

- 68

passive_checks_enabled 1 ; Passive service checks are enabled/accepted

- 69

parallelize_check 1 ; Active service checks should be parallelized;

- 70

; (disabling this can lead to major performance problems)

- 71

obsess_over_service 1 ; We should obsess over this service (if necessary)

- 72

check_freshness 0 ; Default is to NOT check service 'freshness'

- 73

notifications_enabled 1 ; Service notifications are enabled

- 74

event_handler_enabled 1 ; Service event handler is enabled

- 75

flap_detection_enabled 1 ; Flap detection is enabled

- 76


- failure_prediction_enabled 1 ; Failure prediction is enabled
- 77

process_perf_data 1 ; Process performance data

- 78

retain_status_information 1 ; Retain status information across program restarts

- 79

retain_nonstatus_information 1 ; Retain nonstatus information across program restarts

- 80

is_volatile 0 ; The service is not volatile

- 81

check_period 24x7 ; 这⾥的check_period告诉 nagios检查服务的时间段。

- 82

max_check_attempts 3 ; nagios对服务的最⼤检查次 数。

- 83

normal_check_interval 5 ; 此选项是⽤来设置服务检查 时间间隔，也就是说，nagios这⼀次检查和下⼀次检查之间所隔的时间;

- 84

- 85 ; 这⾥是5分钟。

retry_check_interval 2 ; 重试检查时间间隔，单位是 分钟。

- 86

- 87 contact_groups ts ; 指定联系⼈组

notification_options w,u,c,r ; 这个定义的是“通知可以被发 出的情况”。w即warn，表示警告状态;

- 88

; u即unknown，表示不明状 态;

- 89

; c即criticle，表示紧急状 态，r即recover，表示恢复状态;

- 90

; 也就是在服务出现警告状 态、未知状态、紧急状态和重新恢复后都发送通知给使⽤者。

- 91

notification_interval 10 ; Re-notify about service problems every hour

- 92

notification_period 24x7 ; 指定“发送通知”的时间段， 也就是可以在什么时候发送通知给使⽤者。

- 93

register 0 ; DONT REGISTER THIS DEFINITION - ITS NOT A REAL SERVICE, JUST A TEMPLATE!

- 94

- 95 }

- 96 define service{

name local-service ; The name of this service template

- 97

use generic-service ; Inherit default values from the generic-service definition

- 98

max_check_attempts 4 ; Re-check the service up to 4 times in order to determine its final (hard) state

- 99

normal_check_interval 5 ; Check the service every 5 minutes under normal conditions

- 100

retry_check_interval 1 ; Re-check the service every minute until a hard state can be determined

- 101


register 0 ; DONT REGISTER THIS DEFINITION - ITS NOT A REAL SERVICE, JUST A TEMPLATE!

- 102

- 103 }


![image 8](<Linux下Nagios的安装与配置.note_images/imageFile8.png>)

- b. resource.cfg⽂件 resource.cfg是nagios的变量定义⽂件，⽂件内容只有⼀⾏：

其中，变量$USER1$指定了安装nagios插件的路径，如果把插件安装在了其它路径，只需在这⾥进⾏ 修改即可。需要注意的是，变量必须先定义，然后才能在其它配置⽂件中进⾏引⽤。

- c. comands.cfg⽂件 此⽂件默认是存在的，⽆需修改即可使⽤，当然如果有新的命令需要加⼊时，在此⽂件进⾏添加即 可。


1 $USER1$=/usr/local/nagios/libexec

![image 9](<Linux下Nagios的安装与配置.note_images/imageFile9.png>)

- 1 #notify-host-by-email命令的定义

- 2 define command{

command_name notify-host-by-email #命令名称，即定义了⼀个主机 异常时发送邮件的命令。

- 3

command_line /usr/bin/printf "%b" "***** Nagios

*****\n\nNotification Type: $NOTIFICATIONTYPE$\nHost: $HOSTNAME$\nState: $HOSTSTATE$\nAddress: $HOSTADDRESS$\nInfo: $HOSTOUTPUT$\n\nDate/Time: $LONGDATETIME$\n" | /bin/mail -s "** $NOTIFICATIONTYPE$ Host Alert: $HOSTNAME$ is $HOSTSTATE$ **" $CONTACTEMAIL$ #命令具体的 执⾏⽅式。

- 4

- 5 }

- 6 #notify-service-by-email命令的定义

- 7 define command{

command_name notify-service-by-email #命令名称，即定义了⼀个服务 异常时发送邮件的命令

- 8

command_line /usr/bin/printf "%b" "***** Nagios

*****\n\nNotification Type: $NOTIFICATIONTYPE$\n\nService: $SERVICEDESC$\nHost: $HOSTALIAS$\nAddress: $HOSTADDRESS$\nState: $SERVICESTATE$\n\nDate/Time: $LONGDATETIME$\n\nAdditional Info:\n\n$SERVICEOUTPUT$\n" | /bin/mail -s "** $NOTIFICATIONTYPE$ Service Alert: $HOSTALIAS$/$SERVICEDESC$ is $SERVICESTATE$

**" $CONTACTEMAIL$

- 9

- 10 }

- 11 #check-host-alive命令的定义

- 12 define command{

command_name check-host-alive #命令名称，⽤来检测主机状 态。

- 13

command_line $USER1$/check_ping -H $HOSTADDRESS$ -w 3000.0,80% -c 5000.0,100% -p 5

- 14

# 这⾥的变量$USER1$在resource.cfg⽂件中进⾏定义，即 $USER1$=/usr/local/nagios/libexec;

- 15

# 那么check_ping的完整路径 为/usr/local/nagios/libexec/check_ping;

- 16

# “-w 3000.0,80%”中“-w”说明后⾯的⼀对值对应的是“WARNING”状 态，“80%”是其临界值。

- 17

# “-c 5000.0,100%”中“-c”说明后⾯的⼀对值对应的是“CRITICAL”， “100%”是其临界值。

- 18

- 19 # “-p 1”说明每次探测发送⼀个包。

- 20 }

- 21 define command{

- 22 command_name check_local_disk

command_line $USER1$/check_disk -w $ARG1$ -c $ARG2$ -p $ARG3$ #$ARG1$是指在调⽤这个命令的时候，命令后⾯的第⼀个参数。

- 23

- 24 }

- 25 define command{

- 26 command_name check_local_load


- 27 command_line $USER1$/check_load -w $ARG1$ -c $ARG2$

- 28 }

- 29 define command{

- 30 command_name check_local_procs

- 31 command_line $USER1$/check_procs -w $ARG1$ -c $ARG2$ -s $ARG3$

- 32 }

- 33 define command{

- 34 command_name check_local_users

- 35 command_line $USER1$/check_users -w $ARG1$ -c $ARG2$

- 36 }

- 37 define command{

- 38 command_name check_local_swap

- 39 command_line $USER1$/check_swap -w $ARG1$ -c $ARG2$

- 40 }

- 41 define command{

- 42 command_name check_ftp

- 43 command_line $USER1$/check_ftp -H $HOSTADDRESS$ $ARG1$

- 44 }

- 45 define command{

- 46 command_name check_http

- 47 command_line $USER1$/check_http -I $HOSTADDRESS$ $ARG1$

- 48 }

- 49 define command{

- 50 command_name check_ssh

- 51 command_line $USER1$/check_ssh $ARG1$ $HOSTADDRESS$

- 52 }

- 53 define command{

- 54 command_name check_ping

command_line $USER1$/check_ping -H $HOSTADDRESS$ -w $ARG1$ -c $ARG2$

-p 5

- 55

- 56 }

- 57 define command{

- 58 command_name check_nt

command_line $USER1$/check_nt -H $HOSTADDRESS$ -p 12489 -v $ARG1$ $ARG2$

- 59

- 60 }

- 61


![image 10](<Linux下Nagios的安装与配置.note_images/imageFile10.png>)

- d. hosts.cfg⽂件


此⽂件默认不存在，需要⼿动创建，hosts.cfg主要⽤来指定被监控的主机地址以及相关属性信息，根 据实验⽬标配置如下：

![image 11](<Linux下Nagios的安装与配置.note_images/imageFile11.png>)

- 1 define host{

use linux-server #引⽤主机linux-server的属性 信息，linux-server主机在templates.cfg⽂件中进⾏了定义。

- 2

- 3 host_name Nagios-Linux #主机名

- 4 alias Nagios-Linux #主机别名

address 192.168.1.111 #被监控的主机地址，这个地址可以 是ip，也可以是域名。

- 5

- 6 }


- 1 #定义⼀个主机组

- 2 define hostgroup{

- 3 hostgroup_name bsmart-servers #主机组名称，可以随意指定。

- 4 alias bsmart servers #主机组别名

members Nagios-Linux #主机组成员，其中“NagiosLinux”就是上⾯定义的主机。

- 5

- 6 }


![image 12](<Linux下Nagios的安装与配置.note_images/imageFile12.png>)

注意：在/usr/local/nagios/etc/objects 下默认有localhost.cfg 和windows.cfg 这两个配置⽂件， localhost.cfg ⽂件是定义监控主机本身的，windows.cfg ⽂件是定义windows 主机的，其中包括了对 host 和相关services 的定义。所以在本次实验中，将直接在localhost.cfg 中定义监控主机（NagiosServer），在windows.cfg中定义windows 主机（Nagios-Windows）。根据⾃⼰的需要修改其中的相 关配置，详细如下： localhost.cfg

![image 13](<Linux下Nagios的安装与配置.note_images/imageFile13.png>)

use linux-server ; Name of host template to use

- 2

; This host definition will inherit all variables that are defined

- 3

; in (or inherited by) the linux-server host template definition.

- 4

- 5 host_name Nagios-Server

- 6 alias Nagios-Server

- 7 address 127.0.0.1

- 8 }

- 9 define hostgroup{

- 10 hostgroup_name linux-servers ; The name of the hostgroup

- 11 alias Linux Servers ; Long name of the group

members Nagios-Server ; Comma separated list of hosts that belong to this group

- 12

- 13 }

- 14 define service{

use local-service ; Name of service template to use

- 15

- 16 host_name Nagios-Server

- 17 service_description PING

- 18 check_command check_ping!100.0,20%!500.0,60%

- 19 }

- 20 define service{

use local-service ; Name of service template to use

- 21

- 22 host_name Nagios-Server

- 23 service_description Root Partition

- 24 check_command check_local_disk!20%!10%!/

- 25 }

- 26 define service{

use local-service ; Name of service template to use

- 27

- 28 host_name Nagios-Server

- 29 service_description Current Users

- 30 check_command check_local_users!20!50

- 31 }

- 32 define service{

use local-service ; Name of service template to use

- 33

- 34 host_name Nagios-Server


- 35 service_description Total Processes

- 36 check_command check_local_procs!250!400!RSZDT

- 37 }

- 38 define service{

use local-service ; Name of service template to use

- 39

- 40 host_name Nagios-Server

- 41 service_description Current Load

check_command check_local_load!5.0,4.0,3.0!10.0,6.0,4.0

- 42

- 43 }

- 44 define service{

use local-service ; Name of service template to use

- 45

- 46 host_name Nagios-Server

- 47 service_description Swap Usage

- 48 check_command check_local_swap!20!10

- 49 }

- 50 define service{

use local-service ; Name of service template to use

- 51

- 52 host_name Nagios-Server

- 53 service_description SSH

- 54 check_command check_ssh

- 55 notifications_enabled 0

- 56 }

- 57 define service{

use local-service ; Name of service template to use

- 58

- 59 host_name Nagios-Server

- 60 service_description HTTP

- 61 check_command check_http

- 62 notifications_enabled 0

- 63 }


![image 14](<Linux下Nagios的安装与配置.note_images/imageFile14.png>)

windows.cfg

![image 15](<Linux下Nagios的安装与配置.note_images/imageFile15.png>)

- 2 use windows-server ; Inherit default values from a template

- 3 host_name Nagios-Windows ; The name we're giving to this host

alias My Windows Server ; A longer name associated with the host

- 4

- 5 address 192.168.1.113 ; IP address of the host

- 6 }

- 7 define hostgroup{

- 8 hostgroup_name windows-servers ; The name of the hostgroup

- 9 alias Windows Servers ; Long name of the group

- 10 }

- 11 define service{

- 12 use generic-service

- 13 host_name Nagios-Windows

- 14 service_description NSClient++ Version

- 15 check_command check_nt!CLIENTVERSION

- 16 }

- 17 define service{

- 18 use generic-service

- 19 host_name Nagios-Windows

- 20 service_description Uptime

- 21 check_command check_nt!UPTIME

- 22 }

- 23 define service{

- 24 use generic-service

- 25 host_name Nagios-Windows

- 26 service_description CPU Load

- 27 check_command check_nt!CPULOAD!-l 5,80,90

- 28 }

- 29 define service{

- 30 use generic-service

- 31 host_name Nagios-Windows

- 32 service_description Memory Usage

- 33 check_command check_nt!MEMUSE!-w 80 -c 90

- 34 }

- 35 define service{

- 36 use generic-service

- 37 host_name Nagios-Windows

- 38 service_description C:\ Drive Space

- 39 check_command check_nt!USEDDISKSPACE!-l c -w 80 -c 90


- 40 }

- 41 define service{

- 42 use generic-service

- 43 host_name Nagios-Windows

- 44 service_description W3SVC

- 45 check_command check_nt!SERVICESTATE!-d SHOWALL -l W3SVC

- 46 }

- 47 define service{

- 48 use generic-service

- 49 host_name Nagios-Windows

- 50 service_description Explorer

- 51 check_command check_nt!PROCSTATE!-d SHOWALL -l Explorer.exe

- 52 }


![image 16](<Linux下Nagios的安装与配置.note_images/imageFile16.png>)

- e. services.cfg⽂件 此⽂件默认也不存在，需要⼿动创建，services.cfg⽂件主要⽤于定义监控的服务和主机资源，例如监 控htp服务、ftp服务、主机磁盘空间、主机系统负载等等。Nagios-Server 和Nagios-Windows 相关 服务已在相应的配置⽂件中定义，所以这⾥只需要定义Nagios-Linux 相关服务即可，这⾥只定义⼀个 检测是否存活的服务来验证配置⽂件的正确性，其他服务的定义将在后⾯讲到。
- f. contacts.cfg⽂件 contacts.cfg是⼀个定义联系⼈和联系⼈组的配置⽂件，当监控的主机或者服务出现故障，nagios会通 过指定的通知⽅式（邮件或者短信）将信息发给这⾥指定的联系⼈或者使⽤者。


![image 17](<Linux下Nagios的安装与配置.note_images/imageFile17.png>)

- 1 define service{

use local-service #引⽤local-service服务的属 性值，local-service在templates.cfg⽂件中进⾏了定义。

- 2

host_name Nagios-Linux #指定要监控哪个主机上的服务， “Nagios-Server”在hosts.cfg⽂件中进⾏了定义。

- 3

service_description check-host-alive #对监控服务内容的描述，以供维 护⼈员参考。

- 4

- 5 check_command check-host-alive #指定检查的命令。

- 6 }

- 7


![image 18](<Linux下Nagios的安装与配置.note_images/imageFile18.png>)

![image 19](<Linux下Nagios的安装与配置.note_images/imageFile19.png>)

- 1 define contact{

contact_name David #联系⼈的名称,这个地⽅不要 有空格

- 2

use generic-contact #引⽤generic-contact的 属性信息，其中“generic-contact”在templates.cfg⽂件中进⾏定义

- 3

- 4 alias Nagios Admin

- 5 email david.tang@bsmart.cn

- 6 }

- 7


- 1 define contactgroup{

contactgroup_name ts #联系⼈组的名称,同 样不能空格

- 2

- 3 alias Technical Support #联系⼈组描述

members David #联系⼈组成员，其中 “david”就是上⾯定义的联系⼈，如果有多个联系⼈则以逗号相隔

- 4

- 5 }


![image 20](<Linux下Nagios的安装与配置.note_images/imageFile20.png>)

- g. timeperiods.cfg⽂件 此⽂件只要⽤于定义监控的时间段，下⾯是⼀个配置好的实例：


![image 21](<Linux下Nagios的安装与配置.note_images/imageFile21.png>)

- 1 #下⾯是定义⼀个名为24x7的时间段，即监控所有时间段

- 2 define timeperiod{

- 3 timeperiod_name 24x7 #时间段的名称,这个地⽅不要有空格

- 4 alias 24 Hours A Day, 7 Days A Week

- 5 sunday 00:00-24:00

- 6 monday 00:00-24:00

- 7 tuesday 00:00-24:00

- 8 wednesday 00:00-24:00

- 9 thursday 00:00-24:00

- 10 friday 00:00-24:00

- 11 saturday 00:00-24:00

- 12 }

- 13 #下⾯是定义⼀个名为workhours的时间段，即⼯作时间段。

- 14 define timeperiod{

- 15 timeperiod_name workhours

- 16 alias Normal Work Hours

- 17 monday 09:00-17:00

- 18 tuesday 09:00-17:00

- 19 wednesday 09:00-17:00

- 20 thursday 09:00-17:00

- 21 friday 09:00-17:00

- 22 }


![image 22](<Linux下Nagios的安装与配置.note_images/imageFile22.png>)

- h. cgi.cfg⽂件 此⽂件⽤来控制相关cgi脚本，如果想在nagios的web监控界⾯执⾏cgi脚本，例如重启nagios进程、关 闭nagios通知、停⽌nagios主机检测等，这时就需要配置cgi.cfg⽂件了。 由于nagios的web监控界⾯验证⽤户为david，所以只需在cgi.cfg⽂件中添加此⽤户的执⾏权限就可以 了，需要修改的配置信息如下：


![image 23](<Linux下Nagios的安装与配置.note_images/imageFile23.png>)

- 1 default_user_name=david

- 2 authorized_for_system_information=nagiosadmin,david

- 3 authorized_for_configuration_information=nagiosadmin,david

- 4 authorized_for_system_commands=david

- 5 authorized_for_all_services=nagiosadmin,david

- 6 authorized_for_all_hosts=nagiosadmin,david

- 7 authorized_for_all_service_commands=nagiosadmin,david

- 8 authorized_for_all_host_commands=nagiosadmin,david


![image 24](<Linux下Nagios的安装与配置.note_images/imageFile24.png>)

- i. nagios.cfg⽂件 nagios.cfg默认的路径为/usr/local/nagios/etc/nagios.cfg，是nagios的核⼼配置⽂件，所有的对象配置 ⽂件都必须在这个⽂件中进⾏定义才能发挥其作⽤，这⾥只需将对象配置⽂件在Nagios.cfg⽂件中进⾏ 引⽤即可。


![image 25](<Linux下Nagios的安装与配置.note_images/imageFile25.png>)

log_file=/usr/local/nagios/var/nagios.log # 定义nagios⽇志⽂件的 路径

- 1

cfg_file=/usr/local/nagios/etc/objects/commands.cfg # “cfg_file”变量⽤来引 ⽤对象配置⽂件，如果有更多的对象配置⽂件，在这⾥依次添加即可。

- 2

- 3 cfg_file=/usr/local/nagios/etc/objects/contacts.cfg

- 4 cfg_file=/usr/local/nagios/etc/objects/hosts.cfg

- 5 cfg_file=/usr/local/nagios/etc/objects/services.cfg

- 6 cfg_file=/usr/local/nagios/etc/objects/timeperiods.cfg

- 7 cfg_file=/usr/local/nagios/etc/objects/templates.cfg

- 8 cfg_file=/usr/local/nagios/etc/objects/localhost.cfg # 本机配置⽂件

- 9 cfg_file=/usr/local/nagios/etc/objects/windows.cfg # windows 主机配置⽂件

object_cache_file=/usr/local/nagios/var/objects.cache # 该变量⽤于指定⼀个“所 有对象配置⽂件”的副本⽂件，或者叫对象缓冲⽂件

- 10

- 11 precached_object_file=/usr/local/nagios/var/objects.precache

resource_file=/usr/local/nagios/etc/resource.cfg # 该变量⽤于指定nagios 资源⽂件的路径，可以在nagios.cfg中定义多个资源⽂件。

- 12

status_file=/usr/local/nagios/var/status.dat # 该变量⽤于定义⼀个状态 ⽂件，此⽂件⽤于保存nagios的当前状态、注释和宕机信息等。

- 13

status_update_interval=10 # 该变量⽤于定义状态⽂件 （即status.dat）的更新时间间隔，单位是秒，最⼩更新间隔是1秒。

- 14

nagios_user=nagios # 该变量指定了Nagios进 程使⽤哪个⽤户运⾏。

- 15

nagios_group=nagios # 该变量⽤于指定Nagios 使⽤哪个⽤户组运⾏。

- 16

check_external_commands=1 # 该变量⽤于设置是否允许 nagios在web监控界⾯运⾏cgi命令;

- 17

# 也就是是否允许nagios 在web界⾯下执⾏重启nagios、停⽌主机/服务检查等操作;

- 18

# “1”为运⾏，“0”为不允 许。

- 19

command_check_interval=10s # 该变量⽤于设置nagios 对外部命令检测的时间间隔，如果指定了⼀个数字加⼀个"s"(如10s);

- 20

# 那么外部检测命令的间隔 是这个数值以秒为单位的时间间隔;

- 21

# 如果没有⽤"s"，那么外 部检测命令的间隔是以这个数值的“时间单位”的时间间隔。

- 22

interval_length=60 # 该变量指定了nagios的 时间单位，默认值是60秒，也就是1分钟;

- 23

# 即在nagios配置中所有 的时间单位都是分钟。

- 24

- 25


![image 26](<Linux下Nagios的安装与配置.note_images/imageFile26.png>)

- 6.4 验证Nagios 配置⽂件的正确性


Nagios 在验证配置⽂件⽅⾯做的⾮常到位，只需通过⼀个命令即可完成：

1 # /usr/local/nagios/bin/nagios -v /usr/local/nagios/etc/nagios.cfg

![image 27](<Linux下Nagios的安装与配置.note_images/imageFile27.png>)

Nagios提供的这个验证功能⾮常有⽤，在错误信息中通常会打印出错误的配置⽂件以及⽂件中的哪⼀ ⾏，这使得nagios的配置变得⾮常容易，报警信息通常是可以忽略的，因为⼀般那些只是建议性的。 看到上⾯这些信息就说明没问题了，然后启动Nagios 服务。

七、Nagios的启动与停⽌

- 7.1 启动Nagios


- a. 通过初始化脚本启动nagios
- b. ⼿⼯⽅式启动nagios 通过nagios命令的“-d”参数来启动nagios守护进程：


- 1 # /etc/init.d/nagios start

- 2 or

- 3 # service nagios start


1 # /usr/local/nagios/bin/nagios -d /usr/local/nagios/etc/nagios.cfg

- 7.2 重启Nagios 当修改了配置⽂件让其⽣效时，需要重启/重载Nagios服务。


- a. 通过初始化脚本来重启nagios
- b. 通过web监控⻚重启nagios


- 1 # /etc/init.d/nagios reload

- 2 or

- 3 # /etc/init.d/nagios restart

- 4 or

- 5 # service nagios restart


可以通过web监控⻚的 "Proces Info" -> "Restart the Nagios proces"来重启nagios

![image 28](<Linux下Nagios的安装与配置.note_images/imageFile28.png>)

- c. ⼿⼯⽅式平滑重启


1 # kill -HUP <nagios_pid>

- 7.3 停⽌Nagios


- a. 通过初始化脚本关闭nagios服务
- b. 通过web监控⻚停⽌nagios 可以通过web监控⻚的 "Proces Info" -> "Shutdown the Nagios proces"来停⽌nagios
- c. ⼿⼯⽅式停⽌Nagios


- 1 # /etc/init.d/nagios stop

- 2 or

- 3 # service nagios stop


![image 29](<Linux下Nagios的安装与配置.note_images/imageFile29.png>)

1 # kill <nagios_pid>

⼋、查看初步配置情况

- 8.1 启动完成之后，登录Nagios Web监控⻚ 查看相关信息。


htp:/192.168.1.108/nagios/

- 8.2 点击左⾯的Curent Status -> Hosts 可以看到所定义的三台主机已经全部UP了。
- 8.3 点击Curent Status -> Services 查看服务监控情况。


![image 30](<Linux下Nagios的安装与配置.note_images/imageFile30.png>)

![image 31](<Linux下Nagios的安装与配置.note_images/imageFile31.png>)

看到Nagios-Linux和Nagios-Server的服务状态已经OK了，但是Nagios-Windows的服务状态为 CRITICAL，Status Information 提示Conection refused。因为Nagios-Windows上还未安装插件，内 部服务还⽆法查看，所以出现这种情况。将在下⾯具体讲解。 九、利⽤NRPE监控远程Linux上的“本地信息” 上⾯已经对远程Linux 主机是否存活做了监控，⽽判断远程机器是否存活，我们可以使⽤ping ⼯具对 其监测。还有⼀些远程主机服务，例如ftp、 sh、htp，都是对外开放的服务，即使不⽤Nagios，我们 也可以试的出来，随便找⼀台机器看能不能访问这些服务就⾏了。但是对于像磁盘容量，cpu负载这样 的“本地信息”，Nagios只能监测⾃⼰所在的主机，⽽对其他的机器则显得有点⽆能为⼒。毕竟没得到 被控主机的适当权限是不可能得到这些信息的。为了解决这个问题，nagios有这样⼀个附加组件 “NRPE”，⽤它就可以完成对Linux 类型主机"本地信息”的监控。

- 9.1 NRPE ⼯作原理


NRPE 总共由两部分组成：

check_nrpe 插件，位于监控主机上

NRPE daemon，运⾏在远程的Linux主机上(通常就是被监控机)

按照上图，整个的监控过程如下： 当Nagios 需要监控某个远程Linux 主机的服务或者资源情况时：

- 1.
- 2.
- 3.
- 4.


Nagios 会运⾏check_nrpe 这个插件，告诉它要检查什么； check_nrpe 插件会连接到远程的NRPE daemon，所⽤的⽅式是 SL； NRPE daemon 会运⾏相应的Nagios 插件来执⾏检查； NRPE daemon 将检查的结果返回给check_nrpe 插件，插件将其递交给nagios做处理。

注意：NRPE daemon 需要Nagios 插件安装在远程的Linux主机上，否则，daemon不能做任何的监 控。

- 9.2 在被监控机（Nagios-Linux）上


- a. 增加⽤户&设定密码 # userad nagios # paswd nagios
- b. 安装Nagios 插件


![image 32](<Linux下Nagios的安装与配置.note_images/imageFile32.png>)

- 1 # tar zxvf nagios-plugins-1.4.16.tar.gz

- 2 # cd nagios-plugins-1.4.16

- 3 # ./configure --prefix=/usr/local/nagios

- 4 # make && make install


这⼀步完成后会在/usr/local/nagios/下⽣成三个⽬录include、libexec和share。

![image 33](<Linux下Nagios的安装与配置.note_images/imageFile33.png>)

修改⽬录权限

- 1 # chown nagios.nagios /usr/local/nagios

- 2 # chown -R nagios.nagios /usr/local/nagios/libexec


![image 34](<Linux下Nagios的安装与配置.note_images/imageFile34.png>)

- c. 安装NRPE


- 1 # wget http://prdownloads.sourceforge.net/sourceforge/nagios/nrpe-2.15.tar.gz

- 2 # tar zxvf nrpe-2.15.tar.gz

- 3 # cd nrpe-2.15

- 4 # ./configure


安装nrpe时提示错误：configure: eror: canot findsl headers 原因是缺少opensl-devel包， yum -y instal opensl-devel 问题解决

![image 35](<Linux下Nagios的安装与配置.note_images/imageFile35.png>)

1 # make all

![image 36](<Linux下Nagios的安装与配置.note_images/imageFile36.png>)

接下来安装NPRE插件，daemon和示例配置⽂件。

- c.1 安装check_nrpe 这个插件 # make instal-plugin 监控机需要安装check_nrpe 这个插件，被监控机并不需要，我们在这⾥安装它只是为了测试⽬的。

- c.2 安装deamon # make instal-daemon
- c.3 安装配置⽂件 # make instal-daemon-config


现在再查看nagios ⽬录就会发现有5个⽬录了

按照安装⽂档的说明，是将NRPE deamon作为xinetd下的⼀个服务运⾏的。在这样的情况下xinetd就 必须要先安装好，不过⼀般系统已经默认安装了。

- d. 安装xinted 脚本 # make instal-xinetd


![image 37](<Linux下Nagios的安装与配置.note_images/imageFile37.png>)

![image 38](<Linux下Nagios的安装与配置.note_images/imageFile38.png>)

![image 39](<Linux下Nagios的安装与配置.note_images/imageFile39.png>)

可以看到创建了这个⽂件/etc/xinetd.d/nrpe。 编辑这个脚本：

在only_from 后增加监控主机的IP地址。 编辑/etc/services ⽂件，增加NRPE服务

![image 40](<Linux下Nagios的安装与配置.note_images/imageFile40.png>)

重启xinted 服务 # service xinetd restart

报错xinetd:unrecognized service 需要安装xinetd：yum -y instal xinetd

![image 41](<Linux下Nagios的安装与配置.note_images/imageFile41.png>)

查看NRPE 是否已经启动

![image 42](<Linux下Nagios的安装与配置.note_images/imageFile42.png>)

可以看到5 6端⼝已经在监听了。

- e. 测试NRPE是否则正常⼯作 使⽤上⾯在被监控机上安装的check_nrpe 这个插件测试NRPE 是否⼯作正常。 # /usr/local/nagios/libexec/check_nrpe -H 127.0.0.1 会返回当前NRPE的版本

也就是在本地⽤check_nrpe连接nrpe daemon是正常的。 注：为了后⾯⼯作的顺利进⾏，注意本地防⽕墙要打开5 6能让外部的监控机访问。

- f. check_nrpe 命令⽤法 查看check_nrpe 命令⽤法 # /usr/local/nagios/libexec/check_nrpe –h


![image 43](<Linux下Nagios的安装与配置.note_images/imageFile43.png>)

![image 44](<Linux下Nagios的安装与配置.note_images/imageFile44.png>)

可以看到⽤法是：

check_nrpe –H 被监控的主机 -c 要执⾏的监控命令 注意：-c 后⾯接的监控命令必须是nrpe.cfg ⽂件中定义的。也就是NRPE daemon只运⾏nrpe.cfg中所 定义的命令。

- g. 查看NRPE的监控命令 # cd /usr/local/nagios/etc # cat nrpe.cfg |grep -v "^#"|grep -v "^$"


![image 45](<Linux下Nagios的安装与配置.note_images/imageFile45.png>)

- 1 [root@Nagiso-Linux etc]# cat nrpe.cfg |grep -v "^#"|grep -v "^$"

- 2 log_facility=daemon

- 3 pid_file=/var/run/nrpe.pid

- 4 server_port=5666

- 5 nrpe_user=nagios

- 6 nrpe_group=nagios

- 7 allowed_hosts=127.0.0.1

- 8

- 9 dont_blame_nrpe=0

- 10 debug=0

- 11 command_timeout=60

- 12 connection_timeout=300

- 13 command[check_users]=/usr/local/nagios/libexec/check_users -w 5 -c 10

- 14 command[check_load]=/usr/local/nagios/libexec/check_load -w 15,10,5 -c 30,25,20

command[check_sda1]=/usr/local/nagios/libexec/check_disk -w 20% -c 10% -p /dev/sda1

- 15

command[check_zombie_procs]=/usr/local/nagios/libexec/check_procs -w 5 -c 10 -s Z

- 16

- 17 command[check_total_procs]=/usr/local/nagios/libexec/check_procs -w 150 -c 200

- 18 [root@Nagiso-Linux etc]#


![image 46](<Linux下Nagios的安装与配置.note_images/imageFile46.png>)

红⾊部分是命令名，也就是check_nrpe 的-c 参数可以接的内容，等号 “=” 后⾯是实际执⾏的插件程 序（这与comands.cfg 中定义命令的形式⼗分相似，只不过是写在了⼀⾏）。也就是说check_users 就是等号后⾯/usr/local/nagios/libexec/check_users -w 5 -c 10 的简称。 我们可以很容易知道上⾯这5⾏定义的命令分别是检测登陆⽤户数，cpu负载，sda1的容量，僵⼫进 程，总进程数。各条命令具体的含义⻅插件⽤法（执⾏“插件程序名 –h”）。 由于-c 后⾯只能接nrpe.cfg 中定义的命令，也就是说现在我们只能⽤上⾯定义的这五条命令。我们可 以在本机实验⼀下。

![image 47](<Linux下Nagios的安装与配置.note_images/imageFile47.png>)

- 9.3 在监控主机（Nagios-Server）上 之前已经将Nagios运⾏起来了，现在要做的事情是：


安装check_nrpe 插件；

在comands.cfg 中创建check_nrpe 的命令定义，因为只有在comands.cfg 中定义过的命令才 能在services.cfg 中使⽤；

创建对被监控主机的监控项⽬；

- 9.3.1 安装check_nrpe 插件

只运⾏这⼀步就⾏了，因为只需要check_nrpe插件。 在Nagios-Linux 上我们已经装好了nrpe，现在我们测试⼀下监控机使⽤check_nrpe 与被监控机运⾏ 的nrpe daemon之间的通信。 /usr/local/nagios/libexec/check_nrpe -H 192.168.0.15

看到已经正确返回了NRPE的版本信息，说明⼀切正常。

- 9.3.2 在comands.cfg中增加对check_nrpe的定义 # vi /usr/local/nagios/etc/objects/comands.cfg 在最后⾯增加如下内容：


- 1 # tar zxvf nrpe-2.15.tar.gz

- 2 # cd nrpe-2.15

- 3 # ./configure

- 4 # make all

- 5 # make install-plugin


![image 48](<Linux下Nagios的安装与配置.note_images/imageFile48.png>)

![image 49](<Linux下Nagios的安装与配置.note_images/imageFile49.png>)

意义如下：

- 1 # 'check_nrpe' command definition

- 2 define command{

command_name check_nrpe # 定义命令名称为check_nrpe,在 services.cfg中要使⽤这个名称.

- 3

command_line $USER1$/check_nrpe -H $HOSTADDRESS$ -c $ARG1$ #这 是定义实际运⾏的插件程序.

- 4

# 这个命令⾏的书写要完全按照check_nrpe这个命令的⽤法,不知道⽤法 的就⽤check_nrpe –h查看.

- 5

- 6 }


-c 后⾯带的$ARG1$ 参数是传给nrpe daemon 执⾏的检测命令，之前说过了它必须是nrpe.cfg 中所定 义的那5条命令中的其中⼀条。在services.cfg 中使⽤check_nrpe 的时候要⽤ “!” 带上这个参数。

- 9.3.3 定义对Nagios-Linux 主机的监控 下⾯就可以在services.cfg 中定义对Nagios-Linux 主机的监控了。


![image 50](<Linux下Nagios的安装与配置.note_images/imageFile50.png>)

- 1 define service{

- 2 use local-service

- 3 host_name Nagios-Linux

- 4 service_description Current Load

- 5 check_command check_nrpe!check_load

- 6 }

- 7

- 8 define service{

- 9 use local-service

- 10 host_name Nagios-Linux

- 11 service_description Check Disk sda1

- 12 check_command check_nrpe!check_sda1

- 13 }

- 14

- 15 define service{

- 16 use local-service

- 17 host_name Nagios-Linux

- 18 service_description Total Processes

- 19 check_command check_nrpe!check_total_procs

- 20 }

- 21

- 22 define service{

- 23 use local-service

- 24 host_name Nagios-Linux

- 25 service_description Current Users

- 26 check_command check_nrpe!check_users

- 27 }

- 28

- 29 define service{

- 30 use local-service

- 31 host_name Nagios-Linux

- 32 service_description Check Zombie Procs

- 33 check_command check_nrpe!check_zombie_procs

- 34 }


![image 51](<Linux下Nagios的安装与配置.note_images/imageFile51.png>)

还有⼀个任务是要监控Nagios-Linux 的swap 使⽤情况。但是在nrpe.cfg 中默认没有定义这个监控功 能的命令。怎么办？⼿动在nrpe.cfg 中添加，也就是⾃定义NRPE命令。

现在我们要监控swap 分区，如果空闲空间⼩于20%则为警告状态 -> warning；如果⼩于10%则为严重 状态 -> critical。我们可以查得需要使⽤check_swap插件，完整的命令⾏应该是下⾯这样。 # /usr/local/nagios/libexec/check_swap -w 20% -c 10% 在被监控机（Nagios-Linux）上增加check_swap 命令的定义 # vi /usr/local/nagios/etc/nrpe.cfg 增加下⾯这⼀⾏ comand[check_swap]=/usr/local/nagios/libexec/check_swap -w 20% -c 10% 我们知道check_swap 现在就可以作为check_nrpe 的-c 的参数使⽤了 修改了配置⽂件，当然要重启。 如果你是以独⽴的daemon运⾏的nrpe，那么需要⼿动重启；如果你是在xinetd 下⾯运⾏的，则不需 要。 由于本实验中nrpe 是xinetd 下运⾏的，所以不需要重启服务。 在监控机（Nagios-Server）上增加这个check_swap 监控项⽬

- 1 define service{

- 2 use local-service

- 3 host_name Nagios-Linux

- 4 service_description Check Swap

- 5 check_command check_nrpe!check_swap

- 6 }


同理，Nagios-Linux 上我还开启了htp 服务，需要监控⼀下，按照上⾯的做法，在被监控机 （Nagios-Linux）上增加check_htp 命令的定义 # vi /usr/local/nagios/etc/nrpe.cfg 增加下⾯这⼀⾏ comand[check_htp]=/usr/local/nagios/libexec/check_htp -I 127.0.0.1 在监控机（Nagios-Server）上增加check_htp 监控项⽬

- 1 define service{

- 2 use local-service

- 3 host_name Nagios-Linux

- 4 service_description HTTP

- 5 check_command check_nrpe!check_http

- 6 }


所有的配置⽂件已经修改好了，现在重启Nagios。

# service nagios restart

- 9.3.4 查看配置情况 登录Nagios Web监控⻚ 查看相关信息。


htp:/192.168.1.108/nagios/

![image 52](<Linux下Nagios的安装与配置.note_images/imageFile52.png>)

可以看到，对于Nagios-Server 和Nagios-Linux 上的相关服务的监控已经成功了，还有NagiosWindows 上的服务还没有定义，下⾯讲到。 ⼗、利⽤NSClient+监控远程Windows上的“本地信息” 在Nagios的libexec下有check_nt这个插件，它就是⽤来检查windows机器的服务的。其功能类似于 check_nrpe。不过还需要搭配另外⼀个软件NSClient+，它则类似于NRPE。 NSClient+的原理如下图

![image 53](<Linux下Nagios的安装与配置.note_images/imageFile53.png>)

可以看到NSClient与nrpe最⼤的区别就是：

被监控机上安装有nrpe，并且还有插件，最终的监控是由这些插件来进⾏的。当监控主机将监控请 求发给nrpe后，nrpe调⽤插件来完成监控。

NSClient+则不同，被监控机上只安装NSClient，没有任何的插件。当监控主机将监控请求发给 NSClient+后，NSClient直接完成监控，所有的监控是由NSClient完成的。

这也说明了NSClient+的⼀个很⼤的问题：不灵活、没有可扩展性。它只能完成⾃⼰本身包含的监控 操作，不能由⼀些插件来扩展。好在NSClient+已经做的不错了，基本上可以完全满⾜我们的监控需 求。

- 10.1 安装NSClient+ 从 下载NSClient+-0.2.7.zip 解压到C盘根⽬录。 打开cmd 切换到c:\NSClient+-0.2.7 执⾏nsclient+ /instal 进⾏安装


htp:/ w.nsclient.org/nscp/downloads

![image 54](<Linux下Nagios的安装与配置.note_images/imageFile54.png>)

执⾏nsclient+ SysTray （注意⼤⼩写），这⼀步是安装系统托盘，时间稍微有点⻓。

![image 55](<Linux下Nagios的安装与配置.note_images/imageFile55.png>)

在运⾏⾥⾯输⼊services.msc 打开“服务”

![image 56](<Linux下Nagios的安装与配置.note_images/imageFile56.png>)

看到下图就说明NSClient服务已经安装上了

![image 57](<Linux下Nagios的安装与配置.note_images/imageFile57.png>)

双击打开，点“登录”标签，在“允许服务与桌⾯交互”前打勾。

![image 58](<Linux下Nagios的安装与配置.note_images/imageFile58.png>)

编辑c:\NSClient+-0.2.7下的NSC.ini⽂件。 将 [modules]部分的所有模块前⾯的注释都去掉，除了CheckWMI.dl 和 RemoteConfiguration.dl 这两 个。

![image 59](<Linux下Nagios的安装与配置.note_images/imageFile59.png>)

在[Setings]部分设置'pasword'选项来设置密码，作⽤是在nagios连接过来时要求提供密码。这⼀步 是可选的，我这⾥设置为'123456'。 将[Setings]部分'alowed_hosts'选项的注释去掉，并且加上运⾏nagios的监控主机的IP。各IP之间以 逗号相隔。这个地⽅是⽀持⼦⽹的，如果写成192.168.1.0/24则表示该⼦⽹内的所有机器都可以访问。 如果这个地⽅是空⽩则表示所有的主机都可以连接上来。

注意是[Setings]部分的，因为[NSClient]部分也有这个选项。

![image 60](<Linux下Nagios的安装与配置.note_images/imageFile60.png>)

必须保证[NSClient]的'port'选项并没有被注释，并且它的值是'12489'，这是NSClient的默认监听端 ⼝。

![image 61](<Linux下Nagios的安装与配置.note_images/imageFile61.png>)

在cmd 中执⾏nsclient+ /start启动服务，注意所在⽬录是c:\NSClient+-0.2.7

![image 62](<Linux下Nagios的安装与配置.note_images/imageFile62.png>)

这时在桌⾯右下⻆的系统托盘处会出现⼀个⻩⾊的M字样的图标

![image 63](<Linux下Nagios的安装与配置.note_images/imageFile63.png>)

查看服务

![image 64](<Linux下Nagios的安装与配置.note_images/imageFile64.png>)

已经正常启动了。 注意服务默认设的是“⾃动”，也就是说是开机⾃动启动的。 在cmd ⾥⾯执⾏netstat –an 可以看到已经开始监听tcp的12489端⼝了。

![image 65](<Linux下Nagios的安装与配置.note_images/imageFile65.png>)

这样外部就可以访问了吗？ 错！ 防⽕墙也要打开tcp的12489端⼝，否则nagios 检查此服务的时候会报错。 这样被监控机的配置就搞定了，它就等待nagios 发出某个监控请求，然后它执⾏请求将监控的结果发 回到nagios监控主机上。 之前已经在监控主机（Nagios-Server）上对Windows 主机的监控做了配置，但是comands.cfg 中 默认没有设置密码项，所以要修改⼀下，增加"-s 123456"，如下：

- 1 # 'check_nt' command definition

- 2 define command{

- 3 command_name check_nt

command_line $USER1$/check_nt -H $HOSTADDRESS$ -p 12489 -s 123456 -v $ARG1$ $ARG2$

- 4

- 5 }


现在打开Nagios Web监控⻚便可查看到相关信息了。

![image 66](<Linux下Nagios的安装与配置.note_images/imageFile66.png>)

可以看到有错误：NSClient - EROR: PDH Colection thread not runing. Gogle ⼀下，是由于操作系统语⾔的问题，好像NSClient 默认⽀持的语⾔并不多，具体可以百度⼀ 下。 查看NSClient的⽇志C:\NSClient+-0.2.7\nsclient.log，信息如下：

2013-02-02 22:05:30: error:.\PDHCollector.cpp:98: You need to manually configure performance counters!

1

需要⼿动配置performance counters。 打开C:\NSClient+-0.2.7\counters.defs⽂件，复制⽂件⾥⾯"English US"那部分内容，粘贴到 counters.defs ⽂件的最后，修改Description = "Chinese"。

修改完之后，在 mc中重启NSClient 服务。 然后查看⽇志，内容如下：

![image 67](<Linux下Nagios的安装与配置.note_images/imageFile67.png>)

在正常执⾏了。 打开Nagios Web监控⻚查看。

![image 68](<Linux下Nagios的安装与配置.note_images/imageFile68.png>)

执⾏成功，但是W3SVC服务为Unknown 状态。查资料，需要开启Windows 的 IS服务。 打开“控制⾯板”进⾏安装。

![image 69](<Linux下Nagios的安装与配置.note_images/imageFile69.png>)

安装完毕后，再到Nagios Web监控⻚查看，全部监控正常。

![image 70](<Linux下Nagios的安装与配置.note_images/imageFile70.png>)

⼗⼀、Nagios邮件报警的配置

1.1 安装sendmail 组件 ⾸先要确保sendmail 相关组件的完整安装，我们可以使⽤如下的命令来完成sendmail 的安装： # yum instal -y sendmail* 然后重新启动sendmail服务： # service sendmail restart 然后发送测试邮件，验证sendmail的可⽤性： # echo "Helo World" | mail david.tang@bsmart.cn

- 1.2 邮件报警的配置

在上⾯我们已经简单配置过了/usr/local/nagios/etc/objects/contacts.cfg ⽂件，Nagios 会将报警邮件 发送到配置⽂件⾥的E-mail 地址。

- 1.3 Nagios 通知


PROBLEM

![image 71](<Linux下Nagios的安装与配置.note_images/imageFile71.png>)

RECOVERY

![image 72](<Linux下Nagios的安装与配置.note_images/imageFile72.png>)

Linux下Nagios安装配置完毕。

