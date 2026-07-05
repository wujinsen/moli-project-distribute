---
title: synchronized 与锁原理
slug: synchronized与锁原理
type: article
status: active
tags: [java, synchronized, 锁升级, 偏向锁, 监视器]
sources:
- raw/wujinsen_markdown/面试笔试/Java面试题精选/【68期】面试官：对并发熟悉吗？说说Synchronized及实现原理.note.md
related: [java-并发, jmm与happens-before, volatile与可见性, java-并发面试题]
created: 2026-06-22
updated: 2026-07-05
---

# synchronized 与锁原理

> 枢纽 [[java/java-并发]]；JMM 背景 [[java/jmm与happens-before]]。

## 三种用法

```java
public synchronized void instanceMethod() { } // 锁 this
public static synchronized void staticMethod() { } // 锁 Class 对象
synchronized (obj) { /* 代码块 */ } // 锁 obj
```

同一时刻，**同一把锁**上的 synchronized 块/方法互斥。

## 三大作用

1. **互斥**：同一监视器下只有一个线程执行
2. **可见性**：释放锁会把工作内存刷新到主内存；获锁会从主内存加载（happens-before）
3. **有序性**：在监视器内 as-if-serial，且 unlock hb 后续 lock

## 实现原理（对象头 + 监视器）

- 每个 Java 对象关联一个 **Monitor**（监视器锁）。
- 对象头 Mark Word 存储锁状态：无锁 → **偏向锁** → **轻量级锁** → **重量级锁**（OS mutex）。
- **锁升级**（JDK 6+）：多数无竞争场景偏向锁几乎无开销；有竞争则膨胀为重量级。
- 同步块编译后生成 **monitorenter / monitorexit** 字节码；异常退出也会 monitorexit。

## synchronized vs volatile

| | synchronized | volatile |
|---|--------------|----------|
| 互斥 | ✅ | ❌ |
| 可见性 | ✅ | ✅ |
| 适用 | 复合操作、临界区 | 状态标志、单次读/写 |

`i++` 即使用 volatile 字段也不原子，需 synchronized 或 Atomic*。

## 使用注意

- **锁粒度**：锁整个方法 vs 锁细粒度对象；避免在热点路径锁过大对象。
- **死锁**：固定加锁顺序；tryLock + 超时。
- **别在锁内做 IO/远程调用**：持锁时间过长拖垮吞吐。

## 面试要点

- 静态 synchronized 锁的是 **Class**，实例方法锁 **this**。
- 可重入：同一线程可多次获取同一把监视器锁。
- JDK 6 后锁优化（偏向/轻量/自适应自旋）使 synchronized 不再是「绝对重量级」。

详见 [[java/java-并发面试题]] Q2～Q4。

<!-- t22-wujinsen-images:raw/wujinsen_markdown/面试笔试/Java面试题精选/【68期】面试官：对并发熟悉吗？说说Synchronized及实现原理.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/面试笔试/Java面试题精选/【68期】面试官：对并发熟悉吗？说说Synchronized及实现原理.note.md` · T22 **B** 档

### 来自：【68期】面试官：对并发熟悉吗？说说Synchronized及实现原理

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Java%E9%9D%A2%E8%AF%95%E9%A2%98%E7%B2%BE%E9%80%89/%E3%80%9068%E6%9C%9F%E3%80%91%E9%9D%A2%E8%AF%95%E5%AE%98%EF%BC%9A%E5%AF%B9%E5%B9%B6%E5%8F%91%E7%86%9F%E6%82%89%E5%90%97%EF%BC%9F%E8%AF%B4%E8%AF%B4Synchronized%E5%8F%8A%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Java%E9%9D%A2%E8%AF%95%E9%A2%98%E7%B2%BE%E9%80%89/%E3%80%9068%E6%9C%9F%E3%80%91%E9%9D%A2%E8%AF%95%E5%AE%98%EF%BC%9A%E5%AF%B9%E5%B9%B6%E5%8F%91%E7%86%9F%E6%82%89%E5%90%97%EF%BC%9F%E8%AF%B4%E8%AF%B4Synchronized%E5%8F%8A%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86.note_images/imageFile2.png)
