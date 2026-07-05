---
title: Spring Cloud Gateway
slug: spring-cloud-gateway
type: concept
status: active
tags: [gateway, Spring Cloud, 微服务, WebFlux]
sources:
- docs/zh-CN/ARCHITECTURE.md
- moli-gateway/src/main/resources/application-dev.yml
- raw/wujinsen_markdown/架构/MicroServer/Dubbo/服务架构演进.note.md
- raw/wujinsen_markdown/架构/MicroServer/Java 微服务实践.note.md
- raw/wujinsen_markdown/架构/MicroServer/Service mesh ：下一代微服务？.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringBoot/2.0/Spring Boot 2.0 - WebFlux framework.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringBoot/Spring Boot改变JDK版本编译.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringBoot/quesion/在@RestController的方法中，如果路径参数带.(点号)会截断，如何配置？.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringBoot/server.jsp-servlet.init-parameters.development=true.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringBoot/无标题笔记.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/Hystrix dashboard.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/Hystrix 使用与分析.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/Hystrix使用入门手册（中文）.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/Hystrix入门.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/RxJava/RxJava 从入门到放弃再到不离不弃.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/RxJava/RxJava 驯服数据流之 hot & cold Observable.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/RxJava/前言.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/RxJava/彻底搞清楚 RxJava 是什么东西.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/RxJava/我所理解的RxJava——上手其实很简单（一）.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/it隐/Hystrix的简单介绍.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/it隐/使用Hystrix提高系统可用性.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/it隐/使用hystrix保护你的应用.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/it隐/无标题笔记.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/it隐/隔离术之使用 Hystrix 实现隔离.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/官网文档/How it Works.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/官网文档/Hystrix.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/聊聊高并发系统/聊聊高并发系统之HTTP缓存.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/聊聊高并发系统/聊聊高并发系统之队列术.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/聊聊高并发系统/聊聊高并发系统之降级特技.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/聊聊高并发系统/聊聊高并发系统之限流特技-1.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/聊聊高并发系统/聊聊高并发系统之限流特技-2.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/获取.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/SpringCloudGateWay/SpringCloudGateway功能.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/SpringCloudGateWay/gateway网关与前端请求跨域问题的解决方案.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/SpringCloudGateWay/深入剖析网关gateway原理.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/SpringCloud组件方案.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/sentinel/Sentinel滑动窗口介绍.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/sentinel/sentinel动态规则源.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/Spring Cloud Feign 上传文件的常见问题.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/boostrap application application-dev.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/fiegn默认是不支持传递文件, 修改为支持传递文件multipartfile.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/spring cloud Dalston.SR4 feign 实际开发中踩坑(一).note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/spring cloud Dalston.SR4 feign 实际开发中踩坑(二).note.md
related: [gateway-路由与过滤器, spring-mvc请求流程, 认证与会话机制, io模型与-netty, netty-reactor与线程模型, 跨域与前后端分离]
created: 2026-06-22
updated: 2026-07-05
---

# Spring Cloud Gateway（概念枢纽）

> 实例页 ；路由配置 [[middleware/gateway-路由与过滤器]]；全链路。

**Spring Cloud Gateway** 是 Spring 官方 API 网关：基于 **WebFlux + Netty**（Reactor 模型见 [[middleware/io模型与-netty]]），非 Tomcat Servlet。 `moli-gateway` 端口 **21000**，统一对外 HTTP 入口。

## 与 Spring MVC 对比

| | Gateway | 业务服务 MVC |
|---|---------|--------------|
| 模型 | 响应式 WebFlux | Servlet 阻塞 |
| 职责 | 路由、过滤、聚合 | 业务 REST |
| 端口 | 21000 | 8888/8087/… |

见 [[spring/spring-mvc请求流程]]。

## 核心概念

| 概念 | 说明 |
|------|------|
| **Route** | id + URI + Predicate + Filter |
| **Predicate** | 匹配条件，如 `Path=/UserCenter/**` |
| **Filter** | 改写请求/响应，如 `StripPrefix=1` |
| **URI** | `lb://service-name` 经 Nacos + Ribbon 负载均衡 |

## 典型访问

```
http://localhost:21000/UserCenter/login
 → StripPrefix → user-center:8888/login
```

## 演进方向

- 网关统一鉴权 / JWT 校验（可选）
- Sentinel 限流（秒杀/登录防刷）
- 全局 CORS、请求日志、TraceId — 前端跨域见 [[middleware/跨域与前后端分离]]

配置细节 [[middleware/gateway-路由与过滤器]]。
## Gateway 要点（raw）

- **路由**：Predicate + Filter 链；动态路由可接 Nacos
- **跨域**：全局 `CorsWebFilter` 或 YAML `globalcors`
- **与 Sentinel**：网关层 QPS/热点参数限流 [[middleware/sentinel-限流与熔断]]

## 批次#1312 增补（wujinsen P1）

合并 Gateway 原理/跨域 raw。

原文插图 annex：[[middleware/annex-聊聊高并发系统之HTTP缓存]]

原文插图 annex：[[middleware/annex-Hystrix使用入门手册（中文）]]

## 原文插图（wujinsen）

> wujinsen 原文插图回迁（T22）· 共 7 组

> 图源 `raw/wujinsen_markdown/架构/MicroServer/Dubbo/服务架构演进.note.md` · T22 **B** 档

### 来自：服务架构演进

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/Dubbo/%E6%9C%8D%E5%8A%A1%E6%9E%B6%E6%9E%84%E6%BC%94%E8%BF%9B.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/it隐/Hystrix的简单介绍.note.md` · T22 **B** 档

### 来自：Hystrix的简单介绍

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/Hystrix/it%E9%9A%90/Hystrix%E7%9A%84%E7%AE%80%E5%8D%95%E4%BB%8B%E7%BB%8D.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/SpringCloudGateWay/深入剖析网关gateway原理.note.md` · T22 **B** 档

### 来自：深入剖析网关gateway原理

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/SpringCloudGateWay/%E6%B7%B1%E5%85%A5%E5%89%96%E6%9E%90%E7%BD%91%E5%85%B3gateway%E5%8E%9F%E7%90%86.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/it隐/使用Hystrix提高系统可用性.note.md` · T22 **B** 档

### 来自：使用Hystrix提高系统可用性

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/Hystrix/it%E9%9A%90/%E4%BD%BF%E7%94%A8Hystrix%E6%8F%90%E9%AB%98%E7%B3%BB%E7%BB%9F%E5%8F%AF%E7%94%A8%E6%80%A7.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/Hystrix/it%E9%9A%90/%E4%BD%BF%E7%94%A8Hystrix%E6%8F%90%E9%AB%98%E7%B3%BB%E7%BB%9F%E5%8F%AF%E7%94%A8%E6%80%A7.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/聊聊高并发系统/聊聊高并发系统之限流特技-1.note.md` · T22 **B** 档

### 来自：聊聊高并发系统之限流特技-1

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/Hystrix/%E8%81%8A%E8%81%8A%E9%AB%98%E5%B9%B6%E5%8F%91%E7%B3%BB%E7%BB%9F/%E8%81%8A%E8%81%8A%E9%AB%98%E5%B9%B6%E5%8F%91%E7%B3%BB%E7%BB%9F%E4%B9%8B%E9%99%90%E6%B5%81%E7%89%B9%E6%8A%80-1.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/Hystrix/%E8%81%8A%E8%81%8A%E9%AB%98%E5%B9%B6%E5%8F%91%E7%B3%BB%E7%BB%9F/%E8%81%8A%E8%81%8A%E9%AB%98%E5%B9%B6%E5%8F%91%E7%B3%BB%E7%BB%9F%E4%B9%8B%E9%99%90%E6%B5%81%E7%89%B9%E6%8A%80-1.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/聊聊高并发系统/聊聊高并发系统之限流特技-2.note.md` · T22 **B** 档

### 来自：聊聊高并发系统之限流特技-2

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/Hystrix/%E8%81%8A%E8%81%8A%E9%AB%98%E5%B9%B6%E5%8F%91%E7%B3%BB%E7%BB%9F/%E8%81%8A%E8%81%8A%E9%AB%98%E5%B9%B6%E5%8F%91%E7%B3%BB%E7%BB%9F%E4%B9%8B%E9%99%90%E6%B5%81%E7%89%B9%E6%8A%80-2.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/Hystrix/%E8%81%8A%E8%81%8A%E9%AB%98%E5%B9%B6%E5%8F%91%E7%B3%BB%E7%BB%9F/%E8%81%8A%E8%81%8A%E9%AB%98%E5%B9%B6%E5%8F%91%E7%B3%BB%E7%BB%9F%E4%B9%8B%E9%99%90%E6%B5%81%E7%89%B9%E6%8A%80-2.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/SpringCloudGateWay/gateway网关与前端请求跨域问题的解决方案.note.md` · T22 **B** 档

### 来自：gateway网关与前端请求跨域问题的解决方案

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/SpringCloudGateWay/gateway%E7%BD%91%E5%85%B3%E4%B8%8E%E5%89%8D%E7%AB%AF%E8%AF%B7%E6%B1%82%E8%B7%A8%E5%9F%9F%E9%97%AE%E9%A2%98%E7%9A%84%E8%A7%A3%E5%86%B3%E6%96%B9%E6%A1%88.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/SpringCloudGateWay/gateway%E7%BD%91%E5%85%B3%E4%B8%8E%E5%89%8D%E7%AB%AF%E8%AF%B7%E6%B1%82%E8%B7%A8%E5%9F%9F%E9%97%AE%E9%A2%98%E7%9A%84%E8%A7%A3%E5%86%B3%E6%96%B9%E6%A1%88.note_images/imageFile2.png)

原文插图 annex：[[middleware/annex-聊聊高并发系统之HTTP缓存]]

原文插图 annex：[[middleware/annex-Hystrix使用入门手册（中文）]]

原文插图 annex：[[middleware/annex-使用hystrix保护你的应用]]

原文插图 annex：[[middleware/annex-Hystrix]]

原文插图 annex：[[middleware/annex-聊聊高并发系统之队列术]]

原文插图 annex：[[middleware/annex-Service-mesh-：下一代微服务？]]

原文插图 annex：[[middleware/annex-Hystrix-使用与分析]]

原文插图 annex：[[middleware/annex-How-it-Works]]

原文插图 annex：[[middleware/annex-RxJava-从入门到放弃再到不离不弃]]

原文插图 annex：[[middleware/annex-彻底搞清楚-RxJava-是什么东西]]

原文插图 annex：[[middleware/annex-Hystrix-dashboard]]

原文插图 annex：[[middleware/annex-spring-cloud-Dalston.SR4-feign-实际开发中踩坑(一)]]

原文插图 annex：[[middleware/annex-RxJava-驯服数据流之-hot-&-cold-Observable]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/架构/MicroServer/Java 微服务实践.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/架构/MicroServer/Java 微服务实践.note.md` · T22 **B** 档

### 来自：Java 微服务实践

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/Java%20%E5%BE%AE%E6%9C%8D%E5%8A%A1%E5%AE%9E%E8%B7%B5.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/架构/MicroServer/SpringBoot/2.0/Spring Boot 2.0 - WebFlux framework.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/架构/MicroServer/SpringBoot/2.0/Spring Boot 2.0 - WebFlux framework.note.md` · T22 **B** 档

### 来自：Spring Boot 2.0 - WebFlux framework

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringBoot/2.0/Spring%20Boot%202.0%20-%20WebFlux%20framework.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/Spring Cloud Feign 上传文件的常见问题.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/Spring Cloud Feign 上传文件的常见问题.note.md` · T22 **B** 档

### 来自：Spring Cloud Feign 上传文件的常见问题

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/%E9%87%87%E5%9D%91%E8%AE%B0%E5%BD%95/Spring%20Cloud%20Feign%20%E4%B8%8A%E4%BC%A0%E6%96%87%E4%BB%B6%E7%9A%84%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/%E9%87%87%E5%9D%91%E8%AE%B0%E5%BD%95/Spring%20Cloud%20Feign%20%E4%B8%8A%E4%BC%A0%E6%96%87%E4%BB%B6%E7%9A%84%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98.note_images/imageFile2.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/spring cloud Dalston.SR4 feign 实际开发中踩坑(二).note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/spring cloud Dalston.SR4 feign 实际开发中踩坑(二).note.md` · T22 **B** 档

### 来自：spring cloud Dalston.SR4 feign 实际开发中踩坑(二)

![imageFile1.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/%E9%87%87%E5%9D%91%E8%AE%B0%E5%BD%95/spring%20cloud%20Dalston.SR4%20feign%20%E5%AE%9E%E9%99%85%E5%BC%80%E5%8F%91%E4%B8%AD%E8%B8%A9%E5%9D%91%28%E4%BA%8C%29.note_images/imageFile1.png)

![imageFile2.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%9E%B6%E6%9E%84/MicroServer/SpringCloud/%E9%87%87%E5%9D%91%E8%AE%B0%E5%BD%95/spring%20cloud%20Dalston.SR4%20feign%20%E5%AE%9E%E9%99%85%E5%BC%80%E5%8F%91%E4%B8%AD%E8%B8%A9%E5%9D%91%28%E4%BA%8C%29.note_images/imageFile2.png)
