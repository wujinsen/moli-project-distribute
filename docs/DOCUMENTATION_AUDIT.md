# 文档健康度巡检

> **巡检日期**：2026-06-20（第二轮补齐 P1/P2）  
> **范围**：PRD / 设计 / API / 测试 / 运维 / SQL / 模块 README / wiki 成品 / 架构图  
> **约定**：[`README.md`](README.md) · [`AGENTS.md`](../AGENTS.md)

## 总览

| 维度 | 状态 | 说明 |
|------|------|------|
| 五类文档索引 | ✅ | `docs/{product,design,api,test,ops}/README.md` |
| 五微服务 wiki 页 | ✅ | `kb/wiki/services/` |
| 五服务 product wiki 摘要 | ✅ | `kb/wiki/guides/*产品说明.md` |
| 运维手册 | ✅ | `kb/wiki-ops/` |
| 网关 / 用户中心 / 订单 / 知识库全链路 | ✅ | PRD·设计·API·测试 |
| BI（moli-ai） | ✅ | 设计 + 产品摘要 + bi-smoke |
| 公共模块 README | ✅ | `moli-distribute-common/README.md` |
| 架构图 PNG | ✅ | `docs/diagrams/png/`（16 张） |
| API 迭代地图 | ✅ | `docs/api-iteration-map.md` |
| 工作台前端 Spec | ✅ | `knowledge-workbench-frontend.md` §10 |
| raw/prd 投喂 | ✅ | gateway / order / user-center 三份 |

## 按模块

| 模块 | PRD | 设计 | API | 测试 | wiki product |
|------|-----|------|-----|------|--------------|
| gateway | ✅ | ✅ | ✅ | ✅ | ✅ |
| user-center | ✅ | ✅ | ✅ | ✅ | ✅ |
| order | ✅ | ✅ | ✅ | ✅ | ✅ |
| moli-ai (BI) | 🟡 scope | ✅ | ✅ | ✅ | ✅ |
| knowledge | ✅ | ✅ | ✅ | ✅ | ✅ |
| common | — | — | — | — | N/A |

## 仍可选（非阻塞）

| 项 | 说明 |
|----|------|
| meiling-ui 落地 §10 | 前端代码实现 nextSteps / 治理四步 |
| raw/design 外部稿 | 有评审 PDF 时再投喂 |
| 向量检索 / Meilisearch | v2+ |

## 维护节奏

1. 改接口 → `docs/api/` + `api-iteration-map.md`
2. 改架构图 → `.drawio` → `export-diagrams.ps1`
3. 新服务 PRD → `raw/prd/` + `docs/product/` + wiki `*产品说明.md`
4. 发版前 → 更新本页日期 + `release-smoke-checklist.md`

## 相关

- [api-iteration-map.md](api/api-iteration-map.md)
- [product/README.md](product/README.md)
- [moli-v1-release-scope.md](product/moli-v1-release-scope.md)
