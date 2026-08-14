---
title: 本地YUM源制作.note（原文插图 annex）
slug: annex-本地YUM源制作-linux
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/Linux/本地YUM源制作.note.md
related: [linux-运维基础]
created: 2026-07-05
updated: 2026-07-05
---

本地YUM源制作

- 1. YUM相关概念

- 1.1.什么是YUM

YUM（全称为 Yelow dog Updater, Modified）是⼀个在Fedora和RedHat以及CentOS中的Shel前端 软件包管理器。基于RPM包管理，能够从指定的服务器⾃动下载RPM包并且安装，可以⾃动处理依赖 性关系，并且⼀次安装所有依赖的软件包，⽆须繁琐地⼀次次下载、安装。

- 1.2. YUM的作⽤

在Linux上使⽤源码的⽅式安装软件⾮常满分，使⽤yum可以简化安装的过程

2. YUM的常⽤命令

安装htpd并确认安装 yum instl -y htpd

列出所有可⽤的package和package组 yum list

清除所有缓冲数据 yum clean al

列出⼀个包所有依赖的包 yum deplist htpd

删除htpd yum remove htpd

3.制作本地YUM源

- 3.1.为什么要制作本地YUM源




YUM源虽然可以简化我们在Linux上安装软件的过程，但是⽣成环境通常⽆法上⽹，不能连接外⽹的 YUM源，说以接就⽆法使⽤yum命令安装软件了。为了在内⽹中也可以使⽤yum安装相关的软件，就 要配置yum源。

- 3.2. YUM源的原理


YUM源其实就是⼀个保存了多个RPM包的服务器，可以通过htp的⽅式来检索、下载并安装相关的 RPM包

![image 1](assets/imageFile1.png)

# 3.3.制作本地YUM源

- 1.
- 2.
- 3.
- 4.


准备⼀台Linux服务器，⽤最简单的版本CentOS-6.7-x86_64-minimal.iso 配置好这台服务器的IP地址 上传CentOS-6.7-x86_64-bin-DVD1.iso到服务器 将CentOS-6.7-x86_64-bin-DVD1.iso镜像挂载到某个⽬录

mkdir /var/iso mount -o l op CentOS-6.7-x86_64-bin-DVD1.iso /var/iso cd /var/iso ls | wc –l 查看有多少⾏⽂件

1.

修改本机上的YUM源配置⽂件，将源指向⾃⼰

备份原有的YUM源的配置⽂件 cd /etc/yum.repos.d/ rename .repo .repo.bak * cp CentOS-Base.repo.bak CentOS-Local.repo 复制YUM源的Base配置⽂件 vi CentOS-Local.repo

<table>
  <tr>
    <th>[base] name=CentOS-Local baseurl=file: /var/iso gpgcheck=1</th>
  </tr>
</table>


gpgkey=file: /etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-6

添加上⾯内容保存退出

1.

清除YUM缓冲

yum clean al

1.

列出可⽤的YUM源

yum repolist

1.

安装相应的软件

yum instal -y htpd service htpd status ps -ef | grep htpd

netstat -anpt | grep 159 (-anpt a所有 n数字 p进程 t TCP)

- 9.开启htpd使⽤浏览器访问 （如果访问不通，检查防⽕墙是否开启了80端⼜ 或关闭防⽕墙） service htpd start
- 10.将YUM源配置到htpd（Apache Server）中，其他的服务器即可通过⽹络访问这个内⽹中的YUM源 了


htp:/192.168.0.10 80

cd /var/ w/html/ vi index.html htpd服务器的主页⾯

cp -r /var/iso/ /var/ w/html/CentOS-6.7 把iso下的都拷贝到CentOS.6.7⽬录下

1.取消先前挂载的镜像 umount /var/iso 12.在浏览器中访问

htp:/192.168.0.10/CentOS-6.7/

Chkconfig htpd on

![image 2](assets/imageFile2.png)

1.

让其他需要安装RPM包的服务器指向这个YUM源，准备⼀台新的服务器，备份或删除原有的YUM 源配置⽂件

cd /etc/yum.repos.d/ rename .repo .repo.bak * vi CentOS-Local.repo

<table>
  <tr>
    <th>[base] name=CentOS-Local baseurl=htp:/192.168.0.10/CentOS-6.7 gpgcheck=1</th>
  </tr>
</table>


gpgkey=file: /etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-6

添加上⾯内容保存退出

1.

在这台新的服务器上执⾏YUM的命令

yum clean al yum repolist

1.

安装相应的软件

yum instal -y gc

配置 sh免密登陆 sh-keygen –t rsa
