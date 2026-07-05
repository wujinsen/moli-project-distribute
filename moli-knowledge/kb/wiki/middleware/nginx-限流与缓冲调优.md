---
title: Nginx 限流与缓冲调优
slug: nginx-限流与缓冲调优
type: article
status: active
tags: [Nginx, 限流, 运维]
sources:
- raw/wujinsen_markdown/大数据资料-王/a安装文档/Nginx安装及配置简介.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/Nginx安装部署.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/nginx + tomcat.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/nginx_varnish_rsync安装文档.note.md
- raw/wujinsen_markdown/大数据资料-王/nginx+ka+lvs/Keepalived原理与实战精讲.note.md
- raw/wujinsen_markdown/大数据资料-王/nginx+ka+lvs/Nginx 教程.note.md
- raw/wujinsen_markdown/大数据资料-王/nginx+ka+lvs/Nginx 配置高并发.note.md
- raw/wujinsen_markdown/大数据资料-王/nginx+ka+lvs/ipvsadm --persistent 与 --set.note.md
- raw/wujinsen_markdown/大数据资料-王/nginx+ka+lvs/ipvsadm命令参考.note.md
- raw/wujinsen_markdown/大数据资料-王/nginx+ka+lvs/nginx upstream的分配方式。.note.md
- raw/wujinsen_markdown/大数据资料-王/nginx+ka+lvs/nginx.conf详解.note.md
related: [限流算法与令牌桶, sse-服务端推送, gateway-超时与重试配置]
created: 2026-06-21
updated: 2026-07-05
---

# Nginx 限流与缓冲调优

> 反向代理 `moli-knowledge/kb/wiki-moli/ops/nginx反向代理与前端部署指南.md`；算法 [[middleware/限流算法与令牌桶]]；SSE [[middleware/sse-服务端推送]]。

## 1. 限流 zone

```nginx
limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;

location /api/ {
 limit_req zone=api burst=20 nodelay;
 proxy_pass http://gateway;
}
```

- **leaky bucket** 语义；`burst` 允许突发
- 秒杀入口可更严 + Gateway 二层 [[middleware/sentinel-限流与熔断]]

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

- 502/504：upstream 超时 vs Gateway [[middleware/gateway-超时与重试配置]]
- 499：客户端断开；看是否压测过早结束

## 相关

[[ops/linux-ulimit与文件句柄]] · [[ops/容量规划与水平扩展]]
