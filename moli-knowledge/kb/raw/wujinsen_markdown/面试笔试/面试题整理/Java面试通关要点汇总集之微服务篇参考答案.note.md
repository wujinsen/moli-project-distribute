# 微服务篇

## 微服务

前后端分离是如何做的

在前后端分离架构中，后端只需要负责按照约定的数据格式向前端提供可调⽤的API服务即可。前 后端之间通过HTTP请求进⾏交互，前端获取到数据后，进⾏⻚⾯的组装和渲染，最终返回给浏览 器。

| 后端 | 前端 | | | ------------------------------------------ | ------------------------------------------------------------ | --------------------

---------------------------------------- | | 服务器 | 浏览器 | | | JAVA | NodeJS | JS + HTML + CSS | | 服务层提供数据接⼝维持数据稳定封装业务逻辑 | 跑在服務器上的JS转发数据，串接服务路由设 计，控制逻辑渲染⻚⾯，体验优化更多的可能 | 跑在浏览器上的JSCSS、JS加載與運⾏DOM操作 任何的前端框架與⼯具共⽤模版、路由 | 参考⾃： 和

https://www.jianshu.com/p/fc0c63404cc7 http://2014.jsconf.cn/slides/herman-taobao web/index.html#/69

微服务哪些框架

Dubbo，是阿⾥巴巴服务化治理的核⼼框架，并被⼴泛应⽤于阿⾥巴巴集团的各成员站点。阿⾥巴

巴近⼏年对开源社区的贡献不论在国内还是国外都是引⼈注⽬的，⽐如：JStorm捐赠给Apache并 加⼊Apache基⾦会等，为中国互联⽹⼈争⾜了⾯⼦，使得阿⾥巴巴在国⼈眼⾥已经从电商升级为 ⼀家科技公司了。

Spring Cloud，从命名我们就可以知道，它是Spring Source的产物，Spring社区的强⼤背书可以说 是Java企业界最有影响⼒的组织了，除了Spring Source之外，还有Pivotal和Netfix是其强⼤的后盾 与技术输出。其中Netflix开源的整套微服务架构套件是Spring Cloud的核⼼。 参考⾃：

http://blog.didispace.com/microservice-framework/ 你怎么理解 RPC 框架

https://www.zhihu.com/question/25536695 说说 RPC 的实现原理

请参考：

⾸先需要有处理⽹络连接通讯的模块，负责连接建⽴、管理和消息的传输。其次需要有编解码的模 块，因为⽹络通讯都是传输的字节码，需要将我们使⽤的对象序列化和反序列化。剩下的就是客户 端和服务器端的部分，服务器端暴露要开放的服务接⼝，客户调⽤服务接⼝的⼀个代理实现，这个 代理实现负责收集数据、编码并传输给服务器然后等待结果返回。 参考⾃：

https://liuzhengyang.github.io/2016/12/16/rpc-principle/ 说说 Dubbo 的实现原理

dubbo作为rpc框架，实现的效果就是调⽤远程的⽅法就像在本地调⽤⼀样。如何做到呢？就是本 地有对远程⽅法的描述，包括⽅法名、参数、返回值，在dubbo中是远程和本地使⽤同样的接⼝； 然后呢，要有对⽹络通信的封装，要对调⽤⽅来说通信细节是完全不可⻅的，⽹络通信要做的就是 将调⽤⽅法的属性通过⼀定的协议（简单来说就是消息格式）传递到服务端；服务端按照协议解析 出调⽤的信息；执⾏相应的⽅法；在将⽅法的返回值通过协议传递给客户端；客户端再解析；在调 ⽤⽅式上⼜可以分为同步调⽤和异步调⽤；简单来说基本就这个过程 作者：北冥有⻥ 链接： 来源：知乎 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

https://www.zhihu.com/question/52133065/answer/129153953

你怎么理解 RESTful

http://www.cnblogs.com/artech/p/3506553.html 说说如何设计⼀个良好的 API

https://juejin.im/entry/59b8d34c6fb9a00a4455dd04 如何理解 RESTful API 的幂等性

http://blog.720ui.com/2016/restful_idempotent/ 如何保证接⼝的幂等性

http://www.spring4all.com/article/914 说说 CAP 定理、 BASE 理论

http://my.oschina.net/foodon/blog/372703 怎么考虑数据⼀致性问题

https://opentalk.upyun.com/310.html 说说最终⼀致性的实现⽅案

http://www.cnblogs.com/soundcode/p/5590710.html 你怎么看待微服务

http://dockone.io/article/394 微服务与 SOA 的区别

http://dockone.io/article/2399 如何拆分服务

http://dockone.io/article/2516 微服务如何进⾏数据库管理

http://www.uml.org.cn/wfw/201705271.asp 如何应对微服务的链式调⽤异常

http://blog.720ui.com/2017/msa_design/?utm_source=tuicool&utm_medium=referral 对于快速追踪与定位问题

依赖⽇志

微服务的安全

http://dockone.io/article/1507

分布式

谈谈业务中使⽤分布式的场景

https://segmentfault.com/q/1010000006095431/a-1020000006114658 Session 分布式⽅案

https://yq.aliyun.com/articles/387723 分布式锁的场景

https://yq.aliyun.com/articles/465311 分布是锁的实现⽅案

https://yq.aliyun.com/articles/60663 分布式事务

http://www.hollischuang.com/archives/681 集群与负载均衡的算法与实现

https://yq.aliyun.com/articles/218895 说说分库与分表设计

- http://blog.720ui.com/2017/mysql_core_08_multi_db_table/

分库与分表带来的分布式困境与应对之策

- http://blog.720ui.com/2017/mysql_core_09_multi_db_table2/


安全问题

安全要素与 STRIDE 威胁

http://blog.720ui.com/2017/security_stride/ 防范常⻅的 Web 攻击

http://blog.720ui.com/2016/security_web/ 服务端通信安全攻防

http://blog.720ui.com/2016/security_data_transmission/ HTTPS 原理剖析

http://blog.720ui.com/2016/security_https/ HTTPS 降级攻击

http://blog.jobbole.com/106792/ 授权与认证

https://www.jianshu.com/p/cda95dff698c 基于⻆⾊的访问控制

https://www.douban.com/note/259930498/ 基于数据的访问控制

https://www.zhihu.com/question/64888533

性能优化

性能指标有哪些

https://www.douban.com/note/168911628/ 如何发现性能瓶颈

http://blog.csdn.net/shan9liang/article/details/24035001 性能调优的常⻅⼿段

http://blog.csdn.net/jyonghu003/article/details/70055832 说说你在项⽬中如何进⾏性能调优

https://www.jianshu.com/p/08d029607b9a

