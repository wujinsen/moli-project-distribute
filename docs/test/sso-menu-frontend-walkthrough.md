# SSO-MENU-1 · 前端走查清单（meiling-ui）

> **更新**：2026-07-13 · **联合走查通过**（F-SSO-1～6 · S3～S7/S10 ✅；S1/S2/S8/S9 边界项未测）  
> **读者**：meiling-ui 前端、user-center 后端联调  
> **契约**：[sso-menu-frontend-handoff.md](../api/sso-menu-frontend-handoff.md)  
> **前端镜像**：[meiling-ui/docs/test/sso-menu-frontend-walkthrough.md](../../meiling-ui/docs/test/sso-menu-frontend-walkthrough.md)  
> **后端设计**：[sso-menu-system-isolation.md](../design/sso-menu-system-isolation.md)

---

## 0. 环境

| 项 | 期望 |
|----|------|
| user-center | `:8888` 已合入 **SSO-MENU-1** 后端 + 执行 `docs/sql/30_sso_menu_system_id.sql`（P1 backfill） |
| meiling-ui | `:5141` · `VITE_USE_MOCK_AUTH=false` |
| 门户 | `sso.enabled=true`，`sys_system` 有 ≥2 条启用（含 INTERNAL `moli-admin`、EXTERNAL `moli-knowledge`） |
| 账号 | 普通用户分配多系统；超管 `admin` 用于 S6/S7 |
| 代理 | `vite.config.ts` → `8888`（`/menu`、`/system`、`/login`） |

---

## 1. 记录表

| ID | 结果 | 备注 |
|----|------|------|
| F-SSO-1 | ✅ | API+UI：enter 后必有 `getRouters` |
| F-SSO-2 | ✅ | login→`/system-select`；未 enter 时 `getRouters=[]` |
| F-SSO-3 | ✅ | 选 moli-admin 后侧栏与 API 一致 |
| F-SSO-4 | ✅ | SystemSwitcher 切换后 tab 全部消失（手验 2026-07-13） |
| F-SSO-5 | ✅ | 多系统 login `menuVoList=[]` → 选系统页 |
| F-SSO-6 | ✅ | admin 侧栏有 Knowledge Base；无 ChatGPT/BI |
| S1 | ⬜ | 需 `sso.enabled=false` |
| S2 | ⬜ | 需仅一条 INTERNAL 种子 |
| S3 | ✅ | enter admin：6 顶栏；无 500/600；有 900 |
| S4 | ✅ | switch→admin 后 `getRouters` 恢复 6 项 |
| S5 | ✅ | enter 39：`redirectUrl` + `getRouters=[]` |
| S6 | ✅ | admin 运行时无 500/600；zhangsan 仅 1 顶栏 |
| S7 | ✅ | `getMenuTreeAll` count=8 |
| S8 | ⬜ | 需角色仅勾子菜单账号 |
| S9 | ⬜ | 需专测角色 |
| S10 | ✅ | 未 enter `getRouters=[]` |

**走查人**：superadmin / admin · **日期**：2026-07-13 · **8888**：本地 dev · **meiling-ui**：`:5141`

**结论**：主链路（login → 选系统 → enter → SystemSwitcher 切换 → 菜单隔离 + tab 清空）**已通过**；S1/S2/S8/S9 为配置/角色边界，不阻塞交付。

---

## 2. 前端任务走查

### F-SSO-1 · 统一拉菜单

| 操作 | Network | 通过 |
|------|---------|------|
| 登录 enter admin 后进首页 | `GET /menu/getRouters` 200 | 侧栏与响应一致 |
| F5 刷新 | 再次 `getRouters` | 路由可恢复，无 404 |
| DevTools | 无「仅用 enter 内嵌 `menuVoList`、不再请求 getRouters」 | enter/switch 后**必有** `getRouters` |

### F-SSO-2 · Q3 守卫

| 操作 | 通过 |
|------|------|
| 门户多系统 login 后未点系统，直输 `/operation/project` | 重定向 **`/system-select`** |
| DevTools：`getRouters` → `data:[]` | 不渲染旧系统侧栏；`localStorage` 菜单缓存已清 |

### F-SSO-3 · enter / switch

| 操作 | 通过 |
|------|------|
| 选系统 enter **moli-admin** | enter 后**另有** `getRouters`；非仅 enter 响应内嵌树 |
| 顶栏 `SystemSwitcher` 切换系统 | switch 后 `getRouters` + 侧栏变化 |

### F-SSO-4 · 清理态

| 操作 | 通过 |
|------|------|
| admin → 另一 INTERNAL（若有） | 旧业务 tab 已清（`resetPageTabs`） |
| Network | 无旧系统路由组件请求 |
| Vue Router | 旧动态 `addRoute` 已 `removeRoute` |

### F-SSO-5 · login

| 操作 | 通过 |
|------|------|
| 多系统 login | `menuVoList=[]`，进选系统 |
| 单 INTERNAL login | 自动 enter + 侧栏正常 |
| 门户关闭 login | 直进主页，菜单与现网一致 |

### F-SSO-6 · 知识库 Q5-A

| 操作 | 通过 |
|------|------|
| enter **moli-admin**，角色有 KB | 侧栏 **900 企业知识库**，`/knowledge/browse` 可开 |
| enter **moli-knowledge (39)** | 浏览器跳转 `redirectUrl`；无 meiling-ui 侧栏 KB |
| admin 侧栏 | **无** ChatGPT(500)/BI(600) 段（若后端 backfill 正确） |
| KB API | 页面内请求仍走 `8090` / 网关，与菜单隔离无关 |

---

## 3. 后端联动场景（S1–S10）

| ID | 步骤 | 期望 |
|----|------|------|
| S1 | `sso.enabled=false` 登录 | 与现网全量菜单一致 |
| S2 | 仅 `moli-admin` 一条 INTERNAL，登录 | 自动 enter；`menuVoList` / `getRouters` 仅 system_id=1 段 |
| S3 | 多系统用户 enter admin | 无 500/600；**有 900**（有权限） |
| S4 | `POST /system/switch` → `getRouters` | 菜单随 `currentSystemId` 变 |
| S5 | enter 39 | 空菜单 + `redirectUrl`；无 meiling-ui 动态 KB 路由 |
| S6 | superadmin enter `moli-admin` | 运行时侧栏无其它系统段（500/600 等） |
| S7 | 菜单管理 `GET /menu/getMenuTreeAll` | 仍见**跨系统全树** |
| S8 | 角色只勾 401 等子菜单 | 侧栏含父目录 + 子项，且仅当前系统 |
| S9 | 子菜单 system_id=1，父目录同系统 | 树结构完整，无孤儿叶子 |
| S10 | 门户开启、清 Session 系统后直接 `getRouters` | `[]` + 前端跳 **`/system-select`** |

---

## 4. 冒烟顺序（约 20–30 分钟）

```text
1. S1 或 S2（基线）
2. F-SSO-5 → F-SSO-2 → S10（Q3）
3. F-SSO-3 + S3 + F-SSO-6（admin + KB）
4. S4 + F-SSO-4（switch）
5. S5（EXTERNAL 39）
6. S6 + S7（超管）
```

---

## 5. 相关

- [sso-menu-frontend-handoff.md](../api/sso-menu-frontend-handoff.md) — F-SSO-1～6 · `reloadRoutesFromServer`
- [frontend-backend-dependencies.md](../api/frontend-backend-dependencies.md) §5
- [sso-frontend-dev-guide.md](../../meiling-ui/docs/sso-frontend-dev-guide.md)

---

## 6. 自动化冒烟（可选复跑）

```powershell
powershell -File docs/test/_sso_walkthrough_api.ps1
```

前置：`system_id` 已 backfill（46/47）· `Authorization` 头传 login `token` · `sso.enabled=true`。
