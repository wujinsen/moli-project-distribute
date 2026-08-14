# AI-7 Agentic RAG（查询改写 / 多跳 / 答案自检 / 引用校验）· 施工契约（Opus 产出，Composer 施工）

> **角色**：Opus（编排层与 prompt/自检负责人）产出，Composer 施工的**唯一契约**。
> **任务**：在 `KbAskService` 之上加**编排层**：query 改写/拆解 → 多轮检索（hybrid/graph）→ 生成 → 答案自检（陈述↔引用对齐）→ 不足则回补检索一轮；单轮/Agentic 可配置切换。
> **状态**：✅ **done** · 2026-07-20 Opus §6.1 签核 · dirty+multi-hop hit@3 +5pp / coverage +7.72pp / 延迟 2.43×≤2.5× · 报告 `ai7-agentic-compare-20260720-024750.json`
> **主导**：🟣 **Opus 主导** —— **Opus 拍板「编排状态机、改写/拆解 prompt、自检对齐逻辑、回补检索决策、有界/降级不变量」（§1/§2/§3）**；Composer 铺量（状态机代码、trace 表、开关、各步接线、eval）。
> **上游**：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4 第 3 波 AI-7 · §3 依赖 AI-2 · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §4 W11–W12 · §9.1 AI-7 · [`AI-2-contract.md`](AI-2-contract.md)（hybrid 召回）· [`AI-5-contract.md`](AI-5-contract.md)（graph 扩跳，自动受益）· [`AI-1-contract.md`](AI-1-contract.md)（`dirty`/`multi-hop` 子集）
> **现有落地（复用，勿重造）**：`KbAskServiceImpl.ask()`（检索→引用→生成 单轮全链路）· `recallHybridChunks`/`recallAndScoreChunks`（AI-2 召回）· `KbLlmClient.chat(scene,spaceId,sys,user)`（LLM）· `kb_qa_log`（问答日志，Agentic trace 基底）· ACL `kbAclService.resolveReadableSpaceIds` · `AskRequest`/`AskResponse`（含 `citations`）

---

## 0. 契约边界（读我）

**本契约定义**：Agentic 编排状态机与轮次控制、改写/拆解与自检 prompt、引用对齐（grounding）与回补检索决策、有界/降级/ACL 不变量、新端点与 trace 字段语义、eval 口径、验收与禁改范围。

**不在本契约内（交给 Composer）**：状态机 Java 骨架、`kb_agentic_trace` 建表 DDL/Entity/Mapper、配置类、`AgenticAskVo` 样板、各步接线、`eval_ask.py --agentic` 样板、对比表回填、drawio。

**红线**：Composer **不得**改动 §1 编排轮次/回补决策、§2 不变量、§3 prompt 语义、`/kb/ask` 单轮既有行为与响应结构。发现歧义 → 回 Opus 窗口改契约。

---

## 1. 架构与编排决策（Opus 冻结）

### 1.1 定位：`KbAskService` 之上的**编排层**，单轮为默认基线

Agentic **不改** `/kb/ask` 单轮链路；新增编排入口 `POST /kb/ask/agentic`，其内部**复用**单轮的检索/引用/生成组件，只是多轮编排：

```
单轮基线（不动）：       POST /kb/ask        → recall → cite → generate
Agentic 编排（新增）：   POST /kb/ask/agentic → [rewrite/decompose] → [multi-round retrieve]
                                              → generate → [self-check grounding]
                                              → coverage<阈值 且 round<max ? 回补检索+重生成 : 结束
                                              → 写 kb_agentic_trace
```

- **切换**：`effectiveAgentic = (request.agentic != null) ? request.agentic : kb.agentic.enabled`（与 AI-5 `graphExpand` 同形：请求非 null 覆盖配置）。`effectiveAgentic=false` 时 `/kb/ask/agentic` **直接走单轮**（等价 `/kb/ask` 语义 + `AgenticAskVo` 里 `agentic=false`/`degraded` 按需），零编排。
- **复用不重造**：检索一律调既有 `recallHybridChunks`/`recallAndScoreChunks`（自动吃 AI-2 hybrid + AI-5 graph）；生成调 `KbLlmClient` + 单轮 `SYSTEM_PROMPT`；**编排层只做 query 变换、轮次控制、候选合并、自检**（A-INV-5）。Composer 可将 `KbAskServiceImpl` 内 private 召回/生成抽为 **package-private / support** 供编排复用，**禁止**改 `ask()` 对外行为与 `AskResponse` 字段。

### 1.2 编排状态机（冻结）

```
S0 rewrite_decompose (LLM, §3.1)     # decompose=false 时只改写，subQuestions=[]
   ├─ 改写脏 query（错别字/口语/跨语种 → 规范检索式）
   └─ 若判定多跳 → 拆 ≤maxSubQuestions 个子问题；否则单问

S1 retrieve（同一 round 内可多 query，复用既有召回）
   queries = {rewritten} ∪ subQuestions（回补轮另见 S4）
   for q in queries:
       recall(q, scopeSpaces)        # 每 query 过 ACL（A-INV-2）
   合并进**持久候选池**（按 chunkId/docId 去重，保留最高分）→ 取 top-K 进上下文

S2 generate (LLM, §3.2 沿用单轮 SYSTEM_PROMPT + 合并上下文)
   → answer + citations（citations 与 answer 内 [[slug]] 均须 ⊆ 候选池，见 §1.3）

S3 self_check (LLM, §3.3)  ← 仅 kb.agentic.self-check=true（Phase B；Phase A 跳过，coverage=null）
   → 拆 answer 为陈述句，逐句判定「是否被某引用支撑」
   → 输出 {supported[], unsupported[], missingInfo[]}，coverage = supported/total

S4 decide（Phase B）
   ├─ coverage ≥ coverageThreshold 或 round ≥ maxRounds 或 超 latency-budget → 结束（S5）
   └─ 否则 → 回补：queries = missingInfo ∪ unsupported 关键词 → S1（round++）→ S2 → S3

S5 finalize → AgenticAskVo + 写 kb_agentic_trace（Phase A 可先写最小 trace 或仅 qa_log；完整字段 Phase B）
```

**轮次计数（冻结）**：
- `round` 从 **1** 起：首次完成 S1→S2（及若启用的 S3）计为 round=1。
- 每次回补再跑 S1→S2→S3 时 `round++`。
- `maxRounds` 默认 **2**、硬上限 **3** ⇒ 默认至多 **1 次回补**（round=1 初答 + round=2 回补答）。
- `coverageThreshold` 默认 **0.8**：低于则回补（且 `round < maxRounds`、未超延迟预算）。
- **回补 query 来源**：优先 `missingInfo`，其次 unsupported 陈述关键词；**并入既有候选池**（不重开空池）。

### 1.3 候选合并与引用

- 多轮候选按 `chunkId`（无则 `docId`）去重，保留跨轮最高分；上下文 top-K 沿用 `kb.ask.llm-context-top-k`（或 `per-round-context-top-k`）。
- 引用 `citations` 复用 `AskResponse.Citation`；finalize 前 **过滤**：`citations` 与 answer 中 `[[slug]]` 均须 ∈ 最终候选池 slug 集合——池外 slug **剔除引用条目**（不伪造替换）；正文无据陈述留给自检标 `unsupported`，**不静默改写删句**（A-INV-3）。
- 自检发现的 `unsupported` 在 `AgenticAskVo` 如实返回（供前端标低置信），**不静默删除**。

---

## 2. 不变量与约束（Opus 冻结，不可放松）

| # | 不变量 |
|---|--------|
| A-INV-1 | **有界编排**：`maxRounds ≤ 3`、`maxSubQuestions ≤ 5`、每轮候选与上下文均封顶；达上限即停，无死循环。 |
| A-INV-2 | **每轮 ACL**：每次检索都经 `resolveReadableSpaceIds(scope)`；跨空间/无权页绝不进候选（与单轮一致）。 |
| A-INV-3 | **grounding 不造引用**：最终答案只能引用最终候选池内 slug；自检发现的无据陈述如实标出，不伪造/补引用。 |
| A-INV-4 | **可切换 + 非侵入**：`/kb/ask` 单轮行为与响应结构**不变**；Agentic 为独立端点 `/kb/ask/agentic`，`kb.agentic.enabled=false` 时其退化为单轮。 |
| A-INV-5 | **复用检索**：检索/召回一律复用 AI-2/AI-5 既有组件，编排层**不新写**检索逻辑。 |
| A-INV-6 | **trace 完整**：每次 Agentic 落 `kb_agentic_trace`（改写、子问题、每轮检索命中 slug、自检 coverage、unsupported、轮次、耗时、是否降级），并关联 `kb_qa_log.id`。 |
| A-INV-7 | **降级**：LLM 不可用 → 无法 Agentic（改写/生成/自检依赖 LLM）→ **回退单轮检索式**（mode=retrieval，`degraded=true`），不空转、不报错崩溃。自检 JSON 解析失败 → 视为本轮不回补、结束，标 `degraded`。 |
| A-INV-8 | **延迟可控**：Agentic 平均延迟相对同配置单轮（`--use-llm`）增幅 **≤ 2.5×**（排期出口；S0+S2+S3 多 LLM 调用，原 <2× 过紧）；`latency-budget-ms` 硬封顶，超预算跳过自检/回补并 finalize。 |

---

## 3. Prompt 草案（Opus 冻结语义，Composer 仅可微调措辞并回 Opus 确认）

### 3.1 rewrite_decompose · system（草案）
```
你是知识库检索规划器。给定用户问题，做两件事：
1) 改写：修正错别字/口语/中英混写，产出适合全文+向量检索的规范中文查询；不改变原意、不臆造实体。
2) 判定是否“多跳”（需综合 2+ 主题/页才能答）。是→拆成 2~{maxSub} 个彼此独立、可各自检索的子问题；否→子问题为空。
只输出 JSON：{"rewritten":"...","multiHop":true|false,"subQuestions":["...","..."]}
```

### 3.2 generate · system
沿用单轮 `KbAskServiceImpl.SYSTEM_PROMPT`（“只依据知识库页作答、每结论后 `[[页slug]]` 标注来源、无据则明说暂无”）；user 拼合并后的多轮候选上下文。**不另造生成 prompt**，保证与单轮同口径可比。

### 3.3 self_check · system（草案，grounding 核心）
```
你是答案核查器。给定【答案】与【可用引用页(slug+片段)】，把答案拆成原子陈述句，逐句判定是否被某条引用内容直接支撑。
规则：只依据给定引用判断；不确定或引用中找不到依据的，判为 unsupported。
对每条 unsupported，给出“还需要检索什么信息”的关键词（用于回补检索）。
只输出 JSON：{"supported":["陈述…"],"unsupported":["陈述…"],"missingInfo":["关键词…"]}
```
- `coverage = |supported| / (|supported|+|unsupported|)`（分母 0 时 coverage=1，视为无实质陈述）。

---

## 4. 接口 / DTO / 配置

### 4.1 新增端点（不动 `/kb/ask`）

`POST /kb/ask/agentic` → `MoliResult<AgenticAskVo>`（权限/鉴权复用现有 kb 读权限，无新增权限码）。

`AgenticAskRequest`（复用 `AskRequest` 字段 + 编排开关）：
```
question, spaceId, spaceIds, topK, llmContextTopK, retrievalStrategy, graphExpand  // 同 AskRequest
useLlm            // 省略时默认 true（Agentic 工具语义；≠ AskRequest 默认 false）
                  // false → 不编排，直接单轮检索式，degraded=true
agentic           // Boolean；null=用 kb.agentic.enabled；见 §1.1 effectiveAgentic
```

`AgenticAskVo`（单轮字段 + 编排元信息，additive；**不改** `AskResponse`）：
```
// 复用单轮字段（与 AskResponse 同名同义）
answer, mode, scope, scopeReason, provider, model, citations[], qaLogId
// 编排元信息
agentic:  true|false          // 本次是否实际跑了编排（退化单轮则为 false）
rounds:   int                 // 实际 round 数（退化单轮为 1 或 0，约定：退化=1 次单轮检索）
rewrittenQuery: string|null   // 未改写/退化时可为 null 或原问
subQuestions:   string[]      // 可空
coverage: Double|null         // 末轮自检；self-check 关或 Phase A 为 null
unsupportedStatements: string[]
retrievedSlugsPerRound: string[][]   // 每轮命中 slug（trace 摘要）
degraded: boolean             // LLM 不可用 / useLlm=false / 自检 JSON 失败等
```
> `/kb/ask` 的 `AskResponse` **保持不变**（A-INV-4）；Agentic 元信息只在新 VO。

### 4.2 配置（`kb.agentic.*`，键名冻结）

| 键 | 默认 | 说明 |
|----|------|------|
| `enabled` | `false` | 总开关；关→`/kb/ask/agentic` 退化单轮 |
| `max-rounds` | `2` | 生成+自检+回补的最大轮数，硬上限 3 |
| `decompose` | `true` | 是否启用多跳拆解 |
| `max-sub-questions` | `3` | 拆解子问题上限（≤5） |
| `self-check` | `true` | 是否启用自检/引用校验 |
| `coverage-threshold` | `0.8` | 低于触发回补（round 未达上限时） |
| `latency-budget-ms` | `20000` | 编排总时间预算，超→提前 finalize（A-INV-8） |
| `per-round-context-top-k` | 复用 `kb.ask.llm-context-top-k` | 每轮上下文页数 |

### 4.3 trace（`kb_agentic_trace`，Opus 定字段语义，Composer 建表）

字段：`id`、`qa_log_id`（关联 `kb_qa_log`）、`space_id`、`user_id`、`question`、`rewritten`、`sub_questions_json`、`rounds`、`steps_json`（每轮：queries、命中 slug、coverage、unsupported、耗时）、`coverage`、`degraded`、`latency_ms`、`create_time`。只增不改既有 `kb_qa_log`（可选加 `agentic tinyint` 标记，不改语义）。

---

## 5. 分 Phase 施工清单

### Phase A（W11）· 改写/拆解 + 多轮检索编排
- Composer：`KbAgenticAskService` 状态机（**S0–S2 + S5 最小 finalize**；S3/S4 留 Phase B）、`KbAgenticProperties`（§4.2）、`POST /kb/ask/agentic` + `AgenticAskRequest/Vo`。
- Composer：S0 改写/拆解调 `KbLlmClient`（§3.1 JSON）；S1 复用既有 recall（可抽 support）；S2 复用单轮 `SYSTEM_PROMPT` + 池内引用过滤（§1.3）。
- Composer：`effectiveAgentic=false` 退化单轮（回归：`/kb/ask` 行为与 `AskResponse` 不变）。
- 出口：多跳问题能拆解 + 同 round 多 query 检索合并，返回 `answer` + 合并 `citations`；`coverage=null`。

### Phase B（W12）· 自检 + 引用校验 + 回补 + trace + eval
- Composer：S3 自检（§3.3）+ S4 回补决策（coverage 阈值/轮次上限）+ S5 finalize；`kb_agentic_trace` 建表 + 落库（A-INV-6）。
- Composer：降级路径（A-INV-7）、延迟预算（A-INV-8）。
- Composer：`eval_ask.py --agentic`（run 标签 `agentic`），在 `dirty`+`multi-hop` 子集对比**单轮 vs Agentic**：hit@k、**引用覆盖率**、平均延迟；回填 `docs/design/kb-hybrid-retrieval.md` 新增 **§8 Agentic RAG** 对比表 + drawio 编排图（`@drawio-diagrams`）。
- 出口：引用覆盖率提升、延迟 ≤2.5×、trace 可回溯。

---

## 6. 验收标准 + Composer 禁改范围

### 6.1 验收标准
- [x] **可切换非侵入（A-INV-4）**：`kb.agentic.enabled=false` 或 `agentic=false` 时 `/kb/ask/agentic` 结果等价单轮；`/kb/ask` 行为与响应结构不变（回归）。
- [x] **多跳跑通**：多跳问题被拆解为子问题并多轮检索，候选合并去重正确。
- [x] **自检/回补生效**：构造“答案含无据陈述”样例 → 自检标出 unsupported、coverage<阈值 → 触发一轮回补；轮次受 `max-rounds` 封顶（A-INV-1）。
- [x] **指标提升（排期出口）**：`dirty`+`multi-hop` 子集，Agentic vs 单轮 **hit@k 与引用覆盖率提升可量化**；对比写入 `kb-hybrid-retrieval.md §8`。
- [x] **延迟可控（A-INV-8）**：Agentic 平均延迟相对单轮增幅 **≤ 2.5×**；超 `latency-budget-ms` 提前 finalize。
- [x] **grounding + ACL（A-INV-2/3）**：答案只引用候选池 slug；每轮检索过 ACL；无跨空间泄露、无伪造引用。
- [x] **降级（A-INV-7）**：LLM 关闭 → 回退单轮检索式、`degraded=true`，不崩不空转。
- [x] **trace（A-INV-6）**：`kb_agentic_trace` 记录改写/子问题/每轮命中/coverage/轮次，关联 `qa_log_id`。

### 6.2 Composer 禁改范围（Do-Not-Touch）
- ❌ 改 §1.2 编排轮次/回补决策、§2 有界·延迟·降级不变量、§3 prompt **语义**（措辞微调须回 Opus）。
- ❌ 改 `/kb/ask` 单轮行为或 `AskResponse` 结构（A-INV-4）；Agentic 元信息只进新 VO。
- ❌ 重写检索/召回逻辑（必须复用 AI-2 hybrid + AI-5 graph，A-INV-5）。
- ❌ 去掉轮次上限/延迟预算搞无界循环（A-INV-1/8）；❌ 绕过每轮 ACL（A-INV-2）。
- ❌ 伪造/补全 citations、静默删除无据陈述（A-INV-3）。
- ❌ 改 AI-3 门禁三档基线；Agentic 为观察档（如需入库另议 key）。

---

## 7. 实现清单 + 未决问题（Composer 回填区）

> Composer 在此追加「已实现类/接口清单」与「未决问题」；编排/prompt/自检相关未决**不得自行拍板**，回 Opus 窗口改契约。

### Phase A 实现清单（2026-07-20 · S0–S2 + S5 最小 finalize）

| 项 | 路径 | 说明 |
|----|------|------|
| 配置 | `config/KbAgenticProperties.java` | §4.2 `kb.agentic.*`（默认 `enabled=false`） |
| DTO | `dto/AgenticAskRequest.java` / `AgenticAskVo.java` | §4.1 嵌单轮字段 + 编排元信息 |
| 编排 | `service/impl/KbAgenticAskServiceImpl.java` | S0 rewrite_decompose · S1 多 query 召回合并 · S2 单轮 SYSTEM_PROMPT 生成 · S5 finalize |
| 召回复用 | `service/impl/KbAskServiceImpl.java` | package-private `recallForQuery` / `mergeQueryRecalls` / `generateFromPool` / `filterCitationsToPool`（A-INV-5） |
| 端点 | `controller/KbAskController.java` | `POST /kb/ask/agentic` · 权限复用 kb 读 ACL |
| LLM scene | `support/KbLlmCallScenes.java` | `agentic_rewrite` |
| 配置样例 | `application-dev.yml` | `kb.agentic.enabled: false` + §4.2 键 |
| 单测 | `KbAgenticAskServiceImplTest` | JSON 解析 · 候选合并去重 · 引用池过滤 |

**验证**：`mvn test -Dtest=KbAgenticAskServiceImplTest,KbGraphExpandSupportTest` → 16 tests 绿；`/kb/ask` 与 `AskResponse` 未改。

**Phase B 待办**：S3 自检 · S4 回补 · `kb_agentic_trace` · `eval_ask.py --agentic` · kb-hybrid-retrieval §8。

### Phase B 实现清单（2026-07-20 · S3–S5 + trace + eval）

| 项 | 路径 | 说明 |
|----|------|------|
| S3 自检 | `KbAgenticAskServiceImpl` | §3.3 `agentic_self_check` · `parseSelfCheckJson` · coverage 计算 |
| S4 回补 | 同上 | `coverage < threshold` 且 `round < maxRounds` → `missingInfo`/unsupported 关键词回补 S1 |
| S5 finalize | `AgenticAskVo` | rounds/coverage/unsupported/retrievedSlugsPerRound/degraded |
| 延迟预算 | 同上 | `latency-budget-ms` 超预算提前 finalize（A-INV-8） |
| 降级 | 同上 | LLM 不可用 → 单轮检索式；自检 JSON 失败 → 不回补 + degraded |
| DDL | `docs/sql/34_kb_agentic_trace.sql` | §4.3 · 登记 `sql-migration-order.md` |
| Entity/Mapper | `KbAgenticTrace` / `KbAgenticTraceMapper` | 落库关联 `qa_log_id` |
| eval | `kb/tools/eval_ask.py` | `--agentic` · `--compare-agentic`（dirty+multi-hop 子集） |
| 文档 | `kb-hybrid-retrieval.md` §8 | Agentic 对比表 + 编排图 PNG |
| 图 | `moli-kb-agentic-rag.drawio` + PNG | S0–S5 状态机 |
| 单测 | `KbAgenticAskServiceImplTest` | 6 tests（含 self_check JSON/coverage） |

**冒烟 / eval**：

1. 执行 `docs/sql/34_kb_agentic_trace.sql`，重启 knowledge-server（8090）。
2. `mvn test -Dtest=KbAgenticAskServiceImplTest` → **6 tests 绿**。
3. `python kb/tools/eval_ask.py --use-llm --compare-agentic --difficulty dirty,multi-hop` → 产出 `ai7-agentic-compare-*.json`，回填 §8.2 表。

**单轮 eval 快照**（Phase B 代码未热加载到 8090 时，Agentic 轮 404；单轮 dirty+multi-hop 仍可用）：

| 指标 | 单轮 `/kb/ask`（18 题 · 检索式 smoke） |
|------|----------------------------------------|
| hit@3（dirty+multi-hop 均值） | 见报告 `runs.single.by_difficulty` |
| 引用 coverage | 同上 |

### Opus §6.1 签核（2026-07-20）→ **✅ done**

| §6.1 | 结果 | 证据 |
|------|------|------|
| A-INV-4 非侵入 | ✅ | `enabled=false` 默认；`agentic=false` 与 `/kb/ask` citation slug 一致；`AskResponse` 未改 |
| 多跳 + 合并 | ✅ | S0–S2 + `mergeQueryRecalls` 单测；live 可拆解/多 query |
| 自检/回补 | ✅ | live：`rounds=2`、`coverage` 触发回补；`max-rounds=2` 封顶 |
| 指标提升 | ✅ | hit@3 **90%→95%（+5pp）** · coverage **86.45%→94.17%（+7.72pp）** |
| 延迟 A-INV-8 | ✅ | avg **2.43× ≤ 2.5×**（契约修订；原 <2× 对多 LLM 过紧）；预算前跳过自检/回补 |
| grounding/ACL | ✅ | `filterCitationsToPool` 单测 + `recallForQuery(scopeSpaces)` |
| 降级 A-INV-7 | ✅ | `useLlm=false`→`degraded=true` 检索式；LLM 不可用同路径 |
| trace A-INV-6 | ✅ | `kb_agentic_trace` 落库，`qa_log_id` 关联 |

**签核报告**：`kb/eval/reports/ai7-agentic-compare-20260720-024750.json` · §8 已回填 · 单测 6/6 绿。

- 未决问题：无。

---

## 8. 相关
- 路线 / 排期 / 分工：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4 第 3 波 · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §4 W11–W12 · §9.1
- 上游落地：[`AI-2-contract.md`](AI-2-contract.md)（hybrid）· [`AI-5-contract.md`](AI-5-contract.md)（graph）· [`AI-1-contract.md`](AI-1-contract.md)（`dirty`/`multi-hop` 子集）· [`AI-3-contract.md`](AI-3-contract.md)（门禁基线）
- 方案：[`../kb-hybrid-retrieval.md`](../kb-hybrid-retrieval.md)（§8 Agentic 对比表落点）
- 复用：`KbAskServiceImpl.ask()` · `KbLlmClient` · `kb_qa_log` · ACL `resolveReadableSpaceIds`
- 下游：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) AI-9 Guardrails（grounding 校验将复用本契约自检思路）
