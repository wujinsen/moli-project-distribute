---
title: JVM OOM 与排查入门
slug: jvm-oom与排查入门
type: article
status: active
tags: [jvm, OOM, 排查, 调优]
sources:
  - raw/wujinsen_markdown/jvm/OutOfMemoryError异常与实战.note.md
  - raw/wujinsen_markdown/jvm/调优/java CPU 100% 排查.note.md
  - raw/wujinsen_markdown/jvm/cpu 100%.note.md
related: [jvm-内存与gc, jvm-面试题, 故障排查指南]
created: 2026-06-22
updated: 2026-06-22
---

# JVM OOM 与排查入门

> 内存结构 [[jvm-内存与gc]]；茉莉环境联调 [[故障排查指南]]。

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
top -Hp <pid>          # 找高 CPU 线程 tid
printf "%x\n" <tid>    # 转 16 进制
jstack <pid> | grep <hex> -A 30
```

常见：Full GC 死循环、正则回溯、锁自旋、日志狂刷。

## 微服务场景提示

- **Druid 连接池** 连接未关 → 堆/连接泄漏表现像 OOM 或获取连接超时
- **线程池队列无界** → heap 涨（任务对象堆积）
- 本地 dev：先 `-Xmx1g` 排除默认过小，再查泄漏

## 预防

- 设 `-Xms=-Xmx`、Metaspace 上限
- 线程池有界队列 + 拒绝策略（见 [[java-并发]]）
- 定期看 GC 日志，Full GC 频率异常即告警
