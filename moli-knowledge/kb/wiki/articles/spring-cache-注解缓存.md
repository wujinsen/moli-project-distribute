---
title: Spring Cache 注解缓存
slug: spring-cache-注解缓存
type: article
status: active
tags: [Spring, 缓存, Redis]
sources:
 - raw/wujinsen_markdown/面试笔试/Spring/69道Spring面试题和答案.note.md
related: [redis-缓存, cache-aside与缓存更新模式, caffeine-本地缓存实践, 缓存双写与一致性策略]
created: 2026-06-21
updated: 2026-06-21
---

# Spring Cache 注解缓存

> Redis 后端 [[redis-缓存]]；更新模式 [[cache-aside与缓存更新模式]]；一致性 [[缓存双写与一致性策略]]；本地层 [[caffeine-本地缓存实践]]。

`@EnableCaching` + `@Cacheable` / `@CachePut` / `@CacheEvict` 声明式缓存，底层可接 Redis、Caffeine。

## 1. 核心注解

| 注解 | 行为 |
|------|------|
| `@Cacheable` | 命中则跳过方法体 |
| `@CachePut` | 始终执行并写缓存 |
| `@CacheEvict` | 删 key / 清 cacheName |
| `@Caching` | 组合多条 |

```java
@Cacheable(cacheNames = "user", key = "#id", unless = "#result == null")
public SysUser getById(Long id) { return mapper.selectById(id); }

@CacheEvict(cacheNames = "user", key = "#user.id")
public void update(SysUser user) { mapper.updateById(user); }
```

## 2. Redis 配置要点

- `RedisCacheManager` + JSON 序列化（注意 **类版本** 变更）
- **TTL**：`RedisCacheConfiguration.entryTtl(Duration.ofMinutes(30))`
- 多服务共享 cacheName 需约定前缀，防 key 冲突

## 4. 陷阱

| 坑 | 处理 |
|----|------|
| 自调用不缓存 | AOP 代理，拆 Service 或注入 self [[spring-aop与代理]] |
| 缓存穿透 null | `unless="#result==null"` 或布隆 |
| 更新顺序 | 先 DB 后 evict [[缓存双写与一致性策略]] |
| 热点 key | 本地 Caffeine 一级 [[多级缓存架构]] |

## 相关

[[redisson-看门狗与分布式锁]] ·
