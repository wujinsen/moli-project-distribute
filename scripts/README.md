# 数据库脚本

## 字符集与导入（utf8mb4 · 必读）

- 库与表：**`utf8mb4`**（`CREATE DATABASE ... CHARSET utf8mb4`）。
- **含中文的 SQL**：禁止 PowerShell **`Get-Content ... | mysql`** 管道导入（会写成 `?`）。
- **正确做法**：
  - 一键：`.\scripts\init-db.ps1`（已用 `--default-character-set=utf8mb4` + `source`）
  - 单文件：

```powershell
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
& $mysql -u root -p12345678 --default-character-set=utf8mb4 moli -e "source D:/work/moli_project/moli-project-distribute/docs/sql/07_kb_space_ops_manual.sql"
```

- CMD 重定向 `mysql ... moli < file.sql` 可用于纯 ASCII 或 UTF-8 文件；中文种子仍建议加 `--default-character-set=utf8mb4`。
- Navicat：连接字符集选 **utf8mb4**。
- 详见 [`docs/sql/README.md`](../docs/sql/README.md)「字符集与导入约束」。

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

`init-db.ps1` 默认会依次导入上述文件，并 **`07_kb_space_ops_manual.sql`**（茉莉系统手册空间）；可用 `-SkipKnowledge` 跳过。

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
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS moli DEFAULT CHARSET utf8mb4;"
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 --default-character-set=utf8mb4 moli < scripts\moli.sql
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 --default-character-set=utf8mb4 moli < docs\sql\02_seckill_schema.sql
```

PowerShell 单文件（**含中文时必须这样，禁止 `Get-Content | mysql` 管道**）：

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 --default-character-set=utf8mb4 moli -e "source D:/work/moli_project/moli-project-distribute/docs/sql/03_knowledge_schema.sql"
```

## 数据库工具（Navicat 等）

1. 选中 `moli` 库  
2. 连接/库字符集选 **utf8mb4**  
3. 运行 SQL 文件 → 选 `scripts/moli.sql`  
4. 再执行 `docs/sql/02_seckill_schema.sql`（若需要秒杀压测）

## 压测登录账号

`scripts/moli.sql` 里含 `zhangsan`、`admin`、`superadmin` 等用户，**默认密码均为 `123456`**（SHA-256 + salt `moli`，15 轮）。

**注意：** 数据库 `password` 列存的是 **64 位哈希**，不是明文 `123456`。Navicat 里看起来一样，若差一个字符或仍是旧哈希 `c4bc9714...`，登录就会失败。把 `zhangsan` 的 `password` + `salt` 整行复制到其他用户能成功，说明之前哈希并不真相同。

一键统一所有演示用户为 `123456`：

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 --default-character-set=utf8mb4 moli -e "source D:/work/moli_project/moli-project-distribute/scripts/reset-demo-passwords.sql"
```

- k6 可通过环境变量传入：

```powershell
k6 run -e LOGIN_PASSWORD=你的密码 -e VIA_GATEWAY=false load-test/k6/user-center-login-smoke.js
```

## 旧文件

早期拆分基线 `docs/sql/00_schema.sql`、`01_baseline_data.sql` 已删除，统一以 `scripts/moli.sql` 为准。
