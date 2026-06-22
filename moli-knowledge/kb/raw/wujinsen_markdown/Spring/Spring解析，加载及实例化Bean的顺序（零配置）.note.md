htps:/mp.weixin.q.com/s/_uIqQxZEjowZEMnv5tdHog

在使⽤Spring时，Bean之间会有些依赖，⽐如⼀个Bean A实例化时需要⽤到Bean B,那么B应该在A之前 实例化好。很多时候Spring智能地为我们做好了这些⼯作，但某些情况下可能不是，⽐如Springboot的 @AutoConfigureAfter注解，⼿动的指定Bean的实例化顺序。 了解Spring内Bean的解析，加载和实例化顺序机制有助于我们更好的使⽤Spring/Springboot，避免⼿动 的去⼲预Bean的加载过程，搭建更优雅的框架。 Spring容器在实例化时会加载容器内所有⾮延迟加载的单例类型Bean，看如下源码：

public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext, DisposableBean {

//刷 新 Spring容 器 ，相 当 于 初 始 化 public void refresh() throws BeansException, IllegalStateException {

...... // Instantiate all remaining (non-lazy-init) singletons. finishBeanFactoryInitialization(beanFactory);

} }

public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {

/** List of bean definition names, in registration order */ private volatile List<String> beanDefinitionNames = new ArrayList<String>(256);

public void preInstantiateSingletons() throws BeansException { List<String> beanNames = new ArrayList<String>(this.beanDefinitionNames); for (String beanName : beanNames) {

...... getBean(beanName); //实 例 化 Bean

} }

}

ApplicationContext内置⼀个BeanFactory对象，作为实际的Bean⼯⼚，和Bean相关业务都交给 BeanFactory去处理。 在BeanFactory实例化所有⾮延迟加载的单例Bean时，遍历beanDefinitionNames 集合，按顺序实例化指 定名称的Bean。beanDefinitionNames 属性是Spring在加载Bean Class⽣成的BeanDefinition时，为这些 Bean预先定义好的名称，看如下代码：

public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {

public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {

...... this.beanDefinitionNames.add(beanName);

} }

BeanFactory在加载⼀个BeanDefinition（也就是加载Bean Class）时，将相应的beanName存⼊ beanDefinitionNames属性中，在加载完所有的BeanDefinition后，执⾏Bean实例化⼯作，此时会依据 beanDefinitionNames的顺序来有序实例化Bean，也就是说Spring容器内Bean的加载和实例化是有顺序 的，⽽且近似⼀致，当然仅是近似。 Spring在初始化容器时，会先解析和加载所有的Bean Class，如果符合要求则通过Class⽣成 BeanDefinition，存⼊BeanFactory中，在加载完所有Bean Class后，开始有序的通过BeanDefinition实例 化Bean。 我们先看加载Bean Class过程，零配置下Spring Bean的加载起始于ConfigurationClassPostProcessor的 postProcessBeanDefinitionRegistry（BeanDefinitionRegistry）⽅法，我总结了下其加载解析Bean Class的 流程：

![image 1](<Spring解析，加载及实例化Bean的顺序（零配置）.note_images/imageFile1.png>)

配置类可以是Spring容器的起始配置类，也可以是通过@ComponentScan扫描得到的类，也可以是通过 @Import引⼊的类。如果这个类上含有@Configuration，@Component，@ComponentScan，@Import， @ImportResource注解中的⼀个，或者内部含有@Bean标识的⽅法，那么这个类就是⼀个配置类， Spring就会按照⼀定流程去解析这个类上的信息。 在解析的第⼀步会校验当前类是否已经被解析过了，如果是，那么需要按照⼀定的规则处理 （@ComponentScan得到的Bean能覆盖@Import得到的Bean，@Bean定义的优先级最⾼）。 如果未解析过，那么开始解析：

- 1. 解析内部类，查看内部类是否应该被定义成⼀个Bean，如果是，递归解析。


- 2.
- 3.
- 4.
- 5.
- 6.


解析@PropertySource，也就是解析被引⼊的Properties⽂件。 解析配置类上是否有@ComponentScan注解，如果有则执⾏扫描动作，通过扫描得到的Bean Class会被⽴ 即解析成BeanDefinition，添加进beanDefinitionNames属性中。之后查看扫描到的Bean Class是否是⼀个配 置类（⼤部分情况是，因为标识@Component注解），如果是则递归解析这个Bean Class。 解析@Import引⼊的类，如果这个类是⼀个配置类，则递归解析。 解析@Bean标识的⽅法，此种形式定义的Bean Class不会被递归解析 解析⽗类上的@ComponentScan，@Import，@Bean，⽗类不会被再次实例化，因为其⼦类能够做⽗类的 ⼯作，不需要额外的Bean了。

在1，3，4，6中都有递归操作，也就是在解析⼀个Bean Class A时，发现其上能够获取到其他Bean Class B信息，此时会递归的解析Bean Class B，在解析完Bean Class B后再接着解析Bean Class A，可能 在解析B时能够获取到C，那么也会先解析C再解析B，就这样不断的递归解析。 在第3步中，通过@ComponentScan扫描直接得到的Bean Class会被⽴即加载⼊beanDefinitionNames中， 但@Import和@Bean形式定义的Bean Class则不会，也就是说正常情况下⾯@ComponentScan直接得到的 Bean其实例化时机⽐其他两种形式的要早。 通过@Bean和@Import形式定义的Bean Class不会⽴即加载，他们会被放⼊⼀个ConfigurationClass类中， 然后按照解析的顺序有序排列，就是图⽚上的 “将配置类有序排列”。⼀个ConfigurationClass代表⼀个 配置类，这个类可能是被@ComponentScan扫描到的，则此类已经被加载过了；也可能是被@Import引 ⼊的，则此类还未被加载；此类中可能含有@Bean标识的⽅法。 Spring在解析完了所有Bean Class后，开始加载ConfigurationClass。如果这个ConfigurationClass是被 Import的，也就是说在加载@ComponentScan时其未被加载，那么此时加载ConfigurationClass代表的 Bean Class。然后加载ConfigurationClass内的@Bean⽅法。 顺序总结：@ComponentScan > @Import > @Bean 下⾯看实际的启动流程：

![image 2](<Spring解析，加载及实例化Bean的顺序（零配置）.note_images/imageFile2.png>)

Bean Class的结构图如上所示，A是配置类的⼊⼝，通过A能直接或间接的引⼊⼀个模块。 此时启动Spring容器，将A引⼊容器内。 如果A是通过@ComponentScan扫描到的，那么此时的加载顺序是： A > D > F > B > E > G > C 如果A是通过@Import形式引⼊的，那么此时的加载顺讯是： D > F > B > E > G > A > C 当然以上仅仅代表着加载Bean Class的顺序，实际实例化Bean的顺序和加载顺序⼤体相同，但还是会有 ⼀些差别。 Spring在通过getBean(beanName)形式实例化Bean时，会通过BeanDefinition去⽣成Bean对象。在这个过 程中，如果BeanDefinition的DependsOn不为空，从字⾯理解就是依赖某个什么，其值⼀般是某个或多 个beanName，也就是说依赖于其他Bean，此时Spring会将DependsOn指定的这些名称的Bean先实例化， 也就是先调⽤getBean(dependsOn)⽅法。我们可以通过在Bean Class或者@Bean的⽅法上标识

**@DependsOn**注解，来指定当前Bean实例化时需要触发哪些Bean的提前实例化。 当⼀个Bean A内部通过@Autowired或者@Resource注⼊Bean B，那么在实例化A时会触发B的提前实例 化，此时会注册A>B的dependsOn依赖关系，实质和@DependsOn⼀样，这个是Spring⾃动为我们处理好 的。 了解Spring Bean的解析，加载及实例化的顺序机制能够加深对Spring的理解，搭建更优雅简介的Spring 框架。

