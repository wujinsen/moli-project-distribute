---
title: K8s 健康检查探针
slug: k8s-健康检查探针
type: article
status: active
tags: [K8s, 运维, 监控]
sources:
 - raw/wujinsen_markdown/
related: [k8s入门与容器编排, spring-boot-actuator监控, docker部署指南]
created: 2026-06-21
updated: 2026-06-21
---

# K8s 健康检查探针

> K8s 入门 [[k8s入门与容器编排]]；Actuator [[spring-boot-actuator监控]]；Docker。

## 1. 三类探针

| 探针 | 作用 |
|------|------|
| **startup** | 慢启动保护，成功前不杀 liveness |
| **liveness** | 进程死锁/僵死时重启 Pod |
| **readiness** | 未就绪不接入 Service 流量 |

## 2. Actuator 映射

```yaml
livenessProbe:
 httpGet:
 path: /actuator/health/liveness
 port: 8080
readinessProbe:
 httpGet:
 path: /actuator/health/readiness
```

依赖（DB/Redis）放 **readiness**，避免全集群重启。

## 相关

[[gateway-超时与重试配置]] · [[prometheus-告警规则设计]]
