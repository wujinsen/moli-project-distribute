---
title: Java 编码规范与 Code Review 要点
slug: java-编码规范与CodeReview要点
type: guide
status: active
tags: [Java, 编码规范, CodeReview, MyBatis]
sources:
- raw/wujinsen_markdown/架构/编码规范/程序编码/如何更规范化编写Java 代码.note.md
related: [mybatis-plus-用法与注入防护, hashmap-面试题, java-集合框架, concurrenthashmap原理]
created: 2026-07-05
updated: 2026-07-05
---

# Java 编码规范与 Code Review 要点

> MyBatis 安全见 [[database/mybatis-plus-用法与注入防护]]。集合/并发见 [[java/java-集合框架]]、[[java/concurrenthashmap原理]]。

## 1. MyBatis 动态 SQL

**反例**：`WHERE 1=1` + `<if>` — 优化器难用索引，且有注入风险。

**正例**：`<where>` / `<set>` 标签，条件为空时自动去掉多余 AND。

## 2. 集合与 Map

| 场景 | 反例 | 正例 |
|------|------|------|
| 遍历 Map 要 key+value | 先 `keySet()` 再 `get` | `entrySet()` |
| 判空 | `size() == 0` | `isEmpty()`（O(1)） |
| 已知容量 | `new ArrayList<>()` 默认扩容 | `new ArrayList<>(n)` |
| 频繁 `contains` | `List.contains` O(n) | 转 `HashSet` O(1) |

## 3. 字符串

- 循环内拼接用 **`StringBuilder`**，勿 `+=`。
- 转字符串用 **`String.valueOf(x)`**，少用 `"" + x`。
- **`split(regex)`** 对 `.` `|` 等需转义：`split("\\.")`。

## 4. 数值与对象

- **`BigDecimal`** 用 `BigDecimal.valueOf(0.1)`，勿 `new BigDecimal(0.1)`（精度坑）。
- **`equals`** 用常量在左：`"ok".equals(var)` 或 `Objects.equals(a,b)`。
- 返回集合/数组**空实例**，勿 `null`（`Collections.emptyList()`）。

## 5. 枚举与工具类

- 枚举字段 **`private final`**，无 public setter。
- 工具类 **`private` 构造函数**，禁止实例化。

## 6. 异常

- 勿 `catch` 后只 `throw e`；要么处理，要么去掉 catch 声明 throws。

## 7. Code Review 清单（摘要）

1. SQL / 索引 / N+1
2. 并发与锁范围
3. 资源关闭（try-with-resources）
4. 日志与敏感信息
5. 单元测试覆盖核心分支
