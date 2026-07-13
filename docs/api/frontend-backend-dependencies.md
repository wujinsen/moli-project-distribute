# 前端 → 后端依赖清单（meiling-ui · 2026-07-13）

> **读者**：user-center / knowledge-server 后端、DBA、运维  
> **前端开工** → **[operation-frontend-handoff.md](operation-frontend-handoff.md)**（S-VO 任务 + 走查）· 索引 [frontend-gaps.md](../frontend-gaps.md)  
> **前端状态**：meiling-ui 主线已联调；本文汇总**三模块阻塞、运维配置与后端已交付项**；前端落地见 handoff  
> **Breaking 已对齐**：`POST /operation/project`、`POST /operation/component`、**`POST /operation/server`** 的 `data` 已为新建 **Long id**（见 [operation-backend-handoff.md](operation-backend-handoff.md)）

**相关入口**：[operation-frontend.md](operation-frontend.md) · [KNOWLEDGE_API.md](KNOWLEDGE_API.md) · [user-center-api-map.md](user-center-api-map.md)

---

## 0. 给前端同学（可立即开工）

| 任务 | 文档 | 后端依赖 |
|------|------|----------|
| **S-VO 关系计数** | [operation-frontend-handoff.md §2](operation-frontend-handoff.md#2-本轮前端任务s-vo--关系计数) | ✅ `toVo()` 派生 |
| **部署中心增强** | [operation-frontend-handoff.md §3](operation-frontend-handoff.md#3-部署中心与异步任务2026-07-13-新增) | ✅ upload / batch/task / cancel |
| **create 返回 id** | handoff §3.1 · §4 | ✅ project/component/**server** |
| Phase R 收尾 | [operation-frontend.md §13](operation-frontend.md#13-phase-r-改造--前端必改2026-07-11) | ✅ |
| 浏览器走查 | handoff §5（W1–W10） | ✅ |
| 知识库 facet / Lint 分页 | [knowledge-workbench-frontend.md](knowledge-workbench-frontend.md) | ✅ |
| SSO 菜单隔离 | [sso-menu-system-isolation.md](../design/sso-menu-system-isolation.md) · 本文 §4 | ⬜ 等后端 P0 |

**转发前端**：见 [operation-frontend-handoff.md §7.1](operation-frontend-handoff.md#71-转发前端可复制)

**本地**：重启 user-center 后发版/meiling-ui proxy `8888` 后再跑 handoff §5（W1–W10）。

---

## 1. 总览：三模块阻塞情况

| 模块 | 端口 / 网关 | 前端状态 | 后端阻塞 | 说明 |
|------|-------------|----------|----------|------|
| **运营管理** | user-center `:8888` | ✅ 主线完成 | **无** | 后端契约已齐（S-VO、deploy、cancel）；前端对接见 handoff §2–§3 |
| **知识库** | knowledge `:8090` | ✅ 主线完成 | **环境/配置** | 功能 API 已落地；生产需 `KB_LLM_CONFIG_SECRET`、定时 sync/lint、浏览 facet 多选已对接 |
| **SSO / 多系统** | user-center `:8888` | 🔵 待后端 | **菜单按系统隔离** | `getRouters` 未按 `sys_menu.system_id` 过滤；切换系统后菜单可能串台 |

**本地联调基线**：Vite `http://127.0.0.1:5141` → proxy `8888` / `8090`；账号 `admin`/`123456` 或 `superadmin`/`aa123456`。

---

## 2. 运营管理（user-center · 8888）

> 权威契约：[operation-frontend.md](operation-frontend.md) · [operation-deploy-api.md](operation-deploy-api.md) · [operation-backend-handoff.md](operation-backend-handoff.md)

### 2.1 VO 字段契约（2026-07-13 · `toVo()` 统一派生）

| 字段 | list / detail / check | 前端 |
|------|----------------------|------|
| `serverCount` / `componentCount`（项目） | ✅ 同源 `toVo()` | **`row.serverCount`**；恒等 `serverIds.length` |
| `serverCount` / `projectCount`（组件） | ✅ | 同上 |
| `projectCount` / `componentCount`（服务器） | ✅ | 同上 |
| `serverIds` | ✅ | 关联弹窗仍 `GET .../links` |
| `POST /operation/server` 响应 | ✅ **Long id**（2026-07-13，对齐 project/component） |

**实现**：`OperationProjectServiceImpl.toVo()` 等 — `serverCount = resolveServerIds(...).size()`；已移除 `fillRelationCounts` 分叉逻辑。

### 2.2 Smoke 清单（浏览器 · 后端配合项）

| # | 场景 | 通过标准 | 后端注意 |
|---|------|----------|----------|
| 1 | 项目关联 1 台 | list `serverCount=1`；`GET /operation/relations/project/{id}` servers=1 | links 同步主表 |
| 2 | 新建项目 | `POST` 响应 `data` 为数字 id | Breaking 已上线 |
| 3 | 部署中心五服务 | `GET /operation/deploy/presets` 含 order/bi | 目标机 `moli-service.sh` |
| 4 | 远程启停 | 返回 `taskId`；轮询 `GET /operation/task/{id}` | `OPS_DEPLOY_ENABLED=true` |
| 5 | 拓扑图 | `GET /operation/topology` 200 | 菜单 407 或 supplement 路由 |
| 6 | 生产 serverId | 未传 serverId → **10109** | `allow-local=false` |
| 7 | 新建服务器 | `POST /operation/server` → `data` 为数字 id | Breaking 与 project/component 一致 |
| 8 | 上传并发布 | `POST /operation/file/upload` → `taskId`；轮询至 `finished` | `OPS_UPLOAD_ENABLED=true` + SSH |
| 9 | 批量滚动重启 | `POST /operation/deploy/batch/task` → 单 taskId；日志 `[BATCH]` | 替代 N 次单任务扇出 |
| 10 | 任务取消 | `POST /operation/task/{id}/cancel` → `status=cancelled` | 协作式；SSH 中须等当前步结束 |

完整走查：[operation-frontend-handoff.md §5](operation-frontend-handoff.md#5-浏览器走查)（W1–W10）· [operation-relations-topology-acceptance.md](../test/operation-relations-topology-acceptance.md) · [operation-deploy-center-acceptance.md](../test/operation-deploy-center-acceptance.md)。

### 2.3 DBA / 运维事项

| 项 | 脚本 / 配置 | 说明 |
|----|-------------|------|
| 拓扑菜单 | `docs/sql/28_operation_topology_menu.sql` | 老库未执行时前端用 **supplement 路由** 兜底任务历史/拓扑 |
| 部署中心三开关 | `OPS_DEPLOY_ENABLED` / `OPS_UPLOAD_ENABLED` / `OPS_COMMAND_ENABLED` | 默认 false，本地联调常漏配 |
| SSH 密钥 | `OPS_SECRET_KEY` | 远程 deploy/upload/command 必填 |
| SQL 顺序 | [sql-migration-order.md](../ops/sql-migration-order.md) | 17→28 按序；新环境可用 `moli.sql` 一次建库 |

### 2.4 后端已交付 · 前端待对接（2026-07-13）

| 优先级 | 项 | 状态 / 前端动作 |
|--------|-----|----------------|
| P1 | 详情 VO 计数字段（`toVo()`） | ✅ 后端 · 前端用 `row.serverCount`（handoff §2） |
| P2 | 批量滚动重启 | ✅ `POST /operation/deploy/batch/task` · 前端 `createDeployBatchTaskApi`（handoff §3.3） |
| P2 | `POST /operation/server` 返回 id | ✅ · 前端 `addServerApi` → `number`（handoff §3.1） |
| P3 | 批量 links | ✅ `GET .../links/batch?ids=`（最多 50）· 可选减 N+1，**勿**用于 chips（handoff §3.5） |
| P3 | 任务取消 | ✅ `POST /operation/task/{id}/cancel` · 前端任务面板加取消（handoff §3.4） |
| — | `deploy_running` 全量 SSH 同步 | ⬜ 路线图 R3.2 · `ops.deploy.status-sync-mode=ssh` |

---

## 3. 知识库（knowledge-server · 8090）

> 权威契约：[KNOWLEDGE_API.md](KNOWLEDGE_API.md) · 运维：[knowledge-ops-frontend.md](knowledge-ops-frontend.md)

### 3.1 环境部署（后端 / 运维）

| 配置项 | 用途 | 未配影响 |
|--------|------|----------|
| **`KB_LLM_CONFIG_SECRET`** | LLM api-key AES 落库 | PUT 平台 LLM 带新 key 失败；`encryptionReady=false` |
| `kb.sync.schedule-enabled` | 定时 wiki→DB sync | 仅手动 Sync |
| `kb.lint.schedule-enabled` | 定时扫描落库 | 体检工单需手动「扫描并落库」 |
| 告警 webhook | sync/lint 失败通知 | 运维无自动告警 |
| Gateway 大文件 | 上传 / Ingest | **勿经 21000**；直连 8090 或调高超时 |

本地：`moli-knowledge-server` 默认 **8090**；chunk 召回 `kb.search.chunk-enabled: true`（可选）。

### 3.2 Lint 分页（O8 · 已对接）

| API | 说明 |
|-----|------|
| `GET /kb/lint/issues?pageNum&pageSize` | 服务端分页；前端 `KbLintIssuesPanel` 已用 |
| `GET /kb/lint/issue-types` | 类型筛选下拉 |
| `PUT /kb/lint/issues/batch` | 批量状态变更 |

**后端无需改契约**；若旧环境仍返回全量数组，前端可客户端 slice 兼容。

### 3.3 浏览多选 facet（v3 · 已对接）

平行双 facet + 多选联动，契约见 [KNOWLEDGE_API.md §2.1.3](KNOWLEDGE_API.md#213-浏览管理页筛选-ui-规范体裁--分类--平行双-facet)：

| 场景 | 请求 |
|------|------|
| 体裁多选 | `GET /kb/document/search?kbTypes=a,b` |
| 分类多选 | `GET /kb/document/search?categoryIds=1,2` |
| 联动计数 | 体裁已选 → `/kb/index?kbTypes=…`；分类已选 → `/kb/index/types?categoryIds=…` |

**后端**：`KbDocumentFilterSupport` 已支持 `List` 绑定；facet 仍只返回 `count>0` 分组。  
**可选后续**：Meilisearch `facetDistribution`（规划文档 `知识库-meilisearch接入规划`），接口形状不变。

### 3.4 知识库 smoke（节选）

| # | 场景 | API |
|---|------|-----|
| K1 | 空间/分类树 | `GET /kb/space/mine`、`GET /kb/index` |
| K2 | 问答 | `POST /kb/ask`（chunk 召回可开） |
| K3 | LLM 平台 | `GET /kb/platform/llm` · `encryptionReady` |
| K4 | Sync | Web「Wiki 同步」或 `sync_to_db.py` |
| K5 | 体检分页 | `GET /kb/lint/issues?pageNum=1&pageSize=20` |

---

## 4. SSO · 按系统隔离菜单

> **权威设计**：[sso-menu-system-isolation.md](../design/sso-menu-system-isolation.md)（数据模型 · 过滤算法 · SQL `30_sso_menu_system_id.sql` · 发布阶段）  
> 现状：`GET /menu/getRouters` 按角色拉全量树，**未**按 Session `currentSystemId` / `sys_menu.system_id` 过滤 → 切换系统菜单串台。

| 阶段 | 负责 | 要点 |
|------|------|------|
| P0 | user-center + DBA | `system_id` 列 + `MenuService.resolveRoutersForCurrentSystem`；门户关闭不过滤 |
| P1 | DBA | 按段位 backfill（1/400/500/600/800/900 → `sys_system.id`） |
| P2 | meiling-ui | `enter`/`switch` 后重拉 `getRouters` 并清动态路由缓存 |

菜单段位：[frontend-routes-map.md](frontend-routes-map.md) · 门户分组：[portal-system-group.md](../design/portal-system-group.md)。

---

## 5. 建议处理顺序

| 顺序 | 负责 | 项 | 理由 |
|------|------|-----|------|
| **1** | 运维/DBA | 运营 SQL 28、三开关、`OPS_SECRET_KEY` | unblock 部署中心与拓扑浏览器走查 |
| **2** | **meiling-ui** | **S-VO + 部署中心对接 + 走查 W1–W10** | [operation-frontend-handoff.md](operation-frontend-handoff.md) §2–§5 · 后端 ✅ |
| **3** | 运维 | `KB_LLM_CONFIG_SECRET` + knowledge 定时任务 | 生产 LLM 与 sync/lint 闭环 |
| **4** | user-center | SSO `getRouters` + `sys_menu.system_id`（§4） | 多系统菜单隔离 |
| **5** | **meiling-ui** | batch/task、upload 轮询、task cancel（§2.4） | 部署中心产品化；后端 API 已就绪 |

---

## 6. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-13 | 初版：三模块总览 + 运营 VO/smoke/DBA + 知识库环境/facet + SSO 菜单隔离 |
| 2026-07-13 | 详情 `*Count` 后端 ✅；新增 [operation-frontend-handoff.md](operation-frontend-handoff.md) · §0 给前端 |
| 2026-07-13 | **`toVo()` 统一派生**；移除 `fillRelationCounts`；文档同步 handoff §0、§7.1 |
| 2026-07-13 | SSO 菜单隔离权威设计 [sso-menu-system-isolation.md](../design/sso-menu-system-isolation.md) · §4 精简 |
