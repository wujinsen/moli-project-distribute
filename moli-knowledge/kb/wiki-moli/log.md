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

## [2026-06-29] governance | 空间分离 + enterprise-kb 去茉莉 branding
