- 1.声明bean的注解 @Component 组件，没有明确的⻆⾊ @Service 在业务逻辑层使⽤（service层） @Repository 在数据访问层使⽤（dao层） @Controler 在展现层使⽤，控制器的声明（C）
- 2.注⼊bean的注解 @Autowired：由Spring提供 @Inject：由JSR-30提供 @Resource：由JSR-250提供 都可以注解在set⽅法和属性上，推荐注解在属性上（⼀⽬了然，少写代码）。
- 3.java配置类相关注解 @Configuration 声明当前类为配置类，相当于xml形式的Spring配置（类上） @Bean 注解在⽅法上，声明当前⽅法的返回值为⼀个bean，替代xml中的⽅式（⽅法上） @Configuration 声明当前类为配置类，其中内部组合了@Component注解，表明这个类是⼀个bean （类上） @ComponentScan ⽤于对Component进⾏扫描，相当于xml中的（类上） @WishlyConfiguration 为@Configuration与@ComponentScan的组合注解，可以替代这两个注解
- 4.切⾯（AOP）相关注解 Spring⽀持AspectJ的注解式切⾯编程。 @Aspect 声明⼀个切⾯（类上） 使⽤@After、@Before、@Around定义建⾔（advice），可直接将拦截规则（切点）作为参数。 @After 在⽅法执⾏之后执⾏（⽅法上） @Before 在⽅法执⾏之前执⾏（⽅法上） @Around 在⽅法执⾏之前与之后执⾏（⽅法上） @PointCut 声明切点 在java配置类中使⽤@EnableAspectJAutoProxy注解开启Spring对AspectJ代理的⽀持（类上）
- 5.@Bean的属性⽀持 @Scope 设置Spring容器如何新建Bean实例（⽅法上，得有@Bean） 其设置类型包括： Singleton （单例,⼀个Spring容器中只有⼀个bean实例，默认模式）, Protetype （每次调⽤新建⼀个bean）, Request （web项⽬中，给每个htp request新建⼀个bean）, Sesion （web项⽬中，给每个htp sesion新建⼀个bean）, GlobalSesion（给每⼀个 global htp sesion新建⼀个Bean实例） @StepScope 在Spring Batch中还有涉及 @PostConstruct 由JSR-250提供，在构造函数执⾏完之后执⾏，等价于xml配置⽂件中bean的 initMethod


- @PreDestory 由JSR-250提供，在Bean销毁之前执⾏，等价于xml配置⽂件中bean的destroyMethod
- 6.@Value注解 @Value 为属性注⼊值（属性上） ⽀持如下⽅式的注⼊： 》注⼊普通字符 @Value("Michael Jackson")String name; 》注⼊操作系统属性 @Value("#{systemProperties['os.name']}")String osName; 》注⼊表达式结果 @Value("#{ T(java.lang.Math).random() *10 }")String randomNumber; 》注⼊其它bean属性 @Value("#{domeClas.name}")String name; 》注⼊⽂件资源 @Value("claspath:com/hgs/helo/test.txt")String Resource file; 》注⼊⽹站资源 @Value("htp:/ w.javastack.cn")Resource url; 》注⼊配置⽂件 Value("${bok.name}")String bokName; 注⼊配置使⽤⽅法：

- ① 编写配置⽂件（test.properties） bok.name=《三体》

- ② @PropertySource 加载配置⽂件(类上) @PropertySource("claspath:com/hgs/helo/test/test.propertie")

- ③ 还需配置⼀个PropertySourcesPlaceholderConfigurer的bean。


- 7.环境切换 @Profile 通过设定Environment的ActiveProfiles来设定当前context需要使⽤的配置环境。（类或⽅法 上） @Conditional Spring4中可以使⽤此注解定义条件话的bean，通过实现Condition接⼝，并重写 matches⽅法，从⽽决定该bean是否被实例化。（⽅法上）

@EnableAsync 配置类中，通过此注解开启对异步任务的⽀持，叙事性AsyncConfigurer接⼝（类 上），点击 了解使⽤详情。 @Async 在实际执⾏的bean⽅法使⽤该注解来申明其是⼀个异步任务（⽅法上或类上所 有 的 ⽅ 法 都 将 异 步 ，需要@EnableAsync开启异步任务）

- 9.定时任务相关 @EnableScheduling 在配置类上使⽤，开启计划任务的⽀持（类上）


- 8.异步相关


# 这 ⾥

@Scheduled 来申明这是⼀个任务，包括cron,fixDelay,fixRate等类型（⽅法上，需先开启计划任务的 ⽀持）

- 10.@Enable*注解说明 这些注解主要⽤来开启对 x的⽀持。 @EnableAspectJAutoProxy 开启对AspectJ⾃动代理的⽀持 @EnableAsync 开启异步⽅法的⽀持 @EnableScheduling 开启计划任务的⽀持 @EnableWebMvc 开启Web MVC的配置⽀持 @EnableConfigurationProperties 开启对@ConfigurationProperties注解配置Bean的⽀持 @EnableJpaRepositories 开启对SpringData JPA Repository的⽀持 @EnableTransactionManagement 开启注解式事务的⽀持 @EnableTransactionManagement 开启注解式事务的⽀持 @EnableCaching 开启注解式的缓存⽀持


1.测试相关注解

@RunWith 运⾏器，Spring中通常⽤于对JUnit的⽀持 @RunWith(SpringJUnit4ClasRuner.clas) @ContextConfiguration ⽤来加载配置AplicationContext，其中clases属性⽤来加载配置类 @ContextConfiguration(clases={TestConfig.clas}) 12.SpringMVC相关注解 @EnableWebMvc 在配置类中开启Web MVC的配置⽀持，如⼀些ViewResolver或者 MesageConverter等，若⽆此句，重写WebMvcConfigurerAdapter⽅法（⽤于对SpringMVC的配 置）。 @Controler 声明该类为SpringMVC中的Controler @RequestMaping ⽤于映射Web请求，包括访问路径和参数（类或⽅法上） @ResponseBody ⽀持将返回值放在response内，⽽不是⼀个⻚⾯，通常⽤户返回json数据（返回值 旁或⽅法上） @RequestBody 允许request的参数在request体中，⽽不是在直接连接在地址后⾯。（放在参数前） @PathVariable ⽤于接收路径参数，⽐如@RequestMaping(“/helo/{name}”)申明的路径，将注解放在 参数中前，即可获取该值，通常作为Restful的接⼝实现⽅法。 @RestControler 该注解为⼀个组合注解，相当于@Controler和@ResponseBody的组合，注解在类 上，意味着，该Controler的所有⽅法都默认加上了@ResponseBody。

@ControlerAdvice 通过该注解，我们可以将对于控制器的全局配置放置在同⼀个位置，注解了 @Controler的类的⽅法可使⽤@ExceptionHandler、@InitBinder、@ModelAtribute注解到⽅法上， 这对所有注解了 @RequestMaping的控制器内的⽅法有效。 @ExceptionHandler ⽤于全局处理控制器⾥的异常 @InitBinder ⽤来设置WebDataBinder，WebDataBinder⽤来⾃动绑定前台请求参数到Model中。

## @ModelAtribute 本来的作⽤是绑定键值对到Model⾥，在@ControlerAdvice中是让全局的 @RequestMaping都能获得在此处设置的键值对。

