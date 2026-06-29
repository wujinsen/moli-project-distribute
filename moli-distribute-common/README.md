# moli-distribute-common · 公共模块

> 各业务服务依赖的**共享库**（非独立部署服务）。改公共类时需 `mvn install` 后重启依赖方。

## 职责

| 包 | 内容 |
|----|------|
| `com.moli.common.core` | `MoliResult`、`BaseEntity`、`BasePage`、雪花 ID |
| `com.moli.common.constant` | Redis/Shiro/权限/系统分组等常量 |
| `com.moli.common.enums` | 业务类型、HTTP 方法、操作者类型、响应码 |
| `com.moli.common.exception` | `BaseException`、`GlobalExceptionHandler` |
| `com.moli.common.utils` | Servlet、IP、I18n、字符串、日期等工具 |
| `com.moli.common.minio` | MinIO 配置属性 |
| `com.moli.common.log` | `@MoliLog` 操作日志注解 |
| `com.moli.common.autoconfigure` | Jackson / 公共 Bean 自动配置 |

## 依赖关系

```
moli-distribute-parent (BOM)
        │
        ▼
moli-distribute-common  ◀── moli-gateway
        ▲                 ◀── moli-user-center-*
        │                 ◀── moli-order-server
        │                 ◀── moli-ai-server
        └─────────────────◀── moli-knowledge-server
```

## 使用约定

- **统一响应**：对外 HTTP 优先 `MoliResult<T>`（与网关/前端约定一致）。
- **异常**：业务抛 `BaseException`，由 `GlobalExceptionHandler` 转 JSON。
- **常量**：跨服务 Redis key、权限前缀放 `constant/`，避免各模块硬编码分叉。
- **版本**：随 monorepo 父 POM 发布，**不**单独 semver。

## 构建

```bash
cd moli-distribute-parent && mvn clean install -DskipTests
cd ../moli-distribute-common && mvn clean install -DskipTests
```

## 相关文档

| 类型 | 路径 |
|------|------|
| 文档总览 | [docs/README.md](../docs/README.md) |
| 技术栈 | [docs/zh-CN/TECH_STACK.md](../docs/zh-CN/TECH_STACK.md) |
| 全局异常 / 响应约定 | 本模块 `GlobalExceptionHandler`、`MoliResult` 源码 |
