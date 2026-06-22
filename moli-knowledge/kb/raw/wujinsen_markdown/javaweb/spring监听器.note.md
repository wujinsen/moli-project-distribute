<!- 使⽤ContextLoaderListener配置，指定spring配置⽂件位置 -> <context-param>

<param-name>contextConfigLocation</param-name> <param-value>claspath:spring-mybatis.xml</param-value>

</context-param>

<!- SpringMVC前端控制器 ->

<！ - DispatcherServlet载⼊后，它从xml⽂件中载⼊Spring的应⽤上下⽂，该XML⽂件的名字取 决于<servlet-name>->

<servlet> <servlet-name>SpringMVC</servlet-name> <servlet-clas>org.springframework.web.servlet.DispatcherServlet</servlet-clas> <init-param>

<param-name>contextConfigLocation</param-name> <param-value>claspath:spring-mvc.xml</param-value>

</init-param> <load-on-startup>1</load-on-startup> <async-suported>true</async-suported>

</servlet> <servlet-maping>

<servlet-name>SpringMVC</servlet-name> <url-patern>/</url-patern>

</servlet-maping>

