# 知识库 · 健康体检工单（KBOPS-8/10）验收

> 模块：`moli-knowledge-server`  
> 契约：[KNOWLEDGE_API.md §4](../api/KNOWLEDGE_API.md#42-体检只算不落库get-kblint) · 前端：[knowledge-ops-frontend.md §3.7](../api/knowledge-ops-frontend.md#37-p1--体检工单增强o5o8--kbops-8)  
> 分工：`wiki-moli/guides/查询与体检指南` §3.3（**DB 快照 vs lint.py 文件真值**）

---

## 1. 范围

| 能力 | API | 说明 |
|------|-----|------|
| DB 体检（不落库） | `GET /kb/lint` | `LintVo` 含 12 类检查 + `dataSource=db_snapshot` |
| 扫描落库 | `POST /kb/lint/scan` | 写 `kb_lint_issue`；清旧 `status=0` |
| 工单列表 | `GET /kb/lint/issues` | 支持 `issueType` / `assigneeId` / `priority` / `status` |
| 单条状态 | `PUT /kb/lint/issue/{id}?status=` | 0待处理 / 1已忽略 / 2已修复 |
| 批量状态 | `PUT /kb/lint/issues/batch-status` | KBOPS-8 |
| 单条指派 | `PUT /kb/lint/issue/{id}/assign` | `assigneeId` / `priority` |
| 批量指派 | `PUT /kb/lint/issues/batch-assign` | KBOPS-8 |
| 类型对照 | `GET /kb/lint/issue-types` | KBOPS-10 · Web ↔ lint.py |
| **Scan 状态（只读）** | `GET /kb/lint/scan/status` | `scheduleEnabled` / `lastScanTime` / `openIssueCount`（O9） |
| 定时 scan | `KbLintScanScheduler` | `kb.lint.schedule-enabled`（默认关） |

**不在本验收**：`POST /kb/wiki-moli/lint-space`（文件真值）→ 见 [knowledge-wiki-lint-space.md](knowledge-wiki-lint-space.md)。

---

## 2. 自动化测试清单

测试目录：`moli-knowledge/moli-knowledge-server/src/test/java/com/moli/knowledge/server/`

### 2.1 `KbLintIssueTypesTest`（5）

| # | 方法 | 预期 |
|---|------|------|
| 1 | `all_containsTwelveWebIssueTypes` | `all()` 含 12 种 Web `issue_type` |
| 2 | `descriptors_mapsDuplicateToDupSlug` | `duplicate` → lint.py `dup_slug` |
| 3 | `descriptors_marksNoSummaryWebOnly` | `no_summary` 仅 Web |
| 4 | `descriptors_includesLintPyOnlyKinds` | 含 `space_branding` / `near_dup` |
| 5 | `descriptors_staleMapsToOutdated` | `stale` → `outdated` |

### 2.2 `KbLintIssueDetectorTest`（10）

| 类别 | 覆盖 |
|------|------|
| duplicate / stale / conflict | slug 歧义、库龄、supersedes、contentHash |
| frontmatter | `missing_source` / `bad_type` / `missing_title` / `slug_mismatch` / `missing_dates` |
| missing_concept | 断链目标多引用 |

### 2.3 `KbInsightServiceImplLintOpsTest`（9）

| # | 方法 | 预期 |
|---|------|------|
| 1 | `batchAssignIssues_updatesAssigneeAndPriority` | 写入 assignee + priority |
| 2 | `batchAssignIssues_skipsMissingIds` | 不存在 ID 跳过 |
| 3 | `batchAssignIssues_rejectsEmptyIds` | `BaseException` |
| 4 | `batchAssignIssues_requiresAssigneeOrPriority` | 至少一项 |
| 5 | `assignIssue_rejectsInvalidPriority` | priority 0–2 |
| 6 | `assignIssue_updatesPriorityOnly` | 仅改 priority |
| 7 | `batchUpdateIssueStatus_updatesStatus` | 批量改 status |
| 8 | `batchUpdateIssueStatus_rejectsNullStatus` | `BaseException` |
| 9 | `batchUpdateIssueStatus_rejectsEmptyIds` | `BaseException` |

### 2.4 `KbInsightControllerLintOpsApiTest`（6）

| # | 方法 | 预期 |
|---|------|------|
| 1 | `PUT_kb_lint_issues_batch_assign` | `code=200`，返回更新条数 |
| 2 | `GET_kb_lint_issue_types` | 含 `broken_link`、`space_branding` lintPyOnly |
| 3 | `PUT_kb_lint_issues_batch_status` | 批量状态 API 契约 |
| 4 | `PUT_kb_lint_issue_assign` | 单条指派委托 Service |
| 5 | `PUT_kb_lint_issue_status` | 单条状态委托 Service |
| 6 | `GET_kb_lint_scan_status` | 返回 `scheduleEnabled` 等字段 |

### 2.4.1 `KbInsightServiceImplLintScanStatusTest`（2）

| # | 方法 | 预期 |
|---|------|------|
| 1 | `scanStatus_readsScheduleConfigAndRedisLastScan` | yml 配置 + Redis 最近 scan 时间 |
| 2 | `scanStatus_globalRequiresAdmin` | 全库 status 需 admin |

### 2.5 `KbLintScanSchedulerTest`（3）

| # | 方法 | 预期 |
|---|------|------|
| 1 | `scheduledScan_skipsWhenDisabled` | `schedule-enabled=false` 不调 scan |
| 2 | `scheduledScan_usesConfiguredSpaceIds` | 按配置空间列表 scan |
| 3 | `scheduledScan_scansAllSpacesWhenIdsEmpty` | 空配置时扫全部 `kb_space` |

**合计**：**32** 用例（KBOPS-8/10 相关）

### 2.6 最近一次执行

| 项 | 值 |
|----|-----|
| 执行时间 | 2026-07-12 |
| 环境 | Windows · JDK 11 · Maven 3.9 |
| 结果 | **32** 通过 · **0** 失败 |

---

## 3. 运行命令

```bash
cd moli-knowledge/moli-knowledge-server
mvn test "-Dtest=KbLintIssueTypesTest,KbLintIssueDetectorTest,KbInsightServiceImplLintOpsTest,KbInsightControllerLintOpsApiTest,KbLintScanSchedulerTest"
```

PowerShell 下 `-Dtest` 含逗号须加引号。

---

## 4. 手工 / 联调验收（P1）

前置：已执行 `docs/sql/17_kb_lint_ops_enhance.sql`（`assignee_id` / `priority` 列）；角色含 `kb:lint:scan`；至少一个空间已 Sync。

| ID | 步骤 | 期望 |
|----|------|------|
| **L1** | `GET /kb/lint?spaceId=` | `data.dataSource=db_snapshot`；`counts` 含 `duplicates`/`missingSources` 等 |
| **L2** | `GET /kb/lint/issue-types` | 返回 `LintIssueTypeVo[]`；`no_summary.webOnly=true` |
| **L3** | `POST /kb/lint/scan?spaceId=` | `kb_lint_issue` 新增待处理行；同空间旧 `status=0` 被清除 |
| **L4** | `GET /kb/lint/issues?issueType=broken_link` | 仅断链工单 |
| **L5** | `PUT /kb/lint/issue/{id}/assign?assigneeId=&priority=1` | 行内 assignee/priority 更新 |
| **L6** | `PUT /kb/lint/issues/batch-status` body `{ids:[...],status:2}` | 多条标记已修复 |
| **L7** | 改 wiki 未 Sync 再 `GET /kb/lint` | 结果与磁盘不一致（证明 DB 快照） |
| **L8** | `POST /kb/wiki-moli/lint-space` 同空间 | 可检出 lint.py 独有项（如 `space_branding`） |

---

## 5. 与 lint.py 分工（KBOPS-10）

| 场景 | 用谁 |
|------|------|
| Sync **之前** CI 门禁 | `python kb/tools/lint.py --strict` |
| 治理页批量修 frontmatter | `lint-space` + `script-fix` |
| Sync **之后** 工单跟踪 | `POST /kb/lint/scan` + `GET /kb/lint/issues` |
| 仅 CLI 检查项 | `near_dup` / `space_branding` / `asym_related` → **不**出现在 Web DB 体检 |

---

## 6. 相关

| 文档 | 内容 |
|------|------|
| [knowledge-script-vs-llm-matrix.md](knowledge-script-vs-llm-matrix.md) | §4 健康体检矩阵 |
| [knowledge-wiki-lint-space.md](knowledge-wiki-lint-space.md) | 文件真值 lint-space |
| [knowledge-e2e-regression.md](knowledge-e2e-regression.md) | 全链路回归 |
