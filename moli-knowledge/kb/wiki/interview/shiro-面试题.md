---
title: Shiro 与 SSO（面试题系列）
slug: shiro-面试题
type: interview
status: active
tags: [Shiro, SSO, 面试题, 鉴权]
sources:
 - moli-user-center/moli-user-center-shiro-starter/
 - docs/zh-CN/RBAC.md
related: [shiro-鉴权体系, shiro-starter与跨服务校验, sso与系统门户, 认证与会话机制, rbac-权限模型]
created: 2026-06-22
updated: 2026-06-22
---

# Shiro 与 SSO（面试题系列）

> [[shiro-鉴权体系]] [[shiro-starter与跨服务校验]] [[sso与系统门户]] [[认证与会话机制]]

## Q2. 为什么业务服务不能 POST /login？

Starter 的 Realm 认证直接抛异常；登录仅 user-center，保证单点签发。

## Q3. 跨服务怎么知道用户有效？

AuthenticationFilter：Session 恢复后 Dubbo `getUserById` 查停用/删除。

## Q4. 权限从哪来？

`@RequiresPermissions` → Realm 授权 → Dubbo `getPermissionsByUserId` → RBAC 合并菜单+动作。

## Q5. Redis 不一致会怎样？

Session 读不到 → 全链路 401。见。

## Q6. anon 路径有哪些？

login、swagger、actuator、sso/validate、loadtest 等；其余 authc。

## Q7. INTERNAL vs EXTERNAL 系统？

INTERNAL 菜单内嵌；EXTERNAL 门户跳转 base_url（如 KnowledgeServer）。

## Q8. SSO Ticket 干什么用？

`POST /sso/validate` 一次性 ticket，外系统服务端校验；与 Header token 场景互补。

## Q9. Shiro vs Spring Security？

选型 Shiro + 自研 Starter；Session 存 Redis 非 JWT。

## Q10. 新微服务如何接入鉴权？

引 shiro-starter + 同 Redis + Dubbo 订 user-center + 网关路由。见 [[shiro-starter与跨服务校验]]。
