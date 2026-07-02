---
title: JMM 与 happens-before
slug: jmm与happens-before
type: concept
status: active
tags: [java, JMM, 内存模型, happens-before, 重排序]
sources:
 - raw/wujinsen_markdown/jvm/《深入理解 Java 内存模型》读书笔记.note.md
related: [java-并发, volatile与可见性, synchronized与锁原理, java-并发面试题]
created: 2026-06-22
updated: 2026-06-22
---

# JMM 与 happens-before

> 并发枢纽 [[java/java-并发]]；volatile 如何落实可见性见 [[java/volatile与可见性]]。

## JMM 抽象

每个线程有**工作内存**（本地缓存抽象），共享变量在主内存。线程 A 写变量 → 先改工作内存 → **刷新到主内存**；线程 B 读 → 从主内存加载到工作内存。通信必须经过主内存。

## 重排序（三类）

1. **编译器优化**重排序（单线程语义不变前提下）
2. **指令级并行**重排序（无数据依赖时可乱序）
3. **内存系统**重排序（写缓冲区、缓存导致读写在其他线程看来乱序）

多线程下重排序 → **可见性**与**有序性**问题（如 DCL 单例半初始化对象暴露）。

## happens-before（程序员必背 4 条 + 传递）

若 A happens-before B，则 A 的结果对 B **可见**，且 A 在顺序上排在 B 之前（不要求时间先后，只保证可见性与约束）。

| 规则 | 含义 |
|------|------|
| **程序顺序** | 同线程内，前面的操作 hb 后面 |
| **监视器锁** | unlock hb 后续 lock（同一把锁） |
| **volatile** | 对 volatile 写 hb 后续对该域读 |
| **传递性** | A hb B 且 B hb C → A hb C |

synchronized 的释放-获取语义、volatile 的读写语义，都通过 hb 保证可见性。

## 内存屏障

JMM 在生成的指令中插入 **LoadLoad / StoreStore / LoadStore / StoreLoad** 屏障，禁止特定处理器重排序。volatile 写后常配合 StoreLoad 屏障。

## 与 synchronized / volatile 的关系

- **synchronized**：释放锁前的写对获锁线程可见（监视器锁规则）。
- **volatile**：写立即刷主存语义 + 禁止与读写相关的重排序（见 [[java/volatile与可见性]]）。

## 面试一句话

> happens-before 描述可见性与有序性约束，不是「谁先执行」；JMM 通过锁、volatile、final 等规则，在允许优化的前提下给程序员可依赖的内存语义。
