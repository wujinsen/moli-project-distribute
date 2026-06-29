---
title: API 向后兼容策略
slug: api-向后兼容策略
type: article
status: active
tags: [API, 设计, 微服务]
sources:
 - raw/wujinsen_markdown/
related: [openapi3-与接口文档, dubbo-分组版本与环境, git-分支与发布策略]
created: 2026-06-21
updated: 2026-06-21
---

# API 向后兼容策略

> 文档 [[openapi3-与接口文档]]；Dubbo 版本 [[dubbo-分组版本与环境]]；发布 [[git-分支与发布策略]]。

## 1. 兼容原则

- **只增不删**：新字段 optional；旧字段 deprecated 周期
- **不改语义**：枚举值只追加
- **URL 版本**：`/v1/` 或 Header `Accept-Version`

## 2. 破坏性变更

| 变更 | 做法 |
|------|------|
| 删字段 | 双写过渡期 + 新 API 版本 |
| 改类型 | 新 endpoint，旧映射层 |
| Dubbo 接口 | `version=2.0.0` 并行 [[dubbo-分组版本与环境]] |

## 3. 契约保障

Consumer 驱动契约测试 [[pact-契约测试入门]]；CI 门禁 [[ci-知识库同步门禁]]。

## 相关

[[架构决策-adr]] · [[蓝绿与滚动发布]]
