⽬录

- 1.
- 2. ⼀、背景知识：


SAML 作⽤ SAML框架

SAML即安全断⾔标记语⾔，英⽂全称是Security Asertion Markup Language。它是⼀个基于 XML的标准，⽤于在不同的安全域(security domain)之间交换认证和授权数据。在SAML标准定义了身 份提供者(identity provider)和服务提供者(service provider)，这两者构成了前⾯所说的不同的安全 域。 SAML是OASIS组织安全服务技术委员会(Security Services Technical Comite)的产品。

SAML（Security Asertion Markup Language）是⼀个XML框架，也就是⼀组协议，可以⽤来传输 安全声明。⽐如，两台远程机器之间要通讯，为了保证安全，我们可以采⽤加密等措施，也可以采⽤ SAML来传输，传输的数据以XML形式，符合SAML规范，这样我们就可以不要求两台机器采⽤什么样 的系统，只要求能理解SAML规范即可，显然⽐传统的⽅式更好。SAML 规范是⼀组Schema 定义。 可以这么说，在Web Service 领域，schema就是规范，在Java领域，API就是规范。

SAML 作⽤

SAML 主要包括三个⽅⾯：

- 1.认证申明。表明⽤户是否已经认证，通常⽤于单点登录。
- 2.属性申明。表明 某个Subject 的属性。
- 3.授权申明。表明 某个资源的权限。


SAML框架

SAML就是客户向服务器发送SAML 请求，然后服务器返回SAML响应。数据的传输以符合SAML规范 的XML格式表示。 SAML 可以建⽴在SOAP上传输，也可以建⽴在其他协议上传输。 因为SAML的规范由⼏个部分构成：SAML Asertion，SAML Prototol，SAML binding等

安全 由于SAML在两个拥有共享⽤户的站点间建⽴了信任关系，所以安全性是需考虑的⼀个⾮常重要的因 素。SAML中的安全弱点可能危及⽤户在⽬标站点的个⼈信息。SAML依靠⼀批制定完善的安全标准， 包括 SL和X.509，来保护SAML源站点和⽬标站点之间通信的安全。源站点和⽬标站点之间的所有通 信都经过了加密。为确保参与SAML交互的双⽅站点都能验证对⽅的身份，还使⽤了证书。

应⽤ ⽬前SAML已经在很多商业/开源产品得到应⽤推⼴，主要有：

IBM Tivoli Aces Manager Weblogic Oblix NetPoint SunONE Identity Server

Baltimore, SelectAces Entegrity Solutions AsureAces Internet2 OpenSAML Yale CAS 3 Netegrity SiteMinder Sigaba Secure Mesaging Solutions RSA Security ClearTrust VeriSign Trust Integration Tolkit Entrust GetAces 7

⼆、基于 SAML的 SO 下⾯简单介绍使⽤基于SAML的 SO登录到WebAp1的过程（下图源⾃SAML 的 Gogle Aps SO， 笔者偷懒，简单做了修改）

![image 1](<基于SAML的单点登录介绍.note_images/imageFile1.png>)

此图⽚说明了以下步骤。

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


⽤户尝试访问WebAp1。 WebAp1 ⽣成⼀个 SAML 身份验证请求。SAML 请求将进⾏编码并嵌⼊到 SO 服务的⽹址中。 包含⽤户尝试访问的 WebAp1 应⽤程序的编码⽹址的 RelayState 参数也会嵌⼊到 SO ⽹址中。 该 RelayState 参数作为不透明标识符，将直接传回该标识符⽽不进⾏任何修改或检查。 WebAp1将重定向发送到⽤户的浏览器。重定向⽹址包含应向 SO 服务提交的编码 SAML 身份验 证请求。

SO（统⼀认证中⼼或叫Identity Provider）解码 SAML 请求，并提取 WebAp1的 ACS（声明客 户服务）⽹址以及⽤户的⽬标⽹址（RelayState 参数）。然后，统⼀认证中⼼对⽤户进⾏身份验 证。统⼀认证中⼼可能会要求提供有效登录凭据或检查有效会话 Cokie 以验证⽤户身份。 统⼀认证中⼼⽣成⼀个 SAML 响应，其中包含经过验证的⽤户的⽤户名。按照 SAML 2.0 规范， 此响应将使⽤统⼀认证中⼼的 DSA/RSA 公钥和私钥进⾏数字签名。 统⼀认证中⼼对 SAML 响应和 RelayState 参数进⾏编码，并将该信息返回到⽤户的浏览器。统⼀ 认证中⼼提供了⼀种机制，以便浏览器可以将该信息转发到 WebAp1 ACS。 WebAp1使⽤统⼀认证中⼼的公钥验证 SAML 响应。如果成功验证该响应，ACS 则会将⽤户重定 向到⽬标⽹址。 ⽤户将重定向到⽬标⽹址并登录到 WebAp1。

三、开源资源：

- 1,SAML SO for ASP.NET

其中SAML组件使⽤的是ComponentSpace SAML v2.0 for .NET，此组件貌似是澳洲⼀家公司开发 的，收费，但不贵。 ⾥边有VS05,08,10的例⼦（部分例⼦是C#,部分是vb.net），也有java调⽤.net SO的例⼦。

- 2,a set of WinForms and WebForms SAML demos with Ful Source Code


htp:/samlso.codeplex.com/

htp:/samlclients.codeplex.com/

此开源项⽬采⽤的是UltimateSaml.dl SAML组件,但不开源。⾥边同时有C#、Vb.net的例⼦， Webform及winform的例⼦。 四、⽹友的⽂章推荐：

- 1.
- 2.
- 3.
- 4.


揭开SAML的神秘⾯纱 htp:/ w.2cto.com/Article/201312/268617.html Web 单点登录系统 htp:/ w.2cto.com/Article/201312/268618.html 基于SAML的单点登录.NET代理端实现⽅案htp:/ w.2cto.com/kf/201312/268619.html SAML htp:/ w.2cto.com/kf/201312/268620.html

五、本⼈实现的 SO（介绍建⽴⼀个demo简单的思路）

- 1.
- 2.


采⽤开源项⽬：htp:/samlso.codeplex.com/ 建⽴⼀个认证中⼼（IDP），⼆个web应⽤（SP1），⼀个类库 SO.Client

- a,其中IDP包括2+3个⽹⻚


- 2个:⼀个Default. x,⼀个Login.aspx


asp

- 3个： SOService.aspx（单点登录服务），SingleLogoutService.aspx（单点登录退出服 务）,ArtifactResponder.aspx(HTP-Artifact应答服务)


- b,2个Web的应⽤结构类似 1+3个⽹⻚ 1个：default.aspx主⻚，获取登录信息 3个：AsertionConsumerService.aspx（校验IDP返回的SAML服务） SingleLogoutService.aspx（校验IDP返回的退出请求及响应） ArtifactResponder.aspx（HTP-Artifact应答服务）
- c, SO.Client类库 主要包括⼀个 SOEntry 及 SOConfig（配置类）【思想可以参考：基于SAML的单点登录.NET代


htp:/ w.2cto.com/kf/201312/268619.html

理端实现⽅案 】

其中 SOEntry部分代码如下:

public class SSOEntry : System.Web.IHttpModule , IRequiresSessionState, IConfigurationSectionHandler

- 1

- 2 {

- 3 #region IHttpModule 成员

- 4

- 5 System.Web.HttpApplication Context;

- 6

- 7 public void Dispose()

- 8 {

// throw new Exception("The method or operation is not implemented.");

- 9

- 10 }

- 11

- 12 public void Init(System.Web.HttpApplication context)

- 13 {

- 14 Context = context;

context.AcquireRequestState += new EventHandler(context_BeginRequest);

- 15

- 16

- 17 }

public object Create(object parent, object configContext, XmlNode section)

- 18

- 19 {

- 20 NameValueSectionHandler handler = new NameValueSectionHandler();

- 21 return handler.Create(parent, configContext, section);

- 22 }

- 23

- 24 void context_BeginRequest(object sender, EventArgs e)

- 25 {

- 26 HttpApplication application = (HttpApplication)sender;

- 27

- 28 Uri url = application.Request.Url;

//如果不是aspx⽹⻚,就不管他了，还可以再加上其它条件，根据正则过滤⼀些⽆需单点登录 的⻚⾯

- 29

if (!url.AbsolutePath.EndsWith(".aspx", StringComparison.OrdinalIgnoreCase) || url.AbsolutePath.IndexOf("/SAML")>-1)

- 30

- 31 return;

- 32

- 33 HttpResponse Response = Context.Response;

//Response.AddHeader("P3P", "CP=CAO PSA OUR");//加上这个,防⽌在Iframe的 时间Cookie丢失

- 34


- 35

- 36 if ("" == Context.User.Identity.Name)

- 37 {

RequestLoginAtIdentityProvider(application); // 这个⽅法可以参考开 源项⽬，此处不介绍

- 38

- 39 }

- 40

- 41 }

- 42

- 43 #endregion

- 44

- 45 …… 其它代码省略

- 46

- 47 }


- d,WebSite1,WebSite2调⽤ SO.Client 只需修改Web应⽤的web.config配置⽂件即可，加⼊如下配置信息。这样在请求Web应⽤的aspx⻚⾯ 时，将⾸先通过 SO.Client. SOEntry的context_BeginRequest⽅法判断⽤户是否已登录，若未登录或 者已超时则⽣成SAML请求转发⾄统⼀认证中⼼（IDP） <!-模块或⼦ 配置段配置信息 ->< configSections><section name="SO" type="SO.Client. SOEntry, SO.Client"/>< /configSections>< !-单点登陆配置信息 -><SO><!单点登陆登陆⻚⾯地址 -><ad key="SO.DefaultURL" value=" "/><!-单 点登陆服务的⻚⾯地址 -><ad key="SO. SOServiceURL" value="


系统

htp:/127.0.0.1/website1 htp:/127.0.0.1/ SOIDP/SAML/

SOService.aspx htp:/127.0.0.1/ SOIDP/SAML/Sin gleLogoutService.aspx

"/><ad key="SO.LogoutServiceURL" value="

"/><!- Configuration for comunicating with the IdP. Valid values for ServiceBinding(SP to IDP) are: urn:oasis:names:tc:SAML 2.0:bindings:HTP-POST urn:oasis:names:tc:SAML 2.0:bindings:HTP-Redirect urn:oasis:names:tc:SAML 2.0:bindings:HTP-Artifact -><ad key="SO.SpToIdpBinding" value="urn:oasis:names:tc:SAML 2.0:bindings:HTP-POST"/><!- Valid values for ServiceBinding(IDP to SP) are: urn:oasis:names:tc:SAML 2.0:bindings:HTP-POST urn:oasis:names:tc:SAML 2.0:bindings:HTP-Artifact -><ad key="SO.IdpToSPBinding" value="urn:oasis:names:tc:SAML 2.0:bindings:HTP-POST"/><!-<ad key="SO.ArtifactResolutionServiceURL" value="

htp:/127.0.0.1/ SOIDP/SAML/ArtifactResolutionS ervice.aspx

"/>->< / SO>

六、基于SAML的 SO的好处

1. 出现⼤⼤简化了 SO，提升了安全性

- 2.
- 3.


跨域不再是问题，不需要域名也可访问 不仅⽅便的实现Webform、Winform的单点登录，⽽且可以⽅便的实现java与.net应⽤的单点登录

本⼈只是粗略研究了基于SAML的单点登录应⽤，认知有限，不对之处请各位前辈指点。同时借此博 ⽂分享我的学习⼼得，抛砖引⽟

