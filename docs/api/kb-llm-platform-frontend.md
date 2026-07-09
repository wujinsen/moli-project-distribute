# 知识库 LLM · 平台设置页 · 前端对接说明（meiling-ui · T19d）

> **读者**：meiling-ui 前端。后端 **T19a–c / T19e ✅** 已就绪；本文是 **T19d 联调权威说明**。  
> **运维排期总览**：[knowledge-ops-frontend.md](knowledge-ops-frontend.md) · **产品 PRD**：[knowledge-ops-prd.md](../product/knowledge-ops-prd.md)  
> **HTTP 契约总表**：[KNOWLEDGE_API.md](KNOWLEDGE_API.md) §3.5  
> **产品设计**：[kb-llm-platform-settings.md](../design/kb-llm-platform-settings.md)  
> **菜单 SQL**：[`docs/sql/12_kb_platform_llm_menu.sql`](../sql/12_kb_platform_llm_menu.sql)（部署前 DBA 执行）

---

## 1. 做什么

平台管理员在 **系统管理 → 知识库 LLM** 配置全站共用的 LLM（Ask / Ingest / Wiki 治理 / Express 一键入库）。

| 项 | 说明 |
|----|------|
| 配置域 | **平台级**，不是知识库空间设置 |
| API Key | **加密存 MySQL**，GET 永不回明文 |
| 保存后 | 后端热刷新 `KbLlmRuntime`，**无需重启** knowledge-server |
| 与 Ask 页关系 | Ask/Ingest 仍用 `GET /kb/ask/llm-config` 探测 `available`；本页是**管理入口** |

---

## 2. 路由与菜单

| 项 | 值 |
|----|-----|
| 菜单名 | 知识库 LLM |
| 父菜单 | 系统管理（`parent_id=1`） |
| 路由 path | `kb-llm`（完整 URL 如 `/system/kb-llm`） |
| 组件 path | `system/kb-llm/index` |
| route name | `KbPlatformLlmSettings` |
| 权限码 | `kb:platform:llm` |
| 图标 | `cpu`（SQL 已写，可按 UI 规范调整） |

**网关前缀**（与现有知识库 API 一致）：

```
{VITE_API_BASE_URL}/KnowledgeServer/kb/platform/llm-config
```

`meiling-ui` 内建议沿用 `src/api/knowledge.ts` 的 `KB_BASE = '/KnowledgeServer/kb'`。

---

## 3. 接口一览

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | GET | `/kb/platform/llm-config` | 进入页加载表单（含脱敏 key、来源标签） |
| 2 | PUT | `/kb/platform/llm-config` | 保存；保存后 Runtime 立即生效 |
| 3 | POST | `/kb/platform/llm-config/test` | 连通性测试（可带未保存的表单值） |

**权限**：平台超管 **或** 拥有 `kb:platform:llm`。无权限时 HTTP 200 但 `code≠200`，`msg` 为 `无权管理平台 LLM 配置`。

**探测 API（只读，给 Ask/Ingest 用，本页可不调用）**：

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kb/ask/llm-config` | 仅 `available` / `provider` / `model`，**无** baseUrl、**无** key |

保存成功后可选再调一次 `GET /kb/ask/llm-config` 验证 `available=true`。

---

## 4. TypeScript 类型（建议放入 `src/types/knowledge.ts`）

```typescript
/** GET /kb/platform/llm-config — 管理视图 */
export type KbPlatformLlmConfig = {
  enabled: boolean
  provider: string
  baseUrl: string
  apiKeyConfigured: boolean
  apiKeyMask?: string
  model: string
  temperature?: number
  timeoutSeconds?: number
  extraModels?: string[]
  /** 当前运行时是否可调用 LLM */
  available: boolean
  /** database | yaml_fallback */
  source: string
  /** DB 是否已持久化 api-key（与 yaml 兜底区分） */
  persistedInDatabase?: boolean
  updateTime?: string
}

/** PUT /kb/platform/llm-config */
export type KbPlatformLlmConfigSaveRequest = {
  enabled: boolean
  provider: string
  baseUrl: string
  /** 空字符串 = 不修改已有 key */
  apiKey?: string
  /** true = 清除 DB 中 key，运行时回退 yaml */
  clearApiKey?: boolean
  model: string
  temperature?: number
  timeoutSeconds?: number
  extraModels?: string[]
}

/** POST /kb/platform/llm-config/test — 字段均可选，缺省用已保存/生效配置 */
export type KbPlatformLlmConfigTestRequest = {
  message?: string
  enabled?: boolean
  provider?: string
  baseUrl?: string
  apiKey?: string
  model?: string
  temperature?: number
  timeoutSeconds?: number
  extraModels?: string[]
}

export type KbPlatformLlmConfigTestResult = {
  success: boolean
  latencyMs?: number
  model?: string
  replyPreview?: string
  error?: string
}
```

---

## 5. API 封装（建议 `src/api/knowledge.ts`）

```typescript
const KB_BASE = '/KnowledgeServer/kb'

export function getKbPlatformLlmConfigApi() {
  return request<KbPlatformLlmConfig>(`${KB_BASE}/platform/llm-config`, { method: 'GET' })
}

export function saveKbPlatformLlmConfigApi(body: KbPlatformLlmConfigSaveRequest) {
  return request<KbPlatformLlmConfig>(`${KB_BASE}/platform/llm-config`, {
    method: 'PUT',
    body: jsonEntityBody(body as Record<string, unknown>),
  })
}

export function testKbPlatformLlmConfigApi(body?: KbPlatformLlmConfigTestRequest) {
  return request<KbPlatformLlmConfigTestResult>(`${KB_BASE}/platform/llm-config/test`, {
    method: 'POST',
    body: jsonEntityBody((body ?? {}) as Record<string, unknown>),
  })
}
```

权限常量建议加入 `src/constants/permissions.ts`：

```typescript
KB_PLATFORM_LLM: 'kb:platform:llm',
```

按钮可用 `guardAction(PERM.KB_PLATFORM_LLM)` 或依赖菜单路由守卫（菜单 SQL 已绑 `perms`）。

---

## 6. 响应示例

### 6.1 GET

```json
{
  "code": 200,
  "data": {
    "enabled": true,
    "provider": "glm",
    "baseUrl": "https://open.bigmodel.cn/api/paas/v4",
    "apiKeyConfigured": true,
    "apiKeyMask": "****tdLzM0",
    "model": "glm-4-flash",
    "temperature": 0.3,
    "timeoutSeconds": 90,
    "extraModels": ["glm-4-flash", "glm-4-air"],
    "available": true,
    "source": "database",
    "persistedInDatabase": true,
    "updateTime": "2026-06-28 15:00:00"
  }
}
```

| 字段 | UI 用途 |
|------|---------|
| `available` | 顶部状态条：绿「LLM 可用」/ 红「未就绪」 |
| `source` | 标签：`database` →「数据库」；`yaml_fallback` →「yaml 兜底」 |
| `apiKeyMask` | api-key 输入框 placeholder，如 `已配置 ****tdLzM0` |
| `persistedInDatabase` | 区分 key 在 DB 还是仅 yaml；配合「清除 DB Key」按钮 |
| `apiKeyConfigured=false` | 提示需填写 api-key 并保存 |

### 6.2 PUT

请求示例（**首次保存 key**）：

```json
{
  "enabled": true,
  "provider": "glm",
  "baseUrl": "https://open.bigmodel.cn/api/paas/v4",
  "apiKey": "your-api-key-here",
  "model": "glm-4-flash",
  "temperature": 0.3,
  "timeoutSeconds": 90,
  "extraModels": ["glm-4-flash", "glm-4-air"]
}
```

**只改 model、不动 key**（`apiKey` 传 `""` 或省略）：

```json
{
  "enabled": true,
  "provider": "glm",
  "baseUrl": "https://open.bigmodel.cn/api/paas/v4",
  "apiKey": "",
  "model": "glm-4-air",
  "temperature": 0.3,
  "timeoutSeconds": 90
}
```

**清除 DB 中的 key**（回退 yaml；需二次确认）：

```json
{
  "enabled": true,
  "provider": "glm",
  "baseUrl": "https://open.bigmodel.cn/api/paas/v4",
  "apiKey": "",
  "clearApiKey": true,
  "model": "glm-4-flash"
}
```

### 6.3 POST test

保存前测试（带表单草稿）：

```json
{
  "message": "ping",
  "enabled": true,
  "provider": "glm",
  "baseUrl": "https://open.bigmodel.cn/api/paas/v4",
  "apiKey": "sk-test-only-if-new-key",
  "model": "glm-4-flash"
}
```

成功（HTTP 200，`code=200`）：

```json
{
  "code": 200,
  "data": {
    "success": true,
    "latencyMs": 842,
    "model": "glm-4-flash",
    "replyPreview": "pong",
    "error": null
  }
}
```

失败（仍为 HTTP 200，看 `data.success` 与 `data.error`）：

```json
{
  "code": 200,
  "data": {
    "success": false,
    "latencyMs": 1203,
    "model": "glm-4-flash",
    "replyPreview": null,
    "error": "401 Unauthorized"
  }
}
```

---

## 7. 页面结构建议

新建 `src/views/system/KbPlatformLlmSettingsView.vue`（或 `system/kb-llm/index.vue`，与菜单 `component` 一致）。

```
KbPlatformLlmSettingsView
├─ 状态条：available + source + updateTime
├─ 表单
│   ├─ enabled（Switch）
│   ├─ provider（Select：deepseek / qwen / glm / custom）
│   ├─ baseUrl（Input；选 preset 时自动填充，custom 可编辑）
│   ├─ apiKey（Password；见 §8）
│   ├─ model（Input / Select）
│   ├─ temperature（0～2，默认 0.3）
│   ├─ timeoutSeconds（5～300，默认 90）
│   └─ extraModels（Tags 多选，供治理/Ingest 模型下拉）
├─ 操作栏
│   ├─ 测试连接
│   ├─ 保存
│   └─ 清除 DB Key（danger，二次确认）
└─ 说明区：生效范围（Ask / Ingest Plan·草稿·Express / Wiki AI·治理）
```

**Provider 预设**（前端本地表，选后填充 baseUrl + 默认 model，用户仍可改）：

| provider | baseUrl | 默认 model |
|----------|---------|------------|
| `deepseek` | `https://api.deepseek.com/v1` | `deepseek-chat` |
| `qwen` | `https://dashscope.aliyuncs.com/compatible-mode/v1` | `qwen-plus` |
| `glm` | `https://open.bigmodel.cn/api/paas/v4` | `glm-4-flash` |
| `custom` | 不自动改 | 用户自填 |

参考现有系统页：`DictManageView.vue`（表单 + 保存 + toast）、`guardAction` 权限。

---

## 8. api-key 交互（重要）

| 规则 | 前端做法 |
|------|----------|
| GET 不回明文 | 输入框**不要**用 `apiKeyMask` 当 value |
| 未改 key | PUT 时 `apiKey: ""` 或不传 |
| 更换 key | 用户输入新 key → PUT 带非空 `apiKey` |
| 占位提示 | `apiKeyConfigured` 时 placeholder = `已配置 ${apiKeyMask}` |
| 首次配置 | 空库 + yaml 兜底时，`source=yaml_fallback` 且可能有 `available=true` 但 `persistedInDatabase=false` |

**保存 api-key 到 DB 的前置条件**（运维侧，前端需友好提示）：

服务端须配置 `KB_LLM_CONFIG_SECRET`（或 `kb.llm.config-secret`）。未配置时 PUT 带新 key 会失败：

```
未配置 kb.llm.config-secret（KB_LLM_CONFIG_SECRET），无法将 api-key 加密存入数据库
```

可在说明区链到设计文档 §7，或展示「请联系运维配置加密密钥」。

---

## 9. 测试连接 vs 保存

| 操作 | 行为 |
|------|------|
| **测试连接** | POST test；body 用**当前表单值**（含未保存的 apiKey）；**不写库** |
| **保存** | PUT；成功后用返回的 `data` 刷新表单（含新 `apiKeyMask`），清空 apiKey 输入框 |

推荐流程：改表单 → **先测试** → 成功后再 **保存**。

测试时若用户未填新 key 但 DB/yaml 已有 key，POST body 可省略 `apiKey`，后端用已生效 key。

---

## 10. 校验与错误

| 场景 | 后端 msg（示例） | 前端 |
|------|------------------|------|
| 无权限 | `无权管理平台 LLM 配置` | toast + 可选跳转 |
| baseUrl 非法 | `base-url 须以 http:// 或 https:// 开头` | 字段级错误 |
| 内网 SSRF | `base-url 不允许指向内网/元数据地址` | 字段级错误 |
| temperature | `temperature 须在 0.0～2.0 之间` | 表单校验 |
| timeout | `timeoutSeconds 须在 5～300 之间` | 表单校验 |
| 未执行 DDL | `平台 LLM 配置表不可用，请先执行 11_kb_platform_llm_config.sql` | 全页告警 |
| 缺加密密钥 | 见 §8 | Alert 说明区 |
| 测试失败 | `data.error` | 展示 error + latencyMs |

`enabled=true` 但无 key 时，`available=false`；测试返回 `success=false`, `error=LLM 未启用或未配置 api-key`。

---

## 11. 与现有组件关系

| 现有 | 变更 |
|------|------|
| `KbLlmToggle.vue` | **不改**；继续 `getKbLlmConfigApi()` 探测 Ask |
| `KnowledgeAskView` | 无需改；配置好后 `available` 自动变 true |
| Ingest / Wiki 治理 | 无需改；后端已读 `KbLlmRuntime` |

本页上线后，运维可逐步去掉 yaml 中的明文 `kb.llm.api-key`（可选）。

---

## 12. i18n 建议（`system.kbLlm.*`）

与 `knowledge.ask.llm` 区分（Ask 是「是否用 LLM 回答」；本页是「平台密钥配置」）。

| 键 | 中文示例 |
|----|----------|
| `title` | 知识库 LLM |
| `status.available` | LLM 已就绪 |
| `status.unavailable` | LLM 未配置或未启用 |
| `source.database` | 配置来源：数据库 |
| `source.yaml` | 配置来源：yaml 兜底 |
| `field.enabled` | 启用 LLM |
| `field.provider` | 提供方 |
| `field.baseUrl` | API 地址 |
| `field.apiKey` | API Key |
| `field.apiKey.placeholder` | 留空表示不修改；已配置 {mask} |
| `field.model` | 默认模型 |
| `field.extraModels` | 扩展模型（治理/Ingest） |
| `action.test` | 测试连接 |
| `action.save` | 保存 |
| `action.clearKey` | 清除数据库中的 Key |
| `action.clearKey.confirm` | 清除后将回退到服务器 yaml 中的 Key，确定？ |
| `test.success` | 连接成功（{ms} ms） |
| `test.fail` | 连接失败：{error} |
| `scope.hint` | 生效于：智能问答、Ingest、Wiki AI 改稿与治理 |

---

## 13. 本地联调步骤

1. MySQL 执行 `docs/sql/11_kb_platform_llm_config.sql`、`12_kb_platform_llm_menu.sql`
2. 设置环境变量（保存 key 到 DB 时需要）：
   ```powershell
   $bytes = New-Object byte[] 32
   [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
   $env:KB_LLM_CONFIG_SECRET = [Convert]::ToBase64String($bytes)
   ```
3. 启动 `moli-knowledge-server`（:8090）与网关
4. 用**系统管理员**或平台超管登录 meiling-ui
5. 菜单应出现 **系统管理 → 知识库 LLM**（若无，检查 `getRouters` 与角色 `sys_role_action`）
6. 保存 → `GET /kb/ask/llm-config` 返回 `available=true`

---

## 14. 验收清单（前端自测）

- [ ] 无 `kb:platform:llm` 用户看不到菜单或 PUT/GET 报无权限
- [ ] 进入页 GET 加载表单；`source` 标签正确
- [ ] 选 provider 预设自动填 baseUrl/model
- [ ] api-key 留空保存不丢已有 key（mask 不变）
- [ ] 填新 key 保存后 mask 更新；输入框清空
- [ ] **测试连接**：保存前用表单值可测通/测失败
- [ ] **清除 DB Key**：二次确认后 `persistedInDatabase=false`，若 yaml 有 key 则 `source=yaml_fallback`
- [ ] 保存后 Ask 页 LLM 开关变为可用（无需重启后端）
- [ ] i18n zh/en/ja 三套键齐全

---

## 15. 相关文件（meiling-ui 待增/改）

| 文件 | 动作 |
|------|------|
| `src/views/system/kb-llm/index.vue` 或 `KbPlatformLlmSettingsView.vue` | **新建** |
| `src/api/knowledge.ts` | 增 3 个 API 函数 |
| `src/types/knowledge.ts` | 增 §4 类型 |
| `src/constants/permissions.ts` | 增 `KB_PLATFORM_LLM` |
| `src/locales/**/system.json`（或等价） | 增 `system.kbLlm.*` |

路由由后端 **动态菜单** 加载（`component: system/kb-llm/index`），一般**不必**改 `router` 静态表；若本地 dev 需静态 fallback，参考其它 `system/*` 页。

---

## 16. 相关文档

- [KNOWLEDGE_API.md §3.5](KNOWLEDGE_API.md) — HTTP 契约
- [kb-llm-platform-settings.md](../design/kb-llm-platform-settings.md) — 架构与加密说明
- [KNOWLEDGE_SCHEMA.md §kb_platform_llm_config](../sql/KNOWLEDGE_SCHEMA.md) — 表结构
