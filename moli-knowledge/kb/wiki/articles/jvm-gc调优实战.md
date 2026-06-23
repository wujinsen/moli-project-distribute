---
title: JVM GC 调优实战
slug: jvm-gc调优实战
type: article
status: active
tags: [JVM, GC, 性能]
sources:
  - raw/wujinsen_markdown/
related: [jvm-内存与gc, jvm-垃圾收集算法与收集器, production-jvm启动参数]
created: 2026-06-21
updated: 2026-06-21
---

# JVM GC 调优实战

> 内存模型 [[jvm-内存与gc]]；收集器 [[jvm-垃圾收集算法与收集器]]；启动参数 [[production-jvm启动参数]]。

## 1. 目标指标

- **STW** P99 可接受（微服务 < 200ms 常见目标）
- **Full GC** 频率趋近 0（G1/ZGC）
- 堆使用率平稳，无 OOM

## 2. G1 调参起点（Java 8+ 茉莉栈）

```
-Xms2g -Xmx2g -XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError
```

## 3. 排查流程

1. `jstat -gcutil` 看 E/O/M/F/Y 频率
2. GC log（`-Xlog:gc*` JDK9+）分析 Pause
3. Heap Dump：MAT 看 Dominator [[jvm-oom与排查入门]]
4. 压测前后对比 [[压测报告解读指南]]

## 4. 误区

- 堆越大 ≠ 越好（回收负担）
- 盲目调 Survivor/Ratio
- 忽略 metaspace / 类加载泄漏

## 相关

[[java-cpu-100排查实战]] · [[jvm-面试题]]
