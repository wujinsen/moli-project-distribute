# 运维文档

> **日常 SOP 正文**：`moli-knowledge/kb/wiki-moli/`（空间 `moli-ops-manual`）。本目录为 **发布 / 工程索引**。

## v1 发布（P0）

| 文档 | 用途 |
|------|------|
| **[v1-release-runbook.md](v1-release-runbook.md)** | 发布步骤：DB → 配置 → 启动 → Sync → 冒烟 |
| [production-checklist.md](production-checklist.md) | 生产/预发配置与安全检查 |
| **[../../deploy/README.md](../../deploy/README.md)** · [`deploy/上线流程.md`](../../deploy/上线流程.md) | **生产部署**（systemd · env 模板） |
| [sql-migration-order.md](sql-migration-order.md) | **SQL 增量执行顺序** |
| [monitoring-and-logs.md](monitoring-and-logs.md) | 日志与基础监控（v1） |
| [rollback-guide.md](rollback-guide.md) | 发布回滚 |
| [../test/release-smoke-checklist.md](../test/release-smoke-checklist.md) | 发布后冒烟 |

## 知识库工作台

| 文档 | 用途 |
|------|------|
| [knowledge-workbench-operations.md](knowledge-workbench-operations.md) | 入库 + Wiki 治理操作 |
| **[kb-sync-failure-runbook.md](kb-sync-failure-runbook.md)** | **Sync 失败定位 / 重跑 / 三空间验收（KBOPS-A2）** |
| **[knowledge-ops-prd.md](../product/knowledge-ops-prd.md)** | **KBOPS 产品 PRD（优先级与验收）** |
| **[knowledge-ops-frontend.md](../api/knowledge-ops-frontend.md)** | **KB 运维 · 前端对接（给 meiling-ui）** |
| **[kb-t20-design-docs-release.md](kb-t20-design-docs-release.md)** | **T20 后端 + 概要设计 Web 浏览 · 上线 Runbook（2026-07-06）** |
| [../product/moli-v1-release-scope.md](../product/moli-v1-release-scope.md) | v1 功能边界 |

## 茉莉系统手册（wiki-moli）

[`kb/wiki-moli/index.md`](../../moli-knowledge/kb/wiki-moli/index.md) · 空间 **茉莉系统手册**

| 场景 | 页面 |
|------|------|
| **全项目文档地图** | `guides/项目文档总览.md` |
| 本地启动 | `guides/本地启动指南.md` |
| 数据库 | `guides/数据库初始化指南.md` + [scripts/README.md](../../scripts/README.md) |
| 登录鉴权 | `guides/登录与鉴权指南.md` |
| 故障排查 | `guides/故障排查指南.md` |
| wiki 同步 | `ops/wiki同步指南.md` |

## 工程配套

| 路径 | 内容 |
|------|------|
| [scripts/README.md](../../scripts/README.md) | init-db.ps1、moli.sql |
| [sql/README.md](../sql/README.md) | 增量 DDL |
| [nacos/](../nacos/) | 配置样例 |

同步 ops 空间：

```bash
python kb/tools/sync_to_db.py --wiki-dir wiki-moli --space moli-ops-manual
```
