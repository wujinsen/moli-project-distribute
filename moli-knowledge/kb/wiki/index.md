# 知识库目录（index）

> 全库内容目录。每次 Ingest / Query 回写后更新。Query 时先读本文件定位相关页。

## guides（操作指导 · P0）

- [[本地启动指南]] — 从零本地启动茉莉微服务全家桶（Nacos/MySQL/Redis + 各服务，含启动顺序）
- [[登录与鉴权指南]] — 怎么登录拿 token、怎么带 `Authorization` 头跨服务调接口、常见返回码
- [[权限管理操作指南]] — 怎么管用户/角色/菜单/部门、给新员工开通权限的标准流程

## services（微服务实体）

- [[用户中心]] — 权限中枢：用户/角色/菜单/权限 + Shiro 认证 + Dubbo Provider（HTTP 8888）
- [[网关]] — Spring Cloud Gateway 统一入口，按路径转发（HTTP 21000，无鉴权/无 Sentinel）
- [[订单服务]] — 订单业务（通用为骨架）+ 秒杀子系统（HTTP 8087）
- [[bi服务]] — BI 服务，当前为骨架（HTTP 1128）
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

## articles（技术文章沉淀 · P1）

- [[redis分布式锁实现]] — Redis 锁演进史（SETNX→SET NX PX→Lua→看门狗→Redlock）与正确姿势、常见 bug
- [[mysql-复合索引与最左前缀]] — 联合索引设计、最左前缀、窄/宽索引与 EXPLAIN 验证
- [[mysql-覆盖索引与回表优化]] — 回表代价、Using index、ICP 与 SELECT * 规避
- [[mysql-索引失效场景]] — 运算/隐式转换/LIKE/OR/最左前缀等失效与排查习惯
- [[synchronized与锁原理]] — 三种用法、Monitor、锁升级、与 volatile 对比
- [[volatile与可见性]] — 可见性/有序性、DCL 单例、内存屏障
- [[concurrenthashmap原理]] — CHM 7 vs 8、无锁 get、put 与扩容

## interview（面试题 · P2）

- [[spring-事务]] — Spring 事务面试题系列：ACID / 隔离级别 / 7 种传播行为 / @Transactional 失效场景（由 6 篇杂乱原文去重提炼）
- [[分布式锁面试题]] — 分布式锁面试题：SETNX 误删/续期/可重入/Redlock 争议/DB·ZK 实现要点
- [[mysql-索引面试题]] — MySQL 索引 10 题：B+Tree / 聚簇·回表 / 覆盖·最左前缀 / 失效 / EXPLAIN
- [[java-并发面试题]] — Java 并发 10 题：JMM / synchronized·volatile / CHM / 线程池 / ThreadLocal

## outputs（问答回写综合页）

_（暂无，Query --crystallize 后生成）_
