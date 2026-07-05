---
title: JVM OOM 与排查入门
slug: jvm-oom与排查入门
type: article
status: active
tags: [jvm, OOM, 排查, 调优]
sources:
- raw/wujinsen_markdown/jvm/GC/G1/G1基本概念.note.md
- raw/wujinsen_markdown/jvm/GC/G1/G1字符串去重.note.md
- raw/wujinsen_markdown/jvm/GC/G1/GC Root.note.md
- raw/wujinsen_markdown/jvm/GC/G1/GC回收过程.note.md
- raw/wujinsen_markdown/jvm/GC/G1/无标题笔记.note.md
- raw/wujinsen_markdown/jvm/GC/各个GC收集器对比.note.md
- raw/wujinsen_markdown/jvm/GC日志分析、年轻代、老年代.note.md
- raw/wujinsen_markdown/jvm/GC的触发时间.note.md
- raw/wujinsen_markdown/jvm/HotSpot.note.md
- raw/wujinsen_markdown/jvm/JDK的命令行工具.note.md
- raw/wujinsen_markdown/jvm/JVM内存划分.note.md
- raw/wujinsen_markdown/jvm/JVM调优总结.note.md
- raw/wujinsen_markdown/jvm/Minor GC和Full GC区别.note.md
- raw/wujinsen_markdown/jvm/OutOfMemoryError异常与实战.note.md
- raw/wujinsen_markdown/jvm/cpu 100%.note.md
- raw/wujinsen_markdown/jvm/eclipse memory analyzer 安装使用示例.note.md
- raw/wujinsen_markdown/jvm/eclipse启动时间插件.note.md
- raw/wujinsen_markdown/jvm/eclipse设置jvm.note.md
- raw/wujinsen_markdown/jvm/jdk7、8、9默认垃圾回收器.note.md
- raw/wujinsen_markdown/jvm/jstatd用法详解.note.md
- raw/wujinsen_markdown/jvm/jstatd结合jvisualvm远程监控hot spot JVM.note.md
- raw/wujinsen_markdown/jvm/jvm的参数查询列表.note.md
- raw/wujinsen_markdown/jvm/《深入理解 Java 内存模型》读书笔记.note.md
- raw/wujinsen_markdown/jvm/个人笔记/JVM虚拟机笔记.note.md
- raw/wujinsen_markdown/jvm/你假笨.note.md
- raw/wujinsen_markdown/jvm/内存分配与回收策略.note.md
- raw/wujinsen_markdown/jvm/周志明的书.note.attach/Java虚拟机：JVM高级特性与最佳实践（第2版）.md
- raw/wujinsen_markdown/jvm/周志明的书.note.md
- raw/wujinsen_markdown/jvm/垃圾回收器.note.md
- raw/wujinsen_markdown/jvm/垃圾收集算法.note.md
- raw/wujinsen_markdown/jvm/对象的内存布局.note.md
- raw/wujinsen_markdown/jvm/对象的创建.note.md
- raw/wujinsen_markdown/jvm/对象的访问定位.note.md
- raw/wujinsen_markdown/jvm/小白都能看得懂的java虚拟机内存模型.note.md
- raw/wujinsen_markdown/jvm/新生代Eden与两个Survivor区的解释.note.md
- raw/wujinsen_markdown/jvm/理解GC日志.note.md
- raw/wujinsen_markdown/jvm/调优/JVM源码分析之Metaspace解密.note.md
- raw/wujinsen_markdown/jvm/调优/java CPU 100% 排查.note.md
- raw/wujinsen_markdown/jvm/调优/九大工具助你玩转Java性能优化.note.md
- raw/wujinsen_markdown/jvm/调优/假笨说-警惕大量类加载器的创建导致诡异的Full GC.note.md
related: [jvm-内存与gc, jvm-面试题]
created: 2026-06-22
updated: 2026-07-05
---

# JVM OOM 与排查入门

> 内存结构 [[java/jvm-内存与gc]]；环境联调。

## 常见 OOM 类型

| 异常 | 常见原因 |
|------|----------|
| Java heap space | 对象泄漏、堆过小、大集合 |
| Metaspace | 类加载过多、CGLIB/Fastjson 动态类 |
| unable to create native thread | 线程数超限 |
| Direct buffer memory | NIO 堆外未释放 |
| GC overhead limit exceeded | 几乎全 GC 仍回收不了 |

## 排查步骤

1. **看日志** — 完整 OOM stack，哪块内存
2. **jmap** — `jmap -heap <pid>`、`jmap -dump:format=b,file=heap.hprof <pid>`
3. **MAT / VisualVM** — 分析 hprof，找 GC Roots 引用链
4. **jstack** — CPU 100% 时查死循环/频繁 GC 线程

## CPU 100% 快查

```bash
top -Hp <pid> # 找高 CPU 线程 tid
printf "%x\n" <tid> # 转 16 进制
jstack <pid> | grep <hex> -A 30
```

常见：Full GC 死循环、正则回溯、锁自旋、日志狂刷。

## 微服务场景提示

- **Druid 连接池** 连接未关 → 堆/连接泄漏表现像 OOM 或获取连接超时
- **线程池队列无界** → heap 涨（任务对象堆积）
- 本地 dev：先 `-Xmx1g` 排除默认过小，再查泄漏

## 预防

- 设 `-Xms=-Xmx`、Metaspace 上限
- 线程池有界队列 + 拒绝策略（见 [[java/java-并发]]）
- 定期看 GC 日志，Full GC 频率异常即告警

原文插图 annex：[[java/annex-小白都能看得懂的java虚拟机内存模型]]

原文插图 annex：[[java/annex-JDK的命令行工具]]

原文插图 annex：[[java/annex-九大工具助你玩转Java性能优化]]

## 原文插图（wujinsen）

> wujinsen 原文插图回迁（T22）· 共 7 组

> 图源 `raw/wujinsen_markdown/jvm/JVM内存划分.note.md` · T22 **B** 档

### 来自：JVM内存划分

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/JVM%E5%86%85%E5%AD%98%E5%88%92%E5%88%86.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/jvm/对象的内存布局.note.md` · T22 **B** 档

### 来自：对象的内存布局

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%AF%B9%E8%B1%A1%E7%9A%84%E5%86%85%E5%AD%98%E5%B8%83%E5%B1%80.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/jvm/对象的创建.note.md` · T22 **B** 档

### 来自：对象的创建

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%AF%B9%E8%B1%A1%E7%9A%84%E5%88%9B%E5%BB%BA.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/jvm/新生代Eden与两个Survivor区的解释.note.md` · T22 **B** 档

### 来自：新生代Eden与两个Survivor区的解释

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E6%96%B0%E7%94%9F%E4%BB%A3Eden%E4%B8%8E%E4%B8%A4%E4%B8%AASurvivor%E5%8C%BA%E7%9A%84%E8%A7%A3%E9%87%8A.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/jvm/理解GC日志.note.md` · T22 **B** 档

### 来自：理解GC日志

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E7%90%86%E8%A7%A3GC%E6%97%A5%E5%BF%97.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/jvm/对象的访问定位.note.md` · T22 **B** 档

### 来自：对象的访问定位

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%AF%B9%E8%B1%A1%E7%9A%84%E8%AE%BF%E9%97%AE%E5%AE%9A%E4%BD%8D.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%AF%B9%E8%B1%A1%E7%9A%84%E8%AE%BF%E9%97%AE%E5%AE%9A%E4%BD%8D.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/jvm/垃圾收集算法.note.md` · T22 **B** 档

### 来自：垃圾收集算法

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%9E%83%E5%9C%BE%E6%94%B6%E9%9B%86%E7%AE%97%E6%B3%95.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%9E%83%E5%9C%BE%E6%94%B6%E9%9B%86%E7%AE%97%E6%B3%95.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%9E%83%E5%9C%BE%E6%94%B6%E9%9B%86%E7%AE%97%E6%B3%95.note_images/imageFile3.png)

原文插图 annex：[[java/annex-小白都能看得懂的java虚拟机内存模型]]

原文插图 annex：[[java/annex-JDK的命令行工具]]

原文插图 annex：[[java/annex-九大工具助你玩转Java性能优化]]

原文插图 annex：[[java/annex-垃圾回收器]]

原文插图 annex：[[java/annex-JVM虚拟机笔记]]

原文插图 annex：[[java/annex-Java虚拟机：JVM高级特性与最佳实践（第2版）]]

原文插图 annex：[[java/annex-《深入理解-Java-内存模型》读书笔记]]

原文插图 annex：[[java/annex-eclipse-memory-analyzer-安装使用示例]]

原文插图 annex：[[java/annex-假笨说-警惕大量类加载器的创建导致诡异的Full-GC]]

原文插图 annex：[[java/annex-cpu-100%]]

原文插图 annex：[[java/annex-java-CPU-100%-排查]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/jvm/Minor GC和Full GC区别.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/jvm/Minor GC和Full GC区别.note.md` · T22 **B** 档

### 来自：Minor GC和Full GC区别

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/Minor%20GC%E5%92%8CFull%20GC%E5%8C%BA%E5%88%AB.note_images/imageFile1.png)
