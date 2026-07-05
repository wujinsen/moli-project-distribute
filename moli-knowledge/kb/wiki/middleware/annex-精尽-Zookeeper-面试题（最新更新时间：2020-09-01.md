---
title: 精尽 Zookeeper 面试题（最新更新时间：2020-09-01.note（原文插图 annex）
slug: annex-精尽-Zookeeper-面试题（最新更新时间：2020-09-01
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/面试笔试/框架/zookeeper/精尽 Zookeeper 面试题（最新更新时间：2020-09-01.note.md
related: [zookeeper-与协调服务]
created: 2026-07-05
updated: 2026-07-05
---

以下⾯试题，基于⽹络整理，和⾃⼰编辑。具体参考的⽂章，会在⽂末给出所有的链接。 如果胖友有⾃⼰的疑问，欢迎在星球提问，我们⼀起整理吊吊的 Zookeeper ⾯试题的⼤保健。 ⽽题⽬的难度，艿艿尽量按照从容易到困难的顺序，逐步下去。

Zookeeper 是什么？

ZooKeeper 是⼀个开放源码的分布式协调服务，它是集群的管理者，监视着集群中各个节点的状态根 据节点提交的反馈进⾏下⼀步合理操作。最终，将简单易⽤的接⼝和性能⾼效、功能稳定的系统提供 给⽤户。 分布式应⽤程序可以基于 Zookeeper 实现诸如数据发布/订阅、负载均衡、命名服务、分布式协调/通 知、集群管理、Master 选举、分布式锁和分布式队列等功能。 Zookeeper 具有如下特性：

顺序⼀致性(有序性)

从同⼀个客户端发起的事务请求，最终将会严格地按照其发起顺序被应⽤ 到 Zookeeper 中去。 有序性是 Zookeeper 中⾮常重要的⼀个特性。

# 所有的更新都是全局有序的，每个更新都有⼀个唯⼀的时间戳，这个 时间戳称为zxid(Zookeeper Transaction Id)。 ⽽读请求只会相对于更新有序，也就是读请求的返回结果中会带有这 个 Zookeeper 最新的 zxid 。

原⼦性

所有事务请求的处理结果在整个集群中所有机器上的应⽤情况是⼀致的， 即整个集群要么都成功应⽤了某个事务，要么都没有应⽤。

单⼀视图

⽆论客户端连接的是哪个 Zookeeper 服务器，其看到的服务端数据模型都 是⼀致的。

可靠性

⼀旦服务端成功地应⽤了⼀个事务，并完成对客户端的响应，那么该事务 所引起的服务端状态变更将会⼀直被保留，除⾮有另⼀个事务对其进⾏了 变更。

实时性

# Zookeeper 保证在⼀定的时间段内，客户端最终⼀定能够从服务端上读取到 最新的数据状态。

Zookeeper 对于读写请求有所不同：

客户端的读请求可以被集群中的任意⼀台机器处理，如果读请求在节点上注册了监听器，这个监听 器也是由所连接的 Zookeeper 机器来处理。

对于写请求，这些请求会同时发给其他 Zookeeper 机器并且达成⼀致后，请求才会返回成功。因 此，随着 Zookeeper 的集群机器增多，读请求的吞吐会提⾼但是写请求的吞吐会下降。

🦅 Chubby 是什么？和 Zookeeper 对⽐你怎么看？

Chubby 是 Google 的，完全实现 Paxos 算法，不开源。

Zookeeper 是 Chubby 的开源实现，使⽤ ZAB 协议(Paxos 算法的变种)。

🦅 Zookeeper 的 Java 客户端都有哪些？

Zookeeper ⾃带的 zkclient

Apache 开源的 Curator

# 实际项⽬中，采⽤ Curator 居多。因为，功能更加强⼤。

《ZK 客户端操作》

具体的使⽤，可以看看 ⽂章。 另外，Zookeeper 没有特别好⽤的 GUI ⼯具，有需要的胖友，可以看看 ，凑活能⽤。

ZooInspector

Zookeeper 的设计⽬标？

- 1、简单的数据结构，Zookeeper 使得分布式程序能够通过⼀个共享的树形结构的名字空间来进⾏ 相互协调，即 Zookeeper 服务器内存中的数据模型由⼀系列被称为 ZNode 的数据节点组成， Zookeeper 将全量的数据存储在内存中，以此来提⾼服务器吞吐、减少延迟的⽬的。

- 2、可以构建集群 Zookeeper 集群通常由⼀组机器构成，组成 Zookeeper 集群的⽽每台机器都会在 内存中维护当前服务器状态，并且每台机器之间都相互通信。

- 3、顺序访问，对于来⾃客户端的每个更新请求，Zookeeper 都会分配⼀个全局唯⼀的递增编号， 这个编号反映了所有事务操作的先后顺序。

- 4、⾼性能，Zookeeper 和 Redis ⼀样全量数据存储在内存中，100%读请求压测 QPS 12-13W 。


# 《ZooKeeper 的 ⼀个性能测试》

# 没具体测试过，⽐想象中的⾼。感兴趣的胖友，可以看看 。

Zookeeper 有哪些应⽤场景？

Zookeeper 的功能很强⼤，应⽤场景很多，结合我们实际⼯作中使⽤ Dubbo 框架的情况，Zookeeper 主要是做注册中⼼⽤。

基于 Dubbo 框架开发的提供者、消费者都向 Zookeeper 注册⾃⼰的 URL ，消费者还能拿到并订阅 提供者的注册 URL ，以便在后续程序的执⾏中去调⽤提供者。

⽽提供者发⽣了变动，也会通过 Zookeeper 向订阅的消费者发送通知。

当然，Zookeeper 能提供的不仅仅如此，再例如：

统⼀命名服务。

命名服务是指通过指定的名字来获取资源或服务的地址，利⽤zk创建⼀个 全局的路径，即时唯⼀的路径，这个路径就可以作为⼀个名字，指向集群 中机器或者提供服务的地址，⼜或者⼀个远程的对象等。

分布式锁服务。

这个⽐较好理解，Zookeeper 实现的分布式锁的可靠性会⽐ Redis 实现的分 布式锁⾼，当然相对来说，性能会低。

配置管理。

例如说， ，就实现了基于 Zookeeper 的 Spring Cloud Config 的实现，提供配置中⼼的服务。

# Spring Cloud Config Zookeeper

注册与发现。

是否有机器加⼊或退出 所有机器约定在⽗⽬录下创建临时⽬录节点，然后监听⽗⽬录节点下的⼦ 节点变化。⼀旦有机器挂掉，该机器与 ZooKeeper 的连接断开，其所创建 的临时⽬录节点也被删除，所有其他机器都收到通知：某个节点被删除 了。

Master 选举。

基于 Zookeeper 实现分布式协调，从⽽实现主从的选举。这个在 Kafka、 Elastic-Job 等等中间件，都有所使⽤到。

分布式锁。

有了 ZooKeeper 的⼀致性⽂件系统，锁的问题变得容易。锁服务可以分成 两类，⼀个是保持独占，另⼀个是控制时序。

# 1、保持独占，我们把 znode 看作是⼀把锁，通过 createZnode 的⽅式 来实现。所有客户端都去创建 /distribute_lock 节点，最终成功 创建的那个客户端也即拥有了这把锁。⽤完删除掉⾃⼰创建 的 /distribute_lock 节点就释放出锁。

2、控制时序，/distribute_lock 已经预先存在，所有客户端在 它下⾯创建临时顺序编号⽬录节点，和 Master ⼀样，编号最⼩的获 得锁，⽤完删除，依次⽅便。

最近艿艿画了⼀个 Curator 基于 ZooKeeper 实现分布式锁的流程图，胖友可 以点击 查看。

# 传送⻔

队列管理

两种类型的队列。

- 1、同步队列，当⼀个队列的成员都聚⻬时，这个队列才可⽤，否则 ⼀直等待。在约定的⽬录下创建临时⽬录节点，监听节点数⽬是否是 我们要求的数⽬。

- 2、队列按照 FIFO ⽅式进⾏⼊队和出队操作。和分布式锁服务中的 控制时序的场景基本原理⼀致，⼊列有编号，出列按编号。创建 PERSISTENT_SEQUENTIAL 节点，创建成功时 Watcher 通知等待的 队列，队列删除序列号最⼩的节点以消费。此场景下，znode ⽤于消 息存储，znode 存储的数据就是消息队列中的消息内容， SEQUENTIAL 序列号就是消息的编号，按序取出即可。由于创建的 节点是持久化的，所以不必担⼼队列消息丢失的问题。


当然，详细的可以看看 ⽂章。另外，该问对 Zookeeper 的“特点”介绍，也要 重点看看。 😈 上述的很多功能，在 Apache Curator 已经默认提供实现了，直接调⽤ API 即可使⽤。 🦅 作为服务注册中⼼，Eureka ⽐ Zookeeper 好在哪⾥？ 参⻅ ⽂章。 ⽐较重要的原因是，注册中⼼对可⽤性⽐⼀致性有更⾼的要求，也就是说，能够容忍在异常情况下， 读取到⼏分钟前的数据。

《Zookeeper 技术浅析》

《作为服务注册中⼼，Eureka ⽐ Zookeeper 好在哪⾥》

Zookeeper 提供了什么？

- 1、⽂件系统。

- 2、通知机制。


Zookeeper 的⽂件系统是什么？

Zookeeper 提供⼀个多层级的节点命名空间(节点称为 znode)。与⽂件系统不同的是，这些节点都可以 设置关联的数据，⽽⽂件系统中只有⽂件节点可以存放数据⽽⽬录节点不⾏。

Zookeeper 为了保证⾼吞吐和低延迟，在内存中维护了这个树状的⽬录结构，这种特性使得 Zookeeper 不能⽤于存放⼤量的数据，每个节点的存放数据上限为 1M 。 🦅 Zookeeper 有哪⼏种节点类型？

PERSISTENT 持久节点

创建之后⼀直存在，除⾮有删除操作，创建节点的客户端会话失效也不影 响此节点。

PERSISTENT_SEQUENTIAL 持久顺序节点

跟持久⼀样，就是⽗节点在创建下⼀级⼦节点的时候，记录每个⼦节点创 建的先后顺序，会给每个⼦节点名加上⼀个数字后缀。

EPHEMERAL 临时节点

创建客户端会话失效（注意是会话失效，不是连接断了），节点也就没 了。不能建⼦节点。

EPHEMERAL_SEQUENTIAL 临时顺序节点

基本特性同临时节点，增加了顺序属性，节点名后边会追加⼀个由⽗节点 维护的⾃增整型数字。

如下是艿艿整理的 Elastic-Job-Lite 使⽤ Zookeeper 作为存储的明细：

![image 1](assets/imageFile1.png)

Elastic-Job-Lite 详细

Zookeeper 的通知机制是什么？

Zookeeper 允许客户端向服务端的某个 znode 注册⼀个 Watcher 监听，当服务端的⼀些指定事件触发 了这个 Watcher ，服务端会向指定客户端发送⼀个事件通知来实现分布式的通知功能，然后客户端根 据 Watcher 通知状态和事件类型做出业务上的改变。 整个流程如下：

具体的过程，下⾯每个⼩问题，进⾏说明。

第⼀步，客户端注册 Watcher 。 第⼆步，服务端处理 Watcher 。

第三步，客户端回调 Watcher 。

Watcher 的特性总结：

- 1、⼀次性。

Apache Curator

- 2、客户端串⾏执⾏。

- 3、轻量级 Watch 机制。

Watcher 通知⾮常简单，只会告诉客户端发⽣了事件，⽽不会说明事 件的具体内容。 客户端向服务端注册 Watcher 的时候，并不会把客户端真实的 Watcher 对象实体传递到服务端，仅仅是在客户端请求中使⽤ boolean 类型属性进⾏了标记。

- 4、Watcher event 异步发送 Watcher 的通知事件从 Server 发送到Client 是异步的，这就存在⼀个 问题，不同的客户端和服务器之间通过Socket 进⾏通信，由于⽹络延迟或其他因素导致客户端在不 通的时刻监听到事件，由于 Zookeeper 本身提供了 ordering guarantee ，即客户端监听事件后，才 会感知它所监视 znode 发⽣了变化。所以我们使⽤ Zookeeper 不能期望能够监控到节点每次的变 化。Zookeeper 只能保证最终的⼀致性，⽽⽆法保证强⼀致性。

- 5、可以注册 Watcher 的操作：getData、exists、getChildren 。

- 6、可以触发 Watcher 的操作：create、delete、setData 。

- 7、当⼀个 Client 连接到⼀个新的服务器上时，watch 将会被以任意会话事件触发。当与⼀个服务 器失去连接的时候，是⽆法接收到 watch 的。⽽当 Client 重新连接时，如果需要的话，所有先前注 册过的watch ，都会被重新注册。通常这是完全透明的。只有在⼀个特殊情况下，watch 可能会丢 失：对于⼀个未创建的 znode 的 exists watch ，如果在客户端断开连接期间被创建了，并且随后在 客户端连接上之前⼜删除了，这种情况下，这个 watch 事件可能会被丢失。


⽆论是服务端还是客户端，⼀旦⼀个 Watcher 被触发， Zookeeper 都会将其 从相应的存储中移除。这样的设计有效的减轻了服务端的压⼒，不然对于 更新⾮常频繁的节点，服务端会不断的向客户端发送事件通知，⽆论对于 ⽹络还是服务端的压⼒都⾮常⼤。 😈 注意哟，这个特性可以变成⼀个⾯试题「Zookeeper 对节点的 watch 监 听通知是永久的吗？」。 如果我们使⽤ 作为操作 Zookeeper 的客户端，它可以帮我们 ⾃动透明的实现持续的 watch 操作，⾮常⽅便。

客户端 Watcher 回调的过程是⼀个串⾏同步的过程。

😈 看了这么多特性总结，最最最重要的是【⼀次性】。

艿艿：下⾯三个步骤，选择性了解即可。⾯试如果问到，就当倒霉。

🦅 第⼀步，客户端注册 Watcher 实现？

- 1、调⽤ getData、getChildren、exist 三个 API ，传⼊Watcher 对象。

- 2、标记请求 request ，封装 Watcher 到 WatchRegistration 。

- 3、封装成 Packe t对象，发服务端发送 request 。

- 4、收到服务端响应后，将 Watcher 注册到 ZKWatcherManager 中进⾏管理。

- 5、请求返回，完成注册。


🦅 第⼆步，服务端处理 Watcher 实现？

- 1、服务端接收 Watcher 并存储。

- 2、Watcher 触发。

封装 WatchedEvent ：

查询 Watcher ：

没找到 ：说明没有客户端在该数据节点上注册过 Watcher 。 找到 ：提取并从 WatchTable 和 Watch2Paths 中删除对应 Watcher (从这⾥可以看出 Watcher 在服务端是⼀次性的，触发⼀次就失效 了)。

- 3、调⽤ process ⽅法来触发 Watcher 。


接收到客户端请求，处理请求判断是否需要注册 Watcher ，需要的话将数 据节点的节点路径和 ServerCnxn(ServerCnxn 代表⼀个客户端和服务端的连 接，实现了 Watcher 的 process 接⼝，此时可以看成⼀个 Watcher 对象)存储 在 WatcherManager 的 WatchTable 和 Watch2Paths 中去。

以服务端接收到 setData 事务请求触发 NodeDataChanged 事件为例：

将通知状态（SyncConnected）、事件类型 （NodeDataChanged）以及节点路径封装成⼀个 WatchedEvent对象

从 WatchTable 中根据节点路径查找 Watcher 。

# 这⾥ process 主要就是通过 ServerCnxn 对应的 TCP 连接发送 Watcher 事件 通知。

🦅 第三步，客户端回调 Watcher 实现？ 客户端 SendThread 线程接收事件通知，交由 EventThread 线程回调Watcher 。 客户端的 Watcher 机制同样是⼀次性的，⼀旦被触发后，该 Watcher 就失效了。

Zookeeper 采⽤什么权限控制机制？

# 在⽹上看到⼀个「你们的 Zookeeper 的节点加密是⽤的什么⽅式？」问 题，应该也是问这个。

⽬前，在 Linux/Unix ⽂件系统中，使⽤ UGO(User/Group/Others) 权限模型，也是使⽤最⼴泛的权限 控制⽅式。是⼀种粗粒度的⽂件系统权限控制模式。

# ⼀般我们管理后台，采⽤的 RBAC 居多，和 UGO ⽐较类似，差别在于⼀ 般将权限分配给 Role ，⽽不是直接给 User 。

对于 Zookeeper ，它采⽤ ACL（Access Control List）访问控制列表。包括三个⽅⾯：

权限模式（Scheme）

IP ：从 IP 地址粒度进⾏权限控制 【常⽤】Digest ：最常⽤，⽤类似于 username:password 的权限 标识来进⾏权限配置，便于区分不同应⽤来进⾏权限控制。 World ：最开放的权限控制⽅式，是⼀种特殊的 digest 模式，只有⼀ 个权限标识 “world:anyone” 。 Super ：超级⽤户。

授权对象

授权对象指的是权限赋予的⽤户或⼀个指定实体，例如 IP 地址或是机器 等。

权限 Permission

# CREATE ：数据节点创建权限，允许授权对象在该 znode 下创建⼦节 点。 DELETE ：⼦节点删除权限，允许授权对象删除该数据节点的⼦节 点。

READ ：数据节点的读取权限，允许授权对象访问该数据节点并读取 其数据内容或⼦节点列表等。 WRITE ：数据节点更新权限，允许授权对象对该数据节点进⾏更新 操作。 ADMIN ：数据节点管理权限，允许授权对象对该数据节点进⾏ ACL 相关设置操作。

🦅 Chroot 特性是什么？ Zookeeper 3.2.0 版本后，添加了 Chroot 特性。该特性允许每个客户端为⾃⼰设置⼀个命名空间。如 果⼀个客户端设置了 Chroot ，那么该客户端对服务器的任何操作，都将会被限制在其⾃⼰的命名空间 下。 通过设置 Chroot ，能够将⼀个客户端应⽤于 Zookeeper 服务端的⼀颗⼦树相对应，在那些多个应⽤ 公⽤⼀个 Zookeeper 进群的场景下，对实现不同应⽤间的相互隔离⾮常有帮助。

# 艿艿：貌似实际还⽤的⽐较少。

Zookeeper 的会话管理是怎么样的？

ZooKeeper 的每个客户端都维护⼀组服务端信息，在创建连接时由应⽤指定，客户端随机选择⼀个服 务端进⾏连接，连接成功后，服务端为每个连接分配⼀个唯⼀标识。

客户端在创建连接时可以指定溢出时间，客户端会周期性的向服务端发送 PING 请求来保持连接。

# 如果客户端异常下线，或者⽹络问题，导致⼀段时间没⼼跳给 Zookeeper 服务端，则会被 Zookeeper 标记为下线。

当客户端检测到与服务端断开连接后，客户端将⾃动选择服务端列表中的另⼀个服务端进⾏重连。 客户端允许应⽤修改服务端列表，但修改可能导致客户端与服务端的重连。

详细的，推荐阅读如下两篇⽂章：

《ZooKeeper session 管理》

《ZooKeeper 技术内幕：会话》 更原理层⾯。

Zookeeper 的部署⽅式？

Zookeeper 有两种部署⽅式：

- 1、单机

- 2、集群


Zookeeper 集群，是⼀个由多个 Server 组成，⼀个 Leader，多个 Follower。 （这个不同于我们常⻅的 Master/Slave 模式）Leader 为客户端服务器提供 读写服务，除了 Leader 外其他的机器只能提供读服务。

每个 Server 保存⼀份数据副本全数据⼀致，分布式读 Follower ，写由 Leader 实施更新请求转发，由 Leader 实施更新请求顺序进⾏，来⾃同⼀个 Client 的更新请求按其发送顺序依次执⾏数据更新原⼦性，⼀次数据更新 要么成功，要么失败。 全局唯⼀数据视图，Client ⽆论连接到哪个 Server，数据视图都是⼀致的实 时性，在⼀定事件范围内，Client 能读到最新数据。

![image 2](assets/imageFile2.png)

Zookeeper 集群

⼀般来说，测试环境部署单机，⽽⽣产环境必须必须必须部署集群。 🦅 集群中的机器⻆⾊有哪些？ 集群中⼀共有三种⻆⾊：

- 1、Leader

事务请求的唯⼀调度和处理者，保证集群事务处理的顺序性。 集群内部各服务的调度者。

- 2、Follower

处理客户端的⾮事务请求，转发事务请求给 Leader 服务器。 参与事务请求 Proposal 的投票。 参与 Leader 选举投票。

- 3、Observer


3.3.0 版本以后引⼊的⼀个服务器⻆⾊，在不影响集群事务处理能⼒的基础 上提升集群的⾮事务处理能⼒。

# 处理客户端的⾮事务请求，转发事务请求给 Leader 服务器 不参与任何形式的投票。

# 如果 ZooKeeper 集群的读取负载很⾼，或者客户端多到跨机房，可以设置 ⼀些 Observer 服务器，以提⾼读取的吞吐量。Observer 和 Follower ⽐较相 似，只有⼀些⼩区别：

# ⾸先 Observer 不属于法定⼈数，即不参加选举也不响应提议，也不参 与写操作的“过半写成功”策略； 其次是 Observer 不需要将事务持久化到磁盘，⼀旦 Observer 被重 启，需要从 Leader 重新同步整个名字空间。

在⼀个集群中，最少需要 3 台。或者保证 2N + 1 台，即奇数。为什么保证奇数？主要是为了选举算 法。 🦅 集群如果有 3 台机器，挂掉 1 台集群还能⼯作吗？挂掉 2 台呢？ 记住⼀个原则：过半存活即可⽤。所以挂掉 1 台可以继续⼯作，挂掉 2 台不可以⼯作。 🦅 集群⽀持动态添加机器吗？ 在 3.5 版本开始，⽀持动态扩容。 ⽽在 3.5 版本之前，Zookeeper 在这⽅⾯不太好。所以需要如下两种⽅式：

全部重启：关闭所有 Zookeeper 服务，修改配置之后启动。不影响之前客户端的会话。

逐个重启：顾名思义。这是⽐较常⽤的⽅式。

🦅 Zookeeper 下 Server ⼯作状态？ 服务器具有四种状态，分别是：

LOOKING 寻找 Leader 状态

当服务器处于该状态时，它会认为当前集群中没有 Leader ，因此需要进⼊ Leader 选举状态。

FOLLOWING 跟随者状态

表明当前服务器⻆⾊是 Follower 。

LEADING 领导者状态

表明当前服务器⻆⾊是 Leader 。

OBSERVING 观察者状态

表明当前服务器⻆⾊是 Observer 。

ZooKeeper 的⼯作原理？

ZooKeeper 的核⼼是原⼦⼴播，这个机制保证了各个 Server 之间的同步。实现这个机制的协议叫 做 Zab 协议。Zab 协议有两种模式，它们分别是恢复模式（选主）和⼴播模式（同步）：

选主：当服务启动或者 Leader 崩溃后，Zab 就进⼊了恢复模式，当新的 Leader 被选举出来，且⼤ 多数 Server 完成了和 Leader 的状态同步以后，恢复模式就结束了。

更加详细的描述。 当整个 Zookeeper 集群刚刚启动，或者 Leader 服务器宕机、重启或者⽹络 故障导致不存在过半的服务器与 Leader服务器保持正常通信时，所有进程 （服务器）进⼊崩溃恢复模式。

⾸先，选举产⽣新的Leader服务器。 然后，集群中 Follower 服务器开始与新的 Leader 服务器进⾏数据同 步。 当集群中超过半数机器与该Leader服务器完成数据同步之后，退出恢 复模式进⼊消息⼴播模式，

同步：状态同步保证了 Leader 和 Server 具有相同的系统状态。

更加详细的描述。 Leader 服务器开始接收客户端的事务请求，⽣成事务提案来进⾏事务请求 处理。

🦅 ZooKeeper 是如何保证事务的顺序⼀致性的？ ZooKeeper 采⽤了递增的事务 id 来识别，所有的 proposal（提议）都在被提出的时候加上了 zxid 。 zxid 实际上是⼀个 64 位数字。

⾼ 32 位是 epoch ⽤来标识 Leader 是否发⽣了改变，如果有新的 Leader 产⽣出来，epoch会⾃ 增。

低 32 位⽤来递增计数。

当新产⽣的 peoposal 的时候，会依据数据库的两阶段过程，⾸先会向其他的 Server 发出事务执⾏请 求，如果超过半数的机器都能执⾏并且能够成功，那么就会开始执⾏。 🦅 ZooKeeper 集群中个服务器之间是怎样通信的？ Leader 服务器会和每⼀个 Follower/Observer 服务器都建⽴ TCP 连接，同时为每个 Follower/Observer 都创建⼀个叫做 LearnerHandler 的实体。

LearnerHandler 主要负责 Leader 和 Follower/Observer 之间的⽹络通讯，包括数据同步，请求转 发和 Proposal 提议的投票等。

Leader 服务器保存了所有 Follower/Observer 的 LearnerHandler 。

🦅 ZAB 和 Paxos 算法的联系与区别？ Paxos 算法是分布式选举算法，Zookeeper 使⽤的 ZAB 协议（Zookeeper 原⼦⼴播）。 ⼆者有相同的地⽅：

都有⼀个 Leader，⽤来协调 N 个 Follower 的运⾏

Leader 要等待超半数的 Follower做 出正确反馈之后才进⾏提案。

⼆者都有⼀个值来代表 Leader 的周期。ZAB 协议中，每个 Proposal 中都包含⼀个 epoch 值来代 表当前的Leader周期，Paxos中名字为 Ballot 。

不同的地⽅在于：

ZAB ⽤来构建⾼可⽤的分布式数据主备系统（Zookeeper），Paxos 是⽤来构建分布式⼀致性状态 机系统。

Paxos 算法、ZAB 协议要想讲清楚可不是⼀时半会的事⼉，⾃ 1990 年莱斯利·兰伯特提出 Paxos 算法 以来，因为晦涩难懂并没有受到重视。后续⼏年，兰伯特通过好⼏篇论⽂对其进⾏更进⼀步地解释， 也直到 06 年⾕歌发表了三篇论⽂，选择 Paxos 作为 Chubby cell 的⼀致性算法，Paxo s才真正流⾏起 来。 对于普通开发者来说，尤其是学习使⽤ Zookeeper 的开发者明确⼀点就好：分布式 Zookeeper 选举 Leader 服务器的算法，与 Paxos 有很深的关系。

Zookeeper 的选举过程？

当 Leader 崩溃，或者 Leader 失去⼤多数的 Follower，这时 Zookeeper 进⼊恢复模式，恢复模式需要 重新选举出⼀个新的 Leader，让所有的 Server 都恢复到⼀个正确的状态。 Zookeeper 的选举算法有两种：⼀种是基于 basic paxos 实现的，另外⼀种是基于 fast paxos 算法实 现的。系统默认的选举算法为 fast paxos 。

《【分布式】Zookeeper的Leader选举》 《Z ookeeper 源码分析 —— Zookeeper Leader 选举算法》

# 相对详细的，胖友可以看看 和 。

不同阶段的选举流程

服务器启动时期的 Leader 选举。 服务器运⾏时期的 Leader 选举。

三种选举算法 LeaderElection ：使⽤ basic paxos 算法。 FastLeaderElection ：使⽤ fast paxos 算法。 AuthFastLeaderElection ：在 FastLeaderElection 的基础上，增加认 证。 最终在 Zookeeper 3.4.0 版本之后，只保留 FastLeaderElection 版本。

😈 看下⾯的原理描述，还是有点懵逼。等后⾯艿艿⾃⼰去撸下源码，可能会清晰⼀些。 🦅 Zookeeper 选主流程(basic paxos)？

选择性了解。

1、选举线程由当前 Server 发起选举的线程担任，其主要功能是对投票结果进⾏统计，并选出推荐 的 Server 。

- 2、选举线程⾸先向所有 Server 发起⼀次询问(包括⾃⼰)。

- 3、选举线程收到回复后，验证是否是⾃⼰发起的询问(验证 zxid 是否⼀致)，然后获取对⽅的 id(myid)，并存储到当前询问对象列表中，最后获取对⽅提议的 Leader相关信息(id，zxid)，并将这 些信息存储到当次选举的投票记录表中。

- 4、收到所有 Server 回复以后，就计算出 zxid 最⼤的那个 Server ，并将这个 Server 相关信息设置 成下⼀次要投票的 Server 。

- 5、线程将当前 zxid 最⼤的 Server 设置为当前 Server 要推荐的 Leader ，如果此时获胜的 Server 获得 n/2+1 的 Server 票数，设置当前推荐的 Leader 为获胜的 Server ，将根据获胜的 Server 相 关信息设置⾃⼰的状态，否则，继续这个过程，直到 Leader 被选举出来。


通过流程分析我们可以得出：要使 Leader 获得多数 Server 的⽀持，则 Server 总数必须是奇 数 2n+1 ，且存活的 Server 的数⽬不得少于n+1 。每个 Server 启动后都会重复以上流程。 在恢复模式下，如果是刚从崩溃状态恢复的或者刚启动的 Server 还会从磁盘快照中恢复数据和会话信 息，Zookeeper 会记录事务⽇志并定期进⾏快照，⽅便在恢复时进⾏状态恢复。

![image 3](assets/imageFile3.png)

流程 🦅 Zookeeper 选主流程(fast paxos)？

# 《Zookeeper 源码分析 —— Zookeeper Leader 选举算 法》

# 重点了解。这块在 写的⽐较详细。

由于 LeaderElection 收敛速度较慢，所以 Zookeeper 引⼊了 FastLeaderElection 选举算法， FastLeaderElection 也成了Zookeeper默认的Leader选举算法。

FastLeaderElection 是标准的 Fast Paxos 的实现。它⾸先向所有 Server 提议⾃⼰要成为 Leader ，当 其它 Server 收到提议以后，解决 epoch 和 zxid 的冲突，并接受对⽅的提议，然后向对⽅发送接受提 议完成的消息。重复这个流程，最后⼀定能选举出Leader。 FastLeaderElection 算法通过异步的通信⽅式来收集其它节点的选票，同时在分析选票时⼜根据投票 者的当前状态来作不同的处理，以加快 Leader 的选举进程。

![image 4](assets/imageFile4.png)

流程 🦅 为什么 Zookeeper 集群推荐节点数是单数？ 在统计投票时，有个过半的概念，⼤于集群机器数量的⼀半，即⼤于或等于(n/2+1)。那么我们来看看 如下的统计：

<table>
  <tr>
    <th>集群数量</th>
    <th>⾄少正常运⾏数量</th>
    <th>允许挂掉的数量</th>
  </tr>
  <tr>
    <td>2</td>
    <td>2 的半数为 1，半数以上最少为</td>
    <td>0</td>
  </tr>
  <tr>
    <td>3</td>
    <td>2<br>3 的半数为 1.5，半数以上最少<br></td>
    <td>1</td>
  </tr>
  <tr>
    <td>4</td>
    <td>为 2 4 的半数为 2，半数以上最少为</td>
    <td>1</td>
  </tr>
  <tr>
    <td>5</td>
    <td>3 5 的半数为 2.5，半数以上最少</td>
    <td>2</td>
  </tr>
  <tr>
    <td>6</td>
    <td>为 3 6 的半数为 3，半数以上最少为</td>
    <td>2</td>
  </tr>
</table>


4

通过以上可以发现：

3 台服务器和 4 台服务器都最多允许 1 台服务器挂掉，5 台服务器和 6 台服务器都最多允许 2 台服 务器挂掉，明显 4 台服务器成本⾼于 3 台服务器成本，6 台服务器成本⾼于 5 服务器成本。

这是由于半数以上投票通过决定的。所以，Zookeeper 集群推荐节点数是单数。

简单的来说，节省资源！ 另外，因为 Zookeeper 使⽤⼀致性协议，过多的节点，反倒会降低性能。😈 🦅 Zookeeper 是否需存在脑裂？ 按道理说，Zookeeper 选举不会存在脑裂问题，因为需要 n / 2 + 1 投票通过，才能执⾏对应的写 操作。但是听朋友说，实际场景下，貌似发⽣过脑裂问题。关于这块，艿艿⼼⾥也不太有底，欢迎在 星球⼀起讨论。

《Zookeeper 已经分布式环境中的假死脑裂》

认为存在脑裂问题，以及提供怎么解决。

《zookeeper（⼆）常⻅问题汇总》

认为不会存在脑裂问题。

🦅 机器中为什么会有 Leader？ 在分布式环境中，有些业务逻辑只需要集群中的某⼀台机器进⾏执⾏，其他的机器可以共享这个结 果，这样可以⼤⼤减少重复计算，提⾼性能，于是就需要进⾏ Leader 选举。

Zookeeper 的同步流程？

选完 Leader 以后，Zookeeper 就进⼊状态同步过程。

- 1、Leader 等待 Server 连接。

- 2、Follower 连接 Leader ，将最⼤的 zxid 发送给 Leader 。

- 3、Leader 根据 Follower 的 zxid 确定同步点。

- 4、完成同步后通知 Follower 已经成为 update 状态。

- 5、Follower 收到 update 消息后，⼜可以重新接受 Client 的请求进⾏服务了。 《Zookeeper Leader 和 Learner


当然，同步流程并不是像上述描述的这么简单，具体的，还是得看看 。

的数据同步》

666. 彩蛋

估计⼤多数胖友，学习 Zookeeper 的过程，是因为使⽤ Dubbo 时，需要使⽤到 Zookeeper 作为注册 中⼼，然后快速搭建了下。然后，断断续续看了下 Zookeeper 的⽂章。🙂 就当是复习。⾯对的时候， ⽐较重点的⼏个问题是：

Zookeeper 的选举过程？

Zookeeper 如何提供分布式锁？

Zookeeper 的⼀些应⽤场景？
