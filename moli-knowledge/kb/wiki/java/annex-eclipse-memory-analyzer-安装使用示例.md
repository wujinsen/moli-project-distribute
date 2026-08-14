---
title: eclipse memory analyzer 安装使用示例.note（原文插图 annex）
slug: annex-eclipse-memory-analyzer-安装使用示例
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/jvm/eclipse memory analyzer 安装使用示例.note.md
related: [jvm-gc调优实战]
created: 2026-07-05
updated: 2026-07-05
---

⼀、准备⼯作

分析较⼤的dump⽂件（根据我⾃⼰的经验2G以上的dump⽂件就需要使⽤以下介绍的⽅法，不然mat 会出现oom）需要调整虚拟机参数

找个64位的系统在MemoryAnalyzer.ini设置-Xmx2g

⼆、开始使⽤MAT进⾏OOM分析

第⼀步，启动mat ,选择File->Open Heap Dump 选择你的dump⽂件。下⾯开始等待,mat解析dump⽂ 件需要花⼀些时间，在解析的同时会在硬盘上写⼊⼀些解析结果⽂件，这样下次打开时速度会快很 多。有时候mat在解析过程中可能会出现出错的情况，这个时候可以将那些临时⽂件删除以后重试第⼀ 步，如果你的rp够好的话重试也许会解析成功。

第⼆步，查看内存泄漏分析报表。mat解析完成以后会出现如下图的提示：

![image 1](assets/imageFile1.png)

因为我们就是为了查找内存泄漏的问题，所以保持默认选项直接点“Finish”就可以。

Mat会⾮常直观的展现内存泄漏的可疑点，类似下⾯的报表可以直接看到某个线程占⽤了⼤量的内存

![image 2](assets/imageFile2.png)

问题的详细分析信息：

![image 3](assets/imageFile3.png)

第三步，开始寻找导致内存泄漏的代码点。这时往往需要打开对象依赖关系树形视图，点击如图按钮 即可。

![image 4](assets/imageFile4.png)

这时会看到如下视图

![image 5](assets/imageFile5.png)

这个视图的左边⼤区域可以看到对象的依赖关系，选中某个对象以后可以在左边⼩窗⼝查看对象的⼀ 些属性。如果属性的值是⼀些内存地址你还可以点击⼯具栏的搜索按钮来搜索具体的对象信息。在进 ⾏具体分析的时候MAT只是起了帮助你进⾏分析的⼯具的功能，OOM问题分析没有固定⽅法和准则。 只能发挥你敏锐的洞察⼒，结合源代码，对内存中的对象进⾏分析从⽽找到代码中的BUG.

使⽤贴⼠：

关于shallow size、retained size(摘⾃ http://www.360doc.com/content/11/0830/16/4520139_144514377.shtml)

Shallow size就是对象本身占⽤内存的⼤⼩，不包含对其他对象的引⽤，也就是对象头加成员变量（不 是成员变量的值）的总和。在32位系统上，对象头占⽤8字节，int占⽤4字节，不管成员变量（对象或 数组）是否引⽤了其他对象（实例）或者赋值为null它始终占⽤4字节。故此，对于String对象实例来 说，它有三个int成员（3*4=12字节）、⼀个char[]成员（1*4=4字节）以及⼀个对象头（8字节），总 共3*4 +1*4+8=24字节。根据这⼀原则，对String a=”rosen jiang”来说，实例a的shallow size也是24 字节

Retained size是该对象⾃⼰的shallow size，加上从该对象能直接或间接访问到对象的shallow size之 和。换句话说，retained size是该对象被GC之后所能回收到内存的总和。为了更好的理解retained size，不妨看个例⼦。

把内存中的对象看成下图中的节点，并且对象和对象之间互相引⽤。这⾥有⼀个特殊的节点GC Roots，正解！这就是reference chain的起点。

![image 6](assets/imageFile6.png)

![image 7](assets/imageFile7.png)

从obj1⼊⼿，上图中蓝⾊节点代表仅仅只有通过obj1才能直接或间接访问的对象。因为可以通过GC Roots访问，所以左图的obj3不是蓝⾊节点；⽽在右图却是蓝⾊，因为它已经被包含在retained集合 内。

所以对于左图，obj1的retained size是obj1、obj2、obj4的shallow size总和；右图的retained size是 obj1、obj2、obj3、obj4的shallow size总和。obj2的retained size可以通过相同的⽅式计算。

如何查看某⼀个对象占⽤的内存空间

- 1.按以下⽅式打开新窗⼝即可

- 2.输⼊类名（输⼊类的全名）


![image 8](assets/imageFile8.png)

![image 9](assets/imageFile9.png)
