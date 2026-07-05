---
title: linux 内存清理 释放命令 清理linux内存.note（原文插图 annex）
slug: annex-linux-内存清理-释放命令-清理linux内存
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/linux/linux 内存清理 释放命令 清理linux内存.note.md
related: [linux-运维基础]
created: 2026-07-05
updated: 2026-07-05
---

- 1.清理前内存使⽤情况 fre -m
- 2.开始清理 echo 1 > /proc/sys/vm/drop_caches
- 3.清理后内存使⽤情况 fre -m
- 4.完成!查看内存条数命令： dmidecode | grep -A16 "Memory Device$"


![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

![image 3](assets/imageFile3.png)
