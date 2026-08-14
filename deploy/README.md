# 生产部署（deploy/）

> **按云平台选正文**（勿混用安装命令）：

| 云平台 | 正文 | 系统 |
|--------|------|------|
| **腾讯云 CVM** | **[`腾讯云上线流程.md`](腾讯云上线流程.md)** | Ubuntu · 完整上线 |
| **腾讯云 CVM** | [`docs/ops/tencent-cloud-cvm-bootstrap.md`](../docs/ops/tencent-cloud-cvm-bootstrap.md) | Ubuntu · **仅基础环境安装** |
| **AWS EC2** | [`上线流程.md`](上线流程.md) | Amazon Linux |

工程索引：[`docs/ops/tencent-cloud-release-runbook.md`](../docs/ops/tencent-cloud-release-runbook.md)（腾讯）· [`docs/ops/v1-release-runbook.md`](../docs/ops/v1-release-runbook.md)（通用）

## 目录

| 路径 | 用途 |
|------|------|
| [`腾讯云上线流程.md`](腾讯云上线流程.md) | **腾讯云** 首次安装 + 迁移 + 上线检查表 |
| [`上线流程.md`](上线流程.md) | **AWS EC2** 上线检查表 + 上传 + systemd + Wiki Sync |
| [`linux/`](linux/) | `moli-*.service` · `moli-*.env.example` · `moli-service.sh` |
| [`user-center/`](user-center/) | `application-pro.yml` 模板 · `conf/moli-user-center.env`（**不提交 Git**） |
| [`gateway/`](gateway/) | 网关配置模板 |
| [`knowledge/`](knowledge/) | 知识库配置模板 |

## v1 最小三件套

user-center · knowledge-server · gateway → 详见对应云平台 **`上线流程`** §1–§4。

**部署布局**：整仓 `git pull` 用 **方式 A**（JAR 在 `*/target/`）；rsync/scp 换包用 **方式 B**（JAR 在服务根目录）。二者勿混用。

**知识库插图（T22）**：Sync 只进 MySQL；B/D 档另传 **`kb/tools/raw-asset-bundle.tar.gz`**，见各平台上线流程 §插图包。

## 敏感文件

`conf/moli-*.env`、`application-pro.yml` 含密码与密钥，**仅留在部署机**；仓库内只保留 `.example` 与 yml 模板。

## `JAVA_HOME` 按平台

| 平台 | 示例 |
|------|------|
| AWS EC2 | `/usr/lib/jvm/java-11-amazon-corretto` |
| 腾讯云 Ubuntu | `/usr/lib/jvm/java-11-openjdk-amd64` |

模板 `moli-*.env.example` 默认为 AWS 路径；腾讯云部署时按 [`腾讯云上线流程.md`](腾讯云上线流程.md) §3.1 修改。
