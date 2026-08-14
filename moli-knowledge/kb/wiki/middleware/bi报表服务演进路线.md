---
title: BI 报表服务演进路线
slug: bi报表服务演进路线
type: article
status: active
tags: [BI, 报表, 演进, 设计]
sources:
- moli-ai/moli-ai-server/
- wiki/search/elasticsearch-搜索.md
- wiki/security/字段级数据权限设计.md
related: [字段级数据权限设计, elasticsearch-搜索, mybatis-与-druid持久层, rbac-权限模型]
created: 2026-06-22
updated: 2026-07-05
---

# BI 报表服务演进路线

> 服务实体 ；列级权限 [[security/字段级数据权限设计]]；检索扩展 [[search/elasticsearch-搜索]]。

针对当前 **ai-server 骨架** 的推荐落地顺序（非已实现功能）。

## 阶段 0：修骨架（必做）

| 任务 | 说明 |
|------|------|
| 修正 `@MapperScan` | 改为 `com.moli.ai.server.mapper` 或删除 |
| 对齐持久层 | 引入 MyBatis-Plus + Druid，与用户中心一致 [[database/mybatis-与-druid持久层]] |
| 健康检查 | `/actuator/health` 可选暴露 |

## 阶段 1：只读报表 API

- 只读 MySQL 账号连接 `moli` 库
- 指标 API：分页、时间范围、固定 SQL + 索引 [[database/mysql-索引]]
- 全走 `@RequiresPermissions`，权限走 RBAC [[security/rbac-权限模型]]

## 阶段 2：字段级权限（可选）

按 [[security/字段级数据权限设计]] 三方案之一：

- 应用层 DTO 字段过滤（Interceptor / Jackson 视图）
- 或 SQL 列白名单（维护成本高）

BI 是列级权限的**首要业务场景**。

## 阶段 3：搜索与分析

| 需求 | 技术 |
|------|------|
| 模糊搜日志/文档 | [[search/elasticsearch-搜索]] |
| 大数据导出 | 异步任务 + MinIO（参考 `moli-knowledge/kb/wiki-moli/ops/minio-附件存储指南.md`） |
| 实时大屏 | Redis 缓存热点聚合 [[cache/redis-缓存]] |

## 阶段 4：与全家桶集成

- 网关 `/AiServer/**` 已预留
- SSO 门户可注册 `sys_system`（参考 [[security/sso与系统门户]]）
- 压测 profile 已含 Bi 路由

## 反模式

- 在 BI 内做登录（应禁止，走 Starter）[[security/shiro-starter与跨服务校验]]
- 报表 SQL 无索引全表扫拖垮主库
- 与订单写库共用高权限 DB 账号

## 验收建议

1. 带 token 调 `/AiServer/report/...` 返回 200
2. 无 perm 返回 10009
3. 列权限生效时字段从 JSON 消失
4. Grafana 只看 BI 实例 Druid/HTTP 指标
