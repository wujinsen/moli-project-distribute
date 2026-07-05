---
title: Codis作者黄东旭细说分布式Redis架构设计和踩过的那些坑们.note（原文插图 annex）
slug: annex-Codis作者黄东旭细说分布式Redis架构设计和踩过的那些坑们
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/DataBase/Redis/Codis作者黄东旭细说分布式Redis架构设计和踩过的那些坑们.note.md
related: [redis-面试题]
created: 2026-07-05
updated: 2026-07-05
---

本次分享的内容主要包括五个⼤部分：

Redis、RedisCluster和Codis; 我们更爱⼀致性; Codis在⽣产环境中的使⽤的经验和坑们; 对于分布式数据库和分布式架构的⼀些看法; Q & A环节。

Codis是⼀个分布式Redis解决⽅案，与官⽅的纯P2P的模式不同，Codis采⽤的是Proxy-based的⽅ 案。今天我们介绍⼀下Codis及下⼀个⼤版本RebornDB的设计，同时会介绍⼀些Codis在实际应⽤场景 中的tips。最后抛砖引⽟，会介绍⼀下我对分布式存储的⼀些观点和看法，望各位⾸席们雅正。 ⼀、 Redis，RedisCluster和Codis

Redis：想必⼤家的架构中，Redis已经是⼀个必不可少的部件，丰富的数据结构和超⾼的性能以及简 单的协议，让Redis能够很好的作为数据库的上游缓存层。但是我们会⽐较担⼼Redis的单点问题，单 点Redis容量⼤⼩总受限于内存，在业务对性能要求⽐较⾼的情况下，理想情况下我们希望所有的数据 都能在内存⾥⾯，不要打到数据库上，所以很⾃然的就会寻求其他⽅案。 ⽐如， SD将内存换成了磁 盘，以换取更⼤的容量。更⾃然的想法是将Redis变成⼀个可以⽔平扩展的分布式缓存服务，在Codis 之前，业界只有Twemproxy，但是Twemproxy本身是⼀个静态的分布式Redis⽅案，进⾏扩容/缩容时 候对运维要求⾮常⾼，⽽且很难做到平滑的扩缩容。Codis的⽬标其实就是尽量兼容Twemproxy的基础 上，加上数据迁移的功能以实现扩容和缩容，最终替换Twemproxy。从豌⾖荚最后上线的结果来看， 最后完全替换了Twem，⼤概2T左右的内存集群。 Redis Cluster：与Codis同期发布正式版的官⽅cluster，我认为有优点也有缺点，作为架构师，我并 不会在⽣产环境中使⽤，原因有两个:

cluster的数据存储模块和分布式的逻辑模块是耦合在⼀起的，这个带来的好处是部署异常简单，alin-the-box，没有像Codis那么多概念，组件和依赖。但是带来的缺点是，你很难对业务进⾏⽆痛的 升级。⽐如哪天Redis cluster的分布式逻辑出现了⽐较严重的bug，你该如何升级?除了滚动重启整 个集群，没什么好办法。这个⽐较伤运维。 对协议进⾏了较⼤的修改，对客户端不太友好，⽬前很多客户端已经成为事实标准，⽽且很多程序 已经写好了，让业务⽅去更换Redisclient，是不太现实的，⽽且⽬前很难说有哪个Rediscluster客户 端经过了⼤规模⽣产环境的验证，从HunanTV开源的Rediscluster proxy上可以看得出这个影响还是 蛮⼤的，否则就会⽀持使⽤cluster的client了。

Codis：和Redis cluster不同的是，Codis采⽤⼀层⽆状态的proxy层，将分布式逻辑写在proxy上，底 层的存储引擎还是Redis本身（尽管基于Redis2.8.13上做了⼀些⼩patch），数据的分布状态存储于 zokeper(etcd)中，底层的数据存储变成了可插拔的部件。这个事情的好处其实不⽤多说，就是各个 部件是可以动态⽔平扩展的，尤其⽆状态的proxy对于动态的负载均衡，还是意义很⼤的，⽽且还可以 做⼀些有意思的事情，⽐如发现⼀些slot的数据⽐较冷，可以专⻔⽤⼀个⽀持持久化存储的server group来负责这部分slot，以节省内存，当这部分数据变热起来时，可以再动态的迁移到内存的server group上，⼀切对业务透明。⽐较有意思的是，在Twiter内部弃⽤Twmeproxy后，t家⾃⼰开发了⼀个 新的分布式Redis解决⽅案，仍然⾛的是proxy-based路线。不过没有开源出来。可插拔存储引擎这个 事情也是Codis的下⼀代产品RebornDB在做的⼀件事情。btw，RebornDB和它的持久化引擎都是完全 开源的，⻅htps:/github.com/reborndb/reborn和htps:/github.com/reborndb/qdb。当然这样的设 计的坏处是，经过了proxy，多了⼀次⽹络交互，看上去性能下降了⼀些，但是记住，我们的proxy是 可以动态扩展的，整个服务的QPS并不由单个proxy的性能决定（所以⽣产环境中我建议使⽤LVS/HA Proxy或者Jodis），每个proxy其实都是⼀样的。

![image 1](assets/imageFile1.png)

⼆、我们更爱⼀致性

很多朋友问我，为什么不⽀持读写分离，其实这个事情的原因很简单，因为我们当时的业务场景不能 容忍数据不⼀致，由于Redis本身的replication模型是主从异步复制，在master上写成功后，在slave上 是否能读到这个数据是没有保证的，⽽让业务⽅处理⼀致性的问题还是蛮麻烦的。⽽且Redis单点的性 能还是蛮⾼的，不像mysql之类的真正的数据库，没有必要为了提升⼀点点读QPS⽽让业务⽅困惑。这 和数据库的⻆⾊不太⼀样。所以，你可能看出来了，其实Codis的HA，并不能保证数据完全不丢失， 因为是异步复制，所以master挂掉后，如果有没有同步到slave上的数据，此时将slave提升成master 后，刚刚写⼊的还没来得及同步的数据就会丢失。不过在RebornDB中我们会尝试对持久化存储引擎 （qdb）可能会⽀持同步复制(syncreplication)，让⼀些对数据⼀致性和安全性有更强要求的服务可以 使⽤。 说到⼀致性，这也是Codis⽀持的MGET/MSET⽆法保证原本单点时的原⼦语义的原因。 因为MSET所 参与的key可能分不在不同的机器上，如果需要保证原来的语义，也就是要么⼀起成功，要么⼀起失 败，这样就是⼀个分布式事务的问题，对于Redis来说，并没有WAL或者回滚这么⼀说，所以即使是⼀ 个最简单的⼆阶段提交的策略都很难实现，⽽且即使实现了，性能也没有保证。所以在Codis中使⽤ MSET/MGET其实和你本地开个多线程SET/GET效果⼀样，只不过是由服务端打包返回罢了，我们加上 这个命令的⽀持只是为了更好的⽀持以前⽤Twemproxy的业务。

在实际场景中，很多朋友使⽤了lua脚本以扩展Redis的功能，其实Codis这边是⽀持的，但记住， Codis在涉及这种场景的时候，仅仅是转发⽽已，它并不保证你的脚本操作的数据是否在正确的节点 上。⽐如，你的脚本⾥涉及操作多个key，Codis能做的就是将这个脚本分配到参数列表中的第⼀个key 的机器上执⾏。所以这种场景下，你需要⾃⼰保证你的脚本所⽤到的key分布在同⼀个机器上，这⾥可 以采⽤hashtag的⽅式。 ⽐如你有⼀个脚本是操作某个⽤户的多个信息，如uid1age，uid1sex，uid1name形如此类的key，如果 你不⽤hashtag的话，这些key可能会分散在不同的机器上，如果使⽤了hashtag(⽤花括号扩住计算 hash的区域)：{uid1}age，{uid1}sex，{uid1}name，这样就保证这些key分布在同⼀个机器上。这个是 twemproxy引⼊的⼀个语法，我们这边也⽀持了。 在开源Codis后，我们收到了很多社区的反馈，⼤多数的意⻅是集中在Zokeper的依赖，Redis的修 改，还有为啥需要Proxy上⾯，我们也在思考，这⼏个东⻄是不是必须的。当然这⼏个部件带来的好处 毋庸置疑，上⾯也阐述过了，但是有没有办法能做得更漂亮。于是，我们在下⼀阶段会再往前⾛⼀ 步，实现以下⼏个设计：

使⽤proxy内置的Raft来代替外部的Zokeper，zk对于我们来说，其实只是⼀个强⼀致性存储⽽ 已，我们其实可以使⽤Raft来做到同样的事情。将raft嵌⼊proxy，来同步路由信息。达到减少依赖 的效果。 抽象存储引擎层，由proxy或者第三⽅的agent来负责启动和管理存储引擎的⽣命周期。具体来说， 就是现在codis还需要⼿动的去部署底层的Redis或者qdb，⾃⼰配置主从关系什么的，但是未来我 们会把这个事情交给⼀个⾃动化的agent或者甚⾄在proxy内部集成存储引擎。这样的好处是我们可 以最⼤程度上的减⼩Proxy转发的损耗（⽐如proxy会在本地启动Redis instance）和⼈⼯误操作， 提升了整个系统的⾃动化程度。 还有replication based migration。众所周知，现在Codis的数据迁移⽅式是通过修改底层Redis，加 ⼊单key的原⼦迁移命令实现的。这样的好处是实现简单、迁移过程对业务⽆感知。但是坏处也是 很明显，⾸先就是速度⽐较慢，⽽且对Redis有侵⼊性，还有维护slot信息给Redis带来额外的内存 开销。⼤概对于⼩key-value为主业务和原⽣Redis是1:1.5的⽐例，所以还是⽐较费内存的。

在RebornDB中我们会尝试提供基于复制的迁移⽅式，也就是开始迁移时，记录某slot的操作，然后在 后台开始同步到slave，当slave同步完后，开始将记录的操作回放，回放差不多后，将master的写⼊停 ⽌，追平后修改路由表，将需要迁移的slot切换成新的master，主从（半）同步复制，这个之前提到 过。 三、Codis在⽣产环境中的使⽤的经验和坑们

来说⼀些 tips，作为开发⼯程师，⼀线的操作经验肯定没有运维的同学多，⼤家⼀会可以⼀起再深度 讨论。 关于多产品线部署：很多朋友问我们如果有多个项⽬时，codis如何部署⽐较好，我们当时在豌⾖荚的 时候，⼀个产品线会部署⼀整套codis，但是zk共⽤⼀个，不同的codis集群拥有不同的product name 来区分，codis本身的设计没有命名空间那么⼀说，⼀个codis只能对应⼀个product name。不同 product name的codis集群在同⼀个zk上不会相互⼲扰。

关于zk：由于Codis是⼀个强依赖的zk的项⽬，⽽且在proxy和zk的连接发⽣抖动造成sesionexpired 的时候，proxy是不能对外提供服务的，所以尽量保证proxy和zk部署在同⼀个机房。⽣产环境中zk⼀ 定要是>=3台的奇数台机器，建议5台物理机。 关于HA：这⾥的HA分成两部分，⼀个是proxy层的HA，还有底层Redis的HA。先说proxy层的HA。之 前提到过proxy本身是⽆状态的，所以proxy本身的HA是⽐较好做的，因为连接到任何⼀个活着的 proxy上都是⼀样的，在⽣产环境中，我们使⽤的是jodis，这个是我们开发的⼀个jedis连接池，很简 单，就是监听zk上⾯的存活proxy列表，挨个返回jedis对象，达到负载均衡和HA的效果。也有朋友在 ⽣产环境中使⽤LVS和HA Proxy来做负载均衡，这也是可以的。 Redis本身的HA，这⾥的Redis指的是 codis底层的各个server group的master，在⼀开始的时候codis本来就没有将这部分的HA设计进去， 因为Redis在挂掉后，如果直接将slave提升上来的话，可能会造成数据不⼀致的情况，因为有新的修改 可能在master中还没有同步到slave上，这种情况下需要管理员⼿动的操作修复数据。后来我们发现这 个需求确实⽐较多的朋友反映，于是我们开发了⼀个简单的ha⼯具：codis-ha，⽤于监控各个server group的master的存活情况，如果某个master挂掉了，会直接提升该group的⼀个slave成为新的 master。 项⽬的地址是：htps:/github.com/ngaut/codis-ha。 关于dashboard：dashboard在codis中是⼀个很重要的⻆⾊，所有的集群信息变更操作都是通过 dashboard发起的（这个设计有点像docker），dashboard对外暴露了⼀系列RESTfulAPI接⼝，不管 是web管理⼯具，还是命令⾏⼯具都是通过访问这些htpapi来进⾏操作的，所以请保证dashboard和 其他各个组件的⽹络连通性。⽐如，经常发现有⽤户的dashboard中集群的ops为0，就是因为 dashboard⽆法连接到proxy的机器的缘故。 关于go环境：在⽣产环境中尽量使⽤go1.3.x的版本，go的1.4的性能很差，更像是⼀个中间版本，还没 有达到production ready的状态就发布了。很多朋友对go的gc颇有微词，这⾥我们不讨论哲学问题， 选择go是多⽅⾯因素权衡后的结果，⽽且codis是⼀个中间件类型的产品，并不会有太多⼩对象常驻内 存，所以对于gc来说基本毫⽆压⼒，所以不⽤考虑gc的问题。 关于队列的设计：其实简单来说，就是「不要把鸡蛋放在⼀个篮⼦」的道理，尽量不要把数据都往⼀ 个key⾥放，因为codis是⼀个分布式的集群，如果你永远只操作⼀个key，就相当于退化成单个Redis 实例了。很多朋友将Redis⽤来做队列，但是Codis并没有提供BLPOP/BLPUSH的接⼝，这没问题，可 以将列表在逻辑上拆成多个LIST的key，在业务端通过定时轮询来实现（除⾮你的队列需要严格的时序 要求），这样就可以让不同的Redis来分担这个同⼀个列表的访问压⼒。⽽且单key过⼤可能会造成迁 移时的阻塞，由于Redis是⼀个单线程的程序，所以迁移的时候会阻塞正常的访问。 关于主从和bgsave：codis本身并不负责维护Redis的主从关系，在codis⾥⾯的master和slave只是概 念上的：proxy会将请求打到「master」上，master挂了codis-ha会将某⼀个「slave」提升成 master。⽽真正的主从复制，需要在启动底层的Redis时⼿动的配置。在⽣产环境中，我建议master的 机器不要开bgsave，也不要轻易的执⾏save命令，数据的备份尽量放在slave上操作。 关于跨机房/多活：想都别想。。。codis没有多副本的概念，⽽且codis多⽤于缓存的业务场景，业务 的压⼒是直接打到缓存上的，在这层做跨机房架构的话，性能和⼀致性是很难得到保证的

关于proxy的部署：其实可以将proxy部署在client很近的地⽅，⽐如同⼀个物理机上，这样有利于减少 延迟，但是需要注意的是，⽬前jodis并不会根据proxy的位置来选择位置最佳的实例，需要修改。 四、对于分布式数据库和分布式架构的⼀些看法（one more Thing）

Codis相关的内容告⼀段落。接下来我想聊聊我对于分布式数据库和分布式架构的⼀些看法。 架构师们 是如此贪⼼，有单点就⼀定要变成分布式，同时还希望尽可能的透明:P。就MySQL来看，从最早的单 点到主从读写分离，再到后来阿⾥的类似Cobar和TDL，分布式和可扩展性是达到了，但是牺牲了事 务⽀持，于是有了后来的OceanBase。Redis从单点到Twemproxy，再到Codis，再到Reborn。到最后 的存储早已和最初的⾯⽬全⾮，但协议和接⼝永存，⽐如SQL和Redis Protocol。 NoSQL来了⼀茬⼜⼀茬，从HBase到Casandra到MongoDB，解决的是数据的扩展性问题，通过裁剪 业务的存储和查询的模型来在CAP上平衡。但是⼏乎还是都丢掉了跨⾏事务（插⼀句，⼩⽶上在 HBase上加⼊了跨⾏事务，不错的⼯作）。 我认为，抛开底层存储的细节，对于业务来说，KV，SQL查询（关系型数据库⽀持）和事务，可以说 是构成业务系统的存储原语。为什么memcached/Redis+mysql的组合如此的受欢迎，正是因为这个组 合，⼏个原语都能⽤上，对于业务来说，可以很⽅便的实现各种业务的存储需求，能轻易的写出「正 确」的程序。但是，现在的问题是数据⼤到⼀定程度上时，从单机向分布式进化的过程中，最难搞定 的就是事务，SQL⽀持什么的还可以通过各种mysqlproxy搞定，KV就不⽤说了，天⽣对分布式友好。 于是这样，我们就默认进⼊了⼀个没有（跨⾏）事务⽀持的世界⾥，很多业务场景我们只能牺牲业务 的正确性来在实现的复杂度上平衡。⽐如⼀个很简单的需求：微博关注数的变化，最直⽩，最正常的 写法应该是，将被关注者的被关注数的修改和关注者的关注数修改放到同⼀个事务⾥，⼀起提交，要 么⼀起成功，要么⼀起失败。但是现在为了考虑性能，为了考虑实现复杂度，⼀般来说的做法可能是 队列辅助异步的修改，或者通过cache先暂存等等⽅式绕开事务。 但是在⼀些需要强事务⽀持的场景就没有那么好绕过去了（⽬前我们只讨论开源的架构⽅案），⽐如 ⽀付/积分变更业务，常⻅的搞法是关键路径根据⽤户特征sharding到单点MySQL，或者MySQLXA， 但是性能下降得太厉害。 后来Gogle在他们的⼴告业务中遇到这个问题，既需要⾼性能，⼜需要分布式事务，还必须保证⼀致 性:)，Gogle在此之前是通过⼀个⼤规模的MySQL集群通过sharding苦苦⽀撑，这个架构的可运维/扩 展性实在太差。这要是在⼀般公司，估计也就忍了，但是Gogle可不是⼀般公司，⽤原⼦钟搞定 Spaner，然后再Spaner上构建了SQL查询层F1。我在第⼀次看到这个系统的时候，感觉简直惊艳， 应该是第⼀个可以真正称为NewSQL的公开设计的系统。所以，BigTable(KV)+F1(SQL)+Spaner(⾼性 能分布式事务⽀持)，同时Spaner还有⼀个⾮常重要的特性是跨数据中⼼的复制和⼀致性保证（通过 Paxos实现），多数据中⼼，刚好补全了整个Gogle的基础设施的数据库栈，使得Gogle对于⼏乎任 何类型的业务系统开发都⾮常⽅便。我想，这就是未来的⽅向吧，⼀个可扩展的KV数据库（作为缓存 和简单对象存储），⼀个⾼性能⽀持分布式事务和SQL查询接⼝的分布式关系型数据库，提供表⽀ 持。 五、Q & A

# Q1：我没看过Codis，您说Codis没有多副本概念，请问是什么意思？

- A1：Codis是⼀个分布式Redis解决⽅案，是通过presharding把数据在概念上分成1024个slot，然后通 过proxy将不同的key的请求转发到不同的机器上，数据的副本还是通过Redis本身保证

Q2：Codis的信息在⼀个zk⾥⾯存储着，zk在Codis中还有别的作⽤吗？主从切换为何不⽤sentinel

- A2：Codis的特点是动态的扩容缩容，对业务透明；zk除了存储路由信息，同时还作为⼀个事件同步的 媒介服务，⽐如变更master或者数据迁移这样的事情，需要所有的proxy通过监听特定zk事件来实现 可以说zk被我们当做了⼀个可靠的rpc的信道来使⽤。因为只有集群变更的admin时候会往zk上发事 件，proxy监听到以后，回复在zk上，admin收到各个proxy的回复后才继续。本身集群变更的事情不会 经常发⽣，所以数据量不⼤。Redis的主从切换是通过codis-ha在zk上遍历各个server group的master 判断存活情况，来决定是否发起提升新master的命令。

Q3：数据分⽚，是⽤的⼀致性hash吗？请具体介绍下，谢谢。

- A3：不是，是通过presharding，hash算法是crc32(key)%1024

Q4：怎么进⾏权限管理？

- A4：Codis中没有鉴权相关的命令，在reborndb中加⼊了auth指令。

Q5：怎么禁⽌普通⽤户链接Redis破坏数据？

- A5：同上，⽬前Codis没有auth，接下来的版本会加⼊。

Q6：Redis跨机房有什么⽅案？

- A6：⽬前没有好的办法，我们的Codis定位是同⼀个机房内部的缓存服务，跨机房复制对于Redis这样 的服务来说，⼀是延迟较⼤，⼆是⼀致性难以保证，对于性能要求⽐较⾼的缓存服务，我觉得跨机房 不是好的选择。

Q7：集群的主从怎么做（⽐如集群S是集群M的从，S和M的节点数可能不⼀样，S和M可能不在⼀个 机房）？

- A7：Codis只是⼀个proxy-based的中间件，并不负责数据副本相关的⼯作。也就是数据只有⼀份，在 Redis内部。

Q8：根据你介绍了这么多，我可以下⼀个结论，你们没有多租户的概念，也没有做到⾼可⽤。可以这 么说吧？你们更多的是把Redis当做⼀个cache来设计。

- A8：对，其实我们内部多租户是通过多Codis集群解决的，Codis更多的是为了替换twemproxy的⼀个 项⽬。⾼可⽤是通过第三⽅⼯具实现。Redis是cache，Codis主要解决的是Redis单点、⽔平扩展的问 题。把codis的介绍贴⼀下： Auto rebalance Extremely simple to use Suport both Redis or rocksdb transparently. GUI dashboard & admin tols Suports most of Redis comands. Fuly compatible with twemproxy(htps:/github.com/twiter/twemproxy). Native Redis clients are suported Safe and transparent data migration, Easily ad or remove nodes on-demand.解决的问题是这些。业务不 停的情况下，怎么动态的扩展缓存层，这个是codis关注的。


# Q9：对于Redis冷备的数据库的迁移，您有啥经验没有？对于Redis热数据，可以通过migrate命令实 现两个Redis进程间的数据转移，当然如果对端有密码，migrate就玩完了（这个我已经给Redis官⽅ 提交了patch）。

- A9：冷数据我们现在是实现了完整的Redisync协议，同时实现了⼀个基于rocksdb的磁盘存储引擎， 备机的冷数据，全部是存在磁盘上的，直接作为⼀个从挂在master上的。实际使⽤时，3个group， keys数量⼀致，但其中⼀个的ops是另外两个的两倍，有可能是什么原因造成的？key的数量⼀致并不 代表实际请求是均匀分布的，不如你可能某⼏个key特别热，它⼀定是会落在实际存储这个key的机器 上的。刚才说的rocksdb的存储引擎：htps:/github.com/reborndb/qdb，其实启动后就是个Redisserver，⽀持了PSYNC协议，所以可以直接当成Redis从来⽤。是⼀个节省从库内存的好⽅法。

Q10：Redis实例内存占⽐超过50%，此时执⾏bgsave，开了虚拟内存⽀持的会阻塞，不开虚拟内存 ⽀持的会直接返回er，对吗？

- A10：不⼀定，这个要看写数据（开启bgsave后修改的数据）的频繁程度，在Redis内部执⾏bgsave， 其实是通过操作系统COW机制来实现复制，如果你这段时间的把⼏乎所有的数据都修改了，这样操作 系统只能全部完整的复制出来，这样就爆了。 Q1：刚读完，赞⼀个。可否介绍下codis的autorebalance实现。 A1：算法⽐较简单， htps:/github.com/wandoulabs/codis/blob/master/cmd/config/rebalancer.go#L104。代码⽐较清 楚，code talks:)。其实就是根据各个实例的内存⽐例，分配slot好的。 Q12：主要想了解对降低数据迁移对线上服务的影响，有没有什么经验介绍？ A12：其实现在codis数据迁移的⽅式已经很温和了，是⼀个个key的原⼦迁移，如果怕抖动甚⾄可以加 上每个key的延迟时间。这个好处就是对业务基本没感知，但是缺点就是慢。
