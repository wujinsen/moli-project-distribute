# 运营管理 · 任务历史按项目分组验收（DC-4）

> **任务 ID**：DC-4  
> **模块**：`moli-user-center-server` `:8888` + `meiling-ui` `TaskHistoryView.vue`  
> **契约**：[`docs/api/operation-deploy-api.md`](../api/operation-deploy-api.md) §`GET /operation/task/groups` · [`docs/api/p3-optional-backend-handoff.md`](../api/p3-optional-backend-handoff.md) §1  
> **自动化**：`mvn -pl moli-user-center/moli-user-center-server -am test "-Dtest=OperationTaskServiceImplTest,OperationRemoteDeployControllersApiTest" "-Dsurefire.failIfNoSpecifiedTests=false"`

---

## 0. 前置条件

| # | 项 | 期望 |
|---|-----|------|
| P0 | DB | `operation_task` 表存在；至少有 2 个不同 `project_id` 的任务样本 + 若干 `project_id IS NULL` 任务（如 `health_probe`） |
| P1 | user-center | `:8888` 已部署含 DC-4 代码（`GET /operation/task/groups` 可访问） |
| P2 | 权限 | 联调账号含 `operation:server:list` |
| P3 | 项目台账 | `operation_project_deploy_info` 中对应 `project_id` 有 `project_name`（用于组头展示） |

**造数建议**（无历史任务时）：

1. 部署中心对项目 A 执行 1 次 `restart`（`projectId` 有值）
2. 对项目 B 执行 1 次 `upload` 或 `command`
3. 运维 → 全站探活 `POST /operation/health/probe-all`（通常 `projectId` 为空）

---

## 1. HTTP 接口（后端）

> 以下请求经网关或直连 `:8888`，Header 带登录 Token。路径前缀以实际为准（直连无 `/UserServer` 前缀）。

| ID | 场景 | 请求 | 期望 |
|----|------|------|------|
| GR-1 | 默认分组列表 | `GET /operation/task/groups` | `code=200`；`data.total` 为**项目组数**；`data.list[]` 每组含 `taskCount`、`tasks[]` |
| GR-2 | 分页在组维度 | `GET /operation/task/groups?pageNum=1&pageSize=1` | `list.length=1`；`total` ≥ 1；翻 `pageNum=2` 返回另一组（若有） |
| GR-3 | 组内条数上限 | `GET /operation/task/groups?tasksPerGroup=1` | 每组 `tasks.length ≤ 1`；`taskCount` 可大于 1 |
| GR-4 | 未关联项目组 | 样本含 `project_id` 为空的任务 | 存在 `projectId=null` 的组；`projectName` 为 null |
| GR-5 | 项目名解析 | 组 `projectId` 在台账存在 | `projectName` 与台账 `project_name` 一致 |
| GR-6 | 聚合计数 | 组内含 pending/running/success/failed 混合任务 | `runningCount` = pending+running；`failedCount`/`successCount` 与状态一致；`cancelled` 仅计入 `taskCount` |
| GR-7 | 最近时间 | 组内多条任务 | `latestCreateTime` = 组内最大 `createTime` |
| GR-8 | 组内排序 | 组内 `tasks` 多条 | 按 `createTime` **降序** |
| GR-9 | 按项目筛选 | `GET /operation/task/groups?projectId={id}` | 仅 1 组（或 0）；`projectId` 匹配 |
| GR-10 | 按状态筛选 | `GET /operation/task/groups?status=running` | 仅返回 status=running 的任务参与分组；空库时 `total=0` |
| GR-11 | 按类型筛选 | `GET /operation/task/groups?taskType=deploy` | 仅 deploy 任务参与分组 |
| GR-12 | 不含大日志 | 任意组内 `tasks[]` | **无** `task_log` / `logChunk` 字段 |
| GR-13 | 与 flat list 不冲突 | 同时调 `GET /operation/task/list` | 扁平分页语义不变；`total` 仍为**任务数**非组数 |
| GR-14 | 无权限 | 无 `operation:server:list` 的账号 | 403 |

**GR-1 响应结构抽检**：

```json
{
  "code": 200,
  "data": {
    "total": 2,
    "pageNum": 1,
    "pageSize": 10,
    "list": [
      {
        "projectId": 731708402010423296,
        "projectName": "moli-user-center",
        "taskCount": 3,
        "runningCount": 0,
        "failedCount": 1,
        "successCount": 2,
        "latestCreateTime": "2026-07-13T10:00:00.000+00:00",
        "tasks": [
          { "id": 99, "taskType": "deploy", "status": "success", "projectId": 731708402010423296 }
        ]
      }
    ]
  }
}
```

**与 flat list 对照**（GR-13）：

```http
GET /operation/task/list?projectId={id}&pageNum=1&pageSize=100
```

组内 `taskCount` 应与 flat list 同条件下 `total` 一致（无 `status` 额外筛选时）。

---

## 2. 前端（meiling-ui · TaskHistoryView）

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| FE-1 | 视图切换 | 任务历史页切换「平铺 / 按项目分组」 | 平铺仍调 `list`；分组调 `groups` |
| FE-2 | 分组头展示 | 分组视图首屏 | 显示项目名（null →「未关联项目」）、`taskCount`、进行中/失败计数、最近时间 |
| FE-3 | 手风琴展开 | 点击某项目组 | 展示组内任务行：服务器、类型、状态、日志入口 |
| FE-4 | 部署中心跳入 | 部署中心选项目 →「任务历史」`?projectId=` | 分组视图仅 1 组或与筛选一致 |
| FE-5 | 组分页 | 项目组 > `pageSize` | 翻页 `pageNum` 更换组，不串组 |
| FE-6 | 组内补全 | `taskCount > tasks.length` | 可提供「查看全部」链到 `list?projectId=` |
| FE-7 | i18n | 切换 zh/en/ja | 「未关联项目」等文案正确 |

---

## 3. 自动化用例映射

| 验收 ID | 单测 / API 测试 |
|---------|-----------------|
| GR-1～GR-8 | `OperationTaskServiceImplTest#listGroups_*` |
| GR-2～GR-3 | `listGroups_paginates_groups_and_limits_tasks_per_group` |
| GR-4～GR-6 | `listGroups_groups_by_project_and_computes_aggregates` |
| GR-9 | `listGroups_single_project_filter_returns_one_group` |
| GR-12 | `toListVo` 路径；list/groups 均 `select` 排除 `task_log` |
| GR-14 | Shiro 集成环境手测；Controller 单测不覆盖权限 |
| HTTP 入参转发 | `GET_operation_task_groups_forwards_query_params` |

---

## 4. 执行命令

```powershell
# 单测（DC-4 相关）
cd D:\work\moli_project\moli-project-distribute
mvn -pl moli-user-center/moli-user-center-server -am test `
  "-Dtest=OperationTaskServiceImplTest,OperationRemoteDeployControllersApiTest" `
  "-Dsurefire.failIfNoSpecifiedTests=false" -q

# 部署中心全量（含 task）
mvn -pl moli-user-center/moli-user-center-server -am test `
  "-Dtest=Operation*Remote*,Operation*Task*,Operation*Upload*,OperationSsh*,OperationShellGuard*" `
  "-Dsurefire.failIfNoSpecifiedTests=false" -q
```

---

## 5. 相关

| 文档 | 用途 |
|------|------|
| [operation-deploy-center-acceptance.md](operation-deploy-center-acceptance.md) §2 TASK-* | 任务轮询 / flat list 基线 |
| [p3-optional-backend-handoff.md](../api/p3-optional-backend-handoff.md) §1.6 | 前端验收勾选 |
| [operation-frontend.md](../api/operation-frontend.md) §11.2.1 | TypeScript 类型 |
