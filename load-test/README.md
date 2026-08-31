# Moli 百万 QPS 压测（user-center + gateway + order）

面向 **user-center 登录/鉴权** + **gateway** + **order 秒杀** 全链路的分布式压测方案。

## 架构

```text
[k6 x N] ──▶ gateway:21000
                 ├── /UserCenter/loadtest/login、/user/list、/menu/getRouters  → user-center（压测登录走专用接口）
                 └── /OrderServer/seckill/order                      → order → Redis Lua
```

**百万 QPS 现实约束：** 单机 k6 通常上限约 **5k~30k RPS**（视 CPU/网络而定）。达到 **1,000,000 RPS** 需要：

| 组件 | 建议规模 |
|------|----------|
| k6 压测节点 | 50~200 台（每节点 5k~20k RPS） |
| gateway 实例 | 10+（前置 L4/L7 负载均衡） |
| user-center-server | 10~30+（登录/Session/Redis） |
| order-server | 20~50+ 无状态水平扩展 |
| Redis Cluster | 分片 + 本地缓存预热 |
| MySQL | 异步落库，主库仅消费队列 |

## 1. 准备环境

### 1.1 数据库

**请使用最新全库快照 `scripts/moli.sql`**。

```powershell
cd D:\work\moli_project\moli-project-distribute
.\scripts\init-db.ps1
```

或数据库工具里直接执行 `scripts/moli.sql`，再执行 `docs/sql/02_seckill_schema.sql`（秒杀压测需要）。

### 1.2 启动依赖

- Nacos `8848`
- Redis **`16379`**（database `1`，与 order 配置一致；Windows 本地见 `wiki-moli/guides/本地启动指南`）
- MySQL

### 1.3 启动服务（压测 profile）

```bash
# 三个服务均使用 loadtest profile
cd moli-user-center/moli-user-center-server && mvn spring-boot:run -Dspring-boot.run.profiles=loadtest

cd moli-gateway && mvn spring-boot:run -Dspring-boot.run.profiles=loadtest

cd moli-order/moli-order-server && mvn spring-boot:run -Dspring-boot.run.profiles=loadtest
```

### 1.4 初始化秒杀活动

```powershell
.\load-test\scripts\init-seckill-data.ps1 -Stock 1000000
```

或：

```bash
./load-test/scripts/init-seckill-data.sh
```

## 2. 压测 API

### 2.1 User-center（经 gateway `/UserCenter`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/UserCenter/loadtest/login` | **压测专用**登录（仅 `loadtest` profile，只返回 token + user） |
| POST | `/UserCenter/login` | 产品登录（含系统门户/菜单，**不要用于 k6 压测**） |
| GET | `/UserCenter/user/list` | 用户列表（需 Authorization） |
| GET | `/UserCenter/menu/getRouters` | 菜单路由（需 Authorization） |
| GET | `/UserCenter/actuator/prometheus` | Prometheus 指标（loadtest） |

登录请求体：`{"userName":"zhangsan","password":"123456"}`（演示用户默认密码见 `scripts/README.md`）

压测与业务分离：`POST /loadtest/login` 由 `@Profile("loadtest")` 的 `LoadtestLoginController` 提供，不写登录日志、不查菜单/门户；产品 `POST /login` 代码无压测分支。

### 2.2 Order 秒杀

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/OrderServer/seckill/ping` | 连通性基线（无鉴权） |
| GET | `/OrderServer/seckill/activity/{id}` | 查库存 |
| POST | `/OrderServer/seckill/order` | 秒杀下单（核心） |
| GET | `/OrderServer/seckill/metrics` | Redis 计数指标 |
| POST | `/OrderServer/seckill/admin/init` | 初始化活动（仅 `load-test-mode=true`） |

**下单请求体：**

```json
{
  "activityId": 1,
  "userId": "u-12345",
  "requestId": "optional-idempotency-key"
}
```

**响应：** `200` 成功 / `409` 售罄 / `429` 重复抢购

## 3. 压测脚本

### 3.1 user-center 登录冒烟

```powershell
.\load-test\scripts\run-user-center-smoke.ps1 -LoginPassword "123456"
```

脚本默认：

- 压测进行中打开 **http://127.0.0.1:5665**（k6 实时看板）
- 结束后导出 HTML 到 `load-test/reports/user-center-smoke-<时间>.html` 并自动用浏览器打开
- 不需要 HTML 时加 `-NoHtmlReport`

```bash
k6 run load-test/k6/user-center-login-smoke.js
```

### 3.2 秒杀冒烟（~100 RPS）

```powershell
.\load-test\scripts\run-smoke.ps1
```

```bash
k6 run load-test/k6/seckill-smoke.js
```

### 3.3 混合场景（登录 + 业务 API + 秒杀）

```bash
k6 run load-test/k6/mixed-login-seckill.js
```

### 3.4 阶梯加压

**登录 ramp 注意：** 直接 `k6 run user-center-login-ramp.js` 旧版目标最高 **5000 RPS**，单机 user-center 会超时，`login ok` 仅 ~30% **不是密码错**。请用本地档位或脚本：

```powershell
# 推荐：本地档位（最高 ~300 RPS）+ HTML 报告
.\load-test\scripts\run-user-center-ramp.ps1 -LoginPassword "123456"

# 多用户
.\load-test\scripts\run-user-center-ramp.ps1 -LoginPassword "123456" -LoginUserPool "zhangsan,lisi,wangwu"

# 全量压测（需集群）
.\load-test\scripts\run-user-center-ramp.ps1 -LoginPassword "123456" -StressRamp
```

```bash
k6 run -e LOGIN_PASSWORD=123456 load-test/k6/user-center-login-ramp.js
k6 run -e STRESS_RAMP=true -e LOGIN_PASSWORD=123456 load-test/k6/user-center-login-ramp.js
k6 run load-test/k6/seckill-ramp.js
```

### 3.5 百万 QPS 分布式 k6

**公式：** `aggregate_rps = workers × per_worker_rps`

```powershell
# 10  worker × 10k = 100k RPS（示例）
.\load-test\scripts\run-distributed-k6.ps1 -Workers 10 -TargetRps 100000 -Duration 5m

# 100 worker × 10k = 1M RPS（需多机/ K8s）
.\load-test\scripts\run-distributed-k6.ps1 -Workers 100 -TargetRps 1000000 -Duration 10m
```

```bash
WORKERS=100 TARGET_RPS=1000000 DURATION=10m ./load-test/scripts/run-distributed-k6.sh
```

### 3.4 网关基线（隔离 Redis）

```bash
k6 run -e TARGET_RPS=50000 load-test/k6/gateway-ping-baseline.js
```

### 3.5 wrk（Linux，极限吞吐）

```bash
wrk -t24 -c20000 -d120s -s load-test/wrk/seckill.lua http://localhost:21000/OrderServer/seckill/order
```

### 3.6 Gatling（JVM 高密度）

```bash
cd load-test/gatling
mvn gatling:test -DbaseUrl=http://localhost:21000 -DtargetRps=50000 -DdurationSec=300
```

## 4. Docker 分布式压测

```bash
cd load-test/docker
BASE_URL=http://host.docker.internal:21000 TARGET_RPS=1000000 WORKERS=100 \
  docker compose -f docker-compose.loadgen.yml up --scale k6-worker=100
```

- InfluxDB: `http://localhost:8086`
- Grafana: `http://localhost:3000`（添加 InfluxDB 数据源 `k6`）

## 5. Kubernetes（k6-operator）

```bash
kubectl apply -f load-test/k8s/k6-seckill-testrun.yaml
```

默认 `parallelism: 100`，每 Pod `TARGET_RPS=10000` → 聚合 **1M RPS**。按集群能力调整 `parallelism` 与 `resources`。

## 6. Prometheus + Grafana 监控

压测时建议同时启动监控栈，采集 Gateway / Order 的 JVM 与 HTTP 指标。

### 6.1 启动监控

```powershell
.\load-test\scripts\start-monitoring.ps1 -Detach
```

```bash
./load-test/scripts/start-monitoring.sh
```

| 组件 | 地址 | 说明 |
|------|------|------|
| Prometheus | http://localhost:9090 | 抓取 `/actuator/prometheus` |
| Grafana | http://localhost:3000 | 默认 `admin` / `admin` |
| Gateway 指标 | http://localhost:21000/actuator/prometheus | loadtest profile |
| User-center 指标 | http://localhost:8888/actuator/prometheus | loadtest profile |
| Order 指标 | http://localhost:8087/actuator/prometheus | loadtest profile |

Grafana 预置 Dashboard：**Moli Load Test / Seckill**（RPS、P95、Gateway 路由、JVM CPU/Heap、**Druid 连接池**）。

### 6.2 验证抓取

```bash
curl http://localhost:21000/actuator/prometheus | head
curl http://localhost:8888/actuator/prometheus | grep druid_pool
curl http://localhost:8087/actuator/prometheus | head
```

Prometheus Targets 页面应显示 `moli-gateway`、`moli-user-center`、`moli-order` 为 **UP**。

Prometheus **Alerts** 页可查看连接池告警规则（见 `load-test/docker/prometheus/alerts/moli-druid.yml`）。

### 6.3 生产 / 预发布 Druid 与告警

`application-pro.yml` / `application-pre.yml` 已配置：

| 项 | 说明 |
|----|------|
| `max-active` | 默认 80（pro）/ 50（pre），可用 `DRUID_MAX_ACTIVE` 环境变量覆盖 |
| `remove-abandoned` | 泄漏连接 120s 自动回收，**不必只能重启** |
| `slow-sql-millis` | 2000ms 慢 SQL 日志 |
| `/actuator/prometheus` | 暴露 `druid.pool.active|max|waiting|peak|idle` |

**容量公式：** `max-active × 实例数 < MySQL max_connections × 0.7`

**Prometheus 告警（自动加载）：**

| 告警 | 条件 |
|------|------|
| `DruidPoolWaiting` | `druid_pool_waiting > 0` 持续 30s |
| `DruidPoolHighUsage` | 使用率 > 85% 持续 1m |
| `DruidPoolCritical` | 使用率 > 95% 持续 30s |
| `DruidPoolCreateErrors` | 5m 内新建连接失败 |
| `UserCenterLogin5xxRate` | 登录接口 5xx > 5% |

### 6.4 Gateway loadtest 调优项

`moli-gateway/application-loadtest.yml` 已配置：

- Netty 连接超时、codec 内存上限
- 下游 `httpclient` 弹性连接池（`max-connections: 10000`）
- Actuator 暴露 `prometheus`、`metrics`、`gateway`
- _histogram 分位数（P95/P99 可在 Grafana 查询）

### 6.5 观测指标

| 来源 | 指标 |
|------|------|
| k6 输出 | `http_reqs`（RPS）、`http_req_duration`（P95/P99）、`http_req_failed` |
| Prometheus | `http_server_requests_seconds_*`、`druid_pool_*` |
| GET `/seckill/metrics` | `success`、`sold_out`、`duplicate`、`persisted` |
| Grafana Dashboard | RPS、P95、Druid 连接池、池使用率 |

## 7. 调优清单

1. **order-server**：`application-loadtest.yml` 已放大 Tomcat/Redis 连接池
2. **gateway**：多实例 + `StripPrefix` 路由已配置
3. **Redis**：Cluster、Pipeline、避免大 key；秒杀 key 按活动分片
4. **MySQL**：仅异步消费队列，热路径不直连 DB
5. **Shiro**：秒杀路径 `/seckill/**` 在 loadtest 下免鉴权（生产应改为边缘令牌）
6. **Sentinel**：可在 `SeckillController` 加 `@SentinelResource` 验证限流

## 8. 目录结构

```text
load-test/
├── README.md
├── config/env.example
├── k6/
│   ├── user-center-login-*.js   # 登录压测
│   ├── mixed-login-seckill.js   # 全链路混合
│   └── seckill-*.js             # 秒杀压测
├── wrk/                   # wrk Lua
├── gatling/               # Gatling Maven 工程
├── docker/
│   ├── docker-compose.loadgen.yml   # k6 分布式压测 + InfluxDB
│   └── docker-compose.monitoring.yml # Prometheus + Grafana
│   ├── prometheus/prometheus.yml
│   └── grafana/                      # 数据源 + Dashboard 预置
├── k8s/                   # k6-operator TestRun
└── scripts/               # init / smoke / distributed 启动脚本
```

## 9. 安装 k6

- Windows: `choco install k6` 或 [官方安装包](https://k6.io/docs/get-started/installation/)
- macOS: `brew install k6`
- Linux: 见官方文档

---

**本地验证路径：** `init-seckill-data` → `run-smoke` → `seckill-ramp` → 多机 `run-distributed-k6` → K8s 百万压测。
