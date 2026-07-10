---


title: BI服务产品说明
slug: BI服务产品说明
type: guide
status: active
tags: [微服务, BI, 骨架, product, P2]
sources:
  - docs/product/moli-v1-release-scope.md
  - docs/design/bi-module-overview.md
  - moli-ai/README.md
related: [bi服务, 茉莉-bi-报表规划, 网关]
created: 2026-06-20
updated: 2026-06-20
---

# BI 服务产品说明（v1 占位）

> **模块**：`moli-ai` · Nacos **`bi-server`** · **不纳入 v1 业务验收**  
> **设计**：`docs/design/bi-module-overview.md`

## 一句话

v1 仅验证 **网关 + 注册 + Shiro 骨架**；`GET /demo/test` 探针。

## v1 范围

| 项 | 状态 |
|----|------|
| `/BiServer/demo/test` | ✅ 骨架 |
| 报表 / 大屏 / 数据集 | ❌ v2+ |

## 验收（可选冒烟）

- `docs/test/bi-smoke.md`
- `docs/test/release-smoke-checklist.md` G4

## v2 展望

见 [[茉莉-bi-报表规划]] 与 `docs/design/bi-module-overview.md` §6。

## 相关

- [[bi服务]]
- [[网关]]
