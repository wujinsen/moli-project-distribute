# 产品文档（PRD / 产品方案）

## 权威位置

| 阶段 | 路径 |
|------|------|
| **v1 发布范围** | **[`moli-v1-release-scope.md`](moli-v1-release-scope.md)** — 首版交付边界 |
| **需求总览（知识库）** | [`knowledge-workbench-requirements.md`](knowledge-workbench-requirements.md) |
| **新稿投喂** | `moli-knowledge/kb/raw/prd/` |
| **维护 / 浏览** | `moli-knowledge/kb/wiki-moli/guides/` |
| **Web 浏览** | 空间 `moli-ops-manual`（`wiki-moli/`） |

## 已有页面

| 文档 | 路径 | 状态 |
|------|------|------|
| **v1.0 发布范围** | [`moli-v1-release-scope.md`](moli-v1-release-scope.md) | 2026-06-28 |
| **网关需求（工程索引）** | [`gateway-requirements.md`](gateway-requirements.md) | v1 ✅ |
| **用户中心需求（工程索引）** | [`user-center-requirements.md`](user-center-requirements.md) | v1 ✅ |
| **秒杀压测需求** | [`order-seckill-requirements.md`](order-seckill-requirements.md) | v1 ✅ |
| **知识库模块需求（工程索引）** | [`knowledge-module-requirements.md`](knowledge-module-requirements.md) | v1 ✅ |
| **知识库工作台需求总览** | [`knowledge-workbench-requirements.md`](knowledge-workbench-requirements.md) | 2026-06-27 |
| **知识库双入口导入 PRD（T20）** | [`knowledge-import-entry-prd.md`](knowledge-import-entry-prd.md) | draft · 2026-07-05 |
| **知识库内容管道运维 PRD（KBOPS）** | [`knowledge-ops-prd.md`](knowledge-ops-prd.md) | draft · 2026-07-09 |
| **wujinsen 图片回迁 PRD（T22）** | [`wujinsen-wiki-image-remediation-prd.md`](wujinsen-wiki-image-remediation-prd.md) | ✅ 397/397 · 2026-07-05 |
| **网关 PRD 原文** | [`kb/raw/prd/gateway-prd-v1.md`](../../moli-knowledge/kb/raw/prd/gateway-prd-v1.md) | raw |
| **订单秒杀 PRD 原文** | [`kb/raw/prd/order-seckill-prd-v1.md`](../../moli-knowledge/kb/raw/prd/order-seckill-prd-v1.md) | raw |
| **用户中心 PRD 原文** | [`kb/raw/prd/user-center-prd-v1.md`](../../moli-knowledge/kb/raw/prd/user-center-prd-v1.md) | raw |
| **Ingest 工作台产品方案** | [`kb/wiki-moli/develop/Ingest工作台产品方案.md`](../../moli-knowledge/kb/wiki-moli/develop/Ingest工作台产品方案.md) | wiki |
| **Wiki 治理产品方案** | [`kb/wiki-moli/develop/Wiki治理工作台产品方案.md`](../../moli-knowledge/kb/wiki-moli/develop/Wiki治理工作台产品方案.md) | wiki |
| **各服务产品摘要（wiki）** | 网关 / 用户中心 / 订单秒杀 / BI / 知识库 → `kb/wiki-moli/guides/*产品说明.md` | wiki |
| 路线图 | `kb/ROADMAP.md` | 模块级 |

## 工作流

1. 新版 PRD → `raw/prd/`（新文件）
2. Agent Ingest → enrich wiki
3. 工程索引 → 更新本目录短页 + `moli-v1-release-scope.md`

## 不要

- 在 `docs/product/` 写超长 PRD 正文（索引 + 验收口径即可）
- 为 v2/v5 新建平行 slug（除非策略 C）
