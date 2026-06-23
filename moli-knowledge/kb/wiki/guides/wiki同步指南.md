---
title: wiki 同步指南
slug: wiki同步指南
type: guide
status: active
tags: [知识库, 运维, P0, 同步]
sources:
  - moli-knowledge/kb/tools/sync_to_db.py
  - moli-knowledge/kb/AGENTS.md
related: [知识库使用指南, 知识库服务, 数据库初始化指南, 知识库三操作]
created: 2026-06-22
updated: 2026-06-23
---

# wiki 同步指南

> 权威内容在 `moli-knowledge/kb/wiki/`（Agent 维护）；Java 后端读 `kb_document`。服务实体 [[知识库服务]]；API 浏览见 [[知识库使用指南]]。

面向「wiki 改完后怎么进 MySQL、让 `/kb/page` 有正链/反链」的操作说明。

## 1. 同步方向与原则

| 方向 | 说明 |
|------|------|
| **wiki → DB** | `sync_to_db.py` 单向写入 |
| **DB ↛ wiki** | 库内改文档**不会**回写 markdown |
| **幂等** | 按 `(space_id, slug)` upsert；`content_hash` 未变则 skip |

`index.md`、`log.md` **不同步**（元数据页）。

## 2. slug 规则

slug = wiki 相对路径去扩展名，与 `[[双链]]`、`graph/edges.jsonl` 节点名一致：

| 文件 | slug |
|------|------|
| `wiki/guides/本地启动指南.md` | `guides/本地启动指南` |
| `wiki/services/用户中心.md` | `services/用户中心` |

API 请求示例：`GET /kb/page?slug=guides/本地启动指南`。

## 3. 前置条件

1. MySQL 已导入全库 + 知识库表（见 [[数据库初始化指南]]）
2. `docs/sql/03_knowledge_schema.sql` 已执行，存在 `kb_space` 种子（默认 `enterprise-kb`，id=`900000000000000001`）
3. Python 3 + `pip install pymysql`（仅真正写库时需要）

## 4. 推荐流程

### 4.1 先 dry-run（不连库）

```bash
cd D:\work\moli_project\moli-project-distribute
python moli-knowledge/kb/tools/sync_to_db.py --dry-run
```

输出：待 insert/update/skip/delete 的 slug 列表、关系边统计。**先核对再写库。**

### 4.2 真正同步

```bash
python moli-knowledge/kb/tools/sync_to_db.py \
  --host 127.0.0.1 --port 3306 --user root --password 12345678 \
  --db moli --space enterprise-kb
```

参数默认值对齐 `moli-knowledge-server` 的 `application-dev.yml`。

### 4.3 验证

目录 **meta**（应只有 `count`，`items` 为空）：

```bash
curl "http://127.0.0.1:21000/KnowledgeServer/kb/index?spaceId=900000000000000001" \
  -H "Authorization: login_token_xxx"
```

展开某分组（例：操作指导）：

```bash
curl "http://127.0.0.1:21000/KnowledgeServer/kb/index/items?spaceId=900000000000000001&type=guide&pageNum=1&pageSize=50" \
  -H "Authorization: login_token_xxx"
```

单页应含 `outLinks` / `backLinks`（来自 `kb_relation`）：

```bash
curl "http://127.0.0.1:21000/KnowledgeServer/kb/page?slug=guides/本地启动指南&spaceId=900000000000000001" \
  -H "Authorization: login_token_xxx"
```

> 完整 browse API 见 `docs/KNOWLEDGE_API.md` §2（含 `/kb/index/search`、`/kb/index/locate`）。

## 5. 脚本行为摘要

| 动作 | 行为 |
|------|------|
| **upsert 文档** | 解析 frontmatter（title/type/tags/status）+ 正文 → `kb_document` |
| **跳过未变** | `content_hash` 相同则 skip，减少写放大 |
| **软删除** | DB 中 `source='kb'` 且 slug 已从 wiki 消失的 → `is_delete=1` |
| **标签** | frontmatter `tags` → `kb_tag` + `kb_document_tag`（按文档重建） |
| **关系** | 正文 `[[..]]` → `links_to`；frontmatter `related` → `related`；`edges.jsonl` → 原 type（`depends_on`/`relates_to`/…） |
| **断链** | 目标 slug 解析不到 → `kb_relation.resolved=0` |
| **审计** | 每批写入 `kb_sync_log` |

## 6. 与 Ingest 的配合

Agent 完成 **Ingest**（见 [[知识库三操作]]）后：

1. 更新 `wiki/index.md`、`log.md`、`graph/edges.jsonl`
2. 运行本指南的 dry-run → sync
3. 前端/API 即可检索新页

**不要**在 Navicat 里手改 `kb_document.content` 当权威源——下次 sync 会被 wiki 覆盖或产生漂移。

## 7. 常见问题

| 现象 | 处理 |
|------|------|
| `space enterprise-kb not found` | 补跑 `03_knowledge_schema.sql` |
| 关系为空 | 是否跑过 sync；wiki 正文是否有 `[[slug]]` |
| 部分页 skip | 正常，内容 hash 未变 |
| pymysql 缺失 | `pip install pymysql` |
| 中文 slug 乱码 | 终端 UTF-8；MySQL `utf8mb4` |

## 8. 相关

- 本地 Viewer 的 Query/体检：`python kb/tools/serve.py` → [[查询与体检指南]]
- Java 侧 CRUD/搜索：[[知识库使用指南]]
