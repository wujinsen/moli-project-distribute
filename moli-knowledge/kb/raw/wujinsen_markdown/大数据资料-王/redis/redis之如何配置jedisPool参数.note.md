# 如何配置Pol的参数

JedisPol的配置参数很⼤程度上依赖于实际应⽤需求、软硬件能⼒，JedisPol的配置参数⼤部分是由 JedisPolConfig的对应项来赋值的。

maxActive：控制⼀个pol可分配多少个jedis实例，通过pol.getResource()来获取；如果赋值为-1， 则表示不限制；如果pol已经分配了maxActive

个jedis实例，则此时pol的状态就成exhausted了，在JedisPolConfig maxIdle：控制⼀个pol最多有多少个状态为idle的jedis实例；

whenExhaustedAction：表示当pol中的jedis实例都被alocated完时，pol要采取的操作； 默认有三种WHEN_EXHAUSTED_FAIL（表示⽆jedis实例时，直接抛出 NoSuchElementException）、WHEN_EXHAUSTED_BLOCK（则表示阻塞住，或者达到 maxWait时抛出JedisConectionException）、WHEN_EXHAUSTED_GROW（则表示新建⼀ 个jedis实例，也就说设置的maxActive⽆⽤）；

maxWait：表示当borow⼀个jedis实例时，最⼤的等待时间，如果超过等待时间，则直接抛出 JedisConectionException； testOnBorow：在borow⼀个jedis实例时，是否提前进⾏alidate操作；如果为true，则得到的jedis实 例均是可⽤的； testOnReturn：在return给pol时，是否提前进⾏validate操作； testWhileIdle：如果为true，表示有⼀个idle object evitor线程对idle object进⾏扫描，如果validate失 败，此object会被从pol中drop掉；这⼀项只有

在timeBetwenEvictionRunsMilis⼤于0时才有意义； timeBetwenEvictionRunsMilis：表示idle object evitor两次扫描之间要sl ep的毫秒数； numTestsPerEvictionRun：表示idle object evitor每次扫描的最多的对象数； minEvictableIdleTimeMilis：表示⼀个对象⾄少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；这⼀项只有在

timeBetwenEvictionRunsMilis⼤于0时才有意义； softMinEvictableIdleTimeMilis：在minEvictableIdleTimeMilis基础上，加⼊了⾄少minIdle个对象已经 在pol⾥⾯了。如果为-1，evicted不会根据idle

time驱逐任何对象。如果minEvictableIdleTimeMilis>0，则此项设置⽆意义，且 只有在timeBetwenEvictionRunsMilis⼤于0时才有意义；

lifo：borowObject返回对象时，是采⽤DEFAULT_LIFO（last in first out，即类似cache的最频繁使⽤ 队列），如果为False，则表示FIFO队列；

其中JedisPolConfig对⼀些参数的默认设置如下： testWhileIdle=true

## minEvictableIdleTimeMils=6 0 timeBetwenEvictionRunsMilis=3 0 numTestsPerEvictionRun=-1

