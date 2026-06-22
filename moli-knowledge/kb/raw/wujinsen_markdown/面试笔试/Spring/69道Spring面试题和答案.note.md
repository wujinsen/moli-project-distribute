# Spring概述

- 1. 什么是spring? Spring 是个java企业级应⽤的开源开发框架。Spring主要⽤来开发Java应⽤，但是有些扩展是针对构 建J2E平台的web应⽤。Spring 框架⽬标是简化Java企业级应⽤开发，并通过POJO为基础的编程模 型促进良好的编程习惯。

- 2. 使⽤Spring框架的好处是什么？

- 3.Spring由哪些模块组成? 以下是Spring 框架的基本模块：

- 4. 核⼼容器（应⽤上下⽂) 模块。 这是基本的Spring模块，提供spring 框架的基础功能，BeanFactory 是 任何以spring为基础的应⽤的 核⼼。Spring 框架建⽴在此模块之上，它使Spring成为⼀个容器。

- 5. BeanFactory – BeanFactory 实现举例。


轻量：Spring 是轻量的，基本的版本⼤约2MB。 控制反转：Spring通过控制反转实现了松散耦合，对象们给出它们的依赖，⽽不是创建或查找依赖 的对象们。 ⾯向切⾯的编程(AOP)：Spring⽀持⾯向切⾯的编程，并且把应⽤业务逻辑和系统服务分开。 容器：Spring 包含并管理应⽤中对象的⽣命周期和配置。 MVC框架：Spring的WEB框架是个精⼼设计的框架，是Web框架的⼀个很好的替代品。 事务管理：Spring 提供⼀个持续的事务管理接⼝，可以扩展到上⾄本地事务下⾄全局事务 （JTA）。 异常处理：Spring 提供⽅便的API把具体技术相关的异常（⽐如由JDBC，Hibernate or JDO抛出 的）转化为⼀致的unchecked 异常。

Core module Bean module Context module Expresion Language module JDBC module ORM module OXM module Java Mesaging Service(JMS) module Transaction module Web module Web-Servlet module Web-Struts module Web-Portlet module

Bean ⼯⼚是⼯⼚模式的⼀个实现，提供了控制反转功能，⽤来把应⽤的配置和依赖从正真的应⽤代码 中分离。 最常⽤的BeanFactory 实现是XmlBeanFactory 类。

- 6. XMLBeanFactory 最常⽤的就是org.springframework.beans.factory.xml.XmlBeanFactory ，它根据XML⽂件中的定义加 载beans。该容器从XML ⽂件读取配置元数据并⽤它去创建⼀个完全配置的系统或应⽤。

- 7. 解释AOP模块 AOP模块⽤于发给我们的Spring应⽤做⾯向切⾯的开发， 很多⽀持由AOP联盟提供，这样就确保了 Spring和其他AOP框架的共通性。这个模块将元数据编程引⼊Spring。

- 8. 解释JDBC抽象和DAO模块。 通过使⽤JDBC抽象和DAO模块，保证数据库代码的简洁，并能避免数据库资源错误关闭导致的问题， 它在各种不同的数据库的错误信息之上，提供了⼀个统⼀的异常访问层。它还利⽤Spring的AOP 模块 给Spring应⽤中的对象提供事务管理服务。

- 9. 解释对象/关系映射集成模块。 Spring 通过提供ORM模块，⽀持我们在直接JDBC之上使⽤⼀个对象/关系映射映射(ORM)⼯具， Spring ⽀持集成主流的ORM框架，如Hiberate,JDO和 iBATIS SQL Maps。Spring的事务管理同样⽀持 以上所有ORM框架及JDBC。

- 10.解释WEB 模块。 Spring的WEB模块是构建在aplication context 模块基础之上，提供⼀个适合web应⽤的上下⽂。这个 模块也包括⽀持多种⾯向web的任务，如透明地处理多个⽂件上传请求和程序级请求参数的绑定到你 的业务对象。它也有对Jakarta Struts的⽀持。


- 12.Spring配置⽂件 Spring配置⽂件是个XML ⽂件，这个⽂件包含了类信息，描述了如何配置它们，以及如何相互调⽤。

- 13.什么是Spring IOC 容器？ Spring IOC 负责创建对象，管理对象（通过依赖注⼊（DI），装配对象，配置对象，并且管理这些对 象的整个⽣命周期。

- 14.IOC的优点是什么？ IOC 或 依赖注⼊把应⽤的代码量降到最低。它使应⽤容易测试，单元测试不再需要单例和JNDI查找机 制。最⼩的代价和最⼩的侵⼊性使松散耦合得以实现。IOC容器⽀持加载服务时的饿汉式初始化和懒加 载。

- 15. AplicationContext通常的实现是什么? FileSystemXmlAplicationContext ：此容器从⼀个XML⽂件中加载beans的定义，XML Bean 配 置⽂件的全路径名必须提供给它的构造函数。 ClasPathXmlAplicationContext：此容器也从⼀个XML⽂件中加载beans的定义，这⾥，你需 要正确设置claspath因为这个容器将在claspath⾥找bean配置。


WebXmlAplicationContext：此容器加载⼀个XML⽂件，此⽂件定义了⼀个WEB应⽤的所有 bean。

- 16. Bean ⼯⼚和 Aplication contexts 有什么区别？ Aplication contexts提供⼀种⽅法处理⽂本消息，⼀个通常的做法是加载⽂件资源（⽐如镜像），它 们可以向注册为监听器的bean发布事件。另外，在容器或容器内的对象上执⾏的那些不得不由bean⼯ ⼚以程序化⽅式处理的操作，可以在Aplication contexts中以声明的⽅式处理。Aplication contexts 实现了MesageSource接⼝，该接⼝的实现以可插拔的⽅式提供获取本地化消息的⽅法。

- 17. ⼀个Spring的应⽤看起来象什么？

依赖注⼊

- 18. 什么是Spring的依赖注⼊？ 依赖注⼊，是IOC的⼀个⽅⾯，是个通常的概念，它有多种解释。这概念是说你不⽤创建对象，⽽只需 要描述它如何被创建。你不在代码⾥直接组装你的组件和服务，但是要在配置⽂件⾥描述哪些组件需 要哪些服务，之后⼀个容器（IOC容器）负责把他们组装起来。

- 19.有哪些不同类型的IOC（依赖注⼊）⽅式？

- 20. 哪种依赖注⼊⽅式你建议使⽤，构造器注⼊，还是 Seter⽅法注⼊？ 你两种依赖⽅式都可以使⽤，构造器注⼊和Seter⽅法注⼊。最好的解决⽅案是⽤构造器参数实现强制 依赖，seter⽅法实现可选依赖。

SpringBeans

- 21.什么是Spring beans? Spring beans 是那些形成Spring应⽤的主⼲的java对象。它们被Spring IOC容器初始化，装配，和管 理。这些beans通过容器中配置的元数据创建。⽐如，以XML⽂件中<bean/> 的形式定义。 Spring 框架定义的beans都是单件beans。在bean tag中有个属性”singleton”，如果它被赋为TRUE， bean 就是单件，否则就是⼀个 prototype bean。默认是TRUE，所以所有在Spring框架中的beans 缺 省都是单件。


⼀个定义了⼀些功能的接⼝。 这实现包括属性，它的Seter ， geter ⽅法和函数等。 Spring AOP。 Spring 的XML 配置⽂件。 使⽤以上功能的客户端程序。

构造器依赖注⼊：构造器依赖注⼊通过容器触发⼀个类的构造器来实现的，该类有⼀系列参数，每 个参数代表⼀个对其他类的依赖。 Seter⽅法注⼊：Seter⽅法注⼊是容器通过调⽤⽆参构造器或⽆参static⼯⼚ ⽅法实例化bean之 后，调⽤该bean的seter⽅法，即实现了基于seter的依赖注⼊。

## 2. ⼀个 Spring Bean 定义 包含什么？

⼀个Spring Bean 的定义包含容器必知的所有配置元数据，包括如何创建⼀个bean，它的⽣命周期详 情及它的依赖。

- 23. 如何给Spring 容器提供配置元数据? 这⾥有三种重要的⽅法给Spring 容器提供配置元数据。 XML配置⽂件。 基于注解的配置。 基于java的配置。

- 24. 你怎样定义类的作⽤域? 当定义⼀个<bean> 在Spring⾥，我们还能给这个bean声明⼀个作⽤域。它可以通过bean 定义中的 scope属性来定义。如，当Spring要在需要的时候每次⽣产⼀个新的bean实例，bean的scope属性被 指定为prototype。另⼀⽅⾯，⼀个bean每次使⽤的时候必须返回同⼀个实例，这个bean的scope 属 性 必须设为 singleton。

- 25. 解释Spring⽀持的⼏种bean的作⽤域。 Spring框架⽀持以下五种bean的作⽤域：

缺省的Spring bean 的作⽤域是Singleton.

- 26. Spring框架中的单例bean是线程安全的吗? 不，Spring框架中的单例bean不是线程安全的。

- 27. 解释Spring框架中bean的⽣命周期。

- 28.哪些是重要的bean⽣命周期⽅法？ 你能重载它们吗？


singleton :bean在每个Spring ioc 容器中只有⼀个实例。 prototype：⼀个bean的定义可以有多个实例。 request：每次htp请求都会创建⼀个bean，该作⽤域仅在基于web的Spring AplicationContext 情形下有效。 sesion：在⼀个HTP Sesion中，⼀个bean定义对应⼀个实例。该作⽤域仅在基于web的 Spring AplicationContext情形下有效。 global-sesion：在⼀个全局的HTP Sesion中，⼀个bean定义对应⼀个实例。该作⽤域仅在基 于web的Spring AplicationContext情形下有效。

Spring容器 从XML ⽂件中读取bean的定义，并实例化bean。 Spring根据bean的定义填充所有的属性。 如果bean实现了BeanNameAware 接⼝，Spring 传递bean 的ID 到 setBeanName⽅法。 如果Bean 实现了 BeanFactoryAware 接⼝， Spring传递beanfactory 给setBeanFactory ⽅法。 如果有任何与bean相关联的BeanPostProcesors，Spring会在postProceserBeforeInitialization() ⽅法内调⽤它们。 如果bean实现IntializingBean了，调⽤它的afterPropertySet⽅法，如果bean声明了初始化⽅法， 调⽤此初始化⽅法。 如果有BeanPostProcesors 和bean 关联，这些bean的postProcesAfterInitialization() ⽅法将被 调⽤。 如果bean实现了 DisposableBean，它将调⽤destroy()⽅法。

有两个重要的bean ⽣命周期⽅法，第⼀个是setup ， 它是在容器加载bean的时候被调⽤。第⼆个⽅法 是 teardown 它是在容器卸载类的时候被调⽤。 The bean 标签有两个重要的属性（init-method和destroy-method）。⽤它们你可以⾃⼰定制初始化 和注销⽅法。它们也有相应的注解（@PostConstruct和@PreDestroy）。

- 29. 什么是Spring的内部bean？ 当⼀个bean仅被⽤作另⼀个bean的属性时，它能被声明为⼀个内部bean，为了定义i ner bean，在 Spring 的 基于XML的 配置元数据中，可以在 <property/>或 <constructor-arg/> 元素内使⽤ <bean/> 元素，内部bean通常是匿名的，它们的Scope⼀般是prototype。

- 30. 在 Spring中如何注⼊⼀个java集合？ Spring提供以下⼏种集合的配置元素：

- 31. 什么是bean装配? 装配，或bean 装配是指在Spring 容器中把bean组装到⼀起，前提是容器需要知道bean的依赖关系， 如何通过依赖注⼊来把它们装配到⼀起。

- 32. 什么是bean的⾃动装配？ Spring 容器能够⾃动装配相互合作的bean，这意味着容器不需要<constructor-arg>和<property>配 置，能通过Bean⼯⼚⾃动处理bean之间的协作。


<list>类型⽤于注⼊⼀列值，允许有相同的值。 <set> 类型⽤于注⼊⼀组值，不允许有相同的值。 <map> 类型⽤于注⼊⼀组键值对，键和值都可以为任意类型。 <props>类型⽤于注⼊⼀组键值对，键和值都只能为String类型。

## 3. 解释不同⽅式的⾃动装配 。

有五种⾃动装配的⽅式，可以⽤来指导Spring容器⽤⾃动装配⽅式来进⾏依赖注⼊。

no：默认的⽅式是不进⾏⾃动装配，通过显式设置ref 属性来进⾏装配。 byName：通过参数名 ⾃动装配，Spring容器在配置⽂件中发现bean的autowire属性被设置成 byname，之后容器试图匹配、装配和该bean的属性具有相同名字的bean。 byType:：通过参数类型⾃动装配，Spring容器在配置⽂件中发现bean的autowire属性被设置成 byType，之后容器试图匹配、装配和该bean的属性具有相同类型的bean。如果有多个bean符合条 件，则抛出错误。 constructor：这个⽅式类似于byType， 但是要提供给构造器参数，如果没有确定的带参数的构造 器参数类型，将会抛出异常。 autodetect：⾸先尝试使⽤constructor来⾃动装配，如果⽆法⼯作，则使⽤byType⽅式。

- 34.⾃动装配有哪些局限性 ? ⾃动装配的局限性是：


重写： 你仍需⽤ <constructor-arg>和 <property> 配置来定义依赖，意味着总要重写⾃动装配。 基本数据类型：你不能⾃动装配简单的属性，如基本数据类型，String字符串，和类。 模糊特性：⾃动装配不如显式装配精确，如果有可能，建议使⽤显式装配。

- 35. 你可以在Spring中注⼊⼀个nul 和⼀个空字符串吗？ 可以。

Spring注解

- 36. 什么是基于Java的Spring注解配置? 给⼀些注解的例⼦. 基于Java的配置，允许你在少量的Java注解的帮助下，进⾏你的⼤部分Spring配置⽽⾮通过XML⽂ 件。 以@Configuration 注解为例，它⽤来标记类可以当做⼀个bean的定义，被Spring IOC容器使⽤。另⼀ 个例⼦是@Bean注解，它表示此⽅法将要返回⼀个对象，作为⼀个bean注册进Spring应⽤上下⽂。

- 37. 什么是基于注解的容器配置? 相对于XML⽂件，注解型的配置依赖于通过字节码元数据装配组件，⽽⾮尖括号的声明。 开发者通过在相应的类，⽅法或属性上使⽤注解的⽅式，直接组件类中进⾏配置，⽽不是使⽤xml表述 bean的装配关系。

- 38. 怎样开启注解装配？ 注解装配在默认情况下是不开启的，为了使⽤注解装配，我们必须在Spring配置⽂件中配 置 <context:anotation-config/>元素。

- 39. @Required 注解 这个注解表明bean的属性必须在配置的时候设置，通过⼀个bean定义的显式的属性值或通过⾃动装 配，若@Required注解的bean属性未被设置，容器将抛出BeanInitializationException。

- 40. @Autowired 注解 @Autowired 注解提供了更细粒度的控制，包括在何处以及如何完成⾃动装配。它的⽤法和@Required ⼀样，修饰seter⽅法、构造器、属性或者具有任意名称和/或多个参数的PN⽅法。

- 41. @Qualifier 注解 当有多个相同类型的bean却只有⼀个需要⾃动装配时，将@Qualifier 注解和@Autowire 注解结合使⽤ 以消除这种混淆，指定需要装配的确切的bean。

Spring数据访问

- 42.在Spring框架中如何更有效地使⽤JDBC? 使⽤SpringJDBC 框架，资源管理和错误处理的代价都会被减轻。所以开发者只需写 statements 和 queries从数据存取数据，JDBC也可以在Spring框架提供的模板类的帮助下更有效地被 使⽤，这个模板叫JdbcTemplate （例⼦⻅这⾥ ）

- 43. JdbcTemplate JdbcTemplate 类提供了很多便利的⽅法解决诸如把数据库数据转变成基本数据类型或对象，执⾏写好 的或可调⽤的数据库操作语句，提供⾃定义的数据错误处理。


here

## 4. Spring对DAO的⽀持

Spring对数据访问对象（DAO）的⽀持旨在简化它和数据访问技术如JDBC，Hibernate or JDO 结合使 ⽤。这使我们可以⽅便切换持久层。编码时也不⽤担⼼会捕获每种技术特有的异常。

- 45. 使⽤Spring通过什么⽅式访问Hibernate? 在Spring中有两种⽅式访问Hibernate：

- 46. Spring⽀持的ORM Spring⽀持以下ORM：

- 47.如何通过HibernateDaoSuport将Spring和Hibernate结合起来？ ⽤Spring的 SesionFactory 调⽤ LocalSesionFactory。集成过程分三步：

- 48. Spring⽀持的事务管理类型 Spring⽀持两种类型的事务管理：

- 49. Spring框架的事务管理有哪些优点？

- 50. 你更倾向⽤那种事务管理类型？ ⼤多数Spring框架的⽤户选择声明式事务管理，因为它对应⽤代码的影响最⼩，因此更符合⼀个⽆侵 ⼊的轻量级容器的思想。声明式事务管理要优于编程式事务管理，虽然⽐编程式事务管理（这种⽅式 允许你通过代码控制事务）少了⼀点灵活性。

Spring⾯向切⾯编程（AOP）

- 51.解释AOP ⾯向切⾯的编程，或AOP， 是⼀种编程技术，允许程序模块化横向切割关注点，或横切典型的责任划 分，如⽇志和事务管理。

- 52. Aspect 切⾯


控制反转 Hibernate Template和 Calback。 继承 HibernateDAOSuport提供⼀个AOP 拦截器。

Hibernate iBatis JPA (Java Persistence API) TopLink JDO (Java Data Objects) OJB

配置the Hibernate SesionFactory。 继承HibernateDaoSuport实现⼀个DAO。 在AOP⽀持的事务中装配。

编程式事务管理：这意味你通过编程的⽅式管理事务，给你带来极⼤的灵活性，但是难维护。 声明式事务管理：这意味着你可以将业务代码和事务管理分离，你只需⽤注解和XML配置来管理事 务。

它为不同的事务API 如 JTA，JDBC，Hibernate，JPA 和JDO，提供⼀个不变的编程模式。 它为编程式事务管理提供了⼀套简单的API⽽不是⼀些复杂的事务API如 它⽀持声明式事务管理。 它和Spring各种数据访问抽象层很好得集成。

AOP核⼼就是切⾯，它将多个类的通⽤⾏为封装成可重⽤的模块，该模块含有⼀组API提供横切功能。 ⽐如，⼀个⽇志模块可以被称作⽇志的AOP切⾯。根据需求的不同，⼀个应⽤程序可以有若⼲切⾯。 在Spring AOP中，切⾯通过带有@Aspect注解的类实现。

## 52. 在Spring AOP 中，关注点和横切关注的区别是什么？

关注点是应⽤中⼀个模块的⾏为，⼀个关注点可能会被定义成⼀个我们想实现的⼀个功能。 横切关注点是⼀个关注点，此关注点是整个应⽤都会使⽤的功能，并影响整个应⽤，⽐如⽇志，安全 和数据传输，⼏乎应⽤的每个模块都需要的功能。因此这些都属于横切关注点。

## 54. 连接点

连接点代表⼀个应⽤程序的某个位置，在这个位置我们可以插⼊⼀个AOP切⾯，它实际上是个应⽤程 序执⾏Spring AOP的位置。

## 5. 通知

通知是个在⽅法执⾏前或执⾏后要做的动作，实际上是程序执⾏时要通过SpringAOP框架触发的代码 段。 Spring切⾯可以应⽤五种类型的通知：

before：前置通知，在⼀个⽅法执⾏前被调⽤。 after:在⽅法执⾏之后调⽤的通知，⽆论⽅法执⾏是否成功。 after-returning:仅当⽅法成功完成后执⾏的通知。 after-throwing:在⽅法抛出异常退出时执⾏的通知。 around:在⽅法执⾏之前和之后调⽤的通知。

- 56. 切点 切⼊点是⼀个或⼀组连接点，通知将在这些位置执⾏。可以通过表达式或匹配的⽅式指明切⼊点。

- 57. 什么是引⼊? 引⼊允许我们在已存在的类中增加新的⽅法和属性。

- 58. 什么是⽬标对象? 被⼀个或者多个切⾯所通知的对象。它通常是⼀个代理对象。也指被通知（advised）对象。

- 59. 什么是代理? 代理是通知⽬标对象后创建的对象。从客户端的⻆度看，代理对象和⽬标对象是⼀样的。

- 60. 有⼏种不同类型的⾃动代理？ BeanNameAutoProxyCreator DefaultAdvisorAutoProxyCreator Metadata autoproxying

- 61. 什么是织⼊。什么是织⼊应⽤的不同点？ 织⼊是将切⾯和到其他应⽤类型或对象连接或创建⼀个被通知对象的过程。 织⼊可以在编译时，加载时，或运⾏时完成。

- 62. 解释基于XML Schema⽅式的切⾯实现。 在这种情况下，切⾯由常规类以及基于XML的配置实现。


- 63. 解释基于注解的切⾯实现 在这种情况下(基于@AspectJ的实现)，涉及到的切⾯声明的⻛格与带有java5标注的普通java类⼀致。

Spring的MVC

- 64. 什么是Spring的MVC框架？ Spring 配备构建Web 应⽤的全功能MVC框架。Spring可以很便捷地和其他MVC框架集成，如Struts， Spring 的MVC框架⽤控制反转把业务对象和控制逻辑清晰地隔离。它也允许以声明的⽅式把请求参数 和业务对象绑定。

- 65. DispatcherServlet Spring的MVC框架是围绕DispatcherServlet来设计的，它⽤来处理所有的HTP请求和响应。


## 6. WebAplicationContext

WebAplicationContext 继承了AplicationContext并增加了⼀些WEB应⽤必备的特有功能，它不同 于⼀般的AplicationContext ，因为它能处理主题，并找到被关联的servlet。

- 67. 什么是Spring MVC框架的控制器？ 控制器提供⼀个访问应⽤程序的⾏为，此⾏为通常通过服务接⼝实现。控制器解析⽤户输⼊并将其转 换为⼀个由视图呈现给⽤户的模型。Spring⽤⼀个⾮常抽象的⽅式实现了⼀个控制层，允许⽤户创建 多种⽤途的控制器。

- 68. @Controler 注解 该注解表明该类扮演控制器的⻆⾊，Spring不需要你继承任何其他控制器基类或引⽤Servlet API。

- 69. @RequestMaping 注解 该注解是⽤来映射⼀个URL到⼀个类或⼀个特定的⽅处理法上。


