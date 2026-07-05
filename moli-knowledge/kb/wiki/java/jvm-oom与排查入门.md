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
