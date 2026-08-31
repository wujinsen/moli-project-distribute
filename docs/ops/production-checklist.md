# v1.0 生产 / 预发环境检查表

> 配合 [v1-release-runbook.md](v1-release-runbook.md) 使用 · 更新：2026-06-28

---

## 1. 基础设施

| 项 | 检查 | ✓ |
|----|------|---|
| MySQL 8.x，`moli` 库 utf8mb4 | 连接数、慢查询日志 | |
| Redis 6+ | 内存上限、持久化策略 | |
| Nacos 2.x | 命名空间隔离 dev/stage/prod | |
| 时钟同步 | NTP，影响雪花 ID / Session TTL | |

---

## 2. 网络与端口

> **运行时权威（SVR-21 后）**：运维台「端口矩阵」菜单维护的 DB 表 `operation_port_matrix`；本表为发布前人工核对清单，变更后请同步核对运维台配置。设计：[`operation-port-matrix-config.md`](../design/operation-port-matrix-config.md)。

| 服务 | 端口 | 对外暴露 |
|------|------|----------|
| gateway | 28100 | ✅ 唯一 HTTP 入口（推荐） |
| user-center | 28101 | ❌ 仅内网 |
| order | 28102 | ❌ 仅内网 |
| knowledge | 28104 | ❌ 仅内网 |
| bi | 28103 | ❌ 仅内网 |
| Nacos | 28548 | 内网 |
| MySQL | 3306 | 内网 |
| Redis | 6379 | 内网 |

---

## 3. 配置项（必查）

### 3.1 全服务通用

| 键 | 说明 |
|----|------|
| `spring.datasource.*` | 生产库地址、账号、**非默认密码** |
| `spring.redis.database` | **各服务一致**（与 dev 相同 db 号） |
| `spring.redis.password` | 生产 Redis 密码 |
| Nacos `namespace` | 非 `dev` |

### 3.2 user-center

| 键 | 说明 |
|----|------|
| `captcha.enabled` | 生产建议 `true` |
| Shiro Session TTL | 与 Redis 过期一致 |

### 3.3 knowledge

| 键 | 说明 |
|----|------|
| `kb.llm.api-key` / 平台设置 | LLM 功能；空则降级 |
| `kb.wiki.space-dirs` | 两空间目录映射 |
| `kb.ingest.enabled` | 是否开放 Ingest |
| MinIO endpoint | 附件存储 |

### 3.4 order

| 键 | 说明 |
|----|------|
| `moli.seckill.load-test-mode` | **生产应为 false** |
| `moli.seckill.redis-key-prefix` | 多环境隔离前缀 |

### 3.5 gateway

| 项 | 说明 |
|----|------|
| CORS | 仅允许前端域名 |
| HTTPS | 终端 TLS 在 Nginx/LB |

---

## 4. 安全

| 项 | 检查 | ✓ |
|----|------|---|
| 默认演示账号密码已改 | admin 等 | |
| DB 账号最小权限 | 非 root 跑应用 | |
| Swagger 生产暴露 | 建议关闭或 IP 白名单 | |
| `/seckill/admin/init` | loadtest 关闭 | |
| 知识库空间 ACL | 私有空间成员正确 | |
| LLM Key | 不在 Git；DB 加密或 Nacos 密文 | |

---

## 5. 数据

| 项 | 检查 | ✓ |
|----|------|---|
| `scripts/moli.sql` 或迁移脚本版本 | 与发布 tag 一致 | |
| 知识库增量 SQL 已全部执行 | 见 runbook §2.2 | |
| 发布前 DB 备份 | 可恢复 | |
| wiki Sync 计划 | 发布步骤含 sync-all | |

---

## 6. 可观测性

| 项 | 检查 | ✓ |
|----|------|---|
| 日志落盘 | 各服务 `logs/{service}.log` 存在且滚动正常 | |
| MyBatis | `log-impl: Slf4jImpl`（`deploy/*/application-pro.yml`） | |
| Alloy | 生产主机 tail 业务 `logs/*.log` → Loki | |
| Prometheus | 抓取 `/actuator/prometheus`（内网） | |
| Grafana | 大盘 + Loki 数据源可用 | |
| SkyWalking | Agent 可选；`SKYWALKING_ENABLED` 与 OAP 地址 | |
| Actuator 暴露 | **不得**经公网/Gateway 暴露 health/prometheus | |

PoC 与 LogQL 示例：[monitoring-and-logs.md](monitoring-and-logs.md) · **生产可靠性**：[observability-production.md](observability-production.md) · 规划：[observability-platform-plan.md](../design/observability-platform-plan.md)

---

## 7. 发布签核

| 检查人 | 日期 | 环境 |
|--------|------|------|
| | | staging / prod |

全部 P0 勾选后执行 [release-smoke-checklist.md](../test/release-smoke-checklist.md)。

---

## 8. 相关

- [v1-release-runbook.md](v1-release-runbook.md)
- [moli-v1-release-scope.md](../product/moli-v1-release-scope.md)
- Nacos 样例：[docs/nacos/](../nacos/)
