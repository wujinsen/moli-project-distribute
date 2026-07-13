# 前端缺口与联调索引

> **更新**：2026-07-13 · **运营 W1–W10 前端已完工** · **SSO-MENU-1 前端可开工**  
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

契约：[operation-frontend-handoff.md](api/operation-frontend-handoff.md) · [operation-frontend.md](api/operation-frontend.md) §10/§16

### 1.2 剩余（联合走查 · 非新代码）

| 项 | 负责方 | 文档 |
|----|--------|------|
| **W1–W10** + §10/§16 | 前端 + 后端 | [operation-w1-w10-walkthrough.md](test/operation-w1-w10-walkthrough.md) |
| 菜单 407（老库） | DBA | `docs/sql/28_operation_topology_menu.sql`（本机 dev 已有可跳过） |

### 1.3 可选增强

| 任务 ID | 阻塞 |
|---------|------|
| **DC-4** | 后端 task 聚合 VO |
| **KB-LINT-1/2** · **KBOPS-2** | 见 dependencies §2.2 |

---

## 二、SSO 菜单隔离（SSO-MENU-1 · 2026-07-13 可开工）

| 任务 | 说明 |
|------|------|
| **F-SSO-1～6** | 守卫 · `reloadRoutesFromServer` · enter/switch 刷新 · Q5 知识库双入口 |

契约：[api/sso-menu-frontend-handoff.md](api/sso-menu-frontend-handoff.md) · 走查：[test/sso-menu-frontend-walkthrough.md](test/sso-menu-frontend-walkthrough.md)

**依赖**：全量 E2E 需后端 `system_id` 过滤；前端守卫逻辑可先行。

---

## 三、知识库

| 任务 | 状态 |
|------|------|
| KB-O4 · KB-BROWSE-1 | 前端 ✅ · **点验** `kb:prd-acceptance` |
| KB-LLM-DB | 本地 dev `encryptionReady=true`；生产需 secret |
| KB-LINT-1/2 | 可选 P2 |

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
| **知识库工作台** | [knowledge-workbench-frontend.md](api/knowledge-workbench-frontend.md) |

---

## 五、建议顺序

1. **运营** W1–W10（`:8888` commit `b4ac176a` + 重启）
2. **KB** `kb:prd-acceptance`（8090 secret + O4）
3. **SSO** F-SSO-1～6（与后端 P0 并行）· [sso-menu-frontend-handoff.md](api/sso-menu-frontend-handoff.md)
4. 可选：DC-4 · KB-LINT
