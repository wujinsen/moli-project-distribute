# 本地 Nacos · Docker（端口 28548 / 29548）

> 全仓默认 Nacos 地址：**http://127.0.0.1:28548**（见各模块 `bootstrap.yml`）。  
> 完整端口表：[local-dev-ports.md](local-dev-ports.md)

---

## 快速启动

```powershell
cd D:\work\moli_project\moli-project-distribute
.\scripts\nacos-docker.ps1          # 启动
.\scripts\nacos-docker.ps1 stop       # 停止
.\scripts\nacos-docker.ps1 restart    # 重启
.\scripts\nacos-docker.ps1 status     # 状态
.\scripts\nacos-docker.ps1 logs       # 日志
```

| 项 | 值 |
|---|---|
| HTTP 控制台 | http://127.0.0.1:28548/nacos |
| 客户端 gRPC | **29548**（= 28548 + 1000） |
| 服务端 gRPC | **29549**（= 28548 + 1001，Docker 须映射） |
| Docker 镜像 | `nacos/nacos-server:v2.5.2` |
| Java `nacos-client` | **2.5.2**（父 POM 显式锁定，覆盖 SCA 2.2.7 默认的 2.0.3） |
| Spring Cloud Alibaba | `2.2.7.RELEASE`（Spring Boot 2.3.12） |
| 账号 | `nacos` / `nacos` |
| 命名空间 | `dev`（脚本 start 自动创建） |

Docker 映射：`28548:8848`、`29548:9848`、`29549:9849`（容器内仍是官方 8848/9848/9849）。

---

## Java 服务

`bootstrap.yml` 已默认：

```yaml
discovery:
  server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:28548}
```

`server-addr` **不要**加 `http://` 前缀，否则 gRPC 可能仍连默认 `9848`。

**一般无需再设环境变量。** 若需覆盖（连远程 Nacos），在 IDEA Run Configuration 加 `NACOS_SERVER_ADDR=...` 即可。

IDEA 预置启动项见 **`.run/*-dev.run.xml`**。

---

## 手动 compose

```powershell
cd deploy/docker
docker compose -f docker-compose.nacos.yml up -d
```

---

## 常见问题

### 控制台打不开

`docker ps` 中 **Port(s)** 应含 `28548->8848`。用本仓库脚本或上述 compose，不要无 `-p` 裸跑容器。

### Java 报 `Client not connected, current status: STARTING`

Nacos 2.x 除 HTTP **28548** 外，还必须能连 **gRPC 29548 / 29549**（相对主端口 +1000 / +1001）。

1. `docker ps` 的 Port(s) 应含 `28548->8848`、`29548->9848`、`29549->9849`
2. 重启 Nacos：`.\scripts\nacos-docker.ps1 restart`
3. `bootstrap.yml` 中 `server-addr` 为 `127.0.0.1:28548`（无 `http://`）
4. IDEA **Rebuild** 后重启 Java 服务

### Java 仍连 8848 / 9848 / **18848** 超时

1. 确认用的是 **新 bootstrap 默认 28548**（Reload Maven / Rebuild）
2. **Windows 用户环境变量**（常见根因）：PowerShell 执行  
   `[Environment]::GetEnvironmentVariable('NACOS_SERVER_ADDR','User')`  
   若仍是 `http://127.0.0.1:18848` 或 `8848`，改为 `127.0.0.1:28548` 或删除该变量
3. IDEA Run Configuration → Environment variables：删掉错误的 `NACOS_SERVER_ADDR`；或直接用仓库 `.run/*-dev.run.xml`（已写死 `127.0.0.1:28548`）
4. **完全重启 IDEA**（仅 Stop/Run 不会刷新已加载的系统环境变量）
5. 重启 Nacos：`.\scripts\nacos-docker.ps1 restart`

---

## 相关

- [local-dev-ports.md](local-dev-ports.md) · [gateway-routes.md](../api/gateway-routes.md)
- 配置样例：[docs/nacos/README.md](../nacos/README.md)
