# AI 服务冒烟

> 模块 **`moli-ai`**，Nacos 名 **`ai-server`**。  
> v1 骨架 + **AI-4 ChatBI**（W6/W7）。

## 前置

- MySQL：`ai_chat_trace`（`docs/sql/32_ai_chat_trace.sql`）+ 权限（`docs/sql/33_ai_chat_permissions.sql`）
- Nacos：`user-center-server`、`ai-server`、`gateway` 可见
- sidecar：`uvicorn app.main:app --host 127.0.0.1 --port 1130`（`moli-ai/moli-ai-server/ai-agent`）
- 登录 token：`POST /UserCenter/login` → `data.token`

```powershell
$login = Invoke-RestMethod -Method POST -Uri "http://127.0.0.1:21000/UserCenter/login" `
  -ContentType "application/json" -Body '{"username":"superadmin","password":"123456"}'
$h = @{ Authorization = $login.data.token }
```

---

## 用例 · 骨架

| # | 步骤 | 期望 |
|---|------|------|
| B1 | 直连 `GET http://127.0.0.1:1128/demo/test` | `200`，`test success` |
| B2 | 网关 `GET http://127.0.0.1:21000/AiServer/demo/test` | 同上 |

---

## 用例 · ChatBI（W7 端到端）

| # | 步骤 | 期望 |
|---|------|------|
| C1 | 无 token `POST .../AiServer/bi/chat/ask` | 非 200 业务成功（token 失效 / 10009） |
| C2 | 带 token 非流式问数 | `code=200`，`status=SUCCESS`，含 `sql`/`explanation`/`chart`/`traceId` |
| C3 | 带 token `stream=true` | SSE：`stage`→`sql`→`chart`→`token`*→`done` |
| C4 | 「请删除所有订单」 | `status=REJECTED`，`rejectCode=REJECT_SEMANTIC` |
| C5 | `GET .../AiServer/bi/chat/schema` | 白名单 2 表，无敏感列 |
| C6 | `GET .../AiServer/bi/chat/trace/{traceId}` | 含 `steps` 链路 |

### C2 示例

```powershell
Invoke-RestMethod -Method POST -Uri "http://127.0.0.1:21000/AiServer/bi/chat/ask" `
  -Headers $h -ContentType "application/json" `
  -Body '{"question":"秒杀订单有多少？","stream":false}'
```

### C3 SSE 示例

```powershell
curl -N -X POST "http://127.0.0.1:21000/AiServer/bi/chat/ask" `
  -H "Authorization: $($login.data.token)" `
  -H "Content-Type: application/json" `
  -d '{"question":"秒杀订单有多少？","stream":true}'
```

---

## Gradio 本地（可选）

```powershell
$env:BI_CHAT_BASE = "http://127.0.0.1:21000/AiServer"
$env:BI_CHAT_TOKEN = $login.data.token
cd moli-ai/moli-ai-server/ai-agent
python gradio_app.py
```

---

## 契约

- [ai-api.md](../api/ai-api.md) · §3 NL2SQL 评测
- [bi-chatbi-nl2sql-contract.md](../design/bi-chatbi-nl2sql-contract.md)
- [gateway-routes.md](../api/gateway-routes.md)
- 评测脚本：`moli-ai/moli-ai-server/bi/eval/eval_nl2sql.py`
