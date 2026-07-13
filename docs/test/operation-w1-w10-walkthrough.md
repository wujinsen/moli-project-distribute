# 运营管理 · W1–W10 联合走查清单（给后端联调）

> **更新**：2026-07-13  
> **读者**：user-center 后端、meiling-ui 前端、联调同学  
> **前端状态**：**W1–W10 代码已完工**（meiling-ui [operation-frontend-handoff](../../meiling-ui/docs/api/operation-frontend-handoff.md) §4）  
> **后端状态**：commit **`b4ac176a`**（本地）；`:8888` **`-am install` + 重启** 后满足 2026-07-13 全集（见 [frontend-backend-dependencies.md](../api/frontend-backend-dependencies.md) §8.4①）  
> **环境**：`http://127.0.0.1:5141` · proxy → `8888` · `admin`/`123456` · `VITE_USE_MOCK_AUTH=false`

---

## 0. 结论（给后端一句话）

| 项 | 状态 |
|----|------|
| **前端开发** | ✅ S-VO · DC-2/3 · W7–W10 均已落地；**无待开发阻塞项** |
| **联合走查** | 🟡 后端 API smoke 已通过（§5）；W4/W5/W8/W10 待浏览器与前端共验 |
| **后端需保证** | `toVo()` `*Count` · create 返回 id · `POST /deploy/batch/task` · `POST /task/{id}/cancel` · `ops.upload/deploy.enabled` |

---

## 1. 走查前检查（后端）

| # | 项 | 期望 |
|---|-----|------|
| P0 | user-center `:8888` | **`mvn -pl moli-user-center-server -am install`** 后重启（勿仅跑 server 模块 `spring-boot:run`） |
| P1 | 运维开关 | `ops.upload.enabled=true`（W8）；`ops.deploy.enabled=true`（W9/W10） |
| P2 | 测试数据 | ≥1 项目（可映射 `user-center`/`gateway`/`knowledge`）；≥2 台 **SSH 已配置** 服务器（W8–W10） |
| P3 | 权限 | 联调账号含 `operation:deploy:exec`、`operation:file:upload`、`operation:server:add` 等 |
| P4 | dev 路由 | 大文件上传走 Vite → `8888`，**勿经 Gateway** |
| P5 | 新建服务器 body | 字段 **`ip`**（非 `serverIp`） |

**后端 smoke（可选）**：见 [operation-frontend-handoff.md §5](../api/operation-frontend-handoff.md#5-浏览器走查) · [operation-backend-handoff.md](../api/operation-backend-handoff.md)。

---

## 2. W1–W6 · S-VO 与关联

### W1 · 列表 chips 用 VO 计数

| 项 | 内容 |
|----|------|
| **路径** | `/operation/project` · `/operation/component` · `/operation/server` |
| **通过** | Network 仅 `GET .../list`；**无**连续 `GET .../links`；chips = `*Count` |

### W2 · list 与 `GET /{id}` 一致

| **通过** | 同行 `serverCount` / `componentCount` / `projectCount` 一致 |

### W2b · `serverCount === serverIds.length`

| **通过** | list 与 detail 均成立 |

### W3 · RelationDrawer

| **Network** | `GET /operation/relations/{type}/{id}` |
| **通过** | Tab 有数据；`recentTasks` 可开任务抽屉 |

### W4 · 关联弹窗无幽灵机

| **操作** | `PUT .../links` 只留 1 台 |
| **通过** | chips = 1；`GET /{id}` `serverCount=1` |

### W5 · chips URL 反向过滤

| **通过** | `?serverId=` / `?projectId=` 与列表联动 |

### W6 · 拓扑 + 组件依赖

| 子项 | 路径 | 通过 |
|------|------|------|
| 6a | `/operation/topology` | `GET /operation/topology` 200 |
| 6b | 项目「组件依赖」 | `GET/PUT .../component-links` |

---

## 3. W7–W10 · 部署与任务

### W7 · `POST /operation/server` → id

### W8 · `POST /operation/file/upload` → taskId 轮询

### W9 · `POST /operation/deploy/batch/task`（单父 taskId，日志 `[BATCH]`）

### W10 · `POST /operation/task/{id}/cancel` → `status=cancelled`

> 多机上传/命令仍前端扇出，**不算 W9 失败**。

---

## 4. 建议顺序

```text
W1 → W2 → W2b → W3 → W4 → W5 → W6 → W7 → W8 → W9 → W10
```

---

## 5. 记录表（联调后回填）

| ID | 结果 | 备注 |
|----|------|------|
| W1 | ✅ | 后端 API `b4ac176a` · list `serverCount` |
| W2 | ✅ | list = detail `*Count` |
| W2b | ✅ | `serverCount === serverIds.length` |
| W3 | ✅ | `GET /operation/relations/project/{id}` 200 |
| W4 | 🟡 | 需浏览器 `PUT .../links`（后端契约已验） |
| W5 | 🟡 | API `?serverId=202` 可过滤；与前端共验 chips |
| W6 | ✅ | topology 200 · component-links 200 |
| W7 | ✅ | `POST /server` body **`ip`+`serverName`** → id |
| W8 | 🟡 | 需浏览器 multipart + SSH 目标机 |
| W9 | ✅ | `POST /deploy/batch/task` → 单 taskId |
| W10 | 🟡 | cancel 路由 OK；短任务秒结束 → 10012，需运行中任务与前端共验 |

**走查人**：后端 API smoke　**日期**：2026-07-13　**8888 commit**：`b4ac176a`（文档 commit 待 push）

---

## 6. 相关文档

| 文档 | 用途 |
|------|------|
| [operation-frontend-handoff.md](../api/operation-frontend-handoff.md) | 契约 · TS 片段 |
| [frontend-backend-dependencies.md](../api/frontend-backend-dependencies.md) | 跨模块 · §7 转发 |
| [operation-frontend.md](../api/operation-frontend.md) §10 · §16 | 验收总表 |
| [operation-relations-topology-acceptance.md](operation-relations-topology-acceptance.md) | LC/UI |
| [operation-deploy-center-acceptance.md](operation-deploy-center-acceptance.md) | 部署/SFTP |
