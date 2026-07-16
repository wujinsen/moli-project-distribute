# AI-1 golden 评测集扩容 · 施工契约（Opus 产出，Composer 施工）

> **角色**：本文件由 Opus（架构与安全负责人）产出，作为 Composer 施工的**唯一契约**。
> **任务**：AI-1 golden 评测集扩容（12 → ≥50，理想 100），第 1 波 W1。
> **状态**：**done** · Opus 复核通过 2026-07-17（首轮 review→修订→复核全过程见 §9；验收结论 §9.7）
> **上游**：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4 第 1 波 AI-1 · [`../kb-hybrid-retrieval.md`](../kb-hybrid-retrieval.md) §2 · `moli-knowledge/kb/eval/README.md`
> **目标产物**：`moli-knowledge/kb/eval/golden.jsonl`（扩容）+ `kb/tools/eval_ask.py`（分层统计增强）+ `kb/eval/reports/` 扩容基线报告。

---

## 0. 契约边界（读我）

**本契约只定义**：golden.jsonl 字段与取值语义、分层配额、拒答判定标准、`eval_ask.py` 需要新增的统计输出形状、验收基线报告格式、Composer 文件级施工清单与禁改范围。

**不在本契约内（Composer 负责按现有风格落地）**：具体 50~100 道题的**题面撰写**（需读 wiki/kb_qa_log 出题，属内容工作，见 §6 交付流程）、`eval_ask.py` 的 Python 实现代码、报告归档脚本。

**红线（Composer 不得擅改）**：
- 不改 `/kb/ask` 对外请求/响应结构（评测是只读消费方）。
- 不改现有 `hit@k` / `mrr` / `coverage` 的既有计算语义（只做「分母收敛到可答子集」+ 新增分层维度，见 §4）。
- 不删现有 12 题（只增改标签，遵守 eval README「只增不删」）。
- 拒答判定标准（§3）是评测正确性核心，如需调整回 Opus 窗口改契约。

---

## 1. golden.jsonl 字段扩展

### 1.1 字段契约（在现有基础上新增 3 个字段）

现有字段（保留，语义不变）：`id` / `space` / `question` / `expect_slugs` / `expect_keywords` / `note`。

| 字段 | 必填 | 类型 | 契约语义 |
|------|------|------|----------|
| `id` | 是 | string | 唯一；前缀=空间：`M*`=moli-ops-manual、`E*`=enterprise-kb、`J*`=jp-fe-ap-exam（沿用 README） |
| `space` | 是 | string | `kb_space.space_code`，运行时解析 spaceId |
| `question` | 是 | string | 用户视角提问，按 `difficulty` 决定口语化/换说法程度 |
| `expect_slugs` | **条件** | string[] | **`expect_answerable=true` 时必填非空**；`false`（negative）时**必须为 `[]`** |
| `expect_keywords` | 否 | string[] | 生成式答案应含关键词（小写比较），仅 `--use-llm` 检查；negative 题**禁填** |
| `note` | 否 | string | 出题理由 / 考察点 |
| **`difficulty`** | **是（新增）** | enum | `easy` \| `paraphrase` \| `dirty` \| `multi-hop` \| `negative` |
| **`expect_answerable`** | **是（新增）** | boolean | `true`=库内有据应命中；`false`=库内无据应拒答（命中判定反转，见 §3） |
| **`expect_all`** | 否（新增，可选） | boolean | 仅 `multi-hop` 用；`true` 表示 `expect_slugs` **全部**被引用才算完全命中（默认 `false`=任一命中，保持向后兼容） |

### 1.2 取值约束（Composer 在 `load_golden` 增校验）

1. `difficulty ∈ {easy,paraphrase,dirty,multi-hop,negative}`，否则报错行号。
2. `difficulty=negative` ⇒ `expect_answerable=false` 且 `expect_slugs==[]` 且无 `expect_keywords`；违反报错。
3. `difficulty!=negative` ⇒ `expect_answerable=true` 且 `expect_slugs` 非空。
4. `expect_all=true` 仅当 `difficulty=multi-hop`。
5. `id` 前缀与 `space` 一致（M/E/J 对齐 space_code）。
6. 现有 `load_golden` 对 `expect_slugs` 的「非空」硬校验需**放宽**为「按 `expect_answerable` 条件校验」（negative 允许空）。

### 1.3 现有 12 题迁移标注（不改题面，只补标签）

Composer 为现存 M01–M10、E01–E02 逐题补 `difficulty` + `expect_answerable=true`。建议标注（Opus 定，可微调）：

| id | 建议 difficulty | 理由 |
|----|-----------------|------|
| M01 M02 M04 M05 M06 M08 E01 E02 | `easy` | 词面较贴近页标题/正文 |
| M03 M07 M10 | `paraphrase` | 「怎么设计/换说法/升级=接入」等近义提问 |
| M09 | `multi-hop`（`expect_all=false`） | 跨 guides/ops 两页 |

> M09 也可保守标 `easy`；以「是否需综合 2+ 页」为准，Composer 落定后在报告里自查分布。

### 1.4 字段示例（契约形状，非最终题）

```jsonc
// paraphrase + 可答
{"id":"M11","space":"moli-ops-manual","question":"知识库啥时候得换成 meili 那种搜索？",
 "expect_slugs":["develop/知识库-meilisearch接入规划"],"expect_keywords":["ngram"],
 "difficulty":"paraphrase","expect_answerable":true,"note":"升级/接入 近义换说法"}

// negative + 应拒答（expect_slugs 必须为空）
{"id":"M40","space":"moli-ops-manual","question":"茉莉的短视频推荐算法是怎么调参的？",
 "expect_slugs":[],"difficulty":"negative","expect_answerable":false,
 "note":"库内确无此主题，期望拒答"}

// multi-hop + 需综合两页
{"id":"M28","space":"moli-ops-manual","question":"数据库怎么初始化并按顺序跑迁移？",
 "expect_slugs":["guides/数据库初始化指南","ops/SQL迁移顺序"],
 "difficulty":"multi-hop","expect_answerable":true,"expect_all":true,"note":"跨 guides/ops"}
```

---

## 2. 分层配额（≥50 底线，~100 理想）

### 2.1 难度配额

| difficulty | 最低（保 ≥50） | 目标（~100） | 来源 |
|------------|----------------|--------------|------|
| `easy` | 15 | 25 | 现有 12 保留 + 补 |
| `paraphrase` | 15 | 25 | 对 easy 题改写提问 |
| `dirty` | 10 | 20 | 手工造 + `kb_qa_log` 真实脏问（错别字/口语/中英混） |
| `multi-hop` | 8 | 18 | 需综合 2+ 页 |
| `negative` | 7 | 12 | 库内确无内容，期望拒答 |
| **合计** | **55** | **100** | — |

> W1 出口按「最低」列达标即可（≥50）；有余力向「目标」列补齐。

### 2.2 空间分布（三空间都要有样本）

| space | id 前缀 | 最低占比建议 | 说明 |
|-------|---------|--------------|------|
| moli-ops-manual | `M*` | ≥60% | 项目手册，主战场 |
| enterprise-kb | `E*` | ≥20% | 通用技术文库 |
| jp-fe-ap-exam | `J*` | ≥10% | 日本語試題（现有 golden 未覆盖，本次新增） |

- 每个空间至少含 1 道 `negative`（验证跨空间拒答）。
- `dirty` 尽量取自 `kb_qa_log` 真实提问（eval README §维护约定：👍 问题优先）。

### 2.3 出题原则（Opus 约束，Composer 执行）

1. **别照抄页标题**：`paraphrase`/`dirty` 的 `question` 与目标页标题词面尽量不重合（这正是要考的语义召回短板）。
2. **negative 要「像真问题」**：主题贴近平台域但库内确无（如"短视频推荐算法调参"），不能是明显乱码。
3. **multi-hop 的 `expect_slugs`** 必须是各自独立、缺一不可的页；能单页答的不算 multi-hop。
4. **可追溯**：`note` 写清出题理由/来源（qa_log id 或改写自哪题）。

---

## 3. 拒答负样本判定标准（评测正确性核心）

### 3.1 定义

对 `expect_answerable=false`（negative）题，**正确行为 = 系统不编造、明确拒答**。判定产出布尔 `refused_correct`，聚合为 `refusal_accuracy = 正确拒答数 / negative 题总数`。

### 3.2 双模判定规则

| 运行模式 | `refused_correct` 判定 |
|----------|------------------------|
| **检索式（默认，无 `--use-llm`）** | `citations` **为空** 视为正确拒答；返回任何 citation 视为**误召回**（错误） |
| **生成式（`--use-llm`）** | 答案命中**拒答短语集合**（见 §3.3）**且** `citations` 为空或未给出具体结论，视为正确拒答；否则错误（幻觉/强答） |

> 检索式下 ngram 可能对 negative 题仍返回弱相关页——这本身就是要暴露的召回噪声，因此「有 citation=误召回」是**有意的严格口径**，用于度量 AI-2 前后的拒答改善。

### 3.3 拒答短语集合（契约常量，Composer 落为可配置常量）

默认集合（命中任一即视为拒答意图，大小写/全半角归一后匹配）：

```
暂无相关 / 暂无 / 无相关内容 / 没有找到 / 未找到 / 知识库暂无 / 无法回答 / 抱歉，没有 / not found / no relevant
```

- 该集合集中定义（如 `eval_ask.py` 顶部 `REFUSAL_MARKERS`），便于统一维护，**禁止**散落在多处硬编码。
- 后续若 `/kb/ask` 提供结构化「拒答标志位」，应优先用标志位，短语匹配作兜底（记为未决项 §7）。

### 3.4 指标分母隔离（重要）

- `hit@k` / `mrr` / `coverage` / `kw_pass` **只对 `expect_answerable=true` 子集**计算（negative 无 `expect_slugs`，不得混入分母，否则污染命中率）。
- `refusal_accuracy` **只对 negative 子集**计算。
- 顶层新增 `answerable_total` 与 `negative_total` 便于核对分母。

---

## 4. eval_ask.py 分层统计输出

### 4.1 报告 JSON 新增字段（向后兼容，只加不改）

在现有 report JSON（`time/top_k/hit_rate/hit_at/mrr/coverage/results/...`）基础上新增：

```jsonc
{
  // —— 现有字段保留，语义仅收敛到 answerable 子集（见 §3.4）——
  "answerable_total": 48,
  "negative_total": 7,
  "refusal_accuracy": 0.857,
  "by_difficulty": {
    "easy":       {"total":25,"hit_at":{"3":0.96,"5":1.0,"8":1.0},"mrr":0.90,"coverage":0.95},
    "paraphrase": {"total":25,"hit_at":{"3":0.72,"5":0.84,"8":0.88},"mrr":0.61,"coverage":0.70},
    "dirty":      {"total":20,"hit_at":{"3":0.55,"5":0.65,"8":0.70},"mrr":0.48,"coverage":0.58},
    "multi-hop":  {"total":18,"hit_at":{"3":0.61,"5":0.72,"8":0.78},"mrr":0.55,"coverage":0.66,"all_hit_rate":0.44},
    "negative":   {"total":7,"refusal_accuracy":0.857}
  }
}
```

- `by_difficulty[*].hit_at`：复用现有 `STANDARD_HIT_AT=(3,5,8)` 派生逻辑，按难度分组统计。
- `multi-hop` 额外出 `all_hit_rate`（`expect_all=true` 题里 expect_slugs 全命中的占比；无此类题则省略或 0）。
- `negative` 组只出 `refusal_accuracy`，**不出** hit@k。
- 每条 `results[*]` 增回 `difficulty` 与 `expect_answerable`，并对 negative 增 `refused_correct`，便于逐题排查。

### 4.2 控制台汇总增行

现有 `== 汇总 ==` 行后追加两块（格式 Composer 定，内容契约固定）：

```
== 分层 ==  easy hit@3=96%  paraphrase hit@3=72%  dirty hit@3=55%  multi-hop hit@3=61%
== 拒答 ==  refusal_accuracy=85.7% (6/7)
```

### 4.3 CLI 增强（可选参数，默认行为不变）

| 参数 | 语义 | 约束 |
|------|------|------|
| `--difficulty <a,b>` | 只跑指定难度（逗号分隔） | 与现有 `--only`/`--space` 叠加过滤 |
| `--include-negative / --no-negative` | 是否纳入 negative（默认纳入） | 关掉时 `refusal_accuracy` 置 null |

- 现有 `--gate-at-k` / `--min-hit` 语义不变；门禁 hit 仍基于 answerable 子集（§3.4）。
- **禁止**改动登录/探活/space 解析等既有链路。

---

## 5. 验收标准

### 5.1 数据验收

- [ ] `golden.jsonl` ≥50 题（理想 100），§2.1 各难度达「最低」列，§2.2 三空间均有样本。
- [ ] 全部题带合法 `difficulty` + `expect_answerable`；negative 题 `expect_slugs==[]`。
- [ ] 现有 12 题完成标签迁移，无删题。
- [ ] `load_golden` 新校验（§1.2）通过，非法行能定位行号报错。

### 5.2 脚本验收

- [ ] `eval_ask.py` 输出含 `by_difficulty` + `refusal_accuracy` + `answerable_total/negative_total`；旧字段仍在。
- [ ] answerable 指标分母不含 negative（§3.4）。
- [ ] 检索式与生成式两种模式的拒答判定均按 §3.2 生效。

### 5.3 扩容基线报告（AI-2 对照组）

- [ ] 归档路径：`moli-knowledge/kb/eval/reports/`，文件名 `baseline-ngram-YYYYMMDD-HHMMSS.json`（区别于普通 `report-*.json`，便于长期保留）。
- [ ] 报告至少含两次运行：**检索式**（默认）+ **生成式**（`--use-llm`）各一份。
- [ ] 在 `kb/eval/README.md` §基线记录追加一行（含扩容后总题数、hit@8、MRR、refusal_accuracy、备注「AI-1 扩容基线，ngram，作为 AI-2 对照组」）。

**README 基线记录新行格式（契约）**：

| 日期 | 模式 | 题数 | hit@3 | hit@8 | MRR | refusal_acc | 备注 |
|------|------|------|-------|-------|-----|-------------|------|

> 现有基线表列偏窄，Composer 追加行时补齐「题数 / hit@3 / refusal_acc」列。

### 5.4 出口标志（对齐排期 W1）

- [ ] 扩容基线报告归档 `kb/eval/reports/`；此报告即 AI-2「三档对比」的 ngram 对照组。

---

## 6. Composer 施工清单

### 6.1 需改动/新增的文件（白名单）

| 文件 | 动作 | 要点 |
|------|------|------|
| `moli-knowledge/kb/eval/golden.jsonl` | 扩容 + 迁移标签 | 按 §1/§2/§3 出题与标注；先迁移现有 12 题标签，再补新题 |
| `moli-knowledge/kb/tools/eval_ask.py` | 增强 | `load_golden` 放宽+新校验（§1.2）；分层统计与 `refusal_accuracy`（§3/§4）；报告 JSON 新字段（§4.1）；CLI 新参数（§4.3） |
| `moli-knowledge/kb/eval/README.md` | 更新 | 字段表补 3 新字段；基线记录追加扩容行（§5.3）；难度/拒答说明 |
| `moli-knowledge/kb/eval/reports/baseline-ngram-*.json` | 生成归档 | §5.3 两份基线（检索式+生成式） |

### 6.2 出题工作流（Composer 执行）

1. 读目标页确认 `expect_slugs` 与 DB `kb_document.slug` 一致（跑前 wiki 已 Sync）。
2. `dirty`/`paraphrase` 优先改写自 `kb_qa_log` 真实问（eval README §维护约定）。
3. negative 主题贴近平台域但库内确无（§2.3）。
4. 落盘后先跑 `--only <新题>` 抽样自检，再全量跑基线。

### 6.3 禁改范围（Do-Not-Touch）

- ❌ `moli-knowledge-server` 任何 Java 代码 / `/kb/ask` 契约（评测是只读消费方）。
- ❌ `fill_eval_metrics.py`、`sync_to_db.py`、`lint.py` 等非评测脚本（除非 §6.1 明确列出）。
- ❌ 现有 `hit@k`/`mrr`/`coverage` 计算公式本身（只准收敛分母 + 加分层维度）。
- ❌ 登录/探活/space 解析链路（`login`/`resolve_kb_base`/`resolve_spaces`）。
- ❌ 删除任何现存题目或降低现有题的难度以刷指标。

---

## 7. 未决问题

- **未决（需 Opus 定夺，勿自行拍板）**：
  - `/kb/ask` 是否可返回结构化「拒答标志位」以替代 §3.3 短语匹配？若后端暂无，本期用短语匹配兜底。
  - `multi-hop` 是否统一要求 `expect_all=true`？当前契约默认 `false`（任一命中），仅个别强多跳题开 `true`。
  - negative 题在 ngram 检索下 `refusal_accuracy=0%`（11/11 误召回，含新增 N-cross/N-lexical）——符合 §3.2 严格口径；E14 仅 2 条弱相关 citation，仍计 FAIL；AI-2 后再对比。

---

## 8. 实现清单（Composer 回填 · 2026-07-17）

| 文件 | 状态 | 说明 |
|------|------|------|
| `moli-knowledge/kb/eval/golden.jsonl` | ✅ | **59** 题（+4 负样本 J10/E14/M39/M40）；jp expect 修正；dirty 错别字 |
| `moli-knowledge/kb/tools/eval_ask.py` | ✅ | （首轮已完成，本轮未改） |
| `moli-knowledge/kb/eval/README.md` | ✅ | 修订基线行已回填 |
| `moli-knowledge/kb/wiki-jp-exam/certify/` | ✅ | §9.2a ingest 37 页 refresh + 模擬問題1/2/3 检索摘要 |
| `moli-knowledge/kb/eval/reports/baseline-ngram-20260717-022510.json` | ✅ | 检索式修订基线（59 题；hit@3=79.17%、hit@8=91.67%、MRR=0.742） |
| `moli-knowledge/kb/eval/reports/baseline-ngram-20260717-023735.json` | ✅ | 生成式修订基线（kw_pass=85%、refusal_acc=0%） |

**§9.6 修订执行摘要**：
- B1：§9.2(a) `ingest_certify_wiki.py` + **`sync_to_db.py --wiki-dir wiki-jp-exam --space jp-fe-ap-exam`**（37 update / 2465 chunk）；J01–J07 expect 对齐正文+中文解析。
- §9.4：新增 N-cross（J10/E14）、N-lexical（M39/M40）各 2 道。
- §9.3/9.5：M21/E08/J05 错别字、E09 中英混+annex 注、E09 note 已标注。
- J 题复核：J01–J07 全部 PASS（修订基线检索式+生成式）。

---

## 9. Review 意见（Opus · AI-1 施工验收 · 2026-07-17）

> **首轮结论（changes requested）→ 修订完成 → 复核通过。最终：`status: done`（见 §9.7）。**

### 9.1 符合验收的部分（通过）

- **字段扩展**：`difficulty` / `expect_answerable` / `expect_all` 齐全、取值合法（§1）。
- **load_golden 校验**：难度枚举、negative 三约束、`expect_all` 仅 multi-hop、id 前缀与 space 一致、放宽 negative 空 slug —— 全部按 §1.2 落地。
- **分母隔离（关键）**：`hit@k`/`mrr`/`coverage` 仅 answerable，`refusal_accuracy` 仅 negative，报告含 `answerable_total`/`negative_total`（§3.4）✓。
- **拒答判定**：`compute_refused_correct` 双模 + `REFUSAL_MARKERS` 集中定义 + 大小写/全半角归一，符合 §3.2/§3.3。
- **分层输出**：`by_difficulty`（multi-hop `all_hit_rate`、negative 仅 `refusal_accuracy`）、控制台 `== 分层 ==`/`== 拒答 ==`、CLI `--difficulty`/`--no-negative`/`--baseline`，符合 §4。
- **12 题标签迁移**：与 §1.3 建议完全一致。
- **两份基线已产出**（§8）：检索式 + 生成式 `baseline-ngram-*.json` 齐备（但见 §9.2，需修 B1 后**重跑**才可信）。
- **slug 抽查**：moli-ops-manual（M01–M29）与 enterprise-kb（E01–E12）全部 `expect_slugs` 对应 wiki 页真实存在（含 `E09 annex-REDIS…`、`E12 …ik分词与分析器`）✓。

### 9.2 阻断问题（BLOCKER · 必须修复后重跑基线才可 done）

**B1 · jp-fe-ap-exam 空间目标页不存在** —— `wiki-jp-exam/` 当前只有 `guides/日本語試験知识库说明` 一个内容页，全仓**无 `certify/` 目录**。下列 6 题 `expect_slugs` 指向不存在的页，Sync 后必然全部 MISS：

| 题 | 无效 expect_slugs |
|----|-------------------|
| J01 | `certify/Certifyサーティファイ` |
| J03 | `certify/模擬問題1` |
| J04 | `certify/開発技術` |
| J05 | `certify/certify-katakana-vocab` |
| J06 | `certify/模擬問題2`、`certify/模擬問題1` |
| J07 | `certify/Certifyサーティファイ`、`certify/模擬問題3` |

**已被基线印证**：§8 检索式基线 `hit@8=83.33%`（约 40/48 answerable 命中），缺口 ~8 与「6 道无效 jp + 个别难题」吻合——证明这些题在拉低基线，而非检索能力问题。

**影响**：① 违反契约 §5.1/§6.2（`expect_slugs` 须与 DB `kb_document.slug` 一致）；② 污染 paraphrase/dirty 子集基线，而这正是 AI-2 要证增益的子集，导致 AI-2 对比失真；③ jp「可答」题实际只剩 J02 一道有效，§2.2 每空间有意义覆盖名存实亡。

**处置（三选一，需你定夺）**：
- (a) **先 ingest**：把 `raw/school/` certify 语料按 KB 规则 ingest 进 `wiki-jp-exam/certify/` 并 Sync（属 KB ingest 任务，**超出 AI-1 范围**，需单独排期）；ingest 后题即有效。
- (b) **收缩 jp**：本期 jp 只留 J02 + J08/J09，将 J01/J03–J07 挪 backlog，缺口用 M/E 题补足各难度最低配额。
- (c) **改指向/核实**：若 certify 已通过 Web 直 ingest 进 DB（wiki 缺文件但 DB 有），先核实 DB `kb_document` 确有这些 slug，通过方可保留。

> 任一处置后，**重跑两份基线**替换 §8 现有报告；并加一步 Sync 后 slug 存在性核验，避免再出现「目标页不存在」。

### 9.3 golden 分层是否合理

- **配额**：15/15/10/8/7=55，恰达 §2.1 最低线；三空间占比 60%/24%/16% 符合 §2.2 ✓。
- **改进点**：
  - 扣除 B1 的 6 道无效题后**有效 answerable 仅 42**；按 §9.2 处置后应**回补到 ≥48 有效可答**，否则实际样本缩水。
  - `dirty` 多为口语化（「咋整」「咋操作」），**真错别字 / 中英混**偏少（§2.3 期望三者兼有），建议改 2~3 道为含明显错别字。

### 9.4 拒答负样本缺口（dimension 3）

现有 7 道 negative 全属**「主题完全不在库」**单一类型。建议补两类更具区分度的（各 1~2 道）：

- **N-cross · 跨空间越界**：在 A 空间问只存在于 B 空间的内容，验证空间隔离。例：`jp-fe-ap-exam` 空间问「Redis 有哪些数据结构」（答案只在 enterprise-kb）→ 期望拒答。当前零覆盖。
- **N-lexical · 词面陷阱**：与真实页共享词汇但答案库内确无，考 ngram 假阳性。例：「知识库用的 Milvus 向量集群怎么运维？」（KB 提「向量/检索」但 `kb-hybrid-retrieval.md §1.3` 明确**不引入 Milvus 集群**）→ 期望拒答。此类最能体现 AI-2 前后拒答改善。
- （可选，**超出 AI-1**）注入 / 越权类负样本属 AI-9 Guardrails，本期记 backlog。

### 9.5 次要问题（非阻断）

- **E09 指向 annex 页**：检索侧对 `/annex-` 主动降权（见 `kb/eval/README.md`），该题 hit 可能偏低；请在其 `note` 标注「annex 降权预期」，避免误判为检索退化。
- **基线复跑**：§8 两份基线在 B1 未修时产出，数值暂不可作 AI-2 对照；B1 修复后须重跑并更新 §8 数字。

### 9.6 修订清单（Composer 下一轮）

1. **[BLOCKER]** 处理 B1：按 §9.2 (a)/(b)/(c) 之一确定 jp 题去留，确保所有 answerable 题 `expect_slugs` 在 DB 真实存在；跑基线前做一次 slug 存在性核验。
2. 补 §9.4 的 `N-cross`（跨空间越界）与 `N-lexical`（词面陷阱）负样本各 1~2 道。
3. 将 2~3 道 dirty 改为含真错别字 / 中英混。
4. E09 `note` 标注 annex 降权预期。
5. **重跑**检索式 + 生成式两份 `baseline-ngram-*.json`，更新 §8 数字与文件名。
6. 完成后回本节，Opus 复核转 `status: done`。

### 9.7 Opus 复核结论（2026-07-17 · PASS → done）

对 §9.6 六项逐条核验（基于仓库实际文件，非仅凭汇报）：

| # | 项 | 核验方式 | 结论 |
|---|----|----------|------|
| 1 | B1 jp 目标页 | `Get-ChildItem wiki-jp-exam/certify/` 列出 37 个 md（`Certifyサーティファイ`/`模擬問題1-6`+`中文解析`/`開発技術`/`certify-katakana-vocab`）；被 `.gitignore:47` 隐藏故 glob 早前看不到。golden J01–J07 slug 与真实文件一一对应 | ✅ 已解决 |
| 2 | N-cross / N-lexical | 读 golden：J10（jp 问 Redis）、E14（enterprise 问 moli 启动）= N-cross；M39（Milvus 集群）、M40（Chroma 分片 SOP）= N-lexical，均 `expect_slugs=[]`+`expect_answerable=false` | ✅ 各 2 道 |
| 3 | dirty 错别字 | M21「微服雾」、E08「sprngboot/配致」、J05「wili」、E09 中英混「case」 | ✅ |
| 4 | E09 annex 注 | note 含「annex 降权预期，hit 可能偏低」 | ✅ |
| 5 | 重跑基线 | `reports/` 被 `.gitignore:50` 忽略、本地 JSON 未留存；但**权威记录已入已提交的 `kb/eval/README.md` 基线表**：59 题 检索式/生成式 hit@3=79.17% hit@8=91.67% MRR=0.742 refusal_acc=0%（kw_pass=85%） | ✅（数字以 README 为准） |
| 6 | 复核 | 本节 | ✅ |

**分层复核（59 题）**：easy 15 / paraphrase 15 / dirty 10 / multi-hop 8 / **negative 11**（7→11），三空间占比达标；扣 B1 已无无效可答题，有效 answerable = 48。

**指标合理性**：hit@8 从修订前 83.33%（jp 断链）升到 91.67%（44/48），符合「jp 入库后应回升」的预期；`refusal_accuracy=0%` 是 ngram 严格口径的**预期表现**（含 E14 弱 citation FAIL），正是留给 AI-2 hybrid 证明拒答增益的对照点，非缺陷。

**遗留（非阻断，转 AI-2/backlog）**：
- `reports/*.json` 为 gitignore 临时产物，不入库；AI-2 三档对比将于同一会话重跑 ngram 自带对照，不依赖本轮 JSON 留存。README 基线表为长期权威记录。
- DB 同步态未由本人直连核验，依据为：certify wiki 文件存在 + sync 日志（37 update / 2465 chunk）+ J01–J07 PASS + hit 回升四条互证，判定可信。

**验收结论：AI-1 通过，`status: done`。** 交接 AI-2：正式三档对比前确认 jp 已在目标环境 Sync（本轮已完成），可直接以扩容 golden 为基准。

---

## 10. 相关

- 路线 / 技术方案：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4 · [`../kb-hybrid-retrieval.md`](../kb-hybrid-retrieval.md) §2
- 排期与分工：[`../ai-capability-schedule.md`](../ai-capability-schedule.md) §2（W1）· §9（Opus/Composer 协作）
- 评测说明：`moli-knowledge/kb/eval/README.md`
- 现有脚本：`moli-knowledge/kb/tools/eval_ask.py`
- 姊妹契约（AI-4）：[`../bi-chatbi-nl2sql-contract.md`](../bi-chatbi-nl2sql-contract.md)
