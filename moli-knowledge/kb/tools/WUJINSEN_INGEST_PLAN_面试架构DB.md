# wujinsen_markdown Ingest 规划 · 面试笔试 + 架构 + DataBase

> **空间**：仅 `enterprise-kb`（`kb/wiki/{category}/`）· **禁止**写入 `wiki-moli/`
> **策略**：默认 A（enrich 已有 slug）· 建议批次 `#1310`
> **统计**：raw `.md` **372** 篇 · 已被 wiki `sources` 引用 **113** · 待挂接 **297**

## 执行顺序

1. 按 P0 → P1 → P2 执行 enrich/create
2. `python kb/tools/lint.py --strict`
3. `python kb/tools/sync_to_db.py --wiki-dir wiki --space enterprise-kb`
4. Web 单空间浏览验证分类/体裁 facet

## 规划表

| 优先级 | 动作 | raw 簇 | raw≈ | 未挂接≈ | 分类 | 目标 slug | 体裁 | 已有 wiki | 说明 |
|--------|------|--------|------|---------|------|-----------|------|-----------|------|
| P0 | **enrich** | `DataBase/Redis/` | 27 | 26 | `cache` | `cache/redis-面试题` | `interview` | `cache/redisson-看门狗与分布式锁` | Redis 夺命16问等 |
| P0 | **enrich** | `DataBase/mysql/` | 34 | 21 | `database` | `database/mysql-索引面试题` | `interview` | `database/b-plus树与-innodb索引结构`, `database/mysql-innodb锁机制` +12 | B+树/ROW_FORMAT/20道经典题等 |
| P0 | **enrich** | `架构/DevOps/` | 44 | 41 | `ops` | `ops/jenkins-ci入门` | `guide` | `ops/jenkins-ci入门` | CI/CD |
| P0 | **enrich** | `架构/MicroServer/` | 41 | 35 | `middleware` | `—` | `article` | `middleware/feign-开发踩坑`, `middleware/sentinel-接入与规则配置` +3 | 见下表：Gateway/Sentinel/Feign 等多 slug |
| P0 | **enrich** | `架构/MicroServer/SpringCloud/Hystrix/` | 22 | 21 | `middleware` | `middleware/sentinel-限流与熔断` | `article` | `middleware/sentinel-限流与熔断`, `middleware/限流算法与令牌桶` | Hystrix 限流降级（历史对照 enrich） |
| P0 | **enrich** | `架构/中间件/` | 5 | 5 | `middleware` | `middleware/消息队列` | `concept` | — | 中间件总览 raw |
| P0 | **enrich** | `架构/分布式事务/` | 11 | 4 | `middleware` | `middleware/分布式事务` | `concept` | `cache/redisson-看门狗与分布式锁`, `cache/redis分布式锁实现` +6 | Seata/TCC 等 |
| P0 | **enrich** | `架构/安全框架/` | 7 | 7 | `security` | `security/shiro-鉴权体系` | `concept` | — | 安全框架选型/配置 |
| P0 | **enrich** | `架构/容器/Docker/` | 8 | 7 | `ops` | `ops/容器与-docker` | `guide` | `ops/容器与-docker` | Docker 安装/命令 |
| P0 | **enrich** | `架构/容器/k8s/` | 6 | 5 | `ops` | `ops/k8s入门与容器编排` | `guide` | `ops/k8s入门与容器编排` | K8s 笔记 |
| P0 | **enrich** | `面试笔试/Dubbo/` | 6 | 5 | `middleware` | `middleware/dubbo-调用原理与分层` | `article` | `middleware/rpc-超时重试与链路` | Dubbo 剖析 |
| P0 | **enrich** | `面试笔试/Java/` | 16 | 16 | `java` | `java/java-并发面试题` | `interview` | — | Java 基础/并发包 |
| P0 | **enrich** | `面试笔试/面试小结/` | 8 | 4 | `java` | `—` | `interview` | `cache/分布式锁`, `cache/分布式锁面试题` +13 | 按主题拆 enrich，不建汇总页 |
| P0 | **enrich** | `面试笔试/面试题整理/` | 10 | 4 | `java` | `—` | `interview` | `cache/redis-数据结构与使用场景`, `database/mysql-复合索引与最左前缀` +8 | 按题 merge 到已有 interview |
| P1 | **enrich** | `DataBase/canal/` | 1 | 1 | `database` | `database/mysql-binlog与canal同步` | `article` | — | Canal 同步 |
| P1 | **enrich** | `DataBase/mongodb/` | 3 | 2 | `database` | `database/mongodb与文档库选型` | `article` | `database/mongodb与文档库选型` | 文档库选型 |
| P1 | **enrich** | `DataBase/mysql/索引/` | 7 | 1 | `database` | `database/mysql-索引` | `concept` | `database/b-plus树与-innodb索引结构`, `database/mysql-复合索引与最左前缀` +5 | 索引原理 |
| P1 | **enrich** | `架构/MicroServer/SpringCloud/SpringCloudGateWay/` | 3 | 2 | `spring` | `spring/spring-cloud-gateway` | `concept` | `middleware/跨域与前后端分离` | Gateway 原理/跨域 |
| P1 | **enrich** | `架构/MicroServer/SpringCloud/采坑记录/` | 5 | 3 | `middleware` | `middleware/feign-开发踩坑` | `article` | `middleware/feign-开发踩坑` | Feign 上传/配置踩坑 |
| P1 | **enrich** | `架构/安全/` | 4 | 4 | `security` | `security/api-接口安全设计` | `article` | — | 安全实践 |
| P1 | **enrich** | `架构/微服务认证/` | 1 | 1 | `security` | `security/sso与系统门户` | `article` | — | Spring Cloud Security+CAS |
| P1 | **enrich** | `架构/性能调优/Arthas/` | 1 | 1 | `java` | `java/arthas-在线诊断` | `guide` | — |  |
| P1 | **enrich** | `架构/性能调优/JVM/` | 2 | 2 | `java` | `java/jvm-gc调优实战` | `article` | — | GC/堆设置 |
| P1 | **create** | `架构/文件存储/minio/` | 5 | 5 | `middleware` | `middleware/minio-对象存储实践` | `guide` | — | MinIO 安装/迁移 |
| P1 | **enrich** | `架构/服务注册发现/nacos/` | 2 | 2 | `middleware` | `middleware/nacos-注册与配置` | `concept` | — | Nacos 架构 |
| P1 | **enrich** | `架构/消息队列/RabbitMQ/` | 4 | 2 | `middleware` | `middleware/rabbitmq-入门与使用场景` | `guide` | `middleware/rabbitmq-入门与使用场景` | 安装教程 |
| P1 | **enrich** | `架构/消息队列/RocketMQ/` | 5 | 1 | `middleware` | `middleware/rocketmq-架构与实战` | `article` | `cache/延迟消息与队列`, `database/mysql-binlog与canal同步` +2 | 安装+实战 |
| P1 | **enrich** | `架构/缓存/` | 2 | 2 | `cache` | `cache/cache-aside与缓存更新模式` | `article` | — | 穿透/击穿/雪崩 |
| P1 | **enrich** | `架构/运维/` | 2 | 1 | `ops` | `ops/linux-运维基础` | `guide` | `ops/linux-运维基础` | 组件/防火墙 |
| P1 | **enrich** | `架构/项目踩坑/` | 2 | 2 | `middleware` | `middleware/feign-开发踩坑` | `article` | — | 版本兼容 |
| P1 | **enrich** | `面试笔试/Database/` | 3 | 1 | `database` | `database/mysql-索引面试题` | `interview` | `database/b-plus树与-innodb索引结构`, `database/mysql-索引面试题` | MySQL 20 道等 |
| P1 | **enrich** | `面试笔试/ElasticSearch/` | 2 | 2 | `search` | `search/elasticsearch-面试题` | `interview` | — | ES 小结 |
| P1 | **enrich** | `面试笔试/Java/JVM/` | 2 | 2 | `java` | `java/jvm-面试题` | `interview` | — | JVM/GC |
| P1 | **enrich** | `面试笔试/Java面试题精选/` | 4 | 1 | `java` | `java/java-并发面试题` | `interview` | `java/concurrenthashmap原理`, `java/java-并发面试题` +3 | 【67-70期】系列 |
| P1 | **enrich** | `面试笔试/kafka/` | 4 | 3 | `middleware` | `middleware/kafka-与-mq选型` | `interview` | `middleware/kafka-与-mq选型` | Kafka 面试+丢消息 |
| P1 | **enrich** | `面试笔试/分布式/` | 1 | 1 | `cache` | `cache/分布式锁面试题` | `interview` | — | 分布式锁 |
| P1 | **enrich** | `面试笔试/安全性/` | 2 | 1 | `security` | `security/api-接口安全设计` | `article` | `security/api-接口安全设计` | API 安全 |
| P1 | **enrich** | `面试笔试/树/` | 2 | 2 | `database` | `database/b-plus树与-innodb索引结构` | `concept` | — | B/B+ 树 |
| P1 | **enrich** | `面试笔试/高级java/` | 5 | 1 | `java` | `java/hashmap-面试题` | `interview` | `cache/cache-aside与缓存更新模式`, `cache/redis-实现延迟队列` +11 | HashMap/高级面试 |
| P2 | **enrich** | `DataBase/Redis/Jedis/` | 1 | 0 | `cache` | `cache/redis-数据结构与使用场景` | `concept` | `cache/redisson-看门狗与分布式锁` | Jedis/Redisson 选型 |
| P2 | **enrich** | `DataBase/mysql/事务/` | 0 | 0 | `database` | `database/mysql-事务面试题` | `interview` | — | 隔离/MVCC/幻读 |
| P2 | **create** | `DataBase/mysql/分库分表/` | 1 | 1 | `database` | `database/sharding-分库分表入门` | `article` | — | 分库分表 raw 合并新建 |
| P2 | **enrich** | `DataBase/mysql/锁/` | 0 | 0 | `database` | `database/mysql-innodb锁机制` | `article` | — | InnoDB 锁 |
| P2 | **enrich** | `架构/MicroServer/SpringCloud/sentinel/` | 2 | 0 | `middleware` | `middleware/sentinel-限流与熔断` | `concept` | `middleware/sentinel-接入与规则配置`, `middleware/sentinel-限流与熔断` | Sentinel 滑动窗口/动态规则 |
| P2 | **create** | `架构/性能监控/skywalking/` | 1 | 1 | `ops` | `ops/skywalking-安装与链路追踪` | `guide` | — | SkyWalking 安装 |
| P2 | **create** | `架构/编码规范/` | 1 | 0 | `java` | `java/java-编码规范与CodeReview要点` | `guide` | `database/mybatis-plus-用法与注入防护` | Java 规范/CR |
| P2 | **create** | `架构/高并发/` | 1 | 1 | `middleware` | `middleware/高并发券系统实战` | `article` | — | 通用券系统/QPS，不进 wiki-moli |
| P2 | **enrich** | `面试笔试/Spring/` | 5 | 0 | `spring` | `spring/spring-事务` | `interview` | `java/异步编程面试题`, `spring/spring-aop与代理` +5 | Spring 事务多篇 |
| P2 | **enrich** | `面试笔试/redis/` | 1 | 0 | `cache` | `cache/redis分布式锁实现` | `article` | `cache/redis-面试题`, `cache/redis分布式锁实现` +1 | Redis 分布式锁 |
| P2 | **enrich** | `面试笔试/框架/zookeeper/` | 1 | 0 | `middleware` | `middleware/zookeeper-面试题` | `interview` | `middleware/zookeeper-与协调服务`, `middleware/zookeeper-面试题` |  |
| P2 | **create** | `面试笔试/算法/` | 1 | 1 | `patterns` | `patterns/算法面试题精选` | `interview` | — | 动态规划等 |
| P2 | **enrich** | `面试笔试/精尽面试题/JVM/` | 1 | 0 | `java` | `java/jvm-面试题` | `interview` | `java/jvm-面试题` | 精尽 JVM |
| P2 | **enrich** | `面试笔试/精尽面试题/dubbo/` | 1 | 0 | `middleware` | `middleware/dubbo-面试题` | `interview` | `middleware/dubbo-与-nacos`, `middleware/dubbo-调用原理与分层` +3 |  |
| skip | **skip** | `DataBase/Oracle/` | 2 | 2 | `database` | `—` | `—` | — | Oracle 非主栈 |
| skip | **skip** | `架构/Git/` | 9 | 9 | `ops` | `—` | `—` | — | Git 踩坑 → 已在 wiki-moli/git协作指南 有架构/Git sources；本批不进 enterprise 新建 |
| skip | **skip** | `架构/SAML/` | 13 | 13 | `security` | `—` | `—` | — | SAML/SSO 旧方案，按需下批 |
| skip | **skip** | `架构/区块链/` | 14 | 14 | `middleware` | `—` | `—` | — | 区块链非主栈 |
| skip | **skip** | `架构/开发工具/` | 3 | 3 | `ops` | `—` | `—` | — | IDE 踩坑，非 KB 核心 |
| skip | **skip** | `架构/消息队列/ActiveMQ/` | 2 | 2 | `middleware` | `—` | `—` | — | ActiveMQ 非主栈 |
| skip | **skip** | `架构/腾讯云/` | 1 | 1 | `ops` | `—` | `—` | — | 无标题空壳 |
| skip | **skip** | `架构/通信协议/Thrift/` | 4 | 4 | `middleware` | `—` | `—` | — | Thrift 非主栈 |
| skip | **skip** | `面试笔试/2020程序员内推/` | 0 | 0 | `middleware` | `—` | `—` | — | 内推/offer 营销 |
| skip | **skip** | `面试笔试/2020面试题整理/` | 2 | 2 | `java` | `—` | `—` | — | 与 面试题整理 重复倾向 |
| skip | **skip** | `面试笔试/大数据/` | 1 | 1 | `middleware` | `—` | `—` | — | 大数据岗真题 |
| skip | **skip** | `面试笔试/面试公司/` | 2 | 2 | `middleware` | `—` | `—` | — | 个人面试记录 |
| skip | **skip** | `面试笔试/面试要求/` | 1 | 1 | `middleware` | `—` | `—` | — | JD 剪藏 |

## create 新建页（6）

- **`database/sharding-分库分表入门`** · `article` · raw `DataBase/mysql/分库分表/`（1 篇）
- **`patterns/算法面试题精选`** · `interview` · raw `面试笔试/算法/`（1 篇）
- **`middleware/高并发券系统实战`** · `article` · raw `架构/高并发/`（1 篇）
- **`ops/skywalking-安装与链路追踪`** · `guide` · raw `架构/性能监控/skywalking/`（1 篇）
- **`middleware/minio-对象存储实践`** · `guide` · raw `架构/文件存储/minio/`（5 篇）
- **`java/java-编码规范与CodeReview要点`** · `guide` · raw `架构/编码规范/`（1 篇）

## skip（不 ingest）

- `DataBase/Oracle/`（2 篇）— Oracle 非主栈
- `架构/Git/`（9 篇）— Git 踩坑 → 已在 wiki-moli/git协作指南 有架构/Git sources；本批不进 enterprise 新建
- `架构/区块链/`（14 篇）— 区块链非主栈
- `架构/SAML/`（13 篇）— SAML/SSO 旧方案，按需下批
- `架构/开发工具/`（3 篇）— IDE 踩坑，非 KB 核心
- `面试笔试/2020面试题整理/`（2 篇）— 与 面试题整理 重复倾向
- `面试笔试/2020程序员内推/`（0 篇）— 内推/offer 营销
- `面试笔试/面试公司/`（2 篇）— 个人面试记录
- `面试笔试/面试要求/`（1 篇）— JD 剪藏
- `面试笔试/大数据/`（1 篇）— 大数据岗真题
- `架构/消息队列/ActiveMQ/`（2 篇）— ActiveMQ 非主栈
- `架构/腾讯云/`（1 篇）— 无标题空壳
- `架构/通信协议/Thrift/`（4 篇）— Thrift 非主栈

## enrich · P0 优先

- `database/mysql-索引面试题` ← `DataBase/mysql/`（+21 sources）
- `cache/redis-面试题` ← `DataBase/Redis/`（+26 sources）
- `middleware/—` ← `架构/MicroServer/`（+35 sources）
- `middleware/sentinel-限流与熔断` ← `架构/MicroServer/SpringCloud/Hystrix/`（+21 sources）
- `middleware/分布式事务` ← `架构/分布式事务/`（+4 sources）
- `security/shiro-鉴权体系` ← `架构/安全框架/`（+7 sources）
- `middleware/消息队列` ← `架构/中间件/`（+5 sources）
- `java/java-并发面试题` ← `面试笔试/Java/`（+16 sources）
- `middleware/dubbo-调用原理与分层` ← `面试笔试/Dubbo/`（+5 sources）
- `java/—` ← `面试笔试/面试小结/`（+4 sources）
- `java/—` ← `面试笔试/面试题整理/`（+4 sources）
- `ops/容器与-docker` ← `架构/容器/Docker/`（+7 sources）
- `ops/k8s入门与容器编排` ← `架构/容器/k8s/`（+5 sources）
- `ops/jenkins-ci入门` ← `架构/DevOps/`（+41 sources）

## 未纳入主题的 raw（需人工或下批）

- `DataBase/MySQL外键设置中的的 Cascade、NO ACTION、Restrict、SET NULL.note.md/` — 1 篇（建议 skip 或并入邻近 enrich）
- `DataBase/MySQL查询语句练习题，测试基本够用了.note.md/` — 1 篇（建议 skip 或并入邻近 enrich）
- `DataBase/left join on 和where条件的放置.note.md/` — 1 篇（建议 skip 或并入邻近 enrich）
- `DataBase/mysql5.6修改编码 .note.md/` — 1 篇（建议 skip 或并入邻近 enrich）
- `DataBase/postgresql/` — 1 篇（建议 skip 或并入邻近 enrich）
- `DataBase/中间件/` — 1 篇（建议 skip 或并入邻近 enrich）
- `架构/DDD领域驱动/` — 1 篇（建议 skip 或并入邻近 enrich）
- `架构/Lambda架构/` — 1 篇（建议 skip 或并入邻近 enrich）
- `架构/NaiXue/` — 1 篇（建议 skip 或并入邻近 enrich）
- `架构/云原生/` — 1 篇（建议 skip 或并入邻近 enrich）
- `架构/分库分表/` — 1 篇（建议 skip 或并入邻近 enrich）
- `架构/埋点/` — 1 篇（建议 skip 或并入邻近 enrich）
- `架构/性能监控/` — 1 篇（建议 skip 或并入邻近 enrich）
- `架构/文件存储/` — 1 篇（建议 skip 或并入邻近 enrich）
- `架构/轻量级分布式 RPC 框架.note.md/` — 1 篇（建议 skip 或并入邻近 enrich）

## 面试小结 / 面试题整理 → 多 slug enrich 映射

| raw 子篇 | 目标 slug | 体裁 |
|----------|-----------|------|
| 面试小结之并发篇 | `java/java-并发面试题` | interview |
| 面试小结之 IO 篇 | `middleware/netty-与-io面试题` | interview |
| 面试小结之 Elasticsearch 篇 | `search/elasticsearch-面试题` | interview |
| 面试小结之综合篇 | 按题拆到 database/cache/java | interview |
| 面试题整理 · MySQL/索引 | `database/mysql-索引面试题` | interview |
| 面试题整理 · JVM | `java/jvm-面试题` | interview |
| 面试题整理 · CPU100% | `java/java-cpu-100排查实战` | article |

## conflicts / 人工确认

- **架构/高并发/优惠券**：落 `middleware/高并发券系统实战`（create），**不**写 `wiki-moli`
- **Redis 双路径**：`DataBase/Redis` 与 `架构/缓存` 统一 enrich 到 `cache/*`
- **冲突副本**：如 `Redis夺命16问(同步发生冲突)` — delete 或 skip 后再 ingest
- **面试小结/面试题整理**：拆到各主题 interview 页，不建「汇总 output」
