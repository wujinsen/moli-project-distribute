htps:/mp.weixin.q.com/s/Q3VgfiG4pw6pETI94kfDA

@Autowired注解的实现过程，其实就是Spring Bean的⾃动装配过程。通过看@Autowired源码注释部分 我们可以看到@Autowired的实现是通过AutowiredAnnotationBeanPostProcessor后置处理器中实现的。

## AutowiredAnnotationBeanPostProcessor 类图

![image 1](<@Autowired注解实现原理（Spring Bean的自动装配）.note_images/imageFile1.png>)

img

PriorityOrdered：确认 AutowiredAnnotationBeanPostProcessor 后置处理器的执⾏优先级

BeanFactoryAware：使得AutowiredAnnotationBeanPostProcessor可以直接通过BeanFactory获取容 器中的Bean

BeanPostProcessor：在 Bean 初始化前后执⾏的后置处理器

InstantiationAwareBeanPostProcessor：在 Bean 实例化前后和Bean设置属性值时执⾏的后置处理器

SmartInstantiationAwareBeanPostProcessor：智能实例化Bean的后处理器，如预测Bean的类型和确认 Bean的构造函数等。

MergedBeanDefinitionPostProcessor：合并Bean的定义信息。

在分析⾃动装配前我们先来介绍⼀下上⾯的这些后置处理器。

### 后置处理器介绍

#### BeanPostProcessor

BeanPostProcessor有两个⽅法，postProcessBeforeInitialization和 postProcessAfterInitialization。它们分别在任何bean初始化回调之前或之后执⾏（例如 InitializingBean的afterPropertiesSet⽅法或⾃定义init-method⽅法之前或者之后）， 在这个时候该bean的 属性值已经填充完成了，并且我们返回的bean实例可能已经是原始实例的包装类型了。例如返回⼀个 FactoryBean。

#### InstantiationAwareBeanPostProcessor

InstantiationAwareBeanPostProcessor继承⾃BeanPostProcessor接⼝。主要多提供了以下三个⽅法。

##### postProcessBeforeInstantiation

该⽅法是在Bean实例化⽬标对象之前调⽤，返回的Bean对象可以代理⽬标，从⽽有效的阻⽌了⽬标 Bean的默认实例化。

protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) { Object bean = null; if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) { // Make sure bean class is actually resolved at this point. if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {

Class<?> targetType = determineTargetType(beanName, mbd); if (targetType != null) {

bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName); if (bean != null) {

// 如 果 此 ⽅ 法 返 回 ⼀ 个 ⾮ null对 象 ， 则 Bean创 建 过 程 将 被 短 路 。 // 唯 ⼀ 应 ⽤ 的 进 ⼀ 步 处 理 是来 ⾃ 已 配 置 BeanPostProcessors的 postProcessAfterInitialization回 调 bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);

} }

} mbd.beforeInstantiationResolved = (bean != null);

} return bean;

}

protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) { for (BeanPostProcessor bp : getBeanPostProcessors()) {

if (bp instanceof InstantiationAwareBeanPostProcessor) { InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp; // 执 ⾏ Bean实 例 化 ⽬ 标 对 象 之 前 的 后 置 处 理 ⽅ 法 Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName); if (result != null) {

return result; }

}

} return null;

}

跟进源码我们可以看出，如果此⽅法返回⼀个⾮null对象，则Bean创建过程将被短路。唯⼀应⽤的进⼀ 步处理是来⾃已配置BeanPostProcessors的postProcessAfterInitialization回调。

##### postProcessAfterInstantiation

该⽅法执⾏在通过构造函数或⼯⼚⽅法在实例化bean之后但在发⽣Spring属性填充（通过显式属性或⾃ 动装配）之前执⾏操作。这是在Spring的⾃动装配开始之前对给定的bean实例执⾏⾃定义字段注⼊的理 想回调。如果该⽅法返回false，那么它会阻断后续InstantiationAwareBeanPostProcessor后置处理器 的执⾏，并且会阻⽌后续属性填充的执⾏逻辑。

##### postProcessPropertyValues

在⼯⼚将给定属性值应⽤于给定bean之前，对它们进⾏后处理。允许检查是否满⾜所有依赖关系，例 如基于bean属性设置器上的“ Required”注解。还允许替换要应⽤的属性值，通常是通过基于原始 PropertyValues创建新的MutablePropertyValues实例，添加或删除特定值来实现。

#### SmartInstantiationAwareBeanPostProcessor

智能实例化Bean的后处理器，主要提供了三个⽅法。

##### predictBeanType

预测从此处理器的postProcessBeforeInstantiation回调最终返回的bean的类型。

##### determineCandidateConstructors

确定使⽤实例化Bean的构造函数。

##### getEarlyBeanReference

获取提早暴露的Bean的引⽤，提早暴露的Bean就是只完成了实例化，还未完成属性赋值和初始化的 Bean。

#### MergedBeanDefinitionPostProcessor

##### postProcessMergedBeanDefinition

合并Bean的定义信息的后处理⽅法，该⽅法是在Bean的实例化之后设置值之前调⽤。

### ⾃动装配的实现

找到需要⾃动装配的元素

AutowiredAnnotationBeanPostProcessor后置处理器主要负责对添加了@Autowired和@Value注解的元 素实现⾃动装配。所以找到需要⾃动装配的元素，其实就是对@Autowired和@Value注解的解析。 AutowiredAnnotationBeanPostProcessor后置处理器，找出需要⾃动装配的元素是在 MergedBeanDefinitionPostProcessor.postProcessMergedBeanDefinition这个⽅法中实现的，调⽤链路 如下：

doWith:445, AutowiredAnnotationBeanPostProcessor$2 (org.springframework.beans.factory.annotation) doWithLocalFields:657, ReflectionUtils (org.springframework.util) buildAutowiringMetadata:433, AutowiredAnnotationBeanPostProcessor (org.springframework.beans.factor y.annotation) findAutowiringMetadata:412, AutowiredAnnotationBeanPostProcessor (org.springframework.beans.factory

.annotation) postProcessMergedBeanDefinition:235, AutowiredAnnotationBeanPostProcessor (org.springframework.bean s.factory.annotation) applyMergedBeanDefinitionPostProcessors:1000, AbstractAutowireCapableBeanFactory (org.springframewo rk.beans.factory.support) doCreateBean:523, AbstractAutowireCapableBeanFactory (org.springframework.beans.factory.support) createBean:483, AbstractAutowireCapableBeanFactory (org.springframework.beans.factory.support) getObject:312, AbstractBeanFactory$1 (org.springframework.beans.factory.support) getSingleton:230, DefaultSingletonBeanRegistry (org.springframework.beans.factory.support) doGetBean:308, AbstractBeanFactory (org.springframework.beans.factory.support) getBean:197, AbstractBeanFactory (org.springframework.beans.factory.support)

preInstantiateSingletons:761, DefaultListableBeanFactory (org.springframework.beans.factory.support ) finishBeanFactoryInitialization:867, AbstractApplicationContext (org.springframework.context.suppor t) refresh:543, AbstractApplicationContext (org.springframework.context.support) <init>:84, AnnotationConfigApplicationContext (org.springframework.context.annotation)

从链路上我们可以看到，找到需要⾃动装配的元素是在findAutowiringMetadata⽅法中实现的，该⽅法 会去调⽤buildAutowiringMetadata⽅法构建元数据信息。如果注解被加载属性上将会被封装成

AutowiredFieldElement对象；如果注解加在⽅法上，那么元素会被封装成AutowiredMethodElement对 象。这⾥两个对象的inject⽅法将最后完成属性值的注⼊，主要区别就是使⽤反射注⼊值的⽅式不⼀ 样。源码如下：

private InjectionMetadata buildAutowiringMetadata(final Class<?> clazz) {

LinkedList<InjectionMetadata.InjectedElement> elements = new LinkedList<InjectionMetadata.Injected Element>();

Class<?> targetClass = clazz;

do { // 存 放 我 们 找 到 的 元 数据 信 息 final LinkedList<InjectionMetadata.InjectedElement> currElements =

new LinkedList<InjectionMetadata.InjectedElement>();

// 通过 反 射 找 出 对 应 Class对 象 的 所 有 Field ReflectionUtils.doWithLocalFields(targetClass, new ReflectionUtils.FieldCallback() {

@Override public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {

// 通过 反 射 找 到 该 字 段 上 所 有 的 注 解 信 息 ， 并 判 断 是 否 有 @Autowired和 @Value注 解 ， 如 果有 就将 该 字 段 封 成

AutowiredFieldElement对 象 AnnotationAttributes ann = findAutowiredAnnotation(field); if (ann != null) {

if (Modifier.isStatic(field.getModifiers())) { if (logger.isWarnEnabled()) { logger.warn("Autowired annotation is not supported on static fields: " + field);

} return;

} boolean required = determineRequiredStatus(ann);、 // 将 该 字 段 封 成 AutowiredFieldElement对 象 ， 并 放 到 缓 存 中 currElements.add(new AutowiredFieldElement(field, required));

} } });

// 通过 反 射 找 出 对 应 Class对 象 的 所 有 Method ReflectionUtils.doWithLocalMethods(targetClass, new ReflectionUtils.MethodCallback() {

@Override public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {

Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);

if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) { return;

} // 通过 反 射 找 到 该 字 段 上 所 有 的 注 解 信 息 ， 并 判 断 是 否 有 @Autowired和 @Value注 解 ， 如 果有 就将 该 字 段 封 成

AutowiredMethodElement对 象 AnnotationAttributes ann = findAutowiredAnnotation(bridgedMethod); if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {

if (Modifier.isStatic(method.getModifiers())) { if (logger.isWarnEnabled()) { logger.warn("Autowired annotation is not supported on static methods: " + method);

} return;

} if (method.getParameterTypes().length == 0) {

if (logger.isWarnEnabled()) { logger.warn("Autowired annotation should only be used on methods with parameters: " +

method); }

} boolean required = determineRequiredStatus(ann); PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz); // 将 该 字 段 封 成 AutowiredMethodElement对 象 currElements.add(new AutowiredMethodElement(method, required, pd));

} } });

elements.addAll(0, currElements); targetClass = targetClass.getSuperclass();

} // 循 环 处 理 ⽗ 类 需 要 ⾃ 动 装 配 的 元 素 while (targetClass != null && targetClass != Object.class); // 将 需 要 ⾃ 动 装 配 的 元 素 封 装 成 InjectionMetadata对 象 ,最 后合 并 到 Bean定 义中 return new InjectionMetadata(clazz, elements);

}

寻找需要⾃动装配过程：

- 1.
- 2.
- 3.
- 4.
- 5.


根据Class对象，通过反射获取所有的Field和```Method````对象 通过反射获取Field和Method上的注解，并判断是否有@Autowired和@Value注解 将注解了@Autowired和@Value的Field和Method封装成AutowiredFieldElement和 AutowiredMethodElement对象，等待下⼀步的⾃动装配。 循环处理⽗类需要⾃动装配的元素 将需要⾃动装配的元素封装成InjectionMetadata对象，最后合并到Bean定义的 externallyManagedConfigMembers属性中

注⼊属性值

AutowiredAnnotationBeanPostProcessor后置处理器注⼊属性值是在postProcessPropertyValues⽅法中 实现的。源码如下：

public void inject(Object target, String beanName, PropertyValues pvs) throws Throwable { // 获 取 需 要 ⾃ 动 装 配 的 元 数据 信 息 （ 这 ⾥ 实 在 缓 存 中 取 ） Collection<InjectedElement> elementsToIterate =

(this.checkedElements != null ? this.checkedElements : this.injectedElements);

if (!elementsToIterate.isEmpty()) { boolean debug = logger.isDebugEnabled(); for (InjectedElement element : elementsToIterate) {

if (debug) { logger.debug("Processing injected element of bean '" + beanName + "': " + element);

} // 调 ⽤ AutowiredFieldElement或 AutowiredMethodElement对 象 的 inject⽅ 法注 ⼊ 属 性 值 element.inject(target, beanName, pvs);

} }

}

##### AutowiredFieldElement#inject

@Override protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

Field field = (Field) this.member; Object value; if (this.cached) {

value = resolvedCachedArgument(beanName, this.cachedFieldValue);

} else {

DependencyDescriptor desc = new DependencyDescriptor(field, this.required); desc.setContainingClass(bean.getClass()); Set<String> autowiredBeanNames = new LinkedHashSet<String>(1); TypeConverter typeConverter = beanFactory.getTypeConverter(); try {

// 在 容 器 中 获 取 需 要装 配 的 Bean value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);

}

...

} if (value != null) { // 通过 反 射 设 置 属 性 值 ReflectionUtils.makeAccessible(field); field.set(bean, value);

} }

##### AutowiredMethodElement#inject

@Override protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

if (checkPropertySkipping(pvs)) { return;

} Method method = (Method) this.member; Object[] arguments; if (this.cached) {

// Shortcut for avoiding synchronization... arguments = resolveCachedArguments(beanName);

} else {

Class<?>[] paramTypes = method.getParameterTypes(); arguments = new Object[paramTypes.length]; DependencyDescriptor[] descriptors = new DependencyDescriptor[paramTypes.length]; Set<String> autowiredBeans = new LinkedHashSet<String>(paramTypes.length); TypeConverter typeConverter = beanFactory.getTypeConverter(); for (int i = 0; i < arguments.length; i++) {

MethodParameter methodParam = new MethodParameter(method, i); DependencyDescriptor currDesc = new DependencyDescriptor(methodParam, this.required); currDesc.setContainingClass(bean.getClass()); descriptors[i] = currDesc; try {

// 在 容 器 中 获 取 需 要装 配 的 Bean Object arg = beanFactory.resolveDependency(currDesc, beanName, autowiredBeans, typeConverter); if (arg == null && !this.required) {

arguments = null; break;

} arguments[i] = arg;

} catch (BeansException ex) {

throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(methodParam), ex); }

}

...

} if (arguments != null) {

try { // 通过 反 射 调 ⽤ ⽅ 法 设 置 元 素 值 ReflectionUtils.makeAccessible(method); method.invoke(bean, arguments);

}

... }

}

从这⾥的源码我们可以看出AutowiredFieldElement和AutowiredMethodElement完成⾃动装配都是先去 容器中找对应的Bean，然后通过反射将获取到的Bean设置到⽬标对象中，来完成Bean的⾃动装配。

### 总结

我们可以看出Spring Bean的⾃动装配过程就是：

1. 根据Class对象，通过反射获取所有的Field和Method信息

- 2.
- 3.
- 4.
- 5.
- 6.


通反射获取Field和Method的注解信息，并根据注解类型，判断是否需要⾃动装配 将需要⾃动装配的元素，封装成AutowiredFieldElement或AutowiredMethodElement对象 调⽤AutowiredFieldElement或AutowiredMethodElement的inject⽅法，唤起后续步骤 通过调⽤容器的getBean()⽅法找到需要注⼊的源数据Bean 通过反射将找到的源数据Bean注⼊到⽬标Bean中

在⾃动装配过程中还涉及循环依赖的问题，有兴趣的可以看下这篇⽂章Spring 源码（⼋）循环依赖

# “

注意：注解注⼊将在XML注⼊之前执⾏；因此，对于通过这两种⽅法注⼊的属性，XML注⼊将覆盖注 解注⼊。

2021Java深⼊资料领取⽅式回复“2021012”

墙裂推荐 【深度】互联⽹技术⼈的社群，点击了解！

![image 2](<@Autowired注解实现原理（Spring Bean的自动装配）.note_images/imageFile2.png>)

- ●Java 应⽤线上问题排查思路、⼯具⼩结

- ●⽤字节码来看 try-catch-finaly 和 return具体怎么执⾏？
- ●⼿把⼿教你基于Retrofit实现⾃⼰的轻量级htp调⽤⼯具
- ●如何使⽤Redis实现⻚⾯UV统计-HyperLogLog实现详解
- ●Redis 6.0 客户端缓存特性及实践


