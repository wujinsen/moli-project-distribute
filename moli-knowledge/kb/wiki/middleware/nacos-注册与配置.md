---
title: Nacos 注册与配置
slug: nacos-注册与配置
type: article
status: active
tags: [nacos, 注册发现, 配置中心]
sources:
 - raw/wujinsen_markdown/moli项目/使用Nacos作为配置中心和服务注册发现.note.md
 - raw/wujinsen_markdown/moli项目/运维/moli项目配置.note.md
related: [dubbo-与-nacos, 本地启动指南, 服务调用与架构, 故障排查指南, nacos-config动态配置实践]
created: 2026-06-22
updated: 2026-06-22
---

# Nacos 注册与配置

> 枢纽 [[middleware/dubbo-与-nacos]]；本地启动。

## 服务注册发现

依赖：

```xml
<dependency>
 <groupId>com.alibaba.cloud</groupId>
 <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

`bootstrap.yml` 示例：

```yaml
spring:
 application:
 name: user-center-server
 cloud:
 nacos:
 discovery:
 server-addr: 127.0.0.1:8848
 namespace: dev
```

启动后可在 Nacos 控制台「服务管理」看到实例。Dubbo 注册地址通常指向同一 Nacos。

## 配置中心（可选）

```xml
<artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
```

扩展配置 `ext-config` 可拆 `datasource.properties`、`redis.properties` 等到 Nacos。**当前常见 dev 以本地 `application-dev.yml` 为主**，Nacos Config 在 `bootstrap.yml` 中为 `enabled: false`。启用与动态刷新见 [[middleware/nacos-config动态配置实践]]。

## 排查

- 控制台无实例 → 服务未启动、`server-addr` 错、namespace 不一致
- Dubbo 无 Provider → 查 Nacos 服务列表 + dubbo `group`/`version`
- 详见

## 与 Gateway

Gateway 通过 Nacos Discovery + `lb://service-name` 转发，StripPrefix 后打到各服务 context-path。
