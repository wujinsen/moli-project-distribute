---
name: sql-migration-baseline
description: >-
  Authors and merges Moli SQL incremental migrations (docs/sql/NN_*.sql) into
  scripts/moli.sql baseline and updates USER_CENTER_SCHEMA.md. Use when adding
  tables, columns, menus, seed data, merging migration into baseline, or user
  says SQL migration / moli.sql / 合并基线 / 菜单 SQL.
---

# Moli · SQL 迁移与基线合并

> 权威顺序：[`docs/ops/sql-migration-order.md`](../../docs/ops/sql-migration-order.md)  
> 用户中心表说明：[`docs/sql/USER_CENTER_SCHEMA.md`](../../docs/sql/USER_CENTER_SCHEMA.md)  
> 字符集：[`docs/sql/README.md`](../../docs/sql/README.md) §utf8mb4

## 何时使用

- 新增 `docs/sql/NN_*.sql` 增量脚本
- 把增量**合并进** `scripts/moli.sql`（新环境 init-db 一次到位）
- 新增 `sys_menu` / `sys_role_menu` / `operation_*` 表
- 用户说「合并进 moli.sql」「基线合并」「已有库要跑哪个脚本」

## 决策：只写增量 vs 还要合并基线

| 场景 | 动作 |
|------|------|
| **已有生产/测试库升级** | 只交付 `docs/sql/NN_*.sql`，在 `sql-migration-order.md` 登记顺序 |
| **功能已稳定、新环境也要** | 增量脚本 **+** 合并 `scripts/moli.sql` **+** 更新 SCHEMA/README |
| **仅菜单/种子、无新表** | 可只增量；若菜单 id 固定（如 407），建议同步进 `moli.sql` |

**禁止**：只改 `moli.sql` 不写增量（老库无法升级）。

## 工作流（按顺序）

```
- [ ] 1. 定编号 NN（查 docs/sql/ 最大号 + sql-migration-order.md）
- [ ] 2. 写 docs/sql/NN_{主题}.sql（头注释 + SET NAMES utf8mb4）
- [ ] 3. 登记 docs/ops/sql-migration-order.md
- [ ] 4. （若合并基线）改 scripts/moli.sql
- [ ] 5. 更新 docs/sql/USER_CENTER_SCHEMA.md（表清单/ER/种子行数）
- [ ] 6. 更新 docs/sql/README.md 行数快照（如有种子）
- [ ] 7. 相关设计/API 文档提及迁移路径
```

## 增量脚本规范

**文件头**（必填）：

```sql
-- =============================================================
-- {模块} · {简述}（{任务编号}）
-- 运行顺序：在 {上一脚本} 之后执行
-- 设计：docs/design/xxx.md
-- =============================================================
SET NAMES utf8mb4;
```

**表**：`CREATE TABLE IF NOT EXISTS`；逻辑外键、**无物理 FK**（与现有 `operation_*` N:N 一致）。

**菜单**：`INSERT ... ON DUPLICATE KEY UPDATE`；`sys_role_menu` id 建议 `910400{menuId}`（role 1）、`910720{menuId}`（演示角色）；增量脚本可另给 role 2。

**幂等**：已有库可重复执行或 `IF NOT EXISTS` / `ON DUPLICATE KEY UPDATE`。

## 合并 scripts/moli.sql

1. **表结构**：插在同类表附近（`operation_*` 聚在一起）；格式与现有块一致（`DROP TABLE IF EXISTS` → `CREATE` → `INSERT` 种子）。
2. **菜单**：插在 `sys_menu` 运营块（parent 400）内，按 `order_num`；`sys_role_menu` 跟 405/406/407 同模式。
3. **种子 id**：与增量脚本一致，避免新环境与升级脚本冲突。
4. **勿改**：无关表的 charset/collation；审计大表数据除非任务要求。

## 文档同步清单

| 文件 | 更新什么 |
|------|----------|
| `USER_CENTER_SCHEMA.md` | §2.3 表行、§3 ER、§5 迁移说明、§6 `sys_menu` 行数 |
| `docs/sql/README.md` | 表行数快照表 |
| `sql-migration-order.md` | 新行 + 标注「已合并进 moli.sql 基线」若已合并 |
| `docs/design/*-roadmap.md` | 任务状态 ✅ |

## 验证

```powershell
# 语法/导入（本地有 MySQL 时）
& mysql -u root -p --default-character-set=utf8mb4 moli -e "source D:/work/moli_project/moli-project-distribute/docs/sql/NN_xxx.sql"
```

新环境：`.\scripts\init-db.ps1` 后抽查 `SHOW TABLES LIKE 'operation_%'`、`SELECT id,menu_name FROM sys_menu WHERE parent_id=400`。

## 常见错误

| 错误 | 后果 |
|------|------|
| PowerShell 管道导入含中文 SQL | 中文变 `?` → 用 `source` 或 `init-db.ps1` |
| 只合并 moli.sql 不写增量 | 老库无法升级 |
| 菜单 id 与 moli.sql 不一致 | 新/旧环境菜单错位 |
| 忘记 `sys_role_menu` | 菜单存在但角色看不到 |

## 模板

菜单与表块模板见 [reference.md](reference.md)。

## 用户怎么说

```
@sql-migration-baseline 把 29_operation_project_component 合并进 moli.sql
```

```
新增运维菜单 SQL，并更新 USER_CENTER_SCHEMA
```
