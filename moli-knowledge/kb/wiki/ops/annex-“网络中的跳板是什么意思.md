---
title: “网络中的跳板是什么意思.note（原文插图 annex）
slug: annex-“网络中的跳板是什么意思
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/linux/“网络中的跳板是什么意思.note.md
related: [linux-运维基础]
created: 2026-07-05
updated: 2026-07-05
---

![image 1](assets/imageFile1.png)

⿊客想攻击内⽹中的servers

⾛的是红⾊虚线，但是到了防⽕墙那⾥被拦截啦，没有成功。 第⼆次⾛的是蓝⾊实线，通过控制内⽹⼀台员⼯的电脑，来攻击servers。这时防⽕墙认为是合法的。 攻击就成功啦。 这时员⼯的那台电脑就成了 所谓的“跳板”。 图画的不好，能看懂就⾏。 希望能够帮助到你，别忘了采纳吆~

第⼀次

背景 我在Qunar的⼯作是运维开发，因此需要通过终端连接到远程开发机进⾏⼯作，由于安全等因素，登录 开发机时需要先登录跳板机，然后在跳板机上再实际连接开发机，如下图所示：

![image 2](assets/imageFile2.png)

正常的登录流程 使⽤ sh命令登录跳板机，输⼊⾃⼰的设置的密码+token⽣成的动态密码；

登录跳板机成功后，在跳板机分配的终端中使⽤ sh命令再登录开发机，跳板机和开发机之间采⽤带密 码的 sh验证，因此需要输⼊ sh私钥的密码。 登录过程如下图所示： 连接跳板机

![image 3](assets/imageFile3.png)

输⼊⽤户名

![image 4](assets/imageFile4.png)

输⼊⽤户密码+token动态密码

![image 5](assets/imageFile5.png)

跳板机登录成功

![image 6](assets/imageFile6.png)

从跳板机上登录开发机

![image 7](assets/imageFile7.png)

输⼊ sh私钥密码后，登录成功

![image 8](assets/imageFile8.png)

简化流程 登录跳板机，输⼊⽤户名这个过程可以简化，即使⽤XShel⾃动完成⽤户名的输⼊，如下图所示：

![image 9](assets/imageFile9.png)

# 登录跳板机成功后，可以设置⾃动登录开发机

![image 10](assets/imageFile10.png)

# 下图是XShel登录跳板机成功后，⾃动输⼊的登录开发机命令：

![image 11](assets/imageFile11.png)
