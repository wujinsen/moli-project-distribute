# Wiki 治理工作台 · 前端对接说明（meiling-ui）

> **读者**：meiling-ui 前端。后端 **T16a/e/g ✅** 已就绪；**T16f 待开发**。  
> **运维排期总览**：[knowledge-ops-frontend.md](knowledge-ops-frontend.md) · **产品 PRD**：[knowledge-ops-prd.md](../product/knowledge-ops-prd.md)  
> **现 UI 与完整方案差距**（仅 Lint + AI 两步时）：[`docs/ops/knowledge-workbench-operations.md` §3.2](../ops/knowledge-workbench-operations.md#32-当前-web-页-vs-完整能力t16f-差距)  
> **总览**：[knowledge-workbench-frontend.md](knowledge-workbench-frontend.md)  
> **HTTP 契约总表**：[KNOWLEDGE_API.md](KNOWLEDGE_API.md) §4.6 + §8.6  
> **产品方案**：[Wiki治理工作台产品方案.md](../../moli-knowledge/kb/wiki-moli/develop/Wiki治理工作台产品方案.md)

---

## 1. 页面与路由

| 项 | 值 |
|----|-----|
| 路由 path | `knowledge/wiki-govern/index` |
| 组件名 | `KnowledgeWikiGovernView`（建议目录 `src/views/knowledge/wiki-govern/`） |
| API 模块 | `src/api/knowledge/kbWikiGovern.ts` |
| 类型 | `src/types/knowledge/kbWikiGovern.ts` |
| 菜单 perms | `kb:wiki:govern:list`（菜单 910，见 `docs/sql/11_kb_wiki_govern_menu.sql`） |
| 网关前缀 | `{VITE_API_BASE_URL}/KnowledgeServer` + 下表路径 |

**与「健康体检」页分工**：

| 页 | 数据源 | 用途 |
|----|--------|------|
| **Wiki 治理**（本页） | 磁盘 `lint.py` | 改 wiki 文件前/后的**文件真值**体检与批量修复 |
| **健康体检** | MySQL `GET /kb/lint` | Sync 后 DB 快照；「扫描并落库」写 `kb_lint_issue` |

治理页修完文件后，若需 DB 可见：勾选 Sync 或跳转健康体检触发同步。

---

## 2. 推荐界面结构

```
KnowledgeWikiGovernView.vue
├─ KbSpaceSelector                    # 复用
├─ GovernLintPanel                    # ① Lint 结果 + 勾选
├─ GovernFixPanel                     # ② 脚本 / AI / 一键 + 模型 + 进度
└─ （可选）Sync 勾选 + 问题数 before→after 摘要
```

**不要**在本页做批量 **enrich**（会 append 章节，不是 metadata 修复）。ingest 仅旁路链接到 Ingest 工作台。

---

## 3. 状态机

```
idle → linted → fixing → (relinted) → (synced)
```

| 状态 | 触发 |
|------|------|
| `idle` | 进入页，已选空间，未 Lint |
| `linted` | `POST /kb/wiki-moli/lint-space` 成功 |
| `fixing` | 调用 script-fix / ai-batch-fix / auto-fix |
| `relinted` | auto-fix 内 `relintAfter` 或用户手动再 Lint |
| `synced` | `syncAfter=true` 或 `POST /kb/sync/trigger` |

---

## 4. 接口一览

经网关完整 URL 示例：`POST /KnowledgeServer/kb/wiki-moli/lint-space`

| # | 方法 | 路径 | 写盘 | LLM | 权限 |
|---|------|------|------|-----|------|
| 1 | POST | `/kb/wiki-moli/lint-space` | 否 | 否 | 空间 **editor** |
| 2 | GET | `/kb/wiki-moli/govern/options` | 否 | 否 | 登录即可 |
| 3 | POST | `/kb/wiki-moli/govern/script-fix` | **是** | 否 | editor |
| 4 | POST | `/kb/wiki-moli/govern/ai-batch-fix` | **是** | 是 | editor + `kb.llm` |
| 5 | POST | `/kb/wiki-moli/govern/auto-fix` | 是 | 部分 | editor |
| 6 | POST | `/kb/wiki-moli/govern/merge-hint` | 否 | 否 | editor |
| 7 | POST | `/kb/sync/trigger?spaceId=` | DB | 否 | `kb:sync:trigger` 或空间 admin |

单页 diff 预览（可选）：`POST /kb/wiki-moli/ai-revise`（不写盘）→ `PUT /kb/wiki-moli/page`。

---

## 5. issue.kind 分类（前端勾选逻辑）

优先读 **`GET /kb/wiki-moli/govern/options`** 的 `scriptFixableKinds` / `aiFixableKinds`；离线兜底用下表。

| 分类 | kind 列表 | 修复方式 |
|------|-----------|----------|
| **脚本** | `missing_dates`, `slug_mismatch`, `missing_source` | `script-fix` |
| **AI** | `broken_link`, `bad_type`, `missing_title`, `orphan`, `missing_concept`, `outdated`, `asym_related`, `near_dup`, `dup_content` | `ai-batch-fix` |
| **仅人工** | `dup_slug` | `merge-hint` 或跳转 `/knowledge/wiki/edit?slug=` |

> `near_dup` / `dup_content` 可走 **AI 修复** 或 **merge-hint**（复制 Cursor 指令手动合并）。

**Lint 完成后默认勾选**：全部「脚本 + AI」可修项；**不勾选** `dup_slug` 与 `level=info` 项（可选：info 默认折叠）。

建议 TypeScript 工具函数：

```typescript
const MANUAL_ONLY = new Set(['dup_slug'])

export function isScriptFixable(kind: string, scriptKinds: string[]) {
  return scriptKinds.includes(kind)
}

export function isAiFixable(kind: string, aiKinds: string[]) {
  return aiKinds.includes(kind)
}

export function isSelectableForBatch(issue: KbWikiLintIssue, scriptKinds: string[], aiKinds: string[]) {
  if (MANUAL_ONLY.has(issue.kind)) return false
  return isScriptFixable(issue.kind, scriptKinds) || isAiFixable(issue.kind, aiKinds)
}

/** 传给修复 API：同一 issue 对象即可，后端按 page+kind 分组 */
export function buildSelectedIssues(
  all: KbWikiLintIssue[],
  selectedKeys: Set<string> // 如 `${page}|${kind}`
) {
  return all.filter(i => selectedKeys.has(`${i.page}|${i.kind}`))
}
```

---

## 6. 请求 / 响应 TypeScript 类型

```typescript
/** §4.6 lint-space */
export interface KbWikiLintIssue {
  level: 'error' | 'warn' | 'info'
  kind: string
  page: string       // = slug，修复目标
  detail?: string
  suggest?: string
}

export interface KbWikiSpaceLintResult {
  spaceCode: string
  wikiDir: string
  stats?: Record<string, number | Record<string, number>>
  issues: KbWikiLintIssue[]
  exitCode: number   // 非 HTTP 失败；0=无阻断
  outputTail?: string
}

/** govern/options */
export interface KbWikiGovernOptions {
  llmAvailable: boolean
  provider?: string
  defaultModel?: string
  models: { id: string; displayName: string }[]
  scriptFixableKinds: string[]
  aiFixableKinds: string[]
  manualOnlyKinds: string[]   // 默认 ['dup_slug']
}

export interface WikiGovernMergeHintItem {
  kind: string
  page: string
  detail?: string
  relatedSlugs?: string[]
  canonicalSlug?: string
  cursorPrompt: string
  manualSteps?: string[]
}

/** script-fix / ai-batch-fix 共用 issue 列表 */
export interface KbWikiGovernFixRequest {
  spaceId: number
  issues: KbWikiLintIssue[]
  dryRun?: boolean   // true=只返回 previewContent，不写盘
}

export interface KbWikiGovernPageResult {
  slug: string
  status: 'ok' | 'skipped' | 'failed'
  kinds?: string[]
  message?: string
  previewContent?: string  // dryRun
}

export interface KbWikiGovernScriptFixResult {
  fixedPages: number
  skippedPages: number
  failedPages: number
  pages: KbWikiGovernPageResult[]
}

export interface KbWikiGovernAiBatchFixResult extends KbWikiGovernScriptFixResult {
  model?: string
}

/** auto-fix */
export interface KbWikiGovernAutoFixRequest {
  spaceId: number
  issues: KbWikiLintIssue[]
  model?: string
  scriptFix?: boolean    // 默认 true
  aiFix?: boolean        // 默认 true
  relintAfter?: boolean  // 默认 true
  strict?: boolean       // relint 时传给 lint-space
  syncAfter?: boolean    // 默认 false
}

export interface KbWikiGovernAutoFixResult {
  issuesBefore: number
  issuesAfter?: number
  scriptFix?: KbWikiGovernScriptFixResult
  aiFix?: KbWikiGovernAiBatchFixResult
  relint?: KbWikiSpaceLintResult
  sync?: { batchNo?: string; success?: boolean; message?: string } // SyncTriggerVo
}
```

---

## 7. API 封装建议（`api/knowledge/kbWikiGovern.ts`）

```typescript
import request from '@/utils/request'

export function lintWikiSpaceApi(data: { spaceId: number; strict?: boolean }) {
  return request.post<MoliResult<KbWikiSpaceLintResult>>('/kb/wiki-moli/lint-space', data)
}

export function getWikiGovernOptionsApi() {
  return request.get<MoliResult<KbWikiGovernOptions>>('/kb/wiki-moli/govern/options')
}

export function wikiGovernScriptFixApi(data: KbWikiGovernFixRequest) {
  return request.post<MoliResult<KbWikiGovernScriptFixResult>>('/kb/wiki-moli/govern/script-fix', data)
}

export function wikiGovernAiBatchFixApi(data: KbWikiGovernFixRequest & { model?: string }) {
  return request.post<MoliResult<KbWikiGovernAiBatchFixResult>>('/kb/wiki-moli/govern/ai-batch-fix', data)
}

export function wikiGovernAutoFixApi(data: KbWikiGovernAutoFixRequest) {
  return request.post<MoliResult<KbWikiGovernAutoFixResult>>('/kb/wiki-moli/govern/auto-fix', data)
}

export function wikiGovernMergeHintApi(data: { spaceId: number; issues: KbWikiLintIssue[] }) {
  return request.post<MoliResult<{ items: WikiGovernMergeHintItem[] }>>('/kb/wiki-moli/govern/merge-hint', data)
}
```

---

## 8. 按钮与调用顺序

### 8.1 主流程（推荐）

1. 用户选空间 → 点 **「开始 Lint」** → `lintWikiSpaceApi({ spaceId })`
2. 挂载时或 Lint 前 → `getWikiGovernOptionsApi()`（模型下拉 + kind 列表）
3. Lint 成功 → **默认勾选**全部可批量修复项
4. 用户操作三选一：

| 按钮 | API | 说明 |
|------|-----|------|
| **脚本修复** | `script-fix` | 仅 metadata；秒级；无需 model |
| **AI 修复** | `ai-batch-fix` | 需 `llmAvailable`；传 `model: defaultModel \|\| 用户选择` |
| **一键修复已选** | `auto-fix` | `scriptFix+aiFix+relintAfter: true`；Sync 用 checkbox |

### 8.2 一键修复默认请求体

```json
{
  "spaceId": 900000000000000001,
  "issues": [ /* 当前勾选 */ ],
  "model": "deepseek-chat",
  "scriptFix": true,
  "aiFix": true,
  "relintAfter": true,
  "strict": false,
  "syncAfter": false
}
```

成功后在 UI 展示：

- `issuesBefore` → `issuesAfter`（来自 `relint.issues.length`）
- `scriptFix.fixedPages` / `aiFix.fixedPages` / 失败明细 `pages[].message`
- 若勾选 Sync：`syncAfter: true` 或单独调 `POST /kb/sync/trigger?spaceId=`

### 8.3 LLM 不可用

- **脚本修复**：照常可用
- **AI 修复 / 一键（含 aiFix）**：按钮 disabled + 文案「请配置 kb.llm」
- **一键**：可仅 `scriptFix: true, aiFix: false` 降级

### 8.4 dup_slug

列表项显示 **「需手动合并」**，提供链接：

`/knowledge/wiki/edit?slug={encodeURIComponent(page)}&issueType=dup_slug`

不进入批量修复 API。

### 8.5 重复页合并（dup_slug / dup_content）

| 按钮 | API |
|------|-----|
| **复制合并指令** | `POST /kb/wiki-moli/govern/merge-hint` |

请求：`{ spaceId, issues: [ /* dup_slug | dup_content | near_dup */ ] }`

响应 `items[]`：

| 字段 | 用途 |
|------|------|
| `cursorPrompt` | 复制到 Cursor Chat |
| `manualSteps` | UI 步骤列表 |
| `relatedSlugs` / `canonicalSlug` | 展示冲突页 |

---

## 9. GovernLintPanel 要点

- 按 `kind` 分组折叠；组头显示 `stats.by_kind[kind]` 或本地 count
- 行 key：`${issue.page}|${issue.kind}|${index}`（同页多 kind 可多行勾选）
- `level=error` 用醒目样式；`info` 可默认折叠
- 展示 `detail` + `suggest`；`page` 可点击跳编辑页

---

## 10. GovernFixPanel 要点

| 控件 | 行为 |
|------|------|
| 模型下拉 | `options.models`；默认 `defaultModel`；仅 AI / 一键需要 |
| Sync 勾选 | 映射 `auto-fix.syncAfter` 或修复后单独 trigger |
| 进度 | auto-fix 为**单次 HTTP**（后端串行）；显示 loading + 完成后汇总 |
| 结果表格 | `pages[]`：slug / status / kinds / message |

**移除**（若旧代码存在）：批量 enrich、Cloud Agent、治理页内单独 relint+sync 多步向导（一键已含 relint）。

---

## 11. 错误与边界

| 场景 | HTTP | 前端处理 |
|------|------|----------|
| Lint 脚本失败 | `code≠200` | toast + 展示 `outputTail` |
| Lint 有问题 | `code=200`, `exitCode≠0` | **正常**；仍展示 issues |
| script-fix 单页失败 | `code=200`, `pages[].status=failed` | 汇总失败数，不阻断其它页 |
| ai-batch-fix LLM 未配置 | `code≠200` | 提示配置 `kb.llm` |
| 乐观锁冲突 | write 409 语义 | 本 API 已写盘；失败页提示「文件已被修改，请再 Lint」 |
| issues 为空 | `code≠200` | 修复前校验至少勾选 1 项 |

---

## 12. i18n 键建议（`knowledge.wikiGovern.*`）

| 键 | 中文示例 |
|----|----------|
| `title` | Wiki 治理 |
| `lint.start` | 开始 Lint |
| `lint.stats` | 共 {pages} 页，{errors} 错误，{warnings} 告警 |
| `fix.script` | 脚本修复 |
| `fix.ai` | AI 修复 |
| `fix.auto` | 一键修复已选 |
| `fix.model` | 模型 |
| `fix.syncAfter` | 修复后同步到库 |
| `fix.result` | 已修 {fixed} 页，跳过 {skipped}，失败 {failed} |
| `fix.relint` | 问题数 {before} → {after} |
| `kind.manual` | 需手动处理 |
| `llm.unavailable` | LLM 未配置，仅可使用脚本修复 |

---

## 13. 验收清单与进度（W1–W8）

### 13.1 总清单

| ID | 项 | 后端 | 前端 | 验收 |
|----|-----|------|------|------|
| **W1** | `lint-space` + issues 分组勾选 | ✅ | ⚠️ | 按 kind 分组；`exitCode≠0` 仍展示列表 |
| **W2** | **脚本修复** `script-fix` | ✅ | ❌ | `slug_mismatch` **不要**进「需手改」 |
| **W3** | **AI 修复** `ai-batch-fix` | ✅ | ⚠️ | 模型下拉；`llmAvailable=false` 时 disabled |
| **W4** | **一键修复** `auto-fix` | ✅ | ❌ | `issuesBefore→issuesAfter` + 可选 `syncAfter` |
| **W5** | **merge-hint** 复制 Cursor 指令 | ✅ | ❌ | `dup_slug` / `dup_content` / `near_dup` |
| **W6** | kind 分类读 **options** | ✅ | ❌ | 以 `scriptFixableKinds` 为准，勿硬编码误分手改 |
| **W7** | 复检 + Sync 摘要 | ✅ | ❌ | `relintAfter` 或再 Lint；Sync 勾选 |
| **W8** | 禁止治理页批量 **enrich** | — | ✅ | 无 enrich 批量入口 |

**现 UI 约等于 W1 + W3 + 部分 W8**；W2/W4/W5/W6/W7 待补。对照 [ops §3.2](../ops/knowledge-workbench-operations.md#32-当前-web-页-vs-完整能力t16f-差距)。

### 13.2 回归场景（手工）

- [ ] `enterprise-kb` 或 `wiki-jp-exam` Lint → 勾选 → script → AI → auto 顺序  
- [ ] `missing_dates` 经 script-fix 补 frontmatter  
- [ ] `dup_slug` 仅 merge-hint / 编辑，不进 ai-batch-fix  
- [ ] `syncAfter=true` 后 DB 体检一致  

---

## 14. 代码落点（meiling-ui）

| 文件 | 职责 |
|------|------|
| `src/api/knowledge/kbWikiGovern.ts` | §7 六个 API |
| `src/types/knowledge/kbWikiGovern.ts` | §6 类型 + `isScriptFixable` 等 |
| `src/views/knowledge/wiki-govern/index.vue` | 页面容器 |
| `src/views/knowledge/wiki-govern/GovernLintPanel.vue` | W1 勾选逻辑 |
| `src/views/knowledge/wiki-govern/GovernFixPanel.vue` | W2–W4 按钮 + W7 Sync/摘要 |
| `src/components/knowledge/KbMergeHintDialog.vue`（建议） | W5 复制 `cursorPrompt` |

菜单：`docs/sql/11_kb_wiki_govern_menu.sql` · perm `kb:wiki:govern:list`。

---

## 15. 相关文件（后端 · 联调对照）

| 路径 | 说明 |
|------|------|
| `KbWikiController.java` | `/govern/script-fix`, `/ai-batch-fix`, `/auto-fix` |
| `KbWikiGovernServiceImpl.java` | 编排逻辑 |
| `KbWikiFrontmatterFixUtil.java` | 脚本修复规则 |
| `WikiGovernKindUtil.java` | kind 分类 |
| `kb/tools/lint.py` | issue.kind 定义 |

---

## 16. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-06-28 | §13 W1–W8 验收进度 + §14 落点；对齐 ops §3.2 |
| 2026-06-28 | 对齐总览；补 manualOnlyKinds、merge-hint 类型、代码落点 |
| 2026-06-27 | T16e：新增 script-fix / ai-batch-fix / auto-fix；治理页不再推荐 enrich 批量 |
