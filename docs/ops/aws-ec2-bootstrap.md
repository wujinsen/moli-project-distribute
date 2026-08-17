# AWS EC2 · 基础环境安装与自检（Amazon Linux）

> **适用**：AWS EC2 · **Amazon Linux 2023**（验证：`dnf` · 用户 `ec2-user`）  
> **亦参考**：Amazon Linux 2（包管理为 `yum`，见 §2 注）  
> **完整上线**（JAR、Nginx 站点、Sync、冒烟）：[`deploy/上线流程.md`](../../deploy/上线流程.md)  
> **腾讯云勿用本文**：Ubuntu 见 [`tencent-cloud-cvm-bootstrap.md`](tencent-cloud-cvm-bootstrap.md)  
> 更新：2026-08-17

面向「新购 EC2 · Amazon Linux，先装 **JDK 11 · MySQL 8 · Nginx · Redis · Nacos · Python**」，再进入应用部署。

---

## 1. 三个名字别混

| 名称 | 是什么 | 不是什么 |
|------|--------|----------|
| **`ec2-user`** | EC2 **操作系统**登录用户（SSH、`/opt` 文件属主） | ❌ MySQL 账号 |
| **`moli`（推荐）或 `root`** | **MySQL 应用账号**（建库、导库、Java 连接） | ❌ Linux 超级用户（装包用 `sudo`） |
| **`moli`** | **数据库名**（`CREATE DATABASE moli`） | ❌ 与 MySQL 账号同名时易混，见上 |

应用 `conf/moli-*.env` 推荐（与 `deploy/linux/moli-*.env.example` 一致）：

```bash
DB_HOST=127.0.0.1
SPRING_DATASOURCE_USERNAME=moli
SPRING_DATASOURCE_PASSWORD=<强密码>
SPRING_REDIS_HOST=127.0.0.1
JAVA_HOME=/usr/lib/jvm/java-11-amazon-corretto
```

> 若沿用 **`root` + 密码** 连库也可以，三份 env 里 `SPRING_DATASOURCE_USERNAME` 改为 `root` 即可；**勿**与 `ec2-user` 混淆。

---

## 2. 登录与系统准备

```bash
ssh ec2-user@<公网IP>
# 或控制台 EC2 Instance Connect

# 确认系统（2023 应有 dnf；2 为 yum）
cat /etc/os-release | head -5

sudo dnf update -y
sudo timedatectl set-timezone Asia/Shanghai

sudo mkdir -p /opt/moli-project-distribute
sudo chown ec2-user:ec2-user /opt/moli-project-distribute
```

**Security Group**：公网开放 **22 / 80 / 443**（22 建议限办公 IP 或 VPN）；**勿**对公网开放 3306、6379、8848、8888、8090、21000。

| 常见误操作 | 原因 |
|------------|------|
| `sudo apt install ...` → `apt: command not found` | Amazon Linux **没有 apt**，用 **`dnf`**（AL2 亦可用 `yum`） |
| `openjdk-11-jdk` | Ubuntu 包名；AWS 用 **`java-11-amazon-corretto-devel`** |

> **Amazon Linux 2**：下文命令把 `dnf` 换成 `yum` 即可；Redis 可为 `sudo amazon-linux-extras install redis6 -y`。MySQL 8 同样建议走 §3.2 官方 community 源，**勿**误装 MariaDB 当 MySQL 8 契约库。

---

## 3. 安装顺序（复制执行）

推荐顺序：**JDK 11 → MySQL 8 → Nginx → Redis → Nacos → Python3/pymysql**

### 3.0 装完快速自检

每步装完可跑 §4 全文；期望：Java **11** · MySQL **8.0** · Redis **PONG** · Nginx **active**。

### 3.1 JDK 11（Amazon Corretto）

```bash
sudo dnf install -y java-11-amazon-corretto-devel
java -version
ls -d /usr/lib/jvm/java-11-amazon-corretto
```

`JAVA_HOME`（写入三份 `moli-*.env`）：

```bash
JAVA_HOME=/usr/lib/jvm/java-11-amazon-corretto
```

> 腾讯云 Ubuntu 路径为 `/usr/lib/jvm/java-11-openjdk-amd64`，**勿**在 EC2 上照抄。

### 3.2 MySQL 8

Amazon Linux **默认源不含 MySQL 8**（常见为 MariaDB）。项目契约要求 **MySQL 8.x**，请用 **MySQL 官方 community 源**：

```bash
sudo dnf install -y wget
cd /tmp
sudo wget -O mysql80-community-release.rpm \
  https://dev.mysql.com/get/mysql80-community-release-el9-4.noarch.rpm
sudo dnf install -y mysql80-community-release.rpm
sudo rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2023

# 若仓库默认指向 8.4，改 pin 到 8.0（无 8.4 子库时可跳过 disable）
sudo dnf config-manager --disable mysql-8.4-lts-community 2>/dev/null || true
sudo dnf config-manager --disable mysql-tools-8.4-lts-community 2>/dev/null || true
sudo dnf config-manager --enable mysql80-community 2>/dev/null || true
sudo dnf config-manager --enable mysql-tools-community 2>/dev/null || true

# AL2023 的 releasever 是 2023，MySQL 源按 EL9 发布；须固定为 9，否则 repolist 有库但无包
sudo sed -i 's/$releasever/9/g' /etc/yum.repos.d/mysql-community.repo

sudo dnf makecache
sudo dnf repolist | grep -i mysql
sudo dnf list available 'mysql-community-server' | head -5

sudo dnf install -y mysql-community-server
sudo systemctl enable --now mysqld

mysql --version
# 期望：mysql  Ver 8.0.xx ...
```

**首次 root 密码**（community 安装常见为临时密码）：

```bash
sudo grep 'temporary password' /var/log/mysqld.log
# 记下末尾临时密码，下面 -p 输入它
mysql -u root -p
```

在 `mysql>` 中执行：

```sql
CREATE DATABASE IF NOT EXISTS moli
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'moli'@'localhost' IDENTIFIED BY '请替换为强密码';
GRANT ALL PRIVILEGES ON moli.* TO 'moli'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

验证（`ec2-user`，交互输入 **moli** 密码）：

```bash
mysql -u moli -p moli -e "SELECT 1"
```

导入种子（仓库就位后）：

```bash
mysql -u moli -p --default-character-set=utf8mb4 moli \
  < /opt/moli-project-distribute/scripts/moli.sql
```

增量 SQL 顺序：[`sql-migration-order.md`](sql-migration-order.md)。

### 3.3 Nginx

```bash
sudo dnf install -y nginx
sudo systemctl enable --now nginx
sudo nginx -t
curl -I http://127.0.0.1
```

**443 端口**：留给 Nginx HTTPS；**勿**让 `sshd` 长期监听 443（与 nginx 冲突）。

站点反代配置见 [`deploy/上线流程.md` §8](../../deploy/上线流程.md) 与 wiki [[nginx反向代理与前端部署指南]]。

### 3.4 Redis 6+

Amazon Linux 2023 包名为 **`redis6`**，CLI 为 **`redis6-cli`**（**没有** `redis-cli` 命令）。安装后服务默认 **disabled**，须显式 `enable --now`。

```bash
sudo dnf install -y redis6
sudo systemctl enable --now redis6
systemctl is-active redis6
# 期望：active

redis6-cli ping
# 期望：PONG
```

**查配置文件路径**（AL2023 上 **`/etc/redis6/redis.conf` 通常不存在**，勿硬编码）：

```bash
rpm -ql redis6 | grep '\.conf$'
# 常见：/etc/redis/redis.conf 或 /etc/redis6.conf
sudo find /etc -name '*redis*.conf' 2>/dev/null
```

**生产设密码**（路径以 `rpm -ql` 为准；与各服务 `SPRING_REDIS_PASSWORD` **完全一致**）：

```bash
sudo grep -n requirepass /etc/redis/redis.conf
sudo vi /etc/redis/redis.conf
# 取消注释或新增：requirepass 你的强密码

sudo systemctl restart redis6
redis6-cli -a 你的强密码 ping
# 期望：PONG
```

应用侧同步改 EC2 运行时 env（改完须 **restart** Java 服务）：

- `moli-user-center/conf/moli-user-center.env` → `SPRING_REDIS_PASSWORD=...`
- `moli-knowledge/conf/moli-knowledge.env` → 同上（**host/port/password/database 各服务必须一致**）

可选：创建 `redis-cli` 软链便于脚本习惯：

```bash
sudo ln -sf /usr/bin/redis6-cli /usr/local/bin/redis-cli
```

> AL2 若无 `redis6` 包：`sudo amazon-linux-extras install redis6 -y`，服务名可能是 `redis`，配置文件路径仍用 `rpm -ql redis6` 或 `redis` 查询。

### 3.5 Nacos 2.x（单机）

```bash
cd /opt
sudo wget https://github.com/alibaba/nacos/releases/download/2.0.3/nacos-server-2.0.3.tar.gz
sudo tar -xzf nacos-server-2.0.3.tar.gz
sudo chown -R ec2-user:ec2-user /opt/nacos

export JAVA_HOME=/usr/lib/jvm/java-11-amazon-corretto
cd /opt/nacos/bin
sh startup.sh -m standalone
```

控制台：`http://127.0.0.1:8848/nacos`（**勿**公网开放 8848）。创建命名空间 **`prod`**，与各服务 `NACOS_NAMESPACE=prod` 一致。

可选：用 systemd 托管 Nacos（见 [`deploy/上线流程.md`](../../deploy/上线流程.md) 或自行写 unit）。

### 3.6 Python3 + pymysql（Wiki Sync）

Amazon Linux 最小镜像常**只有 python3、没有 pip**：

```bash
sudo dnf install -y python3-pip
python3 -m pip --version

# 仓库就位后：
python3 -m pip install -r /opt/moli-project-distribute/moli-knowledge/kb/tools/requirements-sync.txt
python3 -c "import pymysql; print('pymysql OK', pymysql.__version__)"
```

若 `dnf` 无 `python3-pip`：

```bash
curl -sS https://bootstrap.pypa.io/get-pip.py -o /tmp/get-pip.py
python3 /tmp/get-pip.py --user
python3 -m pip install --user -r /opt/moli-project-distribute/moli-knowledge/kb/tools/requirements-sync.txt
```

Sync 前须 `source moli-knowledge/conf/moli-knowledge.env`，详见 [`deploy/上线流程.md` §9](../../deploy/上线流程.md)。

---

## 4. 安装后一键自检

在 EC2 上以 **`ec2-user`** 执行：

```bash
echo "=== OS ===" && cat /etc/os-release | grep PRETTY_NAME
echo "=== JDK ===" && java -version 2>&1 | head -1
echo "=== MySQL ===" && mysql --version
echo "=== MySQL service ===" && systemctl is-active mysqld
echo "=== Redis ===" && redis6-cli ping
echo "=== Redis service ===" && systemctl is-active redis6
echo "=== Nginx ===" && nginx -v 2>&1
echo "=== Nginx service ===" && systemctl is-active nginx
echo "=== Nacos (optional) ===" && curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8848/nacos/
echo ""
echo "=== DB moli ===" && mysql -u moli -p moli -e "SELECT COUNT(*) AS tables_cnt FROM information_schema.tables WHERE table_schema='moli';"
```

| 检查项 | 期望 |
|--------|------|
| `java -version` | 11.x · **Corretto** |
| `mysql --version` | **8.0** 开头（非 MariaDB） |
| `redis6-cli ping` | `PONG` |
| `nginx -t` | syntax ok |
| `systemctl is-active mysqld/redis6/nginx` | `active` |
| `moli` 表数量 | 导入 `moli.sql` 后 > 0 |

---

## 5. 常见问题

| 现象 | 处理 |
|------|------|
| `apt: command not found` | 本文 §2：用 **`dnf install`**，勿用 Ubuntu 文档 |
| `java: command not found` | §3.1 安装 Corretto；env 设 `JAVA_HOME` |
| `dnf install mysql-community-server` 仍 No match | 仓库已 enable 但 AL2023 `$releasever=2023` 与 EL9 源不匹配 → `sudo sed -i 's/$releasever/9/g' /etc/yum.repos.d/mysql-community.repo` 后 `dnf makecache` |
| `dnf install mysql-server` 装成 MariaDB | §3.2 走 **mysql-community-server** + 8.0 源 |
| `grep temporary password` 无输出 | 部分安装 root 无密码：`sudo mysql -u root` 进库改密 |
| `1045` / sync 失败 | 未 `source moli-knowledge.env`，用了默认 `root/12345678` |
| `python3: No module named pip` | §3.6 装 `python3-pip` 或 get-pip.py |
| `python: command not found` | EC2 只有 **`python3`** |
| Windows 上传脚本 `$'\r'` | `sed -i 's/\r$//' deploy/linux/*.sh moli-*/conf/moli-*.env` |
| 安全组已开 80 仍无法访问 | 弹性 IP、Nginx、`curl -I http://127.0.0.1` |
| 443 被占用 | `sudo ss -tlnp \| grep 443`；sshd 勿占 443 |
| `redis6` **inactive (dead)** / **disabled** | `sudo systemctl enable --now redis6`；仅 `dnf install` 不会自启 |
| `redis-cli: command not found` | AL2023 用 **`redis6-cli`**；或 `ln -sf /usr/bin/redis6-cli /usr/local/bin/redis-cli` |
| `grep: /etc/redis6/redis.conf: No such file` | 配置不在该路径 → `rpm -ql redis6 \| grep '\.conf$'` 或 `find /etc -name '*redis*.conf'` |
| Java 连 Redis 超时 / NOAUTH | Redis 未启动，或 `requirepass` 与 `SPRING_REDIS_PASSWORD` 不一致 |

更多应用层排错：[`deploy/上线流程.md` 附录 A](../../deploy/上线流程.md)。

---

## 6. 下一步

| 阶段 | 文档 |
|------|------|
| 代码就位、env、启停三服务 | [`deploy/上线流程.md` §5–§11](../../deploy/上线流程.md) |
| SQL 增量 | [`sql-migration-order.md`](sql-migration-order.md) |
| 发布后冒烟 | [`../test/release-smoke-checklist.md`](../test/release-smoke-checklist.md) |
| Web 运维台 SSH 启停 | [`../api/operation-deploy-api.md`](../api/operation-deploy-api.md) + SQL `21`/`22` |
| 配置与安全检查 | [`production-checklist.md`](production-checklist.md) |

---

## 7. 相关

- [`v1-release-runbook.md`](v1-release-runbook.md) — 通用发布步骤  
- [`deploy/README.md`](../../deploy/README.md) — 按云平台选正文  
- [`tencent-cloud-cvm-bootstrap.md`](tencent-cloud-cvm-bootstrap.md) — 腾讯云对照（**命令不可混用**）
