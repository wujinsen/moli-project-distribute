---
title: Prometheus 告警规则设计
slug: prometheus-告警规则设计
type: article
status: active
tags: [Prometheus, 监控, 运维]
sources:
 - raw/wujinsen_markdown/
related: [压测监控与prometheus, spring-boot-actuator监控, 可观测性与运维体系汇总]
created: 2026-06-21
updated: 2026-06-21
---

# Prometheus 告警规则设计

> 监控栈 [[压测监控与prometheus]]；指标 [[spring-boot-actuator监控]]；体系。

## 1. 告警原则

- **可行动**：每条告警对应 runbook
- **少而精**：避免 alert fatigue
- **分级**：P0 电话 / P1 工单 / P2 日报

## 2. 规则示例

```yaml
- alert: HighErrorRate
 expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
 for: 5m
 labels: { severity: critical }
```

## 相关

[[压测监控与prometheus]] · [[druid连接池与监控]]
