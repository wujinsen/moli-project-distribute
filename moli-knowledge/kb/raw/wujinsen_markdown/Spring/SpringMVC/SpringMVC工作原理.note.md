作者：平凡希 https://www.cnblogs.com/xiaoxi/p/6164383.html Java架构师之路做了编排

⼀：SpringMVC的⼯作原理图

![image 1](<SpringMVC工作原理.note_images/imageFile1.png>)

# ⼆：SpringMVC流程

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


⽤户发送请求⾄前端控制器DispatcherServlet。 DispatcherServlet收到请求调⽤HandlerMapping处理器映射器。 处理器映射器找到具体的处理器(可以根据xml配置、注解进⾏查找)，⽣成处理器对象及处理器拦 截器(如果有则⽣成)⼀并返回给DispatcherServlet。 DispatcherServlet调⽤HandlerAdapter处理器适配器。 HandlerAdapter经过适配调⽤具体的处理器(Controller，也叫后端控制器)。 Controller执⾏完成返回ModelAndView。 HandlerAdapter将controller执⾏结果ModelAndView返回给DispatcherServlet。 DispatcherServlet将ModelAndView传给ViewReslover视图解析器。 ViewReslover解析后返回具体View。 DispatcherServlet根据View进⾏渲染视图（即将模型数据填充⾄视图中）。 DispatcherServlet响应⽤户。

三：组件说明

以下组件通常使⽤框架提供实现： DispatcherServlet：作为前端控制器，整个流程控制的中⼼，控制其它组件执⾏，统⼀调度，降低组件 之间的耦合性，提⾼每个组件的扩展性。

HandlerMapping：通过扩展处理器映射器实现不同的映射⽅式，例如：配置⽂件⽅式，实现接⼜⽅ 式，注解⽅式等。 HandlAdapter：通过扩展处理器适配器，⽀持更多类型的处理器。 ViewResolver：通过扩展视图解析器，⽀持更多类型的视图解析，例如：jsp、freemarker、pdf、excel 等。

组件：

- 1.前端控制器DispatcherServlet（不需要⼯程师开发）,由框架提供 作⽤：接收请求，响应结果，相当于转发器，中央处理器。有了dispatcherServlet减少了其它组件之间 的耦合度。 ⽤户请求到达前端控制器，它就相当于mvc模式中的c，dispatcherServlet是整个流程控制的中⼼，由它 调⽤其它组件处理⽤户的请求，dispatcherServlet的存在降低了组件之间的耦合性。

- 2.处理器映射器HandlerMapping(不需要⼯程师开发),由框架提供 作⽤：根据请求的url查找Handler HandlerMapping负责根据⽤户请求找到Handler即处理器，springmvc提供了不同的映射器实现不同的映 射⽅式，例如：配置⽂件⽅式，实现接⼜⽅式，注解⽅式等。

- 3.处理器适配器HandlerAdapter 作⽤：按照特定规则（HandlerAdapter要求的规则）去执⾏Handler 通过HandlerAdapter对处理器进⾏执⾏，这是适配器模式的应⽤，通过扩展适配器可以对更多类型的处 理器进⾏执⾏。

- 4.处理器Handler(需要⼯程师开发) 注意：编写Handler时按照HandlerAdapter的要求去做，这样适配器才可以去正确执⾏Handler Handler 是继DispatcherServlet前端控制器的后端控制器，在DispatcherServlet的控制下Handler对具体的 ⽤户请求进⾏处理。 由于Handler涉及到具体的⽤户业务请求，所以⼀般情况需要⼯程师根据业务需求开发Handler。

- 5.视图解析器View resolver(不需要⼯程师开发),由框架提供 作⽤：进⾏视图解析，根据逻辑视图名解析成真正的视图（view） View Resolver负责将处理结果⽣成View视图，View Resolver⾸先根据逻辑视图名解析成物理视图名即具 体的页⾯地址，再⽣成View视图对象，最后对View进⾏渲染将处理结果通过页⾯展⽰给⽤户。 springmvc框架提供了很多的View视图类型，包括：jstlView、freemarkerView、pdfView等。 ⼀般情况下需要通过页⾯标签或页⾯模版技术将模型数据通过页⾯展⽰给⽤户，需要由⼯程师根据业 务需求开发具体的页⾯。

- 6.视图View(需要⼯程师开发jsp...) View是⼀个接⼜，实现类⽀持不同的View类型（jsp、freemarker、pdf...）


核⼼架构的具体流程步骤如下：

- 1.⾸先⽤户发送请求——>DispatcherServlet，前端控制器收到请求后⾃⼰不进⾏处理，⽽是委托给其他 的解析器进⾏处理，作为统⼀访问点，进⾏全局的流程控制；

- 2.DispatcherServlet——>HandlerMapping， HandlerMapping 将会把请求映射为HandlerExecutionChain 对象（包含⼀个Handler 处理器（页⾯控制器）对象、多个HandlerInterceptor 拦截器）对象，通过这种 策略模式，很容易添加新的映射策略；

- 3.DispatcherServlet——>HandlerAdapter，HandlerAdapter 将会把处理器包装为适配器，从⽽⽀持多种 类型的处理器，即适配器设计模式的应⽤，从⽽很容易⽀持很多类型的处理器；

- 4.HandlerAdapter——>处理器功能处理⽅法的调⽤，HandlerAdapter 将会根据适配的结果调⽤真正的 处理器的功能处理⽅法，完成功能处理；并返回⼀个ModelAndView 对象（包含模型数据、逻辑视图 名）；

- 5.ModelAndView的逻辑视图名——> ViewResolver， ViewResolver 将把逻辑视图名解析为具体的View， 通过这种策略模式，很容易更换其他视图技术；

- 6.View——>渲染，View会根据传进来的Model模型数据进⾏渲染，此处的Model实际是⼀个Map数据结 构，因此很容易⽀持其他视图技术；


7返回控制权给DispatcherServlet，由DispatcherServlet返回响应给⽤户，到此⼀个流程结束。

下边两个组件通常情况下需要开发： Handler：处理器，即后端控制器⽤controller表⽰。 View：视图，即展⽰给⽤户的界⾯，视图中通常需要标签语⾔展⽰模型数据。

四：什么是MVC模式

MVC：MVC是⼀种设计模式 MVC的原理图：

![image 2](<SpringMVC工作原理.note_images/imageFile2.png>)

分析： M-Model 模型（完成业务逻辑：有javaBean构成，service+dao+entity） V-View 视图（做界⾯的展⽰ jsp，html……） C-Controller 控制器（接收请求—>调⽤模型—>根据结果派发页⾯）

五：Spring MVC是什么

springMVC是⼀个MVC的开源框架，springMVC=struts2+spring，springMVC就相当于是Struts2加上 sring的整合，但是这⾥有⼀个疑惑就是，springMVC和spring是什么样的关系呢？这个在百度百科上有 ⼀个很好的解释：意思是说，springMVC是spring的⼀个后续产品，其实就是spring在原有基础上，又提 供了web应⽤的MVC模块，可以简单的把springMVC理解为是spring的⼀个模块（类似AOP，IOC这样的 模块），⽹络上经常会说springMVC和spring⽆缝集成，其实springMVC就是spring的⼀个⼦模块，所以 根本不需要同spring进⾏整合。

六：Spring MVC原理图

![image 3](<SpringMVC工作原理.note_images/imageFile3.png>)

看到这个图⼤家可能会有很多的疑惑，现在我们来看⼀下这个图的步骤：（可以对⽐MVC的原理图进 ⾏理解） 第⼀步：⽤户发起请求到前端控制器（DispatcherServlet） 第⼆步：前端控制器请求处理器映射器（HandlerMappering）去查找处理器（Handle）,通过xml配置或 者注解进⾏查找 第 三 步 ： 找 到 以 后 处 理 器 映 射 器 （ HandlerMappering ） 像 前 端 控 制 器 返 回 执 ⾏ 链 （HandlerExecutionChain） 第四步：前端控制器（DispatcherServlet）调⽤处理器适配器（HandlerAdapter）去执⾏处理器 （Handler） 第五步：处理器适配器去执⾏Handler 第六步：Handler执⾏完给处理器适配器返回ModelAndView 第七步：处理器适配器向前端控制器返回ModelAndView 第⼋步：前端控制器请求视图解析器（ViewResolver）去进⾏视图解析 第九步：视图解析器像前端控制器返回View 第⼗步：前端控制器对视图进⾏渲染 第⼗⼀步：前端控制器向⽤户响应结果

看到这些步骤我相信⼤家很感觉⾮常的乱，这是正常的，但是这⾥主要是要⼤家理解springMVC中的⼏ 个组件： 前端控制器（DispatcherServlet）：接收请求，响应结果，相当于电脑的CPU。 处理器映射器（HandlerMapping）：根据URL去查找处理器 处理器（Handler）：（需要程序员去写代码处理逻辑的）

处理器适配器（HandlerAdapter）：会把处理器包装成适配器，这样就可以⽀持多种类型的处理器，类 ⽐笔记本的适配器（适配器模式的应⽤） 视图解析器（ViewResovler）：进⾏视图解析，多返回的字符串，进⾏处理，可以解析成对应的页⾯

-END近期热⽂：

