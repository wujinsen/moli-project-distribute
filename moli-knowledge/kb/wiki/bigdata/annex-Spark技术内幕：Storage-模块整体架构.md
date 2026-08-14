---
title: Spark技术内幕：Storage 模块整体架构.note（原文插图 annex）
slug: annex-Spark技术内幕：Storage-模块整体架构
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Storage 模块整体架构.note.md
related: [spark-核心概念与实践]
created: 2026-07-05
updated: 2026-07-05
---

## Spark技术内幕：Storage 模块整体架构

分类： 2015-01-18 19:13 7869⼈阅读 (16) 收藏

Spark 架构探索 评论 举报

sparkspark storage

Storage模块负责了Spark计算过程中所有的存储，包括基于Disk的和基于Memory的。⽤户在实际 编程中，⾯对的是RDD，可以将RDD的数据通过调⽤org.apache.spark.rdd.RDD#cache将数据持 久化；持久化的动作都是由Storage模块完成的。包括Shuffle过程中的数据，也都是由Storage模 块管理的。可以说，RDD实现了⽤户的逻辑，⽽Storage则管理了⽤户的数据。本章将讲解 Storage模块的实现。

# 1.1 模块整体架构

org.apache.spark.storage.BlockManager是Storage模块与其他模块交互最主要的类，它提供了读 和写Block的接⼝。 这⾥的Block，实际上就对应了RDD中提到的partition，每⼀个partition都会对 应⼀个Block。每个Block由唯⼀的Block ID（org.apache.spark.storage.RDDBlockId） 标识，格 式是"rdd_" + rddId + "_" + partitionId。

BlockManager会运⾏在Driver和每个Executor上。⽽运⾏在Driver上的BlockManger负责整个Job 的Block的管理⼯作；运⾏在Executor上的BlockManger负责管理该Executor上的Block，并且向 Driver的BlockManager汇报Block的信息和接收来⾃它的命令。

![image 1](assets/imageFile1.png)

各个主要类的功能说明：

- 1) org.apache.spark.storage.BlockManager： 提供了Storage模块与其他模块的交互接⼝，管 理Storage模块。

- 2) org.apache.spark.storage.BlockManagerMaster： Block管理的接⼝类，主要通过调⽤ org.apache.spark.storage.BlockManagerMasterActor来完成。

- 3) org.apache.spark.storage.BlockManagerMasterActor： 在Driver节点上的Actor，负责track 所有Slave节点的Block的信息

- 4) org.apache.spark.storage.BlockManagerSlaveActor：运⾏在所有的节点上，接收来⾃ org.apache.spark.storage.BlockManagerMasterActor的命令，⽐如删除某个RDD的数据，删除某 个Block，删除某个Shuffle数据，返回某些Block的状态等。

- 5) org.apache.spark.storage.BlockManagerSource：负责搜集Storage模块的Metric信息，包 括最⼤的内存数，剩余的内存数，使⽤的内存数和使⽤的Disk⼤⼩。这些是通过调⽤ org.apache.spark.storage.BlockManagerMaster的getStorageStatus接⼝实现的。

- 6) org.apache.spark.storage.BlockObjectWriter：⼀个抽象类，可以将任何的JVM object写⼊ 外部存储系统。注意，它不⽀持并发的写操作。

- 7) org.apache.spark.storage.DiskBlockObjectWriter：⽀持直接写⼊⼀个⽂件到Disk，并且还 ⽀持⽂件的append。实际上它是org.apache.spark.storage.BlockObjectWriter的⼀个实现。现在 下⾯的类在需要Spill数据到Disk时，就是通过它来完成的：

- a) org.apache.spark.util.collection.ExternalSorter

- b) org.apache.spark.shuffle.FileShuffleBlockManager


- 8) org.apache.spark.storage.DiskBlockManager：管理和维护了逻辑上的Block和存储在Disk 上的物理的Block的映射。⼀般来说，⼀个逻辑的Block会根据它的BlockId⽣成的名字映射到⼀个 物理上的⽂件。这些物理⽂件会被hash到由spark.local.dir（或者通过SPARK_LOCAL_DIRS来设 置）上的不同⽬录中。

- 9) org.apache.spark.storage.BlockStore：存储Block的抽象类。现在它的实现有：


- a) org.apache.spark.storage.DiskStore

- b) org.apache.spark.storage.MemoryStore

- c) org.apache.spark.storage.TachyonStore


- 10) org.apache.spark.storage.DiskStore：实现了存储Block到Disk上。其中写Disk是通过 org.apache.spark.storage.DiskBlockObjectWriter实现的。

- 11) org.apache.spark.storage.MemoryStore：实现了存储Block到内存中。

- 12) org.apache.spark.storage.TachyonStore：实现了存储Block到Tachyon上。

- 13) org.apache.spark.storage.TachyonBlockManager：管理和维护逻辑上的Block和Tachyon⽂ 件系统上的⽂件之间的映射。这点和org.apache.spark.storage.DiskBlockManager功能类似。

- 14) org.apache.spark.storage.ShuffleBlockFetcherIterator：实现了取Shuffle的Blocks的逻辑， 包括读取本地的和发起⽹络请求读取其他节点上的。具体实现可以参照《Shuffle模块详解》。
