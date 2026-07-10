# 知识库导入入口 · 前端对接说明（meiling-ui · T20）

> **读者**：meiling-ui 前端。  
> **状态**：**T20a/T20b 后端 ✅**；前端三 Tab 可联调（Mock 可选关闭）。  
> **HTTP 契约总表**：[KNOWLEDGE_API.md](KNOWLEDGE_API.md) §8.8（Wiki 成品）· §9.10（Raw 投喂）  
> **产品设计**：[knowledge-import-entry-prd.md](../product/knowledge-import-entry-prd.md)  
> **技术方案**：[kb-import-entry-design.md](../design/kb-import-entry-design.md)

---

## 1. 做什么

在 **Ingest 工作台** 增加三 Tab，让 Editor **全程浏览器**完成「投喂 → 入库 → 成品导入」，**不需要 SSH**。

| Tab | 名称 | 用户动作 | 关键 API |
|-----|------|----------|----------|
| **Tab1** | 投喂 Raw | 选空间 + prefix + 上传 md/txt | `POST /kb/ingest/raw-upload` |
| **Tab2** | 选源入库 | 现有 Expert / Express（**不变**） | [ingest-workbench-frontend.md](ingest-workbench-frontend.md) §9 |
| **Tab3** | 成品导入 | 选空间 + 分类 + 上传成品 md | `POST /kb/wiki/page/import` |

流程图：[`docs/diagrams/png/moli-kb-import-entry.png`](../diagrams/png/moli-kb-import-entry.png) · API 时序：[`moli-kb-import-entry-api.png`](../diagrams/png/moli-kb-import-entry-api.png)

### 1.1 产品边界（用户侧 vs 运维侧）

| 归属 | 做什么 | 文档 |
|------|--------|------|
| **用户侧（本迭代 T20）** | Tab1 浏览器上传 raw · Tab2 Ingest · Tab3 浏览器导入成品 md · Wiki 单篇编辑 | 本文 + [kb-import-entry-frontend.md](kb-import-entry-frontend.md) |
| **运维侧（不在前端/产品范围）** | SSH/SFTP/rsync、`git pull` 批量落盘、T22 raw 图包发布、EC2 磁盘备份 | [knowledge-workbench-operations.md](../ops/knowledge-workbench-operations.md) · `deploy/` |

**结论**：Editor **从不**走 SSH；运维直写磁盘是 bulk/首发/CI **兜底**，与 T20 Web API **并列、不替代**。

---

## 2. 路由与菜单

| 项 | 值 |
|----|-----|
| 页面 | 仍 **`knowledge/ingest/index`**（扩展为三 Tab，**不新开路由**） |
| 组件 | `KnowledgeIngestWorkbenchView.vue`（或现有等价命名） |
| 菜单 | 906 Ingest 工作台（`kb:ingest:list`） |
| Tab1 权限 | `kb:ingest:rawUpload` + 空间 **editor**（SQL：`docs/sql/16_kb_import_entry_menu.sql`） |
| Tab3 权限 | `kb:wiki:edit` + 空间 **editor**；内嵌 Sync 需 `kb:sync:trigger` |

**网关前缀**（与现有知识库 API 一致）：

```
{VITE_API_BASE_URL}/KnowledgeServer/kb/ingest/raw-upload
{VITE_API_BASE_URL}/KnowledgeServer/kb/wiki/page/import
```

建议沿用 `src/api/knowledge.ts` 的 `KB_BASE = '/KnowledgeServer/kb'`。

> **路径说明**：Wiki 编辑既有接口在 [KNOWLEDGE_API §8.1](KNOWLEDGE_API.md#81-读写-wiki-文件-t14a-已实现) 记为 `/kb/wiki-moli/page`；Controller 实际为 `/kb/wiki/*`。T20 新增 **`/kb/wiki/page/import`**，与 PUT `/kb/wiki/page` 同 Controller；前端封装时与现有 `kbWiki.ts` 基路径保持一致即可。

---

## 3. 页面结构

```text
KnowledgeIngestWorkbenchView.vue
├─ KbSpaceSelector                    # 三 Tab 共享 spaceId
├─ KbImportDecisionHint（折叠）        # PRD 决策树：何时 Tab1 vs Tab2 vs Tab3
├─ el-tabs
│   ├─ Tab1: KbRawUploadPanel
│   ├─ Tab2: 现有 Ingest UI（不变）
│   └─ Tab3: KbWikiImportPanel
└─ KbWorkflowNextSteps（Tab3 成功后）
```

**Tab 切换联动（Tab1 → Tab2）**

上传成功后 emit：

```typescript
emit('switch-tab', 'ingest', {
  highlightRawPaths: ['test-walkthrough/demo.md'], // 相对 kb/raw/，与 raw-tree 一致
  expandPrefix: 'test-walkthrough',
})
```

Tab2 `IngestRawPanel` 收到后：刷新 `GET /kb/ingest/raw-tree`，展开 prefix，勾选对应 `rawPaths`。

---

## 4. 接口一览

| # | Tab | 方法 | 路径 | 状态 |
|---|-----|------|------|------|
| 1 | Tab1 | POST | `/kb/ingest/raw-upload` | ✅ T20a |
| 2 | Tab1 | GET | `/kb/ingest/raw-tree` | ✅ 已有 |
| 3 | Tab1 | GET | `/kb/ingest/raw-prefixes` | 🔵 P1 可选 |
| 4 | Tab3 | POST | `/kb/wiki/page/import` | ✅ T20b |
| 5 | Tab3 | GET | `/kb/category/tree?spaceId=` | ✅ 已有 |
| 6 | Tab3 | POST | `/kb/wiki/page/lint-preview` | ✅ 已有（可选） |
| 7 | Tab3 | POST | `/kb/sync/trigger` | ✅ 已有（import 内嵌或手动） |

P1（可选增强 UI）：`POST /kb/ingest/raw-upload/zip` ✅、`POST /kb/wiki/page/import/batch` ✅。

---

## 5. Tab1 · Raw 投喂

### 5.1 UI 要素

| 元素 | 行为 |
|------|------|
| 空间 | 与 Tab2 共享；无 editor 时整 Tab disabled |
| prefix | 输入 + 可选下拉（P1：`raw-prefixes`）；例 `test-walkthrough`、`school/fe` |
| 文件 | drag-drop / 多选；仅 `.md` `.markdown` `.txt` |
| 冲突策略 | 单选：`SKIP`（默认）/ `OVERWRITE` / `RENAME` |
| 上传 | 调用 `uploadRaw` → 结果表（uploaded / skipped / renamed） |
| CTA | 「去选源入库」→ 切 Tab2 + 高亮路径 |

### 5.2 请求 · `POST /kb/ingest/raw-upload`

**Content-Type**：`multipart/form-data`

| 字段 | 必填 | 说明 |
|------|------|------|
| `spaceId` | 是 | ACL 按空间；raw 目录物理共享 |
| `prefix` | 是 | 相对 `kb/raw/` 的子路径 |
| `file` | 是 | 可重复字段名，多文件 |
| `onConflict` | 否 | `SKIP` / `OVERWRITE` / `RENAME`，默认 `SKIP` |

**示例（axios）**

```typescript
const form = new FormData()
form.append('spaceId', String(spaceId))
form.append('prefix', prefix)
form.append('onConflict', onConflict)
files.forEach((f) => form.append('file', f))

await request.post(`${KB_BASE}/ingest/raw-upload`, form, {
  headers: { 'Content-Type': 'multipart/form-data' },
})
```

### 5.3 响应 · `RawUploadResultVo`

```typescript
export type RawUploadItemVo = {
  path: string       // 相对 kb/raw/，如 test-walkthrough/demo.md
  size: number
  overwritten?: boolean
}

export type RawUploadSkippedVo = {
  path: string
  reason: 'ALREADY_EXISTS' | string
}

export type RawUploadRenamedVo = {
  path: string       // 最终路径，如 demo-1.md
  originalName: string
}

export type RawUploadResultVo = {
  uploaded: RawUploadItemVo[]
  skipped: RawUploadSkippedVo[]
  renamed: RawUploadRenamedVo[]
}
```

`path` 与 Ingest `rawPaths`、raw-tree 节点 **完全一致**（不含 `raw/` 前缀）。

### 5.4 错误与权限

| HTTP / code | 场景 | UI |
|-------------|------|-----|
| 403 | 无 `kb:ingest:rawUpload` 或非空间 editor | Tab1 只读 + 文案「请联系管理员开通 Raw 投喂」 |
| 400 | `prefix` 含 `..`、非法扩展名、超大小/数量 | 字段级错误 Toast |
| partial 成功 | 部分文件失败 | 结果表区分 uploaded / skipped；已成功文件保留 |

**限制**（后端配置，前端可预校验）：单文件 ≤ 5MB；单次 ≤ 20 文件。

---

## 6. Tab3 · Wiki 成品导入

### 6.1 UI 步骤

| 步 | UI |
|----|-----|
| 1 | 空间 + 分类（`GET /kb/category/tree?spaceId=`） |
| 2 | 选单个 `.md`（P0）；P1 多文件 |
| 3 | 表格：文件名 / slug / title（可编辑） |
| 4 | 预览落盘路径 `{dir_slug}/{slug}.md` |
| 5 | ☑ 导入后 Sync（**默认开**）· ☑ lint 预检（可选） |
| 6 | 确认 → import → Toast + `KbWorkflowNextSteps` |

**与 T14e 复用**：分类选择组件、`kbWikiTemplate.ts` frontmatter 常量（新建文档与 Tab3 共用）。

**插图提示**（MVP）：含 `![](foo.png)` 相对路径时，Sync 后 Web 可能空白 — Alert 链 [kb-markdown-image-frontend.md](kb-markdown-image-frontend.md)。

### 6.2 请求 · `POST /kb/wiki/page/import`

**Content-Type**：`multipart/form-data`

| 字段 | 必填 | 说明 |
|------|------|------|
| `spaceId` | 是 | 目标空间 |
| `categoryId` | 是 | `kb_category.id` → 落盘 `{dir_slug}/{slug}.md` |
| `file` | 是 | 单个 `.md` |
| `slug` | 否 | 裸 slug（无分类前缀）；默认文件名 stem |
| `title` | 否 | 覆盖 frontmatter title |
| `onConflict` | 否 | `FAIL`（默认）/ `OVERWRITE` |
| `lintPreview` | 否 | `true` 返回 warn，不阻塞 |
| `sync` | 否 | `true`（**默认 true**）导入后 trigger Sync |
| `assetsZip` | 否 | **T20e** · zip 插图包（png/jpg/gif/webp）；解压到 `{slug}.assets/` |

```typescript
const form = new FormData()
form.append('spaceId', String(spaceId))
form.append('categoryId', String(categoryId))
form.append('file', file)
if (slug) form.append('slug', slug)
if (title) form.append('title', title)
form.append('onConflict', onConflict)
form.append('lintPreview', String(lintPreview))
form.append('sync', String(syncAfter))

await request.post(`${KB_BASE}/wiki/page/import`, form, {
  headers: { 'Content-Type': 'multipart/form-data' },
})
```

### 6.3 响应 · `WikiImportResultVo`

```typescript
export type WikiImportSyncVo = {
  triggered: boolean
  success: boolean
  documentId?: number
  message?: string
}

export type WikiImportResultVo = {
  slug: string              // 全 slug，如 ops/运维手册
  spaceId: number
  relativePath: string      // 如 wiki-moli/ops/运维手册.md
  created: boolean
  contentHash: string
  lintWarnings: string[]
  sync: WikiImportSyncVo
  nextSteps: KbWorkflowHintVo[]
  assetsImported: string[]   // T20e · 如 assets/imageFile1.png
}
```

`nextSteps` 与 Ingest commit 相同，复用 `KbWorkflowNextSteps.vue`（keys：`wiki_govern_lint`、`kb_health_scan` 等，见 [ingest-workbench-frontend.md §5](ingest-workbench-frontend.md#5-nextsteps-t19--前端增量)）。

### 6.4 错误

| 场景 | 预期 |
|------|------|
| 上传 `.pdf` / 非 md | 400 + 文案「成品 Wiki 请用 .md；原始语料请用 Tab1」 |
| slug 已存在 + `onConflict=FAIL` | 409 |
| import 成功、sync 失败 | HTTP 200；`sync.success=false`；提示手动「Wiki 同步」 |
| 无 `kb:sync:trigger` 且 `sync=true` | 403 或 sync 跳过（以后端为准） |

---

## 7. TypeScript 封装建议

**`src/api/knowledge/kbIngest.ts`**

```typescript
import type { RawUploadResultVo } from '@/types/knowledge/kbImport'

export function uploadRawApi(
  spaceId: number,
  prefix: string,
  files: File[],
  onConflict: 'SKIP' | 'OVERWRITE' | 'RENAME' = 'SKIP',
) {
  const form = new FormData()
  form.append('spaceId', String(spaceId))
  form.append('prefix', prefix)
  form.append('onConflict', onConflict)
  files.forEach((f) => form.append('file', f))
  return request.post<MoliResult<RawUploadResultVo>>(
    `${KB_BASE}/ingest/raw-upload`,
    form,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  )
}
```

**`src/api/knowledge/kbWiki.ts`**

```typescript
import type { WikiImportResultVo } from '@/types/knowledge/kbImport'

export type WikiImportForm = {
  spaceId: number
  categoryId: number
  file: File
  slug?: string
  title?: string
  onConflict?: 'FAIL' | 'OVERWRITE'
  lintPreview?: boolean
  sync?: boolean
}

export function importWikiPageApi(payload: WikiImportForm) {
  const form = new FormData()
  Object.entries(payload).forEach(([k, v]) => {
    if (v !== undefined && v !== null) form.append(k, v instanceof File ? v : String(v))
  })
  return request.post<MoliResult<WikiImportResultVo>>(
    `${KB_BASE}/wiki/page/import`,
    form,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  )
}
```

**`src/types/knowledge/kbImport.ts`** — 汇总 §5.3、§6.3 类型。

---

## 8. i18n 键建议

| 键 | 中文示例 |
|----|----------|
| `knowledge.ingest.tabRawUpload` | 投喂 Raw |
| `knowledge.ingest.tabIngest` | 选源入库 |
| `knowledge.ingest.tabWikiImport` | 成品导入 |
| `knowledge.ingest.rawUpload.prefix` | 存放目录（prefix） |
| `knowledge.ingest.rawUpload.goIngest` | 去选源入库 |
| `knowledge.ingest.wikiImport.previewPath` | 将写入：{path} |
| `knowledge.ingest.wikiImport.syncDefault` | 导入后自动 Sync |
| `knowledge.ingest.wikiImport.imageWarn` | 含本地相对插图时，Sync 后可能无法显示 |

与现有 `knowledge.ingest.*` 并列，zh / en / ja 三语。

---

## 9. Mock / 联调策略（后端未就绪时）

1. **环境变量** `VITE_MOCK_KB_IMPORT=true` 时，`uploadRawApi` / `importWikiPageApi` 返回本文 JSON 样例（延迟 300ms）。
2. **Tab2 联调**：Tab1 Mock 仍应 emit `highlightRawPaths`，验证 raw-tree 勾选联动。
3. **Swagger 就绪后**：删除 Mock，对照 [KNOWLEDGE_API §8.8 / §9.10](KNOWLEDGE_API.md) 字段 diff。

---

## 10. 验收清单（T20 · 前端）

| ID | 项 | 依赖 | 验收 |
|----|-----|------|------|
| **T20-F1** | 三 Tab + 决策树折叠说明 | — | 切换 Tab 不丢 spaceId |
| **T20-F2** | Tab1 上传 + 结果表 | T20a | md 上传 → Tab2 raw-tree 可见 |
| **T20-F3** | Tab1 → Tab2 高亮 | T20a | 自动勾选刚上传 rawPaths |
| **T20-F4** | Tab3 单文件 import | T20b | 成品 md → Sync → 浏览可搜 |
| **T20-F5** | nextSteps 卡片 | T20b | 复用 `KbWorkflowNextSteps` |
| **T20-F6** | 权限 v-if | SQL 906 | 无 rawUpload 时 Tab1 只读 |
| **T20-F7** | 与 T14e 组件复用 | T14e | 分类选择一致 |

### 手工回归

- [ ] Tab1：`prefix=test-walkthrough` 上传 2 个 md → Tab2 Express 模板入库 → Sync → 浏览可见  
- [ ] Tab3：选 moli-ops-manual + ops 分类 → 上传成品 md → 默认 Sync → 搜索命中  
- [ ] Tab3：`onConflict=FAIL` 重复 slug → 409 可改 OVERWRITE  
- [ ] Tab3：关 Sync → 磁盘有文件、浏览可能旧数据 → 手动 Sync 修复  
- [ ] 无 `kb:ingest:rawUpload` → Tab1 禁用 + 说明文案  

---

## 11. 代码落点（meiling-ui · 增量）

| 文件 | 职责 |
|------|------|
| `src/views/knowledge/ingest/index.vue` | 三 Tab 容器 |
| `src/components/knowledge/KbRawUploadPanel.vue` | Tab1 |
| `src/components/knowledge/KbWikiImportPanel.vue` | Tab3 |
| `src/components/knowledge/KbImportDecisionHint.vue` | PRD 决策树 |
| `src/api/knowledge/kbIngest.ts` | `uploadRawApi` |
| `src/api/knowledge/kbWiki.ts` | `importWikiPageApi` |
| `src/types/knowledge/kbImport.ts` | T20 类型 |
| `src/components/knowledge/KbWorkflowNextSteps.vue` | 已有，Tab3 复用 |

---

## 12. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-06 | 初版：T20 Tab1/Tab3 契约、类型、Mock 策略、验收清单（后端并行开发） |
