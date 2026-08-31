# 监控与日志 · v1 运维要点

> v1 **最低可运维**指南；完整建设方案见 [可观测性平台规划](../design/observability-platform-plan.md)（Prometheus + Grafana + Loki/Alloy + SkyWalking）。  
> 本地 PoC 启动：[`deploy/observability/README.md`](../../deploy/observability/README.md) · IDEA 启动：[`idea-local-dev.md`](idea-local-dev.md)  
> 发布：[production-checklist.md](production-checklist.md) · 排障：[wiki-moli/故障排查指南](../../moli-knowledge/kb/wiki-moli/ops/故障排查指南.md)

---

## 1. 日志位置与 Loki 链路

### 1.1 落盘规则

各服务 `logback-spring.xml`：**dev** = 控制台 + 文件；**pro** = 仅文件（`deploy/*/application-pro.yml` + `moli-service.sh`）。

| 服务 | 日志文件（dev 默认路径） | Loki `service` 标签 |
|------|--------------------------|---------------------|
| user-center | `moli-user-center/moli-user-center-server/logs/user-center-server.log` | `user-center-server` |
| gateway | `moli-gateway/logs/moli-gateway.log` | `moli-gateway` |
| order | `moli-order/moli-order-server/logs/order-server.log` | `order-server` |
| ai | `moli-ai/moli-ai-server/logs/ai-server.log` | `ai-server` |
| knowledge | `moli-knowledge/moli-knowledge-server/logs/knowledge-server.log` | `knowledge-server` |

IDEA 工作目录应为 **`$PROJECT_DIR$`**（仓库根），见 [idea-local-dev.md](idea-local-dev.md)。

### 1.2 MyBatis SQL 进 Loki

**dev / pro 均使用 `Slf4jImpl`**（勿用 `StdOutImpl` / `NoLoggingImpl`）。SQL 以 **DEBUG** 写入上述 log 文件，Alloy 秒级 tail → Loki。

改配置后须 **Rebuild + 重启** 服务；发版后 15 分钟内各服务 **无 ERROR 持续刷屏**。

### 1.3 不是什么

| 组件 | 作用 | 是否推送 Loki |
|------|------|---------------|
| **Alloy** | tail `*.log` 文件 | ✅ 采集源 |
| **SkyWalking Agent** | Trace / 拓扑 | ❌ |
| **IDEA 控制台** | stdout（旧版 MyBatis StdOutImpl） | ❌ |

---

## 2. 本地可观测性栈（PoC）

```powershell
cd D:\work\moli_project\moli-project-distribute
docker compose -f deploy/observability/docker-compose.observability.yml up -d
```

| 组件 | 地址 |
|------|------|
| Grafana | http://127.0.0.1:28300（admin/admin，请改密码） |
| Prometheus | http://127.0.0.1:29090 |
| Loki API | http://127.0.0.1:28110（根路径 404 正常，用 `/ready`） |
| SkyWalking UI | http://127.0.0.1:28120 |

完整端口表：[local-dev-ports.md](local-dev-ports.md)

---

## 3. Grafana Explore 查日志

1. **Explore** → 数据源 **Loki**
2. **关掉 Live**（右上角）；用 **Run query**
3. 时间范围：**Last 15 minutes** 或 **Last 1 hour**（Loki 按**日志行时间**过滤，不是采集时间）

### 3.1 Code 模式示例

```logql
{service="user-center-server"}
```

按内容（参数变更等业务日志）：

```logql
{service="user-center-server"} |= "覆盖值更新为"
```

ERROR：

```logql
{service="user-center-server"} | level="ERROR"
```

MyBatis SQL：

```logql
{service="user-center-server"} |= "Preparing"
```

### 3.2 Builder 模式

1. Label：`service` = `user-center-server`
2. **+ Operations** → **Line filter** → `|=` → 输入关键字（如 `覆盖值更新为`）
3. 查 SQL 时 **不要** 固定 `level=INFO`（SQL 为 DEBUG）

### 3.3 查不到时

见 [`deploy/observability/README.md`](../../deploy/observability/README.md)「Grafana 查不到日志 / 查询转圈」。

---

## 4. 健康检查

### 4.1 业务探测（推荐）

| 探测 | 命令/URL |
|------|----------|
| 网关 | `curl /OrderServer/seckill/ping` |
| 登录 | `POST /UserCenter/login` |
| 知识库 | `GET /KnowledgeServer/kb/index?spaceId=...` |

见 [release-smoke-checklist.md](../test/release-smoke-checklist.md)。

### 4.2 Actuator

五个 Java 服务统一暴露 `/actuator/health`、`/actuator/info`、`/actuator/prometheus`（与业务 **同端口**）。

带 Shiro 的服务（order / ai / knowledge）在 `moli-user-center-shiro-starter` 中将 `/actuator/**` 配为 anon，且 **`AuthenticationFilter` 不得注册为 Spring `@Bean`**（否则 Boot 会把它挂到 `/*`，绕过 Shiro 链）。Rebuild **`moli-user-center-shiro-starter`** + 对应服务后重启。

本地验证：`http://127.0.0.1:28104/actuator/prometheus` 应返回 `# HELP jvm_...` 文本，**不是** JSON `10006`。

`health` 不返回内部细节。生产必须用安全组/防火墙限制为管理网访问，**不得经公网或 Gateway 暴露**。

### 4.3 Prometheus / Actuator 排障（Shiro 服务）

**现象**

| 信号 | 说明 |
|------|------|
| 浏览器 / `curl` 访问 `/actuator/health` 或 `/actuator/prometheus` | 返回 `{"code":10006,"msg":"请登录"}` |
| Prometheus **Targets** 页 | order / ai / knowledge 为 **DOWN**；错误含 *invalid format* 或 *connection refused* |
| user-center（28101）同路径 | 正常 `{"status":"UP"}` 或 Prometheus 文本 |

**易误判（本次踩坑）**

1. **不是 JAR 没更新**：`.m2` 里 `moli-user-center-shiro-starter` 已含 `/actuator/**`，`javap` 也能看到，但问题仍在。
2. **不是 YAML `anon-paths` 没合并**：链上已有 anon，仍被拦截。
3. **management 独立端口不是根因**：拆端口只是绕过，未解决 Filter 重复注册。

**根因**

`moli-user-center-shiro-starter` 曾将 `AuthenticationFilter` 声明为 `@Bean`。Spring Boot 2.x 会把所有 `Filter` 类型 Bean **自动注册为 Servlet Filter（`/*`）**，与 Shiro `ShiroFilterFactoryBean` 内的链并行存在：

```
请求 /actuator/prometheus
  → Spring Boot 注册的全局 AuthenticationFilter（无 anon 概念）→ 10006
  → 即使 Shiro 链上已配 /actuator/** = anon 也无效
```

**user-center 为何正常**：`ShiroConfig` 使用 `new AuthenticationFilter()` 放入 Shiro 链，**未**暴露为 Spring `@Bean`。

**修复（已合入 starter）**

- 删除 `@Bean authenticationFilter()`；
- 在 `shiroFilterFactory(...)` 内 `new AuthenticationFilter()` 并 `setUserCenterServer`；
- `@ConditionalOnMissingBean(name = "shiroFilterFactory")` 便于服务自定义整链。

**操作步骤**

```powershell
# 1. 重装 starter + 业务模块
cd D:\work\moli_project\moli-project-distribute
mvn -pl moli-user-center/moli-user-center-shiro-starter,moli-knowledge/moli-knowledge-server -am install -DskipTests

# 2. IDEA：Stop → Rebuild Project → 重新 Run Knowledge (dev)

# 3. 验证（PowerShell 用 curl.exe）
curl.exe -s http://127.0.0.1:28104/actuator/health
curl.exe -s http://127.0.0.1:28104/actuator/prometheus | Select-Object -First 3
curl.exe -s http://127.0.0.1:28101/actuator/health   # 对照：user-center 应 UP
```

期望 knowledge：`{"status":"UP"}` 与 `# HELP jvm_...`。**不是** `10006`。

**Prometheus 仍 DOWN 时**

| 原因 | 处理 |
|------|------|
| 服务未启动（28102/28103/28104） | IDEA 启动 `Order (dev)` / `Ai (dev)` / `Knowledge (dev)` |
| 刚改 `prometheus.yml` | `docker compose -f deploy/observability/docker-compose.observability.yml restart prometheus` |
| Target 仍 scrape 旧端口 | 对照 `deploy/observability/prometheus/prometheus.yml` 与 `docs/ops/local-dev-ports.md` |

**代码位置**

| 文件 | 说明 |
|------|------|
| `moli-user-center-shiro-starter/.../UserCenterShiroAutoConfiguration.java` | Filter 链 + 禁止 `@Bean` AuthenticationFilter |
| `moli-user-center-server/.../ShiroConfig.java` | user-center 参考实现（`new AuthenticationFilter()`） |

Wiki 详述：[[茉莉-shiro-跨服务]] · [[故障排查指南]] § Prometheus。

---

## 5. 指标

| 服务 | 指标 |
|------|------|
| order | `GET /seckill/metrics` Redis 计数 |
| 五个 Java 服务 | `/actuator/prometheus` JVM、HTTP、线程、连接池等 |
| user-center | Druid 连接池自定义指标 |

压测栈见 [load-test/README.md](../../load-test/README.md)。

---

## 6. 生产部署要点

1. 业务主机安装 **Alloy**，采集 `/opt/moli-project-distribute/moli-*/logs/*.log`（勿复用本地 Docker 挂载路径）
2. 独立监控机运行 Prometheus、Grafana、Loki、SkyWalking OAP/UI
3. `application-pro.yml` 中 `log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl`
4. `SKYWALKING_ENABLED=true` 见 `deploy/linux/moli-*.env.example`

PoC Compose 单机适用开发/预发验证；**生产部署、日志可靠性、对象存储**见 [observability-production.md](observability-production.md)。

---

## 7. 知识库专项

| 场景 | 查看 |
|------|------|
| Sync 失败 | `kb_sync_log` 表、应用日志 |
| Lint CI 红 | `python kb/tools/lint.py --strict` |
| Ask 无 LLM | 平台 LLM 设置 / `kb.llm.usable()` |
| Web 页旧 | 是否执行 `sync-all` |

---

## 8. 告警（v1 可选）

- 网关 5xx 率 > 阈值
- MySQL/Redis 不可达
- knowledge-server 连续 Sync 失败

---

## 9. 相关

- [`deploy/observability/README.md`](../../deploy/observability/README.md)
- [rollback-guide.md](rollback-guide.md)
- [v1-release-runbook.md](v1-release-runbook.md)
- wiki [[监控与日志]] · [[可观测性平台]]
