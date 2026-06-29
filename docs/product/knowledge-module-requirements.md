# 知识库模块 · 需求说明（v1 工程索引）

> **工作台细项**：[knowledge-workbench-requirements.md](knowledge-workbench-requirements.md)  
> **v1 发布范围**：[moli-v1-release-scope.md](moli-v1-release-scope.md) §3.4  
> **概要设计**：[knowledge-module-overview.md](../design/knowledge-module-overview.md)

---

## 1. 产品目标

为企业提供 **Web 知识库**：浏览、问答、Ingest 入库、Wiki 治理、三空间 Sync；知识正文以 **`kb/wiki*` markdown** 为唯一源。

**v1 不做**：Meilisearch/向量库、评论回写 wiki、多 Git 仓联邦。

---

## 2. 能力清单

### 2.1 浏览与检索（P0）

| 需求 | 验收 |
|------|------|
| 空间/分类/文档树 | `GET /kb/index` meta + 懒加载 |
| 单页 Markdown | `GET /kb/page?slug=` |
| 全文搜索 | ngram `MATCH AGAINST` |
| 智能问答 | `POST /kb/ask`（LLM 或检索式） |
| 关系图谱 | `GET /kb/graph` |

### 2.2 Ingest 工作台（P0）

| 需求 | 验收 |
|------|------|
| Expert 六步 | Plan → 生成 → 审阅 → lint → commit |
| Express | 一键预览 + publish |
| 模板模式 | `useLlmGenerate=false` |
| raw 覆盖门禁 | commit 结构化 conflicts |
| nextSteps | 引导 Wiki 治理 / 健康体检 |

详见 [knowledge-ingest-acceptance.md](../test/knowledge-ingest-acceptance.md)。

### 2.3 Wiki 编辑（P0）

| 需求 | 验收 |
|------|------|
| 单页读写 | `GET/PUT /kb/wiki/page` |
| AI 改稿 | `POST /kb/wiki/ai-revise` |
| Enrich | `POST /kb/wiki/enrich`（单页） |

### 2.4 Wiki 治理（P0 后端 / P1 前端）

| 需求 | 验收 |
|------|------|
| 文件真值 Lint | `POST /kb/wiki/lint-space` |
| script / AI / auto-fix | 治理 API 全绿 |
| merge-hint | dup 合并提示 |
| **Web UI 全链路** | T16f 🔵 部分 |

### 2.5 平台 LLM（P1）

| 需求 | 验收 |
|------|------|
| DB 存 Key | `kb_platform_llm_config` |
| 管理 API | `GET/PUT/POST test` |
| 前端设置页 | T19d 🔵 |

### 2.6 三空间（P0）

| 空间 | 用途 |
|------|------|
| enterprise-kb | 技术文库 |
| moli-ops-manual | 运维手册 |
| jp-fe-ap-exam | 考试题库 |

---

## 3. 非功能

| 项 | 要求 |
|----|------|
| 同步 | 单向 kb → MySQL；`sync-all` CI |
| 权限 | 空间 ACL + Shiro |
| Lint | `lint.py --strict` 门禁 |
| 附件 | MinIO 可选 |

---

## 4. 文档与测试

| 类型 | 路径 |
|------|------|
| API | [KNOWLEDGE_API.md](../api/KNOWLEDGE_API.md) |
| 操作 | [knowledge-workbench-operations.md](../ops/knowledge-workbench-operations.md) |
| 回归 | [knowledge-e2e-regression.md](../test/knowledge-e2e-regression.md) |
| DDL | [KNOWLEDGE_SCHEMA.md](../sql/KNOWLEDGE_SCHEMA.md) |

---

## 5. /wiki 产品方案

- [Ingest工作台产品方案](../../moli-knowledge/kb/wiki/guides/Ingest工作台产品方案.md)
- [Wiki治理工作台产品方案](../../moli-knowledge/kb/wiki/guides/Wiki治理工作台产品方案.md)
- [Wiki在线编辑与AI协助改稿](../../moli-knowledge/kb/wiki/guides/Wiki在线编辑与AI协助改稿.md)
