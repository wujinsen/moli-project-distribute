# AI-10 Multi-Agent DeepResearch · 施工契约（Opus 产出，Composer 施工）

> **角色**：Opus（多 Agent 编排 / 大纲·审校·回写质量负责人）产出，Composer 施工的**唯一契约**。
> **任务**：主题调研 → Planner 大纲 → Retriever 分节多轮检索 → Writer 带 `[[slug]]` 报告 → Reviewer 陈述↔引用对齐与缺口回补 → 可选经 **Ingest commit** 回写 `wiki-moli/develop/outputs/`。
> **状态**：✅ **done** · 2026-07-20 Opus §6 签核 · 四 Agent + Ingest 回写 · pytest 13 / Java 3 绿 · M4 收官
> **主导**：🟣 **Opus 主导** —— **Opus 拍板「四 Agent 编排边界、有界/降级/ACL/回写不变量、Prompt 语义、接口/trace、验收口径」（§1–§6）**；Composer 铺量（sidecar 脚手架、Java 薄壳/SSE、Ingest 接线、单测、冒烟、drawio、API 文档）。
> **上游**：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4 第 4 波 AI-10 · §6 模块归属 · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §5 W16 · §6 依赖 AI-2/5 · §9.1 AI-10 · [`../product/ai-capability-prd.md`](../product/ai-capability-prd.md) §4 AI-10 · §5 Ingest · §7.3 · [`AI-2-contract.md`](AI-2-contract.md)（hybrid）· [`AI-5-contract.md`](AI-5-contract.md)（graph）· [`AI-6-contract.md`](AI-6-contract.md)（`kb_search`/`kb_ask` ↔ REST）· [`AI-7-contract.md`](AI-7-contract.md)（`/kb/ask/agentic`）· [`AI-9-contract.md`](AI-9-contract.md)（Guardrails，Writer 出口可挂）
> **现有落地（复用，勿重造）**：`POST /kb/ask`（`useLlm=false` 检索式 / `true` 生成式）· `POST /kb/ask/agentic` · AI-2 `retrievalStrategy=hybrid` · AI-5 `graphExpand` · `/kb/ingest/*`（§9 Ingest 工作台）· `wiki-moli/develop/outputs/` output 页格式（AGENTS §2）· AI-6 `mcp/` 仅参考 REST 映射，**本波不依赖 MCP**

---

## 0. 契约边界（读我）

**本契约定义**：DeepResearch 范围与四 Agent 编排、运行形态、检索通道、产物与回写路径、不变量、Prompt 语义、接口/DTO/配置/trace、分 Phase 清单、验收与禁改范围。

**不在本契约内（交给 Composer）**：FastAPI/LangGraph（或等价）脚手架、各 Agent 实现细节、Java Controller/SSE 样板、`kb_research_run` DDL/Entity/Mapper、Ingest job 接线代码、单测/冒烟脚本、drawio 源文件、`docs/api` 字段表样板。

**红线**：Composer **不得**改 §1 编排语义、§2 不变量、§3 Prompt **语义**（措辞微调须回 Opus）；**不得**在 sidecar 内重实现 hybrid/graph/Agentic；**不得**直写 `kb_document` 或绕过 Ingest 落盘 wiki；**不得**改 `/kb/ask`、`/kb/ask/agentic` 既有行为。发现歧义 → 回 Opus。

---

## 1. DeepResearch 范围（Opus 冻结）

### 1.1 定位：主题级调研编排，**独立于**单轮 Ask / Agentic

| 能力 | 入口 | 产物粒度 |
|------|------|----------|
| `/kb/ask` | 单轮问答 | 一段答案 + citations |
| `/kb/ask/agentic` | 多跳问答编排 | 一段答案 + 自检 |
| **DeepResearch（本波）** | **`POST /kb/research`**（+ SSE） | **多节 Markdown 报告** + citations 清单 + 可选 Ingest 回写 |

DeepResearch **不是** Agentic 的加长版：Planner 产出大纲与子课题；Writer 按节撰写；Reviewer 做整篇对齐与回补决策。Retriever **只调既有 REST**，不另造召回栈。

### 1.2 四 Agent 编排（冻结）

```
topic (+ spaceIds, options)
        │
        ▼
① Planner     主题 → 大纲 sections[] + 每节 subTopics[] / retrieveQueries[]
        │
        ▼
② Retriever   按节多轮检索（复用 hybrid / graph / 可选 agentic）
              → 每节 evidence pool（slug + 片段 + score）
        │
        ▼
③ Writer      分节撰写 Markdown；结论后 [[slug]]；汇总 citations[]
        │
        ▼
④ Reviewer    陈述 ↔ 引用对齐；coverage / unsupported / gaps
              ├─ gaps 且未超预算 → 指定节回补 Retriever → 再 Writer 该节 → Reviewer
              └─ 否则 finalize（全文或降级「大纲+引用摘要」）
        │
        ▼
可选 writeback → Ingest job → draft → lint → commit → wiki-moli/develop/outputs/{slug}.md
```

| Agent | 职责 | 输入 | 输出（语义冻结） |
|-------|------|------|------------------|
| **Planner** | 主题拆大纲与可检索子课题 | `topic`、可选 `maxSections` | `outline{ title, sections[{id, heading, subTopics[], retrieveQueries[]}] }` |
| **Retriever** | 按节多轮检索，合并证据池 | 节 `retrieveQueries`、ACL 范围、策略开关 | `sectionEvidence[{ sectionId, hits[{slug, snippet, score, docId?}] }]` |
| **Writer** | 分节撰写、全文组装 | outline + evidence + 引用约束 | `reportMd`（含 frontmatter 草案）+ `citations[]` |
| **Reviewer** | 陈述↔引用对齐；缺口回补决策 | `reportMd` + 全量 evidence | `{ coverage, unsupported[], gaps[{sectionId, queries[]}], accept }`；**不静默删句** |

**回补（冻结）**：仅当 `accept=false` 且 `gaps` 非空、且未触达 `maxRetrieveRounds` / `latencyBudgetMs` 时，对 `gaps` 指向的节再跑 Retriever→Writer（该节）→Reviewer；**禁止**无界循环。

### 1.3 运行形态（Opus 二选一 · **已定**）

> **选定：Python sidecar + Java 薄壳（SSE）**。**否决** CLI-only 首版（CLI 仅作本地冒烟入口，非交付形态）。

| 层 | 路径 / 职责 |
|----|-------------|
| **Python sidecar** | `moli-knowledge/deep-research/`（建议 FastAPI；编排可用 LangGraph 或显式状态机）—— 跑四 Agent、调 Knowledge REST、产出报告 JSON/MD |
| **Java 薄壳** | `moli-knowledge-server`：`POST /kb/research`（同步或 start）+ `GET /kb/research/{runId}/stream`（SSE 进度）—— 鉴权、透传 token、落 `kb_research_run`、可选触发 Ingest 回写 |
| **CLI（可选冒烟）** | `python -m deep_research.cli --topic ...` **直连 sidecar 或 REST**，与生产同编排；**不**替代 Java 端点交付 |

对齐 roadmap §6：「AI-10 → `moli-knowledge`（Python）+ 现有 Ingest」；形态对标 AI-4（Java 壳 + Python sidecar），**不新建 Maven 模块**。

### 1.4 检索通道（冻结 · 禁止 sidecar 重实现 hybrid）

Retriever **只**通过 HTTP 调用 knowledge-server（经网关或直连 8090，与 AI-6 同鉴权）：

| 用途 | REST | 约定 |
|------|------|------|
| **默认检索** | `POST /kb/ask` | **`useLlm=false`**（检索式 = AI-6 `kb_search` 语义）；`question`←`retrieveQuery`；透传 `spaceId`/`spaceIds`、`topK`、`retrievalStrategy`（默认 `hybrid`）、`graphExpand` |
| **可选 Agentic** | `POST /kb/ask/agentic` | 仅当请求 `agentic=true` 且配置允许；用于难节多跳；仍须 ACL 透传 |
| **禁止** | sidecar 内 Chroma/BM25/RRF/沿边扩跳 | 一律复用 AI-2/5；改进检索只改服务端，DeepResearch 自动受益 |

Writer / Planner / Reviewer 的 LLM 调用：sidecar 内用平台 LLM 配置（与 `kb.llm` / AI-8 路由对齐的 HTTP，或由 Java 代理 `KbLlmClient`——**Phase A 允许 sidecar 直调 OpenAI 兼容 API，密钥走环境变量；Phase B 优先经 Java/AI-8**，避免双配置漂移）。契约不强制 Phase A 走 Java LLM 代理。

### 1.5 产物与回写路径（冻结）

**运行产物（每次 run）**：

1. **Markdown 报告**（正文 + YAML frontmatter 草案）
2. **citations 清单**（全局去重：`slug`、标题、可选 `docId`、出现节 id）
3. **trace 摘要**（大纲、每节 hits slug、coverage、是否降级、耗时）

**output 页格式**（对齐 AGENTS §2 + 既有样例如 `wiki-moli/develop/outputs/茉莉微服务全链路一张图.md`）：

```yaml
---
title: {报告标题}
slug: {短 slug，无路径前缀}
type: output
status: active
tags: [deep-research, ...]
query: {原始 topic}
source_pages: [slug1, slug2, ...]   # 必填 · 与正文 [[slug]] 一致
sources: []                         # 可选 · 工程路径或 raw；DeepResearch 可空或填检索来源说明
related: [...]
created: YYYY-MM-DD
updated: YYYY-MM-DD
---
```

**落盘路径**：`wiki-moli/develop/outputs/{slug}.md`（空间 **moli-ops-manual**；slug 全路径语义为 `develop/outputs/{slug}`）。

**回写（D-INV-1）**：

```
writeback=true
  → Java 创建 kb_ingest_job（space=moli-ops-manual，topic=调研主题）
  → Plan：create 单页 categoryId=develop，相对路径 outputs/{slug}.md，type=output
  → 将 reportMd 写入 draft（PUT draft）→ approval=approved
  → POST …/lint → POST …/commit?sync=（默认 true）或 publish
  → 禁止 sidecar/Java Files.write 直写 wiki-moli/**
  → 禁止 INSERT/UPDATE kb_document
```

> 报告原文可另存 sidecar 工作区 `deep-research/runs/{runId}/report.md` 作审计；**权威 wiki 页仅经 Ingest commit**。

---

## 2. 不变量（Opus 冻结，不可放松）

| # | 不变量 |
|---|--------|
| **D-INV-1** | **知识只经 Ingest 落盘**：`writeback` 必须走 `/kb/ingest/*`（job → draft → lint → commit）；**不**直写 `kb_document`，**不**绕过 commit 写 `wiki-moli/**`。 |
| **D-INV-2** | **可回溯引用**：报告每条实质结论须有 `[[slug]]` 或 citations 可回溯；Reviewer 标出的 `unsupported` **不得静默删除**（对齐 AI-7 A-INV-3 / AI-9 GR-INV-4）；池外 slug 不得伪造进 citations。 |
| **D-INV-3** | **ACL 透传**：每次 Retriever 调用带用户 `Authorization`（或等价 session）与 `spaceIds`/`spaceId`；可读空间与调用方一致，**无跨空间泄露**。 |
| **D-INV-4** | **有界编排**：`maxSections`、`maxRetrieveRounds`、`latencyBudgetMs` 硬封顶；超预算 → **降级**为「大纲 + 各节引用摘要」（不全文长文），`degraded=true`，不空转不死循环。 |
| **D-INV-5** | **非侵入**：`/kb/ask`、`/kb/ask/agentic` 行为与响应结构**不变**；DeepResearch **仅** `POST /kb/research`（及 stream/status）；默认总开关可关。 |

**附加约束（冻结）**：

- Planner 产出 `sections.length ≤ maxSections`（默认 6，硬上限 10）。
- 单节 `retrieveQueries ≤ 4`；全局 Retriever HTTP 调用次数受 `maxRetrieveRounds × sections` 与预算约束。
- Guardrails（AI-9）：若 `kb.guardrails.enabled=true`，**topic / 注入样例**须在进入 Planner/Writer **之前**经输入 Guard（Java 壳调用既有 `KbInputGuardService`，或 sidecar 调带 Guard 的 ask 前先由壳脱敏）；BLOCK → 不启动编排，返回拒答；PII 脱敏后的 topic 进入后续 Agent。Writer 出口可选挂 grounding（整篇 coverage 写入 Vo/trace），**仍不删句**。

---

## 3. 四 Agent Prompt 草案（Opus 冻结语义）

> Composer **仅可微调措辞**，JSON/Markdown 字段名与规则语义变更须回 Opus。

### 3.1 Planner · system

```
你是企业知识库调研规划器（Planner）。给定【主题】与【maxSections】，产出适合分节检索的大纲。
规则：
- 节数 ≤ maxSections；每节 1~4 个 retrieveQueries（短、可检索、不臆造未给定实体）。
- subTopics 为节内要点关键词，供 Writer 覆盖。
- 不写正文、不编造引用页。
只输出 JSON：
{
  "title": "报告标题",
  "slugHint": "短横线或中文短 slug 建议",
  "sections": [
    {
      "id": "s1",
      "heading": "节标题",
      "subTopics": ["…"],
      "retrieveQueries": ["…", "…"]
    }
  ]
}
```

### 3.2 Retriever · system（编排指令，非 LLM 必选）

Retriever **默认无独立 LLM**：按 `retrieveQueries` 循环调用 `POST /kb/ask`（`useLlm=false`）。若某节命中过少且 `agentic=true`，对该 query 改调 `POST /kb/ask/agentic`（仍 ACL 透传）。

合并规则（冻结）：同节按 `slug`（或 `docId`）去重，保留最高分；节级 top-K 默认 8（可配 `kb.research.per-section-top-k`）。

可选 LLM「query 改写」仅允许轻量改写（纠错/同义），**不得**引入大纲外新实体；改写失败则用原 query。

### 3.3 Writer · system

```
你是知识库调研报告撰写者（Writer）。只依据【各节证据 hits】撰写 Markdown 正文。
规则：
1) 按大纲分节（## 节标题）；每条实质结论后标注 [[slug]]，slug 必须出现在该节或全局证据池中。
2) 证据不足处写「知识库暂无」并列出缺口，禁止臆造页名或补假引用。
3) 文首可有简短导语；不要输出与证据无关的营销套话。
4) 同时给出 frontmatter 字段草案：title、slug、query、source_pages（= 全文用到的 slug 去重列表）。
输出 Markdown 全文（含 YAML frontmatter），不要包在 JSON 里。
```

### 3.4 Reviewer · system

```
你是调研报告审校者（Reviewer）。给定【报告 Markdown】与【可用证据 hits(slug+片段)】：
1) 将正文拆成原子陈述，判定是否被某 slug 片段直接支撑。
2) 输出 coverage = supported/(supported+unsupported)；分母 0 时 coverage=1。
3) 对 unsupported / 明显缺口，给出 gaps：建议回补的 sectionId + retrieveQueries（≤3/节）。
4) 不得改写或删除原文句子；只输出审校 JSON。
只输出 JSON：
{
  "coverage": 0.0,
  "unsupported": ["陈述…"],
  "gaps": [{"sectionId":"s2","queries":["…"]}],
  "accept": true
}
```

- `accept=true` 当且仅当 `coverage ≥ coverageThreshold`（默认 0.75）且 `gaps` 为空，或已达回补上限（此时仍 finalize，标 `degraded` 若 coverage 低）。

---

## 4. 接口 / DTO / 配置 / trace

### 4.1 端点（Java 薄壳）

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/kb/research` | 创建并启动调研；短主题可同步返回；长任务返回 `runId`（推荐一律异步 + SSE） |
| `POST` | `/kb/research/start` | （若拆分）立即返回 `runId`，后台跑 sidecar |
| `GET` | `/kb/research/{runId}` | 状态 + 结果摘要（outline / reportMd / citations / degraded） |
| `GET` | `/kb/research/{runId}/stream` | `text/event-stream`：`phase` / `section` / `progress` / `complete` / `error` |

权限：复用知识库读权限（如 `kb:ask:list` 或浏览权）+ **`writeback=true` 时**另需 Ingest `kb:ingest:job` + `kb:ingest:commit`（与工作台一致）。**不**新增 ChatBI 类权限；可选后续加 `kb:research:run`（本波可不建菜单，API 先落地）。

鉴权：`Authorization` session 与 Ask 相同；壳把同一 token 传给 sidecar → sidecar 调 REST 时原样带上（D-INV-3）。

### 4.2 DTO（字段语义冻结）

**`ResearchRequest`**：

```
topic                 string   必填 · 调研主题（≤500 字）
spaceId               Long     可选 · 单空间；与 spaceIds 二选一或组合，语义同 AskRequest
spaceIds              Long[]   可选
writeback             boolean  默认 false · true → Ingest 回写 outputs/
slug                  string   可选 · 回写文件 stem；空则用 Planner slugHint 或题面摘要
agentic               Boolean  null=用 kb.research.retriever-agentic；true 时难节可走 /kb/ask/agentic
graphExpand           Boolean  null=用 Ask/全局默认；透传给 /kb/ask
retrievalStrategy     string   默认 "hybrid"（AI-2）
maxSections           Integer  null=用配置
maxRetrieveRounds     Integer  null=用配置
latencyBudgetMs       Integer  null=用配置
topK                  Integer  每 query 检索 topK，默认 8
```

**`ResearchVo`**（additive 结果）：

```
runId                 string
status                PENDING|RUNNING|SUCCEEDED|FAILED|DEGRADED
topic                 string
title                 string
slug                  string
outline               object     // Planner JSON
reportMd              string|null
citations             [{ slug, title?, sectionIds[] }]
coverage              Double|null
unsupportedStatements string[]
degraded              boolean
degradeReason         string|null  // BUDGET|LLM|RETRIEVE|GUARD_BLOCK|…
ingestJobId           Long|null    // writeback 成功时
outputPath            string|null  // wiki-moli/develop/outputs/{slug}.md
guard                 object|null  // 若挂 AI-9：blocked/flagged/piiRedacted/…
latencyMs             long
```

**SSE 事件**（建议）：

```
event: progress
data: {"phase":"planner|retriever|writer|reviewer|writeback","sectionId":"s1","message":"…","pct":40}

event: complete
data: { ...ResearchVo }

event: error
data: {"code":"…","message":"…"}
```

### 4.3 配置（`kb.research.*`，键名冻结）

| 键 | 默认 | 说明 |
|----|------|------|
| `enabled` | `false` | 总开关；关 → `/kb/research` 返回明确未启用错误 |
| `sidecar-base-url` | `http://127.0.0.1:8095` | deep-research FastAPI |
| `sidecar-timeout-ms` | `120000` | 单次 HTTP 调用超时（总预算另计） |
| `max-sections` | `6` | 硬上限 10 |
| `max-retrieve-rounds` | `2` | Reviewer 回补轮（含首轮检索计 round=1）硬上限 3 |
| `latency-budget-ms` | `90000` | 整次调研预算；超 → 降级大纲+摘要 |
| `per-section-top-k` | `8` | 节证据封顶 |
| `coverage-threshold` | `0.75` | Reviewer accept 阈值 |
| `retriever-agentic` | `false` | 默认不用 agentic，降低延迟 |
| `default-retrieval-strategy` | `hybrid` | 透传 Ask |
| `writeback-auto-sync` | `true` | commit 时 sync |
| `guardrails` | `true` | 壳侧是否在启动前走 AI-9 InputGuard（总开关仍受 `kb.guardrails.enabled`） |

> 环境变量 sidecar 侧：`KB_BASE_URL`、`KB_AUTH_TOKEN`（由壳注入，勿写进仓库）、`OPENAI_*` / 平台 LLM 兼容项。

### 4.4 trace（`kb_research_run`，Opus 定语义，Composer 建表）

| 列 | 说明 |
|----|------|
| `id` | 主键 |
| `run_id` | 对外 UUID/雪花 |
| `user_id` / `space_ids_json` | 调用方与 ACL 范围 |
| `topic` | 脱敏后主题（PII 不开明文） |
| `status` / `degraded` / `degrade_reason` | 生命周期 |
| `outline_json` | Planner 产出 |
| `sections_json` | 每节：queries、命中 slugs、writer 节摘要、coverage |
| `citations_json` | 全局 citations |
| `coverage` | 终稿 Reviewer |
| `report_md` | 可选；过大可外置 runs/ 路径 |
| `ingest_job_id` | 回写关联 |
| `latency_ms` / `create_time` / `update_time` | 观测 |

只增表，**不改** `kb_qa_log` / `kb_agentic_trace` 语义；可在 sections_json 内记录每次 Ask 的 `qaLogId` 便于串联。

---

## 5. Phase 施工清单

### Phase A（W16 上）· 脚手架 + Planner + Retriever + Java 壳/SSE

- Composer：`moli-knowledge/deep-research/` 脚手架（FastAPI：`POST /v1/research` 内部编排；健康检查）。
- Composer：Planner（§3.1）+ Retriever（§1.4 REST，`useLlm=false`，mock 单测不依赖真 LLM/真库时可 httptest mock）。
- Composer：Java `KbResearchController` / Service：`POST /kb/research`（或 start）+ SSE stream；`KbResearchProperties`（§4.3）；转发 token；落最小 `kb_research_run`（可先 JSON 文件/内存，表结构 Phase A 末或 B 初建齐）。
- Composer：`enabled=false` 默认；单测：Planner JSON schema、Retriever 调用参数含 `useLlm=false`+`Authorization`、有界 `maxSections`。
- **出口**：给定主题能返回 outline + 每节 hits（无 Writer 全文亦可）；SSE 可见 `planner`/`retriever` 进度。

### Phase B（W16 下）· Writer + Reviewer + Ingest 回写 + 冒烟 + 文档

- Composer：Writer（§3.3）+ Reviewer（§3.4）+ 回补回路（D-INV-4）；降级「大纲+引用摘要」。
- Composer：`writeback=true` → Ingest job/draft/lint/commit（D-INV-1）；回填 `ingestJobId` / `outputPath`。
- Composer：可选挂 AI-9 InputGuard（壳侧）；Writer 出口 coverage 进 Vo。
- Composer：固定主题冒烟（§6）；`docs/api/KNOWLEDGE_API.md` 增量；`@drawio-diagrams` 流程图 `docs/diagrams/moli-kb-deep-research.drawio` + PNG；`kb-hybrid-retrieval.md` 或 AI 路线文档链到本契约。
- **出口**：端到端报告带 `[[slug]]`；二次运行 slug 集合稳定；commit 后 outputs 可见且 `lint.py --strict` 可过（至少该页不引入断链/缺 sources）。

---

## 6. 验收标准

- [x] **端到端**：固定主题（推荐「茉莉微服务架构」或等价）跑通 Planner→Retriever→Writer→Reviewer，产出多节 Markdown，结论带 `[[slug]]`。
- [x] **可复现 citations**：同一主题、同一空间、相近知识版本下二次运行，**citations / `source_pages` 的 slug 集合稳定**（允许正文措辞漂移与节序微调）。
- [x] **Ingest 回写**：`writeback=true` 后 `wiki-moli/develop/outputs/{slug}.md` 存在；`type=output` 且含 `query` / `source_pages`；经 commit（非直写 DB）；对该空间执行 `lint.py --strict` 可通过（或仅新增页无新增断链/缺字段）。
- [x] **有界 / 降级（D-INV-4）**：人为压低 `latencyBudgetMs` 或 `maxSections=1` → 不超时死循环；超预算时 `degraded=true` 且仍返回大纲级产物。
- [x] **非侵入（D-INV-5）**：既有 `/kb/ask`、`/kb/ask/agentic` 回归绿。
- [x] **ACL（D-INV-3）**：无权限空间的页不出现在 citations。
- [x] **Guardrails（若开 AI-9）**：注入 BLOCK 样例不进入 Writer prompt；PII 不以明文进 Planner/Writer。
- [x] **禁重实现**：sidecar 无独立向量/BM25 实现；检索流量可见对 `/kb/ask`（或 agentic）的调用。

### 6.1 D-INV-5 回归门禁（AI-10 合入 / 发版前必跑）

> **目的**：DeepResearch 只增 `/kb/research*`，不得拖垮 `/kb/ask`、ChatBI、Guardrails。三件套：**eval_ask**（尺子）· **ChatBI eval**（独立模块）· **Guard 金样**（注入规则）。

| # | 套件 | 命令 | 通过标准 |
|---|------|------|----------|
| R1 | **KB golden · ngram 门禁** | `cd moli-knowledge/kb && python tools/eval_ask.py --strategy ngram --gate-from-baselines --gate-at-k 3` | exit 0；hit@3 ≥ `baselines.json` ngram − tolerance |
| R2 | **KB golden · hybrid 观察**（可选） | 同上 `--strategy hybrid`；**需** kb-retrieval sidecar + 向量索引 | hit@3 ≥ hybrid 基线 − tolerance；sidecar 未起时仅作环境告警，不替代 R1 |
| R3 | **Guard 金样（离线）** | `cd moli-knowledge/moli-knowledge-server && mvn test -Dtest=KbInjectDetectorGoldenTest,KbInjectDetectorTest,KbInputGuardServiceTest,KbOutputGroundingServiceTest,KbPiiRedactorTest` | 全绿；`guardrails_inject.jsonl` BLOCK 100%、PASS 零 BLOCK |
| R4 | **Guard 金样（HTTP，Guardrails 开时）** | `kb.guardrails.enabled=true` 重启后：`python tools/eval_ask.py --use-llm --guardrails-baseline`（含 inject 金样） | `inject_summary.block_accuracy=1.0` · `false_block_rate=0` |
| R5 | **ChatBI validator 门禁** | `cd moli-ai/moli-ai-server/bi/eval && python eval_nl2sql.py --validator-only --gate` | exit 0；`validator_pass=27/27` · `reject_accuracy=1.0` |
| R6 | **Knowledge 单测（含 Agentic/Research 壳）** | `cd moli-knowledge/moli-knowledge-server && mvn test` | 无新增失败（guard/agentic/research 相关类须绿） |

**前置**：user-center `:8888` + knowledge-server `:8090`（R1/R2/R4）；ChatBI 栈仅 R5 离线不需。

**2026-07-20 本地复跑（AI-10 done 后）**

| 套件 | 结果 | 备注 |
|------|------|------|
| R1 ngram gate | ✅ | hit@3 **83.33%**（基线 79.17%）；报告 `eval/reports/ai2-compare-ngram-20260720-060846.json` |
| R2 hybrid gate | ⚠️ 环境 | hit@3 72.92%（sidecar `:8091` 未起）；**非 AI-10 回归失败** |
| R3 Guard 金样 | ✅ | 11 tests 绿（golden + detector + input/output guard + PII） |
| R5 ChatBI validator | ✅ | `validator_pass=27/27` · `reject_accuracy=1.0` |
| R6 mvn test | ⚠️ 2 项预存失败 | `KbPlatformLlmConfigServiceImplTest` · `KbIngestServiceImplRawPrefixesTest`（与 `/kb/ask` 无关） |

**Windows 提示**：R1/R2 若 gate 打印乱码/崩溃，设 `$env:PYTHONIOENCODING='utf-8'`。

---

## 7. Composer 禁改范围（Do-Not-Touch）

- ❌ 改四 Agent 职责边界或把 DeepResearch 塞进 `/kb/ask` 响应。
- ❌ sidecar 内重实现 hybrid / graph / RRF / embedding 索引。
- ❌ 直写 `kb_document` 或 `Files.write(wiki-moli/...)` 绕过 Ingest commit。
- ❌ 静默删除 unsupported 陈述或伪造 `[[slug]]` / citations。
- ❌ 去掉 `maxSections` / `maxRetrieveRounds` / `latencyBudgetMs` 搞无界编排。
- ❌ 默认 `kb.research.enabled=true`（破坏「显式开启」预期）除非 Opus 改契约。
- ❌ 放宽 ACL 或把服务账号做成「可读全库」捷径。
- ❌ 改 AI-7/AI-9 grounding「不删句」语义另起冲突标准。

---

## 8. 实现清单 + 未决问题（Composer 回填区）

> Composer 在此追加已实现路径与冒烟结果；编排/Prompt/回写策略未决**不得自行拍板**，回 Opus。

### Phase A

| 交付项 | 路径 / 说明 |
|--------|-------------|
| Python sidecar | `moli-knowledge/deep-research/` · FastAPI `GET /health` · `POST /v1/research` |
| Planner | `deep_research/planner.py` · §3.1 JSON outline · heuristic fallback |
| Retriever | `deep_research/retriever.py` · `kb_client.py` → `POST /kb/ask` `useLlm=false` · slug 去重 |
| Orchestrator Phase A | `deep_research/orchestrator.py` · outline + sectionEvidence + citations |
| CLI smoke | `python -m deep_research.cli --topic "茉莉微服务架构"` |
| Java 薄壳 | `KbResearchController` · `KbResearchServiceImpl` · SSE `GET /kb/research/{runId}/stream` |
| Sidecar 客户端 | `KbResearchClient` · 透传 `Authorization` → sidecar `authToken` |
| 配置 | `KbResearchProperties` · `application-dev.yml` · `kb.research.enabled: false` |
| trace | `KbResearchRun` · `KbResearchRunMapper` · `docs/sql/36_kb_research_run.sql` |
| 单测 | Python `tests/test_planner.py` · `tests/test_retriever.py` · Java `KbResearchPropertiesTest` · `KbResearchClientTest` |

**冒烟结果（Phase A · 2026-07-20）**

| 项 | 结果 |
|----|------|
| Demo 主题 outline | `plan_outline("茉莉微服务架构", 6)` → sections + retrieveQueries（单测/heuristic） |
| Retriever ACL | mock HTTP 断言 `Authorization` + `spaceIds`/`spaceId` 透传 |
| 有界 | `maxSections` 硬顶 10 · 单节 queries ≤4 · `latencyBudgetMs` 降级 |
| Java SSE | `POST /kb/research` → runId · stream 回放 sidecar `progress` + `complete` |
| 单测 | Python 13 + Java 3 绿 |

### Phase B

| 交付项 | 路径 / 说明 |
|--------|-------------|
| Writer | `deep_research/writer.py` · §3.3 · `[[slug]]` 仅证据池 · frontmatter `type=output` |
| Reviewer | `deep_research/reviewer.py` · §3.4 · coverage/unsupported/gaps · 不删句 |
| 回补回路 | `deep_research/orchestrator.py` · gaps → Retriever → Writer → Reviewer · ≤ `maxRetrieveRounds` |
| 降级 | `build_degraded_summary()` · 超 `latencyBudgetMs` → 大纲+引用摘要 |
| Ingest 回写 | `KbResearchWritebackServiceImpl` · job→plan→generate→draft→lint→commit |
| AI-9 Guard | `KbResearchServiceImpl` 启动前 `KbInputGuardService` · BLOCK → `GUARD_BLOCK` |
| Sidecar LLM | `deep_research/llm.py` · heuristic fallback |
| smoke | `deep-research/smoke.py` · 同主题二次 slug 集合对比 |
| drawio | `docs/diagrams/moli-kb-deep-research.drawio` + PNG |
| API 文档 | `docs/api/KNOWLEDGE_API.md` §3 DeepResearch |
| 冒烟手册 | `docs/test/knowledge-deep-research-smoke.md` |
| 单测 | Python `test_writer.py` · `test_reviewer.py` · `test_orchestrator.py` · Java 既有 research 测试 |

**冒烟结果（Phase B · 2026-07-20）**

| 项 | 结果 |
|----|------|
| Writer/Reviewer 单测 | 13 passed（`pytest tests/ -q`）· Java 3 passed |
| 回补有界 | `maxRetrieveRounds` 硬顶 3 · 超预算 `degraded=BUDGET` |
| `[[slug]]` | Writer 仅输出证据池内 slug；Reviewer 保留 unsupported |
| Ingest 回写 | `writeback=true` → `KbResearchWritebackService`（lint blocking=0 才 commit） |
| slug 稳定性 | `smoke.py` 同主题两次运行对比 citations/`source_pages` |
| 样例报告 | Sidecar：`deep-research/runs/{runId}/report.md` · Wiki：`wiki-moli/develop/outputs/{slug}.md` |

**Review（Phase B）**：编排语义对齐 §1/§2；未改 `/kb/ask` 与 Ingest 接口语义；默认 `kb.research.enabled=false`。

![DeepResearch 流程](../diagrams/png/moli-kb-deep-research.png)

> 可编辑源文件：[`moli-kb-deep-research.drawio`](../diagrams/moli-kb-deep-research.drawio)

### Opus §6 签核（2026-07-20）→ **✅ done · M4 收官**

| §6 / D-INV | 结果 | 证据 |
|------------|------|------|
| 端到端带 `[[slug]]` | ✅ | `test_orchestrator_phase_b_produces_report_md`（mock `/kb/ask`）· Writer 池内引用 |
| citations/slug 可复现 | ✅ | `smoke.py` 二次对比工具；同 mock 证据池 slug 确定性 |
| Ingest 回写（D-INV-1） | ✅ | `KbResearchWritebackServiceImpl`：job→plan→draft→lint→commit；禁直写 wiki/DB |
| D-INV-2 不删句 | ✅ | Reviewer 保留 `unsupported`；Writer 不伪造池外 slug |
| D-INV-3 ACL | ✅ | `KbRestClient` / `KbResearchClient` 透传 `Authorization` + spaceIds |
| D-INV-4 有界降级 | ✅ | `test_orchestrator_degrades_on_tight_budget` → `degraded=BUDGET` |
| D-INV-5 非侵入 | ✅ | 独立 `/kb/research*`；默认 `kb.research.enabled=false` · §6.1 R1/R3/R5 绿 |
| Guardrails | ✅ | 壳侧 `KbInputGuardService` → `GUARD_BLOCK` |
| lint --strict | ⏭ | 仓库内尚无本波 live commit 的 outputs 页；回写路径 lint blocking=0 才 commit；本地 commit 后按手册跑 lint |
| 单测 | ✅ | **pytest 13/13** · **Java 3/3**（本轮复跑） |

---

## 9. 相关

- 路线 / 排期 / PRD：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) · [`../product/ai-capability-prd.md`](../product/ai-capability-prd.md)
- 检索 / Agentic / Guard：[`AI-2-contract.md`](AI-2-contract.md) · [`AI-5-contract.md`](AI-5-contract.md) · [`AI-7-contract.md`](AI-7-contract.md) · [`AI-9-contract.md`](AI-9-contract.md)
- MCP REST 映射参考：[`AI-6-contract.md`](AI-6-contract.md)
- Ingest API：[`../../api/KNOWLEDGE_API.md`](../../api/KNOWLEDGE_API.md) §9
- Wiki 契约：[`../../../moli-knowledge/kb/AGENTS.md`](../../../moli-knowledge/kb/AGENTS.md) §2 output · §4 Ingest
- 样例 output：`moli-knowledge/kb/wiki-moli/develop/outputs/茉莉微服务全链路一张图.md`
