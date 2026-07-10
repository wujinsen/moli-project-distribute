---
title: wiki 同步指南
slug: wiki同步指南
type: guide
status: active
tags: [知识库, 运维, P0, 同步]
sources:
  - moli-knowledge/kb/tools/sync_to_db.py
  - moli-knowledge/kb/tools/ci/run_sync.sh
  - moli-knowledge/kb/AGENTS.md
related: [知识库设计哲学-docs-as-code, 知识库使用指南, 知识库服务, 数据库初始化指南, 知识库三操作, 项目文档总览]
created: 2026-06-22
updated: 2026-07-01
---

# wiki 同步指南

> markdown 权威源在 `moli-knowledge/kb/` 下 **三个 wiki 目录**；Java 后端只读 `kb_document`。服务实体 [[知识库服务]]；API 浏览见 [[知识库使用指南]]。

面向「wiki 改完后怎么进 MySQL、让 `/kb/page` 有正链/反链」的操作说明。

## 1. 三空间映射（单一真相表）

**改完任意 wiki 目录后，优先用 §4 的 `sync-all` 一次同步三空间**，避免只 sync 一个空间或 `--space` 写错。

| wiki 目录（相对 `kb/`） | `space_code` | `space_id` | 用途 | 初始化 SQL |
|-------------------------|--------------|------------|------|------------|
| `wiki/` | `enterprise-kb` | `900000000000000001` | **仅占位 index**（茉莉正文勿写此目录） | `docs/sql/03_knowledge_schema.sql` |
| `wiki-moli/` | `moli-ops-manual` | `900000000000000003` | **茉莉系统手册**：产品·技术·测试·运维·操作（全项目） | `docs/sql/07_kb_space_ops_manual.sql` |
| `wiki-jp-exam/` | `jp-fe-ap-exam` | `900000000000000002` | 日本語 FE/AP 题库 | `docs/sql/04_kb_space_jp_exam.sql` |

**规则**：

- 每篇 guide **只活在一个 wiki 目录 / 一个空间**（运维类只在 `wiki-moli/`，见 wiki-moli（茉莉系统手册） [[项目文档总览]] 跳转说明）。
- `sync_to_db.py` 的 `--wiki-dir` 与 `--space` **必须成对**，与上表一致。
- `index.md`、`log.md` **不同步**（各目录内的 meta 页）。

## 2. 同步方向与原则

| 方向 | 说明 |
|------|------|
| **wiki → DB** | `sync_to_db.py` 单向写入 |
| **DB ↛ wiki** | 库内改文档**不会**回写 markdown |
| **幂等** | 按 `(space_id, slug)` upsert；`content_hash` 未变则 skip |

## 3. slug 与分类（moli-ops-manual）

slug = wiki 相对路径去扩展名，与 `[[双链]]`、`graph/edges.jsonl` 节点名一致。

**moli-ops-manual 分类=目录**（Web「分类管理」与 markdown 一级目录绑定；sync 按 slug 首段回填 `category_id`）：

| 分类名 | `dir_slug` | 默认体裁 | 典型内容 |
|--------|------------|----------|----------|
| 操作指导 | `guides` | `guide` | 本地启动、登录鉴权、知识库使用 |
| 产品 | `product` | `guide` | PRD 索引 |
| 技术 | `develop` | `guide` | 架构/API/SQL 索引、微服务实体、概念 |
| 运维 | `ops` | `guide` | 发布 Runbook、部署、排障、wiki 同步 |
| 测试 | `test` | `guide` | 测试/冒烟索引 |

| 文件 | slug |
|------|------|
| `wiki-moli/guides/本地启动指南.md` | `guides/本地启动指南` |
| `wiki-moli/guides/本地启动指南.md` | `guides/本地启动指南` |
| `wiki-moli/develop/用户中心.md` | `develop/用户中心` |
| `wiki-moli/ops/wiki同步指南.md` | `ops/wiki同步指南` |
| `wiki-jp-exam/interview/fe-xxx.md` | `interview/fe-xxx` |

API 请求示例：`GET /kb/page?slug=guides/本地启动指南&spaceId=900000000000000003`（ops 空间）。

## 4. 前置条件

1. MySQL 已导入全库 + 知识库表（见 [[数据库初始化指南]]）
2. 三空间种子已导入：`03_knowledge_schema.sql` + 按需 `04_kb_space_jp_exam.sql`、`07_kb_space_ops_manual.sql`
3. Python 3 + `pip install pymysql`（仅真正写库时需要）

CI / 本地统一入口：`moli-knowledge/kb/tools/ci/run_sync.sh`（`lint-strict-all` · `sync-all` · `init-schema`）。**PR 合并前须过** `dry-run-all` + `lint-strict-all`（KBOPS-A1）。

## 5. 推荐流程

### 5.1 先 dry-run（不连库）

**enterprise-kb（默认 `wiki/`）**：

```bash
cd D:\work\moli_project\moli-project-distribute
python moli-knowledge/kb/tools/sync_to_db.py --dry-run
```

**三空间逐个预览**（改动了哪个目录就跑哪条；或一次跑全）：

```bash
bash moli-knowledge/kb/tools/ci/run_sync.sh dry-run-all
```

等价于对 `wiki` / `wiki-moli` / `wiki-jp-exam` 各执行一次 `--dry-run`。

输出：待 insert/update/skip/delete 的 slug 列表、关系边统计。**先核对再写库。**

### 5.2 真正同步（推荐）

**日常默认：三空间一次同步**

```bash
bash moli-knowledge/kb/tools/ci/run_sync.sh sync-all
```

CI main 分支 merge 后亦走此命令。

**单空间调试**（仅当你明确只改了某一目录时）：

```bash
# enterprise-kb
python moli-knowledge/kb/tools/sync_to_db.py \
  --host 127.0.0.1 --port 3306 --user root --password 12345678 \
  --db moli --space enterprise-kb

# moli-ops-manual
python moli-knowledge/kb/tools/sync_to_db.py \
  --wiki-dir wiki-moli --space moli-ops-manual \
  --host 127.0.0.1 --port 3306 --user root --password 12345678 --db moli

# jp-fe-ap-exam
python moli-knowledge/kb/tools/sync_to_db.py \
  --wiki-dir wiki-jp-exam --space jp-fe-ap-exam \
  --host 127.0.0.1 --port 3306 --user root --password 12345678 --db moli
```

参数默认值对齐 `moli-knowledge-server` 的 `application-dev.yml`。

### 5.3 验证

enterprise-kb 目录 meta（`spaceId=900000000000000001`）：

```bash
curl "http://127.0.0.1:21000/KnowledgeServer/kb/index?spaceId=900000000000000001" \
  -H "Authorization: login_token_xxx"
```

moli-ops-manual 单页（示例）：

```bash
curl "http://127.0.0.1:21000/KnowledgeServer/kb/page?slug=guides/本地启动指南&spaceId=900000000000000003" \
  -H "Authorization: login_token_xxx"
```

单页应含 `outLinks` / `backLinks`（来自 `kb_relation`）。

> 完整 browse API 见 `docs/KNOWLEDGE_API.md` §2（含 `/kb/index/search`、`/kb/index/locate`）。

## 6. 脚本行为摘要

| 动作 | 行为 |
|------|------|
| **upsert 文档** | 解析 frontmatter（title/type/tags/status）+ 正文 → `kb_document` |
| **跳过未变** | `content_hash` 相同则 skip，减少写放大 |
| **软删除** | DB 中 `source='kb'` 且 slug 已从 wiki 消失的 → `is_delete=1` |
| **标签** | frontmatter `tags` → `kb_tag` + `kb_document_tag`（按文档重建） |
| **关系** | 正文 `[[..]]` → `links_to`；frontmatter `related` → `related`；`edges.jsonl` → 原 type（`depends_on`/`relates_to`/…） |
| **断链** | 目标 slug 解析不到 → `kb_relation.resolved=0` |
| **审计** | 每批写入 `kb_sync_log` |

## 7. 与 Ingest 的配合

Agent 完成 **Ingest**（见 [[知识库三操作]]）后：

1. 更新对应 wiki 目录的 `index.md`、`log.md`、`graph/edges.jsonl`
2. `python kb/tools/lint.py --strict`（或 CI 门禁）
3. **Web 工作台**：`commit` / `publish` **默认自动 Sync**（`kb.ingest.commit-auto-sync=true`）；Expert 需跳过时可传 `sync=false`
4. **CLI / Cursor Agent 直改 markdown**：`run_sync.sh dry-run-all` → **`sync-all`**
5. 前端/API 即可检索新页

**不要**在 Navicat 里手改 `kb_document.content` 当权威源——下次 sync 会被 wiki 覆盖或产生漂移。

## 8. 常见问题

| 现象 | 处理 |
|------|------|
| `space xxx not found` | 补跑对应空间种子 SQL（见 §1 映射表） |
| 改了 ops 页但 Web 看不到 | 是否只 sync 了 `enterprise-kb`；应 `--wiki-dir wiki-moli --space moli-ops-manual` 或 `sync-all` |
| 关系为空 | 是否跑过 sync；wiki 正文是否有 `[[slug]]` |
| 部分页 skip | 正常，内容 hash 未变 |
| pymysql 缺失 | `pip install pymysql` |
| **Sync 失败 / exitCode≠0 / CI 红灯** | [`docs/ops/kb-sync-failure-runbook.md`](../../../../docs/ops/kb-sync-failure-runbook.md) |
| 中文 slug / 空间名乱码（`?`） | **客户端** `--default-character-set=utf8mb4` + 脚本 `SET NAMES utf8mb4`；**禁止** PowerShell `Get-Content \| mysql`；见 [[数据库初始化指南]] §0 |

## 9. 相关

- 本地 Viewer 的 Query/体检：`python kb/tools/serve.py` → [[查询与体检指南]]
- Java 侧 CRUD/搜索：[[知识库使用指南]]
- wiki-moli（茉莉系统手册）跳转说明：`kb/wiki-moli/guides/项目文档总览.md`（含三空间映射摘要）
