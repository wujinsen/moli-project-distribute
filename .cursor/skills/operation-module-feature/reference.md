# 运维模块 · 文件路径

## 代码

| 层 | 路径 |
|----|------|
| Controller | `moli-user-center-server/.../operation/controller/Operation*Controller.java` |
| Service | `moli-user-center-server/.../operation/service/` + `impl/` |
| 关系读取 | `.../operation/support/OperationRelationQuerySupport.java` |
| 级联 | `.../operation/support/OperationServerCascadeSupport.java` |
| Entity | `moli-user-center-common/.../entity/Operation*.java` |
| VO | `moli-user-center-common/.../vo/Operation*.java` |
| 权限 | `moli-common/.../PermissionConstants.java` |

## 测试

| 类型 | 路径 |
|------|------|
| Controller API | `moli-user-center-server/src/test/java/.../api/Operation*ApiTest.java` |
| Service | `.../operation/service/impl/Operation*Test.java` |
| Support | `.../operation/support/Operation*Test.java` |
| 基类 | `.../testsupport/AbstractApiTest.java`、`ControllerTestSupport.java` |

## 文档

| 文档 | 用途 |
|------|------|
| `docs/design/server-ops-module-roadmap.md` | SVR 任务状态 |
| `docs/api/operation-frontend.md` | 前端对接 + 验收 S* |
| `docs/api/user-center-api-map.md` | 接口清单 |
| `docs/test/operation-*-acceptance.md` | 手测 + CI 命令 |
| `docs/sql/NN_operation_*.sql` | 表/菜单迁移 |

## 近期范例（拓扑/关联）

- `OperationTopologyController` + `OperationTopologyServiceImpl`
- `OperationRelationsController` + `OperationRelationServiceImpl`
- `OperationProjectComponentLinkServiceImpl` + `component-links`
- `OperationRelationsTopologyControllersApiTest`
