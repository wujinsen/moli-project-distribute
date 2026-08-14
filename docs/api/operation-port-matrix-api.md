# 端口矩阵管理 · HTTP API 契约（SVR-21）

> **状态**：已实现 · **更新**：2026-07-11  
> **服务**：`moli-user-center-server` · 前缀 `/operation/port-matrix`  
> **设计**：[`operation-port-matrix-config.md`](../design/operation-port-matrix-config.md)  
> **审计只读**（不变）：`GET /operation/audit/port-matrix` · 权限 `operation:project:list`

---

## 1. 权限

| perm_code | 用途 |
|-----------|------|
| `operation:port-matrix:list` | 菜单 406、列表、详情 |
| `operation:port-matrix:add` | `POST` 新增（需与 `list` AND） |
| `operation:port-matrix:edit` | `PUT` 更新（需与 `list` AND） |
| `operation:port-matrix:remove` | `DELETE` 删除（需与 `list` AND） |

菜单：`sys_menu.id = 406`，父级 400「运营管理」。  
迁移：[`24_operation_port_matrix.sql`](../sql/24_operation_port_matrix.sql)。

---

## 2. 数据类型

### 2.1 `OperationPortMatrixVo`（列表/详情）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | 主键 |
| `matrixKey` | string | 如 `user-center` |
| `displayName` | string | 展示名 |
| `expectedPort` | string | 期望端口 |
| `aliases` | string[] | 别名列表（不含 matrixKey） |
| `sortOrder` | int | 排序 |
| `enabled` | boolean | 是否启用 |
| `source` | string | 来源 |
| `remark` | string | 备注 |
| `createTime` / `updateTime` | string | ISO 或 `yyyy-MM-dd HH:mm:ss` |

### 2.2 `OperationPortMatrixSaveRequest`（新增/更新）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | long | 更新必填 | 新增不传 |
| `matrixKey` | string | 是 | 新增必填；**更新不可改** |
| `displayName` | string | 否 | |
| `expectedPort` | string | 是 | `1..65535` |
| `aliases` | string[] | 否 | 全量替换；去重、归一化后存储 |
| `sortOrder` | int | 否 | 默认 0 |
| `enabled` | boolean | 否 | 默认 true |
| `source` | string | 否 | 默认 `ops-console` |
| `remark` | string | 否 | |

### 2.3 列表查询参数

与现有运维 CRUD 一致，继承分页基类：

| 参数 | 说明 |
|------|------|
| `pageNum` / `pageSize` | 分页 |
| `matrixKey` | 模糊 |
| `displayName` | 模糊 |
| `enabled` | `true` / `false` / 不传=全部 |

---

## 3. 接口一览

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/operation/port-matrix/list` | `list` | 分页列表 |
| GET | `/operation/port-matrix/{id}` | `list` | 单条详情 |
| POST | `/operation/port-matrix` | `add` + `list` | 新增 |
| PUT | `/operation/port-matrix` | `edit` + `list` | 更新 |
| DELETE | `/operation/port-matrix/{ids}` | `remove` + `list` | 批量删除，逗号分隔或路径数组 |

**说明**：矩阵变更后服务端自动 `refresh` 内存缓存，**无需**单独刷新接口。

---

## 4. 接口详情

### 4.1 列表

```http
GET /operation/port-matrix/list?pageNum=1&pageSize=10&matrixKey=user
```

**响应** `MoliResult<PageRes<OperationPortMatrixVo>>`

```json
{
  "code": 200,
  "data": {
    "total": 8,
    "list": [
      {
        "id": 501,
        "matrixKey": "user-center",
        "displayName": "用户中心",
        "expectedPort": "8888",
        "aliases": ["user-center", "moli-user-center", "user-center-server", "moli-server"],
        "sortOrder": 20,
        "enabled": true,
        "source": "migration:java-default",
        "remark": null
      }
    ]
  }
}
```

### 4.2 详情

```http
GET /operation/port-matrix/501
```

**响应** `MoliResult<OperationPortMatrixVo>`

### 4.3 新增

```http
POST /operation/port-matrix
Content-Type: application/json
```

```json
{
  "matrixKey": "minio",
  "displayName": "MinIO",
  "expectedPort": "9000",
  "aliases": ["minio", "minio-api"],
  "sortOrder": 90,
  "enabled": true,
  "remark": "对象存储 API 端口"
}
```

**响应** `MoliResult<Boolean>`，`data: true`

**错误示例**（业务码以 `BaseException` 为准）：

| 场景 | message 示例 |
|------|----------------|
| matrixKey 重复 | `matrixKey 已存在：minio` |
| 别名冲突 | `别名已被占用：moli-server` |
| 端口非法 | `expectedPort 须在 1..65535` |

### 4.4 更新

```http
PUT /operation/port-matrix
```

```json
{
  "id": 501,
  "matrixKey": "user-center",
  "displayName": "用户中心",
  "expectedPort": "9080",
  "aliases": ["moli-server", "user-center-server"],
  "enabled": true
}
```

- `matrixKey` 必须与库内一致，**不可改名**。
- `aliases` 为**全量替换**（传 `[]` 表示清空别名，仅保留 key 匹配）。

### 4.5 删除

```http
DELETE /operation/port-matrix/501,502
```

**响应** `MoliResult<Boolean>`

---

## 5. 与审计 API 的关系

`GET /operation/audit/port-matrix` **不迁移**到本 Controller，仍由 `OperationAuditController` 提供。

变更点（实现后）：

| 字段 | 原值 | 新值 |
|------|------|------|
| `matrix[].source` | 固定 `docs/ops/production-checklist.md` | DB 行 `source` |
| `matrix[].key` / `expectedPort` | Java 硬编码 | DB 启用行 |
| `items[].portMatchStatus` | 算法不变 | 数据源改为 `OperationPortMatrixProvider` |

**联调验证**：更新 user-center 期望端口后，立即调用审计 API，无需重启 user-center。

---

## 6. 前端 API 模块（建议）

```typescript
// src/api/operation.ts
const OP = '/operation'

export const listPortMatrixApi = (params: PortMatrixQuery) =>
  request<PageRes<OperationPortMatrix>>(`${OP}/port-matrix/list`, { method: 'GET', params })

export const getPortMatrixApi = (id: number | string) =>
  request<OperationPortMatrix>(`${OP}/port-matrix/${id}`, { method: 'GET' })

export const addPortMatrixApi = (body: PortMatrixSaveRequest) =>
  request<boolean>(`${OP}/port-matrix`, { method: 'POST', data: body })

export const updatePortMatrixApi = (body: PortMatrixSaveRequest) =>
  request<boolean>(`${OP}/port-matrix`, { method: 'PUT', data: body })

export const removePortMatrixApi = (ids: string) =>
  request<boolean>(`${OP}/port-matrix/${ids}`, { method: 'DELETE' })
```

类型定义见 [`operation-frontend.md`](operation-frontend.md) §14。

---

## 7. 错误码与日志

| 操作 | `@MoliLog` title |
|------|------------------|
| POST | 新增端口矩阵 |
| PUT | 更新端口矩阵 |
| DELETE | 删除端口矩阵 |

参数校验失败走全局 `GlobalExceptionHandler`；Shiro 无权限 → 401/403。

---

## 8. 相关

- 设计：[`operation-port-matrix-config.md`](../design/operation-port-matrix-config.md)
- 验收：[`operation-port-matrix-acceptance.md`](../test/operation-port-matrix-acceptance.md)
- 前端：[`operation-frontend.md`](operation-frontend.md) §14
- 审计：[`operation-frontend.md`](operation-frontend.md) §6.1
- API 地图：[`user-center-api-map.md`](user-center-api-map.md)（实现后补 §4 条目）
