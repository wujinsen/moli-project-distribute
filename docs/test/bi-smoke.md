# BI 服务冒烟（v1 骨架）

> 模块 **`moli-ai`**，Nacos 名 **`bi-server`**。v1 仅验证注册 + 网关 + 占位接口，见 [moli-v1-release-scope.md](../product/moli-v1-release-scope.md) §3.5。

## 前置

- user-center、gateway 已启动（若走 Shiro 全链路）
- Nacos 可见 `bi-server`

## 用例

| # | 步骤 | 期望 |
|---|------|------|
| B1 | 直连 `GET http://127.0.0.1:1128/demo/test` | `200`，body 含 `test success` |
| B2 | 经网关 `GET http://127.0.0.1:21000/BiServer/demo/test` | 同上（StripPrefix=1） |
| B3 | Nacos 控制台 | 实例健康 |

## 契约

- [bi-api.md](../api/bi-api.md)
- [gateway-routes.md](../api/gateway-routes.md)

## 回归入口

- [release-smoke-checklist.md](release-smoke-checklist.md) — G4 BI 段
