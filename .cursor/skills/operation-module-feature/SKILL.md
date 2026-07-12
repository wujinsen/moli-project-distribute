---
name: operation-module-feature
description: >-
  Implements moli-user-center operation_* features (SVR tasks): Controller,
  Service, QuerySupport, VO, tests, and docs/api updates. Use when adding or
  changing /operation/* APIs, server topology, relations, links, deploy center,
  or user says 运维模块 / operation API / SVR-.
---

# Moli · 运维模块（operation_*）功能开发

> 模块：`moli-user-center` · 表 `operation_*` · HTTP 前缀 `/operation/*`  
> 路线图：[`docs/design/server-ops-module-roadmap.md`](../../docs/design/server-ops-module-roadmap.md)  
> 前端契约：[`docs/api/operation-frontend.md`](../../docs/api/operation-frontend.md)  
> API 地图：[`docs/api/user-center-api-map.md`](../../docs/api/user-center-api-map.md)

## 何时使用

- 新增/改 `Operation*Controller`、`operation_*` 表、SVR 编号任务
- N:N 关联（`links` / `component-links`）、关系 API、拓扑聚合
- 写运维单测、验收文档

**架构/ER 主图**：需要时 `@drawio-diagrams`，不是每个小 API 都画。

## 分层约定

```
Controller  → 权限 @RequiresPermissions + MoliResult
Service     → 业务 + VO 组装
QuerySupport → N:N 读取/计数/反向过滤（关系类必复用 OperationRelationQuerySupport）
Mapper      → MyBatis-Plus；复杂 N:N 用 OperationServerLinkMapper 等
```

**关系类功能**（项目↔服务器↔组件）：
- 读取/计数/列表过滤 → **只写一份** `OperationRelationQuerySupport`
- 统一详情 → `GET /operation/relations/{server|project|component}/{id}`
- 全局图 → `GET /operation/topology`（复用同一 QuerySupport）

**勿再新增** `GET /operation/server/{id}/topology`（SVR-5 已删除）。

## 新 API 检查清单

```
- [ ] Entity / VO（moli-user-center-common/.../vo/）
- [ ] Service 接口 + Impl
- [ ] Controller + PermissionConstants
- [ ] 列表反向过滤：实体 @TableField(exist=false) + QuerySupport.apply*Filter
- [ ] 删除级联：OperationServerCascadeSupport 扩展
- [ ] docs/api/user-center-api-map.md
- [ ] docs/api/operation-frontend.md（TypeScript 类型 + 验收表行）
- [ ] docs/test/operation-*-acceptance.md（手测 + mvn 命令）
- [ ] 单测：ServiceImplTest + *ControllersApiTest
- [ ] SQL：@sql-migration-baseline（若有表/菜单）
- [ ] docs/design/*-roadmap.md 任务状态
```

## N:N / links API 对称模式

| 实体 | GET | PUT |
|------|-----|-----|
| 服务器 | `/operation/server/{id}/links` | 全量替换 projectIds + componentIds |
| 项目 | `/operation/project/{id}/links` | 全量替换 serverIds |
| 项目依赖组件 | `/operation/project/{id}/component-links` | 全量替换 componentIds |
| 组件 | `/operation/component/{id}/links` | 全量替换 serverIds |

PUT 权限：`operation:{entity}:edit` + `list`（Logical.AND）。

## 测试

**Controller**（与 `OperationControllersApiTest` 同风格）：

```java
@RunWith(MockitoJUnitRunner.class)
public class OperationXxxControllersApiTest extends AbstractApiTest {
    @InjectMocks private OperationXxxController controller;
    @Mock private OperationXxxService service;
    // assertSuccess + verify(service)
}
```

**Service**：Mock Mapper + `MybatisPlusTestSupport.initAll()`（`@Before`）。

**批量跑**：

```powershell
mvn -pl moli-user-center-server "-Dtest=Operation*Relation*,Operation*Topology*,OperationProjectComponentLink*,Operation*ControllersApiTest" test
```

## 文档与验收

- 验收文档放 `docs/test/`，索引进 `docs/test/README.md`
- 前端不在本仓（meiling-ui）；只维护 `docs/api/operation-frontend.md`
- 设计稿放 `docs/design/`，大功能更新 `server-ops-module-roadmap.md`

## 权限速查

| 资源 | list | edit |
|------|------|------|
| server | `operation:server:list` | `operation:server:edit` |
| project | `operation:project:list` | `operation:project:edit` |
| component | `operation:component:list` | `operation:component:edit` |
| 拓扑 | `operation:server:list` | — |
| relations | `operation:project:list`（当前实现） | — |

## 详细文件路径

见 [reference.md](reference.md)。

## 用户怎么说

```
@operation-module-feature 做 SVR-28c 后端还缺的部分
```

```
加 operation 关联 API，补单测和 operation-frontend 文档
```
