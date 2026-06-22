- 1.简单⼯⼚(⾮23种设计模式中的⼀种)

- 2.⼯⼚⽅法

- 3.单例模式

- 4.适配器模式

- 5.装饰器模式

- 6.代理模式

- 7.观察者模式

- 8.策略模式

- 9.模版⽅法模式


![image 1](<Spring 中经典的 9 种设计模式，打死也要记住啊！.note_images/imageFile1.png>)

Spring中涉及的设计模式总结

# 1.简单⼯⼚(⾮23种设计模式中的⼀种) 实现⽅式：

BeanFactory。Spring中的BeanFactory就是简单⼯⼚模式的体现，根据传⼊⼀个唯⼀的标识来获得Bean 对象，但是否是在传⼊参数后创建还是传⼊参数前创建这个要根据具体情况来定。

实质：

由⼀个⼯⼚类根据传⼊的参数，动态决定应该创建哪⼀个产品类。

实现原理：

bean容器的启动阶段：

读取bean的xml配置⽂件,将bean元素分别转换成⼀个BeanDefinition对象。

然后通过BeanDefinitionRegistry将这些bean注册到beanFactory中，保存在它的⼀个 ConcurrentHashMap中。

将BeanDefinition注册到了beanFactory之后，在这⾥Spring为我们提供了⼀个扩展的切⼝，允许我们 通过实现接⼝BeanFactoryPostProcessor 在此处来插⼊我们定义的代码。

典型的例⼦就是：PropertyPlaceholderConfigurer，我们⼀般在配置数据库的dataSource时使⽤到的占位 符的值，就是它注⼊进去的。 容器中bean的实例化阶段： 实例化阶段主要是通过反射或者CGLIB对bean进⾏实例化，在这个阶段Spring⼜给我们暴露了很多的扩 展点：

各种的Aware接⼝ ，⽐如 BeanFactoryAware，对于实现了这些Aware接⼝的bean，在实例化bean时 Spring会帮我们注⼊对应的BeanFactory的实例。

BeanPostProcessor接⼝ ，实现了BeanPostProcessor接⼝的bean，在实例化bean时Spring会帮我们调⽤ 接⼝中的⽅法。

InitializingBean接⼝ ，实现了InitializingBean接⼝的bean，在实例化bean时Spring会帮我们调⽤接⼝ 中的⽅法。

DisposableBean接⼝ ，实现了BeanPostProcessor接⼝的bean，在该bean死亡时Spring会帮我们调⽤接 ⼝中的⽅法。

## 设计意义：

松耦合。 可以将原来硬编码的依赖，通过Spring这个beanFactory这个⼯⼚来注⼊依赖，也就是说原来只 有依赖⽅和被依赖⽅，现在我们引⼊了第三⽅——spring这个beanFactory，由它来解决bean之间的依赖 问题，达到了松耦合的效果. bean的额外处理。 通过Spring接⼝的暴露，在实例化bean的阶段我们可以进⾏⼀些额外的处理，这些额 外的处理只需要让bean实现对应的接⼝即可，那么spring就会在bean的⽣命周期调⽤我们实现的接⼝来 处理该bean。[⾮常重要]

# 2.⼯⼚⽅法 实现⽅式：

FactoryBean接⼝。

实现原理：

实现了FactoryBean接⼝的bean是⼀类叫做factory的bean。其特点是，spring会在使⽤getBean()调⽤获得 该bean时，会⾃动调⽤该bean的getObject()⽅法，所以返回的不是factory这个bean，⽽是这个 bean.getOjbect()⽅法的返回值。

例⼦：

典型的例⼦有spring与mybatis的结合。 代码示例：

![image 2](<Spring 中经典的 9 种设计模式，打死也要记住啊！.note_images/imageFile2.png>)

说明： 我们看上⾯该bean，因为实现了FactoryBean接⼝，所以返回的不是 SqlSessionFactoryBean 的实例，⽽ 是它的 SqlSessionFactoryBean.getObject() 的返回值。

# 3.单例模式

Spring依赖注⼊Bean实例默认是单例的。 Spring的依赖注⼊（包括lazy-init⽅式）都是发⽣在AbstractBeanFactory的getBean⾥。getBean的 doGetBean⽅法调⽤getSingleton进⾏bean的创建。 分析getSingleton()⽅法

public Object getSingleton(String beanName){ //参数true设置标识允许早期依赖 return getSingleton(beanName,true);

} protected Object getSingleton(String beanName, boolean allowEarlyReference) {

//检查缓存中是否存在实例 Object singletonObject = this.singletonObjects.get(beanName); if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {

//如果为空，则锁定全局变量并进⾏处理。 synchronized (this.singletonObjects) {

//如果此bean正在加载，则不处理 singletonObject = this.earlySingletonObjects.get(beanName); if (singletonObject == null && allowEarlyReference) {

//当某些⽅法需要提前初始化的时候则会调⽤addSingleFactory ⽅法将对应的ObjectFactory初始化策略存

储在singletonFactories ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName); if (singletonFactory != null) {

//调⽤预先设定的getObject⽅法

singletonObject = singletonFactory.getObject(); //记录在缓存中，earlysingletonObjects和singletonFactories互斥 this.earlySingletonObjects.put(beanName, singletonObject); this.singletonFactories.remove(beanName);

} }

}

} return (singletonObject != NULL_OBJECT ? singletonObject : null);

}

getSingleton()过程图 ps：spring依赖注⼊时，使⽤了 双重判断加锁 的单例模式

![image 3](<Spring 中经典的 9 种设计模式，打死也要记住啊！.note_images/imageFile3.png>)

总结 单例模式定义： 保证⼀个类仅有⼀个实例，并提供⼀个访问它的全局访问点。 spring对单例的实现： spring中的单例模式完成了后半句话，即提供了全局的访问点BeanFactory。但没 有从构造器级别去控制单例，这是因为spring管理的是任意的java对象。

# 4.适配器模式 实现⽅式：

SpringMVC中的适配器HandlerAdatper。

实现原理：

HandlerAdatper根据Handler规则执⾏不同的Handler。

实现过程：

DispatcherServlet根据HandlerMapping返回的handler，向HandlerAdatper发起请求，处理Handler。 HandlerAdapter根据规则找到对应的Handler并让其执⾏，执⾏完毕后Handler会向HandlerAdapter返回⼀ 个ModelAndView，最后由HandlerAdapter向DispatchServelet返回⼀个ModelAndView。

实现意义：

HandlerAdatper使得Handler的扩展变得容易，只需要增加⼀个新的Handler和⼀个对应的HandlerAdapter 即可。 因此Spring定义了⼀个适配接⼝，使得每⼀种Controller有⼀种对应的适配器实现类，让适配器代替 controller执⾏相应的⽅法。这样在扩展Controller时，只需要增加⼀个适配器类就完成了SpringMVC的 扩展了。

# 5.装饰器模式 实现⽅式：

Spring中⽤到的包装器模式在类名上有两种表现：⼀种是类名中含有Wrapper，另⼀种是类名中含有 Decorator。

实质：

动态地给⼀个对象添加⼀些额外的职责。 就增加功能来说，Decorator模式相⽐⽣成⼦类更为灵活。

# 6.代理模式 实现⽅式：

AOP底层，就是动态代理模式的实现。

动态代理：

在内存中构建的，不需要⼿动编写代理类

静态代理：

需要⼿⼯编写代理类，代理类引⽤被代理对象。

实现原理：

切⾯在应⽤运⾏的时刻被织⼊。⼀般情况下，在织⼊切⾯时，AOP容器会为⽬标对象创建动态的创建 ⼀个代理对象。SpringAOP就是以这种⽅式织⼊切⾯的。 织⼊：把切⾯应⽤到⽬标对象并创建新的代理对象的过程。

# 7.观察者模式 实现⽅式：

spring的事件驱动模型使⽤的是 观察者模式 ，Spring中Observer模式常⽤的地⽅是listener的实现。

具体实现：

事件机制的实现需要三个部分,事件源,事件,事件监听器 ApplicationEvent抽象类[事件] 继承⾃jdk的EventObject,所有的事件都需要继承ApplicationEvent,并且通过构造器参数source得到事件源. 该类的实现类ApplicationContextEvent表示ApplicaitonContext的容器事件. 代码：

public abstract class ApplicationEvent extends EventObject { private static final long serialVersionUID = 7099057708183571937L; private final long timestamp; public ApplicationEvent(Object source) { super(source); this.timestamp = System.currentTimeMillis(); } public final long getTimestamp() {

return this.timestamp; }

}

ApplicationListener接⼝[事件监听器] 继承⾃jdk的EventListener,所有的监听器都要实现这个接⼝。 这个接⼝只有⼀个onApplicationEvent()⽅法,该⽅法接受⼀个ApplicationEvent或其⼦类对象作为参数,在 ⽅法体中,可以通过不同对Event类的判断来进⾏相应的处理。 当事件触发时所有的监听器都会收到消息。 代码：

public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

void onApplicationEvent(E event); }

ApplicationContext接⼝[事件源] ApplicationContext是spring中的全局容器，翻译过来是”应⽤上下⽂”。 实现了ApplicationEventPublisher接⼝。

## 职责：

负责读取bean的配置⽂档,管理bean的加载,维护bean之间的依赖关系,可以说是负责bean的整个⽣命周期, 再通俗⼀点就是我们平时所说的IOC容器。 代码：

public interface ApplicationEventPublisher {

void publishEvent(ApplicationEvent event); }

public void publishEvent(ApplicationEvent event) { Assert.notNull(event, "Event must not be null"); if (logger.isTraceEnabled()) {

logger.trace("Publishing event in " + getDisplayName() + ": " + event);

} getApplicationEventMulticaster().multicastEvent(event);

if (this.parent != null) { this.parent.publishEvent(event); }

}

ApplicationEventMulticaster抽象类[事件源中publishEvent⽅法需要调⽤其⽅法 getApplicationEventMulticaster]

属于事件⼴播器,它的作⽤是把Applicationcontext发布的Event⼴播给所有的监听器. 代码：

public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext, DisposableBean { private ApplicationEventMulticaster applicationEventMulticaster; protected void registerListeners() { // Register statically specified listeners first. for (ApplicationListener<?> listener : getApplicationListeners()) { getApplicationEventMulticaster().addApplicationListener(listener); } // Do not initialize FactoryBeans here: We need to leave all regular beans // uninitialized to let post-processors apply to them! String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false); for (String lisName : listenerBeanNames) { getApplicationEventMulticaster().addApplicationListenerBean(lisName); }

} }

# 8.策略模式 实现⽅式：

Spring框架的资源访问Resource接⼝。该接⼝提供了更强的资源访问能⼒，Spring 框架本身⼤量使⽤了 Resource 接⼝来访问底层资源。

## Resource 接⼝介绍

source 接⼝是具体资源访问策略的抽象，也是所有资源访问类所实现的接⼝。 Resource 接⼝主要提供了如下⼏个⽅法:

getInputStream()： 定位并打开资源，返回资源对应的输⼊流。每次调⽤都返回新的输⼊流。调⽤ 者必须负责关闭输⼊流。

exists()： 返回 Resource 所指向的资源是否存在。

isOpen()： 返回资源⽂件是否打开，如果资源⽂件不能多次读取，每次读取结束应该显式关闭，以 防⽌资源泄漏。

getDescription()： 返回资源的描述信息，通常⽤于资源处理出错时输出该信息，通常是全限定⽂件 名或实际 URL。

getFile： 返回资源对应的 File 对象。

getURL： 返回资源对应的 URL 对象。

最后两个⽅法通常⽆须使⽤，仅在通过简单⽅式访问⽆法实现时，Resource 提供传统的资源访问的功 能。 Resource 接⼝本身没有提供访问任何底层资源的实现逻辑，针对不同的底层资源，Spring 将会提供不 同的 Resource 实现类，不同的实现类负责不同的资源访问逻辑。 Spring 为 Resource 接⼝提供了如下实现类：

UrlResource： 访问⽹络资源的实现类。

ClassPathResource： 访问类加载路径⾥资源的实现类。

FileSystemResource： 访问⽂件系统⾥资源的实现类。

ServletContextResource： 访问相对于 ServletContext 路径⾥的资源的实现类.

InputStreamResource： 访问输⼊流资源的实现类。 ByteArrayResource： 访问字节数组资源的实现类。

这些 Resource 实现类，针对不同的的底层资源，提供了相应的资源访问逻辑，并提供便捷的包装，以 利于客户端程序的资源访问。

# 9.模版⽅法模式 经典模板⽅法定义：

⽗类定义了⻣架（调⽤哪些⽅法及顺序），某些特定⽅法由⼦类实现。 最⼤的好处：代码复⽤，减少重复代码。除了⼦类要实现的特定⽅法，其他⽅法及⽅法调⽤顺序都在 ⽗类中预先写好了。 所以⽗类模板⽅法中有两类⽅法： 共同的⽅法： 所有⼦类都会⽤到的代码 不同的⽅法： ⼦类要覆盖的⽅法，分为两种：

抽象⽅法：⽗类中的是抽象⽅法，⼦类必须覆盖

钩⼦⽅法：⽗类中是⼀个空⽅法，⼦类继承了默认也是空的

注：为什么叫钩⼦，⼦类可以通过这个钩⼦（⽅法），控制⽗类，因为这个钩⼦实际是⽗类的⽅法 （空⽅法）！

Spring模板⽅法模式实质：

是模板⽅法模式和回调模式的结合，是Template Method不需要继承的另⼀种实现⽅式。Spring⼏乎所有 的外接扩展都采⽤这种模式。

具体实现：

JDBC的抽象和对Hibernate的集成，都采⽤了⼀种理念或者处理⽅式，那就是模板⽅法模式与相应的 Callback接⼝相结合。 采⽤模板⽅法模式是为了以⼀种统⼀⽽集中的⽅式来处理资源的获取和释放，以JdbcTempalte为例:

public abstract class JdbcTemplate { public final Object execute（String sql）{

Connection con=null; Statement stmt=null;

### try{

con=getConnection（）; stmt=con.createStatement（）; Object retValue=executeWithStatement（stmt,sql）; return retValue; }catch（SQLException e）{

...

}finally{ closeStatement（stmt）; releaseConnection（con）;

}

} protected abstract Object executeWithStatement（Statement stmt, String sql）;

}

## 引⼊回调原因：

JdbcTemplate是抽象类，不能够独⽴使⽤，我们每次进⾏数据访问的时候都要给出⼀个相应的⼦类实现, 这样肯定不⽅便，所以就引⼊了回调。 回调代码

public interface StatementCallback{

Object doWithStatement（Statement stmt）; }

利⽤回调⽅法重写JdbcTemplate⽅法

public class JdbcTemplate { public final Object execute（StatementCallback callback）{

Connection con=null; Statement stmt=null; try{

con=getConnection（）; stmt=con.createStatement（）; Object retValue=callback.doWithStatement（stmt）; return retValue; }catch（SQLException e）{

...

}finally{ closeStatement（stmt）; releaseConnection（con）;

} }

...//其它⽅法定义 }

Jdbc使⽤⽅法如下：

JdbcTemplate jdbcTemplate=...; final String sql=...; StatementCallback callback=new StatementCallback(){ public Object=doWithStatement(Statement stmt){

### return ...; }

} jdbcTemplate.execute(callback);

## 为什么JdbcTemplate没有使⽤继承？

因为这个类的⽅法太多，但是我们还是想⽤到JdbcTemplate已有的稳定的、公⽤的数据库连接，那么我 们怎么办呢？ 我们可以把变化的东⻄抽出来作为⼀个参数传⼊JdbcTemplate的⽅法中。但是变化的东⻄是⼀段代码， ⽽且这段代码会⽤到JdbcTemplate中的变量。怎么办？ 那我们就⽤回调对象吧。在这个回调对象中定义⼀个操纵JdbcTemplate中变量的⽅法，我们去实现这个 ⽅法，就把变化的东⻄集中到这⾥了。然后我们再传⼊这个回调对象到JdbcTemplate，从⽽完成了调 ⽤。

