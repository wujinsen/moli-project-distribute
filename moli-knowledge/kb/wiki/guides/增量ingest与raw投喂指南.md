---
title: 增量 Ingest 与 raw 投喂指南
slug: 增量ingest与raw投喂指南
type: guide
status: active
tags: [知识库, ingest, raw, P0]
sources:
  - moli-knowledge/kb/AGENTS.md
  - moli-knowledge/kb/raw/README.md
related: [知识库三操作, 系统操作手册入口, AI自我进化与MD审校流程]
created: 2026-06-22
updated: 2026-06-25
---

# 增量 Ingest 与 raw 投喂指南

主题簇首轮 ingest 完成后，**日常增量**按本文操作。契约 [[知识库三操作]]；raw 目录说明见 `kb/raw/README.md`。

## 1. 什么时候走增量 ingest

| 场景 | 做法 |
|------|------|
| 新笔记/文章导出 | 放入 `kb/raw/`，触发 ingest |
| 项目代码/文档更新 | 复制到 `raw/docs/` 或让 Agent 读仓库路径作 sources |
| 仅问已有知识 | **Query**，不必 ingest |
| 多页综合 onboarding/故障汇总 | **crystallize** → `wiki/outputs/` |

## 2. 投喂 raw（人类）

```text
kb/raw/
  README.md          ← 本流程说明
  wujinsen_markdown/ ← 历史语料（示例）
  docs/              ← 可选：项目文档副本
```

规则：**只追加/新增文件**，不编辑 Agent 已依赖的 raw 路径除非有意替换语料。

## 3. 触发 Agent ingest（一句话模板）

```
请对 kb/raw/{路径或主题} 做 ingest 批次#{N}：
- 主题：{如 Redis 哨兵}
- 产出：concepts 枢纽 + articles 1 篇 + interview 可选
- 只改 wiki/**，更新 index/log/edges
- 与已有 [[redis-缓存]] 合并，勿重复 slug
```

Agent 必读：`kb/AGENTS.md`、`wiki/index.md`。

## 4. 批次交付物（检查清单）

-  新页带 YAML frontmatter（`title/slug/type/sources/related`）
-  正文 `[[slug]]` 交叉链接
-  `wiki/index.md` 目录条目
-  `wiki/log.md` append 一行 `ingest | 批次#N ...`
-  `wiki/graph/edges.jsonl` append 关系边
-  相关旧页 **enrich** 反向链接（避免孤儿）
-  **同主题新版本** raw 再 ingest → 按 `AGENTS.md` **§4.1**（默认 enrich，conflicts 等人确认）

## 5. 批次规模建议

| 类型 | 建议 |
|------|------|
| 新主题簇 | 5–8 页/批 |
| 单点补充 | 1–2 页 + enrich |
| 大目录 raw（如 BigData） | 按簇跳过或 ⏭，不 1:1 |

## 6. ingest 后：Lint + sync

```bash
cd moli-knowledge
python kb/tools/lint.py --strict
bash kb/tools/ci/run_sync.sh dry-run-all
bash kb/tools/ci/run_sync.sh sync-all
```

- **Lint**：断链 / 孤儿 / 缺 sources（`lint.py --strict` 与 CI 同口径）
- **sync**：`wiki/` → `enterprise-kb`；三空间映射见 [[系统操作手册入口]]；完整命令见 ops 空间 `guides/wiki同步指南`

## 7. 与 Git 协作

wiki 变更随代码 **commit**；可选 `kb/tools/install_git_hook.ps1` 在 commit 后提醒 sync。

## 8. 示例：已完成的主题簇

`index.md` 底部 **待 ingest 主题 backlog** 中茉莉相关项已 ✅；后续新 raw 按主题增量即可，例如：

- 新业务模块 → `services/{名}.md` + `guides/` 操作页
- 中间件升级 → enrich 对应 `concepts/` 页 + `articles/` 踩坑

## 相关

[[系统操作手册入口]] · [[系统操作手册入口]] · [[茉莉新人上手checklist]]
