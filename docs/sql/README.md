# Moli 数据库基线 SQL

> **新环境请用 [`scripts/moli.sql`](../scripts/moli.sql)**（Navicat 全库导出，2026-06-21）。  
> 早期拆分脚本 `00_schema.sql` / `01_baseline_data.sql` 已删除（被 `scripts/moli.sql` 取代）。

## 字符集与导入约束（utf8mb4 · 必读）

全库与知识库表均为 **`utf8mb4`**（见 [`KNOWLEDGE_SCHEMA.md`](KNOWLEDGE_SCHEMA.md) 通用约定、`03_knowledge_schema.sql` 表定义）。**含中文的 SQL 种子**（如 `kb_space.space_name`、菜单名、`sys_action.name`）导入时必须保证 **客户端连接字符集也是 utf8mb4**，否则中文会落成 `?`。

| 方式 | 是否推荐 | 说明 |
|------|----------|------|
| **`.\scripts\init-db.ps1`** | ✅ 推荐 | 内部用 `mysql --default-character-set=utf8mb4` + `source 文件` |
| **CMD 重定向** `mysql ... moli < file.sql` | ✅ | 文件本身 UTF-8 即可 |
| **Navicat 运行 SQL 文件** | ✅ | 连接/库字符集选 **utf8mb4** |
| **PowerShell 管道** `Get-Content \| mysql` | ❌ **禁止**（含中文时） | 即使 `-Encoding UTF8`，mysql 客户端默认连接仍可能 latin1 |
| **PowerShell + `source`** | ✅ | 见 [`scripts/README.md`](../scripts/README.md) 示例 |

含中文的增量脚本请在文件头保留 `SET NAMES utf8mb4;`（范例：`07_kb_space_ops_manual.sql`、`patch_sys_user_fill_nulls.sql`）。

**已乱码修复**（仅 `moli-ops-manual` 空间名/描述）：

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 --default-character-set=utf8mb4 moli -e "source D:/work/moli_project/moli-project-distribute/docs/sql/07_kb_space_ops_manual_fix_charset.sql"
```

## 文件说明

| 文件 | 说明 |
|------|------|
| **`../scripts/moli.sql`** | **推荐** 最新全库（结构+数据） |
| `02_seckill_schema.sql` | 秒杀压测表（在 moli.sql 之后追加） |
| `03_knowledge_schema.sql` | 企业知识库表（在 moli.sql 之后追加） |
| [`KNOWLEDGE_SCHEMA.md`](KNOWLEDGE_SCHEMA.md) | 知识库表结构设计说明 + ER 关系图 |
| [`USER_CENTER_SCHEMA.md`](USER_CENTER_SCHEMA.md) | **用户中心**表结构（sys_* + operation_*） |
| [`KNOWLEDGE_SCHEMA_ER.png`](KNOWLEDGE_SCHEMA_ER.png) | ER 关系图 PNG（任意 MD 阅读器可看） |
| `04_knowledge_menu.sql` | 知识库菜单 + sys_action 动作（在 03 之后追加） |
| `05_knowledge_action_patch.sql` | 已有环境修正 sys_action 分组（空间 CRUD / 体检+同步） |
| `06_remove_kb_admin.sql` | 移除废弃的 kb:admin 动作与菜单 906 |
| `07_kb_space_ops_manual.sql` | **茉莉系统手册**独立空间 `moli-ops-manual` + 成员示例 |
| `40_purge_jp_exam.sql` | **一次性**：删除已下线的 jp-fe-ap-exam 空间数据 |
| `07_kb_space_ops_manual_fix_charset.sql` | 修复乱码或刷新 `moli-ops-manual` 空间名为「茉莉系统手册」 |
| `08_kb_ingest_workbench.sql` | Ingest 工作台表（job/plan/draft 等） |
| `09_kb_ingest_t15e.sql` | Ingest T15e 增量字段 |
| `10_kb_category_dir_slug.sql` | **分类=目录**：`kb_category` 加 `dir_slug` + 三空间目录种子（已有库必跑） |
| `15_kb_category_drop_default_type.sql` | 删除已废弃列 `kb_category.default_type`（体裁仅 frontmatter `type:`） |
| `11_kb_category_enterprise_trim.sql` | **enterprise-kb 精简为 3 类**（软删 guides/services/outputs） |
| `13_kb_category_enterprise_topic.sql` | **enterprise-kb 方案 B**：新建 **11** 个主题分类（id 141–**151**，含 `bigdata`）；§2 软删旧类见下 |
| `14_kb_category_bigdata.sql` | **增量**：仅补 `bigdata` 分类（已跑旧 §1 且无 151 时用） |
| `13_kb_category_enterprise_topic_retire_old.sql` | Sync 后**物理删除** enterprise-kb 旧三类 concepts/articles/interview |
| （脚本）`moli-knowledge/kb/tools/seed_enterprise_kb_topic_categories.py` | 与 `13_…sql` §1 等价；`--execute` 写库，`--retire-old` 软删旧类 |
| `14_kb_space_member_enterprise.sql` | enterprise-kb 成员：superadmin/admin/editor 演示账号 |
| `11_kb_wiki_govern_menu.sql` | Wiki 治理菜单 + `kb:wiki:govern` 动作 |
| `11_kb_platform_llm_config.sql` | **T19** 平台 LLM 配置表 `kb_platform_llm_config` |
| `12_kb_platform_llm_menu.sql` | **T19** 系统管理菜单「知识库 LLM」+ `kb:platform:llm` |
| `16_kb_import_entry_menu.sql` | **T20** Ingest Tab1 · `kb:ingest:rawUpload` 动作 + 角色 2/3 授权 |

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
| `operation_project_component` | 6 | 是 |
| `operation_server_component` | 10 | 是 |
| `operation_server_info` | 6 | 是 |
| `operation_server_project` | 6 | 是 |
| `sys_action` | 38 | 是 |
| `sys_dept` | 34 | 是 |
| `sys_dict_data` | 35 | 是 |
| `sys_dict_type` | 12 | 是 |
| `sys_login_log` | 39 | 否（审计表） |
| `sys_menu` | 32 | 是 |
| `sys_operation_log` | 166 | 否（审计表） |
| `sys_post` | 39 | 是 |
| `sys_role` | 10 | 是 |
| `sys_role_action` | 56 | 是 |
| `sys_role_menu` | 47 | 是 |
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
