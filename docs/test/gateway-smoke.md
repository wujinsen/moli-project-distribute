# 网关 · 冒烟测试

> 契约：[gateway-routes.md](../api/gateway-routes.md) · 设计：[gateway-design.md](../design/gateway-design.md)  
> 已纳入 [release-smoke-checklist.md](release-smoke-checklist.md) §1；本文展开网关专项。

---

## 1. 前置

- gateway :21000、Nacos、至少 user-center + order 已注册
- 可选：knowledge、bi 同启以测四路由

---

## 2. 路由连通

| # | 请求 | 期望 |
|---|------|------|
| R1 | `GET /OrderServer/seckill/ping` | 200，`pong:true` |
| R2 | `POST /UserCenter/login` | 200，含 `token` |
| R3 | `GET /AiServer/demo/test` | body `test success` |
| R4 | `GET /KnowledgeServer/kb/index?spaceId=900000000000000001` | 200，groups |

---

## 3. StripPrefix

| 网关路径 | 下游等价 |
|----------|----------|
| `/UserCenter/login` | user-center `/login` |
| `/OrderServer/seckill/ping` | order `/seckill/ping` |

直连 8888/8087 与经 21000 响应结构应一致（除网关附加头）。

---

## 4. Authorization 透传

1. 登录拿 `token`
2. `GET /UserCenter/user/list` + `Authorization: {token}`

期望：非 token 失效。

---

## 5. 负例

| # | 场景 | 期望 |
|---|------|------|
| N1 | 错误前缀 `/usercenter/login` | 404 |
| N2 | 下游未启动时访问 | 503 |
| N3 | 无 token 访问受保护接口 | 业务 JSON token 失效 |

---

## 6. Swagger 经网关

打开 `http://localhost:21000/UserCenter/swagger-ui.html`，Authorize 填 token 后可试调接口。

---

## 7. 相关

- [release-smoke-checklist.md](release-smoke-checklist.md) §1
- [moli-gateway/README.md](../../moli-gateway/README.md)
