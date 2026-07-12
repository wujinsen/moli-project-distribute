# 知识库工作台 · 操作手册

> **按本文操作即可走完「知识入库」与「Wiki 治理」两块内容。**  
> 产品方案、API 字段、前端对接见 [§7 文档索引](#7-文档索引按角色)；本文只写**怎么点、怎么调、怎么验收**。

| 模块 | 菜单 / 路由 | Web 可用 | 本文章节 |
|------|-------------|----------|----------|
| **知识入库** | 知识库 → Ingest 工作台 · `knowledge/ingest/index` | ✅ | [§2](#2-模块一知识入库ingest) |
| **Wiki 治理** | 知识库 → Wiki 治理 · `knowledge/wiki-govern/index` | ⚠️ **部分**（Lint + AI）；缺 script/auto/merge-hint | [§3](#3-模块二wiki-治理) · [§3.2](#32-当前-web-页-vs-完整能力t16f-差距) |

---

## 目录

1. [共用前置](#1-共用前置)
2. [模块一：知识入库（Ingest）](#2-模块一知识入库ingest)
3. [模块二：Wiki 治理](#3-模块二wiki-治理)  
   - [3.2 当前 Web 页 vs 完整能力](#32-当前-web-页-vs-完整能力t16f-差距)
4. [串联：入库 → 治理 → Sync → 体检](#4-串联入库--治理--sync--体检)
5. [总验收清单](#5-总验收清单)
6. [两条链路对照](#6-两条链路对照)
7. [文档索引（按角色）](#7-文档索引按角色)

---

## 1. 共用前置

### 1.1 启动与环境

| 项 | 说明 |
|----|------|
| 启动顺序 | 见 [`kb/wiki-moli/guides/本地启动指南.md`](../../moli-knowledge/kb/wiki-moli/guides/本地启动指南.md) |
| 必起服务 | MySQL、Redis、Nacos、用户中心、**网关 `:21000`**、`moli-knowledge-server` **`:8090`** |
| 网关 Base | `http://127.0.0.1:21000/KnowledgeServer` |
| Swagger | `http://127.0.0.1:8090/swagger-ui.html`（Wiki 治理现期推荐） |

### 1.2 登录与权限

1. 浏览器登录 meiling-ui，或 `POST /UserCenter/login` 拿 token  
2. 后续请求头：`Authorization: login_token_xxx`  
3. 写操作需目标空间 **editor**（或平台超管）

### 1.3 默认演示空间

| 字段 | 值 |
|------|-----|
| spaceId | `900000000000000001` |
| spaceCode | `enterprise-kb` |
| wiki 磁盘 | `moli-knowledge/kb/wiki/` |
| raw 磁盘 | `moli-knowledge/kb/raw/`（只读投喂） |

> 茉莉项目手册空间为 `moli-ops-manual` · 磁盘 `kb/wiki-moli/`；Ingest 治理演示常用 `enterprise-kb`。

### 1.4 依赖对照（按能力）

| 你要做的 | 是否需要 LLM | 是否需要 Python |
|----------|--------------|-----------------|
| Ingest **模板入库** | ❌ | ❌ |
| Ingest **LLM 改写/Plan** | ✅ `kb.llm` | ❌ |
| 治理 **脚本修复** | ❌ | ✅ 服务端跑 `lint.py` |
| 治理 **AI 修复** | ✅ | ✅ |
| **Sync / 健康体检** | ❌ | ❌ |

### 1.5 三个入口别混（必读）

| 入口 | 扫什么 | 何时用 |
|------|--------|--------|
| **Ingest** | raw → **写 wiki 文件** | 新内容进库 |
| **Wiki 治理 `lint-space`** | **磁盘 wiki 文件** | 修 metadata、断链、孤儿（Sync 前后都应做） |
| **健康体检 `GET /kb/lint`** | **MySQL 已 Sync 文档** | Sync 后验收、工单跟踪 |

### 1.6 演练用测试 raw（可选）

```bash
mkdir -p moli-knowledge/kb/raw/test-walkthrough
```

创建 `moli-knowledge/kb/raw/test-walkthrough/demo-note.md`：

```markdown
# 入库演练笔记

用于走通 Ingest → 治理 → Sync 的测试正文。

- 要点 A
- 要点 B
```

下文示例路径：`test-walkthrough/demo-note.md`（**不含** `raw/` 前缀）。

---

## 2. 模块一：知识入库（Ingest）

### 2.1 做什么

把 `kb/raw/` 里的材料变成 `kb/wiki-moli/` 下的 markdown 页，并更新 `index.md`、`log.md`、`edges.jsonl`。

```
raw 勾选 → Plan → 生成草稿 → diff 审阅 → lint → commit 落盘 → (Sync)
```

### 2.2 模式怎么选

| 场景 | 模式 | 关键参数 |
|------|------|----------|
| 首次演练 / raw 已是 md | **Express + 模板** | `useLlmPlan=false`，`useLlmGenerate=false` |
| 快速 1 raw → 1 页 | **Express + LLM** | 默认 |
| 多页规划、改 slug/分类 | **Expert 六步** | 逐步 Plan / approve |
| 题库、文档搬运 | **模板入库** | 不调 LLM，见 [`knowledge-ingest-acceptance.md`](../test/knowledge-ingest-acceptance.md) §1 |

### 2.3 操作 · Web Express（推荐）

**入口**：meiling-ui → **知识库 → Ingest 工作台**

| 步 | 操作 | 预期 |
|----|------|------|
| 1 | 空间选 **企业知识库** | — |
| 2 | raw 树勾选 `test-walkthrough/demo-note.md` | 左侧可见文件 |
| 3 | 勾选 **「模板入库（不调 LLM）」**（首次建议） | — |
| 4 | 点 **「一键预览」** | 创建批次 + Plan + 草稿 |
| 5 | 进入批次详情，看 **Plan 表** | 1 条 `create`，slug 如 `demo-note` |
| 6 | 打开 **草稿 diff** | 右侧 = frontmatter + raw 正文 |
| 7 | 点 **「确认入库」** | 等价 `publish?sync=true&approveAll=true` |
| 8 | 看 **nextSteps** | 提示去 Wiki 治理 Lint |

**Expert 六步**（需细调 Plan 时）：

```
① 选 raw → ② 生成/编辑 Plan → ③ 生成草稿 → ④ 逐页 diff/approve
→ ⑤ lint 预检 → ⑥ commit（可选 Sync）
```

### 2.4 操作 · API / curl

`$TOKEN` = 登录 token；`$BASE` = `http://127.0.0.1:21000/KnowledgeServer`

**① 一键预览（Express 模板）**

```bash
curl -X POST "$BASE/kb/ingest/jobs/express?useLlmPlan=false&useLlmGenerate=false" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "spaceId": 900000000000000001,
    "batchNo": "ops-walkthrough-001",
    "topic": "入库演练",
    "rawPaths": ["test-walkthrough/demo-note.md"]
  }'
```

记下 `data.job.id` → `{jobId}`

**② 查看草稿**

```bash
curl "$BASE/kb/ingest/jobs/{jobId}/drafts" -H "Authorization: $TOKEN"
```

**③ 确认入库**

```bash
curl -X POST "$BASE/kb/ingest/jobs/{jobId}/publish?sync=true&approveAll=true" \
  -H "Authorization: $TOKEN"
```

**④ 磁盘验收**

```bash
# 仓库根
dir moli-knowledge\kb\wiki\guides\demo-note.md
git diff --stat moli-knowledge/kb/wiki-moli/
```

成功标志：`data.commit.files[]` 有路径；`data.nextSteps[]` 含 `wiki_govern_lint`。

### 2.5 入库常见问题

| 现象 | 处理 |
|------|------|
| commit 报 raw 已被引用 | raw 覆盖门禁：改 Plan 为 `enrich` 同一 slug，或换 raw |
| 生成失败 / 很慢 | 无 LLM → 勾选模板模式 `useLlmGenerate=false` |
| 落盘目录不对 | Expert 模式编辑 Plan 的 `categoryId` / slug 后重新 generate |
| lint ERROR 阻塞 commit | 改草稿断链/frontmatter 后再 publish |
| raw **簇已引用** / commit 被拒 | 说明页 `sources` 写了目录（如 `kb/raw/school/fe/`）→ 改 Plan 为 **enrich** 已有 slug，或收窄 sources；见 [§2.6](#26-raw-覆盖与簇已引用) |

### 2.6 raw 覆盖与「簇已引用」

Ingest 列表里 raw 可能显示：

| UI 标签 | 含义 |
|---------|------|
| **已 ingest** | 该 raw 文件已被某 wiki 页 `sources` **精确引用** |
| **簇已引用** | 被某页 **目录级** sources 覆盖（如 `sources: kb/raw/school/fe/`） |

**commit 时**：若 raw 已被引用，且本批是 **create 新 slug**（非 enrich 同一 slug）→ 拒绝。响应 `code=10012`，`data.errorKind=INGEST_RAW_ALREADY_COVERED`，`data.conflicts[]` 含 `path` / `wikiSlugs` / `coverage` / `matchKind`（详见 [ingest-workbench-frontend §6](../api/ingest-workbench-frontend.md#6-raw-覆盖门禁commit-错误处理)）。典型 `msg`：

> raw 已被 wiki 引用… → wiki [guides/日本語試験知识库说明]。请 enrich 或更换 raw 源。

| 你想做的 | 做法 |
|----------|------|
| 把 qs2 并进已有页 | Expert Plan → **enrich** 指向已有 slug |
| 新建独立页 `fe/xxx.md` | 先改说明页 sources，去掉 `kb/raw/school/fe/` 整目录占位 |
| 换路径 | raw 放到未被引用的目录（不推荐，不如 enrich） |

**绿色「已使用模板入库」只表示草稿生成成功**；若随后 commit 报错，**磁盘不会有新文件**（勿在 `wiki-jp-exam/fe/` 等路径空等）。

### 2.7 T20 双入口导入（规划 · Editor 浏览器）

> 设计：[`docs/design/kb-import-entry-design.md`](../design/kb-import-entry-design.md) · PRD：[`knowledge-import-entry-prd.md`](../product/knowledge-import-entry-prd.md)

**Editor 不需要 SSH**。服务已部署在 Linux 上时，上传 = 浏览器 → 网关 → `knowledge-server` 写**同机** `kb/raw` 或 `kb/wiki*`。

| 入口 | Tab | Editor 操作 | T20 状态 |
|------|-----|-------------|----------|
| raw 投喂 | Tab1 | 选 prefix · 上传 md → 自动跳 Tab2 Ingest | 🔵 待实现 |
| 选源入库 | Tab2 | 勾选 raw → Express/Expert → commit → Sync | ✅ 已有 |
| 成品 wiki | Tab3 | 选空间+分类 · 上传 md → 默认 Sync | 🔵 待实现 |

**T20 上线前**：Editor 只能 Tab2（且 raw 须已在服务器磁盘，常来自 Git 部署）或 Wiki 编辑单篇。**运维** bulk 语料可用 rsync/git 直写磁盘，不属于 Editor 常规 SOP。

---

## 3. 模块二：Wiki 治理

### 3.1 做什么

对**已有** wiki 文件做空间级体检与批量修复：

```
选空间 → lint-space → 勾选 issues → script-fix / ai-batch-fix / auto-fix → (Sync)
```

**禁止**：在治理页批量 `enrich`（会 append 章节，不能修 metadata/断链）。enrich 仅用于 T14 单页编辑或 Ingest Plan。

### 3.2 当前 Web 页 vs 完整能力（T16f 差距）

> **结论**：后端 **T16a/e/g 已齐**；meiling-ui 当前多为 **「① Lint + ② AI 批量修复」两步 MVP**，与产品五步链路不一致。  
> 完整按钮清单见 [`wiki-govern-frontend.md` §8](../api/wiki-govern-frontend.md#8-按钮与调用顺序)。

#### 步骤对照（产品 vs 现 UI vs 你怎么补）

| 产品步骤 | 后端 API | 现 Web 页 | 缺口补法 |
|----------|----------|-----------|----------|
| ① 文件 Lint | `POST /kb/wiki-moli/lint-space` | ✅ 一般有「开始 Lint」 | — |
| ②a **脚本修复**（metadata） | `POST /kb/wiki-moli/govern/script-fix` | ❌ **无按钮** | Swagger / §3.4 curl **③** |
| ②b AI 批量修复 | `POST /kb/wiki-moli/govern/ai-batch-fix` | ✅「开始批量 AI 修复」 | — |
| ②c **一键修复**（脚本→AI→再 Lint） | `POST /kb/wiki-moli/govern/auto-fix` | ❌ **无按钮** | Swagger / §3.4 curl **④** |
| ③ dup 合并提示 | `POST /kb/wiki-moli/govern/merge-hint` | ❌ 多仅「编辑」链接 | Swagger；或单页编辑 |
| ④ **再 Lint / before→after** | auto-fix 内 `relintAfter` 或再调 lint-space | ❌ 无独立复检区 | auto-fix 或手动再 Lint |
| ⑤ **Sync** | `syncAfter` 或 `POST /kb/sync/trigger` | ❌ 常无勾选 | 健康体检 · Wiki 同步 Tab 或 §3.4 **⑥** |

**现 UI 常见形态**（你看到的「只有 2 步」）：

```
① Lint 结果 + 勾选
② 批量修复（仅 AI 修复 Tab +「需手改 N 条」）
   （缺少：脚本修复 / 一键修复 / merge-hint / Sync / 复检摘要）
```

#### issue.kind 归类（前端易错）

以 `GET /kb/wiki-moli/govern/options` 为准：

| kind | 后端分类 | 现 UI 常见误标 | 正确操作 |
|------|----------|----------------|----------|
| `missing_dates`, `slug_mismatch`, `missing_source` | **脚本** | 有时并入「需手改」 | **script-fix** |
| `broken_link`, `orphan`, `dup_content`… | **AI** | ✅ AI 修复 | ai-batch-fix |
| `dup_slug` | **仅人工** | ✅ 手改 | merge-hint + 编辑页 |
| `slug_mismatch` | **脚本**（不是手改） | ❌ 与 dup_slug 一并提示手改 | **script-fix** |

#### UI 未接全时的推荐闭环（Swagger）

```
lint-space → script-fix → ai-batch-fix（或 auto-fix 一次做完前两者+relint）
→ 手改 dup_slug → 再 lint-space → sync/trigger → 健康体检
```

日本語試験空间示例：`spaceId=900000000000000002`，wiki 目录 `wiki-jp-exam/`。

#### 发给前端（T16f 待办）

1. 增加 **脚本修复**、**一键修复已选** 按钮  
2. 增加 **复制合并指令**（merge-hint）  
3. 修复后 **Sync 勾选** + `issuesBefore → issuesAfter` 摘要  
4. **`slug_mismatch` 进 script-fixable**，不要与 `dup_slug` 同列「需手改」  
5. 对照验收：[`wiki-govern-frontend.md` §13](../api/wiki-govern-frontend.md#13-验收清单前端自测)

### 3.3 操作 · Web（完整版 · T16f 目标）

**入口**：meiling-ui → **知识库 → Wiki 治理**

| 步 | 操作 | 预期 |
|----|------|------|
| 1 | 选 **企业知识库** | — |
| 2 | **开始 Lint** | `issues[]` 按 kind 分组 |
| 3 | 默认勾选 script + AI 可修项 | **不勾选** `dup_slug` |
| 4 | **脚本修复** | `missing_dates` 等秒级修复 |
| 5 | **AI 修复** 或 **一键修复已选** | 问题数下降（需 LLM） |
| 6 | 勾选 **修复后 Sync** 或手动 Sync | wiki → DB |
| 7 | `dup_slug` → **复制合并指令** | `merge-hint` → Cursor 手改 |

按钮与 API 对应：[`docs/api/wiki-govern-frontend.md`](../api/wiki-govern-frontend.md)

> 若页上只有 AI 修复，按 [§3.2](#32-当前-web-页-vs-完整能力t16f-差距) 用 Swagger 补全。

### 3.4 操作 · Swagger / curl（**UI 未接全时推荐**）

Swagger → `知识库 Wiki` / `Wiki 治理` 相关接口。

**① Lint（文件真值）**

```bash
curl -X POST "$BASE/kb/wiki-moli/lint-space" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"spaceId": 900000000000000001, "strict": false}'
```

- `code=200` 且 `exitCode≠0` **正常**（表示有问题）  
- 保存 `data.issues[]`，`page` = 要修的 slug

**② 查 kind 分类（可选）**

```bash
curl "$BASE/kb/wiki-moli/govern/options" -H "Authorization: $TOKEN"
```

| kind 类型 | 示例 | 修复 API |
|-----------|------|----------|
| 脚本 | `missing_dates`, `slug_mismatch`, `missing_source` | `script-fix` |
| AI | `broken_link`, `orphan`, `dup_content`… | `ai-batch-fix` |
| 仅人工 | `dup_slug` | `merge-hint` + 手改 |

**③ 脚本修复**

```bash
curl -X POST "$BASE/kb/wiki-moli/govern/script-fix" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "spaceId": 900000000000000001,
    "issues": [{"page": "guides/demo-note", "kind": "missing_dates", "level": "warn"}],
    "dryRun": false
  }'
```

`dryRun: true` 仅预览不写盘。

**④ 一键修复（推荐）**

```bash
curl -X POST "$BASE/kb/wiki-moli/govern/auto-fix" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "spaceId": 900000000000000001,
    "issues": [ /* 粘贴 ① 中勾选的全部 issues */ ],
    "scriptFix": true,
    "aiFix": true,
    "relintAfter": true,
    "syncAfter": false
  }'
```

无 LLM 时设 `"aiFix": false`。

**⑤ 再 Lint** — 重复 ①，对比 issues 数量。

**⑥ Sync**

```bash
curl -X POST "$BASE/kb/sync/trigger?spaceId=900000000000000001" \
  -H "Authorization: $TOKEN"
```

### 3.5 操作 · 命令行（本地预检）

与 `lint-space` 扫同一套 wiki 文件：

```bash
cd moli-knowledge
python kb/tools/lint.py --wiki-dir wiki --strict
```

改 md 后重复；CI 门禁同款命令。

### 3.6 治理常见问题

| 现象 | 处理 |
|------|------|
| lint-space 500 / 脚本失败 | 检查本机 `python` 与 `kb/tools/lint.py` 路径配置 |
| AI 修复不可用 | 配置 `kb.llm`；或仅 `scriptFix` |
| 治理后 Web 浏览仍旧 | 未 Sync → 执行 `POST /kb/sync/trigger` |
| 健康体检与治理结果不一致 | 正常：体检扫 DB，治理扫磁盘；先 Sync 再体检 |
| **页上只有 2 步 / 无脚本修复** | 前端 T16f 未接全；见 [§3.2](#32-当前-web-页-vs-完整能力t16f-差距) |
| `slug_mismatch` 被提示手改 | 应走 **script-fix**，非手改 |

---

## 4. 串联：入库 → 治理 → Sync → 体检

**标准顺序**（不要跳步）：

```
§2 入库 commit 成功
    ↓ nextSteps 提示
§3 lint-space → 修复 → 再 Lint
    ↓
POST /kb/sync/trigger
    ↓
健康体检 → 扫描并落库（DB 验收）
```

| 步 | Web | API |
|----|-----|-----|
| 入库 | Ingest → 确认入库 | §2.4 ③ |
| 治理 | Wiki 治理（或 Swagger） | §3.4 ①–⑥ |
| Sync | 健康体检页 · Wiki 同步 Tab | `POST /kb/sync/trigger` |
| DB 体检 | 健康体检 → 扫描并落库 | `POST /kb/lint/scan` |

**Sync 失败**（exitCode≠0、webhook 告警、CI 红灯）：见 **[kb-sync-failure-runbook.md](kb-sync-failure-runbook.md)**（查 `kb_sync_log`、重跑 `sync-all`、verify-all）。

---

## 5. 总验收清单

复制打勾即完成一轮：

**前置**
- [ ] 网关 + knowledge-server 已启动
- [ ] 已登录，对 `enterprise-kb` 有 editor

**模块一 · 入库**
- [ ] 准备 raw 或使用已有 raw
- [ ] Express 模板预览 → 确认入库
- [ ] 磁盘存在 `kb/wiki-moli/.../xxx.md`
- [ ] 响应含 `nextSteps`

**模块二 · 治理**
- [ ] `lint-space` 返回 issues 列表
- [ ] **script-fix** 或 **auto-fix** 后 issues 减少（UI 无按钮时用 Swagger）
- [ ] `dup_slug` 未误走批量 enrich；`slug_mismatch` 走脚本而非手改

**收尾**
- [ ] `sync/trigger` 成功
- [ ] 健康体检 / `GET /kb/lint` 与文件一致
- [ ] （可选）`git diff` 检查 index/log/edges

**P0 · Sync 失败 UI（O4 / P0-O4）** — 环境无历史 fail 时必做点验  
见 **[kb-sync-failure-runbook.md §9](kb-sync-failure-runbook.md#9-p0-o4-点验故意制造失败仅显示失败筛选)**：

- [ ] 在 `wiki/_p0o4-fail-test/` 放未分类测试页 → 触发 Sync 失败
- [ ] 健康体检日志表：fail 行着色 + 展开 message + Toast
- [ ] 勾选 **「仅显示失败」** 仅见 fail 行
- [ ] 删除测试目录后重跑 Sync 成功
- [ ] （可选）`meiling-ui` 执行 `npm run kb:prd-acceptance` → P0-O4 通过

---

## 6. 两条链路对照

| | 知识入库 | Wiki 治理 |
|--|----------|-----------|
| **输入** | `kb/raw/` | 已有 `kb/wiki*` |
| **输出** | wiki 文件 + index/log/edges | 修质量项 |
| **典型 LLM** | 生成正文（可关） | 仅 AI 修复 |
| **Lint** | 批次 lint（commit 前） | `lint-space`（文件真值） |
| **Web** | ✅ | ⚠️ 部分（见 §3.2） |

---

## 7. 文档索引（按角色）

| 角色 | 文档 |
|------|------|
| **操作（本文）** | 本文件 |
| **Sync 失败 Runbook** | [`docs/ops/kb-sync-failure-runbook.md`](kb-sync-failure-runbook.md) |
| 产品 / 决策 | [`docs/product/knowledge-workbench-requirements.md`](../product/knowledge-workbench-requirements.md) |
| Ingest 产品方案 | [`kb/wiki-moli/develop/Ingest工作台产品方案.md`](../../moli-knowledge/kb/wiki-moli/develop/Ingest工作台产品方案.md) |
| Wiki 治理产品方案 | [`kb/wiki-moli/develop/Wiki治理工作台产品方案.md`](../../moli-knowledge/kb/wiki-moli/develop/Wiki治理工作台产品方案.md) |
| Agent 投喂 raw | [`kb/wiki-moli/develop/增量ingest与raw投喂指南.md`](../../moli-knowledge/kb/wiki-moli/develop/增量ingest与raw投喂指南.md) |
| HTTP 契约 | [`docs/api/KNOWLEDGE_API.md`](../api/KNOWLEDGE_API.md) §8–9 |
| 前端对接 | [`docs/api/knowledge-workbench-frontend.md`](../api/knowledge-workbench-frontend.md) |
| Lint / Sync / 体检 | [`kb/wiki-moli/guides/查询与体检指南.md`](../../moli-knowledge/kb/wiki-moli/guides/查询与体检指南.md) |
| Ingest 验收 | [`docs/test/knowledge-ingest-acceptance.md`](../test/knowledge-ingest-acceptance.md) |
| 治理 API 测试 | [`docs/test/knowledge-wiki-lint-space.md`](../test/knowledge-wiki-lint-space.md) |
| 治理前端对接 | [`docs/api/wiki-govern-frontend.md`](../api/wiki-govern-frontend.md) |
| **T22 插图回迁** | [`docs/test/knowledge-t22-image-remediation.md`](../test/knowledge-t22-image-remediation.md) · `kb/tools/WUJINSEN_*` |

---

## 8. T22 · wujinsen 插图回迁（运维）

> PRD：`docs/product/wujinsen-wiki-image-remediation-prd.md` · **生产上线**：[`deploy/上线流程.md`](../../deploy/上线流程.md) §4.3

| 步骤 | 命令 / 动作 |
|------|-------------|
| 验收报告 | `python3 kb/tools/verify_wujinsen_images.py --report` → `WUJINSEN_R3_REPORT.md` |
| manifest | `kb/tools/WUJINSEN_IMAGE_REMEDIATION.json`（397/397 done） |
| Sync | `bash kb/tools/ci/run_sync.sh sync-all`（需 pymysql；**不**上传 png） |
| **生产 raw 图包** | 开发机 `pack_raw_assets.py` → EC2 上传 `raw-asset-bundle.tar.gz` + `deploy_raw_assets.sh` |
| 部署 | knowledge-server（Asset API）+ meiling-ui（`KbMarkdownImage`）+ `KB_RAW_ROOT` |

**勿**上传整包 `wujinsen_markdown`；最小包约 **12 MiB / 212 png**。annex 图在 `kb/wiki/**/.assets/`。

Web 抽检：[`docs/test/knowledge-t22-image-remediation.md`](../test/knowledge-t22-image-remediation.md) §4 · 冒烟 S6：[`deploy/上线流程.md`](../../deploy/上线流程.md) §6。

---

## 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-12 | §9 P0-O4 点验；§5 总验收增加 O4 勾选项 |
| 2026-07-06 | §2.7 T20：Editor 浏览器上传、SSH 仅运维兜底 |
| 2026-07-06 | §8 对齐生产：raw-asset-bundle + deploy 脚本；Sync 与插图分包 |
| 2026-07-05 | §8 T22 插图回迁运维；§1.3 修正 enterprise-kb 磁盘路径 |
| 2026-06-28 | §3.2 Web vs 完整能力差距；§2.6 raw 簇已引用 |
| 2026-06-28 | 整合入库 + 治理为统一操作手册 |
