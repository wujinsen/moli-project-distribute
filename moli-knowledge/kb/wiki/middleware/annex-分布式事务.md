---
title: 分布式事务.note（原文插图 annex）
slug: annex-分布式事务
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/分布式事务/分布式事务.note.md
related: [分布式事务]
created: 2026-07-05
updated: 2026-07-05
---

分布式事务模型与规范

DTP模型

分布式事务主要的规范是JTA/XA， 其中：JTA是Java的事务管理器规范， XA是⼯业标准的X/Open CAE规范，可被两阶段提交及回滚的事务资源定义

- 1.XA XA是由X/Open组织提出的分布式事务的规范。XA规范主要定义了(全局)事务管理器(Transaction Manager)和(局部)资源管理器(Resource Manager)之间的接⼝。XA接⼝是双向的系统接⼝，在事务管 理器（Transaction Manager）以及⼀个或多个资源管理器（Resource Manager）之间形成通信桥 梁。XA之所以需要引⼊事务管理器是因为，在分布式系统中，从理论上讲（参考Fischer等的论⽂）， 两台机器理论上⽆法达到⼀致的状态，需要引⼊⼀个单点进⾏协调。事务管理器控制着全局事务，管 理事务⽣命周期，并协调资源。资源管理器负责控制和管理实际资源（如数据库或JMS队列）。下图 说明了事务管理器、资源管理器，与应⽤程序之间的关系：

图1.XA规范下的分布式事务各类参与者之间的关系

- 2.JTA


![image 1](assets/imageFile1.png)

作为java平台上事务规范JTA（Java Transaction API）也定义了对XA事务的⽀持，实际上，JTA是基 于XA架构上建模的，在JTA 中，事务管理器抽象为javax.transaction.TransactionManager接⼝，并通 过底层事务服务（即JTS）实现。像很多其他的java规范⼀样，JTA仅仅定义了接⼝，具体的实现则是 由供应商(如J2E⼚商)负责提供，⽬前JTA的实现主要由以下⼏种：

- 1.J2E容器所提供的JTA实现(JBos)

- 2.独⽴的JTA实现:如JOTM，Atomikos.这些实现可以应⽤在那些不使⽤J2E应⽤服务器的环境⾥⽤以 提供分布事事务保证。如Tomcat,Jety以及普通的java应⽤。 两阶段提交


幂等

补偿操作
