---
title: Redis如何通过Spring Session实现分布式Session共享.note（原文插图 annex）
slug: annex-Redis如何通过Spring-Session实现分布式Session共享
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/DataBase/Redis/Redis如何通过Spring Session实现分布式Session共享.note.md
related: [redis-面试题]
created: 2026-07-05
updated: 2026-07-05
---

问题导读

- 1、将同⼀个应⽤部署在多个服务器上通过负载均衡对外提供访问，如何实现Session共享？

- 2、Spring Session的过滤器是如何配置的？

- 3、如何解决Redis云服务Unable to configure Redis to keyspace notifications异常？

- 4、Redis云服务如何配置管理后台？


![image 1](assets/imageFile1.png)

通常情况下，Tomcat、Jetty等Servlet容器，会默认将Session保存在内存中。如果是单个服务器实例的 应⽤，将Session保存在服务器内存中是⼀个⾮常好的⽅案。但是这种⽅案有⼀个缺点，就是不利于扩 展。 ⽬前越来越多的应⽤采⽤分布式部署，⽤于实现⾼可⽤性和负载均衡等。那么问题来了，如果将同⼀ 个应⽤部署在多个服务器上通过负载均衡对外提供访问，如何实现Session共享？ 实际上实现Session共享的⽅案很多，其中⼀种常⽤的就是使⽤Tomcat、Jetty等服务器提供的Session 共享功能，将Session的内容统⼀存储在⼀个数据库（如MySQL）或缓存（如Redis）中。我在

以前的⼀ 篇博客

中有介绍如何配置Jetty的Session存储在MySQL或MongoDB中。 本⽂主要介绍另⼀种实现Session共享的⽅案，不依赖于Servlet容器，⽽是Web应⽤代码层⾯的实现， 直接在已有项⽬基础上加⼊Spring Session框架来实现Session统⼀存储在Redis中。如果你的Web应⽤ 是基于Spring框架开发的，只需要对现有项⽬进⾏少量配置，即可将⼀个单机版的Web应⽤改为⼀个分 布式应⽤，由于不基于Servlet容器，所以可以随意将项⽬移植到其他容器。

## Maven依赖

在项⽬中加⼊Spring Session的相关依赖包，包括Spring Data Redis、Jedis、Apache Commons Pool：

<!-- Jedis --> <dependency>

<groupId>redis.clients</groupId> <artifactId>jedis</artifactId> <version>2.9.0</version>

</dependency> <!-- Spring Data Redis --> <dependency>

<groupId>org.springframework.data</groupId> <artifactId>spring-data-redis</artifactId> <version>1.7.3.RELEASE</version>

</dependency>

<!-- Spring Session --> <dependency>

<groupId>org.springframework.session</groupId> <artifactId>spring-session</artifactId>

- <version>1.2.2.RELEASE</version>

</dependency> <!-- Apache Commons Pool --> <dependency>

<groupId>org.apache.commons</groupId> <artifactId>commons-pool2</artifactId>

- <version>2.4.2</version>


</dependency>

# 配置Filter

在web.xml中加⼊以下过滤器，注意如果web.xml中有其他过滤器，⼀般情况下Spring Session的过滤器 要放在第⼀位。

<ﬁlter>

<ﬁlter-name>springSessionRepositoryFilter</ﬁlter-name> <ﬁlter-class>org.springframework.web.ﬁlter.DelegatingFilterProxy</ﬁlter-class>

</ﬁlter> <ﬁlter-mapping>

<ﬁlter-name>springSessionRepositoryFilter</ﬁlter-name> <url-pattern>/*</url-pattern>

<dispatcher>REQUEST</dispatcher> <dispatcher>ERROR</dispatcher>

</ﬁlter-mapping>

## Spring配置⽂件

<bean class="org.springframework.session.data.redis.conﬁg.annotation.web.http.RedisHttpSessionConﬁguration"/> <bean class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory">

<property name="hostName" value="localhost" /> <property name="password" value="your-password" /> <property name="port" value="6379" /> <property name="database" value="10" />

</bean>

只需要以上简单的配置，⾄此为⽌即已经完成Web应⽤Session统⼀存储在Redis中，可以说是及其简 单。

# 解决Redis云服务Unable to configure Redis to keyspace notifications异常

如果是⾃建服务器搭建Redis服务，以上已经完成了Spring Session配置，这⼀节就不⽤看了。不过很多 公司为了稳定性、减少运维成本，会选择使⽤Redis云服务，例如阿⾥云数据库Redis版、腾讯云存储 Redis等。使⽤过程中会出现异常：

Context initialization failed org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'enableRedisKeyspaceNotiﬁcationsInitializer' deﬁned in class path resource [org/springframework/session/data/redis/conﬁg/annotation/web/http/RedisHttpSessionConﬁguration.class]: Invocation of init method failed; nested exception is java.lang.IllegalStateException: Unable to conﬁgure Redis to keyspace notiﬁcations. See [url= ] [/url] ... nt/reference/html5/#apiredisoperationssessionrepository-sessiondestroyedevent Caused by: redis.clients.jedis.exceptions.JedisDataException: ERR unknown command conﬁg

http://docs.spring.io/spring-ses http://docs.spring.io/spring-ses

实际上这种异常发⽣的原因是，很多Redis云服务提供商考虑到安全因素，会禁⽤掉Redis的config命 令：

![image 2](assets/imageFile2.png)

禁⽤config命令

在错误提示链接的⽂档中，可以看到Redis需要以下的配置：

[Plain Text] 纯⽂本查看 复制代码

?

<table>
  <tr>
    <th>1</th>
    <th>redis-cli config set notify-keyspace-events Egx</th>
  </tr>
</table>


⽂档地址：

http://docs.spring.io/spring-session/docs/current/reference/html5/#api-redisoperationssessionreposit ory-sessiondestroyedevent

⾸先要想办法给云服务Redis加上这个配置。

部分Redis云服务提供商可以在对应的管理后台配置：

![image 3](assets/imageFile3.png)

### 配置notify-keyspace-events

如果不能在后台配置，可以通过⼯单联系售后⼯程师帮忙配置，例如阿⾥云：

![image 4](assets/imageFile4.png)

阿⾥云⼯单 完成之后，还需要在Spring配置⽂件中加上⼀个配置，让Spring Session不再执⾏config命令：

However, in a secured Redis enviornment the config command is disabled. This means that Spring Session cannot configure Redis Keyspace events for you. To disable the automatic configuration add ConfigureRedisAction.NO_OP as a bean.

配置：

[XML] 纯⽂本查看 复制代码 ?

<table>
  <tr>
    <th>01<br>02<br>03<br>04<br>05<br>06<br>07<br>08<br>09<br>10 1<br><br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br></th>
    <th><?xml version="1.0" encoding="UTF-8"?> <beans xmlns="htp:/ w.springframework.org/schema/beans"<br><br>xmlns:util="htp:/ w.springframework.org/schema/util" xmlns:xsi="htp:/ w.w3.org/201/XMLSchema-instance"<br><br>xsi:schemaLocation="htp:/ w.springframework.org/schem a/beans<br><br>[url=htp:/ w.springframework.org/schema/beans/springbeans.xsd]htp:/ w.springframework.org/schema/beans/sp ring-beans.xsd[/url]<br><br>[url=htp:/ w.springframework.org/schema/util]htp:/ w. springframework.org/schema/util[/url]<br><br>[url=htp:/ w.springframework.org/schema/util/springutil.xsd]htp:/ w.springframework.org/schema/util/springutil.xsd[/url]"><br><br><bean clas="org.springframework.sesion.data.redis.config.anota tion.web.htp.RedisHtpSesionConfiguration"/><br><br><bean clas="org.springframework.data.redis.conection.jedis.Jedi sConectionFactory"><br><br><property name="hostName" value="localhost" /> <property name="pasword" value="your-pasword" /> <property name="port" value="6379" /> <property name="database" value="10" /> </bean><br><br><!- 让Spring Sesion不再执⾏config命令 -> <util:constant static-<br><br>field="org.springframework.sesion.data.redis.config.Configu reRedisAction.NO_OP"/></th>
  </tr>
</table>


</beans>

作者：叉叉哥的BLOG 来源：xxgblog
