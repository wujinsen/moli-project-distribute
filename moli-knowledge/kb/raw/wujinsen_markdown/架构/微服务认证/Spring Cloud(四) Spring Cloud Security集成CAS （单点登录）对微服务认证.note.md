# ⼀、前⾔

由于leader要求在搭好的spring cloud 框架中加⼊对微服务的认证包括单点登录认证，来确保系统的安全，所 以研究了Spring Cloud Security这个组件。在前⾯搭好的demo中，如何确保微服务的安全，为整个系统添加 安全控制，就需要⽤到Spring Cloud Security。⽤户通过服务⽹关zul来访问任何⼀个微服务的时候，都需要 跳转到第三⽅的认证⽐如github或者⾃⼰搭好的CAS单点登录服务，当认证通过才能访问对应的服务。在研 究spring cloud security 之前先对⼀些概念进⾏了解了。 OAuth2（重点），参考⽂档：http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html Spring Security OAuth2，参考⽂档：http://docs.spring.io/springboot/docs/1.5.2.RELEASE/reference/htmlsingle/#boot-features-security-oauth2

在这个⽂章中主要记录当⽤户通过服务⽹关zul⼊⼝访问任何⼀个微服务。需要先跳转到GitHub，使⽤ Github进⾏认证，认证通过之后才能跳转到访问我们提供的微服务。

⼆、详细实现

- 2.1 准备⼯作

- (1) 前往https://github.com/settings/developers，点击“Register a new aplication”按钮，添加⼀个应 ⽤。点击按钮后，界⾯如下图所示。Homepage URL 和calback url是写zul的端⼝。

- (2) 点击“Register aplication”按钮，即可出现如下图的界⾯。


记住这边的Client ID以及Client Secret，后⾯有⽤。 ⾄此，准备⼯作就完成了。

- 2.2 编码


代码测试成功之后的Github地址:https://github.com/LoveIpo/spring-clouddemo/tree/master/Zuul_CAS这个Zul_CAS是在zul中进⼀步完善！ 在这⾥，我们正式进⾏编码。因为我是在服务⽹关zul中添加单点登录的服务认证授权。所以对前⾯demo中 的zul ⼯程进⼀步完善。

- （1） 在pom.xml⽂件为应⽤添加spring-cloud-starter-oauth2、spring-cloud-starter-security两个依赖。


<dependencies>

<dependency> <groupId>org.springframework.cloud</groupId> <artifactId>spring-cloud-starter-zuul</artifactId>

</dependency> <dependency>

<groupId>org.springframework.cloud</groupId> <artifactId>spring-cloud-starter</artifactId>

</dependency> <dependency>

<groupId>org.springframework.cloud</groupId> <artifactId>spring-cloud-starter-eureka</artifactId>

</dependency> <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-test</artifactId> <scope>test</scope>

</dependency> <dependency>

<groupId>org.springframework.cloud</groupId> <artifactId>spring-cloud-starter-oauth2</artifactId>

</dependency> <dependency>

<groupId>org.springframework.cloud</groupId> <artifactId>spring-cloud-starter-security</artifactId>

</dependency> </dependencies>

- （2） 在zul的启动类中添加如下代码


@SpringBootApplication @EnableZuulProxy @RestController public class GatewayApplication {

public static void main(String[] args) {

SpringApplication.run(GatewayApplication.class, args); }

@GetMapping("/") public String welcome() { return "welcome"; }

@RequestMapping("/user") public Principal user(Principal user) {

return user; }

@Component @EnableOAuth2Sso // 实现基于OAuth2的单点登录，建议跟踪进代码阅读以下该注解的注释，很有⽤ public static class SecurityConfiguration extends WebSecurityConfigurerAdapter {

@Override public void configure(HttpSecurity http) throws Exception {

http.

antMatcher("/**") // 所有请求都得经过认证和授权

.authorizeRequests().anyRequest().authenticated()

.and().authorizeRequests().antMatchers("/","/anon").permitAll()

.and() // 这⾥之所以要禁⽤csrf，是为了⽅便。 // 否则，退出链接必须要发送⼀个post请求，请求还得带csrf token // 那样我还得写⼀个界⾯，发送post请求

.csrf().disable() // 退出的URL是/logout

.logout().logoutUrl("/logout").permitAll() // 退出成功后，跳转到/路径。

.logoutSuccessUrl("/login"); }

} }

如代码所示，在这⾥，我们使⽤@EnableOAuth2Sso 注解，启⽤了“基于OAuth2的单点登录”，做了⼀些安全 配置；同时，还定义了两个端点，/ 端点返回“welcome”字符串，/user 端点返回当前登录⽤户的认证信息。

这⾥说明⼀下，@EnableOAuth2Sso注解。如果WebSecurityConfigurerAdapter类上注释了 @EnableOAuth2Sso注解，那么将会添加身份验证过滤器和身份验证⼊⼝。如果只有⼀个 @EnableOAuth2Sso注解没有编写在WebSecurityConfigurerAdapter上，那么它将会为所有路径启⽤安全， 并且会在基于HTP Basic认证的安全链之前被添加。详⻅@EnableOAuth2Sso的注释。

- （3） 修改zul 的aplication.yml⽂件，部分代码如下 server:


port: 7073 security: user:

password: user # 直接登录时的密码 ignored: / sessions: never # session策略 oauth2:

sso: loginPath: /login # 登录路径

client: clientId: 你的clientId clientSecret: 你的clientSecret accessTokenUri: https://github.com/login/oauth/access_token userAuthorizationUri: https://github.com/login/oauth/authorize

resource: userInfoUri: https://api.github.com/user preferTokenInfo: false

spring:

application: name: zuul

eureka: client: serviceUrl: defaultZone: http://localhost:7071/eureka/

这样，通过服务⽹关zul来访问任何⼀个服务都要跳转到github进⾏认证的主要代码就编写完成了。

# 2.3 测试

- (1) 启动Eureka、zul、serviceA
- (2) 当通过服务⽹关zul（端⼝7073) 访问serviceA 的url：http://localhost:7073/api-a/add? a=111&b=113时。⻚⾯会⾃动跳转到github进⾏认证。 你也可以通过zul访问serviceB也会⾃动跳转到github进⾏认证之后才能回调到serviceB。

- (3) 当输⼊github的⽤户名和密码认证通过之后，会出现serviceA的调⽤结果。如下图所示


- (4) 当你认证通过之后输⼊http://localhost:7073/user 可以看到你github 的⽤户信息。


htps:/segmentfault.com/a/19 01098539

