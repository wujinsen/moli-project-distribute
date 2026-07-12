# 用户中心 · 表结构设计

> 更新：2026-07-12 · 基线脚本 [`scripts/moli.sql`](../../scripts/moli.sql)  
> 实体源码：`moli-user-center-common/.../domain/entity/`  
> RBAC 字段说明：[`../zh-CN/RBAC.md`](../zh-CN/RBAC.md) §2

---

## 1. 设计目标

用户中心数据分为 **RBAC 核心**（`sys_*`）与 **运维资产**（`operation_*`）两套域，共用 MySQL 库 `moli`。

| 目标 | 说明 |
|------|------|
| RBAC | 用户—角色—菜单/动作；部门、岗位、字典 |
| 多系统门户 | 系统注册、用户—系统授权、INTERNAL/EXTERNAL |
| 审计 | 登录日志、操作日志（可清理，不参与业务 FK） |
| 运维演示 | 平台/服务器/项目/组件及部署关系 |

通用约定：`bigint` 雪花主键；`create_id/create_time/update_id/update_time`；`is_delete` 逻辑删除；**utf8mb4**。

导入含中文种子数据须 `--default-character-set=utf8mb4`，禁止 PowerShell 管道导入 — 见 [`README.md`](README.md)。

---

## 2. 表清单（26 张）

### 2.1 RBAC 与组织（14）

| 表 | 说明 |
|----|------|
| `sys_user` | 用户账号、密码盐、部门、状态 |
| `sys_dept` | 部门树（`parent_id`） |
| `sys_post` | 岗位 |
| `sys_user_post` | 用户—岗位 N:N |
| `sys_role` | 角色 |
| `sys_user_role` | 用户—角色 N:N |
| `sys_menu` | 菜单/目录/按钮；`perms`、`menu_type`、`path`、`component` |
| `sys_role_menu` | 角色—菜单 N:N |
| `sys_action` | 细粒度动作码（挂 `menu_id`） |
| `sys_role_action` | 角色—动作 N:N |
| `sys_dict_type` / `sys_dict_data` | 字典 |
| `sys_system` | 业务系统注册（`sso_mode`、`system_group`） |
| `sys_user_system` | 用户可访问系统 |

### 2.2 审计（2）

| 表 | 说明 |
|----|------|
| `sys_login_log` | 登录成功/失败 |
| `sys_operation_log` | 操作审计（模块、方法、参数、耗时） |

### 2.3 运维资产（10）

| 表 | 说明 |
|----|------|
| `operation_platform_info` | 平台 |
| `operation_server_info` | 服务器 |
| `operation_server_project` | 服务器—项目 N:N |
| `operation_project_deploy_info` | 项目部署信息 |
| `operation_project_component` | 项目—组件依赖 N:N（SVR-26a；拓扑 `depends_on` 边） |
| `operation_server_component` | 服务器—组件 N:N |
| `operation_component_deploy_info` | 组件部署信息（含 `server_id`） |
| `operation_task` | 运维异步任务（deploy/upload/command/health_probe）；含可选 `project_id` |
| `operation_port_matrix` | 端口矩阵主表（SVR-21） |
| `operation_port_matrix_alias` | 端口矩阵别名（全局唯一） |

> 迁移：端口矩阵见 [`24_operation_port_matrix.sql`](24_operation_port_matrix.sql)；项目依赖见 [`29_operation_project_component.sql`](29_operation_project_component.sql)（已合并进 `moli.sql` 基线）。拓扑图菜单 id **407** 见 [`28_operation_topology_menu.sql`](28_operation_topology_menu.sql)（已合并进 `moli.sql` 基线）。

---

## 3. ER 关系（逻辑）

```
sys_dept ◄── sys_user ──► sys_user_role ──► sys_role ──► sys_role_menu ──► sys_menu
                │              │                │
                │              │                └──► sys_role_action ──► sys_action ──► sys_menu
                ├── sys_user_post ──► sys_post
                └── sys_user_system ──► sys_system

operation_platform_info ──► operation_server_info ──► operation_server_project ──► operation_project_deploy_info
                                              └──► operation_server_component ──► operation_component_deploy_info
operation_project_deploy_info ──► operation_project_component ──► operation_component_deploy_info
```

可视化 RBAC 子集：[`../diagrams/moli-rbac-model.drawio`](../diagrams/moli-rbac-model.drawio) / [PNG](../diagrams/png/moli-rbac-model.png)。

---

## 4. 关键字段

### 4.1 `sys_user`

| 字段 | 说明 |
|------|------|
| `user_name` | 登录名（唯一业务键） |
| `password` + `salt` | SHA-256 迭代 15 次 |
| `dept_id` | 所属部门 |
| `status` | 0 正常 / 1 锁定 |
| `is_delete` | 逻辑删除 |

### 4.2 `sys_menu`

| 字段 | 说明 |
|------|------|
| `menu_type` | M 目录 / C 菜单 / F 按钮 |
| `perms` | 权限标识，如 `system:user:list` |
| `path` / `component` / `route_name` | 前端路由 |
| `parent_id` | 树结构 |

### 4.3 `sys_system`

| 字段 | 说明 |
|------|------|
| `system_code` | 唯一编码 |
| `sso_mode` | `INTERNAL`（内置菜单）/ `EXTERNAL`（外链 SSO） |
| `system_group` | 门户分组：platform/business/data/tech/ops |
| `base_url` / `entry_path` | EXTERNAL 跳转 |

### 4.4 `sys_action`

| 字段 | 说明 |
|------|------|
| `action_code` | 如 `system:user:add` |
| `menu_id` | 所属页面 |
| `status` | 启用/停用 |

### 4.5 `operation_project_component`（SVR-26a）

| 字段 | 说明 |
|------|------|
| `project_id` | 项目 ID（逻辑外键 → `operation_project_deploy_info`） |
| `component_id` | 组件 ID（逻辑外键 → `operation_component_deploy_info`） |
| `remark` | 依赖说明，如「业务库」「会话缓存」 |

唯一约束：`uk_operation_project_component(project_id, component_id)`。删除项目/组件时由 `OperationServerCascadeSupport` 级联清理。

---

## 5. DDL 来源与增量

| 场景 | 操作 |
|------|------|
| **新环境** | `scripts/init-db.ps1` 或导入 `scripts/moli.sql` |
| **秒杀表** | 追加 `docs/sql/02_seckill_schema.sql` |
| **知识库表** | 追加 `docs/sql/03_knowledge_schema.sql` |
| **历史 patch** | 已合并进 `moli.sql`（含 21/22 部署中心 + 23 组件 `server_id` + **28 拓扑菜单 407** + **29 项目依赖表**）；老库按 [`sql-migration-order.md`](../ops/sql-migration-order.md) 追 17→29 |

---

## 6. 种子数据快照（导出时）

| 表 | 行数 | 备注 |
|----|------|------|
| `sys_user` | 33 | 含演示账号 |
| `sys_menu` | 32 | 含 RBAC + 运维（含拓扑图 407）+ 知识库菜单 |
| `sys_role` | 10 | |
| `sys_system` | 35 | 多系统门户演示 |
| `sys_action` | 38 | 动作码目录 |

完整快照见 [`README.md`](README.md) §表行数。

---

## 7. 相关文档

- [`user-center-overview.md`](../design/user-center-overview.md)
- [`portal-system-group.md`](../design/portal-system-group.md)
- [`user-center-api-map.md`](../api/user-center-api-map.md)
