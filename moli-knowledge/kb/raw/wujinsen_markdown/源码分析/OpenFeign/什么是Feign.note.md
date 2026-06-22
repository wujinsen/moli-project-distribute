# 什么是Feign

Feign是由Retrofit，JAXRS-2.0和WebSocket启发的⼀个java到htp客户端绑定。 Feign的主要⽬标是 将Java Htp Clients变得简单。Feign的源码地址：htps:/github.com/OpenFeign/feign

# 写⼀个Feign

在我之前的博⽂有写到如何⽤Feign去消费服务，⽂章地址： htp:/blog.csdn.net/forezp/article/details/69808079 。 简单的实现⼀个Feign客户端，⾸先通过@FeignClient，客户端，其中value为调⽤其他服务的名称， FeignConfig.clas为FeignClient的配置⽂件，代码如下：

@FeignClient(value = "service-hi",configuration = FeignConfig.clas) public interface SchedualServiceHi { @GetMaping(value = "/hi") String sayHiFromClientOne(@RequestParam(value = "name") String name);

}

其配置⽂件如下：

@Configuration public clas FeignConfig {

@Bean public Retryer feignRetryer() {

return new Retryer.Default(10, SECONDS.toMilis(1), 5); }

}

查看FeignClient的源码，其代码如下：

@Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) @Documented public @interface FeignClient {

@AliasFor("name") String value() default ";

@AliasFor("value") String name() default ";

@AliasFor("value") String name() default "; String url() default "; bolean decode404() default false;

Clas<?>[] configuration() default {}; Clas<?> falback() default void.clas;

Clas<?> falbackFactory() default void.clas; }

String path() default ";

bolean primary() default true;

feign ⽤于声明具有该接⼝的REST客户端的接⼝的注释应该是创建（例如⽤于⾃动连接到另⼀个组 件。 如果功能区可⽤，那将是 ⽤于负载平衡后端请求，并且可以配置负载平衡器 使⽤与伪装客户端相 同名称（即值）@Ri bonClient 。 其中value()和name()⼀样，是被调⽤的 service的名称。 url(),直接填写硬编码的url,decode404()即 404是否被解码，还是抛异常；configuration()，标明FeignClient的配置类，默认的配置类为 FeignClientsConfiguration类，可以覆盖Decoder、Encoder和Contract等信息，进⾏⾃定义配置。 falback(),填写熔断器的信息类。

# FeignClient的配置

默认的配置类为FeignClientsConfiguration，这个类在spring-cloud-netflix-core的jar包下，打开这个 类，可以发现它是⼀个配置类，注⼊了很多的相关配置的bean，包括feignRetryer、 FeignLogerFactory、FormatingConversionService等,其中还包括了Decoder、Encoder、 Contract，如果这三个bean在没有注⼊的情况下，会⾃动注⼊默认的配置。

Decoder feignDecoder: ResponseEntityDecoder(这是对SpringDecoder的封装)

Encoder feignEncoder: SpringEncoder

Loger feignLoger: Slf4jLoger

Contract feignContract: SpringMvcContract

Feign.Builder feignBuilder: HystrixFeign.Builder

代码如下：

@Configuration public clas FeignClientsConfiguration {

./省 略 代 码

@Bean

@ConditionalOnMisingBean public Decoder feignDecoder() {

return new ResponseEntityDecoder(new SpringDecoder(this.mesageConverters); }

@Bean @ConditionalOnMisingBean public Encoder feignEncoder() {

return new SpringEncoder(this.mesageConverters); }

@Bean @ConditionalOnMisingBean public Contract feignContract(ConversionService feignConversionService) {

return new SpringMvcContract(this.parameterProcesors, feignConversionService); }

./省 略 代 码 }

重写配置： 你可以重写FeignClientsConfiguration中的bean，从⽽达到⾃定义配置的⽬的，⽐如 FeignClientsConfiguration的默认重试次数为Retryer.NEVER_RETRY，即不重试，那么希望做到重 写，代码如下：

@Configuration public clas FeignConfig {

@Bean public Retryer feignRetryer() {

return new Retryer.Default(10, SECONDS.toMilis(1), 5); }

}

在上述代码更改了该FeignClient的重试次数，重试间隔为10ms，最⼤重试时间为1s,重试次数为5 次。

# Feign的⼯作原理

feign是⼀个伪客户端，即它不做任何的请求处理。Feign通过处理注解⽣成request，从⽽实现简化 HTP API开发的⽬的，即开发⼈员可以使⽤注解的⽅式定制request api模板，在发送htp request请 求之前，feign通过处理注解的⽅式替换掉request模板中的参数，这种实现⽅式显得更为直接、可理 解。 通过包扫描注⼊FeignClient的bean，该源码在FeignClientsRegistrar类： ⾸先在启动配置上检查是否 有@EnableFeignClients注解，如果有该注解，则开启包扫描，扫描被@FeignClient注解接⼝。代码如 下：

private void registerDefaultConfiguration(AnotationMetadata metadata, BeanDefinitionRegistry registry) { Map<String, Object> defaultAtrs = metadata

.getAnotationAtributes(EnableFeignClients.clas.getName(), true);

if (defaultAtrs != nul & defaultAtrs.containsKey("defaultConfiguration") { String name; if (metadata.hasEnclosingClas() {

name = "default." + metadata.getEnclosingClasName();

} else {

name = "default." + metadata.getClasName();

} registerClientConfiguration(registry, name,

defaultAtrs.get("defaultConfiguration"); }

}

扫描到FeignClient后，将信息取出，以bean的形式注⼊到ioc容器中，源码如下：

public void registerFeignClients(AnotationMetadata metadata,

BeanDefinitionRegistry registry) { ClasPathScaningCandidateComponentProvider scaner = getScaner(); scaner.setResourceLoader(this.resourceLoader);

Set<String> basePackages;

Map<String, Object> atrs = metadata

.getAnotationAtributes(EnableFeignClients.clas.getName(); AnotationTypeFilter anotationTypeFilter = new AnotationTypeFilter(

FeignClient.clas); final Clas<?>[] clients = atrs = nul ? nul

: (Clas<?>[]) atrs.get("clients"); if (clients = nul | clients.length = 0) {

scaner.adIncludeFilter(anotationTypeFilter); basePackages = getBasePackages(metadata);

} else {

final Set<String> clientClases = new HashSet<>(); basePackages = new HashSet<>(); for (Clas<?> claz : clients) {

basePackages.ad(ClasUtils.getPackageName(claz); clientClases.ad(claz.getCanonicalName();

} AbstractClasTestingTypeFilter filter = new AbstractClasTestingTypeFilter() {

@Overide protected bolean match(ClasMetadata metadata) {

String cleaned = metadata.getClasName().replaceAl("\$", ".");

return clientClases.contains(cleaned); }

}; scaner.adIncludeFilter(

new AlTypeFilter(Arays.asList(filter, anotationTypeFilter ); }

for (String basePackage : basePackages) { Set<BeanDefinition> candidateComponents = scaner

.findCandidateComponents(basePackage); for (BeanDefinition candidateComponent : candidateComponents) { if (candidateComponent instanceof AnotatedBeanDefinition) { / verify anotated clas is an interface

AnotatedBeanDefinition beanDefinition = (AnotatedBeanDefinition) candidateComponent;

AnotationMetadata anotationMetadata = beanDefinition.getMetadata();

Asert.isTrue(anotationMetadata.isInterface(),

"@FeignClient can only be specified on an interface");

Map<String, Object> atributes = anotationMetadata

.getAnotationAtributes(

FeignClient.clas.getCanonicalName();

String name = getClientName(atributes); registerClientConfiguration(registry, name,

atributes.get("configuration");

registerFeignClient(registry, anotationMetadata, atributes); }

} }

}

private void registerFeignClient(BeanDefinitionRegistry registry,

AnotationMetadata anotationMetadata, Map<String, Object> atributes) { String clasName = anotationMetadata.getClasName(); BeanDefinitionBuilder definition = BeanDefinitionBuilder

.genericBeanDefinition(FeignClientFactoryBean.clas);

validate(atributes); definition.adPropertyValue("url", getUrl(atributes); definition.adPropertyValue("path", getPath(atributes); String name = getName(atributes); definition.adPropertyValue("name", name); definition.adPropertyValue("type", clasName); definition.adPropertyValue("decode404", atributes.get("decode404"); definition.adPropertyValue("falback", atributes.get("falback"); definition.adPropertyValue("falbackFactory", atributes.get("falbackFactory"); definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

String alias = name + "FeignClient"; AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();

bolean primary = (Bolean)atributes.get("primary"); / has a default, won't be nul

beanDefinition.setPrimary(primary);

String qualifier = getQualifier(atributes); if (StringUtils.hasText(qualifier) {

alias = qualifier; }

BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, clasName, new String[] { alias });

BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry); }

注⼊bean之后，通过jdk的代理，当请求Feign Client的⽅法时会被拦截，代码在ReflectiveFeign类， 代码如下：

public <T> T newInstance(Target<T> target) { Map<String, MethodHandler> nameToHandler = targetToHandlersByName.aply(target); Map<Method, MethodHandler> methodToHandler = new LinkedHashMap<Method, MethodHandler>(); List<DefaultMethodHandler> defaultMethodHandlers = new LinkedList<DefaultMethodHandler>();

for (Method method : target.type().getMethods() { if (method.getDeclaringClas() = Object.clas) {

continue;

} else if(Util.isDefault(method) { DefaultMethodHandler handler = new DefaultMethodHandler(method); defaultMethodHandlers.ad(handler); methodToHandler.put(method, handler);

} else {

methodToHandler.put(method, nameToHandler.get(Feign.configKey(target.type(), method ); }

} InvocationHandler handler = factory.create(target, methodToHandler); T proxy = (T) Proxy.newProxyInstance(target.type().getClasLoader(), new Clas<?>[]{target.type()},

handler);

for(DefaultMethodHandler defaultMethodHandler : defaultMethodHandlers) { defaultMethodHandler.bindTo(proxy);

} return proxy;

}

在SynchronousMethodHandler类进⾏拦截处理，当被拦截会根据参数⽣成RequestTemplate对象，该 对象就是htp请求的模板，代码如下：

@Overide

public Object invoke(Object[] argv) throws Throwable { RequestTemplate template = buildTemplateFromArgs.create(argv); Retryer retryer = this.retryer.clone(); while (true) {

try { return executeAndDecode(template);

} catch (RetryableException e) { retryer.continueOrPropagate(e); if (logLevel != Loger.Level.NONE) {

loger.logRetry(metadata.configKey(), logLevel);

} continue;

} }

}

其中有个executeAndDecode()⽅法，该⽅法是通RequestTemplate⽣成Request请求对象，然后根据 ⽤client获取response。

Object executeAndDecode(RequestTemplate template) throws Throwable { Request request = targetRequest(template);

./省 略 代 码

response = client.execute(request, options);

./省 略 代 码

}

# Client组件

其中Client组件是⼀个⾮常重要的组件，Feign最终发送request请求以及接收response响应，都是由 Client组件完成的，其中Client的实现类，只要有Client.Default，该类由HtpURLCo nection实现⽹ 络请求，另外还⽀持HtpClient、Okhtp. ⾸先来看以下在FeignRi bonClient的⾃动配置类，FeignRi bonClientAutoConfiguration ，主要在⼯ 程启动的时候注⼊⼀些bean,其代码如下：

@Bean

@ConditionalOnMisingBean public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory,

SpringClientFactory clientFactory) {

return new LoadBalancerFeignClient(new Client.Default(nul, nul), cachingFactory, clientFactory); }

}

在缺失配置feignClient的情况下，会⾃动注⼊new Client.Default(),跟踪Client.Default()源码，它使⽤ 的⽹络请求框架为HtpURLConection，代码如下：

@Overide

public Response execute(Request request, Options options) throws IOException { HtpURLConection conection = convertAndSend(request, options); return convertResponse(conection).toBuilder().request(request).build();

}

怎么在feign中使⽤HtpClient，查看FeignRi bonClientAutoConfiguration的源码

./省 略 代 码

@Configuration @ConditionalOnClas(ApacheHtpClient.clas) @ConditionalOnProperty(value = "feign.htpclient.enabled", matchIfMising = true) protected static clas HtpClientFeignLoadBalancedConfiguration {

@Autowired(required = false) private HtpClient htpClient;

@Bean @ConditionalOnMisingBean(Client.clas) public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory,

SpringClientFactory clientFactory) {

ApacheHtpClient delegate; if (this.htpClient != nul) {

delegate = new ApacheHtpClient(this.htpClient);

} else {

delegate = new ApacheHtpClient();

} return new LoadBalancerFeignClient(delegate, cachingFactory, clientFactory);

} }

./省 略 代 码 }

## 从代码@ConditionalOnClas(ApacheHtpClient.clas)注解可知道，只需要在pom⽂件加上HtpClient 的claspath就⾏了，另外需要在配置⽂件上加上feign.htpclient.enabled为true，从 @ConditionalOnProperty注解可知，这个可以不写，在默认的情况下就为true. 在pom⽂件加上：

<dependency> <groupId>com.netflix.feign</groupId> <artifactId>feign-htpclient</artifactId> <version>RELEASE</version>

</dependency>

同理，如果想要feign使⽤Okhtp，则只需要在pom⽂件上加上feign-okhtp的依赖：

<dependency> <groupId>com.netflix.feign</groupId> <artifactId>feign-okhtp</artifactId> <version>RELEASE</version>

</dependency>

# feign的负载均衡是怎么样实现的呢？

通过上述的FeignRi bonClientAutoConfiguration类配置Client的类型(htpurlconection，okhtp和 htpclient)时候，可知最终向容器注⼊的是LoadBalancerFeignClient，即负载均衡客户端。现在来看 下LoadBalancerFeignClient的代码：

@Overide public Response execute(Request request, Request.Options options) throws IOException {

try {

URI asUri = URI.create(request.url(); String clientName = asUri.getHost(); URI uriWithoutHost = cleanUrl(request.url(), clientName);

FeignLoadBalancer.Ri bonRequest ri bonRequest = new FeignLoadBalancer.Ri bonRequest(

this.delegate, request, uriWithoutHost);

IClientConfig requestConfig = getClientConfig(options, clientName); return lbClient(clientName).executeWithLoadBalancer(ri bonRequest, requestConfig).toResponse();

} catch (ClientException e) {

IOException io = findIOException(e); if (io != nul) {

throw io;

} throw new RuntimeException(e);

} }

其中有个executeWithLoadBalancer()⽅法，即通过负载均衡的⽅式请求。

public T executeWithLoadBalancer(final S request, final IClientConfig requestConfig) throws

ClientException { RequestSpecificRetryHandler handler = getRequestSpecificRetryHandler(request, requestConfig); LoadBalancerCo mand<T> comand = LoadBalancerCo mand.<T>builder()

.withLoadBalancerContext(this)

.withRetryHandler(handler)

.withLoadBalancerURI(request.getUri()

.build();

try { return comand.submit(

new ServerOperation<T>() { @Overide public Observable<T> cal(Server server) {

URI finalUri = reconstructURIWithServer(server, request.getUri(); S requestForServer = (S) request.replaceUri(finalUri); try {

return

Observable.just(AbstractLoadBalancerAwareClient.this.execute(requestForServer, requestConfig); } catch (Exception e) {

return Observable.eror(e); }

} })

.toBlocking()

.single();

} catch (Exception e) { Throwable t = e.getCause(); if (t instanceof ClientException) { throw (ClientException) t; } else {

throw new ClientException(e); }

}

}

其中服务在submit()⽅法上，点击submit进⼊具体的⽅法,这个⽅法是LoadBalancerComand的⽅ 法：

Observable<T> o = (server = nul ? selectServer() : Observable.just(server)

.concatMap(new Func1<Server, Observable<T>() { @Overide / Caled for each server being selected public Observable<T> cal(Server server) { context.setServer(server);

}

上述代码中有个selectServe()，该⽅法是选择服务的进⾏负载均衡的⽅法，代码如下：

private Observable<Server> selectServer() {

return Observable.create(new OnSubscribe<Server>() { @Overide public void cal(Subscriber<? super Server> next) {

try {

Server server = loadBalancerContext.getServerFromLoadBalancer(loadBalancerURI, loadBalancerKey);

next.onNext(server); next.onCompleted();

} catch (Exception e) { next.onEror(e); }

} });

}

最终负载均衡交给loadBalancerContext来处理，即之前讲述的Ri bon，在这⾥不再重复。

# 总结

总到来说，Feign的源码实现的过程如下：

⾸先通过@EnableFeignCleints注解开启FeignCleint

根据Feign的规则实现接⼝，并加@FeignCleint注解

程序启动后，会进⾏包扫描，扫描所有的@ FeignCleint的注解的类，并将这些信息注⼊到ioc容器 中。

当接⼝的⽅法被调⽤，通过jdk的代理，来⽣成具体的RequesTemplate

RequesTemplate在⽣成Request

Request交给Client去处理，其中Client可以是HtpUrlConection、HtpClient也可以是Okhtp

最后Client被封装到LoadBalanceClient类，这个类结合类Ri bon做到了负载均衡。

# 参考资料

htps:/github.com/OpenFeign/feign

htps:/blog.de-swaef.eu/the-netflix-stack-using-spring-bot-part-3-feign/

本⽂为原创⽂章，转载请标明出处。 本⽂链接：htps:/ w.fangzhipeng.com/springcloud/2017/08/1/sc-feign-raw.html 本⽂出⾃⽅志朋的博客

