---


title: Swagger 接口调试指南
slug: swagger接口调试指南
type: guide
status: active
tags: [swagger, API, 调试, P0]
sources:
  - moli-user-center/moli-user-center-server/src/main/java/com/moli/user/center/server/config/swagger/Swagger2Config.java
  - raw/wujinsen_markdown/插件/swagger/swagger注解.note.md
  - docs/KNOWLEDGE_API.md
related: [登录与鉴权指南, 本地启动指南, 用户中心, 知识库使用指南, 网关]
created: 2026-06-22
updated: 2026-06-22
---

# Swagger 接口调试指南

> 登录拿 token [[登录与鉴权指南]]；知识库 REST 见 [[知识库使用指南]] / `docs/KNOWLEDGE_API.md`。

茉莉使用 **Springfox Swagger 2.9.2**（[[技术栈与版本]]）。Shiro 将 Swagger 静态资源设为 **anon**，但**业务接口仍要 Authorization**。

## 1. 访问地址

| 服务 | 直连 Swagger UI | 经网关（注意 StripPrefix） |
|------|-----------------|----------------------------|
| [[用户中心]] | http://localhost:8888/swagger-ui.html | 网关一般不转发 swagger，**建议直连** |
| [[订单服务]] | http://localhost:8087/swagger-ui.html | 同上 |
| [[知识库服务]] | http://localhost:8090/swagger-ui.html | 同上 |

开关：`application-dev.yml` → `swagger.show: true`（`Swagger2Config` 的 `@Value`）。

## 2. 全局 Authorization 头

`Swagger2Config` 已注入全局参数：

| 参数 | 位置 | 说明 |
|------|------|------|
| `Authorization` | header | 填 `login_token_xxx` |

步骤：

1. `POST /login` 拿 token（Swagger 或 curl，见 [[登录与鉴权指南]]）
2. Swagger 页顶 **Authorize** → 粘贴 token（可带或不带 `Bearer` 前缀，与项目 Filter 一致即可）
3. 调需 `authc` 的接口

## 3. 常用注解（Controller）

| 注解 | 作用 |
|------|------|
| `@Api` | 类说明 |
| `@ApiOperation` | 方法说明 |
| `@ApiModelProperty` | DTO 字段 |

示例见 `LoginController`、`UserController`。

## 4. 与 Shiro 白名单

以下路径 **无需 token**（user-center / Starter 一致）：

- `/login`、`/swagger-ui.html`、`/swagger-resources/**`、`/v2/api-docs`
- `/actuator/**`（若暴露）
- `/loadtest/**`（loadtest profile）
- `/sso/validate`

其余 `/**` → `authc`。

## 5. 网关调试注意

[[网关]] 只做路由转发，**Swagger 通常直连各服务端口**。经 `21000/UserCenter/...` 调 API 时：

- 路径：`/UserCenter/login` → 下游 `/login`
- Header：`Authorization: login_token_xxx`

## 6. 知识库 API

knowledge-server 同样有 `Swagger2Config`；附件、问答等见 [[知识库使用指南]]。经网关 Base：`http://127.0.0.1:21000/KnowledgeServer`。

## 7. 常见问题

| 现象 | 处理 |
|------|------|
| swagger-ui 404 | `swagger.show=false` 或依赖缺失 |
| 接口 10006 | Authorize 未填或 token 过期 |
| 跨服务 401 | Redis 不一致 [[茉莉登录与鉴权故障根因汇总]] |
| 生产环境 | **务必** `swagger.show=false`，勿暴露公网 |

## 8. 与 OpenAPI 3

当前为 Swagger 2（Springfox）。升级 SpringDoc/OpenAPI 3 未在茉莉排期；新模块沿用现有 `Swagger2Config` 模式即可。
