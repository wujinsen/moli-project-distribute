- 1、官⽹下载VirtualBox，傻⽠安装
- 2、在VirtualBox下新建linux，⽤iso镜像安装centos 注意：安装之前，需要修改存储，⽤iso镜像安装，需要修改⽹络，改为桥接
- 3、安装完成：修改⽹络 解释：


修改对应⽹卡的IP地址的配置⽂件

# vi /etc/sysconfig/network-scripts/ifcfg-eth0

修改以下内容

DEVICE=eth0 #描述⽹卡对应的设备别名，例如ifcfg-eth0的⽂件中它为eth0

BOTPROTO=static #设置⽹卡获得ip地址的⽅式，可能的选项为static，dhcp或botp，分别对应静 态指定的ip地址，通过dhcp协议获得的ip地址，通过botp协议获得的ip地址

BROADCAST=192.168.0.25 #对应的⼦⽹⼴播地址

HWADR=0 07 E9 05 E8 B4 #对应的⽹卡物理地址

IPADR=192.168.1.2 #如果设置⽹卡获得 ip地址的⽅式为静态指定，此字段就指定了⽹卡对应的ip地 址

IPV6INIT=no

IPV6_AUTOCONF=no

NETMASK=25.25.25.0 #⽹卡对应的⽹络掩码

NETWORK=192.168.1.0 #⽹卡对应的⽹络地址

ONBOT=yes #系统启动时是否设置此⽹络接⼝，设置为yes时，系统启动时激活此设备

需要修改的内容: 修改： BOTPROTO=static 添加： IPADR=192.168.170.136 GATEWAY=192.168.170.1 DNS1=8.8.8.8

或者可以在这⾥修改⽹关：

CentOS 修改⽹关

修改对应⽹卡的⽹关的配置⽂件

[rot@centos]# vi /etc/sysconfig/network

修改以下内容

NETWORKING=yes(表示系统是否使⽤⽹络，⼀般设置为yes。如果设为no，则不能使⽤⽹络，⽽且很 多系统服务程序将⽆法启动)

HOSTNAME=centos(设置本机的主机名，这⾥设置的主机名要和/etc/hosts中设置的主机名对应)

GATEWAY=192.168.170.1(设置本机连接的⽹关的IP地址。例如，⽹关为10.0.0.2)

或者可以在这⾥修改DNS

CentOS 修改DNS

修改对应⽹卡的DNS的配置⽂件

# vi /etc/resolv.conf

修改以下内容

nameserver 8.8.8.8 #gogle域名服务器

nameserver 8.8.4.4 #gogle域名服务器

重启⽹卡

service network restart

路由添加默认⽹关： route ad defaule gw 192.168.0.1

- 4、可以上⽹了。完毕


