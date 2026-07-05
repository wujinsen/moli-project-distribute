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

原文插图 annex：[[java/annex-九大工具助你玩转Java性能优化]]

## 原文插图（wujinsen）

> wujinsen 原文插图回迁（T22）· 共 3 组

> 图源 `raw/wujinsen_markdown/面试笔试/面试题整理/复合索引的优点和注意事项.note.md` · T22 **B** 档

### 来自：复合索引的优点和注意事项

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/%E9%9D%A2%E8%AF%95%E9%A2%98%E6%95%B4%E7%90%86/%E5%A4%8D%E5%90%88%E7%B4%A2%E5%BC%95%E7%9A%84%E4%BC%98%E7%82%B9%E5%92%8C%E6%B3%A8%E6%84%8F%E4%BA%8B%E9%A1%B9.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/面试笔试/面试题整理/游戏排行榜算法设计实现比较.note.md` · T22 **B** 档

### 来自：游戏排行榜算法设计实现比较

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/%E9%9D%A2%E8%AF%95%E9%A2%98%E6%95%B4%E7%90%86/%E6%B8%B8%E6%88%8F%E6%8E%92%E8%A1%8C%E6%A6%9C%E7%AE%97%E6%B3%95%E8%AE%BE%E8%AE%A1%E5%AE%9E%E7%8E%B0%E6%AF%94%E8%BE%83.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/DataBase/mysql/优化/解决mysql占用cpu高的问题.note.md` · T22 **B** 档

### 来自：解决mysql占用cpu高的问题

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E4%BC%98%E5%8C%96/%E8%A7%A3%E5%86%B3mysql%E5%8D%A0%E7%94%A8cpu%E9%AB%98%E7%9A%84%E9%97%AE%E9%A2%98.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E4%BC%98%E5%8C%96/%E8%A7%A3%E5%86%B3mysql%E5%8D%A0%E7%94%A8cpu%E9%AB%98%E7%9A%84%E9%97%AE%E9%A2%98.note_images/imageFile2.png)

原文插图 annex：[[java/annex-九大工具助你玩转Java性能优化]]

原文插图 annex：[[database/annex-Java后台面试-常见问题]]

原文插图 annex：[[java/annex-假笨说-警惕大量类加载器的创建导致诡异的Full-GC]]

原文插图 annex：[[java/annex-java-CPU-100%-排查]]

原文插图 annex：[[database/annex-java-CPU-100%-排查-database]]
