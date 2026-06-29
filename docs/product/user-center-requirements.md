# 用户中心 · 需求说明（v1 工程索引）

> **权威 PRD 原文**：[`moli-knowledge/kb/raw/prd/user-center-prd-v1.md`](../../moli-knowledge/kb/raw/prd/user-center-prd-v1.md)  
> **v1 发布范围**：[`moli-v1-release-scope.md`](moli-v1-release-scope.md) §3.1  
> **模块**：`moli-user-center` · 服务名 `user-center-server` · 更新：2026-06-28

本文是 **工程侧需求导航**（验收口径 + 文档链），不重复 PRD 全文。

---

## 1. 产品目标（摘要）

用户中心是茉莉平台的 **统一身份、RBAC、会话签发** 中心：

- 所有业务经 **网关** 访问；Session 存 **Redis**，order/bi/knowledge 通过 **Shiro Starter + Dubbo** 校验
- 提供 **~70 HTTP 接口** + **3 个 Dubbo 方法**
- 支持 **多系统门户**（INTERNAL 菜单 / EXTERNAL 跳转）

**v1 不做**：多租户、OAuth2 开放平台、字段级数据权限。

---

## 2. 功能清单与验收

### 2.1 认证与会话（P0）

| 需求 | 验收标准 |
|------|----------|
| 用户名密码登录 | `POST /login` 返回 `token`（= sessionId）、用户信息 |
| 图形验证码 | `captcha.enabled=true` 时必传 `code` + `uuid` |
| 登出 | `POST /logout` 清除 Session |
| 跨服务 Session | order/bi/knowledge 同 `Authorization` 头可访问 |
| 未登录 | JSON「token 失效」，非 302 |

**接口**：[`user-center-api-map.md`](../api/user-center-api-map.md) §登录

### 2.2 RBAC（P0）

| 需求 | 验收标准 |
|------|----------|
| 用户/角色/菜单/部门/岗位/字典 CRUD | ApiTest 全绿 |
| 菜单动态路由 | `GET /menu/getRouters` 按角色过滤 |
| 动作码双轨 | 角色授权「有动作必先有页面」 |
| 接口权限 | `@RequiresPermissions`；无权限 `code=10009` |
| 超管 | `admin` / `superadmin` → `*:*:*` |

**设计**：[RBAC.md](../zh-CN/RBAC.md) · [user-center-detailed-design.md](../design/user-center-detailed-design.md)

### 2.3 多系统门户（P1 · v1 交付）

| 需求 | 验收标准 |
|------|----------|
| 系统注册 `sys_system` | INTERNAL / EXTERNAL 类型 |
| 门户分组 | `system_group`：platform / business / data / tech / ops |
| 进入/切换 | `POST /system/enter`、`POST /system/switch` |
| 权限快照 | `GET /auth/capabilities` |
| SSO Ticket | `POST /sso/validate`（可选 `X-Sso-Secret`） |

**设计**：[portal-system-group.md](../design/portal-system-group.md)

### 2.4 Dubbo（P0）

| 方法 | 用途 |
|------|------|
| `getInfoByUserName` | 按用户名查用户 |
| `getUserById` | 按 ID 查用户 |
| `getPermissionsByUserId` | 合并菜单+动作权限 |

**契约**：[user-center-dubbo.md](../api/user-center-dubbo.md) · 端口 **20881**

### 2.5 审计（P1）

- 登录日志、操作日志（`@MoliLog` AOP）分页查询

### 2.6 压测（P2 · loadtest profile）

- `POST /loadtest/login` 批量签发 Session → 配合 [load-test](../../load-test/README.md)

---

## 3. 非功能需求

| 项 | 要求 |
|----|------|
| 启动顺序 | **先于** order/bi/knowledge（Dubbo Provider） |
| Redis | 与业务服务 **同一实例、同一 database**（dev 默认 db=1） |
| 端口 | HTTP **8888**；网关 `/UserCenter/**` |
| 数据 | 22 张表 → [USER_CENTER_SCHEMA.md](../sql/USER_CENTER_SCHEMA.md) |

---

## 4. 测试与运维

| 类型 | 文档 |
|------|------|
| ApiTest 清单 | [user-center.md](../test/user-center.md) |
| 冒烟（含登录） | [release-smoke-checklist.md](../test/release-smoke-checklist.md) |
| 运维要点 | [wiki-ops/user-center-运维要点](../../moli-knowledge/kb/wiki-ops/ops/user-center-运维要点.md) |
| 模块 README | [moli-user-center/README.md](../../moli-user-center/README.md) |

---

## 5. 里程碑（对齐 PRD）

| 阶段 | 内容 | v1 |
|------|------|-----|
| M1 | RBAC + 登录 + Dubbo | ✅ |
| M2 | 多系统门户 + SSO | ✅ |
| M3 | 动作码 + capabilities | ✅ |
| M4 | ApiTest + loadtest | ✅ |
| M5 | OAuth / 多租户 | 📋 v2+ |

---

## 6. 相关

- 概要设计：[user-center-overview.md](../design/user-center-overview.md)
- API 地图：[user-center-api-map.md](../api/user-center-api-map.md)
- 跨服务接入：[moli-user-center-shiro-starter/README.md](../../moli-user-center/moli-user-center-shiro-starter/README.md)
