# 知识库 Ingest · 验收与手测清单

> 合并自原 `knowledge-ingest-{template-mode,express,category-slug,delete-job}.md`。  
> 契约：[KNOWLEDGE_API.md](../api/KNOWLEDGE_API.md) §9 · 矩阵：[knowledge-script-vs-llm-matrix.md](knowledge-script-vs-llm-matrix.md) · 操作：[knowledge-workbench-operations.md](../ops/knowledge-workbench-operations.md)

---

## 1. 模板模式（`useLlmGenerate=false`）

**P1**：题库 / 文档搬运等「raw 已是 markdown、不需 LLM 改写正文」场景。

### 1.1 何时使用

| 场景 | 推荐 |
|------|------|
| raw 已是完整 markdown，只需进 wiki + frontmatter | **模板模式** `useLlmGenerate=false` |
| 需要摘要、互链、改写、多源融合 | LLM 模式（默认 `true`） |
| Plan 也无需 LLM | 叠加 `useLlmPlan=false`（Express skeleton） |

### 1.2 API

| 接口 | 参数 | 默认 |
|------|------|------|
| `POST /kb/ingest/jobs/{id}/generate` | `useLlmGenerate` | `true` |
| `POST /kb/ingest/jobs/{id}/prepare` | `useLlmPlan`, `useLlmGenerate` | `false`, `true` |
| `POST /kb/ingest/jobs/express` | 同上 | `false`, `true` |
| `POST /kb/ingest/jobs/{id}/draft/regenerate` | `useLlmGenerate` | `true` |

Express 模板一键预览：

```
POST /kb/ingest/jobs/express?useLlmPlan=false&useLlmGenerate=false
Body: { spaceId, batchNo, topic, rawPaths: ["prd/foo.md"] }
```

响应 `prepare.generate.templateMode = true`。

### 1.3 生成规则

- **create**：`KbIngestTemplateWriter.buildCreatePage` — frontmatter + raw 全文（去 raw 自身 frontmatter）；**不**套用 `raw-snippet-chars`（4000）截断
- **enrich**：追加 `## {reason}` + raw 正文
- **不调用** `kb.llm`
- 可选上限：`kb.ingest.template-raw-max-chars`（默认 `0` = 不截断）

### 1.4 commit 与入库后

- 仍跑批次 lint（ERROR 阻塞）
- **raw 覆盖**：`sources` 中 raw 已被**其它** wiki 页引用 → commit 拒绝
- `nextSteps` 含 `wiki_govern_lint`、`kb_health_scan`

### 1.5 验收勾选

- [ ] `useLlmGenerate=false` 时无 LLM 调用，草稿含完整 frontmatter
- [ ] raw 正文进入 wiki（去 raw frontmatter）
- [ ] 重复 ingest 已 covered raw → commit 报错
- [ ] publish 后 `nextSteps` 含 Wiki 治理链接

---

## 2. Express 一键入库（T18）

前置：知识库服务已启动；账号对目标空间有 **editor**；Express Plan 不依赖 LLM。

### 2.1 UI 手测

**一键预览**

1. 打开 **知识库 → Ingest 工作台**
2. 选择空间 `wiki-jp-exam`（或含 `fe` 分类的空间）
3. 勾选 `raw/school/fe/fe_kamoku_b_set_sample_qs.md`
4. 填写主题，点击 **一键预览**
5. 期望：批次详情 `?express=1`；Plan create 含 `categoryId`（FE）、`slug=fe_kamoku_b_set_sample_qs`；至少 1 页草稿

**确认入库**

1. Express 横幅 **确认入库**，确认路径列表
2. 期望：`committed=true`；磁盘 `kb/wiki-jp-exam/fe/fe_kamoku_b_set_sample_qs.md`；批次 `committed`
3. 文档管理可浏览，`category_id` 对应 FE

**Lint 阻塞（负例）**

1. 手工改草稿引入断链或 frontmatter 错误
2. 再次 **确认入库**
3. 期望：`committed=false`；展示 lint ERROR；wiki 未覆盖

### 2.2 API 直调（可选）

鉴权：`POST .../login` 拿 `data.token`，请求头 `Authorization: <token>`。  
**rawPaths** 不含 `raw/` 前缀。开发机 `:8090` 或网关 `21000/KnowledgeServer`。

```bash
curl -X POST "http://127.0.0.1:8888/login" \
  -H "Content-Type: application/json" \
  -d '{"userName":"admin","password":"123456"}'

curl -X POST "http://127.0.0.1:8090/kb/ingest/jobs/express?useLlmPlan=false" \
  -H "Content-Type: application/json" \
  -H "Authorization: <token>" \
  -d '{"spaceId":"900000000000000002","topic":"FE 样题","rawPaths":["fe/fe_kamoku_b_set_sample_qs.md"]}'

curl -X POST "http://127.0.0.1:8090/kb/ingest/jobs/<JOB_ID>/publish?sync=true&approveAll=true" \
  -H "Authorization: <token>"
```

> **分类推断**：空间需存在 `dir_slug=fe` 的分类，Plan 才带 `categoryId` 并落盘 `wiki-jp-exam/fe/...`；否则 legacy `type=article` → `articles/`。

契约：KNOWLEDGE_API §9.6.6。

---

## 3. 分类 + slug 落盘（T17）

产品：[`Ingest工作台产品方案.md`](../../moli-knowledge/kb/wiki/guides/Ingest工作台产品方案.md) §3.1、§3.5。

### 3.1 前置

| 项 | 值 |
|----|-----|
| 空间 | `jp-fe-ap-exam`（`space_id=900000000000000002`） |
| wiki 目录 | `moli-knowledge/kb/wiki-jp-exam/` |
| 分类 | **FE 题库**，`dir_slug=fe`，`default_type=interview` |
| raw 源 | `raw/school/fe/fe_kamoku_b_set_sample_qs.md` |
| 权限 | 空间 editor + LLM 可用（或 skeleton Plan） |

### 3.2 用例

**用例 1 · 分类 + 默认 slug（主路径）**

1. 文档管理确认 `fe` 分类与 `wiki-jp-exam/fe/`
2. Ingest 新建批次，勾选 raw
3. Plan：分类选 FE，`slug=fe_kamoku_b_set_sample_qs` → 保存
4. 预览列：`wiki-jp-exam/fe/fe_kamoku_b_set_sample_qs.md`
5. 生成草稿 → 批准 → Lint 通过 → 落盘并 Sync
6. 验收：磁盘路径、`type=interview`、文档管理 `category_id`、Sync 后浏览树在 fe 下

**用例 2 · 改 slug**

Plan 改 `slug` → 保存 → 全量重新生成 → Commit 路径为 `fe/{新slug}.md`。

**用例 3 · legacy（无 categoryId）**

Plan `create[]` 仅 `{ type, slug, sources }` → 落盘 `articles/foo.md`。

**用例 4 · 非法 Plan**

| 操作 | 期望 |
|------|------|
| `categoryId` + `slug` 含 `/` | 4xx |
| 其它空间的 `categoryId` | 4xx |
| 分类无 `dir_slug` | 4xx |

**回归**：`enrich[]` 仍按已有 wiki 路径增补；不支持 enrich 改分类。

---

## 4. 删除批次（自动化测试）

> API：`DELETE /kb/ingest/jobs/{id}` · 契约：KNOWLEDGE_API §9.2.4

### 4.1 范围

| 层级 | 类 |
|------|-----|
| Service | `KbIngestServiceImplDeleteJobTest` |
| Controller | `KbIngestControllerDeleteJobApiTest` |

目录：`moli-knowledge/moli-knowledge-server/src/test/java/com/moli/knowledge/server/`

### 4.2 用例（7 条，2026-06-27 全通过）

**Service（6）**：ingest 关闭 / null id / 批次不存在 / 已删 / ACL 拒绝 / 软删成功。

**Controller（1）**：`DELETE` 返回 `MoliResult<Boolean>`，`code=200`。

### 4.3 运行

```bash
cd moli-knowledge/moli-knowledge-server
mvn test "-Dtest=KbIngestControllerDeleteJobApiTest,KbIngestServiceImplDeleteJobTest"
```

无需 MySQL/Redis（纯 Mock）。

### 4.4 手工联调

`DELETE /kb/ingest/jobs/{id}` — 列表不含该批次；曾 commit 的 wiki 文件不受影响。

对称接口：§9.6.3 删批次模板 · 表 `kb_ingest_job`（`08_kb_ingest_workbench.sql`）。
