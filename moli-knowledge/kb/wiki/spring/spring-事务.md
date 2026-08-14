---
title: Spring 事务（面试题系列）
slug: spring-事务
type: interview
status: active
tags: [Spring, 事务, Transaction, 面试题, Java]
sources:
- raw/wujinsen_markdown/DataBase/mysql/数据库事务的四大特性以及事务的隔离级别.note.md
- raw/wujinsen_markdown/Spring/@Autowired注解实现原理（Spring Bean的自动装配）.note.md
- raw/wujinsen_markdown/Spring/Spring 事务管理探究.note.md
- raw/wujinsen_markdown/Spring/SpringBoot源码解析/@EnableAutoConfiguration自动装配.note.md
- raw/wujinsen_markdown/Spring/SpringBoot源码解析/@EnableAutoConfiguraton自动装配原理.note.md
- raw/wujinsen_markdown/Spring/SpringBoot源码解析/SpringApplication初始化阶段.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note.md
- raw/wujinsen_markdown/Spring/SpringMVC/Java 必须掌握的 12 种 Spring 常用注解！.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/ModelMap、ModelAndView和@Modelattribute的区别.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/Spring 中经典的 9 种设计模式，打死也要记住啊！.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/Spring 事务管理探究.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/Spring 最常用的 7 个注解，你用哪几个？.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/SpringMVC工作原理.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/SpringMVC接收复杂集合参数.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/defaultServlet.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/spring service事务传播.note.md
- raw/wujinsen_markdown/Spring/Spring、SpringMVC和SpringBoot看这一篇就够了！.note.md
- raw/wujinsen_markdown/Spring/Spring循环依赖原理解析.note.md
- raw/wujinsen_markdown/Spring/Spring源码分析：@Autowired注解原理分析.note.md
- raw/wujinsen_markdown/Spring/Spring解析，加载及实例化Bean的顺序（零配置）.note.md
- raw/wujinsen_markdown/Spring/事务/@Transactional失效的几种场景.note.md
- raw/wujinsen_markdown/Spring/事务/深入理解 Spring 事务原理.note.md
- raw/wujinsen_markdown/Spring/什么是循环依赖.note.md
- raw/wujinsen_markdown/Spring/深入理解 Spring 事务原理 传播属性.note.md
- raw/wujinsen_markdown/Spring/真实项目中 ThreadLocal 的妙用.note.md
- raw/wujinsen_markdown/Spring/采坑记录.note.md
- raw/wujinsen_markdown/Spring/采坑记录/springboot与web前端的下划线与驼峰的json转换配置.note.md
- raw/wujinsen_markdown/面试笔试/Spring/69道Spring面试题和答案.note.md
- raw/wujinsen_markdown/面试笔试/Spring/@transactional注解在什么情况下会失效，为什么。.note.md
- raw/wujinsen_markdown/面试笔试/Spring/一文带你深入理解 Spring 事务原理.note.md
- raw/wujinsen_markdown/面试笔试/Spring/介绍一下Spring的事务管理.note.md
- raw/wujinsen_markdown/面试笔试/Spring/关于Spring事务的面试题.note.md
related: [spring-声明式事务, spring-boot-自动配置]
created: 2026-06-22
updated: 2026-07-05
---

# Spring 事务（面试题系列）

> 概念枢纽 [[spring/spring-声明式事务]]。本页由 6 篇杂乱原文**去重提炼**而成（传播级别、隔离级别、失效场景在多篇中重复，已合并）。
> 按面试高频顺序组织，每题给「要点 + 关键陷阱」。

## Q1. 事务的四大特性 ACID？

- **原子性（Atomicity）**：一组操作要么全成功、要么全回滚，不可分割。
- **一致性（Consistency）**：事务前后数据库都处于一致状态（如转账前后总额不变）。
- **隔离性（Isolation）**：并发事务相互不干扰。
- **持久性（Durability）**：提交后改动永久生效，宕机也不丢。

> 底层：原子性/持久性靠 **undo log / redo log**，隔离性靠 **锁 + MVCC**，一致性是前三者 + 业务共同保证的结果。

## Q2. 并发事务会有哪些问题？

| 问题 | 含义 |
|------|------|
| **脏读** | 读到另一事务**未提交**的数据，对方一旦回滚就是脏数据 |
| **不可重复读** | 同一事务内两次读**同一行**，期间被别的已提交事务**改了**，值不一致 |
| **幻读** | 同一事务内两次按**范围**查，期间别的事务**插入/删除**，行数变了（针对一批数据） |

> 区别要点：不可重复读针对「同一行被 update」，幻读针对「一批数据 insert/delete」。

## Q3. 四种隔离级别？各能避免什么？

| 隔离级别 | 脏读 | 不可重复读 | 幻读 |
|----------|:----:|:----------:|:----:|
| Read Uncommitted（读未提交） | ✗ | ✗ | ✗ |
| Read Committed（读已提交） | ✓ | ✗ | ✗ |
| Repeatable Read（可重复读） | ✓ | ✓ | ✗ |
| Serializable（串行化） | ✓ | ✓ | ✓ |

（✓=可避免）

- **MySQL InnoDB 默认 Repeatable Read**（且通过 MVCC + 间隙锁在很大程度上避免了幻读）。
- **Oracle / SQL Server 默认 Read Committed**。
- 级别越高越安全，但并发性能越差（Serializable 近似锁表）。

## Q4. Spring 事务的本质/原理？

- **本质是对数据库事务能力的封装**——没有数据库事务支持，Spring 也变不出事务。
- 纯 JDBC 要手写：取连接 → `setAutoCommit(false)` → CRUD → `commit/rollback` → 关连接。Spring 用 **AOP** 把「开启/提交/回滚」织入，业务代码只写 CRUD。
- 统一抽象接口 **`PlatformTransactionManager`**，不同数据访问技术有不同实现（`DataSourceTransactionManager`、`JpaTransactionManager`、`HibernateTransactionManager`…）。
- `@Transactional` 是**基于 AOP 代理**：启动时为带注解的类/方法生成代理，方法正常结束提交、抛异常回滚。

## Q5. 七种传播行为（Propagation）？

| 传播行为 | 当前有事务 | 当前无事务 |
|----------|------------|------------|
| **REQUIRED**（默认） | 加入 | 新建 |
| **REQUIRES_NEW** | 挂起旧的，新建独立事务 | 新建 |
| **NESTED** | 嵌套子事务（带 savepoint） | 类似 REQUIRED 新建 |
| SUPPORTS | 加入 | 非事务执行 |
| NOT_SUPPORTED | 挂起，非事务执行 | 非事务执行 |
| MANDATORY | 加入 | 抛异常 |
| NEVER | 抛异常 | 非事务执行 |

**重点辨析 REQUIRES_NEW vs NESTED**：

- `REQUIRES_NEW`：父子是**两个独立事务**。子已提交后父回滚，子**不回滚**；子回滚（异常被父 catch）父可继续提交。典型场景：发红包记日志，子事务失败不影响父。
- `NESTED`：子是父的一部分，靠 **savepoint**。子回滚只回到 savepoint（父可走异常分支继续）；**父回滚则子一定跟着回滚**；提交由父统一提交。

## Q6. 编程式 vs 声明式事务？

- **声明式**（`@Transactional` / `<tx>` 标签）：基于 AOP，**侵入小、最常用**。
- **编程式**（`TransactionTemplate` / `PlatformTransactionManager` 手动 commit/rollback）：**控制最精确**（能把事务范围缩到最小），但代码冗余。大型/复杂逻辑里精确控制范围时用。

## Q7. `@Transactional` 失效的常见场景？（高频陷阱）

1. **自调用**：同类中 A 方法（无事务）调用本类的 B 方法（有事务）→ 不走代理，失效。最常见。
 - 解法：注入自身代理、拆到别的 Bean、或用 `AopContext.currentProxy()`。
2. **非 public 方法**：`@Transactional` 只对 public 生效（拦截器读不到属性）。
3. **异常类型不匹配**：默认只回滚 **unchecked 异常（RuntimeException/Error）**；checked 异常不回滚。
 - 解法：`@Transactional(rollbackFor = Exception.class)`。
4. **异常被 catch 吞掉**：方法内 try-catch 了异常没抛出 → 事务感知不到，不回滚。
5. **存储引擎不支持**：MySQL 用了 **MyISAM**（不支持事务），要用 **InnoDB**。
6. **未开启注解驱动 / 未被扫描**：没配 `<tx:annotation-driven>`（或 `@EnableTransactionManagement`）、包没被 component-scan 扫到。
7. **传播行为不参与事务**：如 `NOT_SUPPORTED` / `NEVER`，本就不在事务里跑。

## Q8. 常用属性？

- `readOnly = true`：只读事务，提示数据源优化（多条统计查询保证整体读一致性时用）。
- `rollbackFor` / `noRollbackFor`：自定义触发/不触发回滚的异常类型。
- `timeout`：超时时间，防大事务。
- `isolation` / `propagation`：隔离级别 / 传播行为。

---

## 延伸（知识库待补）

- 同主题若再 ingest 偏「源码/原理」的文章（如 `Spring 事务管理探究`、`深入理解 Spring 事务原理` 传播属性篇），建议另建 `articles/spring-事务原理`，并加 `concepts/spring-事务` 枢纽页用 `[[]]` 串联本面试页与文章页（见 `AGENTS.md` §5 同主题跨类型约定）。
## @Transactional 失效场景速查（raw 汇总）

1. **非 public** 方法
2. **同类自调用**（绕过代理）
3. **异常被吞**或未配置 `rollbackFor=Exception.class`
4. **传播行为**误用（如 `NOT_SUPPORTED`）
5. **数据库引擎**非 InnoDB

原理页 [[spring/spring-声明式事务]]。

## 批次#1313 增补（wujinsen P2）

确认 `面试笔试/Spring/` 五篇 raw 已挂接。

原文插图 annex：[[patterns/annex-SpringMVC接收复杂集合参数]]

## 原文插图（wujinsen）

> wujinsen 原文插图回迁（T22）· 共 3 组

> 图源 `raw/wujinsen_markdown/面试笔试/Spring/关于Spring事务的面试题.note.md` · T22 **B** 档

### 来自：关于Spring事务的面试题

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Spring/%E5%85%B3%E4%BA%8ESpring%E4%BA%8B%E5%8A%A1%E7%9A%84%E9%9D%A2%E8%AF%95%E9%A2%98.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/Spring/Spring解析，加载及实例化Bean的顺序（零配置）.note.md` · T22 **B** 档

### 来自：Spring解析，加载及实例化Bean的顺序（零配置）

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/Spring%E8%A7%A3%E6%9E%90%EF%BC%8C%E5%8A%A0%E8%BD%BD%E5%8F%8A%E5%AE%9E%E4%BE%8B%E5%8C%96Bean%E7%9A%84%E9%A1%BA%E5%BA%8F%EF%BC%88%E9%9B%B6%E9%85%8D%E7%BD%AE%EF%BC%89.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/Spring%E8%A7%A3%E6%9E%90%EF%BC%8C%E5%8A%A0%E8%BD%BD%E5%8F%8A%E5%AE%9E%E4%BE%8B%E5%8C%96Bean%E7%9A%84%E9%A1%BA%E5%BA%8F%EF%BC%88%E9%9B%B6%E9%85%8D%E7%BD%AE%EF%BC%89.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/Spring/SpringMVC/SpringMVC工作原理.note.md` · T22 **B** 档

### 来自：SpringMVC工作原理

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/SpringMVC/SpringMVC%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/SpringMVC/SpringMVC%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/SpringMVC/SpringMVC%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86.note_images/imageFile3.png)

原文插图 annex：[[patterns/annex-SpringMVC接收复杂集合参数]]

原文插图 annex：[[patterns/annex-ModelMap、ModelAndView和@Modelattribute的区别]]

原文插图 annex：[[patterns/annex-Spring、SpringMVC和SpringBoot看这一篇就够了！]]

原文插图 annex：[[database/annex-数据库事务的四大特性以及事务的隔离级别]]

原文插图 annex：[[patterns/annex-@RequestParam-@RequestBody-@PathVariable-等参数绑定注解详解(转)]]

原文插图 annex：[[spring/annex-一文带你深入理解-Spring-事务原理]]

原文插图 annex：[[patterns/annex-Spring-中经典的-9-种设计模式，打死也要记住啊！]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/Spring/Spring 事务管理探究.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/Spring/Spring 事务管理探究.note.md` · T22 **B** 档

### 来自：Spring 事务管理探究

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/Spring%20%E4%BA%8B%E5%8A%A1%E7%AE%A1%E7%90%86%E6%8E%A2%E7%A9%B6.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/Spring/事务/深入理解 Spring 事务原理.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/Spring/事务/深入理解 Spring 事务原理.note.md` · T22 **B** 档

### 来自：深入理解 Spring 事务原理

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/%E4%BA%8B%E5%8A%A1/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%20Spring%20%E4%BA%8B%E5%8A%A1%E5%8E%9F%E7%90%86.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/Spring/@Autowired注解实现原理（Spring Bean的自动装配）.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/Spring/@Autowired注解实现原理（Spring Bean的自动装配）.note.md` · T22 **B** 档

### 来自：@Autowired注解实现原理（Spring Bean的自动装配）

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/%40Autowired%E6%B3%A8%E8%A7%A3%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86%EF%BC%88Spring%20Bean%E7%9A%84%E8%87%AA%E5%8A%A8%E8%A3%85%E9%85%8D%EF%BC%89.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/%40Autowired%E6%B3%A8%E8%A7%A3%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86%EF%BC%88Spring%20Bean%E7%9A%84%E8%87%AA%E5%8A%A8%E8%A3%85%E9%85%8D%EF%BC%89.note_images/imageFile2.png)
