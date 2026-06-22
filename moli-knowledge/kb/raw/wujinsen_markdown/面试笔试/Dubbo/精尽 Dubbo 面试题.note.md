以下⾯试题，基于⽹络整理，和⾃⼰编辑。具体参考的⽂章，会在⽂末给出所有的链接。 如果胖友有⾃⼰的疑问，欢迎在星球提问，我们⼀起整理吊吊的 Dubo ⾯试题的⼤保健。 ⽽题⽬的难度，艿艿尽量按照从容易到困难的顺序，逐步下去。 Dubo 有⼏种配置⽅式？ 正如在 中所⻅，⼀共有四种配置⽅式：

《Dubo ⽤户指南 ⸺ 配置》 XML 配置 注解配置 属性配置 Java API 配置

实际上，还有第五种⽅式，外部化配置。参⻅ 。 ⽬前，主要使⽤的是 XML 配置和注解配置。具体使⽤哪⼀种，就看⼤家各⾃的喜好。⽬前，艿艿偏好 XML 配置，更加清晰好管理。 Dubo 如何和 Spring Bot 进⾏集成？ 官⽅提供提供了集成库 dubbo-spring-boot ，对应仓库为

《Dubo 新编程模型之外部化配置》

htps:/github.com/apache/incubator-d ubo-spring-bot-project

。

Dubo 框架的分层设计 在 ⼀⽂中，对 Dubo 框架的分层已经有过介绍，这⾥ 再来⼀次。 相对⽐较复杂，⼀共分成 10 层，当然理解后是⾮常清晰的。如下图所示：

《精尽 Dubo 源码分析 ⸺ 核⼼流程⼀览》

![image 1](<精尽 Dubbo 面试题.note_images/imageFile1.png>)

整体设计

图例说明

最顶上九个图标，代表本图中的对象与流程。 图中左边 淡蓝背景( Consumer ) 的为服务消费⽅使⽤的接⼝，右边 淡绿⾊背景( Provider ) 的为服 务提供⽅使⽤的接⼝，位于中轴线上的为双⽅都⽤到的接⼝。 图中从下⾄上分为⼗层，各层均为单向依赖，右边的 ⿊⾊箭头( Depend ) 代表层之间的依赖关系， 每⼀层都可以剥离上层被复⽤。其中，Service 和 Config 层为 API，其它各层均为 SPI 。

注意，Dubo 并未使⽤ JDK SPI 机制，⽽是⾃⼰实现了⼀套 Dubo SPI 机制。

图中 绿⾊⼩块( Interface ) 的为扩展接⼝，蓝⾊⼩块( Clas ) 为实现类，图中只显示⽤于关联各层 的实现类。 图中 蓝⾊虚线( Init ) 为初始化过程，即启动时组装链。红⾊实线( Cal )为⽅法调⽤过程，即运⾏时 调时链。紫⾊三⻆箭头( Inherit )为继承，可以把⼦类看作⽗类的同⼀个节点，线上的⽂字为调⽤的 ⽅法。

各层说明 虽然，有 10 层这么多，但是总体是分层 Busines、RPC、Remoting三⼤层。如下：

= Busines = Service 业务层：业务代码的接⼝与实现。我们实际使⽤ Dubo 的业务层级。

接⼝层，给服务提供者和消费者来实现的。

= RPC = config 配置层：对外配置接⼝，以 ServiceConfig, ReferenceConfig 为中⼼，可以直接初始化配置 类，也可以通过 Spring 解析配置⽣成配置类。

配置层，主要是对 Dubo 进⾏各种配置的。

proxy 服务代理层：服务接⼝透明代理，⽣成服务的客户端 Stub 和服务器端 Skeleton, 扩展接⼝为 ProxyFactory 。

服务代理层，⽆论是 consumer 还是 provider，Dubo 都会给你⽣成代理，代理之间进⾏⽹络通 信。 如果胖友了解 Spring Cloud 体系，可以类⽐成 Feign 对于 consumer ，Spring MVC 对于 provider 。

registry 注册中⼼层：封装服务地址的注册与发现，以服务 URL 为中⼼，扩展接⼝为 RegistryFactory, Registry, RegistryService 。

服务注册层，负责服务的注册与发现。 如果胖友了解 Spring Cloud 体系，可以类⽐成 Eureka Client 。

cluster 路由层：封装多个提供者的路由及负载均衡，并桥接注册中⼼，以 Invoker 为中⼼，扩展接 ⼝为 Cluster, Directory, Router, LoadBalance 。

集群层，封装多个服务提供者的路由以及负载均衡，将多个实例组合成⼀个服务。 如果胖友了解 Spring Cloud 体系，可以类⽐城 Ri bon 。

monitor 监控层：RPC 调⽤次数和调⽤时间监控，以 Statistics 为中⼼，扩展接⼝为 MonitorFactory, Monitor, MonitorService 。

监控层，对 rpc 接⼝的调⽤次数和调⽤时间进⾏监控。

如果胖友了解 SkyWalking 链路追踪，你会发现，SkyWalking 基于 MonitorFilter 实现增强，从⽽ 透明化埋点监控。

= Remoting = protocol 远程调⽤层：封将 RPC 调⽤，以 Invocation, Result 为中⼼，扩展接⼝为 Protocol, Invoker, Exporter 。

远程调⽤层，封装 rpc 调⽤。

exchange 信息交换层：封装请求响应模式，同步转异步，以 Request, Response 为中⼼，扩展接 ⼝为 Exchanger, ExchangeChanel, ExchangeClient, ExchangeServer 。

信息交换层，封装请求响应模式，同步转异步。

transport ⽹络传输层：抽象 mina 和 nety 为统⼀接⼝，以 Mesage 为中⼼，扩展接⼝为 Chanel, Transporter, Client, Server, Codec 。

⽹络传输层，抽象 mina 和 nety 为统⼀接⼝。

serialize 数据序列化层：可复⽤的⼀些⼯具，扩展接⼝为 Serialization, ObjectInput, ObjectOutput, ThreadPol 。

数据序列化层。 Dubo 调⽤流程

![image 2](<精尽 Dubbo 面试题.note_images/imageFile2.png>)

简化调⽤图

Provider

- 第 0 步，start 启动服务。

- 第 1 步，register 注册服务到注册中⼼。

Consumer

- 第 2 步，subscribe 向注册中⼼订阅服务。 注意，只订阅使⽤到的服务。


再注意，⾸次会拉取订阅的服务列表，缓存在本地。 【异步】第 3 步，notify 当服务发⽣变化时，获取最新的服务列表，更新本地缓存。

invoke 调⽤ Consumer 直接发起对 Provider 的调⽤，⽆需经过注册中⼼。⽽对多个 Provider 的负载均衡， Consumer 通过 cluster 组件实现。

count 监控 【异步】Consumer 和 Provider 都异步通知监控中⼼。

这⾥艿艿在引⽤⼀张在⽹上看到的图，更⽴体的展示 Dubo 的调⽤流程：

![image 3](<精尽 Dubbo 面试题.note_images/imageFile3.png>)

详细调⽤图

注意，图中的【代理】指的是 proxy 代理服务层，和 Consumer 或 Provider 在同⼀进城中。 注意，图中的【负载均衡】指的是 cluster 路由层，和 Consumer 或 Provider 在同⼀进程中。

Dubo 调⽤是同步的吗？ 默认情况下，调⽤是同步的⽅式。 可以参考 ⽂档，配置异步调⽤的⽅式。当然，使⽤上，感觉蛮不 优雅的。所以，在 Dubo 2.7 版本后，⼜提供了新的两种⽅式，具体先参⻅

《Dubo ⽤户指南 ⸺ 异步调⽤》

《Dubo下⼀站：Apach e顶级项⽬》

⽂章。估计，后续才会更新官⽅⽂档。 谈谈对 Dubo 的异常处理机制？ Dubo 异常处理机制涉及的内容⽐较多，核⼼在于 Provider 的 异常过滤器 ExceptionFilter 对调⽤结 果的各种情况的处理。所以建议胖友看如下三篇⽂章：

墙裂推荐 《Dubo(四) 异常处理》 《浅谈 Dubo 的 ExceptionFilter 异常处理》 《精尽 Dubo 源码分析 ⸺ 过滤器（七）之 ExceptionFilter》

Dubo 如何做参数校验？ 在 中，介绍如下：

《Dubo ⽤户指南 ⸺ 参数验证》 JSR303

参数验证功能是基于 实现的，⽤户只需标识 JSR303 标准的验证 anotation，并通过声 明 filter 来实现验证。

参数校验功能，通过参数校验过滤器 ValidationFilter 来实现。 ValidationFilter 在 Dubo Provider 和 Consumer 都可⽣效。

如果我们将校验注解写在 Service 接⼝的⽅法上，那么 Consumer 在本地就会校验。如果校验 不通过，直接抛出校验失败的异常，不会发起 Dubo 调⽤。 如果我们将校验注解写在 Service 实现的⽅法上，那么 Consumer 在本地不会校验，⽽是由 Provider 校验。

Dubo 可以对调⽤结果进⾏缓存吗? Dubo 通过 CacheFilter 过滤器，提供结果缓存的功能，且既可以适⽤于 Consumer 也可以适⽤于 Provider 。 通过结果缓存，⽤于加速热⻔数据的访问速度，Dubo 提供声明式缓存，以减少⽤户加缓存的⼯作 量。 Dubo ⽬前提供三种实现：

lru ：基于最近最少使⽤原则删除多余缓存，保持最热的数据被缓存。 threadlocal ：当前线程缓存，⽐如⼀个⻚⾯渲染，⽤到很多 portal，每个 portal 都要去查⽤户 信息，通过线程缓存，可以减少这种多余访问。 jcache ：与 JSR107 集成，可以桥接各种缓存实现。

详细的源码解析，可⻅ 。 注册中⼼挂了还可以通信吗？

《精尽 Dubo 源码分析 ⸺ 过滤器（⼗）之 CacheFilter》

可以。对于正在运⾏的 Consumer 调⽤ Provider 是不需要经过注册中⼼，所以不受影响。并且， Consumer 进程中，内存已经缓存了 Provider 列表。 那么，此时 Provider 如果下线呢？如果 Provider 是正常关闭，它会主动且直接对和其处于连接中的 Consumer 们，发送⼀条“我要关闭”了的消息。那么，Consumer 们就不会调⽤该 Provider ，⽽调⽤ 其它的 Provider 。 另外，因为 Consumer 也会持久化 Provider 列表到本地⽂件。所以，此处如果 Consumer 重启，依然 能够通过本地缓存的⽂件，获得到 Provider 列表。 再另外，⼀般情况下，注册中⼼是⼀个集群，如果⼀个节点挂了，Dubo Consumer 和 Provider 将⾃ 动切换到集群的另外⼀个节点上。 Dubo 在 Zokeper 存储了哪些信息？ 下⾯，我们先来看下 ⽂档，内容如下：

《Dubo ⽤户指南 ⸺ zokeper 注册中⼼》

![image 4](<精尽 Dubbo 面试题.note_images/imageFile4.png>)

流程

流程说明：

服务提供者启动时: 向 /dubbo/com.foo.BarService/providers ⽬录下写⼊⾃⼰的 URL 地址 服务消费者启动时: 订阅 /dubbo/com.foo.BarService/providers ⽬录下的提供者 URL 地 址。并向 /dubbo/com.foo.BarService/consumers ⽬录下写⼊⾃⼰的 URL 地址 监控中⼼启动时: 订阅 /dubbo/com.foo.BarService ⽬录下的所有提供者和消费者 URL 地址。 在图中，我们可以看到 Zokeper 的节点层级，⾃上⽽下是：

Rot 层：根⽬录，可通过 <dubbo:registry group="dubbo" /> 的 "group" 设置 Zokeper 的根节点，缺省使⽤ "dubbo" 。 Service 层：服务接⼝全名。 Type 层：分类。⽬前除了我们在图中看到的 "providers"( 服务提供者列表 )"consumers" ( 服务消费者列表 ) 外，还有 ( 路由规则列表 ) 和 ( 配置规则列 表 )。

"routes" "configurations"

URL 层：URL ，根据不同 Type ⽬录，下⾯可以是服务提供者 URL 、服务消费者 URL 、路由 规则 URL 、配置规则 URL 。 实际上 URL 上带有 "category" 参数，已经能判断每个 URL 的分类，但是 Zokeper 是基于 节点⽬录订阅的，所以增加了 Type 层。

实际上，服务消费者启动后，不仅仅订阅了 "providers" 分类，也订阅 了 "routes" "configurations" 分类。

Dubo Provider 如何实现优雅停机？ 在 中，已经对这块进⾏了详细的说明。 优雅停机

《Dubo ⽤户指南 ⸺ 优雅停机》

Dubo 是通过 JDK 的 ShutdownHok 来完成优雅停机的，所以如果⽤户使⽤ kill -9 PID 等 强制关闭指令，是不会执⾏优雅停机的，只有通过 kill PID 时，才会执⾏。

因为⼤多数情况下，Dubo 的声明周期是交给 Spring 进⾏管理，所以在最新的 Dubo 版本中，增 加了对 Spring 关闭事件的监听，从⽽关闭 Dubo 服务。对应可⻅

htps:/github.com/apache/incu bator-dubo/isues/2865

。

服务提供⽅的优雅停机过程

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


⾸先，从注册中⼼中取消注册⾃⼰，从⽽使消费者不要再拉取到它。 然后，sl ep 10 秒( 可配 )，等到服务消费，接收到注册中⼼通知到该服务提供者已经下线，加⼤ 了在不重试情况下优雅停机的成功率。😈 此处是个概率学，嘻嘻。 之后，⼴播 READONLY 事件给所有 Consumer 们，告诉它们不要在调⽤我了！！！【很有趣的⼀ 个步骤】并且，如果此处注册中⼼挂掉的情况，依然能达到告诉 Consumer ，我要下线了的功 能。 再之后，sl ep 10 毫秒，保证 Consumer 们，尽可能接收到该消息。 再再之后，先标记为不接收新请求，新请求过来时直接报错，让客户端重试其它机器。 再再再之后，关闭⼼跳线程。 最后，检测线程池中的线程是否正在运⾏，如果有，等待所有线程执⾏完成，除⾮超时，则强制 关闭。 最最后，关闭服务器。

整个过程⽐较复杂，感兴趣的胖友，可以详细来看看 。 服务消费⽅的优雅停机过程

《精尽 Dubo 源码解析 ⸺ 优雅停机》

- 1.
- 2.


停⽌时，不再发起新的调⽤请求，所有新的调⽤在客户端即报错。 然后，检测有没有请求的响应还没有返回，等待响应返回，除⾮超时，则强制关闭。

Dubo Provider 异步关闭时，如何从注册中⼼下线？

- ① Zokeper 注册中⼼的情况下 服务提供者，注册到 Zokeper 上时，创建的是 EPHEMERAL 临时节点。所以在服务提供者异常关闭 时，等待 Zokeper 会话超时，那么该临时节点就会⾃动删除。

- ② Redis 注册中⼼的情况下 使⽤ Redis 作为注册中⼼，是有点⼩众的选择，我们就不在本⽂详细说了。感兴趣的胖友，可以看看


《精尽 Dubo 源码分析 ⸺ 注册中⼼（三）之 Redis》

⼀⽂。总的来说，实现上，还是蛮有趣的。因 为，需要通知到消费者，服务列表发⽣变化，所以就⽆法使⽤ Redis Key ⾃动过期。所以… 还是看⽂ 章吧。哈哈哈哈。 Dubo Consumer 只能调⽤从注册中⼼获取的 Provider 么？ 不是，Consumer 可以强制直连 Provider 。 在开发及测试环境下，经常需要绕过注册中⼼，只测试指定服务提供者，这时候可能需要点对点直 连，点对点直连⽅式，将以服务接⼝为单位，忽略注册中⼼的提供者列表，A 接⼝配置点对点，不影 响 B 接⼝从注册中⼼获取列表。 相关⽂档，可⻅ 。

《Dubo ⽤户指南 ⸺ 直连提供者》

另外，直连 Dubo Provider 时，如果要 Debug 调试 Dubo Provider ，可以通过配置，禁⽤该 Provider 注册到注册中⼼。否则，会被其它 Consumer 调⽤到。具体的配置⽅式，参⻅

《Dubo ⽤户 指南 ⸺ 只订阅》

。 Dubo ⽀持哪些通信协议？ 对应【protocol 远程调⽤层】。 Dubo ⽬前⽀持如下 9 种通信协议：

【重要】dubbo:// ，默认协议。参⻅ 《Dubo ⽤户指南 ⸺ dubo:/》 。 【重要】rest:// ，贡献⾃ Dubox ，⽬前最合适的 HTP Restful API 协议。参⻅

《Dubo ⽤户

指南 ⸺ rest:/》 rmi:// ，参⻅ 《Dubo ⽤户指南 ⸺ rmi:/》 。 webservice:// ，参⻅ 《Dubo ⽤户指南 ⸺ webservice:/》 。 hessian:// ，参⻅ 《Dubo ⽤户指南 ⸺ hesian:/》 。 thrift:// ，参⻅ 《Dubo ⽤户指南 ⸺ thrift:/》 。 memcached:// ，参⻅ 《Dubo ⽤户指南 ⸺ memcached:/》 。 redis:// ，参⻅ 《Dubo ⽤户指南 ⸺ redis:/》 。 http:// ，参⻅ 。注意，这个和我们理解的 HTP 协议有差异， ⽽是 Spring 的 HtpInvoker 实现。

。

《Dubo ⽤户指南 ⸺ htp:/》

实际上，社区⾥还有其他通信协议正处于孵化：

jsonrpc:// ，对应 Github 仓库为 ， 来⾃千⽶⽹的贡献。

htps:/github.com/apache/incubator-dubo-rpc-jsonrpc

😈 每⼀种通信协议的实现，在 中，都有详细解析。 另外，在 中，官⽅提供了上述协议的性能测试对⽐。 什么是本地暴露和远程暴露，他们的区别？ 远程暴露，⽐较好理解。在 问题汇总，我们看到的，都是远程暴露。 每次 Consumer 调⽤ Provider 都是跨进程，需要进⾏⽹络通信。 本地暴露，在 ⼀⽂中，定义如下：

《精尽 Dubo 源码解析》 《Dubo ⽤户指南 ⸺ 性能测试报告》

「Dubo ⽀持哪些通信协议？」

《Dubo ⽤户指南 ⸺ 本地调⽤》

本地调⽤使⽤了 injvm:// 协议，是⼀个伪协议，它不开启端⼝，不发起远程调⽤，只在 JVM 内 直接关联，但执⾏ Dubo 的 Filter 链。

怎么理解呢？本地的 Dubo Service Proxy 对象，每次调⽤时，会⾛ Dubo Filter 链。 举个例⼦，Spring Bot Controler 调⽤ Service 逻辑，就变成了调⽤ Dubo Service Proxy 对象。 这样，如果未来有⼀天，本地 Dubo Service 迁移成远程的 Dubo Service ，只需要进⾏配置的修 改，⽽对 Controler 是透明的。

Dubo 使⽤什么通信框架？ 对应【transport ⽹络传输层】。 在通信框架的选择上，强⼤的技术社区有⾮常多的选择，如下列表：

- Nety3

- Nety4


Mina Grizly

那么 Dubo 是如何做技术选型和实现的呢？Dubo 在通信层拆分成了 API 层、实现层。项⽬结构如 下：

API 层：

dubbo-remoting-api 实现层：

- dubbo-remoting-netty3

- dubbo-remoting-netty4 dubbo-remoting-mina dubbo-remoting-grizzly


再配合上 Dubo SPI 的机制，使⽤者可以⾃定义使⽤哪⼀种具体的实现。美滋滋。 在 Dubo 的最新版本，默认使⽤ Nety4 的版本。😈 这就是结论。嘻嘻。 Dubo ⽀持哪些序列化⽅式？

对应【serialize 数据序列化层】。 Dubo ⽬前⽀付如下 7 种序列化⽅式：

【重要】Hesian2 ：基于 Hesian 实现的序列化拓展。dubbo:// 协议的默认序列化⽅案。 Hesian 除了是 Web 服务，也提供了其序列化实现，因此 Dubo 基于它实现了序列化拓展。 另外，Dubo 维护了⾃⼰的 ，对 的 序列化 部分的精简、改进、 BugFix 。

hessian-lite Hesian 2

Dubo ：Dubo ⾃⼰实现的序列化拓展。

具体可参⻅ 《精尽 Dubo 源码分析 ⸺ 序列化（⼆）之 Dubo 实现》 。 Kryo ：基于 Kryo 实现的序列化拓展。

具体可参⻅ 《Dubo ⽤户指南 ⸺ Kryo 序列化》 FST ：基于 FST 实现的序列化拓展。

具体可参⻅ 《Dubo ⽤户指南 ⸺ FST 序列化》 JSON ：基于 Fastjson 实现的序列化拓展。 NativeJava ：基于 Java 原⽣的序列化拓展。 CompactedJava ：在 NativeJava 的基础上，实现了对 ClasDescriptor 的处理。

可能胖友会⼀脸懵逼，有这么多？其实还好，上述基本是市⾯上主流的集中序列化⼯具，Dubo 基于 它们之上提供序列化拓展。 然后，胖友可能会说，Protobuf 也是⾮常优秀的序列化⽅案，为什么 Dubo 没有基于它的序列化拓 展？从 Dubo 后续的开发计划上，应该会增加该序列化的⽀持。另外，微博的 Motan 有实现对 Protobuf 序列化的⽀持，感兴趣的胖友，可以看看 的

《深⼊理解RPC之序列化篇 ⸺ 总结篇》 「Pro tostuf实现」

⼩节。 Dubo 有哪些负载均衡策略？ 对应【cluster 路由层】的 LoadBalance 组件。

在 中，我们可以看到 Dubo 内置 4 种负载均衡策略。其中，默认 使⽤ random 随机调⽤策略。 Random LoadBalance

《Dubo ⽤户指南 ⸺ 负载均衡》

随机，按权重设置随机概率。 在⼀个截⾯上碰撞的概率⾼，但调⽤量越⼤分布越均匀，⽽且按概率使⽤权重后也⽐较均匀，有利 于动态调整提供者权重。

RoundRobin LoadBalance

轮询，按公约后的权重设置轮询⽐率。 存在慢的提供者累积请求的问题，⽐如：第⼆台机器很慢，但没挂，当请求调到第⼆台时就卡在 那，久⽽久之，所有请求都卡在调到第⼆台上。

举个栗⼦。 跟运维同学申请机器，有的时候，我们运⽓好，正好公司资源⽐较充⾜，刚刚有⼀批热⽓腾腾、刚 刚做好的⼀批虚拟机新鲜出炉，配置都⽐较⾼。8核+16g，机器，2 台。过了⼀段时间，我感觉 2 台机器有点不太够，我去找运维同学，哥⼉们，你能不能再给我 1 台机器，4核+8G的机器。我还 是得要。 这个时候，可以给两台 8核16g 的机器设置权重 4，给剩余 1 台 4核8G 的机器设置权重 2。

LeastActive LoadBalance

最少活跃调⽤数，相同活跃数的随机，活跃数指调⽤前后计数差。 使慢的提供者收到更少请求，因为越慢的提供者的调⽤前后计数差会越⼤。

这个就是⾃动感知⼀下，如果某个机器性能越差，那么接收的请求越少，越不活跃，此时就会给不 活跃的性能差的机器更少的请求。

ConsistentHash LoadBalance

⼀致性 Hash，相同参数的请求总是发到同⼀提供者。 当某⼀台提供者挂时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧 烈变动。

Dubo 有哪些集群容错策略？

对应【cluster 路由层】的 Cluster 组件。 在 中，我们可以看到 Dubo 内置 6 种负载均衡策略。其中，默认 使⽤ failover 失败⾃动重试其他服务的策略。 Failover Cluster 失败⾃动切换，当出现失败，重试其它服务器。通常⽤于读操作，但重试会带来更⻓延迟。可通 过 retries="2" 来设置重试次数(不含第⼀次)。 Failfast Cluster 快速失败，只发起⼀次调⽤，失败⽴即报错。通常⽤于⾮幂等性的写操作，⽐如新增记录。 Failsafe Cluster 失败安全，出现异常时，直接忽略。通常⽤于写⼊审计⽇志等操作。 Failback Cluster

《Dubo ⽤户指南 ⸺ 集群容错》

失败⾃动恢复，后台记录失败请求，定时重发。通常⽤于消息通知操作。 Forking Cluster 并⾏调⽤多个服务器，只要⼀个成功即返回。通常⽤于实时性要求较⾼的读操作，但需要浪费更多服 务资源。可通过 forks="2" 来设置最⼤并⾏数。 Broadcast Cluster ⼴播调⽤所有提供者，逐个调⽤，任意⼀台报错则报错。通常⽤于通知所有提供者更新缓存或⽇志等 本地资源信息。 Dubo 有哪些动态代理策略？

对应【proxy 服务代理层】。 可能有胖友对动态代理不是很了解。因为，Consumer 仅仅引⽤服务 ***-api.jar 包，那么可以获 得到需要服务的 XService 接⼝。那么，通过动态创建对应调⽤ Dubo 服务的实现类。简化代码如 下：

<table>
  <tr>
    <th>/ ProxyFactory.java /*<br><br>* create proxy.<br><br>*<br><br>* 创建 Proxy ，在引⽤服务调⽤。<br><br>*<br><br>* @param invoker Invoker 对象<br>* @return proxy<br>*/ @Adaptive({Constants.PROXY_KEY})<br></th>
  </tr>
</table>


<T> T getProxy(Invoker<T> invoker) throws RpcException;

⽅法参数 invoker ，实现了调⽤ Dubo 服务的逻辑。 返回的 <T> 结果，就是 XService 的实现类，⽽这个实现类，就是通过动态代理的⼯具类进⾏⽣ 成。

通过动态代理的⽅式，实现了对于我们开发使⽤ Dubo 时，透明的效果。当然，因为实际场景下，我 们是结合 Spring 场景在使⽤，所以不会直接使⽤该 API 。

⽬前实现动态代理的⼯具类还是蛮多的，如下：

Javasist JDK原 ⽣ ⾃ 带 CGLIB ASM

其中，Dubo 动态代理使⽤了 Javasist 和 JDK 两种⽅式。

默认情况下，使⽤ Javasist 。 可通过 SPI 机制，切换使⽤ JDK 的⽅式。

为什么默认使⽤ Javasist？ 在 Dubo 开发者【梁⻜】的博客 中，我们可以看到这⼏种⽅式的性能差 异，⽽ Javasit 排在第⼀。也就是说，因为性能的原因。

《动态代理⽅案性能对⽐》

有⼀点需要注意，Javasit 提供字节码 bytecode ⽣成⽅式和动态代理接⼝两种⽅式。后者的性能⽐ JDK ⾃带的还慢，所以 Dubo 使⽤的是前者字节码 bytecode ⽣成⽅式。 那么是不是 JDK 代理就没意义？ 实际上，JDK 代理在 JDK 1.8 版本下，性能已经有很⼤的提升，并且⽆需引⼊三⽅⼯具的依赖，也是 ⾮常棒的选择。所以，Spring 和 Motan 在动态代理⽣成上，优先选择 JDK 代理。

注意，Spring 同时也选择了 CGLIB 作为⽣成动态代理的⼯具之⼀。

更多的内容，⾮常推荐阅读徐妈的 。很棒！ Dubo SPI 的设计思想是什么？ ⾸先的⾸先，我们得来理解 Java SPI 是什么？因为徐妈在这块已经写了⾮常⾮常⾮常不错的⽂章，我 们直接认真，⼀定要认真看 。 那么既然 Java SPI 机制已经这么⽜逼，为什么 Dubo 还要⾃⼰实现 Dubo SPI 机制呢？良⼼的 Dubo 在 中，给出了答案：

《深⼊理解 RPC 之动态代理篇》

《JAVA 拾遗 ⸺ 关于 SPI 机制》

《Dubo 开发指南 ⸺ 扩展点加载》

- 1、JDK 标准的 SPI 会⼀次性实例化扩展点所有实现，如果有扩展实现初始化很耗时，但如果没⽤ 上也加载，会很浪费资源。

- 2、如果扩展点加载失败，连扩展点的名称都拿不到了。⽐如：JDK 标准的 ScriptEngine，通过 getName() 获取脚本类型的名称，但如果 RubyScriptEngine 因为所依赖的 jruby.jar 不存在，导致 RubyScriptEngine 类加载失败，这个失败原因被吃掉了，和 ruby 对应不起来，当⽤户执⾏ ruby 脚 本时，会报不⽀持 ruby，⽽不是真正失败的原因。

- 3、增加了对扩展点 IoC 和 AOP 的⽀持，⼀个扩展点可以直接 seter 注⼊其它扩展点。


什么意思呢？

第⼀点问题，Dubo 有很多的拓展点，例如 Protocol、Filter 等等。并且每个拓展点有多种的实 现，例如 Protocol 有 DuboProtocol、InjvmProtocol、RestProtocol 等等。那么使⽤ JDK SPI 机 制，会初始化⽆⽤的拓展点及其实现，造成不必要的耗时与资源浪费。 第⼆点问题，因为没⽤过 ScriptEngine ，所以看不懂，哈哈哈哈。 第三点问题，严格来说，这不算问题，⽽是增加了功能特性，更多的提现是，Dubo SPI 提供类似 Spring IoC 和 AOP 的功能。

《精尽 Dubo 源码分析 ⸺ 拓展机制 SP I》

如果如果如果想要深⼊理解 Dubo SPI 体系，胖友可以阅读

。艿话说的好，读懂 Dubo SPI 的源码，你就读懂了⼀半 Dubo 的源码。 如果说，胖友想要⾃定义⼀个 Dubo SPI 某个拓展点的实现，可以阅读

《Dubo 开发指南 ⸺ 扩展 点加载》 Filter 调 ⽤拦截扩展

。当然，如果你是⾸次写，可能会有⼀丢丢复杂。实际场景下，我们写的最多的是 。所以，撸起袖⼦，来⼀发！

当然，虽然 Dubo 实现了 Dubo SPI ，这并意味着 Java SPI 不好⽤。实际上，Java SPI 被⼤量中间 件所采⽤，例如 Tomcat、SkyWalking、JDBC 等等。

再引申下，有些刁钻的⾯试官，可能会让你先讲讲 Spring IoC 是如何实现的，Dubo SPI 是怎么提供 IoC 功能的，那么你可以看看如下两篇⽂章来准备：

Spring IoC ，《⾯试问烂的 Spring IoC 过程》 。 Dubo SPI IoC ，《Dubo SPI 机制和 IoC》 的 「IOC 注⼊」。

再再引申下，有些刁钻的⾯试官，可能会让你先讲讲 Spring AOP 是如何实现的，Dubo SPI 是怎么提 供 AOP 功能的，那么你可以看看如下两篇⽂章来准备：

Spring AOP ，《⾯试问烂的 Spring AOP 原理》 。 Dubo SPI AOP ，详细⻅ 《精尽 Dubo 源码分析 ⸺ 拓展机制 SPI》 ⽂章。核⼼源码是：

<table>
  <tr>
    <th>private static final ConcurrentMap<Clas<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Clas<?>, Object>();<br><br>1:<br><br>7: @SupresWarnings("unchecked")<br>8: private T createExtension(String name) {<br>9: / 获得拓展名对应的拓展实现类<br>10: Clas<?> claz = getExtensionClases().get(name);<br><br><br>1: if (claz = nul) {<br><br>12: throw findException(name); / 抛出异常<br>13: }<br>14: try {<br>15: / 从缓存中，获得拓展对象。<br>16: T instance = (T) EXTENSION_INSTANCES.get(claz);<br>17: if (instance = nul) {<br>18: / 当缓存不存在时，创建拓展对象，并添加到缓存中。<br>19: EXTENSION_INSTANCES.putIfAbsent(claz, claz.newInstance();<br>20: instance = (T) EXTENSION_INSTANCES.get(claz);<br>21: }<br><br><br>2: / 注⼊依赖的属性<br><br>23: injectExtension(instance);<br>24: / 创建 Wraper 拓展对象<br>25: Set<Clas<?> wraperClases = cachedWraperClases;<br>26: if (wraperClases != nul & !wraperClases.isEmpty() {<br>27: for (Clas<?> wraperClas : wraperClases) {<br>28: instance = injectExtension(T) wraperClas.getConstructor(type).newInstance(instance);<br>29: }<br>30: }<br>31: return instance;<br>32: } catch (Throwable t) {<br><br><br>3: throw new IlegalStateException("Extension instance(name: " + name + ", clas: " +<br><br><br>34: type + ") could not be instantiated: " + t.getMesage(), t);<br>35: }<br></th>
  </tr>
</table>


36: }

第 24 ⾄ 30 ⾏：创建 Wraper 拓展对象，将 instance 包装在其中。在 ⽂章中，如此介绍 Wraper 类：

《Dubo 开发指南 ⸺ 扩展点加载》

Wraper 类同样实现了扩展点接⼝，但是 Wraper 不是扩展点的真正实现。它的⽤途主要是⽤于 从 ExtensionLoader 返回扩展点时，包装在真正的扩展点实现外。即从 ExtensionLoader 中返回的 实际上是 Wraper 类的实例，Wraper 持有了实际的扩展点实现类。 扩展点的 Wraper 类可以有多个，也可以根据需要新增。 通过 Wraper 类可以把所有扩展点公共逻辑移⾄ Wraper 中。新加的 Wraper 在所有的扩展点 上添加了逻辑，有些类似 AOP，即 Wraper 代理了扩展点。

例如：ListenerExporterWraper、ProtocolFilterWraper 。

Dubo 服务如何监控和管理？

⼀旦使⽤ Dubo 做了服务化后，必须必须必须要做的服务治理，也就是说，要做服务的管理与监控。 当然，还有服务的降级和限流。这块，放在下⾯的⾯试题，在详细解析。 Dubo 管理平台 + 监控平台

dubbo-monitor 监控平台，基于 Dubo 的【monitor 监控层】，实现相应的监控数据的收集到监 控平台。 dubbo-admin 管理平台，基于注册中⼼，可以获取到服务相关的信息。

关于这块的选择，胖友直接看看 。 另外，⽬前 Dubo 正在重做 dubbo-admin 管理平台，感兴趣的胖友，可以跟进

《Dubo监控和管理（dubokeper）》

htps:/github.com/ apache/incubator-dubo-ops

。

链路追踪 关链路追踪的概念，就不重复介绍了，😈 如果不懂，请⾃⾏ Gogle 下。 ⽬前能够实现链路追踪的组件还是⽐较多的，如下：

Apache SkyWalking 【推荐】 Zipkin Cat PinPoint

具体集成的⽅式，Dubo 官⽅推荐了两篇博⽂：

《使⽤ Apache SkyWalking (Incubator) 做分布式跟踪》 《在 Dubo 中使⽤ Zipkin》

Dubo 服务如何做降级？ ⽐如说服务 A 调⽤服务 B，结果服务 B 挂掉了。服务 A 再重试⼏次调⽤服务 B，还是不⾏，那么直接 降级，⾛⼀个备⽤的逻辑，给⽤户返回响应。 在 Dubo 中，实现服务降级的功能，⼀共有两⼤种⽅式。

- ① Dubo 原⽣⾃带的服务降级功能 具体可以看看 。 当然，这个功能，并不能实现现代微服务的熔断器的功能。所以⼀般情况下，不太推荐这种⽅式，⽽ 是采⽤第⼆种⽅式。

- ② 引⼊⽀持服务降级的组件 ⽬前开源社区常⽤的有两种组件⽀持服务降级的功能，分别是：


《Dubo ⽤户指南 ⸺ 服务降级》

Alibaba Sentinel Netflix Hystrix

因为⽬前 Hystrix 已经停⽌维护，并且和 Dubo 的集成度不是特别⾼，需要做⼆次开发，所以推荐使 ⽤ Sentinel 。具体的介绍，胖友可以看看 。 关于 Dubo 如何集成 Sentinel ，胖友可以阅读 ⼀⽂。 关于 Sentinel 和 Hystrix 对⽐，胖友可以阅读 ⼀⽂。 Dubo 如何做限流？ 在做服务稳定性时，有⼀句⾮常经典的话：

《Sentinel 介绍》

《Sentinel 为 Dubo 服务保驾护航》 《Sentinel 与 Hystrix 的对⽐》

怀疑第三⽅ 防备使⽤⽅ 做好⾃⼰

那么，上⾯看到的服务降级，就属于怀疑第三⽅。 ⽽本⼩节的限流⽬的，就是防备使⽤⽅。 此处，艿艿要再推荐⼀篇⽂章： 。

《你应该如何正确健壮后端服务？》

⽬前，在 Dubo 中，实现服务降级的功能，⼀共有两⼤种⽅式。

- ① Dubo 原⽣⾃带的限流功能 通过 TpsLimitFilter 实现，仅适⽤于服务提供者。具体的使⽤⽅式，源码实现，看看

。 😈 参照 TpsLimitFilter 的思路，可以实现⾃定义限流的 Filter ，并且使⽤ Guava RateLimiter ⼯具类， 达到 的功能。

- ② 引⼊⽀持限流的组件


《精尽 Dubo 源 码分析 ⸺ 过滤器（九）之 TpsLimitFilter》

令牌桶算法限流

关于这个功能，还是推荐集成 Sentinel 组件。 Dubo 的失败重试是什么？ 所谓失败重试，就是 consumer 调⽤ provider 要是失败了，⽐如抛异常了，此时应该是可以重试的， 或者调⽤超时了也可以重试。 实际场景下，我们⼀般会禁⽤掉重试。因为，因为超时后重试会有问题，超时你不知道是成功还是失 败。例如，可能会导致两次扣款的问题。 所以，我们⼀般使⽤ failfast 集群容错策略，⽽不是 failover 策略。配置如下：

<table>
  <tr>
    <th><dubo:service cluster="failfast" timeout="2 0" /></th>
  </tr>
</table>


另外，⼀定⼀定⼀定要配置适合⾃⼰业务的超时时间。 当然，可以将操作分成读和写两种，前者⽀持重试，后者不⽀持重试。因为，读操作天然具有幂等 性。 Dubo ⽀持哪些注册中⼼？ Dubo ⽀持多种主流注册中⼼，如下：

【默认】Zokeper ，参⻅ 《⽤户指南 ⸺ Zokeper 注册中⼼》 。 Redis ，参⻅ 《⽤户指南 ⸺ Redis 注册中⼼》 。 Multicast 注册中⼼，参⻅ 《⽤户指南 ⸺ Multicast 注册中⼼》 。 Simple 注册中⼼，参⻅ 《⽤户指南 ⸺ Simple 注册中⼼》 。

⽬前 Alibaba 正在开源新的注册中⼼ ，也是未来的选择之⼀。 当然，Netflix Eureka 也是注册中⼼的⼀个选择，不过 Dubo 暂未集成实现。 另外，此处会引申⼀个经典的问题，⻅ ⽂章。

Nacos

《为什么不应该使⽤ ZoKeper 做服务发现》

# Dubo 接⼝如何实现幂等性？

所谓幂等，简单地说，就是对接⼝的多次调⽤所产⽣的结果和调⽤⼀次是⼀致的。扩展⼀下，这⾥ 的接⼝，可以理解为对外发布的 HTP 接⼝或者 Thrift 接⼝，也可以是接收消息的内部接⼝，甚⾄ 是⼀个内部⽅法或操作。 那么我们为什么需要接⼝具有幂等性呢？设想⼀下以下情形：

在 Ap 中下订单的时候，点击确认之后，没反应，就⼜点击了⼏次。在这种情况下，如果⽆法 保证该接⼝的幂等性，那么将会出现重复下单问题。 在接收消息的时候，消息推送重复。如果处理消息的接⼝⽆法保证幂等，那么重复消费消息产 ⽣的影响可能会⾮常⼤。

所以，从这段描述中，幂等性不仅仅是 Dubo 接⼝的问题，包括 HTP 接⼝、Thrift 接⼝都存在这样 的问题，甚⾄说 MQ 消息、定时任务，都会碰到这样的场景。那么应该怎么办呢？

这个不是技术问题，这个没有通⽤的⼀个⽅法，这个应该结合业务来保证幂等性。 所谓幂等性，就是说⼀个接⼝，多次发起同⼀个请求，你这个接⼝得保证结果是准确的，⽐如不能 多扣款、不能多插⼊⼀条数据、不能将统计值多加了 1。这就是幂等性。 其实保证幂等性主要是三点：

对于每个请求必须有⼀个唯⼀的标识，举个栗⼦：订单⽀付请求，肯定得包含订单 id，⼀个订 单 id 最多⽀付⼀次，对吧。 每次处理完请求之后，必须有⼀个记录标识这个请求处理过了。常⻅的⽅案是在 mysql 中记录 个状态啥的，⽐如⽀付之前记录⼀条这个订单的⽀付流⽔。 每次接收请求需要进⾏判断，判断之前是否处理过。⽐如说，如果有⼀个订单已经⽀付了，就 已经有了⼀条⽀付流⽔，那么如果重复发送这个请求，则此时先插⼊⽀付流⽔，orderId 已经存 在了，唯⼀键约束⽣效，报错插⼊不进去的。然后你就不⽤再扣款了。

实际运作过程中，你要结合⾃⼰的业务来，⽐如说利⽤ redis，⽤ orderId 作为唯⼀键。只有成功 插⼊这个⽀付流⽔，才可以执⾏实际的⽀付扣款。 要求是⽀付⼀个订单，必须插⼊⼀条⽀付流⽔，order_id 建⼀个唯⼀键 unique key。你在⽀付 ⼀个订单之前，先插⼊⼀条⽀付流⽔，order_id 就已经进去了。你就可以写⼀个标识到 redis ⾥⾯ 去，set order_id payed，下⼀次重复请求过来了，先查 redis 的 order_id 对应的 value，如 果是 payed 就说明已经⽀付过了，你就别重复⽀付了。

Dubo 如何升级接⼝？ 参考 。 当⼀个接⼝实现，出现不兼容升级时，可以⽤版本号过渡，版本号不同的服务相互间不引⽤。 可以按照以下的步骤进⾏版本迁移：

《Dubo ⽤户指南 ⸺ 多版本》

- 1.
- 2.
- 3.


在低压⼒时间段，先升级⼀半提供者为新版本。 再将所有消费者升级为新版本。 然后将剩下的⼀半提供者升级为新版本。

利⽤多版本的特性，我们也能实现灰度的功能。对于第 2 步，不要升级所有消费者为新版本，⽽是⼀ 半。 Dubo 在安全机制⽅⾯是如何解决的？

通过令牌验证在注册中⼼控制权限，以决定要不要下发令牌给消费者，可以防⽌消费者绕过注册中⼼ 访问提供者。 另外通过注册中⼼可灵活改变授权⽅式，⽽不需修改或升级提供者。

![image 5](<精尽 Dubbo 面试题.note_images/imageFile5.png>)

认证流程

相关⽂档，可以参⻅ 。 源码解析，可以参⻅ 。 Dubo 需要 Web 容器吗？ 这个问题，仔细回答，需要思考 Web 容器的定义。然⽽实际上，真正想问的是，Dubo 服务启动是否 需要启动类似 Tomcat、Jety 等服务器。 这个答案可以是，也可以是不是。为什么呢？根据协议的不同，Provider 会启动不同的服务器。

《Dubo ⽤户指南 ⸺ 令牌验证》 《精尽 Dubo 源码分析 ⸺ 过滤器（⼋）之 TokenFilter》

在使⽤ dubbo:// 协议时，答案是否，因为 Provider 启动 Nety、Mina 等 NIO Server 。 在使⽤ rest:// 协议时，答案是是，Provider 启动 Tomcat、Jety 等 HTP 服务器，或者也可以 使⽤ Nety 封装的 HTP 服务器。 在使⽤ hessian:// 协议时，答案是是，Provider 启动 Jety、Tomcat 等 HTP 服务器。

为什么要将系统进⾏拆分？ 这个问题，不是仅仅适⽤于 Dubo 的场景，⽽是 SOA、微服务。

⽹上查查，答案极度零散和复杂，很琐碎，原因⼀⼤坨。但是我这⾥给⼤家直观的感受： 要是不拆分，⼀个⼤系统⼏⼗万⾏代码，20 个⼈维护⼀份代码，简直是悲剧啊。代码经常改着改 着就冲突了，各种代码冲突和合并要处理，⾮常耗费时间；经常我改动了我的代码，你调⽤了我 的，导致你的代码也得重新测试，麻烦的要死；然后每次发布都是⼏⼗万⾏代码的系统⼀起发布， ⼤家得⼀起提⼼吊胆准备上线，⼏⼗万⾏代码的上线，可能每次上线都要做很多的检查，很多异常 问题的处理，简直是⼜麻烦⼜痛苦；⽽且如果我现在打算把技术升级到最新的 spring 版本，还不 ⾏，因为这可能导致你的代码报错，我不敢随意乱改技术。

假设⼀个系统是 20 万⾏代码，其中 ⼩A 在⾥⾯改了 1 0 ⾏代码，但是此时发布的时候是这个 20 万⾏代码的⼤系统⼀块⼉发布。就意味着 20 万上代码在线上就可能出现各种变化，20 个⼈， 每个⼈都要紧张地等在电脑⾯前，上线之后，检查⽇志，看⾃⼰负责的那⼀块⼉有没有什么问题。 ⼩A 就检查了⾃⼰负责的 1 万⾏代码对应的功能，确保ok就闪⼈了；结果不巧的是，⼩A 上线的时 候不⼩⼼修改了线上机器的某个配置，导致另外 ⼩B 和 ⼩C 负责的 2 万⾏代码对应的⼀些功能， 出错了。 ⼏⼗个⼈负责维护⼀个⼏⼗万⾏代码的单块应⽤，每次上线，准备⼏个礼拜，上线 -> 部署 -> 检 查⾃⼰负责的功能。 拆分了以后，整个世界清爽了，⼏⼗万⾏代码的系统，拆分成 20 个服务，平均每个服务就 1~2 万 ⾏代码，每个服务部署到单独的机器上。20 个⼯程，20 个 git 代码仓库⾥，20 个码农，每个⼈维 护⾃⼰的那个服务就可以了，是⾃⼰独⽴的代码，跟别⼈没关系。再也没有代码冲突了，爽。每次 就测试我⾃⼰的代码就可以了，爽。每次就发布我⾃⼰的⼀个⼩服务就可以了，爽。技术上想怎么 升级就怎么升级，保持接⼝不变就可以了，爽。 所以简单来说，⼀句话总结，如果是那种代码量多达⼏⼗万⾏的中⼤型项⽬，团队⾥有⼏⼗个⼈， 那么如果不拆分系统，开发效率极其低下，问题很多。但是拆分系统之后，每个⼈就负责⾃⼰的⼀ ⼩部分就好了，可以随便玩⼉随便弄。分布式系统拆分之后，可以⼤幅度提升复杂系统⼤型团队的 开发效率。 但是同时，也要提醒的⼀点是，系统拆分成分布式系统之后，⼤量的分布式系统⾯临的问题也是接 踵⽽来，所以后⾯的问题都是在围绕分布式系统带来的复杂技术挑战在说。

艿艿曾经维护过⼀个⼏⼗万⾏的单体项⽬，并且基本是⼀天发布 2-3 次，期间的痛苦，简直了。 Dubo 如何集成配置中⼼？ 对于使⽤了 Dubo 的系统，配置分成两类：

- ① Dubo ⾃身配置。 例如：Dubo 请求超时，Dubo 重试次数等等。

- ② ⾮ Dubo ⾃身配置 基建配置，例如：数据库、Redis 等配置。 业务配置，例如：订单超时时间，下单频率等等配置。


- 对于 ① ，如果我们在 Provider 配置 Dubo 请求超时时间，当 Consumer 未配置请求超时时间，会继 承该配置，使⽤该请求超时时间。


实现原理： Provider 启动时，会注册到注册中⼼中，包括我们在 <dubbo:service /> 中的配置。 Consumer 启动时，从注册中⼼获取到 Provider 列表后，会合并它们在 <dubbo:service /> 的配置来使⽤。当然，如果 Consumer ⾃⼰配置了该配置项，则使⽤⾃身的。例如说，

Provider 配置了请求超时时间是 10s ，⽽ Consumer 配置了请求超时超时是 5s ，那么最终 Consumer 请求超时的时间是 5s 。 绝⼤数配置可以被继承，合并的核⼼逻辑，⻅

ClusterUtils#mergeUrl(URL remoteUr l, Map<String, String> localMap)

⽅法。

实现代码，⻅ 《精尽 Dubo 源码解析 ⸺ 集群容错（六）之 Configurator 实现》 。

- 对于 ② ，市⾯上有⾮常多的配置中⼼可供选择：


Apolo Nacos Disconf

这个问题不⼤。对于配置中⼼的选择，我们考虑的不是它和 Dubo 的集成，⽽是它和 Spring 的集 成。因为，⼤多数情况下，我们都是使⽤ Spring 作为框架的整合基础。⽬前，Apolo 和 Nacos 对 Spring 的⽀持是⽐较不错的。

Dubo 如何实现分布式事务？ ⾸先，关于分布式事务的功能，不是 Dubo 作为服务治理框架需要去实现的，所以 Dubo 本身并没 有实现。所以在 也提到，⽬前并未实现。 说起分布式，理论的⽂章很多，落地的实践很少。笔者翻阅了各种分布式事务组件的选型，⼤体如 下：

《Dubo ⽤户指南 ⸺ 分布式事务》

TC 模型：TC-Transaction、Hmily XA 模型：Sharding Sphere、MyCAT 2PC 模型：raincat、lcn MQ 模型：RocketMQ BED 模型：Sharding Sphere Saga 模型：ServiceComb Saga

那怎么选择呢？⽬前社区对于分布式事务的选择，暂时没有定论，⾄少笔者没有看到。笔者的想法如 下：

从覆盖场景来说，TC ⽆疑是最优秀的，但是⼤家觉得相对复杂。实际上，复杂场景下，使⽤ TC 来实现，反倒会容易很多。另外，TC 模型，⼀直没有⼤⼚开源，也是⼀⼤痛点。 从使⽤建议来说，MQ 可能是相对合适的( 不说 XA 的原因还是性能问题 )，并且基本轮询了⼀圈朋 友，发现⼤多都是使⽤ MQ 实现最终⼀致性居多。 2PC 模型的实现，笔者觉得⾮常新奇，奈何隔离性是⼀个硬伤。 Saga 模型，可以认为是 TC 模型的简化版，所以在理解和编写的难度上，简单⾮常多。

所以结论是什么呢？

TC 模型：TC-Transaction、Hmily 。

已经提供了和 Dubo 集成的⽅案，胖友可以⾃⼰去试试。 XA 模型：Sharding Sphere、MyCAT 。

⽆需和 Dubo 进⾏集成。 2PC 模型：raincat、lcn 。

已经提供了和 Dubo 集成的⽅案，胖友可以⾃⼰去试试。 MQ 模型：RocketMQ 。

⽆需和 Dubo 进⾏集成。 BED 模型：Sharding Sphere 。

⽆需和 Dubo 进⾏集成。 Saga 模型：ServiceComb Saga 。 好像已经提供了和 Dubo 集成的⽅案，参⻅ 《Saga-dubo-demo》 ⽂档。 😈 暂时没去深⼊研究。

另外，胖友在理解分布式事务时，⼀定要记住，分布式事务需要由多个本地事务组成。⽆论是上述的 那种事务组件模型，它们都是扮演⼀个协调者，使多个本地事务达到最终⼀致性。⽽协调的过程中， 就⾮常依赖每个⽅法操作可以被重复执⾏不会产⽣副作⽤，那么就需要：

幂等性！因为可能会被重复调⽤。如果调⽤两次退款，结果退了两次钱，那就麻烦⼤了。 本地事务！因为执⾏过程中可能会出错，需要回滚。

Dubo 如何集成⽹关服务？ Dubo 如何集成到⽹关服务，需要思考两个问题：

⽹关如何调⽤ Dubo 服务。 ⽹关如何发现 Dubo 服务。

我们先来看看，市⾯上有哪些⽹关服务：

Zul Spring Cloud Gateway Kong

如上三个解决⽅案，都是基于 HTP 调⽤后端的服务。那么，这样的情况下，Dubo 只能通过暴 露 rest:// 协议的服务，才能被它们调⽤。 那么 Dubo 的 rest:// 协议的服务，怎么能够被如上三个解决⽅案注册发现呢？

因为 Dubo 可⽤的注册中⼼有 Zokeper ，如果要被 Zul 或 Spring Cloud Gateway 注册发现， 可以使⽤ spring-cloud-starter-zookeeper-discovery 库。具体可参⻅

《Service Discov

ery with Zokeper》 Dubo 与 Kong 的集成，相对⽐较麻烦，需要通过 Kong 的 API 添加相应的路由规则。具体可参⻅ 《选择 Kong 作为你的 API ⽹关》 ⽂章。

⽂章。

可能会有胖友问，有没⽀持 dubbo:// 协议的⽹关服务呢？⽬前有新的⽹关开源 ，基于 Dubo 泛化调⽤的特性，实现对 dubbo:// 协议的 Dubo 服务的调⽤。

Soul

感兴趣的胖友，可以去研究下。 关于 Dubo 泛化调⽤的特性，胖友可以看看 《Dubo ⽤户指南 ⸺ 使⽤泛化调⽤》 。

实际场景下，我们真的需要 Dubo 集成到⽹关吗？具艿艿了解到，很多公司，并未使⽤⽹关，⽽是使 ⽤ Spring Bot 搭建⼀个 Web 项⽬，引⼊ *-api.jar 包，然后进⾏调⽤，从⽽对外暴露 HTP API 。

如何进⾏系统拆分？ 这个问题，不是仅仅适⽤于 Dubo 的场景，⽽是 SOA、微服务。接上⾯

「为什么要将系统进⾏拆 分？」

。

这个问题说⼤可以很⼤，可以扯到领域驱动模型设计上去，说⼩了也很⼩，我不太想给⼤家太过于 学术的说法，因为你也不可能背这个答案，过去了直接说吧。还是说的简单⼀点，⼤家⾃⼰到时候 知道怎么回答就⾏了。 系统拆分为分布式系统，拆成多个服务，拆成微服务的架构，是需要拆很多轮的。并不是说上来⼀ 个架构师⼀次就给拆好了，⽽以后都不⽤拆。 第⼀轮；团队继续扩⼤，拆好的某个服务，刚开始是 1 个⼈维护 1 万⾏代码，后来业务系统越来越 复杂，这个服务是 10 万⾏代码，5 个⼈；第⼆轮，1个服务 -> 5个服务，每个服务 2 万⾏代码， 每⼈负责⼀个服务。 如果是多⼈维护⼀个服务，最理想的情况下，⼏⼗个⼈，1 个⼈负责 1 个或 2~3 个服务；某个服务 ⼯作量变⼤了，代码量越来越多，某个同学，负责⼀个服务，代码量变成了 10 万⾏了，他⾃⼰不 堪重负，他现在⼀个⼈拆开，5 个服务，1 个⼈顶着，负责 5 个⼈，接着招⼈，2 个⼈，给那个同 学带着，3 个⼈负责 5 个服务，其中 2 个⼈每个⼈负责 2 个服务，1 个⼈负责 1 个服务。 个⼈建议，⼀个服务的代码不要太多，1万⾏左右，两三万撑死了吧。 ⼤部分的系统，是要进⾏多轮拆分的，第⼀次拆分，可能就是将以前的多个模块该拆分开来了，⽐ 如说将电商系统拆分成订单系统、商品系统、采购系统、仓储系统、⽤户系统，等等吧。 但是后⾯可能每个系统⼜变得越来越复杂了，⽐如说采购系统⾥⾯⼜分成了供应商管理系统、采购 单管理系统，订单系统⼜拆分成了购物⻋系统、价格系统、订单管理系统。 扯深了实在很深，所以这⾥先给⼤家举个例⼦，你⾃⼰感受⼀下，核⼼意思就是根据情况，先拆分 ⼀轮，后⾯如果系统更复杂了，可以继续分拆。你根据⾃⼰负责系统的例⼦，来考虑⼀下就好了。

拆分后不⽤ Dubo 可以吗？ 当然是可以，⽅式还有很多：

第⼀种，使⽤ Spring Cloud 技术体系，这个也是⽬前可能最主流的之⼀。 第⼆种，Dubo 换成 gRPC 或者 Thrift 。当然，此时要⾃⼰实现注册发现、负载均衡、集群容错等 等功能。 第三种，Dubo 换成同等定位的服务化框架，例如微博的 Motan 、蚂蚁⾦服的 SofaRPC 。 第四种，Spring MVC + Nginx 。 第五种，每个服务拆成⼀个 Maven 项⽬，打成 Jar 包，给其它服务使⽤。😈 当然，这个不是⼀个 ⽐较特别的⽅案。

当然可以了，⼤不了最次，就是各个系统之间，直接基于 spring mvc，就纯 htp 接⼝互相通信 呗，还能咋样。但是这个肯定是有问题的，因为 htp 接⼝通信维护起来成本很⾼，你要考虑超时 重试、负载均衡等等各种乱七⼋糟的问题，⽐如说你的订单系统调⽤商品系统，商品系统部署了 5 台机器，你怎么把请求均匀地甩给那 5 台机器？这不就是负载均衡？你要是都⾃⼰搞那是可以的， 但是确实很痛苦。 所以 dubo 说⽩了，是⼀种 rpc 框架，就是说本地就是进⾏接⼝调⽤，但是 dubo 会代理这个调 ⽤请求，跟远程机器⽹络通信，给你处理掉负载均衡了、服务实例上下线⾃动感知了、超时重试 了，等等乱七⼋糟的问题。那你就不⽤⾃⼰做了，⽤ dubo 就可以了。

Spring Cloud 与 Dubo 怎么选择？

⾸先，我们来看看这两个技术栈在国内的流⾏程度，据艿艿了解到：

对于国外，Spring Cloud 基本已经统⼀国外的微服务体系。 对于国内，⽼的系统使⽤ Dubo 较多，新的系统使⽤ Spring Cloud 较多。

这样说起来，仿佛 Spring Cloud 和 Dubo 是冲突的关系？！ 实际上，并不然。我们现在所使⽤的 Spring Cloud 技术体系，实际上是 Spring Cloud Netflix 为主， 例如说：

Netflix Eureka 注册中⼼ Netflix Hystrix 熔断组件 Netflix Ri bon 负载均衡 Netflix Zul ⽹关服务

但是，开源的世界，总是这么有趣。⽬前 Alibaba 基于 Spring Cloud 的接⼝，对的是接⼝，实现了⼀ 套 技术体系，并且已经获得 Spring Cloud 的认可，处于孵化状态。组件如下：

Spring Cloud Alibaba Nacos 注册中⼼，对标 Eureka 。 Nacos 配置中⼼，集成到 Spring Cloud Config 。 Sentinel 服务保障，对标 Hystrix 。 Dubo 服务调⽤( 包括负载均衡 )，对标 Ri bon + Feign 。 缺失 ⽹关服务。 RocketMQ 队列服务，集成到 Spring Cloud Stream 。

更多的讨论，胖友可以尾随知乎上的

《请问哪位⼤神⽐较过 spring cloud 和 dubo ，各⾃的优缺点是 什么?》

。 艿艿的个⼈态度上，还是⾮常看好 Spring Cloud Alibaba 技术体系的。为什么呢？因为 Alibaba 背后 有阿⾥云的存在，提供开源项⽬和商业服务的统⼀。😈 这个，是 Netflix 所⽆法⽐拟的。例如说：

<table>
  <tr>
    <th>开源项⽬</th>
    <th>阿⾥云服务</th>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>Tengine</td>
    <td>LBS</td>
  </tr>
  <tr>
    <td>Dubo</td>
    <td>EDAS</td>
  </tr>
</table>


RocketMQ ONS

这⾥在抛出⼀个话题。⽬前传说 Dubo 在国外的接受度⽐较低，那么在 Spring Cloud Alibaba 成功孵 化完后，是否能够杀⼊国外的市场呢？让我们拭⽬以待。 在聊⼀丢丢有意思的事情。 事实上，Netflix 已经基本不再维护 Eureka、Hystrix ，更有趣的是，因为⽹关的事情，Zul 和 Spring Cloud 团队有点闹掰了，因⽽后来有了 Spring Cloud Gateway 。因⽽，Zul2 后续在 Spring Cloud 体 系中的情况，会⾮常有趣~ 另外，Spring Cloud 貌似也实现了⼀个 LoadBalance 负载均衡组件哟。 如何⾃⼰设计⼀个类似 Dubo 的 RPC 框架？ ⾯试官⼼理分析

说实话，就这问题，其实就跟问你如何⾃⼰设计⼀个 MQ ⼀样的道理，就考两个：

你有没有对某个 rpc 框架原理有⾮常深⼊的理解。 你能不能从整体上来思考⼀下，如何设计⼀个 rpc 框架，考考你的系统设计能⼒。

⾯试题剖析 其实问到你这问题，你起码不能认怂，因为是知识的扫盲，那我不可能给你深⼊讲解什么 kafka 源 码剖析，dubo 源码剖析，何况我就算讲了，你要真的消化理解和吸收，起码个把⽉以后了。 所以我给⼤家⼀个建议，遇到这类问题，起码从你了解的类似框架的原理⼊⼿，⾃⼰说说参照 dubo 的原理，你来设计⼀下，举个例⼦，dubo 不是有那么多分层么？⽽且每个分层是⼲啥 的，你⼤概是不是知道？那就按照这个思路⼤致说⼀下吧，起码你不能懵逼，要⽐那些上来就懵， 啥也说不出来的⼈要好⼀些。 举个栗⼦，我给⼤家说个最简单的回答思路：

上来你的服务就得去注册中⼼注册吧，你是不是得有个注册中⼼，保留各个服务的信⼼，可以 ⽤ zokeper 来做，对吧。 然后你的消费者需要去注册中⼼拿对应的服务信息吧，对吧，⽽且每个服务可能会存在于多台 机器上。 接着你就该发起⼀次请求了，咋发起？当然是基于动态代理了，你⾯向接⼝获取到⼀个动态代 理，这个动态代理就是接⼝在本地的⼀个代理，然后这个代理会找到服务对应的机器地址。 然后找哪个机器发送请求？那肯定得有个负载均衡算法了，⽐如最简单的可以随机轮询是不 是。 接着找到⼀台机器，就可以跟它发送请求了，第⼀个问题咋发送？你可以说⽤ nety 了，nio ⽅ 式；第⼆个问题发送啥格式数据？你可以说⽤ hesian 序列化协议了，或者是别的，对吧。然 后请求过去了。 服务器那边⼀样的，需要针对你⾃⼰的服务⽣成⼀个动态代理，监听某个⽹络端⼝了，然后代 理你本地的服务代码。接收到请求的时候，就调⽤对应的服务代码，对吧。

这就是⼀个最最基本的 rpc 框架的思路，先不说你有多⽜逼的技术功底，哪怕这个最简单的思路你 先给出来⾏不⾏？

如果上述描述，胖友看的⽐较闷逼，可以阅读下徐妈写的 ，⾃⼰动⼿撸 ⼀个最最最基础的 RPC 通信的过程。 因为 Dubo 实现了⼤量的抽象，并且提供了多种代码实现，以及⼤量的 RPC 特性，所以代码量会相 对较多。 如果胖友是⾃⼰实现⼀个最⼩化的 PRC 框架，可能代码量会⽐想象中的少很多，可能⼏千⾏代码就够 了。强烈推荐，胖友⾃⼰撸起袖⼦，动起⼿来。从此之后，你会对 RPC 框架，有更深⼊的理解。 其他问题 当然，Dubo 还有很多⾮常细节，甚⾄牵扯到源码的问题，艿艿并未全部列列举。如下的问题，需要 胖友⾃⼰去耐⼼看源码，思考答案。

《简单了解 RPC 实现原理》

Dubo 服务发布过程中，做了哪些事？ 《精尽 Dubo 源码分析 ⸺ 服务暴露（⼀）之本地暴露（Injvm）》 《精尽 Dubo 源码分析 ⸺ 服务暴露（⼆）之远程暴露（Dubo）》

Dubo 服务引⽤过程中，做了哪些事？ 《精尽 Dubo 源码分析 ⸺ 服务引⽤（⼀）之本地引⽤（Injvm）》 《精尽 Dubo 源码分析 ⸺ 服务引⽤（⼆）之远程引⽤（Dubo）》

Dubo 管理平台能够动态改变接⼝的⼀些配置，其原理是怎样的?

路由规则 《精尽 Dubo 源码解析 ⸺ 集群容错（七）之 Router 实现》 《Dubo ⽤户指南 ⸺ 路由规则》

配置规则 《精尽 Dubo 源码解析 ⸺ 集群容错（六）之 Configurator 实现》 《Dubo ⽤户指南 ⸺ 配置规则》

在 Dubo 中，什么时候更新本地的 Zokeper 信息缓存⽂件？订阅Zokeper 信息的整体过程是 怎么样的?

《精尽 Dubo 源码分析 ⸺ 注册中⼼（⼀）之抽象 API》 《精尽 Dubo 源码分析 ⸺ 注册中⼼（⼆）之 Zokeper》

最⼩活跃数算法中是如何统计这个活跃数的？

《精尽 Dubo 源码分析 ⸺ 过滤器（四）之 ActiveLimitFilter & ExecuteLimitFilter》 「2. 2. RpcStatus」 「3. ActiveLimitFilter」

，主 要 和 部分。

部分。 《精尽 Dubo 源码解析 ⸺ 集群容错（四）之 LoadBalance 实现》 「6. LeastActiveL oadBalance」

，主要

简单谈谈你对⼀致性哈希算法的认识？

部分。 《精尽 Dubo 源码解析 ⸺ 集群容错（四）之 LoadBalance 实现》 「7. ConsistentH ashSelector」 关于⼀致性哈希算法在缓存中的使⽤，我们会单独在缓存相关的⾯试题中分享。

，主要

6. 彩蛋 在看到此处，胖友有没发现，在实际⾯试的 Dubo 问题中，Dubo 官⽅⽂档已经给了我们很多答案。 这说明什么呢？⼀定⼀定⼀定要认真研读官⽅提供的知识，毕竟，这是最系统，且第⼀⼿的资料。 如果胖友想对 RPC 有⼀个整体的理解，推荐看看徐妈的这套⽂章《深⼊理解 RPC 系列》：

《简单了解 RPC 实现原理》

