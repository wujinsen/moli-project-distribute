# 运营管理 · 部署中心验收用例（SVR-13 ~ SVR-16）

> 模块：`moli-user-center-server` + `meiling-ui`  
> 契约：[`docs/api/operation-frontend.md`](../api/operation-frontend.md) §11  
> 自动化：`mvn -Dtest=Operation*Remote*,Operation*Task*,Operation*Upload*,OperationSsh* test`（user-center-server）

## 0. 前置条件

| # | 项 | 期望 |
|---|-----|------|
| P0 | DB 已执行 `docs/sql/17_*`～`21_operation_ssh_deploy.sql` | `operation_task` 表存在；菜单 405「部署中心」可见 |
| P1 | user-center 配置 | `OPS_SECRET_KEY` 已设；`OPS_DEPLOY_ENABLED=true`；`OPS_UPLOAD_ENABLED=true` |
| P2 | 角色权限 | `operation:ssh:manage`、`operation:deploy:exec`、`operation:file:upload`、`operation:server:list` |
| P3 | 腾讯云 CVM | OS 用户 `ubuntu`；`sudo nginx -s reload` 已配 NOPASSWD（若测 nginxReload） |
| P4 | 服务器台账 | 目标 CVM 已录入；SSH 私钥已配置且测试连接成功 |

---

## 1. SSH 凭据（SVR-13）

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| SSH-1 | 保存私钥 | 服务器管理 → SSH 配置 → 上传 `.pem`、用户 `ubuntu`、端口 22 → 保存 | 200；列表行显示 SSH 标识；**接口不返回私钥明文** |
| SSH-2 | 测试连接 | 已保存私钥 → 测试连接 | 返回 `whoami=ubuntu`（或实际用户）、延迟 ms |
| SSH-3 | 未配置 SSH | 未配私钥的服务器 → 测试连接 | 明确错误：未配置认证方式 |
| SSH-4 | 无权限 | 无 `operation:ssh:manage` 的用户 | 403 / 前端按钮不可见 |
| SSH-5 | 密钥错误 | 故意上传错误私钥 → 测试连接 | 失败信息含主机/端口，不泄露密钥内容 |

---

## 2. 异步任务轮询（SVR-14）

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| TASK-1 | 轮询增量日志 | `GET /operation/task/{id}?logOffset=0` 后 offset=上次 `nextLogOffset` | `logChunk` 仅为新增段；`finished=false` 时 status=running |
| TASK-2 | 任务完成 | 任务 success/failed 后最后一次轮询 | `finished=true`；progress=100（成功）或失败摘要 |
| TASK-3 | 任务历史 | `GET /operation/task/list?taskType=deploy` | 分页列表**不含** `task_log` 大字段 |
| TASK-4 | 互斥锁 | 同一 server+service 连续点两次 restart | 第二次提示「已有任务在执行」 |
| TASK-5 | 前端抽屉 | 部署中心启停后打开任务抽屉 | 1.5s 轮询；日志自动追加；完成后停止轮询 |

---

## 3. 远程启停（SVR-15）

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| DEP-1 | 本机只读 status | `GET /operation/deploy/user-center/status`（无 serverId） | 返回 running/available/output |
| DEP-2 | 远程只读 status | `GET .../status?serverId={CVM}` | SSH 执行 `moli-service.sh status` |
| DEP-3 | 远程 restart | `POST /operation/deploy/user-center/restart/task?serverId={CVM}` | 返回 `taskId`；日志含 `[SSH]`、`moli-service.sh` |
| DEP-4 | deploy 开关关闭 | `ops.deploy.enabled=false` 时点 restart | 错误：部署变更动作未启用 |
| DEP-5 | 非法 serviceKey | `POST .../moli-order/restart/task` | 不支持的 serviceKey |
| DEP-6 | 脚本自动就位 | 远端删除 `moli-service.sh` 后执行 restart | 任务日志显示脚本上传 + chmod；随后执行成功 |
| DEP-7 | 三件套白名单 | gateway / knowledge start-stop-restart | 均可创建任务；order/bi 应拒绝 |

---

## 4. 文件上传发布（SVR-16）

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| UPL-1 | 上传 JAR | 选 CVM → 路径 `.../moli-user-center/` → 文件 `*.jar` → postAction `restartService:user-center` | taskId；SFTP 进度 0→80%；后置 restart 日志；最终 success |
| UPL-2 | 上传前端 zip | 路径 `/opt/moli/frontend/dist/` + `unzipToDist` | 远端 dist 切换；旧 dist 备份为 `dist.bak.*` |
| UPL-3 | nginx reload | 任意合法路径 + `nginxReload` | 执行 `sudo nginx -t && sudo nginx -s reload` |
| UPL-4 | 路径白名单 | targetPath=`/tmp/evil.jar` | 拒绝：不在白名单 |
| UPL-5 | 路径穿越 | targetPath 含 `..` | 拒绝 |
| UPL-6 | upload 开关关闭 | `ops.upload.enabled=false` | 拒绝上传 |
| UPL-7 | 超大文件 | 上传 > `ops.upload.max-bytes` | 拒绝并提示 MB 上限 |
| UPL-8 | 非法后置动作 | postAction=`rm -rf /` | 拒绝：不支持的后置动作 |

---

## 5. 前端页面（SVR-17）

| ID | 场景 | 步骤 | 期望 |
|----|------|------|------|
| FE-1 | 菜单 | 重新登录 → 运营管理 | 可见「部署中心」 |
| FE-2 | 服务器列表 | 部署中心左侧 | 显示 SSH 已配置标识 |
| FE-3 | 服务卡片 | 选 CVM → 三件套卡片 | 显示运行状态；start/stop/restart 需 `operation:deploy:exec` |
| FE-4 | 文件面板 | 拖拽 jar → 选路径/后置动作 → 上传 | 打开任务抽屉；见 TASK-5 |
| FE-5 | i18n | 切换 zh/en/ja | 部署中心、SSH、任务状态文案正确 |

---

## 6. 自动化测试（CI / 本地）

### 后端（Mock，无需 MySQL/SSH）

```bash
cd moli-user-center/moli-user-center-server
mvn -Dtest=OperationSshClientTest,OperationTaskServiceImplTest,OperationRemoteDeployServiceImplTest,OperationFileUploadServiceImplTest,OperationRemoteDeployControllersApiTest test
```

| 测试类 | 覆盖点 |
|--------|--------|
| `OperationSshClientTest` | `shellQuote` 防注入 |
| `OperationTaskServiceImplTest` | 轮询 logOffset 截取 |
| `OperationRemoteDeployServiceImplTest` | deploy 开关、白名单、taskId |
| `OperationFileUploadServiceImplTest` | 路径白名单、postAction 枚举 |
| `OperationRemoteDeployControllersApiTest` | SSH/任务/上传 Controller 200 |

### 前端

```bash
cd meiling-ui
npm test -- src/composables/useOperationTaskPoll.spec.ts
```

---

## 7. 相关

- 用户中心测试索引：[`user-center.md`](user-center.md)
- 上线冒烟：[`release-smoke-checklist.md`](release-smoke-checklist.md)
- 腾讯云手工步骤：[`deploy/腾讯云上线流程.md`](../../deploy/腾讯云上线流程.md) §14
