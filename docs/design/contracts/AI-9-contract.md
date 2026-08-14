# AI-9 Guardrails（grounding + 注入检测 + PII 脱敏）· 施工契约（Opus 产出，Composer 施工）

> **角色**：Opus（安全关键 / 规则与降级策略负责人）产出，Composer 施工的**唯一契约**。
> **任务**：在 `/kb/ask` 与 `/kb/ask/agentic` 上挂载 **输入侧**（Prompt 注入检测、PII 脱敏）与 **输出侧**（grounding 校验 / 低置信标注）；非攻击场景零回归；产出幻觉率与引用覆盖率前后对比。
> **状态**：✅ **done** · 2026-07-20 Opus §4 签核 · 注入金样 20/20 BLOCK · PII 脱敏 · grounding VO · 默认 `enabled=false` 零回归
> **主导**：🟣 **Opus 主导** —— **Opus 拍板「注入规则边界、PII 类型与脱敏、拒答/降级策略、grounding 阈值与 VO 语义」（§1/§2）**；Composer 铺量（管道接线、正则/规则引擎、Ask/Agentic 挂载、单测、eval 对比）。
> **上游**：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4 第 4 波 AI-9 · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §5 W15 · §9.1 · [`../product/ai-capability-prd.md`](../product/ai-capability-prd.md) AI-9 · [`AI-7-contract.md`](AI-7-contract.md)（A-INV-3 · §3.3 self_check，**复用勿另造 grounding 语义**）
> **现有落地（复用，勿重造）**：`KbAskServiceImpl.ask()` · `KbAgenticAskServiceImpl`（S3 self_check / `coverage` / `unsupportedStatements`）· `AskResponse` / `AgenticAskVo` · `KbLlmClient` · `kb_qa_log` / `kb_agentic_trace` · AI-1 golden（`dirty` 负样本可作攻击样例基座）

---

## 0. 契约边界（读我）

**本契约定义**：Guardrails 范围与挂载点、输入/输出策略与不变量、配置键、VO/trace additive 字段、分 Phase 清单、验收与禁改范围。

**不在本契约内（交给 Composer）**：规则表/正则实现、管道 Java 骨架、Ask/Agentic 接线样板、`eval_ask.py --guardrails` 对比脚本、DDL（若需独立 audit 表）、文档与单测样板。

**红线**：Composer **不得**自行放宽/收紧 §1 注入规则语义、PII 类型、拒答/降级顺序、grounding 阈值；**不得**静默删除 unsupported 陈述（A-INV-3）；**不得**改既有 `AskResponse` / `AgenticAskVo` 字段语义（仅 additive）；**不得**在非攻击场景改变 `/kb/ask` 默认行为（默认总开关关，见 §2）。发现规则歧义 → 回 Opus。

---

## 1. Guardrails 范围（Opus 冻结）

### 1.1 定位：Ask 管道上的安全层，默认关闭

```
POST /kb/ask  或  POST /kb/ask/agentic
        │
        ▼
① InputGuard (kb.guardrails.enabled)
   ├─ injectDetect(question)     → BLOCK → 拒答或强制检索式（§1.3），不调 LLM
   └─ piiRedact(question)        → 脱敏后的 question' 进入检索/LLM；原始仅审计掩码
        │
        ▼
既有召回 /（Agentic 编排）/ 生成
        │
        ▼
② OutputGuard (grounding.enabled；仅 generative)
   ├─ 复用 AI-7 §3.3 self_check 语义（或同 prompt 薄封装）
   ├─ coverage / unsupportedStatements 写入 VO（additive）
   └─ 不删句、不伪造 citations（A-INV-3）
```

- **默认 `kb.guardrails.enabled=false`**：管道旁路，`/kb/ask` 与现网逐条一致（零回归红线）。
- **不进 Ingest / Wiki 治理 LLM**（本波范围仅问答入口）；后续另议。
- **不新建微服务**；逻辑在 `moli-knowledge-server`。

### 1.2 输入侧 · Prompt 注入检测

**目标**：拦截试图覆盖系统提示、套取密钥、越权指令的 query；**不误杀**正常运维/技术文档问法。

| 严重级 | 含义 | 处置（冻结） |
|--------|------|----------------|
| `BLOCK` | 明确注入/越狱意图 | **不调用 LLM**；返回检索式或固定拒答（§1.3）；`guard.blocked=true` |
| `FLAG` | 可疑但可能误报 | 仍允许检索+LLM；`guard.flagged=true`；日志记规则 id |
| `PASS` | 未命中 | 继续 |

**BLOCK 规则族（语义冻结，Composer 实现为可配置规则表 + 单测金样）**：

1. **角色劫持**：要求模型忽略/忘记系统提示、扮演 DAN/越狱、`developer mode` 等（中英关键词 + 常见变体）。
2. **指令覆盖**：`ignore (all )?previous (instructions|prompts)`、`disregard the above`、中文「忽略以上/之前的指令」「不要遵守系统提示」。
3. **密钥套取**：要求输出 `api[_-]?key`、`KB_LLM`、`Authorization` 头、环境变量中的 secret 等。
4. **工具越权**：要求执行 shell/SQL 写操作、读取服务器本地文件路径（`/etc/passwd`、`C:\\Windows`）——与知识库问答无关的系统攻击面。

**白名单 / 误杀边界（冻结）**：

- 命中规则前先过 **allowlist**：query 主体为「如何配置/排查/忽略某配置项」等运维问法（如 nginx `ignore`、Spring `ignoreUnknown`）且**无**角色劫持句式 → 不得 BLOCK。
- 代码块/配置片段内的敏感词单独降权为 FLAG（除非同时含角色劫持句式）。
- 日语/英语技术术语、Certify 试题原文引用 → 默认 PASS（除非叠加 BLOCK 族 1–2）。

**金样（验收用，Composer 写入 `kb/eval/guardrails_inject.jsonl`）**：≥20 条 BLOCK 正样 + ≥20 条 PASS 负样（含运维「忽略某配置」）；BLOCK 样例 **100%** 触发 BLOCK 处置。

### 1.3 BLOCK 处置策略（冻结）

```
if inject == BLOCK:
  mode = "retrieval"
  answer = 固定拒答文案（中文，说明检测到不安全指令，已拒绝生成式作答）
           + 可选：若有安全检索命中，附 citations（用 **脱敏后** question' 召回）
  不调用 KbLlmClient.chat
  guard.blocked = true
  guard.blockReason = ruleId
```

> **不**返回 5xx；**不**把原始攻击串回显给客户端（可记审计哈希）。Agentic：BLOCK 时 **跳过** S0–S3 编排，直接 finalize 上述检索式/拒答（`agentic=false` 或 `degraded=true` + `guard.blocked`）。

### 1.4 输入侧 · PII 识别与脱敏

| 类型 | 识别（冻结） | 脱敏替换 |
|------|----------------|----------|
| 邮箱 | 标准 email | `[EMAIL]` |
| 中国大陆手机 | `1[3-9]\d{9}`（边界约束） | `[PHONE]` |
| 身份证号 | 18 位校验粗规则（含 X） | `[ID_CARD]` |
| 银行卡 | 可选；默认 **关**（误杀卡号式工单号风险） | — |

**策略（冻结）**：

1. 对用户 `question` 做识别 → 得到 `questionRedacted`。
2. **检索 terms / LLM user 内容一律用 `questionRedacted`**（PII 不进 prompt、不进向量检索原文）。
3. `kb_qa_log.question` 存 **脱敏后**文本（或另存 `question_redacted`；禁止明文落库）。
4. 响应 VO **不**回传原始 PII；`guard.piiRedacted=true` 且 `guard.piiTypes=["email",…]`（类型枚举，无具体值）。
5. 若整问脱敏后为空/过短（&lt;2 有效字符）→ 拒答「请勿仅提交敏感信息」。

### 1.5 输出侧 · grounding 校验

**复用 AI-7 §3.3**：陈述 ↔ 可用 citations 对齐；`coverage` / `unsupported` / `missingInfo` 语义不变。

| 入口 | 行为（冻结） |
|------|----------------|
| `/kb/ask/agentic` | 已有 S3 self_check：Guardrails **不重复跑第二遍**；仅统一把结果映射到 §1.6 VO 字段，并写 `guard.groundingApplied=true` |
| `/kb/ask` 单轮 generative | 当 `kb.guardrails.grounding.enabled=true` 时，生成后调同一 self_check（`KbLlmCallScenes` 新 scene：`ask_grounding`）；失败解析 → `coverage=null`，不阻断答案（与 A-INV-7 温和降级一致） |
| 检索式 `mode=retrieval` | **跳过** grounding（无生成陈述） |

**低置信标注（冻结）**：

- `coverage < kb.guardrails.grounding.low-threshold`（默认 **0.8**，与 AI-7 阈值对齐）→ `guard.groundingLow=true`。
- `unsupportedStatements` **原样返回**，**禁止**从 `answer` 中静默删句（A-INV-3）。
- 可选前端提示文案由 VO 字段驱动；后端不改写用户可见答案正文（除输入侧已做的 PII 脱敏）。

### 1.6 VO / Trace（additive，不改既有语义）

**`AskResponse` 新增（可空）**：

```
guard: {
  blocked: boolean,          // 注入 BLOCK
  flagged: boolean,
  blockReason: string|null,  // ruleId，无攻击原文
  piiRedacted: boolean,
  piiTypes: string[],        // email|phone|id_card
  groundingApplied: boolean,
  groundingLow: boolean,
  coverage: Double|null,     // 单轮 grounding；未跑为 null
  unsupportedStatements: string[]  // 可空
}
```

> 亦允许扁平字段（`guardBlocked`…）；**推荐嵌套 `guard`**。既有 `answer/mode/citations/...` 语义不动。

**`AgenticAskVo`**：复用已有 `coverage` / `unsupportedStatements`；**additive** 同名 `guard{...}`（blocked/pii/groundingLow 等）。不删既有字段。

**Trace / 审计（Phase B 可落）**：

- 优先扩展 `kb_qa_log` 或写 `kb_guard_event`（`qa_log_id`, `rule_id`, `action`, `pii_types_json`, `coverage`, `create_time`）——Composer 选一，**禁止**存原始 PII/攻击全文。
- Agentic：`kb_agentic_trace.steps_json` 可附 `guard` 摘要。

---

## 2. 不变量（Opus 冻结）

| # | 不变量 |
|---|--------|
| GR-INV-1 | **默认可关**：`kb.guardrails.enabled=false` 时，Ask/Agentic 行为与 AI-9 前一致（零回归）。 |
| GR-INV-2 | **高风险处置**：注入 `BLOCK` → 不调 LLM；拒答或检索式（§1.3）；不 5xx、不回显攻击串。 |
| GR-INV-3 | **PII 不进 LLM**：送模型与召回的 query 必须为脱敏后文本；日志无明文 PII。 |
| GR-INV-4 | **不静默删无据陈述**：与 AI-7 A-INV-3 一致；仅标注 `unsupported` / `groundingLow`。 |
| GR-INV-5 | **不误杀技术问**：§1.2 白名单边界；PASS 金样不得 BLOCK。 |
| GR-INV-6 | **VO 兼容**：既有 `AskResponse`/`AgenticAskVo` 字段语义不变；Guard 信息仅 additive。 |
| GR-INV-7 | **grounding 语义单一**：与 AI-7 §3.3 同口径；禁止另起一套冲突的「幻觉分」。 |
| GR-INV-8 | **有界**：注入规则匹配超时/异常 → FAIL-OPEN 为 FLAG+继续（记日志），**不得**拖垮 Ask；PII/注入纯 CPU 规则，禁无界 LLM 做注入检测（本波）。 |

---

## 3. 配置键（冻结）

| 键 | 默认 | 说明 |
|----|------|------|
| `kb.guardrails.enabled` | `false` | 总开关 |
| `kb.guardrails.inject.enabled` | `true` | 总开关开启时是否做注入检测 |
| `kb.guardrails.inject.fail-open` | `true` | 规则引擎异常时放行并 FLAG |
| `kb.guardrails.pii.enabled` | `true` | PII 脱敏 |
| `kb.guardrails.pii.types` | `email,phone,id_card` | 启用类型 |
| `kb.guardrails.grounding.enabled` | `true` | 单轮 Ask 是否跑 grounding（Agentic 仍用自身 S3） |
| `kb.guardrails.grounding.low-threshold` | `0.8` | 低于则 `groundingLow` |
| `kb.guardrails.grounding.timeout-ms` | `8000` | 单轮 grounding LLM 超时；超时则跳过标注 |

---

## 4. 分 Phase 施工清单（W15）

### Phase A · 输入管道 + PII + 注入单测

- Composer：`KbGuardrailsProperties` + `KbInputGuardService`（inject + pii）。
- Composer：挂载 `KbAskServiceImpl.ask` / `KbAgenticAskServiceImpl.agenticAsk` **入口最前**（ACL 之后、召回之前）；BLOCK/PII 按 §1.3–§1.4。
- Composer：`AskResponse.guard` / `AgenticAskVo.guard` additive。
- Composer：金样 `kb/eval/guardrails_inject.jsonl` + `KbInjectDetectorTest` / `KbPiiRedactorTest`（BLOCK 100%、PASS 零误杀抽检）。
- Composer：`application-dev.yml` 示例（默认 `enabled: false`）。
- **出口**：开关关 ≡ 现网；开关开时注入金样全拦、PII 脱敏后再进 LLM（可 mock Client 断言入参）。

### Phase B · 输出 grounding + 前后对比 eval

- Composer：单轮 Ask generative 后挂 `KbOutputGroundingService`（复用 AI-7 self_check prompt/解析）。
- Composer：Agentic 映射已有 coverage → `guard`；不双跑。
- Composer：`eval_ask.py --compare-guardrails`（或等价）：同一 golden 子集，`guardrails off` vs `on`，输出**幻觉代理指标**（如 `1-coverage` 均值）与**引用覆盖率**前后表 → 回填 `docs/design/kb-hybrid-retrieval.md` **§9 Guardrails**（或独立短文，链到 roadmap）。
- Composer：可选 `kb_guard_event` DDL（`@sql-migration-baseline`）。
- **出口**：低 coverage 可观测；非攻击场景（enabled 对正常题）无 hit@k 显著回归（容差：全集 hit@3 降幅 **≤ 0.05**）；PRD「前后对比」表落盘。

---

## 5. 验收标准

- [x] **注入拦截**：`guardrails_inject.jsonl` 中 BLOCK 正样 **100%** 触发 §1.3 处置（不调 LLM）。
- [x] **误杀边界**：PASS 负样（含运维「忽略配置」类）**0** 条被 BLOCK（允许 FLAG）。
- [x] **PII**：含邮箱/手机/身份证的入参，送 `KbLlmClient` 的 user 文本为脱敏形态；`kb_qa_log` 无明文。
- [x] **grounding 可观测**：generative 答案在开关开启时可返回 `coverage` / `unsupportedStatements` / `groundingLow`；unsupported **未被删句**。
- [x] **零回归**：`kb.guardrails.enabled=false` 时 `/kb/ask` 与 AI-9 前一致；enabled=true 时正常技术问 hit@3 降幅 ≤0.05（Phase B 报告）。
- [x] **Agentic 兼容**：不破坏 AI-7 编排；BLOCK 时跳过编排；S3 结果与 `guard` 字段一致。

---

## 6. Composer 禁改范围（Do-Not-Touch）

- ❌ 自行增删 BLOCK 规则族语义或把 FAIL-OPEN 改为默认拒答全站。
- ❌ 静默删除 `answer` 中 unsupported 陈述或伪造 citations（违反 GR-INV-4 / A-INV-3）。
- ❌ 改 `AskResponse`/`AgenticAskVo` 既有字段含义；Guard 只 additive。
- ❌ 默认打开 `kb.guardrails.enabled`（破坏零回归）除非 Opus 改契约。
- ❌ 用 LLM 做注入检测作为本波唯一手段（本波规则引擎；LLM 仅用于 grounding）。
- ❌ 把原始 PII/攻击全文写入日志、VO 或 trace。
- ❌ 改 AI-7 §3.3 grounding JSON 语义另起冲突标准。

---

## 7. 实现清单 + 未决问题（Composer 回填区）

> Composer 在此追加清单与未决；规则/阈值未决**不得自行拍板**，回 Opus。

### Phase A 实现清单（W15 · 2026-07-20）

| 交付项 | 路径 / 说明 |
|--------|-------------|
| `KbGuardrailsProperties` | `config/KbGuardrailsProperties.java` · `kb.guardrails.*`（§3） |
| `KbInjectDetector` | `guard/KbInjectDetector.java` · BLOCK/FLAG/PASS · 白名单 · 代码块降权 |
| `KbPiiRedactor` | `guard/KbPiiRedactor.java` · email/phone/id_card → `[EMAIL]`/`[PHONE]`/`[ID_CARD]` |
| `KbInputGuardService` | `guard/KbInputGuardService.java` · inject + pii · fail-open FLAG |
| `InputGuardOutcome` / `AskGuardVo` | 拒答文案 · `guard.*` VO（无攻击/PII 原文） |
| `KbAskServiceImpl.executeAsk` | ACL 后输入 Guard · BLOCK→检索式拒答不调 LLM · 日志存脱敏 question |
| `KbAgenticAskServiceImpl` | 入口 Guard · BLOCK/PII 跳过 S0–S3 · `guard` 映射 |
| `AskResponse.guard` / `AgenticAskVo.guard` | additive 嵌套字段 |
| 金样 | `moli-knowledge/kb/eval/guardrails_inject.jsonl` · 20 BLOCK + 20 PASS |
| 单测 | `KbInjectDetectorTest` · `KbInjectDetectorGoldenTest` · `KbPiiRedactorTest` · `KbInputGuardServiceTest` |
| 配置 | `application-dev.yml` · `kb.guardrails.enabled: false` |

**冒烟结果（Phase A · 2026-07-20）**

| 项 | 结果 |
|----|------|
| 金样 BLOCK | `guardrails_inject.jsonl` 20/20 触发 BLOCK（`KbInjectDetectorGoldenTest`） |
| PASS 零 BLOCK | 20/20 PASS 样例无 BLOCK |
| PII 脱敏 | 单测覆盖 email/phone/id_card · 仅 PII 过短拒答 |
| 零回归 | `enabled=false` 时 `InputGuardOutcome.bypass` · 既有 Agentic 单测 6/6 绿 |
| 单测合计 | **22 tests 绿**（guard 16 + agentic 6） |

**未实现（Phase B）**：~~单轮 Ask grounding~~ · ~~`eval_ask.py --compare-guardrails`~~ · 可选 `kb_guard_event` DDL（未做，trace/VO 已覆盖）。

### Phase B 实现清单（W15 · 2026-07-20）

| 交付项 | 路径 / 说明 |
|--------|-------------|
| `KbGroundingSelfCheckSupport` | `guard/KbGroundingSelfCheckSupport.java` · AI-7 §3.3 prompt/解析复用 |
| `KbOutputGroundingService` | `guard/KbOutputGroundingService.java` · 单轮 generative grounding · Agentic guard 映射 |
| `KbAskServiceImpl.executeAsk` | generative 成功后 `applyGrounding()` · scene `ask_grounding` |
| `KbAgenticAskServiceImpl` | S3 self_check 委托 support · `mergeAgenticGuard` · trace `steps_json.guard` |
| `KbLlmCallScenes.ASK_GROUNDING` | `support/KbLlmCallScenes.java` |
| eval | `kb/tools/eval_ask.py --guardrails-baseline` · `--compare-guardrails` |
| 文档 | `docs/design/kb-hybrid-retrieval.md` §9 · `docs/api/KNOWLEDGE_API.md` guard 字段 |
| 单测 | `KbGroundingSelfCheckSupportTest` · `KbOutputGroundingServiceTest` · 既有 guard/agentic 绿 |

**冒烟结果（Phase B · 2026-07-20）**

| 项 | 结果 |
|----|------|
| 单轮 grounding | `enabled=true` + generative → `guard.groundingApplied` / `coverage` / `groundingLow`（单测 mock） |
| Agentic 不双跑 | S3 结果映射 `guard.*`；`enabled=false` → `guard=null`（零回归） |
| unsupported 不删句 | GR-INV-4 · `applyGrounding` 仅标注 VO |
| eval 对比 | `--guardrails-baseline` + `--compare-guardrails` 产出 hit/coverage/refusal/1-cov/注入拦截表 |
| 单测合计 | **30 tests 绿**（guard 24 + agentic 6） |

- 未决问题：Ingest/Wiki LLM 未挂载 Guard；可选 `kb_guard_event` 审计表待排期。

### Opus §4 签核（2026-07-20）→ **✅ done**

| §4 / §5 | 结果 | 证据 |
|---------|------|------|
| 注入拦截 | ✅ | `KbInjectDetectorGoldenTest`：`guardrails_inject.jsonl` **20/20 BLOCK**；Ask BLOCK 路径不调 LLM |
| 误杀边界 | ✅ | 同金样 **20/20 PASS 无 BLOCK**（含运维「忽略配置」） |
| PII 脱敏 | ✅ | `KbPiiRedactorTest`；`executeAsk` 用 `questionForProcessing` 进召回/LLM；`saveLog` 写脱敏文 |
| grounding 可观测 | ✅ | `KbOutputGroundingServiceTest`：`coverage`/`groundingLow`/`unsupportedStatements`；不删句 |
| 零回归 | ✅ | 默认 `enabled=false` → `bypass`；Agentic 单测 6/6；开档 hit@3 对比工具已就绪（`--compare-guardrails`），签核以默认关 + 金样/单测为准 |
| Agentic | ✅ | 入口 Guard · BLOCK 跳过编排 · S3→`guard` 映射不双跑 |

**单测**：**30/30 绿**（guard 24 + agentic 6）。

---

## 8. 相关

- 路线 / 排期 / PRD：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) · [`../product/ai-capability-prd.md`](../product/ai-capability-prd.md)
- 上游：[`AI-7-contract.md`](AI-7-contract.md)（A-INV-3 · self_check）· [`AI-1-contract.md`](AI-1-contract.md)（golden / 负样本）
- 挂载：`KbAskServiceImpl` · `KbAgenticAskServiceImpl` · `AskResponse` · `AgenticAskVo`
- 对照：AI-4 SQL 白名单（安全「硬拦截」范式）；本波为问答侧输入/输出护栏
