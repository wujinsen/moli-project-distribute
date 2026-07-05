---
title: linux虚拟机配置双网卡(1)(23-14-15).note（原文插图 annex）
slug: annex-linux虚拟机配置双网卡(1)(23-14-15)
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/linux/linux虚拟机配置双网卡(1)(23-14-15).note.md
related: [linux-运维基础]
created: 2026-07-05
updated: 2026-07-05
---

- 1、需要配置桥接和hostonly双⽹卡。建议第⼀块⽹卡为桥接，第⼆块为hostonly。
- 2、虚拟机如果只有⼀块⽹卡，关闭虚拟机后在设置⾥再次添加⼀块，启动虚拟机即可。3、如果配置 的第⼀块⽹卡是hostonly，hostonly则不要设置默认⽹关，否则两块⽹卡不能同时正常上⽹。


![image 1](<linux虚拟机配置双网卡(1)(23-14-15).note_images/imageFile1.png>)

hostonly配置 （第⼆块情况）

![image 2](<linux虚拟机配置双网卡(1)(23-14-15).note_images/imageFile2.png>)

桥接都是默认即可。

4、桥接⽹卡全部⾃动配置即可，hostonly原来⽹卡配置不需改动。 5、配好后，在虚拟机中ping 和 主从虚拟机ip，正常都可ping同。

w.baidu.com

在宿主电脑中ping虚拟机两块⽹卡ip，都能ping通，则ok~。
