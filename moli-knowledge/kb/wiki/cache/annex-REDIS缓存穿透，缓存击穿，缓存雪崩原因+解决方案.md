---
title: REDIS缓存穿透，缓存击穿，缓存雪崩原因+解决方案.note（原文插图 annex）
slug: annex-REDIS缓存穿透，缓存击穿，缓存雪崩原因+解决方案
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/缓存/REDIS缓存穿透，缓存击穿，缓存雪崩原因+解决方案.note.md
related: [cache-aside与缓存更新模式]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.cnblogs.com/xichji/p/128643.html

⼀、前⾔ 在我们⽇常的开发中，⽆不都是使⽤数据库来进⾏数据的存储，由于⼀般的系统任务中通常不会存在 ⾼并发的情况，所以这样看起来并没有什么问题，可是⼀旦涉及⼤数据量的需求，⽐如⼀些商品抢购 的情景，或者是主⻚访问量瞬间较⼤的时候，单⼀使⽤数据库来保存数据的系统会因为⾯向磁盘，磁 盘读/写速度⽐较慢的问题⽽存在严重的性能弊端，⼀瞬间成千上万的请求到来，需要系统在极短的时 间内完成成千上万次的读/写操作，这个时候往往不是数据库能够承受的，极其容易造成数据库系统瘫 痪，最终导致服务宕机的严重⽣产问题。 为了克服上述的问题，项⽬通常会引⼊NoSQL技术，这是⼀种基于内存的数据库，并且提供⼀定的持 久化功能。 redis技术就是NoSQL技术中的⼀种，但是引⼊redis⼜有可能出现缓存穿透，缓存击穿，缓存雪崩等 问题。本⽂就对这三种问题进⾏较深⼊剖析。 ⼆、初认识

缓存穿透：key对应的数据在数据源并不存在，每次针对此key的请求从缓存获取不到，请求都会到 数据源，从⽽可能压垮数据源。⽐如⽤⼀个不存在的⽤户id获取⽤户信息，不论缓存还是数据库都 没有，若⿊客利⽤此漏洞进⾏攻击可能压垮数据库。

缓存击穿：key对应的数据存在，但在redis中过期，此时若有⼤量并发请求过来，这些请求发现缓 存过期⼀般都会从后端DB加载数据并回设到缓存，这个时候⼤并发的请求可能会瞬间把后端DB压 垮。

缓存雪崩：当缓存服务器重启或者⼤量缓存集中在某⼀个时间段失效，这样在失效的时候，也会给 后端系统(⽐如DB)带来很⼤压⼒。

三、缓存穿透解决⽅案 ⼀个⼀定不存在缓存及查询不到的数据，由于缓存是不命中时被动写的，并且出于容错考虑，如果从 存储层查不到数据则不写⼊缓存，这将导致这个不存在的数据每次请求都要到存储层去查询，失去了 缓存的意义。 有很多种⽅法可以有效地解决缓存穿透问题，最常⻅的则是采⽤布隆过滤器，将所有可能存在的数据 哈希到⼀个⾜够⼤的bitmap中，⼀个⼀定不存在的数据会被 这个bitmap拦截掉，从⽽避免了对底层 存储系统的查询压⼒。另外也有⼀个更为简单粗暴的⽅法（我们采⽤的就是这种），如果⼀个查询返 回的数据为空（不管是数据不存在，还是系统故障），我们仍然把这个空结果进⾏缓存，但它的过期 时间会很短，最⻓不超过五分钟。 粗暴⽅式伪代码：

//伪代码 public object GetProductListNew() {

int cacheTime = 30; String cacheKey = "product_list";

String cacheValue = CacheHelper.Get(cacheKey); if (cacheValue != null) {

return cacheValue; }

cacheValue = CacheHelper.Get(cacheKey); if (cacheValue != null) {

return cacheValue;

} else { //数据库查询不到，为空 cacheValue = GetProductListFromDB(); if (cacheValue == null) {

//如果发现为空，设置个默认值，也缓存起来 cacheValue = string.Empty;

} CacheHelper.Add(cacheKey, cacheValue, cacheTime); return cacheValue;

} }

四、缓存击穿解决⽅案 key可能会在某些时间点被超⾼并发地访问，是⼀种⾮常“热点”的数据。这个时候，需要考虑⼀个问 题：缓存被“击穿”的问题。 使⽤互斥锁(mutex key) 业界⽐较常⽤的做法，是使⽤mutex。简单地来说，就是在缓存失效的时候（判断拿出来的值为 空），不是⽴即去load db，⽽是先使⽤缓存⼯具的某些带成功操作返回值的操作（⽐如Redis的 SETNX或者Memcache的ADD）去set⼀个mutex key，当操作返回成功时，再进⾏load db的操作 并回设缓存；否则，就重试整个get缓存的⽅法。 SETNX，是「SET if Not eXists」的缩写，也就是只有不存在的时候才设置，可以利⽤它来实现锁的 效果。

public String get(key) { String value = redis.get(key); if (value == null) { //代表缓存值过期

//设置3min的超时，防⽌del操作失败的时候，下次缓存过期⼀直不能load db if (redis.setnx(key_mutex, 1, 3 * 60) == 1) { //代表设置成功

value = db.get(key); redis.set(key, value, expire_secs); redis.del(key_mutex);

} else { //这个时候代表同时候的其他线程已经load db并回设到缓存了，这时候重试获取缓存值即 可

sleep(50); get(key); //重试

} } else {

return value; }

}

memcache代码：

if (memcache.get(key) == null) { // 3 min timeout to avoid mutex holder crash if (memcache.add(key_mutex, 3 * 60 * 1000) == true) {

value = db.get(key); memcache.set(key, value); memcache.delete(key_mutex);

} else { sleep(50); retry();

} }

其它⽅案：待各位补充。 五、缓存雪崩解决⽅案 与缓存击穿的区别在于这⾥针对很多key缓存，前者则是某⼀个key。 缓存正常从Redis中获取，示意图如下：

![image 1](assets/imageFile1.png)

缓存失效瞬间示意图如下：

![image 2](assets/imageFile2.png)

缓存失效时的雪崩效应对底层系统的冲击⾮常可怕！⼤多数系统设计者考虑⽤加锁或者队列的⽅式保 证来保证不会有⼤量的线程对数据库⼀次性进⾏读写，从⽽避免失效时⼤量的并发请求落到底层存储 系统上。还有⼀个简单⽅案就时讲缓存失效时间分散开，⽐如我们可以在原有的失效时间基础上增加 ⼀个随机值，⽐如1-5分钟随机，这样每⼀个缓存的过期时间的重复率就会降低，就很难引发集体失效 的事件。 加锁排队，伪代码如下：

String lockKey = cacheKey;

String cacheValue = CacheHelper.get(cacheKey); if (cacheValue != null) {

return cacheValue; } else {

synchronized(lockKey) { cacheValue = CacheHelper.get(cacheKey); if (cacheValue != null) {

return cacheValue; } else {

//这⾥⼀般是sql查询数据 cacheValue = GetProductListFromDB(); CacheHelper.Add(cacheKey, cacheValue, cacheTime);

}

} return cacheValue;

} }

加锁排队只是为了减轻数据库的压⼒，并没有提⾼系统吞吐量。假设在⾼并发下，缓存重建期间key是 锁着的，这是过来1000个请求999个都在阻塞的。同样会导致⽤户等待超时，这是个治标不治本的⽅ 法！ 注意：加锁排队的解决⽅式分布式环境的并发问题，有可能还要解决分布式锁的问题；线程还会被阻 塞，⽤户体验很差！因此，在真正的⾼并发场景下很少使⽤！ 随机值伪代码：

//缓存标记 String cacheSign = cacheKey + "_sign";

String sign = CacheHelper.Get(cacheSign); //获取缓存值 String cacheValue = CacheHelper.Get(cacheKey); if (sign != null) {

return cacheValue; //未过期，直接返回

} else { CacheHelper.Add(cacheSign, "1", cacheTime); ThreadPool.QueueUserWorkItem((arg) -> {

//这⾥⼀般是 sql查询数据

cacheValue = GetProductListFromDB(); //⽇期设缓存时间的2倍，⽤于脏读 CacheHelper.Add(cacheKey, cacheValue, cacheTime * 2);

}); return cacheValue;

} }

解释说明：

缓存标记：记录缓存数据是否过期，如果过期会触发通知另外的线程在后台去更新实际key的缓 存；

缓存数据：它的过期时间⽐缓存标记的时间延⻓1倍，例：标记缓存时间30分钟，数据缓存设置为 60分钟。这样，当缓存标记key过期后，实际缓存还能把旧数据返回给调⽤端，直到另外的线程在 后台更新完成后，才会返回新缓存。

关于缓存崩溃的解决⽅法，这⾥提出了三种⽅案：使⽤锁或队列、设置过期标志更新缓存、为key设置 不同的缓存失效时间，还有⼀种被称为“⼆级缓存”的解决⽅法。 六、⼩结 针对业务系统，永远都是具体情况具体分析，没有最好，只有最合适。 于缓存其它问题，缓存满了和数据丢失等问题，⼤伙可⾃⾏学习。最后也提⼀下三个词LRU、RDB、 AOF，通常我们采⽤LRU策略处理溢出，Redis的RDB和AOF持久化策略来保证⼀定情况下的数据安 全。 参考相关链接：

https://blog.csdn.net/zeb_perfect/article/details/54135506 https://blog.csdn.net/fanrenxiang/article/details/80542580 https://baijiahao.baidu.com/s?id=1619572269435584821&wfr=spider&for=pc https://blog.csdn.net/xlgen157387/article/details/79530877

视频资源获取，可直进百度云群：

https://pan.baidu.com/mbox/homepage?short=btNBJoN

本⽂在⽶兜公众号链接

https://mp.weixin.qq.com/s/ksVC1049wZgPIOy2gGziNA

欢迎关注⽶兜Java，⼀个注在共享、交流的Java学习平台。
