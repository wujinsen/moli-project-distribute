htp:/ w.qb520.com/article/4763.html

# SpringBoot项⽬War包部署⽆法注册到Nacos中

注： 其实标题的描述不是很准确，准确的说是已经注册成功并且可以正常访问，但是在服务列表却看 不到。

问题

最近在进⾏Eureka迁移Nacos架构升级的时候，发现有两个之前的旧项⽬，虽然也是SpringBoot项 ⽬，但是启动⽅式是通过外置Tomcat启动的。就在项⽬改造完成后，发现启动正常，访问也正常，但 就是在nacos客户端服务列表中看不到这个服务。

解决⽅案

我们从Nacos的注册类NacosAutoServiceRegistration 进去之后可以发现它继承了SpringCloud的 AbstractAutoServiceRegistration 注册类，在AbstractAutoServiceRegistration 中有⼀个绑定监 听事件，他的作⽤就是监听到内置容器启动完成之后获取容器端⼝向注册中⼼注册，如下图：

![image 1](<War包部署无法注册到Nacos.note_images/imageFile1.png>)

因为这个接⼝只可以监听内置容器，所以我们就可以得出结论：之所以会出现上⾯的问题，就是因为 使⽤外部容器时，不会触发监听事件，所以也就注册不到nacos中。 因此我们可以借助SpringBoot提供的ApplicationRunner接⼝，这个接⼝的作⽤就是在应⽤启动完成之 后执⾏⼀些定义好的初始化操作。所以我们可以在服务启动成功之后，通过这个接⼝将我们的项⽬注 册到Nacos中，下⾯看代码

/**

- * @author shy

- * @date 2021/11/29 16:23

- */ @Component public class NacosConfig implements ApplicationRunner {


@Autowired(required = false) private NacosAutoServiceRegistration registration;

@Value("${server.port}") Integer port;

@Override public void run(ApplicationArguments args) {

if (registration != null && port != null) { //如果getTomcatPort()端⼝获取异常,就采⽤配置⽂件中配置的端⼝ Integer tomcatPort = port; try {

tomcatPort = new Integer(getTomcatPort()); } catch (Exception e) {

e.printStackTrace();

} registration.setPort(tomcatPort); registration.start();

} }

/**

- * 获取外置tomcat端⼝

- */ public String getTomcatPort() throws Exception {


MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer(); Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),

Query.match(Query.attr("protocol"), Query.value("HTTP/1.1"))); String port = objectNames.iterator().next().getKeyProperty("port"); return port;

} }

加上这个配置类启动之后，我们就可以在Nacos客户端服务列表中看到相应的服务

# SpringBoot项⽬war包部署及出现的问题

Failed to bind properties under 'mybatis.configuration.mapped-statements[0].

- 1.修改pom⽂件 修改打包⽅式 为war； 添加tomcat使⽤范围，provided的意思即在发布的时候有外部提供，内置的tomcat就不会打包进去


<groupId>com.school</groupId> <artifactId>daniel</artifactId> <version>0.0.1-SNAPSHOT</version> <name>daniel</name> <description>student information project for Spring Boot</description> <!--打包⽅式，发布时使⽤此项--> <packaging>war</packaging>

<properties>

<java.version>1.8</java.version> </properties>

<dependencies> <!--需要发布发war包时使⽤--> <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-tomcat</artifactId> <scope>provided</scope>

</dependency>

## 2.在启动类或者配置类中继承SpringBootServletInitializer

如果需要打war包部署，需要继承此类，重写configure⽅法。

@SpringBootApplication public class DanielApplication extends SpringBootServletInitializer {

protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) { return builder.sources(DanielApplication.class);

} public static void main(String[] args) {

SpringApplication.run(DanielApplication.class, args); }

}

注意： 如果使⽤的springboot最新的版本则会报，我当时⽤的是

spring-boot-starter-parent：2.2.0.RELEASE

mybatis-spring-boot-starter：2.1.1

修改为下⾯的版本问题解决，通过查询资料发现现在的最新版本springboot与mybatis兼容性存在问 题，到后⾯升级后应该就没有问题了。

Failed to bind properties under 'mybatis.configuration.mapped-statements[0].parameter-map.parameter-…

<parent> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-parent</artifactId> <!--<version>2.2.0.RELEASE</version>--> <version>2.1.5.RELEASE</version> <relativePath/> <!-- lookup parent from repository -->

</parent> <dependency> <groupId>org.mybatis.spring.boot</groupId> <artifactId>mybatis-spring-boot-starter</artifactId> <!-- <version>2.1.1</version>--> <version>2.0.1</version>

</dependency>

附：以上的问题都已解决，但是我的项⽬中使⽤了shiro框架，在外置tomcat部署时，⽆法使⽤shiro。 没有改版本的时候使⽤idea是正常的… 以上为个⼈经验，希望能给⼤家⼀个参考，也希望⼤家多多⽀持。

