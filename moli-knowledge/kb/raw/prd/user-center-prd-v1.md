# 用户中心 PRD v1

> 状态：基线已实现 · 更新：2026-06-25  
> 模块：`moli-user-center` · 服务名 `user-center-server`

## 1. 背景与目标

茉莉微服务以用户中心为**统一身份与权限底座**。所有业务服务（订单、BI、知识库等）经网关访问，会话由 user-center 签发，业务侧通过 Shiro Starter + Dubbo 校验，不在本地重复登录。

**目标**

- 提供企业级 RBAC（用户/角色/菜单/按钮/动作双轨权限）
- 支持多业务系统门户（INTERNAL 内置菜单 / EXTERNAL 跳转 SSO）
- 对外暴露 Dubbo 契约供微服务拉用户与权限
- 提供审计能力（登录日志、操作日志）

**非目标（v1 不做）**

- 多租户 SaaS 隔离
- OAuth2 / 开放 API 第三方授权（见 wiki 规划稿）
- 字段级/行级数据权限（仅有概念文章，未全量实现）

## 2. 用户与场景

| 角色 | 场景 |
|------|------|
| 系统管理员 | 维护用户/角色/菜单/系统注册；分配门户可见系统 |
| 部门管理员 | 按部门维护用户、岗位 |
| 业务开发 | 通过 Dubbo/Starter 接入鉴权；查阅 API 地图 |
| 运维 | 本地/生产部署、Redis 会话一致性、日志排查 |
| 终端用户 | 登录 → 选系统（若启用门户）→ 按菜单与按钮权限操作 |

## 3. 功能范围

### 3.1 认证与会话（P0）

- 用户名密码登录（SHA-256 + 盐，15 次迭代）
- 可选图形验证码（`captcha.enabled`）
- 登出、Session 写入 Redis（全服务共享）
- Token 载体：HTTP 头 `Authorization` = sessionId（禁用 Cookie 链路透传）

**验收**

- 登录返回 `token`、`user`、`permissions`（或门户模式下先返回 `systemList`）
- 未登录访问受保护接口 → JSON「token 失效」
- order/bi 使用同一 token 可访问（共享 Redis）

### 3.2 RBAC 管理（P0）

- 用户 CRUD、启停、重置密码、角色分配、系统授权
- 角色 CRUD、菜单树 + 动作码授权（「有动作必先有页面」）
- 菜单 CRUD、动态路由 `GET /menu/getRouters`
- 部门树、岗位、字典
- 动作目录 `sys_action` + `sys_role_action` 细粒度控制

**验收**

- `@RequiresPermissions` 与 `PermissionService` 并集生效
- 无权限：HTTP 200 + `code=10009`
- `superadmin`/`admin` 拥有 `*:*:*`

### 3.3 多系统门户（P1）

- `sys_system` 注册业务系统（INTERNAL / EXTERNAL）
- 门户分组 `system_group`：platform / business / data / tech / ops
- 登录后多系统选入口；`POST /system/enter|switch` 切换上下文
- SSO Ticket：`POST /sso/validate`（可配 `X-Sso-Secret`）

**验收**

- 单 INTERNAL 系统自动进入并下发菜单
- EXTERNAL 系统返回 `redirectUrl`
- `GET /auth/capabilities` 可补拉当前系统权限

### 3.4 Dubbo 对外能力（P0）

- `getInfoByUserName` / `getUserById` / `getPermissionsByUserId`
- 消费方：order-server、bi-server、knowledge-server（via shiro-starter）

### 3.5 审计（P1）

- 登录日志、操作日志（AOP `@MoliLog`）分页查询与清理

### 3.6 运维资产域（P2 · 遗留）

- 平台/服务器/项目/组件 CRUD（`operation_*` 表），供运维演示，非核心路径

### 3.7 压测专用（P2）

- Profile `loadtest` 下 `POST /loadtest/login` 批量签发 Session

## 4. 接口与数据

- HTTP：约 70 个接口 → [`docs/api/user-center-api-map.md`](../../../../docs/api/user-center-api-map.md)
- Dubbo：3 方法 → [`docs/api/user-center-dubbo.md`](../../../../docs/api/user-center-dubbo.md)
- 数据库：22 张表 → [`docs/sql/USER_CENTER_SCHEMA.md`](../../../../docs/sql/USER_CENTER_SCHEMA.md)

## 5. 约束与依赖

- 必须先于业务服务启动（Dubbo Provider + Session 写入方）
- Redis、MySQL、Nacos 与 [`本地启动指南`](../../wiki-moli/guides/本地启动指南.md) 一致
- 前端 `meiling-ui` 经网关 `/UserCenter/**` 访问

## 6. 里程碑

| 阶段 | 内容 | 状态 |
|------|------|------|
| M1 | RBAC + 登录 + Dubbo | ✅ |
| M2 | 多系统门户 + SSO Ticket | ✅ |
| M3 | 动作码双轨 + capabilities | ✅ |
| M4 | ApiTest 回归 + loadtest profile | ✅ |
| M5 | 扩展能力（OAuth/多租户） | 📋 见 `wiki-moli/develop/articles/用户中心-扩展能力规划.md` |

## 7. 成功指标

- 核心登录/RBAC 接口 ApiTest 全绿
- 网关 + 至少 2 个业务服务共享 Session 联调通过
- 权限变更后 F5 可通过 `/auth/capabilities` 或重新 enter 系统生效
