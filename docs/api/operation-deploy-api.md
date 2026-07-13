# 运维部署中心 · HTTP API 契约（SVR-13 ~ SVR-20）

> **读者**：前端联调、测试、运维；**后端实现**：`moli-user-center-server` `com.moli.user.center.server.operation.*`  
> **前端对接摘要**：[operation-frontend.md](operation-frontend.md) §11  
> **全量索引**：[user-center-api-map.md](user-center-api-map.md) §4  
> **验收**：[operation-deploy-center-acceptance.md](../test/operation-deploy-center-acceptance.md)

统一响应：`MoliResult<T>` → `{ "code": 200, "data": T, "msg": "..." }`；失败 `code` 非 200，`msg` 为业务错误说明。

---

## 1. 数据库与权限

### 1.1 SQL 迁移（按序）

| 顺序 | 脚本 | 内容 |
|------|------|------|
| 1 | `docs/sql/21_operation_ssh_deploy.sql` | SSH 字段、`operation_task` 表、菜单 405、上传/SSH/部署权限 |
| 2 | `docs/sql/22_operation_command_flex.sql` | `upload_allowed_roots` 列、`operation:command:exec` |

### 1.2 权限码

| perm_code | 用途 |
|-----------|------|
| `operation:server:list` | 部署中心菜单、读 status/presets、任务轮询 |
| `operation:ssh:manage` | `PUT /operation/server/{id}/ssh`、测试 SSH |
| `operation:deploy:exec` | 异步启停 `.../task`、上传后置 `restartService:*` |
| `operation:file:upload` | `POST /operation/file/upload` |
| `operation:command:exec` | `POST /operation/command/exec/task`、上传 `postAction=custom` |

---

## 2. 服务端配置

`moli-user-center-server` `application.yml`（生产用环境变量覆盖）：

```yaml
ops:
  secret:
    key: ${OPS_SECRET_KEY:}          # SSH/凭据 AES，必填
  deploy:
    enabled: ${OPS_DEPLOY_ENABLED:false}
    deploy-root: ${OPS_DEPLOY_ROOT:/opt/moli-project-distribute}
    status-sync-mode: ${OPS_DEPLOY_STATUS_SYNC_MODE:ssh}   # local | ssh | off（定时 deploy_running 同步）
    allow-local: ${OPS_DEPLOY_ALLOW_LOCAL:false}           # serverId 为空时是否允许本机 moli-service.sh
    services:                          # serviceKey 注册表（见 §6 presets.serviceKeys）
      - key: user-center
        label: 用户中心
        aliases: [user-center, moli-user-center, user-center-server, moli-server]
      - key: gateway
        label: 网关
        aliases: [gateway, moli-gateway]
      - key: knowledge
        label: 知识库
        aliases: [knowledge, moli-knowledge, knowledge-server]
      - key: order
        label: 订单服务
        aliases: [order, moli-order]
      - key: bi
        label: BI 服务
        aliases: [bi, moli-bi]
  health:
    probe-enabled: ${OPS_HEALTH_PROBE_ENABLED:false}
    probe-cron: ${OPS_HEALTH_PROBE_CRON:0 */15 * * * ?}
    probe-parallelism: ${OPS_HEALTH_PROBE_PARALLELISM:8}
    probe-timeout-seconds: ${OPS_HEALTH_PROBE_TIMEOUT_SECONDS:120}
  upload:
    enabled: ${OPS_UPLOAD_ENABLED:false}
    allowed-paths: ${OPS_UPLOAD_ALLOWED_PATHS:/opt/moli/frontend/,/opt/moli-project-distribute/}
    max-bytes: ${OPS_UPLOAD_MAX_BYTES:209715200}
  command:
    enabled: ${OPS_COMMAND_ENABLED:false}
    max-chars: ${OPS_COMMAND_MAX_CHARS:8192}
    default-work-dir: ${OPS_COMMAND_DEFAULT_WORK_DIR:/opt/moli-project-distribute}
```

**serviceKey 说明**：`order` / `bi` 与端口矩阵别名一致，会出现在 `GET .../presets` 的 `serviceKeys`；远程启停依赖目标机 `deploy/linux/moli-service.sh`（已支持 `user-center` · `gateway` · `knowledge` · **`order`** · **`bi`**）。

| 开关 | 影响 |
|------|------|
| `ops.deploy.enabled=false` | 拒绝 `start`/`stop`/`restart` 及上传后置 `restartService:*` |
| `ops.deploy.allow-local=false`（**默认**） | `serverId` 为空时拒绝 status/execute/task 本机回退，返回 **10109** |
| `ops.deploy.status-sync-mode=ssh` | 定时探活同步 `deploy_running` 时走 SSH（推荐生产） |
| `ops.deploy.status-sync-mode=local` | 同步时走本机脚本（仅 Linux 开发机） |
| `ops.deploy.status-sync-mode=off` | 不同步 `deploy_running` |
| `ops.upload.enabled=false` | 拒绝文件上传 |
| `ops.command.enabled=false` | 拒绝远程命令、上传 `postAction=custom` |
| `ops.health.probe-enabled=false` | 关闭定时批量探活调度器 |

### 2.1 本地 dev 推荐片段（`application-dev.yml`）

部署中心联调时三开关默认 **false**，需在 profile `dev` 显式打开（`application.yml` 主配置仍为 false，生产安全）：

```yaml
ops:
  secret:
    key: your-local-dev-secret    # 或 OPS_SECRET_KEY
  deploy:
    enabled: true
    allow-local: true             # 仅本地；无 serverId 时允许本机 moli-service.sh
  upload:
    enabled: true
  command:
    enabled: true
```

注意：`application-dev.yml` 中 `spring.servlet.multipart` 默认为 **10MB**；大于 10MB 的 zip 需调大或走 `OPS_UPLOAD_MAX_FILE_SIZE`。

### 2.2 常见联调错误

| 浏览器/接口现象 | 原因 | 处理 |
|-----------------|------|------|
| `Failed to fetch`（上传） | 经 Gateway 传大 multipart、或服务未启 | dev 用 vite proxy → `:8888`；确认 user-center 进程 |
| `文件上传发布未启用` | `ops.upload.enabled=false` | §2.1 或 `OPS_UPLOAD_ENABLED=true` |
| `远程命令执行未启用` | `ops.command.enabled=false` | §2.1 或 `OPS_COMMAND_ENABLED=true` |
| `serverIp 与 serverId 不一致`（旧版） | 换关联服务器后行内旧 IP | **2026-07-13 起** 后端以 `serverId` 覆盖 IP；升级后重试 |
| 关联 1 台显示 2 台（旧版） | 主表与 N:N 不同步 | **2026-07-13 起** 保存 links 同步主表；或重新点「确定关联」 |

---

## 3. SSH 凭据（SVR-13）

### `PUT /operation/server/{id}/ssh`

- **权限**：`operation:ssh:manage`
- **说明**：私钥/口令 AES 加密存储，**只写不读**；留空字段表示不修改已存值。

**请求体** `OperationServerSshVo`：

```json
{
  "sshPort": 22,
  "sshUser": "ubuntu",
  "sshAuthType": 1,
  "connPref": "auto",
  "privateKey": "-----BEGIN RSA PRIVATE KEY-----\n...",
  "passphrase": "",
  "uploadAllowedRoots": "/opt/moli-project-distribute/\n/home/ubuntu/app/"
}
```

| 字段 | 说明 |
|------|------|
| `connPref` | `auto`（内网 IP 优先）· `inner` · `public` |
| `uploadAllowedRoots` | 该服务器允许上传的路径前缀，逗号/换行/分号分隔（SVR-19） |

**响应**：`data: true`

### `POST /operation/server/{id}/ssh/test`

- **权限**：`operation:ssh:manage`
- **响应** `OperationSshTestVo`：`success`、`host`、`output`（whoami/hostname）、`elapsedMs`、`message`

### `GET /operation/server/{id}` 增量字段

`OperationServerVo` 含 `sshConfigured`、`sshPort`、`sshUser`、`connPref`、`uploadAllowedRoots`（不含私钥）。

---

## 4. 异步任务（SVR-14）

### `GET /operation/task/{id}?logOffset={n}`

- **权限**：`operation:server:list`
- **响应** `OperationTaskVo`：

| 字段 | 说明 |
|------|------|
| `serverId` | 目标服务器 |
| `projectId` | 关联项目（deploy 可选） |
| `status` | `pending` / `running` / `success` / `failed` / **`cancelled`** |
| `progress` | 0–100 |
| `logChunk` | 自 `logOffset` 起的增量日志 |
| `nextLogOffset` | 下次轮询传入 |
| `finished` | 是否终态（含 `cancelled`） |

### `POST /operation/task/{id}/cancel`

- **权限**：`operation:server:list`（与轮询一致）
- **行为**：`pending` / `running` **协作式取消**；`success` / `failed` / `cancelled` 返回错误「任务已结束，无法取消」
- **响应**：`OperationTaskVo`（取消请求后的快照；运行中任务可能稍后才是终态 `cancelled`）
- **说明**：
  - 批量 deploy / 探活等在**步骤间隙**响应取消；单次 SSH 须等当前命令结束后才退出
  - 日志含 `[CANCEL]` 行；`message` 为「用户取消」

### `GET /operation/task/groups`

- **权限**：`operation:server:list`
- **查询**：`taskType`、`serverId`、**`projectId`**、**`status`**、`pageNum`、`pageSize`、**`tasksPerGroup`**（默认 20）
- **说明**：分页维度为**项目组**；`total` 为组数；组内 `tasks` 按 `createTime` 降序，条数受 `tasksPerGroup` 限制；`projectId=null` 为「未关联项目」组

### `GET /operation/task/list`

- **权限**：`operation:server:list`
- **查询**：`taskType`（`deploy` / `deploy_batch` / `upload` / `command` / `health_probe`）、`serverId`、**`projectId`**、`pageNum`、`pageSize`
- **说明**：列表**不含** `task_log` 大字段

### `GET /operation/task/{id}/poll?logOffset={n}`

- **权限**：`operation:server:list`
- **说明**：与 `GET /operation/task/{id}` 等价别名，供前端轮询习惯使用

---

## 4.1 批量探活（SVR-12 · Phase R3 异步）

### `POST /operation/health/probe-all`

- **权限**：`operation:server:list`
- **行为（Breaking）**：**不再**同步返回探活统计；创建 `taskType=health_probe` 任务后立即返回 `taskId`
- **响应**：`data: taskId`（Long）
- **轮询**：`GET /operation/task/{taskId}?logOffset=0`（或 `/poll`），直至 `finished=true`
- **任务日志**：含并行 TCP 探活、批量写库、`deploy_running` SSH 同步摘要

定时调度器仍内部调用同步 `probeAll()`；仅 HTTP 入口异步化。

配置：`ops.health.probe-parallelism`（默认 8）、`ops.health.probe-timeout-seconds`（默认 120）。

---

## 5. 远程启停（SVR-15）

### `GET /operation/deploy/{serviceKey}/status?serverId=`

- **权限**：`operation:server:list`
- **serviceKey**：见 `ops.deploy.services` / `GET .../presets` 的 `serviceKeys`（默认 `user-center` · `gateway` · `knowledge`）
- **serverId 非空**：SSH 远程只读；脚本缺失时自动 SFTP 上传（与异步 deploy 一致）
- **serverId 空**：仅当 `ops.deploy.allow-local=true` 时本机执行 `moli-service.sh status`；否则 **10109**

### `POST /operation/deploy/{serviceKey}/{action}?serverId=&arg=`

- **权限**：`operation:deploy:exec` + `operation:server:list`（同步执行，少用）
- **serverId 规则**：同 status

### `POST /operation/deploy/{serviceKey}/{action}/task?serverId=&projectId=`

- **权限**：`operation:deploy:exec` + `operation:server:list`
- **action**：`start` · `stop` · `restart`
- **projectId**（可选）：关联 `operation_project_deploy_info.id`；须与 `serviceKey` 映射一致；若项目已绑 `serverId` 则自动回填/校验
- **响应**：`data: taskId`（Long）；任务表写入 `project_id`

### `POST /operation/deploy/batch/task`

- **权限**：`operation:deploy:exec` + `operation:server:list`
- **Content-Type**：`application/json`
- **请求体** `OperationDeployBatchTaskRequest`：

| 字段 | 必填 | 说明 |
|------|------|------|
| `steps` | 是 | 1~32 步；每步同单任务 `OperationDeployTaskRequest`（`serviceKey` / `action` / `serverId` / `projectId`） |
| `projectId` | 否 | 批次级项目；步骤未传 `projectId` 时回填 |
| `stopOnFailure` | 否 | 默认 `true`；某步失败是否中断后续 |
| `intervalSeconds` | 否 | 步骤间隔秒数 0~300，默认 0（滚动重启） |

- **行为**：创建 **单父任务**（`taskType=deploy_batch`），顺序执行各步 SSH/本机启停；全局锁 `deploy_batch:global`
- **响应**：`data: taskId`（Long）；轮询 `GET /operation/task/{id}` 查看 `[BATCH]` 分段日志

---

## 6. 部署预设（SVR-20）

### `GET /operation/deploy/presets?serverId=`

- **权限**：`operation:server:list`
- **响应** `OperationDeployPresetsVo`：

```json
{
  "pathPresets": [
    "/opt/moli/frontend/dist/",
    "/opt/moli-project-distribute/moli-gateway/",
    "/opt/moli-project-distribute/moli-knowledge/",
    "/opt/moli-project-distribute/moli-user-center/"
  ],
  "actionPresets": [
    { "value": "none", "label": "无" },
    { "value": "nginxReload", "label": "sudo nginx -s reload" },
    { "value": "unzipToDist", "label": "解压 zip 到 dist（备份旧目录）" },
    { "value": "restartService:user-center", "label": "重启 user-center" },
    { "value": "restartService:gateway", "label": "重启 gateway" },
    { "value": "restartService:knowledge", "label": "重启 knowledge" }
  ],
  "serviceKeys": [
    { "key": "user-center", "label": "用户中心" },
    { "key": "gateway", "label": "网关" },
    { "key": "knowledge", "label": "知识库" }
  ]
}
```

- `pathPresets`：默认四条 + 全局 `allowed-paths` / `allow-any-under` + 该服务器 `upload_allowed_roots` 合并去重排序
- **`serviceKeys`**：与 `ops.deploy.services` 一致；**前端部署中心应用此列表渲染启停下拉**，勿硬编码 `MOLI_DEPLOY_SERVICES`
- **自定义命令**：不在 `actionPresets` 中；前端用 `postAction=custom` 或独立命令 API

---

## 7. 文件上传发布（SVR-16 / SVR-19）

### `POST /operation/file/upload`

- **权限**：`operation:file:upload` + `operation:server:list`；`postAction=custom` 另需 `operation:command:exec`
- **Content-Type**：`multipart/form-data`

| 字段 | 必填 | 说明 |
|------|------|------|
| `file` | 是 | 上传文件 |
| `serverId` | 是 | 目标服务器 ID |
| `targetPath` | 是 | **手输**绝对路径；若以 `/` 结尾则自动拼接文件名 |
| `postAction` | 否 | 默认 `none` |
| `postCommand` | 条件 | `postAction=custom` 时必填 |

**postAction 枚举**：

| 值 | 远程行为 | 额外要求 |
|----|----------|----------|
| `none` | 仅 SFTP | — |
| `nginxReload` | `sudo nginx -t && sudo nginx -s reload` | — |
| `unzipToDist` | 解压 zip 到 `dist`（备份旧目录） | 目标须为 `.zip` |
| `restartService:user-center` 等 | `moli-service.sh {key} restart` | `ops.deploy.enabled=true` |
| `custom` | 执行 `postCommand` | `ops.command.enabled=true` + command 权限 |

**响应**：`data: taskId`

### 路径白名单（三层 OR）

目标路径须满足**任一**前缀（`OperationPathPolicy`）：

1. `ops.upload.allowed-paths`（默认 `/opt/moli/frontend/`、`/opt/moli-project-distribute/`）
2. `ops.upload.allow-any-under`（默认 `/opt/`、`/home/ubuntu/`）
3. 服务器 `upload_allowed_roots`（SSH 配置写入）

禁止：`..`、非绝对路径、不在白名单内。

---

## 8. 远程命令（SVR-18）

### `POST /operation/command/exec/task`

- **权限**：`operation:command:exec` + `operation:server:list`
- **要求**：`ops.command.enabled=true`

**请求体** `OperationCommandExecVo`：

```json
{
  "serverId": 204,
  "command": "nginx -t && sudo nginx -s reload",
  "workDir": "/opt/moli-project-distribute"
}
```

| 字段 | 说明 |
|------|------|
| `command` | shell，允许多行；允许 `;` `&&` `\|` |
| `workDir` | 可选；空则用 `ops.command.default-work-dir`；远程执行为 `cd {workDir} && {command}` |

**响应**：`data: taskId`；`taskType=command`

### Shell 高危拦截（`OperationShellGuard`）

拒绝含（非穷举）：`rm -rf /`、`mkfs`、`dd if=`、重定向到 `/etc/`、`curl|bash`、`reboot`、`shutdown` 等。

单条命令长度上限：`ops.command.max-chars`（默认 8192）。

---

## 9. 业务错误码（Phase R2）

`MoliResult.code` 非 200 时，`msg` 含可读说明；下列为运维模块稳定业务码（`BaseException.errorCode`）：

| code | 常量 | 场景 | 前端建议 |
|------|------|------|----------|
| 10101 | `OPERATION_DUPLICATE_IP` | 同环境下 IP 重复 | Toast + 高亮 IP 字段 |
| 10102 | `OPERATION_SERVER_NOT_FOUND` | serverId 不存在 | Toast |
| 10103 | `OPERATION_ENTITY_NOT_FOUND` | 平台/项目/组件不存在 | Toast |
| 10104 | `OPERATION_MISSING_ID` | 更新缺 id | 表单校验 |
| 10105 | `OPERATION_SSH_NOT_CONFIGURED` | SSH 未配置 | 引导打开 SSH 弹窗 |
| 10106 | `OPERATION_DEPLOY_DISABLED` | `ops.deploy.enabled=false` | 说明需运维开开关 |
| 10107 | `OPERATION_SERVER_TASK_RUNNING` | 删服务器时有 running/pending 任务 | Toast + 链到任务列表 |
| 10108 | `OPERATION_UPLOAD_DISABLED` | 上传开关关 | 同 10106 |
| 10109 | `OPERATION_LOCAL_DEPLOY_DISABLED` | 未传 serverId 且 `allow-local=false` | **必须传 serverId** 或提示仅 dev 可用 |

Bean Validation（`@Valid` SaveRequest）失败通常返回通用参数错误码，见网关/全局异常处理。

---

## 10. 典型错误（msg 示例）

| 场景 | msg 示例 |
|------|----------|
| 本机部署禁用 | OPERATION_LOCAL_DEPLOY_DISABLED: 本机部署未启用，请指定 serverId 或配置 ops.deploy.allow-local=true |
| 上传未开 | 文件上传发布未启用，请配置 ops.upload.enabled=true |
| 路径越权 | targetPath 不在允许范围内… |
| 命令未开 | 远程命令执行未启用，请配置 ops.command.enabled=true |
| 高危命令 | 命令包含高危操作，已被拦截 |
| 任务互斥 | 已有任务在执行（同 server+service 或 upload/command 锁） |
| 删服务器有任务 | OPERATION_SERVER_TASK_RUNNING: 服务器 {id} 仍有进行中的运维任务 |

---

## 11. 相关

- 路线图：[server-ops-module-roadmap.md](../design/server-ops-module-roadmap.md) P3 / P3+
- SQL 顺序：[sql-migration-order.md](../ops/sql-migration-order.md)
- 腾讯云手工对照：[腾讯云上线流程.md](../../deploy/腾讯云上线流程.md) §14
- 部署脚本：`deploy/linux/moli-service.sh`
