# raw/school · 日本語試験语料（jp-fe-ap-exam）

> 成品空间：`kb/wiki-jp-exam/` → sync `space_code=jp-fe-ap-exam`  
> **不要**在 `raw/` 下按 `space_code` 建目录；日本语试题统一放本目录。

## 子目录

| 路径 | 内容 | Ingest 目标 |
|------|------|-------------|
| **`school/fe/`** | 基本情報技術者（FE）真题、样题、答案 | `wiki-jp-exam/fe/` 等 |
| **`school/ap/`** | 応用情報技術者（AP）语料（暂无文件时先建目录） | `wiki-jp-exam/ap/` 等 |
| **`school/certify/`** | Certify（サーティファイ）IT 基础 Moodle 模拟题、中文解析、片假名词汇 | **`wiki-jp-exam/certify/`** |

### Certify 中文解析流水线（本地语料，不入 GitHub）

语料目录 `school/certify/` 与成品 `wiki-jp-exam/certify/` 已加入根目录 `.gitignore`，仅在本地维护。流水线见本地 wiki `Certifyサーティファイ` §中文题干维护。

## 命名建议

- 科目 B 样题：`fe_kamoku_b_set_sample_qs.md` / `_ans.md`
- 历年题：`{年}r{回}_fe_kamoku_{a|b}_{qs|ans}.md`
- 午前/午後：`fe_am` / `fe_pm`

## frontmatter sources 写法

```yaml
sources:
  - raw/school/fe/fe_kamoku_b_set_sample_qs.md
```

**禁止**在说明页写目录级 `sources: raw/school/fe/`（会触发「簇已引用」拦截重复 ingest）。

## 相关

- [`../README.md`](../README.md)
- [`../../wiki-jp-exam/index.md`](../../wiki-jp-exam/index.md)
