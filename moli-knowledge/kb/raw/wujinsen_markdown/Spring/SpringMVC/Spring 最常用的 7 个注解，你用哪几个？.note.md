随着技术的更新迭代，Java5.0开始⽀持注解。⽽作为java中的领军框架spring，⾃从更新了2.5版本之 后也开始慢慢舍弃xml配置，更多使⽤注解来控制spring框架。 ⽽spring的的注解那么多，可能做java很多年，都⽤不上。这⾥按照类型总结了这7种最常⽤的注解。

- 1 核⼼注解


@Required 此注解⽤于bean的seter⽅法上。表示此属性是必须的，必须在配置阶段注⼊，否则会抛出 BeanInitializationExcepion。 @Autowired 此注解⽤于bean的field、seter⽅法以及构造⽅法上，显式地声明依赖。根据type来autowiring。 当在field上使⽤此注解，并且使⽤属性来传递值时，Spring会⾃动把值赋给此field。也可以将此注解⽤ 于私有属性(不推荐)，如下。

@Component public class User {

@Autowired private Address address;

}

最经常的⽤法是将此注解⽤于seter上，这样可以在seter⽅法中添加⾃定义代码。如下：

@Component public class User {

private Address address;

@AutoWired public setAddress(Address address) {

// custom code this.address=address;

} }

当在构造⽅法上使⽤此注解的时候，需要注意的⼀点就是⼀个类中只允许有⼀个构造⽅法使⽤此注 解。

此外，在Spring4.3后，如果⼀个类仅仅只有⼀个构造⽅法，那么即使不使⽤此注解，那么Spring也会 ⾃动注⼊相关的bean。如下：

@Component

public class User { private Address address;

public User(Address address) {

this.address=address; }

}

<bean id="user" class="xx.User"/>

@Qualifier 此注解是和@Autowired⼀起使⽤的。使⽤此注解可以让你对注⼊的过程有更多的控制。 @Qualifier可以被⽤在单个构造器或者⽅法的参数上。当上下⽂有⼏个相同类型的bean, 使⽤ @Autowired则⽆法区分要绑定的bean，此时可以使⽤@Qualifier来指定名称。

@Component public class User {

@Autowired @Qualifier("address1") private Address address;

... }

@Configuration 此注解⽤在clas上来定义bean。其作⽤和xml配置⽂件相同，表示此bean是⼀个Spring配置。此外， 此类可以使⽤@Bean注解来初始化定义bean。

@Configuartion public class SpringCoreConfig {

@Bean public AdminUser adminUser() {

AdminUser adminUser = new AdminUser(); return adminUser;

} }

@ComponentScan 此注解⼀般和@Configuration注解⼀起使⽤，指定Spring扫描注解的package。如果没有指定包，那么 默认会扫描此配置类所在的package。 @Lazy 此注解使⽤在Spring的组件类上。默认的，Spring中Bean的依赖⼀开始就被创建和配置。如果想要延 迟初始化⼀个bean，那么可以在此类上使⽤Lazy注解，表示此bean只有在第⼀次被使⽤的时候才会被 创建和初始化。

此注解也可以使⽤在被@Configuration注解的类上，表示其中所有被@Bean注解的⽅法都会延迟初始 化。 @Value 此注解使⽤在字段、构造器参数和⽅法参数上。@Value可以指定属性取值的表达式，⽀持通过#{}使⽤ SpringEL来取值，也⽀持使⽤${}来将属性来源中(Properties⽂件、本地环境变量、系统属性等)的值注 ⼊到bean的属性中。 推荐⼤家看下： ，这篇也是必看了。 此注解值的注⼊发⽣在AutowiredAnotationBeanPostProcesor类中。

Java 必须掌握的 12 种 Spring 常⽤注解

- 2 Spring MVC和REST注解


@Controler 此注解使⽤在clas上声明此类是⼀个Spring controler，是@Component注解的⼀种具体形式。 @RequestMaping 此注解可以⽤在clas和method上，⽤来映射web请求到某⼀个handler类或者handler⽅法上。 当此注解⽤在Clas上时，就创造了⼀个基础url，其所有的⽅法上的@RequestMaping都是在此url之 上的。 可以使⽤其method属性来限制请求匹配的htp method。

@Controller @RequestMapping("/users") public class UserController {

@RequestMapping(method = RequestMethod.GET) public String getUserList() {

return "users"; }

}

Spring MVC常⽤注解

这篇也推荐⼤家看下： 。 此外，Spring4.3之后引⼊了⼀系列@RequestMaping的变种。如下：

@GetMaping @PostMaping @PutMaping @PatchMaping @DeleteMaping

分别对应了相应method的RequestMaping配置。

关注微信公众号：Java技术栈，在后台回复：spring，可以获取我整理的 N 篇最新 Spring 教程，都是 ⼲货。 @CokieValue 此注解⽤在@RequestMaping声明的⽅法的参数上，可以把HTP cokie中相应名称的cokie绑定上 去。

@ReuestMapping("/cookieValue")

public void getCookieValue(@CookieValue("JSESSIONID") String cookie){ }

cokie即htp请求中name为JSESIONID的cokie值。 @CrosOrigin 此注解⽤在clas和method上⽤来⽀持跨域请求，是Spring 4.2后引⼊的。

@CrossOrigin(maxAge = 3600) @RestController @RequestMapping("/users") public class AccountController {

@CrossOrigin(origins = "http://xx.com") @RequestMapping("/login") public Result userLogin() {

// ... }

}

@ExceptionHandler 此注解使⽤在⽅法级别，声明对Exception的处理逻辑。可以指定⽬标Exception。 @InitBinder 此注解使⽤在⽅法上，声明对WebDataBinder的初始化(绑定请求参数到JavaBean上的DataBinder)。 在controler上使⽤此注解可以⾃定义请求参数的绑定。 @MatrixVariable 此注解使⽤在请求handler⽅法的参数上，Spring可以注⼊matrix url中相关的值。这⾥的矩阵变量可以 出现在url中的任何地⽅，变量之间⽤;分隔。如下：

// GET /pets/42;q=11;r=22 @RequestMapping(value = "/pets/{petId}") public void findPet(@PathVariable String petId, @MatrixVariable int q) {

// petId == 42 // q == 11

}

需要注意的是默认Spring mvc是不⽀持矩阵变量的，需要开启。

<mvc:annotation-driven enable-matrix-variables="true" />

注解配置则需要如下开启：

@Configuration public class WebConfig extends WebMvcConfigurerAdapter {

@Override public void configurePathMatch(PathMatchConfigurer configurer) {

UrlPathHelper urlPathHelper = new UrlPathHelper(); urlPathHelper.setRemoveSemicolonContent(false); configurer.setUrlPathHelper(urlPathHelper);

} }

@PathVariable 此注解使⽤在请求handler⽅法的参数上。@RequestMaping可以定义动态路径，如:

@RequestMapping("/users/{uid}")

可以使⽤@PathVariable将路径中的参数绑定到请求⽅法参数上。

@RequestMapping("/users/{uid}") public String execute(@PathVariable("uid") String uid){ }

关注微信公众号：Java技术栈，在后台回复：spring，可以获取我整理的 N 篇最新 Spring 系列程，都 是⼲货。

@RequestAtribute

此注解⽤在请求handler⽅法的参数上，⽤于将web请求中的属性(request atributes，是服务器放⼊的 属性值)绑定到⽅法参数上。 @RequestBody 此注解⽤在请求handler⽅法的参数上，⽤于将htp请求的Body映射绑定到此参数上。 HtpMesageConverter负责将对象转换为htp请求。 @RequestHeader 此注解⽤在请求handler⽅法的参数上，⽤于将htp请求头部的值绑定到参数上。 @RequestParam 此注解⽤在请求handler⽅法的参数上，⽤于将htp请求参数的值绑定到参数上。 @RequestPart 此注解⽤在请求handler⽅法的参数上，⽤于将⽂件之类的multipart绑定到参数上。

@ResponseBody 此注解⽤在请求handler⽅法上。和@RequestBody作⽤类似，⽤于将⽅法的返回对象直接输出到htp 响应中。 @ResponseStatus 此注解⽤于⽅法和exception类上，声明此⽅法或者异常类返回的htp状态码。可以在Controler上使⽤ 此注解，这样所有的@RequestMaping都会继承。 @ControlerAdvice 此注解⽤于clas上。前⾯说过可以对每⼀个controler声明⼀个ExceptionMethod。 这⾥可以使⽤@ControlerAdvice来声明⼀个类来统⼀对所有@RequestMaping⽅法来做 @ExceptionHandler、@InitBinder以及@ModelAtribute处理。 @RestControler 此注解⽤于clas上，声明此controler返回的不是⼀个视图⽽是⼀个领域对象。其同时引⼊了 @Controler和@ResponseBody两个注解。 @RestControlerAdvice 此注解⽤于clas上，同时引⼊了@ControlerAdvice和@ResponseBody两个注解。 @SesionAtribute 此注解⽤于⽅法的参数上，⽤于将sesion中的属性绑定到参数。 @SesionAtributes 此注解⽤于type级别，⽤于将JavaBean对象存储到sesion中。⼀般和@ModelAtribute注解⼀起使 ⽤。如下：

@ModelAttribute("user")

public PUser getUser() {}

// controller和 上 ⾯ 的 代 码 在 同 ⼀ controller中 @Controller @SeesionAttributes(value = "user", types = {

User.class })

public class UserController {}

- 3 Spring Bot注解


@EnableAutoConfiguration

此注解通常被⽤在主应⽤clas上，告诉Spring Bot⾃动基于当前包添加Bean、对bean的属性进⾏设 置等。 @SpringBotAplication 此注解⽤在Spring Bot项⽬的应⽤主类上（此类需要在base package中）。 使⽤了此注解的类⾸先会让Spring Bot启动对base package以及其sub-pacakage下的类进⾏ component scan。这篇整理的也⾮常全： ，建议⼤家看下。 此注解同时添加了以下⼏个注解：

Spring Bot 最核⼼的 25 个注解

@Configuration @EnableAutoConfiguration @ComponentScan

- 4 Stereotype注解

@Component 此注解使⽤在clas上来声明⼀个Spring组件(Bean), 将其加⼊到应⽤上下⽂中。 @Controler 前⽂已经提到过 @Service 此注解使⽤在clas上，声明此类是⼀个服务类，执⾏业务逻辑、计算、调⽤内部api等。是 @Component注解的⼀种具体形式。 @Repository 此类使⽤在clas上声明此类⽤于访问数据库，⼀般作为DAO的⻆⾊。 此注解有⾃动翻译的特性，例如：当此种component抛出了⼀个异常，那么会有⼀个handler来处理此 异常，⽆需使⽤try-catch块。

- 5 数据访问注解

@Transactional 此注解使⽤在接⼝定义、接⼝中的⽅法、类定义或者类中的public⽅法上。需要注意的是此注解并不激 活事务⾏为，它仅仅是⼀个元数据，会被⼀些运⾏时基础设施来消费。

- 6


任务执⾏、调度注解

@Scheduled 此注解使⽤在⽅法上，声明此⽅法被定时调度。使⽤了此注解的⽅法返回类型需要是Void，并且不能 接受任何参数。

@Scheduled(fixedDelay=1000) public void schedule() {

}

@Scheduled(fixedRate=1000) public void schedulg() {

}

第⼆个与第⼀个不同之处在于其不会等待上⼀次的任务执⾏结束。 @Async 此注解使⽤在⽅法上，声明此⽅法会在⼀个单独的线程中执⾏。不同于Scheduled注解，此注解可以接 受参数。 使⽤此注解的⽅法的返回类型可以是Void也可是返回值。但是返回值的类型必须是⼀个Future。

- 7 测试注解


@ContextConfiguration 此注解使⽤在Clas上，声明测试使⽤的配置⽂件，此外，也可以指定加载上下⽂的类。 此注解⼀般需要搭配SpringJUnit4ClasRuner使⽤。

@RunWith(SpringJUnit4ClassRunner.class) @ContextConfiguration(classes = SpringCoreConfig.class) public class UserServiceTest {

}

# -END-

