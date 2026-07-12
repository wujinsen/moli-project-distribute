# 运营管理 · 端口矩阵验收用例（SVR-21）

> 模块：`moli-user-center-server` + `meiling-ui`  
> 契约：[`docs/api/operation-port-matrix-api.md`](../api/operation-port-matrix-api.md) · 前端 §14 [`operation-frontend.md`](../api/operation-frontend.md)  
> 设计：[`docs/design/operation-port-matrix-config.md`](../design/operation-port-matrix-config.md)  
> 自动化：`mvn -Dtest=OperationPortMatrix*Test test`（user-center-server）

---

## 0. 前置条件

| # | 项 | 期望 |
|---|-----|------|
| P0 | DB 已执行 `docs/sql/24_operation_port_matrix.sql`（在 `23_*` 之后） | `SHOW TABLES LIKE 'operation_port_matrix%'` 返回 2 表；种子 8 条 |
| P1 | user-center 已部署含 SVR-21 的代码 | 含 `OperationPortMatrixProvider`、`OperationPortMatrixController` |
| P2 | 用户已**重新登录** | 侧栏「运营管理」下可见菜单 **406「端口矩阵」** |
| P3 | 角色权限 | `operation:port-matrix:list`；写操作另需 `add` / `edit` / `remove`（均与 `list` AND） |
| P4 | 审计只读（SVR-7 保持） | `GET /operation/audit/port-matrix` 仍用 `operation:project:list`，与矩阵 CRUD 权限分离 |
| P5 | 台账种子（可选） | `operation_project_deploy_info` 含 `moli-server` + 端口 `9080`（demo 种子 id 401） |

**快速自检 SQL**

```sql
SELECT COUNT(*) FROM operation_port_matrix WHERE enabled = 1;          -- 期望 >= 8
SELECT matrix_key, expected_port FROM operation_port_matrix ORDER BY sort_order;
SELECT id, menu_name, perms FROM sys_menu WHERE id = 406;
```

---

## 1. 矩阵 CRUD API

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| API-1 | 列表 | `GET /operation/port-matrix/list?pageNum=1&pageSize=10` | 200；`total>=8`；含 `user-center` / `expectedPort=8888` / `aliases` 数组 |
| API-2 | 详情 | `GET /operation/port-matrix/{id}`（user-center 对应行） | 200；`aliases` 含 `moli-server` |
| API-3 | 新增 | `POST /operation/port-matrix` body `{ matrixKey:"minio", displayName:"MinIO", expectedPort:"9000", aliases:["minio"] }` | 200；列表可见新行 |
| API-4 | 更新端口 | `PUT` 将 `user-center` 的 `expectedPort` 改为 `9080` | 200；**无需重启** user-center |
| API-5 | 别名全量替换 | `PUT` 传 `aliases: ["moli-server","user-center-server"]`（不含其它旧别名） | 保存后详情仅含传入别名 |
| API-6 | 删除 | `DELETE /operation/port-matrix/{minioId}` | 200；列表不再出现 |
| API-7 | matrixKey 重复 | `POST` 重复 `matrixKey=user-center` | 4xx；`matrixKey 已存在` |
| API-8 | 别名冲突 | `POST` 新行别名含已被占用的 `moli-server` | 4xx；`别名已被占用` |
| API-9 | 端口非法 | `expectedPort=99999` 或 `abc` | 4xx；`expectedPort 须在 1..65535` |
| API-10 | 改名禁止 | `PUT` 修改已有行的 `matrixKey` | 4xx；`matrixKey 不可修改` |
| API-11 | 无 list 权限 | 无 `operation:port-matrix:list` 调 `GET /list` | 403 |
| API-12 | 无 edit 权限 | 无 `operation:port-matrix:edit` 调 `PUT` | 403 |

---

## 2. 审计与台账联动（SVR-7 + SVR-21）

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| AUD-1 | 审计矩阵来源 | `GET /operation/audit/port-matrix` | `matrix` 数组来自 DB（`source` 为行内字段，非写死 checklist 路径） |
| AUD-2 | 种子 demo 不符 | 台账 `moli-server` + `9080`（期望仍为 8888） | `portMatchStatus=2`（MISMATCH）；`expectedPort=8888` |
| AUD-3 | 改矩阵即时生效 | API-4 将 user-center 改为 `9080` 后**立即**再调 AUD-1 | 同一台账项变 `portMatchStatus=1`（MATCH）；**不重启** |
| AUD-4 | MySQL 一致 | 组件台账 `MySQL` + `3306` | `MATCH` |
| AUD-5 | 未映射 | 台账名 `moli-admin` + 任意端口 | `UNMAPPED`（0） |
| AUD-6 | 跳过 | 端口 `-` 或空 | `SKIPPED`（3） |
| AUD-7 | 新增别名 | 为 user-center 增加别名 `moli-uc`；台账项目名 `moli-uc:8888` | `MATCH` |
| AUD-8 | 停用条目 | 将 `mysql` 行 `enabled=false` 保存 | 台账 `MySQL:3306` 变 `UNMAPPED` |
| AUD-9 | 项目列表 badge | `GET /operation/project/list` | VO 含 `portMatchStatus` / `expectedPort`，与 AUD 逻辑一致 |
| AUD-10 | 组件列表 badge | `GET /operation/component/list` | 同上 |

---

## 3. 缓存与兜底

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| CACHE-1 | CRUD 刷新 | 任意矩阵增删改后立刻调 AUD-1 | 结果已反映变更（Provider `refresh`） |
| CACHE-2 | 空表回退 | 清空 `operation_port_matrix` + `operation_port_matrix_alias` 后**重启** user-center | 启动日志 WARN：`无启用记录，端口审计回退内置默认矩阵`；AUD-1 仍返回 8 条内置矩阵；审计仍可用 |

> CACHE-2 为破坏性用例，仅在测试库执行。

---

## 4. 前端页面（meiling-ui）

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| FE-1 | 菜单 | 重新登录 → 运营管理 | 可见「端口矩阵」（406） |
| FE-2 | 列表 | 打开端口矩阵页 | 表格：matrixKey、展示名、期望端口、别名 tags、启用状态 |
| FE-3 | 新增 | 点新增 → 填 matrixKey/端口/别名 Tag → 保存 | Toast 成功；列表刷新 |
| FE-4 | 编辑 | 编辑 user-center 期望端口 | 保存后列表更新；matrixKey 字段只读 |
| FE-5 | 删除 | 删除测试行（如 minio） | 二次确认后消失 |
| FE-6 | 权限按钮 | 无 `add`/`edit`/`remove` 的角色 | 对应按钮隐藏或禁用 |
| FE-7 | 审计入口 | 项目管理 → 端口审计弹窗 | 有「管理端口矩阵」链接 → 跳转 `/operation/port-matrix` |
| FE-8 | 端口审计弹窗 | 端口矩阵页或项目页打开审计 | 展示 matrix + items 统计（matched/mismatched/…） |
| FE-9 | i18n | 切换 zh / en / ja | 端口矩阵、别名提示、启用筛选文案正确 |

---

## 5. P0 冒烟（5 分钟）

最小路径，发版前建议全勾：

| # | 步骤 | 通过 |
|---|------|------|
| S1 | 执行/确认 `24_operation_port_matrix.sql`；`SELECT COUNT(*) FROM operation_port_matrix` ≥ 8 | ☐ |
| S2 | 重启 user-center；超级管理员重新登录 | ☐ |
| S3 | `GET /operation/port-matrix/list` 返回 8 条种子 | ☐ |
| S4 | `GET /operation/audit/port-matrix` → `moli-server:9080` 为 MISMATCH（期望 8888） | ☐ |
| S5 | 前端改 user-center 期望端口为 `9080` 保存 → 再开审计，同一项变 MATCH | ☐ |
| S6 | 前端「端口矩阵」菜单可打开且无 403 | ☐ |

---

## 6. 自动化测试（CI / 本地）

### 后端（Mock，无需 MySQL）

```bash
cd moli-user-center/moli-user-center-server
mvn -Dtest=OperationPortMatrixProviderTest,OperationPortMatrixSnapshotTest,OperationPortMatrixServiceImplTest,OperationPortMatrixControllersApiTest test
```

| 测试类 | 覆盖点 |
|--------|--------|
| `OperationPortMatrixSnapshotTest` | 内置默认矩阵：MATCH / MISMATCH / UNMAPPED / SKIPPED |
| `OperationPortMatrixProviderTest` | DB 加载 + 空表回退 + 别名匹配 |
| `OperationPortMatrixServiceImplTest` | CRUD 校验、别名冲突、matrixKey 不可改、refresh 调用 |
| `OperationPortMatrixControllersApiTest` | Controller 200 + 权限注解挂载 |

### 全量运维回归（可选）

```bash
cd moli-user-center/moli-user-center-server
mvn -Dtest=Operation*Test test
```

---

## 7. 缺陷记录模板

| 日期 | 用例 ID | 现象 | 环境 | 处理人 |
|------|---------|------|------|--------|
| | | | | |

---

## 8. 相关

- 部署中心验收：[`operation-deploy-center-acceptance.md`](operation-deploy-center-acceptance.md)
- 用户中心测试索引：[`user-center.md`](user-center.md)
- 上线冒烟：[`release-smoke-checklist.md`](release-smoke-checklist.md)
- SQL 顺序：[`docs/ops/sql-migration-order.md`](../ops/sql-migration-order.md)
