---
title: 压测监控与 Prometheus
slug: 压测监控与prometheus
type: article
status: active
tags: [压测, Prometheus, Grafana, 监控]
sources:
 - load-test/README.md
 - load-test/docker/prometheus/prometheus.yml
 - load-test/docker/grafana/dashboards/moli-loadtest.json
related: [秒杀压测指南, 故障排查指南, jvm-oom与排查入门]
created: 2026-06-22
updated: 2026-06-22
---

# 压测监控与 Prometheus

> 压测步骤 ；JVM 排查 [[java/jvm-oom与排查入门]]。

## 启动监控栈

```powershell
.\load-test\scripts\start-monitoring.ps1 -Detach
```

| 组件 | 地址 | 默认账号 |
|------|------|----------|
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3000 | admin / admin |

## 指标端点（loadtest profile）

| 服务 | URL |
|------|-----|
| Gateway | http://localhost:21000/actuator/prometheus |
| User-center | http://localhost:8888/actuator/prometheus |
| Order | http://localhost:8087/actuator/prometheus |

验证：`curl http://localhost:9090/api/v1/targets` 看 UP 状态。

## Grafana Dashboard

预置：**Moli Load Test / Seckill**

常见面板：

- HTTP RPS、P95 延迟
- Gateway 路由维度
- JVM CPU / Heap
- **Druid 连接池**（活跃连接、等待线程）

## 压测时看什么

| 现象 | 可能原因 |
|------|----------|
| P95 陡升 + CPU 100% | GC、线程池满、Lua 热点 |
| Druid 等待高 | MySQL 慢或池过小 |
| Gateway 502 增 | 下游实例不足 |
| Redis 超时 | 单实例瓶颈 → 集群 |

## k6 自带看板

本地 smoke/ramp 脚本可开 **http://127.0.0.1:5665** 实时看板；HTML 报告在 `load-test/reports/`。

## 与生产区别

监控栈在 `load-test/docker/`，**不随业务服务部署**；生产需单独规划 Prometheus/Grafana 或云监控。
