# 腾讯云 CVM · 基础环境安装与自检（Ubuntu）

> **适用**：腾讯云 CVM · **Ubuntu 22.04 / 24.04 LTS**（验证：24.04 Noble）  
> **完整上线**（JAR、Nginx 站点、Sync、冒烟）：[`deploy/腾讯云上线流程.md`](../../deploy/腾讯云上线流程.md)  
> **AWS 勿用本文**：Amazon Linux 见 [`deploy/上线流程.md`](../../deploy/上线流程.md)  
> 更新：2026-07-13

面向「新购腾讯云 Ubuntu 服务器，先装 **JDK 11 · MySQL 8 · Nginx · Redis · Nacos · Python**」，再进入应用部署。

---

## 1. 三个名字别混

| 名称 | 是什么 | 不是什么 |
|------|--------|----------|
| **`ubuntu`** | CVM **操作系统**登录用户（SSH、`/opt` 文件属主） | ❌ MySQL 账号 |
| **`root`** | **MySQL 管理员**（建库、导库、Java 连接） | ❌ 日常 SSH 用的 Linux 超级用户（装包用 `sudo`） |
| **`moli`** | **数据库名**（`CREATE DATABASE moli`） | ❌ MySQL 账号、❌ Linux 用户 |

应用 `conf/moli-*.env` 推荐：

```bash
DB_HOST=localhost
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=<强密码>
SPRING_REDIS_HOST=127.0.0.1
```

---

## 2. 登录与系统准备

```bash
ssh ubuntu@<弹性公网IP>
# 或控制台「标准登录」

sudo apt update && sudo apt upgrade -y
sudo timedatectl set-timezone Asia/Shanghai

sudo mkdir -p /opt/moli-project-distribute
sudo chown ubuntu:ubuntu /opt/moli-project-distribute
```

**安全组**：公网开放 **22 / 80 / 443**；**勿**对公网开放 3306、6379、8848、8888、8090、21000。

---

## 3. 安装顺序（复制执行）

### 3.1 JDK 11

```bash
sudo apt install -y openjdk-11-jdk
java -version
# 期望：openjdk version "11.x"
```

`JAVA_HOME`（写入三份 `moli-*.env`）：

```bash
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
```

> Temurin 等替代方案见 [`deploy/腾讯云上线流程.md` §3.1](../../deploy/腾讯云上线流程.md)。

### 3.2 MySQL 8

```bash
sudo apt install -y mysql-server
sudo systemctl enable mysql
sudo systemctl start mysql

mysql --version
# 期望：mysql  Ver 8.0.xx-0ubuntu0.24.04.x ...

dpkg -l | grep -E 'mysql-server|mariadb'
# 期望：mysql-server-8.0；不应出现 mariadb-server
```

**改 root 认证**（新装 Ubuntu 默认 `auth_socket`，`mysql -u root -p` 会 **ERROR 1698**）：

```bash
sudo mysql -u root
```

```sql
CREATE DATABASE IF NOT EXISTS moli
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '请替换为强密码';
FLUSH PRIVILEGES;
EXIT;
```

验证（`ubuntu` 用户，交互输入密码）：

```bash
mysql -u root -p moli -e "SELECT 1"
```

导入种子（仓库就位后）：

```bash
mysql -u root -p --default-character-set=utf8mb4 moli \
  < /opt/moli-project-distribute/scripts/moli.sql
```

增量 SQL 顺序：[`sql-migration-order.md`](sql-migration-order.md)。

### 3.3 Nginx

```bash
sudo apt install -y nginx
sudo systemctl enable nginx
sudo systemctl start nginx
sudo nginx -t
curl -I http://127.0.0.1
```

站点反代配置见 [`deploy/腾讯云上线流程.md` §8](../../deploy/腾讯云上线流程.md) 与 wiki [[nginx反向代理与前端部署指南]]。

### 3.4 Redis 6+

```bash
sudo apt install -y redis-server
sudo sed -i 's/^supervised no/supervised systemd/' /etc/redis/redis.conf
sudo systemctl enable redis-server
sudo systemctl restart redis-server
redis-cli ping
# 期望：PONG
```

生产建议设置 `requirepass`，与各服务 `SPRING_REDIS_PASSWORD` 一致（改 `/etc/redis/redis.conf` 后 `sudo systemctl restart redis-server`）。

### 3.5 Nacos 2.x（单机）

```bash
cd /opt
sudo wget https://github.com/alibaba/nacos/releases/download/2.0.3/nacos-server-2.0.3.tar.gz
sudo tar -xzf nacos-server-2.0.3.tar.gz
sudo chown -R ubuntu:ubuntu /opt/nacos

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
cd /opt/nacos/bin
sh startup.sh -m standalone
```

控制台：`http://<内网IP>:8848/nacos`（**勿**公网开放 8848）。创建命名空间 **`prod`**。

### 3.6 Python3 + pymysql（Wiki Sync）

```bash
sudo apt install -y python3 python3-pip python3-venv
# 仓库就位后：
python3 -m pip install -r /opt/moli-project-distribute/moli-knowledge/kb/tools/requirements-sync.txt
python3 -c "import pymysql; print('pymysql OK')"
```

---

## 4. 安装后一键自检

在 CVM 上以 **`ubuntu`** 执行：

```bash
echo "=== JDK ===" && java -version 2>&1 | head -1
echo "=== MySQL ===" && mysql --version
echo "=== MySQL service ===" && systemctl is-active mysql
echo "=== Redis ===" && redis-cli ping
echo "=== Redis service ===" && systemctl is-active redis-server
echo "=== Nginx ===" && nginx -v 2>&1
echo "=== Nginx service ===" && systemctl is-active nginx
echo "=== Nacos (optional) ===" && curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8848/nacos/
echo ""
echo "=== DB moli ===" && mysql -u root -p moli -e "SELECT COUNT(*) AS tables_cnt FROM information_schema.tables WHERE table_schema='moli';"
```

| 检查项 | 期望 |
|--------|------|
| `java -version` | 11.x |
| `mysql --version` | **8.0** 开头（非 MariaDB） |
| `redis-cli ping` | `PONG` |
| `nginx -t` | syntax ok |
| `systemctl is-active mysql/redis-server/nginx` | `active` |
| `moli` 表数量 | 导入 `moli.sql` 后 > 0 |

---

## 5. 常见问题

| 现象 | 处理 |
|------|------|
| `ERROR 1698` root@localhost | §3.2：`sudo mysql` → `ALTER USER` 改 `mysql_native_password` |
| `mysql --version` 不是 8.0 | 勿装 `mariadb-server`；重装 `mysql-server-8.0` |
| `1045` / sync 失败 | `source moli-knowledge/conf/moli-knowledge.env` 后再 Sync |
| `java not found` | 设 `JAVA_HOME` 到 `/usr/lib/jvm/java-11-openjdk-amd64` |
| 安全组已开 80 仍无法访问 | 检查弹性公网 IP、Nginx、`ufw status` |
| Windows 上传脚本 `$'\r'` | `sed -i 's/\r$//' deploy/linux/*.sh moli-*/conf/moli-*.env` |

更多见 [`deploy/腾讯云上线流程.md` 附录 A](../../deploy/腾讯云上线流程.md)。

---

## 6. 下一步

| 阶段 | 文档 |
|------|------|
| 代码就位、env、启停三服务 | [`deploy/腾讯云上线流程.md` §4–§10](../../deploy/腾讯云上线流程.md) |
| SQL 增量 | [`sql-migration-order.md`](sql-migration-order.md) |
| 发布后冒烟 | [`../test/release-smoke-checklist.md`](../test/release-smoke-checklist.md) |
| Web 运维台 SSH 启停 | [`../api/operation-deploy-api.md`](../api/operation-deploy-api.md) + SQL `21`/`22` |
| 浏览版摘要 | wiki [[腾讯云生产部署指南]] |

---

## 7. 相关

- [`tencent-cloud-release-runbook.md`](tencent-cloud-release-runbook.md) — 工程索引  
- [`deploy/README.md`](../../deploy/README.md) — 按云平台选正文  
- [`production-checklist.md`](production-checklist.md) — 配置与安全检查
