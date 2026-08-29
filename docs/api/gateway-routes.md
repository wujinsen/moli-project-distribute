# API 网关 · 路由与联调说明

> 模块：`moli-gateway` · 端口：**21000**  
> 配置：`moli-gateway/src/main/resources/application-dev.yml`  
> 架构：[ARCHITECTURE.md](../zh-CN/ARCHITECTURE.md)

---

## 1. 路由总览

![网关路由](../diagrams/png/moli-gateway-routes.png)

> 可编辑源文件：[moli-gateway-routes.drawio](../diagrams/moli-gateway-routes.drawio)

| 路由 id | 网关路径 | 目标服务 | Nacos 服务名 | StripPrefix |
|---------|----------|----------|--------------|-------------|
| `user-center-route` | `/UserCenter/**` | user-center-server | `user-center-server` | **1** |
| `order-route` | `/OrderServer/**` | order-server | `order-server` | **1** |
| `ai-route` | `/AiServer/**` | ai-server | `ai-server` | **1** |
| `knowledge-route` | `/KnowledgeServer/**` | knowledge-server | `knowledge-server` | **1** |
| `aiops-route` | `/AiOpsServer/**` | moli-aiops FastAPI | 固定 URI `:8099`（演示） | **1** |

**StripPrefix=1**：去掉第一段路径再转发。

示例：`GET /UserCenter/login` → 转发到 user-center `GET /login`。

---

## 2. 前端联调基址

meiling-ui 通常配置：

```
VITE_API_BASE_URL = http://{host}:21000
```

| 业务 | 请求前缀 |
|------|----------|
| 用户中心 | `{base}/UserCenter` |
| 订单/秒杀 | `{base}/OrderServer` |
| BI | `{base}/AiServer` |
| 知识库 | `{base}/KnowledgeServer` |

---

## 3. 鉴权头透传

- 客户端：`Authorization: <sessionId>`（user-center 登录返回的 `token`）
- 网关：**不**剥离 Authorization，原样转发
- 业务服务：Shiro Starter 从 Redis 还原 Session

登录接口：`POST {base}/UserCenter/login`

---

## 4. Swagger 经网关访问

| 服务 | Swagger UI（经网关） |
|------|----------------------|
| user-center | `http://localhost:21000/UserCenter/swagger-ui.html` |
| order | `http://localhost:21000/OrderServer/swagger-ui.html` |
| bi | `http://localhost:21000/AiServer/swagger-ui.html` |
| knowledge | `http://localhost:21000/KnowledgeServer/swagger-ui.html` |

调试时在 Swagger **Authorize** 填入登录拿到的 token。

---

## 5. 服务端口速查

| 服务 | 直连端口 | Dubbo |
|------|----------|-------|
| gateway | 21000 | — |
| user-center-server | 8888 | 20881 |
| order-server | 8087 | 20882 |
| ai-server | 1128 | 20883 |
| knowledge-server | 8090 | — |

---

## 6. 连通性探测

```bash
# 网关存活（若配置了 actuator）
curl -I http://127.0.0.1:21000/

# 经网关探测 order
curl http://127.0.0.1:21000/OrderServer/seckill/ping

# 经网关登录
curl -X POST http://127.0.0.1:21000/UserCenter/login \
  -H "Content-Type: application/json" \
  -d '{"userName":"admin","password":"123456"}'
```

详见 [release-smoke-checklist.md](../test/release-smoke-checklist.md)。

---

## 7. 配置说明

- **服务发现**：`lb://{service-name}`，依赖 Nacos 注册
- **小写服务 id**：`discovery.locator.lower-case-service-id: true`
- **Profile**：`dev`（日常）、`loadtest`（压测，见各服务 `application-loadtest.yml`）

---

## 8. 相关

- 部署拓扑：[moli-deploy-topology.drawio](../diagrams/moli-deploy-topology.drawio)
- 模块 README：[moli-gateway/README.md](../../moli-gateway/README.md)
- 本地启动：[wiki-moli/本地启动指南](../../moli-knowledge/kb/wiki-moli/guides/本地启动指南.md)
