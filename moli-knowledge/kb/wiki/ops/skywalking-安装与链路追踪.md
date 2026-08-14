---
title: SkyWalking 安装与链路追踪
slug: skywalking-安装与链路追踪
type: guide
status: active
tags: [SkyWalking, APM, 链路追踪, 可观测性]
sources:
- raw/wujinsen_markdown/架构/性能监控/skywalking/skywalking 安装.note.md
- raw/wujinsen_markdown/架构/性能监控/问题排查/程序奔溃排查命令.note.md
related: [jenkins-ci入门, k8s入门与容器编排, arthas-在线诊断, mdc-日志链路上下文, rpc-超时重试与链路]
created: 2026-07-05
updated: 2026-07-05
---

# SkyWalking 安装与链路追踪

> 与 [[java/arthas-在线诊断]]（在线诊断）、[[java/mdc-日志链路上下文]]（日志 traceId）互补；RPC 超时见 [[middleware/rpc-超时重试与链路]]。

## 1. 组件

| 组件 | 作用 |
|------|------|
| **OAP** | 收集 Trace/Metrics，聚合存储 |
| **UI** | 可视化（拓扑、慢调用） |
| **Agent** | `-javaagent` 字节码增强，无侵入埋点 |

## 2. 安装步骤（raw 摘要）

1. 下载 `apache-skywalking-apm-x.x.x` 与 `skywalking-java-agent-x.x.x`
2. 启动 OAP：`bin/oapService.sh`（或 `startup.sh` 新版）
3. 启动 UI：`bin/webappService.sh`（UI 默认 **8080** 或 **8081**，以包内配置为准）
4. 业务 JVM 挂载 Agent：

```bash
java -javaagent:/opt/skywalking-agent/skywalking-agent.jar \
  -Dskywalking.agent.service_name=order-server \
  -jar app.jar
```

## 3. 端口备忘

| 服务 | 常见端口 |
|------|----------|
| OAP gRPC | 11800 |
| OAP HTTP | 12800 |
| UI | 8080 / 8081 |

## 4. 接入注意

- `service_name` 与微服务名一致，便于拓扑对齐 Dubbo/Gateway。
- Agent 与 OAP **版本尽量匹配**。
- 高 QPS 下关注 OAP 存储（ES/H2）；生产勿用默认 H2。
- K8s 部署可将 Agent 挂 InitContainer 或 sidecar，见 [[ops/k8s入门与容器编排]]。

## 5. 与 Sentinel / Prometheus

- SkyWalking：**调用链 + 慢 SQL 定位**
- Sentinel：**限流熔断**（[[middleware/sentinel-限流与熔断]]）
- Prometheus：**指标告警**（[[ops/prometheus-告警规则设计]]）

三者分工不同，可并存。
