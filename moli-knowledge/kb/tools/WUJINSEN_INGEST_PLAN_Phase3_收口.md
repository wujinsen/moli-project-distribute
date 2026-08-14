# wujinsen_markdown Ingest 规划 · Phase 3 收口

> **空间**：`enterprise-kb` only · **批次建议** `#1330`
> **目标**：370 篇未 cited → **enrich 补挂 / skip 定案 / delete 空壳**，不再新建分类/体裁
> **审计**：raw **1435** · cited **1083** · 未 cited **370**

## 动作统计

| 动作 | 篇数 | 说明 |
|------|------|------|
| **enrich** | 48 | 补挂 `sources` 到已有 slug（不新建页） |
| **skip** | 307 | 定案 skip，可选物理删 raw |
| **delete** | 15 | 物理删除 raw（空壳/冲突副本） |

## 执行顺序

1. **#1330-delete**：物理删 `delete` 清单（冲突副本/无标题/dfsdfa）
2. **#1330-enrich**：按 §规划表 enrich（脚本 `run_phase3_wujinsen_ingest.py`）
3. **#1330-skip**：`WUJINSEN_SKIP_MANIFEST.md` 定案，可选删 raw
4. `lint.py --strict` → `sync_to_db.py --space enterprise-kb`
5. 复跑 `_audit_wujinsen_coverage.py`，未 cited 应 ≈ skip 清单

## 规划表（按动作 · 簇）

| 动作 | raw 簇 / 规则 | 篇数≈ | 目标 slug | 说明 |
|------|---------------|-------|-----------|------|
| **skip** | `prefix-skip` | 239 | `—` | Phase1/2 已决策 skip |
| **skip** | `a安装文档` | 21 | `—` | 无组件关键词，安装碎片 skip |
| **enrich** | `BigData/零散` | 16 | `bigdata/* 就近` | 并入最近 bigdata 或 search/middleware slug |
| **enrich** | `大数据资料-王/x线程` | 14 | `java/java-并发面试题` | — |
| **skip** | `王树/剩余` | 11 | `—` | 低价值/重复，不单独 enrich |
| **delete** | `name-skip` | 9 | `—` | Phase1/2 已决策 skip |
| **skip** | `大数据资料-王/scala/` | 7 | `Spark 生态可选，非 Java 主栈` | — |
| **delete** | `prefix-skip` | 6 | `—` | Phase1/2 已决策 skip |
| **skip** | `other` | 6 | `—` | 未识别，默认 skip |
| **enrich** | `javaweb/` | 5 | `database/mybatis-与-druid持久层|security/认证与会话机制` | — |
| **skip** | `BigData/用户画像/` | 4 | `业务案例（非无标题）` | — |
| **enrich** | `大数据资料-王/rpc/` | 3 | `middleware/dubbo-调用原理与分层` | — |
| **skip** | `面试笔试/面试小结/2018` | 3 | `个人面试记录` | — |
| **skip** | `name-skip` | 2 | `—` | Phase1/2 已决策 skip |
| **skip** | `大数据资料-王/log4j/` | 2 | `日志配置碎片` | — |
| **enrich** | `架构/DDD领域驱动/` | 1 | `middleware/分布式事务` | — |
| **enrich** | `架构/DevOps/nexus/` | 1 | `ops/maven-多模块与依赖管理` | — |
| **enrich** | `架构/埋点/` | 1 | `bigdata/kafka-大数据管道` | — |
| **enrich** | `架构/轻量级分布式 RPC 框架` | 1 | `middleware/dubbo-调用原理与分层` | — |
| **enrich** | `源码分析/` | 1 | `见 Phase2 源码映射` | — |
| **enrich** | `面试笔试/JVM/` | 1 | `java/jvm-面试题` | — |
| **enrich** | `面试笔试/使用logstash` | 1 | `bigdata/elk-日志分析栈` | — |
| **enrich** | `面试笔试/教你如何迅速秒杀` | 1 | `patterns/算法面试题精选|bigdata/hadoop-面试题` | — |
| **enrich** | `面试笔试/海量数据处理` | 1 | `bigdata/spark-核心概念与实践|patterns/算法面试题精选` | — |
| **enrich** | `面试笔试/面试小结/面试小结之综合篇` | 1 | `java/java-并发面试题|database/mysql-索引面试题` | — |
| **skip** | `BigData/版本问题/` | 1 | `版本踩坑单篇` | — |
| **skip** | `BigData/知识图谱/` | 1 | `业务案例单篇` | — |
| **skip** | `DataBase/MySQL查询语句练习题` | 1 | `练习题，非 KB 正文` | — |
| **skip** | `DataBase/mysql5.6修改编码` | 1 | `版本过旧安装备忘` | — |
| **skip** | `DataBase/postgresql/` | 1 | `PostgreSQL 非主栈` | — |
| **skip** | `架构/Lambda架构/` | 1 | `架构范式单篇，非主栈面试` | — |
| **skip** | `架构/NaiXue/` | 1 | `外链/课程剪藏` | — |
| **skip** | `架构/云原生/quarkus/` | 1 | `Quarkus 非主栈` | — |
| **skip** | `架构/文件存储/fastdfs/` | 1 | `MinIO 已覆盖对象存储主栈` | — |
| **skip** | `面试笔试/mianshi` | 1 | `个人面试记录` | — |
| **skip** | `面试笔试/京东商城` | 1 | `JD/公司向` | — |
| **skip** | `面试笔试/简历` | 1 | `个人简历` | — |

## enrich 明细 · 按目标 slug

### `bigdata/* 就近`（+16 sources）
- `BigData/Ambari/mac docer安装部署ambari.note.md`
- `BigData/Apache Griffin/2016-12-21.note.md`
- `BigData/Cloudera/Apache、CDH和Cloudera三者有什么区别？.note.md`
- `BigData/Cloudera/cloudera安装部署/Cloudera Manager、CDH零基础入门、线路指导.note.md`
- `BigData/Cloudera/cloudera安装部署/Cloudera到底是否可以免费试用.note.md`
- `BigData/Hudi/方案/百信银行基于ApacheHudi实时数据湖演进方案.note.md`
- `BigData/Kylin/Kylin简介.note.md`
- `BigData/MongoDB/MongoDB数据同步工具之 MongoShake.note.md`
- … 还有 8 篇

### `java/java-并发面试题`（+15 sources）
- `大数据资料-王/x线程/Callable与Future的介绍.note.md`
- `大数据资料-王/x线程/CountDownLatch的介绍和使用.note.md`
- `大数据资料-王/x线程/ExecutorService线程池 .note.md`
- `大数据资料-王/x线程/Java Callable测试.note.md`
- `大数据资料-王/x线程/Java Callable用法.note.md`
- `大数据资料-王/x线程/Java多线程的用法详解.note.md`
- `大数据资料-王/x线程/Java并发编程：Lock.note.md`
- `大数据资料-王/x线程/Java并发编程：synchronized.note.md`
- … 还有 7 篇

### `database/mybatis-与-druid持久层`（+5 sources）
- `javaweb/Servlet生命周期与工作原理.note.md`
- `javaweb/jackson-mapper-asl总结一下自己使用jackson处理对象与JSON之间相互转换的心得。.note.md`
- `javaweb/spring监听器.note.md`
- `javaweb/为什么我再也不使用MVC框架了？.note.md`
- `javaweb/客户端跳转与服务器端跳转的区别.note.md`

### `security/认证与会话机制`（+5 sources）
- `javaweb/Servlet生命周期与工作原理.note.md`
- `javaweb/jackson-mapper-asl总结一下自己使用jackson处理对象与JSON之间相互转换的心得。.note.md`
- `javaweb/spring监听器.note.md`
- `javaweb/为什么我再也不使用MVC框架了？.note.md`
- `javaweb/客户端跳转与服务器端跳转的区别.note.md`

### `middleware/dubbo-调用原理与分层`（+4 sources）
- `大数据资料-王/rpc/webservice(1)(23-14-14).note.md`
- `大数据资料-王/rpc/webservice.note.md`
- `大数据资料-王/rpc/轻量级分布式 RPC 框架.note.md`
- `架构/轻量级分布式 RPC 框架.note.md`

### `patterns/算法面试题精选`（+2 sources）
- `面试笔试/教你如何迅速秒杀掉：99%的海量数据处理面试题.note.md`
- `面试笔试/海量数据处理：十道面试题与十个海量数据处理方法总结.note.md`

### `middleware/分布式事务`（+1 sources）
- `架构/DDD领域驱动/《中台架构与实现 DDD和微服务》核心思想.note.md`

### `ops/maven-多模块与依赖管理`（+1 sources）
- `架构/DevOps/nexus/maven---nexus私服配置setting和pom.note.md`

### `bigdata/kafka-大数据管道`（+1 sources）
- `架构/埋点/西瓜客户端埋点实践：基于责任链的埋点框架.note.md`

### `见 Phase2 源码映射`（+1 sources）
- `源码分析/芋道源码/精尽 Dubbo 源码分析 —— 序列化（二）之 Dubbo 实现.note.md`

### `java/jvm-面试题`（+1 sources）
- `面试笔试/JVM/jdk1.8——jvm分析与调优.note.md`

### `bigdata/elk-日志分析栈`（+1 sources）
- `面试笔试/使用logstash收集日志的可靠性验证.note.md`

### `bigdata/hadoop-面试题`（+1 sources）
- `面试笔试/教你如何迅速秒杀掉：99%的海量数据处理面试题.note.md`

### `bigdata/spark-核心概念与实践`（+1 sources）
- `面试笔试/海量数据处理：十道面试题与十个海量数据处理方法总结.note.md`

### `database/mysql-索引面试题`（+1 sources）
- `面试笔试/面试小结/面试小结之综合篇.note.md`


## skip 定案（不再 ingest）

以下 prefix 与 Phase 1/2 一致；Phase 3 仅 **定案 + 可选删 raw**：

- `AI/` — 29 篇
- `产品/` — 6 篇
- `写作/` — 2 篇
- `硬件/` — 1 篇
- `操作系统/` — 1 篇
- `EnglishDoc/` — 2 篇
- `Full Stack/` — 2 篇
- `IM通讯/` — 2 篇
- `DataBase/Oracle/` — 2 篇
- `架构/Git/` — 9 篇
- `架构/SAML/` — 13 篇
- `架构/区块链/` — 14 篇
- `架构/开发工具/` — 3 篇
- `架构/消息队列/ActiveMQ/` — 2 篇
- `架构/腾讯云/` — 1 篇
- `架构/通信协议/Thrift/` — 4 篇
- `面试笔试/2020面试题整理/` — 2 篇
- `面试笔试/面试公司/` — 2 篇
- `面试笔试/面试要求/` — 1 篇
- `面试笔试/大数据/` — 1 篇
- `大数据资料-王/QA/` — 115 篇
- `大数据资料-王/loadrunner/` — 7 篇
- `大数据资料-王/selecnium/` — 5 篇
- `大数据资料-王/thrift/` — 2 篇
- `大数据资料-王/mahout/` — 6 篇
- `大数据资料-王/nutch/` — 5 篇
- `大数据资料-王/lucene&solr/` — 6 篇

王树剩余低价值（无关键词安装稿外）：**~43 篇** → skip，不 enrich。

## delete 清单（物理删 raw）

- `AI/opensource/无标题笔记.note.md`
- `AI/无标题笔记.note.md`
- `AI/绘画/StableDiffusion/无标题笔记.note.md`
- `AI/绘画/踩坑记录/无标题笔记.note.md`
- `BigData/2023/无标题笔记.note.md`
- `BigData/Ambari/无标题笔记.note.md`
- `BigData/架构设计/大数据总体架构设计/无标题笔记.note.md`
- `BigData/用户画像/无标题笔记.note.md`
- `BigData/集群管理/无标题笔记.note.md`
- `DataBase/Oracle/无标题笔记.note.md`
- `DataBase/Redis/Redis夺命16问(同步发生冲突).note.md`
- `DataBase/mysql/dfsdfa.note.md`
- `jvm/jvm的参数查询列表(同步发生冲突).note.md`
- `架构/容器/k8s/k8s相关网站(同步发生冲突).note.md`
- `架构/腾讯云/无标题笔记.note.md`

## 验收

- [ ] 规划内 **enrich** 簇已补 sources
- [ ] **delete** 已从 raw 删除
- [ ] **skip** 写入 manifest（可选删 raw）
- [ ] 未 cited 仅剩 skip 簇（AI/QA/产品/王树低价值等）
- [ ] sync 无未分类文档

## conflicts

- **BigData/零散 27 篇**：优先挂最近 bigdata slug，不 create 新页
- **王树 206 uncited**：QA(115) + 安装 + 低价值；Phase 3 **不** bulk enrich QA
- **install 无关键词 21 篇**：默认 skip，不全文 ingest
