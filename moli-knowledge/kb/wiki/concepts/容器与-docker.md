---
title: 容器与 Docker
slug: 容器与-docker
type: concept
status: active
tags: [Docker, 容器, 部署, DevOps]
sources:
  - raw/wujinsen_markdown/架构/容器/Docker/docker 部署 java 项目.note.md
related: [docker部署指南, 本地启动指南, minio-附件存储指南, 生产环境服务启停脚本]
created: 2026-06-22
updated: 2026-06-22
---

# 容器与 Docker

> 操作 [[docker部署指南]]；本地 Jar 启动 [[本地启动指南]]；MinIO 容器 [[minio-附件存储指南]]。

**Docker** 是容器引擎：把应用 + 依赖打成**镜像**，在隔离的**容器**里运行。茉莉项目 **dev 以本机 Maven/Jar 为主**；Docker 用于中间件（MySQL/Redis/Nacos/MinIO）与生产部署。

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

## 3. 茉莉栈中的 Docker

| 组件 | 常见用法 |
|------|----------|
| MinIO | `docker run` 9000/9001 [[minio-附件存储指南]] |
| MySQL/Redis/Nacos | 开发 docker-compose（可选，非仓库强制） |
| **Java 微服务** | 可打镜像部署；当前文档以 **Jar + 脚本** 为主 [[生产环境服务启停脚本]] |

## 4. 何时用 Docker 跑 Java 服务

| 适合 | 不适合 |
|------|--------|
| 环境一致、CI/CD | 纯本地调试 IDE 断点 |
| 多实例扩缩 | 未容器化的遗留脚本运维 |

## 5. 与 K8s

Docker 单机容器；**Kubernetes** 编排多节点。ROADMAP 可观测/生产或需 K8s，当前 wiki 未展开。

## 6. 学习路径

安装与命令 [[docker部署指南]] → 再考虑 Java 镜像与 compose。
