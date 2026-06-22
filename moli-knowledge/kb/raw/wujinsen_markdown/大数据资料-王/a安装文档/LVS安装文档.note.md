Lvs之NAT、DR、TUN三种模式的应⽤配置案例.pdf 1.69MB

第⼀部分： 系统环境：centos6.5 1.安装lvs应⽤模块: yum -y instal ipvs*

2、确定本机ip_vs模块是否加载，也就是是否⽀持lvs，2.4.2后都⽀持了；然后安装ipvsadm ⽤户操作命令

[rot@client lvs]#grep -i 'ip_vs' /bot/config-2.6.32431.el6.x86_64 CONFIG_IP_VS=m CONFIG_IP_VS_IPV6=y # CONFIG_IP_VS_DEBUG is not set CONFIG_IP_VS_TAB_BITS=12 CONFIG_IP_VS_PROTO_TCP=y CONFIG_IP_VS_PROTO_UDP=y CONFIG_IP_VS_PROTO_AH_ESP=y CONFIG_IP_VS_PROTO_ESP=y CONFIG_IP_VS_PROTO_AH=y CONFIG_IP_VS_PROTO_SCTP=y CONFIG_IP_VS_R=m CONFIG_IP_VS_WR=m CONFIG_IP_VS_L C=m CONFIG_IP_VS_WLC=m CONFIG_IP_VS_LBLC=m CONFIG_IP_VS_LBLCR=m CONFIG_IP_VS_ DH=m CONFIG_IP_VS_SH=m CONFIG_IP_VS_SED=m CONFIG_IP_VS_NQ=m CONFIG_IP_VS_FTP= m CONFIG_IP_VS_PE_SIP=m

第⼆部分：

- 1、在lvs server机器上执⾏命令：（functions这个脚本是给/etc/init.d⾥边的⽂件使⽤的（可理解为全局⽂件）。） chmod 75 /etc/rc.d/init.d/functions

- 2、安装完ipvsadm 在l vs srever上执⾏以下脚本： vi lvs_dr.sh #!/bin/bash #description:start lvserver echo "1">/proc/sys/net/ipv4/ip_forward #开启ip转发


- WEB1=192.168.56.98
- WEB2=192.168.56.97 VIP1=192.168.56.70 /etc/rc.d/init.d/functions#初始化function case "$1"in #第⼀个参数


start) #第⼀个参数是start echo "start LVS ofdirectorServer" #set the Virtual adresand sysctl parameter /sbin/ifconfig eth0 0 $VIP1broadcast $VIP1 netmask 25.25.25.25 up#设置⽹络 #clear ipvs table /sbin/ipvsadm –C #清除内核虚拟服务器表中的所有记录 #set LVS #web apache or tomcat /sbin/ipvsadm -A -t$VIP1 8080 -sr #设置 r模式

- /sbin/ipvsadm -a -t$VIP1 8080 -r $WEB1 8080 -g
- /sbin/ipvsadm -a -t$VIP1 8080 -r $WEB2 8080 -g #run LVS


/sbin/ipvsadm #启动lvs

; stop) #如果第⼀个参数是stop echo "close LVSdirectorserver"

- echo "0">/proc/sys/net/ipv4/ip_forward #关闭ip转发 /sbin/ipvsadm –C #清除内核虚拟服务器表中的所有记录 /sbin/ipvsadm –Z #虚拟服务表计数器清零（清空当前的连接数量等）

;

*) #如果第⼀个参数是其他任何值 echo "usage:$0{start|stop}" #提示输⼊start或者stop exit 1 #退出 esac #循环结束

- 3、执⾏脚本#./lvs-dr.sh start
- 4、执⾏命令：#ipvsadm –Ln


看到上⾯信息说明ipvsadm启动成功。

5、在web1 和web2机器上分别执⾏命令：（functions这个脚本是给/etc/init.d⾥边的⽂件使⽤的（可理解为全局⽂ 件）。）

chmod 75 /etc/rc.d/init.d/functions

6、在分别在web1 和web2服务器上执⾏下⾯脚本： vi lvs-rs.sh #!/bin/sh #description startrealserver #chkconfig 235 26 26 VIP1=192.168.56.70 /etc/rc.d/init.d/functions case "$1" in start)

echo "start LVSof realserver" /sbin/ifconfig lo:0 $VIP1broadcast $VIP1 netmask 25.25.25.25 up

- echo "1">/proc/sys/net/ipv4/conf/lo/arp_ignore#定义接收到ARP请求时的响应级别
- echo "2">/proc/sys/net/ipv4/conf/lo/arp_anounce#定义将⾃⼰的地址向外通告时的级别


- echo "1">/proc/sys/net/ipv4/conf/al/arp_ignore
- echo "2">/proc/sys/net/ipv4/conf/al/arp_anounce ;


stop) /sbin/ifconfig lo:0 down echo "close lvs dirctorserver" echo "0">/proc/sys/net/ipv4/conf/lo/arp_ignore

echo "0">/proc/sys/net/ipv4/conf/lo/arp_anounce echo "0">/proc/sys/net/ipv4/conf/al/arp_ignore echo "0">/proc/sys/net/ipv4/conf/al/arp_anounce

; *) echo"usage:$0{start|stop}" exit 1 esac

PS: arp_ignore:定义接收到ARP请求时的响应级别

- 0：默认，只⽤本地配置的有响应地址都给予响应
- 1：仅仅在⽬标IP是本地地址，并且是配置在请求进来的接⼝上的时候才给予响应(仅在请求的⽬标地址配置请


求到达的接⼝上的时候，才给予响应) arp_anounce：定义将⾃⼰的地址向外通告时的级别

- 0：默认，表示使⽤配置在任何接⼝的任何地址向外通告
- 1：试图仅向⽬标⽹络通告与其⽹络匹配的地址
- 2：仅向与本地接⼝上地址匹配的⽹络进⾏通告


- 7、在web1 和web2机器上分别执⾏命令： #./lvs-rs.sh start

- 8、⾄此lvs 安装完毕。设置连接超时值(秒)，--set tcptcpfin udp 设置连接超时值 ipvsadm-set 1 1 1

- 9、关闭


./lvs-rs.shstop ./lvs-dr.shstop

