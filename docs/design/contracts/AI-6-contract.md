# AI-6 知识库 MCP Server（`kb.search` / `kb.ask` / `kb.graph`）· 施工契约（Opus 产出，Composer 施工）

> **角色**：Opus（工具 schema 语义负责人）产出，Composer 施工的**唯一契约**。
> **任务**：把知识库既有 REST 薄封装为 **MCP 工具**，让 Cursor / Claude 等客户端直连 moli 知识库做带引用问答；**复用现有 REST + token 鉴权，零新增后端端点/权限**。
> **状态**：✅ **done** · 2026-07-20 Opus §5.1 签核 · Phase A+B + 业务码 10006→`KB_UNAUTHORIZED` 映射 · `moli-knowledge/mcp/`
> **主导**：🟢 **Composer**（MCP 脚手架、REST 包装、token 接线、README、Cursor 配置样例为样板量最大部分）；**Opus 仅拍板「三工具的 input/output schema 语义 + 鉴权/ACL/只读透传不变量」（§1/§2）**。
> **上游**：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4 第 3 波 AI-6 · §6「AI-6 = `moli-knowledge/` 下薄 `mcp/`」· [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §4 W10 · §9.1 AI-6 · [`AI-2-contract.md`](AI-2-contract.md)（hybrid/`retrievalStrategy` 落地）
> **现有落地（复用，勿重造）**：`POST /kb/ask`（`AskRequest`→`AskResponse`，带 `citations`）· `GET /kb/wiki/graph`（`GraphVo`）· 网关 `/KnowledgeServer/**`（knowledge-server 8090）· 鉴权 `Authorization: <sessionId>`→Shiro 从 Redis 还原 Session（见 `docs/api/gateway-routes.md` §3）

---

## 0. 契约边界（读我）

**本契约定义**：三个 MCP 工具的名称/语义/输入输出 schema、映射到哪个既有 REST、鉴权与 ACL 透传规则、只读边界、错误映射、验收与禁改范围。

**不在本契约内（交给 Composer）**：MCP server 脚手架（Python MCP SDK + stdio）、httpx 调 REST 的样板、配置/env 读取、结果字段拷贝、README、Cursor `mcp.json` 样例、冒烟脚本。

**红线**：Composer **不得**扩出写操作工具、不得绕过 token/ACL、不得在代码内写死凭据、不得改后端 REST 契约。发现 schema 歧义 → 回 Opus 窗口改契约。

---

## 1. 架构与工具 schema 决策（Opus 冻结）

### 1.1 定位：薄代理（thin proxy），不含业务逻辑

```
Cursor / Claude ──(MCP stdio)──> moli-kb MCP server (Python, moli-knowledge/mcp/)
                                        │  透传 Authorization: <token>
                                        ▼
                             既有 REST（网关 /KnowledgeServer/** 或 8090）
                                POST /kb/ask · GET /kb/wiki/graph
```

- **无状态薄代理**：MCP server 只做「MCP 工具调用 → 拼 HTTP → 调既有 REST → 回填结果」，**不重实现检索/图谱/鉴权逻辑**（AI-2 hybrid、ACL、引用均在后端）。
- **传输**：MCP **stdio**（Cursor/Claude 以 command 方式拉起）；技术栈 **Python MCP SDK**（就近复用 `moli-knowledge/kb/tools` Python 生态，roadmap §6「薄 mcp/」）。
- **鉴权透传**：调用方 token 经 env/客户端配置注入，MCP server 每次 REST 调用带 `Authorization: <token>`；**ACL 完全由后端 Shiro 裁决**，MCP 不做本地放行。

### 1.2 三工具 → REST 映射（语义冻结）

| MCP 工具 | 语义（何时用） | 映射 REST | 关键约定 |
|----------|----------------|-----------|----------|
| `kb_search` | **只检索**：给关键词/问题，返回排序命中片段与出处，**不生成答案**（省 token、供客户端自行组织） | `POST /kb/ask`，**强制 `useLlm=false`** | 入参 `query` → 体字段 `question`；返回 `citations` 包装为 `hits`（mode=retrieval）；复用 AI-2 hybrid/`retrievalStrategy` |
| `kb_ask` | **带引用问答**：要一段有出处的答案 | `POST /kb/ask`；MCP 省略 `useLlm` 时**发 `true`**（可被入参覆盖为 false） | 全量透传 `AskResponse`；无 LLM key 时后端自动降级检索式（`mode=retrieval`） |
| `kb_graph` | **看关联**：某空间 wiki 磁盘图（`[[wikilink]]` / related / edges.jsonl） | `GET /kb/wiki/graph` | 透传 `spaceId/mode/maxNodes/minDeg`；返回 `nodes/links/meta`；**不支持 ego**（ego 属 `GET /kb/graph/ego`，本波不封装） |

> **为何 `kb_search` = `/kb/ask` + `useLlm=false`**：知识库无独立“语义搜索”端点，`/kb/ask` 的检索式模式即“召回 + 引用”，正是 search 语义；复用可直接吃到 AI-2/AI-5 的检索改进，无需新端点。  
> **`useLlm` 默认分层**：REST `AskRequest.useLlm` 默认 **false**（见 `KNOWLEDGE_API`）；`kb_ask` 工具层默认发 **true**（问答语义）；`kb_search` 工具层**恒**发 false（检索语义）。

### 1.3 工具 input/output schema（冻结）

> 空间/文档 ID 均为 **雪花 long**（JSON `number`，**禁止**标成 32-bit `int`；例 `900000000000000003`）。

**`kb_search`**
```jsonc
// input
{
  "query":            "string (required, 1..500)  → REST body.question",
  "spaceIds":         "number[] (optional, int64[]; 省略=当前用户可读空间)",
  "topK":             "number (optional; 命中数上限，默认后端 citation-top-k=8)",
  "retrievalStrategy":"string (optional; ngram|hybrid|hybrid-rerank；省略用后端默认)"
}
// output（citations 条目同构 → hits；另透传 scope）
{
  "hits": [
    {"docId":90020,"spaceId":900000000000000001,"slug":"...","title":"...","kbType":"...","snippet":"..."}
  ],
  "scope": "string|null",
  "scopeReason": "string|null"
}
```

**`kb_ask`**
```jsonc
// input
{
  "question":         "string (required, 1..500)",
  "spaceIds":         "number[] (optional, int64[])",
  "useLlm":           "bool   (optional; MCP 省略时发 true ≠ REST 默认 false)",
  "topK":             "number (optional)",
  "llmContextTopK":   "number (optional)",
  "retrievalStrategy":"string (optional; ngram|hybrid|hybrid-rerank)"
}
// output（AskResponse 全量同构透传，M-INV-4；下列为核心字段）
{
  "answer":   "string",
  "mode":     "generative|retrieval",
  "scope":    "string|null",
  "scopeReason": "string|null",
  "citations":[{"docId":..,"spaceId":..,"slug":"..","title":"..","kbType":"..","snippet":".."}],
  "provider": "string|null",
  "model":    "string|null",
  "qaLogId":  "number|null"
}
```

**`kb_graph`**（对齐 `KNOWLEDGE_API` §4.1.2 `GET /kb/wiki/graph`）
```jsonc
// input
{
  "spaceId":  "number (required, int64)",
  "mode":     "string (optional; full|summary，默认 full — 与 REST 一致；无 ego)",
  "maxNodes": "number (optional; REST 默认 full=300 / summary=50，上限 2000)",
  "minDeg":   "number (optional; 默认 0)"
}
// output（GraphVo 同构）
{
  "nodes":[{"id":"guides/...","title":"..","type":"..","deg":3}],
  "links":[{"source":"..","target":"..","type":"links_to|relates_to|..."}],
  "meta": {"totalNodes":..,"returnedNodes":..,"truncated":false,"source":"wiki_file","mode":"full|summary"}
}
```

- 工具 `description` 必须写清「何时用」（见 §1.2 语义列）与「返回带出处、不臆造」，供客户端 LLM 正确选工具。
- **不新增字段、不改 REST 语义**：`kb_ask`/`kb_graph` output 为对应 DTO **同构透传**；`kb_search` 仅允许 `query→question` 与 `citations→hits` 薄包装（条目字段不裁剪）。本波**不**暴露 `graphExpand` / `/kb/graph/ego`（另议）。

---

## 2. 鉴权 / ACL / 只读不变量（Opus 冻结）

| # | 不变量 |
|---|--------|
| M-INV-1 | **token 透传，不本地放行**：每次 REST 调用带调用方 `Authorization: <token>`；ACL 由后端 Shiro 裁决，MCP **不实现**任何本地权限判断/绕过。 |
| M-INV-2 | **只读工具面**：AI-6 只暴露 `kb_search`/`kb_ask`/`kb_graph` 三只读工具；**禁**封装任何写/治理/ingest/sync/删除端点为工具。 |
| M-INV-3 | **凭据不落码**：token / base-url 只来自 env 或客户端 MCP 配置（`MCP_KB_TOKEN`/`MCP_KB_BASE_URL`），**禁**硬编码、禁写入仓库、禁日志打印 token。 |
| M-INV-4 | **不伪造引用**：`citations`/`nodes` 原样透传后端结果；MCP 不生成、不补全、不删减来源。 |
| M-INV-5 | **错误不泄露**：后端 4xx/5xx/超时 → 映射为结构化 MCP 工具错误（`{code,message}`），message 脱敏（不回传堆栈/连接串/token）。 |
| M-INV-6 | **不改后端契约**：只调既有 REST；不新增后端端点、不新增权限码、不改 DTO。 |
| M-INV-7 | **超时与降级**：REST 超时（`MCP_KB_TIMEOUT_MS`，默认 15s）→ 返回工具错误 `KB_UPSTREAM_TIMEOUT`，不吊死客户端。 |

> **鉴权说明**：知识库以 `Authorization: <sessionId>` + Shiro Redis Session 鉴权（`gateway-routes.md §3`）。MCP 客户端在配置里放该 token；无有效 token → 后端返回未授权 → MCP 映射为 `KB_UNAUTHORIZED` 工具错误。**不设服务账号后门**（避免以固定身份越过调用方 ACL，M-INV-1）。

---

## 3. 配置 / env（键名冻结）

| env | 默认 | 说明 |
|-----|------|------|
| `MCP_KB_BASE_URL` | `http://127.0.0.1:21000/KnowledgeServer` | 后端基址（网关或直连 8090） |
| `MCP_KB_TOKEN` | 空 | 调用方 session token（`Authorization` 值）；空→工具返回 `KB_UNAUTHORIZED` |
| `MCP_KB_TIMEOUT_MS` | `15000` | REST 调用超时 |
| `MCP_KB_DEFAULT_SPACE_IDS` | 空 | 省略 `spaceIds` 时的默认作用域（逗号分隔，可空=全库） |
| `MCP_KB_DEFAULT_STRATEGY` | 空 | 省略 `retrievalStrategy` 时默认（空=后端默认） |

Cursor 侧 `mcp.json` 样例（Composer 写进 README）：
```jsonc
{
  "mcpServers": {
    "moli-kb": {
      "command": "python",
      "args": ["-m", "mcp_server"],
      "cwd": "moli-knowledge/mcp",
      "env": { "MCP_KB_BASE_URL": "http://127.0.0.1:21000/KnowledgeServer", "MCP_KB_TOKEN": "<登录后 token>" }
    }
  }
}
```

---

## 4. 分 Phase 施工清单（W10）

### Phase A · 脚手架 + `kb_search` + `kb_ask`
- Composer：`moli-knowledge/mcp/`（Python MCP SDK + stdio）、`requirements.txt`、`config.py`（§3 env）、httpx REST client（统一带 `Authorization`、超时、错误映射 §2）。
- Composer：注册 `kb_search`（`/kb/ask` `useLlm=false`）、`kb_ask`（`/kb/ask` `useLlm=true`），**严格按 §1.3 schema + §1.2 description 语义**。
- Composer：README（启动、Cursor `mcp.json`、token 获取方式）。
- 出口：`list_tools` 返回两工具及 schema；本地对已启动 knowledge-server 调通，返回带 `citations`。

### Phase B · `kb_graph` + 鉴权/降级 + 演示
- Composer：`kb_graph`（`/kb/wiki/graph`），透传 `spaceId/mode/maxNodes/minDeg`。
- Composer：错误映射（`KB_UNAUTHORIZED`/`KB_UPSTREAM_TIMEOUT`/`KB_UPSTREAM_ERROR`）、token 缺失/失效路径、脱敏。
- Composer：Cursor 端 `@moli-kb` **带引用问答演示**（README 录步骤/截图）；冒烟脚本 `mcp/smoke.py`（起 server → list_tools → 各工具一次）。
- 出口：Cursor 中经 MCP 完成一次带引用问答（`kb_ask`）+ 一次检索（`kb_search`）+ 一次图查询（`kb_graph`）。

---

## 5. 验收标准 + Composer 禁改范围

### 5.1 验收标准

- [x] **工具可发现**：`list_tools` 返回 `kb_search`/`kb_ask`/`kb_graph` 三工具，input schema 与 §1.3 一致，description 含「何时用 + 带出处」。
- [x] **端到端（Cursor 演示可复现）**：`kb_ask` 返回 `answer` + 非空 `citations`；`kb_search` 返回排序 `hits`；`kb_graph` 返回 `nodes/links/meta`。
- [x] **鉴权透传（M-INV-1）**：带有效 token → 正常；无/失效 token → `KB_UNAUTHORIZED` 工具错误（不崩）；对无读权限空间 → 后端 ACL 拦截，MCP 如实回错。
- [x] **只读面（M-INV-2）**：工具清单只含三只读工具，无任何写/sync/govern/ingest 工具。
- [x] **凭据安全（M-INV-3/5）**：token 仅来自 env/配置，代码与日志无明文 token；错误 message 无堆栈/连接串。
- [x] **不改后端**：无新增后端端点/权限/DTO 改动；仅调既有 REST。
- [x] **降级（M-INV-7）**：后端不可达/超时 → `KB_UPSTREAM_TIMEOUT`/`KB_UPSTREAM_ERROR`，客户端不吊死。

### 5.2 Composer 禁改范围（Do-Not-Touch）

- ❌ 扩出**写/治理/ingest/sync/删除**类工具（AI-6 只读面，M-INV-2）。
- ❌ 绕过 token/ACL：不设服务账号后门、不在 MCP 本地判权放行（M-INV-1）。
- ❌ 硬编码 token/base-url / 打印 token（M-INV-3）。
- ❌ 二次加工或裁剪 `citations`/`nodes`（不伪造、不丢引用，M-INV-4）。
- ❌ 改后端 REST 契约 / 新增后端端点或权限码（M-INV-6）；本任务**不**碰 Java。
- ❌ 改 `kb_search`=检索式（`useLlm=false`）、`kb_ask`=可生成 的语义分工；不合并成一个工具。

---

## 6. 实现清单 + 未决问题（Composer 回填区）

> Composer 在此追加「已实现工具/文件清单」与「未决问题」；鉴权/只读边界相关未决**不得自行拍板**，回 Opus 窗口改契约。

### Phase A 实现清单（2026-07-20 · kb_search + kb_ask · stdio）

| 项 | 路径 | 说明 |
|----|------|------|
| 依赖 | `mcp/requirements.txt` | `mcp>=1.4,<2` · `httpx` |
| 配置 | `mcp/config.py` | §3 env：`MCP_KB_BASE_URL` / `MCP_KB_TOKEN` / `MCP_KB_TIMEOUT_MS` / 默认 space/strategy |
| REST 客户端 | `mcp/kb_client.py` | `POST /kb/ask` · 统一 `Authorization` · 超时 · 错误映射入口 |
| 错误 | `mcp/errors.py` | `KB_UNAUTHORIZED` / `KB_UPSTREAM_TIMEOUT` / `KB_UPSTREAM_ERROR` · M-INV-5 脱敏 |
| 工具 | `mcp/tools_impl.py` | `kb_search`（`useLlm=false` → hits）· `kb_ask`（默认 `useLlm=true` → AskResponse 透传）· §1.3 schema |
| 入口 | `mcp/mcp_server.py` | MCP SDK stdio · `list_tools` → 两工具 · `call_tool` 分发 |
| 文档 | `mcp/README.md` | 启动 · Cursor `mcp.json` · token 获取 |
| 冒烟 | `mcp/smoke_local.py` | login → `kb_search`/`kb_ask` · citations 非空 |

**验证**：`python smoke_local.py` → `list_tools: ['kb_search', 'kb_ask']` · hits/citations 非空（8090 + admin 登录）。

### Phase B 实现清单（2026-07-20 · kb_graph + 错误映射 + 冒烟）

| 项 | 路径 | 说明 |
|----|------|------|
| REST GET | `mcp/kb_client.py` | `get_wiki_graph()` → `GET /kb/wiki/graph` · 透传 `spaceId/mode/maxNodes/minDeg` |
| 工具 | `mcp/tools_impl.py` | `kb_graph` · GraphVo 同构透传 `nodes/links/meta` · §1.3 schema + description |
| 错误 | `mcp/errors.py` | `sanitize_message` · 401/403→`KB_UNAUTHORIZED` · timeout→`KB_UPSTREAM_TIMEOUT` · 其它→`KB_UPSTREAM_ERROR`（M-INV-5/7） |
| 入口 | `mcp/mcp_server.py` | `list_tools` → 三工具 · 意外异常不泄露堆栈到客户端 |
| 冒烟 | `mcp/smoke.py` | MCP stdio E2E：`list_tools` + 三工具各调一次 |
| 文档 | `mcp/README.md` | Cursor `@moli-kb` 演示步骤（kb_ask / kb_search / kb_graph） |

**冒烟结果**（2026-07-20 · knowledge-server 8090 · admin 登录）：

```
python smoke.py
list_tools: ['kb_search', 'kb_ask', 'kb_graph']
kb_search hits=5 scope='[guide, service]'
kb_ask mode=retrieval citations=5
kb_graph nodes=50 links=378 meta.source='wiki_file' mode='summary'
OK
```

**Cursor 演示**：README §「Cursor demo」— `@moli-kb` + `kb_ask` 带引用问答可复现。

### Opus §5.1 签核（2026-07-20）→ **✅ done**

| §5.1 | 结果 | 证据 |
|------|------|------|
| list_tools + schema | ✅ | `smoke.py` → `['kb_search','kb_ask','kb_graph']`；`tools_impl.py` 对齐 §1.3；description 含 Use when + sources/citations |
| E2E 三工具 | ✅ | search hits=5 · ask citations=5 · graph nodes=50/links=378/`wiki_file` |
| 鉴权 | ✅ | 缺 token→`KB_UNAUTHORIZED`；失效 token（HTTP 200 + biz **10006**）→`KB_UNAUTHORIZED`（签核时补 `kb_client` 业务码映射） |
| 只读面 | ✅ | 仅三工具，无 sync/ingest/govern |
| 凭据 | ✅ | token 仅 `MCP_KB_TOKEN`；日志只打 `tool call name=`；`sanitize_message` |
| 不改后端（M-INV-6） | ✅ | AI-6 交付仅 `moli-knowledge/mcp/` Python 薄代理，未为本任务改 Java DTO/端点 |
| 超时/降级 | ✅ | `errors.map_request_error` → `KB_UPSTREAM_TIMEOUT` / `KB_UPSTREAM_ERROR` |

- 未决问题：无。

---

## 7. 相关

- 路线 / 排期 / 分工：[`../ai-capability-roadmap.md`](../ai-capability-roadmap.md) §4 第 3 波 · §6 · [`../ai-capability-schedule.md`](../ai-capability-schedule.md) §4 W10 · §9.1
- 复用 REST：`POST /kb/ask`（`AskRequest`/`AskResponse`）· `GET /kb/wiki/graph`（`GraphVo`）· 鉴权 `docs/api/gateway-routes.md §3`
- 检索能力来源：[`AI-2-contract.md`](AI-2-contract.md)（hybrid/`retrievalStrategy`）· [`AI-5-contract.md`](AI-5-contract.md)（GraphRAG，`kb_ask`/`kb_search` 自动受益）
- 对外 API：[`../api/KNOWLEDGE_API.md`](../api/KNOWLEDGE_API.md)
