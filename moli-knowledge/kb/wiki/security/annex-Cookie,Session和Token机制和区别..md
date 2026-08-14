---
title: Cookie,Session和Token机制和区别..note（原文插图 annex）
slug: annex-Cookie,Session和Token机制和区别.
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/javaweb/jwt/Cookie,Session和Token机制和区别..note.md
related: [api-接口安全设计]
created: 2026-07-05
updated: 2026-07-05
---

# 1.背景介绍

由于HTP是⼀种⽆状态协议,服务器没有办法单单从⽹络连接上⾯知道访问者的身份,为了解决这个问 题,就诞⽣了Cokie Cokie实际上是⼀⼩段的⽂本信息。客户端请求服务器，如果服务器需要记录该⽤户状态，就使⽤ response向客户端浏览器颁发⼀个Cokie 客户端浏览器会把Cokie保存起来。当浏览器再请求该⽹站时，浏览器把请求的⽹址连同该Cokie⼀ 同提交给服务器。服务器检查该Cokie， 以此来辨认⽤户状态。服务器还可以根据需要修改Cokie的内容。 实际就是颁发⼀个通⾏证，每⼈⼀个，⽆论谁访问都必须携带⾃⼰通⾏证。这样服务器就能从通⾏证 上确认客户身份了。这就是Cokie的⼯作原理 cokie 可以让服务端程序跟踪每个客户端的访问，但是每次客户端的访问都必须传回这些 Cokie，如 果 Cokie 很多，这⽆形地增加了客户端与服务端的数据传输量， ⽽ Sesion 的出现正是为了解决这个问题。同⼀个客户端每次和服务端交互时，不需要每次都传回所 有的 Cokie 值，⽽是只要传回⼀个 ID，这个 ID 是客户端第⼀次访问服务器的时候⽣成的， ⽽且每个客户端是唯⼀的。这样每个客户端就有了⼀个唯⼀的 ID，客户端只要传回这个 ID 就⾏了，这 个 ID 通常是 NANE 为 JSESIONID 的⼀个 Cokie。 Sesion翻译过来为会话的意思 ,这⾥的sesion指的是客户端与服务器之间保存状态的解决⽅案.

# 2.知识剖析

cokie机制

<table>
  <tr>
    <th>![image 1](assets/imageFile1.png)</th>
  </tr>
</table>


cokie的内容主要包括name(名字)、value(值)、maxAge(失效时间)、path(路径),domain(域)和secure name：cokie的名字，⼀旦创建，名称不可更改。 value：cokie的值，如果值为Unicode字符，需要为字符编码。如果为⼆进制数据，则需要使⽤ BASE64编码. maxAge：cokie失效时间，单位秒。如果为正数，则该cokie在maxAge后失效。如果为负数，该 cokie为临时cokie，关闭浏览器即失效， 浏览器也不会以任何形式保存该cokie。如果为0，表示删除该cokie。默认为-1 path：该cokie的使⽤路径。如果设置为"/sesionWeb/"，则只有ContextPath为“/sesionWeb/”的程 序可以访问该cokie。如果设置为“/”，则本域名下ContextPath都可以访问该cokie。 domain:域.可以访问该Cokie的域名。第⼀个字符必须为".",如果设置为".gogle.com",则所有 以"gogle.com结尾的域名都可以访问该cokie",如果不设置,则为所有域名 secure：该cokie是否仅被使⽤安全协议传输。 Sesion机制 Sesion机制是⼀种服务端的机制，服务器使⽤⼀种类似散列表的结构来保存信息。 当程序需要为某个客户端的请求创建⼀个sesion的时候，服务器⾸先检查这个客户端⾥的请求⾥是否 已包含了⼀个sesion标识 -sesionID， 如果已经包含⼀个sesionID，则说明以前已经为此客户端创建过sesion，服务器就按照sesionID把 这个sesion检索出来使⽤ 如果客户端请求不包含sesionID，则为此客户端创建⼀个sesion并且声称⼀个与此sesion相关联的 sesionID， sesionID的值应该是⼀个既不会重复，⼜不容易被找到规律以仿造的字符串(服务器会⾃动创建),这个 sesionID将被在本次响应中返回给客户端保存。

# 3.常⻅问题

使⽤cokie的弊端 使⽤sesion的弊端 cokie和sesion的区别

# 4.解决⽅案

使⽤cokie的缺点 如果浏览器使⽤的是 cokie，那么所有的数据都保存在浏览器端，

cokie可以被⽤户禁⽌ cokie不安全(对于敏感数据，需要加密) cokie只能保存少量的数据(⼤约是4k)，cokie的数量也有限制(⼤约是⼏百个)，不同浏览器设置不⼀ 样，反正都不多 cokie只能保存字符串 对服务器压⼒⼩ 使⽤sesion的缺点 ⼀般是寄⽣在Cokie下的，当Cokie被禁⽌，Sesion也被禁⽌ 当然可以通过url重写来摆脱cokie 当⽤户访问量很⼤时，对服务器压⼒⼤ 我们现在知道sesion是将⽤户信息储存在服务器上⾯,如果访问服务器的⽤户越来越多,那么服务器上 ⾯的sesion也越来越多, sesion会对服务器造成压⼒，影响服务器的负载.如果Sesion内容过于复 杂，当⼤量客户访问服务器时还可能会导致内存溢出。 ⽤户信息丢失, 或者说⽤户访问的不是这台服务器的情况下,就会出现数据库丢失. cokie和sesion的区别 具体来说cokie机制采⽤的是在客户端保持状态的⽅案，⽽sesion机制采⽤的是在服务器端保持状态 的⽅案。同时我们也看到， 由于采⽤服务器端保持状态的⽅案在客户端也需要保存⼀个标识，所以sesion机制可能需要借助于 cokie机制来达到保存标识的⽬的 cokie不是很安全，别⼈可以分析存放在本地的cokie并进⾏cokie欺骗，考虑到安全应当使⽤ sesion sesion会在⼀定时间内保存在服务器上。当访问增多，会⽐较占⽤你服务器的性能，考虑到减轻服务 器性能⽅⾯，应当使⽤cokie 单个cokie保存的数据不能超过4k,很多浏览器都限制⼀个站点最多保存20个cokie。 可以将登陆信息等重要信息存放为sesion。

- 5.编码实战
- 6.扩展思考


其他⼏种认证登录⽅式

HTPBasicAuth

HTP Basic Auth简单点说明就是每次请求API时都提供⽤户的username和pasword，简⾔之，Basic Auth是配合RESTful API 使⽤的最简单的认证⽅式，只需提供⽤户名密码即可，但由于有把⽤户名密码 暴露给第三⽅客户端的⻛险，在⽣产环境下被使⽤的越来越少。因此，在开发对外开放的RESTful API 时，尽量避免采⽤HTP Basic Auth

### OAuth

OAuth（开放授权）是⼀个开放的授权标准，允许⽤户让第三⽅应⽤访问该⽤户在某⼀web服务上存储 的私密的资源（如照⽚，视频，联系⼈列表），⽽⽆需将⽤户名和密码提供给第三⽅应⽤。 OAuth允许⽤户提供⼀个令牌，⽽不是⽤户名和密码来访问他们存放在特定服务提供者的数据。每⼀个 令牌授权⼀个特定的第三⽅系统（例如，视频编辑⽹站)在特定的时段（例如，接下来的2⼩时内）内 访问特定的资源（例如仅仅是某⼀相册中的视频）。 这样，OAuth让⽤户可以授权第三⽅⽹站访问他们存储在另外服务提供者的某些特定信息，⽽⾮所有内 容

<table>
  <tr>
    <th>![image 2](assets/imageFile2.png)</th>
  </tr>
</table>


#### OAuth的认证机制适⽤于个⼈消费者类的互联⽹产品，如社交类AP等应⽤，但是不太适合拥有⾃有认 证权限管理的企业应⽤； JWT的Token认证

<table>
  <tr>
    <th>![image 3](assets/imageFile3.png)</th>
  </tr>
</table>


⼀个JWT实际上就是⼀个字符串，它由三部分组成，头部、载荷与签名。 JWT的头部⽤于描述关于该JWT的最基本的信息，例如其类型以及签名所⽤的算法等。这也可以被表 示成⼀个JSON对象。 "typ": "JWT", "alg": "HS256" 当然头部要进⾏BASE64编码 签名（Signature） 将上⾯的两个编码后的字符串都⽤句号.连接在⼀起（头部在前）例如头部使⽤base64编码后为 123.456 我们将上⾯拼接完的字符串⽤HS256算法进⾏加密。在加密的时候，还需要我们⾃⼰提供⼀个密钥 （secret)。 得到789. 将他们完全拼在⼀起,我们就得到了完整的JWT"123.456.789" 在我们的请求URL中会带上这串JWT字 符串 载荷 is: 该JWT的签发者， 是否使⽤是可选的； sub: 该JWT所⾯向的⽤户，是否使⽤是可选的； aud: 接收该JWT的⼀⽅， 是否使⽤是可选的； exp(expires): 什么时候过期，这⾥是⼀个Unix时间戳，是否使⽤是可选的； iat(isued at): 在什么时候签发的(UNIX时间)，是否使⽤是可选的； nbf (Not Before)：如果当前时间在nbf⾥的时间之前，则Token不被接受；⼀般都会留⼀些余地，⽐如 ⼏分钟；，是否使⽤是可选的； JWT机制实现认证

<table>
  <tr>
    <th>![image 4](assets/imageFile4.png)</th>
  </tr>
</table>


ection>

对Token认证的五点认识 对Token认证机制有5点直接注意的地⽅： ⼀个Token就是⼀些信息的集合； 在Token中包含⾜够多的信息，以便在后续请求中减少查询数据库的⼏率； 服务端需要对cokie和HTP Authrorization Header进⾏Token信息的检查； 基于上⼀点，你可以⽤⼀套token认证代码来⾯对浏览器类客户端和⾮浏览器类客户端； 因为token是被签名的，所以我们可以认为⼀个可以解码认证通过的token是由我们系统发放的，其中 带的信息是合法有效的； Token机制相对于Cokie机制⼜有什么好处呢？ ⽀持跨域访问:Cokie是不允许垮域访问,这⼀点对Token机制是不存在的,前提是传输的⽤户认证信息通 过HTP头传输. ⽆状态(也称：服务端可扩展⾏):Token机制在服务端不需要存储sesion信息，因为Token ⾃身包含了 所有登录⽤户的信息，只需要在客户端的cokie或本地介质存储状态信息. 更适⽤CDN: 可以通过内容分发⽹络请求你服务端的所有资料（如：javascript，HTML,图⽚等），⽽ 你的服务端只要提供API即可. 去耦: 不需要绑定到⼀个特定的身份验证⽅案。Token可以在任何地⽅⽣成，只要在你的API被调⽤的时 候，你可以进⾏Token⽣成调⽤即可. 更适⽤于移动应⽤: 当你的客户端是⼀个原⽣平台（iOS, Android，Windows 8等）时，Cokie是不被 ⽀持的（你需要通过Cokie容器进⾏处理），这时采⽤Token认证机制就会简单得多。 CSRF:因为不再依赖于Cokie，所以你就不需要考虑对CSRF（跨站请求伪造）的防范。 性能: ⼀次⽹络往返时间（通过数据库查询sesion信息）总⽐做⼀次HMACSHA256计算 的Token验证 和解析要费时得多. 不需要为登录⻚⾯做特殊处理: 如果你使⽤Protractor 做功能测试的时候，不再需要为登录⻚⾯做特殊 处理. 基于标准化:你的API可以采⽤标准化的 JSON Web Token (JWT). 这个标准已经存在多个后端库 （.NET, Ruby, Java,Python, PHP）和多家公司的⽀持（如：Firebase,Gogle, Microsoft）.

# 7.参考⽂献

百度 htp:/blog.csdn.net/fangaoxin/article/details/6952954/ htp:/ w.cnblogs.com/xiekeli/p/5607107.html

## 今天的分享就到这⾥啦，欢迎⼤家点赞、转发、留⾔、拍砖~

技能树.IT修真院 “我们相信⼈⼈都可以成为⼀个⼯程师，现在开始，找个师兄，带你⼊⻔，掌控⾃⼰学习的节奏，学习 的路上不再迷茫”。 这⾥是技能树.IT修真院，成千上万的师兄在这⾥找到了⾃⼰的学习路线，学习透明化，成⻓可⻅化， 师兄1对1免费指导。快来与我⼀起学习吧~

w.jnshu.com/login/1/1470868 PT

作者：⿊⽩电影_ 链接：htps:/ w.jianshu.com/p/013f810cdb75 來源：简书 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。
