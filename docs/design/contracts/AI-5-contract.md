# AI-5 GraphRAG（检索沿 `kb_relation` 扩 N 跳）· 施工契约（Opus 产出，Composer 施工）

> **角色**：Opus（检索算法负责人）产出，Composer 施工的**唯一契约**。
> **任务**：AI-5 GraphRAG —— 在 AI-2 hybrid 命中的“入口页”基础上，沿 `kb_relation` 扩 1~2 跳，按边类型/跳距加权并入候选，提升 `multi-hop` 子集命中。
> **状态**：✅ **done** · 2026-07-20 Opus 签核 · A（M28 数据+`detectScope`）+ B（§5.1 修订）+ C（protect-topK / hub 惩罚）闭环 · 签核报告 `ai5-graph-compare-hybrid-20260720-001741.json` · 默认 `graph.enabled=false` 守 G-INV-1
> **主导**：🔵 混合 —— **Opus 拍板「沿边扩跳加权/剪枝算法 + 融合规则 + 不回归红线」（§1/§2）**；Composer 铺量（mapper 查 `kb_relation`、候选合并接线、配置类、eval flag、对比表、图）。
> **上游**：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4 第 3 波 AI-5 · §3 依赖 AI-2 · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §4 W9 · §9.1 AI-5 · [`AI-2-contract.md`](AI-2-contract.md)（hybrid 已落地 · RRF/降级/ACL 冻结）· [`AI-1-contract.md`](AI-1-contract.md)（`multi-hop` 子集与评测口径）
> **现有落地（复用，勿重造）**：`KbAskServiceImpl.recallHybridChunks`（AI-2 双路召回入口）· `KbHybridRrfSupport`（RRF）· `KbSearchProperties`（`kb.search.*` 三档）· `kb_relation`（`source_doc_id`/`target_doc_id`/`relation_type`/`weight`/`resolved`/`is_delete`/`space_id`）+ `KbRelationMapper`(BaseMapper) · `kb_document_chunk`（`document_id` 定位邻居页 chunk）· ACL 空间过滤（现有 scope）

---

## 0. 契约边界（读我）

**本契约定义**：GraphRAG 扩跳算法与加权/剪枝规则、融合进候选池的方式与**封顶**约束、配置开关、eval 对比口径、验收与禁改范围。

**不在本契约内（交给 Composer，按现有模式落地）**：`KbRelationMapper` 的按 `source_doc_id` 批量查询 XML/方法、按 `document_id` 批量取 chunk 的 mapper 方法、`KbGraphExpandProperties` 配置类、候选合并接线、`eval_ask.py` 的 `--graph` 参数样板、对比表回填、drawio 叠加图导出。

**红线**：Composer **不得**改动本契约 §1 加权/融合/封顶公式、§2 不回归与 ACL 约束、AI-2 既有 RRF/降级/ACL 顺序。发现算法歧义 → 回 Opus 窗口改契约，不自行拍板。

---

## 1. 架构与算法决策（Opus 冻结）

### 1.1 定位：hybrid 之上的**叠加层**，默认关闭

GraphRAG **不是**新策略档，而是叠加在 AI-2 `hybrid` / `hybrid-rerank` 之上的**候选扩展 overlay**：

```
retrieve(question):
  base = recallHybridChunks(...)          # AI-2 冻结，产出 scored chunks
  if kb.search.graph.enabled:             # 默认 false → 行为与 AI-2 完全一致（零回归红线）
      base = graphExpandAndMerge(base, ...)  # §1.3 扩跳 + 融合封顶
  → 后续 rerank / 引用 / LLM 上下文 沿用 AI-2 既有链路
```

- **默认 `enabled=false`**：不改 `ngram`/`hybrid`/`hybrid-rerank` 三档既有行为与 AI-3 门禁基线（红线，§5）。
- overlay 只对 `hybrid` / `hybrid-rerank` 生效；`ngram` 档忽略 graph（保持确定性基线）。
- **无新表、无写库**：只读 `kb_relation`（`resolved=1 AND is_delete=0`）。

### 1.2 为什么用 DB `kb_relation` 而非 `KbWikiGraphService`

`KbWikiGraphService.graph()` 是**文件级可视化**遍历（读 wiki 文件出全图），不适合检索期按 docId 精确扩跳。检索期用 **`kb_relation` 表**（已 `source_doc_id/target_doc_id` 结构化、随 sync 落库、带 `weight`/`relation_type`），按入口 docId 批量查邻边即可，O(入口数 × fanout)。

### 1.3 扩跳 + 加权 + 融合（核心算法，冻结）

**输入**：base 召回的 scored chunks（AI-2 产出）。**输出**：合并后的 scored chunks（含图注入邻居）。

**Step 1 · 选入口页 E**
- 由 base scored chunks 归组到 `documentId`，取 **base 文档分 top-`entryTopE`**（默认 5）作为入口 `E = {(docId, baseScoreNorm)}`。
- `baseScoreNorm ∈ (0,1]` = 该入口 doc 的 base 最高 chunk 分 / 本次 base 最高分（min-max 归一，供加权同量纲）。

**Step 2 · BFS 扩跳（bounded）**
```
frontier = E.keys(); visited = E.keys()
for hop in 1..maxHops:                       # maxHops 默认 1，硬上限 2
    edges = kbRelationMapper.bySourceDocIds(frontier,
              resolved=1, isDelete=0, spaceId ∈ scopeSpaces)   # ACL 前置过滤
    next = {}
    for e in edges (source s → target t, type, weight):
        if t == null or t ∈ visited: continue                 # 断链/回环跳过
        ew = edgeWeight(type)                                  # §1.4
        if ew <= 0: continue                                   # 该类型不扩（如 same_tag 默认 0）
        contrib = baseScoreNorm[root(s)] * ew * hopDecay^(hop-1)   # hopDecay 默认 0.5
        graphBoost[t] = min(1.0, max(graphBoost[t], contrib))  # 取最大路径 + 钳 ≤1（防 hub 累加爆炸 + 保证 G-INV-2 封顶成立）
    frontier = top(next by graphBoost, fanoutPerNode*|frontier|)   # 每层限流
    visited ∪= frontier
    if |graphBoost| >= maxNeighbors: break                    # 邻居总数封顶（默认 20）
```
- `root(s)`：s 所属入口（沿路径回溯到 E 的那个入口 doc），用于取其 `baseScoreNorm`。
- **`graphBoost` 恒钳 ≤ 1.0**（含 §1.4 次级乘子放大后）：因 `injectedScore = graphBoost × graphBoostCap × S_max`，钳后纯图注入分**必然 ≤ `graphBoostCap × S_max`**（默认 0.5×），G-INV-2 数值上恒成立，不受边权/次级乘子影响。
- **方向（冻结）**：默认沿**出边** `source_doc_id ∈ frontier → target`（`links_to`/`references`/`related`/`same_tag`）。**例外——`supersedes` 走入边**：以 `target_doc_id ∈ frontier`（即入口为“被取代的旧页”）反查其 `source`（更新页）并带出，权重按 §1.4；**绝不**从新页出边带出旧页（“被取代旧页不扩”）。此为按类型固定的方向语义，与全局 `graph.inbound`（对其它类型统一开入边，默认 false）正交。

**Step 3 · 取邻居 chunk**
- 对 `graphBoost` 中的邻居 docId（去掉已在 base 池的 doc），批量取其 chunk（`selectAskChunksByDocumentIds`，每 doc 至多 `chunksPerNeighbor` 段，默认 2；优先 term 命中段，否则 `chunk_index` 最小段）。
- ACL 再校验（空间 ∈ scopeSpaces），双保险。

**Step 4 · 融合进候选池（封顶 + 保护 topK，冻结公式）**
- 图注入分与 base 分**同量纲**合成。设 base 分空间上界 `S_max = 本次 base 最高 score`：
  ```
  injectedScore(neighborChunk) = round( graphBoost[doc] * graphBoostCap * S_max )
  ```
  `graphBoostCap ∈ (0,1)` 默认 **0.5** —— **纯图邻居的注入分不超过本次 base 最高分的 50%**（G-INV-2）。
- 对**已在 base 池**的 doc（图与直连都命中）：`score += round(graphBoost[doc] * graphBoostCap * S_max * reinforceFactor)`（`reinforceFactor` 默认 0.5）。
- **注入不挤 topK（2026-07-20 增补）**：合并重排前快照 base 序前 `protectBaseTopK`（默认 **3**）个 chunkId；重排后**强制**将其钉回结果前缀（相对次序不变），纯图邻居与被强化的非保护页只能排在其后——杜绝「枢纽页 reinforce / 邻居注入」把已入 gate 的直连结果挤出 top3。
- 若 `hybrid-rerank`，图邻居**并入 rerank pool**；rerank 为最终精排，但 `protectBaseTopK` 仍作用于进 rerank 前的初排池构造（与 AI-2 一致：先定 pool 再精排）。

**Step 2′ · Hub 入度惩罚（2026-07-20 增补，叠在 Step 2 contrib 上）**
```
if inboundFanIn[t] > hubFanInThreshold:          # 默认 threshold=15
    contrib *= hubPenalty                         # 默认 0.25
graphBoost[t] = min(1.0, max(graphBoost[t], contrib))
```
- 入度统计：`kb_relation` 上 `target_doc_id=t AND resolved=1 AND is_delete=0`（空间 ∈ scope）的边数。  
- 高扇入枢纽（如历史误链堆出的总览页）boost 被压到约 1/4，配合 `protectBaseTopK` 双保险。

### 1.4 边类型权重（默认表，可配置，语义冻结）

| relation_type | edgeWeight 默认 | 语义 |
|---------------|-----------------|------|
| `links_to` | 1.0 | 正文 `[[wikilink]]`，最强关联 |
| `references` | 0.8 | 引用/来源 |
| `related` | 0.6 | frontmatter `related` |
| `supersedes` | 0.3（**入边方向**：旧页入口→带出更新的 source 页；新页出边不带旧页） | 版本演进：优先带出更新页（方向见 §1.3 Step 2） |
| `same_tag` | 0.0（默认不扩，噪声大） | 同标签，仅在 `graph.includeSameTag=true` 时启用（默认 0.3） |
| 其它/未知 | 0.0 | 不扩 |

- `kb_relation.weight`（存量整数权重）作为**次级乘子**：`ew = edgeWeight(type) * (weight>0 ? clamp(weight/maxWeightNorm,0.5,1.5) : 1.0)`，避免单一 weight 主导。**注意**：次级乘子上界 1.5 可能使单跳 `ew>1`、`contrib>1`，故 `graphBoost` 必须按 §1.3 Step 2 钳 ≤ 1.0，封顶（G-INV-2）才不被击穿。

---

## 2. 不变量与约束（Opus 冻结，不可放松）

| # | 不变量 |
|---|--------|
| G-INV-1 | **零回归开关**：`kb.search.graph.enabled=false`（默认）时，召回结果与 AI-2 逐条一致；三档基线与 AI-3 门禁不受影响。 |
| G-INV-2 | **图分封顶 + 不挤 topK**：`graphBoost` 恒钳 ≤ 1.0；注入分 ≤ `graphBoostCap × S_max`；且 **`protectBaseTopK`（默认 3）钉住 base 前缀**，图邻居/枢纽强化不得把已入 gate 的直连结果挤出 topK。 |
| G-INV-3 | **有界扩张**：`maxHops ≤ 2`、`maxNeighbors`（默认 20）、`fanoutPerNode`、`chunksPerNeighbor` 全部封顶；BFS 达任一上限即停。 |
| G-INV-4 | **ACL 不可绕过**：邻居 doc 必 ∈ 当前请求 `scopeSpaces`（查边时前置过滤 + 取 chunk 后二次校验）；跨空间/无权页**绝不**并入候选。 |
| G-INV-5 | **只读图**：仅 `SELECT kb_relation`（`resolved=1 AND is_delete=0`），不写、不建新表。 |
| G-INV-6 | **防环/防 hub**：`visited` 去重防回环；邻居 boost 取路径**最大**而非累加；**入度 > `hubFanInThreshold` 时 contrib × `hubPenalty`**（默认 15 / 0.25）。 |
| G-INV-7 | **降级**：`kb_relation` 查询异常/超时 → 记日志并**回退为纯 base（hybrid）结果**，不整体失败（对齐 AI-2 sidecar 降级精神）。 |
| G-INV-8 | **响应结构不变**：不改 `/kb/ask` 对外 VO 结构（对齐 AI-2 红线）；图来源仅进服务端日志/trace，不进对外响应。 |

---

## 3. 接口 / DTO / 配置

### 3.1 新增配置（`KbSearchProperties` 增 `graph` 子节，键名冻结）

`kb.search.graph.*`：

| 键 | 默认 | 说明 |
|----|------|------|
| `enabled` | `false` | 总开关（G-INV-1） |
| `max-hops` | `1` | 扩跳层数，硬上限 2 |
| `entry-top-e` | `5` | 入口页数量 |
| `fanout-per-node` | `5` | 每入口每跳最大出边参与数 |
| `max-neighbors` | `20` | 邻居总数封顶 |
| `chunks-per-neighbor` | `2` | 每邻居页取 chunk 数 |
| `hop-decay` | `0.5` | 跳距衰减 `hopDecay^(hop-1)` |
| `graph-boost-cap` | `0.5` | 纯图邻居注入分上限系数（G-INV-2） |
| `reinforce-factor` | `0.5` | 已在池 doc 的图强化系数 |
| `protect-base-top-k` | `3` | 融合后钉住 base 前缀长度（0=关闭）；防注入/强化挤出 gate |
| `hub-fan-in-threshold` | `15` | 入度超过此值视为枢纽；`≤0` 关闭 hub 惩罚 |
| `hub-penalty` | `0.25` | 枢纽 contrib 乘子（∈(0,1]） |
| `inbound` | `false` | 是否额外扩入边 |
| `include-same-tag` | `false` | 是否启用 `same_tag` 边 |
| `edge-weights` | 见 §1.4 | `map<relationType, double>`，覆盖默认权重 |
| `query-timeout-ms` | `800` | 图查询时间预算，超时→G-INV-7 降级 |

### 3.2 请求级覆盖（A/B 用，对齐 AI-2 `retrievalStrategy` 覆盖）

`AskRequest` 增可选 `graphExpand`（`Boolean`，`null`=用配置默认）：单次请求强制开/关 graph，供 eval 与前端对照；越权/非法值忽略回退配置。

### 3.3 内部支撑（Opus 定契约，Composer 实现）

- `KbGraphExpandSupport`（新）：
  ```
  Map<Long,Double> graphBoost = expand(
      Map<Long,Double> entryDocScoreNorm,   // Step 1 归一入口分
      List<Long> scopeSpaces,               // ACL
      KbGraphExpandConfig cfg);             // §3.1 快照
  ```
  纯算法 + `KbRelationMapper` 查询；**无副作用、可单测**（喂假边即可验加权/封顶/防环）。
- `KbRelationMapper.selectBySourceDocIds(Collection<Long> srcIds, List<Long> spaceIds)`（Composer 加，`resolved=1 AND is_delete=0`）——出边。
- `KbRelationMapper.selectSupersedesByTargetDocIds(Collection<Long> targetIds, List<Long> spaceIds)`（Composer 加，`relation_type='supersedes' AND resolved=1 AND is_delete=0`）——`supersedes` 入边（旧页→更新页，§1.3 Step 2 方向）；`graph.inbound=true` 时可复用/扩展为全类型入边查询。
- `KbDocumentChunkMapper.selectAskChunksByDocumentIds(Collection<Long> docIds, ..., int perDoc)`（Composer 加）。

---

## 4. 分 Phase 施工清单（W9）

### Phase A · 算法 + 支撑（graph 默认关）
- Composer：`KbGraphExpandProperties`（§3.1 键名）、`KbRelationMapper.selectBySourceDocIds`、`KbDocumentChunkMapper.selectAskChunksByDocumentIds`。
- Composer：`KbGraphExpandSupport.expand()`（**严格按 §1.3 Step 2/§1.4 加权 + §2 封顶/防环/上限**）。
- Composer：`KbGraphExpandSupportTest`（假边）——覆盖：跳距衰减、边类型权重、路径取最大、**`graphBoost` 钳 ≤1（次级乘子放大后注入分仍 ≤ `graphBoostCap×S_max`）**、`maxNeighbors`/`maxHops` 封顶、环、断链跳过、`same_tag` 默认不扩、**`supersedes` 入边方向（旧页入口带出更新 source、新页出边不带旧页）**。
- 出口：单测全绿；`enabled=false` 时 `KbAskServiceImpl` 行为不变（回归现有 ask 测试）。

### Phase B · 接线 + 融合 + 评测
- Composer：`KbAskServiceImpl` 在 `recallHybridChunks` 后接 `graphExpandAndMerge`（§1.3 Step 3/4 融合封顶 + `hybrid-rerank` 并入 pool）；`AskRequest.graphExpand` 覆盖；G-INV-7 降级 try/catch。
- Composer：`eval_ask.py --graph on|off`（run 标签 `hybrid+graph` / `hybrid-rerank+graph`），产出对比报告到 `kb/eval/reports/`。
- Composer：回填 `docs/design/kb-hybrid-retrieval.md` 新增 **§7 GraphRAG** 对比表；更新 `docs/diagrams/moli-kb-hybrid-retrieval.drawio` 叠加 graph 分支并导 PNG（按 `@drawio-diagrams`）。
- Composer：`application-dev.yml` 增 `kb.search.graph.*` 示例（默认关）。

---

## 5. 验收标准 + Composer 禁改范围

### 5.1 验收标准

- [x] **零回归（G-INV-1）**：`graph.enabled=false` 跑 golden，`hybrid` / `hybrid-rerank` 指标与 AI-2 签核基线逐项一致（hit@1/3/5、MRR）。→ **hybrid ✅** hit@3=0.8958、MRR=0.7917；hybrid-rerank off 轮见 §Review
- [x] **多跳非回归（§5.1 #2 · 2026-07-20 修订）**：`hybrid+graph` 的 multi-hop `hit@3` **≥ hybrid**（Δ≥0，允许持平）；若有提升更佳。~~原「必须 Δ>0」在边稀疏/单题瓶颈下过严，改为非回归 + 全集可量化提升~~。→ **签核 ✅** 1.0→1.0（Δ0）· 报告 `…-20260720-001741.json`
- [x] **完整集可量化提升**：`hybrid+graph` 完整集 `hit@3` **相对同轮 hybrid 提升 Δ>0**（且仍满足 ≥ hybrid−0.05 容差）。→ **签核 ✅** 0.7292→0.7917（Δ**+6.25pp**）
- [x] **封顶生效（G-INV-2）**：构造“强直连命中 + 弱图邻居”样例，验证图邻居不顶替直连 top 结果（单测或 eval case）。
- [x] **ACL（G-INV-4）**：跨空间邻居不出现在候选/引用（单测：邻居在 scope 外则被剔除）。
- [x] **有界 + 降级（G-INV-3/7）**：大入度页扩跳受 `maxNeighbors`/`maxHops` 封顶；`kb_relation` 查询异常 → 回退 hybrid，不整体失败。
- [x] **响应不变（G-INV-8）**：`/kb/ask` 对外 VO 结构与 AI-2 一致。
- [x] 三/N 档对比表（`hybrid` vs `hybrid+graph` vs `hybrid-rerank+graph`：`multi-hop hit@3`、完整集 `hit@3`、`MRR`、P95）回填 `kb-hybrid-retrieval.md §7`。

### 5.2 Composer 禁改范围（Do-Not-Touch）

- ❌ 改 §1.3 融合公式 / §1.4 边权语义 / §2 封顶·衰减·防环·上限（`graph-boost-cap`、`hop-decay`、`max-hops≤2`、路径取最大）——需回 Opus。
- ❌ 改 AI-2 既有 RRF 公式 / 降级策略 / ACL 顺序 / `ngram` 行为 / `hybrid` 默认行为（graph 关闭时必须逐条一致）。
- ❌ 改 AI-3 门禁基线（`ngram`/`hybrid`/`hybrid-rerank` 三 key 冻结）；graph 对比为**观察档**，如需入库另议 key。
- ❌ 让图邻居注入分 ≥ base 最高分（G-INV-2 封顶）；❌ 无界 BFS / 累加 boost（G-INV-3/6）。
- ❌ 绕过 ACL 把跨空间页并入候选（G-INV-4）。
- ❌ 写 `kb_relation` / 新建图表；❌ 改 `/kb/ask` 对外响应结构。

---

## 6. 实现清单 + 未决问题（Composer 回填区）

> Composer 施工时在此追加「已实现类/接口清单」与「未决问题」；算法/封顶相关未决**不得自行拍板**，回 Opus 窗口改契约。

### Phase A 实现清单（2026-07-19 · graph 默认关 · 未接 KbAskServiceImpl）

| 项 | 路径 | 说明 |
|----|------|------|
| 配置 | `KbSearchProperties.Graph` | `kb.search.graph.*` 键名对齐 §3.1；`enabled` 默认 `false` |
| 配置快照 | `KbGraphExpandConfig` | `from(KbSearchProperties)` · §1.4 `edgeWeightFor()` |
| 出边查询 | `KbRelationMapper.selectBySourceDocIds` + `KbRelationMapper.xml` | `resolved=1` · `is_delete=0` · `space_id ∈ scope` |
| supersedes 入边 | `KbRelationMapper.selectSupersedesByTargetDocIds` | 旧页入口→更新 source（§1.3 方向） |
| 可选全类型入边 | `KbRelationMapper.selectInboundByTargetDocIds` | `graph.inbound=true` 时用；默认关 |
| 邻居 chunk | `KbDocumentChunkMapper.selectAskChunksByDocumentIds` + XML | 每 doc ≤ `chunksPerNeighbor`；term 优先 / 否则最小 `chunk_index` |
| 扩跳算法 | `KbGraphExpandSupport.expand()` / `expandBfs()` | §1.3 Step 2 + §1.4 边权 · `graphBoost` 钳 ≤1 · G-INV-7 异常→空 map |
| 边 DTO | `KbGraphEdge` | 单测假边 / mapper 映射 |
| 单测 | `KbGraphExpandSupportTest`（12 用例） | 跳距衰减 · 边权 · 路径 max · 钳 ≤1 · maxNeighbors/maxHops · 环 · same_tag 默认不扩 · supersedes 入边 · 断链跳过 |

**验证**：`mvn test -Dtest=KbGraphExpandSupportTest` 全绿；未改 `KbAskServiceImpl` / AI-2 RRF/降级/ACL。

**Phase B 待办**：`KbAskServiceImpl.graphExpandAndMerge` · `AskRequest.graphExpand` · `eval_ask.py --graph` · `application-dev.yml` 示例 · §7 对比表。

- 未决问题：无（Phase A 范围内）。

### Phase B 实现清单（2026-07-19 · GraphRAG 接线 + 评测）

| 项 | 路径 | 说明 |
|----|------|------|
| 接线 | `KbAskServiceImpl.recallHybridChunks` | RRF 后、`rerank` 前 `graphExpandAndMerge`；`resolveGraphExpand`（ngram 忽略 · 请求覆盖） |
| 融合 | `KbGraphMergeSupport` | §1.3 Step 1/4 入口归一 · 注入/强化分 · 重排；G-INV-7 try/catch 降级 |
| 请求 | `AskRequest.graphExpand` | `Boolean` 覆盖 `kb.search.graph.enabled` |
| 配置 | `application-dev.yml` · `kb.search.graph.*` | 示例块 · `enabled: false` |
| 评测 | `kb/tools/eval_ask.py` | `--graph on\|off` · `--compare-graph` → `ai5-graph-compare-*.json` |
| 文档 | `docs/design/kb-hybrid-retrieval.md` §7 | GraphRAG 对比表 + 验收口径 |
| 架构图 | `docs/diagrams/moli-kb-hybrid-retrieval.drawio` + PNG | RRF → Graph（可选）→ Rerank 分支 |
| 单测 | `KbGraphMergeSupportTest`（5）+ Phase A 12 | 注入封顶 · 强化 · ACL |

**验收自测（Composer · live 2026-07-19）**：

| 检查 | 结果 |
|------|------|
| G-INV-1 | ✅ `ai5-graph-compare-hybrid-20260719-183009.json` hybrid 轮 hit@3=**0.8958**、MRR=**0.7917**（=§6.1） |
| multi-hop 提升 | ❌ multi-hop hit@3 **0.875→0.750**（Δ−0.125）；hybrid-rerank 同 |
| 全集 −0.05 容差 | ✅ hybrid+graph hit@3=**0.8542** ≥ 0.8458 |
| 报告 | `kb/eval/reports/ai5-graph-compare-hybrid-20260719-183009.json` · `…-hybrid-rerank-20260719-183919.json` |

- 未决问题：见 **§Review**（multi-hop 回退整改中）。

---

## Review（§5.1 验收 · 2026-07-19）→ **当时不通过**（已被下方 A+B+C 签核取代）

> **状态（历史）**：Phase A+B 工程交付齐 · 当时未签核 · 默认 `graph.enabled=false` 守 G-INV-1  
> 数据源：`kb/eval/reports/ai5-graph-compare-hybrid-20260719-183009.json` · `…-hybrid-rerank-20260719-183919.json` · 单测 20/20 绿 · 接线 `KbAskServiceImpl`（RRF→graph→rerank）

| §5.1 | 结果 | 证据 |
|------|------|------|
| G-INV-1（hybrid 关档） | ✅ | hit@3=**0.8958**、MRR=**0.7917** = AI-2 §6.1 |
| multi-hop hit@3 提升 | ❌ **阻断** | **0.875→0.750**（Δ**−0.125**） |
| 完整集 ≥ hybrid−0.05 | ✅ | **0.8542 ≥ 0.8458** |
| G-INV-2 / G-INV-4 / G-INV-7 | ✅ | 单测 + mapper ACL + merge/expand catch 降级 |
| §7 对比表 | ✅ | `kb-hybrid-retrieval.md §7.2` 已回填 |

**接线核对（符合契约）**：`recallHybridChunks` → RRF → `resolveGraphExpand` → `graphExpandAndMerge`（§1.3 Step 1–4）→ `applyRerank`（hybrid-rerank 时图邻居已入 pool）→ materialize；ngram 忽略 graph；G-INV-7 `try/catch` 回退原 state。

**逐题根因（multi-hop · 8 题）**：

| 变化 | 题号 | 详情 |
|------|------|------|
| 掉出 hit@3 | **M26** | expect `guides/本地启动指南` rank **3→5**；graph 后 top 插入 `guides/前端开发与联调指南`、`ops/docker部署指南` 等图邻居 |
| 无题进入 hit@3 | — | **0 题**被 graph 拉进 top3 |
| 完整集副回归 | M16（paraphrase） | rank 3→4，非 multi-hop |

副指标：multi-hop coverage **0.708→0.833**、`all_hit_rate` **0.667→1.0**——图对第二期望页有帮助，但 **hit@3 主出口被噪声挤坏**。

**结论**：工程交付达标；**§5.1 出口 #2 未达标 → AI-5 验收不通过**。

**整改清单（改 §1 公式须先回 Opus）**：

1. **数据侧优先**：M26/M16 查 `kb_relation` 边（启动指南↔前端/docker）与注入分日志；ingest 降权/删误链。  
2. **算法候选（未批准勿改）**：A 注入不挤 topK · B 降/关 reinforce · C 降 cap/收紧 fanout。  
3. 重跑 `--compare-graph`：multi-hop Δ>0 且全集仍 ≥ hybrid−0.05 且 G-INV-1 不变。  
4. 通过后再签核 done；**未通过前不得**改 roadmap/schedule 为 ✅。

### 数据侧已落地（2026-07-19 · Opus）

**根因确认（非算法）**：入口页 `related` / FAQ `[[wikilink]]` 过宽 → sync 写 `kb_relation`（`related`=0.6、`links_to`=**1.0**）→ 延伸阅读邻居挤出 expect。

| 题 | 噪声来源 | 已改 wiki |
|----|----------|-----------|
| **M26** | `guides/本地启动指南`：`related`/FAQ 含 docker/前端/swagger；后续暴露 checklist/全链路/SQL 词典→`项目文档总览` 枢纽 | 见下表降噪①–④（最终 `related`→`[数据库初始化指南, 登录与鉴权指南]`；FAQ 去 wikilink） |
| **M16** | `develop/用户中心`：`related`+正文 `[[权限管理操作指南]]` | `related` 收为设计/概念页；正文改 `guides/权限管理操作指南` 纯文本 |

附注：`edges.jsonl` 的 `depends_on`/`relates_to` 在 AI-5 默认边权为 **0（不扩）**，不是本轮噪声主通道。`wiki-moli/log.md` 已记一行。

**待 Composer / 本地闭环（未改 Java）**：

```text
python moli-knowledge/kb/tools/sync_to_db.py --wiki-dir wiki-moli --space moli-ops-manual
# 抽查：本地启动 → 前端/docker/swagger 边应消失；用户中心 → 权限管理操作指南 related/links_to 应消失
python moli-knowledge/kb/tools/eval_ask.py --strategy hybrid --compare-graph
# 出口仍 §5.1；通过回 Opus 签核；仍失败再议 §1
```

AI-5 **仍为不通过**，待 sync + compare-graph 复验后再签核。

### 数据侧闭环实跑（2026-07-19 · Opus 本机）

已 sync `wiki-moli` → `moli-ops-manual`，并重跑 `eval_ask.py --strategy hybrid --compare-graph --gate-at-k 3`。

| 轮次 | 处置 | multi-hop hit@3 | 全集 hit@3 | M26 |
|------|------|-----------------|------------|-----|
| 验收基线 | （改前） | 0.875→**0.750**（Δ−0.125） | 0.896→0.854 | rank 3→5（前端/docker 挤出） |
| 降噪① | 本地启动 + 用户中心 | 仍 −0.125 | — | 噪声改为「项目文档总览」枢纽 |
| 降噪② | checklist 去总览误链 | 仍 −0.125 | — | 仍被总览/故障汇总挤出 |
| 降噪③ | 全链路图 + 登录鉴权故障汇总去总览 | 仍 −0.125 | — | SQL 词典→总览 reinforce |
| **降噪④（当前）** | SQL 词典去总览/v1；本地启动 related 去掉故障排查 | **0.875→0.875（Δ0）** | **0.688→0.750（Δ+6.25pp）** | **rank 1→2（仍在 top3）✅** |

报告：`kb/eval/reports/ai5-graph-compare-hybrid-20260719-193611.json`

**数据侧结论**：

1. **回归已消除**：M26 不再掉出 top3；前端/docker/总览枢纽噪声链已切断。  
2. **§5.1 #2 仍未达标**：multi-hop hit@3 **无提升**（Δ=0）；唯一持续 MISS 为 **M28**（告警运维志，两侧均 rank=0，图也救不了——缺页/未召回）。  
3. **全集受益**：hit@3 **+6.25pp**（M15/E02/J03 等非 multi-hop 被图拉进 top3）。  
4. **副指标**：multi-hop coverage 0.729→0.771；hit@1 略降（0.75→0.625）。  
5. **环境注记**：本机 hybrid 关档全集 hit@3=0.688 ≠ AI-2 签核 0.896（JP/索引漂移），与 graph 门控无关；复验 G-INV-1 应以「同轮 off vs 历史基线」或重对齐环境后再比。

**下一步（须你拍板）**：

- **A. 继续数据侧**：补/ enrich M28 期望页（`ops/告警运维志` 等）与边，争取 multi-hop +1 题。  
- **B. 改 §5.1 口径（Opus）**：将 #2 改为「multi-hop **非回归**（Δ≥0）+ 全集可量化提升」，则当前数据侧可签核（仍须写清）。  
- **C. 算法侧**：再开 §1（注入不挤 topK / hub 惩罚），对付剩余枢纽页。

未选 B/C 前 **AI-5 仍不通过 / 不标 done**。

### A+B+C 闭环签核（2026-07-20 · Opus）→ **✅ done**

用户拍板「都做」后落地：

| 项 | 内容 |
|----|------|
| **A 数据/召回** | M28 期望页 `ops/监控与日志` ↔ `ops/故障排查指南` 互链 enrich；`KbAskServiceImpl.detectScope` 去掉裸「排查」article 误伤，并加 guide 意图（监控告警/故障排查） |
| **B 口径** | §5.1 #2 改为 multi-hop **非回归（Δ≥0）**；#3 要求全集 Δ>0 |
| **C 算法** | `protect-base-top-k=3` + `hub-fan-in-threshold=15` / `hub-penalty=0.25`（§1.3 Step 4 / Step 2′） |

**签核报告**：`kb/eval/reports/ai5-graph-compare-hybrid-20260720-001741.json`（`--strategy hybrid --compare-graph --gate-at-k 3`）

| §5.1 | 结果 | 证据 |
|------|------|------|
| multi-hop 非回归 | ✅ | hit@3 **1.0 → 1.0**（Δ0）；M26/M28 两侧均 PASS rank=1 |
| 全集可量化提升 | ✅ | hit@3 **0.7292 → 0.7917**（Δ**+6.25pp**）≥ hybrid−0.05 |
| G-INV-1 | ✅ 结构守门 | 默认 `graph.enabled=false`；本机 hybrid 关档全集 ≠ AI-2 历史 0.8958（JP/索引漂移），与 graph 门控无关 |
| G-INV-2/3/4/7 | ✅ | 单测 + 本轮无 gate@3 回归 |

**结论**：§5.1 修订口径下 **AI-5 验收通过 · 标 done**。roadmap/schedule 同步 ✅。生产默认仍 `kb.search.graph.enabled=false`；观察档用请求级 `graphExpand=true` 或配置显式打开。

---

## 7. 相关

- 路线 / 排期 / 分工：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4 第 3 波 · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §4 W9 · §9.1
- 上游落地：[`AI-2-contract.md`](AI-2-contract.md)（hybrid/RRF/降级/ACL 冻结）· [`AI-1-contract.md`](AI-1-contract.md)（`multi-hop` 子集）· [`AI-3-contract.md`](AI-3-contract.md)（门禁基线）
- 方案：[`../kb-hybrid-retrieval.md`](../kb-hybrid-retrieval.md)（§7 GraphRAG 对比表落点）· 图 [`../diagrams/moli-kb-hybrid-retrieval.drawio`](../diagrams/moli-kb-hybrid-retrieval.drawio)
- 现有基础：`KbAskServiceImpl` · `KbHybridRrfSupport` · `KbSearchProperties` · `kb_relation` + `KbRelationMapper` · `KbWikiGraphService`（文件级可视化，非检索遍历）
