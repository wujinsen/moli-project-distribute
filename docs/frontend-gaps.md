# 前端缺口与联调索引

> **更新**：2026-07-13 · **运营 W1–W10 ✅** · **P3 前后端 ✅** · **KB 点验 ✅**（`kb:prd` **17/17**）· **SSO-MENU-1 ✅**  
> **走查稿**：[test/operation-w1-w10-walkthrough.md](test/operation-w1-w10-walkthrough.md)  
> **跨模块**：[api/frontend-backend-dependencies.md](api/frontend-backend-dependencies.md) §7–§8  
> **前端仓库索引**：[`meiling-ui/docs/frontend-gaps.md`](../../meiling-ui/docs/frontend-gaps.md)

---

## 0. 给前端（2026-07-13 · ✅ 已完工）

| 任务 | 状态 | 落点 |
|------|------|------|
| **DC-4** | ✅ | `listTaskGroupsApi` · `TaskHistoryView.vue` |
| **KBOPS-2** | ✅ | `getKbOpsDashboardApi` · `KnowledgeOpsDashboardView.vue` |
| **KB-LINT-1/2** | ✅ | `kbLint.ts` 服务端分页信任 · `KbLintIssuesPanel.vue` |

详稿：[p3-optional-backend-handoff.md](api/p3-optional-backend-handoff.md) §6 · meiling-ui commit `a7b6fa9`。

---

## 一、运营管理（2026-07-13）

### 1.1 已完成（前后端契约已对齐）

| 任务 | 说明 |
|------|------|
| **S-VO** | `toVo()` `*Count`；去掉列表 links 水合 |
| **SVR-25/28/26b** | 拓扑 · 关联抽屉 · component-links |
| **DC-2/3** | 部署中心项目优先 · 追加台账机 |
| **W7–W10** | server create id · upload · batch deploy · task cancel |
| **S-ERR-1** · **S-DEPLOY-1** | 错误码 Toast · order/bi 映射 |
| **W1–W10 走查** | ✅ `npm run op:walkthrough` + 浏览器（2026-07-13） |

契约：[operation-frontend-handoff.md](api/operation-frontend-handoff.md) · [operation-frontend.md](api/operation-frontend.md) §10/§16

### 1.2 剩余（非新代码）

| 项 | 负责方 | 文档 |
|----|--------|------|
| 菜单 407（老库） | DBA | `docs/sql/28_operation_topology_menu.sql`（本机 dev 已有可跳过） |

~~**W1–W10** 联合走查~~ → ✅ **2026-07-13**（见 [operation-w1-w10-walkthrough.md](test/operation-w1-w10-walkthrough.md) §5 · §5.1）。

### 1.3 P3 增强（2026-07-13 · 前后端 ✅）

| 任务 ID | 后端 API | 前端 | 详稿 |
|---------|----------|------|------|
| **DC-4** | ✅ `8888` `GET /operation/task/groups`（`755abd43`） | ✅ `TaskHistoryView` 分组 | [p3-optional-backend-handoff.md](api/p3-optional-backend-handoff.md) §1 |
| **KB-LINT-1/2** | ✅ `8090` 分页 + `unassignedOnly` | ✅ 服务端分页信任 | 同上 §2 |
| **KBOPS-2** | ✅ `8090` `GET /kb/ops/dashboard` | ✅ 单请求 + legacy 降级 | 同上 §3 |

### 1.4 运维剩余（非前后端代码）

| 项 | 负责方 | 说明 |
|----|--------|------|
| **共享环境部署** | 运维 | `origin/ci/kb-sync-multi-space-gate` 已 push（含 `b4ac176a` / `755abd43` / `38570430`）；共享 `:8888`/`:8090` 需 **install + 重启 jar** |
| **`kb_llm_call_log`** | DBA | 共享/生产 MySQL 执行 [`docs/sql/18_kb_llm_call_log.sql`](sql/18_kb_llm_call_log.sql)；dev 本机已补表；缺表时 dashboard 500 → 前端降级 |
| **菜单 407** | DBA | 老库 `28_operation_topology_menu.sql`（dev 已有可跳过） |

---

## 二、SSO 菜单隔离（SSO-MENU-1 · 2026-07-13 ✅）

### 2.1 已完成（代码 + 走查）

| 任务 | 说明 |
|------|------|
| **后端 P0/P1** | `sys_menu.system_id` · `resolveRoutersForCurrentSystem` · Q3/Q5 backfill |
| **F-SSO-1～6** | 守卫 · `reloadRoutesFromServer` · enter/switch · tab 清空 · Q5 知识库双入口 |
| **S1～S10** | API 冒烟 + 边界项全部通过（2026-07-13） |

契约：[api/sso-menu-frontend-handoff.md](api/sso-menu-frontend-handoff.md) · 走查：[test/sso-menu-frontend-walkthrough.md](test/sso-menu-frontend-walkthrough.md) · 边界脚本：`test/_sso_walkthrough_boundary.ps1`

### 2.2 可选补测（非阻塞）

无剩余 SSO-MENU-1 阻塞项。复跑见走查 §6 `_sso_walkthrough_api.ps1` / `_sso_walkthrough_boundary.ps1`。

---

## 三、知识库（2026-07-13 ✅ 点验）

| 任务 | 状态 |
|------|------|
| **KB-O4** | ✅ 2026-07-13 `kb:prd` **P0-O4**（`enterprise-kb` fail 行） |
| **KB-BROWSE-1** | ✅ 2026-07-13 **P0-browse-v3** |
| **KB-LLM-DB** | ✅ 2026-07-13 `encryptionReady=true` · **REG-llm-on** |
| **KB-LINT-SCAN** | ✅ 2026-07-13 **P0-O9** · O5–O8 |
| **KB-GOV-LLM** | ✅ **REG-llm-on** + **REG-llm-off**（2026-07-13 · 17/17） |
| **KB-LINT-1/2** | ✅ 8090 + **前端收紧** | [p3-optional-backend-handoff.md](api/p3-optional-backend-handoff.md) §2 |
| **KBOPS-2** | ✅ 8090 + **前端 dashboard 接线** | 同上 §3 |
| **KB-O4 清理** | ✅ dev 样本目录已删 · Sync success（`batch=20260713181714`） | [kb-sync-failure-runbook.md](ops/kb-sync-failure-runbook.md) §9.4 |

点验脚本：meiling-ui **`npm run kb:prd`**（2026-07-13 · **17/17**）。**运维**：共享库补 `18_kb_llm_call_log.sql` → dashboard 单请求免降级。

---

## 四、文档地图

| 方向 | 文档 |
|------|------|
| **运营开工/契约** | [operation-frontend-handoff.md](api/operation-frontend-handoff.md) |
| **SSO 菜单开工** | [sso-menu-frontend-handoff.md](api/sso-menu-frontend-handoff.md) |
| **SSO 走查** | [sso-menu-frontend-walkthrough.md](test/sso-menu-frontend-walkthrough.md) |
| **后端联调通知** | [operation-backend-handoff.md](api/operation-backend-handoff.md) |
| **跨模块给后端** | [frontend-backend-dependencies.md](api/frontend-backend-dependencies.md) |
| **W1–W10 走查** | [operation-w1-w10-walkthrough.md](test/operation-w1-w10-walkthrough.md) |
| **P3 可选 API** | [p3-optional-backend-handoff.md](api/p3-optional-backend-handoff.md) |
| **知识库工作台** | [knowledge-workbench-frontend.md](api/knowledge-workbench-frontend.md) |

---

## 五、建议顺序

1. ~~**运营** W1–W10~~ ✅ 2026-07-13
2. ~~**KB** `npm run kb:prd`~~ ✅ 2026-07-13（17/17）
3. ~~**SSO** F-SSO-1～6~~ ✅ 2026-07-13
4. ~~**P3 可选**~~ ✅ 2026-07-13（DC-4 · KBOPS-2 · KB-LINT）
