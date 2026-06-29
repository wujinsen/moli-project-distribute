# raw/ 语料目录（只读）

> **Agent 规则**：本目录及子目录 **只读**，Ingest 时读取、蒸馏到 `wiki/`，**绝不修改或删除** raw 文件。  
> 契约见 [`../AGENTS.md`](../AGENTS.md)。

## 放什么

| 子目录 | 内容 | 成品见 |
|--------|------|--------|
| **`raw/prd/`** | 产品 PRD、产品方案 | `wiki/guides/` → [docs/product/](../../../docs/product/) |
| **`raw/design/`** | 技术方案、架构评审稿 | `wiki/concepts/`、`articles/` → [docs/design/](../../../docs/design/) |
| **`raw/api/`** | API 外部稿（可选） | `wiki/services/`；契约 → **[docs/api/](../../../docs/api/)** |
| **`raw/test/`** | 测试/QA 外部稿 | `wiki/guides/`；压测 → [load-test/](../../../load-test/) |
| **`raw/ops/`** | 运维 SOP | **`wiki-ops/`** → [docs/ops/](../../../docs/ops/) |
| **`raw/school/fe/`** | 日本語 FE 试题/答案 | **`wiki-jp-exam/`**（`jp-fe-ap-exam`） |
| **`raw/school/ap/`** | 日本語 AP 试题/答案 | **`wiki-jp-exam/`**（`jp-fe-ap-exam`） |
| `raw/docs/` | 项目文档副本 | ingest 后分散到 wiki 各类型 |
| `raw/articles/` | 技术文章、笔记导出 | `wiki/articles/` |
| `raw/interview/` | 面试题原始材料 | `wiki/interview/` |
| `wujinsen_markdown/` 等 | 历史语料 | 按主题簇 ingest |

总导航：[**docs/README.md**](../../../docs/README.md)

文件名建议 UTF-8；`.note.md` 为常见导出格式。

## 标准 Ingest 流程（Agent 厚 Ingest）

唯一路径：`raw/` → Agent 读源、写/补 `wiki/**` → `lint.py` → `sync_to_db.py` → MySQL。  
**不**把 raw 原样索引进 DB，也**不**用批量脚本绕过 wiki（见 `AGENTS.md`）。

1. **把文件放进 `raw/`**（保持目录结构即可，无需改 wiki）。
2. **对 Agent 说一句**（示例）：
   ```
   请对 kb/raw/xxx 做 ingest 批次#N，主题：Redis 集群，优先 concepts+articles，只改 wiki/
   ```
3. Agent 会：
   - 读 `AGENTS.md` + `wiki/index.md` 去重
   - 写/更新 `wiki/**` 页（frontmatter + `[[slug]]`）
   - append `log.md`、`graph/edges.jsonl`，更新 `index.md`
4. **可选体检**：`python kb/tools/serve.py` → 体检页，或 [[查询与体检指南]]
5. **同步 DB**（wiki 变更后）：
   ```bash
   python kb/tools/lint.py --strict
   bash kb/tools/ci/run_sync.sh dry-run-all
   bash kb/tools/ci/run_sync.sh sync-all
   ```
   详见 [[系统操作手册入口]]（Sync 映射）与 ops 空间 `guides/wiki同步指南`；或 `kb/wiki/guides/增量ingest与raw投喂指南.md`。

## 不要做什么

- 不要直接在 `raw/` 里改 wiki 已沉淀的内容（改 wiki 源页）
- 不要 1:1 为每个 raw 文件建 wiki 页（按**主题簇**合并）
- 不要把密钥、生产密码写进 raw 再 ingest（Agent 会蒸馏，但源文件仍进 Git）

## 与 Query / crystallize

- **Query**：问已有 wiki，不必新增 raw。
- **crystallize**：多页综合答案 → `wiki/outputs/`，适合 onboarding、故障汇总等。

操作总览 [[知识库三操作]]（需先 sync 到 DB 后在 Web 端浏览）。
