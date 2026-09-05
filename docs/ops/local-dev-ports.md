# 本地开发端口一览（281xx / 285xx）

> 避开 Windows 常见占用：`808x`、`8848`、`8888`、`8090`、`21000` 等。  
> 生产环境可通过 `SERVER_PORT`、`NACOS_SERVER_ADDR` 等环境变量覆盖。

| 组件 | 端口 | 说明 |
|------|------|------|
| **Nacos HTTP** | **28548** | 控制台与注册发现；Docker `28548:8848` |
| **Nacos gRPC** | **29548** | 客户端自动 `HTTP+1000`；Docker `29548:9848` |
| **Gateway** | **28100** | 统一 HTTP 入口 |
| **user-center** | **28101** | Dubbo **28881** |
| **order** | **28102** | Dubbo **28882** |
| **ai** | **28103** | Dubbo **28883** |
| **knowledge** | **28104** | Dubbo **28884** |
| **aiops (FastAPI)** | **28105** | 网关 `/AiOpsServer/**` |

**可观测性（本地 Docker PoC）：**

| 组件 | 端口 |
|------|------|
| Grafana | **28300** |
| Loki API | **28110** |
| Alloy UI | **28111** |
| Prometheus | **29090** |
| Alertmanager | **29093** |
| SkyWalking UI | **28120** |
| SkyWalking OAP gRPC | **28121** |

详见 [`deploy/observability/README.md`](../../deploy/observability/README.md) · [`monitoring-and-logs.md`](monitoring-and-logs.md)

**网关示例：**

```text
http://127.0.0.1:28100/UserCenter/...
http://127.0.0.1:28100/KnowledgeServer/...
```

**Nacos 控制台：** http://127.0.0.1:28548/nacos（`nacos` / `nacos`）

**前端 meiling-ui（Vite `:5141`）** 代理默认连 `28101` / `28104` / `28105`，见 `meiling-ui/vite.config.ts`。

启动 Nacos：`.\scripts\nacos-docker.ps1`

**IDEA 启动（推荐，配置在 Git）：** [idea-local-dev.md](idea-local-dev.md)

相关：[nacos-local-dev.md](nacos-local-dev.md) · [gateway-routes.md](../api/gateway-routes.md)
