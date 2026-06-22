# 摘要：

在Web应⽤中，使⽤JWT替代sesion并不是个好主意

适合JWT的使⽤场景

抱歉，当了回标题党。我并不否认JWT的价值，只是它经常被误⽤。

# 什么是JWT

根据维基百科的定义，JSON WEB Token（JWT，读作 [/dʒɒt/]），是⼀种基于JSON的、⽤于在⽹络 上声明某种主张的令牌（token）。JWT通常由三部分组成: 头信息（header）, 消息体（payload）和 签名（signature）。

头信息指定了该JWT使⽤的签名算法:

header = '{"alg":"HS256","typ":"JWT"}'

HS256 表示使⽤了 HMAC-SHA256 来⽣成签名。

消息体包含了JWT的意图：

payload = '{"loggedInAs":"admin","iat":1422779638}'//iat表示令牌⽣成的时间

未签名的令牌由base64url编码的头信息和消息体拼接⽽成（使⽤"."分隔），签名则通过私有的key计 算⽽成：

key = 'secretkey' unsignedToken = encodeBase64(header) + '.' + encodeBase64(payload) signature = HMAC-SHA256(key, unsignedToken)

最后在未签名的令牌尾部拼接上base64url编码的签名（同样使⽤"."分隔）就是JWT了：

token = encodeBase64(header) + '.' + encodeBase64(payload) + '.' + encodeBase64(signature)

# token看起来像这样: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsb2dnZWRJbkFzIjoiYWRtaW4iLCJpYXQiOjE0MjI3Nzk2Mzh9.gz SraSYS8EXBxLN_oWnFSRgCzcmJmMjLiuyu5CSpyHI

JWT常常被⽤作保护服务端的资源（resource），客户端通常将JWT通过HTP的Authorization header发送给服务端，服务端使⽤⾃⼰保存的key计算、验证签名以判断该JWT是否可信：

Authorization: Bearer eyJhbGci*...<snip>...*yu5CSpyHI

# 那怎么就误⽤了呢

近年来RESTful API开始⻛靡，使⽤HTP header来传递认证令牌似乎变得理所应当，⽽单⻚应⽤ （SPA）、前后端分离架构似乎正在促成越来越多的WEB应⽤放弃历史悠久的cokie-sesion认证机 制，转⽽使⽤JWT来管理⽤户sesion。⽀持该⽅案的⼈认为：

- 1.该⽅案更易于⽔平扩展


在cokie-sesion⽅案中，cokie内仅包含⼀个sesion标识符，⽽诸如⽤户信息、授权列表等都保存 在服务端的sesion中。如果把sesion中的认证信息都保存在JWT中，在服务端就没有sesion存在的 必要了。当服务端⽔平扩展的时候，就不⽤处理sesion复制（sesion replication）/ sesion黏连 （sticky sesion）或是引⼊外部sesion存储了。

<table>
  <tr>
    <th>![image 1](<讲真，别再使用JWT了！.note_images/imageFile1.png>)</th>
  </tr>
</table>


从这个⻆度来说，这个优点确实存在，但实际上外部sesion存储⽅案已经⾮常成熟了（⽐如 Redis），在⼀些Framework的帮助下（⽐如 和 ），sesion复制也并没有想 象中的麻烦。所以除⾮你的应⽤访问量⾮常⾮常⾮常（此处省略N个⾮常）⼤，使⽤cokie-sesion配 合外部sesion存储完全够⽤了。

spring-sesion hazelcast

- 2.该⽅案可防护CSRF攻击


跨站请求伪造 （简称CSRF, 读作 [sea-surf]）是⼀种典型的利⽤cokiesesion漏洞的攻击，这⾥借⽤ 的⼀个例⼦来解释CSRF：

Cros-site request forgery spring-security

假设你经常使⽤bank.example.com进⾏⽹上转账，在你提交转账请求时bank.example.com的前端代 码会提交⼀个HTP请求:

POST /transfer HTTP/1.1 Host: bank.example.com cookie: JsessionID=randomid; Domain=bank.example.com; Secure; HttpOnly Content-Type: application/x-www-form-urlencoded

amount=100.00&routingNumber=1234&account=9876

你图⽅便没有登出bank.example.com，随后⼜访问了⼀个恶意⽹站，该⽹站的HTML⻚⾯包含了这样 ⼀个表单：

<form action="https://bank.example.com/transfer" method="post"> <input type="hidden" name="amount" value="100.00"/> <input type="hidden" name="routingNumber" value="evilsRoutingNumber"/> <input type="hidden" name="account" value="evilsAccountNumber"/> <input type="submit" value="点击就送!"/>

</form>

你被“点击就送”吸引了，当你点了提交按钮时你已经向攻击者的账号转了10元。现实中的攻击可能更 隐蔽，恶意⽹站的⻚⾯可能使⽤Javascript⾃动完成提交。尽管恶意⽹站没有办法盗取你的sesion cokie（从⽽假冒你的身份），但恶意⽹站向bank.example.com发起请求时，你的cokie会被⾃动发 送过去。

因此，有些⼈认为前端代码将JWT通过HTP header发送给服务端（⽽不是通过cokie⾃动发送）可 以有效防护CSRF。在这种⽅案中，服务端代码在完成认证后，会在HTP response的header中返回 JWT，前端代码将该JWT存放到Local Storage⾥待⽤，或是服务端直接在cokie中保存 HtpOnly=false的JWT。

<table>
  <tr>
    <th>![image 2](<讲真，别再使用JWT了！.note_images/imageFile2.png>)</th>
  </tr>
</table>


在向服务端发起请求时，⽤Javascript取出JWT（否则前端Javascript代码⽆权从cokie中获取数 据），再通过header发送回服务端通过认证。由于恶意⽹站的代码⽆法获取bank.example.com的 cokie/Local Storage中的JWT，这种⽅式确实能防护CSRF，但将JWT保存在cokie/Local Storage中 可能会给另⼀种攻击可乘之机，我们⼀会详细讨论它：跨站脚本攻击⸺XS。

- 3.该⽅案更安全


由于JWT要求有⼀个秘钥，还有⼀个算法，⽣成的令牌看上去不可读，不少⼈误认为该令牌是被加密 的。但实际上秘钥和算法是⽤来⽣成签名的，令牌本身不可读仅是因为base64url编码，可以直接解 码，所以如果JWT中如果保存了敏感的信息，相对cokie-sesion将数据放在服务端来说，更不安 全。

除了以上这些误解外，使⽤JWT管理sesion还有如下缺点：

- 1.
- 2.
- 3.
- 4.


更多的空间占⽤。如果将原存在服务端sesion中的各类信息都放在JWT中保存在客户端，可能造 成JWT占⽤的空间变⼤，需要考虑cokie的空间限制等因素，如果放在Local Storage，则可能受 到XS攻击。

更不安全。这⾥是特指将JWT保存在Local Storage中，然后使⽤Javascript取出后作为HTP header发送给服务端的⽅案。在Local Storage中保存敏感信息并不安全，容易受到跨站脚本攻 击，跨站脚本（Cros site script，简称xs）是⼀种“HTML注⼊”，由于攻击的脚本多数时候是跨 域的，所以称之为“跨域脚本”，这些脚本代码可以盗取cokie或是Local Storage中的数据。可以 从这篇⽂章查看XS攻击的原理解释。

⽆法作废已颁布的令牌。所有的认证信息都在JWT中，由于在服务端没有状态，即使你知道了某 个JWT被盗取了，你也没有办法将其作废。在JWT过期之前（你绝对应该设置过期时间），你⽆ 能为⼒。

不易应对数据过期。与上⼀条类似，JWT有点类似缓存，由于⽆法作废已颁布的令牌，在其过期 前，你只能忍受“过期”的数据。

看到这⾥后，你可能发现，将JWT保存在Local Storage中，并使⽤JWT来管理sesion并不是⼀个好主 意，那有没有可能“正确”地使⽤JWT来管理sesion呢？⽐如：

不再使⽤Local Storage存储JWT，使⽤cokie，并且设置HtpOnly=true，这意味着只能由服务端 保存以及通过⾃动回传的cokie取得JWT，以便防御XS攻击

在JWT的内容中加⼊⼀个随机值作为CSRF令牌，由服务端将该CSRF令牌也保存在cokie中，但设 置HtpOnly=false，这样前端Javascript代码就可以取得该CSRF令牌，并在请求API时作为HTP header传回。服务端在认证时，从JWT中取出CSRF令牌与header中获得CSRF令牌⽐较，从⽽实现 对CSRF攻击的防护

考虑到cokie的空间限制（⼤约4k左右），在JWT中尽可能只放“够⽤”的认证信息，其他信息放在 数据库，需要时再获取，同时也解决之前提到的数据过期问题

这个⽅案看上去是挺不错的，恭喜你，你重新发明了cokie-sesion，可能实现还不⼀定有现有的好。

# 那究竟JWT可以⽤来做什么

我的同事做过⼀个形象的解释：

JWT（其实还有SAML）最适合的应⽤场景就是“开票”，或者“签字”。

在有纸化办公时代，多部⻔、多组织之间的协同⼯作往往会需要拿着A部⻔领导的“签字”或者“盖 章”去B部⻔“使⽤”或者“访问”对应的资源，其实这种“领导签字／盖章”就是JWT，都是⼀种由具有 ⼀定权⼒的实体“签发”并“授权”的“票据”。⼀般的，这种票据具有可验证性（领导签名／盖章可以 被验证，且难于模仿），不可篡改性（涂改过的⽂件不被接受，除⾮在涂改处再次签字确认）；并 且这种票据⼀般都是“⼀次性”使⽤的，在访问到对应的资源后，该票据⼀般会被资源持有⽅收回留 底，⽤于后续的审计、追溯等⽤途。

举两个例⼦：

- 1.
- 2.


员⼯李雷需要请假⼀天，于是填写请假申请单，李雷在获得其主管部⻔领导签字后，将请假单 交给HR部⻔韩梅梅，韩梅梅确认领导签字⽆误后，将请假单收回，并在公司考勤表中做相应 记录。

员⼯李雷和韩梅梅因⼯外出需要使⽤公司汽⻋⼀天，于是填写⽤⻋申请单，签字后李雷将申请 单交给⻋队司机⽼王，乘坐⽼王驾驶的⻋辆外出办事，同时⽼王将⽤⻋申请单收回并存档。

在以上的两个例⼦中，“请假申请单”和“⽤⻋申请单”就是JWT中的payload，领导签字就是base64 后的数字签名，领导是isuer，“HR部⻔的韩梅梅”和“司机⽼王”即为JWT的audience，audience 需要验证领导签名是否合法，验证合法后根据payload中请求的资源给予相应的权限，同时将JWT 收回。

放到系统集成的场景中，JWT更适合⼀次性操作的认证:

服务B你好, 服务A告诉我，我可以操作<JWT内容>, 这是我的凭证（即JWT）

在这⾥，服务A负责认证⽤户身份（相当于上例中领导批准请假），并颁布⼀个很短过期时间的JWT给 浏览器（相当于上例中的请假单），浏览器（相当于上例中的请假员⼯）在向服务B的请求中带上该 JWT，则服务B（相当于上例中的HR员⼯）可以通过验证该JWT来判断⽤户是否有权执⾏该操作。这 样，服务B就成为⼀个安全的⽆状态的服务了。

# 总结

- 1.
- 2.


在Web应⽤中，别再把JWT当做sesion使⽤，绝⼤多数情况下，传统的cokie-sesion机制⼯作 得更好

JWT适合⼀次性的命令认证，颁发⼀个有效期极短的JWT，即使暴露了危险也很⼩，由于每次操 作都会⽣成新的JWT，因此也没必要保存JWT，真正实现⽆状态。

⽂／ThoughtWorks 周宇刚

作者：ThoughtWorks中国 链接：htps:/ w.jianshu.com/p/af8360b83a9f 來源：简书 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

