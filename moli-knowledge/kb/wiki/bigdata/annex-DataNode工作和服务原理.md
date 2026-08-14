---
title: DataNode工作和服务原理.note（原文插图 annex）
slug: annex-DataNode工作和服务原理
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hadoop/DataNode工作和服务原理.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

DataNode查看他的属性，可以分成以下⼏个⽅⾯：

- 1.offerService()⽅法，此⽅法在DataNode主循环中执⾏，做的事情包括和NameNode⼼跳交互；通知NameNode ⼀段时间以来收到的block；本机block的报告

- 2.DataXceiverServer，主要处理block的读写

- 3.BlockScanner，对本机block的扫描和校验处理

- 4.FSDataset，本机block存储的⼊⼝

- 5.ipcServer，主要是DataNode和DataNode之间recover block时使⽤。


这⾥主要说明第1点，其它⼏点在另外⼀⽂中已经介绍过，offerService的主要流程⻅下图：

![image 1](assets/imageFile1.png)

offerService是在⼀个while循环⾥⾯被执⾏，只要DataNode存活，就⼀直被执⾏，⼯作流程如下：

- 1.如果距离上⼀次heartbeat时间超过了指定的时间，调⽤namenode.sendHeartbeat⽅法，namenode是ipc的框架 下的⼀个proxy，可以认为就是远程的NameNode，该⽅法向NameNode汇报DataNode还存活，以及汇报DataNode 的利⽤率，NameNode会返回⼀系列关于本DataNode的BlockCommand

- 2.DataNode处理返回的BlockCommand，BlockCommand有DNA_TRANSFER(发送block去指定的 DataNode),DNA_INVALIDATE(删除指定的本机上⾯的block),DNA_SHUTDOWN(DataNode停⽌⼯ 作),DNA_REGISTER(DataNode向NameNode注册),DNA_FINALIZE(DataNode完成升级流程)， DNA_RECOVERBLOCK(recover block)等

- 3.报告⾃从上⼀次⼼跳以来DataNode收到的block信息，调⽤namenode.blockReceived⽅法

- 4.如果距离上⼀次block report时间超过了指定的时间，调⽤namenode.blockReport⽅法，NameNode会返回⼀系 列的DatanodeCommand，接下来的处理就和第2点⼀样了

- 5.根据其他条件，适当的做⼀些处理，主要是为了考虑资源利⽤等等
