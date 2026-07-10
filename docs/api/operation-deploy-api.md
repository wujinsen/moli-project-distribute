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
  upload:
    enabled: ${OPS_UPLOAD_ENABLED:false}
    allowed-paths: ${OPS_UPLOAD_ALLOWED_PATHS:/opt/moli/frontend/,/opt/moli-project-distribute/}
    max-bytes: ${OPS_UPLOAD_MAX_BYTES:209715200}
  command:
    enabled: ${OPS_COMMAND_ENABLED:false}
    max-chars: ${OPS_COMMAND_MAX_CHARS:8192}
    default-work-dir: ${OPS_COMMAND_DEFAULT_WORK_DIR:/opt/moli-project-distribute}
```

| 开关 | 影响 |
|------|------|
| `ops.deploy.enabled=false` | 拒绝 `start`/`stop`/`restart` 及上传后置 `restartService:*` |
| `ops.upload.enabled=false` | 拒绝文件上传 |
| `ops.command.enabled=false` | 拒绝远程命令、上传 `postAction=custom` |

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
| `status` | `pending` / `running` / `success` / `failed` |
| `progress` | 0–100 |
| `logChunk` | 自 `logOffset` 起的增量日志 |
| `nextLogOffset` | 下次轮询传入 |
| `finished` | 是否终态 |

### `GET /operation/task/list`

- **权限**：`operation:server:list`
- **查询**：`taskType`（`deploy`/`upload`/`command`）、`serverId`、`pageNum`、`pageSize`
- **说明**：列表**不含** `task_log` 大字段

---

## 5. 远程启停（SVR-15）

### `GET /operation/deploy/{serviceKey}/status?serverId=`

- **权限**：`operation:server:list`
- **serviceKey**：`user-center` · `gateway` · `knowledge`
- **serverId 空**：本机执行 `moli-service.sh status`；**非空**：SSH 远程只读

### `POST /operation/deploy/{serviceKey}/{action}/task?serverId=`

- **权限**：`operation:deploy:exec` + `operation:server:list`
- **action**：`start` · `stop` · `restart`
- **响应**：`data: taskId`（Long）
- **要求**：`ops.deploy.enabled=true`；同一 `serverId+serviceKey` 互斥

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
  ]
}
```

- `pathPresets`：默认四条 + 全局 `allowed-paths` / `allow-any-under` + 该服务器 `upload_allowed_roots` 合并去重排序
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

## 9. 典型错误

| 场景 | msg 示例 |
|------|----------|
| 上传未开 | 文件上传发布未启用，请配置 ops.upload.enabled=true |
| 路径越权 | targetPath 不在允许范围内… |
| 命令未开 | 远程命令执行未启用，请配置 ops.command.enabled=true |
| 高危命令 | 命令包含高危操作，已被拦截 |
| 任务互斥 | 已有任务在执行（同 server+service 或 upload/command 锁） |

---

## 10. 相关

- 路线图：[server-ops-module-roadmap.md](../design/server-ops-module-roadmap.md) P3 / P3+
- SQL 顺序：[sql-migration-order.md](../ops/sql-migration-order.md)
- 腾讯云手工对照：[腾讯云上线流程.md](../../deploy/腾讯云上线流程.md) §14
- 部署脚本：`deploy/linux/moli-service.sh`
