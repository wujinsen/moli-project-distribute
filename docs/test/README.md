# 测试文档

## v1 上线

| 文档 | 用途 |
|------|------|
| **[release-smoke-checklist.md](release-smoke-checklist.md)** | **上线冒烟（P0 必跑）** |
| [gateway-smoke.md](gateway-smoke.md) | 网关路由专项冒烟 |
| [bi-smoke.md](bi-smoke.md) | BI 骨架冒烟（v1 占位） |
| [order-seckill.md](order-seckill.md) | 秒杀手测 |
| [user-center.md](user-center.md) | 用户中心 ApiTest |

## 知识库

| 文档 | 用途 |
|------|------|
| [knowledge-ingest-acceptance.md](knowledge-ingest-acceptance.md) | Ingest 分场景验收 |
| **[knowledge-e2e-regression.md](knowledge-e2e-regression.md)** | **知识库深度回归（CI + 手测）** |
| [knowledge-wiki-lint-space.md](knowledge-wiki-lint-space.md) | lint-space 单测 |
| [knowledge-script-vs-llm-matrix.md](knowledge-script-vs-llm-matrix.md) | 脚本 vs LLM |
| **[knowledge-t22-image-remediation.md](knowledge-t22-image-remediation.md)** | **T22 wujinsen 插图回迁验收 + 自动化测试** |

## 压测

| 文档 | 用途 |
|------|------|
| [load-test/README.md](../../load-test/README.md) | k6 秒杀 / 登录压测 |
| [../diagrams/moli-seckill-flow.drawio](../diagrams/moli-seckill-flow.drawio) | 秒杀链路图 |

## 工作流

1. 发版前：`release-smoke-checklist` 全 P0
2. 模块回归：`mvn test`（user-center、knowledge-server）
3. 压测：load-test（可选 P1）

## 不要

- k6 脚本放进 `docs/test/`（保持在 `load-test/`）
