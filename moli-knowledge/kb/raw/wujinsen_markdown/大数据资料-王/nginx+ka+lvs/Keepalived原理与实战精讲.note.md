什么是Keepalived呢，keepalived观其名可知，保持存活，在⽹络⾥⾯就是

保持在线了，也就是所谓的⾼可⽤或热备，⽤来防⽌单点故障(单点故障是指⼀旦某⼀点出现故障就会 导致整个系统架构的不可⽤)的发⽣，那说到keepalived时不得不说的⼀个协议就是VRRP协议，可以说 这个协议就是keepalived实现的基础，那么⾸先我们来看看VRRP协议

注：搞运维的要有⾜够的耐⼼哦，不理解协议就很难透彻的掌握keepalived的了

⼀，VRP协议

VRP协议

学过⽹络的朋友都知道，⽹络在设计的时候必须考虑到冗余容灾，包括线路冗余，设备冗余等，防⽌ ⽹络存在单点故障，那在路由器或三层交换机处实现冗余就显得尤为重要，在⽹络⾥⾯有个协议就是 来做这事的，这个协议就是VRRP协议，Keepalived就是巧⽤VRRP协议来实现⾼可⽤性(HA)的

VRRP协议有⼀篇⽂章写的⾮常好，⼤家可以直接看这⾥(记得认真看看哦，后⾯基本都已这个为基础的 了) 帖⼦地址： 只需要把服务器当作路由器即可！

htp:/ bs.ywlm.net/thread-790-1-1.html

在《 》⾥讲到了虚拟路由器的ID也就是VRID在这⾥⽐较重要

VRP协议

keepalived完全遵守VRRP协议，包括竞选机制等等

⼆，Kepalived原理

Kepalived原理 keepalived也是模块化设计，不同模块复杂不同的功能，下⾯是keepalived的组件

core check vrp libipfwc libipvs-2.4 libipvs-2.6

core：是keepalived的核⼼，复杂主进程的启动和维护，全局配置⽂件的加载解析等 check：负责healthchecker(健康检查)，包括了各种健康检查⽅式，以及对应的配置的解析包括LVS的 配置解析 vrrp：VRRPD⼦进程，VRRPD⼦进程就是来实现VRRP协议的 libipfwc：iptables(ipchains)库，配置LVS会⽤到 libipvs*：配置LVS会⽤到

注意，keepalived和LVS完全是两码事，只不过他们各负其责相互配合⽽已

![image 1](<Keepalived原理与实战精讲.note_images/imageFile1.png>)

keepalived启动后会有三个进程 ⽗进程：内存管理，⼦进程管理等等 ⼦进程：VRRP⼦进程 ⼦进程：healthchecker⼦进程

有图可知，两个⼦进程都被系统WatchDog看管，两个⼦进程各⾃复杂⾃⼰的事，healthchecker⼦进 程复杂检查各⾃服务器的健康程度，例如HTP，LVS等等，如果 healthchecker⼦进程检查到MASTER上服务不可⽤了，就会通知本机上的兄弟 VRP⼦进程，让他删除通告，并且去掉虚拟IP，转换为BACKUP状态

三，Kepalived配置⽂件详解

keepalived配置详解 keepalived有三类配置区域(姑且就叫区域吧)，注意不是三种配置⽂件，是⼀个配置⽂件⾥⾯三种不同 类别的配置区域

全局配置(Global Configuration)

VRPD配置 LVS配置

⼀，全局配置 全局配置⼜包括两个⼦配置：

全局定义(global definition)

静态路由配置(static ipadres/routes)

- 1，全局定义(global definition) 配置范例


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.


global_defs { notification_email { admin@example.com } notification_email_from admin@example.com smtp_server 127.0.0.1 stmp_conect_timeout 30 router_id node1 }复制代码全局配置解析

global_defs全局配置标识，表⾯这个区域{}是全局配置

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


notification_email

{

admin@example.com admin@ywlm.net

}复制代码表示keepalived在发⽣诸如切换操作时需要发送email通知，以及email发送给哪些邮件地 址，邮件地址可以多个，每⾏⼀个

notification_email_from 表示发送通知邮件时邮件源地址是谁

admin@example.com

smtp_server 127.0.0.1 表示发送email时使⽤的smtp服务器地址，这⾥可以⽤本地的sendmail来实现

smtp_connect_timeout 30 连接smtp连接超时时间

router_id node1 机器标识

- 2，静态地址和路由配置范例


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


static_ipadres {

- 192.168.1.1/24 brd + dev eth0 scope global
- 192.168.1.2/24 brd + dev eth1 scope global } static_routes { src $SRC_IP to $DST_IP dev $SRC_DEVICE src $SRC_IP to $DST_IP via $GW dev $SRC_DEVICE }复制代码


这⾥实际上和系统⾥⾯命令配置IP地址和路由⼀样例如： 192.168.1.1/24 brd + dev eth0 scope global 相当于: ip addr add 192.168.1.1/24 brd + dev eth0 scope global 就是给eth0配置IP地址 路由同理 ⼀般这个区域不需要配置 这⾥实际上就是给服务器配置真实的IP地址和路由的，在复杂的环境下可能需要配置， ⼀般不会⽤这个来配置，我们可以直接⽤vi /etc/sysconfig/network-script/ifcfg-eth1来配置，切记这⾥ 可不是VIP哦，不要搞混淆了，切记切记！

⼆，VRRPD配置 VRRPD配置包括三个类

VRP同步组(synchroization group)

VRP实例(VRP Instance)VRP脚本

- 1，VRP同步组(synchroization group)配置范例

smtp alter表示切换时给global defs中定义的邮件地址发送右键通知

- 2，VRP实例(instance) 配置范例


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.


vrp_sync_group VG_1 { group { htp mysql } notify_master /path/to/to_master.sh notify_backup /path_to/to_backup.sh notify_fault "/path/fault.sh VG_1" notify /path/to/notify.sh smtp_alert }复制代码其中：

- 1.
- 2.
- 3.
- 4.


group { htp mysql }复制代码http和mysql是实例名和下⾯的实例名⼀致

- 1.
- 2.
- 3.
- 4.
- 5.


notify_master /path/to/to_master.sh：表示当切换到master状态时，要执⾏的脚本

notify_backup /path_to/to_backup.sh：表示当切换到backup状态时，要执⾏的脚本

notify_fault "/path/fault.sh VG_1"复制代码notify /path/to/notify.sh：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.


vrp_instance htp { state MASTER interface eth0 dont_track_primary track_interface {

- eth0
- eth1 } mcast_src_ip <IPADR> garp_master_delay 10 virtual_router_id 51 priority 10 advert_int 1 authentication { auth_type PAS autp_pas 1234 } virtual_ipadres { #<IPADR>/<MASK> brd <IPADR> dev <STRING> scope <SCOPT> label <LABEL>


- 192.168.20.17/24 dev eth1
- 192.168.20.18/24 dev eth2 label eth2 1 } virtual_routes { # src <IPADR> [to] <IPADR>/<MASK> via|gw <IPADR> dev <STRING> scope <SCOPE> tab src 192.168.10.1 to 192.168.109.0/24 via 192.168.20.254 dev eth1 192.168.10.0/24 via 192.168.20.254 dev eth1 192.168. 1.0/24 dev eth2 192.168.12.0/24 via 192.168.10.254 } noprempt premtp_delay 30 debug }复制代码


state：state指定instance(Initial)的初始状态，就是说在配置好后，这台服务器的初始状态就是这⾥指

定的，但这⾥指定的不算，还是得要通过竞选通过优先级来确定，⾥如果这⾥设置为master，但如若 他的优先级不及另外⼀台，那么这台在发送通告时，会发送⾃⼰的优先级，另外⼀台发现优先级不如 ⾃⼰的⾼，那么他会就回抢占为master

interface：实例绑定的⽹卡，因为在配置虚拟IP的时候必须是在已有的⽹卡上添加的

dont track primary：忽略VRRP的interface错误

track interface：跟踪接⼝，设置额外的监控，⾥⾯任意⼀块⽹卡出现问题，都会进⼊故障(FAULT)状 态，例如，⽤nginx做均衡器的时候，内⽹必须正常⼯作，如果内⽹出问题了，这个均衡器也就⽆法运 作了，所以必须对内外⽹同时做健康检查

mcast src ip：发送多播数据包时的源IP地址，这⾥注意了，这⾥实际上就是在那个地址上发送VRRP通 告，这个⾮常重要，⼀定要选择稳定的⽹卡端⼝来发送，这⾥相当于heartbeat的⼼跳端⼝，如果没有 设置那么就⽤默认的绑定的⽹卡的IP，也就是interface指定的IP地址

garp master delay：在切换到master状态后，延迟进⾏免费的ARP(gratuitous ARP)请求

virtual router id：这⾥设置VRID，这⾥⾮常重要，相同的VRID为⼀个组，他将决定多播的MAC地址

priority 10：设置本节点的优先级，优先级⾼的为master

advert int：检查间隔，默认为1秒

virtual ipadres：这⾥设置的就是VIP，也就是虚拟IP地址，他随着state的变化⽽增加删除，当state 为master的时候就添加，当state为backup的时候删除，这⾥主要是有优先级来决定的，和state设置的 值没有多⼤关系，这⾥可以设置多个IP地址

virtual routes：原理和virtual ipaddress⼀样，只不过这⾥是增加和删除路由

lvs sync daemon interface：lvs syncd绑定的⽹卡

authentication：这⾥设置认证

auth type：认证⽅式，可以是PASS或AH两种认证⽅式

auth pas：认证密码

noprempt：设置不抢占，这⾥只能设置在state为backup的节点上，⽽且这个节点的优先级必须别另 外的⾼

prempt delay：抢占延迟

debug：debug级别

notify master：和sync group这⾥设置的含义⼀样，可以单独设置，例如不同的实例通知不同的管理⼈ 员，http实例发给⽹站管理员，mysql的就发邮件给DBA

- 3，VRP脚本


- 1.
- 2.
- 3.
- 4.


vrp_script check_runing { script "/usr/local/bin/check_runing" interval 10 weight 10

- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.


}

vrp_instance htp { state BACKUP smtp_alert interface eth0 virtual_router_id 101 priority 90 advert_int 3 authentication { auth_type PAS auth_pas whatever } virtual_ipadres { 1.1.1.1 } track_script { check_runing weight 20 }

}复制代码

⾸先在vrp_script区域定义脚本名字和脚本执⾏的间隔和脚本执⾏的优先级变更

vrp_script check_runing { script "/usr/local/bin/check_runing" interval 10 #脚本执⾏间隔 weight 10 #脚本结果导致的优先级变更：10表示优先级+10；-10则表示优先级-10 } 然后在 实例( vrp_instance)⾥⾯引⽤，有点类似脚本⾥⾯的函数引⽤⼀样：先定义，后引⽤函数名 track_script {

check_runing weight 20 }

注意：VRRP脚本(vrrp_script)和VRRP实例(

vrp_instance)属于同⼀个级别

LVS配置

如果你没有配置LVS+keepalived那么⽆需配置这段区域，⾥如果你⽤的是nginx来代替LVS，这⽆限配 置这款，这⾥的LVS配置是专⻔为keepalived+LVS集成准备的。 注意了，这⾥LVS配置并不是指真的安装LVS然后⽤ipvsadm来配置他，⽽是⽤keepalived的配置⽂件来 代替ipvsadm来配置LVS，这样会⽅便很多，⼀个配置⽂件搞定这些，维护⽅便，配置⽅便是也！

这⾥LVS配置也有两个配置

⼀个是虚拟主机组配置

⼀个是虚拟主机配置

- 1，虚拟主机组配置⽂件详解 这个配置是可选的，根据需求来配置吧，这⾥配置主要是为了让⼀台realserver上的某个服务可以属于 多个Virtual Server，并且只做⼀次健康检查

virtual_server_group <STRING> { # VIP port <IPADDR> <PORT> <IPADDR> <PORT> fwmark <INT> }

- 2，虚拟主机配置


virtual server可以以下⾯三种的任意⼀种来配置

- 1.
- 2.
- 3.


- 1. virtual server IP port
- 2. virtual server fwmark int
- 3. virtual server group string复制代码下⾯以第⼀种⽐较常⽤的⽅式来配详细解说⼀下


virtual_server 192.168.1.2 80 { #设置⼀个virtual server: VIP:Vport

delay_loop 3 # service polling的delay时间，即服务轮询的时间间隔

lb_algo rr|wrr|lc|wlc|lblc|sh|dh #LVS调度算法 lb_kind NAT|DR|TUN #LVS集群模式 persistence_timeout 120 #会话保持时间（秒为单位），即以⽤户在120秒内被

分配到同⼀个后端realserver persistence_granularity <NETMASK> #LVS会话保持粒度，ipvsadm中的-M参数，默认是 0xffffffff，即每个客户端都做会话保持 protocol TCP #健康检查⽤的是TCP还是UDP ha_suspend #suspendhealthchecker’s activity virtualhost <string> #HTTP_GET做健康检查时，检查的web服务器的虚拟 主机（即host：头）

sorry_server <IPADDR> <PORT> #备⽤机，就是当所有后端realserver节点都不可⽤时， 就⽤这⾥设置的，也就是临时把所有的请求都发送到这⾥啦

real_server <IPADDR> <PORT> #后端真实节点主机的权重等设置，主要，后端有⼏台 这⾥就要设置⼏个 { weight 1 #给每台的权重，0表示失效(不知给他转发请求知道 他恢复正常)，默认是1 inhibit_on_failure #表示在节点失败后，把他权重设置成0，⽽不是冲 IPVS中删除

notify_up <STRING> | <QUOTED-STRING> #检查服务器正常(UP)后，要执⾏的脚本 notify_down <STRING> | <QUOTED-STRING> #检查服务器失败(down)后，要执⾏的脚本

HTTP_GET #健康检查⽅式 { url { #要坚持的URL，可以有多个 path / #具体路径 digest <STRING> status_code 200 #返回状态码 } connect_port 80 #监控检查的端⼝

bindto <IPADD> #健康检查的IP地址

- connect_timeout 3 #连接超时时间 nb_get_retry 3 #重连次数 delay_before_retry 2 #重连间隔 } # END OF HTTP_GET|SSL_GET

#下⾯是常⽤的健康检查⽅式，健康检查⽅式⼀共有 HTTP_GET|SSL_GET|TCP_CHECK|SMTP_CHECK|MISC_CHECK这些

#TCP⽅式 TCP_CHECK { connect_port 80 bindto 192.168.1.1

- connect_timeout 4 } # TCP_CHECK


# SMTP⽅式，这个可以⽤来给邮件服务器做集群 SMTP_CHECK host { connect_ip <IP ADDRESS> connect_port <PORT> #默认检查25端⼝ 14 KEEPALIVED bindto <IP ADDRESS> } connect_timeout <INTEGER> retry <INTEGER> delay_before_retry <INTEGER> # "smtp HELO"ž|·- ëê§Œà " helo_name <STRING>|<QUOTED-STRING> } #SMTP_CHECK

#MISC⽅式，这个可以⽤来检查很多服务器只需要⾃⼰会些脚本即可 MISC_CHECK { misc_path <STRING>|<QUOTED-STRING> #外部程序或脚本

misc_timeout <INT> #脚本或程序执⾏超时时间

misc_dynamic #这个就很好⽤了，可以⾮常精确的来调整权重，是后 端每天服务器的压⼒都能均衡调配，这个主要是通过执⾏的程序或脚本返回的状态代码来动态调整 weight值，使权重根据真实的后端压⼒来适当调整，不过这需要有过硬的脚本功夫才⾏哦

- #返回0：健康检查没问题，不修改权重

- #返回1：健康检查失败，权重设置为0 #返回2-255：健康检查没问题，但是权重却要根据返回代码修改为 返回码-2，例如如果程序或脚本执⾏后返回的代码为200，#那么权重这回被修改为 200-2 } } # Realserver } # Virtual Server


配置⽂件到此就讲完了，下⾯是⼀份未加备注的完整配置⽂件

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.


global_defs

{

notification_email

{

admin@example.com

}

notification_email_from admin@example.com

smtp_server 127.0.0.1

stmp_conect_timeout 30

router_id node1

}

notification_email

{

admin@example.com

admin@ywlm.net

}

static_ipadres

{

- 192.168.1.1/24 brd + dev eth0 scope global

- 192.168.1.2/24 brd + dev eth1 scope global


}

static_routes

{

src $SRC_IP to $DST_IP dev $SRC_DEVICE

src $SRC_IP to $DST_IP via $GW dev $SRC_DEVICE

}

vrp_sync_group VG_1 {

group {

htp

mysql

}

notify_master /path/to/to_master.sh

notify_backup /path_to/to_backup.sh

notify_fault "/path/fault.sh VG_1"

notify /path/to/notify.sh

- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.


smtp_alert

}

group {

htp

mysql

}

vrp_script check_runing {

script "/usr/local/bin/check_runing"

interval 10

weight 10

}

vrp_instance htp {

state MASTER interface eth0 dont_track_primary

track_interface {

- eth0

- eth1


}

mcast_src_ip <IPADR>

garp_master_delay 10

virtual_router_id 51

priority 10 advert_int 1 authentication {

auth_type PAS autp_pas 1234 }

virtual_ipadres {

#<IPADR>/<MASK> brd <IPADR> dev <STRING> scope <SCOPT> label <LABEL>

- 192.168.20.17/24 dev eth1

- 192.168.20.18/24 dev eth2 label eth2 1


}

- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.
- 88.
- 89.
- 90.
- 91.
- 92.
- 93.
- 94.
- 95.
- 96.
- 97.
- 98.
- 99.
- 100.
- 101.
- 102.
- 103.
- 104.
- 105.
- 106.
- 107.
- 108.
- 109.
- 110.


virtual_routes {

# src <IPADR> [to] <IPADR>/<MASK> via|gw <IPADR> dev <STRING> scope <SCOPE> tab

src 192.168.10.1 to 192.168.109.0/24 via 192.168.20.254 dev eth1

192.168.10.0/24 via 192.168.20.254 dev eth1

192.168. 1.0/24 dev eth2

192.168.12.0/24 via 192.168.10.254

}

track_script {

check_runing weight 20

}

noprempt

premtp_delay 30

debug

}

virtual_server_group <STRING> {

# VIP port

<IPADR> <PORT> <IPADR> <PORT> fwmark <INT>

}

virtual_server 192.168.1.2 80 {

delay_l op 3

lb_algor|wr|lc|wlc|lblc|sh|dh

lb_kind NAT|DR|TUN

persistence_timeout 120

persistence_granularity <NETMASK>

protocol TCP

ha_suspend

virtualhost <string>

sory_server <IPADR> <PORT>

- 111.
- 112.
- 113.
- 114.
- 115.
- 116.
- 117.
- 118.
- 119.
- 120.
- 121.
- 122.
- 123.
- 124.
- 125.
- 126.
- 127.
- 128.
- 129.
- 130.
- 131.
- 132.
- 133.
- 134.


real_server <IPADR> <PORT>

{

weight 1

inhibit_on_failure

notify_up <STRING> | <QUOTED-STRING>

notify_down <STRING> | <QUOTED-STRING>

#HTP_GET⽅式

HTP_GET | SL_GET

{

url {

path /

digest <STRING>

status_code 20

}

conect_port 80

bindto <IPAD>

conect_timeout 3

nb_get_retry 3

delay_before_retry 2

} } }

复制代码

注意，这⾥仅仅是罗列，并不是可⽤的配置⽂件。⾥⾯需要根据⾃⼰的时间情况稍加配置才能⽤ 在下⾯我会根据实际的需求给出我平时的配置案例，在⼆楼三楼四楼分别给出三个案例

