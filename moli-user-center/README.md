# moli-user-center · 用户中心

茉莉微服务的**权限中枢**：登录/会话、RBAC、多系统门户、审计日志，并作为 **Dubbo Provider** 向 order / bi / knowledge 输出鉴权能力。

## 模块构成

| 子模块 | 职责 |
|--------|------|
| `moli-user-center-common` | 实体（SysUser/Role/Menu/Dept/Action/System…）、VO、工具类 |
| `moli-user-center-api` | Dubbo 契约 `UserCenterServer` |
| `moli-user-center-server` | HTTP API、Shiro、MyBatis、Dubbo Provider |
| `moli-user-center-shiro-starter` | 供业务服务复用的 Session 校验 Starter（见子目录 README） |

## 运行时

| 项 | 值 |
|----|----|
| Nacos 服务名 | `user-center-server` |
| HTTP 端口 | **28101** |
| Dubbo 端口 | **20881**（`version=1.0.0`, `group=moli`） |
| 网关路由 | `/UserCenter/**` → StripPrefix=1 |
| Swagger | `http://localhost:28101/swagger-ui.html`（经网关：`/UserCenter/swagger-ui.html`） |

## 功能域

| 域 | Controller 前缀 | 说明 |
|----|-----------------|------|
| 认证 | `/login`, `/logout`, `/captchaImage` | 登录签发 Session（token=sessionId） |
| SSO | `/sso/validate` | 子系统 Ticket 校验 |
| 多系统门户 | `/system`, `/auth/capabilities` | 系统注册、进入/切换、权限快照 |
| RBAC | `/user`, `/role`, `/menu`, `/dept`, `/post`, `/dict`, `/action` | 用户/角色/菜单/部门/岗位/字典/动作 |
| 审计 | `/log` | 登录日志、操作日志 |
| 运维资产 | `/operation/platform|server|project|component` | 平台/服务器/项目/组件台账 |
| 部署中心 | `/operation/deploy`、`/operation/file`、`/operation/command`、`/operation/task` | 远程启停、SFTP 上传、受控 shell（见 [`docs/api/operation-deploy-api.md`](../docs/api/operation-deploy-api.md)） |
| 压测 | `/loadtest/login` | 仅 `spring.profiles.active=loadtest` |
| Dubbo | — | `getInfoByUserName` / `getUserById` / `getPermissionsByUserId` |

## 依赖

- **MySQL** `moli` 库（`sys_*` + `operation_*` 表，见 [`docs/sql/USER_CENTER_SCHEMA.md`](../docs/sql/USER_CENTER_SCHEMA.md)）
- **Redis** — Shiro Session / 权限缓存（各服务须同一实例；`application-dev.yml` 默认 `database: 1`）
- **Nacos** — 服务注册（`bootstrap.yml` namespace `dev`）

## 本地编译与启动

```bash
cd moli-distribute-parent && mvn clean install -DskipTests
cd ../moli-distribute-common && mvn clean install -DskipTests
cd ../moli-user-center && mvn clean package -DskipTests
java -jar moli-user-center-server/target/moli-user-center-server-*.jar
```

启动顺序与全栈说明：[`kb/wiki-moli/guides/本地启动指南.md`](../moli-knowledge/kb/wiki-moli/guides/本地启动指南.md)。

## 文档索引

| 类型 | 路径 |
|------|------|
| **PRD** | [`docs/product/user-center-requirements.md`](../docs/product/user-center-requirements.md) · raw [`user-center-prd-v1.md`](../moli-knowledge/kb/raw/prd/user-center-prd-v1.md) |
| **概要设计** | [`docs/design/user-center-overview.md`](../docs/design/user-center-overview.md) |
| **详细设计** | [`docs/design/user-center-detailed-design.md`](../docs/design/user-center-detailed-design.md) |
| **RBAC 模型** | [`docs/zh-CN/RBAC.md`](../docs/zh-CN/RBAC.md) |
| **全链路架构** | [`docs/zh-CN/ARCHITECTURE.md`](../docs/zh-CN/ARCHITECTURE.md) |
| **表结构 / DDL** | [`docs/sql/USER_CENTER_SCHEMA.md`](../docs/sql/USER_CENTER_SCHEMA.md) + [`scripts/moli.sql`](../scripts/moli.sql) |
| **HTTP API** | [`docs/api/user-center-api-map.md`](../docs/api/user-center-api-map.md) |
| **Dubbo API** | [`docs/api/user-center-dubbo.md`](../docs/api/user-center-dubbo.md) |
| **测试** | [`docs/test/user-center.md`](../docs/test/user-center.md) + [`load-test/README.md`](../load-test/README.md) |
| **运维** | [`kb/wiki-moli/ops/user-center-运维要点.md`](../moli-knowledge/kb/wiki-moli/ops/user-center-运维要点.md) |
| **Wiki 浏览页** | [`kb/wiki/services/用户中心.md`](../moli-knowledge/kb/wiki/services/用户中心.md) |
| **跨服务接入** | [`moli-user-center-shiro-starter/README.md`](moli-user-center-shiro-starter/README.md) |

## 测试

```bash
cd moli-user-center-server
mvn test
```

详见 [`docs/test/user-center.md`](../docs/test/user-center.md)。
