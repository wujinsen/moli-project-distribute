# SSO · 菜单按系统隔离 PRD（SSO-MENU-1）

> **状态**：active · 2026-07-13（前后端 + 走查 ✅）  
> **技术设计**：[`docs/design/sso-menu-system-isolation.md`](../design/sso-menu-system-isolation.md)  
> **前端契约**：[`docs/api/sso-menu-frontend-handoff.md`](../api/sso-menu-frontend-handoff.md)  
> **走查**：[`docs/test/sso-menu-frontend-walkthrough.md`](../test/sso-menu-frontend-walkthrough.md)  
> **SQL**：[`docs/sql/30_sso_menu_system_id.sql`](../sql/30_sso_menu_system_id.sql)

---

## 1. 问题

多系统门户（`sys_system`）已支持 enter/switch，但运行时菜单未按当前系统过滤，导致：

- 进入 `moli-admin` 后侧栏同时出现系统管理、运营管理、知识库等**全部**已授权菜单（串台）  
- 切换系统后 `getRouters` 仍返回全量树

---

## 2. 产品目标

1. **运行时菜单**仅展示当前 Session 系统可见子树  
2. **门户未 enter** 时路由为空，引导选系统（Q3）  
3. **知识库双入口**（Q5）：900 段归属 admin 内嵌 + `moli-knowledge`(39) EXTERNAL 跳转  
4. 超管**菜单管理 UI** 仍展示全树；运行时按系统过滤

---

## 3. 功能需求

| ID | 需求 | 验收 |
|----|------|------|
| **F-SSO-1** | 路由守卫：门户开启且未 enter → 跳选系统 | 走查 S3 |
| **F-SSO-2** | enter/switch 后 `reloadRoutesFromServer` | 走查 S4/S5 |
| **F-SSO-3** | tab 清空 / 缓存隔离 | 走查 S6 |
| **F-SSO-4** | `sys_menu.system_id` 回填 | SQL 30 + Q5 backfill |
| **F-SSO-5** | `resolveRoutersForCurrentSystem` 过滤 | 后端 P0/P1 |
| **F-SSO-6** | 知识库 EXTERNAL 第二入口 | redirectUrl 保留 |

---

## 4. 数据与权限

| 项 | 说明 |
|----|------|
| `sys_menu.system_id` | NULL = 全系统共享；非 NULL = 仅该 `sys_system.id` |
| 角色 | 仍用全局 `sys_role_menu`；运行时 **角色菜单 ∩ 系统过滤** |
| 老库 | 执行 `30_sso_menu_system_id.sql` 或新 `moli.sql` 基线 |

---

## 5. 非目标

- 按系统拆分角色/权限码（P0 不做）  
- EXTERNAL 系统内嵌菜单  
- 前端拆多 SPA

---

## 6. 验收

- [x] API 冒烟 S1–S10（2026-07-13）  
- [x] 边界脚本 `_sso_walkthrough_boundary.ps1`  
- [x] Q3：未 enter → `getRouters` 返回 `[]`  
- [x] Q5：900 段 `system_id=1`；id=39 保留 EXTERNAL

---

## 7. 相关

| 文档 | 用途 |
|------|------|
| [portal-system-group.md](../design/portal-system-group.md) | 门户分组 |
| [frontend-routes-map.md](../api/frontend-routes-map.md) | menu_id 段位 |
| [user-center-requirements.md](user-center-requirements.md) §2.3/§2.7 | 工程索引 |

---

## 8. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-13 | 初稿：SSO-MENU-1 产品索引 + 走查结论 |
