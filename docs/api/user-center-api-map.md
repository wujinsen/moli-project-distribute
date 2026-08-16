# 后台接口迭代地图

最后更新: 2026-06-11  
适用范围: `moli-server` 当前代码中的 Controller 接口

## 1. 基本结论

- 控制器数量: 17
- HTTP 接口数量: 约 75（`GET/POST/PUT/DELETE` 注解统计）
- 统一返回: `MoliResult<T>`
- 分页返回: `PageRes<T>`
- 鉴权方式: Shiro Session（token 实际为 sessionId）
- 细粒度权限: 页面 `sys_menu.perms`（如 `system:user:list`）+ 动作 `sys_role_action`（如 `system:user:add`）；`PermissionService` 并集后供 Shiro 与 `GET /auth/capabilities`
- 超级管理员: `superadmin` / `admin` 拥有 `*:*:*`，可进入任意已注册系统（含停用），进入 INTERNAL 系统时返回全部菜单；多系统时与普通用户一样需先选系统（仅 1 个 INTERNAL 时登录自动进入）
- 无权限响应: HTTP 200 + `code=10009`；Shiro 鉴权失败 `msg=无权限操作`；业务层校验保留细分文案
- 全局鉴权策略:
  - 放行: `/login`、`/sso/validate`、Swagger 相关
  - 其余路径: `authc` 拦截（未登录返回 token 失效 JSON）

## 2. 认证与会话

### `LoginController`（前缀为空）

- `POST /login`
  - 入参: `SysUser`（JSON body）
  - 出参: `MoliResult<LoginVo>`（token + user + `fullPermission` + `systemPortalEnabled` + 可选 `systemList` / `currentSystem` / `menuVoList`）
  - 出参含 `permissions: string[]`（门户关闭或单 INTERNAL 自动进入时与 `menuVoList` 一并下发）
  - 说明: 启用 `sso.enabled` 且存在 `sys_system` 时返回系统门户；**门户关闭**时直出 `menuVoList` + `permissions`；仅 1 个 INTERNAL 系统时 `fillLoginContext` 完整拷贝 `currentSystem`、`menuVoList`、`permissions`、`fullPermission`；多系统时 `menuVoList` 为空，需调 `/system/enter` 或走选系统页
- `POST /logout`
  - 入参: 无
  - 出参: `MoliResult`
- `POST /captchaImage`
  - 入参: 无
  - 出参: `MoliResult<CaptchaImageVo>`
  - 现状: 支持开关控制（`captcha.enabled`），关闭时返回“验证码功能暂时关闭”

### `SystemController`（前缀 `/system`）

- `GET /system/my`：当前用户可访问系统；`SystemVo` 含 `systemGroup`（门户分组，见 [`docs/design/portal-system-group.md`](../design/portal-system-group.md)）
- `POST /system/enter`、`POST /system/switch`：进入/切换系统（同一 Session）；INTERNAL 返回 `menuVoList`；EXTERNAL 返回 `redirectUrl`
  - `SystemEnterVo` 含 `permissions`、`fullPermission`（与 `LoginVo` 对齐）
- `GET /auth/capabilities`：当前系统上下文 `{ permissions, fullPermission }`；F5 / 缓存缺失时补拉
- `GET /action/list?menuId=`：页面可分配动作（角色授权 UI）
- **P4 动作目录**：`GET /action/page`、`GET /action/{id}`、`POST/PUT/DELETE /action`、`PUT /action/changeStatus`；权限与菜单管理一致（读 `system:menu:list`，写 `system:menu:edit` + `list`）
- `GET/POST/PUT/DELETE /system`：系统注册维护（权限 `system:system:list`；增删改另需 `superadmin`/`admin`）；`SysSystem.systemGroup` 支持 `platform`/`business`/`data`/`tech`/`ops`；`GET /system/list` 可按 `systemGroup` 筛选
- SQL：`patch_sys_system_group` 已合并至 `docs/sql` 基线（`sys_system.group_code` 等）
- 侧栏菜单：`path=system`，`component=system/system/index`，`route_name=SystemRegistry`，`perms=system:system:list`（先执行 `patch_sys_menu_route_name.sql`）

### `SsoController`（前缀 `/sso`）

- `POST /sso/validate`：子系统校验 Ticket（匿名；可配请求头 `X-Sso-Secret`）；响应含 `fullPermission`（超管为 `true`，外部系统可据此授予本地最大权限）

### `ChatGPTController`（前缀 `chatgpt`）

- `GET /chatgpt/v1/createCompletion`
  - 入参: `Messages`（query 参数形态）
  - 出参: `MoliResult<String>`
- `GET /chatgpt/v1/createCompletionTurbo`
  - 入参: `Messages`（query 参数形态）
  - 出参: `MoliResult<String>`

## 3. 系统管理域接口

### 用户管理 `UserController`（前缀 `/user`，13个）

- `GET /user/list`：分页用户列表（`UserVo` 查询参数）；`superadmin`（最大权限）与 `admin`（特殊管理员）对外隐藏，仅特殊账号登录时可见；**未选部门**时特殊账号可见，**按部门筛选**时仅展示 `dept_id` 落在该部门树内的用户（无部门归属的特殊账号不会出现于各部门子列表）
- 用户查询/删除/改状态/重置密码等：非特殊账号访问 `superadmin`/`admin` 返回无权限（`10009`）
- `POST /user`：新增用户；权限 `system:user:add` + `system:user:list`
- `PUT /user`：更新用户；**本人**仅可改昵称/联系方式等个人信息（仅需登录）；改他人需 `system:user:edit` + `system:user:list`
- `GET /user/{id}`：查询用户
- `GET /user/getUserDetail/{id}`：查询用户详情（含 postIds）
- `GET /user/profile`：当前登录用户信息
- `DELETE /user/{userIds}`：批量删除；权限 `system:user:remove` + `system:user:list`
- `PUT /user/changeStatus`：用户启停；权限 `system:user:edit` + `system:user:list`
- `GET /user/getRoleByUserId/{userId}`：用户角色信息
- `PUT /user/insertUserRole`：重设用户角色；权限 `system:user:assignRole` + `system:user:list`
- `PUT /user/addUserRole`：给角色新增用户；成功后 `msg` 提示刷新页面；写入操作日志
- `GET /user/getUserByRole`：查询角色下用户
- `PUT /user/removeUsers`：移除角色下用户；成功后 `msg` 提示刷新页面；写入操作日志
- `GET /user/unauthorizedUsers`：角色未授权用户列表
- `PUT /user/resetPassword`：重置密码；**本人**可改自己密码（仅需登录，可选传 `oldPassword` 校验）；改他人需 `system:user:resetPwd` + `system:user:list`
- `GET /user/getSystemByUserId/{userId}`：用户已授权系统
- `PUT /user/insertUserSystem`：保存用户可访问系统；权限 `system:user:assignSystem` + `system:user:list`
- `GET /user/getSystemByUserId/{userId}`：超管目标用户 `systemIds` 为全部系统，`systemList` 含停用系统
- `GET /user/getUserBySystem`：按系统查已授权用户（query：`systemId`、分页、`userName`/`telephone`）；含 `sys_user_system` 关联及超管
- `GET /user/unauthorizedUsersBySystem`：按系统查未授权用户（参数同上；排除已授权与超管）
- 侧栏菜单「系统用户分配」：`path=system-user`，`component=system/system-user/index`，`route_name=SystemUserAssign`，`perms=system:user:list`（`patch_sys_menu_system_user_assign.sql`）
- 菜单顶层分组（方案 1）：`patch_sys_menu_grouping.sql` — 「多系统」(`path=portal`)、「审计日志」(`path=audit`)；角色仅勾子菜单时后端自动补齐父级，无需改 `sys_role_menu`

### 角色管理 `RoleController`（前缀 `/role`，8个）

- `GET /role/list`：分页列表；权限 `system:role:list`
- `POST /role`：新增角色（`menuIds` + `actionCodes`）；权限 `system:role:add` + `system:role:list`
- `PUT /role`：更新角色（`menuIds` + `actionCodes`）；强制「有动作必先有页面」；权限 `system:role:edit` + `system:role:list`
- `GET /role/{id}/auth`：授权回显 `{ menuIds, actionCodes }`；权限 `system:role:list`
- `GET /role/{id}`：查询角色；权限 `system:role:list`
- `DELETE /role/{ids}`：删除角色（含关系）；权限 `system:role:remove` + `system:role:list`
- `PUT /role/changeStatus`：角色状态变更；权限 `system:role:edit` + `system:role:list`
- `GET /role/getRoleAll`：获取有效角色列表；权限 `system:role:list`
- 用户模块 `PUT /user/addUserRole`、`PUT /user/removeUsers`：权限 `system:role:edit` + `system:role:list`

### 菜单管理 `MenuController`（前缀 `/menu`，8个）

- `GET /menu/getRouters`：当前用户菜单树；门户开启时按 Session `currentSystemId` + `sys_menu.system_id` 过滤（**SSO-MENU-1**）；未 enter 返回 **`[]`**（见 [sso-menu-frontend-handoff.md](sso-menu-frontend-handoff.md)）
- `GET /menu/list`：菜单列表（`menuName`、`status`）；权限 `system:menu:list`
- `POST /menu`：新增菜单；权限 `system:menu:add` + `system:menu:list`
- `PUT /menu`：更新菜单；权限 `system:menu:edit` + `system:menu:list`
- `GET /menu/{id}`：菜单详情；权限 `system:menu:list`
- `DELETE /menu/{id}`：删除菜单；权限 `system:menu:remove` + `system:menu:list`
- `GET /menu/selectMenuTreeByRoleId/{roleId}`：角色菜单树
- `GET /menu/getMenuTreeAll`：全量菜单树

### 部门管理 `DeptController`（前缀 `dept`，6个）

- `GET /dept/list`：部门列表；权限 `system:dept:list`
- `GET /dept/getDeptTreeList`：部门树；权限 `system:dept:list`
- `POST /dept`：新增部门；权限 `system:dept:add` + `system:dept:list`
- `PUT /dept`：更新部门；权限 `system:dept:edit` + `system:dept:list`
- `GET /dept/{id}`：部门详情；权限 `system:dept:list`
- `DELETE /dept/{id}`：删除部门（级联删除其下所有子部门；部门不存在时返回 `data: false`）；权限 `system:dept:remove` + `system:dept:list`

### 岗位管理 `PostController`（前缀 `post`，6个）

- `GET /post/list`：分页岗位；权限 `system:post:list`
- `POST /post`：新增岗位；权限 `system:post:add` + `system:post:list`
- `PUT /post`：更新岗位；权限 `system:post:edit` + `system:post:list`
- `GET /post/{id}`：岗位详情；权限 `system:post:list`
- `DELETE /post/{ids}`：批量删除；权限 `system:post:remove` + `system:post:list`
- `GET /post/allPost`：全部岗位；权限 `system:post:list`

### 参数设置 `ConfigController`（前缀 `config`，3个）

契约详见 [`sys-config-notice-api.md`](sys-config-notice-api.md) §2。

- `GET /config/list`：参数列表（**注册表驱动**，非表驱动；无分页）；权限 `system:config:list`；可选 `group=SECURITY|PORTAL|OPS`
- `PUT /config`：设置覆盖值；权限 `system:config:edit` + `system:config:list`
- `DELETE /config/{configKey}`：重置为默认（删覆盖行，**不是**删参数）；权限 `system:config:remove` + `system:config:list`

**无 `POST`**：参数在代码 `ConfigKey` 枚举里声明，不能由 UI 创建，故也无 `system:config:add`。

### 通知公告 `NoticeController`（前缀 `notice`，10个）

契约详见 [`sys-config-notice-api.md`](sys-config-notice-api.md) §3–§4。

后台管理（可见草稿/已撤回）：

- `GET /notice/list`：分页列表，不含正文；权限 `system:notice:list`
- `GET /notice/{id}`：详情，含正文；权限 `system:notice:list`
- `POST /notice`：新增，**强制落草稿**；权限 `system:notice:add` + `system:notice:list`
- `PUT /notice`：修改，不改状态；权限 `system:notice:edit` + `system:notice:list`
- `PUT /notice/publish/{id}`：发布（仅草稿/已撤回可发布）；权限 `system:notice:edit` + `system:notice:list`
- `PUT /notice/revoke/{id}`：撤回（仅已发布可撤回）；权限 `system:notice:edit` + `system:notice:list`
- `DELETE /notice/{ids}`：批量物理删除，仅用于清理误建草稿；权限 `system:notice:remove` + `system:notice:list`

阅读侧（**仅需登录，无 perms**；只可见已发布且未过期）：

- `GET /notice/feed`：有效公告 + 未读数（一次请求拿列表与角标）
- `GET /notice/feed/{id}`：详情，含正文
- `PUT /notice/feed/read`：把已读**水位**推进到此刻

前端渲染通知栏必须用 `/notice/feed*`，用后台接口会让普通用户吃 403。

### 字典管理 `DictController`（前缀 `dict`，10个）

- 类型:
  - `GET /dict/type/list`、`listAll`、`GET /dict/type/{id}`：`system:dict:list`
  - `POST /dict/type`：`system:dict:add` + `list`
  - `PUT /dict/type`：`system:dict:edit` + `list`
  - `DELETE /dict/type/{dictIds}`：`system:dict:remove` + `list`
- 数据:
  - `GET /dict/data/list`、`GET /dict/data/{id}`：`system:dict:list`
  - `POST/PUT /dict/data`：`add`/`edit` + `list`
  - `DELETE /dict/data/{dictIds}`：`system:dict:remove` + `list`

### 日志管理 `LogController`（前缀 `/log`，2个）

- `GET /log/loginLogList`：登录日志分页；`system:loginlog:list`
- `DELETE /log/loginLog/{ids}`、`DELETE /log/loginLog/clean`：`system:loginlog:remove` + `list`
- `GET /log/operationLogList`：操作日志分页；`system:operlog:list`
- `DELETE /log/operationLog/{ids}`、`DELETE /log/operationLog/clean`：`system:operlog:remove` + `list`

## 4. 运维域接口

### 4.0 前端依赖与 links 契约（2026-07-13）

| 项 | 状态 | 说明 |
|----|------|------|
| `PUT .../project\|component/links` 同步主表 | ✅ | N:N 全量替换 + `server_id`/`server_ip` 对齐首台 |
| `GET .../links` 与 list `serverIds` | ✅ | N:N 为空时回退 `[serverId]`；与 `OperationRelationQuerySupport` 一致 |
| `serverCount` / `componentCount` | ✅ 凡 `toVo()` 出口 | **`toVo()` 派生**；`serverCount === serverIds.length`；前端 [operation-frontend-handoff.md](operation-frontend-handoff.md) §0 |
| 历史脏数据 | 运维一次性 | `GET /operation/audit/reconcile-relations` |

**给后端总览**：[frontend-backend-dependencies.md](frontend-backend-dependencies.md) · [frontend-gaps.md](../frontend-gaps.md)

### 平台管理 `OperationPlatformController`（前缀 `/operation/platform`，6个）

- `GET /operation/platform/list`：`operation:platform:list`；返回 `OperationPlatformVo`（含 `passwordConfigured` / `passwordMask`，无明文）
- `POST`：`operation:platform:add` + `list`；`PUT`：`edit` + `list`；`DELETE`：`remove` + `list`
- `GET /operation/platform/{id}`：`operation:platform:list`；返回 VO
- `GET /operation/platform/{id}/secret`：`operation:secret:view`；返回 `{ password }` 明文（记审计日志）

### 服务器管理 `OperationServerController`（前缀 `/operation/server`，11个）

- `GET /operation/server/list`：`operation:server:list`；返回 `OperationServerVo`（含 `status` / `lastCheckTime` / `projectCount` / `componentCount` / `tags` / `serverRole`）；支持 `projectId`、`componentId` 反向过滤
- `POST /operation/server`：`operation:server:add` + `list`；**响应 `data` 为新建 `id`（Long）**
- `PUT /operation/server`：`operation:server:edit` + `list`
- `GET /operation/server/{id}`：`operation:server:list`；返回 VO（含 **`projectCount` / `componentCount`**）
- `DELETE /operation/server/{ids}`：`operation:server:remove` + `list`
- `GET /operation/server/{id}/links`：`operation:server:list`；N:N 项目/组件 ID
- ~~`GET /operation/server/{id}/topology`~~：**已删除**（SVR-5）；改用 `GET /operation/relations/server/{id}`
- `POST /operation/server/{id}/check`：`operation:server:list`；TCP 探活，更新并返回 `OperationServerVo`
- `GET /operation/server/{id}/links`：`operation:server:list`；返回 `OperationServerLinksVo`（`projectIds` / `componentIds`）
- `PUT /operation/server/{id}/links`：`operation:server:edit` + `list`；全量替换 N:N 关联
- `PUT /operation/server/{id}/ssh`：`operation:ssh:manage`；保存 SSH 凭据 + `uploadAllowedRoots`（只写不读）
- `POST /operation/server/{id}/ssh/test`：`operation:ssh:manage`；测试 SSH，返回 `OperationSshTestVo`

### 项目管理 `OperationProjectController`（前缀 `/operation/project`，9个）

- `GET /operation/project/list`：`operation:project:list`；返回 `OperationProjectVo`（含 `serverIds` / `serverCount` / `componentCount` / `expectedPort` / `portMatchStatus` / `deployRunning`）；支持 `serverId`、`componentId` 反向过滤
- `POST /operation/project`：`operation:project:add` + `list`；body 可传 `serverIds[]`，同步 `operation_server_project`；**响应 `data` 为新建 `id`（Long）**
- `POST /operation/component`：同上对称；**响应 `data` 为新建 `id`（Long）**
- `PUT /operation/project`：`operation:project:edit` + `list`
- `GET /operation/project/{id}`：`operation:project:list`；返回 VO（含 `serverIds`、**`serverCount` / `componentCount`**）
- `DELETE /operation/project/{ids}`：`operation:project:remove` + `list`
- `GET /operation/project/{id}/links`：`operation:project:list`；返回 `{ projectId, serverIds }`
- `GET /operation/project/links/batch?ids=`：`operation:project:list`；逗号分隔最多 50；返回 `{ items: OperationProjectLinksVo[] }`
- `PUT /operation/project/{id}/links`：`operation:project:edit` + `list`；全量替换 N:N，**并同步主表 `server_id`/`server_ip` 为 `serverIds[0]`**（2026-07-13）
- `GET /operation/project/{id}/component-links`：`operation:project:list`；返回 `{ projectId, componentIds }`（SVR-26a）
- `PUT /operation/project/{id}/component-links`：`operation:project:edit` + `list`；全量替换 `operation_project_component`

### 组件管理 `OperationComponentController`（前缀 `/operation/component`，9个）

- `GET /operation/component/list`：`operation:component:list`；返回 `OperationComponentVo`（含 `serverIds` / `serverCount` / `projectCount` / `status` / `lastCheckTime`）；支持 `serverId`（含 N:N）、`projectId` 反向过滤
- `POST /operation/component`：`operation:component:add` + `list`；body 可传 `serverIds[]`
- `PUT /operation/component`：`operation:component:edit` + `list`
- `GET /operation/component/{id}`：`operation:component:list`；返回 VO（含 `serverIds`、**`serverCount` / `projectCount`**）
- `GET /operation/component/{id}/secret`：`operation:secret:view`
- `DELETE /operation/component/{ids}`：`operation:component:remove` + `list`
- `POST /operation/component/{id}/check`：`operation:component:list`；TCP 探活，更新并返回 `OperationComponentVo`
- `GET /operation/component/{id}/links`：`operation:component:list`；返回 `{ componentId, serverIds }`
- `GET /operation/component/links/batch?ids=`：`operation:component:list`；逗号分隔最多 50；返回 `{ items: OperationComponentLinksVo[] }`
- `PUT /operation/component/{id}/links`：`operation:component:edit` + `list`；全量替换 N:N，**并同步主表 `server_id`**（2026-07-13）

> **前端对接专稿**：[operation-frontend.md](operation-frontend.md) · **后端联调通知（给前端）**：[operation-backend-handoff.md](operation-backend-handoff.md)
> **部署中心 HTTP 契约**：[operation-deploy-api.md](operation-deploy-api.md)（SVR-13~20）  
> **路线图 / 待办**：[server-ops-module-roadmap.md](../design/server-ops-module-roadmap.md) §5.1

### 运维审计 `OperationAuditController`（前缀 `/operation/audit`，1个）

- `GET /operation/audit/port-matrix`：`operation:project:list`；对照 DB 端口矩阵校验项目/组件端口（SVR-21 后数据源为 `operation_port_matrix`）

### 端口矩阵管理 `OperationPortMatrixController`（前缀 `/operation/port-matrix`，5个 · SVR-21 设计稿）

- `GET /operation/port-matrix/list`：`operation:port-matrix:list`；分页列表
- `GET /operation/port-matrix/{id}`：`operation:port-matrix:list`
- `POST /operation/port-matrix`：`operation:port-matrix:add` + `list`
- `PUT /operation/port-matrix`：`operation:port-matrix:edit` + `list`
- `DELETE /operation/port-matrix/{ids}`：`operation:port-matrix:remove` + `list`

> 契约：[`operation-port-matrix-api.md`](operation-port-matrix-api.md) · 设计：[`operation-port-matrix-config.md`](../design/operation-port-matrix-config.md)

### 运维统计 `OperationStatsController`（前缀 `/operation`，1个）

- `GET /operation/stats`：`operation:project:list`；台账计数 + 端口不符数 + 健康 DOWN 数（驾驶舱 ops 用）

### 全局拓扑 `OperationTopologyController`（前缀 `/operation/topology`，1个 · SVR-25a）

- `GET /operation/topology`：`operation:server:list`；返回 `OperationTopologyGraphVo`（servers/projects/components 节点 + `deploys`/`depends_on` 边）

### 关联关系 `OperationRelationsController`（前缀 `/operation/relations`，1个 · SVR-28b）

- `GET /operation/relations/{entityType}/{id}`：`operation:project:list`；`entityType` = `server`|`project`|`component`；返回 `OperationRelationsVo`（关联实体 + 最近 5 条任务）

### 部署与发布 `OperationDeployController` / `OperationFileController` / `OperationCommandController` / `OperationTaskController`

> 字段级说明见 **[operation-deploy-api.md](operation-deploy-api.md)**。

**`OperationDeployController`**（`/operation/deploy`）

- `GET /operation/deploy/presets?serverId=`：`operation:server:list`；常用上传路径 + 快捷后置动作 + **`serviceKeys`**
- `GET /operation/deploy/{serviceKey}/status?serverId=`：`operation:server:list`；SSH 远程或（`allow-local=true` 且 serverId 空）本机 status
- `POST /operation/deploy/{serviceKey}/{action}`：`operation:deploy:exec` + `list`；同步执行（少用）
- `POST /operation/deploy/{serviceKey}/{action}/task?serverId=&projectId=`：`operation:deploy:exec` + `list`；异步启停，返回 `taskId`；可选 `projectId` 关联项目台账
- `POST /operation/deploy/batch/task`：`operation:deploy:exec` + `list`；JSON 批量滚动重启（`steps[]` / `stopOnFailure` / `intervalSeconds`）→ 单父 `deploy_batch` 任务

**`OperationFileController`**（`/operation/file`）

- `POST /operation/file/upload`：`operation:file:upload` + `list`；multipart：`file, serverId, targetPath, postAction?, postCommand?`；`custom` 需 `operation:command:exec`

**`OperationCommandController`**（`/operation/command`）

- `POST /operation/command/exec/task`：`operation:command:exec` + `list`；JSON `{ serverId, command, workDir? }` → `taskId`

**`OperationTaskController`**（`/operation/task`）

- `GET /operation/task/{id}?logOffset=`：`operation:server:list`；轮询进度与增量日志
- `POST /operation/task/{id}/cancel`：`operation:server:list`；协作式取消 pending/running
- `GET /operation/task/list`：`operation:server:list`；分页历史（不含大段 log）；可按 `projectId` 过滤
- `GET /operation/task/groups`：`operation:server:list`；按 `projectId` 分组分页（`tasksPerGroup` 控制组内条数）；可选 `status` 过滤

### 运维健康 `OperationHealthController`（前缀 `/operation/health`，1个）

- `POST /operation/health/probe-all`：`operation:server:list`；**异步**创建 `health_probe` 任务，返回 **`taskId`**（Breaking）；轮询 `GET /operation/task/{id}`；定时调度仍内部同步（`ops.health.probe-enabled` / `ops.health.probe-cron`）

## 5. 当前可见接口风险（用于迭代排期）

- 已修复（2026-05-06）: 字典数据删除接口路径变量绑定与删除 Mapper 误用问题
- 已调整（2026-05-06）: 验证码接口改为配置开关模式（`captcha.enabled`）
- 已调整（2026-06-08）: 系统/运维 Controller 补充 `@RequiresPermissions`，与菜单 perms 对齐
- 已调整（2026-06-08）: 角色授权接口返回刷新提示；授权后清除 Shiro 授权缓存
- 已调整（2026-06-08）: 操作/登录日志菜单 perms 已含于 `docs/sql` 基线
- 控制器层已覆盖主要管理接口权限注解；`/menu/getRouters`、`/dict/data/type/{dictType}`、`/user/profile` 等仍仅要求登录
- 多数接口直接接收 Entity 作为入参，需评估字段越权更新风险

## 6. 建议的下一步（接口维度）

- P1: 补齐验证码校验链路（登录请求校验 code + uuid，并消费 Redis）
- P1: 前端联调角色授权接口的 `msg` 提示（刷新页面后菜单生效）
- P1: 给用户/角色/菜单/字典建立最小 API 回归测试
- P2: 推进 DTO 化与参数校验注解，降低直接暴露实体风险
- P2: 增加接口变更日志（新增/废弃/兼容策略）

## 7. 迭代记录追加模板（接口版）

### 2026-06-08 权限与角色授权

- 新增: `PermissionConstants`、`PermissionService`、`ShiroExceptionHandler`
- 鉴权变更: 系统/运维管理接口启用 `@RequiresPermissions`
- 返回结构变更: `insertUserRole` / `addUserRole` / `removeUsers` 成功时 `msg` 含刷新提示
- 前端联调影响: 无权限时 `code=10009`；角色授权成功需展示 `msg` 并建议用户刷新
- 回归验证: 非授权角色访问管理接口应被拒绝；授权后刷新可见新菜单

### 2026-07-11 部署中心灵活化（SVR-18~20）

- 新增接口: `GET /operation/deploy/presets`、`POST /operation/command/exec/task`；`POST /operation/file/upload` 增 `postCommand`
- 变更接口: `PUT /operation/server/{id}/ssh` 增 `uploadAllowedRoots`；上传 `targetPath` 改手输 + 三层路径白名单
- 鉴权变更: 新增 `operation:command:exec`（`22_operation_command_flex.sql`）
- 配置变更: `ops.command.enabled`、`ops.upload.allow-any-under`
- 契约文档: [operation-deploy-api.md](operation-deploy-api.md)
- 前端联调影响: 部署中心路径/后置/远程命令改 API 驱动；详见 [operation-frontend.md](operation-frontend.md) §11

### [日期-迭代号]

- 新增接口:
- 变更接口:
- 废弃接口:
- 鉴权变更:
- 请求参数变更:
- 返回结构变更:
- 前端联调影响:
- 回归验证:

