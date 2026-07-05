---
title: 生产环境 JVM 启动参数
slug: production-jvm启动参数
type: article
status: active
tags: [jvm, 运维, 生产, GC]
sources:
- raw/wujinsen_markdown/ (enterprise-kb/java 专题页)
related: [jvm-内存与gc, jvm-oom与排查入门, jvm-垃圾收集算法与收集器, 生产环境服务启停脚本]
created: 2026-06-22
updated: 2026-07-05
---

# 生产环境 JVM 启动参数

> 内存/GC 概念 [[java/jvm-内存与gc]]；OOM 排查 [[java/jvm-oom与排查入门]]；启停脚本 [[ops/生产环境服务启停脚本]]。

摘自历史生产运维笔记（JDK 8 + CMS 时代），供理解**大堆 + 详细 GC 日志 + OOM dump** 的配置思路；**新部署优先 G1**（见 [[java/jvm-垃圾收集算法与收集器]]）。

## 1. 大内存 Web 服务示例（~10G 堆）

典型 `-server` 大堆配置（原文档整理，参数间缺 `-` 的已补正）：

```text
-server
-Xms10240m -Xmx10240m
-Xmn6144m
-XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=512m
-XX:SurvivorRatio=8
-XX:+UseParNewGC -XX:+UseConcMarkSweepGC
-XX:+UseCMSCompactAtFullCollection
-XX:MaxTenuringThreshold=15
-XX:CMSFullGCsBeforeCompaction=5
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/home/xxx/logs/heapdump.hprof
-Dspring.profiles.active=prod
```

| 参数 | 含义 |
|------|------|
| `-Xms/-Xmx` 相等 | 避免堆动态扩缩带来的抖动 |
| `-Xmn` | 年轻代大小；需与 SurvivorRatio、晋升速率联调 |
| ParNew + CMS | JDK8 常见组合；**JDK9+ 默认 G1，CMS 已移除** |
| `HeapDumpOnOOM` | OOM 时落盘，配合 `jmap`/MAT 分析 |

## 2. 中等堆 + 详细 GC 日志（6G）

另一套 6G 堆 + **可运维 GC 日志** 片段：

```text
-server -Xms6144m -Xmx6144m
-XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=512m
-XX:CompressedClassSpaceSize=512m
-XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=5
-XX:GCTimeRatio=19
-Xnoclassgc -XX:+DisableExplicitGC
-XX:+UseParNewGC -XX:+UseConcMarkSweepGC
-XX:+UseCMSCompactAtFullCollection
-XX:CMSFullGCsBeforeCompaction=0
-XX:-CMSParallelRemarkEnabled
-XX:CMSInitiatingOccupancyFraction=70
-XX:SoftRefLRUPolicyMSPerMB=0
-XX:+PrintGCDetails -XX:+PrintGCDateStamps
-Xloggc:/opt/logs/gc.log
-Dlog4j2.formatMsgNoLookups=true
```

| 参数 | 运维价值 |
|------|----------|
| `-Xloggc` + PrintGC* | 历史 JDK8 GC 日志；JDK9+ 改用 `-Xlog:gc*` |
| `CMSInitiatingOccupancyFraction=70` | 老年代 70% 触发 CMS |
| `DisableExplicitGC` | 禁止 `System.gc()` 误触发 Full GC |
| `log4j2.formatMsgNoLookups` | Log4j2 安全项 |

## 4. 现代化建议（JDK 11+）

1. **弃 CMS**，改用 G1：`-XX:+UseG1GC`，按停顿目标 `-XX:MaxGCPauseMillis` 调优
2. GC 日志：`-Xlog:gc*,gc+age=trace:file=gc.log:time,uptime,level,tags`
3. 保留 `-XX:+HeapDumpOnOutOfMemoryError` + 明确 `HeapDumpPath`
4. 容器/K8s 内用 `-XX:MaxRAMPercentage` 代替手写 `-Xmx`

## 5. 排查联动

- CPU 100% / 线程：`jstack` → [[java/jvm-oom与排查入门]]
- 老年代满 / FGC 频繁：看 GC 日志 + 堆直方图
- OOM：heapdump 路径是否与脚本一致、磁盘是否可写

> 以上为历史参数归档，**上线前需按实际 JDK 版本与堆大小重新压测验证**。
