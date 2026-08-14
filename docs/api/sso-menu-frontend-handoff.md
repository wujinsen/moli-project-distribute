# SSO 菜单隔离 · 前端开工手册（meiling-ui · SSO-MENU-1）

> **更新**：2026-07-13（**Q3/Q5 已定案** · 后端 P0/P1 **已实现** · 前端 F-SSO **已落地** · **联合走查通过**）  
> **任务 ID**：**SSO-MENU-1**（P2）  
> **前端仓库**：`meiling-ui` · 镜像 [meiling-ui/docs/api/sso-menu-frontend-handoff.md](../../meiling-ui/docs/api/sso-menu-frontend-handoff.md)  
> **后端设计**：[sso-menu-system-isolation.md](../design/sso-menu-system-isolation.md) · SQL：`docs/sql/30_sso_menu_system_id.sql`  
> **给后端**：[frontend-backend-dependencies.md](frontend-backend-dependencies.md) §5  
> **走查**：[sso-menu-frontend-walkthrough.md](../test/sso-menu-frontend-walkthrough.md)  
> **SSO 总览**（前端仓）：[meiling-ui/docs/sso-frontend-dev-guide.md](../../meiling-ui/docs/sso-frontend-dev-guide.md)

本地：`http://127.0.0.1:5141` → proxy `8888` · `admin`/`123456` · `VITE_USE_MOCK_AUTH=false`  
门户开关：`sso.enabled=true` 且 DB 有启用行 `sys_system`（见走查 §0）

---

## 0. 给前端一句话

> 1. **运行时菜单只看当前系统** — `enter`/`switch` 成功后**必须**再调 `GET /menu/getRouters`，**清空**旧动态路由后重建；勿长期缓存 login/enter 里的 `menuVoList`。  
> 2. **未选系统**（门户多系统、Session 无 `currentSystemId`）— `getRouters` 返回 **`[]`**（Q3-A）→ 路由守卫跳 **`/system-select`**，勿用上一次系统的侧栏。  
> 3. **知识库 900** — 仍挂在 **moli-admin** 侧栏（Q5-A）；门户 enter **moli-knowledge(39)** 走 `redirectUrl`，与 admin 内嵌是**两条入口**。  
> 4. **联调** 需 `:8888` 合入 SSO-MENU-1 后端 + 执行 `30_sso_menu_system_id.sql`；dev fallback（`msg: 使用前端默认菜单`）与真·空树 `[]` 须区分。

---

## 1. 前后端分工

| 侧 | 内容 | 状态 |
|----|------|------|
| **后端 P0/P1** | `sys_menu.system_id` · `resolveRoutersForCurrentSystem` · `getRouters` / `enter` / `switch` 委托 | ✅ 已实现 |
| **前端 F-SSO-1～6** | `reloadRoutesFromServer` · 守卫 · enter/switch · 单测 | ✅ **已落地**（meiling-ui） |
| **联合走查** | 走查稿 S1–S10 + F-SSO-1～6 | ✅ **已通过**（2026-07-13；S1/S2/S8/S9 边界未测） |

**代码落点（meiling-ui，2026-07-13）**：

| 能力 | 文件 |
|------|------|
| 统一拉菜单 | `src/composables/usePermission.ts` → `reloadRoutesFromServer` |
| Q3 守卫 | `src/router/index.ts` · `needsSystemSelect` |
| enter/switch | `src/composables/useSystemPortal.ts` → `applyEnterResult` |
| dev fallback 区分 | `src/api/menu.ts` → `MENU_DEV_FALLBACK_MSG` |
| 单测 | `src/composables/reloadRoutesFromServer.spec.ts` |

---

## 2. 产品定案（实现必遵）

| # | 结论 | 前端影响 |
|---|------|----------|
| **Q3-A** | 门户开启且未 `enter` → `getRouters` = **`[]`** | 守卫：空树 + `portalEnabled` → `/system-select`；**禁止**缓存侧栏 |
| **Q5-A** | 900 段 `system_id=1`（admin 内嵌） | enter **moli-admin** 侧栏可有 KB；enter **39** 只 `redirectUrl` |
| 门户关闭 | 过滤不生效 | login 直出 `menuVoList` |
| 唯一 INTERNAL | login 自动 enter | layout 仍 `reloadRoutesFromServer` 双保险 |

---

## 3. API 契约（user-center `:8888`）

dev 直连：`/menu`、`/system`、`/login`（Vite proxy → `8888`）。经 Gateway 前缀见 [frontend-routes-map.md](frontend-routes-map.md) §1。

### 3.1 `POST /login`

| 场景 | 关键字段 |
|------|----------|
| 门户关闭 | `menuVoList` + `permissions` 直出 |
| 门户开启 · 多系统 | `systemPortalEnabled=true`，`systemList[]`，**`menuVoList=[]`** |
| 门户开启 · 唯一 INTERNAL | 自动 enter：`currentSystem` + **已过滤** `menuVoList` |

### 3.2 `POST /system/enter` · `POST /system/switch`

| `ssoMode` | 响应 | 前端 |
|-----------|------|------|
| **INTERNAL** | `currentSystem` + `menuVoList`（过滤后）+ `permissions` | **仍要** `getRouters`（勿仅信 `menuVoList`） |
| **EXTERNAL** | `menuVoList=[]`，`redirectUrl` | **不** `addRoute`；`window.location.href = redirectUrl` |

### 3.3 `GET /menu/getRouters`

| 项 | 说明 |
|----|------|
| 过滤依据 | Session **`currentSystemId`**（SSO-MENU-1 已生效） |
| 未 enter | **`data: []`**（Q3-A） |
| 形状 | 不变，`MenuVo[]` 树 |

### 3.4 本仓 API 落点（meiling-ui）

```typescript
// src/api/menu.ts
import { request } from '@/api/http'
import type { MenuVo, MoliResult } from '@/types/api'

export const MENU_DEV_FALLBACK_MSG = '使用前端默认菜单'

export async function getRoutersApi(): Promise<MoliResult<MenuVo[]>> {
  try {
    const result = await request<MenuVo[]>('/menu/getRouters', { method: 'GET' })
    if (result.code === API_SUCCESS_CODE) return result
  } catch { /* 后端未就绪 */ }
  return { code: API_SUCCESS_CODE, msg: MENU_DEV_FALLBACK_MSG, data: getDefaultMenus() }
}

// src/api/system.ts
export async function enterSystemApi(systemId: number | string) {
  return request<SystemEnterVo>('/system/enter', {
    method: 'POST',
    body: jsonEntityBody({ systemId }),
  })
}
```

---

## 4. 前端任务清单（F-SSO-1～6）

> 技术栈：**composables**（无 Pinia）。动态路由在 `usePermission.ts`；门户在 `useSystemPortal.ts`。

### F-SSO-1 · `reloadRoutesFromServer` ✅

1. `GET /menu/getRouters`
2. 空树 + 门户开启 + 无 `currentSystem` + **非** dev fallback → `{ ok: false, needsSystemSelect: true }`
3. `resetDynamicRoutes()` → `applyMenusToRouter`
4. 调用点：`useSystemPortal.applyEnterResult`、`router/index.ts` 守卫

```typescript
// usePermission.ts（摘要）
if (!menuSource.length && portalOn && !current?.id && !isFallback) {
  await resetDynamicRoutes()
  return { ok: false, needsSystemSelect: true }
}
```

> **注**：`msg === '使用前端默认菜单'` 时为 dev fallback，**不**触发 Q3 守卫。

### F-SSO-2 · 未 enter 守卫（Q3-A）✅

- 白名单：`login`、`system-select`（`meta.skipMenuGuard`）
- `router/index.ts`：`needsSystemSelect` → `/system-select`

### F-SSO-3 · enter / switch 后强制刷新 ✅

`useSystemPortal.applyEnterResult`：EXTERNAL → redirect；INTERNAL → `reloadRoutesFromServer({ force: true })`。

### F-SSO-4 · 切换清理态 ✅

`resetDynamicRoutes` · `resetPageTabs` · `clearMenus` / `saveMenus`

### F-SSO-5 · login 多系统 ✅

`handlePostLogin`：无 `currentSystem` → `/system-select`；门户关闭 → `reloadRoutesFromServer`

### F-SSO-6 · 知识库 Q5-A ✅

| 场景 | 期望 | 走查 |
|------|------|------|
| enter **moli-admin** + KB 权限 | 侧栏 **900**；`/knowledge/browse` 可进 | ✅ |
| enter **39** | `redirectUrl`；不依赖 `getRouters` 的 900 | ✅ API |
| 切到其它 INTERNAL | 侧栏**无** 900（`system_id=1` 仅 admin） | ✅ SystemSwitcher |

`knowledgeSupplementRoutes` 仅在已有 Knowledge **父节点** 下补子项，不会凭空注入 900 根目录。

---

## 5. 建议改动文件（meiling-ui）

| ID | 文件 | 状态 |
|----|------|------|
| F-SSO-1 | `src/composables/usePermission.ts` · `src/api/menu.ts` | ✅ |
| F-SSO-2 | `src/router/index.ts` | ✅ |
| F-SSO-3 | `src/composables/useSystemPortal.ts` · `SystemSelectView` · `SystemSwitcher` | ✅ |
| F-SSO-4 | `src/utils/authSession.ts` · `usePageTabs.ts` | ✅ |
| F-SSO-5 | `useAuth.ts` / `handlePostLogin` | ✅ |
| F-SSO-6 | — | ✅ |

---

## 6. 验收 / 走查

完整勾选：[sso-menu-frontend-walkthrough.md](../test/sso-menu-frontend-walkthrough.md)

| ID | 场景 |
|----|------|
| S4 | switch 后菜单不串台 |
| S10 | 未 enter → `[]` → `/system-select` |
| S3 | enter admin：有 900、无 500/600 |
| S5 | enter 39 → redirect |

---

## 7. 联调顺序

```text
① 老库执行 docs/sql/30_sso_menu_system_id.sql（或新环境 moli.sql 基线）
② 8888 install+重启（含 resolveRoutersForCurrentSystem）
③ meiling-ui :5141 · VITE_USE_MOCK_AUTH=false
④ 走查稿 §4 冒烟顺序（约 20–30 分钟）
```

---

## 8. 相关文档

| 文档 | 用途 |
|------|------|
| [sso-menu-system-isolation.md](../design/sso-menu-system-isolation.md) | 后端算法 · S1–S10 |
| [sso-frontend-dev-guide.md](../../meiling-ui/docs/sso-frontend-dev-guide.md) | 门户 · enter/switch |
| [portal-system-group-ui.md](../../meiling-ui/docs/portal-system-group-ui.md) | 选系统页分组 |
| [user-center-api-map.md](user-center-api-map.md) | System / Menu HTTP |
| [frontend-gaps.md](../frontend-gaps.md) §2 | 排期索引 |

> **废弃**：meiling-ui `per-system-menu-isolation.md`（2026-06-22）已被本稿 + 设计稿取代；Q5 以 **admin 内嵌（Q5-A）** 为准。
