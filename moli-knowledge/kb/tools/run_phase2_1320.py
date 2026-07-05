#!/usr/bin/env python3
"""Phase 2 batch #1320: P0 enrich + Phase1 tail + create ops/shell-脚本入门."""
from __future__ import annotations

from pathlib import Path

from wujinsen_ingest_lib import (
    WIKI,
    append_log,
    apply_enrich_batch,
    build_slug_sources,
    find_prefix_by_marker,
    list_raw_md,
)

TODAY = "2026-07-05"
BATCH = "#1320"
LABEL = "wujinsen Phase2 P0"

_ALGO_PREFIX = find_prefix_by_marker("链表反转.note.md") or "数据结构及算法"

P1320_SINGLE: dict[str, str] = {
    "源码分析/dubbo": "middleware/dubbo-调用原理与分层",
    "源码分析/OpenFeign": "middleware/openfeign-与-http客户端",
    "源码分析/RocketMQ": "middleware/rocketmq-事务消息实践",
    "源码分析/Kafka": "middleware/kafka-与-mq选型",
    "源码分析/MyCat": "database/sharding-分库分表入门",
    "源码分析/nacos": "middleware/nacos-注册与配置",
    "源码分析/OpenJDK": "java/jvm-内存与gc",
    "源码分析/spring": "spring/spring-ioc与bean生命周期",
    "架构/DevOps/jenkins": "ops/jenkins-ci入门",
    "DataBase/中间件": "middleware/消息队列",
}

P1320_MULTI: dict[str, list[str]] = {
    "jvm": [
        "java/jvm-面试题",
        "java/jvm-内存与gc",
        "java/jvm-垃圾收集算法与收集器",
        "java/jvm-gc调优实战",
        "java/jvm-oom与排查入门",
    ],
    "jvm/GC": ["java/jvm-垃圾收集算法与收集器"],
    "jvm/调优": ["java/jvm-gc调优实战", "java/java-cpu-100排查实战"],
    "Spring": [
        "spring/spring-mvc请求流程",
        "spring/spring-容器面试题",
        "spring/spring-boot-自动配置",
        "spring/enableautoconfiguration原理",
        "spring/spring-boot-面试题",
        "spring/spring-事务",
        "spring/spring-声明式事务",
        "spring/spring-三级缓存与循环依赖",
        "spring/spring-ioc与bean生命周期",
        "spring/spring-application启动流程",
        "patterns/spring框架中的设计模式",
    ],
    "并发编程/Netty": [
        "middleware/netty-reactor与线程模型",
        "middleware/netty-pipeline与编解码",
        "middleware/io模型与-netty",
    ],
    "并发编程/java": ["java/java-并发面试题", "java/bio-nio-aio对比"],
    "Linux": ["ops/linux-运维基础"],
    "Linux/Shell教程": ["ops/linux-运维基础", "ops/shell-脚本入门"],
    "前端/Vue": ["frontend/前端技术栈", "frontend/前端基础面试题"],
    "前端": [
        "frontend/前端技术栈",
        "frontend/前端基础面试题",
        "middleware/跨域与前后端分离",
    ],
    "javaweb/jwt": ["security/api-接口安全设计", "security/认证与会话机制"],
    "javaweb/Mybatis": [
        "database/mybatis-与-druid持久层",
        "database/mybatis-plus-用法与注入防护",
    ],
    "架构/MicroServer/SpringBoot": [
        "spring/spring-boot-面试题",
        "spring/spring-boot-自动配置",
    ],
    "架构/MicroServer/SpringCloud": [
        "middleware/feign-开发踩坑",
        "middleware/sentinel-限流与熔断",
    ],
    "架构/MicroServer": [
        "middleware/feign-开发踩坑",
        "middleware/dubbo-调用原理与分层",
        "middleware/消息队列",
        "spring/spring-cloud-gateway",
    ],
    "架构/分库分表": ["database/sharding-分库分表入门"],
    "架构/性能监控": ["ops/skywalking-安装与链路追踪"],
}
P1320_MULTI[_ALGO_PREFIX] = ["patterns/算法面试题精选"]

P1320_FILES: dict[str, list[str]] = {
    "database/mysql-索引面试题": [
        "DataBase/MySQL外键设置中的的 Cascade、NO ACTION、Restrict、SET NULL.note.md",
        "DataBase/left join on 和where条件的放置.note.md",
    ],
    "database/sharding-分库分表入门": ["架构/分库分表/分库分表.note.md"],
    "middleware/feign-开发踩坑": [
        "架构/MicroServer/SpringBoot/quesion/在@RestController的方法中，请求路径参数.(标题)被截断，怎么配置？.note.md",
    ],
    "spring/spring-cloud-gateway": [
        "架构/MicroServer/SpringCloud/SpringCloud基础/SpringCloud基础.note.md",
    ],
    "spring/spring-boot-自动配置": [
        "架构/MicroServer/SpringBoot/2.0/Spring Boot 2.0 - WebFlux framework.note.md",
        "架构/MicroServer/SpringBoot/Spring Boot修改JDK版本配置.note.md",
        "架构/MicroServer/SpringBoot/server.jsp-servlet.init-parameters.development=true.note.md",
    ],
}

NOTES: dict[str, str] = {
    "java/jvm-面试题": "合并 `jvm/` 全树及 GC/调优 raw sources。",
    "java/jvm-垃圾收集算法与收集器": "合并 `jvm/GC/` 收集器对比 raw。",
    "java/jvm-gc调优实战": "合并 `jvm/调优/` 与架构性能调优 raw。",
    "spring/spring-mvc请求流程": "合并 `Spring/SpringMVC/` 原理与设计模式 raw。",
    "spring/spring-三级缓存与循环依赖": "合并 Spring 循环依赖与 IOC 解析 raw。",
    "middleware/netty-reactor与线程模型": "合并 `并发编程/Netty/` Reactor raw。",
    "ops/linux-运维基础": "合并 `Linux/` 运维与 Shell 教程 raw。",
    "ops/shell-脚本入门": "新建页；sources 来自 `Linux/Shell教程/`。",
    "frontend/前端技术栈": "合并 `前端/Vue/` 与交互方式 raw。",
    "frontend/前端基础面试题": "合并 jQuery/JS 面试向 raw。",
    "middleware/跨域与前后端分离": "合并 `前端/` 跨域方案 raw。",
    "security/api-接口安全设计": "合并 `javaweb/jwt/` JWT 实践 raw。",
    "database/mybatis-与-druid持久层": "合并 MyBatis #{} ${} 与 order by 注入防护 raw。",
    "patterns/算法面试题精选": "合并 `数据结构及算法/` 八篇（DP/哈希/B树/链表等）。",
    "middleware/dubbo-调用原理与分层": "合并 `源码分析/dubbo/` SPI raw。",
    "middleware/openfeign-与-http客户端": "合并 OpenFeign 源码阅读 raw。",
    "middleware/rocketmq-事务消息实践": "合并 RocketMQ 事务消息源码 raw。",
    "database/sharding-分库分表入门": "合并 MyCat 源码与架构分库分表 raw。",
    "ops/jenkins-ci入门": "补挂 `架构/DevOps/jenkins/` Maven 构建 raw。",
}

BODY_EXTRAS: dict[str, str] = {
    "patterns/算法面试题精选": """
## Q3. 一致性哈希

**场景**：分布式缓存节点扩缩容时，尽量减少 key 迁移。

**要点**：将哈希空间构成环；节点与 key 均哈希落环；顺时针找第一个节点。虚拟节点平衡负载。

## Q4. 反转链表

迭代：`prev=null`，每次 `next=cur.next; cur.next=prev;` 推进。空间 O(1)。

## Q5. 二叉树遍历

前/中/后序递归或栈；层序用队列 BFS。面试常考「中序+前序还原树」。

## Q6. 哈希表原理

数组 + 链表/红黑树解决冲突；负载因子触发 rehash。见 [[java/hashmap-面试题]]。
""",
    "security/认证与会话机制": """
## JWT 与 Session（raw javaweb/jwt）

| | Session | JWT |
|---|---------|-----|
| 状态 | 服务端存 | 自包含，无服务端会话 |
| 扩展 | 需 sticky/共享存储 | 适合跨域 API |
| 撤销 | 删 session | 短 TTL + 黑名单 |

Cookie 存 token 时注意 `HttpOnly`、`Secure`、CSRF 防护。见 [[security/api-接口安全设计]]。
""",
    "database/mybatis-plus-用法与注入防护": """
## #{} 与 ${}（raw MyBatis）

- **`#{}`**：预编译占位，**防 SQL 注入**（推荐）
- **`${}`**：字符串替换，仅用于**表名/列名/order by** 等白名单场景

`${}` 拼接用户输入会导致注入；动态排序需枚举校验。
""",
    "ops/shell-脚本入门": """
## 常用片段

```bash
# 变量与默认值
name=${{1:-default}}
# 命令替换
files=$(ls *.log 2>/dev/null)
# 循环
for f in "$@"; do echo "$f"; done
```

引号：双引号保留变量；单引号字面量。管道与 `$?` 检查上一条退出码。
""",
}


def create_shell_page() -> None:
    slug = "ops/shell-脚本入门"
    path = WIKI / "ops" / "shell-脚本入门.md"
    if path.exists():
        return
    sources = list_raw_md("Linux/Shell教程")
    src_yaml = "\n".join(f" - {s}" for s in sources)
    content = f"""---
title: Shell 脚本入门
slug: shell-脚本入门
type: guide
status: active
tags: [Linux, Shell, 运维]
sources:
{src_yaml}
related: [linux-运维基础, jenkins-ci入门, 生产环境服务启停脚本]
created: {TODAY}
updated: {TODAY}
---

# Shell 脚本入门

> 运维脚本、CI 前置检查、日志归档常用 Bash。系统命令见 [[ops/linux-运维基础]]。

## 1. 基础语法

| 项 | 说明 |
|----|------|
| Shebang | `#!/bin/bash` |
| 变量 | `name=value`，引用 `$name` 或 `${{name}}` |
| 条件 | `[ "$a" = "b" ]` 或 `[[ ... ]]` |
| 测试文件 | `-f` 文件 · `-d` 目录 · `-x` 可执行 |

## 2. 引号与特殊字符

- **双引号**：展开变量与命令替换
- **单引号**：纯字面
- **反引号 / `$()`**：命令替换

## 3. 实用示例（raw 摘要）

```bash
# basename / dirname
base=$(basename "$0")
dir=$(dirname "$0")
# 防火墙开放端口（需 root）
# firewall-cmd --add-port=8080/tcp --permanent && firewall-cmd --reload
```

## 4. 与 Jenkins

构建前检查磁盘、拉代码、打包见 [[ops/jenkins-ci入门]]。
"""
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")
    print("CREATE", slug)


def main() -> None:
    (WIKI / "bigdata").mkdir(parents=True, exist_ok=True)
    create_shell_page()

    slug_sources = build_slug_sources(P1320_SINGLE, P1320_MULTI, P1320_FILES)
    touched = apply_enrich_batch(slug_sources, TODAY, BATCH, LABEL, NOTES, BODY_EXTRAS)

    append_log(
        TODAY,
        BATCH,
        f"批次{BATCH} wujinsen Phase2 P0 → enrich {len(touched)} 页 + create ops/shell-脚本入门 "
        f"({', '.join(touched[:6])}{'…' if len(touched) > 6 else ''})",
    )
    print("Touched", len(touched), "pages")


if __name__ == "__main__":
    main()
