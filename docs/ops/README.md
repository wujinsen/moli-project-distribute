# 运维文档

> **正文权威位置：`moli-knowledge/kb/wiki-ops/`**（空间 `moli-ops-manual`）。本目录仅为工程侧索引。

## 知识库工作台（入库 + 治理）

| 文档 | 用途 |
|------|------|
| **[knowledge-workbench-operations.md](knowledge-workbench-operations.md)** | **操作手册（按此走流程）** — 知识入库 + Wiki 治理 + Sync + 体检 |
| [../product/knowledge-workbench-requirements.md](../product/knowledge-workbench-requirements.md) | 产品需求与决策 |
| [../api/KNOWLEDGE_API.md](../api/KNOWLEDGE_API.md) | HTTP 契约 §8–9 |

## 运维手册入口

打开 [`kb/wiki-ops/index.md`](../../moli-knowledge/kb/wiki-ops/index.md)，或 Web 空间 **茉莉系统操作手册**。

| 场景 | wiki-ops 页面 |
|------|----------------|
| 本地启动 | `guides/本地启动指南.md` |
| 用户中心运维 | `guides/user-center-运维要点.md` |
| 数据库 | `guides/数据库初始化指南.md` + `scripts/README.md` |
| 登录鉴权 | `guides/登录与鉴权指南.md` |
| 权限开通 | `guides/权限管理操作指南.md` |
| 故障排查 | `guides/故障排查指南.md` |
| Docker / Nginx / MinIO | `guides/docker部署指南.md` 等 |
| wiki 同步 | `guides/wiki同步指南.md` |
| Lint / 体检分工 | `guides/查询与体检指南.md` |

## 工程配套（非 wiki 正文）

| 路径 | 内容 |
|------|------|
| [`scripts/README.md`](../../scripts/README.md) | `init-db.ps1`、`moli.sql`、utf8mb4 |
| [`docs/sql/README.md`](../sql/README.md) | 知识库/秒杀增量 SQL |
| [`docs/nacos/`](../nacos/) | Nacos 配置样例 |

## 新稿投喂

运维 SOP、变更说明 → **`kb/raw/ops/`** → Ingest → **`wiki-ops/guides/`**（勿写进 `enterprise-kb` 的 `wiki/guides/` 运维副本）。

同步：

```bash
python kb/tools/sync_to_db.py --wiki-dir wiki-ops --space moli-ops-manual
```
