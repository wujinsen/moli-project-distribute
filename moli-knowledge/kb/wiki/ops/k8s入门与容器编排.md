---
title: K8s 入门与容器编排
slug: k8s入门与容器编排
type: concept
status: active
tags: [Kubernetes, K8s, 容器]
sources:
- load-test/k8s/k6-seckill-testrun.yaml
- raw/wujinsen_markdown/架构/容器/k8s/K8s运维锦囊，19个常见故障解决方法.note.md
- raw/wujinsen_markdown/架构/容器/k8s/Kuboard/Kuboard.note.md
- raw/wujinsen_markdown/架构/容器/k8s/k8s相关网站.note.md
- raw/wujinsen_markdown/架构/容器/k8s/最新、最全、最详细的 K8S 学习笔记总结（2021最新版）（一）.note.md
- raw/wujinsen_markdown/架构/容器/k8s/最新、最全、最详细的 K8S 学习笔记总结（2021最新版）（二）.note.md
related: [容器与-docker, jenkins-ci入门]
created: 2026-06-22
updated: 2026-07-05
---

# K8s 入门与容器编排

**Kubernetes** 编排容器：部署、扩缩容、服务发现、滚动升级。当前常见以**本地/单机 + Docker Compose** 为主；K8s 用于**压测分布式 k6** 或未来生产上云。

## 1. 核心对象

| 对象 | 作用 |
|------|------|
| Pod | 最小调度单元（1+ 容器） |
| Deployment | 无状态应用副本与滚动更新 |
| Service | 集群内稳定访问（ClusterIP/NodePort/LB） |
| ConfigMap/Secret | 配置与密钥 |
| Ingress | HTTP 路由（类似 `moli-knowledge/kb/wiki-moli/ops/nginx反向代理与前端部署指南.md`） |

## 3. 与 Docker 区别

- Docker：单机跑容器。
- K8s：多节点调度、自愈、HPA 扩缩容。
- 入门先掌握 [[ops/容器与-docker]]，再上 K8s。

## 4. 上 K8s 注意点

- **Redis Session 一致**：各副本仍须连同一 Redis 集群。
- **Nacos**：注册地址改为 K8s Service DNS。
- **配置**：`application-pro.yml` 或 Nacos 配置，勿 baked 进镜像。
- **健康检查**：readiness/liveness 探针对接 Actuator。

## 5. 学习路径

1. minikube/k3d 本地集群
2. 部署一个 Spring Boot jar 镜像
3. 配置 Ingress 暴露
4. 压测 Job 跑 k6

## 相关

 · [[ops/jenkins-ci入门]] ·
## 批次#1310 增补（wujinsen P0）

合并 K8s 学习笔记、故障锦囊、Kuboard raw。

原文插图 annex：[[ops/annex-K8s运维锦囊，19个常见故障解决方法]]

原文插图 annex：[[ops/annex-最新、最全、最详细的-K8S-学习笔记总结（2021最新版）（一）]]

原文插图 annex：[[ops/annex-最新、最全、最详细的-K8S-学习笔记总结（2021最新版）（二）]]
