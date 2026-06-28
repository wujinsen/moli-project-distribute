# Ingest 模板模式（useLlmGenerate=false）

> **P1**：题库 / 文档搬运等「raw 已是 markdown、不需 LLM 改写正文」场景。  
> 契约：[KNOWLEDGE_API.md](../api/KNOWLEDGE_API.md) §9 · 矩阵：[knowledge-script-vs-llm-matrix.md](knowledge-script-vs-llm-matrix.md)

---

## 1. 何时使用

| 场景 | 推荐 |
|------|------|
| raw 已是完整 markdown，只需进 wiki + frontmatter | **模板模式** `useLlmGenerate=false` |
| 需要摘要、互链、改写、多源融合 | LLM 模式（默认 `true`） |
| Plan 也无需 LLM | 叠加 `useLlmPlan=false`（Express skeleton） |

---

## 2. API

| 接口 | 参数 | 默认 |
|------|------|------|
| `POST /kb/ingest/jobs/{id}/generate` | `useLlmGenerate` | `true` |
| `POST /kb/ingest/jobs/{id}/prepare` | `useLlmPlan`, `useLlmGenerate` | `false`, `true` |
| `POST /kb/ingest/jobs/express` | 同上 | `false`, `true` |
| `POST /kb/ingest/jobs/{id}/draft/regenerate` | `useLlmGenerate` | `true` |

### 示例：Express 模板一键预览

```
POST /kb/ingest/jobs/express?useLlmPlan=false&useLlmGenerate=false
Body: { spaceId, batchNo, topic, rawPaths: ["prd/foo.md"] }
```

响应 `prepare.generate.templateMode = true`。

---

## 3. 生成规则

- **create**：`KbIngestTemplateWriter.buildCreatePage`
  - frontmatter：`title/slug/type/sources/related/created/updated`
  - 正文：raw 文件去掉自身 frontmatter 后的内容
- **enrich**：追加 `## {reason}` + raw 正文（模板 patch）
- **不调用** `kb.llm`（无需 api-key）

---

## 4. commit 门禁（P1）

- 仍跑批次 **lint**（ERROR 阻塞）
- **raw 覆盖**：若 `sources` 中 raw 已被**其它** wiki 页引用 → commit 拒绝  
  （本批 enrich 同一 slug 除外）

---

## 5. 入库后引导

`commit.nextSteps` / `publish.nextSteps` 含：

| key | 跳转 |
|-----|------|
| `wiki_govern_lint` | Wiki 治理 |
| `kb_health_scan` | 健康体检 |

---

## 6. 前端建议

- Express 页增加 **「模板入库（不调 LLM）」** 勾选 → `useLlmGenerate=false`
- 与「Express Plan（skeleton）」勾选独立
- LLM 不可用时自动降级模板模式并提示

---

## 7. 验收

- [ ] `useLlmGenerate=false` 时无 LLM 调用，草稿含完整 frontmatter
- [ ] raw 正文进入 wiki 正文（去 raw frontmatter）
- [ ] 重复 ingest 已 covered raw → commit 报错
- [ ] publish 后 `nextSteps` 含 Wiki 治理链接
