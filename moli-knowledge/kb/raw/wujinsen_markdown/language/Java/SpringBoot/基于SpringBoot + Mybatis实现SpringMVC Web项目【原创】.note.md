# http://7player.cn/2015/08/30/%E3%80%90%E5%8E%9F%E5%88%9 B%E3%80%91%E5%9F%BA%E4%BA%8Espringboot-mybatis%E5% AE%9E%E7%8E%B0springmvc-web%E9%A1%B9%E7%9B%AE/

⼀、热身 ⼀个现实的场景是：当我们开发⼀个Web⼯程时，架构师和开发⼯程师可能更关⼼项⽬技术结构上的 设计。⽽⼏乎所有结构良好的软件（项⽬）都使⽤了分层设计。分层设计是将项⽬按技术职能分为⼏ 个内聚的部分，从⽽将技术或接⼝的实现细节隐藏起来。

![image 1](<基于SpringBoot + Mybatis实现SpringMVC Web项目【原创】.note_images/imageFile1.png>)

从另⼀个⻆度上来看，结构上的分层往往也能促进了技术⼈员的分⼯，可以使开发⼈员更专注于某⼀ 层业务与功能的实现，⽐如前端⼯程师只关⼼⻚⾯的展示与交互效果（例如专注于HTML，JS等），⽽ 后端⼯程师只关⼼数据和业务逻辑的处理（专注于Java，Mysql等）。两者之间通过标准接⼝（协议） 进⾏沟通。 在实现分层的过程中我们会使⽤⼀些框架，例如SpringMVC。但利⽤框架带来了⼀些使⽤⽅⾯的问 题。我们经常要做的⼯作就是配置各种XML⽂件，然后还需要搭建配置Tomcat或者Jety作为容器来运 ⾏这个⼯程。每次构建⼀个新项⽬，都要经历这个流程。更为不幸的是有时候前端⼈员为了能在本地 调试或测试程序，也需要先配置这些环境，或者需要后端⼈员先实现⼀些服务功能。这就和刚才提到 的“良好的分层结构”相冲突。 每⼀种技术和框架都有⼀定的学习曲线。开发⼈员需要了解具体细节，才知道如何把项⽬整合成⼀个 完整的解决⽅案。事实上，⼀个整合良好的项⽬框架不仅仅能实现技术、业务的分离，还应该关注并 满⾜开发⼈员的“隔离”。 为了解决此类问题，便产⽣了Spring Bot这⼀全新框架。Spring Bot就是⽤来简化Spring应⽤的搭建 以及开发过程。该框架致⼒于实现免XML配置，提供便捷，独⽴的运⾏环境，实现“⼀键运⾏”满⾜快 速应⽤开发的需求。

与此同时，⼀个完整的Web应⽤难免少不了数据库的⽀持。利⽤JDBC的API需要编写复杂重复且冗余 的代码。⽽使⽤O/RM（例如Hibernate）⼯具需要基于⼀些假设和规则，例如最普遍的⼀个假设就是 数据库被恰当的规范了。这些规范在现实项⽬中并⾮能完美实现。由此，诞⽣了⼀种混合型解决⽅案 ⸺Mybatis。Mybatis是⼀个持久层框架，它从各种数据库访问⼯具中汲取⼤量优秀的思想，适⽤于 任何⼤⼩和⽤途的数据库。根据官⽅⽂档的描述：MyBatis 是⽀持定制化 SQL、存储过程以及⾼级映 射的优秀的持久层框架。MyBatis 避免了⼏乎所有的 JDBC 代码和⼿动设置参数以及获取结果集。 MyBatis 可 以 对 配 置 和 原 ⽣ Map使 ⽤ 简 单 的 XML 或 注 解 ， 将 接 ⼝ 和 Java 的 POJOs(Plain Old Java Objects，普通的 Java对象)映射成数据库中的记录。 最后，再回到技术结构分层上，⽬前主流倡导的设计模式为MVC，即模型(model)－视图(view)－控制 器(controler)。实现该设计模式的框架有很多，例如Struts。⽽前⽂提到的SpringMVC是另⼀个更为 优秀，灵活易⽤的MVC框架。 SpringMVC是⼀种基于Java的以请求为驱动类型的轻量级Web框架，其 ⽬的是将Web层进⾏解耦，即使⽤“请求-响应”模型，从⼯程结构上实现良好的分层，区分职责，简化 Web开发。 ⽬前，对于如何把这些技术整合起来形成⼀个完整的解决⽅案，并没有相关的最佳实践。将 SpringBot和Mybatis两者整合使⽤的资料和案例较少。因此，本⽂提供（介绍）⼀个完整利⽤ SpringBot和Mybatis来构架Web项⽬的案例。该案例基于SpringMVC架构提供完整且简洁的实现 Demo，便于开发⼈员根据不同需求和业务进⾏拓展。 补充提示，Spring Bot 推荐采⽤基于 Java注解的配置⽅式，⽽不是传统的 XML。只需要在主配 置 Java 类上添加“@EnableAutoConfiguration”注解就可以启⽤⾃动配置。Spring Bot 的⾃动配置功 能是没有侵⼊性的，只是作为⼀种基本的默认实现。开发⼈员可以通过定义其他 bean 来替代⾃动配置 所提供的功能，例如在配置本案例数据源（DataSource）时，可以体会到该⽤法。

⼆、实践 ⼀些说明： 项⽬IDE采⽤Intelij（主要原因在于Intelij颜值完爆Eclipse，谁叫这是⼀个看脸的时代） ⼯程依赖管理采⽤个⼈⽐较熟悉的Maven（事实上SpringBot与Grovy才是天⽣⼀对）

- 1.预览：


- （1）github地址

git :

- （2）完整项⽬结构


htps:/github.com/djmpink/springbot-mybatis htps:/github.com/djmpink/springbot-mybatis.git

![image 2](<基于SpringBoot + Mybatis实现SpringMVC Web项目【原创】.note_images/imageFile2.png>)

## （3）数据库 数据库名：test 【user.sql】

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br><br><br>16</th>
    <th>SET FOREIGN_KEY_CHECKS=0;<br><br>-<br><br>-Table structure foruser<br><br>DOP TABLE IFEXISTS`user`; CREATE TABLE`user`( `id`int(1)NOTNUL, `name`varchar(25)DEFAULTNUL, `age`int(1)DEFAULTNUL, `pasword`varchar(25)DEFAULTNUL, PRIMARY KEY(`id`)<br><br>)ENGINE=I noDB DEFAULTCHARSET=latin1;<br><br>-<br><br>-Records of user<br><br>SERT</th>
  </tr>
</table>


INTO`user`VALUES('1','7player','18','123456');

- 2.Maven配置 完整的【pom.xml】配置如下：


- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9


?xml version="1.0"encoding="UTF-8"?> <project xmlns="htp:/maven.apache.org/POM/4.0.0" xmlns:xsi=htp:/ w.w3.org/201/XMLSchem a-instance" xsi:schemaLocation="htp:/maven.apache.org/ POM/4.0.0 htp:/maven.apache.org/xsd/maven4.0.0.xsd">

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


<modelVersion>4.0.0</modelVersion> <groupId>cn.7player.framework</groupId> <artifactId>springbot-mybatis</artifactId> <version>1.0-SNAPSHOT</version> <parent>

<groupId>org.springframework.bot</grou pId>

<artifactId>spring-bot-starterparent</artifactId>

19

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


<version>1.2.5.RELEASE</version> /parent>

<properties>

<project.build.sourceEncoding>UTF8</project.build.sourceEncoding>

<java.version>1.7</java.version> /properties>

<dependecies> <!-Spring Bot->

29

<!-⽀持Web应⽤开发，包含Tomcat和 spring-mvc。 ->

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


<dependency>

<groupId>org.springframework.bot</gr oupId>

<artifactId>spring-bot-starterweb</artifactId>

</dependency> <!-模板引擎 -> <dependency>

39

<groupId>org.springframework.bot</gr oupId>

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


<artifactId>spring-bot-starter-

thymeleaf</artifactId> </dependency> <!-⽀持使⽤JDBC访问数据库 -> <dependency>

<groupId>org.springframework.bot</gr oupId>

<artifactId>spring-bot-starter-

49

jdbc</artifactId> </dependency> <!-添加适⽤于⽣产环境的功能，如性能指

- 0
- 1
- 2
- 3
- 4
- 5


标和监测等功能。 -> <dependency>

<groupId>org.springframework.bot</gr oupId>

56

- 7
- 8


<artifactId>spring-bot-starter-

actuator</artifactId> /dependency> !-Mybatis<dependency> <groupId>org.mybatis</groupId> <artifactId>mybatis-spring</artifactId> <version>1.2.2</version>

59

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


/dependency>

<dependency> <groupId>org.mybatis</groupId> <artifactId>mybatis</artifactId> <version>3.2.8</version>

69

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


/dependency> !-Mysql/DataSource->

<dependency> <groupId>org.apache.tomcat</groupId> <artifactId>tomcat-jdbc</artifactId>

/dependency>

<dependency> <groupId>mysql</groupId> <artifactId>mysql-conector-

79

java</artifactId> /dependency> !-Json Suport->

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


<dependency> <groupId>com.alibaba</groupId> <artifactId>fastjson</artifactId> <version>1.1.43</version>

/dependency> !-Swager suport->

<dependency> <groupId>com.mangofactory</groupId> <artifactId>swager-

89

- 0
- 1
- 2


springmc</artifactId>

<version>0.9.5</version> </dependency>

93

/dependencies> <build>

<plugins> <plugin>

<groupId>org.springframework.bot</ groupId>

<artifactId>spring-bot-mavenplugin</artifactId>

</plugin> </plugins>

/build> <repositories>

<repository> <id>spring-milestone</id> <url>htps:/repo.spring.io/libs-

release</url>

</repository> /repositorie>

<pluginRepositories> <pluginRepository> <id>spring-milestone</id>

<url>htps:/repo.spring.io/libsrelease</url>

</pluginRepository> </pluginRepositories>

</project>

- 3.主函数 【Aplication.java】包含main函数，像普通java程序启动即可。 此外，该类中还包含和数据库相关的DataSource，SqlSesion配置内容。 注：@MaperScan(“cn.no7player.maper”)表示Mybatis的映射路径（package路径）


1 2 3 4 5 6 7 8 9

packagecn.no7player; import org.apache.ibatis.sesion.SqlSesionFactory;

por org.apache.log4j.Loger; import org.mybatis.spring.SqlSesionFactoryBean; import org.mybatis.spring.anotation.MaperScan; import org.springframework.bot.SpringAplication; import org.springframework.bot.autoconfigure.Enable AutoConfiguration; import org.springframework.bot.autoconfigure.Spring BotAplication; import org.springframework.bot.context.properties.C onfigurationProperties; import org.springframework.context.anotation.Bean; import org.springframework.context.anotation.Compo nentScan; import org.springframework.core.io.suport.PathMatc hingResourcePaternResolver; import org.springframework.jdbc.datasource.DataSour ceTransactionManager; import org.springframework.transaction.PlatformTrans actionManager;

0 1 2 3 4 5 6 7 8

19 0 1 2 3 4 5 6 7 8

29 0 1 2 3 4 5 6 7 8

import javax.sql.DataSource;

EnableAutoConfiguration SpringBotAplication ComponentScan

39 0 1 2 3 4 5 6 7 8

@MaperScan("cn.no7player.maper") publi clasAplication{ privatestaticLoger

loger=Loger.getLoger(Aplication.clas); /DataSource配置 Bean

@ConfigurationProperties(prefix="spring.dat asorce")

49 0 1 2 3 4 5

publicDataSourcedataSource(){

returneworg.apache.tomcat.jdbc.pol.Dat aSource();

}

/提供SqlSesion @Bean

56

- 7
- 8


publicSqlSesionFactory sqlSesionFactoryBean()throwsException{

- 59
- 60


SqlSesionFactoryBean sqlSesionFactoryBean=newSqlSesionFactory Bean();

sqlSesionFactoryBean.setDataSource(dat aSource();

PathMatchingResourcePaternResolver resolver=newPathMatchingResourcePaternRes olver();

sqlSesionFactoryBean.setMaperLocation s(resolver.getResources("claspath:/mybatis/*.x ml");

returnsqlSesionFactoryBean.getObject();

} @Bean publicPlatformTransactionManager

transactionManager(){ returnewDataSourceTransactionManager(

dataSource(); } /*

Main Start

*/ publicstaticvoidmain(String[]args){

SpringAplication.run(Aplication.clas,arg s);

loger.info(" = SpringBot Start Suces =");

} }

- 4.Controler 请求⼊⼝Controler部分提供三种接⼝样例：视图模板，Json，restful⻛格


- （1）视图模板 返回结果为视图⽂件路径。视图相关⽂件默认放置在路径 resource/templates下：


<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br><br><br>25</th>
    <th>packagecn.no7player.controler;<br><br>por org.apache.log4j.Loger; import org.springframework.stereotype.Controler; impor org.springframework.ui.Model; import org.springframework.web.bind.anotation.Requ estMaping; import org.springframework.web.bind.anotation.Requ estParam;<br><br>@Controler publi clasHeloControler{<br><br>privateLoger loger=Loger.getLoger(HeloControler.clas) ;<br><br>/*<br><br>* htp:/localhost:8080/helo? name=cn.7player<br>*/<br><br><br>@RequestMaping("/helo") publicStringreting(@RequestParam(value=<br><br>"name",required=false,defaultValue="World")St ringname,Model model){<br><br>logerinfo("helo"); model.adAtribute("name",name); return"helo";<br><br>}</th>
  </tr>
</table>


}

- （2）Json 返回Json格式数据，多⽤于Ajax请求。


<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>29<br><br>0<br>1<br>2<br>3<br></th>
    <th>packagecn.no7player.controler; mpor n.nopl er.model.User; i por cn.no7player.service.UserService;<br><br>por org.apache.log4j.Loger; import org.springframework.beans.factory.anotation. Autowired; import org.springframework.stereotype.Controler; import org.springframework.web.bind.anotation.Requ estMaping; import org.springframework.web.bind.anotation.Resp onseBody;<br><br>@Controler publi clasUserControler{<br><br>privateLoger loger=Loger.getLoger(UserControler.clas) ;<br><br>@Autowired privateUserService userService;<br><br>/<br><br>htp:/localhost:8080/getUserInfo<br><br>*/<br><br>equestMaping("/getUserInfo") @ResponseBody publicUser getUserInfo(){<br><br>User user=userService.getUserInfo(); if(user!=nul){<br><br>System.out.println("user.getName():"+us er.getName();<br><br>loger.info("user.getAge():"+user.getAge ();<br><br>} returnuser;<br><br>}</th>
  </tr>
</table>


}

- （3）restful REST 指的是⼀组架构约束条件和原则。满⾜这些约束条件和原则的应⽤程序或设计就是 RESTful。 此外，有⼀款RESTFUL接⼝的⽂档在线⾃动⽣成+功能测试功能软件⸺Swager UI，具体配置过程可 移步《Spring Bot 利⽤ Swager 实现restful测试》


1 2 3 4 5 6 7 8 9

packagecn.no7player.controler;

mpor cn.no7player.model.User; import com.wordnik.swager.anotations.ApiOperation ; import org.springframework.web.bind.anotation.Path Variable; import org.springframework.web.bind.anotation.Requ estMaping; import org.springframework.web.bind.anotation.Requ estMethod; import org.springframework.web.bind.anotation.Rest Controler;

0 1 2 3 4 5 6 7 8

19 0 1 2 3 4 5 6 7 8

i rtaa.ti.ArayList; import java.util.List;

estControler @RequestMaping(value="/users") publi clasSwagerControler{

/

htp:/localhost:8080/swager/index.html

29 0 1 2 3 4 5 6 7 8

*/

@ApiOperation(value="Get al users",notes="requires noting")

@RequestMaping(method=RequestMethod. GET)

publicList<User>getUsers(){ List<User>list=newArayList<User>(); User user=newUser(); user.setName("helo"); list.ad(user);

39

- 0
- 1
- 2


User user2=newUser(); user.setName("world"); list.ad(user2); returnlist;

43

} @ApiOperation(value="Get userwith

id",notes="requires the idof user")

@RequestMaping(value="/{name}",method= RequestMethod.GET)

publicUser getUserById(@PathVariable

Stringname){ User user=newUser(); user.setName("helo world"); returnuser;

} }

- 5.Mybatis 配置相关代码在Aplication.java中体现。


- （1）【aplication.properties】

注意，在Aplication.java代码中，配置DataSource时的注解 @ConfigurationProperties(prefix=“spring.datasource”) 表示将根据前缀“spring.datasource”从aplication.properties中匹配相关属性值。

- （2）【UserMaper.xml】 Mybatis的sql映射⽂件。Mybatis同样⽀持注解⽅式，在此不予举例了。

- （3）接⼝UserMaper


<table>
  <tr>
    <th>1<br>2<br>3<br>4<br></th>
    <th>spring.datasource.url=jdbc:mysql:/127.0.0.1  3 06/test? useUnicode=true&characterEncoding=gbk&zer oDateTimeBehavior=convertToNul srn.atasoure.username=rot spr .daasor .pasword=123456 spring.datasource.driver-clas-</th>
  </tr>
</table>


name=com.mysql.jdbc.Driver

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br></th>
    <th>?xml version="1.0"encoding="UTF-8"?> <!DOCTYPE maper PUBLIC"-/mybatis.org/DTD Maper 3.0/EN"htp:/mybatis.org/dtd/mybatis-3ma er.dtd"> <maper namespace="cn.no7player.maper.UserMaper "><br><br><select id="findUserInfo"resultType="cn.no7player.mod el.User"><br><br>select name,age,pasword from user; </select></th>
  </tr>
</table>


</maper>

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br></th>
    <th>packagecn.no7player.maper; import cn.no7player.model.User; publicinterfaceUserMaper{<br><br>publicUser findUserInfo();</th>
  </tr>
</table>


7 }

三、总结

- （1）运⾏ Aplication.java

- （2）控制台输出：


![image 3](<基于SpringBoot + Mybatis实现SpringMVC Web项目【原创】.note_images/imageFile3.png>)

….（略过⽆数内容）

![image 4](<基于SpringBoot + Mybatis实现SpringMVC Web项目【原创】.note_images/imageFile4.png>)

- （3）访问： 针对三种控制器的访问分别为： 视图：


htp:/localhost:8080/helo?name=7player

![image 5](<基于SpringBoot + Mybatis实现SpringMVC Web项目【原创】.note_images/imageFile5.png>)

Json：

htp:/localhost:8080/getUserInfo

![image 6](<基于SpringBoot + Mybatis实现SpringMVC Web项目【原创】.note_images/imageFile6.png>)

Restful（使⽤了swager）：

htp:/localhost:8080/swager/index.html

![image 7](<基于SpringBoot + Mybatis实现SpringMVC Web项目【原创】.note_images/imageFile7.png>)

四、参阅 《Spring Bot – Quick Start》

htp:/projects.spring.io/spring-bot/#quick-start

《mybatis》

htp:/mybatis.github.io/mybatis-3/

《使⽤ Spring Bot 快速构建 Spring 框架应⽤》

htp:/ w.ibm.com/developerworks/cn/java/j-lo-spring-bot/

《Using @ConfigurationProperties in Spring Bot》

htp:/ w.javacodegeks.com/2014/09/using-configurationproperties-in-spring-bot.html?utm_s ource=tuicol

《Springbot-Mybatis-Mysample》

htps:/github.com/mizukyf/springbot-mybatis-mysample

《Serving Web Content with Spring MVC》

htp:/spring.io/guides/gs/serving-web-content/

《理解RESTful架构》

htp:/ w.ruanyifeng.com/blog/201/09/restful

附录： Spring Bot 推荐的基础 POM ⽂件

<table>
  <tr>
    <th>名称</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>spring-bot-starter</td>
    <td>核⼼ POM，包含⾃动配置⽀持、⽇志库和 配置⽂件的⽀持。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-amqp</td>
    <td>对 YAML 通过 spring-rabit ⽀持 AMQP。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-aop</td>
    <td>包含 spring-aop 和 AspectJ 来⽀持⾯向切⾯编程 ）。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-batch</td>
    <td>（AOP ⽀持 Spring Batch，包含 HSQLDB。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-data-jpa</td>
    <td>包含 spring-data-jpa、spring。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-data-mongodb</td>
    <td>orm 和 Hibernate 来⽀持 JPA 包含 spring-data-mongodb 来⽀持 MongoDB。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-data-rest</td>
    <td>通过 spring-data-rest-webmvc ⽀持以 REST ⽅ 仓库。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-jdbc</td>
    <td>式暴露 Spring Data ⽀持使⽤ JDBC 访问数据库。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-security</td>
    <td>包含 spring-security。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-test</td>
    <td>包含常⽤的测试所需的依赖，如 JUnit、</td>
  </tr>
  <tr>
    <td>spring-bot-starter-velocity</td>
    <td>Hamcrest、Mockito 和 spring-test 等。 ⽀持使⽤ Velocity 作为模板引擎。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-web</td>
    <td>⽀持 Web 应⽤开发，包含 Tomcat 和 spring。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-websocket</td>
    <td>mvc ⽀持使⽤ Tomcat 开发 WebSocket 应⽤。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-ws</td>
    <td>⽀持 Spring Web Services。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-actuator</td>
    <td>添加适⽤于⽣产环境的功能，如性能指标和监测 等功能。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-remote-shel</td>
    <td>添加远程 SH ⽀持。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-jety</td>
    <td>使⽤ Jety ⽽不是默认的 Tomcat 作为应⽤服务 器。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-log4j</td>
    <td>添加 Log4j 的⽀持。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-loging</td>
    <td>使⽤ Spring Bot 默认的⽇志框架 Logback。</td>
  </tr>
  <tr>
    <td>spring-bot-starter-tomcat</td>
    <td>使⽤ Spring Bot 默认的 Tomcat 作为应⽤服务 器。</td>
  </tr>
</table>


