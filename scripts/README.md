# 数据库脚本

## 权威数据源（请用这个）

| 文件 | 说明 |
|------|------|
| **`scripts/moli.sql`** | Navicat 导出的 **最新全库快照**（结构 + 数据，含真实用户/登录日志等） |

压测、联调、新环境初始化 **优先导入此文件**。

## 企业知识库表

| 文件 | 说明 |
|------|------|
| `docs/sql/03_knowledge_schema.sql` | 知识库业务表 |
| `docs/sql/04_knowledge_menu.sql` | 知识库 `sys_menu` + `sys_role_menu`（`getRouters` 下发给前端） |

`init-db.ps1` 默认会依次导入上述两个文件（可用 `-SkipKnowledge` 跳过）。

---

| 文件 | 说明 |
|------|------|
| `docs/sql/02_seckill_schema.sql` | 秒杀表 `seckill_activity` / `seckill_order`（`moli.sql` 不含时可追加） |

## Windows 一键导入

```powershell
cd D:\work\moli_project\moli-project-distribute
.\scripts\init-db.ps1
```

仅导入全库（不含秒杀表）：

```powershell
.\scripts\init-db.ps1 -SkipSeckill
```

## 手动导入（CMD，路径有空格需引号）

```cmd
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 -e "CREATE DATABASE IF NOT EXISTS moli DEFAULT CHARSET utf8mb4;"
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 moli < scripts\moli.sql
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 moli < docs\sql\02_seckill_schema.sql
```

## 数据库工具（Navicat 等）

1. 选中 `moli` 库  
2. 运行 SQL 文件 → 选 `scripts/moli.sql`  
3. 再执行 `docs/sql/02_seckill_schema.sql`（若需要秒杀压测）

## 压测登录账号

`scripts/moli.sql` 里含 `zhangsan`、`admin`、`superadmin` 等用户，**默认密码均为 `123456`**（SHA-256 + salt `moli`，15 轮）。

**注意：** 数据库 `password` 列存的是 **64 位哈希**，不是明文 `123456`。Navicat 里看起来一样，若差一个字符或仍是旧哈希 `c4bc9714...`，登录就会失败。把 `zhangsan` 的 `password` + `salt` 整行复制到其他用户能成功，说明之前哈希并不真相同。

一键统一所有演示用户为 `123456`：

```powershell
Get-Content scripts\reset-demo-passwords.sql -Raw | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 moli
```

- k6 可通过环境变量传入：

```powershell
k6 run -e LOGIN_PASSWORD=你的密码 -e VIA_GATEWAY=false load-test/k6/user-center-login-smoke.js
```

## 旧文件

早期拆分基线 `docs/sql/00_schema.sql`、`01_baseline_data.sql` 已删除，统一以 `scripts/moli.sql` 为准。
