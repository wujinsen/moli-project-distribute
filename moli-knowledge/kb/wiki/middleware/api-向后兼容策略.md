---
title: API 向后兼容策略
slug: api-向后兼容策略
type: article
status: active
tags: [API, 设计, 微服务]
sources:
- raw/wujinsen_markdown/ (enterprise-kb/middleware 专题页)
related: [openfeign-与-http客户端]
created: 2026-06-21
updated: 2026-07-05
---

# API 向后兼容策略

> 文档 [[middleware/openfeign-与-http客户端]]；Dubbo 版本 `moli-knowledge/kb/wiki-moli/develop/茉莉-dubbo-group版本.md`；发布 `moli-knowledge/kb/wiki-moli/develop/茉莉-规范-git分支.md`。

## 1. 兼容原则

- **只增不删**：新字段 optional；旧字段 deprecated 周期
- **不改语义**：枚举值只追加
- **URL 版本**：`/v1/` 或 Header `Accept-Version`

## 2. 破坏性变更

| 变更 | 做法 |
|------|------|
| 删字段 | 双写过渡期 + 新 API 版本 |
| 改类型 | 新 endpoint，旧映射层 |
| Dubbo 接口 | `version=2.0.0` 并行 `moli-knowledge/kb/wiki-moli/develop/茉莉-dubbo-group版本.md` |

## 3. 契约保障

Consumer 驱动契约测试 [[middleware/pact-契约测试入门]]；CI 门禁 `moli-knowledge/kb/wiki-moli/ops/wiki同步指南.md`。

## 相关

`moli-knowledge/kb/wiki-moli/develop/茉莉-规范-adr.md` · [[ops/蓝绿与滚动发布]]
