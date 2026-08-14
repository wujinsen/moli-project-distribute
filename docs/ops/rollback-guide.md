# 发布回滚指南

> 配合 [v1-release-runbook.md](v1-release-runbook.md) 使用 · v1 最小回滚 playbook

---

## 1. 何时回滚

- 冒烟 [release-smoke-checklist.md](../test/release-smoke-checklist.md) **P0 多项失败**
- 核心路径不可用：登录、知识库浏览、网关 503
- 数据错误且无法热修（罕见）

---

## 2. 应用回滚（推荐首选）

### 2.1 步骤

1. **停止**当前版本各服务（逆序：gateway → 业务 → user-center）
2. **部署**上一稳定版本 JAR/镜像（保留配置不变）
3. **启动**（正序：user-center → 业务 → gateway）
4. 执行冒烟 **G1–G3、K1–K2** 最小集

### 2.2 配置

- Nacos 若已改配置，**回退配置版本**或还原备份
- 勿在回滚中同时改 DB

---

## 3. 数据库回滚

**风险高** — v1 增量 SQL **多数无 down 脚本**。

| 情况 | 建议 |
|------|------|
| 发版前已备份 | **还原备份**（staging 先验证） |
| 仅应用 bug | **只回滚应用**，不动 DB |
| 错误增量 SQL | 手工写 reverse DDL（需 DBA 评审） |

发版前必做：[production-checklist.md](production-checklist.md) §5 备份。

---

## 4. Wiki / Sync 回滚

知识库正文源在 **Git + `kb/wiki*`**：

1. `git revert` 或 checkout 上一 tag 的 wiki 目录
2. 重新执行：

```bash
bash moli-knowledge/kb/tools/ci/run_sync.sh dry-run-all
bash moli-knowledge/kb/tools/ci/run_sync.sh sync-all
```

3. Web 浏览验证 + 健康体检

**注意**：Sync 不会自动「撤销」已删页的 DB 软删，需 wiki 文件与 Git 一致。

---

## 5. Redis / 秒杀

| 场景 | 动作 |
|------|------|
| 秒杀库存错乱 | loadtest：`POST /seckill/admin/init` 重置；生产清 key 前缀 `moli:seckill:` |
| Session 污染 | 一般不整库 flush；让用户重新登录 |

---

## 6. 回滚后沟通

- 记录：版本号、回滚时间、根因、是否动 DB
- 更新 [wiki/guides/事故复盘-postmortem.md](../../moli-knowledge/kb/wiki-moli/guides/事故复盘-postmortem.md)（若影响用户）

---

## 7. 相关

- [monitoring-and-logs.md](monitoring-and-logs.md)
- [sql-migration-order.md](sql-migration-order.md)
