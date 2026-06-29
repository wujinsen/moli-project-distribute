---
title: Shiro Starter 与跨服务校验
slug: shiro-starter与跨服务校验
type: article
status: active
tags: [Shiro, Starter, Dubbo, 微服务, 循环依赖, SpringBoot]
sources:
 - moli-user-center/moli-user-center-shiro-starter/README.md
 - moli-user-center/moli-user-center-shiro-starter/src/main/java/com/moli/user/center/starter/
 - moli-user-center/moli-user-center-shiro-starter/src/main/java/com/moli/user/center/starter/autoconfigure/UserCenterShiroAutoConfiguration.java
related: [shiro-鉴权体系, 认证与会话机制, 用户中心, 订单服务, dubbo-与-nacos, spring-aop与代理, 登录与鉴权故障根因汇总]
created: 2026-06-22
updated: 2026-06-23
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
spring.redis: # 与 user-center 完全一致
 host / port / password / database

dubbo:
 cloud.subscribed-services: user-center-server
 consumer.check: false

moli.user-center.shiro:
 enabled: true
 session-expire-seconds: 86400
 anon-paths: # 可选，如 /sso/**
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

见。

## 设计意图

**单点登录、分布式校验**：密码与权限计算集中在 user-center，业务服务无用户表写权限，降低重复实现与安全面。

## 启动期循环依赖（securityManager）根因与解法

> 实战案例（2026-06-23，knowledge-server 8090）。现象：应用启动直接 `APPLICATION FAILED TO START`，报 `BeanCurrentlyInCreationException`，循环始终指向 `securityManager` 的 **parameter 1**：
>
> ```
> targeterBeanPostProcessor → shiroFilterFactory → securityManager(param 1) → securityManager
> ```

### 两条循环依赖的边（容易被各种 @Lazy 误导）

**1. 主因：`SecurityManager` 接口 `extends SessionManager`**

```java
public interface SecurityManager extends Authenticator, Authorizer, SessionManager
```

因此 `securityManager` 这个 Bean **本身也是一个 `SessionManager` 候选**。当 `securityManager(ShiroRealm, SessionManager, RedisCacheManager)` 按 `SessionManager` 接口注入第二个参数时，Spring 在 **Dubbo `targeterBeanPostProcessor` 触发的早期实例化阶段**，会把 `securityManager` 自己也当成 `SessionManager` 候选选中 → `securityManager → securityManager` 自循环。这正是错误一直卡在「parameter 1」的原因。

**2. 次因：Shiro AOP 鉴权 Advisor**

Springfox（`@EnableSwagger2`）的 `objectMapperConfigurer` 触发 AOP 自动代理；代理任意 Bean 时要取出全部 `Advisor`，于是在其它 Shiro Bean 还没造好时强制创建 `authorizationAttributeSourceAdvisor`，而它又依赖 `securityManager`，形成 `securityManager → (AOP 检索 Advisor) → advisor → securityManager`。

### 为什么 `@Lazy` / `allow-circular-references` 都不管用

- `spring.main.allow-circular-references: true` 早已在 `application.yml` 配好——它对 **`@Bean` 方法参数注入（构造式）** 的循环无能为力，只能救 setter/field。
- 在 `@Bean` 方法参数上加 `@Lazy` 在本场景未能稳定生成延迟代理；且即便 advisor 延迟，主因（SessionManager 歧义）依旧自循环。
- 把 `@DubboReference` 抽成独立 Bean（`UserCenterServerRef`）只动了 Dubbo 这条边，没打中真正的 SessionManager 歧义。

### 最终解法（`UserCenterShiroAutoConfiguration`）

1. **按具体类型注入，消除 SessionManager 歧义**：`sessionManager()` 返回类型与 `securityManager(...)` 的参数都改成具体类 `ShiroSessionManager`。`DefaultWebSecurityManager` 不是 `ShiroSessionManager`，候选唯一，自循环彻底消失。

 ```java
 public ShiroSessionManager sessionManager(...) { ... }

 public SecurityManager securityManager(ShiroRealm shiroRealm,
 ShiroSessionManager sessionManager, // 具体类型，非 SessionManager 接口
 RedisCacheManager cacheManager) { ... }
 ```

2. **Advisor 延迟回填 securityManager**：`authorizationAttributeSourceAdvisor()` 创建期不再注入 `securityManager`，改由 `SmartInitializingSingleton` 在所有单例就绪后回填。注解式鉴权拦截器只在请求期用到 securityManager，此时早已回填完成。

 ```java
 @Bean
 public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
 return new AuthorizationAttributeSourceAdvisor(); // 创建期不依赖 securityManager
 }

 @Bean
 public SmartInitializingSingleton shiroSecurityManagerInitializer(
 AuthorizationAttributeSourceAdvisor advisor, SecurityManager securityManager) {
 return () -> advisor.setSecurityManager(securityManager);
 }
 ```

结果：`Started KnowledgeApplication`，8090 正常监听。改动在共享的 `moli-user-center-shiro-starter`，user-center 等接入方同样受益。

### 排错经验

- **看完整嵌套异常链 + cycle 框，不要只看折叠的单行**：`parameter 1` 这一线索直接指向 `SessionManager` 参数歧义。
- **同样的报错反复出现，先怀疑构建/进程未刷新**：本次排障期间有十几个卡住 jar、仍跑旧代码的僵尸 JVM 在干扰；务必先 `mvn install` starter 到 `.m2`，杀掉残留 java 进程、确认端口空闲，再做「干净一次」的验证。
- 接口继承关系（`SecurityManager extends SessionManager`）是 Spring 按类型自动装配歧义的隐蔽来源，按具体类注入是最稳的消歧方式。

详见 [[spring-aop与代理]]、体系枢纽 [[shiro-鉴权体系]]。
