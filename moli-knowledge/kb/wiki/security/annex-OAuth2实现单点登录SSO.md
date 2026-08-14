---
title: OAuth2实现单点登录SSO.note（原文插图 annex）
slug: annex-OAuth2实现单点登录SSO
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/安全框架/开源项目/OAuth2实现单点登录SSO.note.md
related: [shiro-鉴权体系]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.cnblogs.com/cjsblog/p/1054802.html

# 1. 前⾔

技术这东⻄吧，看别⼈写的好像很简单似的，到⾃⼰去写的时候就各种问题，“⼀看就会，⼀做就错”。⽹上 关于实现 SO的⽂章⼀⼤堆，但是当你真的照着写的时候就会发现根本不是那么回事⼉，简直让⼈抓狂，尤 其是对于我这样的菜⻦。⼏经曲折，终于搞定了，决定记录下来，以便后续查看。先来看⼀下效果

![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

![image 3](assets/imageFile3.png)

# 2. 准备

- 2.1. 单点登录 最常⻅的例⼦是，我们打开淘宝AP，⾸⻚就会有天猫、聚划算等服务的链接，当你点击以后就直接跳过去 了，并没有让你再登录⼀次


![image 4](assets/imageFile4.png)

### 下⾯这个图是我再⽹上找的，我觉得画得⽐较明⽩：

![image 5](assets/imageFile5.png)

可惜有点⼉不清晰，于是我⼜画了个简版的： 重要的是理解：

SSO服务端和SSO客户端直接是通过授权以后发放Token的形式来访问受保护的资源 相对于浏览器来说，业务系统是服务端，相对于SSO服务端来说，业务系统是客户端 浏览器和业务系统之间通过会话正常访问

不是每次浏览器请求都要去SSO服务端去验证，只要浏览器和它所访问的服务端的会话有效它就可以正常 访问

![image 6](assets/imageFile6.png)

- 2.2. OAuth2 推荐以下⼏篇博客 《 》 《 》
- 3. 利⽤OAuth2实现单点登录


OAuth 2.0 Spring Security对OAuth2的⽀持

接下来，只讲跟本例相关的⼀些配置，不讲原理，不讲为什么 众所周知，在OAuth2在有授权服务器、资源服务器、客户端这样⼏个⻆⾊，当我们⽤它来实现 SO的时候是 不需要资源服务器这个⻆⾊的，有授权服务器和客户端就够了。 授权服务器当然是⽤来做认证的，客户端就是各个应⽤系统，我们只需要登录成功后拿到⽤户信息以及⽤户 所拥有的权限即可 之前我⼀直认为把那些需要权限控制的资源放到资源服务器⾥保护起来就可以实现权限控制，其实是我想错 了，权限控制还得通过Spring Security或者⾃定义拦截器来做

- 3.1. Spring Security 、OAuth2、JWT、 SO 在本例中，⼀定要分清楚这⼏个的作⽤ ⾸先， SO是⼀种思想，或者说是⼀种解决⽅案，是抽象的，我们要做的就是按照它的这种思想去实现它 其次，OAuth2是⽤来允许⽤户授权第三⽅应⽤访问他在另⼀个服务器上的资源的⼀种协议，它不是⽤来做单 点登录的，但我们可以利⽤它来实现单点登录。在本例实现 SO的过程中，受保护的资源就是⽤户的信息 （包括，⽤户的基本信息，以及⽤户所具有的权限），⽽我们想要访问这这⼀资源就需要⽤户登录并授权， OAuth2服务端负责令牌的发放等操作，这令牌的⽣成我们采⽤JWT，也就是说JWT是⽤来承载⽤户的 Aces_Token的 最后，Spring Security是⽤于安全访问的，这⾥我们我们⽤来做访问权限控制


# 4. 认证服务器配置

- 4.1. Maven依赖


<table>
  <tr>
    <th>![image 7](assets/imageFile7.png)</th>
  </tr>
</table>


<?xml version="1.0" encoding="UTF-8"?> <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchemainstance"

xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-

- 4.0.0.xsd"> <modelVersion>4.0.0</modelVersion> <parent>


<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-parent</artifactId> <version>2.1.3.RELEASE</version> <relativePath/> <!-- lookup parent from repository -->

</parent> <groupId>com.cjs.sso</groupId> <artifactId>oauth2-sso-auth-server</artifactId> <version>0.0.1-SNAPSHOT</version> <name>oauth2-sso-auth-server</name>

<properties>

<java.version>1.8</java.version> </properties>

<dependencies>

<dependency> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-data-jpa</artifactId>

</dependency> <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-data-redis</artifactId>

</dependency> <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-security</artifactId>

</dependency> <dependency>

<groupId>org.springframework.security.oauth.boot</groupId> <artifactId>spring-security-oauth2-autoconfigure</artifactId> <version>2.1.3.RELEASE</version>

</dependency> <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-thymeleaf</artifactId>

</dependency> <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-web</artifactId>

</dependency> <dependency>

<groupId>org.springframework.session</groupId>

<artifactId>spring-session-data-redis</artifactId> </dependency> <dependency>

<groupId>mysql</groupId> <artifactId>mysql-connector-java</artifactId> <scope>runtime</scope>

</dependency> <dependency>

<groupId>org.projectlombok</groupId> <artifactId>lombok</artifactId> <optional>true</optional>

</dependency> <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-test</artifactId> <scope>test</scope>

</dependency> <dependency>

<groupId>org.springframework.security</groupId> <artifactId>spring-security-test</artifactId> <scope>test</scope>

</dependency>

<dependency> <groupId>org.apache.commons</groupId> <artifactId>commons-lang3</artifactId> <version>3.8.1</version>

</dependency> <dependency>

<groupId>com.alibaba</groupId> <artifactId>fastjson</artifactId> <version>1.2.56</version>

</dependency>

</dependencies>

<build> <plugins>

<plugin> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-maven-plugin</artifactId>

</plugin> </plugins>

</build>

</project>

<table>
  <tr>
    <th>![image 8](assets/imageFile8.png)</th>
  </tr>
</table>


这⾥⾯最重要的依赖是：spring-security-oauth2-autoconfigure

## 4.2. aplication.yml

<table>
  <tr>
    <th>![image 9](assets/imageFile9.png)</th>
  </tr>
</table>


spring:

datasource: url: jdbc:mysql://localhost:3306/permission username: root password: 123456 driver-class-name: com.mysql.jdbc.Driver

jpa:

show-sql: true session:

store-type: redis

redis: host: 127.0.0.1 password: 123456 port: 6379

server: port: 8080

<table>
  <tr>
    <th>![image 10](assets/imageFile10.png)</th>
  </tr>
</table>


## 4.3. AuthorizationServerConfig（重要）

<table>
  <tr>
    <th>![image 11](assets/imageFile11.png)</th>
  </tr>
</table>


package com.cjs.sso.config;

import org.springframework.beans.factory.annotation.Autowired; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.context.annotation.Primary; import org.springframework.security.core.token.DefaultToken; import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer; import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigur erAdapter; import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer; import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsC onfigurer; import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityCo nfigurer; import org.springframework.security.oauth2.provider.token.DefaultTokenServices; import org.springframework.security.oauth2.provider.token.TokenStore; import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter; import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.sql.DataSource;

/**

- * @author ChengJianSheng

- * @date 2019-02-11

- */


@Configuration @EnableAuthorizationServer public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

@Autowired private DataSource dataSource;

@Override public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {

security.allowFormAuthenticationForClients(); security.tokenKeyAccess("isAuthenticated()");

}

@Override public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

clients.jdbc(dataSource); }

@Override public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

endpoints.accessTokenConverter(jwtAccessTokenConverter()); endpoints.tokenStore(jwtTokenStore());

// endpoints.tokenServices(defaultTokenServices()); }

/*@Primary @Bean public DefaultTokenServices defaultTokenServices() {

DefaultTokenServices defaultTokenServices = new DefaultTokenServices(); defaultTokenServices.setTokenStore(jwtTokenStore()); defaultTokenServices.setSupportRefreshToken(true); return defaultTokenServices;

}*/

@Bean public JwtTokenStore jwtTokenStore() {

return new JwtTokenStore(jwtAccessTokenConverter()); }

@Bean public JwtAccessTokenConverter jwtAccessTokenConverter() {

JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter(); jwtAccessTokenConverter.setSigningKey("cjs"); // Sets the JWT signing key return jwtAccessTokenConverter;

}

}

<table>
  <tr>
    <th>![image 12](assets/imageFile12.png)</th>
  </tr>
</table>


说明： 别忘了@EnableAuthorizationServer Token存储采⽤的是JWT 客户端以及登录⽤户这些配置存储在数据库，为了减少数据库的查询次数，可以从数据库读出来以后再 放到内存中

- 1.
- 2.
- 3.


- 4.4. WebSecurityConfig（重要）


<table>
  <tr>
    <th>![image 13](assets/imageFile13.png)</th>
  </tr>
</table>


package com.cjs.sso.config;

import com.cjs.sso.service.MyUserDetailsService; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder ; import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.config.annotation.web.builders.WebSecurity; import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder;

/**

- * @author ChengJianSheng

- * @date 2019-02-11

- */


@Configuration @EnableWebSecurity public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

@Autowired private MyUserDetailsService userDetailsService;

@Override protected void configure(AuthenticationManagerBuilder auth) throws Exception {

auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder()); }

@Override public void configure(WebSecurity web) throws Exception {

web.ignoring().antMatchers("/assets/**", "/css/**", "/images/**"); }

@Override protected void configure(HttpSecurity http) throws Exception {

http.formLogin()

.loginPage("/login")

.and()

.authorizeRequests()

.antMatchers("/login").permitAll()

.anyRequest()

.authenticated()

.and().csrf().disable().cors(); }

@Bean public PasswordEncoder passwordEncoder() {

return new BCryptPasswordEncoder(); }

}

<table>
  <tr>
    <th>![image 14](assets/imageFile14.png)</th>
  </tr>
</table>


## 4.5. ⾃定义登录⻚⾯（⼀般来讲都是要⾃定义的）

<table>
  <tr>
    <th>![image 15](assets/imageFile15.png)</th>
  </tr>
</table>


package com.cjs.sso.controller;

import org.springframework.stereotype.Controller; import org.springframework.web.bind.annotation.GetMapping;

/**

- * @author ChengJianSheng

- * @date 2019-02-12

- */


@Controller public class LoginController {

@GetMapping("/login") public String login() { return "login"; }

@GetMapping("/") public String index() { return "index"; }

}

<table>
  <tr>
    <th>![image 16](assets/imageFile16.png)</th>
  </tr>
</table>


### ⾃定义登录⻚⾯的时候，只需要准备⼀个登录⻚⾯，然后写个Controler令其可以访问到即可，登录⻚⾯表单 提交的时候method⼀定要是post，最重要的时候action要跟访问登录⻚⾯的url⼀样 千万记住了，访问登录⻚⾯的时候是GET请求，表单提交的时候是POST请求，其它的就不⽤管了

<table>
  <tr>
    <th>![image 17](assets/imageFile17.png)</th>
  </tr>
</table>


<!DOCTYPE html> <html xmlns:th="http://www.thymeleaf.org"> <head>

<meta charset="utf-8"> <meta http-equiv="X-UA-Compatible" content="IE=edge"> <title>Ela Admin - HTML5 Admin Template</title> <meta name="description" content="Ela Admin - HTML5 Admin Template"> <meta name="viewport" content="width=device-width, initial-scale=1">

<link type="text/css" rel="stylesheet" th:href="@{/assets/css/normalize.css}"> <link type="text/css" rel="stylesheet" th:href="@{/assets/bootstrap-4.3.1-

dist/css/bootstrap.min.css}"> <link type="text/css" rel="stylesheet" th:href="@{/assets/css/font-awesome.min.css}"> <link type="text/css" rel="stylesheet" th:href="@{/assets/css/style.css}">

</head> <body class="bg-dark">

<div class="sufee-login d-flex align-content-center flex-wrap"> <div class="container"> <div class="login-content"> <div class="login-logo">

<h1 style="color: #57bf95;">欢迎来到王者荣耀</h1> </div> <div class="login-form">

<form th:action="@{/login}" method="post">

<div class="form-group"> <label>Username</label> <input type="text" class="form-control" name="username"

placeholder="Username"> </div> <div class="form-group">

<label>Password</label> <input type="password" class="form-control" name="password"

placeholder="Password"> </div> <div class="checkbox">

<label>

<input type="checkbox"> Remember Me </label> <label class="pull-right">

<a href="#">Forgotten Password?</a>

</label> </div> <button type="submit" class="btn btn-success btn-flat m-b-30 m-t-30"

style="font-size: 18px;">登录</button>

</form> </div>

</div>

</div> </div>

<script type="text/javascript" th:src="@{/assets/js/jquery-2.1.4.min.js}"></script> <script type="text/javascript" th:src="@{/assets/bootstrap-4.3.1-dist/js/bootstrap.min.js}"> </script> <script type="text/javascript" th:src="@{/assets/js/main.js}"></script>

</body> </html>

<table>
  <tr>
    <th>![image 18](assets/imageFile18.png)</th>
  </tr>
</table>


- 4.6. 定义客户端
- 4.7. 加载⽤户 登录账户


![image 19](assets/imageFile19.png)

![image 20](assets/imageFile20.png)

<table>
  <tr>
    <th>![image 21](assets/imageFile21.png)</th>
  </tr>
</table>


package com.cjs.sso.domain;

import lombok.Data; import org.springframework.security.core.GrantedAuthority; import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**

- * ⼤部分时候直接⽤User即可不必扩展

- * @author ChengJianSheng

- * @date 2019-02-11

- */


@Data public class MyUser extends User {

private Integer departmentId; // 举个例⼦，部⻔ID

private String mobile; // 举个例⼦，假设我们想增加⼀个字段，这⾥我们增加⼀个mobile表示⼿机号

public MyUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {

super(username, password, authorities); }

public MyUser(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {

super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);

} }

<table>
  <tr>
    <th>![image 22](assets/imageFile22.png)</th>
  </tr>
</table>


### 加载登录账户

<table>
  <tr>
    <th>![image 23](assets/imageFile23.png)</th>
  </tr>
</table>


package com.cjs.sso.service;

import com.alibaba.fastjson.JSON; import com.cjs.sso.domain.MyUser; import com.cjs.sso.entity.SysPermission; import com.cjs.sso.entity.SysUser; import lombok.extern.slf4j.Slf4j; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.security.core.authority.SimpleGrantedAuthority; import org.springframework.security.core.userdetails.UserDetails; import org.springframework.security.core.userdetails.UserDetailsService; import org.springframework.security.core.userdetails.UsernameNotFoundException; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.util.CollectionUtils;

import java.util.ArrayList; import java.util.List;

/**

- * @author ChengJianSheng

- * @date 2019-02-11

- */


@Slf4j @Service public class MyUserDetailsService implements UserDetailsService {

@Autowired private PasswordEncoder passwordEncoder;

@Autowired private UserService userService;

@Autowired private PermissionService permissionService;

@Override public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

SysUser sysUser = userService.getByUsername(username); if (null == sysUser) {

log.warn("⽤户{}不存在", username); throw new UsernameNotFoundException(username);

} List<SysPermission> permissionList = permissionService.findByUserId(sysUser.getId()); List<SimpleGrantedAuthority> authorityList = new ArrayList<>(); if (!CollectionUtils.isEmpty(permissionList)) {

for (SysPermission sysPermission : permissionList) {

authorityList.add(new SimpleGrantedAuthority(sysPermission.getCode())); }

}

MyUser myUser = new MyUser(sysUser.getUsername(), passwordEncoder.encode(sysUser.getPassword()), authorityList);

log.info("登录成功！⽤户: {}", JSON.toJSONString(myUser));

return myUser; }

}

<table>
  <tr>
    <th>![image 24](assets/imageFile24.png)</th>
  </tr>
</table>


## 4.8. 验证

![image 25](assets/imageFile25.png)

当我们看到这个界⾯的时候，表示认证服务器配置完成

# 5. 两个客户端

- 5.1. Maven依赖


<table>
  <tr>
    <th>![image 26](assets/imageFile26.png)</th>
  </tr>
</table>


<?xml version="1.0" encoding="UTF-8"?> <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchemainstance"

xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-

- 4.0.0.xsd"> <modelVersion>4.0.0</modelVersion> <parent>


<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-parent</artifactId> <version>2.1.3.RELEASE</version> <relativePath/> <!-- lookup parent from repository -->

</parent> <groupId>com.cjs.sso</groupId> <artifactId>oauth2-sso-client-member</artifactId> <version>0.0.1-SNAPSHOT</version> <name>oauth2-sso-client-member</name> <description>Demo project for Spring Boot</description>

<properties>

<java.version>1.8</java.version> </properties>

<dependencies>

<dependency> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-data-jpa</artifactId>

</dependency> <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-oauth2-client</artifactId>

</dependency> <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-security</artifactId>

</dependency> <dependency>

<groupId>org.springframework.security.oauth.boot</groupId> <artifactId>spring-security-oauth2-autoconfigure</artifactId> <version>2.1.3.RELEASE</version>

</dependency> <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-thymeleaf</artifactId>

</dependency> <dependency>

<groupId>org.thymeleaf.extras</groupId> <artifactId>thymeleaf-extras-springsecurity5</artifactId> <version>3.0.4.RELEASE</version>

</dependency>

<dependency> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-web</artifactId>

</dependency>

<dependency> <groupId>com.h2database</groupId> <artifactId>h2</artifactId> <scope>runtime</scope>

</dependency> <dependency>

<groupId>org.projectlombok</groupId> <artifactId>lombok</artifactId> <optional>true</optional>

</dependency> <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-test</artifactId> <scope>test</scope>

</dependency> <dependency>

<groupId>org.springframework.security</groupId> <artifactId>spring-security-test</artifactId> <scope>test</scope>

</dependency> </dependencies>

<build> <plugins>

<plugin> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-maven-plugin</artifactId>

</plugin> </plugins>

</build>

</project>

<table>
  <tr>
    <th>![image 27](assets/imageFile27.png)</th>
  </tr>
</table>


## 5.2. aplication.yml

<table>
  <tr>
    <th>![image 28](assets/imageFile28.png)</th>
  </tr>
</table>


server: port: 8082 servlet:

context-path: /memberSystem security:

oauth2:

client: client-id: UserManagement client-secret: user123 access-token-uri: http://localhost:8080/oauth/token user-authorization-uri: http://localhost:8080/oauth/authorize

resource: jwt: key-uri: http://localhost:8080/oauth/token_key

<table>
  <tr>
    <th>![image 29](assets/imageFile29.png)</th>
  </tr>
</table>


![image 30](assets/imageFile30.png)

这⾥context-path不要设成/，不然重定向获取code的时候回被拦截

## 5.3. WebSecurityConfig

<table>
  <tr>
    <th>![image 31](assets/imageFile31.png)</th>
  </tr>
</table>


package com.cjs.example.config;

import com.cjs.example.util.EnvironmentUtils; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso; import org.springframework.context.annotation.Configuration; import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.config.annotation.web.builders.WebSecurity; import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**

- * @author ChengJianSheng

- * @date 2019-03-03

- */


@EnableOAuth2Sso @Configuration public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

@Autowired private EnvironmentUtils environmentUtils;

@Override public void configure(WebSecurity web) throws Exception {

web.ignoring().antMatchers("/bootstrap/**"); }

@Override protected void configure(HttpSecurity http) throws Exception {

if ("local".equals(environmentUtils.getActiveProfile())) { http.authorizeRequests().anyRequest().permitAll(); }else {

http.logout().logoutSuccessUrl("http://localhost:8080/logout")

.and()

.authorizeRequests()

.anyRequest().authenticated()

.and()

.csrf().disable(); }

} }

<table>
  <tr>
    <th>![image 32](assets/imageFile32.png)</th>
  </tr>
</table>


说明： 最重要的注解是@EnableOAuth2Sso 权限控制这⾥采⽤Spring Security⽅法级别的访问控制，结合Thymeleaf可以很容易做权限控制 顺便多提⼀句，如果是前后端分离的话，前端需求加载⽤户的权限，然后判断应该显示那些按钮那些菜 单

- 1.
- 2.
- 3.


- 5.4. MemberControler


<table>
  <tr>
    <th>![image 33](assets/imageFile33.png)</th>
  </tr>
</table>


package com.cjs.example.controller;

import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.Authentication; import org.springframework.stereotype.Controller; import org.springframework.web.bind.annotation.GetMapping; import org.springframework.web.bind.annotation.PostMapping; import org.springframework.web.bind.annotation.RequestMapping; import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

/**

- * @author ChengJianSheng

- * @date 2019-03-03

- */


@Controller @RequestMapping("/member") public class MemberController {

@GetMapping("/list") public String list() {

return "member/list"; }

@GetMapping("/info") @ResponseBody public Principal info(Principal principal) {

return principal; }

@GetMapping("/me") @ResponseBody public Authentication me(Authentication authentication) {

return authentication; }

@PreAuthorize("hasAuthority('member:save')") @ResponseBody @PostMapping("/add") public String add() {

return "add"; }

@PreAuthorize("hasAuthority('member:detail')") @ResponseBody @GetMapping("/detail")

public String detail() { return "detail"; }

}

<table>
  <tr>
    <th>![image 34](assets/imageFile34.png)</th>
  </tr>
</table>


## 5.5. Order项⽬跟它是⼀样的

<table>
  <tr>
    <th>![image 35](assets/imageFile35.png)</th>
  </tr>
</table>


server: port: 8083 servlet:

context-path: /orderSystem security:

oauth2:

client: client-id: OrderManagement client-secret: order123 access-token-uri: http://localhost:8080/oauth/token user-authorization-uri: http://localhost:8080/oauth/authorize

resource: jwt: key-uri: http://localhost:8080/oauth/token_key

<table>
  <tr>
    <th>![image 36](assets/imageFile36.png)</th>
  </tr>
</table>


- 5.6. 关于退出 退出就是清空⽤于与 SO客户端建⽴的所有的会话，简单的来说就是使所有端点的Sesion失效，如果想做得 更好的话可以令Token失效，但是由于我们⽤的JWT，故⽽撤销Token就不是那么容易，关于这⼀点，在官⽹ 上也有提到：


![image 37](assets/imageFile37.png)

本例中采⽤的⽅式是在退出的时候先退出业务服务器，成功以后再回调认证服务器，但是这样有⼀个问题， 就是需要主动依次调⽤各个业务服务器的logout

# 6. ⼯程结构

![image 38](assets/imageFile38.png)

附上源码： htps:/github.com/chengjiansheng/cjs-oauth2-so-demo.git

# 7. 演示

![image 39](assets/imageFile39.png)

- 8. 参考
- 9. ⽂档


htps:/ w.cnblogs.com/cjsblog/p/9174797.html htps:/ w.cnblogs.com/cjsblog/p/9184173.html htps:/ w.cnblogs.com/cjsblog/p/923090.html htps:/ w.cnblogs.com/cjsblog/p/92767.html htps:/blog.csdn.net/foeliot/article/details/83617941 htp:/blog.leapoahead.com/2015/09/07/user-authentication-with-jwt/ htps:/ w.cnblogs.com/lihaoyang/p/858107.html htps:/ w.cnblogs.com/charlypage/p/9383420.html htp:/ w.360doc.com/content/18/0306/17/16915_734789216.shtml htps:/blog.csdn.net/chenjianandiyi/article/details/78604376 htps:/ w.baeldung.com/spring-security-oauth-jwt htps:/ w.baeldung.com/spring-security-oauth-revoke-tokens htps:/ w.reinforce.cn/t/630.html

htps:/projects.spring.io/spring-security-oauth/docs/oauth2.html htps:/docs.spring.io/spring-security-oauth2-bot/docs/2.1.3.RELEASE/reference/htmlsingle/

htps:/docs.spring.io/spring-security-oauth2-bot/docs/2.1.3.RELEASE/ htps:/docs.spring.io/spring-security-oauth2-bot/docs/ htps:/docs.spring.io/spring-bot/docs/2.1.3.RELEASE/ htps:/docs.spring.io/spring-bot/docs/ htps:/docs.spring.io/spring-framework/docs/ htps:/docs.spring.io/spring-framework/docs/5.1.4.RELEASE/

htps:/spring.io/guides/tutorials/spring-bot-oauth2/

htps:/docs.spring.io/spring-security/site/docs/curent/reference/htmlsingle/#core-services-paswordencoding htps:/spring.io/projects/spring-cloud-security htps:/cloud.spring.io/spring-cloud-security/single/spring-cloud-security.html htps:/docs.spring.io/spring-sesion/docs/curent/reference/html5/guides/java-security.html htps:/docs.spring.io/spring-sesion/docs/curent/reference/html5/guides/bot-redis.html#bot-springconfiguration
