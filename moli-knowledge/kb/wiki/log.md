# 知识库时间线（log）

> append-only。每次 ingest / query / lint 追加一行。
> 语法：`## [YYYY-MM-DD] {操作} | {简述}`
> 速查最近动作：`grep "^## \[" log.md | tail -5`

## [2026-06-22] init | 初始化知识库骨架（AGENTS.md 契约 + wiki 目录 + index/log）
## [2026-06-22] ingest | README.zh-CN.md → guides/本地启动指南, services/{用户中心,网关,订单服务,bi服务}, concepts/rbac-权限模型
## [2026-06-22] ingest | M1 批量: README + docs/zh-CN/{ARCHITECTURE,TECH_STACK,RBAC} + api-iteration-map + 源码梳理 → 校准5页 + 新增 concepts/{认证与会话机制,服务调用与架构,技术栈与版本,秒杀设计} + services/知识库服务 + guides/{登录与鉴权指南,权限管理操作指南}; 标注端口1127→8888等过时点
## [2026-06-22] ingest | raw/wujinsen_markdown Spring事务簇(6篇,含2组重复) 去重提炼 → interview/spring-事务（控量示范，1962篇语料中首个）
## [2026-06-22] ingest | raw 分布式锁簇(5篇,#2与#9近重复) 跨类型提炼 → concepts/分布式锁(枢纽)+interview/分布式锁面试题+articles/redis分布式锁实现; 互链并联 秒杀设计
## [2026-06-22] ingest | raw DataBase/mysql/索引簇(7篇)+B+Tree原理+面试题整理(3篇) 去重提炼 → concepts/{mysql-索引(枢纽),b-plus树与-innodb索引结构}+articles/{复合索引与最左前缀,覆盖索引与回表优化,索引失效场景}+interview/mysql-索引面试题; 6页互链+edges; ~9.5k chars wiki 产出
## [2026-06-22] ingest | raw Java并发簇(JMM读书笔记+面试精选3篇+并发小结) → concepts/{java-并发(枢纽),jmm与happens-before}+articles/{synchronized与锁原理,volatile与可见性,concurrenthashmap原理}+interview/java-并发面试题; 6页; P0跑厚批次#1(核心技术栈)
