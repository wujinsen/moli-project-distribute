htps:/blog.csdn.net/nsrainbow/article/details/36945267/

问题

shiro中提供了rememberMe功能，它⽤起来是这样的

UsernamePaswordToken token = new UsernamePaswordToken(loginForm.getUsername(),loginForm.getPasword();

if(loginForm.getRememberMe() != nul & "Y".equals(loginForm.getRememberMe( ){

token.setRememberMe(true); }

你可以⾃⼰设置⼀个标志位，然后根据这个标志位判断⼀下⽤户是否勾选了记住我，如果勾选了就使 ⽤ token.setRememberMe(true) 设置为记住我。 相信很多⼈跟我⼀开始想的⼀样，觉得这样设置完了，然后不退出直接关浏览器再打开浏览器，进⼊ 我们的⽹站就会⾃动登陆。但是结果是：当你重开了浏览器后，进⼊⽹站依然让你输⼊⽤户名和密 码！

那么，究竟这个功能要怎么使⽤呢？

原理解释

shiro对cokie做了什么?

其实你设置了这个rememberMe之后shiro还是有做⼀点事情的，它会⽣成⼀个cokie值叫 rememberMe 并保存在你的浏览器⾥⾯，⽽且这个参数会随着你调⽤ subject.logout() 会被⾃动清 除。这个参数的值是⼀串很⻓的Base64加密过的字符串，⼤概⻓这样 名称： rememberMe

内容： 6gYvaCGZaDXt1c0xwriXj/Uvz6g8OMT3VSaAK4WL0Fvqvkcm0nf3CfTwk WT4EjeS/EoQjRfCPv 4WKUXezQDvoNwVgFMtsLIeYMAfTd17ey5BrZQMxW+xU1lBSDoEM1yOy/i1ENh6eXjmYeQFv0yGbh chGdJWzk5W3MxJjv2SljlW4dkGxOSsol3mucoShzmcQ4VqiDjTcbVfZ7mxSHF/0M1JnXRphi8meDaI m9IwM4Hilgjmai+yzdVHFVDHv/vsU/fZmjb+2tJnBiZ+jrDhl2Elt4qBDKxUKT05cDtXaUZWYQmP1be t2EqTfE8eiofa1+FO3iSTJmEocRLDLPWKSJ26bUWA8wUl/QdpH07Ymq1W0ho8EIdFhOsELxM6oM cj7a/8LVzypJXAXZdMFaNe8cBSN2dXpv4PwiktCs3J9P9vP4XrmYes5x27UmXNqYFk86xQhRjFdJ sw5A9ctDKXzPYvJmWFouo3qT5hugX0uxWALCfWg8MHJnG9w7QgVKM8oy3Xy4Ut8lSvYlA=

这串字符串其实是对你登陆后的 Principal 进⾏了序列化后再Base64的结果。Principal 是 shiro 的⼀个 概念，表示⼀个唯⼀的字符串能表示你这个⽤户的，如果你按照最简单的⽤户名密码登陆的⽅式，并 且使⽤的是 SimpleAuthenticationInfo 对象，那么这个 Principal 其实就是⼀个字符串，就是你的⽤户 名 username 所以这串东⻄解密出来就是你的username

shiro觉得rememberMe不安全

shiro觉得不能把rememberMe等同于已经登陆了，这样不安全。所以shiro 觉得就算 rememberMe = true 也不能算是 authc 的⽽是 user 级别的。 我们⼀般设置路径拦截是这样设置的 /* = authc

这样就保证了所有路径都需要登陆才能访问。就算你是 rememberMe=true也不能访问，官⽅说你如果 设置成拦截级别为user就能访问，⽐如 /* = user

这样就可以访问了，但是官⽅建议不敏感的部分⽤user，敏感的部分还是要让⽤户再登陆⼀次，就像 你上淘宝⽹就算不登陆，只要上⼀次有登陆过，你依然可以直接看我的淘宝那个⻚⾯，但是点击 我的 宝⻉的时候就⼜要让你登陆了。 但是！我们的确有很多时候是 需要记住⽤户就相当于⽤户登录了！ 设置成user这个⽅案还有⼀个问题，就是我们实际项⽬中在登陆后有做了很多设置⽤户上下⽂的⼯ 作，⽐如设置sesion等，如果我们只是设置拦截级别为user，那么再次进⼊的时候虽然可以访问，但 是sesion是空的，我们的⻚⾯必然异常频出。 解决⽅案

前提条件

采⽤这个解决⽅案的前提是，你必须⾃⼰先实现⼀个realm，不过这个我相信⼤家都会实现的，毕竟默 认的不是jdbcRealm ，真正的项⽬都是要查数据库才能确定⽤户是否登录的。那么我就假定⼤家的项 ⽬中都有那么⼀个负责验证登录的 JdbcRealm， 并且是采⽤⽤户名密码认证的，在 doGetAuthenticationInfo ⽅法⾥⾯是采⽤如下的⽅法来做认证

. info = new SimpleAuthenticationInfo(username, pasword.toCharAray(), getName();

这个前提条件保证你的principal是username，相信⼤部分⼈根据教程做shiro的时候都采⽤了这种⽅式 STEP1复写 FormAuthenticationFilter 的 isAcesAlowed ⽅法

做⼀个新类继承FormAuthenticationFilter ，并复写 isAcesAlowed ⽅法 package com.yqr.jxc.shiro;

import javax.anotation.Resource; import javax.servlet.ServletRequest; import javax.servlet.ServletResponse;

import org.apache.shiro.sesion.Sesion; import org.apache.shiro.subject.Subject; import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import com.yqr.jxc.service.global.GlobalUserService;

public clas RememberAuthenticationFilter extends FormAuthenticationFilter {

@Resource(name="globalUserService") private GlobalUserService globalUserService;

/*

- * 这个⽅法决定了是否能让⽤户登录
- */ @Overide protected bolean isAcesAlowed(ServletRequest request, ServletResponse response,


Object mapedValue) { Subject subject = getSubject(request, response);

/如果 isAuthenticated 为 false 证明不是登录过的，同时 isRememberd 为true 证明是没登陆直接 通过记住我功能进来的

if(!subject.isAuthenticated() & subject.isRemembered(){

/获取sesion看看是不是空的 Sesion sesion = subject.getSesion(true);

/随便拿sesion的⼀个属性来看sesion当前是否是空的，我⽤userId，你们的项⽬可以⾃⾏ 发挥

if(sesion.getAtribute("userId") = nul){

/如果是空的才初始化，否则每次都要初始化，项⽬得慢死 /这边根据前⾯的前提假设，拿到的是username

String username = subject.getPrincipal().toString();

/在这个⽅法⾥⾯做初始化⽤户上下⽂的事情，⽐如通过查询数据库来设置sesion值，你们 ⾃⼰发挥

globalUserService.initUserContext(username, subject);

} }

/这个⽅法本来只返回 subject.isAuthenticated() 现在我们加上 subject.isRemembered() 让它同 时也兼容remember这种情况

return subject.isAuthenticated()| subject.isRemembered(); }

} STEP2 设置使⽤这个新的 AuthenticationFilter (认证过滤器)

如果你⽤的是spring那么 <!- 整合了rememberMe功能的filter-> <bean id="rememberAuthFilter" clas="com.yqr.jxc.shiro.RememberAuthenticationFilter" ></bean>

<!-将之前的 /* = authc 替换成 rememberAuthFilter

. /* = rememberAuthFilter

.

如果你⽤的是 ini ⽂件，那么 rememberAuthFilter=com.yqr.jxc.shiro.RememberAuthenticationFilter

#将之前的 /* = authc 替换成 rememberAuthFilter

. /* = rememberAuthFilter

然后重启项⽬我们来测试⼀下，先登录⼀次系统，然后直接关掉浏览器，然后打开浏览器直接输⼊系 统某个⻚⾯的地址，发现可以直接进去了，sesion什么的也设置好了

看起来很美？但是！

忙活了半天，最后我还是决定在我的系统中撤下了这个功能。为什么呢？因为这个功能有个致命的安 全缺陷就是随便谁把这个cokie值拿到别的浏览器都可以登录。就算你⽤再⽜逼的加密，或者是这个 cokie值根据浏览器的各个别的属性来达到仅供这个浏览器使⽤，但是对于⿊客来说，只要你是通过 表单把东⻄发送出去，这整个表单都是可以伪造的。就算是增加了过期时间，在这段时间之内还是有 被伪造的⻛险，我⽬前没有想到什么好的解决⽅案。 唯⼀能想到的就是对于使⽤场景的选择，在严格的业务系统中不能使⽤记住我这个功能，在⾮严格的 系统中，⽐如不敏感的系统，像看看流量看看微博之类的，还是可以使⽤以上的⽅式来解决 rememberMe的问题的。 所以，请谨慎选择是否要将 rememberMe 功能范围扩⼤化！

最后感谢来⾃俄罗斯的 meri 的这篇精辟的shiro研究⽂ htp:/meristuf.blogspot.com/201/03/apache-shiro-part-1-basics.html 本⽂是根据meri 和 blurblurNick 精彩的问答写成的

⸻版权声明：本⽂为CSDN博主「alexiyang」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上原 ⽂出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/nsrainbow/article/details/36945267/

