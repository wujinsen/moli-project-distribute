本⽂假设已经成功安装了Cent OS，笔者使⽤的Vbox安装的CentOS6.5。也就是说⼀下的操作都是基 于虚拟机的。

- 1，⽹络配置 在虚拟机安装后，需要进⾏⽹络配置才能链接外⽹或者被其他机器访问。

⽹络链接有⼏种⽅式，我们⽐较常⽤的有两种，⼀种是通过NAT(⽹络地址转换)，另⼀种是桥接的⽅ 式。

两种链接⽅式的区别，简单说就是桥接使⽤的是虚拟机独⽴的⽹卡，具有独⽴的IP地址，该ip地址和 虚拟机所在的物理机是同等级的。NAT翻译过来就是⽹络地址转换，相当于是把虚拟机所在的物理机 当做路由器在使⽤。

专业的解释，互联⽹上有很多相关的介绍，有兴趣的朋友可以进⾏学习。本⽂中，我们采⽤的是桥 接的⽅式，即让虚拟机拥有独⽴的IP。

- 2，配置当前虚拟机的⽹卡等信息 新安装的操作系统，既是使⽤了桥接的⽅式，也不⼀定能够上⽹，因为每个⼈的⽹络环境并⾮⼀样


![image 1](<第一小节：Cent OS 网卡配置.note_images/imageFile1.png>)

的。所以如果你在使⽤ping 命令，可能会碰到⼀下的错误：

- 1）ping域名的时候出现ping:unknown host x. x
- 2）ping域名的时候出现conect: network is unreachable 的问题 以上两种问题的出现，⼀般都是ip地址没有配置好。下图的是常⽤⽹卡配置的参数


![image 2](<第一小节：Cent OS 网卡配置.note_images/imageFile2.png>)

配置⽹卡有⼏种⽅式，可以⾃⾏百度下，本⽂是修改⽹卡的配置⽂件。 配置⽂件名称：/etc/sysconfig/network-scripts/ifcfg-eth0

- 3，配置完毕之后，重启⽹卡 service network restart


![image 3](<第一小节：Cent OS 网卡配置.note_images/imageFile3.png>)

vi /etc/udev/rules.d/70-persistent-net.rules

