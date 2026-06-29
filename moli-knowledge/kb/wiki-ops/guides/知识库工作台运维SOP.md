---
title: 知识库工作台运维SOP
slug: 知识库工作台运维SOP
type: guide
status: active
tags: [知识库, Ingest, Wiki治理, 运维]
sources:
  - docs/ops/knowledge-workbench-operations.md
related: [知识库使用指南, wiki同步指南, 查询与体检指南, API契约索引, 项目文档总览]
created: 2026-06-20
updated: 2026-06-20
---

# 知识库工作台 · 运维 SOP

> **完整逐步操作**（含截图级步骤）：[`docs/ops/knowledge-workbench-operations.md`](../../../../docs/ops/knowledge-workbench-operations.md)  
> 产品方案在 **enterprise-kb**；API 见 [[API契约索引]]。

## 模块对照

| 模块 | 路由 | Web |
|------|------|-----|
| 知识入库 | `knowledge/ingest/index` | ✅ Expert + Express |
| Wiki 治理 | `knowledge/wiki-govern/index` | ⚠️ Lint + AI（script/auto 见 API 规格） |

## 共用前置

1. 启动：[[本地启动指南]]（含 knowledge :8090、网关 :21000）
2. 登录拿 token → `Authorization` 头
3. 空间权限：目标空间 **editor**

## 串联流程

```
Ingest（raw→commit）→ Wiki 治理（lint-space）→ sync-all → 健康体检
```

命令见 [[wiki同步指南]]。

## 文档索引

| 角色 | 文档 |
|------|------|
| 运维 | 本文 + [knowledge-workbench-operations.md](../../../../docs/ops/knowledge-workbench-operations.md) |
| 前端 | [knowledge-workbench-frontend.md](../../../../docs/api/knowledge-workbench-frontend.md) §10 |
| 测试 | [knowledge-ingest-acceptance.md](../../../../docs/test/knowledge-ingest-acceptance.md) |

## 相关

[[项目文档总览]] · [[知识库使用指南]]
