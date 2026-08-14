---
title: Redis 数据类型.note（原文插图 annex）
slug: annex-Redis-数据类型
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/DataBase/Redis/教程/Redis 数据类型.note.md
related: [redis-面试题]
created: 2026-07-05
updated: 2026-07-05
---

Redis⽀持五种数据类型：string（字符串），hash（哈希），list（列表），set（集合）及zset(sorted set：有序集合)。

# String（字符串）

string 是 redis 最基本的类型，你可以理解成与 Memcached ⼀模⼀样的类型，⼀个 key 对应⼀个 value。 string 类型是⼆进制安全的。意思是 redis 的 string 可以包含任何数据。⽐如jpg图⽚或者序列化的对象。 string 类型是 Redis 最基本的数据类型，string 类型的值最⼤能存储 512MB。

实例

redis 127.0.0.1:6379> SET runoob "菜⻦教程" OK redis 127.0.0.1:6379> GET runoob "菜⻦教程"

在以上实例中我们使⽤了 Redis 的 SET 和 GET 命令。键为 runob，对应的值为 菜⻦教程。 注意：⼀个键最⼤能存储 512MB。

# Hash（哈希）

Redis hash 是⼀个键值(key=>value)对集合。 Redis hash 是⼀个 string 类型的 field 和 value 的映射表，hash 特别适合⽤于存储对象。

实例

DEL runob ⽤于删除前⾯测试⽤过的 key，不然会报错：(eror) WRONGTYPE Operation against a key holding the wrong kind of value

![image 1](assets/imageFile1.png)

redis 127.0.0.1:6379> DEL runoob redis 127.0.0.1:6379> HMSET runoob field1 "Hello" field2 "World" "OK" redis 127.0.0.1:6379> HGET runoob field1 "Hello" redis 127.0.0.1:6379> HGET runoob field2 "World"

实例中我们使⽤了 Redis HMSET, HGET 命令，HMSET 设置了两个 field=>value 对, HGET 获取对应 field 对应的 value。

每个 hash 可以存储 232 -1 键值对（40多亿）。

# List（列表）

Redis 列表是简单的字符串列表，按照插⼊顺序排序。你可以添加⼀个元素到列表的头部（左边）或者尾部（右边）。

实例

redis 127.0.0.1:6379> DEL runoob redis 127.0.0.1:6379> lpush runoob redis

- (integer) 1 redis 127.0.0.1:6379> lpush runoob mongodb

- (integer) 2 redis 127.0.0.1:6379> lpush runoob rabbitmq

- (integer) 3 redis 127.0.0.1:6379> lrange runoob 0 10


- 1) "rabbitmq"

- 2) "mongodb"

- 3) "redis" redis 127.0.0.1:6379> 列表最多可存储 232 - 1 元素 (4294967295, 每个列表可存储40多亿)。


# Set（集合）

Redis 的 Set 是 string 类型的⽆序集合。 集合是通过哈希表实现的，所以添加，删除，查找的复杂度都是 O(1)。

sad 命令

添加⼀个 string 元素到 key 对应的 set 集合中，成功返回 1，如果元素已经在集合中返回 0。

sadd key member

## 实例

redis 127.0.0.1:6379> DEL runoob redis 127.0.0.1:6379> sadd runoob redis (integer) 1 redis 127.0.0.1:6379> sadd runoob mongodb (integer) 1 redis 127.0.0.1:6379> sadd runoob rabbitmq (integer) 1 redis 127.0.0.1:6379> sadd runoob rabbitmq

- (integer) 0 redis 127.0.0.1:6379> smembers runoob


- 1) "redis"

- 2) "rabbitmq"

- 3) "mongodb" 注意：以上实例中 rabitmq 添加了两次，但根据集合内元素的唯⼀性，第⼆次插⼊的元素将被忽略。 集合中最⼤的成员数为 232 - 1(4294967295, 每个集合可存储40多亿个成员)。


# zset(sortedset：有序集合)

Redis zset 和 set ⼀样也是string类型元素的集合,且不允许重复的成员。

不同的是每个元素都会关联⼀个double类型的分数。redis正是通过分数来为集合中的成员进⾏从⼩到⼤的排序。 zset的成员是唯⼀的,但分数(score)却可以重复。

## zad 命令

添加元素到集合，元素在集合中存在则更新对应score

zadd key score member

## 实例

redis 127.0.0.1:6379> DEL runoob redis 127.0.0.1:6379> zadd runoob 0 redis

- (integer) 1 redis 127.0.0.1:6379> zadd runoob 0 mongodb (integer) 1 redis 127.0.0.1:6379> zadd runoob 0 rabbitmq (integer) 1 redis 127.0.0.1:6379> zadd runoob 0 rabbitmq (integer) 0 redis 127.0.0.1:6379> ZRANGEBYSCORE runoob 0 1000


- 1) "mongodb"

- 2) "rabbitmq"

- 3) "redis"
