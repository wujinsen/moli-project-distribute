---
title: Linux ulimit 与文件句柄
slug: linux-ulimit与文件句柄
type: article
status: active
tags: [Linux, 运维, 排查]
sources:
 - raw/wujinsen_markdown/
related: [linux-运维基础, nginx-限流与缓冲调优, 故障排查指南, tomcat-连接器调优]
created: 2026-06-21
updated: 2026-06-21
---

# Linux ulimit 与文件句柄

> Linux 基础 [[ops/linux-运维基础]]；Nginx [[middleware/nginx-限流与缓冲调优]]；Tomcat [[tomcat-连接器调优]]。

每个进程可打开的 **fd（文件/ socket）** 受 `ulimit -n` 限制；高并发下耗尽表现为 `Too many open files`。

## 1. 查看

```bash
ulimit -n # 当前 shell
cat /proc/<pid>/limits
lsof -p <pid> | wc -l
```

## 2. 调优

```ini
# /etc/security/limits.d/app.conf
appuser soft nofile 65535
appuser hard nofile 65535
```

 systemd 服务还可 `LimitNOFILE=65535`。

## 4. 排查流程

1. 日志 `Too many open files`
2. `lsof` 看泄漏（CLOSE_WAIT 堆积 → 应用未关 socket）
3. 对齐 [[ops/网络-端口与连通性排查]]

## 相关

[[java/java-cpu-100排查实战]] · [[ops/生产环境服务启停脚本]]
