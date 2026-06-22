# moli-user-center-shiro-starter

用户中心 **Shiro Spring Boot Starter**：配合 user-center **单点登录**，在 order/bi 等节点只校验已签发的 Session，**不提供登录接口**。

自动配置类：`com.moli.user.center.starter.autoconfigure.UserCenterShiroAutoConfiguration`（`META-INF/spring.factories`）。

## 模块说明

| 模块 | 职责 |
|------|------|
| `moli-user-center-api` | Dubbo 契约 `UserCenterServer` |
| `moli-user-center-shiro-starter` | SSO 会话校验 Starter（本模块） |

## 快速接入

### 1. Maven

```xml
<dependency>
    <groupId>com.moli</groupId>
    <artifactId>moli-user-center-shiro-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 启动类

普通 `@SpringBootApplication` 即可，**无需** `@ComponentScan`。

### 3. 配置

```yaml
# bootstrap.yml
dubbo:
  cloud:
    subscribed-services: user-center-server
  consumer:
    check: false
```

```yaml
# application.yml — Redis 必须与 user-center-server 一致（含 database）
spring:
  redis:
    host: localhost
    port: 6379
    password: 123456
    database: 1

moli:
  user-center:
    shiro:
      enabled: true
      session-expire-seconds: 86400
```

### 4. 前端

- 登录 / 登出：仅 `http://<gateway>/UserCenter/login`、`/UserCenter/logout`
- 业务 API：`http://<gateway>/OrderServer/...`，Header `Authorization: <token>`

## 关闭 Starter

```yaml
moli:
  user-center:
    shiro:
      enabled: false
```

详见 `docs/zh-CN/ARCHITECTURE.md`。
