# wiki-moli 变更日志

## [2026-06-20] init | 茉莉系统手册独立空间

- 新建 `wiki-moli/`，从 `wiki/` 迁入 guides（13）、services（5）、concepts（2）
- 对应 DB 空间 `moli-ops-manual`（`space_id=900000000000000003`，内部可见）
- 种子 SQL：`docs/sql/07_kb_space_ops_manual.sql`

## [2026-06-25] maintenance | wiki同步指南 两空间映射表 + sync-all / dry-run-all 统一入口
## [2026-06-28] ingest | 批次#WB-20260628201240 test1 (Web工作台) → create fe_kamoku_b_set_sample_qs <!-- ingest-job:726515364753719296 -->
## [2026-06-25] reorg | 分类=目录：guides/product/develop/ops/test（原 services/concepts 并入 develop）
## [2026-06-20] maintenance | 移除误 ingest 的 FE 样题页（已下线空间）

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

## [2026-07-10] enrich | 运营管理收尾 → enrich 服务器运维模块规划（P0–P2 + SVR-10/11/12 已落地；meiling-ui S6/S7 前端补齐）

## [2026-07-10] maintenance | #moli-link-governance-20260710 wiki-moli 断链治理 → 修复 178 文件（enterprise 路径 + moli 内链）

## [2026-07-10] maintenance | #moli-link-governance-20260710 wiki-moli 断链治理 → 修复 180 文件（enterprise 路径 + moli 内链）

## [2026-07-10] maintenance | lint-strict 收口 → 删除 T20/E2E 验收页（guides/x、guides/t20f-wiki-144730、wiki/database/e2e-import）；index 运维链修正；fix_moli_orphans 无 orphan 时跳过

## [2026-07-11] enrich | 远程部署自动化 SVR-13~17 → 前端部署中心/SSH 弹窗 + docs/sql/21 + operation-frontend §11 + wiki/腾讯云文档

## [2026-07-13] implement | ask 作用域/精排优化 → golden hit@8=100%（M03/M06/E01 修复）
## [2026-07-13] implement | chunk 切段 v1：sync_to_db 写 kb_document_chunk + /kb/ask 按段召回（+ eval 回归）
## [2026-07-13] docs | 腾讯云 CVM 基础环境安装 → docs/ops/tencent-cloud-cvm-bootstrap.md + enrich wiki/腾讯云生产部署指南、deploy/腾讯云上线流程 §3.0

## [2026-07-19] maintenance | AI-5 GraphRAG ���ݲཱུ�� �� enrich guides/��������ָ�ϣ�related ������ + FAQ ȥ [[ǰ��/docker/swagger]]���� develop/�û����ģ�related �����ҳ + ȥ [[Ȩ�޹�������ָ��]]��

## [2026-07-19] maintenance | AI-5 GraphRAG ���ݲ� �� checklist ȥ��Ŧ���� �� enrich develop/outputs/������������checklist��[[��Ŀ�ĵ�����]] ������Ϊ��������/���ʼ��/��¼��Ȩ����·����

## [2026-07-19] maintenance | AI-5 GraphRAG ���ݲ� �� ȥ��Ŀ�ĵ�������Ŧ �� enrich ����΢����ȫ��·һ��ͼ�������¼���Ȩ���ϸ�����ܣ�[[��Ŀ�ĵ�����]] ������Ϊ��������/��¼��Ȩ/���صȣ�

## [2026-07-19] maintenance | AI-5 GraphRAG ���ݲ� �� SQL�������ֵ�����ȥ��Ŧ�ߣ��������� related ȥ�������Ų�

## [2026-07-20] maintenance | AI-5 A+B+C �� enrich ops/�������־+�����Ų�ָ�ϣ�M28 �������� �㷨 protectBaseTopK/hubPenalty


## [2026-08-31] ingest | 可观测性生产部署 → create ops/可观测性生产部署; docs/ops/observability-production.md + draw.io prod-topology/log-resilience; enrich 可观测性平台/监控与日志/生产检查清单/observability-platform-plan
