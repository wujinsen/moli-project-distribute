---
title: Druid 连接池泄漏排查
slug: druid-连接池泄漏排查
type: article
status: active
tags: [MySQL, Druid, 排查]
sources:
 - raw/wujinsen_markdown/
related: [druid连接池与监控, 故障排查指南, mybatis-与-druid持久层]
created: 2026-06-21
updated: 2026-06-21
---

# Druid 连接池泄漏排查

> 池配置 [[druid连接池与监控]]；总指南 ；持久层 [[mybatis-与-druid持久层]]。

连接池「泄漏」= 借出连接未归还，活跃数持续涨直到 `maxActive` 耗尽。

## 1. 现象

- 日志 `GetConnectionTimeoutException` / 等待超时
- Druid 监控 **Active 持续接近 MaxActive**，Pending 排队
- 业务随机超时，重启暂时恢复

## 2. 常见根因

| 根因 | 说明 |
|------|------|
| 未关 Connection/Statement | try-with-resources 缺失 |
| 长事务 | `@Transactional` 内 RPC/HTTP 阻塞 |
| 手动 getConnection | 绕过 MyBatis 未 close |
| 池配置过小 | 非泄漏，需调参 |

## 3. 排查步骤

1. Druid `/druid/sql.html` 看 **ActiveConnectionStackTrace**（需 `removeAbandoned=true`）
2. `SHOW PROCESSLIST` / `information_schema.innodb_trx` 找长事务
3. Arthas `thread -b` 看阻塞链 [[arthas-在线诊断]]
4. 对照代码：所有 JDBC 资源在 `finally` 关闭

## 相关

[[mysql-死锁与排查]] · [[jvm-oom与排查入门]]
