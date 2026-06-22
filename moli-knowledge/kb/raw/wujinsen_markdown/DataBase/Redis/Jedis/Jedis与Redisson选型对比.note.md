1 概述

- 1.1. 主要内容 本⽂的主要内容为对⽐Redis的两个框架：Jedis与Redisson，分析各⾃的优势与缺点，为项⽬中 Java缓存⽅案中的Redis编程模型的选择提供参考。

- 2. Jedis与Redisson对⽐


- 2.1. 概况对⽐ Jedis是Redis的Java实现的客户端，其API提供了⽐较全⾯的Redis命令的⽀持；Redisson实现 了分布式和可扩展的Java数据结构，和Jedis相⽐，功能较为简单，不⽀持字符串操作，不⽀持排 序、事务、管道、分区等Redis特性。Redisson的宗旨是促进使⽤者对Redis的关注分离，从⽽让 使⽤者能够将精⼒更集中地放在处理业务逻辑上。

- 2.2. 编程模型 Jedis中的⽅法调⽤是⽐较底层的暴露的Redis的API，也即Jedis中的Java⽅法基本和Redis的API 保持着⼀致，了解Redis的API，也就能熟练的使⽤Jedis。⽽Redisson中的⽅法则是进⾏⽐较⾼ 的抽象，每个⽅法调⽤可能进⾏了⼀个或多个Redis⽅法调⽤。 如下分别为Jedis和Redisson操作的简单示例： Jedis设置key-value与set操作： Jedis jedis = …; jedis.set("key", "value"); List<String> values = jedis.mget("key", "key2", "key3"); Redisson操作map： Redisson redisson = … RMap map = redisson.getMap("my-map"); // implement java.util.Map map.put("key", "value"); map.containsKey("key"); map.get("key");

- 2.3. 可伸缩性 Jedis使⽤阻塞的I/O，且其⽅法调⽤都是同步的，程序流需要等到sockets处理完I/O才能执⾏， 不⽀持异步。Jedis客户端实例不是线程安全的，所以需要通过连接池来使⽤Jedis。 Redisson使⽤⾮阻塞的I/O和基于Netty框架的事件驱动的通信层，其⽅法调⽤是异步的。 Redisson的API是线程安全的，所以可以操作单个Redisson连接来完成各种操作。

- 2.4. 数据结构 Jedis仅⽀持基本的数据类型如：String、Hash、List、Set、Sorted Set。 Redisson不仅提供了⼀系列的分布式Java常⽤对象，基本可以与Java的基本数据结构通⽤，还提 供了许多分布式服务，其中包括（BitSet, Set, Multimap, SortedSet, Map, List, Queue, BlockingQueue, Deque, BlockingDeque, Semaphore, Lock, AtomicLong, CountDownLatch, Publish / Subscribe, Bloom filter, Remote service, Spring cache, Executor service, Live Object service, Scheduler service）。


- 在分布式开发中，Redisson可提供更便捷的⽅法。
- 2.5. 第三⽅框架整合


- 1 Redisson提供了和Spring框架的各项特性类似的，以Spring XML的命名空间的⽅式配置 RedissonClient实例和它所⽀持的所有对象和服务；

- 2 Redisson完整的实现了Spring框架⾥的缓存机制；

- 3 Redisson在Redis的基础上实现了Java缓存标准规范；

- 4 Redisson为Apache Tomcat集群提供了基于Redis的⾮黏性会话管理功能。该功能⽀持 Apache Tomcat的6、7和8版。

- 5 Redisson还提供了Spring Session会话管理器的实现。


外⾯的世界那么浮躁，我只想要⼀块键盘，安静下来，奏出精彩的代码篇章。

