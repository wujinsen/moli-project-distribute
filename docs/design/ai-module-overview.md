# AI 模块 · 概要设计

> 模块：**`moli-ai`**（父）/ **`moli-ai-server`**（artifactId + 目录）· Nacos 服务名 **`ai-server`** · HTTP **1128**  
> v1 范围：[moli-v1-release-scope.md](../product/moli-v1-release-scope.md) §3.5  
> API：[ai-api.md](../api/ai-api.md) · 冒烟：[ai-smoke.md](../test/ai-smoke.md)

---

## 1. 定位（v1）

AI 在 v1 是 **可启动的占位微服务**，用于验证：

| 验证项 | 说明 |
|--------|------|
| 网关路由 | `/AiServer/**` → `ai-server` |
| Nacos 注册 | 与其它服务同发现模型 |
| Shiro 骨架 | 可接入 `moli-user-center-shiro-starter` |
| Dubbo Consumer | 端口 **20883**，可拉 user-center |

**v1 不做**：报表引擎、OLAP、数据仓库、定时 ETL、大屏。

![部署拓扑](../diagrams/png/moli-deploy-topology.png)

> [moli-deploy-topology.drawio](../diagrams/moli-deploy-topology.drawio)

---

## 2. 模块结构

```
moli-ai/
  moli-ai-server/     # Spring Boot 主应用，application.name=ai-server
  pom.xml
```

依赖：`moli-distribute-common`、Nacos、MySQL/Redis（配置对齐其它服务）、可选 Shiro Starter。

---

## 3. 运行时

| 项 | 值 |
|----|----|
| HTTP | **1128** |
| Dubbo | **20883** |
| 网关 | `/AiServer/**`（StripPrefix=1） |
| v1 接口 | `GET /demo/test` → `test success` |

---

## 4. 与其它服务关系

```
meiling-ui / curl
      │
      ▼
moli-gateway :21000  /AiServer/**
      │
      ▼
ai-server :1128
      ├── Shiro（可选）→ Redis Session（user-center 签发）
      └── Dubbo → user-center-server :20881
```

鉴权策略与 order 一致：**Session 在 user-center 签发**，BI 只做校验。

---

## 5. 数据层（v1）

- 无独立 BI 业务表
- 使用公共数据源配置占位（与平台一致，便于 v2 扩展）

---

## 6. v2+ 演进路线

| 阶段 | 能力 | wiki 参考 |
|------|------|-----------|
| v2 | **ChatBI / NL2SQL Agent（AI-4）**：自然语言 → SQL → 图表 + 解读，接 order/user 只读库 | [`ai-capability-roadmap.md`](ai-capability-roadmap.md) §4 第 2 波 |
| v2 | 报表 API、数据集元数据 | [[bi报表服务演进路线]] |
| v2+ | 对接 MinIO/ES、权限域隔离 | concepts/ |
| v3 | 大屏、订阅推送 | 待定 |

---

## 7. 文档链

| 类型 | 路径 |
|------|------|
| 模块 README | [moli-ai/README.md](../../moli-ai/README.md) |
| API | [ai-api.md](../api/ai-api.md) |
| 测试 | [ai-smoke.md](../test/ai-smoke.md) |
| wiki 服务 | [kb/wiki-moli/develop/bi服务.md](../../moli-knowledge/kb/wiki-moli/develop/bi服务.md) |
| wiki 产品 | [kb/wiki-moli/product/BI服务产品说明.md](../../moli-knowledge/kb/wiki-moli/product/BI服务产品说明.md) |
