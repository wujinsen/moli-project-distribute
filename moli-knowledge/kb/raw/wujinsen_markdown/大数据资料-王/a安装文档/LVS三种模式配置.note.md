LVS三种（LVS-DR,LVS-NAT,LVS-TUN）模式的简要配置 LVS是什么:

htp:/ w.linuxvirtualserver.org/VS-NAT.html htp:/ w.linuxvirtualserver.org/VS-IPTuneling.html htp:/ w.linuxvirtualserver.org/VS-DRouting.html

⾸先是安装ipvsadm管理程序 下载： 注意对应⾃⼰的内核版本 ipvsadm-1.24.tar.gz tar zxvf ipvsadm-1.24.tar.gz cd ipvsadm-1.24 make make instal 1： LVS-DR 模式(调度器与实际服务器都有⼀块⽹卡连在同⼀物理⽹段上) 简要的⽹络结构如下所示

htp:/ w.linuxvirtualserver.org/software/

配置LVS server 引⽤#！/bin/sh VIP=192.168.0.210

- RIP1=192.168.0.175
- RIP2=192.168.0.145


. /etc/rc.d/init.d/functions case "$1" in start) echo "start LVS of DirectorServer" #Set the Virtual IP Adres /sbin/ifconfig eth0 1 $VIP broadcast $VIP netmask 25.25.25.25 up /sbin/route ad -host $VIP dev eth0 1 #Clear IPVS Table /sbin/ipvsadm -C #Set Lvs /sbin/ipvsadm -A -t $VIP 80 -sr

- /sbin/ipvsadm -a -t $VIP 80 -r $RIP1 80 -g
- /sbin/ipvsadm -a -t $VIP 80 -r $RIP2 80 -g #Run Lvs


/sbin/ipvsadm

; stop) echo "close LVS Directorserver" /sbin/ipvsadm -C /sbin/ifconfig eth0 1 down

; *) echo "Usage： $0 {start|stop}" exit 1 esac

配置 RIP server 引⽤#!/bin/bash VIP=192.168.0.210 LOCAL_Name=50bang BROADCAST=192.168.0.25 #vip's broadcast

. /etc/rc.d/init.d/functions case "$1" in

start) echo "reparing for Real Server"

- echo "1" >/proc/sys/net/ipv4/conf/lo/arp_ignore
- echo "2" >/proc/sys/net/ipv4/conf/lo/arp_anounce


- echo "1" >/proc/sys/net/ipv4/conf/al/arp_ignore
- echo "2" >/proc/sys/net/ipv4/conf/al/arp_anounce ifconfig lo:0 $VIP netmask 25.25.25.25 broadcast $BROADCAST up


/sbin/route ad -host $VIP dev lo:0 ; stop)

ifconfig lo:0 down echo "0" >/proc/sys/net/ipv4/conf/lo/arp_ignore echo "0" >/proc/sys/net/ipv4/conf/lo/arp_anounce echo "0" >/proc/sys/net/ipv4/conf/al/arp_ignore echo "0" >/proc/sys/net/ipv4/conf/al/arp_anounce

;

*)

echo "Usage: lvs {start|stop}" exit 1

esac

- 2: LVS-TUN 模式 简要的⽹络架构如下:


<table>
  <tr>
    <th>![image 1](<LVS三种模式配置.note_images/imageFile1.png>)</th>
  </tr>
</table>


配置lvs server 引⽤ #!/bin/sh # description: start LVS of Directorserver VIP=192.168.25.41（注意，lvs server那台机器2个ip，⼀个是vip,⼀个是本身ip例如192.168.25.42）

- RIP1=192.168.25. 4
- RIP2=192.168.25.45 #RIPn=192.168.0.n GW=192.168.25.254


. /etc/rc.d/init.d/functions case "$1" in start) echo " start LVS of DirectorServer" # set the Virtual IP Adres /sbin/ifconfig tunl0 $VIP broadcast $VIP netmask 25.25.25.0 up /sbin/route ad -host $VIP dev tunl0 #Clear IPVS table /sbin/ipvsadm -C #set LVS /sbin/ipvsadm -A -t $VIP 80 -sr /sbin/ipvsadm -a -t $VIP 80 -r $RIP1 80 -i /sbin/ipvsadm -a -t $VIP 80 -r $RIP2 80 -i #/sbin/ipvsadm -a -t $VIP 80 -r $RIP3 80 -i #Run LVS /sbin/ipvsadm #end

; stop) echo "close LVS Directorserver" ifconfig tunl0 down /sbin/ipvsadm -C

; *) echo "Usage: $0 {start|stop}" exit 1 esac .

配置real server 引⽤#!/bin/sh # ghb in 2060812 # description: Config realserver tunl port and aply arp patch VIP=192.168.25.43

. /etc/rc.d/init.d/functions case "$1" in start) echo "Tunl port starting" ifconfig tunl0 $VIP netmask 25.25.25.0 broadcast $VIP up /sbin/route ad -host $VIP dev tunl0

- echo "1" >/proc/sys/net/ipv4/conf/tunl0/arp_ignore
- echo "2" >/proc/sys/net/ipv4/conf/tunl0/arp_anounce


- echo "1" >/proc/sys/net/ipv4/conf/al/arp_ignore
- echo "2" >/proc/sys/net/ipv4/conf/al/arp_anounce sysctl -p


; stop) echo "Tunl port closing" ifconfig tunl0 down echo 1 > /proc/sys/net/ipv4/ip_forward echo 0 > /proc/sys/net/ipv4/conf/al/arp_anounce

; *) echo "Usage: $0 {start|stop}" exit 1 esac

- 3: LVS-NAT 模式 简要的⽹络架构如下图:


<table>
  <tr>
    <th>![image 2](<LVS三种模式配置.note_images/imageFile2.png>)</th>
  </tr>
</table>


配置LVS server 引⽤#!/bin/sh # description: start LVS of Nat VLAN-IP=202. 9.59.10

- RIP1=10.1.1.2
- RIP2=10.1.1.3 #RIPn=10.1.1.n GW=10.1.1.1


. /etc/rc.d/init.d/functions case "$1" in start) echo " start LVS of NAtServer" echo "1" >/proc/sys/net/ipv4/ip_forward

- echo "0" >/proc/sys/net/ipv4/conf/al/send_redirects

- echo "0" >/proc/sys/net/ipv4/conf/default/send_redirects

- echo "0" >/proc/sys/net/ipv4/conf/eth0/send_redirects
- echo "0" >/proc/sys/net/ipv4/conf/eth1/send_redirects(内⽹卡上的) #Clear IPVS table /sbin/ipvsadm -C #set LVS /sbin/ipvsadm -a -t 202. 9.59.10 80 -r 10.1.1.2 80 -m -w 1 /sbin/ipvsadm -a -t 202. 9.59.10 80 -r 10.1.1.3 80 -m -w 1 #Run LVS /sbin/ipvsadm #end


; stop) echo "close LVS Nat server"

- echo "0" >/proc/sys/net/ipv4/ip_forward
- echo "1" >/proc/sys/net/ipv4/conf/al/send_redirects


- echo "1" >/proc/sys/net/ipv4/conf/default/send_redirects


- echo "1" >/proc/sys/net/ipv4/conf/eth0/send_redirects echo "1" >/proc/sys/net/ipv4/conf/eth1/send_redirects(内⽹卡上的) /sbin/ipvsadm -C


; *) echo "Usage: $0 {start|stop}"

exit 1 esac 配置real server LVS-Nat 模式的后端机器不需要配置. tips： -g 表示使⽤DR⽅式，-m表示NAT⽅式，-i表示tuneling⽅式。

