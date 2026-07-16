# 知识库模块 · 概要设计

> 模块：`moli-knowledge` · 服务 `knowledge-server` :8090  
> 更新：2026-07-13  
> v1 范围：[moli-v1-release-scope.md](../product/moli-v1-release-scope.md) §3.4 · §9
> 表结构：[KNOWLEDGE_SCHEMA.md](../sql/KNOWLEDGE_SCHEMA.md) · API：[KNOWLEDGE_API.md](../api/KNOWLEDGE_API.md)

---

## 1. 定位

企业知识库采用 **LLM-Wiki 双轨**：

| 轨 | 路径 | 职责 |
|----|------|------|
| **生产轨** | `kb/wiki*` markdown | 唯一正文源；Agent/Web Ingest 写入 |
| **服务轨** | `moli-knowledge-server` | REST + MySQL 只读门面；Shiro ACL |

**铁律**：知识正文只在 `kb/` 产生；Java **不**双写 `kb_document` 正文（Web 编辑写 wiki 文件后再 Sync）。

---

## 2. 架构

![知识库双轨](../diagrams/png/moli-kb-architecture.png)

> 源文件：[moli-kb-architecture.drawio](../diagrams/moli-kb-architecture.drawio)

![RAW 全链路](../diagrams/png/moli-kb-raw-pipeline.png)

> 源文件：[moli-kb-raw-pipeline.drawio](../diagrams/moli-kb-raw-pipeline.drawio)

```
raw/ + Cursor Agent          Web Ingest / Wiki 编辑
        │                              │
        ▼                              ▼
   kb/wiki*  (markdown)  ◄──  lint.py 门禁
        │
        │  sync_to_db.py / run_sync.sh
        ▼
   MySQL kb_document …
        │
        ▼
   knowledge-server REST  ──►  meiling-ui
        │
        └── /kb/ask (LLM 可选)
```

---

## 3. 三空间

| wiki 目录 | space_code | 用途 |
|-----------|------------|------|
| `kb/wiki/` | `enterprise-kb` | 通用技术文库 |
| `kb/wiki-moli/` | `moli-ops-manual` | 茉莉系统手册 |
| `kb/wiki-jp-exam/` | `jp-fe-ap-exam` | 日本语 FE/AP 考试 |

路径解析：`KbRepoPathUtil`（8090 Java + Python 脚本统一根目录）。

---

## 4. 核心能力（v1）

### 4.1 浏览与检索

- `GET /kb/index` meta 目录 + 分组懒加载
- `GET /kb/page?slug=` 单页 + 出/入链
- MySQL **ngram 全文**搜索 + `/kb/ask` 问答

### 4.2 Ingest 工作台（T15+）

raw → Plan → 草稿 → lint → commit → Sync；Express / 模板模式。  
详见 [knowledge-workbench-requirements.md](../product/knowledge-workbench-requirements.md)。

### 4.3 Wiki 治理（T16）

磁盘 `lint-space` → script-fix / ai-batch-fix / auto-fix / merge-hint。  
**不**做空间级批量 enrich。

### 4.4 单页编辑（T14）

`GET/PUT /kb/wiki-moli/page`、ai-revise、enrich（单页）。

### 4.5 平台 LLM（T19 · ✅）

`kb_platform_llm_config` + 系统管理 UI（T19d）+ 可选 `kb_llm_call_log` 审计。

### 4.6 内容管道运维（KBOPS · 2026-07）

| 能力 | 说明 |
|------|------|
| Sync 可观测 + 并发锁 | `kb_sync_log` · Redis 锁 · O1–O4 UI |
| 体检工单 | `kb_lint_issue` · O5–O8 |
| 运维 Dashboard | `GET /kb/ops/dashboard` · `kb_llm_call_log` |
| CI 门禁 | `lint-strict-all` + dry-run |

详见 [kb-ops-roadmap.md](kb-ops-roadmap.md) · [knowledge-ops-prd.md](../product/knowledge-ops-prd.md)。

### 4.7 Ask chunk 召回（2026-07）

- markdown 正文按 chunk 切段入库/检索  
- `eval_ask.py` 黄金集回归  
- 配置：`kb.ask.recall-mode=chunk`

---

## 5. 同步机制

![Sync 双轨](../diagrams/png/moli-knowledge-sync.png)

> [moli-knowledge-sync.drawio](../diagrams/moli-knowledge-sync.drawio)

| 项 | 说明 |
|----|------|
| 触发 | CLI、`POST /kb/sync/trigger`、CI `sync-all` |
| 幂等 | `(space_id, slug)` + `content_hash` |
| 删除 | wiki 删页 → DB `is_delete=1` |
| 审计 | `kb_sync_log` |

命令见 [wiki同步指南](../../moli-knowledge/kb/wiki-moli/ops/wiki同步指南.md)。

---

## 6. 模块目录

```
moli-knowledge/
  kb/                    # LLM-Wiki（AGENTS.md 契约）
  moli-knowledge-server/ # Java :8090
  TASKS.md               # 任务清单
```

---

## 7. 依赖

| 依赖 | 用途 |
|------|------|
| MySQL | kb_* 表 + kb_document |
| Redis db=2 | Shiro Session（dev 配置） |
| Nacos | 注册 |
| user-center | Dubbo 鉴权 |
| MinIO | 附件（可选） |
| Python 3 | lint.py、sync_to_db.py |

---

## 8. 详细设计索引

| 主题 | 文档 |
|------|------|
| LLM 平台设置 | [kb-llm-platform-settings.md](kb-llm-platform-settings.md) |
| KBOPS 路线图 | [kb-ops-roadmap.md](kb-ops-roadmap.md) |
| T20 双入口导入 | [kb-import-entry-design.md](kb-import-entry-design.md) |
| 契约 | [kb/AGENTS.md](../../moli-knowledge/kb/AGENTS.md) |
| 操作 | [knowledge-workbench-operations.md](../ops/knowledge-workbench-operations.md) |
| 测试 | [knowledge-e2e-regression.md](../test/knowledge-e2e-regression.md) |

---

## 9. 后续（v2+ 信号）

- Meilisearch / 向量检索（文档量触发）
- 评论/反馈回写 wiki（当前不回写正文）
- 多 Git 仓 wiki 聚合
