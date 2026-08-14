---


title: RBAC 权限模型
slug: rbac-权限模型
type: concept
status: active
tags: [权限, RBAC, Shiro, 安全]
sources:
  - docs/zh-CN/RBAC.md
  - docs/api/user-center-api-map.md
  - moli-user-center/moli-user-center-server/
related: [用户中心, 认证与会话机制, 权限管理操作指南]
created: 2026-06-22
updated: 2026-06-22
---

# RBAC 权限模型

茉莉项目的权限基础，由 [[用户中心]] 实现。认证授权用 Apache Shiro，会话存 Redis（见 [[认证与会话机制]]）。

## 模型

```
用户 SysUser ──N:N(sys_user_role)──▶ 角色 SysRole ──┬─N:N(sys_role_menu)──▶ 菜单 SysMenu
                                                    └─N:N(sys_role_action)─▶ 动作 SysAction
部门 SysDept（组织架构，独立于角色授权）
```

## 双轨权限（重要）

授权集合由两条线**并集**而成：

1. **菜单 perms**：`menuType=C`（页面）且 `perms` 非空的菜单 → 进授权集。
2. **动作码 sys_action**：`sys_role_action` 关联的、启用的 `SysAction.permCode` → 进授权集。
3. `menuType=F`（按钮菜单）**已废弃**，按钮级权限改用 `sys_action`。

> 这是文档校准点：`docs/zh-CN/RBAC.md` 里把接口权限写成"预留"，但实际在 `docs/api/user-center-api-map.md` + 源码中已经在用 `@RequiresPermissions` 强制校验，以代码为准。

## 权限标识规范

格式 `模块:资源:操作`，例：`system:user:list`、`system:role:assignPerm`。常量定义在 `moli-distribute-common` 的 `PermissionConstants`。

| 概念 | 表 | 说明 |
|------|----|------|
| 用户 | `sys_user` | 登录账号，绑定角色 |
| 角色 | `sys_role` | 权限载体，绑菜单 + 动作 |
| 菜单 | `sys_menu` | M目录/C页面/F按钮(废弃)，`perms` 定义页面权限 |
| 动作 | `sys_action` | 细粒度动作权限码 `perm_code` |
| 部门 | `sys_dept` | 组织架构 |

关联表：`sys_user_role`、`sys_role_menu`、`sys_role_action`。

## 超管

`superadmin` / `admin` 直接拥有 `*:*:*`、全部菜单、全部系统准入（对普通管理员隐藏）。

## 接口校验

```java
@RequiresPermissions(PermissionConstants.SYSTEM_USER_LIST)
@RequiresPermissions(value = {SYSTEM_USER_ADD, SYSTEM_USER_LIST}, logical = Logical.AND)
```

授权变更后清 `shiro:cache` 缓存（`PermissionAuthUtils.clearUserAuthorizationCache`）。

> 列级/字段级数据权限**未实现**；BI 扩展思路见 `moli-knowledge/kb/wiki/security/字段级数据权限设计.md`。

## 相关

- 怎么管用户/角色/授权：[[权限管理操作指南]]
- 登录与会话怎么走：[[认证与会话机制]]