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
| **联合走查** | ✅ **W1–W10 已通过**（API `npm run op:walkthrough` + 浏览器部署中心/任务历史 · 2026-07-13） |
| **后端需保证** | `toVo()` `*Count` · create 返回 id · `POST /deploy/batch/task` · `POST /task/{id}/cancel` · `ops.upload/deploy.enabled` |

---

## 1. 走查前检查（后端）

| # | 项 | 期望 |
|---|-----|------|
| P0 | user-center `:8888` | **`mvn -pl moli-user-center-server -am install`** 后重启（勿仅跑 server 模块 `spring-boot:run`） |
| P1 | 运维开关 | `ops.upload.enabled=true`（W8）；`ops.deploy.enabled=true`（W9/W10） |
| P2 | 测试数据 | ≥1 项目；**W9** 先执行 **`npm run op:seed:w9`**（双机 + SSH 克隆） |
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

| 项 | 内容 |
|----|------|
| **请求** | `multipart/form-data`：`file`、`serverId`、`targetPath`、`postAction`（默认 `none`） |
| **路径** | `targetPath` 须在 `GET /operation/deploy/presets` → `pathPresets` 内（如 `/home/ubuntu/...`）；`/tmp/` → **10012** |
| **目标机** | 须 `sshConfigured=true`（本地 dev：**server 201**） |
| **轮询** | `GET /operation/task/{taskId}?logOffset=`（**无** `/poll` 后缀）至 `finished=true` |
| **通过** | `code=200` 返回 `taskId`；日志含 `[SSH]`、`[SFTP]`；终态 `status=success` |

### W9 · `POST /operation/deploy/batch/task`（单父 taskId，日志 `[BATCH]`）

### W10 · `POST /operation/task/{id}/cancel` → `status=cancelled`

| 项 | 内容 |
|----|------|
| **可取消** | 仅 `pending` / `running` |
| **终态拒绝** | 已 `success` / `failed` / `cancelled` → **10012** `任务已结束，无法取消`（**预期**，非缺陷） |
| **走查技巧** | 用 `POST /operation/command/exec/task` + `sleep 25`（server 201），或 W8 上传运行中点取消；取消后**继续 poll** 至 `status=cancelled` |
| **通过** | `POST .../cancel` → **200**，`status=cancelled`，`finished=true` |

> 多机上传/命令仍前端扇出，**不算 W9 失败**。

---

## 4. 建议顺序

```text
W1 → W2 → W2b → W3 → W4 → W5 → W6 → W7 → W8 → W9 → W10
```

---

## 5. 记录表（联调后回填 · 可转发）

| ID | 结果 | 后端接口 / 备注 |
|----|------|-----------------|
| W1 | ✅ | list `*Count`；`pageNum/pageSize` 分页；无 links 水合（API） |
| W2 | ✅ | list vs `GET /{id}` 同行 `serverCount`/`componentCount` 一致 |
| W2b | ✅ | `serverCount === serverIds.length`（list + detail） |
| W3 | ✅ | `GET /operation/relations/server/{id}` · `recentTasks[]` |
| W4 | ✅ | `PUT .../links` 后 chips/`GET /{id}` 计数同步（已还原） |
| W5 | ✅ | `GET /operation/project/list?serverId=` 反向过滤 |
| W6 | ✅ | topology 12 节点 · component-links GET |
| W7 | ✅ | `POST /operation/server` → snowflake id（测后 DELETE） |
| W8 | ✅ | upload → taskId · poll `finished=true` `status=success` · path=`/opt/moli/frontend/dist/` |
| W9 | ✅ | 双机 `batch/task` · 任务历史 `deploy_batch`「2 步」；远端 restart 可 **失败**（exit 1）不影响走查 |
| W10 | ✅ | `POST /task/{id}/cancel` → `cancelled`；任务历史列表/抽屉展示正常 |

**走查人**：admin · **日期**：2026-07-13 · **8888 构建**：`b4ac176a`（本地）· **meiling-ui**：`:5141`

**自动化**：`npm run op:walkthrough`（日志 `operation-w1-w10-walkthrough.log`）  
**W9 种子**：`npm run op:seed:w9` · SQL 说明见 [`meiling-ui/docs/sql/31_operation_w9_dual_server_seed.sql`](../../meiling-ui/docs/sql/31_operation_w9_dual_server_seed.sql)

**W9 部署中心**：项目 **`w9-batch-smoke`** → 勾选 **201 + w9-smoke-b** → restart → 单次 `POST /operation/deploy/batch/task`。

### 5.1 浏览器补记（2026-07-13）

| 项 | 结果 | 说明 |
|----|------|------|
| 部署中心 | ✅ | 项目下拉显示 `w9-batch-smoke`；双机勾选 → 创建 `deploy_batch` |
| 任务历史 | ✅ | 列表含类型/状态/进度/备注/时间；「查看日志」开抽屉 |
| W9 远端失败 | ⚪ 预期外、走查仍过 | 备注 `远程脚本返回非零退出码: 1`、进度 95% = SSH 已执行但 `moli-service.sh` 未成功；**非前端缺陷** |

> **走查通过标准**：API 契约 + UI 接线；不要求 batch restart 在种子机上业务成功。

### 5.2 W8 / W10 API 复验附录（2026-07-13 午后 · `:8888` 本地）

环境：`admin`/`123456` · `ops.upload.enabled=true` · `ops.command.enabled=true` · 目标机 **server 201**（`sshConfigured=true`）。

#### W8 · 上传 + SSH/SFTP

```http
POST /operation/file/upload
Content-Type: multipart/form-data
  file=<47B txt>
  serverId=201
  targetPath=/home/ubuntu/w8-smoke-133906.txt
  postAction=none
→ 200  taskId=731852137528557568

GET /operation/task/731852137528557568?logOffset=0
→ 200  status=success  finished=true
     logChunk 含 [SSH]、[SFTP] 上传至 /home/ubuntu/w8-smoke-133906.txt
```

| 对照 | 结果 |
|------|------|
| `/tmp/w8-smoke.txt` | **10012** `targetPath 不在允许范围内`（路径白名单校验正常） |
| `/home/ubuntu/...` | **200** → SFTP 成功 |

#### W10 · 运行中取消

```http
POST /operation/command/exec/task
{"serverId":201,"command":"sleep 25","workDir":"/tmp"}
→ 200  taskId=731852047640428544

POST /operation/task/731852047640428544/cancel   （约 800ms 后）
→ 200  status=cancelled  finished=true
```

| 对照 | 结果 |
|------|------|
| 短任务（batch deploy 秒结束）后 cancel | **10012** `任务已结束，无法取消`（契约符合 handoff §任务） |
| `sleep 25` 运行中 cancel | **200** `cancelled` |

浏览器走查见上 §5.1；本附录为后端 API smoke 留档。

---

## 6. 相关文档

| 文档 | 用途 |
|------|------|
| [operation-frontend-handoff.md](../api/operation-frontend-handoff.md) | 契约 · TS 片段 |
| [frontend-backend-dependencies.md](../api/frontend-backend-dependencies.md) | 跨模块 · §7 转发 |
| [operation-frontend.md](../api/operation-frontend.md) §10 · §16 | 验收总表 |
| [operation-relations-topology-acceptance.md](operation-relations-topology-acceptance.md) | LC/UI |
| [operation-deploy-center-acceptance.md](operation-deploy-center-acceptance.md) | 部署/SFTP |
