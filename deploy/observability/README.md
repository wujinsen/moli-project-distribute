# 茉莉可观测性栈 · 本地/预发验证

> Prometheus + Grafana + Loki + Alloy + SkyWalking。  
> 设计与实施阶段：`docs/design/observability-platform-plan.md`

## 1. 端口

| 组件 | 本机地址 |
|------|----------|
| Prometheus | http://127.0.0.1:29090 |
| Alertmanager | http://127.0.0.1:29093 |
| Grafana | http://127.0.0.1:28300 |
| Loki API | http://127.0.0.1:28110 |
| Alloy UI | http://127.0.0.1:28111 |
| SkyWalking UI | http://127.0.0.1:28120 |
| SkyWalking OAP gRPC | `127.0.0.1:28121` |
| SkyWalking OAP HTTP | http://127.0.0.1:28122 |
| BanyanDB UI（可选） | http://127.0.0.1:28123 |

Grafana 默认账号为 `admin` / `admin`。启动前应通过环境变量修改：

```powershell
$env:GRAFANA_ADMIN_PASSWORD = "<strong-password>"
```

## 2. 启动

先启动至少一个 Java 服务，再执行：

```powershell
cd D:\work\moli_project\moli-project-distribute
docker compose -f deploy/observability/docker-compose.observability.yml up -d
docker compose -f deploy/observability/docker-compose.observability.yml ps
```

停止：

```powershell
docker compose -f deploy/observability/docker-compose.observability.yml down
```

清空本地监控数据：

```powershell
docker compose -f deploy/observability/docker-compose.observability.yml down -v
```

## 3. 首次验证

1. 服务指标：`http://127.0.0.1:28101/actuator/prometheus`
2. Prometheus → **Status / Targets**：已启动服务应为 `UP`；**Alerts** 应加载 `moli-core`（`MoliServiceDown` / `MoliHttp5xx` / `MoliHeapHigh`）
2b. Alertmanager http://127.0.0.1:29093 ：firing 后 webhook 到本机 `moli-aiops :28105/hooks/alertmanager`（Bearer `moli-local-alert-webhook`，需先起 AIOps 并设置相同 secret）
3. Grafana → **Dashboards / Moli / Moli Observability Overview**（http://127.0.0.1:28300，admin/admin）
4. Grafana → **Dashboards / Moli / Moli Trace Logs**：SkyWalking 复制 Trace ID 前 32 位 → 一次查全服务日志（操作见 `docs/ops/monitoring-and-logs.md` §4）
5. Grafana → **Explore / Loki**：查询 `{service="user-center-server"}`（时间范围选 **Last 7 days** 或更长；若只选 Last 1 hour 而服务近期未写日志文件，会显示 No logs found）
6. SkyWalking UI：http://127.0.0.1:28120 ，启用 Java Agent 后应出现对应服务和 Trace  
   - 服务名用 `knowledge-server`（不要选幽灵名 `moli-knowledge`）  
   - 右上角保持 **UTC+8**（OAP 已设 `Asia/Shanghai`）。「最近一小时」按北京时间查，才能对上 logback 日志时间。不要改成 UTC+0。  
   - Trace 列表经 `skywalking-graphql-compat`（Caddy h2c + Python 改写）把 UI 10.4 的 `queryBasicTraces` 转成 BanyanDB 的 `queryTraces`；**改 compose 后需 `up -d` 并硬刷新浏览器**

**SkyWalking Agent ≠ Loki 日志**：`-javaagent` 只把 **Trace/拓扑** 上报到 OAP（28121），**不会**把日志推到 Loki。Grafana 里的日志来自 Alloy 采集各模块目录下的 `*.log` 文件（见 §4）。

常见访问误区：

- `http://127.0.0.1:28110` 根路径返回 **404 是正常的**；Loki 是 API，不是网页。健康检查用 `http://127.0.0.1:28110/ready`
- SkyWalking UI 报 **Internal Server Error**，通常是 OAP 未就绪。先执行 `docker compose ... ps`，确认 `moli-skywalking-oap` 为 `Up`，不是 `Restarting`

未启动的 Java 服务在 Prometheus 中显示 `DOWN` 属于预期，不影响其他服务。

## 4. 日志采集

本地 Alloy 只读挂载仓库，并采集：

- 根目录 `logs/*.log`（标为 `service=unknown-root`）
- `moli-gateway/logs/*.log`
- 各 `moli-*-server/logs/*.log`

为了避免多个服务都写入根 `logs/` 后无法区分，IDEA Run Configuration 应为每个服务设置：

```text
WORKING_DIRECTORY = $MODULE_WORKING_DIR$
```

并在各服务 `application-dev.yml` 中设置：

```yaml
logging:
  file:
    path: ./logs
```

或显式 JVM 参数：`-Dlogging.file.path=<service-module>/logs`

**Grafana 查不到日志 / 查询一直转圈时排查**：

1. **先点 Cancel，确认 Live 已关闭**（Explore 右上角 **Live** 按钮）。Live 走 WebSocket 长连接 tail，没有新日志时会一直转圈，看起来像「卡住」；Range 查询应在 1 秒内返回。
2. **Options 里 Legend 留空**（不要填 `error`）。Legend 只用于 Metrics 查询；筛 ERROR 请用 LogQL：`{service="user-center-server"} | level="ERROR"`。
3. 时间范围：服务刚重启时用 **Last 15 minutes**；若仍无结果，改 **Last 1 hour** 或 **Last 7 days**。
4. 模块 `logs/` 下是否有 `user-center-server.log` 等（IDEA 工作目录应为 `$PROJECT_DIR$`，见 `docs/ops/idea-local-dev.md`）。
5. Loki 直连验证（应秒回）：  
   `curl "http://127.0.0.1:28110/loki/api/v1/label/service/values"`  
   `curl -G "http://127.0.0.1:28110/loki/api/v1/query_range" --data-urlencode 'query={service="user-center-server"}' --data-urlencode limit=5 --data-urlencode start=0 --data-urlencode end=$(date +%s)000000000`（PowerShell 需自行算 start/end 纳秒时间戳）
6. Alloy / Loki 异常时：`docker compose -f deploy/observability/docker-compose.observability.yml restart loki alloy grafana`

生产环境不要直接复用本地挂载路径；应在业务主机安装 Alloy，采集：

```text
/opt/moli-project-distribute/moli-*/logs/*.log
```

## 5. SkyWalking Agent

Compose 启动的是 OAP/UI，不会自动修改业务 JVM。业务 JVM 通过外部 Agent 接入。

**目录结构（必须）**：`-javaagent` 指向的 JAR 同级要有 `plugins/`、`config/`，否则只有 JVM 心跳、**无 HTTP/Dubbo Trace**（UI 里服务在列表但 Trace 永远 0 条）。

```text
deploy/observability/skywalking-agent/
  skywalking-agent.jar
  plugins/          ← Spring MVC、Tomcat、Dubbo 等插件
  config/agent.config
```

解压示例（Windows，在 `deploy/observability/skywalking-agent/` 下已有 `apache-skywalking-java-agent-9.7.0.tgz` 时）：

```powershell
cd deploy/observability/skywalking-agent
tar -xzf apache-skywalking-java-agent-9.7.0.tgz
robocopy apache-skywalking-java-agent-9.7.0\skywalking-agent . /E
# macOS 解压会带入 ._ 元数据文件，Windows 上必须删除，否则插件全部加载失败、Trace 永远为空：
Get-ChildItem . -Recurse -Filter "._*" -File | Remove-Item -Force
```

**Gateway（WebFlux）** 还需把 optional 插件拷入 `plugins/`（茉莉 Spring Cloud Hoxton 用 `apm-spring-cloud-gateway-2.1.x` + `apm-spring-webflux-5.x`）。

VM 参数（与 `.run/* (dev).run.xml` 一致）：

```text
-javaagent:$PROJECT_DIR$/deploy/observability/skywalking-agent/skywalking-agent.jar
-Dskywalking.agent.service_name=user-center-server
-Dskywalking.collector.backend_service=127.0.0.1:28121
```

**日志 ≠ Trace**：Loki/Alloy 采集 `logs/*.log`；SkyWalking 只采集 Agent 上报的 Span，读 log 文件不会出现 Trace。

Linux `moli-service.sh` 已支持 `SKYWALKING_ENABLED` 等环境变量；见 `deploy/linux/moli-*.env.example`（生产 Agent 同样需完整目录，如 `/opt/skywalking-agent/`）。

本地 Agent 建议使用支持 Java 8 字节码的 SkyWalking Java Agent **9.x**；Agent 与 OAP 版本需在预发验证（当前 Compose OAP **10.4**）。

## 6. 版本说明

- Prometheus `2.51.2`、Grafana `10.4.2`：沿用现有压测栈，降低首次迁移风险。
- Loki `3.7.7`、Alloy `1.19.2`：固定版本；Promtail 已 EOL。
- SkyWalking OAP/UI `10.4.0` + BanyanDB `0.10.3`：SkyWalking 10.2+ 已移除 H2，OAP 10.4 需要 BanyanDB API 0.10。  
  UI 10.4 Trace 页仍可能走 `queryBasicTraces`（BanyanDB 已禁用），Compose 中用 `skywalking-graphql-compat` 转成 `queryTraces`。

## 7. 生产限制

该 Compose 用于单机开发和预发 PoC：

- SkyWalking 使用 BanyanDB standalone；容器重建后 Trace 不保证长期保留。
- Loki 使用单机 filesystem。
- 未配置 TLS、统一认证和高可用。Alertmanager 已接入本地 webhook（PoC secret，生产必须更换）。

生产上线前必须改为独立监控主机、正式存储、内网 ACL、备份与容量告警。
