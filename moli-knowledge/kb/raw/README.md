# raw/ 语料目录（只读）

> **Agent 规则**：本目录及子目录 **只读**，Ingest 时读取、蒸馏到 `wiki/`，**绝不修改或删除** raw 文件。  
> 契约见 [`../AGENTS.md`](../AGENTS.md)。

## 放什么

| 子目录建议 | 内容 |
|------------|------|
| `raw/docs/` | 项目文档副本（README、ARCHITECTURE 等） |
| `raw/articles/` | 技术文章、笔记导出（`.note.md`） |
| `raw/interview/` | 面试题原始材料 |
| 任意层级 | 现有 `wujinsen_markdown/` 等历史语料 |

文件名建议 UTF-8；`.note.md` 为常见导出格式。

## 新增资料后怎么做（增量 Ingest）

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
   python kb/tools/sync_to_db.py --dry-run
   python kb/tools/sync_to_db.py
   ```
   详见 [[wiki同步指南]]（wiki 内）或 `kb/wiki/guides/增量ingest与raw投喂指南.md`。

## 不要做什么

- 不要直接在 `raw/` 里改 wiki 已沉淀的内容（改 wiki 源页）
- 不要 1:1 为每个 raw 文件建 wiki 页（按**主题簇**合并）
- 不要把密钥、生产密码写进 raw 再 ingest（Agent 会蒸馏，但源文件仍进 Git）

## 与 Query / crystallize

- **Query**：问已有 wiki，不必新增 raw。
- **crystallize**：多页综合答案 → `wiki/outputs/`，适合 onboarding、故障汇总等。

操作总览 [[知识库三操作]]（需先 sync 到 DB 后在 Web 端浏览）。
