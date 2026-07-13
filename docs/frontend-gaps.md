# 前端缺口与联调索引

> **更新**：2026-07-13 · **运营 W1–W10 走查 ✅** · **KB 点验 ✅**（`kb:prd` 16/17）· **SSO-MENU-1 全量走查通过**  
> **走查稿**：[test/operation-w1-w10-walkthrough.md](test/operation-w1-w10-walkthrough.md)  
> **给后端**：[api/frontend-backend-dependencies.md](api/frontend-backend-dependencies.md) §7–§8  
> **前端仓库索引**：[`meiling-ui/docs/frontend-gaps.md`](../../meiling-ui/docs/frontend-gaps.md)

---

## 0. 给前端（一键开工 · 2026-07-13）

**后端已全部就绪，无 API 阻塞。** 主入口：[p3-optional-backend-handoff.md](api/p3-optional-backend-handoff.md) §0。

| 优先级 | 任务 | 服务 | 落点 |
|--------|------|------|------|
| ① | **DC-4** 任务历史分组 | `:8888` | `listTaskGroupsApi` · `TaskHistoryView.vue` |
| ② | **KBOPS-2** Dashboard 单请求 | `:8090` | `GET /kb/ops/dashboard` |
| ③ | **KB-LINT** 分页收紧 | `:8090` | `kbLint.ts` · 质量 Tab |

**本地前置**：重启 `:8888`（含 `755abd43` DC-4）、`:8090`（含 `38570430` kb 路径修复）后再联调。

```
详稿：docs/api/p3-optional-backend-handoff.md §0.1（可贴进 meiling-ui 对话）
运营契约：operation-frontend.md §11.2.1 · 验收：test/operation-task-groups-acceptance.md
KB 点验：npm run kb:prd ✅ 16/17（2026-07-13）
```

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

### 1.3 可选增强（P3 · 后端均已交付 · 前端可排期）

| 任务 ID | 后端 API | 前端动作 | 详稿 |
|---------|----------|----------|------|
| **DC-4** | ✅ `8888` `GET /operation/task/groups` | **可开工** TaskHistoryView 分组视图 | [p3-optional-backend-handoff.md](api/p3-optional-backend-handoff.md) §1 |
| **KB-LINT-1/2** | ✅ `8090` 已交付 | 可选收紧服务端分页 | 同上 §2 |
| **KBOPS-2** | ✅ `8090` 已交付 | **可开工** 单请求 dashboard | 同上 §3 |

> **给前端**：复制 [p3-optional-backend-handoff.md](api/p3-optional-backend-handoff.md) §0 或 §0.1 即可开工。

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
| **KB-GOV-LLM** | ✅ **REG-llm-on**；⏭ REG-llm-off UI（关 LLM 后可选） |
| **KB-LINT-1/2** | ✅ 8090 API 已交付；前端可选收紧 | [p3-optional-backend-handoff.md](api/p3-optional-backend-handoff.md) §2 |
| **KBOPS-2** | ✅ 8090 API 已交付；**前端待接线** dashboard | 同上 §3 |

点验脚本：meiling-ui **`npm run kb:prd`**（2026-07-13 · **16/17**；日志 `kb-prd-acceptance.log`）。

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
2. ~~**KB** `npm run kb:prd`~~ ✅ 2026-07-13（16/17）
3. ~~**SSO** F-SSO-1～6~~ ✅ 2026-07-13
4. **P3 可选（三项 API 已就绪）**：DC-4 TaskHistoryView 分组 → KBOPS-2 dashboard → KB-LINT 收紧（见 [p3-optional-backend-handoff.md](api/p3-optional-backend-handoff.md) §4）
