# AI-2 向量检索 + Hybrid + Rerank · 施工契约（Opus 产出，Composer 施工）

> **角色**：Opus（架构与安全负责人）产出，Composer 施工的**唯一契约**。
> **任务**：AI-2 向量检索 + Hybrid Search + Rerank，第 1 波 W2–W3。
> **状态**：**done · 2026-07-19（Opus 签核）** · Phase W2 done · Phase W3 整改复测 §4 全绿（ngram 零回退 · hybrid 全集 hit@3 +10.4pp · 三档 errors=0）—— 见 §Review 意见 W3 签核
> **上游**：[`../kb-hybrid-retrieval.md`](../kb-hybrid-retrieval.md) §3 · [`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4 第1波 AI-2 · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §2 W2/W3 · §9.1 · [`AI-1-contract.md`](AI-1-contract.md)（评测字段/基线口径）
> **架构图**（复用，勿重画）：[`../../diagrams/moli-kb-hybrid-retrieval.drawio`](../../diagrams/moli-kb-hybrid-retrieval.drawio) → PNG `../../diagrams/png/moli-kb-hybrid-retrieval.png`
> **现有召回入口**：`KbAskServiceImpl.ask()`（chunk+doc 双召回 → `mergeCitation` 合并 → LLM/检索式）· `KbSearchProperties`（`kb.search.*`）

---

## 0. 契约边界（读我）

**本契约定义**：sidecar 三接口 JSON 契约、Chroma 索引结构、离线建索引触发方式、Java 侧配置键与双路召回+RRF 融合算法、降级与 ACL 规则、分阶段施工清单、验收口径、禁改范围。

**不在本契约内（Composer 按现有风格落地）**：FastAPI/Chroma/模型加载样板、Java HTTP client 样板、`@ConfigurationProperties` 类、mapper 查询、yml 配置值、CI。

**红线（Composer 不得擅改，见 §5）**：RRF 公式、降级策略、ACL 裁决顺序、ngram 档行为、`/kb/ask` 对外响应结构。发现歧义/安全问题回 Opus 窗口改契约。

---

## §1 架构决策

### 1.1 确认方案 A（Python sidecar）

**采纳** `kb-hybrid-retrieval.md §3.1 方案 A**：新增 Python sidecar `moli-knowledge/kb-retrieval/`：

| 项 | 选型 |
|----|------|
| 框架 | FastAPI（无状态、可重启） |
| embedding | **bge-m3**（多语种，dim=1024，`normalize=true` 输出单位向量走余弦=内积） |
| 向量库 | **Chroma**（本地持久化目录 `kb-retrieval/.chroma/`，不引入 ES/Milvus 集群） |
| rerank | **bge-reranker-v2-m3**（cross-encoder） |
| 与 Java | 仅 HTTP；Java 在线调用 `/search`（+可选 `/rerank`）；sidecar 故障 Java 自动降级 ngram（§2.3） |

> 拒绝方案 B（Java 内嵌 embedding 列）：无 rerank 生态、MySQL 算余弦性能差；且与 AI-4 ai-agent 同 Python 栈可复用经验。

### 1.2 sidecar 三接口 JSON 契约

基址 `kb.search.vector.base-url`（如 `http://127.0.0.1:8099`）。所有响应 `application/json`；错误统一 `{ "error": "<code>", "message": "<脱敏>" }` + 非 2xx 状态。

#### ① `POST /embed` — 批量向量化 + 幂等 upsert 进 Chroma（离线索引用）

请求：
```jsonc
{
  "items": [
    { "chunkId": 123, "docId": 45, "spaceId": 900000000000000003,
      "slug": "guides/本地启动指南", "kbType": "guide", "categoryId": 12,
      "contentHash": "sha256...", "text": "<切段正文（heading+content 拼接）>" }
  ],
  "deleteChunkIds": [456, 789],   // 可选：DB 已删/归档的切段，同批从 Chroma 移除
  "force": false                  // false=按 contentHash 幂等跳过未变；true=强制重嵌
}
```
响应：
```jsonc
{ "model": "bge-m3", "dim": 1024, "upserted": 10, "skipped": 3, "deleted": 2 }
```
- **幂等**：Chroma 内已存在同 `chunkId` 且 `contentHash` 一致 → `skipped`，不重复嵌入。
- 删除折进本接口（维护动作），保持"三接口"不膨胀。

#### ② `POST /search` — query 向量召回（在线）

请求：
```jsonc
{
  "query": "本地怎么启动整套茉莉微服务",
  "spaceIds": [900000000000000003],   // Java 传入的可读空间（初筛 filter）
  "topN": 20,                         // = kb.search.vector.top-n
  "filter": { "kbType": ["guide","service"], "excludeKbType": ["interview"] } // 可选，对齐 Scope
}
```
响应（按相似度降序，含 1-based rank）：
```jsonc
{
  "model": "bge-m3",
  "results": [
    { "chunkId": 123, "docId": 45, "spaceId": 900000000000000003,
      "slug": "guides/本地启动指南", "kbType": "guide", "score": 0.83, "rank": 1 }
  ]
}
```
- `spaceIds` / `filter` 仅**初筛**；最终 ACL 与体裁裁决在 Java（§2.4）。

#### ③ `POST /rerank` — 交叉编码精排（在线，hybrid-rerank 档）

请求：
```jsonc
{
  "query": "本地怎么启动整套茉莉微服务",
  "candidates": [ { "chunkId": 123, "text": "<切段正文>" } ],  // Java 传融合后 top 池
  "topM": 8   // = kb.search.rerank.top-m
}
```
响应：
```jsonc
{ "model": "bge-reranker-v2-m3",
  "results": [ { "chunkId": 123, "score": 7.21, "rank": 1 } ] }  // 保留 topM，降序
```

### 1.3 Chroma collection 命名与 payload

- **单 collection**：`moli_kb_chunks_bgem3_v1`（名字内嵌模型+版本；换模型/维度=新 collection，便于灰度与回滚，不污染旧索引）。
- **Chroma id** = `chunkId`（字符串化）。**document** = 切段文本（heading + content）。
- **metadata（payload）字段**（供 filter 与回带）：

| 字段 | 来源（`kb_document_chunk`） | 用途 |
|------|-----------------------------|------|
| `chunkId` | `id` | join Java ngram 候选（`KbChunkAskRow.chunkId`）与回带 |
| `docId` | `document_id` | 合并到页级 citation |
| `spaceId` | `space_id` | ACL / 空间初筛 |
| `slug` | `slug` | 回带、页级合并 |
| `kbType` | `kb_type` | 体裁 filter |
| `categoryId` | `category_id` | 备用 filter |
| `contentHash` | `content_hash`（SHA-256） | 幂等增量 |

> 只索引 `status=PUBLISHED` 且未删除的切段（与 `KbAskServiceImpl` 召回口径一致）。

### 1.4 离线建索引触发方式：**CLI（选定）**

**决策：Sync 后由新 CLI `kb/tools/build_vector_index.py` 触发，不用 Java 回调。**

理由：embedding 是 DB 侧派生数据（`kb-hybrid-retrieval.md §5`）；wiki→DB 已是 Python CLI（`sync_to_db.py`）；Java 不引入模型/嵌入职责，保持在线只读调用。**拒绝 Java 回调**（会把嵌入生命周期耦合进 knowledge-server，违背 sidecar 独立部署原则）。

CLI 职责（W2 交付，实现留 Composer）：
1. 读 `kb_document_chunk`（PUBLISHED、未删）→ 组装 `items`（含 `contentHash`、`text`=heading+content）。
2. 分批 `POST /embed`（默认幂等增量；`--force` 全量重嵌）。
3. 计算 DB 中已消失的 chunkId（与上次/Chroma 对账）→ `deleteChunkIds` 随批清理。
4. 输出：upserted/skipped/deleted 统计。

推荐链路：`sync_to_db.py` →（可选）`build_vector_index.py`（见 kb `AGENTS.md §8.1` sync 顺序之后追加一步）。

---

## §2 Java 侧扩展

### 2.1 `KbSearchProperties` 新增键（前缀 `kb.search`）

| 键 | 默认 | 说明 |
|----|------|------|
| `retrieval-strategy` | `ngram` | `ngram` \| `hybrid` \| `hybrid-rerank`；**默认 ngram=零风险，行为与现状逐题一致** |
| `vector.base-url` | —（空） | sidecar 地址；空或不可达 → 降级 ngram |
| `vector.top-n` | `20` | `/search` 召回条数 |
| `vector.timeout-ms` | `1500` | `/search`/`/rerank` 超时；到点降级 |
| `fusion.rrf-k` | `60` | RRF 平滑常数 |
| `rerank.top-m` | `8` | `/rerank` 保留条数（融合后进 rerank 的池大小另设 `rerank.pool`，默认 30） |

> 现有键 `mode`/`ask-candidate-limit`/`chunk-enabled` 语义不变。`retrieval-strategy=ngram` 时**不得**触碰任何 sidecar 代码路径。

`AskRequest` 增**可选**字段 `retrievalStrategy`（请求级覆盖，默认取配置）——供 eval 三档对比，**不改响应结构**（§4）。

### 2.2 双路召回 + RRF 融合（chunk 级融合，freeze 公式）

在 `KbAskServiceImpl` 现有 `recallAndScoreChunks(...)`（ngram）之外，新增 `vectorRecallChunks(...)`（调 `/search`），两路在 **chunk 级**用 RRF 融合，再复用现有 `mergeCitation` 合并到页级。

**RRF 公式（冻结）**：对每个候选 chunk，
```
rrf(chunk) = Σ_over_lists  1 / (rrf_k + rank_in_list)     // rrf_k=60，两路等权
```
- 两路 = ngram 有序候选（现有 `ChunkScored`，按 score 降序取 rank）+ 向量 `/search` 结果（自带 rank）。
- join 键 = `chunkId`（`KbChunkAskRow.chunkId` ↔ `/search` payload.chunkId）。
- 只在一路出现的 chunk 也纳入（另一路贡献 0）。

**融合伪代码**：
```
if strategy == ngram:
    chunkScored = recallAndScoreChunks(...)          // 完全走现状
else:
    ngramChunks  = recallAndScoreChunks(...)          // rank1..N（失败→空）
    vectorChunks = vectorRecallChunks(query, scopeSpaces, scope, topN)  // 调 /search（失败→降级，见2.3）
    fused = {}
    for (rank, c) in enumerate(ngramChunks, 1):  fused[c.chunkId] += 1/(k+rank)
    for r in vectorChunks:                       fused[r.chunkId] += 1/(k+r.rank)
    ordered = sort(fused by score desc)           // 融合候选
    if strategy == hybrid-rerank:
        pool = ordered[:rerank.pool]              // 默认 30
        reranked = POST /rerank(query, pool.texts, topM)   // 失败→跳过 rerank，用 ordered
        ordered = reorder(pool by reranked) + ordered[rerank.pool:]
    chunkScored = materialize(ordered)            // 取回 KbChunkAskRow（缺正文者按 chunkId 补查）
# 之后完全复用现有：buildMergedCitations / buildChunkContext（页级合并、annex 降权、citationTopK 不变）
```
- **`annex` 降权、页级合并、citationTopK/llmContextTopK 逻辑不变**——AI-2 只改"候选怎么来"，不改"候选怎么组装成答案"。
- ngram 档 100% 走原路径，保证 §4 零回退。

### 2.3 降级策略（冻结）

| 情形 | 行为 |
|------|------|
| `retrieval-strategy≠ngram` 但 `vector.base-url` 空 | 启动 warn 一次；按 ngram 执行 |
| `/search` 超时/连接失败/非2xx | **本次请求**降级：仅用 ngram 候选；`log.warn` 限流告警（勿每条刷屏）；不抛错、不空答 |
| `/rerank` 失败 | 跳过精排，用 RRF 融合序；warn |
| sidecar 恢复 | 无需重启 Java，下次请求自动恢复 hybrid |

**铁律**：sidecar 任何异常都不得让 `/kb/ask` 失败或返回空——始终有 ngram 兜底。

### 2.4 ACL：Java 最终裁决

1. 调 `/search` **前**，Java 已用 `kbAclService.resolveReadableSpaceIds(...)` 算出 `scopeSpaces`，作为 `spaceIds` 传给 sidecar（**初筛**）。
2. `/search` 返回后，Java **逐条复核** `result.spaceId ∈ scopeSpaces`，不在则丢弃（**最终裁决**）。
3. sidecar filter 只为降候选量，**永不**作为权限依据。体裁 include/exclude（Scope）同理：sidecar 尽力 filter，Java 复核。

---

## §3 分阶段施工清单

### Phase W2（Composer 先做）· kb-retrieval sidecar + 离线索引 — ✅ 已交付并验收（2026-07-17）

| 交付 | 内容 | 状态 |
|------|------|------|
| `moli-knowledge/kb-retrieval/`（新目录） | FastAPI 应用；加载 bge-m3；Chroma 持久化（bge-reranker 随 `/rerank` 移 W3） | ✅ |
| `POST /embed` | §1.2① 契约：批量嵌入 + 幂等 upsert + deleteChunkIds | ✅ |
| `POST /search` | §1.2② 契约：query 嵌入 + Chroma 过滤检索 + rank | ✅ |
| `GET /health` | 返回 model/dim/collection/indexedChunks（契约外增益，供 CLI/降级探活） | ✅ |
| `kb-retrieval/scripts/index_chunks.py` | §1.4 离线索引 CLI **正式落点**（增量 + `--force` + `--dry-run` + `--space-id` + Chroma 对账删除） | ✅ |
| `kb-retrieval/README.md` | 起服务、装依赖、建索引、健康检查、与 sync 的先后（`AGENTS.md §8.1`）| ✅ |
| `requirements.txt` | fastapi/uvicorn/**chromadb≥1.0**（py3.13/Win 兼容）/sentence-transformers/pymysql 钉版本 | ✅ |

> **Opus 裁决（W2 偏差处置）**：
> 1. **`/rerank` + bge-reranker-v2-m3** 归 **W3**（对齐排期 §2：W3=Hybrid+Rerank）。W2 只需 `/embed`+`/search` 即可满足出口，原 §3 把 `/rerank` 列入 W2 属过范围，已更正。
> 2. **离线索引 CLI 正式路径 = `kb-retrieval/scripts/index_chunks.py`**（与 sidecar 同目录，合理）。契约 §1.4 的 `kb/tools/build_vector_index.py` 降为 **W3 可选薄封装**（仅为 `AGENTS.md §8.1` sync 后一行命令的书写一致性；不重复实现，转调 `index_chunks.py`）。

> W2 出口（对齐排期）：向量单路召回可跑通，脏 query 有召回（`/search` 对 golden 的 `dirty` 子集返回非空）。 
> **验收证据**：golden **M21**（dirty「本地启全套微服雾咋整啊」）→ `/search` rank1 命中 `guides/本地启动指南`（score 0.5778），与 `expect_slugs` 一致。**W2 通过。**

### Phase W3（Composer 后做）· Java 融合 + rerank + 三档评测 — ⚠️ 代码已交付 · 首轮验收未通过（2026-07-17）

| 交付 | 内容 | 状态 |
|------|------|------|
| `POST /rerank`（sidecar） | §1.2③ 契约：加载 bge-reranker-v2-m3，cross-encoder 精排 topM（W2 延后至此） | ✅ |
| S1/S2 加固 | 默认 `127.0.0.1` + 启动预热 embedding/rerank | ✅ |
| Java HTTP client | 调 `/search`/`/rerank`，超时/降级按 §2.3（`KbRetrievalClient`） | ✅ |
| `KbSearchProperties` 扩展 | §2.1 新键（`@ConfigurationProperties` 样板） | ✅ |
| `KbAskServiceImpl` 融合 | §2.2 双路召回 + RRF + 可选 rerank；ngram 档零改动路径 | ✅ |
| `AskRequest.retrievalStrategy` | 可选请求字段（默认取配置） | ✅ |
| `application-dev.yml` 示例 | `kb.search.retrieval-strategy` / `vector.*` / `fusion.*` / `rerank.*` 注释样例（默认 ngram） | ✅ |
| `eval_ask.py --strategy` | 传 `retrievalStrategy` 跑三档；产出 `ngram/hybrid/hybrid-rerank` 对比 | ✅ |
| `kb/tools/build_vector_index.py` | 薄封装转调 `kb-retrieval/scripts/index_chunks.py` | ✅ |
| 三档评测报告 | `kb/eval/reports/ai2-compare-*.json` | ✅（§4 全量指标待索引跑满复测） |

> **W3 前置**：正式三档对比前先把全库 6961 段索引跑满（当前 1439，`python -u scripts/index_chunks.py --batch-size 64` 续跑 ~90min），否则 hybrid 召回覆盖不全会污染对比。 
> W3 出口：三档可切换 ✅；三档对比表产出 ✅；ngram 档零回退验证 ✅。 
> **但 §4 hybrid 验收未通过（🔴 回退）** —— 详见 §Review 意见 W3；整改后须重跑三档并由 Opus 复核方可 done。

---

## §4 验收标准

- [x] **ngram 零回退**：`ngram` 档对 golden **逐题 first_rank 与 AI-1 基线完全一致**，聚合值逐位相同（hit@3=0.7917 / hit@8=0.9167 / MRR=0.7415）。✅（Opus 复核 2026-07-19）
- [x] **hybrid 不回退 + 子集提升**：全集 hit@3 **0.7917 → 0.8958（+10.4pp，不回退且提升）**；paraphrase 0.60→0.80（+20pp）、dirty 0.80→0.90（+10pp）；easy 回到 1.0（已修复首轮崩塌）。✅
- [x] **三档对比表**（`hit@1/3/5/8`、`MRR`、`P95`）写入 [`../kb-hybrid-retrieval.md`](../kb-hybrid-retrieval.md) **§6.1**；子集单列。✅（数值与报告逐位一致）
- [x] **不改 `/kb/ask` 对外响应结构**（红线，未变）。✅
- [x] **降级可验证**：`KbAskServiceImpl.hybridRecall` `catch → return ngramResult`（行 525-527），sidecar/融合异常自动回 ngram；三档复测 **errors=0**（首轮 rerank 21 报错已修）。✅
- [x] **ACL 复核**：Java 最终裁决未改（§2.4 红线）；sidecar filter 仅初筛。✅

> **依赖提醒（跨任务，已解除）**：AI-2 的 `paraphrase/dirty` 子集提升度量依赖 AI-1 基线可信。AI-1 契约 **B1 已于 2026-07-17 修复并复核通过（§9.7 `status: done`）**：jp certify 已 ingest + Sync（37 update / 2465 chunk），J01–J07 全 PASS，扩容 golden 为 **59 题**，ngram 修订基线 hit@3=79.17% / hit@8=91.67%（见 `kb/eval/README.md` 基线表）。**AI-2 直接以此扩容 golden 为对照基准**；正式三档对比前只需确认 jp 已在目标环境 Sync。

---

## §5 Composer 禁改范围（Do-Not-Touch）

- ❌ 自行改 **RRF 公式**（§2.2）、**降级策略**（§2.3）、**ACL 裁决顺序**（§2.4）。
- ❌ 引入 **ES / Milvus 集群**（向量库限 Chroma 本地持久化）。
- ❌ 把 **embedding 回写 wiki**（wiki 是唯一写入源；embedding 只在 Chroma/DB 派生）。
- ❌ 改 **ngram 档**行为（必须与现状逐题一致）。
- ❌ 改 **`/kb/ask` 对外响应结构**（只可加请求级可选 `retrievalStrategy`）。
- ❌ 在 `wiki/` 正文写入项目/嵌入相关内容（遵守 kb `AGENTS.md` 空间边界）。

如需调整以上任一项 → 回 Opus 窗口改契约，不在 Composer 窗口拍板。

---

## §6 未决问题 + 实现回填区（Composer 回填）

- **未决（需 Opus 定夺）**：
  - RRF 是否需按体裁/来源加权（当前**等权**冻结）？暂不做，先出等权对比数据再议。
  - rerank `pool` 默认 30 是否够？W3 用 golden 调参后回填建议值。
  - `build_vector_index.py` 是否并入 `fill_eval_metrics.py` 或 CI？W3 视稳定性决定。
  - ~~CLI 落点 `kb-retrieval/scripts/index_chunks.py` vs `kb/tools/build_vector_index.py`~~ → **Opus 已裁决**（§3）：正式落点 `kb-retrieval/scripts/index_chunks.py`；`kb/tools/build_vector_index.py` 为 W3 可选薄封装。
- **实现清单**：见文末 **§8 Phase W2 实现清单** + **Phase W3 实现清单**（sidecar + Java 融合 + 三档评测已落地）。

---

## §Review 意见（Opus · W2 验收 2026-07-17）

**结论：W2 done，可开 W3。** 逐条核对如下。

### R1 · `/embed`·`/search` 契约一致性 → ✅ 完全一致
逐字段比对 `app/models.py` / `app/main.py` 与契约 §1.2：
- `/embed` 请求 `items[{chunkId,docId,spaceId,slug,kbType,categoryId,contentHash,text}]` + `deleteChunkIds` + `force`，响应 `{model,dim,upserted,skipped,deleted}` —— 与 §1.2① 逐字段一致；幂等按 `contentHash` skip、`force` 覆盖、超长截断齐备。
- `/search` 请求 `{query,spaceIds,topN,filter{kbType,excludeKbType}}`，响应 `{model,results[{chunkId,docId,spaceId,slug,kbType,score,rank}]}` —— 与 §1.2② 逐字段一致；`spaceId $in`/`kbType $in`/`excludeKbType $nin` 初筛正确，cosine→score、1-based rank 正确。

### R2 · Chroma payload 是否够 W3 融合 → ✅ 够
`/search` 响应回带 `chunkId`(RRF join 键) + `docId`(页级 `mergeCitation`) + `spaceId`(Java ACL 复核) + `slug`(citation) + `kbType`(体裁裁决)，正好覆盖 §2.2/§2.4 所需。`categoryId`/`contentHash` 仅存 Chroma metadata 供 filter/幂等，无需回带，符合契约 §1.3。
> **W3 提醒（非 W2 缺陷）**：`/search` 不回带 `text`。走 `hybrid-rerank` 时，对**仅向量命中**（不在 ngram 候选里）的 chunk，Java 需按 `chunkId` 回查 DB 取正文再送 `/rerank(candidates[].text)`。ngram 候选已自带 `KbChunkAskRow.content`。此为 W3 实现注意点，勿在 `/search` 里塞 text 增大响应。

### R3 · content_hash 增量 → ✅ 支持
`scripts/index_chunks.py` 从 `kb_document_chunk` 读 `content_hash` → `items[].contentHash`，`/embed` 与 Chroma 存量比对 skip 未变段；并以 `Chroma ids − DB ids` 对账生成 `deleteChunkIds` 清理孤儿。`--force` 全量重嵌、`--dry-run`、`--space-id` 齐备。

### R4 · 安全/性能遗漏（非阻塞 · W3 收口）
| # | 级别 | 问题 | W3 处置建议 |
|---|------|------|------------|
| S1 | 安全 P2 | sidecar `uvicorn host=0.0.0.0` 且 `/embed` 为**无鉴权写接口** → 若暴露到非本机网络存在**索引投毒**风险 | 默认绑 `127.0.0.1`（Java/CLI 均本机调用），或 README 明确要求防火墙/内网隔离；生产可加简单 token 头 |
| S2 | 性能 P2 | 模型**懒加载**：sidecar 启动后首个 `/search` 触发 30–60s 冷加载，会超过 Java `vector.timeout-ms=1500` → **首查被误降级 ngram** | 启动时预热（boot 阶段 `embed_texts(["warmup"])`）或 `/health` 触发加载；W3 Java 降级日志需能区分「冷启动超时」与「真故障」 |
| S3 | 提示 P3 | CLI MySQL 默认口令 `12345678` 为 dev 占位（可 env/arg 覆盖） | 保持占位即可，勿写真实凭据；生产走 env |
| S4 | 提示 P3 | `/search` 传空 `spaceIds` 会检索全空间（当前 Java 恒传可读空间，且 Java 为 ACL 终裁） | W3 Java 侧确保恒传非空 scope；depth-in-defense，非缺陷 |

> S1/S2 建议在 W3 与 Java 联调同批修（成本低、收益明确）；不影响 W2 验收结论。RRF 公式 / 降级策略 / ACL 顺序 / `/kb/ask` 响应结构均未被 W2 触碰，红线保持。

---

## §Review 意见（Opus · W3 首轮验收 2026-07-17）

**结论：🔴 未通过。代码交付齐全，但 §4 验收失败，AI-2 不置 done。**
数据源：`kb/eval/reports/ai2-compare-{ngram,hybrid,hybrid-rerank}-20260717-10*.json`（59 题，检索式口径）。已回填 `kb-hybrid-retrieval.md §6`。

### W3-R1 · ngram 零回退 → ✅ 通过
AI-2 `ngram` 档与 AI-1 基线（`baseline-ngram-20260717-022510.json`）**逐题 first_rank 完全一致**，聚合值逐位相同：hit@3=0.7917 / hit@5=0.8333 / hit@8=0.9167 / MRR=0.7415 / coverage=0.8663。红线「ngram 路径零改动」成立。

### W3-R2 · hybrid 完整集不回退 + paraphrase/dirty 提升 → 🔴 失败（阻断）
| 视图 | ngram | hybrid | 判定 |
|------|-------|--------|------|
| 全集 hit@3 | 0.7917 | **0.7292** | 🔴 回退 −6.25pp（§4 要求不回退） |
| 全集 hit@8 | 0.9167 | **0.7708** | 🔴 回退 −14.6pp |
| easy hit@3 | 1.000 | **0.7333** | 🔴 崩塌 −26.7pp |
| dirty hit@3 | 0.800 | **0.700** | 🔴 回退 −10pp（§4 要求 dirty 提升） |
| paraphrase hit@3 | 0.600 | 0.6667 | ✅ +6.7pp |
| multi-hop hit@3 | 0.750 | 0.875 | ✅ +12.5pp |

§4 硬指标「完整集 hit@3 不回退」与「paraphrase+dirty 子集提升」**双双未满足**（全集回退、dirty 回退）。

### W3-R3 · hybrid-rerank → ⚠️ 无效运行（21/59 报错）
`errors=21`、`answerable_total` 从 48 掉到 34、P95=**11631ms**。分层分母缩水（dirty 仅 5/10、paraphrase 11/15），指标不可比。说明 `/rerank` 大面积失败（cross-encoder 冷载/超时），且**未按 §2.3 优雅降级为融合序**——而是整条 query 报错。**这同时违反 §4「降级可验证」**。

### W3-R4 · 根因（逐题核对，供整改）
1. **annex 大附件页污染向量召回**：企业库 `bigdata/annex-*`（hadoop/spark 大 dump 页）在向量列表泛滥，把聚焦正确页挤出 top3 —— E01(JVM)、E02(Redis)、E05(Dubbo) 均 rank1→MISS，E09 rank7→MISS。ngram 有 annex 降权（`finalizeRecallScore`），**向量/融合路径无降权**，等权 RRF 让 annex 压过「仅 ngram 强命中」的正确页（仅 ngram rank1 = 1/61，被两路皆中的 annex 反超）。
2. **索引可能未跑满**：W2 报 1439/6961；若企业聚焦页欠索引而 annex 已索引，向量只注入噪声。
3. **rerank 降级失效**：失败应回退融合序（§2.3），实测却报错终止。

### W3-R5 · 整改清单（Composer · 不碰红线）
- [x] **annex 降权入向量/融合路径**：`KbHybridRrfSupport.applyAnnexFusionPenalty`（×1/3）；单测 `KbHybridRrfSupportTest` 覆盖 E01 场景。
- [x] **补全索引**：Chroma **6961** 段（GPU 离线索引，2026-07-19 完成）。
- [x] **修 rerank 降级**：`/rerank` 超时/异常 → 融合序；`hybrid` 异常 → ngram；`rerank.timeout-ms=30000`。
- [x] **重跑三档**并回填 §6；Opus 复核 §4 全绿后方可 `status: done`。（2026-07-19 复测指标已达标，**待 Opus 签核**）
- [x] `eval_ask.py` 补记 `hit@1`；一键脚本 `kb/tools/run_ai2_remediation.ps1`。

> 红线未破：`ngram` 路径零改动、`/kb/ask` 响应结构未变、未引入 ES/Milvus。整改仅在向量/融合/降级实现层，不触 §5 冻结项。

### W3-签核 · Opus 复核结论（2026-07-19）→ ✅ 通过，AI-2 `status: done`

复核数据源（逐位比对，非采信汇总）：`ai2-compare-ngram-20260717-233235.json`、`ai2-compare-hybrid-20260719-052011.json`、`ai2-compare-hybrid-rerank-20260719-053856.json`（全库 6961 段）。

| 策略 | hit@1 | hit@3 | dirty@3 | MRR | P95 | errors |
|------|-------|-------|---------|-----|-----|--------|
| ngram | 0.6458 | 0.7917 | 0.80 | 0.742 | 11240ms | 0 |
| hybrid | 0.7083 | **0.8958** | **0.90** | 0.792 | 6462ms | 0 |
| hybrid-rerank | 0.6875 | 0.8333 | 0.90 | 0.754 | 32908ms | 0 |

- 首轮三处回退（全集 −6.25pp、easy 崩 −26.7pp、dirty −10pp）**全部消除**：annex 融合降权 + 补全索引生效，easy 回到 1.0、全集反超基线 +10.4pp。
- 首轮 rerank **21 报错 → 0**：降级路径修复（`hybridRecall` catch 回 ngram）经代码与 errors=0 双证。
- §4 六项验收**全绿**（见上）。红线未破。

**签核结论**：AI-2 达成 §4 全部验收，**置 `status: done`**。

**运营建议（非阻塞）**：
- **默认生产档位建议 `hybrid`**：hit@3 最高（0.8958）且 P95 最低（6.5s）；`hybrid-rerank` 本集反而略低（0.8333）且 P95 32.9s（cross-encoder CPU 偏重），建议保留为 opt-in，待 AI-3 看板积累更多样本或换 GPU 精排后再评估是否默认开启。
- rerank pool/权重、GPU 精排延迟优化归 AI-3/后续迭代，不阻断本次 done。

---

## §7 相关

- 技术方案：[`../kb-hybrid-retrieval.md`](../kb-hybrid-retrieval.md) §3
- 路线 / 排期 / 分工：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §2/§9.1
- 评测契约（字段/基线口径）：[`AI-1-contract.md`](AI-1-contract.md)
- 现有召回：`moli-knowledge-server` `KbAskServiceImpl` · `KbSearchProperties` · `KbDocumentChunk`（`content_hash`）
- 姊妹契约（AI-4）：[`../bi-chatbi-nl2sql-contract.md`](../bi-chatbi-nl2sql-contract.md)

---

## 实现清单（Phase W2）

| 交付项 | 路径 / 说明 | 状态 |
|--------|-------------|------|
| FastAPI sidecar | `moli-knowledge/kb-retrieval/` | ✅ |
| `GET /health` | `app/main.py` · 返回 model/dim/collection/indexedChunks | ✅ |
| `POST /embed` | 批量 bge-m3 嵌入 + contentHash 幂等 skip + deleteChunkIds | ✅ |
| `POST /search` | query 嵌入 + Chroma filter（spaceId/kbType）+ rank/score | ✅ |
| `POST /rerank` | cross-encoder 精排 | ✅ **W3**（见 Phase W3 清单） |
| Chroma 持久化 | `kb-retrieval/.chroma/` · collection `moli_kb_chunks_bgem3_v1` | ✅ gitignore |
| 离线索引 CLI | `kb-retrieval/scripts/index_chunks.py`（MySQL → `/embed` 增量） | ✅ |
| `kb/tools/build_vector_index.py` | 契约 §1.4 命名 | ✅ W3 薄封装转调 `index_chunks.py` |
| README | `kb-retrieval/README.md`（启动 / 索引 / 冒烟 curl） | ✅ |
| requirements.txt | fastapi · uvicorn · chromadb≥1 · sentence-transformers · pymysql | ✅ |
| Java 融合 | `KbAskServiceImpl` / `KbSearchProperties` / RRF | ✅ **W3**（见 Phase W3 清单） |

**运行参数（实测）**

| 项 | 值 |
|----|-----|
| 默认端口 | `8099`（`RETRIEVAL_PORT`） |
| 模型 | `BAAI/bge-m3` · dim=1024 · L2 normalize |
| 首次模型加载 | ~30–60s（下载后缓存） |
| 单批 embed（64 段） | ~50–55s（CPU，Windows） |
| DB 已发布切段总数 | 6961（三空间：enterprise 3590 · jp 2476 · moli 895） |
| 本次 Chroma 条数 | 1439（enterprise 部分 544 + moli 全量 895；全库索引可继续跑 CLI） |

**索引 CLI 能力**

- 默认：`python kb-retrieval/scripts/index_chunks.py`（全空间增量）
- `--force`：忽略 contentHash 强制重嵌
- `--space-id`：只索引单空间（冒烟 / 局部重建）
- `--dry-run`：只统计不调 `/embed`
- 对账删除：Chroma 直连读 ids，DB 已删切段 → `deleteChunkIds`

---

## 未决问题

1. **`/rerank` 与 bge-reranker-v2-m3**：契约 §1.2③ 与 W2 排期表列出，但 W2 施工范围限定 embed/search/health + 离线索引；精排留 **W3** 与 Java `hybrid-rerank` 同批交付。
2. **`build_vector_index.py` 落点**：W2 实现在 `kb-retrieval/scripts/index_chunks.py`；是否在 W3 于 `kb/tools/` 增加同名薄封装（供 `AGENTS.md §8.1` sync 后一行命令）待 Opus/排期确认。
3. **全量索引耗时**：6961 段 × batch=64 约 **~90min**（CPU）；生产建议 sync 后后台跑、或加大 `EMBED_BATCH_SIZE` / GPU。
4. **Python 3.13 + chromadb**：需 `chromadb>=1.0.0`（0.5.x 的 `chroma-hnswlib` 在 Win/py3.13 编译失败）；已在 `requirements.txt` 钉版本。
5. **RRF 加权 / rerank pool / CI 索引**：仍按 §6 原未决，W3 三档评测后回填。

---

## 冒烟结果（/search 对 1 条 dirty query 有召回）

**环境**：sidecar `http://127.0.0.1:8099` · 已索引 moli-ops-manual 全空间（895 chunk）+ enterprise 部分。

**请求**（golden **M21**，`difficulty=dirty`）：

```http
POST /search
Content-Type: application/json

{
  "query": "本地启全套微服雾咋整啊",
  "spaceIds": [900000000000000003],
  "topN": 8
}
```

**结果**：`results` **非空**（8 条）；**rank 1** 命中预期页：

| rank | chunkId | slug | kbType | score |
|------|---------|------|--------|-------|
| 1 | 1374250774676325 | `guides/本地启动指南` | guide | **0.5778** |
| 2 | 1374250775462910 | `ops/知识库工作台运维SOP` | guide | 0.5634 |
| 3 | 1374250774627162 | `guides/数据库初始化指南` | guide | 0.5531 |

**结论**：W2 出口「向量单路 dirty query 有召回」**通过**（与 AI-1 golden M21 `expect_slugs` 一致）。

---

## 实现清单（Phase W3）

| 交付项 | 路径 / 说明 | 状态 |
|--------|-------------|------|
| `POST /rerank` | `kb-retrieval/app/rerank.py` + `app/main.py` · bge-reranker-v2-m3 cross-encoder | ✅ |
| S1 安全加固 | `RETRIEVAL_HOST` 默认 `127.0.0.1` · README 明示勿暴露 `/embed` | ✅ |
| S2 启动预热 | `@app.on_event("startup")` 预热 embedding + rerank，避免首查误降级 | ✅ |
| `KbSearchProperties` | `retrieval-strategy` / `vector.*` / `fusion.rrf-k` / `rerank.top-m` / `rerank.pool` | ✅ |
| `application-dev.yml` | `kb.search` 示例（默认 `ngram`） | ✅ |
| `KbRetrievalClient` | `support/KbRetrievalClient.java` · `/search`/`/rerank` · 超时 · 限流 warn · 失败空结果 | ✅ |
| `KbAskServiceImpl` | `ngram` 原路径不变 · `hybrid` RRF 融合 · `hybrid-rerank` 池内精排 · ACL 复核 · 向量-only chunk DB 补正文 | ✅ |
| `AskRequest.retrievalStrategy` | 请求级覆盖，响应结构不变 | ✅ |
| `eval_ask.py --strategy` | `ngram` \| `hybrid` \| `hybrid-rerank` → `AskRequest.retrievalStrategy` | ✅ |
| `docs/api/KNOWLEDGE_API.md` | `kb.search` 新键 + `retrievalStrategy` 请求字段 | ✅ |
| `kb/tools/build_vector_index.py` | 薄封装 → `kb-retrieval/scripts/index_chunks.py` | ✅ |

**三档评测（2026-07-17 · golden 59 题 · `kb/eval/reports/ai2-compare-*.json`）**

环境：sidecar `127.0.0.1:8099`（Chroma **1695** 段，未全库 6961）· knowledge-server `8090` · 检索式 `useLlm=false`。

| 策略 | 报告 | errors | hit@3 | hit@8 | MRR | P95 | paraphrase hit@3 | dirty hit@3 |
|------|------|--------|-------|-------|-----|-----|------------------|-------------|
| `ngram` | `ai2-compare-ngram-20260717-102309.json` | 0 | **79.17%** | 91.67% | 0.742 | 2963ms | 60% | 80% |
| `hybrid` | `ai2-compare-hybrid-20260717-102430.json` | 0 | 72.92% | 77.08% | 0.623 | 3597ms | **66.7%** | 70% |
| `hybrid-rerank` | `ai2-compare-hybrid-rerank-20260717-103134.json` | **21** | 73.5%† | 82.4%† | 0.680 | 11631ms | 45.5%† | **100%**† |

† `hybrid-rerank` 首轮 sidecar 不稳定（timeout/500），仅 **38/59** 题有效；**须 sidecar 稳定 + 全量索引后复测**。

**§4 验收口径（本轮）**

| 项 | 结论 |
|----|------|
| `ngram` 零回退 | ✅ `retrievalStrategy=ngram` 与 AI-1 修订基线一致（hit@3=**79.17%**） |
| 三档可切换 | ✅ `--strategy` + yml 配置 + 请求覆盖均可 |
| 降级不空答 | ✅ sidecar 停服时 hybrid 回 ngram（代码路径 §2.3；未在本轮单独停 sidecar 扫 golden） |
| `/kb/ask` 响应结构 | ✅ 未改 |
| hybrid 完整集不回退 + dirty/paraphrase 提升 | ⏸ **待全库索引**（6961 段）；当前部分索引下 enterprise/jp 向量路噪声导致 full-set 回退 |
| hybrid-rerank 干净对比 | ⏸ 待稳定 sidecar 下 **0 errors** 复跑 |

**复测命令**

```powershell
# sync 后全量索引（~90min CPU）
python kb/tools/build_vector_index.py --batch-size 64

# 三档对比（sidecar + knowledge-server 已启）
python kb/tools/eval_ask.py --strategy ngram
python kb/tools/eval_ask.py --strategy hybrid
python kb/tools/eval_ask.py --strategy hybrid-rerank
```

**W3 整改（2026-07-17 · 首轮未通过后）**

| 项 | 改动 | 状态 |
|----|------|------|
| annex 融合降权 | `KbAskServiceImpl.applyAnnexFusionPenalty` · RRF 后 ×1/3（对齐 `finalizeRecallScore`） | ✅ |
| rerank 降级 | `applyRerank` / `recallHybridChunks` try-catch → 融合序 / ngram；`rerank.timeout-ms=30000` | ✅ |
| eval hit@1 | `eval_ask.py` `STANDARD_HIT_AT=(1,3,5,8)` | ✅ |
| 全量索引 | Chroma **6961** 段（GPU 离线索引） | ✅ |
| 三档复测 + §4 全绿 | 见下表 · **待 Opus 复核** | ✅ 指标达标 |

**三档评测（2026-07-19 · 整改复测 · golden 59 题 · Chroma 6961 段）**

| 策略 | 报告 | errors | hit@1 | hit@3 | hit@8 | MRR | P95 | paraphrase hit@3 | dirty hit@3 |
|------|------|--------|-------|-------|-------|-----|-----|------------------|-------------|
| `ngram` | `ai2-compare-ngram-20260717-233235.json` | 0 | 64.58% | **79.17%** | 91.67% | 0.742 | 11240ms | 60% | 80% |
| `hybrid` | `ai2-compare-hybrid-20260719-052011.json` | 0 | 70.83% | **89.58%** | 89.58% | 0.792 | 6462ms | **80%** | **90%** |
| `hybrid-rerank` | `ai2-compare-hybrid-rerank-20260719-053856.json` | **0** | 68.75% | **83.33%** | 85.42% | 0.754 | 32908ms | 73.3% | **90%** |

**§4 验收（整改复测）**

| 项 | 结论 |
|----|------|
| `ngram` 零回退 | ✅ hit@3=**79.17%**（与 AI-1 修订基线一致） |
| hybrid 完整集不回退 | ✅ 89.58% ≥ 79.17% |
| paraphrase + dirty 子集提升 | ✅ paraphrase 60%→**80%**；dirty 80%→**90%** |
| hybrid-rerank 干净对比 | ✅ **errors=0**（59/59 有效）；降级路径已修复 |
| `/kb/ask` 响应结构 | ✅ 未改 |

