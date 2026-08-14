---
title: Nacos Config 动态配置实践
slug: nacos-config动态配置实践
type: article
status: active
tags: [nacos, 配置中心, RefreshScope]
sources:
- moli-user-center/moli-user-center-server/src/main/resources/bootstrap.yml
related: [nacos-注册与配置, dubbo-与-nacos]
created: 2026-06-22
updated: 2026-07-05
---

# Nacos Config 动态配置实践

> 注册发现 [[middleware/nacos-注册与配置]]；Dubbo 注册 [[middleware/dubbo-与-nacos]]。

## 2. 何时用 Nacos Config

| 场景 | 建议 |
|------|------|
| 本地单人 dev | 本地 yml 即可（现状） |
| 多环境 test/pre/pro | 配置放 Nacos，按 namespace 隔离 |
| 动态调参（限流阈值等） | Nacos + `@RefreshScope` |
| 敏感信息 | Nacos 权限 + 加密（或外部密钥管理） |

## 3. 接入步骤

### 3.1 依赖

```xml
<dependency>
 <groupId>com.alibaba.cloud</groupId>
 <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

### 3.2 bootstrap.yml

```yaml
spring:
 application:
 name: user-center-server
 cloud:
 nacos:
 config:
 enabled: true
 server-addr: 127.0.0.1:8848
 namespace: dev
 file-extension: yaml
 extension-configs:
 - data-id: datasource-dev.yaml
 group: moli-user-center
 refresh: true
 - data-id: redis-dev.yaml
 group: moli-user-center
 refresh: true
```

> raw 笔记用 `ext-config` + `datasource.properties` / `redis.properties` 拆分，原理相同；**data-id + group + namespace** 三者唯一确定配置。

### 3.3 Nacos 控制台

「配置管理」新建 Data ID，内容与本地 yml 片段对应。改配置后，带 `refresh: true` 的项可触发 Spring Cloud 刷新。

### 3.4 动态刷新

```java
@RefreshScope
@ConfigurationProperties(prefix = "moli.feature")
public class FeatureFlags { ... }
```

无 `@RefreshScope` 的 `@Value` 通常需重启生效。

## 4. 与 Discovery 分工

| 组件 | 职责 |
|------|------|
| **Nacos Discovery** | 实例列表 → Gateway `lb://`、Dubbo 注册 |
| **Nacos Config** | 键值配置，可热更新 |
| **本地 application-*.yml** | profile 默认、开关、Swagger 等 |

Dubbo `registry.address: spring-cloud://` 走 Discovery，**不替代** Config。

## 5. 多环境 namespace

| Profile | 典型 namespace |
|---------|----------------|
| dev | `dev`（名称或 ID） |
| test | 独立 UUID（见 user-center `bootstrap.yml` test 段） |

Discovery 与 Config 的 **namespace 必须一致**，否则「服务看见但配置错环境」。

## 6. 排查

| 现象 | 处理 |
|------|------|
| 配置不生效 | `enabled: true`？data-id/group/namespace 匹配？ |
| 刷新无效 | `refresh: true` + `@RefreshScope` |
| 启动读不到 | `bootstrap.yml` 先于 `application.yml` 加载 |
| 仍用本地 Redis | Config 未启用时正常 [[middleware/nacos-注册与配置]] |
