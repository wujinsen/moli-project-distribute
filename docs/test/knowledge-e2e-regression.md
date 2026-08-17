# 知识库 · E2E 回归清单

> 合并 Ingest / Wiki Lint / 脚本矩阵的 **回归门禁**；细节见子文档。  
> 操作：[knowledge-workbench-operations.md](../ops/knowledge-workbench-operations.md)  
> 发布冒烟：[release-smoke-checklist.md](release-smoke-checklist.md) §4–§5

---

## 1. 自动化（CI / 发版前必跑）

```bash
cd moli-knowledge/moli-knowledge-server
mvn test
```

| 测试类 | 覆盖 |
|--------|------|
| `KbWikiLintServiceImplTest` + `KbWikiControllerLintApiTest` | lint-space · 见 [knowledge-wiki-lint-space.md](knowledge-wiki-lint-space.md) |
| `KbIngestServiceImplDeleteJobTest` + `KbIngestControllerDeleteJobApiTest` | 删批次 |
| `KbIngestServiceImplPlanPathTest` + `IngestPlanPathResolverTest` | Plan 路径 / categoryId |
| `KbRawCoverageServiceImplAssertTest` + `KbKnowledgeExceptionHandlerTest` | raw 覆盖门禁 |
| `IngestLlmGenerateModeUtilTest` + `KbIngestTemplateWriterTest` | 模板模式 |
| `KbWikiFrontmatterFixUtilTest` + `KbWikiMergeHintUtilTest` | 治理脚本 |
| `KbPlatformLlmConfigServiceImpl*` + `KbLlmConfigCipherTest` | T19 LLM 平台 |
| `KbSyncServiceImplWikiDirTest` | 两空间 wiki 目录映射 |
| `KbAttachment*Test` | 附件 |

**wiki 文件门禁**（仓库根或 kb 目录）：

```bash
python moli-knowledge/kb/tools/lint.py --strict
```

---

## 2. 手测 · 浏览与问答（P0）

| # | 步骤 | 期望 |
|---|------|------|
| B1 | `GET /kb/index?spaceId=900000000000000001` | groups + count |
| B2 | `GET /kb/page?slug=guides/...` | 正文 + links |
| B3 | `POST /kb/ask` `{question}` | answer + citations 或检索式降级 |
| B4 | Web 侧栏搜索 | 有结果 |

---

## 3. 手测 · Ingest（P0）

按 [knowledge-ingest-acceptance.md](knowledge-ingest-acceptance.md) 执行：

- [ ] §1 模板模式 `useLlmGenerate=false`
- [ ] §2 Express 一键预览 + publish
- [ ] §3 分类 + slug 落盘（T17）
- [ ] §4 删批次 API
- [ ] raw 覆盖 commit 拒绝（conflicts 结构化 body）

---

## 4. 手测 · Wiki 编辑与治理（P0/P1）

| # | 步骤 | 期望 |
|---|------|------|
| W1 | `GET/PUT /kb/wiki-moli/page` | 保存成功 |
| W2 | `POST /kb/wiki-moli/lint-space` | issues 列表 |
| W3 | `POST /kb/wiki-moli/govern/script-fix` | metadata 修复 |
| W4 | `POST /kb/wiki-moli/govern/ai-batch-fix` | 需 LLM |
| W5 | `POST /kb/wiki-moli/govern/auto-fix` | 部分 LLM |
| W6 | `POST /kb/wiki-moli/govern/merge-hint` | dup 提示 |

Web UI：T16f 部分能力未全 — 以 API 手测为准。矩阵：[knowledge-script-vs-llm-matrix.md](knowledge-script-vs-llm-matrix.md)

---

## 5. Sync 与两空间（P0）

```bash
cd moli-knowledge/kb
bash tools/ci/run_sync.sh dry-run-all
bash tools/ci/run_sync.sh sync-all
```

| 空间 | wiki 目录 | 抽检 |
|------|-----------|------|
| enterprise-kb | wiki/ | 浏览树有更新页 |
| moli-ops-manual | wiki-moli/ | 本地启动指南可开 |
| moli-ops-manual | wiki-moli/ | develop 分类下文档 |

---

## 6. 与发布冒烟的关系

| 文档 | 范围 |
|------|------|
| **release-smoke-checklist** | 全平台最小集（含知识库 §4–§5） |
| **本文** | 知识库深度回归（发版前 / 大改 kb 后） |

---

## 7. 失败排查

| 现象 | 查 |
|------|-----|
| lint-space exitCode≠0 | `kb/tools/lint.py` 本地复现 |
| commit 409 raw 冲突 | [ops §2.6](../ops/knowledge-workbench-operations.md) |
| Sync 后 Web 无页 | 是否 sync 错空间；`kb_sync_log` |
| LLM 相关失败 | 平台 LLM 设置 / `kb.llm.usable()` |

---

## 8. 子文档索引

| 文档 | 内容 |
|------|------|
| [knowledge-ingest-acceptance.md](knowledge-ingest-acceptance.md) | Ingest 分场景 |
| [knowledge-wiki-lint-space.md](knowledge-wiki-lint-space.md) | lint-space 单测 |
| [knowledge-script-vs-llm-matrix.md](knowledge-script-vs-llm-matrix.md) | 脚本 vs LLM |
