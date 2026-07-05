---
title: executor启动和任务处理流程.note（原文插图 annex）
slug: annex-executor启动和任务处理流程
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/spark/spark源码分析/executor启动和任务处理流程.note.md
related: [spark-核心概念与实践]
created: 2026-07-05
updated: 2026-07-05
---

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


在创建taskScheduler的时候SparkContext.createTaskScheduler(this, master)，进⾏了 new SparkDeploySchedulerBackend（）的步骤，在SparkDeploySchedulerBackend的84⾏，执⾏ 了app运⾏使⽤的调度器为CoarseGrainedExecutorBackendval command = Command("org.apache.spark.executor.CoarseGrainedExecutorBackend", //-----------指定调⽤的executor是哪 个 在下⾯的new ApClient（）中，有传⼊comond 查看ApClient的onStart（）⽅法，调⽤了registerWithMaster（），然后调⽤了 tryRegisterAlMasters（）⽅法 在tryRegisterAlMasters（）⽅法中向master发送了消息 RegisterApplication(appDescription, self) master接收到消息后，向apclient发送消息RegisteredAplication，监听任务运⾏状态，然后调 ⽤schedule（）⽅法 在schedule（）⽅法中调⽤startExecutorOnWorks（）⽅法，在worker上调度和启动 executor，在此⽅法中计算每个worker上可⽤的资源，并且分配每个worker上需要启动的资 源，调⽤allocateWorkerResourceExecutor⽅法启动executor allocateWorkerResourceExecutor⽅法中，调⽤lauchExecutor（）⽅法，启动 executor 向worker发送消息，启动executor，向appclient发送消息，改变executor的状态

![image 1](assets/imageFile1.png)
