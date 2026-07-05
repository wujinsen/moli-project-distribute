---
title: Testcontainers 集成测试
slug: testcontainers-集成测试
type: article
status: active
tags: [测试, Docker, MySQL]
sources:
- raw/wujinsen_markdown/ (enterprise-kb/database 专题页)
related: [pact-契约测试入门]
created: 2026-06-21
updated: 2026-07-05
---

# Testcontainers 集成测试

> JUnit5 + Docker 容器；库表初始化见 `moli-knowledge/kb/wiki-moli/guides/数据库初始化指南.md`。

JUnit 5 测试中 **Docker 拉起** MySQL/Redis，真实依赖集成验证。

## 1. 示例

```java
@Container
static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

@Test
void loginFlow() {
 // 指向 mysql.getJdbcUrl()
}
```

## 3. 注意

- 启动慢 → 复用容器 `@Testcontainers(disabledWithoutDocker = true)`
- 与 `scripts/moli.sql` schema 对齐

## 相关

[[java/java-编码规范与CodeReview要点]] · [[middleware/pact-契约测试入门]]
