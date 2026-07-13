# 前端缺口与联调索引

> **更新**：2026-07-13 · **运营 W1–W10 走查 ✅** · **SSO-MENU-1 全量走查通过（含边界 S1/S2/S8/S9）**  
> **走查稿**：[test/operation-w1-w10-walkthrough.md](test/operation-w1-w10-walkthrough.md)  
> **给后端**：[api/frontend-backend-dependencies.md](api/frontend-backend-dependencies.md) §7–§8  
> **前端仓库索引**：[`meiling-ui/docs/frontend-gaps.md`](../../meiling-ui/docs/frontend-gaps.md)

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

### 1.3 可选增强（P3）

| 任务 ID | 后端 API | 前端动作 | 详稿 |
|---------|----------|----------|------|
| **DC-4** | ⬜ 待 `8888` | **暂缓** TaskHistoryView 分组 | [p3-optional-backend-handoff.md](api/p3-optional-backend-handoff.md) §1 |
| **KB-LINT-1/2** | ✅ `8090` 已交付 | 可选收紧服务端分页 | 同上 §2 |
| **KBOPS-2** | ✅ `8090` 已交付 | **可排期** 单请求 dashboard | 同上 §3 |

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

## 三、知识库

| 任务 | 状态 |
|------|------|
| **KB-O4** | 前端 ✅ · 本地 O4 样本已就绪 |
| **KB-BROWSE-1** | 前端 ✅ · `npm run kb:prd` P0-browse-v3 |
| **KB-LLM-DB** | 本地 dev `encryptionReady=true`；生产需 `KB_LLM_CONFIG_SECRET` |
| **KB-LINT-1/2** | ✅ 8090 API 已交付；前端可选收紧 | [p3-optional-backend-handoff.md](api/p3-optional-backend-handoff.md) §2 |
| **KBOPS-2** | ✅ 8090 API 已交付；**前端待接线** dashboard | 同上 §3 |

点验脚本：meiling-ui **`npm run kb:prd`**（文档亦称 `kb:prd-acceptance`）。

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
2. **KB** `npm run kb:prd`（8090 secret + O4 UI 点验）
3. ~~**SSO** F-SSO-1～6~~ ✅ 2026-07-13
4. 可选：**KBOPS-2 前端接线** · KB-LINT 收紧 · DC-4 等 8888 API
