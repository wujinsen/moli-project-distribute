---
title: Dubbo 调用原理与分层
slug: dubbo-调用原理与分层
type: article
status: active
tags: [dubbo, RPC, 微服务]
sources:
- raw/wujinsen_markdown/大数据资料-王/rpc/webservice(1)(23-14-14).note.md
- raw/wujinsen_markdown/大数据资料-王/rpc/webservice.note.md
- raw/wujinsen_markdown/大数据资料-王/rpc/轻量级分布式 RPC 框架.note.md
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
- raw/wujinsen_markdown/架构/轻量级分布式 RPC 框架.note.md
- raw/wujinsen_markdown/源码分析/dubbo/一、Dubbo 源码分析 – SPI 机制.note.md
- raw/wujinsen_markdown/源码分析/芋道源码/精尽 Dubbo 源码分析 —— 序列化（二）之 Dubbo 实现.note.md
- raw/wujinsen_markdown/面试笔试/Dubbo/Dubbo剖析-线程模型.note.md
- raw/wujinsen_markdown/面试笔试/Dubbo/Dubbo剖析-负载均衡.note.md
- raw/wujinsen_markdown/面试笔试/Dubbo/Dubbo剖析-集群容错.note.md
- raw/wujinsen_markdown/面试笔试/Dubbo/Dubbo是阿里巴巴SOA服务化治理方案的核心框架，每天为2,000+个服务提供3,000,000,000+次访问量支持，并被广泛应用于阿里巴巴集团的各成员站点.note.md
- raw/wujinsen_markdown/面试笔试/Dubbo/dubbo--zookeeper面试中问题解答.note.md
- raw/wujinsen_markdown/面试笔试/Dubbo/精尽 Dubbo 面试题.note.md
- raw/wujinsen_markdown/面试笔试/精尽面试题/dubbo/精尽 Dubbo 面试题.note.md
related: [dubbo-与-nacos, dubbo-面试题, netty-reactor与线程模型, io模型与-netty]
created: 2026-06-22
updated: 2026-07-05
---

# Dubbo 调用原理与分层

> 枢纽 [[middleware/dubbo-与-nacos]]；系统架构。

## 一次 RPC 调用（Consumer → Provider）

1. Consumer 代理调用接口方法
2. **Cluster** 负载均衡选 Invoker（从注册中心 Directory）
3. **Protocol** 封装 Invocation
4. **Serialization** 序列化
5. **Transport** Netty 发送（线程模型 [[middleware/netty-reactor与线程模型]]）
6. Provider 反序列化 → 执行实现 → 返回 Result
7. Consumer 收到响应

## 十层架构（简化为三层）

```
Business — Service 接口与实现（@DubboService / @DubboReference）
RPC — config / proxy / registry / cluster / monitor
Remoting — protocol / exchange / transport / serialize
```

- **Proxy**：生成 Stub/Skeleton 透明代理
- **Registry**：Nacos/ZK 注册与订阅 URL
- **Cluster + LoadBalance**：失败转移、随机/轮询/最少活跃等
- **Protocol**：默认 dubbo 协议（长连接、hessian2/kryo 等）

## Dubbo SPI

Dubbo **自研 SPI**（非 JDK SPI）：`META-INF/dubbo/` 扩展点，支持 Adaptive、Wrapper。

## 常见问题

- **No provider** — Provider 未启动、group/version 不一致、Nacos 命名空间错误
- **超时** — `timeout`、线程池满、下游慢 SQL
- **序列化** — 接口 DTO 需 Serializable，版本兼容
## Dubbo 剖析补充（raw 架构/面试笔试）

| 主题 | 要点 |
|------|------|
| **集群容错** | Failover（默认重试）、Failfast、Failsafe、Failback、Forking、Broadcast |
| **负载均衡** | Random、RoundRobin、LeastActive、ConsistentHash |
| **线程模型** | Netty boss/worker；Provider 业务线程池与 `threads`/`queues` 配置 |

见 [[middleware/dubbo-面试题]]。
## 批次#1310 增补（wujinsen P0）

合并 Dubbo 剖析（集群容错/负载均衡/线程模型）与精尽 Dubbo 面试题 raw。

## 批次#1320 增补（wujinsen Phase2 P0）

合并 `源码分析/dubbo/` SPI raw。

## 批次#1330 增补（wujinsen Phase3 收口）

Phase3：RPC/webservice raw。

原文插图 annex：[[middleware/annex-聊聊高并发系统之HTTP缓存]]

原文插图 annex：[[middleware/annex-Hystrix使用入门手册（中文）]]
