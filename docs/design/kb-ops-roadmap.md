# 知识库运维 · 子域规划（内容管道运维）

> 更新：2026-07-11 · 状态：**P0/P1 后端大部分已落地**（KBOPS-1/2/3/O1 · KBOPS-4/5/9 后端 · T19e）；前端 T16f/T19d/O1–O4 待 meiling-ui  
> **产品 PRD**：[`docs/product/knowledge-ops-prd.md`](../product/knowledge-ops-prd.md) · **前端对接**：[`docs/api/knowledge-ops-frontend.md`](../api/knowledge-ops-frontend.md)  
> 归属：`moli-knowledge` · `kb_sync_log` / `kb_lint_issue` / `kb_platform_llm_config`
> 边界：**只管知识库内容管道运维**（wiki→DB 同步、体检、LLM 配置）；服务器/基础设施运维见 [`server-ops-module-roadmap.md`](server-ops-module-roadmap.md)（另一条独立路线，互不重叠）

---

## 1. 背景与定位

知识库运维是 **`moli-knowledge` 自己的运维子域**，不属于 user-center 的「运维管理」菜单，也不是服务器运维。

它面向**知识库编辑/管理员**，保障"markdown wiki → MySQL → 对外可查"这条内容管道的**同步正确性、内容质量、LLM 可用性**。

> **纠偏**：早期讨论曾把它挂到"服务器运维"下，属误归。二者领域、服务对象、归属模块均不同。

---

## 2. 现状

| 能力 | 代码 | 表 / 配置 | 状态 |
|------|------|-----------|------|
| Sync（wiki→DB 单向增量） | `KbSyncServiceImpl` + `KbSyncScheduler` + `sync_to_db.py` | `kb_sync_log` | ✅ 手动/定时/CI · **KBOPS-1/2/O1** · `POST trigger?async=true` 可选后台 |
| DB 健康体检（工单） | `KbInsightServiceImpl` | `kb_lint_issue` | ✅ |
| 文件级 Lint（治理前门禁） | `KbWikiLintServiceImpl` + `lint.py` | 不落库 | ✅ 后端 |
| 平台 LLM 配置（T19） | `KbPlatformLlmConfigServiceImpl` | `kb_platform_llm_config` | ✅ 后端 + **T19e**；前端 T19d 📋 |
| Wiki 治理批量修复 | `KbWikiGovernServiceImpl` | — | ✅ 后端；前端 T16f 📋 |

接口前缀：`/kb/sync/*`、`/kb/lint*`、`/kb/wiki/lint-space`、`/kb/wiki/govern/*`、`/kb/platform/llm-config`。

---

## 3. 现状问题与缺口

| # | 问题 | 严重度 | 位置 |
|---|------|--------|------|
| K-P1 | ~~Sync 失败不可见~~ | ✅ KBOPS-1 | `sync_to_db.py` + `failCount` |
| K-P2 | ~~Sync 无并发锁~~ | ✅ KBOPS-2 | Redis `kb:sync:lock:{spaceCode}` |
| K-P2b | **Sync 轮询字段缺失**（前端 O1） | ✅ KBOPS-O1 | `SyncStatusVo.running/lastStatus/...` |
| K-P3 | ~~Sync 无失败告警~~ | ✅ KBOPS-5 | `KbSyncAlertService` + `kb.sync.alert.*`（默认关，配 webhook 启用） |
| K-P4 | **定时同步默认关闭**：`schedule-enabled=false`；开启后 `resolveScheduleSpaceCodes()` 已支持三空间 / 配置列表 | 🟡 低 | `KbSyncScheduler` · 生产按需开 cron |
| K-P5 | ~~权限码未 enforce~~ | ✅ KBOPS-3 | `KbAclService.assertCanSyncTrigger` / `assertCanLintScan` |
| K-P6 | **DB 体检 issue_type 不全**：仅 `broken_link` / `orphan` / `no_summary`；DDL 注释里的 duplicate/stale/conflict 未实现；工单无 assignee/优先级/批量状态变更 | 🟢 低 | `KbInsightServiceImpl` |
| K-P7 | **前端缺口**：T16f 治理全按钮、T19d LLM 设置页（后端均已就绪） | 🟡 中 | meiling-ui |
| K-P8 | Web 健康体检检查项**少于** `lint.py`（missing_source/bad_type/dup_slug/outdated 等未覆盖） | 🟢 低 | 设计差异 |

---

## 4. 目标

1. Sync **失败可见、并发安全、失败可告警**。
2. 定时同步覆盖**三空间**（或明确可配多空间）。
3. 权限模型与动作码**对齐**。
4. 体检工单**闭环**（状态流转、批量、可选定时扫描）。
5. 补齐**运维操作界面**（治理 + LLM 设置），让 SOP 可点、不用 Swagger。

---

## 5. 路线图

### P0 —— 正确性与安全

| 任务 | 内容 | 涉及 |
|------|------|------|
| **KBOPS-1** | **Sync 失败可观测**：`sync_to_db.py` 区分 success/fail；`failCount` 生效 | ✅ |
| **KBOPS-2** | **Sync 并发锁**：Redis 锁；已在跑则拒绝 | ✅ |
| **KBOPS-O1** | **Sync 状态轮询**：`GET /kb/sync/status` 增 `running`/`lastStatus`/`lastMessage`/`successCount`；`SyncTriggerVo` 增 `batchNo` | ✅ |
| **KBOPS-3** | **权限码对齐**：`/kb/sync/trigger` enforce `kb:sync:trigger`、`/kb/lint/scan` enforce `kb:lint:scan` | ✅ `KbAclServiceImpl` |

### P1 —— 闭环与界面

| 任务 | 内容 |
|------|------|
| **KBOPS-4** | 定时同步 **sync-all 三空间**（或 `kb.sync.schedule-space-codes`）；`KbSyncScheduler` 按列表循环 | ✅ 后端 · 生产 cron 待开 |
| **KBOPS-5** | **失败告警**：Sync/定时失败 webhook（飞书/企微/generic JSON），`kb.sync.alert.enabled` | ✅ 后端 · 运维配 URL |
| **KBOPS-6** | **前端 T16f**：Wiki 治理全按钮（script-fix / auto-fix / merge-hint / Sync 勾选 / 复检摘要）—— 见 `docs/api/wiki-govern-frontend.md` |
| **KBOPS-7** | **前端 T19d**：平台 LLM 设置页 —— 见 `docs/api/kb-llm-platform-frontend.md` |
| **T19e** | **LLM 加密就绪信号**：`encryptionReady` + 环境变量 `KB_LLM_CONFIG_SECRET` 运行时回退（保存新 key 无需重启） | ✅ |

### P2 —— 增强（按需）

| 任务 | 内容 |
|------|------|
| **KBOPS-8** | 体检工单增强：扩展 issue_type、assignee/优先级、批量状态变更、可选定时 scan | ✅ 2026-07-12 |
| **KBOPS-9** | **知识库运维 Dashboard**：`GET /kb/ops/dashboard` — Sync 趋势、Lint 工单、LLM 摘要、漂移采样 | ✅ 后端 · 前端 O4 📋 |
| **KBOPS-10** | Web 健康体检检查项对齐 `lint.py` + 分工文档（`GET /kb/lint/issue-types`、`LintVo.dataSource`、查询与体检指南 §3.3） | ✅ 2026-07-12 |

---

## 6. 表与 API

| 表 | 用途 | 增量规划 |
|----|------|----------|
| `kb_sync_log` | Sync 审计 | ✅ KBOPS-1 `status/message`；O1 读 `action=batch` 汇总行 |
| `kb_lint_issue` | 体检工单 | ✅ KBOPS-8 assignee/priority + 批量 API + 定时 scan |
| `kb_platform_llm_config` | 平台 LLM 单例 | 已就绪（T19） |
| `kb_llm_call_log`（新） | LLM 调用审计 | KBOPS-9 可选 |

关键接口见 `docs/api/KNOWLEDGE_API.md` §3.5（LLM）、§4（体检/Sync）、§8.6（治理）。

### 工程补充

| 任务 | 内容 | 涉及 | 状态 |
|------|------|------|------|
| **KBOPS-A1** | CI **lint-strict 硬门禁**：PR `dry-run-all` + `lint-strict-all` blocking | `.github/workflows/kb-sync.yml` | ✅ |
| **KBOPS-A2** | Sync **失败 Runbook** | `docs/ops/kb-sync-failure-runbook.md` | ✅ |
| **KBOPS-A3** | wiki↔DB **漂移检测** | 脚本 + `GET /kb/sync/drift` + Dashboard `driftSummary` | ✅ |

---

## 7. 边界（不做）

- **不含服务器/基础设施运维**（平台账号、服务器、组件台账）—— 见 [`server-ops-module-roadmap.md`](server-ops-module-roadmap.md)。
- 不含平台级 APM / ELK / Prometheus（`docs/ops/monitoring-and-logs.md`）。
- 不改 `raw/`；wiki 唯一写路径仍是 `PUT /kb/wiki/page` → Sync（见 `moli-knowledge/kb/AGENTS.md` §8）。

---

## 8. 相关

- 任务跟踪：`moli-knowledge/TASKS.md`（T16f / T19d / 本 KBOPS 系列）
- Roadmap：`moli-knowledge/kb/ROADMAP.md`
- 运维 SOP：`docs/ops/knowledge-workbench-operations.md` · Sync 失败：[`kb-sync-failure-runbook.md`](../ops/kb-sync-failure-runbook.md)
- 前端对接：`docs/api/wiki-govern-frontend.md`、`docs/api/kb-llm-platform-frontend.md`
- LLM 设计：[`kb-llm-platform-settings.md`](kb-llm-platform-settings.md)
