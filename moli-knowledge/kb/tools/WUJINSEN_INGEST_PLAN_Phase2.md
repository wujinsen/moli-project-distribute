# wujinsen_markdown Ingest 规划 · Phase 2（大数据 + Java 栈补全）

> **空间**：仅 `enterprise-kb`（`kb/wiki/{category}/`）· **禁止**写入 `wiki-moli/`  
> **策略**：默认 A（enrich 已有 slug）· 建议批次 **`#1320` 起**（Phase 1 已完成 `#1310`–`#1313`）  
> **前置**：Phase 1 见 [`WUJINSEN_INGEST_PLAN_面试架构DB.md`](WUJINSEN_INGEST_PLAN_面试架构DB.md)  
> **统计**（2026-07-05 审计）：Phase 2 范围 raw `.md` **1063** 篇 · 全库 wujinsen 已 cited **319** · Phase 2 树内 mostly **未挂接**

## 范围定义

**纳入 Phase 2**（除 Phase 1 三棵目录外的全部 top-level）：

| top-level | raw≈ | 定位 |
|-----------|------|------|
| `大数据资料-王/` | 522 | 安装/QA/面试向笔记，与 `BigData/` 大量重叠 |
| `BigData/` | 288 | 数仓/OLAP/组件架构向 |
| `jvm/` | 41 | JVM/GC/调优 → `java/*` |
| `Linux/` | 40 | Shell/系统 → `ops/*` |
| `前端/` | 39 | Vue/jQuery/跨域 → `frontend/*` |
| `Spring/` | 26 | 源码/MVC/事务补全 → `spring/*` |
| `AI/` | 29 | **默认 skip**（绘画/工具剪藏） |
| `并发编程/` | 13 | Netty/并发 → `middleware/*` + `java/*` |
| `javaweb/` | 13 | JWT/MyBatis → `security/*` + `database/*` |
| `源码分析/` | 14 | Dubbo/Feign/MQ → `middleware/*` |
| `数据结构及算法/` | 8 | → `patterns/算法面试题精选` enrich |
| `插件/` | 5 | Maven/Swagger → `ops/*` / `middleware/*` |
| 其余小目录 | ≤6/项 | 见 skip 表 |

**Phase 1 补尾**（仍在 `面试笔试`/`架构`/`DataBase` 内、未 cited 的 **38** 篇）：并入 **#1320 首批**，不单独开 Phase。

## 分类扩展（执行前确认）

Phase 2 需 **新建 Web 分类**（或 Agent 建 `wiki/bigdata/` 目录后补分类）：

| `dir_slug` | 建议 `default_type` | 用途 |
|------------|---------------------|------|
| **`bigdata`** | `article` | Hadoop/Spark/Flink/Hive/数仓/OLAP/采集 |

其余 **11 类**（Phase 2 已含 `bigdata`）：`database` · `cache` · `java` · `spring` · `middleware` · `search` · `security` · `ops` · `patterns` · `frontend` · **`bigdata`**。

## 执行顺序

1. **#1320** Phase 1 补尾 + P0（jvm / Spring / 并发 / 源码 / Linux / 前端）
2. **#1321** BigData 计算存储核心（Hadoop · Spark · Flink · Hive · HBase · Kafka）
3. **#1322** `大数据资料-王` 与 `BigData` **去重挂接**（同主题并 slug，不双建页）
4. **#1323** 数仓 / OLAP / ELK / 调度（Daas · OLAP · ELK · DolphinScheduler）
5. **#1324** P2 长尾 enrich + **物理 skip 清理**（无标题壳、QA 批量、重复安装稿）
6. `python kb/tools/lint.py --strict` → `sync_to_db.py --wiki-dir wiki --space enterprise-kb`
7. Web 验证 `bigdata` 分类 facet

## 规划表

| 优先级 | 动作 | raw 簇 | raw≈ | 未挂接≈ | 分类 | 目标 slug | 体裁 | 已有 wiki | 说明 |
|--------|------|--------|------|---------|------|-----------|------|-----------|------|
| P0 | **enrich** | `jvm/` | 41 | ~38 | `java` | `jvm-面试题` · `jvm-内存与gc` · `jvm-垃圾收集算法与收集器` · `jvm-gc调优实战` · `jvm-oom与排查入门` | interview/article | 已有 5+ 页 | GC/调优/内存模型；**#1320** |
| P0 | **enrich** | `jvm/GC/` | 6 | 6 | `java` | `jvm-垃圾收集算法与收集器` | article | 已有 | 收集器对比 |
| P0 | **enrich** | `jvm/调优/` | 4 | 3 | `java` | `jvm-gc调优实战` · `java-cpu-100排查实战` | article | 已有 | CPU100% 已有 1 篇 cited |
| P0 | **enrich** | `Spring/SpringMVC/` | 10 | ~9 | `spring` | `spring-mvc请求流程` · `spring-容器面试题` | interview/concept | 已有 | MVC 原理/设计模式篇 |
| P0 | **enrich** | `Spring/SpringBoot源码解析/` | 3 | ~1 | `spring` | `spring-boot-自动配置` · `enableautoconfiguration原理` | concept | 已有 | 自动装配补 sources |
| P0 | **enrich** | `Spring/事务/` | 2 | ~1 | `spring` | `spring-事务` · `spring-声明式事务` | interview | 已有 | @Transactional 失效 |
| P0 | **enrich** | `Spring/`（根·循环依赖） | ~11 | ~8 | `spring` | `spring-三级缓存与循环依赖` · `spring-ioc与bean生命周期` | concept | 已有 | 与 Phase 1 面试 Spring 互补 |
| P0 | **enrich** | `并发编程/Netty/` | 7 | ~5 | `middleware` | `netty-reactor与线程模型` · `netty-pipeline与编解码` · `io模型与-netty` | concept | 已有 | Reactor/ Pipeline |
| P0 | **enrich** | `并发编程/java/` | 6 | 6 | `java` | `java-并发面试题` · `bio-nio-aio对比` | interview/concept | 已有 | 与 `大数据资料-王/nio` 后续合并 |
| P0 | **enrich** | `源码分析/` | 14 | ~12 | `middleware` | 见下「源码 → slug 映射」 | article | 部分已有 | Dubbo/Feign/RocketMQ/MyCat 等 |
| P0 | **enrich** | `Linux/` | 40 | ~38 | `ops` | `linux-运维基础` · **create** `ops/shell-脚本入门` | guide | linux-运维基础 | Shell 教程单独 create |
| P0 | **enrich** | `前端/Vue/` | 11 | ~10 | `frontend` | `前端技术栈` · `前端基础面试题` | guide/interview | 已有 | Vue 笔记 |
| P0 | **enrich** | `前端/`（JS/jQuery/跨域） | 28 | ~25 | `frontend` · `middleware` | `前端基础面试题` · `跨域与前后端分离` | interview/article | 已有 | jQuery 偏历史，正文摘要即可 |
| P0 | **enrich** | `javaweb/jwt/` | 4 | 4 | `security` | `api-接口安全设计` · `认证与会话机制` | article | 已有 | JWT 实践 |
| P0 | **enrich** | `javaweb/Mybatis/` | 3 | 3 | `database` | `mybatis-与-druid持久层` · `mybatis-plus-用法与注入防护` | guide | 已有 | 持久层 |
| P0 | **enrich** | `数据结构及算法/` | 8 | 8 | `patterns` | `算法面试题精选` | interview | Phase1 create | DP/树/数组题 enrich |
| P0 | **补尾** | Phase 1 缺口 38 篇 | 38 | 38 | 各 | 见 Phase 1 §未纳入 | — | — | MicroServer 零散/无标题/冲突副本；**#1320 同批** |
| P1 | **create** | `BigData/Hadoop/` + `大数据资料-王/hadoop/` | 89 | ~85 | `bigdata` | `hadoop-生态入门` | concept | — | HDFS/YARN/MapReduce 综述 |
| P1 | **create** | `BigData/Spark/` + `spark(1)/` + `大数据资料-王/spark/` | 85 | ~83 | `bigdata` | `spark-核心概念与实践` | article | — | RDD/DataFrame/调优；**spark(1) 子目录合并** |
| P1 | **create** | `BigData/Flink/` | 11 | 11 | `bigdata` | `flink-流批一体入门` | article | — | DataStream/Checkpoint |
| P1 | **create** | `BigData/Hive/` + `大数据资料-王/hive/` | 30 | ~28 | `bigdata` | `hive-数仓与-sql` | guide | — | HiveQL/分区/桶 |
| P1 | **create** | `BigData/Kafka/` + `大数据资料-王/kafka/` | 44 | ~40 | `bigdata` | `kafka-大数据管道` | concept | `middleware/kafka-与-mq选型` | 管道/副本/ISR；与 middleware 面试页 **互链** |
| P1 | **enrich** | `BigData/HBase/` + `大数据资料-王/hbase/` | 18 | 18 | `bigdata` | **create** `hbase-列式存储入门` | concept | — | 列族/RowKey |
| P1 | **enrich** | `BigData/Flume/` + `大数据资料-王/flume/` | 7 | 7 | `bigdata` | **create** `flume-与-数据采集` | guide | — | Agent/Source/Channel |
| P1 | **enrich** | `BigData/Zookeeper/` + `王/zookeeper` | 14 | ~12 | `middleware` | `zookeeper-与协调服务` · `zookeeper-面试题` | concept/interview | 已有 | ZAB/选举 enrich |
| P1 | **enrich** | `BigData/ElasticSearch/` | 20 | ~18 | `search` | `elasticsearch-搜索` · `elasticsearch-面试题` · `es-match与bool查询` | article/interview | 已有 | 教程簇 bulk sources |
| P1 | **enrich** | `大数据资料-王/redis/` | 29 | ~27 | `cache` | `redis-集群与哨兵实践` · `redis-面试题` | guide/interview | 部分已有 | 安装稿多 → 摘要 + sources |
| P1 | **enrich** | `大数据资料-王/mysql/` | 19 | 19 | `database` | `mysql-索引` · `mysql-索引面试题` | concept/interview | 已有 | 安装/调优笔记并入 |
| P1 | **enrich** | `大数据资料-王/netty/` + `nio/` | 24 | ~22 | `middleware` · `java` | `netty-*` · `bio-nio-aio对比` | concept | 已有 | 与并发编程去重 |
| P1 | **enrich** | `大数据资料-王/linux/` | 39 | ~37 | `ops` | `linux-运维基础` | guide | 已有 | 与 `Linux/` 双树合并 |
| P1 | **enrich** | `大数据资料-王/jvm/` | 20 | 20 | `java` | `jvm-*` 多页 | interview/article | 已有 | 与 `jvm/` 双树合并 |
| P1 | **enrich** | `大数据资料-王/nginx+ka+lvs/` | 7 | 7 | `middleware` · `ops` | `nginx-限流与缓冲调优` · **create** `ops/nginx-反向代理与负载` | guide | nginx 限流已有 | LVS/KA 入门 |
| P2 | **create** | `BigData/架构设计/Daas/` | ~29 | ~29 | `bigdata` | `数仓分层与建模` | concept | — | ODS/DWD/DWS/ADS |
| P2 | **create** | `BigData/OLAP/` | 20 | 20 | `bigdata` | `olap-与-实时数仓` | article | — | ClickHouse/Kylin/Presto 选型 |
| P2 | **create** | `BigData/ELK/` + `FileBeat` | 5 | 5 | `bigdata` | `elk-日志分析栈` | guide | — | ES+Logstash+Kibana |
| P2 | **create** | `BigData/DolphinScheduler/` | 8 | 8 | `bigdata` | `dolphinscheduler-任务调度` | guide | — | DS vs Azkaban/Oozie |
| P2 | **enrich** | `BigData/数据采集/` + `Sqoop` + `王/sqoop` | 12 | 12 | `bigdata` | **create** `数据采集与-etl-工具选型` | article | — | DataX/Sqoop/Flume 对比 |
| P2 | **enrich** | `BigData/Storm/` + `王/storm` | 20 | 20 | `bigdata` | enrich `flink-*` 历史对照节 | article | — | Storm 降级为对照，不单独 create |
| P2 | **enrich** | `BigData/技术选型/` | 4 | 4 | `bigdata` | `数据采集与-etl-工具选型` | article | — | FlinkX/DataX/CDC |
| P2 | **enrich** | `源码分析/`（剩余） | — | — | `middleware` | `rocketmq-事务消息实践` · `openfeign-*` · `mycat-分片中间件` | article | 部分 | MyCat/ClickHouse |
| P2 | **enrich** | `插件/` | 5 | 5 | `ops` · `middleware` | **create** `ops/maven-多模块与依赖管理` | guide | — | PageHelper → database enrich |
| P2 | **enrich** | `性能优化/` | 3 | 3 | `database` · `java` | `mysql-索引` · `jvm-gc调优实战` | article | 已有 | 小簇并入 |
| P2 | **enrich** | `大数据资料-王/a安装文档/` | 54 | ~52 | `ops` · `cache` · `bigdata` | 各组件 **guide 节** + sources | guide | 部分 | **不全文粘贴**；按组件挂到已有 slug |
| P2 | **create** | `BigData/` · `王/` 面试向 | ~30 | ~30 | `bigdata` | `hadoop-面试题` · `spark-面试题` · **create** `flink-面试题` | interview | — | 从 QA/笔记抽题 enrich |
| skip | **skip** | `AI/` | 29 | 29 | — | — | — | — | 绘画/ChatGPT 剪藏/非技术 KB 核心 |
| skip | **skip** | `产品/` | 6 | 6 | — | — | — | — | 产品方法论，非 enterprise 八股 |
| skip | **skip** | `写作/` · `硬件/` · `操作系统/` · `EnglishDoc/` · `Full Stack/` · `IM通讯/` · `学习方法/` · `英语学习/` | ≤12 | 12 | — | — | — | — | 非主栈 |
| skip | **skip** | `大数据资料-王/QA/` | 115 | 115 | — | — | — | — | 测试/QA 题海，不 bulk ingest |
| skip | **skip** | `大数据资料-王/loadrunner/` · `selecnium/` | 12 | 12 | — | — | — | — | 压测/自动化测试工具 |
| skip | **skip** | `BigData/用户画像/` · `知识图谱/` · `集群管理/`（无标题） | 7 | 7 | — | — | — | — | 业务案例/空壳；按需单篇 enrich |
| skip | **skip** | `大数据资料-王/thrift/` · `mahout/` · `nutch/` · `lucene&solr/` | 19 | 19 | — | — | — | — | 非主栈或已有 ES 覆盖 |
| skip | **skip** | 无标题笔记 · `(同步发生冲突)` · `.note.attach` | — | — | — | — | — | — | 物理 delete 或 skip |

## create 新建页（Phase 2 · 约 18）

| slug | 体裁 | raw 主簇 | 批次 |
|------|------|----------|------|
| `bigdata/hadoop-生态入门` | concept | `BigData/Hadoop/` + `王/hadoop/` | #1321 |
| `bigdata/spark-核心概念与实践` | article | `BigData/Spark/` + `spark(1)/` + `王/spark/` | #1321 |
| `bigdata/flink-流批一体入门` | article | `BigData/Flink/` | #1321 |
| `bigdata/hive-数仓与-sql` | guide | `BigData/Hive/` + `王/hive/` | #1321 |
| `bigdata/kafka-大数据管道` | concept | `BigData/Kafka/` + `王/kafka/` | #1321 |
| `bigdata/hbase-列式存储入门` | concept | `王/hbase/` | #1321 |
| `bigdata/flume-与-数据采集` | guide | `BigData/Flume/` + `王/flume/` | #1321 |
| `bigdata/数仓分层与建模` | concept | `BigData/架构设计/Daas/` | #1323 |
| `bigdata/olap-与-实时数仓` | article | `BigData/OLAP/` | #1323 |
| `bigdata/elk-日志分析栈` | guide | `BigData/ELK/` | #1323 |
| `bigdata/dolphinscheduler-任务调度` | guide | `BigData/DolphinScheduler/` | #1323 |
| `bigdata/数据采集与-etl-工具选型` | article | `数据采集/` · `Sqoop` · `技术选型/` | #1323 |
| `bigdata/hadoop-面试题` | interview | 双树面试向 | #1323 |
| `bigdata/spark-面试题` | interview | 双树面试向 | #1323 |
| `bigdata/flink-面试题` | interview | `Flink/` + 实时数仓 | #1323 |
| `ops/shell-脚本入门` | guide | `Linux/Shell教程/` | #1320 |
| `ops/nginx-反向代理与负载` | guide | `王/nginx+ka+lvs/` | #1321 |
| `ops/maven-多模块与依赖管理` | guide | `插件/maven/` · `javaweb/Maven/` | #1324 |

## 源码分析 → slug 映射

| raw 子目录 | 目标 slug | 体裁 |
|------------|-----------|------|
| `源码分析/dubbo/` | `middleware/dubbo-调用原理与分层` | article |
| `源码分析/OpenFeign/` | `middleware/openfeign-与-http客户端` | article |
| `源码分析/RocketMQ/` | `middleware/rocketmq-事务消息实践` | article |
| `源码分析/Kafka/` | `middleware/kafka-与-mq选型` | interview |
| `源码分析/MyCat/` | **create** `middleware/mycat-分片中间件` 或 enrich `database/sharding-分库分表入门` | article |
| `源码分析/nacos/` | `middleware/nacos-注册与配置` | concept |
| `源码分析/clickhouse/` | enrich `bigdata/olap-与-实时数仓` | article |
| `源码分析/OpenJDK/` | `java/jvm-内存与gc` | concept |
| `源码分析/spring/` | `spring-ioc与bean生命周期` | concept |

## BigData ↔ 大数据资料-王 · 去重规则

1. **同主题只 enrich 一个 slug 族**，两树 raw 均写入 `sources`，正文不复制两份。  
2. **安装稿**（`a安装文档/`、各组件 install）：挂 sources + 运维要点 bullet，不全文 ingest。  
3. **重叠计数**：Hadoop 89 · Spark 85 · Kafka 44 · Hive 30 · Redis 29 · MySQL 19 · Linux 39 · JVM 20 · Netty/NIO 24 — 优先 **#1321 enrich create 页**，#1322 批量补 sources。  
4. **与 Phase 1 已有页**：Kafka/Redis/MySQL/ZK/ES 走 **enrich + 互链**，不新建平行页。

## Phase 1 补尾 · 38 篇（#1320 同批）

| raw | 建议动作 |
|-----|----------|
| `DataBase/` 零散 8 篇（外键/练习题/postgresql/中间件/冲突副本） | enrich 邻近 `database/*` 或 **skip** 空壳 |
| `架构/MicroServer/` 未挂接 ~15 篇 | enrich `middleware/feign-*` · `spring-cloud-gateway` · **skip** 无标题 |
| `架构/DDD` · `Lambda` · `NaiXue` · `云原生` · `分库分表` · `埋点` 等单篇 | enrich 邻近或 P2 **skip** |
| `架构/DevOps/jenkins/` 单篇 | enrich `ops/jenkins-ci入门` |

## skip 汇总（Phase 2 不 ingest · 约 200+）

- `AI/`（29）— 绘画 / 工具 / 非 KB  
- `产品/` · `写作/` · 微型生活目录（≤12）  
- `大数据资料-王/QA/`（115）— 测试题海  
- `loadrunner/` · `selecnium/`（12）  
- `thrift/` · `mahout/` · `nutch/` · `lucene&solr/`（19）  
- `BigData/` 空壳/用户画像/知识图谱（7，可选 P2 单篇）  
- 全库 **无标题** · **同步发生冲突** · `.note.attach` — delete 后 skip  

## conflicts / 人工确认

| 项 | 建议 |
|----|------|
| **新建 `bigdata` 分类** | Web 分类管理建 `dir_slug=bigdata`；Agent 可先 `mkdir wiki/bigdata/` |
| **Kafka 双 slug** | `middleware/kafka-与-mq选型`（面试/MQ）+ `bigdata/kafka-大数据管道`（管道/日志）互链 `related` |
| **Spark 子目录 `spark(1)/`（52 篇）** | 与 `Spark/` 合并为一个 slug，避免 `spark-2` 平行页 |
| **Storm vs Flink** | Storm 仅作 Flink 页历史对照段，不 create Storm 主页 |
| **MyCat create vs sharding enrich** | 倾向 **enrich** `database/sharding-分库分表入门`；若 MyCat 细节多再 create |
| **前端 jQuery 11 篇** | enrich `前端基础面试题` 一节「历史栈」，不 create jQuery 专题 |
| **AI 是否例外 ingest** | 默认 skip；若保留 `AI/opensource/` 单篇需用户确认 |
| **物理删除 skip 簇** | 与 Phase 1 相同：skip 后可删 raw 减体积（raw 只读契约下需用户确认 delete） |

## 批次与工作量预估

| 批次 | 范围 | enrich≈ | create≈ | raw 触达≈ |
|------|------|---------|---------|-----------|
| **#1320** | P0 + Phase1 补尾 | 25 页 | 1 | ~180 |
| **#1321** | BigData 核心 + 王·计算存储 | 10 页 | 7 | ~350 |
| **#1322** | 王·中间件/运维双树挂接 | 15 页 | 1 | ~200 |
| **#1323** | 数仓/OLAP/ELK/调度/面试 | 8 页 | 7 | ~120 |
| **#1324** | 插件/性能/安装稿摘要 + skip 清理 | 5 页 | 1 | ~80 |
| **skip** | QA/AI/测试/非主栈 | — | — | ~200（不写入 wiki） |

**说明**：触达 raw 数 **远大于** enrich 页数；未触达的 skip/空壳可在 #1324 后做第二轮 delete 审计。

## 验收标准

1. Phase 2 规划内 **P0/P1 create 页** 全部存在且 `sources` 含对应 raw 路径  
2. 双树主题（Hadoop/Kafka/Spark 等）**≥80%** 代表 raw 出现在某一 slug 的 `sources`  
3. `lint.py --strict` 通过（含修复 enterprise → wiki-moli 断链，可与 Phase 2 并行）  
4. `sync_to_db` 后 Web `enterprise-kb` 可见 **`bigdata` 分类**

---

**下一步**：确认是否新建 `bigdata` 分类 → 执行 **#1320**（可复用 Phase 1 脚本模式：`run_p0_wujinsen_phase2.py`）。
