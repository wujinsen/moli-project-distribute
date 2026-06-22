---
title: loadtest Profile 与压测登录
slug: loadtest-profile与压测登录
type: article
status: active
tags: [压测, loadtest, user-center, Shiro]
sources:
  - moli-user-center/moli-user-center-server/src/main/java/com/moli/user/center/server/loadtest/LoadtestLoginController.java
  - load-test/README.md
  - moli-user-center/moli-user-center-server/src/main/resources/application-loadtest.yml
related: [秒杀压测指南, 认证与会话机制, 登录与鉴权指南, 用户中心]
created: 2026-06-22
updated: 2026-06-22
---

# loadtest Profile 与压测登录

> 操作手册 [[秒杀压测指南]]；产品登录 [[登录与鉴权指南]]；Session 机制 [[认证与会话机制]]。

## 为什么单独压测登录？

产品接口 `POST /login` 会：

- 写登录日志
- 查菜单、门户、权限列表
- 返回完整 `LoginVo`

k6 高并发下这些 IO 成为瓶颈，**不能代表**秒杀链路的登录吞吐。

## loadtest 专用接口

| 项 | 值 |
|----|-----|
| Profile | `@Profile("loadtest")` 才注册 |
| 路径 | `POST /loadtest/login`（网关：`/UserCenter/loadtest/login`） |
| Shiro | `/loadtest/**` 为 `anon`，Controller 内手动 `Subject.login` |
| 返回 | 精简 token + user，**无菜单/门户** |

源码：`LoadtestLoginController.java`（[[用户中心]] 模块）。

## 与产品登录的关系

- **同一套** Shiro + Redis Session：token 仍是 `login_token_*`，下游 `Authorization` 用法不变
- 压测脚本拿到 token 后，可调 `/user/list` 等需 `authc` 的接口
- **禁止**在生产 profile 暴露 `/loadtest/**`（仅 loadtest 环境启用）

## 各服务 loadtest 差异（要点）

| 服务 | loadtest 特有点 |
|------|-----------------|
| user-center | LoadtestLoginController、Prometheus、登录日志可关 |
| gateway | 路由可能不含 Knowledge；Prometheus |
| order | Redis database 常为 **1**；`load-test-mode` 允许 admin init |

具体 YAML 以各模块 `application-loadtest.yml` 为准。

## k6 配置提示

```javascript
// 环境变量见 load-test/k6/lib/config.js
// LOGIN_PASSWORD、BASE_URL=http://localhost:21000
```

混合脚本 `mixed-login-seckill.js`：先 loadtest 登录再带 token 秒杀。

## 排查

- 404 on `/loadtest/login` → user-center **未启 loadtest profile**
- 401 on 业务 API → token 未带或 Redis 不一致
- 见 [[故障排查指南]]
