---
title: Flyway 数据库版本迁移
slug: flyway-数据库版本迁移
type: article
status: active
tags: [MySQL, DevOps, 数据库]
sources:
 - raw/wujinsen_markdown/
related: [数据库初始化指南, api-向后兼容策略, git-分支与发布策略, 蓝绿与滚动发布]
created: 2026-06-21
updated: 2026-06-21
---

# Flyway 数据库版本迁移

> 初始化 ；API 兼容 [[middleware/api-向后兼容策略]]；发布 [[ops/蓝绿与滚动发布]]。

**Flyway** 用版本化 SQL 管理 schema 变更，替代手工执行零散脚本。

## 1. 约定

```
db/migration/
 V1__baseline.sql
 V2__seckill_tables.sql
 V3__kb_schema.sql
```

- `V{version}__{description}.sql` 只增不改历史
- **`flyway_schema_history`** 记录已执行版本

## 2. Spring Boot 集成

```yaml
spring.flyway:
 enabled: true
 locations: classpath:db/migration
 baseline-on-migrate: true
```

启动时自动 migrate；多服务共享库需 **统一 migration 模块** 或严格分工。

## 4. 安全原则

| 规则 | 原因 |
|------|------|
| 禁止 DROP 生产列 | 向后兼容 [[middleware/api-向后兼容策略]] |
| 大表 DDL 低峰 + pt-osc | 锁表风险 |
| 可回滚应用，难回滚 DB | 先扩后删 |

## 5. vs Liquibase

Flyway 纯 SQL 直观；Liquibase XML/YAML 差分。 SQL 文化选 Flyway 即可。

## 相关

[[mysql-备份与恢复]] · [[mysql-主从读写分离]]
