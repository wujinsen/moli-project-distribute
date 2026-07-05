---
title: Feign 开发踩坑
slug: feign-开发踩坑
type: article
status: active
tags: [Feign, Spring Cloud, 踩坑]
sources:
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
- raw/wujinsen_markdown/架构/项目踩坑/无标题笔记.note.md
- raw/wujinsen_markdown/架构/项目踩坑/版本兼容问题/Error attempting to get column time from result set java.note.md
related: [openfeign-与-http客户端, 跨域与前后端分离, dubbo-与-nacos]
created: 2026-06-22
updated: 2026-07-05
---

# Feign 开发踩坑

> 概念 [[middleware/openfeign-与-http客户端]]。 Dubbo 为主，本文供 Spring Cloud HTTP 栈参考。

## 1. GET 带 @RequestBody

HTTP 语义不推荐；部分网关/代理丢弃 body → 改 POST 或 `@SpringQueryMap`。

## 2. 文件上传

- 勿用 `@RequestBody MultipartFile`
- 用 `@RequestPart` + 正确 `consumes = MULTIPART_FORM_DATA`
- 大文件调超时、内存

## 3. 重试导致重复写

Feign 默认重试 → 写接口需 **幂等** [[middleware/接口幂等性实践]] 或关闭重试。

## 4. 404/503 与负载

服务名错误、Nacos 无实例、Ribbon 缓存旧列表 → 查注册中心。

## 5. 与 Dubbo 选型

内部 Java 服务优先 Dubbo；仅当对方只暴露 REST 时用 Feign/RestTemplate。

## 相关

 · [[middleware/nacos-注册与配置]]
## Feign 常见问题（raw 采坑）

| 问题 | 处理 |
|------|------|
| **Multipart 上传** | Feign 默认不支持文件；改 `SpringFormEncoder` 或换 RestTemplate |
| **bootstrap vs application** | 配置加载顺序导致注册失败；统一 Spring Cloud 版本 |
| **Dalston/SR4 踩坑** | 与 Hystrix/Ribbon 版本对齐 |

Gateway 跨域见 [[spring/spring-cloud-gateway]]。

## 批次#1312 增补（wujinsen P1）

合并 Feign 采坑记录 + 项目版本兼容 raw。
