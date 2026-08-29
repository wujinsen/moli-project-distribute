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
| gRPC（客户端自动） | 29548 |
| 账号 | `nacos` / `nacos` |
| 命名空间 | `dev`（脚本 start 自动创建） |

Docker 映射：`28548:8848`、`29548:9848`（容器内仍是官方 8848/9848）。

---

## Java 服务

`bootstrap.yml` 已默认：

```yaml
discovery:
  server-addr: ${NACOS_SERVER_ADDR:http://127.0.0.1:28548}
```

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

### Java 仍连 8848 / 9848 超时

1. 确认用的是 **新 bootstrap 默认 28548**（Reload Maven / Rebuild）
2. 旧 Run Configuration 若写了 `NACOS_SERVER_ADDR=http://127.0.0.1:18848` 或 `8848`，请删掉或改为 `28548`
3. 重启 Nacos：`.\scripts\nacos-docker.ps1 restart`

---

## 相关

- [local-dev-ports.md](local-dev-ports.md) · [gateway-routes.md](../api/gateway-routes.md)
- 配置样例：[docs/nacos/README.md](../nacos/README.md)
