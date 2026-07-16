# Moli Microservices (moli-project-distribute)

**Languages / 语言 / 言語**: [中文](README.md) | [English](README.en.md) | [日本語](README.ja.md)

[![Java](https://img.shields.io/badge/Java-1.8-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.3.12-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-Hoxton.SR12-blue.svg)](https://spring.io/projects/spring-cloud)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

[![RAG](https://img.shields.io/badge/RAG-Retrieval%2BLLM-8A2BE2.svg)](moli-knowledge/README.md)
[![LLM](https://img.shields.io/badge/LLM-OpenAI%20Compatible-412991.svg)](moli-knowledge/moli-knowledge-server/src/main/java/com/moli/knowledge/server/service/KbLlmClient.java)
[![Agentic Coding](https://img.shields.io/badge/Agentic%20Coding-AGENTS%2BSkills-000000.svg)](AGENTS.md)
[![Python](https://img.shields.io/badge/Python-KB%20Tooling-3776AB.svg)](moli-knowledge/kb/tools)

> 🤖 **In one line**: an open-source system combining a **Spring Cloud microservices backend** with an **enterprise-grade LLM knowledge base** — production backend × RAG Q&A × retrieval evaluation × Agentic Coding.

## ✨ AI Highlights (quick read)

![Knowledge base architecture](docs/diagrams/png/moli-kb-architecture.png)

| Capability | What's built |
|------------|--------------|
| 🔎 **RAG Q&A** | Chunk splitting → retrieval (`/kb/ask`) → **LLM generative answers with citations**; OpenAI-compatible client (DB config first + yaml fallback + call logging) |
| 📊 **Retrieval eval** | `golden.jsonl` → **hit@k / MRR / coverage**, retrieval-only vs generative comparison |
| 🧠 **LLM-Wiki governance** | Ingest / Lint / Enrich; knowledge "compiled once, kept fresh"; one-way incremental idempotent sync |
| 🤝 **Agentic Coding** | Layered `AGENTS.md` rules + custom Cursor Skills (diagrams / SQL migration / KB ingest), a reusable AI dev workflow |
| 🏗️ **Production backend** | Spring Cloud Alibaba (Nacos / Dubbo / Sentinel / Gateway) + Shiro distributed auth + GitHub Actions CI |

**📈 Key metrics** (auto-filled by `kb/tools/fill_eval_metrics.py`):
<!-- KB_METRICS:START -->
hit@1 `66.7%` | hit@3 `100.0%` | hit@8 `100.0%` | MRR `0.833` | coverage `100.0%` | avg latency `0.47s` | `12` questions
<!-- KB_METRICS:END -->

**🎬 Demo**
- Live demo: `<link after deploy>`
- Demo GIF: `docs/portfolio/kb-demo.gif` `<to record>`
- 30-sec local run (zero deps): `python moli-knowledge/kb/tools/serve.py` → http://127.0.0.1:8765

**📄 One-pager**: see [PORTFOLIO.md](PORTFOLIO.md)

---

## Introduction

**Moli Microservices** (moli-project-distribute) is a distributed microservices sample built on **Spring Cloud + Spring Cloud Alibaba**. It covers API gateway, service discovery, configuration management, RPC/HTTP invocation, authentication & authorization, and data persistence.

The user center serves as the core foundation, with order and BI modules built on top. Suitable for learning Spring Cloud, secondary development, or as a project scaffold.

### Key Features

- **Unified API Gateway** — Spring Cloud Gateway with path-based routing
- **Registry & Config** — Nacos for discovery and centralized configuration
- **Service Invocation** — External HTTP/REST via Gateway; inter-service Spring Cloud Dubbo RPC
- **Traffic Protection** — Sentinel circuit breaking, degradation, and rate limiting
- **Security** — Apache Shiro + Redis distributed Session + JWT
- **Data Layer** — MySQL + MyBatis-Plus + Druid connection pool
- **Extensibility** — Planned Seata, RocketMQ, XXL-JOB, ELK, SkyWalking, Prometheus + Grafana

---

## Project Structure

```
moli-project-distribute/
├── moli-distribute-parent/       # Parent POM, dependency management
├── moli-distribute-common/       # Shared utilities, unified response
├── moli-gateway/                 # API Gateway
├── moli-user-center/             # User Center
│   ├── moli-user-center-common/
│   ├── moli-user-center-client/  # Dubbo contract + Shiro integration for order/bi
│   └── moli-user-center-server/  # Shiro, Dubbo Provider
├── moli-order/
│   └── moli-order-server/
├── moli-ai/                      # BI (Nacos: ai-server)
│   └── moli-ai-server/
├── moli-knowledge/
│   └── moli-knowledge-server/
└── docs/                         # See docs/README.md
    ├── product/ design/ api/ test/ ops/ sql/
    └── zh-CN/ en/ ja/
```

### Services

| Module | Service Name | Default Port | Description |
|--------|--------------|--------------|-------------|
| moli-gateway | `moli-gateway` | 21000 | Unified API Gateway |
| moli-user-center-server | `user-center-server` | **8888** | Users, roles, menus, dictionaries |
| moli-order-server | `order-server` | 8087 | Orders (incl. seckill); Dubbo to user center |
| moli-ai-server | `ai-server` | 1128 | BI skeleton (v1 placeholder) |
| moli-knowledge-server | `knowledge-server` | see module README | Knowledge base / Ingest / Ask |

### Gateway Routes

| Route Prefix | Target Service |
|--------------|----------------|
| `/UserCenter/**` | `lb://user-center-server` |
| `/OrderServer/**` | `lb://order-server` |
| `/AiServer/**` | `lb://ai-server` |
| `/KnowledgeServer/**` | `lb://knowledge-server` |

> See [docs/api/gateway-routes.md](docs/api/gateway-routes.md).

---

## Tech Stack

| Capability | Technology |
|------------|------------|
| Service Discovery | Spring Cloud Alibaba Nacos Discovery |
| Config Center | Spring Cloud Alibaba Nacos Config |
| API Gateway | Spring Cloud Gateway |
| Load Balancing | Spring Cloud Ribbon |
| Circuit Breaking | Spring Cloud Alibaba Sentinel |
| Service Invocation | External HTTP/REST (Gateway) + inter-service Spring Cloud Dubbo |
| Database | MySQL |
| Cache | Redis |
| Object Storage | MinIO |
| Security | Apache Shiro |
| Job Scheduling | XXL-JOB (planned) |
| Connection Pool | Alibaba Druid |
| Persistence | MyBatis + MyBatis-Plus |
| Logging | ELK |
| Tracing | SkyWalking |
| Monitoring | Prometheus + Grafana |

### Core Versions

| Component | Version |
|-----------|---------|
| JDK | 1.8 |
| Spring Boot | 2.3.12.RELEASE |
| Spring Cloud | Hoxton.SR12 |
| Spring Cloud Alibaba | 2.2.7.RELEASE |
| Nacos | 2.0.3 |
| Sentinel | 1.8.1 |
| MySQL | 8.0.3 |
| Redis | 5.0.13 |

> See [docs/en/TECH_STACK.md](docs/en/TECH_STACK.md) for the full version matrix.

---

## Requirements

| Dependency | Recommended Version |
|------------|---------------------|
| JDK | 1.8+ |
| Maven | 3.6+ |
| Nacos Server | 2.0.3 |
| MySQL | 8.0.3 |
| Redis | 5.0.13 |

---

## Quick Start

### 1. Clone

```bash
git clone git@github.com:wujinsen/moli-project-distribute.git
cd moli-project-distribute
```

### 2. Start Infrastructure

1. Start **Nacos** (default `http://127.0.0.1:8848`)
2. Start **MySQL**, create database (e.g. `moli`) and import scripts if any
3. Start **Redis**

### 3. Configure

Edit `bootstrap.yml` and `application-dev.yml` in each module for Nacos, MySQL, and Redis settings.

### 4. Build

```bash
cd moli-distribute-parent && mvn clean install -DskipTests
cd ../moli-distribute-common && mvn clean install -DskipTests
cd ../moli-user-center && mvn clean package -DskipTests
```

### 5. Start Services

1. `moli-user-center-server`
2. `moli-order-server`
3. `moli-ai-server` (optional, v1 skeleton)
4. `moli-knowledge-server` (optional)
5. `moli-gateway`

Access via gateway:

```
http://localhost:21000/UserCenter/...
http://localhost:21000/OrderServer/...
http://localhost:21000/AiServer/...
http://localhost:21000/KnowledgeServer/...
```

---

## Configuration

- **Profiles**: `spring.profiles.active` in `application.yml` (`dev` / `test` / `pre`)
- **Nacos namespace**: per environment in `bootstrap.yml`
- **Dubbo ports**: user center `20881`, order `20882`

---

## RBAC Design

The user center uses **RBAC (Role-Based Access Control)** with Apache Shiro and Redis distributed Session.

### Model

```
User (SysUser) ──N:N──▶ Role (SysRole) ──N:N──▶ Menu (SysMenu)
                                                      │
                                              perms (button permission)
```

| Concept | Description |
|---------|-------------|
| User | Login account; bound to roles via `sys_user_role` |
| Role | Permission carrier; bound to menus via `sys_role_menu` |
| Menu | Directory (M), page (C), button (F); `perms` defines API permissions |
| Department | Org structure (`SysDept`); independent from role authorization |

### Auth Flow

- **Login**: `POST /login` → Shiro validates password → returns `token` + user + menu tree
- **Menu auth**: menus aggregated by user roles; username `admin` gets all menus
- **API permissions**: format `sys:module:action` (e.g. `sys:user:create`); Shiro annotations reserved
- **Cross-service**: `moli-user-center-client` module; Dubbo user lookup + shared Redis session

| Module | Path Prefix | Capabilities |
|--------|-------------|--------------|
| User | `/user` | User CRUD, role assignment |
| Role | `/role` | Role CRUD, menu authorization |
| Menu | `/menu` | Menu CRUD, dynamic routes |
| Dept | `/dept` | Department CRUD |

> Full design: [docs/en/RBAC.md](docs/en/RBAC.md)

---

## Documentation

- [Docs hub](docs/README.md) · [Documentation audit](docs/DOCUMENTATION_AUDIT.md)
- [Architecture / Invocation / Auth (EN)](docs/en/ARCHITECTURE.md)
- [Tech Stack (EN)](docs/en/TECH_STACK.md)
- [RBAC Design (EN)](docs/en/RBAC.md)
- [Tech Stack (中文)](docs/zh-CN/TECH_STACK.md)
- [RBAC (中文)](docs/zh-CN/RBAC.md)
- [Tech Stack (日本語)](docs/ja/TECH_STACK.md)
- [RBAC (日本語)](docs/ja/RBAC.md)

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/xxx`)
3. Commit changes (`git commit -m 'Add xxx'`)
4. Push the branch (`git push origin feature/xxx`)
5. Open a Pull Request

---

## License

Licensed under [Apache License 2.0](LICENSE).

Copyright 2026 wujinsen

---

## Author

- **wujinsen** — [GitHub](https://github.com/wujinsen)

Feedback and issues are welcome via GitHub Issues.
