# Ingest 工作台 · 前端对接说明（meiling-ui）

> **读者**：meiling-ui 前端。后端 **T15a–e + T18 + T19 + T20a–e/c/d ✅**；**T20f 三 Tab UI 🔵 待 meiling-ui**。  
> **总览**：[knowledge-workbench-frontend.md](knowledge-workbench-frontend.md)  
> **HTTP 契约**：[KNOWLEDGE_API.md](KNOWLEDGE_API.md) §9 · **T20 Tab1/Tab3** → **[kb-import-entry-frontend.md](kb-import-entry-frontend.md)**  
> **产品方案**：[Ingest工作台产品方案.md](../../moli-knowledge/kb/wiki-moli/develop/Ingest工作台产品方案.md) · **T20 PRD**：[knowledge-import-entry-prd.md](../product/knowledge-import-entry-prd.md)

---

## 0. T20 · 三 Tab 导入入口（新增）

Ingest 页扩展为 **投喂 Raw · 选源入库 · 成品导入** 三 Tab；Tab2 本文 §1–§13 **不变**。

| Tab | 文档 | API |
|-----|------|-----|
| Tab1 投喂 Raw | [kb-import-entry-frontend.md §5–§6](kb-import-entry-frontend.md#5-tab1--raw-投喂) | `POST /kb/ingest/raw-upload`（§9.10） |
| Tab2 选源入库 | 本文 §1–§13 | §9 既有接口 |
| Tab3 成品导入 | [kb-import-entry-frontend.md §6](kb-import-entry-frontend.md#6-tab3--wiki-成品导入) | `POST /kb/wiki/page/import`（§8.8） |

完整 UI 结构、TypeScript 类型、Mock 策略、验收 **T20-F1–F7** → **[kb-import-entry-frontend.md](kb-import-entry-frontend.md)**。

---

## 1. 页面与路由

| 项 | 值 |
|----|-----|
| 列表/详情 | `knowledge/ingest/index`（query **`id`** 打开批次详情；**`jobId`** 为 DeepResearch 等外链别名，进入后规范化为 `id`） |
| 组件 | `KnowledgeIngestView`（或现有等价命名） |
| 网关 | `{VITE_API_BASE_URL}/KnowledgeServer/kb/ingest/*` |

**两种模式**（同一页，URL 或 Tab 切换）：

| 模式 | 用户路径 | 关键 API |
|------|----------|----------|
| **Expert** | ①选 raw → ②Plan → ③生成 → ④审阅 → ⑤lint → ⑥commit | 逐步调用 §9 各接口 |
| **Express** | 勾选 raw → **一键预览** → diff 扫一眼 → **确认入库** | `express` / `publish` |

**深链 Query**（列表 ↔ Expert 详情）：

| 参数 | 说明 |
|------|------|
| `id` | 批次 ID（canonical） |
| `jobId` | 与 `id` 等价；主题调研 writeback 成功后 `router.push({ query: { jobId } })` |
| `express=1` | Express 模式（与批次 ID 组合） |

`KnowledgeIngestWorkbenchView`：`jobId` computed 读取 `id` 优先，否则 `jobId`；仅带 `jobId` 时 `router.replace` 为 `?id=`。

---

## 2. 推荐界面结构

> **T20**：外层增加 `el-tabs`；Tab2 结构如下；Tab1/Tab3 见 [kb-import-entry-frontend.md §3](kb-import-entry-frontend.md#3-页面结构)。

```
KnowledgeIngestView.vue
├─ KbSpaceSelector
├─ IngestRawPanel              # raw-tree + raw-coverage 筛选
├─ IngestJobList               # 批次分页
├─ IngestJobDetail             # id 打开
│   ├─ ExpressBanner           # ?express=1 或模式切换
│   ├─ ExpressOptions          # ☑ skeleton Plan  ☑ 模板入库（不调 LLM）
│   ├─ PlanEditor              # create/enrich/skip 表
│   ├─ DraftDiffList           # baseline ↔ draft / patch
│   ├─ LintPanel               # commit 前预检
│   └─ CommitResult + KbWorkflowNextSteps
└─ TemplatePicker（可选）      # T15e 批次模板
```

---

## 3. 状态机

`IngestJobVo.status`：

```
created → planned → reviewing → committed
```

| 状态 | UI 提示 | 可用操作 |
|------|---------|----------|
| `created` | 已选 raw，未规划 | 生成 Plan、Express 预览 |
| `planned` | Plan 已保存 | 编辑 Plan、生成草稿 |
| `reviewing` | 草稿已生成 | diff 审阅、approve、lint、commit |
| `committed` | 已落盘 | 只读；可跳转 Wiki 治理 |

---

## 4. Express 与模板模式（T18 + T19 · 前端增量）

### 4.1 Express 一键预览

```http
POST /kb/ingest/jobs/express?useLlmPlan=false&useLlmGenerate=true
Content-Type: application/json

{
  "spaceId": 900000000000000001,
  "batchNo": "batch-20260628-001",
  "topic": "FE 题库样例",
  "rawPaths": ["moli-user-center/README.md"]
}
```

| Query | 默认 | UI 控件建议 |
|-------|------|-------------|
| `useLlmPlan` | `false` | ☑「Express Plan（skeleton，1 raw → 1 create）」 |
| `useLlmGenerate` | `true` | ☑「模板入库（不调 LLM）」→ 传 `false` |

响应 `IngestExpressStartVo`：含 `job` + `prepare`（内含 `generate.templateMode`）。

已有批次时：

```http
POST /kb/ingest/jobs/{id}/prepare?useLlmPlan=false&useLlmGenerate=false
```

### 4.2 确认入库

```http
POST /kb/ingest/jobs/{id}/publish?sync=true&approveAll=true
```

`approveAll=true`：全部草稿置 `approved` → lint → commit → 可选 Sync。

### 4.3 模板模式行为

- `useLlmGenerate=false`：`generate` / `prepare` / `express` / `draft/regenerate` 均不调 LLM  
- 响应 `IngestGenerateResultVo.templateMode = true`  
- 草稿 = frontmatter + raw 正文（去掉 raw 自身 frontmatter）  
- **LLM 不可用时的降级**：自动勾选模板模式 + Toast「已降级为模板入库」

- **LLM 不可用时的降级（B3）**：请求仍传 `useLlmGenerate=true` 时，后端自动改模板模式；响应 `llmFallback=true` + `llmFallbackReason` → Toast

### 4.4 Expert 生成 / 重生成

**请求参数**（与 Express 模板勾选同一语义）：

```typescript
// templateMode = UI「模板入库（不调 LLM）」勾选
useLlmGenerate: !templateMode
```

```http
POST /kb/ingest/jobs/{id}/generate?resume=false&useLlmGenerate=true
POST /kb/ingest/jobs/{id}/draft/regenerate?slug=guides/foo&useLlmGenerate=true
```

**响应处理**：

| 字段 | 含义 | UI |
|------|------|-----|
| `templateMode` | 实际是否模板生成 | 标签「模板模式」 |
| `llmFallback` | 是否 LLM 不可用自动降级 | `true` → Toast `llmFallbackReason` |
| `llmFallbackReason` | Toast 文案 | 例：「LLM 未配置…已自动改用模板模式」 |

`regenerate` 的上述字段在 **`IngestDraftVo`** 上（非 `IngestGenerateResultVo`）。

### 4.4a 异步生成 + SSE 进度（T15f） ✅

> **后端** ✅ · **前端** ✅ `KnowledgeIngestWorkbenchView.vue` + `kbIngest.ts`  
> 契约：`KNOWLEDGE_API.md` §9.4.1a · 设计：`docs/design/kb-ingest-generate-sse-design.md`

Expert 详情页「生成草稿 / 续跑生成」默认走异步链路；旧版 knowledge-server 无 `generate/start` 时自动回退同步 `POST .../generate`。

| 步骤 | API | 前端 |
|------|-----|------|
| 1 | `POST /kb/ingest/jobs/{id}/generate/start?resume=&useLlmGenerate=` | `startKbIngestGenerateApi` → `taskId` |
| 2 | `GET /kb/ingest/jobs/{id}/generate/stream?taskId=` | `subscribeKbIngestGenerateStream`（fetch + Authorization） |
| 3 | SSE `complete` 后 | `GET /drafts` 刷新列表 |

**SSE 事件**：`started` · `page_start`（展示当前 slug）· `page_done` · `progress` · `complete` · `error`

**UI**：生成中展示 `knowledge.ingest.generateLive`（`正在生成：{slug}`）；`progress` 更新 `lastGenerateStats`。

**回退**：stream 404/405 或「异步 generate 未启用」→ 同步 `generateKbIngestDraftsApi`（与 T15b 行为一致）。

```typescript
// src/api/knowledge/kbIngest.ts
startKbIngestGenerateApi(jobId, { resume, useLlmGenerate })
subscribeKbIngestGenerateStream(jobId, taskId, handlers, signal?)
```

---

## 5. nextSteps（T19 · 前端增量）

`commit` / `publish` 成功后展示 CTA 条：

```typescript
export interface KbWorkflowHintVo {
  key: string
  label: string
  description?: string
  routePath: string
  routeQuery?: Record<string, string>
}

export interface IngestCommitResultVo {
  jobId: number
  created: number
  updated: number
  files: string[]
  syncTriggered: boolean
  syncResult?: SyncTriggerVo
  nextSteps: KbWorkflowHintVo[]
}
```

| key | 跳转 |
|-----|------|
| `wiki_govern_lint` | `knowledge/wiki-govern/index?spaceId=` |
| `kb_health_scan` | `knowledge/lint/index?spaceId=` |

**组件建议**：`KbWorkflowNextSteps.vue` — 接收 `nextSteps`，渲染为按钮组。

---

## 6. raw 覆盖门禁（commit 错误处理）

commit / publish 时若 raw 已被**其它** wiki 页 `sources` 引用 → HTTP 业务错误（`code=10012`）。

前端处理：

- Toast 展示后端 `msg`  
- **结构化详情**：读 `data.errorKind === 'INGEST_RAW_ALREADY_COVERED'` 时渲染 `data.conflicts[]`（`path`、`coverage`、`matchKind`、`wikiSlugs`），可一键跳转 enrich 目标 slug  
- 引导用户改 Plan 为 `enrich` 同一 slug，或跳过该 raw  
- raw 列表：`GET /kb/ingest/raw-coverage?filter=open` 预筛未 ingest 项（**仍可选已 covered**，仅 commit 时拦截）

**错误体示例**见 [knowledge-workbench-frontend §8.1 B4](knowledge-workbench-frontend.md#b4--i4--commit-冲突错误体)。

---

## 7. 接口速查（24 个）

完整字段见 [KNOWLEDGE_API.md §9](KNOWLEDGE_API.md#9-ingest-工作台t15)。

| 步骤 | 方法 | 路径 |
|------|------|------|
| raw 树 | GET | `/kb/ingest/raw-tree` |
| raw 覆盖 | GET | `/kb/ingest/raw-coverage` |
| 创建批次 | POST | `/kb/ingest/jobs` |
| 批次列表 | GET | `/kb/ingest/jobs` |
| 批次详情 | GET | `/kb/ingest/jobs/{id}` |
| 删批次 | DELETE | `/kb/ingest/jobs/{id}` |
| LLM Plan | POST | `/kb/ingest/jobs/{id}/plan` |
| 改 Plan | PUT | `/kb/ingest/jobs/{id}/plan` |
| 生成草稿 | POST | `/kb/ingest/jobs/{id}/generate?resume=&useLlmGenerate=` |
| 草稿列表 | GET | `/kb/ingest/jobs/{id}/drafts` |
| 单页草稿 | GET | `/kb/ingest/jobs/{id}/draft?slug=` |
| 改草稿 | PUT | `/kb/ingest/jobs/{id}/draft?slug=` |
| 重生成 | POST | `/kb/ingest/jobs/{id}/draft/regenerate?slug=&useLlmGenerate=` |
| 审批 | PUT | `/kb/ingest/jobs/{id}/draft/approval?slug=&approval=` |
| lint | POST | `/kb/ingest/jobs/{id}/lint` |
| commit | POST | `/kb/ingest/jobs/{id}/commit?sync=` |
| Express 新建 | POST | `/kb/ingest/jobs/express?useLlmPlan=&useLlmGenerate=` |
| Express 准备 | POST | `/kb/ingest/jobs/{id}/prepare?useLlmPlan=&useLlmGenerate=` |
| Express 发布 | POST | `/kb/ingest/jobs/{id}/publish?sync=&approveAll=` |
| 模板 CRUD | GET/POST/DELETE | `/kb/ingest/templates*`、`from-template`、`save-as-template` |

---

## 8. TypeScript 类型（核心）

```typescript
export interface IngestDraftVo {
  id: number
  jobId: number
  slug: string
  displaySlug: string
  kbType: string
  action: 'create' | 'enrich'
  baseline?: string
  patch?: string
  draft: string
  approval: 'draft' | 'approved' | 'rejected'
  categoryId?: number
  dirSlug?: string
  categoryName?: string
}

export interface IngestGenerateResultVo {
  jobId: number
  generated: number
  skipped: number
  failed: number
  templateMode: boolean
  llmFallback?: boolean
  llmFallbackReason?: string
  drafts: IngestDraftVo[]
}

export interface IngestPublishResultVo {
  jobId: number
  commit: IngestCommitResultVo
  lint?: IngestLintVo
  nextSteps: KbWorkflowHintVo[]
}
```

`slug` 含 `/` 时一律走 **query 参数**，不要拼 path。

---

## 9. API 封装增量（`kbIngest.ts`）

```typescript
export function expressStartApi(
  data: IngestJobCreateRequest,
  opts?: { useLlmPlan?: boolean; useLlmGenerate?: boolean }
) {
  const { useLlmPlan = false, useLlmGenerate = true } = opts ?? {}
  return request.post<MoliResult<IngestExpressStartVo>>(
    `/kb/ingest/jobs/express?useLlmPlan=${useLlmPlan}&useLlmGenerate=${useLlmGenerate}`,
    data
  )
}

export function generateDraftsApi(
  jobId: number,
  opts?: { resume?: boolean; useLlmGenerate?: boolean }
) {
  const { resume = false, useLlmGenerate = true } = opts ?? {}
  return request.post<MoliResult<IngestGenerateResultVo>>(
    `/kb/ingest/jobs/${jobId}/generate?resume=${resume}&useLlmGenerate=${useLlmGenerate}`
  )
}

export function publishIngestJobApi(
  jobId: number,
  opts?: { sync?: boolean; approveAll?: boolean }
) {
  const { sync = true, approveAll = true } = opts ?? {}
  return request.post<MoliResult<IngestPublishResultVo>>(
    `/kb/ingest/jobs/${jobId}/publish?sync=${sync}&approveAll=${approveAll}`
  )
}
```

---

## 10. i18n 键建议（增量）

| 键 | 中文示例 |
|----|----------|
| `ingest.express.preview` | 一键预览 |
| `ingest.express.publish` | 确认入库 |
| `ingest.express.templateMode` | 模板入库（不调 LLM） |
| `ingest.express.skeletonPlan` | Express Plan（快速 skeleton） |
| `ingest.nextSteps.title` | 入库完成，建议下一步 |
| `ingest.rawCoverage.blocked` | 该 raw 已被其它 wiki 页引用，请 enrich 或换源 |

---

## 11. 验收清单与进度（I1–I5）

### 11.1 总清单

| ID | 项 | 后端 | 前端 | 验收 |
|----|-----|------|------|------|
| **I1** | Express / Expert 模板 & LLM 降级 | ✅ | ⚠️ | `useLlmGenerate=!templateMode`；`llmFallback` → Toast |
| **I2** | `publish`/`commit` 后 **nextSteps** CTA | ✅ | 🔵 | 跳转 Wiki 治理 / 健康体检 |
| **I3** | raw **覆盖/簇引用** commit 报错可读 | ✅ | 🔵 | 解析 `data.conflicts` + `msg`；引导 enrich（见 [ops §2.6](../ops/knowledge-workbench-operations.md#26-raw-覆盖与簇已引用)） |
| **I4** | Express **`useLlmPlan`** 与模板勾选独立 | ✅ | ⚠️ | 两 checkbox 分别映射 query |
| **I5** | Expert **`generate?resume=`** 断点续跑 | ✅ | ✅ | 跳过已生成 slug |
| **I6** | Expert **T15f SSE** `generate/start` + `stream` + 当前 slug 进度 | ✅ | ✅ | 无 start 时回退同步 generate |

### 11.2 回归场景（手工）

- [ ] Express：2 个 raw → 一键预览 → diff → 确认入库  
- [ ] 模板入库 → 无 LLM 调用  
- [ ] 重复 ingest 已 covered raw → 报错 + 文案  
- [ ] enrich 草稿 diff：`baseline` + `patch`  

---

## 12. 代码落点（meiling-ui · 增量）

| 文件 | 职责 |
|------|------|
| `src/api/knowledge/kbIngest.ts` | `expressStartApi` / `generateDraftsApi` / `publishIngestJobApi`（§9） |
| `src/components/knowledge/KbWorkflowNextSteps.vue` | 渲染 `nextSteps[]`（I2） |
| `src/views/knowledge/ingest/*` | ExpressOptions 双 checkbox（I1/I4）；commit 错误区（I3） |
| `src/types/knowledge/kbIngest.ts` | `IngestCommitResultVo.nextSteps`、`IngestGenerateResultVo.templateMode` |

---

## 13. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-12 | §4.4a T15f SSE generate 进度；§11 I6 验收 |
| 2026-07-06 | §0 T20 三 Tab + 链 [kb-import-entry-frontend.md](kb-import-entry-frontend.md) |
| 2026-06-28 | §4.4 Expert `useLlmGenerate=!templateMode`；B3 `llmFallback` 字段 |
| 2026-06-28 | §11 I1–I5 验收进度 + §12 落点 |
| 2026-06-28 | 新增前端对接文档：Express 参数、模板模式、nextSteps、raw 门禁 |
| 2026-06-25 | T15 后端交付；Expert 六步已在 KNOWLEDGE_API §9 |
