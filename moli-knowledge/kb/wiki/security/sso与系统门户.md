---
title: SSO 与系统门户
slug: sso与系统门户
type: article
status: active
tags: [SSO, 门户, sys_system, 多系统]
sources:
- docs/sql/03_knowledge_schema.sql
- moli-user-center/moli-user-center-server/src/main/java/com/moli/user/center/server/controller/LoginController.java
- moli-user-center/moli-user-center-server/src/main/java/com/moli/user/center/server/controller/SsoController.java
- raw/wujinsen_markdown/架构/微服务认证/Spring Cloud(四) Spring Cloud Security集成CAS （单点登录）对微服务认证.note.md
related: [shiro-鉴权体系, rbac-权限模型]
created: 2026-06-22
updated: 2026-07-05
---

# SSO 与系统门户

> Shiro Session [[security/认证与会话机制]]；鉴权体系 [[security/shiro-鉴权体系]]。

登录成功后 `LoginVo.systemList` 来自 **`sys_system` + `sys_user_system`**，驱动前端「系统门户」卡片。

## 核心表

| 表 | 作用 |
|----|------|
| `sys_system` | 注册业务系统（code、名称、base_url、图标、sso_mode） |
| `sys_user_system` | 用户可访问哪些系统 |

## sso_mode

| 值 | 含义 | 登录后行为 |
|----|------|------------|
| **INTERNAL** | 内置系统 | 菜单在本前端内切换 |
| **EXTERNAL** | 外跳系统 | 点门户打开 `base_url`（如知识库网关地址） |

示例：`moli-knowledge` id=39，`base_url=http://127.0.0.1:21000/KnowledgeServer`，`sso_mode=EXTERNAL`。

## 登录返回逻辑（简化）

1. 查用户可访问 `systemList`
2. 若**仅 1 个 INTERNAL 系统** → 自动 `enterSystem`，返回默认菜单
3. 若多个 → 前端展示门户，用户选择

## SSO Ticket（外系统服务端）

`POST /sso/validate`（anon）

- 请求体：`{ ticket, systemCode }`
- 可选头：`X-Sso-Secret`（与 `sso.shared-secret` 配置一致）
- Ticket 存 Redis，一次性校验

外系统前端仍建议统一用 **`Authorization: login_token_*`** 调网关 API；Ticket 用于部分服务端互信场景。

## 给用户分配系统

管理端：`UserController#insertUserSystem` 或通过 流程维护 `sys_user_system`。

无分配 → `systemList` 空 → 登录后无门户入口。

## 与网关的关系

门户 `base_url` 通常指向 **Gateway 前缀**（如 `/KnowledgeServer`），不是裸服务端口，保证 Session 头透传。

## 批次#1312 增补（wujinsen P1）

合并 Spring Cloud Security + CAS raw。
