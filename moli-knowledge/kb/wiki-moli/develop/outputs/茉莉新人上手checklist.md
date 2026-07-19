---
title: 茉莉新人上手 checklist
slug: 茉莉新人上手checklist
type: output
status: active
tags: [上手, 综合, P0, onboarding]
query: 新同事第一天如何从零跑通茉莉微服务并完成一次登录调接口？
source_pages: [本地启动指南, 数据库初始化指南, 登录与鉴权指南, 故障排查指南, 知识库使用指南, 茉莉微服务全链路一张图]
sources:
  - wiki-moli/guides/本地启动指南.md
  - wiki-moli/guides/数据库初始化指南.md
  - wiki-moli/guides/登录与鉴权指南.md
  - wiki-moli/guides/前端开发与联调指南.md
  - wiki-moli/guides/知识库使用指南.md
  - wiki-moli/guides/故障排查指南.md
  - wiki-moli/develop/outputs/茉莉微服务全链路一张图.md
# GraphRAG：勿链「项目文档总览」枢纽（曾致 M26 图扩跳挤出 expect）；只链 Day-1 主路径页
related: [本地启动指南, 数据库初始化指南, 登录与鉴权指南, 茉莉微服务全链路一张图]
created: 2026-06-22
updated: 2026-07-19
---

# 茉莉新人上手 checklist

> **Query crystallize**：综合 [[本地启动指南]]、[[数据库初始化指南]]、[[登录与鉴权指南]] 等，供 Day-1 自检。细节以源页为准。

## 0. 你要达成什么

-  本机 **Nacos + MySQL + Redis** 已运行
-  **user-center → order（可选 bi/knowledge）→ gateway** 启动成功
-  能 **登录拿 token**，经网关调通至少一个业务接口
-  （可选）前端 devServer 联调或 Swagger 调试
-  （可选）浏览 wiki / 跑 sync 后看 KnowledgeServer

全链路图见 [[茉莉微服务全链路一张图]]。

---

## 1. 环境准备（约 30–60 分钟）

| # | 动作 | 参考 |
|---|------|------|
| 1 | 安装 JDK 8、Maven 3.6+ | [[本地启动指南]] §1 |
| 2 | 启动 Nacos `8848`，命名空间 `dev` | [[本地启动指南]] §2 |
| 3 | 建库 `moli`，执行 `scripts/init-db.ps1` 或手动导入 | [[数据库初始化指南]] |
| 4 | 启动 Redis `6379` | [[本地启动指南]] §2 |
| 5 | 父 POM + common `mvn install` | [[本地启动指南]] §4 |

**秒杀/压测**不需要时可跳过 `02_seckill_schema`；**知识库 Web** 需要 `03_knowledge_schema.sql`（init 脚本已含）。

---

## 2. 配置核对（必做）

-  各服务 `bootstrap.yml` → Nacos 地址与 **namespace=dev**
-  各服务 `application-dev.yml` → **同一 MySQL**、**同一 Redis**（host/port/**database**/password 全一致）
-  user-center 端口 **8888**（非旧文档 1127）

Redis 不一致 → 跨服务 **401**，见 [[茉莉登录与鉴权故障根因汇总]]。

---

## 3. 启动服务（严格顺序）

1.  [[用户中心]] `:8888`
2.  [[订单服务]] `:8087`（可选 [[bi服务]] `:1128`、[[知识库服务]] `:8090`）
3.  [[网关]] `:21000` **最后**

验证 Nacos 控制台可见各服务注册；失败见 [[故障排查指南]]「No provider / 注册不上」。

---

## 4. 第一次登录 + 调 API

| # | 动作 | 参考 |
|---|------|------|
| 1 | `POST http://localhost:21000/UserCenter/login/...`（演示账号见 [[登录与鉴权指南]]） | [[登录与鉴权指南]] |
| 2 | 复制响应中的 token | |
| 3 | 请求业务接口时加 `Authorization: <token>` | |
| 4 | 或用 Swagger：`http://localhost:8888/swagger-ui.html` 等 | `guides/swagger接口调试指南` |

网关 **不验 token**，鉴权在业务服务 Shiro 层；路由见 [[网关]]。

---

## 5. 前端联调（可选）

-  Node 14+，`npm install && npm run dev`
-  `vue.config.js` proxy 指向 `21000` 或配网关 CORS
- 见 `guides/前端开发与联调指南`、[[茉莉-gateway-cors]]（延伸联调，非 Day-1 主链）

---

## 6. 知识库（可选）

| # | 动作 | 参考 |
|---|------|------|
| 1 | 启动 knowledge-server + 网关 `/KnowledgeServer` | [[知识库使用指南]] |
| 2 | `python moli-knowledge/kb/tools/sync_to_db.py --dry-run` | [[知识库使用指南]] |
| 3 | 写库后 `GET /kb/page?slug=guides/本地启动指南` | |
| 4 | 本地 Viewer：`python kb/tools/serve.py` → `:8765` | [[知识库使用指南]] |

---

## 7. 踩坑速查

| 现象 | 先看 |
|------|------|
| 登录 500 | Redis 未起 / 连错 [[故障排查指南]] |
| 401 跨服务 | Redis database 不一致 [[茉莉登录与鉴权故障根因汇总]] |
| Dubbo No provider | Nacos、启动顺序 [[本地启动指南]] / [[故障排查指南]] |
| 浏览器 CORS | 网关 CORS 或 devServer proxy [[茉莉-gateway-cors]] |

---

## 8. 源页索引

[[本地启动指南]] · [[数据库初始化指南]] · [[登录与鉴权指南]] · [[故障排查指南]] · [[知识库使用指南]] · [[茉莉微服务全链路一张图]]
