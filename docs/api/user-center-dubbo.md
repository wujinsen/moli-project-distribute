# 用户中心 · Dubbo API

> 契约模块：`moli-user-center-api`  
> 实现：`moli-user-center-server` → `UserServerProvider`  
> HTTP 接口见 [`user-center-api-map.md`](user-center-api-map.md)

## 1. 服务坐标

| 项 | 值 |
|----|----|
| 接口 | `com.moli.user.center.api.UserCenterServer` |
| 提供方 | `user-center-server` |
| 协议 | dubbo |
| 端口 | **20881** |
| version | `1.0.0` |
| group | `moli` |

消费方配置示例（业务服务 `bootstrap.yml`）：

```yaml
dubbo:
  cloud:
    subscribed-services: user-center-server
  consumer:
    check: false
```

Starter 依赖：

```xml
<dependency>
  <groupId>com.moli</groupId>
  <artifactId>moli-user-center-shiro-starter</artifactId>
</dependency>
```

## 2. 方法说明

### 2.1 getInfoByUserName

```java
MoliResult<SysUser> getInfoByUserName(String userName);
```

| 项 | 说明 |
|----|------|
| 用途 | user-center **内部登录**时 ShiroRealm 查用户；业务服务**禁止**用于 `subject.login` |
| 入参 | 登录名 |
| 出参 | `SysUser`（不含明文密码） |
| 失败 | `MoliResult` 业务码 |

### 2.2 getUserById

```java
MoliResult<SysUser> getUserById(Long userId);
```

| 项 | 说明 |
|----|------|
| 用途 | 业务服务每次请求校验用户是否仍存在、未锁定、未删除 |
| 入参 | 用户 ID（来自 Session） |
| 出参 | `SysUser` |

### 2.3 getPermissionsByUserId

```java
MoliResult<Set<String>> getPermissionsByUserId(Long userId, String userName);
```

| 项 | 说明 |
|----|------|
| 用途 | Starter `ShiroRealm` 授权；合并菜单 perms + 动作码 |
| 入参 | 用户 ID + 用户名（超管判定） |
| 出参 | 权限字符串集合；超管含 `*:*:*` |
| 上下文 | 依赖 Session 中 `currentSystemId`（多系统 enter 后） |

## 3. 调用时序（业务服务）

```
HTTP 请求 + Authorization: sessionId
  → AuthenticationFilter
  → Redis 还原 Session
  → getUserById(userId)
  → getPermissionsByUserId(userId, userName)
  → @RequiresPermissions 校验
```

## 4. 与 HTTP 的边界

| 能力 | 对外暴露 |
|------|----------|
| 登录/登出 | **仅 HTTP**（user-center-server） |
| 会话校验/拉权限 | **Dubbo**（内网） |
| 用户 CRUD | **仅 HTTP**（管理端） |

已移除 OpenFeign `UserCenterClient`，避免内网能力经 REST 暴露。

## 5. 错误处理

- Dubbo 超时/无 Provider：业务服务启动需 `dubbo.consumer.check: false`；运行期记录日志并视为鉴权失败
- 返回 `MoliResult` 非成功：Starter 按未授权处理

## 6. 源码

- 接口：`moli-user-center-api/src/main/java/com/moli/user/center/api/UserCenterServer.java`
- 实现：`moli-user-center-server/.../provider/UserServerProvider.java`
