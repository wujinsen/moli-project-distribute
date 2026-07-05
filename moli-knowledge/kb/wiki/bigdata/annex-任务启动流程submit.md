---
title: 任务启动流程submit.note（原文插图 annex）
slug: annex-任务启动流程submit
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/spark/spark源码分析/任务启动流程submit.note.md
related: [spark-核心概念与实践]
created: 2026-07-05
updated: 2026-07-05
---

- 1.
- 2.

- a.
- b.
- c.


- 3.


在脚本spark-submit中调⽤SparkSubmit的main⽅法 在main⽅法中执⾏如下：

获取提交参数，繁琐就打印 通过提交的action匹配是什么⾏为 如果是提交任务，执⾏submit（args）⽅法

在submit（args）⽅法中执⾏如下： 准备运⾏环境 定义doRunMain（）⽅法

- a.
- b.

ⅰ.

- c.


调⽤runmain（）⽅法，执⾏⽤户提交主类的main（）⽅法 调⽤doRunMain（）⽅法

![image 1](assets/imageFile1.png)
