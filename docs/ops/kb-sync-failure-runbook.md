# 知识库 · Sync 失败 Runbook（KBOPS-A2）

> **适用**：wiki → MySQL 同步失败、定时 Sync 告警、CI `KB Wiki Sync` 红灯、Web 与磁盘不一致。  
> **相关**：[wiki 同步指南](../../moli-knowledge/kb/wiki-moli/ops/wiki同步指南.md) · [knowledge-ops-prd.md](../product/knowledge-ops-prd.md) · [KNOWLEDGE_API.md](../api/KNOWLEDGE_API.md) §4

---

## 1. 先判断「真失败」还是「未 Sync」

| 现象 | 可能原因 | 第一步 |
|------|----------|--------|
| Web 正文旧、磁盘已改 | **未 Sync** | 执行 Sync（§5） |
| `POST /kb/sync/trigger` 返回 `success=false` 或 `exitCode≠0` | **Sync 失败** | §3–§6 |
| 定时任务日志 `定时同步结束 success=false` | 脚本/分类/部分页失败 | §3 + §5 |
| 飞书/企微收到 `[知识库 Sync 失败]` | KBOPS-5 webhook | §3 查批次 + §5 重跑 |
| CI `KB Wiki Sync` / `Dry-run` job 失败 | dry-run 解析失败或 **lint-strict** 门禁 | §7 |
| 第二个 trigger 报「正在同步中」 | Redis 并发锁（KBOPS-2） | 等当前批次结束或 §6.4 |

**铁律**：健康体检扫 **MySQL**；Wiki 治理 `lint-space` 扫 **磁盘**。Sync 失败与「未 Sync」处理方式不同。

---

## 2. 三空间清单（勿搞错 `--wiki-dir` / `--space`）

与 `kb/tools/ci/run_sync.sh` 中 `KB_SPACES` 一致：

| wiki 目录 | `space_code` |
|-----------|--------------|
| `wiki/` | `enterprise-kb` |
| `wiki-moli/` | `moli-ops-manual` |
| `wiki-jp-exam/` | `jp-fe-ap-exam` |

---

## 3. 怎么看失败（KBOPS-1 可观测）

### 3.1 Web API（需 `kb:sync:trigger` 或空间 admin）

```bash
BASE=http://127.0.0.1:21000/KnowledgeServer
TOKEN=login_token_xxx

# 最近一批统计（failCount > 0 表示本批有 fail 行）
curl -s "$BASE/kb/sync/status?spaceId=900000000000000001" \
  -H "Authorization: $TOKEN" | jq .

# 分页查日志（status=fail 的行带 message）
curl -s "$BASE/kb/sync/logs?spaceId=900000000000000001&pageNum=1&pageSize=20" \
  -H "Authorization: $TOKEN" | jq .
```

关注字段：

| 字段 | 含义 |
|------|------|
| `batchNo` | 批次号（`YYYYMMDDHHMMSS`） |
| `failCount` | 本批 `status=fail` 行数 |
| `actionCounts` | insert/update/skip/delete/**batch** 等 |
| 日志行 `status` | `success` / **`fail`** |
| 日志行 `message` | 单页错误或批次摘要（≤512 字符） |
| 日志行 `action=batch` | 批次汇总；整批异常回滚后仍可能有独立连接写入的 fail 行 |

### 3.2 MySQL 直查

```sql
-- 某空间最近失败行
SELECT batch_no, action, status, message, source_path, create_time
FROM kb_sync_log
WHERE space_id = (SELECT id FROM kb_space WHERE space_code = 'enterprise-kb')
  AND status = 'fail'
ORDER BY create_time DESC
LIMIT 20;

-- 某批次全貌
SELECT action, status, message, source_path
FROM kb_sync_log
WHERE batch_no = '20260709140000'
ORDER BY id;
```

### 3.3 脚本 / 应用日志

- **CLI**：stderr 含 `[error] 文档同步失败 {slug}` 或 `[error] 同步失败，已回滚`
- **knowledge-server**：`[sync]` 行输出脚本 stdout；失败时 `exitCode` 非 0
- **定时任务**：`KbSyncScheduler` 日志 `定时同步结束 spaceCode=... success=false exitCode=...`

### 3.4 `sync_to_db.py` 退出码

| 码 | 含义 |
|----|------|
| `0` | 成功（`fail=0`） |
| `1` | 有失败页、整批异常或 `stats.fail>0` |
| `2` | 缺 pymysql |
| `3` | 找不到 `space_code` |
| `4` | **未分类文档**（slug 一级目录不在 `kb_category.dir_slug`）；Sync 中止 |

---

## 4. 常见根因与处理

| 根因 | 识别 | 处理 |
|------|------|------|
| **未分类文档** | exit `4`；日志提示 `未绑定 kb_category.dir_slug` | 移入正确 wiki 子目录或 Web 建分类；应急 `--allow-uncategorized`（不推荐） |
| **单页 DB 错误** | 批次内部分 `action=sync` 且 `status=fail` | 看 `message` 修该 md/DB 约束；修后重跑 Sync |
| **整批回滚** | 仅 `action=batch` + fail，无 insert 行 | 看 `message`（超时、SQL、tags 等）；修根因后重跑 |
| **脚本超时** | API 报「同步脚本超时」 | 增大 `kb.sync.timeout-seconds`；排查大库/慢 SQL |
| **并发锁** | 「该空间正在同步中」 | 等待或查 Redis `kb:sync:lock:{spaceCode}` TTL |
| **Python/路径** | 「同步脚本不存在」 | 检查 `kb.sync.script-path`、`KB_SYNC_PYTHON` |
| **lint-strict CI 失败** | CI 在 dry-run 步失败，无写库 | §7 本地 `lint-strict-all` 修 wiki |
| **dry-run 解析失败** | CI dry-run-all 失败 | 修 frontmatter/slug/断链等解析错误 |

---

## 5. 怎么重跑 Sync

### 5.1 Web（单空间）

```bash
curl -X POST "$BASE/kb/sync/trigger?spaceId=900000000000000001" \
  -H "Authorization: $TOKEN"
```

对三空间各 trigger 一次，或使用定时任务配置的 `schedule-space-codes` 列表。

### 5.2 CLI · 三空间（推荐运维）

```bash
cd moli-knowledge

# 先门禁（与 CI 同款）
bash kb/tools/ci/run_sync.sh dry-run-all
bash kb/tools/ci/run_sync.sh lint-strict-all   # 见 §7

# 写库
export KB_SYNC_HOST=127.0.0.1 KB_SYNC_USER=root KB_SYNC_PASSWORD=xxx KB_SYNC_DB=moli
bash kb/tools/ci/run_sync.sh sync-all

# 验收：每空间至少 1 篇 active kb 文档、无未分类已发布页
bash kb/tools/ci/run_sync.sh verify-all
```

### 5.3 单空间示例

```bash
python kb/tools/sync_to_db.py \
  --host 127.0.0.1 --user root --password xxx --db moli \
  --wiki-dir wiki-moli --space moli-ops-manual
```

### 5.4 GitHub Actions

| 场景 | 操作 |
|------|------|
| PR 验证 | 自动跑 `dry-run-all` + **`lint-strict-all`**（KBOPS-A1） |
| 合并 main 后 | 自动 `sync-all` + `verify-all`（需 MySQL service） |
| 生产/远程库 | Actions → **KB Wiki Sync** → `workflow_dispatch` → target **remote**（配置 Secrets） |

### 5.5 失败告警（KBOPS-5）

生产启用：

```yaml
kb:
  sync:
    alert:
      enabled: true
      type: feishu   # 或 wecom
      webhook-url: ${KB_SYNC_ALERT_WEBHOOK_URL}
      scheduled-only: true   # false 则手动 trigger 失败也告警
```

---

## 6. 验收清单（重跑后）

- [ ] `GET /kb/sync/status` → 最近批 `failCount=0`（或已知可接受的部分 fail 已处理）
- [ ] `bash kb/tools/ci/run_sync.sh verify-all` 通过
- [ ] Web 抽检：三空间各打开 1 个 slug，正文与磁盘一致
- [ ] （可选）健康体检 → 扫描并落库，工单无新增异常

### 6.1 三空间 space_id 速查（种子默认）

| space_code | 常见 space_id |
|------------|---------------|
| `enterprise-kb` | `900000000000000001` |
| `jp-fe-ap-exam` | `900000000000000002` |
| `moli-ops-manual` | `900000000000000003` |

以库内 `kb_space` 为准。

---

## 7. CI 硬门禁（KBOPS-A1）

**PR / push 触达 wiki 路径时**，workflow [`.github/workflows/kb-sync.yml`](../../.github/workflows/kb-sync.yml) 的 `dry-run` job 会执行：

1. `dry-run-all` — 三空间解析 + 未分类校验（blocking）
2. **`lint-strict-all`** — 三空间 `lint.py --strict`（**blocking**，有 ERROR 或 WARN 即失败）

本地复现：

```bash
cd moli-knowledge
bash kb/tools/ci/run_sync.sh dry-run-all
bash kb/tools/ci/run_sync.sh lint-strict-all
```

修复 wiki 后重新 push。渐进治理期若仅改工具脚本、未改 wiki，仍会对**全库三空间**跑 strict（与 CI 一致）。

非阻塞报告（可选、本地自查）：

```bash
bash kb/tools/ci/run_sync.sh lint-all   # exit 0，仅打印问题
```

---

## 8. 升级与联系

| 项 | 文档 |
|----|------|
| 日常入库 + 治理 + Sync 顺序 | [knowledge-workbench-operations.md](knowledge-workbench-operations.md) §4 |
| 发布 Sync 步骤 | [v1-release-runbook.md](v1-release-runbook.md) |
| 回滚 | [rollback-guide.md](rollback-guide.md) |
| KBOPS 产品优先级 | [knowledge-ops-prd.md](../product/knowledge-ops-prd.md) |

---

## 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-10 | 初稿 KBOPS-A2：失败定位、重跑、三空间 verify、CI lint-strict 门禁 |
