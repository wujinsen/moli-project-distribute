# 服务器运维 · 运营管理 · 前端对接说明（meiling-ui）

> **读者**：meiling-ui 前端（菜单「运营管理」· 驾驶舱 ops 页）。  
> **更新**：2026-07-13 · **前端开工**：[operation-frontend-handoff.md](operation-frontend-handoff.md) · **后端通知**：[operation-backend-handoff.md](operation-backend-handoff.md)  
> **meiling-ui 副本**：[`meiling-ui/docs/api/operation-frontend.md`](../../meiling-ui/docs/api/operation-frontend.md) · handoff：[`meiling-ui/docs/api/operation-frontend-handoff.md`](../../meiling-ui/docs/api/operation-frontend-handoff.md)（与 monorepo 同步）  
> **技术规划**：[server-ops-module-roadmap.md](../design/server-ops-module-roadmap.md)  
> **HTTP 契约索引**：[user-center-api-map.md](user-center-api-map.md) §4  
> **端口矩阵权威**：[production-checklist.md](../ops/production-checklist.md) §2  
> **边界**：本文档是 **user-center `operation_*` 基础设施台账**；**不是**知识库内容管道运维（见 [knowledge-ops-frontend.md](knowledge-ops-frontend.md)）。

---

## 1. 开发优先级（给前端排期）

| 优先级 | 模块 | meiling-ui 路由（建议） | 后端 | 前端任务 ID |
|--------|------|-------------------------|------|-------------|
| **P0** | 凭据安全 | `operation/platform/index`、`operation/component/index` | ✅ SVR-1/2/3 | **S0** 密码掩码 + 明文 reveal |
| **P1** | 健康探测 | `operation/server/index`、`operation/component/index` | ✅ SVR-4 | **S1** 状态灯 + 行内探测 |
| **P1** | 服务器拓扑 | `operation/server/index` | ✅ SVR-5/6 | **S2** 拓扑弹窗 |
| **P2** | 端口矩阵校验 | `operation/project/index`、`operation/component/index` | ✅ SVR-7 | **S3** 端口校验弹窗 + 组件列 badge |
| **P2** | 部署进程状态 | `operation/project/index` | ✅ SVR-8 | **S4** 进程状态（只读） |
| **P2** | 驾驶舱 ops KPI | `CandlelightDragon/cockpit/index`（tab=ops） | ✅ SVR-9 | **S5** 合并 `/operation/stats` |
| **P2** | N:N 关联维护 | `operation/server/index` | ✅ SVR-11 | **S6** 拓扑弹窗内编辑关联 |
| **P2+** | 项目/组件多服务器 | `operation/project/index`、`operation/component/index` | ✅ SVR-22 | **S6-b** 列表行「关联服务器」多选弹窗 |
| **P4** | 全局拓扑图 | `operation/topology/index`（菜单 407） | ✅ SVR-25a/b | **S10** `OperationTopologyGraphView` |
| **P4** | 关联关系导航 | 三管理页 + 部署/任务/端口/平台 | ✅ SVR-26a/28a～f | **S11** `RelationDrawer` + chips + URL 过滤 |
| **P2** | 批量探活 / 部署同步 | 服务器页工具栏 | ✅ SVR-12 | **S7** 异步 probe-all + 轮询（见 §13） |
| **P3** | SSH 凭据 | `operation/server/index` | ✅ SVR-13 | **S8** SSH 配置弹窗 + 测试连接 |
| **P3** | 部署中心 | `operation/deploy/index` | ✅ SVR-14~20 | **S9** 远程启停 + 灵活上传 + 远程命令 + 任务轮询 |

**建议迭代顺序**：**S0 → S1/S2 → S3 → S4 → S5 → S6 → S8 → S9**

**网关 / 联调前缀**：

| 环境 | 请求前缀 |
|------|----------|
| 本地 dev（meiling-ui vite proxy） | `/operation/*` → `user-center :8888` |
| 经网关（若统一入口） | `{gateway}/UserCenterServer/operation/*`（以 [gateway-routes.md](gateway-routes.md) 为准） |

**统一响应**：`MoliResult<T>` → `{ code: 200, data: T, msg?: string }`；分页 `data` 为 `PageRes<T>` → `{ list, total, pageNum, pageSize }`。

---

## 2. 菜单 ↔ 路由 ↔ 权限

父菜单 **运营管理**（`sys_menu.id = 400`）。

| 页面 | 菜单 path | 列表权限（C 菜单 perms） | 写操作额外权限 |
|------|-----------|--------------------------|----------------|
| 项目管理 | `operation/project/index` | `operation:project:list` | `add` / `edit` / `remove` + **list** |
| 服务器管理 | `operation/server/index` | `operation:server:list` | 同上 |
| 平台管理 | `operation/platform/index` | `operation:platform:list` | 同上 |
| 组件管理 | `operation/component/index` | `operation:component:list` | 同上 |
| 部署中心 | `operation/deploy/index` | `operation:server:list` | `deploy:exec` / `file:upload` / `command:exec` |
| 任务历史 | `operation/task/index` | `operation:server:list` | 只读列表 + 日志抽屉（与部署中心同权） |
| **端口矩阵** | `operation/port-matrix/index` | `operation:port-matrix:list` | `add` / `edit` / `remove` + **list**（SVR-21） |
| **拓扑图** | `operation/topology/index` | `operation:server:list` | 只读全图（菜单 id **407** · SVR-25c） |

**跨域权限**（非菜单 perms，需角色 `sys_action` 绑定）：

| perm | 用途 |
|------|------|
| `operation:secret:view` | `GET .../secret` 查看平台/组件密码明文 |
| `operation:deploy:exec` | `POST /operation/deploy/{key}/{action}/task` 异步启停 |
| `operation:file:upload` | `POST /operation/file/upload` 文件 SFTP 发布 |
| `operation:command:exec` | `POST /operation/command/exec/task` 远程 shell；上传 `postAction=custom` |
| `operation:ssh:manage` | `PUT /operation/server/{id}/ssh` SSH 凭据与 `uploadAllowedRoots` |

迁移脚本（已有库需执行）：`docs/sql/17_operation_secret_view.sql`～`21_operation_ssh_deploy.sql`、**`22_operation_command_flex.sql`**、**`23_operation_schema_hardening.sql`**、**`24_operation_port_matrix.sql`**（SVR-21）、**`26_operation_server_role.sql`**（SVR-23）、**`27_operation_server_tags.sql`**（SVR-24）、**`29_operation_project_component.sql`**（SVR-26a）。完整顺序见 [`sql-migration-order.md`](../ops/sql-migration-order.md)。

---

## 3. 枚举与字段约定

### 3.1 环境 `environment`

| 值 | 含义 |
|----|------|
| `1` | dev |
| `2` | test |
| `3` | pre |
| `4` | pro |

列表筛选：query 传 `environment`；空或不传表示全部。

### 3.2 服务器角色 `serverRole`（SVR-23）

与 `environment` **正交**：环境表示 dev/test/pre/pro；角色表示主机职能分类。

| 值 | 含义 | i18n key |
|----|------|----------|
| `app` | 应用 / 业务服务主机 | `operation.serverRole.app` |
| `db` | 数据库 | `operation.serverRole.db` |
| `cache` | 缓存（Redis 等） | `operation.serverRole.cache` |
| `mq` | 消息队列 | `operation.serverRole.mq` |
| `gateway` | 网关 / 负载均衡 / Nginx | `operation.serverRole.gateway` |
| `bastion` | 堡垒机 / 跳板 | `operation.serverRole.bastion` |
| `middleware` | 中间件（Nacos、ZK、Kafka 等） | `operation.serverRole.middleware` |
| `other` | 其它 | `operation.serverRole.other` |

- 列表筛选：`GET /operation/server/list?serverRole=app`；空或不传表示全部。
- 新建默认 `app`（后端未传时回填）；`POST`/`PUT` 请求体可带 `serverRole`；非法值校验失败。

### 3.3 服务器标签 `tags`（SVR-24）

自由标签，**一台服务器可多个**；存库为 JSON 数组字符串（如 `["pro","gz"]`）。

| 规则 | 说明 |
|------|------|
| 格式 | 每项 1–32 字符，小写字母/数字/`-`/`:`/`_`，首字符须为字母或数字 |
| 数量 | 最多 20 个；保存时去重、转小写 |
| 筛选 | `GET /operation/server/list?tag=pro` 精确匹配单项（`JSON_CONTAINS`） |
| 联想 | `GET /operation/server/tag-options` 返回全库已用标签去重排序 |

请求/响应 VO 字段为 `tags: string[]`（非 JSON 字符串）。

### 3.4 健康状态 `status`（服务器 / 组件）

| 值 | 含义 | UI 建议 |
|----|------|---------|
| `0` | 未知（未探测） | 灰色 |
| `1` | 可达（TCP 连通） | 绿色 |
| `2` | 不可达 | 红色 |
| `3` | 跳过（缺 IP 或端口） | 琥珀色 |

探测接口会**写库**并返回更新后的 VO：`POST /operation/server/{id}/check`、`POST /operation/component/{id}/check`。

### 3.5 端口校验 `portMatchStatus`（组件列表 VO）

| 值 | 含义 | UI 建议 |
|----|------|---------|
| `0` | 未映射（名称不在端口矩阵） | 灰色 |
| `1` | 与矩阵一致 | 绿色 |
| `2` | 与矩阵不符 | 红色，可展示 `expectedPort` |
| `3` | 跳过（台账端口为空或 `-`） | 琥珀色 |

矩阵服务名与期望端口见 [§7](#7-端口矩阵对照表)。

### 3.6 密码字段（平台 / 组件）

| 场景 | 行为 |
|------|------|
| `GET list` / `GET {id}` | **永不**返回 `password` 明文；返回 `passwordConfigured`、`passwordMask` |
| `POST` / `PUT` | 请求体可带 `password`；**留空表示保留原密码**（仅更新场景） |
| `GET {id}/secret` | 需 `operation:secret:view`；返回 `{ password: string }`；记审计日志 |

---

## 4. P0 · 凭据安全（S0）

### 4.1 涉及页面

- `PlatformManageView` — 平台账号
- `ComponentManageView` — 组件账号

### 4.2 API

```http
GET  /operation/platform/list
GET  /operation/platform/{id}
GET  /operation/platform/{id}/secret     # operation:secret:view
POST /operation/platform
PUT  /operation/platform

GET  /operation/component/list
GET  /operation/component/{id}
GET  /operation/component/{id}/secret      # operation:secret:view
POST /operation/component
PUT  /operation/component
```

### 4.3 UI 要点

| ID | 要求 |
|----|------|
| **S0-1** | 列表/表单展示 `passwordMask` 或「未配置」；不展示历史明文 |
| **S0-2** | 编辑时独立密码输入框；hint：留空保存 = 保留原密码 |
| **S0-3** | 有 `operation:secret:view` 时显示「查看明文」；调用 reveal API；仅当前会话展示 |
| **S0-4** | 无 reveal 权限时隐藏按钮，勿静默调 secret 接口 |

### 4.4 TypeScript（建议 `src/types/operation.ts`）

```typescript
export type OperationPlatform = {
  id?: number | string
  platformName?: string
  url?: string
  account?: string
  password?: string              // 仅 POST/PUT 提交
  passwordConfigured?: boolean
  passwordMask?: string | null
  environment?: 1 | 2 | 3 | 4
  remark?: string
  createTime?: string | number
}

export type OperationSecretReveal = { password?: string }
```

---

## 5. P1 · 健康探测与拓扑（S1 / S2）

### 5.1 服务器列表 + 探测（S1）

```http
GET  /operation/server/list?pageNum=1&pageSize=10&serverName=&ip=&environment=&serverRole=&tag=
GET  /operation/server/tag-options
POST /operation/server/{id}/check
```

**`OperationServerVo` 增量字段**：

```typescript
export type OperationServer = {
  // ...serverName, ip, innerIp, port, environment, serverRole, tags, remark
  serverRole?: 'app' | 'db' | 'cache' | 'mq' | 'gateway' | 'bastion' | 'middleware' | 'other' | null
  tags?: string[]
  status?: 0 | 1 | 2 | 3 | null
  lastCheckTime?: string | number | null
}
```

| ID | UI |
|----|-----|
| **S1-0** | 列表/表单增加「角色」列与筛选（`ServerRoleBadge` / `ServerRoleSelect`） |
| **S1-0b** | 列表/表单「标签」列与筛选；`GET /tag-options` 联想（`ServerTagsInput` / `ServerTagsBadges`） |
| **S1-1** | 列表增加「健康状态」列（灯 + 可选 `lastCheckTime`） |
| **S1-2** | 行操作「探测」→ `check` API；成功后就地更新该行 `status` / `lastCheckTime` |

### 5.2 组件列表 + 探测（S1）

```http
GET  /operation/component/list?...
POST /operation/component/{id}/check
```

`OperationComponentVo` 同样含 `status`、`lastCheckTime`（另含 §3.3 端口字段）。

### 5.3 服务器关联详情（SVR-28b · 替代原 SVR-5 单机拓扑）

> **已删除** `GET /operation/server/{id}/topology`（SVR-5）。服务器行「拓扑/关联」弹窗统一改调：

```http
GET /operation/relations/server/{id}
```

**响应 `OperationRelationsVo`**（见 §5.7.2）：`entity` + `projects[]` + `components[]` + `recentTasks[]`（含 `deployRunning` / `portMatchStatus`）。

| ID | UI |
|----|-----|
| **S2-1** | 服务器行「关联」→ `RelationDrawer` 调 `GET /operation/relations/server/{id}` |
| **S2-2** | 组件行带 `portMatchStatus`、健康 `status`；项目行带 `deployRunning` |
| **S2-3** | （可选升级 **S6**）弹窗内「编辑关联」→ 调 `GET/PUT .../links` |

**种子数据 smoke**：`GET /operation/relations/server/201` 应含项目 401/406、组件 306/307/304（以库内 seed 为准）。

### 5.4 N:N 关联维护（S6）

```http
GET /operation/server/{id}/links
PUT /operation/server/{id}/links
```

**请求/响应 `OperationServerLinksVo`**：

```typescript
export type OperationServerLinks = {
  serverId?: number | string
  projectIds?: (number | string)[]
  componentIds?: (number | string)[]
}
```

| ID | UI |
|----|-----|
| **S6-1** | 拓扑弹窗增加「编辑关联」；多选项目/组件 ID（或名称搜索后勾选） |
| **S6-2** | `PUT` 为**全量替换**；保存成功后刷新拓扑 |
| **S6-3** | 无效 ID 后端返回业务错误（项目/组件不存在） |

### 5.5 项目/组件 · 多选服务器（S6-b · SVR-22 · ✅）

设计文档：[operation-server-links.md](../design/operation-server-links.md)  
meiling-ui 实现：`OperationServerLinksModal` · `OperationLinkedServersCell` · [`meiling-ui/docs/api/operation-frontend.md`](../../meiling-ui/docs/api/operation-frontend.md) §15

**后端联调（2026-07-13 ✅）**：`POST` create 接受 `serverIds`；`PUT .../links` 同步主表 `server_id` / `server_ip` / `innerIp`；详见 [`operation-server-links.md`](../design/operation-server-links.md) §2.3。

```http
GET /operation/project/{id}/links
PUT /operation/project/{id}/links
GET /operation/component/{id}/links
PUT /operation/component/{id}/links
```

**CRUD body 扩展**（`POST`/`PUT` 项目或组件）：

```typescript
export type OperationProjectSave = {
  serverId?: number          // 主服务器（部署/探活默认目标）
  serverIds?: number[]       // N:N 全量；含 serverId；保存时同步关联表
  projectName: string
  // ...
}
export type OperationComponentSave = {
  serverId?: number
  serverIds?: number[]
  componentName: string
  // ...
}
```

**列表/详情/探活 VO** 均在服务端 **`toVo()`** 内赋值 `serverIds` 与 `*Count`（2026-07-13）；**`serverCount === serverIds.length` 恒等**。N:N 为空时 `serverIds` 回退 `[serverId]`。前端见 [operation-frontend-handoff.md §0–§2](operation-frontend-handoff.md)。

| ID | UI | 状态 |
|----|-----|------|
| **S6-b-1** | 项目列表行：「关联服务器」→ 多选弹窗，`PUT .../links` | ✅ |
| **S6-b-2** | 组件列表行：同上 | ✅ |
| **S6-b-3** | 列表列：主 `名称 · IP` + `+N` | ✅ |
| **S6-b-4** | 部署启停仍用主 `serverId`（`row.serverId`） | ✅ |

**`POST` 响应（2026-07-13）**：`POST /operation/project` · `POST /operation/component` 的 `data` 为新建 **`id`（Long）**，不再返回 `boolean`；前端 create 后可直接用 `id` 补调 `PUT .../links`。

### 5.6 项目依赖组件（SVR-26a · ✅ 后端）

```http
GET /operation/project/{id}/component-links
PUT /operation/project/{id}/component-links
```

**请求/响应 `OperationProjectComponentLinksVo`**：

```typescript
export type OperationProjectComponentLinks = {
  projectId?: number | string
  componentIds?: (number | string)[]
}
```

| ID | UI | 状态 |
|----|-----|------|
| **S26-1** | 项目行「依赖组件」→ `OperationProjectComponentLinksModal`，`PUT .../component-links` 全量替换 | ✅ |
| **S26-2** | 拓扑图 `depends_on` 边（`GET /operation/topology` 已含） | ✅ |

### 5.7 关联关系导航（SVR-28 · 后端 ✅）

#### 5.7.1 列表关系计数 + 反向过滤（SVR-28a）

**凡经 `toVo()` 的响应**（`GET .../list` 每行、`GET /{id}`、行内 `check` 返回 VO）均含下列字段，**同一套派生逻辑**：

| 页 | 字段 |
|----|------|
| 项目 | `serverCount`、`componentCount`（`serverCount === serverIds.length`） |
| 组件 | `serverCount`、`projectCount`（`serverCount === serverIds.length`） |
| 服务器 | `projectCount`、`componentCount` |

列表 VO 字段（与详情相同）：

反向过滤 query（可与 `environment` 等叠加）：

| 列表 | 参数 | 语义 |
|------|------|------|
| `GET /operation/project/list` | `serverId` | 关联某服务器（含 N:N + 主 `server_id`） |
| | `componentId` | 依赖某组件 |
| `GET /operation/component/list` | `serverId` | 部署在某服务器（含 N:N） |
| | `projectId` | 被某项目依赖 |
| `GET /operation/server/list` | `projectId` | 承载某项目 |
| | `componentId` | 承载某组件 |

#### 5.7.2 统一关系 API（SVR-28b）

```http
GET /operation/relations/{entityType}/{id}
```

- `entityType`：`server` | `project` | `component`
- 权限：`operation:project:list`（与项目列表相同）
- 响应 `OperationRelationsVo`：`entity` + `servers[]`（含 `primary`）+ `projects[]` + `components[]` + `recentTasks[]`（最多 5 条）

```typescript
export type OperationRelations = {
  entityType: 'server' | 'project' | 'component'
  entity: { entityType: string; id: number; name: string; environment?: number }
  servers: Array<{
    id: number; serverName: string; ip?: string; innerIp?: string
    environment?: number; serverRole?: string; tags?: string[]
    status?: number; primary?: boolean
  }>
  projects: Array<{
    id: number; projectName: string; port?: string; environment?: number
    deployRunning?: boolean; portMatchStatus?: number
  }>
  components: Array<{
    id: number; componentName: string; port?: string; version?: string
    environment?: number; status?: number; portMatchStatus?: number
  }>
  recentTasks: Array<{
    id: number; taskType?: string; action?: string; status?: string; createTime?: string
  }>
}
```

| ID | UI | 状态 |
|----|-----|------|
| **S11-1** | `RelationDrawer` 调统一 API，三管理页 `OperationRelationChips` | ✅ |
| **S11-2** | 行内「定位」跳列表带 `serverId`/`projectId`/`componentId` 过滤 | ✅ |
| **S11-3** | 部署中心/任务历史/端口审计/平台实体名可点 → 同一抽屉 | ✅ |

### 5.8 全局拓扑图（SVR-25a · ✅ 后端）

```http
GET /operation/topology
```

- 权限：`operation:server:list`
- 响应 `OperationTopologyGraphVo`：节点 id 前缀 `s-` / `p-` / `c-`；边 `type` = `deploys` | `depends_on`

```typescript
export type OperationTopologyGraph = {
  servers: Array<{ id: string; serverId: number; serverName: string; ip?: string; innerIp?: string
    environment?: number; serverRole?: string; tags?: string[]; status?: number }>
  projects: Array<{ id: string; projectId: number; projectName: string; port?: string
    environment?: number; deployRunning?: boolean; portMatchStatus?: number }>
  components: Array<{ id: string; componentId: number; componentName: string; port?: string
    version?: string; environment?: number; status?: number; portMatchStatus?: number }>
  links: Array<{ source: string; target: string; type: 'deploys' | 'depends_on' }>
}
```

| ID | UI | 状态 |
|----|-----|------|
| **S10-1** | `OperationTopologyGraphView`（ECharts force/circular） | ✅ |
| **S10-2** | 筛选：环境/角色/标签/关键字；点服务器 → `ServerDetailModal` | ✅ |
| **S10-3** | 深链 `?focus=s-{id}|p-{id}|c-{id}`；导出 PNG | ✅ |

---

## 6. P2 · 端口校验 / 部署状态 / 驾驶舱（S3 / S4 / S5）

### 6.1 端口矩阵校验（S3）

```http
GET /operation/audit/port-matrix
```

权限：`operation:project:list`（与项目管理列表相同）。

**响应 `OperationPortAuditVo`**：

```typescript
export type OperationPortAudit = {
  total: number
  matched: number
  mismatched: number
  unmapped: number
  skipped: number
  matrix: { key: string; expectedPort: string; source?: string }[]
  items: OperationPortAuditItem[]
}

export type OperationPortAuditItem = {
  id: number
  recordType: 'project' | 'component'
  name: string
  actualPort?: string | null
  expectedPort?: string | null
  matrixKey?: string | null
  portMatchStatus: 0 | 1 | 2 | 3
  message?: string
  environment?: number
}
```

| ID | UI |
|----|-----|
| **S3-1** | 项目/组件页工具栏「端口校验」→ 弹窗：顶部汇总 + 矩阵表 + 明细表 |
| **S3-2** | 组件列表可直接用行内 `portMatchStatus` / `expectedPort`（`GET list` 已 enrichment） |
| **S3-3** | 项目列表同样返回 `portMatchStatus` / `expectedPort`（`OperationProjectVo`）；`portMatchStatus === 2` 高亮 |

### 6.2 部署进程状态（S4，只读默认）

```http
GET  /operation/deploy/{serviceKey}/status?serverId=
POST /operation/deploy/{serviceKey}/{action}?serverId=   # 变更动作见下
```

**`serviceKey`**：优先从 `GET /operation/deploy/presets` 的 **`serviceKeys[]`** 读取（与 `ops.deploy.services` 一致）；勿在前端硬编码常量数组。

**项目名 → serviceKey**：台账列表可本地映射作「是否可点进程状态」判断；**实际 HTTP 请求以 presets / 后端 Registry 为准**。

| 台账 `projectName`（别名示例） | serviceKey |
|--------------------------------|------------|
| `moli-server`、`user-center-server` 等 | `user-center` |
| `moli-gateway` | `gateway` |
| `knowledge-server` 等 | `knowledge` |

**⚠ 生产必传 `serverId`**：`ops.deploy.allow-local` 默认 **false**。未传 `serverId` 调 status/execute 将返回 **10109**。项目页手动查 status 时必须带 **`row.serverId`**；无 serverId 时禁用按钮并提示先绑定服务器。

**响应 `OperationDeployStatusVo`**：

```typescript
export type OperationDeployStatus = {
  serviceKey: string
  action: string
  available?: boolean      // 脚本是否可调用
  running?: boolean        // 解析输出推断是否运行中
  output?: string          // 脚本 stdout
  message?: string
}
```

| ID | UI |
|----|-----|
| **S4-1** | 可映射的项目行显示「进程状态」；**优先**读列表 VO 的 `deployRunning` / `lastDeployCheckTime`（定时 SSH 同步）；手动刷新须 `GET .../status?serverId={row.serverId}` |
| **S4-2** | `available === false` 时展示 `message`（Windows 开发机 / 脚本不存在等） |
| **S4-3** | **默认不做** start/stop/restart 按钮；若做需 `operation:deploy:exec` + 二次确认 |

**变更动作（可选，高风险）**：

```http
POST /operation/deploy/user-center/restart
POST /operation/deploy/gateway/logs?arg=200
```

- `status` / `logs`：只需 `operation:server:list`（若走 POST 路由则仍受 Controller 注解约束；推荐只用 GET status）
- `start` / `stop` / `restart`：需 `operation:deploy:exec`；且服务端 `ops.deploy.enabled=true`

### 6.3 驾驶舱 ops KPI（S5）

```http
GET /operation/stats
```

权限：`operation:project:list`。

**响应 `OperationStatsVo`**：

```typescript
export type OperationStats = {
  projects: number
  servers: number
  platforms: number
  components: number
  portMismatches: number
  healthDown: number
  envBreakdown: { env: 1 | 2 | 3 | 4; count: number }[]
}
```

| ID | UI |
|----|-----|
| **S5-1** | 驾驶舱 `tab=ops` 时请求 `/operation/stats`，用真实计数覆盖 Mock KPI |
| **S5-2** | 建议映射：`projects/servers/components/platforms` → 对应 KPI 卡片数值 |
| **S5-3** | `portMismatches + healthDown` 可合并展示为「告警」类 KPI（文案产品自定） |
| **S5-4** | `envBreakdown` 可驱动环境分布图（可选） |

---

## 7. 端口矩阵对照表

> **SVR-21 后**：运行时权威改为 DB + 运维台「端口矩阵」菜单（`operation/port-matrix/index`）。下表为**初始种子**；改端口请在管理页维护，无需发版。设计：[`operation-port-matrix-config.md`](../design/operation-port-matrix-config.md)。

| matrixKey | 期望端口 | 匹配别名（名称归一化后） |
|-----------|----------|--------------------------|
| gateway | 21000 | gateway, moli-gateway |
| user-center | 8888 | user-center, moli-user-center, user-center-server, **moli-server** |
| order | 8087 | order, moli-order |
| knowledge | 8090 | knowledge, moli-knowledge, knowledge-server |
| bi | 1128 | bi, moli-ai |
| nacos | 8848 | nacos |
| mysql | 3306 | mysql |
| redis | 6379 | redis |

**预期 demo**：种子中 `moli-server` 端口 `9080` → 审计为 **不符**（期望 8888）；`MySQL:3306` → **一致**。

---

## 8. 建议 API 模块（`src/api/operation.ts`）

```typescript
import { request } from '@/api/http'
import type { PageRes } from '@/types/page'
import type {
  OperationComponent,
  OperationPlatform,
  OperationProject,
  OperationServer,
  OperationRelations,
  OperationPortAudit,
  OperationStats,
  OperationDeployStatus,
  OperationSecretReveal,
} from '@/types/operation'

const OP = '/operation'

// CRUD：project / server / platform / component — 标准 list|get|add|update|remove

export const checkServerApi = (id: number | string) =>
  request<OperationServer>(`${OP}/server/${id}/check`, { method: 'POST' })

export const checkComponentApi = (id: number | string) =>
  request<OperationComponent>(`${OP}/component/${id}/check`, { method: 'POST' })

export const getServerRelationsApi = (id: number | string) =>
  request<OperationRelations>(`${OP}/relations/server/${id}`, { method: 'GET' })

export const revealPlatformSecretApi = (id: number | string) =>
  request<OperationSecretReveal>(`${OP}/platform/${id}/secret`, { method: 'GET' })

export const revealComponentSecretApi = (id: number | string) =>
  request<OperationSecretReveal>(`${OP}/component/${id}/secret`, { method: 'GET' })

export const getPortAuditApi = () =>
  request<OperationPortAudit>(`${OP}/audit/port-matrix`, { method: 'GET' })

export const getOperationStatsApi = () =>
  request<OperationStats>(`${OP}/stats`, { method: 'GET' })

export const getDeployStatusApi = (serviceKey: string, serverId?: number | string | null) => {
  const qs = serverId != null && serverId !== '' ? `?serverId=${serverId}` : ''
  return request<OperationDeployStatus>(`${OP}/deploy/${serviceKey}/status${qs}`, { method: 'GET' })
}

export const getServerLinksApi = (id: number | string) =>
  request<OperationServerLinks>(`${OP}/server/${id}/links`, { method: 'GET' })

export const saveServerLinksApi = (id: number | string, body: OperationServerLinks) =>
  request<boolean>(`${OP}/server/${id}/links`, { method: 'PUT', data: body })

/** Phase R3 Breaking：返回 taskId，须轮询 GET /operation/task/{id} */
export const probeAllHealthApi = () =>
  request<number>(`${OP}/health/probe-all`, { method: 'POST' })
```

`OperationProject` 列表 VO 补充字段：

```typescript
export type OperationProject = {
  // ...原有字段
  serverId?: number | string | null   // 远程 status / 启停必填（生产）
  expectedPort?: string | null
  portMatchStatus?: 0 | 1 | 2 | 3 | null
  deployRunning?: boolean | null
  lastDeployCheckTime?: string | number | null
}

/** @deprecated 同步探活统计已移除；轮询 health_probe 任务 */
export type OperationHealthProbeResult = {
  serversProbed: number
  componentsProbed: number
  deployStatusesSynced: number
  serverIdsSynced: number
}

export type OperationDeployServiceOption = { key: string; label: string }

export type OperationDeployPresets = {
  pathPresets?: string[]
  actionPresets?: OperationDeployPresetItem[]
  serviceKeys?: OperationDeployServiceOption[]
}
```

---

## 9. 联调 checklist

| 步骤 | 检查 |
|------|------|
| 1 | user-center 启动（`:8888`），`ops.secret.key` / `OPS_SECRET_KEY` 已配置 |
| 2 | DB 已执行 `17_*`～`29_*`（含 `operation_project_component`、拓扑菜单 407）；顺序见 [`sql-migration-order.md`](../ops/sql-migration-order.md) |
| 3 | **部署中心三开关**（默认均为 false）：`ops.upload.enabled`、`ops.command.enabled`、`ops.deploy.enabled` — 本地 dev 建议在 `application-dev.yml` 置 `true` |
| 4 | meiling-ui **vite proxy** `/operation` → `8888`；**大文件上传勿经 Gateway**（易浏览器 `Failed to fetch`） |
| 5 | 登录角色含 `operation:*:list`；部署中心另需 `file:upload`、`command:exec`、`deploy:exec`、`ssh:manage` |
| 6 | 平台/组件：列表只见 mask；reveal 需 `operation:secret:view` |
| 7 | 服务器/组件：探测后 `status` / `lastCheckTime` 更新 |
| 8 | `GET /operation/relations/server/{id}` 含关联项目与组件（旧 topology API 已删除） |
| 9 | 项目关联服务器：弹窗保存 1 台后 `serverCount`=1，抽屉 servers 仅 1 条且 `primary` 正确 |
| 10 | 端口校验：`mismatched >= 1`（种子 moli-server 9080 vs 矩阵 8888） |
| 11 | 驾驶舱 ops：`/operation/stats` 计数与库内台账一致 |
| 12 | 部署中心：SSH 已配置且测试连接成功 → 启停/上传返回 `taskId` → 任务抽屉轮询 |
| 13 | 文件上传：`postAction=custom` 需 command 开关 + 权限；zip 解压推荐预设 `unzipToDist` |
| 14 | probe-all：POST 得 taskId → 轮询至 finished |
| 15 | 生产：`allow-local=false` 时所有 deploy API 必须带 `serverId` |

---

## 13. Phase R 改造 · 前端必改（2026-07-11）

> **2026-07-13 后端追加**：[`operation-backend-handoff.md`](operation-backend-handoff.md)（create 返回 id · links 已验收 · order/ai 脚本）

### 13.0 Breaking · `POST` create 返回 id（2026-07-13）

| API | 旧 | 新 |
|-----|----|----|
| `POST /operation/project` | `data: true` | **`data: number`**（新建 id） |
| `POST /operation/component` | `data: true` | **`data: number`**（新建 id） |

`addProjectApi` / `addComponentApi` 返回类型改为 `number`；create 带 `serverIds` 时一般无需再 `PUT links`。

### 13.1 Breaking 清单（按优先级）

| P | 任务 | 文件（meiling-ui） | 说明 |
|---|------|-------------------|------|
| **P0** | probe-all 异步 | `src/api/operation.ts`、`ServerManageView.vue`、`cockpit/index.vue` | `probeAllHealthApi()` 返回 **`number` taskId**；复用 `useOperationTaskPoll` 打开抽屉轮询；成功后 `listServerApi` / `listProjectApi` 刷新 |
| **P0** | 项目 status 带 serverId | `ProjectManageView.vue` | `getDeployStatusApi(key, row.serverId)`；`serverId` 空则禁用「进程状态」 |
| **P0** | 部署中心 serviceKeys | `DeployCenterView.vue`、`types/operation.ts` | 删除硬编码 `MOLI_DEPLOY_SERVICES`；`loadPresets()` 读 `data.serviceKeys` 渲染启停卡片 |
| **P1** | 删服务器 409 | `ServerManageView.vue` | 捕获 **10107**，Toast「有进行中的任务」+ 可选跳转任务列表 |
| **P1** | 错误码文案 | `types/operation.ts` 或 i18n | 10101–10109 友好提示（尤其 **10109** 本机部署禁用） |
| **P1** | 项目页跳转启停 | `ProjectManageView.vue` | `createDeployTaskApi(key, action, row.serverId, row.id)` 传 **projectId** |
| **P2** | orphan 标记 | 列表页 | `serverId == null` 行显示「未绑定服务器」 |

### 13.2 probe-all 推荐流程（S7）

```typescript
async function probeAll() {
  probingAll.value = true
  try {
    const result = await probeAllHealthApi()
    if (result.code !== 200 || result.data == null) throw new Error(result.msg)
    await openTask(result.data)  // useOperationTaskPoll：1.5s 轮询
    // task.status === 'success' 后：
    await reloadServerList()
    await reloadProjectList()
    showToast('success', t('operation.health.probeAllTaskDone'))
  } finally {
    probingAll.value = false
  }
}
```

i18n 建议：`probeAllOk` 改为「探活任务已提交」/ 完成后再 Toast 统计（统计在任务 `message` 或 log 中，非 HTTP 同步 body）。

### 13.3 部署中心 presets 示例

```typescript
const serviceKeys = ref<OperationDeployServiceOption[]>([])

async function loadPresets() {
  const result = await getDeployPresetsApi(selectedServerId.value || undefined)
  serviceKeys.value = result.data?.serviceKeys ?? []
  // refreshAllStatus: serviceKeys.value.map(k => getDeployStatusApi(k.key, serverId))
}
```

### 13.4 生产环境约定

- 前端**始终**传 `serverId`（部署中心、项目 status、启停 task）
- 仅本地 dev 后端设 `OPS_DEPLOY_ALLOW_LOCAL=true`；前端不应依赖无 serverId 的本机回退
- `deployRunning` 列以列表 VO 为准（后端 `status-sync-mode=ssh` 定时同步）；手动刷新须 SSH 到对应机器

### 13.5 后端业务码速查

见 [operation-deploy-api.md §9](operation-deploy-api.md#9-业务错误码phase-r2)。

---

## 10. 验收总表

| ID | 场景 | 通过标准 |
|----|------|----------|
| S0 | 密码 | 列表无明文；reveal 受权限控制；空密码更新保留原值 |
| S1 | 探测 | 行内探测更新状态灯；失败 Toast |
| S2 | 拓扑 | 弹窗展示 projects + components；空列表友好提示 |
| S3 | 端口 | 弹窗汇总与明细正确；组件列 badge 与 audit 一致 |
| S4 | 部署 | status 只读可查；不可用时 message 可读 |
| S5 | 驾驶舱 | ops KPI 使用真实 stats，非纯 Mock |
| S6 | 关联 | links GET/PUT 可维护拓扑；保存后拓扑刷新 | ✅ |
| S7 | 批量探活 | probe-all 返回 taskId → 轮询 → 刷新列表 | ✅ |
| S8 | SSH | 配置私钥后 `sshConfigured=true`；测试连接返回 whoami | ✅ |
| S9 | 部署中心 | 选服务器 → 启停三件套返回 taskId；轮询进度/日志；上传 jar/zip | ✅ |
| S10 | 拓扑图 | `GET /operation/topology` 节点/边正确；ECharts 页可筛选聚焦 | ✅ |
| S11 | 关系导航 | 列表 chips 计数；RelationDrawer 三向关联 + 定位过滤 | ✅ |
| S26 | 项目依赖组件 | `component-links` GET/PUT；拓扑 `depends_on` 边 | ✅ |

---

## 11. P3 · 部署中心 / SSH（S8 / S9 / SVR-18~20）

> **HTTP 请求/响应、路径白名单、Shell 守卫、配置项权威说明** → **[operation-deploy-api.md](operation-deploy-api.md)**

### 11.1 菜单与权限

| 菜单 | component | perms（菜单） | 动作权限 |
|------|-----------|---------------|----------|
| 部署中心 | `operation/deploy/index` | `operation:server:list` | `operation:deploy:exec`、`operation:file:upload`、**`operation:command:exec`** |
| SSH 配置（服务器行内） | — | — | `operation:ssh:manage` |

迁移：`docs/sql/21_operation_ssh_deploy.sql`、**`22_operation_command_flex.sql`**（灵活路径/远程命令）

### 11.2 API 索引（meiling-ui 封装见 `src/api/operation.ts`）

完整字段与示例见 **[operation-deploy-api.md](operation-deploy-api.md)**。下表为路由速查：

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `saveServerSshApi` | PUT | `/operation/server/{id}/ssh` | 私钥/用户/端口/`uploadAllowedRoots`；只写不读 |
| `testServerSshApi` | POST | `/operation/server/{id}/ssh/test` | 测试 SSH |
| `getDeployStatusApi` | GET | `/operation/deploy/{key}/status?serverId=` | 远程/本机进程状态 |
| `createDeployTaskApi` | POST | `/operation/deploy/{key}/{action}/task?serverId=&projectId=` | 异步启停，返回 `taskId` |
| `getTaskApi` | GET | `/operation/task/{id}?logOffset=` | 轮询进度 + 增量日志 |
| `listTaskGroupsApi` | GET | `/operation/task/groups` | 任务历史按项目分组（DC-4）；分页在组维度 |
| `getDeployPresetsApi` | GET | `/operation/deploy/presets?serverId=` | 常用路径 + 快捷后置动作（替代前端硬编码） |
| `createCommandTaskApi` | POST JSON | `/operation/command/exec/task` | 远程 shell；body `{ serverId, command, workDir? }` → `taskId` |
| `uploadFileApi` | POST multipart | `/operation/file/upload` | `file, serverId, targetPath, postAction, postCommand?` |

**serviceKey**：来自 `GET /operation/deploy/presets` → **`serviceKeys[]`**（勿硬编码）

**postAction**：`none` · `nginxReload` · `unzipToDist` · `restartService:{key}` · **`custom`**（需 `postCommand` + `operation:command:exec`）

**目标路径**：手输绝对路径；白名单三层 OR — `ops.upload.allowed-paths`、`ops.upload.allow-any-under`（默认 `/opt/`、`/home/ubuntu/`）、服务器 `upload_allowed_roots`

**生产**：所有 deploy/file/command API **必须**带 `serverId`（`allow-local` 默认 false）。

#### 11.2.1 任务历史分组（DC-4）

```typescript
export type OperationTaskProjectGroup = {
  projectId: number | null      // null → 前端显示「未关联项目」
  projectName: string | null
  taskCount: number
  runningCount: number            // pending + running
  failedCount: number
  successCount: number
  latestCreateTime?: string
  tasks: OperationTask[]          // createTime 降序，条数受 tasksPerGroup 限制
}

// OperationTask 字段与 GET /operation/task/list 行一致
export type OperationTask = {
  id: number
  taskType?: string
  serverId?: number
  projectId?: number
  serviceKey?: string
  action?: string
  targetName?: string
  status?: string
  progress?: number
  message?: string
  createTime?: string
  finishTime?: string
  finished?: boolean
}
```

Query：`pageNum` · `pageSize` · `tasksPerGroup`（默认 20）· `taskType` · `projectId` · `serverId` · `status`

### 11.3 前端页面

| 文件 | 说明 |
|------|------|
| `views/operation/DeployCenterView.vue` | 选服务器 · 三件套启停 · **手输路径上传** · **远程命令** |
| `views/operation/TaskHistoryView.vue` | 任务历史 · **DC-4 分组视图**（`listTaskGroupsApi`）· 平铺 `list` 保留 |
| `components/operation/ServerSshModal.vue` | SSH 凭据 + **upload_allowed_roots** |
| `components/operation/DeployTaskDrawer.vue` | 任务进度条 + 日志终端 |
| `composables/useOperationTaskPoll.ts` | 1.5s 轮询 `getTaskApi` |

### 11.4 服务端配置（生产必开）

```yaml
ops:
  deploy:
    enabled: true
    allow-local: false          # 生产必须 false；仅 dev Linux 可 true
    status-sync-mode: ssh       # deploy_running 与 SSH status 一致
  upload:
    enabled: true
  command:
    enabled: true   # 远程命令 + 上传 custom 后置
  secret:
    key: ${OPS_SECRET_KEY}   # SSH/凭据 AES
```

CVM 上 `ubuntu` 用户需能 `sudo nginx -s reload`（见 `deploy/腾讯云上线流程.md` §14）。

手测用例：[`docs/test/operation-deploy-center-acceptance.md`](../../test/operation-deploy-center-acceptance.md) · 端口矩阵：[`operation-port-matrix-acceptance.md`](../../test/operation-port-matrix-acceptance.md) · 拓扑/关联：[`operation-relations-topology-acceptance.md`](../../test/operation-relations-topology-acceptance.md)。

自动化（拓扑/关联后端）：`mvn -pl moli-user-center-server -Dtest=Operation*Relation*,Operation*Topology*,OperationProjectComponentLink* test`

---

## 14. 端口矩阵管理页（SVR-21 · 设计稿）

> **后端契约**：[operation-port-matrix-api.md](operation-port-matrix-api.md) · **方案**：[operation-port-matrix-config.md](../design/operation-port-matrix-config.md)

| 项 | 值 |
|----|-----|
| 菜单 id | 406（父 400） |
| 路由 | `operation/port-matrix/index` |
| 列表权限 | `operation:port-matrix:list` |
| 写权限 | `add` / `edit` / `remove` + **list** |

### 14.1 页面能力

| 功能 | API |
|------|-----|
| 分页列表 | `GET /operation/port-matrix/list` |
| 新增/编辑弹窗 | `POST` / `PUT /operation/port-matrix` |
| 删除 | `DELETE /operation/port-matrix/{ids}` |
| 别名编辑 | 请求体 `aliases: string[]` 全量替换；UI 用 Tag 输入 |

保存成功后**无需重启** user-center；可立即调 `GET /operation/audit/port-matrix` 验证 `portMatchStatus` 变化。

### 14.2 TypeScript 类型（建议）

```typescript
export interface OperationPortMatrix {
  id: number
  matrixKey: string
  displayName?: string
  expectedPort: string
  aliases: string[]
  sortOrder?: number
  enabled: boolean
  source?: string
  remark?: string
}

export interface PortMatrixSaveRequest {
  id?: number
  matrixKey: string
  displayName?: string
  expectedPort: string
  aliases?: string[]
  sortOrder?: number
  enabled?: boolean
  source?: string
  remark?: string
}
```

### 14.3 与 S3 审计弹窗联动

项目/组件管理页的端口审计弹窗（§6.1）增加入口「管理端口矩阵」→ 路由跳转本页。审计 API 权限仍为 `operation:project:list`，与矩阵 CRUD 权限分离。

---

## 15. 关系计数 VO（S-VO · 2026-07-13 · `toVo()` 统一派生）

> **前端必读**：[operation-frontend-handoff.md §0](operation-frontend-handoff.md#0-给前端一句话2026-07-13) · [§2 S-VO](operation-frontend-handoff.md#2-本轮前端任务s-vo--关系计数) · [§3 部署/任务](operation-frontend-handoff.md#3-部署中心与异步任务2026-07-13-新增)

### 15.1 后端行为（前端可依赖）

| 实现 | 说明 |
|------|------|
| `Operation*ServiceImpl.toVo()` | 组装 `serverIds` 后 **`serverCount = serverIds.size()`**；`componentCount` / `projectCount` 同源 `resolve*Ids` |
| 已删除 | `fillRelationCounts()` 第二套批量回填（避免 list/detail 分叉） |
| 覆盖 API | `GET .../list`、`GET /{id}`、`POST .../check` 等凡 `map(toVo)` 的入口 |

### 15.2 字段表

| API | 字段 |
|-----|------|
| `GET /operation/project/{id}` · list 行 | `serverIds`、`serverCount`、`componentCount` |
| `GET /operation/component/{id}` · list 行 | `serverIds`、`serverCount`、`projectCount` |
| `GET /operation/server/{id}` · list 行 | `projectCount`、`componentCount` |

### 15.3 前端任务

| ID | 说明 |
|----|------|
| **S-VO-1** | `types/operation.ts` — `*Count` 与 list 同型 |
| **S-VO-2** | 详情/chips 用 `row.serverCount`，**勿** links 水合计数 |
| **S-VO-3** | `enrichRowsWithLinks` 仅关联弹窗 |
| **S-VO-4** | Chips：`row.serverCount`（兼容：`?? row.serverIds?.length`） |
| **S-VO-5** | `PUT links` 后 `listXxxApi()` 刷新 |

验收：LC-1b · W1–W2b · [operation-relations-topology-acceptance.md](../test/operation-relations-topology-acceptance.md)

---

## 16. 浏览器走查与未做可选项

### 16.1 走查（运营 · 建议每轮发版前）

| ID | 引用 | 说明 |
|----|------|------|
| §10 验收总表 | 本文 §10 | S0–S11、S26 回归 |
| handoff §5 | [operation-backend-handoff.md](operation-backend-handoff.md) | 6 条 smoke |
| S-VO W1–W10 | [operation-w1-w10-walkthrough.md](../test/operation-w1-w10-walkthrough.md) | S-VO + 上传 + batch + cancel |
| 部署中心 | [operation-deploy-center-acceptance.md](../test/operation-deploy-center-acceptance.md) | SSH/启停/上传/命令 |
| 拓扑/关联 | [operation-relations-topology-acceptance.md](../test/operation-relations-topology-acceptance.md) §5 | UI 点验 |

### 16.2 前后端对齐状态（2026-07-13）

| 项 | 后端 | 前端（meiling-ui） |
|----|------|-------------------|
| `POST /operation/server` 返回 id | ✅ `b4ac176a` | ✅ W7 |
| 批量滚动重启 | ✅ batch/task | ✅ W9 |
| 批量 links | ✅ 可选 | 可选 |
| 任务取消 | ✅ cancel | ✅ W10 |
| S-VO `*Count` | ✅ `toVo()` | ✅ W1–W6 |
| **联合走查** | 参与 | 🟡 [走查稿](../test/operation-w1-w10-walkthrough.md) |
| SSO 菜单按系统 | **✅ 已交付** | [sso-menu-frontend-handoff.md](sso-menu-frontend-handoff.md) · [走查](../test/sso-menu-frontend-walkthrough.md) |

---

## 12. 相关

- **前端开工手册**：[operation-frontend-handoff.md](operation-frontend-handoff.md)
- **部署中心 HTTP 契约（后端）**：[operation-deploy-api.md](operation-deploy-api.md)
- **端口矩阵 HTTP 契约（SVR-21）**：[operation-port-matrix-api.md](operation-port-matrix-api.md)
- 后端路线图：[server-ops-module-roadmap.md](../design/server-ops-module-roadmap.md)
- API 全量列表：[user-center-api-map.md](user-center-api-map.md) §4
- 部署脚本：`deploy/linux/moli-service.sh`（S4 服务端调用）
- 知识库运维（另一条线）：[knowledge-ops-frontend.md](knowledge-ops-frontend.md)
