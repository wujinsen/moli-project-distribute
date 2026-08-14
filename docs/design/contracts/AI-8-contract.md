# AI-8 LLM 网关升级（多 provider 路由 + 语义缓存 + 成本看板）· 施工契约（Opus 产出，Composer 施工）

> **角色**：Opus（路由/缓存/降级策略负责人）产出，Composer 施工的**唯一契约**。
> **任务**：在既有 `KbLlmClient` + `KbLlmRuntime`（DB 优先 / yaml 兜底）之上加**路由层**与 **Redis 语义缓存**，失败自动切备用；调用仍落 `kb_llm_call_log`（含 `cacheHit`）；`KbOpsService` Dashboard 增成本/命中率卡片。
> **状态**：✅ **done** · 2026-07-20 Opus §4 签核 · Router failover + Redis 语义缓存 + Ops 成本/命中率 · 默认 router/cache 关零回归
> **主导**：🔵 **混合** —— **Opus 拍板「路由优先级 / 降级顺序 / 缓存键与阈值 / 看板字段语义」（§1/§2）**；Composer 铺量（Router/适配器、Redis 缓存、DDL 扩展、Ops 卡片、单测、文档）。
> **上游**：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4 第 4 波 AI-8 · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §5 W13–W14 · §9.1 AI-8 · [`../kb-llm-platform-settings.md`](../kb-llm-platform-settings.md)（T19 `KbLlmRuntime` / `kb_platform_llm_config`）· [`AI-3-contract.md`](AI-3-contract.md)（Dashboard 卡片 additive 模式）
> **现有落地（复用，勿重造）**：`KbLlmClient.chat(scene,spaceId,sys,user,modelOverride)` · `KbLlmRuntime` / `KbLlmEffectiveConfig` · `KbLlmCallLogService` + `kb_llm_call_log` · `KbOpsServiceImpl.buildLlmSummary` → `KbOpsLlmSummaryVo` · `StringRedisTemplate`（已有）· OpenAI 兼容 HTTP（`doChat`）

---

## 0. 契约边界（读我）

**本契约定义**：路由层定位与降级顺序、语义缓存键/相似度/TTL、配置键、`kb_llm_call_log` 扩展字段语义、Ops 看板 additive 字段、分 Phase 清单、验收与禁改范围。

**不在本契约内（交给 Composer）**：Router/Cache Java 骨架、provider 适配器拆分、`StringRedisTemplate` 读写样板、DDL 迁移落盘（`@sql-migration-baseline`）、`KbOps*Vo` 样板、单测、`application-dev.yml` 示例块、文档回填。

**红线**：Composer **不得**自行改 §1 路由优先级/降级顺序、§2 缓存阈值/TTL 默认、看板字段语义；**不得**改 `KbLlmClient.chat(...)` 既有重载签名（仅允许 additive 可选参数，且默认行为与现网一致）；**不得**改 `/kb/ask` 对外 VO。发现策略歧义 → 回 Opus 窗口改契约。

---

## 1. 架构决策（Opus 冻结）

### 1.1 定位：`KbLlmClient` 之内的路由 + 缓存，调用方无感

```
Ask / Ingest / Wiki / Agentic
        │  既有 chat(scene, spaceId, sys, user [, modelOverride])
        ▼
KbLlmClient.chat(...)                    # 对外签名不变（A-INV-4）
        │
        ├─① KbLlmSemanticCache.lookup   # kb.llm.cache.enabled；命中 → ②' 直接返回
        │
        ├─② KbLlmRouter.execute         # 主 provider → 备用链（§1.2）
        │      └─ ProviderAdapter.doChat (从现 doChat 抽出；OpenAI 兼容)
        │
        └─③ KbLlmCallLogService.record* # 含 cacheHit / estimatedCost / failover 标记
```

- **不新服务**：仍在 `moli-knowledge-server`（与 T19「LLM 放知识库」一致）；不抽独立 `moli-ai` 网关。
- **平台单例仍是权威主配置**：`KbLlmRuntime`（`kb_platform_llm_config` / yaml 兜底）= **primary**；备用链来自 `kb.llm.router.fallbacks`（yaml/env，**不**进 T19 Web 一期多 Key UI）。
- **Ask 降级不变**：路由层最终失败仍抛 `BaseException`；`KbAskServiceImpl` / Agentic 既有「LLM 不可用 → 检索式」路径继续生效（不在本契约重写 Ask）。

### 1.2 路由策略（可用性优先 · failover 链）

| 项 | 冻结决策 |
|----|----------|
| 模式 | **`failover`**（本波唯一实现）：按有序列表尝试；**不做**成本最优选路（`cost_aware` 仅预留键，默认关，本波不实现） |
| Primary | 始终为 `KbLlmRuntime.current()`（T19 平台配置）；若 `!usable()` → 直接失败（与现 `assertUsable` 一致），**不**用 fallback 绕过「平台未配置」 |
| Fallback | `kb.llm.router.fallbacks[]`：每项 `{provider, baseUrl, apiKeyEnv, model, timeoutSeconds?}`；`apiKey` **只从 env 读**（如 `KB_LLM_FALLBACK_1_KEY`），禁入库/禁写死 |
| 触发切备用 | **超时**（connect/read）· **HTTP 5xx** · **连接失败** · **HTTP 429**（可先按 `retry` 重试 primary，仍失败再切） |
| 不切备用 | HTTP **4xx**（除 429）：视为请求/鉴权问题，直接失败（避免错误请求打爆备用） |
| 全失败 | 抛现有 `BaseException("LLM 调用失败：…")`；调用方（Ask）自行降级检索式 |
| 延迟策略 | 单次尝试超时 = 该 provider 的 `timeoutSeconds`（fallback 可覆盖；默认继承 primary）；**整链**无额外全局超时（靠链长短 + 单次超时有界） |

**不变量（R-INV）**

| # | 不变量 |
|---|--------|
| R-INV-1 | Primary 不可用（平台未配置）≠ 走 fallback；fallback 仅在 **primary 已 usable 且本次调用失败** 时启用。 |
| R-INV-2 | 降级顺序严格按配置列表；Composer 不得重排或按成本动态插入。 |
| R-INV-3 | 每次上游尝试（含失败）可写 log；最终成功记录**实际生效**的 `provider/model`；failover 时 `failover=true`（见 §1.4）。 |
| R-INV-4 | `chat()` 对外签名与返回值语义不变；路由对调用方透明。 |

### 1.3 语义缓存（Redis）

**键（精确命中，默认路径）**

```
cacheKey = "kb:llm:cache:v1:" + sha256(
  normalize(userPrompt) + "\n" +
  scene + "\n" +
  model + "\n" +
  contextFingerprint
)
```

| 组件 | 规则（冻结） |
|------|----------------|
| `normalize(q)` | trim → 连续空白压成单空格 → NFKC → lower-case（中英混排：仅对 ASCII 做 lower） |
| `scene` | 传入的 scene；null → `"default"` |
| `model` | 解析后的实际 model（含 override） |
| `contextFingerprint` | `sha256(normalize(systemPrompt)).substring(0,16)` —— **system 变则缓存不命中**（防跨 prompt 串味） |
| 值 | JSON：`{answer, provider, model, createdAt}`；TTL = `kb.llm.cache.ttl-seconds` |

**近似命中（可选，默认关）**

| 项 | 冻结 |
|----|------|
| 开关 | `kb.llm.cache.approx-enabled` 默认 **false** |
| 向量 | 复用 AI-2 `kb-retrieval` sidecar `POST /embed`（或现有 embedding 客户端）；失败 → 退化为仅精确命中，不报错中断主路径 |
| 索引 | Redis：另键 `kb:llm:cache:vec:v1:{scene}:{model}` 为 **有界** 列表/ZSET（最多 `approx-max-entries`，默认 500；FIFO 淘汰）存 `{key, embedding}` |
| 判定 | cosine(queryEmb, entryEmb) ≥ `similarity-threshold`（默认 **0.92**）→ 命中该 entry 的 answer |
| 安全 | 近似命中**仍须** `contextFingerprint` 一致（同 system 指纹），避免不同系统提示串答案 |

**缓存命中与日志（强制）**

- 命中：返回缓存 answer；**仍写** `kb_llm_call_log`：`status=success`、`cache_hit=1`、`latency_ms`≈查 Redis 耗时、`provider/model`=缓存内记录值、`estimated_cost=0`。
- 未命中且上游成功：写答入 Redis；log `cache_hit=0` + 成本估算。
- 上游失败：不写缓存；log `fail`（与现网一致）。

### 1.4 `kb_llm_call_log` 扩展（additive DDL）

新增列（Composer 出 `docs/sql/35_kb_llm_call_log_ai8.sql` + 合基线）：

| 列 | 类型 | 说明 |
|----|------|------|
| `cache_hit` | tinyint(1) NOT NULL DEFAULT 0 | 是否语义缓存命中 |
| `failover` | tinyint(1) NOT NULL DEFAULT 0 | 是否经 fallback 成功（primary 失败后） |
| `prompt_tokens_est` | int NULL | 估算 prompt tokens（§1.5） |
| `completion_tokens_est` | int NULL | 估算 completion tokens |
| `estimated_cost_usd` | decimal(12,6) NULL | 估算成本（USD）；缓存命中为 0 |

> 既有列语义不动。Entity / `recordSuccess`/`recordFail` **扩展重载或可选参数**，旧调用点默认 `cacheHit=false, failover=false`。

### 1.5 成本估算（看板用，非计费权威）

```
tokens_est(text) = max(1, ceil(utf8_length(text) / 4))   # 粗算，冻结
estimated_cost_usd = (prompt_tokens_est * input_per_1k + completion_tokens_est * output_per_1k) / 1000
```

单价来自 `kb.llm.router.pricing.{provider}.{model}`，缺省用 `kb.llm.router.pricing.default`（`input-per-1k-usd` / `output-per-1k-usd`）。**不调用**供应商账单 API。

---

## 2. 配置键（冻结）

### 2.1 `kb.llm.router.*`

| 键 | 默认 | 说明 |
|----|------|------|
| `enabled` | `false` | 总开关；false → 行为与现网一致（仅 Runtime primary，无 fallback） |
| `mode` | `failover` | 本波只实现 `failover` |
| `retry` | `0` | primary 失败（含 429）时**同 provider** 重试次数（不含 fallback 切换） |
| `retry-backoff-ms` | `200` | 重试间隔 |
| `fallbacks` | `[]` | 备用列表（见 §1.2）；空 = 无降级链 |
| `pricing.default.input-per-1k-usd` | `0.001` | 缺省输入单价 |
| `pricing.default.output-per-1k-usd` | `0.002` | 缺省输出单价 |

`application-dev.yml` 示例（Composer 写入，密钥走 env）：

```yaml
kb:
  llm:
    router:
      enabled: false
      mode: failover
      retry: 0
      fallbacks:
        # - provider: deepseek
        #   base-url: https://api.deepseek.com
        #   api-key-env: KB_LLM_FALLBACK_1_KEY
        #   model: deepseek-chat
        #   timeout-seconds: 60
```

### 2.2 `kb.llm.cache.*`

| 键 | 默认 | 说明 |
|----|------|------|
| `enabled` | `false` | 语义缓存总开关（A-INV：默认关，零回归） |
| `ttl-seconds` | `3600` | 精确缓存 TTL |
| `approx-enabled` | `false` | 是否启用 embedding 近似命中 |
| `similarity-threshold` | `0.92` | 近似命中 cosine 下限（∈(0,1]） |
| `approx-max-entries` | `500` | 每 scene+model 向量索引上限 |
| `embed-timeout-ms` | `800` | 调 sidecar embed 超时；失败则仅精确路径 |

---

## 3. 分 Phase 施工清单（W13–W14）

### Phase A（W13）· `KbLlmRouter` + 适配器 + 降级

- Composer：从 `KbLlmClient.doChat` 抽出 `KbLlmProviderAdapter`（OpenAI 兼容）；`KbLlmClient.chat` 经 Router 调适配器。
- Composer：`KbLlmRouterProperties`（§2.1）+ `KbLlmRouter`（failover 链 · R-INV-1~3）。
- Composer：`recordSuccess/Fail` 支持 `failover` 标记（DDL 可与 Phase B 同迁或本 Phase 先加 `failover`/`cache_hit` 列）。
- Composer：单测 —— primary 成功；primary 5xx/超时 → fallback 成功；全失败抛错；`router.enabled=false` ≡ 现网。
- Composer：`application-dev.yml` 示例块（默认 router/cache 均 false）。
- **出口**：主 provider 挂（可 mock）→ 自动切备用；`chat()` 签名不变；Ask 冒烟仍可用。

### Phase B（W14）· 语义缓存 + 成本/命中率看板

- Composer：`KbLlmSemanticCache`（Redis 精确键 §1.3；可选 approx）。
- Composer：缓存命中仍写 `kb_llm_call_log`（`cache_hit=1`，成本 0）。
- Composer：成本估算写入 log（§1.5）。
- Composer：扩展 `KbOpsLlmSummaryVo`（**additive**，仿 AI-3 `retrievalQuality`）：

  | 字段 | 说明 |
  |------|------|
  | `cacheHitRate` | 窗口内 `cache_hit=1` / 成功调用 |
  | `estimatedCostUsd` | 窗口内 `estimated_cost_usd` 求和 |
  | `failoverCount` | 窗口内 `failover=1` 次数 |
  | `costTrend[]` | 按日：`date`、`estimatedCostUsd`、`cacheHits`、`calls`（仿 `KbOpsLlmCallTrendPointVo`） |

  现有 `llm.*` 字段语义**不动**；权限仍 `kb:ops:dashboard`。
- Composer：文档 —— `kb-llm-platform-settings.md` 增「AI-8 路由/缓存」小节；`KNOWLEDGE_API` Dashboard 字段表补一行（若有 llm 卡片说明）。
- **出口**：重复问命中缓存 + log 可见；Dashboard 可见成本与命中率；默认开关关时零回归。

---

## 4. 验收标准

- [x] **路由/降级可测（R-INV）**：`router.enabled=true` 且配置 fallback 时，mock/断 primary（超时或 5xx）→ 自动切备用成功；全失败 → `BaseException`；`enabled=false` 行为与升级前一致。
- [x] **缓存命中可观测**：相同/（若开 approx）近似问第二次 `chat` 命中；对应 `kb_llm_call_log.cache_hit=1` 且 `estimated_cost_usd=0`。
- [x] **Dashboard**：`GET /kb/ops/dashboard` 的 `llm` 卡片含 `cacheHitRate` / `estimatedCostUsd` / `costTrend`（或等价 additive 字段）；原字段不回归。
- [x] **对外契约**：`/kb/ask` 与 `AskResponse` 不变；`KbLlmClient.chat(...)` 既有重载签名不变（无破坏性改参）。
- [x] **凭据安全**：fallback api-key 仅 env；日志/异常无明文 key（延续 T19）。
- [x] **有界**：fallback 列表长度建议 ≤3（配置校验可 warn）；approx 索引有 `approx-max-entries` 封顶。

---

## 5. Composer 禁改范围（Do-Not-Touch）

- ❌ 自行改路由优先级、降级触发条件、fallback 顺序（R-INV-2）。
- ❌ 自行改缓存键公式、`similarity-threshold` / TTL 默认、或把 approx 默认打开。
- ❌ 改 `KbLlmClient.chat` 既有方法签名或迫使所有调用方改代码。
- ❌ 改 `/kb/ask` / `AskResponse` / T19 平台 LLM Web API 语义。
- ❌ 用 fallback 绕过「平台 LLM 未配置」（违反 R-INV-1）。
- ❌ 实现本波未批准的 `cost_aware` 动态选路。
- ❌ 在仓库硬编码 api-key / 打印 key。

---

## 6. 实现清单 + 未决问题（Composer 回填区）

> Composer 施工时在此追加「已实现类/接口清单」与「未决问题」；策略类未决**不得自行拍板**，回 Opus。

### Phase A 实现清单（W13 · 2026-07-20）

| 交付项 | 路径 / 说明 |
|--------|-------------|
| `KbLlmRouterProperties` | `config/KbLlmRouterProperties.java` · `kb.llm.router.*`（§2.1 键名对齐） |
| `KbLlmProviderAdapter` | `llm/KbLlmProviderAdapter.java` · 从 `KbLlmClient.doChat` 抽出；`chatLegacy` 保持 AI-8 前语义 |
| `KbLlmRouter` | `llm/KbLlmRouter.java` · primary（`KbLlmRuntime`）→ 有序 fallback · R-INV-1~3 |
| `LlmRetryableException` | `llm/LlmRetryableException.java` · 429/5xx/超时/连接失败 |
| `KbLlmRouterResult` | `llm/KbLlmRouterResult.java` · answer / provider / model / failover |
| `KbLlmClient` | `router.enabled=false` → `chatLegacy` 零回归；`true` → `KbLlmRouter.execute` |
| `KbLlmCallLogService` | `recordSuccess/Fail` 增 `failover` 默认重载（DDL 列 Phase B） |
| 配置示例 | `application-dev.yml` · `kb.llm.router.enabled: false` · `fallbacks: []` |
| 单测 | `KbLlmRouterTest` · primary 失败→fallback 成功 · 4xx 不切备用 · 全失败 · 无 fallback |
| 单测 | `KbLlmClientRouterTest` · `enabled=false` 不经 Router、走 `chatLegacy` |
| 单测 | `KbLlmRouterPropertiesTest` · fallback 过滤与 ≤3 封顶 |

**未实现（Phase B）**：`KbLlmSemanticCache` · `kb_llm_call_log` DDL（`35_kb_llm_call_log_ai8.sql`）· Ops 成本/命中率卡片。

- 未决问题：Phase B 前 `failover`/`cache_hit` 仅接口重载，未落库；Dashboard 仍用既有 `llm.*` 字段。

### Phase B 实现清单（W14 · 2026-07-20）

| 交付项 | 路径 / 说明 |
|--------|-------------|
| `KbLlmCacheProperties` | `config/KbLlmCacheProperties.java` · `kb.llm.cache.*`（§2.2） |
| `KbLlmSemanticCache` | `llm/KbLlmSemanticCache.java` · Redis 精确键 + 可选 approx（sidecar `/embed-query`） |
| `KbLlmPromptNormalizer` / `KbLlmCacheKeyBuilder` | §1.3 归一化与 `kb:llm:cache:v1:` 键 |
| `KbLlmCostEstimator` | §1.5 token/USD 粗算 → `kb_llm_call_log` |
| DDL | `docs/sql/35_kb_llm_call_log_ai8.sql` · `cache_hit`/`failover`/tokens/cost 列 |
| `KbLlmClient` | cache lookup → router/legacy → cache put；命中 log `cache_hit=1`、cost=0 |
| `KbOpsLlmSummaryVo` | additive：`cacheHitRate` · `estimatedCostUsd` · `failoverCount` · `costTrend[]` · 节省字段 |
| `KbRetrievalClient.embedQuery` + sidecar `/embed-query` | approx 路径（默认关） |
| 配置 | `application-dev.yml` · `kb.llm.cache.enabled: false` |
| 文档 | `kb-llm-platform-settings.md` §12 · `knowledge-ops-frontend.md` D3 字段表 |
| 单测 | `KbLlmSemanticCacheTest` · `KbLlmClientCacheTest` · normalizer/key builder |

### 冒烟结果（Phase B · 2026-07-20）

| 项 | 结果 |
|----|------|
| 单元测试 | `KbLlmRouterTest` + `KbLlmClientCacheTest` + `KbLlmSemanticCacheTest` 等 **18 tests 绿**（`mvn test -Dtest=…`） |
| 同问二次命中 | 逻辑：`cache.enabled=true` + 相同 scene/system/user → 第二次 `lookup` 命中、跳过 provider（单测 `KbLlmClientCacheTest`） |
| Dashboard 命中率 | `buildLlmSummary` 聚合 `cacheHitRate` / `costTrend`（需 DDL 35 + 真实 Redis/调用后 E2E 签核） |
| 零回归 | `cache.enabled=false` 且 `router.enabled=false` 时与 AI-8 前路径一致 |

- 未决问题：生产 E2E 需 DBA 执行 `35_kb_llm_call_log_ai8.sql` + Redis 可用；approx 依赖 sidecar `/embed-query` 与 `kb.search.vector.base-url`。

### Opus §4 签核（2026-07-20）→ **✅ done**

| §4 | 结果 | 证据 |
|----|------|------|
| 路由/降级 | ✅ | `KbLlmRouterTest`：primary 503→fallback 成功 · 4xx 不切 · 全失败抛错 · `enabled=false` 仅 primary；`KbLlmClientRouterTest` 走 `chatLegacy` |
| 缓存 + log | ✅ | `KbLlmClientCacheTest` 二次命中；`recordSuccess(..., cacheHit=true, cost=0)`；DDL `35_kb_llm_call_log_ai8.sql` |
| Dashboard | ✅ | `KbOpsLlmSummaryVo` additive + `buildLlmSummary` 填 `cacheHitRate`/`estimatedCostUsd`/`costTrend` |
| 调用方无破坏 | ✅ | `chat(scene,spaceId,sys,user[,model])` 签名未改；Ask/Ingest/Wiki/Agentic 仍原调用 |
| 凭据 / 有界 | ✅ | `api-key-env`；`normalizedFallbacks` ≤3；approx 默认关 + `approx-max-entries` |

**单测**：Router/Cache/Client/CallLog/Ops 相关 **25 tests 绿**。默认 `router.enabled=false`、`cache.enabled=false` 零回归。

---

## 7. 相关

- 路线 / 排期：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4 第 4 波 · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §5 W13–W14 · §9.1
- 平台 LLM：[`../kb-llm-platform-settings.md`](../kb-llm-platform-settings.md) · `KbLlmRuntime` · `kb_platform_llm_config`
- 看板模式：[`AI-3-contract.md`](AI-3-contract.md) §3.1 additive 卡片
- 代码：`KbLlmClient` · `KbLlmCallLogServiceImpl` · `KbOpsServiceImpl.buildLlmSummary` · `docs/sql/18_kb_llm_call_log.sql`
- 下游：AI-9 Guardrails / AI-10 DeepResearch 继续走升级后的 `KbLlmClient`（自动受益路由与缓存）
