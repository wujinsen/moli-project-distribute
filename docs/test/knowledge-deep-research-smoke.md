# DeepResearch 冒烟（AI-10 Phase B）

> Sidecar：`moli-knowledge/deep-research/` · Java：`POST /kb/research` · 契约：[`AI-10-contract.md`](../design/contracts/AI-10-contract.md)

## 前置

- MySQL：`kb_research_run`（`docs/sql/36_kb_research_run.sql`）
- `kb.research.enabled=true` · sidecar `:8095` · KnowledgeServer `:8090`
- 登录 token（ACL 透传）

```powershell
$login = Invoke-RestMethod -Method POST -Uri "http://127.0.0.1:21000/UserCenter/login" `
  -ContentType "application/json" -Body '{"username":"superadmin","password":"123456"}'
$h = @{ Authorization = $login.data.token }
```

## 用例

| # | 步骤 | 期望 |
|---|------|------|
| R1 | Sidecar `GET http://127.0.0.1:8095/health` | `status=ok` |
| R2 | `python moli-knowledge/deep-research/smoke.py --topic "茉莉微服务架构"` | 两次运行均有 `reportMd`；`slugSetStable=true`（同 KB 版本） |
| R3 | `POST /KnowledgeServer/kb/research/start` + SSE stream | `progress` 含 planner/retriever/writer/reviewer；`complete` 含 `reportMd` + `[[slug]]` |
| R4 | `writeback=true` | `ingestJobId` + `outputPath=wiki-moli/develop/outputs/{slug}.md` |
| R5 | 压低 `latencyBudgetMs=1000` | `degraded=true`，仍有大纲级 `reportMd` |
| R6 | Guard 注入样例（AI-9 开） | `GUARD_BLOCK`，不进入 Writer |

### R3 示例

```powershell
$body = '{"topic":"茉莉微服务架构","spaceId":900000000000000003,"writeback":false}'
$start = Invoke-RestMethod -Method POST -Uri "http://127.0.0.1:21000/KnowledgeServer/kb/research/start" `
  -Headers $h -ContentType "application/json" -Body $body
curl -N -H "Authorization: $($login.data.token)" `
  "http://127.0.0.1:21000/KnowledgeServer/kb/research/$($start.data.runId)/stream"
```

### 单测（CI）

```powershell
cd moli-knowledge/deep-research
pytest tests/ -q

cd moli-knowledge/moli-knowledge-server
mvn test -Dtest=KbResearchPropertiesTest,KbResearchClientTest
```

## 样例报告路径

- Sidecar 审计：`moli-knowledge/deep-research/runs/{runId}/report.md`
- Wiki 权威页（writeback）：`moli-knowledge/kb/wiki-moli/develop/outputs/{slug}.md`（经 Ingest commit）
