---
title: MyBatis-Plus 用法与注入防护
slug: mybatis-plus-用法与注入防护
type: article
status: active
tags: [mybatis, mybatis-plus, SQL注入, 持久层]
sources:
 - raw/wujinsen_markdown/javaweb/Mybatis/mybatis的#{}和${}的区别以及order by注入问题.note.md
 - raw/wujinsen_markdown/架构/编码规范/程序编码/如何更规范化编写Java 代码.note.md
related: [mybatis-与-druid持久层, mysql-索引, spring-声明式事务, 字段级数据权限设计]
created: 2026-06-22
updated: 2026-06-22
---

# MyBatis-Plus 用法与注入防护

> 持久层枢纽 [[mybatis-与-druid持久层]]；列级权限扩展 [[字段级数据权限设计]]。

## 1. `#{}` vs `${}`

| 写法 | 机制 | 安全性 |
|------|------|--------|
| **`#{}`** | PreparedStatement 占位符，预编译 | **安全**，防 SQL 注入 |
| **`${}`** | 字符串拼接替换 | **不安全**，仅特定场景 |

```xml
<!-- 安全：参数绑定 -->
<select id="byName">
 SELECT * FROM sys_user WHERE user_name = #{name}
</select>

<!-- 危险：仅白名单字段 -->
<select id="orderBy">
 SELECT * FROM sys_user ORDER BY ${column}
</select>
```

### 为什么 ORDER BY 不能用 `#{}`

`ORDER BY #{column}` 会变成 `ORDER BY 'user_name'`（带引号字符串），**语法错误且无法排序**。列名、表名、ORDER BY 片段只能 `${}`，但必须**白名单校验**：

```java
Set<String> allowed = Set.of("user_name", "create_time");
if (!allowed.contains(column)) throw new IllegalArgumentException();
```

### LIKE 写法

推荐：

```sql
WHERE name LIKE CONCAT('%', #{keyword}, '%')
```

避免 `${}` 直接拼用户输入。

## 2. MyBatis-Plus 常用 API

```java
// CRUD
userMapper.selectById(id);
userMapper.insert(entity);

// 条件
LambdaQueryWrapper<SysUser> w = Wrappers.lambdaQuery();
w.eq(SysUser::getStatus, 1).like(SysUser::getUserName, keyword);
userMapper.selectPage(new Page<>(1, 20), w);

// 更新
userMapper.updateById(entity);
```

Plus 生成的 CRUD 均用 `#{}`；自定义 `@Select` 字符串拼接需注意注入。

## 3. 项目规范

| 规范 | 原因 |
|------|------|
| 值一律 `#{}` | 防注入 |
| 动态列/排序白名单 | `${}` 唯一安全用法 |
| 避免 `WHERE 1=1` 手拼 | 用 Wrapper / `<where>` |
| 禁止 `${}` 拼用户输入的 WHERE 值 | 经典注入面 |

编码规范笔记：**不要为多条件查询写 `1=1` 开头**，用 MyBatis 动态标签或 Plus Wrapper。

## 4. 与事务

Mapper 方法本身无事务；**Service 类** `@Transactional` 包住多次 Mapper 调用（见 [[spring-声明式事务]]）。

注意：同类自调用事务不生效；只读查询可 `readOnly=true`。

## 5. 扩展：字段级权限

[[字段级数据权限设计]] 提出拦截器改 SELECT 列——若用 `${}` 拼列名，同样要白名单；更稳妥是元数据驱动 + `#{}` 参数化条件。

## 6. 面试速记

- `#{}` → `?` 预编译；`${}` → 文本替换
- `${}` 场景：列名、表名、ORDER BY、部分 GROUP BY
- Plus `apply()` 写片段时警惕注入
- JDBC `PreparedStatement` 不能预编译标识符（列名）
