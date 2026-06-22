# 前⾔

分布式锁⼀般有三种实现⽅式：1. 数据库乐观锁；2. 基于Redis的分布式锁；3. 基于ZooKeeper的分布式锁。本篇博客将介 绍第⼆种⽅式，基于Redis实现分布式锁。虽然⽹上已经有各种介绍Redis分布式锁实现的博客，然⽽他们的实现却有着各种 各样的问题，为了避免误⼈⼦弟，本篇博客将详细介绍如何正确地实现Redis分布式锁。

# 可靠性

⾸先，为了确保分布式锁可⽤，我们⾄少要确保锁的实现同时满⾜以下四个条件：

- 1.
- 2.
- 3.
- 4.


互斥性。在任意时刻，只有⼀个客户端能持有锁。 不会发⽣死锁。即使有⼀个客户端在持有锁的期间崩溃⽽没有主动解锁，也能保证后续其他客户端能加锁。 具有容错性。只要⼤部分的Redis节点正常运⾏，客户端就可以加锁和解锁。 解铃还须系铃⼈。加锁和解锁必须是同⼀个客户端，客户端⾃⼰不能把别⼈加的锁给解了。

# 代码实现

组件依赖

⾸先我们要通过Maven引⼊Jedis开源组件，在pom.xml⽂件加⼊下⾯的代码：

<dependency> <groupId>redis.clients</groupId> <artifactId>jedis</artifactId> <version>2.9.0</version>

</dependency>

加锁代码

正确姿势

Talk is cheap, show me the code。先展示代码，再带⼤家慢慢解释为什么这样实现：

![image 1](<Redis分布式锁的正确实现方式.note_images/imageFile1.png>)

复制代码

public class RedisTool {

private static final String LOCK_SUCCESS = "OK"; private static final String SET_IF_NOT_EXIST = "NX"; private static final String SET_WITH_EXPIRE_TIME = "PX";

/**

- * 尝试获取分布式锁

- * @param jedis Redis客户端

- * @param lockKey 锁

- * @param requestId 请求标识

- * @param expireTime 超期时间

- * @return 是否获取成功

- */


public static boolean tryGetDistributedLock(Jedis jedis, String lockKey, String requestId, int expireTime) {

String result = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);

if (LOCK_SUCCESS.equals(result)) { return true;

} return false;

}

}

![image 2](<Redis分布式锁的正确实现方式.note_images/imageFile2.png>)

复制代码

可以看到，我们加锁就⼀⾏代码：jedis.set(String key, String value, String nxxx, String expx, int time)，这个set()⽅法⼀共有五个形参：

第⼀个为key，我们使⽤key来当锁，因为key是唯⼀的。 第⼆个为value，我们传的是requestId，很多童鞋可能不明⽩，有key作为锁不就够了吗，为什么还要⽤到value？原因就是我们在 上⾯讲到可靠性时，分布式锁要满⾜第四个条件解铃还须系铃⼈，通过给value赋值为requestId，我们就知道这把锁是哪个请求加 的了，在解锁的时候就可以有依据。requestId可以使⽤UUID.randomUUID().toString()⽅法⽣成。 第三个为nxxx，这个参数我们填的是NX，意思是SET IF NOT EXIST，即当key不存在时，我们进⾏set操作；若key已经存在，则 不做任何操作； 第四个为expx，这个参数我们传的是PX，意思是我们要给这个key加⼀个过期的设置，具体时间由第五个参数决定。 第五个为time，与第四个参数相呼应，代表key的过期时间。

总的来说，执⾏上⾯的set()⽅法就只会导致两种结果：1. 当前没有锁（key不存在），那么就进⾏加锁操作，并对锁设置个 有效期，同时value表示加锁的客户端。2. 已有锁存在，不做任何操作。 ⼼细的童鞋就会发现了，我们的加锁代码满⾜我们可靠性⾥描述的三个条件。⾸先，set()加⼊了NX参数，可以保证如果已有 key存在，则函数不会调⽤成功，也就是只有⼀个客户端能持有锁，满⾜互斥性。其次，由于我们对锁设置了过期时间，即使 锁的持有者后续发⽣崩溃⽽没有解锁，锁也会因为到了过期时间⽽⾃动解锁（即key被删除），不会发⽣死锁。最后，因为我 们将value赋值为requestId，代表加锁的客户端请求标识，那么在客户端在解锁的时候就可以进⾏校验是否是同⼀个客户 端。由于我们只考虑Redis单机部署的场景，所以容错性我们暂不考虑。

- 错误示例1 ⽐较常⻅的错误示例就是使⽤jedis.setnx()和jedis.expire()组合实现加锁，代码如下：


![image 3](<Redis分布式锁的正确实现方式.note_images/imageFile3.png>)

复制代码

public static void wrongGetLock1(Jedis jedis, String lockKey, String requestId, int expireTime) {

Long result = jedis.setnx(lockKey, requestId); if (result == 1) {

// 若在这⾥程序突然崩溃，则⽆法设置过期时间，将发⽣死锁 jedis.expire(lockKey, expireTime);

}

}

![image 4](<Redis分布式锁的正确实现方式.note_images/imageFile4.png>)

复制代码

setnx()⽅法作⽤就是SET IF NOT EXIST，expire()⽅法就是给锁加⼀个过期时间。乍⼀看好像和前⾯的set()⽅法结果⼀ 样，然⽽由于这是两条Redis命令，不具有原⼦性，如果程序在执⾏完setnx()之后突然崩溃，导致锁没有设置过期时间。那 么将会发⽣死锁。⽹上之所以有⼈这样实现，是因为低版本的jedis并不⽀持多参数的set()⽅法。

- 错误示例2


![image 5](<Redis分布式锁的正确实现方式.note_images/imageFile5.png>)

复制代码

public static boolean wrongGetLock2(Jedis jedis, String lockKey, int expireTime) {

long expires = System.currentTimeMillis() + expireTime; String expiresStr = String.valueOf(expires);

// 如果当前锁不存在，返回加锁成功 if (jedis.setnx(lockKey, expiresStr) == 1) {

return true; }

// 如果锁存在，获取锁的过期时间 String currentValueStr = jedis.get(lockKey); if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {

// 锁已过期，获取上⼀个锁的过期时间，并设置现在锁的过期时间 String oldValueStr = jedis.getSet(lockKey, expiresStr); if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {

// 考虑多线程并发的情况，只有⼀个线程的设置值和当前值相同，它才有权利加锁 return true;

} }

// 其他情况，⼀律返回加锁失败 return false;

}

![image 6](<Redis分布式锁的正确实现方式.note_images/imageFile6.png>)

复制代码

这⼀种错误示例就⽐较难以发现问题，⽽且实现也⽐较复杂。实现思路：使⽤jedis.setnx()命令实现加锁，其中key是 锁，value是锁的过期时间。执⾏过程：1. 通过setnx()⽅法尝试加锁，如果当前锁不存在，返回加锁成功。2. 如果锁已经存 在则获取锁的过期时间，和当前时间⽐较，如果锁已经过期，则设置新的过期时间，返回加锁成功。代码如下：

那么这段代码问题在哪⾥？1. 由于是客户端⾃⼰⽣成过期时间，所以需要强制要求分布式下每个客户端的时间必须同步。 2. 当锁过期的时候，如果多个客户端同时执⾏jedis.getSet()⽅法，那么虽然最终只有⼀个客户端可以加锁，但是这个客户 端的锁的过期时间可能被其他客户端覆盖。3. 锁不具备拥有者标识，即任何客户端都可以解锁。

## 解锁代码

正确姿势

还是先展示代码，再带⼤家慢慢解释为什么这样实现：

![image 7](<Redis分布式锁的正确实现方式.note_images/imageFile7.png>)

复制代码

public class RedisTool {

private static final Long RELEASE_SUCCESS = 1L;

/**

- * 释放分布式锁

- * @param jedis Redis客户端

- * @param lockKey 锁

- * @param requestId 请求标识

- * @return 是否释放成功

- */


public static boolean releaseDistributedLock(Jedis jedis, String lockKey, String requestId) {

String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));

if (RELEASE_SUCCESS.equals(result)) { return true;

} return false;

}

}

![image 8](<Redis分布式锁的正确实现方式.note_images/imageFile8.png>)

复制代码

可以看到，我们解锁只需要两⾏代码就搞定了！第⼀⾏代码，我们写了⼀个简单的Lua脚本代码，上⼀次⻅到这个编程语⾔还 是在《⿊客与画家》⾥，没想到这次居然⽤上了。第⼆⾏代码，我们将Lua代码传到jedis.eval()⽅法⾥，并使参数 KEYS[1]赋值为lockKey，ARGV[1]赋值为requestId。eval()⽅法是将Lua代码交给Redis服务端执⾏。 那么这段Lua代码的功能是什么呢？其实很简单，⾸先获取锁对应的value值，检查是否与requestId相等，如果相等则删除 锁（解锁）。那么为什么要使⽤Lua语⾔来实现呢？因为要确保上述操作是原⼦性的。关于⾮原⼦性会带来什么问题，可以阅 读 。那么为什么执⾏eval()⽅法可以确保原⼦性，源于Redis的特性，下⾯是官⽹对eval命令的部分 解释：

【解锁代码-错误示例2】

![image 9](<Redis分布式锁的正确实现方式.note_images/imageFile9.png>)

简单来说，就是在eval命令执⾏Lua代码的时候，Lua代码将被当成⼀个命令去执⾏，并且直到eval命令执⾏完成，Redis才 会执⾏其他命令。

- 错误示例1 最常⻅的解锁代码就是直接使⽤jedis.del()⽅法删除锁，这种不先判断锁的拥有者⽽直接解锁的⽅式，会导致任何客户端 都可以随时进⾏解锁，即使这把锁不是它的。

- public static void wrongReleaseLock1(Jedis jedis, String lockKey) { jedis.del(lockKey);

}

错误示例2

这种解锁代码乍⼀看也是没问题，甚⾄我之前也差点这样实现，与正确姿势差不多，唯⼀区别的是分成两条命令去执⾏，代码 如下：

- public static void wrongReleaseLock2(Jedis jedis, String lockKey, String requestId) {




![image 10](<Redis分布式锁的正确实现方式.note_images/imageFile10.png>)

复制代码

// 判断加锁与解锁是不是同⼀个客户端 if (requestId.equals(jedis.get(lockKey))) {

// 若在此时，这把锁突然不是这个客户端的，则会误解锁 jedis.del(lockKey);

}

}

![image 11](<Redis分布式锁的正确实现方式.note_images/imageFile11.png>)

复制代码

如代码注释，问题在于如果调⽤jedis.del()⽅法的时候，这把锁已经不属于当前客户端的时候会解除他⼈加的锁。那么是 否真的有这种场景？答案是肯定的，⽐如客户端A加锁，⼀段时间之后客户端A解锁，在执⾏jedis.del()之前，锁突然过期 了，此时客户端B尝试加锁成功，然后客户端A再执⾏del()⽅法，则将客户端B的锁给解除了。

总结

本⽂主要介绍了如何使⽤Java代码正确实现Redis分布式锁，对于加锁和解锁也分别给出了两个⽐较经典的错误示例。其实想 要通过Redis实现分布式锁并不难，只要保证能满⾜可靠性⾥的四个条件。互联⽹虽然给我们带来了⽅便，只要有问题就可以 google，然⽽⽹上的答案⼀定是对的吗？其实不然，所以我们更应该时刻保持着质疑精神，多想多验证。 如果你的项⽬中Redis是多机部署的，那么可以尝试使⽤Redisson实现分布式锁，这是Redis官⽅提供的Java组件，链接在 参考阅读章节已经给出。

