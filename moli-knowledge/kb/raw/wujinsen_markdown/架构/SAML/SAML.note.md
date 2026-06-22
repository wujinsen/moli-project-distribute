SAML是OASIS制定的⼀种安全性断⾔标记语⾔，它⽤于在复杂的环境下交换⽤户的⾝份识别信息。在SAML诞⽣之 前，如果想在Websphere、Weblogic和SunONE等之间实现SSO，我们必须分别实现⼀个适配层，来达成⼀种相互理解 的协议，在该协议上，产品能够共享各⾃的⽤户认证/授权信息。SAML诞⽣之后，我们免去了这种烦恼。可以预计， 将来⼤部分产品都可以实现基于SAML的联邦服务。

事实上，SAML已经在很多商业/开源产品中得到实现，包括：

IBM Tivoli Access Manager Weblogic Oblix NetPoint SunONE Identity Server Baltimore, SelectAccess Entegrity Solutions AssureAccess Internet2 OpenSAML Yale CAS 3 Netegrity SiteMinder Sigaba Secure Messaging Solutions RSA Security ClearTrust VeriSign Trust Integration Toolkit Entrust GetAccess 7

SAML背后是强⼤的商业联盟和开源联盟，尽管Microsoft迟迟未能在SAML2.0观点上达成⼀致，但它也正努⼒跟进 SAML标准化过程，由此可见SAML协议已经是势在必⾏。

1 SAML的基本概念

理解SAML的概念很重要，个⼈认为SAML协议的原理跟CAS/Kerberos很类似，理解上不存在困难，但SAML引⼊ 了⼀些新的概念名词，因此要先把握清楚这些概念。

断⾔，这个在SAML的”A”，是整个SAML协议中出现的最多的字眼，我们可以将断⾔看作是⼀种判断，并且我们 相信这种判断，因此，做出断⾔的⼀⽅必须被信赖。校验来⾃断⾔⽅的断⾔必须通过⼀些⼿段，⽐如数字签名，以确 保断⾔的确实来⾃断⾔⽅。

SAML⽬标是让多个应⽤间实现联邦⾝份(IdentityFederation)，提起联邦，⼤家可以想象⼀下欧盟，欧盟国家之间

的公民都具有⼀个联邦⾝份，⽐如Peter是法国公民，John是⽐利时公民，这两个公民的⾝份都能够互相被共享，恰 好，张三是⼀个中国公民，但他能像Peter和Jhhn那样随意进⼊欧盟国家，显然不能，因为它不具有欧盟联邦⾝份。

理解了联邦的概念，我们就可以回到SAML上，SAML解决了联邦环境中如何识别⾝份信息共享的标准化问题，⽐ 如，法国的Peter进⼊⽐利时，他如何证明⾃⼰的⾝份呢？

SAML就是为了解决这个问题。

在联邦环境中，通常有下⾯的3种实体：

Subject主题)：Subject是SAML实体中的第⼀个重要的概念，Subject包括了User、Entity、Workstation等能够象征⼀ 个参与信息交换的实体。

RelyingParty信任⽅)：SAML中的ServiceProvider⾓⾊，也就是提供服务的⼀⽅。

AssertingParty断⾔⽅)：SAML中的IdentityProvider⾓⾊，⽤于提供对主题的⾝份信息的正确的断⾔，类似⼀个公 证机构。

我们看看联邦环境的⼀个场景：

假设有⼀个Peter(Subject)的法国公民，他需要访问⽐利时(ServiceProvider)，他在⽐利时机场被要求提供⾝份信 息，Peter提供了欧盟(Federation)的通⾏证件，随即，这个通⾏证件在⽐利时机场被审核，或通过计算机送到欧盟⾝份 认证中⼼(IdentityProvider)，该中⼼有⼀个由所有欧盟国家共同建⽴的公民 ，中⼼审核了Peter的⾝份信息，并 断⾔“Yes，HeisPeterFromFrance”，于是，Peter得到礼貌的回应“欢迎光临⽐利时”。

# 数据库

如果你将欧盟看作是⼀个联邦环境，你会发现上⾯的场景跟在联邦环境应⽤SAML很相似。

在联邦环境下，任何需要授权访问的服务都需要知道服务请求⽅的⾝份主题信息(Whoareyou)，服务提供⽅ (ServiceProvider)不负责审核⽤户的⾝份信息，但它依赖于1个甚⾄多个IdentityProvider来完成此任务，见下图。

1个IdnetityProvider可以被多个ServiceProvider共享，当然，共享的前提是建⽴信任关系(即ServiceProvider要信任 IdnetityProvider)，就好⽐如，如果⽐利时(ServiceProvider)需要开放对欧盟国家成员访问，它信任欧盟的 IdnetityProvider，它信任欧盟的IdnetityProvider的任何判断，包括”HeisPeterFromFrance”。⾄于是否让Peter⼊境，那是 受权限策略的控制(注意SAML同样对Authorization断⾔做了标准化，此处，我们仅仅关注Authentication)。

![image 1](<SAML.note_images/imageFile1.png>)

2 SAML 的 2 种典型模式

在协议的⾓度， SAML 原理⾮常类似 CAS 和 Kerberos ， CAS 协议依赖于 CAS Server ， Kerberos 依赖于 KDC ， ⽽ SAML 则依赖于 Identity Provider 。

根据 Service Provider( 以下简称 SP) 和 Identity Provider( 以下简称 IDP) 的交互⽅式， SAML 可以分为以下⼏种模 式：⼀种是 SP 拉⽅式，⼀种是 IDP 推⽅式。

在 SAML 中，最重要的环节是 SP 如何获取对 Subject 的断⾔， SP 拉⽅式是 SP 主动到 IDP 去了解 Subject 的⾝份 断⾔，⽽ IDP 推⽅式则是 IDP 主动把 Subject 的⾝份断⾔通过某种途径告诉 SP 。

2.1 SAML 的 POST/Artifact Bindings ⽅式（即 SP 拉⽅式）

该⽅式的主要特点是， SP 获得客户端的凭证 ( 是 IDP 对 Subject 的⼀种⾝份认可 ) 之后，主动请求 IDP 对 Subject 的凭证的断⾔。如下图所⽰： Subject 是根据凭证去访问 SP 的。凭证代表了 Subject 的⾝份，它类似于“来⾃ IDP 证 明：我就是 Peter ，法国公民”。 现在，让我们看看 SP 拉⽅式是如何进⾏的：

Subject 访问 SP 的受保护资源， SP 发现 Subject 的请求中没有包含任何的授权信息，于是它重定向⽤户访问 IDP.

![image 2](<SAML.note_images/imageFile2.png>)

协议执⾏：

- 1，Subject 向 IDP 请求凭证 ( ⽅式是提交⽤户名 / 密码 )

- 2，IDP 通过验证 Subject 提供的信息，来确定是否提供凭证给 Subject

- 3，假如 Subject 的验证信息正确，他将获取 IDP 的凭证以及将服务请求同时提交给 SP 。

- 4，SP 接受到 Subject 的凭证，它是提供服务之前必须验证次凭证，于是，它产⽣了⼀个 SAML 请求，要求 IDP

对凭证断⾔

- 5，凭证是 IDP 产⽣的，它当然知道凭证的内容，于是它回应⼀个 SAML 断⾔给 SP

- 6，SP 信任 IDP 的 SAML 断⾔，它会根据断⾔结果确定是否为 Subject 提供服务。


2.2 SAML 的 Redirect/POST Bindings ⽅式 ( 即 IDP 推⽅式 )

该⽅式的主要特点是， IDP 交给 Subject 的不是凭证，⽽是断⾔。

过程如下图所⽰：

![image 3](<SAML.note_images/imageFile3.png>)

- 1，Subject 访问 SP 的授权服务， SP 重定向 Subject 到 IDP 获取断⾔。

- 2，IDP 会要求 Subject 提供能够证明它⾃⼰⾝份的⼿段 (Password ， X.509 证书等 )

- 3，Subject 向 IDP 提供了⾃⼰的帐号密码。

- 4，IDP 验证密码之后，会重订向 Subject 到原来的 SP 。

- 5，SP 校验 IDP 的断⾔ ( 注意， IDP 会对⾃⼰的断⾔签名， SP 信任 IDP 的证书，因此，通过校验签名，能够确

信从 Subject 过来的断⾔确实来⾃ IDP 的断⾔ ) 。

- 6，如果签名正确， SP 将向 Subject 提供该服务。


