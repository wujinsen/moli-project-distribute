⼀、概念

- 1、Spring Spring是⼀个开源容器框架，可以接管web层，业务层，dao层，持久层的组件，并且可以配置各种 bean,和维护bean与bean之间的关系。其核⼼就是控制反转(IOC),和⾯向切⾯(AOP),简单的说就是⼀个 分层的轻量级开源框架。
- 2、SpringMVC Spring MVC属于SpringFrameWork的后续产品，已经融合在Spring Web Flow⾥⾯。SpringMVC是⼀ 种web层mvc框架，⽤于替代servlet（处理|响应请求，获取表单参数，表单校验等。SpringMVC是⼀ 个MVC的开源框架，SpringMVC=struts2+spring，springMVC就相当于是Struts2加上Spring的整合。
- 3、SpringBot Springbot是⼀个微服务框架，延续了spring框架的核⼼思想IOC和AOP，简化了应⽤的开发和部署。 Spring Bot是为了简化Spring应⽤的创建、运⾏、调试、部署等⽽出现的，使⽤它可以做到专注于 Spring应⽤的开发，⽽⽆需过多关注XML的配置。提供了⼀堆依赖打包，并已经按照使⽤习惯解决了 依赖问题 ->习惯⼤于约定。 ⼆、原理和结构


- 1、Spring的原理和组成 Spring为简化我们的开发⼯作，封装了⼀系列的开箱即⽤的组件功能模块，包括：Spring JDBC 、 Spring MVC 、Spring Security、 Spring AOP 、Spring ORM 、Spring Test等。如下图：


![image 1](<Spring、SpringMVC和SpringBoot看这一篇就够了！.note_images/imageFile1.png>)

简化的理解图：

![image 2](<Spring、SpringMVC和SpringBoot看这一篇就够了！.note_images/imageFile2.png>)

# 2、SpringMVC的原理和组成 从上图中可以看出：SpringMVC是属于SpringWeb⾥⾯的⼀个功能模块（SpringWebMVC）。专⻔⽤ 来开发SpringWeb项⽬的⼀种MVC模式的技术框架实现。其原理如下：

![image 3](<Spring、SpringMVC和SpringBoot看这一篇就够了！.note_images/imageFile3.png>)

MVC：Model（模型）、VIew（视图）、Controler（控制器）；我们从开始接触并学习javaWeb开发 就知道MVC这⼀种架构，如早起的Sturts1、Sturts2等。

# 3、SpringBot的原理和特性

Spring Boot基本上是Spring框架的扩展，它消除了设置Spring应⽤程序所需的XML配置，为更快，更⾼效 的开发⽣态系统铺平了道路。 Spring Boot中的⼀些特点：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


创建独⽴的spring应⽤。 嵌⼊Tomcat,JettyUndertow ⽽且不需要部署他们。 提供的“starters” poms来简化Maven配置 尽可能⾃动配置spring应⽤。 提供⽣产指标,健壮检查和外部化配置 绝对没有代码⽣成和XML配置要求。

组成和结构如下图：

![image 4](<Spring、SpringMVC和SpringBoot看这一篇就够了！.note_images/imageFile4.png>)

从图中可以看出SpringBot是包含了Spring的核⼼（IOC）和（AOP）；以及封装了⼀些扩展，如 Stater：

![image 5](<Spring、SpringMVC和SpringBoot看这一篇就够了！.note_images/imageFile5.png>)

三、区别与总结

1.简单理解为：Spring包含了SpringMVC，⽽SpringBot⼜包含了Spring或者说是在Spring的基础上做 得⼀个扩展。

![image 6](<Spring、SpringMVC和SpringBoot看这一篇就够了！.note_images/imageFile6.png>)

- 2、关系⼤概就是这样： spring mvc < spring < springbot

- 3、Spring Boot 对⽐Spring的⼀些优点包括：

- 4、结论 Spring Boot只是Spring本身的扩展，使开发，测试和部署更加⽅便。


提供嵌⼊式容器⽀持

使⽤命令java -jar独⽴运⾏jar

在外部容器中部署时，可以选择排除依赖关系以避免潜在的jar冲突

部署时灵活指定配置⽂件的选项

⽤于集成测试的随机端⼝⽣成

