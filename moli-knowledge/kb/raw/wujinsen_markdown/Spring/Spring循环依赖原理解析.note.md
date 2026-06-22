什么是循环依赖？

很简单，就是A对象依赖了B对象，B对象依赖了A对象。

⽐如：

- / A依赖了B

- clas A{ public B b;

} / B依赖了A

- clas B{ public A a;




}

那么循环依赖是个问题吗？

如果不考虑Spring，循环依赖并不是问题，因为对象之间相互依赖是很正常的事情。

⽐如

- A a = new A();
- B b = new B(); a.b = b;b.a = a;


这样，A,B就依赖上了。

但是，在Spring中循环依赖就是⼀个问题了，为什么？

因为，在Spring中，⼀个对象并不是简单new出来了，⽽是会经过⼀系列的Bean的⽣命周期，就是因 为Bean的⽣命周期所以才会出现循环依赖问题。当然，在Spring中，出现循环依赖的场景很多，有的 场景Spring⾃动帮我们解决了，⽽有的场景则需要程序员来解决，下⽂详细来说。

要明⽩Spring中的循环依赖，得先明⽩Spring中Bean的⽣命周期。

Bean的⽣命周期

这⾥不会对Bean的⽣命周期进⾏详细的描述，只描述⼀下⼤概的过程。

Bean的⽣命周期指的就是：在Spring中，Bean是如何⽣成的？

被Spring管理的对象叫做Bean。Bean的⽣成步骤如下：

Spring扫描clas得到BeanDefinition 根据得到的BeanDefinition去⽣成bean ⾸先根据clas推断构造⽅法 根据推断出来的构造⽅法，反射，得到⼀个对象（暂时叫做原始对象） 填充原始对象中的属性（依赖注⼊） 如果原始对象中的某个⽅法被AOP了，那么则需要根据原始对象⽣成⼀个代理对象 把最终⽣成的代理对象放⼊单例池（源码中叫做singletonObjects）中，下次getBean时就直接从单例 池拿即可 可以看到，对于Spring中的Bean的⽣成过程，步骤还是很多的，并且不仅仅只有上⾯的7步，还有很多 很多，⽐如Aware回调、初始化等等，这⾥不详细讨论。

可以发现，在Spring中，构造⼀个Bean，包括了new这个步骤（第4步构造⽅法反射）。

得到⼀个原始对象后，Spring需要给对象中的属性进⾏依赖注⼊，那么这个注⼊过程是怎样的？

⽐如上⽂说的A类，A类中存在⼀个B类的b属性，所以，当A类⽣成了⼀个原始对象之后，就会去给b属 性去赋值，此时就会根据b属性的类型和属性名去BeanFactory中去获取B类所对应的单例bean。如果 此时BeanFactory中存在B对应的Bean，那么直接拿来赋值给b属性；如果此时BeanFactory中不存在B 对应的Bean，则需要⽣成⼀个B对应的Bean，然后赋值给b属性。

问题就出现在第⼆种情况，如果此时B类在BeanFactory中还没有⽣成对应的Bean，那么就需要去⽣ 成，就会经过B的Bean的⽣命周期。

那么在创建B类的Bean的过程中，如果B类中存在⼀个A类的a属性，那么在创建B的Bean的过程中就需 要A类对应的Bean，但是，触发B类Bean的创建的条件是A类Bean在创建过程中的依赖注⼊，所以这⾥ 就出现了循环依赖：

ABean创建–>依赖了B属性–>触发 Bean创建—>B依赖了A属性—>需要ABean（但ABean还在创建过 程中）

从⽽导致ABean创建不出来， Bean也创建不出来。

这是循环依赖的场景，但是上⽂说了，在Spring中，通过某些机制帮开发者解决了部分循环依赖的问 题，这个机制就是三级缓存。

三级缓存

三级缓存是通⽤的叫法。 ⼀级缓存为：singletonObjects ⼆级缓存为：earlySingletonObjects 三级缓存为：singletonFactories 先稍微解释⼀下这三个缓存的作⽤，后⾯详细分析：

singletonObjects中缓存的是已经经历了完整⽣命周期的bean对象。 earlySingletonObjects⽐singletonObjects多了⼀个early，表示缓存的是早期的bean对象。早期是什 么意思？表示Bean的⽣命周期还没⾛完就把这个Bean放⼊了earlySingletonObjects。 singletonFactories中缓存的是ObjectFactory，表示对象⼯⼚，⽤来创建某个对象的。 解决循环依赖思路分析 先来分析为什么缓存能解决循环依赖。

上⽂分析得到，之所以产⽣循环依赖的问题，主要是：

A创建时—>需要B ->B去创建—>需要A，从⽽产⽣了循环

那么如何打破这个循环，加个中间⼈（缓存）

A的Bean在创建过程中，在进⾏依赖注⼊之前，先把A的原始Bean放⼊缓存（提早暴露，只要放到缓 存了，其他Bean需要时就可以从缓存中拿了），放⼊缓存后，再进⾏依赖注⼊，此时A的Bean依赖了B 的Bean，如果B的Bean不存在，则需要创建B的Bean，⽽创建B的Bean的过程和A⼀样，也是先创建⼀ 个B的原始对象，然后把B的原始对象提早暴露出来放⼊缓存中，然后在对B的原始对象进⾏依赖注⼊ A，此时能从缓存中拿到A的原始对象（虽然是A的原始对象，还不是最终的Bean），B的原始对象依 赖注⼊完了之后，B的⽣命周期结束，那么A的⽣命周期也能结束。

因为整个过程中，都只有⼀个A原始对象，所以对于B⽽⾔，就算在属性注⼊时，注⼊的是A原始对 象，也没有关系，因为A原始对象在后续的⽣命周期中在堆中没有发⽣变化。

从上⾯这个分析过程中可以得出，只需要⼀个缓存就能解决循环依赖了，那么为什么Spring中还需要 singletonFactories呢？

这是难点，基于上⾯的场景想⼀个问题：如果A的原始对象注⼊给B的属性之后，A的原始对象进⾏了 AOP产⽣了⼀个代理对象，此时就会出现，对于A⽽⾔，它的Bean对象其实应该是AOP之后的代理对 象，⽽B的a属性对应的并不是AOP之后的代理对象，这就产⽣了冲突。

B依赖的A和最终的A不是同⼀个对象。

那么如何解决这个问题？这个问题可以说没有办法解决。

因为在⼀个Bean的⽣命周期最后，Spring提供了BeanPostProcesor可以去对Bean进⾏加⼯，这个加 ⼯不仅仅只是能修改Bean的属性值，也可以替换掉当前Bean。

举个例⼦：

@Component public clas User { }

- 1
- 2
- 3 @Component public clas LubanBeanPostProcesor implements BeanPostProcesor {


@Overide public Object postProcesAfterInitialization(Object bean, String beanName) throws

BeansException { / 注意这⾥，⽣成了⼀个新的User对象

if (beanName.equals("user") { System.out.println(bean); User user = new User(); return user;

}

return bean; }

}

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10 1


12 13 public clas Test {

public static void main(String[] args) {

AnotationConfigAplicationContext context = new AnotationConfigAplicationContext(ApConfig.clas);

User user = context.getBean("user", User.clas); System.out.println(user); }

- 1
- 2
- 3
- 4
- 5 运⾏main⽅法，得到的打印如下：


com.luban.service.User@5e025e70 com.luban.service.User@1b0375b3

- 1
- 2 所以在BeanPostProcesor中可以完全替换掉某个beanName对应的bean对象。


⽽BeanPostProcesor的执⾏在Bean的⽣命周期中是处于属性注⼊之后的，循环依赖是发⽣在属性注 ⼊过程中的，所以很有可能导致，注⼊给B对象的A对象和经历过完整⽣命周期之后的A对象，不是⼀ 个对象。这就是有问题的。

所以在这种情况下的循环依赖，Spring是解决不了的，因为在属性注⼊时，Spring也不知道A对象后续 会经过哪些BeanPostProcesor以及会对A对象做什么处理。

Spring到底解决了哪种情况下的循环依赖

虽然上⾯的情况可能发⽣，但是肯定发⽣得很少，我们通常在开发过程中，不会这样去做，但是，某 个beanName对应的最终对象和原始对象不是⼀个对象却会经常出现，这就是AOP。

AOP就是通过⼀个BeanPostProcesor来实现的，这个BeanPostProcesor就是 AnotationAwareAspectJAutoProxyCreator，它的⽗类是AbstractAutoProxyCreator，⽽在Spring中 AOP利⽤的要么是JDK动态代理，要么CGLib的动态代理，所以如果给⼀个类中的某个⽅法设置了切 ⾯，那么这个类最终就需要⽣成⼀个代理对象。

⼀般过程就是：A类—>⽣成⼀个普通对象–>属性注⼊–>基于切⾯⽣成⼀个代理对象–>把代理对象放⼊ singletonObjects单例池中。

⽽AOP可以说是Spring中除开IOC的另外⼀⼤功能，⽽循环依赖⼜是属于IOC范畴的，所以这两⼤功能 想要并存，Spring需要特殊处理。

如何处理的，就是利⽤了第三级缓存singletonFactories。

⾸先，singletonFactories中存的是某个beanName对应的ObjectFactory，在bean的⽣命周期中，⽣成 完原始对象之后，就会构造⼀个ObjectFactory存⼊singletonFactories中。这个ObjectFactory是⼀个 函数式接⼝，所以⽀持Lambda表达式：() -> getEarlyBeanReference(beanName, mbd, bean)

上⾯的Lambda表达式就是⼀个ObjectFactory，执⾏该Lambda表达式就会去执⾏ getEarlyBeanReference⽅法，⽽该⽅法如下：

protected Object getEarlyBeanReference(String beanName, RotBeanDefinition mbd, Object bean) {

Object exposedObject = bean; if (!mbd.isSynthetic() & hasInstantiationAwareBeanPostProcesors() {

for (BeanPostProcesor bp : getBeanPostProcesors() {

if (bp instanceof SmartInstantiationAwareBeanPostProcesor) {

SmartInstantiationAwareBeanPostProcesor ibp = (SmartInstantiationAwareBeanPostProcesor) bp;

exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName); } } } return exposedObject;

}

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9 该⽅法会去执⾏SmartInstantiationAwareBeanPostProcesor中的getEarlyBeanReference⽅法，⽽这 个接⼝下的实现类中只有两个类实现了这个⽅法，⼀个是AbstractAutoProxyCreator，⼀个是 InstantiationAwareBeanPostProcesorAdapter，它的实现如下：


/ InstantiationAwareBeanPostProcesorAdapter @Overidepublic Object getEarlyBeanReference(Object bean, String beanName) throws BeansException { return bean; }

- 1
- 2
- 3 / AbstractAutoProxyCreator


@Overidepublic Object getEarlyBeanReference(Object bean, String beanName) { Object cacheKey = getCacheKey(bean.getClas(), beanName); this.earlyProxyReferences.put(cacheKey, bean);

return wrapIfNecesary(bean, beanName, cacheKey); }

- 1
- 2
- 3
- 4


所以很明显，在整个Spring中，默认就只有AbstractAutoProxyCreator真正意义上实现了 getEarlyBeanReference⽅法，⽽该类就是⽤来进⾏AOP的。上⽂提到的 AnotationAwareAspectJAutoProxyCreator的⽗类就是AbstractAutoProxyCreator。

那么getEarlyBeanReference⽅法到底在⼲什么？

⾸先得到⼀个cachekey，cachekey就是beanName。

然后把beanName和bean（这是原始对象）存⼊earlyProxyReferences中

调⽤wrapIfNecesary进⾏AOP，得到⼀个代理对象。

那么，什么时候会调⽤getEarlyBeanReference⽅法呢？回到循环依赖的场景中

左边⽂字： 这个ObjectFactory就是上⽂说的labmda表达式，中间有getEarlyBeanReference⽅法，注意存⼊ singletonFactories时并不会执⾏lambda表达式，也就是不会执⾏getEarlyBeanReference⽅法 右边⽂字： 从singletonFactories根据beanName得到⼀个ObjectFactory，然后执⾏ObjectFactory，也就是执⾏ getEarlyBeanReference⽅法，此时会得到⼀个A原始对象经过AOP之后的代理对象，然后把该代理对 象放⼊earlySingletonObjects中，注意此时并没有把代理对象放⼊singletonObjects中，那什么时候放 ⼊到singletonObjects中呢？ 我们这个时候得来理解⼀下earlySingletonObjects的作⽤，此时，我们只得到了A原始对象的代理对 象，这个对象还不完整，因为A原始对象还没有进⾏属性填充，所以此时不能直接把A的代理对象放⼊ singletonObjects中，所以只能把代理对象放⼊earlySingletonObjects，假设现在有其他对象依赖了 A，那么则可以从earlySingletonObjects中得到A原始对象的代理对象了，并且是A的同⼀个代理对 象。

当B创建完了之后，A继续进⾏⽣命周期，⽽A在完成属性注⼊后，会按照它本身的逻辑去进⾏AOP， ⽽此时我们知道A原始对象已经经历过了AOP，所以对于A本身⽽⾔，不会再去进⾏AOP了，那么怎么 判断⼀个对象是否经历过了AOP呢？会利⽤上⽂提到的earlyProxyReferences，在 AbstractAutoProxyCreator的postProcesAfterInitialization⽅法中，会去判断当前beanName是否在 earlyProxyReferences，如果在则表示已经提前进⾏过AOP了，⽆需再次进⾏AOP。

对于A⽽⾔，进⾏了AOP的判断后，以及BeanPostProcesor的执⾏之后，就需要把A对应的对象放⼊ singletonObjects中了，但是我们知道，应该是要A的代理对象放⼊singletonObjects中，所以此时需 要从earlySingletonObjects中得到代理对象，然后⼊singletonObjects中。

整个循环依赖解决完毕。

总结

⾄此，总结⼀下三级缓存：

singletonObjects：缓存某个beanName对应的经过了完整⽣命周期的bean earlySingletonObjects：缓存提前拿原始对象进⾏了AOP之后得到的代理对象，原始对象还没有进⾏ 属性注⼊和后续的BeanPostProcesor等⽣命周期 singletonFactories：缓存的是⼀个ObjectFactory，主要⽤来去⽣成原始对象进⾏了AOP之后得到的代 理对象，在每个Bean的⽣成过程中，都会提前暴露⼀个⼯⼚，这个⼯⼚可能⽤到，也可能⽤不到，如 果没有出现循环依赖依赖本bean，那么这个⼯⼚⽆⽤，本bean按照⾃⼰的⽣命周期执⾏，执⾏完后直 接把本bean放⼊singletonObjects中即可，如果出现了循环依赖依赖了本bean，则另外那个bean执⾏ ObjectFactory提交得到⼀个AOP之后的代理对象(如果有AOP的话，如果⽆需AOP，则直接得到⼀个原 始对象)。 其实还要⼀个缓存，就是earlyProxyReferences，它⽤来记录某个原始对象是否进⾏过AOP了。 哪⾥不对的，或者有不同的理解的，欢迎评论⾥⾯指出来，觉得有帮助的，可以点个赞⽀持⼀下

⸻版权声明：本⽂为CSDN博主「⽉云银」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上原⽂ 出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/yueyunyin/article/details/10830428

