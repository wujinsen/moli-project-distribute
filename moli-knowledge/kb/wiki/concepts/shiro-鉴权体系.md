---
title: Shiro 鉴权体系
slug: shiro-鉴权体系
type: concept
status: active
tags: [Shiro, 鉴权, SSO, 微服务]
sources:
  - moli-user-center/moli-user-center-shiro-starter/
  - moli-user-center/moli-user-center-server/src/main/java/com/moli/user/center/server/config/shiro/
  - docs/zh-CN/RBAC.md
related: [认证与会话机制, shiro-starter与跨服务校验, sso与系统门户, rbac-权限模型, 用户中心, shiro-面试题]
created: 2026-06-22
updated: 2026-06-22
---

# Shiro 鉴权体系（概念枢纽）

> Session/token 见 [[认证与会话机制]]；Starter 细节 [[shiro-starter与跨服务校验]]；多系统门户 [[sso与系统门户]]；权限模型 [[rbac-权限模型]]。

茉莉用 **Apache Shiro + Redis Session + Dubbo** 实现「用户中心签发、业务服务校验」的 SSO 式鉴权。

面试速记 [[shiro-面试题]]。

## 两档 Realm 分工

| 部署 | Realm 认证 `doGetAuthenticationInfo` | 授权 `doGetAuthorizationInfo` |
|------|--------------------------------------|-------------------------------|
| **user-center** | 查 `sys_user`、校验密码 | 本地算权限集 |
| **order/bi/knowledge**（Starter） | **直接抛异常**（禁止本地登录） | Dubbo `getPermissionsByUserId` |

业务服务从 **Redis** 读 Session 得到 Principal，不跑登录逻辑。

## 过滤器链（Starter 默认）

```
anon: /swagger*, /static/**, 配置的 anonPaths
authc: /**  → 自定义 AuthenticationFilter
```

`AuthenticationFilter` 在 Shiro 判定已登录后，**额外 Dubbo 查用户**是否停用/删除，失效则 logout + JSON 401。

## Redis 键空间

| 前缀 | 用途 |
|------|------|
| `shiro:session:login_token_*` | Session 序列化 |
| `shiro:cache:*` | 授权缓存 |
| `moli:shiro:user-session(s):*` | 单/多端登录索引 |

**全服务 database 必须一致**，否则 Session 读不到。

## 注解鉴权

```java
@RequiresPermissions("system:user:list")
```

Starter 注册 `AuthorizationAttributeSourceAdvisor`，触发 Shiro AOP；权限来自 Dubbo 拉取的字符串集合。

## 与用户中心登录的关系

```
唯一登录入口: POST /UserCenter/login
业务 API: Header Authorization: login_token_xxx
登出: POST /UserCenter/logout
```

实操见 [[登录与鉴权指南]]；压测专用 [[loadtest-profile与压测登录]]。

## 模块依赖

```
moli-user-center-api          → Dubbo 接口 UserCenterServer
moli-user-center-shiro-starter → AutoConfiguration + Realm + Filter
```

新微服务接入：引 starter + 配 Redis + Dubbo 订阅 user-center。
