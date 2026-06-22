# 1、介绍

- 1.1 什么是响应式编程（Reactive Programing）？ 简单来说，响应式编程是针对异步和事件驱动的⾮阻塞应⽤程序，并且需要少量线程来垂直缩放（即 在 JVM 内）⽽不是⽔平（即通过集群）。 响应式应⽤的⼀个关键⽅⾯是“背压（backpresure）”的概念，这是确保⽣产者不会压倒消费者的机 制。例如，当HTP连接太慢时，从数据库延伸到HTP响应的反应组件的流⽔线、数据存储库也可以 减慢或停⽌，直到⽹络容量释放。 响应式编程也导致从命令式到声明异步组合逻辑的重⼤转变。与使⽤Java 8的 CompletableFuture 编写 封锁代码相⽐，可以通过 lambda 表达式编写后续操作。

- 1.2 响应式 API（Reactive API）和 构建块（Building Blocks） Spring Framework 5 将 Reactive Streams 作为通过异步组件和库进⾏背压通信的合同。Reactive Streams 是通过⾏业协作创建的规范，也已在Java 9中被采⽤为 java.util.concurrent.Flow。 Spring Framework 在内部使⽤ Reactor ⾃⼰的响应⽀持。Reactor 是⼀个 Reactive Streams 实现， 进⼀步扩展基本的 Reactive Streams Publisher 、Flux 和 Mono 可组合的API类型，以提供对 0..N 和 0..1 的数据序列的声明性操作。 Spring Framework 在许多⾃⼰的 Reactive API 中暴露了 Flux 和 Mono。然⽽，在应⽤级别，⼀如既 往，Spring 提供了选择，并完全⽀持使⽤RxJava。有关的更多信息，请查看 Sebastien Deleuze 发表 的 "Understanding Reactive Types" 。


# 2、Spring WebFlux 模块 Spring Framework 5 包括⼀个新的 spring-webflux 模块。该模块包含对响应式 HTP 和 WebSocket 客户端的⽀持，以及对REST，HTML浏览器和 WebSocket⻛格交互的响应式服务器Web应⽤程序的⽀ 持。 2.1、服务器端 在服务器端 WebFlux ⽀持2种不同的编程模型：

基于注解的 @Controller 和其他注解也⽀持 Spring MVC Functional 、Java 8 lambda ⻛格的路由和处理

![image 1](<Spring Boot 2.0 - WebFlux framework.note_images/imageFile1.png>)

WebFlux 可以在⽀持 Servlet 3.1 ⾮阻塞 IO API 以及其他异步运⾏时（如 Nety 和 Undertow ）的 Servlet 容器上运⾏。每个运⾏时都适⽤于响应型 ServerHttpRequest 和 ServerHttpResponse，将请求 和响应的正⽂暴露为 Flux<DataBuffer>，⽽不是具有响应背压的 InputStream 和 OutputStream 。顶部 作为 Flux<Object> ⽀持REST⻛格的 JSON 和 XML 序列化和反序列化，HTML视图呈现和服务器发送 事件也是如此。 基于注解的编程模式

WebFlux中也⽀持相同的 @Controller 编程模型和 Spring MVC 中使⽤的相同注解。主要区别在于底层 核⼼框架契约（即 HandlerMappingHandlerAdapter ）是⾮阻塞的，并且在响应型 ServerHttpRequest和 ServerHttpResponse 上运⾏，⽽不是在 HttpServletRequest 和 HttpServletResponse 上运⾏。以下是

⼀个响应式 Controler 的例⼦：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.


@RestController public class PersonController {

private final PersonRepository repository; public PersonController(PersonRepository repository) {

this.repository = repository;

} @PostMapping("/person") Mono<Void> create(@RequestBody Publisher<Person> personStream) {

return this.repository.save(personStream).then();

} @GetMapping("/person") Flux<Person> list() {

return this.repository.findAll();

} @GetMapping("/person/{id}") Mono<Person> findById(@PathVariable String id) {

return this.repository.findOne(id);

- 18.
- 19.


} }

函数式编程模式 HandlerFunctions 传⼊的HTP请求由 HandlerFunction 处理， HandlerFunction 本质上是⼀个接收 ServerRequest 并返 回 Mono<ServerResponse> 的函数。处理函数的注解对应⽅法将是⼀个 @RequestMapping 的⽅法。

ServerRequest 和 ServerResponse 是提供JDK-8友好访问底层HTP消息的不可变接⼝。两者都通过在 反应堆顶部建⽴完全反应：请求将身体暴露为 Flux 或 Mono; 响应接受任何 ReactiveStreamsPublisher 作为主体。

ServerRequest 可以访问各种HTP请求元素：⽅法，URI，查询参数，以及通过单独的 ServerRequest.Headers 接⼝ - 头。通过 body⽅法 提供对 body 的访问。例如，这是如何将请求体提 取为 Mono<String>：

- 1.


Mono<String> string = request.bodyToMono(String.class);

这⾥是如何将身体提取为 Flux，其中 Person 是可以从body内容反序列化的类（即如果body包含 JSON，则由Jackson⽀持，或者如果是XML，则为JAXB）。

- 1.


Flux<Person> people = request.bodyToFlux(Person.class);

上⾯的两个⽅法（ bodyToMono 和 bodyToFlux）实际上是使⽤通⽤ ServerRequest.body（ BodyExtractor）函数的便利⽅法。 BodyExtractor 是⼀个功能策略界⾯，允许您编写⾃⼰的提取逻 辑，但在 BodyExtractors 实⽤程序类中可以找到常⻅的 BodyExtractor实例。所以，上⾯的例⼦可以 替换为：

- 1.
- 2.


Mono<String> string = request.body(BodyExtractors.toMono(String.class); Flux<Person> people = request.body(BodyExtractors.toFlux(Person.class);

类似地， ServerResponse 提供对HTP响应的访问。由于它是不可变的，您可以使⽤构建器创建⼀个 ServerResponse 。构建器允许您设置响应状态，添加响应标题并提供正⽂。例如，这是如何使⽤20 OK状态创建响应，JSON内容类型和正⽂：

- 1.
- 2.


Mono<Person> person = ... ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(person);

这⾥是如何使⽤201创建的状态，位置标题和空⽩体来构建响应：

- 1.
- 2.


URI location = ... ServerResponse.created(location).build();

将这些组合在⼀起可以创建⼀个 HandlerFunction。例如，这⾥是⼀个简单的“Helo World”处理程序 lambda 的示例，它返回⼀个20状态的响应和⼀个基于 String 的主体：

- 1.
- 2.


HandlerFunction<ServerResponse> helloWorld = request -> ServerResponse.ok().body(fromObject("Hello World"));

使⽤ lambda 写处理函数，就像我们上⾯所说的那样很⽅便，但是在处理多个函数时可能缺乏可读性， 变得不那么容易维护。因此，建议将相关处理函数分组到⼀个处理程序或控制器类中。例如，这是⼀ 个暴露了⼀个响应式的 Person 存储库的类：

import static org.springframework.http.MediaType.APPLICATION_JSON; import static org.springframework.web.reactive.function.BodyInserters.fromObject; public class PersonHandler {

- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.


private final PersonRepository repository; public PersonHandler(PersonRepository repository) {

this.repository = repository;

} // 1 public Mono<ServerResponse> listPeople(ServerRequest request) {

Flux<Person> people = repository.allPeople(); return ServerResponse.ok().contentType(APPLICATION_JSON).body(people,

Person.class); }

- // 2 public Mono<ServerResponse> createPerson(ServerRequest request) {

Mono<Person> person = request.bodyToMono(Person.class); return ServerResponse.ok().build(repository.savePerson(person));

}

- // 3 public Mono<ServerResponse> getPerson(ServerRequest request) {


int personId = Integer.valueOf(request.pathVariable("id")); Mono<ServerResponse> notFound = ServerResponse.notFound().build(); Mono<Person> personMono = this.repository.getPerson(personId); return personMono

.flatMap(person -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(person)))

.switchIfEmpty(notFound); }

}

- 1/listPeople 是⼀个处理函数，它将数据库中发现的所有 Person对象返回为JSON。

- 2/createPerson 是⼀个处理函数，⽤于存储请求正⽂中包含的新 Person。请注意， PersonRepository.savePerson(Person) 返回 Mono<Void>：发出完成信号的空 Mono，当⼈从请求中 读取并存储时，发出完成信号。因此，当接收到完成信号时，即当 Person已被保存时，我们使⽤ build(Publisher<Void>) ⽅法来发送响应。

- 3/getPerson 是⼀个处理函数，它通过路径变量id来标识⼀个⼈。我们通过数据库检索该 Person， 并创建⼀个JSON响应（如果找到）。如果没有找到，我们使⽤ switchIfEmpty(Mono<T>) 来返回 404 Not Found 响应。


RouterFunctions 传⼊请求将路由到处理函数，并使⽤⼀个 RouterFunction，它是⼀个服务器 ServerRequest的函数，并 返回⼀个 Mono<HandlerFunction>。如果请求与特定路由匹配，则返回处理函数; 否则返回⼀个空的 Mono。 RouterFunction 与 @Controller 类中的 @RequestMapping 注解类似。

通常，您不要⾃⼰编写路由器功能，⽽是使⽤ RouterFunctions.route(RequestPredicate,HandlerFunction)， 使⽤请求谓词和处理函数创建⼀个。 如果谓词适⽤，请求将路由到给定的处理函数; 否则不执⾏路由，导致 404 Not Found 响应。虽然您 可以编写⾃⼰的 RequestPredicate ，但是您不需要： RequestPredicates 实⽤程序类提供常⽤的谓 词，基于路径，HTP⽅法，内容类型等进⾏匹配。使⽤路由，我们可以路由到我们的 “Helo World” 处理函数：

- 1.
- 2.
- 3.


RouterFunction<ServerResponse> helloWorldRoute = RouterFunctions.route(RequestPredicates.path("/hello-world"), request -> Response.ok().body(fromObject("Hello World")));

两个路由功能可以组成⼀个新的路由功能，路由到任⼀处理函数：如果第⼀个路由的谓词不匹配，则 第⼆个被评估。组合的路由器功能按顺序进⾏评估，因此在通⽤功能之前放置特定功能是有意义的。 您可以通过调⽤ RouterFunction.and(RouterFunction) 或通过调⽤

RouterFunction.andRoute(RequestPredicate,HandlerFunction) 来组成两个路由功能，这是 RouterFunction.and() 与 RouterFunctions.route() 的⼀种⽅便组合。 给定我们上⾯显示的 PersonHandler，我们现在可以定义路由功能，路由到相应的处理函数。我们使 ⽤ ⽅法引⽤（method-references） 来引⽤处理函数：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


import static org.springframework.http.MediaType.APPLICATION_JSON; import static org.springframework.web.reactive.function.server.RequestPredicates.*; PersonRepository repository = ... PersonHandler handler = new PersonHandler(repository); RouterFunction<ServerResponse> personRoute =

route(GET("/person/{id}").and(accept(APPLICATION_JSON)), handler::getPerson)

.andRoute(GET("/person").and(accept(APPLICATION_JSON)), handler::listPeople)

.andRoute(POST("/person").and(contentType(APPLICATION_JSON)), handler::createPerson);

除路由功能之外，您还可以通过调⽤RequestPredicate.and(RequestPredicate) 或

RequestPredicate.or(RequestPredicate) 来构成请求谓词。这些⼯作正如预期的那样：如果给定的谓 词匹配，则⽣成的谓词匹配; 或者如果任⼀谓词都匹配。 RequestPredicates 中发现的⼤多数谓词是组 合的。例如， RequestPredicates.GET(String) 是 RequestPredicates.method(HttpMethod) 和

RequestPredicates.path(String) 的组合。 启动服务器 现在只有⼀个难题遗留：在HTP服务器中运⾏路由功能。您可以使⽤ RouterFunctions.toHttpHandler(RouterFunction) 将路由功能转换为 HttpHandler。 HttpHandler允许 您运⾏各种响应场景：Reactor Nety，Servlet 3.1和Undertow。以下是在 Reactor Nety 中运⾏路由 功能的⽅法，例如：

- 1.
- 2.


RouterFunction<ServerResponse> route = ... HttpHandler httpHandler = RouterFunctions.toHttpHandler(route);

ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler); HttpServer server = HttpServer.create(HOST, PORT); server.newHandler(adapter).block();

- 4.
- 5.


对于 Tomcat ，它看起来像这样：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


RouterFunction<ServerResponse> route = ... HttpHandler httpHandler = RouterFunctions.toHttpHandler(route); HttpServlet servlet = new ServletHttpHandlerAdapter(httpHandler); Tomcat server = new Tomcat(); Context rootContext = server.addContext("", System.getProperty("java.io.tmpdir")); Tomcat.addServlet(rootContext, "servlet", servlet); rootContext.addServletMapping("/", "servlet"); tomcatServer.start();

待完成：DispatcherHandlerHandlerFilterFunction 路由功能映射的路由可以通过调⽤ RouterFunction.filter(HandlerFilterFunction) 进⾏过滤，其中HandlerFilterFunction 本质上是⼀ 个接收 ServerRequest 和HandlerFunction 的函数，并返回⼀个 ServerResponse 。处理函数参数表示链 中的下⼀个元素：通常是路由到的 HandlerFunction ，但是如果应⽤了多个过滤器，也可以是另⼀个

FilterFunction 。使⽤注解，可以使⽤ @ControllerAdvice 和 / 或 ServletFilter 来实现类似的功能。 让我们在我们的路由中添加⼀个简单的安全过滤器，假设我们有⼀个 SecurityManager 可以确定是否允 许特定的路径：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.


import static org.springframework.http.HttpStatus.UNAUTHORIZED; SecurityManager securityManager = ... RouterFunction<ServerResponse> route = ... RouterFunction<ServerResponse> filteredRoute =

route.filter(request, next) -> { if (securityManager.allowAccessTo(request.path())) { return next.handle(request);

} else {

return ServerResponse.status(UNAUTHORIZED).build(); }

});

在这个例⼦中可以看到，调⽤ next.handle(ServerRequest) 是可选的：我们只允许在允许访问时执⾏ 处理函数。

- 2.2 客户端（Client Side） WebFlux 包括⼀个 functional, reactive WebClient，它为 RestTemplate 提供了⼀种完全⽆阻塞和响应 式的替代⽅案。 它将⽹络输⼊和输出公开为客户端 HttpRequest 和 ClientHttpResponse ，其中请求和 响应的主体是 Flux<DataBuffer>⽽不是 InputStream 和 OutputStream。此外，它还⽀持与服务器端相 同的响应式 JSON，XML和 SE 序列化机制，因此您可以使⽤类型化对象。以下是使⽤需要 ClientHttpConnector 实现的 WebClient 插⼊特定HTP客户端（如 Reactor Nety）的示例：


- 1.


WebClient client = WebClient.create("http://example.com");

- 2.
- 3.
- 4.
- 5.
- 6.


Mono<Account> account = client.get()

.url("/accounts/{id}", 1L)

.accept(APPLICATION_JSON)

.exchange(request)

.flatMap(response -> response.bodyToMono(Account.class));

AsyncRestTemplate 还⽀持⾮阻塞交互。主要区别在于它不⽀持⾮阻塞流，例如 Twiter one ，因 为它基本上仍然依赖于 InputStream 和OutputStream。

- 2.4 请求体和响应体的转换（Request and Response Body Conversion） spring-core 模块提供了响应式 Encoder(编码器) 和 Decoder(解码器)，使得能够串⾏化字符串与类型对 象的转换。 spring-web 模块添加了 JSON（Jackson）和 XML（JAXB）实现，⽤于Web应⽤程序以及 其他⽤于 SE流和零拷⻉⽂件传输。 ⽀持以下 Reactive API：


Reactor 3.x ⽀持开箱即⽤ io.reactivex.rxjava2:rxjava 依赖项在类路径上时⽀持 RxJava 2.x 当 ·io.reactivex:rxjava和io.reactivex:rxjava-reactive-streams`（RxJava 和 Reactive Streams 之间 的适配器）依赖关系在类路径上时，⽀持 RxJava 1.x

例如，请求体可以是以下⽅式之⼀，它将在注解和功能编程模型中⾃动解码：

Accountaccount - 在调⽤控制器之前，acount 将⽆阻塞地被反序列化。 Mono<Account>account - controler 可以使⽤ Mono 来声明在反序列化 acount 后执⾏的逻辑。 Single<Account>account - 和 Mono 类似，但是⽤的是 RxJava Flux<Account>accounts - 输⼊流场景 Observable<Account>accounts - RxJava 的 输⼊流场景

响应体（response body）可以是以下之⼀：

Mono<Account> - 当 Mono 完成时，序列化⽽不阻塞给定的Acount。 Single<Account> - 与上类似，但是使⽤的 RxJava Flux<Account> - 流式场景，可能是 SE，具体取决于所请求的内容类型。 Observable<Account> - 与上类似， 但是使⽤的 RxJavaObservable 类型 Flowable<Account> - 与上类似， 但是使⽤的 RxJava 2Flowable 类型。 Publisher<Account> 或 Flow.Publisher<Account> - ⽀持任何实现Reactive StreamsPublisher 的类 型。 Flux<ServerSentEvent> - SE 流。 Mono<Void> - 当 Mono 完成时，请求处理完成。 Account - 序列化⽽不阻塞给定的Acount; 意味着同步、⾮阻塞的 Controler ⽅法。 Void - 特定于基于注解的编程模型，⽅法返回时，请求处理完成; 意味着同步、⾮阻塞的 Controler ⽅法。

当使⽤像 Flux 或 Observable 这样的流类型时，请求/响应或映射/路由级别中指定的媒体类型⽤于确定 数据应如何序列化和刷新。例如，返回 Flux<Account> 的REST端点将默认序列化如下：

application/json :Flux<Account> 作为异步集合处理，并在完成事件发布时将其序列化为具有显式 刷新的JSON数组。 application/stream+json : ⼀个 Flux<Account> 将作为⼀系列的 Account 元素处理，作为以新⾏分 隔的单个JSON对象，并在每个元素之后显式刷新。WebClient ⽀持JSON流解码，因此这对于服务 器到服务器的⽤例来说是⼀个很好的⽤例。 text/event-stream : ⼀个 Flux<Account> 或 Flux<ServerSentEvent<Account>>将作为⼀

个 Stream 或 ServerSentEvent 元素的流处理，作为单独的 SE 元素，使⽤默认的JSON进⾏数据编 码和每个元素之间的显式刷新。这⾮常适合将流暴露给浏览器客户端。WebClient 也⽀持读取 SE 流。

- 2.4 响应式 Websocket ⽀持 WebFlux 包括响应式 WebSocket 客户端和服务器⽀持。Java WebSocket API（JSR-356），Jety， Undertow和Reactor Nety都⽀持客户端和服务器。 在服务器端，声明⼀个 WebSocketHandlerAdapter，然后简单地添加映射到基于 WebSocketHandler 的端 点：


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.


@Bean public HandlerMapping webSocketMapping() {

Map<String, WebSocketHandler> map = new HashMap<>();

map.put("/foo", new FooWebSocketHandler()); map.put("/bar", new BarWebSocketHandler()); SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();

mapping.setOrder(10); mapping.setUrlMap(map); return mapping;

} @Bean public WebSocketHandlerAdapter handlerAdapter() {

return new WebSocketHandlerAdapter(); }

在客户端，为上⾯列出的⽀持的库之⼀创建⼀个 WebSocketClient：

- 1.
- 2.


WebSocketClient client = new ReactorNettyWebSocketClient(); client.execute("ws://localhost:8080/echo"), session -> {... }).blockMillis(5000);

- 2.5 测试 spring-test 模块包括⼀个 WebTestClient，可⽤于测试具有或不具有正在运⾏的服务器的 WebFlux 服 务器端点。 没有运⾏服务器的测试与来⾃Spring MVC的 MockMvc 相当，其中使⽤模拟请求和响应，⽽不是使⽤套 接字通过⽹络连接。然⽽， WebTestClient 也可以针对正在运⾏的服务器执⾏测试。 更多请查看 sample tests

3、开始⼊⻔

- 3.1 Spring Bot Starter


通过 htp:/start.spring.io 提供的 Spring Bot WebFlux 启动器是最快的⼊⻔⽅式。它做所有必要的， 所以你开始像Spring MVC⼀样编写@Controler类。只需转到 htp:/start.spring.io ，选择版本

- 2.0.0.BUILD-SNAPSHOT，并在依赖关系框中键⼊ respond。 默认情况下，启动器使⽤ Reactor Nety 运⾏，但依赖关系可以像往常⼀样通过 Spring Bot 更改为不同的运⾏时。有关更多详细信息和说明， 请参阅 Spring Bo t参考⽂档⻚⾯。

- 3.2 ⼿动引导（Manual Botstraping） 对于依赖关系，从 spring-webflux 和 spring-context 开始。 然后添加 jackson-databind 和 io.netty:netty-buffer（暂时⻅SPR-14528）以获得JSON⽀持。最后添加⼀个⽀持的运⾏时的依赖 项：


Tomcat —org.apache.tomcat.embed:tomcat-embed-core Jetty —org.eclipse.jety:jety-server 和 org.eclipse.jety:jety-servlet ReactorNetty —io.projectreactor.ipc:reactor-nety Undertow —io.undertow:undertow-core

基于注解编程模式的引导：

- 1.
- 2.


ApplicationContext context = new AnnotationConfigApplicationContext(DelegatingWebFluxConfiguration.class); // (1) HttpHandler handler = DispatcherHandler.toHttpHandler(context); // (2)

以上加载默认的 Spring Web 框架配置（1），然后创建⼀个 DispatcherHandler，主类驱动请求处理 （2），并适应 HttpHandler - 响应式HTP请求处理的最低级别的Spring抽象。 函数编程模式的引导：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(); // (1) context.registerBean(FooBean.class, () -> new FooBeanImpl()); // (2) context.registerBean(BarBean.class); // (3) context.refresh(); HttpHandler handler = WebHttpHandlerBuilder

.webHandler(RouterFunctions.toHttpHandler(...))

.applicationContext(context)

.build(); // (4)

以上创建了⼀个 AnnotationConfigApplicationContext 实例（1），可以利⽤新的功能 bean 注册API （2）使⽤ Java 8 供应商注册 bean，或者只需通过指定其类（3）即可。 HttpHandler是使⽤ WebHttpHandlerBuilder（4）创建的。 然后可以将 HttpHandler 安装在⽀持的运⾏服务器之⼀中：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


// Tomcat and Jetty (also see notes below) HttpServlet servlet = new ServletHttpHandlerAdapter(handler);

... // Reactor Netty ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler); HttpServer.create(host, port).newHandler(adapter).block();

- 7.
- 8.
- 9.
- 10.


// Undertow UndertowHttpHandlerAdapter adapter = new UndertowHttpHandlerAdapter(handler); Undertow server = Undertow.builder().addHttpListener(port, host).setHandler(adapter).build(); server.start();

对于特别是使⽤ WAR 部署的 Servlet 容器，可以使⽤作为 WebApplicationInitializer 的 AbstractAnnotationConfigDispatcherHandlerInitializer，并由 Servlet容器⾃动检测。它负责注 册 ServletHttpHandlerAdapter ，如上所示。您将需要实现⼀个抽象⽅法来指向您的 Spring 配 置。

- 3.3 Examples 您将在以下项⽬中找到有助于构建反应式 Web 应⽤程序的代码示例：


Functional progra ming model sample Spring Reactive Playground: playground for most Spring Web reactive features Reactor website: the spring-functional branch is a Spring 5 functional, Java 8 lambda-style aplication Spring Reactive University sesion: live-coded project from this Devox BE 2106 university talk Reactive Thymeleaf Sandbox Mix-it 2017 website: Kotlin + Reactive + Functional web and bean registration API aplication Reactor by example: code sni pets coming from this InfoQ article Spring integration tests: various features tested with Reactor StepVerifier

