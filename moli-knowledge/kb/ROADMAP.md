# 企业级知识库 · 功能规划（LLM-Wiki 范式）

> 更新：2026-06-28
> 范式：Karpathy「LLM-Wiki」为主。知识 = 由 Agent 维护的、持久互链的 markdown wiki。
> 规则契约见 [AGENTS.md](AGENTS.md)。本文件是功能规划的**唯一入口**。
>
> 状态标记：✅ 已做 ｜ 🔜 近期规划 ｜ 💤 后期/按需
>
> 目标形态（已确认）：**做成给团队/对外的多人 Web 知识库产品**。

---

## 〇、两项目分工（kb ↔ moli-knowledge-server）

本知识库由两个项目协作，**职责严格分离、单一写入源**：

| | `kb/`（LLM-Wiki） | `moli-knowledge-server/`（Java 微服务） |
|---|---|---|
| 角色 | 知识的**生产与治理层**（大脑） | **服务与展示层**（门面） |
| 形态 | markdown + git，Agent 维护 | Spring Cloud + MySQL + Web/API |
| 谁写知识 | Agent（ingest/lint/synthesis） | **不写知识**，只读展示（评论/反馈不回写正文） |
| 谁使用 | 我 + Agent（Cursor/Obsidian） | 多人、对外、其它微服务 |
| 权限 | 文件级 | Shiro + Dubbo 细粒度 ACL |

**铁律**：知识只在 `kb/`（markdown）里产生和维护，Java 服务是**下游只读**。绝不双写入源。

### 数据流

```
kb/wiki/*.md ──[同步脚本: 解析 frontmatter+正文]──▶ kb_document(MySQL)
                                                       │
                                       moli-knowledge-server REST/Dubbo
                                                       │
                                  Web UI / 其它微服务 / 多人访问 + ACL
```

### kb → kb_document 同步方案（单向、增量、幂等）

| 项 | 设计 |
|----|------|
| 方向 | 严格单向 kb → DB；DB 侧不编辑知识正文 |
| 触发 | ✅ 手动 `sync_to_db.py`；git hook；定时任务；**GitHub Actions**（PR dry-run / main 写库） |
| 主键 | ✅ `kb_document.slug`（= wiki 相对路径去扩展名）+ `(space_id,slug)` 唯一键，幂等 upsert |
| 字段映射 | ✅ `title→title`、`type→kb_type`、`tags→domain` 推断、`tags→kb_tag(+关联)`、正文`→content`、`doc_type=markdown`、`status:active→1已发布` |
| 关系 | ✅ `[[..]]→links_to`、`related→related`、`edges.jsonl→边 type` 写入 `kb_relation`（断链记 `resolved=0`） |
| 增量 | ✅ 按 `content_hash`(SHA-256) 比对，未变 skip，只同步变更页 |
| 删除 | ✅ kb 删页 → DB 置 `is_delete=1`；每条动作记 `kb_sync_log` |

### 两项目集成里程碑

| 里程碑 | 项目 | 内容 |
|--------|------|------|
| **M1 知识跑厚** | kb | ingest P0/P1/P2、跑通 Query、Lint；先不碰 Java |
| **M2 同步打通** | kb + server | ✅ 同步脚本 [`kb/tools/sync_to_db.py`](tools/sync_to_db.py)（dry-run 已通）；✅ `kb_document` 加 `slug` 唯一键 + 同步三件套；✅ Java 只读查询/详情 API |
| **M3 Web 门面** | server | ✅ 前端展示（目录树/页面/关系图/搜索）+ 接 Shiro ACL（见 TASKS T5/T6/T11） |
| **M4 检索后端** | server | ✅ **第一阶段：MySQL ngram 全文索引**（browse `search` + Query `ask` 均走 `MATCH AGAINST`，ask 改为全文召回 top-N + 内存精排，去掉全量扫描）；🔜 信号触发再上 Meilisearch/ES（见 §五③） |
| **M5 Web Wiki 编辑** | kb + server + meiling-ui | ✅ **单篇**编辑：Markdown + AI 改稿 + enrich + 保存 → Sync（[[Wiki在线编辑与AI协助改稿]]、T14） |
| **M6 Ingest 工作台** | kb + server + meiling-ui | ✅ **批次 Ingest**：raw→Plan→多页→lint→commit→Sync（[[Ingest工作台产品方案]]、T15）；前端 nextSteps/conflicts UI 🔵 |
| **M7 Wiki 治理** | kb + server + meiling-ui | ✅ 后端 lint-space + script/ai/auto-fix + merge-hint（T16a/e/g）；🔵 前端 T16f 全链路 UI |

> **M5 / M6 / M7 与铁律**：仍保持 **wiki 为唯一正文源**；Web 不写 `kb_document` 正文，而是写服务器 wiki 文件后再 Sync。

---

## 一、核心操作（Agent 四件套）

| 能力 | 说明 | 状态 |
|------|------|------|
| **Ingest 吸收** | 读源 → 抽要点 → 写/更新页 → 建交叉引用，边写边去重 | ✅ 骨架+示范（5 页） |
| **Query 问答** | 读 index → 按 type/tags 限定作用域 → 选 ≤15 页 → 带 `[[]]` 引用作答 → 好答案回写 | ✅ serve.py + Java `/kb/ask`（作用域过滤+生成式/检索式） |
| **Lint 体检** | 扫全库找：重复 / 矛盾 / 过时 / 孤儿页 / 断链 / 缺来源 | ✅ **自动化** [`kb/tools/lint.py`](tools/lint.py)（分级报告+CI 门禁，详见 [查询与体检指南](wiki/guides/查询与体检指南.md)） |
| **Synthesis 综合** | 去重提炼、重编排成"系列"（如面试题系列）、生成综述页 | ✅ serve.py 提炼页（枢纽页/跨类型对照，LLM）；🔜 系列化重编排 |

---

## 二、知识治理（范式核心价值）

| 功能 | 实现方式 | 状态 |
|------|----------|------|
| 去重 | 写入时查 `index.md` 合并 + Lint 扫描近似重复 | ✅ `lint.py --dups`（content_hash 全等 + MinHash 近似 Jaccard） |
| 提炼/精炼 | Ingest 抽要点、归一化；发现更优解则更新覆盖 | 🔜 |
| 交叉引用 | `[[slug]]` 链接 + `graph/edges.jsonl` 类型化关系边 | ✅ 已用；`lint.py` 查断链/孤儿/缺概念/大小写写错 |
| 版本与时间线 | `log.md`（append-only）+ git 历史 | ✅；`lint.py --log` 追加体检留痕 |
| 冲突/过时标记 | `supersedes` 边 + Lint 提示 | ✅ `lint.py` outdated 检查（被 supersedes 取代仍 active 即告警） |
| 反哺闭环（P1） | 发现更优解 → 更新文章页 → 同步索引 | 🔜 |

---

## 三、分类与过滤（无需数据库）

| 功能 | 实现方式 | 状态 |
|------|----------|------|
| 类型隔离 | 目录 `guides/services/concepts/articles/interview/outputs` | ✅ |
| 元数据 | frontmatter `type` / `tags` / `domain`(FE/AP/…) / `sources` | ✅ |
| 查询作用域过滤 | 按 `type`/`tags` 预过滤（等价 RAG 元数据预过滤） | ✅ 契约已定 |
| 同主题跨类型组织 | 同名 slug 分目录 + `concepts/` 枢纽页串联 | ✅ 契约已定 |

---

## 四、场景落地（按优先级）

| 优先级 | 场景 | 内容 | 状态 |
|--------|------|------|------|
| **P0** | 微服务用户指导手册 | 聚合各服务文档 → guides + services + concepts | ✅ 已示范，🔜 批量铺开 |
| **P1** | 技术文章沉淀 | 文章入库、提炼、发现更优解反哺 | 🔜 |
| **P2** | 面试题系列 | 杂乱源 → 去重提炼 → 按主题/域(FE/AP)编成系列 | 🔜 |

---

## 五、检索演进（信号驱动，不过早优化）

| 阶段 | 触发信号 | 用什么 | 状态 |
|------|----------|--------|------|
| ① 纯 index | < ~300 页 | `index.md` 直读 + 全文读 | ✅ Agent 层（kb/ + serve.py）现仍用 |
| ② 本地混合检索 | ~300–1000 页 / 召回变差 | qmd（本地 BM25+小向量+rerank，无服务） | 💤 暂不需要（Agent 层 index 仍够用） |
| ③ 检索后端 | >1000–2000 页 / 多人产品 | **MySQL ngram 全文（已上）** → **chunk 切段（规范已定）** → Meilisearch/Typesense → ES（海量）→ 向量库（语义） | ✅ ngram 已落地；🔜 chunk 实现（[[知识库-chunk切段规范]]） |

> 详见 [AGENTS.md §7](AGENTS.md)。原则：先把 markdown wiki 跑厚，搜不准再上 qmd，产品化才谈服务端。
>
> **M4 现状（2026-06-23）**：产品层（Java `moli-knowledge-server`）已用 MySQL 内建 ngram 全文索引
> `ftx_kb_document(title,summary,content)`——属于 ③ 的「无独立搜索服务」轻量形态，1398 页绰绰有余。
> 关键改动：把 Query(`/kb/ask`) 的候选选取从「全量 `selectList` + 内存打分」改为
> 「ngram 全文按相关度召回 ≤`kb.search.ask-candidate-limit`(默认100) 页 → 内存 bigram 精排」，
> 并保留全文未启用/0 命中时的全量扫描兜底。只有当召回明显变差或文档量再上一个量级、
> 或需要语义检索时，才升级到 Meilisearch/Typesense（独立服务）或向量库。
>
> **2026-07-16**：向量/Hybrid/Rerank 已从「按需再说」升级为**排期任务**（AI-2），连同评测扩容（AI-1）、
> GraphRAG（AI-5）、Agentic RAG（AI-7）等统一规划——见
> [`docs/design/ai-capability-roadmap.md`](../../docs/design/ai-capability-roadmap.md)。

---

## 六、可视化与产品化（已确认要做，见 §〇）

| 功能 | 说明 | 状态 |
|------|------|------|
| 图谱浏览 | Obsidian 打开 `kb/` 看关系图；或基于 `edges.jsonl` 自渲染 | 🔜 |
| 多人 Web | kb → `kb_document` 单向同步，Java `moli-knowledge-server` 对外服务（M2/M3） | ✅ 浏览/问答/图谱/体检 |
| **Wiki 在线编辑（单篇）** | 浏览/体检 → 编辑页 → AI 改稿 → diff → 保存 wiki → Sync | ✅ M5 / T14 |
| **Ingest 工作台（批次）** | raw 选源 → Plan → 多页草稿 → diff → lint → commit → Sync | ✅ M6 / T15（前端部分 🔵） |
| **Wiki 治理（空间级）** | lint-space → script/ai/auto-fix → merge-hint → Sync | ✅ 后端 T16；🔵 T16f UI |
| 权限隔离 | 复用现有 Shiro + Dubbo，在服务层/检索选页时做 ACL 过滤 | ✅ 空间 viewer/editor |
| 评测 | 标准问答集 + 答对率/命中率/引用可追溯，回归看改动好坏 | 🔜 骨架已建：`kb/eval/golden.jsonl`（12 题）+ `kb/tools/eval_ask.py`（hit@k/MRR/coverage，`--min-hit` 可作 CI 门禁）；待跑通基线并扩题 |

---

## 七、当前进度小结

- ✅ 已定范式、写好 `AGENTS.md` 契约、搭好 wiki 骨架。
- ✅ 已示范 ingest 顶层 README → 5 页（guides/services/concepts），含关系边。
- ✅ **Phase 0 治理（2026-06-25）**：运维页归位 `wiki-moli/`；清批次占位页；wiki **375 页**；CI **`lint-strict` 门禁** + `sync-all` 两空间。
- ✅ **Agent 知识治理自动化**：[`kb/tools/lint.py`](tools/lint.py) 分级体检；PR 阻断 ERROR/WARN。
- ✅ **M5 T14**：单篇 Wiki 在线编辑 + AI 改稿 + enrich（[[Wiki在线编辑与AI协助改稿]]）。
- ✅ **M6 T15**：批次 Ingest 工作台（[[Ingest工作台产品方案]]）；前端 nextSteps/conflicts 🔵。
- ✅ **M7 T16 后端**：Wiki 治理 lint-space + 批量修复 + merge-hint；**T16f 前端** 🔵。
- 💤 `serve.py` 提炼 Tab：保留为**本地辅助**，不进 Web 产品主线。
