# 测试文档

## 权威位置

| 类型 | 路径 |
|------|------|
| **秒杀 / 百万 QPS 压测** | [`load-test/README.md`](../../load-test/README.md)（脚本 + 环境） |
| **秒杀链路图** | 见 [`test/README.md`](README.md) · 源文件 [`moli-seckill-flow.drawio`](../diagrams/moli-seckill-flow.drawio) |
| **用户中心 ApiTest** | [`user-center.md`](user-center.md) |
| **知识库 Wiki Lint（T16a）** | [`knowledge-wiki-lint-space.md`](knowledge-wiki-lint-space.md) |
| **知识库 Ingest 删批次** | [`knowledge-ingest-delete-job.md`](knowledge-ingest-delete-job.md) |
| **压测操作指南（浏览）** | `kb/wiki/guides/秒杀压测指南.md` |
| **压测报告解读** | `kb/wiki/guides/压测报告解读指南.md` |
| **测试概念 / 面试** | `kb/wiki/concepts/测试金字塔-与分层.md`、`wiki/interview/测试与质量面试题.md` |
| **新稿投喂** | `kb/raw/test/` |

![秒杀全链路](../diagrams/png/moli-seckill-flow.png)

> 可编辑源文件：[moli-seckill-flow.drawio](../diagrams/moli-seckill-flow.drawio)

## 工作流

1. 压测方案变更 → 改 `load-test/` + enrich `wiki/guides/秒杀压测指南.md`
2. 通用测试方法论外部稿 → `raw/test/` → Ingest → `articles/` 或 `concepts/`
3. sync → Web 浏览

## 不要

- 把 k6 脚本放进 `docs/test/`（保持在 `load-test/`）
