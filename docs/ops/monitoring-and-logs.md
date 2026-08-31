# 监控与日志 · v1 运维要点

> v1 **最低可运维**指南；完整建设方案见 [可观测性平台规划](../design/observability-platform-plan.md)（Prometheus + Grafana + Loki/Alloy + SkyWalking）。
> 发布：[production-checklist.md](production-checklist.md) · 排障：[wiki-moli/故障排查指南](../../moli-knowledge/kb/wiki-moli/ops/故障排查指南.md)

---

## 1. 日志位置

各服务 `logback-spring.xml` 默认 **控制台（dev）+ 文件**；生产 **仅文件**（`deploy/*/application-pro.yml` + `moli-service.sh`）。

MyBatis SQL：**dev / pro 均使用 `Slf4jImpl`**，经 logback 写入 `{service}.log`，Alloy/Loki 可采集（LogQL：`{service="..."} |= "Preparing"` 或 `| level="DEBUG"`）。勿使用 `StdOutImpl`（仅 IDEA 控制台）或 `NoLoggingImpl`。

| 服务 | 建议关注 |
|------|----------|
| user-center | 登录失败、Shiro、Dubbo |
| gateway | 503、路由超时 |
| order | 秒杀 Consumer、Redis |
| knowledge | Ingest commit、Sync、LLM |
| bi | 仅启动日志 |

**v1 最低要求**：发版后 15 分钟内各服务 **无 ERROR 持续刷屏**。

---

## 2. 健康检查

### 2.1 业务探测（推荐）

| 探测 | 命令/URL |
|------|----------|
| 网关 | `curl /OrderServer/seckill/ping` |
| 登录 | `POST /UserCenter/login` |
| 知识库 | `GET /KnowledgeServer/kb/index?spaceId=...` |

见 [release-smoke-checklist.md](../test/release-smoke-checklist.md)。

### 2.2 Actuator

五个 Java 服务统一暴露：

- `/actuator/health`
- `/actuator/info`
- `/actuator/prometheus`

`health` 不返回内部细节。生产必须用安全组/防火墙限制为管理网访问，**不得经公网或 Gateway 暴露**。

---

## 3. 指标

| 服务 | 指标 |
|------|------|
| order | `GET /seckill/metrics` Redis 计数 |
| 五个 Java 服务 | `/actuator/prometheus` JVM、HTTP、线程、连接池等 |
| user-center | Druid 连接池自定义指标 |

压测栈见 [load-test/README.md](../../load-test/README.md)；统一本地观测栈见 [`deploy/observability/README.md`](../../deploy/observability/README.md)。

---

## 4. 知识库专项

| 场景 | 查看 |
|------|------|
| Sync 失败 | `kb_sync_log` 表、应用日志 |
| Lint CI 红 | `python kb/tools/lint.py --strict` |
| Ask 无 LLM | 平台 LLM 设置 / `kb.llm.usable()` |
| Web 页旧 | 是否执行 `sync-all` |

---

## 5. 基础设施监控（建议）

| 组件 | 指标 |
|------|------|
| MySQL | 连接数、慢查询 |
| Redis | 内存、命中率、db=1 连接 |
| Nacos | 实例心跳、服务列表 |
| 磁盘 | wiki 目录、日志分区 |

应用侧指标、Loki/Alloy 和 SkyWalking 接入骨架已经落地。当前 Compose 是单机 PoC；生产存储、TLS、认证和高可用仍按 [可观测性平台规划](../design/observability-platform-plan.md) 分阶段实施。

---

## 6. 告警（v1 可选）

- 网关 5xx 率 > 阈值
- MySQL/Redis 不可达
- knowledge-server 连续 Sync 失败

---

## 7. 相关

- [`deploy/observability/README.md`](../../deploy/observability/README.md)
- [rollback-guide.md](rollback-guide.md)
- [v1-release-runbook.md](v1-release-runbook.md)
