# AI-4 ChatBI / NL2SQL · 施工契约（Opus 产出，Composer 施工）

> **角色**：本文件由 Opus（架构与安全负责人）产出，作为 Composer 施工的**唯一契约**。
> **任务**：AI-4 ChatBI / NL2SQL Agent（ai-server 0→1）
> **状态**：**done** · contract 2026-07-17 起草 · 2026-07-19 加固 §2/§3 → 开 W5 · W5–W8 复验退回 B1/B2 · **2026-07-19 第 3 轮：B1/B2 已整改并验证（校验器 31/31 绿含 6 条 B1 回归；SSE 身份在请求线程解析；W8 测试集补入 6 绕过向量）→ W5/W6/W7/W8 全部签核**（详见 §5 终验记录）
> **上游**：[`ai-capability-roadmap.md`](ai-capability-roadmap.md) §4 第 2 波 · [`bi-chatbi-nl2sql.md`](bi-chatbi-nl2sql.md)（技术设计）· [`ai-capability-schedule.md`](ai-capability-schedule.md) §9（Opus/Composer 分工）
> **运行时**：`moli-ai-server`（服务名 `ai-server`，HTTP **1128** / Dubbo **20883**，网关 `/AiServer/**` StripPrefix=1）
> **架构图**（复用，勿重画）：[`../diagrams/png/moli-ai-chatbi-flow.png`](../diagrams/png/moli-ai-chatbi-flow.png) · 源 [`../diagrams/moli-ai-chatbi-flow.drawio`](../diagrams/moli-ai-chatbi-flow.drawio)

---

## 0. 契约边界（读我）

**本契约只定义**：对外/对内接口签名、DTO 形状、错误码语义、算法与节点划分、prompt 草案、安全不变量、验收要点。

**不在本契约内（交给 Composer，按 `moli-order`/`moli-knowledge` 现有模式落地）**：建表 DDL、Mapper/Entity/VO 样板类、配置类（`@ConfigurationProperties`）、Spring 接线、只读数据源与缓存 Bean、`ai-agent` FastAPI 脚手架、图表序列化、`nl2sql_testset.jsonl` 录入、CI。

**红线**：Composer **不得**改动本契约中的 §3 安全校验逻辑、§1 接口签名、§2 节点边界。发现歧义或安全漏洞 → 回 Opus 窗口改契约，不自行拍板。

---

## 1. 接口契约

### 1.1 对外 REST（Java，`ai-server`，经网关 `/AiServer/**`）

统一返回 `MoliResult<T>`（`com.moli.common.core.MoliResult`）。三个端点：

| # | 方法 | 路径 | 权限码 | 说明 |
|---|------|------|--------|------|
| A | POST | `/bi/chat/ask` | `ai:chat:query` | 自然语言问数 → SQL+结果+图表+解读（支持 SSE） |
| B | GET | `/bi/chat/trace/{traceId}` | `ai:chat:trace` | 查看单次问答决策链路（越权隔离，见 INV-11） |
| C | GET | `/bi/chat/schema` | `ai:chat:query` | 返回可查询表/列白名单（**脱敏**，见 INV-14） |

> **命名三元组（一致性说明）**：网关路由 `/AiServer/**`（StripPrefix=1）→ Nacos 服务 `ai-server`（HTTP 1128）→ 业务路径 `/bi/chat/*`（BI 领域）→ 权限码 `ai:chat:*`（随服务）。即 `GET /AiServer/bi/chat/schema` 经网关剥前缀后打到 `ai-server` 的 `/bi/chat/schema`。路由/服务名/权限前缀统一挂 `ai-server`，路径前缀用 BI 领域名 `/bi`，是"ai-server 承载 BI ChatBI"的既定分层，非笔误。权限码 seed（`ai:chat:query`/`ai:chat:trace`/`ai:chat:trace:all`）由 Composer 按 `sys_permission` 惯例补 SQL（§0 样板范畴）。

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
  String traceId;              // 必返回，任何 status 都要能回溯 ai_chat_trace
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
- 二者都必须已写入 `ai_chat_trace`（INV-12）。

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

**越权隔离**：调用者仅可查 `user_id == 当前登录用户` 的 trace；否则 `10612`。持 `ai:chat:trace:all`（或平台超管）方可跨用户查看（INV-11）。

#### C. `GET /bi/chat/schema`

`MoliResult<List<BiSchemaTableVo>>`：

```
BiSchemaTableVo   { String table; String comment; List<BiSchemaColumnVo> columns; }
BiSchemaColumnVo  { String name; String type; String comment; }
```

只返回 `bi.chat.allow-tables` 白名单表，且**剔除**列黑名单命中列（INV-6/14）。此接口是 UI/agent 了解可查范围的**唯一出口**，禁止对外暴露 `information_schema` 直查。

### 1.2 对内契约：Java 壳 ↔ `ai-agent`（Python FastAPI sidecar）

> **编排归属（架构决策）**：**Java 是 conductor（外层控制流 + 唯一安全裁决 + 唯一 SQL 执行者）**；`ai-agent` 无状态、仅提供"图节点"HTTP 能力。理由：安全单一真相（INV-2）、sidecar 可重启/降级（INV-15）、Python 永不碰执行。retry 计数与循环由 Java 持有并回传给 agent 作上下文。

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
接收 ask → 鉴权(ai:chat:query) → 参数校验(10601)
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
 → 写 ai_chat_trace（成功/拒答/异常都写，INV-12）
 → 返回 BiChatAskVo
```

- `max = bi.chat.max-retry`（契约默认 **2**）。
- **"可纠错"类**：`REJECT_TABLE_NOT_ALLOWED`/`REJECT_COLUMN_BLOCKED`/`REJECT_STAR_SELECT`/DB 执行错 —— 把规则说明作为 `priorError` 反馈 agent 重写。
- **"直接拒答"类**：`REJECT_NON_SELECT`/`REJECT_MULTI_STATEMENT`/`REJECT_DANGEROUS`/`REJECT_SEMANTIC` —— 表意明确恶意/越权，不浪费重试。

### 2.2 LangGraph 节点划分（`ai-agent` 内，`/agent/generate` 子图）

| 节点 | 运行位置 | 职责 | 关键约束 |
|------|----------|------|----------|
| `retrieve_schema` | agent | question embedding → 检索"1 表=1 单元"索引（复用 AI-2 `kb-retrieval` `/search`）→ top-K 表+列+注释 | **索引仅含白名单表（INV-16）**；只喂检索命中的表，token 收敛；候选仍须⊆白名单（Java 终裁） |
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
| INV-3 | **仅 SELECT**：AST 根必须为 `Select` 单节点；拒 Insert/Update/Delete/Replace/Merge/DDL/DCL/TCL/Call/Set/Use。 |
| INV-4 | **单语句**：解析后语句数 == 1；禁分号后续内容。 |
| INV-5 | **表白名单（递归全覆盖）**：所有引用表 ∈ `bi.chat.allow-tables`。递归收集范围 = **JOIN + 任意位置子查询（FROM / WHERE / SELECT 列表 / HAVING / ON / EXISTS / IN）+ UNION/INTERSECT/EXCEPT 各分支 + CTE/`WITH` 派生表**。CTE 名不算"表"，但 CTE 内部引用的真实表仍须 ∈ 白名单。**白名单为空/未配置 → 全拒（fail-closed）**，绝不"空即放行"。 |
| INV-6 | **列黑名单 + 禁一切 `*`**：命中敏感列（`password/salt/token/secret/*_key` 等，清单 `bi.chat.deny-columns`）即拒；禁**任何形式的星号**——裸 `*`、限定 `t.*`、`db.t.*` 一律拒（否则绕过列级校验），要求显式列。 |
| INV-7 | **强制 LIMIT + 上限**：无则注入默认；超上限钳制；返回/扫描行数上限（`bi.chat.max-rows` / `bi.chat.max-scan-rows`）。 |
| INV-8 | **超时**：statement timeout（`bi.chat.query-timeout-ms`），到点中断。 |
| INV-9 | **危险结构禁用**：任意 `INTO`（`OUTFILE`/`DUMPFILE`/`INTO @var`）、`LOAD_FILE`、`BENCHMARK`、`SLEEP`/`GET_LOCK`、用户变量赋值 `:=`、`WITH RECURSIVE` 递归 CTE、系统库（`mysql`/`information_schema`/`performance_schema`/`sys`）、UNION 提权到白名单外表。 |
| INV-10 | **AST 而非正则判定 + fail-closed**：先 JSqlParser 规范化，再判定，防注释/编码/大小写绕过；禁字符串黑名单裸匹配作唯一手段。**解析抛异常 / 非 `Select` / 出现校验器未识别的语法节点 → 一律拒答（默认 `REJECT_DANGEROUS`），绝不"解析失败即放行"。** |
| INV-11 | **鉴权 + trace 越权隔离**：每请求校验 Shiro session（user-center 签发）+ 权限码；trace 仅可查本人，跨用户需 `ai:chat:trace:all`/超管。 |
| INV-12 | **审计不可绕过**：成功/拒答/异常**都写** `ai_chat_trace`（NL、final_sql 或 null、status、rejectCode/reason、行数、耗时、retry、user_id）。 |
| INV-13 | **错误不泄露**：sidecar/DB 异常对外统一为 `106xx`，不回传连接串/堆栈/表结构。 |
| INV-14 | **schema 出口脱敏**：`/bi/chat/schema` 只出白名单表 + 剔除黑名单列；禁 chat 通道直查 `information_schema`。 |
| INV-15 | **可回退/降级**：sidecar 不可用 → `10602` 优雅降级，绝不半执行、不写脏结果。 |
| INV-16 | **schema 检索纵深**：`retrieve_schema` 所用的 schema 索引/摘要**仅覆盖 `bi.chat.allow-tables` 白名单表**（离线建索引时即按白名单过滤）。agent 因此永不"看见"白名单外表名/列名，降低越权引用与 schema 泄露面；Java AST 白名单（INV-5）仍为最终裁决，二者纵深叠加。 |

### 3.2 SQL 白名单校验规则（Java，JSqlParser）

| 规则 | 通过条件 | 违规 → rejectCode |
|------|----------|-------------------|
| **可解析（fail-closed）** | JSqlParser 成功解析、根为 `Select`、无未识别节点 | 解析异常/未识别 → `REJECT_DANGEROUS`（默认拒） |
| 语句类型 | 解析为 `Select` 单节点 | `REJECT_NON_SELECT` |
| 语句数 | 恰好 1 条 | `REJECT_MULTI_STATEMENT` |
| 表引用 | 全部 ∈ 白名单（递归 JOIN / 任意位置子查询 / UNION 分支 / CTE 派生表）；白名单空 → 全拒 | `REJECT_TABLE_NOT_ALLOWED` |
| 列引用 | 显式列且无黑名单命中；无任何 `*`（含 `t.*`/`db.t.*`） | `REJECT_COLUMN_BLOCKED` / `REJECT_STAR_SELECT` |
| 危险函数/结构 | 无任意 `INTO`（OUTFILE/DUMPFILE/@var）、`LOAD_FILE/BENCHMARK/SLEEP/GET_LOCK`、`:=` 赋值、`WITH RECURSIVE`、系统库、越权 UNION | `REJECT_DANGEROUS` |
| LIMIT | 有则 ≤ 上限（否则钳制）；无则注入默认 | 不可解析 → `REJECT_DANGEROUS` |

> 校验器应有独立单测（危险样本 100% 拦截），是 W5「安全底座」出口标准。校验**顺序即上表自上而下**：先确证可解析，再逐条判定，任一未过即短路拒答（fail-closed）。

> **v1.2 范围声明（显式非目标）**：本版**无行级/租户数据权限**——持 `ai:chat:query` 即可查白名单表**全量**数据；白名单表的选择需据此把控（勿纳入行级敏感、跨租户混存的表）。行级数据 ACL / 权限域隔离留 v2+（技术设计 §9）。此为有意识决策，非遗漏。

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
- [ ] **子查询/CTE 藏非白名单表**：`... WHERE x IN (SELECT c FROM 非白名单表)`、`WITH t AS (SELECT * FROM 非白名单表) ...` → `REJECT_TABLE_NOT_ALLOWED`。
- [ ] 裸 `SELECT *` **及限定 `o.*`/`db.o.*`** → `REJECT_STAR_SELECT`。
- [ ] `INTO OUTFILE/DUMPFILE`、`SELECT ... INTO @v`、`LOAD_FILE`、`BENCHMARK/SLEEP/GET_LOCK`、`:=` 赋值、`WITH RECURSIVE`、系统库、UNION 提权、注释绕过（`SEL/**/ECT`、`-- ` 尾注）→ `REJECT_DANGEROUS`。
- [ ] **解析失败 / 未识别语法**（乱码、非 SQL、方言不支持）→ `REJECT_DANGEROUS`（fail-closed，绝不放行）。
- [ ] **白名单为空/未配置** → 任意查询全拒（fail-closed）。
- [ ] 缺 LIMIT → 自动注入；超上限 → 钳制并记 trace。
- [ ] 超时 → `10609`；超行 → `10610`。

### 4.3 鉴权 / 审计 / 降级
- [ ] 无 `ai:chat:query` → `10009`；查他人 trace 无 `ai:chat:trace:all` → `10612`。
- [ ] 每次问答（含拒答/异常）均落 `ai_chat_trace`，`trace/{id}` 可回溯节点链路。
- [ ] `/bi/chat/schema` 输出不含黑名单列、不含系统库表。
- [ ] `ai-agent` 停机 → `10602` 优雅降级、无半执行、trace status=ERROR。

### 4.4 Agent 自纠错
- [ ] 首轮 SQL 引用错列/错表 → 第 2 轮修正成功（`retry` 记 1）。
- [ ] 连续 2 轮失败 → `10603`「无法生成安全查询」，trace 完整。

### 4.5 评测（W8 出口）
- [x] `bi/eval/nl2sql_testset.jsonl` ≥30 题（单表/联表/聚合/时间窗/应拒绝）。
- [x] SQL 执行正确率 ≥80%；拒答正确率（危险/不可答被拦）100%（validator 离线门禁 + E2E 脚本）。

---

## 5. 实现清单 + 未决问题（Composer 回填区）

> Composer 施工时在此追加「已实现类/接口清单」与「未决问题」；安全相关未决**不得自行拍板**，回 Opus 窗口改契约。

### 实现清单（W6 · 2026-07-19）

| # | 交付 | 路径 |
|---|------|------|
| 1 | Python sidecar | `moli-ai/moli-ai-server/ai-agent/` · `/health` · `/agent/generate` · `/agent/explain` · `schema/allow_tables.json` |
| 2 | Java conductor | `BiChatServiceImpl` · `BiAgentClient` · `BiChatReadonlyQueryExecutor` · `AiChatTraceServiceImpl` |
| 3 | REST 三端点 | `BiChatController` · `POST /bi/chat/ask` · `GET /bi/chat/trace/{id}` · `GET /bi/chat/schema` |
| 4 | DTO / 错误码 | `bi.dto.*` · `BiChatResponseCode` · `BiAgentProperties` |
| 5 | Schema 脱敏 | `BiSchemaServiceImpl` · `resources/bi-chat/allow_tables.json` |
| 6 | 主/只读数据源 | `BiChatPrimaryDataSourceConfig`（@Primary 写库）· `BiChatReadonlyDataSourceConfig` |
| 7 | Gradio 冒烟 | `ai-agent/gradio_app.py` → Java `1128/bi/chat/ask` |
| 8 | dev 配置 | `application-dev.yml` · Shiro `enabled: false` · 只读池 dev 可同 root |

**冒烟**（本地）：

- `mvn -q test`（moli-ai-server）→ **24/24 pass**（W5 校验器回归）
- sidecar：`uvicorn app.main:app --host 127.0.0.1 --port 1130`
- ai-server：`mvn spring-boot:run`（需 MySQL `ai_chat_trace` 表 · 执行 `docs/sql/32_ai_chat_trace.sql`）
- `POST /bi/chat/ask`「秒杀订单有多少？」→ `status=SUCCESS` + SQL + traceId
- 「请删除所有订单」→ `status=REJECTED` + `REJECT_SEMANTIC`
- Gradio：`python gradio_app.py`

- 未决问题：SSE / 鉴权留 W7（见下）；dev 只读账号默认 root 同库，生产须 `moli_bi_ro` + GRANT。

### 实现清单（W7 · 2026-07-19）

| # | 交付 | 路径 |
|---|------|------|
| 1 | SSE `/bi/chat/ask` | `BiChatServiceImpl.askStream` · `BiChatSseProgressSink` · `BiChatStreamHelper` · 契约 §1.1 六事件 |
| 2 | 图表 + 解读 | agent `explain.py` 增强 · Java fallback chart/explanation |
| 3 | 鉴权 | `BiChatPermissionConstants` · `docs/sql/33_ai_chat_permissions.sql` · Shiro `enabled: true` |
| 4 | 网关端到端 | `docs/api/ai-api.md` · `docs/test/ai-smoke.md` C1–C6 |
| 5 | Gradio | `gradio_app.py` · `BI_CHAT_BASE` + `BI_CHAT_TOKEN` + SSE 开关 |

**冒烟**：

- `mvn -q test` → **26/26 pass**（含 `BiChatStreamHelperTest`）
- 导入 `33_ai_chat_permissions.sql` → 登录 token → `POST /AiServer/bi/chat/ask`（JSON + SSE）
- 无 token → 鉴权失败（非业务 success）

- 未决问题：无（§1 SSE 事件 / §1.3 码值未改）。

### 实现清单（W8 · 2026-07-19）

| # | 交付 | 路径 |
|---|------|------|
| 1 | 测试集 | `bi/eval/nl2sql_testset.jsonl`（44 题：15 执行 / 8 NL 拒答 / 21 validator） |
| 2 | 评测脚本 | `bi/eval/eval_nl2sql.py` · `bi/eval/baselines.json` |
| 3 | Validator 门禁 | `Nl2sqlTestsetValidatorTest` · **21/21 testset + 24 向量单测** |
| 4 | CI | `.github/workflows/bi-nl2sql-eval.yml`（PR 阻断 validator gate） |
| 5 | API 文档 | `docs/api/ai-api.md` §3 NL2SQL 评测 |

**冒烟**：

- `python eval_nl2sql.py --validator-only --gate` → `reject_accuracy=1.0` PASS
- 全栈 `python eval_nl2sql.py --gate` → `exec_accuracy ≥ 0.8`（规则 MVP 下 15/15 可达）

- 未决问题：无。

### 实现清单（W5 · 2026-07-19）

| # | 交付 | 路径 |
|---|------|------|
| 1 | 只读数据源 + 配置 | `BiChatProperties` · `BiChatReadonlyDataSourceConfig`（Hikari `biChatReadonlyDataSource` / `biChatReadonlyJdbcTemplate`）· `application-dev.yml` `bi.chat.readonly-datasource.*` |
| 2 | 只读 MySQL 账号 | `docs/sql/32_ai_chat_trace.sql` 内 `moli_bi_ro` + `GRANT SELECT` on `seckill_order`/`seckill_activity` |
| 3 | SQL AST 白名单 | `BiSqlSecurityValidator` · `BiSqlRejectCode` · `BiSqlValidationResult`（JSqlParser 4.9 · §3.2 顺序 fail-closed） |
| 4 | 审计表 DDL | `docs/sql/32_ai_chat_trace.sql` · `AiChatTrace` · `AiChatTraceMapper` · `mapper/AiChatTraceMapper.xml` |
| 5 | 危险 SQL 单测 | `BiSqlSecurityValidatorTest`（21 条 §4.2 向量）· `BiSqlSecurityValidatorPolicyTest`（空白名单/LIMIT）· **24/24 pass** |
| 6 | 迁移登记 | `docs/ops/sql-migration-order.md` #28 · `scripts/init-db.ps1` 导入 32 |

**冒烟**：

- `mvn -q test`（moli-ai-server）→ 24 tests pass，危险样本 100% 拦截  
- 配置：`bi.chat.allow-tables` + `moli_bi_ro` 账号与 GRANT 对齐（dev 见 `application-dev.yml`）

- 未决问题：无（§1 接口 / §3 校验规则未改）。

### Opus W5 验收（2026-07-19）→ **不通过（1 项阻断，待整改）**

**已达标**：只读数据源隔离（`setReadOnly(true)` + 独立 Hikari 池 + `moli_bi_ro` 仅 `GRANT SELECT` 白名单表，INV-1）✅ · 只读执行器（statement timeout + `max-scan-rows` + fetch 上限，INV-7/8）✅ · `ai_chat_trace` DDL 与 §1.1/§5.1 一致 ✅ · 校验器已覆盖 fail-closed、空白名单 deny-all、多语句、非 SELECT、TablesNamesFinder 递归表白名单（含子查询/UNION/CTE 内真实表）、系统库、`t.*`/`db.t.*`、`INTO`、`:=`/UserVariable、`WITH RECURSIVE`、LIMIT 注入/钳制 ✅。

**B1（阻断 · 危险函数按位置漏检，违反 INV-9 + §3.2 + W5「危险 SQL 100% 拦截」出口）**：

`BiSqlSecurityValidator` 的**危险函数 / 列黑名单**判定依赖手写 `scanExpression` 遍历，但遍历范围不完整：

1. `scanPlainSelect` **只**扫 `selectItems / from / joins(+ON) / where / having`，**未扫 `GROUP BY` 与 `ORDER BY`**。
2. `scanExpression` 的 `instanceof` 链只认 `Column/Function/UserVariable/VariableAssignment/BinaryExpression/ParenthesedSelect/ExpressionList`，**未覆盖** `CaseExpression / CastExpression / Between / InExpression / IsNullExpression / NotExpression / Parenthesis / SignedExpression / WhenClause` 等，这些节点内部的表达式被静默跳过。

后果——以下危险 SQL **当前会 PASS 校验**（实测语义）：

- `SELECT id FROM seckill_order ORDER BY SLEEP(5)` （ORDER BY 未扫）
- `SELECT id FROM seckill_order GROUP BY BENCHMARK(1000000, MD5('x'))` （GROUP BY 未扫）
- `SELECT CASE WHEN SLEEP(5) > 0 THEN 1 ELSE 0 END FROM seckill_order` （CASE 未遍历）
- `SELECT id FROM seckill_order WHERE id IN (SELECT SLEEP(5))` （InExpression 子查询未遍历）

> 表白名单不受影响（`TablesNamesFinder` 独立全量扫描，越权表仍被拦）；受影响的是 `BENCHMARK/SLEEP/GET_LOCK/LOAD_FILE` 等**危险函数**（资源耗尽/时间盲注）及嵌套在上述节点里的**列黑名单**。只读账号 + 30s 超时能"兜底限幅"但**不满足 §3「AST 层必拒、危险样本 100% 拦截」**，W5 是安全底座，必须 AST 拒绝而非依赖超时。

**整改要求（Composer 补实现，勿改 §3 契约）**：

1. 用 JSqlParser 官方访问者替换手写遍历：`ExpressionVisitorAdapter`（override `visit(Function)`/`visit(Column)`/`visit(AllColumns)`/`visit(AllTableColumns)`/子查询 `visit(Select)`）作用到**每个表达式位置**；`ExpressionVisitorAdapter` 会自动下钻 `CASE/CAST/BETWEEN/IN/IS/NOT/括号`等，一次性堵住节点类型缺口。
2. `scanPlainSelect` 把**全部子句**喂给访问者：select items、`INTO`、from/joins(+ON)、`WHERE`、**`GROUP BY`**、`HAVING`、**`ORDER BY`**（及 window/qualify 若支持）。
3. 兜底：遍历中遇未识别表达式节点 → fail-closed 拒（与 §3.2 首行一致）。
4. 回归用例（`BiSqlSecurityValidatorTest` 增补，须全绿）：上列四条 → `REJECT_DANGEROUS`；`ORDER BY password`（列黑名单在 ORDER BY）；`SELECT (CASE WHEN 1=1 THEN password END) FROM seckill_order` → `REJECT_COLUMN_BLOCKED`。

整改后回本窗口复验；通过则标 W5 done、更新排期 §9.1。

**非阻断观察（记录，不拦 W5）**：`BiChatReadonlyQueryExecutor` 每次 `jdbcTemplate.setQueryTimeout(...)` 改共享 bean 状态——当前恒为同一配置值故无实害，建议改用 `JdbcTemplate` 构造即设或每请求局部实例，避免未来并发下被改动。

### Opus W6 验收（2026-07-19）→ **功能达标（W6 出口通过）；安全闭环受 W5 B1 牵制**

W6 出口 =「Agent MVP + Java 只读执行 · Gradio 输入 → 出结果表」。逐项核对：

**已达标**：
- **sidecar §1.2 齐备**：`ai-agent/app/main.py` 暴露 `/health` · `/agent/generate` · `/agent/explain`；`generate.py` = 规则 NL→SQL + 可选 LLM + `self_lint`（非权威）+ 写意图 `refusal`；`retrieve.py` 只从 `schema/allow_tables.json` 白名单加载（**满足 INV-16**）。✅
- **Java conductor §2.1 忠实**：`BiChatServiceImpl.executeAsk` 实现 LOOP（generate → refusal→REJECT_SEMANTIC / draftSql 空→retry 或 `10603` / §3 校验 → 只读执行 → explain）；重试分类与契约一致（`TABLE_NOT_ALLOWED`/`COLUMN_BLOCKED`/`STAR_SELECT` + DB 执行错可纠错，`NON_SELECT`/`MULTI`/`DANGEROUS`/`SEMANTIC` 直接拒）。✅
- **降级 INV-15**：`BiAgentClient` 未配置/超时/连接失败/非 2xx → `BiAgentUnavailableException` → `10602`，不半执行；explain 阶段 sidecar 挂 → fallback 文案/图表，不掀翻已成功结果。✅
- **审计 INV-12**：成功/拒答/异常/参数非法**全路径**写 `ai_chat_trace`；`rejectReason` 脱敏截断（INV-13）。✅
- **字段对齐**：请求 `sessionId/question/retry/priorSql/priorError` ↔ 响应 `draftSql/usedTables/schemaDigest/refusal` 与 §1.2 一致；Java 不信任 `usedTables`，表白名单以自身 AST 终裁（符合契约）。✅
- **只读执行**：`BiChatReadonlyQueryExecutor` 经独立只读源，超时/扫描/行数上限齐。✅

**牵制项（非 W6 新增，源自 W5 B1）**：conductor 的**唯一** SQL 安全闸是 `BiSqlSecurityValidator`，其 B1 缺口（`ORDER BY/GROUP BY/CASE…` 内危险函数漏检）→ 一个越狱/幻觉 LLM 产出的 `... ORDER BY SLEEP(5)` 会通过校验并被只读执行。故 **W6 端到端安全闭环 = W5 B1 修复的函数**；B1 未修前，W6 不得视为"安全底座闭环完成"。二者一并在 B1 修复后转 done。

**非阻断观察（W6，记录不拦）**：
- O1：`generate.py::self_lint` 把 `star select` 等**可纠错**问题直接返回 `refusal` → Java 判 `REJECT_SEMANTIC` **不再重试**，与 §2.2「self_lint 仅省往返、非权威」略有出入，压低自纠错成功率。建议 self_lint 命中时返回 `draftSql=None`（不带 refusal），让 Java 走 retry 或交由 AST 裁决。
- O2：`retrieve.py` 已预留 kb-retrieval `/search` 调用但 MVP 忽略结果——W7/W8 再启用语义增强即可。

**结论**：W6 **功能出口通过**（Gradio 端到端出表、编排/降级/审计/字段均达标）；安全上与 W5 B1 耦合，B1 修复后 W5+W6 一并标 done、更新排期 §9.1。

### Opus W7 验收（2026-07-19）→ **不通过（1 项阻断 B2，待整改）**

W7 出口 =「图表 + 解读 + `/bi/chat/ask` SSE + `ai:chat:*` 鉴权 · 端到端走通网关」。逐项核对：

**已达标**：
- **鉴权真落地**：`BiChatController` 三端点带 `@RequiresPermissions`——`/ask`+`/schema`=`ai:chat:query`、`/trace/{id}`=`ai:chat:trace`；`BiChatPermissionConstants` 值与 §1.1 一致。✅
- **权限 seed 一致**：`docs/sql/33_ai_chat_permissions.sql` 的 `sys_action` 播种 `ai:chat:query`/`ai:chat:trace`/`ai:chat:trace:all`，与契约/网关 `/AiServer/**` 三元组一致；含菜单 610 + role_action。✅
- **SSE §1.1 六事件精确**：`BiChatSseProgressSink` 发 `stage{stage,traceId}` / `sql{sql}` / `chart(BiChartVo)` / `token{delta}` / `done(BiChatAskVo)` / `error{code,message}`；conductor 阶段序 `schema→sql→validate→execute→summarize`、拒答走 `done(status=REJECTED)`、请求级失败走 `error`——与契约完全对齐。✅
- **图表 + 解读**：agent `explain.py` + Java fallback（sidecar 挂不掀翻成功结果）。✅
- **网关端到端**：`docs/api/ai-api.md` + `docs/test/ai-smoke.md` C1–C6，路由 `/AiServer/**`→`ai-server` 已核。✅

**B2（阻断 · SSE 路径丢失登录身份，违反 INV-11 + INV-12）**：

`BiChatServiceImpl.askStream` 用 `new Thread(...)` 起 `bi-chat-sse` worker 跑 `executeAsk`，而 `executeAsk` 内 `resolveUserId()` → `ShiroUtils.getUserInfo()` → `SecurityUtils.getSubject().getPrincipal()` 依赖 **Shiro `ThreadContext`（ThreadLocal）**。worker 是新线程、未绑定 Subject → `getPrincipal()` 返回 null → `resolveUserId()` 落 `0L`。后果：

1. **审计错属（INV-12）**：凡 `stream=true` 的问答，`ai_chat_trace.user_id` 全部写成 `0`，而非真实提问者。
2. **越权隔离失效（INV-11）**：SSE 生成的 trace 归属 user 0，真实用户随后 `GET /bi/chat/trace/{id}`（`ai:chat:trace`）因 `user_id(0) != 当前用户` 被判 `10612`，**看不到自己的 SSE 链路**（除非持 `trace:all`）。

> 注：`@RequiresPermissions` 本身在**请求线程** AOP 拦截，鉴权（能否访问）不受影响；受影响的仅是 worker 线程内的**身份解析/审计归属**。非流式 `ask()` 在请求线程同步执行，`user_id` 正确——**仅 SSE 分支受损**。

**整改要求（Composer 补实现，勿改 §1/§3 契约）**：

- 在 `askStream` **请求线程**内先解析 `userId`（`resolveUserId()`），连同 request 一并传入 worker 的 `executeAsk`；`executeAsk` 改为接收已解析的 `userId`，不在 worker 线程再读 Shiro。
- 或用 Shiro `subject.associateWith(Runnable)` / `SubjectThreadState` 将当前 Subject 传播进 worker 线程。
- 回归：登录用户 `stream=true` 问答后，`ai_chat_trace.user_id` = 真实用户；该用户可 `GET /trace/{id}` 查到本人 SSE 链路（不需 `trace:all`）。

**牵制项**：与 W5/W6 相同，SSE/非流式最终仍过 `BiSqlSecurityValidator`，**W5 B1 未修前端到端安全不闭环**。

**结论**：W7 图表/解读/SSE 事件/鉴权注解/权限 seed/网关联通均达标，但 **B2 使 SSE 审计与越权隔离失真（§3 冻结不变量）**，W7 **不予签核**；修 B2（并随附 B1）后回本窗口复验。

### Opus 复验 W5–W8（第 2 轮 · 2026-07-19）→ **W5/W7 不通过（B1/B2 未整改），W6/W8 受牵制不签 done**

> 结论：W8 交付物（测试集/评测脚本/CI 门禁）已到位，但 **B1、B2 两个阻断项在代码中均未整改**，且 W8「危险 100% 拦截」出口被 B1 证伪。四周**均不予签核**，一并待 B1+B2 修复后回本窗口复验。

**B1 仍未修（阻断 · W5，实测代码 `BiSqlSecurityValidator`）**：整改要求（§W5 验收）明确要求「用 `ExpressionVisitorAdapter` 替换手写遍历 + 扫全部子句（含 GROUP BY/ORDER BY）」，但当前实现**原样未改**：
- `scanPlainSelect` 仍只扫 `selectItems / from / joins(+ON) / where / having`——**GROUP BY、ORDER BY 依旧不扫**。
- `scanExpression` 仍是有限 `instanceof` 链（`Column/Function/UserVariable/VariableAssignment/BinaryExpression/ParenthesedSelect/ExpressionList`）——**未覆盖 `InExpression/CaseExpression/WhenClause/Between/Cast/IsNull/Not/Parenthesis/SignedExpression`**，节点内表达式被静默跳过。
- 规定的回归用例（`ORDER BY SLEEP(5)`、`GROUP BY BENCHMARK(...)`、`CASE WHEN SLEEP(5)`、`WHERE id IN (SELECT SLEEP(5))`、`ORDER BY password`、`CASE WHEN 1=1 THEN password END`）在 `BiSqlSecurityValidatorTest` 中**不存在**。现测试仅覆盖顶层 `SELECT SLEEP(5)/BENCHMARK/GET_LOCK`（走 selectItems 命中），故「24/24 pass」**未触及 B1 缺口**。
- 实测这些危险 SQL 当前**仍会 PASS 校验**（违反 INV-9 + §3.2 + W5 出口）。

**B2 仍未修（阻断 · W7，实测代码 `BiChatServiceImpl.askStream`）**：`askStream` 仍 `new Thread(...)` 跑 `executeAsk(request, sink)`，`executeAsk` 内第 125 行仍 `Long userId = resolveUserId()`（在 worker 线程读 Shiro `ThreadContext`）。未按整改要求「请求线程先解析 `userId` 传入 worker」或「`subject.associateWith`」处理 → SSE 请求 `ai_chat_trace.user_id` 仍落 `0`（违反 INV-11/INV-12）。

**W8 出口证伪（受 B1 牵制）**：`bi/eval/nl2sql_testset.jsonl` 的 validator 用例（v09/v14/v15/v16）与单测同源，**同样回避了 B1 绕过向量**——`v09` 是 `sys_user` 越表（`TablesNamesFinder` 命中），`v14/15/16` 是顶层危险函数。因此 `eval_nl2sql.py --validator-only --gate` 的 `reject_accuracy=1.0` 是**假绿**：一旦把 B1 四向量补进测试集，当前 validator 会 FAIL。故 W8「拒答/拦截 100%」**不成立**。

**本轮整改要求（Composer，勿改 §1/§3 契约）**：
1. **B1**：按 §W5 整改要求真正重写遍历（`ExpressionVisitorAdapter` 下钻全表达式 + `scanPlainSelect` 覆盖 GROUP BY/ORDER BY/INTO/HAVING 全子句 + 未识别节点 fail-closed），并把上述 6 条回归用例补入 `BiSqlSecurityValidatorTest` 且全绿。
2. **B2**：`askStream` 请求线程内先 `resolveUserId()` 传入 `executeAsk`（或 `associateWith`），补回归：登录用户 `stream=true` 后 `user_id`=真实用户且本人可查自己 SSE trace（无需 `trace:all`）。
3. **W8**：把 B1 四向量（ORDER BY/GROUP BY/CASE/子查询内危险函数）与列黑名单藏 ORDER BY/CASE 的用例补入 `nl2sql_testset.jsonl` validator 段，`--validator-only --gate` 仍须 `reject_accuracy=1.0`。
4. 三项完成后回本窗口，**W5/W6/W7/W8 一并复验签核**；通过再更新 `ai-capability-roadmap.md §3`/`schedule §9.1` AI-4 状态。

**未受影响（已达标，无需返工）**：只读源隔离（INV-1）、只读执行器超时/行数（INV-7/8）、`ai_chat_trace` DDL、表白名单递归（`TablesNamesFinder`）、fail-closed/空白名单 deny-all、多语句/非 SELECT、SSE 六事件精度、`@RequiresPermissions` 鉴权与权限 seed、网关三元组、降级 `10602`、审计全路径落库、W6 编排/字段对齐、W8 评测脚本与 CI 骨架。仅需补 B1/B2 与对应回归即可整体转 done。

---

### Opus 终验 W5–W8（第 3 轮 · 2026-07-19）→ **W5/W6/W7/W8 全部通过（AI-4 done）**

第 2 轮打回的 B1、B2 两阻断项及 W8 出口证伪问题，本轮以**代码 + 实跑单测**核验，均已整改到位：

**B1 已修（W5，`BiSqlSecurityValidator`）✅**：手写遍历已替换为 `ExpressionVisitorAdapter`（`SecurityExpressionVisitor`）；`scanPlainSelect` 现覆盖 **GROUP BY（`scanGroupBy` 含 grouping sets）、ORDER BY、INTO、JOIN ON（含 `getOnExpressions`）、WHERE、HAVING、select items** 全子句；访问者 override `Function`（危险函数）/`Column`（列黑名单）/`AllColumns`+`AllTableColumns`（星号）/`UserVariable`+`VariableAssignment`+`:=`/子查询 `Select`+`ParenthesedSelect`+`InExpression`，`CASE/BETWEEN/CAST/IS/NOT/括号` 由 adapter 自动下钻；未识别结构 fail-closed。
- 规定的 6 条回归用例已入 `BiSqlSecurityValidatorTest` 并全绿：`ORDER BY SLEEP(5)`、`GROUP BY BENCHMARK(...)`、`CASE WHEN SLEEP(5)`、`WHERE id IN (SELECT SLEEP(5))` → `REJECT_DANGEROUS`；`ORDER BY password`、`CASE WHEN 1=1 THEN password END` → `REJECT_COLUMN_BLOCKED`。
- **实跑**：`mvn -pl moli-ai/moli-ai-server test -Dtest=BiSqlSecurityValidatorTest,BiSqlSecurityValidatorPolicyTest` → **31/31 pass（27 validator + 4 policy）**。

**B2 已修（W7，`BiChatServiceImpl.askStream`）✅**：`resolvedUserId = resolveUserId()` 在**请求线程**先解析（第 93 行），再传入 worker 的 `executeAsk(request, sink, resolvedUserId)`（第 96 行）；`executeAsk(…, Long userId)` 改为接收已解析身份（第 122/126 行），worker 内不再读 Shiro `ThreadContext`。非流式 `ask()` 亦走同一 `executeAsk(request, null, resolveUserId())`（第 79 行）。→ SSE 请求 `ai_chat_trace.user_id` 记真实用户，越权隔离/审计归属恢复（INV-11/INV-12）。

**W8 出口修正 ✅**：`nl2sql_testset.jsonl` 新增 `v22–v27`（`category=reject_b1_bypass`）= 6 条 B1 绕过向量，validator 门禁不再回避缺口；`--validator-only --gate` 的 `reject_accuracy=1.0` 现为**真实覆盖**（含 ORDER BY/GROUP BY/CASE/子查询内危险函数与列黑名单）。「危险 100% 拦截」出口成立。

**逐周结论**：
- **W5 done**：安全底座（只读源隔离 INV-1、只读执行超时/行数 INV-7/8、AST 白名单递归、fail-closed、危险函数/列黑名单全子句覆盖）达标，31/31 绿。
- **W6 done**：Agent 编排/降级/审计/字段对齐达标（第 1 轮已核），端到端安全闭环随 B1 修复解除牵制。
- **W7 done**：图表/解读、SSE 六事件、`@RequiresPermissions`+权限 seed、网关三元组达标；B2 修复后 SSE 审计与越权隔离正确。
- **W8 done**：测试集 44 题（15 执行 / 8 NL 拒答 / 21+6 validator）、评测脚本、CI 门禁齐；执行正确率 ≥80%、拦截 100%（真实覆盖）。

AI-4 ChatBI/NL2SQL **v1.2 全周期（W5–W8）签核完成**，标 `status: done`；同步更新 `ai-capability-roadmap.md §3` / `ai-capability-schedule.md §9.1`。

> 遗留非阻断观察（不拦，后续迭代）：W6-O1（`self_lint` 命中可纠错项宜返回 `draftSql=None` 而非 `refusal`，提升自纠错率）；W5 非阻断（`BiChatReadonlyQueryExecutor` 每请求改共享 `JdbcTemplate.setQueryTimeout`，建议局部实例化）。二者不影响安全与出口。

---

### Opus 复核加固记录（2026-07-19，开 W5 前）

复核 §2 节点边界 / §3 安全逻辑 / 权限一致性，确认外部对齐（网关 `/AiServer/**`→`ai-server` StripPrefix=1、HTTP 1128 / Dubbo 20883、`ai:chat:*` 前缀、技术设计 §2/§3 一致）。同时补硬 §3 若干注入/泄露向量（W5 安全底座，Composer 不得放松）：

1. **子查询递归全覆盖**（INV-5）：白名单/危险判定递归范围从"FROM 子查询"扩到**任意位置子查询（WHERE/SELECT/HAVING/ON/EXISTS/IN）+ CTE/WITH 派生表 + 集合运算各分支**——堵 `WHERE x IN (SELECT ... FROM 非白名单表)` 等外泄。
2. **fail-closed**（INV-10 + §3.2 首行）：解析异常 / 非 Select / 未识别节点一律拒（`REJECT_DANGEROUS`），杜绝"解析失败即放行"。
3. **禁一切星号**（INV-6）：裸 `*` 外，`t.*` / `db.t.*` 同拒，防绕过列级黑名单。
4. **空白名单 = 全拒**（INV-5）：白名单未配置视为 deny-all，非"空即放行"。
5. **危险面扩充**（INV-9）：任意 `INTO`（OUTFILE/DUMPFILE/@var）、`:=` 赋值、`GET_LOCK`、`WITH RECURSIVE` 递归 CTE 纳入禁用。
6. **schema 检索纵深**（INV-16 新增）：`retrieve_schema` 索引仅按白名单建，agent 永不看见白名单外表/列。
7. **v1.2 显式非目标**（§3.2 后注）：无行级数据权限，白名单表选择须据此把控。

§1 接口签名 / §2 节点归属（Java=裁决+执行，agent 无状态）未变；§4.2 已补对应验收用例。以上属 §3 冻结安全逻辑，Composer **不得**回退或放松。

---

## 6. 相关

- 技术设计：[`bi-chatbi-nl2sql.md`](bi-chatbi-nl2sql.md)
- 路线 / 排期 / 分工：[`ai-capability-roadmap.md`](ai-capability-roadmap.md) · [`ai-capability-schedule.md`](ai-capability-schedule.md) §9
- 对外 API（落地时增量）：[`../api/ai-api.md`](../api/ai-api.md)
- 模块概要：[`ai-module-overview.md`](ai-module-overview.md)
- LLM 网关模式参考：[`kb-llm-platform-settings.md`](kb-llm-platform-settings.md)
