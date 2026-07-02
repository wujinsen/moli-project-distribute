---
title: Testcontainers 集成测试
slug: testcontainers-集成测试
type: article
status: active
tags: [测试, Docker, MySQL]
sources:
 - raw/wujinsen_markdown/
related: [junit5-单元测试, docker部署指南, 数据库初始化指南]
created: 2026-06-21
updated: 2026-06-21
---

# Testcontainers 集成测试

> JUnit5 [[junit5-单元测试]]；Docker ；初始化。

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

[[mockito-测试实战]] · [[middleware/pact-契约测试入门]]
