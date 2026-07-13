# 前端缺口与联调索引

> **运营 · 最新通知（2026-07-13）**  
> 后端交付：**S-VO `*Count`** · **create 返回 id（含 server）** · **批量 deploy** · **任务取消** · **batch links（可选）**。  
> **开工** → **[api/operation-frontend-handoff.md](api/operation-frontend-handoff.md)** · **转发** handoff §7.1  
> **给后端** → **[api/frontend-backend-dependencies.md](api/frontend-backend-dependencies.md)**

---

## 一、前端可开工（2026-07-13）

| 模块 | 优先级 | 任务 | 文档 |
|------|--------|------|------|
| **运营 meiling-ui** | **P0** | **S-VO**：`row.serverCount`；去掉 links 水合计数 | [operation-frontend-handoff.md](api/operation-frontend-handoff.md) §2 |
| 运营 | **P0** | **create 返回 id**（含 server）· **上传轮询** · **batch 重启** · **任务取消** | handoff §3 |
| 运营 | P0 | 验证 `serverCount === serverIds.length`（W2b） | handoff §5 |
| 运营 | P0 | Phase R 未合入项（probe-all、presets、10107） | [operation-frontend.md](api/operation-frontend.md) §13 |
| 运营 | P1 | 浏览器走查 W1–W10 | handoff §5 |
| **知识库** | P1 | 浏览 facet 多选、Lint 分页 | [knowledge-workbench-frontend.md](api/knowledge-workbench-frontend.md) |
| SSO | — | **等后端** `getRouters` 按 `system_id` | [frontend-backend-dependencies.md](api/frontend-backend-dependencies.md) §4 |

**前置**：重启 user-center `:8888`（含 `toVo` 计数改动）后再联调。

---

## 二、文档地图

| 方向 | 文档 |
|------|------|
| **运营前端开工（首选）** | **[api/operation-frontend-handoff.md](api/operation-frontend-handoff.md)** |
| 运营完整契约 | [api/operation-frontend.md](api/operation-frontend.md) §15–§16 |
| 运营后端通知 | [api/operation-backend-handoff.md](api/operation-backend-handoff.md) |
| 知识库工作台 | [api/knowledge-workbench-frontend.md](api/knowledge-workbench-frontend.md) |
| 菜单 ↔ API | [api/frontend-routes-map.md](api/frontend-routes-map.md) |
| 冒烟 | [test/release-smoke-checklist.md](test/release-smoke-checklist.md) |

---

## 三、给后端（非前端阻塞）

Breaking create 返回 id、links 同步、**`toVo()` `*Count`**、**批量 deploy / batch links / 任务取消** 已对齐。运营剩余主要为 SSO 菜单 — 见 [frontend-backend-dependencies.md](api/frontend-backend-dependencies.md) §4。
