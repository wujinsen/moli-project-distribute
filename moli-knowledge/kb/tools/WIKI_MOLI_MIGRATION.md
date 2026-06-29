# wiki ↔ wiki-moli 空间治理说明

> **当前规则（2026-06-29 起）**：**项目文档** ↔ **`wiki-moli/`** · **moli-ops-manual**；**通用技术语料** ↔ **`wiki/`** · **enterprise-kb**。  
> 详见 [`AGENTS.md`](../AGENTS.md) §1.0.1。

## 已完成

| 动作 | 脚本 |
|------|------|
| 通用 articles/concepts/interview 迁回 `wiki/` | `revert_corpus_to_enterprise_kb.py` |
| 删 `## …茉莉…` 模板节 | 同上 |
| 项目专文迁入 `wiki-moli/`、enterprise-kb 去茉莉 branding | `kb_space_governance.py` |
| Web 与磁盘对齐 | `bash kb/tools/ci/run_sync.sh sync-all` |

## 项目页（wiki-moli 示例）

- `develop/服务调用与架构.md`、`develop/秒杀设计.md`、`develop/技术栈与版本.md`
- `guides/`、`product/`、`ops/` Runbook、`develop/outputs/茉莉*汇总.md`

## 通用语料（wiki 示例）

- `articles/dubbo-超时链路传递.md`、`concepts/redis-缓存.md`、`interview/dubbo-面试题.md`
- **禁止**正文出现「茉莉」或链到项目手册页

## 验收

```bash
python kb/tools/kb_space_governance.py --dry-run
python kb/tools/lint.py --wiki-dir wiki --strict   # space_branding 应为 0
bash kb/tools/ci/run_sync.sh dry-run-all
bash kb/tools/ci/run_sync.sh sync-all
```

moli-ops-manual 文档数应约 **~70**；enterprise-kb 约 **~340**。
