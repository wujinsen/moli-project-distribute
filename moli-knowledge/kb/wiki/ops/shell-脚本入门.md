---
title: Shell 脚本入门
slug: shell-脚本入门
type: guide
status: active
tags: [Linux, Shell, 运维]
sources:
- raw/wujinsen_markdown/Linux/Shell教程/Linux Shell 中的反引号，单引号，双引号.note.md
- raw/wujinsen_markdown/Linux/Shell教程/Shell 传递参数(三).note.md
- raw/wujinsen_markdown/Linux/Shell教程/Shell 传递参数(四).note.md
- raw/wujinsen_markdown/Linux/Shell教程/Shell 变量(二).note.md
- raw/wujinsen_markdown/Linux/Shell教程/Shell教程.note.md
- raw/wujinsen_markdown/Linux/Shell教程/linux basename命令学习.note.md
- raw/wujinsen_markdown/Linux/Shell教程/防火墙开放端口.note.md
related: [linux-运维基础, jenkins-ci入门, 生产环境服务启停脚本]
created: 2026-07-05
updated: 2026-07-05
---

# Shell 脚本入门

> 运维脚本、CI 前置检查、日志归档常用 Bash。系统命令见 [[ops/linux-运维基础]]。

## 1. 基础语法

| 项 | 说明 |
|----|------|
| Shebang | `#!/bin/bash` |
| 变量 | `name=value`，引用 `$name` 或 `${name}` |
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

## 批次#1320 增补（wujinsen Phase2 P0）

新建页；sources 来自 `Linux/Shell教程/`。
