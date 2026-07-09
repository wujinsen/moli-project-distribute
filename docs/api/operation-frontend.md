# 服务器运维 · 运营管理 · 前端对接说明（meiling-ui）

> **读者**：meiling-ui 前端（菜单「运营管理」· 驾驶舱 ops 页）。  
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
| **P2** | N:N 关联维护 | `operation/server/index` | ✅ SVR-10 | **S6** 拓扑弹窗内编辑关联 |
| **P2** | 批量探活 / 部署同步 | 驾驶舱或服务器页 | ✅ SVR-11 | **S7** 手动触发 `probe-all`（可选） |

**建议迭代顺序**：**S0 → S1/S2 → S3 → S4 → S5 → S6**

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

**跨域权限**（非菜单 perms，需角色 `sys_action` 绑定）：

| perm | 用途 |
|------|------|
| `operation:secret:view` | `GET .../secret` 查看平台/组件密码明文 |
| `operation:deploy:exec` | `POST /operation/deploy/{key}/{action}` 执行 start/stop/restart |

迁移脚本（已有库需执行）：`docs/sql/17_operation_secret_view.sql`、`18_operation_health_columns.sql`、`19_operation_deploy_exec.sql`、`20_operation_project_deploy_columns.sql`。

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

### 3.2 健康状态 `status`（服务器 / 组件）

| 值 | 含义 | UI 建议 |
|----|------|---------|
| `0` | 未知（未探测） | 灰色 |
| `1` | 可达（TCP 连通） | 绿色 |
| `2` | 不可达 | 红色 |
| `3` | 跳过（缺 IP 或端口） | 琥珀色 |

探测接口会**写库**并返回更新后的 VO：`POST /operation/server/{id}/check`、`POST /operation/component/{id}/check`。

### 3.3 端口校验 `portMatchStatus`（组件列表 VO）

| 值 | 含义 | UI 建议 |
|----|------|---------|
| `0` | 未映射（名称不在端口矩阵） | 灰色 |
| `1` | 与矩阵一致 | 绿色 |
| `2` | 与矩阵不符 | 红色，可展示 `expectedPort` |
| `3` | 跳过（台账端口为空或 `-`） | 琥珀色 |

矩阵服务名与期望端口见 [§7](#7-端口矩阵对照表)。

### 3.4 密码字段（平台 / 组件）

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
GET  /operation/server/list?pageNum=1&pageSize=10&serverName=&ip=&environment=
POST /operation/server/{id}/check
```

**`OperationServerVo` 增量字段**：

```typescript
export type OperationServer = {
  // ...serverName, ip, innerIp, port, environment, remark
  status?: 0 | 1 | 2 | 3 | null
  lastCheckTime?: string | number | null
}
```

| ID | UI |
|----|-----|
| **S1-1** | 列表增加「健康状态」列（灯 + 可选 `lastCheckTime`） |
| **S1-2** | 行操作「探测」→ `check` API；成功后就地更新该行 `status` / `lastCheckTime` |

### 5.2 组件列表 + 探测（S1）

```http
GET  /operation/component/list?...
POST /operation/component/{id}/check
```

`OperationComponentVo` 同样含 `status`、`lastCheckTime`（另含 §3.3 端口字段）。

### 5.3 服务器拓扑（S2）

```http
GET /operation/server/{id}/topology
```

**响应 `OperationServerTopologyVo`**：

```typescript
export type OperationServerTopology = {
  server?: OperationServer
  projects?: OperationTopologyProject[]
  components?: OperationTopologyComponent[]
}

export type OperationTopologyProject = {
  id?: number | string
  projectName?: string
  serverIp?: string
  port?: string
  deployPath?: string
  url?: string
  environment?: number
}

export type OperationTopologyComponent = {
  id?: number | string
  componentName?: string
  serverIp?: string
  port?: string
  version?: string
  status?: 0 | 1 | 2 | 3 | null
}
```

| ID | UI |
|----|-----|
| **S2-1** | 服务器行操作「拓扑」→ 弹窗展示 `server` 摘要 + 关联 `projects` / `components` 列表 |
| **S2-2** | 组件行可带 `HealthStatusBadge`（与列表一致） |
| **S2-3** | （可选升级 **S6**）弹窗内「编辑关联」→ 调 `GET/PUT .../links` |

**种子数据 smoke**：`GET /operation/server/201/topology` 应含项目 401/406、组件 306/307/304（以库内 seed 为准）。

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
GET  /operation/deploy/{serviceKey}/status
POST /operation/deploy/{serviceKey}/{action}   # 变更动作见下
```

**`serviceKey` 白名单**：`user-center` | `gateway` | `knowledge`

**项目名 → serviceKey 映射（前端本地）**：

| 台账 `projectName`（不区分大小写） | serviceKey |
|-----------------------------------|------------|
| `user-center`、`moli-user-center`、`user-center-server`、`moli-server` | `user-center` |
| `gateway`、`moli-gateway` | `gateway` |
| `knowledge`、`moli-knowledge`、`knowledge-server` | `knowledge` |

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
| **S4-1** | 可映射的项目行显示「进程状态」；优先读列表 VO 的 `deployRunning` / `lastDeployCheckTime`（定时同步）；也可手动调 `GET .../status` |
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

权威来源：`docs/ops/production-checklist.md` §2 + 后端 `OperationPortMatrix`。

| matrixKey | 期望端口 | 匹配别名（名称归一化后） |
|-----------|----------|--------------------------|
| gateway | 21000 | gateway, moli-gateway |
| user-center | 8888 | user-center, moli-user-center, user-center-server, **moli-server** |
| order | 8087 | order, moli-order |
| knowledge | 8090 | knowledge, moli-knowledge, knowledge-server |
| bi | 1128 | bi, moli-bi |
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
  OperationServerTopology,
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

export const getServerTopologyApi = (id: number | string) =>
  request<OperationServerTopology>(`${OP}/server/${id}/topology`, { method: 'GET' })

export const revealPlatformSecretApi = (id: number | string) =>
  request<OperationSecretReveal>(`${OP}/platform/${id}/secret`, { method: 'GET' })

export const revealComponentSecretApi = (id: number | string) =>
  request<OperationSecretReveal>(`${OP}/component/${id}/secret`, { method: 'GET' })

export const getPortAuditApi = () =>
  request<OperationPortAudit>(`${OP}/audit/port-matrix`, { method: 'GET' })

export const getOperationStatsApi = () =>
  request<OperationStats>(`${OP}/stats`, { method: 'GET' })

export const getDeployStatusApi = (serviceKey: string) =>
  request<OperationDeployStatus>(`${OP}/deploy/${serviceKey}/status`, { method: 'GET' })

export const getServerLinksApi = (id: number | string) =>
  request<OperationServerLinks>(`${OP}/server/${id}/links`, { method: 'GET' })

export const saveServerLinksApi = (id: number | string, body: OperationServerLinks) =>
  request<boolean>(`${OP}/server/${id}/links`, { method: 'PUT', data: body })

export const probeAllHealthApi = () =>
  request<OperationHealthProbeResult>(`${OP}/health/probe-all`, { method: 'POST' })
```

`OperationProject` 列表 VO 补充字段：

```typescript
export type OperationProject = {
  // ...原有字段
  expectedPort?: string | null
  portMatchStatus?: 0 | 1 | 2 | 3 | null
  deployRunning?: boolean | null
  lastDeployCheckTime?: string | number | null
}

export type OperationHealthProbeResult = {
  serversProbed: number
  componentsProbed: number
  deployStatusesSynced: number
  serverIdsSynced: number
}
```

---

## 9. 联调 checklist

| 步骤 | 检查 |
|------|------|
| 1 | user-center 启动（`:8888`），`OPS_SECRET_KEY` 已配置（P0 加密） |
| 2 | DB 已执行 `17_*`～`20_*`；需要部署按钮权限时执行 `19_*` |
| 3 | meiling-ui proxy `/operation` → `8888`；登录角色含 `operation:*:list` |
| 4 | 平台/组件：列表只见 mask；reveal 需 `operation:secret:view` |
| 5 | 服务器/组件：探测后 `status` / `lastCheckTime` 更新 |
| 6 | 服务器 id=201：拓扑含关联项目与组件 |
| 7 | 端口校验：`mismatched >= 1`（种子 moli-server 9080） |
| 8 | 驾驶舱 ops：`/operation/stats` 计数与库内台账一致 |

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
| S6 | 关联 | links GET/PUT 可维护拓扑；保存后拓扑刷新 |

---

## 11. 相关

- 后端路线图：[server-ops-module-roadmap.md](../design/server-ops-module-roadmap.md)
- API 全量列表：[user-center-api-map.md](user-center-api-map.md) §4
- 部署脚本：`deploy/linux/moli-service.sh`（S4 服务端调用）
- 知识库运维（另一条线）：[knowledge-ops-frontend.md](knowledge-ops-frontend.md)
