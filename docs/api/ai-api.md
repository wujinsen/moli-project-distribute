# AI 服务 · API

> 模块：`moli-ai-server` · Nacos 名 `ai-server` · HTTP **1128** / Dubbo **20883**  
> 网关：`http://{gateway}:21000/AiServer/**`（StripPrefix=1）  
> 设计契约：[`bi-chatbi-nl2sql-contract.md`](../design/bi-chatbi-nl2sql-contract.md)

---

## 1. 占位 / 健康

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/demo/test` | 骨架 demo，返回 `test success` |

```bash
curl http://127.0.0.1:21000/AiServer/demo/test
```

---

## 2. ChatBI（AI-4）

**鉴权**：Shiro session（user-center 登录 `Authorization: {token}`）+ 权限码 `ai:chat:*`（见 `docs/sql/33_ai_chat_permissions.sql`）。

| 方法 | 网关路径 | 权限 | 说明 |
|------|----------|------|------|
| POST | `/AiServer/bi/chat/ask` | `ai:chat:query` | 自然语言问数；`stream=true` 返回 SSE |
| GET | `/AiServer/bi/chat/trace/{traceId}` | `ai:chat:trace` | 决策链路（本人；跨用户需 `ai:chat:trace:all`） |
| GET | `/AiServer/bi/chat/schema` | `ai:chat:query` | 白名单表/列（脱敏） |

### 2.1 `POST /bi/chat/ask`（非流式）

**Request**

```json
{
  "sessionId": "optional",
  "question": "秒杀订单有多少？",
  "stream": false,
  "maxRows": 100
}
```

**Response** `MoliResult<BiChatAskVo>`：`status` = `SUCCESS` | `REJECTED` | `ERROR`；拒答仍 `code=200` + `rejectCode`。

### 2.2 `POST /bi/chat/ask`（SSE · `stream=true`）

`Content-Type: text/event-stream`

| event | data | 说明 |
|-------|------|------|
| `stage` | `{"stage":"schema\|sql\|validate\|execute\|summarize","traceId":"..."}` | 阶段推进 |
| `sql` | `{"sql":"SELECT ..."}` | 校验通过后、执行前 |
| `chart` | `BiChartVo` JSON | 图表建议 |
| `token` | `{"delta":"..."}` | 解读逐 token |
| `done` | `BiChatAskVo` 全量 | 终态（含 REJECTED） |
| `error` | `{"code":106xx,"message":"..."}` | 请求级失败 |

```bash
curl -N -X POST http://127.0.0.1:21000/AiServer/bi/chat/ask \
  -H "Authorization: YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"question":"秒杀订单有多少？","stream":true}'
```

### 2.3 错误码

请求级：`10601` 问题无效 · `10602` sidecar 不可用 · `10603` 生成失败 · `10609/10610/10613` 执行类 · `10611/10612` trace。

拒答（HTTP 仍 success）：`REJECT_*` 见契约 §1.3。

---

## 3. NL2SQL 评测（AI-4 W8）

| 资源 | 路径 |
|------|------|
| 测试集 | `moli-ai/moli-ai-server/bi/eval/nl2sql_testset.jsonl`（**50 题**） |
| 门禁基线 | `bi/eval/baselines.json`（执行 ≥80% · 拒答 100%） |
| 脚本 | `bi/eval/eval_nl2sql.py` |

**离线（validator 危险 SQL 100% 拦截）**

```powershell
cd moli-ai/moli-ai-server/bi/eval
python eval_nl2sql.py --validator-only --gate
```

**全量 E2E（需 ai-server + ai-agent + user-center 登录；网关可选）**

```powershell
$env:MOLI_AI_BASE = "http://127.0.0.1:1128"          # 或 gateway: http://127.0.0.1:21000/AiServer
$env:MOLI_LOGIN_BASE = "http://127.0.0.1:8888"       # 无网关时直连 user-center
$env:MOLI_EVAL_USER = "admin"
$env:MOLI_EVAL_PASS = "123456"
python eval_nl2sql.py --gate
```

报告字段：`exec_accuracy`（`expect=success` 问句）· `reject_accuracy`（NL 拒答 + validator 段）· `failures[]`。

CI：`.github/workflows/bi-nl2sql-eval.yml`（PR 阻断 validator gate）。

---

## 4. Swagger

- 直连：`http://localhost:1128/swagger-ui.html`
- 网关：`http://localhost:21000/AiServer/swagger-ui.html`

---

## 5. 冒烟

见 [`ai-smoke.md`](../test/ai-smoke.md) · 发布 [`release-smoke-checklist.md`](../test/release-smoke-checklist.md) G4/G5。

---

## 6. 相关

- 模块 README：[moli-ai/README.md](../../moli-ai/README.md)
- sidecar：`moli-ai/moli-ai-server/ai-agent/README.md`
