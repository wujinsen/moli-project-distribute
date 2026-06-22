---
title: Shiro Starter 与跨服务校验
slug: shiro-starter与跨服务校验
type: article
status: active
tags: [Shiro, Starter, Dubbo, 微服务]
sources:
  - moli-user-center/moli-user-center-shiro-starter/README.md
  - moli-user-center/moli-user-center-shiro-starter/src/main/java/com/moli/user/center/starter/
related: [shiro-鉴权体系, 认证与会话机制, 用户中心, 订单服务, dubbo-与-nacos]
created: 2026-06-22
updated: 2026-06-22
---

# Shiro Starter 与跨服务校验

> 体系枢纽 [[shiro-鉴权体系]]；Dubbo [[dubbo-与-nacos]]。

## 自动配置入口

`UserCenterShiroAutoConfiguration`（`META-INF/spring.factories`）在 `moli.user-center.shiro.enabled=true`（默认）时注册：

- `ShiroFilterFactoryBean` + 过滤器链
- `DefaultWebSecurityManager` + `ShiroRealm`
- `RedisSessionDAO` / `RedisCacheManager`（crazycake shiro-redis）
- `AuthorizationAttributeSourceAdvisor`

## 跨服务校验三步

1. **Session 恢复**：请求头 `Authorization` → Shiro 从 Redis 加载 Session → Principal=`SysUser`
2. **账号有效性**（`AuthenticationFilter`）：Dubbo `getUserById` — 删除/停用 → 强制 logout
3. **接口权限**（`@RequiresPermissions`）：`ShiroRealm.doGetAuthorizationInfo` → Dubbo `getPermissionsByUserId`

## Starter 接入 checklist

```xml
<dependency>
  <artifactId>moli-user-center-shiro-starter</artifactId>
</dependency>
```

```yaml
spring.redis:          # 与 user-center 完全一致
  host / port / password / database

dubbo:
  cloud.subscribed-services: user-center-server
  consumer.check: false

moli.user-center.shiro:
  enabled: true
  session-expire-seconds: 86400
  anon-paths:            # 可选，如 /sso/**
    - /actuator/**
```

## user-center vs 业务服务 ShiroConfig

| | user-center | Starter |
|---|-------------|---------|
| 登录 | 本地 Realm 验密 | 禁止 |
| SessionId | `ShiroSessionIdGenerator` `login_token_*` | 同 |
| 过滤器 | 含 logout、sso validate | 仅 authc + anon |

## 常见故障

| 现象 | 原因 |
|------|------|
| 401 全服务 | Redis 不一致或未启动 |
| 403 权限不足 | 角色未赋 perm；缓存未清 |
| Dubbo No provider | user-center 未起或未注册 Nacos |

见 [[故障排查指南]]。

## 设计意图

**单点登录、分布式校验**：密码与权限计算集中在 user-center，业务服务无用户表写权限，降低重复实现与安全面。
