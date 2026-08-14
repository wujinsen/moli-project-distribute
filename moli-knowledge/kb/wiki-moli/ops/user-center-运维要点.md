---


title: user-center-运维要点
slug: user-center-运维要点
type: guide
status: active
tags: [运维, 用户中心, 部署]
sources:
  - moli-user-center/README.md
  - docs/design/user-center-overview.md
related: [用户中心, 本地启动指南, 登录与鉴权指南, 故障排查指南, 数据库初始化指南]
created: 2026-06-25
updated: 2026-06-25
---

# 用户中心 · 运维要点

> 服务名 `user-center-server` · 模块 README：`moli-user-center/README.md`

## 1. 端口与路由

| 项 | 值 |
|----|----|
| HTTP | **8888** |
| Dubbo | **20881** |
| 网关 | `/UserCenter/**` → StripPrefix=1 |
| Swagger | `/UserCenter/swagger-ui.html` |

## 2. 启动顺序

用户中心须在 **order / bi / knowledge 之前**启动（Dubbo Provider + Session 写入方）。网关建议最后启动。

详见 [[本地启动指南]] 与架构文档 §9。

## 3. 关键依赖

| 依赖 | 要求 |
|------|------|
| MySQL | 库 `moli`，基线 `scripts/moli.sql` |
| Redis | **与所有业务服务相同** host/port/password/**database** |
| Nacos | `8848`，namespace `dev`（与各服务 `bootstrap.yml` 一致） |

> 常见故障：order 能启动但 401 — 多为 Redis database 或 password 与用户中心不一致。

## 4. 配置 Profile

| Profile | 文件 | 用途 |
|---------|------|------|
| dev | `application-dev.yml` | 本地开发（默认） |
| test | `application-test.yml` | 测试环境 |
| pre / pro | `application-pre.yml` / `application-pro.yml` | 预发/生产 |
| loadtest | `application-loadtest.yml` | 压测专用接口 |

生产建议：关闭 `swagger.show`；`captcha.enabled=true`；Redis 密码走环境变量/Nacos。

## 5. 日志与审计

| 类型 | 表 / 入口 |
|------|-----------|
| 登录日志 | `sys_login_log`，`GET /log/loginLogList` |
| 操作日志 | `sys_operation_log`，`GET /log/operationLogList` |
| 应用日志 | `logback-spring.xml` |

## 6. 健康检查

- Nacos 控制台可见 `user-center-server` 实例
- Dubbo：业务服务日志无 `No provider available for UserCenterServer`
- HTTP：`GET /UserCenter/captchaImage`（匿名）或登录后 `GET /user/profile`

## 7. 变更注意

| 变更 | 影响 |
|------|------|
| 改 `sys_menu` / `sys_action` | 需重新 enter 系统或调 `/auth/capabilities` |
| 改 Dubbo version/group | 所有消费方同步 |
| 清 Redis Session | 全员掉线，需重新登录 |

## 8. 相关

- [[登录与鉴权指南]]
- [[权限管理操作指南]]
- [[故障排查指南]]
- [[数据库初始化指南]]
- `docs/api/user-center-api-map.md`
