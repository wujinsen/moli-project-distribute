# 前⾔

关于@Autowired这个注解，我们再熟悉不过了，经常跟@Resource来做对⽐，这篇⽂章我们不讨论 两者有何异同，仅分析@Autowired的原理（基于Spring5）。

# 问题

假如⼀个接⼝(IUserService)有两个实现类，分别是(UserServiceImpl01)和(UserServiceImpl02)，在 我们给类注⼊的时候，这样写(@Autowired private IUserService userService)会发⽣什么情况？答案 肯定是报错，那么原理呢？⽂字描述：因为⾸先@Autowired是按照类型注⼊的，也就是.clas，但 UserServiceImpl01和UserServiceImpl02都是IUserService类型的，于是Spring就会按照后⾯的名字 (userService)在容器中查找，但发现根本没有这个名字，因为两个实现类在不指定名字情况下，就是 ⾸字⺟⼩写的类名，然后抛出异常：expected single matching bean but found 2。。。

# 如何解决这类问题

- 1.
- 2.


如果有两个实现类，还要使⽤@Autowired注解，可以将userService改成我们指定的实现类名称， ⽐如UserServiceImpl01，或者不想改userService，可以加@Qualifier(value = "userServiceImpl01")，指定需要注⼊的实现类。 使⽤@Resource注解，⼿动指定实现类名称。

还有很多种⽅法，但基本思想都⼀样，⽆⾮就是如何区分两个同祖宗的⼉⼦，既然根⼉相同，那就只 有指定名字了。

# @Autowired原理

提到@Autowired我们⼀般都知道叫依赖注⼊

- 1.
- 2.
- 3.


什么是依赖注⼊？ 什么是注⼊，注到哪⾥？ 什么时候注⼊的？

什么是依赖注⼊？

依赖注⼊：Dependency Injection，简称DI，说⽩了就是利⽤反射机制为类的属性赋值的操作。

什么是注⼊，注⼊到哪⾥？

注⼊就是为某个对象的外部资源赋值，注⼊某个对象所需要的外部资源（包括对象、资源、常量数据 等）。IOC容器注⼊应⽤程序某个对象，应⽤程序所依赖的对象。

什么时候注⼊的？

在完成对象的创建，为对象变量进⾏赋值的时候进⾏注⼊（populate）。

源码分析

1.

⾸先点开@Autowired，注释上写Please consult the javadoc for the AutowiredAnotationBeanPostProcesor，让我们去查阅这个类，看⼀下这个类的继承关系树， 如下：

可⻅它间接实现InstantiationAwareBeanPostProcesor，就具备了实例化前后(⽽不是初始化前后) 管理对象的能⼒，实现了BeanPostProcesor，具有初始化前后管理对象的能⼒，实现 BeanFactoryAware，具备随时拿到BeanFactory的能⼒，也就是说，这个 AutowiredAnotationBeanPostProcesor具备⼀切后置处理器的能⼒。

- 1.
- 2.


容器在初始化的时候，后置处理器的初始化要优先于剩下⾃定义Bean(⽐如我们⾃定义的 Service，Controler等等)的初始化的，我们⾃定义的Bean初始化是在 finishBeanFactoryInitialization(beanFactory)这⾥完成的，来到AbstractAplicationContext的 refresh()⽅法。 finishBeanFactoryInitialization(beanFactory)->beanFactory.preInstantiateSingletons()>getBean(beanName)->doGetBean(beanName)->来到AbstractBeanFactory第317⾏ createBean(beanName, mbd, args)，来创建bean实例 ->来到 AbstractAutowireCapableBeanFactory第503⾏doCreateBean(beanName, mbdToUse, args)-> 紧接着来到AbstractAutowireCapableBeanFactory的第543⾏，instanceWraper = createBeanInstance(beanName, mbd, args)就已经把Bean实例创建出来了，只不过 instanceWraper是⼀个被包装过了的bean，它⾥⾯的属性还未赋实际值 ->然后来到第 5⾏ aplyMergedBeanDefinitionPostProcesors(mbd, beanType, beanName)，这⼀步的作⽤就是将 所有的后置处理器拿出来，并且把名字叫beanName的类中的变量都封装到InjectionMetadata的 injectedElements集合⾥⾯，⽬的是以后从中获取，挨个创建实例，通过反射注⼊到相应类中。 紧接着来到AbstractAutowireCapableBeanFactory第58⾏populateBean(beanName, mbd, instanceWraper)->点进去，来到AbstractAutowireCapableBeanFactory的第1347⾏，来循环 遍历所有的后置处理器for (BeanPostProcesor bp : getBeanPostProcesors()，从⽅法名字 postProcesPropertyValues也能看出来，就是给属性赋值，当bp是 AutowiredAnotationBeanPostProcesor的时候，进⼊postProcesPropertyValues⽅法，来到 AutowiredAnotationBeanPostProcesor的postProcesPropertyValues⽅法，如下：

1.

⾸先找到需要注⼊的哪些元数据，然后metadata.inject（注⼊），注⼊⽅法点进去，来到 InjectionMetadata的inject⽅法，在⼀个for循环⾥⾯依次执⾏element.inject(target, beanName, pvs)，来对属性进⾏注⼊。

1.

进⼊element.inject(target, beanName, pvs)，注意，这⾥必须要debug才可以进⼊真正的⽅法。 来到AutowiredAnotationBeanPostProcesor的inject⽅法，

第584⾏，value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter)，由⼯⼚解析这个依赖，进⼊，来到DefaultListableBeanFactory第1065⾏，result = doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter)再 次解析依赖，点击进⼊，来到DefaultListableBeanFactory的doResolveDependency()⽅法，前⾯是⼀ 堆判断，⽐较，查看属性类型，这种类型的有⼏个(matchingBeans)，如果只有⼀个匹配，那么来到第 138⾏，instanceCandidate = descriptor.resolveCandidate(autowiredBeanName, type, this)，进⼊ 这个⽅法，可以看到就是前⾯说的根据⼯⼚来创建实例的过程了： beanFactory.getBean(beanName)，其中这个beanName就是属性的名称，当经过⼀系列操作完成属 性的实例化后，便来到AutowiredAnotationBeanPostProcesor的第61⾏，利⽤反射为此对象赋值。 这样，对象的创建以及赋值就完成了。

# 总结

在容器启动，为对象赋值的时候，遇到@Autowired注解，会⽤后置处理器机制，来创建属性的实 例，然后再利⽤反射机制，将实例化好的属性，赋值给对象上，这就是Autowired的原理。

