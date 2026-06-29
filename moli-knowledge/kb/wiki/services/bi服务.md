---
title: bi服务
slug: bi服务
type: service
status: active
tags: [微服务, BI, 骨架]
sources:
  - moli-ai/README.md
  - docs/api/bi-api.md
related: [用户中心, 网关, 本地启动指南]
created: 2026-06-22
updated: 2026-06-28
---

# BI 服务（bi-server）

BI / 报表微服务 **v1 骨架**：可启动、接 Shiro+Dubbo，尚无真实报表能力。

| 项 | 值 |
|----|----|
| 服务名 | `bi-server` |
| HTTP | **1128** |
| Dubbo | **20883**（Consumer） |
| 网关 | `/BiServer/**` |

## v1 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/demo/test` | 连通探针，返回 `test success` |

## 文档（工程权威）

| 类型 | 链接 |
|------|------|
| API | `docs/api/bi-api.md` |
| 测试 | `docs/test/bi-smoke.md` |
| 模块 README | `moli-ai/README.md` |

## 运维手册

[[系统操作手册入口]] · `wiki-ops/develop/bi服务.md`。

## 概念关联

- [[用户中心]]
- [[网关]]
- [[bi报表服务演进路线]]
