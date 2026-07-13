# 前端 → 后端依赖清单（meiling-ui · 2026-07-13）

> **读者**：user-center / knowledge-server 后端、DBA、运维  
> **前端仓库**：`meiling-ui`（**W1–W10 走查 ✅** · 2026-07-13）  
> **走查稿**：[operation-w1-w10-walkthrough.md](../test/operation-w1-w10-walkthrough.md)  
> **前端交付（meiling-ui 侧）**：[`meiling-ui/docs/api/operation-frontend-handoff.md`](../../meiling-ui/docs/api/operation-frontend-handoff.md)  
> **契约权威**：本仓库 [operation-frontend-handoff.md](operation-frontend-handoff.md) · [operation-backend-handoff.md](operation-backend-handoff.md)

---

## 1. 总览

| 模块 | 端口 | 阻塞新 API？ | 后端现在要做什么 |
|------|------|--------------|------------------|
| **运营管理** | `8888` | **否** | **W1–W10 走查 ✅**（2026-07-13）；共享环境需 push+部署 `b4ac176a` |
| **知识库** | `8090` | **部分**（规模化/Lint） | **P0 点验** + `KB_LLM_CONFIG_SECRET`；本地 dev 已可验 |
| **SSO** | user-center | **否** | **SSO-MENU-1 已交付**（走查 ✅ [sso-menu-frontend-walkthrough.md](../test/sso-menu-frontend-walkthrough.md)） |

### 1.1 已与前端对齐（勿再 Breaking）

| 项 | 日期 | 后端勿改 |
|----|------|----------|
| `POST /operation/project`、`/component`、**`/server`** → `data` 为新建 **id** | 2026-07-13 | 勿退回 `boolean` |
| `toVo()` 统一 `*Count`；list / `GET /{id}` / check 一致 | 2026-07-13 | `serverCount === serverIds.length` |
| 前端 **S-VO**：chips 用 VO 计数，**不**批量 `GET .../links` | 2026-07-13 | links 仅关联弹窗 |

### 1.2 前端已完工、后端无需排期（对照）

| 任务 ID | 说明 |
|---------|------|
| **S-VO** | 三管理页去掉 links 水合 |
| **DC-2** / **DC-3** | 部署中心项目优先 · 追加台账机 |
| **S-ERR-1** | 10101–10109 i18n Toast |
| **S-DEPLOY-1** | order/bi 项目名映射 |
| **W7–W10** | server create id · batch deploy · upload · task cancel |

**联合走查**：[operation-w1-w10-walkthrough.md](../test/operation-w1-w10-walkthrough.md)

---

## 2. 按任务 ID

### 2.0 状态矩阵

| 状态 | 含义 | 任务 ID |
|------|------|---------|
| 🟢 **点验** | 无新 API；环境 + 走查 | §10/§16、KB-O4、KB-BROWSE-1、KB-LLM-DB、407 SQL |
| 🟡 **可选开发** | 体验/规模化 | **DC-4**（8888）；**KBOPS-2 前端接线**（8090 API ✅） |
| ⚪ **已完成** | 前后端已对齐 | S-VO、**W1–W10**、W7–W10、DC-2/3、S-ERR-1、S-DEPLOY-1、create id、**batch deploy**、**SSO-MENU-1** |

### 2.1 联调点验（无新 API）

| 任务 ID | 后端动作 | 通过标准 |
|---------|----------|----------|
| **W1–W6** | `:8888` 含 `toVo()` | 走查稿 §2 |
| **W7–W10** | batch/task · cancel · upload | 走查稿 §3 |
| **§10/§16** | 同上；upload dev 走 **8888** | [operation-frontend.md](operation-frontend.md) §10 |
| **KB-O4** | sync fail 样本 | `npm run kb:prd` P0-O4 · [kb-sync-failure-runbook.md](../ops/kb-sync-failure-runbook.md) §9 |
| **KB-LLM-DB** | **`KB_LLM_CONFIG_SECRET`** | `GET /kb/platform/llm-config` → `encryptionReady=true` |
| **407** | 老库执行 `28_operation_topology_menu.sql` | 本机 dev 库 **已存在** 407 可跳过 |
| **SSO-MENU-1** | `30_sso_menu_system_id.sql`（老库） | ✅ 走查通过 2026-07-13 · [sso-menu-frontend-walkthrough.md](../test/sso-menu-frontend-walkthrough.md) |

### 2.2 可选排期（P3）

| 任务 ID | 服务 | 后端 | 前端 | 详稿 |
|---------|------|------|------|------|
| **DC-4** | 8888 | ⬜ `GET /operation/task/groups` | 暂缓 | [p3-optional-backend-handoff.md](p3-optional-backend-handoff.md) §1 |
| **KB-LINT-1/2** | 8090 | ✅ 分页 + `unassignedOnly` | 可选收紧 | 同上 §2 |
| **KBOPS-2** | 8090 | ✅ `GET /kb/ops/dashboard` | **可排期接线** | 同上 §3 |

> **说明**：滚动批量重启已由 **`POST /operation/deploy/batch/task`**（`b4ac176a`）覆盖；meiling-ui 任务 **DC-BE-1** 若指同一能力，可标为已交付。  
> **SSO-MENU-1** 后端 + 前端 + 联合走查 ✅（2026-07-13）；见 §5。

### 2.3 纯前端 ✅（2026-07-13）

DC-3 · S-ERR-1 · S-DEPLOY-1 · S-VO · W7–W10

---

## 3. 运营管理（8888）

> [operation-backend-handoff.md](operation-backend-handoff.md) · [user-center-api-map.md](user-center-api-map.md) §4

### 3.1 请持续保证

- `GET /operation/relations/{type}/{id}`：`recentTasks[]`、`deployRunning`、`portMatchStatus`
- `GET /operation/topology`：全图四数组 + 链接
- list / `GET /{id}`：`*Count` 与 `serverIds` 同源 `toVo()`
- `POST` create → **Long id**；`PUT/GET .../links` 同步主表
- `POST /operation/deploy/batch/task` · `POST /operation/task/{id}/cancel`
- 错误码 **10101–10109** 稳定；**10107** 可带 `data=taskId`

### 3.2 后端 smoke

```http
GET /operation/project/list
GET /operation/project/{id}
# serverCount === serverIds.length

GET /operation/topology
POST /operation/deploy/batch/task
POST /operation/task/{id}/cancel
```

---

## 4. 知识库（8090）

| 配置 / 数据 | 用途 |
|-------------|------|
| **`KB_LLM_CONFIG_SECRET`** | 平台 LLM Key 加密入库 |
| **KB-O4 fail 样本** | `kb/wiki/_p0o4-fail-test/`（dev 可逆） |
| facet 多选 | `kbTypes` / `categoryIds` 逗号分隔 · [KNOWLEDGE_API.md §2.1.3](KNOWLEDGE_API.md#213-浏览管理页筛选-ui-规范体裁--分类--平行双-facet) |

**本地 dev（2026-07-13）**：`application-dev.yml` 已设 secret 默认 + sync 脚本路径 `../kb/tools/`；8090 重启后 `encryptionReady=true`，Sync 可造 fail。

---

## 5. SSO（SSO-MENU-1）

| 文档 | 读者 |
|------|------|
| **[sso-menu-frontend-handoff.md](sso-menu-frontend-handoff.md)** | **F-SSO-1～6 契约（前后端已落地）** |
| [sso-menu-frontend-walkthrough.md](../test/sso-menu-frontend-walkthrough.md) | 走查记录（✅ 2026-07-13） |
| [sso-menu-system-isolation.md](../design/sso-menu-system-isolation.md) | 后端设计 · SQL |

**已定案（2026-07-13）**：

| 项 | 结论 |
|----|------|
| **Q3** | 门户开启且未 `enter` → `getRouters` 返回 **`[]`**；前端跳选系统 |
| **Q5** | 900 段 **`system_id=1`**（admin 内嵌）；`moli-knowledge`(39) 保留 EXTERNAL `redirectUrl` 第二入口 |

**分工**：后端 **P0/P1 ✅**；前端 **F-SSO-1～6 ✅**；**联合走查 ✅**（2026-07-13）。老库执行 `docs/sql/30_sso_menu_system_id.sql` 或新 `moli.sql` 基线。

---

## 6. 建议处理顺序

```text
① 8888：push/deploy b4ac176a（共享环境）或本地 install+重启
② 8090：KB_LLM_CONFIG_SECRET + KB-O4 → npm run kb:prd
③ DBA：407 SQL（老库按需）
④ ~~W1–W10 联合走查~~ ✅ 2026-07-13
⑤ ~~SSO-MENU-1 联合走查~~ ✅ 2026-07-13
⑥ 可选：KBOPS-2 前端接线 · KB-LINT 收紧 · DC-4 等 8888
```

---

## 7. 转发（可复制）

```
【meiling-ui · 后端配合 · 2026-07-13】

运营（8888）：
· 前端 + 后端 W1–W10 走查 ✅（2026-07-13）
· 后端 commit b4ac176a（本地；共享环境需 push+部署）
· 走查稿：monorepo docs/test/operation-w1-w10-walkthrough.md §5

关键 API（勿 Breaking）：
· toVo() *Count · POST project/component/server → data=id
· POST /operation/deploy/batch/task · POST /operation/task/{id}/cancel
· 新建服务器 body 字段 ip（非 serverIp）

8090 点验：
· KB_LLM_CONFIG_SECRET · KB-O4 fail · npm run kb:prd

SSO-MENU-1：✅ 已交付（走查 2026-07-13）
下迭代 P3：DC-4=8888待开发；KB-LINT/KBOPS=8090已交付→前端见 p3-optional-backend-handoff.md

详稿：docs/api/frontend-backend-dependencies.md
```

---

## 8. 评估与后端回复（§8.4）

### 8.1 结论

| 维度 | 评估 |
|------|------|
| **运营** | **无 API 阻塞**；**W1–W10 走查 ✅**（2026-07-13） |
| **知识库** | **点验级**；本地 secret + O4 已就绪 |
| **SSO** | **已交付**；F-SSO-1～6 + S3～S7/S10 走查 ✅（2026-07-13） |
| **文档↔代码** | **一致**（与 meiling-ui `frontend-gaps` / handoff 互引） |

### 8.2 ① 8888 版本与联调

| 项 | 状态 |
|----|------|
| **`b4ac176a`**（本地 commit） | server create id · `toVo()` · batch/links · **cancel** · handoff 文档 |
| **`ebf16fd7`** | project/component create id · order/bi deploy |
| **远程** | 分支 ahead 38 · **`b4ac176a` 未 push** |

**结论**：共享环境 last push jar **不全**；本地 `mvn -pl moli-user-center-server -am install` + 重启 → **可联调 W1–W10**。

### 8.3 ② 8090 点验环境

| 项 | 状态 |
|----|------|
| 功能 API | facet · Lint 分页 · chunk ask ✅ |
| **本地 P0** | `KB_LLM_CONFIG_SECRET` dev 默认 · O4 样本 · `encryptionReady=true` ✅ |
| **生产** | 运维注入真实 secret + 定时任务 |

### 8.4 ③ 下迭代（后端已回复）

| 项 | 排期 |
|----|------|
| **SSO-MENU-1** | ✅ **已交付 + 走查通过**（2026-07-13） |
| **DC-4** | ⬜ P3 · 8888 待开发 |
| **KB-LINT-1/2** | ✅ 8090 已交付 · 前端可选收紧 |
| **KBOPS-2** | ✅ 8090 已交付 · **前端待接线** |

### 8.5 联调前置

```text
8888：☑ W1–W10 走查（2026-07-13）  □ push+部署 b4ac176a（共享环境）
SSO：☑ 30_sso_menu_system_id.sql  ☑ sso-menu-frontend-walkthrough（2026-07-13）
8090：□ KB_LLM_CONFIG_SECRET  □ KB-O4 UI 点验  □ npm run kb:prd
```

---

## 9. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-13 | 初版三模块总览 |
| 2026-07-13 | SSO 设计链入 §4/§5 |
| 2026-07-13 | **对齐 meiling-ui**：§1.2/§2.0/§8 · 走查稿 · 前端 W7–W10 完工 · §8.4 后端回复落定 |
| 2026-07-13 | **P3 状态更正**：KB-LINT/KBOPS 8090 ✅；仅 DC-4 待 8888；前端 handoff 改版 |
