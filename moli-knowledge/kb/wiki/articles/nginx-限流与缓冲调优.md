---
title: Nginx 限流与缓冲调优
slug: nginx-限流与缓冲调优
type: article
status: active
tags: [Nginx, 限流, 运维]
sources:
 - raw/wujinsen_markdown/
related: [nginx反向代理与前端部署指南, 限流算法与令牌桶, sse-服务端推送, gateway-超时与重试配置]
created: 2026-06-21
updated: 2026-06-21
---

# Nginx 限流与缓冲调优

> 反向代理 [[nginx反向代理与前端部署指南]]；算法 [[限流算法与令牌桶]]；SSE [[sse-服务端推送]]。

## 1. 限流 zone

```nginx
limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;

location /api/ {
 limit_req zone=api burst=20 nodelay;
 proxy_pass http://gateway;
}
```

- **leaky bucket** 语义；`burst` 允许突发
- 秒杀入口可更严 + Gateway 二层 [[sentinel-限流与熔断]]

## 2. 连接限制

```nginx
limit_conn_zone $binary_remote_addr zone=addr:10m;
limit_conn addr 50;
```

防单 IP 占满 upstream 连接。

## 3. 缓冲与 SSE/WebSocket

| 场景 | 配置 |
|------|------|
| SSE 流式 | `proxy_buffering off;` `chunked_transfer_encoding on;` |
| 大文件上传 | `client_max_body_size`；调超时 |
| 普通 API | 默认 buffer 即可 |

## 5. 排查

- 502/504：upstream 超时 vs Gateway [[gateway-超时与重试配置]]
- 499：客户端断开；看是否压测过早结束

## 相关

[[linux-ulimit与文件句柄]] · [[容量规划与水平扩展]]
