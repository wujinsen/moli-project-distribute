---
title: Arthas 在线诊断入门
slug: arthas-在线诊断
type: article
status: active
tags: [JVM, 排查, 运维]
sources:
- raw/wujinsen_markdown/架构/性能调优/Arthas/arthas.note.md
related: [java-cpu-100排查实战, jvm-oom与排查入门]
created: 2026-06-21
updated: 2026-07-05
---

# Arthas 在线诊断入门

> CPU [[java/java-cpu-100排查实战]]；故障树 ；OOM [[java/jvm-oom与排查入门]]。

阿里开源 **Java 诊断**工具，attach 运行中进程，无需重启。

## 1. 常用命令

| 命令 | 用途 |
|------|------|
| `dashboard` | 线程/内存/GC 概览 |
| `thread -n 3` | CPU 最高线程 |
| `thread -b` | 阻塞线程 |
| `jad` / `mc` / `redefine` | 反编译、热更新（谨慎） |
| `watch` / `trace` | 方法入参/耗时 |
| `heapdump` | 导出 dump |

## 3. 安全

- 生产需授权窗口；attach 权限管控
- `stop` 卸载 agent

## 相关

[[ops/skywalking-安装与链路追踪]] · [[middleware/压测监控与prometheus]]

## 批次#1312 增补（wujinsen P1）

合并 Arthas 安装使用 raw。
