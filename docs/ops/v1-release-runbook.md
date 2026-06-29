# v1.0 发布 Runbook

> **范围**：[moli-v1-release-scope.md](../product/moli-v1-release-scope.md)  
> **冒烟**：[release-smoke-checklist.md](../test/release-smoke-checklist.md)  
> 更新：2026-06-28

---

## 1. 发布前检查

1. 代码已合并目标分支，CI 通过（含 knowledge `lint-strict` 若启用）
2. 完成 [production-checklist.md](production-checklist.md)
3. 确认发布窗口与回滚联系人

---

## 2. 数据库

### 2.1 新环境（全量）

```powershell
cd D:\work\moli_project\moli-project-distribute
.\scripts\init-db.ps1
```

等价于：`moli.sql` → `02_seckill_schema.sql` → `03_knowledge_schema.sql` → `04_knowledge_menu.sql` → `07_kb_space_ops_manual.sql`（及脚本内其它增量）。

### 2.2 已有库（仅增量）

详见 **[sql-migration-order.md](sql-migration-order.md)**（完整顺序 + 判断是否已执行）。

**字符集**：含中文脚本必须用 `utf8mb4` + `source`，见 [scripts/README.md](../../scripts/README.md)。

---

## 3. 配置

| 组件 | 检查项 |
|------|--------|
| Nacos | 各服务 `bootstrap.yml` namespace、数据源、Redis |
| Redis | 全服务 **同一 database**（dev 默认 1） |
| 知识库 LLM | `kb.llm` 或平台设置 API；无 Key 可降级 |
| MinIO | 附件功能需要；可跳过 |
| 网关 | `application-dev.yml` 四路由存在 |

生产密钥 **不要** 提交 Git；使用 Nacos 或环境变量覆盖。

---

## 4. 服务启动顺序

```
1. Nacos、MySQL、Redis
2. user-center-server     (:8888)
3. order-server           (:8087)
4. bi-server              (:1128)    # 可选
5. knowledge-server       (:8090)
6. moli-gateway           (:21000)   # 最后
7. meiling-ui 静态 / Nginx 反代      # 若有
```

本地详情：[wiki-ops/本地启动指南](../../moli-knowledge/kb/wiki-ops/guides/本地启动指南.md)

---

## 5. Wiki → DB 同步

发布含 wiki 变更时 **必须** Sync：

```bash
cd moli-knowledge/kb
bash tools/ci/run_sync.sh dry-run-all
bash tools/ci/run_sync.sh sync-all
```

或分空间：

```bash
python tools/sync_to_db.py --wiki-dir wiki --space enterprise-kb
python tools/sync_to_db.py --wiki-dir wiki-ops --space moli-ops-manual
python tools/sync_to_db.py --wiki-dir wiki-jp-exam --space jp-fe-ap-exam
```

Web 端：知识库 → 同步触发 / 健康体检。

---

## 6. 冒烟验收

执行 [release-smoke-checklist.md](../test/release-smoke-checklist.md) 全部 **P0** 项并签核。

---

## 7. 发布后

| 动作 | 说明 |
|------|------|
| 观察日志 | gateway / user-center / knowledge 无 ERROR 尖峰 |
| 验证登录 | 生产域名 + HTTPS 下 token 正常 |
| 备份 DB | 发布前快照 |
| 公告 | 知会测试/业务 v1 范围（见 release-scope） |

---

## 8. 回滚（简要）

详见 **[rollback-guide.md](rollback-guide.md)**。

| 层级 | 动作 |
|------|------|
| 应用 | 回退上一版本 JAR / 镜像，按相反顺序重启 |
| DB | **谨慎**：增量 SQL 一般无自动 down；优先应用回滚 + 数据修复脚本 |
| Wiki | Git 回退 wiki 提交 + 重新 `sync-all` |

---

## 9. 相关

- 生产检查：[production-checklist.md](production-checklist.md)
- 监控日志：[monitoring-and-logs.md](monitoring-and-logs.md)
- 回滚详情：[rollback-guide.md](rollback-guide.md)
- 知识库操作：[knowledge-workbench-operations.md](knowledge-workbench-operations.md)
- Docker/Nginx：[wiki-ops/docker部署指南](../../moli-knowledge/kb/wiki-ops/ops/docker部署指南.md)
