SAML的相关内容就不介绍了，想深究的可以研究下相关的规范，主要写下⼤体的思路。

通过SAML实现单点登录的运转过程如图：

![image 1](<基于SAML的单点登录.NET代理端实现方案.note_images/imageFile1.png>)

此处的ServiceProvider是服务提供者，也就是⽤户需要访问的应⽤，IdentityProvider是认证提供者。 通过上图可以看出，当⽤户访问⼀个应⽤的时候，如果⽤户没有登陆，那么需要将⽤户重定向到认证 服务，认证服务判断该⽤户是否已经经过认证，如果没有认证过那么进⾏认证，然后⽣成断⾔响应发 送给SP，SP负责处理断⾔，验证⽤户是否认证通过。也就是说SP进⾏了请求转发和断⾔解析两个过 程。在.NET中我们可以通过HtpModule来过滤请求，对没有认证的请求进⾏转发，通过HtpHandler 过滤断⾔请求，对断⾔进⾏处理。 这种⽅案的好处是，不需要修改既有的应⽤程序代码，就能够将其集成到单点登录 中。也就是只 要把认证的Dl复制到应⽤执⾏⽬录下，然后配置HtpModule和HtpHandler即可。

系统

示例Module和Handler如下： / <sumary> / HTP Module 处理所有Htp请求 / </sumary>

public clas SPDispatcherFilter : IHtpModule {

public void Init(HtpAplication context)

{

context.AcquireRequestState += new EventHandler(context_AcquireRequestState); }

/ <sumary> / 在sesion创建完成后处理请求 / </sumary> / <param name="sender"></param> / <param name="e"></param>

void context_AcquireRequestState(object sender, EventArgs e) {

HtpAplication ap = sender as HtpAplication;

/如果还没加载过配置信息，那么进⾏加载 if (!ConfigInfo.HasLoadMetaData)

MetaData.Load(ap.Request.PhysicalAplicationPath + ConfigurationManager.ApSeting s["MetaDataPath"]);

/处理htp请求 if (HTPUtils.HandleRequest(ap.Context)

ap.CompleteRequest(); }

}

/ <sumary> / Htp Handler 处理SAML请求 / </sumary>

public clas SPDispatcherHandler:IHtpHandler,IRequiresSesionState {

public bol IsReusable {

get { return true; } }

public void ProcesRequest(HtpContext context) {

HTPUtils.HandleRequest(context); }

}

配置⽅式如下：

- 1. 在 IS应⽤程序中增加模块（Module），如图：

名称随便，类型为：处理类的完整名称（命名空间+类名）

- 2. 在 IS应⽤程序中增加HtpHandler，如图：


![image 2](<基于SAML的单点登录.NET代理端实现方案.note_images/imageFile2.png>)

![image 3](<基于SAML的单点登录.NET代理端实现方案.note_images/imageFile3.png>)

请求路径为：*/SAML/* 类型为：处理类的完整名称（命名空间+类名）

