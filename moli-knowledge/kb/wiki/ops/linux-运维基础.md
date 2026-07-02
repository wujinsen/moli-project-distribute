---
title: Linux 运维基础
slug: linux-运维基础
type: concept
status: active
tags: [Linux, 运维, 服务器]
sources:
 - raw/wujinsen_markdown/moli项目/运维/启动脚本.note.md
 - raw/wujinsen_markdown/架构/运维/安全/防火墙命令.note.md
 - raw/wujinsen_markdown/前端/Vue/linux 安装nodejs.note.md
related: [nginx反向代理与前端部署指南, docker部署指南, 故障排查指南, jenkins-ci入门]
created: 2026-06-22
updated: 2026-06-22
---

# Linux 运维基础

生产/测试机常见为 CentOS/Ubuntu 云主机，跑 MySQL、Redis、Nacos、Nginx、MinIO 与各 Java 进程。本文归纳**与项目相关的**运维动作，不含具体主机密码（凭据见团队密钥库）。

## 1. 常用命令

| 场景 | 命令 |
|------|------|
| 查端口占用 | `lsof -i:8080` / `ss -tlnp \| grep 8080` |
| 杀进程 | `kill -9 $(lsof -ti:8201)` |
| 磁盘/内存 | `df -h` · `free -m` · `top` |
| 日志 tail | `tail -f logs/xxx.log` |
| 防火墙 | `firewall-cmd --list-ports`（CentOS） |

## 2. Java 服务启停

典型脚本模式（与 顺序一致）：

```bash
nohup java -jar user-center-server.jar --spring.profiles.active=pro > logs/uc.log 2>&1 &
```

生产 JVM 参数见 [[java/production-jvm启动参数]]。压测 profile 见 [[middleware/loadtest-profile与压测登录]]。

## 3. 中间件路径习惯

| 组件 | 常见安装/操作 |
|------|----------------|
| Nginx | `/usr/local/nginx/sbin/nginx -s reload` |
| MySQL | `systemctl start mysqld` |
| Redis | `redis-cli -a <pwd> ping` |
| Nacos | `./startup.sh -m standalone` |
| MinIO | 二进制或 Docker，见 [[minio-附件存储指南]] |

## 4. 监控与排障

- CPU 100%：见 raw「java CPU 100% 排查」思路 — 先 `top` 找 PID，再 `jstack`。
- 连不上服务：安全组/防火墙端口、bind 地址、Nacos 注册。
- 系统决策树：。

## 5. 与 Docker 的关系

单机可裸装；多环境复现优先 + compose，减少「仅在某台机器能跑」的问题。

## 相关

[[nginx反向代理与前端部署指南]] · · [[ops/容器与-docker]]
