---
title: Prometheus 告警规则设计
slug: prometheus-告警规则设计
type: article
status: active
tags: [Prometheus, 监控, 运维]
sources:
  - raw/wujinsen_markdown/
related: [压测监控与prometheus, spring-boot-actuator监控, 茉莉可观测性与运维体系汇总]
created: 2026-06-21
updated: 2026-06-21
---

# Prometheus 告警规则设计

> 监控栈 [[压测监控与prometheus]]；指标 [[spring-boot-actuator监控]]；体系 [[茉莉可观测性与运维体系汇总]]。

## 1. 告警原则

- **可行动**：每条告警对应 runbook [[故障排查指南]]
- **少而精**：避免 alert fatigue
- **分级**：P0 电话 / P1 工单 / P2 日报

## 2. 规则示例

```yaml
- alert: HighErrorRate
  expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
  for: 5m
  labels: { severity: critical }
```

## 3. 茉莉推荐信号

| 信号 | 阈值思路 |
|------|----------|
| JVM heap > 85% | 持续 10m |
| Druid active/max > 0.9 | 连接泄漏 |
| MQ consumer lag | 业务 SLA |
| Gateway 5xx rate | 与压测基线比 [[压测报告解读指南]] |

## 相关

[[压测监控与prometheus]] · [[druid连接池与监控]]
