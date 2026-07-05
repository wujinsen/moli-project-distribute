# 生产部署（deploy/）

> **正文 Runbook**：[`deploy/上线流程.md`](上线流程.md) · 工程索引 [`docs/ops/v1-release-runbook.md`](../docs/ops/v1-release-runbook.md)

## 目录

| 路径 | 用途 |
|------|------|
| [`上线流程.md`](上线流程.md) | **生产上线检查表 + 上传 + systemd 启停 + Wiki Sync** |
| [`linux/`](linux/) | `moli-*.service` · `moli-*.env.example` · `moli-service.sh` |
| [`user-center/`](user-center/) | `application-pro.yml` 模板 · `conf/moli-user-center.env`（**不提交 Git**） |
| [`gateway/`](gateway/) | 网关配置模板 |
| [`knowledge/`](knowledge/) | 知识库配置模板 |

## v1 最小三件套

user-center · knowledge-server · gateway → 详见 [`上线流程.md`](上线流程.md) §1–§4。

## 敏感文件

`conf/moli-*.env`、`application-pro.yml` 含密码与密钥，**仅留在部署机**；仓库内只保留 `.example` 与 yml 模板。
