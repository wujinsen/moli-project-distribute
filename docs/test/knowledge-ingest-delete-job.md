# 知识库 · Ingest 删除历史批次测试说明

> 模块：`moli-knowledge-server` · API：`DELETE /kb/ingest/jobs/{id}` · 契约：[`KNOWLEDGE_API.md` §9.2.4](../api/KNOWLEDGE_API.md#924-删除-delete-kbingestjobsid)

## 1. 测试范围

| 层级 | 类 | 说明 |
|------|-----|------|
| Service 单元 | `KbIngestServiceImplDeleteJobTest` | 参数校验、功能开关、ACL、软删写入 |
| Controller 契约 | `KbIngestControllerDeleteJobApiTest` | `DELETE /kb/ingest/jobs/{id}` 返回 `MoliResult<Boolean>` |

测试目录：

`moli-knowledge/moli-knowledge-server/src/test/java/com/moli/knowledge/server/`

## 2. 用例清单

### 2.1 `KbIngestServiceImplDeleteJobTest`（6 用例）

| # | 方法 | 类型 | 预期 |
|---|------|------|------|
| 1 | `deleteJob_rejectsWhenIngestDisabled` | Mock | `kb.ingest.enabled=false` → `BaseException` |
| 2 | `deleteJob_rejectsNullId` | Mock | `BaseException` |
| 3 | `deleteJob_rejectsMissingJob` | Mock | 批次不存在 → `BaseException` |
| 4 | `deleteJob_rejectsAlreadyDeleted` | Mock | `is_delete=1` → `BaseException` |
| 5 | `deleteJob_propagatesAclDenied` | Mock | `assertCanEdit` 拒绝 → `BaseException` |
| 6 | `deleteJob_softDeletesWhenEditor` | Mock | `updateById` 且 `is_delete=1` |

### 2.2 `KbIngestControllerDeleteJobApiTest`（1 用例）

| # | 方法 | 类型 | 预期 |
|---|------|------|------|
| 1 | `DELETE_kb_ingest_jobs_id` | Mock | `code=200`；`data=true`；委托 `KbIngestService.deleteJob` |

## 3. 最近一次测试报告

| 项 | 值 |
|----|-----|
| 执行时间 | 2026-06-27 |
| 环境 | JDK 11 · Maven 3.9.16 |
| 命令 | 见 §4 |
| **合计** | **7** 用例 · **7** 通过 · **0** 失败 |

Surefire 报告：`moli-knowledge/moli-knowledge-server/target/surefire-reports/`

## 4. 运行方式

```bash
cd moli-knowledge/moli-knowledge-server
mvn test "-Dtest=KbIngestControllerDeleteJobApiTest,KbIngestServiceImplDeleteJobTest"
```

单类：

```bash
mvn test -Dtest=KbIngestServiceImplDeleteJobTest
mvn test -Dtest=KbIngestControllerDeleteJobApiTest
```

### 依赖说明

| 依赖 | 需要 |
|------|------|
| MySQL / Redis | 否（纯 Mock） |
| Spring 容器 | 否 |

## 5. 手工联调（可选）

```http
DELETE /kb/ingest/jobs/{id}
Authorization: <token>
```

删除后：

- `GET /kb/ingest/jobs` 列表不再含该批次
- `GET /kb/ingest/jobs/{id}` 返回业务错误「批次不存在」
- 若批次曾 `commit`，磁盘 wiki 文件不受影响

## 6. 相关

- API 契约：[`KNOWLEDGE_API.md` §9.0、§9.2.4](../api/KNOWLEDGE_API.md)
- 对称接口：§9.6.3 `DELETE /kb/ingest/templates/{id}`（删批次模板）
- 数据表：`kb_ingest_job`（`docs/sql/08_kb_ingest_workbench.sql`）
