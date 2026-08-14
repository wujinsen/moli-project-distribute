---
title: Linux常用软件安装.note（原文插图 annex）
slug: annex-Linux常用软件安装
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/Linux/Linux常用软件安装.note.md
related: [linux-运维基础]
created: 2026-07-05
updated: 2026-07-05
---

在centos中使⽤yum安装软件时可能出现Could not retrieve mirrorlist，这种情况⼀般是⽹络出现问 题。 如果配置的DHCP动态⽹络，则在/etc/resolv.conf⽂件中添加 nameserver 8.8.8.8 如果是静态⽹络，则在/etc/sysconﬁg/network-scripts/ifcfg-eth0⽂件中添加 DNS1=8.8.8.8

CentOS6重启后/etc/resolv.conf被还原解决办法 在这个⽂件中加⼊:

- DNS1=202.96.209.5

- DNS2=202.96.209.133


这类的设置就好了.

在此要强调⼀点的是，直接修改/etc/resolv.conf这个⽂件是没⽤的，⽹络服务重启以后会根 据/etc/sysconfig /network-scripts/ifcfg-eth0来重载配置，如果ifcfg-eth0没有配置DNS，那么 resolv.conf会被冲掉，重 新变成空值。

- 1.修改主机名 vi /etc/sysconfig/network
- 2.修改ip地址 vi /etc/sysconfig/network-scripts/ifcfg-eth0

service network restart

- 3.修改ip地址和主机名的映射关系 vi /etc/hosts
- 4.关闭iptables并设置其开机启动/不启动 service iptables stop chkconfig iptables on chkconfig iptables of


<table>
  <tr>
    <th>NETWORKING=yes</th>
  </tr>
</table>


HOSTNAME=server1.itcast.cn

<table>
  <tr>
    <th>DEVICE=eth0 TYPE=Ethernet ONBOT=yes BOTPROTO=static IPADR=192.168.0.101</th>
  </tr>
</table>


NETMASK=25.25.25.0

<table>
  <tr>
    <th>127.0.0.1 localhost localhost.localdomain localhost4 localhost4.localdomain4 :1 localhost localhost.localdomain localhost6 localhost6.localdomain6</th>
  </tr>
</table>


192.168.0.101 server1.itcast.cn

- 5.修改⽹卡信息 /etc/udev/rules.d/70-persistent-net.rules


# 3. 安装JDK

- 1.上传jdk-7u45-linux-x64.tar.gz到Linux上
- 2.解压jdk到/usr/local⽬录 tar -zxvf jdk-7u45-linux-x64.tar.gz -C /usr/local/
- 3.设置环境变量，在/etc/profile⽂件最后追加相关内容 vi /etc/profile
- 4.刷新环境变量 source /etc/profile
- 5.测试java命令是否可⽤ java -version


<table>
  <tr>
    <th>export JAVA_HOME=/usr/local/jdk1.7.0_45</th>
  </tr>
</table>


export PATH=$PATH:$JAVA_HOME/bin

# 4. 安装Tomcat

- 1.上传apache-tomcat-7.0.68.tar.gz到Linux上
- 2.解压tomcat tar -zxvf apache-tomcat-7.0.68.tar.gz -C /usr/local/
- 3.启动tomcat /usr/local/apache-tomcat-7.0.68/bin/startup.sh
- 4.查看tomcat进程是否启动 jps
- 5.查看tomcat进程端⼝ Ps –ef | grep tomcat?

netstat -anpt | grep 2465

- 6.通过浏览器访问tomcat


![image 1](assets/imageFile1.png)

htp:/192.168.0.101 8080/

![image 2](assets/imageFile2.png)

# 5. 安装MySQL

- 1.上传MySQL-server-5.5.48-1.linux2.6.x86_64.rpm、MySQL-client-5.5.48-1.linux2.6.x86_64.rpm到 Linux上
- 2.使⽤rpm命令安装MySQL-server-5.5.48-1.linux2.6.x86_64.rpm，缺少perl依赖 rpm -ivh MySQL-server-5.5.48-1.linux2.6.x86_64.rpm
- 3.安装perl依赖，上传6个perl相关的rpm包

rpm -i perl-*

- 4.再安装MySQL-server，rpm包冲突 rpm -ivh MySQL-server-5.5.48-1.linux2.6.x86_64.rpm
- 5.卸载冲突的rpm包 rpm -e mysql-libs-5.1.73-5.el6_6.x86_64-nodeps
- 6.再安装MySQL-client和MySQL-server rpm -ivh MySQL-client-5.5.48-1.linux2.6.x86_64.rpm rpm -ivh MySQL-server-5.5.48-1.linux2.6.x86_64.rpm
- 7.启动MySQL服务，然后初始化MySQL service mysql start /usr/bin/mysql_secure_instalation
- 8.测试MySQL mysql -u rot -p


![image 3](assets/imageFile3.png)

![image 4](assets/imageFile4.png)

![image 5](assets/imageFile5.png)
