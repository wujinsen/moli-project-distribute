# SSO-MENU-1 · 前端走查清单（meiling-ui）

> **更新**：2026-07-13  
> **读者**：meiling-ui 前端、user-center 后端联调  
> **契约**：[sso-menu-frontend-handoff.md](../api/sso-menu-frontend-handoff.md)  
> **后端设计**：[sso-menu-system-isolation.md](../design/sso-menu-system-isolation.md)

---

## 0. 环境

| 项 | 期望 |
|----|------|
| user-center | `:8888` 已合入 **SSO-MENU-1** 后端 + 执行 `docs/sql/30_sso_menu_system_id.sql`（P1 backfill） |
| meiling-ui | `:5141` · `VITE_USE_MOCK_AUTH=false` |
| 门户 | `sso.enabled=true`，`sys_system` 有 ≥2 条启用（含 INTERNAL `moli-admin`、EXTERNAL `moli-knowledge`） |
| 账号 | 普通用户分配多系统；超管 `admin` 用于 S6/S7 |

---

## 1. 记录表

| ID | 结果 | 备注 |
|----|------|------|
| F-SSO-1 | ⬜ | `reloadRoutesFromServer` 统一入口 |
| F-SSO-2 | ⬜ | 未 enter / 空 `getRouters` → 选系统 |
| F-SSO-3 | ⬜ | enter/switch 后强制 `getRouters` |
| F-SSO-4 | ⬜ | switch 清动态路由 / tabs |
| F-SSO-5 | ⬜ | login 多系统不误注册旧路由 |
| F-SSO-6 | ⬜ | Q5-A：admin 有 900 · 39 走 redirect |
| S1 | ⬜ | 门户关闭回归 |
| S3 | ⬜ | enter admin 菜单范围 |
| S4 | ⬜ | switch 不串台 |
| S5 | ⬜ | EXTERNAL 39 |
| S10 | ⬜ | 未 enter 空树 |

**走查人**：　**日期**：　**8888 commit**：　**meiling-ui commit**：

---

## 2. 前端任务走查

### F-SSO-1 · 统一拉菜单

| 操作 | Network | 通过 |
|------|---------|------|
| 登录 enter admin 后进首页 | `GET /menu/getRouters` 200 | 侧栏与响应一致 |
| F5 刷新 | 再次 `getRouters` | 路由可恢复，无 404 |

### F-SSO-2 · Q3 守卫

| 操作 | 通过 |
|------|------|
| 门户多系统 login 后未点系统，直输 `/operation/project` | 重定向选系统页 |
| DevTools：`getRouters` → `data:[]` | 不渲染旧系统侧栏 |

### F-SSO-3 · enter / switch

| 操作 | 通过 |
|------|------|
| 选系统 enter **moli-admin** | enter 后**另有** `getRouters`；非仅 enter 内嵌树 |
| 顶栏切换系统 | switch 后 `getRouters` + 侧栏变化 |

### F-SSO-4 · 清理态

| 操作 | 通过 |
|------|------|
| admin → 另一 INTERNAL（若有） | 旧业务 tab 不可访问或已清 |
| Network | 无旧系统路由组件请求 |

### F-SSO-5 · login

| 操作 | 通过 |
|------|------|
| 多系统 login | `menuVoList=[]`，进选系统 |
| 单 INTERNAL login | 自动 enter + 侧栏正常 |

### F-SSO-6 · 知识库 Q5-A

| 操作 | 通过 |
|------|------|
| enter **moli-admin**，角色有 KB | 侧栏 **900 企业知识库**，`/knowledge/browse` 可开 |
| enter **moli-knowledge (39)** | 浏览器跳转 `redirectUrl`；无 meiling-ui 侧栏 KB |
| admin 侧栏 | **无** ChatGPT(500)/BI(600) 段（若后端 backfill 正确） |

---

## 3. 后端联动场景（S1–S10 子集）

| ID | 步骤 | 期望 |
|----|------|------|
| S1 | `sso.enabled=false` 登录 | 与现网全量菜单一致 |
| S3 | 多系统用户 enter admin | 无 500/600；**有 900**（有权限） |
| S4 | switch → `getRouters` | 菜单随 `currentSystemId` 变 |
| S5 | enter 39 | 空菜单 + redirect |
| S10 | 清 Session 系统后直接 `getRouters` | `[]` + 前端跳选系统 |

---

## 4. 相关

- [sso-menu-frontend-handoff.md](../api/sso-menu-frontend-handoff.md)
- [frontend-backend-dependencies.md](../api/frontend-backend-dependencies.md) §5
