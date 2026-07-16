# moli-ai · AI 服务（骨架）

> **v1 定位**：[moli-v1-release-scope.md](../docs/product/moli-v1-release-scope.md) §3.5 — **占位服务，不纳入业务验收**  
> 验证网关路由 + Shiro 骨架 + Nacos 注册。

## 模块构成

| 子模块 | 职责 |
|--------|------|
| `moli-ai-server` | 父模块 `moli-ai` · artifactId `moli-ai-server` · 应用名 `ai-server` |

## 运行时

| 项 | 值 |
|----|----|
| Nacos 服务名 | `ai-server` |
| HTTP 端口 | **1128** |
| Dubbo 端口 | **20883** |
| 网关路由 | `/AiServer/**` → `lb://ai-server`，StripPrefix=1 |

## 功能（v1）

| 接口 | 说明 |
|------|------|
| `GET /demo/test` | 返回 `test success` |

完整契约：[docs/api/ai-api.md](../docs/api/ai-api.md)

## 依赖

- Nacos、MySQL、Redis（配置同其它业务服务）
- user-center（Shiro Starter，若启用鉴权）

## 本地启动

```bash
cd moli-distribute-parent && mvn clean install -DskipTests
cd ../moli-distribute-common && mvn clean install -DskipTests
cd ../moli-user-center && mvn clean install -DskipTests
cd ../moli-ai/moli-ai-server
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

## 文档索引

| 类型 | 路径 |
|------|------|
| API | [docs/api/ai-api.md](../docs/api/ai-api.md) |
| 设计 | [docs/design/ai-module-overview.md](../docs/design/ai-module-overview.md) |
| 冒烟 | [docs/test/ai-smoke.md](../docs/test/ai-smoke.md) · [release-smoke-checklist.md](../docs/test/release-smoke-checklist.md) G4 |

## 后续（v2+）

- 报表 / 大屏 / 数据接口
- 独立 BI 表结构与权限域
