package hello;

import org.springframework.web.bind.annotation.RestController; import org.springframework.web.bind.annotation.RequestMapping;

@RestController public class HelloController {

@RequestMapping("/") public String index() {

return "Greetings from Spring Boot!"; }

}

HelloController类上标注了注解@RestControler，这意味着可以使⽤ SpringMVC来处理Web请 求。 @RestControler集合了 @Controler和@ResponseBody两种注解

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner; import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication; import org.springframework.context.ApplicationContext; import org.springframework.context.annotation.Bean;

@SpringBootApplication public class Application {

public static void main(String[] args) {

SpringApplication.run(Application.class, args); }

@Bean public CommandLineRunner commandLineRunner(ApplicationContext ctx) {

return args -> {

System.out.println("Let's inspect the beans provided by Spring Boot:");

String[] beanNames = ctx.getBeanDefinitionNames(); Arrays.sort(beanNames); for (String beanName : beanNames) {

System.out.println(beanName); }

}; }

0 }

@SpringBootApplication注解⾮常⽅便，它包含了以下注解： @Configuration:标记该类作为资源Bean，定义应⽤的上下⽂环境 @EnableAutoConfiguration:通知SpringBoot开始添加基于classpath配置的beans，其他的 beans，各种property配置 @EnableWebMvc:通常会⼿动添加这个注解开启SpringMVC应⽤，但SpringBoot发现classpath 上有spring-webmvc会⾃动添加该注解。该注解标记应⽤程序成为⼀个web应⽤并激活关键⾏为，相 当于设置了DispatcherServlet。 @ComponentScan:它会通知Spring扫描hello包下的其他组件、配置⽂件和服务，也可以找到 controller 通过运⾏main()下的SpringApplication.run()来启动应⽤程序，你会发现没有XML⽂件了，这 是⼀个100%纯java的Web应⽤。

@Bean注解 ComandLineRuner()⽅法上标记了@Bean注解并在启动时运⾏。它会检索所有beans，包括应⽤程 序⾥创建的或是⾃动添加到SpringBot的。该⽅法会排序和输出这些beans。

import static org.hamcrest.Matchers.equalTo; import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content; import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status; import org.junit.Test; import org.junit.runner.RunWith; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockM vc; import org.springframework.boot.test.context.SpringBootTest; import org.springframework.http.MediaType; import org.springframework.test.context.junit4.SpringRunner; import org.springframework.test.web.servlet.MockMvc; import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class) @SpringBootTest @AutoConfigureMockMvc public class HelloControllerTest {

@Autowired private MockMvc mvc;

@Test public void getHello() throws Exception {

mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_J SON))

.andExpect(status().isOk())

.andExpect(content().string(equalTo("Greetings from Spring Boot!")));

}

} MockMvc来源于SpringTest，它能允许你⽅便的创建类，它会模拟发送HTTP请求到 DispatcherServlet，为返回的结果设置断⾔。 请注意需要使⽤这两个注解@AutoConfigureMockMvc @SpringBootTest来注⼊MockMvc对象。 使⽤@SpringBootTest注解相当于我们请求整个应⽤程序来创建 。另⼀种是使⽤@WebMvcTest通 知SpringBoot创建只有Web层的上下⽂环境。这两种情况下SpringBoot会⾃动定位你的应⽤⾥⾯的 主应⽤类，你也可以覆盖它，或者窄化它，如果你想为此创建不同的东⻄。

import static org.hamcrest.Matchers.equalTo; import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.Before; import org.junit.Test; import org.junit.runner.RunWith; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.context.embedded.LocalServerPort; import org.springframework.boot.test.context.SpringBootTest; import org.springframework.boot.test.web.client.TestRestTemplate; import org.springframework.http.ResponseEntity; import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class) @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) public class HelloControllerIT {

@LocalServerPort private int port;

private URL base;

@Autowired private TestRestTemplate template;

@Before public void setUp() throws Exception {

this.base = new URL("http://localhost:" + port + "/"); }

@Test public void getHello() throws Exception { ResponseEntity<String> response = template.getForEntity(base.toString(),

String.class);

assertThat(response.getBody(), equalTo("Greetings from Spring Boot!")); }

} 内嵌服务器启动时会通过webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT⽣成随机端⼝号，⽽端⼝号是运⾏时通过 @LocalServerPort⽣成

