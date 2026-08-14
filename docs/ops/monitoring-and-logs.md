# 监控与日志 · v1 运维要点

> v1 **最低可运维**指南；完整 APM 见 [TECH_STACK.md](../zh-CN/TECH_STACK.md)（ELK / SkyWalking / Prometheus 为规划项）。  
> 发布：[production-checklist.md](production-checklist.md) · 排障：[wiki-moli/故障排查指南](../../moli-knowledge/kb/wiki-moli/ops/故障排查指南.md)

---

## 1. 日志位置

各服务 `logback-spring.xml` 默认 **控制台 + 文件**（具体路径见各模块 `src/main/resources/`）。

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

### 2.2 Actuator（若启用）

部分 profile 暴露 `/actuator/health`；**生产默认不对外网开放**。

---

## 3. 压测指标（loadtest）

| 服务 | 指标 |
|------|------|
| order | `GET /seckill/metrics` Redis 计数 |
| gateway | Prometheus（loadtest profile 可选） |

见 [load-test/README.md](../../load-test/README.md)。

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

v2 可接 Prometheus + Grafana 或云厂商监控。

---

## 6. 告警（v1 可选）

- 网关 5xx 率 > 阈值
- MySQL/Redis 不可达
- knowledge-server 连续 Sync 失败

---

## 7. 相关

- [rollback-guide.md](rollback-guide.md)
- [v1-release-runbook.md](v1-release-runbook.md)
