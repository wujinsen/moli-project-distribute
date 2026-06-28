# 知识库 Ingest · 分类 + slug 落盘（T17 手测）

> 契约：[`docs/api/KNOWLEDGE_API.md`](../api/KNOWLEDGE_API.md) §9.3（Plan v2）、§9.5.2（commit 路径）。  
> 产品：[`moli-knowledge/kb/wiki/guides/Ingest工作台产品方案.md`](../../moli-knowledge/kb/wiki/guides/Ingest工作台产品方案.md) §3.1、§3.5。

## 前置

| 项 | 值 |
|----|-----|
| 空间 | `jp-fe-ap-exam`（`space_id=900000000000000002`） |
| wiki 目录 | `moli-knowledge/kb/wiki-jp-exam/` |
| 分类 | 文档管理新建：**FE 题库**，`dir_slug=fe`，`default_type=interview` |
| raw 源 | `raw/fe/fe_kamoku_b_set_sample_qs.md` |
| 权限 | 空间 editor + LLM 可用（或 skeleton Plan 手填） |

## 用例 1 · 分类 + 默认 slug（主路径）

1. **文档管理** 确认存在 `fe` 分类且磁盘有 `wiki-jp-exam/fe/`（或 `.gitkeep`）。
2. **Ingest 工作台** 新建批次：空间 `jp-fe-ap-exam`，勾选上述 raw。
3. **① Plan**：生成或 skeleton → 可视化表中 **分类选 FE 题库 (fe)**，`slug` 保持 `fe_kamoku_b_set_sample_qs` → **保存 Plan**。
4. 预览列应显示：`wiki-jp-exam/fe/fe_kamoku_b_set_sample_qs.md`。
5. **② 生成草稿** → 逐页 **批准**。
6. **③ Lint 预检** 通过；**落盘预览** 列出同一路径。
7. **落盘并 Sync**；确认对话框含路径列表。
8. **验收**：
   - 磁盘：`kb/wiki-jp-exam/fe/fe_kamoku_b_set_sample_qs.md`
   - frontmatter `type` 为 `interview`（分类 defaultType）
   - 文档管理：该文档 `category_id` = FE 分类
   - `sync_to_db.py` / Web Sync 后浏览树分组在 fe 下

## 用例 2 · 改 slug 不重生成 Plan 全文

1. 在 Plan 表将 `slug` 改为 `fe_kamoku_b_sample_v2` → 保存 Plan。
2. **全量重新生成草稿**（会清空旧草稿）。
3. Commit 后路径为 `wiki-jp-exam/fe/fe_kamoku_b_sample_v2.md`。

## 用例 3 · legacy（无 categoryId）

1. Plan 高级 JSON：`create[]` 仅 `{ "type": "article", "slug": "foo", "sources": [...] }`。
2. 落盘：`wiki-jp-exam/articles/foo.md`（或 enterprise 空间 `wiki/articles/foo.md`）。

## 用例 4 · 非法 Plan 拒绝

| 操作 | 期望 |
|------|------|
| `categoryId` + `slug` 含 `/` | 保存 Plan 4xx |
| 其它空间的 `categoryId` | 保存 Plan 4xx |
| 分类无 `dir_slug` | 保存 Plan 4xx |

## 回归 · enrich 不变

- `enrich[]` 仍按已有 wiki 页路径增补；本迭代 **不支持** enrich 改分类。

## 相关任务

- T17a 后端路径 · T17c Plan 可视化表 · **T17d** 落盘预览 + 本文档
