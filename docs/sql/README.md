# Moli 数据库基线 SQL

> **新环境请用 [`scripts/moli.sql`](../scripts/moli.sql)**（Navicat 全库导出，2026-06-21）。  
> 早期拆分脚本 `00_schema.sql` / `01_baseline_data.sql` 已删除（被 `scripts/moli.sql` 取代）。

## 文件说明

| 文件 | 说明 |
|------|------|
| **`../scripts/moli.sql`** | **推荐** 最新全库（结构+数据） |
| `02_seckill_schema.sql` | 秒杀压测表（在 moli.sql 之后追加） |
| `03_knowledge_schema.sql` | 企业知识库表（在 moli.sql 之后追加） |
| [`KNOWLEDGE_SCHEMA.md`](KNOWLEDGE_SCHEMA.md) | 知识库表结构设计说明 + ER 关系图 |
| [`KNOWLEDGE_SCHEMA_ER.png`](KNOWLEDGE_SCHEMA_ER.png) | ER 关系图 PNG（任意 MD 阅读器可看） |
| `04_knowledge_menu.sql` | 知识库菜单 + sys_action 动作（在 03 之后追加） |
| `05_knowledge_action_patch.sql` | 已有环境修正 sys_action 分组（空间 CRUD / 体检+同步） |
| `04_kb_space_jp_exam.sql` | 日本語試験私有空间 + 成员示例（在 04_knowledge_menu 之后追加） |

## 新环境初始化

```powershell
.\scripts\init-db.ps1
```

详见 [`scripts/README.md`](../scripts/README.md)。

## 表行数（导出时快照）

| 表 | 行数 | 纳入数据 |
|----|------|----------|
| `operation_component_deploy_info` | 8 | 是 |
| `operation_platform_info` | 6 | 是 |
| `operation_project_deploy_info` | 6 | 是 |
| `operation_server_component` | 10 | 是 |
| `operation_server_info` | 6 | 是 |
| `operation_server_project` | 6 | 是 |
| `sys_action` | 38 | 是 |
| `sys_dept` | 34 | 是 |
| `sys_dict_data` | 35 | 是 |
| `sys_dict_type` | 12 | 是 |
| `sys_login_log` | 39 | 否（审计表） |
| `sys_menu` | 31 | 是 |
| `sys_operation_log` | 166 | 否（审计表） |
| `sys_post` | 39 | 是 |
| `sys_role` | 10 | 是 |
| `sys_role_action` | 56 | 是 |
| `sys_role_menu` | 45 | 是 |
| `sys_system` | 35 | 是 |
| `sys_user` | 33 | 是 |
| `sys_user_post` | 1 | 是 |
| `sys_user_role` | 31 | 是 |
| `sys_user_system` | 70 | 是 |

历史增量脚本（`patch_*.sql`、`migrate_sys_action.sql`）已合并进本基线，新环境无需再执行旧 patch。

## ER 图导出（维护者）

对外文档使用 **PNG 静态图**（`KNOWLEDGE_SCHEMA_ER.png`），读者无需安装 Mermaid。改表后在本目录执行：

```powershell
npx @mermaid-js/mermaid-cli -i KNOWLEDGE_SCHEMA_ER.mmd -o KNOWLEDGE_SCHEMA_ER.png -b white -w 2400
```

将更新后的 `.mmd` 与 `.png` 一并提交。
