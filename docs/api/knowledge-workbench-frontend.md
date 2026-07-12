# 知识库工作台 · 前端开发总览（meiling-ui）

> **读者**：meiling-ui 前端。后端 API **已就绪**；本文是联调入口，细节见各子文档。  
> **产品需求**：[knowledge-workbench-requirements.md](../product/knowledge-workbench-requirements.md) · **[知识库运维 PRD](../product/knowledge-ops-prd.md)**  
> **HTTP 契约总表**：[KNOWLEDGE_API.md](KNOWLEDGE_API.md)

---

## 1. 开发优先级

| 优先级 | 页面 | 路由 | 后端 | 前端 | 对接文档 |
|--------|------|------|------|------|----------|
| **P0** | **健康体检 · Sync 区** | `knowledge/lint/index` | 🔵 KBOPS-1 | 🔵 **O1–O4** · **O9** | **[knowledge-ops-frontend.md §3](knowledge-ops-frontend.md#3-p0--健康体检页-sync-增强o1o4)** |
| **P1** | **健康体检 · 工单区** | `knowledge/lint/index` | ✅ KBOPS-8/10 | 🔵 **O5–O8**（**KBOPS-8f**） | **[knowledge-ops-frontend.md §3.7](knowledge-ops-frontend.md#37-p1--体检工单增强o5o8--kbops-810)** |
| **P0** | Ingest 工作台 | `knowledge/ingest/index` | ✅ T15+T18+T19+T20+**T15f SSE** | ✅ **T20f** + Expert SSE 进度 | [ingest-workbench-frontend.md](ingest-workbench-frontend.md) · **[kb-import-entry-frontend.md](kb-import-entry-frontend.md)** |
| **P0** | Wiki 治理 | `knowledge/wiki-govern/index` | ✅ T16a/e/g | 🔵 **Spec 已定**（见 §10.2；当前 MVP 仅 Lint+AI） | [wiki-govern-frontend.md](wiki-govern-frontend.md) · **[knowledge-ops-frontend.md §4](knowledge-ops-frontend.md#4-p0--wiki-治理t16f--kbops-6)** |
| P1 | 平台 LLM 设置 | `system/kb-llm` | ✅ T19 | 🔵 T19d | [kb-llm-platform-frontend.md](kb-llm-platform-frontend.md) |
| P1 | Wiki 编辑 | `knowledge/wiki/edit` | ✅ T14 | ✅ 已有 | KNOWLEDGE_API §8 |

**网关前缀**：`{VITE_API_BASE_URL}/KnowledgeServer` + 下表路径（如 `/kb/wiki-moli/lint-space`）。

### 1.1 文档浏览 / 文档管理 · 体裁 × 分类筛选（P0 · v3 多选后端已就绪）

> **权威契约**：[KNOWLEDGE_API.md §2.1.3](KNOWLEDGE_API.md#213-浏览管理页筛选-ui-规范体裁--分类--平行双-facet)（含 v2 联动 + **v3 多选**）  
> **产品/交互稿**（meiling-ui 仓）：`docs/kb-browse-multi-select-filter.md`

**筛选模型**：维度内 **OR**（`kbTypes` / `categoryIds` 重复 query）；维度间 **AND**；空=不过滤。

### 1.2 页附件（MinIO）· UI 入口定案

> **产品**：与 [knowledge-import-entry-prd.md §4.4](../product/knowledge-import-entry-prd.md#44-wiki-页附件t4-已有--t21-增强--不在本-tab) 一致。  
> **存储**：见 [KNOWLEDGE_API.md §5.6](KNOWLEDGE_API.md#56-附件-kbattachmentt4)（`kb_attachment.object_key` + 运行时 `/kb/attachment/{id}`）。

| 页面 | 路由 | 附件 UI |
|------|------|---------|
| **文档浏览** | `knowledge/browse/index` | **只读**：`GET /kb/attachment/list` + 下载；**禁止** upload/delete |
| **Wiki 编辑** | `knowledge/wiki/edit` | **可写**：upload / delete / 列表（`KbAttachmentsPanel` 或等价组件） |

**前置条件**

1. 页已 Sync，有 `documentId`（upload 必填 `documentId`）。  
2. 当前空间 `canEdit=true` 才展示编辑页上传区。  
3. 浏览页 viewer 仅见列表与下载，不见上传按钮。

**浏览页 editor 可选 CTA**

```text
[管理附件 →]  →  router.push('/knowledge/wiki/edit?slug=...&spaceId=...&documentId=...')
```

**与 inline 图区分**

| 能力 | 存储 | 入口 |
|------|------|------|
| MinIO 页附件（pdf/zip） | `kb_attachment` + MinIO | Wiki **编辑**页 |
| 正文 inline 图（T22） | wiki `.assets/` 或 raw + Asset API | markdown + `/kb/wiki/asset` |

### 1.3 Markdown 插图鉴权（T22 F1 · P0）

> **完整对接** → **[kb-markdown-image-frontend.md](kb-markdown-image-frontend.md)**（`KbMarkdownImage`、URL 解析、blob 拉图、验收清单）。  
> **后端**：[KNOWLEDGE_API.md §8.0](KNOWLEDGE_API.md#80-inline-图片-assett22-r0) ✅  
> **现状**：回迁正文已入库（D/A 档）；浏览页插图空白 = **前端未带 token 请求 asset**。

| 任务 | 优先级 | 说明 |
|------|--------|------|
| `KbMarkdownImage` + 接入 browse/preview | **P0** | `/kb/raw/asset`、`/kb/wiki/asset` |
| 浏览页附件只读 + 跳转编辑页 | **P1** | 见 §1.2 |
| 编辑页 Vditor 插图上传（F2） | ✅ | `POST /kb/wiki/asset` · `KbWikiImageInsert` |

| 行 | 数据源 | 单选 (v2) | 多选 (v3) |
|----|--------|-----------|-----------|
| 体裁 | `GET /kb/index/types?spaceId=` | `kbType` | `kbTypes=…&kbTypes=…` |
| 分类 | `GET /kb/index?spaceId=` | `categoryId` / `uncategorizedOnly` | `categoryIds=…` + 可选 `uncategorizedOnly` |
| 列表 | `GET /kb/document/search?source=kb&…` | 同上 | 同上；**total 以 search 为准** |

**facet 联动**：分类已选 → `/kb/index/types?categoryIds=…`；体裁已选 → `/kb/index?kbTypes=…`。

**TypeScript 请求参数建议**（前端自行落地，勿与后端 DTO 混用单值/列表）：

```typescript
interface KbBrowseFilters {
  spaceId?: string
  spaceIds?: string[]
  /** 多选优先；长度 0 或不传 = 体裁「全部」 */
  kbTypes?: string[]
  /** 多选优先；与 uncategorizedOnly 可组合 */
  categoryIds?: string[]
  uncategorizedOnly?: boolean
  keyword?: string
}
```

---

## 2. 两条主链路（勿混淆）

### 2.1 Ingest — 投喂 **新 raw**

```
选 raw → Plan → 生成草稿 → diff 审阅 → lint → commit → (Sync) → nextSteps
```

- **Expert**：六步逐步操作  
- **Express**：`POST .../express` 一键预览 → `POST .../publish` 确认入库  
- **模板模式**：`useLlmGenerate=false`（raw 直贴，不调 LLM）

### 2.2 Wiki 治理 — 修 **已有 wiki 页**

```
选空间 → lint-space（文件真值）→ script-fix / ai-batch-fix / auto-fix → (Sync)
```

- **不要**在治理页做批量 `POST /kb/wiki-moli/enrich`（会 append 章节，不能修 metadata/断链）  
- `dup_slug` → `merge-hint` 复制 Cursor 指令 + 跳转单页编辑

---

## 3. 共享能力

### 3.1 空间选择器

复用 `KbSpaceSelector` + `useKbSpace.ts`；所有写操作需空间 **editor**。

### 3.2 nextSteps（入库 / Sync 后 CTA）

`commit` / `publish` / `sync` 响应含 `nextSteps: KbWorkflowHintVo[]`：

```typescript
export interface KbWorkflowHintVo {
  key: 'wiki_govern_lint' | 'kb_health_scan' | string
  label: string
  description?: string
  routePath: string          // 如 knowledge/wiki-govern/index
  routeQuery?: Record<string, string>  // 如 { spaceId: '900...' }
}
```

渲染：遍历 `nextSteps`，`router.push({ path: hint.routePath, query: hint.routeQuery })`。

### 3.3 脚本 vs LLM

| 能力 | LLM | 文档 |
|------|-----|------|
| Ingest Plan | 可选（Express 默认 skeleton） | ingest-workbench §4 |
| Ingest 正文 | 默认是；模板模式否 | [knowledge-ingest-acceptance.md](../test/knowledge-ingest-acceptance.md) §1 |
| Wiki 治理 metadata | 否（script-fix） | wiki-govern §8 |
| Wiki 治理断链/孤儿 | 是（ai-batch-fix） | wiki-govern §8 |

矩阵：[knowledge-script-vs-llm-matrix.md](../test/knowledge-script-vs-llm-matrix.md)

---

## 4. 建议代码落点（meiling-ui）

| 模块 | 建议路径 |
|------|----------|
| Wiki 治理 API | `src/api/knowledge/kbWikiGovern.ts` |
| Wiki 治理类型 | `src/types/knowledge/kbWikiGovern.ts` |
| Wiki 治理页 | `src/views/knowledge/wiki-govern/index.vue` |
| Ingest API（已有可扩展） | `src/api/knowledge/kbIngest.ts` |
| nextSteps 组件 | `src/components/knowledge/KbWorkflowNextSteps.vue`（可复用） |

菜单 SQL：`docs/sql/11_kb_wiki_govern_menu.sql`（910 · `kb:wiki:govern:list`）。

---

## 5. 联调环境

1. 启动 `moli-knowledge-server` + 网关  
2. 配置 `kb.llm`（AI 修复 / Ingest LLM 需要）  
3. 部署机可执行 `kb/tools/lint.py`（治理 Lint 依赖）  
4. 测试空间：`enterprise-kb` / `wiki-jp-exam` / `wiki-moli`

---

## 6. 验收分工

| 页面 | 文档章节 |
|------|----------|
| **T20 导入入口** | [kb-import-entry-frontend.md §10](kb-import-entry-frontend.md#10-验收清单t20--前端) |
| Wiki 治理 | [wiki-govern-frontend.md §13–§15](wiki-govern-frontend.md#13-验收清单与进度w1w8) |
| **KB 运维 Sync UI** | [knowledge-ops-frontend.md §3、§10](knowledge-ops-frontend.md#36-验收-o1o4) |
| Ingest 增量 | [ingest-workbench-frontend.md §11–§13](ingest-workbench-frontend.md#11-验收清单与进度i1i5) |
| 操作手册（非前端） | [knowledge-workbench-operations.md §3.2](../ops/knowledge-workbench-operations.md#32-当前-web-页-vs-完整能力t16f-差距) |

---

## 7. 进度摘要（2026-07-11 · 后端视角）

| 模块 | 后端 | 前端 UI | 缺口摘要 |
|------|------|---------|----------|
| **Ingest** | ✅ T15+T18+T19+T20+T20g 后端 | 🔵 **T20f UI** | Tab1：`raw-prefixes` ✅ · zip/batch ✅；前端接下拉。Tab2/3 见 [kb-import-entry-frontend.md](kb-import-entry-frontend.md) |
| **Wiki 治理** | ✅ T16a/e/g | ✅ 全链路 UI | script-fix / auto-fix / merge-hint / syncAfter 已接（2026-07） |
| **共享** | ✅ `nextSteps` + Sync 轮询 O1 | ✅ | `KbWorkflowNextSteps` · 健康体检 O1–O4 可对接 |

---

## 8. 后端确认清单（B1–B10 · 联调前勾选）

> **确认方**：moli-knowledge-server 维护者。前端按「可联调」列接 API；「前端待接」不阻塞 Swagger 验证。

| ID | 能力 | API / 字段 | 后端 | 前端待接 | 备注 |
|----|------|------------|------|----------|------|
| **B1** | 空间 Lint（文件真值） | `POST /kb/wiki-moli/lint-space` | ✅ | ⚠️ 已接 | `exitCode≠0` 仍 HTTP 200 |
| **B2** | 治理 options | `GET /kb/wiki-moli/govern/options` | ✅ | ⚠️ 部分 | 含 `scriptFixableKinds` / `aiFixableKinds` / `manualOnlyKinds` |
| **B3** | 脚本修复 | `POST /kb/wiki-moli/govern/script-fix` | ✅ | ✅ | `missing_dates` / `slug_mismatch` / `missing_source` |
| **B4** | AI 批量修复 | `POST /kb/wiki-moli/govern/ai-batch-fix` | ✅ | ✅ | 需 `kb.llm` |
| **B5** | 一键 auto-fix | `POST /kb/wiki-moli/govern/auto-fix` | ✅ | ✅ | 含 `relintAfter` / `syncAfter` |
| **B6** | 合并提示 | `POST /kb/wiki-moli/govern/merge-hint` | ✅ | ✅ | `manualSteps` / `relatedSlugs` · `dup_slug` 等 |
| **B7** | Sync | `POST /kb/sync/trigger` + `GET /kb/sync/status` | ✅ | ✅ | O1 轮询 `running`/`lastStatus`；trigger 仍同步阻塞 |
| **B8** | Ingest 模板模式 | `useLlmGenerate=false` on express/generate/prepare/regenerate | ✅ | ⚠️ 已接 | 响应 `templateMode=true` |
| **B9** | 入库后引导 | `commit.nextSteps` / `publish.nextSteps` / `SyncTriggerVo.nextSteps` | ✅ | 🔵 Spec §10.1 | keys: `wiki_govern_lint`, `kb_health_scan` |
| **B10** | raw 覆盖门禁 | commit/publish 业务错误 | ✅ | 🔵 Spec §10.1 | `code=10012` + `IngestRawConflictVo` |

**后端实现入口**：`KbWikiController`（govern/*）、`KbIngestController`（§9）、`KbRawCoverageServiceImpl.assertRawOpenForCommit`、`KbWorkflowHints`。

---

## 8.1 联调 FAQ（前端优先确认 · 后端已定案）

### B1 / I1 — Express 列表「一键入库」行为

| 问题 | **后端结论** |
|------|----------------|
| 应预览再 publish，还是 express+publish 一次完成？ | **必须两步**。`POST .../jobs/express` = `createJob` + `prepare`（Plan + `generate`），**不含** publish/commit。确认入库另调 `POST .../jobs/{id}/publish?sync=&approveAll=`。 |
| 能否一个 HTTP 完成？ | **否**。无合并接口；前端可链式调用，但中间应展示 diff。 |

实现：`KbIngestServiceImpl.expressStart` → `createJob` + `prepare`；`publish` 独立。

---

### B2 / I2 — `nextSteps` 契约

| 字段 | **后端结论** |
|------|----------------|
| 何时有 `nextSteps[]` | **`commit` 成功**后必有（`KbWorkflowHints.afterWikiWrite`）。`publish` 在 `committed=true` 时从 `commit` 拷贝。`POST /kb/sync/trigger` 成功时 `SyncTriggerVo` 也有。 |
| 失败时 | `publish` 若 lint 未过：`committed=false`，**无** `nextSteps`。commit 抛错（如 raw 门禁）：HTTP 业务错误，**无** body 内 nextSteps。 |
| `key` | 固定：`wiki_govern_lint`、`kb_health_scan`（`KbWorkflowHints` 常量） |
| `routePath` | 固定：`knowledge/wiki-govern/index`、`knowledge/lint/index` |
| `routeQuery.spaceId` | 固定：**字符串**雪花 ID，`String.valueOf(spaceId)`，如 `"900000000000000002"` |

前端：`router.push({ path: hint.routePath, query: hint.routeQuery })`，勿写死 spaceId。

---

### B3 / I3 — LLM 不可用降级

| 场景 | **后端结论** |
|------|----------------|
| `useLlmGenerate=true` 且 LLM **未配置/已禁用** | **`generate` / `prepare` / `express` / `draft/regenerate` 自动降级模板模式**，不抛错。 |
| 响应字段 | `templateMode=true`，`llmFallback=true`，`llmFallbackReason`（Toast 文案） |
| `generatePlan`（Expert Plan） | LLM 不可用时 **降级 skeleton Plan**（仅 Plan，不是正文）。 |
| 用户显式 `useLlmGenerate=false` | `templateMode=true`，`llmFallback=false` |
| LLM **已配置但调用失败**（HTTP 超时等） | 单页 `failed++`，**不**整批自动降级；需人工重试或改模板 |

**前端（meiling-ui）**：

1. Expert **生成 / 重生成**：`useLlmGenerate: !templateMode`（与 Express 勾选一致）
2. 响应 `llmFallback===true` → Toast `llmFallbackReason`（或固定「已改用模板模式」）
3. 勾选「模板入库」时传 `useLlmGenerate=false`，Toast「未调用 LLM」

**Swagger 联调**：LLM 关闭时 `POST .../generate?useLlmGenerate=true` 应 200 且 `data.llmFallback=true`。

---

### B4 / I4 — commit 冲突错误体

| 项 | **后端结论** |
|----|----------------|
| 响应形态 | `MoliResult`：`code=10012`，**`msg` + 结构化 `data`**（`IngestRawConflictVo`） |
| `data.errorKind` | 固定 `"INGEST_RAW_ALREADY_COVERED"` |
| `data.conflicts[]` | 同 `GET /kb/ingest/raw-coverage` 的 `items`：`path`、`coverage`、`matchKind`、`wikiSlugs` |
| `data.spaceId` / `data.jobId` | 当前空间与批次 |
| `data.hint` | 建议文案（enrich 或换 raw） |
| 典型 `msg` | 仍保留可读整句（单条或多条汇总） |
| 前端建议 | Toast 用 `msg`；详情面板用 `data.conflicts` 渲染冲突表 + 「去 enrich」链到已有 `wikiSlugs`；列表侧仍可用 raw-coverage 预标注 |

**示例**（commit / publish 被 raw 门禁拦截）：

```json
{
  "code": 10012,
  "msg": "raw 已被 wiki 引用，禁止重复 ingest：raw/school/fe/foo.md → wiki [guides/说明]。请对已有页 enrich 或更换 raw 源。",
  "data": {
    "errorKind": "INGEST_RAW_ALREADY_COVERED",
    "spaceId": "900000000000000002",
    "jobId": "900000000000000100",
    "hint": "请对已有页 enrich 或更换 raw 源。",
    "conflicts": [
      {
        "path": "fe/foo.md",
        "coverage": "cluster",
        "matchKind": "dir_prefix",
        "wikiSlugs": ["guides/说明"]
      }
    ]
  }
}
```

---

### W1 / W5 — 治理 kind 与 relint

| 项 | **后端结论** |
|----|----------------|
| `govern/options` vs `lint.py` | kind **名称一致**（`missing_dates`、`slug_mismatch`、`broken_link`、`dup_slug` 等）。分类在 **`WikiGovernKindUtil` 硬编码**，与 `lint.py` 输出对齐，**非运行时读 lint 脚本**。 |
| 分类摘要 | **脚本**：`missing_dates`、`slug_mismatch`、`missing_source` · **AI**：`broken_link`、`orphan`、`dup_content`… · **仅人工**：`dup_slug` |
| `dup_slug` 的 `page` | lint 可能为 `null`；script/AI 批量会 **skip**（要求 `page` 非空）；走 merge-hint / 手改。 |
| 单独 `script-fix` / `ai-batch-fix` 后 | **不会**自动再 Lint。 |
| `auto-fix` | 默认 **`relintAfter=true`**，响应含 `relint` + `issuesBefore`/`issuesAfter`。 |
| 前端建议 | 单独点 script/AI 后 **再调 `lint-space`**，或直接用 **auto-fix**。 |

---

## 10. 前端实现规格（meiling-ui · 2026-06-20 定案）

> 本节补全原「待完善」项：**契约与 UI 行为已写死**，前端按表实现即可勾选 B9/B10/W3–W6。

### 10.1 Ingest · nextSteps + raw 冲突（B9 / B10）

#### 组件 `KbWorkflowNextSteps.vue`

| 项 | 规格 |
|----|------|
| 挂载点 | Ingest 详情页 commit/publish 成功弹窗/底栏；Sync 成功 Toast 旁 |
| 输入 | `nextSteps: KbWorkflowHintVo[]`（可能为空） |
| 渲染 | 每个 hint：`el-button` 或 link，`label` 作标题，`description` 作副文案 |
| 点击 | `router.push({ path: hint.routePath, query: hint.routeQuery })` — **勿写死 spaceId** |
| 已知 key | `wiki_govern_lint` → 治理页；`kb_health_scan` → 健康体检 |

#### raw 冲突面板（`code=10012`）

| 项 | 规格 |
|----|------|
| 触发 | `POST .../publish` 或 commit 返回 `code=10012` |
| Toast | 首行用 `msg`（整句可读） |
| 详情抽屉 | 表格列：`path`、`coverage`（open/covered/cluster）、`matchKind`、`wikiSlugs[]` |
| cluster 文案 | `coverage=cluster` 时副标题：「目录级 sources 已引用该 raw 簇，请 enrich 已有页或收窄 sources」 |
| CTA | 每行 wikiSlug → 链 Ingest enrich 或 Wiki 编辑（带 `spaceId` query） |
| 预检（可选 P1） | 勾选 raw 时调 `GET /kb/ingest/raw-coverage`，`cluster`/`covered` 行黄色标记 |

#### Express 两步（B1）

| 步骤 | API | UI |
|------|-----|-----|
| 1 预览 | `POST .../jobs/express` | 展示 Plan + 草稿 diff，**禁止**静默 publish |
| 2 确认 | `POST .../jobs/{id}/publish?sync=&approveAll=` | 成功后再渲染 nextSteps |

### 10.2 Wiki 治理 · 四步闭环（W3–W6）

**2026-07 现状**：meiling-ui 已接 lint → script/AI → merge-hint → auto-fix → Sync → 复检；下文为 API 对照。

```
① lint-space → ② script-fix（metadata）→ ③ ai-batch-fix（断链/孤儿）
     → ④ merge-hint（dup_slug）→ ⑤ auto-fix（一键）→ ⑥ Sync → ⑦ 复检 lint-space
```

| 步骤 | API | UI 要点 |
|------|-----|---------|
| W3 script-fix | `POST /kb/wiki-moli/govern/script-fix` | 勾选 kinds：`missing_dates` / `slug_mismatch` / `missing_source`；展示 `fixed/skipped` |
| W4 merge-hint | `POST /kb/wiki-moli/govern/merge-hint` | `dup_slug` 仅复制 Cursor 指令 + 跳转单页编辑（无自动合并） |
| W5 auto-fix | `POST /kb/wiki-moli/govern/auto-fix` | 默认 `relintAfter=true`；展示 `issuesBefore` → `issuesAfter` |
| W6 Sync | `POST /kb/sync/trigger` 或 auto-fix 的 `syncAfter` | 成功后 nextSteps 同 §10.1 |

**options 下拉**：`GET /kb/wiki-moli/govern/options` 的 `scriptFixableKinds` / `aiFixableKinds` / `manualOnlyKinds` 用于禁用不可点的 kind。

### 10.3 完成定义（前端 DoD）

| 页面 | DoD |
|------|-----|
| Ingest | Express 两步 + templateMode/llmFallback Toast + nextSteps + 10012 冲突抽屉 |
| Wiki 治理 | lint-space + script + AI + merge-hint 入口 + auto-fix + Sync + 复检 |
| 共享 | `KbWorkflowNextSteps.vue` 被 Ingest / Sync / 治理复用 |

---

## 11. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-06-20 | §10 前端实现规格（nextSteps / raw 冲突 / 治理四步闭环 DoD） |
| 2026-06-28 | §8.1 B3 LLM 自动模板降级 + Expert 前端待办 |
| 2026-06-28 | §8.1 B4 结构化冲突 `data`（`IngestRawConflictVo`） |
| 2026-06-28 | §8.1 联调 FAQ（B1–B4 / W1·W5 后端定案） |
| 2026-07-11 | §7/§8/§10.2 对齐现网：治理全按钮 ✅；Ingest Tab1 `raw-prefixes` ⏳；Sync O1 轮询 |
| 2026-06-28 | 新增前端总览；拆分 Ingest / Wiki 治理对接文档 |
| 2026-06-27 | Wiki 治理 T16e 后端 + wiki-govern-frontend 初版 |
| 2026-07-05 | §1.2 页附件 UI 定案；§1.3 + [kb-markdown-image-frontend.md](kb-markdown-image-frontend.md) T22 F1 |
