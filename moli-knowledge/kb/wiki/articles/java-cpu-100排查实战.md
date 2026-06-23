---
title: Java CPU 100% 排查实战
slug: java-cpu-100排查实战
type: article
status: active
tags: [JVM, 排查, CPU, 运维]
sources:
  - raw/wujinsen_markdown/jvm/调优/java CPU 100% 排查.note.md
  - raw/wujinsen_markdown/面试笔试/面试题整理/java CPU 100% 排查.note.md
  - raw/wujinsen_markdown/DataBase/mysql/优化/解决mysql占用cpu高的问题.note.md
related: [故障排查指南, jvm-oom与排查入门, production-jvm启动参数, druid连接池与监控, mysql-深分页与慢sql优化]
created: 2026-06-22
updated: 2026-06-22
---

# Java CPU 100% 排查实战

> 决策树 [[故障排查指南]]；OOM [[jvm-oom与排查入门]]。

## 1. 先分 Java vs 系统

```bash
top          # 看哪个 PID
top -H -p PID   # 看哪个线程占 CPU
printf "%x\n" TID   # 转 16 进制
jstack PID | grep -A 30 <hex-tid>
```

## 2. 常见 Java 原因

| 栈特征 | 可能原因 |
|--------|----------|
| 业务方法死循环 | 代码 bug |
| GC 线程满 CPU | Full GC 频繁、堆过小 [[jvm-内存与gc]] |
| 正则/JSON 热点 | 优化或缓存 |
| 锁竞争 | `jstack` 见 BLOCKED |

## 3. MySQL CPU 高（联动）

- 慢 SQL、缺索引 [[mysql-索引]]
- 深分页 [[mysql-深分页与慢sql优化]]
- 连接数过多 → Druid [[druid连接池与监控]]

## 4. 茉莉压测场景

k6 压测时 CPU 高：区分 **Gateway Netty** vs **Tomcat 业务**；看 Prometheus [[压测监控与prometheus]]。

## 5. 处理顺序

1. 采样 jstack 3 次（间隔 5s）确认稳定热点
2. 临时扩容/限流 [[sentinel-限流与熔断]]
3. 修代码或调 JVM [[production-jvm启动参数]]
4. 复盘写 wiki / log

## 相关

[[linux-运维基础]] · [[故障排查指南]]
