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
## [2026-06-22] ingest | 批次#6 Redis缓存簇(缓存更新套路+高级java) → concepts/redis-缓存+articles/{cache-aside与缓存更新模式,redis-数据结构与使用场景,redis-持久化与高可用}+interview/redis-面试题; 5页
## [2026-06-22] ingest | 批次#7 JVM簇(内存划分+GC算法+收集器对比+精尽面试题) → concepts/jvm-内存与gc+articles/{jvm-垃圾收集算法与收集器,jvm-oom与排查入门}+interview/jvm-面试题; 4页
## [2026-06-22] ingest | 批次#8 SpringBoot簇(自动装配源码笔记+SpringApplication) → concepts/{spring-boot-自动配置,spring-声明式事务}+articles/{enableautoconfiguration原理,spring-application启动流程}+interview/spring-boot-面试题; 5页
## [2026-06-22] ingest | 批次#9 Dubbo+Nacos簇(精尽Dubbo面试题+moli项目Nacos笔记) → concepts/dubbo-与-nacos+articles/{dubbo-调用原理与分层,nacos-注册与配置}+interview/dubbo-面试题; 4页
## [2026-06-22] ingest | 批次#10 P0故障排查(本地踩坑+运维笔记+JVM排查) → guides/故障排查指南; 并联本地启动/Redis/Nacos/Dubbo; 1页
## [2026-06-22] ingest | 批次#11 MySQL事务锁簇(隔离/MVCC+表锁MDL+死锁6案例+深分页优化) → concepts/mysql-事务与锁+articles/{隔离级别与mvcc,innodb锁机制,死锁与排查,深分页与慢sql优化}+interview/mysql-事务面试题; 6页; 并联mysql-索引与spring-声明式事务
## [2026-06-22] ingest | 批次#12 Spring容器+Gateway簇(循环依赖2篇+SpringMVC+gateway yml) → concepts/{spring-ioc与bean生命周期,spring-cloud-gateway}+articles/{三级缓存与循环依赖,spring-mvc请求流程,gateway-路由与过滤器}+interview/spring-容器面试题; enrich services/网关; 6页
## [2026-06-22] ingest | 批次#13 秒杀压测+运维(load-test README+LoadtestLogin+moli字段权限) → guides/秒杀压测指南+articles/{loadtest-profile与压测登录,压测监控与prometheus,字段级数据权限设计}; enrich concepts/秒杀设计; 4页
## [2026-06-22] ingest | 批次#14 Shiro+SSO+知识库API(shiro-starter源码+KNOWLEDGE_API+sys_system) → concepts/shiro-鉴权体系+articles/{shiro-starter与跨服务校验,sso与系统门户}+guides/知识库使用指南+interview/shiro-面试题; enrich 认证/知识库服务; 5页
## [2026-06-22] ingest | 批次#15 P0运维闭环(sync_to_db+serve Query/Lint+scripts/init-db+运维JVM/启停脚本) → guides/{wiki同步指南,查询与体检指南,数据库初始化指南}+concepts/知识库三操作+articles/{production-jvm启动参数,生产环境服务启停脚本}; 6页; 闭环 M1 三操作与 DB 初始化
## [2026-06-22] ingest | 批次#16 Sentinel+MyBatis/Druid簇(限流特技+滑动窗口+mybatis#{}+DruidPoolMetrics) → concepts/{sentinel-限流与熔断,mybatis-与-druid持久层}+articles/{限流算法与令牌桶,sentinel-接入与规则配置,mybatis-plus-用法与注入防护,druid连接池与监控}+interview/sentinel-面试题; 7页; 并联Gateway/秒杀/压测监控
## [2026-06-22] ingest | 批次#17 ES+MQ+MinIO簇(ES面试小结+match/bool+Kafka选型+minio安装+knowledge附件) → concepts/{elasticsearch-搜索,消息队列}+articles/{es-索引与写入流程,es-搜索与分片路由,es-match与bool查询,kafka-与-mq选型}+interview/elasticsearch-面试题+guides/minio-附件存储指南; 8页; 并联知识库/秒杀/技术栈
## [2026-06-22] query | crystallize + ingest 批次#18 → outputs/茉莉登录与鉴权故障根因汇总(首篇crystallize)+articles/nacos-config动态配置实践+guides/swagger接口调试指南; 3页; 并联故障排查/登录/Nacos
## [2026-06-22] query | crystallize + ingest 批次#19 → concepts/io模型与-netty+articles/{bio-nio-aio对比,netty-reactor与线程模型,netty-pipeline与编解码}+interview/netty-与-io面试题+outputs/秒杀全链路与压测要点汇总; 6页; 并联Dubbo/Gateway/秒杀压测
## [2026-06-22] lint | 全库99页: 断链11(皆meta示例误报), 孤儿2已修, 缺sources0
## [2026-06-22] ingest | 批次#20 Lint+BI+Tomcat簇 → enrich services/bi服务+articles/{bi报表服务演进路线,servlet生命周期与请求流程}+concepts/tomcat与-servlet容器+interview/http与-servlet面试题; 5页; enrich本地启动/shiro/查询与体检
## [2026-06-22] query | crystallize + ingest 批次#21 → concepts/{设计模式,容器与-docker}+articles/{spring框架中的设计模式,https与-tls基础}+interview/设计模式面试题+guides/docker部署指南+outputs/茉莉微服务全链路一张图; 7页; index追加主题backlog
## [2026-06-22] ingest | 批次#22 前端簇 → concepts/前端技术栈+guides/前端开发与联调指南+articles/跨域与前后端分离+interview/前端基础面试题; 4页; enrich本地启动/登录/gateway
## [2026-06-22] ingest | 批次#23 Linux/Nginx簇 → concepts/linux-运维基础+guides/nginx反向代理与前端部署指南+articles/moli生产部署拓扑备忘; 3页
## [2026-06-22] ingest | 批次#24 分布式理论簇 → concepts/分布式理论基础+articles/接口幂等性实践+interview/分布式理论面试题; 3页; enrich秒杀设计
## [2026-06-22] ingest | 批次#25 DevOps簇 → guides/git协作指南+concepts/{jenkins-ci入门,k8s入门与茉莉关系}; 3页
## [2026-06-22] ingest | 批次#26 MongoDB选型 → concepts/mongodb与文档库选型; 1页; index backlog 茉莉主题扫光
## [2026-06-22] query | crystallize 茉莉新人上手checklist + ingest guides/增量ingest与raw投喂指南; 2页; sync_to_db 标签大小写修复
## [2026-06-22] sync | wiki→MySQL enterprise-kb 全量同步（124+ 页）
