# 知识库 · T20 后端 + 概要设计 Web 浏览 · 上线 Runbook

> **日期**：2026-07-06  
> **分支**：`ci/kb-sync-multi-space-gate`（合并进发布分支后再拉取）  
> **关联**：[`deploy/上线流程.md`](../../deploy/上线流程.md) §4 · [`knowledge-workbench-operations.md`](knowledge-workbench-operations.md)

---

## 1. 本批变更（上线范围）

| 项 | 内容 | 上线后用户能做什么 |
|----|------|-------------------|
| **T20a** | `POST /kb/ingest/raw-upload` | Swagger/curl 浏览器投喂 raw（**前端 Tab1 待 T20f**） |
| **T20b** | `POST /kb/wiki/page/import` | Swagger/curl 成品 md 导入 wiki + Sync |
| **T20e** | 同上，可选 `assetsZip` 插图包 | 导入时自动 `![](…)` → `![](assets/…)` |
| **概要设计浏览** | `docs/design/` → `wiki-moli/develop/` 11 页 | **文档浏览**可搜「用户中心 · 概要设计」等 |
| **SQL（可选）** | `docs/sql/16_kb_import_entry_menu.sql` | 给角色绑定 `kb:ingest:rawUpload`（Tab1 用） |

**本批不包含**

- meiling-ui **T20f** 三 Tab 界面（仍用 Swagger 或后续前端发版）
- T20c 批量 import、T20d Controller Shiro 注解（P1）
- 架构图 PNG 内嵌展示（正文为 `` `docs/diagrams/png/...` `` 路径，Sync 不上传 png）

---

## 2. 上线前检查（T-1）

复制打勾：

- [ ] 目标分支已合并，本地 `mvn -pl moli-knowledge/moli-knowledge-server -am test` 通过
- [ ] MySQL 已备份（含 `moli` 库）
- [ ] 确认 EC2 路径：`/opt/moli-project-distribute`
- [ ] 确认 `moli-knowledge/conf/moli-knowledge.env` 中 DB/Redis 正确
- [ ] 确认 `KB_WIKI_ROOT=/opt/moli-project-distribute/moli-knowledge/kb`（或与 `application-pro.yml` 一致）
- [ ] Python3 + pymysql 已装（见 [`deploy/上线流程.md`](../../deploy/上线流程.md) §4.1）

---

## 3. 本地：提交并构建（Windows）

### 3.1 确认 Git 含 wiki 与脚本

本批除已 push 的 T20 后端外，还需包含：

- `moli-knowledge/kb/wiki-moli/develop/*概要设计*.md` 等 **11 页**
- `moli-knowledge/kb/tools/import_design_to_wiki.py`
- `wiki-moli/develop/技术方案与架构索引.md`、`index.md`、`log.md` 等更新

```powershell
cd D:\work\moli_project\moli-project-distribute
git status
git pull
```

合并/推送至线上要拉的 branch 后，再在 EC2 `git pull`。

### 3.2 打 knowledge-server JAR

```powershell
mvn -pl moli-knowledge/moli-knowledge-server -am package -DskipTests
```

产物：

`moli-knowledge\moli-knowledge-server\target\moli-knowledge-server-*.jar`

上传到 EC2：`/opt/moli-project-distribute/moli-knowledge/moli-knowledge-server-*.jar`（与各服务现有 JAR 命名一致即可）。

> gateway / user-center **本批可不换**，除非同批还有其他改动。

---

## 4. 线上：拉代码

SSH 登录 EC2：

```bash
cd /opt/moli-project-distribute
git pull
```

**验收磁盘**（必须存在）：

```bash
ls moli-knowledge/kb/wiki-moli/develop/用户中心-概要设计.md
ls moli-knowledge/kb/tools/import_design_to_wiki.py
ls moli-knowledge/moli-knowledge-server/target/moli-knowledge-server-*.jar 2>/dev/null || ls moli-knowledge/*.jar
```

---

## 5. 线上：SQL（可选 · 仅当要用 Raw 投喂 Tab1 权限）

若生产库**尚未**执行过 `16_kb_import_entry_menu.sql`：

```bash
mysql --default-character-set=utf8mb4 -h"$DB_HOST" -u"$SPRING_DATASOURCE_USERNAME" -p"$SPRING_DATASOURCE_PASSWORD" "$DB_NAME" \
  < /opt/moli-project-distribute/docs/sql/16_kb_import_entry_menu.sql
```

- 写入动作权限 `kb:ingest:rawUpload`，绑定角色 2/3（管理员/研发）
- **Tab3 成品导入**仍用空间 **editor** + `kb:wiki:edit`，不依赖本 SQL
- 执行后相关用户**重新登录**一次

跳过本步：仅上线「概要设计浏览 + page/import API」，不影响文档浏览 Sync。

---

## 6. 线上：换 JAR 并重启 knowledge

```bash
cd /opt/moli-project-distribute
chmod +x deploy/linux/moli-service.sh

# 若 JAR 已上传到 moli-knowledge/ 根目录
./deploy/linux/moli-service.sh knowledge stop
# 替换 JAR 文件（按你们现有命名习惯覆盖旧包）
./deploy/linux/moli-service.sh knowledge start
./deploy/linux/moli-service.sh knowledge status
```

**可选配置**（未写则走 Java 默认值）：在 `moli-knowledge/application-pro.yml` 的 `kb.wiki` / `kb.ingest` 下可追加：

```yaml
kb:
  wiki:
    import-assets-zip-max-bytes: 52428800   # T20e zip 50MB
    import-assets-max-entries: 200
  ingest:
    raw-upload-max-bytes: 5242880           # T20a 单文件 5MB
    raw-upload-max-files: 20
```

`spring.servlet.multipart` 生产模板已为 **50MB**，与 T20e 兼容。

---

## 7. 线上：Wiki → MySQL Sync（概要设计浏览必做）

Sync **只写 markdown 进库**，不上传 png；**必须执行**后 Web 文档浏览才看得到新页。

```bash
cd /opt/moli-project-distribute/moli-knowledge/kb

set -a
source <(sed 's/\r$//' /opt/moli-project-distribute/moli-knowledge/conf/moli-knowledge.env)
set +a
export KB_SYNC_HOST="${DB_HOST:-127.0.0.1}"
export KB_SYNC_USER="${SPRING_DATASOURCE_USERNAME}"
export KB_SYNC_PASSWORD="${SPRING_DATASOURCE_PASSWORD}"
export KB_SYNC_DB="${DB_NAME:-moli}"
export KB_SYNC_PYTHON=python3
```

### 7.1 预览

```bash
bash tools/ci/run_sync.sh dry-run-all
```

在输出中确认出现 **`develop/用户中心-概要设计`** 等待 insert/update。

### 7.2 写库（二选一）

**必须先加载 DB 账号**（与 [`deploy/上线流程.md`](../../deploy/上线流程.md) §4.2.1 相同），否则会 `1045 Access denied`：

```bash
cd /opt/moli-project-distribute/moli-knowledge/kb
set -a
source <(sed 's/\r$//' /opt/moli-project-distribute/moli-knowledge/conf/moli-knowledge.env)
set +a
export KB_SYNC_HOST="${DB_HOST:-127.0.0.1}"
export KB_SYNC_PORT="${DB_PORT:-3306}"
export KB_SYNC_USER="${SPRING_DATASOURCE_USERNAME}"
export KB_SYNC_PASSWORD="${SPRING_DATASOURCE_PASSWORD}"
export KB_SYNC_DB="${DB_NAME:-moli}"

# 建议先验证
mysql -h"${KB_SYNC_HOST}" -P"${KB_SYNC_PORT}" -u"${KB_SYNC_USER}" -p"${KB_SYNC_PASSWORD}" "${KB_SYNC_DB}" -e "SELECT 1"
```

**仅茉莉系统手册**（推荐，本批只改了 `wiki-moli`）：

```bash
python3 tools/sync_to_db.py \
  --wiki-dir wiki-moli \
  --space moli-ops-manual \
  --host "${KB_SYNC_HOST}" \
  --port "${KB_SYNC_PORT}" \
  --user "${KB_SYNC_USER}" \
  --password "${KB_SYNC_PASSWORD}" \
  --db "${KB_SYNC_DB}"
```

**三空间一起**：

```bash
export KB_SYNC_PYTHON=python3
bash tools/ci/run_sync.sh sync-all
```

### 7.3 Web 触发 Sync（SSH 不便时）

前提：磁盘上已有新 md（§4 `git pull` 完成）。

1. 登录 meiling-ui（超管或空间 admin）
2. **健康体检** → **Wiki 同步**
3. 选空间 **茉莉系统手册**（`spaceId=900000000000000003`）
4. 触发同步

等价 API：

```bash
curl -X POST "http://127.0.0.1:21000/KnowledgeServer/kb/sync/trigger?spaceId=900000000000000003" \
  -H "Authorization: <登录token>"
```

---

## 8. 验收

### 8.1 文档浏览（概要设计）

| # | 操作 | 期望 |
|---|------|------|
| 1 | 空间选 **茉莉系统手册** | 有数据 |
| 2 | 文档浏览搜 **`概要设计`** 或 **`用户中心 · 概要设计`** | 命中 `develop/用户中心-概要设计` |
| 3 | 打开 **技术 → 技术方案与架构索引** | 表格链到各 [[概要设计]] wiki 页 |
| 4 | 打开 **用户中心 · 概要设计** 正文 | 可读；架构图为 `` `docs/diagrams/png/...` `` 提示（非内嵌图） |

### 8.2 API 冒烟（T20 · 需 editor 权限 + token）

先登录拿 token，替换下面 `$TOKEN`、`$SPACE_ID`（茉莉手册分类 id 从 Web 或 `/kb/index` 取）。

**Tab3 成品导入（无 zip）**

```bash
curl -s -X POST "http://127.0.0.1:21000/KnowledgeServer/kb/wiki/page/import" \
  -H "Authorization: $TOKEN" \
  -F "spaceId=900000000000000003" \
  -F "categoryId=<develop分类ID>" \
  -F "file=@/path/to/test.md" \
  -F "sync=true"
```

**T20e 带 assetsZip**（md 内已有 `![](imageFile1.png)`，zip 含同名文件）

```bash
curl -s -X POST "http://127.0.0.1:21000/KnowledgeServer/kb/wiki/page/import" \
  -H "Authorization: $TOKEN" \
  -F "spaceId=900000000000000003" \
  -F "categoryId=<develop分类ID>" \
  -F "file=@/path/to/with-image.md" \
  -F "assetsZip=@/path/to/assets.zip" \
  -F "sync=true"
```

响应应含 `assetsImported[]`（有 zip 时）。

**Tab1 raw-upload**（需 `kb:ingest:rawUpload` + §5 SQL）

```bash
curl -s -X POST "http://127.0.0.1:21000/KnowledgeServer/kb/ingest/raw-upload" \
  -H "Authorization: $TOKEN" \
  -F "spaceId=900000000000000001" \
  -F "prefix=test-walkthrough" \
  -F "files=@/path/to/demo.md"
```

契约详情：[`docs/api/kb-import-entry-frontend.md`](../api/kb-import-entry-frontend.md)

### 8.3 Sync 日志

```bash
curl -s "http://127.0.0.1:21000/KnowledgeServer/kb/sync/status?spaceId=900000000000000003" \
  -H "Authorization: $TOKEN"
```

或查表 `kb_sync_log` 最近一批 success。

---

## 9. 回滚

| 层级 | 做法 |
|------|------|
| **JAR** | 换回上一版 `moli-knowledge-server` JAR，`knowledge stop/start` |
| **DB 文档** | wiki 页可保留；若需撤销浏览，git revert wiki 提交后再 Sync 同一空间 |
| **SQL 16** | 一般不回滚；最多 `DELETE FROM sys_role_action WHERE perm_code='kb:ingest:rawUpload'` |

---

## 10. 发布后维护

### 更新 `docs/design/` 后再上浏览

开发机：

```bash
cd moli-knowledge/kb
python tools/import_design_to_wiki.py
git add wiki-moli/develop/ && git commit && git push
```

线上：§4 `git pull` → §7 Sync（`wiki-moli` / `moli-ops-manual`）。

### 相关文档

| 文档 | 用途 |
|------|------|
| [`docs/api/kb-import-entry-frontend.md`](../api/kb-import-entry-frontend.md) | T20 HTTP 契约 |
| [`docs/design/kb-import-entry-design.md`](../design/kb-import-entry-design.md) | T20 技术设计 |
| [`moli-knowledge/kb/wiki-moli/ops/wiki同步指南.md`](../../moli-knowledge/kb/wiki-moli/ops/wiki同步指南.md) | Sync 日常 SOP |
| [`deploy/上线流程.md`](../../deploy/上线流程.md) | 全站发布主流程 |

---

## 11. 最短路径（老手）

```bash
# EC2
cd /opt/moli-project-distribute && git pull
# 换 knowledge JAR → knowledge restart
cd moli-knowledge/kb
set -a && source <(sed 's/\r$//' ../conf/moli-knowledge.env) && set +a
python3 tools/sync_to_db.py --wiki-dir wiki-moli --space moli-ops-manual \
  --host "${DB_HOST:-127.0.0.1}" --user "${SPRING_DATASOURCE_USERNAME}" \
  --password "${SPRING_DATASOURCE_PASSWORD}" --db "${DB_NAME:-moli}"
# Web：茉莉系统手册 → 搜「概要设计」
```
