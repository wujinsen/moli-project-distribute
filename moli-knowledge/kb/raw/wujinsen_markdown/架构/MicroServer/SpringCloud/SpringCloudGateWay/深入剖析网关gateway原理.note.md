htps:/ w.jb51.net/article/ 21046.htm

# ⽬录

⼀ 背景

微服务架构提供⼀种简单有效的统⼀的 API⼊⼝

⼆需要的依赖：

添加配置⽂件及说明：

说明

路由(Route) 是 gateway 中最基本的组件之⼀

什么是⽹关？

为什么使⽤⽹关？

Gateway 服务的启动底层是通过谁去实现的？

Gateway 服务做请求转发时⼀定要在注册中⼼进⾏注册吗？

三 负载均衡

⽹关层⾯是如何实现负载均衡的？

⽹关层⾯是如何通过服务名查找服务实例的？

你了解Ri bon中的哪些负载均衡算法?

⽹关进⾏请求转发的流程是怎样，有哪些关键对象？

⽹关层⾯服务的映射⽅式怎样的？

⽹关层如何记录服务的映射？

⽹关(Gateway)

诞⽣的背景? ⽹关的选型? Spring Cloud Gateway的⼊⻔实现

Spring Cloud Gateway中的负载均衡? Spring Cloud Gateway中的断⾔配置? Spring Cloud Gateway中的过滤器配置?

Spring Cloud Gateway中的限流设计?

Gateway在互联⽹架构中的位置?

Gateway底层负载均衡的实现?

Gateway应⽤过程中设计的主要概念?

Gateway中你做过哪些断⾔配置? Gateway中你⽤的过滤器有哪些?

⼀ 背景 微服务架构提供⼀种简单有效的统⼀的 API⼊⼝ 负责服务请求路由、组合及协议转换，并且基于 Filter 链的⽅式提供了权限认证，监控、限流等功能。

优点：

性能强劲：是第⼀代⽹关Zul的1.6倍。 功能强⼤：内置了很多实⽤的功能，例如转发、监控、限流等设计优雅，容易扩展。

缺点：

依赖Nety与WebFlux(Spring5.0)，不是传统的Servlet编程模型(Spring MVC就是基于此模型实现)，学 习成本⾼。需要Spring Bot 2.0及以上的版本，才⽀持 ⼆需要的依赖：

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br></th>
    <th><dependency><br><br><groupId>org.springframew ork.cloud</groupId><br><br><artifactId>spring-cloudstarter-gateway</artifactId><br><br></dep ndency><br><br></th>
  </tr>
</table>


e

添加配置⽂件及说明： server: port: 9 0 spring: aplication: name: sca-gateway cloud: gateway: routes: #配置⽹关路由规则

- - id: route01 #路由id,⾃⼰指定⼀个唯⼀值即可 uri: htp:/localhost:8081/ #⽹关帮我们转发的url predicates: #断⾔(谓此):匹配请求规则

- - Path=/nacos/provider/echo/* #请求路径定义,此路径对应uri中的资源 filters: #⽹关过滤器,⽤于对谓词中的内容进⾏判断分析以及处理

- - StripPrefix=1 #转发之前去掉path中第⼀层路径，例如nacos


说明 路由(Route) 是 gateway 中最基本的组件之⼀ 表示⼀个具体的路由信息载体。 主要定义了下⾯的⼏个信息: id，路由标识符，区别于其他 Route。 uri，路由指向的⽬的地 uri，即客户端请求最终被转发到的微服务。 predicate，断⾔(谓词)的作⽤是进⾏条件判断，只有断⾔都返回真，才会执⾏路由。 filter，过滤器⽤于修改请求和响应信息。 什么是⽹关？

服务访问(流量)的⼀个⼊⼝，类似⽣活中的“海关“ 为什么使⽤⽹关？ 服务安全，统⼀服务⼊⼝管理，负载均衡，限流，鉴权 Spring Cloud Gateway 应⽤的初始构建过程(添加依赖，配置 Gateway 服务的启动底层是通过谁去实现的？ Nety⽹络编程框架-ServerSocket Gateway 服务做请求转发时⼀定要在注册中⼼进⾏注册吗？ 不⼀定，可以直接通过远端url进⾏服务访问 三 负载均衡 需要的porm⽂件是nacos的配置和包，是通过nacos配置中⼼，寻找实例。

⽹关层⾯是如何实现负载均衡的？ 通过服务名去查找具体的服务实例 ⽹关层⾯是如何通过服务名查找服务实例的？

Ri bon 你了解Ri bon中的哪些负载均衡算法? 轮询，权重，hash, …可通过IRule接⼝进⾏查看分析 ⽹关进⾏请求转发的流程是怎样，有哪些关键对象？ XxHandlerMaping，Handler，。。。 ⽹关层⾯服务的映射⽅式怎样的？ 谓词-path，…,服务名/服务实例 ⽹关层如何记录服务的映射？ 通过map，并要考虑读写锁的应⽤ 下图是定义在⽹关层⾯定义全局过滤器

<table>
  <tr>
    <th>![image 1](<深入剖析网关gateway原理.note_images/imageFile1.png>)</th>
  </tr>
</table>


⽹关(Gateway) 诞⽣的背景? 第⼀:统⼀微服务访问的⼊⼝, 第⼆:对系统服务进⾏保护, 第三进⾏统⼀的认证,授权,限流 ⽹关的选型? Netifix Zul,Spring Cloud Gateway,… Spring Cloud Gateway的⼊⻔实现 添加依赖,路由配置,启动类 Spring Cloud Gateway中的负载均衡? ⽹关服务注册,服务的发现,基于uri:lb:/服务id⽅式访问具体服务实例 Spring Cloud Gateway中的断⾔配置? 掌握常⽤⼏个就可,⽤时可以通过搜索引擎去查 Spring Cloud Gateway中的过滤器配置? 掌握过滤器中的两⼤类型-局部和全局 Spring Cloud Gateway中的限流设计? Sentinel Gateway在互联⽹架构中的位置? nginx->gateway–>微服务–>微服务 Gateway底层负载均衡的实现? Ri bon Gateway应⽤过程中设计的主要概念? 路由id,路由uri,断⾔,过滤器 Gateway中你做过哪些断⾔配置? after,header,path,cokie,… Gateway中你⽤的过滤器有哪些? 添加前缀,去掉前缀,添加请求头,…,负载均衡,… 以上就是深⼊理解⽹关gateway的详细内容，更多关于⽹关gateway的资料请关注脚本之家其它相关⽂ 章！ 您可能感兴趣的⽂章:

gateway⽹关接⼝请求的校验⽅式 Gateway⽹关⼯作原理及使⽤⽅法

