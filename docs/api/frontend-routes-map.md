# meiling-ui · 前端路由与后端对照

> **v1 联调索引** · 菜单来源：`GET /UserCenter/menu/getRouters`  
> 网关基址：`VITE_API_BASE_URL`（如 `http://host:21000`）  
> 种子 SQL：`docs/sql/04_knowledge_menu.sql`、`08_kb_ingest_workbench.sql`、`11_kb_wiki_govern_menu.sql`

---

## 1. 网关前缀

| 模块 | HTTP 前缀 |
|------|-----------|
| 用户中心 | `{base}/UserCenter` |
| 知识库 | `{base}/KnowledgeServer` |
| 订单 | `{base}/OrderServer` |
| BI | `{base}/BiServer` |

---

## 2. 系统管理（user-center）

由 `sys_menu` 动态下发（非本文穷举）。登录：

```
POST {base}/UserCenter/login
Authorization: {token}  # 后续请求
```

---

## 3. 企业知识库菜单（sys_menu 900 段）

| menu_id | 名称 | 路由 path | component | perms | 后端 API 域 |
|---------|------|-----------|-----------|-------|-------------|
| 900 | 企业知识库 | `knowledge` | Layout | — | — |
| 901 | 文档浏览 | `knowledge/browse/index` | KnowledgeBrowse | `kb:browse:list` | `/kb/index` `/kb/page` |
| 902 | 智能问答 | `knowledge/ask/index` | KnowledgeAsk | `kb:ask:list` | `/kb/ask` |
| 903 | 关系图谱 | `knowledge/graph/index` | KnowledgeGraph | `kb:graph:list` | `/kb/graph` |
| 904 | 健康体检 | `knowledge/lint/index` | KnowledgeLint | `kb:lint:list` | `/kb/lint` |
| 906 | Ingest 工作台 | `knowledge/ingest/index` | KnowledgeIngest | `kb:ingest:list` | `/kb/ingest/*` |
| 909 | 空间管理 | `knowledge/spaces/index` | KnowledgeSpaces | `kb:space:admin` | `/kb/space` |
| 910 | Wiki 治理 | `knowledge/wiki-govern/index` | KnowledgeWikiGovern | `kb:wiki:govern:list` | `/kb/wiki-moli/lint-space` `/kb/wiki-moli/govern/*` |

**Wiki 单页编辑**：通常从浏览页跳入 `knowledge/wiki/edit`（perms `kb:wiki:edit`，见 KNOWLEDGE_API §8）。

**平台 LLM 设置**：系统管理菜单（`12_kb_platform_llm_menu.sql`），`/kb/platform/llm-config`。

---

## 4. 运营管理菜单（sys_menu 400 段）

| menu_id | 名称 | 路由 path | component | perms | 后端 API 域 |
|---------|------|-----------|-----------|-------|-------------|
| 400 | 运营管理 | `operation` | Layout | — | — |
| 401 | 项目管理 | `operation/project/index` | ProjectManageView | `operation:project:list` | `/operation/project` |
| 402 | 服务器管理 | `operation/server/index` | ServerManageView | `operation:server:list` | `/operation/server` + `topology` / `links` / `check` |
| 403 | 平台管理 | `operation/platform/index` | PlatformManageView | `operation:platform:list` | `/operation/platform` + `secret` |
| 404 | 组件管理 | `operation/component/index` | ComponentManageView | `operation:component:list` | `/operation/component` + `check` / `secret` |
| 405 | 部署中心 | `operation/deploy/index` | DeployCenterView | `operation:server:list` | `/operation/deploy/*`、`/operation/file/upload`、`/operation/command/*`、`/operation/task/*` |
| 406 | 端口矩阵 | `operation/port-matrix/index` | PortMatrixManageView（建议） | `operation:port-matrix:list` | `/operation/port-matrix` CRUD · 设计 [operation-port-matrix-api.md](operation-port-matrix-api.md) |

**跨域权限**：`operation:secret:view`（明文 reveal）、`operation:deploy:exec`（启停）、`operation:file:upload`（上传）、`operation:command:exec`（远程命令 / custom 后置）、`operation:ssh:manage`（SSH 配置）。

**辅助 API**（驾驶舱 / 工具栏 / 部署中心）：

| 端点 | 用途 | 权限 |
|------|------|------|
| `GET /operation/audit/port-matrix` | 端口矩阵校验 | `operation:project:list` |
| `GET /operation/stats` | 驾驶舱 ops KPI | `operation:project:list` |
| `GET /operation/deploy/presets` | 上传路径/后置预设 | `operation:server:list` |
| `GET/POST /operation/deploy/{serviceKey}/...` | 部署进程状态/启停任务 | list / `deploy:exec` |
| `POST /operation/file/upload` | SFTP 上传发布 | `file:upload` + list |
| `POST /operation/command/exec/task` | 远程 shell | `command:exec` + list |
| `GET /operation/task/{id}` | 任务日志轮询 | `operation:server:list` |
| `POST /operation/health/probe-all` | 批量探活 | `operation:server:list` |

**前端对接**：[operation-frontend.md](operation-frontend.md) · **HTTP 契约**：[operation-deploy-api.md](operation-deploy-api.md)

---

## 5. 前端对接文档

| 页面 | 文档 |
|------|------|
| Ingest | [ingest-workbench-frontend.md](ingest-workbench-frontend.md) |
| Wiki 治理 | [wiki-govern-frontend.md](wiki-govern-frontend.md) |
| 总览 | [knowledge-workbench-frontend.md](knowledge-workbench-frontend.md) |
| LLM 设置 | [kb-llm-platform-frontend.md](kb-llm-platform-frontend.md) |
| 运营管理 | [operation-frontend.md](operation-frontend.md) |

---

## 6. v1 前端完成度（后端视角）

| 页面 | 后端 | 前端 |
|------|------|------|
| 浏览/问答/图谱/体检 | ✅ | ✅ |
| Ingest | ✅ | ⚠️ nextSteps / conflicts UI |
| Wiki 编辑 | ✅ | ✅ |
| Wiki 治理 | ✅ | ⚠️ T16f 部分 |
| LLM 设置 | ✅ | 🔵 T19d |
| 运营管理四页 | ✅ | ✅ |

---

## 7. 相关

- [gateway-routes.md](gateway-routes.md)
- [moli-v1-release-scope.md](../product/moli-v1-release-scope.md) §4
- [wiki-moli/前端开发与联调指南](../../moli-knowledge/kb/wiki-moli/guides/前端开发与联调指南.md)
