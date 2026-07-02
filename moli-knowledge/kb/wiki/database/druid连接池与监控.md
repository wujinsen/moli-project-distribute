---
title: Druid 连接池与监控
slug: druid连接池与监控
type: article
status: active
tags: [druid, 连接池, 监控, prometheus]
sources:
 - moli-user-center/moli-user-center-server/src/main/resources/application-dev.yml
 - moli-user-center/moli-user-center-server/src/main/java/com/moli/user/center/server/config/DruidPoolMetricsConfiguration.java
 - moli-knowledge/kb/wiki/middleware/压测监控与prometheus.md
related: [mybatis-与-druid持久层, 故障排查指南, 压测监控与prometheus, mysql-深分页与慢sql优化, 限流算法与令牌桶, sentinel-限流与熔断]
created: 2026-06-22
updated: 2026-06-22
---

# Druid 连接池与监控

> 持久层总览 [[database/mybatis-与-druid持久层]]；压测看板 [[middleware/压测监控与prometheus]]；池耗尽排查。

Alibaba **Druid** 是各服务默认 JDBC 连接池（1.1.14），除池化外支持 **SQL 监控、防泄漏、Wall 防火墙**（按模块启用情况）。

## 1. 核心配置（dev 示例）

用户中心 `application-dev.yml`：

```yaml
spring:
 datasource:
 type: com.alibaba.druid.pool.DruidDataSource
 druid:
 initial-size: 10
 min-idle: 10
 max-active: 100
 max-wait: 10000
 validation-query: SELECT 1
 test-while-idle: true
 remove-abandoned: true
 remove-abandoned-timeout: 120
 log-abandoned: true
```

| 参数 | 说明 |
|------|------|
| max-active | 池上限；**本质是限流**（见 [[middleware/限流算法与令牌桶]]） |
| max-wait | 超等待时间抛异常 |
| remove-abandoned | 借出超时未还的连接强制回收 |
| test-while-idle | 空闲时检测连接有效 |

loadtest profile 可增大 `max-active` 以扛压测并发。

## 2. 连接池即「限流总资源数」

多服务共享 MySQL 时，每实例 `max-active` 之和不应远超 MySQL `max_connections`。否则：

- 本服务 `waiting` 线程飙升
- 他服务获取连接失败

压测前评估：**实例数 × max-active ≤ DB 承受能力**。

## 3. Prometheus 指标（用户中心已实现）

`DruidPoolMetricsConfiguration` 注册 Micrometer Gauge：

| 指标 | 含义 |
|------|------|
| `druid.pool.active` | 当前活跃连接 |
| `druid.pool.max` | 配置上限 |
| `druid.pool.waiting` | 等待连接的线程数 |
| `druid.pool.peak` | 历史峰值 active |
| `druid.pool.idle` | 空闲连接 |
| `druid.pool.create_error` | 创建连接失败次数 |

暴露路径：`/actuator/prometheus`（需 actuator 依赖与配置）。Grafana 看板见 [[middleware/压测监控与prometheus]]。

## 4. 告警解读

| 现象 | 可能原因 | 动作 |
|------|----------|------|
| active ≈ max 持续 | 慢 SQL / 长事务 / 泄漏 | EXPLAIN、查事务、看 abandoned 日志 |
| waiting > 0 增长 | 池过小或 DB 阻塞 | 调池/优化 SQL/限流入口 [[middleware/sentinel-限流与熔断]] |
| create_error 增 | DB 宕机/密码/网络 | |
| 压测后 active 不降 | 未 commit/未关流 | 代码审查 Mapper 用法 |

## 5. Druid 内置监控页（可选）

Druid 可配 `stat-view-servlet` 看 SQL 统计、URI 监控。开发环境 **未统一开启**；生产若开启需 **鉴权 + 勿公网暴露**。

## 6. 与慢 SQL 联动

1. Druid SQL 面板 / 慢日志 → 定位语句
2. `EXPLAIN` → [[database/mysql-索引]]、[[database/mysql-深分页与慢sql优化]]
3. 索引优化后 active 下降，往往比单纯加大 `max-active` 更有效

## 7. OOM vs 池耗尽

「获取连接超时」常被误认为 OOM。区分：

- **池耗尽**：Druid waiting 高、active=max
- **OOM**：heap dump；也可能因连接/对象泄漏间接导致

见 [[java/jvm-oom与排查入门]]。
