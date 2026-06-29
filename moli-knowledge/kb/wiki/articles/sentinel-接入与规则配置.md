---
title: Sentinel 接入与规则配置
slug: sentinel-接入与规则配置
type: article
status: active
tags: [sentinel, gateway, dubbo, 配置]
sources:
 - raw/wujinsen_markdown/架构/MicroServer/SpringCloud/sentinel/Sentinel滑动窗口介绍.note.md
 - raw/wujinsen_markdown/架构/MicroServer/SpringCloud/sentinel/sentinel动态规则源.note.md
 - moli-knowledge/kb/wiki/articles/gateway-路由与过滤器.md
related: [sentinel-限流与熔断, spring-cloud-gateway, gateway-路由与过滤器, dubbo-与-nacos, 限流算法与令牌桶, 网关]
created: 2026-06-22
updated: 2026-06-22
---

# Sentinel 接入与规则配置

> 概念枢纽 [[sentinel-限流与熔断]]；路由 [[gateway-路由与过滤器]]；Dubbo [[dubbo-与-nacos]]。

**当前常见未接入**，本文为规划与面试/实施参考。版本见 （Sentinel 1.8.1 + SCA 2.2.7）。

## 1. 处理链（Slot Chain）

Sentinel 对资源（URL、方法名、Dubbo 接口）走 **ProcessorSlotChain**：

```
NodeSelector → ClusterBuilder → Statistic → Authority → System → Flow → Degrade → ...
```

- **StatisticSlot**：滑动窗口统计 pass/block/exception/RT
- **FlowSlot**：流控规则
- **DegradeSlot**：熔断规则

### 滑动窗口（原理摘要）

- `LeapArray`：环形数组，每个元素 `WindowWrap` 包 `MetricBucket`
- 按时间戳算下标；窗过期则 reset 复用
- 指标：`PASS`、`BLOCK`、`EXCEPTION`、`RT` 等

理解即可：限流判断来自**近实时统计**，非简单全局计数。

## 2. Gateway 接入步骤

1. **依赖**（`moli-gateway`）：

```xml
<dependency>
 <groupId>com.alibaba.cloud</groupId>
 <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
<dependency>
 <groupId>com.alibaba.cloud</groupId>
 <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
</dependency>
```

2. **配置**（示例）：

```yaml
spring:
 cloud:
 sentinel:
 transport:
 dashboard: 127.0.0.1:8858
 eager: true
```

3. **规则**：在 Sentinel Dashboard 对 routeId 或自定义 API 分组配 **QPS / 并发线程数**

4. **优先资源**：

| 资源 | 建议 |
|------|------|
| `user-center-route` + `/login` | 防刷，如 50 QPS/实例 |
| `order-route` + `/seckill/**` | 高 QPS + 热点参数 `activityId` |
| 全局限流 | 系统规则 Load/CPU（慎用） |

与 [[gateway-路由与过滤器]] 中 `RequestRateLimiter`（Redis）二选一或叠加；Sentinel 规则可动态推送。

## 3. Dubbo 接入

精尽 Dubbo 笔记推荐 **Sentinel 替代 Hystrix**：

- Provider/Consumer 侧 filter
- 规则：QPS、并发、慢调用比例熔断
- 与 Nacos 可配合 **动态规则源**（`ReadableDataSource` → Nacos）

 Dubbo 现状：`group=moli` `version=1.0.0`，无 Sentinel Filter。

## 4. 规则类型

| 类型 | 字段要点 |
|------|----------|
| **流控** | grade=QPS/线程数；controlBehavior=直接拒绝/冷启动/排队 |
| **熔断** | 慢调用比例 / 异常比例 / 异常数；最小请求数、统计时长 |
| **热点** | paramIdx + 例外项 QPS |
| **系统** | 整 JVM Load、RT、线程数 |

动态规则：Dashboard 推送、或 **Nacos 数据源**（`sentinel动态规则源` 指向官方 Wiki）。

## 5. 与降级的配合

限流触发 → 快速失败或 warm-up；熔断触发 → 走 fallback（需业务实现，非 Sentinel 自动兜底数据）。

降级思路（丢卒保帅）：读服务走缓存默认值、写服务异步化——见高并发降级笔记，秒杀落库已异步（）。

## 6. 验证清单

- [ ] Dashboard 能看到 `moli-gateway` 资源
- [ ] 压测时 block 数上升、业务返回 429/统一 MoliResult
- [ ] 规则持久化到 Nacos，重启不丢
- [ ] 与 [[压测监控与prometheus]] 联调：限流后 Druid `waiting` 应下降

## 7. 常见坑

| 坑 | 说明 |
|----|------|
| 只引依赖不配 Dashboard | 规则不生效或看不到监控 |
| Gateway 与 MVC 资源名不一致 | 按 routeId / API 定义统一命名 |
| 限流过严 | 正常登录/压测 smoke 失败 |
| 与 Shiro 顺序 | 限流一般在 Gateway 最先 |
