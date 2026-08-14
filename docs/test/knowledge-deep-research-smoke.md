# DeepResearch 冒烟与验收（AI-10 · 含前端深链）

> Sidecar：`moli-knowledge/deep-research/` · Java：`POST /kb/research` · 前端：`knowledge/research/index`  
> 契约：[AI-10-contract.md](../design/contracts/AI-10-contract.md) · 对接：[knowledge-workbench-frontend.md §1.4](../api/knowledge-workbench-frontend.md#14-主题调研--ingest-回写深链ai-10--p1)

---

## 0. 前置（环境与权限）

| 项 | 说明 |
|----|------|
| DDL | `docs/sql/36_kb_research_run.sql`（`kb_research_run`） |
| 菜单 | `docs/sql/37_kb_research_menu.sql`（menu **911** · 侧栏「主题调研」） |
| 配置 | `kb.research.enabled=true` · sidecar 默认 `:8095` · KnowledgeServer `:8090`（经网关 `:21000`） |
| LLM | sidecar 环境变量或 Java `kb.llm` 可达（Planner/Writer/Reviewer） |
| KB 数据 | 目标空间已 Sync，有可检索 wiki 页（建议 `wiki-moli` 或 `enterprise-kb`） |
| 账号 | 列表/调研：`kb:ask:list`；回写 Ingest：`kb:ingest:job` + `kb:ingest:commit` |

```powershell
$login = Invoke-RestMethod -Method POST -Uri "http://127.0.0.1:21000/UserCenter/login" `
  -ContentType "application/json" -Body '{"username":"superadmin","password":"123456"}'
$h = @{ Authorization = $login.data.token }
```

---

## 1. 快速冒烟（约 10 分钟 · API + 单测）

| # | 步骤 | 期望 | ✓ |
|---|------|------|---|
| R1 | Sidecar `GET http://127.0.0.1:8095/health` | `status=ok` | |
| R2 | `python moli-knowledge/deep-research/smoke.py --topic "茉莉微服务架构"` | 两次均有 `reportMd`；`slugSetStable=true` | |
| R3 | `POST /KnowledgeServer/kb/research/start` + SSE stream | `progress` 含 planner/retriever/writer/reviewer；`complete` 含 `reportMd` + `[[slug]]` | |
| R4 | 同上 `writeback=true` | `ingestJobId` + `outputPath=wiki-moli/develop/outputs/{slug}.md` | |
| R5 | 压低 `latencyBudgetMs=1000` | `degraded=true`，仍有大纲级 `reportMd` | |
| R6 | Guard 注入样例（AI-9 开） | `GUARD_BLOCK`，不进入 Writer | |

### R3 示例

```powershell
$body = '{"topic":"茉莉微服务架构","spaceId":900000000000000003,"writeback":false}'
$start = Invoke-RestMethod -Method POST -Uri "http://127.0.0.1:21000/KnowledgeServer/kb/research/start" `
  -Headers $h -ContentType "application/json" -Body $body
curl -N -H "Authorization: $($login.data.token)" `
  "http://127.0.0.1:21000/KnowledgeServer/kb/research/$($start.data.runId)/stream"
```

### 单测（CI · 合入前）

```powershell
cd moli-knowledge/deep-research
pytest tests/ -q

cd moli-knowledge/moli-knowledge-server
mvn test -Dtest=KbResearchPropertiesTest,KbResearchClientTest
```

D-INV-5 回归（发版前）：[AI-10-contract.md §6.1](../design/contracts/AI-10-contract.md#61-d-inv-5-回归门禁ai-10-合入--发版前必跑) · `eval_ask` / Guard 金样 / ChatBI validator。

---

## 2. 前端联调验收（meiling-ui · 约 15 分钟）

**入口**：企业知识库 → **主题调研** `/knowledge/research`（或菜单 911）

| # | 步骤 | 期望 | ✓ |
|---|------|------|---|
| F1 | 选空间 + 填主题，**不勾选**回写，提交 | SSE 四阶段进度；报告区渲染 Markdown；侧栏有 citations / coverage | |
| F2 | 报告正文 | 含 `[[slug]]` 可识别样式（或原样展示 wikilink） | |
| F3 | 无 `kb:ingest:*` 权限账号 | 「回写 outputs/」禁用或提示 `writebackHint` | |
| F4 | 勾选回写，提交（需 ingest 权限） | 完成后显示 `ingestJobId`；出现 **打开 Ingest 批次** 按钮 | |
| F5 | 点击 **打开 Ingest 批次** | 跳转 `/knowledge/ingest?jobId={id}` → 自动打开批次详情 | |
| F6 | 观察地址栏 | URL **replace** 为 canonical `?id={id}`（无残留 `jobId`） | |
| F7 | 批次详情 | Plan / 草稿含 `develop/outputs/{slug}`；状态与 API 一致 | |
| F8 | 浏览器直接打开 `/knowledge/ingest?jobId={已知批次id}` | 与 F5/F6 相同：打开详情 + URL 规范化为 `?id=` | |
| F9 | 浏览器打开 `/knowledge/ingest?id={id}` | 打开同一批次（canonical 路径） | |
| F10 | `kb.research.enabled=false` 或 sidecar 不可达 | 前端友好错误；不白屏 | |

**Ingest 深链约定**（实现：`KnowledgeIngestWorkbenchView.vue`）

| Query | 含义 |
|-------|------|
| `id` | **canonical** · 打开批次 Expert 详情 |
| `jobId` | **别名**（DeepResearch / 外链）· 进入后 replace 为 `id` |
| `express=1` | Express 模式（与 `id` 组合，见 [ingest-workbench-frontend.md §1](../api/ingest-workbench-frontend.md#1-页面与路由)） |

---

## 3. 回写 → Ingest → commit 全链路（P1 · 可选深度）

> 验证 D-INV-1：只经 `/kb/ingest/*`，禁直写 wiki 磁盘。

| # | 步骤 | 期望 | ✓ |
|---|------|------|---|
| W1 | DeepResearch `writeback=true` 成功 | 返回 `ingestJobId`；后端创建 Ingest job + plan + draft | |
| W2 | Ingest 工作台打开该批次 | Plan 中 create/enrich 指向 `wiki-moli/develop/outputs/` | |
| W3 | 批次 **Lint** | 无 blocker ERROR（或有则 UI 展示可修复项） | |
| W4 | **Commit**（含 sync） | `committed=true`；磁盘 `kb/wiki-moli/develop/outputs/{slug}.md` 存在 | |
| W5 | Web 文档管理 / 浏览 | 新页可检索；`sources` 可追溯 DeepResearch | |
| W6 | 同 topic 二次 writeback | 新 job 或 enrich 策略符合 Ingest 规则；无静默覆盖 wiki | |

Ingest 通用场景：[knowledge-ingest-acceptance.md](knowledge-ingest-acceptance.md)

---

## 4. 验收勾选总表（发版 / 功能签收）

复制本节到工单或 PR 描述。

### P0 · 必过

- [ ] R1 sidecar health
- [ ] R3 API start + SSE complete（含 `reportMd`）
- [ ] F1 前端主题调研基本流（无回写）
- [ ] F8 `/knowledge/ingest?jobId=` 深链打开批次 + URL 规范化
- [ ] D-INV-5：`/kb/ask` 行为未变（`eval_ask` 或抽样手测）

### P1 · 启用 DeepResearch 时

- [ ] R4 writeback 返回 `ingestJobId`
- [ ] F4–F7 前端回写 + 打开 Ingest 批次
- [ ] W1–W4 回写 commit 落盘（若生产开启 writeback）
- [ ] R5 预算降级 · R6 Guard 阻断（环境有 AI-9 时）
- [ ] pytest 13/13 · Java research 单测绿

### P2 · 运维

- [ ] SQL `36` + `37` 已执行
- [ ] `kb.research.enabled` 与 sidecar 部署文档一致
- [ ] 菜单 911 对目标角色可见

---

## 5. 样例路径

| 产物 | 路径 |
|------|------|
| Sidecar 审计 | `moli-knowledge/deep-research/runs/{runId}/report.md` |
| Wiki 权威页（writeback + commit 后） | `moli-knowledge/kb/wiki-moli/develop/outputs/{slug}.md` |
| 运行记录 | MySQL `kb_research_run` |

---

## 6. 失败排查

| 现象 | 查 |
|------|-----|
| 404 / research disabled | `kb.research.enabled`、KnowledgeServer 重启 |
| SSE 无 complete | sidecar 日志；LLM 超时；`GET /health` |
| writeback 无 `ingestJobId` | 权限 `kb:ingest:job`/`commit`；Ingest 模块开关 |
| `?jobId=` 进列表不打开详情 | 前端 `KnowledgeIngestWorkbenchView` `onMounted` normalize；批次 id 是否存在 |
| citations 空 | 空间无索引 / ACL；`/kb/ask` 召回是否正常 |
