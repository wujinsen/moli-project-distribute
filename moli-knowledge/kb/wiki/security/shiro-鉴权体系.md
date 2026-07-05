---
title: Shiro 鉴权体系
slug: shiro-鉴权体系
type: concept
status: active
tags: [Shiro, 鉴权, SSO, 微服务]
sources:
- docs/zh-CN/RBAC.md
- moli-user-center/moli-user-center-server/src/main/java/com/moli/user/center/server/config/shiro/
- moli-user-center/moli-user-center-shiro-starter/
- raw/wujinsen_markdown/架构/安全框架/cas/OAuth2实现单点登录SSO.note.md
- raw/wujinsen_markdown/架构/安全框架/cas/cas5.3.2单点登录-骨架搭建(一).note.md
- raw/wujinsen_markdown/架构/安全框架/cas/无标题笔记.note.md
- raw/wujinsen_markdown/架构/安全框架/shiro/Shiro的 rememberMe 功能使用指导(为什么rememberMe设置了没作用？).note.md
- raw/wujinsen_markdown/架构/安全框架/shiro/SpringBoot 整合Shiro实现动态权限加载更新+Session共享+单点登录.note.md
- raw/wujinsen_markdown/架构/安全框架/shiro/shiro认证授权.note.md
- raw/wujinsen_markdown/架构/安全框架/开源项目/OAuth2实现单点登录SSO.note.md
related: [认证与会话机制, shiro-starter与跨服务校验, sso与系统门户, rbac-权限模型, shiro-面试题]
created: 2026-06-22
updated: 2026-07-05
---

# Shiro 鉴权体系（概念枢纽）

> Session/token 见 [[security/认证与会话机制]]；Starter 细节 [[security/shiro-starter与跨服务校验]]；多系统门户 [[security/sso与系统门户]]；权限模型 [[security/rbac-权限模型]]。

用 **Apache Shiro + Redis Session + Dubbo** 实现「用户中心签发、业务服务校验」的 SSO 式鉴权。

面试速记 [[security/shiro-面试题]]。

## 两档 Realm 分工

| 部署 | Realm 认证 `doGetAuthenticationInfo` | 授权 `doGetAuthorizationInfo` |
|------|--------------------------------------|-------------------------------|
| **user-center** | 查 `sys_user`、校验密码 | 本地算权限集 |
| **order/bi/knowledge**（Starter） | **直接抛异常**（禁止本地登录） | Dubbo `getPermissionsByUserId` |

业务服务从 **Redis** 读 Session 得到 Principal，不跑登录逻辑。

## 过滤器链（Starter 默认）

```
anon: /swagger*, /static/**, 配置的 anonPaths
authc: /** → 自定义 AuthenticationFilter
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

实操见 ；压测专用 [[middleware/loadtest-profile与压测登录]]。

## 模块依赖

```
moli-user-center-api → Dubbo 接口 UserCenterServer
moli-user-center-shiro-starter → AutoConfiguration + Realm + Filter
```

新微服务接入：引 starter + 配 Redis + Dubbo 订阅 user-center。
## 批次#1310 增补（wujinsen P0）

合并 `架构/安全框架/` Shiro/Spring Security 配置类 raw（通用概念，非茉莉手册）。

原文插图 annex：[[security/annex-OAuth2实现单点登录SSO]]

原文插图 annex：[[security/annex-SpringBoot-整合Shiro实现动态权限加载更新+Session共享+单点登录]]
