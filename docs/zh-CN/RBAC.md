# RBAC 权限设计文档

**Languages / 语言 / 言語**: [中文](RBAC.md) | [English](../en/RBAC.md) | [日本語](../ja/RBAC.md)

> 本文档描述茉莉项目用户中心（`moli-user-center`）基于 **RBAC（Role-Based Access Control，基于角色的访问控制）** 的权限模型、认证授权流程及接口设计。

---

## 1. 设计概述

项目采用经典的 **用户 — 角色 — 权限（菜单）** 三层模型：

- **用户（User）** 通过 `sys_user_role` 关联一个或多个 **角色（Role）**
- **角色（Role）** 通过 `sys_role_menu` 关联一组 **菜单/按钮（Menu）**
- **菜单** 承载前端路由与 **权限标识（perms）**，按钮类型菜单的 `perms` 字段作为接口级权限码

认证与鉴权由 **Apache Shiro** 实现，Session 与权限缓存存储在 **Redis**，支持多实例部署下的分布式会话。

```
┌─────────┐     N:N      ┌─────────┐     N:N      ┌─────────┐
│  用户   │─────────────▶│  角色   │─────────────▶│  菜单   │
│ SysUser │  sys_user_role│ SysRole │ sys_role_menu│ SysMenu │
└─────────┘              └─────────┘              └─────────┘
     │                                                  │
     │ deptId                                           │ perms（按钮权限标识）
     ▼                                                  ▼
┌─────────┐                                      接口 / 按钮控制
│  部门   │
│ SysDept │
└─────────┘
```

---

## 2. 核心实体

### 2.1 用户（SysUser）

| 字段 | 说明 |
|------|------|
| `userName` | 登录用户名 |
| `password` | 密码（SHA-256 + 盐值，15 次迭代） |
| `salt` | 密码盐值 |
| `deptId` | 所属部门 |
| `status` | 是否锁定（0-未锁，1-已锁） |
| `isDelete` | 逻辑删除（0-未删，1-已删） |

### 2.2 角色（SysRole）

| 字段 | 说明 |
|------|------|
| `roleName` | 角色名称 |
| `status` | 角色状态（1-正常，0-停用） |
| `orderNum` | 排序 |
| `remark` | 备注 |

### 2.3 菜单（SysMenu）

菜单同时承担 **前端路由** 与 **权限资源** 两种职责：

| 字段 | 说明 |
|------|------|
| `menuName` | 菜单名称 |
| `parentId` | 父菜单 ID（0 为顶级） |
| `path` | 前端路由路径 |
| `component` | 前端组件路径 |
| `menuType` | 菜单类型（见下表） |
| `perms` | 权限标识（按钮类型时使用） |
| `status` | 启用状态（1-启用，0-禁用） |
| `icon` / `orderNum` | 图标与排序 |

**菜单类型（`menuType`）**

| 类型码 | 含义 | 用途 |
|--------|------|------|
| `M` | 目录（Directory） | 一级/多级目录，组织菜单树 |
| `C` | 菜单（Menu） | 具体页面路由 |
| `F` | 按钮（Button） | 页面内按钮/接口权限，`perms` 为权限码 |

### 2.4 关联表

| 表 | 字段 | 说明 |
|----|------|------|
| `sys_user_role` | `userId`, `roleId` | 用户与角色多对多 |
| `sys_role_menu` | `roleId`, `menuId` | 角色与菜单多对多 |

### 2.5 部门（SysDept）

用户通过 `deptId` 归属部门，部门为树形结构（`parentId`），用于组织架构管理，与 RBAC 角色授权相互独立。

---

## 3. 权限标识规范

按钮级权限使用 **`模块:资源:操作`** 格式，常量定义见 `CommonPermissionConstant`：

| 权限标识 | 说明 |
|----------|------|
| `sys:user:create` | 新增用户 |
| `sys:user:update` | 修改用户 |
| `sys:user:delete` | 删除用户 |
| `sys:role:create` | 新增角色 |
| `sys:role:update` | 修改角色 |
| `sys:role:delete` | 删除角色 |
| `sys:dept:create` | 新增部门 |
| `sys:dept:update` | 修改部门 |
| `sys:dept:delete` | 删除部门 |

菜单数据中 `menuType = F` 的记录，`perms` 字段应填写上述权限码；Shiro 授权启用后，可通过 `@RequiresPermissions("sys:user:create")` 等方式进行接口级校验。

---

## 4. 认证流程

### 4.1 登录

```
客户端                    用户中心                         Redis
  │                         │                              │
  │── POST /login ─────────▶│                              │
  │   {userName, password}  │                              │
  │                         │── Shiro subject.login() ────▶│
  │                         │   UsernamePasswordToken      │ 写入 Session
  │                         │                              │ shiro:session:*
  │                         │── 查询用户菜单树 ─────────────│
  │                         │   selectMenuTreeByUserId     │
  │◀── LoginVo ────────────│                              │
  │   token + user + menus  │                              │
```

**登录接口**：`POST /login`

**返回结构（LoginVo）**

| 字段 | 说明 |
|------|------|
| `token` | Shiro Session ID，后续请求携带 |
| `user` | 当前用户信息（密码、盐值已脱敏） |
| `menuVoList` | 当前用户可见的菜单树（前端动态路由） |

### 4.2 密码加密

- 算法：**SHA-256**
- 迭代次数：**15 次**
- 每个用户独立 `salt`，工具类：`SHA256Util.sha256(password, salt)`

### 4.3 Session 与 Token

- Shiro Session 持久化至 Redis，Key 前缀：`shiro:session:`
- 登录成功后返回的 `token` 即为 Session ID
- 前端需在后续请求的 Header 中携带 Session 标识（与 Shiro Cookie / 自定义 Header 策略配合）
- 权限缓存 Key 前缀：`shiro:cache:`，Principal 标识字段为 `userName`

### 4.4 登出

- 接口：`POST /logout`
- 调用 `ShiroUtils.logout()` 清除 Subject 与 Session

### 4.5 未认证访问

除白名单路径外，所有请求需通过 Shiro `authc` 过滤器。未登录时返回 JSON 错误（非页面重定向），适配前后端分离：

| 白名单路径 | 说明 |
|------------|------|
| `/login` | 登录 |
| `/swagger-ui.html`、`/v2/**` 等 | Swagger 文档 |
| `/static/**` | 静态资源 |

---

## 5. 授权流程

### 5.1 菜单授权（已实现）

普通用户菜单查询链路：

```
用户 ID
  → sys_user_role（查角色 ID 列表）
  → sys_role_menu（查菜单 ID 列表）
  → sys_menu（查菜单详情）
  → 构建 MenuVo 树（createTree）
```

**超级管理员**：用户名为 `admin` 时，跳过角色过滤，返回全部菜单。

**核心实现**：`MenuServiceImpl.selectMenuListByUserId()`

### 5.2 接口权限（预留）

`ShiroRealm.doGetAuthorizationInfo()` 中已预留完整授权逻辑（当前注释状态），启用后将：

1. 根据用户 ID 查询关联角色 → 写入 `rolesSet`
2. 根据角色查询关联菜单 → 筛选 `menuType = F` 的按钮 → 提取 `perms` → 写入 `permsSet`
3. 缓存至 Redis，供 `@RequiresPermissions` / `@RequiresRoles` 注解使用

启用后可在 Controller 方法上添加注解，例如：

```java
@RequiresPermissions("sys:user:delete")
@DeleteMapping("/{userIds}")
public MoliResult delete(@PathVariable Long[] userIds) { ... }
```

### 5.3 前端路由与菜单树

登录或调用 `GET /menu/getRouters` 时，后端返回 `MenuVo` 树结构，包含：

- `path`、`component`、`name` — 前端路由
- `meta.title`、`meta.icon` — 菜单展示
- `children` — 子菜单
- `hidden` — 是否隐藏（由 `status` 推导）

菜单树构建规则见 `MenuServiceImpl.createTree()`，支持多级目录嵌套。

---

## 6. RBAC 管理接口

### 6.1 用户管理（`/user`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/user/list` | 用户分页列表 |
| POST | `/user` | 新增用户 |
| PUT | `/user` | 更新用户 |
| GET | `/user/{id}` | 查询用户 |
| DELETE | `/user/{userIds}` | 逻辑删除用户 |
| PUT | `/user/changeStatus` | 修改用户状态 |
| GET | `/user/getRoleByUserId/{userId}` | 查询用户已分配角色 |
| PUT | `/user/inserUserRole` | 为用户分配角色 |

### 6.2 角色管理（`/role`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/role/list` | 角色分页列表 |
| POST | `/role` | 新增角色（可同时绑定菜单 `menuIds`） |
| PUT | `/role` | 更新角色 |
| GET | `/role/{id}` | 查询角色 |
| DELETE | `/role/{ids}` | 删除角色及角色-菜单关联 |
| PUT | `/role/changeStatus` | 修改角色状态 |

### 6.3 菜单管理（`/menu`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/menu/getRouters` | 当前登录用户的路由菜单树 |
| GET | `/menu/list` | 当前用户可见菜单列表 |
| GET | `/menu/getMenuTreeAll` | 全部菜单树（管理用） |
| GET | `/menu/selectMenuTreeByRoleId/{roleId}` | 角色菜单授权回显 |
| POST | `/menu` | 新增菜单 |
| PUT | `/menu` | 更新菜单 |
| GET | `/menu/{id}` | 查询菜单 |
| DELETE | `/menu/{id}` | 删除菜单 |

### 6.4 部门管理（`/dept`）

部门 CRUD，与用户 `deptId` 关联，用于组织架构，不参与角色权限计算。

---

## 7. 跨服务认证

其他微服务（如 `moli-order`、`moli-bi`）通过 **`moli-user-center-client`** 模块集成 Shiro：

```
业务服务                         用户中心
    │                               │
    │── Feign: getInfoByUserName ──▶│  查询用户信息
    │                               │
    │── 共享 Redis Session ──────────│  shiro:session:* / shiro:cache:*
    │                               │
    │── FeignConfiguration ─────────│  透传 Authorization Header
```

- **client 模块 ShiroRealm**：通过 `UserCenterClient` 远程获取用户信息完成认证
- **FeignConfiguration**：服务间调用自动传递 `Authorization` 请求头
- 各业务服务与用户中心共用 Redis，实现 Session 共享

---

## 8. 数据库表设计（参考）

```sql
-- 用户表
CREATE TABLE sys_user (
    id           BIGINT PRIMARY KEY,
    dept_id      BIGINT,
    user_name    VARCHAR(64) NOT NULL,
    password     VARCHAR(128) NOT NULL,
    salt         VARCHAR(32),
    nick_name    VARCHAR(64),
    status       TINYINT DEFAULT 0 COMMENT '0-未锁 1-已锁',
    is_delete    TINYINT DEFAULT 0,
    create_time  DATETIME,
    update_time  DATETIME
);

-- 角色表
CREATE TABLE sys_role (
    id           BIGINT PRIMARY KEY,
    role_name    VARCHAR(64) NOT NULL,
    status       TINYINT DEFAULT 1 COMMENT '1-正常 0-停用',
    order_num    VARCHAR(16),
    remark       VARCHAR(256),
    create_time  DATETIME,
    update_time  DATETIME
);

-- 菜单表
CREATE TABLE sys_menu (
    id           BIGINT PRIMARY KEY,
    menu_name    VARCHAR(64) NOT NULL,
    parent_id    BIGINT DEFAULT 0,
    path         VARCHAR(128),
    component    VARCHAR(128),
    menu_type    CHAR(1) COMMENT 'M-目录 C-菜单 F-按钮',
    perms        VARCHAR(128) COMMENT '权限标识，如 sys:user:create',
    status       TINYINT DEFAULT 1,
    icon         VARCHAR(64),
    order_num    INT,
    create_time  DATETIME,
    update_time  DATETIME
);

-- 用户-角色关联
CREATE TABLE sys_user_role (
    id           BIGINT PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    role_id      BIGINT NOT NULL
);

-- 角色-菜单关联
CREATE TABLE sys_role_menu (
    id           BIGINT PRIMARY KEY,
    role_id      BIGINT NOT NULL,
    menu_id      BIGINT NOT NULL
);

-- 部门表
CREATE TABLE sys_dept (
    id           BIGINT PRIMARY KEY,
    parent_id    BIGINT DEFAULT 0,
    dept_name    VARCHAR(64),
    order_num    INT,
    status       TINYINT DEFAULT 1,
    create_time  DATETIME,
    update_time  DATETIME
);
```

> 以上为根据实体类推导的参考 DDL，实际建表请以项目 SQL 脚本为准。

---

## 9. 典型使用场景

### 9.1 为新员工分配权限

1. 管理员创建用户 → `POST /user`
2. 查询可选角色 → `GET /role/list`
3. 为用户绑定角色 → `PUT /user/inserUserRole`
4. 用户登录后自动获得对应角色下的菜单与按钮权限

### 9.2 新建角色并授权菜单

1. 获取全部菜单树 → `GET /menu/getMenuTreeAll`
2. 创建角色并勾选菜单 → `POST /role`（Body 含 `menuIds`）
3. 将角色分配给用户 → `PUT /user/inserUserRole`

### 9.3 新增接口权限

1. 在 `sys_menu` 中新增 `menuType = F` 的记录，`perms = sys:xxx:yyy`
2. 在 `CommonPermissionConstant` 中补充常量（可选）
3. 启用 `ShiroRealm` 授权逻辑
4. 在 Controller 方法上添加 `@RequiresPermissions("sys:xxx:yyy")`
5. 在角色管理中为对应角色勾选该按钮权限

---

## 10. 实现状态与扩展建议

| 能力 | 状态 | 说明 |
|------|------|------|
| 用户/角色/菜单 CRUD | ✅ 已实现 | 见各 Controller |
| 用户-角色分配 | ✅ 已实现 | `inserUserRole` |
| 角色-菜单绑定 | ✅ 已实现 | 创建角色时写入 `sys_role_menu` |
| 登录认证 + Redis Session | ✅ 已实现 | Shiro + shiro-redis |
| 按角色返回菜单树 | ✅ 已实现 | 含 admin 超级管理员 bypass |
| Shiro 接口级权限注解 | ⏳ 预留 | `doGetAuthorizationInfo` 逻辑已注释，待启用 |
| 验证码 | ⏳ 预留 | `captchaImage` 接口框架已存在 |

**后续扩展建议：**

1. 启用 `ShiroRealm.doGetAuthorizationInfo()` 中的角色/权限加载逻辑
2. 在敏感接口上添加 `@RequiresPermissions` 注解
3. 角色更新时同步维护 `sys_role_menu`（当前 `PUT /role` 未更新菜单关联）
4. 用户角色分配前先清除旧关联，避免重复授权
5. 结合网关统一鉴权，或在 Gateway 层校验 Token

---

## 11. 相关代码位置

| 模块 | 路径 | 职责 |
|------|------|------|
| 实体定义 | `moli-user-center-common/.../domain/entity/` | SysUser、SysRole、SysMenu 等 |
| 权限常量 | `moli-user-center-common/.../constant/CommonPermissionConstant.java` | 权限标识常量 |
| Shiro 配置 | `moli-user-center-server/.../config/shiro/ShiroConfig.java` | 过滤器链、Redis Session |
| 认证 Realm | `moli-user-center-server/.../config/shiro/ShiroRealm.java` | 身份认证与授权 |
| 菜单服务 | `moli-user-center-server/.../service/impl/MenuServiceImpl.java` | 菜单树与 RBAC 查询 |
| 登录 | `moli-user-center-server/.../controller/LoginController.java` | 登录/登出 |
| 跨服务 Client | `moli-user-center-client/` | Feign + Shiro 集成 |
