# 用户中心 · 测试说明

> 模块：`moli-user-center-server` · 压测：[`load-test/README.md`](../../load-test/README.md)

## 1. 单元 / 接口回归（ApiTest）

测试目录：`moli-user-center-server/src/test/java/`

| 测试类 | 覆盖 |
|--------|------|
| `LoginControllerApiTest` | 登录、登出 |
| `UserControllerApiTest` | 用户 CRUD、角色/系统分配 |
| `RoleControllerApiTest` | 角色与授权 |
| `MenuControllerApiTest` | 菜单、getRouters |
| `DeptControllerApiTest` | 部门树 |
| `PostControllerApiTest` | 岗位 |
| `DictControllerApiTest` | 字典 |
| `ActionControllerApiTest` | 动作码 |
| `SystemControllerApiTest` | 门户 enter/switch |
| `SsoControllerApiTest` | SSO Ticket |
| `LogControllerApiTest` | 日志查询 |
| `OperationControllersApiTest` | 运维资产 CRUD |
| `PermissionServiceImplTest` | 权限合并逻辑 |
| `SysSystemServiceImplTest` | 门户上下文 |
| `LoadtestLoginControllerTest` | loadtest Profile |
| `PrivilegedUserUtilsTest` | 特殊账号规则 |

基类 `AbstractApiTest` 初始化 MyBatis-Plus 元数据；Controller 测试多用 **Mockito** 注入 Service/Mapper，不依赖完整 Spring 容器。

### 运行

```bash
cd moli-user-center
mvn test
# 或仅 server 模块
cd moli-user-center-server && mvn test
# 单类
mvn -Dtest=LoginControllerApiTest test
```

**环境**：纯 Mock 测试无需 MySQL/Redis；集成联调见下方。

## 2. 本地联调清单

| 步骤 | 验证 |
|------|------|
| 启动 Nacos + MySQL + Redis | 服务注册成功 |
| 启动 user-center-server | `8888` 健康 |
| `POST /UserCenter/login` | 返回 token |
| 带 `Authorization` 调 `/UserCenter/user/profile` | 200 |
| 启动 order-server + 同一 Redis | 同一 token 访问 `/OrderServer/**` |

操作细节：[`kb/wiki-moli/guides/登录与鉴权指南.md`](../../moli-knowledge/kb/wiki-moli/guides/登录与鉴权指南.md)。

## 3. 压测（loadtest Profile）

| 项 | 说明 |
|----|------|
| 配置 | `application-loadtest.yml`，激活 `-Dspring.profiles.active=loadtest,dev` |
| 接口 | `POST /loadtest/login` 批量签发 Session |
| 脚本 | `load-test/k6/user-center-login-*.js` |
| 文档 | [`load-test/README.md`](../../load-test/README.md)、wiki `loadtest-profile与压测登录` |

## 4. Swagger 手工测试

经网关：`http://localhost:21000/UserCenter/swagger-ui.html`  
直连：`http://localhost:8888/swagger-ui.html`

见 [`kb/wiki-moli/guides/swagger接口调试指南.md`](../../moli-knowledge/kb/wiki-moli/guides/swagger接口调试指南.md)。

## 5. 回归策略（建议）

1. 改 Controller/Permission 逻辑 → 跑对应 `*ApiTest`
2. 发版前 → `mvn test` 全绿
3. 改登录/Session → 加跑 load-test smoke + order 联调
4. API 地图 §6 迭代项与测试类保持同名索引（见 [`user-center-api-map.md`](../api/user-center-api-map.md)）

## 6. 相关

- [`user-center-api-map.md`](../api/user-center-api-map.md)
- [`docs/test/README.md`](README.md)
