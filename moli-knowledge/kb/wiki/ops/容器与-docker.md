---
title: 容器与 Docker
slug: 容器与-docker
type: concept
status: active
tags: [Docker, 容器, 部署, DevOps]
sources:
- raw/wujinsen_markdown/架构/容器/Docker/Docker Compose.note.md
- raw/wujinsen_markdown/架构/容器/Docker/docker 部署 java 项目.note.md
- raw/wujinsen_markdown/架构/容器/Docker/docker可视化工具portainer.note.md
- raw/wujinsen_markdown/架构/容器/Docker/docker基本命令.note.md
- raw/wujinsen_markdown/架构/容器/Docker/docker安装.note.md
- raw/wujinsen_markdown/架构/容器/Docker/docker挂载命令.note.md
- raw/wujinsen_markdown/架构/容器/Docker/容器基本使用.note.md
- raw/wujinsen_markdown/架构/容器/Docker/某小公司项目环境部署演变之路.note.md
related: [生产环境服务启停脚本]
created: 2026-06-22
updated: 2026-07-05
---

# 容器与 Docker

> 操作 ；本地 Jar 启动 ；MinIO 容器 `moli-knowledge/kb/wiki-moli/ops/minio-附件存储指南.md`。

**Docker** 是容器引擎：把应用 + 依赖打成**镜像**，在隔离的**容器**里运行。目标系统 **dev 以本机 Maven/Jar 为主**；Docker 用于中间件（MySQL/Redis/Nacos/MinIO）与生产部署。

## 1. 核心概念

| 概念 | 说明 |
|------|------|
| **Image** | 只读模板（类） |
| **Container** | 镜像运行实例（对象） |
| **Registry** | 镜像仓库（Docker Hub 等） |
| **Dockerfile** | 构建镜像脚本 |

## 2. vs 虚拟机

| | 容器 | VM |
|---|------|-----|
| 启动 | 秒级 | 分钟级 |
| 资源 | 共享内核、轻量 | 完整 OS |
| 隔离 | 进程级 | 硬件虚拟化 |

## 4. 何时用 Docker 跑 Java 服务

| 适合 | 不适合 |
|------|--------|
| 环境一致、CI/CD | 纯本地调试 IDE 断点 |
| 多实例扩缩 | 未容器化的遗留脚本运维 |

## 5. 与 K8s

Docker 单机容器；**Kubernetes** 编排多节点。ROADMAP 可观测/生产或需 K8s，当前 wiki 未展开。

## 6. 学习路径

安装与命令 → 再考虑 Java 镜像与 compose。
## 批次#1310 增补（wujinsen P0）

合并 Docker 安装/命令/挂载/Java 部署 raw。

