---


title: Nginx 反向代理与前端部署指南
slug: nginx反向代理与前端部署指南
type: guide
status: active
tags: [Nginx, 部署, 前端, 反向代理]
sources:
  - raw/wujinsen_markdown/架构/DevOps/nginx/moli nginx配置文件.note.md
  - raw/wujinsen_markdown/架构/DevOps/nginx/使用nginx部署多个前端项目.note.md
related: [前端开发与联调指南, 茉莉-gateway-cors, 网关]
created: 2026-06-22
updated: 2026-06-22
---

# Nginx 反向代理与前端部署指南

生产环境常见拓扑：**Nginx 对外 80/443** → 静态 `dist/` + API 反代到 [[网关]] 或直连 user-center。

## 1. Vue SPA 静态托管

```nginx
server {
    listen 80;
    server_name moli.example.com;

    location / {
        root /opt/moli-vue/dist;
        try_files $uri $uri/ /index.html;
        index index.html;
    }
}
```

`try_files` 解决 history 路由刷新 404。

## 2. API 反向代理

经网关（推荐，与 [[本地启动指南]] 路径一致）：

```nginx
location /UserCenter/ {
    proxy_pass http://127.0.0.1:21000/UserCenter/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header Cookie $http_cookie;
}
```

同理配置 `/OrderServer/`、`/KnowledgeServer/` 等前缀。

> 若前端 axios `baseURL` 为 `/UserCenter`，与网关 StripPrefix 规则需与开发环境一致。

## 3. 多前端项目

不同 `server_name` 或 `listen` 端口指向不同 `root`（如 admin / portal / 文档站）。参考 raw「nginx 部署多个前端项目」。

## 4. 跨域

Nginx 同域部署时，浏览器访问 `https://moli.example.com` 调 `/UserCenter/...` **不跨域**，通常无需 CORS。若静态与 API 不同域，则 API 侧需 CORS 或统一域名。

## 5. HTTPS

对外 TLS 在 Nginx 终结，证书配置见 `moli-knowledge/kb/wiki/security/https与-tls基础.md`；后端可仍走 HTTP 内网。

## 6. 运维命令

```bash
nginx -t                    # 检查配置
nginx -s reload           # 平滑重载
```

更多 Linux 习惯见 `moli-knowledge/kb/wiki/ops/linux-运维基础.md`。

## 相关

`moli-knowledge/kb/wiki/frontend/前端技术栈.md` · [[生产部署拓扑备忘]] · [[docker部署指南]]
