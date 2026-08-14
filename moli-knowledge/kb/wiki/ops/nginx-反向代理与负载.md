---
title: Nginx 反向代理与负载
slug: nginx-反向代理与负载
type: guide
status: active
tags: [Nginx, LVS, Keepalived, 负载均衡, 运维]
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
related: [nginx-限流与缓冲调优, linux-运维基础, 跨域与前后端分离]
created: 2026-07-05
updated: 2026-07-05
---

# Nginx 反向代理与负载

## 1. 反向代理

```nginx
upstream backend {
    server 127.0.0.1:8081;
    server 127.0.0.1:8082;
}
server {
    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 2. 负载策略

`round_robin`（默认）、`ip_hash`、`least_conn`；健康检查配合 `max_fails`。

## 3. LVS + Keepalived（raw 摘要）

四层 DR/TUN/NAT 模式；VIP 漂移；Nginx 七层在其上。限流调参见 [[middleware/nginx-限流与缓冲调优]]。

## 4. 与网关

Spring Cloud Gateway 见 [[spring/spring-cloud-gateway]]；静态资源与 SPA 反代见 [[frontend/前端技术栈]]。

## 批次#1323 增补（wujinsen Phase2 P2 数仓调度）

本页 Phase 2 #1323 创建；sources **7** 篇。

<!-- t22-wujinsen-images:raw/wujinsen_markdown/大数据资料-王/nginx+ka+lvs/Keepalived原理与实战精讲.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/大数据资料-王/nginx+ka+lvs/Keepalived原理与实战精讲.note.md` · T22 **B** 档

### 来自：Keepalived原理与实战精讲

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/nginx%2Bka%2Blvs/Keepalived%E5%8E%9F%E7%90%86%E4%B8%8E%E5%AE%9E%E6%88%98%E7%B2%BE%E8%AE%B2.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/大数据资料-王/nginx+ka+lvs/nginx upstream的分配方式。.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/大数据资料-王/nginx+ka+lvs/nginx upstream的分配方式。.note.md` · T22 **B** 档

### 来自：nginx upstream的分配方式。

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/nginx%2Bka%2Blvs/nginx%20upstream%E7%9A%84%E5%88%86%E9%85%8D%E6%96%B9%E5%BC%8F%E3%80%82.note_images/imageFile1.png)
