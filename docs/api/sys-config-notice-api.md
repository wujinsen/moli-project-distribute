# 参数设置 / 通知公告 · HTTP API 契约

> **状态**：后端已实现（前端待接） · **更新**：2026-08-16  
> **服务**：`moli-user-center-server` · 前缀 `/config`、`/notice`  
> **设计**：[`sys-config-notice.md`](../design/sys-config-notice.md)  
> **迁移**：[`38_sys_config.sql`](../sql/38_sys_config.sql) · [`39_sys_notice.sql`](../sql/39_sys_notice.sql)

---

## 0. 读前须知（两个与其它模块不同的地方）

1. **参数不能新建**。`/config` 没有 `POST`。参数在代码 `ConfigKey` 枚举里声明，
   页面能做的只是「改值」和「重置为默认」。因此也没有 `system:config:add` 权限。
2. **公告有两套接口**。`/notice/*` 是后台管理（要 `system:notice:*` 权限，可见草稿）；
   `/notice/feed*` 是给所有登录用户的阅读侧（无 perms，只可见已发布且未过期）。
   前端**不要**用后台接口渲染通知栏，否则普通用户会收到 403。

---

## 1. 权限

| perm_code | 用途 |
|-----------|------|
| `system:config:list` | 菜单 8、参数列表 |
| `system:config:edit` | `PUT /config`（需与 `list` AND） |
| `system:config:remove` | `DELETE /config/{configKey}` 重置为默认（需与 `list` AND） |
| `system:notice:list` | 菜单 9、后台列表与详情 |
| `system:notice:add` | `POST /notice`（需与 `list` AND） |
| `system:notice:edit` | `PUT /notice`、发布、撤回（需与 `list` AND） |
| `system:notice:remove` | `DELETE /notice/{ids}`（需与 `list` AND） |

`list` 权限来自 `sys_menu.perms`（同 `system:post:*` 惯例），写操作在 `sys_action`。  
菜单：`sys_menu.id = 8`「参数设置」、`id = 9`「通知公告」，父级 1「系统管理」。  
迁移脚本会为角色 1（超级管理员）、2（系统管理员）补齐授权，**执行后需重新登录**。

---

## 2. 参数设置 `/config`

### 2.1 `GET /config/list`

参数列表。**数据源是代码注册表 ∪ 覆盖值表**，所以从未被改过的参数也会返回 —— 运维第一次进
页面就能看到系统有哪些开关，而不是一张空表。**无分页**（参数是数十条量级）。

| 查询参数 | 类型 | 必填 | 说明 |
|----------|------|------|------|
| `group` | string | 否 | 分组过滤：`SECURITY` / `PORTAL` / `OPS`；为空返回全部 |

响应 `data` 为 `ConfigItemVo[]`：

| 字段 | 类型 | 说明 |
|------|------|------|
| `configKey` | string | 参数键名，同时是业务主键 |
| `effectiveValue` | string | 当前生效值 |
| `defaultValue` | string | 代码声明的默认值 |
| `valueType` | string | `BOOLEAN` / `INT` / `STRING`，前端据此渲染控件 |
| `groupCode` | string | `SECURITY` / `PORTAL` / `OPS` |
| `groupName` | string | 分组展示名，可直接用于左侧分区标题 |
| `description` | string | 参数说明 |
| `source` | string | `DB_OVERRIDE` / `ENVIRONMENT` / `DEFAULT`，见 §2.4 |
| `overridden` | boolean | 是否存在覆盖值；`true` 时展示「重置为默认」按钮 |

**前端渲染建议**：`valueType=BOOLEAN` 用开关，`INT` 用数字输入，`STRING` 用文本框。
不要自己维护「哪个 key 是开关」的映射表 —— 类型由后端声明下发。

### 2.2 `PUT /config`

设置覆盖值。

```json
{ "configKey": "captcha.enabled", "configValue": "true" }
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `configKey` | string | 是 | 必须是已声明的 key，否则 400 |
| `configValue` | string | 是 | 一律用**字符串**传，后端按声明类型解析 |

失败情形（返回错误码 + `msg`，可直接展示）：

- key 未在注册表中声明 → `参数 xxx 未在 ConfigKey 注册表中声明，不能设置`
- 值不符合声明类型 → 如 `BOOLEAN` 传 `maybe` → `只能是 true 或 false`
- 值为空白 → `参数值不能为空；如需恢复默认值请使用重置`

**生效时机**：写库成功即清缓存，下一次读取立刻生效，**无需重启**。

### 2.3 `DELETE /config/{configKey}`

重置为默认值 —— **删除的是覆盖行，不是参数**。参数由代码声明，永远存在；
重置后生效值回落到 yaml 或声明默认值。对未被覆盖的参数重复调用不报错（幂等）。

### 2.4 生效值来源（`source` 字段的含义）

后端按四级顺序取值，`source` 表示命中了哪一级：

| source | 含义 | 前端提示 |
|--------|------|----------|
| `DB_OVERRIDE` | 有人在页面上改过 | 「已自定义」，可重置 |
| `ENVIRONMENT` | 来自 yaml / Nacos | 「来自部署配置」 |
| `DEFAULT` | 既没改过也没配过 | 「默认值」 |

运维需要这个字段来判断「这个值是我改的，还是部署时就这样」。

### 2.5 首批参数

| configKey | 类型 | 默认 | 分组 | 说明 |
|-----------|------|------|------|------|
| `captcha.enabled` | BOOLEAN | `false` | SECURITY | 登录验证码开关 |
| `sso.enabled` | BOOLEAN | `true` | PORTAL | 多系统门户开关 |
| `ops.command.enabled` | BOOLEAN | `false` | OPS | 运维远程命令开关（安全开关，事故时需能立即关停） |
| `ops.health.probe-enabled` | BOOLEAN | `true` | OPS | 服务器健康巡检定时任务开关 |

新增参数是**代码变更**（加一个枚举常量），不需要写 SQL，也不需要前端改动 ——
列表是注册表驱动的，新参数会自动出现在页面上。

---

## 3. 通知公告 · 后台管理 `/notice`

### 3.1 `GET /notice/list`

后台分页列表，含草稿与已撤回。**不返回正文**（避免列表响应被长 Markdown 撑大）。

| 查询参数 | 类型 | 说明 |
|----------|------|------|
| `pageNum` / `pageSize` | int | 分页 |
| `noticeTitle` | string | 标题模糊匹配 |
| `noticeType` | int | `1` 通知 / `2` 公告 / `3` 维护 |
| `status` | int | `0` 草稿 / `1` 已发布 / `2` 已撤回 |

响应为 `PageRes<SysNotice>`，按 `createTime` 倒序。

### 3.2 `GET /notice/{id}`

后台详情，任意状态可见，**含正文**。

### 3.3 `POST /notice`

新增。**强制落草稿**：请求里的 `status` 与 `publishTime` 会被忽略，
状态流转只能走发布/撤回接口 —— 否则「新建即全员可见」会绕过发布这道确认。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `noticeTitle` | string | 是 | 标题 |
| `noticeType` | int | 是 | 对应字典 `sys_notice_type` |
| `noticeContent` | string | 否 | **Markdown 源文** |
| `topFlag` | int | 否 | `1` 置顶 / `0` 普通 |
| `expireTime` | datetime | 否 | 不传=长期有效；**不能是过去时间** |

响应 `data` 为新公告 id。

### 3.4 `PUT /notice`

修改。同样忽略 `status` / `publishTime`。`id` 必填。

### 3.5 `PUT /notice/publish/{id}`

发布：置为已发布并写入 `publishTime`，阅读侧立即可见。

- 只允许从**草稿**或**已撤回**发布。已发布状态重复发布会被拒绝 ——
  重复发布会刷新 `publishTime`，把旧公告在所有人的通知栏里顶成未读。
- 若 `expireTime` 已过去会被拒绝（发出去就永不可见）。

### 3.6 `PUT /notice/revoke/{id}`

撤回：置为已撤回，阅读侧立即不可见，但保留「曾经发布过」的痕迹。仅已发布可撤回。

### 3.7 `DELETE /notice/{ids}`

批量物理删除，逗号分隔。**仅用于清理误建草稿**；已发布内容要下线请用撤回。

### 3.8 状态机

```
草稿(0) --publish--> 已发布(1) --revoke--> 已撤回(2)
                          ^                    |
                          +------publish-------+
```

`status` 只能由 publish / revoke 改变，新增/编辑接口一律不改它。

---

## 4. 通知公告 · 阅读侧 `/notice/feed`（仅需登录）

公告的意义是全员可见，这三个接口**不挂 perms**，只要登录即可访问。

### 4.1 `GET /notice/feed`

当前有效公告 + 未读数。有效 = 已发布 且（无过期时间 或 未过期）。
排序：置顶优先，然后按发布时间倒序。

响应 `data`：

| 字段 | 类型 | 说明 |
|------|------|------|
| `list` | `NoticeBriefVo[]` | 有效公告，**不含正文** |
| `unreadCount` | int | 未读数量，可直接用于角标 |

`NoticeBriefVo`：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | 公告 id |
| `noticeTitle` | string | 标题 |
| `noticeType` | int | 类型 |
| `topFlag` | int | 是否置顶 |
| `publishTime` | datetime | 发布时间 |
| `expireTime` | datetime | 过期时间，可能为 null |
| `unread` | boolean | 相对当前用户已读水位是否未读 |

列表与角标合在一个响应里返回，前端渲染通知栏只需一次请求，也不会出现
「列表与角标来自两个时刻」的不一致。

### 4.2 `GET /notice/feed/{id}`

阅读侧详情，含正文。**只返回已发布且未过期的公告**；草稿与已撤回一律报
「公告不存在或已不可见」，不区分两种情况（避免被用来探测草稿是否存在）。

### 4.3 `PUT /notice/feed/read`

把当前用户的已读水位推进到此刻，无请求体。调用后 `unreadCount` 归零。

**未读机制是「水位」而非逐条标记**：后端一个用户只存一行 `last_read_time`，
`publishTime > last_read_time` 即算未读。因此：

- 能表达「X 时刻之前的都已读」，**不能**乱序标记单条已读；
- 前端应在用户打开通知面板时调用一次，而不是每条公告点开都调；
- 好处是行数只随用户数增长，不随「用户数 × 公告数」增长。

---

## 5. 相关

- 设计：[`../design/sys-config-notice.md`](../design/sys-config-notice.md)
- ER 图：[`../diagrams/moli-sys-config-notice-er.drawio`](../diagrams/moli-sys-config-notice-er.drawio)
- 表结构：[`../sql/USER_CENTER_SCHEMA.md`](../sql/USER_CENTER_SCHEMA.md)
- API 总索引：[`user-center-api-map.md`](user-center-api-map.md)
