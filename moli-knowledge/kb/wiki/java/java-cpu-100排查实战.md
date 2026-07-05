---
title: Java CPU 100% 排查实战
slug: java-cpu-100排查实战
type: article
status: active
tags: [JVM, 排查, CPU, 运维]
sources:
- raw/wujinsen_markdown/DataBase/mysql/优化/解决mysql占用cpu高的问题.note.md
- raw/wujinsen_markdown/jvm/调优/JVM源码分析之Metaspace解密.note.md
- raw/wujinsen_markdown/jvm/调优/java CPU 100% 排查.note.md
- raw/wujinsen_markdown/jvm/调优/九大工具助你玩转Java性能优化.note.md
- raw/wujinsen_markdown/jvm/调优/假笨说-警惕大量类加载器的创建导致诡异的Full GC.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/JVM群面试题.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/Java后台面试 常见问题.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/Java面试通关要点汇总集之微服务篇参考答案.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/Java面试通关要点汇总集基础篇之参考答案.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/Java面试通关要点汇总集核心篇.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/MySQL数据库MyISAM和InnoDB存储引擎的比较.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/java CPU 100% 排查.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/复合索引的优点和注意事项.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/游戏排行榜算法设计实现比较.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/面试题整理.note.md
related: [jvm-oom与排查入门, production-jvm启动参数, druid连接池与监控, mysql-深分页与慢sql优化]
created: 2026-06-22
updated: 2026-07-05
---

# Java CPU 100% 排查实战

> 决策树 ；OOM [[java/jvm-oom与排查入门]]。

## 1. 先分 Java vs 系统

```bash
top # 看哪个 PID
top -H -p PID # 看哪个线程占 CPU
printf "%x\n" TID # 转 16 进制
jstack PID | grep -A 30 <hex-tid>
```

## 2. 常见 Java 原因

| 栈特征 | 可能原因 |
|--------|----------|
| 业务方法死循环 | 代码 bug |
| GC 线程满 CPU | Full GC 频繁、堆过小 [[java/jvm-内存与gc]] |
| 正则/JSON 热点 | 优化或缓存 |
| 锁竞争 | `jstack` 见 BLOCKED |

## 3. MySQL CPU 高（联动）

- 慢 SQL、缺索引 [[database/mysql-索引]]
- 深分页 [[database/mysql-深分页与慢sql优化]]
- 连接数过多 → Druid [[database/druid连接池与监控]]

## 5. 处理顺序

1. 采样 jstack 3 次（间隔 5s）确认稳定热点
2. 临时扩容/限流 [[middleware/sentinel-限流与熔断]]
3. 修代码或调 JVM [[java/production-jvm启动参数]]
4. 复盘写 wiki / log

## 相关

[[ops/linux-运维基础]] ·
