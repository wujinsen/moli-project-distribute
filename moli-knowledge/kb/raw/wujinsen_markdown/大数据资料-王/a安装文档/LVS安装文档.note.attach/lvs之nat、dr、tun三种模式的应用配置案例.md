![image 1](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile1.png>)

![image 2](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile2.png>)

![image 3](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile3.png>)

![image 4](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile4.png>)

![image 5](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile5.png>)

![image 6](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile6.png>)

![image 7](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile7.png>)

51CTO首页 51CTO博客 我的博客 搜索 每日博报 登录 注册 社区：学院 论坛 博客 下载 更多

![image 8](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile8.png>)

原创:90 翻译:0 转载:16

# Coffee_蓝山

http://lansgg.blog.51cto.com 【复制】 【订阅】

### 博 客 | 图库 | 写博文 | 帮 助

首页 | Services | Script | Perl_function | LinuxSys | Essay | Protocol | Mysql | Oracle | Security | Ras | Java | Puppet | Minitor

![image 9](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile9.png>)

### shuocaocao 的BLOG

<table>
  <tr>
    <th>![image 10](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile10.png>)</th>
  </tr>
</table>


·【征文】聊IT运维标准化与安全，奖50元京东购物卡 ·Python项目实训：网络爬虫(3课时精讲视频) 博主的更多文章>>

![image 11](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile11.png>)

Lvs之NAT、DR、TUN三种模式的应用配置案例

![image 12](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile12.png>)

2013-06-25 16:57:50

写留言 邀请进圈子 发消息 加友情链接 进家园 加好友

标签：LVS dr nat director tun

原创作品，允许转载，转载时请务必以超链接形式标明文章 原始出处 、作者信息和本声明。否则将追究法律责 任。http://lansgg.blog.51cto.com/5675165/1229421

博客统计信息

本文系统Centos6.0 1、NAT模式； NAT模型:地址转换类型，主要是做地址转换，类似于iptables的DNAT类型，它通过多目标地址转换，来实现负载均 衡； 特点和要求：

51CTO推荐博客 用户名：shuocaocao 文章数：106 评论数：143 访问量：69675 无忧币：6176 博客积分：2344 博客等级：6 注册日期：2012-08-06

- 1、LVS（Director）上面需要双网卡：DIP(内网)和VIP（外网）
- 2、内网的Real Server主机的IP必须和DIP在同一个网络中，并且要求其网关都需要指向DIP的地址
- 3、RIP都是私有IP地址，仅用于各个节点之间的通信
- 4、Director位于client和Real Server之间，负载处理所有的进站、出站的通信
- 5、支持端口映射
- 6、通常应用在较大规模的应用场景中，但Director易成为整个架构的瓶颈！ 相关机器信息； （Director Server） LB1 eth0:192.168.244.132 (Vip) （公网）


热门专题 更多>>

![image 13](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile13.png>)

DIY强大的虚拟化环境

阅读量：2789

eth1:192.168.27.128 (Dip) (内网)

![image 14](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile14.png>)

SCCM 2012 R2从入门到精通

- rs1 rs1 eth0:192.168.27.130 (Rip) （内网）getway:192.168.27.128
- rs2 rs2 eth0:192.168.27.131 (Rip) （内网）getway:192.168.27.128


阅读量：1244

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


[root@rs1 ~]# cat /etc/sysconfig/network-scripts/ifcfg-eth0 DEVICE="eth0" BOOTPROTO=static IPADDR=192.168.27.130 NETMASK=255.255.255.0 GATEWAY=192.168.27.128 NM_CONTROLLED="yes" ONBOOT="yes

![image 15](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile15.png>)

Jquery+EasyUI开发案例详 解 阅读量：2094

![image 16](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile16.png>)

Microsoft Hyper-V Server 2012开启虚拟化 阅读量：7030

拓扑如下：

热门文章

Linux之系统故障分析与排查 linux之vpn服务器间ip隧.. mysql主从复制之mysql-pr.. Lvs之NAT、DR、TUN三种模..

![image 17](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile17.png>)

Centos6.0系统lvs+keepal.. linux系统安全常规优化 Linux之Shell管理脚本（一） linux之IP隧道配置

搜索BLOG文章

<table>
  <tr>
    <th> </th>
  </tr>
</table>


![image 18](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile18.png>)

搜 索

我的技术圈(5) 更多>>

首先在rs1、rs2部署httpd，并且进行测试是否OK!

![image 19](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile19.png>)

美女黑客技术联盟 linux-北京圈 [Linux]服务器方向 RHEL 技术交流 mysql_sql

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10


- [root@rs1 ~]# yum install httpd -y

- [root@rs1 ~]# echo web1 > /var/www/html/index.html

- [root@rs1 ~]# service httpd start
- [root@rs2 ~]# yum install httpd -y


- [root@rs2 ~]# echo web2 > /var/www/html/index.html


- [root@rs2 ~]# service httpd start [root@rs1 ~]# curl http://127.0.0.1


- web1 [root@rs1 ~]# curl http://192.168.27.131
- web2


最近访客

现在在LB上操作； 确定本机ip_vs模块是否加载，也就是是否支持lvs，2.4.2后都支持了；然后安装ipvsadm 用户操作命令

<table>
  <tr>
    <th>![image 20](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile20.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 21](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile21.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 22](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile22.png>)</th>
  </tr>
</table>


1 [root@LB1 ~]# grep -i 'ip_vs' /boot/config-2.6.32-71.el6.i686

![image 23](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile23.png>)

taosf

coolo..

zhang..

<table>
  <tr>
    <th>![image 24](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile24.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 25](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile25.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 26](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile26.png>)</th>
  </tr>
</table>


ddqdos

吃遍

sysname

<table>
  <tr>
    <th>![image 27](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile27.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 28](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile28.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 29](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile29.png>)</th>
  </tr>
</table>


Edwardz

犀首

40697..

<table>
  <tr>
    <th>![image 30](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile30.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 31](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile31.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 32](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile32.png>)</th>
  </tr>
</table>


上官战

xxl714

爱爱..

ipvsadm安装：

最新评论

1 [root@LB1 ~]# yum install ipvsadm -y

hewuqi：我在A服务器上设置拨入的 pptp帐号的..

- 1
- 2
- 3
- 4


echo 1 > /proc/sys/net/ipv4/ip_forward ipvsadm -A -t 192.168.244.132:80 -s rr ipvsadm -a -t 192.168.244.132:80 -r 192.168.27.131 -m ipvsadm -a -t 192.168.244.132:80 -r 192.168.27.130 -m

hewuqi：您好，服务器A本身连接国外 的速度很..

shuocaocao：回复 hewuqi: 您好： 思 路是您..

测试页面

![image 33](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile33.png>)

hewuqi：博主您好，我有这样一个需 求，希望..

shuocaocao：回复 pigpig0109: leo是 我授权的..

51CTO推荐博文 更多>>

轻松搞定Windows Azure网络配置 DNS服务部署的那点事儿 Kali Linux渗透测试实战 1.4 小试.. Openmeetings 开源视频会议系统介.. 用amoeba实现mysql的读写分离 解决服务器复制中SID冲突问题

页面轮询交替出现；说明机器轮询提供服务； 如果使用加权轮询的话；比如rs1提供2次，rs2提供1次，这样来提供服务；

- 1
- 2
- 3


[root@LB1 html]# ipvsadm -E -t 192.168.244.132:80 -s wrr

- [root@LB1 html]# ipvsadm -e -t 192.168.244.132:80 -r 192.168.27.130 -m -w 2
- [root@LB1 html]# ipvsadm -e -t 192.168.244.132:80 -r 192.168.27.131 -m -w 1


现在来测试：

![image 34](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile34.png>)

PUPPET-通过配置服务器对多台服务.. Exchange2010管理DSN邮件：将退信.. 通过keepalived实现LVS的高可用，.. 服务端socket开发之多线程和geven.. centos6.2 lnmp环境下安装 zabbi..

友情链接

Share your knowle.. 大数据的虚拟化 抚琴煮酒 烟雨楼台 51CTO博客开发

![image 35](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile35.png>)

- 2、DR模式： 特点和要求


- 1、各个集群节点必须和Director在同一个物理网络中
- 2、RIP地址不能为私有地址，可以实现便捷的远程管理和监控
- 3、Director仅仅负责处理入站请求，响应报文则由Real Server直接发往客户端
- 4、集群节点Real Server 的网关一定不能指向DIP，而是指向外部路由
- 5、Director不支持端口映射
- 6、Director能够支持比NAT多很多的Real Server 原理： DR模型：直接路由模型，每个Real Server上都有两个IP：VIP和RIP，但是VIP是隐藏的，就是不能提高解析等功能， 只是用来做请求回复的源IP的，Director上只需要一个网卡，然后利用别名来配置两个IP：VIP和DIP


Director在接受到外部主机的请求的时候转发给Real Server的时候并不更改目标地址，只是通过arp解析的MAC地 址进行封装然后转给Real Server，Real Server在接受到信息以后拆除MAC帧封装，然后直接回复给CIP。

![image 36](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile36.png>)

LB1: eth0: 192.168.182.13 vip（eth0:0）: 192.168.182.20

- RS1: eth0:192.168.182.130 lo:0(vip) :192.168.182.20
- RS2: eth0:192.168.182.129 lo:0(vip) 192.168.182.200


通信原理：

每个Real Server上都有两个IP：VIP和RIP，但是VIP是隐藏的，就是不能提高解析等功能，只是用来做请求回复 的源IP的，Director上只需要一个网卡，然后利用别名来配置两个IP：VIP和DIP

Director在接受到外部主机的请求的时候转发给Real Server的时候并不更改目标地址，只是通过arp解析的MAC 地址进行封装然后转给Real Server，Real Server在接受到信息以后拆除MAC帧封装，然后直接回复给CIP。

而此时需要关闭RS上的基于VIP的arp解析，在linux内核2.4以后，内核中都内置了这种功能，通过一些设置可以 关闭其arp的功能：

arp_ignore:定义接收到ARP请求时的响应级别

- 0：默认，只用本地配置的有响应地址都给予响应


- 1：仅仅在目标IP是本地地址，并且是配置在请求进来的接口上的时候才给予响应(仅在请求的目标地址配


置请求到达的接口上的时候，才给予响应) arp_announce：定义将自己的地址向外通告时的级别

- 0：默认，表示使用配置在任何接口的任何地址向外通告
- 1：试图仅向目标网络通告与其网络匹配的地址
- 2：仅向与本地接口上地址匹配的网络进行通告


Ps：要想让其功能生效，必须先设置相关设置，然后在配置IP地址等信息

- 1、开始在RS1操作：

上面的就是定义了arp响应的级别；还有就是vip的请求数据，从rs1的本地ip进行了回复；

- 2、在RS2上执行上面同样的操作
- 3、在LB上操作: 配置eth0网卡ip；


- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


[root@rs1 ~]# echo 2 > /proc/sys/net/ipv4/conf/all/arp_announce [root@rs1 ~]# echo 2 > /proc/sys/net/ipv4/conf/eth0/arp_announce [root@rs1 ~]# echo 1 > /proc/sys/net/ipv4/conf/all/arp_ignore [root@rs1 ~]# echo 1 > /proc/sys/net/ipv4/conf/eth0/arp_ignore [root@rs1 ~]# service network restart [root@rs1 ~]# ifconfig lo:0 192.168.182.200 netmask 255.255.255.255 broadcast 182.168.182.200 [root@rs1 ~]# route add -host 192.168.182.200 dev lo:0 [root@rs1 ~]# yum install httpd -y

1 [root@LB1 ~]# ifconfig eth0:0 192.168.182.200/24 #在eth0:0配置vip

验证RS的web服务

![image 37](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile37.png>)

下面开始着手配置ipvs

- 1
- 2
- 3
- 4


[root@LB1 ~]# yum install ipvsadm -y [root@LB1 ~]# ipvsadm -A -t 192.168.182.200:80 -s rr [root@LB1 ~]# ipvsadm -a -t 192.168.182.200:80 -r 192.168.182.130 -g [root@LB1 ~]# ipvsadm -a -t 192.168.182.200:80 -r 192.168.182.129 -g

![image 38](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile38.png>)

测试效果；

![image 39](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile39.png>)

- 3、TUN模式； 其实数据转发原理和上图是一样的，不过这个我个人认为主要是位于不同位置（不同机房）；LB是通过隧道进行了信 息传输，虽然增加了负载，可是因为地理位置不同的优势，还是可以参考的一种方案； 优点：负载均衡器只负责将请求包分发给物理服务器，而物理服务器将应答包直接发给用户。所以，负载均衡器能处 理很巨大的请求量，这种方式，一台负载均衡能为超过100台的物理服务器服务，负载均衡器不再是系统的瓶颈。使 用VS-TUN方式，如果你的负载均衡器拥有100M的全双工网卡的话，就能使得整个Virtual Server能达到1G的吞吐量。 不足：但是，这种方式需要所有的服务器支持"IP Tunneling"(IP Encapsulation)协议； LB1: eth0: 192.168.182.132


vip(tunl0）: 192.168.182.20

- RS1: eth0:192.168.27.130


tunl0(vip) :192.168.182.200

- RS2: eth0:192.168.138.131 tunl0(vip) :192.168.182.20


LB1操作：

- 1
- 2
- 3
- 4
- 5
- 6


yum install ipvsadm -y ifconfig tunl0192.168.182.200 broadcast 192.168.182.200 netmask 255.255.255.0 up route add -host $VIP dev tunl0 ipvsadm -A -t 192.168.182.200:80 -s rr ipvsadm -a -t 192.168.182.200:80 -r 192.168.27.130 -i ipvsadm -a -t 192.168.182.200:80 -r 192.168.138.131 -i

- RS1操作：
- RS2同上：


- 1
- 2
- 3
- 4
- 5
- 6


ifconfig tunl0 192.168.182.200 netmask 255.255.255.0 broadcast 192.168.182.200 up route add -host 192.168.182.200 dev tunl0

- echo "1" >/proc/sys/net/ipv4/conf/tunl0/arp_ignore
- echo "2" >/proc/sys/net/ipv4/conf/tunl0/arp_announce


- echo "1" >/proc/sys/net/ipv4/conf/all/arp_ignore
- echo "2" >/proc/sys/net/ipv4/conf/all/arp_announce


访问vip进行测试即可；

本文出自 “Coffee_蓝山” 博客，请务必保留此出处http://lansgg.blog.51cto.com/5675165/1229421

![image 40](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile40.png>)

![image 41](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile41.png>)

### 4

![image 42](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile42.png>)

tongling_zzu、zc_nicolas、zwr2264 了这篇文章

7人

类别：Ras┆技术圈(0)┆阅读(1655)┆评论(4) ┆ 推送到技术圈┆返回首页

上一篇 Linux之rsync简单应用&&rsync+inotify实时应用 下一篇 vpn之ip隧道多对一模式实现跳转功能（系真实案..

![image 43](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile43.png>)

相关文章

LVS——概念、架构、模型 负载均衡集群--LVS 配置基于DR模式和NAT模式Lvs集群 例题解析LVS：NAT和DR模型 LVS 三种模型集群 LVS (DR, NAT)模式应用 抓包比较lvs的nat模式和dr模式 运维工程师必备LVS_NAT && LVS_ DR配置 LVS的DR和NAT模式配置 全方位剖析LVS及基于LVS的NAT、DR模型实现

文章评论

- [1楼] ontheway2015 回复

![image 44](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile44.png>)

- [2楼] liuchenchu 回复

![image 45](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile45.png>)

- [3楼] jimmy_lixw 回复

![image 46](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile46.png>)

- [4楼] jimmy_lixw 回复


- 2013-06-26 14:58:51

顶一个，那天我看你的ip Tunnel文章，打算试一下的，结果还是你抢先了，我就直接收藏了

- 2013-06-27 13:31:07


很详细，值得借鉴。

2013-07-19 22:51:21 博主的lvs写的不错。

![image 47](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile47.png>)

2013-07-19 22:51:39 非常感谢分享，学习了。

发表评论 周刊：甲骨文（Oracle）面试归来的感悟

<table>
  <tr>
    <th> </th>
  </tr>
</table>


昵 称：

登录 快速注册

<table>
  <tr>
    <th> </th>
  </tr>
</table>


验证码：

请点击后输入验证码 博客过2级，无需填写验证码

<table>
  <tr>
    <th>![image 48](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile48.png>)</th>
  </tr>
</table>


内 容：

Copyright By 51CTO.COM 版权所有

![image 49](<lvs之nat、dr、tun三种模式的应用配置案例_images/imageFile49.png>)

