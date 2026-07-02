# 知识库运维 · 子域规划（内容管道运维）

> 更新：2026-07-02 · 状态：**规划**（部分后端已就绪）
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
| Sync（wiki→DB 单向增量） | `KbSyncServiceImpl` + `KbSyncScheduler` + `sync_to_db.py` | `kb_sync_log` | ✅ 手动/定时/CI |
| DB 健康体检（工单） | `KbInsightServiceImpl` | `kb_lint_issue` | ✅ |
| 文件级 Lint（治理前门禁） | `KbWikiLintServiceImpl` + `lint.py` | 不落库 | ✅ 后端 |
| 平台 LLM 配置（T19） | `KbPlatformLlmConfigServiceImpl` | `kb_platform_llm_config` | ✅ 后端；前端 T19d 📋 |
| Wiki 治理批量修复 | `KbWikiGovernServiceImpl` | — | ✅ 后端；前端 T16f 📋 |

接口前缀：`/kb/sync/*`、`/kb/lint*`、`/kb/wiki/lint-space`、`/kb/wiki/govern/*`、`/kb/platform/llm-config`。

---

## 3. 现状问题与缺口

| # | 问题 | 严重度 | 位置 |
|---|------|--------|------|
| K-P1 | **Sync 失败不可见**：`sync_to_db.py` 的 `_log()` 固定写 `status='success'`，`stats["fail"]` 从不递增 → `kb_sync_log.status=fail` 与 `SyncStatusVo.failCount` 恒为 0 | 🔴 高 | `sync_to_db.py` |
| K-P2 | **Sync 无并发锁**：定时 + 多人手动可并发跑同一空间，无互斥 | 🟡 中 | `KbSyncServiceImpl` |
| K-P3 | **Sync 无失败告警、无重试**：定时失败仅 `log.error`；超时 `destroyForcibly` | 🟡 中 | `KbSyncScheduler` |
| K-P4 | **定时同步默认关闭 + 仅单空间**：`schedule-enabled=false`，且只 sync `space-code`（enterprise-kb），不是 sync-all 三空间 | 🟡 中 | `application-dev.yml` |
| K-P5 | **权限码未 enforce**：`kb:sync:trigger`、`kb:lint:scan` 仅在菜单 SQL，后端实际用空间 ACL，动作码没生效 | 🟡 中 | `KbSyncServiceImpl` / `KbInsightServiceImpl` |
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
| **KBOPS-1** | **Sync 失败可观测**：`sync_to_db.py` 真正区分 success/fail 写 `kb_sync_log.status`，异常页记 `message`，进程以非 0 退出码收尾；后端 `failCount` 生效 | `sync_to_db.py`、`KbSyncServiceImpl` |
| **KBOPS-2** | **Sync 并发锁**：按 `space_code` 加 Redis 分布式锁，防定时+手动并发；已在跑则拒绝并提示 | `KbSyncServiceImpl` |
| **KBOPS-3** | **权限码对齐**：`/kb/sync/trigger` enforce `kb:sync:trigger`、`/kb/lint/scan` enforce `kb:lint:scan`（或明确文档为空间 ACL 并删菜单动作码） | 两个 ServiceImpl |

### P1 —— 闭环与界面

| 任务 | 内容 |
|------|------|
| **KBOPS-4** | 定时同步改 **sync-all 三空间**（或配置化 `space-codes` 列表）；文档与 `KbSyncScheduler` 对齐 |
| **KBOPS-5** | **失败告警**：Sync/定时失败发 webhook（企业微信/飞书/邮件其一），可开关 |
| **KBOPS-6** | **前端 T16f**：Wiki 治理全按钮（script-fix / auto-fix / merge-hint / Sync 勾选 / 复检摘要）—— 见 `docs/api/wiki-govern-frontend.md` |
| **KBOPS-7** | **前端 T19d**：平台 LLM 设置页 —— 见 `docs/api/kb-llm-platform-frontend.md` |

### P2 —— 增强（按需）

| 任务 | 内容 |
|------|------|
| **KBOPS-8** | 体检工单增强：扩展 issue_type、assignee/优先级、批量状态变更、可选定时 scan |
| **KBOPS-9** | **知识库运维 Dashboard**：Sync 批次趋势、Lint 工单、LLM 调用/失败率（需新增 `kb_llm_call_log`）、断链 `resolved=0` 汇总 |
| **KBOPS-10** | Web 健康体检检查项对齐 `lint.py`（或明确"文件真值用治理页、DB 快照用体检页"的分工文档） |

---

## 6. 表与 API

| 表 | 用途 | 增量规划 |
|----|------|----------|
| `kb_sync_log` | Sync 审计 | KBOPS-1 让 `status/message` 真实生效 |
| `kb_lint_issue` | 体检工单 | KBOPS-8 增 assignee/priority |
| `kb_platform_llm_config` | 平台 LLM 单例 | 已就绪（T19） |
| `kb_llm_call_log`（新） | LLM 调用审计 | KBOPS-9 可选 |

关键接口见 `docs/api/KNOWLEDGE_API.md` §3.5（LLM）、§4（体检/Sync）、§8.6（治理）。

---

## 7. 边界（不做）

- **不含服务器/基础设施运维**（平台账号、服务器、组件台账）—— 见 [`server-ops-module-roadmap.md`](server-ops-module-roadmap.md)。
- 不含平台级 APM / ELK / Prometheus（`docs/ops/monitoring-and-logs.md`）。
- 不改 `raw/`；wiki 唯一写路径仍是 `PUT /kb/wiki/page` → Sync（见 `moli-knowledge/kb/AGENTS.md` §8）。

---

## 8. 相关

- 任务跟踪：`moli-knowledge/TASKS.md`（T16f / T19d / 本 KBOPS 系列）
- Roadmap：`moli-knowledge/kb/ROADMAP.md`
- 运维 SOP：`docs/ops/knowledge-workbench-operations.md`
- 前端对接：`docs/api/wiki-govern-frontend.md`、`docs/api/kb-llm-platform-frontend.md`
- LLM 设计：[`kb-llm-platform-settings.md`](kb-llm-platform-settings.md)
