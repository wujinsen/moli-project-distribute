# SAML 2.0简介

SAML(Security Asertion Markup Language) 安全断⾔标记语⾔是由标识化组织 OASIS 提出的⽤于安 全互操作的标准。SAML 是⼀个 XML 框架，由⼀组协议组成，⽤来传输安全声明。SAML 获得了⼴泛 的⾏业认可，并被诸多主流⼚商所⽀持。SAML 的初始版本 1.0 最初于 202 年发布，发展数年后，于 205 年推出 SAML 2.0。实际上，SAML 2.0 是由三个原有的认证联邦标准：SAML1.1，ID-F (Identity Federation Framework) 1.2 和 Shi boleth 构成。

- 图 1. SAML 2.0 发展路线图

![image 1](<基于 SAML 2.0 的 WebSphere Application Server SSO 实现.note_images/imageFile1.png>)

Extensible Markup Language (XML)

XML Schema

XML Signature

XML Encryption (SAML 2.0 only)

Hypertext Transfer Protocol (HTP)

SOAP

SAML Asertions 断⾔：定义交互的数据格式 (XML)

SAML Protocols 协议：定义交互的消息格式 (XML+procesing rules) SAML Bindings 绑定：定义如何与常⻅的通信协议绑定 (HTP,SOAP) SAML Profile 使⽤框架：给出对 SAML 断⾔及协议如何使⽤的建议 (Protocols+Bindings)

- 图 2. SAML 协议核⼼


SAML 2.0 是在以下⼏个技术标准的基础上构建的：

SAML 2.0 的核⼼内容被涵盖在官⽅⽂档 SAMLConform, SAMLCore, SAMLBind 和 SAMLProf 中。 SAML 2.0 规范说明书主要包含以下四⽅⾯内容：

![image 2](<基于 SAML 2.0 的 WebSphere Application Server SSO 实现.note_images/imageFile2.png>)

在介绍 SAML 四⼤内容之前，简单介绍下在 SAML 协议标准中出现的两个⻆⾊，⼀个是 Identity Provider(IdP)，通常 IdP 负责创建、维护和管理⽤户认证。⼀个是 Service Provider(SP)，通常 SP 控 制⽤户是否能够使⽤该 SP 提供的服务和资源。 SAML 断⾔定义了⼀系列 XML 编码格式安全断⾔的语法和语义规范，通常断⾔由 SAML IdP 端⽣成并 发送到 SAML SP 端，由 SP 端来分析和处理断⾔。断⾔内容中可能包含三类声明（statements），声 明是 SP 端⽤来分析并判断⽤户能否接⼊服务或资源的依据：

认证声明：声明⽤户是否已经认证，通常⽤于单点登录。

属性声明：声明某个 Subject 所具有的属性。

授权决策声明：声明某个资源的权限，即⼀个⽤户在资源 R 上具有给定的 E 权限⽽能够执⾏ A 操 作。

SAML Protocols 描述了 SAML 元素（包括断⾔）如何被打包到 SAML 请求和响应元素中，并规定 SAML 实体（IdP、SP 等）处理这些元素时必须遵守的处理规则。在⼤多数情况下，SAML Protocols 就是⼀个简单的请求 - 响应协议。 SAML Bindings( 绑定 ) 是 SAML Protocols 信息到⼀个标准信息格式或者通信协议的映射过程。例如 SAML SOAP 绑定就定义了⼀个 SAML 消息如何被封装到 SOAP envelope 中。 SAML Profile( 使⽤框架 ) 描述了如何使⽤ SAML 协议信息和断⾔来处理特定的业务⽤户实例。SAML 2.0 较之 SAML1.1 提供了更多使⽤框架，不过⽬前最常被使⽤的依然是 Web Browser SO。本⽂下⼀ 章将重点介绍 Web Browser SO Profile。

回⻚⾸

# SAML 2.0 Web Browser SO使⽤框架

Web Browser SO 定义了如何使⽤ SAML 消息和绑定来⽀持 web SO。该使⽤框架提供了多种选 项，不过⾸先需要做的两个决定是：⼀，该消息流是由 IdP 发起还是由 SP 发起。⼆，在 Idp 和 SP 之 间⽤哪类 SAML Binding 来传递消息。SAML ⽀持两类消息流来实现 web SO 信息交互，最常⽤的 web SO 信息交互⽅式是由 SP 发起，⽤户选择⼀个浏览器标签或者点击⼀个链接，然后⽤户将被转 到他们需要连⼊的 SP 应⽤。但是，这个时候⽤户在 SP 端并没有获得任何认证许可，因此 SP 将⽤户 转向 IdP 来获得认证，Idp 构建⼀个 SAML Asertion 断⾔来代表⽤户在 IdP 侧的认证，然后将带有断 ⾔的⽤户实体转向到 SP 端。由 SP 来处理断⾔并决定是否授予该⽤户接⼊资源的权限。另⼀种 web

- SO 消息交互是由 IdP 发起的，这需要⽤户先访问 IdP 然后点击⼀个链接到 SP。同样 IdP 需要构建⼀


个断⾔发送到 SP 端，由 SP 端决定⽤户的授权。这个⽅法在某些场景⾮常实⽤，但它需要 Idp 配置⼀ 个内部转换链接到 SP 站点。本⽂根据⽣产环境实际应⽤场景，将主要介绍由 IdP 发起的 web SO Profile。

## SAML 2.0 HTP POST BINDING

SAML 2.0 HTP POST binding 定义了由 HTML 表单控制（ Form Control）来传送 SAML 协议消息的 机制。这个绑定可能和 HTP 重定向绑定（Redirect Binding）结合使⽤。HTP Post Binding 是适⽤ 于当 SAML 请求者和响应者需要通过 HTP ⽤户代理来通信时。该绑定具体需要传送的数据可以通过 SAML 2.0 的⽂档描述⽂件中找到。需要注意的是该绑定要求传送的 HTP 表单数据使⽤ base64 编 码。在下⼀节将具体介绍 Web Browser SO Profile 定义的如何使⽤ HTP Post Binding 来传送 SAML 消息。

## SAML 2.0 Web Browser SO 流程

上⽂中有提到 Web Browser SO 可以由 IdP 端发起，也可以由 SP 端发起。本⽂将介绍由 IdP 端发起 的 Web Browser SO。 在由 IdP 发起的 SO ⽤户场景中，IdP 端配置了⼀个专⻔的链接来指向请求的 SP。这些链接实际上指 向的是本地 IdP 的 SO 服务，并传递参数到该 SO 服务来鉴定远程 SP。在该场景中，⽤户并不是直 接访问 SP 端服务。⽽是连到 IdP 站点点击其中的链接来获得远程 SP 服务的接⼊权限。这个链接触发 了 SAML 断⾔的⽣成，在本案例中，将使⽤ HTP POST Binding 来传送信息到服务端：

- 图 3. IdP 初始请求流程图


![image 3](<基于 SAML 2.0 的 WebSphere Application Server SSO 实现.note_images/imageFile3.png>)

- 1.
- 2.
- 3.
- 4.


如果⽤户在 IdP 端没有⼀个合法本地安全上下⽂的话，在某个时刻⽤户将被要求提供他们的认证 信息给 IdP 站点。 ⽤户提供有效的认证信息后，IdP 将为⽤户创建⼀个本地登录安全上下⽂。 ⽤户在 IdP 端选择⼀个菜单选项或者链接以请求访问⼀个 SP 的站点。⽐如 sp.example.com。这 个动作将导致 IdP 端的 SO 服务被调⽤。

SO 服务构建⼀个 SAML 断⾔来表示⽤户的登录安全上下⽂，由于这⾥使⽤的是 POST binding。断⾔将在被封装到 SAML<Response> 之前进⾏数字签名，然后 <Response> 消息将作 为⼀个隐藏的表单控件被放⼊⼀个 HTML FORM 中，并且这个表单控件的名称必须命名为 “SAMLResponse”, 如果在 IdP 和 SP 端约定⽀持某⼀个特定的应⽤资源，那么可以将 SP 端的资源 URL 经过 Base64 编码后，使⽤隐藏的表单控件加到表单中，该控件应被命名为“RelayState”。

SO 服务通过 HTP Response 发送 HTML 表单给浏览器，通常出于使⽤⽅便的⽬的，该 HTML FORM 都是包含脚本代码以⾃动执⾏提交表单的操作到⽬的站点。

- 1 <form method="post"

- 2 action="https://sp.example.com/SAML2/SSO/POST" ...>

- 3 <input type="hidden" name="SAMLResponse" value="response" />

- 4 <input type="hidden" name="RelayState" value="token" />...

- 5 <input type="submit" value="Submit" /></form>


其中 SAMLResponse 参数的值是基于 Base64 编码。

- 5.
- 6.


浏览器根据⽤户操作或者脚本的⾃动执⾏，产⽣⼀个 HTP POST 请求来发送该表单给 SP 端的 Asertion Consumer Service（ACS）。

- 1 POST /SAML2/SSO/POST HTTP/1.1

- 2 Host: sp.example.com

- 3 Content-Type: application/x-www-form-urlencoded

- 4 Content-Length: nnn

- 5 SAMLResponse=response&RelayState=token


- SP 端的 ACS 从 HTML FORM 中获得 <Response> 消息来处理，ACS 必须⾸先验证在 SAML 断⾔中的 数字签名，然后再对断⾔的内容进⾏处理以便在 SP 端给⽤户创建⼀个本地的登录安全上下⽂。⼀旦这 个过程完成，SP 将取到 RelayState（如果有的话）来决定⽤户期望访问的应⽤资源 URL，并发送⼀个 HTP 重定向响应给浏览器，将⽤户定向到所请求的资源。


访问检查是为了确定⽤户是否有正确的访问权限来接⼊所请求的资源。如果访问检查通过，资源 将被返回到浏览器。

回⻚⾸

# Web SO⽤户场景简介

在上⼀章节，介绍了 SAML 2.0 中为实现 web SO 所定义的 Web SO Browser Profile。我们知道如 果需要实现客户端到服务端的 Web SO，⾸先需要在 IdP 端实现 SO 服务。⽽在 SAML 2.0 标准 中，由于引⼊了联邦认证的概念，在⼀个庞⼤的 IT 系统环境中，IdP 有可能作为⼀个独⽴的第三⽅服 务出现。但在本⽂中使⽤的 IdP，是由客户⼀端⾃有 IT 认证系统提供的服务，即在服务提供商与客户 之间没有引⼊第三⽅的 IdP，因此略去了 SP 与客户需要在 IdP 注册服务的过程。在下⽂中客户端 SO 服务将指代 IdP 的认证服务。 这⾥简单介绍下⽂中使⽤的 Web SO ⽤户场景：终端⽤户点击企业内部⽹站某个链接或某个菜单选项 时，客户端 Web SO 服务将被调⽤并发送包含⼀个 SAML Asertion 的 HTP FORM 请求给服务端， 该 SAML 断⾔携带了断⾔创建时间、⽤户 ID 等信息，当服务端从表单中提取断⾔信息后分析认为该断 ⾔在有效期内，那么取得⽤户 ID 信息后，在服务端⽤户管理服务（例如 LDAP、DB）中查找该⽤户 ID，如果找到匹配的⽤户 ID，那么认为该服务请求有效，并将⽤户重定向到请求的资源。该过程并不 需要⽤户再次输⼊⽤户名密码等信息。但是在客户端需要产⽣⼀个携带 SAML 断⾔的表单请求。下⾯ 的内容就将介绍如何实现客户端的请求以及服务端的 Asertion Consumer Service。

回⻚⾸

客户端 Web SO的实现

在本⼩节，将详细介绍如何使⽤ OpenSAML 库来实现客户端 SO 服务。当客户与 SP 协商好使⽤ HTP POST Binding 来传递服务，并由客户⼀端（IdP）来发起该 web SO 之后，需要实现 web SO 的客户端部分，可以理解为当终端⽤户在点击企业内部⽹站某个链接或某个菜单选项时，能够发送包 含 SAML 断⾔在内的 <SAMLResponse> 表单给服务端。包含 HTML FORM 表单的 html ⽂件的编写 是⼀个相对简单的过程，读者可以参考⽹路上的资源。

- 1 <form method="post" action="https://sp.example.com/SAML2/SSO/POST" ...>

- 2 <input type="hidden" name="SAMLResponse" value="response" />

- 3 <input type="hidden" name="RelayState" value="token" />

- 4 ...

- 5 <input type="submit" value="Submit" />

- 6 </form>


其中控件名为 SAMLResponse 的值如何⽣成是最关键的部分，是下⾯将要具体介绍的。上述表单代码 中名为 SAMLResponse 表单元素的值为“response”，未来我们将使⽤经过 base64 编码后的 <Response> 消息的字符串来代替。注意，由于 SAML 2.0 标准的规定，携带 SAML 2.0 断⾔的 <Response> 必须命名为”SAMLResponse”。尽管如此，并不意味着它是⼀个响应消息，在 SP 端看 来，这是⼀个 SAML 2.0 SO 的 HTP 请求中携带⼀个字段信息。

## SAML 2.0 SO 请求的构成实现

根据 SAML 2.0 标准的要求，当⼀个 SAML Response 包含 0 个或多个断⾔时，需要使⽤ <Response> 消息元素。这⾥给出 <Response> 的⼀个实例，我们将基于这个实例来讨论它的实现过程。

- 1 <samlp:Response ID="_0dac9fb0c5fedaae24e26d2eb4ffe8a4"

- 2 IssueInstant="2011-08-23T08:28:09.109Z"

- 3 Version="2.0" xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol">

- 4 <saml:Issuer xmlns:saml="urn:oasis:names:tc:SAML:2.0:

- 5 assertion">http://mycom.com/issuer</saml:Issuer>

- 6 <samlp:Status>

- 7 <samlp:StatusCode Value="urn:oasis:names:tc:SAML:2.0:status:Success" />

- 8 </samlp:Status>

- 9 <saml:Assertion ID="_9fa97d8e3552f2d4ae1fc001c887c614"

- 10 IssueInstant="2011-08-23T08:28:09.078Z" Version="2.0"

- 11 xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion">

- 12 <saml:Issuer>http://mycom.com/issuer</saml:Issuer>

- 13 <saml:Subject>

- 14 <saml:NameID Format="urn:oasis:names:tc:SAML:2.0:

- 15 nameid-format:emailAddress">****@cn.ibm.com</saml:NameID>

- 16 <saml:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer">

- 17 <saml:SubjectConfirmationData NotOnOrAfter="2011-08-23T08:58:06.578Z"

- 18 Recipient="http://ip:port/SSO/serviceA" />

- 19 </saml:SubjectConfirmation>

- 20 </saml:Subject>

- 21 <saml:Conditions>

- 22 <saml:AudienceRestriction>

- 23 <saml:Audience> https://saml.example.com </saml:Audience>

- 24 </saml:AudienceRestriction>

- 25 </saml:Conditions>

- 26 </saml:Assertion>

- 27 </samlp:Response>


通常，需要对 SAML 断⾔进⾏数字签名处理，这⾥为了使 Response 内容简洁清晰，仅给出数字签名 前的 <Response> 消息，在后⾯的代码实现中将提供数字签名。在上⾯给出 Response 消息实例中， 包含⼀个 Asertion，其中包含了⽤户 ID 信息：<saml:NameID Format="urn:oasis:names:tc:SAML 2.0:nameidformat:emailAdres"> *@cn.ibm.com</saml:NameID>，并标识该 NameID 格式为邮件地址。 <NameID> 元素是封装在 < Subject > 中的⼀个⼦元素。根据 SAML 2.0 标准规定，进⾏认证请求的断 ⾔中，<Asertion> 元素必须包含⼀个具有 <Subject> 元素的 <AuthnStatement>, 并且该 <Subject> 元素必须包含⼀个 <SubjectConfirmation>。⽽对不同的⽬的的 Asertion，必须携带的信息字段不 同，具体请参考 SAML 2.0 标准规定。下⾯是使⽤ OpenSAML 库实现的 Subject 字段和 Asertion 字 段的构建代码：

- 1 public Subject createSubject

- 2 (String username, String format, String confirmationMethod)

- 3 {

- 4 NameID nameID = create (NameID.class, NameID.DEFAULT_ELEMENT_NAME);

- 5 nameID.setValue (username);

- 6 if (format != null)

- 7 nameID.setFormat (format);

- 8 Subject subject = create (Subject.class, Subject.DEFAULT_ELEMENT_NAME);

- 9 subject.setNameID (nameID);

- 10

- 11 if (confirmationMethod != null)

- 12 {

- 13 SubjectConfirmation confirmation = create

- 14 (SubjectConfirmation.class, SubjectConfirmation.DEFAULT_ELEMENT_NAME);

- 15 confirmation.setMethod (CM_PREFIX + confirmationMethod);

- 16 DateTime now=new DateTime();

- 17 DateTime afterTime=now.plusMinutes(30);

- 18 SubjectConfirmationData subConData=create

(SubjectConfirmationData.class,SubjectConfirmationData.DEFAULT_ELEMENT_NAME);

- 19

- 20 subConData.setNotOnOrAfter(afterTime);

- 21 subConData.setRecipient(recipient);

- 22 confirmation.setSubjectConfirmationData(subConData);

- 23 subject.getSubjectConfirmations ().add (confirmation);

- 24 }

- 25

- 26 return subject;

- 27 }


完成 <Subject> 和 <Asertion> 的构建之后，需要对 <Asertion> 进⾏数字签名之后再构建 <Response> 信息。由于数字签名所涉及的内容不在本⽂讨论范围之内，因此不在此赘述。读者可以 参考本⽂所提供的代码实现数字签名部分。在完成 <Asertion> 的数字签名之后，才能完成最终的 <Response> 信息的构建：

- 1 /**

- 2 根据给定的状态码，状态消息和查询 ID 来构建⼀个 Response

- 3 */

public Response createResponse(String statusCode, String message, String inResponseTo)

- 4

- 5 {

- 6 Response response = create

- 7 (Response.class, Response.DEFAULT_ELEMENT_NAME);

- 8 response.setID (generator.generateIdentifier ());

- 9 if (inResponseTo != null)

- 10 response.setInResponseTo (inResponseTo);

- 11 DateTime now = new DateTime ();

- 12 response.setIssueInstant (now);

- 13 if (issuerURL != null)

- 14 response.setIssuer (spawnIssuer ());

- 15 StatusCode statusCodeElement = create

- 16 (StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);

- 17 statusCodeElement.setValue (statusCode);

- 18 Status status = create (Status.class, Status.DEFAULT_ELEMENT_NAME);

- 19 status.setStatusCode (statusCodeElement);

- 20 response.setStatus (status);

- 21

- 22 if (message != null)

- 23 {

- 24 StatusMessage statusMessage = create

- 25 (StatusMessage.class, StatusMessage.DEFAULT_ELEMENT_NAME);

- 26 statusMessage.setMessage (message);

- 27 status.setStatusMessage (statusMessage);

- 28 }

- 29

- 30 return response;

- 31 }


根据 SAML 2.0 标准规定，我们提供给 SP 的 HTP FORM 中，名为”SAMLResponse”表单控件的值需 要经过 Base64 编码后发送给 SP 端，因此需要对最终构建完成的 <Response> 进⾏ Base64 编码， ⽹上有很多类似的资源，读者也可以参考本⽂提供的 Sample Code 来实现，这⾥将不做介绍。

回⻚⾸

# 服务端 Web SO的实现

当客户端 Web SO 服务发送⽣成的 HTP 表单请求到服务端后，服务端的 Web SO 服务需要处理来 ⾃客户端的请求，解析 SAMLResponse 参数值并从中获得⽤户 ID 字段信息，当 SP 认为该⽤户 ID 合 法，则将⽤户重定向到所请求的资源。本⽂服务端 Web SO 将基于 WAS 的 Trust Asociation Interceptor 实现。

回⻚⾸

# WAS TAI SO简介

简单来说，WAS Trust Asociation Interceptor(TAI) 信任联合拦截器会拦截所有来⾃客户端的请求， 并对 htp 请求内容进⾏分析，如果满⾜ TAI 的认证要求，将认为⽤户是合法的使⽤者，并将创建⼀个 ⽤户信息对象传递给 WAS 继续处理，根据应⽤配置的不同，通过 TAI 的认证后展现的内容不同。本章 将简单介绍 WAS TAI，重点介绍如何利⽤ OpenSAML 库来解析来⾃客户端的 SAMLResponse 请求。 如果读者对 TAI 的定制化实现感兴趣，可以参考 IBM 提供的 Websphere Aplication Server 官⽅⽂ 档。 WAS TAI 接⼝的两个关键⽅法如下：

public bolean isTargetInterceptor(HtpServletRequest req) 如果 TAI 应该处理该请求，则该⽅法 将返回 true。如果为 false 则告之 WAS TAI 忽略此次请求。

public TAIResult negotiateValidateandEstablishTrust(HtpServletRequest req, HtpServletResponse res) 该⽅法返回⼀个 TAIResult 对象，表明被处理的请求的状态。如果有需 要可以在此修改 HTP 响应。

TAIResult 类有三个静态⽅法来创建⼀个 TAIResult。三个静态⽅法都将⼀个 int 类型值作为第⼀个参 数。这个参数应该是⼀个合法的 HTP 请求返回代码。例如 HtpServletResponse.SC_OK 告诉 WAS TAI 完成协商，WAS 将使⽤在 TAIResult 中的信息来创建⽤户身份。读者可以在本⽂提供的实例代码 中看到如何实现⾃⼰定制化的 TAI。这⾥不再详细介绍。在使⽤ isTargetInterceptor () ⽅法判断来⾃客 户端的 SO 属于 SAML SO 请求后，将使⽤ negotiateValidateandEstablishTrust() ⽅法来分析⽤户 的 SAML 2.0 SO 请求，并根据认证的结果创建⼀个 TAIResult 对象。在该⽅法中调⽤了⽅法 TAI SOConsumer() 来解析 SAML 2.0Web SO 请求。下⾯将介绍如何实现 TAI SOConsumer()( 即 SAML 2.0 服务端的 Asertion Consumer Service 部分 ).

## SAML 2.0 SO 请求的认证过程

TAI SOCoumsumer() ⽅法是实现 SAML 2.0 ACS 对 SAML Response 的认证处理。如果来⾃客户端 SAML2 Response 经过加密与数字签名处理，那么需要先解密 Asertion 然后对其进⾏数字签名的验 证。上⽂的案例中对 Response 提供了数字签名并经过 Base64 编码，因此在本⽂中需要对 Response 先进⾏ Base64 解码处理，然后进⾏认证数字签名。下⾯是实现的代码⽚段：

- 1 public String parseResponse(String RspStr,JKSKeyData jksdata) {

- 2 String NameId = null;

- 3 //decode the response string

- 4 String SResponse=new String(Base64.decode(RspStr));

- 5 //get the Credential information

- 6 char[] password =jksdata.getPassword();

- 7 ……

- 8 BasicX509Credential credential = new BasicX509Credential();

- 9 credential.setEntityCertificate(certificate);

- 10 credential.setPublicKey(certificate.getPublicKey());

- 11 ……

- 12 // Initialize the library

- 13 DefaultBootstrap.bootstrap();

- 14 // Get parser pool manager

- 15 BasicParserPool ppMgr = new BasicParserPool();

- 16 ppMgr.setNamespaceAware(true);

- 17 // Parse metadata file

- 18 ......

- 19 // Get apropriate unmarshaller

UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();

- 20

- 21 Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(metadataRoot);

- 22 // Unmarshall using the document root element

- 23 ……

- 24 Response rsp=(Response)messageContext.getInboundMessage();

- 25 // Verify issue time, make sure the assertion time is valid

- 26 DateTime time = rsp.getIssueInstant();

- 27 ……

- 28 //verify response signature

- 29 Signature rspSignature = rsp.getSignature();

- 30 ……

- 31 rspv.validate(rspSignature);

- 32 ……

- 33 //verify assertion signature

- 34 ……

- 35 //get the name identifier after verify successfully

- 36 NameId=samlAssertion.getSubject().getNameID().getValue();

- 37 NameID nid=samlAssertion.getSubject().getNameID();

- 38 return NameId;


- 39 }


由 TAI SOCoumsumer() 返回的字符串 NameId 将被作为关键字，⽤来在 LDAP、DB 或者其他存储⽤ 户信息的地⽅查询，如果该⽤户能够被查询到，则整个 TAI 认证过程完成并返回成功的 HTP 响应给 ⽤户，如果没有查询到该⽤户 ID，则认为 TAI 认证失败，将返回⼀个失败的 HTP 响应或者错误⻚⾯ 给⽤户。

回⻚⾸

# 结束语

本⽂通过介绍 SAML 2.0 标准以及其中被普遍使⽤的 Web Browser SO 使⽤框架，向读者展示了如何 依据 SAML 2.0 的使⽤框架并利⽤ OpenSAML 库来实现客户端与服务端的 Web SO。本⽂中所涉及 的 SAML 2.0 Web SO 流程是⼀个被⼴泛使⽤的框架，如果⽤户希望实现更为复杂的使⽤框架，可以 参考 SAML 2.0 的官⽅⽂档与 OpenSAML 的官⽅站点。

