---
title: HBase查询一条数据的过程. .note（原文插图 annex）
slug: annex-HBase查询一条数据的过程.
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hbase/HBase查询一条数据的过程. .note.md
related: [hbase-列式存储入门]
created: 2026-07-05
updated: 2026-07-05
---

HBase中的Client如何路由到正确的RegionServer 在HBase中，⼤部分的操作都是在RegionServer完成的，Client端想要插⼊，删除，查询数据都需要先 找到相应的 RegionServer。什么叫相应的RegionServer？就是管理你要操作的那个Region的 RegionServer。Client本身并 不知道哪个RegionServer管理哪个Region，那么它是如何找到相应的 RegionServer的？本⽂就是在研究源码的基础上揭秘这个过程。 在前⾯的⽂章“HBase存储架构”中我们已经讨论了HBase基本的存储架构。在此基础上我们引⼊两个特 殊的概念：-ROT-和.META.。这是什么？它们是HBase的两张内置表，从存储结构和操作⽅法的⻆度 来说，它们和其他HBase的表没有任何区别，你可以认为这就是两张普通的表，对于普通表 的操作对 它们都适⽤。它们与众不同的地⽅是HBase⽤它们来存贮⼀个重要的系统信息⸺Region的分布情况以 及每个Region的详细信息。 好了，既然我们前⾯说到-ROT-和.META.可以被看作是两张普通的表，那么它们和其他表⼀样就应该 有⾃⼰的表结构。没错，它们有⾃⼰的表结构，并且这两张表的表结构是相同的，在分析源码之后我 将这个表结构⼤致的画了出来：

我们来仔细分析⼀下这个结构，每条Row记录了⼀个Region的信息。 ⾸先是RowKey，RowKey由三部分组成：TableName, StartKey 和 TimeStamp。RowKey存储的内容 我们⼜称之为Region的Name。哦，还记得吗？我们在前⾯的⽂章中提到的，⽤来存放Region的⽂件 夹的名字是RegionName的Hash值，因为RegionName可能包含某些⾮法字符。现在你应该知道为什 么RegionName会包含⾮法字符 了吧，因为StartKey是被允许包含任何值的。将组成RowKey的三个部 分⽤逗号连接就构成了整个RowKey，这⾥TimeStamp使⽤⼗进制 的数字字符串来表示的。这⾥有⼀ 个RowKey的例⼦： Table1,RK1 0,12345678 然后是表中最主要的Family：info，info⾥⾯包含三个Column：regioninfo, server, serverstartcode。 其中regioninfo就是Region的详细信息，包括StartKey, EndKey 以及每个Family的信息等等。server存 储的就是管理这个Region的RegionServer的地址。 所以当Region被拆分、合并或者重新分配的时候，都需要来修改这张表的内容。 到⽬前为⽌我们已经学习了必须的背景知识，下⾯我们要正式开始介绍Client端寻找RegionServer的整 个过程。我打算⽤⼀个假想的例⼦来学习这个过程，因此我先构建了假想的-ROT-表和.META.表。 我们先来看.META.表，假设HBase中只有两张⽤户表：Table1和Table2，Table1⾮常⼤，被划分成了很 多Region，因此 在.META.表中有很多条Row⽤来记录这些Region。⽽Table2很⼩，只是被划分成了两 个Region，因此在.META.中只有两条Row ⽤来记录。这个表的内容看上去是这个样⼦的：

.META.

现在假设我们要从Table2⾥⾯插寻⼀条RowKey是RK1 0的数据。那么我们应该遵循以下步骤：

- 1. 从.META.表⾥⾯查询哪个Region包含这条数据。
- 2. 获取管理这个Region的RegionServer地址。
- 3. 连接这个RegionServer, 查到这条数据。


好，我们先来第⼀步。问题是.META.也是⼀张普通的表，我们需要先知道哪个RegionServer管理 了.META.表，怎么办？有⼀个⽅法，我们把管 理.META.表的RegionServer的地址放到ZoKeper上⾯ 不久⾏了，这样⼤家都知道了谁在管理.META.。 貌似问题解决了，但对于这个例⼦我们遇到了⼀个新问题。因为Table1实在太⼤了，它的Region实在 太多了，.META.为了存储这些Region信 息，花费了⼤量的空间，⾃⼰也需要划分成多个Region。这就 意味着可能有多个RegionServer在管理.META.。怎么办？在 ZoKeper⾥⾯存储所有管理.META.的 RegionServer地址让Client⾃⼰去遍历？HBase并不是这么做的。 HBase的做法是⽤另外⼀个表来记录.META.的Region信息，就和.META.记录⽤户表的Region信息⼀模 ⼀样。这个表就是-ROT-表。这也解释了为什么-ROT-和.META.拥有相同的表结构，因为他们的原 理是⼀模⼀样的。 假设.META.表被分成了两个Region，那么-ROT-的内容看上去⼤概是这个样⼦的：

-ROT-

![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

这么⼀来Client端就需要先去访问-ROT-表。所以需要知道管理-ROT-表的RegionServer的地址。这 个地址被存在ZoKeper中。默认的路径是： /hbase/rot-region-server 等等，如果-ROT-表太⼤了，要被分成多个Region怎么办？嘿嘿，HBase认为-ROT-表不会⼤到那 个程度，因此-ROT-只会有⼀个Region，这个Region的信息也是被存在HBase内部的。 现在让我们从头来过，我们要查询Table2中RowKey是RK1 0的数据。整个路由过程的主要代码在 org.apache.hadop.hbase.client.HConectionManager.TableServers中： private HRegionLocation locateRegion(final byte [] tableName,

final byte [] row, bolean useCache) throws IOException{

if (tableName = nul | tableName.length = 0) {

throw new IlegalArgumentException(

“table name canot be nul or zero length”);

}

if (Bytes.equals(tableName, ROT_TABLE_NAME) {

synchronized (rotRegionLock) {

/ This block guards against two threads trying to find the rot

/ region at the same time. One wil go do the find while the

/ second waits. The second thread wil not do find.

if (!useCache| rotRegionLocation = nul) {

this.rotRegionLocation = locateRotRegion();

}

return this.rotRegionLocation;

}

} else if (Bytes.equals(tableName, META_TABLE_NAME) {

return locateRegionInMeta(ROT_TABLE_NAME, tableName, row, useCache, metaRegionLock);

} else {

/ Region not in the cache – have to go to the meta. RS

return locateRegionInMeta(META_TABLE_NAME, tableName, row, useCache, userRegionLock);

}

} 这是⼀个递归调⽤的过程： 获取Table2，RowKey为RK1 0的RegionServer

=>

获取.META.，RowKey为Table2,RK1 0, 9的RegionServer

=>

获取-ROT-，RowKey为.META.,Table2,RK1 0, 9, 9的 RegionServer

=>

获取-ROT-的RegionServer

=>

从ZoKeper得到-ROT-的RegionServer

=>

从-ROT-表中查到RowKey最接近（⼩于）

.META.,Table2,RK1 0, 9, 9的⼀条Row，并得到.META.的 RegionServer

=>

从.META.表中查到RowKey最接近（⼩于）Table2,RK1 0, 9的⼀条Row，并得到 Table2的RegionServer

=>

从Table2中查到RK1 0的Row 到此为⽌Client完成了路由RegionServer的整个过程，在整个过程中使⽤了添加“ 9” 后缀并查找最接近（⼩于）RowKey的⽅法。对于这个⽅法⼤家可以仔细揣摩⼀下，并不是很难理解。 最后要提醒⼤家注意两件事情： 在整个路由过程中并没有涉及到MasterServer，也就是说HBase⽇常的数据操作并不需要 MasterServer，不会造成MasterServer的负担。 Client端并不会每次数据操作都做这整个路由过程，很多数据都会被Cache起来。⾄于如何Cache，则 不在本⽂的讨论范围之内。
