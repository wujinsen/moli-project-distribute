在Java中，关于锁我想⼤家都很熟悉。在并发编程中，我们通过锁，来避免由于竞争⽽造成的数据不 ⼀致问题。通常，我们以synchronized 、Lock来使⽤它。

但是Java中的锁，只能保证在同⼀个JVM进程内中执⾏。如果在分布式集群环境下呢？

⼀、分布式锁

分布式锁，是⼀种思想，它的实现⽅式有很多。⽐如，我们将沙滩当做分布式锁的组件，那么它看起 来应该是这样的：

加锁 在沙滩上踩⼀脚，留下⾃⼰的脚印，就对应了加锁操作。其他进程或者线程，看到沙滩上已经有脚 印，证明锁已被别⼈持有，则等待。

解锁 把脚印从沙滩上抹去，就是解锁的过程。

锁超时 为了避免死锁，我们可以设置⼀阵⻛，在单位时间后刮起，将脚印⾃动抹去。

分布式锁的实现有很多，⽐如基于数据库、memcached、Redis、系统⽂件、zokeper等。它们的核 ⼼的理念跟上⾯的过程⼤致相同。

⼆、redis

我们先来看如何通过单节点Redis实现⼀个简单的分布式锁。

- 1、加锁


加锁实际上就是在redis中，给Key键设置⼀个值，为避免死锁，并给定⼀个过期时间。

SET lock_key random_value NX PX 5 0

值得注意的是： random_value 是客户端⽣成的唯⼀的字符串。

NX 代表只在键不存在时，才对键进⾏设置操作。 PX 5 0 设置键的过期时间为5 0毫秒。

这样，如果上⾯的命令执⾏成功，则证明客户端获取到了锁。

- 2、解锁

解锁的过程就是将Key键删除。但也不能乱删，不能说客户端1的请求将客户端2的锁给删除掉。这时候 random_value的作⽤就体现出来。

为了保证解锁操作的原⼦性，我们⽤LUA脚本完成这⼀操作。先判断当前锁的字符串是否与传⼊的值 相等，是的话就删除Key，解锁成功。

if redis.cal('get',KEYS[1]) = ARGV[1] then

return redis.cal('del',KEYS[1]) else

return 0 end

- 3、实现


⾸先，我们在pom⽂件中，引⼊Jedis。在这⾥，笔者⽤的是最新版本，注意由于版本的不同，API可 能有所差异。

<dependency> <groupId>redis.clients</groupId> <artifactId>jedis</artifactId> <version>3.0.1</version>

</dependency> 加锁的过程很简单，就是通过SET指令来设置值，成功则返回；否则就循环等待，在timeout时间内仍 未获取到锁，则获取失败。

@Service public clas RedisLock {

Loger loger = LogerFactory.getLoger(this.getClas();

private String lock_key = "redis_lock"; /锁键

protected long internalLockLeaseTime = 3 0;/锁过期时间

private long timeout = 9; /获取锁的超时时间

/SET命令的参数 SetParams params = SetParams.setParams().nx().px(internalLockLeaseTime);

@Autowired JedisPol jedisPol;

/*

- * 加锁
- * @param id
- * @return
- */ public bolean lock(String id){


Jedis jedis = jedisPol.getResource(); Long start = System.curentTimeMilis(); try{

for(;){

/SET命令返回OK ，则证明获取锁成功 String lock = jedis.set(lock_key, id, params); if("OK".equals(lock){

return true; }

/否则循环等待，在timeout时间内仍未获取到锁，则获取失败 long l = System.curentTimeMilis() - start; if (l>=timeout) { return false;

} try {

Thread.sl ep(10); } catch (InteruptedException e) { e.printStackTrace();

} }

}finaly {

jedis.close(); }

}

} 解锁我们通过jedis.eval来执⾏⼀段LUA就可以。将锁的Key键和⽣成的字符串当做参数传进来。

/*

- * 解锁
- * @param id
- * @return
- */ public bolean unlock(String id){


Jedis jedis = jedisPol.getResource(); String script =

"if redis.cal('get',KEYS[1]) = ARGV[1] then" + " return redis.cal('del',KEYS[1]) " + "else" + " return 0 " + "end";

try { Object result = jedis.eval(script, Colections.singletonList(lock_key),

Colections.singletonList(id); if("1".equals(result.toString( ){

return true;

} return false;

}finaly {

jedis.close(); }

} 最后，我们可以在多线程环境下测试⼀下。我们开启1 0个线程，对count进⾏累加。调⽤的时候，关 键是唯⼀字符串的⽣成。这⾥，笔者使⽤的是Snowflake算法。

@Controler

public clas IndexControler {

@Autowired RedisLock redisLock;

int count = 0;

@RequestMaping("/index") @ResponseBody public String index() throws InteruptedException {

int clientcount =1 0; CountDownLatch countDownLatch = new CountDownLatch(clientcount);

ExecutorService executorService = Executors.newFixedThreadPol(clientcount); long start = System.curentTimeMilis(); for (int i = 0;i<clientcount;i +){

executorService.execute() -> {

/通过Snowflake算法获取唯⼀的ID字符串 String id = IdUtil.getId(); try {

redisLock.lock(id); count+;

}finaly { redisLock.unlock(id);

} countDownLatch.countDown();

});

} countDownLatch.await(); long end = System.curentTimeMilis(); loger.info("执⾏线程数:{},总耗时:{},count数为:{}",clientcount,end-start,count); return "Helo";

} }

⾄此，单节点Redis的分布式锁的实现就已经完成了。⽐较简单，但是问题也⽐较⼤，最重要的⼀点 是，锁不具有可重⼊性。

三、redison

Redison是架设在Redis基础上的⼀个Java驻内存数据⽹格（In-Memory Data Grid）。充分的利⽤了 Redis键值数据库提供的⼀系列优势，基于Java实⽤⼯具包中常⽤接⼝，为使⽤者提供了⼀系列具有分 布式特性的常⽤⼯具类。使得原本作为协调单机多线程并发程序的⼯具包获得了协调分布式多机多线 程并发系统的能⼒，⼤⼤降低了设计和研发⼤规模分布式系统的难度。同时结合各富特⾊的分布式服 务，更进⼀步简化了分布式环境中程序相互之间的协作。 相对于Jedis⽽⾔，Redison强⼤的⼀批。当然了，随之⽽来的就是它的复杂性。它⾥⾯也实现了分布 式锁，⽽且包含多种类型的锁，更多请参阅分布式锁和同步器

- 1、可重⼊锁


上⾯我们⾃⼰实现的Redis分布式锁，其实不具有可重⼊性。那么下⾯我们先来看看Redison中如何调 ⽤可重⼊锁。

在这⾥，笔者使⽤的是它的最新版本，3.10.1。

<dependency> <groupId>org.redison</groupId> <artifactId>redison</artifactId> <version>3.10.1</version>

</dependency> ⾸先，通过配置获取RedisonClient客户端的实例，然后getLock获取锁的实例，进⾏操作即可。

public static void main(String[] args) {

Config config = new Config(); config.useSingleServer().setAdres("redis:/127.0.0.1 6379"); config.useSingleServer().setPasword("redis1234");

final RedisonClient client = Redison.create(config); RLock lock = client.getLock("lock1");

try{

lock.lock(); }finaly{

lock.unlock(); }

}

- 2、获取锁实例

我们先来看RLock lock = client.getLock("lock1"); 这句代码就是为了获取锁的实例，然后我们可以看 到它返回的是⼀个RedisonLock对象。

public RLock getLock(String name) { return new RedisonLock(conectionManager.getComandExecutor(), name);

} 在RedisonLock构造⽅法中，主要初始化⼀些属性。

public RedisonLock(ComandAsyncExecutor comandExecutor, String name) { super(comandExecutor, name); /命令执⾏器 this.comandExecutor = comandExecutor; / UID字符串 this.id = comandExecutor.getConectionManager().getId(); /内部锁过期时间 this.internalLockLeaseTime = comandExecutor.

getConectionManager().getCfg().getLockWatchdogTimeout(); this.entryName = id + ":" + name;

}

- 3、加锁


当我们调⽤lock⽅法，定位到lockInteruptibly。在这⾥，完成了加锁的逻辑。

public void lockInteruptibly(long leaseTime, TimeUnit unit) throws InteruptedException {

/当前线程ID long threadId = Thread.curentThread().getId(); /尝试获取锁 Longtl = tryAcquire(leaseTime, unit, threadId); / 如果 tl为空，则证明获取锁成功

if (tl = nul) {

return; }

/如果获取锁失败，则订阅到对应这个锁的chanel RFuture<RedisonLockEntry> future = subscribe(threadId); comandExecutor.syncSubscription(future);

try {

while (true) { /再次尝试获取锁 tl = tryAcquire(leaseTime, unit, threadId); /tl为空，说明成功获取锁，返回

if (tl = nul) {

break; }

/tl⼤于0 则等待 tl时间后继续尝试获取 if (tl >= 0) {

getEntry(threadId).getLatch().tryAcquire(tl, TimeUnit.MI LISECONDS); } else {

getEntry(threadId).getLatch().acquire(); }

} } finaly { /取消对chanel的订阅

unsubscribe(future, threadId); }

/get(lockAsync(leaseTime, unit);

} 如上代码，就是加锁的全过程。先调⽤tryAcquire来获取锁，如果返回值 tl为空，则证明加锁成功，返 回；如果不为空，则证明加锁失败。这时候，它会订阅这个锁的Chanel，等待锁释放的消息，然后重 新尝试获取锁。流程如下：

获取锁

获取锁的过程是怎样的呢？接下来就要看tryAcquire⽅法。在这⾥，它有两种处理⽅式，⼀种是带有过 期时间的锁，⼀种是不带过期时间的锁。

private <T> RFuture<Long> tryAcquireAsync(long leaseTime, TimeUnit unit, final long threadId) {

/如果带有过期时间，则按照普通⽅式获取锁 if (leaseTime != -1) {

return tryLockI nerAsync(leaseTime, unit, threadId, RedisComands.EVAL_LONG); }

/先按照30秒的过期时间来执⾏获取锁的⽅法

RFuture<Long>tlRemainingFuture = tryLockI nerAsync( comandExecutor.getConectionManager().getCfg().getLockWatchdogTimeout(), TimeUnit.MI LISECONDS, threadId, RedisComands.EVAL_LONG);

/如果还持有这个锁，则开启定时任务不断刷新该锁的过期时间 tlRemainingFuture.adListener(new FutureListener<Long>() {

@Overide public void operationComplete(Future<Long> future) throws Exception {

if (!future.isSuces() {

return; }

LongtlRemaining = future.getNow(); / lock acquired if (tlRemaining = nul) {

scheduleExpirationRenewal(threadId); }

}

}); returntlRemainingFuture;

} 接着往下看，tryLockI nerAsync⽅法是真正执⾏获取锁的逻辑，它是⼀段LUA脚本代码。在这⾥，它 使⽤的是hash数据结构。

<T> RFuture<T> tryLockI nerAsync(long leaseTime, TimeUnit unit, long threadId, RedisStrictComand<T> comand) {

/过期时间

internalLockLeaseTime = unit.toMilis(leaseTime);

return comandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, comand, /如果锁不存在，则通过hset设置它的值，并设置过期时间

"if (redis.cal('exists', KEYS[1]) = 0) then " + "redis.cal('hset', KEYS[1], ARGV[2], 1); " + "redis.cal('pexpire', KEYS[1], ARGV[1]); " + "return nil; " +

"end; " + /如果锁已存在，并且锁的是当前线程，则通过hincrby给数值递增1

"if (redis.cal('hexists', KEYS[1], ARGV[2]) = 1) then " + "redis.cal('hincrby', KEYS[1], ARGV[2], 1); " + "redis.cal('pexpire', KEYS[1], ARGV[1]); " + "return nil; " +

"end; " +

/如果锁已存在，但并⾮本线程，则返回过期时间 tl "return redis.cal('ptl', KEYS[1]);",

Colections.<Object>singletonList(getName(),

internalLockLeaseTime, getLockName(threadId); }

这段LUA代码看起来并不复杂，有三个判断：

通过exists判断，如果锁不存在，则设置值和过期时间，加锁成功 通过hexists判断，如果锁已存在，并且锁的是当前线程，则证明是重⼊锁，加锁成功 如果锁已存在，但锁的不是当前线程，则证明有其他线程持有锁。返回当前锁的过期时间，加锁失败

加锁成功后，在redis的内存数据中，就有⼀条hash结构的数据。Key为锁的名称；field为随机字符串

+线程ID；值为1。如果同⼀线程多次调⽤lock⽅法，值递增1。

127.0.0.1 6379> hgetal lock1

- 1) "b5ae0be4-5623-45a5-8fa-ab7eb167ce87 1"
- 2) "1"


- 4、解锁


我们通过调⽤unlock⽅法来解锁。

public RFuture<Void> unlockAsync(final long threadId) {

final RPromise<Void> result = new RedisonPromise<Void>();

/解锁⽅法 RFuture<Bolean> future = unlockI nerAsync(threadId);

future.adListener(new FutureListener<Bolean>() { @Overide public void operationComplete(Future<Bolean> future) throws Exception {

if (!future.isSuces() { cancelExpirationRenewal(threadId); result.tryFailure(future.cause(); return;

} /获取返回值 Bolean opStatus = future.getNow();

/如果返回空，则证明解锁的线程和当前锁不是同⼀个线程，抛出异常 if (opStatus = nul) {

IlegalMonitorStateException cause = new IlegalMonitorStateException("

atempt to unlock lock, not locked by curent thread by node id: "

+ id + " thread-id: " + threadId); result.tryFailure(cause); return;

}

/解锁成功，取消刷新过期时间的那个定时任务 if (opStatus) {

cancelExpirationRenewal(nul);

} result.trySuces(nul);

} });

return result;

} 然后我们再看unlockI nerAsync⽅法。这⾥也是⼀段LUA脚本代码。

protected RFuture<Bolean> unlockI nerAsync(long threadId) {

return comandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, EVAL,

/如果锁已经不存在， 发布锁释放的消息

"if (redis.cal('exists', KEYS[1]) = 0) then " + "redis.cal('publish', KEYS[2], ARGV[1]); " + "return 1; " +

"end;" +

/如果释放锁的线程和已存在锁的线程不是同⼀个线程，返回nul "if (redis.cal('hexists', KEYS[1], ARGV[3]) = 0) then " +

"return nil;" +

"end; " + /通过hincrby递减1的⽅式，释放⼀次锁 /若剩余次数⼤于0 ，则刷新过期时间

"local counter = redis.cal('hincrby', KEYS[1], ARGV[3], -1); " + "if (counter > 0) then " +

"redis.cal('pexpire', KEYS[1], ARGV[2]); " + "return 0; " +

/否则证明锁已经释放，删除key并发布锁释放的消息

"else " + "redis.cal('del', KEYS[1]); " + "redis.cal('publish', KEYS[2], ARGV[1]); " + "return 1; "+

"end; " + "return nil;",

Arays.<Object>asList(getName(), getChanelName(), LockPubSub.unlockMesage, internalLockLeaseTime, getLockName(threadId);

} 如上代码，就是释放锁的逻辑。同样的，它也是有三个判断：

如果锁已经不存在，通过publish发布锁释放的消息，解锁成功

如果解锁的线程和当前锁的线程不是同⼀个，解锁失败，抛出异常

通过hincrby递减1，先释放⼀次锁。若剩余次数还⼤于0，则证明当前锁是重⼊锁，刷新过期时间；若 剩余次数⼩于0，删除key并发布锁释放的消息，解锁成功

⾄此，Redison中的可重⼊锁的逻辑，就分析完了。但值得注意的是，上⾯的两种实现⽅式都是针对 单机Redis实例⽽进⾏的。如果我们有多个Redis实例，请参阅Redlock算法。该算法的具体内容，请参 考htp:/redis.cn/topics/distlock.html

作者：清幽之地 链接：htps:/ w.jianshu.com/p/47fd7f86c848 来源：简书 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

