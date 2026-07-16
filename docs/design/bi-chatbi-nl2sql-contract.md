# AI-4 ChatBI / NL2SQL · 施工契约（Opus 产出，Composer 施工）

> **角色**：本文件由 Opus（架构与安全负责人）产出，作为 Composer 施工的**唯一契约**。
> **任务**：AI-4 ChatBI / NL2SQL Agent（bi-server 0→1）
> **状态**：contract · 2026-07-17 · 未开工
> **上游**：[`ai-capability-roadmap.md`](ai-capability-roadmap.md) §4 第 2 波 · [`bi-chatbi-nl2sql.md`](bi-chatbi-nl2sql.md)（技术设计）· [`ai-capability-schedule.md`](ai-capability-schedule.md) §9（Opus/Composer 分工）
> **运行时**：`moli-ai-server`（服务名 `bi-server`，HTTP **1128** / Dubbo **20883**，网关 `/BiServer/**` StripPrefix=1）
> **架构图**（复用，勿重画）：[`../diagrams/png/moli-bi-chatbi-flow.png`](../diagrams/png/moli-bi-chatbi-flow.png) · 源 [`../diagrams/moli-bi-chatbi-flow.drawio`](../diagrams/moli-bi-chatbi-flow.drawio)

---

## 0. 契约边界（读我）

**本契约只定义**：对外/对内接口签名、DTO 形状、错误码语义、算法与节点划分、prompt 草案、安全不变量、验收要点。

**不在本契约内（交给 Composer，按 `moli-order`/`moli-knowledge` 现有模式落地）**：建表 DDL、Mapper/Entity/VO 样板类、配置类（`@ConfigurationProperties`）、Spring 接线、只读数据源与缓存 Bean、`bi-agent` FastAPI 脚手架、图表序列化、`nl2sql_testset.jsonl` 录入、CI。

**红线**：Composer **不得**改动本契约中的 §3 安全校验逻辑、§1 接口签名、§2 节点边界。发现歧义或安全漏洞 → 回 Opus 窗口改契约，不自行拍板。

---

## 1. 接口契约

### 1.1 对外 REST（Java，`bi-server`，经网关 `/BiServer/**`）

统一返回 `MoliResult<T>`（`com.moli.common.core.MoliResult`）。三个端点：

| # | 方法 | 路径 | 权限码 | 说明 |
|---|------|------|--------|------|
| A | POST | `/bi/chat/ask` | `bi:chat:query` | 自然语言问数 → SQL+结果+图表+解读（支持 SSE） |
| B | GET | `/bi/chat/trace/{traceId}` | `bi:chat:trace` | 查看单次问答决策链路（越权隔离，见 INV-11） |
| C | GET | `/bi/chat/schema` | `bi:chat:query` | 返回可查询表/列白名单（**脱敏**，见 INV-14） |

#### A. `POST /bi/chat/ask`

**Request** `BiChatAskRequest`：

| 字段 | 类型 | 必填 | 约束 |
|------|------|------|------|
| `sessionId` | String | 否 | 空则服务端生成；仅用于同会话轻量上下文与 trace 归组 |
| `question` | String | 是 | 去空白后 1..500 字符；超长/空 → `10601` |
| `stream` | Boolean | 否 | 默认 `false`；`true` 走 SSE（见下 SSE 契约） |
| `maxRows` | Integer | 否 | 用户期望行数；服务端按 `bi.chat.max-rows` 钳制（见 INV-7），不可放大 |

**Response（非流式）** `MoliResult<BiChatAskVo>`：

```
BiChatAskVo {
  String traceId;              // 必返回，任何 status 都要能回溯 bi_chat_trace
  String sessionId;
  String status;              // SUCCESS | REJECTED | ERROR
  String sql;                 // 最终执行 SQL；REJECTED/ERROR 时为 null
  List<BiColumnVo> columns;   // 结果列元信息
  List<Map<String,Object>> rows; // 结果行（已受 LIMIT 约束）
  BiChartVo chart;            // 图表建议；无合适图表时 type=table/none
  String explanation;         // 自然语言解读；REJECTED 时为拒答说明
  String rejectCode;          // status=REJECTED 时必填，取值见 §1.3 拒答码
  String rejectReason;        // 人类可读拒答原因（不含表结构/连接细节）
  Integer rowCount;
  Long latencyMs;
  Integer retry;              // 自纠错实际次数
}
BiColumnVo   { String name; String type; String label; }
BiChartVo    { String type;  // bar|line|pie|table|none
               String x; List<String> y; String title; }
```

**关键约定**：
- **校验拒答 = 业务成功包**：LLM 生成后被 §3 安全校验拦下的，返回 `MoliResult.success` + `status=REJECTED` + `rejectCode`（chat UI 可优雅渲染"无法安全回答"）。
- **请求级失败 = MoliResult 错误码**：空问题/无权限/sidecar 不可用等，返回 `MoliResult.error(code)`（见 §1.3）。
- 二者都必须已写入 `bi_chat_trace`（INV-12）。

**SSE 契约（`stream=true`）**：`text/event-stream`，事件名 + JSON data：

| event | data | 时机 |
|-------|------|------|
| `stage` | `{stage, traceId}` | 阶段推进：`schema`→`sql`→`validate`→`execute`→`summarize` |
| `sql` | `{sql}` | 通过校验、执行前 |
| `chart` | `BiChartVo` | 图表建议就绪 |
| `token` | `{delta}` | 解读文案逐 token |
| `done` | `BiChatAskVo` | 终态全量对象（与非流式一致） |
| `error` | `{code, message}` | 请求级失败；拒答仍走 `done`（status=REJECTED） |

#### B. `GET /bi/chat/trace/{traceId}`

`MoliResult<BiChatTraceVo>`：

```
BiChatTraceVo {
  String traceId; String sessionId; Long userId;
  String question; String finalSql; String status;
  String rejectCode; String rejectReason;
  Integer rowCount; Long latencyMs; Integer retry;
  List<BiTraceStep> steps;   // 决策链路
  String createdAt;
}
BiTraceStep {
  String node;               // retrieve_schema|generate_sql|validate|execute|summarize
  String outcome;            // ok|rejected|error|retry
  String detail;             // 摘要（脱敏，禁堆栈/连接串）
  Long costMs;
}
```

**越权隔离**：调用者仅可查 `user_id == 当前登录用户` 的 trace；否则 `10612`。持 `bi:chat:trace:all`（或平台超管）方可跨用户查看（INV-11）。

#### C. `GET /bi/chat/schema`

`MoliResult<List<BiSchemaTableVo>>`：

```
BiSchemaTableVo   { String table; String comment; List<BiSchemaColumnVo> columns; }
BiSchemaColumnVo  { String name; String type; String comment; }
```

只返回 `bi.chat.allow-tables` 白名单表，且**剔除**列黑名单命中列（INV-6/14）。此接口是 UI/agent 了解可查范围的**唯一出口**，禁止对外暴露 `information_schema` 直查。

### 1.2 对内契约：Java 壳 ↔ `bi-agent`（Python FastAPI sidecar）

> **编排归属（架构决策）**：**Java 是 conductor（外层控制流 + 唯一安全裁决 + 唯一 SQL 执行者）**；`bi-agent` 无状态、仅提供"图节点"HTTP 能力。理由：安全单一真相（INV-2）、sidecar 可重启/降级（INV-15）、Python 永不碰执行。retry 计数与循环由 Java 持有并回传给 agent 作上下文。

两个内部端点（仅内网，走 `bi.agent.base-url`，**不经网关、不对外**）：

**① `POST /agent/generate`** — 检索 schema + 生成候选 SQL（含重试上下文）

Request：
```
{ sessionId, question,
  retry,            // 当前第几次（0 起），由 Java 传入
  priorSql,         // 上一轮候选（retry>0 时）
  priorError }      // 上一轮 Java 校验/执行错误（retry>0 时）
```
Response：
```
{ draftSql,        // 单条 SELECT 候选；无法生成 → draftSql=null
  usedTables[],    // agent 声明引用到的表（供 Java 交叉核对，非最终裁决）
  schemaDigest,    // 本轮喂给 LLM 的表/列摘要（写 trace）
  refusal }        // 语义层判定"不可答"（如要求写操作）→ refusal!=null，Java 直接 REJECTED
```

**② `POST /agent/explain`** — 结果解读 + 图表建议

Request：`{ sessionId, question, sql, columns[], rowsSample[], rowCount }`（rowsSample 为截断样本，控 token）
Response：`{ explanation, chart:{type,x,y,title} }`

**降级**：任一内部端点超时/连接失败 → Java 返回 `10602`，**不半执行**、不缓存、照写 trace（status=ERROR）。

### 1.3 错误码（BI ChatBI 专用块 `106xx`）

> Composer 在 `moli-ai-server` 建 `BiChatResponseCode` 枚举承载下表；**码值与语义由本契约冻结**，不得改动。通用参数/鉴权错误沿用 `ResponseCodeEnums`（`10003/10004/10006/10009`）。

**请求级错误（`MoliResult.error`）**：

| code | 常量 | 触发 | 前端处理 |
|------|------|------|----------|
| 10601 | `BI_CHAT_QUESTION_INVALID` | question 空/超长 | 表单校验 |
| 10602 | `BI_CHAT_AGENT_UNAVAILABLE` | sidecar 超时/不可用 | Toast「服务繁忙，请重试」（不暴露细节） |
| 10603 | `BI_SQL_GENERATION_FAILED` | 自纠错达上限仍无安全 SQL | 展示「无法生成安全查询」 |
| 10609 | `BI_SQL_EXEC_TIMEOUT` | 只读执行超时 | Toast + 建议缩小范围 |
| 10610 | `BI_SQL_EXEC_ROWS_EXCEEDED` | 扫描/返回行数超限 | 同上 |
| 10613 | `BI_SQL_EXEC_ERROR` | 白名单通过但 DB 执行报错 | Toast（脱敏） |
| 10611 | `BI_CHAT_TRACE_NOT_FOUND` | traceId 不存在 | Toast |
| 10612 | `BI_CHAT_TRACE_FORBIDDEN` | 越权查他人 trace | Toast |

**拒答码（`status=REJECTED` 时的 `rejectCode`，HTTP 层仍是 success）**：

| rejectCode | 触发（§3 校验） |
|------------|-----------------|
| `REJECT_NON_SELECT` | AST 非 SELECT（DML/DDL/DCL/TCL/Call/Set/Use） |
| `REJECT_MULTI_STATEMENT` | 多语句 |
| `REJECT_TABLE_NOT_ALLOWED` | 引用表不在白名单 |
| `REJECT_COLUMN_BLOCKED` | 命中列黑名单 |
| `REJECT_STAR_SELECT` | 使用裸 `SELECT *`（要求显式列，见 INV-6） |
| `REJECT_DANGEROUS` | 危险函数/结构（`INTO OUTFILE`、`LOAD_FILE`、`BENCHMARK`、`SLEEP`、系统库、UNION 提权等） |
| `REJECT_SEMANTIC` | agent 语义层判定不可答（越权/含写意图/模糊到无法生成） |

---

## 2. 关键算法与节点划分

### 2.1 端到端控制流（Java conductor）

```
接收 ask → 鉴权(bi:chat:query) → 参数校验(10601)
 → retry=0
 → LOOP:
     POST /agent/generate {question, retry, priorSql, priorError}
     ├─ refusal!=null            → status=REJECTED(REJECT_SEMANTIC) → break
     ├─ draftSql==null           → (retry<max? retry++ 继续 : 10603) 
     → §3 SQL 安全校验(JSqlParser AST)
        ├─ 命中规则 → 记 rejectCode
        │   ├─ retry<max 且属"可纠错"类 → priorError=规则说明; retry++; continue
        │   └─ 否则 → status=REJECTED → break
        → 只读执行(LIMIT 钳制/注入 + statement timeout + 行数上限)
           ├─ 超时 10609 / 超行 10610 / DB 错 10613
           │   └─ DB 错且 retry<max → priorError=错误; retry++; continue
           └─ 成功 → rows/columns → break(SUCCESS)
 → SUCCESS 时 POST /agent/explain → explanation + chart
 → 写 bi_chat_trace（成功/拒答/异常都写，INV-12）
 → 返回 BiChatAskVo
```

- `max = bi.chat.max-retry`（契约默认 **2**）。
- **"可纠错"类**：`REJECT_TABLE_NOT_ALLOWED`/`REJECT_COLUMN_BLOCKED`/`REJECT_STAR_SELECT`/DB 执行错 —— 把规则说明作为 `priorError` 反馈 agent 重写。
- **"直接拒答"类**：`REJECT_NON_SELECT`/`REJECT_MULTI_STATEMENT`/`REJECT_DANGEROUS`/`REJECT_SEMANTIC` —— 表意明确恶意/越权，不浪费重试。

### 2.2 LangGraph 节点划分（`bi-agent` 内，`/agent/generate` 子图）

| 节点 | 运行位置 | 职责 | 关键约束 |
|------|----------|------|----------|
| `retrieve_schema` | agent | question embedding → 检索"1 表=1 单元"索引（复用 AI-2 `kb-retrieval` `/search`）→ top-K 表+列+注释 | 只喂检索命中的表，token 收敛；候选须⊆白名单（Java 终裁） |
| `generate_sql` | agent | LLM few-shot 生成**单条 SELECT** | MySQL 方言、显式列、强制 LIMIT、无注释、无多语句；retry 时并入 priorSql/priorError |
| `self_lint` | agent | 廉价启发式：明显非 SELECT/多语句先自毙 | **非权威**，仅省一次跨进程往返 |
| `validate` + `execute` | **Java** | §3 AST 白名单 + 只读执行 | **唯一安全裁决 + 唯一执行者**（不在 agent） |
| `summarize` | agent（`/agent/explain`） | 结果解读 + 图表类型/字段建议 | 只依据传入 rowsSample/rowCount，**禁编造数字** |

> `BiChatState`（TypedDict）沿用技术设计 §3；retry/validate_error 由 Java 通过请求体注入，agent 无状态。

### 2.3 算法要点

- **schema 检索收敛**：top-K 默认 8 表；命中不足回退关键词匹配；绝不把全库 schema 灌 LLM。
- **LIMIT 策略**：AST 无 LIMIT → 注入 `LIMIT {bi.chat.default-rows}`；有 LIMIT 但 > `bi.chat.max-rows` → **钳制**到上限并记 trace；不可解析 → 拒答。
- **LLM 不做算术/聚合**：一律下推 SQL 引擎，规避幻觉（技术设计 §3 原则 1）。
- **自纠错有界**：`max-retry=2`，超限 → `10603`。

### 2.4 Prompt 草案

**generate_sql · system（草案）**：
```
你是只读数据分析师，只能针对给定的 MySQL 业务库生成【单条 SELECT】查询。
硬性规则（违反即作废）：
1) 只允许 SELECT；禁止 INSERT/UPDATE/DELETE/REPLACE/DDL/DCL/事务/SET/USE/多语句。
2) 只能引用【schema 上下文】中出现的表与列；禁止 SELECT *，必须列出具体列。
3) 必须包含 LIMIT，且不超过 {max_rows}。
4) 禁止：INTO OUTFILE/DUMPFILE、LOAD_FILE、BENCHMARK、SLEEP、访问 information_schema 及系统库、以 UNION 引入白名单外的表。
5) 时间范围用参数化的日期条件表达；不臆造不存在的列。
若问题要求写数据、越权、或信息不足以生成安全查询：输出 refusal，并说明原因，不要编造 SQL。
输出 JSON：{"sql": "<单条SELECT或null>", "tables": ["..."], "refusal": "<原因或null>"}
【schema 上下文】{schema_digest}
```
**retry 追加**：
```
上一次候选 SQL：{prior_sql}
校验/执行反馈：{prior_error}
请在遵守全部硬性规则前提下修正；仍无法安全生成则输出 refusal。
```
**summarize · system（草案）**：
```
根据【问题】【SQL】【结果列】【结果样本/总行数】，用简洁中文解读结论，
只能基于给定数据，禁止编造未出现的数字或趋势。
再从 bar/line/pie/table 选择最合适图表并给出 x 轴与 y 轴字段；不合适则 table。
输出 JSON：{"explanation":"...","chart":{"type":"...","x":"...","y":["..."],"title":"..."}}
```
**few-shot（≥3，Composer 补全到 agent）**：单表过滤 / 聚合+分组 / 时间窗；外加 1 条"要求删数据 → refusal"负例。

---

## 3. 安全与不变量约束清单（Opus 冻结，不可放松）

### 3.1 不变量（任何路径都必须成立）

| # | 不变量 |
|---|--------|
| INV-1 | **只读账号**：bi 专用 DB 账号仅 `SELECT` 权限，与 order/user 写账号物理隔离；连接串独立配置（`bi.chat.readonly-datasource.*`）；DB 层 `GRANT SELECT` 兜底。 |
| INV-2 | **单一裁决点**：所有 SQL 执行前必过 Java AST 校验；**Python 永不执行 SQL**；LLM 输出**永不**直接拼接执行。 |
| INV-3 | **仅 SELECT**：AST 根必须为 Select；拒 Insert/Update/Delete/Replace/Merge/DDL/DCL/TCL/Call/Set/Use。 |
| INV-4 | **单语句**：解析后语句数 == 1；禁分号后续内容。 |
| INV-5 | **表白名单**：所有引用表（含 JOIN、FROM 子查询、UNION 各分支）∈ `bi.chat.allow-tables`。 |
| INV-6 | **列黑名单 + 禁裸 `*`**：命中敏感列（`password/salt/token/secret/*_key` 等，清单 `bi.chat.deny-columns`）即拒；禁 `SELECT *`（要求显式列以便列级校验）。 |
| INV-7 | **强制 LIMIT + 上限**：无则注入默认；超上限钳制；返回/扫描行数上限（`bi.chat.max-rows` / `bi.chat.max-scan-rows`）。 |
| INV-8 | **超时**：statement timeout（`bi.chat.query-timeout-ms`），到点中断。 |
| INV-9 | **危险结构禁用**：`INTO OUTFILE/DUMPFILE`、`LOAD_FILE`、`BENCHMARK`、`SLEEP`、系统库（`mysql`/`information_schema`/`performance_schema`/`sys`）、UNION 提权到白名单外表。 |
| INV-10 | **AST 而非正则判定**：先 JSqlParser 规范化，再判定，防注释/编码/大小写绕过；禁字符串黑名单裸匹配作唯一手段。 |
| INV-11 | **鉴权 + trace 越权隔离**：每请求校验 Shiro session（user-center 签发）+ 权限码；trace 仅可查本人，跨用户需 `bi:chat:trace:all`/超管。 |
| INV-12 | **审计不可绕过**：成功/拒答/异常**都写** `bi_chat_trace`（NL、final_sql 或 null、status、rejectCode/reason、行数、耗时、retry、user_id）。 |
| INV-13 | **错误不泄露**：sidecar/DB 异常对外统一为 `106xx`，不回传连接串/堆栈/表结构。 |
| INV-14 | **schema 出口脱敏**：`/bi/chat/schema` 只出白名单表 + 剔除黑名单列；禁 chat 通道直查 `information_schema`。 |
| INV-15 | **可回退/降级**：sidecar 不可用 → `10602` 优雅降级，绝不半执行、不写脏结果。 |

### 3.2 SQL 白名单校验规则（Java，JSqlParser）

| 规则 | 通过条件 | 违规 → rejectCode |
|------|----------|-------------------|
| 语句类型 | 解析为 `Select` 单节点 | `REJECT_NON_SELECT` |
| 语句数 | 恰好 1 条 | `REJECT_MULTI_STATEMENT` |
| 表引用 | 全部 ∈ 白名单（递归 JOIN/子查询/UNION） | `REJECT_TABLE_NOT_ALLOWED` |
| 列引用 | 显式列且无黑名单命中；无裸 `*` | `REJECT_COLUMN_BLOCKED` / `REJECT_STAR_SELECT` |
| 危险函数/结构 | 无 `OUTFILE/LOAD_FILE/BENCHMARK/SLEEP`、无系统库、无越权 UNION | `REJECT_DANGEROUS` |
| LIMIT | 有则 ≤ 上限（否则钳制）；无则注入默认 | 不可解析 → `REJECT_DANGEROUS` |

> 校验器应有独立单测（危险样本 100% 拦截），是 W5「安全底座」出口标准。

### 3.3 契约常量（Composer 建配置类承载，键名冻结）

`bi.chat.allow-tables` · `bi.chat.deny-columns` · `bi.chat.default-rows` · `bi.chat.max-rows` · `bi.chat.max-scan-rows` · `bi.chat.query-timeout-ms` · `bi.chat.max-retry`(=2) · `bi.agent.base-url` · `bi.agent.timeout-ms` · `bi.chat.readonly-datasource.*`

---

## 4. 验收用例要点

### 4.1 功能（Happy path）
- [ ] 单表过滤：如「查上月订单数」→ 正确 SQL + 行数 + 图表 + 解读。
- [ ] 聚合分组：如「各状态订单量」→ `GROUP BY` + bar/pie 建议合理。
- [ ] 时间窗：如「近 7 天每日 GMV」→ 日期条件正确 + line 图。
- [ ] 白名单内联表：JOIN 两张白名单表可查通。

### 4.2 安全（危险 100% 拦截，全部可在 trace 复现）
- [ ] 写操作（`UPDATE/DELETE/DROP/INSERT`）→ `REJECT_NON_SELECT`。
- [ ] 多语句（`SELECT ...; DROP ...`）→ `REJECT_MULTI_STATEMENT`。
- [ ] 非白名单表 / `sys_user.password` → `REJECT_TABLE_NOT_ALLOWED` / `REJECT_COLUMN_BLOCKED`。
- [ ] 裸 `SELECT *` → `REJECT_STAR_SELECT`。
- [ ] `INTO OUTFILE`、`LOAD_FILE`、`BENCHMARK/SLEEP`、系统库、UNION 提权、注释绕过（`SEL/**/ECT`、`-- ` 尾注）→ `REJECT_DANGEROUS`。
- [ ] 缺 LIMIT → 自动注入；超上限 → 钳制并记 trace。
- [ ] 超时 → `10609`；超行 → `10610`。

### 4.3 鉴权 / 审计 / 降级
- [ ] 无 `bi:chat:query` → `10009`；查他人 trace 无 `bi:chat:trace:all` → `10612`。
- [ ] 每次问答（含拒答/异常）均落 `bi_chat_trace`，`trace/{id}` 可回溯节点链路。
- [ ] `/bi/chat/schema` 输出不含黑名单列、不含系统库表。
- [ ] `bi-agent` 停机 → `10602` 优雅降级、无半执行、trace status=ERROR。

### 4.4 Agent 自纠错
- [ ] 首轮 SQL 引用错列/错表 → 第 2 轮修正成功（`retry` 记 1）。
- [ ] 连续 2 轮失败 → `10603`「无法生成安全查询」，trace 完整。

### 4.5 评测（W8 出口）
- [ ] `bi/eval/nl2sql_testset.jsonl` ≥30 题（单表/联表/聚合/时间窗/应拒绝）。
- [ ] SQL 执行正确率 ≥80%；拒答正确率（危险/不可答被拦）100%。

---

## 5. 实现清单 + 未决问题（Composer 回填区）

> Composer 施工时在此追加「已实现类/接口清单」与「未决问题」；安全相关未决**不得自行拍板**，回 Opus 窗口改契约。

- 实现清单：_（待 Composer 填）_
- 未决问题：_（待 Composer 填）_

---

## 6. 相关

- 技术设计：[`bi-chatbi-nl2sql.md`](bi-chatbi-nl2sql.md)
- 路线 / 排期 / 分工：[`ai-capability-roadmap.md`](ai-capability-roadmap.md) · [`ai-capability-schedule.md`](ai-capability-schedule.md) §9
- 对外 API（落地时增量）：[`../api/bi-api.md`](../api/bi-api.md)
- 模块概要：[`bi-module-overview.md`](bi-module-overview.md)
- LLM 网关模式参考：[`kb-llm-platform-settings.md`](kb-llm-platform-settings.md)
