# 生产部署模板（可提交 Git）

> **真实密钥与覆盖配置**放在仓库根目录 **`pro/`**（已 `.gitignore`，勿提交）。  
> 本目录仅含 **`.example` 模板**与启停脚本。

---

## 1. 目录约定

monorepo 整仓检出到 **`/opt/moli-project-distribute`**：

| 路径 | 内容 |
|------|------|
| `/opt/moli-project-distribute/moli-user-center/` | `*.jar` + `application-pro.yml` + `conf/moli-user-center.env` |
| `/opt/moli-project-distribute/moli-gateway/` | `moli-gateway-*.jar` + 配置 |
| `/opt/moli-project-distribute/moli-knowledge/` | `moli-knowledge-server-*.jar` + 配置 |
| `/opt/moli-project-distribute/moli-knowledge/kb/` | wiki 源（随仓库，Sync 用） |
| `/opt/moli-project-distribute/deploy/linux/moli-service.sh` | 启停脚本（无需复制） |

---

## 2. 本地 `pro/` 初始化（Windows）

```powershell
cd D:\work\moli_project\moli-project-distribute
.\deploy\setup-pro.ps1
```

生成 `pro/上线流程.md`（上线步骤）及各服务配置。

---

## 3. Linux 部署步骤

```bash
# 0. 检出（或 rsync）到 /opt/moli-project-distribute
cd /opt/moli-project-distribute

# 1. 构建
mvn -pl moli-user-center/moli-user-center-server,moli-gateway,moli-knowledge/moli-knowledge-server -am package -DskipTests

# 2. 运行目录
sudo mkdir -p moli-user-center/{conf,run,logs} moli-gateway/{conf,run,logs} moli-knowledge/{conf,run,logs}

# 3. 配置（改密码后再启动）
sudo cp deploy/linux/user-center.env.example moli-user-center/conf/moli-user-center.env
sudo cp deploy/application-pro/user-center.yml.example moli-user-center/application-pro.yml
sudo cp deploy/linux/gateway.env.example moli-gateway/conf/moli-gateway.env
sudo cp deploy/application-pro/gateway.yml.example moli-gateway/application-pro.yml
sudo cp deploy/linux/knowledge.env.example moli-knowledge/conf/moli-knowledge.env
sudo cp deploy/application-pro/knowledge.yml.example moli-knowledge/application-pro.yml
sudo chmod 600 moli-*/conf/*.env
sudo chmod +x deploy/linux/moli-service.sh

# 4. JAR 放到各模块目录
sudo cp moli-user-center/moli-user-center-server/target/moli-user-center-server-*.jar moli-user-center/
sudo cp moli-gateway/target/moli-gateway-*.jar moli-gateway/
sudo cp moli-knowledge/moli-knowledge-server/target/moli-knowledge-server-*.jar moli-knowledge/

# 5. 启动（顺序）
./deploy/linux/moli-service.sh user-center start
./deploy/linux/moli-service.sh knowledge start
./deploy/linux/moli-service.sh gateway start
```

可选 systemd：`deploy/linux/*.service` → `/etc/systemd/system/`（已写死 `/opt/moli-project-distribute` 路径）

---

## 4. Spring profile `pro`

JAR 内**无** `application-pro.yml`。生产需外挂：

1. `moli-xxx/application-pro.yml`（从 `deploy/application-pro/*.yml.example`）
2. `moli-xxx/conf/moli-xxx.env`（从 `deploy/linux/*.env.example`）
3. `SPRING_PROFILES_ACTIVE=pro`

---

## 5. 相关

- [docs/ops/production-checklist.md](../docs/ops/production-checklist.md)
- [docs/ops/v1-release-runbook.md](../docs/ops/v1-release-runbook.md)
