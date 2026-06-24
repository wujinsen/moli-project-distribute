# 茉莉企业知识库 · Wiki 维护契约（AGENTS.md）

> 本文件是这套知识库的**唯一规则源**（schema / 契约）。
> 范式：以 Karpathy「LLM-Wiki」为主、AutoSci 为辅。
> 任何 AI Agent 在本目录工作时，**必须先读本文件**，再执行 Ingest / Query / Lint。

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
    docs/                #   项目/微服务文档(可放副本或软链)
    articles/            #   技术文章(P1)
    interview/           #   面试题原始材料(P2)
  wiki/                  # Agent 全权拥有的知识页
    index.md             #   内容目录(catalog), 每次 ingest/crystallize 后更新
    log.md               #   append-only 时间线
    guides/              #   P0 操作指导页(面向"怎么用")
    services/            #   微服务实体页(每个服务一页)
    concepts/            #   跨文档概念页(RBAC/秒杀/MySQL优化...)
    articles/            #   P1 技术文章沉淀页
    interview/           #   P2 面试题页(精炼后)
    outputs/             #   Query 回写的综合页
    graph/
      edges.jsonl        #   类型化关系边(append-only)
```

**所有权规则**：
- `raw/` —— 用户拥有，Agent 只读，绝不覆盖。
- `wiki/` —— Agent 拥有，自由创建/编辑。
- `wiki/log.md` —— append-only，永不原地重写。
- `wiki/graph/edges.jsonl` —— append-only。

---

## 2. 页面格式约定

### 2.1 文件名（slug）
- 路径：`wiki/{类型}/{slug}.md`
- slug 允许中文、英文、数字，词间用连字符 `-`；同一类型内唯一。
- 例：`wiki/services/用户中心.md`、`wiki/concepts/rbac-权限模型.md`

### 2.2 frontmatter（YAML 头，必填）
每个页面开头必须有：

```yaml
---
title: 用户中心
slug: 用户中心
type: service            # guide | service | concept | article | interview | output
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
1. **读源**，提炼要点；判断属于哪个/哪些实体类型。
2. **查重**：读 `wiki/index.md`，看是否已有同主题页。
   - 已有 → **编辑补充**（不要新建重复页）。
   - 没有 → **新建页**（按 §2 格式）。
3. 一个源通常会触及 **5–15 个页**：新建主页 + 更新相关概念/服务页 + 补交叉引用。
4. 在涉及的页之间补 `[[slug]]` 引用；必要时向 `graph/edges.jsonl` 追加边。
5. 更新 `wiki/index.md`（新增/变更条目）。
6. 向 `wiki/log.md` 追加一行（见 §6 语法）。
7. 向用户汇报：新建/更新了哪些页、建立了哪些关系、发现的矛盾或缺口。

**P0 专项**：ingest 微服务文档时，优先产出 `guides/`（怎么操作）+ `services/`（服务实体），并在 guide 里用 `[[服务页]]` 串起来。

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
