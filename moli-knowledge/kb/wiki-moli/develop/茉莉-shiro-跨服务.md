---
title: 茉莉 Shiro 跨服务
slug: 茉莉-shiro-跨服务
type: article
status: active
tags: [茉莉, Shiro, 鉴权, Starter, Actuator, Prometheus, P0]
sources:
  - moli-user-center/moli-user-center-shiro-starter/README.md
  - moli-user-center/moli-user-center-shiro-starter/src/main/java/com/moli/user/center/starter/autoconfigure/UserCenterShiroAutoConfiguration.java
  - moli-user-center/moli-user-center-server/src/main/java/com/moli/user/center/server/config/shiro/ShiroConfig.java
  - docs/ops/monitoring-and-logs.md
  - docs/ops/idea-local-dev.md
related: [认证与会话机制, 茉莉登录与鉴权故障根因汇总, 故障排查指南, 监控与日志, 用户中心, 知识库服务]
created: 2026-06-21
updated: 2026-09-01
---

# 茉莉 Shiro 跨服务

> 机制总览见 [[认证与会话机制]]；登录/token 操作见 [[登录与鉴权指南]]。  
> 工程契约：`moli-user-center-shiro-starter` · `docs/ops/monitoring-and-logs.md` §4.3。

## 1. 模块分工

| 模块 | 职责 |
|------|------|
| `moli-user-center-server` | **唯一登录入口**；本地 `ShiroConfig` + `ShiroRealm` 查 DB |
| `moli-user-center-shiro-starter` | order / ai / knowledge 等 **只校验 Session**，禁止本地 login |
| `moli-user-center-api` | Dubbo `UserCenterServer`（`getUserById`、`getPermissionsByUserId`） |

接入方依赖 starter + **与 user-center 相同的 Redis**（含 `database`）。详见 [[茉莉登录与鉴权故障根因汇总]] §4。

## 2. Starter 自动配置要点

类：`UserCenterShiroAutoConfiguration`（`META-INF/spring.factories`）。

| Bean | 说明 |
|------|------|
| `shiroFilterFactory` | Filter 链：`/swagger-*`、`/actuator/**` → `anon`；`/**` → `authc` |
| `AuthenticationFilter` | 未登录返回 JSON `10006`，**须在工厂方法内 `new`，不得 `@Bean`**（见 §4） |
| `SecurityManager` | 注入具体类型 `ShiroSessionManager`，避免接口自循环 |
| `AuthorizationAttributeSourceAdvisor` | 启动期用 `SmartInitializingSingleton` 回填 securityManager，避免与 Springfox AOP 循环依赖 |

YAML 扩展白名单：

```yaml
moli:
  user-center:
    shiro:
      anon-paths:
        - /actuator/**
```

## 3. 与 user-center 本地 ShiroConfig 的差异

| 项 | user-center `ShiroConfig` | shiro-starter |
|----|---------------------------|---------------|
| 登录 | `/login` anon | 无 login（抛异常） |
| AuthenticationFilter | `new AuthenticationFilter()` | 同左（修复后） |
| `/actuator/**` | hardcode anon | hardcode anon |
| Dubbo | 本地 PermissionService | `@DubboReference UserCenterServer` |

**为何 user-center 监控正常、knowledge 曾异常**：user-center 从未把 `AuthenticationFilter` 注册为 Spring Bean；starter 旧版曾 `@Bean`，触发 Boot 全局 Filter 注册。

## 4. Actuator / Prometheus 踩坑（2026-09）

### 4.1 现象

- `GET /actuator/health`、`/actuator/prometheus` → `{"code":10006,"msg":"请登录"}`
- Prometheus Targets：knowledge / order / ai **DOWN**（JSON 非 Prometheus 格式）
- user-center 28101 同路径正常

### 4.2 易误判

| 误判 | 为何不对 |
|------|----------|
| `.m2` JAR 太旧 | `javap` 已见 `/actuator/**`，问题仍在 |
| 只改 YAML `anon-paths` | Shiro 链已对，全局 Filter 仍拦截 |
| 拆 `management.server.port` | 绕过症状，非根因 |

### 4.3 根因

Spring Boot 2.x：**任意 `Filter` 类型 `@Bean` 会自动注册为 Servlet Filter（`/*`）**。

旧 starter：

```java
@Bean
public AuthenticationFilter authenticationFilter() { ... }  // ❌ Boot 全局注册

@Bean
public ShiroFilterFactoryBean shiroFilterFactory(..., AuthenticationFilter f) {
    factory.getFilters().put("authc", f);  // Shiro 链里也有，但全局 Filter 先/并行拦截 actuator
}
```

请求 `/actuator/prometheus` 时，全局 `AuthenticationFilter` 无 Shiro anon 概念 → **10006**。

### 4.4 修复

```java
// UserCenterShiroAutoConfiguration — 与 ShiroConfig 一致
AuthenticationFilter authenticationFilter = new AuthenticationFilter();
authenticationFilter.setUserCenterServer(userCenterServer);
shiroFilterFactoryBean.getFilters().put("authc", authenticationFilter);
// 不要 @Bean authenticationFilter()
```

**操作**：`mvn install` shiro-starter + 业务模块 → IDEA Rebuild + 重启 `(dev)`。

**验证**：

```powershell
curl.exe -s http://127.0.0.1:28104/actuator/health
curl.exe -s http://127.0.0.1:28104/actuator/prometheus | Select-Object -First 3
```

### 4.5 扩展：若必须暴露 Filter 为 Bean

参考 Spring Boot 文档，用 `FilterRegistrationBean` 并 `setEnabled(false)`，仅让 Shiro 管理该 Filter。茉莉项目**不需要**此路径——直接 `new` 即可。

## 5. 启动期循环依赖（knowledge 等）

knowledge 引入 Springfox + Shiro AOP 时，曾出现 `securityManager` 循环创建。starter 已处理：

1. `AuthorizationAttributeSourceAdvisor` 创建时不注入 `SecurityManager`
2. `SmartInitializingSingleton` 全单例就绪后 `setSecurityManager`
3. `SecurityManager` 按 **具体类** `ShiroSessionManager` 注入，不按接口

见 `UserCenterShiroAutoConfiguration` 内注释。

## 6. 相关

- [[认证与会话机制]] — Session / Redis / 过滤器链
- [[茉莉登录与鉴权故障根因汇总]] — 10006 / 401 对照表
- [[监控与日志]] — Loki / Prometheus 运维
- [[故障排查指南]] — § Prometheus Targets
- `docs/ops/monitoring-and-logs.md` — 工程权威 §4.3
