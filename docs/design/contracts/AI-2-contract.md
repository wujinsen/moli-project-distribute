# AI-2 向量检索 + Hybrid + Rerank · 施工契约（Opus 产出，Composer 施工）

> **角色**：Opus（架构与安全负责人）产出，Composer 施工的**唯一契约**。
> **任务**：AI-2 向量检索 + Hybrid Search + Rerank，第 1 波 W2–W3。
> **状态**：contract · 2026-07-17 · **Phase W2 已交付**（W3 Java 融合待做）
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

### Phase W2（Composer 先做）· kb-retrieval sidecar + 离线索引

| 交付 | 内容 |
|------|------|
| `moli-knowledge/kb-retrieval/`（新目录） | FastAPI 应用；加载 bge-m3 + bge-reranker-v2-m3；Chroma 持久化 |
| `POST /embed` | §1.2① 契约：批量嵌入 + 幂等 upsert + deleteChunkIds |
| `POST /search` | §1.2② 契约：query 嵌入 + Chroma 过滤检索 + rank |
| `POST /rerank` | §1.2③ 契约：cross-encoder 精排 topM |
| `kb/tools/build_vector_index.py` | §1.4 离线索引 CLI（增量 + `--force` + 对账删除） |
| `kb-retrieval/README.md` | 起服务、装依赖、建索引、健康检查、与 sync 的先后（`AGENTS.md §8.1`）|
| `requirements.txt` | 固定 fastapi/uvicorn/chromadb/FlagEmbedding 等版本 |

> W2 出口（对齐排期）：向量单路召回可跑通，脏 query 有召回（`/search` 对 golden 的 `dirty` 子集返回非空）。

### Phase W3（Composer 后做）· Java 融合 + 三档评测

| 交付 | 内容 |
|------|------|
| Java HTTP client | 调 `/search`/`/rerank`，超时/降级按 §2.3（可用现有 HTTP 工具，勿引重依赖） |
| `KbSearchProperties` 扩展 | §2.1 新键（`@ConfigurationProperties` 样板） |
| `KbAskServiceImpl` 融合 | §2.2 双路召回 + RRF + 可选 rerank；ngram 档零改动路径 |
| `AskRequest.retrievalStrategy` | 可选请求字段（默认取配置） |
| `application-dev.yml` 示例 | `kb.search.retrieval-strategy` / `vector.*` / `fusion.*` / `rerank.*` 注释样例（默认 ngram） |
| `eval_ask.py --strategy` | 传 `retrievalStrategy` 跑三档；产出 `ngram/hybrid/hybrid-rerank` 对比（复用 AI-1 `by_difficulty`/分母隔离） |

> W3 出口：三档可切换；三档对比表产出；ngram 档零回退验证。

---

## §4 验收标准

- [ ] **ngram 零回退**：`retrieval-strategy=ngram` 时对 golden **逐题**结果与 AI-1 ngram 基线一致（citations、rank 不变）。
- [ ] **hybrid 不回退 + 子集提升**：同一扩容 golden 上，`hybrid` 相对 `ngram` **完整集 hit@3 不回退**；`paraphrase`+`dirty` 子集 hit@3 **显著提升**（用 AI-1 `by_difficulty` 对比）。
- [ ] **三档对比表**（`hit@1/3/5`、`MRR`、`P95 延迟`）写入 [`../kb-hybrid-retrieval.md`](../kb-hybrid-retrieval.md) **§6**；paraphrase/dirty/multi-hop 子集单列。
- [ ] **不改 `/kb/ask` 对外响应结构**（`answer`/`citations`/`mode`/`scope` 字段不变）。
- [ ] **降级可验证**：停 sidecar，hybrid 档自动回 ngram，问答不失败。
- [ ] **ACL 复核**：跨空间/越权 chunk 不出现在 citations（Java 复核生效）。

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
- **实现清单**：见文末 **§8 Phase W2 实现清单**（sidecar 已落地；Java client / 三档评测留 W3）。

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
| `POST /rerank` | cross-encoder 精排 | ⏸ **W3**（W2 用户范围未含） |
| Chroma 持久化 | `kb-retrieval/.chroma/` · collection `moli_kb_chunks_bgem3_v1` | ✅ gitignore |
| 离线索引 CLI | `kb-retrieval/scripts/index_chunks.py`（MySQL → `/embed` 增量） | ✅ |
| `kb/tools/build_vector_index.py` | 契约 §1.4 命名 | ⏸ W3 可薄封装指向 `index_chunks.py` |
| README | `kb-retrieval/README.md`（启动 / 索引 / 冒烟 curl） | ✅ |
| requirements.txt | fastapi · uvicorn · chromadb≥1 · sentence-transformers · pymysql | ✅ |
| Java 融合 | `KbAskServiceImpl` / `KbSearchProperties` / RRF | ❌ **W3**（本阶段未改） |

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

