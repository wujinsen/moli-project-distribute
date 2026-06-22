# 知识库目录（index）



> 全库内容目录。每次 Ingest / Query 回写后更新。Query 时先读本文件定位相关页。



## guides（操作指导 · P0）



- [[本地启动指南]] — 从零本地启动茉莉微服务全家桶（Nacos/MySQL/Redis + 各服务，含启动顺序）

- [[登录与鉴权指南]] — 怎么登录拿 token、怎么带 `Authorization` 头跨服务调接口、常见返回码

- [[权限管理操作指南]] — 怎么管用户/角色/菜单/部门、给新员工开通权限的标准流程

- [[故障排查指南]] — 登录 500/Redis/Dubbo/Nacos/DB/JVM 等常见故障决策树（P0 运维）
- [[秒杀压测指南]] — loadtest 环境、k6 脚本阶梯、秒杀 API、checklist（P0 压测）
- [[知识库使用指南]] — 浏览/问答/图谱 API、空间权限、wiki 同步（P0）
- [[数据库初始化指南]] — scripts/moli.sql 一键导入、演示账号、秒杀/知识库表（P0）
- [[wiki同步指南]] — sync_to_db dry-run/写库、slug/kb_relation、与 Ingest 配合（P0）
- [[查询与体检指南]] — Query 作用域/Lint 检查、serve.py 与 /kb/ask（P0）
- [[minio-附件存储指南]] — MinIO 本地启动、knowledge 附件 API（P0）
- [[swagger接口调试指南]] — Springfox UI、Authorization 头、各服务端口（P0）
- [[docker部署指南]] — Docker 安装、常用命令、Java 镜像与 compose（P1）
- [[前端开发与联调指南]] — Node/Vue 本地启动、devServer 代理、token 联调（P0）
- [[nginx反向代理与前端部署指南]] — dist 托管、API 反代、HTTPS（P1）
- [[git协作指南]] — 分支/MR、冲突与常见 Git 问题（P1）
- [[增量ingest与raw投喂指南]] — raw 投喂、增量 ingest 批次、Lint/sync 闭环（P0）

## services（微服务实体）



- [[用户中心]] — 权限中枢：用户/角色/菜单/权限 + Shiro 认证 + Dubbo Provider（HTTP 8888）

- [[网关]] — Spring Cloud Gateway 统一入口，按路径转发（HTTP 21000，无鉴权/无 Sentinel）

- [[订单服务]] — 订单业务（通用为骨架）+ 秒杀子系统（HTTP 8087）

- [[bi服务]] — BI 骨架（1128）：Shiro+Dubbo 就绪，报表 API 待建；演进 [[bi报表服务演进路线]]

- [[知识库服务]] — 企业知识库 Java REST 后端，检索为 LIKE（HTTP 8090）



## concepts（跨文档概念）



- [[rbac-权限模型]] — 用户→角色→菜单/动作 的双轨 RBAC + Shiro 校验

- [[认证与会话机制]] — 无 JWT，token=Shiro Session ID，共享 Redis，跨服务校验

- [[服务调用与架构]] — 全链路、Dubbo vs HTTP 调用分层、基础设施、鉴权边界

- [[技术栈与版本]] — 框架/中间件版本矩阵与能力映射

- [[秒杀设计]] — Redis+Lua 原子扣减 + Redis队列异步落库

- [[分布式锁]] — 枢纽页：为什么需要 / 三种实现对比（DB·Redis·ZK）/ 选型

- [[mysql-索引]] — MySQL 索引枢纽：B+Tree / 聚簇·二级 / 建索引原则 / EXPLAIN 排查

- [[b-plus树与-innodb索引结构]] — B+Tree 选型、InnoDB 聚簇与二级索引、页分裂与自增主键

- [[java-并发]] — Java 并发枢纽：线程状态 / JMM / synchronized·volatile / 线程池 / 茉莉触点

- [[jmm与happens-before]] — 工作内存、重排序、happens-before 四条规则与内存屏障

- [[redis-缓存]] — Redis 枢纽：Session/秒杀/Cache-Aside / 击穿穿透雪崩 / 茉莉联调要点

- [[jvm-内存与gc]] — JVM 运行时区域、分代堆、默认 GC、常用参数

- [[spring-boot-自动配置]] — @SpringBootApplication、Starter、Conditional 自动装配

- [[spring-声明式事务]] — @Transactional、传播/失效、与 Boot TM 自动配置

- [[dubbo-与-nacos]] — 注册发现 + RPC 分工、茉莉 group/version、与 Gateway 边界
- [[mysql-事务与锁]] — ACID / 隔离级别 / 全局·表·行锁 / 与 Spring 事务分工
- [[spring-ioc与bean生命周期]] — IoC、Bean 生命周期、作用域、与 AOP/事务关系
- [[spring-cloud-gateway]] — Gateway vs MVC、Route/Predicate/Filter、茉莉 21000 现状
- [[shiro-鉴权体系]] — 双档 Realm、过滤器链、Redis 键、Starter 接入
- [[知识库三操作]] — Ingest / Query / Lint 契约、目录分工、wiki→DB 闭环
- [[sentinel-限流与熔断]] — 流控/熔断/降级、滑动窗口、茉莉未接入现状
- [[mybatis-与-druid持久层]] — MyBatis-Plus + Druid 池、Mapper 分层、排查触点
- [[elasticsearch-搜索]] — 倒排/分片/近实时、与 MySQL 分工、茉莉未部署
- [[消息队列]] — 解耦削峰、秒杀 Redis 队列 vs MQ 演进
- [[io模型与-netty]] — BIO/NIO/Reactor、Dubbo/Gateway 与 Netty 关系
- [[tomcat与-servlet容器]] — 内嵌 Tomcat、Filter 链、与 Gateway Netty 对比
- [[设计模式]] — GoF 三类、Spring 9 模式映射
- [[容器与-docker]] — 镜像/容器、与 VM 对比、茉莉用法
- [[前端技术栈]] — Vue/Element/axios、与后端契约
- [[linux-运维基础]] — 端口/进程/中间件启停、排障命令
- [[分布式理论基础]] — CAP/BASE、最终一致与茉莉取舍
- [[jenkins-ci入门]] — Pipeline、Maven 构建与部署
- [[k8s入门与茉莉关系]] — Pod/Deployment、压测 k6 Job
- [[mongodb与文档库选型]] — 与 MySQL/ES 对照（茉莉未用）

## articles（技术文章沉淀 · P1）



- [[redis分布式锁实现]] — Redis 锁演进史（SETNX→SET NX PX→Lua→看门狗→Redlock）与正确姿势、常见 bug

- [[mysql-复合索引与最左前缀]] — 联合索引设计、最左前缀、窄/宽索引与 EXPLAIN 验证

- [[mysql-覆盖索引与回表优化]] — 回表代价、Using index、ICP 与 SELECT * 规避

- [[mysql-索引失效场景]] — 运算/隐式转换/LIKE/OR/最左前缀等失效与排查习惯

- [[synchronized与锁原理]] — 三种用法、Monitor、锁升级、与 volatile 对比

- [[volatile与可见性]] — 可见性/有序性、DCL 单例、内存屏障

- [[concurrenthashmap原理]] — CHM 7 vs 8、无锁 get、put 与扩容

- [[cache-aside与缓存更新模式]] — 四种 Pattern、先写库后删缓存、race 与 TTL

- [[redis-数据结构与使用场景]] — String/Hash/ZSet 等与秒杀/Session/排行榜映射

- [[redis-持久化与高可用]] — RDB/AOF、主从哨兵集群

- [[jvm-垃圾收集算法与收集器]] — 标记清除/复制/整理、Parallel/G1/CMS

- [[jvm-oom与排查入门]] — OOM 类型、jmap/jstack、CPU 100%

- [[enableautoconfiguration原理]] — spring.factories、ImportSelector、@Conditional

- [[spring-application启动流程]] — SpringApplication.run、refresh、bootstrap 配置

- [[dubbo-调用原理与分层]] — RPC 十层、Consumer→Provider 链路、茉莉 Dubbo 配置

- [[nacos-注册与配置]] — Discovery/Config、bootstrap、namespace dev
- [[mysql-隔离级别与mvcc]] — 快照读/当前读、Read View、RR 与幻读
- [[mysql-innodb锁机制]] — 全局锁/MDL/Record·Gap·Next-Key
- [[mysql-死锁与排查]] — 典型案例、SHOW ENGINE INNODB STATUS、预防
- [[mysql-深分页与慢sql优化]] — 延迟关联、游标分页、EXPLAIN 流程
- [[spring-三级缓存与循环依赖]] — 三级缓存、构造器无解、AOP earlyReference
- [[spring-mvc请求流程]] — DispatcherServlet 链路、与 Gateway/Shiro 关系
- [[gateway-路由与过滤器]] — 茉莉四路由、StripPrefix、Sentinel 规划
- [[loadtest-profile与压测登录]] — loadtest 专用登录 vs 产品 /login、Profile 差异
- [[压测监控与prometheus]] — Grafana 看板、Druid/JVM 指标、压测解读
- [[字段级数据权限设计]] — 列级权限三方案（扩展设计，当前未实现）
- [[shiro-starter与跨服务校验]] — AutoConfiguration、AuthenticationFilter、接入 checklist
- [[sso与系统门户]] — sys_system、INTERNAL/EXTERNAL、Ticket 校验
- [[production-jvm启动参数]] — 历史 CMS 大堆参数、GC 日志、JDK11 G1 建议
- [[生产环境服务启停脚本]] — bash start/stop/restart 模板与运维清单
- [[限流算法与令牌桶]] — 计数器/漏桶/令牌桶、Guava、Redis Lua
- [[sentinel-接入与规则配置]] — Slot 链、Gateway/Dubbo 接入、规则类型
- [[mybatis-plus-用法与注入防护]] — #{} vs ${}、Plus CRUD、白名单
- [[druid连接池与监控]] — max-active/waiting、Prometheus 指标、慢 SQL 联动
- [[es-索引与写入流程]] — refresh/flush/translog、segment 不可变
- [[es-搜索与分片路由]] — Query Then Fetch、深分页、master 选举
- [[es-match与bool查询]] — match 等价 bool、boost、filter
- [[kafka-与-mq选型]] — Kafka/RocketMQ/RabbitMQ 对比、秒杀演进
- [[nacos-config动态配置实践]] — Config 启用/refresh、茉莉 dev 默认关闭
- [[bio-nio-aio对比]] — Buffer/Channel/Selector、epoll
- [[netty-reactor与线程模型]] — Boss/Worker、EventLoop、Dubbo/Gateway
- [[netty-pipeline与编解码]] — Pipeline、粘包、ByteBuf
- [[servlet生命周期与请求流程]] — Filter→DispatcherServlet、Shiro 拦截点
- [[bi报表服务演进路线]] — bi-server 四阶段落地、列级权限
- [[spring框架中的设计模式]] — BeanFactory/FactoryBean/AOP/HandlerAdapter 等 9 种
- [[https与-tls基础]] — TLS 握手、证书、网关终结 TLS
- [[跨域与前后端分离]] — CORS、Gateway globalCors、devServer proxy
- [[moli生产部署拓扑备忘]] — 生产拓扑与上线 checklist（无明文凭据）
- [[接口幂等性实践]] — Token/唯一键/乐观锁/秒杀幂等

## interview（面试题 · P2）



- [[spring-事务]] — Spring 事务面试题系列：ACID / 隔离级别 / 7 种传播行为 / @Transactional 失效场景（由 6 篇杂乱原文去重提炼）

- [[分布式锁面试题]] — 分布式锁面试题：SETNX 误删/续期/可重入/Redlock 争议/DB·ZK 实现要点

- [[mysql-索引面试题]] — MySQL 索引 10 题：B+Tree / 聚簇·回表 / 覆盖·最左前缀 / 失效 / EXPLAIN

- [[java-并发面试题]] — Java 并发 10 题：JMM / synchronized·volatile / CHM / 线程池 / ThreadLocal

- [[redis-面试题]] — Redis 10 题：快的原因、Cache-Aside、持久化、分布式锁、茉莉用途

- [[jvm-面试题]] — JVM 10 题：内存区域、GC 算法、默认收集器、OOM 排查

- [[spring-boot-面试题]] — Spring Boot 10 题：自动配置、Starter、bootstrap、排除 AutoConfig

- [[dubbo-面试题]] — Dubbo 10 题：分层、负载均衡、容错、Nacos、No provider 排查
- [[mysql-事务面试题]] — MySQL 事务/锁 10 题：ACID、MVCC、MDL、死锁、与 Spring 关系
- [[spring-容器面试题]] — Spring IoC/循环依赖/MVC/Gateway 10 题
- [[shiro-面试题]] — Shiro/SSO/Starter/门户 10 题
- [[sentinel-面试题]] — Sentinel/限流熔断/Dubbo/Gateway 10 题
- [[elasticsearch-面试题]] — ES 写入/搜索/选主/优化 10 题
- [[netty-与-io面试题]] — BIO/NIO/Reactor/粘包/EventLoop 12 题
- [[http与-servlet面试题]] — GET/POST、Servlet 生命周期、Tomcat vs Gateway 12 题
- [[设计模式面试题]] — 单例/工厂/代理/Spring 对照 10 题
- [[前端基础面试题]] — CORS/proxy/token/history 路由 10 题
- [[分布式理论面试题]] — CAP/BASE/幂等/秒杀一致 10 题

## outputs（问答回写综合页）

- [[茉莉登录与鉴权故障根因汇总]] — Query crystallize：Redis/密码/跨服务 401 决策树与对照表（P0）
- [[秒杀全链路与压测要点汇总]] — Query crystallize：秒杀链路图 + 压测 checklist + 监控解读（P0）
- [[茉莉微服务全链路一张图]] — Query crystallize：架构图 + 登录调订单 Happy Path（P0）
- [[茉莉新人上手checklist]] — Query crystallize：Day-1 环境/启动/登录/联调自检清单（P0）

_（更多页在 Query --crystallize 后追加）_

---

## 待 ingest 主题 backlog（主题扫光进度）

> 原则：按**主题簇**蒸馏，不 1:1 扫 1939 raw。✅=已有枢纽/指南，🔜=待做，⏭=低优先级/离题。

| 主题簇 | 状态 | 说明 |
|--------|------|------|
| P0 运维/微服务/核心栈 | ✅ | guides + services + 主要 concepts |
| 设计模式 / HTTPS / Docker | ✅ | 批次 #21 |
| Netty / Tomcat / Servlet | ✅ | 批次 #19–20 |
| ES / MQ / MinIO | ✅ | 批次 #17 |
| Sentinel / MyBatis-Druid | ✅ | 批次 #16 |
| **前端 Vue/React** | ✅ | 批次 #22：前端栈+联调+跨域 |
| **Linux 运维** | ✅ | 批次 #23：linux+Nginx+生产拓扑 |
| **MongoDB / PostgreSQL** | ✅ | 批次 #26：选型对照（茉莉用 MySQL） |
| **K8s / Jenkins / GitLab CI** | ✅ | 批次 #24–25：Git/Jenkins/K8s |
| **CAP/BASE/分布式理论** | ✅ | 批次 #24：理论+幂等+面试 |
| **数据结构与算法** | ⏭ | 面试向，与茉莉弱相关 |
| **BigData/Spark/Flink** | ⏭ | raw 800+，离题 |
| **机器学习/AI** | ⏭ | raw ~29 |

_茉莉相关主题簇 **已扫光**（按簇蒸馏，非 1939 raw 1:1）。后续增量：新 raw 投喂 → Ingest；Query crystallize → outputs。_


