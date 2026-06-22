---
title: bi服务
slug: bi服务
type: service
status: active
tags: [微服务, BI, 骨架]
sources:
  - README.zh-CN.md
  - moli-bi/
related: [用户中心, 本地启动指南]
created: 2026-06-22
updated: 2026-06-22
---

# BI 服务（bi-server）

BI 相关服务（**当前为骨架**，预留槽位）。

| 项 | 值 |
|----|----|
| 服务名 | `bi-server` |
| HTTP 端口 | 1128 |
| Dubbo 端口 | 20883（Consumer） |
| 网关路由 | `/BiServer/**` → StripPrefix=1 |

## 现状（实事求是）

- 仅一个 demo 接口 `GET /demo/test` → 返回 `"test success"`。
- `BiApplication` 的 `@MapperScan` 还是**遗留错误包名**（`com.shushan.demo.server.mapper`），无对应 Mapper。
- Web/MySQL/Nacos/Dubbo/Shiro 依赖都已引入但基本未使用。
- 复用 `moli-user-center-shiro-starter` 校验 [[用户中心]] 的 Session。

## 定位

预留的 BI/报表微服务槽位，待接数据源、报表 API 或 OLAP。后续真正开发时应 ingest 其设计文档补充本页。
