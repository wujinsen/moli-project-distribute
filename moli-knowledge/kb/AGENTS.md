# 茉莉企业知识库 · Wiki 维护契约（AGENTS.md）

> **作用域：仅 `moli-knowledge/kb/`**（Ingest / Query / Lint / sync）。  
> **全仓库规则**（含 draw.io、各微服务文档落点）：见仓库根 [`AGENTS.md`](../../AGENTS.md) + [`.cursor/rules/`](../../.cursor/rules/)。  
> 各微服务**不要**再复制本文件；模块差异写在 `moli-xxx/README.md`。
>
> 本文件是知识库的**L2 规则源**（wiki schema / 契约）。  
> 范式：以 Karpathy「LLM-Wiki」为主、AutoSci 为辅。  
> 在 **`kb/` 目录**工作时，**必须先读本文件**，再执行 Ingest / Query / Lint。

---

## 0.0 与全仓库规则的关系

| 层级 | 文件 | 何时读 |
|------|------|--------|
| L1 | [`/AGENTS.md`](../../AGENTS.md) | 任何模块文档、架构图、API 索引 |
| **L2** | **本文件** | 仅 `kb/raw`、`kb/wiki*` 维护 |
| L3 | [`.cursor/skills/drawio-diagrams/`](../../.cursor/skills/drawio-diagrams/SKILL.md) | 文档需架构/ER/流程图时 **必须**调用 |
| L3 | [`.cursor/skills/kb-ingest-sync/`](../../.cursor/skills/kb-ingest-sync/SKILL.md) | Ingest/Lint/Sync **速查**（完整规则仍在本文件） |

**draw.io**：wiki 页与 `docs/` 同样禁止用 ASCII 箭头图作主图；优先链到 `docs/diagrams/moli-*.drawio` + PNG（见 L1 §3）。

---

## 0. 核心理念（务必内化）

知识库不是「每次提问临时检索原始文档」（朴素 RAG），而是**由 LLM 增量维护的、持久互链的 markdown wiki**：

- 新增来源时，不只是存档，而是**读它 → 抽取要点 → 融进已有 wiki**：更新相关页、建立交叉引用、标记新旧冲突。
- 知识**编译一次，持续保鲜**，而不是每次查询重新拼。
- **人负责**：投喂源、提问、判断方向；**Agent 负责**：总结、交叉引用、归档、记账等所有琐活。
- 检索默认**不用向量库**：读 `index.md` 目录匹配 → 选 ≤15 页 → 整页读入 → 带 `[[页名]]` 引用回答。文档量大到 index 撑不住时，再叠向量检索（见 §7）。

---

## 1. 目录结构

```
kb/
  AGENTS.md              # 本契约
  raw/                   # 只读源头, Agent 绝不修改
    prd/                 #   产品 PRD / 产品方案外部稿
    design/              #   技术方案 / 架构设计外部稿
    api/                 #   API 说明外部稿（契约权威在 docs/api/）
    test/                #   测试 / 压测 / QA 外部稿
    ops/                 #   运维 SOP / 变更说明外部稿
    docs/                #   项目文档副本（README、ARCHITECTURE 等）
    articles/            #   技术文章(P1)
    interview/           #   面试题原始材料(P2)
    wujinsen_markdown/   #   历史语料（只读归档）
  wiki-moli/             # **茉莉系统手册**（moli-project-distribute **项目文档** · 权威）
    index.md / log.md
    guides/ product/ develop/ ops/ test/   # 一级分类 = Web dir_slug
    develop/outputs/     #   Query crystallize 汇总（项目向）
  wiki/                  # enterprise-kb · **通用技术文库**（articles / concepts / interview）
    graph/edges.jsonl
```

> **分类 = 目录（单一真相）**：Web 浏览、Ingest 落盘、Sync 回填 `category_id` 均以 **wiki 一级子目录** 为准。  
> **体裁 `type`**（frontmatter → `kb_type`）为内部字段，用于 Query/图谱/Lint/筛选；**仅**由正文 frontmatter `type:` 维护，**不对用户展示分组**。  
> `develop/outputs/` 等为 develop 下二级目录；Sync 仍按 slug **首段**回填分类。  
> **通用技术语料**（Dubbo/MySQL/Redis 等 articles、concepts、interview）在 **`wiki/`（enterprise-kb）**，**禁止**写入 wiki-moli 或在其正文加「茉莉触点」模板节。

**两空间 wiki 源**（见 `wiki-moli/ops/wiki同步指南`）：

| 空间 | wiki 源 | 定位 |
|------|---------|------|
| **moli-ops-manual**（茉莉系统手册） | **`wiki-moli/`** | **项目文档**：产品 · 服务实体 · 架构索引 · 运维 Runbook · 项目测试 · outputs 汇总 |
| **enterprise-kb** | **`wiki/`** | **通用技术文库**（ingest articles / concepts / interview）；与项目手册分空间 |

**所有权规则**：
- `raw/` —— 用户拥有，Agent 只读，绝不覆盖。
- **`wiki-moli/`** —— 茉莉**项目** wiki，Agent 自由 create/enrich；**勿**把通用八股/技术文章整库迁入。
- **`wiki/`** —— 通用技术语料（articles / concepts / interview）；正文 **不得** 含 ingest 模板「茉莉触点」节。
- `wiki-moli/log.md`、`wiki-moli/graph/edges.jsonl` —— append-only。

### 1.0.1 空间边界（铁律 · Agent 必守）

| 写入目标 | 允许内容 | **禁止** |
|----------|----------|----------|
| **`wiki-moli/`** | PRD、服务实体、Runbook、项目架构、outputs 汇总 | 通用八股/面试题整库、`raw/wujinsen_markdown` 批量骨架 |
| **`wiki/`** | Dubbo/MySQL/Redis/Vue 等**通用** articles/concepts/interview | 任何「茉莉触点」节、项目端口拓扑、链到 `[[项目文档总览]]` 等手册页 |

Ingest / Enrich / 批量脚本落盘前自检：

1. 路径是否在正确空间？
2. 通用语料正文是否含「茉莉」二字或项目 wikilink？→ **删改后再写盘**
3. 项目文档是否误落在 `wiki/articles/`？→ **移到 wiki-moli**

治理脚本：`python kb/tools/kb_space_governance.py` · 迁回语料：`revert_corpus_to_enterprise_kb.py` · **清空空壳**：`reset_enterprise_kb.py`（删除 `_gen_batches_287_1286` 批量模板页，保留 raw 提炼正文）

**禁止**再运行 `tools/_gen_batches_287_1286.py`（已废弃，会向 enterprise-kb 灌入千级空壳）。

### 1.1 五类项目文档 · 落点（与 `docs/README.md` 一致）

| 文档类型 | raw 投喂 | wiki 成品（**wiki-moli**） | 工程契约（不 ingest 全文） |
|----------|----------|---------------------------|---------------------------|
| PRD / 产品 | `raw/prd/` | **`product/`** | `docs/product/` 索引 |
| 操作 / 用户指南 | `raw/ops/`、`docs/` | **`guides/`** | — |
| 技术方案 / 概念 / 文章 | `raw/design/`、`raw/articles/` | **`develop/`**（项目页、outputs） | `docs/design/`、`docs/zh-CN/`；**通用 articles → `wiki/`** |
| 微服务实体 | **`moli-xxx/README.md`** | **`develop/{服务名}.md`** | 模块 README |
| API | `raw/api/`（可选） | **`develop/`** 摘要 + 链到契约 | **`docs/api/`** 权威 |
| 测试 / 压测 Runbook | `raw/test/` | **`test/`**（项目测试文档） | `docs/test/`、`load-test/` |
| 面试题语料 | `raw/interview/` | **`wiki/interview/`**（enterprise-kb） | — |
| 运维 / 发布 | `raw/ops/` | **`ops/`** | `docs/ops/` 索引 |
| Query 汇总 | — | **`develop/outputs/`** | — |

**铁律**：茉莉项目正文 **只维护 `wiki-moli/`**，禁止再写 `wiki/guides/`、`wiki/services/` 等 enterprise-kb 副本。API **只维护 `docs/api/`**。

迁移清单与批次：[`tools/WIKI_MOLI_MIGRATION.md`](tools/WIKI_MOLI_MIGRATION.md) · 脚本 `tools/migrate_wiki_to_moli.py`。

### 1.2 微服务文档归属（统一入口 + 按服务写源）

> 完整说明见 [`docs/README.md`](../../docs/README.md)「微服务：统一放还是各项目各自放？」。

**原则**：入口统一（`docs/` + **`wiki-moli/`**）；**禁止**每个微服务各写一套《本地启动全家桶》（统一看 `wiki-moli/guides/本地启动指南`）。分类：**guides** 操作 · **product** 产品 · **develop** 技术 · **ops** 运维 · **test** 测试。

| 文档 | 第一稿 / 源 | Agent Ingest 目标 | 说明 |
|------|-------------|-------------------|------|
| PRD、产品路线图 | `raw/prd/` | **`wiki-moli/product/`** | |
| 跨服务架构 | `raw/design/`、`docs/zh-CN/` | **`wiki-moli/develop/`**、`develop/concepts/`、`develop/articles/` | |
| **单服务设计** | **`moli-xxx/README.md`** | **`wiki-moli/develop/{服务名}.md`** | sources 指向模块 README |
| HTTP 联调契约 | — | **`develop/`** 摘要 | **勿**复制全文；链 `docs/api/` |
| 运维 SOP / 发布 | `raw/ops/` | **`wiki-moli/ops/`** | |
| 压测 / 面试 | `raw/test/` | **`wiki-moli/test/`** | |
| 用户操作 | — | **`wiki-moli/guides/`** | |

**Ingest 微服务 README 时**：

1. 读 `moli-{gateway,user-center,order,bi,knowledge}/README.md`。
2. 查 `wiki-moli/index.md`：已有 `develop/{名}` → **enrich**；无 → **create**（带 `categoryId`=develop）。
3. 操作步骤链到 `guides/` / `ops/`，不在 develop 页展开运维细节。
4. 版本再 ingest → §4.1 策略 A（同 slug enrich）。

**决策树（Agent 写/改文档前）**：

```
茉莉项目任意文档？           → wiki-moli/（按 guides/product/develop/ops/test）
HTTP 契约给前端/测试？       → 只链 docs/api/
工程索引 / 未入库设计稿？    → docs/（权威）+ 可选 enrich wiki-moli
```

**多 Git 仓库**：各仓保留 README；平台仓 Ingest 汇总到 **`wiki-moli/develop/`**。

### 1.3 分类优先 · 新建分类（Ingest / 新 raw）

1. **浏览与落盘只认分类**（`{dir_slug}/{slug}.md`），不认「按体裁分目录」为新页默认路径。
2. Ingest Plan 的 `create[]` **必须**带 `categoryId`；Express/骨架 Plan 会按 `raw/` 路径首段推断（如 `raw/prd/`→`product`）。
3. **无合适分类时**（新主题 raw）：
   - **Web**：空间 → 分类管理 → 新建（填 `categoryName` + `dir_slug`，系统自动 `mkdir` wiki 子目录）。
   - **Cursor Agent**：与用户确认 `dir_slug`（单段 `[A-Za-z0-9_-]`）→ 告知用户在 Web 建分类，或自行在 `wiki*/{dir_slug}/` 建目录并让用户补分类 → 再落盘 → `sync`。
4. `frontmatter.type` 由正文或 Ingest Plan 显式指定；Sync 后 Web 侧栏 **只按分类** 展示（`groupBy=category`）。

---

## 2. 页面格式约定

### 2.1 文件名（slug）
- 路径：`wiki/{分类dir_slug}/{slug}.md`（或 `wiki-moli/` 下同级结构）
- slug 允许中文、英文、数字，词间用连字符 `-`；**同一分类目录内** stem 唯一。
- 例：`wiki-moli/develop/用户中心.md`、`wiki/guides/本地启动指南.md`
- DB / API 全路径 slug：`develop/用户中心`（含分类前缀）

### 2.2 frontmatter（YAML 头，必填）
每个页面开头必须有：

```yaml
---
title: 用户中心
slug: 用户中心
type: service            # 内部体裁；新建页在 frontmatter 或 Ingest Plan 中指定
status: active           # draft | active | archived
tags: [微服务, 权限]
sources:                 # 该页知识来源(raw 路径或 URL), 保证可追溯
  - moli-user-center/README.md
  - docs/zh-CN/RBAC.md
related: [rbac-权限模型, 本地启动指南]   # 相关页 slug(交叉引用)
created: 2026-06-22
updated: 2026-06-22
---
```

### 2.3 正文
- 用 Markdown。关键陈述尽量带来源/交叉引用。
- **交叉引用语法**：`[[slug]]`，例如「认证由 [[rbac-权限模型]] 提供」。
- **工程文件路径**（`docs/`、模块 README、raw）：写 **仓库相对路径 + 反引号**，如 `` `docs/api/gateway-routes.md` ``。**禁止**在正文用 `` [text](../../../../docs/...) `` —— Web 端不解析文件相对链接，会显示成原始 markdown。
- `output` 类型(Query 回写)额外要求 frontmatter 含 `query`(原始问题) 与 `source_pages`(引用到的页 slug)。

---

## 3. 关系边（graph/edges.jsonl，AutoSci 辅助能力）

每行一个 JSON，记录页间类型化关系（比 `related` 更精确，可带证据）：

```json
{"from":"services/订单服务","to":"services/用户中心","type":"depends_on","evidence":"订单通过 Dubbo 调用用户中心鉴权","date":"2026-06-22"}
```

允许的 `type`：
- `depends_on` —— 依赖（服务/模块）
- `relates_to` —— 一般相关
- `derived_from` —— output 页来源于某页
- `supersedes` —— 新页取代旧结论
- `part_of` —— 从属（如某服务属于某域）

边是**附加增强**，非必须；先把页面和 `[[]]` 引用做好，边按需补。

---

## 4. 操作一：Ingest（吸收一个源）

输入：`raw/` 下一个文件或一段材料 / 一个 URL。

流程：
1. **读源**，提炼要点；判断应落入哪个 **分类**（`dir_slug`），无则按 §1.3 新建。
2. **定空间**（§1.0.1）：通用技术 → **`wiki/`**；项目文档 → **`wiki-moli/`**。**禁止**在 `wiki/` 正文加「茉莉」 branding 或项目 wikilink。
3. **查重**：读对应空间的 `index.md`，看是否已有同主题页（**同主题再 ingest 见 §4.1**）。
   - 已有 → **编辑补充**（不要新建重复页）。
   - 没有 → **新建页**（按 §2 格式）。
3. 一个源通常会触及 **5–15 个页**：新建主页 + 更新相关概念/服务页 + 补交叉引用。
4. 在涉及的页之间补 `[[slug]]` 引用；必要时向 `graph/edges.jsonl` 追加边。
5. 更新 `wiki/index.md`（新增/变更条目）。
6. 向 `wiki/log.md` 追加一行（见 §6 语法）。
7. 向用户汇报：新建/更新了哪些页、建立了哪些关系、发现的矛盾或缺口。

**P0 专项**：ingest 微服务文档时，优先产出 `guides/`（怎么操作）+ `services/`（服务实体），并在 guide 里用 `[[服务页]]` 串起来。模块 README → `services/` 的 enrich 规则见 **§1.2**。

### 4.1 版本再 Ingest（产品/技术文档 v2+ 进库）

同一主题的外部文档多次进库（如产品 PRD v1.0 → v5.0）时，**不是**在 wiki 再建一套带版本号的平行页，而是**规划式合并**进已有 slug。详见 [[增量ingest与raw投喂指南]]；Plan JSON 形态见 [[Ingest工作台产品方案]]（T15）。

**权威写入位置**：进库后的日常维护改 `wiki/`（见 §4 与 §8），**不要**指望改 `docs/` 或旧 `raw/` 自动同步到 wiki。

#### raw 侧（投喂）

- 新版本以**新文件**放入 `raw/`（如 `raw/products/xxx-v5.md`），**不覆盖**已 ingest 过的 v1 raw。
- `raw/` 只读、只追加；版本历史靠 **多条 `sources` + `log.md`** 追溯，不靠 wiki 文件名带 `-v5`。

#### 规划（写 wiki 前必做）

读新源后、改任何 wiki 文件前，Agent 必须先读 `wiki/index.md` 与疑似同主题页，产出**规划**（对话汇报或 Plan JSON），每源/每主题四类动作：

| 动作 | 含义 | 默认优先级 |
|------|------|------------|
| **enrich** | 已有 slug：增补章节、改写过时段落 | **默认**（同一产品/同一主题） |
| **create** | 确无同主题页，或 v5 全新子模块 | 仅规划确认后 |
| **skip** | 与 wiki 已 ingest 内容重复，不再写 | 常配合 enrich |
| **conflicts** | 新旧结论矛盾 | **只报告，等人确认后再改** |

**禁止**：未做规划直接批量新建 `xxx-v2`、`xxx-v5` 等同主题 slug（除非用户 **§4.1 策略 C** 明确要求并列保留）。

#### 三种合并策略（默认 A）

| 策略 | 何时 | Agent 行为 |
|------|------|------------|
| **A. 原地演进（默认）** | 同一产品/文档连续迭代 | **enrich** 原 slug；`sources` 追加新 raw 路径；更新 `updated` |
| **B. 归档旧版** | v5 整体推翻 v1 方向 | 旧页 `status: archived`；新建或 enrich 新页；`edges.jsonl` 加 `supersedes`（新 → 旧） |
| **C. 版本并列** | 用户要求同时保留 v1 与 v5 供对比 | 新建带版本后缀 slug + 枢纽页 `[[]]` 互链；**须用户显式指定** |

用户未说明时，**一律按策略 A**；若检测到可能需 B/C，在 **conflicts** 中说明并**停笔等人选**。

#### 冲突与人审

- **conflicts** 不得静默吞掉：须在汇报中列出（页 slug、矛盾摘要、建议 A/B/C）。
- **未经用户确认**，不得删除大段旧正文、不得 `archived` 旧页、不得写入与现有页矛盾的结论。
- 小改（错别字、补一段）可不经再 ingest，**直接改 wiki** → lint → sync（§8.1）。

#### 交付物（与 §4 一致）

- enrich/create 的页：更新 frontmatter `sources`（保留 v1 与 v5 路径）、`updated`。
- `wiki/index.md`：更新说明，**不**为同主题重复加 slug。
- `wiki/log.md`：append 一行，含批次号与动作摘要，例：`ingest | 批次#1300 产品v5 → enrich Ingest工作台产品方案 (+1 create, 2 skip)`。
- `wiki/graph/edges.jsonl`：按需 `relates_to` / `supersedes` / `derived_from`。
- 完成后 **lint.py --strict** → sync（§8.1）。

#### 用户指令模板（可复制）

```
请对 kb/raw/{路径} 做 ingest 批次#{N}：
- 主题：{如 Ingest 工作台产品}
- 与已有 wiki 合并，默认策略 A（enrich 优先）
- 先输出规划（create/enrich/skip/conflicts），conflicts 等我确认再写盘
- 只改 wiki/**，更新 index/log/edges
```

---

## 5. 操作二：Query（向知识库提问）

流程：
1. **先定作用域（关键）**：解析问题意图，确定要搜哪些类型 / 排除哪些类型，作为「WHERE 子句」。
   - 默认在全部类型里搜；但若问题暗示了类型，必须限定：
     - 「方案 / 怎么解决 / 最佳实践」→ 限 `type:article`（+相关 `concept`）。
     - 「面试题 / 突击 / 怎么答」→ 限 `type:interview`。
     - 「怎么操作 / 怎么用 / 怎么启动」→ 限 `type:guide`（+相关 `service`）。
   - 显式排除优先：用户说「不要面试题」→ 直接跳过整个 `wiki/interview/`。
   - 类型 + `tags` 叠加过滤：如「mysql 性能优化方案」= `type:article` ∧ tag 含 `mysql`/`性能优化`。
   - 这等价于企业 RAG 的「元数据预过滤」，只是这里靠读 frontmatter（`type`/`tags`）实现。
2. 读 `wiki/index.md`，在作用域内定位候选页；按相关度选 **≤15 页**。
3. 整页读入，必要时顺 `[[]]` 引用与 `edges.jsonl` 扩展（扩展时仍尊重作用域；除非用户想跨类型对照）。
4. **带引用作答**：每个关键结论后跟 `[[slug]]`，指向真实存在的页。
5. **不编造**：wiki 无据的部分，明说「知识库暂无」，并建议该 ingest 哪些源。
6. **回写判断（crystallize）**：若答案综合了多页、形成新洞见或填补缺口，建议写回 `wiki/outputs/{slug}.md`（或并入已有页），并更新 index/log、按需加 `derived_from` 边。简单事实查询则不回写。

> **同主题、跨类型的组织约定**：同一主题（如「MySQL 性能优化」）若既有文章又有面试题，分别建 `articles/` 与 `interview/` 页（同名 slug 不同目录），并建一个 `concepts/` 枢纽页用 `[[]]` 链接两者、用 `relates_to` 边连起来。这样按类型搜互不干扰，需要时又能从概念页互跳。

---

## 6. 操作三：Lint（健康检查）

定期扫描整个 `wiki/`，输出分级问题清单：
- **矛盾**：不同页对同一事实结论冲突。
- **过时**：被更新来源取代仍未修订的陈述（看 `supersedes` 边）。
- **孤儿页**：无任何入链的页。
- **缺概念页**：被多处提及却没有独立页的概念。
- **断链**：`[[slug]]` 指向不存在的页。
- **缺来源**：frontmatter `sources` 为空。
只报告 + 给修复建议，重写动作需用户确认。

### log.md 行语法（append-only）
```
## [YYYY-MM-DD] {操作} | {简述}
```
例：`## [2026-06-22] ingest | README.zh-CN.md → guides/本地启动指南, services/用户中心(+3 页)`
好处：`grep "^## \[" log.md | tail -5` 可看最近动作。

---

## 7. 演进路线（何时加重武器）

| 触发条件 | 升级动作 |
|----------|----------|
| 页 > 数百、index 选页不准 | 加本地混合检索（BM25+向量，参考 qmd），仍读 markdown |
| 需要多人/部门隔离 | 接现有 Shiro/Dubbo，在 Query 选页时做 ACL 过滤 |
| 需对外 Web 展示 | 把 wiki 同步进 Java `moli-knowledge-server` 的 `kb_document` 提供页面 |
| 需要图谱可视化 | 基于 `edges.jsonl` 渲染关系图 |

**原则**：先把「markdown wiki + 三操作」跑扎实，再按需加。不要过早上向量库/GraphRAG。

---

## 8. 自我进化闭环（Sync + AI 审校）

三操作解决「写什么、怎么问、健不健康」；**对外可用**还需 **Sync**（wiki → MySQL）。完整操作手册：

`wiki/guides/AI自我进化与MD审校流程.md`（[[AI自我进化与MD审校流程]]）

### 8.1 推荐顺序（勿颠倒）

```
改 wiki/*.md（Ingest / crystallize / AI 审校）
  → python kb/tools/lint.py --strict     # wiki 门禁，先于 Sync
  → 人工确认 git diff
  → （Web Ingest commit/publish 默认已 Sync；CLI/Agent 手改 wiki 时）
  → python kb/tools/sync_to_db.py        # 或 Web「Wiki 同步」
  → （可选）Web「扫描并落库」+ Query 验证
```

### 8.2 Agent 职责摘要

| 阶段 | Agent 做什么 |
|------|----------------|
| Ingest | raw → 多页 wiki + index/log/edges |
| Crystallize | Query 综合 → `wiki/outputs/` + `derived_from` |
| AI 审校 | 单篇 MD：frontmatter、[[链接]]、sources、事实 |
| Lint 失败 | 修断链/孤儿/sources 直到 `lint.py --strict` 通过 |
| Sync 后 | 建议用户 Web 验证或再 Query |

**禁止**：跳过 lint 直接 sync；修改 `raw/`；无据编造 wiki 内容。

### 8.3 Web 与 CLI 分工

| 能力 | CLI / Agent | Web（meiling-ui） |
|------|-------------|-------------------|
| Ingest / crystallize / AI 改 MD | ✅ | ❌ |
| wiki Lint | `lint.py` | ❌（无等价） |
| Sync | `sync_to_db.py` | 健康体检 → Wiki 同步 |
| DB Lint + 问题工单 | — | 扫描并落库 → `kb_lint_issue` |
| Query | serve.py | 智能问答 |

Web「扫描并落库」写的是体检**问题表**，不是 Sync，也不能代替 `lint.py`。
