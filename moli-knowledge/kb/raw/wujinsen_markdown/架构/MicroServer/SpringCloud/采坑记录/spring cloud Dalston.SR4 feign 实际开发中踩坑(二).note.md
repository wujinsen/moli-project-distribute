htps:/my.oschina.net/1 0/blog/159147

- 坑4、 ⽆法扫描到引⽤包的feign接⼝ 在实际的⽣产中 我们服务模块是有很多的, 如果 A的接⼝ B要调⽤ 我们声明⼀次feigin客户端 api 然 后C也要调A的接⼝ 我们还要声明⼀次feigin客户端api 如果还有更多 就会有很多重复代码 为了提⾼ 代码复⽤，我们往往单独声明⼀个包来引⼊feigin 的api 其他包如果调⽤的化引⼊到⼯程中就⾏了 但 是 问题来了 引⽤包⽆法被直接扫描到 我们知道 ⼀个微服务模块 如果需要开启feign调⽤功能 需要加


上

@EnableFeignClients

源码：

/*

- * Copyright 2013-2015 the original author or authors.

*

- * Licensed under the Apache License, Version 2.0 (the "License");

- * you may not use this file except in compliance with the License.

- * You may obtain a copy of the License at

*

- * http://www.apache.org/licenses/LICENSE-2.0

*

- * Unless required by applicable law or agreed to in writing, software

- * distributed under the License is distributed on an "AS IS" BASIS,

- * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

- * See the License for the specific language governing permissions and

- * limitations under the License.

- */


package org.springframework.cloud.netflix.feign;

import java.lang.annotation.Documented; import java.lang.annotation.ElementType; import java.lang.annotation.Retention; import java.lang.annotation.RetentionPolicy; import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**

- * Scans for interfaces that declare they are feign clients (via {@link FeignClient

- * <code>@FeignClient</code>}). Configures component scanning directives for use with

- * {@link org.springframework.context.annotation.Configuration

- * <code>@Configuration</code>} classes.

*

- * @author Spencer Gibb

- * @author Dave Syer

- * @since 1.0

- */


@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @Documented @Import(FeignClientsRegistrar.class) public @interface EnableFeignClients {

/**

* Alias for the {@link #basePackages()} attribute. Allows for more concise annotation

- * declarations e.g.: {@code @ComponentScan("org.my.pkg")} instead of

- * {@code @ComponentScan(basePackages="org.my.pkg")}.

- * @return the array of 'basePackages'.

- */


String[] value() default {};

/**

- * Base packages to scan for annotated components.

- * <p>

- * {@link #value()} is an alias for (and mutually exclusive with) this attribute.

- * <p>

- * Use {@link #basePackageClasses()} for a type-safe alternative to String-based

- * package names.

*

- * @return the array of 'basePackages'.

- */


String[] basePackages() default {};

/**

- * Type-safe alternative to {@link #basePackages()} for specifying the packages to

- * scan for annotated components. The package of each class specified will be


scanned.

- * <p>

- * Consider creating a special no-op marker class or interface in each package that

- * serves no purpose other than being referenced by this attribute.

*

- * @return the array of 'basePackageClasses'.

- */


Class<?>[] basePackageClasses() default {};

/**

- * A custom <code>@Configuration</code> for all feign clients. Can contain override

- * <code>@Bean</code> definition for the pieces that make up the client, for


instance

- * {@link feign.codec.Decoder}, {@link feign.codec.Encoder}, {@link feign.Contract}.

*

- * @see FeignClientsConfiguration for the defaults

- */


Class<?>[] defaultConfiguration() default {};

/**

* List of classes annotated with @FeignClient. If not empty, disables classpath scanning.

- * @return

- */


Class<?>[] clients() default {}; }

其中basePackages 声明 扫描 feignclient 注解所在的包的包路径 声明后就能扫描到你在该包下 @FeignClient 标记的 feign接⼝

- 坑5、 ⽆法扫描到引⼊包的服务降级实现，⼤多数情况 我们要对feignClient接⼝ 显式声明⼀个 falback 以便进⾏服务降级 但是如果你的feignclient 接⼝ 不在 springbot 的启动类的⼦类 会⽆法启


动 显示

Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'com.xxx.feign.api.UserService': FactoryBean threw exception on object creation; nested exception is java.lang.IllegalStateException:

No fallback instance of type class com.xxx.feign.api.UserServiceHystrix found for feign client MEMBER-SERVICE

也就是你feign 接⼝的实现类 ⽆法被注⼊

先看⼀下 源码：

/*

* Copyright 2013-2016 the original author or authors.

*

- * Licensed under the Apache License, Version 2.0 (the "License");

- * you may not use this file except in compliance with the License.

- * You may obtain a copy of the License at


*

* http://www.apache.org/licenses/LICENSE-2.0

*

- * Unless required by applicable law or agreed to in writing, software

- * distributed under the License is distributed on an "AS IS" BASIS,

- * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

- * See the License for the specific language governing permissions and

- * limitations under the License.

- */


package org.springframework.cloud.netflix.feign;

import java.lang.annotation.Documented; import java.lang.annotation.ElementType; import java.lang.annotation.Retention; import java.lang.annotation.RetentionPolicy; import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**

- * Annotation for interfaces declaring that a REST client with that interface should be

- * created (e.g. for autowiring into another component). If ribbon is available it will be

- * used to load balance the backend requests, and the load balancer can be configured

- * using a <code>@RibbonClient</code> with the same name (i.e. value) as the feign client.

*

- * @author Spencer Gibb

- * @author Venil Noronha

- */


@Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) @Documented public @interface FeignClient {

/**

- * The name of the service with optional protocol prefix. Synonym for {@link #name()

- * name}. A name must be specified for all clients, whether or not a url is


provided.

- * Can be specified as property key, eg: ${propertyKey}.


- */


@AliasFor("name") String value() default "";

/**

- * The service id with optional protocol prefix. Synonym for {@link #value() value}.

*

- * @deprecated use {@link #name() name} instead

- */


@Deprecated String serviceId() default "";

/**

- * The service id with optional protocol prefix. Synonym for {@link #value() value}.

- */


@AliasFor("value") String name() default "";

/**

- * Sets the <code>@Qualifier</code> value for the feign client.

- */


String qualifier() default "";

/**

- * An absolute URL or resolvable hostname (the protocol is optional).

- */


String url() default "";

/**

- * Whether 404s should be decoded instead of throwing FeignExceptions

- */


boolean decode404() default false;

/**

- * A custom <code>@Configuration</code> for the feign client. Can contain override

- * <code>@Bean</code> definition for the pieces that make up the client, for


instance

- * {@link feign.codec.Decoder}, {@link feign.codec.Encoder}, {@link feign.Contract}.

*

- * @see FeignClientsConfiguration for the defaults

- */


Class<?>[] configuration() default {};

/**

- * Fallback class for the specified Feign client interface. The fallback class must


- * implement the interface annotated by this annotation and be a valid spring bean.

- */


Class<?> fallback() default void.class;

/**

- * Define a fallback factory for the specified Feign client interface. The fallback

- * factory must produce instances of fallback classes that implement the interface

- * annotated by {@link FeignClient}. The fallback factory must be a valid spring

- * bean.

*

- * @see feign.hystrix.FallbackFactory for details.

- */


Class<?> fallbackFactory() default void.class;

/**

- * Path prefix to be used by all method-level mappings. Can be used with or without

- * <code>@RibbonClient</code>.

- */


String path() default "";

/**

- * Whether to mark the feign proxy as a primary bean. Defaults to true.

- */


boolean primary() default true;

}

* Falback clas for the specified Feign client interface. The falback clas must

implement the interface anotated by this anotation and be a valid spring bean. * falback 上会有这样的注释 说的是 声明feign客户端接⼝ 的降级类 ⽽且 这个降级类必须实现 该 feign 接⼝ 并且必须是⼀个可⽤的spring bean

如果你仅仅在这个实现类上加⼊spring bean 声明注解 ⽐如 你会发现依然 ⽆法注 ⼊ 来⼤致猜想⼀下流程 熟悉springbot 的 应该清楚 springbot 启动的时候 会扫描其main类 所 在包的⼦包进⾏ bean 实例化 如果不在⼦包 默认是扫描不到的 那么如何扫描到呢 声明扫描的路径 也就是需要在main类上使⽤注解@ComponentScan 注解 但是 如果 我们仅仅声明了 feign 降级实现 的路径 你会发现 main类的⼦包⽆法扫描到了 所以 此处应该

@Component

@ComponentScan(basePackages = {"main 所在的包","降级类所在的包"})

配置好后 我们写⼀个降级类：

@Component public class UserServiceHystrix implements UserService {

@Override public User get(User user) {

System.out.println("<><><><><><><><><><> MEMBER-SERVICE 挂了<><><><><><><><><><> "); user.setAge(20017); user.setGender("male"); user.setName("服务挂了");

return user; }

}

然后我们测试⼀下 启动消费⽅ 不启动 提供⽅MEMBER-SERVICE 发现熔断可⽤：

![image 1](<spring cloud Dalston.SR4 feign 实际开发中踩坑(二).note_images/imageFile1.png>)

![image 2](<spring cloud Dalston.SR4 feign 实际开发中踩坑(二).note_images/imageFile2.png>)

