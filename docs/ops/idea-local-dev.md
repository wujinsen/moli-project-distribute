# IDEA 本地启动（免手配 Run Configuration）

> 配置落在 **Git 仓库**（`.run/` + `application-dev.yml`），重装 IDEA、换机器 `git clone` 后**不用重新填 VM 参数**。  
> 端口表：[local-dev-ports.md](local-dev-ports.md) · 可观测性：[monitoring-and-logs.md](monitoring-and-logs.md)

## 1. 推荐方式：用仓库里的 `(dev)` 配置

项目根 `.run/` 已提交五个 Spring Boot 配置：

| 配置名 | 模块 |
|--------|------|
| `UserCenter (dev)` | moli-user-center-server |
| `Gateway (dev)` | moli-gateway |
| `Knowledge (dev)` | moli-knowledge-server |
| `Order (dev)` | moli-order-server |
| `Ai (dev)` | moli-ai-server |

打开工程后 IDEA **自动加载** `.run/*.run.xml`。右上角 Run 下拉里选带 **`(dev)`** 的项即可。

**不要用** `AiApplication`、`KnowledgeApplication` 等 IDE 临时生成的配置（没有 Nacos / 日志 / Agent 环境）。

### 重装 IDEA 后只需做一次

1. **Run → Edit Configurations**
2. 若看到**同名两份**（一份来自 `.run`、一份旧 workspace），删掉**没有** Environment / VM options 的那份
3. 选中 `(dev)` 配置 → 勾选 **Store as project file**（应指向 `.run/xxx.run.xml`）
4. **Apply**

之后换 IDE 版本、重装，只要仓库在，配置随 Git 走。

## 2. 日志路径：不用写 D 盘绝对路径

`application-dev.yml`（dev profile）：

```yaml
logging:
  file:
    # 不设 MOLI_LOG_DIR 时，默认写到各模块目录（相对 monorepo 根），IDEA 工作目录在仓库根也能分服务落盘
    path: ${MOLI_LOG_DIR:moli-user-center/moli-user-center-server/logs}
```

`.run` 里统一（日志路径已写在 yml，**不必再设 MOLI_LOG_DIR**）：

```text
WORKING_DIRECTORY=$PROJECT_DIR$
Environment: NACOS_SERVER_ADDR=127.0.0.1:28548
VM options: -javaagent:$PROJECT_DIR$/deploy/observability/skywalking-agent/skywalking-agent.jar ...
```

**不要**在 VM options 里加 `-Dlogging.file.path=...`（尤其指向仓库根目录）。

| 服务 | 默认日志目录 | 日志文件 |
|------|-------------|----------|
| UserCenter | `moli-user-center/moli-user-center-server/logs/` | `user-center-server.log` |
| Gateway | `moli-gateway/logs/` | `moli-gateway.log` |
| Knowledge | `moli-knowledge/moli-knowledge-server/logs/` | `knowledge-server.log` |
| Order | `moli-order/moli-order-server/logs/` | `order-server.log` |
| Ai | `moli-ai/moli-ai-server/logs/` | `ai-server.log` |

五个服务 **logback 已统一**：每个服务一个主日志文件 + `archive/` 滚动归档；不再使用 `web_info.log` / `web_error.log` 等按级别拆分（旧文件可手动删除）。

生产 `pro` profile 不设 `MOLI_LOG_DIR`，`moli-service.sh` 会 `cd $APP_HOME` 后用 `./logs`；**文件名与 dev 一致**（见下表）。`moli-service.sh logs` 与 logback 默认指向同一 `{APP_NAME}.log`。

| 服务 | 生产 `LOG_FILE` / `logging.file.name` |
|------|--------------------------------------|
| UserCenter | `user-center-server.log` |
| Gateway | `moli-gateway.log` |
| Knowledge | `knowledge-server.log` |
| Order | `order-server.log` |
| Ai | `ai-server.log` |

模板：`deploy/*/application-pro.yml`、`deploy/linux/moli-*.env.example`。生产 `pro` 下 logback **只写文件**；`startup.log` 仅捕获 JVM 启动期非 logback 输出。

## 3. SkyWalking Agent

Agent 路径也用仓库宏（换盘符不用改）：

```text
-javaagent:$PROJECT_DIR$/deploy/observability/skywalking-agent/skywalking-agent.jar
```

首次使用需解压 Java Agent 9.x 到 `deploy/observability/skywalking-agent/`，**必须**保证与 `skywalking-agent.jar` 同级存在 `plugins/`、`config/` 目录（仅单独一个 JAR 只会注册服务名，**不会有 Trace**）。可从 `apache-skywalking-java-agent-9.7.0.tgz` 解压后复制 `skywalking-agent/*` 到该目录。详见 `deploy/observability/README.md` §5。

## 4. 不用 IDEA 时：Maven 启动（可选）

```powershell
cd D:\work\moli_project\moli-project-distribute
$env:NACOS_SERVER_ADDR = "127.0.0.1:28548"
$env:MOLI_LOG_DIR = "$PWD\moli-user-center\moli-user-center-server\logs"
mvn -pl moli-user-center/moli-user-center-server -am spring-boot:run "-Dspring-boot.run.profiles=dev"
```

脚本启停见 `deploy/linux/moli-service.sh`（Linux 生产/预发）。

## 5. 自检

重启 `UserCenter (dev)` 后：

```powershell
Get-Item "...\moli-user-center\moli-user-center-server\logs\user-center-server.log" | Select LastWriteTime
```

根目录 `moli-project-distribute\logs\` **不应再更新**（历史文件可删）。

若出现 **`logging.path_IS_UNDEFINED/`** 文件夹：Logback 在 Spring 注入 `logging.file.path` 之前解析了 `${logging.path}` 时的误产物，Knowledge/Ai 已修复；**整目录可删**，重启 `(dev)` 后不应再出现。

Grafana Loki：`{service="user-center-server"}`，时间选 **Last 15 minutes**。

查 MyBatis SQL（需 **Rebuild + 重启** 后生效，`Slf4jImpl` 写入 log 文件）：

```logql
{service="user-center-server"} |= "Preparing"
```

或 `{service="user-center-server"} | level="DEBUG"`。控制台仍可见 SQL，且会同步落盘供 Loki 采集。

## 6. Prometheus / Actuator（Shiro 服务）

### 6.1 快速验证

| 服务 | 端口 | 命令 |
|------|------|------|
| knowledge | 28104 | `curl.exe -s http://127.0.0.1:28104/actuator/prometheus` |
| order | 28102 | `curl.exe -s http://127.0.0.1:28102/actuator/health` |
| ai | 28103 | 同上 |
| user-center（对照） | 28101 | 应始终正常 |

正常：Prometheus 文本 `# HELP jvm_...`；health 为 `{"status":"UP"}`。  
异常：`{"code":10006,"msg":"请登录"}` → 见下节。

### 6.2 返回 10006 时（已踩坑记录）

**根因**：`moli-user-center-shiro-starter` 曾把 `AuthenticationFilter` 注册为 Spring `@Bean`，Boot 自动将其挂为**全局 Servlet Filter（`/*`）**，在 Shiro 链之外拦截 `/actuator/**`。Rebuild JAR、改 YAML `anon-paths`、检查 `.m2` 时间戳均**不能**单独解决。

**修复**：starter 改为在 `shiroFilterFactory` 内 `new AuthenticationFilter()`（与 user-center `ShiroConfig` 一致）。操作：

1. `mvn -pl moli-user-center/moli-user-center-shiro-starter,<服务模块> -am install -DskipTests`
2. IDEA **Stop → Rebuild → Run (dev)**
3. 再测 `/actuator/prometheus`

完整现象表、Prometheus Targets 排障、代码位置见 [`monitoring-and-logs.md`](monitoring-and-logs.md) §4.3。Wiki：[[茉莉-shiro-跨服务]]、[[故障排查指南]]。

### 6.3 Prometheus 栈

改 `deploy/observability/prometheus/prometheus.yml` 后：

```powershell
docker compose -f deploy/observability/docker-compose.observability.yml restart prometheus
```

Targets UI：http://127.0.0.1:29090/targets 。order/ai/knowledge 须对应端口进程已启动。

## 7. 知识库 wiki 根目录与 D7 漂移自检

本地 wiki 磁盘根目录（**两空间共用**）：

```text
D:\work\moli_project\moli-project-distribute\moli-knowledge\kb
  wiki/       → enterprise-kb
  wiki-moli/  → moli-ops-manual
```

`Knowledge (dev)` 已在 `.run/Knowledge (dev).run.xml` 注入 `KB_WIKI_ROOT=$PROJECT_DIR$/moli-knowledge/kb`；`application-dev.yml` 为 `kb.wiki.root: ${KB_WIKI_ROOT:moli-knowledge/kb}`。  
**改路径后须 Stop → Rebuild → Run**，否则 D7 仍可能 `scanEmpty` 或全 0。

### 7.1 命令行漂移（免登录，与后端口径一致）

在 `moli-knowledge/kb` 下：

```powershell
cd D:\work\moli_project\moli-project-distribute\moli-knowledge\kb

python tools/detect_wiki_db_drift.py --space enterprise-kb --password 12345678
python tools/detect_wiki_db_drift.py --wiki-dir wiki-moli --space moli-ops-manual --password 12345678
```

期望：`wikiPageTotal` / `in_sync` **大于 0**（enterprise-kb 约 474；moli-ops-manual 约 175+）。  
若 `wiki_only` / `hash_mismatch` > 0，在 Web「Wiki 同步」或 `python tools/sync_to_db.py --wiki-dir wiki-moli --space moli-ops-manual` 后再跑一遍。

### 7.2 运维看板 D7（需登录）

1. 启动 `UserCenter (dev)` + `Gateway (dev)` + `Knowledge (dev)`（Knowledge 须含最新 `ci/kb-sync-multi-space-gate` 分支，`GET /kb/sync/drift` 需 `kb:ops:dashboard`）。
2. meiling-ui 本地 dev（`:5141`）→ **知识库 → 运维看板**。
3. D7 应显示各空间 **Wiki 页数 / DB 页数**，而非「未检测」或全 0 +「已对齐」。

直连 API（带登录 Cookie / Token）：

```powershell
curl.exe -s "http://127.0.0.1:28104/kb/sync/drift?spaceId=900000000000000003&sampleLimit=5"
curl.exe -s "http://127.0.0.1:28104/kb/ops/dashboard?trendDays=7&spaceId=900000000000000003"
```

空间 ID：`enterprise-kb` = `900000000000000001`，`moli-ops-manual` = `900000000000000003`。

### 7.3 LLM 调用日志开关（可选）

本地库已执行 `docs/sql/41_kb_platform_llm_call_log_enabled.sql` 后，**系统管理 → 知识库 LLM** 可切换「记录 LLM 调用」；看板 D6 成本趋势在开启且有调用后才有数据。
