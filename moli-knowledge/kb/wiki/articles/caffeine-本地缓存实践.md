---
title: Caffeine 本地缓存实践
slug: caffeine-本地缓存实践
type: article
status: active
tags: [缓存, Java, 性能]
sources:
 - raw/wujinsen_markdown/
related: [多级缓存架构, spring-cache-注解缓存, redis-缓存, 缓存双写与一致性策略]
created: 2026-06-21
updated: 2026-06-21
---

# Caffeine 本地缓存实践

> 架构 [[多级缓存架构]]；Spring 集成 [[spring-cache-注解缓存]]；Redis L2 [[redis-缓存]]。

**Caffeine** 高性能 JVM 本地缓存（W-TinyLFU 淘汰），适合读多写少、可容忍短时不一致的数据。

## 1. 典型 L1+L2

```
读：Caffeine → miss → Redis → miss → DB
写：DB → 删 Redis → 删/等 TTL 本地（或广播失效）
```

## 2. 构建示例

```java
Cache<Long, SeckillActivityVo> local = Caffeine.newBuilder()
 .maximumSize(10_000)
 .expireAfterWrite(30, TimeUnit.SECONDS)
 .recordStats()
 .build();

SeckillActivityVo get(Long id) {
 return local.get(id, k -> redisTemplate.opsForValue().get("act:" + k));
}
```

`recordStats()` → 命中率对接 [[micrometer-与指标暴露]]。

## 3. 参数建议

| 参数 | 说明 |
|------|------|
| `maximumSize` / `maximumWeight` | 防堆 OOM |
| `expireAfterWrite` | 活动页、字典 |
| `refreshAfterWrite` | 后台异步刷新，防击穿 |
| `weakKeys` | 大对象场景慎用 |

## 5. 多实例一致性

单机 Caffeine 各节点独立 → 写后 **Redis Pub/Sub 广播 evict** 或接受秒级延迟；强一致走 Redis only。

## 相关

[[cache-aside与缓存更新模式]] · [[sentinel-热点参数限流]]
