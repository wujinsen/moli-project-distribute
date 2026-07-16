# 知识库 · 混合检索 + 评测升级（AI-1/2/3 技术设计）

> **状态**：design · 2026-07-17（未开工）
> **产品 PRD**：[`docs/product/ai-capability-prd.md`](../product/ai-capability-prd.md) §4 P0
> **路线总纲**：[`ai-capability-roadmap.md`](ai-capability-roadmap.md) §4 第 1 波
> **架构图**：[`docs/diagrams/moli-kb-hybrid-retrieval.drawio`](../diagrams/moli-kb-hybrid-retrieval.drawio)
> **API 契约（实现时补章）**：[`docs/api/KNOWLEDGE_API.md`](../api/KNOWLEDGE_API.md)
> **排期**：[`ai-capability-schedule.md`](ai-capability-schedule.md) W1–W4

---

## 1. 背景与目标

### 1.1 现状（2026-07-17）

| 环节 | 实现 | 位置 |
|------|------|------|
| 文档召回 | MySQL ngram 全文 `MATCH AGAINST`（`ftx_kb_document`），失败降级 LIKE | `KbDocumentMapper.searchFullText` · `KbSearchProperties.mode` |
| chunk 召回 | `kb_document_chunk` 按段 ngram 全文召回 top-N | `KbDocumentChunkMapper` · `kb.search.chunk-enabled=true` |
| 候选精排 | 内存 bigram 打分，召回上限 `kb.search.ask-candidate-limit`（默认 100） | `KbAskServiceImpl` |
| 生成 | `KbLlmClient` 带引用作答；`citationTopK` / `llmContextTopK` / `llmContextMaxChars` 分离控制 | `KbAskServiceImpl` · `KbAskProperties` |
| 评测 | `golden.jsonl`（12 题）+ `eval_ask.py`（hit@1/3/5/8、MRR、coverage、`--gate-at-k`） | `kb/tools/eval_ask.py` |

**已知短板**（`kb/eval/README.md` 首次基线记录）：未命中题的共性是"**问题换说法与页标题词面不重合**"——正是纯词面全文检索的固有缺陷。

![知识库混合检索架构](../diagrams/png/moli-kb-hybrid-retrieval.png)

> 源文件：[moli-kb-hybrid-retrieval.drawio](../diagrams/moli-kb-hybrid-retrieval.drawio)

### 1.2 目标

1. **AI-1**：把 golden 从 12 题扩到 ≥50 题，覆盖脏 query 与拒答负样本，作为可信基线。
2. **AI-2**：在 ngram 全文之外增加**向量语义召回**，两路 **RRF 融合**，可选 **Rerank** 精排；全部以 `kb.search.*` 开关控制、可回退。
3. **AI-3**：评测结果**落库 + 看板 + CI 门禁**，让检索改动可回归、防退化。

### 1.3 非目标

- 不引入 ES/Milvus 集群；向量存储用轻量方案（见 §3 选型）。
- 不改动 wiki→DB 单向同步铁律；embedding 是 DB 侧派生数据，不回写 wiki。
- 不改 `/kb/ask` 对外响应结构（citations/answer 字段不变），仅内部召回增强。

---

## 2. AI-1 评测集扩容

### 2.1 样本构成（目标 ≥50，理想 100）

| 类别 | 标签 | 数量建议 | 来源 |
|------|------|----------|------|
| 直问（词面命中） | `easy` | 15 | 现有 12 题保留 + 补 |
| 换说法/近义 | `paraphrase` | 15 | 对 easy 题改写提问 |
| 口语化/含错别字 | `dirty` | 10 | 手工造 + `kb_qa_log` 真实脏问 |
| 跨页/多跳 | `multi-hop` | 8 | 需综合 2+ 页才能答 |
| 应拒答（无据） | `negative` | 7 | 知识库确无内容，期望"暂无相关内容" |

### 2.2 golden.jsonl 字段扩展

现有：`id / space / question / expect_slugs / expect_keywords / note`。新增：

```jsonc
{
  "id": "P01",
  "space": "moli-ops-manual",
  "question": "知识库啥时候得换成 meili 那个搜索啊",   // dirty + paraphrase
  "expect_slugs": ["develop/知识库-meilisearch接入规划"],
  "expect_keywords": ["ngram"],
  "difficulty": "dirty",          // 新增：easy|paraphrase|dirty|multi-hop|negative
  "expect_answerable": true,       // 新增：false 时期望拒答，命中判定反转
  "note": "语义换说法 + 口语"
}
```

### 2.3 eval_ask.py 增强

- 读取 `difficulty` 分组，报告按难度分层输出 hit@k（现有 `STANDARD_HIT_AT=(3,5,8)` 复用）。
- `expect_answerable=false` 时，判定"是否正确拒答"（答案含"暂无/无相关"且无 citation），纳入 `refusal_accuracy`。
- 报告 JSON 增 `by_difficulty` 与 `refusal_accuracy` 字段（向后兼容）。

**验收**：golden ≥50 题、分层标注齐；跑出**扩容基线报告**存 `kb/eval/reports/`，作为 AI-2 对照组。

---

## 3. AI-2 向量 + Hybrid + Rerank

### 3.1 选型（两方案，默认 A）

| 方案 | 向量存储 | embedding/rerank | 适配 | 取舍 |
|------|----------|------------------|------|------|
| **A. Python 检索 sidecar（默认）** | Chroma（本地持久化） | bge-m3 embedding + bge-reranker-v2-m3，Python 直跑 | 新增 `kb-retrieval` FastAPI 服务，Java 经 HTTP 调 | 模型生态在 Python；不污染 Java 依赖；与未来 Agent sidecar 同栈 |
| B. Java 内嵌 | `kb_document_chunk` 加 `embedding` 列（MySQL/pgvector） | 调远程 embedding API，余弦在 SQL/内存算 | 不加新服务 | 无 rerank 生态；大向量在 MySQL 算余弦性能一般 |

> 选 A：与 AI-4 ChatBI 的 Python Agent sidecar 同一技术栈，一套 Python 服务化经验复用；rerank 模型只在 Python 侧可得。

### 3.2 数据流（离线建索引 + 在线检索）

**离线（Sync 后触发）**：
```
kb_document_chunk（新增/变更，按 content_hash 增量）
  → kb-retrieval /embed 批量向量化（bge-m3）
  → 写入 Chroma collection（payload: chunkId, docId, spaceId, slug, kbType, category）
```

**在线（/kb/ask 召回）**：
```
用户 query
  ├─ 路 1：MySQL ngram 全文（现有 searchFullText，top-N）
  └─ 路 2：kb-retrieval /search 向量召回（Chroma，top-N，带 ACL/空间 filter）
  → RRF 融合（Reciprocal Rank Fusion，score = Σ 1/(k+rank)）
  → （可选）bge-reranker 精排 top-M
  → 交给现有 buildContext / buildChunkContext 组装 → LLM
```

### 3.3 配置开关（扩展 `KbSearchProperties`，前缀 `kb.search`）

| 键 | 默认 | 说明 |
|----|------|------|
| `mode` | `fulltext` | 现有：fulltext / like |
| `ask-candidate-limit` | 100 | 现有：候选召回上限 |
| `chunk-enabled` | true | 现有：按 chunk 召回 |
| **`retrieval-strategy`** | `ngram` | **新增**：`ngram` / `hybrid` / `hybrid-rerank` 三档 |
| **`vector.base-url`** | — | 新增：kb-retrieval sidecar 地址 |
| **`vector.top-n`** | 20 | 新增：向量召回条数 |
| **`fusion.rrf-k`** | 60 | 新增：RRF 平滑常数 |
| **`rerank.top-m`** | 8 | 新增：rerank 保留条数 |

`retrieval-strategy=ngram` 时行为与现状完全一致（**零风险回退**）；sidecar 不可达时自动降级到 ngram 并告警。

### 3.4 服务边界

| 组件 | 职责 | 技术 |
|------|------|------|
| `moli-knowledge-server`（Java） | 召回编排、RRF 融合、ACL 过滤、组装上下文、LLM 调用、`kb_qa_log` | 现有 `KbAskServiceImpl` 扩展 |
| `kb-retrieval`（Python sidecar，新增） | `/embed` 批量向量化、`/search` 向量检索、`/rerank` 精排 | FastAPI + bge-m3 + bge-reranker + Chroma |

> ACL/空间过滤在 **Java 侧最终裁决**（sidecar filter 只做初筛），保持权限单一真相。

### 3.5 验收

- 三档可切换；`ngram` 档与现状逐题一致。
- 同一扩容 golden：`hybrid` 相对 `ngram` **完整集 hit@3 不回退**，`paraphrase`+`dirty` 子集 hit@3 显著提升。
- 产出 **三档对比表**（hit@1/3/5、MRR、P95 延迟）写入 README 指标区与本文件 §6。

---

## 4. AI-3 评测回归看板 + CI 门禁

### 4.1 结果落库

新增表 `kb_eval_run`（DDL 走 `docs/sql/` 迁移基线流程）：

| 字段 | 说明 |
|------|------|
| `id` | 主键 |
| `run_at` | 运行时间 |
| `strategy` | ngram / hybrid / hybrid-rerank |
| `use_llm` | 是否生成式 |
| `total` / `hit1` / `hit3` / `hit5` / `mrr` / `coverage` | 指标 |
| `by_difficulty_json` | 分层指标 JSON |
| `report_path` | `kb/eval/reports/*.json` 路径 |
| `git_sha` | 关联提交 |

`eval_ask.py` 增 `--emit-db`（或由 CI 脚本读报告 JSON 落库），不侵入现有 CLI 默认行为。

### 4.2 看板

`KbOpsService` / 运维 Dashboard 增"**检索质量趋势**"卡片：strategy 分组的 hit@3 / MRR 时间曲线，链到单次报告明细。API 归 `/kb/ops/*` 域。

### 4.3 CI 门禁

沿用现有 `--gate-at-k` + `--min-hit`：

```bash
python kb/tools/eval_ask.py --strategy hybrid --gate-at-k 3 --min-hit <基线-容差>
```

PR 触发检索相关路径变更时运行；低于阈值非 0 退出拦截合并。基线值随 AI-2 落地后更新并记录在 `kb/eval/README.md`。

### 4.4 验收

- 每次检索改动看板可见曲线；人为制造回退能被 CI 拦截。

---

## 5. 兼容性与回滚

| 关注点 | 处理 |
|--------|------|
| 对外 API | `/kb/ask` 响应结构不变；仅内部召回增强 |
| 回退 | `kb.search.retrieval-strategy=ngram` 一键回到现状 |
| sidecar 故障 | Java 侧超时/连接失败自动降级 ngram + 告警，不阻断问答 |
| 数据一致 | embedding 由 chunk `content_hash` 增量驱动；wiki 仍是唯一写入源 |
| 权限 | ACL 最终在 Java 裁决，向量 filter 仅初筛 |

---

## 6. 指标对比（落地后回填）

| 策略 | hit@1 | hit@3 | hit@5 | MRR | P95 延迟 | 样本 |
|------|-------|-------|-------|-----|----------|------|
| ngram（基线） | — | — | — | — | — | — |
| hybrid | — | — | — | — | — | — |
| hybrid+rerank | — | — | — | — | — | — |

> paraphrase / dirty / multi-hop 子集单列，突出语义检索增益。

---

## 7. 相关

- 路线总纲：[`ai-capability-roadmap.md`](ai-capability-roadmap.md)
- PRD：[`../product/ai-capability-prd.md`](../product/ai-capability-prd.md)
- 排期：[`ai-capability-schedule.md`](ai-capability-schedule.md)
- 现状检索：`kb/wiki-moli/develop/知识库服务.md` · `kb/ROADMAP.md` §五
- Meilisearch 备选蓝图：`kb/wiki-moli/develop/知识库-meilisearch接入规划.md`
- 评测说明：`kb/eval/README.md`
