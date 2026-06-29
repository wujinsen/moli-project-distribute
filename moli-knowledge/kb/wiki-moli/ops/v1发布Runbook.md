---
title: v1发布Runbook
slug: v1发布Runbook
type: guide
status: active
tags: [发布, 运维, v1, P0]
sources:
  - docs/ops/v1-release-runbook.md
  - docs/product/moli-v1-release-scope.md
related: [生产环境检查清单, SQL迁移顺序, 测试文档索引, 发布回滚指南, 监控与日志, 本地启动指南, wiki同步指南]
created: 2026-06-20
updated: 2026-06-20
---

# v1.0 发布 Runbook

> 范围：`docs/product/moli-v1-release-scope.md` · 冒烟：[[测试文档索引]]

## 1. 发布前检查

1. 代码已合并，CI 通过（含 knowledge `lint-strict` 若启用）
2. 完成 [[生产环境检查清单]]
3. 确认发布窗口与回滚联系人

## 2. 数据库

**新环境**：`.\scripts\init-db.ps1`（见 [[数据库初始化指南]]）

**已有库增量**：[[SQL迁移顺序]] — 字符集必须 `utf8mb4` + `source`

## 3. 配置

| 组件 | 检查项 |
|------|--------|
| Nacos | namespace、数据源、Redis |
| Redis | 全服务 **同一 database** |
| 知识库 LLM | 可空则降级 |
| 网关 | 四路由 |

生产密钥 **勿提交 Git**。

## 4. 启动顺序

```
Nacos / MySQL / Redis
→ user-center (:8888)
→ order (:8087)
→ bi (:1128, 可选)
→ knowledge (:8090)
→ gateway (:21000)
→ 前端 / Nginx
```

详见 [[本地启动指南]]。

## 5. Wiki → DB 同步

```bash
cd moli-knowledge/kb
bash tools/ci/run_sync.sh dry-run-all
bash tools/ci/run_sync.sh sync-all
```

详见 [[wiki同步指南]]。

## 6. 冒烟

执行 `docs/test/release-smoke-checklist.md` 全部 **P0**。

## 7. 发布后

观察 gateway / user-center / knowledge 日志；验证登录；DB 备份。

## 8. 回滚

详见 [[发布回滚指南]]。

## 工程原文

`docs/ops/v1-release-runbook.md`
