# 知识库目录（index）



> 全库内容目录。每次 Ingest / Query 回写后更新。Query 时先读本文件定位相关页。  
> **系统操作手册**已独立为知识空间 `moli-ops-manual`（wiki 源 `kb/wiki-ops/`），本目录仍保留副本便于跨库检索；运维向入口见该空间或 [[guides/知识库使用指南]]。



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
- [[AI自我进化与MD审校流程]] — LLM-Wiki 自我进化闭环、Ingest/Lint/Sync/Crystallize、AI 审校 MD → lint → Sync（P0）
- [[Wiki在线编辑与AI协助改稿]] — Web 界面编辑 wiki、AI 协助改稿、diff 对比、保存回 wiki（draft · T14/M5）

## services（微服务实体）



- [[用户中心]] — 权限中枢：用户/角色/菜单/权限 + Shiro 认证 + Dubbo Provider（HTTP 8888）

- [[网关]] — Spring Cloud Gateway 统一入口，按路径转发（HTTP 21000，无鉴权/无 Sentinel）

- [[订单服务]] — 订单业务（通用为骨架）+ 秒杀子系统（HTTP 8087）

- [[bi服务]] — BI 骨架（1128）：Shiro+Dubbo 就绪，报表 API 待建；演进 [[bi报表服务演进路线]]

- [[知识库服务]] — 企业知识库 Java REST 后端，检索走 MySQL ngram 全文（HTTP 8090）



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
- [[zookeeper-与协调服务]] — ZAB、临时节点、与 Nacos/Dubbo 对比
- [[分布式事务]] — 2PC/TCC/MQ 事务消息/Saga、茉莉最终一致取向
- [[java-集合框架]] — List/Map 体系、HashMap 1.8 结构
- [[openfeign-与-http客户端]] — 声明式 HTTP vs Dubbo、Spring Cloud 场景

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
- [[shiro-starter与跨服务校验]] — AutoConfiguration、AuthenticationFilter、接入 checklist、启动期循环依赖(securityManager)根因与解法
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
- [[redis-集群与哨兵实践]] — 主从/Sentinel/Cluster、茉莉 Session 注意
- [[rocketmq-架构与实战]] — NameServer/Broker、No route 排查
- [[rocketmq-事务消息实践]] — 半消息、回查、与 Seata 对比
- [[rabbitmq-入门与使用场景]] — Exchange/Queue、与 Kafka/Rocket 选型
- [[feign-开发踩坑]] — 上传/重试/GET body
- [[api-接口安全设计]] — 鉴权/限流/签名/HTTPS
- [[java-cpu-100排查实战]] — top+jstack、GC/慢 SQL 联动

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
- [[zookeeper-面试题]] — ZAB/Watcher/选举/与 Nacos 10 题
- [[hashmap-面试题]] — 结构/扩容/ConcurrentHashMap 12 题

## outputs（问答回写综合页）

- [[茉莉登录与鉴权故障根因汇总]] — Query crystallize：Redis/密码/跨服务 401 决策树与对照表（P0）
- [[秒杀全链路与压测要点汇总]] — Query crystallize：秒杀链路图 + 压测 checklist + 监控解读（P0）
- [[茉莉微服务全链路一张图]] — Query crystallize：架构图 + 登录调订单 Happy Path（P0）
- [[茉莉新人上手checklist]] — Query crystallize：Day-1 环境/启动/登录/联调自检清单（P0）
- [[茉莉中间件与依赖选型速查]] — Query crystallize：Nacos/MySQL/Redis/Dubbo/MQ 职责与故障症状（P0）

_（更多页在 Query --crystallize 后追加）_

---

## 待 ingest 主题 backlog（主题扫光进度）

> 原则：按**主题簇**蒸馏，不 1:1 扫 1939 raw。✅=已有枢纽/指南，🔜=待做，⏭=低优先级/离题。

| 主题簇 | 状态 | 说明 |
|--------|------|------|
| P0 运维/微服务/核心栈 | ✅ | guides + services + 主要 concepts |
| Redis 集群 / ZK / MQ 深度 | ✅ | 批次 #27–#31 |
| 分布式事务 / Feign / API 安全 | ✅ | 批次 #29–#34 |
| Java 集合 / CPU 排查 | ✅ | 批次 #32、#35 |
| 中间件选型 crystallize | ✅ | 批次 #36 outputs |
| **数据结构与算法** | ⏭ | 面试向，与茉莉弱相关 |
| **BigData/Spark/Flink** | ⏭ | raw 800+，离题 |
| **机器学习/AI** | ⏭ | raw ~29 |


## 批次 #37–#86 新增（50 批）

- [[kafka-消费组与再均衡]] — Kafka 消费组与再均衡（#37）
- [[elasticsearch-聚合与分析]] — Elasticsearch 聚合与分析（#38）
- [[布隆过滤器与缓存穿透]] — 布隆过滤器与缓存穿透（#39）
- [[一致性哈希与负载均衡]] — 一致性哈希与负载均衡（#40）
- [[ribbon-客户端负载均衡]] — Ribbon 客户端负载均衡（#41）
- [[hystrix-熔断降级]] — Hystrix 熔断降级（#42）
- [[seata-分布式事务入门]] — Seata 分布式事务入门（#43）
- [[分库分表入门]] — 分库分表入门（#44）
- [[mysql-主从读写分离]] — MySQL 主从读写分离（#45）
- [[postgresql-选型对比]] — PostgreSQL 选型对比（#46）
- [[oauth2-与开放接口]] — OAuth2 与开放接口（#47）
- [[jwt-与-session-对比]] — JWT 与 Session 对比（#48）
- [[websocket-实时通信]] — WebSocket 实时通信（#49）
- [[xxl-job-分布式定时任务]] — XXL-Job 分布式定时任务（#50）
- [[spring-scheduled-定时任务]] — Spring Scheduled 定时任务（#51）
- [[线程池-实战调优]] — 线程池实战调优（#52）
- [[logback-日志配置]] — Logback 日志配置（#53）
- [[skywalking-链路追踪]] — SkyWalking 链路追踪（#54）
- [[elk-日志检索]] — ELK 日志检索（#55）
- [[restful-api-设计规范]] — RESTful API 设计规范（#56）
- [[api-版本与兼容]] — API 版本与兼容（#57）
- [[jackson-序列化配置]] — Jackson 序列化配置（#58）
- [[文件上传与大对象存储]] — 文件上传与大对象存储（#59）
- [[junit5-单元测试]] — JUnit 5 单元测试（#60）
- [[mockito-测试实战]] — Mockito 测试实战（#61）
- [[mybatis-读写分离路由]] — MyBatis 读写分离路由（#62）
- [[多级缓存架构]] — 多级缓存架构（#63）
- [[redis-热key与大key治理]] — Redis 热 Key 与大 Key 治理（#64）
- [[分布式限流实现]] — 分布式限流实现（#65）
- [[网关灰度与金丝雀发布]] — 网关灰度与金丝雀发布（#66）
- [[dubbo-负载均衡与集群容错]] — Dubbo 负载均衡与集群容错（#67）
- [[nacos-集群与高可用]] — Nacos 集群与高可用（#68）
- [[elasticsearch-写入调优]] — Elasticsearch 写入调优（#69）
- [[mysql-explain-执行计划进阶]] — MySQL EXPLAIN 执行计划进阶（#70）
- [[订单-状态机设计]] — 订单状态机设计（#71）
- [[支付回调与安全验签]] — 支付回调与安全验签（#72）
- [[购物车-设计与缓存]] — 购物车设计与缓存（#73）
- [[库存-超卖防护]] — 库存超卖防护（#74）
- [[优惠券-高并发领取]] — 优惠券高并发领取（#75）
- [[用户中心-扩展能力规划]] — 用户中心扩展能力规划（#76）
- [[bi-元数据与指标治理]] — BI 元数据与指标治理（#77）
- [[知识库-全文检索规划]] — 知识库全文检索规划（#78）
- [[gateway-接入-sentinel规划]] — Gateway 接入 Sentinel 规划（#79）
- [[微服务-优雅停机]] — 微服务优雅停机（#80）
- [[配置-敏感信息与加密]] — 配置敏感信息与加密（#81）
- [[docker-compose-茉莉依赖栈]] — Docker Compose 茉莉依赖栈（#82）
- [[maven-多模块依赖管理]] — Maven 多模块依赖管理（#83）
- [[idea-远程调试与断点]] — IDEA 远程调试与断点（#84）
- [[git-分支与发布策略]] — Git 分支与发布策略（#85）
- [[茉莉高并发架构模式汇总]] — 茉莉高并发架构模式汇总（#86）

_下一批从 **#87** 起。_

## 批次 #87–#96 新增（10 批）

- [[分布式id生成]] · [[分布式id面试题]] — #87
- [[雪花算法与时钟回拨]] — #88
- [[redisson-看门狗与分布式锁]] — #89
- [[延迟消息与队列]] · [[redis-实现延迟队列]] — #90–#91
- [[mysql-binlog与canal同步]] — #92
- [[spring-aop与代理]] · [[spring-aop执行流程]] — #93–#94
- [[rpc-超时重试与链路]] — #94
- [[缓存双写与一致性策略]] · [[压测报告解读指南]] — #95
- [[茉莉数据层设计要点汇总]] — #96


## 批次 #97–#146 新增（50 批）

- [[spring-事件机制]] — Spring 事件机制（#97）
- [[spring-boot-actuator监控]] — Spring Boot Actuator 监控（#98）
- [[micrometer-与指标暴露]] — Micrometer 与指标暴露（#99）
- [[java-类加载与双亲委派]] — Java 类加载与双亲委派（#100）
- [[java-spi机制]] — Java SPI 机制（#101）
- [[java-字符串与常量池]] — Java 字符串与常量池（#102）
- [[tomcat-连接器调优]] — Tomcat 连接器调优（#103）
- [[http2-与多路复用]] — HTTP/2 与多路复用（#104）
- [[cdn-与静态加速]] — CDN 与静态加速（#105）
- [[mysql-备份与恢复]] — MySQL 备份与恢复（#106）
- [[redis-备份策略]] — Redis 备份策略（#107）
- [[灾备-rpo-rto]] — 灾备 RPO 与 RTO（#108）
- [[功能开关-feature-flag]] — 功能开关 Feature Flag（#109）
- [[服务降级与熔断实践]] — 服务降级与熔断实践（#110）
- [[混沌工程入门]] — 混沌工程入门（#111）
- [[openapi3-与接口文档]] — OpenAPI 3 与接口文档（#112）
- [[mybatis-插件与拦截器]] — MyBatis 插件与拦截器（#113）
- [[pagehelper-分页实践]] — PageHelper 分页实践（#114）
- [[字段级加密存储]] — 字段级加密存储（#115）
- [[日志脱敏规范]] — 日志脱敏规范（#116）
- [[ldap-与企业账号]] — LDAP 与企业账号（#117）
- [[cas-单点登录协议]] — CAS 单点登录协议（#118）
- [[webflux-响应式入门]] — WebFlux 响应式入门（#119）
- [[reactor-mono与-flux]] — Reactor Mono 与 Flux（#120）
- [[jdk-升级与兼容性]] — JDK 升级与兼容性（#121）
- [[java-虚拟线程]] — Java 虚拟线程（#122）
- [[graphql-入门]] — GraphQL 入门（#123）
- [[protobuf-序列化]] — Protobuf 序列化（#124）
- [[thrift-rpc框架]] — Thrift RPC 框架（#125）
- [[grpc-入门]] — gRPC 入门（#126）
- [[对象存储-生命周期]] — 对象存储生命周期（#127）
- [[minio-高可用规划]] — MinIO 高可用规划（#128）
- [[nacos-配置灰度发布]] — Nacos 配置灰度发布（#129）
- [[sentinel-热点参数限流]] — Sentinel 热点参数限流（#130）
- [[dubbo-分组版本与环境]] — Dubbo 分组版本与环境（#131）
- [[gateway-断言与请求改写]] — Gateway 断言与请求改写（#132）
- [[shiro-rememberme-安全]] — Shiro RememberMe 安全（#133）
- [[rbac-行级数据权限]] — RBAC 行级数据权限（#134）
- [[订单-对账与补偿]] — 订单对账与补偿（#135）
- [[秒杀-库存对账校正]] — 秒杀库存对账校正（#136）
- [[bi-自助查询规划]] — BI 自助查询规划（#137）
- [[知识库-混合检索规划]] — 知识库混合检索规划（#138）
- [[wiki-ingest-质量规范]] — Wiki Ingest 质量规范（#139）
- [[wiki-图谱与lint治理]] — Wiki 图谱与 Lint 治理（#140）
- [[ci-知识库同步门禁]] — CI 知识库同步门禁（#141）
- [[sonarqube-代码质量]] — SonarQube 代码质量（#142）
- [[代码审查-checklist]] — 代码审查 Checklist（#143）
- [[技术债-管理]] — 技术债管理（#144）
- [[架构决策-adr]] — 架构决策记录 ADR（#145）
- [[茉莉可观测性与运维体系汇总]] — 茉莉可观测性与运维体系汇总（#146）

## 批次 #147–#156 新增（10 批）

- [[spring-async与线程池]] — Spring @Async 与线程池（#147）
- [[completablefuture-异步编排]] — CompletableFuture 异步编排（#148）
- [[threadlocal-与上下文传递]] — ThreadLocal 与上下文传递（#149）
- [[mdc-日志链路上下文]] — MDC 日志链路上下文（#150）
- [[spring-cache-注解缓存]] — Spring Cache 注解缓存（#151）
- [[caffeine-本地缓存实践]] — Caffeine 本地缓存实践（#152）
- [[juc-并发工具类]] — JUC 并发工具类（#153）
- [[transmittable-thread-local跨线程]] — TTL 跨线程传递（#154）
- [[异步编程面试题]] — 异步编程面试题（#155）
- [[茉莉异步与线程模型要点汇总]] — 茉莉异步与线程模型要点汇总（#156）

## 批次 #157–#176 新增（20 批）

- [[druid-连接池泄漏排查]] — Druid 连接池泄漏排查（#157）
- [[mysql-slow-log慢查询分析]] — MySQL Slow Log 慢查询分析（#158）
- [[rocketmq-消息堆积排查]] — RocketMQ 消息堆积排查（#159）
- [[spring-boot-启动优化]] — Spring Boot 启动优化（#160）
- [[jvm-gc调优实战]] — JVM GC 调优实战（#161）
- [[arthas-在线诊断]] — Arthas 在线诊断入门（#162）
- [[gateway-超时与重试配置]] — Gateway 超时与重试配置（#163）
- [[feign-超时重试配置]] — Feign 超时重试配置（#164）
- [[dubbo-超时链路传递]] — Dubbo 超时与链路传递（#165）
- [[蓝绿与滚动发布]] — 蓝绿与滚动发布（#166）
- [[k8s-健康检查探针]] — K8s 健康检查探针（#167）
- [[csrf与xss防护]] — CSRF 与 XSS 防护（#168）
- [[bcrypt-密码哈希与加盐]] — BCrypt 密码哈希与加盐（#169）
- [[api-向后兼容策略]] — API 向后兼容策略（#170）
- [[pact-契约测试入门]] — Pact 契约测试入门（#171）
- [[testcontainers-集成测试]] — Testcontainers 集成测试（#172）
- [[prometheus-告警规则设计]] — Prometheus 告警规则设计（#173）
- [[事故复盘-postmortem]] — 事故复盘（Postmortem）（#174）
- [[容量规划与水平扩展]] — 容量规划与水平扩展（#175）
- [[茉莉稳定性与故障排查要点汇总]] — 茉莉稳定性与故障排查要点汇总（#176）

## 批次 #177–#186 新增（10 批）

- [[elasticsearch-ik分词与分析器]] — Elasticsearch IK 分词（#177）
- [[kb-wiki到es同步流水线]] — Wiki 到 ES 同步流水线（#178）
- [[okhttp-与-http客户端]] — OkHttp 与 HTTP 客户端（#179）
- [[webclient-与-resttemplate]] — WebClient 与 RestTemplate（#180）
- [[sse-服务端推送]] — SSE 服务端推送（#181）
- [[nginx-限流与缓冲调优]] — Nginx 限流与缓冲（#182）
- [[linux-ulimit与文件句柄]] — Linux ulimit 与文件句柄（#183）
- [[网络-端口与连通性排查]] — 网络端口与连通性排查（#184）
- [[flyway-数据库版本迁移]] — Flyway 数据库版本迁移（#185）
- [[茉莉知识库检索与存储全链路汇总]] — 知识库检索与存储全链路汇总（#186）

## 批次 #187–#211 新增（25 批）

- [[vue3-composition-api入门]] — Vue 3 Composition API 入门（#187）
- [[vue-router-路由守卫]] — Vue Router 路由守卫（#188）
- [[pinia-状态管理]] — Pinia 状态管理（#189）
- [[axios-拦截器实践]] — Axios 拦截器实践（#190）
- [[element-plus-组件实践]] — Element Plus 组件实践（#191）
- [[前端性能-懒加载与分包]] — 前端性能：懒加载与分包（#192）
- [[vite-构建与代理]] — Vite 构建与代理（#193）
- [[前端鉴权与路由存储]] — 前端鉴权与路由存储（#194）
- [[vitest-单元测试]] — Vitest 单元测试（#195）
- [[vue-前端面试题]] — Vue 前端面试题（#196）
- [[订单-拆单与合单]] — 订单拆单与合单（#197）
- [[支付-渠道路由]] — 支付渠道路由（#198）
- [[退款-流程与幂等]] — 退款流程与幂等（#199）
- [[库存-预占与释放]] — 库存预占与释放（#200）
- [[结算-与对账]] — 结算与对账（#201）
- [[物流-轨迹规划]] — 物流轨迹规划（#202）
- [[营销-优惠券叠加规则]] — 营销优惠券叠加规则（#203）
- [[会员-积分体系规划]] — 会员积分体系规划（#204）
- [[商品-sku与spu设计]] — 商品 SKU 与 SPU 设计（#205）
- [[茉莉电商交易链路汇总]] — 茉莉电商交易链路汇总（#206）
- [[java-stream-api实践]] — Java Stream API 实践（#207）
- [[java-optional与-null安全]] — Optional 与 Null 安全（#208）
- [[java-反射与注解]] — Java 反射与注解（#209）
- [[java-序列化安全]] — Java 序列化安全（#210）
- [[java-time-api实践]] — Java Time API 实践（#211）

## 批次 #212–#236 新增（25 批）

- [[java-records与-sealed]] — Java Records 与 Sealed Class（#212）
- [[java-module-jpms]] — Java 模块系统 JPMS（#213）
- [[java-nio-file实践]] — Java NIO File 实践（#214）
- [[java-异常最佳实践]] — Java 异常最佳实践（#215）
- [[java-语言特性面试题]] — Java 语言特性面试题（#216）
- [[spring-validation校验]] — Spring Validation 校验（#217）
- [[spring-converter与-formatter]] — Spring Converter 与 Formatter（#218）
- [[spring-conditional条件装配]] — Spring Conditional 条件装配（#219）
- [[spring-profile多环境]] — Spring Profile 多环境（#220）
- [[spring-boot-starter定制]] — Spring Boot Starter 定制（#221）
- [[spring-boot-devtools]] — Spring Boot DevTools（#222）
- [[spring-websocket-stomp]] — Spring WebSocket STOMP（#223）
- [[spring-task与-quartz对比]] — Spring Task 与 Quartz 对比（#224）
- [[spring-国际化-i18n]] — Spring 国际化 i18n（#225）
- [[spring-boot-进阶面试题]] — Spring Boot 进阶面试题（#226）
- [[rabbitmq-消费确认与重试]] — RabbitMQ 消费确认与重试（#227）
- [[rabbitmq-死信队列]] — RabbitMQ 死信队列（#228）
- [[kafka-分区与副本]] — Kafka 分区与副本（#229）
- [[kafka-生产者幂等]] — Kafka 生产者幂等（#230）
- [[redis-pipeline与批量]] — Redis Pipeline 与批量（#231）
- [[redis-pub-sub实践]] — Redis Pub/Sub 实践（#232）
- [[redis-bitmap与hyperloglog]] — Redis Bitmap 与 HyperLogLog（#233）
- [[zookeeper-watch与选举]] — ZooKeeper Watch 与选举（#234）
- [[nacos-长轮询与配置推送]] — Nacos 长轮询与配置推送（#235）
- [[茉莉中间件集成实践汇总]] — 茉莉中间件集成实践汇总（#236）

## 批次 #237–#261 新增（25 批）

- [[mysql-执行计划缓存]] — MySQL 执行计划缓存（#237）
- [[mysql-分区表实践]] — MySQL 分区表实践（#238）
- [[mysql-在线ddl-pt-osc]] — MySQL 在线 DDL 与 pt-osc（#239）
- [[mysql-读延迟与一致性]] — MySQL 读延迟与一致性（#240）
- [[redis-cluster-槽迁移]] — Redis Cluster 槽迁移（#241）
- [[mongodb-聚合管道]] — MongoDB 聚合管道（#242）
- [[es-搜索高亮与建议]] — ES 搜索高亮与建议（#243）
- [[es-索引生命周期-ilm]] — ES 索引生命周期 ILM（#244）
- [[数据归档-与冷热分离]] — 数据归档与冷热分离（#245）
- [[mysql-dba-面试题]] — MySQL DBA 面试题（#246）
- [[docker-镜像分层与优化]] — Docker 镜像分层与优化（#247）
- [[docker-network-网络模式]] — Docker Network 网络模式（#248）
- [[k8s-deployment-滚动更新]] — K8s Deployment 滚动更新（#249）
- [[k8s-configmap与-secret]] — K8s ConfigMap 与 Secret（#250）
- [[k8s-pv-pvc-存储]] — K8s PV 与 PVC 存储（#251）
- [[helm-chart-入门]] — Helm Chart 入门（#252）
- [[gitops-声明式部署]] — GitOps 声明式部署（#253）
- [[jenkins-pipeline-流水线]] — Jenkins Pipeline 流水线（#254）
- [[镜像扫描与供应链安全]] — 镜像扫描与供应链安全（#255）
- [[devops-面试题]] — DevOps 面试题（#256）
- [[jwt-实现与刷新令牌]] — JWT 实现与刷新令牌（#257）
- [[oauth2-授权码模式详解]] — OAuth2 授权码模式详解（#258）
- [[api-签名校验]] — API 签名校验（#259）
- [[敏感数据-脱敏与加密]] — 敏感数据脱敏与加密（#260）
- [[waf-与-ddos-防护]] — WAF 与 DDoS 防护（#261）

## 批次 #262–#286 新增（25 批）

- [[审计日志-设计]] — 审计日志设计（#262）
- [[密钥轮换-与-kms规划]] — 密钥轮换与 KMS 规划（#263）
- [[零信任-安全模型]] — 零信任安全模型（#264）
- [[渗透测试-基线]] — 渗透测试基线（#265）
- [[茉莉安全与合规要点汇总]] — 茉莉安全与合规要点汇总（#266）
- [[junit5-参数化测试]] — JUnit5 参数化测试（#267）
- [[mockito-静态方法与void]] — Mockito 静态方法与 void（#268）
- [[测试金字塔-与分层]] — 测试金字塔与分层（#269）
- [[pact-broker-与-ci]] — Pact Broker 与 CI（#270）
- [[jmeter-性能测试入门]] — JMeter 性能测试入门（#271）
- [[混沌测试-工具与实践]] — 混沌测试工具与实践（#272）
- [[jacoco-代码覆盖率]] — JaCoCo 代码覆盖率（#273）
- [[mutation-test-入门]] — Mutation Test 入门（#274）
- [[测试数据-工厂模式]] — 测试数据工厂模式（#275）
- [[测试与质量面试题]] — 测试与质量面试题（#276）
- [[领域驱动设计-入门]] — 领域驱动设计入门（#277）
- [[事件风暴-工作坊]] — 事件风暴工作坊（#278）
- [[cqrs-读写分离架构]] — CQRS 读写分离架构（#279）
- [[事件溯源-入门]] — 事件溯源入门（#280）
- [[saas-多租户架构]] — SaaS 多租户架构（#281）
- [[微服务-拆分原则]] — 微服务拆分原则（#282）
- [[康威定律-与组织]] — 康威定律与组织（#283）
- [[技术雷达-维护]] — 技术雷达维护（#284）
- [[茉莉微服务演进路线-2026]] — 茉莉微服务演进路线 2026（#285）
- [[茉莉知识体系100批索引]] — 茉莉知识体系100批索引（#286）

## 批次 #187–#286 总览（100 批）

分域入口见 [[茉莉知识体系100批索引]]；按段列表见上方四段（#187–#211 / #212–#236 / #237–#261 / #262–#286）。

## 批次 #287–#1286 总览（1000 批 · 已完成）

- **总入口**：[[茉莉知识体系1000批总索引]]
- **上一档**：[[茉莉知识体系100批索引]]（#187–#286）
- **10 个 megacluster**：每 100 批一域（前端 / Java / Spring / 数据 / 中间件 / DevOps / 安全 / 测试 / 架构 / 茉莉）
- **汇总页**：[[茉莉前端与客户端体系汇总]] · [[茉莉数据存储深化汇总]] · [[茉莉集成中间件100批汇总]] · [[茉莉平台工程与SRE汇总]] · [[茉莉安全深化100批汇总]] · [[茉莉质量工程100批汇总]] · [[茉莉架构模式100批汇总]]

_1000 批计划 **#287–#1286** 已完成；下一批从 **#1287** 起。_

_增量 ingest 见 [[增量ingest与raw投喂指南]]。_
