# 茉莉项目微服务 — 技术栈文档

> 本文档基于项目 `README.md` 与微服务技术选型规范整理，描述茉莉分布式项目（moli-project-distribute）所采用的技术体系、版本要求及组件职责。

---

## 1. 项目概述

茉莉项目微服务版本采用 **Spring Cloud + Spring Cloud Alibaba** 体系构建，面向服务注册发现、统一网关、配置管理、熔断降级、RPC 调用等典型微服务场景，配合 MySQL、Redis 及可观测性组件，支撑用户中心、订单、BI 等业务模块。

---

## 2. 微服务架构选型

| 能力 | 技术组件 | 说明 |
|------|----------|------|
| 服务发现 | Spring Cloud Alibaba **Nacos Discovery** | 各微服务注册至 Nacos，网关与客户端通过服务名路由 |
| 配置中心 | Spring Cloud Alibaba **Nacos Config** | 集中管理 `bootstrap.yml` / 业务配置，支持动态刷新 |
| 服务网关 | **Spring Cloud Gateway** | 统一入口、路由转发（如 `moli-gateway` 模块） |
| 负载均衡 | **Spring Cloud Ribbon** | 配合 Nacos 实现客户端负载均衡（Spring Cloud Hoxton 内置） |
| 熔断降级 | Spring Cloud Alibaba **Sentinel** | 流量控制、熔断与降级保护 |
| 服务调用 | **Spring Cloud Dubbo** + **OpenFeign** | Dubbo 用于 RPC 服务暴露与引用；OpenFeign 用于 HTTP 声明式调用 |

### 服务调用说明

项目中两种调用方式并存，职责互补：

- **Dubbo**：高性能 RPC，适用于服务间强耦合、接口稳定的内部调用（如 `UserServerProvider` 暴露、`OrderController` 通过 `@DubboReference` 引用）。
- **OpenFeign**：基于 HTTP 的声明式客户端，适用于 REST 风格跨服务调用（如 `UserCenterClient`）。

---

## 3. 核心框架与中间件版本

### 3.1 Spring 生态

| 组件 | 版本 | 备注 |
|------|------|------|
| Java | **JDK 1.8** | `maven.compiler.source/target = 8` |
| Spring Boot | **2.3.12.RELEASE** | 父 POM 统一管理 |
| Spring Cloud | **Hoxton.SR12** | 与 Boot 2.3.x 配套 |
| Spring Cloud Alibaba | **2.2.7.RELEASE** | Nacos、Sentinel、Dubbo 等 Starter 版本来源 |

### 3.2 阿里巴巴中间件（部署/运行时版本）

| 组件 | 版本 | 用途 |
|------|------|------|
| Nacos | **2.0.3** | 注册中心 + 配置中心 |
| Sentinel | **1.8.1** | 限流、熔断、降级 |
| Seata | **1.3.0** | 分布式事务（规划/扩展） |
| RocketMQ | **4.6.1** | 消息队列（规划/扩展） |

> Seata、RocketMQ 为技术选型预留能力，具体接入以各业务模块实现为准。

---

## 4. 数据存储与缓存

| 类型 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 关系型数据库 | **MySQL** | **8.0.3** | 业务持久化，驱动 `mysql-connector-java` |
| 缓存 | **Redis** | **5.0.13** | 会话、缓存等，`spring-boot-starter-data-redis` + Jedis |
| 对象存储 | **MinIO** | 7.0.2（客户端） | 文件/对象存储，`io.minio:minio` |
| 连接池 | **Alibaba Druid** | 1.1.14 | 数据库连接池与监控 |
| 持久层 | **MyBatis** + **MyBatis-Plus** | 3.4.2 | ORM 与 CRUD 增强 |

---

## 5. 安全、任务与工具组件

| 类别 | 技术 | 版本（项目内） | 说明 |
|------|------|----------------|------|
| 安全框架 | **Apache Shiro** | 1.4.2 | 认证授权，配合 `shiro-redis` 分布式 Session |
| Token | **java-jwt** (Auth0) | 3.8.2 | JWT 令牌认证 |
| 分布式任务 | **XXL-JOB** | — | 分布式任务调度平台（README 规划） |
| API 文档 | **Swagger (Springfox)** | 2.9.2 | 接口文档与 UI |
| JSON | **Fastjson** | 1.2.46 / 1.2.70 | JSON 序列化 |
| Excel | **EasyExcel** | 2.2.10 | 导入导出 |
| 工具 | **Lombok** | 1.18.6 | 简化样板代码 |
| 校验 | **Hibernate Validator** | 6.1.6.Final | JSR-303/380 参数校验 |

> RBAC 权限模型、认证授权流程及管理接口详见 [RBAC.md](RBAC.md)。

---

## 6. 可观测性与运维

| 能力 | 技术 | 说明 |
|------|------|------|
| 日志分析 | **ELK**（Elasticsearch + Logstash + Kibana） | 集中日志采集与分析 |
| 链路追踪 | **Apache SkyWalking** | 分布式调用链追踪 |
| 服务监控 | **Prometheus + Grafana** | 指标采集与可视化大盘 |

各服务均配置 `logback-spring.xml`，日志输出规范需与 ELK 采集策略对齐。

---

## 7. 项目模块与依赖关系

```
moli-project-distribute/
├── moli-distribute-parent/     # 父 POM，统一版本与依赖管理
├── moli-distribute-common/     # 公共工具、统一响应等
├── moli-gateway/               # API 网关（Gateway + Nacos Discovery）
├── moli-user-center/           # 用户中心
│   ├── moli-user-center-common/
│   ├── moli-user-center-client/  # Feign 客户端
│   └── moli-user-center-server/  # Nacos + Sentinel + Dubbo + Shiro
├── moli-order/                 # 订单服务
│   └── moli-order-server/
└── moli-bi/                    # BI 服务
    └── moli-bi-server/
```

### 各模块主要技术栈

| 模块 | 主要依赖 |
|------|----------|
| moli-gateway | Nacos Discovery、Spring Cloud Gateway |
| moli-user-center-server | Nacos Discovery/Config、Sentinel、Dubbo、MyBatis-Plus、Shiro、Redis、MinIO |
| moli-user-center-client | Nacos Discovery、OpenFeign |
| moli-order-server | Nacos、Sentinel、Dubbo、OpenFeign、MyBatis-Plus |
| moli-bi-server | OpenFeign（调用用户中心） |

---

## 8. 环境要求

### 8.1 开发与构建

| 项 | 要求 |
|----|------|
| JDK | 1.8+ |
| Maven | 3.6+（推荐） |
| IDE | IntelliJ IDEA / Eclipse，Lombok 插件 |

### 8.2 基础设施（建议版本）

| 服务 | 版本 |
|------|------|
| Nacos Server | 2.0.3 |
| MySQL | 8.0.3 |
| Redis | 5.0.13 |
| Sentinel Dashboard | 与 Sentinel 1.8.1 配套 |
| MinIO | 与客户端 7.x API 兼容的版本 |

### 8.3 本地启动前置条件

1. Nacos 已启动，并完成命名空间 / 配置导入（参考各模块 `bootstrap.yml`）。
2. MySQL、Redis 可用，且配置与 Nacos 中数据源一致。
3. 通过网关或直连端口访问各微服务；Dubbo 服务需保证注册中心可达。

---

## 9. 版本兼容性说明

Spring Boot **2.3.12**、Spring Cloud **Hoxton.SR12**、Spring Cloud Alibaba **2.2.7** 为官方验证组合，升级任一项需同步评估其余组件兼容性。

| 组合 | 版本 |
|------|------|
| Boot + Cloud + Alibaba | 2.3.12 + Hoxton.SR12 + 2.2.7 |
| Nacos Client（由 Alibaba BOM 管理） | 适配 Nacos Server 2.0.x |
| Dubbo Spring Cloud | 由 `spring-cloud-starter-dubbo` 统一管理 |

---

## 10. 附录：README 与代码差异说明

`README.md` 中将「服务发现」写为 OpenFeign，实际项目中：

- **服务发现**由 **Nacos Discovery** 承担；
- **OpenFeign** 属于 **HTTP 服务调用** 方式，与 Dubbo 并列使用。

建议以本文档及 `moli-distribute-parent/pom.xml`、各服务 `pom.xml` 为准进行环境搭建与版本对齐。

---

## 11. 参考链接

- [Spring Cloud 官方文档](https://spring.io/projects/spring-cloud)
- [Spring Cloud Alibaba 文档](https://github.com/alibaba/spring-cloud-alibaba/wiki)
- [Nacos 文档](https://nacos.io/docs/latest/what-is-nacos/)
- [Sentinel 文档](https://sentinelguard.io/zh-cn/docs/introduction.html)
- [Apache Dubbo 文档](https://dubbo.apache.org/zh/)
