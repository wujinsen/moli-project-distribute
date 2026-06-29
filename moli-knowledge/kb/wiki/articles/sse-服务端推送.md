---
title: SSE 服务端推送
slug: sse-服务端推送
type: article
status: active
tags: [HTTP, 实时, 前端]
sources:
 - raw/wujinsen_markdown/
related: [websocket-实时通信, webflux-响应式入门, 跨域与前后端分离, 前端开发与联调指南]
created: 2026-06-21
updated: 2026-06-21
---

# SSE 服务端推送

> WebSocket [[websocket-实时通信]]；WebFlux [[webflux-响应式入门]]；联调。

**Server-Sent Events**：HTTP 长连接，**服务端 → 客户端**单向文本流，`Content-Type: text/event-stream`。

## 1. 与 WebSocket

| | SSE | WebSocket |
|---|-----|-----------|
| 方向 | 单向 | 双向 |
| 协议 | HTTP | 升级 WS |
| 代理/防火墙 | 友好 | 有时受限 |
| 重连 | 浏览器自动 EventSource | 需自实现 |

## 2. Spring MVC 示例

```java
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter stream() {
 SseEmitter emitter = new SseEmitter(30_000L);
 executor.execute(() -> {
 try {
 emitter.send(SseEmitter.event().name("msg").data("hello"));
 emitter.complete();
 } catch (IOException e) { emitter.completeWithError(e); }
 });
 return emitter;
}
```

## 4. 注意

- Nginx：`proxy_buffering off` [[nginx-限流与缓冲调优]]
- CORS：EventSource 跨域需正确头 [[跨域与前后端分离]]
- 连接数占用：限连接 + 心跳注释 `: keepalive`

## 相关

[[nginx反向代理与前端部署指南]] · [[api-接口安全设计]]
