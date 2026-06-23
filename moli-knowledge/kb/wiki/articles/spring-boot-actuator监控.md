---
title: Spring Boot Actuator 监控
slug: spring-boot-actuator监控
type: article
status: active
tags: ['Spring Boot', '监控']
sources:
  - raw/wujinsen_markdown/
related: [micrometer-与指标暴露, 压测监控与prometheus]
created: 2026-06-22
updated: 2026-06-22
---

# Spring Boot Actuator 监控

## 端点

- `/actuator/health` `/metrics` `/info`
- 生产暴露需鉴权或内网
- 与 Prometheus scrape 配合
