---
title: Spring 是如何解决循环依赖的？.note（原文插图 annex）
slug: annex-Spring-是如何解决循环依赖的？
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/源码分析/spring/Spring 是如何解决循环依赖的？.note.md
related: [spring-ioc与bean生命周期]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.zhihu.com/question/43824718/answer/173052725

作者：苏三说技术 链接：htps:/ w.zhihu.com/question/43824718/answer/173052725 来源：知乎 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

# 1.由同事抛的⼀个问题开始

最近项⽬组的⼀个同事遇到了⼀个问题，问我的意⻅，⼀下⼦引起的我的兴趣，因为这个问题我也是 第⼀次遇到。平时⾃认为对spring循环依赖问题还是⽐较了解的，直到遇到这个和后⾯的⼏个问题后， 重新刷新了我的认识。 我们先看看当时出问题的代码⽚段：

@Service

- public class TestService1 {

@Autowired private TestService2 testService2;

@Async public void test1() { }

}

@Service

- public class TestService2 {


@Autowired private TestService1 testService1;

public void test2() { }

} 这两段代码中定义了两个Service类：TestService1和TestService2，在TestService1中注⼊了 TestService2的实例，同时在TestService2中注⼊了TestService1的实例，这⾥构成了循环依赖。 最近⽆意间获得⼀份BAT⼤⼚⼤佬写的刷题笔记，⼀下⼦打通了我的任督⼆脉，越来越觉得算法没有想 象中那么难了。 [BAT⼤佬写的刷题笔记，让我ofer拿到⼿软](

这位BAT⼤佬写的Letcode刷题笔记，让我ofer拿到⼿ 软

) 只不过，这不是普通的循环依赖，因为TestService1的test1⽅法上加了⼀个@Async注解。 ⼤家猜猜程序启动后运⾏结果会怎样？

org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'testService1': Bean with name 'testService1' has been injected into other beans [testService2] in its raw version as part of a circular reference, but has eventually been wrapped. This means that said other beans do not use the final version of the bean. This is often the result of over-eager type matching - consider using 'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.

报错了。。。原因是出现了循环依赖。 「不科学呀，spring不是号称能解决循环依赖问题吗，怎么还会出现？」 如果把上⾯的代码稍微调整⼀下：

@Service public class TestService1 {

@Autowired private TestService2 testService2;

public void test1() { }

} 把TestService1的test1⽅法上的@Async注解去掉，TestService1和TestService2都需要注⼊对⽅的实 例，同样构成了循环依赖。 但是重新启动项⽬，发现它能够正常运⾏。这⼜是为什么？ 带着这两个问题，让我们⼀起开始spring循环依赖的探秘之旅。

# 2.什么是循环依赖？

循环依赖：说⽩是⼀个或多个对象实例之间存在直接或间接的依赖关系，这种依赖关系构成了构成⼀ 个环形调⽤。 第⼀种情况：⾃⼰依赖⾃⼰的直接依赖

![image 1](assets/imageFile1.png)

第⼆种情况：两个对象之间的直接依赖

![image 2](assets/imageFile2.png)

### 第三种情况：多个对象之间的间接依赖

<table>
  <tr>
    <th>![image 3](assets/imageFile3.png)</th>
  </tr>
</table>


前⾯两种情况的直接循环依赖⽐较直观，⾮常好识别，但是第三种间接循环依赖的情况有时候因为业 务代码调⽤层级很深，不容易识别出来。

# 3.循环依赖的N种场景

spring中出现循环依赖主要有以下场景：

![image 4](assets/imageFile4.png)

最近⽆意间获得⼀份BAT⼤⼚⼤佬写的刷题笔记，⼀下⼦打通了我的任督⼆脉，越来越觉得算法没有想 象中那么难了。 [BAT⼤佬写的刷题笔记，让我ofer拿到⼿软](

这位BAT⼤佬写的Letcode刷题笔记，让我ofer拿到⼿ 软

)

单例的seter注⼊

这种注⼊⽅式应该是spring⽤的最多的，代码如下：

@Service

- public class TestService1 {

@Autowired private TestService2 testService2;

public void test1() { }

}

@Service

- public class TestService2 {


@Autowired private TestService1 testService1;

public void test2() { }

}

这是⼀个经典的循环依赖，但是它能正常运⾏，得益于spring的内部机制，让我们根本⽆法感知它有问 题，因为spring默默帮我们解决了。 spring内部有三级缓存：

singletonObjects ⼀级缓存，⽤于保存实例化、注⼊、初始化完成的bean实例

earlySingletonObjects ⼆级缓存，⽤于保存实例化完成的bean实例

singletonFactories 三级缓存，⽤于保存bean创建⼯⼚，以便于后⾯扩展有机会创建代理对象。

下⾯⽤⼀张图告诉你，spring是如何解决循环依赖的：

<table>
  <tr>
    <th>![image 5](assets/imageFile5.png)</th>
  </tr>
</table>


- 图1 细⼼的朋友可能会发现在这种场景中第⼆级缓存作⽤不⼤。 那么问题来了，为什么要⽤第⼆级缓存呢？


试想⼀下，如果出现以下这种情况，我们要如何处理？

@Service

- public class TestService1 {

@Autowired

- private TestService2 testService2; @Autowired
- private TestService3 testService3;


public void test1() { }

}

@Service

- public class TestService2 {

@Autowired private TestService1 testService1;

public void test2() { }

}

@Service

- public class TestService3 {


@Autowired private TestService1 testService1;

public void test3() { }

}

TestService1依赖于TestService2和TestService3，⽽TestService2依赖于TestService1，同时 TestService3也依赖于TestService1。 按照上图的流程可以把TestService1注⼊到TestService2，并且TestService1的实例是从第三级缓存中 获取的。 最近⽆意间获得⼀份BAT⼤⼚⼤佬写的刷题笔记，⼀下⼦打通了我的任督⼆脉，越来越觉得算法没有想 象中那么难了。

[BAT⼤佬写的刷题笔记，让我ofer拿到⼿软]( ) 假设不⽤第⼆级缓存，TestService1注⼊到TestService3的流程如图：

这位BAT⼤佬写的Letcode刷题笔记，让我ofer拿到⼿ 软

<table>
  <tr>
    <th>![image 6](assets/imageFile6.png)</th>
  </tr>
</table>


- 图2 TestService1注⼊到TestService3⼜需要从第三级缓存中获取实例，⽽第三级缓存⾥保存的并⾮真正的 实例对象，⽽是ObjectFactory对象。说⽩了，两次从三级缓存中获取都是ObjectFactory对象，⽽通过 它创建的实例对象每次可能都不⼀样的。 这样不是有问题？ 为了解决这个问题，spring引⼊的第⼆级缓存。上⾯图1其实TestService1对象的实例已经被添加到第 ⼆级缓存中了，⽽在TestService1注⼊到TestService3时，只⽤从第⼆级缓存中获取该对象即可。


<table>
  <tr>
    <th>![image 7](assets/imageFile7.png)</th>
  </tr>
</table>


- 图3 还有个问题，第三级缓存中为什么要添加ObjectFactory对象，直接保存实例对象不⾏吗？ 答：不⾏，因为假如你想对添加到三级缓存中的实例对象进⾏增强，直接⽤实例对象是⾏不通的。 针对这种场景spring是怎么做的呢？ 答案就在AbstractAutowireCapableBeanFactory类doCreateBean⽅法的这段代码中：


<table>
  <tr>
    <th>![image 8](assets/imageFile8.png)</th>
  </tr>
</table>


它定义了⼀个匿名内部类，通过getEarlyBeanReference⽅法获取代理对象，其实底层是通过 AbstractAutoProxyCreator类的getEarlyBeanReference⽣成代理对象。

## 多例的seter注⼊

这种注⼊⽅法偶然会有，特别是在 的场景下，具体代码如下：

多线程

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) @Service

- public class TestService1 {

@Autowired private TestService2 testService2;

public void test1() { }

}

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) @Service

- public class TestService2 {


@Autowired private TestService1 testService1;

public void test2() { }

} 很多⼈说这种情况spring容器启动会报错，其实是不对的，我⾮常负责任的告诉你程序能够正常启动。 为什么呢？ 其实在AbstractApplicationContext类的refresh⽅法中告诉了我们答案，它会调⽤ finishBeanFactoryInitialization⽅法，该⽅法的作⽤是为了spring容器启动的时候提前初始化⼀些 bean。该⽅法的内部⼜调⽤了preInstantiateSingletons⽅法

![image 9](assets/imageFile9.png)

标红的地⽅明显能够看出：⾮抽象、单例 并且⾮懒加载的类才能被提前初始bean。 ⽽多例即SCOPE_PROTOTYPE类型的类，⾮单例，不会被提前初始化bean，所以程序能够正常启动。 如何让他提前初始化bean呢？

只需要再定义⼀个单例的类，在它⾥⾯注⼊TestService1

@Service

- public class TestService3 {


@Autowired private TestService1 testService1;

}

重新启动程序，执⾏结果：

Requested bean is currently in creation: Is there an unresolvable circular reference?

果然出现了循环依赖。 注意：这种循环依赖问题是⽆法解决的，因为它没有⽤缓存，每次都会⽣成⼀个新对象。

## 构造器注⼊

这种注⼊⽅式现在其实⽤的已经⾮常少了，但是我们还是有必要了解⼀下，看看如下代码：

@Service

- public class TestService1 {

public TestService1(TestService2 testService2) { }

}

@Service

- public class TestService2 {


public TestService2(TestService1 testService1) { }

}

运⾏结果：

Requested bean is currently in creation: Is there an unresolvable circular reference?

出现了循环依赖，为什么呢？

<table>
  <tr>
    <th>![image 10](assets/imageFile10.png)</th>
  </tr>
</table>


从图中的流程看出构造器注⼊没能添加到三级缓存，也没有使⽤缓存，所以也⽆法解决循环依赖问 题。

## 单例的代理对象seter注⼊

这种注⼊⽅式其实也⽐较常⽤，⽐如平时使⽤：@Async注解的场景，会通过AOP⾃动⽣成代理对象。 我那位同事的问题也是这种情况。 @Service

- public class TestService1 {

@Autowired private TestService2 testService2;

@Async public void test1() { }

}

@Service

- public class TestService2 {


@Autowired private TestService1 testService1;

public void test2() { }

}

从前⾯得知程序启动会报错，出现了循环依赖：

org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'testService1': Bean with name 'testService1' has been injected into other beans [testService2] in its raw version as part of a circular reference, but has eventually been wrapped. This means that said other beans do not use the final version of the bean. This is often the result of over-eager type matching - consider using 'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.

为什么会循环依赖呢？ 答案就在下⾯这张图中：

<table>
  <tr>
    <th>![image 11](assets/imageFile11.png)</th>
  </tr>
</table>


### 说⽩了，bean初始化完成之后，后⾯还有⼀步去检查：第⼆级缓存 和 原始对象 是否相等。由于它对 前⾯流程来说⽆关紧要，所以前⾯的流程图中省略了，但是在这⾥是关键点，我们重点说说：

<table>
  <tr>
    <th>![image 12](assets/imageFile12.png)</th>
  </tr>
</table>


那位同事的问题正好是⾛到这段代码，发现第⼆级缓存 和 原始对象不相等，所以抛出了循环依赖的异 常。 如果这时候把TestService1改个名字，改成：TestService6，其他的都不变。

@Service public class TestService6 {

@Autowired private TestService2 testService2;

@Async public void test1() { }

}

再重新启动⼀下程序，神奇般的好了。 what？ 这⼜是为什么？ 这就要从spring的bean加载顺序说起了，默认情况下，spring是按照⽂件完整路径递归查找的，按路 径+⽂件名排序，排在前⾯的先加载。所以TestService1⽐TestService2先加载，⽽改了⽂件名称之 后，TestService2⽐TestService6先加载。 为什么TestService2⽐TestService6先加载就没问题呢？ 答案在下⾯这张图中：

<table>
  <tr>
    <th>![image 13](assets/imageFile13.png)</th>
  </tr>
</table>


这种情况testService6中其实第⼆级缓存是空的，不需要跟原始对象判断，所以不会抛出循环依赖。

## DependsOn循环依赖

还有⼀种有些特殊的场景，⽐如我们需要在实例化Bean A之前，先实例化Bean B，这个时候就可以使 ⽤@DependsOn注解。 @DependsOn(value = "testService2") @Service public class TestService1 {

@Autowired private TestService2 testService2;

public void test1() { }

}

@DependsOn(value = "testService1") @Service public class TestService2 {

@Autowired private TestService1 testService1;

public void test2() { }

}

程序启动之后，执⾏结果： Circular depends-on relationship between 'testService2' and 'testService1' 这个例⼦中本来如果TestService1和TestService2都没有加@DependsOn注解是没问题的，反⽽加了这个 注解会出现循环依赖问题。 这⼜是为什么？ 答案在AbstractBeanFactory类的doGetBean⽅法的这段代码中：

![image 14](assets/imageFile14.png)

它会检查dependsOn的实例有没有循环依赖，如果有循环依赖则抛异常。

# 4.出现循环依赖如何解决？

项⽬中如果出现循环依赖问题，说明是spring默认⽆法解决的循环依赖，要看项⽬的打印⽇志，属于哪 种循环依赖。⽬前包含下⾯⼏种情况：

<table>
  <tr>
    <th>![image 15](assets/imageFile15.png)</th>
  </tr>
</table>


## ⽣成代理对象产⽣的循环依赖

这类循环依赖问题解决⽅法很多，主要有：

- 1.
- 2.
- 3.


使⽤@Lazy注解，延迟加载 使⽤@DependsOn注解，指定加载先后关系 修改⽂件名称，改变循环依赖类的加载顺序

## 使⽤@DependsOn产⽣的循环依赖

这类循环依赖问题要找到@DependsOn注解循环依赖的地⽅，迫使它不循环依赖就可以解决问题。

## 多例循环依赖

这类循环依赖问题可以通过把bean改成单例的解决。

## 构造器循环依赖

这类循环依赖问题可以通过使⽤@Lazy注解解决。 最近⽆意间获得⼀份阿⾥⼤佬写的刷题笔记，⼀下⼦打通了我的任督⼆脉，进⼤⼚原来没那么难。

<table>
  <tr>
    <th>![image 16](assets/imageFile16.png)</th>
  </tr>
</table>


链接： 密码：bhbe 不会有⼈刷到这⾥还想⽩嫖吧？点赞对我真的⾮常重要！在线求赞。加个关注我会⾮常感激！

htps:/pan.baidu.com/s/1UECE5yuaoTRpJfi5LU5TQ

@苏三说技术

# 最后说⼀句(求关注，别⽩嫖我)

如果这篇⽂章对您有所帮助，或者有所启发的话，帮忙关注⼀下：苏三说技术，您的⽀持是我坚持写 作最⼤的动⼒。收藏不是真爱，点赞才是。
