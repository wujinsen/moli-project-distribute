---
title: Redis 分布式锁实现（演进与正确姿势）
slug: redis分布式锁实现
type: article
status: active
tags: [分布式锁, Redis, Redisson, Redlock, Lua]
sources:
 - raw/wujinsen_markdown/架构/分布式事务/redis/Redis 分布式锁进化史解读+缺陷分析.note.md
 - raw/wujinsen_markdown/架构/分布式事务/redis/Redis 分布式锁没这么简单，网上大多数都有 bug.note.md
 - raw/wujinsen_markdown/面试笔试/redis/分布式锁之Redis实现.note.md
related: [分布式锁, 分布式锁面试题, 秒杀设计]
created: 2026-06-22
updated: 2026-06-22
---

# Redis 分布式锁实现（演进与正确姿势）

> 概念与选型见 [[cache/分布式锁]]；面试速记见 [[cache/分布式锁面试题]]。
> 本页梳理 Redis 锁的**演进史**与各版本缺陷，给出工程上的正确实现。

## 一、演进史（每一版都在补前一版的坑）

| 版本 | 做法 | 缺陷 |
|------|------|------|
| V1.0 | `SETNX` + `EXPIRE` 两条命令 | 非原子：设完锁宕机→过期没设上→死锁。用 Lua 合并仍有主从切换问题 |
| V1.1 | `SETNX` + `GETSET` 时间戳判过期 | 高竞争下 value 被反复覆盖、锁过期时间被他人延长 |
| V2.0 | `SET k v NX PX`（2.6.12+，原子） | C1 执行过久锁超时→C2 获取→C1 完成 `DEL` 误删 C2 的锁 |
| V3.0 | value 存唯一时间戳，释放时校验 + Lua | 抢红包等极端并发下时间戳可能重复、物理时钟不一致 |
| **V3.1** | value 用**自增唯一 ID/UUID** 替代时间戳 | **单实例最优**；但集群主从异步仍可能丢锁 → 需 Redlock |

## 二、正确实现（单实例）

### 加锁

```
SET lock_resource_name <uuid> NX PX 30000
```

- `NX`：不存在才设置（互斥）；`PX 30000`：30s 自动过期（防死锁）；`<uuid>`：持有者唯一标识（防误删）。
- 超时时间经验值：压测得平均执行时间，再放大 **3~5 倍**，给网络抖动/GC 留缓冲。别设太长（宕机后全员等待）。

### 释放（Lua 保证「校验 + 删除」原子）

```lua
if redis.call("get", KEYS[1]) == ARGV[1] then
 return redis.call("del", KEYS[1])
else
 return 0
end
```

### 加解锁的代码位置（易错）

```java
redisLock.lock(); // 必须在 try 外？不——放 try 内首行
try {
 // 业务
} finally {
 redisLock.unlock(); // 释放必须放 finally，否则异常后锁不释放
}
```

> `lock()` 放在 `try` 内首行：即使加锁响应超时（指令其实已到服务端执行），也能进入 `finally` 解锁。

## 三、续期：看门狗（Watchdog）

固定超时两难：太短业务没跑完锁就没了，太长宕机后空等。解法：获取锁后开**守护线程**定时检测，快过期且业务未完成就**自动续期**。**Redisson** 封装好了（默认锁 30s，每 ⅓ 时间续一次）。

## 四、可重入：Redis Hash + Lua（Redisson 实现）

key=锁名，Hash 的 field=`uuid:线程ID`，value=重入次数。

- 加锁：锁不存在 → `hset` 置 1 + `pexpire`；已存在且是本线程 → `hincrby +1` 续期；否则返回持有锁的 TTL（失败）。
- 解锁：`hincrby -1`，>0 则仅续期返回 0；=0 则 `del` 并 `publish` 释放消息。

## 五、集群/主从问题与 Redlock

**问题**：主从异步复制，master 加锁后未同步即宕机，slave 升主 → 锁丢失。

**Redlock 算法**（antirez 提出，建议 5 个独立实例，奇数）：

1. 记录开始时间 T1；
2. 依次向 N 个实例用相同 key/value 加锁，每次请求超时远小于锁有效期；
3. 多数实例（**N/2+1**）成功，且总耗时 < 锁有效期 → 加锁成功（有效期减去耗时）；
4. 否则向**所有**实例发起解锁。

**争议**：Martin Kleppmann 质疑其依赖时钟一致性、GC 停顿仍会失效，主张 **fencing token**（操作带递增令牌校验）；antirez 回应过期机制的合理性。**结论**：分布式锁无绝对安全，正确性场景务必**业务幂等**兜底。

## 六、常见 bug 清单

- 用 `SETNX`+`EXPIRE` 非原子 → 死锁。
- 释放不校验持有者 → 误删别人锁。
- 「get+del」不用 Lua → 仍有并发窗口。
- 释放锁不放 `finally` → 异常后锁泄漏。
- 固定超时不续期 → 长任务锁提前失效。
- 集群主从切换 → 锁丢失（需 Redlock + 幂等）。
