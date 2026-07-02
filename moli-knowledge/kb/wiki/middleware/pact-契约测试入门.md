---
title: Pact 契约测试入门
slug: pact-契约测试入门
type: article
status: active
tags: [测试, 微服务, API]
sources:
 - raw/wujinsen_markdown/
related: [junit5-单元测试, api-向后兼容策略, mockito-测试实战]
created: 2026-06-21
updated: 2026-06-21
---

# Pact 契约测试入门

> 单测 [[junit5-单元测试]]；兼容 [[middleware/api-向后兼容策略]]；Mock [[mockito-测试实战]]。

**Consumer-Driven Contract**：消费方定义期望交互，提供方验证，防静默破坏。

## 1. 流程

```
Consumer 测试生成 pact.json → Pact Broker 存储
Provider 测试 replay 请求 → 验证响应匹配
```

## 3. 与集成测试

Pact 快、隔离；全链路用 [[database/testcontainers-集成测试]]。

## 相关

[[ci-知识库同步门禁]] · [[代码审查-checklist]]
