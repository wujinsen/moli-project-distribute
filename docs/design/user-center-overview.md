# 用户中心 · 概要设计

> 模块：`moli-user-center` · 更新：2026-07-13  
> 详细设计：[`user-center-detailed-design.md`](user-center-detailed-design.md)  
> 跨服务全链路：[`../zh-CN/ARCHITECTURE.md`](../zh-CN/ARCHITECTURE.md)

## 1. 定位

用户中心是茉莉平台的**身份认证、授权管理、会话签发**中心，也是唯一允许完成登录/SSO 的微服务。其它服务只消费其 Redis Session 与 Dubbo 接口。

![用户中心平台定位](../diagrams/png/moli-user-center-position.png)

> 可编辑源文件：[moli-user-center-position.drawio](../diagrams/moli-user-center-position.drawio) · 鉴权时序见 [moli-auth-flow.drawio](../diagrams/moli-auth-flow.drawio)

<details>
<summary>ASCII 简图（备查）</summary>

```
meiling-ui ──HTTP──► gateway:21000/UserCenter/** ──► user-center-server:8888
                                                          │
                     order/bi/knowledge ◄──Dubbo──────────┘
                              │
                              └── Redis Session（共享）
```

</details>

![RBAC 模型](../diagrams/png/moli-rbac-model.png)

> 源文件：[moli-rbac-model.drawio](../diagrams/moli-rbac-model.drawio)

## 2. 模块划分

| 模块 | 包/路径 | 职责 |
|------|---------|------|
| common | `moli-user-center-common` | 22 实体、VO、常量、密码工具 |
| api | `moli-user-center-api` | Dubbo 接口定义（无实现） |
| server | `moli-user-center-server` | Controller、Service、Shiro、Mapper |
| shiro-starter | `moli-user-center-shiro-starter` | 业务服务自动装配 Filter/Realm/Dubbo 消费 |

## 3. 核心子系统

### 3.1 认证子系统

- `LoginController` + `server/ShiroRealm`（本地 DB 认证）
- `RedisSessionDAO` 持久化 Session
- 白名单：`/login`、`/sso/validate`、Swagger

### 3.2 授权子系统

- 双轨权限：**菜单 perms**（`sys_menu`）+ **动作码**（`sys_action` → `sys_role_action`）
- `PermissionService` 合并为 Shiro `AuthorizationInfo`
- 注解 `@RequiresPermissions` + 编程式校验

详见 [`../zh-CN/RBAC.md`](../zh-CN/RBAC.md)。

### 3.3 门户子系统

- 表：`sys_system`、`sys_user_system`、`sys_menu.system_id`（SSO-MENU-1）
- `SysSystemService`：登录上下文、enter/switch、**按系统过滤**运行时菜单
- 分组常量：[`SystemGroupConstant`](../../moli-distribute-common/src/main/java/com/moli/common/constant/SystemGroupConstant.java) — 见 [`portal-system-group.md`](portal-system-group.md) · [`sso-menu-system-isolation.md`](sso-menu-system-isolation.md)

### 3.4 运营管理子系统（2026-07）

- 包：`operation/` · 表 `operation_*` · API `/operation/*`
- 四台账 + 部署中心 + 拓扑/关系 + 任务历史（含 DC-4 分组）
- 详见 [`server-ops-module-roadmap.md`](server-ops-module-roadmap.md) · [operation-server-ops-prd.md](../product/operation-server-ops-prd.md)

### 3.5 对外 RPC

- `UserServerProvider` 实现 `UserCenterServer`
- 注册：`@DubboService(version="1.0.0", group="moli")`

### 3.6 审计

- `LogAspect` + `@MoliLog` → `sys_operation_log`
- 登录成功/失败 → `sys_login_log`

## 4. 技术选型

| 层次 | 技术 |
|------|------|
| Web | Spring Boot 2.3、Spring MVC |
| 持久化 | MyBatis-Plus、Druid |
| 鉴权 | Apache Shiro + shiro-redis |
| RPC | Spring Cloud Dubbo |
| 注册/配置 | Nacos Discovery（Config 可选 extension-configs） |
| 文档 | Swagger2（`swagger.show` 可关） |
| ID | 雪花算法（`IdGenerator`） |

## 5. 部署视图

![本地部署拓扑](../diagrams/png/moli-deploy-topology.png)

| 组件 | 端口/说明 |
|------|-----------|
| user-center-server HTTP | 8888 |
| Dubbo | 20881 |
| MySQL | 3306 / 库 `moli` |
| Redis | 6379（dev 配置 database=1，须与消费方一致） |
| Nacos | 8848 / namespace `dev` |

## 6. 与其它文档关系

| 文档 | 内容 |
|------|------|
| [`user-center-detailed-design.md`](user-center-detailed-design.md) | 类职责、关键流程、配置项 |
| [`server-ops-module-roadmap.md`](server-ops-module-roadmap.md) | 运营管理路线图 |
| [`sso-menu-system-isolation.md`](sso-menu-system-isolation.md) | SSO-MENU-1 设计 |
| [`USER_CENTER_SCHEMA.md`](../sql/USER_CENTER_SCHEMA.md) | 表结构 |
| [`user-center-api-map.md`](../api/user-center-api-map.md) | HTTP 接口 |
| [`user-center-dubbo.md`](../api/user-center-dubbo.md) | Dubbo 契约 |
| [`moli-user-center/README.md`](../../moli-user-center/README.md) | 模块入口 |
