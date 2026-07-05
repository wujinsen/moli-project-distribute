---
title: VM利用host-only上网.note（原文插图 annex）
slug: annex-VM利用host-only上网
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/Linux/VM利用host-only上网.note.md
related: [linux-运维基础]
created: 2026-07-05
updated: 2026-07-05
---

利⽤host-only连接⽅式让VM上⽹

- 1、打开⽹络连接

- 2、右键本地连接-》属性-》共享

- 3、设置Vnet1的IP地址

- 4、设置VM的IP，打开编辑


![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

![image 3](assets/imageFile3.png)

![image 4](assets/imageFile4.png)

- 5、设置DHCP的ip的包含段落

- 6、设置虚拟机⾥⾯系统的IP


![image 5](assets/imageFile5.png)

![image 6](assets/imageFile6.png)

- 7、试⼀试在浏览器中输⼊www.baidu.com,看看嫩打开吗


vi /etc/resolv.conf 修改DNS

nameserver 8.8.8.8 nameserver 8.8.4.4

解决⽅案⼆（推荐）：

对接⼝添加dns信息；编辑/etc/sysconfig/network-scripts/ifcfg-ethX,具体的X根据你的⽹卡确 定，添加

- DNS1=8.8.8.8
- DNS2=8.8.4.4
