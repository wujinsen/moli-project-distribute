# SQL 增量脚本 · 执行顺序

> 基线：[`scripts/moli.sql`](../../scripts/moli.sql)（全库结构+种子数据）  
> 一键初始化：[`init-db.ps1`](../../scripts/init-db.ps1)  
> 字符集约束：[sql/README.md](../sql/README.md)

---

## 1. 新环境（推荐）

```powershell
.\scripts\init-db.ps1
```

**实际执行顺序**（脚本内固定）：

| 顺序 | 文件 | 说明 |
|------|------|------|
| 1 | `scripts/moli.sql` | 用户中心 + sys_* 全量 |
| 2 | `docs/sql/02_seckill_schema.sql` | 秒杀表 + 活动种子 |
| 3 | `docs/sql/03_knowledge_schema.sql` | 知识库 14 表 |
| 4 | `docs/sql/04_knowledge_menu.sql` | 知识库菜单 |
| 5 | `docs/sql/07_kb_space_ops_manual.sql` | ops 空间 |

跳过选项：`-SkipSeckill`、`-SkipKnowledge`。

**init-db 未包含、需手动追加的 v1 脚本**（已有库或完整 v1 功能）：

| 顺序 | 文件 | 说明 |
|------|------|------|
| 6 | `05_knowledge_action_patch.sql` | sys_action 分组修正 |
| 7 | `06_remove_kb_admin.sql` | 移除废弃 kb:admin |
| 8 | `04_kb_space_jp_exam.sql` | 考试空间（可选） |
| 9 | `08_kb_ingest_workbench.sql` | Ingest 批次表 |
| 10 | `09_kb_ingest_t15e.sql` | Ingest enrich 列 |
| 11 | `10_kb_category_dir_slug.sql` | 分类 dir_slug |
| 12 | `11_kb_wiki_govern_menu.sql` | Wiki 治理菜单 |
| 13 | `11_kb_platform_llm_config.sql` | LLM 配置表 |
| 14 | `12_kb_platform_llm_menu.sql` | LLM 设置菜单 |
| 15 | `17_operation_secret_view.sql`～`21_operation_ssh_deploy.sql` | 运维 SSH/远程部署 |
| 16 | `22_operation_command_flex.sql` | 灵活路径、远程命令权限、`upload_allowed_roots` |
| 17 | `23_operation_schema_hardening.sql` | 组件 `server_id`、N:N 唯一、查询索引（Phase R1） |
| 18 | `17_kb_lint_ops_enhance.sql` | KBOPS-8 体检工单 `assignee_id`/`priority` + `kb:ops:dashboard` 动作 |
| 18 | `24_operation_port_matrix.sql` | 端口矩阵表 + 种子 + 菜单 406 + `operation:port-matrix:*`（SVR-21） |
| 19 | `25_operation_task_project_id.sql` | `operation_task.project_id` + 索引（Phase R3 P1）；**RelationDrawer / 拓扑关联任务查询依赖此列** |
| 20 | `26_operation_server_role.sql` | `operation_server_info.server_role` + 索引 + 按名称回填（SVR-23） |
| 22 | `27_operation_server_tags.sql` | `operation_server_info.tags` JSON 数组 + 环境种子回填（SVR-24）；未执行 26 时可单独跑（`tags` 列接 `environment` 后） |
| 23 | `29_operation_project_component.sql` | 项目→组件依赖 N:N 表（SVR-26a）；拓扑 `depends_on` 边与 `component-links` API 前置（**已合并进 `moli.sql` 基线**） |
| 24 | `28_operation_topology_menu.sql` | 拓扑图菜单 407（SVR-25c）；perms 复用 `operation:server:list`（**已合并进 `moli.sql` 基线**） |
| 25 | `29_kb_document_chunk.sql` | 知识库切段表 `kb_document_chunk` + ngram 全文（`/kb/ask` 按段召回） |

---

## 2. 已有库升级（从旧快照迁移）

### 2.1 判断是否已执行

```sql
-- 知识库核心表
SHOW TABLES LIKE 'kb_document';
-- Ingest
SHOW TABLES LIKE 'kb_ingest_job';
-- LLM 平台
SHOW TABLES LIKE 'kb_platform_llm_config';
-- 分类 dir_slug
SHOW COLUMNS FROM kb_category LIKE 'dir_slug';
```

### 2.2 推荐顺序（按依赖）

```
03_knowledge_schema.sql      # 若无 kb_* 表
04_knowledge_menu.sql
05_knowledge_action_patch.sql
06_remove_kb_admin.sql
07_kb_space_ops_manual.sql
07_kb_space_ops_manual_fix_charset.sql   # 仅乱码修复
04_kb_space_jp_exam.sql                  # 需要考试空间时
08_kb_ingest_workbench.sql
09_kb_ingest_t15e.sql
10_kb_category_dir_slug.sql
11_kb_wiki_govern_menu.sql
11_kb_platform_llm_config.sql
12_kb_platform_llm_menu.sql
02_seckill_schema.sql                    # 若无秒杀表
```

**规则**

- 每个文件执行前看文件头注释（多数可重复执行或含 `IF NOT EXISTS`）
- `09`、`10` 等 ALTER 脚本：**重复执行前确认列/索引未存在**
- 含中文种子必须用 `utf8mb4` + `source`

### 2.3 单文件示例

```powershell
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
& $mysql -u root -p12345678 --default-character-set=utf8mb4 moli -e "source D:/work/moli_project/moli-project-distribute/docs/sql/08_kb_ingest_workbench.sql"
```

---

## 3. 与发布 Runbook 的关系

发布 checklist：[v1-release-runbook.md](v1-release-runbook.md) §2  
生产检查：[production-checklist.md](production-checklist.md) §5

**发版前**：在 staging 跑完全部未执行脚本 → 冒烟 → 再 prod。

---

## 4. 不要

- PowerShell `Get-Content | mysql` 导入含中文 SQL
- 跳过 `03` 直接跑 `04`（菜单依赖表结构）
- 在生产重复跑 destructive 脚本（执行前读文件头）

---

## 5. 相关

- 表设计：[KNOWLEDGE_SCHEMA.md](../sql/KNOWLEDGE_SCHEMA.md) · [USER_CENTER_SCHEMA.md](../sql/USER_CENTER_SCHEMA.md)
- 文件清单：[sql/README.md](../sql/README.md)
