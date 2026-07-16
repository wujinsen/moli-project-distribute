# Moli Microservices — Tech Stack

**Languages / 语言 / 言語**: [中文](../zh-CN/TECH_STACK.md) | [English](TECH_STACK.md) | [日本語](../ja/TECH_STACK.md)

> This document describes the technology stack, version requirements, and component responsibilities of moli-project-distribute.

---

## 1. Overview

Built on **Spring Cloud + Spring Cloud Alibaba**, covering service discovery, API gateway, config management, circuit breaking, RPC invocation, with MySQL, Redis, and observability components supporting user center, order, and BI modules.

---

## 2. Microservice Architecture

| Capability | Component | Description |
|------------|-----------|-------------|
| Service Discovery | **Nacos Discovery** | Services register to Nacos; gateway routes by service name |
| Config Center | **Nacos Config** | Centralized `bootstrap.yml` / business config with dynamic refresh |
| API Gateway | **Spring Cloud Gateway** | Unified entry (`moli-gateway`) |
| Load Balancing | **Spring Cloud Ribbon** | Client-side LB with Nacos (Hoxton built-in) |
| Circuit Breaking | **Sentinel** | Rate limiting, circuit breaking, degradation |
| Service Invocation | **Spring Cloud Dubbo** | All service-to-service calls use Dubbo RPC; external traffic uses HTTP/REST via gateway |

### Invocation Patterns

- **Service ↔ Service uses Dubbo only**: high-performance binary RPC, not exposed over HTTP, which avoids the risk of internal endpoints being reachable from outside.
  - Provider: user-center `UserServerProvider` (`@DubboService(version="1.0.0", group="moli")`).
  - Consumers: `order-server` / `bi-server` use `@DubboReference` in `ShiroConfig`, then inject into `ShiroRealm` for login authentication.
  - Registry: Dubbo mounts to Nacos via `spring-cloud://`, sharing the Spring Cloud registry.
- **External traffic uses HTTP/REST**: browser / `meiling-ui` → gateway → service controllers, unified `MoliResult<T>` response.
- Note: the earlier OpenFeign example (`UserCenterClient`) has been removed to avoid exposing REST endpoints for internal calls. See `docs/en/ARCHITECTURE.md`.

---

## 3. Core Versions

### 3.1 Spring Ecosystem

| Component | Version | Notes |
|-----------|---------|-------|
| Java | **JDK 1.8** | `maven.compiler.source/target = 8` |
| Spring Boot | **2.3.12.RELEASE** | Parent POM |
| Spring Cloud | **Hoxton.SR12** | Paired with Boot 2.3.x |
| Spring Cloud Alibaba | **2.2.7.RELEASE** | Nacos, Sentinel, Dubbo starters |

### 3.2 Alibaba Middleware (Runtime)

| Component | Version | Purpose |
|-----------|---------|---------|
| Nacos | **2.0.3** | Registry + Config |
| Sentinel | **1.8.1** | Flow control, circuit breaking |
| Seata | **1.3.0** | Distributed transactions (planned) |
| RocketMQ | **4.6.1** | Message queue (planned) |

---

## 4. Data Storage & Cache

| Type | Technology | Version | Notes |
|------|------------|---------|-------|
| RDBMS | **MySQL** | **8.0.3** | `mysql-connector-java` |
| Cache | **Redis** | **5.0.13** | Jedis + Spring Data Redis |
| Object Storage | **MinIO** | 7.0.2 (client) | `io.minio:minio` |
| Connection Pool | **Druid** | 1.1.14 | Pool + monitoring |
| ORM | **MyBatis + MyBatis-Plus** | 3.4.2 | CRUD enhancement |

---

## 5. Security, Jobs & Tools

| Category | Technology | Version | Notes |
|----------|------------|---------|-------|
| Security | **Apache Shiro** | 1.4.2 | Auth with `shiro-redis` distributed Session |
| Token | **java-jwt** | 3.8.2 | JWT authentication |
| Jobs | **XXL-JOB** | — | Planned |
| API Docs | **Swagger (Springfox)** | 2.9.2 | API documentation |
| JSON | **Fastjson** | 1.2.46 / 1.2.70 | Serialization |
| Excel | **EasyExcel** | 2.2.10 | Import/export |
| Utils | **Lombok** | 1.18.6 | Boilerplate reduction |
| Validation | **Hibernate Validator** | 6.1.6.Final | JSR-303/380 |

> RBAC model and auth flow: [RBAC.md](RBAC.md)

---

## 6. Observability

| Capability | Technology | Notes |
|------------|------------|-------|
| Logging | **ELK** | Centralized log analysis |
| Tracing | **SkyWalking** | Distributed tracing |
| Monitoring | **Prometheus + Grafana** | Metrics and dashboards |

---

## 7. Module Dependencies

| Module | Key Dependencies |
|--------|------------------|
| moli-gateway | Nacos Discovery, Spring Cloud Gateway |
| moli-user-center-server | Nacos, Sentinel, Dubbo, MyBatis-Plus, Shiro, Redis, MinIO |
| moli-user-center-client | Nacos Discovery, Spring Cloud Dubbo, `UserCenterServer` contract, Shiro integration |
| moli-order-server | Nacos, Sentinel, Dubbo, MyBatis-Plus, Shiro (client module) |
| moli-ai-server | Nacos, Dubbo, Shiro (client module); application name `bi-server` |

---

## 8. Environment Requirements

| Item | Requirement |
|------|-------------|
| JDK | 1.8+ |
| Maven | 3.6+ |
| Nacos Server | 2.0.3 |
| MySQL | 8.0.3 |
| Redis | 5.0.13 |

**Prerequisites:** Nacos running with namespace/config imported; MySQL and Redis available; Dubbo registry reachable.

---

## 9. Version Compatibility

| Stack | Versions |
|-------|----------|
| Boot + Cloud + Alibaba | 2.3.12 + Hoxton.SR12 + 2.2.7 |
| Nacos Client | Compatible with Nacos Server 2.0.x |
| Dubbo Spring Cloud | Managed by `spring-cloud-starter-dubbo` |

---

## 10. References

- [Spring Cloud](https://spring.io/projects/spring-cloud)
- [Spring Cloud Alibaba](https://github.com/alibaba/spring-cloud-alibaba/wiki)
- [Nacos](https://nacos.io/docs/latest/what-is-nacos/)
- [Sentinel](https://sentinelguard.io/en-us/docs/introduction.html)
- [Apache Dubbo](https://dubbo.apache.org/en/)
