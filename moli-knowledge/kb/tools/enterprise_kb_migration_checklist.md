# enterprise-kb 目录迁移 · 分组检查清单

> 来源：`enterprise_kb_migration_draft.csv`（定稿前请在 CSV 的 `review_note` 列标注后再迁移）  
> 空间：`enterprise-kb` · wiki 源目录 `kb/wiki/`  
> 原则：**只改首段目录**，`kb_type`（frontmatter `type:`）保持不变。

## 总览

| new_dir_slug | 分类名 | 合计 | concept | article | interview | 待复核* |
|--------------|--------|------|---------|---------|-----------|---------|
| `database` | 数据库 | 23 | 6 | 15 | 2 | 7 |
| `cache` | 缓存与 Redis | 14 | 3 | 9 | 2 | 4 |
| `java` | Java 与 JVM | 25 | 5 | 15 | 5 | 6 |
| `middleware` | 微服务与中间件 | 43 | 8 | 29 | 6 | 7 |
| `spring` | Spring 生态 | 16 | 6 | 7 | 3 | 5 |
| `search` | 搜索与 ES | 6 | 1 | 4 | 1 | 1 |
| `security` | 网络与安全 | 12 | 4 | 7 | 1 | 4 |
| `ops` | 运维与 Linux | 11 | 6 | 5 | 0 | 0 |
| `patterns` | 设计模式 | 3 | 1 | 1 | 1 | 0 |
| `frontend` | 前端 | 2 | 1 | 0 | 1 | 0 |

\* **待复核**：`assign_reason` 命中多条规则，迁移前建议人工确认目录。

## 迁移前通用检查（每类完成后打勾）

- [ ] Web 已建分类（`dir_slug` + 中文名）
- [ ] `git mv` 本类全部文件到新目录
- [ ] `lint.py --strict` 无新增断链
- [ ] `sync_to_db.py --wiki-dir wiki --space enterprise-kb`
- [ ] Web 验证：分类 chip + 体裁 chip + 列表 AND 筛选

---

## `database` · 数据库（23 篇）

**Web 分类**：`dir_slug=database` · concept 6 · article 15 · interview 2

### 概念枢纽（优先核对互链）

- [ ] `database/b-plus树与-innodb索引结构` — B+Tree 与 InnoDB 索引结构
- [ ] `database/mongodb与文档库选型` — MongoDB 与文档库选型
- [ ] `database/mybatis-与-druid持久层` — MyBatis 与 Druid 持久层
- [ ] `database/mysql-事务与锁` — MySQL 事务与锁
- [ ] `database/mysql-索引` — MySQL 索引
- [ ] `database/分布式id生成` — 分布式 ID 生成 ⚠️

### 文章（15）

- [ ] `articles/druid-连接池泄漏排查` → `database/druid-连接池泄漏排查` ⚠️
- [ ] `articles/druid连接池与监控` → `database/druid连接池与监控` ⚠️
- [ ] `articles/flyway-数据库版本迁移` → `database/flyway-数据库版本迁移` ⚠️
- [ ] `articles/mybatis-plus-用法与注入防护` → `database/mybatis-plus-用法与注入防护`
- [ ] `articles/mysql-binlog与canal同步` → `database/mysql-binlog与canal同步`
- [ ] `articles/mysql-innodb锁机制` → `database/mysql-innodb锁机制`
- [ ] `articles/mysql-slow-log慢查询分析` → `database/mysql-slow-log慢查询分析` ⚠️
- [ ] `articles/mysql-复合索引与最左前缀` → `database/mysql-复合索引与最左前缀`
- [ ] `articles/mysql-死锁与排查` → `database/mysql-死锁与排查` ⚠️
- [ ] `articles/mysql-深分页与慢sql优化` → `database/mysql-深分页与慢sql优化`
- [ ] `articles/mysql-索引失效场景` → `database/mysql-索引失效场景`
- [ ] `articles/mysql-覆盖索引与回表优化` → `database/mysql-覆盖索引与回表优化`
- [ ] `articles/mysql-隔离级别与mvcc` → `database/mysql-隔离级别与mvcc`
- [ ] `articles/testcontainers-集成测试` → `database/testcontainers-集成测试` ⚠️
- [ ] `articles/雪花算法与时钟回拨` → `database/雪花算法与时钟回拨`

### 面试题（2）

- [ ] `interview/mysql-事务面试题` → `database/mysql-事务面试题`
- [ ] `interview/mysql-索引面试题` → `database/mysql-索引面试题`

### 本类自检

- [ ] 同主题 concept / article / interview 是否都在本目录（如 mysql-* 三件套）
- [ ] 概念枢纽 `related` / 正文 `[[..]]` 无 `concepts/` `articles/` `interview/` 旧前缀
- [ ] 无遗漏进 `uncategorized`

---

## `cache` · 缓存与 Redis（14 篇）

**Web 分类**：`dir_slug=cache` · concept 3 · article 9 · interview 2

### 概念枢纽（优先核对互链）

- [ ] `cache/redis-缓存` — Redis 缓存 ⚠️
- [ ] `cache/分布式锁` — 分布式锁
- [ ] `cache/延迟消息与队列` — 延迟消息与队列

### 文章（9）

- [ ] `articles/cache-aside与缓存更新模式` → `cache/cache-aside与缓存更新模式`
- [ ] `articles/caffeine-本地缓存实践` → `cache/caffeine-本地缓存实践`
- [ ] `articles/redis-实现延迟队列` → `cache/redis-实现延迟队列`
- [ ] `articles/redis-持久化与高可用` → `cache/redis-持久化与高可用`
- [ ] `articles/redis-数据结构与使用场景` → `cache/redis-数据结构与使用场景`
- [ ] `articles/redis-集群与哨兵实践` → `cache/redis-集群与哨兵实践`
- [ ] `articles/redisson-看门狗与分布式锁` → `cache/redisson-看门狗与分布式锁` ⚠️
- [ ] `articles/redis分布式锁实现` → `cache/redis分布式锁实现` ⚠️
- [ ] `articles/缓存双写与一致性策略` → `cache/缓存双写与一致性策略`

### 面试题（2）

- [ ] `interview/redis-面试题` → `cache/redis-面试题`
- [ ] `interview/分布式锁面试题` → `cache/分布式锁面试题` ⚠️

### 本类自检

- [ ] 同主题 concept / article / interview 是否都在本目录（如 mysql-* 三件套）
- [ ] 概念枢纽 `related` / 正文 `[[..]]` 无 `concepts/` `articles/` `interview/` 旧前缀
- [ ] 无遗漏进 `uncategorized`

---

## `java` · Java 与 JVM（25 篇）

**Web 分类**：`dir_slug=java` · concept 5 · article 15 · interview 5

### 概念枢纽（优先核对互链）

- [ ] `java/java-并发` — Java 并发
- [ ] `java/java-集合框架` — Java 集合框架
- [ ] `java/jmm与happens-before` — JMM 与 happens-before
- [ ] `java/jvm-内存与gc` — JVM 内存与 GC
- [ ] `java/tomcat与-servlet容器` — Tomcat 与 Servlet 容器

### 文章（15）

- [ ] `articles/arthas-在线诊断` → `java/arthas-在线诊断` ⚠️
- [ ] `articles/bio-nio-aio对比` → `java/bio-nio-aio对比`
- [ ] `articles/completablefuture-异步编排` → `java/completablefuture-异步编排`
- [ ] `articles/concurrenthashmap原理` → `java/concurrenthashmap原理`
- [ ] `articles/java-cpu-100排查实战` → `java/java-cpu-100排查实战` ⚠️
- [ ] `articles/juc-并发工具类` → `java/juc-并发工具类`
- [ ] `articles/jvm-gc调优实战` → `java/jvm-gc调优实战` ⚠️
- [ ] `articles/jvm-oom与排查入门` → `java/jvm-oom与排查入门` ⚠️
- [ ] `articles/jvm-垃圾收集算法与收集器` → `java/jvm-垃圾收集算法与收集器`
- [ ] `articles/mdc-日志链路上下文` → `java/mdc-日志链路上下文` ⚠️
- [ ] `articles/production-jvm启动参数` → `java/production-jvm启动参数` ⚠️
- [ ] `articles/servlet生命周期与请求流程` → `java/servlet生命周期与请求流程`
- [ ] `articles/synchronized与锁原理` → `java/synchronized与锁原理`
- [ ] `articles/transmittable-thread-local跨线程` → `java/transmittable-thread-local跨线程`
- [ ] `articles/volatile与可见性` → `java/volatile与可见性`

### 面试题（5）

- [ ] `interview/hashmap-面试题` → `java/hashmap-面试题`
- [ ] `interview/http与-servlet面试题` → `java/http与-servlet面试题`
- [ ] `interview/java-并发面试题` → `java/java-并发面试题`
- [ ] `interview/jvm-面试题` → `java/jvm-面试题`
- [ ] `interview/异步编程面试题` → `java/异步编程面试题`

### 本类自检

- [ ] 同主题 concept / article / interview 是否都在本目录（如 mysql-* 三件套）
- [ ] 概念枢纽 `related` / 正文 `[[..]]` 无 `concepts/` `articles/` `interview/` 旧前缀
- [ ] 无遗漏进 `uncategorized`

---

## `middleware` · 微服务与中间件（43 篇）

**Web 分类**：`dir_slug=middleware` · concept 8 · article 29 · interview 6

### 概念枢纽（优先核对互链）

- [ ] `middleware/dubbo-与-nacos` — Dubbo 与 Nacos
- [ ] `middleware/io模型与-netty` — IO 模型与 Netty ⚠️
- [ ] `middleware/openfeign-与-http客户端` — OpenFeign 与 HTTP 客户端
- [ ] `middleware/sentinel-限流与熔断` — Sentinel 限流与熔断
- [ ] `middleware/zookeeper-与协调服务` — Zookeeper 与协调服务
- [ ] `middleware/分布式事务` — 分布式事务
- [ ] `middleware/分布式理论基础` — 分布式理论基础
- [ ] `middleware/消息队列` — 消息队列

### 文章（29）

- [ ] `articles/api-向后兼容策略` → `middleware/api-向后兼容策略`
- [ ] `articles/bi报表服务演进路线` → `middleware/bi报表服务演进路线`
- [ ] `articles/dubbo-调用原理与分层` → `middleware/dubbo-调用原理与分层`
- [ ] `articles/dubbo-超时链路传递` → `middleware/dubbo-超时链路传递`
- [ ] `articles/feign-开发踩坑` → `middleware/feign-开发踩坑`
- [ ] `articles/feign-超时重试配置` → `middleware/feign-超时重试配置`
- [ ] `articles/gateway-超时与重试配置` → `middleware/gateway-超时与重试配置`
- [ ] `articles/gateway-路由与过滤器` → `middleware/gateway-路由与过滤器`
- [ ] `articles/kafka-与-mq选型` → `middleware/kafka-与-mq选型`
- [ ] `articles/loadtest-profile与压测登录` → `middleware/loadtest-profile与压测登录` ⚠️
- [ ] `articles/nacos-config动态配置实践` → `middleware/nacos-config动态配置实践`
- [ ] `articles/nacos-注册与配置` → `middleware/nacos-注册与配置`
- [ ] `articles/netty-pipeline与编解码` → `middleware/netty-pipeline与编解码`
- [ ] `articles/netty-reactor与线程模型` → `middleware/netty-reactor与线程模型` ⚠️
- [ ] `articles/nginx-限流与缓冲调优` → `middleware/nginx-限流与缓冲调优` ⚠️
- [ ] `articles/okhttp-与-http客户端` → `middleware/okhttp-与-http客户端`
- [ ] `articles/pact-契约测试入门` → `middleware/pact-契约测试入门`
- [ ] `articles/rabbitmq-入门与使用场景` → `middleware/rabbitmq-入门与使用场景`
- [ ] `articles/rocketmq-事务消息实践` → `middleware/rocketmq-事务消息实践`
- [ ] `articles/rocketmq-架构与实战` → `middleware/rocketmq-架构与实战`
- [ ] `articles/rocketmq-消息堆积排查` → `middleware/rocketmq-消息堆积排查` ⚠️
- [ ] `articles/rpc-超时重试与链路` → `middleware/rpc-超时重试与链路`
- [ ] `articles/sentinel-接入与规则配置` → `middleware/sentinel-接入与规则配置`
- [ ] `articles/sse-服务端推送` → `middleware/sse-服务端推送` ⚠️
- [ ] `articles/webclient-与-resttemplate` → `middleware/webclient-与-resttemplate`
- [ ] `articles/压测监控与prometheus` → `middleware/压测监控与prometheus` ⚠️
- [ ] `articles/接口幂等性实践` → `middleware/接口幂等性实践`
- [ ] `articles/跨域与前后端分离` → `middleware/跨域与前后端分离`
- [ ] `articles/限流算法与令牌桶` → `middleware/限流算法与令牌桶`

### 面试题（6）

- [ ] `interview/dubbo-面试题` → `middleware/dubbo-面试题`
- [ ] `interview/netty-与-io面试题` → `middleware/netty-与-io面试题`
- [ ] `interview/sentinel-面试题` → `middleware/sentinel-面试题`
- [ ] `interview/zookeeper-面试题` → `middleware/zookeeper-面试题`
- [ ] `interview/分布式id面试题` → `middleware/分布式id面试题`
- [ ] `interview/分布式理论面试题` → `middleware/分布式理论面试题`

### 本类自检

- [ ] 同主题 concept / article / interview 是否都在本目录（如 mysql-* 三件套）
- [ ] 概念枢纽 `related` / 正文 `[[..]]` 无 `concepts/` `articles/` `interview/` 旧前缀
- [ ] 无遗漏进 `uncategorized`

---

## `spring` · Spring 生态（16 篇）

**Web 分类**：`dir_slug=spring` · concept 6 · article 7 · interview 3

### 概念枢纽（优先核对互链）

- [ ] `spring/spring-aop与代理` — Spring AOP 与代理
- [ ] `spring/spring-async与线程池` — Spring @Async 与线程池 ⚠️
- [ ] `spring/spring-boot-自动配置` — Spring Boot 自动配置
- [ ] `spring/spring-cloud-gateway` — Spring Cloud Gateway ⚠️
- [ ] `spring/spring-ioc与bean生命周期` — Spring IoC 与 Bean 生命周期
- [ ] `spring/spring-声明式事务` — Spring 声明式事务

### 文章（7）

- [ ] `articles/enableautoconfiguration原理` → `spring/enableautoconfiguration原理`
- [ ] `articles/spring-aop执行流程` → `spring/spring-aop执行流程`
- [ ] `articles/spring-application启动流程` → `spring/spring-application启动流程`
- [ ] `articles/spring-boot-启动优化` → `spring/spring-boot-启动优化` ⚠️
- [ ] `articles/spring-cache-注解缓存` → `spring/spring-cache-注解缓存` ⚠️
- [ ] `articles/spring-mvc请求流程` → `spring/spring-mvc请求流程`
- [ ] `articles/spring-三级缓存与循环依赖` → `spring/spring-三级缓存与循环依赖` ⚠️

### 面试题（3）

- [ ] `interview/spring-boot-面试题` → `spring/spring-boot-面试题`
- [ ] `interview/spring-事务` → `spring/spring-事务`
- [ ] `interview/spring-容器面试题` → `spring/spring-容器面试题`

### 本类自检

- [ ] 同主题 concept / article / interview 是否都在本目录（如 mysql-* 三件套）
- [ ] 概念枢纽 `related` / 正文 `[[..]]` 无 `concepts/` `articles/` `interview/` 旧前缀
- [ ] 无遗漏进 `uncategorized`

---

## `search` · 搜索与 ES（6 篇）

**Web 分类**：`dir_slug=search` · concept 1 · article 4 · interview 1

### 概念枢纽（优先核对互链）

- [ ] `search/elasticsearch-搜索` — Elasticsearch 搜索

### 文章（4）

- [ ] `articles/elasticsearch-ik分词与分析器` → `search/elasticsearch-ik分词与分析器`
- [ ] `articles/es-match与bool查询` → `search/es-match与bool查询`
- [ ] `articles/es-搜索与分片路由` → `search/es-搜索与分片路由`
- [ ] `articles/es-索引与写入流程` → `search/es-索引与写入流程` ⚠️

### 面试题（1）

- [ ] `interview/elasticsearch-面试题` → `search/elasticsearch-面试题`

### 本类自检

- [ ] 同主题 concept / article / interview 是否都在本目录（如 mysql-* 三件套）
- [ ] 概念枢纽 `related` / 正文 `[[..]]` 无 `concepts/` `articles/` `interview/` 旧前缀
- [ ] 无遗漏进 `uncategorized`

---

## `security` · 网络与安全（12 篇）

**Web 分类**：`dir_slug=security` · concept 4 · article 7 · interview 1

### 概念枢纽（优先核对互链）

- [ ] `security/rbac-权限模型` — RBAC 权限模型
- [ ] `security/shiro-鉴权体系` — Shiro 鉴权体系 ⚠️
- [ ] `security/threadlocal-与上下文传递` — ThreadLocal 与上下文传递 ⚠️
- [ ] `security/认证与会话机制` — 认证与会话机制

### 文章（7）

- [ ] `articles/api-接口安全设计` → `security/api-接口安全设计`
- [ ] `articles/bcrypt-密码哈希与加盐` → `security/bcrypt-密码哈希与加盐`
- [ ] `articles/csrf与xss防护` → `security/csrf与xss防护` ⚠️
- [ ] `articles/https与-tls基础` → `security/https与-tls基础`
- [ ] `articles/shiro-starter与跨服务校验` → `security/shiro-starter与跨服务校验` ⚠️
- [ ] `articles/sso与系统门户` → `security/sso与系统门户`
- [ ] `articles/字段级数据权限设计` → `security/字段级数据权限设计`

### 面试题（1）

- [ ] `interview/shiro-面试题` → `security/shiro-面试题`

### 本类自检

- [ ] 同主题 concept / article / interview 是否都在本目录（如 mysql-* 三件套）
- [ ] 概念枢纽 `related` / 正文 `[[..]]` 无 `concepts/` `articles/` `interview/` 旧前缀
- [ ] 无遗漏进 `uncategorized`

---

## `ops` · 运维与 Linux（11 篇）

**Web 分类**：`dir_slug=ops` · concept 6 · article 5 · interview 0

### 概念枢纽（优先核对互链）

- [ ] `ops/jenkins-ci入门` — Jenkins CI 入门
- [ ] `ops/k8s入门与容器编排` — K8s 入门与容器编排
- [ ] `ops/linux-运维基础` — Linux 运维基础
- [ ] `ops/容器与-docker` — 容器与 Docker
- [ ] `ops/容量规划与水平扩展` — 容量规划与水平扩展
- [ ] `ops/蓝绿与滚动发布` — 蓝绿与滚动发布

### 文章（5）

- [ ] `articles/k8s-健康检查探针` → `ops/k8s-健康检查探针`
- [ ] `articles/linux-ulimit与文件句柄` → `ops/linux-ulimit与文件句柄`
- [ ] `articles/prometheus-告警规则设计` → `ops/prometheus-告警规则设计`
- [ ] `articles/生产环境服务启停脚本` → `ops/生产环境服务启停脚本`
- [ ] `articles/网络-端口与连通性排查` → `ops/网络-端口与连通性排查`

### 本类自检

- [ ] 同主题 concept / article / interview 是否都在本目录（如 mysql-* 三件套）
- [ ] 概念枢纽 `related` / 正文 `[[..]]` 无 `concepts/` `articles/` `interview/` 旧前缀
- [ ] 无遗漏进 `uncategorized`

---

## `patterns` · 设计模式（3 篇）

**Web 分类**：`dir_slug=patterns` · concept 1 · article 1 · interview 1

### 概念枢纽（优先核对互链）

- [ ] `patterns/设计模式` — 设计模式

### 文章（1）

- [ ] `articles/spring框架中的设计模式` → `patterns/spring框架中的设计模式`

### 面试题（1）

- [ ] `interview/设计模式面试题` → `patterns/设计模式面试题`

### 本类自检

- [ ] 同主题 concept / article / interview 是否都在本目录（如 mysql-* 三件套）
- [ ] 概念枢纽 `related` / 正文 `[[..]]` 无 `concepts/` `articles/` `interview/` 旧前缀
- [ ] 无遗漏进 `uncategorized`

---

## `frontend` · 前端（2 篇）

**Web 分类**：`dir_slug=frontend` · concept 1 · article 0 · interview 1

### 概念枢纽（优先核对互链）

- [ ] `frontend/前端技术栈` — 前端技术栈

### 面试题（1）

- [ ] `interview/前端基础面试题` → `frontend/前端基础面试题`

### 本类自检

- [ ] 同主题 concept / article / interview 是否都在本目录（如 mysql-* 三件套）
- [ ] 概念枢纽 `related` / 正文 `[[..]]` 无 `concepts/` `articles/` `interview/` 旧前缀
- [ ] 无遗漏进 `uncategorized`

---

## 全库边界篇复核清单（⚠️ 多规则命中）

以下篇目在 CSV 中 `assign_reason` 含多条规则，定稿时请确认 `new_dir_slug`：

- [ ] `cache/redis-缓存` — Redis 缓存 （rule:cache,security）
- [ ] `cache/redisson-看门狗与分布式锁` — Redisson 看门狗与分布式锁 （rule:cache,security）
- [ ] `cache/redis分布式锁实现` — Redis 分布式锁实现（演进与正确姿势） （rule:cache,security）
- [ ] `cache/分布式锁面试题` — 分布式锁（面试题系列） （rule:cache,middleware,java）
- [ ] `database/druid-连接池泄漏排查` — Druid 连接池泄漏排查 （rule:database,ops）
- [ ] `database/druid连接池与监控` — Druid 连接池与监控 （rule:database,ops）
- [ ] `database/flyway-数据库版本迁移` — Flyway 数据库版本迁移 （rule:database,ops）
- [ ] `database/mysql-slow-log慢查询分析` — MySQL Slow Log 慢查询分析 （rule:database,ops）
- [ ] `database/mysql-死锁与排查` — MySQL 死锁与排查 （rule:database,ops）
- [ ] `database/testcontainers-集成测试` — Testcontainers 集成测试 （rule:database,ops）
- [ ] `database/分布式id生成` — 分布式 ID 生成 （rule:database,middleware）
- [ ] `java/arthas-在线诊断` — Arthas 在线诊断入门 （rule:ops,java）
- [ ] `java/java-cpu-100排查实战` — Java CPU 100% 排查实战 （rule:ops,java）
- [ ] `java/jvm-gc调优实战` — JVM GC 调优实战 （rule:ops,java）
- [ ] `java/jvm-oom与排查入门` — JVM OOM 与排查入门 （rule:ops,java）
- [ ] `java/mdc-日志链路上下文` — MDC 日志链路上下文 （rule:ops,java）
- [ ] `java/production-jvm启动参数` — 生产环境 JVM 启动参数 （rule:ops,java）
- [ ] `middleware/io模型与-netty` — IO 模型与 Netty （rule:middleware,java）
- [ ] `middleware/loadtest-profile与压测登录` — loadtest Profile 与压测登录 （rule:middleware,security）
- [ ] `middleware/netty-reactor与线程模型` — Netty Reactor 与线程模型 （rule:middleware,frontend）
- [ ] `middleware/nginx-限流与缓冲调优` — Nginx 限流与缓冲调优 （rule:middleware,ops）
- [ ] `middleware/rocketmq-消息堆积排查` — RocketMQ 消息堆积排查 （rule:middleware,ops）
- [ ] `middleware/sse-服务端推送` — SSE 服务端推送 （rule:middleware,frontend）
- [ ] `middleware/压测监控与prometheus` — 压测监控与 Prometheus （rule:middleware,ops）
- [ ] `search/es-索引与写入流程` — ES 索引与写入流程 （rule:search,database）
- [ ] `security/csrf与xss防护` — CSRF 与 XSS 防护 （rule:security,frontend）
- [ ] `security/shiro-starter与跨服务校验` — Shiro Starter 与跨服务校验 （rule:spring,middleware,security）
- [ ] `security/shiro-鉴权体系` — Shiro 鉴权体系 （rule:middleware,security）
- [ ] `security/threadlocal-与上下文传递` — ThreadLocal 与上下文传递 （rule:security,java）
- [ ] `spring/spring-async与线程池` — Spring @Async 与线程池 （rule:spring,java）
- [ ] `spring/spring-boot-启动优化` — Spring Boot 启动优化 （rule:spring,ops,java）
- [ ] `spring/spring-cache-注解缓存` — Spring Cache 注解缓存 （rule:cache,spring）
- [ ] `spring/spring-cloud-gateway` — Spring Cloud Gateway （rule:spring,middleware）
- [ ] `spring/spring-三级缓存与循环依赖` — Spring 三级缓存与循环依赖 （rule:cache,spring）

## 旧目录退役

- [ ] 确认 `concepts/`、`articles/`、`interview/` 已空
- [ ] SQL 物理删旧 `kb_category`（concepts/articles/interview 三条）
- [ ] 再 sync 一次确认文档 category_id 已指向新主题分类
