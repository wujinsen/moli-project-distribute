# wiki-moli 变更日志

## [2026-06-20] init | 茉莉系统手册独立空间

- 新建 `wiki-moli/`，从 `wiki/` 迁入 guides（13）、services（5）、concepts（2）
- 对应 DB 空间 `moli-ops-manual`（`space_id=900000000000000003`，内部可见）
- 种子 SQL：`docs/sql/07_kb_space_ops_manual.sql`

## [2026-06-25] maintenance | wiki同步指南 三空间映射表 + sync-all / dry-run-all 统一入口
## [2026-06-28] ingest | 批次#WB-20260628201240 test1 (Web工作台) → create fe_kamoku_b_set_sample_qs <!-- ingest-job:726515364753719296 -->
## [2026-06-25] reorg | 分类=目录：guides/product/develop/ops/test（原 services/concepts 并入 develop）
## [2026-06-20] maintenance | 移除误 ingest 的 jp-exam 页 fe_kamoku_b_set_sample_qs（应属 wiki-jp-exam）

## [2026-06-29] migrate | wiki → wiki-moli | batch=guides,services,concepts,outputs move=100 delete=6 skip=0 edges+=508

## [2026-06-29] migrate | wiki → wiki-moli | batch=articles,interview move=283 delete=0 skip=0 edges+=0

## [2026-06-29] migrate | wiki → wiki-moli | rewrite-all patched=56

## [2026-06-29] governance | 空间分离：项目页→wiki-moli；enterprise-kb 去茉莉 branding + 跨空间链

## [2026-06-29] governance | 空间分离 + enterprise-kb 去茉莉 branding

## [2026-07-01] crystallize | Docs as Code 设计哲学 → create develop/知识库设计哲学-docs-as-code (+5 enrich: 知识库三操作, 知识库服务, 知识库使用指南, wiki同步指南, 技术方案与架构索引)

## [2026-07-01] ingest | Meilisearch 接入规划 → create develop/知识库-meilisearch接入规划 (+draw.io moli-kb-meilisearch; enrich: kb-wiki到es同步流水线, 知识库服务, 知识库设计哲学-docs-as-code, index)

## [2026-07-01] maintenance | 体裁过滤后端落地（search kbType + /kb/index/types facet + /kb/meta/kb-types）→ enrich 知识库服务；API 文档 §2.1.1/§2.1.2 + 前端对接表

## [2026-07-01] maintenance | 浏览筛选 UX 改版：体裁×分类平行双 facet（废弃嵌套「先分类后体裁」）→ enrich API §2.1.3、知识库服务、知识库设计哲学 §4.1

## [2026-07-05] maintenance | 全项目文档巡检第三轮：修正 enterprise-kb↔wiki/ 映射 · deploy/README · api-iteration-map 路径 · T22 测试索引 · 删除 kb/tools 临时产物 → enrich 项目文档总览/文档健康度巡检/API契约索引/测试文档索引

## [2026-07-06] ingest | docs/design → wiki-moli/develop 概要设计 11 页（用户中心/API网关/知识库/BI 等）+ enrich 技术方案与架构索引 · 脚本 `tools/import_design_to_wiki.py`
