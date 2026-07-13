# 腾讯云发布 Runbook（工程索引）

> **正文 SOP**：[`deploy/腾讯云上线流程.md`](../../deploy/腾讯云上线流程.md)  
> **仅装 CVM 基础环境**（JDK/MySQL/Redis/Nginx）：[tencent-cloud-cvm-bootstrap.md](tencent-cloud-cvm-bootstrap.md)  
> **AWS 专版**：[`deploy/上线流程.md`](../../deploy/上线流程.md)  
> **通用发布步骤**（与云平台无关）：[v1-release-runbook.md](v1-release-runbook.md)  
> 更新：2026-07-13

---

## 1. 文档分工

| 文档 | 云平台 | 系统 |
|------|--------|------|
| **[deploy/腾讯云上线流程.md](../../deploy/腾讯云上线流程.md)** | **腾讯云 CVM** | Ubuntu 22.04 / 24.04 · 完整上线 |
| **[tencent-cloud-cvm-bootstrap.md](tencent-cloud-cvm-bootstrap.md)** | **腾讯云 CVM** | Ubuntu · **仅基础环境安装 + 自检** |
| [deploy/上线流程.md](../../deploy/上线流程.md) | **AWS EC2** | Amazon Linux |
| [v1-release-runbook.md](v1-release-runbook.md) | 通用 | DB / 配置 / 启动顺序 / Sync |
| [production-checklist.md](production-checklist.md) | 通用 | 安全与配置项 |

**勿混用**：腾讯云 CVM 上执行 `yum`（AWS 命令）会失败；AWS EC2 上执行 `apt`（本文命令）会失败。

---

## 2. 腾讯云迁移路径（AWS → 腾讯云）

```
1. CVM 购机 + 安全组（80/443 公网，3306/6379/8848/21000 内网）
2. SSH：ubuntu@<弹性IP>（腾讯云 Ubuntu 默认用户）
3. §3 安装 JDK11 / MySQL8 / Nginx / Redis / Nacos（腾讯云上线流程）
4. mysqldump 从 AWS 导入 moli 库
5. rsync/scp（`ubuntu@<IP>`）JAR + kb + deploy 配置到 /opt/moli-project-distribute
6. 修改 moli-*.env：JAVA_HOME、DB/Redis/Nacos 地址
7. 启服 + Nginx + sync-all + 冒烟
8. DNS 切流到新弹性 IP
```

---

## 3. 与通用 Runbook 的衔接

| 阶段 | 看哪份文档 |
|------|------------|
| SQL 增量顺序 | [sql-migration-order.md](sql-migration-order.md) |
| 发布后冒烟 | [../test/release-smoke-checklist.md](../test/release-smoke-checklist.md) |
| Sync 失败 | [kb-sync-failure-runbook.md](kb-sync-failure-runbook.md) |
| 回滚 | [rollback-guide.md](rollback-guide.md) |
| 插图 T22 | [../test/knowledge-t22-image-remediation.md](../test/knowledge-t22-image-remediation.md) |

---

## 4. 浏览版（wiki-moli）

Web 侧摘要：[`wiki-moli/ops/腾讯云生产部署指南.md`](../../moli-knowledge/kb/wiki-moli/ops/腾讯云生产部署指南.md)（链到本目录与 `deploy/` 正文）。
