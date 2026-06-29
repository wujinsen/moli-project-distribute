---
title: Docker 部署指南
slug: docker部署指南
type: guide
status: active
tags: [Docker, 部署, 运维, P1]
sources:
  - raw/wujinsen_markdown/架构/容器/Docker/docker安装.note.md
  - raw/wujinsen_markdown/架构/容器/Docker/docker 部署 java 项目.note.md
  - moli-knowledge/kb/wiki-moli/ops/minio-附件存储指南.md
related: [容器与-docker, 本地启动指南, minio-附件存储指南, 生产环境服务启停脚本]
created: 2026-06-22
updated: 2026-06-22
---

# Docker 部署指南

> 概念 [[容器与-docker]]；本机 Jar 启动 [[本地启动指南]]；MinIO 示例 [[minio-附件存储指南]]。

面向「用 Docker 跑中间件或打包 Java 服务」的操作摘要。茉莉 **dev 不强制 Docker**，但 MinIO/监控栈常用容器。

## 1. 安装（Linux）

```bash
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
# 或 daocloud 一键脚本
sudo systemctl enable docker && sudo systemctl start docker
docker version
```

要求：内核 ≥ 3.10（`uname -r`）。Windows 可用 Docker Desktop。

## 2. 常用命令

| 命令 | 作用 |
|------|------|
| `docker pull nginx` | 拉镜像 |
| `docker images` | 列表 |
| `docker run -d -p 8080:80 --name web nginx` | 后台运行 |
| `docker ps` / `docker ps -a` | 运行中 / 全部容器 |
| `docker exec -it web /bin/bash` | 进容器 |
| `docker logs -f web` | 日志 |
| `docker stop web && docker rm web` | 停并删 |
| `docker build -t moli/user-center:1.0 .` | 构建镜像 |

## 3. 跑 MinIO（与知识库一致）

见 [[minio-附件存储指南]] Docker 一节；bucket `moli-knowledge`。

## 4. Java 微服务镜像（概要）

**Dockerfile** 示例思路：

```dockerfile
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY moli-user-center-server/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java","-jar","app.jar","--spring.profiles.active=prod"]
```

构建前 `mvn package`；运行时注入环境变量或挂载 `application-pro.yml`。

| 注意 | 说明 |
|------|------|
| 时区 | `-Duser.timezone=Asia/Shanghai` |
| 内存 | `-Xms/-Xmx` 见 [[production-jvm启动参数]] |
| 注册中心 | 容器内 Nacos 地址用服务名非 127.0.0.1 |

## 5. docker-compose（多中间件）

适合一次起 MySQL + Redis + Nacos（本地 dev）。项目 `load-test/docker/` 有监控 compose 参考 [[压测监控与prometheus]]。

## 6. 启动方式选择

| 场景 | 推荐 |
|------|------|
| 日常开发 | IDE / `mvn spring-boot:run` [[本地启动指南]] |
| 演示/CI | Docker 镜像 |
| 压测 | 专用 loadtest profile + 可选 compose |

## 7. 常见问题

| 现象 | 处理 |
|------|------|
| 权限 denied | `sudo` 或加入 docker 组 |
| 端口占用 | 改 `-p` 映射 |
| 连不上宿主机 MySQL | Linux 用 `host.docker.internal` 或 host 网络 |
| 镜像过大 | 多阶段构建、JRE 非 JDK |

## 8. 生产

- 用 Registry 私有仓库，勿依赖单机 `docker save`
- 健康检查 `HEALTHCHECK` 或 K8s probe
- 日志挂载 volume 或接 ELK（规划项 [[技术栈与版本]]）
