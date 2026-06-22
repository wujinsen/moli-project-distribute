# 什么是JWT

Json web token (JWT), 是为了在⽹络应⽤环境间传递声明⽽执⾏的⼀种基于JSON的开放标准（

).该token被设计为紧凑且安全的，特别适⽤于分布式站点的单点登录（ SO）场景。 JWT的声明⼀般被⽤来在身份提供者和服务提供者间传递被认证的⽤户身份信息，以便于从资源服 务器获取资源，也可以增加⼀些额外的其它业务逻辑所必须的声明信息，该token也可直接被⽤于 认证，也可被加密。

(RFC 7519

## 起源

说起JWT，我们应该来谈⼀谈基于token的认证和传统的sesion认证的区别。

传统的sesion认证

我们知道，htp协议本身是⼀种⽆状态的协议，⽽这就意味着如果⽤户向我们的应⽤提供了⽤户名和密 码来进⾏⽤户认证，那么下⼀次请求时，⽤户还要再⼀次进⾏⽤户认证才⾏，因为根据htp协议，我们 并不能知道是哪个⽤户发出的请求，所以为了让我们的应⽤能识别是哪个⽤户发出的请求，我们只能 在服务器存储⼀份⽤户登录的信息，这份登录信息会在响应时传递给浏览器，告诉其保存为cokie,以 便下次请求时发送给我们的应⽤，这样我们的应⽤就能识别请求来⾃哪个⽤户了,这就是传统的基于 sesion认证。

但是这种基于sesion的认证使应⽤本身很难得到扩展，随着不同客户端⽤户的增加，独⽴的服务器已 ⽆法承载更多的⽤户，⽽这时候基于sesion认证应⽤的问题就会暴露出来.

基于sesion认证所显露的问题

Sesion: 每个⽤户经过我们的应⽤认证之后，我们的应⽤都要在服务端做⼀次记录，以⽅便⽤户下次 请求的鉴别，通常⽽⾔sesion都是保存在内存中，⽽随着认证⽤户的增多，服务端的开销会明显增 ⼤。

扩展性: ⽤户认证之后，服务端做认证记录，如果认证的记录被保存在内存中的话，这意味着⽤户下次 请求还必须要请求在这台服务器上,这样才能拿到授权的资源，这样在分布式的应⽤上，相应的限制了 负载均衡器的能⼒。这也意味着限制了应⽤的扩展能⼒。

CSRF: 因为是基于cokie来进⾏⽤户识别的, cokie如果被截获，⽤户就会很容易受到跨站请求伪造的 攻击。

## 基于token的鉴权机制

基于token的鉴权机制类似于htp协议也是⽆状态的，它不需要在服务端去保留⽤户的认证信息或者会 话信息。这就意味着基于token认证机制的应⽤不需要去考虑⽤户在哪⼀台服务器登录了，这就为应⽤ 的扩展提供了便利。

流程上是这样的：

⽤户使⽤⽤户名密码来请求服务器

服务器进⾏验证⽤户的信息

服务器通过验证发送给⽤户⼀个token

客户端存储token，并在每次请求时附送上这个token值

服务端验证token值，并返回数据

这个token必须要在每次请求时传递给服务端，它应该保存在请求头⾥， 另外，服务端要⽀持CORS(跨来 源资源共享)策略，⼀般我们在服务端这么做就可以了Access-Control-Allow-Origin: *。

那么我们现在回到JWT的主题上。

## JWT⻓什么样？

JWT是由三段信息构成的，将这三段信息⽂本⽤.链接⼀起就构成了Jwt字符串。就像这样:

eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWR taW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ

## JWT的构成

第⼀部分我们称它为头部（header),第⼆部分我们称其为载荷（payload, 类似于⻜机上承载的物品)， 第三部分是签证（signature).

header

jwt的头部承载两部分信息：

声明类型，这⾥是jwt

声明加密的算法 通常直接使⽤ HMAC SHA256

完整的头部就像下⾯这样的JSON：

{

'typ': 'JWT', 'alg': 'HS256'

}

然后将头部进⾏base64加密（该加密是可以对称解密的),构成了第⼀部分.

eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9

playload

载荷就是存放有效信息的地⽅。这个名字像是特指⻜机上承载的货品，这些有效信息包含三个部分

标准中注册的声明

公共的声明

私有的声明

标准中注册的声明 (建议但不强制使⽤) ：

is: jwt签发者

sub: jwt所⾯向的⽤户

aud: 接收jwt的⼀⽅

exp: jwt的过期时间，这个过期时间必须要⼤于签发时间

nbf: 定义在什么时间之前，该jwt都是不可⽤的.

iat: jwt的签发时间

jti: jwt的唯⼀身份标识，主要⽤来作为⼀次性token,从⽽回避重放攻击。

公共的声明 ：

公共的声明可以添加任何的信息，⼀般添加⽤户的相关信息或其他业务需要的必要信息.但不建议添加 敏感信息，因为该部分在客户端可解密.

私有的声明 ：

私有声明是提供者和消费者所共同定义的声明，⼀般不建议存放敏感信息，因为base64是对称解密 的，意味着该部分信息可以归类为明⽂信息。

定义⼀个payload:

{

"sub": "1234567890", "name": "John Doe", "admin": true

}

然后将其进⾏base64加密，得到Jwt的第⼆部分。

eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9

### signature

jwt的第三部分是⼀个签证信息，这个签证信息由三部分组成：

header (base64后的)

payload (base64后的)

secret

这个部分需要base64加密后的header和base64加密后的payload使⽤.连接组成的字符串，然后通过 header中声明的加密⽅式进⾏加盐secret组合加密，然后就构成了jwt的第三部分。

// javascript var encodedString = base64UrlEncode(header) + '.' + base64UrlEncode(payload);

var signature = HMACSHA256(encodedString, 'secret'); // TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ

将这三部分⽤.连接成⼀个完整的字符串,构成了最终的jwt:

eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWR taW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ

注意：secret是保存在服务器端的，jwt的签发⽣成也是在服务器端的，secret就是⽤来进⾏jwt的签 发和jwt的验证，所以，它就是你服务端的私钥，在任何场景都不应该流露出去。⼀旦客户端得知这个 secret, 那就意味着客户端是可以⾃我签发jwt了。

### 如何应⽤

⼀般是在请求头⾥加⼊Authorization，并加上Bearer标注：

fetch('api/user/1', { headers: {

'Authorization': 'Bearer ' + token }

})

服务端会验证token，如果验证通过就会返回相应的资源。整个流程就是这样的:

<table>
  <tr>
    <th>![image 1](<什么是 JWT -- JSON WEB TOKEN.note_images/imageFile1.png>)</th>
  </tr>
</table>


jwt-diagram

## 总结

优点

因为json的通⽤性，所以JWT是可以进⾏跨语⾔⽀持的，像JAVA,JavaScript,NodeJS,PHP等很多语 ⾔都可以使⽤。

因为有了payload部分，所以JWT可以在⾃身存储⼀些其他业务逻辑所必要的⾮敏感信息。

便于传输，jwt的构成⾮常简单，字节占⽤很⼩，所以它是⾮常便于传输的。

它不需要在服务端保存会话信息, 所以它易于应⽤的扩展

安全相关

不应该在jwt的payload部分存放敏感信息，因为该部分是客户端可解密的部分。

保护好secret私钥，该私钥⾮常重要。

如果可以，请使⽤htps协议

作者：Dearmadman 链接：htps:/ w.jianshu.com/p/576dbf4b2ae 來源：简书 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

