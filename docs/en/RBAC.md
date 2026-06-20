# RBAC Permission Design

**Languages / 语言 / 言語**: [中文](../zh-CN/RBAC.md) | [English](RBAC.md) | [日本語](../ja/RBAC.md)

> RBAC model, authentication/authorization flow, and API design for `moli-user-center`.

---

## 1. Overview

Classic **User — Role — Permission (Menu)** model:

- **User** ↔ **Role** via `sys_user_role` (N:N)
- **Role** ↔ **Menu** via `sys_role_menu` (N:N)
- **Menu** carries frontend routes and **permission codes (`perms`)**; button menus define API-level permissions

**Apache Shiro** handles auth; Session and permission cache stored in **Redis** for distributed deployment.

```
User (SysUser) ──N:N──▶ Role (SysRole) ──N:N──▶ Menu (SysMenu)
                                                      │
                                              perms (button permission)
     │
     ▼ deptId
Department (SysDept)
```

---

## 2. Core Entities

### SysUser

| Field | Description |
|-------|-------------|
| `userName` | Login username |
| `password` | SHA-256 + salt, 15 iterations |
| `salt` | Password salt |
| `deptId` | Department ID |
| `status` | Locked (0=no, 1=yes) |
| `isDelete` | Soft delete (0=no, 1=yes) |

### SysRole

| Field | Description |
|-------|-------------|
| `roleName` | Role name |
| `status` | 1=active, 0=disabled |
| `orderNum` | Sort order |
| `remark` | Remark |

### SysMenu

| Field | Description |
|-------|-------------|
| `menuName` | Menu name |
| `parentId` | Parent menu ID (0 = root) |
| `path` | Frontend route path |
| `component` | Frontend component |
| `menuType` | M=Directory, C=Menu, F=Button |
| `perms` | Permission code (for buttons) |
| `status` | 1=enabled, 0=disabled |

**Menu Types**

| Code | Type | Purpose |
|------|------|---------|
| `M` | Directory | Menu tree organization |
| `C` | Menu | Page route |
| `F` | Button | Button/API permission via `perms` |

### Association Tables

| Table | Fields | Description |
|-------|--------|-------------|
| `sys_user_role` | `userId`, `roleId` | User-Role N:N |
| `sys_role_menu` | `roleId`, `menuId` | Role-Menu N:N |

---

## 3. Permission Codes

Format: **`module:resource:action`** (see `CommonPermissionConstant`)

| Code | Description |
|------|-------------|
| `sys:user:create` | Create user |
| `sys:user:update` | Update user |
| `sys:user:delete` | Delete user |
| `sys:role:create` | Create role |
| `sys:role:update` | Update role |
| `sys:role:delete` | Delete role |
| `sys:dept:create` | Create department |
| `sys:dept:update` | Update department |
| `sys:dept:delete` | Delete department |

Use `@RequiresPermissions("sys:user:create")` when Shiro authorization is enabled.

---

## 4. Authentication Flow

### Login — `POST /login`

1. Client sends `{userName, password}`
2. Shiro `subject.login()` validates credentials
3. Session stored in Redis (`shiro:session:*`)
4. Menu tree loaded via `selectMenuTreeByUserId`
5. Returns `LoginVo`: `token` (Session ID) + `user` + `menuVoList`

### Password Hashing

- Algorithm: **SHA-256**, **15 iterations**, per-user `salt`
- Utility: `SHA256Util.sha256(password, salt)`

### Session & Token

- Redis key prefix: `shiro:session:` / `shiro:cache:`
- Principal field: `userName`
- Logout: `POST /logout` → `ShiroUtils.logout()`

### Whitelist (no auth required)

| Path | Purpose |
|------|---------|
| `/login` | Login |
| `/swagger-ui.html`, `/v2/**` | Swagger |
| `/static/**` | Static assets |

---

## 5. Authorization Flow

### Menu Authorization (Implemented)

```
User ID → sys_user_role → sys_role_menu → sys_menu → MenuVo tree
```

- **Super admin**: username `admin` bypasses role filter, gets all menus
- Implementation: `MenuServiceImpl.selectMenuListByUserId()`

### API Permissions (Reserved)

`ShiroRealm.doGetAuthorizationInfo()` logic is commented out. When enabled:

1. Load roles → `rolesSet`
2. Load button menus (`menuType = F`) → extract `perms` → `permsSet`
3. Cache in Redis for `@RequiresPermissions` / `@RequiresRoles`

### Frontend Routes

`GET /menu/getRouters` returns `MenuVo` tree with `path`, `component`, `meta`, `children`.

---

## 6. Management APIs

### `/user`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/user/list` | Paginated user list |
| POST | `/user` | Create user |
| PUT | `/user` | Update user |
| DELETE | `/user/{userIds}` | Soft delete |
| PUT | `/user/inserUserRole` | Assign roles |

### `/role`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/role/list` | Role list |
| POST | `/role` | Create role (+ `menuIds`) |
| DELETE | `/role/{ids}` | Delete role and role-menu links |

### `/menu`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/menu/getRouters` | Current user's route tree |
| GET | `/menu/getMenuTreeAll` | Full menu tree (admin) |
| GET | `/menu/selectMenuTreeByRoleId/{roleId}` | Role menu assignment preview |

### `/dept`

Department CRUD; linked via `deptId`, independent from RBAC roles.

---

## 7. Cross-Service Auth

Other services use **`moli-user-center-client`**:

```
Business service                  User center
    │── Dubbo: getInfoByUserName ──▶│  UserServerProvider (RPC, no HTTP)
    │── Shared Redis Session ────────│  shiro:session:* / shiro:cache:*
    │── Authorization header ────────│  External traffic via gateway
```

- **`ShiroConfig`**: `@DubboReference UserCenterServer`, passed to `ShiroRealm` via setter
- **`ShiroRealm`**: fetches user via Dubbo during login, validates password locally
- See [Architecture / Invocation / Auth](ARCHITECTURE.md)

---

## 8. Implementation Status

| Feature | Status | Notes |
|---------|--------|-------|
| User/Role/Menu CRUD | ✅ Done | Controllers |
| User-Role assignment | ✅ Done | `inserUserRole` |
| Role-Menu binding | ✅ Done | On role create |
| Login + Redis Session | ✅ Done | Shiro + shiro-redis |
| Menu tree by role | ✅ Done | Admin bypass included |
| Shiro `@RequiresPermissions` | ⏳ Reserved | Realm auth logic commented |
| Captcha | ⏳ Reserved | Framework exists |

---

## 9. Code Locations

| Module | Path | Responsibility |
|--------|------|----------------|
| Entities | `moli-user-center-common/.../entity/` | SysUser, SysRole, SysMenu |
| Constants | `.../CommonPermissionConstant.java` | Permission codes |
| Shiro Config | `.../config/shiro/ShiroConfig.java` | Filter chain, Redis Session |
| Realm | `.../config/shiro/ShiroRealm.java` | Authentication & authorization |
| Menu Service | `.../MenuServiceImpl.java` | Menu tree & RBAC queries |
| Login | `.../LoginController.java` | Login/logout |
| Client | `moli-user-center-client/` | Dubbo contract + Shiro integration |
