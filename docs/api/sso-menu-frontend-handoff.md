# SSO 菜单隔离 · 前端开工手册（meiling-ui · SSO-MENU-1）

> **更新**：2026-07-13（**Q3/Q5 已定案** · 可与后端 P0 **并行开工**）  
> **任务 ID**：**SSO-MENU-1**（P2）  
> **后端设计**：[sso-menu-system-isolation.md](../design/sso-menu-system-isolation.md) · SQL：`docs/sql/30_sso_menu_system_id.sql`  
> **给后端**：[frontend-backend-dependencies.md](frontend-backend-dependencies.md) §5  
> **走查**：[sso-menu-frontend-walkthrough.md](../test/sso-menu-frontend-walkthrough.md)  
> **meiling-ui 镜像**：建议同步到 `meiling-ui/docs/api/sso-menu-frontend-handoff.md`

本地：`http://127.0.0.1:5141` → proxy `8888` · `admin`/`123456` · `VITE_USE_MOCK_AUTH=false`  
门户开关：`sso.enabled=true` 且 DB 有启用行 `sys_system`（见走查 §0）

---

## 0. 给前端一句话

> 1. **运行时菜单只看当前系统** — `enter`/`switch` 成功后**必须**再调 `GET /menu/getRouters`，**清空**旧动态路由后重建；勿长期缓存 login/enter 里的 `menuVoList`。  
> 2. **未选系统**（门户多系统、Session 无 `currentSystemId`）— `getRouters` 返回 **`[]`**（Q3-A）→ 路由守卫跳**选系统页**，勿用上一次系统的侧栏。  
> 3. **知识库 900** — 仍挂在 **moli-admin** 侧栏（Q5-A）；门户 enter **moli-knowledge(39)** 走 `redirectUrl`，与 admin 内嵌是**两条入口**，前端各走各的分支。  
> 4. **全量 E2E** 依赖后端 `system_id` 过滤上线；守卫与刷新逻辑可**先写**，用 mock 或旧后端自测守卫分支。

---

## 1. 前后端分工

| 侧 | 内容 | 状态 |
|----|------|------|
| **后端 P0** | `sys_menu.system_id` · `resolveRoutersForCurrentSystem` · 三处委托 | ⬜ 待实现 |
| **后端 P1** | backfill SQL · 新菜单 INSERT 带 `system_id` | ⬜ 待实现 |
| **前端 P2** | 本稿 F-SSO-1～6 · 走查 S1–S10 | ⬜ **可开工** |

**可并行**：前端先改 `permission`/`router`/`system` 模块；联调前确认 `:8888` 已合入 SSO-MENU-1 后端 commit。

---

## 2. 产品定案（实现必遵）

| # | 结论 | 前端影响 |
|---|------|----------|
| **Q3-A** | 门户开启且未 `enter` → `getRouters` = **`[]`** | 全局守卫：空树 + `systemPortalEnabled` → `/system/select`（或现有选系统路由） |
| **Q5-A** | 900 段 `system_id=1`（admin 内嵌） | enter **moli-admin** 后侧栏**仍可有**「企业知识库」；enter **39** 不注册 KB 动态路由，只 `window.location` / `redirectUrl` |
| 门户关闭 | 过滤不生效，行为同现网 | 无需选系统；login 直出 `menuVoList` |
| 唯一 INTERNAL | login 自动 `enter`，有过滤后的菜单 | 与现网一致，注意仍要在 layout 用 `getRouters` 刷新 |

---

## 3. API 契约（user-center `:8888`）

前缀 dev 直连：`/menu`、`/system`、`/login`（不经 Gateway 时无 `/UserCenter` 前缀；经网关见 [frontend-routes-map.md](frontend-routes-map.md) §1）。

### 3.1 `POST /login`

| 场景 | 关键字段 |
|------|----------|
| 门户关闭 | `menuVoList` + `permissions` 直出 |
| 门户开启 · 多系统 | `systemPortalEnabled=true`，`systemList[]`，**`menuVoList=[]`** |
| 门户开启 · 唯一 INTERNAL | 自动 enter：`currentSystem` + **已过滤** `menuVoList` |

### 3.2 `POST /system/enter` · `POST /system/switch`

| `ssoMode` | 响应 | 前端 |
|-----------|------|------|
| **INTERNAL** | `currentSystem` + `menuVoList`（过滤后）+ `permissions` | 见 §4.2：**仍要** `getRouters` 重建路由 |
| **EXTERNAL** | `menuVoList=[]`，`redirectUrl` + `hubToken` | **不** `addRoutes`；跳转 `redirectUrl` |

### 3.3 `GET /menu/getRouters`

| 项 | 说明 |
|----|------|
| 过滤依据 | Session **`currentSystemId`**（后端实现后生效） |
| 未 enter | **`data: []`**（Q3-A） |
| 形状 | 不变，`List<MenuVo>` 树 |
| 轮询 | 无；仅在 login / enter / switch / 手动刷新时调用 |

```typescript
// src/api/system/menu.ts（路径按 meiling-ui 实际调整）
export const getRoutersApi = () =>
  request<MenuVo[]>('/menu/getRouters')

export const enterSystemApi = (systemId: number) =>
  request<SystemEnterVo>('/system/enter', { method: 'POST', data: { systemId } })

export const switchSystemApi = (systemId: number) =>
  request<SystemEnterVo>('/system/switch', { method: 'POST', data: { systemId } })
```

---

## 4. 前端任务清单

### F-SSO-1 · 统一「拉菜单 + 注册路由」

抽一个函数（命名示例 `reloadRoutesFromServer`），**唯一**入口负责：

1. `GET /menu/getRouters`
2. 若 `data.length === 0` 且门户开启 → 走 F-SSO-2，**return**
3. `permissionStore.setRoutes(routes)` / `setPermissions`
4. `resetRouter()` → `addRoutes`（或项目现有 `generateRoutes` 流程）
5. Pinia 写入 `currentSystem`（若调用方未写）

**调用点**（至少）：

- 应用 layout **首次挂载**（有 token、已 enter）
- `enterSystem` / `switchSystem` **成功之后**
- 可选：浏览器刷新（F5）后 layout 恢复态

```typescript
export async function reloadRoutesFromServer() {
  const { data: routes } = await getRoutersApi()
  const userStore = useUserStore()
  if (!routes?.length && userStore.systemPortalEnabled && !userStore.currentSystem?.id) {
    await router.replace('/system/select') // 项目实际选系统 path
    return false
  }
  const permissionStore = usePermissionStore()
  permissionStore.setRoutes(routes)
  resetRouter()
  const accessRoutes = permissionStore.generateRoutes(routes)
  accessRoutes.forEach((r) => router.addRoute(r))
  return true
}
```

### F-SSO-2 · 未 enter 守卫（Q3-A）

| 条件 | 动作 |
|------|------|
| `systemPortalEnabled && !currentSystem?.id` | 除白名单（login、选系统、sso callback）外 → **选系统页** |
| `getRouters` 返回 `[]` 且门户开启 | 同上；**禁止**展示上一次缓存的侧栏 |

白名单建议：`/login`、`/system/select`、`/sso/*`。

### F-SSO-3 · enter / switch 后强制刷新（P2 核心）

```typescript
export async function onEnterOrSwitchSystem(systemId: number, isSwitch = false) {
  const api = isSwitch ? switchSystemApi : enterSystemApi
  const { data } = await api(systemId)
  const userStore = useUserStore()
  userStore.setCurrentSystem(data.currentSystem)

  if (data.redirectUrl) {
    // EXTERNAL · Q5：第二入口，不注册路由
    window.location.href = data.redirectUrl
    return
  }

  // INTERNAL：勿仅信任 data.menuVoList
  await reloadRoutesFromServer()
  await router.replace('/') // 或默认首页
}
```

### F-SSO-4 · 切换系统清理态

| 项 | 建议 |
|----|------|
| 动态路由 | `resetRouter()`（F-SSO-1 已含） |
| Tags / keep-alive | 清空或保留首页 tab（产品可选，走查记一笔） |
| `permissions` | 与 `getRouters` 同批更新；若仍用 enter 的 `permissions`，switch 后应重拉 |

### F-SSO-5 · login 多系统分支

现网若 login 后 `menuVoList=[]` 已跳选系统 — **保持**；确认：

- 不会用空数组调用 `addRoutes` 后仍显示旧路由
- 唯一 INTERNAL 自动 enter 后仍走 **F-SSO-1**（双保险）

### F-SSO-6 · 知识库 · Q5-A 验收点

| 场景 | 期望 |
|------|------|
| enter **moli-admin** + 角色有 KB 权限 | 侧栏有 **900 企业知识库**；`/knowledge/*` 可进 |
| enter **moli-knowledge (39)** | `redirectUrl` 跳转；meiling-ui **不**依赖 900 出现在 `getRouters` |
| 从 admin 切到其它 INTERNAL（若有 BI 等） | 侧栏**无** 900（900 仅 `system_id=1`） |

知识库 API 仍走 `KnowledgeServer`；本任务**只改菜单来源与路由刷新**，不改 KB 页面。

---

## 5. 建议改动文件（meiling-ui）

> 路径为惯例命名，以仓库实际为准。

| ID | 区域 | 文件（示例） |
|----|------|----------------|
| F-SSO-1 | API | `src/api/system/menu.ts`、`src/api/system/system.ts` |
| F-SSO-1 | 路由 | `src/permission.ts` 或 `src/router/guard.ts` |
| F-SSO-1 | Store | `stores/permission.ts`、`stores/user.ts`（`currentSystem`、`systemPortalEnabled`） |
| F-SSO-3 | 视图 | 选系统页、`SystemSwitch` 组件 |
| F-SSO-4 | 布局 | `layout/index.vue`（挂载时 `reloadRoutesFromServer`） |

---

## 6. 验收 / 走查

完整步骤：[sso-menu-frontend-walkthrough.md](../test/sso-menu-frontend-walkthrough.md)

| ID | 场景 | 通过 |
|----|------|------|
| S4 | switch 后 `getRouters` | 侧栏菜单集变化，无串台 |
| S10 | 未 enter 调 `getRouters` | 空树 → 跳选系统 |
| S3 | enter admin | 有运营/系统段；**有 900**（有权限时）；**无** 500/600 段 |
| S5 | enter knowledge 39 | `redirectUrl`，无动态路由注册 |

---

## 7. 联调顺序

```text
① 前端合入 F-SSO-1～5（守卫 + reloadRoutesFromServer）
② 后端合入 SSO-MENU-1 P0+P1 + 执行 30_sso_menu_system_id.sql
③ 8888 重启 · 门户开启 · 多系统账号走查 S1–S10
④ meiling-ui 同步本文到 docs/api/
```

---

## 8. 相关文档

| 文档 | 用途 |
|------|------|
| [sso-menu-system-isolation.md](../design/sso-menu-system-isolation.md) | 后端算法 · Q3/Q5 · 测试 S1–S10 |
| [user-center-api-map.md](user-center-api-map.md) | System / Menu HTTP 索引 |
| [frontend-routes-map.md](frontend-routes-map.md) | menu_id ↔ 路由 ↔ API |
| [KNOWLEDGE_API.md §1.5](KNOWLEDGE_API.md#15-左侧菜单getrouters) | 900 段与 Q5-A |
| [portal-system-group.md](../design/portal-system-group.md) | 选系统页分组 |
