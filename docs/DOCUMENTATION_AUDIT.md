# 文档健康度巡检

> **巡检日期**：2026-07-05（第三轮 · T22 收尾 + 部署索引 + 路径校准）  
> **范围**：PRD / 设计 / API / 测试 / 运维 / SQL / 模块 README / wiki 成品 / 架构图  
> **约定**：[`README.md`](README.md) · [`AGENTS.md`](../AGENTS.md)

## 总览

| 维度 | 状态 | 说明 |
|------|------|------|
| 五类文档索引 | ✅ | `docs/{product,design,api,test,ops}/README.md` |
| 五微服务 wiki 页 | ✅ | `kb/wiki-moli/develop/` |
| 茉莉系统手册 | ✅ | `kb/wiki-moli/`（`moli-ops-manual`） |
| enterprise-kb | ✅ | `kb/wiki/`（通用技术文库，与手册分空间） |
| 网关 / 用户中心 / 订单 / 知识库全链路 | ✅ | PRD·设计·API·测试 |
| BI（moli-ai） | ✅ | 设计 + 产品摘要 + bi-smoke |
| 公共模块 README | ✅ | `moli-distribute-common/README.md` |
| 架构图 PNG | ✅ | `docs/diagrams/png/`（含 T20/T22/Meilisearch） |
| API 迭代地图 | ✅ | [`docs/api/api-iteration-map.md`](api/api-iteration-map.md) |
| 生产部署索引 | ✅ | [`deploy/README.md`](../deploy/README.md) · [`deploy/上线流程.md`](../deploy/上线流程.md) |
| T22 wujinsen 插图回迁 | ✅ | manifest 397/397 · R3 PASS · 见 [`test/knowledge-t22-image-remediation.md`](test/knowledge-t22-image-remediation.md) |
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

## 本轮变更（2026-07-05）

| 项 | 动作 |
|----|------|
| `docs/api-iteration-map.md` | 迁入 `docs/api/`，修正全仓断链 |
| `deploy/README.md` | 恢复部署目录索引 |
| `kb/tools/_*.txt`、`lint-*.json` | 删除临时工具产物 |
| `docs/test/knowledge-t22-image-remediation.md` | 新增 T22 验收与自动化测试索引 |
| `wiki-moli/guides/项目文档总览` | 修正 enterprise-kb ↔ `kb/wiki/` 映射 |
| `docs/diagrams/README.md` | 同上 · 三空间 Sync 表 |

## 仍可选（非阻塞）

| 项 | 说明 |
|----|------|
| meiling-ui 工作台 §10 | 🔵 部分前端规格待代码对齐 |
| raw/design 外部稿 | 有评审 PDF 时再投喂 |
| Meilisearch 生产接入 | 规划已入库，代码 v2+ |
| `docs/design/archive/` | 历史稿保留备查，非冗余 |

## 维护节奏

1. 改接口 → `docs/api/` + [`api/api-iteration-map.md`](api/api-iteration-map.md)
2. 改架构图 → `.drawio` → `export-diagrams.ps1`
3. 新服务 PRD → `raw/prd/` + `docs/product/` + wiki `*产品说明.md`
4. 发版前 → 更新本页日期 + [`release-smoke-checklist.md`](test/release-smoke-checklist.md)

## 相关

- [api/api-iteration-map.md](api/api-iteration-map.md)
- [product/README.md](product/README.md)
- [moli-v1-release-scope.md](product/moli-v1-release-scope.md)
- [kb/wiki-moli/guides/项目文档总览.md](../moli-knowledge/kb/wiki-moli/guides/项目文档总览.md)
