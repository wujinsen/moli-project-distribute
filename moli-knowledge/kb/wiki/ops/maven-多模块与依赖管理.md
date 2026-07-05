---
title: Maven 多模块与依赖管理
slug: maven-多模块与依赖管理
type: guide
status: active
tags: [Maven, 构建, 依赖, 运维]
sources:
- raw/wujinsen_markdown/javaweb/Maven/Maven Scope取值的含义.note.md
- raw/wujinsen_markdown/插件/PageHelper/PageHelper采坑问题记录.note.md
- raw/wujinsen_markdown/插件/maven/maven deploy部署SNATSHOP到仓库jar包带时间戳问题.note.md
- raw/wujinsen_markdown/插件/maven/maven常用命令.note.md
- raw/wujinsen_markdown/插件/maven/用dependency插件解决依赖包冲突.note.md
- raw/wujinsen_markdown/插件/swagger/swagger注解.note.md
- raw/wujinsen_markdown/架构/DevOps/nexus/maven---nexus私服配置setting和pom.note.md
related: [jenkins-ci入门, mybatis-与-druid持久层, java-编码规范与CodeReview要点]
created: 2026-07-05
updated: 2026-07-05
---

# Maven 多模块与依赖管理

## 1. 多模块结构

```
parent (pom packaging)
├── common
├── api
└── server
```

父 POM 统一 `dependencyManagement` 与插件版本；子模块继承。

## 2. Scope 含义（raw 摘要）

| Scope | 说明 |
|-------|------|
| compile | 默认；编译+运行+测试 |
| provided | 容器提供，如 servlet-api |
| runtime | 运行需要，如 JDBC 驱动 |
| test | 仅测试 |

## 3. 依赖冲突

`mvn dependency:tree` 查传递依赖；`<exclusions>` 排除；`<dependencyManagement>` 统一版本。

## 4. 与 CI

打包发布见 [[ops/jenkins-ci入门]]；持久层见 [[database/mybatis-与-druid持久层]]。

## 批次#1324 增补（wujinsen Phase2 长尾）

合并 `插件/maven/` 与 javaweb Maven Scope raw。
