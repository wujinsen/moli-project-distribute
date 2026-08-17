---


title: 知识库设计哲学 · Docs as Code
slug: 知识库设计哲学-docs-as-code
type: concept
status: active
tags: [知识库, 架构, docs-as-code, 设计]
sources:
  - moli-knowledge/kb/AGENTS.md
  - docs/design/knowledge-module-overview.md
  - moli-knowledge/kb/ROADMAP.md
related: [知识库三操作, 知识库服务, 知识库使用指南, wiki同步指南, 技术方案与架构索引, 查询与体检指南, 知识库-meilisearch接入规划]
created: 2026-07-01
updated: 2026-07-01
---

# 知识库设计哲学 · Docs as Code

> 一句话：**Markdown wiki 文件是真相源，MySQL `kb_document` 是派生镜像**；Web/API 只读库或写文件，**禁止绕过 wiki 直写库**。  
> 这不是茉莉独创，而是 **Docs as Code**（文档即代码）流派在企业知识库场景下的落地；茉莉额外加了 **DB 镜像 + 权限 + LLM 问答** 服务层。

## 1. 业界两大流派

| | **文件即真相（Docs as Code）** | **数据库即真相（传统 CMS/Wiki）** |
|---|---|---|
| 真相源 | Git 里的 Markdown / MDX | 数据库行（富文本 block） |
| 编辑 | 改文件 → commit → 构建/同步 | 网页编辑器直接写库 |
| 典型产品 | GitBook、Docusaurus、MkDocs、Hugo、Obsidian、Nextra、Stripe/GitHub/K8s 开发者文档 | Confluence、MediaWiki、WordPress、语雀、飞书文档 |
| 搜索 | 构建期灌入 Algolia / Meilisearch / ES | 直接查库 + 全文索引 |
| 删文档 | 删文件 + 重新构建（**通常没有「网页删库」按钮**） | 网页点删除，改 `is_delete` |

茉莉知识库属于**第一流派**，并在此基础上增加了 Java 服务层（见 §3）。

## 2. 茉莉的契约（铁律）

与 `docs/design/knowledge-module-overview.md` §1、`kb/AGENTS.md` 一致：

| 轨 | 路径 | 职责 |
|----|------|------|
| **生产轨** | `kb/wiki-moli/`、`kb/wiki/` | **唯一正文源**；Agent / Web Ingest / `PUT /kb/wiki-moli/page` 写这里 |
| **服务轨** | `moli-knowledge-server` + MySQL `kb_*` | REST 浏览、ACL、问答、图谱；**正文只读** |

**同步方向**（详见 [[wiki同步指南]]）：

- wiki → DB：`python kb/tools/sync_to_db.py`（或 Web「Wiki 同步」）
- DB ↛ wiki：库内改文档**不会**回写 markdown
- 幂等：按 `(space_id, slug)` upsert；wiki 中消失的 slug → DB `is_delete=1`

**禁止 Web 直写库**：`POST/PUT/DELETE /kb/document` 等接口已停用，调用会返回「请通过 wiki 源文件 + Sync」。这与 Docs as Code 里「禁止绕过 Git 改线上文档、靠 CI 强制」目的一致。

### 架构图（已有）

> 双轨概览：`docs/diagrams/png/moli-kb-architecture.png`（源文件 `docs/diagrams/moli-kb-architecture.drawio`）  
> 功能流程（Browse / Ask / Sync / Lint）：`docs/diagrams/png/moli-kb-functional-flows.png`  
> 分类=目录 + Sync 回填：`docs/diagrams/png/moli-kb-category-flow.png`

## 3. 与「纯静态 Docs as Code」的差异

业界常见 Docs as Code：**文件 → 静态站点（HTML）**，运行时**没有数据库**。

茉莉多了一层 **MySQL 镜像**，原因：

| 需求 | 纯静态 | 茉莉 + DB 镜像 |
|------|--------|----------------|
| 登录与空间 ACL | 难（或靠 CDN/网关） | Shiro + `kb_space_member` |
| 与 user-center / 门户 SSO | 需自建 | 已集成 [[认证与会话机制]] |
| `/kb/ask` 检索 + LLM | 需外部搜索服务 | MySQL ngram + 可选 LLM |
| Agent 直接读 MD | ✅ | ✅（`kb/` 仍在磁盘/Git） |

代价：**必须维护 wiki ↔ DB 同步**；文件与库漂移时以 **wiki 为准**，Sync 可重建 DB 视图。

## 4. 分类 = 目录（单一真相）

Web「分类管理」与 wiki **一级子目录**绑定（如 `develop/`、`guides/`）。`sync_to_db.py` 按 slug **首段**回填 `category_id`。

| 能力 | 文件层 | DB 层 |
|------|--------|-------|
| 文档归属分类 | 物理路径 `{dir_slug}/{slug}.md` | `kb_document.category_id`（镜像） |
| 多分类一篇文档 | ❌ 一篇 MD 只能在一个目录 | 可用 `kb_tag` 做**标签**（辅助维度） |
| 分类树深度 | 当前以**一级目录**为主 | `kb_category.parent_id` 支持树，但正文 slug 仍以首段为准 |

这是文件系统的自然约束，不是 bug；需要「一篇多类目」时用 **tags + 交叉引用 `[[slug]]`**，而非复制 MD。

## 4.1 体裁 (kb_type) vs 分类 —— 两个维度

| | **分类** | **体裁 (kb_type)** |
|---|----------|-------------------|
| 真相源 | wiki **一级目录** `{dir_slug}/` | frontmatter **`type:`** |
| DB 字段 | `kb_document.category_id` | `kb_document.kb_type` |
| Web 维护 | 分类管理（建目录 `dir_slug`） | 编辑页改 frontmatter `type:` → Sync |
| 典型值 (wiki-moli) | `guides` / `develop` / `ops` / `test` | `guide` / `service` / `concept` / `article` / `output` |
| 典型值 (enterprise-kb) | `concepts` / `articles` / `interview` | `concept` / `article` / `interview` |

**浏览/管理页筛选（2026-07 产品约定）**：体裁与分类为 **平行双 facet**（两行 chip，均可选「全部」），列表 `GET /kb/document/search` 对 `kbType` 与 `categoryId` 做 **AND**。详见 `docs/api/KNOWLEDGE_API.md` §2.1.3。

> **enterprise-kb 注意**：历史目录按体裁划分，分类中文名与体裁中文名常重合（如「概念」），同时选中两维时结果可能与只选一维接近——是目录布局，不是接口 bug。wiki-moli 的 `develop/` 下混有多种体裁，两维差异更明显。

## 5. 日常操作：增 / 改 / 删 / 查

| 操作 | 正确姿势 | 错误姿势 |
|------|----------|----------|
| **增** | Ingest / 新建 `{分类}/{slug}.md` → lint → sync | `POST /kb/document` |
| **改** | 改 MD 或 `PUT /kb/wiki-moli/page` → sync | `PUT /kb/document/{id}` 改正文 |
| **删** | 删（或移走）MD 文件 → sync → DB 软删 | `DELETE /kb/document/{id}`（已停用） |
| **查** | Web `/kb/page`、`/kb/ask`；本地 `serve.py` | 只查 DB 不 sync（可能过期） |

闭环三操作见 [[知识库三操作]]；人类操作入口 [[知识库使用指南]]。

### 5.1 删除文档（当前无 Web 按钮）

Docs as Code 流派里**删文档 = 删源文件 + 重新同步**，与 Confluence「点删除改库」不同。

**生产环境示例**（`moli-ops-manual` 空间）：

```bash
# 1. 删除或移入回收站（推荐先 mv，可恢复）
KB_ROOT=/opt/moli-project-distribute/moli-knowledge/kb
rm "$KB_ROOT/wiki-moli/develop/某文档.md"
# 或：mkdir -p "$KB_ROOT/.trash" && mv ... "$KB_ROOT/.trash/"

# 2. 同步（该 slug 在 DB 标记 is_delete=1）
cd "$KB_ROOT"
python tools/sync_to_db.py --wiki-dir wiki-moli --space moli-ops-manual
# 或：bash tools/ci/run_sync.sh sync-all
```

注意：

- Sync **之后** Web 文档列表才消失；只删文件不同步则 DB 仍可见。
- 其他页中的 `[[slug]]` 可能变**断链**，Sync 后可用 Web「健康体检」或 `lint.py` 发现。
- MinIO 附件对象**不会**随 MD 删除自动清理，见 [[minio-附件存储指南]]。

### 5.2 何时加「Web 删除」按钮

| 场景 | 建议 |
|------|------|
| 仅运维 / 开发 SSH 管 wiki | **不必加**；`rm + sync` 即可 |
| 业务 / 运营在 Web 管文档 | **可加** `DELETE /kb/wiki-moli/page`：删/移 MD → 触发 Sync（推荐回收站策略） |
| 只想下架、不真删 | 改 frontmatter `status: archived`，再 sync |

加 Web 删除时仍须**动文件、再 Sync**，不能恢复 `DELETE /kb/document` 直写库。

## 6. 与 Agent / LLM 的关系

`kb/AGENTS.md` 约定 Agent **直接读写 markdown**（Ingest / Query / Lint），再 `lint.py --strict` → `sync_to_db.py`。  
这与 RAG 业界趋势一致：**源语料在文件/Git，向量库/DB 是可重建索引**。茉莉当前 Query 以读 wiki + MySQL 全文为主；Meilisearch/ES 为演进选项，接入蓝图见 [[知识库-meilisearch接入规划]]。

## 7. 快速对照：「只有我们这么干吗？」

| 问题 | 答案 |
|------|------|
| wiki 文件是真相，业界有吗？ | 有，Docs as Code 是开发者文档主流 |
| 禁止 Web 直写库正常吗？ | 正常，等价于「必须走 Git/源文件」 |
| 没有网页删除按钮正常吗？ | 在该流派下正常；CMS 流派才有 |
| 茉莉特殊在哪？ | **DB 镜像 + 权限 + 问答**，为 enterprise 集成服务，而非纯静态站 |

## 相关

[[知识库三操作]] · [[知识库服务]] · [[wiki同步指南]] · [[知识库使用指南]] · [[技术方案与架构索引]] · 工程契约 `docs/design/knowledge-module-overview.md`
